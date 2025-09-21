package com.tourapi.tourapi.chatbot.dto;

import com.tourapi.tourapi.chatbot.domain.ChatConversation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ConversationHistoryResponse {

    @Schema(description = "대화방 ID")
    private Long id;

    @Schema(description = "대화방 제목")
    private String title;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "메시지 목록")
    private List<ChatResponse> messages;

    public static ConversationHistoryResponse from(ChatConversation conversation) {
        return ConversationHistoryResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .messages(conversation.getMessages().stream()
                        .map(ChatResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}