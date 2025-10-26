package com.tourapi.tourapi.web.controller.coupon;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.coupon.status.CouponErrorStatus;
import com.tourapi.tourapi.common.exception.coupon.status.CouponSuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.coupon.domain.CouponExchangeHistory;
import com.tourapi.tourapi.coupon.dto.CouponExchangeHistoryResponse;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import com.tourapi.tourapi.coupon.service.CouponExchangeService;
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

@RestController
@RequestMapping("/api/coupons/exchange")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupon Exchange", description = "쿠폰 교환 내역 API")
public class CouponExchangeController {

    private final CouponExchangeService couponExchangeService;

    @GetMapping("/history")
    @Operation(summary = "쿠폰 교환 내역 조회", description = "내 쿠폰 교환 내역을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Page<CouponExchangeHistoryResponse>>> getExchangeHistory(
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<CouponExchangeHistory> history = couponExchangeService.getExchangeHistory(memberId, pageable);
        Page<CouponExchangeHistoryResponse> response = history.map(CouponExchangeHistoryResponse::from);

        log.info("Exchange history retrieved: memberId={}, totalElements={}", memberId, history.getTotalElements());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_EXCHANGE_HISTORY_FOUND, response);
    }

    @GetMapping("/history/type/{exchangeType}")
    @Operation(summary = "교환 타입별 내역 조회", description = "특정 교환 타입의 내역을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Page<CouponExchangeHistoryResponse>>> getExchangeHistoryByType(
            @PathVariable ExchangeType exchangeType,
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<CouponExchangeHistory> history = couponExchangeService.getExchangeHistoryByType(memberId, exchangeType, pageable);
        Page<CouponExchangeHistoryResponse> response = history.map(CouponExchangeHistoryResponse::from);

        log.info("Exchange history by type retrieved: memberId={}, exchangeType={}, totalElements={}", 
                memberId, exchangeType, history.getTotalElements());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_EXCHANGE_HISTORY_FOUND, response);
    }
}
