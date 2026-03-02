---
title:  "Spring Boot - NATS!"
date: 2025-01-09


categories:
  - springboot
---

## 개요  

> <https://docs.nats.io/>
> <https://docs.nats.io/nats-concepts/what-is-nats>  
> <https://github.com/nats-io/nats-server>

Go로 개발된 Pub/Sub 기반 메시징 시스템  

`Kafka` 나 `RabbitMQ` 와 같이 **구독/발행** 구조의 메시지 브로커 역할을 하지만
서비스 실행 초반에 큐/파티션 등을 만드는 브로커와 다르게 가볍고 빠르게 `Pub/Sub` 이 가능하다.  

Core NATS 는 기본적으로 `At most once QoS(fire-and-forget)` 기반으로 동작하며 메시지를 메모리에만 보관하고 디스크에 작성하지 않는다.  

```conf
# default.conf
# Client port of 4222 on all interfaces
port: 4222

# HTTP monitoring port
monitor_port: 8222

# Server name (required for JetStream cluster, optional for standalone)
server_name: "nats-server"

# JetStream configuration
# Core NATS와 JetStream 모두 사용 가능
jetstream {
  store_dir: "/data/jetstream"
}

authorization {
  user: admin
  password: password
}
```

```yaml
version: '3.8'

services:
  nats:
    image: nats:2.9.15
    container_name: nats-server
    ports:
      - "4222:4222"  # Client port
      - "8222:8222"  # HTTP monitoring port
      - "6222:6222"  # Cluster port
    volumes:
      - ./etc/nats:/etc/nats
      - nats-jetstream-data:/data/jetstream
    command:
      - --http_port=8222
      - -c=/etc/nats/default.conf
    restart: unless-stopped

volumes:
  nats-jetstream-data:
```

`default.conf` 에서 각종 `nats` 관련 설정 가능  

> <https://docs.nats.io/running-a-nats-service/configuration>

```shell
# nats client 툴 설치
$ brew tap nats-io/nats-tools
$ brew install nats-io/nats-tools/nats

$ nats sub msg.test # Terminal1 구독

$ nats pub msg.test nats-message-1 # Terminal2 발행
$ nats pub msg.test "NATS MESSAGE 2"  # Terminal2 발행
```

> 배치 테스트: <https://docs.nats.io/using-nats/nats-tools/nats_cli/natsbench>

### 토픽

일반적인 토픽 구성은 영숫자를 사용하는 것을 권장 `a-z, A-Z, 0-9`

와일드카드 역할을 하는 특수문자 또한 제공됨  

`.`: 토픽 구분
`*`: 한 토픽 단위에 대한 와일드카드 (for Matching A Single Token)
`>`: 이하 모든 토픽 단위에 대한 와일드 카드 (for Matching Multiple Tokens)

> `*.*.east.>` 믹싱 가능

`$SYS`, `$JS`, `$KV` 예약어

### 메시지 구조 

- **subject**  
- **payload (byte array)**  
  - 기본 크기는 max_payload=1MB 로 지정되어 있지만 64MB까지 확장 가능.  
  - 8MB 이하를 권장함.  
- **header**  
- **reply address(선택사항)**  
  - 메시지가 정상 송신되었을 경우 reply 될 주소  
  - 요청-응답 패턴에서 수신 여부를 확인할 수 있음

```
PUB <subject> [reply-to] <#bytes> [payload]
```

### JetStream

> <https://docs.nats.io/nats-concepts/jetstream>

`JetStream` 을 한 문장으로 `built-in distributed persistence system`(분산 지속형 시스템) 으로 부르며 다른 메시지 스트리밍 기술의 여러 특수한 기능들을 모두 지원한다고 쓰여있다.  
신뢰성 있는 메시지 전송, 메시지 유실에 대한 보존정책 등, 안정성을 위한 개념으로 `STAN(aka NATS Streaming)` 의 후속 개념.  

`JetStream` 과 `Core NATS` 는 메시지 교환 방식에서 중요한 차이가 있다.  
여기서 **Stream** 이란 `JetStream` 시스템 내부에서 메시지를 수집하고 저장하는 일종의 **저장소(Container)** 개념.  
마치 Kafka Topic 로그처럼, 사전에 정의된 Subject 패턴에 매칭되는 메세지를 캡처하여 이 **Stream** 에 영구적으로(또는 설정된 정책만큼) 보관하게 된다.  

`Stream` 은 사용전에 생성해야한다.  

```shell
$ nats stream add default-stream --subjects "jetstream.nats.>"
```

- **JetStream으로 발행 → Core NATS로 수신**  
  - **A: subject가 Stream에 포함된 경우**
    - `JetStream.publish("test.subject", "message")`  
    - `Stream` 이 `test.subject`를 포함할 경우 `Stream` 에 저장됨 로직  
    - Core NATS subscriber도 받을 수 있음 (같은 subject 를 구독 중이라면)  
  - **B: subject가 Stream에 포함되지 않은 경우**  
    - **에러 발생**, JetStream 은 메시지가 Stream 에 안전하게 저장되었다는 **Ack** 를 받아야 한다.  
    - Stream 이 정의되어 있지 않다면 저장이 불가능하므로 에러가 발생.

