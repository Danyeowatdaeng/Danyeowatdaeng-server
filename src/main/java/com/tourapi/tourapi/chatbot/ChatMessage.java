package com.tourapi.tourapi.chatbot;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_message_member", columnList = "memberId"),
                @Index(name = "idx_chat_message_session", columnList = "sessionId"),
                @Index(name = "idx_chat_message_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON 형태로 추가 정보 저장

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum MessageType {
        USER,    // 사용자 메시지
        BOT      // 봇 응답
    }

    // 비즈니스 메서드
    public void deactivate() {
        this.isActive = false;
    }

    public boolean isUserMessage() {
        return MessageType.USER.equals(this.messageType);
    }

    public boolean isBotMessage() {
        return MessageType.BOT.equals(this.messageType);
    }
}