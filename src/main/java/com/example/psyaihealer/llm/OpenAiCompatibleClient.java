package com.example.psyaihealer.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class OpenAiCompatibleClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleClient.class);

    private final LlmProperties properties;

    public OpenAiCompatibleClient(LlmProperties properties) {
        this.properties = properties;
    }

    public String chatCompletion(List<ChatMessage> messages) {
        if (!properties.isEnabled()) {
            return null;
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            log.warn("LLM 已启用但未配置 app.ai.apiKey（或环境变量 APP_AI_API_KEY），将降级为占位实现。");
            return null;
        }

        RestClient restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        ChatCompletionRequest request = new ChatCompletionRequest(
                properties.getModel(),
                messages,
                properties.getTemperature(),
                properties.getMaxTokens()
        );

        try {
            log.info("Calling LLM: baseUrl={}, model={}, messages={}",
                trimTrailingSlash(properties.getBaseUrl()),
                properties.getModel(),
                messages == null ? 0 : messages.size());
            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .headers(headers -> applyAuth(headers))
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return null;
            }
            ChatCompletionChoice first = response.choices().get(0);
            if (first == null || first.message() == null) {
                return null;
            }
            return first.message().content();
        } catch (Exception ex) {
            // 不要输出 apiKey
            log.warn("调用 LLM 失败：{}", ex.getMessage());
            return null;
        }
    }

    private void applyAuth(HttpHeaders headers) {
        String headerName = properties.getApiKeyHeader();
        String apiKey = properties.getApiKey();
        if (headerName == null || headerName.isBlank() || apiKey == null || apiKey.isBlank()) {
            return;
        }

        if ("Authorization".equalsIgnoreCase(headerName)) {
            String scheme = properties.getAuthorizationScheme();
            String value = (scheme == null || scheme.isBlank()) ? apiKey : (scheme.trim() + " " + apiKey.trim());
            headers.set(HttpHeaders.AUTHORIZATION, value);
        } else {
            headers.set(headerName, apiKey);
        }
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    public record ChatMessage(String role, String content) {
        public ChatMessage {
            Objects.requireNonNull(role, "role");
            Objects.requireNonNull(content, "content");
        }
    }

    public record ChatCompletionRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            Integer max_tokens
    ) {
        public ChatCompletionRequest {
            if (messages == null) {
                messages = new ArrayList<>();
            }
        }
    }

    public record ChatCompletionResponse(List<ChatCompletionChoice> choices) {
    }

    public record ChatCompletionChoice(ChatCompletionMessage message) {
    }

    public record ChatCompletionMessage(String role, String content) {
    }
}
