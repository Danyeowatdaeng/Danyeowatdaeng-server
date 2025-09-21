package com.tourapi.tourapi.quest.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "member_quest",
        indexes = {
                @Index(name = "idx_member_quest_date", columnList = "memberId, questDate"),
                @Index(name = "idx_member_quest_type", columnList = "memberId, questType, questDate")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberQuest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(nullable = false)
    private LocalDate questDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRewardClaimed = false;

    // 편의를 위한 questType 컬럼 (Quest 조인 없이 조회 가능)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private com.tourapi.tourapi.quest.enums.QuestType questType;
}