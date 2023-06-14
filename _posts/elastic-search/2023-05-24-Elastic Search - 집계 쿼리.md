---
title:  "Elastic Search - 집계 쿼리!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 집계쿼리

Elastic Search 에선 데이터를 그룹화해서 각종 **통계지표**를 제공하기 위해 독자적인 `Aggregation API` 를 제공한다.  
`SQL` 의 `GroupBy` 연산과 비슷하다.  

또한 솔라의 분산노드 집계 기술인 `Facet API` 보다 효율적인 `Aggregation API` 를 사용한다.  

- **메트릭 집계 Metric Aggregation**  
  쿼리결과로 출력된 문서에서 추출된 값을 가지고 Sum, Max, Min, Avg 등의 산술연산을 수행한다.  

- **버킷 집계 Bucket Aggregation**  
  집계 중 가장 많이 사용한다. 쿼리결과로 출력된 문서의 필드를 기준으로 그룹화, 산술연산을 수행하여 집계처리, 나눠진 그룹이 각 버킷에 해당된다.  

- **파이프라인 집계 Pipeline Aggregation**  
  버킷에서 도출된 결과 문서를 다시 재분류, 즉 다른 집계에 의해 생성된 출력 결과를 다시 한번 집계한다.  
  Elastic Search 에서만 중첩집계를 지원한다.  

- **행렬 집계 Matrix Aggregation**  
  쿼리결과로 출력된 문서의 필드를 기반으로 행렬을 만들고 값을 합하거나 곱한다. 실험적인 기능이라 잘 사용 안함  

예시로 `apache web log` 에서 다음과 같이 `region_name` 을 기반으로 로그개수를 카운팅해 집계를 내릴 수 있다.  

`query` 와 `aggs` 를 같이 사용하여 질의 결과 안에서 집계가 수행하는 구조이다.  
`size` 를 0 으로 설정해서 query 로 출력된 결과는 생략하는 편이다.  

```json
GET /apache-web-log/_search?size=0
{ 
  "query": {
    "match_all": {}
  },
  "aggs": {
    "region_count": {
      "terms": { "field": "geoip.region_name.keyword", "size": 20 }
    }
  }
}
```

> `aggregations` 을 `aggs` 로 줄여서 사용 가능  
> 만약 `query` 가 생략되면 자동으로 `match_all` 쿼리를 사용한다.  

실제 `apatch web log` 의 `geoip` 필드의 경우 아래와 같이 설정되어 있는데  
대부분 필드가 `text` 타입이면서 내부적으로 `keyword` 필드를 하나 더 가지고있고 해당 필드의 `Data type` 은 `keyword` 이다.  

집계쿼리에선 항상 keyword 를 기반으로 집계해야 성능에 문제가 생기지 않는다.  

```json
{
    "geoip": {
        "properties": {
            "region_name": {
                "type": "text",
                "fields": {
                    "keyword": { "type": "keyword", "ignore_above": 256 }
                }
            },
            "city_name": {
                "type": "text",
                "fields": {
                    "keyword": { "type": "keyword", "ignore_above": 256 }
                }
            },
            "continent_code": {
                "type": "text",
                "fields": {
                    "keyword": { "type": "keyword", "ignore_above": 256 }
                }
            },
            ...
        }
    }
    ...
}
```

### 집계쿼리 구조  

```json
GET /apache-web-log/_search?size=0
{
  "query": {
    "constant_score": {
      "filter": {
        "match": {
          "geoip.country_name": "United States"
        }
      }
    }
  },
  "aggs": {
    "us_city_names": { // 집계이름
      "terms": { // 집계타입
        "field": "geoip.city_name.keyword" // 필드명
      }
    }
  }
}
```

- **집계이름**: 출력 구분용  
- **집계타입**: 합계, 평균, 시계열 등의 집계연산타입을 명시  
- **필드명**  : 집계의 대상이 되는 필드를 명시  

