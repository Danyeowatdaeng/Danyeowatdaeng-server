package com.tourapi.tourapi.common.exception.chatbot.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum ChatbotSuccessStatus implements SuccessResponse {

    CHAT_SUCCESS(HttpStatus.OK, "CHATBOT2001", "챗봇 응답이 성공적으로 생성되었습니다."),
    CHAT_HISTORY_FOUND(HttpStatus.OK, "CHATBOT2002", "대화 기록을 성공적으로 조회했습니다."),
    CHAT_HISTORY_DELETED(HttpStatus.OK, "CHATBOT2003", "대화 기록이 성공적으로 삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ChatbotSuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getSuccessStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}