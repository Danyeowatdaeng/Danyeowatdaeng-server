package com.tourapi.tourapi.petAvatar.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String cdnUrl; // CDN URL
    private String thumbCdnUrl; // 썸네일 CDN URL
    private String imageMime;
    private Integer width;
    private Integer height;
    private Boolean primary;
    private Integer version;
    private Boolean isActive;
    private Boolean isCustom;
    private String originalImageUrl;
    private PetAvatarStyle style;
    @JsonIgnore
    private Long memberId;
    private LocalDateTime createdAt;

    public static PetAvatarResponse from(PetAvatar petAvatar) {
        return PetAvatarResponse.builder()
                .id(petAvatar.getId())
                .pet(petAvatar.getPet())
                .code(petAvatar.getCode())
                .displayName(petAvatar.getDisplayName())
                .cdnUrl(petAvatar.getCdnUrl())
                .thumbCdnUrl(generateThumbCdnUrl(petAvatar))
                .imageMime(petAvatar.getImageMime())
                .width(petAvatar.getWidth())
                .height(petAvatar.getHeight())
                .primary(petAvatar.getPrimary())
                .version(petAvatar.getVersion())
                .isActive(petAvatar.getIsActive())
                .isCustom(petAvatar.getIsCustom())
                .originalImageUrl(petAvatar.getOriginalImageUrl())
                .style(petAvatar.getStyle())
                .memberId(petAvatar.getMemberId())
                .createdAt(petAvatar.getCreatedAt())
                .build();
    }

    /**
     * 썸네일 CDN URL 생성
     */
    private static String generateThumbCdnUrl(PetAvatar petAvatar) {
        if (petAvatar.getThumbKey() != null && !petAvatar.getThumbKey().isEmpty()) {
            // S3Service의 generateCdnUrl 로직을 여기서 구현
            // 실제로는 S3Service를 주입받아서 사용하는 것이 좋지만, 
            // DTO에서는 간단하게 구현
            return petAvatar.getCdnUrl() != null ? 
                petAvatar.getCdnUrl().replace("result/", "thumb/").replace(".png", "_256.webp") : 
                null;
        }
        return null;
    }
}
