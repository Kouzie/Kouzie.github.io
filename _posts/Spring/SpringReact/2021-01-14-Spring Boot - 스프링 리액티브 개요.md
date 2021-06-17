---
title:  "Spring Boot - 스프링 리액티브!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - reative
  - spring

toc: true
toc_sticky: true

---

# 개요  

> 참고: https://kouzie.github.io/java//java-리액티브-프로그래밍/  
> https://kouzie.github.io/java//java-리액터/  

# 스프링 코어 for Reactive

스프링 5.0 에서부터 리액티브 코드를 위한 여러가지 클래스들이 수정, 추가되었다.  

## ReactiveAdapter, ReactiveAdapterRegistry

`RxJava, Reactor` 에서 사용하는 발행자 클래스를 `Publihser` 로 변환해주는 `Adapter` 가 `springframework.core` 에 추가되어 사용 가능해졌다.  

아래처럼 `ReactiveAdapter` 를 상속받아 RxJava Maybe 와 Publisher 간의 변환 작업을 해주는 Adapter 를 작성해서 사용하거나  

```java
@Component
public class MaybeReactiveAdapter extends ReactiveAdapter {

    public MaybeReactiveAdapter() {
        /**
         * Descriptor for a reactive type that can produce 0..1 values.
         * @param type the reactive type
         * @param emptySupplier a supplier of an empty-value instance of the reactive type
         */
        super(ReactiveTypeDescriptor.singleOptionalValue(Maybe.class, Maybe::empty),
            maybe -> ((Maybe<?>) maybe).toFlowable(), // Maybe->Publisher
            publisher -> Flowable.fromPublisher(publisher).singleElement()); // Publisher->Maybe
    }
}
```

`ReactiveAdapterRegistry` 를 사용해 싱글턴 `Instance` 변수에 `Adapter` 용 코드를 작성해 필요할때 마다 꺼내어 쓸 수 있다.  

```java
@PostConstruct
public void init() {
    ReactiveAdapterRegistry
        .getSharedInstance()
        .registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(Maybe.class, Maybe::empty),
            maybe -> ((Maybe<?>) maybe).toFlowable(), // Maybe->Publisher
            publisher -> Flowable.fromPublisher(publisher).singleElement()); // Publisher->Maybe
}

...

ReactiveAdapter adapter = ReactiveAdapterRegistry
    .getSharedInstance()
    .getAdapter(Maybe.class);
...
```

## 리액티브 I/O, 코덱

`springframework.core.io` 에 저장된 `DataBuffer`, `DataBufferUtils` 를 사용하면 I/O 작업이 필요한 파일, 네트워크 자원으로 부터 리액티브 스트림 형태로 작업을 처리할 수 있다. `jav.nio.ByteBuffer` 클래스의 형변환 기능을 추가하여 보다 쉽게 사용 가능하다.  

```java
Flux<DataBuffer> reactiveHamlet = DataBufferUtils.read(
    new DefaultResourceLoader().getResource("hamlet.txt"),
    new DefaultDataBufferFactory(),
    1024);
```

`springframework.core.codec` 에 정의된 인터페이스 `Encoder`, `Decoder` 를 사용하면 `Non Blocking` 방식으로 직렬화 데이터를 자바객체, 자바객체를 직렬화 데이터로 변환 가능하다.  

# WebFlux

`Sprinb Boot 2` 에 리액티브 웹서버를 위한 `WebFlux` 모델을 사용할 수 있도록 `spring-boot-starter-webflux` 라는 새로운 패키지를 추가할 수 있게 되었다.  

해당 모듈은 `Reactive Stream Adapter` 위에 구축된며 `Servlet 3.1+ 지원서버(Tomcat, Jetty 등)`, `Netty`, `Undertow` 서버엔진에서 모두 지원한다.  

> 위의 엔진들은 `java 8` 에 추가된 `java NIO` 로 구현되어 Http 요청을 논블럭킹으로 처리한다.  

![springboot_react2](/assets/springboot/springboot_react2.png)  


일반적은 `WebMVC` 모듈도 `Spring 5.0` 에 이르러 `spring-boot-starter-web` `Servlet 3.1` 을지원하면서 일부분은 리액티브 스트림을 지원하게 되었다.  

`ResponseBodyEmitterReturnValueHandler` 클래스가 업그레이드 되면서 `ReactiveTypeHandler` 필드를 사용해 `WebMVC` 의 인프라 구조를 크게 해치지 않고 `컨트롤러 메서드`가 반환하는 `Flux, Mono, Flowable` 등의 `Publisher`(리액티브 스트림)을 처리한다.    

![springboot_react1](/assets/springboot/springboot_react1.png)  

물론 서블릿 API 를 사용하기에 `블록킹/스레드풀` 방식을 사용한다.  

## WebFlux 개요

기존 `WebMVC` 모델 구조는 아래와 같다.  

![springboot_react3](/assets/springboot/springboot_react3.png)  
> `ViewResolver` 는 `Rest` 방식에선 생략된다.  

각종 서블릿 컨테이너(`Tomcat, JBoss` 등) 요청을 서블릿 클래스를 상속한 `DispatcherServlet` 이 스프링 부트 컨트롤러 매핑에 따라 요청을 분배한다.  

그림처럼 기존 `WebMVC` 방식은 `동기/블로킹` 방식으로 동작한다.  

