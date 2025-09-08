package com.tourapi.tourapi.web.controller.mypet;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.diary.status.DiaryErrorStatus;
import com.tourapi.tourapi.common.exception.diary.status.DiarySuccessStatus;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.mypet.Diary;
import com.tourapi.tourapi.mypet.dto.*;
import com.tourapi.tourapi.mypet.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Diary", description = "다이어리 관리 API")
public class DiaryController {

    private final DiaryService diaryService;

    // 다이어리 생성
    @PostMapping
    @Operation(summary = "다이어리 생성", description = "새로운 다이어리를 생성합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4004", "DIARY4005", "DIARY5001"})
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> createDiary(
            @Valid @RequestBody DiaryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Diary diary = diaryService.createDiary(memberId, request);
        DiaryDetailResponse response = DiaryDetailResponse.from(diary);

        log.info("Diary created for member {}: {}", memberId, diary.getId());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_CREATED, response);
    }

    // 다이어리 수정
    @PutMapping("/{diaryId}")
    @Operation(summary = "다이어리 수정", description = "기존 다이어리를 수정합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001", "DIARY4002", "DIARY4004", "DIARY4005", "DIARY5002"})
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> updateDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Diary diary = diaryService.updateDiary(diaryId, memberId, request);
        DiaryDetailResponse response = DiaryDetailResponse.from(diary);

        log.info("Diary updated for member {}: {}", memberId, diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_UPDATED, response);
    }

    // 다이어리 삭제
    @DeleteMapping("/{diaryId}")
    @Operation(summary = "다이어리 삭제", description = "다이어리를 삭제합니다. (소프트 삭제)")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001", "DIARY4002", "DIARY5003"})
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        diaryService.deleteDiary(diaryId, memberId);

        log.info("Diary deleted for member {}: {}", memberId, diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_DELETED);
    }

    // 다이어리 목록 조회
    @GetMapping
    @Operation(summary = "다이어리 목록 조회", description = "사용자의 다이어리 목록을 조회합니다. (제목과 이미지 포함)")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<DiaryListResponse>> getDiaries(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();

        if (size <= 0) {
            // 페이징 없이 전체 조회
            List<Diary> diaries = diaryService.getDiariesByMember(memberId);
            List<DiaryResponse> diaryResponses = diaries.stream()
                    .map(DiaryResponse::from)
                    .collect(Collectors.toList());

            DiaryListResponse response = DiaryListResponse.from(diaryResponses, diaryResponses.size());

            log.info("All diaries retrieved for member {}: {} items", memberId, diaryResponses.size());
            return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_LIST_FOUND, response);
        } else {
            // 페이징 조회
            Pageable pageable = PageRequest.of(page, size);
            Page<Diary> diaryPage = diaryService.getDiariesByMember(memberId, pageable);

            List<DiaryResponse> diaryResponses = diaryPage.getContent().stream()
                    .map(DiaryResponse::from)
                    .collect(Collectors.toList());

            DiaryListResponse response = DiaryListResponse.from(
                    diaryResponses,
                    (int) diaryPage.getTotalElements(),
                    diaryPage.hasNext(),
                    diaryPage.getNumber(),
                    diaryPage.getTotalPages()
            );

            log.info("Paged diaries retrieved for member {}: page {}, {} items", memberId, page, diaryResponses.size());
            return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_LIST_FOUND, response);
        }
    }

    // 다이어리 상세 조회
    @GetMapping("/{diaryId}")
    @Operation(summary = "다이어리 상세 조회", description = "다이어리의 상세 정보를 조회합니다. (제목, 본문, 이미지, 작성시간 포함)")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = DiaryErrorStatus.class, codes = {"DIARY4001", "DIARY4002"})
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> getDiaryDetail(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Diary diary = diaryService.getDiaryDetail(diaryId, memberId);
        DiaryDetailResponse response = DiaryDetailResponse.from(diary);

        log.info("Diary detail retrieved for member {}: {}", memberId, diaryId);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_DETAIL_FOUND, response);
    }

    // 다이어리 개수 조회
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

        Long memberId = principal.getId();
        long count = diaryService.getDiaryCount(memberId);

        log.info("Diary count retrieved for member {}: {}", memberId, count);
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_COUNT_FOUND, count);
    }

    // 제목으로 다이어리 검색
    @GetMapping("/search")
    @Operation(summary = "다이어리 제목 검색", description = "제목으로 다이어리를 검색합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<DiaryListResponse>> searchDiaries(
            @Parameter(description = "검색할 제목", example = "산책")
            @RequestParam String title,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        List<Diary> diaries = diaryService.searchDiariesByTitle(memberId, title);
        List<DiaryResponse> diaryResponses = diaries.stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());

        DiaryListResponse response = DiaryListResponse.from(diaryResponses, diaryResponses.size());

        log.info("Diary search completed for member {}: query '{}', {} results", memberId, title, diaryResponses.size());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_SEARCH_FOUND, response);
    }

    // 최근 다이어리 조회
    @GetMapping("/recent")
    @Operation(summary = "최근 다이어리 조회", description = "최근 작성한 다이어리를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<DiaryListResponse>> getRecentDiaries(
            @Parameter(description = "조회할 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        List<Diary> diaries = diaryService.getRecentDiaries(memberId, limit);
        List<DiaryResponse> diaryResponses = diaries.stream()
                .map(DiaryResponse::from)
                .collect(Collectors.toList());

        DiaryListResponse response = DiaryListResponse.from(diaryResponses, diaryResponses.size());

        log.info("Recent diaries retrieved for member {}: {} items", memberId, diaryResponses.size());
        return ApiResponse.onSuccess(DiarySuccessStatus.DIARY_RECENT_FOUND, response);
    }
}