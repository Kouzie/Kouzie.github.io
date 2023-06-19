---
title:  "Spring React - Message!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## classes: wide

categories:
  - spring-reative
---


## SSE(Server-Sent Event)

> 참고: <https://www.youtube.com/watch?v=4HlNv1qpZFY&t=1283s>  

평범한 `HTTP, Websocket, SSE 프로토콜`의 시퀀스 비교이다.  
`HTTP 1.1` 이 배포되고 `TCP Session` 을 종료할 필요가 없어지자 `SSE` 나 `Websocket` 처럼 지속적으로 데이터 스트림 전송하는 프로토콜이 생겨났다.  

![springboot_websocket2](/assets/springboot/springboot_websocket2.png)  

`Websocket` 의 편의성에 밀려 `SSE` 는 자주 사용되지 않지만 효율성 측면에선 SSE 가 더 뛰어나다.  

### WebMVC with SSE

`spring-boot-starter-web` 에서도 `SSE` 사용이 가능하다.  

`ApplicationEventPublisher` 클래스를 사용해 1초마다 `Temperature` 데이터 발행.  

```java
@Component
@RequiredArgsConstructor
public class TemperatureSensor {
   // 스프링 제공 이벤트 발행 구독 지원 클래스
   private final ApplicationEventPublisher publisher;
   private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
   private final Random rnd = new Random();

   @PostConstruct
   public void startProcessing() {
      this.executor.schedule(this::probe, 1, SECONDS); // 데이터 발행 시작 
   }

   private void probe() {
      double temperature = 16 + rnd.nextGaussian() * 10;
      publisher.publishEvent(new Temperature(temperature)); // 이벤트 데이터 발행 
      executor.schedule(this::probe, rnd.nextInt(5000), MILLISECONDS); // 5초후 재발행
   }
}
```

`@EventListener` 를 사용해 `ApplicationEventPublisher` 에 발행된 데이터를 받을 수 있다.  

`HTTP Response` 객체를 `SseEmitter` 로 설정하고 서버 내부에서도 지속적으로 유지, 관리한다.  

```java
@Slf4j
@RestController
public class TemperatureController {
    static final long SSE_SESSION_TIMEOUT = 5 * 1000L;
    // 연결 클라이언트 관리 list
    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    // TemperatureSensor 의 ApplicationEventPublisher 에서 발행되는 데이터 대응
    @Async
    @EventListener
    public void handleMessage(Temperature temperature) {
        log.info(format("Temperature: %4.2f C, active subscribers: %d", temperature.getValue(), clients.size()));
        // 관리되는 클라이언트에게 발행된 Temperature 데이터 전달 및 예외발생 클라이언트 삭제처리
        List<SseEmitter> deadEmitters = new ArrayList<>();
        clients.forEach(emitter -> {
            try {
                Instant start = Instant.now();
                emitter.send(temperature, MediaType.APPLICATION_JSON);
                log.info("Sent to client, took: {}", Duration.between(start, Instant.now()));
            } catch (Exception ignore) {
                deadEmitters.add(emitter);
            }
        });
        clients.removeAll(deadEmitters); 
    }

    @RequestMapping(value = "/temperature-stream", method = RequestMethod.GET)
    public SseEmitter events(HttpServletRequest request) {
        log.info("SSE stream opened for client: " + request.getRemoteAddr());
        SseEmitter emitter = new SseEmitter(SSE_SESSION_TIMEOUT); // 5 초간 연결
        clients.add(emitter); // 관리 emitter 목록에 추가

        // Remove SseEmitter from active clients on error or client disconnect
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onCompletion(() -> clients.remove(emitter));

        return emitter;
    }

    // 위에 설정된 5초가 지나면 AsyncRequestTimeoutException 이 발생하고 호출됨.  
    @ExceptionHandler(value = AsyncRequestTimeoutException.class)
    public ModelAndView handleTimeout(HttpServletResponse rsp) throws IOException {
        log.warn("handle timeout");
        if (!rsp.isCommitted()) {
            rsp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return new ModelAndView();
    }
}
```

