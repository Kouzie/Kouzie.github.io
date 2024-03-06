---
title:  "Spring Cloud - Resilience4J!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## Resilience4J

> <https://github.com/resilience4j/resilience4j>  
> <https://resilience4j.readme.io/docs>  

`Resilience4J` 는 서비스의 가용성을 위해 아래 MSA 가용성 향상을 위한 기술을 지원하는 라이브러리이다.  

회로차단기 기능부터 회복, 재시도 와 같은 기능을 제공한다.  

- `resilience4j-circuitbreaker`: Circuit breaking  
- `resilience4j-bulkhead`: Bulkheading  
- `resilience4j-ratelimiter`: Rate limiting  
- `resilience4j-retry`: Automatic retrying (sync, async)  
- `resilience4j-timelimiter`: Timeout handling  

`Resilience4J` 라이브러리도 결국은 메서드의 겉을 `try..catch` 문으로 감싸고 개발자가 정의한 로직대로 움직이도록 지원하는 정교한 라이브러리일 뿐이며  
각종 람다식과 데코레이터 패턴을 구성하여 좀더 유지보수하기 쉽고 간결하게 구현하였을 뿐이다.  

위의 `Resilience4J` 의 코어 라이브러리 별 역할을 알아보고 어떻게 우리가 정의한 메서드에 적용시킬 수 있는지 알아본다.  

### CircuitBreaker

> Circuit breaker(회로 차단기)
> 위키: 전기 회로에서 과부하가 걸리거나 단락으로 인한 피해를 막기 위해 자동으로 회로를 정지시키는 장치

MSA 환경에선 하나의 서비스 장애가 다른 서비스로의 장애 전파로 이뤄질 수 있기 때문에  
서비스의 장애가 발견되면 개발자의 장애 대응 로직으로 전환될수 있도록 해야한다.  

이때 장애를 발견하고 대응 로직으로 이동시키는 것을 `Circuit breaker` 라 한다.  

![r4j1](/assets/springboot/spring-cloud/springcloud_r4j1.png)  

그림과 같이 `connection problem` 이 발생해 2번 이상 `time out` 에러가 발생하면 `circuit breaker` 을 동작시킨다.  

`Resilience4J` 에서 `Circuit breaker` 는 3가지 상태를 할당받는 회로차단기이자 `state machine` 이다.  

- `closed`  
- `open`  
- `half-open`  

함수 호출의 실패율이 임계값을 넘으면 `closed` 에서 `open` 으로 변경된다.  
`open` 상태일 때 메서드 호출을 거부하고 설정한 대기시간이 지나면 `half-open` 한다.  
`half-open` 상태에서 메서드 호출을 허용하고 설정한 실패 임계치에 따라 다시 `open` 할지 `closed` 할지 결정한다.  

임계치를 통한 상태의 결정은 `sliding window` 를 통해 이루어지는데  
설정한 `sliding window` 개수 안에 임계치가 넘는 장애가 발생하면 `closed` 에서 `open` 으로 상태가 변경된다.  

`sliding window` 는 `time based`, `count based` 가 있으며 기본값은 `COUNT_BASED` 이다.  

실패 비율 및 각종 설정을 할 수 있다.  

```java
// Create a custom configuration for a CircuitBreaker
CircuitBreakerConfig cbc = CircuitBreakerConfig.custom()
  .failureRateThreshold(50) // 실패 비율 임계치 백분율, 상태의 전환점, default 50
  .slowCallRateThreshold(50) // 느린 호출 임계치 백분율, 상태의 전환점, default 100
  .slowCallDurationThreshold(Duration.ofSeconds(2)) // 느린호출 판단 임계치
  .waitDurationInOpenState(Duration.ofMillis(1000)) // open에서 half-open으로 전환하기 전 기다리는 시간
  .permittedNumberOfCallsInHalfOpenState(3) // half-open 시 허용 호출 수, default 10
  .minimumNumberOfCalls(10) // slide window 를 위한 최소 호출 수, default 100
  .slidingWindowSize(5) // close 상태에서 호출 결과를 기록할 때 쓸 window 크기, defualt 100
  .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED) // default COUNT_BASED
  .recordExceptions(IOException.class, TimeoutException.class) // 실패로 기록할 예외
  .recordException(e -> true) // 모든 예외를 실패로 기록, 커스텀하여 false 반환하면 기록하지 않음
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class) // 실패, 성공으로 기록하지 않음
  .build();
```

