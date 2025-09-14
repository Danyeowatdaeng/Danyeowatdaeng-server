package com.tourapi.tourapi.common.exception.mypet.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum MyPetErrorStatus implements ErrorResponse {

    @ExplainError("다이어리를 찾을 수 없음")
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPET4001", "다이어리를 찾을 수 없습니다."),

    @ExplainError("다이어리 접근 권한 없음")
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MYPET4003", "다이어리에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 다이어리")
    DIARY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "MYPET4004", "이미 삭제된 다이어리입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MyPetErrorStatus(HttpStatus httpStatus, String code, String message) {
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