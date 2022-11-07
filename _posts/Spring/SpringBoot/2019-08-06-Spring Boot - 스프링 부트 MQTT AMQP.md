---
title:  "Spring Boot - 스프링 부트 MTQQ, AMQP!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## MTQQ 개요

`Message Queuing Telemetry Transport`의 약자로서 많은 IoT 기기들에 최적화된 가벼운 메세징 프로토콜.  
기존에 웹에서 통신하던 HTTP등의 프로토콜보다 제한적이고 특수한 상황에서 사용할 수 있는 모바일 특화 프로토콜.  특히 IoT 영역에서 주목받고 있다.  

> https://wnsgml972.github.io/mqtt/mqtt.html

## MTQQ 구성

MQTT 프로토콜은 메시지를 해당 `Topic`에 발행(`Publish`) 하고 해당 `Topic`에 구독(`Subscribe`)하는 모양으로 이루어진다.

개설된 `Topic`에 `message`를 발행하면 해당 `Topic`을 구독하는 `client` 들에게 `message`를 전송,  
따라서 one to multi 또는 one to one message 전송을 모두 지원할 수 있다. 

![mqtt1](/assets/2019/mqtt1.png){: .shadow}  

`MQTT` 에선 서버역할로 `Borker`가 존재.    
발행자(`Publisher`)가 지정된 `Topic`에 대한 메세지를 `Broker`에게 전달하면 `Broker`에 붙어있는 여러 구독자(`Subscriber`)에게 해당 메세지를 전달한다.    


## QOS

![mqtt2](/assets/2019/mqtt2.png){: .shadow}  

그림과 같이 총 3개 레벨의 `QOS`가 있다.  

### Level 0 (At most once)

메시지는 한번만 전달된다. Fire and Forget이라고도 한다. 즉 보내고 잊는다. 한번만 전달하지만 전달여부는 확인하지 않는 레벨이다.  

### Level 1 (At least once)

메시지는 최소 한번은 전달된다. 유일하게 핸드셰이킹 같은 연결 여부를 확인하지 않고 메시지를 전달하는 레벨이다.
위에 그림을 보면 메시지를 성공적으로 전달하면 `Broker`가 `Publisher`에게 PUBACK을 보내어 전달 성공을 알리지만  
만약 정상적 통신이 이루어지지 않을 경우 `Loss`가 발생하여 `PUBACK`을 받지 못하여 `Publisher`는 적정 시간이 지나 실패로 알고 다시 메시지를 보내어  
`Subscribe`에게 중복메시지를 보내는 경우가 생기게 된다. (무료 라이브러리는 대부분 `Level1`까지 지원한다)   

### Level 2 (Exactly once)

메시지는 반드시 한번 전달된다. 위에 있는 `PUBACK` 과정을 `PUBREC`으로 핸드 셰이킹을 함으로서 메시지가 정확히 한번만 가는 레벨이다.
만약 위의 과정처럼 `Broker`가 `PUBREC`을 전달 받지 못해 `Loss`가 일어나게 되어도 `Broker`는 이미 보냈다는 사실을 알고 있기 때문에 새로 보내지 않는다.  

## Topic

구독자가 어떤 메세지를 구독할 건지, 발행자가 어떤 메세지를 발행할 건지 `Topic`을 통해 지정할 수 있다.  

토픽은 `/`로 구분된 계층구조를 갖으며 와일드카드문자를 지원한다.  

`computer/part/cpu`  
`computer/part/ram`  
`computer/part/gpu`  
`computer/part/cooler`  
위와같은 토필들이 존재할 때 `computer/part/*`로 토픽을 지정하면 4개의 토픽을 모두 구독할 수 있다.  

`*` 은 `/` 사이사이에도 들어갈 수 있다.  

`computer/part/cpu/intel`  
`computer/part/cpu/amd`  

만약 하위의 토픽들을 모두 선택하고 싶다면 `#`을 사용  

`computer/part/#` cpu의 하위까지 모두 구독한다.  

