---
title:  "FastAPI - CRUD!"
date: 2024-07-07


categories:
  - python


---


## FastAPI 개요

> <https://fastapi.tiangolo.com/>  
> <https://github.com/tiangolo/fastapi>  

```
ASGI 서버 (Uvicorn / Gunicorn)
├── Worker 1 (별도 프로세스)
│   ├── FastAPI 앱 인스턴스
│   └── Event Loop 1
│       ├── Task 1 ── request 1
│       ├── Task 2 ── request 2
│       └── Task 3 ── request 3
└── Worker 2 (별도 프로세스)
    ├── FastAPI 앱 인스턴스
    └── Event Loop 2
        ├── Task 1 ── request 4
        ├── Task 2 ── request 5
        └── Task 3 ── request 6
```

- Worker 하나 = 보통 하나의 프로세스
- Event Loop 하나 = 해당 Worker의 비동기 작업을 처리
- Request 하나 = Event Loop에서 실행되는 Coroutine/Task

워커는 별개의 프로세스로 메모리도 공유하지 않는다.  
API 호출횟수 등을 구하려면 redis, prometheus 같은 외부저장소의 도움을 받아야한다.  

> **WSGI(Web Server Gateway Interface)**  
> Python 웹서버에서 사용하는 웹 어플리케이션 인터페이스, `gunicorn` 이 구현하여 제공한다.  
> 
> **ASGI(Asynchronous Server Gateway Interface)**  
> Python 웹서버에서 사용, 비동기 웹 애플리케이션을 지원하기 위해 WSGI의 비동기 확장판으로 개발된 인터페이스, `uvicorn` 이 구현하여 제공한다.  
> 
> FastAPI 는 ASGI 에서 동작하는 웹 어플리케이션, `app = FastAPI()` 로 만든 그 객체가 곧 ASGI 애플리케이션이고, `uvicorn app.main:app` 의 `app.main:app` 은 **"어느 모듈의 어느 변수를 띄울지"** 를 가리킨다.  
> 
> `gunicorn -k uvicorn.workers.UvicornWorker` 조합도 오래 쓰였지만, 지금은 uvicorn 자체가 `--workers` 를 지원하므로 굳이 얹을 이유가 줄었다.
> 컨테이너 환경이라면 워커 수를 늘리는 대신 **컨테이너 replica 를 늘리는 쪽**이 스케줄링·모니터링 면에서 다루기 쉽다.  
> <https://gunicorn.org/>  
> <https://www.uvicorn.org/>

### 실행환경

**Python 의 `pip install` 은 기본적으로 인터프리터 전역에 설치된다.**  
프로젝트마다 라이브러리 버전이 충돌하는 걸 막으려면 가상환경이 필요하다.  

FastAPI 는 **빈 디렉터리에서 파일을 직접 만들어 시작**한다.  

```
myapi/
├── .venv/
├── .gitignore
├── main.py
└── requirements.txt
```

```py
# main.py
from fastapi import FastAPI

app = FastAPI()                     # ① 애플리케이션 인스턴스

@app.get("/")                       # ② 라우트 등록
def read_root():
    return {"Hello": "World"}       # ③ dict 반환 → JSON 자동 변환

@app.get("/items/{item_id}")
async def read_item(item_id: int, q: str | None = None):
    return {"item_id": item_id, "q": q}
```

```sh
# myapi
python3 -m venv .venv                 # .venv/ 디렉터리에 독립된 인터프리터 생성
source .venv/bin/activate             # macOS/Linux 활성화

python -m pip install --upgrade pip
python -m pip install "fastapi[standard]"
python -m pip freeze > requirements.txt
```

가상환경을 활성화하면 현재 셸의 `python`, `pip`, `fastapi` 명령은 `.venv` 안의 실행 파일을 사용한다.  
개발 서버도 같은 셸에서 실행한다.  

```sh
fastapi dev main.py
```

활성화하지 않고 실행하려면 `.venv` 안의 실행 파일을 직접 지정하면 된다.  

```sh
.venv/bin/fastapi dev main.py
```

`fastapi` 만 설치하면 웹 프레임워크의 핵심 의존성만 들어온다.  
서버와 CLI, 폼·파일 처리, 테스트 도구까지 같이 쓰려면 보통 `[standard]` 를 붙인다.  

| 패키지 | 역할 |
| --- | --- |
| `fastapi` | 웹 API 프레임워크 |
| `starlette` | ASGI 웹 기능의 기반 |
| `pydantic` | 요청·응답 모델 검증 |
| `uvicorn[standard]` | ASGI 애플리케이션 서버 |
| `fastapi-cli[standard]` | `fastapi dev` / `fastapi run` 명령 |
| `httpx` | HTTP 클라이언트, `TestClient` 지원 |
| `jinja2` | HTML 템플릿 엔진 |
| `python-multipart` | Form 및 파일 업로드 파싱 |
| `email-validator` | `EmailStr` 이메일 검증 |

`pydantic-settings`(환경변수·`.env` 설정)와 `pydantic-extra-types`(추가 데이터 타입)는 `[standard]` 가 아니라 `[all]` extra 에 들어 있다.
설정 관리가 필요하면 뒤(Pydantic 기본)에서처럼 `pydantic-settings` 를 따로 설치한다.  

`uvicorn[standard]` 는 서버 실행에 필요한 패키지를 다시 묶어서 설치한다.  

| 패키지 | 역할 |
| --- | --- |
| `uvloop` | 고성능 asyncio 이벤트 루프(지원 플랫폼에서 설치) |
| `httptools` | 고성능 HTTP 프로토콜 파서 |
| `watchfiles` | 개발 서버의 파일 변경 감지와 자동 재시작 |
| `websockets` | WebSocket 프로토콜 지원 |
| `python-dotenv` | Uvicorn 의 `--env-file` 지원 |
| `PyYAML` | YAML 형식 로그 설정 지원 |

`python-dotenv` 는 FastAPI 의 직접 의존성이 아니라 `uvicorn[standard]` 를 통해 설치되는 간접 의존성이다.  

> **최근 버전은 `fastapi-cloud-cli` 와 `sentry-sdk` 까지 함께 설치된다.**  
> 클라우드 배포용 CLI 인데, 쓰지 않을 거라면 이 extra 를 쓰면 빠진다.  
>
> ```sh
> pip install "fastapi[standard-no-fastapi-cloud-cli]"
> ```


```sh
fastapi dev              # 개발 모드, 자동 리로드, 127.0.0.1 만 접근가능
fastapi run --workers 4  # 운영 모드
```

`fastapi-cli` 는 **uvicorn 래퍼**일 뿐이다. 아래 두 줄은 사실상 같다.  

```sh
fastapi dev
uvicorn app.main:app --reload

fastapi run --workers 4 
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

4코어·8GB 컨테이너 환경이라면 아래와 같이 설정할 수 있다.  

```py
# main.py
from fastapi import FastAPI
import uvicorn

app = FastAPI()

if __name__ == "__main__":
    uvicorn.run(
        "main:app",           # main.py의 FastAPI app 실행
        host="0.0.0.0",       # 외부 접속 허용
        port=8000,            # 서버 포트
        workers=2,            # 프로세스 2개 실행
        limit_concurrency=50, # 워커당 동시 요청 최대 50개
        backlog=256,          # 연결 대기열 최대 256개
        timeout_keep_alive=5, # 유휴 연결을 5초 후 종료
    )
```

### async / 동기 관련 함정 정리

**uvicorn 워커는 서로 메모리를 공유하지 않는 별개 프로세스**다.  

여기서 나오는 실수는 아래와 같다.    

1. 프로세스 변수에 상태를 담으면 워커를 늘리는 순간 깨진다  
   요청이 어느 워커로 갈지는 알 수 없다. 공유가 필요하면 Redis 로 빼고, 불일치가 무해한 캐시만 프로세스 로컬로 둔다.  
2. `async def` 안의 블로킹 호출은 워커 전체를 멈춘다  
   `time.sleep`, `requests.get`, 동기 SDK, 파일 I/O 는 `asyncio.to_thread()` 로 감싸거나 핸들러를 `def` 로 선언한다.  
3. CPU 작업은 스레드로 해결되지 않는다  
   이미지 처리나 암호화처럼 CPU 를 오래 쓰는 작업은 `ProcessPoolExecutor` 같은 별도 프로세스로 보낸다.  
4. 프로세스 로컬 상태는 워커 사이에 공유되지 않는다  
   `--workers 4` 는 서로 다른 메모리를 가진 프로세스 네 개를 만든다. 공유 상태는 외부 시스템으로 분리한다.  

### 자동 탐색 규칙(auto-discovered)

`fastapi dev` 는 인자 없이 실행하면 아래 순서로 파일을 찾는다.  

```
main.py  →  app.py  →  api.py  →  app/main.py  →  app/app.py  →  app/api.py
```

파일을 찾으면 그 안에서 앱 변수를 `app` → `api` 순으로 찾고,
없으면 FastAPI 인스턴스인 아무 변수나 집어 든다.  

> `__init__.py` 유무에 따라 import 경로가 달라진다.
>
> 같은 `app/main.py` 인데 결과가 다르다.
>
> ```
> app/__init__.py 있음  →  import string: app.main:app
> app/__init__.py 없음  →  import string: main:app
> ```
>
> 후자는 `app/` 디렉터리 자체를 경로에 넣고 `main` 만 import 한다.
> 이 상태에서 `from app.core.config import settings` 같은 **절대 import 를 쓰면 전부 깨진다.**  

Python 은 `__init__.py` 가 있어야 (전통적인 의미의) 패키지다.  
그래서 뒤에 나오는 예제 구조에서는 **모든 디렉터리에 빈 `__init__.py` 를 둔다.**  

```sh
touch app/__init__.py app/core/__init__.py app/domains/__init__.py ...
```

탐색에 의존하지 말고 실행할 파일이나 모듈을 직접 지정하는 게 안전하다.  

```sh
fastapi dev app/main.py          # fastapi CLI 는 파일 경로를 받는다
uvicorn app.main:app --reload    # uvicorn 은 "모듈:변수" 문자열을 받는다
```

경로를 직접 넘기면 출력에서 `(auto-discovered)` 표시가 사라지고 항상 같은 앱을 띄운다.  

### 실제 프로젝트로 커지는 순서

파일 하나로 시작해 단계적으로 쪼개면 된다. **처음부터 3단계로 갈 필요는 없다.**  

**1단계 — 파일 하나 (~100줄)**  

```
myapi/
├── .venv/
├── .gitignore
├── main.py
└── requirements.txt
```

**2단계 — 라우터 분리 (엔드포인트가 10개를 넘을 때쯤)**  

```
myapi/
├── app/
│   ├── __init__.py
│   ├── main.py            # FastAPI 인스턴스 + 라우터 등록
│   └── routers/
│       ├── __init__.py
│       └── users.py       # APIRouter
├── .env
└── requirements.txt
```

```py
# app/main.py
from fastapi import FastAPI
from app.routers import users

