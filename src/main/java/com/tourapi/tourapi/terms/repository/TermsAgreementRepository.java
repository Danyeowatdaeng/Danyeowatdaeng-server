package com.tourapi.tourapi.terms.repository;

import com.tourapi.tourapi.terms.TermsAgreement;
import com.tourapi.tourapi.terms.TermsCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
    
    // 특정 회원의 특정 약관 동의 여부 조회
    Optional<TermsAgreement> findByMemberIdAndTermsCodeAndTermsVersion(Long memberId, TermsCode termsCode, String termsVersion);
    
    // 특정 회원의 모든 약관 동의 기록 조회
    List<TermsAgreement> findByMemberIdOrderByAgreedAtDesc(Long memberId);
    
    // 특정 회원이 특정 약관에 동의했는지 확인
    boolean existsByMemberIdAndTermsCodeAndTermsVersionAndAgreedTrue(Long memberId, TermsCode termsCode, String termsVersion);
    
    // 특정 회원의 필수 약관 동의 완료 여부 확인
    @Query("SELECT COUNT(ta) = (SELECT COUNT(td) FROM TermsDocument td WHERE td.required = true AND (td.effectiveTo IS NULL OR td.effectiveTo >= CURRENT_DATE) AND td.effectiveFrom <= CURRENT_DATE) FROM TermsAgreement ta WHERE ta.member.id = :memberId AND ta.agreed = true AND ta.termsCode IN (SELECT td.code FROM TermsDocument td WHERE td.required = true AND (td.effectiveTo IS NULL OR td.effectiveTo >= CURRENT_DATE) AND td.effectiveFrom <= CURRENT_DATE)")
    boolean hasAgreedToAllRequiredTerms(@Param("memberId") Long memberId);
}
