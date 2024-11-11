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

## Spring Data redis

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
}
```

`application.properties` 에 아래와 같이 설정 한 후 Bean 객체 생성이 필요하다.  

```conf
redis.host=localhost
redis.port=6379
redis.timeout=0
```

```java
@Slf4j
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }
}
```

`redis` 서버와 연결을 위한 객체를 생성하는 `RedisConnectionFactory`,  

`Spring Data Redis` 프로젝트를 사용해서 `CrudRepository` 구현체를 통해  
`redis` 에 저장할 도메인 객체를 정의할 수 있다.  

아래와 같이 `@RedisHash` 어노테이션을 사용한다.  

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

```java
public interface PointRedisRepository extends CrudRepository<Point, String> {
}
```

```java
@Slf4j
@RestController
@RequestMapping("/repository")
@RequiredArgsConstructor
public class RedisRepositoryController {

    private final PointRedisRepository repository;

    @GetMapping("/{id}")
    public Point getPointById(@PathVariable String id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("invalid id:" + id));
    }

    @PostMapping
    public Point addPoint(@RequestBody Point point) {
        return repository.save(point);
    }

    @DeleteMapping("/{id}")
    public void removePoint(@PathVariable String id) {
        repository.deleteById(id);
    }

    @PatchMapping("/{id}")
    public Point updatePoint(@PathVariable String id, @RequestBody Point point) {
        Point entity = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("invalid id:" + id));
        entity.update(point);
        entity= repository.save(entity);
        return entity;
    }
}
```

`CrudRepository` 통해 객체타입의 데이터를 `redis` 에 저장하면 `hash`와 `set` 데이터구조로 관리한다.  

아래와 같이 curl 로 2개의 point 를 저장하고 `redis-cli` 로 내부값을 살펴보면  
2개의 hash 타입 데이터, 1개의 set 타입 데이터가 저장된 것을 확인할 수 있다.  

```
curl --location 'http://127.0.0.1:8080/repository' \
--header 'Content-Type: application/json' \
--data '{
    "id": "1",
    "amount": "1000",
    "refreshTime": "2023-03-22T11:11:11.000"
}'

curl --location 'http://127.0.0.1:8080/repository' \
--header 'Content-Type: application/json' \
--data '{
    "id": "2",
    "amount": "2000",
    "refreshTime": "2023-03-22T22:22:22.000"
}'
```

```sh
$ redis-cli

127.0.0.1:6379> keys *
# 1) "point:1"
# 2) "point"
# 3) "point:2"

127.0.0.1:6379> smembers point
# 1) "1"
# 2) "2"

127.0.0.1:6379> HGETALL point:1
# 1) "_class"
# 2) "com.example.redis.model.Point"
# 3) "amount"
# 4) "1000"
# 5) "id"
# 6) "1"
# 7) "refreshTime"
# 8) "2023-03-22T11:11:11"

127.0.0.1:6379> HGETALL point:2
# 1) "_class"
# 2) "com.example.redis.model.Point"
# 3) "amount"
# 4) "2000"
# 5) "id"
# 6) "2"
# 7) "refreshTime"
# 8) "2023-03-22T22:22:22"
```

## RedisTemplate

위에서 생성했던 `RedisConnectionFactory` 빈 객체를 주입해서 `RedisTemplate` 구성할 수 있다.  

```java
@Bean
public RedisTemplate<?, ?> redisTemplate(@Autowired RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    return redisTemplate;
}


@Bean
public StringRedisTemplate stringRedisTemplate(@Autowired RedisConnectionFactory redisConnectionFactory) {
    StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
    stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
    return stringRedisTemplate;
}
```

범용적으로 사용하기 위해 객체를 `json` 으로 직렬화 저장하는 경우가 많은데, 이 때 `StringRedisTemplate` 을 사용하면 좋다.  

테스트를 위한 컨트롤러를 생성

```java
@Slf4j
@Controller
@RequestMapping("/template")
public class RedisTemplateController {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/sample")
    public void sample(@RequestParam(required = false) String key, Model model) {
        log.info("sample invoked");
        Set<String> keys;
        if (StringUtils.hasText(key)){
            keys = stringRedisTemplate.keys(key);
        }
        else {
            keys= stringRedisTemplate.keys("*");
        }
        model.addAttribute("keys", new ArrayList<>(keys));
    }

    @GetMapping("/insert")
    public String insert(@RequestParam String key, @RequestParam String value) {
        log.info("insert invoked, {}:{}", key, value);
        stringRedisTemplate.opsForValue().set(key, value);
        stringRedisTemplate.opsForValue().get(key);
        return "redirect:/template/sample";
    }


