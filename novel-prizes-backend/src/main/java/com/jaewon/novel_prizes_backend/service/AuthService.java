package com.jaewon.novel_prizes_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaewon.novel_prizes_backend.dto.auth.AuthResponse;
import com.jaewon.novel_prizes_backend.dto.auth.LoginRequest;
import com.jaewon.novel_prizes_backend.dto.auth.SignupRequest;
import com.jaewon.novel_prizes_backend.dto.auth.TokenRefreshRequest;
import com.jaewon.novel_prizes_backend.entity.User;
import com.jaewon.novel_prizes_backend.entity.UserProfile;
import com.jaewon.novel_prizes_backend.repository.UserRepository;
import com.jaewon.novel_prizes_backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 인증을 처리하는 서비스 클래스
 * 
 * 주요 기능:
 * - 회원가입 및 로그인 처리
 * - JWT 토큰 기반 인증/인가
 * - 토큰 갱신 및 로그아웃
 * - 사용자 계정 관리
 * 
 * 아키텍처 패턴: Service Layer Pattern
 * 보안: Spring Security + JWT
 * 트랜잭션: @Transactional로 데이터 일관성 보장
 * 
 * 포트폴리오 포인트:
 * - 완전한 JWT 인증 시스템 구현
 * - 보안 모범 사례 적용 (비밀번호 해싱, 토큰 관리)
 * - 견고한 예외 처리 및 검증 로직
 * - 확장 가능한 사용자 관리 시스템
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // JWT 토큰 만료 시간 (초 단위) - 1시간
    private static final long ACCESS_TOKEN_EXPIRATION = 3600L;

    /**
     * 새로운 사용자 회원가입 처리
     * 
     * 처리 과정:
     * 1. 이메일 중복 검사
     * 2. 닉네임 중복 검사
     * 3. 비밀번호 일치 검증
     * 4. 비밀번호 해싱
     * 5. 사용자 및 프로필 엔티티 생성
     * 6. 데이터베이스 저장
     * 7. JWT 토큰 생성 및 응답
     * 
     * 보안 고려사항:
     * - BCrypt를 이용한 강력한 비밀번호 해싱
     * - 이메일/닉네임 중복 방지로 데이터 무결성 확보
     * - 트랜잭션으로 원자성 보장
     * 
     * @param signupRequest 회원가입 요청 정보
     * @return 인증 토큰과 사용자 정보가 포함된 응답
     * @throws IllegalArgumentException 유효하지 않은 입력 데이터
     * @throws RuntimeException 회원가입 처리 중 오류
     */
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        log.info("회원가입 요청 시작 - 이메일: {}, 닉네임: {}", 
                signupRequest.getNormalizedEmail(), signupRequest.getNormalizedNickname());

        // 1. 입력 데이터 검증
        validateSignupRequest(signupRequest);

        // 2. 이메일 중복 검사
        String normalizedEmail = signupRequest.getNormalizedEmail();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("이메일 중복 - {}", normalizedEmail);
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. 닉네임 중복 검사
        String normalizedNickname = signupRequest.getNormalizedNickname();
        if (userRepository.existsByNickname(normalizedNickname)) {
            log.warn("닉네임 중복 - {}", normalizedNickname);
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 4. 사용자 엔티티 생성
        User user = createUserFromSignupRequest(signupRequest);
        User savedUser = userRepository.save(user);

        // 5. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        log.info("회원가입 완료 - 사용자 ID: {}, 이메일: {}", savedUser.getId(), savedUser.getEmail());

        // 6. 응답 생성
        return AuthResponse.of(
                accessToken,
                refreshToken,
                ACCESS_TOKEN_EXPIRATION,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getUserType(),
                savedUser.getIsActive(),
                savedUser.getLastLoginAt(),
                savedUser.getCreatedAt()
        );
    }

    /**
     * 사용자 로그인 처리
     * 
     * 동작 원리:
     * 1. 이메일/비밀번호 검증
     * 2. Spring Security AuthenticationManager를 통한 인증
     * 3. JWT Access/Refresh 토큰 생성
     * 4. 마지막 로그인 시간 업데이트
     * 5. 인증 응답 반환
     * 
     * 보안 전략:
     * - Spring Security의 인증 메커니즘 활용
     * - 로그인 실패 시 구체적인 실패 원인 숨김 (보안)
     * - 로그인 시간 기록으로 사용자 활동 추적
     * 
     * @param loginRequest 로그인 요청 정보 (email, password)
     * @return JWT 토큰이 포함된 인증 응답
     * @throws BadCredentialsException 인증 실패 시
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("로그인 요청 - 이메일: {}", loginRequest.getEmail());

        try {
            // 1. Spring Security를 통한 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            findUserIdByEmail(loginRequest.getEmail()),
                            loginRequest.getPassword()
                    )
            );

            // 2. 인증된 사용자 정보 조회
            String userId = authentication.getName();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

            // 3. 계정 활성화 상태 확인
            if (!user.getIsActive()) {
                log.warn("비활성화된 계정 로그인 시도 - 사용자 ID: {}", userId);
                throw new BadCredentialsException("비활성화된 계정입니다. 고객센터에 문의해주세요.");
            }

            // 4. JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(userId);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            // 5. 마지막 로그인 시간 업데이트
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("로그인 성공 - 사용자 ID: {}", userId);

            // 6. 응답 생성
            return AuthResponse.of(
                    accessToken,
                    refreshToken,
                    ACCESS_TOKEN_EXPIRATION,
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getUserType(),
                    user.getIsActive(),
                    user.getLastLoginAt(),
                    user.getCreatedAt()
            );

        } catch (AuthenticationException ex) {
            log.warn("로그인 실패 - 이메일: {}, 원인: {}", loginRequest.getEmail(), ex.getMessage());
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * JWT 토큰 갱신 처리
     * 
     * 처리 과정:
     * 1. Refresh Token 유효성 검증
     * 2. 토큰에서 사용자 정보 추출
     * 3. 사용자 계정 상태 확인
     * 4. 새로운 Access Token 생성
     * 5. 토큰 갱신 응답 반환
     * 
     * 보안 전략:
     * - Refresh Token의 서명 및 만료 시간 검증
     * - 토큰 갱신 시 사용자 계정 상태 재확인
     * - 향후 Rotating Refresh Token 구현 가능
     * 
     * @param tokenRefreshRequest Refresh Token을 포함한 갱신 요청
     * @return 새로운 Access Token이 포함된 응답
     * @throws IllegalArgumentException 유효하지 않은 Refresh Token
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        String refreshToken = tokenRefreshRequest.getRefreshToken();
        
        log.debug("토큰 갱신 요청 - Refresh Token 검증 시작");

        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("유효하지 않은 Refresh Token으로 갱신 시도");
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
        }

        // 2. 토큰에서 사용자 ID 추출
        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            log.warn("Refresh Token에서 사용자 ID 추출 실패");
            throw new IllegalArgumentException("토큰 정보를 읽을 수 없습니다. 다시 로그인해주세요.");
        }

        // 3. 사용자 존재 및 활성화 상태 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.getIsActive()) {
            log.warn("비활성화된 계정의 토큰 갱신 시도 - 사용자 ID: {}", userId);
            throw new IllegalArgumentException("비활성화된 계정입니다. 고객센터에 문의해주세요.");
        }

        // 4. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

        log.debug("토큰 갱신 완료 - 사용자 ID: {}", userId);

        // 5. 토큰 갱신 응답 반환
        return AuthResponse.ofTokenRefresh(newAccessToken, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 회원가입 요청 데이터 검증
     * 
     * 검증 항목:
     * - 비밀번호 일치 여부
     * - 이용약관 동의 여부
     * - 입력 데이터 정규화
     * 
     * @param signupRequest 검증할 회원가입 요청
     * @throws IllegalArgumentException 검증 실패 시
     */
    private void validateSignupRequest(SignupRequest signupRequest) {
        // 비밀번호 일치 검증
        if (!signupRequest.isPasswordMatched()) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 이용약관 동의 검증
        if (!Boolean.TRUE.equals(signupRequest.getTermsAgreed())) {
            throw new IllegalArgumentException("이용약관에 동의해야 합니다.");
        }
    }

    /**
     * 회원가입 요청으로부터 User 엔티티 생성
     * 
     * 생성 과정:
     * 1. UUID 기반 고유 ID 생성
     * 2. 비밀번호 BCrypt 해싱
     * 3. 이메일/닉네임 정규화
     * 4. 기본 사용자 프로필 생성
     * 
     * @param signupRequest 회원가입 요청 정보
     * @return 생성된 User 엔티티 (프로필 포함)
     */
    private User createUserFromSignupRequest(SignupRequest signupRequest) {
        // 1. 고유 ID 생성
        String userId = UUID.randomUUID().toString();

        // 2. 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 3. User 엔티티 생성
        User user = User.builder()
                .id(userId)
                .email(signupRequest.getNormalizedEmail())
                .passwordHash(hashedPassword)
                .nickname(signupRequest.getNormalizedNickname())
                .userType(signupRequest.getUserType())
                .isActive(true)
                .build();

        // 4. 기본 사용자 프로필 생성
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .user(user)
                .build();

        user.setProfile(profile);

        return user;
    }

    /**
     * 이메일로 사용자 ID 조회
     * 
     * 목적:
     * - Spring Security 인증에서 username(사용자 ID) 필요
     * - 클라이언트는 이메일로 로그인, 내부적으로는 ID 사용
     * 
     * @param email 사용자 이메일
     * @return 사용자 고유 ID
     * @throws BadCredentialsException 사용자를 찾을 수 없는 경우
     */
    private String findUserIdByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .map(User::getId)
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));
    }
}