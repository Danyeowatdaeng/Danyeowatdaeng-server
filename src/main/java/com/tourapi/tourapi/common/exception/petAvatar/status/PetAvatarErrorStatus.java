package com.tourapi.tourapi.common.exception.petAvatar.status;

import org.springframework.http.HttpStatus;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;

public enum PetAvatarErrorStatus implements ErrorResponse {

    PET_AVATAR_NOT_FOUND(HttpStatus.NOT_FOUND, "PET4001", "PetAvatar를 찾을 수 없습니다."),
    PET_AVATAR_INACTIVE(HttpStatus.BAD_REQUEST, "PET4002", "비활성화된 PetAvatar입니다."),
    PET_AVATAR_ALREADY_SELECTED(HttpStatus.BAD_REQUEST, "PET4003", "이미 PetAvatar가 선택되었습니다."),
    PET_AVATAR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PET4004", "커스텀 PetAvatar에 대한 접근 권한이 없습니다."),

    // AI 확장용 에러 상태
    PET_AVATAR_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PET4010", "이미지 업로드에 실패했습니다."),
    PET_AVATAR_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PET4011", "AI 변환에 실패했습니다."),
    PET_AVATAR_CONVERT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "PET4012", "AI 변환 시간이 초과되었습니다."),
    PET_AVATAR_INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "PET4013", "지원하지 않는 이미지 형식입니다."),
    PET_AVATAR_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "PET4014", "이미지 크기가 제한을 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PetAvatarErrorStatus(HttpStatus httpStatus, String code, String message) {
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
