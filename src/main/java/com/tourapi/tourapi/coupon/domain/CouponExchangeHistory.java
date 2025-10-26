package com.tourapi.tourapi.coupon.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "coupon_exchange_history",
        indexes = {
                @Index(name = "idx_exchange_member", columnList = "memberId"),
                @Index(name = "idx_exchange_coupon", columnList = "couponId"),
                @Index(name = "idx_exchange_created", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponExchangeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponId", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userCouponId")
    private UserCoupon userCoupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeType exchangeType; // 교환 타입

    @Column(nullable = false)
    private Integer exchangeCost; // 교환 비용

    @Column(length = 200)
    private String description; // 교환 설명

    // 교환 내역 생성 팩토리 메서드
    public static CouponExchangeHistory create(Member member, Coupon coupon, UserCoupon userCoupon, ExchangeType exchangeType, Integer exchangeCost, String description) {
        return CouponExchangeHistory.builder()
                .member(member)
                .coupon(coupon)
                .userCoupon(userCoupon)
                .exchangeType(exchangeType)
                .exchangeCost(exchangeCost)
                .description(description)
                .build();
    }
}
