package com.tourapi.tourapi.common.exception.mypet.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum MyPetSuccessStatus implements SuccessResponse {

    DIARY_CREATED(HttpStatus.CREATED, "MYPET2001", "다이어리가 성공적으로 작성되었습니다."),
    DIARY_UPDATED(HttpStatus.OK, "MYPET2002", "다이어리가 성공적으로 수정되었습니다."),
    DIARY_DELETED(HttpStatus.OK, "MYPET2003", "다이어리가 성공적으로 삭제되었습니다."),
    DIARY_LIST_FOUND(HttpStatus.OK, "MYPET2004", "다이어리 목록을 성공적으로 조회했습니다."),
    DIARY_DETAIL_FOUND(HttpStatus.OK, "MYPET2005", "다이어리 상세 정보를 성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MyPetSuccessStatus(HttpStatus httpStatus, String code, String message) {
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