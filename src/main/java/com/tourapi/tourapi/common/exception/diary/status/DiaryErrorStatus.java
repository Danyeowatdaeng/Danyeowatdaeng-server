package com.tourapi.tourapi.common.exception.diary.status;

import com.tourapi.tourapi.common.exception.ExplainError;
import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;
import org.springframework.http.HttpStatus;

public enum DiaryErrorStatus implements ErrorResponse {

    @ExplainError("다이어리를 찾을 수 없음")
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY4001", "다이어리를 찾을 수 없습니다."),

    @ExplainError("다이어리 접근 권한 없음")
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DIARY4002", "해당 다이어리에 접근할 권한이 없습니다."),

    @ExplainError("이미 삭제된 다이어리")
    DIARY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "DIARY4003", "이미 삭제된 다이어리입니다."),

    @ExplainError("다이어리 제목이 비어있음")
    DIARY_TITLE_EMPTY(HttpStatus.BAD_REQUEST, "DIARY4004", "다이어리 제목은 필수입니다."),

    @ExplainError("다이어리 제목이 너무 김")
    DIARY_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "DIARY4005", "다이어리 제목은 100자 이하로 입력해주세요."),

    @ExplainError("다이어리 내용이 너무 김")
    DIARY_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "DIARY4006", "다이어리 내용이 너무 깁니다."),

    @ExplainError("잘못된 이미지 URL")
    DIARY_INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "DIARY4007", "유효하지 않은 이미지 URL입니다."),

    @ExplainError("다이어리 생성 실패")
    DIARY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DIARY5001", "다이어리 생성에 실패했습니다."),

    @ExplainError("다이어리 수정 실패")
    DIARY_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DIARY5002", "다이어리 수정에 실패했습니다."),

    @ExplainError("다이어리 삭제 실패")
    DIARY_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DIARY5003", "다이어리 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    DiaryErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getErrorStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}