### WebFlux With SSE

`WebFlux` 에선 `Flux`, `ServerSentEvent` 를 사용해 `SSE` 를 지원한다.  

```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class ServerSentController {

    private static Sinks.Many<String> many = Sinks.many().multicast().directAllOrNothing();

    @GetMapping(path = "/sse/time", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> timeStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> "Flux - " + LocalTime.now());
    }
    
    @GetMapping("/sse/interval")
    public Flux<ServerSentEvent<String>> intervalStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(i -> ServerSentEvent.builder("data " + i).build());
    }

    @GetMapping(path = "/sse/many", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tempStream() {
        return many.asFlux();
    }

    @PostMapping("/add/message/{message}")
    public void addMessage(@PathVariable String message) {
        many.tryEmitNext(message);
    }
}
```

## Sinks

> <https://projectreactor.io/docs/core/snapshot/api/reactor/core/publisher/Sinks.html>

`Reactive Streams` 신호를 **프로그래밍 방식으로 푸시**할 수 있는 `constructs` 라고 한다.  
`Websocket`, `SSE` 방식 모두 클라이언트에게 메세지를 푸시해야 하다 보니 WebFlux 에서 필수로 사용한다.  

아래 4가지 명령으로 생성 가능하다.  

- **empty()**  
  `terminal signal` 이라 부르는 complete 신호, error 신호 한개를 반환할 때 사용  
- **many()**  
  여러개 `data signal` 을 `broadcast`, `unicast` 할 때 사용  
- **one()**  
  단 한건의 `data signal` 을 전송  
- **unsafe()**  
  thread safe 하지 않은 `Reactive Streams` 신호 생성, `unsafe` 한 `[empty, many, one]` `Reactive Streams` 신호 생성 가능  
  동시접근 검증을 하지 않기 때문에 오버헤드가 적은 `Sinks` 생성이 가능하다.  

### Sinks.ManySpec

> <https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Sinks.ManySpec.html>

대부분 `Reactive Streams` 신호를 통해 다수의 데이터를 전달함으로 `Sinks.many()` 함수를 많이 사용한다.  

`many()` 함수는 `subscriber` 에 대해 `[multicast, replay, unicast]` 처리를 진행한다.  

- **multicast()**
  `Sinks.MulticastSpec` 반환, 모든 `Subscriber` 에게 데이터 전달  
- **unicast()**  
  `Sinks.UnicastSpec` 반환, 하나의 `Subscriber` 에게 데이터 전달  
- **replay()**  
  `Sinks.MulticastReplaySpec`모든 `Subscriber` 에게 기존에 저장된 데이터까지 모두 전달  

`Sinks.MulticastSpec` 아래 메서드를 통해 `Sink.Many` 객체를 반환한다.  

- **onBackpressureBuffer([int bufferSize], [bool autoCancel])**
  `bufferSize` 만큼 데이터를 저장해두는 웜업과정이 있다. `subscribe` 가 없더라도 향후에 데이터를 수신받을 수 있다.  
  `autoCancel` 이 설정되어 있다면 마지막 subscriber 가 연결 해제되는 순간 `Reactive Streams` 이 닫힌다.  
- **directAllOrNothing()**
  웜업과정이 없다. 모든사용자에게 보낼수 있는 상태일 때만 발생, 그렇지 않을경우 보내지 않는다.  
  그래서 각 `subscriber` 속도에 따라 일부 데이터는 유실될 수 있다.  
- **directBestEffort()**  
  웜업과정이 없다. 처리할 수 있는 가장 빠른 사용자에게 일단 데이터를 보낸다.


## WebSocket with WebFlux

