package com.tourapi.tourapi.web.controller.walk;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.walk.status.WalkErrorStatus;
import com.tourapi.tourapi.common.exception.walk.status.WalkSuccessStatus;
import com.tourapi.tourapi.walk.domain.Walk;
import com.tourapi.tourapi.walk.dto.*;
import com.tourapi.tourapi.walk.service.WalkService;
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

@RestController
@RequestMapping("/api/walk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Walk", description = "산책 기록 관리 API")
public class WalkController {

    private final WalkService walkService;

    @PostMapping
    @Operation(summary = "산책 기록 등록", description = "새로운 산책 기록을 등록합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    public ResponseEntity<ApiResponse<WalkDetailResponse>> createWalk(
            @Valid @RequestBody WalkCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Walk walk = walkService.createWalk(memberId, request);
        WalkDetailResponse response = WalkDetailResponse.from(walk);

        log.info("Walk created: walkId={}, memberId={}", walk.getId(), memberId);
        return ApiResponse.onSuccess(WalkSuccessStatus.WALK_CREATED, response);
    }

    @DeleteMapping("/{walkId}")
    @Operation(summary = "산책 기록 삭제", description = "산책 기록을 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WalkErrorStatus.class, codes = {"WALK4001", "WALK4003"})
    public ResponseEntity<ApiResponse<Void>> deleteWalk(
            @PathVariable Long walkId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        walkService.deleteWalk(memberId, walkId);

        log.info("Walk deleted: walkId={}, memberId={}", walkId, memberId);
        return ApiResponse.onSuccess(WalkSuccessStatus.WALK_DELETED);
    }

    @GetMapping
    @Operation(
            summary = "산책 기록 목록 조회",
            description = "내 산책 기록 목록을 조회합니다. 이미지와 등록시간을 포함합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Page<WalkListResponse>>> getWalkList(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<Walk> walkPage = walkService.getWalkList(memberId, pageable);
        Page<WalkListResponse> responsePage = walkPage.map(WalkListResponse::from);

        log.info("Walk list retrieved: memberId={}, totalElements={}",
                memberId, walkPage.getTotalElements());
        return ApiResponse.onSuccess(WalkSuccessStatus.WALK_LIST_FOUND, responsePage);
    }

    @GetMapping("/{walkId}")
    @Operation(
            summary = "산책 기록 상세 조회",
            description = "산책 기록 상세 정보를 조회합니다. 이미지, 등록시간, 수정시간을 포함합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = WalkErrorStatus.class, codes = {"WALK4001", "WALK4003"})
    public ResponseEntity<ApiResponse<WalkDetailResponse>> getWalk(
            @Parameter(description = "산책 기록 ID", required = true)
            @PathVariable Long walkId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Walk walk = walkService.getWalk(memberId, walkId);
        WalkDetailResponse response = WalkDetailResponse.from(walk);

        log.info("Walk detail retrieved: walkId={}, memberId={}", walkId, memberId);
        return ApiResponse.onSuccess(WalkSuccessStatus.WALK_DETAIL_FOUND, response);
    }
}