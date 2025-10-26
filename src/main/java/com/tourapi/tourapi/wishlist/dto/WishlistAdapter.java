package com.tourapi.tourapi.wishlist.dto;

import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.dto.CommunityFacilityDto;
import org.springframework.stereotype.Component;

/**
 * 다양한 API 응답을 WishlistAddRequest로 변환하는 어댑터
 */
@Component
public class WishlistAdapter {

    /**
     * TourLocation (TourAPI 데이터)을 WishlistAddRequest로 변환
     */
    public WishlistAddRequest fromTourLocation(TourLocation tourLocation) {
        if (tourLocation == null) {
            return null;
        }

        WishlistAddRequest request = new WishlistAddRequest();
        request.setContentId(tourLocation.getId());
        request.setContentTypeId(tourLocation.getCategory());
        request.setTitle(tourLocation.getTitle());
        request.setAddress(tourLocation.getAddress());
        request.setImageUrl(tourLocation.getImageUrl1()); // 첫 번째 이미지 사용
        request.setLatitude(tourLocation.getLatitude());
        request.setLongitude(tourLocation.getLongitude());
        request.setSource("TOUR_API");

        return request;
    }

    /**
     * CommunityFacilityDto (CSV 데이터)를 WishlistAddRequest로 변환
     */
    public WishlistAddRequest fromCommunityFacility(CommunityFacilityDto facility) {
        if (facility == null) {
            return null;
        }

        WishlistAddRequest request = new WishlistAddRequest();
        // CSV 데이터는 contentId가 없으므로 랜덤 숫자 생성
        request.setContentId(generateRandomContentId());
        request.setContentTypeId(null); // CSV에는 contentTypeId가 없음
        request.setTitle(facility.getName());
        request.setAddress(facility.getRoadAddress() != null ? facility.getRoadAddress() : facility.getJibunAddress());
        request.setImageUrl(null); // CSV에는 이미지 URL이 없음
        request.setLatitude(facility.getLatitude());
        request.setLongitude(facility.getLongitude());
        request.setSource("CSV");

        return request;
    }

    /**
     * CSV 데이터용 랜덤 contentId 생성
     * 100000 ~ 999999 범위의 랜덤 숫자
     */
    private Long generateRandomContentId() {
        return (long) (Math.random() * 900000) + 100000;
    }
}
