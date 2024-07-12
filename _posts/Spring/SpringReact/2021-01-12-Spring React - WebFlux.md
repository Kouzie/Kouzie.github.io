---
title:  "Spring React - WebFlux!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## classes: wide

categories:
  - spring-reative
---

## 스프링 코어 for Reactive

기존 스프링 MVC 에선 tomcat 기반으로 웹서버를 실행해 왔다.  
tomcat 의 경우 기본설정된 thread 개수가 200개 정도인데, 이는 동시요청이 200개가 들어오면 http 요청이 block 된다는 뜻이다.  

최근 웹 어플리케이션은 외부API 요청, DB로부터의 CRUD 가 전부인 경우가 많다, 복잡한 `CPU Bound Job` 보다는 외부의존성과 연결을 통해 `IO Bound Job` 이 더 많다는 뜻이다.  
때문에 `멀티스레드 & blocking` 기반으로 동작하는 `tomcat` 은 요청이 몰렸을 때 외부 의존성(DB, API서버) 에 의해 `blocking` 되어 아무런 동작도 하지 않는상태로 대기중인 경우가 많아진다.  
CPU 사용률을 0% 에 가까워지고 요청이 완료되어 인터럽트가 발생하기만을 기다리게 되버린다.  

이는 파일처리에 대해서도 동일한 문제였기 때문에 1990 년쯤 linux 에서 NIO 기능을 지원하기 시작했고,  
위와같은 문제를 알고있는 개발자들도 프레임워크에 NIO 기능을 넣어 멀티스레드의 문제점을 해결해줬다.  

- 2004 년 netty 가 개발되어 NIO 웹서버를 쓸 수 있게 되었다.  
- 2009 년 Nodejs 역시 NIO 기반으로 동작할 수 있는 프레임워크를 만들어주었다.  
- 2009 년 비동기 서블릿(servlet 3.0) 이 출시했다.  

`Spring WebFlux` 는 NIO 웹서버인 `Netty` 를 기반으로 2017년 `Spring Framework 5.0` 에 처음 도입되었다.  

### ReactiveAdapter, ReactiveAdapterRegistry

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

### 리액티브 I/O, 코덱

`springframework.core.io` 에 저장된 `DataBuffer`, `DataBufferUtils` 를 사용하면 I/O 작업이 필요한 파일, 네트워크 자원으로 부터 리액티브 스트림 형태로 작업을 처리할 수 있다. `jav.nio.ByteBuffer` 클래스의 형변환 기능을 추가하여 보다 쉽게 사용 가능하다.  

```java
Flux<DataBuffer> reactiveHamlet = DataBufferUtils.read(
    new DefaultResourceLoader().getResource("hamlet.txt"),
    new DefaultDataBufferFactory(),
    1024);
```

`springframework.core.codec` 에 정의된 인터페이스 `Encoder`, `Decoder` 를 사용하면 `Non Blocking` 방식으로 직렬화 데이터를 자바객체, 자바객체를 직렬화 데이터로 변환 가능하다.  

## WebFlux

`Sprinb Boot 2` 에 리액티브 웹서버를 위한 `WebFlux` 모델을 사용할 수 있도록 `spring-boot-starter-webflux` 라는 새로운 패키지를 추가할 수 있게 되었다.  

해당 모듈은 `Reactive Stream Adapter` 위에 구축된며 `Servlet 3.1+ 지원서버(Tomcat, Jetty 등)`, `Netty`, `Undertow` 서버엔진에서 모두 지원한다.  

> 위의 엔진들은 `java 8` 에 추가된 `java NIO` 로 구현되어 HTTP 요청을 논블럭킹으로 처리한다.  

![springboot_react2](/assets/springboot/springboot_react2.png)  

리액티브 방식을 사용하려면 `Netty` 와 같은 서버를 사용해야 하는데 서블릿 API 서버를 변경할 수 없다면  
아래 그림과 같이 Spring MVC 를 사용하면서도 리액티브하게 개발할 수 있다.  

물론 서블릿 API 사용하기에 `블록킹/스레드풀` 방식을 사용한다.  

