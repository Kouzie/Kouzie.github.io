---
title:  "Spring Boot - Kafka!"
date: 2023-04-13


categories:
  - springboot
---

## 개요

> <https://kafka.apache.org/>
>
> 출처: <https://github.com/AndersonChoi/tacademy-kafka>

카프카는 과거 링크드인의 제이 크렙스, 준 라오, 네하 나크헤데에 의해 탄생한 스칼라로 개발한 오픈 소스 메시지 브로커 프로젝트이다  

기존의 `end to end` 방식의 연결 구조에서 `messaging broker` 방식으로 변경하기 위한 메시지 브로커 오픈소스  
기존의 복잡한 파이프라인 구조를 아래와 같이 `kafka`를 통해 간편화시킨다.  

![kafka1](/assets/springboot/springboot_kafka2.png)  

![kafka1](/assets/springboot/springboot_kafka1.png)  

카프카를 사용함으로 얻을수 있는 장점은 아래와 같다.  

- 높은 처리량  
- 순서 보장  
- 적어도 한 번 (at least once) 전송 방식  
- 강력한 파티셔닝  
- 자연스러운 백 프레셔 핸들링  
- 로그 컴팩션  

이번 포스트에선 스프링 부트에서 카프카를 사용하는 방법을 알아보는 것임으로 카프카의 간단한 구성요소만 알아보고 바로 라이브러리 사용법을 살펴볼 예정이다.  

### 용어  

- `Broker` : **카프카 애플리케이션 서버** 단위  
- `Topic` : **데이터 분리 단위**. 다수 파티션 보유  
- `Partition` : **레코드 저장소**. 컨슈머 요청시 레코드 전달  
- `Offset` : 각 레코드당 파티션에 할당된 **레코드 고유 번호**  
- `Consumer` : **레코드를 가져가는(polling) 애플리케이션**  
- `Consumer group` : **다수 컨슈머 묶음**  
- `Consumer offset` : 특정 **컨슈머가 가져간 레코드의 번호**  
- `Producer` : 레코드를 브로커로 전송하는 **레코드 저장 애플리케이션**  

파티션(메시지 저장소)의 복제(고가용성)를 위해 일반적으로 3개 이상의 `broker`를 운영하며   
확장(스케일 아웃), 장애 발생 시 메시지 백업 등의 안정성을 제공한다.  

### 파티션 

다른 브로커와는 다르게 파티션이라는 저장공간을 제공하며 저장되는 리소스(메시지)의 백업, 복제 등을 제공한다.  

![kafka3](/assets/springboot/springboot_kafka3.png)  

파티션을 나눔으로 여러개의 클라이언트(`consumer`) 가 각 파티션에 연결하여 동시에 처리할 수 있다.  

> 파티션 개수마다 `consumer` 를 사용해야한다. 물론 하나의 `consumer` 가 여러개의 파티션을 담당할 수 있다.   

파티션과 `consumer` 가 `N:1` 관계를 맺기에 `consumer` 끼리 경쟁할 필요가 없으며   
어떤 `consumer` 가 데이터를 어디까지 가져갔는지 파악가능하다.  

트래픽이 많아질수록 파티션과 `consumer` 를 늘리는 전략으로 스케일 아웃이 가능하다.  
단 파티션은 한번 늘리면 줄이는 것은 불가능함으로 파티션을 늘리기전 충분한 테스트 과정이 필요하다.  

반대로 파티션 개수보다 `consumer` 가 많아지면 추가생성된 `consumer`는 동작하지 않는다.  

### 복제  

여러개의 브로커를 구성하고 하나의 토픽에 여러개의 파티션을 지정하면 브로커별로 파티션을 나누어 저장한다.  
아래 그림의 경우 3개의 브로커에 3개의 파티션을 지정한 경우이다.  

![kafka6](/assets/springboot/springboot_kafka6.png)  


모종의 이유로 셋 중 하나의 브로커가 중단된다면 데이터 손실이 발생하므로 카프카에서는 아래와 같이 복제 정책을 사용한다.  

