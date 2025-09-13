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

    @Column(name = "result_key", columnDefinition = "TEXT")
    private String resultKey; // S3 결과 이미지 키

    @Column(name = "thumb_key", columnDefinition = "TEXT")
    private String thumbKey; // S3 썸네일 이미지 키

    @Column(name = "cdn_url", columnDefinition = "TEXT")
    private String cdnUrl; // CloudFront CDN URL

    @Column(name = "image_mime", length = 50)
    private String imageMime = "image/png"; // 이미지 MIME 타입

    @Column(name = "width")
    private Integer width; // 이미지 너비

    @Column(name = "height")
    private Integer height; // 이미지 높이

    @Column(name = "primary", nullable = false)
    private Boolean primary = false; // 대표 아바타 여부

    @Column(name = "version", nullable = false)
    private Integer version = 1; // 버전 관리

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
    public PetAvatar(PetType pet, String code, String displayName, 
                    String resultKey, String thumbKey, String cdnUrl, String imageMime,
                    Integer width, Integer height, Boolean primary, Integer version,
                    Boolean isActive, Boolean isCustom, String originalImageUrl, 
                    PetAvatarStyle style, Long memberId) {
        this.pet = pet;
        this.code = code;
        this.displayName = displayName;
        this.resultKey = resultKey;
        this.thumbKey = thumbKey;
        this.cdnUrl = cdnUrl;
        this.imageMime = imageMime != null ? imageMime : "image/png";
        this.width = width;
        this.height = height;
        this.primary = primary != null ? primary : false;
        this.version = version != null ? version : 1;
        this.isActive = isActive != null ? isActive : true;
        this.isCustom = isCustom != null ? isCustom : false;
        this.originalImageUrl = originalImageUrl;
        this.style = style;
        this.memberId = memberId;
    }

    // 기본 PetAvatar 생성용 정적 팩토리 메서드 (기존 호환성)
    public static PetAvatar createDefault(PetType pet, String code, String displayName, String cdnUrl) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .cdnUrl(cdnUrl)
                .isActive(true)
                .isCustom(false)
                .style(PetAvatarStyle.DEFAULT)
                .build();
    }

    // S3 기반 기본 PetAvatar 생성용 정적 팩토리 메서드
    public static PetAvatar createDefaultWithS3(PetType pet, String code, String displayName, 
                                               String resultKey, String thumbKey, String cdnUrl,
                                               String imageMime, Integer width, Integer height) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .resultKey(resultKey)
                .thumbKey(thumbKey)
                .cdnUrl(cdnUrl)
                .imageMime(imageMime)
                .width(width)
                .height(height)
                .isActive(true)
                .isCustom(false)
                .style(PetAvatarStyle.DEFAULT)
                .build();
    }

    // 커스텀 PetAvatar 생성용 정적 팩토리 메서드 (기존 호환성)
    public static PetAvatar createCustom(PetType pet, String code, String displayName, 
                                       String cdnUrl, String originalImageUrl, 
                                       PetAvatarStyle style, Long memberId) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .cdnUrl(cdnUrl)
                .isActive(true)
                .isCustom(true)
                .originalImageUrl(originalImageUrl)
                .style(style)
                .memberId(memberId)
                .build();
    }

    // S3 기반 커스텀 PetAvatar 생성용 정적 팩토리 메서드
    public static PetAvatar createCustomWithS3(PetType pet, String code, String displayName,
                                             String resultKey, String thumbKey, String cdnUrl,
                                             String imageMime, Integer width, Integer height,
                                             String originalImageUrl, PetAvatarStyle style, 
                                             Long memberId, Boolean primary) {
        return PetAvatar.builder()
                .pet(pet)
                .code(code)
                .displayName(displayName)
                .resultKey(resultKey)
                .thumbKey(thumbKey)
                .cdnUrl(cdnUrl)
                .imageMime(imageMime)
                .width(width)
                .height(height)
                .primary(primary != null ? primary : false)
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

    // 대표 아바타로 설정
    public void setAsPrimary() {
        this.primary = true;
    }

    // 대표 아바타 해제
    public void unsetAsPrimary() {
        this.primary = false;
    }

    // 버전 업데이트
    public void updateVersion() {
        this.version = this.version + 1;
    }

    // S3 정보 업데이트
    public void updateS3Info(String resultKey, String thumbKey, String cdnUrl, 
                           String imageMime, Integer width, Integer height) {
        this.resultKey = resultKey;
        this.thumbKey = thumbKey;
        this.cdnUrl = cdnUrl;
        this.imageMime = imageMime;
        this.width = width;
        this.height = height;
    }
}
