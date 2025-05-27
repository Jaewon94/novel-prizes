# novel-prizes-backend

국내 주요 소설 플랫폼(카카오페이지, 네이버시리즈, 리디북스, 문피아, 조아라 등)의 인기/신작/완결 랭킹을 통합 제공하고, 자체 무료 연재 공간 및 타 플랫폼 유료 데뷔 연계를 지원하는 백엔드 시스템입니다.

---

## 🚀 프로젝트 개요

- **주요 기능**
  - 여러 소설 플랫폼의 인기/신작/완결 랭킹 통합 제공 (메타 랭킹)
  - 작품 상세, 검색, 트렌드, 작가 관리, 커뮤니티 등 API 제공
  - 자체 무료 연재 공간 및 타 플랫폼 유료 데뷔 연계 지원

- **기술 스택**
  - Java 17, Spring Boot 3.5.x, Gradle(Groovy), MySQL 8.x, Redis 7.x
  - RESTful API, JWT 인증, JPA, Redis, Flyway, GraphQL(선택), Docker

---

## 🛠️ 개발 환경 및 실행 방법

### 1. 필수 환경
- Java 17 이상
- Gradle 8.x
- MySQL 8.x
- Redis 7.x
- (선택) Docker, Docker Compose

### 2. 프로젝트 실행

#### 1) 로컬 개발 환경
```bash
# 환경 변수 또는 application-dev.yml에 DB/Redis 정보 입력
./gradlew build
java -jar build/libs/novel-prizes-backend.jar --spring.profiles.active=dev
```

#### 2) Docker 환경
```bash
docker-compose up --build
```

#### 3) 주요 환경설정 파일
- src/main/resources/application.yml (공통)
- src/main/resources/application-dev.yml (개발)
- src/main/resources/application-prod.yml (운영)

---

## 📁 폴더 구조 예시

```
novel-prizes-backend/
├── src/
│   ├── main/
│   │   ├── java/com/jaewon/novelprizesbackend/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── util/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/   # Flyway 마이그레이션 스크립트
│   └── test/java/com/jaewon/novelprizesbackend/
├── build.gradle
├── Dockerfile
├── docker-compose.yml
├── README.md
└── ...
```

---

## 🔑 주요 라이브러리
- Spring Web, Spring Data JPA, MySQL Driver, Spring Data Redis, Spring Security, JWT, Lombok, Swagger(SpringDoc), Flyway, (선택) GraphQL

---

## 📚 참고 문서
- [@2-2-1 System Architecture Diagram.md](../02.%20design/02.%20System%20Architecture/2-2-1%20System%20Architecture%20Diagram.md)
- [@2-2-2 API Specification.md](../02.%20design/02.%20System%20Architecture/2-2-2%20API%20Specification.md)
- [@2-2-3 Database Schema.md](../02.%20design/02.%20System%20Architecture/2-2-3%20Database%20Schema.md)
- [@backend-guide.md](./backend-guide.md)

---

## 📝 기타
- 운영 환경의 민감 정보(DB 비밀번호 등)는 환경 변수 또는 Secret Manager로 관리 권장
- 테스트 코드는 src/test/java/ 이하에 작성
- 커밋/협업/배포 등 실무 가이드는 [backend-guide.md](./backend-guide.md) 참고
