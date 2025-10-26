package com.tourapi.tourapi.web.controller.storage;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.petAvatar.dto.PresignUploadResponse;
import com.tourapi.tourapi.petAvatar.dto.CreateAvatarFromStorageRequest;
import com.tourapi.tourapi.petAvatar.dto.CreateAvatarFromStorageResponse;
import com.tourapi.tourapi.petAvatar.dto.AttachImageRequest;
import com.tourapi.tourapi.petAvatar.service.S3PresignService;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;
import com.tourapi.tourapi.petAvatar.PetAvatar;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 스토리지 관련 API 컨트롤러
 * S3 사전서명 URL 생성을 담당
 */
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Storage")
public class StorageController {

    private final S3PresignService s3PresignService;
    private final PetAvatarService petAvatarService;

    /**
     * 새 PetAvatar 생성용 사전서명 업로드 URL 생성
     * Gemini 변환 결과를 저장할 수 있는 사전서명 URL을 생성합니다.
     */
    @PostMapping("/presign/pet-avatars")
    @Operation(
        summary = "새 PetAvatar 생성용 사전서명 URL 생성",
        description = "Gemini 변환 결과를 저장할 수 있는 사전서명 URL을 생성합니다. " +
                    "업로드 완료 후 /api/v1/storage/pet-avatars 엔드포인트로 새 PetAvatar를 생성할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<PresignUploadResponse>> presignPetAvatarUpload() {

        log.info("=== 새 PetAvatar 생성용 사전서명 URL 생성 요청 시작 ===");

        try {
            // Gemini 변환 결과 저장용 사전서명 URL 생성
            String enforcedPrefix = "result"; // Gemini 변환 결과는 result 폴더에 저장
            log.info("사전서명 URL 생성 중 - prefix: {}", enforcedPrefix);

            S3PresignService.PresignUploadRequest presignRequest =
                S3PresignService.PresignUploadRequest.builder()
                    .fileExtension(".png")
                    .mimeType("image/png")
                    .maxFileSize(null)
                    .prefix(enforcedPrefix)
                    .build();

            S3PresignService.PresignUploadResponse presignResponse =
                s3PresignService.generatePresignUploadUrl(presignRequest);

            PresignUploadResponse response = PresignUploadResponse.builder()
                .uploadUrl(presignResponse.getUploadUrl())
                .objectKey(presignResponse.getObjectKey())
                .expiresIn(presignResponse.getExpiresIn())
                .cdnUrl(presignResponse.getCdnUrl())
                .build();

            log.info("=== 새 PetAvatar 생성용 사전서명 URL 생성 완료 ===");
            log.info("생성된 S3 키: {}", presignResponse.getObjectKey());
            log.info("만료 시간: {} 초", presignResponse.getExpiresIn());
            log.info("CDN URL: {}", presignResponse.getCdnUrl());
            log.info("다음 단계: S3 업로드 완료 후 POST /api/v1/storage/pet-avatars로 새 PetAvatar 생성");
            
            return ApiResponse.onSuccess(SuccessStatus.OK, response);

        } catch (Exception e) {
            log.error("새 PetAvatar 생성용 사전서명 URL 생성 실패", e);
            return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


    /**
     * S3에 업로드된 이미지로 새 PetAvatar 생성
     */
    @PostMapping("/pet-avatars")
    @Operation(
        summary = "S3 URL로 새 PetAvatar 생성",
        description = "presign 업로드 완료된 s3Key/cdnUrl로 새 커스텀 PetAvatar를 생성합니다.",
        security = @SecurityRequirement(name = "accessToken")
    )
    public ResponseEntity<ApiResponse<CreateAvatarFromStorageResponse>> createPetAvatarFromStorage(
            @RequestBody CreateAvatarFromStorageRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        log.info("=== 새 PetAvatar 생성 요청 시작 ===");
        
        if (principal == null) {
            log.warn("인증되지 않은 사용자의 PetAvatar 생성 시도");
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }
        
        Long memberId = principal.getId();
        log.info("요청 정보 - DisplayName: {}, S3Key: {}, MemberId: {}", 
                request.getDisplayName(), request.getS3Key(), memberId);

        try {
            String displayName = (request.getDisplayName() == null || request.getDisplayName().isBlank())
                ? "Custom Avatar"
                : request.getDisplayName();

            log.info("PetAvatar 생성 중 - DisplayName: {}", displayName);
            var created = petAvatarService.createCustomPetAvatarFromStorage(
                com.tourapi.tourapi.petAvatar.enums.PetType.CUSTOM,
                displayName,
                request.getS3Key(),
                request.getCdnUrl(),
                request.getMime(),
                com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle.PIXEL,
                memberId
            );

            CreateAvatarFromStorageResponse response = CreateAvatarFromStorageResponse.builder()
                .id(created.getId())
                .code(created.getCode())
                .imageUrl(created.getCdnUrl())
                .storageKey(created.getResultKey())
                .imageVersion(created.getVersion())
                .build();

            log.info("=== 새 PetAvatar 생성 완료 ===");
            log.info("생성된 PetAvatar - ID: {}, Code: {}, Version: {}", 
                    created.getId(), created.getCode(), created.getVersion());

            return ApiResponse.onSuccess(SuccessStatus.OK, response);
        } catch (IllegalArgumentException e) {
            log.warn("새 PetAvatar 생성 실패 - 에러: {}", e.getMessage());
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            log.error("새 PetAvatar 생성 중 예외 발생", e);
            return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


}
