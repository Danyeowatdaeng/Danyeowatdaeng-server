package com.tourapi.tourapi.common.exception.mypet.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum DiaryErrorStatus implements ErrorResponse {

    @ExplainError("다이어리를 찾을 수 없음")
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY4001", "다이어리를 찾을 수 없습니다."),

    @ExplainError("다이어리 접근 권한 없음")
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DIARY4002", "해당 다이어리에 접근할 권한이 없습니다."),

    @ExplainError("다이어리 제목이 비어있음")
    DIARY_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "DIARY4003", "다이어리 제목은 필수입니다."),

    @ExplainError("다이어리 제목이 너무 김")
    DIARY_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "DIARY4004", "다이어리 제목은 100자 이하여야 합니다."),

    @ExplainError("이미지 업로드 실패")
    DIARY_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DIARY4005", "이미지 업로드에 실패했습니다."),

    @ExplainError("지원하지 않는 이미지 형식")
    DIARY_INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "DIARY4006", "지원하지 않는 이미지 형식입니다."),

    @ExplainError("이미지 크기 초과")
    DIARY_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "DIARY4007", "이미지 크기가 제한을 초과했습니다."),

    @ExplainError("다이어리가 이미 삭제됨")
    DIARY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "DIARY4008", "이미 삭제된 다이어리입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DiaryErrorStatus(HttpStatus httpStatus, String code, String message) {
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