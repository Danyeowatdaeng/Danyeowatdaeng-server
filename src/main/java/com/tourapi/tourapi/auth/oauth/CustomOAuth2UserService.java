package com.tourapi.tourapi.auth.oauth;

import com.tourapi.tourapi.auth.dto.TokenResponse;
import com.tourapi.tourapi.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthService authService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 사용자 로드 시작: provider={}", registrationId);
        log.debug("OAuth2 클라이언트 등록 정보: clientId={}, redirectUri={}", 
            userRequest.getClientRegistration().getClientId(),
            userRequest.getClientRegistration().getRedirectUri());

        try {
            // 기본 OAuth2User 로드
            OAuth2User oauth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oauth2User.getAttributes();
            log.info("OAuth2 사용자 정보 로드 완료: provider={}, attributes={}", registrationId, attributes);
            log.debug("OAuth2 사용자 상세 정보: name={}, authorities={}", oauth2User.getName(), oauth2User.getAuthorities());

            // Provider별 사용자 정보 처리
            String providerUserId;
            String email;
            String name;

            switch (registrationId) {
                case "google":
                    log.info("구글 OAuth 사용자 정보 처리 시작: attributes={}", attributes);
                    providerUserId = (String) attributes.get("sub");
                    email = (String) attributes.get("email");
                    name = (String) attributes.get("name");
                    log.info("구글 OAuth 사용자 정보 추출 완료: providerUserId={}, email={}, name={}", 
                        providerUserId, email, name);
                    break;
                case "kakao":
                    providerUserId = String.valueOf(attributes.get("id"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    email = (String) kakaoAccount.get("email");
                    name = (String) profile.get("nickname");
                    break;
                case "naver":
                    @SuppressWarnings("unchecked")
                    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                    if (response == null) {
                        log.error("Naver 응답에 response 필드가 없습니다. attributes={}", attributes);
                        throw new OAuth2AuthenticationException("Naver 사용자 정보 형식 오류");
                    }
                    providerUserId = String.valueOf(response.get("id"));
                    email = (String) response.get("email");
                    name = (String) response.get("name");
                    break;
                default:
                    throw new OAuth2AuthenticationException("지원하지 않는 OAuth 공급자: " + registrationId);
            }

            // JWT 토큰 발급
            TokenResponse tokenResponse = authService.oauthCallbackLogin(
                registrationId, providerUserId, email, name);

            log.info("OAuth2 로그인 성공: provider={}, userId={}, email={}", 
                registrationId, providerUserId, email);

            // CustomOAuth2User로 래핑하여 토큰 정보 포함
            return new CustomOAuth2User(attributes, tokenResponse, registrationId);

        } catch (Exception e) {
            log.error("OAuth2 사용자 로드 실패: provider={}, error={}", registrationId, e.getMessage(), e);
            throw new OAuth2AuthenticationException("OAuth2 인증 실패: " + e.getMessage());
        }
    }
}