app = FastAPI()
app.include_router(users.router)    # 컴포넌트 스캔 대신 명시적 등록
```

**3단계 — 도메인·계층 분리**  
라우터와 비즈니스 로직이 커지고, 에러 응답을 통일해야 하는 시점.  
**이 글의 나머지가 다루는 구조가 여기다.**  

```
myapi/
├── app/
│   ├── main.py
│   ├── core/              # 설정·에러·로깅 등 공통 관심사
│   └── domains/user/      # 도메인별 schema/service/router
├── tests/
├── .env
├── docker-compose.yml
└── pyproject.toml
```

2단계에서 3단계로 넘어가는 신호는 대체로 이렇다.  

- 라우터 함수 안에 비즈니스 로직이 30줄씩 쌓인다 → `service` 분리  
- 같은 에러 응답을 여러 곳에서 `HTTPException` 으로 만들고 있다 → `core/errors.py` 분리  

## Pydantic 기본

Pydantic 은 **Python 타입 힌트를 실제 런타임 검증·변환 규칙으로 사용하는 라이브러리**다.  
FastAPI 없이도 사용할 수 있으며, Spring 기준으로는 `DTO + Bean Validation + Jackson` 역할에 가깝다.  
여기서는 **Pydantic v2, Python 3.11 이상**을 기준으로 설명한다.  

```sh
# 앞에서 만든 가상환경을 활성화한 상태에서 실행
python -m pip install "pydantic[email]>=2,<3" "pydantic-settings>=2,<3"
```

| 패키지 | 역할 |
| --- | --- |
| `pydantic` | `BaseModel`, 필드 검증, 변환, 직렬화 |
| `pydantic[email]` | 기본 `pydantic` + `EmailStr` 에 필요한 `email-validator` |
| `pydantic-settings` | 환경변수와 `.env` 를 읽는 `BaseSettings` |

`pydantic[email]` 은 별도 패키지 이름이 아니라 **기본 `pydantic` 에 `email` extra 를 추가하는 설치 표기**다.  
따라서 `pydantic` 을 다시 적거나 따로 설치할 필요가 없다. 이메일 검증이 필요 없으면 `[email]` 을 빼면 된다.  

```toml
[project]
dependencies = [
    # 기존 FastAPI 등 의존성은 유지
    "pydantic[email]>=2,<3",
    "pydantic-settings>=2,<3",
]
```

> **v2 에서 모든 import 경로가 바뀐 것은 아니다.**  
> 일반 모델 기능은 계속 `pydantic` 에서 가져오며, 환경설정용 `BaseSettings` 만 별도 패키지로 분리되었다.  
>
> ```py
> from pydantic import BaseModel, ConfigDict, Field
> from pydantic_settings import BaseSettings, SettingsConfigDict
> ```
>
> 참고: [설치](https://docs.pydantic.dev/latest/install/), [설정 관리](https://docs.pydantic.dev/latest/concepts/pydantic_settings/)  

처음에는 아래 항목만 알면 대부분의 요청·응답 DTO 를 만들 수 있다.  

| 종류 | 대표 API / 목차 | 역할 |
| --- | --- | --- |
| 외부 설정 | [`BaseSettings`, `SettingsConfigDict`](#BaseSettings) | 환경변수·`.env` 바인딩 |
| 모델 | [`BaseModel`](#BaseModel) | 구조가 있는 데이터 객체 선언 |
| 필드·검증기 | [`Field`, `Annotated`, 검증 데코레이터](#Field-Annotated-검증-데코레이터) | 선언적 제약과 사용자 정의 검증 |
| 설정 | [`ConfigDict`](#ConfigDict) | 해당 모델 상속 계층의 검증·변환 정책 |
| 타입 | [내장 타입](#내장-타입) — `EmailStr`, `HttpUrl`, `UUID`, `datetime` | 자주 쓰는 데이터 형식 검증 |
| 오류 | [`ValidationError`](#ValidationError) | 여러 검증 실패를 하나로 수집 |
| 어댑터 | [`TypeAdapter`](#TypeAdapter), [`RootModel`](#RootModel) | 모델 밖의 타입과 루트 컬렉션 검증 |
| 직렬화 | [직렬화 데코레이터](#직렬화-데코레이터) — `@field_serializer`, `@computed_field` | 출력값 변환과 계산 필드 |
| FastAPI | [요청·응답 모델 연결](#FastAPI-연결) | 요청 검증, OpenAPI 생성, 응답 필터링 |

### BaseSettings

`BaseSettings` 는 일반 요청 DTO 가 아니라 **환경변수와 `.env` 파일을 읽는 설정 모델**이다. Pydantic v2 에서는 `pydantic-settings` 패키지에서 가져온다.  

```py
# app/core/config.py
from functools import lru_cache
from typing import Annotated, Literal

from fastapi import Depends
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    # BaseSettings 가 환경설정을 어디서 읽고 어떻게 다룰지 정하는 클래스 단위 정책
    model_config = SettingsConfigDict(
        env_file=".env",       # 환경변수에 값이 없으면 .env 파일에서 읽음
        extra="ignore",        # 선언하지 않은 .env 항목은 무시
        frozen=True,           # 생성된 설정 객체의 값을 변경하지 못하게 함
    )

    PROJECT_NAME: str = "fastapi-user-sample"
    PROFILE: Literal["local", "dev", "prd"] = "local"
    API_PREFIX: str = "/api"
    LOG_LEVEL: Literal["DEBUG", "INFO", "WARNING", "ERROR"] = "INFO"


# 첫 호출에서 만든 Settings 를 캐시해 같은 프로세스에서는 계속 재사용
# 캐시는 프로세스끼리 공유되지 않으므로 uvicorn --workers 4 이면 워커마다 1개씩, 최대 4개 생성
# 함수로 감싸 두면 FastAPI 테스트에서 dependency_overrides 로 교체 가능
@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()


SettingsDep = Annotated[Settings, Depends(get_settings)]
```

`PROFILE: Literal["local", "dev", "prd"] = "local"` 은 생략하면 `local` 을 사용하고, 다른 값이 들어오면 설정 로딩을 실패시키는 검증 규칙이다.  
`PROFILE` 자체가 다른 설정 파일을 선택하는 것은 아니다. **실행 환경마다 필요한 값을 환경변수로 함께 주입**한다.  

```sh
# prd 환경의 실행 설정
PROFILE=prd \
API_PREFIX=/api \
LOG_LEVEL=INFO \
fastapi run
```

애플리케이션 코드와 `get_settings()` 는 환경별로 달라지지 않는다. 각 워커에서 `Settings()` 가 처음 생성될 때 현재 프로세스의 환경변수를 자동으로 읽는다.  

```text
dev 프로세스 환경변수 → Settings(PROFILE="dev", API_PREFIX="/dev-api", LOG_LEVEL="DEBUG")
prd 프로세스 환경변수 → Settings(PROFILE="prd", API_PREFIX="/api",     LOG_LEVEL="INFO")
```

로컬 개발에서만 `.env` 를 사용하고, dev·prd 서버에는 배포 도구의 환경변수나 Secret 기능으로 값을 주입한다.  
`.env` 와 환경별 비밀값 파일은 저장소나 컨테이너 이미지에 포함하지 않는다.  

값은 **환경변수 > `.env` > 필드 기본값** 순으로 바인딩된다. 타입이 맞지 않으면 `Settings()` 생성 시 검증 오류가 발생한다.  
`SettingsDep` 로 FastAPI 의존성에 연결해 두면 사용하는 라우터나 서비스에서는 설정 객체를 함수 인자로 받을 수 있다.  

테스트에서는 캐시된 전역 객체를 직접 수정하지 않고 의존성을 교체한다.  

```py
app.dependency_overrides[get_settings] = lambda: Settings(PROFILE="local")
```

### BaseModel

일반 Python 클래스의 타입 힌트만으로는 입력값이 자동 검증되지 않는다.  
`BaseModel` 을 상속해야 객체 생성 시 **검증, 타입 변환, 중첩 객체 생성, 직렬화**가 적용된다.  

```py
from pydantic import BaseModel


class Address(BaseModel):
    city: str
    zip_code: str


class Product(BaseModel):
    name: str
    price: int
    manufacturer_address: Address


product = Product.model_validate({
    "name": "키보드",
    "price": "30000",
    "manufacturer_address": {"city": "서울", "zip_code": "04524"},
})

assert product.price == 30000       # 기본 모드에서는 문자열을 int 로 변환
assert isinstance(product.manufacturer_address, Address)  # 내부 dict 도 모델로 변환
assert product.model_dump() == {
    "name": "키보드",
    "price": 30000,
    "manufacturer_address": {"city": "서울", "zip_code": "04524"},
}
```

**`str | None` 은 null 허용이지 필드 생략 허용이 아니다.**  

| 선언 | 생략 가능 | `None` 허용 |
| --- | --- | --- |
| `name: str` | 아니오 | 아니오 |
| `name: str = "guest"` | 예 | 아니오 |
| `name: str \| None` | 아니오 | 예 |
| `name: str \| None = None` | 예 | 예 |
| `name: Optional[str]` | 아니오 | 예 |
| `name: Optional[str] = None` | 예 | 예 |

위 예제처럼 `model_validate()` 에 dict 를 전달하면 기본 타입뿐 아니라 중첩된 dict 도 선언한 `BaseModel` 타입으로 변환된다.  

#### ConfigDict 

`ConfigDict` 는 필드 하나가 아니라 **설정을 선언한 모델과 이를 상속한 하위 모델에 적용할 정책**을 지정한다.  
`BaseModel` 을 상속한 클래스에 `model_config` 를 선언하면 바로 해당 모델과 하위 모델의 정책이 된다.  

```py
from pydantic import ConfigDict


