package com.tourapi.tourapi.web.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.general.status.ErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.service.MemberService;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarSelectionRequest;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Members")
public class MemberController {

    private final MemberService memberService;
    private final PetAvatarService petAvatarService;

    // PetAvatar 선택
    @PostMapping("/pet-avatar")
    @Operation(summary = "03. PetAvatar 선택", description = "회원가입 과정에서 사용자의 PetAvatar를 선택합니다.", tags = {"회원가입 플로우"})
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"}) // 인증 필요
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"}) // MEMBER_NOT_FOUND
    public ResponseEntity<ApiResponse<Void>> selectPetAvatar(
            @Valid @RequestBody PetAvatarSelectionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long memberId = principal.getId();

        petAvatarService.selectPetAvatarForMember(memberId, request.getPetAvatarId());

        log.info("PetAvatar {} selected for member {}", request.getPetAvatarId(), memberId);
        return ApiResponse.onSuccess(MemberSuccessStatus.PET_SELECT_SUCCESS);
    }

    // 회원가입 완료
    @PostMapping("/complete-signup")
    @Operation(summary = "04. 회원가입 완료", description = "PetAvatar 선택 완료 후 회원가입을 완료합니다.", tags = {"회원가입 플로우"})
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4050"}) // PET_AVATAR_NOT_SELECTED
    public ResponseEntity<ApiResponse<Void>> completeSignup(@AuthenticationPrincipal UserPrincipal principal) {
        Long memberId = principal.getId();

        memberService.completeSignup(memberId);

        log.info("Signup completed for member {}", memberId);
        return ApiResponse.onSuccess(MemberSuccessStatus.SIGN_UP_SUCCESS);
    }

    // 회원 정보 조회
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원 정보를 조회합니다.")
    @SecurityRequirement(name = "accessToken")
    @ApiErrorCodeExample(value = ErrorStatus.class, codes = {"COMMON4001"})
    @ApiErrorCodeExample(value = MemberErrorStatus.class, codes = {"MEMBER4001"})
    public ResponseEntity<ApiResponse<Member>> getMemberInfo(@AuthenticationPrincipal UserPrincipal principal) {
        Long memberId = principal.getId();

        Member member = memberService.getMemberById(memberId);

        log.info("Member info retrieved for member {}", memberId);
        return ApiResponse.onSuccess(MemberSuccessStatus.MEMBER_INFO_FETCHED, member);
    }


}
