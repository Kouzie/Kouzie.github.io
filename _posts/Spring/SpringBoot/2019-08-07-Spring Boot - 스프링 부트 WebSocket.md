---
title:  "Spring Boot - 스프링 부트 WebSocket!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---


## Polling, Long Polling

웹 소켓이 생기기전 서버는 클라이언트에게 어떠한 정보를 알리기 위해 무식한 방법인 `Polling`, `Long Polling` 양방향 통신 기법을 사용하였다.  

`http` 프로토콜로는 절대로 서버가 클라이언트에게 요청할 순 없다.   

이를 해결하기 위해 클라이언트가 서버에게 **n초 주기**로 계속 자신에게 필요한 정보가 있는지 물어보는 요청을 날린다.(`Polling`방식)  

`Long Polling`은 조금더 효율적으로 `n`초 주기가 아닌 일단 보내고 서버가 응답을 반환할때까지 **기다리는 방식**이다.  
일단 보내고 time out될 때까지 무한정 기다린다는 것이다  

> https://kamang-it.tistory.com/entry/Webhttp통신을-이용한-양방향-통신기법-long-polling

`HTML5`등장과 함께 `Websocket`이 등장하였고 위와같은 불편한 `Polling, Long Polling` 통신기법을 사용하지 않게되었다.  

웹소켓을 사용하려면 별도의 HTTP 웹소켓 헨드쉐이크 과정을 거쳐야 한다.  

![springboot_websocket3](/assets/springboot/springboot_websocket3.png)

```
GET ws://localhost:8080/ws HTTP/1.1
Origin: http://example.com
Connection: Upgrade
Host: localhost
Upgrade: websocket
```

위 과정을 거쳐 커넥션이 생성되면 자유롭게 서버에서 클라이언트에게 데이터를 전달할 수 있게되었다.  


## tomcat 웹소캣 예제   

여기선 별도의 라이브러리를 사용하지 않고 단순 `websocket` 만을 사용한다.  

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

```java
@SpringBootApplication
public class WebsockApplication {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    public static void main(String[] args) {
        SpringApplication.run(WebsockApplication.class, args);
    }

}
```

웹 소켓을 사용하기위해 `dependency` 와 기본적인 `Bean` 객체를 등록한다.  

아래와 같이 웹 소켓 연결이 들어올 경우 이를 처리할 메서드 정의  
웹 소켓에 연결된 클라이언트를 유지하기 위해 `javax.websocket.Session` 객체를 사용한다.  

```java
import javax.websocket.*;

@Log
@Component
@ServerEndpoint(value = "/websocket")  //서버가 바인딩된 주소를 뜻함.
public class Socket {
    private Session session;
    public static Set<Socket> listeners = new CopyOnWriteArraySet<>();
    private static int onlineCount = 0;

    @OnOpen //클라이언트가 소켓에 연결되때 마다 호출
    public void onOpen(Session session) {
        onlineCount++;
        this.session = session;
        listeners.add(this);
        log.info("onOpen called, userCount:" + onlineCount);
    }

    @OnClose //클라이언트와 소켓과의 연결이 닫힐때 (끊길떄) 마다 호
    public void onClose(Session session) {
        onlineCount--;
        listeners.remove(this);
        log.info("onClose called, userCount:" + onlineCount);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("onMessage called, message:" + message);
        broadcast(message);
    }

    @OnError //의도치 않은 에러 발생
    public void onError(Session session, Throwable throwable) {
        log.warning("onClose called, error:" + throwable.getMessage());
        listeners.remove(this);
        onlineCount--;
    }

    public static void broadcast(String message) {
        for (Socket listener : listeners) {
            listener.sendMessage(message);
        }
    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.warning("Caught exception while sending message to Session " + this.session.getId() + "error:" + e.getMessage());
        }
    }
}
```

`Socket` 클래스는 사용자가 들어올때 마다 생성되며 `static` 으로 정의된 `Set<Socket> listeners` 객체와 유저수 `int onlineCount` 를 공유한다.  

1. 사용자 소켓 연결 - `@OnOpen` 메서드 호출, `Socket` 인스턴스 생성 후 `listeners`에 삽입, `onlineCount` 1증가  
2. 사용자 메세지 전달 - `@OnMessage` 메서드 호출, `broadcast` 메서드로 `listeners`에 들어있는 모든 `Session` 에 메세지 전달  
3. 사용자 소켓 차단 - `@OnClose` 메서드 호출, `listeners`에서 현재 `Socket` 삭제 `onlineCount` 1감소  
4. 에러 발생 - `@OnError` 메서드 호출 `listeners`에서 현재 `Socket` 삭제 `onlineCount` 1감소  