출력결과는 아래와 같은 항목으로 나뉜다.  

```json
{
  "took": 767, // 소요 시간
  "timed_out": false, // 초과 여부
  "_shards": {
    "total": 5,
    "successful": 5,
    "skipped": 0, // 검색 요청에 응답하지 않은 샤드수
    "failed": 0 // 실패 샤드 수
  },
  "hits": {
    "total": 4255, // 집계 문서 수
    "max_score": 0, // 최대 스코어 값
    "hits": []
  },
  "aggregations": { // 집계 결과
    "us_city_names": { // 집계 이름
      "doc_count_error_upper_bound": 30,
      "sum_other_doc_count": 1955,
      "buckets": [ ... ]
    }
  }
}
```

분산노드 구조에서 `Aggregation API` 는 각 노드에서 집계연산을 수행한 후 병합하는 형식으로 운영된다.  

이때문에 어떤 연산은 `size=0` 으로 설정해서 각 노드의 모든 문서에 대해 집계하도록 설정해야 정확한 값을 얻을 수 있다.  
만약 분산환경에서 `size=n` 를 지정하면 각 분산된 노드에서 n개 문서 만큼만 집계하기 때문에 병합한 최종결과가 정확하지 않을 수 있다.  

### 캐시  

집계 쿼리로 값을 조회하면 **마스터 노드**가 여러 노드에 있는 데이터를 집계해 질의에 답변한다.  
데이터의 양이 클수록 각 노드의 많은 양의 CPU 와 메모리 자원이 소모된다.  

캐시를 사용하면 같은 질의에 대해선 버퍼에 보관된 결과를 반환한다.  

`elasticsearch.yml` 에서 결과값이 저장될 캐ㅌ시의 용량을 설정할 수 있다.  

캐시사용 전략 아래 3가지  

- **Node query Cache**: `LRU(Least-Recently-Used)` 캐시 사용전략으로 기본적으로 사용한다.  
- **Shard request Cache**: 각 샤드에서 수행된 쿼리의 결과를 캐싱, 샤드의 내용이 변경되면 캐시가 삭제됨으로 사용에 주의  
- **Field data Cache**: 집계연산에 필요한 모든 필드값을 메모리에 캐싱해두고 계산, 비용이 많이 드는 전략임으로 사용에 주의  

## 메트릭 집계

매트릭 집계는 각종 `sum`, `avg`, `min`, `max` 등 여러가지 작업을 수행한다.  
메트릭 집계는 100% 정확하다고 보장할 수 없다. 메모리를 초과하는 데이터 집계를 수행해야 할 경우 그루핑(카디널리티), 백분위 수 집계에서 근사치 계산을 하기 때문  

### sum, avg

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "total_bytes": {
      "sum": { "field": "bytes" }
    },
    "avg_bytes": {
      "avg": { "field": "bytes" }
    }
  }
}

// result
"aggregations": {
  "total_bytes": {
    "value": 2747282505
  },
  "avg_bytes": {
    "value": 294456.8601286174
  }
}
```

`aggregations` 필드를 보면 모든 `byte` 의 합계와 평균을 출력시킨 것을 알 수 있다.  

### min, max

```json
GET /apache-web-log/_search?size=0
{ 
  "aggs": {
    "min_bytes": {
      "min": { "field": "bytes" }
    },
    "max_byte": {
      "max": { "field": "bytes" }
    }
  }
}

// result
"aggregations": {
  "max_byte": {
    "value": 69192717
  },
  "min_bytes": {
    "value": 35
  }
}
```

### value_count

단일 숫자 메트릭 집계 함수  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_count": {
      "value_count": { "field": "bytes" }
    }
  }
}

// result
"aggregations": {
  "bytes_count": {
    "value": 9330
  }
}
```

### stats, extended_stats

