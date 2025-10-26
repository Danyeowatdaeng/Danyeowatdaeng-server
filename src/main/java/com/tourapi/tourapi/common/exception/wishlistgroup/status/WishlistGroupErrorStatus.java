package com.tourapi.tourapi.common.exception.wishlistgroup.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum WishlistGroupErrorStatus implements ErrorResponse {

    @ExplainError("찜하기 그룹을 찾을 수 없음")
    WISHLIST_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "WISHLISTGROUP4001", "찜하기 그룹을 찾을 수 없습니다."),

    @ExplainError("찜하기 그룹 접근 권한 없음")
    WISHLIST_GROUP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "WISHLISTGROUP4002", "찜하기 그룹에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 찜하기 그룹")
    WISHLIST_GROUP_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "WISHLISTGROUP4003", "이미 삭제된 찜하기 그룹입니다."),

    @ExplainError("이미 그룹에 추가된 찜하기")
    ALREADY_ADDED_TO_GROUP(HttpStatus.CONFLICT, "WISHLISTGROUP4004", "이미 그룹에 추가된 찜하기입니다."),

    @ExplainError("찜하기를 찾을 수 없음")
    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "WISHLISTGROUP4005", "찜하기를 찾을 수 없습니다."),

    @ExplainError("유효하지 않은 그룹 이름")
    INVALID_GROUP_NAME(HttpStatus.BAD_REQUEST, "WISHLISTGROUP4006", "유효하지 않은 그룹 이름입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WishlistGroupErrorStatus(HttpStatus httpStatus, String code, String message) {
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
