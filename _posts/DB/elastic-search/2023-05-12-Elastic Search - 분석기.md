---
title:  "Elastic Search - 분석기!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 개요  

Lucene 은 내부적으로 다양한 분석기를 제공하는데, Elastic Search 는 Lucene이 제공하는 분석기를 그대로 활용한다.  

검색시 예기치 않은 결과가 나오는 경우 대부분 분석기를 제대로 이해하지 않고 구성했을 때이다.  

아무런 설정을 하지 않으면 `Standard Analyzer` 가 사용되며 아래와 같이 한글 타입은 단순 토큰화만되고 형태소 분석 등은 이루어지지 않는다.  

```json
POST _analyze 
{
    "analyzer" : "standard",
    "text" : "우리나라가 좋은나라, 대한민국 화이팅" 
}

// result
{
  "tokens": [
    {
      "token": "우리나라가",
      "start_offset": 0,
      "end_offset": 5,
      "type": "<HANGUL>",
      "position": 0
    },
    {
      "token": "좋은나라",
      "start_offset": 6,
      "end_offset": 10,
      "type": "<HANGUL>",
      "position": 1
    },
    {
      "token": "대한민국",
      "start_offset": 12,
      "end_offset": 16,
      "type": "<HANGUL>",
      "position": 2
    },
    {
      "token": "화이팅",
      "start_offset": 17,
      "end_offset": 20,
      "type": "<HANGUL>",
      "position": 3
    }
  ]
}
```

데이터를 토큰화하기 위해 여러 진행과정을 거치는데 대표적인 작업은 아래 3가지.  

1. **CHARACTER FILTER**  
   텍스트를 개별 토큰화하기 전의 전처리 과정, **문장을 특정한 규칙에 의해 수정한다**  
   문장을 분석하기 전에 입력 텍스트에 대해 특정한 단어를 변경하거나 HTML과 같은 태그를 제거하는 등, 사용자가 정의한 필터를 적용할 수 있다.  

2. **TOKENIZER FILTER**  
   수정한 문장을 **개별 토큰으로 분리한다**.  
   언어별로 전용 형태소 분석기의 `Tokenizer`를 사용한다.  

3. **TOKEN FILTER**  
   토큰화된 단어를 하나씩 필터링해서 **사용자가 원하는 토큰으로 변환한다**.  
   불용어를 제거, 동의어 사전의 단어 추가, 대문자를 소문자로 변환 등의 작업을 수행할 수 있다.  
   `Token Filter`는 여러 단계가 순차적으로 이뤄지며 순서에 따라 검색의 질이 달라질 수 있다.  

예시로 아래와 같은 문자열이 text 필드로 입력되면  
아래 테이블과 같이 색인이 이루어진다.  

```
1. elasticsearch is cool
2. <B>Elasticsearch<B> is great
```

토큰 | 문서번호 | 텀의 위치 (Position) | 텀의 빈도 (Term Frequency)
|---|---|---|---|
elasticsearch | 문서1,문서2 | 1,1 | 2
is | 문서1,문서2 | 2,2 | 2
cool | 문서1 | 3 | 1
great | 문서2 | 3 | 1

![es3](/assets/elastic-search/es3.png)  

문서가 저장되면 분석기는 색인처리를 진행한다.  
여기서 색인 과정은 아래 4가지 과정을 뜻한다.  

- 모든 문서가 가지는 단어의 고유 단어 목록  
- 해당 단어가 어떤 문서에 속해 있는지에 대한 정보  
- 전체 문서에 각 단어가 몇 개 들어있는지에 대한 정보  
- 하나의 문서에 단어가 몇 번씩 출현했는지에 대한 빈도  

인덱스 생성시 색인할 때 사용되는 `Index Analyzer` 와 검색할 때 사용되는 `Search Analyzer` 로 구분하여 지정할 수 있다.  

```json
PUT movie_analyzer
{
  "settings": {
    "index": {
      "number_of_shards": 5,
      "number_of_replicas": 1
    },
    "analysis": {
      "analyzer": {
        "movie_lower_test_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase"
          ]
        },
        "movie_stop_test_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "lowercase",
            "english_stop"
          ]
        }
      },
      "filter": {
        "english_stop": {
          "type": "stop",
          "stopwords": "_english_"
        }
      }
    }
  },
  "mappings": {
    "_doc": {
      "properties": {
        "title": {
          "type": "text",
          "analyzer": "movie_stop_test_analyzer", // 색인용
          "search_analyzer": "movie_lower_test_analyzer" // 검색용
        }
      }
    }
  }
}
```

