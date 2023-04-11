---
title:  "Spring Cloud - ribbon!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## ribbon

기존에 `eureka-zuul`을 사용해 로드밸런싱을 지원했었다.  

> https://kouzie.github.io/spring/스프링-클라우드-eureka-zuul  

이는 `zuul`에 기본적으로 `ribbon`이 포함되었기 때문에 가능한 작업이다.  

이번엔 별도의 gateway를 사용하지 않고 유레카 `Service Discovery`기능과 `RestTemplate`을 사용해 `ribbon`을 사용해보자.  

### 유레카 서버 설정

ribbon을 떠나 서비스를 등록하기 위한 서버를 설정하자.  
아주 기본적인 설정만 적용한다.  

```conf
spring.application.name=server
server.port=8761
# spring.profiles.active=peer1

# 유레가 서버의 보호모드를 off해 90초동안 인스턴스 유지를 없애고 eviction-interval-timer 기본값인 60초 동안기다리도록 설정
eureka.server.enable-self-preservation=false

# 60초가 너무 길기때문에 3초로 설정
eureka.server.eviction-interval-timer-in-ms=3000
# client 하트비드는 1초마다 도착하고 2초가 지나면 클라이언트의 서비스 지원을 중지하고 하트비트가 오지 않고 3초가 지나면 서비스를 퇴거(삭제)한다

# 시큐리티 설정, eureka name/pw을 알아야 접근 가능하다.
spring.security.user.name=admin
spring.security.user.password=qwer

# 암호를 설정했다면 자신에게도 defaultZone에 계정과 비번을 설정한 url을 지정해야 서버에서 오류가 발생하지 않는다.
# 아무리 fetch, register옵션을 false로 지정해도.... (자기 자신에게도 지속적으로 연결 상태를 체크하기때문)
eureka.client.service-url.defaultZone=http://admin:qwer@localhost:8761/eureka/

# 등록 이후 Instance 정보가 변경 되었을 때 Registry 정보를 갱신하기 위한 REST를 2초마다 호출
eureka.client.instance-info-replication-interval-seconds=2

eureka.client.fetch-registry=false
eureka.client.register-with-eureka=false
```  

메인 클래스는 아래와 같이 설정   
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaserverApplication.class, args);
    }
}
```

유레카 서버에선 `ribbon`으로 인한 추가 변경은 일어나지 않는다.  

### 유레카 클라이언트

이 글의 주제가 서비스(유레카 클라이언트)들 간의 `ribbon`, `RestTemplate`을 사용한 부하분산 처리된 커뮤니케이션이다.  

전체적인 구조는 아래 그림과 같다.  

![ribbon1](/assets/2019/ribbon1.png){: .shadow}  

모두 유레카 클라이언트 서비스이며 다음과 같이 `application.properties`파일 구성

```conf
eureka.client.service-url.defaultZone=http://admin:qwer@localhost:8761/eureka/
management.endpoint.shutdown.enabled=true
management.endpoint.shutdown.sensitive=false
management.endpoints.web.exposure.include=*
# 이를 방지하기 위해 1초마다 heartbeat를 전송하고
eureka.instance.lease-renewal-interval-in-seconds=1
# heartbeat 못받고 2초가 지나가면 인스턴스가 제거된다. 더이상 클라이언트로 서비스를 서버가 보내지 않는다
eureka.instance.lease-expiration-duration-in-seconds=2
# 10 초마다 eureka server 로부터 등록된 서비스 패치 기본 30 초
eureka.client.registry-fetch-interval-seconds=10
# 유레카 서버에 본인 서비스를 등록할 건지 여부
eureka.client.register-with-eureka=true
# 유레카 서버로부터 서비스 목록을 로컬 캐시에 저장할 건지 여부, 둘 다 기본값 true라서 지정하지 않아도 상관 없다.
eureka.client.fetchRegistry=true

...

server.port=${PORT:8093}
spring.application.name=product-service

...

spring.application.name=customer-service
server.port=${PORT:8092}

...

spring.application.name=account-service
server.port=${PORT:8091}

...

