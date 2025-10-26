package com.tourapi.tourapi.common.exception.wishlistgroup.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum WishlistGroupSuccessStatus implements SuccessResponse {

    WISHLIST_GROUP_CREATED(HttpStatus.CREATED, "WISHLISTGROUP2001", "찜하기 그룹이 생성되었습니다."),
    WISHLIST_GROUP_UPDATED(HttpStatus.OK, "WISHLISTGROUP2002", "찜하기 그룹이 수정되었습니다."),
    WISHLIST_GROUP_DELETED(HttpStatus.OK, "WISHLISTGROUP2003", "찜하기 그룹이 삭제되었습니다."),
    WISHLIST_GROUP_FOUND(HttpStatus.OK, "WISHLISTGROUP2004", "찜하기 그룹을 조회했습니다."),
    WISHLIST_GROUP_LIST_FOUND(HttpStatus.OK, "WISHLISTGROUP2005", "찜하기 그룹 목록을 조회했습니다."),
    WISHLIST_ADDED_TO_GROUP(HttpStatus.OK, "WISHLISTGROUP2006", "찜하기가 그룹에 추가되었습니다."),
    WISHLIST_REMOVED_FROM_GROUP(HttpStatus.OK, "WISHLISTGROUP2007", "찜하기가 그룹에서 삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WishlistGroupSuccessStatus(HttpStatus httpStatus, String code, String message) {
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
