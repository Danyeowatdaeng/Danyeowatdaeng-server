package com.tourapi.tourapi.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tourapi.tourapi.terms.TermsAgreement;
import com.tourapi.tourapi.terms.TermsCode;

@Repository
public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long> {
    
    // 특정 회원의 특정 약관 동의 여부 조회
    Optional<TermsAgreement> findByMemberIdAndTermsCodeAndTermsVersion(Long memberId, TermsCode termsCode, String termsVersion);
    
    // 특정 회원의 모든 약관 동의 기록 조회
    List<TermsAgreement> findByMemberIdOrderByAgreedAtDesc(Long memberId);
    
    // 특정 회원이 특정 약관에 동의했는지 확인
    boolean existsByMemberIdAndTermsCodeAndTermsVersionAndAgreedTrue(Long memberId, TermsCode termsCode, String termsVersion);
    
    // 특정 회원의 필수 약관 동의 완료 여부 확인
    @Query("""
        SELECT COUNT(DISTINCT ta.termsCode) = 
               (SELECT COUNT(DISTINCT td2.code) 
                FROM TermsDocument td2 
                WHERE td2.required = true 
                  AND td2.effectiveFrom <= CURRENT_DATE 
                  AND (td2.effectiveTo IS NULL OR td2.effectiveTo >= CURRENT_DATE))
        FROM TermsAgreement ta
        JOIN TermsDocument td ON ta.termsCode = td.code 
                             AND ta.termsVersion = td.version
        WHERE ta.member.id = :memberId 
          AND ta.agreed = true
          AND td.required = true
          AND td.effectiveFrom <= CURRENT_DATE
          AND (td.effectiveTo IS NULL OR td.effectiveTo >= CURRENT_DATE)
        """)
    boolean hasAgreedToAllRequiredTerms(@Param("memberId") Long memberId);
}
