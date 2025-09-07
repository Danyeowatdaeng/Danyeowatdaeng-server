package com.tourapi.tourapi.common.exception.map.status;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum MapErrorStatus implements ErrorResponse {

    EXTERNAL_API_FAILURE(HttpStatus.BAD_GATEWAY, "MAP4000", "외부 관광 API 호출에 실패했습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "MAP4001", "요청 파라미터가 올바르지 않습니다."),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP4004", "해당 범위 내 위치를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MapErrorStatus(HttpStatus httpStatus, String code, String message) {
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


