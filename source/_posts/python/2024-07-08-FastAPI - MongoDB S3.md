---
title:  "FastAPI - MongoDB & S3"
date: 2024-07-08

categories:
  - python

---


## MongoDB

```
motor          — 오랫동안 표준이었으나 2026-05 EOL
pymongo 4.13+  — AsyncMongoClient 가 드라이버에 내장됨 (현재 권장)
```

신규 프로젝트라면 `pymongo` 의 `AsyncMongoClient` 를 쓴다.  

```py
# motor
from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase
client = AsyncIOMotorClient(url, tz_aware=True)

# pymongo (권장)
from pymongo import AsyncMongoClient
from pymongo.asynchronous.database import AsyncDatabase
client = AsyncMongoClient(url, tz_aware=True)
```

> `tz_aware=True` 는 반드시 켠다. 끄면 Mongo 에서 읽은 datetime 이 naive 로 돌아와
> UTC 로 저장해 두고 로컬 시간처럼 다루는 버그가 생긴다.  

`AsyncMongoClient` 내부적으로 커넥션풀을 관리한다.  

### 커넥션 풀 lifespan

`AsyncMongoClient` 의 생성과 정리는 FastAPI 의 수명주기 기능인 `lifespan` 을 사용한다.  
`lifespan` 에서 커넥션 풀을 생성하면 사용할 이벤트 루프가 준비된 뒤 연결되고, 프로세스가 종료될 때도 클라이언트를 빠뜨리지 않고 정리할 수 있다.  

```py
# app/main.py
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.core.config import get_settings
from app.db.mongo_config import mongo
from app.db.mongo_index import ensure_indexes


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    await mongo.connect(settings)          # 서버가 요청을 받기 전에 연결
    await ensure_indexes(mongo.database)   # 인덱스 생성 코드 실행
    yield
    await mongo.disconnect()               # 서버 종료 시 연결 정리


app = FastAPI(lifespan=lifespan)
```

```py
# app/db/mongo_config.py
from typing import Annotated

from fastapi import Depends
from pymongo import AsyncMongoClient
from pymongo.asynchronous.database import AsyncDatabase

from app.core.config import Settings


class MongoConnection:
    def __init__(self) -> None:
        self._client: AsyncMongoClient | None = None
        self._database: AsyncDatabase | None = None

    async def connect(self, settings: Settings) -> None:
        if self._client is not None:
            return
        self._client = AsyncMongoClient(
            settings.MONGO_URL, tz_aware=True,
            serverSelectionTimeoutMS=settings.MONGO_TIMEOUT_MS,
        )
        self._database = self._client[settings.mongo_database]
        # 연결은 lazy 라서 여기서 한 번 두드려 봐야 설정 오류를 부팅 시점에 잡는다.
        await self._client.admin.command("ping")

    async def disconnect(self) -> None:
        if self._client is None:
            return
        await self._client.close()
        self._client = None
        self._database = None

    @property
    def database(self) -> AsyncDatabase:
        if self._database is None:
            raise RuntimeError("MongoDB 가 연결되지 않았습니다.")
        return self._database


mongo = MongoConnection()


def get_database() -> AsyncDatabase:
    return mongo.database


 = Annotated[AsyncDatabase, Depends(get_database)]
```

`lifespan` 을 통해 `AsyncMongoClient` 생성해야 **사용할 이벤트 루프가 준비된 상태에 커넥션 풀이 연결되어 사용할 수 있다.**  

```
Worker Process
│
└── Event Loop
     │
     │ ① 서버 시작
     ▼
   lifespan()
     │
     ├── AsyncMongoClient 생성
     │
     ├── Connection Pool 준비
     │
     ▼
   yield
     │
     │ ② 서버가 요청 받기 시작
     ▼
   ┌───────────────────────────┐
   │ Request A                 │
   │ Request B                 │
   │ Request C                 │
   │ Request D                 │
   └───────────────────────────┘
     │
     │ ③ 서버 종료
     ▼
   lifespan() 종료
     │
     └── MongoClient close()
```

모듈 최상단에서 클라이언트 생성시 테스트나 멀티 워커처럼 이벤트 루프가 바뀌는 상황에서 `AsyncMongoClient` 접근시 `Future attached to a different loop` 류의 에러가 난다.  