`+`는 1레벨의 모든 토픽을 의미하고 `computer/+/gpu` 이런식으로 사용 가능하다.  

## RabbitMQ

브로커 종류는 굉장히 많지만 `rabbitMQ`를 사용해보자.  

> https://www.rabbitmq.com/download.html
> https://www.rabbitmq.com/install-generic-unix.html

설치하는 여러방법 `docker`, `brew` 등이 있지만 컴파일된 바이너리 파일로 설치하는것이 깔끔하다.  
> 현버전 3.8.5


docker run -d -p 5672:5672 -p 15672:15672 -p 1883:1883 \
--restart=unless-stopped --name rabbitmq \
-e RABBITMQ_DEFAULT_USER=beyless \
-e RABBITMQ_DEFAULT_PASS=ws-beyless \
-v /data/rabbitmq/lib:/var/lib/rabbitmq \
-v /data/rabbitmq/log:/var/log/rabbitmq rabbitmq:management


<!-- > https://developer.emqx.io/docs/broker/v3/en/install.html#macos

```
emqx start
emqx stop
``` -->

대충 적절한 위치에 압출 풀어서 저장(`/usr/local/etc`) 및 실행  
```
./rabbitmq-plugins enable rabbitmq_management
./rabbitmq-plugins enable rabbitmq_mqtt
./rabbitmq-server

  ##  ##      RabbitMQ 3.8.5
  ##  ##
  ##########  Copyright (c) 2007-2020 VMware, Inc. or its affiliates.
  ######  ##
  ##########  Licensed under the MPL 1.1. Website: https://rabbitmq.com
```


`rabbitMQ`는 저전력모델을 위한 `MQTT` 브로커가 아닌 단순 메세지 브로커 역할로 `AMQP` 라는 프로토콜을 지원하는 브로커이다.  

플러그인을 사용하면 `MQTT` 프로토콜을 지원할 수 있다.   
> https://www.rabbitmq.com/mqtt.html

### MQTT 설정  

```conf
# /usr/local/etc/rabbitmq/etc/rabbitmq/rabbitmq.conf

mqtt.listeners.tcp.default = 1883
## Default MQTT with TLS port is 8883
# mqtt.listeners.ssl.default = 8883

# anonymous connections, if allowed, will use the default
# credentials specified here
mqtt.allow_anonymous  = true
mqtt.default_user     = guest
mqtt.default_pass     = guest

mqtt.vhost            = /
mqtt.exchange         = amq.topic
# 24 hours by default
mqtt.subscription_ttl = 86400000
mqtt.prefetch         = 10
```

`rabbitmq.conf` 파일 생성후 서버 재실행  

### rabbitMQ dashboard


`rabbitmq_management` 플러그인 설치했다면 대시보드 접속이 가능할 것이다.  

서버를 실행하고 http://localhost:15672/#/ 에 접속.  

기본 `id/pw`는 `guest/guest` 이다.  

계정을 추가하고 싶다면 아래처럼 설정  

```
$ ./rabbitmqctl add_user new_user
$ ./rabbitmqctl set_user_tags new_user administrator
```

### MQTT Client - publisher, consumer 

`MQTT Client` 프로그램으로 `mosquitto` 설치하고 간단히 `topic` 을 지정해 `message` 을 발행, 구독해보자.  

```
$ brew install mosquitto
```

```
$ mosquitto_sub -h 127.0.0.1 -p 1883 -t test_topic -q 2
```

`mosquitto_sub` 를 통해 `test_topic` 이름으로 구독


```
$ mosquitto_pub -h 127.0.0.1 -p 1883 -t test_topic -m "Hello RabbitMQ"
```

다른 터미널을 띄우고 `mosquitto_pub` 을 통해 `test_topic` 이름으로 메세지 발행


![mqtt3](/assets/2019/mqtt3.png){: .shadow}  

`mosquitto_sub` 을 2개 실행해 두면 위처럼 바인딩된 큐가 2개 생성된것을 확인할 수 있다.  

### rabbitMQ MQTT 작동 방식  

