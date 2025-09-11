package com.tourapi.tourapi.web.controller.petAvatar;

import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.SuccessStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarSuccessStatus;
import com.tourapi.tourapi.petAvatar.config.PetAvatarProperties;
import com.tourapi.tourapi.petAvatar.dto.EditRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pet-avatars")
public class PetAvatarMvpController {

    private final PetAvatarProperties properties;
    private static final Logger log = LoggerFactory.getLogger(PetAvatarMvpController.class);

    public PetAvatarMvpController(PetAvatarProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        boolean apiKeyOk = properties.getApiKey() != null && !properties.getApiKey().isBlank();
        Map<String, Object> body = Map.of(
            "provider", properties.getProvider(),
            "status", apiKeyOk ? "UP" : "DEGRADED",
            "checks", new Object[]{Map.of("name", "apiKey", "ok", apiKeyOk)}
        );
        return ApiResponse.onSuccess(SuccessStatus.OK, body);
    }

    @PostMapping(value = "/mvp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generate(@Valid @RequestBody EditRequest req) {
        try {
            if (!properties.isEnabled()) {
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_SERVICE_DISABLED);
            }

            if ("mock".equalsIgnoreCase(properties.getProvider())) {
                byte[] mock = new byte[]{};
                return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, mock);
            }

            RestTemplate restTemplate = new RestTemplate();

