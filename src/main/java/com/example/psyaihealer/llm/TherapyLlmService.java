package com.example.psyaihealer.llm;

import com.example.psyaihealer.profile.UserProfile;
import com.example.psyaihealer.profile.UserProfileInsights;
import com.example.psyaihealer.profile.UserProfileService;
import com.example.psyaihealer.therapy.MessageSender;
import com.example.psyaihealer.therapy.TherapyMessage;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final UserProfileService profileService;
    private final ObjectMapper objectMapper;

    public TherapyLlmService(LlmProperties properties,
                            OpenAiCompatibleClient client,
                            UserProfileService profileService,
                            ObjectMapper objectMapper) {
        this.properties = properties;
        this.client = client;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
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

    public LlmResult generateReply(String topic, List<TherapyMessage> history, UserProfile profile) {
        if (!properties.isEnabled()) {
            log.debug("LLM 未启用（app.ai.enabled=false），跳过真实大模型调用");
            return null;
        }

        List<OpenAiCompatibleClient.ChatMessage> messages = new ArrayList<>();
        String profileContext = "";
        try {
            profileContext = profileService.buildPromptContext(profile);
        } catch (Exception ignored) {
            profileContext = "";
        }
        messages.add(new OpenAiCompatibleClient.ChatMessage(
                "system",
                buildSystemPrompt(topic, profileContext)
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

    /**
     * Extract incremental user-profile insights from recent conversation.
     * Conservative by design: only update when the conversation supports it.
     */
    public UserProfileInsights extractProfileInsights(String topic, List<TherapyMessage> history, UserProfile profile) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (history == null || history.isEmpty()) {
            return null;
        }

        List<OpenAiCompatibleClient.ChatMessage> messages = new ArrayList<>();
        String profileContext = "";
        try {
            profileContext = profileService.buildPromptContext(profile);
        } catch (Exception ignored) {
            profileContext = "";
        }

        messages.add(new OpenAiCompatibleClient.ChatMessage(
                "system",
                buildProfileExtractorSystemPrompt(topic, profileContext)
        ));

        List<TherapyMessage> trimmed = trimHistory(history, Math.max(8, properties.getHistoryLimit()));
        for (TherapyMessage m : trimmed) {
            if (m.getSender() == MessageSender.USER) {
                messages.add(new OpenAiCompatibleClient.ChatMessage("user", safeContent(m.getContent())));
            } else if (m.getSender() == MessageSender.AGENT) {
                // Include assistant replies as context, but extractor should prioritize user statements.
                messages.add(new OpenAiCompatibleClient.ChatMessage("assistant", safeContent(m.getContent())));
            }
        }

        String raw = client.chatCompletion(messages);
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            String json = extractFirstJsonObject(raw);
            if (json == null || json.isBlank()) {
                return null;
            }
            return objectMapper.readValue(json, UserProfileInsights.class);
        } catch (Exception e) {
            log.debug("Profile insight parse failed: {}", e.getMessage());
            return null;
        }
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

    private static String buildSystemPrompt(String topic, String profileContext) {
        String t = (topic == null || topic.isBlank()) ? "情绪疏导" : topic;
        String ctx = (profileContext == null) ? "" : profileContext.trim();
        return "你是一名心理支持型 AI 助手，进行同理、倾听、提问与可操作的建议（如呼吸训练、情绪命名、认知重评、行为激活）。\n"
                + "会话主题：" + t + "。\n"
                + (ctx.isBlank() ? "" : (ctx + "\n"))
                + "要求：\n"
                + "1) 避免下诊断结论，不冒充医生，不提供处方或危险指令。\n"
                + "2) 基于用户画像进行‘循循善诱’：先共情复述→提出1个澄清问题→给1-3条可执行的小步骤建议。\n"
                + "3) 若画像显示风险较高（如PHQ-9较高/情绪极低）或用户提到自伤/他伤/急性危机：优先建议联系当地紧急电话/就近医院/可信任的人并寻求专业帮助。\n"
                + "4) 回复使用中文，尽量简洁，优先用 3-6 句 + 1-3 条可执行建议。";
    }

    private static String buildProfileExtractorSystemPrompt(String topic, String profileContext) {
        String t = (topic == null || topic.isBlank()) ? "情绪疏导" : topic;
        String ctx = (profileContext == null) ? "" : profileContext.trim();
        return "你是一个‘用户画像提取器’，目标是在聊天过程中，基于用户表达提炼出可用于后续支持性对话的画像要素。\n"
                + "会话主题：" + t + "。\n"
                + (ctx.isBlank() ? "" : ("当前画像摘要（供参考，不要盲目复述）：\n" + ctx + "\n"))
                + "规则：\n"
                + "1) 必须保守：不要猜测、不要编造；没有把握就输出空字符串。\n"
                + "2) 只提炼可行动/可沟通的信息：目标、触发点、偏好语气、有效的应对方式。\n"
                + "3) 可以提炼‘沟通风格/互动偏好/性格倾向’但必须以‘推测’呈现，且要提供证据（来自用户原话的短引用或摘要）。\n"
                + "4) 不输出敏感诊断标签，不把用户当作患病者。\n"
                + "5) 输出必须是严格 JSON 对象，不要 markdown，不要多余文字。\n"
                + "JSON Schema：\n"
                + "{\n"
                + "  \"preferredTone\": \"\",\n"
                + "  \"goals\": \"\",\n"
                + "  \"triggers\": \"\",\n"
                + "  \"copingPreferences\": \"\",\n"
                + "  \"communicationStyle\": \"\",\n"
                + "  \"personalityNotes\": \"\",\n"
                + "  \"evidence\": \"\",\n"
                + "  \"confidence\": 0.0\n"
                + "}\n"
                + "字段要求：\n"
                + "- preferredTone：例如 温和/鼓励/直接/理性；\n"
                + "- goals/triggers/copingPreferences：用简短要点，建议用‘- ’开头的多行文本；每个字段不超过 300 字。\n"
                + "- communicationStyle：例如 直接/含蓄/需要结构化步骤/更偏倾诉/更偏解决问题；不超过 120 字。\n"
                + "- personalityNotes：一句话‘推测’总结，不超过 120 字（例如：‘可能更在意自我要求、倾向反复确认’，注意用“可能/看起来/倾向于”等措辞）。\n"
                + "- evidence：来自用户表达的 1-3 条证据（短引用或摘要），建议用‘- ’多行；不超过 300 字。\n"
                + "- confidence：0~1，表示你对提炼的把握程度。";
    }

    private static String extractFirstJsonObject(String raw) {
        if (raw == null) return null;
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            return null;
        }
        return raw.substring(start, end + 1);
    }

    public record LlmResult(String reply, String strategy) {
    }
}