![kafka7](/assets/springboot/springboot_kafka7.png)  

파티션이 많아질수록 동기화에 많은 리소스(네트워크, 저장소 등)가 사용되므로 사용량에 따른 적절한 브로커 개수를 설정해야 한다.  

### 컨슈머 그룹  

다른 메시지 브로커 역할을 수행하는 애플리케이션이 그렇듯  
여러 개의 인스턴스가 동일한 `topic`을 구독하고 있을 때 중복 처리를 방지하기 위한 그룹 설정이 필요하다.  

![kafka5](/assets/springboot/springboot_kafka5.png)  

그룹을 지정하고 `consumer` 생성후 파티션에서 레코드를 읽어가면 읽은 부분까지 `offset` 을 `kafka` 서버에 기록(`commit`)한다.  

그룹별로 `offset` 을 지정하기 때문에 같은 `topic` 에 대한 여러가지 로직을 수행하고 싶다면  
해당 레코드를 처리하는 여러 `consumer` 그룹을 생성하면 된다.  

### zookeeper


주키퍼는 Yahoo에서 처음 개발되어 Apache License로 오픈소스 된 프로젝트로 **로드밸런스, 시스템 간의 정보 공유, 상태 체크, 서버들 간의 동기화** 등을 처리해준다.  

카프카 역시 수많은 데이터를 관리하는 분산 데이터 시스템이기에 분산, 복제된 데이터의 충돌을 막고 데이터 동기화를 진행해야 하는데 이를 처리하는 분산 코디네이션 서비스가 주키퍼이다.  

![kafka4](/assets/springboot/springboot_kafka4.png)  

초록색 박스가 주키퍼 서버역할이고 카프카 노드는 주키퍼 클라이언트 역할이다.   

주키퍼는 카프카의 데이터를 액세스하여 데이터를 동기화하고 카프카 노드에 대한 메타데이터도 관리하는데  
카프카가 성장하면서 zookeeper의 성능 한계가 드러나면서 향후 zookeeper는 제외되고 카프카 라프트(KRaft)가 대체할 예정이다.  

### 설치 

> <https://github.com/onlybooks/kafka2/blob/main/appendix_C/single_zk_kafka/docker-compose.yml>

간단히 로컬에 싱글 클러스터로 설치한다.  

```yaml
version: "3.5"
services:
  zk:
    image: confluentinc/cp-zookeeper:5.5.1
    restart: always
    hostname: zk
    container_name: zk
    ports:
      - "2181:2181"
    environment:
      - ZOOKEEPER_SERVER_ID=1
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
      - ZOOKEEPER_INIT_LIMIT=5
      - ZOOKEEPER_SYNC_LIMIT=2
      - ZOOKEEPER_SERVERS=zk:2888:3888

  kafka:
    image: confluentinc/cp-kafka:5.5.1
    restart: always
    hostname: kafka
    container_name: kafka
    ports:
      - "9092:9092"
      - "9999:9999"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zk:2181
      KAFKA_LISTENERS: INTERNAL://kafka:9092
      KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_JMX_PORT: 9999

  kafka_manager:
    image: hlebalbau/kafka-manager:stable
    container_name: cmak
    ports:
      - "9000:9000"
    environment:
      ZK_HOSTS: "zk:2181"
      APPLICATION_SECRET: "random-secret"
    command: -Dpidfile.path=/dev/null
```

생성후 컨테이너 안으로 들어가 각종 kafka cli 툴들을 사용할 수 있다.  

```
docker exec -it kafka bash
root@kafka:/# kafka-topics --version

5.5.1-ccs (Commit:3c4783aac9e33249)
```

<!-- 
> `amazon linux 2` 기준 

`java jvm` 설치 필요

## kafka 실행 바이너리 다운  

`$ wget http://mirror.navercorp.com/apache/kafka/2.5.0/kafka_2.12-2.5.0.tgz`  
`tar xvf kafka_2.12-2.5.0.tgz`  

## 카프카 힙 용량 설정

