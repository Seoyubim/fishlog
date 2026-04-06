<div align="center">

# 🎣 FishLog

### AI 기반 낚시 기록·분석·추천 통합 플랫폼

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![Gemini](https://img.shields.io/badge/Google_Gemini-1.5_Flash-4285F4?logo=google)](https://ai.google.dev/)
[![Swagger](https://img.shields.io/badge/Swagger_UI-SpringDoc-85EA2D?logo=swagger)](https://springdoc.org/)

</div>

---

## 📖 프로젝트 소개

FishLog는 낚시꾼을 위한 기록·분석·추천 통합 플랫폼입니다.
물고기 사진을 올리면 **Google Gemini Vision AI가 어종을 자동 분류**하고, 누적된 기록 데이터를 바탕으로 **개인화된 낚시 리포트와 통계**를 제공합니다.

> **"언제, 어디서, 무엇으로 잡았을 때 성공률이 가장 높은가"**
> — 데이터 기반 낚시 인사이트를 목표로 합니다.

---

## ✨ 주요 기능

| 기능 | 설명 | 상태 |
|------|------|------|
| 낚시 기록 CRUD | 날짜·위치·날씨·어종·크기·미끼 입력 및 관리 | ✅ 완료 |
| 사진 업로드 | UUID 기반 파일 저장, JPEG/PNG/WEBP 지원 | ✅ 완료 |
| AI 어종 분류 | Google Gemini 1.5 Flash Vision API 연동 | ✅ 완료 |
| 개인 통계 리포트 | 월별 어획량, 어종 랭킹, 수계 비중 차트 | ✅ 완료 |
| JWT 인증 | Spring Security + Stateless JWT 로그인/회원가입 | ✅ 완료 |
| API 문서화 | Swagger UI (`/swagger-ui.html`) | ✅ 완료 |
| 프론트엔드 | Thymeleaf + Tailwind CSS + Chart.js (모바일 우선) | ✅ 완료 |
| 테스트 & CI | 단위/통합 테스트 + GitHub Actions | 🚧 진행 중 |
| 클라우드 배포 | Docker + AWS EC2/Railway + CI/CD | 📋 예정 |

---

## 🛠 기술 스택

### Backend
- **Java 21** / **Spring Boot 3**
- **Spring Data JPA** — 엔티티 기반 테이블 자동 생성, JPQL 집계 쿼리
- **Spring Security + JJWT** — Stateless JWT 인증 필터 체인
- **SpringDoc OpenAPI 2.x** — Swagger UI 자동 생성

### Database
- **PostgreSQL** — 운영 DB (`ddl-auto: validate`)
- **H2 In-memory** — 테스트 환경 전용

### AI
- **Google Gemini 1.5 Flash API** — 물고기 사진 → 어종명 + 확신도 JSON 응답
- 인터페이스 기반 설계(`AiFishClassificationClient`)로 AI 제공자 교체 가능

### Frontend
- **Thymeleaf** — 서버사이드 렌더링
- **Tailwind CSS CDN** — 모바일 우선 반응형 UI (40~60대 낚시꾼 타겟)
- **Chart.js** — 월별 막대, 어종 순위, 수계 도넛 차트

### DevOps (예정)
- Docker / Docker Compose
- GitHub Actions CI/CD
- AWS EC2 + RDS / Railway

---

## 🏗 아키텍처

### 레이어 구조

```
사용자 (Browser / App)
        │
        ▼
  ViewController          ← Thymeleaf 화면 라우팅
  RestController          ← JSON API (FishLog / Photo / Stats / Auth)
        │
        ▼
    Service Layer         ← 비즈니스 로직, SecurityContext에서 userId 추출
        │
        ▼
  Repository Layer        ← Spring Data JPA + JPQL 집계 쿼리
        │
        ▼
    PostgreSQL DB
```

### 데이터 모델 (ERD)

```
User ──< FishLog ──< FishPhoto
```

- `User`: id, username, email, password(암호화), role
- `FishLog`: 날짜, 위치, 어종(수동/AI), 크기, 날씨, 수계, 미끼
- `FishPhoto`: 파일경로, AI분류여부, 확신도

---

## ⚙️ 핵심 설계 결정

### 1. AI 장애 격리 설계
사진 저장과 AI 분석을 분리 처리합니다.
AI 서버가 응답하지 않아도 **기록 저장은 반드시 성공**하며, AI 결과는 이후 업데이트됩니다.
외부 의존성 장애가 핵심 기능 전체를 마비시키는 상황을 방지합니다.

### 2. N+1 문제 방지
- 목록 조회(`getList`): 사진 데이터 제외 → 쿼리 1회
- 상세 조회(`getDetail`): 사진 컬렉션 포함 즉시 로딩 → 필요할 때만

### 3. AI 어종명 우선순위
```sql
COALESCE(fishNameAi, fishName)
```
AI 분류 결과가 있으면 우선 사용, 없으면 사용자 입력값 사용.
통계 집계 일관성 확보를 위한 설계입니다.

### 4. 인터페이스 기반 AI 클라이언트
```java
interface AiFishClassificationClient {
    AiClassificationResult classify(MultipartFile image);
}
```
현재 구현체: `GeminiAiFishClassificationClient`
OpenAI, Claude 등 다른 모델로 교체 시 구현체만 교체하면 됩니다.

### 5. JWT Stateless 인증
세션 없이 모든 API 요청에 `Authorization: Bearer {token}` 헤더 검증.
`SecurityContextHolder`에서 userId를 추출하므로 Request DTO에 userId를 직접 넣지 않습니다.

---

## 🚀 실행 방법

### 사전 요구사항
- Java 21
- PostgreSQL 실행 중
- Google Gemini API 키 ([발급](https://aistudio.google.com/apikey))

### 1. 레포지토리 클론

```bash
git clone https://github.com/Seoyubim/fishlog.git
cd fishlog
```

### 2. 환경 설정

`src/main/resources/application-local.yaml` 파일을 생성하고 아래 내용을 입력합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fishlog
    username: your_db_user
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: update

ai:
  gemini:
    api-key: YOUR_GEMINI_API_KEY
    model: gemini-1.5-flash
    base-url: https://generativelanguage.googleapis.com

jwt:
  secret: your-256-bit-secret-key
  expiration-ms: 86400000
```

> ⚠️ `application-local.yaml`은 `.gitignore`에 포함되어 있습니다. API 키를 절대 커밋하지 마세요.

### 3. 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. 접속

| 경로 | 설명 |
|------|------|
| `http://localhost:8080/login` | 로그인 |
| `http://localhost:8080/signup` | 회원가입 |
| `http://localhost:8080/logs` | 낚시 기록 목록 |
| `http://localhost:8080/stats` | 통계 대시보드 |
| `http://localhost:8080/swagger-ui.html` | API 문서 |

---

## 📡 API 명세

Swagger UI에서 전체 명세 확인 가능합니다: `http://localhost:8080/swagger-ui.html`

### 인증 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 반환) |

### 낚시 기록 API *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/fish-logs` | 내 낚시 기록 목록 |
| POST | `/api/v1/fish-logs` | 낚시 기록 등록 |
| GET | `/api/v1/fish-logs/{id}` | 낚시 기록 상세 |
| PUT | `/api/v1/fish-logs/{id}` | 낚시 기록 수정 |
| DELETE | `/api/v1/fish-logs/{id}` | 낚시 기록 삭제 |
| POST | `/api/v1/fish-logs/{id}/photos` | 사진 업로드 + AI 분류 |

### 통계 API *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/stats/annual?year=2025` | 연간 통계 리포트 |

**응답 예시:**
```json
{
  "totalCount": 23,
  "maxLengthCm": 72.5,
  "avgLengthCm": 38.42,
  "monthlyCounts": [{"month": 1, "count": 2}, ...],
  "fishRanking": [{"fishName": "광어", "count": 8}, ...],
  "waterTypeStats": [{"waterType": "SEA", "count": 18}, ...]
}
```

---

## 📁 프로젝트 구조

```
src/main/java/com/fishlog/fishlog/
├── FishlogApplication.java
│
├── config/
│   ├── SecurityConfig.java         # JWT Stateless 보안 설정
│   ├── SwaggerConfig.java          # API 문서화 설정
│   └── AiClientConfig.java         # Gemini RestClient Bean
│
├── domain/
│   ├── User.java
│   ├── FishLog.java
│   ├── FishPhoto.java
│   └── enums/                      # WaterType, WeatherCondition, UserRole
│
├── security/
│   ├── JwtTokenProvider.java       # JWT 생성/검증
│   └── JwtAuthenticationFilter.java
│
├── controller/
│   ├── AuthController.java
│   ├── FishLogController.java
│   ├── PhotoController.java
│   ├── StatsController.java
│   └── ViewController.java         # Thymeleaf 화면 라우팅
│
├── service/
│   ├── AuthService.java
│   ├── FishLogService.java
│   ├── PhotoService.java
│   ├── StatsService.java
│   ├── StorageService.java
│   └── CustomUserDetailsService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── FishLogRepository.java      # 통계용 JPQL 집계 쿼리 포함
│   └── FishPhotoRepository.java
│
├── dto/
│   ├── request/                    # @Valid 유효성 검사 포함
│   └── response/                   # 목록용 / 상세용 분리
│
├── ai/
│   ├── AiFishClassificationClient.java   # 인터페이스
│   ├── GeminiAiFishClassificationClient.java
│   └── AiClassificationResult.java
│
└── exception/
    ├── GlobalExceptionHandler.java
    ├── UserNotFoundException.java
    └── FishLogNotFoundException.java
```

---

## 🗺 개발 로드맵

- [x] **Step 1~4** — MVP: 엔티티 설계, CRUD API, 사진 업로드, 통계 리포트
- [x] **Step 5** — AI 실연동 (Google Gemini 1.5 Flash)
- [x] **Step 6** — API 문서화 (Swagger UI)
- [x] **Step 7** — Spring Security + JWT 인증
- [x] **Step 8** — Thymeleaf 프론트엔드 (모바일 반응형)
- [ ] **Step 9** — 단위/통합 테스트 + GitHub Actions CI
- [ ] **Step 10** — Docker + 클라우드 배포 (AWS EC2 or Railway)

---

## 📄 라이선스

This project is licensed under the MIT License.
