---
title:  "Elastic Search - 문서!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 문서 CRUD

### 문서 조회  

문서조회는 아래와 같이 id 를 기반으로 단건조회할 수 있다.  

```json
GET /movie/_doc/1

// result
{
  "_index": "movie",
  "_type": "_doc",
  "_id": "1",
  "_version": 1,
  "found": true,
  "_source": {
    "movieCd": "1",
    "movieNm": "살아남은 아이",
    "movieNmEn": "Last Child",
    "prdtYear": "2017"
  }
}
```

> 질의문을 통한 검색은 아래 검색쿼리 항목에서 자세히 설명  

### 문서 추가  

문서 추가는 `PUT` 메서드로 수행된다.  
`id` 값은 지정하지 않을경우 `UUID` 를 통해 무작위로 생성된다.  

```json
PUT /movie/_doc/1
{
  "movieCd": "1",
  "movieNm": "살아남은 아이",
  "movieNmEn": "Last Child",
  "prdtYear": "2017"
}

// id 없이 추가
POST /movie/_doc
{
  "movieCd": "2",
  "movieNm": "아이언맨",
  "movieNmEn": "Ironman",
  "prdtYear": "2017"
}

// result
{
    "_id": "QSx_A4gBfkTcbt6LsCrE",
    ...
}
```

타 서비스의 DB 와 동기화 해야할 때 지정하지 않은 ID 가 오류를 야기할 수 있음으로  
데이터 삽입시 id 를 지정하는 것을 권장한다.  

**op_type** 파라미터를 통해 데이터가 존재하면 수정하고 없을경우 에러를 반환하도록 설정 가능하다.  

생성하길 원한다면 `op_type` 에 `create` 를 설정  
수정하길 원한다면 `op_type` 에 `index` 를 설정  

아래의 경우 이미 문서에 `version` 값이 존재한다고 `error` 를 반환하였다.  

```json
PUT movie_dynamic/_doc/1?op_type=create
{
  "movieCd": "20173732",
  "movieNm": "살아남은 아이",
  "movieNmEn": "Last Child",
  "typeNm": "단편"
}
// result
{
  "error": {
    "type": "version_conflict_engine_exception",
    "reason": "[_doc][1]: version conflict, document already exists (current version [8])",
    ...
  },
  "status": 409
}
```

> version 값은 1 부터 시작하여 수정될때 마다 1씩 증가한다.  

**timeout** 파라미터를 통해 문서 색인 타임아웃 설정이 가능하다.  
`default=1m` 인데 이를 `timeout` 파라미터를 통해 조절할 수 있다.  

```json
PUT movie_dynamic/_doc/1?timeout=5m
{
  "movieCd": "20173732",
  "movieNm": "살아남은 아이",
  "movieNmEn": "Last Child",
  "typeNm": "단편"
}
```

### 문서 수정

추가와 수정은 모두 `PUT` 메서드를 사용한다.  
수정할 때에도 추가할 때 썼던 url 을 그대로 사용해서 수정 할 수 있다.  
또한 `_source` 필드가 활성화 돼있어야 한다.  

```json
PUT movie_dynamic/_doc/1
{
  "movieCd": "20173732",
  "movieNm": "살아남은 아이",
  "movieNmEn": "Last Child",
  "typeNm": "단편"
}
```

> 만약 수정되기만을 바란다면 위에서 알아보았던 `op_type=index` 적용  

Elastic Search 에서 수정은 재색인(reindex) 과정까지 발생하기 때문에 생성과정과 비용이 비슷하다.  

**_update** uri 를 사용하면 특정 필드만 수정할 수 있다.  

아래와 같이 문서를 먼저 생성하고  

```json
PUT movie_dynamic/_doc/1
{
  "counter": 1000,
  "movieNmEn": "Last Child"
}
```

`_update` url 을 사용해서특정 필드만 수정시킨다.  

```json
POST movie_dynamic/_doc/1/_update
{
  "script": {
    "source": "ctx._source.counter += params.count",
    "lang": "painless",
    "params": {
      "count": 1
    }
  }
}
```

다시 조회하면 아래와 같이 counter 필드가 1 증가되어있다.  

```json
GET movie_dynamic/_doc/1
//result
{
  "_id": "1",
  ...
  "_source": {
    "counter": 1001,
    "movieNmEn": "Last Child"
  }
}
```

### 문서 삭제

문서 삭제는 `DELETE` 메서드와 `id` 를 url 에 설정한다.  

```
DELETE /movie/_doc/1
```

**_delete_by_query** uri 를 사용하면 query 를 사용해 삭제할 목록을 한번에 지정 가능하다.  

> query 에 대해서는 아래 검색쿼리 항목에서 자세히 설명  

```json
POST movie_dynamic/_delete_by_query
{
  "query": {
    "term": {
      "movieCd": "20173732"
    }
  }
}
```

각 문서를 실시간으로 삭제하는 것이 아니라 인덱스의 **스냅숏**을 불러와 해당 버전의 문서들을 기반으로 삭제를 수행한다.  
만일 대량의 수정 작업이 수행 중일 때 삭제를 수행하면 두 작업의 버전이 충돌하게 되고 `version_conflicts` 항목을 통해 삭제에 실패한 문서의 건수를 출력한다.  

## 기타 API

### _bulk

한 번의 API 호출로 다수의 문서를 색인하거나 삭제할 수 있다.  
속도에서 차이가 많기에 대량 색인이 필요한 경우에는 _ bulk 를 사용하는 것이 좋다.  
하지만 여러 건의 데이터가 한 번에 처리되기 때문에 도중에 실패가 발생하 더라도 이미 갱신되거나 수정된 결과는 롤백되지 않는다는 점이다. 그러므로 항상 처리 결과를 확인해야 한다.

```json
POST _bulk
{"update":{"_index":"movie_dynamic","_type":"_doc","_id":"1"}}
{"doc":{"movieNmEn":"Last Child"}}
{"delete":{"_index":"movie_dynamic","_type":"_doc","_id":"2"}}
{"index":{"_index":"movie_dynamic","_type":"_doc","_id":"3"}}
{"title":"어벤저스"}
```

### _reindex

인덱스에서 다른 인덱스로 문서를 복사할 때 주로 사용한다.  

```json
POST /_reindex
{
  "source": {
    "index": "movie_dynamic"
  },
  "dest": {
    "index": "movie_dynamic_new"
  }
}
```

```json
POST /_reindex
{
  "source": {
    "index": "movie_dynamic",
    "type": "_doc",
    "query": {
      "term": {
        "title.keyword": "프렌즈: 몬스터섬의비밀"
      }
    }
  },
  "dest": {
    "index": "movie_dynamic_new"
  }
}
```

### Multi index

여러개의 index 를 한번에 검색할 수 있다.  

아래처럼 컴마를 사용해 Multi index 를 지정하거나  

```json
POST movie_search,movie_auto/_search
{
  "query": {
    "term": {
      "repGenreNm": "다큐멘터리"
    }
  }
}
```

아래와 같이 와일드카드 문자를 기반으로 Multi index 를 지정할 수 있다.  

```
POST /log-2019-*/_search
```