`ec2` 에 맞게 `default 1GB` 아닌 `400m` 로 변경  

```
export KAFKA_HEAP_OPTS="-Xmx400m -Xms400m"
```

`server.properties` 의 아래 설정 변경    

```conf
# 추적처리 해제 및 공인IP 로 수정 필요 
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://192.168.10.234:9092
```

## kafka 실행  

설정한 `server.properties` 로 `kafka` 실행  


```
$ bin/kafka-server-start.sh -daemon config/server.properties
# kafka 실행 확인  
$ jps
4415 Kafka
```

## 로컬에 클라이언트 설치  

서버에 접속해 여러가지 기능을 수행할 수 있는 클라이언트 프로그램을 설치 및 압축해제

```
$ curl http://mirror.navercorp.com/apache/kafka/2.5.0/kafka_2.13-2.5.0.tgz --output kafka.tgz 
$ tar xvf kafka_2.13-2.5.0.tgz
``` -->

### kafka 테스트

docker 컨테이너 내부에서 cli 툴을 사용해도 되지만  
로컬 PC 에 cli 툴을 설치해서 테스트 진행  

> <https://kafka.apache.org/downloads>

위 사이트에서 카프카 설치를 위한 바이너리를 받을 수 있다.  
`/bin` 에 있는 kafka 클라이언트 sh 파일만 사용할 예정  

파티션 3개로 `test` 토픽 생성  

```
./kafka-topics.sh --create \
 --bootstrap-server 192.168.10.234:9092 \
 --replication-factor 1 \
 --partitions 3 \
 --topic test

Created topic test.
```

> `replication-factor`는 현재 `kafka` 브로커가 하나뿐이므로 1개밖에 설정할 수 없다.  

`test` 토픽 `producer` 생성

```
./kafka-console-producer.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --topic test

>hello
>kafka
>hello
```

`test` 토픽 `consumer` 생성  
`from-beginning` 는 파티션에 저장된 모든 데이터를 처음부터 가져옴  

```
./kafka-console-consumer.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --from-beginning \
 --topic test

kafka
hello
hello
```
  
받아오는 데이터를 보면 입력한 순서가 다른데 파티션 3개에서 데이터를 가져오다 보니 순서가 다르다.  

`test` 토픽의 컨슈머 그룹 `testgroup` 생성  

```
./kafka-console-consumer.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --from-beginning \
 --group testgroup \
 --topic test

kafka
hello
hello
```

그룹으로 가져올 경우 중복처리 방지를 위해 이미 앞에서 해당 그룹으로 데이터를 가져왔다면 다음부턴 그 이후의 데이터만 가져온다.  
다시한번 위의 명령을 실행시 아무 데이터도 가져오지 않는다  

접속중인 컨슈머 그룹 확인, 현재 콘솔을 통해 하나의 컨슈머가 연결된 상태이다.  

```
./kafka-consumer-groups.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --list

console-consumer-14607
```

생성된 컨슈머 그룹 모니터링, 각 파티션당 어느 레코드까지 데이터를 읽어왔는지 확인 가능하다.  

```
./kafka-consumer-groups.sh \
 --bootstrap-server 192.168.10.234:9092  \
 --group testgroup \
 --describe

Consumer group 'testgroup' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
testgroup       test            1          2               2               0               -               -               -
testgroup       test            0          1               1               0               -               -               -
testgroup       test            2          2               2               0               -               -               -
```

각 파티션마다 현재 `OFFSET`, 마지막 `OFFSET`, 읽지 못한 개수(`LAG`) 등을 알 수 있다.  
`LAG`이 늘어날수록 해당 `consumer` 그룹이 데이터 처리를 하지 못한 것이므로 관리가 필요하다.  

컨슈머 그룹 오프셋으로 초기화

```
./kafka-consumer-groups.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --reset-offsets \
 --to-earliest \
 --execute \
 --group testgroup \
 --topic test
```

`testgroup` 의 오프셋 초기화.  
해당 명령 수행후 모니터링 명령(`--describe`) 수행시 `LAG` 와 `LOG-END-OFFSET` 가 동일해진다.  