그외의 설정과 설명은 아래 url 참고

> <https://resilience4j.readme.io/docs/circuitbreaker>

정의한 설정대로 `CircuitBreakerRegistry` 를 생성하고  
`CircuitBreakerRegistry` 를 사용해 `CricuitBreaker` 인스턴스를 생성한다.  

```java
// config
CircuitBreakerConfig cbc1 = CircuitBreakerConfig.ofDefaults();
CircuitBreakerConfig cbc2 = CircuitBreakerConfig.ofDefaults();
// registry
CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(cbc1); // "default" key 로 들어감
registry.addConfiguration("demo-cbc", cbc2);
// instance
CircuitBreaker cb1 = registry.circuitBreaker("default-cb");
CircuitBreaker cb2 = registry.circuitBreaker("demo-cb", "demo-cbc");
CircuitBreaker cb_default = CircuitBreaker.of("my-db", cbc1); // registry 안통하고 바로 생성 가능
```

`CircuitBreakerRegistry` 에서 `CircuitBreakerConfig` 를 `HashMap` 으로 관리하며  
여러개 저장해두 었다 적재적소에 꺼내어 `CircuitBreaker` 인스턴스 생성이 가능하다.  

앞으로 나올 `Resilience4J` 의 다른 core 라이브러리도  
동일한 `registry-config-instance` 생성 구조를 가졌으니 참고  

`CircuitBreaker` 의 유일한 구현체인 `CircuitBreakerStateMachine` 가 회로차단기로써 각종 이벤트들을 처리한다.  

- `onSuccess`: 메서드 정상완료시 발생  
- `onError`: 메서드 실패시 발생  
- `onIgnoredError`: 무시되는 예외일경우 발생  
- `onReset`: 초기화시 발생  
- `onStateTransition`: `state` 변경시 발생  

`onSuccess`, `onError`, `onIgnoredError` 이벤트의 경우 메서드 호출시 발생하며 `onReset` 의 경우 의도적으로 `reset` 하지않는이상 발생할 일이 없다.  

`state` 변경은 `CircuitBreaker` 의 핵심으로 `config` 에 설정한 임계값에 도달할 경우 `state` 가 변경되면서 `onStateTransition` 가 호출된다.  
임계치 감시는 `CircuitBreakerMetrics` 클래스를 통해 이루어지며 처음 `closed` 상태에서 생성되었다가 `CircuitBreaker` 상태가 변경될 때 마다 새로 생성된다.  
`onSuccess`, `onError` 가 발생할 때 마다 이벤트가 호출되면서 `CircuitBreakerMetrics` 를 업데이트하는 구조이다.  

### Bulkhead

> Bulkhead: 격벽  
> 선체 내부공간이 침몰해도 격벽으로 인해 다른 선체공간에는 물이차지 않는다는 개념  
> <https://learn.microsoft.com/ko-kr/azure/architecture/patterns/bulkhead>

![r4j2](/assets/springboot/spring-cloud/springcloud_r4j2.png)  

서비스의 과도한 요청은 해당 서비스를 소비하는 다른 서비스의 장애를 야기함으로  
MSA 환경에서 사용되는 패턴으로 타 서비스에 접근하는 동시 실행 수를 제한하는 패턴을 **격벽패턴(Bulkhead)** 이라 한다.  

- `SemaphoreBulkhead`: 세마포어 사용
- `FixedThreadPoolBulkhead`: 고정된 스레드 풀을 사용

