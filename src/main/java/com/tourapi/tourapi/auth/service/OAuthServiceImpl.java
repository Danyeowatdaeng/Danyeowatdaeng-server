package com.tourapi.tourapi.auth.service;

import com.tourapi.tourapi.auth.oauth.strategy.OAuthProviderStrategy;
import com.tourapi.tourapi.auth.oauth.strategy.OAuthStrategyFactory;
import com.tourapi.tourapi.common.exception.member.status.MemberErrorStatus;
import com.tourapi.tourapi.common.exception.member.status.MemberSuccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
public class OAuthServiceImpl implements OAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthServiceImpl.class);
    
    private final OAuthStrategyFactory strategyFactory;
    
    @Value("${app.auth.success-redirect-url:/app}")
    private String successRedirectUrl;
    
    public OAuthServiceImpl(OAuthStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }
    
    @Override
    public ResponseEntity<Void> startLogin(String provider, HttpServletRequest request) {
        logger.info("OAuth 로그인 시작 요청: provider={}, clientIP={}", provider, getClientIP(request));
        
        try {
            OAuthProviderStrategy strategy = strategyFactory.get(provider);
            ResponseEntity<Void> response = strategy.startLogin(request);
            logger.info("OAuth 로그인 시작 성공: provider={}", provider);
            return response;
        } catch (IllegalArgumentException ex) {
            logger.warn("지원하지 않는 OAuth 공급자: provider={}", provider);
            return ResponseEntity.status(MemberErrorStatus.UNSUPPORTED_OAUTH_PROVIDER.getErrorStatus()).build();
        } catch (Exception ex) {
            logger.error("OAuth 로그인 시작 실패: provider={}", provider, ex);
            return ResponseEntity.status(MemberErrorStatus.OAUTH_LOGIN_START_FAILED.getErrorStatus()).build();
        }
    }
    
    @Override
    public ResponseEntity<Void> handleCallback(String provider, String code, String state, HttpServletRequest request) {
        logger.info("OAuth 콜백 요청: provider={}, hasCode={}, hasState={}, clientIP={}", 
                provider, code != null, state != null, getClientIP(request));
        
        // 인증 코드 검증
        if (code == null || code.trim().isEmpty()) {
            logger.warn("OAuth 인증 코드 누락: provider={}", provider);
            MultiValueMap<String, String> headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, "/login?error=code-missing");
            return new ResponseEntity<>(headers, MemberErrorStatus.OAUTH_CODE_MISSING.getErrorStatus());
        }
        
        HttpSession session = request.getSession(true);
        MultiValueMap<String, String> headers = new HttpHeaders();
        
        try {
            OAuthProviderStrategy strategy = strategyFactory.get(provider);
            Map<String, Object> signed = strategy.handleCallback(request, code, state);
            session.setAttribute("SIGNED_MEMBER", signed);
            
            logger.info("OAuth 로그인 성공: provider={}, userId={}, email={}", 
                    provider, signed.get("providerUserId"), signed.get("email"));
            
            headers.add(HttpHeaders.LOCATION, successRedirectUrl);
            return new ResponseEntity<>(headers, MemberSuccessStatus.OAUTH_CALLBACK_SUCCESS.getSuccessStatus());
        } catch (IllegalArgumentException ex) {
            logger.warn("지원하지 않는 OAuth 공급자: provider={}", provider);
            headers.add(HttpHeaders.LOCATION, "/login?error=unsupported-provider");
            return new ResponseEntity<>(headers, MemberErrorStatus.UNSUPPORTED_OAUTH_PROVIDER.getErrorStatus());
        } catch (Exception e) {
            logger.error("OAuth 콜백 처리 실패: provider={}", provider, e);
            headers.add(HttpHeaders.LOCATION, "/login?error=callback");
            return new ResponseEntity<>(headers, MemberErrorStatus.OAUTH_CALLBACK_FAILED.getErrorStatus());
        }
    }
    
    @Override
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        logger.debug("사용자 정보 조회 요청: clientIP={}", getClientIP(request));
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            logger.debug("세션이 없음");
            throw new IllegalStateException("세션이 만료되었습니다.");
        }
        
        Object signed = session.getAttribute("SIGNED_MEMBER");
        if (signed == null) {
            logger.debug("로그인되지 않은 사용자");
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> member = (Map<String, Object>) signed;
        logger.info("사용자 정보 조회 성공: provider={}, userId={}, email={}", 
                member.get("provider"), member.get("providerUserId"), member.get("email"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("member", member);
        
        return response;
    }
    
    @Override
    public void logout(HttpServletRequest request) {
        logger.info("로그아웃 요청: clientIP={}", getClientIP(request));
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object signed = session.getAttribute("SIGNED_MEMBER");
            if (signed != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> member = (Map<String, Object>) signed;
                logger.info("로그아웃 성공: provider={}, userId={}, email={}", 
                        member.get("provider"), member.get("providerUserId"), member.get("email"));
            }
            session.invalidate();
            logger.info("OAuth 세션 무효화 완료");
        } else {
            logger.debug("로그아웃 요청 - 활성 세션 없음");
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        return request.getRemoteAddr();
    }
}
