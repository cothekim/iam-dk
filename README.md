# IAM-DK - Identity Access Management Developer Kit

MVP 기반의 중앙 인증·사용자 디렉터리·기본 프로비저닝 서비스입니다.

## 📋 개요

- **제품명**: IAM-DK (Identity Access Management Developer Kit)
- **목적**: 사내/파트너 애플리케이션들의 중앙 인증·사용자 디렉터리·기본 프로비저닝 통합
- **벤치마크**: SAP Cloud Identity Services (IAS + IPS) 최소 기능 세트

## 🏗️ 아키텍처

```
┌─────────────┐      ┌────────────────┐      ┌──────────────┐
│ Admin Console│─────▶│Directory Service│─────▶│  PostgreSQL  │
│   (React)   │      │   (Spring Boot) │      │              │
└─────────────┘      └────────────────┘      └──────────────┘
                            │
                            │ SCIM/Admin API
                            ▼
                     ┌────────────────┐
                     │  Auth Server   │
                     │ (Spring Boot)  │
                     │  OIDC Provider │
                     └────────────────┘
                            │
                            │ JWT Validation
                            ▼
                ┌─────────────────────┐
                │  Sample Resource    │
                │      Server         │
                └─────────────────────┘
```

## 📦 구성 요소

| 컴포넌트 | 포트 | 설명 |
|---------|------|------|
| postgres | 5432 | PostgreSQL 데이터베이스 |
| auth-server | 8080 | OAuth2/OIDC Authorization Server |
| directory-service | 8081 | User/Group/SCIM API, Provisioning |
| admin-console | 80 | React 관리 콘솔 |
| sample-resource-server | 8082 | 샘플 앱 (OIDC 연동 예제) |

## 🚀 빠른 시작

### 사전 요구사항

- Docker & Docker Compose
- JDK 17
- Gradle 8.5+ (또는 SDKMAN으로 자동 설치)
- Node.js 18+ (로컬 개발 시)

### 1. 클론 및 빌드

```bash
cd iam-dk

# SDKMAN 설치 (선택사항)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Java 17 설치
sdk install java 17.0.13-tem
sdk use java 17.0.13-tem

# Gradle 8.5 설치
sdk install gradle 8.5
sdk use gradle 8.5

# 전체 빌드
cd auth-server && gradle build && cd ..
cd directory-service && gradle build && cd ..
cd sample-resource-server && gradle build && cd ..

# React Admin Console 빌드
cd admin-console && npm install && npm run build && cd ..
```

### 2. Docker Compose로 실행

```bash
docker compose up -d --build
```

### 3. 초기 계정 설정

PostgreSQL에 기본 admin 계정이 필요합니다:

```bash
docker exec -it iam-dk-directory-db psql -U iamdk -d iamdk_directory -c "
INSERT INTO users (login_name, email, password, first_name, last_name, active, created_at, updated_at)
VALUES ('admin', 'admin@iamdk.local', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System', 'Admin', true, NOW(), NOW())
ON CONFLICT (login_name) DO NOTHING;
"
```

**기본 계정:**
- Username: `admin`
- Password: `admin123`
- BCrypt 해시 미리 계산됨

### 4. 접속

| 서비스 | URL |
|--------|-----|
| Admin Console | http://localhost |
| Auth Server | http://localhost:8080 |
| Directory Service API | http://localhost:8081 |
| Sample App | http://localhost:8082 |

## 📚 사용 가이드

### Admin Console

1. http://localhost 접속
2. admin / admin123 로그인
3. Users, Groups, OAuth Clients, Provisioning 관리

### OAuth 클라이언트 등록

1. Admin Console → OAuth Clients → Add OAuth Client
2. 설정 예시:
   - Client ID: `my-app`
   - Client Secret: `my-secret`
   - Redirect URI: `http://localhost/auth/callback`
   - Grant Types: `authorization_code`, `refresh_token`
   - Scopes: `openid`, `profile`, `email`

### OIDC 로그인 흐름

```
1. 앱 → Authorization Server: GET /oauth2/authorize?client_id=my-app&response_type=code&redirect_uri=...
2. 유저 로그인
3. Authorization Server → 앱: redirect with authorization code
4. 앱 → Authorization Server: POST /oauth2/token with code
5. Authorization Server → 앱: Access Token + Refresh Token
6. 앱 → Resource Server: API 호출 with JWT Bearer token
7. Resource Server → Authorization Server: JWK로 JWT 검증
```

### CSV 프로비저닝

1. Admin Console → Provisioning → Download CSV Template
2. CSV 파일 편집 (loginName, email, firstName, lastName, active)
3. 파일 업로드
4. Dry run으로 확인 후 Execute