spring.application.name=order-service
server.port=${RORT:8090}
```

> 중복되거나 부가적인 설정은 생략.  

### Ribbon Client

client4개를 동작시키면 아래 그림과 같이 구성될 것이다.  

![ribbon2](/assets/2019/ribbon2.png){: .shadow}  

위의 구조 그림을 보면 `order service`에서 기타 3개의 `service`의 REST API를 모두 호출해야 한다.  

이를 위해 `RestTemplate`스프링 빈 객체를 생성하고 각 서비스에 해당하는 `Ribbon Client`를 생성한다.  

```java

@SpringBootApplication
@EnableDiscoveryClient
@RibbonClients({
        @RibbonClient(name = "account-service"),
        @RibbonClient(name = "customer-service"),
        @RibbonClient(name = "product-service")
})
public class OrderApplication {

    @LoadBalanced
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

아래의 방식으로 등록한 `Ribbon Client`의 REST API 호출

```java
Product[] products = restTemplate.postForObject("http://product-service/ids", order.getProductIds(), Product[].class);
Customer customer = restTemplate.getForObject("http://customer-service/withAccounts/{id}", Customer.class, order.getCustomerId());
```

`restTemplate`을 통해 호출한 url을 보면 ip나 dns가 아닌 서비스명을 사용한다.  
이미 유레카 서버를 통해 각종 서비스를 `Ribbon Client`로 보유중이다.  

또한 `Ribbon Client`를 사용하면 자동적으로 로드밸런싱이 가능하다. 유레카에 올라가있는 서비스중 `product-service`, `account-service`는 2개씩 올라가 있는데 Order컨트롤러에서 마지막 호출했던 인스턴스를 기억하고 한번씩 번갈아서 한번씩 호출된다.  


## Feign Client

위의 `Ribbon Client`로도 충분히 마이크로 서비스 운영이 가능하지만 `Feign Client` 좀더 구조적으로 관리 가능하다.  

```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
```

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
// @RibbonClients({
//         @RibbonClient(name = "account-service"),
//         @RibbonClient(name = "customer-service"),
//         @RibbonClient(name = "product-service")
// })
public class OrderApplication {
    ...
}
```
기존의 `@RibbonClients`어노테이션을 주석처리하고 `@EnableFeignClients` 추가  

그리고 위에서 `restTemplate`을 사용해 인스터스를 호출하였던 코드를 `Feign` 인터페이스를 통해 정의한다.   

```java
@FeignClient(name = "customer-service")
public interface CustomerClient {
    // restTemplate.getForObject("http://customer-service/withAccounts/{id}", Customer.class, order.getCustomerId());
    @GetMapping("/withAccounts/{id}")
    Customer findByIdWithAccounts(@PathVariable("id") Long id);
}

@FeignClient(name = "product-service")
public interface ProductClient {
    //restTemplate.postForObject("http://product-service/ids", order.getProductIds(), Product[].class);
    @PostMapping("/ids")
    List<Product> findByIds(List<Long> ids);
}
```

정의한 `FeignClient` 인터페이스는 스프링 프레임워크에 의해 재정의 되며 `@Autowired` 통해 의존성 주입후에 사용하면 된다.  

```java
@Autowired
CustomerClient customerClient;

@Autowired
ProductClient productClient;
...
...

List<Product> products = productClient.findByIds(order.getProductIds());
Customer customer = customerClient.findByIdWithAccounts(order.getCustomerId());
```

### 주의사항  

더이상 `restTemplate`과 반환값, 매개변수등을 설명할 필요 없이 정의해둔 `interface`의 메서드를 재사용하면 된다.  
하지만 `FeignClient`역시 내부적으론 `Eureka Client` 와 `Ribbon`을 합쳐서 사용한다.  

> Get 방식의 `feign client api` 를 호출할 경우 `query parameter` 을 위한 `POJO` 객체 사용시 `@ModelAttribute` 가 아닌 `@SpringQueryMap` 사용한다.  
```java
ActivityDetailResponseDto getActivityDetail(@SpringQueryMap DetailRequestDto requestDto);
```

> `Feign` 과 `apacheHttpClient(default)` 사용시 `patch` 메서드 사용이 불가능하다. 사용하고 싶다면 `OkHttpClient` 로 변경해야 한다.  
```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
    implementation 'io.github.openfeign:feign-okhttp'
    ...
    ...
}
```

### 여러개의 FeignClient 에서 여러개의 interface 상속  

같은 서비스를 가리키는 Feign client 인터페이스를 여러개 생성 불가능하다.  

```java
@FeignClient(name = "product")
public interface ProductClient1 {
}


