package com.example.psyaihealer.llm;

import com.example.psyaihealer.therapy.MessageSender;
import com.example.psyaihealer.therapy.TherapyMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TherapyLlmService {

    private static final Logger log = LoggerFactory.getLogger(TherapyLlmService.class);

    private final LlmProperties properties;
    private final OpenAiCompatibleClient client;

    public TherapyLlmService(LlmProperties properties, OpenAiCompatibleClient client) {
        this.properties = properties;
        this.client = client;
    }

    @PostConstruct
    void logConfigSummary() {
        boolean keyPresent = properties.getApiKey() != null && !properties.getApiKey().isBlank();
        log.info("Therapy LLM config: enabled={}, baseUrl={}, model={}, keyPresent={}, header={}",
                properties.isEnabled(),
                properties.getBaseUrl(),
                properties.getModel(),
                keyPresent,
                properties.getApiKeyHeader());
        if (!properties.isEnabled() && keyPresent) {
            log.warn("检测到已配置 app.ai.apiKey 但 app.ai.enabled=false，将不会调用真实大模型，将降级为占位策略。请设置 APP_AI_ENABLED=true 或 app.ai.enabled=true");
        }
    }

    public LlmResult generateReply(String topic, List<TherapyMessage> history) {
        if (!properties.isEnabled()) {
            log.debug("LLM 未启用（app.ai.enabled=false），跳过真实大模型调用");
            return null;
        }

        List<OpenAiCompatibleClient.ChatMessage> messages = new ArrayList<>();
        messages.add(new OpenAiCompatibleClient.ChatMessage(
                "system",
                buildSystemPrompt(topic)
        ));

        if (history != null && !history.isEmpty()) {
            List<TherapyMessage> trimmed = trimHistory(history, properties.getHistoryLimit());
            for (TherapyMessage m : trimmed) {
                if (m.getSender() == MessageSender.USER) {
                    messages.add(new OpenAiCompatibleClient.ChatMessage("user", safeContent(m.getContent())));
                } else if (m.getSender() == MessageSender.AGENT) {
                    messages.add(new OpenAiCompatibleClient.ChatMessage("assistant", safeContent(m.getContent())));
                }
            }
        }

        String reply = client.chatCompletion(messages);
        if (reply == null || reply.isBlank()) {
            log.warn("LLM 未返回有效回复（可能是未配置key/鉴权失败/模型名错误/网络问题），将触发降级占位策略");
            return null;
        }

        return new LlmResult(reply.trim(), "LLM");
    }

    private static String safeContent(String content) {
        return content == null ? "" : content;
    }

    private static List<TherapyMessage> trimHistory(List<TherapyMessage> history, int limit) {
        if (limit <= 0 || history.size() <= limit) {
            return history;
        }
        return history.subList(Math.max(0, history.size() - limit), history.size());
    }

    private static String buildSystemPrompt(String topic) {
        String t = (topic == null || topic.isBlank()) ? "情绪疏导" : topic;
        return "你是一名心理支持型 AI 助手，进行同理、倾听、提问与可操作的建议（如呼吸训练、情绪命名、认知重评、行为激活）。\n"
                + "会话主题：" + t + "。\n"
                + "要求：\n"
                + "1) 避免下诊断结论，不冒充医生，不提供处方或危险指令。\n"
                + "2) 如果用户提到自伤/他伤/急性危机，优先建议联系当地紧急电话/就近医院/可信任的人并寻求专业帮助。\n"
                + "3) 回复使用中文，尽量简洁，优先用 3-6 句 + 1-3 条可执行建议。";
    }

    public record LlmResult(String reply, String strategy) {
    }
}
