// src/main/java/com/tourapi/tourapi/quest/service/QuestServiceImpl.java
package com.tourapi.tourapi.quest.service;

import com.tourapi.tourapi.quest.domain.MemberQuest;
import com.tourapi.tourapi.quest.domain.Quest;
import com.tourapi.tourapi.quest.dto.DailyQuestSummaryResponse;
import com.tourapi.tourapi.quest.dto.QuestProgressResponse;
import com.tourapi.tourapi.quest.enums.QuestType;
import com.tourapi.tourapi.quest.repository.MemberQuestRepository;
import com.tourapi.tourapi.quest.repository.QuestRepository;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final MemberQuestRepository memberQuestRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public DailyQuestSummaryResponse getTodayQuestProgress(Long memberId) {
        LocalDate today = LocalDate.now();

        // 오늘의 멤버 퀘스트 조회
        List<MemberQuest> memberQuests = memberQuestRepository
                .findByMemberIdAndQuestDateWithQuest(memberId, today);

        // 만약 오늘 퀘스트가 없다면 초기화
        if (memberQuests.isEmpty()) {
            initializeDailyQuests(memberId, today);
            memberQuests = memberQuestRepository
                    .findByMemberIdAndQuestDateWithQuest(memberId, today);
        }

        List<QuestProgressResponse> questProgresses = memberQuests.stream()
                .map(QuestProgressResponse::from)
                .collect(Collectors.toList());

        return DailyQuestSummaryResponse.from(questProgresses);
    }

    @Override
    public void updateQuestProgress(Long memberId, QuestType questType, Integer incrementCount) {
        LocalDate today = LocalDate.now();
        Optional<MemberQuest> memberQuestOpt = memberQuestRepository
                .findByMemberIdAndQuestTypeAndQuestDate(memberId, questType, today);

        if (memberQuestOpt.isPresent()) {
            MemberQuest memberQuest = memberQuestOpt.get();
            if (!memberQuest.getIsCompleted()) {
                int newCount = memberQuest.getCurrentCount() + incrementCount;
                memberQuest.setCurrentCount(newCount);

                // 목표 달성 시 완료 처리
                if (newCount >= memberQuest.getQuest().getTargetCount()) {
                    memberQuest.setIsCompleted(true);
                    log.info("Quest completed: memberId={}, questType={}, date={}",
                            memberId, questType, today);
                }

                memberQuestRepository.save(memberQuest);
            }
        } else {
            // 퀘스트가 없으면 초기화 후 다시 시도
            initializeDailyQuests(memberId, today);
            updateQuestProgress(memberId, questType, incrementCount);
        }
    }

    @Override
    public void initializeDailyQuests(Long memberId, LocalDate date) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Quest> dailyQuests = questRepository.findByIsActiveTrueAndIsDailyTrue();

        for (Quest quest : dailyQuests) {
            Optional<MemberQuest> existingQuest = memberQuestRepository
                    .findByMemberIdAndQuestTypeAndQuestDate(memberId, quest.getType(), date);

            if (existingQuest.isEmpty()) {
                MemberQuest memberQuest = MemberQuest.builder()
                        .member(member)
                        .quest(quest)
                        .questDate(date)
                        .questType(quest.getType())
                        .currentCount(0)
                        .isCompleted(false)
                        .isRewardClaimed(false)
                        .build();

                memberQuestRepository.save(memberQuest);
            }
        }

        log.info("Daily quests initialized: memberId={}, date={}, questCount={}",
                memberId, date, dailyQuests.size());
    }
}