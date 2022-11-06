---
title:  "java with redis!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## redis

> Redis is an open source (BSD licensed), in-memory data structure store, used as a database, cache and message broker.

메모리 공간에 저장되는 휘발성 데이터베이스, Persistence 속성을 통해 메모리의 내용을 파일로 덤프할 수도 있다.  

key, value 기반의 데이터베이스임으로 mongoDB와 같은 NoSQL처럼 사용할 수 있다.

redis 설치는 brew 등의 명령으로 간단히 설치 가능하고 docker를 사용해도 된다.  

서버 시작/종료 명령어 (mac)  
> redis-server  
> redis-cli shutdown  

### 스프링에서 redis사용하기 

`application.properties` 에 아래와 같이 설정 

```conf
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=0
```

설정한 데이터대로 스프링 빈 객체 생성이 필요하다.  

```java
@Configuration
@Log
public class RedisRepoConfig extends CachingConfigurerSupport {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.timeout}")
    private int timeout;

    private RedisServer redisServer;
    private static final Logger logger = LoggerFactory.getLogger(RedisRepoConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("RedisConnectionFactory create");
        return new LettuceConnectionFactory(host, port);
    }
    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
```

`redis`서버와 연결을 위한 객체를 생성하는 `...Factory`, 연결된 객체를 통해 `redis`에 명령을 전달하는 `RedisTemplate`  

`redis`와 연결을 휘한 객체로 `Jedis`, `Lettuce` 2가지가 있다.  

`Lettuce`를 사용하는게 성능상 좋다고 한다.  
> https://jojoldu.tistory.com/418  

`redis`를 위한 스프링 설정이 끝났고 어떤 데이터를 어떤 테이블에 저장할지 domain객체를 생성


```java
@AllArgsConstructor
@ToString
@RedisHash("point")
public class Point {
    @Id
    private String id;
    private Long amount;
    private LocalDateTime refreshTime;

    public void refresh(Long amount, LocalDateTime refreshTime) {
        if (refreshTime.isAfter(this.refreshTime)) {
            this.amount = amount;
            this.refreshTime = refreshTime;
        }
    }
}
```

`redis`의 좋은점은 정말 DB처럼 사용할 수 있기때문에 `CrudRepository` 구현체도 사용 가능하다.  
> `@RedisHash`가 JPA `@Entity`에 해당하는 어노테이션이다.  

```java
public interface PointRedisRepository extends CrudRepository<Point, String> {
}

```

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Log
public class RedisTest1 {
    @Autowired
    private PointRedisRepository repo;

    @After
    public void tearDown() throws Exception {
        repo.deleteAll();
    }

    @Test
    public void defaultInput() {
        String id = "kouzie";
        LocalDateTime refreshTime = LocalDateTime.now();
        Point point = new Point(id, 1000L, refreshTime);
        log.info("before Point : " + point);
        repo.save(point);

        Point savedPoint = repo.findById(id).get();
        log.info("after Point : " + savedPoint);
    }
}
```

## StringRedisTemplate

```java
@Configuration
@Log
public class RedisRepoConfig extends CachingConfigurerSupport {
    ...
    ...
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory());
        return stringRedisTemplate;
    }
}
```
키와함께 value값으로 `json`과 같은 긴 문자열 데이터를 같이 저장하는 경우가 많은데  
이 때 `StringRedisTemplate` 을 사용하면 좋다.  

먼저 컨트롤러 하나 생성,

```java
@Controller
@Log
@RequestMapping("/redis")
public class RedisController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/sample")
    public void sample(Model model) {
        log.info("sample called()....");
        Set<String> keys = stringRedisTemplate.keys("*");

        model.addAttribute("keys", new ArrayList<>(keys));
    }

    @GetMapping("/insert")
    public String insert(
            @Param("key") String key,
            @Param("value") String value
    ) {
        log.info("insert called()....");
        log.info(key + ":" + value);
        stringRedisTemplate.opsForValue().set(key, value);
        return "redirect:/redis/sample";
    }


    @GetMapping("/deleteAll")
    public String deleteAll(Model model) {
        log.info("deleteAll called()....");
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"));
        return "redirect:/redis/sample";
    }
}
```

`CrudRepository`를 사용해서 `redis`에 데이터를 삽입해도 되지만 `StringRedisTemplate` 혹은 그냥 `RedisTemplate`의 메서드로도 충분이 삽입 가능하다.   

```html
<!DOCTYPE html>
<html class="no-js" lang="en"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
    sample_radis

    <form action="/redis/insert">
        key: <input type="text" name="key" >
        value: <input type="text" name="value">
        <button type="submit">submit</button>
    </form>
    <button onclick="location.href='/redis/deleteAll'">delete all</button>

    <p th:each="p:${keys}">[[${p}]]</p>