> https://www.rabbitmq.com/mqtt.html#implementation

사실 `rabbitMQ`를 사용하려는 가장 큰 이유는 `MQTT client` 들과 `AMQP client` 들이 연동되기 때문이다.  

`MQTT` 는 기본적으로 `1:N` 방식으로 `multicast` 형식으로 이루어지기 때문에 중복처리 방지가 불가능하다.  

중복처리를 위해서 `rabbitMQ`의 시스템을 사용해  
메세지를 처리하는 `consumer` 들은 `AMQP Client` 로,  
발행자들은 `MQTT client` 로 등록하여 설정해보자.  

## Java 와 MQTT

우선은 `AMQP` 외에 `MQTT - MQTT` 형식으로 연결을 구성해보자.  

`MQTT`는 디바이스, 네트워크 환경에 구애받지 않은 프로토콜로 `c/c++`, `python`, `java` 등 여러 언어를 지원하고 수많은 엔드포인트 디바이스가 사용가능한 프로토콜이다.  

서버에서 `Java`로 어떻게 `MQTT` 구독자, 발행자를 만들고 브로커와 연결하는지 알아보자.  

`MQTT` 클라이언트를 위한 `dependency` 설정  

```xml
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.0</version>
</dependency>
```

```java
@Slf4j
@Component("mqttComponent")
public class MqttComponent {

    private String brokerAddress = "tcp://localhost";

    private String id = "guest";
    private String password = "guest";
    @Value("${client.id}")
    private String clientID;

    @PostConstruct
    public void init() {
        log.info("MQTT init begin.");
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient mqttClient = new MqttClient(brokerAddress, clientID, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false); //기존에 있던 세션을 지움(구독중인 메세지, 구독옵션등 모두 사라짐)
            connOpts.setConnectionTimeout(10); //10초동안 연결되지 않을경우 타임아웃
            connOpts.setKeepAliveInterval(3);
            connOpts.setAutomaticReconnect(true); //클라이언트가 서버를 찾지 못할경우 자동 재연결
            connOpts.setUserName(id);
            connOpts.setPassword(password.toCharArray());
            mqttClient.setCallback(new MessageCallback());
            mqttClient.connect(connOpts);
            String[] topics = {
                    "/computer/part/cpu",
                    "/computer/part/monitor",
                    "/computer/part/keyboard",
                    "/computer/part/gpu",
                    "/computer/part/ram",
            };
            int[] qos = {1, 1, 1, 1, 1};
            mqttClient.subscribe(topics, qos); //모든 구독에 대해서 qos 레벨 1로 설정
            log.info("MQTT init success.");
        } catch (MqttException e) {
            log.error("MQTT init failed BROKER_ADDRESS = " + brokerAddress + " error :" + e.getMessage());
        }
    }
}
```

```java
@Slf4j
public class MessageCallback implements MqttCallback {
    @Override
    public void connectionLost(Throwable cause) {
        log.info("connection lost.....");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info(message.getId());
        log.info("message arrived " + new String(message.getPayload(), "UTF-8"));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("delivery complete....");
    }
}
```

cli환경에서 `mosquitto` 클라이언트를 사용해서 `topic`에 해당하는 `MQTT` 메세지 발행
```
> mosquitto_pub -h 127.0.0.1 -p 1883 -t /computer/part/cpu -q 1 -m "intel cpu"
> mosquitto_pub -h 127.0.0.1 -p 1883 -t /computer/part/keyboard -q 1 -m "ducky"
> mosquitto_pub -h 127.0.0.1 -p 1883 -t /computer/part/gpu -q 1 -m "amd"
> mosquitto_pub -h 127.0.0.1 -p 1883 -t /computer/part/ram -q 1 -m "samsung"
```

출력값
```
message arrived intel cpu
message arrived ducky
message arrived amd
message arrived samsung
```

<!-- 
> 보안설정을 통해서 로그인한 사용자만 mqtt메세지를 발행할 수 도 있다.  
> 구독자나 발행자나 모두 클라이언트이다. `mqttClient`의 `publish("topic", "message")` 메서드를 통해 다른 mqtt클라이언트에게 메세지 발행이 가능하다.  


