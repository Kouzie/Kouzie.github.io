---
title:  "Spring Boot - Cache!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Spring Cache

`spring-boot-starter` 라이브러리에 기본적으로 사용할 수 있는 캐싱 설정을 제공한다.  
하지만 아래 설명할 `EhCache, Redis` 와 같은 추가 라이브러리를 사용하려면 `spring-boot-starter-cache` 를 추가해줘야 한다.  

```groovy
implementation 'org.springframework.boot:spring-boot-starter-cache'
```

`Spring Cache` 기본 설정에선 `@EnableCaching` 어노테이션을 사용하면 `CacheConfigurations.SimpleCacheConfiguration` 에서 `ConcurrentMapCacheManager` 을 Bean 으로 등록한다.  

> `ConcurrentMapCacheManager` 기본 구성을 사용할 것이라면 `@EnableCaching` 만 적용하면 된다.  

```java
// 2중 ConcurrentMap 형태
public class ConcurrentMapCacheManager implements CacheManager, BeanClassLoaderAware {
    // <cacheName, ConcurrentMapCache>
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);
    ...
}

public class ConcurrentMapCache extends AbstractValueAdaptingCache {
    private final String name; // cacheName
    // <key, data value>
    private final ConcurrentMap<Object, Object> store;
}
```

Map 을 늘리는것을 방지하려면 아래와 같이 Key 값을 지정하여 Bean 생성.  

```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean(name = "localCacheManager")
    public CacheManager localCacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager("customerCache");
        return cacheManager;
    }
}
```

캐시값 제어는 아래 어노테이션을 통해 수행할 수 있다.  

- `@Cacheable`  
- `@CacheEvict`  
- `@CachePut`  

`CacheManager` 구현체마다 세부 구현이 조금씩 다른데 `ConcurrentMapCacheManager` 기준으로 설명할 예정

### @Cacheable

캐시에 값이 있다면 바로 값을 조회해서 반환하고  
값이 없다면 메서드 내부코드를 실행후 캐시에 저장하고 반환한다.  

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    @Cacheable(cacheNames = "customerCache", key = "#input", cacheManager = "localCacheManager")
    public String getTest(String input) throws InterruptedException {
        Thread.sleep(5000);
        // input을 이용한 계산 또는 데이터 로딩 등의 작업 수행
        return "hello world";
    }

    @Cacheable(cacheNames = "customerCache")
    public List<Customer> findAll() throws InterruptedException {
        Thread.sleep(5000);
        List<Customer> result = new ArrayList<>();
        for (int i = 0; i < random.nextInt(10); i++) {
            result.add(CustomerGenerator.random());
        }
        return result;
    }

    @Cacheable(cacheNames = "customerCache")
    public List<Customer> findAll(List<String> ids) throws InterruptedException {
        Thread.sleep(5000);
        List<Customer> result = new ArrayList<>();
        for (String id : ids) {
            result.add(CustomerGenerator.random(id));
        }
        return result;
    }
}
```

`@Cacheable` 설정으로 아래 속성을 자주 사용한다.  

- **cacheNames(value)**: 필수값, 데이터가 저장되어 있는 공간을 찾아가기 위한 캐시 저장공간 네이밍값.  
- **key**: keyName, SpEL 로 지정  
- **cacheManager**: CacheManager 빈 이름  

`cacheNames → key` 형식의 level 형태의 키구조를 생성한다(2중 Map 구조).  
`cacheNames` 을 통해 데이터를 구조적으로 그룹화 하고 관리하는 것이 중요하다.  

`key` 속성을 별도로 지정하지 않았다면 `SimpleKey` 클래스를 사용해 메서드 파라미터를 기반으로 키가 구성된다.  
`key` 를 설정하면 아래와 같이 문자열로 키값을 지정할 수 있다.  

```java
@Cacheable(cacheNames = "userDetailCache", key = "'userId-' + #user.id", condition = "#user.role == 'ADMIN'", cacheManager = "myCacheManager")
public UserDetail getUserDetail(User user) {
}
```

만약 `cacheNames` 를 배열로 지정할 경우 두개의 `<String, Cache>` 가 저장된다.  
그리고 최초 검색되는 캐시값을 반환한다.  

```java
@Cacheable(cacheNames = {"userDetailCache", "test"}, key = "'userId-' + #user.id")
public UserDetail getUserDetail(User user) {
}
```

이외 기타 설정들

- **condition**: 요청 파라미터에 의한 캐시 저장 조건, SpEL 로 지정  
- **unless**: 반환값에 의한 캐시 저장 조건, SpEL 로 지정  
- **keyGenerator**: `KeyGenerator` 인터페이스 구현체, 커스텀하게 key 를 생성하고 싶다면 재정의해서 지정  
- **cacheResolver**: `CacheResolver` 인터페이스 구현체, 커스텀하게 value 를 생성하고 싶다면 재정의해서 지정  

`[condition, unless]` 속성은 아래와 같이 지정 가능  

```java
@Cacheable(value="posts", condition="#postId>10")
public Post findById(Integer postId) { ... }

