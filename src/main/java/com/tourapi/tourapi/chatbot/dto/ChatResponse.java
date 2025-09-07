package com.tourapi.tourapi.chatbot.dto;

import com.tourapi.tourapi.chatbot.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatResponse {

    @Schema(description = "메시지 ID")
    private Long messageId;

    @Schema(description = "세션 ID")
    private String sessionId;

    @Schema(description = "사용자 메시지")
    private String userMessage;

    @Schema(description = "봇 응답")
    private String botResponse;

    @Schema(description = "응답 생성 시간")
    private LocalDateTime responseTime;

    @Schema(description = "토큰 사용량 (선택적)")
    private Integer tokensUsed;

    public static ChatResponse from(String sessionId, String userMessage, String botResponse, Long messageId) {
        return ChatResponse.builder()
                .messageId(messageId)
                .sessionId(sessionId)
                .userMessage(userMessage)
                .botResponse(botResponse)
                .responseTime(LocalDateTime.now())
                .build();
    }

    public static ChatResponse from(ChatMessage userMsg, ChatMessage botMsg) {
        return ChatResponse.builder()
                .messageId(botMsg.getId())
                .sessionId(botMsg.getSessionId())
                .userMessage(userMsg.getContent())
                .botResponse(botMsg.getContent())
                .responseTime(botMsg.getCreatedAt())
                .build();
    }
}