@FeignClient(name = "product")
public interface ProductClient2 {
}
```

```
The bean 'product.FeignClientSpecification' could not be registered. A bean with that name has already been defined and overriding is disabled.
```

`product.FeignClientSpecification` 라는 이름의 `bean` 이 이미 등록되어 있음으로 추가 등록되지 않는다고 출력된다.  


msa 에서 하나의 서버 하나의 컨트롤러가 원칙이지만 컨트롤러가 너무 커지는 것 같아 `컨트롤러 분리 + feign client` 를 사용할 수 있도록 설정해보자.  

```java
@FeignClient(name = "product", contextId = "ProductClient1")
public interface ProductClient1 {
}


@FeignClient(name = "product", contextId = "ProductClient1")
public interface ProductClient2 {
}
```

`contextId` 속성을 통해 `bean` 의 `contextId` 를 다르게 설정한다.

## hoverfly

`ribbon`은 `default` 부하분산 룰로 `RoundrobinRule`을 사용한다.(각 서비스마다 한번씩)

해당 부하분산 룰을 `WeightedResponseTimeRule`로 변경해보자(평균 응답시간에 따라 호출비율 결정)  


테스트할 클라우드 서비스의 테스트를 위해 `hoverfly`를 사용한다.   

여러 방법으로 `REST Api`를 테스트 하기위한 오픈소스 프로젝트, `proxy`객체를 통해 `request`, `response` 과정을 둘러싸 원하는 테스트를 진행할 수 있다. (응답시간 변경 등)  

> https://docs.hoverfly.io/projects/hoverfly-java/en/latest/index.html

먼저 `pom.xml`에 해당 `dependency`를 추가  

```xml
<dependency>
    <groupId>io.specto</groupId>
    <artifactId>hoverfly-java</artifactId>
    <version>0.13.0</version>
    <scope>test</scope>