`movie_stop_test_analyzer` 분석기의 경우 `english_stop` 필터를 사용하는데 불용어를 제거한다.  

만약 아래와 같이 토큰화를 진행할 경우 불용어를 제외한 4개의 단어가 토큰으로 저장된다.  

```json
PUT movie_analyzer/_doc/1
{
  "title": "Harry Potter and the Chamber of Secrets"
}
```

```
[harry], [potter], [chamber], [secrets]
```

검색의 경우 `movie_lower_test_analyzer` 를 사용하는데 아래와 같이 요청할 경우  
소문자로 토큰화 되어 요청하게될 것이다.  

```json
POST movie_analyzer/_search
{
  "query": {
    "query_string": {
      "query": "Chamber of Secrets"
    }
  }
}
```

```
[chamber], [of], [secrets]
```

기본적으로 토큰 데이터는 OR 조건으로 묶여있어 3개중 하나의 토큰만 찾을 수 있으면 검색이 되지만  
만약 `query` 를 AND 조건으로 진행할 경우 위 `of` 토큰이 껴있기 때문에 `Harry Potter and the Chamber of Secrets` 문서는 검색되지 않는다.

## analyzer(분석기)  

```json
{
    ...
    "analyzer": {
        "movie_lower_test_analyzer": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": [ "lowercase" ]
        },
        "movie_stop_test_analyzer": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": [ "lowercase", "english_stop" ]
        }
    }
}
```

`analyzer` 키워드로 시작하는 분석기 정의를 포면 `tokenizer`, `filter` 와 같은 필드를 가지고 있다.  
분석기는 앞으로 소개할 `[전처리필터, 토크나이저, 토큰필터]` 로 구성된다.  

엘라스틱서치에서는 Lucene에 존재하는 대부분의 분석기를 기본 분석기로 제공한다. 대표적인 분석기를 살펴보자

### standard

인덱스를 생성할 때 `analyzer` 를 정의하게 되어 있다.  
별다른 지정을 하지 않을경우 기본적으로 `standard` 분석기를 사용한다

`standard` 분석기는 아래 2가지 토큰필터를 내포하고 있다.  

- `standard` (토큰필터)  
- `lowercase` (토큰필터)  

```json
POST movie_analyzer/_analyze
{
  "analyzer": "standard",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[harry, potter, and, the, chamber, of, secrets]
```

### whitespace

별도의 토큰필터없이 공백기준으로만 띄우는 필터를 가지고 있는 분석기  

```json
POST movie_analyzer/_analyze
{
  "analyzer": "whitespace",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[Harry, Potter, and, the, Chamber, of, Secrets]
```

### keyword

전체 입력 문자열을 하나의 키워드처럼 처리한다. 토큰화 작업을 하지 않는다.

```json
POST movie_analyzer/_analyze
{
  "analyzer": "keyword",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[Harry Potter and the Chamber of Secrets]
```

## char_filter(전처리필터)  

분석기들은 문자열을 토큰으로 분리하기 전에 **전처리 필터**를 통해 데이터를 정제하는 과정을 거친다.  

토크나이저로 와 토큰 필터가 하는역할이 워낙 많기때문에 전처리필터는 활용도도 낮고 토큰화 작업중에서 비중이 떨어진다.  
그래서인지 엘라스틱서치에서 공식적으로 제공하는 전처리필터의 종류도 많지 않다.  

`char_filter` 키워드를 사용해 전처리 필터를 정의한다.  

### html_strip_char_filter

문장에서 HTML 을 제거하는 전처리 필터다.

```json
PUT movie_html_analyzer
{
  "settings": {
    "analysis": {
      "char_filter": {
        "html_strip_char_filter": {
          "type": "html_strip",
          "escaped_tags": [ "b" ]
        }
      },
      "analyzer": {
        "html_strip_analyzer": {
          "char_filter": [ "html_strip_char_filter" ],
          "tokenizer": "keyword"
        }
      }
    }
  }
}

POST movie_html_analyzer/_analyze
{
  "analyzer": "html_strip_analyzer",
  "text": "<span>Harry Potter</span> and the <b>Chamber</b> of Secrets"
}

// result
{
  "tokens": [
    {
      "token": "Harry Potter and the <b>Chamber</b> of Secrets",
      "start_offset": 6,
      "end_offset": 59,
      "type": "word",
      "position": 0
    }
  ]
}
```

`escaped_tags` 에 설정한 대로 `<b>` 태그 외의 모든 HTML태그가 제거되었다.  

## tokenizer(토크나이저)  

