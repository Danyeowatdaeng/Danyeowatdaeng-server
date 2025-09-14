package com.tourapi.tourapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tourapi.tourapi.auth.jwt.JwtAuthenticationFilter;
import com.tourapi.tourapi.auth.oauth.CustomOAuth2UserService;
import com.tourapi.tourapi.auth.oauth.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests((authz) -> authz
                // // OAuth2 로그인 관련 엔드포인트 허용
                // .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                // // 기존 API 엔드포인트들
                // .requestMatchers("/api/auth/refresh", "/api/auth/me").authenticated()
                // .requestMatchers("/api/terms/agree-terms", "/api/terms/agreement-status").authenticated()
                // // 공개 엔드포인트
                // .requestMatchers("/api/terms/current", "/api/terms/*").permitAll()
                // .requestMatchers("/h2-console/**").permitAll()
                // .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll()
        );

        http.csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement((session) -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2Login(oauth2 -> oauth2
                    .clientRegistrationRepository(clientRegistrationRepository)
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                    )
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("/login?error=oauth_failed");
                    })
            );
            // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 특정 도메인만 허용 (프로덕션)
        configuration.addAllowedOrigin("https://danyeowatdaeng.p-e.kr");
        configuration.addAllowedOrigin("https://www.danyeowatdaeng.p-e.kr");
        configuration.addAllowedOrigin("https://danyeowatdaeng-one.vercel.app");
        
        // 개발 환경 도메인 허용
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://localhost:3001");
        configuration.addAllowedOrigin("http://127.0.0.1:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:3001");
        
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