</dependency>
```

`HoverflyRule.inCaptureMode`를 통해 먼저 `proxy`가 캡처한 `request`, `response` 형식을 확인하자.  

아래와 같이 test 용 `application.properties` 파일을 만들고 테스트 클레스에 지정.  

```properties
#Disable discovery
spring.cloud.discovery.enabled=false
#Disable cloud config and config discovery
spring.cloud.config.discovery.enabled=false
spring.cloud.config.enabled=false
# test 에서 사용할 수 있도록 미리 리본 클라이언트 등록
account-service.ribbon.listOfServers=account-service:8091, account-service:9091
```

리본 클라이언트는 기본적으로 라운드 로빈 형식으로 요청을 진행하지만 아래와 같은 설정으로 서비스 요청방식을 변경가능하다.  

```java
@Configuration
public class RibbonConfiguration {
	@Bean
	public IRule ribbonRule() {
		//return new WeightedResponseTimeRule(); // response 시간에 따라 요청
		//return new BestAvailableRule(); // 
		return new AvailabilityFilteringRule();
	}
}
```

`Spring Boot 2.0`, `TestRestTemplate` 객체 사용을 위해 `webEnvironmnet` 속성과 `@RunWith` 어노테이션을 추가지정해야한다.  

```java
@Slf4j
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class) // junut5 에서 TestRestTemplate 빈 객체 생성을 위한 설정.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerControllerTest {

    @Autowired
    private TestRestTemplate template;

    @ClassRule
    public static HoverflyRule hoverflyRule = HoverflyRule
        .inSimulationMode(dsl(
            service("account-service:8091")
                .andDelay(200, TimeUnit.MILLISECONDS).forAll() // 0.2 초 딜레이 설정
                .get(startsWith("/customer/"))
                .willReturn(success("[{\"id\":\"1\",\"number\":\"1234567890\",\"balance\":5000}]", "application/json")),
            service("account-service:10091")
                .andDelay(10000, TimeUnit.MILLISECONDS).forAll() // 10 초 딜레이 설정
                .get(startsWith("/customer/"))
                .willReturn(success("[{\"id\":\"3\",\"number\":\"1234567892\",\"balance\":10000}]", "application/json")),
            service("account-service:9091")
                .andDelay(50, TimeUnit.MILLISECONDS).forAll() 
                .get(startsWith("/customer/"))
                .willReturn(success("[{\"id\":\"2\",\"number\":\"1234567891\",\"balance\":8000}]", "application/json"))))
        .printSimulationData();

    @Test
    public void testCustomerWithAccounts() {
        int a = 0, b = 0, d = 0;
        for (int i = 0; i < 1000; i++) {
            try {
                Customer c = template.getForObject("/withAccounts/{id}", Customer.class, 1);
                log.info("Customer: {}", c);
                if (c != null) {
                    if (c.getAccounts().get(0).getId().equals(1L)) a++;
                    else b++;
                }
            } catch (Exception e) {
                log.error("Error connecting with service", e);
                d++;
            }
        }
        log.info("TEST RESULT: 8091={}, 9091={}, 10091={}", a, b, d);
        // TEST RESULT: 8091=252, 9091=748, 10091=0
    }
}
```
예상대로 호출 delay가 짧은 `8091` 포트의 서비스가 훨씬 많은 빈도수를 차지한다.  

`printSimulationData()` 메서드에 의해 `HoverflyRule` 객체 생성시에 아래와 같은 json 데이터가 출력된다.  

```json
{
  "data" : {
    "pairs" : [ {
      "request" : {
        "path" : [ {
          "matcher" : "regex",
          "value" : "^/customer/.*"
        } ],
        "method" : [ {
          "matcher" : "exact",
          "value" : "GET"
        } ],
    ...
    ...
}
```

해당 데이터를 `capture.json`에 작성해 그대로 실행 가능하다. 

```java
@ClassRule
public static HoverflyRule hoverflyRule = HoverflyRule
    .inSimulationMode(classpath("capture.json"))
    .printSimulationData();
```

> 캐시데이터를 출력하는 것이 거슬리다면 `dsl()`메서드 2번째 변수로 `localConfigs().addCommands("--disable-cache")`를 설정  
```java
@ClassRule
public static HoverflyRule hoverflyRule = HoverflyRule
    .inSimulationMode(classpath("simulate.json") ,localConfigs().addCommands("--disable-cache"))
    .printSimulationData();
```

`restTemplate` 과 `@LoadBalancing` 을 사용하지 않고  
`@FiegnClient` 어노테이션으로 클라이언트 로드밸런싱을 진행 해도 똑같은 설정으로 사용하면 된다.  
(fiegn 역시 내부적으론 ribbon 라이브러리를 사용하기 때문에) 

### junit 5 + hoverfly 

> https://docs.hoverfly.io/projects/hoverfly-java/en/latest/pages/junit5/quickstart.html

```java
@Slf4j
@ExtendWith(HoverflyExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerControllerTest {

    @Autowired
    private TestRestTemplate template;

    @BeforeEach
    public void init(Hoverfly hoverfly) {
        hoverfly.simulate(dsl(
            service("account-service:8080")
                .andDelay(200, TimeUnit.MILLISECONDS).forAll()
                .get("/customer/1")
                .willReturn(success("[{\"id\":\"1\",\"number\":\"1234567890\",\"balance\":6000}]", "application/json")),
            service("account-service:9080")
                .andDelay(50, TimeUnit.MILLISECONDS).forAll()
                .get("/customer/1")
                .willReturn(success("[{\"id\":\"2\",\"number\":\"1234567891\",\"balance\":8000}]", "application/json")),
            service("account-service:10080")
                .andDelay(10000, TimeUnit.MILLISECONDS).forAll()
                .get("/customer/1")
                .willReturn(success("[{\"id\":\"3\",\"number\":\"1234567890\",\"balance\":5000}]", "application/json"))
        ));
        log.info("hover:" + hoverfly.getSimulation().toString());
    }
    ...
}


```

