---
title:  "Spring Cloud - Spring Cloud Stream"
read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## Message Driven System

이벤트 기반 마이크로서비스의 일종  
서비스간 소비 가능한 이벤트를 주고받으며 서로 비동기 통신을 진행하는 시스템을 뜻한다.

![1](/assets/springboot/spring-cloud/springcloud_msg1.png)  

이 이벤트를 `Message System` 을 사용해 구현할 경우 `Message Driven System` 이라 할 수 있다.  

`Message System` 구성을 사용하려면 `Message Broker` 를 주로 사용하는데  
`Spring Boot` 에서 대표적으로 사용하는 `Message Broker` 로 아래와 같은 프로젝트들이 있는데  

- RabbitMQ  
- Apache Kafka  
- Kafka Streams  
- Amazon Kinesis  
- Google PubSub (partner maintained)  
- Solace PubSub+ (partner maintained)  
- Azure Event Hubs (partner maintained)  
- Azure Service Bus (partner maintained)  
- AWS SQS (partner maintained)  
- AWS SNS (partner maintained)  
- Apache RocketMQ (partner maintained)  

> 엄밀히 `Kafka` 분산 스트리밍 플랫폼이지만 여기선 단순 `Message Broker` 용도로 소개  
> `RabbitMQ` 는 같은 VMWare 에서 관리하는 오픈소스 프로젝트이다 보니 서로 많은 지원을 해주는 듯  

모든 프로젝트에서 Java Client 라이브러리를 제공하고 프로젝트에서 직접제공하는 라이브러리를 사용해도 되지만  
여기선 `Spring Cloud Stream` 라이브러리를 사용해 위 두 `Message Broker` 를 쉽게 `Spring Cloud` 환경에 통합시킬 수 있다.  

## Spring Cloud Stream

> <https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/>  
> <https://spring.io/projects/spring-cloud-stream>  

`Spring Cloud Stream` 를 사용하면 어떤 `Message Broker` 를 사용하던 상관없이  
동일한 코드를 통해 어플리케이션과 통합시킬 수 있다.  

`Kafka` 는 `Topic`, `Partition` `Consumer Group` 을 사용하고  
`RabbitMQ` 는 `Exchange`, `Queue`, `Consumer Group` 을 사용한다.  

`Kafka` `RabbitMQ` 외에도 각종 클라우드 서비스(SQS, Google PusSub) 를 `Spring Cloud Strema` 으로 추상화 할 수 있다.  

> 프로젝트의 모든 기능을 사용하고 싶다면 `Spring Cloud Stream` 가 아닌 개별적으로 제공되는 java client 라이브러리 사용을 권장  

각종 메세지, 이벤트 시스템을 하나의 코드로 통합하기 위해 `Spring Cloud Stream` 에선 아래 3가지 추상화 개념을 사용한다.  

- **Destination Binders**  
  외부 메시징 시스템 종류 상관없이 통합을 담당하는 구성 요소

- **Bindings**
  메시지의 생산자(Output Binding) 와 소비자(Input Binding)

- **Message**
  생산자와 소비자가 사용하는 표준 데이터 구조

![2](/assets/springboot/spring-cloud/springcloud_msg2.png)  
![3](/assets/springboot/spring-cloud/springcloud_msg3.png)  

여기선 `Destination Binders` 중 `RabbitMQ` 를 사용해본다.  

```groovy
implementation 'org.springframework.cloud:spring-cloud-stream'

implementation 'org.springframework.cloud:spring-cloud-starter-stream-rabbit'
```

### 단일 바인딩 - Account Service

`Account service` 는 `Order service` 와만 `Message` 통신하고  
`Order service` 는 `Account service`, `Product Service` 두개 서비스와 `Message` 통신한다.  

먼저 하나의 서비스와만 **단일 바인딩** 하여 통신하는 `Account service` 에 대해 구현한다.  

아래와 같이 `@EnableBinding` 어노테이션을 통해 `Message System` 사용을 명시  

