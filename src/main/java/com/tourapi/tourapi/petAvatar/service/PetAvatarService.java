package com.tourapi.tourapi.petAvatar.service;

import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;

import java.util.List;

public interface PetAvatarService {

    // PetAvatar 목록 조회
    List<PetAvatar> getAllActivePetAvatars();
    
    // 기본 PetAvatar 목록 조회
    List<PetAvatar> getDefaultPetAvatars();
    
    // 펫 타입별 PetAvatar 조회
    List<PetAvatar> getPetAvatarsByType(PetType petType);
    
    // 사용자별 사용 가능한 PetAvatar 조회
    List<PetAvatar> getAvailablePetAvatarsForMember(Long memberId);
    
    // 펫 타입별 사용자 사용 가능한 PetAvatar 조회
    List<PetAvatar> getAvailablePetAvatarsForMemberByType(Long memberId, PetType petType);
    
    // ID로 PetAvatar 조회
    PetAvatar getPetAvatarById(Long id);
    
    // 코드로 PetAvatar 조회
    PetAvatar getPetAvatarByCode(String code);
    
    // 사용자별 커스텀 PetAvatar 조회
    List<PetAvatar> getCustomPetAvatarsByMemberId(Long memberId);
    
    // 스타일별 PetAvatar 조회
    List<PetAvatar> getPetAvatarsByStyle(PetAvatarStyle style);
    
    // PetAvatar 선택 처리 (Member에 연결)
    void selectPetAvatarForMember(Long memberId, Long petAvatarId);
    
    // AI 확장용: 커스텀 PetAvatar 생성
    PetAvatar createCustomPetAvatar(PetType petType, String displayName, 
                                   String imageUrl, String originalImageUrl, 
                                   PetAvatarStyle style, Long memberId);
    
    // AI 확장용: PetAvatar 비활성화
    void deactivatePetAvatar(Long petAvatarId);
    
    // AI 확장용: PetAvatar 활성화
    void activatePetAvatar(Long petAvatarId);

    // MVP: 업로드된 이미지로 아바타 생성 (바이트 반환)
    byte[] generateAvatarFromUpload(byte[] imageBytes, String filename, String prompt);
}
