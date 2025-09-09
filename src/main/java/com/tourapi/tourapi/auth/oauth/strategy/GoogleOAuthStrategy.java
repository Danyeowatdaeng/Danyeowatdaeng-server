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
public class GoogleOAuthStrategy implements OAuthProviderStrategy {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${oauth.google.client-id:}")
    private String clientId;

    @Value("${oauth.google.client-secret:}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri:}")
    private String redirectUri;

    @Override
    public String providerName() {
        return "google";
    }

    @Override
    public ResponseEntity<Void> startLogin(HttpServletRequest request) {
        String resolvedRedirect = (redirectUri == null || redirectUri.isBlank()) ? buildRedirectUri(request) : redirectUri;
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(resolvedRedirect)
                + "&scope=" + urlEncode("openid email profile");

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, authUrl);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleCallback(HttpServletRequest request, String code, String state) {
        String resolvedRedirect = (redirectUri == null || redirectUri.isBlank()) ? buildRedirectUri(request) : redirectUri;

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", resolvedRedirect);

        Map<String, Object> token = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (token == null || token.get("access_token") == null) {
            throw new IllegalStateException("token exchange failed");
        }

        Map<String, Object> userinfo = webClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.get("access_token"))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (userinfo == null || userinfo.get("sub") == null) {
            throw new IllegalStateException("userinfo failed");
        }

        Map<String, Object> signed = new HashMap<>();
        signed.put("provider", providerName());
        signed.put("providerUserId", String.valueOf(userinfo.get("sub")));
        signed.put("email", userinfo.get("email"));
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
        return scheme + "://" + host + "/api/auth/callback/google";
    }
}


