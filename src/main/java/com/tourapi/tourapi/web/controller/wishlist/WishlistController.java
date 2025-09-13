package com.tourapi.tourapi.web.controller.wishlist;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistSuccessStatus;
import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.dto.WishlistAddRequest;
import com.tourapi.tourapi.wishlist.dto.WishlistResponse;
import com.tourapi.tourapi.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wishlist", description = "찜하기 관리 API")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @Operation(summary = "찜하기 추가", description = "관광지를 찜하기에 추가합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    @ApiErrorCodeExample(value = WishlistErrorStatus.class, codes = {"WISHLIST4002"}) // ALREADY_ADDED_TO_WISHLIST
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @Valid @RequestBody WishlistAddRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Wishlist wishlist = wishlistService.addToWishlist(memberId, request);
        WishlistResponse response = WishlistResponse.from(wishlist);

        log.info("Wishlist added: memberId={}, contentId={}", memberId, request.getContentId());
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_ADDED, response);
    }

    @DeleteMapping("/{contentId}")
    @Operation(summary = "찜하기 삭제", description = "관광지를 찜하기에서 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistErrorStatus.class, codes = {"WISHLIST4001"}) // WISHLIST_NOT_FOUND
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        wishlistService.removeFromWishlist(memberId, contentId);

        log.info("Wishlist removed: memberId={}, contentId={}", memberId, contentId);
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_REMOVED);
    }

    @PostMapping("/toggle")
    @Operation(summary = "찜하기 토글", description = "찜하기 상태를 토글합니다. (찜한 상태면 삭제, 안한 상태면 추가)")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(
            @Valid @RequestBody WishlistAddRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        boolean isAdded = wishlistService.toggleWishlist(memberId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("isWishlisted", isAdded);
        response.put("contentId", request.getContentId());
        response.put("action", isAdded ? "added" : "removed");

        log.info("Wishlist toggled: memberId={}, contentId={}, action={}",
                memberId, request.getContentId(), isAdded ? "added" : "removed");

        return ApiResponse.onSuccess(
                isAdded ? WishlistSuccessStatus.WISHLIST_ADDED : WishlistSuccessStatus.WISHLIST_REMOVED,
                response
        );
    }

    @GetMapping
    @Operation(
            summary = "찜하기 목록 조회",
            description = "내가 찜한 관광지 목록을 조회합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Page<WishlistResponse>>> getWishlist(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<Wishlist> wishlistPage = wishlistService.getWishlist(memberId, pageable);
        Page<WishlistResponse> responsePage = wishlistPage.map(WishlistResponse::from);

        log.info("Wishlist retrieved: memberId={}, totalElements={}",
                memberId, wishlistPage.getTotalElements());
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_LIST_FOUND, responsePage);
    }

    @GetMapping("/check/{contentId}")
    @Operation(
            summary = "찜하기 상태 확인",
            description = "특정 관광지가 찜되어 있는지 확인합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkWishlistStatus(
            @Parameter(description = "관광지 콘텐츠 ID", required = true)
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        boolean isWishlisted = wishlistService.isInWishlist(memberId, contentId);

        Map<String, Object> response = new HashMap<>();
        response.put("contentId", contentId);
        response.put("isWishlisted", isWishlisted);

        log.info("Wishlist status checked: memberId={}, contentId={}, isWishlisted={}",
                memberId, contentId, isWishlisted);
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_STATUS_CHECKED, response);
    }

    @GetMapping("/content-ids")
    @Operation(
            summary = "찜한 관광지 ID 목록 조회",
            description = "내가 찜한 모든 관광지의 ID 목록을 조회합니다. (지도에서 찜한 장소 표시용)"
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWishlistContentIds(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        List<Long> contentIds = wishlistService.getWishlistContentIds(memberId);
        long totalCount = wishlistService.getWishlistCount(memberId);

        Map<String, Object> response = new HashMap<>();
        response.put("contentIds", contentIds);
        response.put("totalCount", totalCount);

        log.info("Wishlist content IDs retrieved: memberId={}, count={}", memberId, contentIds.size());
        return ApiResponse.onSuccess(WishlistSuccessStatus.WISHLIST_LIST_FOUND, response);
    }
}