`spring-boot-starter-websocket` 모듈에서 제공하는 스프링 웹소켓을 통해 논블록킹으로 메세지를 처리할 수 있을 줄 알았지만 내부에서 블로킹 방식으로 동작하며 리액티브 서버 성능에 영향을 끼친다  

`WebFlux` 에선 비동기/논블록킹 방식의 웹소켓 처리를 위해 `org.springframework.web.reactive.socket` 패키지를 제공한다.  

> 패키지에 `reactive` 가 붙을뿐 클래스명이나 사용방법이 유사하다.  

웹소켓 서버, 웹소켓 클라이언트 모두 제공한다.  

웹소켓 서버는 `WebSocketHandler` 를 사용해 소켓 핸들러 역할을 하는 객체인 `Handler` 를 등록한다.  

```java
public class EchoWebSocketHandler implements WebSocketHandler {
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session
            .receive() // return Flux<WebSocketMessage> 
            .map(wsMessage -> wsMessage.getPayloadAsText()) // websocket 메세지의 payload (string) 흭득
            .map(tm -> "Echo: " + tm) // 문자열 변환
            .map(tm -> session.textMessage(tm)) // WebsocketSession 을 사용, client 보낼 메세지(payload) 작성
            .as(wsMessage -> session.send(wsMessage)); // client 에게 메세지(payload) 전송
    }
}
```

> `WebSocketMessage` 는 `payload` 로 `DataBuffer` 를 구현하여 문자열, 바이트코드로 쉽게 형변환 가능  

```java
public class WebSocketMessage {
    private final Type type;
    private final DataBuffer payload;
    ...
}
```

웹소켓을 사용하려면 먼저 웹소켓 설정 객체를 `Bean` 으로 등록해야 한다.  
위에서 정의한 핸들러를 `url` 에 매핑하고, Request 요청을 Upgrade 하는 어뎁터를 `Bean` 으로 등록한다.  

```java
@Configuration
public class WebSocketConfiguration {

    @Bean
    public HandlerMapping handlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Collections.singletonMap("/ws/echo", new EchoWebSocketHandler())); // 경로기반 매핑 설정
        mapping.setOrder(-1); // 우선순위, 생략시 사용하지 않는 것으로 설정됨 
        return mapping;
    }
    @Bean
    public HandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(); 
        // WebSocket Handshake (upgrade request) 를 처리하는 HandlerAdapter 생성 
    }
}
```

`WebSocketHandler` 를 구현하면 해당 url 에 해당하는  `WebSocketSession` 객체를 사용해 메세지를 받고 보낸다.  

`ws://127.0.0.1:8080/ws/echo` 로 접속후 메세지 전송시 `Echo: ...` 메세지 수신 확인

### WebSocket Client

웹소켓 클라이언트의 경우 `WebSocketClient` 인터페이스를 구현한 `ReactorNettyWebSocketClient` 를 사용한다.  

```java
import org.springframework.web.reactive.socket.client.WebSocketClient;
...

@Bean
public CommandLineRunner commandLineRunner() {
    return (args) -> {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(URI.create("http://localhost:8080/ws/echo"),
            session -> Flux
                .interval(Duration.ofMillis(100))
                .map(String::valueOf)
                .map(session::textMessage)
                .as(session::send))
            .subscribe();
    };
}
```

정의해둔 `echo` 웹소켓 서버에 0.1 초마다 `interval` 데이터를 문자열로 전송  

`client` 역시 `WebsocketHandler` 구현체를 `excute` 함수의 매개변수로 받으며 핸들러 매핑과 핸들러 어뎁터가 `Bean` 으로 등록되지 않을 뿐 웹소켓 서버 코드와 유사하다.  

### 다중 통신 Sinks.Many  

`WebFlux` 의 `Websocket` 은 모든 요청에 대한 응답을 위와같이 람다식이나 함수를 통해 미리 등록해두어야 한다.  

두개 이상의 클라이언트들간 통신을 위해서 각 클라이언트의 `session` 을 찾아 데이터를 발행하는데 `Sinks.Many` 클래스를 사용한다.  

