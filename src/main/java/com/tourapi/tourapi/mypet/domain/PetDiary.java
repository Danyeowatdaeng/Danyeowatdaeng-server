package com.tourapi.tourapi.mypet.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pet_diary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetDiary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    // 삭제 여부 (soft delete)
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}