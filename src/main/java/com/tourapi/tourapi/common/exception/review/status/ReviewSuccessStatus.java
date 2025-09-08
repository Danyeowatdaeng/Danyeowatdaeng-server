package com.tourapi.tourapi.common.exception.review.status;

import org.springframework.http.HttpStatus;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;

public enum ReviewSuccessStatus implements SuccessResponse {

    LIST_SUCCESS(HttpStatus.OK, "REVIEW2000", "리뷰 조회가 성공했습니다."),
    CREATE_SUCCESS(HttpStatus.OK, "REVIEW2001", "리뷰 작성이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ReviewSuccessStatus(HttpStatus httpStatus, String code, String message) {
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