통계 집계(Stats Aggregation) 기능이며 앞서 수행했던 sum, avg, max, min 을 한번에 수행한다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_stats": {
      "stats": { "field": "bytes" }
    }
  }
}

// result
"aggregations": {
  "bytes_stats": {
    "count": 9330,
    "min": 35,
    "max": 69192717,
    "avg": 294456.8601286174,
    "sum": 2747282505
  }
}
```

`extended_stats` 속성을 사용하면 아래 5가지에 대한 추가값을 얻을 수 있다.  

- **sum_of_squares**: 제곱합  
- **variance**: 분산  
- **std_deviation**: 표준편차  
- **std_deviation_bounds**: 표준편차 상한  
- **std_deviation_bounds**: 표준편차 하한  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_extended_stats": {
      "extended_stats": { "field": "bytes" }
    }
  }
}

// result
"aggregations": {
  "bytes_extended_stats": {
    "count": 9330,
    "min": 35,
    "max": 69192717,
    "avg": 294456.8601286174,
    "sum": 2747282505,
    "sum_of_squares": 118280314234513340, // 제곱합
    "variance": 12590713617814.016, // 분산
    "std_deviation": 3548339.5578515334, // 표준편차
    "std_deviation_bounds": { // 표준편차의 범위
      "upper": 7391135.975831684, // 표준편차 상한
      "lower": -6802222.25557445 // 표준편차 하한
    }
  }
}
```

### percentiles, percentile_ranks

백분위 수 집계가 가능하다.  

> 백분위 수: 자료를 순서대로 나열했을 때 백분율로 나타낸 특정 위치의 값을 이르는 용어

백분위 수 집계할 고유값의 개수가 메모리 크기를 넘어가는 대량의 데이터일 경우  
집계할 때에는 근사치 계산을 수행하여 결과에 손실이 발생할 수 있다.  

```json
GET /apache-web-log/_search?size=0
{ 
  "aggs": {
    "bytes_percentiles": {
      "percentiles": {
        "field": "bytes",
        "percents": [ 1, 5, 25, 50, 75, 95, 99 ]
      }
    }
  }
}

// result
"aggregations": {
  "bytes_percentiles": {
    "values": {
      "1.0": 229,
      "5.0": 358,
      "25.0": 3644.410933660933,
      "50.0": 12245.588661832564,
      "75.0": 37522.76424999999,
      "95.0": 171240,
      "99.0": 1204031.8000000163
    }
  }
}
```

`percentile_ranks` 는 입력된 수치에 해당하는 백분위 수 를 확인할 수 있다.  

```json
GET /apache-web-log/_search?size=0
{ 
  "aggs": {
    "bytes_percentile_ranks": {
      "percentile_ranks": {
        "field": "bytes",
        "values": [ 5000, 10000 ]
      }
    }
  }
}
```

### geo_bounds, geo_centroid

`geo_bounds` 속성은 `geo_point` 타입에 대해 **지형 경계 집계(Geo Bounds Aggregation)**를 수행한다.  

**경계 집계**는 수집된 데이터의 범위 중 가장 끝부분에 위치한 정보로 경계가 정해지기 때문에 굉장히 넓은 범위의 위도, 경도 좌표가 지정된다  

```json
GET /apache-web-log-applied-mapping/_search?size=0?
{ 
  "aggs": {
    "viewport": {
      "geo_bounds": {
        "field": "geoip.location",
        "wrap_longitude": true
      }
    }
  }
}

// result
"aggregations": {
  "viewport": {
    "bounds": {
      "top_left": {
        "lat": 69.34059997089207,
        "lon": -159.76670005358756
      },
      "bottom_right": {
        "lat": -45.88390002027154,
        "lon": 176.91669998690486
      }
    }
  }
}
```

`geo_centroid` 는 검색된 `geo_point` 의 중심부의 위치가 반환된다.  