웹소켓 설정 객체를 `Bean` 으로 등록하고 핸들러 매핑하는 것은 동일하다.  

```java
@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping handlerMapping(ChatSocketHandler chatSocketHandler) { // url, handler 매핑
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(Collections.singletonMap("/ws/chat", chatSocketHandler));
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSocketHandler implements WebSocketHandler { // handler 정의 

    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        WebSocketMessageSubscriber subscriber = new WebSocketMessageSubscriber(session);
        return session.receive()
                .map(this::toDto)
                .doOnNext(subscriber::onNext) // 수신 콜백 함수 등록
                .doOnError(subscriber::onError) // 에러 콜백 함수 등록
                .doOnCancel(subscriber::onCancel) // 연결끊김 콜백 함수 등록  
                .zipWith(session.send(subscriber.getMany().asFlux().map(webSocketToClientDto -> // 메세지 발신 콜백 함수 등록
                        session.textMessage(webSocketToClientDto.getFrom() + ":" + webSocketToClientDto.getMessage()))))
                .then();
    }

    private WebSocketFromClientDto toDto(WebSocketMessage message) {
        try {
            WebSocketFromClientDto WsDto = mapper.readValue(message.getPayloadAsText(), WebSocketFromClientDto.class);
            return WsDto;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

`session.send` 에 등록되는 `Flux` 발행자는 `Sinks.Many` 로부터 생성되는데  
이는 아래에서 사용하는 코드와 같이 메세지를 집어넣을 수 있는 발행자다.  
Websocket 외에도 SSE 에서도 사용된다.  

```java
@Slf4j
class WebSocketMessageSubscriber {
    // 본인을 포함한 다른 클라이언트의 웹소켓 메세지 발행자 맵
    public static Map<String, Sinks.Many<WebSocketToClientDto>> userMap = new HashMap<>(); //sessionId, sink
    private final String id;
    @Getter
    private final Sinks.Many<WebSocketToClientDto> many; // 본인의 웹소켓 메세지 발행자

    public WebSocketMessageSubscriber(WebSocketSession session) {
        many = Sinks.many().unicast().onBackpressureBuffer();
        id = session.getId();
        many.tryEmitNext(WebSocketToClientDto.builder().from("system").message("welcome, " + id).build());
        userMap.put(id, many);
    }

    public void onNext(WebSocketFromClientDto msg) {
        log.info("onNext invoked, to:{}, msg:{}", msg.getTo(), msg.getMessage());
        Sinks.Many<WebSocketToClientDto> to = userMap.get(msg.getTo());
        if (to == null)
            many.tryEmitNext(WebSocketToClientDto.builder().from("system").message("no user:" + msg.getTo()).build());
        else
            to.tryEmitNext(WebSocketToClientDto.builder().from(id).message(msg.getMessage()).build());
    }

    public void onError(Throwable error) {
        //TODO log error
        log.error("onError invoked, error:{}, {}", error.getClass().getSimpleName(), error.getMessage());
        many.tryEmitNext(WebSocketToClientDto.builder()
                .from("system")
                .message(id + " on error, error:" + error.getMessage())
                .build());
    }

    public void onCancel() {
        log.info("onCancel invoked, id:{}", id);
        userMap.remove(id);
        for (Map.Entry<String, Sinks.Many<WebSocketToClientDto>> entry : userMap.entrySet()) {
            if (!entry.getKey().equals(id))
                entry.getValue().tryEmitNext(WebSocketToClientDto.builder()
                        .from("system")
                        .message(id + " is exit")
                        .build());
        }
    }
}

@Getter
@Setter
@Builder
class WebSocketToClientDto {
    private String from;
    private String message;
}

@Getter
@Setter
class WebSocketFromClientDto {
    private String to;
    private String message;
}
```

![springboot_react4](/assets/springboot/springboot_react4.png)  
