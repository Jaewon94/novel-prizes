# API 명세서

## 1. 개요

본 문서는 "소설 플랫폼 통합 중계 서비스"의 API 명세를 정의합니다. REST API와 GraphQL API의 엔드포인트, 요청/응답 형식, 인증 방식 등을 상세히 기술합니다.

## 2. API 기본 정보

### 2.1 기본 URL
```
REST API: https://api.novel-prizes.com/v1
GraphQL API: https://api.novel-prizes.com/graphql
```

### 2.2 인증 방식
- JWT 기반 인증
- Authorization 헤더에 Bearer 토큰 사용
```
Authorization: Bearer <access_token>
```

### 2.3 응답 형식
```json
{
  "success": true,
  "data": {
    // 응답 데이터
  },
  "error": null,
  "meta": {
    "timestamp": "2024-03-21T12:00:00Z",
    "version": "1.0"
  }
}
```

### 2.4 에러 응답
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": {
      // 상세 에러 정보
    }
  },
  "meta": {
    "timestamp": "2024-03-21T12:00:00Z",
    "version": "1.0"
  }
}
```

### 2.5 HTTP 상태 코드
- 200 OK: 요청 성공
- 201 Created: 리소스 생성 성공
- 204 No Content: 요청 성공 (응답 본문 없음)
- 400 Bad Request: 잘못된 요청
- 401 Unauthorized: 인증 필요
- 403 Forbidden: 권한 없음
- 404 Not Found: 리소스를 찾을 수 없음
- 409 Conflict: 리소스 충돌
- 429 Too Many Requests: 요청 제한 초과
- 500 Internal Server Error: 서버 내부 오류

### 2.6 인증 필요 여부
- 🔒: 인증 필요
- 🔓: 인증 불필요

## 3. REST API 명세

### 3.1 인증 API

#### 3.1.1 회원가입
```http
POST /auth/signup
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "사용자",
  "userType": "READER" // READER, AUTHOR
}

Response:
{
  "success": true,
  "data": {
    "userId": "user_123",
    "email": "user@example.com",
    "nickname": "사용자",
    "userType": "READER",
    "createdAt": "2024-03-21T12:00:00Z"
  }
}
```

#### 3.1.2 로그인
```http
POST /auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600
  }
}
```

### 3.2 작품 API

#### 3.2.1 작품 목록 조회
```http
GET /novels
Query Parameters:
- platform: string (optional) // 네이버, 카카오, 리디북스 등
- genre: string (optional) // 판타지, 로맨스 등
- status: string (optional) // 연재중, 완결
- sort: string (optional) // popularity, latest, rating
- page: number (optional, default: 1)
- limit: number (optional, default: 20)

Response:
{
  "success": true,
  "data": {
    "novels": [
      {
        "id": "novel_123",
        "title": "소설 제목",
        "author": "작가명",
        "coverUrl": "https://...",
        "platform": "네이버시리즈",
        "genre": "판타지",
        "status": "연재중",
        "rating": 4.5,
        "viewCount": 10000
      }
    ],
    "pagination": {
      "total": 100,
      "page": 1,
      "limit": 20,
      "hasMore": true
    }
  }
}
```

#### 3.2.2 작품 상세 조회
```http
GET /novels/{novelId}

Response:
{
  "success": true,
  "data": {
    "id": "novel_123",
    "title": "소설 제목",
    "author": "작가명",
    "coverUrl": "https://...",
    "description": "줄거리...",
    "platform": "네이버시리즈",
    "genre": "판타지",
    "status": "연재중",
    "rating": 4.5,
    "viewCount": 10000,
    "platformUrl": "https://...",
    "rankings": [
      {
        "platform": "네이버시리즈",
        "rank": 1,
        "category": "판타지",
        "period": "DAILY"
      }
    ],
    "chapters": [
      {
        "id": "chapter_123",
        "title": "1화",
        "isFree": true,
        "price": 0
      }
    ]
  }
}
```

### 3.3 검색 API

#### 3.3.1 작품 검색
```http
GET /search/novels
Query Parameters:
- query: string (required)
- platform: string (optional)
- genre: string (optional)
- status: string (optional)
- page: number (optional, default: 1)
- limit: number (optional, default: 20)

