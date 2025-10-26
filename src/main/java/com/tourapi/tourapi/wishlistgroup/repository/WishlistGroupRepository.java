package com.tourapi.tourapi.wishlistgroup.repository;

import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistGroupRepository extends JpaRepository<WishlistGroup, Long> {

    // 특정 회원의 삭제되지 않은 그룹 목록 조회
    List<WishlistGroup> findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(Long memberId);

    // 특정 회원의 특정 그룹 조회 (삭제되지 않은 것만)
    Optional<WishlistGroup> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);

    // 특정 회원의 공개 그룹 목록 조회
    List<WishlistGroup> findByMemberIdAndIsPublicAndDeletedFalseOrderByCreatedAtDesc(Long memberId, Boolean isPublic);

    // 모든 공개 그룹 목록 조회
    List<WishlistGroup> findByIsPublicTrueAndDeletedFalseOrderByCreatedAtDesc();

    // 특정 회원의 그룹 개수
    long countByMemberIdAndDeletedFalse(Long memberId);

    // 특정 회원이 특정 그룹을 가지고 있는지 확인
    boolean existsByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);
}
