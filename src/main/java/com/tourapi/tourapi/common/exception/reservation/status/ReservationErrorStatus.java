package com.tourapi.tourapi.common.exception.reservation.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum ReservationErrorStatus implements ErrorResponse {

    @ExplainError("예약을 찾을 수 없음")
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION4001", "예약을 찾을 수 없습니다."),

    @ExplainError("예약 접근 권한 없음")
    RESERVATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "RESERVATION4002", "예약에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 예약")
    RESERVATION_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "RESERVATION4003", "이미 삭제된 예약입니다."),

    @ExplainError("유효하지 않은 예약 데이터")
    INVALID_RESERVATION_DATA(HttpStatus.BAD_REQUEST, "RESERVATION4004", "유효하지 않은 예약 데이터입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ReservationErrorStatus(HttpStatus httpStatus, String code, String message) {
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

