package com.tourapi.tourapi.wishlistgroup.dto;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.dto.WishlistResponse;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WishlistGroupResponse {

    @Schema(description = "그룹 ID")
    private Long id;

    @Schema(description = "그룹 이름")
    private String name;

    @Schema(description = "공개 여부")
    private Boolean isPublic;

    @Schema(description = "카테고리 이미지 URL")
    private String categoryImageUrl;

    @Schema(description = "그룹 내 찜 항목 리스트")
    private List<WishlistResponse> wishlists;

    @Schema(description = "그룹 생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "그룹 수정일시")
    private LocalDateTime updatedAt;

    public static WishlistGroupResponse from(WishlistGroup group) {
        return WishlistGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .isPublic(group.getIsPublic())
                .categoryImageUrl(group.getCategoryImageUrl())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }

    public static WishlistGroupResponse from(WishlistGroup group, List<Wishlist> wishlists) {
        List<WishlistResponse> wishlistResponses = wishlists.stream()
                .map(WishlistResponse::from)
                .toList();

        return WishlistGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .isPublic(group.getIsPublic())
                .categoryImageUrl(group.getCategoryImageUrl())
                .wishlists(wishlistResponses)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
