package com.tourapi.tourapi.wishlist.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "wishlist",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wishlist_member_content", columnNames = {"memberId", "contentId"})
        },
        indexes = {
                @Index(name = "idx_wishlist_member", columnList = "memberId"),
                @Index(name = "idx_wishlist_content", columnList = "contentId"),
                @Index(name = "idx_wishlist_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Long contentId; // 관광지 ID

    @Column(nullable = false)
    private Integer contentTypeId; // 관광지 타입 ID

    @Column(length = 200)
    private String title; // 관광지 이름 (캐시용)

    @Column(columnDefinition = "TEXT")
    private String address; // 주소 (캐시용)

    @Column(columnDefinition = "TEXT")
    private String imageUrl; // 이미지 URL (캐시용)

    @Column
    private Double latitude; // 위도 (캐시용)

    @Column
    private Double longitude; // 경도 (캐시용)

    // 삭제 여부 (soft delete)
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}