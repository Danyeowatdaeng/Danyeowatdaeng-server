package com.tourapi.tourapi.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReservationRequest {

    @Schema(description = "장소 이름", example = "강아지카페", required = true)
    @NotNull(message = "장소 이름은 필수입니다")
    private String name;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "예약 날짜", example = "2024-01-15", required = true)
    @NotNull(message = "예약 날짜는 필수입니다")
    private LocalDate reservationDate;

    @Schema(description = "예약 인원", example = "2", required = true)
    @NotNull(message = "예약 인원은 필수입니다")
    private Integer guestCount;

    @Schema(description = "예약 시간대", example = "14:00")
    private LocalTime reservationTimeSlot;

    @Schema(description = "쿠폰 사용 여부", example = "true")
    private Boolean isCouponUsed = false;
}