            if ("dalle".equalsIgnoreCase(properties.getProvider())) {
                byte[] out = callOpenAiImagesEdits(restTemplate, req, buildUrlForProvider()).getBody();
                return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, out);
            } else if ("gemini".equalsIgnoreCase(properties.getProvider())) {
                byte[] out = callGeminiImageGenerate(restTemplate, req).getBody();
                return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, out);
            }

            return ApiResponse.onFailure(PetAvatarErrorStatus.PROVIDER_UNSUPPORTED);
        } catch (HttpStatusCodeException e) {
            return ApiResponse.onFailure(mapGeminiHttpStatus(e));
        } catch (ResponseStatusException e) {
            org.springframework.http.HttpStatusCode status = e.getStatusCode();
            if (status.value() == HttpStatus.BAD_REQUEST.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_BAD_REQUEST);
            if (status.value() == HttpStatus.FORBIDDEN.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_FORBIDDEN);
            if (status.value() == HttpStatus.UNAUTHORIZED.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_INVALID_API_KEY);
            if (status.value() == HttpStatus.NOT_FOUND.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_MODEL_NOT_FOUND);
            if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_RATE_LIMITED);
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UPSTREAM_ERROR);
        } catch (Exception e) {
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UNKNOWN_ERROR);
        }
    }

    @PostMapping(value = "/mvp-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateByUpload(@RequestPart("file") MultipartFile file,
                                                   @RequestPart("prompt") String prompt) {
        try {
            if (!properties.isEnabled()) {
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_SERVICE_DISABLED);
            }

            if ("mock".equalsIgnoreCase(properties.getProvider())) {
                return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, new byte[]{});
            }

            byte[] imageBytes;
            try {
                imageBytes = file.getBytes();
            } catch (Exception e) {
                return ApiResponse.onFailure(PetAvatarErrorStatus.PET_AVATAR_INVALID_IMAGE_FORMAT);
            }

            if ("gemini".equalsIgnoreCase(properties.getProvider())) {
                byte[] out = callGeminiImageGenerateFromBytes(new RestTemplate(), imageBytes, file.getOriginalFilename(), prompt).getBody();
                return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, out);
            } else if ("dalle".equalsIgnoreCase(properties.getProvider())) {
                return ApiResponse.onFailure(PetAvatarErrorStatus.PROVIDER_UNSUPPORTED);
            }

            return ApiResponse.onFailure(PetAvatarErrorStatus.PROVIDER_UNSUPPORTED);
        } catch (HttpStatusCodeException e) {
            return ApiResponse.onFailure(mapGeminiHttpStatus(e));
        } catch (ResponseStatusException e) {
            org.springframework.http.HttpStatusCode status = e.getStatusCode();
            if (status.value() == HttpStatus.BAD_REQUEST.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_BAD_REQUEST);
            if (status.value() == HttpStatus.FORBIDDEN.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_FORBIDDEN);
            if (status.value() == HttpStatus.UNAUTHORIZED.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_INVALID_API_KEY);
            if (status.value() == HttpStatus.NOT_FOUND.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_MODEL_NOT_FOUND);
            if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_RATE_LIMITED);
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UPSTREAM_ERROR);
        } catch (Exception e) {
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UNKNOWN_ERROR);
        }
    }

    private String buildUrlForProvider() {
        String base = properties.getProviderBaseUrl();
        if (!base.endsWith("/")) {
            base += "/";
        }
        if ("dalle".equalsIgnoreCase(properties.getProvider())) {
            return base + "images/edits"; // OpenAI
        } else if ("gemini".equalsIgnoreCase(properties.getProvider())) {
            return base + ":generateContent"; // 실제 호출에서 모델 경로를 완성해 사용
        }
        return base + "nano-banana";
    }

    private static PetAvatarErrorStatus mapGeminiHttpStatus(HttpStatusCodeException e) {
        org.springframework.http.HttpStatusCode status = e.getStatusCode();
        if (status.value() == HttpStatus.UNAUTHORIZED.value()) return PetAvatarErrorStatus.GEMINI_INVALID_API_KEY;
        if (status.value() == HttpStatus.FORBIDDEN.value()) return PetAvatarErrorStatus.GEMINI_FORBIDDEN;
        if (status.value() == HttpStatus.NOT_FOUND.value()) return PetAvatarErrorStatus.GEMINI_MODEL_NOT_FOUND;
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) return PetAvatarErrorStatus.GEMINI_RATE_LIMITED;
        if (status.is4xxClientError()) return PetAvatarErrorStatus.GEMINI_BAD_REQUEST;
        if (status.is5xxServerError()) return PetAvatarErrorStatus.GEMINI_UPSTREAM_ERROR;
        return PetAvatarErrorStatus.GEMINI_UNKNOWN_ERROR;
    }

    private ResponseEntity<byte[]> callGeminiImageGenerate(RestTemplate restTemplate, EditRequest req) {
        // 1) 이미지 다운로드 후 base64 인코딩
        byte[] imageBytes;
        try {
            imageBytes = restTemplate.getForObject(URI.create(req.getImageUrl()), byte[].class);
            if (imageBytes == null || imageBytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to download image");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid imageUrl or fetch failed: " + e.getMessage(), e);
        }
        String b64 = java.util.Base64.getEncoder().encodeToString(imageBytes);

        // 2) 공식 엔드포인트 모델: gemini-2.5-flash-image-preview:generateContent
        String modelPath = "models/gemini-2.5-flash-image-preview:generateContent";
        String endpoint = properties.getProviderBaseUrl() + "/" + modelPath + "?key=" + properties.getApiKey();

        // 3) 요청 본문 구성 (snake_case inline_data 호환)
        Map<String, Object> inlineData = Map.of(
            "mime_type", guessMimeType(req.getImageUrl()),
            "data", b64
        );
        Map<String, Object> body = Map.of(
            "contents", new Object[]{
                Map.of(
                    "parts", new Object[]{
                        Map.of("text", req.getPrompt()),
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

            if (log.isDebugEnabled()) {
                try { log.debug("Gemini raw response: {}", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody)); } catch (Exception ignore) {}
            }

            // 4) 응답 파싱: candidates[].content.parts[] 내 image base64 또는 file_uri
            byte[] out = extractGeminiImageBytes(restTemplate, responseBody);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(out);
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> callGeminiImageGenerateFromBytes(RestTemplate restTemplate, byte[] imageBytes, String filename, String prompt) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty image data");
        }
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
            if (log.isDebugEnabled()) {
                try { log.debug("Gemini raw response(upload): {}", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody)); } catch (Exception ignore) {}
            }
            byte[] out = extractGeminiImageBytes(restTemplate, responseBody);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(out);
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        }
    }

    private static byte[] tryDownloadByFileUri(RestTemplate restTemplate, Map<?, ?> part) {
        Object fileData = part.containsKey("file_data") ? part.get("file_data") : part.get("fileData");
        if (fileData instanceof Map) {
            Object uri = ((Map<?, ?>) fileData).get("file_uri");
            if (uri == null) uri = ((Map<?, ?>) fileData).get("uri");
            if (uri instanceof String) {
                try {
                    return restTemplate.getForObject(URI.create((String) uri), byte[].class);
                } catch (Exception e) {
                    log.warn("Failed to fetch file_uri: {} - {}", uri, e.getMessage());
                }
            }
        }
        return null;
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

            // 1) inline_data / inlineData
            Object inline = part.containsKey("inline_data") ? part.get("inline_data") : part.get("inlineData");
            if (inline instanceof Map && ((Map<?, ?>) inline).containsKey("data")) {
                String data = String.valueOf(((Map<?, ?>) inline).get("data"));
                return java.util.Base64.getDecoder().decode(data);
            }

            // 2) media { mimeType, data }
            Object media = part.get("media");
            if (media instanceof Map && ((Map<?, ?>) media).containsKey("data")) {
                String data = String.valueOf(((Map<?, ?>) media).get("data"));
                return java.util.Base64.getDecoder().decode(data);
            }

            // 3) file_data / fileData (file_uri 제공형 — URL을 추가로 받아와야 함)
            byte[] fetched = tryDownloadByFileUri(restTemplate, part);
            if (fetched != null && fetched.length > 0) return fetched;

            // 4) 혹시 data만 직접 포함하는 경우 (드물지만 안전망)
            if (part.containsKey("data")) {
                String data = String.valueOf(part.get("data"));
                try {
                    return java.util.Base64.getDecoder().decode(data);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        // 진단용: parts 중 text 일부를 포함해 에러 메시지 제공
        String diagnosticText = null;
        for (Object p : (java.util.List<?>) parts) {
            if (p instanceof Map && ((Map<?, ?>) p).containsKey("text")) {
                diagnosticText = String.valueOf(((Map<?, ?>) p).get("text"));
                if (diagnosticText != null && diagnosticText.length() > 200) {
                    diagnosticText = diagnosticText.substring(0, 200) + "...";
                }
                break;
            }
        }
        String message = "No inline image data found" + (diagnosticText != null ? ": " + diagnosticText : "");
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    private static String guessMimeType(String url) {
        String lower = url == null ? "" : url.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/png";
    }

    private ResponseEntity<byte[]> callOpenAiImagesEdits(RestTemplate restTemplate, EditRequest req, String url) {
        // 1) 원본 이미지 다운로드
        byte[] imageBytes;
        try {
            imageBytes = restTemplate.getForObject(URI.create(req.getImageUrl()), byte[].class);
            if (imageBytes == null || imageBytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to download image");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid imageUrl or fetch failed: " + e.getMessage(), e);
        }

        // 2) 멀티파트 구성 (OpenAI images/edits 요구사항)
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + (properties.getApiKey() == null ? "" : properties.getApiKey()));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> multipart = new LinkedMultiValueMap<>();
        multipart.add("model", "gpt-image-1");
        multipart.add("prompt", req.getPrompt());
        multipart.add("response_format", "b64_json");
        multipart.add("size", "1024x1024");

        ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() { return "input.png"; }
        };
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders.setContentType(MediaType.IMAGE_PNG);
        HttpEntity<ByteArrayResource> imagePart = new HttpEntity<>(imageResource, imageHeaders);
        multipart.add("image", imagePart);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(multipart, headers);

        // 3) 호출 및 b64_json 디코드
        Map<?, ?> openAiResp;
        try {
            openAiResp = restTemplate.postForEntity(URI.create(url), entity, Map.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        }
        if (openAiResp == null || !openAiResp.containsKey("data")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid response from OpenAI");
        }

        Object data = openAiResp.get("data");
        if (!(data instanceof java.util.List) || ((java.util.List<?>) data).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No image returned");
        }
        Object first = ((java.util.List<?>) data).get(0);
        if (!(first instanceof Map) || !((Map<?, ?>) first).containsKey("b64_json")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Missing b64_json field");
        }
        String b64 = (String) ((Map<?, ?>) first).get("b64_json");
        byte[] bytes = java.util.Base64.getDecoder().decode(b64);

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(bytes);
    }
}