매우 심플하다.  
간단한 툴로 테스트해보자.  
> `Advanced REST Client` : https://chrome.google.com/webstore/detail/advanced-rest-client/hgmloofddffdnphfgcellkdfbfbjeloo

![springboot_websocket1](/assets/springboot/springboot_websocket1.png)

```
// log 출력물
com.example.websock.component.Socket : onOpen called, userCount:1
com.example.websock.component.Socket : onMessage called, message:hello
com.example.websock.component.Socket : onClose called, userCount:0
```

간단한 테스트용 페이지를 만들어 처리해보자.  

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Your First WebSocket!</title>
</head>
<body>
    <script type="text/javascript">
        var wsUri = "ws://localhost:8080/websocket";
        var websocket;
        var output;
        var textID
        function init() {
            output = document.getElementById("output");
            textID = document.getElementById("textID");
        }
        function connect() {
            if (!websocket) {
                websocket = new WebSocket(wsUri);
                websocket.onopen = function (evt) {
                    onOpen(evt)
                };
                websocket.onmessage = function (evt) {
                    onMessage(evt)
                };
                websocket.onerror = function (evt) {
                    onError(evt)
                };
            }
        }
        
        function disconnect() {
            if (!websocket) websocket.close();
        }
        
        function send_message() {
            var message = textID.value;
            writeToScreen("Message Sent: " + message);
            websocket.send(message);
        }

        function onOpen(evt) {
            writeToScreen("Connected to Endpoint!");
        }

        function onMessage(evt) {
            writeToScreen("Message Received: " + evt.data);
        }

        function onError(evt) {
            writeToScreen('ERROR: ' + evt.data);
        }

        function writeToScreen(message) {
            var pre = document.createElement("p");
            pre.style.wordWrap = "break-word";
            pre.innerHTML = message;
            output.appendChild(pre);
        }
        window.addEventListener("load", init, false);
    </script>
    <h1 style="text-align: center;">Hello World WebSocket Client</h1><br>
    <div style="text-align: center;">
        <form action="">
            <input onclick="connect()" value="Connect" type="button">
            <input onclick="send_message()" value="Send" type="button">
            <input id="textID" name="message" value="Hello WebSocket!" type="text"><br>
        </form>
    </div>
    <div id="output"></div>
</body>
</html>
```

연결해 버든을 누르면 `ws://localhost:8080/websocket` 로 소켓연결을 시도한다.  
> 테스트를 위해 만든용으로 버튼을 누를때마다 소켓연결이 생성된다. `userCount`값 증가와 `Advanced REST Client` 툴에서 메세지가 찍히는지만 확인하자.  

### Bean 가져오기

`@Component` 로 선언되있음에도 불구하고 `ServerEndpoint` 클래스에서 `@Autowired` 사용이 불가능하다.(사용해도 `null`로 초기화된다)

`ServerEndpoint` 는 웹소켓에 의해 세션이 연결될때마다 인스턴스가 `JWA` 구현에 의해 생성되고 관리되기에 생성될때마다 의존성 주입을 할수 있도록 별도 설정이 필요하다.

> https://stackoverflow.com/questions/30483094/springboot-serverendpoint-failed-to-find-the-root-webapplicationcontext

```java
@Configuration
public class CustomSpringConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

    /**
     * Spring application context.
     */
    private static volatile BeanFactory context;

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return context.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CustomSpringConfigurator.context = applicationContext;
    }
}
```

```java
@Component
@ServerEndpoint(value = "/websocket", configurator = CustomSpringConfigurator.class)
public class Socket {
    ...
    ...
    @Autowired
    ...
}
```

아니면 `ApplicationContextAware` 를 사용해 스프링 컨텍스트에서 직접 가져와 사용해도 된다.  

```java
@Component
public class SpringContext implements ApplicationContextAware {
     
    private static ApplicationContext context;
     
    public static <T extends Object> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
     
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContext.context = context;
    }
}
```

## Spring 웹 소켓 예제  

Spring 웹 소켓에서 스프링 관련 기능을 사용할 수 있기 때문에 많이 추상적이지만 간결하다.  
거추장 스러운 `ServerEndPoint` 같은 어노테이션이 모두 삭제되고 아래 2개 스프링 빈 객체만 적용하면된다.  

