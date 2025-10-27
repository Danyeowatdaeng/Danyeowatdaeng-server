package com.tourapi.tourapi.reservation.service;

import com.tourapi.tourapi.reservation.domain.Reservation;
import com.tourapi.tourapi.reservation.dto.ReservationRequest;
import com.tourapi.tourapi.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {

    /**
     * 예약 추가
     */
    Reservation addReservation(Long memberId, ReservationRequest request);

    /**
     * 예약 삭제 (soft delete)
     */
    void removeReservation(Long memberId, Long reservationId);

    /**
     * 예약 목록 조회
     */
    Page<ReservationResponse> getReservations(Long memberId, Pageable pageable);

    /**
     * 예약 개수 조회
     */
    long getReservationCount(Long memberId);

    /**
     * 예약 단건 조회
     */
    ReservationResponse getReservation(Long memberId, Long reservationId);
}

