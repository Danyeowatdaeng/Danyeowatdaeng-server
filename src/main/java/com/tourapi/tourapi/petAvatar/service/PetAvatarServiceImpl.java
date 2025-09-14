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
                                         String imageUrl, String originalImageUrl, 
                                         PetAvatarStyle style, Long memberId) {
        // 고유한 코드 생성
        String code = "CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PetAvatar customPetAvatar = PetAvatar.createCustom(
                petType, code, displayName, imageUrl, originalImageUrl, style, memberId
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
        log.info("MIME 타입: {}", mime);
        log.info("Base64 인코딩 크기: {} characters", b64.length());
        log.info("프롬프트: {}", prompt);
        
        Map<String, Object> inlineData = Map.of(
            "mime_type", mime,
            "data", b64
        );
        Map<String, Object> body = Map.of(
            "contents", new Object[]{
                Map.of(
                    "parts", new Object[]{
                        Map.of("text", prompt),
                        Map.of("inline_data", inlineData)
                    }
                )
            }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("Gemini API 호출 중...");
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForEntity(URI.create(endpoint), new HttpEntity<>(body, headers), (Class<Map<String, Object>>)(Class<?>)Map.class);
            log.info("Gemini API 응답 상태: {}", resp.getStatusCode());
            
            Map<String, Object> responseBody = resp.getBody();
            if (responseBody == null) {
                log.error("Gemini API에서 빈 응답을 받았습니다.");
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from Gemini");
            }
            
            log.info("Gemini API 응답 구조: {}", responseBody.keySet());
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
        Object candidates = responseBody.get("candidates");
        if (!(candidates instanceof java.util.List) || ((java.util.List<?>) candidates).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No candidates in response");
        }
        Object first = ((java.util.List<?>) candidates).get(0);
        if (!(first instanceof Map)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid candidates[0]");
        }
        Object content = ((Map<?, ?>) first).get("content");
        if (!(content instanceof Map)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing content");
        }
        Object parts = ((Map<?, ?>) content).get("parts");
        if (!(parts instanceof java.util.List)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing parts");
        }
        for (Object p : (java.util.List<?>) parts) {
            if (!(p instanceof Map)) continue;
            Map<?, ?> part = (Map<?, ?>) p;

            Object inline = part.containsKey("inline_data") ? part.get("inline_data") : part.get("inlineData");
            if (inline instanceof Map && ((Map<?, ?>) inline).containsKey("data")) {
                String data = String.valueOf(((Map<?, ?>) inline).get("data"));
                return java.util.Base64.getDecoder().decode(data);
            }

            Object media = part.get("media");
            if (media instanceof Map && ((Map<?, ?>) media).containsKey("data")) {
                String data = String.valueOf(((Map<?, ?>) media).get("data"));
                return java.util.Base64.getDecoder().decode(data);
            }

            byte[] fetched = tryDownloadByFileUri(restTemplate, part);
            if (fetched != null && fetched.length > 0) return fetched;
        }
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No inline image data found");
    }

    private static byte[] tryDownloadByFileUri(RestTemplate restTemplate, Map<?, ?> part) {
        Object fileData = part.containsKey("file_data") ? part.get("file_data") : part.get("fileData");
        if (fileData instanceof Map) {
            Object uri = ((Map<?, ?>) fileData).get("file_uri");
            if (uri == null) uri = ((Map<?, ?>) fileData).get("uri");
            if (uri instanceof String) {
                try {
                    return restTemplate.getForObject(URI.create((String) uri), byte[].class);
                } catch (Exception ignored) {}
            }
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
}
