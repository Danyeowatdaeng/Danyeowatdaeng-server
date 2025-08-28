package com.tourapi.tourapi.member;

import com.tourapi.tourapi.auth.enums.OauthProvider;
import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.enums.Role;
import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.terms.TermsAgreement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_provider_user", columnNames = {"provider", "providerUserId"})
        },
        indexes = {
                @Index(name = "uk_member_nickname", columnList = "nickname", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OauthProvider provider;

    @Column(nullable = false, length = 191)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    @Column(length = 40, unique = true)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "petAvatarId", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PetAvatar petAvatar;

    @Column(nullable = false)
    @Builder.Default
    private Integer pointBalance = 0;

    @Column(nullable = false)
    private boolean isSignUpCompleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private LocalDateTime inactive;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TermsAgreement> termsAgreements = new ArrayList<>();

    // 명시적 게터 (일부 정적 분석기에서 Lombok 게터 인식 문제 보완)
    public Long getId() { return id; }
    public boolean isSignUpCompleted() { return isSignUpCompleted; }
}