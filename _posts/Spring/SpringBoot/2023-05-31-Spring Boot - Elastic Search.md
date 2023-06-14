---
title:  "Spring Boot - Elastic Search!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Rest High Level Client  

`Elastic Search` 클라이언트를 사용해 아래 작업을 수행할 수 있다.  

- 매핑 API
- 문서 API  
- 검색, 집계 쿼리  

`Elastic Search` 가 버전 업그레이드 됨에 따라 2가지 방식을 제공한다.  

- `HTTP 클라이언트`: REST API 를 사용한 클라이언트  
- `Transport 클라이언트`: 네티 TCP모듈을 이용한 클라이언트  

`Transport 클라이언트` 가 네티 소켓을 이용해 직접 통신하기에 속도 측명에서 약간의 우위를 가지고 있지만  
`Elastic Search 7.0` 부턴 속도차이가 많이 줄어들었고 `HTTP 클라이언트` 사용을 권장하고 있다.  

> `Transport 클라이언트` 는 Deprecated 될 예정  

아래 의존성 주입을 통해 사용할 수 있다.  

```groovy
dependencies { 
    compile 'org.elasticsearch.client:elasticsearch-rest-high-level-client:6.4.3' 
}
```

```java
@Value("${elasticsearch.host}")
private String esHost;
@Value("${elasticsearch.port}")
private Integer esPort;

@Bean
public RestHighLevelClient restHighLevelClient() {
    return new RestHighLevelClient(
            RestClient.builder(new HttpHost(esHost, esPort, "http"))
    );
}
```

### XContentBuilder

`RestHighLevelClient` 은 내부적으로 `org.apache.http` 를 통해 `Elastic Search` 와 `REST API` 를 사용해 통신한다.  

`XContentBuilder` 는 이때 `json` 구조체를 만들 때 사용하는 클래스로 `RestHighLevelClient` 와 밀접하게 사용된다.  

아래와 같이 `XContentBuilder` 를 설정해서 `movie_rest` 인덱스에 문서저장 요청을 할 수 있다.  

```java
RestHighLevelClient client = ...;

XContentBuilder builder = XContentFactory.jsonBuilder()
    .startObject()
    .field("movieCd", requestDto.getMovieCd())
    .field("movieNm", requestDto.getMovieNm())
    .field("movieNmEn", requestDto.getMovieNmEn())
    .field("prdtYear", requestDto.getPrdtYear())
    .endObject();
/* 
{
    "movieCd": "20173733",
    "movieNm": "마지막 아이",
    "movieNmEn": "Last Child",
    "prdtYear": 20180511
}
*/
IndexRequest request = new IndexRequest("movie_rest", "_doc").source(builder);
IndexResponse response = client.index(request, RequestOptions.DEFAULT);
```

`RestHighLevelClient` 에서 제공하는 문서관련 함수는 아래와 같다.  

- `get`: 문서 조회  
- `index`: 문서 추가  
- `update`: 문서 수정, 문서 Upsert  
- `delete`: 문서 삭제  

### QueryBuilders

분석기를 통해 검색하는 경우 `QueryBuilders` 를 사용해 복잡한 쿼리를 DSL 형식으로 작성 가능하다.  

```java
// 검색 쿼리 설정
QueryBuilder query = QueryBuilders.matchQuery("movieNm", requestDto.getMovieNm());
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
        .query(query)
        .from(0)
        .size(5)
        .sort(new FieldSortBuilder("movieCd").order(SortOrder.DESC));
SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
searchRequest.types(TYPE_NAME);
searchRequest.source(searchSourceBuilder);

SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
return searchResponse;
```

`QueryBuilders` 에서 `matchQuery` 외에도 다양한 함수를 통해 검색할 수 있다.  

- `boolQuery`
- `matchQuery`
- `matchAllQuery`
- `termQuery`
- `nestedQuery`

### 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/es-demo/es-client>

## Spring Data Elastic Search

`Spring Data` 팀에서 `Elastic Search` 를 위한 라이브러리를 만들어 두었다.  
단순한 문서의 CRUD, 검색쿼리만 사용한다면 `Spring Data Elastic Search` 사용이 더 편하다.  

> <https://www.baeldung.com/spring-data-elasticsearch-tutorial>  
> <https://www.baeldung.com/spring-data-elasticsearch-queries>  
> <https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/>  

버전은 아래와 같은데 `Spring Data` 버전과 `Spring Boot` 버전의 의존성이 엮여있기 때문에  
`Elastic Search` 버전에 맞춰 `Spring Boot` 버전도 맞춰주어야 한다.  

| Spring Data Train | Spring Data Elasticsearch | Elasticsearch | Spring Boot |
| ----------------- | ------------------------- | ------------- | ----------- |
| 2023.0            | 5.1.x                     | 8.7.0         | 3.1.x       |
| 2022.0            | 5.0.x                     | 8.5.3         | 3.0.x       |
| 2021.2            | 4.4.x                     | 7.17.3        | 2.7.x       |
| 2021.1            | 4.3.x                     | 7.15.2        | 2.6.x       |
| 2021.0            | 4.2.x                     | 7.12.0        | 2.5.x       |
| 2020.0            | 4.1.x                     | 7.9.3         | 2.4.x       |
| Neumann           | 4.0.x                     | 7.6.2         | 2.3.x       |
| Moore             | 3.2.x                     | 6.8.12        | 2.2.x       |
| Lovelace          | 3.1.x                     | 6.2.2         | 2.1.x       |
| Kay               | 3.0.x                     | 5.5.0         | 2.0.x       |
| Ingalls           | 2.1.x                     | 2.4.0         | 1.5.x       |

