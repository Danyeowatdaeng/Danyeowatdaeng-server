package com.tourapi.tourapi.petAvatar.service;

import com.tourapi.tourapi.common.exception.petAvatar.PetAvatarHandler;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.common.exception.member.MemberHandler;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.repository.PetAvatarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.Map;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PetAvatarServiceImpl implements PetAvatarService {

    private final PetAvatarRepository petAvatarRepository;
    private final MemberRepository memberRepository;
    private final com.tourapi.tourapi.petAvatar.config.PetAvatarProperties properties;

    @Override
    public List<PetAvatar> getAllActivePetAvatars() {
        return petAvatarRepository.findByIsActiveTrue();
    }

    @Override
    public List<PetAvatar> getDefaultPetAvatars() {
        return petAvatarRepository.findByIsCustomFalseAndIsActiveTrue();
    }

    @Override
    public List<PetAvatar> getPetAvatarsByType(PetType petType) {
        return petAvatarRepository.findByPetAndIsActiveTrue(petType);
    }

    @Override
    public List<PetAvatar> getAvailablePetAvatarsForMember(Long memberId) {
        return petAvatarRepository.findAvailablePetAvatarsForMember(memberId);
    }

    @Override
    public List<PetAvatar> getAvailablePetAvatarsForMemberByType(Long memberId, PetType petType) {
        return petAvatarRepository.findAvailablePetAvatarsForMemberByType(memberId, petType);
    }

    @Override
    public PetAvatar getPetAvatarById(Long id) {
        return petAvatarRepository.findById(id)
                .orElseThrow(() -> new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_NOT_FOUND));
    }

    @Override
    public PetAvatar getPetAvatarByCode(String code) {
        return petAvatarRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_NOT_FOUND));
    }

    @Override
    public List<PetAvatar> getCustomPetAvatarsByMemberId(Long memberId) {
        return petAvatarRepository.findByMemberIdAndIsCustomTrueAndIsActiveTrue(memberId);
    }

    @Override
    public List<PetAvatar> getPetAvatarsByStyle(PetAvatarStyle style) {
        return petAvatarRepository.findByStyleAndIsActiveTrue(style);
    }

    @Override
    @Transactional
    public void selectPetAvatarForMember(Long memberId, Long petAvatarId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(MemberErrorStatus.MEMBER_NOT_FOUND));
        
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        
        // PetAvatar가 활성화되어 있는지 확인
        if (!petAvatar.getIsActive()) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_INACTIVE);
        }
        
        // 커스텀 PetAvatar인 경우 소유권 확인
        if (petAvatar.getIsCustom() && !memberId.equals(petAvatar.getMemberId())) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_ACCESS_DENIED);
        }
        
        member.setPetAvatar(petAvatar);
        memberRepository.save(member);
        
        log.info("PetAvatar {} selected for member {}", petAvatarId, memberId);
    }

    @Override
    @Transactional
    public PetAvatar createCustomPetAvatar(PetType petType, String displayName, 
                                         String imageUrl, 
                                         PetAvatarStyle style, Long memberId) {
        // 고유한 코드 생성
        String code = "CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PetAvatar customPetAvatar = PetAvatar.createCustom(
                petType, code, displayName, imageUrl, style, memberId
        );
        
        PetAvatar savedPetAvatar = petAvatarRepository.save(customPetAvatar);
        
        log.info("Custom PetAvatar created for member {}: {}", memberId, savedPetAvatar.getId());
        return savedPetAvatar;
    }

    @Override
    @Transactional
    public void deactivatePetAvatar(Long petAvatarId) {
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        petAvatar.deactivate();
        petAvatarRepository.save(petAvatar);
        
        log.info("PetAvatar {} deactivated", petAvatarId);
    }

    @Override
    @Transactional
    public void activatePetAvatar(Long petAvatarId) {
        PetAvatar petAvatar = getPetAvatarById(petAvatarId);
        petAvatar.activate();
        petAvatarRepository.save(petAvatar);
        
        log.info("PetAvatar {} activated", petAvatarId);
    }

    @Override
    public byte[] generateAvatarFromUpload(byte[] imageBytes, String filename, String prompt) {
        log.info("=== PetAvatar 생성 시작 ===");
        log.info("서비스 활성화 상태: {}", properties.isEnabled());
        log.info("프로바이더: {}", properties.getProvider());
        log.info("파일명: {}", filename);
        log.info("이미지 크기: {} bytes", imageBytes != null ? imageBytes.length : "null");
        log.info("프롬프트 길이: {} characters", prompt != null ? prompt.length() : "null");
        
        if (!properties.isEnabled()) {
            log.error("PetAvatar 서비스가 비활성화되어 있습니다.");
            throw new PetAvatarHandler(PetAvatarErrorStatus.GEMINI_SERVICE_DISABLED);
        }
        if ("mock".equalsIgnoreCase(properties.getProvider())) {
            log.info("Mock 모드로 실행 - 빈 바이트 배열 반환");
            return new byte[]{};
        }
        if (imageBytes == null || imageBytes.length == 0) {
            log.error("이미지 데이터가 null이거나 비어있습니다. 크기: {}", imageBytes != null ? imageBytes.length : "null");
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_INVALID_IMAGE_FORMAT);
        }
        if ("gemini".equalsIgnoreCase(properties.getProvider())) {
            log.info("Gemini API 호출 시작");
            return callGeminiImageGenerateFromBytes(new RestTemplate(), imageBytes, filename, prompt);
        }
        log.error("지원하지 않는 프로바이더: {}", properties.getProvider());
        throw new PetAvatarHandler(PetAvatarErrorStatus.PROVIDER_UNSUPPORTED);
    }

    private byte[] callGeminiImageGenerateFromBytes(RestTemplate restTemplate, byte[] imageBytes, String filename, String prompt) {
        String b64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String modelPath = "models/gemini-2.5-flash-image-preview:generateContent";
        String endpoint = properties.getProviderBaseUrl() + "/" + modelPath + "?key=" + properties.getApiKey();

        String mime = guessMimeType(filename == null ? null : filename);
        log.info("=== Gemini API 요청 정보 ===");
        log.info("엔드포인트: {}", endpoint.replaceAll("key=[^&]+", "key=***"));
        log.info("API 키 길이: {} characters", properties.getApiKey() != null ? properties.getApiKey().length() : 0);
        log.info("API 키 시작: {}", properties.getApiKey() != null ? properties.getApiKey().substring(0, Math.min(10, properties.getApiKey().length())) + "..." : "null");
        log.info("MIME 타입: {}", mime);
        log.info("Base64 인코딩 크기: {} characters", b64.length());
        log.info("프롬프트: {}", prompt);
        
        Map<String, Object> textPart = Map.of("text",
            // 편집 지시를 명확히: "무엇을 유지하고 무엇을 바꿀지"
            prompt
        );

        Map<String, Object> imagePart = Map.of(
            "inline_data", Map.of(
                "mime_type", mime,   // e.g., "image/png"
                "data", b64          // Base64(파일)
            )
        );


        Map<String, Object> content = Map.of("parts", new Object[]{textPart, imagePart});

        Map<String, Object> body = Map.of(
            "contents", new Object[]{content}
            // , "generationConfig", genCfg // <- SDK 쓸 때만
        );
        
        log.info("=== 요청 구조 검증 ===");
        log.info("textPart: {}", textPart);
        log.info("imagePart 키들: {}", imagePart.keySet());
        log.info("content 키들: {}", content.keySet());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Gemini API 호출 중...");
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForEntity(URI.create(endpoint), new HttpEntity<>(body, headers), (Class<Map<String, Object>>)(Class<?>)Map.class);
            log.info("Gemini API 응답 상태: {}", resp.getStatusCode());
            
            Map<String, Object> responseBody = resp.getBody();
            if (responseBody == null) {
                log.error("Gemini API에서 빈 응답을 받았습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from Gemini");
            }
            
            log.info("Gemini API 응답 구조: {}", responseBody.keySet());
            
            // 응답 전체를 더 자세히 로깅 (민감한 정보 제외)
            log.info("=== 응답 상세 정보 ===");
            log.info("응답 크기: {} characters", responseBody.toString().length());
            
            byte[] out = extractGeminiImageBytes(restTemplate, responseBody);
            log.info("이미지 추출 완료. 크기: {} bytes", out.length);
            return out;
        } catch (HttpStatusCodeException e) {
            log.error("=== Gemini API HTTP 에러 ===");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            log.error("요청 URL: {}", endpoint.replaceAll("key=[^&]+", "key=***"));
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("=== Gemini API 일반 에러 ===");
            log.error("에러 메시지: {}", e.getMessage());
            log.error("에러 타입: {}", e.getClass().getSimpleName());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        }
    }

    private static byte[] extractGeminiImageBytes(RestTemplate restTemplate, Map<?, ?> responseBody) {
        // 응답 구조 디버깅을 위한 로깅 추가
        log.info("=== Gemini 응답 구조 분석 시작 ===");
        log.info("응답 키들: {}", responseBody.keySet());
        
        // finishReason 확인
        Object candidates = responseBody.get("candidates");
        if (!(candidates instanceof java.util.List) || ((java.util.List<?>) candidates).isEmpty()) {
            log.error("candidates가 없거나 비어있음");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No candidates in response");
        }
        
        Object first = ((java.util.List<?>) candidates).get(0);
        if (!(first instanceof Map)) {
            log.error("candidates[0]이 Map이 아님: {}", first.getClass().getSimpleName());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid candidates[0]");
        }
        
        Map<?, ?> candidate = (Map<?, ?>) first;
        log.info("candidate 키들: {}", candidate.keySet());
        
        // finishReason 확인
        Object finishReason = candidate.get("finishReason");
        if (finishReason != null) {
            log.info("finishReason: {}", finishReason);
            if ("SAFETY".equals(String.valueOf(finishReason))) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Content blocked by safety filters");
            }
        }
        
        Object content = candidate.get("content");
        if (!(content instanceof Map)) {
            log.error("content가 Map이 아님: {}", content != null ? content.getClass().getSimpleName() : "null");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing content");
        }
        
        Map<?, ?> contentMap = (Map<?, ?>) content;
        log.info("content 키들: {}", contentMap.keySet());
        
        Object parts = contentMap.get("parts");
        if (!(parts instanceof java.util.List)) {
            log.error("parts가 List가 아님: {}", parts != null ? parts.getClass().getSimpleName() : "null");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing parts");
        }
        
        java.util.List<?> partsList = (java.util.List<?>) parts;
        log.info("parts 개수: {}", partsList.size());
        
        for (int i = 0; i < partsList.size(); i++) {
            Object p = partsList.get(i);
            if (!(p instanceof Map)) {
                log.warn("parts[{}]이 Map이 아님: {}", i, p != null ? p.getClass().getSimpleName() : "null");
                continue;
            }
            
            Map<?, ?> part = (Map<?, ?>) p;
            log.info("part[{}] 키들: {}", i, part.keySet());

            // inline_data 또는 inlineData 확인
            Object inline = part.containsKey("inline_data") ? part.get("inline_data") : part.get("inlineData");
            if (inline instanceof Map) {
                Map<?, ?> inlineMap = (Map<?, ?>) inline;
                log.info("inline_data 키들: {}", inlineMap.keySet());
                if (inlineMap.containsKey("data")) {
                    String data = String.valueOf(inlineMap.get("data"));
                    log.info("inline_data에서 이미지 데이터 발견. 크기: {} characters", data.length());
                    return java.util.Base64.getDecoder().decode(data);
                }
            }

            // media 확인
            Object media = part.get("media");
            if (media instanceof Map) {
                Map<?, ?> mediaMap = (Map<?, ?>) media;
                log.info("media 키들: {}", mediaMap.keySet());
                if (mediaMap.containsKey("data")) {
                    String data = String.valueOf(mediaMap.get("data"));
                    log.info("media에서 이미지 데이터 발견. 크기: {} characters", data.length());
                    return java.util.Base64.getDecoder().decode(data);
                }
            }

            // file_uri 다운로드 시도
            byte[] fetched = tryDownloadByFileUri(restTemplate, part);
            if (fetched != null && fetched.length > 0) {
                log.info("file_uri에서 이미지 다운로드 성공. 크기: {} bytes", fetched.length);
                return fetched;
            }
        }
        
        log.error("모든 part에서 이미지 데이터를 찾지 못함");
        log.error("전체 응답 구조: {}", responseBody);
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No inline image data found");
    }

    private static byte[] tryDownloadByFileUri(RestTemplate restTemplate, Map<?, ?> part) {
        log.info("file_uri 다운로드 시도 중...");
        
        Object fileData = part.containsKey("file_data") ? part.get("file_data") : part.get("fileData");
        if (fileData instanceof Map) {
            Map<?, ?> fileDataMap = (Map<?, ?>) fileData;
            log.info("fileData 키들: {}", fileDataMap.keySet());
            
            Object uri = fileDataMap.get("file_uri");
            if (uri == null) uri = fileDataMap.get("uri");
            
            if (uri instanceof String) {
                String uriString = (String) uri;
                log.info("파일 URI 발견: {}", uriString);
                try {
                    byte[] result = restTemplate.getForObject(URI.create(uriString), byte[].class);
                    if (result != null && result.length > 0) {
                        log.info("파일 다운로드 성공. 크기: {} bytes", result.length);
                        return result;
                    } else {
                        log.warn("파일 다운로드 결과가 null이거나 비어있음");
                    }
                } catch (Exception e) {
                    log.warn("파일 다운로드 실패: {}", e.getMessage());
                }
            } else {
                log.info("URI가 String이 아님: {}", uri != null ? uri.getClass().getSimpleName() : "null");
            }
        } else {
            log.info("fileData가 Map이 아님: {}", fileData != null ? fileData.getClass().getSimpleName() : "null");
        }
        return null;
    }

    private static String guessMimeType(String url) {
        String lower = url == null ? "" : url.toLowerCase();
        String mimeType;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (lower.endsWith(".png")) {
            mimeType = "image/png";
        } else if (lower.endsWith(".webp")) {
            mimeType = "image/webp";
        } else {
            mimeType = "image/png"; // 기본값
        }
        log.info("파일명 '{}'에서 추정한 MIME 타입: {}", url, mimeType);
        return mimeType;
    }

    @Override
    public PetAvatar generateAvatarFromS3Key(String s3Key, String prompt, Long memberId) {
        // TODO: S3Service를 주입받아서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PetAvatar saveAvatarSelection(Long memberId, String resultKey, String thumbKey, 
                                       String prompt, String model, Boolean setAsPrimary) {
        // TODO: S3Service를 주입받아서 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setPrimaryAvatar(Long memberId, Long petAvatarId) {
        // TODO: 트랜잭션으로 기존 대표 아바타 해제 후 새로 설정
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void unsetPrimaryAvatar(Long memberId, Long petAvatarId) {
        // TODO: 대표 아바타 해제
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PetAvatar getPrimaryAvatarByMemberId(Long memberId) {
        // TODO: 사용자의 대표 아바타 조회
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional
    public PetAvatar attachImageFromStorage(Long avatarId, String s3Key, String cdnUrl, String mime) {
        if (avatarId == null || s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("avatarId and s3Key are required");
        }
        PetAvatar avatar = getPetAvatarById(avatarId);

        // 간단한 검증: 키/URL 존재
        String resolvedMime = (mime == null || mime.isBlank()) ? "image/png" : mime;

        // resultKey와 cdnUrl 저장, 버전 증가
        avatar.updateS3Info(s3Key, avatar.getThumbKey(), cdnUrl, resolvedMime, avatar.getWidth(), avatar.getHeight());
        avatar.updateVersion();

        return petAvatarRepository.save(avatar);
    }

    @Override
    @Transactional
    public PetAvatar createCustomPetAvatarFromStorage(PetType petType, String displayName,
                                                     String s3Key, String cdnUrl, String mime,
                                                     PetAvatarStyle style, Long memberId) {
        if (petType == null || displayName == null || displayName.isBlank() || s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("petType, displayName, s3Key are required");
        }

        String code = "CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PetAvatar avatar = PetAvatar.createCustomWithS3(
                petType,
                code,
                displayName,
                s3Key,
                null,
                cdnUrl,
                (mime == null || mime.isBlank()) ? "image/png" : mime,
                null,
                null,
                style != null ? style : PetAvatarStyle.DEFAULT,
                memberId
        );

        return petAvatarRepository.save(avatar);
    }
}