특정 파티션만 특정 `offset` 지정도 가능하다.  

```
./kafka-consumer-groups.sh \
 --bootstrap-server 192.168.10.234:9092 \
 --reset-offsets \
 --to-offset 10 \
 --execute \
 --group testgroup \
 --topic test:1
```

`test` 토픽의 1번 파티션의 `offset` 을 `10`으로 지정한다

## java kafka client

### kafka producer

![kafka5](/assets/springboot/springboot_kafka8.png)  

`producer` 내부에서 일련의 작업들을 진행한 후 임시로 메시지들을 모아두었다가  
**배치**로 카프카 서버로 전송하게 된다.  

`ProducerRecord` 는 카프카로 보내기 위한 데이터, 4가지로 분류된다.  

- `topic`  
- `partition` [optional]  
- `key` [optional]  
- `value`  

`partition`은 저장 시에, `key`는 파티션 선택 시에 사용되는 `optional` 값이다.  
지정하지 않으면 라운드로빈으로 각 파티션에 순서대로 저장된다.  

같은 `key`를 가진 메시지는 항상 같은 파티션에 저장되므로, 같은 파티션을 구독하는 `consumer`에서 순서 보장이 가능하다.  

```java
// topic, value
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, data);
// topic, key, value
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, Integer.toString(index), data);
// topic, partition, key, value
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, PARTITION_NUMBER, Integer.toString(index), data);
```

내부에서 지정된 횟수만큼 재시도하며 실패, 성공 결과가 리턴된다.  

카프카 producer 는 3가지 전송 방식으로 나뉜다.  

- `acks=0`  
  **가장 속도가 빠름, 유실 가능성 큼**  
  메시지를 보낸 즉시 성공으로 간주, 리더 팔로워 파티션에 저장되었는지 확인하지 않음  
  센서와 같은 일부 유실되더라도 흐름이 유지되는 경우 `acks=0`을 사용  

- `acks=1(default)`  
  **속도 보통, 유실 가능성 있음**  
  리더 파티션에 저장되었는지 확인, 확인 후 성공 처리  
  복제되기 전에 브로커가 다운되면 유실될 가능성이 있다. 만약 브로커가 하나라면 유실 가능성 없다.  

- `acks=-1` 또는 `acks=all`
  **속도 느림, 유실 가능성 없음**  
  all 옵션이라 부르며 리더, 팔로워 파티션에 모두 저장 성공되었는지 확인  
  모든 파티션에 저장되었는지 확인하기에 속도가 가장 느리다  

```java
public KafkaProducer<String, String> kafkaProducer() {
    Properties configs = new Properties();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig); // bootstrap.servers
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // key.serializer
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // value.serializer
    configs.put(ProducerConfig.ACKS_CONFIG, "1"); // acks
    // configs.put(ProducerConfig.ACKS_CONFIG, "0");
    // configs.put(ProducerConfig.ACKS_CONFIG, "all");
    KafkaProducer<String, String> producer = new KafkaProducer<>(configs);
    return producer;
}
```

```java
@Scheduled(fixedDelay = 1000) // 1초마다 실행
public void producerSchedule() {
    String data = "This is record " + index++;
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, data);
    Future<RecordMetadata> sendResult = producer.send(record);
}
```

`send` 메서드는 에러가 없다면 `RecordMetadata` 객체를 반환한다.  
Future 메서드를 핸들링해도 되고 send 에 callback 함수를 등록해도 된다.  

```java
public class ProducerCallback implements org.apache.kafka.clients.producer.Callback {

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            exception.printStackTrace();
        } else {
            log.info("onCompletion invoked, {}", metadata.toString());
        }
    }
}
```

```java
producer.send(record, new ProducerCallback());
```

### kafka consumer

kafka consumer로 메시지를 가져오는 방법은 아래 3가지  

- 오토 커밋 (Auto Commit)  
- 동기 커밋 (Synchronous Commit)  
- 비동기 커밋 (Asynchronous Commit)  

