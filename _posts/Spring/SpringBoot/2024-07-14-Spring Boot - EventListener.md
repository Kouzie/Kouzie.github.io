---
title:  "Spring Boot - EventListener!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## EventListener

의존성 분리를 위해 Spring 에서 지원하는 이벤트 처리 방식  
아래와 같이 `@EventListener` 어노테이션으로 이벤트를 처리할 메서드를 정의해 놓으면, 이벤트가 처리될 위치에서 AOP 를 통해 `EventListener 핸들러 함수`가 호출되도록 설정된다.  

```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // 기본적으로 유지할 스레드 수
    executor.setMaxPoolSize(50); // 최대 스레드 수
    executor.setQueueCapacity(100); // 큐 용량
    executor.setThreadNamePrefix("AsyncThread-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 처리되지 않은 작업 처리 정책
    executor.initialize();
    return executor;
}

...

@Component
public class DemoEventListener {

    @Async("taskExecutor")
    @EventListener
    public void handleAccountCreateEvent(AccountCreateEvent event) throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("Received custom event, account:" + event.getAccount());
    }
}
```

AOP 로 호출하게 되면 당연히 하나의 스레드로 처리되기 때문에 좀더 확실한 의존성 분리를 위해 `@Async` 어노테이션을 사용하는 경우가 많다.  

아래와 같이 `publishEvent` 함수를 호출하게 되면 내부에서 정의한 `EventListener 핸들러 함수` 를 호출하게 된다.  

`publishEvent` 함수는 `ApplicationContext` 에 `override` 된 `publishEvent` 로 흘러들어가게되고, 정의한 `EventListener 핸들러 함수` 를 호출하게 된다.  

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AccountDto createAccount(CreateAccountRequestDto dto) {
        log.info("createAccount invoked");
        AccountEntity entity = toEntity(dto);
        entity = repository.save(entity);
        log.info("account save end");
        AccountDto result = toDto(entity);
        // 해당 부분에서 aop 를 통해 handleAccountCreateEvent 메서드가 호출되게됨
        eventPublisher.publishEvent(new AccountCreateEvent(result));
        // if (new Random().nextInt() % 2 == 0)
        //     throw new IllegalArgumentException("temp exception");
        log.info("account return");
        return result;
    }
}
```

아무리 비동기로 이벤트 처리 스레드를 나누었다 하더라도 `theradPool` 의 `queueCapacity` 를 넘어가는 이벤트가 발생하게되면 결국 `RejectedExecutionException` 이 발생하게된다.  

적절한 백프레셔 제한처리나 모니터링/알림 처리를 진행해야한다.  

### TransactionalEventListener

위의 경우 의존성은 분리했지만 트랜잭션의 일관성처리는 되고있지 않다.  
이벤트는 발행했는데 후처리 과정에서 `Account` 저장에 실패하게 될 경우 이벤트의 발행 자체가 일관성을 망가뜨리는 행위가 된다.  

때문에 트랜잭션과 이벤트를 묶어 발행시킬 수 있는 `TransactionalEventListener` 를 제공한다.  

- `AFTER_COMMIT`:트랜잭션이 성공적으로 커밋된 후에 호출.  
- `AFTER_ROLLBACK`: 트랜잭션이 롤백된 후에 호출.  
- `AFTER_COMPLETION`: 트랜잭션이 커밋되었는지 롤백되었는지에 관계없이 트랜잭션이 완료된 후에 호출.  
- `BEFORE_COMMIT`: 트랜잭션이 커밋되기 직전에 호출.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleAccountCreateEvent(AccountCreateEvent event) throws InterruptedException {
    Thread.sleep(2000);
    log.info("Received custom event, account:" + event.getAccount());
}
```

실제 출력된 결과를 보면 `account return` 로그가 출력된 뒤 `EventListener` 함수가 호출되었다.  

> Async 메세지를 제거해도 `publishEvent` 메서드로 인해 바로 호출되지 않음  

```log
[nio-8080-exec-1] c.e.e.service.AccountService   : createAccount invoked
[nio-8080-exec-1] c.e.e.service.AccountService   : account save end
[nio-8080-exec-1] c.e.e.service.AccountService   : account return
[nio-8080-exec-1] c.e.e.event.DemoEventListener  : Received custom event, account:AccountDto(accountId=1, name=kouzie, username=kouzie, email=kouzie@naver.com)
```

내부적으로 `publishEvent` 호출시 `EventListener 핸들러 함수` 가 `callback 함수` 형태로 동작하도록 저장해놓고, 나중에 트랜잭션이 완료된 후 해당 `callback 함수`가 동작되도록 하는 방식이다.  

`org.springframework.transaction` 라이브러리에서 `callback 함수` 등록을 지원하기 때문에 가능한 설정이다.  

`publishEvent` 가 호출될 떄 마다 `TransactionalApplicationListenerSynchronization` 인스턴스를 계속 생성하고 `TransactionSynchronizationManager` 에 리스너(`callback 함수`)로 등록한다.  


## 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/event-listener-demo>