```java
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Processor.class)
public class AccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }
}
```

아래 3가지 인터페이스를 제공한다.  

1. `Sink` - 메세지 `inbound` 채널을 생성하고 메세지 수신 서비스를 표시하는데 사용  
2. `Source` - 메세지 `outbound` 채널을 생성하고 메시지 송신 처리  
3. `Processor` - 메세지 송/수신 모두 필요한 경우 사용  

메세지 처리 여부에 따라 원하는 인터페이스를 지정하면 된다.  
대부분의 서비스가 송/수신을 모두 처리하기에 `Processor` 가 주로 쓰인다.

단일 바인딩에서 **메세시 송신**을 위한 컴포넌트는 아래와 같이 구현한다.  
우리는 위에서 `Processor` 를 사용하기로 하였기에 `Processor` 를 주입받는다.  

```java
@Component
@RequiredArgsConstructor
public class AccountMessageSender {
    private final Processor processor;

    public boolean send(String payload) {
        Message<String> message = MessageBuilder.withPayload(payload).build();
        return processor.output().send(message);
    }
}
```

`Processor` 는 `Spring Cloud` 의 메세지 송/수신을 위한 인터페이스로 아래와 같다.  
기본적으로 `output` `input` 이름의 `channel` 을 생성해서 각각 송신, 수신시에 사용하게된다.  

```java
package org.springframework.cloud.stream.messaging;

public interface Processor extends Source, Sink {
}

public interface Source {
    String OUTPUT = "output";

    @Output(Source.OUTPUT)
    MessageChannel output();
}

public interface Sink {
    String INPUT = "input";

    @Input(Sink.INPUT)
    SubscribableChannel input();
}
```

반대로 생성된 **메세지 수신** 을 위한 `listener` 코드를 구성해보자.  

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMessageHandler {

    private final ObjectMapper mapper;
    private final AccountService accountService;

    @StreamListener(Processor.INPUT)
    public void receiveOrder(Order order) throws JsonProcessingException {
        log.info("Order received: {}", mapper.writeValueAsString(order));
        Account account = accountService.findById(order.getAccountId());
        log.info("Account found: {}", mapper.writeValueAsString(account));
        order.setStatus(OrderStatus.ACCEPTED);
    }
}
```

`@StreamListener` 어노테이션으로 핸들링 처리하고 `@SendTo` 어노테이션으로 다시 바인딩으로 메세지를 전달 가능하다.  

기본적으로 `rabbitmq` 연결을 위한 설정과 기본 바인딩인 `output` 과 `input` 에 대한 이름설정을 해주어야 한다.  

```conf
spring.application.name=account-service

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
# for binding
spring.cloud.stream.bindings.output.destination=from-account
spring.cloud.stream.bindings.input.destination=to-account
# for rabbitmq custom
# default topic
spring.cloud.stream.rabbit.bindings.output.producer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.input.consumer.exchange-type=direct
# default '#'
spring.cloud.stream.rabbit.bindings.input.consumer.binding-routing-key=to-account
```

`Account Service` 를 실행해보면 아래와 같이 `Exchange` 와 `Queue` 가 생성된다.  

![1](/assets/springboot/spring-cloud/springcloud_msg4.png)  

![1](/assets/springboot/spring-cloud/springcloud_msg5.png)  

`rabbitmq` 를 `Destination Binders` 로 사용할 경우 기본 `exchange topic` 이다.  
그래서 `binding-routing-key` 역시 모든 메세지를 수신할 수 있는 `#` 을 사용하는데  

`exchange direct` 는 `#` 같은 와일드카드를 사용하지 않고 정확히 일치한 `routing key` 의 경우에만 메세지를 라우팅하기 때문에 `binding-routing-key` 를 별도 문자열로 지정해줘야 한다.  

`exchange direct` 메세지 송신시 별도의 설정을 하지 않으면 `destination` 이름을 사용기에 `to-account` 로 설정했다.  