```java
@Bean
public KafkaConsumer<String, String> kafkaConsumer() {
    Properties configs = new Properties();
    configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig); // bootstrap.servers
    configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // group.id
    configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // key.deserializer
    configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // value.deserializer
    configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // auto.offset.reset
    configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); // enable.auto.commit
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);
    consumer.subscribe(Arrays.asList(topic));
    return consumer;
}
```

`auto.offset.reset`은 컨슈머 그룹이 처음 시작하거나 유효한 offset이 없을 때의 동작을 설정한다.  
- `latest`: 가장 최근 offset부터 시작 (기본값, 새로운 메시지만 처리)  
- `earliest`: 가장 오래된 offset부터 시작 (모든 메시지 처리)

![kafka5](/assets/springboot/springboot_kafka9.png)  

`kafka consumer`는 그림처럼 각 파티션마다 전담 클라이언트가 있으며  
`consumer group`이 있을 경우 각 파티션마다 어떤 `offset`까지 읽었는지 확인 가능하다.  

커밋이란 작업은 이 `offset` 위치를 옮기는 작업을 뜻한다.  

#### 오토커밋  

```java
private void init() {
    new Thread(() -> {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                log.info(record.toString());
                //  ConsumerRecord(topic = test, partition = 0, leaderEpoch = 0, offset = 0, CreateTime = 1681974934012, serialized key size = -1, serialized value size = 4, headers = RecordHeaders(headers = [], isReadOnly = false), key = null, value = TEST)
            }
        }
    }).start();
}
```

개발자가 언제 커밋할지 지정하지 않기 때문에 오류가 발생하면 어떤 메시지까지 처리했는지 알 수 없다.  

커밋 과정에서 오류가 발생해 `offset` 업데이트 실패 시 메시지 중복 수신 장애가 발생할 수 있다.  
또한 `offset` 업데이트는 했지만 미처 처리하지 못한 메시지들은 모두 유실 가능성이 있는 메시지들이 된다.  

이슈가 많이 발생하는 방법이지만 메시지 서비스로 엄격한 트랜잭션 처리를 주로 하지 않기 때문에 오토 커밋을 가장 많이 사용한다.  

#### 동기 커밋

```java
configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // enable.auto.commit
```

```java
@PostConstruct
private void init() {
    if (consumeType.equals("auto-commit")) {
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    log.info(record.toString());
                }
                consumer.commitSync();
            }
        }).start();
    }
}
```

`records` 처리가 끝난 후 `consumer.commitSync()` 메서드로 offset을 커밋한다.  

메시지 처리 완료 후 커밋 과정을 진행하기 때문에 메시지 손실 가능성은 거의 없다.  
하지만 오토 커밋과 마찬가지로 `offset`을 옮기기 전 오류가 발생하면 메시지 중복 처리 장애가 발생할 수 있다.  

#### 비동기 커밋  

```java
@PostConstruct
private void init() {
    if (consumeType.equals("auto-commit")) {
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    log.info(record.toString());
                }
                consumer.commitAsync();
            }
        }).start();
    }
}
```

비동기 가져오기는 `consumer.commitSync()` 대신 `consumer.commitAsync()`를 사용한다.  
`consumer.commitAsync()`는 실패하더라도 재시도 과정 없이 바로 다음 코드를 진행한다.  

메시지 중복 장애는 커밋 순서에도 영향을 주는데 2번 커밋이 5번 커밋보다 나중에 실행되면 최종적으로 offset은 2번을 가리키게 된다.  
커밋 순서의 불일치는 커밋 실패 시 재실행 과정에서 많이 발생하므로 `consumer.commitAsync()`는 커밋 순서의 불일치 가능성을 많이 낮춘다고 볼 수 있다.  

### kafka admin

`consumer`, `producer` 연결이 생기는 순간 기본 설정으로 토픽과 `consumer group`이 자동으로 생성될 수 있다.  

자동 생성 말고 직접 지정해서 사용하고 싶다면 `kafka admin`을 사용해야 한다.  

