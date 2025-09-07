package com.tourapi.tourapi.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatSessionListResponse {

    private List<SessionSummary> sessions;
    private int totalSessions;

    @Getter
    @Builder
    public static class SessionSummary {
        private String sessionId;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private int messageCount;
        private String preview; // 대화 미리보기 (처음 몇 글자)

        public static SessionSummary create(String sessionId, String lastMessage,
                                            LocalDateTime lastMessageTime, int messageCount) {
            String preview = lastMessage != null && lastMessage.length() > 50
                    ? lastMessage.substring(0, 47) + "..."
                    : lastMessage;

            return SessionSummary.builder()
                    .sessionId(sessionId)
                    .lastMessage(lastMessage)
                    .lastMessageTime(lastMessageTime)
                    .messageCount(messageCount)
                    .preview(preview)
                    .build();
        }
    }

    public static ChatSessionListResponse from(List<SessionSummary> sessions) {
        return ChatSessionListResponse.builder()
                .sessions(sessions)
                .totalSessions(sessions.size())
                .build();
    }
}