**분석기를 구성하는 가장 핵심 구성요소**로 어떠한 토크나이저를 사용하느냐에 따라 분석기의 전체적인 성격이 결정된다.  

`tokenizer` 키워드를 사용해 분석기에서 사용하는 토크나이저를 지정한다.  

### standard

엘라스틱서치에서 일반적으로 사용하는 토크나이저로서 **대부분의 기호를 만나면 토큰으로 나눈다**

```json
POST movie_analyzer/_analyze
{
  "tokenizer": "standard",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[Harry, Potter, and, the, Chamber, of, Secrets]
```

### whitespace

공백을 만나면 토큰으로 나눈다.  

```json
POST movie_analyzer/_analyze
{
  "tokenizer": "whitespace",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

### keyword

텍스트 전체를 하나의 토큰으로 생성.  

```json
POST movie_analyzer/_analyze
{
  "tokenizer": "keyword",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[Harry Potter and the Chamber of Secrets]
```

### ngram

n개 길이의 문자를 토큰으로 나눈다.  
자동완성 기능 구현에서 유용하게 사용가능하다.  

```json
PUT movie_ngram_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "ngram_analyzer": { 
          "tokenizer": "ngram_tokenizer"
        }
      },
      "tokenizer": {
        "ngram_tokenizer": {
          "type": "ngram",
          "min_gram": 3,
          "max_gram": 3,
          "token_chars": [ "letter" ]
        }
      }
    }
  }
}

POST movie_ngram_analyzer/_analyze
{
  "tokenizer": "ngram_tokenizer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[
  Har, arr, rry, 
  Pot, ott, tte, ter, 
  and, 
  the, 
  Cha, ham, amb, mbe, ber, 
  Sec, ecr, cre, ret, ets
]
```

문자열을 돌면서 위에 설정한대로 3개씩 끊어서 토큰에 저장한다.  

- `min_gram`: Ngram을 적용할 문자의 최소 길이를 나타낸다. 기본값은 1  
- `max_gram`: Ngram을 적용할 문자의 최대 길이를 나타낸다. 기본값은 2  
- `token_chars`:  토큰에 포함할 문자열을 지정한다.  

`token_chars` 에선 다음과 같은 옵션을 제공한다.  

- `letter`: `a`, `b`, `ï`, `京`  
- `digit`: `1`, `2`, `9`  
- `whitespace`: `' '`, `\n`  
- `punctuation`: `!`, `"`  
- `symbol`: `$`, `√`  
- `custom`: 사용자가 지정한 `custom_token_chars`

`token_chars` 로 `letter` 와 `digit` 정도가 포함될만하다

### edge_ngram

지정된 문자를 만날 때마다 시작 부분을 고정시켜 단어를 자르는 방식 토큰화한다.  
자동 완성을 구현할 때 유용하게 활용할 수 있다.  

```json
PUT movie_engram_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "edge_ngram_analyzer": {
          "tokenizer": "edge_ngram_tokenizer"
        }
      },
      "tokenizer": {
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 10,
          "token_chars": [ "letter" ]
        }
      }
    }
  }
}

POST movie_engram_analyzer/_analyze
{
  "tokenizer": "edge_ngram_tokenizer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[
    Ha, Har, Harr, Harry, 
    Po, Pot, Pott, Potte, Potter, 
    an, and, 
    th, the, 
    Ch, Cha, Cham, Chamb, Chambe, Chamber, 
    of, 
    Se, Sec, Secr, Secre, Secret, Secrets
]
```

## filter(토큰필터)  

`tokenizer` 에서 분리된 배열 형태의 토큰을 토큰필터에 전달하고 토큰필터는 **토큰들은 변형하거나 추가, 삭제** 한다.  

`filter` 키워드를 사용해 분석기에 지정한다.  

### lowercase, uppercase

토큰들을 소문자, 대문자로 변경하는 토큰필터

```json
PUT movie_lower_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "lowercase_analyzer": {
          "tokenizer": "standard",
          "filter": [ "lowercase" ]
        }
      }
    }
  }
}

POST movie_lower_analyzer/_analyze
{
  "analyzer": "lowercase_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[harry, potter, and, the, chamber, of, secrets]
```

```json
PUT movie_upper_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "uppercase_analyzer": {
          "tokenizer": "standard",
          "filter": [ "uppercase" ]
        }
      }
    }
  }
}

POST movie_upper_analyzer/_analyze
{
  "analyzer": "uppercase_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[HARRY, POTTER, AND, THE, CHAMBER, OF, SECRETS]
```

### asciifolding

