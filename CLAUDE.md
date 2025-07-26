# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고할 가이드를 제공합니다.

## 📋 포트폴리오 프로젝트 가이드라인

**이 프로젝트는 포트폴리오용으로 개발되므로 다음 원칙을 반드시 준수해주세요:**

### 코드 작성 원칙
1. **상세한 주석 작성**: 모든 클래스, 메서드, 복잡한 로직에 한국어 주석 작성
2. **동작 원리 설명**: 코드가 어떻게 작동하는지 이해하기 쉽게 설명
3. **아키텍처 패턴 명시**: 사용된 디자인 패턴이나 아키텍처 패턴을 주석으로 설명
4. **예외 처리 설명**: 예외 상황과 처리 방법을 명확히 주석으로 기록

### 문서화 요구사항
1. **실시간 문서 업데이트**: 코드 변경 시 관련 문서(.md 파일들) 즉시 업데이트
2. **API 문서 동기화**: 새로운 API 추가 시 `API Specification.md` 업데이트
3. **DB 스키마 동기화**: 엔티티 변경 시 `Database Schema.md` 업데이트
4. **아키텍처 다이어그램 동기화**: 구조 변경 시 관련 다이어그램 업데이트

### 주석 스타일 가이드
```java
/**
 * 사용자 인증을 처리하는 서비스 클래스
 * 
 * 주요 기능:
 * - JWT 토큰 기반 인증/인가
 * - OAuth 소셜 로그인 연동
 * - 사용자 세션 관리
 * 
 * 아키텍처 패턴: Service Layer Pattern
 * 보안: Spring Security + JWT
 */
@Service
public class AuthService {
    
    /**
     * 사용자 로그인 처리
     * 
     * 동작 원리:
     * 1. 이메일/비밀번호 검증
     * 2. JWT Access/Refresh 토큰 생성
     * 3. Redis에 Refresh 토큰 저장 (보안)
     * 4. 로그인 시간 업데이트
     * 
     * @param loginRequest 로그인 요청 정보 (email, password)
     * @return JWT 토큰이 포함된 인증 응답
     * @throws AuthenticationException 인증 실패 시
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // 구현 내용...
    }
}
```

## 전체 아키텍처

국내 주요 소설 플랫폼(카카오페이지, 네이버시리즈, 리디북스, 문피아, 조아라 등)의 랭킹을 통합하고, 신인 작가를 위한 무료 연재 공간을 제공하는 소설 플랫폼 중계 서비스입니다.

### 프로젝트 구조
- **01. docs/** - 요구사항 및 프로젝트 기획 문서
- **02. design/** - UI/UX 와이어프레임, 시스템 아키텍처, API 명세, 데이터베이스 스키마
- **novel-prizes-backend/** - Spring Boot 백엔드 애플리케이션

### 기술 스택
- **백엔드**: Java 17, Spring Boot 3.3.x, Spring Data JPA, Spring Security
- **데이터베이스**: MySQL 8.x (Flyway 마이그레이션), Redis (캐싱)
- **인증**: JWT 토큰
- **API 문서화**: SpringDoc OpenAPI (Swagger)
- **빌드 도구**: Gradle (Groovy DSL)
- **아키텍처**: QueryDSL (타입 안전 쿼리), JSON 컨버터 (복합 데이터 타입)

## Cursor 스타일 변경 검토 도구

### diff 검토 워크플로우
```bash
# 1. 변경 전 백업 및 준비
./.scripts/preview-changes.sh

# 2. Claude가 변경사항 적용 후 diff 확인
./.scripts/show-diff.sh

# 3. 변경사항 승인 또는 거부
./.scripts/apply-changes.sh    # 승인 시
./.scripts/reject-changes.sh   # 거부 시
```

## 주요 개발 명령어

### 백엔드 개발 (novel-prizes-backend/)
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행 (개발 환경)
./gradlew bootRun
# 또는 특정 프로필로 실행
java -jar build/libs/novel-prizes-backend.jar --spring.profiles.active=dev

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build

# QueryDSL Q-클래스 생성
./gradlew compileJava
```

### 환경 관리
- **개발 환경**: `--spring.profiles.active=dev` (기본값)
- **운영 환경**: `--spring.profiles.active=prod`
- 설정 파일: `application.yml`, `application-dev.yml`, `application-prod.yml`

### 데이터베이스 작업
- Flyway 마이그레이션 파일: `src/main/resources/db/migration/`
- QueryDSL 생성 클래스: `build/generated/querydsl/`

## 코드 아키텍처 패턴

### 엔티티 설계
- Lombok 어노테이션 사용 (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`)
- 복합 데이터 타입을 위한 커스텀 JSON 컨버터 (`JsonMapConverter` 참조)
- 무료 소설(`FreeNovel`, `FreeChapter`)과 플랫폼 소설(`Novel`, `Chapter`) 분리
- 작가 정보는 별도 `Author` 엔티티로 관리
- 플랫폼별 데이터는 `PlatformListing` 엔티티에 저장

### 패키지 구조
```
com.jaewon.novel_prizes_backend/
├── config/          # Spring 설정
├── controller/      # REST API 엔드포인트
├── converter/       # 커스텀 JPA 컨버터
├── dto/            # 데이터 전송 객체
├── entity/         # JPA 엔티티
├── repository/     # JPA 리포지토리 (QueryDSL 지원)
├── security/       # 보안 설정
├── service/        # 비즈니스 로직
└── util/           # 유틸리티 클래스
```

### 주요 아키텍처 결정사항
- **이중 소설 시스템**: 플랫폼 소설(크롤링 데이터)과 무료 소설(자체 연재) 분리 처리
- **플랫폼 추상화**: `PlatformListing` 엔티티로 플랫폼별 소설 데이터 관리
- **랭킹 시스템**: 랭킹 타입과 기간별로 분리된 엔티티 구조
- **사용자 관리**: `User`와 `UserProfile` 엔티티 분리, 다양한 사용자 타입 지원
- **통계 추적**: 작가 및 소설 통계를 위한 전용 엔티티

### 테스트 전략
- 테스트 파일 위치: `src/test/java/`
- Spring Boot Test 프레임워크와 JUnit 사용
- Spring Security Test로 보안 테스트
- GraphQL 테스트 기능 포함

### 배포 및 운영
- **개발 환경**: Docker 컨테이너화를 통한 로컬 개발 및 테스트
- **운영 환경**: AWS ECS (Elastic Container Service) 사용 예정
- **컨테이너화**: 프로젝트 루트의 Dockerfile 사용
- **환경 분리**: dev/prod 프로필로 환경별 설정 관리

### Docker 명령어
```bash
# Docker 이미지 빌드
docker build -t novel-prizes-backend .

# Docker 컨테이너 실행
docker run -p 8080:8080 novel-prizes-backend

# Docker Compose 사용 (MySQL, Redis 포함)
docker-compose up -d
docker-compose down
```

### 중요 참고사항
- QueryDSL Q-클래스는 컴파일 시 생성되며 커밋하지 않음
- 환경별 설정은 Spring 프로필 사용
- API 보안을 위해 JWT 인증 구현
- 데이터베이스 스키마 변경은 Flyway 마이그레이션 사용
- REST API와 GraphQL 엔드포인트 모두 지원