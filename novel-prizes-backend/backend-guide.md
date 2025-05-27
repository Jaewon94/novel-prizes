# 1. 개발 환경 및 프레임워크/라이브러리 버전

## 1) 개발 환경 및 빌드 도구
- **Java**: 17 (LTS)
- **Gradle**: 8.x (Groovy)
- **Spring Boot**: 3.5.x
- **Packaging**: Jar
- **MySQL**: 8.x
- **Redis**: 7.x

## 3) 주요 라이브러리 및 의존성 (Gradle 기준)

- **Spring Web**
  - RESTful API 개발, HTTP 요청/응답 처리
  - Gradle: implementation 'org.springframework.boot:spring-boot-starter-web'

- **Spring Data JPA**
  - ORM 기반 DB 연동, Repository 패턴 지원
  - Gradle: implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

- **MySQL Driver**
  - MySQL 데이터베이스 연결용 JDBC 드라이버
  - Gradle: runtimeOnly 'com.mysql:mysql-connector-j:8.0.33'

- **Spring Data Redis**
  - Redis 캐시/세션 연동
  - Gradle: implementation 'org.springframework.boot:spring-boot-starter-data-redis'

- **Spring Security**
  - 인증/인가, 보안 기능
  - Gradle: implementation 'org.springframework.boot:spring-boot-starter-security'

- **JWT (JSON Web Token)**
  - 토큰 기반 인증/인가 구현
  - Gradle:
    - implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    - runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    - runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

- **Lombok**
  - Getter/Setter/생성자 등 코드 자동 생성
  - Gradle:
    - compileOnly 'org.projectlombok:lombok:1.18.30'
    - annotationProcessor 'org.projectlombok:lombok:1.18.30'

- **Swagger (SpringDoc OpenAPI)**
  - REST API 문서 자동화
  - Gradle: implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'

- **Flyway (DB 마이그레이션)**
  - DB 스키마 버전 관리 및 마이그레이션
  - Gradle: implementation 'org.flywaydb:flyway-mysql:9.22.3'

- **테스트**
  - 단위/통합 테스트, Mock 지원
  - Gradle:
    - testImplementation 'org.springframework.boot:spring-boot-starter-test'
    - testImplementation 'org.mockito:mockito-core:5.2.0'

- **Spring GraphQL (선택)**
  - GraphQL API 지원
  - Gradle: implementation 'org.springframework.boot:spring-boot-starter-graphql:1.2.5'

> 버전은 프로젝트 상황에 따라 최신 LTS로 조정 가능. 위 예시는 2024년 기준 최신 버전 예시임.

---

# 2. 백엔드 개발 전체 진행 순서 (실무 예시)