```json
GET /apache-web-log-applied-mapping/_search?size=0?
{
  "aggs": {
    "centroid": {
      "geo_centroid": {
        "field": "geoip.location"
      }
    }
  }
}

// result
"aggregations": {
  "centroid": {
    "location": {
      "lat": 38.715619301146354,
      "lon": -22.189867686554656
    },
    "count": 9993
  }
}
```

### script  

아래와 같이 `script` 속성을 사용하면 라이브러리를 사용해서 스크립트내부에 정의해둔 코드를 실행시킬 수 있다.  

```json
GET /apache-web-log/_search?size=0
{
  "query": {
    "constant_score": {
      "filter": {
        "match": { "geoip.city_name": "Paris" }
      }
    }
  },
  "aggs": {
    "total_bytes": {
      "sum": {
        "script": {
          "lang": "painless",
          "source": "(double) doc.bytes.value / params.divide_value",
          "params": { "divide_value": 1000 }
        }
      }
    }
  }
}
```

## 버킷 집계

**버킷 집계** 는 메트릭 집계와는 다르게 메트릭을 계산하지 않고 카운팅하는 집계이며 버킷을 생성한다.  
문서의 분류에 목적을 둔 집계이다.  

### bytes_range, bytes_histogram

`bytes_range` 는 지정한 숫자 범위를 기준으로 집계한다.  
데이터 크기가 `1000 ~ 2000 byte` 에 해당하는 문서가 754 개인 것을 확인

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_range": {
      "range": {
        "field": "bytes",
        "ranges": [
          { "key": "start", "to": 1000 },
          { "from": 1000, "to": 2000 },
          { "from": 2000, "to": 3000 }
        ]
      }
    }
  }
}

// result
"aggregations": {
  "bytes_range": {
    "buckets": [
      { "key": "start", "to": 1000, "doc_count": 666 },
      { "key": "1000.0-2000.0", "from": 1000, "to": 2000, "doc_count": 754 },
      { "key": "2000.0-3000.0", "from": 2000, "to": 3000, "doc_count": 81 }
    ]
  }
}
```

`bytes_histogram` 은 숫자 간격별로 범위를 지정해 집계한다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_histogram": {
      "histogram": {
        "field": "bytes",
        "interval": 10000,
        "min_doc_count": 1 // default 0
      }
    }
  }
}

// result
"aggregations": {
  "bytes_histogram": {
    "buckets": [
      {
        "key": 0,
        "doc_count": 4196
      },
      {
        "key": 10000,
        "doc_count": 1930
      },
      {
        "key": 20000,
        "doc_count": 539
      }, ... ,
      {
        "key": 69190000,
        "doc_count": 2
      }
    ]
  }
}
```

### date_range, date_histogram

`date_range` 는 날짜 값을 범위로 집계를 수행한다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "bytes_range": {
      "date_range": {
        "field": "timestamp",
        "ranges": [
          {
            "key": "ragne-test",
            "from": "2015-01-04T05:14:00.000Z",
            "to": "2015-01-04T05:16:00.000Z"
          }
        ]
      }
    }
  }
}

// result
"aggregations": {
  "bytes_range": {
    "buckets": [
      {
        "key": "ragne-test",
        "from": 1420348440000,
        "from_as_string": "2015-01-04T05:14:00.000Z",
        "to": 1420348560000,
        "to_as_string": "2015-01-04T05:16:00.000Z",
        "doc_count": 0
      }
    ]
  }
}
```

> date 필드가 내부적으로 밀리초를 따로 저장하는 듯  

`date_histogram` 는 날짜 간격별로 범위를 지정해 집계한다.  

```json
GET /apache-web-log/_search?size=0
{ 
  "aggs": {
    "daily_request_count": {
      "date_histogram": {
        "field": "timestamp",
        "interval": "hour",
        "format": "yyyy-MM-dd'T'hh:mm:ssZZ",
        "time_zone": "+09:00"
      }
    }
  }
}

