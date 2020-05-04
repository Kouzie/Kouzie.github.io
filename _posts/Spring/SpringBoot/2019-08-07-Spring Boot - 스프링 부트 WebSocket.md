---
title:  "Spring Boot - 스프링 부트 WebSocket!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true

---


## Polling, Long Polling

웹 소켓이 생기기전 서버는 클라이언트에게 어떠한 정보를 알리기 위해 무식한 방법인 `Polling`, `Long Polling` 양방향 통신 기법을 사용하였다.  

`http`프로토콜로는 절대로 서버가 클라이언트에게 요청할 순 없다.   

이를 해결하기 위해 클라이언트가 서버에게 **`n`초 주기**로 계속 자신에게 필요한 정보가 있는지 물어보는 요청을 날린다.(`Polling`방식)  

`Long Polling`은 조금더 효율적으로 `n`초 주기가 아닌 일단 보내고 서버가 응답을 반환할때까지 기다리는 방식이다.  

일단 보내고 time out될 때까지 무한정 기다린다는 것이다  

> https://kamang-it.tistory.com/entry/Webhttp통신을-이용한-양방향-통신기법-long-polling

`HTML5`등장과 함께 `Websocket`이 등장하였고 위와같은 불편한 양방향 통신기법을 사용하지 않게되었다.  

서버는 클라이언트의 요청이 없더라도 자유롭게 클라이언트에게 데이터를 전달할 수 있게되었다.  

하지만 오래된 버전의 브라우저의 경우 아직 웹소켓을 지원하지 않음으로 `Long Polling` 방식을 지원해야 하는데 이를 위해 `SockJS`, `STOMP`같은 라이브러리가 존재한다.  

스프링 부트에서는 `Websocket`을 구현하고 몇가지 속성만 추가하면 `SockJS`까지 지원 가능함으로 2가지 같이 사용가능하다.  
많은 프로젝트에서 아직까지 `SockJS`를 사용중이다.  

