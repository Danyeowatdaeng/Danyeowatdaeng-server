package com.tourapi.tourapi.web.controller.storage;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.petAvatar.dto.PresignUploadResponse;
import com.tourapi.tourapi.petAvatar.dto.CreateAvatarFromStorageRequest;
import com.tourapi.tourapi.petAvatar.dto.CreateAvatarFromStorageResponse;
import com.tourapi.tourapi.petAvatar.service.S3PresignService;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
     * PetAvatar 전용 사전서명 업로드 URL 생성
     * avatarId 하위 경로로 업로드하도록 prefix를 강제합니다.
     */
    @PostMapping("/presign/pet-avatars/{avatarId}")
    @Operation(
        summary = "PetAvatar 업로드용 사전서명 URL 생성",
        description = "지정된 avatarId 경로 하위로 업로드할 수 있는 사전서명 URL을 생성합니다."
    )
    public ResponseEntity<ApiResponse<PresignUploadResponse>> presignPetAvatarUpload(
            @Parameter(name = "avatarId", in = ParameterIn.PATH, required = true, description = "대상 PetAvatar ID")
            @PathVariable("avatarId") Long avatarId) {

        try {
            String enforcedPrefix = "result"; // S3PresignServiceImpl은 input|result|thumb 만 허용

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

            log.info("Generated pet-avatar presigned URL: {} for avatar {}", presignResponse.getObjectKey(), avatarId);
            return ApiResponse.onSuccess(SuccessStatus.OK, response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid presign request for avatar {}: {}", avatarId, e.getMessage());
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            log.error("Failed to generate pet-avatar presigned URL for avatar {}", avatarId, e);
            return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    /**
     * S3에 업로드된 이미지로 새 PetAvatar 생성
     */
    @PostMapping("/pet-avatars")
    @Operation(
        summary = "S3 URL로 새 PetAvatar 생성",
        description = "presign 업로드 완료된 s3Key/cdnUrl로 새 커스텀 PetAvatar를 생성합니다."
    )
    public ResponseEntity<ApiResponse<CreateAvatarFromStorageResponse>> createPetAvatarFromStorage(
            @RequestBody CreateAvatarFromStorageRequest request) {
        try {
            String displayName = (request.getDisplayName() == null || request.getDisplayName().isBlank())
                ? "Custom Avatar"
                : request.getDisplayName();

            var created = petAvatarService.createCustomPetAvatarFromStorage(
                com.tourapi.tourapi.petAvatar.enums.PetType.CUSTOM,
                displayName,
                request.getS3Key(),
                request.getCdnUrl(),
                request.getMime(),
                com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle.PIXEL,
                request.getMemberId()
            );

            CreateAvatarFromStorageResponse response = CreateAvatarFromStorageResponse.builder()
                .id(created.getId())
                .code(created.getCode())
                .imageUrl(created.getCdnUrl())
                .storageKey(created.getResultKey())
                .imageVersion(created.getVersion())
                .build();

            return ApiResponse.onSuccess(SuccessStatus.OK, response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid create-from-storage request: {}", e.getMessage());
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            log.error("Failed to create PetAvatar from storage", e);
            return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, null);
        }
    }


}
