---
title:  "Elastic Search - 데이터 모델링!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 인덱스 CRUD

데이터 모델링은 인덱스 생성하는 과정이라 할 수 있음으로  
인덱스 생성하는 방법을 먼저 알아보자.  

```json
PUT movie_search
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 1
  },
  "mappings": {
    "_doc": {
      "properties": {
        "movieCd": { "type": "keyword" },
        "movieNm": { "type": "text", "analyzer": "standard" }, 
        "movieNmEn": { "type": "text", "analyzer": "standard" },
        "prdtYear": { "type": "integer" },
        "openDt": { "type": "integer" },
        "typeNm": { "type": "keyword" },
        "prdtStatNm": { "type": "keyword" },
        "nationAlt": { "type": "keyword" },
        "genreAlt": { "type": "keyword" },
        "repNationNm": { "type": "keyword" },
        "repGenreNm": { "type": "keyword" },
        "companies": { 
          "properties": {
            "companyCd": { "type": "keyword" },
            "companyNm": { "type": "keyword" }
          }
        },
        "directors": {
          "properties": {
            "peopleNm": { "type": "keyword" }
          }
        }
      }
    }
  }
}
```

```json
GET movie_search/_mapping

// result
{
  "movie_search": {
    "mappings": {
      "_doc": {
        "properties": {
          "genreAlt": { "type": "keyword" },
          "movieCd": { "type": "keyword" },
          "movieNm": { "type": "text", "analyzer":  "standard" },
          "movieNmEn": { "type": "text", "analyzer": "standard" },
          "nationAlt": { "type": "keyword" },
          "openDt": { "type": "integer" },
          "prdtStatNm": { "type": "keyword" },
          "prdtYear": { "type": "integer" },
          "repGenreNm": { "type": "keyword" },
          "repNationNm": { "type": "keyword" },
          "typeNm": { "type": "keyword" },
          "companies": {
            "properties": {
              "companyCd": { "type": "keyword" },
              "companyNm": { "type": "keyword" }
            }
          },
          "directors": {
            "properties": {
              "peopleNm": { "type": "keyword" }
            }
          }
        }
      }
    }
  }
}
```

## 데이터 모델링

문서의 필드에 Data Type 을 지정하고 어떤 색인과정을 거치게 할 지 정하는 과정을 매핑이라 했는데  
인덱스 생성시 매핑도 같이 생성됨으로 사실상 인덱스 생성 방법을 매핑이라 할 수 있다.  

좀더 자세히 말하면 매핑은 색인 시 데이터가 어디에 어떻게 저장될지를 결정하는 설정,  
**색인될 문서의 데이터 모델링**이라고도 할 수 있다.  

생성된 매핑을 변경하려면 삭제 후 재생성 하는 방법밖에 없음으로 처음 인덱스 생성시 주의해야 한다.  

> 만약 인덱스 생성과정을 거치지 않고 문서를 생성하면 Elastic Search 가 자동으로 인덱스를 생성하고 매핑과정을 거친다.  
> (필드를 생성하고 필드 타입까지 결정)  
>
> 한번 정의되면 변경할수 없는 매핑을 자동생성에 맡기는 것은 권장하지 않는다.  

인덱스는 아래와 같은 의식의 흐름으로 구성할 수 있다.  

- 문자열을 분석할 것인가?  
- `_source` 에 어떤 필드를 정의할 것인가?  
- 날짜 필드를 가지는 필드는 무엇인가?  
- 매핑에 정의되지 않고 유입되는 필드는 어떻게 처리할 것인가?  

만약 아래와 같은 문서가 저장될 예정이라면  

```json
{
    "movieCd": "20173732",
    "movieNm": "살아남은 아이",
    "movieNmEn": "Last Child",
    "prdtYear": "2017",
    "openDt": "",
    "typeNm": "장편",
    "prdtStatNm": "기타",
    "nationAlt": "한국",
    "genreAlt": "드라마,가족",
    "repNationNm": "한국",
    "repGenreNm": "드라마",
    "directors": [{ "peopleNm": "신동석" }],
    "companies": [
        { "companyCd": "" },
        { "companyNm": "" }
    ]
}
```