## 스프링 부트 기반 간단한 웹소캣 예제   

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
    function init() {
        output = document.getElementById("output");
    }
    function send_message() {
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

   function onOpen(evt) {
       writeToScreen("Connected to Endpoint!");
       var textID = document.getElementById("textID");
       doSend(textID.value);
    }
    function onMessage(evt) {
        writeToScreen("Message Received: " + evt.data);
    }
    function onError(evt) {
        writeToScreen('ERROR: ' + evt.data);
    }
    function doSend(message) {
        writeToScreen("Message Sent: " + message);
        websocket.send(message);
        //websocket.close();
    }
    function writeToScreen(message) {
        var pre = document.createElement("p");
        pre.style.wordWrap = "break-word";
        pre.innerHTML = message;

       output.appendChild(pre);
    }
    window.addEventListener("load", init, false);
</script>
<h1 style="text-align: center;">Hello World WebSocket Client</h1>
<br>
<div style="text-align: center;">
    <form action="">
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

## STOMP

단순한 웹 소켓 처리는 `javax.websocket` 에서 지원하는 `@ServerEndpoint`, `@OnOpen`, `@OnClose`, `@OnMessage`, `@OnError` 어노테이션과 `ServerEndpointExporter` 빈 객체로 처리할 수 있다.  

조금 복잡한 구조를 웹소켓을 통해 처리하고 싶다면 **구독, 발행 시스템**을 사용하면 처리하기 편한데 
이를 지원하는 라이브러리가 `STOMP` 이다, 이제 매력적인 구독, 발행 구조를 `WebSocket` 을 사용해 구축할 수 있다.   

> https://spring.io/guides/gs/messaging-stomp-websocket/

메시징 형식의 시스템은 브로커와 클라이언트가 있으며 브로커는 토픽에 대한 메세지가 들어오면 
헤딩 토픽을 구독하고 있는 모든 클라이언트에게 메세지를 전달한다.  

기존의 `Websocket` 관련된 `Bean` 객체와 클래스를 주석처리하고 아래 `@Configuration` 객체를 추가한다.  
> 주석처리 하지 않아도 상관은 없지만 일반 `Websocket` 과 `STOMP` 를 사용한 `Websocket` 은 서로 호환되지 않는다.  

```java
@Configuration
@EnableWebSocketMessageBroker //브로커 형식의 웹 소켓 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); //구독 topic 등록시 앞에 붙이는 prefix
        config.setApplicationDestinationPrefixes("/app"); // websocket 메세지 전달시 앞에 붙이는 prefix
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/gs-guide-websocket").withSockJS();
    }
}
```

원래 메세지 브로커 기능을 사용하려면 Mqtt 브로커 서버를 별도로 설치해야 하는데  
스프링에서 어노테이션을 통해 해당 서버를 Java 형식으로 지원한다.  

`@EnableWebSocket` 어노테이션과 `WebSocketMessageBrokerConfigurer` 의 구현을 통해  
혹여 웹소켓을 지원하지 않는 브라우저라도 `Loog Pooling` 방식으로 `SockJS` 지원이 가능하다.  

`SockJS`를 통해 웹 소켓에 연결하는 코드  

```js
function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true); // 다시 connect 버튼 못누르게 disable 하는 메서드
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/greetings', function (greeting) {
            $("#greetings").append("<tr><td>" + JSON.parse(greeting.body).content + "</td></tr>");
        });
    });
}
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false); // 다시 disconnect 버튼 못누르게 disable 하는 메서드
    console.log("Disconnected");
}
```


연결 버튼을 누르면 위의 `connect()` 메서드가 호출되고 아래와 같은 콘솔이 출력된다.  

```
Opening Web Socket... stomp.min.js:8 
Web Socket Opened... stomp.min.js:8 

>>> CONNECT stomp.min.js:8 
accept-version:1.1,1.0
heart-beat:10000,10000 

<<< CONNECTED stomp.min.js:8 
version:1.1
heart-beat:0,0

connected to server undefined stomp.min.js:8 

Connected: CONNECTED app.js:19 
heart-beat:0,0
version:1.1


>>> SUBSCRIBE stomp.min.js:8 
id:sub-0
destination:/topic/greetings

>>> DISCONNECT stomp.min.js:8 

Disconnected app.js:31 
```

대부분의 출력이 `stomp.min.js` 에서 출력된다.  

`SockJS('/gs-guide-websocket')` 메서드로 연결되는 소켓 주소는 아래와 같다.  
`ws://localhost:8080/gs-guide-websocket/743/plasadi4/websocket`

`gs-guide-websocket` 뒤의 `path parameter` 는  랜덤한 숫자와 영문자로 결졍된다.  

메세지 구독형 답게 `stompClient.subscribe` 을 통해 `/topic/greetings` path 를 구독하고 해당 토픽에 메세지가 오면 테이블에 추가한다.  

전송 버튼을 누르면 아래 메서드를 호출한다.  

```js
function sendName() {
    stompClient.send("/app/hello", {}, JSON.stringify({'name': $("#name").val()}));
}
```
`/app/hello` URI 로 `JSON` 객체를 전송한다.  

실제 `Websocket` 을 통해 주고받은 데이터는 아래  

```
0: "SEND↵destination:/app/hello↵content-length:17↵↵{"name":"kouzie"}"

0: "MESSAGE↵destination:/topic/greetings↵content-type:application/json;charset=UTF-8↵subscription:sub-0↵message-id:s0y0s3ic-0↵content-length:28↵↵{"content":"Hello, kouzie!"}"
```

콘솔에는 아래와 같이 출력된다.  

```
>>> SEND
destination:/app/hello
content-length:17

{"name":"kouzie"}

<<< MESSAGE
destination:/topic/greetings
content-type:application/json;charset=UTF-8
subscription:sub-0
message-id:s0y0s3ic-0
content-length:28

{"content":"Hello, kouzie!"}
```

해당 메세지를 위처럼 처리하기 위해 `Messging handler annotaion` 을 사용한다.  

`@MessageMapping`, `@SendTo`  

```java
@Controller
public class MessageHandler {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!"); //html 특수문자 변환
    }
}
```

위에서 클라이언트의 websocket message 수신을 위해 prefix 를 붙였었다.  
`config.setApplicationDestinationPrefixes("/app")`

클라이언트가 `/app/hello` 형식으로 보낸 메세지를 처리하고 `@SendTo` 어노테이션으로 해당 topic 을 구독하고 있는 클라이언트들에게 반환값을 전달한다.  

> `parameter` 와 `return` 값은 자동으로 `Json <-> Object` 로 변환가능함으로 `Setter` 를 정의해두면 편하다.  
클라이언트는 

내부적으로 세션관리할 필요없이 `STOMP` 라이브러리를 사용하면 쉽게 메시지 브로커 형식의 시스템을 사용 가능하다.  

덤으로 구형 브라우저까지 `SockJS` 라이브러리가 지원해주니 일석이조이다.  
단 직관적인 코드를 작성하기엔 일반 `Websocket`을 사용하는것이 더 좋다고 생각한다.  

> 채팅 외에는 굳이 `STOMP` 라이브러리를 사용할 필요를 못느끼겠다.  
> 오히려 자유자재로 `session` 관리가 가능한 소켓이 더 편할 수 있다.  

### 추가 코드  

```js
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloMessage {
    private String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Greeting {
    private String content;
}

@Log
@Controller
public class WebPageController {

    @GetMapping("/sample")
    public void websocket_sample(Model model) {
        log.info("websocket_sample called...");
        model.addAttribute("result", "SUCCESS");
    }

    @GetMapping("/stomp")
    public void websocket_stomp(Model model) {
        log.info("websocket_stomp called...");
        model.addAttribute("result", "SUCCESS");
    }
}
```