Response:
{
  "success": true,
  "data": {
    "novels": [
      {
        "id": "novel_123",
        "title": "검색된 소설",
        "author": "작가명",
        "coverUrl": "https://...",
        "platform": "네이버시리즈",
        "genre": "판타지",
        "status": "연재중",
        "rating": 4.5
      }
    ],
    "pagination": {
      "total": 50,
      "page": 1,
      "limit": 20,
      "hasMore": true
    }
  }
}
```

### 3.4 추천 API

#### 3.4.1 개인화 추천
```http
GET /recommendations/personal
Query Parameters:
- limit: number (optional, default: 10)

Response:
{
  "success": true,
  "data": {
    "novels": [
      {
        "id": "novel_123",
        "title": "추천 소설",
        "author": "작가명",
        "coverUrl": "https://...",
        "platform": "네이버시리즈",
        "genre": "판타지",
        "status": "연재중",
        "rating": 4.5,
        "matchScore": 0.85
      }
    ]
  }
}
```

### 3.5 작가 API

#### 3.5.1 작품 관리
```http
# 새 작품 등록 🔒
POST /novels
Content-Type: application/json

Request:
{
  "title": "소설 제목",
  "description": "줄거리...",
  "genre": "판타지",
  "platform": "네이버시리즈",
  "platformUrl": "https://...",
  "coverUrl": "https://..." // 이미지 업로드 API(/upload/cover)를 통해 얻은 URL
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": "novel_123",
    "title": "소설 제목",
    "author": "작가명",
    "coverUrl": "https://...",
    "createdAt": "2024-03-21T12:00:00Z"
  }
}

# 작품 정보 수정 🔒
PUT /novels/{novelId}
Content-Type: application/json

Request:
{
  "title": "수정된 제목",
  "description": "수정된 줄거리...",
  "genre": "판타지",
  "coverUrl": "https://..." // 이미지 업로드 API(/upload/cover)를 통해 얻은 URL
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": "novel_123",
    "title": "수정된 제목",
    "updatedAt": "2024-03-21T12:00:00Z"
  }
}

# 작품 삭제 🔒
DELETE /novels/{novelId}

Response: 204 No Content
```

#### 3.5.2 챕터 관리
```http
# 새 챕터 등록 🔒
POST /novels/{novelId}/chapters
Content-Type: application/json

Request:
{
  "title": "1화",
  "content": "챕터 내용...",
  "isFree": true,
  "price": 0
}

Response: 201 Created
{
  "success": true,
  "data": {
    "id": "chapter_123",
    "title": "1화",
    "createdAt": "2024-03-21T12:00:00Z"
  }
}

# 챕터 수정 🔒
PUT /novels/{novelId}/chapters/{chapterId}
Content-Type: application/json

Request:
{
  "title": "수정된 제목",
  "content": "수정된 내용...",
  "isFree": false,
  "price": 100
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": "chapter_123",
    "title": "수정된 제목",
    "updatedAt": "2024-03-21T12:00:00Z"
  }
}

# 챕터 삭제 🔒
DELETE /novels/{novelId}/chapters/{chapterId}

Response: 204 No Content
```

#### 3.5.3 작가 통계
```http
# 자신의 작품 목록 조회 🔒
GET /authors/{authorId}/novels
Query Parameters:
- status: string (optional) // 연재중, 완결
- sort: string (optional) // latest, popularity
- page: number (optional, default: 1)
- limit: number (optional, default: 20)

Response: 200 OK
{
  "success": true,
  "data": {
    "novels": [
      {
        "id": "novel_123",
        "title": "소설 제목",
        "status": "연재중",
        "viewCount": 10000,
        "chapterCount": 50,
        "lastUpdatedAt": "2024-03-21T12:00:00Z"
      }
    ],
    "pagination": {
      "total": 10,
      "page": 1,
      "limit": 20,
      "hasMore": false
    }
  }
}

