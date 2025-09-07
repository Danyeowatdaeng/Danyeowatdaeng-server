package com.tourapi.tourapi.common.exception.map.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum MapSuccessStatus implements SuccessResponse {

    SEARCH_SUCCESS(HttpStatus.OK, "MAP2000", "지도 검색이 성공했습니다."),
    KEYWORD_SEARCH_SUCCESS(HttpStatus.OK, "MAP2001", "키워드 검색이 성공했습니다."),
    CATEGORY_SEARCH_SUCCESS(HttpStatus.OK, "MAP2002", "카테고리 검색이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    MapSuccessStatus(HttpStatus httpStatus, String code, String message) {
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


