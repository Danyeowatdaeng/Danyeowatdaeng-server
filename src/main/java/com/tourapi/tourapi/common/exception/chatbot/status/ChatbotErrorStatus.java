package com.tourapi.tourapi.common.exception.chatbot.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum ChatbotErrorStatus implements ErrorResponse {

    @ExplainError("메시지가 비어있음")
    CHAT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "CHATBOT4001", "메시지를 입력해주세요."),

    @ExplainError("메시지가 너무 김")
    CHAT_MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "CHATBOT4002", "메시지는 1000자 이하여야 합니다."),

    @ExplainError("채팅 세션을 찾을 수 없음")
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATBOT4003", "채팅 세션을 찾을 수 없습니다."),

    @ExplainError("메시지를 찾을 수 없음")
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATBOT4004", "메시지를 찾을 수 없습니다."),

    @ExplainError("메시지 접근 권한 없음")
    CHAT_MESSAGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHATBOT4005", "해당 메시지에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 메시지")
    CHAT_MESSAGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "CHATBOT4006", "이미 삭제된 메시지입니다."),

    @ExplainError("AI API 호출 실패")
    AI_API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT5001", "AI 서비스에 일시적인 문제가 발생했습니다."),

    @ExplainError("AI API 응답 파싱 실패")
    AI_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT5002", "AI 응답을 처리하는 중 문제가 발생했습니다."),

    @ExplainError("AI API 키 설정 오류")
    AI_API_KEY_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT5003", "AI 서비스 설정에 문제가 있습니다."),

    @ExplainError("메시지 길이 제한 초과")
    CONVERSATION_CONTEXT_TOO_LARGE(HttpStatus.BAD_REQUEST, "CHATBOT4007", "대화 내용이 너무 길어 처리할 수 없습니다."),

    @ExplainError("세션 생성 실패")
    SESSION_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHATBOT5004", "새 세션을 생성할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ChatbotErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}