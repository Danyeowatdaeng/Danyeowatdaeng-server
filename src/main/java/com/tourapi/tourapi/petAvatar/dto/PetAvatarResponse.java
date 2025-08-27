package com.tourapi.tourapi.petAvatar.dto;

import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PetAvatarResponse {
    private Long id;
    private PetType pet;
    private String code;
    private String displayName;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isCustom;
    private String originalImageUrl;
    private PetAvatarStyle style;
    private Long memberId;
    private LocalDateTime createdAt;

    public static PetAvatarResponse from(PetAvatar petAvatar) {
        return PetAvatarResponse.builder()
                .id(petAvatar.getId())
                .pet(petAvatar.getPet())
                .code(petAvatar.getCode())
                .displayName(petAvatar.getDisplayName())
                .imageUrl(petAvatar.getImageUrl())
                .isActive(petAvatar.getIsActive())
                .isCustom(petAvatar.getIsCustom())
                .originalImageUrl(petAvatar.getOriginalImageUrl())
                .style(petAvatar.getStyle())
                .memberId(petAvatar.getMemberId())
                .createdAt(petAvatar.getCreatedAt())
                .build();
    }
}