- **Core NATS로 발행 → JetStream으로 수신**  
  - **C: Stream에 포함된 subject**
    - `Core NATS.publish("test.subject", "message")`
    - Stream에 저장됨, 하지만 Core NATS publish는 Ack 기반 저장 확인이 없다.  
    - JetStream consumer도 받을 수 있음  
    - Core NATS subscriber도 받을 수 있음  
  - **D: Stream에 포함되지 않은 subject**
    - `Core NATS.publish("other.subject", "message")`
    - Stream에 저장되지 않음, Core NATS subscriber만 받을 수 있음
    - JetStream consumer는 받을 수 없음  


1. **JetStream으로 발행된 메시지**:
   - Stream의 subject 패턴에 포함되어야 저장된다.
   - 저장되면 JetStream consumer와 Core NATS subscriber 모두 수신 가능하다.

2. **Core NATS로 발행된 메시지**:
   - Stream이 해당 subject를 수집하도록 설정되어 있다면 저장된다.
   - Stream이 없거나 subject가 매칭되지 않으면 저장되지 않는다.

3. **메시지 지속성**:
   - **JetStream 발행**: Ack 기반 저장 확인 가능, 재시도/지속성 보장에 적합
   - **Core NATS 발행**: 저장 여부를 확인할 수 없으므로 지속성 보장 불가

따라서 **유실되면 안 되는 중요한 메시지는 JetStream 발행**을 사용하고, **유실되어도 무방한 메시지는 Core NATS**를 사용하는 것이 권장된다. 메시지의 성격에 따라 혼용하지 않고 명확히 구분하여 사용하는 것이 좋다.  

#### retry

JetStream 은 메시지 처리 실패 시 재시도를 지원하며, `maxDeliver` 설정값을 초과하면 해당 컨슈머에 대한 재전송을 중단한다.  
이때 **Advisory Event** 를 발행하여 시스템이 이를 인지할 수 있도록 한다. (메시지 보존 여부는 Stream 보존 정책에 따라 결정됨)

> **Advisory Event**: `$JS.EVENT.ADVISORY.CONSUMER.MAX_DELIVERIES.>` 토픽으로 발행되는 시스템 이벤트. 이를 구독하여 별도의 저장소에 저장하거나 알림을 보낼 수 있다.

기존의 MQ 시스템들이 실패한 메시지를 별도의 Queue 로 이동시키는 것과 달리, NATS JetStream 은 이벤트를 통해 비동기적으로 처리하는 방식을 권장한다.

### JetStream 추가 참고사항

- **Stream 보존 정책**: `Limits`, `Interest`, `WorkQueue`에 따라 삭제 조건이 달라진다. 메시지 삭제 시점은 정책과 `max_age`, `max_bytes` 등의 제한값을 함께 고려해야 한다.
- **Storage 유형**: `Memory`는 빠르지만 재시작 시 사라질 수 있다. 내구성이 필요한 경우 `File` 저장소를 사용한다.
- **Consumer 유형**: `Durable`은 상태(시퀀스)를 유지하고, `Ephemeral`은 연결이 끊기면 사라진다.
- **Push vs Pull**: Push는 실시간 푸시에 적합하고, Pull은 처리량/백프레셔 제어에 유리하다.
- **Ack 정책**: `Explicit`은 메시지 단위 Ack가 필요하고, `None`은 자동 처리(유실 가능), `All`은 마지막 메시지 Ack로 일괄 처리된다.
- **AckWait / MaxDeliver**: 처리 시간이 길면 `ack_wait`을 늘리고, 재시도 횟수(`max_deliver`)를 명확히 정의해야 한다.
- **Deliver 정책**: `All`, `Last`, `New`, `ByStartTime`, `ByStartSequence` 등으로 시작 지점을 제어할 수 있다.
- **중복 제거**: `Nats-Msg-Id` 헤더와 `duplicate_window`를 사용해 중복 메시지를 제어할 수 있다.
- **Stream/Consumer 선행 생성**: JetStream 발행은 Stream이 먼저 정의되어 있어야 한다. 운영 환경에서는 사전 생성 및 관리가 안전하다.

## Spring Boot NATS

NATS Server 와 통신하기 위해 `jnats` 라이브러리를 사용한다.

### Gradle Dependency

```groovy
implementation 'io.nats:jnats:2.16.0'
```

### NATS Config

