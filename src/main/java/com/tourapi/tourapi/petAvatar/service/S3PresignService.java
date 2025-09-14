package com.tourapi.tourapi.petAvatar.service;

import lombok.Builder;
import lombok.Getter;

/**
 * S3 사전서명 URL 서비스 인터페이스
 * 클라이언트가 직접 S3에 업로드할 수 있는 사전서명 URL을 생성
 */
public interface S3PresignService {

    /**
     * 사전서명 업로드 요청
     */
    @Getter
    @Builder
    class PresignUploadRequest {
        private final String fileExtension; // .png, .jpg 등
        private final String mimeType;      // image/png, image/jpeg 등
        private final Long maxFileSize;     // 최대 파일 크기 (bytes)
        private final String prefix;        // S3 키 접두사 (input, result, thumb)
    }

    /**
     * 사전서명 업로드 응답
     */
    @Getter
    @Builder
    class PresignUploadResponse {
        private final String uploadUrl;     // 사전서명 업로드 URL
        private final String objectKey;     // S3 객체 키
        private final long expiresIn;       // 만료 시간 (초)
        private final String cdnUrl;        // 업로드 완료 후 CDN URL
    }

    /**
     * 사전서명 다운로드 요청
     */
    @Getter
    @Builder
    class PresignDownloadRequest {
        private final String objectKey;     // S3 객체 키
        private final long expiresIn;       // 만료 시간 (초)
    }

    /**
     * 사전서명 다운로드 응답
     */
    @Getter
    @Builder
    class PresignDownloadResponse {
        private final String downloadUrl;   // 사전서명 다운로드 URL
        private final String objectKey;     // S3 객체 키
        private final long expiresIn;       // 만료 시간 (초)
    }

    /**
     * 파일 업로드용 사전서명 URL 생성
     * @param request 업로드 요청 정보
     * @return 사전서명 업로드 URL과 메타데이터
     */
    PresignUploadResponse generatePresignUploadUrl(PresignUploadRequest request);

    /**
     * 파일 다운로드용 사전서명 URL 생성
     * @param request 다운로드 요청 정보
     * @return 사전서명 다운로드 URL과 메타데이터
     */
    PresignDownloadResponse generatePresignDownloadUrl(PresignDownloadRequest request);


    /**
     * S3 키가 유효한지 검증
     * @param objectKey S3 객체 키
     * @return 유효성 여부
     */
    boolean isValidObjectKey(String objectKey);

    /**
     * 업로드 정책 검증
     * @param request 업로드 요청
     * @return 검증 결과
     */
    boolean validateUploadRequest(PresignUploadRequest request);
}
