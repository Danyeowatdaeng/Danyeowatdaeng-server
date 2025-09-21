package com.tourapi.tourapi.common.exception.chatbot.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum ChatbotErrorStatus implements ErrorResponse {

    @ExplainError("대화방을 찾을 수 없음")
    CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATBOT4001", "대화방을 찾을 수 없습니다."),

    @ExplainError("AI API 호출 실패")
    AI_API_ERROR(HttpStatus.BAD_GATEWAY, "CHATBOT5001", "AI 서비스 호출에 실패했습니다."),

    @ExplainError("지원하지 않는 모델")
    UNSUPPORTED_MODEL(HttpStatus.BAD_REQUEST, "CHATBOT4002", "지원하지 않는 AI 모델입니다."),

    @ExplainError("메시지가 너무 길음")
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "CHATBOT4005", "메시지가 너무 깁니다."),

    @ExplainError("대화 히스토리가 너무 길음")
    CONVERSATION_TOO_LONG(HttpStatus.BAD_REQUEST, "CHATBOT4006", "대화가 너무 깁니다. 새 대화를 시작해주세요.");

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