class StrictApiModel(BaseModel):
    model_config = ConfigDict(
        extra="forbid",              # 선언하지 않은 입력 필드 거부
        str_strip_whitespace=True,   # 모든 문자열 앞뒤 공백 제거
        validate_assignment=True,    # 생성 후 속성 대입도 검증
    )
```

다만 설정 코드가 객체마다 실행되는 것은 아니다. `StrictApiModel` 클래스가 정의되는 시점에 `BaseModel` 의 메타클래스가 다음 작업을 한다.  

1. 타입 힌트를 읽어 필드와 검증 스키마를 만든다.  
2. 이름이 정해진 클래스 속성 `model_config` 를 읽어 스키마에 정책을 반영한다.  
3. 이후 객체를 생성하거나 값을 대입할 때 미리 만들어 둔 검증기를 사용한다.  

즉 `model_config` 는 Pydantic 이 특별히 인식하는 **클래스 설정값**이다. 일반 데이터 필드가 아니므로 생성자 인자나 `model_dump()` 결과에는 포함되지 않는다.  

```py
class User(StrictApiModel):
    name: str
    age: int


user = User(name="  홍길동  ", age="20")

assert user.name == "홍길동"          # str_strip_whitespace 적용
assert user.age == 20               # Pydantic 기본 타입 변환
assert "model_config" not in user.model_dump()

user.age = "30"
assert user.age == 30               # validate_assignment 적용

# User(name="홍길동", age=20, unknown=True)
# → extra="forbid" 때문에 ValidationError
```

`StrictApiModel` 을 다시 상속한 `User` 에도 설정이 적용되는 이유는 `model_config` 가 **자식 모델로 상속**되기 때문이다.  
따라서 공통 베이스 모델 하나에 프로젝트 정책을 모아 두고 모든 요청·응답 모델이 이를 상속하게 만들 수 있다.  

> **`model_config` 는 애플리케이션 전역 설정이 아니다.**  
> 설정을 선언한 모델과 그 모델을 상속한 자식들에게만 적용되는 **클래스·상속 계층 단위 설정**이다.  
> 같은 프로세스 안에서도 서로 다른 베이스 모델 계층은 각자의 설정을 사용할 수 있다.  

```py
class FlexiblePayload(StrictApiModel):
    # StrictApiModel 의 나머지 설정은 상속하고 extra 만 변경
    model_config = ConfigDict(extra="ignore")

    name: str


class ExternalPayload(BaseModel):
    # StrictApiModel 을 상속하지 않으므로 StrictApiModel 설정과 무관
    model_config = ConfigDict(strict=True)

    name: str
```

`FlexiblePayload` 처럼 자식 모델에 `model_config` 를 다시 선언하면 부모 설정을 기반으로 해당 항목을 덮어쓴다.  
반면 `ExternalPayload` 는 `BaseModel` 에서 시작하는 별도 계층이므로 자신이 선언한 설정만 적용된다.  
어느 모델의 설정이 다른 모델에 이름만으로 전파되거나 전역으로 등록되는 일은 없다.  
단, `BaseModel` 을 상속하지 않은 일반 Python 클래스에서는 Pydantic 이 개입하지 않으므로 같은 이름의 속성을 적어도 아무 효과가 없다.  

| 설정 | 의미 |
| --- | --- |
| `extra="ignore"` | 선언하지 않은 입력 필드를 무시, 기본값 |
| `extra="forbid"` | 선언하지 않은 입력 필드를 오류 처리 |
| `strict=True` | `"20"` → `20` 같은 자동 타입 변환 금지 |
| `frozen=True` | 생성 후 값 변경 금지 |
| `populate_by_name=True` | alias 와 Python 필드명을 모두 입력에 허용 |
| `validate_default=True` | 필드 기본값도 검증 |
| `validate_assignment=True` | 객체 생성 후 속성 변경도 검증 |

이 중 `strict=True` 는 다른 설정과 달리 **검증 규칙이 아니라 타입 변환 정책 자체**를 바꾼다.  
Pydantic 의 기본값은 lax 모드로, 입력 편의를 위해 `"20"` 을 `20` 으로 변환한다. 엄격 모드에서는 이 변환을 하지 않고 선언한 타입과 다르면 바로 오류다.  

```py
class LaxUser(BaseModel):
    id: int


class StrictUser(BaseModel):
    model_config = ConfigDict(strict=True)

    id: int


assert LaxUser(id="1").id == 1     # lax 모드: 문자열을 정수로 변환

# StrictUser(id="1")
# → ValidationError: Input should be a valid integer
```

폼 입력이나 쿼리 문자열처럼 값이 원래 문자열로 전달되는 입력에는 기본 모드가 편리하다. 반대로 캐시·메시지 큐·외부 API 처럼 **JSON 타입까지 계약으로 정해 둔 데이터**라면 엄격 모드가 계약 위반을 그대로 드러내 준다.  
**사용자 입력의 편리한 변환이 목적이면 기본 모드**, **저장된 데이터의 계약 확인이 목적이면 엄격 모드**를 우선 고려한다.  

엄격 모드는 모델 전체가 아니라 더 좁은 범위에도 적용할 수 있다.  

| 적용 범위 | 방법 |
| --- | --- |
| 모델 계층 전체 | `model_config = ConfigDict(strict=True)` |
| 필드 하나 | `id: int = Field(strict=True)` 또는 `Annotated[int, Strict()]` |
| 호출 한 번 | `User.model_validate(data, strict=True)` |

> JSON 입력에는 예외가 있다. JSON 에는 날짜나 UUID 타입이 없으므로, 엄격 모드에서도 `model_validate_json()` 으로 들어온 `"2024-07-07T00:00:00Z"` 같은 문자열은 `datetime` 으로 변환된다.  
> 반면 FastAPI 의 경로·쿼리·폼 파라미터는 항상 문자열로 들어오므로, 그 값을 받는 모델에 `strict=True` 를 걸면 정수·불리언 필드가 전부 실패한다. 엄격 모드는 본문(JSON)이나 내부 데이터 검증 쪽에 적용한다.  

실제 프로젝트에서는 모든 요청·응답 DTO 가 상속할 공통 모델에 반복 정책을 모아 둘 수 있다.  

```py
# app/core/schema.py
class ApiModel(BaseModel):
    """모든 요청·응답 DTO 의 공통 베이스."""

    model_config = ConfigDict(
        extra="ignore",             # 알 수 없는 입력 필드 무시
        populate_by_name=True,       # alias 와 Python 필드명 모두 입력 허용
        str_strip_whitespace=True,   # 모든 문자열 앞뒤 공백 제거
    )
```

`ApiModel` 을 상속한 DTO 에만 이 정책이 적용된다. 별개의 `BaseModel` 상속 계층에는 영향을 주지 않는다.  

| Jackson 설정 | Pydantic |
| --- | --- |
| `FAIL_ON_UNKNOWN_PROPERTIES=false` | `extra="ignore"` |
| `PropertyNamingStrategies.SNAKE_CASE` | `alias_generator=to_snake` |
| `JsonInclude.Include.NON_NULL` | 직렬화 시 `exclude_none=True` |
| `WRITE_DATES_AS_TIMESTAMPS=false` | 기본값이 ISO-8601 |

Python 필드명과 JSON 필드명을 모두 snake_case 로 사용한다면 `alias_generator` 는 필요 없다.  

v1 의 내부 `class Config` 예제가 많이 남아 있지만, v2 에서는 `model_config = ConfigDict(...)` 를 사용한다.  
참고: [모델 설정](https://docs.pydantic.dev/latest/concepts/config/)  

### Field, Annotated

`Field` 는 필드의 **기본값, 범위·길이 제약, alias, 문서 설명**을 선언한다.  

```py
class Example(BaseModel):
      a: str                       # 필수, None 불가
      b: str | None                # 필수, 전달값으로 None 가능
      c: str | None = None         # 선택, 생략하면 None
      d: str = "anonymous"         # 선택, 생략하면 문자열 기본값
      e: int = Field(default=0, ge=0)
```

```py
from pydantic import BaseModel, Field


class UserCreate(BaseModel):
    name: str = Field(min_length=1, max_length=50, description="사용자 이름")
    age: int = Field(ge=0, le=150)
    nickname: str | None = None
    tags: list[str] = Field(default_factory=list)
```

- `gt` / `ge`: 초과 / 이상  
- `lt` / `le`: 미만 / 이하  
- `min_length` / `max_length`: 문자열 길이 또는 컬렉션 항목 수  
- `pattern`: 문자열 정규식  
- `alias`: 입력·출력에 사용할 외부 필드명  
- `default_factory`: 리스트, 현재 시각처럼 호출해서 만들 기본값  

반복되는 제약은 `Annotated` 타입으로 이름을 붙여 실제 요청·응답 DTO 에서 재사용할 수 있다.  
`Annotated` 는 Python 표준 타입 힌트다. Pydantic 이 안쪽의 `Field` 메타데이터를 읽어 검증한다.  

```py
# app/domains/user/schema.py
from datetime import datetime
from typing import Annotated

from pydantic import EmailStr, Field

from app.core.schema import ApiModel


UserName = Annotated[str, Field(min_length=1, max_length=50)]
PositiveId = Annotated[int, Field(gt=0)]
UserAge = Annotated[int, Field(ge=0, le=150)]
Introduction = Annotated[str, Field(max_length=500)]


class UserRequest(ApiModel):
    name: UserName
    email: EmailStr
    age: UserAge
    introduction: Introduction | None = None


class UserResponse(ApiModel):
    id: PositiveId
    created_at: datetime
    updated_at: datetime | None = None
    name: UserName
    email: EmailStr
    age: UserAge
    introduction: Introduction | None = None
```

`UserName`, `UserAge`, `Introduction` 은 요청과 응답 양쪽에서 같은 제약을 반복하지 않게 하고, `PositiveId` 는 서버가 반환하는 ID가 양수인지 검증한다.  

`UserRequest` 는 클라이언트가 입력할 필드만 받고, `UserResponse` 는 서버가 만든 `id` 와 시간 필드까지 포함한다.  
두 모델을 분리하면 클라이언트가 서버 전용 필드를 입력하는 문제를 막고 OpenAPI 의 요청·응답 스키마도 정확하게 나뉜다.  

Pydantic 은 표준 타입 외에도 자주 쓰는 형식을 검증하는 타입을 제공한다.  

```py
from datetime import datetime
from uuid import UUID

