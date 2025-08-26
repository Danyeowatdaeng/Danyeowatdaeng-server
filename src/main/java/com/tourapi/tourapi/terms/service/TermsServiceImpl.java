package com.tourapi.tourapi.terms.service;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.terms.status.TermsErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.terms.TermsAgreement;
import com.tourapi.tourapi.terms.TermsCode;
import com.tourapi.tourapi.terms.TermsDocument;
import com.tourapi.tourapi.terms.repository.TermsAgreementRepository;
import com.tourapi.tourapi.terms.repository.TermsDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {
    
    private final TermsDocumentRepository termsDocumentRepository;
    private final TermsAgreementRepository termsAgreementRepository;
    private final MemberRepository memberRepository;
    
    // 현재 유효한 필수 약관 목록 조회
    @Override
    public List<TermsDocument> getCurrentRequiredTerms() {
        return termsDocumentRepository.findCurrentRequiredTerms();
    }
    
    // 특정 약관의 현재 버전 조회
    @Override
    public TermsDocument getCurrentTerms(TermsCode code) {
        return termsDocumentRepository.findCurrentVersionByCode(code)
            .orElseThrow(() -> new GeneralException(TermsErrorStatus.TERMS_NOT_FOUND));
    }
    
    // 약관 동의 처리 (단일)
    @Override
    public void agreeToTerms(Long memberId, TermsCode termsCode) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));
            
        TermsDocument termsDocument = getCurrentTerms(termsCode);
        
        // 이미 동의했는지 확인
        if (termsAgreementRepository.existsByMemberIdAndTermsCodeAndTermsVersionAndAgreedTrue(
                memberId, termsCode, termsDocument.getVersion())) {
            throw new GeneralException(TermsErrorStatus.TERMS_ALREADY_AGREED);
        }
        
        // 약관 동의 기록 생성
        TermsAgreement agreement = TermsAgreement.builder()
            .member(member)
            .termsCode(termsCode)
            .termsVersion(termsDocument.getVersion())
            .agreed(true)
            .agreedAt(LocalDateTime.now())
            .build();
            
        termsAgreementRepository.save(agreement);
        
        // 모든 필수 약관에 동의했는지 확인하고 회원가입 완료 상태 업데이트
        checkAndUpdateSignUpCompletion(member);
    }
    
    // 여러 약관 동의 처리 (배치)
    @Override
    public void agreeToMultipleTerms(Long memberId, List<TermsCode> termsCodes) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(MemberErrorStatus.MEMBER_NOT_FOUND));
        
        LocalDateTime now = LocalDateTime.now();
        
        for (TermsCode termsCode : termsCodes) {
            TermsDocument termsDocument = getCurrentTerms(termsCode);
            
            // 이미 동의했는지 확인
            if (termsAgreementRepository.existsByMemberIdAndTermsCodeAndTermsVersionAndAgreedTrue(
                    memberId, termsCode, termsDocument.getVersion())) {
                continue; // 이미 동의한 경우 건너뛰기
            }
            
            // 약관 동의 기록 생성
            TermsAgreement agreement = TermsAgreement.builder()
                .member(member)
                .termsCode(termsCode)
                .termsVersion(termsDocument.getVersion())
                .agreed(true)
                .agreedAt(now)
                .build();
                
            termsAgreementRepository.save(agreement);
        }
        
        // 모든 필수 약관에 동의했는지 확인하고 회원가입 완료 상태 업데이트
        checkAndUpdateSignUpCompletion(member);
    }
    
    // 회원가입 완료 상태 확인 및 업데이트
    @Override
    public void checkAndUpdateSignUpCompletion(Member member) {
        if (termsAgreementRepository.hasAgreedToAllRequiredTerms(member.getId())) {
            member.setSignUpCompleted(true);
            memberRepository.save(member);
        }
    }
    
    // 특정 회원의 약관 동의 상태 조회
    @Override
    public Map<TermsCode, Boolean> getMemberTermsAgreementStatus(Long memberId) {
        List<TermsDocument> requiredTerms = getCurrentRequiredTerms();
        Map<TermsCode, Boolean> agreementStatus = new HashMap<>();
        
        for (TermsDocument terms : requiredTerms) {
            boolean agreed = termsAgreementRepository.existsByMemberIdAndTermsCodeAndTermsVersionAndAgreedTrue(
                memberId, terms.getCode(), terms.getVersion());
            agreementStatus.put(terms.getCode(), agreed);
        }
        
        return agreementStatus;
    }
    
    // 특정 회원이 모든 필수 약관에 동의했는지 확인
    @Override
    public boolean hasAgreedToAllRequiredTerms(Long memberId) {
        return termsAgreementRepository.hasAgreedToAllRequiredTerms(memberId);
    }
}
