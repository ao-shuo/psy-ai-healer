package com.example.psyaihealer.llm;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class LlmProperties {

    /**
     * 是否启用真实 LLM。
     * 关闭时，疗愈会话会降级为内置的 MultiAgentService 占位实现。
     */
    private boolean enabled = false;

    /**
     * OpenAI-compatible API 基址，例如：https://api.openai.com/v1
     * 某些厂商会是 https://xxx/v1 或 https://xxx/openai/v1
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * API Key（建议通过环境变量 APP_AI_API_KEY 注入，不要写入仓库）。
     */
    private String apiKey;

    /**
     * 鉴权 Header 名。
     * - OpenAI: Authorization
     * - 部分厂商: X-API-Key
     */
    private String apiKeyHeader = "Authorization";

    /**
     * 当 apiKeyHeader=Authorization 时使用的 scheme，默认 Bearer。
     */
    private String authorizationScheme = "Bearer";

    /**
     * 模型名，例如：gpt-4o-mini / deepseek-chat / qwen-turbo 等（取决于你的供应商）。
     */
    private String model = "gpt-4o-mini";

    /**
     * 生成温度。
     */
    private double temperature = 0.7;

    /**
     * 最大输出 token（不同厂商字段可能不同；此处按 OpenAI-compatible 的 max_tokens）。
     */
    @Min(1)
    private int maxTokens = 512;

    /**
     * 带入上下文的历史消息条数（仅 user/assistant）。
     */
    @Min(0)
    private int historyLimit = 20;

    public boolean isEnabled() {
        String env = firstNonBlank(System.getenv("APP_AI_ENABLED"), System.getenv("DEEPSEEK_AI_ENABLED"));
        if (env != null) {
            return Boolean.parseBoolean(env.trim());
        }
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        String env = firstNonBlank(System.getenv("APP_AI_BASE_URL"), System.getenv("DEEPSEEK_BASE_URL"));
        return env != null ? env : baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        String env = firstNonBlank(System.getenv("APP_AI_API_KEY"), System.getenv("DEEPSEEK_API_KEY"));
        return env != null ? env : apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    public String getAuthorizationScheme() {
        return authorizationScheme;
    }

    public void setAuthorizationScheme(String authorizationScheme) {
        this.authorizationScheme = authorizationScheme;
    }

    public String getModel() {
        String env = firstNonBlank(System.getenv("APP_AI_MODEL"), System.getenv("DEEPSEEK_MODEL"));
        return env != null ? env : model;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getHistoryLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(int historyLimit) {
        this.historyLimit = historyLimit;
    }
}
