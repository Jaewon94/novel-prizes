package com.jaewon.novel_prizes_backend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaewon.novel_prizes_backend.entity.User;
import com.jaewon.novel_prizes_backend.entity.UserType;
import com.jaewon.novel_prizes_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security에서 사용자 인증 정보를 로드하는 서비스
 * 
 * 역할:
 * - 사용자 ID로 데이터베이스에서 사용자 정보 조회
 * - User 엔티티를 Spring Security의 UserDetails로 변환
 * - 사용자 권한(Role) 설정 및 관리
 * - 계정 상태 확인 (활성화, 잠금, 만료 등)
 * 
 * 아키텍처 패턴: Adapter Pattern (User Entity → UserDetails)
 * 보안 전략: Role-Based Access Control (RBAC)
 * 
 * 포트폴리오 포인트:
 * - Spring Security 표준 인터페이스 구현
 * - 엔티티와 보안 컨텍스트 간의 깔끔한 어댑터 패턴
 * - 유연한 권한 시스템으로 확장성 확보
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 인증 정보를 로드하는 메서드
     * 
     * 처리 과정:
     * 1. 사용자 ID로 데이터베이스에서 User 엔티티 조회
     * 2. User 엔티티를 UserDetails 객체로 변환
     * 3. 사용자 타입에 따른 권한 설정
     * 4. 계정 상태 확인 및 설정
     * 
     * 예외 처리:
     * - 사용자를 찾을 수 없는 경우: UsernameNotFoundException
     * - 계정이 비활성화된 경우: 로그 기록 후 비활성화 상태로 반환
     * 
     * @param userId 사용자 고유 ID
     * @return UserDetails 인증 정보 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.debug("사용자 인증 정보 로드 시작 - 사용자 ID: {}", userId);

        // 1. 데이터베이스에서 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없음 - ID: {}", userId);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        // 2. 계정 상태 확인
        if (!user.getIsActive()) {
            log.warn("비활성화된 계정 접근 시도 - 사용자 ID: {}", userId);
        }

        // 3. UserDetails 객체 생성 및 반환
        UserDetails userDetails = createUserDetails(user);
        
        log.debug("사용자 인증 정보 로드 완료 - 사용자 ID: {}, 권한: {}", 
                userId, userDetails.getAuthorities());
        
        return userDetails;
    }

    /**
     * User 엔티티를 Spring Security UserDetails로 변환
     * 
     * UserDetails 구성 요소:
     * - username: 사용자 고유 ID
     * - password: 암호화된 비밀번호
     * - authorities: 사용자 권한 목록
     * - enabled: 계정 활성화 상태
     * - accountNonExpired: 계정 만료 여부 (현재는 항상 true)
     * - accountNonLocked: 계정 잠금 여부 (현재는 항상 true)
     * - credentialsNonExpired: 비밀번호 만료 여부 (현재는 항상 true)
     * 
     * @param user 데이터베이스에서 조회한 사용자 엔티티
     * @return UserDetails 객체
     */
    private UserDetails createUserDetails(User user) {
        // 사용자 타입에 따른 권한 설정
        Collection<GrantedAuthority> authorities = mapUserTypeToAuthorities(user.getUserType());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId()) // 사용자 ID를 username으로 사용
                .password(user.getPasswordHash()) // 암호화된 비밀번호
                .authorities(authorities) // 권한 목록
                .accountExpired(false) // 계정 만료 여부
                .accountLocked(false) // 계정 잠금 여부
                .credentialsExpired(false) // 비밀번호 만료 여부
                .disabled(!user.getIsActive()) // 계정 비활성화 여부
                .build();
    }

    /**
     * 사용자 타입을 Spring Security 권한으로 매핑
     * 
     * 권한 체계:
     * - READER: 일반 독자 권한 (소설 조회, 리뷰 작성 등)
     * - AUTHOR: 작가 권한 (READER 권한 + 소설 등록/수정/삭제)
     * - ADMIN: 관리자 권한 (모든 권한 + 시스템 관리)
     * 
     * 권한 명명 규칙:
     * - ROLE_ 접두사 사용 (Spring Security 표준)
     * - 대문자로 표기
     * 
     * 확장성 고려사항:
     * - 새로운 사용자 타입 추가 시 이 메서드만 수정
     * - 세분화된 권한 체계로 확장 가능 (예: ROLE_PREMIUM_READER)
     * 
     * @param userType 사용자 타입 enum
     * @return Spring Security 권한 컬렉션
     */
    private Collection<GrantedAuthority> mapUserTypeToAuthorities(UserType userType) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        switch (userType) {
            case READER:
                // 독자 권한: 기본 읽기 권한
                authorities.add(new SimpleGrantedAuthority("ROLE_READER"));
                break;
                
            case AUTHOR:
                // 작가 권한: 독자 권한 + 작가 권한
                authorities.add(new SimpleGrantedAuthority("ROLE_READER"));
                authorities.add(new SimpleGrantedAuthority("ROLE_AUTHOR"));
                break;
                
            default:
                // 기본값: 독자 권한
                log.warn("알 수 없는 사용자 타입, 기본 권한 적용: {}", userType);
                authorities.add(new SimpleGrantedAuthority("ROLE_READER"));
                break;
        }

        // 향후 관리자 권한 추가 시
        // if (user.isAdmin()) {
        //     authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // }

        return authorities;
    }

    /**
     * 사용자 권한 업데이트 (향후 확장용)
     * 
     * 사용 예시:
     * - 사용자가 작가로 승급할 때
     * - 관리자 권한을 부여할 때
     * - 특별 권한을 임시로 부여할 때
     * 
     * @param userId 사용자 ID
     * @param newUserType 새로운 사용자 타입
     */
    @Transactional
    public void updateUserAuthorities(String userId, UserType newUserType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        
        UserType oldUserType = user.getUserType();
        user.setUserType(newUserType);
        userRepository.save(user);
        
        log.info("사용자 권한 업데이트 완료 - ID: {}, {} → {}", 
                userId, oldUserType, newUserType);
    }
}