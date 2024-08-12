---
title:  "Elastic Search - 검색 쿼리!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 검색쿼리

검색쿼리는 생성한 인덱스를 통해 문서를 검색하며 `_search` url 을 가지는 것으로 시작된다.  
제일 간단한 쿼리는 아래와 같이 `q` 파라미터에 바로 용어와 일치하는 문자열을 검색하는 방식이다.  

```
GET movie/_search?q=2016
```

좀더 상세히 조건을 지정하고 싶다면 아래 2가지 방식을 사용하면 된다.  

- `HTTP URI(GET)` 형태의 파라미터를 추가해 검색하는 방법  
- `QueryDSL(POST)` 을 `Request Body` 에 추가해 검색하는 방법  

상세히 쿼리를 지정할 수 있는 `QueryDSL` 방식을 선호한다.  
`Request Body`를 사용하면 복잡한 쿼리는 JSON 형식으로 간결하게 표기할 수 있을 뿐더러 URL 방식에서 사용하는 유니코드 `base64` 변환에서도 해방된다.  

```json
// HTTP URL 방식
GET movie/_search?q=prdtYear:2017

// Request Body 방식
POST movie/_search
{
  "query": {
    "term": {
      "prdtYear": "2017"
    }
  }
}

// HTTP URL + Request Body 혼합
POST movie/_search?q=prdtYear:2016
{
  "query": {
    "term": {
      "typeNm": "장편"
    }
  }
}

// result
{
  "hits": {
    "total": 1,
    "max_score": 0.2876821,
    "hits": [
      {
        "_index": "movie",
        ...
        "_score": 0.2876821,
        "_source": {
            "typeNm": "장편",
            ...
        }
      }
    ]
  }
}
```

**QueryDSL 구조** 는 아래와 같다.  

```json
POST movie_search/_search
{
  "from": 0,
  "size": 5,
  "timeout": "1m",
  "_source": [ "movieCd", "movieNm", "mvoieNmEn", "typeNm" ],
  "sort": [
    {
      "_score": { "order": "desc" }, // 1순위
      "movieCd": { "order": "asc" } // 2순위
    }
  ],
  "query": {
    "query_string": {
      "default_field": "movieNmEn",
      "query": "movieNmEn:* OR prdtYear:2017"
    }
  }
}
```

**size(default 10)**  
몇 개의 결과를 반환할지 결정한다, 페이징 처리에서 주로 사용  

**from(default 0)**  
어느 위치부터 반환할지를 결정한다. 0부터 시작하면 상위 0~10건의 데이터를 반환한다, 페이징 처리에서 주로 사용  
size, from 지정해도 풀스캔임으로 주의  

**timeout(default infin)**
검색을 요청해서 결과를 받는 데까지 걸리는 시간, timeout 을 넘기지 않은 문서만 결과로 출력되어 일부만 출력될 수 있다.  

**_source**  
특정 필드만 결과로 반환하고 싶을 때 사용한다.  

**sort**  
특정 필드를 기준으로 정렬한다. asc, desc 지정 가능  

**query**  
검색될 조건을 정의한다  

`QueryDSL` 에서 가장 중요한 부분이 질의문에 해당하는 **query** 영역일 것이다.  
질의문은 분석기에 의한 전문 분석을 진행하는 **쿼리 컨텍스트** 와 단순 `Yes/No` 로 검색하는 **필터 컨텍스트** 로 구분된다.  

### 쿼리 컨텍스트

`query` 속성을 사용해 문서가 쿼리와 얼마나 유사한지를 **Lucene기반 분석기를 사용해 스코어를 기반**으로 출력한다.  


#### match  

텍스트, 숫자, 날짜 등이 포함된 문장을 형태소 분석을 통해 `term` 으로 분리, 검색 질의를 수행한다.  

분석질의를 하는 쿼리이다 보니 내부에 여러가지 부가 옵션설정이 가능하다.  

**operator**  
`AND, OR` 조건을 지정할 수 있다.  

```json
POST movie_search/_search
{
  "query": {
    "match": {
      "movieNm": {
        "query": "그대 장미",
        "operator": "and" // default "or"
      }
    }
  }
}
```

위의 경우 `operator` 에 `and` 를 지정했음으로 `그대` 문자열과 `장미` 문자열이 동시에 존재하는 문서만 검색된다.  

