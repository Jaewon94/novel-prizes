package com.jaewon.novel_prizes_backend.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 생성, 검증, 파싱을 담당하는 컴포넌트
 * 
 * 주요 기능:
 * - Access Token 및 Refresh Token 생성
 * - JWT 토큰 서명 및 검증
 * - 토큰에서 사용자 정보 추출
 * - 토큰 만료 시간 검증
 * 
 * 보안 전략:
 * - HMAC-SHA512 알고리즘 사용으로 강력한 서명 보안
 * - 짧은 Access Token 수명 (1시간) + 긴 Refresh Token 수명 (30일)
 * - Base64 인코딩된 비밀키 사용
 * 
 * 아키텍처 패턴: Provider Pattern
 * 
 * 포트폴리오 포인트:
 * - 업계 표준 JWT 라이브러리 활용 (JJWT)
 * - 토큰 만료 전략을 통한 보안과 사용성의 균형
 * - 상세한 예외 처리로 다양한 토큰 오류 상황 대응
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    
    // Access Token 유효 시간 (1시간)
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;
    
    // Refresh Token 유효 시간 (30일)
    @Value("${jwt.refresh-token-expiration:2592000000}")
    private long refreshTokenExpiration;

    /**
     * JWT 토큰 제공자 생성자
     * 
     * 초기화 과정:
     * 1. application.yml에서 비밀키 로드
     * 2. Base64 디코딩을 통해 바이트 배열로 변환
     * 3. HMAC-SHA 알고리즘용 Key 객체 생성
     * 
     * 보안 고려사항:
     * - 비밀키는 최소 512비트(64바이트) 이상 권장
     * - 운영 환경에서는 환경 변수나 보안 저장소 사용
     * 
     * @param jwtSecret Base64 인코딩된 JWT 비밀키
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     * 
     * 토큰 구성:
     * - Header: 알고리즘 타입 (HS512)
     * - Payload: 사용자 ID, 발급 시간, 만료 시간
     * - Signature: HMAC-SHA512 서명
     * 
     * Payload 클레임:
     * - sub (Subject): 사용자 ID
     * - iat (Issued At): 토큰 발급 시간
     * - exp (Expiration): 토큰 만료 시간
     * 
     * @param userId 사용자 고유 ID
     * @return 생성된 JWT Access Token
     */
    public String generateAccessToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        log.debug("Access Token 생성 완료 - 사용자: {}, 만료시간: {}", userId, expiryDate);
        return token;
    }

    /**
     * Refresh Token 생성
     * 
     * Access Token과의 차이점:
     * - 더 긴 만료 시간 (30일)
     * - 토큰 갱신 용도로만 사용
     * - Redis 등 별도 저장소에서 관리 권장
     * 
     * @param userId 사용자 고유 ID
     * @return 생성된 JWT Refresh Token
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        log.debug("Refresh Token 생성 완료 - 사용자: {}, 만료시간: {}", userId, expiryDate);
        return token;
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     * 
     * 추출 과정:
     * 1. JWT 토큰 파싱 및 서명 검증
     * 2. Claims에서 Subject(사용자 ID) 추출
     * 3. 예외 발생 시 null 반환
     * 
     * @param token JWT 토큰
     * @return 사용자 ID (추출 실패 시 null)
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getSubject();
        } catch (Exception ex) {
            log.warn("토큰에서 사용자 ID 추출 실패: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * JWT 토큰 유효성 검증
     * 
     * 검증 항목:
     * 1. 토큰 형식 유효성 (구조, 인코딩)
     * 2. 서명 검증 (변조 여부 확인)
     * 3. 만료 시간 검증
     * 4. 지원되는 알고리즘 여부
     * 
     * @param token 검증할 JWT 토큰
     * @return true: 유효한 토큰, false: 무효한 토큰
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
            
        } catch (SecurityException | MalformedJwtException ex) {
            log.warn("잘못된 JWT 서명: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("만료된 JWT 토큰: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("지원되지 않는 JWT 토큰: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT 토큰이 비어있음: {}", ex.getMessage());
        } catch (Exception ex) {
            log.error("JWT 토큰 검증 중 예외 발생: {}", ex.getMessage());
        }
        
        return false;
    }

    /**
     * JWT 토큰과 사용자 정보 일치성 검증
     * 
     * 검증 과정:
     * 1. 기본 토큰 유효성 검증
     * 2. 토큰의 사용자 ID와 UserDetails의 사용자명 일치 확인
     * 3. 토큰 만료 시간 재확인
     * 
     * 이중 검증을 통한 보안 강화:
     * - 토큰 자체의 유효성
     * - 토큰과 현재 사용자 정보의 일치성
     * 
     * @param token JWT 토큰
     * @param userDetails 사용자 세부 정보
     * @return true: 토큰과 사용자 정보 일치, false: 불일치
     */
    public boolean validateTokenWithUserDetails(String token, UserDetails userDetails) {
        try {
            // 1. 기본 토큰 유효성 검증
            if (!validateToken(token)) {
                return false;
            }

            // 2. 토큰에서 사용자 ID 추출
            String userIdFromToken = getUserIdFromToken(token);
            
            // 3. 사용자 정보 일치성 확인
            boolean isUserMatched = userIdFromToken != null && 
                                   userIdFromToken.equals(userDetails.getUsername());
            
            // 4. 토큰 만료 시간 재확인
            boolean isTokenNotExpired = !isTokenExpired(token);
            
            return isUserMatched && isTokenNotExpired;
            
        } catch (Exception ex) {
            log.warn("토큰-사용자 정보 검증 실패: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰 만료 여부 확인
     * 
     * @param token JWT 토큰
     * @return true: 만료됨, false: 유효함
     */
    private boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
            
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     * 
     * @param token JWT 토큰
     * @return 토큰 만료 시간 (추출 실패 시 null)
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.getExpiration();
        } catch (Exception ex) {
            log.warn("토큰에서 만료 시간 추출 실패: {}", ex.getMessage());
            return null;
        }
    }
}