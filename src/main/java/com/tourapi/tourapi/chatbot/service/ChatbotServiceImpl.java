package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.ChatMessage;
import com.tourapi.tourapi.chatbot.dto.ChatHistoryResponse;
import com.tourapi.tourapi.chatbot.dto.ChatRequest;
import com.tourapi.tourapi.chatbot.dto.ChatResponse;
import com.tourapi.tourapi.chatbot.dto.ChatSessionListResponse;
import com.tourapi.tourapi.chatbot.repository.ChatMessageRepository;
import com.tourapi.tourapi.common.exception.chatbot.ChatbotHandler;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotErrorStatus;
import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final GeminiApiService geminiApiService;

    @Override
    @Transactional
    public ChatResponse chat(Long memberId, ChatRequest request) {
        Member member = getMemberById(memberId);

        // 세션 ID가 없으면 새로 생성
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = createNewSession(memberId);
        }

        // 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .member(member)
                .sessionId(sessionId)
                .messageType(ChatMessage.MessageType.USER)
                .content(request.getMessage().trim())
                .isActive(true)
                .build();

        ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);

        // 대화 컨텍스트 조회
        List<String> conversationHistory = null;
        if (Boolean.TRUE.equals(request.getIncludeContext())) {
            conversationHistory = getConversationHistory(memberId, sessionId);
        }

        // Gemini API 호출
        String botResponse = geminiApiService.generateResponse(request.getMessage(), conversationHistory);

        // 봇 응답 저장
        ChatMessage botMessage = ChatMessage.builder()
                .member(member)
                .sessionId(sessionId)
                .messageType(ChatMessage.MessageType.BOT)
                .content(botResponse)
                .isActive(true)
                .build();

        ChatMessage savedBotMessage = chatMessageRepository.save(botMessage);

        log.info("Chat completed for member {} in session {}", memberId, sessionId);

        return ChatResponse.from(savedUserMessage, savedBotMessage);
    }

    @Override
    public ChatHistoryResponse getChatHistory(Long memberId, String sessionId) {
        Member member = getMemberById(memberId);

        List<ChatMessage> messages = chatMessageRepository
                .findByMemberIdAndSessionIdAndIsActiveTrueOrderByCreatedAtAsc(memberId, sessionId);

        if (messages.isEmpty()) {
            throw new ChatbotHandler(ChatbotErrorStatus.CHAT_SESSION_NOT_FOUND);
        }

        log.info("Retrieved {} messages for member {} in session {}", messages.size(), memberId, sessionId);

        return ChatHistoryResponse.from(sessionId, messages);
    }

    @Override
    public ChatSessionListResponse getChatSessions(Long memberId) {
        Member member = getMemberById(memberId);

        List<String> sessionIds = chatMessageRepository
                .findDistinctSessionIdsByMemberIdOrderByLatestMessageDesc(memberId);

        List<ChatSessionListResponse.SessionSummary> sessionSummaries = sessionIds.stream()
                .map(sessionId -> {
                    long messageCount = chatMessageRepository
                            .countByMemberIdAndSessionIdAndIsActiveTrue(memberId, sessionId);

                    // 세션의 마지막 메시지 조회
                    List<ChatMessage> recentMessages = chatMessageRepository
                            .findRecentMessagesBySession(memberId, sessionId, PageRequest.of(0, 1));

                    String lastMessage = recentMessages.isEmpty() ? "" : recentMessages.get(0).getContent();

                    return ChatSessionListResponse.SessionSummary.create(
                            sessionId,
                            lastMessage,
                            recentMessages.isEmpty() ? null : recentMessages.get(0).getCreatedAt(),
                            (int) messageCount
                    );
                })
                .collect(Collectors.toList());

        log.info("Retrieved {} chat sessions for member {}", sessionSummaries.size(), memberId);

        return ChatSessionListResponse.from(sessionSummaries);
    }

    @Override
    public String createNewSession(Long memberId) {
        Member member = getMemberById(memberId);
        String sessionId = "session_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        log.info("Created new chat session {} for member {}", sessionId, memberId);
        return sessionId;
    }

    @Override
    @Transactional
    public void deleteSession(Long memberId, String sessionId) {
        Member member = getMemberById(memberId);

        List<ChatMessage> messages = chatMessageRepository
                .findByMemberIdAndSessionIdAndIsActiveTrueOrderByCreatedAtAsc(memberId, sessionId);

        if (messages.isEmpty()) {
            throw new ChatbotHandler(ChatbotErrorStatus.CHAT_SESSION_NOT_FOUND);
        }

        // 모든 메시지를 논리 삭제
        messages.forEach(ChatMessage::deactivate);
        chatMessageRepository.saveAll(messages);

        log.info("Deleted chat session {} for member {} ({} messages)", sessionId, memberId, messages.size());
    }

    @Override
    @Transactional
    public void deleteMessage(Long memberId, Long messageId) {
        Member member = getMemberById(memberId);

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatbotHandler(ChatbotErrorStatus.CHAT_MESSAGE_NOT_FOUND));

        // 메시지 소유권 확인
        if (!message.getMember().getId().equals(memberId)) {
            throw new ChatbotHandler(ChatbotErrorStatus.CHAT_MESSAGE_ACCESS_DENIED);
        }

        if (!message.getIsActive()) {
            throw new ChatbotHandler(ChatbotErrorStatus.CHAT_MESSAGE_ALREADY_DELETED);
        }

        message.deactivate();
        chatMessageRepository.save(message);

        log.info("Deleted chat message {} for member {}", messageId, memberId);
    }

    @Override
    public long getTotalMessageCount(Long memberId) {
        Member member = getMemberById(memberId);
        return chatMessageRepository.countByMemberIdAndIsActiveTrue(memberId);
    }

    // Private helper methods
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }

    private List<String> getConversationHistory(Long memberId, String sessionId) {
        List<ChatMessage> recentMessages = chatMessageRepository
                .findRecentMessagesBySession(memberId, sessionId, PageRequest.of(0, 10));

        return recentMessages.stream()
                .map(msg -> {
                    String role = msg.isUserMessage() ? "사용자" : "봇";
                    return role + ": " + msg.getContent();
                })
                .collect(Collectors.toList());
    }
}