> 성능상 `FixedThreadPoolBulkhead` 사용을 권장  

```java
// SemaphoreBulkhead
BulkheadConfig config = BulkheadConfig.custom()
  .maxConcurrentCalls(150) // 허용할 병렬 실행 수, default 25
  .maxWaitDuration(Duration.ofMillis(500)) // 포화상태일 때 block 시간, default 0
  .build();
BulkheadRegistry registry = BulkheadRegistry.of(config);
Bulkhead b1 = registry.bulkhead("name1");
```

```java
// FixedThreadPoolBulkhead
ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
  .maxThreadPoolSize(10) // 최대 스레드 풀 크기, default availableProcessors
  .coreThreadPoolSize(2) // 코어 스레드 풀 크기, default availableProcessors - 1
  .queueCapacity(20) // 대기열의 용량, default 100
  .build();
ThreadPoolBulkheadRegistry tpbr = ThreadPoolBulkheadRegistry.of(config); // "default" key 로 들어감
ThreadPoolBulkhead tpb = tpbr.bulkhead("name1");
ThreadPoolBulkheadConfig tpbc = ThreadPoolBulkheadConfig.custom()
  .maxThreadPoolSize(5)
  .build();
ThreadPoolBulkhead bulkheadWithCustomConfig = tpbr.bulkhead("name2", tpbc);
```

### RateLimiter

`Rate limiting` 은 서비스의 고가용성과 안정성을 확립하기 위해  
API 요청(트래픽) 제한치를 넘어간 것을 감지했을 때의 동작, 제한 할 요청 타입 등을 정의할 수 있다.  
간단히 제한치를 넘어선 요청을 거부하거나, 큐를 만들어 나중에 실행할 수도 있고, 어떤 방식으로든 두 정책을 조합해도 된다.

`limit` 감지는 아래 그림처럼 이루어 진다.  

![r4j2](/assets/springboot/spring-cloud/springcloud_r4j3.png)  

매 `cycle` 마다 갱신되는 `period` 가 있고  
`period` 가 0일 때 접근시 정지(park) 했다가 다시 접근하는 구조이다.  

```java
RateLimiterConfig config = RateLimiterConfig.custom()
  .limitRefreshPeriod(Duration.ofMillis(1)) // 스레드가 period 흭득을 기다리는 시간, default 5s
  .timeoutDuration(Duration.ofMillis(25)) // period 갱신 주기, default 500ns
  .limitForPeriod(10) // period 수, default 50
  .build();
RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
RateLimiter rateLimiterWithDefaultConfig = rateLimiterRegistry.rateLimiter("name1");
RateLimiter rateLimiterWithCustomConfig = rateLimiterRegistry.rateLimiter("name2", config);
```


### Retry

실패한 요청에 대한 재시도 정책을 구성할 수 있다.  

```java
RetryConfig config = RetryConfig.custom()
  .maxAttempts(2) // 재시도 횟수 default 3
  .waitDuration(Duration.ofMillis(1000)) // 재시도 대기시간
  .retryOnResult(response -> response.getStatus() == 500) // 재시도 여부 predicate, default just return false
  .retryOnException(e -> e instanceof WebServiceException) // 재시도 여부 exception predicate
  .retryExceptions(IOException.class, TimeoutException.class) // 실패도 기록해서 재시도할 예외, default null,
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class) // 무시할 예외, 재시도 X, default null
  .failAfterMaxAttempts(true) // 재시도 결과 끝내 실패시 MaxRetriesExceededException 발생 여부, default false
  .build();
RetryRegistry registry = RetryRegistry.of(config);
Retry retryWithDefaultConfig = registry.retry("name1");
RetryConfig custom = RetryConfig.custom()
  .waitDuration(Duration.ofMillis(100))
  .build();
Retry retryWithCustomConfig = registry.retry("name2", custom);
```

### TimeLimiter  

실행중인 메서드의 `time limit` 을 관리한다.  

