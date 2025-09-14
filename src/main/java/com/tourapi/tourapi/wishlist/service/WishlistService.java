package com.tourapi.tourapi.wishlist.service;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.dto.WishlistAddRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WishlistService {

    /**
     * 찜하기 추가
     */
    Wishlist addToWishlist(Long memberId, WishlistAddRequest request);

    /**
     * 찜하기 삭제 (soft delete)
     */
    void removeFromWishlist(Long memberId, Long contentId);

    /**
     * 찜하기 목록 조회
     */
    Page<Wishlist> getWishlist(Long memberId, Pageable pageable);

    /**
     * 특정 관광지가 찜되어 있는지 확인
     */
    boolean isInWishlist(Long memberId, Long contentId);

    /**
     * 찜하기 개수 조회
     */
    long getWishlistCount(Long memberId);

    /**
     * 찜한 관광지 ID 목록 조회
     */
    List<Long> getWishlistContentIds(Long memberId);

    /**
     * 찜하기 토글 (있으면 삭제, 없으면 추가)
     */
    boolean toggleWishlist(Long memberId, WishlistAddRequest request);
}