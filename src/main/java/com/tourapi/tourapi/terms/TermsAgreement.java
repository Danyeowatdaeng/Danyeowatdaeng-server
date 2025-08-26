package com.tourapi.tourapi.terms;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "terms_agreement",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_terms_agreement_member_terms", columnNames = {"memberId", "termsCode", "termsVersion"})
    },
    indexes = {
        @Index(name = "idx_terms_agreement_member", columnList = "memberId"),
        @Index(name = "idx_terms_agreement_terms", columnList = "termsCode, termsVersion"),
        @Index(name = "idx_terms_agreement_agreed_at", columnList = "agreedAt")
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
