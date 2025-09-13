package com.tourapi.tourapi.wishlist.dto;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WishlistResponse {

    @Schema(description = "찜하기 ID")
    private Long id;

    @Schema(description = "관광지 콘텐츠 ID")
    private Long contentId;

    @Schema(description = "관광지 콘텐츠 타입 ID")
    private Integer contentTypeId;

    @Schema(description = "관광지 이름")
    private String title;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "위도")
    private Double latitude;

    @Schema(description = "경도")
    private Double longitude;

    @Schema(description = "찜한 일시")
    private LocalDateTime createdAt;

    public static WishlistResponse from(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .contentId(wishlist.getContentId())
                .contentTypeId(wishlist.getContentTypeId())
                .title(wishlist.getTitle())
                .address(wishlist.getAddress())
                .imageUrl(wishlist.getImageUrl())
                .latitude(wishlist.getLatitude())
                .longitude(wishlist.getLongitude())
                .createdAt(wishlist.getCreatedAt())
                .build();
    }
}