아래처럼 필드를 타입과 매핑시킬 수 있다.  

매핑명 | 필드명 | 필드 타입  
|---|---|---|
인덱스 키 | movieCd | keyword  
영화제목(국문) | movieNm | text  
영화제목(영문) | movieNmEn | text  
제작연도 | prdtYear | integer  
개봉연도 | openDt | integer  
영화유형 | typeNm | keyword  
제작상태 | prdtStatNm | keyword  
제작국가(전체) | nationAlt | keyword  
장르(전체) | genreAlt | keyword  
대표 제작국가 | repNationNm | keyword  
대표 장르 | repGenreNm | keyword  
영화감독명 | directors.peopleNm | object → keyword  
제작사코드 | companies.companyCd | object → keyword  
제작사명 | companies.companyNm | object → keyword  

실제 인덱스 생성요청은 아래와 같이 json 형태로 이뤄진다.  

```json
PUT movie_search
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 1
  },
  "mappings": {
    "_doc": {
      "properties": {
        "movieCd": { "type": "keyword" },
        "movieNm": { "type": "text", "analyzer": "standard" },
        "movieNmEn": { "type": "text", "analyzer": "standard" },
        "prdtYear": { "type": "integer" },
        "openDt": { "type": "integer" },
        "typeNm": { "type": "keyword" },
        "prdtStatNm": { "type": "keyword" },
        "nationAlt": { "type": "keyword" },
        "genreAlt": { "type": "keyword" },
        "repNationNm": { "type": "keyword" },
        "repGenreNm": { "type": "keyword" },
        "companies": {
          "properties": {
            "companyCd": { "type": "keyword" },
            "companyNm": { "type": "keyword" }
          }
        },
        "directors": {
          "properties": {
            "peopleNm": { "type": "keyword" }
          }
        }
      }
    }
  }
}
```

분석기를 통한 검색대상 필드는 **영화제목** 이기 때문에 `text` 타입으로 정의한다.  
나머지 필드는 해당 정보를 그대로 보여주기만 할 것이기 때문에 특성에 따라 `integer`, `keyword` 타입으로 설정.  

> `text` 필드가 너무 많으면 문서 저장시 수많은 색인 과정이 일어나 오버헤드가 커짐으로 주의  

필드 정의할 때 `analyzer` 를 지정하여 색인 과정에서 문서가 어떻게 역색인 될지 상세하게 정의할 수 있다.  

### 데이터 타입  

**keyword**  
키워드 형태로 사용할 데이터에 적합한 데이터 타입  
원문 그대로 색인되기 때문에 정형화된 콘텐츠에 주로 사용  
집계, 정렬, 필터링 기능사용시 해당 필드 타입을 사용한다.  

**text**  
분석기를 사용하는 타입  
`text` 데이터 색인시 전체 텍스트가 토큰화되어 생성되며 특정 단어를 검색하는 것이 가능해진다(전문검색)  
해당 필드에 정렬, 집계 기능이 필요할 경우 keyword 타입을 동시에 갖는 경우도 있음  

**숫자타입**  
`long`, `integer`, `short`, `byte`, `double`, `float`  

**date**
날짜 데이터는 정형화된 문자열이라 할 수 있다.  
기본값은 ISO-8601 형태의 문자열 (`yyyy-MM-ddTHH:mm:ssZ`) 이다.  

```json
{ 
    ...
    "addDate": { "type": "date","format": "yyyy-MM-dd HH:mm:ss" } 
}
```

이 외에도 ip타입, 객체타입, 배열타입, nested(중첩)타입, geo_point, geo_shape 와 같은 데이터 타입이 존재한다.  

### 매핑 파라미터  

