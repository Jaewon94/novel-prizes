package com.jaewon.novel_prizes_backend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰을 검증하고 인증을 처리하는 필터
 * 
 * 동작 원리:
 * 1. HTTP 요청에서 Authorization 헤더 추출
 * 2. Bearer 토큰 형식 검증 및 JWT 토큰 파싱
 * 3. JWT 토큰의 유효성 검증 (서명, 만료시간 등)
 * 4. 토큰에서 사용자 정보 추출 및 UserDetails 로드
 * 5. Spring Security Context에 인증 정보 설정
 * 
 * 아키텍처 패턴: Filter Chain Pattern
 * 보안 전략: Stateless JWT Authentication
 * 
 * 포트폴리오 포인트:
 * - OncePerRequestFilter 상속으로 요청당 한 번만 실행 보장
 * - 견고한 예외 처리로 보안 취약점 방지
 * - 상세한 로깅으로 디버깅 및 모니터링 지원
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * 요청별 JWT 인증 처리 메서드
     * 
     * 처리 순서:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. 토큰 유효성 검증
     * 3. 토큰에서 사용자 ID 추출
     * 4. UserDetailsService를 통해 사용자 정보 로드
     * 5. Spring Security Context에 인증 정보 설정
     * 6. 다음 필터로 요청 전달
     * 
     * 예외 처리:
     * - 토큰이 없거나 형식이 잘못된 경우 → 인증 없이 진행
     * - 토큰이 만료되거나 유효하지 않은 경우 → 인증 없이 진행
     * - 사용자 정보를 찾을 수 없는 경우 → 인증 없이 진행
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 처리 오류
     * @throws IOException I/O 처리 오류
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);
            
            // 2. 토큰이 있고 유효한 경우 인증 처리
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                
                // 3. 토큰에서 사용자 ID 추출
                String userId = jwtTokenProvider.getUserIdFromToken(jwt);
                
                // 4. 현재 SecurityContext에 인증 정보가 없는 경우에만 처리
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 5. UserDetailsService를 통해 사용자 정보 로드
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    
                    // 6. 토큰과 사용자 정보가 유효한 경우 인증 객체 생성
                    if (jwtTokenProvider.validateTokenWithUserDetails(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                            );
                        
                        // 7. 웹 인증 세부 정보 설정 (IP 주소, 세션 ID 등)
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 8. Security Context에 인증 정보 저장
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // 성공 로그 기록 (DEBUG 레벨)
                        log.debug("JWT 인증 성공 - 사용자: {}, IP: {}", userId, request.getRemoteAddr());
                    }
                }
            }
            
        } catch (Exception ex) {
            // JWT 처리 중 예외 발생 시 로그 기록 후 인증 없이 진행
            log.error("JWT 인증 처리 중 오류 발생 - IP: {}, URI: {}, Error: {}", 
                     request.getRemoteAddr(), 
                     request.getRequestURI(), 
                     ex.getMessage());
            
            // SecurityContext 초기화 (보안을 위해)
            SecurityContextHolder.clearContext();
        }
        
        // 9. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * 
     * Authorization 헤더 형식: "Bearer {JWT_TOKEN}"
     * 
     * 검증 과정:
     * 1. Authorization 헤더 존재 여부 확인
     * 2. "Bearer " 접두사 확인
     * 3. 토큰 부분만 추출하여 반환
     * 
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Authorization 헤더가 있고 "Bearer "로 시작하는 경우
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사를 제거하고 토큰만 반환
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * 필터를 적용하지 않을 요청 경로 설정
     * 
     * 제외 대상:
     * - 인증 관련 API (/auth/*)
     * - 공개 API (/api/public/*)
     * - 개발 도구 (Swagger, H2 Console 등)
     * - 정적 리소스
     * 
     * 성능 최적화:
     * - 불필요한 JWT 검증 과정을 생략하여 응답 속도 향상
     * - 로그 부하 감소
     * 
     * @param request HTTP 요청 객체
     * @return true: 필터 적용 안함, false: 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요하지 않은 경로들
        return path.startsWith("/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/favicon.ico") ||
               path.startsWith("/error");
    }
}