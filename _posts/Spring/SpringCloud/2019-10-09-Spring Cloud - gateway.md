---
title:  "Spring Cloud - gateway!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## Spring Cloud Gateway

> <https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/>

마이크로 서비스에서 게이트웨이는 주로 일괄적인 보안처리, 시스템 복잡도를 숨기기 위해 리버스 프록시 형태로 많이 사용한다.  
과거 `Spring Cloud` 에서 게이트웨이 역할을 해주는 대표적인 프로젝트는 아래 2개  

- `Spring Cloud Netflix Zuul`  
- `Spring Cloud Gateway`  

하지만 `zuul` 은 `Spring Cloud Hoxton Release` 를 마지막으로 더이상 지원하지 않기 때문에  
`Spring Cloud` 의 공식적인 게이트웨이는 `Spring Cloud Gateway` 이다.  

아래와 같이 `spring-cloud-starter-gateway` 의존성을 추가하고  

```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
}
```

`application.properties` 나 `java config` 로 라우팅역할을 해줄 설정만 적용하면 된다.  

`Spring Cloud Gateway` 설정에는 3가지 기본 개념이 있다.  

`route` - 게이트 웨이 기본 요소 목적지 url 과 각종 조건 설정  
`predicates`(조건자) - 각 요청 처리전 실행되는 로직, `java8` `Predicate` 인터페이스 기반 전처리 조건 설정  
`filters`(필터)  - 각 요청, 응답 값의 필터 설정 `GatewayFilter` 의 구현체  

### application.properties

`application.properties` 파일을 통해 `Spring Cloud Gateway` 설정하는법  


```conf
server.port=8000
spring.application.name=gateway-service

spring.cloud.gateway.routes[0].id=order-service
spring.cloud.gateway.routes[0].uri=http://localhost:8082
# Path route 사용
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/order/**
# RewritePath filter 사용, /api/order 를 지우고 order-service 에 request 요청
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/order/(?<path>.*), /$\\{path}
```

### java config 

위의 `application.properties` 를 그대로 `java config` 로 변환  

```java
@Configuration
public class RouteConfig {
  //spring.cloud.gateway.routes[0].id=order-service
  //spring.cloud.gateway.routes[0].uri=http://localhost:8082
  //spring.cloud.gateway.routes[0].predicates[0]=Path=/api/order/**
  //spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/order/(?<path>.*), /$\\{path}
  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
    return routeLocatorBuilder.routes()
      .route("order-service", predicateSpec -> predicateSpec
        .path("/api/order/**")
        .filters(gatewayFilterSpec -> gatewayFilterSpec
          .rewritePath("/api/order/(?<path>.*)", "/${path}"))
        .uri("http://localhost:8082"))
      .build();
  }
}
```

## Spring Cloud Gateway with Eureka


```conf
server.port=80
spring.application.name=api-gateway
eureka.server.username=admin-eureka
eureka.server.password=1234
# 자신의 region 기입
eureka.client.region=my-demo
# 해당 region 에서 사용 가능한 zone
eureka.client.availability-zones.my-demo=zone1
# zone 에 해당하는 유레카 서버 url
eureka.client.service-url.zone1=http://${eureka.server.username}:${eureka.server.password}@127.0.0.1:8761/eureka/
```

```conf
# default value = true, /gateway 로 시작하는 actuator path 접근 허용
management.endpoint.gateway.enabled=true 
# DiscoveryClient 를 토대로 route 구성, 등록된 클라이언트로 자동으로 gateway 라우트가 생성된다.  
spring.cloud.gateway.discovery.locator.enabled=false

# 수동으로 gateway 라우트 생성.
# lb://service-id 형식 사용  
spring.cloud.gateway.routes[0].id=account-service
spring.cloud.gateway.routes[0].uri=lb://account-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/account/**
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/account/(?<path>.*), /$\\{path}

spring.cloud.gateway.routes[1].id=customer-service
spring.cloud.gateway.routes[1].uri=lb://customer-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/customer/**
spring.cloud.gateway.routes[1].filters[0]=RewritePath=/api/customer/(?<path>.*), /$\\{path}

spring.cloud.gateway.routes[2].id=order-service
spring.cloud.gateway.routes[2].uri=lb://order-service
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/order/**
spring.cloud.gateway.routes[2].filters[0]=RewritePath=/api/order/(?<path>.*), /$\\{path}

spring.cloud.gateway.routes[3].id=product-service
spring.cloud.gateway.routes[3].uri=lb://product-service
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/product/**
spring.cloud.gateway.routes[3].filters[0]=RewritePath=/api/product/(?<path>.*), /$\\{path}
```

## Spring Cloud Gateway Actuator API

> <https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#actuator-api>

`management.endpoint.gateway.enabled=true` 설정시 CRUD 를 통해 `spring cloud gateway` 수정이 가능하다.  

`ID` | `HTTP Method` | `Description`
|---|---|---|
`globalfilters` | `GET` | Displays the list of global filters applied to the routes.
`routefilters` | `GET` | Displays the list of GatewayFilter factories applied to a particular route.
`refresh` | `POST` | Clears the routes cache.
`routes` | `GET` | Displays the list of routes defined in the gateway.
`routes/{id}` | `GET` | Displays information about a particular route.
`routes/{id}` | `POST` | Add a new route to the gateway.
`routes/{id}` | `DELETE` | Remove an existing route from the gateway.


```json
{
  // /actuator/gateway/routes/order-service
  "predicate": "Paths: [/order/**], match trailing slash: true",
  "route_id": "order-service",
  "filters": ["[[RewritePath /order/(?<path>.*) = '${path}'], order = 0]"],
  "uri": "lb://order-service",
  "order": 0
}
```