**minimum_should_match**  
OR 조건을 사용하면서 매칭개수를 지정하여 AND 효과를 낼 수 있다.  

```json
POST movie_search/_search
{
  "query": {
    "match": {
      "movieNm": {
        "query": "그대 장미 자전거 여행",
        "minimum_should_match": 2
      }
    }
  }
}
```

3개 문자열중 2개이상 일치하는 문서를 찾는다.  

**fuzziness**  
Levenshtein 편집거리 알고리즘을 기반으로 문서를 탐색한다.  
오차로 설정하수 있는 값은 `0, 1, 2, AUTO`  

```json
POST movie_search/_search
{
  "query": {
    "match": {
      "movieNmEn": {
        "query": "Fli High",
        "fuzziness": 2
      }
    }
  }
}
```

편집거리가 2 이하인 문서들을 찾는다.  

#### match_all

```json
GET /apache-web-log/_search?size=0 
{
  "query": {
    "match_all": {}
  }
}
```

#### query_string

내장된 쿼리 분석기을 이용한 쿼리  
단어는 형태소분석을 통해 분리되고 AND, OR 조건에 따라 일치하는 문서를 반환한다.  

```json
POST movie_search/_search
{
  "query": {
    "query_string": {
      "default_field": "movieNm",
      "query": "(가정) OR (어린이 날)"
    }
  }
}
```


#### multi_match  

여러개의 필드를 대상으로 쿼리가능하다.  

```json
POST movie_search/_search
{
  "query": {
    "multi_match": {
      "fields": [ "movieNm", "movieNmEn" ],
      "query": "가족"
    }
  }
}
```

#### range

범위검색이 가능, 아래와 같은 연산자가 존재한다.  

- `lt`: `<`  
- `gt`: `>`  
- `lte`: `<=`  
- `gte`: `>=`  

```json
POST movie_search/_search
{
  "query": {
    "range": {
      "prdtYear": {
        "gte": "2016",
        "lte": "2017"
      }
    }
  }
}
```

#### term

`Keyword` 데이터 타입을 사용하는 필드를 검색할 때 사용

```json
POST movie_search/_search
{
  "query": {
    "term": {
      "genreAlt": "코미디"
    }
  }
}
```

#### prefix

접두어가 있는 모든 문서를 검색하는 데 사용한다.  
`keyword` 의 경우 접두문자가 있는지 검사하고 `text` 의 경우 분석된 각 토큰의 접두문자가 있는지 검사한다.  

```json
POST movie_search/_search
{
  "query": {
    "prefix": {
      "movieNm": "자전거"
    }
  }
}
```

#### exists

필드의 값이 null 이거나 필드 자체가 존재하지 않는 데이터를 제외하고 실제 값이 존재하는 문서만 찾고 싶을 때 사용  

```json
POST movie_search/_search
{
  "query": {
    "exists": {
      "field": "movieNm"
    }
  }
}
```

#### wildcard

와일드카드와 일치하는 구문을 찾는다.

- `*`: 문자의 길이와 상관없이 와일드카드와 일치하는 모든 문서를 찾는다.  
- `?`: 지정된 위치의 한 글자가 다른 경우의 문서를 찾는다.  

쿼리로 전달된 문자열은 형태소 분석이 이뤄지지 않는다.  

```json
POST movie_search/_search
{
  "query": {
    "wildcard": {
      "movieNm": "경?관"
    }
  }
}
```

#### constant_score

`constant_score` 는 `score` 계산을 무시하는 쿼리로 `score` 가 1로 고정된다.  
내부 filter 속성에 쿼리를 작성하면 된다.  

집계쿼리 혹은 `score` 가 중요하지않은 검색에서 주로 사용한다.  

```json
POST movie_search/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "term": { "typeNm": "단편" }
      }
    }
  }
}

POST movie_search/_search
{
  "query": {
    "constant_score": {
      "filter": {
        "match": { "movieNm": "자전거" }
      }
    }
  }
}
```

#### bool

**각종 쿼리를 조합**해 복합적인 조건을 만족하는 문서를 조회할 수 있다.  

`bool` 쿼리에서 지원하는 논리 조건절은 아래 4가지  

| 필드명   | SQL 비유         |
| -------- | ---------------- |
| must     | AND 칼럼 = 조건  |
| must_not | AND 칼럼 != 조건 |
| should   | OR 칼럼 = 조건   |
| filter   | 칼럼 IN ( 조건 ) |

