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
public class NaverOAuthStrategy implements OAuthProviderStrategy {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${oauth.naver.client-id:}")
    private String clientId;

    @Value("${oauth.naver.client-secret:}")
    private String clientSecret;

    @Value("${oauth.naver.redirect-uri:}")
    private String redirectUri;

    @Override
    public String providerName() {
        return "naver";
    }

    @Override
    public ResponseEntity<Void> startLogin(HttpServletRequest request) {
        String resolvedRedirect = (redirectUri == null || redirectUri.isBlank()) ? buildRedirectUri(request) : redirectUri;
        String authUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(resolvedRedirect)
                + "&scope=" + urlEncode("name email profile_image");

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
        tokenForm.add("state", "RANDOM_STATE"); // 네이버는 state 파라미터 필수

        Map<String, Object> token = webClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(tokenForm)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (token == null || token.get("access_token") == null) {
            throw new IllegalStateException("Naver token exchange failed");
        }

        // 2. 사용자 정보 조회
        Map<String, Object> userinfo = webClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get("access_token"))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (userinfo == null || userinfo.get("response") == null) {
            throw new IllegalStateException("Naver userinfo failed");
        }

        // 3. 사용자 정보 파싱 (네이버는 response 객체 안에 사용자 정보가 있음)
        Map<String, Object> response = (Map<String, Object>) userinfo.get("response");

        Map<String, Object> signed = new HashMap<>();
        signed.put("provider", providerName());
        signed.put("providerUserId", String.valueOf(response.get("id")));
        signed.put("email", response.get("email"));
        signed.put("name", response.get("name"));
        signed.put("profileImage", response.get("profile_image"));

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
        return scheme + "://" + host + "/api/auth/callback/naver";
    }
}
