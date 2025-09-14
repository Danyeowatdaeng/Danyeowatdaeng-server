package com.tourapi.tourapi.wishlist.repository;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 특정 회원의 삭제되지 않은 찜하기 목록 조회 (페이징)
    Page<Wishlist> findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 특정 회원의 특정 찜하기 조회 (삭제되지 않은 것만)
    Optional<Wishlist> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 특정 회원이 특정 관광지를 찜했는지 확인
    Optional<Wishlist> findByMemberIdAndContentIdAndDeletedFalse(Long memberId, Long contentId);

    // 특정 회원의 찜하기 개수
    long countByMemberIdAndDeletedFalse(Long memberId);

    // 특정 관광지의 찜하기 개수
    long countByContentIdAndDeletedFalse(Long contentId);

    // 특정 회원이 찜한 관광지 ID 목록 조회
    @Query("SELECT w.contentId FROM Wishlist w WHERE w.member.id = :memberId AND w.deleted = false")
    List<Long> findContentIdsByMemberIdAndDeletedFalse(@Param("memberId") Long memberId);

    // 특정 회원이 특정 관광지를 찜했는지 확인 (boolean)
    boolean existsByMemberIdAndContentIdAndDeletedFalse(Long memberId, Long contentId);
}