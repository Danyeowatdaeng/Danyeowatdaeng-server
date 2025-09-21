package com.tourapi.tourapi.common.exception.walk.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum WalkSuccessStatus implements SuccessResponse {

    WALK_CREATED(HttpStatus.CREATED, "WALK2001", "산책 기록이 성공적으로 등록되었습니다."),
    WALK_DELETED(HttpStatus.OK, "WALK2003", "산책 기록이 성공적으로 삭제되었습니다."),
    WALK_LIST_FOUND(HttpStatus.OK, "WALK2004", "산책 기록 목록을 성공적으로 조회했습니다."),
    WALK_DETAIL_FOUND(HttpStatus.OK, "WALK2005", "산책 기록 상세 정보를 성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WalkSuccessStatus(HttpStatus httpStatus, String code, String message) {
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