스프링 부트에서 리액티브 방식을 사용하려면 `Servlet 3.1+(Tomcat, Jetty 등)`, `Netty`, `Undertow` 와 같은 서버를 사용해 리액티브하게 구조가 변경되어야 하는데

다행이도 스프링 프로젝트팀이 동일한 어노테이션 기반 프로그래밍 모델을 사용하면서 `비동기/논블록킹` 으로 동작하도록 이미 개발해두었다.  

### WebFlux with Flux  

대략적으로 `WebFlux` 에서 Http Request, Response 어떻게 리액티브로 구현했는지 아래 인터페이스로 확인할 수 있다.  

```java
interface ServerHttpRequest { // Http Request 를 객체로 표현
    Flux<DataBuffer> getBody();
    ...
}
interface ServerHttpResponse { // Http Response 를 객체로 표현
    Mono<Void> writeWith(Publihser<? extends DataBuffer> body);
    ...
}
interface ServerWebExchange { // HTTP Request, Response 컨테이너 역할
    ServerHttpRequest getRequest();
    ServerHttpResponse getResponse();
    Mono<WebSession> getSession();
    ...
}
```

서블릿의 `ServletRequest`, `ServletResponse` 와 연관지어서 새로운 Http 요청, 반환을 객체로 표현할 수 있는 인터페이스들이 정의되어있고  

`DataBuffer` 를 사용해 리액티브 타입과의 결합도를 낮춘다.  

```java
interface WebHandler {
    Mono<Void> handle(ServerWebExchange exchanage);
}
interface WebFilterChain {
    Mono<Void> filter(ServerWebExchange exchanage);
}
interface WebFilter {
    Mono<Void> filter(ServerWebExchange exchanage, WebFilterChain chain);
}
```

`WebHandler` 는 `DispatcherServlet` 역할, 실행결과를 받지 못할 수 있음으로 `handle` 메서드는 `Mono<Void>` 가 반환된다.  
> 클라이언트를 위한 Http 반환은 `exchange` 안의 `ServerHttpResponse` 에  

`WebFilter` 는 서블릿의 요청, 반환 필터처럼 리액티브에서도 비지니스 로직에 집중할 수 있도록 필터기능이 제공된다.  

```java
public interface HttpHandler {
    Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);
}
```

마지막으로 `WebHandler` 로부터 전달받은 `exchange` 객체를 `url` 매핑할 수 있도록 `HttpHandler` 의 `handle` 로 전달된다.  


## WebFlux - Functional Reactive Web Server

`Vert.x` 나 `Ratpack` 과 같은 프레임워크의 인기비결은 스프링의 복잡한 MVC 설정으로 라우팅 설정과 로직이 없이  
간결한 설정으로 라우팅 로직을 작성할 수 있는 API 들이 잘 정의되어 있기 때문이다.  

스프링 프레임워크도 위 프레임워크처럼 간결하게 라우팅 로직을 처리할 수 있는 API 를 개발하였다.  

`spring-boot-starter-webflux` 모듈의 `org.springframework.web.reactive.function.server` 패키지에 정의된 `RouterFunction` 클래스 사용하여 라우팅 로직 정의가 가능하다.  

```java
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootApplication
public class DemoApplication {

    final ServerRedirectHandler serverRedirectHandler = new ServerRedirectHandler();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(OrderHandler orderHandler) {
        return nest(path("/orders"), 
            nest(accept(APPLICATION_JSON),
                route(GET("/{id}"), orderHandler::get)
                    .andRoute(method(HttpMethod.GET), orderHandler::list))
                .andNest(contentType(APPLICATION_JSON),
                    route(POST("/"), orderHandler::create))
                .andNest((serverRequest) -> serverRequest.cookies().containsKey("Redirect-Traffic"),
                    route(all(), serverRedirectHandler))
        );
    }
}
```

`RouterFunction` 을 스프링 `Bean` 으로 등록하고 `/orders` 에 대한 라우팅 로직 설정, 기본적인 `Path, Http method, cookie` 포함여부 등 여러 로직처리 가능하다.  

함수형으로 웹서버를 구축할경우 `Netty` 서버를 같이 사용하면 별도의 스프링 설정없이 서버 실행이 가능하다.  

이런점때문에 단순 테스트의 경우 `@SpringBootApplication` 을 사용하지 않고 단순 `Netty` 서버를 사용해 빠르게 서버를 실행하고 구현한 메서드들을 테스트 할 수 있다.  

만약 패스워드 암호화 및 복호화 테스트를 한다면 서버기능을 하는 객체 외에 추가적으로 필요한 객체는 해시 기능이 있는 `spring-boot-starter-security` 의 `PasswordEncoder` 뿐이다.  
> `PasswordEncoder` 만 사용한다면 `spring-security-core` 만 의존성 처리해도 된다.  

별도의 스프링 관련 어노테이션, Bean 등록과정 없이 `RouterFunction, Netty, PasswordEncoder` 3개 객체만 잘 정의해서 아래처럼 서버 실행이 가능하다.  

