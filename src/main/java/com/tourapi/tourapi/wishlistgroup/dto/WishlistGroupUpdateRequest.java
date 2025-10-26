package com.tourapi.tourapi.wishlistgroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistGroupUpdateRequest {

    @Schema(description = "그룹 이름", example = "강아지 친화 장소")
    private String name;

    @Schema(description = "공개 여부 (true: 공개, false: 비공개)", example = "true")
    private Boolean isPublic;

    @Schema(description = "카테고리 이미지 URL", example = "https://example.com/images/category.png")
    private String categoryImageUrl;
}
