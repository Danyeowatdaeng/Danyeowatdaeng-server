package com.tourapi.tourapi.chatbot.repository;

import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 대화방의 메시지 목록 조회 (시간순)
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}