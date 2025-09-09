package com.tourapi.tourapi.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface OAuthService {
    
    /**
     * OAuth 로그인 시작
     * @param provider OAuth 공급자 (google, kakao 등)
     * @param request HTTP 요청
     * @return 리다이렉트 응답
     */
    ResponseEntity<Void> startLogin(String provider, HttpServletRequest request);
    
    /**
     * OAuth 콜백 처리
     * @param provider OAuth 공급자
     * @param code 인증 코드
     * @param state 상태 값
     * @param request HTTP 요청
     * @return 리다이렉트 응답
     */
    ResponseEntity<Void> handleCallback(String provider, String code, String state, HttpServletRequest request);
    
    /**
     * 현재 로그인된 사용자 정보 조회
     * @param request HTTP 요청
     * @return 사용자 정보
     */
    Map<String, Object> getCurrentUser(HttpServletRequest request);
    
    /**
     * 로그아웃 처리
     * @param request HTTP 요청
     */
    void logout(HttpServletRequest request);
}