from pydantic import EmailStr, HttpUrl


class Account(BaseModel):
    id: UUID
    email: EmailStr
    homepage: HttpUrl | None = None
    joined_at: datetime
```

JSON 문자열을 넣어도 검증 후 `UUID`, `HttpUrl`, `datetime` 객체로 변환된다.  
`EmailStr` 를 사용하려면 설치 시 `pydantic[email]` extra 가 필요하다.  
그 밖에 IP 주소, 날짜, Decimal, Enum 등 Python 표준 타입도 대부분 바로 검증할 수 있다.  

### 검증 데코레이터

문자열을 정리하거나 여러 필드의 관계를 확인하는 것처럼 코드가 필요한 규칙만 **검증 데코레이터**로 작성한다.  

- `@field_validator`: 한 필드 또는 여러 필드에 각각 적용할 규칙  
- `@model_validator`: 모델 전체를 보고 필드 사이의 관계를 검사할 규칙  

```py
from typing import Self

from pydantic import field_validator, model_validator


class Reservation(BaseModel):
    title: str
    start_hour: int = Field(ge=0, le=23)
    end_hour: int = Field(ge=1, le=24)
    participants: list[str] = Field(min_length=1)

    @field_validator("participants", mode="before")
    @classmethod
    def split_participants(cls, value: object) -> object:
        # 타입 검증 전에 "kim, lee" 형식의 문자열을 list[str] 로 변환
        if isinstance(value, str):
            return [name.strip() for name in value.split(",") if name.strip()]
        return value

    @field_validator("title")  # mode="after" 가 기본값
    @classmethod
    def validate_title(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("제목은 비어 있을 수 없습니다.")
        return value

    @model_validator(mode="after")
    def validate_hours(self) -> Self:
        if self.start_hour >= self.end_hour:
            raise ValueError("종료 시간은 시작 시간보다 뒤여야 합니다.")
        return self
```

위 예제에 `participants="kim, lee"` 를 입력하면 `split_participants()` 가 타입 검증 전에 `["kim", "lee"]` 로 바꾼다. 그다음 변환된 값이 실제 `list[str]` 인지와 `min_length=1` 조건을 검증한다.  

`field_validator` 의 위치는 고정되어 있지 않다. **`mode` 를 생략하면 기본값은 `mode="after"`** 이며, 필요하면 `mode="before"` 로 앞당긴다.  
모델 전체의 흐름까지 포함하면 일반적인 실행 순서는 다음과 같다.  

1. `@model_validator(mode="before")` — 모델의 원시 입력 전체  
2. `@field_validator(..., mode="before")` — 해당 필드의 원시 입력  
3. Pydantic 타입 변환과 `Field(...)` 제약 검증  
4. `@field_validator(...)` — 기본값인 `mode="after"`, 변환이 끝난 필드값  
5. `@model_validator(mode="after")` — 모든 필드 검증이 끝난 모델 객체  

| 모드 | 전달받는 값 | 주 사용 목적 |
| --- | --- | --- |
| `field_validator(mode="before")` | 타입 변환 전 해당 필드의 원시 입력 | 입력 형식 정리·변환 |
| `field_validator(mode="after")` | 타입과 `Field` 검증을 통과한 필드값 | 일반적인 필드 규칙 검사, 기본 모드 |
| `model_validator(mode="before")` | 모델로 들어온 원시 입력 전체 | 여러 입력 필드의 전처리 |
| `model_validator(mode="after")` | 모든 필드 검증을 통과한 모델 객체 | 필드 사이의 관계 검사 |

검증에 성공하면 `field_validator` 는 처리한 값을, `model_validator(mode="after")` 는 `self` 를 반드시 반환한다.  
실패할 때 `False` 를 반환하는 것이 아니라 `ValueError` 를 발생시켜야 하며, 오류는 다른 타입·`Field` 오류와 함께 `ValidationError` 에 수집된다.  
검증기는 데이터 형태와 값만 검사하고 외부 API 호출이나 저장 같은 I/O·비즈니스 로직은 서비스 계층에 둔다.  

즉 검증 데코레이터에는 두 역할이 있다.  

1. **변환·정규화** — 값을 수정해서 반환한다. 예: `"kim, lee"` 를 `["kim", "lee"]` 로 변환  
2. **검증·거부** — 허용할 수 없는 값이면 `ValueError` 를 발생시킨다.  

검증기가 HTTP 응답을 직접 반환하는 것은 아니다. Pydantic 이 `ValueError` 를 `ValidationError` 에 모으고, FastAPI 가 요청 검증 중 발생한 오류를 `RequestValidationError` 로 처리해 기본 422 응답을 만든다.  

```text
validator 에서 ValueError
→ Pydantic ValidationError
→ FastAPI RequestValidationError
→ HTTP 422 응답
```

**필드를 생략해 기본값이 선택되면 그 필드의 검증은 기본적으로 실행되지 않는다.**  
여기에는 타입 변환, `Field` 제약과 `field_validator` 가 포함된다. 반대로 입력값을 명시적으로 전달하면 기본값과 같은 값이라도 정상적으로 검증한다.  

```py
class User(BaseModel):
    name: str = "  guest  "

    @field_validator("name")
    @classmethod
    def strip_name(cls, value: str) -> str:
        return value.strip()


assert User().name == "  guest  "          # 필드 생략 → 기본값을 그대로 사용
assert User(name="  guest  ").name == "guest"  # 직접 입력 → 검증기 실행
```

기본값에도 타입·제약·검증기를 적용하려면 `validate_default=True` 를 지정한다.  

```py
class User(BaseModel):
    name: str = Field(default="  guest  ", validate_default=True)

    @field_validator("name")
    @classmethod
    def strip_name(cls, value: str) -> str:
        return value.strip()


assert User().name == "guest"  # 생략해도 기본값에 검증기 적용
```

모든 필드의 기본값을 검증하려면 모델 설정에 `ConfigDict(validate_default=True)` 를 사용할 수도 있다.  
참고: [필드](https://docs.pydantic.dev/latest/concepts/fields/)  

### ValidationError

검증이 실패하면 Pydantic 이 모든 오류를 모아 `ValidationError` 를 발생시킨다.  
검증기에서 이 예외를 직접 만들기보다는 `ValueError` 를 발생시키면 Pydantic 이 수집한다.  

```py
from pydantic import ValidationError

try:
    UserCreate(name="", age=-1)
except ValidationError as exc:
    print(exc.error_count())
    print(exc.errors())       # loc: 위치, msg: 설명, type: 오류 종류
    print(exc.json())
```

Pydantic 을 직접 호출하면 `ValidationError` 가 발생한다. FastAPI 의 요청 검증 실패는 이를 감싼 `RequestValidationError` 로 처리되어 기본적으로 422 응답이 된다.  

### TypeAdapter  

`TypeAdapter` 는 **`BaseModel` 로 감싸지 않은 타입에도 Pydantic 검증을 적용하게 해 주는 도구**다.  

`list[User]` 는 Python 타입 힌트이므로 그 자체에는 `model_validate()` 같은 Pydantic 메서드가 없다. `TypeAdapter` 는 이런 **임의의 타입에 Pydantic 의 검증·직렬화 기능을 연결**한다.  

`TypeAdapter(list[User])` 를 만들면 최상위 리스트에 검증 메서드가 생기고, 그 안의 각 `User` 에 선언된 타입, `Field`, `Annotated`, `@field_validator`, `@model_validator` 가 모두 실행된다.  

```py
# User는 BaseModel이므로 검증 메서드가 있다.
user = User.model_validate_json(user_json)

# TypeAdapter가 list[User] 전체에 검증 메서드를 제공한다. 그리고 User List 로 응답
users = TypeAdapter(list[User]).validate_json(users_json)
```

실제 API에서는 **라우팅 함수 안에서 직접 가져온 데이터를 응답하기 전에 사용해야 할 때** 활용할 수 있다. 예를 들어 Redis나 외부 API에서 가져온 값으로 비즈니스 로직을 수행하려면 먼저 신뢰할 수 있는 객체로 검증하는 편이 안전하다.  

```py
from typing import Annotated

from fastapi import FastAPI
from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    TypeAdapter,
    field_validator,
)

app = FastAPI()

UserId = Annotated[int, Field(gt=0)]
UserName = Annotated[str, Field(min_length=2, max_length=50)]


class User(BaseModel):

    id: UserId
    name: UserName

    @field_validator("name")
    @classmethod
    def name_must_not_be_admin(cls, value: str) -> str:
        if value.lower() == "admin":
            raise ValueError("admin은 사용자 이름으로 사용할 수 없습니다")
        return value


# 타입 분석 비용이 반복되지 않도록 애플리케이션 로딩 시 한 번 생성한다.
UserListAdapter = TypeAdapter(list[User])


def read_users_from_cache() -> bytes:
    # 실제 코드에서는 redis.get("users") 등의 결과라고 가정한다.
    return b'[{"id":1,"name":"kim"},{"id":2,"name":"lee"}]'


@app.get("/users", response_model=list[User])
def get_users():
    cached_json = read_users_from_cache()

    # 캐시의 JSON을 파싱하면서 동시에 list[User]로 검증·변환한다.
    users = UserListAdapter.validate_json(cached_json)
    return users
