package com.tourapi.tourapi.web.controller.chatbot;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.chatbot.dto.ChatHistoryResponse;
import com.tourapi.tourapi.chatbot.dto.ChatRequest;
import com.tourapi.tourapi.chatbot.dto.ChatResponse;
import com.tourapi.tourapi.chatbot.dto.ChatSessionListResponse;
import com.tourapi.tourapi.chatbot.service.ChatbotService;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotErrorStatus;
import com.tourapi.tourapi.common.exception.chatbot.status.ChatbotSuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Chatbot", description = "AI 챗봇 API")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    @Operation(summary = "챗봇과 대화", description = "Gemini AI를 사용하여 챗봇과 대화합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = ChatbotErrorStatus.class, codes = {"CHATBOT4001", "CHATBOT4002"})
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        ChatResponse response = chatbotService.chat(principal.getId(), request);

        log.info("Chat completed for member {} in session {}", principal.getId(), response.getSessionId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_RESPONSE_GENERATED, response);
    }

    @GetMapping("/sessions")
    @Operation(summary = "채팅 세션 목록 조회", description = "사용자의 모든 채팅 세션 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<ChatSessionListResponse>> getChatSessions(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        ChatSessionListResponse response = chatbotService.getChatSessions(principal.getId());

        log.info("Retrieved {} chat sessions for member {}", response.getTotalSessions(), principal.getId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_SESSIONS_FOUND, response);
    }

    @GetMapping("/sessions/{sessionId}/history")
    @Operation(summary = "채팅 히스토리 조회", description = "특정 세션의 채팅 히스토리를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = ChatbotErrorStatus.class, codes = {"CHATBOT4003"})
    public ResponseEntity<ApiResponse<ChatHistoryResponse>> getChatHistory(
            @Parameter(description = "세션 ID") @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        ChatHistoryResponse response = chatbotService.getChatHistory(principal.getId(), sessionId);

        log.info("Retrieved chat history for member {} in session {}: {} messages",
                principal.getId(), sessionId, response.getTotalMessages());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_HISTORY_FOUND, response);
    }

    @PostMapping("/sessions/new")
    @Operation(summary = "새 채팅 세션 시작", description = "새로운 채팅 세션을 시작합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<String>> createNewSession(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        String sessionId = chatbotService.createNewSession(principal.getId());

        log.info("Created new chat session {} for member {}", sessionId, principal.getId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_SESSION_CREATED, sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "채팅 세션 삭제", description = "특정 채팅 세션을 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = ChatbotErrorStatus.class, codes = {"CHATBOT4003"})
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @Parameter(description = "세션 ID") @PathVariable String sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        chatbotService.deleteSession(principal.getId(), sessionId);

        log.info("Deleted chat session {} for member {}", sessionId, principal.getId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_SESSION_DELETED);
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "메시지 삭제", description = "특정 메시지를 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = ChatbotErrorStatus.class, codes = {"CHATBOT4004", "CHATBOT4005", "CHATBOT4006"})
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @Parameter(description = "메시지 ID") @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        chatbotService.deleteMessage(principal.getId(), messageId);

        log.info("Deleted message {} for member {}", messageId, principal.getId());
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_MESSAGE_DELETED);
    }

    @GetMapping("/stats")
    @Operation(summary = "채팅 통계 조회", description = "사용자의 채팅 통계를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Long>> getChatStats(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        long totalMessages = chatbotService.getTotalMessageCount(principal.getId());

        log.info("Retrieved chat stats for member {}: {} total messages", principal.getId(), totalMessages);
        return ApiResponse.onSuccess(ChatbotSuccessStatus.CHAT_STATS_FOUND, totalMessages);
    }
}