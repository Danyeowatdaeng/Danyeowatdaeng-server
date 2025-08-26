package com.tourapi.tourapi.terms;

import com.tourapi.tourapi.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "terms_document",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_terms_document_code_version", columnNames = {"code", "version"})
    },
    indexes = {
        @Index(name = "idx_terms_document_code", columnList = "code"),
        @Index(name = "idx_terms_document_effective_from", columnList = "effectiveFrom")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TermsDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TermsCode code;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentUrl;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column
    private LocalDate effectiveTo; // null이면 현재 유효한 버전
}
