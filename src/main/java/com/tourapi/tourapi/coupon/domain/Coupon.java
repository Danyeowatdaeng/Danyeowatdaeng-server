package com.tourapi.tourapi.coupon.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.coupon.enums.CouponDiscountType;
import com.tourapi.tourapi.coupon.enums.CouponType;
import com.tourapi.tourapi.coupon.enums.ExchangeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon",
        indexes = {
                @Index(name = "idx_coupon_type", columnList = "type"),
                @Index(name = "idx_coupon_active", columnList = "isActive"),
                @Index(name = "idx_coupon_dates", columnList = "startDate, endDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 쿠폰명

    @Column(length = 500)
    private String description; // 쿠폰 설명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type; // 쿠폰 타입

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponDiscountType discountType; // 할인 타입

    @Column(nullable = false)
    private Integer discountValue; // 할인 값 (금액 또는 퍼센트)

    @Column(nullable = false)
    private Integer exchangeCost; // 교환 비용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeType exchangeType; // 교환 타입 (POINT, STAMP)

    @Column(nullable = false)
    private Integer validityDays; // 유효기간 (일)

    @Column(nullable = false)
    private Integer maxExchangeCount; // 최대 교환 가능 수량

    @Column(nullable = false)
    @Builder.Default
    private Integer currentExchangeCount = 0; // 현재 교환된 수량

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 여부

    @Column
    private LocalDateTime startDate; // 쿠폰 시작일

    @Column
    private LocalDateTime endDate; // 쿠폰 종료일

    // 교환 가능 여부 확인
    public boolean isExchangeable() {
        if (!isActive) {
            return false;
        }
        
        if (currentExchangeCount >= maxExchangeCount) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }

    // 교환 수량 증가
    public void incrementExchangeCount() {
        this.currentExchangeCount++;
    }
}