@Cacheable(value="posts", condition="#title.length > 20")
public Post findByTitle(String title) { ... }

@Cacheable(value="titles", unless="#result.length() < 50")
public String getTitle(Post post) { ... }
```

### @CacheEvict

메서드가 호출완료 된 후 `@CacheEvict` 에 적용되는 캐시를 삭제한다.  

```java
//캐시 삭제
@CacheEvict(value = "customerCache")
public void refresh() {
    log.info("cache clear");
}

//캐시 삭제 - 키값 사용
@CacheEvict(value = "customerCache", key = "#id")
public void refresh(String id) {
    log.info("cache clear");
}
```

대부분 속성은 `@Cacheable` 과 동일하고 `beforeInvocation` 속성이 추가되었다.  

- **cacheNames**  
- **key**  
- **cacheManager**  
- **condition**  
- **keyGenerator**  
- **cacheResolver**  
- **beforeInvocation**  
  메서드가 호출되기 전에 제거가 발생해야 하는지 여부, `default false`  
  `true` 지정시 메서드 도중 예외가 발생한다 해도 이미 삭제가 되어있다.  

### @CachePut

`@CachePut` 은 캐시 업데이트를 위한 어노테이션으로  
메서드가 반드시 호출되고, 반환값을 캐시에 저장한다.  

```java
// 캐시 업데이트
@CachePut(value = "customerCache", key = "#id", cacheManager = "redisCacheManager")
public Customer update(String id) throws InterruptedException {
    Thread.sleep(5000);
    Customer customer = CustomerGenerator.random(id);
    return customer;
}
```

사용 속성은 아래와 같다.  

- **cacheNames**  
- **key**  
- **cacheManager**  
- **condition**  
- **unless**  
- **keyGenerator**  
- **cacheResolver**  

## EhCache CacheManager

> <http://ehcache.org/>

대부분의 경우 캐시를 다룰 때 `ConcurrentMapCacheManager` 를 사용하지 않음.  
다음과 같은 장점때문에 `EhCache` 를 로컬레벨의 캐시 라이브러리로 자주 사용한다.  

- 분리된 Tier(heap, off-heap, disk) 에 저장 가능
- TTL, TTI 기능 지원
- JMX 모니터링 지원
- LFU, LRU 등 캐시 Eviction Policy(제거 정책) 지원

> TTI(Time To Idle) 는 생존은 위한 이전 사용시간을 뜻함  

Spring Cache 와 EhCache 모두 JCache 구현체를 지원함으로  
EhCache 와 JCache 의 integration 방법을 사용해야한다.  

> <https://www.ehcache.org/documentation/3.10/107.html>

```groovy
implementation 'org.springframework.boot:spring-boot-starter-cache'
implementation 'org.ehcache:ehcache:3.10.8'
```

```java
@Primary
@Bean(name = "ehCacheManager")
public CacheManager cacheManager() {
    CachingProvider provider = Caching.getCachingProvider();
    EhcacheCachingProvider ehcacheProvider = (EhcacheCachingProvider) provider;
    DefaultConfiguration defaultConfiguration = new DefaultConfiguration(ehcacheProvider.getDefaultClassLoader(),
            new DefaultPersistenceConfiguration(new File("cache/directory")));
    javax.cache.CacheManager cacheManager = ehcacheProvider.getCacheManager(ehcacheProvider.getDefaultURI(), defaultConfiguration);

    CacheConfiguration configuration = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Object.class, // key type
            Object.class, // value type
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(1000, EntryUnit.ENTRIES)
                    .offheap(10, MemoryUnit.MB)
                    .disk(1, MemoryUnit.GB)
                    .build())
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60)))
            .withDefaultDiskStoreThreadPool()
            .build();
    cacheManager.createCache("customerCache",
            Eh107Configuration.fromEhcacheCacheConfiguration(configuration));
    return new JCacheCacheManager(cacheManager);
}
```

> `offheap` 은 메모리 공간이지만 JVM 에 의해 청소되지 않는 공간,  큰 캐시공간 생성 가능.

캐시에 저장될 엔트리는 아래 순서대로 저장된다.  

heap > off-heap > disk

PUT 요청 발생시 상위 티어에 저장공간이 부족할 경우 기본정책인 `LRU` 를 기반으로 사용하지 않은 데이터를 하위티어로 이동(Demotion)된다.  

GET 요청 발생시 하위티어에서 검색될 경우 상위티어로 이동(Promotion)된다.  

설정한 모든 티어에 저장공간이 부족할 경우 Eviction 이 발생하여 데이터는 제거된다.  

## Caffeine CacheManager

> <https://github.com/ben-manes/caffeine>

대부분 로컬캐시에는 대규모 데이터를 저장하기 보단 짧고 빠른 처리를 위해 구성하는것이 대부분.  

웹서비스에선 Caffeine 캐시를 많이 추천한다.  

Caffeine 은 `W-TinyLFU(Window TinyLFU)` 기반의 높은 캐시 히트율 알고리즘을 기반으로 개발되어있으며,  
on-heap 공간만을 지원한는 단순한 구조로 개발되어 오버헤드도 적다.  

```java
@Bean(name = "caffeineCacheManager")
public CacheManager caffeineCacheManager() {
    Caffeine<Object, Object> defaultConfig = Caffeine.newBuilder()
        .maximumSize(100) // 캐시 최대 항목 수
        .expireAfterWrite(10, TimeUnit.MINUTES) // 쓰기 후 10분 뒤 만료
        .recordStats(); // 캐시 통계 기록 (선택 사항)
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("customerCache");
    cacheManager.setCaffeine(defaultConfig);
    return cacheManager;
}
```

제한개수만 설정가능하고 용량은 설정 불가능하지만 오버헤드가 적어 효율적, 설정 설계 미스로 heap 사이즈 초과시 OOM 에러가 발생할 수 있다.  

## Redis CacheManager

`redis` 를 `CacheManager` 로 사용 가능.  
`[cacheNames, key]` 속성이 통합되어 문자열로 변경되어 key 값으로 사용한다.  

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

```java
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        connectionFactory.start();
        return connectionFactory;
    }

    @Bean(name = "redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json()))
                .entryTtl(Duration.ofMinutes(3L));
        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }
}
```

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    // key 이름: customerCache::SimpleKey []
    @Cacheable(cacheNames = "customerCache", cacheManager = "redisCacheManager")
    public List<Customer> findAll() throws InterruptedException {
        Thread.sleep(5000);
        List<Customer> result = new ArrayList<>();
        for (int i = 0; i < random.nextInt(10); i++) {
            result.add(CustomerGenerator.random());
        }
        return result;
    }

    // key 이름: customerCache::1,2,3,4,5
    @Cacheable(cacheNames = "customerCache", cacheManager = "redisCacheManager")
    public List<Customer> findAll(List<String> ids) throws InterruptedException {
        Thread.sleep(5000);
        List<Customer> result = new ArrayList<>();
        for (String id : ids) {
            result.add(CustomerGenerator.random(id));
        }
        return result;
    }
}
```

