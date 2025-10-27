package com.tourapi.tourapi.common.exception.reservation.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum ReservationSuccessStatus implements SuccessResponse {

    RESERVATION_ADDED(HttpStatus.OK, "RESERVATION2001", "예약이 성공적으로 추가되었습니다."),
    RESERVATION_REMOVED(HttpStatus.OK, "RESERVATION2002", "예약이 성공적으로 삭제되었습니다."),
    RESERVATION_RETRIEVED(HttpStatus.OK, "RESERVATION2003", "예약 내역이 성공적으로 조회되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ReservationSuccessStatus(HttpStatus httpStatus, String code, String message) {
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

