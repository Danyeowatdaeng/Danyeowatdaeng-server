package com.tourapi.tourapi.wishlistgroup.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.wishlist.domain.Wishlist;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "wishlist_group_item",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_group_item_group_wishlist", 
            columnNames = {"wishlistGroupId", "wishlistId"})
    },
    indexes = {
        @Index(name = "idx_wishlist_group_item_group", columnList = "wishlistGroupId"),
        @Index(name = "idx_wishlist_group_item_wishlist", columnList = "wishlistId"),
        @Index(name = "idx_wishlist_group_item_created_at", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistGroupItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlistGroupId", nullable = false)
    private WishlistGroup wishlistGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlistId", nullable = false)
    private Wishlist wishlist;

    // 삭제 여부 (soft delete)
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
