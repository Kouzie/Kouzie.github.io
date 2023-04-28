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

바야흐로 클라우드 시대, 서버는 실제 서버 컴퓨터에서 돌아가지않고  
아마존과 같은 호스팅 업체가 관리하는 클라우드 서버에서 실행된다.  

이에 맞춰 클라우드 환경에 어울리는 서버환경을 구축하기 위해 `Spring Cloud` 라이브러리가 추가되었다.  

아래와 같은 클라우드 환경에서 효과적으로 사용할 수 있는 라이브러리가 있으며  

**MODULE**|**VERSION**|
-----|-----
`Spring Cloud Task`|`2.1.2.RELEASE`
`Spring Cloud Config`|`2.1.3.RELEASE`
`Spring Cloud Sleuth`|`2.1.2.RELEASE`
`Spring Cloud Commons`|`2.1.2.RELEASE`
`Spring Cloud Openfeign`|`2.1.2.RELEASE`
`Spring Cloud Kubernetes`|`1.0.2.RELEASE`
`Spring Cloud Aws`|`2.1.2.RELEASE`
`Spring Cloud Vault`|`2.1.2.RELEASE`
`Spring Cloud Function`|`2.0.2.RELEASE`
`Spring Cloud Bus`|`2.1.2.RELEASE`
`Spring Cloud Build`|`2.1.6.RELEASE`
`Spring Cloud Zookeeper`|`2.1.2.RELEASE`
`Spring Cloud Gcp`|`1.1.2.RELEASE`
`Spring Cloud Contract`|`2.1.2.RELEASE`
`Spring Cloud Consul`|`2.1.2.RELEASE`
`Spring Cloud Security`|`2.1.3.RELEASE`
`Spring Cloud Gateway`|`2.1.2.RELEASE`
`Spring Cloud Cloudfoundry`|`2.1.2.RELEASE`
`Spring Cloud Netflix`|`2.1.2.RELEASE`
`Spring Cloud Stream`|`Fishtown.SR3`

`Spring Cloud` 안에는 여러가지 프로젝트가 존재하며 서로 의존관계가 형성되는 경우가 많다.  
하지만 버전은 각기 다르고 이를 외우고 다닐수 없으니 통합으로 사용하는 버전이 있는데 **릴리즈 트레인**이다.  

영국의 지하철 명을 버전명으로 사용하며 스프링 부트 버전에 맞는 릴리즈 트레인 명을 찾아 `Spring Cloud`를 사용하면 된다.  

`Release Train`|`Boot Version`
-----|-----
`Hoxton`| `2.2.x`
`Greenwich`| `2.1.x`
`Finchley`| `2.0.x`
`Edgware`| `1.5.x`
`Dalston`| `1.5.x`

`Release Train` 에 맞춰 자동으로 스프링 클라우드 관련 `dependency` 버전이 설정된다.  

현재 `Spring Boot`버전은 `2.2.6.RELEASE`을 사용하고 있으며 `Spring Cloud`버전은 `Hoxton.SR4`을 사용중이다.  


## eureka

> https://github.com/Kouzie/sample-spring-cloud
> https://coe.gitbook.io/guide/service-discovery/eureka

서버IP는 유동적으로 변경되고 서버 관리자는 이에 대한 조치를 일일이 하기 힘듬으로  
동적으로 이동하는 서버(`eureka client`)를 관리하는 서버(`eureka server`)를 구성하여 조치한다.  

`eureka client` 사이에서 중간다리 역할을 하는것이 `eureka server`  
`eureka client` 관리뿐 아니라 로드 밸런싱이나 장애 복구 기능에도 영향을 끼친다.  

![eureka_discovery](/assets/2019/springcloud_eureka1.png)  

**Eureka Server**  
`eureka client`들을 관리(등록, 제어, 삭제, 제공) 하는 서버  

**Service Registry**  
`eureka client` 가 자기 자신의 정보를 `eureka server` 에 등록하는 행동  

**Service Discovery**  
등록된 `eureka client` 서비스 들을 `Eureka Server` 에서 발견하는 과정  

**Eureka Service**  
`eureka server` 에 등록된 `eureka client` 들의 모음  
서비스 단위로 구분짖기에 동시에 같은 서비스를 지원하는 여러개의 `eureka client` 가 동작할 수 있다. 

