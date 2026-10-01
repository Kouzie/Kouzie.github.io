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

Kafka, RabbitMQ 와 같이 구독/발행 구조의 메시지 브로커 역할을 하지만
서비스 실행 초반에 큐/파티션 등을 만드는 브로커와 다르게 가볍고 빠르게 Pub/Sub 이 가능하다.  

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

`JetStream`은 NATS에 메시지 저장과 재전송 기능을 추가한 `built-in distributed persistence system`이다.

Core NATS가 현재 접속한 subscriber에게 메시지를 즉시 전달하는 데 집중한다면, JetStream은 subject가 일치하는 메시지를 **Stream**에 저장하여 나중에도 다시 읽을 수 있게 한다.

![JetStream 메시지 저장과 Consumer 재수신 흐름](/assets/springboot/nats-jetstream-store-consume-flow.svg)

그림의 요소를 메시지 흐름 순서로 살펴보면 다음과 같다.

**1. Publisher → Stream**

Publisher는 Stream 이름이 아니라 `jetstream.nats.order` 같은 subject로 메시지를 발행한다.

Stream의 `subjects` 설정이 `jetstream.nats.>`라면 subject가 패턴과 일치하므로 메시지를 저장한다.

JetStream publish는 저장 후 Stream 이름과 sequence가 포함된 `PubAck`를 반환한다.

**2. Stream: Persistent Queue / Log**

Stream은 메시지를 `100`, `101`, `102`처럼 증가하는 **Stream sequence**와 함께 보관한다. 이 sequence는 Kafka partition offset처럼 Stream 안의 메시지 위치값이다.  

원본 메시지는 Consumer가 Ack했다고 바로 삭제되지 않는다. 삭제 시점은 `Limits`, `Interest`, `WorkQueue` 보존 정책과 `max_age`, `max_bytes` 같은 제한이 결정한다.

저장소는 내구성이 필요하면 `File`, 재시작 후 유실되어도 되는 데이터라면 `Memory`를 사용한다.

**3. Durable Consumer: Cursor / Ack Floor**

클라이언트별 처리 위치는 Stream sequence 자체가 아니라 **Consumer 상태**로 관리한다. Consumer는 어떤 Stream sequence까지 전달했고 어디까지 Ack되었는지를 서버에 보관하는 객체다.

그림의 `Ack Floor = 102`는 `APP_A`가 102번까지 처리했다는 뜻이다. 따라서 다음 수신 메시지는 103번이다.

Consumer의 `Ack Floor`가 Kafka consumer group의 committed offset과 가장 유사하다. 클라이언트가 offset 파일이나 flag를 직접 보관하는 대신, 재접속할 때 같은 Durable Consumer 이름을 사용하면 서버에 저장된 위치에서 이어받는다.

따라서 서로 다른 Durable Consumer인 `APP_A`, `APP_B`는 같은 Stream sequence의 메시지를 읽더라도 각자 다른 Ack Floor를 가진다. 반대로 여러 클라이언트가 동일한 `APP_A`를 사용하면 하나의 Ack Floor를 공유한다.

Consumer에는 Stream sequence와 별도로 **Consumer sequence**도 존재한다. subject filter로 일부 메시지를 건너뛰거나 같은 메시지가 재전송되면 두 sequence가 서로 달라질 수 있다.

- 여러 worker가 동일한 Durable Consumer를 사용하면 하나의 queue처럼 메시지를 나누어 처리한다.
- 서로 다른 Durable Consumer를 사용하면 각 Consumer가 같은 Stream을 독립적으로 읽는다.
- Push Consumer는 서버가 메시지를 전달한다.
- Pull Consumer는 worker가 처리 가능한 만큼 요청하므로 백프레셔 제어에 유리하다.

**4. Worker 처리와 Ack/Nak**

`Explicit Ack` Consumer에서는 worker가 처리를 완료한 뒤 Ack해야 Cursor가 진행된다.

Ack하지 않고 `AckWait`이 지나거나 `Nak`하면 같은 메시지가 재전송된다. 처리가 오래 걸리면 `InProgress`로 AckWait을 연장하고, 재시도해도 처리할 수 없는 메시지는 `Term`으로 종료할 수 있다.

`AckWait`은 Stream이 아니라 **Consumer에 설정하는 값**이다. 메시지가 worker에 전달된 시점부터 `AckWait` 안에 Ack가 NATS 서버에 도착하지 않으면 서버는 처리에 실패한 것으로 판단하여 같은 메시지를 다시 전달한다.

