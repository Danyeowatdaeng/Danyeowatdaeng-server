package com.tourapi.tourapi.wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistAddRequest {

    @NotNull(message = "콘텐츠 ID는 필수입니다")
    @Schema(description = "관광지 콘텐츠 ID", example = "126508")
    private Long contentId;

    @NotNull(message = "콘텐츠 타입 ID는 필수입니다")
    @Schema(description = "관광지 콘텐츠 타입 ID", example = "12")
    private Integer contentTypeId;

    @Schema(description = "관광지 이름", example = "경복궁")
    private String title;

    @Schema(description = "주소", example = "서울특별시 종로구 사직로 161")
    private String address;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "위도", example = "37.5796")
    private Double latitude;

    @Schema(description = "경도", example = "126.9770")
    private Double longitude;
}