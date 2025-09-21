package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import java.util.List;

public interface GeminiApiService {

    /**
     * Gemini API를 호출하여 응답 생성
     */
    String generateResponse(String message, List<ChatMessage> conversationHistory, String model);

    /**
     * 토큰 수 계산 (근사치)
     */
    Integer estimateTokenCount(String text);

    /**
     * 모델 유효성 검증
     */
    boolean isValidModel(String model);
}