</body>
</html>
```

지금까지 저장된 키 목록이 출력된다.  

## redis data type

redis에는 여러타입의 데이터를 보관할 수 있다.  

`string, hash, lists, sets, hashes, sorted sets`  총 6종류의 데이터 타입이 존재.  

> https://kimpaper.github.io/2016/07/27/redis-datatype/   

위에서 `repo.save` 를 통해 객체를 redis에 저장했는데 어떤식으로 저장되는지 살펴보자.  

```java
@Data
@RedisHash("mid")
public class Member {
    @Id
    private String mid;
    private String mpw;
    private String mname;
}


@PostMapping("/join")
public String joinPOST(Member member, RedirectAttributes rttr) {
    log.info("joinPOST() called...");
    memberRepo.save(member);
    return "redirect:/jwt/main";
}
```

위와 똑같이 클라이언트로부터 `member`의 데이터를 입력받아 `redis`에 `save`한다.  

![redis1](/assets/2019/redis1.png)

redis-cli를 통해 안의 데이터를 출력해보면 사진과 같이 출력된다.  

한번더 다른 데이터로 `member`를 `save`하면 아래 사진형식으로 저장된다.  

![redis2](/assets/2019/redis2.png)

`CrudRepository` 통해 객체를 저장하면 `hash`와 `set`데이터구조로 유지하는 것을 알아두자.  

데이터 타입에 따라 redis-cli로 데이터를 조회하는 명령어가 달라진다.  

* if value is of type `string` -> `GET <key>`  
* if value is of type `hash` -> `HGETALL <key>`  
* if value is of type `lists` -> `lrange <key> <start> <end>`  
* if value is of type `sets` -> `smembers <key>`  
* if value is of type `sorted sets` -> `ZRANGEBYSCORE <key> <min> <max>`  

## redis 이중화  

본격적으로 `redis`설정파일인 `redis.conf`를 수정해야 하는데 http://redisgate.kr/redis/configuration/redis_conf_list.php 사이트를 참고바람.  

### redis replication

기본 `redis.conf` (ver 5.0)

http://download.redis.io/redis-stable/redis.conf  
위 사이트에서 기본 설정파일 `redis.conf`를 다운받고 `master`, `slave` 가 사용할 각 설정파일을 생성  

아래 설정만 일부 변경해 사용하자.  

```
# redis-master.conf
bind 0.0.0.0

# bind가 null 혹은 0.0.0.0이기에 모든 ip에서 접근 가능, yes일 경우 localhost에서만 접근 가능 
protected-mode no

# 로그파일 저장 
logfile "redis-master.log"

# 주석해제
repl-ping-replica-period 10
repl-timeout 60
```


```conf
# redis-slave.conf
bind 0.0.0.0
#protected-mode yes 
#비밀번호 없이 접근
protected-mode no

# 로그파일 저장 
logfile "redis-slave.log"

# 마스터 노드 설정
replicaof 172.17.0.2 6379

