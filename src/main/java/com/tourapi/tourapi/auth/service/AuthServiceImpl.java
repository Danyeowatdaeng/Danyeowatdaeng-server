package com.tourapi.tourapi.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tourapi.tourapi.auth.dto.TokenResponse;
import com.tourapi.tourapi.auth.enums.OauthProvider;
import com.tourapi.tourapi.auth.jwt.JwtProvider;
import com.tourapi.tourapi.auth.oauth.SocialTokenVerifier;
import com.tourapi.tourapi.auth.token.RefreshTokenStore;
import com.tourapi.tourapi.member.Member;
import com.tourapi.tourapi.member.enums.Role;
import com.tourapi.tourapi.member.repository.MemberRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final SocialTokenVerifier socialTokenVerifier;
    private final MemberRepository memberRepository;

    public AuthServiceImpl(JwtProvider jwtProvider, RefreshTokenStore refreshTokenStore, SocialTokenVerifier socialTokenVerifier, MemberRepository memberRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.socialTokenVerifier = socialTokenVerifier;
        this.memberRepository = memberRepository;
    }

    @Override
    public TokenPair issueTokensOnLogin(Long id, String email, String name, Role role, boolean signUpCompleted) {
        String access = jwtProvider.createAccessToken(id, email, name, role, signUpCompleted);
        String refresh = jwtProvider.createRefreshToken(id, email, name, role, signUpCompleted);

        String uid = String.valueOf(id);
        String sid = UUID.randomUUID().toString();
        String familyId = UUID.randomUUID().toString();

        long iat = Instant.now().getEpochSecond();
        long exp = iat + jwtProvider.getRemainingValidity(refresh); // seconds

        refreshTokenStore.saveOnLogin(refresh, uid, sid, familyId, iat, exp);

        return new TokenPair(access, refresh, sid, familyId);
    }

    @Override
    public TokenResponse socialLogin(String provider, String token) {
        try {
            SocialTokenVerifier.SocialUserInfo info = socialTokenVerifier.verify(provider, token);
            String providerUserId = info.getUserId();
            String email = info.getEmail();
            String name = info.getName();

            // 제공자 식별자 + 이메일 기준으로 회원 조회, 없으면 생성
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member == null) {
                member = new Member();
                member.setProvider(OauthProvider.valueOf(provider.toUpperCase()));
                member.setProviderUserId(providerUserId);
                member.setEmail(email);
                member.setNickname(null);
                member.setProfileImageUrl(null);
                member.setSignUpCompleted(false);
                member.setRole(Role.USER);
                member = memberRepository.save(member);
            }

            Long id = member.getId();
            boolean isSignUpCompleted = member.isSignUpCompleted();

            // 가입 미완료면 액세스 토큰만 발급, 완료면 리프레시 포함 발급
            if (!isSignUpCompleted) {
                String accessToken = jwtProvider.createAccessToken(id, email, name, Role.USER, false);
                return new TokenResponse(accessToken, email, name, false);
            }

            TokenPair tokens = issueTokensOnLogin(id, email, name, Role.USER, true);
            return new TokenResponse(tokens.accessToken(), tokens.refreshToken(), tokens.sessionId(), tokens.familyId(), email, name, isSignUpCompleted);
        } catch (Exception e) {
            throw new RuntimeException("소셜 로그인 실패: " + e.getMessage(), e);
        }
    }
     
     @Override
     public TokenResponse refreshToken(String oldRefreshToken) {
         try {
             // 토큰에서 사용자 정보 파싱
             String email = jwtProvider.getEmail(oldRefreshToken);
             String name = jwtProvider.getMemberName(oldRefreshToken);
             Long id = jwtProvider.getId(oldRefreshToken);
             String roleStr = jwtProvider.getRole(oldRefreshToken);
             Role role = Role.valueOf(roleStr);
             
             // 리프레시 토큰 회전
             String uid = String.valueOf(id);
             String sid = UUID.randomUUID().toString();
             String familyId = UUID.randomUUID().toString();
             
             RefreshTokenStore.RotateResult result = refreshTokenStore.rotate(oldRefreshToken, uid, sid, familyId);
             
             // 새 액세스 토큰 생성
             String newAccessToken = jwtProvider.createAccessToken(id, email, name, role, true);
             
             return new TokenResponse(newAccessToken, result.newRefreshRaw(), sid, familyId, email, name, true);
         } catch (Exception e) {
             throw new RuntimeException("토큰 갱신 실패: " + e.getMessage(), e);
         }
     }
 }
