package com.example.psyaihealer.multiagent;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class MultiAgentService {

    private final Random random = new Random();

    public AgentResult process(String userMessage) {
        String scene = buildScene(userMessage);
        String challenge = challengeCognition(userMessage);
        String guide = positiveGuide(userMessage);
        List<String> options = List.of(scene, challenge, guide);
        String selected = options.get(random.nextInt(options.size()));
        String strategy = selected.equals(scene) ? "场景构建" : selected.equals(challenge) ? "认知挑战" : "积极引导";
        return new AgentResult(selected, strategy);
    }

    private String buildScene(String message) {
        return "我理解你现在的处境：" + message + "。让我们先停下来，描述一下此刻的环境和感受。";
    }

    private String challengeCognition(String message) {
        return "当你想到\"" + message + "\"时，是否有证据支持或反驳这些想法？让我们一起寻找更平衡的视角。";
    }

    private String positiveGuide(String message) {
        return "谢谢你分享\"" + message + "\"。试着想一件最近让你感到被支持或感激的小事，我们一起把注意力带到这些积极体验上。";
    }

    public record AgentResult(String reply, String strategy) {}
}
