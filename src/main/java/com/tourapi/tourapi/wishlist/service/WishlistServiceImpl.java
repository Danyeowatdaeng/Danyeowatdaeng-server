package com.tourapi.tourapi.wishlist.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.WishlistHandler;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.dto.WishlistAddRequest;
import com.tourapi.tourapi.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;

    @Override
    public Wishlist addToWishlist(Long memberId, WishlistAddRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));

        // 이미 찜한 관광지인지 확인
        Optional<Wishlist> existingWishlist = wishlistRepository
                .findByMemberIdAndContentIdAndDeletedFalse(memberId, request.getContentId());

        if (existingWishlist.isPresent()) {
            throw new WishlistHandler(WishlistErrorStatus.ALREADY_ADDED_TO_WISHLIST);
        }

        Wishlist wishlist = Wishlist.builder()
                .member(member)
                .contentId(request.getContentId())
                .contentTypeId(request.getContentTypeId())
                .title(request.getTitle())
                .address(request.getAddress())
                .imageUrl(request.getImageUrl())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .deleted(false)
                .build();

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        log.info("Wishlist added: memberId={}, contentId={}, title={}",
                memberId, request.getContentId(), request.getTitle());

        return savedWishlist;
    }

    @Override
    public void removeFromWishlist(Long memberId, Long contentId) {
        Wishlist wishlist = wishlistRepository
                .findByMemberIdAndContentIdAndDeletedFalse(memberId, contentId)
                .orElseThrow(() -> new WishlistHandler(WishlistErrorStatus.WISHLIST_NOT_FOUND));

        wishlist.setDeleted(true);
        wishlistRepository.save(wishlist);

        log.info("Wishlist removed: memberId={}, contentId={}", memberId, contentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Wishlist> getWishlist(Long memberId, Pageable pageable) {
        return wishlistRepository.findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long memberId, Long contentId) {
        return wishlistRepository.existsByMemberIdAndContentIdAndDeletedFalse(memberId, contentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getWishlistCount(Long memberId) {
        return wishlistRepository.countByMemberIdAndDeletedFalse(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishlistContentIds(Long memberId) {
        return wishlistRepository.findContentIdsByMemberIdAndDeletedFalse(memberId);
    }

    @Override
    public boolean toggleWishlist(Long memberId, WishlistAddRequest request) {
        Optional<Wishlist> existingWishlist = wishlistRepository
                .findByMemberIdAndContentIdAndDeletedFalse(memberId, request.getContentId());

        if (existingWishlist.isPresent()) {
            // 이미 찜한 상태면 삭제
            removeFromWishlist(memberId, request.getContentId());
            return false; // 삭제됨
        } else {
            // 찜하지 않은 상태면 추가
            addToWishlist(memberId, request);
            return true; // 추가됨
        }
    }
}