`{cacheNames}::{param eky}` 형태로 `key`,  
Java 클래스 표현식으로 출력된 문자열이 `value` 로 저장된 것을 확인할 수 있다.  

```sh
127.0.0.1:6379> keys *
# 1) "customerCache::SimpleKey []"
# 2) "customerCache::1,2,3,4,5"

127.0.0.1:6379> type "customerCache::SimpleKey []"
# string

127.0.0.1:6379> get "customerCache::SimpleKey []"
# "[\"java.util.ArrayList\",[{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"a0a96847-5042-499a-aabf-787d35242fa1\",\"nickName\":\"npssdlhfrm\",\"customerType\":\"BRONZE\",\"createTime\":[\"java.util.Date\",1686642104620]},{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"e9e102f0-9ab7-402c-9d7f-e9933137cd93\",\"nickName\":\"blcovuitlm\",\"customerType\":\"GOLD\",\"createTime\":[\"java.util.Date\",1686642104620]},{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"aa42d2e7-7628-452b-b1b4-7410d767446d\",\"nickName\":\"zpvctjvkoa\",\"customerType\":\"SILVER\",\"createTime\":[\"java.util.Date\",1686642104620]},{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"83d52f67-002b-43da-9cc2-abf7c9715405\",\"nickName\":\"lwffwuygud\",\"customerType\":\"GOLD\",\"createTime\":[\"java.util.Date\",1686642104620]},{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"160f50df-e2a1-455e-a1a7-291de969aada\",\"nickName\":\"mrtybmfxnc\",\"customerType\":\"DIAMOND\",\"createTime\":[\"java.util.Date\",1686642104620]},{\"@class\":\"com.example.redis.model.Customer\",\"id\":\"77c3d17a-c731-424c-b2c6-397d8b2ac0d3\",\"nickName\":\"dtpankciwp\",\"customerType\":\"SILVER\",\"createTime\":[\"java.util.Date\",1686642104620]}]]"
```

```java
@Cacheable(cacheNames = {"customerCache", "test"}, key = "'customer-' + #id", cacheManager = "redisCacheManager")
public Customer findById(String id) throws InterruptedException {
    Thread.sleep(5000);
    return CustomerGenerator.random(id);
}
```

```sh
127.0.0.1:6379> keys *
# 1) "test::customer-1"
# 2) "customerCache::customer-1"
```


## 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/cache-demo>
