package com.tourapi.tourapi.common.exception.wishlist.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum WishlistSuccessStatus implements SuccessResponse {

    WISHLIST_ADDED(HttpStatus.CREATED, "WISHLIST2001", "찜하기에 추가되었습니다."),
    WISHLIST_REMOVED(HttpStatus.OK, "WISHLIST2002", "찜하기에서 삭제되었습니다."),
    WISHLIST_LIST_FOUND(HttpStatus.OK, "WISHLIST2003", "찜하기 목록을 성공적으로 조회했습니다."),
    WISHLIST_STATUS_CHECKED(HttpStatus.OK, "WISHLIST2004", "찜하기 상태를 확인했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WishlistSuccessStatus(HttpStatus httpStatus, String code, String message) {
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