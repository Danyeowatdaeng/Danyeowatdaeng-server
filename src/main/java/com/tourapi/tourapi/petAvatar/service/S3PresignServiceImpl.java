package com.tourapi.tourapi.petAvatar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3PresignServiceImpl implements S3PresignService {

    private final S3Presigner s3Presigner;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.cloudfrontDomain:}")
    private String cloudfrontDomain;

    @Value("${aws.s3.presignExpireSeconds:600}")
    private long defaultExpireSeconds;

    @Value("${aws.s3.maxFileSize:10485760}") // 10MB 기본값
    private long maxFileSize;

    // 허용된 MIME 타입
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );

    // 허용된 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );

    // S3 키 패턴 검증 (보안)
    private static final Pattern S3_KEY_PATTERN = Pattern.compile(
            "^(input|result|thumb)/\\d{4}/\\d{2}/\\d{2}/[a-f0-9-]+\\.(jpg|jpeg|png|webp|gif)$"
    );

    @Override
    public PresignUploadResponse generatePresignUploadUrl(PresignUploadRequest request) {
        // 요청 검증
        if (!validateUploadRequest(request)) {
            throw new IllegalArgumentException("Invalid upload request");
        }

        // S3 키 생성
        String objectKey = generateObjectKey(request.getPrefix(), request.getFileExtension());
        
        // PutObject 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(request.getMimeType())
                .contentLength(request.getMaxFileSize())
                .build();

        // 사전서명 요청 생성
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(request.getMaxFileSize() != null ? 
                    Math.min(request.getMaxFileSize() / 1024, defaultExpireSeconds) : defaultExpireSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        // 사전서명 URL 생성
        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        
        // CDN URL 생성
        String cdnUrl = s3Service.generateCdnUrl(objectKey);

        log.info("Generated presigned upload URL for key: {}, expires in: {} seconds", 
                objectKey, defaultExpireSeconds);

        return PresignUploadResponse.builder()
                .uploadUrl(presignedUrl)
                .objectKey(objectKey)
                .expiresIn(defaultExpireSeconds)
                .cdnUrl(cdnUrl)
                .build();
    }

    @Override
    public PresignDownloadResponse generatePresignDownloadUrl(PresignDownloadRequest request) {
        // S3 키 검증
        if (!isValidObjectKey(request.getObjectKey())) {
            throw new IllegalArgumentException("Invalid object key: " + request.getObjectKey());
        }

        // GetObject 요청 생성
        software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = 
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(request.getObjectKey())
                        .build();

        // 사전서명 요청 생성
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(request.getExpiresIn()))
                .getObjectRequest(getObjectRequest)
                .build();

        // 사전서명 URL 생성
        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        log.info("Generated presigned download URL for key: {}, expires in: {} seconds", 
                request.getObjectKey(), request.getExpiresIn());

        return PresignDownloadResponse.builder()
                .downloadUrl(presignedUrl)
                .objectKey(request.getObjectKey())
                .expiresIn(request.getExpiresIn())
                .build();
    }

    @Override
    public boolean isValidObjectKey(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return false;
        }

        // 패턴 검증
        if (!S3_KEY_PATTERN.matcher(objectKey).matches()) {
            return false;
        }

        // 우리 버킷의 키인지 확인
        return objectKey.startsWith("input/") || objectKey.startsWith("result/") || objectKey.startsWith("thumb/");
    }

    @Override
    public boolean validateUploadRequest(PresignUploadRequest request) {
        if (request == null) {
            return false;
        }

        // 파일 확장자 검증
        if (request.getFileExtension() == null || 
            !ALLOWED_EXTENSIONS.contains(request.getFileExtension().toLowerCase())) {
            log.warn("Invalid file extension: {}", request.getFileExtension());
            return false;
        }

        // MIME 타입 검증
        if (request.getMimeType() == null || 
            !ALLOWED_MIME_TYPES.contains(request.getMimeType().toLowerCase())) {
            log.warn("Invalid MIME type: {}", request.getMimeType());
            return false;
        }

        // 파일 크기 검증
        if (request.getMaxFileSize() != null && request.getMaxFileSize() > maxFileSize) {
            log.warn("File size too large: {} bytes (max: {} bytes)", 
                    request.getMaxFileSize(), maxFileSize);
            return false;
        }

        // 접두사 검증
        if (request.getPrefix() != null && 
            !Set.of("input", "result", "thumb").contains(request.getPrefix())) {
            log.warn("Invalid prefix: {}", request.getPrefix());
            return false;
        }

        return true;
    }

    /**
     * S3 객체 키 생성
     */
    private String generateObjectKey(String prefix, String fileExtension) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        String safePrefix = prefix != null ? prefix : "input";
        String safeExtension = fileExtension != null ? fileExtension : ".png";
        
        return String.format("%s/%s/%s%s", safePrefix, datePath, uuid, safeExtension);
    }
}
