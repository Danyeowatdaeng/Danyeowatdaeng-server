package com.tourapi.tourapi.common.exception.point.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum PointErrorStatus implements ErrorResponse {

    @ExplainError("포인트 내역을 찾을 수 없음")
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "POINT4001", "포인트 내역을 찾을 수 없습니다."),

    @ExplainError("보유 포인트 부족")
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "POINT4002", "보유 포인트가 부족합니다."),

    @ExplainError("이미 오늘 포인트를 적립함")
    ALREADY_EARNED_TODAY(HttpStatus.BAD_REQUEST, "POINT4003", "오늘 이미 해당 활동으로 포인트를 적립했습니다."),

    @ExplainError("잘못된 포인트 금액")
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "POINT4004", "포인트 금액이 올바르지 않습니다."),

    @ExplainError("포인트 처리 중 오류 발생")
    POINT_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "POINT5001", "포인트 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PointErrorStatus(HttpStatus httpStatus, String code, String message) {
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