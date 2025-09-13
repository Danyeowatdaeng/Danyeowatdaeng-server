package com.tourapi.tourapi.petAvatar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.cloudfrontDomain:}")
    private String cloudfrontDomain;

    @Value("${aws.s3.presignExpireSeconds:600}")
    private long presignExpireSeconds;

    @Override
    public PresignUploadResult generatePresignUploadUrl(String fileExtension, String mimeType) {
        String objectKey = generateDateBasedKey("input", fileExtension);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(mimeType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpireSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        
        log.info("Generated presigned upload URL for key: {}", objectKey);
        return new PresignUploadResult(presignedUrl, objectKey, presignExpireSeconds);
    }

    @Override
    public UploadResult uploadFile(MultipartFile file, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String url = generateCdnUrl(key);
            log.info("Uploaded file to S3: {} -> {}", key, url);
            return new UploadResult(key, url);
        } catch (IOException e) {
            log.error("Failed to upload file to S3: {}", key, e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public UploadResult uploadBytes(byte[] bytes, String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        
        String url = generateCdnUrl(key);
        log.info("Uploaded bytes to S3: {} -> {}", key, url);
        return new UploadResult(key, url);
    }

    @Override
    public UploadResult uploadStringAutoDetect(String content) {
        // Base64 이미지인지 확인
        if (content.startsWith("data:image/")) {
            return uploadBase64Image(content);
        }
        
        // 일반 문자열로 처리
        String key = generateDateBasedKey("temp", ".txt");
        return uploadBytes(content.getBytes(), key, "text/plain");
    }

    private UploadResult uploadBase64Image(String base64Content) {
        try {
            // data:image/png;base64, 부분 제거
            String[] parts = base64Content.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid base64 image format");
            }
            
            String mimeType = parts[0].substring(5); // data: 제거
            String base64Data = parts[1];
            
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            String extension = getExtensionFromMimeType(mimeType);
            String key = generateDateBasedKey("input", extension);
            
            return uploadBytes(imageBytes, key, mimeType);
        } catch (Exception e) {
            log.error("Failed to upload base64 image", e);
            throw new RuntimeException("Failed to upload base64 image", e);
        }
    }

    @Override
    public InputStream downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    @Override
    public byte[] downloadFileAsBytes(String key) {
        try (InputStream inputStream = downloadFile(key)) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("Failed to download file from S3: {}", key, e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Deleted file from S3: {}", key);
    }

    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence in S3: {}", key, e);
            return false;
        }
    }

    @Override
    public String generateCdnUrl(String key) {
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            return cloudfrontDomain + "/" + key;
        }
        return generateS3Url(key);
    }

    @Override
    public String generateS3Url(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    @Override
    public String generateDateBasedKey(String prefix, String fileExtension) {
        LocalDate now = LocalDate.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString();
        return String.format("%s/%s/%s%s", prefix, datePath, uuid, fileExtension);
    }

    @Override
    public String generateThumbnailKey(String originalKey, int size) {
        // result/2025/01/15/uuid/avatar.png -> thumb/2025/01/15/uuid/avatar_256.webp
        String thumbKey = originalKey.replace("result/", "thumb/");
        if (thumbKey.endsWith(".png")) {
            thumbKey = thumbKey.replace(".png", "_" + size + ".webp");
        } else if (thumbKey.endsWith(".jpg") || thumbKey.endsWith(".jpeg")) {
            thumbKey = thumbKey.replace(".jpg", "_" + size + ".webp")
                             .replace(".jpeg", "_" + size + ".webp");
        }
        return thumbKey;
    }

    private String getExtensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".png";
        };
    }
}
