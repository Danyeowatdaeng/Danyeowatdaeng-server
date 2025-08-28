package com.tourapi.tourapi.petAvatar;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pet_avatar")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetAvatar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet", nullable = false, length = 20)
    private PetType pet;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "display_name", nullable = false, length = 40)
    private String displayName;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false; // AI 확장용

    @Column(name = "original_image_url", columnDefinition = "TEXT")
    private String originalImageUrl; // AI 확장용 (원본 이미지)

    @Enumerated(EnumType.STRING)
    @Column(name = "style", length = 20)
    private PetAvatarStyle style; // AI 확장용 (변환 스타일)

    @Column(name = "member_id")
    private Long memberId; // AI 확장용 (소유자)

    @Builder
    public PetAvatar(PetType pet, String code, String displayName, String imageUrl, 
                    Boolean isActive, Boolean isCustom, String originalImageUrl, 
                    PetAvatarStyle style, Long memberId) {
        this.pet = pet;
        this.code = code;
        this.displayName = displayName;
        this.imageUrl = imageUrl;
        this.isActive = isActive != null ? isActive : true;
        this.isCustom = isCustom != null ? isCustom : false;
        this.originalImageUrl = originalImageUrl;
        this.style = style;
        this.memberId = memberId;
    }

    // 기본 PetAvatar 생성용 정적 팩토리 메서드
    public static PetAvatar createDefault(PetType pet, String code, String displayName, String imageUrl) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .imageUrl(imageUrl)
                .isActive(true)
                .isCustom(false)
                .style(PetAvatarStyle.DEFAULT)
                .build();
    }

    // 커스텀 PetAvatar 생성용 정적 팩토리 메서드
    public static PetAvatar createCustom(PetType pet, String code, String displayName, 
                                       String imageUrl, String originalImageUrl, 
                                       PetAvatarStyle style, Long memberId) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .imageUrl(imageUrl)
                .isActive(true)
                .isCustom(true)
                .originalImageUrl(originalImageUrl)
                .style(style)
                .memberId(memberId)
                .build();
    }

    // 비활성화 메서드
    public void deactivate() {
        this.isActive = false;
    }

    // 활성화 메서드
    public void activate() {
        this.isActive = true;
    }
}