```

`TypeAdapter`는 먼저 최상위 값이 리스트인지 확인하고, 각 원소를 `User`로 변환한다. 이 과정에서 `id`의 `Field(gt=0)`, `name`의 `Annotated` 길이 제약, `name_must_not_be_admin()` 검증기가 각 사용자마다 모두 실행된다.  

캐시 데이터를 이용해 계산하거나 서비스 계층으로 넘기는 등 **응답을 만들기 전에 검증된 `list[User]`가 필요할 때**, 또는 FastAPI 라우팅 밖의 일반 함수에서도 같은 검증을 사용하려면 `TypeAdapter`가 유용하다. 

물론 `TypeAdapter`가 필수인 것은 아니다. JSON 배열을 직접 파싱한 뒤 `User.model_validate()`를 반복 호출하거나, 
`for`문으로 각 항목을 검증하는 방식이 더 적합할 때도 있다.  

| 방식 | 적합한 상황 |
| --- | --- |
| `TypeAdapter(list[User])` | 목록 전체가 유효해야 처리하며, 오류 위치를 `0.id`, `2.name`처럼 목록 인덱스와 함께 수집하고 싶을 때 |
| `for`문 `User.model_validate()` | 첫 오류에서 즉시 중단하거나, 실패한 항목만 제외하거나, 항목별 로그·재시도 같은 별도 처리가 필요할 때 |

### RootModel

`RootModel`은 필드 이름 없이 **최상위 값 자체가 모델의 대상**이다. `{"ids": [1, 2, 3]}` 이 아니라 `[1, 2, 3]` 같은 JSON 을 그대로 받는 모델을 만들 때 사용하며, 실제 값은 `root` 속성으로 꺼낸다.  

예를 들어 관리자가 여러 사용자를 한 번에 삭제하는 API의 요청 본문이 다음과 같은 최상위 배열이라고 하자.  

```json
[1, "2", 999]
```

FastAPI는 `list[int]` 선언만으로 각 원소를 정수로 검증·변환한 뒤 함수에 전달한다. 따라서 함수 안에서 받는 `ids`는 `[1, 2, 999]`다.  

```py
from fastapi import FastAPI

app = FastAPI()

# 데이터베이스 대신 사용하는 예제 데이터
users = {
    1: {"id": 1, "name": "kim"},
    2: {"id": 2, "name": "lee"},
    3: {"id": 3, "name": "park"},
}


@app.post("/users/bulk-delete")
def bulk_delete(ids: list[int]):
    deleted_ids = []

    for user_id in ids:
        if users.pop(user_id, None) is not None:
            deleted_ids.append(user_id)

    return {
        "requested_ids": ids,
        "deleted_ids": deleted_ids,
    }
```

`999`는 올바른 정수이므로 요청 검증은 통과하지만 저장소에 해당 사용자가 없어 삭제 결과에서는 제외된다. 반대로 `[1, "abc"]`를 보내면 `"abc"`를 정수로 변환할 수 없으므로 라우팅 함수가 실행되기 전에 FastAPI가 `422 Unprocessable Entity`를 응답한다. 이 경우 FastAPI가 내부적으로 Pydantic을 사용하므로 개발자가 `TypeAdapter(list[int])`를 별도로 호출할 필요는 없다.  

이 API를 `RootModel`로 표현하려면 매개변수 타입만 다음처럼 바꿀 수 있다.  

```py
from pydantic import RootModel


class UserIds(RootModel[list[int]]):
    def contains(self, user_id: int) -> bool:
        return user_id in self.root


@app.post("/users/bulk-delete")
def bulk_delete(ids: UserIds):
    if ids.contains(1):
        print("관리자 계정이 요청에 포함되어 있다")

    deleted_ids = [
        user_id
        for user_id in ids.root
        if users.pop(user_id, None) is not None
    ]
    return {"deleted_ids": deleted_ids}
```

요청 JSON은 여전히 `[1, "2", 999]`이지만, 함수에는 단순 리스트 대신 `UserIds` 객체가 전달된다. 실제 목록은 `ids.root`로 꺼내고, 모델에 정의한 `contains()` 같은 메서드를 그대로 사용할 수 있다.  
`list[int]` 선언일 때와 마찬가지로 각 원소는 정수로 검증·변환되므로 `ids.root`는 `[1, 2, 999]`다.  

라우팅 밖에서도 같은 모델을 재사용한다.  

```py
user_ids = UserIds.model_validate(["1", 2, 3])
assert user_ids.root == [1, 2, 3]
assert user_ids.contains(2)
```

따라서 단순히 배열을 받는다는 이유만으로 `RootModel`을 만들 필요는 없다. 동일한 최상위 배열 타입을 여러 곳에서 재사용하거나, 타입에 이름을 붙여 API 스키마를 구분하거나, 위의 `contains()`처럼 전용 검증기·메서드를 추가할 때 사용한다. 대부분의 일반적인 CRUD 애플리케이션에서는 `RootModel`을 한 번도 사용하지 않을 수도 있다.  

정리하면 **필드가 있는 객체는 `BaseModel`, 별도 모델 없이 임의 타입을 검증할 때는 `TypeAdapter`, 최상위 배열·단일 값을 이름 있는 모델로 만들 때는 `RootModel`** 을 선택한다.  

### 직렬화 데코레이터

`@field_serializer` 는 특정 필드의 출력 형태를 바꾸고, `@computed_field` 는 저장된 값으로 계산한 필드를 출력에 추가한다.  

```py
from datetime import datetime, timezone

from pydantic import computed_field, field_serializer


class Order(BaseModel):
    price: int
    quantity: int
    created_at: datetime

    @computed_field
    @property
    def total_price(self) -> int:
        return self.price * self.quantity

    @field_serializer("created_at")
    def serialize_created_at(self, value: datetime) -> str:
        return value.astimezone(timezone.utc).isoformat().replace("+00:00", "Z")
```

단순히 JSON 호환 형태로 바꾸는 목적이라면 먼저 `model_dump(mode="json")` 또는 `model_dump_json()` 으로 충분한지 확인한다.  
커스텀 직렬화는 API 계약상 별도 출력 형식이 필요할 때만 사용한다.  

| 메서드 | 결과 |
| --- | --- |
| `Model.model_validate(data)` | dict 등의 입력을 검증해 모델 생성 |
| `Model.model_validate_json(text)` | JSON 문자열·바이트를 검증해 모델 생성 |
| `model.model_dump()` | Python 타입을 유지한 dict 반환 |
| `model.model_dump(mode="json")` | JSON 호환 값으로 구성된 dict 반환 |
| `model.model_dump_json()` | JSON 문자열 반환 |
| `Model.model_json_schema()` | JSON Schema 반환 |

`exclude_none=True` 는 값이 `None` 인 필드를, `exclude_unset=True` 는 입력에서 생략한 필드를 제외한다.  
PATCH 요청에서는 생략과 명시적인 null 을 구분하기 위해 `exclude_unset=True` 가 중요하다.  

```py
class UserPatch(BaseModel):
    nickname: str | None = None


assert UserPatch().model_dump(exclude_unset=True) == {}
assert UserPatch(nickname=None).model_dump(exclude_unset=True) == {"nickname": None}
```

> v1 의 `.dict()`, `.json()`, `parse_obj()` 대신 v2 에서는 `model_dump()`, `model_dump_json()`, `model_validate()` 를 사용한다.  
> 참고: [직렬화](https://docs.pydantic.dev/latest/concepts/serialization/)  

## FastAPI - CRUD

```py
from fastapi import FastAPI

app = FastAPI()


class UserResponse(BaseModel):
    name: str
    age: int


@app.post("/users", response_model=UserResponse)
def create_user(request: UserCreate) -> UserResponse:
    return UserResponse(name=request.name, age=request.age)
```

`request: UserCreate` 로 JSON body 바인딩·검증이 적용되고 모델 스키마가 OpenAPI 에 반영된다.  
`response_model` 은 반환 데이터를 다시 검증하고 선언된 필드만 응답에 포함한다.  
요청 검증 실패는 기본적으로 422 응답이지만, 서비스 내부의 `ValidationError` 나 응답 검증 실패까지 모두 요청 오류가 되는 것은 아니다.  
응답에서 값이 `None` 인 필드를 일관되게 제외하려면 공통 `APIRouter` 에 정책을 둘 수 있다.  

```py
# app/core/routing.py
from fastapi import APIRouter

_BODYLESS_STATUS = {204, 304}


class ApiRouter(APIRouter):
    def add_api_route(self, path, endpoint, **kwargs):
        status_code = kwargs.get("status_code")
        if status_code is not None and int(status_code) in _BODYLESS_STATUS:
            kwargs["response_model"] = None
        else:
            kwargs["response_model_exclude_none"] = True
        super().add_api_route(path, endpoint, **kwargs)
```

이후 각 도메인에서 `APIRouter` 대신 `ApiRouter` 를 사용하면 `exclude_none` 을 라우트마다 반복하지 않아도 된다.  
`204`, `304` 는 응답 본문이 없어야 하므로 `response_model` 자체를 제거한다.    

위 객체들을 조합하면 설정과 도메인 스키마를 프로젝트 파일로 분리해 사용할 수 있다.  

### 자주 쓰는 데코레이터

요청·응답을 다루는 데코레이터는 대부분 `app`(또는 `APIRouter`) 인스턴스의 메서드다.  
공통점은 **함수를 정의하는 시점에 등록만 하고 원래 함수는 그대로 돌려준다**는 점이다.  
요청을 감싸 실행하는 래퍼가 아니라, FastAPI 가 내부 테이블에 핸들러를 등록해 두었다가 조건이 맞을 때 호출한다.  

| 데코레이터 | 역할 | 호출 시점 |
| --- | --- | --- |
| `@app.get/post/put/patch/delete` | 경로 작동(엔드포인트) 등록 | 경로·메서드가 매칭되는 요청마다 |
| `@app.middleware("http")` | 모든 요청·응답을 앞뒤로 감싸는 훅 | 매 요청, 라우팅 전후 |
| `@app.exception_handler(Exc)` | 예외를 정해진 응답으로 변환 | 예외가 밖으로 전파될 때 |
| [`@field_validator` / `@model_validator`](#검증-데코레이터) | 요청 DTO 값 검증·변환 | 모델 생성(요청 파싱) 시 |
| [`@field_serializer` / `@computed_field`](#직렬화-데코레이터) | 응답 DTO 출력값 조정 | 직렬화(응답 생성) 시 |

아래 둘은 데코레이터 형태는 아니지만 같은 흐름에서 자주 함께 쓴다.  

- `Depends(...)` — 요청 단위 의존성 주입(인증, DB 세션, 서비스 객체)  
- `lifespan` — 앱 시작·종료 시 한 번 실행하는 컨텍스트 매니저(구 `@app.on_event`)  

#### 경로 작동 데코레이터

`@router.post("/users")` 하나에 요청 바인딩, 응답 필터링, 상태코드, 문서화가 모두 인자로 붙는다.  

```py
@router.post(
    "",                                    # 경로 (prefix 뒤에 붙음)
    response_model=UserResponse,           # 출력 필터 + OpenAPI 응답 스키마
    status_code=201,                       # 성공 시 상태코드
    tags=["user"],                         # OpenAPI 그룹
    summary="사용자 생성",                   # 문서 제목
    dependencies=[Depends(verify_token)],  # 반환값은 안 쓰고 부수효과(인증)만 필요할 때
    responses={409: {"model": ErrorBody}}, # 에러 응답 스키마 문서화
)
async def create_user(service: UserServiceDep, request: UserRequest) -> UserResponse:
    return await service.create(request)
