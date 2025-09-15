package com.tourapi.tourapi.web.controller.map;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.map.status.MapErrorStatus;
import com.tourapi.tourapi.common.exception.map.status.MapSuccessStatus;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.dto.CommunityFacilityDto;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;
import com.tourapi.tourapi.map.dto.DetailAggregate;
import com.tourapi.tourapi.map.service.DetailAggregationService;
import com.tourapi.tourapi.map.service.TourLocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Map", description = "위치/키워드 기반 관광지 검색 API")
public class MapController {

    private final TourLocationService tourLocationService;
    private final DetailAggregationService detailAggregationService;


    @GetMapping("/search/keyword")
    @Operation(
            summary = "키워드 기반 관광지 검색",
            description = "키워드로 관광지를 검색합니다. 페이징/정렬 파라미터 설명: page=0부터 시작하는 페이지 번호, size=페이지당 개수, sort=정렬 기준(형식: 필드,방향 예: name,asc). 여러 정렬은 sort를 여러 번 지정."
    )
    @ApiErrorCodeExample(value = MapErrorStatus.class, codes = {"EXTERNAL_API_FAILURE", "INVALID_PARAMETER"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<List<CommunityFacilityDto>>> searchByKeyword(
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        long start = System.currentTimeMillis();
        List<CommunityFacilityDto> results = tourLocationService.searchByKeyword(keyword, pageable);
        long ms = System.currentTimeMillis() - start;
        log.info("MapController 검색 API 완료: '{}' → 최종 응답 {}건, 총 {} ms 소요", 
                keyword, results.size(), ms);
        return ApiResponse.onSuccess(MapSuccessStatus.KEYWORD_SEARCH_SUCCESS, results);
    }


    @GetMapping("/search/bounds")
    @Operation(
            summary = "영역 기반 관광지 검색",
            description = "지도의 남서(SW)/북동(NE) 좌표를 기준으로 해당 영역 내 관광지를 조회합니다. category 파라미터를 추가하면 해당 카테고리(contentTypeId)로 필터링됩니다. 카카오맵 bounds_changed 이벤트와 연동하여 사용합니다."
    )
    @ApiErrorCodeExample(value = MapErrorStatus.class, codes = {"EXTERNAL_API_FAILURE", "INVALID_PARAMETER", "LOCATION_NOT_FOUND"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<List<TourLocation>>> searchByBounds(
            @RequestParam(name = "swLat") Double swLat,      // 남서쪽 위도
            @RequestParam(name = "swLng") Double swLng,      // 남서쪽 경도
            @RequestParam(name = "neLat") Double neLat,      // 북동쪽 위도
            @RequestParam(name = "neLng") Double neLng,      // 북동쪽 경도
            @RequestParam(name = "category", required = false) Integer category,
            @RequestParam(name = "zoomLevel", required = false) Integer zoomLevel,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        List<TourLocation> results = tourLocationService.searchByBounds(swLat, swLng, neLat, neLng, category, zoomLevel);
        return ApiResponse.onSuccess(MapSuccessStatus.SEARCH_SUCCESS, results);
    }

    @GetMapping("/detail/intro")
    @Operation(
            summary = "상세 소개 조회(detailIntro)",
            description = "contentId와 contentTypeId에 따라 상이한 상세 필드를 반환합니다. 예: 음식점(39)은 firstmenu, opentimefood 등."
    )
    @ApiErrorCodeExample(value = MapErrorStatus.class, codes = {"EXTERNAL_API_FAILURE", "INVALID_PARAMETER", "LOCATION_NOT_FOUND"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<DetailIntroResponse>> getDetailIntro(
            @RequestParam(name = "contentId") Long contentId,
            @RequestParam(name = "contentTypeId") Integer contentTypeId,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        DetailIntroResponse detail = tourLocationService.getDetailIntro(contentId, contentTypeId);
        return ApiResponse.onSuccess(MapSuccessStatus.SEARCH_SUCCESS, detail);
    }

    @GetMapping("/detail")
    @Operation(
            summary = "상세 집계 조회",
            description = "외부 detailIntro + 리뷰 요약/샘플 + (향후) 제휴 이벤트를 합쳐 제공하는 집계 응답"
    )
    @ApiErrorCodeExample(value = MapErrorStatus.class, codes = {"EXTERNAL_API_FAILURE", "INVALID_PARAMETER"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<DetailAggregate>> getDetail(
            @RequestParam(name = "contentId") Long contentId,
            @RequestParam(name = "contentTypeId") Integer contentTypeId
    ) {
        DetailAggregate agg = detailAggregationService.getDetail(contentId, contentTypeId);
        return ApiResponse.onSuccess(MapSuccessStatus.SEARCH_SUCCESS, agg);
    }
}