`spring-data-elasticsearch` 또한 내부적으로 `elasticsearch-rest-high-level-client` 를 사용하기 때문에  
아래와 같이 `java config` 설정을 해주어야 한다.  

```java
@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig extends AbstractElasticsearchConfiguration {

    @Override
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo("192.168.10.234:9200")
            .build();
        return RestClients.create(clientConfiguration)
            .rest();
    }
}
```

### Mapping  

`@Document`, `@Field`, `@Id` 3개 어노테이션으로 간단하게 인덱스와 매핑할 수 있다.  

```java
@Getter
@Document(indexName = "member")
public class MemberDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    protected MemberDocument() {
    }

    public MemberDocument(AddMemberRequestDto requestDto) {
        this.name = requestDto.getName();
    }
}
```

복잡한 매핑 처리를 해야한다면 아래와 같이 `@Mapping` 어노테이션으로 처리하는 것을 권장한다.  

```java
@Getter
@Setter
@Document(indexName = "movie_search")
@Mapping(mappingPath = "movie_search_mapping.json")
public class MovieSearch {
    @Id
    String id;
    String movieCd; // keyword "20174647",
    String movieNm; // text "튼튼이의 모험"
    String movieNmEn; // text "Loser’s Adventure"
    String prdtYear; // integer "2017"
    String openDt; // integer "20180131"
    String typeNm; // keyword "장편"
    String prdtStatNm; // keyword "기타", "개봉예정"
    String nationAlt; // keyword "한국"
    List<String> genreAlt; // keyword, ["드라마"],
    String repNationNm; // keyword "한국"
    String repGenreNm; // keyword "드라마"
    List<Director> directors; //[{ "peopleNm":"고봉수" }],
    List<Company> companys;

    @Getter
    @Setter
    public static class Director {
        String peopleNm; // keyword
    }

    @Getter
    @Setter
    public static class Company {
        String companyCd;
        String companyNm;
    }
}
```

```json
// movie_search_mapping.json
{
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
```

### query-creation

`Spring Data` 프로젝트인만큼 `Repository` 구조를 사용한다.  

```java
public interface MovieSearchRepository extends 
    ElasticsearchRepository<MovieSearch, String> { ... }
```

간단한 쿼리의 경우 `query-creation` 문법을 사용해 쿼리작성하는 것을 추천한다.  

아래처럼 `movieNm` 필드를 기반으로 검색하는 쿼리를 `query-creation` 으로 생성  

```java
public interface MovieSearchRepository extends ElasticsearchRepository<MovieSearch, String> {
    List<MovieSearch> findByMovieNm(String movieNm, Pageable pageable);
}
```

아래와 같이 페이징 객체를 설정해서 호출할 경우 실제 `POST` 요청이 `Elastic Search` 로 전송된다.  

```java
String movieNm = "우리";
Pageable pageable = PageRequest.of(0, 10);
repository.findByMovieNm(movieNm, pageable);
```

```json
POST /movie_search/_search?typed_keys=true&max_concurrent_shard_requests=5&search_type=query_then_fetch&batched_reduce_size=512
{
  "from": 10,
  "size": 11,
  "query": {
    "bool": {
      "must": [
        {
          "query_string": {
            "query": "우리",
            "fields": [ "movieNm^1.0" ],
            "type": "best_fields",
            "default_operator": "and",
            "max_determinized_states": 10000,
            "enable_position_increments": true,
            "fuzziness": "AUTO",
            "fuzzy_prefix_length": 0,
            "fuzzy_max_expansions": 50,
            "phrase_slop": 0,
            "escape": false,
            "auto_generate_synonyms_phrase_query": true,
            "fuzzy_transpositions": true,
            "boost": 1
          }
        }
      ],
      "adjust_pure_negative": true,
      "boost": 1
    }
  },
  "version": true,
  "explain": false
}
```

### ElasticsearchOperations

`Srping Data ElasticSearch` 에서 복잡한 쿼리를 처리해야 할 경우 `NativeSearchQueryBuilder` 를 사용해 DSL 형태의 `Query` 클래스 작성이 가능하다.  
빈으로 등록된 `ElasticsearchOperations` 를 통해 호출한다.  

```java
private final ElasticsearchOperations operations;

...
Query searchQuery = new NativeSearchQueryBuilder()
    .withQuery(QueryBuilders.matchQuery("movieNm", "우리"))
    .build();
SearchHits<MovieSearch> searchHits = operations.search(query, MovieSearch.class);
List<MovieSearch> result = searchHits.stream()
    .map(SearchHit::getContent)
    .collect(Collectors.toList());
```

### 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/es-demo/es-spring>  
