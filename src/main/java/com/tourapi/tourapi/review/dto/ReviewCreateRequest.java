package com.tourapi.tourapi.review.dto;

import lombok.Data;

@Data
public class ReviewCreateRequest {
    private Long contentId;
    private Long userId; // 추후 인증 연동 시 제거하고 컨텍스트에서 주입
    private Integer rating; // 1~5 정수
    private String content;
    private String imagesJson;
}


