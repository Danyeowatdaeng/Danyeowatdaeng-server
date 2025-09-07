package com.tourapi.tourapi.common.exception.chatbot.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum ChatbotSuccessStatus implements SuccessResponse {

    CHAT_RESPONSE_GENERATED(HttpStatus.OK, "CHATBOT2001", "챗봇 응답이 성공적으로 생성되었습니다."),
    CHAT_SESSIONS_FOUND(HttpStatus.OK, "CHATBOT2002", "채팅 세션 목록을 성공적으로 조회했습니다."),
    CHAT_HISTORY_FOUND(HttpStatus.OK, "CHATBOT2003", "채팅 히스토리를 성공적으로 조회했습니다."),
    CHAT_SESSION_CREATED(HttpStatus.CREATED, "CHATBOT2004", "새 채팅 세션이 성공적으로 생성되었습니다."),
    CHAT_SESSION_DELETED(HttpStatus.OK, "CHATBOT2005", "채팅 세션이 성공적으로 삭제되었습니다."),
    CHAT_MESSAGE_DELETED(HttpStatus.OK, "CHATBOT2006", "메시지가 성공적으로 삭제되었습니다."),
    CHAT_STATS_FOUND(HttpStatus.OK, "CHATBOT2007", "채팅 통계를 성공적으로 조회했습니다.");

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