**Eureka Instance**  
`eureka server` 에 등록되어 목록에서 조회 가능한 `eureka service`를 의미  

`Netflix`에서 `Eureka`를 어떻게 사용하고 있는지에대한 그림  

![eureka_tbd](/assets/2019/springcloud_eureka2.png)  

`Eureka Server` 에 `eureka client` 에 `Service Registry` 하고 `Service Discovery` 를 통해 `eureka client` 들끼리 서로 통신할 수 있는 환경이 구축되었다.  

### eureka 서버 설정  

![eureka_registry](/assets/2019/eureka_registry.png)

`eureka client` 들이 `eureka server` 에 `service register` 할 수 있도록  
`eureka server` 서버를 먼저 생성  

* `Spring Boot Tool`  
* `Spring Web`  
* `Eureka Server`  

위 3개의 `dependency`를 `IntelliJ` 프로젝트 생성시 포함,  
아래와 같은 `pom.xml`이 생성된다.  

```xml
...
...
<properties>
    <java.version>1.8</java.version>
    <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
</properties>

<dependencies>
    ...
    ...
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`application.properties`는 아래와 같이 설정  

`spring-cloud-starter-netflix-eureka-server` `dependency`에 `server`기능 뿐 아니라 `client`의 기능도 의존설정되어 있는데 클라이언트의 `discovery`기능을 제거한다.  

서버 자신이 다른서버에 `discovery` 되지 않도록 아래 `proeprties` 설정

```conf
server.port=${PORT:8761}
# 유레카 서버에 본인 서비스를 등록할 건지 여부
eureka.client.register-with-eureka=true
# 유레카 서버로부터 서비스 목록을 로컬 캐시에 저장할 건지 여부, 둘 다 기본값 true라서 지정하지 않아도 상관 없다.
eureka.client.fetchRegistry=true
```

마지막으로 `main`클래스 위에 `@EnableEurekaServer` 어노테이션을 지정하고 실행하면 유레카 서버의 역할을 수행한다.  

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaserverApplication {
    public static void main(String[] args) {...}
}
```


### eureka 클라이언트 설정

서비스를 유레카 서버에 등록하는 유레카 클라이언트 생성과정을 알아보자.  
프로젝트 생성시 `IntelliJ`에서 다음 `dependency`추가,  

* `Spring Boot Tool`  
* `Spring Web`  
* `Eureka Discovery Client`  


```xml
...
...
<properties>
    <java.version>1.8</java.version>
    <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
</properties>
<dependencies>
    ...
    ...
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>
```

`spring-cloud-starter-netflix-eureka-client` `dependency` 가 추가된다.  

```conf
server.port=${PORT:8081}
spring.application.name=demo_eureka_client
eureka.client.service-url.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}
```

유레카 클라이언트가 서비스를 등록할 서버 주소를 지정하기 위해  `eureka.client.service-url.defaultZone` 속성을 사용  

```java
@SpringBootApplication
@EnableDiscoveryClient
public class EurekaclientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaclientApplication.class, args);
    }
}
```

`main` 클래스 위에 `@EnableDiscoveryClient` 어노테이션을 설정하면 완료.  

> `@EnableDiscoveryClient`, `@EnableEurekaClient` 차이점  
> 서비스 디스커버리 라이브러리는 유레카 외에도 주키퍼, 컨설 등이 존재하며 `@EnableDiscoveryClient` 어노테이션은 모든 라이브러리를 지원하며 `@EnableEurekaClient` 는 유레카 라이브러리만을 지원한다.  

`pom.xml`이 있는 위치에서 `mvn package`로 서버 실행가능한 `jar`파일을 생성하고  
2개의 터미널을 열어 각기 다른 포트로 서버를 실행하자.  

```
$ java -jar -DPORT=8081 target/eurekaclient-0.0.1-SNAPSHOT.jar
$ java -jar -DPORT=8082 target/eurekaclient-0.0.1-SNAPSHOT.jar
```

![eureka_registry2](/assets/2019/eureka_registry2.png)  

`http://localhost:8761/`로 접속하면 다음과 같이 2개의 서비스가 등록되어 실행중임을 알 수 있다.  

