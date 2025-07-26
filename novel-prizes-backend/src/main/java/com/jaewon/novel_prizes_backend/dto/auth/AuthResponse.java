package com.jaewon.novel_prizes_backend.dto.auth;

import java.time.LocalDateTime;

import com.jaewon.novel_prizes_backend.entity.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 인증 성공 응답 DTO (Data Transfer Object)
 * 
 * 목적:
 * - 로그인/회원가입 성공 시 클라이언트에게 전달할 정보 구성
 * - JWT 토큰과 사용자 기본 정보를 안전하게 전송
 * - 클라이언트의 상태 관리 및 UI 구성을 위한 데이터 제공
 * 
 * 보안 고려사항:
 * - 민감한 정보(비밀번호 해시, 내부 ID 등) 제외
 * - 토큰 만료 시간 제공으로 클라이언트 측 토큰 관리 지원
 * - 최소한의 사용자 정보만 포함하여 정보 노출 최소화
 * 
 * 아키텍처 패턴: DTO Pattern + Builder Pattern
 * 
 * 포트폴리오 포인트:
 * - RESTful API 표준을 준수한 응답 구조
 * - 클라이언트 친화적인 데이터 설계
 * - 보안과 사용성의 균형을 고려한 정보 제공
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * JWT Access Token
     * 
     * 용도:
     * - API 호출 시 Authorization 헤더에 포함
     * - 인증이 필요한 모든 요청에 사용
     * - 짧은 수명 (1시간)으로 보안 위험 최소화
     * 
     * 클라이언트 처리:
     * - localStorage 또는 sessionStorage에 저장
     * - 자동으로 API 요청 헤더에 포함하도록 설정
     * - 만료 시 Refresh Token으로 갱신
     */
    private String accessToken;

    /**
     * JWT Refresh Token
     * 
     * 용도:
     * - Access Token 만료 시 새로운 토큰 발급
     * - 긴 수명 (30일)으로 사용자 편의성 제공
     * - 서버 측 Redis에서 별도 관리 권장
     * 
     * 보안 전략:
     * - HttpOnly 쿠키로 저장 권장 (XSS 공격 방지)
     * - 또는 안전한 클라이언트 저장소 사용
     * - 로그아웃 시 즉시 무효화
     */
    private String refreshToken;

    /**
     * 토큰 타입 (Bearer)
     * 
     * HTTP Authorization 헤더 형식:
     * "Authorization: Bearer {accessToken}"
     * 
     * 표준 준수:
     * - OAuth 2.0 및 RFC 6750 표준 준수
     * - 다양한 HTTP 클라이언트와 호환성 보장
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access Token 만료 시간 (초 단위)
     * 
     * 클라이언트 활용:
     * - 토큰 자동 갱신 타이밍 결정
     * - 사용자에게 재로그인 안내 시점 계산
     * - 백그라운드 토큰 갱신 스케줄링
     * 
     * 기본값: 3600초 (1시간)
     */
    private Long expiresIn;

    /**
     * 사용자 고유 ID
     * 
     * 용도:
     * - 클라이언트 측 사용자 식별
     * - 사용자별 데이터 관리
     * - 분석 및 로깅
     * 
     * 주의사항:
     * - UUID 형태로 외부 노출에 안전
     * - 순차적 ID 사용 시 정보 유출 위험
     */
    private String userId;

    /**
     * 사용자 이메일 주소
     * 
     * 클라이언트 활용:
     * - 사용자 인터페이스 표시
     * - 프로필 페이지 기본 정보
     * - 이메일 변경 시 현재 값 표시
     */
    private String email;

    /**
     * 사용자 닉네임
     * 
     * 용도:
     * - 사용자 인터페이스에서 친근한 이름 표시
     * - 댓글, 리뷰 등에서 작성자 표시
     * - 개인화된 인사말 구성
     */
    private String nickname;

    /**
     * 사용자 타입 (독자/작가)
     * 
     * 클라이언트 활용:
     * - 권한별 UI 구성 (작가 전용 메뉴 등)
     * - 기능 접근 제어
     * - 사용자 경험 개인화
     * 
     * 값:
     * - READER: 독자 권한
     * - AUTHOR: 작가 권한
     */
    private UserType userType;

    /**
     * 계정 활성화 상태
     * 
     * 용도:
     * - 비활성화된 계정 알림
     * - 제한된 기능 안내
     * - 계정 복구 프로세스 안내
     */
    private Boolean isActive;

    /**
     * 마지막 로그인 시간
     * 
     * 클라이언트 활용:
     * - 보안 정보 표시 ("마지막 로그인: 1시간 전")
     * - 사용자 활동 패턴 분석
     * - 장기 미접속 사용자 안내
     */
    private LocalDateTime lastLoginAt;

    /**
     * 계정 생성 시간
     * 
     * 용도:
     * - 가입 기념일 안내
     * - 계정 연혁 표시
     * - 신규 사용자 온보딩 프로세스
     */
    private LocalDateTime createdAt;

    /**
     * 성공적인 인증 응답 생성을 위한 정적 팩토리 메서드
     * 
     * @param accessToken JWT Access Token
     * @param refreshToken JWT Refresh Token
     * @param expiresIn 토큰 만료 시간 (초)
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param userType 사용자 타입
     * @param isActive 계정 활성화 상태
     * @param lastLoginAt 마지막 로그인 시간
     * @param createdAt 계정 생성 시간
     * @return 완성된 AuthResponse 객체
     */
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn,
                                 String userId, String email, String nickname, UserType userType,
                                 Boolean isActive, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .userType(userType)
                .isActive(isActive)
                .lastLoginAt(lastLoginAt)
                .createdAt(createdAt)
                .build();
    }

    /**
     * 토큰 갱신용 응답 생성을 위한 정적 팩토리 메서드
     * 
     * @param accessToken 새로 발급된 Access Token
     * @param expiresIn 토큰 만료 시간 (초)
     * @return 토큰 정보만 포함된 AuthResponse 객체
     */
    public static AuthResponse ofTokenRefresh(String accessToken, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}