package com.tourapi.tourapi.wishlistgroup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WishlistGroupListItemRequest {

    @Schema(description = "삭제할 찜하기 ID 리스트", example = "[1, 2, 3]", required = true)
    private List<Long> wishlistIds;
}
