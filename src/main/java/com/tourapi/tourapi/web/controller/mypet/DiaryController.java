package com.tourapi.tourapi.web.controller.mypet;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.mypet.status.DiaryErrorStatus;
import com.tourapi.tourapi.common.exception.mypet.status.DiarySuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.mypet.dto.*;
import com.tourapi.tourapi.mypet.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Diary", description = "다이어리 관리 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "다이어리 작성", description = "새로운 다이어리를 작성합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        DiaryResponse response = diaryService.createDiary(principal.getId(), request);

        log.info("Diary created for member {}: {}", principal.getId(), response.getId());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_CREATED, response);
    }

    @GetMapping
    @Operation(summary = "다이어리 목록 조회", description = "사용자의 다이어리 목록을 페이징으로 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<DiaryListResponse>> getDiaries(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Pageable pageable = PageRequest.of(page, size);
        DiaryListResponse response = diaryService.getDiaries(principal.getId(), pageable);

        log.info("Diary list retrieved for member {}: {} items", principal.getId(), response.getTotalCount());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_LIST_FOUND, response);
    }

    @GetMapping("/all")
    @Operation(summary = "모든 다이어리 조회", description = "사용자의 모든 다이어리를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getAllDiaries(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        List<DiaryResponse> response = diaryService.getAllDiaries(principal.getId());

        log.info("All diaries retrieved for member {}: {} items", principal.getId(), response.size());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_LIST_FOUND, response);
    }

    @GetMapping("/{diaryId}")
    @Operation(summary = "다이어리 상세 조회", description = "특정 다이어리의 상세 정보를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001"})
    public ResponseEntity<ApiResponse<DiaryResponse>> getDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        DiaryResponse response = diaryService.getDiary(principal.getId(), diaryId);

        log.info("Diary retrieved for member {}: {}", principal.getId(), diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_DETAIL_FOUND, response);
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "다이어리 수정", description = "기존 다이어리를 수정합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001"})
    public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        DiaryResponse response = diaryService.updateDiary(principal.getId(), diaryId, request);

        log.info("Diary updated for member {}: {}", principal.getId(), diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_UPDATED, response);
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "다이어리 삭제", description = "다이어리를 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001"})
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        diaryService.deleteDiary(principal.getId(), diaryId);

        log.info("Diary deleted for member {}: {}", principal.getId(), diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_DELETED);
    }

    @GetMapping("/date-range")
    @Operation(summary = "기간별 다이어리 조회", description = "특정 기간 내의 다이어리를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<List<DiaryResponse>>> getDiariesByDateRange(
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        List<DiaryResponse> response = diaryService.getDiariesByDateRange(principal.getId(), startDate, endDate);

        log.info("Diaries by date range retrieved for member {}: {} items", principal.getId(), response.size());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_LIST_FOUND, response);
    }

    @GetMapping("/count")
    @Operation(summary = "다이어리 개수 조회", description = "사용자의 총 다이어리 개수를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Long>> getDiaryCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        long count = diaryService.getDiaryCount(principal.getId());

        log.info("Diary count retrieved for member {}: {}", principal.getId(), count);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_COUNT_FOUND, count);
    }

    @GetMapping("/latest")
    @Operation(summary = "최근 다이어리 조회", description = "가장 최근에 작성한 다이어리를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001"})
    public ResponseEntity<ApiResponse<DiaryResponse>> getLatestDiary(
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        DiaryResponse response = diaryService.getLatestDiary(principal.getId());

        log.info("Latest diary retrieved for member {}: {}", principal.getId(), response.getId());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_DETAIL_FOUND, response);
    }
}