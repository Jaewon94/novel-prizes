package com.jaewon.novel_prizes_backend.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 실패 시 처리하는 엔트리 포인트
 * 
 * 역할:
 * - 인증되지 않은 사용자가 보호된 리소스에 접근할 때 실행
 * - 401 Unauthorized 응답과 함께 적절한 에러 메시지 반환
 * - 보안 로그 기록으로 무단 접근 시도 추적
 * 
 * 아키텍처 패턴: Exception Handler Pattern
 * 보안 전략: Fail-Safe Authentication
 * 
 * 포트폴리오 포인트:
 * - 사용자 친화적인 에러 응답 제공
 * - 보안 로깅을 통한 모니터링 지원
 * - RESTful API 표준을 준수한 에러 처리
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 실패 시 실행되는 메서드
     * 
     * 처리 과정:
     * 1. 인증 실패 상황을 로그에 기록
     * 2. 401 Unauthorized 상태 코드 설정
     * 3. JSON 형태의 에러 응답 생성
     * 4. 클라이언트에게 표준화된 에러 응답 전송
     * 
     * 에러 응답 형식:
     * {
     *   "timestamp": "2024-01-15T10:30:00.000Z",
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "인증이 필요합니다. JWT 토큰을 확인해주세요.",
     *   "path": "/api/user/profile"
     * }
     * 
     * @param request 클라이언트 요청 객체
     * @param response 서버 응답 객체
     * @param authException 인증 예외 정보
     * @throws IOException 응답 작성 중 I/O 오류
     * @throws ServletException 서블릿 처리 오류
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        // 보안 로그 기록 - 무단 접근 시도 추적
        log.warn("인증되지 않은 접근 시도 - IP: {}, URI: {}, User-Agent: {}", 
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getHeader("User-Agent"));
        
        // 응답 헤더 설정
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 에러 응답 객체 생성
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(java.time.Instant.now().toString())
                .status(401)
                .error("Unauthorized")
                .message(determineErrorMessage(authException, request))
                .path(request.getRequestURI())
                .build();
        
        // JSON 응답 전송
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 상황에 맞는 에러 메시지 결정
     * 
     * 메시지 분류:
     * - JWT 토큰이 없는 경우
     * - JWT 토큰이 만료된 경우
     * - JWT 토큰이 유효하지 않은 경우
     * - 권한이 부족한 경우
     * 
     * @param authException 인증 예외
     * @param request HTTP 요청
     * @return 사용자 친화적인 에러 메시지
     */
    private String determineErrorMessage(AuthenticationException authException, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        // Authorization 헤더가 없는 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "인증이 필요합니다. JWT 토큰을 포함한 Authorization 헤더를 전송해주세요.";
        }
        
        // JWT 토큰은 있지만 유효하지 않은 경우
        String exceptionMessage = authException.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("expired")) {
                return "토큰이 만료되었습니다. 새로운 토큰으로 다시 로그인해주세요.";
            } else if (exceptionMessage.contains("invalid")) {
                return "유효하지 않은 토큰입니다. 올바른 JWT 토큰을 사용해주세요.";
            } else if (exceptionMessage.contains("malformed")) {
                return "토큰 형식이 올바르지 않습니다. Bearer 토큰 형식을 확인해주세요.";
            }
        }
        
        // 기본 에러 메시지
        return "인증에 실패했습니다. 로그인 상태를 확인해주세요.";
    }

    /**
     * 에러 응답을 위한 내부 클래스
     * 
     * RESTful API 표준에 맞는 에러 응답 구조:
     * - timestamp: 에러 발생 시각
     * - status: HTTP 상태 코드
     * - error: 에러 타입
     * - message: 사용자 친화적 에러 메시지
     * - path: 에러가 발생한 API 경로
     */
    @lombok.Builder
    @lombok.Getter
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
    }
}