### Eureka 클라이언트 종료  

유레카 클라이언트를 안전하게 종료하기 위해 `actuator`의 `shutdown` API를 사용한다.  
중단된 이벤트를 다른 유레카 클라이언트가 가로채거나 이벤트르 다시 서버로 보내기 위해 안전한 종료가 필요하다.  

`pom.xml`에 다음 의존성 추가  

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

그리고 `actuator` 관련 속성을 `application.properties`에 등록한다.  

```conf
management.endpoint.shutdown.enabled=true
# 보안 해제
management.endpoint.shutdown.sensitive=false
management.endpoints.web.exposure.include=*
```


> 출처: https://www.baeldung.com/spring-boot-shutdown  

![eureka_shutdown](/assets/2019/eureka_shutdown.png)  

IntelliJ를 통해 생성된 Mapping 정보를 보면 `/actuator/shutdown` API가 `POST`방식으로 생성된 것을 알 수 있다.  

그이외에도 여러 기능이 추가됨...   

`REST Client`와 같은 툴을 사용해 위 API를 호출하면 다음 메세지를 반환하고 서버가 종료된다.  

```json
{"message": "Shutting down, bye..."}
```  

위와 같은 안전한 방식이 아닌 장애나 네트워크 상황으로 인해 서버가 강제 종료될 경우 이미 종료된 서비스가 서버에 계속 남아있는 상황이 발생할 수 있다.  

이는 유레카 서버가 특이한 메커니즘으로 클라이언트를 유지하기 때문, 일시적 네트워크 장애로 인한 서비스 해제를 막기위해 보호모드를 동작하는데  
이 보호모드 동작시간이 60초가 default 값이다. (속성설정으로 변경 가능)  

해당 시간안에 갱신 요청이 일정 횟수 이상 들어오지 않아야 서비스를 해제한다.  
다음 설정으로 해제 가능하다.  

`eureka.server.enable-self-preservation=false`  

하트비트가 들어오지 않으면 바로 서비스를 제거한다.  
물론 좋지않은 설정이며 시간값을 설정후 `shutdown` 으로 종료해 클라이언트가 유레카 서버에서 서비스를 제거할 수 있도록 하는것이 안정적이다.  

이는 변경이 불가능하며 `server.servlet.context-path`를 통해 `context`값 변경으로 약같 변경 흉내는 가능하다.  


## 클라우드 환경설정   

마이크로 서비스는 점점 많아지고...  
서버마다 설정해야 하는 설정 또한 다양하지고...  
혹시라도 도중에 설정이 변경되거나 한다면 다시 빌드하고 서버 재실행...  

이를 한번에 타파할 수 있는것이 `cloud config`이다.  

여러가지 설정이 있지만 가장 효율적이라 생각하는 `git + cloud config + eureka` 설정방법을 알아보자.  

`cloud config`서버는 git서버에 등록되어 있는 여러 설정파일을 로컬 캐시에 저장,  
`cloud config`를 유레카 서버에 등록하여 여러개의 유레카 클라이언트가 `Service Discovery`기능을 사용하여  
자신이 찾고자 하는 설정을 `cloud config`에서 찾아낸다.  


우선 가장 기본인 `dependency`부터 추가  
`cloud config` 뿐 아니라 유레카 클라이언트로 서버에 등록해야 하기 때문에 `eureka-client`도 추가한다.  
또한 아무 서비스에게 설정을 제공하지 않고 계정/비번을 알고있는 서비스들에게만 설정을 제공하기 위해 `spring-security`도 추가한다.  

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

메인 클래스는 다음 3개의 어노테이션을 추가해서 실행한다.  
```java
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class CloudconfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudconfigApplication.class, args);
	}

}
```

`cloud config`라 매우 어려울 것 같지만  
이제 `application.properties`만 설정해서 실행하면 된다.  

우선 실행하기 전에 `cloud config`를 위한 `git repository`부터 생성   

![cloud-config1](/assets/2019/cloud-config1.png){: .shadow}  

총 6개의 서비스를 위한 설정파일을 생성하였다.  

기본적인 `cloud config`의 설정은 아래와 같다.  
`application`이름을 `config-server`로 등록하기에 유레카 서버에 해당 이름으로 서비스가 등록된다.  

