package com.tourapi.tourapi.common.exception.terms.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum TermsSuccessStatus implements SuccessResponse {

    TERMS_FOUND(HttpStatus.OK, "TERMS2000", "약관이 조회되었습니다."),
    TERMS_AGREED(HttpStatus.OK, "TERMS2001", "약관에 동의하였습니다."),
    AGREEMENT_STATUS_FOUND(HttpStatus.OK, "TERMS2002", "약관 동의 상태가 조회되었습니다."),
    SIGNUP_COMPLETED(HttpStatus.OK, "TERMS2003", "회원가입이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TermsSuccessStatus(HttpStatus httpStatus, String code, String message) {
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
