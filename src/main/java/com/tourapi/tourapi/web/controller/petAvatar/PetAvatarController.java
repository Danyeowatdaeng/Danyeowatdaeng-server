package com.tourapi.tourapi.web.controller.petAvatar;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarSuccessStatus;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarListResponse;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarResponse;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarUploadResponse;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;
import com.tourapi.tourapi.petAvatar.service.S3Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pet-avatars")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PetAvatar")
public class PetAvatarController {

    private final PetAvatarService petAvatarService;
    private final S3Service s3Service;

    public static final String FIXED_PROMPT = """
역할: 너는 ‘사진을 픽셀아트로 재해석’하는 일러스트레이터다.  목표: 첨부한 동물 사진(강아지/고양이 등)을 참고해, 원본의 개체 정체성(털색/무늬/표정/귀 모양 등)을 보존하면서 픽셀아트 스타일로 다시 그려라.  출력 규격: - 해상도: {512|768|1024} × {512|768|1024} (정사각 권장) - 팔레트: {12|16|24}색 제한, 큰 면적에만 약한 디더링, 그라디언트 최소화 - 라인: 1픽셀 외곽선(검정/짙은 갈색), 과도한 윤광·블러 금지 - 명암: 2~3단 셀셰이딩 - 배경: {투명 | 파스텔 단색 | 단순화된 아이콘 2~3개}, 텍스트/워터마크 금지 - 포맷: PNG {투명 배경: 사용/미사용}  리라이팅 지침: - 동물종과 특징 보존: 종(개/고양이), 대표 무늬(예: 이마의 흰 무늬, 등줄무늬, 코 색), 귀/코/눈 형태를 유지 - 구도: 얼굴·귀·코가 잘리지 않게 중앙 배치, 원본 포즈 최대한 유지 - 단순화: 사진 속 복잡한 바닥·담요 무늬는 2~3개 색 블록으로 축약 - 스타일 강도: {낮음|중간|높음} (원본 유사성 우선 → 스타일은 보조)  금지(네거티브): - 사진풍 질감·유화/만화 잉크 효과·과도한 그라디언트·텍스트/로고/워터마크 - 과장된 데포르메로 원본 개체 식별 불가 상태 - base64 인코딩 문자열 절대 금지.   산출물: 위 조건을 만족하는 최종 1장(필요 시 2~3안 제안 가능).
""";

