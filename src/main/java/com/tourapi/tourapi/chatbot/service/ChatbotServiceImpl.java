package com.tourapi.tourapi.chatbot.service;

import com.tourapi.tourapi.chatbot.domain.ChatConversation;
import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import com.tourapi.tourapi.chatbot.dto.ChatRequest;
import com.tourapi.tourapi.chatbot.enums.MessageRole;
import com.tourapi.tourapi.chatbot.repository.ChatConversationRepository;
import com.tourapi.tourapi.chatbot.repository.ChatMessageRepository;
import com.tourapi.tourapi.common.exception.chatbot.ChatbotHandler;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotErrorStatus;
import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final GeminiApiService geminiApiService;

    @Override
    public ChatMessage chat(Long memberId, ChatRequest request) {
        Member member = getMemberById(memberId);

        // 기존 대화방 조회 또는 새 대화방 생성
        List<ChatConversation> conversations = conversationRepository.findLatestByMemberId(memberId);
        ChatConversation conversation = conversations.isEmpty() 
                ? createNewConversation(member)
                : conversations.get(0);

        // 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(request.getMessage())
                .model(request.getModel())
                .tokenCount(geminiApiService.estimateTokenCount(request.getMessage()))
                .build();
        messageRepository.save(userMessage);

        // 대화 히스토리 조회
        List<ChatMessage> conversationHistory = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        try {
            // Gemini API 호출
            String aiResponse = geminiApiService.generateResponse(
                    request.getMessage(),
                    conversationHistory,
                    request.getModel()
            );

            // AI 응답 저장
            ChatMessage assistantMessage = ChatMessage.builder()
                    .conversation(conversation)
                    .role(MessageRole.ASSISTANT)
                    .content(aiResponse)
                    .model(request.getModel())
                    .tokenCount(geminiApiService.estimateTokenCount(aiResponse))
                    .build();

            ChatMessage savedMessage = messageRepository.save(assistantMessage);

            log.info("Chat completed: memberId={}, conversationId={}, model={}",
                    memberId, conversation.getId(), request.getModel());

            return savedMessage;

        } catch (Exception e) {
            log.error("Gemini API call failed: memberId={}, error={}", memberId, e.getMessage());
            throw new ChatbotHandler(ChatbotErrorStatus.AI_API_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversation getChatHistory(Long memberId) {
        List<ChatConversation> conversations = conversationRepository.findLatestByMemberId(memberId);
        if (conversations.isEmpty()) {
            return null; // 대화 기록이 없으면 null 반환
        }
        
        ChatConversation conversation = conversations.get(0);
        return conversationRepository.findByIdAndMemberIdWithMessages(conversation.getId(), memberId)
                .orElse(null);
    }

    @Override
    public void deleteChatHistory(Long memberId) {
        List<ChatConversation> conversations = conversationRepository.findLatestByMemberId(memberId);
        if (!conversations.isEmpty()) {
            ChatConversation conversation = conversations.get(0);
            conversation.setDeleted(true);
            conversationRepository.save(conversation);
            log.info("Chat history deleted: memberId={}, conversationId={}", memberId, conversation.getId());
        }
    }

    private ChatConversation createNewConversation(Member member) {
        ChatConversation conversation = ChatConversation.builder()
                .member(member)
                .title("새로운 대화")
                .deleted(false)
                .build();
        return conversationRepository.save(conversation);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
}