```

| 인자 | 역할 |
| --- | --- |
| `response_model` | 반환 객체를 다시 검증하고 선언된 필드만 직렬화 |
| `status_code` | 성공 응답의 기본 상태코드 (`201`, `204` 등) |
| `tags` | Swagger UI 에서 엔드포인트를 묶는 그룹 |
| `dependencies` | 반환값을 안 쓰는 의존성(인증·권한 검사)만 실행 |
| `responses` | 200 이외 응답의 스키마·설명을 OpenAPI 에 추가 |
| `response_model_exclude_none` | 값이 `None` 인 필드를 응답에서 제외 |

`@app.get` 은 `app` 에 바로, `@router.get` 은 `APIRouter` 에 등록한다.  
앞서 본 `ApiRouter` 로 `response_model_exclude_none` 같은 공통 기본값을 한 번에 걸어 두면 라우트마다 반복하지 않아도 된다.  

### 요청 모델 · @router.get / post

라우터는 요청을 DTO 로 받고, 서비스를 호출하고, 응답 모델과 상태코드를 정하는 역할만 맡긴다.  

```py
@app.get("/users")
async def list_users(
    page: Annotated[int, Query(ge=1)] = 1,
    size: Annotated[int, Query(ge=1, le=100)] = 20,
):
    return {"page": page, "size": size}
```

같은 규칙을 여러 API 에서 사용한다면 `Annotated` 를 타입 별칭으로 미리 정의한다.  

```py
# app/core/params.py
from typing import Annotated

from fastapi import Query


PageNumber = Annotated[int, Query(ge=1)]
PageSize = Annotated[int, Query(ge=1, le=100)]
```

```py
# app/domains/user/router.py
from app.core.params import PageNumber, PageSize


router = ApiRouter(prefix="/users", tags=["user"])


@router.post("", response_model=UserResponse, status_code=201)
async def create_user(service: UserServiceDep, request: UserRequest) -> UserResponse:
    return await service.create(request)


@router.get("", response_model=list[UserResponse])
async def list_users(
    service: UserServiceDep,
    page: PageNumber = 1,
    size: PageSize = 20,
) -> list[UserResponse]:
    return await service.find_all(page, size)


@router.get("/{user_id}", response_model=UserResponse)
async def get_user(service: UserServiceDep, user_id: str) -> UserResponse:
    return await service.get(user_id)


@router.put("/{user_id}", response_model=UserResponse)
async def update_user(
    service: UserServiceDep, user_id: str, request: UserRequest
) -> UserResponse:
    return await service.update(user_id, request)


@router.delete("/{user_id}", status_code=204)
async def delete_user(service: UserServiceDep, user_id: str) -> None:
    await service.delete(user_id)
```

`PageNumber` 와 `PageSize` 는 타입 별칭이라 여러 라우터에서 반복해서 사용할 수 있다.  
`Query(...)` 는 쿼리 파라미터라는 정보와 검증 규칙을 담고, `= 1`, `= 20` 은 해당 엔드포인트의 기본값을 정한다.  
같은 `PageSize` 를 사용하면서 다른 엔드포인트에는 `size: PageSize = 50` 처럼 기본값만 다르게 둘 수도 있다.  

추가로 배열 param 을 **콤마로 묶어 문자열 하나로 보내는 방식**으로 받을때 직접 쪼개야 한다.  
`?tags=park&tags=outdoor` 처럼 같은 이름을 여러 번 보내면 `tags: list[str]` 로 바로 받지만,
`?tags=park,outdoor` 처럼 콤마로 묶어 보내면 FastAPI 는 값 하나로 취급해 `["park,outdoor"]` 가 된다.
이 규약을 쓴다면 문자열로 받아 직접 분리한다.  

```py
@app.get("/products")
async def list_products(tags: Annotated[str | None, Query()] = None):
    tag_list = [t.strip() for t in tags.split(",") if t.strip()] if tags else []
    return {"tags": tag_list}
```

#### multipart/form-data · Form, File

`Form` 을 사용할땐 Annotated 방식을 사용하는것을 권장한다.  
코드 인텔리전스에서 기본타입을 지정할수 있어 효율적인 개발이 가능하다.  

```py
  # 일반 방식
  name: str = Form(min_length=1, max_length=50)
  bio: str | None = Form(None)
  file: UploadFile = File()

  # Annotated 방식
  name: Annotated[str, Form(min_length=1, max_length=50)] = "unknown"
  bio: Annotated[str | None, Form()] = None
  file: Annotated[UploadFile, File()]
```

가장 기본적인 방법은 **일반 필드는 `Form`, 파일은 `File` 로 하나씩 선언**하는 것이다.  

```py
from typing import Annotated

from fastapi import APIRouter, File, Form, UploadFile

router = APIRouter()

@router.post("/with-avatar-fields", status_code=201)
async def create_user_with_avatar_fields(
    name: Annotated[str, Form(min_length=1, max_length=50)],
    nickname: Annotated[str, Form(min_length=2, max_length=20)],
    file: Annotated[UploadFile, File()],
    bio: Annotated[str | None, Form()] = None,
    tags: Annotated[list[str] | None, Form()] = None,
):
    return {
        "name": name,
        "nickname": nickname,
        "bio": bio,
        "tags": tags or [],
        "filename": file.filename,
    }
```

위 예제는 파일을 저장하지 않고 전달받은 필드와 파일명만 반환한다.  
`Form` 에 선언한 길이 제약은 FastAPI 가 검증하며, 기본값이 없는 `name`, `nickname`, `file` 은 필수다.  

```sh
curl -X POST http://127.0.0.1:8000/with-avatar-fields \
  -F 'name=홍길동' \
  -F 'nickname=gildong' \
  -F 'bio=안녕하세요' \
  -F 'tags=python' \
  -F 'tags=fastapi' \
  -F 'file=@./avatar.png'
```

필드가 많아지면 함수 인자가 길어지므로, 아래처럼 폼 모델로 묶는 방식을 고려할 수 있다.  
FastAPI 는 **Pydantic 모델을 폼 전체에 바인딩**할 수 있다(0.113+).

```py
# app/domains/user/schema.py
import json
from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field, field_validator


class AddressRequest(BaseModel):
    city: str
    street: str