```java
TimeLimiterConfig config = TimeLimiterConfig.custom()
  .cancelRunningFuture(true) // 실행중인 Future 에서 취소여부, default true
  .timeoutDuration(Duration.ofMillis(500)) // 제한 시간, default 1s
  .build();
TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
TimeLimiter timeLimiter1 = registry.timeLimiter("name1");
TimeLimiter timeLimiter2 = registry.timeLimiter("name2", config);
```

usage 에서 `@TimeLimiter` 어노테이션을 사용하거나 데코레이터로 감싸 사용하는데  
반환타입이 `Mono`, `Flux`, `CompletableFuture` 셋중 하나여야 한다.  

### usage

`CircuitBreaker`, `Bulkhead`, `RateLimiter`, `Retry`, `TimeLimiter` 모두 데코레이트 패턴을 기반으로 하는 객체들로  
적용하고 싶은 메서드를 감쌓면 된다.  

```java
public class BackendService {
    // 감싸고싶은 메서드
    public String doSomething() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return "error invoked, msg:" + e.getMessage();
        }
        return "hello " + LocalDateTime.now();
    }
}
```

`event publisher` 를 통해 각종 설정에 따라 호출할 `event` 메서드 정의 가능  

```java
public static void main(String[] args) {
BackendService backendService = new BackendService();
CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendService");
circuitBreaker.getEventPublisher()
    .onSuccess(event -> System.out.println("onSuccess invoked, " + event))
    .onError(event -> System.out.println("onError invoked, " + event))
    .onIgnoredError(event -> System.out.println("onIgnoredError invoked, " + event))
    .onReset(event -> System.out.println("onReset invoked, " + event))
    .onStateTransition(event -> System.out.println("onStateTransition invoked, " + event))
;
String result1 = circuitBreaker.executeSupplier(() -> backendService.doSomething());
System.out.println(result1);
}
```

```
onSuccess invoked, 2023-05-02T17:42:36.286077+09:00[Asia/Seoul]: CircuitBreaker 'backendService' recorded a successful call. Elapsed time: 1023 ms
hello 2023-05-02T17:42:36.266595
```

`CircuitBreaker`, `Retry`, `Bulkhead` 모두 한 `supplier` 람다 메서드에서 동작해야 할 경우 아래와 같이 데로레이트 패턴으로 감싸면 된다.  

```java
BackendService backendService = new BackendService();
CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("backendService");
circuitBreaker.getEventPublisher()
    .onSuccess(event -> System.out.println("circuitBreaker onSuccess invoked, " + event))
    .onError(event -> System.out.println("circuitBreaker onError invoked, " + event))
    .onIgnoredError(event -> System.out.println("circuitBreaker onIgnoredError invoked, " + event))
    .onReset(event -> System.out.println("circuitBreaker onReset invoked, " + event))
    .onStateTransition(event -> System.out.println("circuitBreaker onStateTransition invoked, " + event));

Retry retry = Retry.ofDefaults("backendService");
retry.getEventPublisher()
    .onRetry(event -> System.out.println("retry onRetry invoked, " + event))
    .onSuccess(event -> System.out.println("retry onSuccess invoked, " + event))
    .onError(event -> System.out.println("retry onError invoked, " + event))
    .onIgnoredError(event -> System.out.println("retry onIgnoredError invoked, " + event));

Bulkhead bulkhead = Bulkhead.ofDefaults("backendService");
bulkhead.getEventPublisher()
    .onCallPermitted(event -> System.out.println("bulkhead onCallPermitted invoked, " + event))
    .onCallRejected(event -> System.out.println("bulkhead onCallRejected invoked, " + event))
    .onCallFinished(event -> System.out.println("bulkhead onCallFinished invoked, " + event));

// decorate
Supplier<String> supplier = () -> backendService.doSomething();
supplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
supplier = Retry.decorateSupplier(retry, supplier);
supplier = Bulkhead.decorateSupplier(bulkhead, supplier);
String result = supplier.get();
System.out.println(result);
```

