package com.tourapi.tourapi.member.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.PetAvatarHandler;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    
    private final MemberRepository memberRepository;
    
    @Override
    public Member getAuthenticatedMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
    
    @Override
    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
    }
    
    @Override
    public boolean existsById(Long memberId) {
        return memberRepository.existsById(memberId);
    }
    
    @Override
    public void completeSignup(Long memberId) {
        Member member = getMemberById(memberId);
        
        // PetAvatar가 선택되었는지 확인
        if (member.getPetAvatar() == null) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_NOT_SELECTED);
        }
        
        member.setSignUpCompleted(true);
        memberRepository.save(member);
        
        log.info("Signup completed for member {}", memberId);
    }
    
    @Override
    public boolean hasSelectedPetAvatar(Long memberId) {
        Member member = getMemberById(memberId);
        return member.getPetAvatar() != null;
    }
}