```java
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;

public static void main(String... args) {
    long start = System.currentTimeMillis();
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(18); // encoder 생성

    HttpHandler httpHandler = RouterFunctions.toHttpHandler( // RouterFunction -> HttpHandler 변경
        route(POST("/password"), request -> request // RouterFunction 으로 라우터 로직 생성 
            .bodyToMono(PasswordDTO.class)
            .map(p -> passwordEncoder.matches(p.getRaw(), p.getSecured()))
            .flatMap(isMatched -> isMatched
                ? ServerResponse.ok().build()
                : ServerResponse.status(HttpStatus.EXPECTATION_FAILED).build())
        )
    );
    ReactorHttpHandlerAdapter reactorHttpHandler = new ReactorHttpHandlerAdapter(httpHandler); 
    // HandlerAdapter 에 HttpHandler 삽입, BiFunction 를 구현한 클래스임  
    DisposableServer server = HttpServer.create() // Netty Server
        .host("localhost").port(8080)
        .handle(reactorHttpHandler) // BiFunction<HttpServerRequest, HttpServerResponse, Mono<Void>> 요구함
        .bindNow(); // 서버 엔진 시작  
        
    LOGGER.debug("Started in " + (System.currentTimeMillis() - start) + " ms"); // Started in 703 ms
    server.onDispose().block(); // main 스레드 차단  
}
```

서버가 0.7 초만에 실행된다.  
스프링 컨테이너, 의존성 주입, 어노테이션 처리를 하지 않음으로 속도가 굉장히 빠르며  
간단한 테스트진행은 위와같은 방식으로 진행하면 편하다.  

## WebFlux - Annotated Controller

`RouterFunctions`를 사용해도 되지만 `WebMVC` 모델에서 사용하는 `@RestController, @RequestMapping` 등의 어노테이션을 `WebFlux` 에서도 사용할 수 있다.  

```java
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping
    public Flux<Member> getAll() {
        return memberRepository.findAll();
    }

    @GetMapping("/id/{id}")
    public Mono<Member> getById(@PathVariable Long id) {
        return memberRepository.findById(id);
    }
}
```

`Flux` 는 배열, `Mono` 는 객체로 반환된다.  


## WebFlux - Filter

더이상 `javax.servlet.Filter` 을 사용하지 못한다.  

필터기능을 하는 방법은 여러가지다.  

### RouterFunctions 

```java
@SpringBootApplication
public class ReactApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactR2dbcApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> filterFunction(MemberComponent memberComponent) {
        return RouterFunctions
                .route(GET("/member/{memberId}")
                .and(accept(MediaType.APPLICATION_JSON)), memberComponent::getById)
                .filter(new ExampleHandlerFilterFunction());
    }
}

@Component
@RequiredArgsConstructor
class MemberComponent {
    private final MemberRepository memberRepository;
    public Mono<ServerResponse> getById(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
                .body(BodyInserters.fromValue(
                       memberRepository.findById(Long.valueOf(request.pathVariable("memberId")))));
    }
}
```

### WebFilter

```java
@Component
public class ExampleWebFilter implements WebFilter {
  
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, 
      WebFilterChain webFilterChain) {
        serverWebExchange.getResponse().getHeaders().add("web-filter", "web-filter-test");
        return webFilterChain.filter(serverWebExchange);
    }
}
```

별도의 매핑 조건이 없기때문에 조건문 분기가 필요함  

### HandlerFilterFunction

```java
public class ExampleHandlerFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> handlerFunction) {
        if (request.pathVariable("name").equalsIgnoreCase("test")) {
            return ServerResponse.status(HttpStatus.FORBIDDEN).build();
        }
        return handlerFunction.handle(request);
    }
}
```

## WebFlux - Exception Handler

**메서드 레벨**에서 오류처리는 `ServerResponse` 에 status, body 등을 설정하면 쉽게 처리할 수 있다.  
또한 클래스 내부에서 기존 WebMVC 에서 사용하던 `@ExceptionHandler` 를 사용해 처리할 수 있다.  

```java
@Controller public class SimpleController {
    @ExceptionHandler public ResponseEntity<String> handle(IOException ex) {
        // ... 
    }
}
```

**글로벌 레벨**에서 오류처리는 `WebExceptionHandler` 인터페이스를 구현해 필터 방식으로 처리할 수 있다.  

### DefaultErrorAttributes

`DefaultErrorAttributes` 는 `WebFlux` 에서 사용하는 에러 핸들러로 기본 필터로 등록되어 있는 에러 핸들러가 `DefaultErrorAttributes` 안의 `getErrorAttributes` 를 호출해 아래와 같은 반환값을 반환한다.  

```json
{
"timestamp": "...",
"path": "...",
"status": 405,
"error": "Method Not Allowed",
"message": "",
"requestId": "ca35d584-1"
}
```

