package com.tourapi.tourapi.reservation.dto;

import com.tourapi.tourapi.reservation.domain.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class ReservationResponse {

    @Schema(description = "예약 ID")
    private Long id;

    @Schema(description = "장소 이름")
    private String name;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "예약 날짜")
    private LocalDate reservationDate;

    @Schema(description = "예약 인원")
    private Integer guestCount;

    @Schema(description = "예약 시간대")
    private LocalTime reservationTimeSlot;

    @Schema(description = "쿠폰 사용 여부")
    private Boolean isCouponUsed;

    @Schema(description = "예약 생성 일시")
    private LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .imageUrl(reservation.getImageUrl())
                .reservationDate(reservation.getReservationDate())
                .guestCount(reservation.getGuestCount())
                .reservationTimeSlot(reservation.getReservationTimeSlot())
                .isCouponUsed(reservation.getIsCouponUsed())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}

