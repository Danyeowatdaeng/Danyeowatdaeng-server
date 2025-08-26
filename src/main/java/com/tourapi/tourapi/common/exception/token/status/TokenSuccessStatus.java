package com.tourapi.tourapi.common.exception.token.status;


import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum TokenSuccessStatus implements ErrorResponse, SuccessResponse {

    REFRESH_SUCCESS(HttpStatus.OK, "COMMON2002", "새로운 액세스 토큰이 성공적으로 갱신되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TokenSuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() { return httpStatus; }

    @Override
    public HttpStatus getSuccessStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
