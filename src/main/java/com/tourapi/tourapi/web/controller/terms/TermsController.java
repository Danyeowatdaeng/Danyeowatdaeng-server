package com.tourapi.tourapi.web.controller.terms;

import com.tourapi.tourapi.auth.enums.OauthProvider;
import com.tourapi.tourapi.auth.jwt.JwtProvider;
import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.terms.status.TermsErrorStatus;
import com.tourapi.tourapi.common.exception.terms.status.TermsSuccessStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.enums.Role;
import com.tourapi.tourapi.member.repository.MemberRepository;
import com.tourapi.tourapi.terms.TermsCode;
import com.tourapi.tourapi.terms.TermsDocument;
import com.tourapi.tourapi.terms.dto.TermsAgreementRequest;
import com.tourapi.tourapi.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsController {
    
    private final TermsService termsService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    
    @GetMapping("/current")
    @Operation(summary = "현재 유효한 약관 목록 조회", description = "현재 유효한 모든 필수 약관 목록을 조회합니다.")
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND"}
    )
    public ResponseEntity<ApiResponse<List<TermsDocument>>> getCurrentTerms() {
        List<TermsDocument> terms = termsService.getCurrentRequiredTerms();
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_FOUND, terms);
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "특정 약관의 현재 버전 조회", description = "특정 약관의 현재 유효한 버전을 조회합니다.")
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND"}
    )
    public ResponseEntity<ApiResponse<TermsDocument>> getTermsByCode(@PathVariable TermsCode code) {
        TermsDocument terms = termsService.getCurrentTerms(code);
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_FOUND, terms);
    }
    
    @PostMapping("/complete-signup")
    @Operation(summary = "회원가입 완료", description = "필수 약관 동의 후 회원가입을 완료합니다. 현재는 약관 없어서 termsCode 안에 빈걸로 주면 됩니다")
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND", "TERMS_ALREADY_AGREED", "TERMS_VERSION_MISMATCH", "TERMS_AGREEMENT_REQUIRED"}
    )
    public ResponseEntity<ApiResponse<Void>> completeSignUp(@RequestBody TermsAgreementRequest request, @RequestHeader("Authorization") String authorizationHeader) {
        
        // Authorization 헤더에서 액세스 토큰 추출
        String accessToken = authorizationHeader.replace("Bearer ", "");
        
        // 토큰에서 사용자 정보 파싱
        Long memberId = jwtProvider.getId(accessToken);
        String email = jwtProvider.getEmail(accessToken);
        String name = jwtProvider.getMemberName(accessToken);
        Role role = Role.valueOf(jwtProvider.getRole(accessToken));
        
        // email로 기존 Member를 찾거나 새로 생성
        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> {
                // 새로운 Member 생성
                Member newMember = Member.builder()
                    .email(email)
                    .nickname(name)
                    .provider(OauthProvider.GOOGLE) // 기본값, 실제로는 소셜 로그인 정보 필요
                    .providerUserId(email + "_" + System.currentTimeMillis()) // 고유한 값 생성
                    .isSignUpCompleted(false)
                    .role(role)
                    .build();
                return memberRepository.save(newMember);
            });
            
        termsService.completeSignUp(member, request.getTermsCodes());
        return ApiResponse.onSuccess(TermsSuccessStatus.SIGNUP_COMPLETED);
    }
    
    @GetMapping("/agreement-status")
    @Operation(summary = "사용자의 약관 동의 상태 조회", description = "현재 로그인한 사용자의 모든 약관 동의 상태를 조회합니다.")
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND"}
    )
    public ResponseEntity<ApiResponse<Map<TermsCode, Boolean>>> getAgreementStatus() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
            
        Map<TermsCode, Boolean> status = termsService.getMemberTermsAgreementStatus(userPrincipal.getId());
        return ApiResponse.onSuccess(TermsSuccessStatus.AGREEMENT_STATUS_FOUND, status);
    }
}
