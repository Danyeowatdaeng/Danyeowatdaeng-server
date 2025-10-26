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

    /**
     * CSV 데이터의 경우 contentId가 없으므로 가상의 ID를 생성합니다.
     * 위도, 경도, 이름을 기반으로 해시값을 생성하여 고유한 ID를 만듭니다.
     */
    public Long getContentId() {
        if (contentId != null) {
            return contentId;
        }
        
        // CSV 데이터의 경우 가상의 contentId 생성
        if (source != null && "CSV".equals(source) && title != null && latitude != null && longitude != null) {
            String uniqueString = title + latitude + longitude;
            return Math.abs((long) uniqueString.hashCode());
        }
        
        return null;
    }
}