하나의 clientID로 여러번 connect하면 순간 연결을 될 수도 있다. (물론 안될수도 있음)  
그리고 `MqttCallback` 클레스에서 `connectionLost()`가 바로 호출하게 된다.  
(하나의 클라이언트ID는 하나의 서버에서만 사용하자.)

cmd 실행시 docker가 바로 꺼져버림

```
docker run -dit --restart=always --name emq \
-p 18083:18083 \
-p 8083:8083 \
-p 1883:1883 \
--hostname emq emqttd:2.3.11 /bin/bash

/emqttd/bin/emqttd start
```

귀찮지만 일일이 들어가서 start해주자.  

```
docker run -dit --restart=always --name emq-sub1 \
-p 18082:18083 \
-p 8082:8083 \
-p 1882:1883 \
--hostname emq-sub1 emqttd:2.3.11 /bin/bash

docker run -dit --restart=always --name emq-sub2 \
-p 18081:18083 \
-p 8081:8083 \
-p 1881:1883 \
--hostname emq-sub2 emqttd:2.3.11 /bin/bash
```

erl -name emqtt@127.0.0.1
erl -name emqtt-sub1@127.0.0.1
erl -name emqtt-sub2@127.0.0.1


```conf
FROM ubuntu:16.04
MAINTAINER "kouzie"

RUN ["apt-get", "update"]
RUN ["apt-get", "install", "-y", "wget"]
RUN ["apt-get", "install", "-y", "net-tools"]
RUN ["apt-get", "install", "-y", "iputils-ping"]
RUN ["apt-get", "install", "-y", "vim"]
RUN ["apt-get", "install", "-y", "erlang"]
# RUN "apt-get update & apt-get install lksctp-tools"
COPY emqttd/ /emqttd/
COPY .erlang.cookie /root/.erlang.cookie
# emqx will occupy these port:
# - 1883 port for MQTT
# - 8080 for mgmt API
# - 8083 for WebSocket/HTTP
# - 8084 for WSS/HTTPS
# - 8883 port for MQTT(SSL)
# - 11883 port for internal MQTT/TCP
# - 18083 for dashboard
# - 4369 for port mapping
# - 5369 for gen_rpc port mapping
# - 6369 for distributed node
EXPOSE 1883 8080 8083 8084 8883 11883 18083 4369 5369 6369

# WORKDIR /usr/bin/emqttd

# CMD [ "service", "emitted", "start"]
```


~/.erlang.cookie 파일 권한 변경후 beyless로 수정 후 다시 400 권한으로 변경  

아래 명령으로 쿠키확인  
erlang:get_cookie().

서로 연결가능한지 erlang으로 접속후 ping태스트
net_adm:ping('host@ipaddress').

/emqttd/bin/emqttd_ctl cluster join emq-main@172.17.0.2
/emqttd/bin/emqttd_ctl cluster join emq-sub1@172.17.0.3
/emqttd/bin/emqttd_ctl cluster join emq-sub2@172.17.0.4

참고로 join 순서가 중요

main과 sub1를 묶었다면 sub2와 sub1를 묶어라 아마? 여러번의 시행착오 필요  

