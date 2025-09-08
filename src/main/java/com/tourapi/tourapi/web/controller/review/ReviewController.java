package com.tourapi.tourapi.web.controller.review;

import com.tourapi.tourapi.common.exception.review.status.ReviewSuccessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.review.status.ReviewErrorStatus;
import com.tourapi.tourapi.review.domain.Review;
import com.tourapi.tourapi.review.dto.ReviewCreateRequest;
import com.tourapi.tourapi.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 조회/작성 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "리뷰 목록", description = "contentId 기준으로 최신순 리뷰 목록을 조회합니다.")
    @ApiErrorCodeExample(value = ReviewErrorStatus.class, codes = {"INVALID_PARAMETER", "NOT_FOUND"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    @Parameters({
            @Parameter(name = "contentId", description = "관광 콘텐츠 ID"),
            @Parameter(name = "page", description = "0부터 시작하는 페이지 번호"),
            @Parameter(name = "size", description = "페이지당 결과 개수"),
            @Parameter(name = "sort", description = "정렬 기준: 필드,방향 형식 (예: createdAt,desc)")
    })
    public ResponseEntity<ApiResponse<Page<Review>>> list(
            @RequestParam(name = "contentId") Long contentId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<Review> page = reviewService.getReviews(contentId, pageable);
        return ApiResponse.onSuccess(ReviewSuccessStatus.LIST_SUCCESS, page);
    }

    @PostMapping
    @Operation(summary = "리뷰 작성", description = "간단한 리뷰 저장. 인증 연동 전 임시 userId 포함")
    @ApiErrorCodeExample(value = ReviewErrorStatus.class, codes = {"INVALID_PARAMETER", "FORBIDDEN"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<Review>> create(@RequestBody ReviewCreateRequest req) {
        Review saved = reviewService.create(req);
        return ApiResponse.onSuccess(ReviewSuccessStatus.CREATE_SUCCESS, saved);
    }
}


