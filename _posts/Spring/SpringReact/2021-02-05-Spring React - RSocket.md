---
title:  "Spring React - RSocket!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-reative
---

## 개요  

> <https://rsocket.io/>  
> <https://www.youtube.com/watch?v=ipVfRdl5SP0&t=592s>

`RSocket` 은 **네트워크 프로토콜** 로서 `OSI Layer 5/6` 또는 `TCP/IP Application Layer` 프로토콜이다.  
리액티브 애플리케이션 간 통신을 위해 디자인 되었다.  

`Back-Pressure` 같은 `Reactive Stream` 개념을 넣은 리액티브 프로토콜이라 할 수 있다.  

> 흐름제어에 대해 자세히 알고 싶다면 아래 url 참고  
> <https://rsocket.io/about/protocol>

고전적인 `request-response(1:1)` 구조의 HTTP 방식은 최신 리액티브 프로그래밍에 맞지 않다.  
그래서 HTTP 프로토콜 위에서 동작하는 gRPC(HTTP/1.1 & 2), Websocket 방식이 나왔지만  
HTTP 프로토콜 위에서 동작하다 보니 리액티브 프로그래밍에 적용하기 과한 HTTP 규약들을 지켜야 하는 상황이다.  

이를 해결하기 위해 각종 언어별로 동작하는 폴리글랏 RPC 프로토콜인 `RSocket` 이 개발됐다 할 수 있다.  

통신 모델로는 아래 4가지 종류가 있다.  

1. **fire and forget** 0:1 (no response)
2. **request response** 1:1 (single value in out)
3. **request stream** 1:N(single value in and multi out)
4. **channel** N:N (multi value in out)

### 샘플코드  

일반적인 `request stream` 모델을 사용한 샘플코드 작성  

```groovy
implementation 'org.springframework.boot:spring-boot-starter-rsocket'
```

`RSocket` 에서 주고받을 클래스정의

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GreetingRequest {
    private String message;
}
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GreetingResponse {
    private String message;
}
```

`@MessageMapping("greetings")` 어노테이션으로 엔드포인트 위치를 지정한다.  

```java
@Controller
public class GreetingController {
    @MessageMapping("greetings")
    Flux<GreetingResponse> greet(GreetingRequest request) {
        // this pattern is request stream pattern
        Stream<GreetingResponse> stream = Stream.generate(new Supplier<GreetingResponse>() {
            @Override
            public GreetingResponse get() {
                return new GreetingResponse("hello " + request.getMessage() + "@" + Instant.now() + "!");
            }
        });
        return Flux.fromStream(stream).delayElements(Duration.ofSeconds(1));
    }
}
```

## RSocket Client  

> <https://github.com/making/rsc>  
> using Rsocket connection cli tool test my RSocket Server  

`rsc` 툴을 사용하면 간단하게 `RSocket Server`를 테스트 할 수 있다.  

```
$ brew install making/tap/rsc
$ rsc tcp://localhost:8888 --stream --route greetings --log --debug -d "{\"message\":\"kouzie\"}"
```

![springboot_rsocket2](/assets/springboot/spring-react/springboot-rsocket1.png)  

### RSocket Client In Spring  

`Spring` 에서 `RSocket Client`를 사용하기 위해서 어떤 설정  

스프링에선 `RSocketRequester` 객체를 사용해 `RSocket Server` 와 통신한다.  

```java
@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }
}

@Configuration
public class ClientConfig {

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.tcp("localhost", 8888);
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> client(RSocketRequester client) {
        return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent event) {
                client.route("greetings")
                    .data(new GreetingRequest("kouzie"))
                    .retrieveFlux(GreetingResponse.class)
                    .subscribe(System.out::println);
                    // GreetingResponse(message=hello kouzie@2021-02-04T04:57:56.180648Z!)
                    // GreetingResponse(message=hello kouzie@2021-02-04T04:57:57.203748Z!)
                    // ...
            }
        };
    }
}
```

## 양방향 통신 (Bi Direction)

웹소켓 처럼 `Server` 와 `Client` 간 양방향 통신이 가능하다.  

서버로부터 받은 통신데이터를 어떻게 처리할 건지 `Acceptor` 객체를 생성  

```java
@Slf4j
@Controller
public class AcceptorController {

