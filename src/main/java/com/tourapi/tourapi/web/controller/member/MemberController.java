package com.tourapi.tourapi.web.controller.member;

import com.tourapi.tourapi.auth.jwt.JwtProvider;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.service.MemberService;
import com.tourapi.tourapi.petAvatar.dto.PetAvatarSelectionRequest;
import com.tourapi.tourapi.petAvatar.service.PetAvatarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final PetAvatarService petAvatarService;
    private final JwtProvider jwtProvider;

    // PetAvatar 선택
    @PostMapping("/pet-avatar")
    public ResponseEntity<String> selectPetAvatar(
            @Valid @RequestBody PetAvatarSelectionRequest request,
            HttpServletRequest httpRequest) {
        
        Long memberId = getMemberIdFromRequest(httpRequest);
        
        petAvatarService.selectPetAvatarForMember(memberId, request.getPetAvatarId());
        
        log.info("PetAvatar {} selected for member {}", request.getPetAvatarId(), memberId);
        return ResponseEntity.ok("PetAvatar selected successfully");
    }

    // 회원가입 완료
    @PostMapping("/complete-signup")
    public ResponseEntity<String> completeSignup(HttpServletRequest request) {
        Long memberId = getMemberIdFromRequest(request);
        
        memberService.completeSignup(memberId);
        
        log.info("Signup completed for member {}", memberId);
        return ResponseEntity.ok("Signup completed successfully");
    }

    // 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<Member> getMemberInfo(HttpServletRequest request) {
        Long memberId = getMemberIdFromRequest(request);
        
        Member member = memberService.getMemberById(memberId);
        
        log.info("Member info retrieved for member {}", memberId);
        return ResponseEntity.ok(member);
    }

    private Long getMemberIdFromRequest(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        return jwtProvider.getMemberId(token);
    }
}
