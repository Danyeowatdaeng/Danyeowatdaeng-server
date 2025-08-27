package com.tourapi.tourapi.common.exception.terms.status;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum TermsErrorStatus implements ErrorResponse {

    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS4000", "약관을 찾을 수 없습니다."),
    TERMS_ALREADY_AGREED(HttpStatus.BAD_REQUEST, "TERMS4001", "이미 동의한 약관입니다."),
    TERMS_AGREEMENT_REQUIRED(HttpStatus.BAD_REQUEST, "TERMS4002", "필수 약관 동의가 필요합니다."),
    TERMS_VERSION_MISMATCH(HttpStatus.BAD_REQUEST, "TERMS4003", "약관 버전이 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TermsErrorStatus(HttpStatus httpStatus, String code, String message) {
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
