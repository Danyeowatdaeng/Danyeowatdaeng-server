package com.tourapi.tourapi.web.controller.petAvatar;

import com.tourapi.tourapi.auth.jwt.JwtProvider;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarListResponse;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarResponse;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pet-avatars")
@RequiredArgsConstructor
@Slf4j
public class PetAvatarController {

    private final PetAvatarService petAvatarService;
    private final JwtProvider jwtProvider;

    // PetAvatar 목록 조회 (기본 + 커스텀)
    @GetMapping
    public ResponseEntity<PetAvatarListResponse> getAllPetAvatars(HttpServletRequest request) {
        Long memberId = getMemberIdFromRequest(request);
        
        List<PetAvatarResponse> petAvatars = petAvatarService.getAvailablePetAvatarsForMember(memberId)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());
        
        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);
        
        log.info("PetAvatar list retrieved for member {}: {} items", memberId, petAvatars.size());
        return ResponseEntity.ok(response);
    }

    // 기본 PetAvatar만 조회
    @GetMapping("/default")
    public ResponseEntity<PetAvatarListResponse> getDefaultPetAvatars() {
        List<PetAvatarResponse> petAvatars = petAvatarService.getDefaultPetAvatars()
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());
        
        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);
        
        log.info("Default PetAvatar list retrieved: {} items", petAvatars.size());
        return ResponseEntity.ok(response);
    }

    // 특정 타입 PetAvatar 조회
    @GetMapping("/{petType}")
    public ResponseEntity<PetAvatarListResponse> getPetAvatarsByType(
            @PathVariable PetType petType,
            HttpServletRequest request) {
        Long memberId = getMemberIdFromRequest(request);
        
        List<PetAvatarResponse> petAvatars = petAvatarService.getAvailablePetAvatarsForMemberByType(memberId, petType)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());
        
        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);
        
        log.info("PetAvatar list retrieved for type {} and member {}: {} items", petType, memberId, petAvatars.size());
        return ResponseEntity.ok(response);
    }

    // 사용자 커스텀 PetAvatar 조회
    @GetMapping("/custom")
    public ResponseEntity<PetAvatarListResponse> getCustomPetAvatars(HttpServletRequest request) {
        Long memberId = getMemberIdFromRequest(request);
        
        List<PetAvatarResponse> petAvatars = petAvatarService.getCustomPetAvatarsByMemberId(memberId)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());
        
        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);
        
        log.info("Custom PetAvatar list retrieved for member {}: {} items", memberId, petAvatars.size());
        return ResponseEntity.ok(response);
    }

    // 스타일별 PetAvatar 조회
    @GetMapping("/style/{style}")
    public ResponseEntity<PetAvatarListResponse> getPetAvatarsByStyle(@PathVariable PetAvatarStyle style) {
        List<PetAvatarResponse> petAvatars = petAvatarService.getPetAvatarsByStyle(style)
                .stream()
                .map(PetAvatarResponse::from)
                .collect(Collectors.toList());
        
        PetAvatarListResponse response = PetAvatarListResponse.from(petAvatars);
        
        log.info("PetAvatar list retrieved for style {}: {} items", style, petAvatars.size());
        return ResponseEntity.ok(response);
    }

    // ID로 PetAvatar 조회
    @GetMapping("/id/{id}")
    public ResponseEntity<PetAvatarResponse> getPetAvatarById(@PathVariable Long id) {
        PetAvatarResponse response = PetAvatarResponse.from(petAvatarService.getPetAvatarById(id));
        
        log.info("PetAvatar retrieved by id: {}", id);
        return ResponseEntity.ok(response);
    }

    // 코드로 PetAvatar 조회
    @GetMapping("/code/{code}")
    public ResponseEntity<PetAvatarResponse> getPetAvatarByCode(@PathVariable String code) {
        PetAvatarResponse response = PetAvatarResponse.from(petAvatarService.getPetAvatarByCode(code));
        
        log.info("PetAvatar retrieved by code: {}", code);
        return ResponseEntity.ok(response);
    }

    // AI 확장용: 이미지 업로드 (향후 구현)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage() {
        // TODO: AI 확장 시 구현
        return ResponseEntity.ok("Upload functionality will be implemented in Phase 5");
    }

    // AI 확장용: AI 변환 요청 (향후 구현)
    @PostMapping("/convert")
    public ResponseEntity<String> convertImage() {
        // TODO: AI 확장 시 구현
        return ResponseEntity.ok("Convert functionality will be implemented in Phase 5");
    }

    // AI 확장용: 변환 상태 조회 (향후 구현)
    @GetMapping("/convert/{requestId}")
    public ResponseEntity<String> getConvertStatus(@PathVariable String requestId) {
        // TODO: AI 확장 시 구현
        return ResponseEntity.ok("Convert status functionality will be implemented in Phase 5");
    }

    private Long getMemberIdFromRequest(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        return jwtProvider.getMemberId(token);
    }
}