### 멀티 바인딩 - Order Service

`Order service` 의 경우 `Account Service` 와 `Product Service`, 2개의 서비스와 연결되어야 함으로 단순 `Producer` 사용은 불가능하다.  

아래와 같이 별개의 `Producer(Sink, Source)` 바인딩이 구현될 수 있도록 `@Input`, `@Output` 어노테이션을 사용한다.  

```java
// AccountProducer.java
public interface AccountProducer {
    // account -> order
    String INPUT = "to-order-from-account";
    // order -> account
    String OUTPUT = "from-order-to-account";

    @Input(INPUT)
    SubscribableChannel subscribableChannel();

    @Output(OUTPUT)
    MessageChannel messageChannel();
}

// ProductProducer.java
public interface ProductProducer {
    // product -> order
    String INPUT = "to-order-from-product";
    // order -> product
    String OUTPUT = "from-order-to-product";

    @Input(INPUT)
    SubscribableChannel subscribableChannel();

    @Output(OUTPUT)
    MessageChannel messageChannel();
}
```

더이상 Producer 인터페이스를 사용하지 않고 별도의 커스텀 클래스를 통해 바인인처리 됨으로  
`main` 의 `@EnableBinding` 어노테이션 속성도 변경되어야 한다.  

```Java
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding({AccountProducer.class, ProductProducer.class})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

**메세지 송신**을 위한 컴포넌트는 아래와 같다.  

```java
@Service
public class OrderMessageSender {
    
    @Autowired
    @Qualifier(AccountProducer.OUTPUT)
    private MessageChannel accountMessageChannel;

    @Autowired
    @Qualifier(ProductProducer.OUTPUT)
    private MessageChannel productMessageChannel;

    public boolean sendToAccount(String payload) {
        Message<String> msg = MessageBuilder.withPayload(payload).build();
        return accountMessageChannel.send(msg);
    }
    
    public boolean sendToProduct(String payload) {
        Message<String> msg = MessageBuilder.withPayload(payload).build();
        return productMessageChannel.send(msg);
    }
}
```

**메세지 수신**을 위한 컴포넌트는 아래와 같다.  

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageHandler {

    private final ObjectMapper mapper;

    @StreamListener(ProductProducer.INPUT)
    public void receiveProductOrder(Order order) throws JsonProcessingException {
        log.info("Order receiveProductOrder: {}", mapper.writeValueAsString(order));
    }

    @StreamListener(AccountProducer.INPUT)
    public void receiveAccountOrder(Order order) throws JsonProcessingException {
        log.info("Order receiveAccountOrder: {}", mapper.writeValueAsString(order));
    }
}
```

`Account Service` 와 마찬가지로 생성된 바인딩에 대한 설정을 진행  

```conf
spring.application.name=order-service

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
# for account service binding
spring.cloud.stream.bindings.to-order-from-account.destination=from-account
spring.cloud.stream.bindings.from-order-to-account.destination=to-account
# for product service binding
spring.cloud.stream.bindings.to-order-from-product.destination=from-product
spring.cloud.stream.bindings.from-order-to-product.destination=to-product
# for rabbitmq custom
spring.cloud.stream.rabbit.bindings.to-order-from-product.consumer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.from-order-to-product.producer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.to-order-from-account.consumer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.from-order-to-account.producer.exchange-type=direct
# default '#'
spring.cloud.stream.rabbit.bindings.to-order-from-product.consumer.binding-routing-key=from-product
spring.cloud.stream.rabbit.bindings.to-order-from-account.consumer.binding-routing-key=from-account
```

실제 `direct exchange` 가 생성된것을 확인  

![6](/assets/springboot/spring-cloud/springcloud_msg6.png)  

`product-service` 와 `account-service` 역시 `order-service` 에게 `destination` 이름으로 메세지를 보내기에 `binding-routing-key` 를 설정해야 한다.  

### Consumer Group  

