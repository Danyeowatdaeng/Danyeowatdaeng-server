package com.tourapi.tourapi.common.exception.diary.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum DiarySuccessStatus implements SuccessResponse {

    // 다이어리 CRUD
    DIARY_CREATED(HttpStatus.CREATED, "DIARY2001", "다이어리가 성공적으로 생성되었습니다."),
    DIARY_UPDATED(HttpStatus.OK, "DIARY2002", "다이어리가 성공적으로 수정되었습니다."),
    DIARY_DELETED(HttpStatus.OK, "DIARY2003", "다이어리가 성공적으로 삭제되었습니다."),

    // 다이어리 조회
    DIARY_LIST_FOUND(HttpStatus.OK, "DIARY2010", "다이어리 목록을 성공적으로 조회했습니다."),
    DIARY_DETAIL_FOUND(HttpStatus.OK, "DIARY2011", "다이어리 상세 정보를 성공적으로 조회했습니다."),
    DIARY_COUNT_FOUND(HttpStatus.OK, "DIARY2012", "다이어리 개수를 성공적으로 조회했습니다."),

    // 다이어리 검색
    DIARY_SEARCH_FOUND(HttpStatus.OK, "DIARY2020", "다이어리 검색 결과를 성공적으로 조회했습니다."),
    DIARY_RECENT_FOUND(HttpStatus.OK, "DIARY2021", "최근 다이어리를 성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DiarySuccessStatus(HttpStatus httpStatus, String code, String message) {
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