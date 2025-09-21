package com.tourapi.tourapi.config;

import com.tourapi.tourapi.quest.domain.Quest;
import com.tourapi.tourapi.quest.enums.QuestType;
import com.tourapi.tourapi.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestDataInitializer implements CommandLineRunner {

    private final QuestRepository questRepository;

    @Override
    public void run(String... args) throws Exception {
        if (questRepository.count() > 0) {
            log.info("Quest 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("Quest 기본 데이터 초기화를 시작합니다...");

        // 산책하기 퀘스트
        Quest walkQuest = Quest.builder()
                .type(QuestType.WALK_DAILY)
                .title("산책하기")
                .description("오늘의 할 일 퀘스트 완료시 20point 지급!")
                .targetCount(1)
                .rewardPoints(20)
                .isActive(true)
                .isDaily(true)
                .build();
        questRepository.save(walkQuest);

        // 다이어리 남기기 퀘스트
        Quest diaryQuest = Quest.builder()
                .type(QuestType.DIARY_DAILY)
                .title("다이어리 남기기")
                .description("오늘의 할 일 퀘스트 완료시 30point 지급!")
                .targetCount(1)
                .rewardPoints(30)
                .isActive(true)
                .isDaily(true)
                .build();
        questRepository.save(diaryQuest);

        // 리뷰 남기기 퀘스트
        Quest reviewQuest = Quest.builder()
                .type(QuestType.REVIEW_DAILY)
                .title("리뷰 남기기")
                .description("오늘의 할 일 퀘스트 완료시 50point 지급!")
                .targetCount(1)
                .rewardPoints(50)
                .isActive(true)
                .isDaily(true)
                .build();
        questRepository.save(reviewQuest);

        log.info("Quest 기본 데이터 초기화가 완료되었습니다. 총 {}개의 퀘스트가 생성되었습니다.",
                questRepository.count());
    }
}