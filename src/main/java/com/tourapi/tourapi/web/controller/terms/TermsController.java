package com.tourapi.tourapi.web.controller.terms;

import com.tourapi.tourapi.auth.jwt.UserPrincipal;
import com.tourapi.tourapi.common.exception.ApiResponse;
import com.tourapi.tourapi.common.exception.terms.status.TermsErrorStatus;
import com.tourapi.tourapi.common.exception.terms.status.TermsSuccessStatus;
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
    
    @GetMapping("/current")
    @Operation(summary = "현재 유효한 약관 목록 조회", description = "현재 유효한 모든 필수 약관 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TermsDocument>>> getCurrentTerms() {
        List<TermsDocument> terms = termsService.getCurrentRequiredTerms();
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_FOUND, terms);
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "특정 약관의 현재 버전 조회", description = "특정 약관의 현재 유효한 버전을 조회합니다.")
    public ResponseEntity<ApiResponse<TermsDocument>> getTermsByCode(@PathVariable TermsCode code) {
        TermsDocument terms = termsService.getCurrentTerms(code);
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_FOUND, terms);
    }
    
    @PostMapping("/{code}/agree")
    @Operation(summary = "단일 약관 동의", description = "특정 약관에 동의합니다.")
    public ResponseEntity<ApiResponse<Void>> agreeToTerms(@PathVariable TermsCode code) {
        
        // 현재 인증된 사용자 정보 가져오기
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
            
        termsService.agreeToTerms(userPrincipal.getId(), code);
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_AGREED);
    }
    
    @PostMapping("/agree")
    @Operation(summary = "여러 약관 동의", description = "여러 약관을 한 번에 동의합니다.")
    public ResponseEntity<ApiResponse<Void>> agreeToMultipleTerms(@RequestBody TermsAgreementRequest request) {
        
        // 현재 인증된 사용자 정보 가져오기
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
            
        termsService.agreeToMultipleTerms(userPrincipal.getId(), request.getTermsCodes());
        return ApiResponse.onSuccess(TermsSuccessStatus.TERMS_AGREED);
    }
    
    @GetMapping("/agreement-status")
    @Operation(summary = "사용자의 약관 동의 상태 조회", description = "현재 로그인한 사용자의 모든 약관 동의 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<TermsCode, Boolean>>> getAgreementStatus() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
            
        Map<TermsCode, Boolean> status = termsService.getMemberTermsAgreementStatus(userPrincipal.getId());
        return ApiResponse.onSuccess(TermsSuccessStatus.AGREEMENT_STATUS_FOUND, status);
    }
}
