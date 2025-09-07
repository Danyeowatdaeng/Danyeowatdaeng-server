package com.tourapi.tourapi.chatbot.dto;

import com.tourapi.tourapi.chatbot.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatHistoryResponse {

    private String sessionId;
    private List<MessageDto> messages;
    private int totalMessages;
    private LocalDateTime sessionStartTime;
    private LocalDateTime lastMessageTime;

    @Getter
    @Builder
    public static class MessageDto {
        private Long id;
        private ChatMessage.MessageType type;
        private String content;
        private LocalDateTime timestamp;

        public static MessageDto from(ChatMessage message) {
            return MessageDto.builder()
                    .id(message.getId())
                    .type(message.getMessageType())
                    .content(message.getContent())
                    .timestamp(message.getCreatedAt())
                    .build();
        }
    }

    public static ChatHistoryResponse from(String sessionId, List<ChatMessage> messages) {
        List<MessageDto> messageDtos = messages.stream()
                .map(MessageDto::from)
                .toList();

        LocalDateTime sessionStart = messages.isEmpty() ? null :
                messages.stream()
                        .map(ChatMessage::getCreatedAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(null);

        LocalDateTime lastMessage = messages.isEmpty() ? null :
                messages.stream()
                        .map(ChatMessage::getCreatedAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);

        return ChatHistoryResponse.builder()
                .sessionId(sessionId)
                .messages(messageDtos)
                .totalMessages(messages.size())
                .sessionStartTime(sessionStart)
                .lastMessageTime(lastMessage)
                .build();
    }
}