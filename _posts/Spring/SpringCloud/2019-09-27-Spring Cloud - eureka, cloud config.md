---
title:  "Spring Cloud - eureka, cloud config!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## Spring Cloud  

> <https://spring.io/projects/spring-cloud>

바야흐로 클라우드 시대, 서버는 실제 서버 컴퓨터에서 돌아가지않고  
아마존과 같은 호스팅 업체가 관리하는 클라우드 서버에서 실행된다.  

이에 맞춰 클라우드 환경에 어울리는 서버환경을 구축하기 위해 `Spring Cloud` 라이브러리가 추가되었다.  

> 2024년 현재 `Spring Cloud` 라이브러리들은 레거시로 취급되고 있다.  
> k8s 가 출시되면서 대부분의 서비스가 `Eureka` 의 `Server Discovery, LoadBalancing, Cloud Config` 기능을 사용하지 않고 k8s 자체 기능을 사용한다.  
>  
> `Spring Cloud Kubernetes` 라이브러리 역시 굳이 사용하지 않더라도 k8s 설정을 통해 동일한 기능을 구현 가능하기 때문에 꼭 사용할 필요는 없다.  
>  
> `Feign, Gateway, Stream` 같은 기능은 서버 운영편리 및 가독성 때문에 사용하긴 하지만 Spring 기반 MSA 구축시 필수사용은 아니다.  

### 릴리즈 트레인

- `Spring Cloud Task`  
- `Spring Cloud Config`  
- `Spring Cloud Sleuth`  
- `Spring Cloud Commons`  
- `Spring Cloud Openfeign`  
- `Spring Cloud Kubernetes`  
- `Spring Cloud Aws`  
- `Spring Cloud Vault`  
- `Spring Cloud Function`  
- `Spring Cloud Bus`  
- `Spring Cloud Build`  
- `Spring Cloud Zookeeper`  
- `Spring Cloud Gcp`  
- `Spring Cloud Contract`  
- `Spring Cloud Consul`  
- `Spring Cloud Security`  
- `Spring Cloud Gateway`  
- `Spring Cloud Cloudfoundry`  
- `Spring Cloud Netflix`  
- `Spring Cloud Stream`  

`Spring Cloud` 안에는 여러가지 프로젝트가 존재하며 서로 의존관계가 형성되는 경우가 많다.  
하지만 버전은 각기 다르고 이를 외우고 다닐수 없으니 통합으로 사용하는 버전이 있는데 **릴리즈 트레인**이다.  

영국의 지하철 명을 버전명으로 사용하며 스프링 부트 버전에 맞는 릴리즈 트레인 명을 찾아 `Spring Cloud`를 사용하면 된다.  

Release Train|Boot Version
|--|--|
`2022.0.x aka Kilburn` | `3.0.x`  
`2021.0.x aka Jubilee` | `2.6.x, 2.7.x (Starting with 2021.0.3)`  
`2020.0.x aka Ilford` | `2.4.x, 2.5.x (Starting with 2020.0.3)`  
`Hoxton`| `2.2.x`
`Greenwich`| `2.1.x`
`Finchley`| `2.0.x`
`Edgware`| `1.5.x`
`Dalston`| `1.5.x`

`Release Train` 에 맞춰 자동으로 스프링 클라우드 관련 `dependency` 버전이 설정된다.  