```java
ConsumerConfiguration consumerConfig = ConsumerConfiguration.builder()
    .durable("ORDER_CONSUMER")
    .ackPolicy(AckPolicy.Explicit)
    .ackWait(Duration.ofSeconds(30)) // 전달 후 30초 안에 Ack가 없으면 재전송
    .maxDeliver(3)                  // 최초 전달을 포함하여 최대 3회 전달
    .build();
```

NATS CLI로 Consumer를 생성할 때는 `--wait` 옵션으로 설정한다.

```shell
$ nats consumer add ORDERS ORDER_CONSUMER \
    --ack explicit \
    --wait 30s \
    --max-deliver 3
```

`Nak`은 `AckWait` 만료를 기다리지 않고 재전송을 요청한다. 반대로 아무 응답도 하지 않으면 서버는 `AckWait`이 만료될 때까지 기다린 뒤 재전송한다.

**5. DLQ 처리**

JetStream에는 RabbitMQ처럼 `MaxDeliver`를 초과한 메시지를 별도 Queue로 자동 이동시키는 내장 DLQ가 없다.

```text
처리 실패 → Ack 없음/Nak → 재전송 → MaxDeliver 도달
         → MAX_DELIVERIES Advisory 발행
```

`MaxDeliver`에 도달하면 해당 Consumer의 재전송만 중단된다. 원본 메시지는 폐기되지 않고 Stream의 Retention Policy에 따라 계속 보관된다.

실패 이벤트는 다음 Advisory subject로 발행된다.

```text
# MaxDeliver 횟수만큼 재전송한 뒤 서버가 자동으로 발행
$JS.EVENT.ADVISORY.CONSUMER.MAX_DELIVERIES.<STREAM>.<CONSUMER>

# Consumer가 복구 불가능한 메시지에 Term을 보내 재전송을 즉시 종료하면 발행
$JS.EVENT.ADVISORY.CONSUMER.MSG_TERMINATED.<STREAM>.<CONSUMER>
```

Advisory는 실패한 원본 payload가 아니라 `stream`, `consumer`, `stream_seq`, `deliveries` 등의 메타데이터를 담은 시스템 이벤트다. 따라서 DLQ를 구성하는 방법은 다음 두 가지로 나뉜다.

**Advisory를 DLQ Stream에 직접 저장**

별도 프로그램 없이 Advisory subject를 저장하는 Stream을 만들 수 있다.

```shell
# MaxDeliver 실패 이벤트를 저장하는 Stream
$ nats stream add dlq-advisory-stream \
    --subjects '$JS.EVENT.ADVISORY.CONSUMER.MAX_DELIVERIES.>' \
    --storage file \
    --retention limits \
    --defaults

# 저장된 Advisory에서 stream_seq를 확인한 뒤 원본 조회
$ nats stream get default-stream 100
```

이 방식은 DLQ Stream에서 실패 이력을 확인한 뒤, Advisory의 `stream_seq`를 이용해 원본 Stream을 한 번 더 조회한다. 별도 프로그램은 필요 없지만 원본이 Retention Policy로 삭제되면 더 이상 조회할 수 없다.

**원본 메시지를 DLQ Stream에 복사**

DLQ에서 원본 subject, header, payload를 바로 확인하려면 Advisory를 처리하는 별도 프로그램이 필요하다.

```text
MAX_DELIVERIES Advisory 구독
    → stream_seq로 원본 조회
    → dlq.<원본-subject>로 JetStream publish
    → DLQ Stream에서 원본을 바로 조회
```

```shell
# 별도 프로그램이 재발행한 원본 메시지를 저장하는 Stream
$ nats stream add dlq-stream \
    --subjects 'dlq.>' \
    --storage file \
    --retention limits \
    --defaults
```

별도 프로그램은 Advisory의 `stream_seq`로 원본을 조회하고 `dlq.<원본-subject>`로 재발행한다. 이 로직은 독립된 DLQ Relay로 실행하거나 기존 Consumer 애플리케이션에 포함할 수 있다.

| 구성 | DLQ에 저장되는 내용 | 원본 확인 방법 | 별도 프로그램 |
| --- | --- | --- | --- |
| Advisory Stream | 실패 메타데이터와 `stream_seq` | 원본 Stream을 한 번 더 조회 | 불필요 |
| 원본 복사 DLQ Stream | 원본 subject, header, payload | DLQ에서 바로 조회 | 필요 |

