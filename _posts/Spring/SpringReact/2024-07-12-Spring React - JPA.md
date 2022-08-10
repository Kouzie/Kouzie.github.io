---
title:  "Spring React - JPA!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## classes: wide

categories:
  - spring-reative
---


## WebFlux 에서 JPA 사용하기  

`WebFlux` 에서 `JPA` 를 굳이 사용해보려는 이유는 `Sharding Spherer` 를 `WebFlux` 에 적용하기 위해서이다.  

이번포스팅에선 WebMVC 와 WebFlux 의 성능비교와 함께 WebFlux 환경에서 JPA 의 트랜잭션 기능이 모두 동작하는지 테스트하는것을 목표로한다.  

> R2DBC 로 샤딩을 처리하려면 `AbstractRoutingConnectionFactory` 를 사용하면 된다.  
> 하지만 샤딩에 대한 조회 및 병합은 직접 구현해야 한다.  


아래와 같이 1.5 초동안 sleep 처리된 함수를 동시에 1000번 수행하는 테스트를 진행한다.  

```java
@Transactional
public OrderDto addRandomOrder(Long accountId) {
    OrderEntity entity = new OrderEntity(
            RandomTestUtil.generateRandomString(10),
            accountId);
    try {
        Thread.sleep(1500); // 대충 블로킹
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    entity = orderRepository.save(entity);
    return toDto(entity);
}
```

`WebFlux` 에선 아래 코드로 `Blocking` 함수를 감싸 조치한다.  

```java
public class ReactUtil {

    public static <T> Flux<T> blockingToFlux(Supplier<List<T>> supplier) {
        return Flux.defer(() -> Flux.fromIterable(supplier.get()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public static <T> Mono<T> blockingToMono(Supplier<T> supplier) {
        return Mono.fromSupplier(supplier)
                .subscribeOn(Schedulers.boundedElastic());
    }
}

...

@PostMapping("/{accountId}")
public Mono<OrderDto> addRandomOrder(@PathVariable Long accountId) {
    return ReactUtil.blockingToMono(() -> {
        return orderService.addRandomOrder(accountId);
    });
}
```

## 테스트

테스트는 k6 를 통해 진행한다.  
대기시간 1초로 동안 1000개의 vuser 가 요청을 수행한다.  

```js
import http from 'k6/http';

export let options = {
    vus: 1000, // 가상 사용자 수
    duration: '1s', // 테스트 지속 시간
};

export default function () {
    const url = 'http://localhost:8080/order';
    const payload = JSON.stringify({
        // 필요한 경우 요청 페이로드 추가
        // key: 'value'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    http.post(url, payload, params);
}
```

사실 위 코드를 사용해 `MVC` 와 `WebFlux` 를 비교하는것은 큰 의미는 없다.  
현재 동작중인 컴퓨터에서 `Tomcat Thread pool` 는 200개 넘게 만들 수 있지만 `Thread.sleep` 을 동시에 실행하는 횟수에 제한이 있기 때문이다.  

`Thread.sleep` 을 통해 OS 타이머에 접근해 연산하는 과정에 횟수 제한이 있기 때문이라 생각된다.  

`Spring MVC` 에선 `Thread.sleep` 를 동시에 50개, `Spring WebFlux` 에선 `Thread.sleep` 를 동시에 100개 실행할 수 있다.  
이 차이 때문에 `Spring WebFlux` 가 2배정도 빠르게 수행된다.  

하지만 아래와 같이 `Thread.sleep` 이 아닌 `Mono.delay` 를 사용하고 NIO 함수끼리 묶게 될 경우 압도적인 성능차이가 발생한다.  


```java
@PostMapping
public Mono<OrderDto> addRandomOrder() {
    Mono<OrderDto> result = ReactUtil.blockingToMono(() -> {
        AccountDto accountDto = accountService.getRandomAccount();
        return orderService.addRandomOrder(accountDto.getAccountId());
    });
    return  Mono.delay(Duration.ofSeconds(1))
            .doOnNext(t -> System.out.println("timeover"))
            .then(result);
}
```

![1](/assets/springboot/spring-react/springreact_jpa1.png)

![1](/assets/springboot/spring-react/springreact_jpa2.png)

![1](/assets/springboot/spring-react/springreact_jpa3.png)

수행결과는 아래와 같다.  

- SpringMVC + Thread.sleep 조합 (31초, 1992개)  
- SpringWebFlux + Thread.sleep 조합 (17초, 2698)  
- SpringWebFlux + Mono.delay 조합 (1.7초, 3847)  

Tomcat 의 경우 요청대기풀에서 최대 31초간 블락된 사용자가 있을 수 있다(시간제한으로 중단된 요청도 있음).  

`Mono.delay` 내부에서도 `Thread.sleep` 과 마찬자기로 OS 에서 제공하는 타이머를 사용하겠지만 프레임워크에서 구현된 NIO 로직을 통해 효율적으로 진행되는것으로 판단된다.  

### 트랜잭션 테스트

동시에 3개 요청을 보내 status 값을 체크한 뒤 이후과정을 진행하는 테스트 작성  
한번은 200OK, 뒤에 요청되는 두번은 400 에러가 발생하는치 테스트한다.  

```js
import http from 'k6/http';
import { check, group } from 'k6';

// Options for the test
export const options = {
  vus: 3, // Number of Virtual Users
};

const orderId = '1018674689176219648';

export default function () {
  const url = `http://localhost:8080/order/status/${orderId}`;
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Sending 3 requests concurrently
  let responses = [];

  group('Sending 3 concurrent requests', () => {
    responses = http.batch([
      ['PUT', url, null, params],
      ['PUT', url, null, params],
      ['PUT', url, null, params],
    ]);
  });

  // Check responses
  let success = 0;
  let badRequest = 0;

  responses.forEach((response) => {
    if (response.status === 200) {
      success++;
    } else if (response.status === 400) {
      badRequest++;
    }
  });

  check(success, { 'One request succeeded': (s) => s === 1 });
  check(badRequest, { 'Two requests failed with 400 error': (b) => b === 2 });
}
```

`running -> block(1sec) -> complete` 하는 과정으로 진행  

```java
@PutMapping("/status/{id}")
public Mono<OrderDto> updateStatus(@PathVariable Long id) {
    Mono<OrderDto> changeStatusRunning = ReactUtil.blockingToMono(() -> 
        orderService.changeOrderStatusRunning(id));
    Mono<Long> doSomethingDeploy = Mono.delay(Duration.ofSeconds(1))
            .doOnNext(t -> System.out.println("timeover"));
    Mono<OrderDto> changeStatusComplete = ReactUtil.blockingToMono(() -> 
        orderService.changeOrderStatusComplete(id));
    return changeStatusRunning
            .then(doSomethingDeploy)
            .then(changeStatusComplete);
}
```

`orderService.changeOrderStatusRunning` 메서드안에선 `Perssimistc Lock` 을 사용하여 타 스레드의 동시접근을 막았다.  

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = AvailableSettings.JAKARTA_LOCK_TIMEOUT, value   ="5000"))
@Query("SELECT oe FROM OrderEntity oe WHERE oe.id = :id")
Optional<OrderEntity> findByIdForUpdate(@Param("id") Long id);
```

![1](/assets/springboot/spring-react/springreact_jpa4.png)

## 데모코드  

> <https://github.com/Kouzie/spring-reactive-demo/tree/master/spring-react-jpa>  
> <https://github.com/Kouzie/spring-boot-demo/tree/main/sharding-demo/sharding-sphere>  