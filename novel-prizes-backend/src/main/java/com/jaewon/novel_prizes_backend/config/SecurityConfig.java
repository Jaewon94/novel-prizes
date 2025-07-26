package com.jaewon.novel_prizes_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.jaewon.novel_prizes_backend.security.JwtAuthenticationEntryPoint;
import com.jaewon.novel_prizes_backend.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 보안 설정 클래스
 * 
 * 주요 기능:
 * - JWT 기반 인증/인가 시스템 구성
 * - CORS 설정으로 프론트엔드와의 통신 허용
 * - 비밀번호 암호화 설정 (BCrypt)
 * - 세션 비활성화 (Stateless 인증)
 * 
 * 아키텍처 패턴: Configuration Pattern
 * 보안 전략: JWT + Stateless Authentication
 * 
 * 포트폴리오 포인트:
 * - 현대적인 JWT 기반 인증 시스템 구현
 * - 마이크로서비스 아키텍처에 적합한 Stateless 설계
 * - CORS 정책을 통한 보안과 접근성의 균형
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 비밀번호 암호화를 위한 BCrypt 인코더 빈 등록
     * 
     * BCrypt 특징:
     * - Salt를 자동으로 생성하여 Rainbow Table 공격 방지
     * - 계산 비용을 조정할 수 있어 해시 강도 설정 가능
     * - Spring Security에서 권장하는 암호화 방식
     * 
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 강도 12로 설정 (기본값 10보다 강화)
    }

    /**
     * AuthenticationManager 빈 등록
     * 
     * 역할:
     * - 인증 과정의 핵심 컴포넌트
     * - UserDetailsService와 PasswordEncoder를 조합하여 인증 수행
     * - 로그인 시 사용자 자격 증명 검증
     * 
     * @param config Spring Security 인증 설정
     * @return AuthenticationManager 인스턴스
     * @throws Exception 설정 오류 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * 
     * 설정 내용:
     * - 프론트엔드 도메인에서의 접근 허용
     * - REST API 메서드 허용 (GET, POST, PUT, DELETE, OPTIONS)
     * - 인증 헤더 및 JWT 토큰 헤더 허용
     * - Preflight 요청 처리
     * 
     * 보안 고려사항:
     * - 운영 환경에서는 특정 도메인만 허용하도록 제한
     * - allowCredentials: true로 설정하여 쿠키 기반 인증 지원
     * 
     * @return CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin 설정 (개발 환경)
        configuration.addAllowedOriginPattern("http://localhost:*"); // 로컬 개발 서버
        configuration.addAllowedOriginPattern("https://*.jaewon.dev"); // 운영 도메인
        
        // 허용할 HTTP 메서드
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("PATCH");
        
        // 허용할 헤더
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Authorization"); // JWT 토큰 헤더 노출
        
        // 자격 증명 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Spring Security 필터 체인 설정
     * 
     * 보안 정책:
     * 1. 세션 비활성화 (JWT를 위한 Stateless 설정)
     * 2. CSRF 비활성화 (JWT 사용으로 불필요)
     * 3. 경로별 접근 권한 설정
     * 4. JWT 필터 적용
     * 5. 예외 처리 핸들러 설정
     * 
     * 접근 권한 정책:
     * - /auth/** : 인증 없이 접근 가능 (로그인, 회원가입)
     * - /api/public/** : 공개 API (소설 랭킹 조회 등)
     * - /api/admin/** : 관리자만 접근 가능
     * - 나머지 API : 인증된 사용자만 접근 가능
     * 
     * @param http HttpSecurity 설정 객체
     * @return SecurityFilterChain 인스턴스
     * @throws Exception 설정 오류 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 정책 설정 (Stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 요청별 인가 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 관련 엔드포인트는 모든 사용자 접근 허용
                .requestMatchers("/auth/**").permitAll()
                
                // 공개 API 엔드포인트 (소설 랭킹, 검색 등)
                .requestMatchers("/api/public/**").permitAll()
                
                // 개발 도구 접근 허용
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 (개발용)
                
                // 관리자 전용 API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 작가 전용 API (소설 등록, 수정 등)
                .requestMatchers("/api/author/**").hasAnyRole("AUTHOR", "ADMIN")
                
                // 나머지 API는 인증된 사용자만 접근 가능
                .anyRequest().authenticated()
            )
            
            // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 인증 예외 처리 핸들러 설정
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );

        return http.build();
    }
}