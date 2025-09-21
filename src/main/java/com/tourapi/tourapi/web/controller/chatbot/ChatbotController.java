package com.tourapi.tourapi.web.controller.chatbot;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.chatbot.domain.ChatConversation;
import com.tourapi.tourapi.chatbot.domain.ChatMessage;
import com.tourapi.tourapi.chatbot.dto.*;
import com.tourapi.tourapi.chatbot.service.ChatbotService;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotErrorStatus;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotSuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chatbot", description = "AI 챗봇 대화 API")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    @Operation(summary = "챗봇과 대화", description = "Gemini AI와 대화합니다. 자동으로 대화방이 생성/관리됩니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    @ApiErrorCodeExample(value = ChatbotErrorStatus.class, codes = {"CHATBOT5001", "CHATBOT4002"})
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        ChatMessage message = chatbotService.chat(memberId, request);
        ChatResponse response = ChatResponse.from(message);

        log.info("Chat completed: memberId={}, conversationId={}, messageId={}",
                memberId, response.getConversationId(), response.getMessageId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_SUCCESS, response);
    }

    @GetMapping("/history")
    @Operation(summary = "대화 기록 조회", description = "내 대화 기록을 모든 메시지와 함께 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<ConversationHistoryResponse>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        ChatConversation conversation = chatbotService.getChatHistory(memberId);

        if (conversation == null) {
            // 대화 기록이 없는 경우 빈 응답
            ConversationHistoryResponse emptyResponse = ConversationHistoryResponse.builder()
                    .id(null)
                    .title("대화 기록이 없습니다")
                    .messages(java.util.Collections.emptyList())
                    .build();
            return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_HISTORY_FOUND, emptyResponse);
        }

        ConversationHistoryResponse response = ConversationHistoryResponse.from(conversation);

        log.info("Chat history retrieved: memberId={}, conversationId={}, messageCount={}",
                memberId, conversation.getId(), conversation.getMessages().size());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_HISTORY_FOUND, response);
    }

    @DeleteMapping("/history")
    @Operation(summary = "대화 기록 삭제", description = "내 대화 기록을 모두 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Void>> deleteChatHistory(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        chatbotService.deleteChatHistory(memberId);

        log.info("Chat history deleted: memberId={}", memberId);
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_HISTORY_DELETED);
    }
}