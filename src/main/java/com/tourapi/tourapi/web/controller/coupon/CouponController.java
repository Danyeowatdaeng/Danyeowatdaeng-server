package com.tourapi.tourapi.web.controller.coupon;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.coupon.status.CouponErrorStatus;
import com.tourapi.tourapi.common.exception.coupon.status.CouponSuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.coupon.domain.Coupon;
import com.tourapi.tourapi.coupon.domain.UserCoupon;
import com.tourapi.tourapi.coupon.dto.*;
import com.tourapi.tourapi.coupon.enums.CouponType;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
import com.tourapi.tourapi.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Coupon", description = "쿠폰 관리 API")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "교환 가능한 쿠폰 목록 조회", description = "현재 교환 가능한 쿠폰 목록을 조회합니다.")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAvailableCoupons(
            @Parameter(description      = "페이지 정보 (page, size, sort)") 
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        Page<Coupon> coupons = couponService.getAvailableCoupons(pageable);
        Page<CouponResponse> response = coupons.map(CouponResponse::from);
    
        log.info("Available coupons retrieved: totalElements={}", coupons.getTotalElements());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_LIST_FOUND, response);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "타입별 쿠폰 목록 조회", description = "특정 타입의 교환 가능한 쿠폰 목록을 조회합니다.")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAvailableCouponsByType(
            @PathVariable CouponType type,
            @Parameter(description = "페이지 정보 (page, size, sort)") 
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        Page<Coupon> coupons = couponService.getAvailableCouponsByType(type, pageable);
        Page<CouponResponse> response = coupons.map(CouponResponse::from);

        log.info("Available coupons by type retrieved: type={}, totalElements={}", type, coupons.getTotalElements());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_LIST_FOUND, response);
    }

    @GetMapping("/{couponId}")
    @Operation(summary = "쿠폰 상세 조회", description = "특정 쿠폰의 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = CouponErrorStatus.class, codes = {"COUPON4001"})
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Long couponId) {

        Coupon coupon = couponService.getCouponById(couponId);
        CouponResponse response = CouponResponse.from(coupon);

        log.info("Coupon detail retrieved: couponId={}", couponId);
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_DETAIL_FOUND, response);
    }

    @PostMapping("/exchange")
    @Operation(summary = "쿠폰 교환", description = "포인트나 스탬프로 쿠폰을 교환합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = CouponErrorStatus.class, codes = {"COUPON4001", "COUPON4002", "COUPON4003", "COUPON4004", "COUPON4009", "COUPON4010", "COUPON4011"})
    public ResponseEntity<ApiResponse<UserCouponResponse>> exchangeCoupon(
            @Valid @RequestBody CouponExchangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        UserCoupon userCoupon = couponService.exchangeCoupon(memberId, request.getCouponId());
        UserCouponResponse response = UserCouponResponse.from(userCoupon);

        log.info("Coupon exchanged: memberId={}, couponId={}, userCouponId={}", 
                memberId, request.getCouponId(), userCoupon.getId());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_EXCHANGED, response);
    }

    @GetMapping("/my-coupons")
    @Operation(summary = "내 쿠폰 목록 조회", description = "내가 보유한 쿠폰 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Page<UserCouponResponse>>> getMyCoupons(
            @RequestParam(required = false) UserCouponStatus status,
            @Parameter(description = "페이지 정보 (page, size, sort)") 
            @PageableDefault(size = 20, sort = "id", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<UserCoupon> userCoupons = couponService.getUserCoupons(memberId, status, pageable);
        Page<UserCouponResponse> response = userCoupons.map(UserCouponResponse::from);

        log.info("My coupons retrieved: memberId={}, status={}, totalElements={}", 
                memberId, status, userCoupons.getTotalElements());
        return ApiResponse.onSuccess(CouponSuccessStatus.USER_COUPON_LIST_FOUND, response);
    }

    @PostMapping("/use")
    @Operation(summary = "쿠폰 사용", description = "쿠폰 코드로 쿠폰을 사용합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = CouponErrorStatus.class, codes = {"COUPON4006", "COUPON4007", "COUPON4008"})
    public ResponseEntity<ApiResponse<Void>> useCoupon(
            @Valid @RequestBody CouponUseRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        couponService.useCouponByCode(request.getCouponCode(), request.getUsedAtPlace());

        log.info("Coupon used: couponCode={}, usedAtPlace={}", request.getCouponCode(), request.getUsedAtPlace());
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_USED, null);
    }

    @GetMapping("/{couponId}/can-exchange")
    @Operation(summary = "쿠폰 교환 가능 여부 확인", description = "특정 쿠폰의 교환 가능 여부를 확인합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Boolean>> canExchangeCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        boolean canExchange = couponService.canExchangeCoupon(memberId, couponId);

        log.info("Coupon exchange availability checked: memberId={}, couponId={}, canExchange={}", 
                memberId, couponId, canExchange);
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_EXCHANGE_AVAILABLE, canExchange);
    }

    @GetMapping("/validate/{couponCode}")
    @Operation(summary = "쿠폰 코드 유효성 검증", description = "쿠폰 코드의 유효성을 검증합니다.")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Boolean>> validateCouponCode(@PathVariable String couponCode) {

        boolean isValid = couponService.validateCouponCode(couponCode);

        log.info("Coupon code validated: couponCode={}, isValid={}", couponCode, isValid);
        return ApiResponse.onSuccess(CouponSuccessStatus.COUPON_VALIDATED, isValid);
    }
}
