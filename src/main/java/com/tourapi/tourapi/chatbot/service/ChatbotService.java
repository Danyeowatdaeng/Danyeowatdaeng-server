package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.domain.ChatConversation;
import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import com.tourapi.tourapi.chatbot.dto.ChatRequest;

public interface ChatbotService {

    /**
     * 챗봇과 대화
     */
    ChatMessage chat(Long memberId, ChatRequest request);

    /**
     * 대화방 기록 조회 (메시지 포함)
     */
    ChatConversation getChatHistory(Long memberId);

    /**
     * 대화방 삭제
     */
    void deleteChatHistory(Long memberId);
}