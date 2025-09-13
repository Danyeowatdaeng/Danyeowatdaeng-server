package com.tourapi.tourapi.common.exception.wishlist.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum WishlistErrorStatus implements ErrorResponse {

    @ExplainError("찜하기를 찾을 수 없음")
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "WISHLIST4001", "찜하기를 찾을 수 없습니다."),

    @ExplainError("이미 찜한 관광지")
    ALREADY_ADDED_TO_WISHLIST(HttpStatus.CONFLICT, "WISHLIST4002", "이미 찜한 관광지입니다."),

    @ExplainError("찜하기 접근 권한 없음")
    WISHLIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WISHLIST4003", "찜하기에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 찜하기")
    WISHLIST_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "WISHLIST4004", "이미 삭제된 찜하기입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WishlistErrorStatus(HttpStatus httpStatus, String code, String message) {
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