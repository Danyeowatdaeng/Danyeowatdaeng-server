package com.tourapi.tourapi.wishlistgroup.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "wishlist_group",
    indexes = {
        @Index(name = "idx_wishlist_group_member", columnList = "memberId"),
        @Index(name = "idx_wishlist_group_created_at", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(length = 100, nullable = false)
    private String name; // 그룹 이름

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = true; // 공개/비공개 설정 (true: 공개, false: 비공개)

    @Column(columnDefinition = "TEXT")
    private String categoryImageUrl; // 카테고리 이미지 URL

    // 삭제 여부 (soft delete)
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