```json
PUT movie_search
{
  ...
  "mappings": {
    "_doc": {
      "properties": {
        ...
        "movieNmEn": { "type": "text", "analyzer": "standard" },
      }
    }
  }
}
```

위와같이 필드 정의시 `analyzer` 를 포함하여 다양한 파라미터를 제공하는데 **매핑 파라미터**라 부른다.  

#### analyzer  

해당 필드의 데이터를 **형태소 분석**하겠다는 의미의 파라미터, 지정한 분석기로 형태소 분석을 수행
text 데이터 타입의 필드는 analyzer 매핑 파라미터를 기본적으로 사용해야 한다, 분석기를 지정하지 않으면 Standard Analyzer 로 형태소 분석을 수행  

#### normalizer  
  
`cafe` , `cåƒe` , 등 문자열 문서 색인을 `normalizer` 를 통해 분석기에 `asciifolding` 과 같은 필터를 사용하면 같은 데이터로 인식되게 할 수 있다.

#### coerce

색인 시 자동 변환을 허용할지 여부를 설정. 예를 들어 `10` 숫자 형태의 문자열 이 integer 타입의 필드에 들어온다면 엘라스틱서치는 자동으로 형변환을 수행해서 정상적으로 처리한다. 하지만 coerce 미사용으로 변경한다면 색인에 실패할 것이다.

#### copy_to

필드의 값을 지정한 필드로 복사
여러 개의 필드 데이터를 하나의 필드에 모아서 전체 검색 용도로 사용하기도 한다.

```json
{
  "movieCd": "20173732", 
  "movieNm": "살아남은 아이", 
  "movieNmEn": "Last Child"
}
```

`copy_to` 파라미터를 이용하면 `movieNm` 과 `movieNmEn` 의 결과를 합해서 `살아남은 아이 Last Child` 로 데이터를 저장하는 필드 생성 가능  

이런 값을 색인으로 저장해두면 통합검색에 유리하다.  

#### fielddata, doc_values

엘라스틱서치가 힙 공간에 생성하는 메모리 캐시
메모리 부족 현상과 잦은 GC 로 현재는 거의 사용되지 않는다.

text 타입의 필드는 기본적으로 분석기에 의해 형태소 분석이 되기 때문에 집계나 정렬 등의 기능을 수행할 수 없다.  
하지만 부득이하게 text 타입의 필드에서 집계나 정렬을 수행하는 경우도 있을 것이다. 이러한 경우에 한해 fielddata 를 사용할 수 있다

text 타입의 필드를 제외한 모든 필드는 기본적 으로 doc_values 캐시를 사용한다.
운영체제의 파일 시스템 캐시를 통해 디스크에 있는 데이터에 빠르게 접근할 수 있다.

#### dynamic

필드가 추가되면 매핑에 동적으로 추가할지 않을지를 결정  

false 설정시 새로 추가되는 필드는 색인되지 않아 검색할 수 없지만 _source 에는 표시된다.  
true 설정시 매핑에 추가한다.  

#### enabled

메타 성격 데이터처럼 출력은 하고싶지만 색인은 하고싶지 않을 때 enabled 를 false 로 설정하면 _source 에는 검색이 되지만 색인은 하지 않는다.

#### format

날짜/시간 데이터를 단순 문자열로 취급함으로  
포맷 지정해서 날짜 형식으로 출력시킬 수 있다.  

#### ignore_above, ignore_malformed

`ignore_above`: 필드에 저장되는 문자열이 지정한 크기를 넘어서면 빈 값으로 색인한다.
`ignore_malformed`: 필드 포멧이 맞지않으면 해당 필드는 무시하고 문서 색인을 진행  

#### index

필드값을 색인할지를 결정한다. 기본값은 `true`  

#### fields

다중 필드(`multi_field`)를 설정할 수 있는 옵션  
필드 안에 또 다른 필드의 정보를 추가가능  

```json
PUT movie_search_mapping
{
  "mappings": {
    "_doc": {
      "properties": {
        "awards": {
          "type": "text",
          "fields": {
            "name": { 
              "type": "keyword" 
            }
          }
        }
      }
    }
  }
}
```