다음과 같이 `AdminClient` 객체 생성  

```java
@Bean
public AdminClient kafkaConfig() {
    Map<String, Object> conf = new HashMap<>();
    conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServersConfig);
    conf.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
    AdminClient client = AdminClient.create(conf);
    return client;
}    
```

아래와 같이 정의된 API 메서드들을 호출해서 사용하면 된다.  

```java
public ListTopicsResult listTopics() {
    ListTopicsResult ltr = client.listTopics();
    return ltr;
}

public DescribeConfigsResult describeTopicConfigs(String topic) {
    ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
    // get the current topic configuration
    DescribeConfigsResult dcr = client.describeConfigs(Collections.singleton(resource));
    return dcr;
}

public CreateTopicsResult createTopics(String topic, Integer numPartitions, Short replicationFactor) {
    NewTopic newTopic = new NewTopic(topic, numPartitions, replicationFactor);
    CreateTopicsResult ctr = client
            .createTopics(
                    Collections.singleton(newTopic),
                    new CreateTopicsOptions().timeoutMs(10000) // create topic timeout
            );
    return ctr;
}

public DeleteTopicsResult deleteTopics(List<String> topics) {
    DeleteTopicsResult dtr = client.deleteTopics(topics);
    return dtr;
}

public AlterConfigsResult alterTopicConfigs(String topic) {
    ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
    // create a new entry for updating the retention.ms value on the same topic
    ConfigEntry retentionEntry = new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, "60000");
    Map<ConfigResource, Config> updateConfig = new HashMap<>();
    updateConfig.put(resource, new Config(Collections.singleton(retentionEntry)));
    AlterConfigsResult acr = client.alterConfigs(updateConfig);
    return acr;
}
```

## Spring Kafka 사용법

Spring Boot에서는 `spring-kafka` 의존성을 통해 Apache Kafka 클라이언트를 더 쉽게 사용할 수 있다.  
Spring Kafka는 `KafkaTemplate`과 `@KafkaListener` 어노테이션을 제공하여 메시지 발행과 수신을 간편하게 처리할 수 있다.

### 프로젝트 구조

멀티 모듈 프로젝트로 구성하여 Producer와 Consumer를 분리하고, 공통 로직은 `common` 모듈에 배치한다.

```
kafka-demo/
├── common/          # 공통 DTO, 설정
├── producer/        # 메시지 발행 모듈 (포트: 8081)
├── consumer/        # 메시지 수신 모듈 (포트: 8082)
└── docker/          # Docker Compose 설정
```

### KRaft 모드 설정

Kafka 2.8.0부터 Zookeeper 없이 동작하는 KRaft(Kafka Raft) 모드가 지원된다.  
KRaft 모드는 Zookeeper의 성능 한계를 극복하고 더 빠른 시작 시간과 더 나은 확장성을 제공한다.

```yaml
version: '3.8'
services:
  kafka:
    image: confluentinc/cp-kafka:8.1.0
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: 'broker,controller'  # 브로커와 컨트롤러 역할 모두 수행
      KAFKA_LISTENERS: 'SASL_PLAINTEXT://kafka:29092,CONTROLLER://kafka:29093,SASL_PLAINTEXT_HOST://0.0.0.0:9092'
      KAFKA_ADVERTISED_LISTENERS: 'SASL_PLAINTEXT://kafka:29092,SASL_PLAINTEXT_HOST://localhost:9092'
      KAFKA_CONTROLLER_QUORUM_VOTERS: '1@kafka:29093'  # KRaft 컨트롤러 쿼럼
      CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'
```

### SASL 인증 설정

프로덕션 환경에서는 SASL 인증을 통해 Kafka 접근을 제어한다.

**JAAS 설정 파일 (kafka_server_jaas.conf)**
```
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_producer="producer-secret"
    user_consumer="consumer-secret";
};
```

