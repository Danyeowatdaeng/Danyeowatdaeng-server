package com.tourapi.tourapi.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourapi.tourapi.chatbot.config.GeminiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiApiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate geminiRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResponse(String userMessage, List<String> conversationHistory) {
        try {
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    geminiConfig.getBaseUrl(),
                    geminiConfig.getModel(),
                    geminiConfig.getApiKey());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = buildRequestBody(userMessage, conversationHistory);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to Gemini API: {}", url);
            ResponseEntity<String> response = geminiRestTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return extractContentFromResponse(response.getBody());
            } else {
                log.error("Gemini API request failed with status: {}", response.getStatusCode());
                return "죄송합니다. 현재 응답을 생성할 수 없습니다. 잠시 후 다시 시도해주세요.";
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "죄송합니다. 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }

    private Map<String, Object> buildRequestBody(String userMessage, List<String> conversationHistory) {
        Map<String, Object> requestBody = new HashMap<>();

        // 시스템 프롬프트와 대화 히스토리를 조합하여 컨텍스트 생성
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(geminiConfig.getSystemPrompt()).append("\n\n");

        // 대화 히스토리 추가 (최근 5개 메시지만)
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            contextBuilder.append("최근 대화 내역:\n");
            int startIndex = Math.max(0, conversationHistory.size() - 5);
            for (int i = startIndex; i < conversationHistory.size(); i++) {
                contextBuilder.append(conversationHistory.get(i)).append("\n");
            }
            contextBuilder.append("\n");
        }

        contextBuilder.append("사용자 질문: ").append(userMessage);

        // Gemini API 요청 구조
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", contextBuilder.toString());
        content.put("parts", List.of(part));

        requestBody.put("contents", List.of(content));

        // 생성 설정
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", geminiConfig.getTemperature());
        generationConfig.put("maxOutputTokens", geminiConfig.getMaxTokens());
        requestBody.put("generationConfig", generationConfig);

        // 안전 설정
        List<Map<String, Object>> safetySettings = List.of(
                Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
        );
        requestBody.put("safetySettings", safetySettings);

        return requestBody;
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    String text = firstPart.path("text").asText();

                    if (!text.isEmpty()) {
                        return text.trim();
                    }
                }
            }

            log.warn("No content found in Gemini response: {}", responseBody);
            return "죄송합니다. 응답을 처리하는 중 문제가 발생했습니다.";

        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return "죄송합니다. 응답을 처리하는 중 문제가 발생했습니다.";
        }
    }
}