```
bulkhead onCallPermitted invoked, 2023-05-02T18:57:07.145426+09:00[Asia/Seoul]: Bulkhead 'backendService' permitted a call.
circuitBreaker onSuccess invoked, 2023-05-02T18:57:08.172639+09:00[Asia/Seoul]: CircuitBreaker 'backendService' recorded a successful call. Elapsed time: 1000 ms
bulkhead onCallFinished invoked, 2023-05-02T18:57:08.176404+09:00[Asia/Seoul]: Bulkhead 'backendService' has finished a call.
hello 2023-05-02T18:57:08.164362
```

### usage - Spring Cloud

> <https://github.com/resilience4j/resilience4j-spring-boot2-demo>

위의 공식 `Spring Boot Resilience4j Demo` 페이지를 보는 것을 추천  

아래와 같이 Spring Boot 에선 어노테이션과 AOP 환경을 사용해 아래와 같이 구현 가능하다.  

> 위의 데코레이트 패턴으로 `Resilience4j` 를 적용해도 좋은 방법

```java
@Component(value = "backendAService")
public class BackendAService implements Service {

    private static final String BACKEND_A = "backendA";

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    @Retry(name = BACKEND_A)
    public String failure() {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }

    @Override
    @CircuitBreaker(name = BACKEND_A)
    @Bulkhead(name = BACKEND_A)
    public String ignoreException() {
        throw new BusinessException("This exception is ignored by the CircuitBreaker of backend A");
    }
    ...
}
```

`CircuitBreakerRegistry` 에 `CircuitBreakConfig` 를 설정하고 위의 `@CircuitBreaker` `name` 속성과 매핑해서 사용할 수 있다.  

```conf
resilience4j.circuitbreaker.instances.backendA.sliding-window-size=10
resilience4j.circuitbreaker.instances.backendB.sliding-window-size=12
```

`java config` 로 설정하고 싶다면 아래와 같이 `CircuitBreakerRegistry` 를 `@Bean` 으로 재등록해야 한다.  

```java
@Bean
public CircuitBreakerRegistry circuitBreakerRegistry() {
    RegistryStore<CircuitBreaker> stores = new InMemoryRegistryStore<>();
    stores.putIfAbsent("backendA", CircuitBreaker.of("backendA", CircuitBreakerConfig
        .from(CircuitBreakerConfig.ofDefaults())
        .slidingWindowSize(10)
        .build()));
    stores.putIfAbsent("backendB", CircuitBreaker.of("backendB", CircuitBreakerConfig
        .from(CircuitBreakerConfig.ofDefaults())
        .slidingWindowSize(12)
        .build()));
    CircuitBreakerRegistry registry = CircuitBreakerRegistry.custom()
        .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        .withRegistryStore(stores)
        .build();
    return registry;
}
```

`Resilience4j` AOP 어노테이션을 사용하는 경우 미리 생성해둔 `CircuitBreaker` 인스턴스를 부여하는 방식이기 때문에 `RegistryStore` 에 사전에 객체를 생성해서 저장해두어야 한다.  

> 동일한 `CircuitBreaker` 인스턴스를 사용하기 때문에 `state` 가 `open` 으로 변경되면 해당 `CircuitBreaker` 인스턴스를 사용하는 모든 메서드는 예외처리된다.

비단 `CircuitBreaker` 뿐 아니라 `Bulkhead`, `Retry` 모두 `Register` 구조를 따르고 있음으로 동일하게 구성하면 된다.  
`java config` 보다 `properties` 기반 구성이 가독성이 좋음으로 `properties` 사용을 권장  

어노테이션과 AOP 를 사용하면 자동 생성되는 코드는 아래와 같은 순서로 데코레이트된다.  
`properties` 를 사용해 순서를 변경 가능함으로 참고  

```
Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(Function)))))
```