class UserForm(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: Annotated[str, Field(min_length=1, max_length=50)]
    nickname: Annotated[str, Field(min_length=2, max_length=20)]
    bio: str | None = None
    tags: list[str] = []
    address: AddressRequest

    @field_validator("address", mode="before")
    @classmethod
    def parse_address(cls, value):
        # 폼 필드는 전부 문자열로 도착한다. 중첩 객체만 JSON 으로 받아 푼다.
        return json.loads(value) if isinstance(value, str) else value
```

```py
# app/domains/user/router.py
from typing import Annotated

from fastapi import File, Form, UploadFile

from app.core.upload import save_upload
from app.domains.user.schema import UserForm

UserFormDep = Annotated[UserForm, Form()]


@router.post("/with-avatar", status_code=201)
async def create_user_with_avatar(
    request: UserFormDep,
    file: Annotated[UploadFile, File()],
) -> UserResponse:
    saved_path = await save_upload(file)
    ...
```

```sh
curl -X POST http://127.0.0.1:8000/with-avatar \
  -F 'name=홍길동' \
  -F 'nickname=gildong' \
  -F 'bio=안녕하세요' \
  -F 'tags=python' \
  -F 'tags=fastapi' \
  -F 'address={"city":"서울","street":"테헤란로 1"}' \
  -F 'file=@./avatar.png'
```

검증은 JSON 본문일 때와 완전히 같다.  

`Field` 제약, `field_validator`, `model_validator` 가 모두 실행되고, 실패하면 `RequestValidationError` 로 422 가 나가면서 필드 경로까지 그대로 잡힌다.
따로 파싱 의존성을 만들어 `ValidationError` 를 변환할 필요가 없다.  

**주의할 점**  

1. 폼 값은 전부 문자열이다  
   `int`, `bool`, `datetime` 은 Pydantic 이 변환해 주지만, **중첩 구조는 표현할 방법이 없다.**
    위의 `address` 처럼 중첩 객체·배열 필드만 JSON 문자열로 받아 `mode="before"` 에서 푼다.
    이 지점 때문에 모델 전체에 `strict=True` 를 걸면 안 된다.  
2. Pydantic 폼 모델의 파일 필드는 공식 지원하지 않으므로 별도 매개변수로 분리한다.  
3. 브라우저 폼은 값이 없어도 `bio=""` 를 보내는 경우가 많다.
   `str | None` 필드에 빈 문자열이 들어오는 게 싫으면 `mode="before"` 에서 `"" → None` 으로 정리한다.  

```py
# app/core/upload.py
import asyncio
import shutil
from pathlib import Path
from typing import BinaryIO
from uuid import uuid4

from fastapi import UploadFile


UPLOAD_ROOT = Path("/data/uploads")


def _copy_file(source: BinaryIO, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    with destination.open("wb") as output:
        shutil.copyfileobj(source, output)


async def save_upload(file: UploadFile) -> Path:
    # 원본 파일명은 경로로 쓰지 않고 확장자만 가져온다.
    extension = Path(file.filename or "").suffix.lower() or ".bin"
    destination = UPLOAD_ROOT / f"{uuid4().hex}{extension}"
    await asyncio.to_thread(_copy_file, file.file, destination)
    return destination
```

`UploadFile.file` 은 이미 메모리 또는 임시 디스크에 저장된 binary file-like 객체다.
동기 파일 복사는 이벤트 루프를 막지 않도록 `asyncio.to_thread()` 로 실행한다.

### 응답 모델 · response_model

```py
@router.post("", response_model=UserResponse, status_code=201)
async def create_user(...) -> UserResponse:
```

`response_model` 은 단순 문서화가 아니라 **출력 필터**로 동작한다.  
핸들러가 그보다 많은 필드를 가진 객체를 반환해도 `response_model` 에 선언된 필드만 직렬화된다.  
내부 필드나 패스워드 해시 등이 실수로 새어 나가는 것을 막는 마지막 방어선이다.  

`204 No Content` 는 본문이 없어야 하므로 반환 타입을 `None` 으로 둔다
(앞서 `ApiRouter` 가 `response_model` 을 떼어내 준다).  

### 미들웨어 · @app.middleware

`@app.middleware("http")` 는 모든 요청을 라우팅 전후로 감싼다.  
**엔드포인트와 무관하게 매 요청에 적용할 일**을 여기서 처리한다.  

`call_next(request)` **앞은 요청 전처리, 뒤는 응답 후처리**다.  

`request.state` 에 넣어 둔 값은 이후 **라우터와 예외 핸들러에서** `request.state.request_id` 로 꺼내 쓴다.  
다만 미들웨어에서 던진 예외는 `@app.exception_handler` 로 잡히지 않을 수 있으므로, 응답 형태를 통일하는 로직은 예외 핸들러에 두고 미들웨어는 얇게 유지한다.  

```py
import time
import logging

logger = logging.getLogger("access")

# 요청마다 request_id 추가
@app.middleware("http")
async def add_request_id(request: Request, call_next):
    request.state.request_id = uuid4().hex        # 요청 전처리: 상태에 값 저장
    response = await call_next(request)           # 실제 라우팅 실행
    response.headers["X-Request-ID"] = request.state.request_id  # 응답 후처리
    return response

# 요청마다 소요 시간을 재서 헤더로 내려 주고 한 줄 로그로 남긴다.  
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start = time.perf_counter()
    response = await call_next(request)
    elapsed_ms = (time.perf_counter() - start) * 1000

    response.headers["X-Process-Time"] = f"{elapsed_ms:.1f}ms"
    logger.info(
        "%s %s -> %s (%.1fms)",
        request.method, request.url.path, response.status_code, elapsed_ms,
    )
    return response
```

미들웨어는 **나중에 등록한 것이 바깥쪽**에 놓인다. 요청은 바깥쪽에서 안쪽으로 들어오고, 응답은 그 반대로 나간다.  

```text
FastAPI 애플리케이션
└── log_requests                    ← 나중에 등록: 바깥 범위
    └── add_request_id              ← 먼저 등록: 안쪽 범위
        └── 라우터 (GET / POST 핸들러)

요청: log_requests → add_request_id → 라우터
응답: 라우터 → add_request_id → log_requests
```

그래서 모든 응답을 감싸야 하는 CORS, 전체 처리 시간을 재는 로깅처럼 **바깥에서 동작해야 하는 미들웨어일수록 나중에 등록**한다.
순서가 애매하면 CORS 를 가장 마지막에 등록해 가장 바깥에 두는 편이 안전하다.  

> 라우터에서 예외가 났을 때 미들웨어가 보는 것(4xx 는 Response, 500 은 `call_next` 에서 raise)은 아래 **에러 처리** 섹션의 "핸들러는 어디서 실행되나" 에 정리했다.  

#### 클래스형 미들웨어

`@app.middleware("http")` 만으로도 같은 전후 처리를 구현할 수 있으므로 클래스형이 반드시 필요한 것은 아니다.
앱 안에서만 쓰는 짧은 로직은 함수형이 더 간단하다. 반면 다음처럼 **재사용과 설정이 필요한 미들웨어**는
클래스형으로 분리하는 편이 좋다.  

- 여러 `FastAPI` 인스턴스나 프로젝트에서 같은 미들웨어를 재사용할 때
- 헤더 이름, 제외 경로 같은 설정값을 생성자 인자로 받아야 할 때
- 미들웨어 로직을 별도 모듈로 분리하고 독립적으로 테스트할 때
- 다른 사람이 `app.add_middleware()` 로 가져다 쓸 수 있는 컴포넌트로 제공할 때

다만 미들웨어 인스턴스 하나가 여러 요청에 공유되므로, 요청마다 달라지는 값을 `self` 에 저장하면 안 된다.
그런 값은 지역 변수나 `request.state`, `contextvars` 에 보관한다.  

직접 클래스형 미들웨어를 만들 때는 간단한 HTTP 처리라면 Starlette의 `BaseHTTPMiddleware` 를 상속하고
`dispatch()` 를 구현한다. `dispatch()` 에서 `call_next(request)` 를 호출하는 지점을 기준으로 앞은 요청
전처리, 뒤는 응답 후처리가 된다.  

```py
import time

from fastapi import FastAPI, Request
from starlette.middleware.base import BaseHTTPMiddleware


class ProcessTimeMiddleware(BaseHTTPMiddleware):
    def __init__(self, app, header_name: str = "X-Process-Time"):
        super().__init__(app)
        self.header_name = header_name

    async def dispatch(self, request: Request, call_next):
        start = time.perf_counter()               # 요청 전처리
        response = await call_next(request)       # 다음 미들웨어 또는 라우터 실행
        elapsed_ms = (time.perf_counter() - start) * 1000
        response.headers[self.header_name] = f"{elapsed_ms:.1f}ms"  # 응답 후처리
        return response


app = FastAPI()
app.add_middleware(
    ProcessTimeMiddleware,
    header_name="X-Process-Time",                # __init__ 옵션으로 전달
)
```

`app` 은 Starlette가 자동으로 전달하고, `header_name` 같은 나머지 인자는 `add_middleware()` 에 지정한다.
이 방식은 직접 만든 미들웨어뿐 아니라 `CORSMiddleware`, `GZipMiddleware` 같은 미리 정의된 클래스에도
동일하게 적용된다.  

| 사전 정의 미들웨어 | 용도 |
| --- | --- |
| `CORSMiddleware` | 브라우저 교차 출처(cross-origin) 요청 허용 |
| `GZipMiddleware` | 응답 본문 압축 |
| `TrustedHostMiddleware` | `Host` 헤더 화이트리스트 |
| `@app.middleware("http")` | 직접 만드는 요청·응답 훅 |

```text
BaseHTTPMiddleware 상속
└── dispatch(request, call_next) 구현
    ├── call_next() 전  ── 요청 전처리
    ├── call_next()     ── 다음 계층 실행
    └── call_next() 후  ── 응답 후처리

등록
└── app.add_middleware(ProcessTimeMiddleware, ...)
```

`FastAPI()` 인스턴스는 사용자가 `add_middleware()` 를 호출하지 않아도 요청 처리에 필요한 클래스형 미들웨어를 내부 스택에 자동으로 구성한다.  
이들은 `app.user_middleware` 에 넣는 사용자 미들웨어와는 구분된다.  

| 기본 미들웨어 | 위치와 역할 |
| --- | --- |
| `ServerErrorMiddleware` | 전체 스택의 최외곽에서 처리되지 않은 예외를 받아 `500` 응답을 만들고 원본 예외를 다시 발생시킨다. `@exception_handler(Exception)` 또는 상태코드 `500` 핸들러도 여기서 호출한다. 직접 로그를 출력하는 것은 아니며, 다시 발생한 예외를 받은 Uvicorn 같은 ASGI 서버가 로그를 남긴다. |
| `ExceptionMiddleware` | 안쪽 라우터에서 바깥으로 전파되는 `HTTPException`, `RequestValidationError`, `ApiException` 등을 잡아 등록된 핸들러로 `Response`를 만든다. 이 시점부터 예외 전파는 멈추지만, 변환된 응답은 사용자 미들웨어와 `ServerErrorMiddleware`를 거쳐 바깥으로 나간다. |
| `AsyncExitStackMiddleware` | 라우터 바로 바깥에서 요청별 `AsyncExitStack`을 만들고, 파일 등 요청 처리 중 등록된 자원의 정리 작업이 요청 종료 시 실행되도록 관리한다. |

```text
FastAPI 애플리케이션
└── ServerErrorMiddleware                 ← 자동 구성 · 최외곽 500 처리
    └── 사용자 미들웨어                       ← @app.middleware · add_middleware
        └── ExceptionMiddleware           ← 자동 구성 · 구체 예외를 Response로 변환
            └── AsyncExitStackMiddleware  ← 자동 구성 · 요청별 자원 정리
                └── 라우터 (GET / POST 핸들러)

요청: 바깥 → 안쪽
응답: 안쪽 → 바깥
```

`HTTPException(status_code=500)` 을 직접 발생시키면 등록된 HTTP 예외이므로  `ExceptionMiddleware`가 처리한다.  
반면 `RuntimeError`처럼 처리되지 않은 일반 예외가 스택 밖으로 빠져나오면
`ServerErrorMiddleware`가 최종 `500` 응답을 만든다.  

FastAPI/Starlette에 미리 정의된 클래스형 미들웨어는 `app.add_middleware()` 로 등록하여 사용한다.  
직접 만든 클래스형 미들웨어도 같은 방식으로 등록할 수 있다.  

```py
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://example.com"],  # 허용 오리진 ("*" 는 인증정보와 함께 못 씀)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
app.add_middleware(GZipMiddleware, minimum_size=1000)  # 1KB 이상 응답만 압축
```

### 에러 처리 · @app.exception_handler

예외 클래스를 만드는 것만으로는 FastAPI 가 어떤 응답을 만들지 알 수 없다. 예외 타입과 응답 함수를
특정 `FastAPI` 인스턴스에 등록해야 한다. 가장 간단한 방법은 `@app.exception_handler()` 데코레이터다.  

FastAPI의 에러 처리는 별도의 독립된 실행 구조가 아니라, 애플리케이션에 자동 구성되는 `ExceptionMiddleware`와 `ServerErrorMiddleware`를 통해 동작한다. 
`@app.exception_handler()`는 예외를 직접 감싸거나 실행하는 데코레이터가 아니라, **어떤 예외를 어떤 함수로 처리할지 등록**한다.  

- `ExceptionMiddleware`는 라우터에서 바깥으로 전파되는 `HTTPException`, `RequestValidationError`,
  `ApiException` 같은 구체 예외를 잡고 등록된 핸들러를 호출해 `Response`로 변환한다.
- `ServerErrorMiddleware`는 그보다 바깥에서 끝까지 처리되지 않은 예외를 받아 최종 `500` 응답을 만든다.
  `Exception` 또는 상태코드 `500`에 등록한 핸들러도 이 미들웨어가 호출한다.

```text
ServerErrorMiddleware                  ← 처리되지 않은 예외 · 최종 500
└── 사용자 미들웨어
    └── ExceptionMiddleware            ← 등록된 구체 예외를 Response로 변환
        └── 라우터 (예외 발생 지점)
```

밑에서 설명할 각종 예외 핸들러들이 `ExceptionMiddleware` 의 내용을 채워 넣는다.  

#### 기본 예외핸들러

`FastAPI()` 인스턴스를 만들면 요청 처리에 필요한 기본 예외 핸들러가 이미 등록된다. HTTP API에서
주로 마주치는 것은 `RequestValidationError` 와 `StarletteHTTPException` 핸들러다.  

- `RequestValidationError`  
  Path 변수 타입 변환, Query·Header·Cookie 값, JSON Body·Form·File 또는 `Depends()`가 선언한 요청값의 검증이 실패했을 때 발생한다.  
  기본 핸들러는 `422`와 `{"detail": [...]}`를 반환한다.
- `StarletteHTTPException`  
  직접 발생시킨 FastAPI `HTTPException`(`400`·`401`·`403`·`404`·`409`·`429` 등), 존재하지 않는 경로 `404`, 허용하지 않은 HTTP 메서드 `405` 등을 처리한다.  
  기본 핸들러는 예외가 가진 상태코드와 `{"detail": ...}`를 반환한다.

두 핸들러는 상태코드가 아니라 **예외 타입을 기준으로 동작**한다. `RequestValidationError` 핸들러는 FastAPI가 요청값 검증 실패를 이 예외로 변환했을 때 호출된다. 
응답값이 선언한 모델과 맞지 않아 발생하는 `ResponseValidationError` 는 처리하지 않는다.  

예를 들어 `user_id: int` 에 문자열이 들어오면 `RequestValidationError` 가 발생하고 기본 핸들러가 필드 위치와 검증 실패 이유를 배열로 반환한다.  

```json
{
  "detail": [
    {
      "type": "int_parsing",
      "loc": ["path", "user_id"],
      "msg": "Input should be a valid integer",
      "input": "abc"
    }
  ]
}
```

FastAPI의 `HTTPException` 이 `StarletteHTTPException` 클래스를 상속하므로 상태코드별로 나뉘지 않고 모두 함께 처리한다. 
따라서 별도의 `400`·`404` 기본 핸들러가 있는 것이 아니라, 예외가 가진 `status_code` 를 그대로 사용해 응답한다. 
단, 처리되지 않은 일반 `Exception` 으로 발생한 `500`은 이 핸들러가 아니라 바깥의 `ServerErrorMiddleware` 가 처리한다.  

```json
{
  "detail": "Not Found"
}
```

#### 커스텀 예외핸들러

같은 예외 타입으로 커스텀 핸들러를 등록하면 해당 앱에서는 기본 핸들러 대신 새 핸들러가 사용된다.
이를 이용해 서로 다른 기본 응답을 프로젝트의 공통 에러 형식으로 통일할 수 있다.  

```py
app = FastAPI()


class ApiException(Exception):
    def __init__(self, error_code: ErrorCode, *, detail=None, **params):
        self.error_code = error_code
        self.detail = detail
        self.params = params          # 메시지 자리표시자에 채울 값
        super().__init__(error_code.code)


@app.exception_handler(ApiException)
async def api_exception_handler(request: Request, exc: ApiException):
    return JSONResponse(
        status_code=exc.error_code.status,
        content={"code": exc.error_code.code},
    )
```

데코레이터를 통해 **함수를 정의하는 시점에** 아래 등록 메서드를 호출하고 원래 함수를 그대로 반환한다.  

```py
app.add_exception_handler(ApiException, api_exception_handler)
```

등록이 끝나면 요청 처리 중 `ApiException` 이 밖으로 전파될 때 FastAPI 가 등록 테이블에서 핸들러를 찾아 호출한다. 등록하지 않으면 커스텀 예외는 처리되지 않아 500 응답이 된다.  

프로젝트가 커지면 일관된 에러 응답을 구성하고 `register_exception_handlers()` 같은 커스텀 에러 조립 함수를 작성하고 파일로 구분한다.  

```py
# app/core/errors.py
def _respond(request: Request, error_code: ErrorCode, detail=None, **params) -> JSONResponse:
    # 문장은 여기서 딱 한 번 만들어진다. 요청자의 언어를 아는 곳이 여기뿐이다.
    locale = negotiate_locale(request.headers.get("accept-language"))
    return JSONResponse(
        status_code=error_code.status,
        content={
            "code": error_code.code,
            "message": resolve_message(error_code.code, locale, **params),
            "detail": detail,
            "request_id": getattr(request.state, "request_id", None),
        },
        headers={"Content-Language": locale},
    )


def register_exception_handlers(app: FastAPI) -> None:

    @app.exception_handler(ApiException)
    async def _api_exception(request: Request, exc: ApiException):
        # 4xx 는 "정상적인 실패"다. 스택트레이스까지 남기면 로그가 쓸모없어진다.
        logger.info("%s %s -> %s", request.method, request.url.path, exc.error_code.code)
        return _respond(request, exc.error_code, exc.detail, **exc.params)

    @app.exception_handler(RequestValidationError)
    async def _validation(request: Request, exc: RequestValidationError):
        # 필드 단위 정보를 그대로 내려 프론트가 폼 에러를 표시할 수 있게 한다.
        detail = [
            {"field": field_path(error["loc"]), "message": error["msg"], "type": error["type"]}
            for error in exc.errors()
        ]
        return _respond(request, ErrorCode.VALIDATION_ERROR, detail)

    @app.exception_handler(StarletteHTTPException)
    async def _http_exception(request: Request, exc: StarletteHTTPException):
        # 라우트 미존재(404)·메서드 불일치(405) 등 프레임워크가 던지는 예외도 같은 형태로.
        mapping = {
            HTTPStatus.NOT_FOUND: ErrorCode.NOT_FOUND,
            HTTPStatus.METHOD_NOT_ALLOWED: ErrorCode.METHOD_NOT_ALLOWED,
            HTTPStatus.UNAUTHORIZED: ErrorCode.UNAUTHORIZED,
            HTTPStatus.FORBIDDEN: ErrorCode.FORBIDDEN,
        }
        # exc.detail 은 Starlette 이 만든 영어 문장이라 쓰지 않는다. 코드로만 변환한다.
        return _respond(request, mapping.get(exc.status_code, ErrorCode.BAD_REQUEST))

    @app.exception_handler(Exception)
    async def _unhandled(request: Request, exc: Exception):
        # 여기까지 왔다면 예상하지 못한 예외다. 내부 메시지를 밖으로 흘리지 않는다.
        logger.exception("Unhandled exception on %s %s", request.method, request.url.path)
        return _respond(request, ErrorCode.INTERNAL_SERVER_ERROR)
```

함수 안에 선언한 데코레이터도 이 함수를 호출해야 실행된다.  
애플리케이션을 조립할 때 생성한 인스턴스를 넘겨 **한 번 호출해야 한다.**  

```py
# app/main.py
from fastapi import FastAPI

from app.core.errors import register_exception_handlers


def create_app() -> FastAPI:
    app = FastAPI()

    register_exception_handlers(app)
    app.include_router(user_router)

    return app


app = create_app()
```

```text
애플리케이션 시작
├── create_app()
│   ├── FastAPI 인스턴스 생성
│   ├── register_exception_handlers(app)
│   │   ├── ApiException 핸들러 등록
│   │   ├── RequestValidationError 핸들러 교체
│   │   ├── StarletteHTTPException 핸들러 교체
│   │   └── 그 밖의 Exception 핸들러 등록
│   ├── 라우터 등록
│   └── app 반환
└── ASGI 서버가 app으로 요청 처리 시작
```

**주의사항**

1. `StarletteHTTPException` 핸들러를 빼먹지 말 것  
   정의하지 않으면 404, 405 등의 에러는 우리 코드를 타지 않고 프레임워크가 바로 응답한다. 

2. 매칭은 등록 순서가 아니라 예외 클래스의 상속 관계다
   `Exception` 핸들러를 등록해도 `ApiException` 은 자기 핸들러로 간다.  
   FastAPI 가 예외 타입의 MRO 를 따라 가장 가까운 핸들러를 고른다.  

3. 500 핸들러는 응답을 만든 뒤 예외를 다시 던진다  
   Starlette 의 `ServerErrorMiddleware` 동작이다(ASGI 서버가 로그를 남기게 하려고).  

이 배치 때문에 라우터가 던진 예외의 처리 경로가 두 갈래로 갈리고, 그보다 바깥의 사용자 미들웨어가 보는 것도 달라진다.  

```text
라우터에서 예외 발생
└── ExceptionMiddleware에서 구체 핸들러 탐색
    ├── 있음 (예상된 4xx)
    │   └── Response로 변환
    │       └── 사용자 미들웨어가 정상 Response 수신 ✅
    └── 없음 (처리하지 못한 500)
        └── 예외가 바깥쪽으로 전파
            ├── 사용자 미들웨어의 일반 후처리 스킵 가능 ❌
            └── ServerErrorMiddleware가 500 생성 후 예외 재전파 (로깅)
```

- **4xx** 는 안쪽 `ExceptionMiddleware` 에서 이미 Response 로 바뀌므로, 바깥 사용자 미들웨어는 예외가 아니라 **상태코드만 다른 정상 Response** 를 받는다(헤더·로그 후처리 정상 실행).  
- **500** 은 `await call_next()` 에서 그대로 raise 되어 **미들웨어의 후처리(로깅·타이밍·`request_id` 헤더)가 누락**될 수 있다. 항상 실행돼야 한다면 `try/except … finally` 로 감싼다.  

```py
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start = time.perf_counter()
    try:
        response = await call_next(request)
    except Exception:
        logger.exception("unhandled on %s %s", request.method, request.url.path)
        raise                      # 500 응답·서버 로깅은 ServerErrorMiddleware 에 맡긴다
    finally:
        elapsed_ms = (time.perf_counter() - start) * 1000  # 성공·실패 모두 기록
    return response
```