### 의존성 주입

앞의 `Annotated[타입, Depends(팩터리)]` 형태가 FastAPI 의 의존성 주입이다.  
FastAPI는 함수 중심 DI를 택했기 때문에 기본 싱글톤 주입을 거의 쓰지 않는다. 실행하는 의존성 팩터리나 라우터 함수에서 해당 별칭을 인자로 선언하여 객체를 받는다.  

아래 서비스 팩터리는 `AreaRepositoryDep`으로 주입받은 리포지토리를 `AreaService` 생성자에 전달한다.  
일반 서비스 메서드를 직접 호출할 때는 FastAPI가 자동으로 의존성을 주입하지 않는다.  

```py
# service 객체를 만드는 의존성 팩터리
# AreaService의 생성자와 전체 구현은 아래 CRUD에서 설명한다.
def get_area_service(
    repository: AreaRepositoryDep,
    storage: StorageDep,
    settings: SettingsDep,
) -> AreaService:
    return AreaService(repository, storage, settings)

AreaServiceDep = Annotated[AreaService, Depends(get_area_service)]
```

**팩터리가 다른 의존성을 인자로 받으면 FastAPI 가 순서대로 해결한다.**  
실제 저장소 코드가 이 체인 그대로다.  

```
Settings (lru_cache, 프로세스당 1개)
  └─ MongoConnection (lifespan, 프로세스당 1개)
       └─ get_database() → DatabaseDep
            └─ get_area_repository(database: DatabaseDep) → AreaRepositoryDep
                 └─ get_area_service(repository, storage, settings) → AreaServiceDep
                      └─ router(service: AreaServiceDep)
```

