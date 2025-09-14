package com.tourapi.tourapi.web.controller.mypet;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.mypet.status.MyPetErrorStatus;
import com.tourapi.tourapi.common.exception.mypet.status.MyPetSuccessStatus;
import com.tourapi.tourapi.mypet.domain.PetDiary;
import com.tourapi.tourapi.mypet.dto.*;
import com.tourapi.tourapi.mypet.service.PetDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypet/diaries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MyPet Diary", description = "펫 다이어리 관리 API")
public class PetDiaryController {

    private final PetDiaryService petDiaryService;

    @PostMapping
    @Operation(summary = "다이어리 작성", description = "새로운 펫 다이어리를 작성합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    public ResponseEntity<ApiResponse<PetDiaryDetailResponse>> createDiary(
            @Valid @RequestBody PetDiaryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        PetDiary diary = petDiaryService.createDiary(memberId, request);
        PetDiaryDetailResponse response = PetDiaryDetailResponse.from(diary);

        log.info("Pet diary created: diaryId={}, memberId={}", diary.getId(), memberId);
        return ApiResponse.onSuccess(MyPetSuccessStatus.DIARY_CREATED, response);
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "다이어리 수정", description = "기존 펫 다이어리를 수정합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MyPetErrorStatus.class, codes = {"MYPET4001", "MYPET4003"}) // NOT_FOUND, ACCESS_DENIED
    public ResponseEntity<ApiResponse<PetDiaryDetailResponse>> updateDiary(
            @PathVariable Long diaryId,
            @Valid @RequestBody PetDiaryUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        PetDiary diary = petDiaryService.updateDiary(memberId, diaryId, request);
        PetDiaryDetailResponse response = PetDiaryDetailResponse.from(diary);

        log.info("Pet diary updated: diaryId={}, memberId={}", diaryId, memberId);
        return ApiResponse.onSuccess(MyPetSuccessStatus.DIARY_UPDATED, response);
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "다이어리 삭제", description = "펫 다이어리를 삭제합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MyPetErrorStatus.class, codes = {"MYPET4001", "MYPET4003"})
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        petDiaryService.deleteDiary(memberId, diaryId);

        log.info("Pet diary deleted: diaryId={}, memberId={}", diaryId, memberId);
        return ApiResponse.onSuccess(MyPetSuccessStatus.DIARY_DELETED);
    }

    @GetMapping
    @Operation(
            summary = "다이어리 목록 조회",
            description = "내 펫 다이어리 목록을 조회합니다. 제목과 이미지(있는 경우)를 포함합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<Page<PetDiaryListResponse>>> getDiaryList(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        Page<PetDiary> diaryPage = petDiaryService.getDiaryList(memberId, pageable);
        Page<PetDiaryListResponse> responsePage = diaryPage.map(PetDiaryListResponse::from);

        log.info("Pet diary list retrieved: memberId={}, totalElements={}",
                memberId, diaryPage.getTotalElements());
        return ApiResponse.onSuccess(MyPetSuccessStatus.DIARY_LIST_FOUND, responsePage);
    }

    @GetMapping("/{diaryId}")
    @Operation(
            summary = "다이어리 상세 조회",
            description = "펫 다이어리 상세 정보를 조회합니다. 제목, 본문, 이미지, 등록시간을 포함합니다."
    )
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MyPetErrorStatus.class, codes = {"MYPET4001", "MYPET4003"})
    public ResponseEntity<ApiResponse<PetDiaryDetailResponse>> getDiary(
            @Parameter(description = "다이어리 ID", required = true)
            @PathVariable Long diaryId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }

        Long memberId = principal.getId();
        PetDiary diary = petDiaryService.getDiary(memberId, diaryId);
        PetDiaryDetailResponse response = PetDiaryDetailResponse.from(diary);

        log.info("Pet diary detail retrieved: diaryId={}, memberId={}", diaryId, memberId);
        return ApiResponse.onSuccess(MyPetSuccessStatus.DIARY_DETAIL_FOUND, response);
    }
}