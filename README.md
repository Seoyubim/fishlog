<div align="center">

# 🎣 FishLog

### AI 기반 낚시 기록·분석·추천 통합 플랫폼

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql)](https://www.postgresql.org/)
[![Gemini](https://img.shields.io/badge/Google_Gemini-Vision-4285F4?logo=google)](https://ai.google.dev/)
[![KMA](https://img.shields.io/badge/기상청-단기예보_API-0072CE)](https://www.data.go.kr/)
[![Swagger](https://img.shields.io/badge/Swagger_UI-SpringDoc-85EA2D?logo=swagger)](https://springdoc.org/)

</div>

---

## 📖 프로젝트 소개

FishLog는 낚시꾼을 위한 기록·분석 플랫폼입니다.
출조 기록의 **위치와 날씨는 GPS와 기상청 API가 자동으로 채워 주고**, 사용자는 틀린 값만 고치면 됩니다.
잡은 물고기 사진을 올리면 **AI가 어종을 분류**하고, 쌓인 기록은 **나만의 어종 도감**과 **개인화 리포트**가 됩니다.

> **"내가 무엇을 잡았고, 언제·어디서·무엇으로 잘 잡혔는가"**
> — 기록하는 수고는 줄이고, 기록에서 얻는 인사이트는 키웁니다.

> 🚧 **현재 상태: 개발 시작 단계입니다.** 아래 기능은 모두 구현 예정이며, 이 문서는 설계 기준입니다.

---

## 🧭 핵심 플로우

```
① 출조 기록                ② 어획 기록                  ③ 도감 · 리포트
┌──────────────────┐      ┌────────────────────┐      ┌──────────────────────┐
│ GPS → 위치 자동    │      │ 물고기 사진 업로드     │      │ 어종 도감 (수집 현황)    │
│ 기상청 → 날씨 자동  │ ───▶ │ AI 어종 분류 (제안)   │ ───▶ │ 월별 어획량             │
│ 미끼 입력          │      │ 사용자 확정 + 크기     │      │ 어종 랭킹 · 수계 비중    │
│ (모두 수정 가능)    │      │                    │      │ 미끼·날씨별 비교         │
└──────────────────┘      └────────────────────┘      └──────────────────────┘
```

1. **출조 기록** — 출조를 시작하면 현재 위치(GPS)와 날씨(기상청)가 자동으로 입력됩니다. 틀린 값은 직접 수정하고, 미끼를 입력합니다.
2. **어획 기록** — 잡은 물고기 사진을 올리면 AI가 어종을 제안합니다. 사용자가 확정하고 크기를 입력합니다.
3. **도감·리포트** — 확정된 기록이 어종 도감과 개인 리포트(월별 어획량, 어종 랭킹 등)로 쌓입니다.

---

## ✨ 주요 기능

| 우선순위 | 기능 | 설명 | 상태 |
|:---:|------|------|:---:|
| P0 | 출조 기록 | 일시·위치·날씨·미끼 입력, 자동 입력값 수정 가능 | 📋 예정 |
| P0 | 위치·날씨 자동 입력 | 브라우저 GPS + 기상청 단기예보 API | 📋 예정 |
| P0 | 사진 업로드 | UUID 기반 파일 저장, JPEG/PNG/WEBP | 📋 예정 |
| P0 | AI 어종 분류 | Gemini Vision, 도감 어종 목록 안에서 분류 | 📋 예정 |
| P0 | 어종 도감 | 수집한 어종 카드, 미수집 어종 잠금, 달성률 | 📋 예정 |
| P0 | 개인화 리포트 | 월별 어획량, 어종 랭킹 | 📋 예정 |
| P0 | JWT 인증 | Spring Security + Stateless JWT | 📋 예정 |
| P1 | 리포트 확장 | 수계 비중, 어종별 최대·평균 크기, 미끼·날씨별 비교 | 📋 예정 |
| P1 | 모바일 UI | Thymeleaf + Tailwind + Chart.js, 현장 사용 고려 | 📋 예정 |
| P1 | API 문서화 · 테스트 · CI | Swagger UI, 단위/통합 테스트, GitHub Actions | 📋 예정 |
| P2 | 클라우드 배포 | Docker + 배포 환경 | 📋 예정 |

---

## 🛠 기술 스택

### Backend
- **Java 21** / **Spring Boot 3**
- **Spring Data JPA** — 엔티티 기반 모델링, JPQL 집계 쿼리
- **Spring Security + JJWT** — Stateless JWT 인증
- **SpringDoc OpenAPI 2.x** — Swagger UI

### Database
- **PostgreSQL** — 운영 DB
- **H2 In-memory** — 테스트 환경 전용

### AI
- **Google Gemini Vision** — 사진 → 어종명 + 확신도 (JSON 응답)
- 모델명은 설정값으로 분리 (`ai.gemini.model`)
- 인터페이스 기반 설계(`AiFishClassificationClient`)로 제공자 교체 가능

### 외부 API
- **기상청 단기예보 조회서비스 (공공데이터포털)** — 초단기실황(기온·강수형태·풍속·습도), 초단기예보(하늘상태)
- **브라우저 Geolocation API** — 현재 위치 좌표

### Frontend
- **Thymeleaf** — 서버사이드 렌더링
- **Tailwind CSS CDN** — 모바일 우선, 야외에서 한 손으로 쓰기 쉬운 큰 버튼
- **Chart.js** — 월별 막대, 어종 순위, 수계 도넛 차트

### DevOps (예정)
- Docker / Docker Compose
- GitHub Actions CI

---

## 🏗 아키텍처

### 레이어 구조

```
사용자 (Browser)
   │  ├─ Geolocation API (현재 좌표)
   ▼
ViewController          ← Thymeleaf 화면 라우팅
RestController          ← JSON API (Trip / Catch / Dex / Stats / Weather / Auth)
   │
   ▼
Service Layer           ← 비즈니스 로직, SecurityContext에서 userId 추출
   │        ├─ WeatherClient  ──▶ 기상청 API
   │        └─ AiFishClassificationClient ──▶ Gemini Vision
   ▼
Repository Layer        ← Spring Data JPA + JPQL 집계 쿼리
   ▼
PostgreSQL DB
```

### 데이터 모델 (ERD)

```
User ──< Trip ──< Catch >── Species
```

| 엔티티 | 주요 필드 |
|--------|-----------|
| `User` | id, username, email, password(암호화), role |
| `Trip` (출조) | 출조일시, 위도, 경도, 위치명, 수계, 기온, 날씨 상태, 풍속, 습도, 미끼, 메모, 날씨 입력 출처(AUTO/MANUAL) |
| `Catch` (어획) | 사진 경로, 크기(cm), AI 제안 어종, AI 확신도, **확정 어종** |
| `Species` (도감 마스터) | 한글명, 학명, 수계, 설명, 대표 이미지 |

---

## ⚙️ 핵심 설계 결정

### 1. 출조(Trip)와 어획(Catch) 분리
날씨·위치·미끼는 **출조당 한 번**만 입력하고, 그 출조에서 잡은 물고기는 어획으로 여러 건 추가합니다.
한 마리도 못 잡은 출조도 기록할 수 있어서 **"어떤 미끼·날씨에서 성공률이 높은가"** 를 계산할 수 있습니다.
(어획만 기록하면 허탕이 빠져 성공률을 구할 수 없습니다.)

### 2. 자동 입력은 초기값, 저장값은 사용자 확정값
GPS와 기상청 값은 폼의 **초기값**일 뿐입니다. 사용자가 수정한 값이 저장되고, `Trip.weatherSource`에 자동/수동 여부를 남깁니다.
GPS 권한 거부나 API 실패 시에도 **수동 입력으로 기록은 항상 가능**해야 합니다. 실패를 0이나 "맑음"으로 채우지 않습니다.

### 3. AI는 제안, 통계와 도감은 확정 어종만
AI가 낸 어종은 `aiSpecies`에, 사용자가 확정한 어종은 `species`에 따로 저장합니다.
도감과 리포트는 **확정 어종만** 집계하므로 AI 오분류가 통계를 오염시키지 않습니다.
확신도가 낮으면 "확인 필요"로 표시합니다.

### 4. 도감 어종 목록 안에서만 분류
프롬프트에 도감 마스터의 어종 목록을 함께 전달하고 **목록 안에서 선택**하게 합니다.
목록 밖이면 "기타/모름"으로 응답하게 해서, AI가 만든 임의의 이름이 도감과 어긋나는 일을 막습니다.

### 5. 외부 장애 격리
사진 저장과 AI 분석을 분리합니다. AI 서버나 기상청 API가 응답하지 않아도 **기록 저장은 반드시 성공**합니다.
외부 의존성 장애가 핵심 기능 전체를 마비시키지 않게 합니다.

### 6. N+1 문제 방지
- 목록 조회: 사진 컬렉션 제외 → 쿼리 1회
- 상세 조회: 어획 컬렉션 포함 즉시 로딩 → 필요할 때만

### 7. 인터페이스 기반 클라이언트
```java
interface AiFishClassificationClient {
    AiClassificationResult classify(MultipartFile image, List<String> candidateSpecies);
}

interface WeatherClient {
    WeatherSnapshot fetch(double lat, double lon);
}
```
구현체만 교체하면 AI 제공자와 날씨 소스를 바꿀 수 있고, 테스트에서는 가짜 구현체를 주입합니다.

### 8. JWT Stateless 인증
세션 없이 모든 API 요청에 `Authorization: Bearer {token}` 검증.
`SecurityContextHolder`에서 userId를 추출하므로 Request DTO에 userId를 넣지 않습니다.

---

## 🌦 위치·날씨 자동 입력

### 흐름
1. 출조 작성 화면에서 브라우저가 현재 좌표를 가져옵니다. (HTTPS 필요, 권한 거부 시 수동)
2. 서버가 위경도를 **기상청 격자 좌표(nx, ny)** 로 변환합니다.
3. 초단기실황으로 **기온·강수형태·풍속·습도**, 초단기예보로 **하늘상태**를 가져옵니다.
4. 폼에 채우고, 사용자가 수정한 뒤 저장합니다.

### 제약 (설계에 반영)
- 실황은 **현재 시각** 값입니다. 출조 일시가 현재가 아니면(나중에 기록하는 경우) 자동 입력을 끄고 수동 입력으로 전환합니다.
- **수온·파고·물때는 제공하지 않습니다.** (이번 범위 밖)
- GPS는 좌표만 줍니다. **위치명은 사용자가 입력**합니다. (주소 자동 변환은 후속)
- 기상청 API는 공공데이터포털 **서비스 키 발급**이 필요합니다.

---

## 📚 도감 · 개인화 리포트

### 어종 도감
- 전체 어종을 카드 그리드로 보여 줍니다.
- **수집한 어종**: 사진, 첫 포획일, 포획 횟수, 최대 크기
- **미수집 어종**: 잠금 상태
- 상단에 **도감 달성률** (수집 어종 / 전체 어종)

### 개인 리포트
| 항목 | 우선순위 |
|------|:---:|
| 월별 어획량 | P0 |
| 어종 랭킹 | P0 |
| 수계 비중 (바다/민물) | P1 |
| 어종별 최대·평균 크기 | P1 |
| 미끼별·날씨별 출조 성공률 (허탕 포함) | P1 |

**응답 예시 (설계안):**
```json
{
  "totalTrips": 12,
  "totalCatches": 23,
  "successRate": 0.75,
  "monthlyCounts": [{"month": 1, "count": 2}],
  "fishRanking": [{"fishName": "광어", "count": 8}],
  "waterTypeStats": [{"waterType": "SEA", "count": 18}],
  "dexProgress": {"collected": 7, "total": 20}
}
```

---

## 🚀 실행 방법

### 사전 요구사항
- Java 21
- PostgreSQL 실행 중
- Google Gemini API 키 ([발급](https://aistudio.google.com/apikey))
- 공공데이터포털 기상청 단기예보 서비스 키 ([활용 신청](https://www.data.go.kr/))

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
    model: YOUR_GEMINI_MODEL        # 사용 가능한 Vision 지원 모델명을 확인해 입력
    base-url: https://generativelanguage.googleapis.com

weather:
  kma:
    service-key: YOUR_DATA_GO_KR_SERVICE_KEY
    base-url: https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0

jwt:
  secret: your-256-bit-secret-key
  expiration-ms: 86400000
```

> ⚠️ `application-local.yaml`은 `.gitignore`에 포함되어 있습니다. API 키를 절대 커밋하지 마세요.

### 3. 도감 어종 데이터

초기 어종 목록은 `src/main/resources/data/species_seed.csv`로 관리하고, 앱 시작 시 `Species` 테이블에 적재합니다.
(지원 어종 수와 목록은 팀에서 확정합니다.)

### 4. 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 5. 접속

| 경로 | 설명 |
|------|------|
| `http://localhost:8080/login` | 로그인 |
| `http://localhost:8080/signup` | 회원가입 |
| `http://localhost:8080/trips` | 출조 기록 목록 |
| `http://localhost:8080/dex` | 어종 도감 |
| `http://localhost:8080/stats` | 개인 리포트 |
| `http://localhost:8080/swagger-ui.html` | API 문서 |

---

## 📡 API 명세 (설계안)

### 인증

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 반환) |

### 날씨 자동 입력 *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/weather?lat=&lon=` | 좌표 기준 현재 날씨 (폼 초기값용) |

### 출조 *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/trips` | 내 출조 목록 |
| POST | `/api/v1/trips` | 출조 등록 |
| GET | `/api/v1/trips/{id}` | 출조 상세 (어획 포함) |
| PUT | `/api/v1/trips/{id}` | 출조 수정 |
| DELETE | `/api/v1/trips/{id}` | 출조 삭제 |

### 어획 *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/trips/{tripId}/catches` | 사진 업로드 + AI 어종 제안 |
| PATCH | `/api/v1/catches/{id}/species` | 어종 확정·수정 |
| DELETE | `/api/v1/catches/{id}` | 어획 삭제 |

### 도감 · 통계 *(인증 필요)*

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/dex` | 내 도감 (수집 현황) |
| GET | `/api/v1/dex/{speciesId}` | 어종 상세 + 내 기록 |
| GET | `/api/v1/stats/annual?year=2026` | 연간 리포트 |

---

## 📁 프로젝트 구조 (계획)

```
src/main/java/com/fishlog/fishlog/
├── FishlogApplication.java
│
├── config/
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── ExternalClientConfig.java     # Gemini · 기상청 RestClient Bean
│
├── domain/
│   ├── User.java
│   ├── Trip.java
│   ├── Catch.java
│   ├── Species.java
│   └── enums/                        # WaterType, WeatherCondition, WeatherSource, UserRole
│
├── security/
│   ├── JwtTokenProvider.java
│   └── JwtAuthenticationFilter.java
│
├── controller/
│   ├── AuthController.java
│   ├── TripController.java
│   ├── CatchController.java
│   ├── WeatherController.java
│   ├── DexController.java
│   ├── StatsController.java
│   └── ViewController.java
│
├── service/
│   ├── AuthService.java
│   ├── TripService.java
│   ├── CatchService.java
│   ├── DexService.java
│   ├── StatsService.java
│   ├── StorageService.java
│   └── CustomUserDetailsService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── TripRepository.java
│   ├── CatchRepository.java          # 통계용 JPQL 집계 쿼리 포함
│   └── SpeciesRepository.java
│
├── dto/
│   ├── request/                      # @Valid 유효성 검사 포함
│   └── response/                     # 목록용 / 상세용 분리
│
├── ai/
│   ├── AiFishClassificationClient.java
│   ├── GeminiAiFishClassificationClient.java
│   └── AiClassificationResult.java
│
├── weather/
│   ├── WeatherClient.java
│   ├── KmaWeatherClient.java         # 기상청 API 호출
│   ├── KmaGridConverter.java         # 위경도 → 격자(nx, ny)
│   └── WeatherSnapshot.java
│
└── exception/
    ├── GlobalExceptionHandler.java
    ├── TripNotFoundException.java
    └── CatchNotFoundException.java
```

---

## 🗺 개발 로드맵

- [ ] **Step 1** — 프로젝트 세팅, 엔티티(User · Trip · Catch · Species), 도감 어종 시드
- [ ] **Step 2** — Spring Security + JWT 인증
- [ ] **Step 3** — 출조 CRUD + GPS·기상청 날씨 자동 입력 + 수정
- [ ] **Step 4** — 사진 업로드 + AI 어종 제안 + 사용자 확정
- [ ] **Step 5** — 어종 도감 (수집 현황, 달성률)
- [ ] **Step 6** — 개인화 리포트 (월별 어획량, 어종 랭킹, 수계 비중 등)
- [ ] **Step 7** — Thymeleaf 프론트엔드 (모바일 반응형)
- [ ] **Step 8** — API 문서화, 단위/통합 테스트, GitHub Actions CI
- [ ] **Step 9** — Docker + 배포

### 이후 확장 후보
- 기록 기반 조건 추천 (룰 기반: 잘 잡힌 날씨·미끼 조합)
- 사진 EXIF로 과거 출조의 일시·위치 자동 입력
- 수온·물때 데이터 연동
- 금어기·금지체장 판정 (잡은 어종이 가져가도 되는지 안내)
- 주소 자동 변환(역지오코딩)