```conf
server.port=${PORT:8889}
spring.application.name=config-server

# 기본 백앤드 프로파일을 사용하려면 "spring.profiles.active = native"로 config 서버를 시작하십시오
# https://cloud.spring.io/spring-cloud-static/spring-cloud-config/1.1.3.RELEASE/
# spring.profiles.active=native
# 우리는 백앤드 프로필이 아닌 깃으로 부터 프로필을 가져올 것이기에 주석  
spring.cloud.config.server.git.uri=https://github.com/Kouzie/cloud-config-repo.git

# public repo일 경우 아이디와 패스워드는 필요 X
spring.cloud.config.server.git.username=${github.username}
spring.cloud.config.server.git.password=${github.password}

# config server 시작시 git repo를 local에 clone해와서 설정파일 적용
spring.cloud.config.server.git.clone-on-start=true

# config server역시 유레카 서버처럼 보안설정이 가능하다.
spring.security.user.name=admin_config
spring.security.user.password=1234

# spring `cloud config`를 유레카 서비스 서버에 등록, 다른 유레카 클라이언트가 `cloud config`를 유레카 서버를 통해 얻을 수 있다.
eureka.client.service-url.defaultZone=http://admin:qwer@localhost:8761/eureka/
```

> 만약 `git repo`를 사용하지 않고 바로 `cloud config`의 파일 시스템에서 설정파일을 서비스들에게 제공하고 싶다면 
`spring.profiles.active=native` 설정을 사용하자.  

> https://github.com/spring-cloud/spring-cloud-netflix/issues/2754
> 유레카 서버에 안전한 접근을 위해 spring security 를 화성화 한 상태라면 csrf 기능때문에 다른 유레카 클라이언트들이 접근 안될 수 있다. 이를 해재해주자.  

이제 유레카 서버를 실행시키고 `cloud config`서버를 유레카 클라이언트로 서버에 등록해보자.  

아래는 유레카 서버에 대한 `application.properties`이다.

```conf
spring.application.name=server
spring.profiles.active=peer1

# 유레가 서버의 보호모드를 off해 90초동안 인스턴스 유지를 없애고 eviction-interval-timer 기본값인 60초 동안기다리도록 설정
eureka.server.enable-self-preservation=false
# 60초가 너무 길기때문에 3초로 설정
eureka.server.eviction-interval-timer-in-ms=3000
# client 하트비드는 1초마다 도착하고 2초가 지나면 클라이언트의 서비스를 진행하지 않고 하트비트가 오지 않고 3초가 지나면 퇴거(삭제)한다

spring.security.user.name=admin
spring.security.user.password=qwer

# 암호를 설정했다면 서버에도 defaultZone에 계정과 비번을 설정한 url을 지정해야 서버에서 오류가 발생하지 않는다.
# 아무리 fetch, register옵션을 false로 지정해도.... (자기 자신도 지속적으로 연결 상태를 체크?)
eureka.client.service-url.defaultZone=http://admin:qwer@localhost:8761/eureka/

# 등록 이후 Instance 정보가 변경 되었을 때 Registry 정보를 갱신하기 위한 REST를 2초마다 호출
eureka.client.instance-info-replication-interval-seconds=2

# `cloud config`서버에서 보안설정 할경우 유레카 클라이언트와 config서버간 연결을 위한 계정/비번을 유레카 서버가 지니고 있어야함
#spring.cloud.config.username=admin_config
#spring.cloud.config.password=1234

#spring.cloud.config.uri=http://localhost:8889
# application name + profile active => server-peer1 을 `cloud config`에서 찾아 지정한다.

eureka.client.fetch-registry=false
eureka.client.register-with-eureka=false
server.port=8761
```

유레카 서버를 실행하고 `cloud config`서버를 실행하면 유레카 서버에 서비스가 등록되고 다음 url을 통해 여러 설정정보를 가져올 수 있다.  

![cloud-config2](/assets/2019/cloud-config2.png){: .shadow}  
![cloud-config3](/assets/2019/cloud-config3.png){: .shadow}  
![cloud-config4](/assets/2019/cloud-config4.png){: .shadow}  

