package com.tourapi.tourapi.web.controller.storage;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.petAvatar.dto.PresignUploadRequest;
import com.tourapi.tourapi.petAvatar.dto.PresignUploadResponse;
import com.tourapi.tourapi.petAvatar.service.S3PresignService;
import io.swagger.v3.oas.annotations.Operation;
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

    /**
     * 사전서명 업로드 URL 생성
     */
    @PostMapping("/presign-upload")
    @Operation(
        summary = "사전서명 업로드 URL 생성", 
        description = "클라이언트가 직접 S3에 파일을 업로드할 수 있는 사전서명 URL을 생성합니다."
    )
    public ResponseEntity<ApiResponse<PresignUploadResponse>> generatePresignUploadUrl(
            @RequestBody PresignUploadRequest request) {

        try {
            // S3PresignService 요청 객체로 변환
            S3PresignService.PresignUploadRequest presignRequest = 
                S3PresignService.PresignUploadRequest.builder()
                    .fileExtension(request.getExt())
                    .mimeType(request.getMime())
                    .maxFileSize(request.getMaxFileSize())
                    .prefix(request.getPrefix())
                    .build();

            // 사전서명 URL 생성
            S3PresignService.PresignUploadResponse presignResponse = 
                s3PresignService.generatePresignUploadUrl(presignRequest);

            // 응답 DTO로 변환
            PresignUploadResponse response = PresignUploadResponse.builder()
                .uploadUrl(presignResponse.getUploadUrl())
                .objectKey(presignResponse.getObjectKey())
                .expiresIn(presignResponse.getExpiresIn())
                .cdnUrl(presignResponse.getCdnUrl())
                .build();

            log.info("Generated presigned upload URL: {}", presignResponse.getObjectKey());

            return ApiResponse.onSuccess(SuccessStatus.OK, response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid presign upload request: {}", e.getMessage());
            return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, null);
        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL", e);
            return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

}
