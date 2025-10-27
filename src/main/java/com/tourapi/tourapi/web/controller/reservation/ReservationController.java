package com.tourapi.tourapi.web.controller.reservation;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.reservation.status.ReservationErrorStatus;
import com.tourapi.tourapi.common.exception.reservation.status.ReservationSuccessStatus;
import com.tourapi.tourapi.reservation.domain.Reservation;
import com.tourapi.tourapi.reservation.dto.ReservationRequest;
import com.tourapi.tourapi.reservation.dto.ReservationResponse;
import com.tourapi.tourapi.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reservation", description = "예약 관리 API")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "예약 추가", description = "장소 예약을 추가합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    @ApiErrorCodeExample(value = ReservationErrorStatus.class, codes = {"RESERVATION4004"}) // INVALID_RESERVATION_DATA
    public ResponseEntity<ApiResponse<ReservationResponse>> addReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Reservation reservation = reservationService.addReservation(memberId, request);
        ReservationResponse response = ReservationResponse.from(reservation);

        log.info("Reservation added: memberId={}, name={}, date={}", 
                memberId, request.getName(), request.getReservationDate());
        return ApiResponse.onSuccess(ReservationSuccessStatus.RESERVATION_ADDED, response);
    }

    @GetMapping
    @Operation(
            summary = "예약 내역 조회",
            description = "내가 예약한 내역을 조회합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservations(
            @PageableDefault(size = 20, sort = "reservationDate,desc") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<ReservationResponse> responsePage = reservationService.getReservations(memberId, pageable);

        log.info("Reservations retrieved: memberId={}, totalElements={}",
                memberId, responsePage.getTotalElements());
        return ApiResponse.onSuccess(ReservationSuccessStatus.RESERVATION_RETRIEVED, responsePage);
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "예약 취소", description = "예약을 취소합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = ReservationErrorStatus.class, codes = {"RESERVATION4001"}) // RESERVATION_NOT_FOUND
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        reservationService.removeReservation(memberId, reservationId);

        log.info("Reservation cancelled: memberId={}, reservationId={}", memberId, reservationId);
        return ApiResponse.onSuccess(ReservationSuccessStatus.RESERVATION_REMOVED);
    }
}