글로벌 레벨에서 오류처리를 통해 커스텀한 반환값을 설정하고 싶으면 `DefaultErrorAttributes` 의 `getErrorAttributes` 메서드를 오버라이딩 해야한다.  

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    // private final MessageSource messageSource;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable throwable = getError(request);
        String acceptLanguage = request.headers().firstHeader("accept-language");
        Locale locale = acceptLanguage != null && acceptLanguage.startsWith("ko") ? Locale.KOREA : Locale.getDefault();
        log.error("unknown server error:{}, {}" + throwable.getClass().getCanonicalName(), throwable.getMessage());
        // throwable 의 종류에 맞게 반환값을 조정
        Map<String, Object> map = new HashMap<>();
        map.put("code", UNKNOWN_ERROR_CODE);
        map.put("error", UNKNOWN_ERROR_TYPE);
        // map.put("description", messageSource.getMessage("fms.error.unknown_server_error", null, locale));
        return map;
    }
}
```

에러 반환시 아래처럼 출력되도록 설정    

```json
{
"code": "500-00",
"error": "UnknownServerError"
}
```

이제 핸들러에서 `DefaultErrorAttributes` 의 `getErrorAttributes` 가 아닌  
직접 정의한 `GlobalErrorAttributes` 의 `getErrorAttributes` 가 호출되도록 설정하면 된다.  


### AbstractErrorWebExceptionHandler

`AbstractErrorWebExceptionHandler` 는 에러발생시 필터로 등록되어 있는 핸들러  
해당 핸들러보다 더 높은 우선순위를 가진 핸들러로 에러처리하도록 설정  

```java
@Order(-2) // 기본 에러처리는 -1, 보다 빠르게 설정한다.
@Component
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {
    // constructors
    public GlobalErrorWebExceptionHandler(
            GlobalErrorAttributes errorAttributes, WebProperties.Resources resources,
            ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Integer status = Integer.valueOf(((String) errorPropertiesMap.get("code")).substring(0, 3));
        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
```



## WebFlux - WebSocket  

이미 `spring-boot-starter-websocket` 모듈에서 제공하는 스프링 웹소켓을 통해 논블록킹으로 메세지를 처리할 수 있을 줄 알았지만 내부에서 블로킹 방식으로 동작하며 리액티브 서버 성능에 영향을 끼친다 

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


> `WebSocketMessage` 는 `payload` 로 `DataBuffer` 를 구현하여 문자열, 바이트코드로 쉽게 형변환 가능  
```java
public class WebSocketMessage {
	private final Type type;
	private final DataBuffer payload;
    ...
}
```

`WebSocketHandler` 를 구현하면 해당 url 에 해당하는  `WebSocketSession` 객체를 사용해 메세지를 받고 보낸다.  
웹소켓 테스트 툴을 사용해 `ws://127.0.0.1:8080/ws/echo` 로 접속, 메세지 전송시 `Echo: ...` 메세지 수신 확인    


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


> 참고 코드: https://github.com/Kouzie/spring-reactive/tree/master/spring-reactive-websocket-client  
안타깝게도 `WebFlux` 에서 제공하는 웹소켓 내용은 위의 기능이 전부이며 STOMP 를 사용한 메세지 매핑 등의 기능은 제공하지 않는다.  



### 다중 통신 Sinks.Many 

> 코드 참고: https://github.com/Kouzie/spring-reactive/tree/master/spring-reactive-websocket

리액티브의 웹소켓은 WebMVC 에서 사용한 웹소켓과 다른점이 있는데 
모든 요청에 대한 응답을 수행할 때 위와같이 람다식이나 함수를 미리 등록해두어야 하고  
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
Websocket 외에도 SSE (Server Send Events) 에서도 사용된다.  

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


## WebClient  

논블록킹 `Http Client`로 기존 스프링 부트에서 대표적인 `Http Client` 로 `RestTemplate`(블록킹) 이 있다.  
내부에 `Flux, Mono` 리액터 객체를 지원하는 매핑이 내장되어 있어 리액티브 서버에 잘 어울린다.  

`http://localhost:8080/api/user/{id}` url 을 지원하는 간단한 웹서버 생성  

```java
@Slf4j
public class TestWebServer {
    public static void main(String[] args) {
        HttpHandler httpHandler = RouterFunctions.toHttpHandler( // RouterFunction -> HttpHandler 변경
            nest(path("/api"), route(GET("/users/{id}"),
                request -> {
                    String id = request.pathVariable("id");
                    return ServerResponse.ok().syncBody("hello " + id + " user!"); // 반환데이터 동기적으로 생성
                }) // end route
            ) // end nest
        );
        ReactorHttpHandlerAdapter reactorHttpHandler = new ReactorHttpHandlerAdapter(httpHandler);
        DisposableServer server = HttpServer.create()
            .host("localhost").port(8080)
            .handle(reactorHttpHandler)
            .bindNow();
        server.onDispose().block();
    }
}
```

`WebClient` 를 사용해 위 `url` 에 `Http GET Request` 요청  

```java
public class TestWebClient {
    public static void main(String[] args) throws InterruptedException {
        WebClient.create("http://localhost:8080/api") // WebClient 객체 생성 + baseUrl 설정
            .get().uri("/users/{id}", 10) // method, uri 설정
            .retrieve() // 응답 내용 설정. ResponseSpec 반환
            .bodyToMono(String.class) // 응답 body 를 Mono 로 변환
            .subscribe(s -> System.out.println(s)); // Mono 에 대한 구독 설정
            // hello 10 user!
        Thread.sleep(1000); // main thread 종료 방지 
    }
}
```


위의 `WebClient` 는 `GET` 방식이라 `uri` 만 설정했지만 `API` 에 따라 `cookie, header, body` 모두 설정 가능하다.  

HTTP 응답을 처리할 수 있는 메서드가 `retrieve()` 와 `exchage()` 가 있는데  

`retrieve()` 는 `ResponseSpec` 을 반환하고 
`exchage()` 는 `Mono<ClientResponse>` 를 반환한다.  

만약 `exchange()` 를 사용할 경우 아래와 같이 코드작성  

```java
public static void main(String[] args) throws InterruptedException {
    WebClient.create("http://localhost:8080/api") // WebClient 객체 생성
        .get().uri("/users/{id}", 10) // method, uri 설정
        .exchange() // Mono<ClientResponse> 반환
        .flatMap(response -> response.bodyToMono(String.class)) // 응답 body 를 Mono 로 변환
        .subscribe(s -> System.out.println(s)); // Mono 에 대한 구독 설정
    Thread.sleep(1000);
}
```

`exchage` 를 사용하면 `ClientResponse` 에서 제공하는 `Http Response` 의 각종 정보를 조작할 수 있는 여러 메서드로 복잡한 반환 로직 구성이 가능하다.  
`retrieve` 의 경우 `Http status` 만 겨우 조작하여 `DSL` 형식으로 처리할 수 있다.  

`WebClient` 는 인터페이스이고 `DefaultWebClient` 가 `WebClient` 의 유일한 구현체이다.  
실제 `DefaultWebClient` 내부에선 


### WebClient Serialize config

`WebClient` 에서 직렬화, 비직렬화를 수행할때 기존생성한 `ObjectMapper` 를 통해 처리할 수 잇다.  


```java
@Bean
public ObjectMapper objectMapper() {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    module.addSerializer(LocalDateTime.class, localDateTimeSerializer);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(module);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // UnrecognizedPropertyException 처리
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // json -> clas 에서 unknown 속성이 있어도 처리
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 이 아닌것만 변환
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE); // snake case 로 변환
    return objectMapper;
}

@Bean
public WebClient webClient(ObjectMapper objectMapper) {
    ExchangeStrategies jacksonStrategy = ExchangeStrategies.builder()
            .codecs(config -> {
                config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
            }).build();
    return WebClient.builder().exchangeStrategies(jacksonStrategy).build();
}
```

주의사항으로 `uri(uriBuilder->..)` 메서드를 사용해 `query parameter` 를 지정할 경우 문자열에 `/` 가 들어갈 `escape` 문자로 인식하기 때문에 base64 문자열로 변환할 수 없다, url encoding 을 진행하지 않는다.
또한 `WebClient` 생성시 `baseUrl` 을 설정하지 않으면 `uribuilder` 를 통해 `scheme, host, port, path` 빌드함수를 모두 호출해야 하기 때문에 번거롭다. 

`WebClient` 를 `bean` 으로 생성해 `singleton` 방식으로 사용한다면 `StringBuilder` 를 통해 `uri` 를 직접생성하는 것을 권장.

```java
StringBuilder urlBuilder = new StringBuilder(WEATHER_GET_ULTRA_SRT_NCST); /*URL*/
urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + URLEncoder.encode(dataGovApiKey, "UTF-8")); /*공공데이터포털에서 받은 인증키*/
urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8")); /*요청자료형식(XML/JSON)Default: XML*/
urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE), "UTF-8")); /*15년 12월 1일발표*/
urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(String.format("%02d00", curHour), "UTF-8")); /*05시 발표*/
urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nx), "UTF-8")); /*예보지점 X 좌표값*/
urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(ny), "UTF-8")); /*예보지점의 Y 좌표값*/
URI uri = new URL(urlBuilder.toString()).toURI();
```

## SSE(Server-Sent Event)

> 참고: https://www.youtube.com/watch?v=4HlNv1qpZFY&t=1283s  
평범한 `HTTP, Websocket, SSE 프로토콜`의 시퀀스 비교이다.  
![springboot_websocket2](/assets/springboot/springboot_websocket2.png)  

서버 단방향 통신이라 웹소켓이 비해 속도나 오버헤드 측면에서 SSE 가 효율적이지만 양방향이 안되는 이유로 웹소켓이 주로 사용된다.  

`WebFlux` 를 사용하지 않고 `spring-boot-starter-web` 에서 SSE 프로토콜 사용이 가능하다.  


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

`ScheduledExecutorService` 와 `ApplicationEventPublisher` 클래스를 사용해 `Temperature` 데이터를 계속 발행한다.  
`@EventListener` 를 사용해 `ApplicationEventPublisher` 에 발행된 데이터를 받을 수 있다.  


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
`SseEmitter` 를 지속적으로 유지, 관리한다. 반환 타입이 `ResponseEntity, Map` 과 같은 객체가 아닌 `SseEmitter` 인 것이 어색하다.  

`WebFlux` 에선 `Flux` 와 `Spring 5.0` 에 추가된 `ServerSentEvent` 를 사용해 SSE 프로토콜을 지원한다.  

```java
@RestController
@RequiredArgsConstructor
public class ServerSentController {

    private final StocksService stocksService;

    @GetMapping("/sse/stocks")
    public Flux<ServerSentEvent<?>> streamStocks() {
        return stocksService.stream() // Flux<StockItem> 반환
                .map(item -> ServerSentEvent
                        .builder(item)
                        .event("StockItem")
                        .id(item.getId())
                        .build());
    }

    @GetMapping("/sse/stocks2")
    public Flux<StockItem> streamStocks2() {
        return stocksService.stream();
    }
}
```

`WebFlux` 내부에서 발행원소를 `ServerSentEvent` 로 래핑 하기에 단순 `Flux<>` 만 컨트롤러 메서드에서 반환해도 된다.   


# 스프링 시큐리티 with WebFlux

기존 서블릿 기반 스프링 부트는 하나의 스레드에 하나의 연결이 처리되어  
`ThreadLocal` 에 `SecurityContext` 를 저장해 연결동안 보안처리를 진행했지만  

리액티브는 하나의 연결에 여러개의 스레드가 꼬여있을 수 있어 `Reactor Context` 를 사용해야 한다.  
`spring-boot-starter-security` 모듈 역시 기존 서블릿 기반 `WebMVC` 에서 `WebFlux` 를 지원할 수 있도록 업데이트 되었다.  

## ReactiveSecurityContextHolder  

`WebMVC` 에선 `SecurityContextHolder` 에서 `SecurityContext` 를 가져왔다면  
`WebFlux` 에선 `ReactiveSecurityContextHolder` 에서 `SecurityContext` 를 가져온다.  

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SecuredProfileController {

    private final ProfileService profileService;

    @GetMapping("/profiles")
    public Mono<Profile> getProfile() {
        return ReactiveSecurityContextHolder.getContext() // Mono<SecurityContext> 반환
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> profileService.getByUser(auth.getName()));
    }
}
```

로그인한 유저의 정보를 `SecurityContext` 가져와 `Profile` 에서 검색 후 출력한다.   

당연히 `SecurityContext::getAuthentication` 메서드는 `리액터 컨텍스트` 를 사용한다.  

```java
// ReactiveSecurityContextHolder.java

public static Mono<SecurityContext> getContext() {
    return Mono.subscriberContext()
        .filter( c -> c.hasKey(SECURITY_CONTEXT_KEY))
        .flatMap( c-> c.<Mono<SecurityContext>>get(SECURITY_CONTEXT_KEY));
}
```

## SecurityWebFilterChain

인증과정을 거치려면 내부 컨텍스트에 엑세스 하려고 하면 `SecurityContext` 가 해당 `리액터 컨텍스트` 에 존재해야하고 `Authentication` 객체가 `SecurityContext` 안에 할당되어야 한다.  

이 모든 과정을 `ReactorContextWebFilter` 에 `SecurityWebFilterChain` 적용하고 이를 통해 `리액터 컨텍스트` 안에 `SecurityContext` 객체와 `Authentication` 객체를 집어넣는다.  

`SecurityContext` 를 집어 넣는 함수는 `ServerSecurityContextRepository` 를 사용한다.  

```java
package org.springframework.security.web.server.context;

public interface ServerSecurityContextRepository {
	Mono<Void> save(ServerWebExchange exchange, SecurityContext context);
	Mono<SecurityContext> load(ServerWebExchange exchange);
}
```

`SecurityContext` 를 특정 `ServerWebExchange`에 저장, 사용할 수 있다.  



```java
@Configuration
@EnableReactiveMethodSecurity // 별도의 MethodInterceptor 사용시 필요함
public class SecurityConfiguration {

    @Bean // ReactorContextWebFilter 에 적용할 시큐리티 필터
    public SecurityWebFilterChain securityFilterChainConfigurer(ServerHttpSecurity httpSecurity) {
        // 기존 mvc 에서 사용하던 HttpSecurity 에서 webflux 용으로 정의된 ServerHttpSecurity
        // 기존 spring security 사용 방식과 크게 다르지 않다.  
        return httpSecurity
            .authorizeExchange()
            .anyExchange().permitAll().and()
            .httpBasic().and()
            .formLogin().and()
            .build();
    }

    private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");
    private static final String NOOP_PASSWORD_PREFIX = "{noop}";

    @Bean // 로그인에 사용할 테스트 사용자 생성
    public MapReactiveUserDetailsService reactiveUserDetailsService(ObjectProvider<PasswordEncoder> passwordEncoder) {
        return new MapReactiveUserDetailsService(
            User.withUsername("user")
                .password("user")
                .passwordEncoder(p -> getOrDeducePassword(p, passwordEncoder.getIfAvailable()))
                .roles("USER")
                .build(),
            User.withUsername("admin")
                .password("admin")
                .passwordEncoder(p -> getOrDeducePassword(p, passwordEncoder.getIfAvailable()))
                .roles("USER", "ADMIN")
                .build());
    }

    private String getOrDeducePassword(String password, PasswordEncoder encoder) {
        if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
            return password;
        }
        return NOOP_PASSWORD_PREFIX + password;
    }
}
```

## WebFlux with JWT

### Custom SecurityContextRepository, AuthenticationManager

스프링 시큐리티는 `SecurityContextRepository` 를 통해 `SecurityContext` 를 `리액터 컨텍스트`에 저장하고 삭제한다.  

default 스프링 시큐리티의 경우 DB 로부터 UserDetails 를 가져와 등록해두고 사용하겠지만 우리는 JWT 를 사용하기에 `ServerSecurityContextRepository` 상속받아 커스터마이징 해야한다.  

```java
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authToken != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return authenticationManager
                    .authenticate(auth)
                    .map(authentication -> new SecurityContextImpl(authentication));
        } else {
            return Mono.empty();
        }
    }
}
```

`SecurityContext` 를 생성 및 저장하기 request 헤더로 부터 JWT 토큰을 가져와 `AuthenticationManager`로 넘기는 코드가 `load` 에 정의되어 있다.  

```java
@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        if (!jwtUtil.validateToken(authToken)) {
            return Mono.empty();
        }
        Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
        String role = claims.get("role", String.class);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        return Mono.just(new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
    }
}
```

`AuthenticationManager` 는 전달받은 토큰으로 `role` 을 꺼내어 `Authority` 를 지정하고 `Authentication` 인증 객체를 반환한다.  

`SecurityContextPath` 가 리액터 컨텍스트로 변경되었을 뿐 기존 `WebMVC` 모델의 스프링 시큐리티 방식과 비슷하다.  
```java
@Bean // ReactorContextWebFilter 에 적용할 시큐리티 필터
public SecurityWebFilterChain securityFilterChainConfigurer(ServerHttpSecurity httpSecurity) {
    // 기존 mvc 에서 사용하던 HttpSecurity 에서 webflux 용으로 정의된 ServerHttpSecurity
    // 기존 spring security 사용 방식과 크게 다르지 않다.
    return httpSecurity
        .exceptionHandling()
        //.authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> // 미 로그인
        //        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
        //.accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> // 미 로그인
        //        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
        .authenticationEntryPoint((swd, e) -> Mono.error(new AuthenticationCredentialsNotFoundException("")))
        .accessDeniedHandler((swe, e) -> Mono.error(new AccessDeniedException("")))
        .and()
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .csrf().disable()
        .cors().disable()
        .httpBasic().disable() // Basic Authentication disable
        .formLogin().disable()
        .authorizeExchange()
        .pathMatchers("/member/join", "/member/login").permitAll()
        .pathMatchers("/member/**", "/rent/**").authenticated()
        .pathMatchers("/admin/**").hasAnyRole("ROLE_ADMIN")
        .anyExchange().permitAll()
        .and()
        .build();
}
```

마지막으로 `ServerHttpSecurity` 에 커스터마이징한 `SecurityContextRepository, AuthenticationManager` 를 등록하고 별도의 에러 핸들링 처리를 한다면  에러를 반환해 핸들링,  
없다면 `HttpStatus.UNAUTHORIZED` 로 단순 `HTTP Status` 만 반환할 수 있다.  

# 스프링 데이터 with WebFlux

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

## 스프링 데이터 몽고DB 리액티브  

`NoSQL` 의 경우 각 벤더사에서 통합된 규약이 없다.  
각 벤더사에서 자기들만의 드라이버 라이브러리를 제공하고 스프링 데이터 팀은 스프링에서 해당 라이브러리들을 쉽게 사용할 수 있도록 각종 모듈을 개발하고 있다

`NoSQL DB` 는 최근에 만들어 져서 대부분 벤더사가 `리액티브 드라이버` 를 제공하고 있으며  
스프링 데이터 팀은 몽고DB 에서 제공하는 `리액티브 드라이버` 를 쉽고 편하게 사용할 수 있도록 `spring-boot-starter-data-mongodb-reactive` 모듈을 작성해두었다.  

해당 모듈을 사용하면 스프링 팀에서 만든 `Repository` 패턴을 사용해 메서드명 기반으로 쿼리문이 자동 생성/사용 할 수 있다.   


### ReactiveMongoRepository

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

### ReactiveMongoTemplate  

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

### MongoClient


몽고DB 에서 제공하는 리액티브 드라이버 구현체가 `com.mongodb.reactivestreams.client.MongoClient` 클래스이다.  

> https://mongodb.github.io/mongo-java-driver-reactivestreams/  

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

### 트랜잭션(ReactiveMongoTemplate.inTransaction)

`MongoDB 4.0` 버전 이전까지 하나의 문서 에 대해서만 트랜잭션을 제공하는 `Single-Document Transaction` 기능만 있었다.  
하나의 문서에 모든 정보를 삽입하여 사용하기에 하나의 트랜잭션은 하나의 문서만 건들여서 `Single-Document Transaction` 으로도 충분해야 하지만 항상 예외가 있는법,  
결국 여러 문서에 대한 트랜잭션 `Multi-Document Transaction` 기능을 `MongoDB 4.0` 부터 지원한다.   

> WiredTiger 스토리지 엔진의 샤딩설정이 되어 있지 않고 복제설정일 경우에만 `Multi-Document Transaction` 을 지원한다.  

`ReactiveMongoTemplate` 의 `inTransaction` 메서드를 사용하면 

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


## 스프링 데이터 R2DBC

> R2DBC: Reactive Relational Database Connectivity

> https://r2dbc.io/
> https://spring.io/projects/spring-data-r2dbc
> https://spring.io/projects/spring-data-r2dbc

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

지금까지 `스프링 JDBC` 혹은 `스프링 데이터 JDBC` 혹은 `JPA` 를 사용해 생성된 `Hikari CP` 안의 연결객체가 `JDBC` 드라이버를 사용해 관계형 DB 를 사용해 왔다.  

```java
@Repository
public interface BookSpringDataJdbcRepository extends CrudRepository<Book, Integer> {
    
