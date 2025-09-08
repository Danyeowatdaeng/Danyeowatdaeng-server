package com.tourapi.tourapi.common.exception.review.status;

import org.springframework.http.HttpStatus;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;

public enum ReviewErrorStatus implements ErrorResponse {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "REVIEW4001", "요청 파라미터가 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW4004", "리뷰를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW4003", "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REVIEW5000", "리뷰 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ReviewErrorStatus(HttpStatus httpStatus, String code, String message) {
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


