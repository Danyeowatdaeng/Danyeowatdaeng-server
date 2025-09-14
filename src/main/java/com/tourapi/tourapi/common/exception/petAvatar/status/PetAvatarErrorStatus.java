package com.tourapi.tourapi.common.exception.petAvatar.status;

import org.springframework.http.HttpStatus;

import com.tourapi.tourapi.common.exception.general.status.ErrorResponse;

public enum PetAvatarErrorStatus implements ErrorResponse {
    // 도메인 공통
    PET_AVATAR_NOT_FOUND(HttpStatus.NOT_FOUND, "PET4001", "PetAvatar를 찾을 수 없습니다."),
    PET_AVATAR_INACTIVE(HttpStatus.BAD_REQUEST, "PET4002", "비활성화된 PetAvatar입니다."),
    PET_AVATAR_ALREADY_SELECTED(HttpStatus.BAD_REQUEST, "PET4003", "이미 PetAvatar가 선택되었습니다."),
    PET_AVATAR_NOT_SELECTED(HttpStatus.BAD_REQUEST, "PET4004", "PetAvatar가 선택되지 않았습니다."),
    PET_AVATAR_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PET4005", "커스텀 PetAvatar에 대한 접근 권한이 없습니다."),

    // AI 확장용
    PET_AVATAR_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PET4010", "이미지 업로드에 실패했습니다."),
    PET_AVATAR_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PET4011", "AI 변환에 실패했습니다."),
    PET_AVATAR_CONVERT_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "PET4012", "AI 변환 시간이 초과되었습니다."),
    PET_AVATAR_INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "PET4013", "지원하지 않는 이미지 형식입니다."),
    PET_AVATAR_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "PET4014", "이미지 크기가 제한을 초과했습니다."),

    // Gemini Provider 관련
    GEMINI_SERVICE_DISABLED(HttpStatus.SERVICE_UNAVAILABLE, "PET4050", "PetAvatar 서비스가 비활성화되었습니다."),
    GEMINI_INVALID_API_KEY(HttpStatus.UNAUTHORIZED, "PET4051", "잘못된 API Key 입니다."),
    GEMINI_FORBIDDEN(HttpStatus.FORBIDDEN, "PET4052", "Gemini API 접근이 거부되었습니다."),
    GEMINI_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND, "PET4053", "요청한 Gemini 모델을 찾을 수 없습니다."),
    GEMINI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "PET4054", "요청 한도를 초과했습니다."),
    GEMINI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "PET4055", "할당량을 초과했습니다."),
    GEMINI_BAD_REQUEST(HttpStatus.BAD_REQUEST, "PET4056", "잘못된 요청 형식입니다."),
    GEMINI_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "PET4057", "지원하지 않는 미디어 형식입니다."),
    GEMINI_SAFETY_BLOCKED(HttpStatus.BAD_REQUEST, "PET4058", "안전 정책에 의해 차단되었습니다."),
    GEMINI_UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "PET4059", "Gemini 상위 서비스 오류입니다."),
    GEMINI_UNKNOWN_ERROR(HttpStatus.BAD_GATEWAY, "PET4060", "Gemini 처리 중 알 수 없는 오류입니다."),
    PROVIDER_UNSUPPORTED(HttpStatus.BAD_REQUEST, "PET4061", "지원하지 않는 프로바이더입니다.");

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
