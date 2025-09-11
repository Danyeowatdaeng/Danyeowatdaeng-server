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
        if (!properties.isEnabled()) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.GEMINI_SERVICE_DISABLED);
        }
        if ("mock".equalsIgnoreCase(properties.getProvider())) {
            return new byte[]{};
        }
        if (imageBytes == null || imageBytes.length == 0) {
            throw new PetAvatarHandler(PetAvatarErrorStatus.PET_AVATAR_INVALID_IMAGE_FORMAT);
        }
        if ("gemini".equalsIgnoreCase(properties.getProvider())) {
            return callGeminiImageGenerateFromBytes(new RestTemplate(), imageBytes, filename, prompt);
        }
        throw new PetAvatarHandler(PetAvatarErrorStatus.PROVIDER_UNSUPPORTED);
    }

    private byte[] callGeminiImageGenerateFromBytes(RestTemplate restTemplate, byte[] imageBytes, String filename, String prompt) {
        String b64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        String modelPath = "models/gemini-2.5-flash-image-preview:generateContent";
        String endpoint = properties.getProviderBaseUrl() + "/" + modelPath + "?key=" + properties.getApiKey();

        String mime = guessMimeType(filename == null ? null : filename);
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
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForEntity(URI.create(endpoint), new HttpEntity<>(body, headers), (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> responseBody = resp.getBody();
            if (responseBody == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty response from Gemini");
            }
            byte[] out = extractGeminiImageBytes(restTemplate, responseBody);
            return out;
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
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
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/png";
    }
}
