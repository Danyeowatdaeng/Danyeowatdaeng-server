package com.tourapi.tourapi.web.controller.map;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.map.status.MapErrorStatus;
import com.tourapi.tourapi.common.exception.map.status.MapSuccessStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistSuccessStatus;
import com.tourapi.tourapi.map.domain.TourLocation;
import com.tourapi.tourapi.map.domain.Festival;
import com.tourapi.tourapi.map.dto.CommunityFacilityDto;
import com.tourapi.tourapi.map.dto.DetailIntroResponse;
import com.tourapi.tourapi.map.dto.DetailAggregate;
import com.tourapi.tourapi.map.service.DetailAggregationService;
import com.tourapi.tourapi.map.service.TourLocationService;
import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.dto.WishlistAddRequest;
import com.tourapi.tourapi.wishlist.dto.WishlistAdapter;
import com.tourapi.tourapi.wishlist.dto.WishlistResponse;
import com.tourapi.tourapi.wishlist.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Map", description = "위치/키워드 기반 관광지 검색 API")
public class MapController {

    private final TourLocationService tourLocationService;
    private final DetailAggregationService detailAggregationService;
    private final WishlistService wishlistService;
    private final WishlistAdapter wishlistAdapter;


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

    @GetMapping("/festivals")
    @Operation(
            summary = "전국 축제 정보 조회",
            description = "전국에서 진행 중인 축제/행사 정보를 조회합니다. 페이지네이션을 지원합니다."
    )
    @ApiErrorCodeExample(value = MapErrorStatus.class, codes = {"EXTERNAL_API_FAILURE", "INVALID_PARAMETER"})
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    public ResponseEntity<ApiResponse<List<Festival>>> getFestivals(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "numOfRows", defaultValue = "10") Integer numOfRows,
            @RequestParam(name = "json", defaultValue = "true") boolean json
    ) {
        List<Festival> festivals = tourLocationService.getFestivals(pageNo, numOfRows);
        return ApiResponse.onSuccess(MapSuccessStatus.SEARCH_SUCCESS, festivals);
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

    @PostMapping("/wishlist/add")
    @Operation(
            summary = "검색 결과를 찜하기에 추가",
            description = "키워드 검색 결과(CommunityFacility) 또는 영역 검색 결과(TourLocation)를 찜하기에 추가합니다. " +
                        "요청 본문에 'source' 필드로 'CSV' 또는 'TOUR_API'를 명시하여 데이터 타입을 구분합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    @ApiErrorCodeExample(value = WishlistErrorStatus.class, codes = {"WISHLIST4002"}) // ALREADY_ADDED_TO_WISHLIST
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @Valid @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        WishlistAddRequest request;
        
        // source 필드로 데이터 타입 구분
        String source = (String) requestData.get("source");
        if ("CSV".equals(source)) {
            // CommunityFacilityDto로 변환
            CommunityFacilityDto facility = convertToCommunityFacility(requestData);
            request = wishlistAdapter.fromCommunityFacility(facility);
        } else if ("TOUR_API".equals(source)) {
            // TourLocation으로 변환
            TourLocation tourLocation = convertToTourLocation(requestData);
            request = wishlistAdapter.fromTourLocation(tourLocation);
        } else {
            log.error("Invalid source type: {}", source);
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        }

        Wishlist wishlist = wishlistService.addToWishlist(memberId, request);
        WishlistResponse response = WishlistResponse.from(wishlist);

        log.info("Item added to wishlist: memberId={}, contentId={}, title={}, source={}", 
                memberId, request.getContentId(), request.getTitle(), source);
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_ADDED, response);
    }

    /**
     * Map을 CommunityFacilityDto로 변환
     */
    private CommunityFacilityDto convertToCommunityFacility(Map<String, Object> data) {
        return CommunityFacilityDto.builder()
                .name((String) data.get("name"))
                .category3((String) data.get("category3"))
                .roadAddress((String) data.get("roadAddress"))
                .jibunAddress((String) data.get("jibunAddress"))
                .homepage((String) data.get("homepage"))
                .closedDays((String) data.get("closedDays"))
                .openingHours((String) data.get("openingHours"))
                .latitude(data.get("latitude") != null ? ((Number) data.get("latitude")).doubleValue() : null)
                .longitude(data.get("longitude") != null ? ((Number) data.get("longitude")).doubleValue() : null)
                .phone((String) data.get("phone"))
                .source("CSV")
                .build();
    }

    /**
     * Map을 TourLocation으로 변환
     */
    private TourLocation convertToTourLocation(Map<String, Object> data) {
        return TourLocation.builder()
                .id(data.get("id") != null ? ((Number) data.get("id")).longValue() : null)
                .title((String) data.get("title"))
                .category(data.get("category") != null ? ((Number) data.get("category")).intValue() : null)
                .address((String) data.get("address"))
                .description((String) data.get("description"))
                .imageUrl1((String) data.get("imageUrl1"))
                .imageUrl2((String) data.get("imageUrl2"))
                .latitude(data.get("latitude") != null ? ((Number) data.get("latitude")).doubleValue() : null)
                .longitude(data.get("longitude") != null ? ((Number) data.get("longitude")).doubleValue() : null)
                .distance(data.get("distance") != null ? ((Number) data.get("distance")).intValue() : null)
                .phoneNumber((String) data.get("phoneNumber"))
                .homepageUrl((String) data.get("homepageUrl"))
                .build();
    }
}


