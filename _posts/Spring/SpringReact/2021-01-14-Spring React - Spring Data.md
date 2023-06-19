---
title:  "Spring React - Spring Data!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## classes: wide

categories:
  - spring-reative
---

## Spring Data with WebFlux

기존에 `WebMVC` 방식의 데이터베이스 접근시 `JDBC` 를 구현한 `드라이버 라이브러리` 를 사용해 접근해왔다.  

리액티브한 DB 통신도 HTTP 와 다르지 않다.  
이론적으로 DB 접근용 서비스를 생성하고 `WebClient` 를 사용해 DB 데이터를 가져온다면 비동기 DB 접근 라이브러리를 구현한 것 과 다름없다.  

다행이도 다양한 DB 벤더사에서 자바 비동기 DB 연결 라이브러리인 `리액티브 드라이버`를 제공함으로 단순히 라이브러리만 추가하면 **데이터베이스 레이어**에 대한 논 블록킹 엑세스를 할 수 있다.  

`spring-boot-starter-data-mongodb-reactive`  
`spring-boot-starter-data-cassandra-reactive`  
`spring-boot-starter-data-redis-reactive`  
`spring-boot-starter-data-r2dbc`  

스프링 데이터 팀에서 기존에 사용한던 `Repository` 패턴을 리액티브 방식에도 똑같이 사용할 수 있도록 추상화를 통해 구현해두었다.  

각 모듈들이 `ReactiveCurdRepository` 인터페이스를 사용해 `Reactor` 라이브러리와 통합되어 자연스럽게 리액티브하게 코드작성이 가능하다.  

### Spring Data MongoDB with Webflux

`NoSQL` 의 경우 각 벤더사에서 통합된 규약이 없다.  
각 벤더사에서 자기들만의 드라이버 라이브러리를 제공하고 스프링 데이터 팀은 스프링에서 해당 라이브러리들을 쉽게 사용할 수 있도록 각종 모듈을 개발하고 있다

`NoSQL DB` 는 최근에 만들어 져서 대부분 벤더사가 `리액티브 드라이버` 를 제공하고 있으며  
스프링 데이터 팀은 몽고DB 에서 제공하는 `리액티브 드라이버` 를 쉽고 편하게 사용할 수 있도록 `spring-boot-starter-data-mongodb-reactive` 모듈을 작성해두었다.  

해당 모듈을 사용하면 스프링 팀에서 만든 `Repository` 패턴을 사용해 메서드명 기반으로 쿼리문이 자동 생성/사용 할 수 있다.  

#### ReactiveMongoRepository

```java
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

// ReactiveCurdRepository 구현체
@Repository
public interface BookReactiveMongoRepository extends ReactiveMongoRepository<Book, ObjectId> {
    Mono<Book> findOneByTitle(Mono<String> title);

    Flux<Book> findManyByTitleRegex(String regexp);

    @Meta(maxScanDocuments = 3)
    Flux<Book> findByAuthorsOrderByPublishingYearDesc(Publisher<String> authors);

    @Query("{ 'authors.1': { $exists: true } }")
    Flux<Book> booksWithFewAuthors();

    Flux<Book> findByPublishingYearBetweenOrderByPublishingYear(
            Integer from,
            Integer to,
            Pageable pageable
    );
}
```

#### ReactiveMongoTemplate  

`ReactiveMongoRepository` 외에도 `ReactiveMongoTemplate` 를 사용해 쿼리 조작이 가능하다.  

```java
@Service
@RequiredArgsConstructor
public class RxMongoTemplateQueryService {
    private static final String BOOK_COLLECTION = "book";

    private final ReactiveMongoTemplate mongoTemplate; // ReactiveMongoTemplate implements ReactiveMongoOperations

    public Flux<Book> findBooksByTitle(String title) {
        Query query = Query.query(new Criteria("title")
            .regex(".*" + title + ".*"))
            .limit(100);
        return mongoTemplate.find(query, Book.class, BOOK_COLLECTION);
    }
}
```

#### MongoClient

몽고DB 에서 제공하는 리액티브 드라이버 구현체가 `com.mongodb.reactivestreams.client.MongoClient` 클래스이다.  