```conf
resilience4j.circuitbreaker.circuitBreakerAspectOrder=1
resilience4j.retry.retryAspectOrder=2
```

### usage - Feign

아래와 같이 `@FeignClient` 어노테이션을 가진 클래스 메서드에 바로 `Resilience4j` 어노테이션을 적용해도 상관없다.  

```java
@FeignClient(name = "product-service")
public interface ProductClient {
    @CircuitBreaker(name = "product")
    @PostMapping("/product/{ids}")
    List<Product> findByIds(@PathVariable List<Long> ids);
}
```

아래 `resilience4j-feign` 라이브러리를 추가하면 `Feign` 객체에 집접적으로 `Resilience4j` 의 데코레이트 패턴을 적용할 수 있도록 라이브러리를 제공한다.  

> <https://resilience4j.readme.io/docs/feign>

```groovy
implementation "io.github.resilience4j:resilience4j-feign"
```

기존 `Feign.builder` 메서드를 사용하지 않고  `Resilience4jFeign.builder` 메서드를 사용해 `Resilience4j` 로 데코레이트된 `Feign` 객체를 반환한다.  

```java
@Bean
ProductRequestLine productService(@Autowired ObjectFactory<HttpMessageConverters> messageConverters) {
    FeignDecorators decorators = FeignDecorators.builder()
        .withCircuitBreaker(CircuitBreaker.ofDefaults("product"))
        .withRateLimiter(RateLimiter.ofDefaults("product"))
        .build();
    return Resilience4jFeign.builder(decorators)
        .encoder(new SpringEncoder(messageConverters))
        .decoder(new SpringDecoder(messageConverters))
        .target(ProductRequestLine.class, "http://localhost:8080/");
}
```

> 개인적으로 `@FeignClient` 와 `Resilience4j` 어노테이션을 사용하는 방법이 더 구성하기 쉽고 간결한 것 같음  

### fallback

`Resilience4j` 에선 예외 방지정책으로 `fallback` 기능을 제공한다.  

`io.github.resilience4j:resilience4j-all` 의존성을 주입하면 아래처럼 `fallback` 메서 핸들링할 예외를 직접 지정할 수 있다.  
클래스명처럼 데코레이트 패턴을 이용하여 구현한다.  

```java
Supplier<String> supplier = () -> backendService.doSomething();
Decorators.DecorateSupplier<String> decorateSupplier = Decorators.ofSupplier(supplier)
  .withBulkhead(bulkhead)
  .withCircuitBreaker(circuitBreaker)
  .withFallback(asList(TimeoutException.class,
    CallNotPermittedException.class,
    BulkheadFullException.class), throwable -> "Hello from Recovery");
result = decorateSupplier.get();
```

`Resilience4j` 의 모든 core 서비스에는 `fallback` 정의가 가능하다.  

```java
@CircuitBreaker(name = BACKEND_A, fallbackMethod = "failureFallback")
@RateLimiter(name = BACKEND_A, fallbackMethod = "failureFallback")
@Bulkhead(name = BACKEND_A, fallbackMethod = "failureFallback")
@Retry(name = BACKEND_A, fallbackMethod = "failureFallback")
public String failure() {
    log.info("failure invoked");
    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
}

public String failureFallback(Exception e) {
    log.info("failureFallback invoked, error type:{}, msg:{}", e.getClass().getSimpleName(), e.getMessage());
    return "failure invoked but return string";
}
```

`Retry` 의 경우 본 메서드 실패시 `Fallback` 메서드 실행된다(`Fallback` 메서드가 실패하면 다시 `Retry` 의 반복)  
`CircuitBreaker` 의 경우 `state open` 되어야 `Fallback` 메서드가 실행된다.  
`RateLimiter` 의 경우 지정된 `period` 한계를 넘었을 때 `Fallback` 메서드가 실행된다.  
`Bulkhead` 의 경우 세마포어 혹은 스레드 풀이 다 차서 호출할 수 없을 때 `Fallback` 메서드가 실행된다.  