// result
"aggregations": {
  "daily_request_count": {
    "buckets": [
      {
        "key_as_string": "2015-05-17T19:00:00+09:00",
        "key": 1431856800000,
        "doc_count": 74
      },
      {
        "key_as_string": "2015-05-17T20:00:00+09:00",
        "key": 1431860400000,
        "doc_count": 111
      }, ... ,
      {
        "key_as_string": "2015-05-21T06:00:00+09:00",
        "key": 1432155600000,
        "doc_count": 86
      }
    ]
  }
}
```

### terms

카디널리티 집계(Cardinality Aggregation)를 사용해 **단일 숫자 메트릭 집계**한다.  
`value_count` 에 `Grouping` 기능을 합친것이라 볼 수 있다.  

> 카디널리티: 측정기준에 할당된 고유한 값의 개수

카디널리티 집계는 해시기반의 `HyperLogLog++` 알고리즘을 기반으로 동작하는데  
그룹으로 묶을 키의 분포값을 `precision_threshold` 속성으로 설정할 수 있다.  
만약 `precision_threshold` 값보다 고유키값이 더 많다면 결과에 손실이 발생할 수 있다.  

`constant_score` 를 통해 검색된 내용으로부터 `geoip.city_name.keyword` 를 기준으로 그룹화하여 카운팅한다.  
`doc_count` 순으로 출력된다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "us_city_names": {
      "terms": {
        "field": "geoip.city_name.keyword",
        "size": 10 // defaeult 10
      }
    }
  }
}

// result
"aggregations": {
  "us_city_names": {
    "doc_count_error_upper_bound": 56, // 오차 상한선
    "sum_other_doc_count": 5445, // 결과 미포함 문서 수
    "buckets": [
      {
        "key": "Leander",
        "doc_count": 539
      },
      {
        "key": "Lititz",
        "doc_count": 273
      }, ... ,
      {
        "key": "Morganton",
        "doc_count": 108
      }
    ]
  }
}
```

### 버킷집계와 메트릭 집계  

버킷집계로부터 검색된 문서들에 대해 메트릭집계를 수행할 수 있다.  

버킷집계인 `date_histogram` 내부에 메트릭 집계인 `sum` 을 설정한다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "histo": {
      "date_histogram": {
        "field": "timestamp",
        "interval": "day",
        "min_doc_count": 1
      },
      "aggs": {
        "bytes_sum": {
          "sum": { "field": "bytes" }
        }
      }
    }
  }
}

// result
"aggregations": {
  "histo": {
    "buckets": [
      {
        "key_as_string": "2015-05-17T00:00:00.000Z",
        "key": 1431820800000,
        "doc_count": 1632,
        "bytes_sum": { "value": 414259902 }
      }, ... ,
      {
        "key_as_string": "2015-05-20T00:00:00.000Z",
        "key": 1432080000000,
        "doc_count": 2578,
        "bytes_sum": { "value": 878559106 }
      }
    ]
  }
}
```

## 파이프라인 집계

쿼리조건으로 출력된 결과에서 집계하는 것이 아니라  
다른 집계 결과 bucket 으로부터 다시 집계를 수행한다.  

파이프라인 집계는 모든 집계가 완료 후 생성된 버킷을 사용하고 파이프라인 집계간 `bucket_path` 파라미터로 서로 참조할 수 있다.  

파이프라인 집계에는 아래 두 가지 유형이 존재  

- 부모(Parent)  
- 형제(Sibling)  

### 형제집계

동일한 위치에 새로운 집계 결과가 추가되는 것이 형제집계.  
검색된 버킷으로부터 여러가지 집계를 다시 수행할 수 있다.  

아래의 경우 형제집계 연산중 하나인 `max_bucket` 을 사용해서 출력된 버킷 집계들중 가장 큰 값을 구하는 집계  

`buckets_path` 를 통해 집계할 필드를 지정한다.  

```json
GET /apache-web-log/_search?size=0
{
  "aggs": {
    "histo": {
      "date_histogram": {
        "field": "timestamp",
        "interval": "day"
      },
      "aggs": {
        "bytes_sum": {
          "sum": { "field": "bytes" }
        }
      }
    },
    "max_bytes": {
      "max_bucket": { "buckets_path": "histo>bytes_sum" }
    }
  }
}