# 작품 통계 조회 🔒
GET /authors/{authorId}/statistics
Query Parameters:
- period: string (optional) // daily, weekly, monthly, yearly
- startDate: string (optional) // YYYY-MM-DD
- endDate: string (optional) // YYYY-MM-DD

Response: 200 OK
{
  "success": true,
  "data": {
    "totalViews": 100000,
    "totalRevenue": 1000000,
    "novelCount": 5,
    "chapterCount": 250,
    "periodStats": [
      {
        "date": "2024-03-21",
        "views": 1000,
        "revenue": 10000
      }
    ]
  }
}
```

#### 3.5.4 오퍼 관리
```http
# 수신 오퍼 목록 조회 🔒
GET /authors/{authorId}/offers
Query Parameters:
- status: string (optional) // pending, accepted, rejected
- page: number (optional, default: 1)
- limit: number (optional, default: 20)

Response: 200 OK
{
  "success": true,
  "data": {
    "offers": [
      {
        "id": "offer_123",
        "platform": "네이버시리즈",
        "novelId": "novel_123",
        "title": "소설 제목",
        "status": "pending",
        "proposedPrice": 1000000,
        "createdAt": "2024-03-21T12:00:00Z"
      }
    ],
    "pagination": {
      "total": 5,
      "page": 1,
      "limit": 20,
      "hasMore": false
    }
  }
}

# 오퍼 상태 변경 🔒
PUT /authors/{authorId}/offers/{offerId}
Content-Type: application/json

Request:
{
  "status": "accepted" // accepted, rejected
}

Response: 200 OK
{
  "success": true,
  "data": {
    "id": "offer_123",
    "status": "accepted",
    "updatedAt": "2024-03-21T12:00:00Z"
  }
}
```

### 3.6 파일 업로드 API

#### 3.6.1 작품 표지 이미지 업로드
```http
# 이미지 업로드 🔒
POST /upload/cover
Content-Type: multipart/form-data

Request:
- file: binary (required) // 이미지 파일
- novelId: string (optional) // 기존 작품의 경우

Response: 200 OK
{
  "success": true,
  "data": {
    "url": "https://...",
    "width": 800,
    "height": 1200,
    "size": 102400
  }
}
```

## 4. GraphQL API 명세

### 4.1 스키마 정의
```graphql
type Novel {
  id: ID!
  title: String!
  author: String!
  coverUrl: String!
  description: String
  platform: String!
  genre: String!
  status: String!
  rating: Float
  viewCount: Int
  platformUrl: String!
  rankings: [Ranking!]
  chapters: [Chapter!]
}

type Ranking {
  platform: String!
  rank: Int!
  category: String!
  period: String!
}

type Chapter {
  id: ID!
  title: String!
  isFree: Boolean!
  price: Int
}

type Query {
  novel(id: ID!): Novel
  novels(
    platform: String
    genre: String
    status: String
    sort: String
    page: Int
    limit: Int
  ): NovelConnection!
  searchNovels(
    query: String!
    platform: String
    genre: String
    status: String
    page: Int
    limit: Int
  ): NovelConnection!
  recommendations(limit: Int): [Novel!]!
}

type NovelConnection {
  edges: [NovelEdge!]!
  pageInfo: PageInfo!
}

type NovelEdge {
  node: Novel!
  cursor: String!
}

type PageInfo {
  hasNextPage: Boolean!
  endCursor: String
}

type Mutation {
  # 작품 관리
  createNovel(input: CreateNovelInput!): Novel!
  updateNovel(id: ID!, input: UpdateNovelInput!): Novel!
  deleteNovel(id: ID!): Boolean!
  
  # 챕터 관리
  createChapter(novelId: ID!, input: CreateChapterInput!): Chapter!
  updateChapter(id: ID!, input: UpdateChapterInput!): Chapter!
  deleteChapter(id: ID!): Boolean!
  
  # 오퍼 관리
  updateOfferStatus(id: ID!, status: OfferStatus!): Offer!
}