```java
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/websocket")
                .setAllowedOrigins("*");
                .withSockJS(); // sockjs 지원
    }
}
```

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static Set<WebSocketSession> sessions = new ConcurrentHashMap().newKeySet();
    private final TestComponent testComponent;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
        testComponent.printTestString();
        log.info("client{} connect", session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("client{} handle message:{}", session.getRemoteAddress(), message.getPayload());
        for (WebSocketSession webSocketSession : sessions) {
            if (session == webSocketSession) continue;
            webSocketSession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
        log.info("client{} connect", session.getRemoteAddress());
    }
}
```

그 외의 코드는 tomcat 웹소켓과 똑같다.  

### SockJS

대부분의 브라우저가 웹소켓을 지원하지만 오래된 버전의 브라우저의 경우 아직 웹소켓을 지원하지 않음으로 `Long Polling` 방식을 지원해야 하는데 이를 위해 `SockJS` 같은 라이브러리가 존재한다.  

스프링 부트에서는 `Websocket` 을 구현하고 몇가지 속성만 추가하면 `SockJS`까지 지원 가능하다.   
폴링 방식의 코드를 별도로 작성할 필요 없어 많은 프로젝트에서 아직까지 `SockJS`를 사용중이다.  

> `SockJS` 라이브러리가 브라우저의 웹소켓 지원여부를 확인후 웹소켓으로 연결할지 폴링방식으로 연결할지 자동으로 결정한다.  

```js
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/websocket")
                .setAllowedOrigins("*")
                .withSockJS(); // sockjs 지원
        registry.addHandler(webSocketHandler, "/websocket")
                .setAllowedOrigins("*"); // 그냥 websocket 지원
    }
}
```

`client` 에 `SockJs` 를 위한 라이브러리 추가,  
웹 소켓 생성 `url`, 메서드를 `SockJs` 형식으로 변경  

```html
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Your First WebSocket!</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
</head>
<body>
    <script type="text/javascript">
        //var wsUri = "ws://localhost:8080/websocket";
        var wsUri = "http://localhost:8080/websocket";
        var websocket;
        var output;
        var textID
        function init() {
            output = document.getElementById("output");
            textID = document.getElementById("textID");
        }
        function connect() {
            if (!websocket) {
                //websocket = new WebSocket(wsUri);
                websocket = new SockJS(wsUri);
                ...
            }
            ...
        }
    ...
    </script>
    ...
</body>
```

웹소켓을 지원하는 브라우저에선 웹소켓으로, 지원하지 않는 브라우저에선 폴링 방식으로 동작한다.  

실제 웹소켓 연결시 뒤에 임의의 숫자, 문자열이 붙어 연결된다.  

```
ws://localhost:8080/websocket/197/dvkiyrn1/websocket
```

## STOMP 웹 소켓 예제

단순한 웹 소켓 처리는 `톰캣 웹소켓` 혹은 `스프링 웹소켓` 라이브러리로 작성할 수 있다.  

> STOMP: Simple (or Streaming) Text Orientated Messaging Protocol. https://stomp.github.io/

복잡한 구조를 위의 웹소켓 라이브러리 통해 처리하고 세밀한 메세지 구조를 작성 할 수 있지만  
`STOMP` 라이브러리를 통해 **구독, 발행 시스템**을 사용할 수 도 있다.  

> https://spring.io/guides/gs/messaging-stomp-websocket/

메시징 형식의 시스템은 브로커와 클라이언트가 있으며 브로커는 토픽에 대한 메세지가 들어오면 
헤딩 토픽을 구독하고 있는 모든 클라이언트에게 메세지를 전달한다.  

```java
@Configuration
@EnableWebSocketMessageBroker //브로커 형식의 웹 소켓 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독 topic 등록시 앞에 붙이는 prefix
        config.setApplicationDestinationPrefixes("/app"); // websocket 메세지 전달시 앞에 붙이는 prefix
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").withSockJS(); // sockjs 지원
        registry.addEndpoint("/websocket"); // 그냥 websocket 지원
    }
}
```

어노테이션을 통해 해당 메세지 브로커 형식으로 웹소켓 서버를 지원한다.  
`stompClient.connect()` 메서드가 호출되면 아래와 같은 메세지가 콘솔에 출력된다.  

```
Opening Web Socket...
Web Socket Opened...

>>> CONNECT
accept-version:1.1,1.0
heart-beat:10000,10000

<<< CONNECTED
version:1.1
heart-beat:0,0

connected to server undefined
Connected: CONNECTED
heart-beat:0,0
version:1.1

>>> SUBSCRIBE
id:sub-0
destination:/topic/greetings
```

대부분의 출력이 `stomp.min.js` 에서 출력된다.  

`SockJS('/websocket')` 메서드로 연결되는 소켓 주소는 아래와 같다.  
`ws://127.0.0.1:8080/websocket/504/qxhc1u02/websocket`  
랜덤한 숫자와 영문자가 추가로 URL 뒤에 붙는다.  


`stompClient.send(), stompClient.subscribe()` 메서드를 통해 데이터를 송 수신하면 콘솔에 아래와 같이 출력된다.  

