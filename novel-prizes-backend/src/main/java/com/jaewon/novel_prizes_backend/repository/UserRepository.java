package com.jaewon.novel_prizes_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jaewon.novel_prizes_backend.entity.User;

/**
 * 사용자 엔티티에 대한 데이터 접근 리포지토리
 * 
 * 주요 기능:
 * - 기본 CRUD 연산 (JpaRepository 상속)
 * - 이메일 기반 사용자 조회
 * - 닉네임 기반 사용자 조회
 * - 중복 검사를 위한 존재 여부 확인
 * - 사용자 상태 관련 쿼리
 * 
 * 아키텍처 패턴: Repository Pattern
 * 데이터 접근: Spring Data JPA
 * 
 * 포트폴리오 포인트:
 * - Spring Data JPA의 효율적 활용
 * - 명명 규칙을 통한 자동 쿼리 생성
 * - 성능을 고려한 인덱스 활용 쿼리
 * - 비즈니스 로직에 특화된 쿼리 메서드
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 이메일로 사용자 조회
     * 
     * 용도:
     * - 로그인 시 사용자 인증
     * - 이메일 중복 검사
     * - 비밀번호 재설정 등
     * 
     * 인덱스 활용:
     * - email 컬럼에 UNIQUE 인덱스 적용으로 빠른 조회
     * - 대소문자 구분 없는 검색 지원
     * 
     * @param email 사용자 이메일 주소
     * @return 해당 이메일의 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     * 
     * 용도:
     * - 닉네임 중복 검사
     * - 사용자 프로필 검색
     * - 작가명 기반 검색
     * 
     * @param nickname 사용자 닉네임
     * @return 해당 닉네임의 사용자 정보 (Optional)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일 존재 여부 확인
     * 
     * 용도:
     * - 회원가입 시 이메일 중복 검사
     * - 빠른 중복 검증 (exists 쿼리 사용)
     * 
     * 성능 최적화:
     * - COUNT 대신 EXISTS 사용으로 성능 향상
     * - 첫 번째 매치에서 즉시 반환
     * 
     * @param email 검사할 이메일 주소
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     * 
     * 용도:
     * - 회원가입 시 닉네임 중복 검사
     * - 닉네임 변경 시 중복 검증
     * 
     * @param nickname 검사할 닉네임
     * @return true: 존재함, false: 존재하지 않음
     */
    boolean existsByNickname(String nickname);

    /**
     * 활성화된 사용자만 이메일로 조회
     * 
     * 용도:
     * - 로그인 시 비활성화 계정 제외
     * - 활성 사용자 대상 서비스 제공
     * 
     * @param email 사용자 이메일
     * @return 활성화된 사용자 정보 (Optional)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * 특정 기간 내 가입한 사용자 수 조회 (통계용)
     * 
     * 용도:
     * - 관리자 대시보드 통계
     * - 회원 증가 추이 분석
     * - 마케팅 효과 측정
     * 
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 내 가입한 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 마지막 로그인이 특정 기간 이전인 비활성 사용자 조회
     * 
     * 용도:
     * - 장기 미접속 사용자 관리
     * - 리텐션 분석
     * - 재참여 마케팅 대상 선별
     * 
     * @param cutoffDate 기준 날짜 (이 날짜 이전 로그인 사용자)
     * @return 장기 미접속 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate AND u.isActive = true")
    java.util.List<User> findInactiveUsersSince(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
} 