---
title:  "Spring Boot - Virtual Thread!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Virtual Thread

> 출처  
> <https://techblog.woowahan.com/15398/>
> <https://tech.kakaopay.com/post/ro-spring-virtual-thread/>  
> <https://d2.naver.com/helloworld/1203723>  
> <https://www.youtube.com/live/BZMZIM-n4C0>  
> <https://www.youtube.com/watch?v=AuBHv8NOca4>  

Java21 에서 공개된 `Virtual Thread` 를 통해 Blocking 기반의 코드에서 획기적인 성능향상이 가능하다.  

Java21 이전, 기존 JVM 에선 OS에서 제공되는 Thread 와 직접 매핑되는 `Platform Thread` 를 통해 CPU 자원을 사용해왔다.  

![1](/assets/springboot/virtual_thread_1.png)
Java 에서 생성 가능한 Thread 개수는 OS 지원 Thread 를 초과할 수 없고 Request 사용자만큼 Thread 가 생성되는 Spring MVC 서버에선 OS 최대개수만큼 동시접속자 처리가 불가능하다는 뜻이다.  

Java21 Virtual Thread 에선 JVM과 OS 사이에서 JNI(Java Native Interface) 가 수행하는 비싼 Operation JVM 내에서 경량 스레드 방식을 통해 최적화 하는 방식을 지원한다.  

![1](/assets/springboot/virtual_thread_2.png)

- Thread 생성 및 Context switch 오버헤드 감소.  
  - 어플리케이션 레벨에서 메모리조작을 통해 Context Switch 진행  
  - user mode, kernel mode 스위칭 필요 없음  
- Thread 당 수 MB에 달하는 메모리 차지 제거.  
- OS Thread 가용 수 상관없이 Thread 생성 가능.  

이런 방식이 가능한 이유는 Platform Thread 의 ForkJoinPool 스케줄러애서 시스템콜이 필요한 I/O 작업이 발생할 때 마다 CPU 를 사용하는 스레드를 블록킹하지 않고 스케줄링을 통해 내부 Virtual Thread 들이 CPU 자원을 효율적으로 사용할 수 있도록 지원한다.  

### Continuation, park/unpark

> <https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/lang/VirtualThread.java>

기존 JDK 에서 I/O 발생시 호출되는 `park/unpark` native 함수에선 시스템 콜을 통해 현재 실행중인 Thread 를 block 시키지만,  
Virtual Thread 에선 JVM 내부 **Continuation** 기반 스케줄링을 사용하여 Thread 의 중단/재개를 스위칭하지 시스템 콜을 사용하지 않는다.  

> 여기서 **Continuation** 은 코루틴, pyhton 의 yield 와 비슷한 방식

커널 스레드의 중단(시스템콜)이 없어지면서 컨텍스트 스위칭 비용도 많이 낮아졌다.  

Virtual Thread 동작방식을 설명할때 아래 3종류의 Thread 로 구분짖는다.  

- **Virtual Thread**  
  - 사용자 공간에서 관리되는 경량 스레드.
- **Platform Thread**  
  - Java의 기존 스레드, OS Thread 와 같음.  
- **Carrier Thread**  
  - Virtual Thread 가 실행될 때 Platform Thread 로 운반(Carrier) 하여 OS Thread 자원을 사용하는 개념.  
  - Virtual Thread 가 사용을 기다리는 공유자원, ForkJoinPool 에서 관리된다.  

Virtual Thread 가 실제 자원인 Carrier Thread 를 사용하는 순서는 아래와 같다.  

- Virtual Thread는 일반 메서드처럼 실행되다가 차단 가능한 지점(sleep, I/O, lock)에서 `park()`를 호출.  
- `park()` 시점에 실행되던 Virtual Thread 상태를 Continuation 으로 저장하고 Carrier Thread를 반환.
- 이후 적절한 시점에 `unpark()`로 다시 실행을 예약.  

> `park()` 함수는 File(Network) 등 Blocking I/O 함수 호출시 내부 코드에서 항상 사용된다.  
> `unblock()` 함수는 File(Network) 작업이 완료되면 OS 저수준 라이브러리(like `sun.nio.ch`)에서 호출해준다.  
> 기존에 Multiplexing IO 방식으로 동작하는 JavaNIO 방식과는 다른 방식이다.  

Continuation 기반 스케줄링을 통해 I/O 작업을 효율적으로 처리할 수 있게되었다.  

