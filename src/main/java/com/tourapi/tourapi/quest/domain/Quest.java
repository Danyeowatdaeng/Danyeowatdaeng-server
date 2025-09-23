package com.tourapi.tourapi.quest.domain;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.quest.enums.QuestType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer targetCount;

    @Column(nullable = false)
    private Integer rewardPoints;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDaily = true;
}