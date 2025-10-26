package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import com.tourapi.tourapi.chatbot.enums.MessageRole;
import com.tourapi.tourapi.chatbot.service.GeminiApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiApiServiceImpl implements GeminiApiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final Set<String> SUPPORTED_MODELS = Set.of(
            "gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash", "gemini-flash-latest"
    );

    @Override
    public String generateResponse(String message, List<ChatMessage> conversationHistory, String model) {
        if (!isValidModel(model)) {
            model = "gemini-2.5-flash"; // 기본 모델
        }

        try {
            String endpoint = String.format("%s/models/%s:generateContent?key=%s", baseUrl, model, apiKey);

            // 요청 바디 구성
            Map<String, Object> requestBody = buildRequestBody(message, conversationHistory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling Gemini API: model={}, endpoint={}", model, endpoint.replaceAll("key=[^&]+", "key=***"));

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, Map.class);

            return extractResponseText((Map<String, Object>) response.getBody());

        } catch (Exception e) {
            log.error("Gemini API call failed: model={}, error={}", model, e.getMessage());
            throw new RuntimeException("AI 응답 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public Integer estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 한국어 기준 대략적인 토큰 수 계산 (1토큰 ≈ 2-3글자)
        return (int) Math.ceil(text.length() / 2.5);
    }

    @Override
    public boolean isValidModel(String model) {
        return model != null && SUPPORTED_MODELS.contains(model);
    }

    private Map<String, Object> buildRequestBody(String currentMessage, List<ChatMessage> history) {
        List<Map<String, Object>> contents = new ArrayList<>();

        // 시스템 프롬프트 추가
        String systemPrompt = """
            당신은 친근하고 도움이 되는 여행 어시스턴트입니다. 
            한국의 반려동물 동반 여행 정보를 전문으로 하며, 
            사용자의 여행 계획과 반려동물 관련 질문에 정확하고 유용한 정보를 제공합니다.
            답변은 한국어로 하고, 친근하고 이해하기 쉽게 설명해주세요.
            """;

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", systemPrompt))
        ));

        // 대화 히스토리 추가 (최근 10개 메시지만)
        List<ChatMessage> recentHistory = history.stream()
                .skip(Math.max(0, history.size() - 10))
                .collect(Collectors.toList());

        for (ChatMessage msg : recentHistory) {
            String role = msg.getRole() == MessageRole.USER ? "user" : "model";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        // 현재 메시지 추가
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", currentMessage))
        ));

        return Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.8,
                        "maxOutputTokens", 2048
                ),
                "safetySettings", List.of(
                        Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_MEDIUM_AND_ABOVE"),
                        Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_MEDIUM_AND_ABOVE")
                )
        );
    }

    @SuppressWarnings("unchecked")
    private String extractResponseText(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("응답에 candidates가 없습니다");
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("응답에 text가 없습니다");
            }

            String text = (String) parts.get(0).get("text");
            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException("빈 응답입니다");
            }

            return text.trim();

        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", e.getMessage());
            log.error("응답 본문: {}", responseBody);
            throw new RuntimeException("AI 응답 파싱에 실패했습니다", e);
        }
    }
}