즉 Virtual Thread 는 `park/unpark` 라는 스레드 제어를 위한 저수준 API(native 함수) 를 Continuation 스케줄링을 사용해 스레드를 직접 제어한다.  

### Pinning  

만병통치약같은 Virtual Thread 환경에서도 Pinning 을 주의해야한다.  
보통 kernel 영역에서 블록킹되어버리는 상황에서 발생한다.  

Pinning 블록킹 연산이 특정 조건에서 발생할 때 발생한다. Virtual Thread 가 Carrier Thread에 고정(pinned) 되어있는 상황을 뜻한다.  

- synchronized 블록 안에서 블로킹 I/O 호출  
- 네이티브 메서드 안에서 블로킹 I/O 호출  

위와 같은 예에선 블로킹 I/O 가 발생했을 때 suspend 되어 다른 가상스레드가 Carrier Thread 를 사용하지 못하고 블로킹 시간동안 점유하게 된다.  

`synchronized` 키워드는 대부분 라이브러리에서 잘 사용되고 있지 않지만 일부 라이브러리에선 계속 사용되어서 사용시 주의해야한다.  

> `Java24` 부턴 `synchronized` 키워드에서 블로킹 I/O 가 발생해도 동작시키도록 수정되었다. 향후 Java25(LTS) 가 나오면 변경을 추천.  

Pinning 이 일어나는지 확인하려면 코드 실행시 `-Djdk.tracePinnedThreads=full` 옵션을 사용하면 된다.  


### Reactive 와 비교

WebSocket/SSE 같은 **idle but alive** 이벤트 루프(Netty) 모델이 더 났다.  
Virtual Thread 로 웹소켓 수만 개를 연결하게 되면 스레드도 수만 개가 생성되고 메모리와 스케줄링 오버헤드가 누적된다.  

스레드를 수만개 관리하는 것 보다 TCP 소켓 이벤트를 수만개 관리하는 Reactive 모델이 효율적이다.  



## 데모 코드  

> <https://github.com/Kouzie/spring-virtual-thread-test>  

간단히 `spring.threads.virtual.enabled` 설정을 on/off 하여 k6s 로 테스트

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

위 설정을 통해 Tomcat, Async, Schedule 을 위한 Thread Pool 은 Virtual Thread 를 사용한다.  

```kotlin
@SpringBootApplication
class MvcDemoApplication

fun main(args: Array<String>) {
    runApplication<MvcDemoApplication>(*args)
}

@RestController
class DemoController {

    @GetMapping("/demo")
    fun get(): String {
        Thread.sleep(2000)
        return "Is virtual thread: ${Thread.currentThread().isVirtual}"
    }
}
```

2000개의 가상사용자가 동시에 한번 호출하는 시나리오  

```js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 2000, // 가상 사용자 수 (동시 요청 수)
  iterations: 2000, // 총 요청 수 (각 VU가 1번씩 요청)
  duration: '1m', // 테스트 지속 시간
};

export default function () {
  check(res, {
    'is status 200': (r) => r.status === 200,
  });
}
```

Tomcat 에선 기본 스레드 개수인 200개씩 2초마다 처리하느라 20초가 걸리고  
Virtual Thread 를 사용하는 경우 2초에 2000개의 요청이 한번에 처리 완료되었다.  

```sh
k6 run get_demo.js

running (0m20.1s), 0000/2000 VUs, 2000 complete and 0 interrupted iterations
default ✓ [======================================] 2000 VUs  0m20.1s/1m0s  2000/2000 shared iters

k6 run get_demo.js

running (0m02.2s), 0000/2000 VUs, 2000 complete and 0 interrupted iterations
default ✓ [======================================] 2000 VUs  0m02.2s/1m0s  2000/2000 shared iters
```

간단히 Webflux 에서도 테스트 하였는데 Virtual Thread 를 사용하는것과 동일한 결과가 출력되었다.  

```kotlin
@SpringBootApplication
class WebfluxDemoApplication

fun main(args: Array<String>) {
    runApplication<WebfluxDemoApplication>(*args)
}

@RestController
class DemoController {

    @GetMapping("/demo")
    fun get(): Mono<String> {
        return Mono.delay(Duration.ofSeconds(2)) // 2초 대기
            .map { "Demo(Thread=${Thread.currentThread().name})" }
    }
}
```


```sh
running (0m02.2s), 0000/2000 VUs, 2000 complete and 0 interrupted iterations
default ✓ [======================================] 2000 VUs  0m02.2s/1m0s  2000/2000 shared iters
```