package com.tourapi.tourapi.wishlistgroup.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.wishlist.WishlistHandler;
import com.tourapi.tourapi.common.exception.wishlist.status.WishlistErrorStatus;
import com.tourapi.tourapi.common.exception.wishlistgroup.WishlistGroupHandler;
import com.tourapi.tourapi.common.exception.wishlistgroup.status.WishlistGroupErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlist.repository.WishlistRepository;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroupItem;
import com.tourapi.tourapi.wishlistgroup.dto.*;
import com.tourapi.tourapi.wishlistgroup.repository.WishlistGroupItemRepository;
import com.tourapi.tourapi.wishlistgroup.repository.WishlistGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WishlistGroupServiceImpl implements WishlistGroupService {

    private final WishlistGroupRepository wishlistGroupRepository;
    private final WishlistGroupItemRepository wishlistGroupItemRepository;
    private final WishlistRepository wishlistRepository;
    private final MemberRepository memberRepository;

    @Override
    public WishlistGroup createGroup(Long memberId, WishlistGroupCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new WishlistGroupHandler(WishlistGroupErrorStatus.INVALID_GROUP_NAME);
        }

        WishlistGroup group = WishlistGroup.builder()
                .member(member)
                .name(request.getName())
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .categoryImageUrl(request.getCategoryImageUrl())
                .deleted(false)
                .build();

        WishlistGroup savedGroup = wishlistGroupRepository.save(group);
        log.info("WishlistGroup created: memberId={}, groupId={}, name={}", 
                memberId, savedGroup.getId(), savedGroup.getName());

        return savedGroup;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistGroupResponse> getGroups(Long memberId, Boolean isPublic) {
        List<WishlistGroup> groups;
        
        if (isPublic != null) {
            groups = wishlistGroupRepository.findByMemberIdAndIsPublicAndDeletedFalseOrderByCreatedAtDesc(memberId, isPublic);
        } else {
            groups = wishlistGroupRepository.findByMemberIdAndDeletedFalseOrderByCreatedAtDesc(memberId);
        }

        return groups.stream()
                .map(WishlistGroupResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistGroupResponse getGroup(Long memberId, Long groupId) {
        WishlistGroup group = wishlistGroupRepository
                .findByIdAndMemberIdAndDeletedFalse(groupId, memberId)
                .orElseThrow(() -> new WishlistGroupHandler(WishlistGroupErrorStatus.WISHLIST_GROUP_NOT_FOUND));

        // 그룹의 찜하기 항목들 조회
        List<WishlistGroupItem> items = wishlistGroupItemRepository
                .findByWishlistGroupIdAndDeletedFalseOrderByCreatedAtDesc(groupId);

        List<Wishlist> wishlists = items.stream()
                .map(WishlistGroupItem::getWishlist)
                .collect(Collectors.toList());

        return WishlistGroupResponse.from(group, wishlists);
    }

    @Override
    public WishlistGroup updateGroup(Long memberId, Long groupId, WishlistGroupUpdateRequest request) {
        WishlistGroup group = wishlistGroupRepository
                .findByIdAndMemberIdAndDeletedFalse(groupId, memberId)
                .orElseThrow(() -> new WishlistGroupHandler(WishlistGroupErrorStatus.WISHLIST_GROUP_NOT_FOUND));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            group.setName(request.getName());
        }

        if (request.getIsPublic() != null) {
            group.setIsPublic(request.getIsPublic());
        }

        if (request.getCategoryImageUrl() != null) {
            group.setCategoryImageUrl(request.getCategoryImageUrl());
        }

        WishlistGroup updatedGroup = wishlistGroupRepository.save(group);
        log.info("WishlistGroup updated: memberId={}, groupId={}", memberId, groupId);

        return updatedGroup;
    }

    @Override
    public void deleteGroup(Long memberId, Long groupId) {
        WishlistGroup group = wishlistGroupRepository
                .findByIdAndMemberIdAndDeletedFalse(groupId, memberId)
                .orElseThrow(() -> new WishlistGroupHandler(WishlistGroupErrorStatus.WISHLIST_GROUP_NOT_FOUND));

        group.setDeleted(true);
        wishlistGroupRepository.save(group);

        // 그룹의 모든 항목도 삭제
        List<WishlistGroupItem> items = wishlistGroupItemRepository
                .findByWishlistGroupIdAndDeletedFalseOrderByCreatedAtDesc(groupId);
        
        items.forEach(item -> item.setDeleted(true));
        wishlistGroupItemRepository.saveAll(items);

        log.info("WishlistGroup deleted: memberId={}, groupId={}", memberId, groupId);
    }

    @Override
    public void addItemsToGroup(Long memberId, Long groupId, WishlistGroupAddItemRequest request) {
        WishlistGroup group = wishlistGroupRepository
                .findByIdAndMemberIdAndDeletedFalse(groupId, memberId)
                .orElseThrow(() -> new WishlistGroupHandler(WishlistGroupErrorStatus.WISHLIST_GROUP_NOT_FOUND));

        if (request.getWishlistIds() == null || request.getWishlistIds().isEmpty()) {
            throw new WishlistHandler(WishlistErrorStatus.WISHLIST_NOT_FOUND);
        }

        for (Long wishlistId : request.getWishlistIds()) {
            // 찜하기가 존재하고 회원의 것이 맞는지 확인
            Wishlist wishlist = wishlistRepository
                    .findByIdAndMemberIdAndDeletedFalse(wishlistId, memberId)
                    .orElseThrow(() -> new WishlistHandler(WishlistErrorStatus.WISHLIST_NOT_FOUND));

            // 이미 그룹에 추가된 항목인지 확인
            boolean exists = wishlistGroupItemRepository
                    .existsByWishlistGroupIdAndWishlistIdAndDeletedFalse(groupId, wishlistId);

            if (exists) {
                log.warn("Wishlist already in group: groupId={}, wishlistId={}", groupId, wishlistId);
                continue; // 이미 있으면 건너뛰기
            }

            WishlistGroupItem item = WishlistGroupItem.builder()
                    .wishlistGroup(group)
                    .wishlist(wishlist)
                    .deleted(false)
                    .build();

            wishlistGroupItemRepository.save(item);
            log.info("Wishlist added to group: groupId={}, wishlistId={}", groupId, wishlistId);
        }
    }

    @Override
    public void removeItemsFromGroup(Long memberId, Long groupId, WishlistGroupListItemRequest request) {
        WishlistGroup group = wishlistGroupRepository
                .findByIdAndMemberIdAndDeletedFalse(groupId, memberId)
                .orElseThrow(() -> new WishlistGroupHandler(WishlistGroupErrorStatus.WISHLIST_GROUP_NOT_FOUND));

        if (request.getWishlistIds() == null || request.getWishlistIds().isEmpty()) {
            return;
        }

        // 해당 찜하기들이 그룹에 존재하는지 확인하고 삭제
        for (Long wishlistId : request.getWishlistIds()) {
            wishlistGroupItemRepository
                    .findByWishlistGroupAndWishlistAndDeletedFalse(group, 
                            wishlistRepository.findById(wishlistId).orElse(null))
                    .ifPresent(item -> {
                        item.setDeleted(true);
                        wishlistGroupItemRepository.save(item);
                        log.info("Wishlist removed from group: groupId={}, wishlistId={}", groupId, wishlistId);
                    });
        }
    }
}
