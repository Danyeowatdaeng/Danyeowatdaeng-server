package com.tourapi.tourapi.wishlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchWishlistAddRequest {

    @Schema(description = "시설명", example = "강아지카페")
    private String name;

    @Schema(description = "카테고리3", example = "카페")
    private String category3;

    @Schema(description = "도로명주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;

    @Schema(description = "지번주소", example = "서울특별시 강남구 역삼동 456")
    private String jibunAddress;

    @Schema(description = "홈페이지", example = "https://example.com")
    private String homepage;

    @Schema(description = "휴무일", example = "매주 월요일")
    private String closedDays;

    @Schema(description = "운영시간", example = "09:00-22:00")
    private String openingHours;

    @Schema(description = "위도", example = "37.5665")
    private Double latitude;

    @Schema(description = "경도", example = "126.9780")
    private Double longitude;

    @Schema(description = "전화번호", example = "02-1234-5678")
    private String phone;

    @Schema(description = "데이터 소스", example = "CSV")
    private String source;

    /**
     * CSV 데이터의 경우 contentId가 없으므로 해시값으로 생성
     * name + latitude + longitude를 조합하여 고유한 ID 생성
     */
    public Long generateContentId() {
        if (name == null || latitude == null || longitude == null) {
            return null;
        }
        
        String combined = name + "_" + latitude + "_" + longitude;
        return (long) Math.abs(combined.hashCode());
    }

    /**
     * CSV 데이터의 경우 contentTypeId를 기본값으로 설정
     */
    public Integer getContentTypeId() {
        // CSV 데이터는 기본적으로 관광지 카테고리로 설정
        return 12; // 관광지
    }

    /**
     * WishlistAddRequest로 변환
     */
    public WishlistAddRequest toWishlistAddRequest() {
        WishlistAddRequest request = new WishlistAddRequest();
        request.setContentId(generateContentId());
        request.setContentTypeId(getContentTypeId());
        request.setTitle(name);
        request.setAddress(roadAddress != null ? roadAddress : jibunAddress);
        request.setImageUrl(null); // CSV 데이터에는 이미지 URL이 없음
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setSource(source != null ? source : "CSV");
        return request;
    }
}
