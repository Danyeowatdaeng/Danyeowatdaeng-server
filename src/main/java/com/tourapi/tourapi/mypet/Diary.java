package com.tourapi.tourapi.mypet;

import com.tourapi.tourapi.common.entity.BaseEntity;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.mypet.enums.DiaryStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "diary",
        indexes = {
                @Index(name = "idx_diary_member", columnList = "memberId"),
                @Index(name = "idx_diary_status", columnList = "status"),
                @Index(name = "idx_diary_created_at", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DiaryStatus status = DiaryStatus.ACTIVE;

    // 정적 팩토리 메서드
    public static Diary create(Member member, String title, String content, String imageUrl) {
        return Diary.builder()
                .member(member)
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .status(DiaryStatus.ACTIVE)
                .build();
    }

    // 다이어리 수정
    public void updateDiary(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // 다이어리 삭제 (소프트 삭제)
    public void delete() {
        this.status = DiaryStatus.DELETED;
    }

    // 활성 상태 확인
    public boolean isActive() {
        return this.status == DiaryStatus.ACTIVE;
    }
}