다수의 서비스와 다수의 인스턴스를 운영하려면 메세지 중복처리를 설계해야 한다.  
대부분의 Message Broker 가 메세지 로드벨런싱 기능을 지원하며 `rabbitmq` 의 경우 하나의 `Queue` 에 클라이언트들이 연결되면 된다.  

`Spring Cloud` 에선 이를 추상화하여 **Consumer Group** 기능을 제공한다.  

`Consumer Group` 설정없이 `Product Service` 를 2개 실행시키면 아래와 같이 2개의 Queue 가 생성된다.  

![7](/assets/springboot/spring-cloud/springcloud_msg7.png)  

두개의 `Product Service` 가 모두 메세지를 받게된다.  

`Product Service` 에 `Concumer Group` 설정

```conf
# for binding
spring.cloud.stream.bindings.output.destination=from-product
spring.cloud.stream.bindings.input.destination=to-product
# set consumer group
spring.cloud.stream.bindings.input.group=my-product-cg
# for rabbitmq custom
spring.cloud.stream.rabbit.bindings.output.producer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.input.consumer.exchange-type=direct
# default '#'
spring.cloud.stream.rabbit.bindings.input.consumer.binding-routing-key=to-product
```

![8](/assets/springboot/spring-cloud/springcloud_msg8.png)  

## 기타 메세지 기능

### Poller

`Poller` 매초에 하나씩 메세지를 지속석으로 보낼 수 있다.  
`MessageSource` 라는 빈 객체를 생성해야 하며 `fixedDelay` 밀리초에 메세지를 전송한다.  

```java
@Bean
@InboundChannelAdapter(value = AccountProducer.OUTPUT, poller = @Poller(fixedDelay = "3000", maxMessagesPerPoll = "1"))
// org.springframework.integration.core.MessageSource
public MessageSource orderSource() {
    log.info("orderSource invoked");
    return new MessageSource() {
        @Override
        public Message receive() {
            String result = "";
            Order order = Order.builder()
                .status(OrderStatus.NEW)
                .accountId((long) random.nextInt(3))
                .customerId((long) random.nextInt(3))
                .productIds(Collections.singletonList((long) random.nextInt(3)))
                .build();
            try {
                result = mapper.writeValueAsString(order);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
            return new GenericMessage(result);
        }
    };
}
```

### @Transformer

`@StreamListener` 어노테이션으로 바인딩으로부터 메세지를 핸들링하고  
`@SendTo` 어노테이션을 통해 바인딩으로 메세지 반환값을 전달한다.  

```java
@StreamListener(Processor.INPUT)
@SendTo(Processor.OUTPUT)
public Order receiveOrder(Order order) throws JsonProcessingException {
    ...
    ...
    return order;
}
```

`@Transformer` 어노테이션으로도 대체 가능하다.  

```java
@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
public Order receiveOrder(Order order) throws JsonProcessingException {
    ...
    ...
    return order;
}
```

### condition

`Order Service` 의 경우 멀티바인딩을 통해 2개의 `exchange` 를 만들어 메세지 처리를 진행했다.  

```java
@StreamListener(ProductProducer.INPUT)
public void receiveProductOrder(Order order) {
    ...
}

@StreamListener(AccountProducer.INPUT)
public void receiveAccountOrder(Order order) {
    ...
}
```

`@StreamListener` 의 `condition` 속성을 사용하면  
하나의 `Sink` 객체만 Bean 으로 등록해서 사용할 수 있다.  

```java
@StreamListener(value = Order.INPUT, condition = "headers['processor']=='product'")
public void receiveProductOrder(Order order) throws JsonProcessingException {
    ...
}

@StreamListener(value = Order.INPUT, condition = "headers['processor']=='account'")
public void receiveAccountOrder(Order order) throws JsonProcessingException {
    ...
}
```

## Spring Cloud Stream 3.x

> 공식 데모: <https://github.com/spring-cloud/spring-cloud-stream-samples>