## 1) 프로젝트 초기 세팅
- Spring Initializr(https://start.spring.io)에서 위 버전과 의존성 선택 후 프로젝트 생성
- Git 저장소 초기화 및 .gitignore, README.md 작성
- build.gradle 또는 pom.xml에 의존성 추가/확인
- src/main/resources에 application.yml 생성, DB/Redis 등 환경 변수 설정

## 2) 패키지 구조 설계
- 예시: com.novelprizes.{controller, service, repository, entity, dto, config, security, util}

## 3) DB 스키마/엔티티 설계
- @2-2-3 Database Schema.md 참고하여 Entity(@Entity), Repository(JpaRepository) 구현
- Flyway/Liquibase로 DB 마이그레이션 관리 (src/main/resources/db/migration)

## 4) 인증/인가 시스템 구축
- Spring Security 설정 (SecurityConfig)
- JWT 토큰 발급/검증(JwtTokenProvider 등) 구현
- UserDetailsService, PasswordEncoder 등 구현
- 회원가입/로그인 API (POST /auth/signup, /auth/login) 개발

## 5) 핵심 API 개발 (MVP 1단계)
- 작품 API: GET /novels, GET /novels/{id} (조회)
- 파일 업로드 API: POST /upload/cover (표지 이미지 업로드)
- 작가/챕터 API: POST/PUT/DELETE /novels, /novels/{id}/chapters (등록/수정/삭제)
- DTO, Service, Controller 계층 분리 구현

## 6) 크롤러 데이터 연동
- 크롤러(Python/n8n)가 MySQL/Redis에 저장한 데이터와 API 연동
- DB 스키마와 데이터 포맷 일치 확인

## 7) 검색/추천 API 개발
- 검색: MySQL LIKE/Full-Text, 추후 Elasticsearch 등 확장
- 추천: viewCount, rating 등 단순 로직부터 시작

## 8) GraphQL API (선택)
- spring-boot-starter-graphql 추가, *.graphqls 스키마 정의, 리졸버 구현

## 9) 테스트/리팩토링
- JUnit/Mockito로 단위/통합 테스트 작성
- 코드 리팩토링 및 문서화

## 10) 로깅/모니터링/예외처리
- SLF4J/Logback, Spring Boot Actuator, @ControllerAdvice로 전역 예외 처리

## 11) 문서화
- Swagger(SpringDoc)로 REST API 문서 자동화
- GraphQL: GraphiQL 등 도구 활용

## 12) 배포 및 운영
- Dockerfile, docker-compose.yml 작성 (MySQL, Redis, Spring Boot 서비스 컨테이너화)
- CI/CD: GitHub Actions 등으로 자동 빌드/배포
- 운영 환경 application-prod.yml 별도 관리
- 모니터링/백업/보안 정책 적용

---

# 3. GitHub 커밋 가이드

- **초기 세팅/구조 변경/주요 기능 단위로 커밋**
  - 예시: "프로젝트 초기 세팅", "User 엔티티/리포지토리/서비스 구현", "JWT 인증 로직 추가"
- **작업 단위(기능/버그/리팩토링 등)별로 자주 커밋**
  - 1~2시간 이상 작업 시 중간 저장 커밋 권장
- **테스트 통과/빌드 성공 시점에 커밋**
- **PR(풀리퀘스트) 전에는 반드시 커밋/푸시**
- **커밋 메시지는 명확하게(무엇을/왜/어떻게)**
  - 예시: "작품 상세 조회 API 구현 (GET /novels/{id})"

---

# 4. application.yml 개발/운영 환경 분리

- `src/main/resources/application.yml` : 공통 설정
- `src/main/resources/application-dev.yml` : 개발 환경 전용 (DB, Redis, 로컬 경로 등)
- `src/main/resources/application-prod.yml` : 운영 환경 전용 (운영 DB, 운영 Redis, 외부 연동 등)
- Spring Boot 실행 시 `--spring.profiles.active=dev` 또는 `prod`로 환경 선택
- 예시:
  ```yaml
  # application.yml
  spring:
    profiles:
      active: dev
  ---
  # application-dev.yml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/novel_dev
      username: devuser
      password: devpass
    redis:
      host: localhost
      port: 6379
  ---
  # application-prod.yml
  spring:
    datasource:
      url: jdbc:mysql://prod-db:3306/novel_prod
      username: produser
      password: prodpass
    redis:
      host: prod-redis
      port: 6379
  ```

---

# 5. Docker 및 배포 환경

- **Dockerfile**: Spring Boot 애플리케이션 빌드/실행용
  ```dockerfile
  FROM eclipse-temurin:17-jdk-alpine
  WORKDIR /app
  COPY build/libs/novel-prizes-backend.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```
- **docker-compose.yml**: MySQL, Redis, Spring Boot 서비스 통합 실행
  ```yaml
  version: '3.8'
  services:
    db:
      image: mysql:8.0
      environment:
        MYSQL_ROOT_PASSWORD: rootpass
        MYSQL_DATABASE: novel_prizes
        MYSQL_USER: appuser
        MYSQL_PASSWORD: apppass
      ports:
        - "3306:3306"
      volumes:
        - db_data:/var/lib/mysql
    redis:
      image: redis:7
      ports:
        - "6379:6379"
    backend:
      build: .
      image: novel-prizes-backend:latest
      depends_on:
        - db
        - redis
      environment:
        SPRING_PROFILES_ACTIVE: prod
      ports:
        - "8080:8080"
  volumes:
    db_data:
  ```
- **운영/개발 환경 분리**: docker-compose.override.yml 등으로 개발/운영 환경별 설정 분리 가능
- **CI/CD**: GitHub Actions 등으로 빌드/테스트/배포 자동화

---

# 6. 실무 팁 및 참고
- 모든 단계에서 반드시 @2-2-1 System Architecture Diagram.md, @2-2-2 API Specification.md, @2-2-3 Database Schema.md 문서를 참고
- 작은 단위로 개발-테스트-개선 반복 (이터레이션)
- 테스트 코드와 함께 개발, Git으로 코드 이력 관리
- 문서와 실제 코드/설계 동기화

---

# 7. 실무 예시 폴더 구조 트리

아래는 Spring Boot 기반 백엔드 프로젝트의 권장 폴더 구조 예시입니다.

```
novel-prizes-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── novelprizes/
│   │   │           ├── NovelPrizesBackendApplication.java
│   │   │           ├── config/
│   │   │           ├── controller/
│   │   │           ├── dto/
│   │   │           ├── entity/
│   │   │           ├── repository/
│   │   │           ├── security/
│   │   │           ├── service/
│   │   │           └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/   # Flyway 마이그레이션 스크립트
│   │       └── static/          # 정적 리소스(필요시)
│   └── test/
│       └── java/
│           └── com/novelprizes/ # 테스트 코드
├── build.gradle
├── Dockerfile
├── docker-compose.yml
├── README.md
└── ...
```

---

# 8. 실무 팁 및 참고 (보완)
- 모든 단계에서 반드시 @2-2-1 System Architecture Diagram.md, @2-2-2 API Specification.md, @2-2-3 Database Schema.md 문서를 참고
- 작은 단위로 개발-테스트-개선 반복 (이터레이션)
- 테스트 코드와 함께 개발, Git으로 코드 이력 관리
- 문서와 실제 코드/설계 동기화
- **application-prod.yml 등 운영 환경의 민감 정보(DB 비밀번호 등)는 환경 변수 또는 Secret Manager로 관리 권장**
- **테스트 전략: 단위(Unit), 통합(Integration), 인수(UAT), 성능(Performance) 테스트를 단계별로 작성하고, 테스트 커버리지 목표(예: 80% 이상)도 설정**

---

이 가이드와 위 3개 문서를 기반으로 백엔드 개발을 진행하면, 실무에서도 안정적이고 확장성 있는 시스템을 구축할 수 있습니다.