**CSV 형식 예시:**
```csv
loginName,email,firstName,lastName,active
john.doe,john@example.com,John,Doe,true
jane.smith,jane@example.com,Jane,Smith,false
```

## 🔌 API 엔드포인트

### Directory Service

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/admin/login` | Admin 로그인 | - |
| GET | `/api/admin/users` | 사용자 목록 | JWT |
| POST | `/api/admin/users` | 사용자 생성 | JWT |
| PUT | `/api/admin/users/{id}` | 사용자 수정 | JWT |
| DELETE | `/api/admin/users/{id}` | 사용자 삭제 | JWT |
| GET | `/api/scim/v2/Users` | SCIM 사용자 목록 | JWT |
| POST | `/api/scim/v2/Users` | SCIM 사용자 생성 | JWT |
| GET | `/api/scim/v2/Groups` | SCIM 그룹 목록 | JWT |
| POST | `/api/provisioning/jobs` | 프로비저닝 작업 생성 | JWT |
| POST | `/api/provisioning/jobs/{id}/execute` | CSV 업로드 실행 | JWT |

### Auth Server

| Method | Path | 설명 |
|--------|------|------|
| GET | `/.well-known/openid-configuration` | OIDC Discovery |
| GET | `/.well-known/jwks.json` | JWK 공개 키 |
| GET | `/oauth2/authorize` | Authorization Code Flow |
| POST | `/oauth2/token` | Token 요청 |

### Sample Resource Server

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/public/hello` | 공개 엔드포인트 |
| GET | `/api/protected/hello` | 인증 필요 |
| GET | `/api/protected/admin` | Admin 전용 |

## 🧪 테스트

### SCIM API 테스트

```bash
# Get users (with JWT token)
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8081/api/scim/v2/Users

# Create user
curl -X POST \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/scim+json" \
  -d '{
    "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
    "userName": "testuser",
    "emails": [{"value": "test@example.com", "type": "work", "primary": true}],
    "name": {"givenName": "Test", "familyName": "User"},
    "active": true
  }' \
  http://localhost:8081/api/scim/v2/Users
```

### OIDC Flow 테스트

```bash
# 1. Get authorization code (브라우저에서)
http://localhost:8080/oauth2/authorize?client_id=admin-console&response_type=code&redirect_uri=http://localhost/auth/callback&scope=openid%20profile%20email

# 2. Exchange code for token
curl -X POST \
  -u "admin-console:admin-console-secret" \
  -d "grant_type=authorization_code&code=<CODE>&redirect_uri=http://localhost/auth/callback" \
  http://localhost:8080/oauth2/token
```

## 📁 프로젝트 구조

```
iam-dk/
├── docker-compose.yml
├── auth-server/                 # OAuth2/OIDC Provider
│   ├── src/main/java/
│   ├── build.gradle
│   └── Dockerfile
├── directory-service/          # User/Group/SCIM/Provisioning
│   ├── src/main/java/
│   ├── build.gradle
│   └── Dockerfile
├── admin-console/               # React Admin UI
│   ├── src/
│   ├── package.json
│   ├── nginx.conf
│   └── Dockerfile
└── sample-resource-server/     # Sample App
    ├── src/main/java/
    ├── build.gradle
    └── Dockerfile
```

## 🔒 보안

### 비밀번호 정책
- 최소 길이: 8자
- 대문자 필수
- 소문자 필수
- 숫자 필수

### 계정 잠금
- 실패 5회 이상 시 30분 잠금

### JWT
- Access Token: 1시간 유효
- Refresh Token: 30일 유효
- RS256 서명

## 🛠️ 개발

### 로컬 개발 (Docker 없이)

```bash
# PostgreSQL 실행 (Docker)
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=iamdk_directory \
  -e POSTGRES_USER=iamdk \
  -e POSTGRES_PASSWORD=secret \
  postgres:16-alpine

# Auth Server
cd auth-server && gradle bootRun

# Directory Service
cd directory-service && gradle bootRun

# Admin Console
cd admin-console && npm start

# Sample Resource Server
cd sample-resource-server && gradle bootRun
```

## 📝 MVP 성공 지표

- ✅ 관리자 콘솔에서 신규 사용자 등록 → 샘플 앱 로그인 성공
- ✅ CSV 파일 1개로 100명 이상 사용자 생성/업데이트 성공
- ✅ 개발자가 자체 애플리케이션 1개를 OIDC로 연동하여 로그인 완료

## 🚧 후속 Phase (Out of Scope)

- SAML 2.0 지원
- 멀티테넌시
- 고급 Provisioning (Delta Sync, 여러 Target)
- 외부 IdP 연동 (Azure AD 등)
- MFA (TOTP, SMS)
- 고급 Authorization Policy 엔진

## 📄 라이선스

Proprietary - Internal Use Only

## 👥 지원

문의: IAM-DK Development Team
