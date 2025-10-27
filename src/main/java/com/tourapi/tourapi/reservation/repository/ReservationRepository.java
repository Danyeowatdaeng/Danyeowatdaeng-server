package com.tourapi.tourapi.reservation.repository;

import com.tourapi.tourapi.reservation.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 회원의 삭제되지 않은 예약 목록 조회 (페이징)
    Page<Reservation> findByMemberIdAndDeletedFalseOrderByReservationDateDesc(Long memberId, Pageable pageable);

    // 특정 예약 조회 (삭제되지 않은 것만)
    Optional<Reservation> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 특정 회원의 예약 개수
    long countByMemberIdAndDeletedFalse(Long memberId);
}