    @MessageMapping("health")
    Flux<ClientHealthState> healthy() {
        log.info("health invoked");
        Stream<ClientHealthState> stream = Stream.generate(() -> new ClientHealthState(Math.random() > 0.2));
        return Flux.fromStream(stream).delayElements(Duration.ofSeconds(1));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientHealthState {
    private boolean healthy;
}
```

`Acceptor` 역시 `@MessageMapping` 어노테이션 설정으로 경로지정, 어떤 데이터를 받고 반환할지 지정할 수 있다.  

생성한 `Acceptor` 객체를 `SocketAcceptor` 에 저장 후 반환한다.  
여러개의 `Acceptor` 지정이 가능  

그리고 `rSocketRequester` 에 `SocketAcceptor` 의존처리

```java
@Bean
SocketAcceptor socketAcceptor(RSocketStrategies strategies,
                                AcceptorController acceptor) {
    return RSocketMessageHandler.responder(strategies, acceptor);
}

@Bean
RSocketRequester rSocketRequester(RSocketRequester.Builder builder,
                                    SocketAcceptor socketAcceptor) {
    return builder
            .rsocketConnector(connector -> connector.acceptor(socketAcceptor))
            .tcp("localhost", 8888);
}
```

이제 서버 연결후 클라이언트도 단순 서보로 데이터를 전달할 뿐만아니라  
서버로부터 데이터를 받아 처리할 수도 있다.  

다음으로 서버의 `MessageMapping` 컨트롤러 메서드 변경  

```java
@MessageMapping("greetings")
Flux<GreetingResponse> greet(RSocketRequester client, GreetingRequest request) {
    log.info("greetings invoked");
    // this pattern is request stream pattern
    Flux in = client.route("health")
        .retrieveFlux(ClientHealthState.class)
        .filter(clientHealthState -> !clientHealthState.isHealthy())
        .doOnNext(chs -> log.info("not healthy!")); // not healthy! 

    Stream<GreetingResponse> stream = Stream.generate(
        () -> new GreetingResponse("hello " + request.getMessage() + "@" + Instant.now() + "!"));
    Flux out = Flux.fromStream(stream)
        .takeUntilOther(in)
        .delayElements(Duration.ofSeconds(1));
    return out;
}
```

메서드 변수로 `RSocketRequester client` 설정, 의존 객체로 전달받는다.  
`client` 에서 아까 생성한 `Acceptor` 객체에게 `health` 라우팅 경로로 데이터 요청  
`false` 데이터를 발행할 때 까지 `GreetingResponse` 를 발행한다.  


## Security

`Spring security` 에서 제공하는 `UserDetails` 와 `PasswordEncoder`, 

`RSocket Security` 설정을 하기 위해 서버와 클라이언트 모두 추가적으로 3개의 의존성 추가  

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-rsocket'

    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.security:spring-security-rsocket'
    implementation 'org.springframework.security:spring-security-messaging'
}
```

### RSocket Server Security Settings

```java
@Configuration
public class ServerSecurityConfig {

    // default userDetails 객체 설정  
    @Bean
    MapReactiveUserDetailsService authentication() {
        return new MapReactiveUserDetailsService(User
            .withUsername("kouzie")
            .password("{noop}password").roles("USER").build());
    }
    
    // 인증 과정 설정 
    @Bean
    PayloadSocketAcceptorInterceptor authorization(RSocketSecurity rSocketSecurity) {
        return rSocketSecurity
            .authorizePayload(authorizePayloadsSpec -> authorizePayloadsSpec
                .anyExchange()
                .authenticated()) // 모든 요청 인증 절차 필요
            .simpleAuthentication(Customizer.withDefaults()) //
            .build();
    }

    @Bean
    RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
        // 메세지 컨트롤러에서 @AuthenticationPrincipal 를 사용하기 위한 설정
        RSocketMessageHandler rmh = new RSocketMessageHandler();
        rmh.setRSocketStrategies(strategies);
        rmh.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
        return rmh;
    }
}
```

`WebMVC` 에서 `HttpSecurity`, `WebFlux` 에서 `ServerHttpSecurity` 사용한것 처럼  
`RSocket` 에선 `RSocketSecurity` 를 사용해 요청 데이터의 보안처리를 진행한다.  

```java
@MessageMapping("greetings")
Flux<GreetingResponse> greet(RSocketRequester client, @AuthenticationPrincipal UserDetails userDetails) {
    Flux in = client.route("health")
        .retrieveFlux(ClientHealthState.class)
        .filter(clientHealthState -> !clientHealthState.isHealthy())
        .doOnNext(chs -> log.info("not healthy! "));

    Stream<GreetingResponse> stream = Stream.generate(() -> new GreetingResponse("hello " + userDetails.getUsername() + " @ " + Instant.now() + "!"));
    Flux out = Flux.fromStream(stream)
        .takeUntilOther(in)
        .delayElements(Duration.ofSeconds(1));
    return out;
}
```

### RSocket Client Security Settings

클라이언트에선 서버가 원하는 인증정보를 RSocket 데이터안에 설정해서 보내야한다.  

```java
@Configuration
public class ClientConfig {

    private final UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("kouzie", "password");
    private final MimeType mimeType = MimeTypeUtils.parseMimeType(WellKnownMimeType
            .MESSAGE_RSOCKET_AUTHENTICATION
            .getString());

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder,
                                      SocketAcceptor socketAcceptor) {
        return builder
                .setupMetadata(credentials, mimeType)
                .rsocketConnector(connector -> connector.acceptor(socketAcceptor))
                .tcp("localhost", 8888);
    }

    @Bean
    RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
        // AUTHENTICATION_MIME_TYPE 을 사용해 데이터를 인코딩
        return strategies -> strategies.encoder(new SimpleAuthenticationEncoder());
    }
    ...
}
```

`RSocketRequester` 를 사용할 때 `metadata` 메서드를 통해  
`MimeType` 과 인증정보인 `UsernamePasswordMetadata` 가 삽입되어 전송되도록 설정

## 데모코드  

> <https://github.com/Kouzie/spring-reactive-demo>