여기서 한가지 딜레마에 빠지게 되는데 `유레카 서버를 위한 설정은 어디서 가져오느냐` 이다.  
만약 유레카 서버설정을 `cloud config`에서 가져오고 싶다면 `cloud config`를 유레카 클라이언트로 등록하지 않아야 한다.  

하지만 그럴겨우 다른 클라이언트 들은 `cloud config`를 `Service Discovery`기능으로 발견하지 못하게 된다(클라이언트로 등록되지 않았으니까).  

즉 유레카 서버설정을 `cloud config`로 가져오고 싶다면 유레카 클라이언트들의 `Service Discovery`기능을 포기해야 하고, `Service Discovery`기능을 사용하고 싶다면 유레카 서버설정은 `cloud config`로부터 가져올 수 없다.  

`Service Discovery`기능을 사용하는 방식을 `디스커버리 우선 부트스트랩`  
유레카 서버 컨피그를 `cloud config`로부터 가져오는 것을 `컨피그 우선 부트스트랩` 이라한다.  

우리는 지금까지 `디스커버리 우선 부트스트랩` 을 기반으로 `cloud config`와 유레카 서버 설정을 하였다.    

---

이젠 일반적인 유레카 클라이언트를 실행해 `cloud config`서버에 있는 설정 파일을 가져오기만 하면 된다.  
서버 이름은 `client`, profiles는 `zone1`로 설정할 것이기 때문에 `cloud config`에 등록되어 있는 `client-zone1.properties` 설정파일을 읽어온다.  

유레카 클라이언트의 `application.properties` 를 지우고 `bootstrap.properties` 이름으로 아래처럼 설정하자.  

```conf
spring.application.name=client
spring.profiles.active=zone1

# spring cloud security에서 계정, 비번 설정시 아래 defaultZone 과 같이 사용
eureka.client.service-url.defaultZone=http://admin:qwer@localhost:8761/eureka/

management.endpoint.shutdown.enabled=true
management.endpoint.shutdown.sensitive=false
management.endpoints.web.exposure.include=*
# default로 heartbeat를 30초마다 보내고 서버는 보호 모드로 인해 heartbeat를 90초동안 받지 못한다면 instance를 제거한다. 보호모드 제거시 60초에 instance제거
# 이는 client장애가 발생해도 최악의 경우 60초동안 client 연결을 유지시켜준다는 뜻, 사용자 입장에선 최악
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

# config서버를 유레카 서버로 부터 discovery하여 사용,
spring.cloud.config.discovery.enabled=true
# config-server라는 서비스가 config서버임을 지정
spring.cloud.config.discovery.service-id=config-server

# `cloud config`에 안전한 접근을 위해 `cloud config`에서 spring security를 통해 계정/비번 설정을 하였다.
# `cloud config`의 security에서 지정한 계정 비번을 그대로 적용
spring.cloud.config.username=admin_config
spring.cloud.config.password=1234

# git의 변경을 감지하는 메커니즘을 사용자 정의
spring.cloud.config.server.monitor.github.enabled=true
```

처음 서버 실행시 유레카 클라이언트로 등록하기 위한 제일 기본적인 설정 작성후  

유레카 클라이언트를 실행하면 다음과 같이 서버에서 `cloud-config` 서비스를 찾아 등록했다는 메세지가 출력된다.  

```
2019-10-06 17:45:16.413  INFO 31137 --- [(7)-172.30.1.11] c.c.c.ConfigServicePropertySourceLocator : Fetching config from server at : http://172.30.1.11:8889/
2019-10-06 17:45:17.179  INFO 31137 --- [(7)-172.30.1.11] c.c.c.ConfigServicePropertySourceLocator : Located environment: name=client, profiles=[zone1], label=null, version=4c497d99d3e659e284d69a44550a4826737edc25, state=null
```

git 의 properties파일의 설정내용을 변경하면 `cloud config`에는 바로 적용이 된다. 변경된 이후부터는 변경된 내용을 REST API로 출력하며 이후 config 파일을 요청하는 클라이언트 들에게도 변경된 config 파일을 제공한다.   

하지만 기존에 실행된 유레카 클라이언트는 실행된 후 계속 이전의 설정내용을 사용하는데 변경된 설정으로 재기동 없이 변경하려면 `actuator`의 `refresh`기능을 사용하면 된다.  