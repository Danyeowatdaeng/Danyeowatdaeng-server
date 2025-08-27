package com.tourapi.tourapi.member.service;

import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.PetAvatarHandler;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.terms.repository.TermsAgreementRepository;
import com.tourapi.tourapi.petAvatar.repository.PetAvatarRepository;
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
    private final TermsAgreementRepository termsAgreementRepository;
    private final PetAvatarRepository petAvatarRepository;
    
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
        
        // 이미 회원가입이 완료된 경우 중복 호출 방지
        if (member.isSignUpCompleted()) {
            throw new MemberHandler(MemberErrorStatus.MEMBER_ALREADY_SIGN_UP_COMPLETED);
        }
        
        // 필수 약관 동의 여부 확인
        if (!termsAgreementRepository.hasAgreedToAllRequiredTerms(memberId)) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }
        
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
