package com.tourapi.tourapi.petAvatar.service;

import com.tourapi.tourapi.common.exception.petAvatar.PetAvatarHandler;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.repository.PetAvatarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PetAvatarServiceImpl implements PetAvatarService {

    private final PetAvatarRepository petAvatarRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<PetAvatar> getAllActivePetAvatars() {
        return petAvatarRepository.findByIsActiveTrue();
    }

    @Override
    public List<PetAvatar> getDefaultPetAvatars() {
        return petAvatarRepository.findByIsCustomFalseAndIsActiveTrue();
    }

    @Override
    public List<PetAvatar> getPetAvatarsByType(PetType petType) {
        return petAvatarRepository.findByPetAndIsActiveTrue(petType);
    }

    @Override
    public List<PetAvatar> getAvailablePetAvatarsForMember(Long memberId) {
        return petAvatarRepository.findAvailablePetAvatarsForMember(memberId);
    }

    @Override
    public List<PetAvatar> getAvailablePetAvatarsForMemberByType(Long memberId, PetType petType) {
        return petAvatarRepository.findAvailablePetAvatarsForMemberByType(memberId, petType);
    }

    @Override
    public PetAvatar getPetAvatarById(Long id) {
        return petAvatarRepository.findById(id)
                .orElseThrow(() -> new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_NOT_FOUND));
    }

    @Override
    public PetAvatar getPetAvatarByCode(String code) {
        return petAvatarRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_NOT_FOUND));
    }

    @Override
    public List<PetAvatar> getCustomPetAvatarsByMemberId(Long memberId) {
        return petAvatarRepository.findByMemberIdAndIsCustomTrueAndIsActiveTrue(memberId);
    }

    @Override
    public List<PetAvatar> getPetAvatarsByStyle(PetAvatarStyle style) {
        return petAvatarRepository.findByStyleAndIsActiveTrue(style);
    }

    @Override
    @Transactional
    public void selectPetAvatarForMember(Long memberId, Long petAvatarId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));
        
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        
        // PetAvatar가 활성화되어 있는지 확인
        if (!petAvatar.getIsActive()) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_INACTIVE);
        }
        
        // 커스텀 PetAvatar인 경우 소유권 확인
        if (petAvatar.getIsCustom() && !memberId.equals(petAvatar.getMemberId())) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_ACCESS_DENIED);
        }
        
        member.setPetAvatar(petAvatar);
        memberRepository.save(member);
        
        log.info("PetAvatar {} selected for member {}", petAvatarId, memberId);
    }

    @Override
    @Transactional
    public PetAvatar createCustomPetAvatar(PetType petType, String displayName, 
                                         String imageUrl, String originalImageUrl, 
                                         PetAvatarStyle style, Long memberId) {
        // 고유한 코드 생성
        String code = "CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PetAvatar customPetAvatar = PetAvatar.createCustom(
                petType, code, displayName, imageUrl, originalImageUrl, style, memberId
        );
        
        PetAvatar savedPetAvatar = petAvatarRepository.save(customPetAvatar);
        
        log.info("Custom PetAvatar created for member {}: {}", memberId, savedPetAvatar.getId());
        return savedPetAvatar;
    }

    @Override
    @Transactional
    public void deactivatePetAvatar(Long petAvatarId) {
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        petAvatar.deactivate();
        petAvatarRepository.save(petAvatar);
        
        log.info("PetAvatar {} deactivated", petAvatarId);
    }

    @Override
    @Transactional
    public void activatePetAvatar(Long petAvatarId) {
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        petAvatar.activate();
        petAvatarRepository.save(petAvatar);
        
        log.info("PetAvatar {} activated", petAvatarId);
    }
}