> 메서드 실행시 단순 예외 발생으로 인해 `Fallback` 호출하는 경우는 `Retry` 의 `fallback` 속성밖에 없다.  

라이브러리가 다르기에 위에있는 `Decorators` 가 아닌 `FallbackDecorators` 를 사용해 구현하였지만  
근본적으로 데코레이트한 메서드에서 예외가 발생하면 예외 종류별로 지정해둔 핸들러메서드를 호출하는 구조는 동일하다.  

`Retry`, `CircuitBreaker`, `RateLimiter`, `Bulkhead` 별로 발생하는 예외의 종류가 다르기에 별도로 `Fallback` 에 헨들러메서드를 등록해두는 것 뿐이다.  

#### fallback with openfeign

`@FeignClient` 에도 `fallback` 속성이 있지만 자체적인 데코레이트 코드를 사용하는 것이 아니고 `Resilience4j` 에 의존적인 코드이다.  

`openfeign` 라이브러리에 아래와 같은 코드가 있다.  

```java
package org.springframework.cloud.openfeign;

public class FeignAutoConfiguration {

    ...

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(CircuitBreaker.class)
    @ConditionalOnProperty(value = "feign.circuitbreaker.enabled", havingValue = "true")
    protected static class CircuitBreakerPresentFeignTargeterConfiguration {

        @Bean
        @ConditionalOnMissingBean(CircuitBreakerFactory.class)
        public Targeter defaultFeignTargeter() {
            return new DefaultTargeter();
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(CircuitBreakerFactory.class)
        public Targeter circuitBreakerFeignTargeter(CircuitBreakerFactory circuitBreakerFactory) {
            return new FeignCircuitBreakerTargeter(circuitBreakerFactory);
        }
    }
    ...
}
```

`CircuitBreakerFactory` `Bean` 의 존재여부에 따라  
`DefaultTargeter` 를 통해 일반적인 `Feign` 객체를 만들지,  
`FeignCircuitBreakerTargeter` 를 통해 `CircuitBreaker` 로 데코레이트된 `Feign` 객체를 만들지 결정된다.  

`CircuitBreakerFactory`, `CircuitBreaker` 모두 `interface` 로 `Spring Cloud` 라이브러리에 구현체는 존재하지 않는다.  
따라서 직접 구현하거나 `Resilience4j` 에 정의되어 있는 `CircuitBreakerFactory`, `CircuitBreaker` 구현체를 사용해야 한다.  

> 당연히 `Resilience4j` 를 사용하는 것을 권장...

`Resilience4j` 라이브러리를 의존하면서 자연스럽게 모든 `Feign` 객체들은 `FeignCircuitBreakerTargeter` 를 통해 생성될 것이고 `fallback` 속성또한 데코레이트 된다.  

`feign` 의 `fallback` 속성은 모든 예외에 대해 `fallback` 메서드로 전달된다.  

위에서 나온 `FeignDecorators.builder` 로도 fallback 구성이 가능하다.  

```java
@Bean
ProductRequestLine productService(@Autowired ObjectFactory<HttpMessageConverters> messageConverters) {
    // builder 순서대로 ordering 됨
    ProductRequestLine requestFailedFallback = ...;
    ProductRequestLine circuitBreakerFallback = ...;
    FeignDecorators decorators = FeignDecorators.builder()
        .withCircuitBreaker(CircuitBreaker.ofDefaults("product"))
        .withRateLimiter(RateLimiter.ofDefaults("product"))
        .withFallback(requestFailedFallback, FeignException.class)
        .withFallback(circuitBreakerFallback, CircuitBreakerOpenException.class)
      .build();
    return Resilience4jFeign.builder(decorators)
        .encoder(new SpringEncoder(messageConverters))
        .decoder(new SpringDecoder(messageConverters))
        .target(ProductRequestLine.class, "http://localhost:8080/");
}
```
