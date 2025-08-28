package com.tourapi.tourapi.terms.repository;

import com.tourapi.tourapi.terms.TermsCode;
import com.tourapi.tourapi.terms.TermsDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermsDocumentRepository extends JpaRepository<TermsDocument, Long> {
    
    // 특정 코드의 현재 유효한 버전 조회
    @Query("SELECT td FROM TermsDocument td WHERE td.code = :code AND (td.effectiveTo IS NULL OR td.effectiveTo >= CURRENT_DATE) AND td.effectiveFrom <= CURRENT_DATE ORDER BY td.effectiveFrom DESC")
    Optional<TermsDocument> findCurrentVersionByCode(@Param("code") TermsCode code);
    
    // 특정 코드의 모든 버전 조회 (최신순)
    List<TermsDocument> findByCodeOrderByEffectiveFromDesc(TermsCode code);
    
    // 현재 유효한 모든 필수 약관 조회
    @Query("SELECT td FROM TermsDocument td WHERE td.required = true AND (td.effectiveTo IS NULL OR td.effectiveTo >= CURRENT_DATE) AND td.effectiveFrom <= CURRENT_DATE")
    List<TermsDocument> findCurrentRequiredTerms();
}
