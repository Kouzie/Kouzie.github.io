---
title:  "Spring Boot - Keyclock!"
date: 2025-12-14


categories:
  - springboot
---

## Keyclock

> <https://www.keycloak.org/>  
> <https://github.com/keycloak/keycloak>  

Redhat 에서 개발한 `Java, Quarkus, RDB` 조합으로 이루어진 오픈소스 ID 및 접근 관리(IAM) 솔루션.  

Authorization Server 를 Keyclock 으로 대체할 수 있다.  
OAuth 기능뿐 아니라 Realm 도메인을 기반으로 사용자 정보와 그룹을 논리적으로 분리할 수 있다.  

`SSO, OAuth, OIDC` 같은 인증 기능들을 개발할 때 사용자 도메인을 포함하여 다양한 인증 도메인들을 처리하기 위한 포괄적이고 추상적인 코드들과 구현 로직이 필요하다.  

반복적인 보안처리 코드 및 설정을 작성하는것은 지루하고 따분하고,  
인증이 필요한 어플리케이션을 개발할 때 마다 신경쓰지 않은 코드들에선 보안문제가 발생한다.  

Keyclock 과 같은 오픈소스를 사용하는것이 더 안전하고 유리할 수 있다.  

OAuth 기능을 예로 들경우 Keyclock 이 Authorization Server 로서 OAuth 2.0 인증을 수행하고, 사용자 정보를 관리하는 Resource Server 로서 동작한다.  

