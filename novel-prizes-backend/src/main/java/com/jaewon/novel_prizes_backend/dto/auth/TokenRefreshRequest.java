package com.jaewon.novel_prizes_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 토큰 갱신 요청 DTO (Data Transfer Object)
 * 
 * 목적:
 * - Access Token 만료 시 Refresh Token을 이용한 토큰 갱신
 * - 사용자의 재로그인 없이 인증 상태 유지
 * - 보안과 사용자 편의성의 균형 제공
 * 
 * 토큰 갱신 프로세스:
 * 1. 클라이언트가 Access Token 만료 감지
 * 2. 저장된 Refresh Token으로 갱신 요청
 * 3. 서버에서 Refresh Token 유효성 검증
 * 4. 새로운 Access Token 발급 및 응답
 * 5. 필요시 Refresh Token도 함께 갱신 (Rotating Refresh Token)
 * 
 * 보안 전략:
 * - Refresh Token의 짧은 수명과 일회성 사용
 * - 토큰 탈취 시 피해 최소화
 * - 비정상적인 갱신 패턴 감지 및 차단
 * 
 * 아키텍처 패턴: DTO Pattern
 * 
 * 포트폴리오 포인트:
 * - 현대적인 JWT 기반 인증 시스템의 핵심 기능
 * - 사용자 경험과 보안의 균형점 제시
 * - 토큰 기반 인증의 완전한 구현
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequest {

    /**
     * 갱신에 사용할 Refresh Token
     * 
     * 검증 조건:
     * - 필수 입력 (null, 빈 문자열, 공백 불허)
     * - JWT 형식 유효성은 서비스 레이어에서 검증
     * - 만료 시간 및 서명 검증은 JwtTokenProvider에서 처리
     * 
     * 보안 고려사항:
     * - 한 번 사용된 Refresh Token은 즉시 무효화 (Rotating Token)
     * - Redis 등 별도 저장소에서 토큰 상태 관리
     * - 비정상적인 사용 패턴 감지 시 모든 토큰 무효화
     * 
     * 클라이언트 처리:
     * - 안전한 저장소에 보관 (HttpOnly 쿠키 권장)
     * - Access Token과 분리하여 관리
     * - 로그아웃 시 즉시 삭제
     */
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;

    /**
     * 보안을 위한 toString 메서드 오버라이드
     * 
     * 목적:
     * - 로그 파일에 Refresh Token이 노출되는 것을 방지
     * - 개발 과정에서의 토큰 유출 차단
     * - 디버깅 시 민감 정보 보호
     * 
     * @return 토큰을 마스킹한 문자열 표현
     */
    @Override
    public String toString() {
        return "TokenRefreshRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}