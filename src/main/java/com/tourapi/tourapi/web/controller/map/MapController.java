package com.tourapi.tourapi.web.controller.map;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.common.exception.map.status.MapSuccessStatus;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.service.TourLocationService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Tag(name = "Map", description = "위치/키워드 기반 관광지 검색 API")
public class MapController {

    private final TourLocationService tourLocationService;

    @GetMapping("/search")
    @Operation(
            summary = "위치 기반 관광지 검색",
            description = "위도/경도를 기준으로 반경 내 관광지를 조회합니다. category는 한국관광공사 contentTypeId(예: 12,14,15,28,32,38,39)를 사용합니다."
    )
    public ResponseEntity<ApiResponse<List<TourLocation>>> searchLocations(
            @RequestParam(name = "latitude") Double latitude,
            @RequestParam(name = "longitude") Double longitude,
            @RequestParam(name = "radius", defaultValue = "1000") Integer radius,
            @RequestParam(name = "category", required = false) Integer category,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        List<TourLocation> results = tourLocationService.searchByLocation(latitude, longitude, radius, category);
        return ApiResponse.onSuccess(MapSuccessStatus.SEARCH_SUCCESS, results);
    }

    @GetMapping("/search/keyword")
    @Operation(
            summary = "키워드 기반 관광지 검색",
            description = "키워드로 관광지를 검색합니다. pageable 파라미터(page,size,sort) 이용 가능."
    )
    public ResponseEntity<ApiResponse<List<TourLocation>>> searchByKeyword(
            @RequestParam(name = "keyword") String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        List<TourLocation> results = tourLocationService.searchByKeyword(keyword, pageable);
        return ApiResponse.onSuccess(MapSuccessStatus.KEYWORD_SEARCH_SUCCESS, results);
    }
}


