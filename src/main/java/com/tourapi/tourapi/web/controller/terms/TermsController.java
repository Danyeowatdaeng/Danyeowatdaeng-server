package com.tourapi.tourapi.web.controller.terms;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.ApiErrorCodeExample;
import com.tourapi.tourapi.common.exception.terms.status.TermsErrorStatus;
import com.tourapi.tourapi.common.exception.terms.status.TermsSuccessStatus;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.service.MemberService;
import com.tourapi.tourapi.terms.TermsCode;
import com.tourapi.tourapi.terms.TermsDocument;
import com.tourapi.tourapi.terms.dto.TermsAgreementRequest;
import com.tourapi.tourapi.terms.service.TermsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Tag(name = "Terms")
public class TermsController {
    
    private final TermsService termsService;
    private final MemberService memberService;
    
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
    
    @PostMapping("/agree-terms")
    @Operation(summary = "02. 약관 동의", description = "필수 약관에 동의합니다.", tags = {"회원가입 플로우"})
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND", "TERMS_ALREADY_AGREED", "TERMS_VERSION_MISMATCH", "TERMS_AGREEMENT_REQUIRED"}
    )
    public ResponseEntity<ApiResponse<Void>> agreeTerms(@RequestBody TermsAgreementRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        // 인증된 사용자의 Member 정보 조회
        Member member = memberService.getAuthenticatedMember(principal.getId());
            
        termsService.agreeTerms(member, request.getTermsCodes());
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_AGREED);
    }
    
    @GetMapping("/agreement-status")
    @Operation(summary = "사용자의 약관 동의 상태 조회", description = "현재 로그인한 사용자의 모든 약관 동의 상태를 조회합니다.")
    @ApiErrorCodeExample(
            value = TermsErrorStatus.class,
            codes = {"TERMS_NOT_FOUND"}
    )
    public ResponseEntity<ApiResponse<Map<TermsCode, Boolean>>> getAgreementStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        Map<TermsCode, Boolean> status = termsService.getMemberTermsAgreementStatus(principal.getId());
        return ApiResponse.onSuccess(TermsSuccessStatus.AGREEMENT_STATUS_FOUND, status);
    }
}
