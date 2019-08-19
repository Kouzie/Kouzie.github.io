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

웹 소켓이 생기기전 서버는 클라이언트에게 어떠한 정보를 알리기 위해 무식한 방법인 Polling, Long Polling 양방향 통신 기법을 사용하였다.  

http프로토콜로는 절대로 서버가 클라이언트에게 요청할 순 없다.  
이를 해결하기 위해 클라이언트가 서버에게 n초 주기로 계속 자신에게 필요한 정보가 있는지 물어보는 요청을 날린다.(Polling방식)  

Long Polling은 조금더 효율적으로 n초 주기가 아닌 일단 보내고 서버가 응답을 반환할때까지 기다리는 방식이다.  

> https://kamang-it.tistory.com/entry/Webhttp통신을-이용한-양방향-통신기법-long-polling

HTML5등장과 함께 Websocket이 등장하였고 위와같은 불편한 양방향 통신기법을 사용하지 않게되었다.  
서버는 클라이언트의 요청이 없더라도 자유롭게 클라이언트에게 데이터를 전달할 수 있게되었다.  

하지만 오래된 버전의 브라우저의 경우 아직 웹소켓을 지원하지 않음으로 Long Polling 방식을 지원해야 하는데 이를 위해 SockJS, STOMP같은 라이브러리가 존재한다.  

스프링 부트에서는 Websocket을 구현하고 몇가지 속성만 추가하면 SockJS까지 지원 가능함으로 2가지 같이 사용가능하다.  
많은 프로젝트에서 아직까지 SockJS를 사용중이다.  

## 스프링 부트 기반 간단한 웹소캣 예제   


```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

```java
@Component
@ServerEndpoint(value = "/websocket",  //서버가 바인딩된 주소를 뜻함.
                encoders = MessageEncoder.class,
                decoders = MessageDecoder.class)
public class Socket {
    private static Logger log = LoggerFactory.getLogger(Socket.class);
    private Session session;
    public static Set<Socket> listeners = new CopyOnWriteArraySet<>();

    private static int onlineCount = 0;

    @OnOpen //클라이언트가 소켓에 연결되때 마다 호출
    public void onOpen(Session session) {
        log.info("onOpen called()...");
        addOnlineCount();
        this.session = session;
        listeners.add(this);
    }

    @OnClose //클라이언트와 소켓과의 연결이 닫힐때 (끊길떄) 마다 호
    public void onClose(Session session) {
        log.info("onClose called()...");
        subOnlineCount();
        listeners.remove(this);
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("onMessage called()...");
        broadcast(message);
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
            log.error("Caught exception while sending message to Session " + this.session.getId(), e.getMessage(), e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("onError called()...");
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        Socket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        Socket.onlineCount--;
    }
}
```

소켓 동작을 위한 빈 객체 생성 및 설정
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket").withSockJS();
    }
}
```

`@EnableWebSocket` 어노테이션과 `WebSocketMessageBrokerConfigurer` 의 구현을 통해 SockJS 지원이 가능하다.  


만약 별도의 인코딩, 디코딩 과정이 필요하다면 `@ServerEndpoint` 어노테이션에 `decoders`, `encoders` 설정을 아래 클래스로 지정  

구현클래스의 메서드 오버라이딩을 통해 encode, `decode` 설정이 가능.

```java
public class MessageDecoder implements Decoder.Text<String> {
  ...
  ...
}


public class MessageEncoder implements Encoder.Text<String> {
  ...
  ...
}
```


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