package com.tourapi.tourapi.reservation.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservation",
        indexes = {
                @Index(name = "idx_reservation_member", columnList = "memberId"),
                @Index(name = "idx_reservation_date", columnList = "reservationDate"),
                @Index(name = "idx_reservation_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(nullable = false, length = 200)
    private String name; // 장소 이름

    @Column(columnDefinition = "TEXT")
    private String imageUrl; // 이미지 URL

    @Column(nullable = false)
    private LocalDate reservationDate; // 예약 날짜

    @Column(nullable = false)
    private Integer guestCount; // 예약 인원

    @Column
    private LocalTime reservationTimeSlot; // 예약 시간대

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCouponUsed = false; // 쿠폰 사용 여부

    // 삭제 여부 (soft delete)
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}