`@EnableBinding`, `@Input`, `@Out`, `@StreamListener`, `@StreamMessageConverter` 등이 모두 `deprecated` 되었다.  

> 2023 년 기준 `Spring Cloud Stream` 은 최신버전은 `4.0.2`  

기존의 `Destination Binders` `Bindings` `Message` 개념, `Sink` `Source` `Processor` 개념은 동일하지만  
`spring-cloud-stream-reactive` 가 합쳐지면서 **함수형 프로그래밍 방식**으로 바인딩 처리를 진행하도록 변경되었다.  

아무런 설정 없이 `Spring Cloud` 의존성만 설정하고 `@Bean` 으로 함수형 객체를 등록하면 바인딩처리가 완료된다.  

> `@Bean name` 속성을 별도로 설정하지 않을경우 `method name` 이 사용된다.  

```java
@Bean(name = "account-producer")
public Function<String, Order> inputAndOutput() {
    return new Function<String, Order>() {
        @Override
        public Order apply(String msg) {
            log.info("received msg:{}", msg);
            try {
                Order order = mapper.readValue(msg, Order.class);
                log.info("Order received: {}", (order));
                Account account = accountService.findById(order.getAccountId());
                log.info("Account found: {}", mapper.writeValueAsString(account));
                order.setStatus(OrderStatus.ACCEPTED);
                return order;
            } catch (Exception e) {
                log.error("input error invoked, error type:{}, msg:{}", e.getClass().getSimpleName(), e.getMessage());
                return null;
            }
        }
    };
}
```

![8](/assets/springboot/spring-cloud/springcloud_msg9.png)  

바인딩 이름 규칙은 아래와 같다.  

- `<functionName> + -in- + <index>`  
- `<functionName> + -out- + <index>`  

해당 바인딩의 rabbitmq 커스터마이징을 하고싶다면 기존 방식대로 진행하면 된다.  

```conf
spring.application.name=account-service

spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
# for binding
spring.cloud.stream.bindings.account-producer-in-0.destination=to-account
spring.cloud.stream.bindings.account-producer-out-0.destination=from-account
spring.cloud.stream.bindings.account-producer-in-0.group=my-account-cg
spring.cloud.stream.bindings.account-producer-in-0.binder=rabbit
## for rabbitmq custom
spring.cloud.stream.rabbit.bindings.account-producer-in-0.consumer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.account-producer-out-0.producer.exchange-type=direct
#spring.cloud.stream.rabbit.bindings.account-sink-0.consumer.exchange-type=direct

## default '#'
spring.cloud.stream.rabbit.bindings.account-producer-in-0.consumer.binding-routing-key=to-account
```

기본적으로 생성되는 `account-producer-in-0`, `account-producer-out-0` 형식의 바인딩이름이 마음에 안들 수 있다.  

3.x 이전에는 `@Input`, `@Output` 어노테이션으로 바인딩 이름을 지정했었는데  
3.x 이후부턴 properties 를 통해 바인딩 이름을 지정할 수 있다.  
`spring.cloud.stream.function.bindings` 설정 참고  

> 가독성을 좋아질 수 있지만 변경으로 인해 에러를 유발할 수 있음으로 사용하지 않기를 권장한다  

```conf
# for naming binding
spring.cloud.stream.function.bindings.account-producer-in-0=to-account
spring.cloud.stream.function.bindings.account-producer-out-0=from-account
# for binding
spring.cloud.stream.bindings.to-account.destination=to-account
spring.cloud.stream.bindings.from-account.destination=from-account
spring.cloud.stream.bindings.to-account.group=my-account-cg
spring.cloud.stream.bindings.to-account.binder=rabbit
## for rabbitmq custom
spring.cloud.stream.rabbit.bindings.to-account.consumer.exchange-type=direct
spring.cloud.stream.rabbit.bindings.frmo-account.producer.exchange-type=direct
#spring.cloud.stream.rabbit.bindings.account-sink-0.consumer.exchange-type=direct
## default '#'
spring.cloud.stream.rabbit.bindings.to-account.consumer.binding-routing-key=to-account
```