**Producer 설정**
```java
@Configuration
public class KafkaProducerConfig {
    @Value("${my.kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${my.kafka.producer.username:producer}")
    private String username;

    @Value("${my.kafka.producer.password:producer-secret}")
    private String password;

    public Map<String, Object> getConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        configs.put("sasl.mechanism", "PLAIN");
        configs.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                        "username=\"" + username + "\" password=\"" + password + "\";");
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.ACKS_CONFIG, "1");
        return configs;
    }
}
```

### Spring Kafka Producer

Spring Kafka는 `KafkaTemplate`을 통해 메시지 발행을 간편하게 처리한다.

**설정**
```java
@Profile("spring")
@Configuration
@EnableKafka
public class SpringKafkaProducerConfig {
    private final KafkaProducerConfig kafkaProducerConfig;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProducerConfig.getConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

**메시지 발행**
```java
@Component
public class SpringMessagePublisher extends AbstractMessagePublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    protected PublishResult doPublish(String topic, String key, String jsonMessage, String messageId) {
        try {
            SendResult<String, String> result;
            if (key != null) {
                result = kafkaTemplate.send(topic, key, jsonMessage).get(5, TimeUnit.SECONDS);
            } else {
                result = kafkaTemplate.send(topic, jsonMessage).get(5, TimeUnit.SECONDS);
            }
            
            return new PublishResult(
                    messageId,
                    result.getRecordMetadata().topic(),
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    Instant.now()
            );
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("메시지 발행 실패: " + e.getMessage(), e);
        }
    }
}
```

**REST API 컨트롤러**
```java
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final AbstractMessagePublisher messagePublisher;

    @PostMapping
    public String sendMessage(@RequestBody PublishMessageRequest request) {
        String messageId = UUID.randomUUID().toString();
        SampleMessageDto messageDto = new SampleMessageDto(
                messageId,
                request.message(),
                request.sender(),
                Instant.now(),
                request.priority()
        );
        
        PublishResult result = messagePublisher.publish(request.topic(), request.key(), messageDto);
        return String.format("메시지가 발행되었습니다 - messageId: %s, partition: %d, offset: %d", 
                result.messageId(), result.partition(), result.offset());
    }
}
```

### Spring Kafka Consumer

Spring Kafka는 `@KafkaListener` 어노테이션을 통해 메시지 수신을 간편하게 처리한다.

**설정**
```java
@Profile("spring")
@Configuration
@EnableKafka
public class SpringKafkaConsumerConfig {
    private final KafkaConsumerConfig kafkaConsumerConfig;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(kafkaConsumerConfig.getConfigs());
    }

    // 자동 커밋 (RECORD 모드)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    // 수동 커밋 (MANUAL 모드)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> manualKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
```

**자동 커밋 리스너**
```java
@Profile("spring")
@Component
public class SpringMessageListener extends AbstractMessageListener {
    
    @KafkaListener(
            topics = "test1",
            groupId = "test1-group"
    )
    public void consumeTest1Message(ConsumerRecord<String, String> record) {
        MessageMetadata metadata = new MessageMetadata(
                record.key(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp()
        );
        processMessage(record.value(), metadata);
        // auto commit이므로 수동 커밋 불필요
    }
}
```

**수동 커밋 리스너**
```java
@KafkaListener(
        topics = "test2",
        groupId = "test2-group",
        containerFactory = "manualKafkaListenerContainerFactory"
)
public void consumeTest2Message(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    try {
        MessageMetadata metadata = new MessageMetadata(
                record.key(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp()
        );
        processMessage(record.value(), metadata);

        // 메시지 처리 성공 후 수동 커밋
        acknowledgment.acknowledge();
    } catch (Exception e) {
        // 에러 발생 시 커밋하지 않음 (재처리를 위해)
    }
}
```

### 템플릿 메서드 패턴을 사용한 추상화

공통 로직(직렬화, 역직렬화)은 추상 클래스에 구현하고, 실제 발행/수신 로직만 하위 클래스에서 구현한다.

**AbstractMessagePublisher**
```java
public abstract class AbstractMessagePublisher {
    protected final ObjectMapper objectMapper;

    public PublishResult publish(String topic, String key, SampleMessageDto messageDto) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(messageDto);
            // 하위 클래스에서 구현한 발행 로직 실행
            PublishResult result = doPublish(topic, key, jsonMessage, messageDto.id());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("메시지 발행 실패: " + e.getMessage(), e);
        }
    }

