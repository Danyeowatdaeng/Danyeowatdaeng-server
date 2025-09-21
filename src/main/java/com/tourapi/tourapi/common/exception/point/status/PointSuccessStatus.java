package com.tourapi.tourapi.common.exception.point.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum PointSuccessStatus implements SuccessResponse {

    POINT_EARNED(HttpStatus.OK, "POINT2001", "포인트가 성공적으로 적립되었습니다."),
    POINT_SPENT(HttpStatus.OK, "POINT2002", "포인트가 성공적으로 사용되었습니다."),
    POINT_HISTORY_FOUND(HttpStatus.OK, "POINT2003", "포인트 내역을 성공적으로 조회했습니다."),
    POINT_BALANCE_FOUND(HttpStatus.OK, "POINT2004", "포인트 잔액을 성공적으로 조회했습니다."),
    POINT_SUMMARY_FOUND(HttpStatus.OK, "POINT2005", "포인트 요약 정보를 성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PointSuccessStatus(HttpStatus httpStatus, String code, String message) {
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