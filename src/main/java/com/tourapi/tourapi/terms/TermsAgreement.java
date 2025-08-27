package com.tourapi.tourapi.terms;

import java.time.LocalDateTime;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "terms_agreement",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_terms_agreement_member_terms", columnNames = {"memberId", "termsCode", "termsVersion"})
    },
    indexes = {
        @Index(name = "idx_terms_agreement_member", columnList = "memberId"),
        @Index(name = "idx_terms_agreement_terms", columnList = "termsCode, termsVersion"),
        @Index(name = "idx_terms_agreement_agreed_at", columnList = "agreedAt"),
        @Index(name = "idx_terms_agreement_member_agreed", columnList = "memberId, termsCode, termsVersion, agreed")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TermsCode termsCode;

    @Column(nullable = false, length = 20)
    private String termsVersion;

    @Column(nullable = false)
    private boolean agreed;

    @Column(nullable = false)
    private LocalDateTime agreedAt;
}
