package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.dto.ChatHistoryResponse;
import com.tourapi.tourapi.chatbot.dto.ChatRequest;
import com.tourapi.tourapi.chatbot.dto.ChatResponse;
import com.tourapi.tourapi.chatbot.dto.ChatSessionListResponse;

public interface ChatbotService {

    // 챗봇 대화
    ChatResponse chat(Long memberId, ChatRequest request);

    // 특정 세션의 대화 히스토리 조회
    ChatHistoryResponse getChatHistory(Long memberId, String sessionId);

    // 사용자의 모든 세션 목록 조회
    ChatSessionListResponse getChatSessions(Long memberId);

    // 새 세션
    String createNewSession(Long memberId);

    // 세션 삭제
    void deleteSession(Long memberId, String sessionId);

    // 특정 메시지 삭제 (논리 삭제)
    void deleteMessage(Long memberId, Long messageId);

    // 사용자의 총 메시지 수 조회
    long getTotalMessageCount(Long memberId);
}