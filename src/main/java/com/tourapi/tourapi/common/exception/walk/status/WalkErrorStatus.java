package com.tourapi.tourapi.common.exception.walk.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum WalkErrorStatus implements ErrorResponse {

    @ExplainError("산책 기록을 찾을 수 없음")
    WALK_NOT_FOUND(HttpStatus.NOT_FOUND, "WALK4001", "산책 기록을 찾을 수 없습니다."),

    @ExplainError("산책 기록 접근 권한 없음")
    WALK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WALK4003", "산책 기록에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 산책 기록")
    WALK_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "WALK4004", "이미 삭제된 산책 기록입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WalkErrorStatus(HttpStatus httpStatus, String code, String message) {
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