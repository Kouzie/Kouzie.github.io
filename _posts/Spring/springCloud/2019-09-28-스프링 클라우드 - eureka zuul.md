---
title:  "spring cloud - eureka zuul!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - spring

tags:
  - eureka
  - spring-cloud

toc: true

---

## 복제와 고가용성

![eureka_tbd]({{ "/assets/2019/eureka_tbd.png" | absolute_url }}){: .shadow}  

위 사진을 보면 유레카 서비스 등록을 위한 서버가 3개  
실제 서비스가 동작하는 유레카 클라이언트가 2개있다.  

유연한 장애처리와 리소스 부하 분산을 위해 적어도 2개 이상의 유레카 서버와 `ribbon`(로드밸런싱), `zuul`(api gateway)이 필요하다.  

우리는 서버 3개, 클라이언트 3개를 만들고 각 클라이언트에 접근하기 위해 거치는 zuul을 사용해보자.  
(`spring-cloud-starter-netflix-eureka-client` 의존성 안에 로드밸런싱을 위한 `ribbon`이 이미 포함되어있다.)

`spring.profiles.active`속성을 통해 `java -jar`명령으로 쉽고 빠르게 다른 설정을 불러올 수 있도록 설정하자.  

> 참고사항: Spring Boot 버전 -> `2.1.8.RELEASE`, Spring Cloud 버전 -> `Greenwich.SR3`

### 유레카 서버 설정

3개의 유레카 서버가 공통적으로 사용하는 설정은 `application.properties`에 지정,  
각 3개의 유레카 서버가 별도로 사용할 설정을 위한 설정파일을 3개 만들자.  

`application-peer1.properties`  
`application-peer2.properties`  
`application-peer3.properties`  

```conf
# application.properties 설정 내용 - peer 공통 설정
spring.application.name=demo_eureka_server
spring.profiles.active=peer1
# 유레가 서버의 보호모드를 off해 90초동안 인스턴스 유지를 없애고 eviction-interval-timer 기본값인 60초 동안기다리도록 설정
eureka.server.enable-self-preservation=false
# 60초가 너무 길기때문에 3초로 설정
eureka.server.eviction-interval-timer-in-ms=3000
# client 하트비드는 1초마다 도착하고 2초가 지나면 클라이언트의 서비스를 진행하지 않고 하트비트가 오지 않고 3초가 지나면 퇴거(삭제)한다
eureka.client.instance-info-replication-interval-seconds=2
```

```conf
# application-peer1.properties 설정 내용
server.port=${PORT:8761}
spring.profiles.active=peer1
eureka.instance.hostname=peer1
eureka.instance.metadata-map.zone=zone1
#eureka.client.service-url.defaultZone=http://localhost:8762/eureka, http://localhost:8763/eureka
```

```conf
# application-peer2.properties 설정 내용
server.port=${PORT:8762}
spring.profiles.active=peer2
eureka.instance.hostname=peer2
eureka.instance.metadata-map.zone=zone2
eureka.client.service-url.defaultZone=http://localhost:8761/eureka, http://localhost:8763/eureka
```

```conf
# application-peer3.properties 설정 내용
server.port=${PORT:8763}
spring.profiles.active=peer3
eureka.instance.hostname=peer3
eureka.instance.metadata-map.zone=zone3
eureka.client.service-url.defaultZone=http://localhost:8761/eureka, http://localhost:8762/eureka
```


설정이 끝났으면 `mvn package`로 jar파일을 생성하고 아래 명령으로 서버를 3개 실행한다.   

```
java -jar -Dspring.profiles.active=peer1 target/eurekaserver-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=peer2 target/eurekaserver-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=peer3 target/eurekaserver-0.0.1-SNAPSHOT.jar
```

### 유레카 클라이언트 설정

유레카 클라이언트도 마찬가지로 3개 생성하기 때문에 공통적인 설정은 `application.properties`에, 별도 설정은 3개의 설정파일을 생성해 각각 지정한다.  
```conf
# application.properties 설정 내용
# default profile을 zone1로 지정
spring.profiles.active=zone1
spring.application.name=demo_eureka_client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/, http://localhost:8762/eureka/, http://localhost:8763/eureka/
management.endpoint.shutdown.enabled=true
management.endpoint.shutdown.sensitive=false
management.endpoints.web.exposure.include=*
eureka.instance.lease-renewal-interval-in-seconds=1
eureka.instance.lease-expiration-duration-in-seconds=2
eureka.client.fetchRegistry=true
```

```conf
# application-zone1.properties 설정 내용
server.port=${PORT:8081}
spring.profiles.active=zone1
```
```conf
# application-zone2.properties 설정 내용
server.port=${PORT:8082}
spring.profiles.active=zone2
```
```conf
# application-zone3.properties 설정 내용
server.port=${PORT:8083}
spring.profiles.active=zone3
```

간단한 데모 서비스를 생성하자.  

```java
@RestController
public class ClientController {

    @Value("${spring.profiles.active}")
    private String zone;

    @GetMapping("/ping")
    public String ping() {
        return "I'm in zone " + zone;
    }

}
```

마찬가지로 설정이 끝났으면 `mvn package`로 jar파일을 생성하고 아래 명령으로 서버를 3개 실행.   

