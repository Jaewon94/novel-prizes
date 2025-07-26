package com.jaewon.novel_prizes_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaewon.novel_prizes_backend.dto.auth.AuthResponse;
import com.jaewon.novel_prizes_backend.dto.auth.LoginRequest;
import com.jaewon.novel_prizes_backend.dto.auth.SignupRequest;
import com.jaewon.novel_prizes_backend.dto.auth.TokenRefreshRequest;
import com.jaewon.novel_prizes_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 인증 관련 REST API 컨트롤러
 * 
 * 제공 기능:
 * - 회원가입 (POST /auth/signup)
 * - 로그인 (POST /auth/login)
 * - 토큰 갱신 (POST /auth/refresh)
 * - 로그아웃 (POST /auth/logout) - 향후 구현
 * 
 * API 설계 원칙:
 * - RESTful 설계 준수
 * - HTTP 상태 코드 표준 활용
 * - 일관된 응답 형식 제공
 * - 상세한 API 문서화 (Swagger)
 * 
 * 보안 고려사항:
 * - 입력 데이터 검증 (@Valid)
 * - 예외 처리로 보안 정보 노출 방지
 * - 로깅을 통한 보안 이벤트 추적
 * 
 * 아키텍처 패턴: MVC Controller Pattern
 * 
 * 포트폴리오 포인트:
 * - 현대적인 Spring Boot REST API 구현
 * - OpenAPI/Swagger 문서화로 API 가독성 향상
 * - 완전한 JWT 기반 인증 시스템
 * - 예외 처리 및 보안 모범 사례 적용
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "인증 API", description = "사용자 인증 관련 API (회원가입, 로그인, 토큰 갱신)")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * 
     * 기능:
     * - 새로운 사용자 계정 생성
     * - 기본 사용자 프로필 설정
     * - 회원가입과 동시에 자동 로그인 처리
     * - JWT 토큰 발급
     * 
     * 검증 항목:
     * - 이메일 형식 및 중복 검사
     * - 비밀번호 복잡성 및 일치 검사
     * - 닉네임 중복 검사
     * - 이용약관 동의 확인
     * 
     * @param signupRequest 회원가입 요청 정보
     * @return 201 Created + 인증 토큰 및 사용자 정보
     */
    @Operation(
        summary = "회원가입",
        description = "새로운 사용자 계정을 생성하고 자동으로 로그인 처리합니다. " +
                     "회원가입과 동시에 JWT 토큰이 발급되어 즉시 서비스 이용이 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이메일 중복, 비밀번호 불일치, 유효성 검증 실패 등)"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "리소스 충돌 (이메일 또는 닉네임 중복)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("회원가입 API 호출 - 이메일: {}, 사용자타입: {}", 
                signupRequest.getEmail(), signupRequest.getUserType());

        try {
            // 회원가입 처리
            AuthResponse authResponse = authService.signup(signupRequest);
            
            log.info("회원가입 성공 - 사용자 ID: {}", authResponse.getUserId());
            
            // 201 Created 상태로 응답
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
            
        } catch (IllegalArgumentException ex) {
            // 비즈니스 로직 검증 실패 (중복, 유효성 등)
            log.warn("회원가입 실패 - 이메일: {}, 원인: {}", signupRequest.getEmail(), ex.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception ex) {
            // 예상치 못한 서버 오류
            log.error("회원가입 처리 중 서버 오류 - 이메일: {}", signupRequest.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 로그인 API
     * 
     * 기능:
     * - 이메일/비밀번호 기반 사용자 인증
     * - JWT Access Token 및 Refresh Token 발급
     * - 마지막 로그인 시간 업데이트
     * - 사용자 기본 정보 응답
     * 
     * 인증 과정:
     * 1. Spring Security AuthenticationManager로 자격 증명 검증
     * 2. 계정 활성화 상태 확인
     * 3. JWT 토큰 생성 및 발급
     * 4. 로그인 시간 기록
     * 
     * @param loginRequest 로그인 요청 정보 (이메일, 비밀번호)
     * @return 200 OK + JWT 토큰 및 사용자 정보
     */
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다. " +
                     "발급받은 Access Token을 Authorization 헤더에 포함하여 API를 호출할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 필드 누락, 형식 오류 등)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (이메일 또는 비밀번호 오류, 계정 비활성화 등)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("로그인 API 호출 - 이메일: {}", loginRequest.getEmail());

        try {
            // 로그인 처리
            AuthResponse authResponse = authService.login(loginRequest);
            
            log.info("로그인 성공 - 사용자 ID: {}", authResponse.getUserId());
            
            // 200 OK 상태로 응답
            return ResponseEntity.ok(authResponse);
            
        } catch (IllegalArgumentException ex) {
            // 인증 실패 (잘못된 자격 증명)
            log.warn("로그인 실패 - 이메일: {}, 원인: {}", loginRequest.getEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
        } catch (Exception ex) {
            // 예상치 못한 서버 오류
            log.error("로그인 처리 중 서버 오류 - 이메일: {}", loginRequest.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * JWT 토큰 갱신 API
     * 
     * 기능:
     * - Refresh Token을 이용한 Access Token 갱신
     * - 사용자 재로그인 없이 인증 상태 연장
     * - 토큰 만료 전 자동 갱신 지원
     * 
     * 갱신 과정:
     * 1. Refresh Token 유효성 검증
     * 2. 토큰에서 사용자 정보 추출
     * 3. 사용자 계정 상태 확인
     * 4. 새로운 Access Token 생성
     * 
     * 보안 고려사항:
     * - Refresh Token의 서명 및 만료 시간 엄격 검증
     * - 토큰 갱신 시 사용자 계정 상태 재확인
     * - 향후 Rotating Refresh Token 구현 예정
     * 
     * @param tokenRefreshRequest Refresh Token을 포함한 갱신 요청
     * @return 200 OK + 새로운 Access Token
     */
    @Operation(
        summary = "토큰 갱신",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다. " +
                     "Access Token이 만료되기 전에 이 API를 호출하여 인증 상태를 연장할 수 있습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (Refresh Token 누락, 형식 오류 등)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (유효하지 않은 Refresh Token, 계정 비활성화 등)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        log.debug("토큰 갱신 API 호출");

        try {
            // 토큰 갱신 처리
            AuthResponse authResponse = authService.refreshToken(tokenRefreshRequest);
            
            log.debug("토큰 갱신 성공");
            
            // 200 OK 상태로 응답
            return ResponseEntity.ok(authResponse);
            
        } catch (IllegalArgumentException ex) {
            // 토큰 검증 실패
            log.warn("토큰 갱신 실패 - 원인: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            
        } catch (Exception ex) {
            // 예상치 못한 서버 오류
            log.error("토큰 갱신 처리 중 서버 오류", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 로그아웃 API (향후 구현 예정)
     * 
     * 계획된 기능:
     * - 현재 Access Token 무효화
     * - Refresh Token 삭제 (Redis에서 제거)
     * - 로그아웃 시간 기록
     * - 다중 기기 로그아웃 지원
     * 
     * 구현 방식:
     * - JWT Token Blacklist (Redis 활용)
     * - Refresh Token Store에서 해당 토큰 삭제
     * - 클라이언트 측 토큰 저장소 정리 안내
     */
    /*
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 세션을 종료하고 토큰을 무효화합니다.")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        // 향후 구현 예정
        return ResponseEntity.ok().build();
    }
    */
}