    // PetAvatar 목록 조회 (기본 + 커스텀)
    @GetMapping
    @Operation(summary = "PetAvatar 목록 조회", description = "사용자가 선택 가능한 기본+커스텀 PetAvatar 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001", "PET4002"}) // NOT_FOUND, INACTIVE (간접 가능성)
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getAllPetAvatars(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }
        
        Long memberId = principal.getId();

        List<PetAvatarResponse> petAvatars = petAvatarService.getAvailablePetAvatarsForMember(memberId)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());

        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);

        log.info("PetAvatar list retrieved for member {}: {} items", memberId, petAvatars.size());
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_LIST_FOUND, response);
    }

    // 기본 PetAvatar만 조회
    @GetMapping("/default")
    @Operation(summary = "기본 PetAvatar 목록 조회", description = "커스텀을 제외한 기본 PetAvatar 목록을 조회합니다.")
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001"}) // 조회 중 개별 Not Found 가능성
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getDefaultPetAvatars() {
        List<PetAvatarResponse> petAvatars = petAvatarService.getDefaultPetAvatars()
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());

        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);

        log.info("Default PetAvatar list retrieved: {} items", petAvatars.size());
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_LIST_FOUND, response);
    }

    // 특정 타입 PetAvatar 조회
    @GetMapping("/{petType}")
    @Operation(summary = "타입별 PetAvatar 목록 조회", description = "특정 PetType에 해당하는 PetAvatar 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001"})
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getPetAvatarsByType(
            @PathVariable PetType petType,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }
        Long memberId = principal.getId();

        List<PetAvatarResponse> petAvatars = petAvatarService.getAvailablePetAvatarsForMemberByType(memberId, petType)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());

        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);

        log.info("PetAvatar list retrieved for type {} and member {}: {} items", petType, memberId, petAvatars.size());
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_LIST_FOUND, response);
    }

    // 사용자 커스텀 PetAvatar 조회
    @GetMapping("/custom")
    @Operation(summary = "커스텀 PetAvatar 목록 조회", description = "사용자가 생성한 커스텀 PetAvatar 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getCustomPetAvatars(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ApiResponse.onFailure(ErrorStatus.UNAUTHORIZED, null);
        }
        
        Long memberId = principal.getId();

        List<PetAvatarResponse> petAvatars = petAvatarService.getCustomPetAvatarsByMemberId(memberId)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());

        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);

        log.info("Custom PetAvatar list retrieved for member {}: {} items", memberId, petAvatars.size());
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_LIST_FOUND, response);
    }

    // 스타일별 PetAvatar 조회
    @GetMapping("/style/{style}")
    @Operation(summary = "스타일별 PetAvatar 목록 조회", description = "지정한 스타일의 PetAvatar 목록을 조회합니다.")
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001"})
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getPetAvatarsByStyle(@PathVariable PetAvatarStyle style) {
        List<PetAvatarResponse> petAvatars = petAvatarService.getPetAvatarsByStyle(style)
                .stream()
                .map(PetAvatarResponse::from)
                // 공개 엔드포인트: 커스텀 아바타 제외
                .filter(r -> !Boolean.TRUE.equals(r.getIsCustom()))
                .collect(Collectors.toList());

        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);

        log.info("PetAvatar list retrieved for style {}: {} items", style, petAvatars.size());
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_LIST_FOUND, response);
    }

    // ID로 PetAvatar 조회
    @GetMapping("/id/{id}")
    @Operation(summary = "PetAvatar 상세 조회 (ID)", description = "ID로 PetAvatar 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001"})
    public ResponseEntity<ApiResponse<PetAvatarResponse>> getPetAvatarById(@PathVariable Long id) {
        PetAvatarResponse response = PetAvatarResponse.from(petAvatarService.getPetAvatarById(id));

        log.info("PetAvatar retrieved by id: {}", id);
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_DETAIL_FOUND, response);
    }

    // 코드로 PetAvatar 조회
    @GetMapping("/code/{code}")
    @Operation(summary = "PetAvatar 상세 조회 (CODE)", description = "코드로 PetAvatar 상세 정보를 조회합니다.")
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001"})
    public ResponseEntity<ApiResponse<PetAvatarResponse>> getPetAvatarByCode(@PathVariable String code) {
        PetAvatarResponse response = PetAvatarResponse.from(petAvatarService.getPetAvatarByCode(code));

        log.info("PetAvatar retrieved by code: {}", code);
        return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_DETAIL_FOUND, response);
    }

    // === MVP Upload (moved from PetAvatarMvpController) ===
    @PostMapping(value = "/transform-mypet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "펫 이미지를 픽셀아트로 변환", 
               description = "업로드된 펫 이미지를 Gemini AI를 사용하여 픽셀아트 스타일로 변환합니다. " +
                           "지원 형식: PNG, JPEG, WebP. 최대 크기: 10MB. " +
                           "변환된 이미지는 PNG 형식으로 직접 반환됩니다.")
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {
        "PET4050", "PET4014", "PET4056", "PET4052", "PET4051", 
        "PET4053", "PET4054", "PET4059", "PET4060", "PET4061"
    })
    public ResponseEntity<?> generateByUpload(@RequestPart("file") MultipartFile file) {
        log.info("=== PetAvatar 업로드 요청 시작 ===");
        log.info("파일명: {}", file.getOriginalFilename());
        log.info("파일 크기: {} bytes", file.getSize());
        log.info("Content Type: {}", file.getContentType());
        
        try {
            byte[] imageBytes = file.getBytes();
            String effectivePrompt = FIXED_PROMPT;
            log.info("실제 이미지 바이트 크기: {} bytes", imageBytes.length);
            log.info("사용할 프롬프트: {}", effectivePrompt);
            
            byte[] out = petAvatarService.generateAvatarFromUpload(imageBytes, file.getOriginalFilename(), effectivePrompt);
            log.info("PetAvatar 생성 성공. 결과 크기: {} bytes", out.length);
            
            // 이미지 파일로 직접 반환 (JSON 변환 없이)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(out.length);
            headers.set("Content-Disposition", "inline; filename=\"pet-avatar.png\"");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out);
        } catch (ResponseStatusException e) {
            log.error("=== ResponseStatusException 발생 ===");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("에러 메시지: {}", e.getReason());
            log.error("응답 본문: {}", e.getReason());
            
            org.springframework.http.HttpStatusCode status = e.getStatusCode();
            if (status.value() == HttpStatus.BAD_REQUEST.value()) {
                log.error("BAD_REQUEST 에러 - Gemini API 요청 형식 오류");
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_BAD_REQUEST);
            }
            if (status.value() == HttpStatus.FORBIDDEN.value()) {
                log.error("FORBIDDEN 에러 - Gemini API 접근 거부");
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_FORBIDDEN);
            }
            if (status.value() == HttpStatus.UNAUTHORIZED.value()) {
                log.error("UNAUTHORIZED 에러 - API 키 문제");
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_INVALID_API_KEY);
            }
            if (status.value() == HttpStatus.NOT_FOUND.value()) {
                log.error("NOT_FOUND 에러 - 모델을 찾을 수 없음");
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_MODEL_NOT_FOUND);
            }
            if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                log.error("TOO_MANY_REQUESTS 에러 - 요청 한도 초과");
                return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_RATE_LIMITED);
            }
            log.error("기타 HTTP 에러: {}", status);
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UPSTREAM_ERROR);
        } catch (Exception e) {
            log.error("=== 일반 Exception 발생 ===");
            log.error("에러 타입: {}", e.getClass().getSimpleName());
            log.error("에러 메시지: {}", e.getMessage());
            e.printStackTrace();
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UNKNOWN_ERROR);
        }
    }

    // 개선된 방식: Multipart로 직접 S3 업로드
    @PostMapping(value = "/upload-to-s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지를 S3에 업로드", 
               description = "업로드된 이미지 파일을 AWS S3에 저장하고 접근 가능한 URL을 반환합니다. " +
                           "지원 형식: PNG, JPEG, WebP, GIF. 최대 크기: 10MB. " +
                           "업로드된 파일은 고유한 키로 저장되며 CDN URL이 제공됩니다.")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"INTERNAL_SERVER_ERROR"})
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4060"})
    public ResponseEntity<?> uploadGeneratedResultToS3(@RequestPart("file") MultipartFile file) {
        log.info("=== S3 업로드 요청 시작 ===");
        log.info("파일명: {}", file.getOriginalFilename());
        log.info("파일 크기: {} bytes", file.getSize());
        log.info("Content Type: {}", file.getContentType());
        
        try {
            byte[] imageBytes = file.getBytes();
            String filename = file.getOriginalFilename();
            String mimeType = file.getContentType();
            
            // S3에 직접 업로드
            String key = generateS3Key(filename);
            S3Service.UploadResult saved = s3Service.uploadBytes(imageBytes, key, mimeType);
            
            PetAvatarUploadResponse dto = PetAvatarUploadResponse.builder()
                    .key(saved.key())
                    .url(saved.url())
                    .build();
            
            log.info("S3 업로드 성공. Key: {}, URL: {}", saved.key(), saved.url());
            return ApiResponse.onSuccess(PetAvatarSuccessStatus.PET_AVATAR_CONVERTED, dto);
        } catch (Exception e) {
            log.error("S3 업로드 실패", e);
            return ApiResponse.onFailure(PetAvatarErrorStatus.GEMINI_UNKNOWN_ERROR);
        }
    }
    
    private String generateS3Key(String filename) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd/HHmmss"));
        String extension = filename != null && filename.contains(".") 
            ? filename.substring(filename.lastIndexOf(".")) 
            : ".png";
        return String.format("pet-avatars/%s_%s%s", timestamp, java.util.UUID.randomUUID().toString().substring(0, 8), extension);
    }
}