# 주석해제
repl-ping-replica-period 10
repl-timeout 60
```

설정을 변경했다면 해당 설정파일과 출력될 로그파일의 볼륨설정 후 `docker`실행, 
```
docker run -v /Users/gojiyong/myredis/conf:/usr/local/etc/redis -v /Users/gojiyong/myredis/data:/data --name redis-master -p 6379:6379 redis:5.0.7 redis-server /usr/local/etc/redis/redis-master.conf
docker run -v /Users/gojiyong/myredis/conf:/usr/local/etc/redis -v /Users/gojiyong/myredis/data:/data --name redis-slave1 -p 6378:6379 redis:5.0.7 redis-server /usr/local/etc/redis/redis-slave.conf
docker run -v /Users/gojiyong/myredis/conf:/usr/local/etc/redis -v /Users/gojiyong/myredis/data:/data --name redis-slave2 -p 6377:6379 redis:5.0.7 redis-server /usr/local/etc/redis/redis-slave.conf
docker run -v /Users/gojiyong/myredis/conf:/usr/local/etc/redis -v /Users/gojiyong/myredis/data:/data --name redis-slave3 -p 6376:6379 redis:5.0.7 redis-server /usr/local/etc/redis/redis-slave.conf
```
docker run -v /Users/gojiyong/myredis/conf:/usr/local/etc/redis -v /Users/gojiyong/myredis/data:/data --name redis-test -P -p 6379 redis:5.0.7 

> 참고: `slave`의 개수는 홀수가 좋다고 한다.  

`master -> slave` 순으로 실행한다.  

실행 후 각 컨테이너에 접속해 `master`에서 `set`한 데이터가 `slave`에도 복제되어있는지 확인.  

### redis sentinel

**sentinel**의 사전적 의미는 **감시병**이다.  

`master`가 어떤 이유로 종료되면 복제하던 `slave`가 자동으로 `master`로 설정되도록 구성.  

http://download.redis.io/redis-stable/sentinel.conf  
위 사이트에서 sentinel을 위한 기본 설정파일을 다운(redis ver 5.0기준)  

그리고 아래와 같이 일부 수정한다.  

```conf
bind 0.0.0.0
protected-mode no

# 센티널 서버 2대가 master redis가 종료되었다 판단한 순간 failover 작동
sentinel monitor mymaster 172.17.0.2 6379 2
# 5초간 위에서 등록한 mymaster와 연결 안될 시 failover 동작
sentinel down-after-milliseconds mymaster 5000
# 새로운 마스터 서버에 slave서버 하나씩 동기화 처리 
sentinel parallel-syncs mymaster 1
# failover 제한시간, 해당시간안에 교체 미 완료시 다시 처음부터
sentinel failover-timeout mymaster 180000
# failover후 기존 redis 를 사용중일 client에게 새로운 접속 정보를 알리기 위한 쉘 스크립트 실행 
# sentinel client-reconfig-script mymaster /usr/local/etc/redis/reconfig.sh
```

```docker
FROM redis:5.0.7
MAINTAINER "kouzie"
COPY redis-sentinel.conf /usr/local/etc/redis/redis-sentinel.conf
CMD ["redis-sentinel", "/usr/local/etc/redis/redis-sentinel.conf"]
```

`docker build -t redis-sentinel:5.0.7 ./`

```
docker run --name sentinel1 -p 26379:26379 -d redis-sentinel:5.0.7
docker run --name sentinel2 -p 26378:26379 -d redis-sentinel:5.0.7
docker run --name sentinel3 -p 26377:26379 -d redis-sentinel:5.0.7
```

`sentinel.conf` 파일 아래처럼 설정  
```conf
sentinel leader-epoch mymaster 0
sentinel known-replica mymaster 172.17.0.4 6379
sentinel known-replica mymaster 172.17.0.3 6379
sentinel myid 2a8e7dda23abc19e8b980a37c19bb2f740c7c8d2
sentinel known-sentinel mymaster 172.17.0.6 26379 cae7400b5743fb6a86617b1b2dec7f397d7af38d
sentinel known-sentinel mymaster 172.17.0.7 26379 b088269700863c4293f8d31c85103ff33190154a
```