실패했다면 기존정보 rm -rf /emqttd/data/mnesia/* 를 지우고 시작  -->

아래와 같이 하나의 인스턴스를 추가 실행해서 동작시켜 놓으면 2개의 서버에서 모두 메세지를 받아 출력한다.  

```
$ java -Dclient.id=consumer2 -Dserver.port=9080 -DINSTANCE=1 -jar target/mtqq-0.0.1-SNAPSHOT.jar
```

![mqtt4](/assets/2019/mqtt4.png){: .shadow}  

실제로 client 별로 큐가 하나씩 생성되어 있고 수많은 라우팅 키가 각 큐를 가리키고 있다.  
또한 `topic` 이름을 `/computer/part/cpu` 설정했는데 `.computer.part.cpu` 로 변경되어 있다.  

`exchange` 에서 `MQTT` 토픽 라우팅 룰을 그대로 사용하지 않고 `AMQP` 방식의 라우팅 룰로 변환된다.  



## AMQP 클라이언트

`publisher` 는 여전히 `mqtt` 를 사용하면 `mosquitto`로 설정하고 `consumer` 는 `rabbit` 과 `AMQP` 로 연결할 수 있도록 설정  

> sample code: https://www.rabbitmq.com/api-guide.html  
> rabbitMQ API: https://rabbitmq.github.io/rabbitmq-java-client/api/current/index.html  

아래에 쓰이는 각종 메서드는 위의 API문서에 자세히 나와있다.  

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```


```java
@Profile("!mqtt") // profile mqtt 일 경우 생성하지 않음 
@Configuration
public class AMQPConfig {

    private String userName = "guest";
    private String password = "guest";
    private String hostName = "127.0.0.1";
    private int portNumber = 5672;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        // "guest"/"guest" by default, limited to localhost connections
        factory.setUsername(userName);
        factory.setPassword(password);
        factory.setVirtualHost("/");
        factory.setHost(hostName);
        factory.setPort(portNumber);
        return factory;
    }
}
```

```java

@Slf4j
@Profile("!mqtt")
@Component
public class AMQPComponent {

    @Autowired
    private ConnectionFactory factory;
    @Value("${client.id}")
    private String clientId;

    private String exchangeName = "amq.topic";
    private Channel channel;

    @PostConstruct
    public void init() {
        log.info("AMQPComponent init begin. " + clientId);
        try {
            String queueName = "mqtt_to_amqp";
            log.info("init queueName:" + queueName);
            Connection conn = factory.newConnection();
            channel = conn.createChannel();
            //queueName, durable,  exclusive, autoDelete, arguments
            queueName = channel.queueDeclare(queueName, true, false, false, null).getQueue();
            // queueName: 큐의 이름
            // durable: 서버 재시작에도 살아남을 튼튼한(?) 큐로 선언할 것인지 여부
            // exclusive: 현재의 연결에 한정되는 배타적인 큐로 선언할 것인지 여부
            // autoDelete: 사용되지 않을 때 서버에 의해 자동 삭제되는 큐로 선언할 것인지 여부
            // arguments: 큐를 구성하는 다른 속성
            log.info("bind queueName:" + queueName);
            channel.queueBind(queueName, exchangeName, ".computer.part.cpu");
            channel.queueBind(queueName, exchangeName, ".computer.part.monitor");
            channel.queueBind(queueName, exchangeName, ".computer.part.keyboard");
            channel.queueBind(queueName, exchangeName, ".computer.part.gpu");
            channel.queueBind(queueName, exchangeName, ".computer.part.ram");
            // '/' -> '.' 으로 topic 이 변환되기 때문에 
            // channel.queueBind(queueName, exchangeName, ".computer.part.*"); 와일드 카드 적용도 가능
            // kafka 에선 일정 시간마다 받은 메세지 한번에 수신확인 처리하는데 rabbitMQ에서도 비슷한듯?
            // 수동으로 수신처리함수 호출기위해 설정
            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, new AMQPConsumer(channel));
            // channel.basicCancel(queueName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public boolean publishingMessage(String message, String routingKey) {
        try {
            byte[] messageBodyBytes = message.getBytes();
            boolean mandatory;
            // null부분은 basicProperties 가 들어가는데 header 와 같은 역할이다.
            channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
```

```java
@Slf4j
public class AMQPConsumer extends DefaultConsumer {

    public AMQPConsumer(Channel channel) {
        super(channel);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String routingKey = envelope.getRoutingKey();
        String contentType = properties.getContentType();
        long deliveryTag = envelope.getDeliveryTag();
        // (process the message components here ...)
        log.info("message body:" + new String(body));
        // 메세지 수신여부를 broker 에게 알림
        this.getChannel().basicAck(deliveryTag, false);
    }
}
```

![mqtt1](/assets/2019/mqtt5.png){: .shadow}  

위에서 지정한 `mqtt_to_amqp` `exchange` 를 보면 설정한 큐, `routing key` 가 매핑되어 있음을 확인 가능하다.  

![mqtt1](/assets/2019/mqtt6.png){: .shadow}  

동시에 2개의 `channel` 연결시에도 하나의 큐에 2개의 `channel` 이 붙어 중복처리 되지 않는다.  

마찬가지로 `mosquitto_pub` 명령어로 테스트 진행  

### Spring AMQP

위처럼 `rabbitMQ` 라이브러리에서 지원해주는 `ConnectionFactory`, `Connection`, `Channel` 을 가져다 쓰는것도 좋지만 `spring-boot-starter-amqp` 에서 제공하는 스프링에 조금더 추상화된 클래스들을 사용하는 것도 좋다.  

> https://www.rabbitmq.com/tutorials/tutorial-five-spring-amqp.html

```java
@Slf4j
@Profile("spring-amqp")
@Configuration
public class SpringAMQPConfig {

    String queueName = "mqtt_to_amqp";

    @Bean
    public TopicExchange exchangeTopic() {
        return new TopicExchange("amq.topic");
    }

    @Bean
    public Queue queue() {
        // durable, exclusive, autoDelete
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public Binding binding(TopicExchange exchangeTopic, Queue queue) throws JsonProcessingException {
        log.info("biding invoked, queueName:" + queue.getName());
        return BindingBuilder.bind(queue).to(exchangeTopic).with(".computer.part.*");
    }

    @Bean
    public SpringAMQPReceiver springAMQPReceiver() {
        return new SpringAMQPReceiver();
    }

    @Bean
    public SpringAMQPSender springAMQPSender() {
        return new SpringAMQPSender();
    }
}
```

전체적인 구성은 비슷하다, `channel` 생성/연결 과정이 따로 없을뿐 `exchange` 생성, `queue`생성 및 바인딩 처리하는 것은 똑같다.  

단점은 바인딩처리를 한번에 못하고 `Binding`객체를 모두 `bean` 으로 등록해야 한다.  

사용되는 패키지명이 조금씩 다르다.  

```java
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
```

메세지 수신처리는 아래와 갔다.  

```java
@Slf4j
public class SpringAMQPReceiver {

    // bean factory 에서 queue 이름의 빈객체를 찾아 등록
    @RabbitListener(queues = "#{queue.name}")
    public void receive(byte[] payload) throws InterruptedException {
        log.info("message:" + new String(payload));
    }
}
```

`@RabbitListener` 어노테이션으로 빈으로 등록한 큐를 지정하고 처리하면 된다.  

AMQP 메세지는 전송하는 Sender 클래스는 아래와 같다.  

```java
public class SpringAMQPSender {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private TopicExchange topic;

    AtomicInteger index = new AtomicInteger(0);
    AtomicInteger count = new AtomicInteger(0);

    private final String[] topics = {
            ".computer.part.cpu",
            ".computer.part.monitor",
            ".computer.part.keyboard",
            ".computer.part.gpu",
            ".computer.part.ram"};

    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    public void send() {
        if (this.index.incrementAndGet() == topics.length) this.index.set(0);
        String key = topics[this.index.get()];
        String message = "Hello RabbitMQ, key:" + key + ", index:" + index;
        template.convertAndSend(topic.getName(), key, message.getBytes());
        System.out.println("send message:" + message);
    }
}
```

`topic` 을 변경해가며 `AMQP` 메세지를 전송한다.  

`mosquitto_sub` 으로 `SpringAMQPSender` 가 보내는 메세지 확인  

```
$ mosquitto_sub -h 127.0.0.1 -p 1883 -t "/computer/part/*" -q 2
Hello RabbitMQ, key:.computer.part.monitor, index:1
Hello RabbitMQ, key:.computer.part.keyboard, index:2
Hello RabbitMQ, key:.computer.part.gpu, index:3
Hello RabbitMQ, key:.computer.part.ram, index:4
...
...
```