```
java -jar -Dspring.profiles.active=zone1 -Xmx192m target/eurekaclient-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=zone2 -Xmx192m target/eurekaclient-0.0.1-SNAPSHOT.jar
java -jar -Dspring.profiles.active=zone3 -Xmx192m target/eurekaclient-0.0.1-SNAPSHOT.jar
```

### 유레카 zuul 설정  

이제 zuul을 통해 유레카 서버에 등록된 서비스들 중 가장 한적한 서버에게 api요청을 하면 되는데 `zuul`을 통해 진행할 수 있다.  
우선 아래 2개 의존성 추가  
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
```


`appliaction.properties`는 아래와 같다.  

```conf
server.port=8765
spring.application.name=gateway-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# 게이트웨이는 eureka server에 노출될 필요가 없다.  
eureka.client.register-with-eureka=false

# 접두사 추가
zuul.prefix=/api 

# 유레카에 등록되어있는 서비스 아이디
zuul.routes.demo-client.path=/client/**
zuul.routes.demo-client.service-id=demo_eureka_client

# 서버에 demo_eureka_client로 등록되어있는 서비스의 id를 
```

`zuul.routes`속성을 통해 `http://localhost:8765/api/client/...`을 요청하면 3개중 첫번째 유레카 서버에 등록되어 있는 서비스중 id가 `demo_eureka_client` 인 것을 찾아 리다이렉트한다.  

3개의 `demo_eureka_client`가 동작하고 있는데 zuul이 알아서 1:1:1 비율로 리다이렉트 시켜준다.  

```java
@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
public class EurekagatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekagatewayApplication.class, args);
    }
}
```
스프링 어플리케이션 어노테이션과 함께 `zuul`, `Eureka Client` 어노테이션도 같이 추가해야한다.  

설정이 끝났으면 `java -jar` 명령으로 실행하며 된다. (설정파일이 하나기에 추가 속성은 없음)   

![eureka_zuul]({{ "/assets/2019/eureka_zuul.png" | absolute_url }}){: .shadow}  

유레카 서버 대쉬보드로 접속하면 위와같이 3개의 server, 3개의 client가 동작중임을 알 수 있다.  
> 8761, 8762, 8763 어디로 접속하던 상관 없음, http://localhost:8761/eureka/apps/ url로 접속하면 현재 어떤 서비스가 어디 IP에서 동작중임을 알 수 있음  


`http://localhost:8765/api/client/ping` 에 접속하여 어떤 3개중 어떤 서비스가 호출되는지 확인한다.  

![eureka_zuul2]({{ "/assets/2019/eureka_zuul2.gif" | absolute_url }}){: .shadow}  

#### 서비스 라우팅 옵션

참고로 `zuul.routes.demo-client`속성을 통해 path와 service를 유레카 서버로부터 찾아내었는데  
별도설정 없이도 자동으로 `localhost:8765/api/demo_eureka_client/**`형식으로 route경로가 등록된다.  

게이트웨이에 노출되면 안되는 서비스가 있을경우 아래처럼 제외설정을 적용

`zuul.ignoredServices='custom-service'` , `zuul.ignoredServices='*'`(모든 서비스 무시)

등록된 유레카 path를 바꾸고 싶다면 
```conf
zuul.routes.order-service.path=/order/**
zuul.routes.account-service.path=/account/**
zuul.routes.customer-service.path=/customer/**
zuul.routes.product-service.path=/product/**
```

위에설정처럼 서비스명을 등록후에 path를 지정해도 상관 없다.  
```conf
zuul.routes.demo-client.service-id=demo_eureka_client
zuul.routes.demo-client.path=/client/**
```

`zuul.routes.<serviceId>.stripPrefix`: false인 경우 uri를 모두 보내며, true인 경우에는 matching된 값을 제외하고 보낸다  
`zuul.routes.product-service.path=/product/**` 상황에서 `/product/findById/1` 이라는 url요청시 `stripPrefix`가 false일 경우 `product-service`에 `/product/findById/1`전체를 보내고  
true일 경우 `/product`는 제외한 `/findById/1`만 보내진다.  


`actuator`를 추가하고 `management.endpoints.web.exposure.include=*` 속성을 더해 route하는 api목록을 `/actuator/routes`에서 확인해보자.  

```json
{
    "/api/order/**": "order-service",
    "/api/customer/**": "customer-service",
    "/api/account/**": "account-service",
    "/api/product/**": "product-service"
}
```
기타 설정까지 자세히 보고 싶다면 `/actuator/routes/details` 호출  

#### zuul 필터

`/actuator/filter`

```json
{
    "error": [{
        "class": "org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter",
        "order": 0,
        "disabled": false,
        "static": true
    }],
    "post": [{
        "class": "org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter",
        "order": 1000,
        "disabled": false,
        "static": true
    }],
    "pre": [{
        "class": "org.springframework.cloud.netflix.zuul.filters.pre.DebugFilter",
        "order": 1,
        "disabled": false,
        "static": true
    },...],
    "route": [{
        "class": "org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter",
        "order": 100,
        "disabled": false,
        "static": true
    },...]
}
```