package com.tourapi.tourapi.point.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.point.enums.PointType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "point",
        indexes = {
                @Index(name = "idx_point_member", columnList = "memberId"),
                @Index(name = "idx_point_type", columnList = "pointType"),
                @Index(name = "idx_point_created_at", columnList = "createdAt"),
                @Index(name = "idx_point_member_type_date", columnList = "memberId, pointType, createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType pointType;

    @Column(nullable = false)
    private Integer amount; // 양수: 적립, 음수: 사용

    @Column(length = 200)
    private String description; // 포인트 적립/사용 설명

    @Column
    private Long relatedId; // 관련된 엔티티 ID (산책 ID, 다이어리 ID 등)

    // 포인트 적립을 위한 정적 팩토리 메서드
    public static Point createEarn(Member member, PointType pointType, Integer amount, String description, Long relatedId) {
        return Point.builder()
                .member(member)
                .pointType(pointType)
                .amount(Math.abs(amount)) // 적립은 항상 양수
                .description(description)
                .relatedId(relatedId)
                .build();
    }

    // 포인트 사용을 위한 정적 팩토리 메서드
    public static Point createSpend(Member member, PointType pointType, Integer amount, String description, Long relatedId) {
        return Point.builder()
                .member(member)
                .pointType(pointType)
                .amount(-Math.abs(amount)) // 사용은 항상 음수
                .description(description)
                .relatedId(relatedId)
                .build();
    }
}