`Sink` 생성을 원한다면 `Consumer` 함수객체를,  
`Source` 생성을 원한다면 `Supplier` 함수객체를 `Bean` 으로 등록하면 된다.  

```java
@Bean(name = "account-sink")
public Consumer<String> input() {
    return new Consumer<String>() {
        @Override
        public void accept(String msg) {
            System.out.println("Received: " + msg);
        }
    };
}
```

### 멀티 바인딩  

두개 이상의 함수형 객체를 Bean 으로 등록해서 바인딩으로 사용하고 싶다면 아래와 같이 `;` 구분자와 Function Name 을 `spring.cloud.function.definition` 에 지정해줘야 한다.  

```java
@Bean
public Consumer<Order> fromProduct() {
    return order -> log.info("Order fromProduct: {}", order);
}

@Bean
public Consumer<Order> fromAccount() {
    return order -> log.info("Order fromAccount: {}", order);
}
```

```conf
# function def for binding
spring.cloud.function.definition=fromAccount;fromProduct
# for account service binding
spring.cloud.stream.bindings.fromAccount-in-0.destination=from-account
# for product service binding
spring.cloud.stream.bindings.fromProduct-in-0.destination=from-product
```

### Functional Composition

```conf
spring.cloud.function.definition=toUpperCase|wrapInQuotes;
```

```java
@Bean
public Function<String, String> toUpperCase() {
    return s -> s.toUpperCase();
}

@Bean
public Function<String, String> wrapInQuotes() {
    return s -> "\"" + s + "\"";
}
```

![1](/assets/springboot/spring-cloud/springcloud_msg10.png)  

생성된 바인딩명이 난해하다.  
여기서 바인딩명을 명시하는 `spring.cloud.stream.function.bindings` 속성이 가독성에 도움될 수 있다.  

```conf
spring.cloud.function.definition=toUpperCase|wrapInQuotes;account-producer
spring.cloud.stream.function.bindings.toUpperCase|wrapInQuotes-in-0=upperAndWrapIn
spring.cloud.stream.function.bindings.toUpperCase|wrapInQuotes-out-0=upperAndWrapOut
```

### 메세지 송신  

`Spring Cloud Stream 3.x` 에서 메세지 송신만 하는 방법  

#### StreamBridge

`StreamBridge` 프레임워크를 사용해 직접 메세지를 전송할 수 있다.  

먼저 생성하지 못한 `Output` 용 바인딩을 생성해야 한다.  
함수형 메서드를 Bean 으로 등록해서 바인딩을 생성하는게 아니기 때문에 `spring.cloud.stream.source` 속성으로 직접 명시해줘야 한다.  

```conf
spring.cloud.stream.source=toAccount;toProduct
spring.cloud.stream.bindings.toAccount-out-0.destination=to-account
spring.cloud.stream.bindings.toProduct-out-0.destination=to-product
```

생성한 바인딩 이름을 기반으로 `send` 메서드를 호출한다.  

```java
@Service
public class OrderMessageSender {
    @Autowired
    private StreamBridge streamBridge;

    public boolean sendToAccount(String payload) {
        Message<String> msg = MessageBuilder
                .withPayload(payload)
                .build();
        return streamBridge.send("toAccount-out-0", msg);
    }

    public boolean sendToProduct(String payload) {
        Message<String> msg = MessageBuilder.withPayload(payload).build();
        return streamBridge.send("toProduct-out-0", msg);
    }

    @Bean
    @GlobalChannelInterceptor(patterns = "toProduct-*")
    public ChannelInterceptor customInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                ...
            }
        };
    }
}
```

`@GlobalChannelInterceptor` 어노테이션으로 `ChannelInterceptor` 을 Bean 으로 등록하면  
`send` 하기전에 `Message` 인터셉터 처리가 가능하다.  

