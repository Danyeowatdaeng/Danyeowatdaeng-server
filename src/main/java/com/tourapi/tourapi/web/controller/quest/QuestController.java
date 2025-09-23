// src/main/java/com/tourapi/tourapi/web/controller/quest/QuestController.java
package com.tourapi.tourapi.web.controller.quest;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.quest.dto.DailyQuestSummaryResponse;
import com.tourapi.tourapi.quest.service.QuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quest", description = "퀘스트 관리 API")
public class QuestController {

    private final QuestService questService;

    @GetMapping("/today")
    @Operation(summary = "오늘의 퀘스트 달성률 조회", description = "오늘의 일일퀘스트 달성률을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    public ResponseEntity<ApiResponse<DailyQuestSummaryResponse>> getTodayQuestProgress(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        DailyQuestSummaryResponse response = questService.getTodayQuestProgress(memberId);

        log.info("Today quest progress retrieved: memberId={}, completed={}/{}",
                memberId, response.getCompletedQuests(), response.getTotalQuests());
        return ApiResponse.onSuccess(SuccessStatus.OK, response);
    }
}