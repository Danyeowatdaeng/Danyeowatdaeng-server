package com.tourapi.tourapi.chatbot.dto;

import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import com.tourapi.tourapi.chatbot.enums.MessageRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatResponse {

    @Schema(description = "메시지 ID")
    private Long messageId;

    @Schema(description = "대화방 ID")
    private Long conversationId;

    @Schema(description = "메시지 역할")
    private MessageRole role;

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "사용된 모델")
    private String model;

    @Schema(description = "토큰 수")
    private Integer tokenCount;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    public static ChatResponse from(ChatMessage message) {
        return ChatResponse.builder()
                .messageId(message.getId())
                .conversationId(message.getConversation().getId())
                .role(message.getRole())
                .content(message.getContent())
                .model(message.getModel())
                .tokenCount(message.getTokenCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}