#### poller - Supplier

지속적인 poller 형식의 메세지 전송의 경우 Supplier 를 사용한다.  
`Supplier` 객체를 바인딩으로 등록하면 `get()` 메서드가 1초에 한번씩 호출되고 문자열이 전송된다.  

```java
@Bean
public Supplier<String> stringSupplier() {
    return () -> "Hello from Supplier";
}
```

설정을 바꾸고 싶다면 `spring.cloud.stream.poller` 속성을 통해 `default` 값을 변경하면 된다.  

- **fixedDelay**: Fixed delay for default poller in milliseconds. Default: `1000L`  
- **maxMessagesPerPoll**: Maximum messages for each polling event of the default poller. Default: `1L`  
- **cron**: Cron expression value for the Cron Trigger. Default: `none`  
- **initialDelay**: Initial delay for periodic triggers. Default: `0`  
- **timeUnit**: The TimeUnit to apply to delay values. Default: `MILLISECONDS`  

```conf
spring.cloud.stream.poller.fixed-delay=2000
```

`Supplier` 의 경우 데이터 원본을 가지고 있는 서비스가 직접 호출하는 것이기 때문에 데이터 원본관리와 트리거 시점을 정해야 한다.  

### 다중 입력, 다중 출력

지금까지 바인딩 이름 뒤에 붙는 `<index>` 는 `Spring Cloud Stream 3.x` 이상부터 제공하는 **다중 입력, 다중 출력** 지원을 위한 것으로  
`Project Reactor`(Flux, Mono) 에서 제공하는 추상화에 의존하고 있다.  

```conf
spring.cloud.function.definition=gather;scatter
```

```java
@Bean
public Function<Tuple2<Flux<String>, Flux<Integer>>, Flux<String>> gather() {
    return new Function<>() {
        @Override
        public Flux<String> apply(Tuple2<Flux<String>, Flux<Integer>> tuple) {
            Flux<String> stringStream = tuple.getT1()
                .doOnNext(str -> log.info("first flux:{}", str));
            Flux<String> intStream = tuple.getT2()
                .doOnNext(num -> log.info("second flux:{}", num))
                .map(i -> String.valueOf(i));
            return Flux.merge(stringStream, intStream);
        }
    };
}
```

```java
@Bean
public static Function<Flux<Integer>, Tuple2<Flux<String>, Flux<String>>> scatter() {
    return new Function<Flux<Integer>, Tuple2<Flux<String>, Flux<String>>>() {
        @Override
        public Tuple2<Flux<String>, Flux<String>> apply(Flux<Integer> integerFlux) {
            Flux<Integer> connectedFlux = integerFlux.publish().autoConnect(2);
            UnicastProcessor even = UnicastProcessor.create();
            UnicastProcessor odd = UnicastProcessor.create();
            Flux<Integer> evenFlux = connectedFlux
                .filter(number -> number % 2 == 0)
                .doOnNext(number -> even.onNext("EVEN: " + number));
            Flux<Integer> oddFlux = connectedFlux
                .filter(number -> number % 2 != 0)
                .doOnNext(number -> odd.onNext("ODD: " + number));
            return Tuples.of(
                Flux.from(even).doOnSubscribe(x -> evenFlux.subscribe()),
                Flux.from(odd).doOnSubscribe(x -> oddFlux.subscribe())
            );
        }
    };
}
```

위 함수메서드가 Bean 으로 등록되면 아래 그림과 같은 입출력 바인딩이 생성된다.  

![11](/assets/springboot/spring-cloud/springcloud_msg11.png)  

다중 입력, 다중 출력이라고 크게 다를건 없다.  

`gather` 의 경우 두개의 입력 스트림을 한개의 출력 스트림으로 그대로 전송할 뿐이고  
`scatter` 의 경우 한개의 입력 스트림을 두개의 출력 스트림으로 나눠 전송한다.  