```json
POST movie_search/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "term": { "repNationNm": "미국" }
        },
        {
          "term": { "repGenreNm": "코미디" }
        }
      ],
      "filter": [
        {
          "term": { "typeNm": "단편" }
        }
      ]
    }
  }
}
```

`bool` 쿼리 특성상 `term` 질의문을 결합하여 구성하였는데 다른 쿼리들도 조합 가능하다.  

#### nested

중첨구조 형태의 문서에서 조회할 때 사용, `nested` 타입 문서에 질의가 가능하다.  
다행이 Elastic Search 에선 용량이 커서 나눠지지 않는 이상 `Parent`, `Child` 문서를 모두 동일한 샤드에 저장하여 `nested` 질의 시 네트워크 리소스 소모가 적다.  

아래와 같이 `companies` 문서를 중첩구조로 설정해서 인덱스 생성  

```json
PUT movie_nested
{
  "settings": {
    "number_of_replicas": 1,
    "number_of_shards": 5
  },
  "mappings": {
    "_doc": {
      "properties": {
        "repGenreNm": { "type": "keyword" },
        "companies": {
          "type": "nested",
          "properties": {
            "companyCd": { "type": "keyword" },
            "companyNm": { "type": "keyword" }
          }
        }
      }
    }
  }
}
```

아래와 같이 문서를 생성  

```json
PUT movie_nested/_doc/1
{
  "movieCd": "20184623",
  "movieNm": "바람난 아내들2",
  "movieNmEn": "",
  "prdtYear": "2018",
  "openDt": "",
  "typeNm": "장편",
  "prdtStatNm": "개봉예정",
  "nationAlt": "한국",
  "genreAlt": "멜로/로맨스",
  "repNationNm": "한국",
  "repGenreNm": "멜로/로맨스",
  "companies": [
    {
      "companyCd": "20173401",
      "companyNm": "(주)케이피에이기획"
    }
  ]
}
```

`nested` 옵션을 사용해 중첩문서에 조건문 지정해서 문서 검색 가능  

```json
GET movie_nested/_search
{
  "query": {
    "nested": {
      "path": "companies",
      "query": {
        "term": { "companies.companyCd": "20173401" }
      }
    }
  }
}
```

만약 `Parent` 와 `Child` 문서의 조건을 결합하여 질의하려면 전체적으로 `bool` 쿼리로 감쌓야한다.  

```json
GET movie_nested/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "term": { "repGenreNm": "멜로/로맨스" }
        },
        {
          "nested": {
            "path": "companies",
            "query": {
              "bool": {
                "must": [
                  {
                    "term": { "companies.companyCd": "20173401" }
                  }
                ]
              }
            }
          }
        }
      ]
    }
  }
}
```

### highlight

문서를 검색할 때 `highlight` 옵션을 사용해 일치된 부분에 하이라이트 처리를 할 수 있다.  


```json
PUT movie_highlighting/_doc/1
{
  "title": "Harry Potter and the Deathly Hallows"
}

POST movie_highlighting/_search
{
  "query": {
    "match": {
      "title": {
        "query": "harry"
      }
    }
  },
  "highlight": {
    "pre_tags": [ "<strong>" ], // default <em>
    "post_tags": [ "</strong>" ], // default </em>
    "fields": {
      "title": {}
    }
  }
}
```

아래와 같이 검색된 결과에 `highlight` 속성이 추가된다.  

```json
// result
{
  ...
  "hits": {
    ...
    "hits": [
      {
        "_index": "movie_highlighting",
        "_type": "_doc",
        "_id": "1",
        "_score": 0.2876821,
        "_source": {
          "title": "Harry Potter and the Deathly Hallows"
        },
        "highlight": {
          "title": [
            "<strong>Harry</strong> Potter and the Deathly Hallows"
          ]
        }
      }
    ]
  }
}
```

### 스크립팅

Elastic Search 에서는 스크립팅 이라는 **쿼리에 스크립트를 추가해서 동적으로 로직을 수행**하는 기능이 있다.  

Painless
Expression
Mustache
Java

### Suggest API

검색에 사용한 문자열과 일치하는 문서가 없을경우 빈 결과를 반환하는데  
`suggest` 옵션을 사용하면 해당 검색 문자열에 **제안(suggest) 알고리즘**을 통해 결과를 반환할 수 있다.  

