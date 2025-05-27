# 데이터베이스 스키마

## 1. 개요
본 문서는 "소설 플랫폼 통합 중계 서비스"의 데이터베이스 스키마를 정의합니다. MySQL을 기준으로 작성되었으며, 각 테이블의 구조와 관계를 상세히 기술합니다.

## 2. 설계 원칙

### 2.1 확장성 고려
- UUID 사용으로 분산 시스템 지원
  - 분산 환경에서의 고유성 보장
  - 마이그레이션 및 샤딩 용이
  - 보안성 강화
- 파티셔닝 전략으로 대용량 데이터 처리
  - 통계 테이블: date 기준 파티셔닝
  - 로그 테이블: created_at 기준 파티셔닝
  - 작품 테이블: platform 기준 샤딩
- JSON 타입으로 유연한 데이터 구조
  - 소셜 링크, 선호도 등 가변 데이터 저장
  - 스키마 변경 없이 필드 추가 가능
  - 플랫폼별 메타데이터 유연성 확보

### 2.2 성능 최적화
- 적절한 인덱스 설계
  - 단일 컬럼 인덱스: 주요 검색 조건
  - 복합 인덱스: 자주 사용되는 조회 패턴
  - 커버링 인덱스: 자주 조회되는 컬럼 그룹
- 파티셔닝으로 대용량 데이터 관리
  - 통계 데이터: 일별 파티셔닝
  - 로그 데이터: 월별 파티셔닝
  - 작품 데이터: 플랫폼별 샤딩
- 복합 인덱스로 조회 성능 향상
  - 랭킹 조회: (platform, category, period, rank)
  - 통계 조회: (novel_id, date)
  - 검색 로그: (user_id, created_at)

### 2.3 데이터 무결성
- 외래 키 제약으로 참조 무결성 보장
  - CASCADE: 부모 삭제 시 자식도 삭제 (챕터, 통계)
  - SET NULL: 부모 삭제 시 자식은 유지 (무료 작품)
  - RESTRICT: 참조 중인 부모 삭제 방지
- ENUM 타입으로 상태값 제한
  - 작품 상태: NEW, ONGOING, HIATUS, COMPLETED
  - 랭킹 기간: DAILY, WEEKLY, MONTHLY
  - 사용자 타입: READER, AUTHOR
- UNIQUE 제약으로 중복 방지
  - 이메일 중복 방지
  - 플랫폼별 작품 URL 중복 방지
  - 카테고리 매핑 중복 방지

### 2.4 유연성 확보
- 외부 작가 지원
  - authors 테이블의 user_id NULL 허용
  - 외부 작가 정보 독립적 관리
  - 유료화 전환 시 내부 작가로 전환 가능
- 다중 플랫폼 연재
  - platform_listings 테이블로 연재 정보 관리
  - is_primary 플래그로 주요 플랫폼 지정
  - 플랫폼별 URL 및 상태 관리
- 플랫폼별 카테고리 매핑
  - category_mappings 테이블로 통합 관리
  - 플랫폼별 카테고리 유연성 보장
  - 표준 장르 체계 유지

### 2.5 데이터 정규화
- 3NF 준수
  - 중복 데이터 제거
  - 함수적 종속성 제거
  - 이행적 종속성 제거
- 선택적 비정규화
  - 통계 테이블: 조회 성능을 위한 비정규화
  - 검색 로그: 분석 용이성을 위한 비정규화
  - 랭킹 정보: 실시간 조회를 위한 비정규화

### 2.6 데이터 타입 및 제약조건
- UUID: 분산 시스템 지원
- TIMESTAMP: 시간 정보 정확성
- DECIMAL: 금액 데이터 정확성
- MEDIUMTEXT: 대용량 콘텐츠 저장
- JSON: 유연한 메타데이터 저장
- ENUM: 상태값 제한
- NOT NULL: 필수 데이터 보장
- UNIQUE: 중복 방지
- FOREIGN KEY: 참조 무결성 보장

## 3. 테이블 구조

### 3.1 사용자 관련 테이블