`WebMVC` 모듈도 `Spring 5.0` 에 이르러 `spring-boot-starter-web` 에서 `Servlet 3.1` 을 지원하면서 일부분은 리액티브 스트림을 지원하게 되었고  
`ResponseBodyEmitterReturnValueHandler` 클래스가 업그레이드 되면서 `ReactiveTypeHandler` 필드를 사용해 `WebMVC` 의 인프라 구조를 크게 해치지 않고 `컨트롤러 메서드`가 반환하는 `Flux, Mono, Flowable` 등의 `Publisher`(리액티브 스트림)을 처리한다.  

![springboot_react1](/assets/springboot/springboot_react1.png)  

### WebFlux - Flux  

`WebFlux` 에서 `Request, Response` 어떻게 리액티브로 구현했는지 아래 인터페이스로 확인할 수 있다.  

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

기존 서블릿 객체 `ServletRequest`, `ServletResponse` 처럼 Http Request, Response 을 객체로 표현할 수 있는 인터페이스들이 정의되어있고 `DataBuffer` 를 사용해 리액티브 타입과의 결합도를 낮춘다.  

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

> 클라이언트를 위한 `Http Response` 는 `exchange` 안의 `ServerHttpResponse` 에 저장되어 있다.  

`WebFilter` 는 서블릿의 요청, 반환 필터처럼 리액티브에서도 비지니스 로직에 집중할 수 있도록 필터기능이 제공된다.  

```java
public interface HttpHandler {
    Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response);
}
```

마지막으로 `WebHandler` 로부터 전달받은 `exchange` 객체를 `url` 매핑할 수 있도록 `HttpHandler` 의 `handle` 로 전달된다.  

### WebFlux - Functional Reactive Web Server

`Vert.x` 나 `Ratpack` 과 같은 프레임워크의 인기비결은 스프링의 복잡한 MVC 설정 없이 간결한 설정으로 라우팅 로직을 작성할 수 있는 API 들이 잘 정의되어 있기 때문이다.  

Spring 도 위 프레임워크처럼 간결하게 라우팅 로직을 처리할 수 있는 API 를 개발하였다.  

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

### WebFlux - Annotated Controller

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

### WebFlux - Filter

더이상 서블릿의 `javax.servlet.Filter` 을 사용하지 못한다.  

필터기능을 하는 방법은 여러가지다.  

#### RouterFunctions  

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

#### WebFilter

모든 엔드포인트에서 동작하는 필터 등록

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

#### HandlerFilterFunction

라우터 기반 구현에서만 동작하는 필터 등록

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

### WebFlux - Exception Handler

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

#### DefaultErrorAttributes

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

#### AbstractErrorWebExceptionHandler

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

### WebClient  

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


#### WebClient Serialize config

`WebClient` 에서 직렬화, 비직렬화를 수행할때 기존생성한 `ObjectMapper` 를 통해 처리할 수 잇다.  

```java
@Bean
public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    ...
    return objectMapper;
}

@Bean
public WebClient webClient(ObjectMapper objectMapper) {
    ExchangeStrategies jacksonStrategy = ExchangeStrategies.builder()
            .codecs(config -> {
                config.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                config.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                config.defaultCodecs().maxInMemorySize(1024 * 1024 * 50);
            }).build();
    return WebClient.builder().exchangeStrategies(jacksonStrategy).build();
}
```

주의사항으로 `uri(uriBuilder->..)` 메서드를 사용해 `query parameter` 를 지정할 경우 문자열에 `/` 을 `escape` 문자로 인식하기 때문에 base64 문자열로 변환할 수 없다, url encoding 을 진행하지 않는다.  

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

## 기타  

### ListenableFuture

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

### AsyncRestTemplate  

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

### Hooks.onOperatorDebug

디버깅 하기 힘든 Reactive 환경에서 `Hooks.onOperatorDebug()` 를 호출해서 **리액터의 백트레이싱**을 활성화한다.  

아래와 같이 완벽히 동일한 코드에 `Hooks.onOperatorDebug()` 함수만 호출설정한다.  

```java
public class ReactorDebuggingExample {
    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        Mono<Integer> source;
        if (new Random().nextBoolean()) {
            source = Flux.range(1, 10).elementAt(5);
        } else {
            source = Flux.just(1, 2, 3, 4).elementAt(5); // line 17
        }
        source.subscribeOn(Schedulers.parallel()).block(); // line 19
    }
}

public class ReactorExample {
    public static void main(String[] args) {
        Mono<Integer> source;
        if (new Random().nextBoolean()) {
            source = Flux.range(1, 10).elementAt(5);
        } else {
            source = Flux.just(1, 2, 3, 4).elementAt(5);
        }

        source.subscribeOn(Schedulers.parallel()).block(); // lint 18
    }
}
```