Elastic Search 에서 기본적으로 제공되는 기능이다.  

#### Term Suggester API

오타 수정을 위한 기능으로 `suggest` 옵션안에 `term` 옵션을 사용한다.  

`lave` 와 편집거리가 짧은 `term` 을 찾는다.  

```json
PUT movie_term_suggest/_doc/1
{ "movieNm": "lover" } 
PUT movie_term_suggest/_doc/2
{ "movieNm": "Fall love" } 
PUT movie_term_suggest/_doc/3
{ "movieNm": "lovely" } 
PUT movie_term_suggest/_doc/4
{ "movieNm": "lovestory" }
```

위와 같이 제안에 사용할 `love` 문자열을 포함한 여러 문서를 삽입  

```json
POST movie_term_suggest/_search
{
  "suggest": {
    "spell-suggestion": {
      "text": "lave",
      "term": {
        "field": "movieNm"
      }
    }
  }
}
// result
"hits": {
  "total": 0,
  "max_score": 0,
  "hits": []
},
"suggest": {
  "spell-suggestion": [
    {
      "text": "lave",
      "offset": 0,
      "length": 4,
      "options": [
        {
          "text": "love",
          "score": 0.75,
          "freq": 1
        },
        {
          "text": "lover",
          "score": 0.5,
          "freq": 1
        }
      ]
    }
  ]
}
```

결과는 `hits` 는 비어있고 `suggest` 만 채워져있는데 인덱스에서 검색된 `term` 이 출력된다.  

#### Completion Suggest API

자동완성을 위한 기능으로 `suggest` 옵션 안에 `completion` 옵셥을 사용한다.  

자동완성기능은 한글자 입력될 때 마다 호출이 이루어짐으로 속도가 중요하며 `FST(Finite State Transducer)`를 사용한다.  
`FST` 기능을 사용하기 위해 필드 type 에 `completion` 을 사용한다.  

> **FST: Finite State Transducer**  
> Completion Suggest 로 사용할 필드를 모두 메모리에 로드하는 구조,  
> 성능 최적화를 위해 색인 중에 FST 를 작성하게 된다.  

```json
// 인덱스 정의
PUT movie_term_completion
{
  "mappings": {
    "_doc": {
      "properties": {
        "movieNmEnComple": { "type": "completion" }
      }
    }
  }
}

// 문서 삽입
PUT movie_term_completion/_doc/1 
{ "movieNmEnComple": "After Love" }
PUT movie_term_completion/_doc/2 
{ "movieNmEnComple": "Lover" } 
PUT movie_term_completion/_doc/3 
{ "movieNmEnComple": "Love for a mother" } 
PUT movie_term_completion/_doc/4 
{ "movieNmEnComple": "Fall love" } 
PUT movie_term_completion/_doc/5 
{ "movieNmEnComple": "My lovely wife" }
```

제안방식으로 prefix 를 사용한다.  

> `Completion Suggest API` 는 전방일치 방식만 지원한다.  

```json
POST movie_term_completion/_search
{
  "suggest": {
    "movie_completion": {
      "prefix": "l",
      "completion": {
        "field": "movieNmEnComple",
        "size": 5
      }
    }
  }
}

// result
"suggest": {
  "movie_completion": [
    {
      ...
      "options": [
        {
          "text": "Love for a mother",
          "_id": "3",
          ...
        },
        {
          "text": "Lover",
          "_id": "2",
          ...
        }
      ]
    }
  ]
}
```

`prefix` 이다 보니 앞에 `l` 로 시작하는 2개 문서만 제안된다.  

모든 단어에 대해 completion 을 수행하고 싶다면 아래처럼 문자열 리스트로 저장한다.  
다시 검색해보면 l 시작단어를 가지는 모든 문서가 출력된다.  

```json
PUT movie_term_completion/_doc/1 
{ "movieNmEnComple": ["After", "Love"] }
PUT movie_term_completion/_doc/2 
{ "movieNmEnComple": ["Lover"] } 
PUT movie_term_completion/_doc/3 
{ "movieNmEnComple": ["Love", "for", "a", "mother"] } 
PUT movie_term_completion/_doc/4 
{ "movieNmEnComple": ["Fall", "love"] } 
PUT movie_term_completion/_doc/5 
{ "movieNmEnComple": ["My", "lovely", "wife"] }
```

