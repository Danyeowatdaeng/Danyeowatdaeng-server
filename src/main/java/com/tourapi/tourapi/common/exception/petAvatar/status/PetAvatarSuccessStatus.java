package com.tourapi.tourapi.common.exception.petAvatar.status;

import com.tourapi.tourapi.common.exception.general.status.SuccessResponse;
import org.springframework.http.HttpStatus;

public enum PetAvatarSuccessStatus implements SuccessResponse {

    PET_AVATAR_LIST_FOUND(HttpStatus.OK, "PET2001", "PetAvatar 목록을 성공적으로 조회했습니다."),
    PET_AVATAR_SELECTED(HttpStatus.OK, "PET2002", "PetAvatar를 성공적으로 선택했습니다."),
    PET_AVATAR_DETAIL_FOUND(HttpStatus.OK, "PET2003", "PetAvatar 상세 정보를 성공적으로 조회했습니다."),

    // AI 확장용 성공 상태
    PET_AVATAR_UPLOADED(HttpStatus.OK, "PET2010", "이미지를 성공적으로 업로드했습니다."),
    PET_AVATAR_CONVERT_REQUESTED(HttpStatus.OK, "PET2011", "AI 변환을 성공적으로 요청했습니다."),
    PET_AVATAR_CONVERTED(HttpStatus.OK, "PET2012", "AI 변환이 성공적으로 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PetAvatarSuccessStatus(HttpStatus httpStatus, String code, String message) {
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