두 코드의 오류 호출문을 확인해보면 둘다 `IndexOutOfBoundsException` 로 시작하지만 안의 내용은 다르다.  
`Hooks.onOperatorDebug()` 를 호출한 오류코드가 Flux 오류 코드 위치를 정확하게 알려준다.  

```
Exception in thread "main" java.lang.IndexOutOfBoundsException
    at reactor.core.publisher.MonoElementAt$ElementAtSubscriber.onComplete(MonoElementAt.java:160)
    Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Assembly trace from producer [reactor.core.publisher.MonoElementAt] :
    reactor.core.publisher.Flux.elementAt(Flux.java:4715)
    com.example.react.practice.ReactorDebuggingExample.main(ReactorDebuggingExample.java:17)
Error has been observed at the following site(s):
    |_   Flux.elementAt ⇢ at com.example.react.practice.ReactorDebuggingExample.main(ReactorDebuggingExample.java:17)
    |_ Mono.subscribeOn ⇢ at com.example.react.practice.ReactorDebuggingExample.main(ReactorDebuggingExample.java:19)
Stack trace:
...
```

```
Exception in thread "main" java.lang.IndexOutOfBoundsException
    at reactor.core.publisher.MonoElementAt$ElementAtSubscriber.onComplete(MonoElementAt.java:160)
    at reactor.core.publisher.FluxArray$ArraySubscription.fastPath(FluxArray.java:177)
    ...
    at java.base/java.lang.Thread.run(Thread.java:829)
    Suppressed: java.lang.Exception: #block terminated with an error
        at reactor.core.publisher.BlockingSingleSubscriber.blockingGet(BlockingSingleSubscriber.java:99)
...
```

### 블록하운드 BlockHound

블로킹 메소드 호출을 찾아낸다. 
애플리케이션을 시작할 때 블록하운드가 바이트코드를 조작 instrument 할 수 있게 된다.

```groovy
dependencies {
    implementation 'io.projectreactor.tools:blockhound:1.0.8.RELEASE'
}
```

```java
@SpringBootApplication
public class ReactApplication {
    public static void main(String[] args) {
        BlockHound.install();
        SpringApplication.run(ReactApplication.class, args);
    }
}
```

### 리액터 스케줄러  

Webflux 에서 가장 조심해야 할 것은 스레드가 블록되는 코드를 피하는 것  
하지만 코드를 작성하다보면 어쩔수 없이 블록킹 메서드를 사용해야 할 때 가 있다.  

예를들어 `AmqpTemplate` 의 `convertAndSend` 메서드 같은것은 TCP 로 메세지를 전송하는, 일종의 블록킹 API 이다.  
물론 `AmqpTemplate` 의 블록킹 시간이 그리 길진 않겠지만 호출이 많아지면 블록킹 시간이 무시못할 수준으로 쌓일 수 있다.  

그렇다고 해서 코드블럭 안에서 메세지를 보내야할 때 `AmqpTemplate` 를 사용하지 않을 순 없는데  
이럴때 사용하는것이 스케줄러를 사용해서 블로킹 API 감싸는 것이다.  

```java
public Mono<Boolean> convertAndSend(String message) {
    Mono<Boolean> mono = Mono.fromCallable(() -> {
        amqpTemplate.convertAndSend(message);
        return true;
    }).subscribeOn(Schedulers.parallel());
    return mono;
}
```

`publishOn` 과 `subsribeOn` 에 스케줄러 설정이 가능하고 `subsribeOn` 이 좀 더 넓은 범위를 가진다.  

`publishOn` 의 경우 해당 코드 이후부터 스케줄러를 사용  
`subscribeOn` 의 경우 플로우 전 단계에 걸쳐 스케줄러를 사용  

억지로 블록킹 API 를 사용하는 것 보다 reactor 용으로 개발된 라이브러리 사용을 권장  

> <https://projectreactor.io/docs/rabbitmq/snapshot/reference/>