사용할 Spring Boot 버전과 릴리즈 트레인은 아래와 같다.  

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.5.5'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
}
...

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2020.0.2"
    }
}
```

`spring-cloud-dependencies:2020.0.x` 이후로 `bootstrap.properties` 를 사용한 부트스트랩 컨텍스트 초기화는 더이상 사용되지 않음으로  
`application.properties` 를 사용해서 구성해야함  

## Eureka

> 참고  
> <https://docs.spring.io/spring-cloud-netflix/docs/current/reference/html/#service-discovery-eureka-clients>  
> <https://coe.gitbook.io/guide/service-discovery/eureka>  

`Spring Cloud Netflix` 프로젝트중 하나

서버IP는 유동적으로 변경되고 서버 관리자는 이에 대한 조치를 일일이 하기 힘듬으로  
동적으로 이동하는 서버(`Eureka Client`)를 관리하는 서버(`Eureka Server`)를 구성하여 조치한다.  

`Eureka Client` 사이에서 중간다리 역할을 하는것이 `Eureka Server`  
`Eureka Client` 관리뿐 아니라 로드 밸런싱이나 장애 복구 기능에도 영향을 끼친다.  

![eureka_discovery](/assets/springboot/spring-cloud/springcloud_eureka1.png)  

**Eureka Server**  
`Eureka Client`들을 관리(등록, 제어, 삭제, 제공) 하는 서버  

**Service Registry**  
`Eureka Client` 가 자기 자신의 정보를 `Eureka Server` 에 등록하는 행동  

**Service Discovery**  
등록된 `Eureka Client` 서비스 들을 `Eureka Server` 에서 발견하는 과정  

**Eureka Service**  
서비스 단위로 구분지어지는 어플리케이션, `service id` 라는 논리적 식별자로 구분된다.  

**Eureka Instance**  
`Eureka Server` 에 등록되는, 목록에서 조회되어지는 어플리케이션

**Eureka Client**
`Eureka Server` 에서 서비스를 검색가능한 어플리케이션

`Eureka Service` 는 동시에 `Eureka Instance` 의 모음이라 할 수 있고  
`Eureka Instance` 역시 동시에 `Eureka Client` 라 할 수 있다.  

> `Eureka Client` 클라이언트가 `Eureka Server` 에 등록과정까지 거치면 `Eureka Instance` 가 되는것  

`Netflix`에서 `Eureka`를 어떻게 사용하고 있는지에대한 그림  

![2](/assets/springboot/spring-cloud/springcloud_eureka2.png)  

`Eureka Server` 에 `Eureka Client` 에 `Service Registry` 하고 `Service Discovery` 를 통해 `Eureka Client` 들끼리 서로 통신할 수 있는 환경이 구축되었다.  

### Eureka Server 설정  

![3](/assets/springboot/spring-cloud/springcloud_eureka3.png)

`Eureka Client` 들이 `Eureka Server` 에 `service register` 할 수 있도록  
`Eureka Server` 서버를 먼저 생성  

```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-server'
}
```

```conf
spring.application.name=eureka-server
server.port=8761
# 이 클라이언트가 유레카 서버에서 유레카 레지스트리 정보를 가져와야 하는지 여부. default true
eureka.client.fetch-registry=false
```

> `eureka-server` 는 내부 의존으로 `eureka-client` 도 가지고 있어 자동으로 자기자신을 등록한다.  
> `eureka-server` 에서 `eureka.client.fetch-registry=false` 을 false 처리하지 않으면 에러가 나기에 별도 설정 필요  

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

아무런 설정도 하지 않고 `@EnableEurekaServer` 어노테이션을 지정하고 `main` 코드만 작성해도 동작한다.  

`Eureka Server` 보안처리를 위해 `spring-boot-starter-security` 추가  
`application.properties` 와 `java config` 아래와 같이 설정  

```conf
...
# spring security 설정
spring.security.user.name=admin-eureka
spring.security.user.password=1234
```

```java
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        super.configure(http);
    }
}
```

의존설정되어 있는데 클라이언트의 `discovery`기능을 제거한다.  

```conf
# 등록된 클라이언트 연결 해제 방지 옵션, false 일 경우 eviction-interval-timer 동안 Heart beat 미수신시 연결 해제
eureka.server.enable-self-preservation=true
# Heart beat 수신 인터벌, 기본값 60초
eureka.server.eviction-interval-timer-in-ms=3000
```

### Eureka Client 설정

`Eureka Client` 는 아래 의존성만 추가해주면 된다.  

```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
```

Eureka Server 에 보안설정을 해두었으니 아래와 같이 service url 을 설정

```conf
eureka.server.username=admin-eureka
eureka.server.password=1234
eureka.client.service-url.defaultZone=http://${eureka.server.username}:${eureka.server.password}@127.0.0.1:8761/eureka/
```

`main` 클래스 위에 `@EnableDiscoveryClient` 어노테이션을 설정하면 완료.  

```java
@SpringBootApplication
@EnableDiscoveryClient
public class EurekaclientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaclientApplication.class, args);
    }
}
```

> `@EnableDiscoveryClient`, `@EnableEurekaClient` 차이점  
> 서비스 디스커버리 라이브러리는 유레카 외에도 주키퍼, 컨설 등이 존재하며 `@EnableDiscoveryClient` 어노테이션은 모든 라이브러리를 지원하며 `@EnableEurekaClient` 는 유레카 라이브러리만을 지원한다.  



```conf
# Heart beat 보내는 인터벌, default 30
eureka.instance.lease-renewal-interval-in-seconds=10
# heartbeat 전송되지 않을시 인스턴스 제거 대기시간, lease-renewal-interval-in-seconds 보단 길어야함, default 90
eureka.instance.lease-expiration-duration-in-seconds=20
# 유레카 서버에 본인 서비스를 등록할 건지 여부, default true
eureka.client.register-with-eureka=true
# 유레카 서버로부터 서비스 목록을 로컬 캐시에 저장할 건지 여부, default true
eureka.client.fetchRegistry=true
# 같은 존에 있는 서비스 호출을 선호 하도록 설정
eureka.client.prefer-same-zone-eureka=true
# zone 설정
eureka.instance.metadata-map.zone=${ZONE:zone1}
```

![4](/assets/springboot/spring-cloud/springcloud_eureka4.png)

`http://localhost:8761/`로 접속하면 다음과 같이 `Eureka Client` 가 등록되어 실행중임을 알 수 있다.  

### Zone 

Eureka Server 를 1개만 운영할경우 해당 Eureka Server 에 장애가 발생하면 전체 서비스에 장애가 발생할 수 있다.  

따라서 아래 그림과 같이 여러개의 Eureka Server 운영을 권장한다. (최소 3개 이상)  

