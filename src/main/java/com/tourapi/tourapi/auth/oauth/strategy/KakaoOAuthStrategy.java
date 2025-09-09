package com.tourapi.tourapi.auth.oauth.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class KakaoOAuthStrategy implements OAuthProviderStrategy {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${oauth.kakao.client-id:}")
    private String clientId;

    @Value("${oauth.kakao.client-secret:}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri:}")
    private String redirectUri;

    @Override
    public String providerName() {
        return "kakao";
    }

    @Override
    public ResponseEntity<Void> startLogin(HttpServletRequest request) {
        String resolvedRedirect = (redirectUri == null || redirectUri.isBlank()) ? buildRedirectUri(request) : redirectUri;
        String authUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(resolvedRedirect)
                + "&scope=" + urlEncode("profile_nickname account_email");

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, authUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleCallback(HttpServletRequest request, String code, String state) {
        String resolvedRedirect = (redirectUri == null || redirectUri.isBlank()) ? buildRedirectUri(request) : redirectUri;

        // 1. 토큰 교환
        LinkedMultiValueMap<String, String> tokenForm = new LinkedMultiValueMap<>();
        tokenForm.add("grant_type", "authorization_code");
        tokenForm.add("client_id", clientId);
        tokenForm.add("client_secret", clientSecret);
        tokenForm.add("redirect_uri", resolvedRedirect);
        tokenForm.add("code", code);

        Map<String, Object> token = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(tokenForm)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (token == null || token.get("access_token") == null) {
            throw new IllegalStateException("Kakao token exchange failed");
        }

        // 2. 사용자 정보 조회
        Map<String, Object> userinfo = webClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get("access_token"))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userinfo == null || userinfo.get("id") == null) {
            throw new IllegalStateException("Kakao userinfo failed");
        }

        // 3. 사용자 정보 파싱
        Map<String, Object> kakaoAccount = (Map<String, Object>) userinfo.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        Map<String, Object> signed = new HashMap<>();
        signed.put("provider", providerName());
        signed.put("providerUserId", String.valueOf(userinfo.get("id")));
        signed.put("email", kakaoAccount.get("email"));
        signed.put("name", profile.get("nickname"));
        signed.put("profileImage", profile.get("profile_image_url"));

        return signed;
    }

    private static String urlEncode(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String buildRedirectUri(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null || scheme.isBlank()) {
            scheme = request.getScheme();
        }
        String host = request.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            int port = request.getServerPort();
            boolean standard = (port == 80 || port == 443);
            host = request.getServerName() + (standard ? "" : (":" + port));
        }
        return scheme + "://" + host + "/api/auth/callback/kakao";
    }
}