- [Keycloak 공식 문서](https://www.keycloak.org/documentation)
- [Realm 개념 설명](https://www.keycloak.org/docs/latest/server_admin/#_core_concepts)

### 설치  

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: keycloak-postgres
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak
    volumes:
      - ./volumes/postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keycloak"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: keycloak/keycloak:26.4
    container_name: keycloak
    command: start-dev
    environment:
      KC_DB: postgres
      KC_DB_URL_HOST: postgres
      KC_DB_URL_DATABASE: keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: keycloak
      KC_HOSTNAME: localhost
      KC_HOSTNAME_PORT: 8080
      KC_HOSTNAME_STRICT: false
      KC_HOSTNAME_STRICT_HTTPS: false
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin123
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
```

### Keyclock 도메인

- **Realm (영역)**  
  - Keycloak에서 가장 최상위 논리적 격리 단위, 하나의 독립된 인증·인가 도메인 (Security Domain)
  - master Realm: Keycloak 설치 시 기본으로 생성되는 특수 Realm, Keycloak 자체의 관리자 계정이 여기에 속함, 운영 환경에서는 별도의 Realm을 생성하여 사용 권장
  - 완전히 격리된 사용자, 클라이언트, 역할 관리
  - **회사, 조직, 테넌트 단위**로 구분할 때 사용
- **Clients (클라이언트)**  
  - Keycloak에 인증을 위임하는 애플리케이션/서비스, 웹, 모바일, API 서비스 등이 해당  
- **Roles (역할)**  
  - **Realm Role**: Realm 전체에서 공통적으로 사용되는 역할. 모든 클라이언트에 적용 가능.  
  - **Client Role**: 특정 클라이언트에서만 의미 있는 역할. 다른 클라이언트에서는 적용되지 않음.  
  - **Composite Role**: 여러 역할을 묶어서 하나의 상위 역할처럼 사용하는 기능.  
- **Users (사용자)**  
  - Realm 안에서 인증과 인가 대상으로 로그인 가능한 계정들  
- **Groups (그룹)**  
  - User, Role 를 묶는 집한단위, 구조적 관리 가능  

## Realm 구성

```
애플리케이션을 추가해야 할 때:

사용자가 완전히 다른가?
├─ YES → 새로운 Realm 생성
│   (예: 다른 회사, 다른 조직)
│
└─ NO → 같은 Realm에 Client 추가
    (예: 같은 회사의 다른 앱)
    
인증 정책/설정이 다르나?
├─ YES → 새로운 Realm 생성
│   (예: 프로덕션 vs 스테이징)
│
└─ NO → 같은 Realm에 Client 추가
    (예: 같은 환경의 다른 앱)
    
브랜딩/테마가 완전히 다른가?
├─ YES → 새로운 Realm 생성
│   (예: 다른 브랜드)
│
└─ NO → 같은 Realm에 Client 추가
    (예: 같은 브랜드의 다른 앱)
```

**시나리오 1: 단일 회사의 여러 애플리케이션**

```
Realm: "acme-corp"
├─ Client: "employee-portal" (직원 포털)
├─ Client: "customer-portal" (고객 포털)
├─ Client: "mobile-app" (모바일 앱)
└─ Client: "admin-api" (관리 API)

→ 모든 애플리케이션이 같은 사용자 풀 공유
→ 한 번 로그인하면 모든 앱 사용 가능 (SSO)
```

**시나리오 2: 멀티 테넌트 SaaS**

```
Realm: "tenant-company-a" (회사 A)
├─ Client: "web-app"
└─ Client: "mobile-app"

Realm: "tenant-company-b" (회사 B)
├─ Client: "web-app"
└─ Client: "mobile-app"

→ 각 회사는 완전히 독립된 Realm
→ 회사 간 데이터 완전 격리
```

**시나리오 3: 내부/외부 사용자 분리**

```
Realm: "internal" (내부 직원)
├─ Client: "hr-system"
├─ Client: "finance-system"
└─ Client: "it-portal"

Realm: "customers" (고객)
├─ Client: "customer-portal"
└─ Client: "mobile-app"

→ 내부 직원과 고객은 완전히 분리
→ 각각 다른 인증 정책 적용 가능
```


**시나리오 4: 다른 인증 정책/설정**

```
Realm: "production" (프로덕션 - 엄격한 보안)
Realm: "staging" (스테이징 - 완화된 보안)
→ 인증 정책, 비밀번호 정책이 다름
```


### Role 생성 시나리오

**Realm Roles (Realm 레벨 역할)**
- **범위**: Realm 전체에서 사용 가능
- **적용 대상**: 모든 Client에서 인식됨
- **예시**: `admin`, `user`, `manager`, `employee`
- **용도**: 조직 전체 권한 관리, 사용자 기본 권한
- **토큰 표현**: `"admin"`, `"user"` (Client 이름 없음)

**Client Roles (Client 레벨 역할)**
- **범위**: 특정 Client에만 속함
- **적용 대상**: 해당 Client에서만 인식됨
- **예시**: `read-only`, `write-access`, `editor`, `viewer`
- **용도**: 특정 애플리케이션의 세부 권한
- **토큰 표현**: `"web-app:read-only"` (Client 이름 포함)

**권장 사용 패턴:**
- Realm Role: 사용자 기본 권한 레벨 (admin, user 등)
- Client Role: 애플리케이션별 기능 권한 (read, write 등)

```
Realm: "acme-corp"

Realm Roles:
├─ admin (전체 관리자)
├─ user (일반 사용자)
└─ manager (매니저)

Clients:
├─ "hr-system"
│   └─ Client Roles:
│       ├─ hr-read (인사 정보 읽기)
│       ├─ hr-write (인사 정보 쓰기)
│       └─ hr-admin (인사 시스템 관리)
│
├─ "finance-system"
│   └─ Client Roles:
│       ├─ finance-read (재무 정보 읽기)
│       ├─ finance-write (재무 정보 쓰기)
│       └─ finance-admin (재무 시스템 관리)
│
└─ "project-management"
    └─ Client Roles:
        ├─ pm-read (프로젝트 읽기)
        ├─ pm-write (프로젝트 쓰기)
        └─ pm-admin (프로젝트 관리)
```

**토큰 표현**

```javascript
// 사용자가 가진 역할
Realm Roles: ["admin", "user"]
Client Roles: ["web-app:read-only", "mobile-app:editor"]

// JWT 토큰에 포함되는 형태
{
  "realm_access": {
    "roles": ["admin", "user"]  // Realm Roles
  },
  "resource_access": {
    "web-app": {
      "roles": ["read-only"]    // Client Roles (web-app)
    },
    "mobile-app": {
      "roles": ["editor"]       // Client Roles (mobile-app)
    }
  }
}
```


### Group 생성 시나리오

**Group**은 사용자를 논리적으로 묶어서 관리하는 단위.

1. **사용자 일괄 관리**
   - 여러 사용자를 하나의 그룹으로 묶어 관리
   - 그룹에 역할을 부여하면 그룹 내 모든 사용자에게 자동 적용
   - Group에 Role을 부여하면, 그룹의 모든 멤버가 해당 Role을 상속받음
   - 사용자 개별 관리보다 효율적


**시나리오 1: 부서별 권한 관리**

```
회사 구조:
├─ 개발팀 (developers)
│  ├─ Role: developer, code-reviewer
│  └─ Users: 10명
├─ 운영팀 (operations)
│  ├─ Role: operator, monitor
│  └─ Users: 5명
└─ 관리팀 (managers)
   ├─ Role: admin, manager
   └─ Users: 3명
```

**시나리오 2: 프로젝트별 그룹**

```
프로젝트 A:
├─ Group: "project-a-developers"
│  └─ Role: project-a-access
├─ Group: "project-a-managers"
│  └─ Role: project-a-admin
```

**시나리오 3: 지역별 그룹**

```
지역별 조직:
├─ Group: "서울사무소"
│  ├─ Group: "서울-개발팀"
│  └─ Group: "서울-영업팀"
└─ Group: "부산사무소"
   ├─ Group: "부산-개발팀"
   └─ Group: "부산-영업팀"
```

**시나리오 4: 중첩 그룹**  

> **중첩 그룹의 특징:**  
> 하위 그룹은 상위 그룹의 역할을 상속받음  
> 조직 구조를 자연스럽게 표현 가능  

```
Group: "IT부서"
├─ Group: "개발팀"
│  ├─ Role: "developer"
│  └─ Users: john, jane
├─ Group: "운영팀"
│  ├─ Role: "operator"
│  └─ Users: alice, charlie
└─ Group: "QA팀"
   ├─ Role: "tester"
   └─ Users: david, eve
```

## 기본구성

realm 생성시 아래 리소스들이 기본적으로 생성된다.  
기본 클라이언트는 Keyclock 에서 realm 유지에 사용되며 삭제 불가능하다.  

- **client**
  - **account**: 사용자 자신의 계정정보를 관리할 수 있는 인터페이스 클라이언트  
  - **broker**: 외부 ID 제공자(Identity Provider)와의 브로커 연결을 위한 클라이언트. 소셜 로그인, SAML, OIDC 등 외부 인증 제공자와 연동 시 사용  
  - **account-console**: 사용자 자신의 계정정보를 관리할 수 있는 콘솔을 위한 클라이언트
  - **security-admin-console**: `Realm, Client, User` 의 모든 보안 설정을 관리하는 관리자 콘솔을 위한 클라이언트  
  - **admin-cli**: `Keycloak Admin REST API` 를 통해 관리자 작업을 수행하는 클라이언트
  - **realm-management**: `Keycloak Admin REST API` 를 통해 Realm, Client, User, Role 등을 관리를 위한 클라이언트

`Realm Role`, `Client Role` 이 기본적으로 생성된다.  

- **Realm Roles**  
  - **offline_access**: 오프라인 액세스 역할. 리프레시 토큰을 사용하여 사용자가 오프라인 상태에서도 토큰을 갱신하고 인증을 유지할 수 있게 해주는 역할.  
  - **uma_authorization**: UMA(User-Managed Access) 기능 사용을 위한 역할.  
  - **default-roles-{name}-realm**: Realm 생성 시 기본적으로 생성되는 기본 composite 역할. 아래 roles 들을 기본적으로 가지고 있다.  
    - view-profile(account client role)  
    - manage-account(account client role)  
    - offline_access  
    - uma_authorization  

> **UMA(User-Managed Access)**  
> 사용자가 자신의 리소스에 대한 접근 권한을 직접 관리하고 제어할 수 있게 해주는 표준.

- **account Client Roles**  
  - **delete-account**: 계정 삭제 권한
  - **manage-account**: 계정 관리 권한 (프로필 수정, 비밀번호 변경 등)
  - **manage-account-links**: 계정 연결 관리 권한 (소셜 로그인 계정 연결/해제)
  - **manage-consent**: 동의(Consent) 관리 권한
  - **view-applications**: 애플리케이션 목록 조회 권한
  - **view-consent**: 동의 내역 조회 권한
  - **view-groups**: 그룹 목록 조회 권한
  - **view-profile**: 프로필 조회 권한

- **broker Client Roles**
  - **read-token**: 사용자 세션에 연결된 토큰 정보를 조회할 수 있는 권한. 예를 들어 소셜 로그인(OIDC/SAML)로 들어온 사용자의 연동 상태를 확인하거나, 외부 제공자 토큰/클레임을 확인해 추가 처리(연동 디버깅, 사용자 속성 매핑 검증 등)를 해야 할 때 사용됨

- **realm-management Client Roles**
  - **create-client**: 새로운 클라이언트를 생성할 수 있는 권한
  - **impersonation**: 다른 사용자로 로그인(Impersonate) 할 수 있는 권한
  - **manage-authorization**: 권한 부여(Authorization) 정책 관리 권한
  - **manage-clients**: 클라이언트 관리 권한
  - **manage-events**: 이벤트 로그 관리/삭제 권한
  - **manage-identity-providers**: 외부 ID 제공자 설정 관리 권한
  - **manage-realm**: Realm 설정 관리 권한
  - **manage-users**: 사용자 관리 권한 (생성, 삭제, 수정, 역할 매핑 등)
  - **query-clients**: 클라이언트 검색 권한
  - **query-groups**: 그룹 검색 권한
  - **query-realms**: Realm 검색 권한
  - **query-users**: 사용자 검색 권한
  - **realm-admin**: 해당 Realm의 모든 권한을 가진 관리자 역할 (모든 management 권한 포함)
  - **view-authorization**: 권한 부여(Authorization) 정책 조회 권한
  - **view-clients**: 클라이언트 조회 권한
  - **view-events**: 이벤트 로그 조회 권한
  - **view-identity-providers**: 외부 ID 제공자 설정 조회 권한
  - **view-realm**: Realm 설정 조회 권한
  - **view-users**: 사용자 조회 권한

## 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/keycloak-demo>