```
>>> SEND
destination:/app/message
content-length:30

{"message":"Hello WebSocket!"}

<<< MESSAGE
destination:/topic/greetings
content-type:application/json;charset=UTF-8
subscription:sub-0
message-id:qxhc1u02-0
content-length:62
{"now":"2020-12-22T19:17:37.036","content":"Hello WebSocket!"}
```

실제 네트워크 탭에서 송수신한 데이터를 보면 클라이언트에서 보낸 데이터는 `{"message":"Hello WebSocket!"}`
서버에서 보낸 데이터는 `{"now":"2020-12-22T19:17:37.036","content":"Hello WebSocket!"}` 이지만  
부가적으로 보내고 받는 데이터가 여러개 있다.  

> STOMP 라이브러리를 사용하지 않은 일반적인 웹소켓 클라이언트는 호환하기 힘들다. 

해당 메세지를 위처럼 처리하기 위해 `Messging handler annotaion` 을 사용한다.  

```java
@Slf4j
@Controller
public class MessageHandler {

    @MessageMapping("/message")
    @SendTo("/topic/greetings")
    public Greeting greeting(ClientMessage message) throws Exception {
        log.info("message received, message:{}", message.getMessage());
        Thread.sleep(1000); // simulated delay
        return Greeting.builder()
                .content(HtmlUtils.htmlEscape(message.getMessage()))
                .now(LocalDateTime.now())
                .build();
    }
}
```

클라이언트가 `/app/message` 형식으로 보낸 메세지를 처리하고 `@SendTo` 어노테이션으로 해당 `topic` 을 구독하고 있는 클라이언트들에게 반환값을 전달한다.  

내부적으로 세션관리할 필요없이 `STOMP` 라이브러리를 사용하면 쉽게 메시지 브로커 형식의 시스템을 사용 가능하다.  

덤으로 구형 브라우저까지 `SockJS` 라이브러리가 지원해주니 일석이조이다.  

> 간단한 구성의 경우 자유자재로 `session` 관리가 가능한 톰캣, 스프링 웹소켓이 더 편할 수 있다.  

## Websocket Client  

Spring 에서 소켓 서버가 아닌 클라이언트를 사용하고 싶을때 `WebSocketHandler`, `WebSocketSession` 클래스를 사용한다.  

먼저 웹소켓 메세지를 보내고, 받을때 메세지를 처리하는 핸들러 클래스 정의  
`WebSocketHandler` 상속한다.  

```java
import org.springframework.web.socket.*; 

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandlerImpl implements WebSocketHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("afterConnectionEstablished, session:{}, session:{}", session.getId(), session.getUri());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Map<String, String> map = objectMapper.readValue(message.getPayload().toString(), Map.class);
        log.info("handleMessage, message:{}", objectMapper.writeValueAsString(map));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("handleTransportError");

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("afterConnectionClosed, status:{}", closeStatus);

    }

    @Override
    public boolean supportsPartialMessages() {
        log.info("supportsPartialMessages");
        return false;
    }
}
```

여러가지 웹소켓 클라이언트가 있지만 `org.springframework.web.socket.client.standard.StandardWebSocketClient` 사용  

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSender {

    @Value("${websocket.server.url}")
    private String serverUrl;
    @Value("${websocket.server.secret}")
    private String secret;
    private WebSocketSession webSocketSession;

    private final WebSocketHandlerImpl webSocketHandler;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        try {
            URI uri = new URI(serverUrl);
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            headers.add("X-AUTH-TOKEN", secret);
            ListenableFuture<WebSocketSession> listenableFuture = 
                new StandardWebSocketClient().doHandshake(webSocketHandler, headers, uri);
            listenableFuture.addCallback(
                result -> {
                    log.info("WebSocketClient connect success, uri:{}", result.getUri());
                    webSocketSession = result;
                }, ex -> {
                    log.error("WebSocketClient connect failed, error:{}, type{}", ex.getMessage(), ex.getClass().getCanonicalName());
                });
        } catch (URISyntaxException e) {
            log.error("server url syntax error:{}, type:{}", e.getMessage(), e.getClass().getCanonicalName());
        } catch (Exception e) {
            log.error("WebsocketClient init error:{}, type:{}", e.getMessage(), e.getClass().getCanonicalName());
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            this.webSocketSession.close();
        } catch (IOException e) {
            log.error("web socket close error:{}", e.getMessage());
        }
    }

    private boolean sendMessage(String message) {
        try {
            webSocketSession.sendMessage(new TextMessage(message));
            return true;
        } catch (IOException e) {
            log.error("send message error:{}, type:{}, message:{}", e.getMessage(), e.getClass(), message);
            return false;
        }
    }
}
```

## 데모 코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/websock-demo>
