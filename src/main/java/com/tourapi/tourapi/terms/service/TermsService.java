package com.tourapi.tourapi.terms.service;

import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.terms.TermsCode;
import com.tourapi.tourapi.terms.TermsDocument;

import java.util.List;
import java.util.Map;

public interface TermsService {
    // 현재 유효한 필수 약관 목록 조회
    List<TermsDocument> getCurrentRequiredTerms();

    // 특정 약관의 현재 버전 조회
    TermsDocument getCurrentTerms(TermsCode code);

    // 약관 동의 처리
    void agreeToTerms(Long memberId, TermsCode termsCode, String clientIp, String userAgent);

    // 회원가입 완료 상태 확인 및 업데이트
    void checkAndUpdateSignUpCompletion(Member member);

    // 특정 회원의 약관 동의 상태 조회
    Map<TermsCode, Boolean> getMemberTermsAgreementStatus(Long memberId);

    // 특정 회원이 모든 필수 약관에 동의했는지 확인
    boolean hasAgreedToAllRequiredTerms(Long memberId);
}