> <https://mongodb.github.io/mongo-java-driver-reactivestreams/>

`org.mongodb:mongodb-driver-reactivestreams` 모듈에서 제공하며 `spring-boot-starter-data-mongodb-reactive` 에서 내부적으로 사용한다.  

`MongoClient` 클래스를 사용해도 쿼리조작이 가능하다.  

```java
@Service
@RequiredArgsConstructor
public class RxMongoDriverQueryService {

    private final MongoClient mongoClient;
    
    public Flux<Book> findBooksByTitle(String title, boolean negate) {
        return Flux.defer(() -> {
            Bson query = Filters.regex("title", ".*" + title + ".*");
            if (negate) query = Filters.not(query);
            return mongoClient
                .getDatabase("test-db")
                .getCollection("book")
                .find(query);
        }).map(doc -> new Book(
            doc.getObjectId("id"), // Document
            doc.getString("title"),
            doc.getInteger("pubYear")));
    }
}
```

#### 트랜잭션(ReactiveMongoTemplate.inTransaction)

`MongoDB 4.0` 버전 이전까지 **하나의 문서에 대해서만 트랜잭션을 제공**하는 `Single-Document Transaction` 기능만 있었다.  

NoSQL 특성상 하나의 문서에 모든 정보를 사용하기에 `Single-Document Transaction` 으로도 충분해야 하지만 항상 예외가 있는법,  

결국 **여러 문서에 대한 트랜잭션을 제공**하는 `Multi-Document Transaction` 기능을 `MongoDB 4.0` 부터 지원한다.  

> WiredTiger 스토리지 엔진의 샤딩설정이 되어 있지 않고 복제설정일 경우에만 `Multi-Document Transaction` 을 지원한다.  
> 대용량 데이터를 처리할땐 샤딩이 꼭 필요해서 잘 사용하지 않는 기술이기도 하다.  

`ReactiveMongoTemplate` 의 `inTransaction` 메서드를 사용하면 된다.  

```java
private Mono<TxResult> doTransferMoney(String from, String to, Integer amount) {
    return mongoTemplate.inTransaction().execute(session -> session
        .findOne(queryForOwner(from), Wallet.class)
        .flatMap(fromWallet -> session
            .findOne(queryForOwner(to), Wallet.class)
            .flatMap(toWallet -> {
                if (fromWallet.hasEnoughFunds(amount)) {
                    fromWallet.withdraw(amount);
                    toWallet.deposit(amount);

                    return session.save(fromWallet)
                        .then(session.save(toWallet))
                        .then(ReactiveMongoContext.getSession())
                        // An example how to resolve the current session
                        .doOnNext(tx -> log.info("Current session: {}", tx))
                        .then(Mono.just(TxResult.SUCCESS));
                } else {
                    return Mono.just(TxResult.NOT_ENOUGH_FUNDS);
                }
            })))
        .onErrorResume(e -> Mono.error(new RuntimeException("Conflict")))
        .last();
}
```

### Spring Data R2DBC

> R2DBC: Reactive Relational Database Connectivity
> <https://r2dbc.io/>  
> <https://spring.io/projects/spring-data-r2dbc>  

아래와 같은 DBMS 에 대하여 r2dbc 라이브러리를 제공

```
H2 (io.r2dbc:r2dbc-h2)
MariaDB (org.mariadb:r2dbc-mariadb)
Microsoft SQL Server (io.r2dbc:r2dbc-mssql)
MySQL (dev.miku:r2dbc-mysql)
jasync-sql MySQL (com.github.jasync-sql:jasync-r2dbc-mysql)
Postgres (io.r2dbc:r2dbc-postgresql)
Oracle (com.oracle.database.r2dbc:oracle-r2dbc)
```

지금까지 `Spring Data JDBC` 혹은 `JPA` 를 사용해 생성된 `Hikari CP` 안의 연결객체가 `JDBC` 드라이버를 사용해 관계형 DB 를 사용해 왔다.  

