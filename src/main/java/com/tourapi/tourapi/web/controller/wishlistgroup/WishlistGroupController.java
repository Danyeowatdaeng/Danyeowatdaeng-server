package com.tourapi.tourapi.web.controller.wishlistgroup;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.wishlistgroup.status.WishlistGroupErrorStatus;
import com.tourapi.tourapi.common.exception.wishlistgroup.status.WishlistGroupSuccessStatus;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import com.tourapi.tourapi.wishlistgroup.dto.*;
import com.tourapi.tourapi.wishlistgroup.service.WishlistGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist-groups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "WishlistGroup", description = "찜하기 그룹 관리 API")
public class WishlistGroupController {

    private final WishlistGroupService wishlistGroupService;

    @PostMapping
    @Operation(summary = "그룹 생성", description = "새로운 찜하기 그룹을 생성합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4006"})
    public ResponseEntity<ApiResponse<WishlistGroupResponse>> createGroup(
            @Valid @RequestBody WishlistGroupCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        WishlistGroup group = wishlistGroupService.createGroup(memberId, request);
        WishlistGroupResponse response = WishlistGroupResponse.from(group);

        log.info("WishlistGroup created: memberId={}, groupId={}", memberId, group.getId());
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_GROUP_CREATED, response);
    }

    @GetMapping
    @Operation(summary = "그룹 목록 조회", description = "내 그룹 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<List<WishlistGroupResponse>>> getGroups(
            @Parameter(description = "공개 여부 필터 (true: 공개만, false: 비공개만, 미지정: 전체)")
            @RequestParam(required = false) Boolean isPublic,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        List<WishlistGroupResponse> groups = wishlistGroupService.getGroups(memberId, isPublic);

        log.info("WishlistGroups retrieved: memberId={}, count={}", memberId, groups.size());
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_GROUP_LIST_FOUND, groups);
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "그룹 상세 조회", description = "특정 그룹의 상세 정보와 찜하기 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4001"})
    public ResponseEntity<ApiResponse<WishlistGroupResponse>> getGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        WishlistGroupResponse response = wishlistGroupService.getGroup(memberId, groupId);

        log.info("WishlistGroup retrieved: memberId={}, groupId={}", memberId, groupId);
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_GROUP_FOUND, response);
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "그룹 수정", description = "그룹 정보를 수정합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4001"})
    public ResponseEntity<ApiResponse<WishlistGroupResponse>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody WishlistGroupUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        WishlistGroup group = wishlistGroupService.updateGroup(memberId, groupId, request);
        WishlistGroupResponse response = WishlistGroupResponse.from(group);

        log.info("WishlistGroup updated: memberId={}, groupId={}", memberId, groupId);
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_GROUP_UPDATED, response);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4001"})
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        wishlistGroupService.deleteGroup(memberId, groupId);

        log.info("WishlistGroup deleted: memberId={}, groupId={}", memberId, groupId);
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_GROUP_DELETED, null);
    }

    @PostMapping("/{groupId}/items")
    @Operation(summary = "그룹에 찜하기 추가", description = "그룹에 찜한 항목을 추가합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4005"})
    public ResponseEntity<ApiResponse<Void>> addItemsToGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody WishlistGroupAddItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        wishlistGroupService.addItemsToGroup(memberId, groupId, request);

        log.info("Items added to WishlistGroup: memberId={}, groupId={}, count={}", 
                memberId, groupId, request.getWishlistIds().size());
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_ADDED_TO_GROUP, null);
    }

    @DeleteMapping("/{groupId}/items")
    @Operation(summary = "그룹에서 찜하기 삭제", description = "그룹에서 찜한 항목을 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WishlistGroupErrorStatus.class, codes = {"WISHLISTGROUP4001"})
    public ResponseEntity<ApiResponse<Void>> removeItemsFromGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody WishlistGroupListItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        wishlistGroupService.removeItemsFromGroup(memberId, groupId, request);

        log.info("Items removed from WishlistGroup: memberId={}, groupId={}, count={}", 
                memberId, groupId, request.getWishlistIds().size());
        return ApiResponse.onSuccess(WishlistGroupSuccessStatus.WISHLIST_REMOVED_FROM_GROUP, null);
    }
}
