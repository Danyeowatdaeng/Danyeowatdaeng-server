package com.tourapi.tourapi.wishlistgroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistGroupCreateRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Schema(description = "그룹 이름", example = "강아지 친화 장소", required = true)
    private String name;

    @Schema(description = "공개 여부 (true: 공개, false: 비공개)", example = "true", required = true)
    private Boolean isPublic = true;

    @Schema(description = "카테고리 이미지 URL", example = "https://example.com/images/category.png")
    private String categoryImageUrl;
}