원본 조회와 DLQ 재발행은 하나의 트랜잭션이 아니다. 원본 복사 방식을 사용할 때는 DLQ publish의 `PubAck`를 확인하고 `Nats-Msg-Id`로 중복 저장을 방지하는 것이 안전하다.

> 공식 문서: <https://docs.nats.io/using-nats/developing-with-nats/js/consumers#dead-letter-queues-type-functionality>

**6. 특정 위치부터 재수신**

운영 Consumer에 영향을 주지 않으려면 `--deliver 100`처럼 시작 Stream sequence를 지정한 별도 Replay Consumer를 만든다.

NATS Server 2.14 이상에서는 `consumer reset --sequence 100`으로 기존 Consumer도 되돌릴 수 있다. 다만 Ack 상태가 변경되어 중복 처리가 발생할 수 있다.

이 글의 Docker 예시인 Server `2.9.15`에서는 reset API를 지원하지 않으므로 새 Replay Consumer를 사용해야 한다. Retention Policy에 의해 이미 삭제된 메시지는 어떤 방법으로도 다시 받을 수 없다.

**발행 방식과 Stream subject 매칭 여부**

- **A. JetStream publish + Stream subject와 일치**
  - 메시지를 Stream에 저장한다.
  - 발행자는 저장 결과인 `PubAck`를 받는다.
  - 같은 subject를 구독 중인 Core NATS subscriber도 실시간으로 받을 수 있다.
  - 해당 Stream의 JetStream Consumer도 메시지를 받을 수 있다.

- **B. JetStream publish + Stream subject와 불일치**
  - 메시지를 저장할 Stream이 없으므로 저장되지 않는다.
  - `PubAck`를 받을 수 없어 `no responders` 또는 timeout 오류가 발생한다.
  - 같은 subject를 구독 중인 Core NATS subscriber는 실시간 메시지를 받을 수 있지만, JetStream publish 호출 자체는 저장 실패로 처리된다.
  - 저장된 메시지가 없으므로 JetStream Consumer는 받을 수 없다.

- **C. Core NATS publish + Stream subject와 일치**
  - Stream이 메시지를 캡처하여 저장한다.
  - Core NATS publish는 `PubAck`를 기다리지 않으므로 발행자는 저장 성공 여부를 확인하지 않는다.
  - Core NATS subscriber는 실시간으로 받을 수 있다.
  - JetStream Consumer도 저장된 메시지를 받을 수 있다.


**CLI 예제**

```shell
# default.conf에 설정한 계정으로 접속
$ export NATS_URL="nats://admin:password@localhost:4222"

# jetstream.nats.> subject를 파일로 저장하는 Stream 생성
$ nats stream add default-stream \
    --subjects "jetstream.nats.>" \
    --storage file \
    --retention limits \
    --defaults

# JetStream 송신: Stream 저장 완료 후 PubAck 확인
$ nats pub --jetstream jetstream.nats.test "message-1"
$ nats pub --jetstream jetstream.nats.test "message-2"

# APP_A의 수신 위치와 Ack 상태를 서버가 보관하는 Durable Pull Consumer 생성
$ nats consumer add default-stream APP_A \
    --pull \
    --filter "jetstream.nats.>" \
    --ack explicit \
    --deliver all \
    --wait 30s \
    --max-deliver 5 \
    --defaults

# 메시지를 가져와 처리 완료 Ack; 다시 실행하면 다음 위치에서 이어받는다.
$ nats consumer next default-stream APP_A --count 1 --ack
$ nats consumer info default-stream APP_A

# Stream sequence 100부터 독립적으로 다시 읽는 Replay Consumer
$ nats consumer add default-stream APP_A_REPLAY_100 \
    --pull \
    --filter "jetstream.nats.>" \
    --ack explicit \
    --deliver 100 \
    --defaults

$ nats consumer next default-stream APP_A_REPLAY_100 --count 10 --ack

# NATS Server 2.14 이상에서 기존 Consumer를 되돌리는 방법
$ nats consumer reset default-stream APP_A --sequence 100

# Durable Consumer 삭제
# APP_A의 Cursor, Ack Floor, Pending Ack 상태는 삭제되지만 Stream 원본 메시지는 유지된다.
$ nats consumer rm default-stream APP_A
```

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


## 데모코드  

스프링부트에서 nats 를 사용하는 방법

> <https://www.baeldung.com/nats-java-client>
> <https://github.com/Kouzie/spring-boot-demo/tree/main/nats-demo>