`awards` 필드안에 `name` 필드 정의

#### norms

유사도 스코어 계산에 필요한 정규화 인수를 사용할지 여부를 설정한다, 기본값은 `true`.  
유사도 스코어 계산이 필요없거나 단순 필터링 용도로 사용하는 필드는 비활성화해서 디스크 공간을 절약할 수 있다.  

#### null_value

`null_value`를 설정하면 입력된 문서의 필드 값이 `null` 일때 사용할 default 값을 지정할 수 있다.  

```json
PUT movie_search_mapping/_mapping/_doc
{
  "properties": {
    "audiCnt": {
      "type": "integer",
      "null_value": "0"
    }
  }
}
```

#### position_increment_gap

필드 데이 터 중 단어와 단어 사이의 간격( slop )을 허용할지를 설정

배열( Array ) 형태의 데이터를 색인할 때 검색의 정확도를 높이기 위해 제공하는 옵션
예를 들어, 데이터가 `["John Abraham", "Lincon Smith"]` 일 때 `"Abraham Lincon"` 으로 검색하더라도 검색이 가능하다.

#### properties

오브젝트(Object) 타입이나 중첩(Nested) 타입의 스키마를 정의할 때 사용
properties 의 형제레밸의 필드가 있으면 안된다.  

```json
PUT movie_search
{
  "settings": {
    "number_of_shards": 5,
    "number_of_replicas": 1
  },
  "mappings": {
    "_doc": {
      "properties": {
        "movieCd": { "type": "keyword" },
        "movieNm": { "type": "text", "analyzer": "standard" }, 
        "companies": { 
          "properties": {
            "companyCd": { "type": "keyword" },
            "companyNm": { "type": "keyword" }
          }
        },
        "directors": {
          "properties": {
            "peopleNm": { "type": "keyword" }
          }
        }
      }
    }
  }
}
```

#### search_analyzer

검색 시 사용할 분석기를 별도로 지정할 수 있다.

#### similarity

유사도 측정 알고리즘을 지정한다. 유사도 측정 방식을 기본 알고리즘인 BM25 에서 다른 알고리즘으로 변경할 수 있다.

#### store

기본적으로 문서는 색인을 통해 검색을 할수 있는데 `store` 매핑 파라미터를 사용하면 해당 필드를 자체적 저장해 검색에 이용한다.  
해당 매핑 파라미터를 사용하면 디스크를 더 많이 사용한다.

#### boost

필드의 가중치, 유사도 스코어 계산시에 필드의 가중치를 사용해 좀더 높은 스코어를 부여할 수 있다.  
검색결과 정렬에 영향을 준다, 기본값을 `1.0`.  

`keyword`, `test` 필드에서 사용 가능하다.  

### 메타 필드

```json
GET /movie/_doc/1

// result
{
  "_index": "movie",
  "_type": "_doc",
  "_id": "1",
  "_version": 1,
  "_source": {
    ...
  }
  ...
}
```

`_index`, `_type`, `_id`, `_version` 와 같이 `_` 로 시작하는 필드를 메타필드라 한다.  

**_index**  
해당 문서가 속한 인덱스의 이름을 담고 있다.  
해당 인덱스의 문서개수를 확인할 수 있다.  

**_type**  
해당 문서가 속한 매핑의 타입 정보를 담고 있다.  
해당 인덱스 내부에서 타입별로 몇 개의 문서가 있는지 확인할 수 있다  

**_id**  
문서를 식별하는 유일한 키 값

**_routing**  
특정 문서들을 하나의 샤드에 저장하고 싶을때 사용, 문서 id 를 이용해 문서가 색인될 샤드를 결정한다.  
색인할 때 해당 문서들은 동일한 라우팅 ID 를 지정한다.

```
Hash (document_id) % num_of_shards
Hash (_routing) % num_of_shards
```
