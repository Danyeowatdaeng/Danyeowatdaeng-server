package com.tourapi.tourapi.web.controller.petAvatar;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarErrorStatus;
import com.tourapi.tourapi.common.exception.petAvatar.status.PetAvatarSuccessStatus;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarListResponse;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarResponse;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// no-op
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pet-avatars")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PetAvatar")
public class PetAvatarController {

    private final PetAvatarService petAvatarService;

    // PetAvatar 목록 조회 (기본 + 커스텀)
    @GetMapping
    @Operation(summary = "PetAvatar 목록 조회", description = "사용자가 선택 가능한 기본+커스텀 PetAvatar 목록을 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // UNAUTHORIZED
    @ApiErrorCodeExample(value = PetAvatarErrorStatus.class, codes = {"PET4001", "PET4002"}) // NOT_FOUND, INACTIVE (간접 가능성)
    public ResponseEntity<ApiResponse<PetAvatarListResponse>> getAllPetAvatars(@AuthenticationPrincipal UserPrincipal principal) {
        
        
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

    // JwtAuthenticationFilter 가 SecurityContext 에 주입한 UserPrincipal 을 직접 사용합니다.
}
