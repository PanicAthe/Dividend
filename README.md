# 📈 Stock Dividend Service

미국 주식 배당금 정보를 제공하는 API 서비스입니다. 이 서비스는 특정 회사의 배당금 정보를 조회하거나, 회사 정보를 추가 및 삭제할 수 있는 기능을 제공합니다. 또한, 회원가입 및 로그인 기능을 통해 권한이 있는 사용자에게만 API를 제공할 수 있습니다.

## 🛠️ 기술 스택

- **Backend Framework**: Spring Boot
- **Language**: Java
- **Database**: H2 (In-memory DB), JPA
- **Caching**: Redis
- **Web Scraping**: Jsoup
- **Security**: JWT, Password Encryption

## 📝 기능 및 설정

### 1. **스케줄링**
이 프로젝트에서는 정기적으로 배당금 정보를 스크래핑하기 위해 Spring의 스케줄링 기능을 사용합니다. ScraperScheduler 클래스는 다음과 같은 기능을 수행합니다:

- `application.yml` 파일에서 설정한 `spring.scheduler.scrap.yahoo` 크론 표현식에 따라 정기적으로 배당금 정보 스크래핑이 가능합니다.
- `@CacheEvict` 어노테이션을 사용하여 스크래핑 전에 Redis 캐시를 비웁니다.
- `yahooFinanceScheduler` 메서드는 모든 관리 중인 회사 목록을 조회하여 각 회사의 배당금 정보를 새로 스크래핑합니다. 단시간에 연속적 요청을 방지하기 위해 각 요청 사이에 3초의 대기 시간을 둡니다.

### 2. **웹 스크래핑**
이 프로젝트에서는 **Jsoup**을 사용하여 Yahoo Finance 웹사이트에서 배당금 정보를 스크래핑합니다. 주요 스크래핑 작업은 다음과 같습니다:

- **배당금 정보 스크래핑**: 특정 회사의 배당금 이력을 수집하기 위해 `STATISTICS_URL`을 사용하여 HTML을 파싱하고, 배당금 정보를 추출합니다.
- **회사 정보 스크래핑**: `SUMMARY_URL`을 통해 회사의 기본 정보를 수집합니다.

## 📄 API 명세서

### 1. **배당금 정보 조회**
- **Endpoint**: `GET /finance/dividend/{companyName}`
- **설명**: 회사 이름을 입력으로 받아 해당 회사의 메타 정보와 배당금 정보를 반환합니다.
- **Response**:
  - 성공 시: 회사 메타 정보 및 배당금 정보 (JSON)
  - 실패 시: `400 Bad Request`와 에러 메시지

### 2. **회사 이름 자동완성**
- **Endpoint**: `GET /company/autocomplete`
- **설명**: 검색어 prefix를 입력으로 받아 자동완성된 회사명 리스트를 반환합니다.
- **Response**: 회사명 리스트 (JSON)

### 3. **회사 목록 조회**
- **Endpoint**: `GET /company`
- **설명**: 서비스에서 관리 중인 모든 회사 목록을 페이지네이션 형태로 반환합니다.
- **Response**: `Page` 인터페이스 형태로 회사 목록 (JSON)

### 4. **회사 정보 추가**
- **Endpoint**: `POST /company`
- **설명**: 새로운 회사 정보를 추가합니다.
- **Request**: 추가할 회사의 ticker (JSON)
- **Response**:
  - 성공 시: 회사 정보가 성공적으로 저장됨 (200 OK)
  - 실패 시:
    - `400 Bad Request`와 에러 메시지 (이미 존재하는 회사이거나, 잘못된 ticker일 경우)

### 5. **회사 정보 삭제**
- **Endpoint**: `DELETE /company/{ticker}`
- **설명**: 해당 ticker에 대한 회사 정보를 삭제합니다. 회사 정보와 관련된 배당금 정보 및 캐시도 함께 삭제됩니다.
- **Response**:
  - 성공 시: 회사 정보 삭제 완료 (200 OK)
  - 실패 시: `400 Bad Request`와 에러 메시지

### 6. **회원가입**
- **Endpoint**: `POST /auth/signup`
- **설명**: 새로운 사용자를 등록합니다.
- **Request**: 사용자 ID 및 패스워드 (JSON)
- **Response**:
  - 성공 시: 회원가입 완료 (201 Created)
  - 실패 시: `400 Bad Request`와 에러 메시지 (중복 ID일 경우)

### 7. **로그인**
- **Endpoint**: `POST /auth/signin`
- **설명**: 회원가입된 사용자가 로그인하여 JWT 토큰을 발급받습니다.
- **Request**: 사용자 ID 및 패스워드 (JSON)
- **Response**:
  - 성공 시: JWT 토큰 발급 (200 OK)
  - 실패 시: `401 Unauthorized`와 에러 메시지 (잘못된 자격)
