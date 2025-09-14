package com.tourapi.tourapi.petAvatar.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * S3 스토리지 서비스 인터페이스
 * PetAvatar 이미지 업로드/다운로드/관리를 담당
 */
public interface S3Service {

    /**
     * 업로드 결과를 담는 레코드
     */
    record UploadResult(String key, String url) {}

    /**
     * 사전서명 업로드 URL 생성
     * @param fileExtension 파일 확장자 (예: .png, .jpg)
     * @param mimeType MIME 타입 (예: image/png)
     * @return 사전서명 URL과 S3 키
     */
    PresignUploadResult generatePresignUploadUrl(String fileExtension, String mimeType);

    /**
     * 사전서명 업로드 결과
     */
    record PresignUploadResult(String uploadUrl, String objectKey, long expiresIn) {}

    /**
     * MultipartFile을 S3에 업로드
     * @param file 업로드할 파일
     * @param key S3 키
     * @return 업로드 결과
     */
    UploadResult uploadFile(MultipartFile file, String key);

    /**
     * 바이트 배열을 S3에 업로드
     * @param bytes 업로드할 바이트 배열
     * @param key S3 키
     * @param contentType 콘텐츠 타입
     * @return 업로드 결과
     */
    UploadResult uploadBytes(byte[] bytes, String key, String contentType);

    /**
     * 문자열을 S3에 업로드 (자동 MIME 타입 감지)
     * @param content 업로드할 문자열
     * @return 업로드 결과
     */
    UploadResult uploadStringAutoDetect(String content);

    /**
     * S3에서 파일 다운로드
     * @param key S3 키
     * @return 파일 스트림
     */
    InputStream downloadFile(String key);

    /**
     * S3에서 파일을 바이트 배열로 다운로드
     * @param key S3 키
     * @return 파일 바이트 배열
     */
    byte[] downloadFileAsBytes(String key);

    /**
     * S3에서 파일 삭제
     * @param key S3 키
     */
    void deleteFile(String key);

    /**
     * S3 키가 존재하는지 확인
     * @param key S3 키
     * @return 존재 여부
     */
    boolean fileExists(String key);

    /**
     * S3 키로부터 CDN URL 생성
     * @param key S3 키
     * @return CDN URL
     */
    String generateCdnUrl(String key);

    /**
     * S3 키로부터 직접 S3 URL 생성
     * @param key S3 키
     * @return S3 URL
     */
    String generateS3Url(String key);

    /**
     * 날짜 기반 S3 키 생성
     * @param prefix 접두사 (input, result, thumb)
     * @param fileExtension 파일 확장자
     * @return 생성된 S3 키
     */
    String generateDateBasedKey(String prefix, String fileExtension);

    /**
     * 썸네일용 S3 키 생성
     * @param originalKey 원본 S3 키
     * @param size 썸네일 크기 (예: 256)
     * @return 썸네일 S3 키
     */
    String generateThumbnailKey(String originalKey, int size);
}