```java
@Slf4j
@Configuration
public class NatsConfig {

    @Value("${nats.core.uri}")
    private String uri;

    // for core nats
    @Bean
    Connection initConnection() throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(uri)
                .errorListener(new ErrorListenerLoggerImpl())
                .connectionListener((conn, type) -> log.info("connection, type:{}", type.toString()))
                .build();
        return Nats.connect(options);
    }

    // for jetstream
    @Bean
    JetStream jetStream(Connection connection) throws IOException, JetStreamApiException {
        JetStream js = connection.jetStream();

        // 기본 Stream 생성 (없으면 생성 시도)
        try {
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name("default-stream")
                    // .subjects("jetstream.nats.>", "test.>", "default.>")
                    .subjects("jetstream.nats.>")
                    .build();
            connection.jetStreamManagement().addStream(streamConfig);
            log.info("JetStream default-stream created or already exists");
        } catch (JetStreamApiException e) {
            if (e.getErrorCode() == 10058) { // Stream already exists
                log.debug("Stream already exists");
            } else {
                log.warn("Failed to create stream: {}", e.getMessage());
            }
        }
        return js;
    }
}
```

### Core NATS

`Dispatcher` 를 사용하여 비동기적으로 메세지를 구독처리한다.

```java
@Slf4j
@Component
public class CoreNatsComponent {

    private final Connection natsConnection;
    private final Dispatcher dispatcher;

    @Autowired
    public CoreNatsComponent(Connection connection) {
        this.natsConnection = connection;
        this.dispatcher = natsConnection.createDispatcher(msg -> {
             log.info("Received message: {}", new String(msg.getData()));
        });
    }

    public void publish(String topic, String message) {
        natsConnection.publish(topic, message.getBytes());
    }

    public void subscribe(String subject) {
        dispatcher.subscribe(subject);
    }

    public void unsubscribe(String subject) {
        dispatcher.unsubscribe(subject);
    }
}
```

### JetStream

`JetStream` 은 `Publish` 할 때와 `Subscribe` 할 때 `JetStream` 객체를 사용한다.
`PushSubscribeOptions` 을 사용하여 Consumer 설정을 할 수 있다.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class JetStreamComponent {

    private final JetStream jetStream;
    private final Connection connection;

    public void publish(String subject, String message) throws IOException, JetStreamApiException {
        jetStream.publish(subject, message.getBytes());
    }

    // 일반 구독 (Auto Ack)
    public void subscribe(String subject, boolean autoAck) throws IOException, JetStreamApiException {
        Dispatcher dispatcher = connection.createDispatcher();
        PushSubscribeOptions options = PushSubscribeOptions.builder().build();

        jetStream.subscribe(subject, dispatcher, msg -> {
            log.info("Received JetStream message: {}", new String(msg.getData()));
            if (!autoAck) {
                msg.ack();
            }
        }, autoAck, options);
    }
}
```

### DLQ Consumer

메세지 처리에 실패하여 재시도가 필요한 경우 `ConsumerConfiguration` 을 통해 설정할 수 있다.

- `ackWait`: 메세지 처리 대기 시간 (Default 30초)
- `maxDeliver`: 최대 재전송 횟수

```java
    // DLQ 처리용 구독
    public void subscribeDlqProcessor(String subject, boolean autoAck) throws IOException, JetStreamApiException {
        Dispatcher dispatcher = connection.createDispatcher();
        
        // Ephemeral Consumer (임시 컨슈머) 생성: 구독 취소 시 컨슈머도 자동 삭제됨
        ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .ackWait(Duration.ofSeconds(3)) // 30초는 너무 길어서 3초로 설정 (빠른 재전송)
                .maxDeliver(3) // 최대 3번 재전송 후 중단
                .build();

        PushSubscribeOptions options = PushSubscribeOptions.builder()
                .configuration(cc)
                .build();

        jetStream.subscribe(subject, dispatcher, msg -> {
             log.error("Processing failed for message: {}", new String(msg.getData()));
             // Ack를 보내지 않으면 재전송됨 (maxDeliver 횟수만큼)
        }, autoAck, options);
    }
```

**Advisory Listener**

`maxDeliver` 횟수만큼 재전송 했음에도 처리에 실패하면 메세지는 폐기된다.  
이때 `$JS.EVENT.ADVISORY.CONSUMER.MAX_DELIVERIES.>` 토픽으로 이벤트가 발행되는데 이를 구독하여 모니터링 할 수 있다.

```java
        // DLQ 모니터링: 최대 전송 횟수 초과 이벤트 구독
        Dispatcher advisoryDispatcher = connection.createDispatcher(msg -> {
            log.error("Advisory: Max deliveries exceeded for messsage: {}", new String(msg.getData()));
        });
        advisoryDispatcher.subscribe("$JS.EVENT.ADVISORY.CONSUMER.MAX_DELIVERIES.>");
```

## 데모코드  

스프링부트에서 nats 를 사용하는 방법

> <https://www.baeldung.com/nats-java-client>
> <https://github.com/Kouzie/spring-boot-demo/tree/main/nats-demo>
