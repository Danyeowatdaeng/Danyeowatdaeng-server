package com.tourapi.tourapi.wishlistgroup.repository;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroupItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistGroupItemRepository extends JpaRepository<WishlistGroupItem, Long> {

    // 특정 그룹의 삭제되지 않은 항목 목록 조회
    List<WishlistGroupItem> findByWishlistGroupIdAndDeletedFalseOrderByCreatedAtDesc(Long wishlistGroupId);

    // 특정 그룹과 찜하기로 항목 조회 (삭제되지 않은 것만)
    Optional<WishlistGroupItem> findByWishlistGroupAndWishlistAndDeletedFalse(
            WishlistGroup wishlistGroup, Wishlist wishlist);

    // 특정 찜하기가 포함된 그룹 항목 목록 조회
    List<WishlistGroupItem> findByWishlistIdAndDeletedFalse(Long wishlistId);

    // 특정 찜하기가 특정 그룹에 포함되어 있는지 확인
    boolean existsByWishlistGroupIdAndWishlistIdAndDeletedFalse(Long wishlistGroupId, Long wishlistId);

    // 특정 그룹 내 항목 개수
    long countByWishlistGroupIdAndDeletedFalse(Long wishlistGroupId);

    // 특정 그룹의 항목들을 ID 리스트로 조회
    @Query("SELECT wgi.wishlist.id FROM WishlistGroupItem wgi " +
           "WHERE wgi.wishlistGroup.id = :wishlistGroupId AND wgi.deleted = false")
    List<Long> findWishlistIdsByWishlistGroupIdAndDeletedFalse(@Param("wishlistGroupId") Long wishlistGroupId);

    // 특정 그룹과 찜하기 리스트에 해당하는 항목들 조회
    @Query("SELECT wgi FROM WishlistGroupItem wgi " +
           "WHERE wgi.wishlistGroup.id = :wishlistGroupId " +
           "AND wgi.wishlist.id IN :wishlistIds " +
           "AND wgi.deleted = false")
    List<WishlistGroupItem> findByWishlistGroupIdAndWishlistIdsAndDeletedFalse(
            @Param("wishlistGroupId") Long wishlistGroupId, 
            @Param("wishlistIds") List<Long> wishlistIds);
}