// result
"aggregations": {
  "histo": {
    "buckets": [
      {
        "key_as_string": "2015-05-17T00:00:00.000Z",
        "key": 1431820800000,
        "doc_count": 1632,
        "bytes_sum": { "value": 414259902 }
      }, ... ,
      {
        "key_as_string": "2015-05-20T00:00:00.000Z",
        "key": 1432080000000,
        "doc_count": 2578,
        "bytes_sum": { "value": 878559106 }
      }
    ]
  },
  "max_bytes": {
    "value": 878559106,
    "keys": [
      "2015-05-20T00:00:00.000Z"
    ]
  }
}
```

맨마지막 `max_bytes` 형제집계 결과를 보면 가장 큰값의 key 와 value 를 출력한다.  

형제집계에는 아래와 같은 집계연산들을 수행할 수 있다.  

- `avg_bucket`: 평균 버킷 집계  
- `max_bucket`: 최대 버킷 집계  
- `min_bucket`: 최소 버킷 집계  
- `sum_bucket`: 합계 버킷 집계  
- `stats_bucket`: 통계 버킷 집계  
- `extended_bucket`: 확장 통계 버킷 집계  
- `percentiles_bucket`: 백분위수 버킷 집계  
- `moving_bucket`: 이동 평균 집계  

### 부모집계

형제집계와 마찬가지로 생성된 버킷으로부터 다시 집계를 수행하는데  
별도로 집계결과가 json 객체로 출력되지 않고 **기존 버킷 집계결과에 같이 포함되어 출력된다**.  

아래는 `derivative` 속성을 통해 파생집계를 수행하며 바로 직전 버킷과의 값의 차이를 계산하여 출력한다.  

```json
GET /apache-web-log/_search?size=0 
{
  "aggs": {
    "histo": {
      "date_histogram": {
        "field": "timestamp",
        "interval": "day"
      },
      "aggs": {
        "bytes_sum": {
          "sum": { "field": "bytes" }
        },
        "sum_deriv": {
          "derivative": { "buckets_path": "bytes_sum" }
        }
      }
    }
  }
}

// result
"aggregations": {
  "histo": {
    "buckets": [
      {
        "key_as_string": "2015-05-17T00:00:00.000Z",
        "key": 1431820800000,
        "doc_count": 1632,
        "bytes_sum": { "value": 414259902 }
      },
      {
        "key_as_string": "2015-05-18T00:00:00.000Z",
        "key": 1431907200000,
        "doc_count": 2893,
        "bytes_sum": { "value": 788636158 },
        "sum_deriv": { "value": 374376256 }
      },
      {
        "key_as_string": "2015-05-19T00:00:00.000Z",
        "key": 1431993600000,
        "doc_count": 2896,
        "bytes_sum": { "value": 665827339 },
        "sum_deriv": { "value": -122808819 }
      },
      {
        "key_as_string": "2015-05-20T00:00:00.000Z",
        "key": 1432080000000,
        "doc_count": 2578,
        "bytes_sum": { "value": 878559106 },
        "sum_deriv": { "value": 212731767 }
      }
    ]
  }
}
```

`derivative` 특성상 직전 버킷 집계와 비교하기 때문에 0번째 `index` 에는 값이 없다.  

- `derivative`: 파생 집계  
- `cumulative_sum`: 누적 집계  
- `bucket_script`: 버킷 스크립트 집계  
- `bucket_selector`: 버킷 셀렉터 집계  
- `serial_diff`: 시계열 차분 집계  
