package com.tourapi.tourapi.reservation.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.reservation.ReservationHandler;
import com.tourapi.tourapi.common.exception.reservation.status.ReservationErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.reservation.domain.Reservation;
import com.tourapi.tourapi.reservation.dto.ReservationRequest;
import com.tourapi.tourapi.reservation.dto.ReservationResponse;
import com.tourapi.tourapi.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    @Override
    public Reservation addReservation(Long memberId, ReservationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .member(member)
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .reservationDate(request.getReservationDate())
                .guestCount(request.getGuestCount())
                .reservationTimeSlot(request.getReservationTimeSlot())
                .isCouponUsed(request.getIsCouponUsed() != null ? request.getIsCouponUsed() : false)
                .deleted(false)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation added: memberId={}, name={}, date={}, guestCount={}",
                memberId, request.getName(), request.getReservationDate(), request.getGuestCount());

        return savedReservation;
    }

    @Override
    public void removeReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndMemberIdAndDeletedFalse(reservationId, memberId)
                .orElseThrow(() -> new ReservationHandler(ReservationErrorStatus.RESERVATION_NOT_FOUND));

        reservation.setDeleted(true);
        reservationRepository.save(reservation);

        log.info("Reservation removed: memberId={}, reservationId={}", memberId, reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(Long memberId, Pageable pageable) {
        Page<Reservation> reservations = reservationRepository.findByMemberIdAndDeletedFalseOrderByReservationDateDesc(memberId, pageable);
        return reservations.map(ReservationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReservationCount(Long memberId) {
        return reservationRepository.countByMemberIdAndDeletedFalse(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository
                .findByIdAndMemberIdAndDeletedFalse(reservationId, memberId)
                .orElseThrow(() -> new ReservationHandler(ReservationErrorStatus.RESERVATION_NOT_FOUND));

        return ReservationResponse.from(reservation);
    }
}