    @Query("SELECT * FROM book b WHERE b.title = :title")
    CompletableFuture<Book> findBookByTitleAsync(@Param("title") String title);

}
```

`JDBC, JPA` 등의 `관계형 DB 라이브러리`들은 모두 `동기/블럭킹` 방식으로 동작한다.  

다행이도 `spring data jdbc` 를 개발한 `스프링 데이터 Relational` 프로젝트 팀에서  
**리액티브에 적합**한 `자바 DB 드라이버`인 `리액티브 드라이버`를 개발중이다.  
이 `리액티브 드라이버` 를 사용한 프로젝트가 `R2DBC` 프로젝트이다.  

더이상 `JDBC` 를 사용하지 않고 리액티브 스택에 적합한 `리액티브 드라이버`를 사용해 DB 에 접근, 데이터를 조작한다.  

> 안타깝게도 `JPA` 는 기존 코드가 너무 복잡했는지 리액티브 지원을 하지 않을것으로 보인다.  


### ReactiveCrudRepository

```java
@Repository
public interface MemberRepository extends ReactiveCrudRepository<Member, Long> {
    Mono<Member> findByName(String name);

    Mono<Member> findByUserName(String name);

    @Query("SELECT * FROM member WHERE name = :name AND user_name = :userName")
    Mono<Member> findByNameAndUserName(String name, String userName);
}
```

### R2dbcEntityTemplate

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


## 스프링 데이터 Redis

`spring-boot-starter-data-redis-reactive` 모듈을 사용 `ReactiveRedisTemplate` 클래스가 `Redis` 커넥션의 핵심클래스이다.  

> 다른 스프링 데이터 프로젝트와 달리 `Repository` 가 존재하지 않음  
일반적인 데이터 관리 외에도 구독/발행 구조의 메시지 기능도 지원한다.  

> `spring-boot-starter-data-redis-reactive` 모듈은 내부적으로 `Lettuce` 라이브러리를 사용한다.

> https://lettuce.io/, 현재 non blokcing 을 지원하는 redis 라이브러리가 Lettuce 가 유일하다.  
또한 `Lettuce` 라이브러리 내에서 `Reactor` 라이브러리를 사용한다.  

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
# 스프링 세션 리액티브  

`spring-session-data-redis`  
`spring-boot-stater-webflux`  
`spring-boot-starter-data-redis-reactive`  

위 3개 의존성을 사용하면 `redis + session + webflux(reactive)` 형식으로 세션관리가 가능하다.  

`spring-session-data-redis` 에서 `Mono` 타입으로 세션에 접근할 수 있도록 `ReactiveSessionRepository` 를 제공한다.  

# 스프링 테스트 리액티브  

 -->

# 기타  

## ListenableFuture

스프링에서 제공하는 `Future` 구현 클래스  

```java
public interface ListenableFuture<T> extends Future<T> {
	void addCallback(ListenableFutureCallback<? super T> callback);
	void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback);
	default CompletableFuture<T> completable() {
		CompletableFuture<T> completable = new DelegatingCompletableFuture<>(this);
		addCallback(completable::complete, completable::completeExceptionally);
		return completable;
	}
}
```

위의 `SseEmitter` 와 마찬가지로 스프링 리액티브의 반환데이터로 사용된다.  

```java
// 비동기 RestTemplate
AsyncRestTemplate httpClient = new AsyncRestTemplate();

