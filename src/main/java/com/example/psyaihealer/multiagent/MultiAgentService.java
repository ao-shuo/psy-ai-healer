package com.example.psyaihealer.multiagent;

import com.example.psyaihealer.profile.UserProfile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MultiAgentService {

    public AgentResult process(String userMessage) {
        return process(userMessage, null);
    }

    public AgentResult process(String userMessage, UserProfile profile) {
        String tone = profile != null && profile.getPreferredTone() != null && !profile.getPreferredTone().isBlank()
                ? profile.getPreferredTone().trim()
                : "温和";

        String contextHint = buildContextHint(profile);
        List<AgentResult> options = List.of(
                new AgentResult(buildScene(userMessage, tone, contextHint), "场景构建"),
                new AgentResult(challengeCognition(userMessage, tone, contextHint), "认知挑战"),
                new AgentResult(positiveGuide(userMessage, tone, contextHint), "积极引导")
        );
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    private String buildScene(String message, String tone, String contextHint) {
        return prefix(tone)
                + contextHint
                + "我理解你现在的处境：" + message + "。我们先慢一点：此刻你身体的感觉（呼吸/胸口/胃）更像哪一种？再用 1-2 个词给情绪命名。";
    }

    private String challengeCognition(String message, String tone, String contextHint) {
        return prefix(tone)
                + contextHint
                + "当你想到\"" + message + "\"时，你脑中最强烈的那个‘自动想法’是什么？它的证据支持/反驳分别有哪些？我们一起把它改写成一个更平衡、更可承受的说法。";
    }

    private String positiveGuide(String message, String tone, String contextHint) {
        return prefix(tone)
                + contextHint
                + "谢谢你分享\"" + message + "\"。如果把今天当成‘只做一件小事就算赢’，你最愿意尝试的 10 分钟行动是什么（喝水/出门走一圈/整理桌面/给自己做顿简单饭）？";
    }

    private static String prefix(String tone) {
        // Keep it short; tone is user preference.
        if (tone == null || tone.isBlank()) {
            return "";
        }
        return "（语气：" + tone + "）\n";
    }

    private static String buildContextHint(UserProfile profile) {
        if (profile == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (profile.getLastPhq9Score() != null) {
            sb.append("（参考：最近PHQ-9 ").append(profile.getLastPhq9Score()).append("分）\n");
        }
        if (profile.getLastMoodScore() != null) {
            sb.append("（参考：最近心情 ").append(profile.getLastMoodScore()).append("/10）\n");
        }
        return sb.toString();
    }

    public record AgentResult(String reply, String strategy) {}
}