![2](/assets/springboot/spring-cloud/springcloud_eureka2.png)  

이렇께 물리적으로 다른 컴퓨터에서 동작하는 `Eureka Server` 에 `Eureka Instance` 들이 등록될 때 여러개의 `Eureka Server` 의 url 를 등록할 수 있다.  

지금까지는 `eureka.client.service-url.defaultZone` 을 사용하여 기본 `Zone` 만을 이용해 왔는데  
아래와 같이 자기가 위치하고 있는 `Zone` 에 대한 정보를 같이 등록시킬 수 있다.  

```conf
# 자신의 region 기입, default us-east-1
eureka.client.region=my-demo
# 해당 region 에서 사용 가능한 zone
eureka.client.availability-zones.my-demo=zone1
# zone 에 해당하는 유레카 서버 url
eureka.client.service-url.zone1=http://${spring.security.user.name}:${spring.security.user.password}@localhost:8761/eureka/
```

AWS 와 같은 환경에서 사용하다 보니 region, zone 매핑이 이루어지도록 구성되어 있다.  

> 해당 설정은 `Eureka Server` 에서 사용하면 `Replicate` 가 됨

- `eureka.client` : `Eureka Client` 가 레지스트리에서 다른 서비스의 정보를 얻을 수 있는 설정  
- `eureka.instance` : `Eureka Client` 의 행동을 재정의하는 설정(포트나 이름 등)  

## Cloud Config    

Spring Cloud Config 를 사용하면 마이크로 서비스 어플리케이션의 환경설정(application.properties) 를 한곳에서 관리 가능하다.  

가장 기본적인 `cloud config + eureka` 설정방법을 알아보자.

### Cloud Config Server 

```groovy
implementation 'org.springframework.cloud:spring-cloud-config-server'
implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
```

메인 클래스는 다음 3개의 어노테이션을 추가해서 실행한다.  

```java
@EnableDiscoveryClient
@EnableConfigServer
@SpringBootApplication
public class CloudConfigApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudConfigApplication.class, args);
    }
}
```

위에서 `Eureka Server` 에 `Security` 를 사용했기 때문에 `Eureka Server url` 을 아래와 같이 설정   

```conf
server.port=8888
spring.application.name=config-server
eureka.server.username=admin-eureka
eureka.server.password=1234
eureka.client.service-url.defaultZone=http://${eureka.server.username}:${eureka.server.password}@127.0.0.1:8761/eureka/
# https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_file_system_backend
# 기본 로컬환경의 백앤드 프로파일을 사용하려면 "spring.profiles.active=native"로 config 서버를 시작하십시오
spring.profiles.active=native
# 시스템 파일 기반 config 파일 지정
spring.cloud.config.server.native.search-locations=classpath:app-config
```

`Cloud Config` 역시 보안이 중요한 데이터가 들어있다 보니 `Spring Security` 를 통해  인증과정을 거치게 할 수 있다.  
아래와 같이 `Spring Security` 의존성 주입 후 `spring security user` 설정  

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.cloud:spring-cloud-config-server'
}
```

```conf
...
# config server역시 유레카 서버처럼 보안설정이 가능하다.
spring.security.user.name=admin-config
spring.security.user.password=5678
```

간단한 테스트를 위해 `cloud-config` 프로젝트에서 `resources/app-config/` 디렉토리에 아래와 같이 `config` 파일 설정  

```conf
# product-service.properties
test.value=spring-demo-test
test.name=product-demo-test
```

### Cloud Config Client 설정  

`Eureka Server` 에 이미 `Cloud Config` 가 `Eureka Instance` 로써 등록되어 있기 떄문에  
`Eureka Client` 는 간다한 설정을 통해 `Cloud Config` 에 저장된 설정들을 가져올 수 있다.  

기존 `Eureka Client` 로 동작하던 서비스에 `spring-cloud-starter-config` 의존성 추가  

```groovy
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    implementation 'org.springframework.cloud:spring-cloud-starter-config'
}
```

```conf
spring.application.name=product-service
eureka.server.username=admin-eureka
eureka.server.password=1234
eureka.client.service-url.defaultZone=http://${eureka.server.username}:${eureka.server.password}@127.0.0.1:8761/eureka/
# cloud config use
spring.config.import=optional:configserver:
# cloud config user id/pw
spring.cloud.config.username=admin-config
spring.cloud.config.password=5678
```

> <https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#config-data-import>  
> 부트스트랩 컨텍스트가 사라지면서 `spring.config.import` 설정을 지정해줘야 `cloud config` 로부터 사전에 설정들을 가져온다.  

#### 읽어들이는 config 우선순위  

```
1. application.properties              프로젝트의  
2. application.properties              cloud config 의  
3. application-{profile}.properties    프로젝트의
4. {service name}.yaml                 cloud config 의
5. {service name}-{profile},yaml       cloud config 의
```

위와 같이 계층형태의 구성을 가져와 최종적인 properties 를 설정하기 때문에  
기본적인 설정, profile 을 통한 그룹설정 등을 적용할 수 있다.  
