package com.tourapi.tourapi.wishlistgroup.service;

import com.tourapi.tourapi.wishlist.domain.Wishlist;
import com.tourapi.tourapi.wishlistgroup.domain.WishlistGroup;
import com.tourapi.tourapi.wishlistgroup.dto.*;

import java.util.List;

public interface WishlistGroupService {

    /**
     * 그룹 생성
     */
    WishlistGroup createGroup(Long memberId, WishlistGroupCreateRequest request);

    /**
     * 그룹 목록 조회
     */
    List<WishlistGroupResponse> getGroups(Long memberId, Boolean isPublic);

    /**
     * 그룹 상세 조회 (항목 포함)
     */
    WishlistGroupResponse getGroup(Long memberId, Long groupId);

    /**
     * 그룹 수정
     */
    WishlistGroup updateGroup(Long memberId, Long groupId, WishlistGroupUpdateRequest request);

    /**
     * 그룹 삭제
     */
    void deleteGroup(Long memberId, Long groupId);

    /**
     * 그룹에 찜하기 추가
     */
    void addItemsToGroup(Long memberId, Long groupId, WishlistGroupAddItemRequest request);

    /**
     * 그룹에서 찜하기 삭제
     */
    void removeItemsFromGroup(Long memberId, Long groupId, WishlistGroupListItemRequest request);
}