아스키 코드에 해당하는 127 개의 알파벳, 숫자, 기호에 해당하지 않는 경우 문자를 아스키값으로 변경한다.  
수학기호를 몇개 껴 넣어서 토큰필터가 처리한 문자열을 알아보자.  

```json
PUT movie_af_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "asciifolding_analyzer": {
          "tokenizer": "standard",
          "filter": [
            "standard",
            "asciifolding"
          ]
        }
      }
    }
  }
}

POST movie_af_analyzer/_analyze
{
  "analyzer": "asciifolding_analyzer",
  "text": "hello java cåƒe"
}
```

```
[hello, java, cafe]
```

### stop_filter

불용어를 제거하는 필터, 직접 문자열 배열형시으로 불용어 목록을 지정하거나 불용어가 적혀있는 config 파일을 지정한다.  

```json
PUT movie_stop_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "stop_filter_analyzer": {
          "tokenizer": "standard",
          "filter": [
            "standard",
            "stop_filter"
          ]
        }
      },
      "filter": {
        "stop_filter": {
          "type": "stop",
          "stopwords": [ "and", "is", "the" ]
        }
      }
    }
  }
}

POST movie_stop_analyzer/_analyze
{
  "analyzer": "stop_filter_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[Harry, Potter, Chamber, of, Secrets]
```

### stemmer_eng_filter

`Stemming` 알고리즘을 사용해 토큰을 변형하는 필터, 한글은 지원하지 않는다.  

```json
PUT movie_stem_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "stemmer_eng_analyzer": {
          "tokenizer": "standard",
          "filter": [
            "standard",
            "lowercase",
            "stemmer_eng_filter"
          ]
        }
      },
      "filter": {
        "stemmer_eng_filter": {
          "type": "stemmer",
          "name": "english"
        }
      }
    }
  }
}

POST movie_stem_analyzer/_analyze
{
  "analyzer": "stemmer_eng_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

```
[harri, potter, and, the, chmber, of, secret]
```

### synonym_filter

동의어 처리를 위한 필터, 배열로 토큰에 대한 동의어를 지정하거나 동의어 사전을 적용할 수 있다.  

```json
PUT movie_syno_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "synonym_analyzer": {
          "tokenizer": "whitespace",
          "filter": [ "synonym_filter" ]
        }
      },
      "filter": {
        "synonym_filter": {
          "type": "synonym",
          "synonyms": [ "Harry => 해리", "Secrets, 비밀" ]
        }
      }
    }
  }
}

POST movie_syno_analyzer/_analyze
{
  "analyzer": "synonym_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

화살표는 치환되고 컴파는 추가된다.  

```
[해리, Potter, and, the Chamber, of, Secrets, 비밀]
```

현업에선 `synonyms_path` 설정을 통해 직접 동의어사전을 만들어 운영하는 편이다.  

동의어 사전을 수정하면 실시간으로 분석기에 적용될것이라 예상하지만 그렇지 않다.  

`synonym_filter` 를 사용하는 분석기는 색인시점과 검색시점에 사용될 수 있다.  
검색시점 분석기에는 실시간 적용이 되지만 색인시점 분석기에 적용하려면 reload 를 해야한다.  
그리고 이미 old 동의어 사전 색인된 문서들은 색인을 재생성하지 않는 이상 그대로 남아있게 된다.  

그래서 `synonym_filter` 를 사용한 분석기는 검색분석기로 많이 사용한다.  

### ngram, edge_ngram

`ngram`, `edge_ngram` 토크나이저도 있지만 토큰필터에서도 해당 역할을 수행할 수 있다.  
하지만 tokenizer 에는 없는기능이 하나 있는데 `side` 옵션이다.  

```json
PUT movie_ngram_analyzer
{
  "settings": {
    "analysis": {
      "analyzer": {
        "edge_ngram_analyzer": {
          "tokenizer": "standard",
          "filter": [ "edge_ngram_filter" ]
        }
      },
      "filter": {
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 10,
          "side": "back"
        }
      }
    }
  }
}


POST movie_ngram_analyzer/_analyze
{
  "analyzer": "edge_ngram_analyzer",
  "text": "Harry Potter and the Chamber of Secrets"
}
```

아래 결과처럼 `ngram` 을 단어의 첫글자가 아닌 마지막 글자로부터 구한다.  

```
[
    ry, rry, arry, Harry, 
    er, ter, tter, otter, Potter, 
    nd, and, 
    he, the, 
    er, ber, mber, amber, hamber, Chamber, 
    of, 
    ts, ets, rets, crets, ecrets, Secrets
]
```
