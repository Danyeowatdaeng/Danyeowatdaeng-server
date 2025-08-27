package com.tourapi.tourapi.common.exception.petAvatar.status;

import com.tourapi.tourapi.common.exception.status.SuccessStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PetAvatarSuccessStatus implements SuccessStatus {
    
    PET_AVATAR_LIST_FOUND("PET_AVATAR_LIST_FOUND", "PetAvatar 목록을 성공적으로 조회했습니다.", 200),
    PET_AVATAR_SELECTED("PET_AVATAR_SELECTED", "PetAvatar를 성공적으로 선택했습니다.", 200),
    PET_AVATAR_DETAIL_FOUND("PET_AVATAR_DETAIL_FOUND", "PetAvatar 상세 정보를 성공적으로 조회했습니다.", 200),
    
    // AI 확장용 성공 상태
    PET_AVATAR_UPLOADED("PET_AVATAR_UPLOADED", "이미지를 성공적으로 업로드했습니다.", 200),
    PET_AVATAR_CONVERT_REQUESTED("PET_AVATAR_CONVERT_REQUESTED", "AI 변환을 성공적으로 요청했습니다.", 200),
    PET_AVATAR_CONVERTED("PET_AVATAR_CONVERTED", "AI 변환이 성공적으로 완료되었습니다.", 200);

    private final String code;
    private final String message;
    private final int statusCode;
}
