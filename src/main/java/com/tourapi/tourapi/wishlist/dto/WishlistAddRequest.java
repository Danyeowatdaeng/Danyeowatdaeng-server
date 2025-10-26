package com.tourapi.tourapi.wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistAddRequest {

    @Schema(description = "관광지 콘텐츠 ID (TourAPI 데이터의 경우 필수, CSV 데이터의 경우 선택적)", example = "126508")
    private Long contentId;

    @Schema(description = "관광지 콘텐츠 타입 ID (TourAPI 데이터의 경우 필수, CSV 데이터의 경우 선택적)", example = "12")
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

    @Schema(description = "데이터 소스 (TOUR_API, CSV)", example = "TOUR_API")
    private String source;
}