#### users
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    user_type ENUM('READER', 'AUTHOR') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type)
);
```

**설계 의도**: 서비스의 기본 사용자 정보를 관리
- UUID 사용: 보안성과 분산 시스템에서의 고유성 보장
- 이메일 UNIQUE: 계정 식별의 기본 키로 사용
- user_type: 독자/작가 구분으로 권한 관리
- last_login_at: 사용자 활동 모니터링
- is_active: 계정 상태 관리

#### user_profiles
```sql
CREATE TABLE user_profiles (
    user_id VARCHAR(36) PRIMARY KEY,
    bio TEXT,
    profile_image_url VARCHAR(255),
    website_url VARCHAR(255),
    social_links JSON,
    preferences JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**설계 의도**: 사용자의 상세 프로필 정보를 분리하여 관리
- users와 1:1 관계: 기본 정보와 상세 정보 분리
- JSON 타입 사용: 유연한 소셜 링크와 선호도 저장
- ON DELETE CASCADE: 사용자 삭제 시 프로필도 자동 삭제

#### authors
```sql
CREATE TABLE authors (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NULL,
    name VARCHAR(100) NOT NULL,
    bio TEXT,
    profile_image_url VARCHAR(255),
    website_url VARCHAR(255),
    social_links JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_user (user_id)
);
```

**설계 의도**: 작가 정보를 독립적으로 관리하여 외부 작가도 포함
- user_id NULL 허용: 외부 플랫폼 작가 지원
- ON DELETE SET NULL: 사용자 삭제 시 작가 정보는 유지
- 소셜 링크 JSON: 다양한 플랫폼 연동 지원

### 3.2 작품 관련 테이블

#### novels
```sql
CREATE TABLE novels (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    description TEXT,
    genre VARCHAR(50) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    platform_url VARCHAR(255) NOT NULL,
    cover_url VARCHAR(255),
    status ENUM('NEW', 'ONGOING', 'HIATUS', 'COMPLETED') NOT NULL DEFAULT 'NEW',
    rating DECIMAL(3,2),
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES authors(id),
    INDEX idx_author (author_id),
    INDEX idx_platform (platform),
    INDEX idx_genre (genre),
    INDEX idx_status (status),
    INDEX idx_rating (rating)
);
```

**설계 의도**: 작품의 기본 정보와 메타데이터 관리
- status ENUM: 작품 상태의 명확한 구분
- rating DECIMAL(3,2): 정확한 평점 관리
- 복합 인덱스: 다양한 검색 조건 지원
- platform 정보: 원본 플랫폼 추적

#### platform_listings
```sql
CREATE TABLE platform_listings (
    id VARCHAR(36) PRIMARY KEY,
    novel_id VARCHAR(36) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    platform_url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    UNIQUE KEY uk_novel_platform (novel_id, platform),
    INDEX idx_platform (platform)
);
```

**설계 의도**: 다중 플랫폼 연재 작품 관리
- is_primary 플래그: 주요 플랫폼 지정
- UNIQUE 제약: 중복 연재 방지
- CASCADE 삭제: 작품 삭제 시 연재 정보도 삭제

#### chapters
```sql
CREATE TABLE chapters (
    id VARCHAR(36) PRIMARY KEY,
    novel_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    chapter_number INT NOT NULL,
    is_free BOOLEAN NOT NULL DEFAULT FALSE,
    price INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel (novel_id),
    INDEX idx_chapter_number (chapter_number)
);
```

**설계 의도**: 작품의 챕터별 상세 내용 관리
- MEDIUMTEXT: 대용량 콘텐츠 저장
- chapter_number: 순차적 접근 지원
- is_free/price: 유료/무료 구분
- CASCADE 삭제: 작품 삭제 시 챕터도 삭제

#### free_novels
```sql
CREATE TABLE free_novels (
    id VARCHAR(36) PRIMARY KEY,
    novel_id VARCHAR(36) NULL,
    author_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre VARCHAR(50) NOT NULL,
    cover_url VARCHAR(255),
    status ENUM('NEW', 'ONGOING', 'HIATUS', 'COMPLETED') NOT NULL DEFAULT 'NEW',
    rating DECIMAL(3,2),
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE SET NULL,
    FOREIGN KEY (author_id) REFERENCES authors(id),
    INDEX idx_author (author_id),
    INDEX idx_genre (genre),
    INDEX idx_status (status),
    INDEX idx_rating (rating)
);
```

**설계 의도**: 무료 소설 연재 작품 관리
- novel_id NULL 허용: 무료 연재 작품이 처음에는 free_novels에만 존재할 수 있음
- 유료화 전환 시 novels 테이블에 레코드 생성 후 연결
- ON DELETE SET NULL: novels 레코드 삭제 시 free_novels 레코드는 유지

#### free_chapters
```sql
CREATE TABLE free_chapters (
    id VARCHAR(36) PRIMARY KEY,
    free_novel_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content MEDIUMTEXT NOT NULL,
    chapter_number INT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (free_novel_id) REFERENCES free_novels(id) ON DELETE CASCADE,
    INDEX idx_free_novel (free_novel_id),
    INDEX idx_chapter_number (chapter_number)
);
```

**설계 의도**: 무료 소설의 챕터 관리
- 무료 연재 전용 챕터 테이블
- 가격 관련 필드 제외
- 조회수 등 기본 통계만 관리

#### free_novel_statistics
```sql
CREATE TABLE free_novel_statistics (
    id VARCHAR(36) PRIMARY KEY,
    free_novel_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (free_novel_id) REFERENCES free_novels(id) ON DELETE CASCADE,
    INDEX idx_free_novel_date (free_novel_id, date)
);
```

**설계 의도**: 무료 소설의 통계 관리
- 무료 연재 작품의 조회수 등 통계 관리
- 수익 관련 필드 제외
- date 기준 파티셔닝으로 대용량 데이터 효율적 관리

#### novel_rankings
```sql
CREATE TABLE novel_rankings (
    id VARCHAR(36) PRIMARY KEY,
    novel_id VARCHAR(36) NOT NULL,
    platform VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    period ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL,
    rank INT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_platform (novel_id, platform),
    INDEX idx_ranking (platform, category, period, rank)
);
```

**설계 의도**: 플랫폼별 랭킹 정보 관리
- period ENUM: 일간/주간/월간 랭킹 구분
- 복합 인덱스: 랭킹 조회 성능 최적화
- CASCADE 삭제: 작품 삭제 시 랭킹 정보도 삭제

### 3.3 통계 관련 테이블

#### novel_statistics
```sql
CREATE TABLE novel_statistics (
    id VARCHAR(36) PRIMARY KEY,
    novel_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    revenue DECIMAL(10,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    INDEX idx_novel_date (novel_id, date)
);
```

**설계 의도**: 작품별 상세 통계 관리
- date 기준 파티셔닝: 대용량 데이터 효율적 관리
- revenue DECIMAL: 정확한 수익 관리
- 복합 인덱스: 통계 조회 성능 최적화

#### author_statistics
```sql
CREATE TABLE author_statistics (
    id VARCHAR(36) PRIMARY KEY,
    author_id VARCHAR(36) NOT NULL,
    date DATE NOT NULL,
    total_views INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(10,2) NOT NULL DEFAULT 0,
    novel_count INT NOT NULL DEFAULT 0,
    chapter_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE,
    INDEX idx_author_date (author_id, date)
);
```

**설계 의도**: 작가별 종합 통계 관리
- date 기준 파티셔닝: 대용량 데이터 효율적 관리
- total_revenue: 작가 수익 집계
- novel_count/chapter_count: 작품 현황 추적

#### search_logs
```sql
CREATE TABLE search_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    query VARCHAR(255) NOT NULL,
    filters JSON,
    result_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user (user_id),
    INDEX idx_query (query),
    INDEX idx_created_at (created_at)
);
```

**설계 의도**: 검색 패턴 분석을 위한 로그 관리
- JSON filters: 유연한 검색 필터 저장
- created_at 기준 파티셔닝: 로그 데이터 효율적 관리
- user_id NULL 허용: 비회원 검색도 기록

### 3.4 매핑 테이블

#### category_mappings
```sql
CREATE TABLE category_mappings (
    id VARCHAR(36) PRIMARY KEY,
    platform VARCHAR(50) NOT NULL,
    platform_category VARCHAR(50) NOT NULL,
    standard_genre VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_category (platform, platform_category),
    INDEX idx_standard_genre (standard_genre)
);
```

**설계 의도**: 플랫폼별 카테고리 통합 관리
- UNIQUE 제약: 중복 매핑 방지
- standard_genre: 통합 카테고리 체계
- 플랫폼별 카테고리 유연성 보장

## 4. 데이터 관리 정책

### 4.1 작가 정보 처리 정책
1. 우리 서비스 회원인 작가
   - `authors` 테이블에 레코드 생성
   - `user_id`에 해당 사용자 ID 연결
   - `users` 테이블의 `user_type`이 'AUTHOR'인 사용자와 1:1 관계
   - 프로필 정보는 `authors` 테이블의 정보를 우선 사용
   - `user_profiles`의 정보는 서비스 내부용으로만 사용

2. 외부 플랫폼 작가
   - `authors` 테이블에 레코드 생성
   - `user_id`는 NULL
   - 작가 이름, 프로필 정보 등 기본 정보만 저장
   - 프로필 정보는 크롤링 또는 수동 입력 데이터 사용

3. 프로필 정보 동기화
   - `authors` 테이블의 프로필 정보는 공개용 작가 프로필로 사용
   - `user_profiles` 테이블의 정보는 서비스 내부용 개인 프로필로 사용
   - 작가 회원의 경우 두 프로필 정보를 별도로 관리
   - 프로필 이미지, 소셜 링크 등은 `authors` 테이블에서 관리
   - 개인 설정, 알림 설정 등은 `user_profiles` 테이블에서 관리

### 4.2 작품 상태 정의
- NEW: 신작 (연재 시작 전)
- ONGOING: 연재중
- HIATUS: 휴재
- COMPLETED: 완결

### 4.3 플랫폼 연재 정보
- 하나의 작품이 여러 플랫폼에 연재될 수 있음
- `platform_listings` 테이블에서 관리
- `is_primary` 플래그로 주요 플랫폼 지정
- 각 플랫폼별 URL 관리

### 4.4 통계 데이터 관리 정책
1. 작가 통계
   - `author_statistics` 테이블은 모든 작가(회원/비회원)의 통계를 관리
   - `author_id`는 `authors` 테이블을 참조
   - 일별 통계 데이터는 자동 집계
   - 통계 데이터는 1년간 보관 후 아카이브

2. 작품 통계
   - `novel_statistics` 테이블은 개별 작품의 통계를 관리
   - 조회수, 수익 등 실시간 집계
   - 통계 데이터는 1년간 보관 후 아카이브

3. 검색 로그
   - `search_logs` 테이블은 사용자 검색 이력을 관리
   - 검색어, 필터, 결과 수 등 기록
   - 로그 데이터는 3개월간 보관 후 아카이브

4. 무료 소설 통계
   - `free_novel_statistics` 테이블은 무료 연재 작품의 통계를 관리
   - 조회수 등 기본 통계만 집계
   - 통계 데이터는 1년간 보관 후 아카이브

### 4.5 무료 소설 연재 정책
1. 작품 등록
   - 무료 연재 작품은 처음에 `free_novels` 테이블에만 등록
   - `novel_id`는 NULL로 시작하여 유료화 전환 시 연결
   - 기본 메타데이터는 유료 작품과 동일하게 관리
   - 독립적인 ID 체계로 관리하여 유료화 전환 시 용이

2. 챕터 관리
   - 무료 연재 챕터는 `free_chapters` 테이블에 등록
   - 가격 관련 필드 없이 순수 콘텐츠만 관리
   - 조회수 등 기본 통계만 관리

3. 유료화 전환
   - 무료 작품을 유료화할 경우:
     1. `novels` 테이블에 새로운 레코드 생성
     2. `free_novels`의 `novel_id`를 새로 생성된 `novels` ID로 업데이트
     3. 기존 무료 챕터는 `chapters` 테이블로 이전
     4. 통계 데이터도 함께 이전하여 연속성 유지
   - 전환 후에도 `free_novels` 레코드는 유지하여 이력 관리

## 5. 데이터 보안 및 백업

### 5.1 데이터 보안
- 데이터베이스 보안 설정
- 데이터 암호화

### 5.2 데이터 백업
- 전체 백업: 일 1회
- 증분 백업: 시간별
- 바이너리 로그: 실시간
- 보관 기간
  - 전체 백업: 30일
  - 증분 백업: 7일
  - 바이너리 로그: 3일

### 5.3 데이터 복구
- 데이터베이스 복구 전략
- 장애 발생 시 데이터 복구 과정 기록 및 테스트 