package com.tourapi.tourapi.web.controller.point;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.point.status.PointErrorStatus;
import com.tourapi.tourapi.common.exception.point.status.PointSuccessStatus;
import com.tourapi.tourapi.point.domain.Point;
import com.tourapi.tourapi.point.dto.PointResponse;
import com.tourapi.tourapi.point.dto.PointSummaryResponse;
import com.tourapi.tourapi.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Point", description = "포인트 관리 API")
public class PointController {

    private final PointService pointService;

    @GetMapping("/balance")
    @Operation(summary = "현재 포인트 잔액 조회", description = "현재 보유한 포인트 잔액을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getCurrentBalance(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Integer balance = pointService.getCurrentBalance(memberId);

        Map<String, Integer> response = new HashMap<>();
        response.put("balance", balance);

        log.info("Point balance retrieved: memberId={}, balance={}", memberId, balance);
        return ApiResponse.onSuccess(PointSuccessStatus.POINT_BALANCE_FOUND, response);
    }

    @GetMapping("/summary")
    @Operation(summary = "포인트 요약 정보 조회", description = "포인트 잔액, 총 적립/사용 포인트, 거래 횟수 등 요약 정보를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<PointSummaryResponse>> getPointSummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        PointSummaryResponse summary = pointService.getPointSummary(memberId);

        log.info("Point summary retrieved: memberId={}, balance={}", memberId, summary.getCurrentBalance());
        return ApiResponse.onSuccess(PointSuccessStatus.POINT_SUMMARY_FOUND, summary);
    }

    @GetMapping("/history")
    @Operation(
            summary = "포인트 내역 조회",
            description = "포인트 적립/사용 내역을 페이징하여 조회합니다. 최신순으로 정렬됩니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Page<PointResponse>>> getPointHistory(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<Point> pointPage = pointService.getPointHistory(memberId, pageable);
        Page<PointResponse> responsePage = pointPage.map(PointResponse::from);

        log.info("Point history retrieved: memberId={}, totalElements={}",
                memberId, pointPage.getTotalElements());
        return ApiResponse.onSuccess(PointSuccessStatus.POINT_HISTORY_FOUND, responsePage);
    }

    @GetMapping("/check-daily/{pointType}")
    @Operation(summary = "오늘 포인트 적립 여부 확인", description = "오늘 해당 타입의 포인트를 이미 적립했는지 확인합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkDailyPointEarned(
            @PathVariable String pointType,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        try {
            Long memberId = principal.getId();
            com.tourapi.tourapi.point.enums.PointType type =
                    com.tourapi.tourapi.point.enums.PointType.valueOf(pointType.toUpperCase());

            boolean hasEarned = pointService.hasEarnedTodayForType(memberId, type);

            Map<String, Boolean> response = new HashMap<>();
            response.put("hasEarnedToday", hasEarned);

            log.info("Daily point check: memberId={}, pointType={}, hasEarned={}",
                    memberId, pointType, hasEarned);
            return ApiResponse.onSuccess(PointSuccessStatus.POINT_BALANCE_FOUND, response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid point type: {}", pointType);
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        }
    }
}