    @GetMapping("/deleteAll")
    public String deleteAll(Model model) {
        log.info("deleteAll called()....");
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"));
        return "redirect:/template/sample";
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

## Redisson 분산락  

> <https://github.com/redisson/redisson>

`Redis` 기반 분산락 기능을 지원하는 라이브러리.  

`Pub/Sub` 기반의 락을 사용하기에, 계속해서 CPU 자원을 사용하는 스핀락보다 효율적이다.  

```groovy
// 내부적으로 spring data redis 사용
implementation 'org.redisson:redisson-spring-boot-starter:3.30.0'
```

```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://localhost:6379");
    return Redisson.create(config);
}
```

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;
    private Integer count = 0;

    public void executeWithLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            // 락을 획득하기 위해 100초 동안 시도하고, 10초 동안 락을 유지.
            isLocked = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if (isLocked) {
                // 락을 획득한 상태에서 실행할 작업
                Thread.sleep(100);
                count += 1;
                log.info("Lock acquired. Executing protected code. counter:{}", count);
            } else {
                log.info("Could not acquire lock.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (isLocked) {
                lock.unlock(); // TTL 을 넘길경우 unlock 호출시 IllegalMonitorStateException 발생
                log.info("Lock released.");
            }
        }
    }

    public void executeWithoutLock() {
        try {
            Thread.sleep(100);
            count += 1;
            log.info("Lock acquired. Executing protected code. counter:{}", count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

해당 라이브러리의 주의해야할 점은 `LOCK TTL` 동안 임계코드 실행이 완료되지 않는다면 중복실행될 수 있다.  
TTL 로 인해 Lock 이 해제되어도 어플리케이션 코드는 지속 실행되다 `lock.unlock()` 시점에야 TTL 을 넘겼음을 알 수 있다.  

데드락 상황을 감수하고 TTL 을 무제한으로 설정한다면 확실하게 임계영역을 지킬 수 있다.  

### AOP 분산락  

`try, catch, finally` 형태의 분산락 구조를 AOP 형태로 구성하는것이 대부분.  

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String value();
}

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private final RedissonClient redissonClient;

    @Around("@annotation(DistributedLock)")
    public Object executeWithLock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);
        String lockKey = distributedLock.value();  // 어노테이션에서 락 키 가져오기
        RLock lock = redissonClient.getLock(lockKey);
        log.info("Lock acquired. Executing protected code.");
        try {
            lock.lock();
            Object result = joinPoint.proceed();
            return result;
        } finally {
            lock.unlock();
            log.info("Lock released.");
        }
    }
}

@DistributedLock("executeWithAopLock")
public void executeWithAopLock() {
    // 비즈니스 로직 실행
    log.info("business code invoked");
    for (int i = 0; i < 10; i++) {
        count += 1;
    }
    log.info("business end");
}
```


## 데모코드

> Redis demo
> <https://github.com/Kouzie/spring-boot-demo/tree/main/redis-demo>

<!-- 

### RedLock

`Redisson` 은 `RedLock` 이라는 분산환경에서 사용가능한 기법을 사용한다.  

아래와 같이 `Multi Instance` 로 구성된 `Redis Cluster` 에서도 분산락을 흭득할 수 있다.  

```java
@Bean
public RedissonClient redissonClient() {
    Config config = new Config();
    config.useReplicatedServers()
            .addNodeAddress(
                "redis://127.0.0.1:6379", 
                "redis://127.0.0.1:6380", 
                "redis://127.0.0.1:6381"
            );
    return Redisson.create(config);
}
```

RedLock 동작과정은 아래와 같다.  

- 시작시간 구하기
- 과반수 이상의 인스턴스에서 락을 획득
- 락 흭득시간 계산
  락 흭득시간이 TTL 을 넘었을 경우 실패처리
  락 흭득시간을 제하고 TTL 만큼 임계코드 실행
- 획득한 모든 락을 해제

## redis 이중화  

본격적으로 `redis`설정파일인 `redis.conf`를 수정해야 하는데 <http://redisgate.kr/redis/configuration/redis_conf_list.php> 사이트를 참고바람.  

### redis replication

기본 `redis.conf` (ver 5.0)

> <http://download.redis.io/redis-stable/redis.conf>  

위 사이트에서 기본 설정파일 `redis.conf`를 다운받고 `master`, `slave` 가 사용할 각 설정파일을 생성  

아래 설정만 일부 변경해 사용하자.  

```conf
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

`sentinel` 의 사전적 의미는 **감시병**.  

`master`가 어떤 이유로 종료되면  
복제하던 `slave`가 자동으로 `master`로 설정되도록 구성.  

> <http://download.redis.io/redis-stable/sentinel.conf>

위 사이트에서 `sentinel` 을 위한 기본 설정파일을 다운(`redis ver 5.0`기준)  
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
 -->