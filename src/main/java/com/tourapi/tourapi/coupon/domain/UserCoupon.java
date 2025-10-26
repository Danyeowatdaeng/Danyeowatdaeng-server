package com.tourapi.tourapi.coupon.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.coupon.enums.UserCouponStatus;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_coupon",
        indexes = {
                @Index(name = "idx_user_coupon_member", columnList = "memberId"),
                @Index(name = "idx_user_coupon_status", columnList = "status"),
                @Index(name = "idx_user_coupon_expire", columnList = "expireDate"),
                @Index(name = "idx_user_coupon_code", columnList = "couponCode")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponId", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserCouponStatus status = UserCouponStatus.ACTIVE; // 사용 상태

    @Column(nullable = false)
    private LocalDateTime expireDate; // 만료일

    @Column
    private LocalDateTime usedAt; // 사용일시

    @Column(length = 200)
    private String usedAtPlace; // 사용 장소

    @Column(length = 20, unique = true)
    private String couponCode; // 쿠폰 코드 (QR코드용)

    // 사용자 쿠폰 생성 팩토리 메서드
    public static UserCoupon create(Member member, Coupon coupon) {
        LocalDateTime expireDate = LocalDateTime.now().plusDays(coupon.getValidityDays());
        String couponCode = generateCouponCode();
        
        return UserCoupon.builder()
                .member(member)
                .coupon(coupon)
                .status(UserCouponStatus.ACTIVE)
                .expireDate(expireDate)
                .couponCode(couponCode)
                .build();
    }

    // 쿠폰 코드 생성
    private static String generateCouponCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    // 쿠폰 사용
    public void use(String usedAtPlace) {
        this.status = UserCouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.usedAtPlace = usedAtPlace;
    }

    // 쿠폰 만료 처리
    public void expire() {
        this.status = UserCouponStatus.EXPIRED;
    }

    // 사용 가능 여부 확인
    public boolean isUsable() {
        if (status != UserCouponStatus.ACTIVE) {
            return false;
        }
        
        return LocalDateTime.now().isBefore(expireDate);
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireDate);
    }
}
