package com.tourapi.tourapi.common.exception.petAvatar.status;

import com.tourapi.tourapi.common.exception.status.ErrorStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetAvatarErrorStatus implements ErrorStatus {
    
    PET_AVATAR_NOT_FOUND("PET4001", "PetAvatar를 찾을 수 없습니다.", 404),
    PET_AVATAR_INACTIVE("PET4002", "비활성화된 PetAvatar입니다.", 400),
    PET_AVATAR_ALREADY_SELECTED("PET4003", "이미 PetAvatar가 선택되었습니다.", 400),
    PET_AVATAR_ACCESS_DENIED("PET4004", "커스텀 PetAvatar에 대한 접근 권한이 없습니다.", 403),
    
    // AI 확장용 에러 상태
    PET_AVATAR_UPLOAD_FAILED("PET4010", "이미지 업로드에 실패했습니다.", 500),
    PET_AVATAR_CONVERT_FAILED("PET4011", "AI 변환에 실패했습니다.", 500),
    PET_AVATAR_CONVERT_TIMEOUT("PET4012", "AI 변환 시간이 초과되었습니다.", 408),
    PET_AVATAR_INVALID_IMAGE_FORMAT("PET4013", "지원하지 않는 이미지 형식입니다.", 400),
    PET_AVATAR_IMAGE_SIZE_EXCEEDED("PET4014", "이미지 크기가 제한을 초과했습니다.", 400);

    private final String code;
    private final String message;
    private final int statusCode;
}