input CreateNovelInput {
  title: String!
  description: String!
  genre: String!
  platform: String!
  platformUrl: String!
  coverUrl: String # 이미지 업로드 API(/upload/cover)를 통해 얻은 URL
}

input UpdateNovelInput {
  title: String
  description: String
  genre: String
  coverUrl: String # 이미지 업로드 API(/upload/cover)를 통해 얻은 URL
}

input CreateChapterInput {
  title: String!
  content: String!
  isFree: Boolean!
  price: Int
}

input UpdateChapterInput {
  title: String
  content: String
  isFree: Boolean
  price: Int
}

enum OfferStatus {
  PENDING
  ACCEPTED
  REJECTED
}

type Offer {
  id: ID!
  platform: String!
  novelId: ID!
  title: String!
  status: OfferStatus!
  proposedPrice: Int!
  createdAt: DateTime!
  updatedAt: DateTime
}

type Author {
  id: ID!
  nickname: String!
  novels: [Novel!]!
  statistics: AuthorStatistics!
  offers: [Offer!]!
}

type AuthorStatistics {
  totalViews: Int!
  totalRevenue: Int!
  novelCount: Int!
  chapterCount: Int!
  periodStats: [PeriodStat!]!
}

type PeriodStat {
  date: String!
  views: Int!
  revenue: Int!
}
```

### 4.2 쿼리 예시

#### 4.2.1 작품 상세 조회
```graphql
query GetNovel($id: ID!) {
  novel(id: $id) {
    id
    title
    author
    coverUrl
    description
    platform
    genre
    status
    rating
    viewCount
    platformUrl
    rankings {
      platform
      rank
      category
      period
    }
    chapters {
      id
      title
      isFree
      price
    }
  }
}
```

#### 4.2.2 작품 목록 조회
```graphql
query GetNovels(
  $platform: String
  $genre: String
  $status: String
  $sort: String
  $page: Int
  $limit: Int
) {
  novels(
    platform: $platform
    genre: $genre
    status: $status
    sort: $sort
    page: $page
    limit: $limit
  ) {
    edges {
      node {
        id
        title
        author
        coverUrl
        platform
        genre
        status
        rating
      }
      cursor
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}
```

## 5. 에러 코드

### 5.1 공통 에러 코드
- `AUTH_REQUIRED`: 인증이 필요한 요청
- `INVALID_TOKEN`: 유효하지 않은 토큰
- `PERMISSION_DENIED`: 권한 없음
- `INVALID_REQUEST`: 잘못된 요청
- `NOT_FOUND`: 리소스를 찾을 수 없음
- `INTERNAL_ERROR`: 서버 내부 오류

### 5.2 비즈니스 에러 코드
- `NOVEL_NOT_FOUND`: 작품을 찾을 수 없음
- `INVALID_SEARCH_PARAMS`: 잘못된 검색 파라미터
- `RATE_LIMIT_EXCEEDED`: 요청 제한 초과
- `PLATFORM_ERROR`: 플랫폼 연동 오류

## 6. API 버전 관리

### 6.1 버전 관리 정책
- URL에 버전 포함: `/v1/...`
- 하위 호환성 유지
- 주요 변경 시 새 버전 배포

### 6.2 버전 지원 기간
- 최신 버전: 12개월
- 이전 버전: 6개월
- 더 이상 지원하지 않는 버전은 3개월 전 공지

## 7. API 사용 제한

### 7.1 Rate Limiting
- 기본: 1000 요청/시간
- 인증된 사용자: 5000 요청/시간
- 검색 API: 100 요청/시간

### 7.2 캐싱 정책
- GET 요청: 5분
- 검색 결과: 1시간
- 작품 상세: 1시간
- 추천 결과: 30분 