    protected abstract PublishResult doPublish(String topic, String key, String jsonMessage, String messageId);
}
```

**AbstractMessageListener**
```java
public abstract class AbstractMessageListener {
    protected final ObjectMapper objectMapper;

    protected void processMessage(String rawMessage, MessageMetadata metadata) {
        try {
            SampleMessageDto messageDto = objectMapper.readValue(rawMessage, SampleMessageDto.class);
            handle(messageDto);
        } catch (Exception e) {
            log.error("메시지 처리 실패", e);
        }
    }

    public void handle(SampleMessageDto messageDto) {
        log.info("메시지 처리 - id: {}, message: {}", messageDto.id(), messageDto.message());
    }
}
```

### 프로필 기반 설정

Spring Profile을 사용하여 Apache Kafka 클라이언트와 Spring Kafka 중 하나를 선택할 수 있다.

**application.yml**
```yaml
spring:
  profiles:
    active: spring  # 또는 apache

my:
  kafka:
    bootstrap:
      servers: localhost:9092
    producer:
      username: producer
      password: producer-secret
    consumer:
      username: consumer
      password: consumer-secret
```

**프로필별 구현**
```java
@Profile("spring")
@Component
public class SpringMessagePublisher extends AbstractMessagePublisher {
    // Spring Kafka 구현
}

@Profile("apache")
@Component
public class ApacheMessagePublisher extends AbstractMessagePublisher {
    // Apache Kafka 클라이언트 구현
}
```

### AdminClient REST API

AdminClient를 통해 토픽 관리를 REST API로 제공한다.

```java
@RestController
@RequestMapping("/kafka")
public class MyAdminClientController {
    private final MyAdminClientComponent adminClientComponent;

    @GetMapping("/topics/list")
    public Map<String, TopicListing> listTopics() throws ExecutionException, InterruptedException {
        ListTopicsResult ltr = adminClientComponent.listTopics();
        return ltr.namesToListings().get();
    }

    @PostMapping("/topics")
    public void createTopics(@RequestBody CreateTopicsReq request) throws ExecutionException, InterruptedException {
        CreateTopicsResult ctr = adminClientComponent.createTopics(
                request.getTopic(),
                request.getNumPartitions(),
                request.getReplicationFactor());
        ctr.all().get();
    }

    @DeleteMapping("/topics/{topic}")
    public void deleteTopics(@PathVariable String topic) {
        adminClientComponent.deleteTopics(Collections.singletonList(topic));
    }
}
```

### 테스트

**메시지 발행**
```bash
curl -X POST http://localhost:8081/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "test1",
    "message": "테스트 메시지",
    "sender": "test-user",
    "priority": 1
  }'
```

**토픽 생성**
```bash
curl -X POST http://localhost:8081/kafka/topics \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "new-topic",
    "numPartitions": 3,
    "replicationFactor": 1
  }'
```

### 토픽 자동 생성

Kafka는 기본적으로 `auto.create.topics.enable=true` 설정이 활성화되어 있다.

**동작 방식:**
- 존재하지 않는 토픽에 메시지를 발행하면 **자동으로 토픽이 생성**된다
- 자동 생성된 토픽은 기본 설정으로 생성된다:
    - Partition 수: `num.partitions` (기본값: 1)
    - Replication Factor: `default.replication.factor` (기본값: 1)

**예시:**
```bash
# 존재하지 않는 토픽에 메시지 발행
curl -X POST http://localhost:8081/api/messages \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "new-topic",
    "message": "새 토픽 자동 생성 테스트",
    "sender": "test-user",
    "priority": 1
  }'

# 결과: "new-topic" 토픽이 자동으로 생성되고 메시지가 발행됨
```