@GetMapping
public ListenableFuture<?> requestData() {
    AsyncDatabaseClient databaseClient = new FakeAsyncDatabaseClient();
    // /hello 의 호출결과를 CompletableFuture 으로 반환하는 어뎁터
    CompletionStage<String> completionStage = AsyncAdapters.toCompletion(httpClient.execute(
        "http://localhost:8080/hello",
        HttpMethod.GET, null,
        new HttpMessageConverterExtractor<>(String.class, messageConverters) // http 의 body 부분 컨버터들 지정
    ));
    // CompletionStage(CompletableFuture 의 인터페이스) 를 ListenableFuture 로 변환
    return AsyncAdapters.toListenable(databaseClient.store(completionStage));
}
```

`AsyncRestTemplate.execute` 가 반환하는 `ListenableFuture` 를 `CompletionStage` 로 변환
반환된 `CompletionStage` 를 데이터베이스에 저장후 다시 반환된 `CompletionStage` 를 `ListenableFuture` 로 변환한다.  

일반적으로 간단한 비동기 처리는 `Future` 를 구현한 `CompletableFuture`(`CompletionStage` 구현체) 를 주로 사용한다.  

메서드 정의시 반환값을 스프링에서 기본 제공하는 `ListenableFuture` 를 구현하는 객체로 반환하기 힘들기에  
위와같은 `AsyncAdapters` 를 사용해 비동기 결과를 `ListenableFuture` 로 변환해주는 어뎁터를 사용하는 것이 편하다.  

```java
public final class AsyncAdapters {
    // ListenableFuture -> completionStage
    public static <T> CompletionStage<T> toCompletion(ListenableFuture<T> future) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.addCallback(completableFuture::complete, completableFuture::completeExceptionally);
        return completableFuture;
    }
    // completionStage -> ListenableFuture
    public static <T> ListenableFuture<T> toListenable(CompletionStage<T> stage) {
        SettableListenableFuture<T> future = new SettableListenableFuture<>();
        stage.whenComplete((v, t) -> {
            if (t == null) future.set(v);
            else future.setException(t);
        });
        return future;
    }
}
```

## AsyncRestTemplate  

스프링 리액티브에선 동기방식인 일반 `RestTemplate` 을 사용하지 않고 `AsyncRestTemplate` 를 사용한다.  
흔히사용하는 `execute` 메서드의 반환값이 `ListenableFuture` 객체이다. 

```java
@Override
public <T> ListenableFuture<T> execute(String url, HttpMethod method, AsyncRequestCallback requestCallback,
        ResponseExtractor<T> responseExtractor, Object... uriVariables) throws RestClientException {

    URI expanded = getUriTemplateHandler().expand(url, uriVariables);
    return doExecute(expanded, method, requestCallback, responseExtractor);
}
```
