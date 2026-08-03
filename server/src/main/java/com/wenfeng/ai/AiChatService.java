package com.wenfeng.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * 调用任意 OpenAI 兼容的 chat/completions 接口（智谱 GLM / 硅基流动 / Groq 等）。
 */
@Service
public class AiChatService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int maxTokens;
    private final String systemPrompt;

    public AiChatService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
            @Value("${app.ai.base-url}") String baseUrl, @Value("${app.ai.api-key}") String apiKey,
            @Value("${app.ai.model}") String model, @Value("${app.ai.max-tokens}") int maxTokens,
            @Value("${app.ai.system-prompt}") String systemPrompt) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String chat(String userMessage) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userMessage)));

        String response = restClient.post()
                .uri(baseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new IllegalStateException("大模型返回格式异常");
            }
            return content.asText().trim();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("大模型返回格式异常", e);
        }
    }
}