리포지토리를 구성하는 `get_area_repository()`는 [MongoRepository](#MongoRepository) 에서 만든다.  
`DatabaseDep`을 인자로 받기만 하면 되고, 커넥션을 어떻게 얻는지는 알 필요가 없다.  

한 요청 안에서 같은 의존성을 여러 번 요구해도 **기본적으로 한 번만 평가되고 결과가 공유된다.**  
라우터가 `AreaRepositoryDep`와 `DatabaseDep`을 같이 받아도 `get_database()` 는 한 번만 호출된다.  
매번 새로 만들어야 한다면 `Depends(factory, use_cache=False)` 로 끈다.  


#### CRUD

앞의 의존성 주입이 실제 API 요청에서 어떻게 사용되는지 살펴본다.  
문서 모델·리포지토리·스토리지의 구현은 뒤의 각 목차에서 설명하며, 여기서는 계층을 연결하는 호출 흐름에 집중한다.  
이 계층들을 API로 조립하면 **router → service → repository → MongoDB** 순으로 호출된다.  

| 계층 | 책임 | 파일 |
| --- | --- | --- |
| router | HTTP 경로·메서드 등록, 요청 검증, 서비스 호출, 응답 모델·상태 코드 지정 | `app/domains/area/router.py` |
| service | 비즈니스 규칙, DTO ↔ 문서 변환, DB와 스토리지 작업 순서 조율 | `app/domains/area/service.py` |
| repository | 문서 변환과 PyMongo CRUD 실행 | `app/domains/area/repository.py`, `app/db/repository.py` |

**① service — 서비스 생성과 AreaServiceDep 정의**  

`AreaServiceDep`이 어디서 정의되는지 먼저 살펴본다.  
서비스 클래스와 이를 생성하는 팩터리를 정의한 뒤, `Annotated`와 `Depends`로 라우터에서 사용할 의존성 별칭을 만든다.  

```py
# app/domains/area/service.py — 조회에 필요한 부분 발췌
from typing import Annotated

from fastapi import Depends

from app.core.config import Settings, SettingsDep
from app.core.errors import ApiException, ErrorCode
from app.db.repository import MongoRepository
from app.domains.area.model import AreaDoc
from app.domains.area.repository import AreaRepositoryDep
from app.domains.area.schema import AreaResponse
from app.storage.base import ObjectStorage
from app.storage.provider import StorageDep


class AreaService:
    def __init__(
        self,
        repository: MongoRepository[AreaDoc],
        storage: ObjectStorage,
        settings: Settings,
    ) -> None:
        self._repository = repository
        self._storage = storage
        self._settings = settings

    async def get(self, area_id: str) -> AreaResponse:
        document = await self._get_or_raise(area_id)
        return self._to_response(document)

    async def _get_or_raise(self, area_id: str) -> AreaDoc:
        document = await self._repository.find_by_id(area_id)
        if document is None:
            raise ApiException(ErrorCode.AREA_NOT_FOUND)
        return document

    # _to_response()와 생성·수정·삭제 메서드는 생략


def get_area_service(
    repository: AreaRepositoryDep,
    storage: StorageDep,
    settings: SettingsDep,
) -> AreaService:
    return AreaService(repository, storage, settings)


AreaServiceDep = Annotated[AreaService, Depends(get_area_service)]
```

`AreaService`는 생성자에서 repository·storage·settings를 받아 인스턴스에 보관한다.  
FastAPI가 생성자 타입만 보고 자동으로 객체를 만드는 것은 아니다.  
`get_area_service()`의 의존성을 해결하고 이 팩터리가 생성자를 호출하며, 반환한 서비스를 router의 `AreaServiceDep` 인자에 주입한다.  

서비스는 repository에서 `AreaDoc`을 받고, 문서가 없으면 프로젝트의 예외 처리기를 통해 오류 응답으로 처리한다.  
`_to_response()`는 `thumbnail_key`로 다운로드 URL을 만들고 저장된 GeoJSON을 응답 형태로 바꿔 `AreaResponse`를 반환한다.  
즉 DB에 저장하는 모델과 클라이언트에 반환하는 모델은 서로 다른 역할이다.  

**② repository — 실제 MongoDB 조회**  

서비스 팩터리가 받는 `AreaRepositoryDep`은 아래처럼 정의한다.  

```py
# app/domains/area/repository.py
from typing import Annotated

from fastapi import Depends

from app.db.mongo import DatabaseDep
from app.db.repository import MongoRepository
from app.domains.area.model import COLLECTION, AreaDoc

type AreaRepository = MongoRepository[AreaDoc]


def get_area_repository(database: DatabaseDep) -> AreaRepository:
    return MongoRepository(database, COLLECTION, AreaDoc)


type AreaRepositoryDep = Annotated[AreaRepository, Depends(get_area_repository)]
```

`AreaRepositoryDep`을 요구하는 서비스 팩터리에는 이 함수가 반환한 repository가 전달된다.  

`get_area_repository()` 팩터리가 생성하는 `MongoRepository(database, COLLECTION, AreaDoc)`는 아래 [MongoRepository](#MongoRepository)에서 설명한다.

**③ router 등록 — Python 함수를 HTTP API로 연결**  

앞에서 정의한 `AreaServiceDep`을 router 주입에 사용한다.  

```py
# app/domains/area/router.py
from app.core.routing import ApiRouter
from app.domains.area.schema import AreaResponse
from app.domains.area.service import AreaServiceDep

router = ApiRouter(prefix="/areas", tags=["area"])


@router.get("/{area_id}", response_model=AreaResponse, summary="구역 단건 조회")
async def get_area(service: AreaServiceDep, area_id: str) -> AreaResponse:
    return await service.get(area_id)
```

### MongoModel - 공통 문서 모델

연결과 의존성 주입을 준비했으니, 이제 MongoDB에 저장할 문서의 표현을 정의한다.  
`MongoModel`은 이 프로젝트에서 직접 만든 **Pydantic 기반 공통 문서 모델**이다.  
FastAPI나 PyMongo가 제공하는 클래스가 아니며, DB 스키마나 인덱스를 자동 배포하는 기능도 없다.  

도메인 모델이 상속해서 사용할 공통 필드와 MongoDB 문서 ↔ Python 모델 변환을 담당한다.  

| 구성 | 역할 |
| --- | --- |
| `id` | 애플리케이션에서 사용하는 문자열 식별자. 저장할 때 MongoDB의 `_id`로 변환 |
| `created_at`, `updated_at` | 공통 생성·수정 시각. 실제 값의 설정·갱신은 뒤에서 구현할 리포지토리가 담당 |
| `from_mongo()` | 조회한 dict의 `_id`를 문자열 `id`로 바꾸고 Pydantic 검증을 거쳐 모델 생성 |
| `to_mongo()` | 모델을 저장용 dict로 바꾸고 `id`를 `_id`로 변환 |
| `extra="ignore"` | 모델에 선언하지 않은 필드를 읽을 때 무시. 원본의 미지 필드를 보존하지 않음 |

`from_mongo()`는 입력 dict를 복사하므로 원본을 변경하지 않고, 조회 결과가 `None`이면 `None`을 반환한다.  
`cls.model_validate()`를 사용하므로 `AreaDoc.from_mongo()`를 호출하면 공통 모델이 아니라 `AreaDoc`의 도메인 필드까지 검증한 인스턴스를 얻는다.  

`to_mongo()`의 `mode="python"`은 `datetime` 등을 Python 객체로 유지해 PyMongo가 BSON으로 직렬화할 수 있게 한다.  
기본 `exclude_none=True`는 `None` 필드를 저장 대상에서 제외한다.  
`id`가 없으면 `_id`도 넣지 않으므로 insert 시 드라이버가 `ObjectId`를 생성한다.  

```py
# app/db/mongo_model.py
from datetime import datetime
from typing import Any, TypeVar

from bson import ObjectId
from pydantic import BaseModel, ConfigDict

T = TypeVar("T", bound="MongoModel")


class MongoModel(BaseModel):
    model_config = ConfigDict(extra="ignore")

    id: str | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None

    @classmethod
    def from_mongo(cls: type[T], document: dict[str, Any] | None) -> T | None:
        if document is None:
            return None
        data = dict(document)
        raw_id = data.pop("_id", None)
        if raw_id is not None:
            data["id"] = str(raw_id)
        return cls.model_validate(data)

    def to_mongo(self, *, exclude_none: bool = True) -> dict[str, Any]:
        data = self.model_dump(mode="python", exclude_none=exclude_none, exclude={"id"})
        if self.id is not None:
            data["_id"] = to_object_id(self.id)
        return data


def to_object_id(value: str) -> Any:
    """hex 문자열이면 ObjectId 로, 아니면 문자열 `_id` 로 취급한다."""
    return ObjectId(value) if ObjectId.is_valid(value) else value
```

`to_object_id()`는 유효한 24자리 hex 문자열이면 `ObjectId`로, 그 외에는 문자열 `_id`로 취급하는 이 프로젝트의 규칙이다.  
문자열 `_id` 자체가 24자리 hex일 수 있는 서비스라면 타입을 구분하는 별도 규칙이 필요하다.  

#### MongoRepository

`app/db/repository.py`의 `MongoRepository`는 이 프로젝트에서 직접 작성한 공통 CRUD 클래스다. PyMongo 호출과 문서 변환을 명시적으로 구현한다.  
앞의 `MongoModel`이 문서 표현과 변환을 담당한다면, `MongoRepository`는 해당 모델의 조회·저장을 담당한다.  

**제네릭 타입과 생성자**  

`M = TypeVar("M", bound=MongoModel)`은 `MongoModel` 또는 그 하위 모델을 타입 인자로 받도록 선언한다.  
`MongoRepository[AreaDoc]`은 메서드의 입력·반환 모델이 `AreaDoc`임을 타입 검사기에 알려주는 표현이다.  
`[AreaDoc]`만으로 런타임 모델을 선택하는 것은 아니므로 생성자에도 모델 클래스인 `AreaDoc`을 전달한다.  

```py
repository = MongoRepository(database, COLLECTION, AreaDoc)
```

| 생성자 인자 | 보관 위치 | 역할 |
| --- | --- | --- |
| `database` | 컬렉션 선택에 사용 | lifespan에서 연결한 기존 `AsyncDatabase` |
| `collection` | `self._collection = database[collection]` | 접근할 컬렉션 이름 |
| `model` | `self._model = model` | 조회한 dict를 모델로 변환할 실제 클래스 |

이 객체를 생성해도 MongoDB client나 커넥션 풀을 새로 만들지는 않는다.  

**실제 구현**  

```py
# app/db/repository.py
from __future__ import annotations

from typing import Any, Generic, TypeVar

from pymongo.asynchronous.database import AsyncDatabase

from app.core.time import utc_now
from app.db.model import MongoModel, to_object_id

M = TypeVar("M", bound=MongoModel)


class MongoRepository(Generic[M]):
    def __init__(self, database: AsyncDatabase, collection: str, model: type[M]) -> None:
        self._collection = database[collection]
        self._model = model

    def _id_filter(self, id: str) -> dict[str, Any]:
        return {"_id": to_object_id(id)}

    async def save(self, document: M) -> M:
        """신규면 insert, 기존이면 replace. 감사 필드를 여기서 갱신한다."""
        now = utc_now()
        if document.id is None:
            document.created_at = document.created_at or now
            document.updated_at = now
            result = await self._collection.insert_one(document.to_mongo())
            document.id = str(result.inserted_id)
            return document

        document.updated_at = now
        data = document.to_mongo()
        raw_id = data.pop("_id")
        await self._collection.replace_one({"_id": raw_id}, data)
        return document

    async def find_by_id(self, id: str) -> M | None:
        return self._model.from_mongo(await self._collection.find_one(self._id_filter(id)))

    async def find_all(self, *, sort: list[tuple[str, int]] | None = None) -> list[M]:
        return await self.find({}, sort=sort)

    async def find(
        self,
        query: dict[str, Any],
        *,
        sort: list[tuple[str, int]] | None = None,
        skip: int = 0,
        limit: int = 0,
    ) -> list[M]:
        cursor = self._collection.find(query)
        if sort:
            cursor = cursor.sort(sort)
        if skip:
            cursor = cursor.skip(skip)
        if limit:
            cursor = cursor.limit(limit)
        return [self._model.from_mongo(document) async for document in cursor]  # type: ignore[misc]

    async def count(self, query: dict[str, Any] | None = None) -> int:
        return await self._collection.count_documents(query or {})

    async def exists_by_id(self, id: str) -> bool:
        return await self._collection.count_documents(self._id_filter(id), limit=1) > 0

    async def delete_by_id(self, id: str) -> bool:
        result = await self._collection.delete_one(self._id_filter(id))
        return result.deleted_count > 0
```

**메서드별 동작**  

| 메서드 | 동작 |
| --- | --- |
| `_id_filter(id)` | 문자열 식별자를 `to_object_id()`로 변환해 `_id` 조회 조건 구성 |
| `save(document)` | `id`가 없으면 insert, 있으면 replace. 생성·수정 시각 설정 |
| `find_by_id(id)` | `find_one()` 결과를 `self._model.from_mongo()`로 변환. 없으면 `None` |
| `find_all(sort=...)` | 빈 조건으로 `find()`를 호출하여 전체 조회 |
| `find(query, sort, skip, limit)` | 조건·정렬·페이지 범위를 적용하고 비동기 커서를 모델 리스트로 변환 |
| `count(query)` | 조건에 맞는 문서 수 반환 |
| `exists_by_id(id)` | 최대 한 문서를 세어 존재 여부 반환 |
| `delete_by_id(id)` | 삭제된 문서 수를 확인하여 성공 여부를 bool로 반환 |

`save()`는 신규 저장 후 `inserted_id`를 문자열 `id`로 넣어 같은 모델 객체를 반환한다.  
기존 문서 저장은 `$set` 부분 수정이 아니라 `replace_one()` 전체 교체다.  
따라서 모델에서 누락된 필드와 기본 `exclude_none=True`로 제외한 필드는 저장 결과에서 사라질 수 있다.  
또한 `upsert=True`를 사용하지 않고 `matched_count`도 검사하지 않으므로, `id`가 있어도 DB에 대상 문서가 없으면 자동 생성하거나 실패를 알리지 않는다.  
동시 수정 충돌을 막는 버전 검사나 여러 작업을 묶는 트랜잭션도 이 클래스에 포함되어 있지 않다.  

`find()`의 기본 `limit=0`은 제한 없는 조회이므로 데이터가 많으면 페이지 크기를 지정해야 한다.  
`find_by_id()`는 문서가 없으면 `None`을 반환하고, 이를 API 오류로 바꿀지는 서비스의 `_get_or_raise()`가 결정한다.  
조회할 때 사용하는 `from_mongo()`가 Pydantic 검증을 수행하므로, 저장 데이터가 모델과 맞지 않으면 검증 오류가 발생할 수 있다.  

**도메인 리포지토리와 연결**  

```py
# app/domains/area/repository.py
from typing import Annotated

from fastapi import Depends

from app.db.mongo import DatabaseDep
from app.db.repository import MongoRepository
from app.domains.area.model import COLLECTION, AreaDoc


type AreaRepository = MongoRepository[AreaDoc]


def get_area_repository(database: DatabaseDep) -> AreaRepository:
    return MongoRepository(database, COLLECTION, AreaDoc)


type AreaRepositoryDep = Annotated[AreaRepository, Depends(get_area_repository)]
```

`AreaRepository`는 새 클래스가 아니라 영역 모델을 지정한 타입 별칭이다.  
`get_area_repository()`가 실제 객체를 생성하고, `AreaRepositoryDep`이 서비스 팩터리에 전달할 주입 방법을 선언한다.  
`type`을 사용하는 명시적 타입 별칭 문법은 Python 3.12 이상에서 지원한다.  
영역 전용 조회 메서드가 필요하면 타입 별칭 대신 `MongoRepository[AreaDoc]`을 상속한 클래스로 확장할 수 있다.  

#### AreaDoc - 도메인 문서 모델

`AreaDoc`은 앞에서 만든 `MongoModel`을 상속하여 공통 식별자·시각 필드와 변환 메서드를 재사용한다.  
`area` 컬렉션에 저장할 도메인 필드만 추가로 선언한다.  

```py
# app/domains/area/model.py
from app.db.mongo_model import MongoModel

COLLECTION = "area"


class AreaDoc(MongoModel):
    name: str
    address: str
    description: str | None = None
    thumbnail_key: str    # ⚠ URL 이 아니라 오브젝트 key 를 저장한다
    geometry: dict        # GeoJSON Polygon
    tags: list[str] = []
```

**저장 규칙 두 가지만 기억하면 된다.**  

1. **`thumbnail_key` 에는 key 만 저장한다.**  
   presigned URL 을 DB 에 저장하면 만료된 URL 이 그대로 남고, 엔드포인트나 버킷이
   바뀌는 순간 전부 죽는다. URL 은 **조회할 때 만들어서 내려주는 파생 값**이다.  
2. **`geometry` 는 GeoJSON(`{type, coordinates}`) 으로 저장한다.**  
   `coordinates` 만 저장하면 `2dsphere` 인덱스를 태울 수 없다.  
   요청·응답 DTO 는 `coordinates` 배열만 주고받으므로 저장 형태와 달라지고, 변환 함수를 둔다.  

```py
# app/domains/area/model.py — 같은 파일
Coordinates = list[list[list[float]]]


def to_geojson(coordinates: Coordinates) -> dict:
    return {"type": "Polygon", "coordinates": coordinates}


def from_geojson(geometry: dict | None) -> Coordinates:
    return (geometry or {}).get("coordinates", [])
```

### 인덱스, 제약조건, TTL

`FastAPI + PyMongo` 는 모델의 필드 선언을 보고 `MongoDB validator` 나 인덱스를 자동 생성·변경하지 않는다.  

```py
class AreaDoc(MongoModel):
    name: str
    address: str
    description: str | None = None
    thumbnail_key: str
    geometry: dict
    tags: list[str] = []
    category: str | None = None    # 코드 배포만으로 새 문서부터 저장 가능
```

서버 실행과 동시에 인덱스, TTL 등을 생성하고 싶다면 `lifespan` 에서 생성된 `AsyncDatabase` 의 함수를 통해 생성 가능하다.  

```py
from app.domains.area.model import COLLECTION as AREA_COLLECTION

async def ensure_indexes(database: AsyncDatabase) -> None:
    await database[AREA_COLLECTION].create_index("name", name="name_asc") # 조회용 인덱스
    await database[AREA_COLLECTION].create_index(
        "thumbnail_key",
        unique=True,
        name="thumbnail_key_unique",
    ) # 중복방지 인덱스, 중복발생시 DuplicateKeyError 에러 발생

    await database[AREA_COLLECTION].create_index(
        "created_at",
        expireAfterSeconds=3600,
        name="ttl_area_created_at",
    ) # 생성기준 TTL
    await database[AREA_COLLECTION].insert_one({
        "name": "임시 영역",
        "created_at": datetime.now(timezone.utc),
    })
    await database[AREA_COLLECTION].create_index(
        "expires_at",
        expireAfterSeconds=0,
        name="ttl_area_expires_at",
    ) # 만료기준 TTL
```

처음 `lifespan` 에서 필요한 초기 설정을 모아 실행하는 함수 `ensure_indexes()` 를 실행시킨다.  
아래와 같이 배열로 모아 한번에 실행 가능하다.  

```py
# app/db/mongo_index.py
from pymongo import ASCENDING, DESCENDING, GEOSPHERE, IndexModel
from pymongo.asynchronous.database import AsyncDatabase

from app.domains.area.model import COLLECTION as AREA_COLLECTION

async def ensure_indexes(database: AsyncDatabase) -> None:
    await database[AREA_COLLECTION].create_indexes([
        IndexModel([("geometry", GEOSPHERE)], name="geometry_2dsphere"),
        IndexModel([("name", ASCENDING)], name="name_asc"),
        IndexModel([("created_at", DESCENDING)], name="created_at_desc"),
        IndexModel(
            [("thumbnail_key", ASCENDING)],
            unique=True,
            name="thumbnail_key_unique",
        ),
    ])
```

같은 이름·키·옵션으로 반복 실행해도 기존 인덱스를 유지하며 오류는 발생하지 않는다.  

## S3 - boto3

`boto3`는 AWS의 Python SDK다.  
이 글에서는 `boto3.client("s3")`로 파일 업로드·조회·삭제와 presigned URL 생성을 처리한다.  
S3는 bucket 안에 object를 저장하고, key로 각 object를 식별한다.  
MongoDB에는 파일 본문이나 만료되는 URL 대신 key를 저장한다.  

### 클라이언트 설정

아래는 이 프로젝트처럼 MinIO를 사용하는 경우의 설정 예제다.  
AWS S3에서는 일반적으로 `endpoint_url`을 생략하고, 실제 bucket 리전과 IAM Role 등의 자격 증명을 사용한다.  

```py
import uuid
from typing import BinaryIO, Protocol

import boto3
from botocore.config import Config


# 서비스가 사용하는 인터페이스. S3Storage나 LocalStorage로 교체할 수 있다.
class ObjectStorage(Protocol):
    def put(self, key: str, data: BinaryIO | bytes, content_type: str) -> str: ...
    def delete(self, key: str) -> None: ...
    def public_url(self, key: str, minutes: int) -> str: ...


class S3Storage:
    def __init__(self, settings):
        self._bucket = settings.S3_BUCKET
        common = {
            # 비밀 값은 설정에서 가져오고 코드에 직접 작성하지 않는다.
            "aws_access_key_id": settings.S3_ACCESS_KEY,
            "aws_secret_access_key": settings.S3_SECRET_KEY,
            "region_name": settings.S3_REGION,
            "config": Config(
                signature_version="s3v4",
                s3={"addressing_style": "path"},
                # HTTP 연결 풀에서 유지할 최대 연결 수. 동시 S3 요청량에 맞춰 설정한다.
                max_pool_connections=settings.S3_MAX_POOL_CONNECTIONS,
            ),
        }
        # 서버의 업로드·삭제용 주소
        self._client = boto3.client("s3", endpoint_url=settings.S3_ENDPOINT, **common)
        # 브라우저가 접근할 주소로 URL을 서명한다.
        self._presigner = boto3.client(
            "s3", endpoint_url=settings.S3_PUBLIC_ENDPOINT, **common
        )

    def put(self, key: str, data: BinaryIO | bytes, content_type: str) -> str:
        self._client.put_object(
            Bucket=self._bucket, Key=key, Body=data, ContentType=content_type
        )
        return key

    def delete(self, key: str) -> None:
        self._client.delete_object(Bucket=self._bucket, Key=key)

    def public_url(self, key: str, minutes: int) -> str:
        return self._presigner.generate_presigned_url(
            "get_object",
            Params={"Bucket": self._bucket, "Key": key},
            ExpiresIn=minutes * 60,
        )


# 원본 파일명 대신 서버에서 key를 생성한다. S3의 '/'는 폴더가 아니라 key의 일부다.
key = f"areas/{uuid.uuid4().hex}.png"
```

예제는 핵심 호출만 발췌한 것으로, 현재 프로젝트의 URL 캐시와 bucket 초기화는 생략했다.  
클라이언트는 요청마다 생성하지 않고 프로세스 안에서 재사용한다.  
업로드 전에 bucket이 준비되어 있어야 하며, AWS에서는 인프라 배포 단계에서 생성해 두는 방식도 사용할 수 있다.  
`head_bucket()` 실패를 모두 bucket 없음으로 판단하면 안 되며, 권한 오류와 존재 여부를 구분해야 한다.  

`boto3.client("s3")`가 제공하는 자주 쓰는 함수는 다음과 같다.  

| 함수 | 용도 |
| --- | --- |
| `head_bucket()` | bucket 존재 여부와 접근 권한 확인 |
| `create_bucket()` | bucket 생성. AWS는 리전에 따라 추가 설정이 필요할 수 있음 |
| `put_object()` | bytes 또는 바이너리 스트림 업로드. 같은 key면 덮어쓰기 |
| `upload_file()` / `upload_fileobj()` | 파일 경로·열린 스트림 업로드. 큰 파일의 multipart 전송 처리 |
| `get_object()` | 파일 조회. 반환된 `Body` 스트림은 사용 후 닫기 |
| `download_file()` / `download_fileobj()` | 파일을 로컬 경로·열린 스트림으로 다운로드 |
| `head_object()` | 본문 없이 크기·Content-Type 등 객체 메타데이터 조회 |
| `delete_object()` / `delete_objects()` | 객체 하나 또는 여러 개 삭제 |
| `list_objects_v2()` | 객체 목록 조회. 전체 조회에는 paginator 사용 |
| `get_paginator()` | 여러 페이지에 걸친 목록 API 순회 |
| `generate_presigned_url()` | `get_object`, `put_object` 등의 임시 서명 URL 생성 |
| `generate_presigned_post()` | 브라우저 form 업로드용 URL·필드 생성 및 정책 제한 |
| `close()` | 클라이언트의 HTTP 연결 풀 종료 |

### 파일 업로드·조회·삭제

boto3는 동기 SDK이므로 FastAPI의 `async def`에서는 네트워크 작업을 `asyncio.to_thread()`로 실행한다.  
업로드 파일의 크기와 실제 형식은 서버에서 먼저 검증하고, 검증된 스트림을 전달한다.  

```py
import asyncio

# storage는 기존 S3Storage 객체, upload는 검증을 마친 StagedUpload다.
try:
    saved_key = await asyncio.to_thread(
        storage.put, key, upload.stream, upload.content_type
    )
finally:
    upload.close()  # 성공·실패와 무관하게 임시 파일 정리

# 파일 메타데이터 조회. ContentType은 선언된 값이지 실제 형식의 증명은 아니다.
metadata = await asyncio.to_thread(
    storage._client.head_object, Bucket=storage._bucket, Key=saved_key
)

# 파일 삭제
await asyncio.to_thread(storage.delete, saved_key)
```

`_client`에 직접 접근한 부분은 boto3 호출을 보여주기 위한 예제다.  
서비스에서 사용한다면 필요한 조회 기능을 스토리지 인터페이스에 노출하면 된다.  

MongoDB와 S3는 하나의 트랜잭션으로 묶이지 않으므로 서비스에서 작업 순서를 정한다.  
생성은 **파일 업로드 → DB 저장**이고, DB 저장 실패 시 새 파일을 회수한다.  
수정은 **새 파일 업로드 → DB 갱신 → 이전 파일 삭제**다.  
삭제는 **DB 삭제 → 파일 삭제**이고, 파일 정리 실패로 남은 고아 파일은 별도 배치로 처리한다.  

### presigned URL

private object를 브라우저에서 다운로드하게 하려면 일정 시간만 유효한 URL을 생성한다.  

```py
# GET: 브라우저가 파일을 직접 다운로드한다.
url = storage.public_url(saved_key, minutes=10)

# PUT: 브라우저가 API 서버를 거치지 않고 직접 업로드한다.
upload_url = storage._presigner.generate_presigned_url(
    "put_object",
    Params={
        "Bucket": storage._bucket,
        "Key": key,
        "ContentType": "image/png",
    },
    ExpiresIn=600,
)
```

```js
await fetch(uploadUrl, {
  method: "PUT",
  headers: { "Content-Type": "image/png" },
  body: file,
});
```