```java
@Repository
public interface BookSpringDataJdbcRepository extends CrudRepository<Book, Integer> {
    
    @Query("SELECT * FROM book b WHERE b.title = :title")
    CompletableFuture<Book> findBookByTitleAsync(@Param("title") String title);

}
```

`JDBC, JPA` 등의 RDB 라이브러리 들은 모두 `동기/블럭킹` 방식으로 동작한다.  

다행이도 `Spring Data JDBC` 를 개발한 `스프링 데이터 Relational` 프로젝트 팀에서  
**리액티브에 적합**한 `자바 DB 드라이버`인 `리액티브 드라이버`를 개발중이다.  

이 `리액티브 드라이버` 를 사용한 프로젝트가 `R2DBC` 프로젝트이다.  

더이상 `JDBC` 를 사용하지 않고 리액티브 스택에 적합한 `리액티브 드라이버`를 사용해 DB 에 접근, 데이터를 조작한다.  

> 안타깝게도 `JPA` 는 기존 코드가 너무 복잡했는지 리액티브 지원을 하지 않을것으로 보인다.  

#### ReactiveCrudRepository

```java
@Repository
public interface MemberRepository extends ReactiveCrudRepository<Member, Long> {
    Mono<Member> findByName(String name);

    Mono<Member> findByUserName(String name);

    @Query("SELECT * FROM member WHERE name = :name AND user_name = :userName")
    Mono<Member> findByNameAndUserName(String name, String userName);
}
```

#### R2dbcEntityTemplate

```java
@Service
@RequiredArgsConstructor
public class MemberDynamicRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Flux<Member> findTest(String userName) {
        Query query = Query.query(where("user_name").like("%" + userName + "%"))
                .limit(10)
                .offset(0);
        return r2dbcEntityTemplate.select(Member.class)
                .matching(query)
                .all();
    }
}
```

### Spring Data Redis with Webflux

`spring-boot-starter-data-redis-reactive` 모듈을 사용, `ReactiveRedisTemplate` 클래스가 `Redis` 커넥션의 핵심클래스이다.  
다른 `Spring Data` 프로젝트와 달리 `Repository` 가 존재하지 않는다.  

일반적인 데이터 관리 외에도 구독/발행 구조의 메시지 기능도 지원한다.  

`spring-boot-starter-data-redis-reactive` 모듈은 내부적으로 `Lettuce` 라이브러리를 사용한다.

> <https://lettuce.io/>  
> `Lettuce` 라이브러리 내에서 `Reactor` 라이브러리를 사용한다.  
> 현재 `non blokcing` 을 지원하는건 `Lettuce` 가 유일하다.  

```java
public class Sample {
    private String name;
    private String description;
}


@Configuration
public class RedisConfig {
    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private Integer port;

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }
    
    @Bean
    public ReactiveRedisTemplate<String, Sample> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Sample> valueSerializer = new Jackson2JsonRedisSerializer<>(Sample.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Sample> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);

        RedisSerializationContext<String, Sample> context = builder.value(valueSerializer).build();

        return new ReactiveRedisTemplate(reactiveRedisConnectionFactory, context);
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class SampleService {
    private final ReactiveRedisTemplate<String, Sample> redisTemplate;

    public Mono<Boolean> put(String key, Sample sample) {
        return redisTemplate.opsForValue().set(key, sample);
    }

    public Mono<Sample> get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Flux<Sample> getAll(String keyPattern){
        return redisTemplate.keys(keyPattern)
                .flatMap(key-> redisTemplate.opsForValue().get(key));
    }

    public Mono<Boolean> delete(String key) {
        return redisTemplate.opsForValue().delete(key);
    }
}
```

<!-- 
## 스프링 세션 리액티브  

`spring-session-data-redis`  
`spring-boot-stater-webflux`  
`spring-boot-starter-data-redis-reactive`  

위 3개 의존성을 사용하면 `redis + session + webflux(reactive)` 형식으로 세션관리가 가능하다.  

`spring-session-data-redis` 에서 `Mono` 타입으로 세션에 접근할 수 있도록 `ReactiveSessionRepository` 를 제공한다.  

## 스프링 테스트 리액티브  

 -->
