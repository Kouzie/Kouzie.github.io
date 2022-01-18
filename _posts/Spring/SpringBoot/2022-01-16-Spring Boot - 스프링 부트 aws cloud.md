---
title:  "Spring Boot - 스프링 부트 aws cloud!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true
toc_sticky: true

---

## Spring boot with AWS Cloud

`AWS 환경 + 스프링 부트`에서 MSA 전략을 이용해 개발할때 사용할 수 `ECS Cluster` 내에서 MSA 구성시에 사용한 라이브러리와 서비스에대해 소개한다.  

먼저 `Spring Boot` 버전과 `Spring Cloud with AWS` 라이브러리의 버전을 먼저 설정   

> https://awspring.io/


```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.5.5'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2020.0.2"
        // for spring cloud with aws
        mavenBom "io.awspring.cloud:spring-cloud-aws-dependencies:2.3.0"
        mavenBom 'software.amazon.awssdk:bom:2.17.13'

    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    implementation 'io.awspring.cloud:spring-cloud-starter-aws'
    implementation 'io.awspring.cloud:spring-cloud-starter-aws-messaging'
    implementation 'io.awspring.cloud:spring-cloud-starter-aws-parameter-store-config'
}
```

각종 `AWS 리소스`를 사용하기 위한 라이브러리와 Spring Cloud 에서 `AWS 리소스`를 사용할 수 있는 라이브러리이다.  

### AWSCredentialsProvider

`AWS 리소스` 사용시 까다라온 권한검사가 필요한데 긴말 필요없이 `DefaultAWSCredentialsProviderChain` 로 인스턴스 생성하면 모두 해결된다.  

> https://docs.aws.amazon.com/ko_kr/sdk-for-java/v1/developer-guide/credentials.html

`AWSCredentialsProvider` 의 구현객체로 아래와 같은 많은 객체들이 존재한다.  

|클래스명|설명|
|---|---|
`BasicAWSCredentials` | 직접 accessKey, secretKey 설정
`EnvironmentVariableCredentialsProvider` | 환경변수
`SystemPropertiesCredentialsProvider` | java 시스템 속성
`ProfileCredentialsProvider` | `~/.aws/credentials` 에 저장된 프로필
`ContainerCredentialsProvider` | ECS 컨테이너 자격 증명
`InstanceProfileCredentialsProvider` | EC2 인스턴스 자격 증명
`DefaultAWSCredentialsProviderChain` | 위 클래스 순서대로 권한 검사

각 서버 배포 상황에 맞게 생성해서 사용해야 겠지만 `local`, `EC2`, `ECS`, 혹은 아예 다른 클라우드 서비스에 배포되어 사용될 수 있다.  
환경변수를 설정하던 프로필을 파일디렉토리에 삽입하던 `DefaultAWSCredentialsProviderChain` 를 사용하면 알아서 순서에 따라 적합한 자격증명클래스를 생성해준다.  

```java
@Bean
public AWSCredentialsProvider awsCredentialsProvider() {
    return new DefaultAWSCredentialsProviderChain();
}
```

로컬에선 `ProfileCredentialsProvider`  
`ECS 컨테이너`에선 `IAM Role` 를 지정했다면 `ContainerCredentialsProvider` 가 자동으로 설정된다.  


### AWS Parameter Store - Cloud Config

서버별 공통설정을 관리하기 위해 `Spring Cloud Config` 서비스를 사용하거나 `k8s` 의 `ConfigMap` 와 `spring-cloud-starter-kubernetes-client-config` 와 같은 라이브러리를 사용했을 것인데  

> k8s 를 사용한다면 참고: https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/

`AWS Parameter Store` 를 사용해 비슷한 기능을 구현할 수 있다.  
 
![springboot_quartz1](/assets/springboot/springboot_aws1.png)  


```conf
# application.properties
spring.application.name=demo
spring.profiles.active=dev

spring.config.import=aws-parameterstore:
aws.paramstore.enabled=true
aws.paramstore.fail-fast=true
aws.paramstore.prefix=/test
aws.paramstore.region=${aws.region.name}
aws.paramstore.name=${spring.application.name}
aws.paramstore.profile-separator=-
aws.paramstore.default-context=application
```

`spring.config.import`: 어떤 config 형식을 사용할 것인지 지정  
`aws.paramstore.fail-fast`: 실패시 실행x  
`aws.paramstore.prefix`: `default` 값이 `config`  
`aws.paramstore.name`: `default` 값이 `spring.application.name` 로 설정되지만 추가로 더 설정했다  
`aws.paramstore.profile-separator`: `default` 값은 `_`, 환경변수형식으로 설정하고 싶어 `-` 으로 변경했다  
`aws.paramstore.default-context`: `default`값이 `application`  

최종적으로 저장되는 형식은 아래와 같다.  

`/{aws.paramstore.prefix}/{aws.paramstore.name}-{spring.profiles.active}/{지정한config명}`

어플리케이션에 모두 공유하고 싶은 설정을 지정하고 싶다면 아래와 같이 설정  

`/{aws.paramstore.prefix}/{aws.paramstore.name}/{지정한config명}` : `spring.profiles.active` 상관없이 공유
`/{aws.paramstore.prefix}/application/{지정한config명}` : `aws.paramstore.name`, `spring.profiles.active` 상관없이 공유

```java
@Getter
@Configuration
public class ParamStore {
   @Value("${TEST_VALUE}")
    private String profileValue;
}
```

위와 같은 형식으로 최종 `config명`을 설정해서 받아올 수 있다.  

보통 `DB연결정보`, `SeceryKey` 와 같은 민감한 데이터를 코드상에 두지 않고 별도관리할 때 자주 사용한다.  

### AWS CloudMap - Service Discovery

`Service Discovery` 를 지원하는 서드파티 어플리케이션으로 `zookeeper` 와 `Spring Eureka` 등이 있다.  
`k8s` 에서는 `istio` 와 같은 `Service Mesh` 형식을 많이 사용한다. `AWS App Mesh` 서비스가 대표적  

> 서비스 매쉬 참고: https://daddyprogrammer.org/post/13700/service-mesh/

하지만 제일 간단한 방식은 `Private DNS` 를 사용한 `AWS CloudMap` 이라 생각한다.  

> https://aws.amazon.com/ko/blogs/aws/aws-cloud-map-easily-create-and-maintain-custom-maps-of-your-applications/


#### Private Name space (using by CDK)

아래와 같이 `CDK` 의 `PrivateDnsNamespace` 클래스와 `FargateService` 의 `cloudMapOptions` 을 연동지어 fargate task 를 실행하면 DNS 기반으로 서비스 디스커버리가 가능하다.  

```java
PrivateDnsNamespace namespace = PrivateDnsNamespace.Builder.create(this, profile + "-demo-namespace")
    .name("aws.demo")
    .vpc(vpc)
    .build();
...
...
FargateService fargateService = FargateService.Builder.create(stack, "ecs-fargate-device")
    .serviceName("ecs-fargate-device")
    .cluster(clusterDto.getCluster())
    .taskDefinition(taskDefinition)
    .securityGroups(Collections.singletonList(clusterDto.getSg()))
    .desiredCount(1)
    .cloudMapOptions(CloudMapOptions.builder()
        .cloudMapNamespace(clusterDto.getNamespace())
        .name("device")
        .build())
    .build();
```

feign 클라이언트와 같이 사용하면 매우 효과적이다.  

`implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'`

```java
@FeignClient(url = "http://device.aws.demo", contextId = "deviceClient", name = "deviceClient")
public interface DeviceServiceClient extends DeviceFeignService {
}
```

`Route53` 서비스에 가면 실제 `Private DNS` 에 설정된 `클라이언트의 IP` 확인 가능  

![springboot_quartz1](/assets/springboot/springboot_aws2.png)  


### AWS SQS Listener - Event Driven

장애 전파를 막기위해 비동기 메세지 전송을 사용해 서비스를 연동할 경우 주로 메세지 큐 방식을 사용한다.  

스프링 대표 메세지 큐 상품으로 `RabbitMQ` 가 있으며
서비스 환경에 따라 `Kafka` 와 같은 서드파티를 사용하기도 한다.  

AWS 대표상품으로 `ActiveMQ` 를 프로비저닝한 `AmazoneMQ` 가 있다.  
> https://aws.amazon.com/ko/amazon-mq/

하지만 빠른 실시간적인 처리가 필요 없고 컴퓨팅 리소스 비용이 아깝고 `serverless` 가 필요하다면 `AWS SQS` 서비스도 적합하다.  

또한 `AWS IoT Core` 와 같은 서비스와 연동하려면 `SQS` 가 좋은 방안이 될 수 있다.    

```java
@Slf4j
@Service
public class SqsProcessor {
    @SqsListener(value = "${queue.name}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void receiveMqttMessage(@Header("type") String type, String message) {
        log.info("mqtt message received, type:{}, message:{}", type, message);
    }
}

@Component
@RequiredArgsConstructor
public class MessageSender {

    private final Regions regions;
    private final AWSCredentialsProvider credentialsProvider;

    private QueueMessagingTemplate queueMessagingTemplate;

    @PostConstruct
    private void init() {
        AmazonSQSAsync amazonSQSAsync = AmazonSQSAsyncClientBuilder
                .standard()
                .withRegion(regions)
                .withCredentials(credentialsProvider)
                .build();
        queueMessagingTemplate = new QueueMessagingTemplate(amazonSQSAsync);
    }

    public void sendMessage(String serviceQueueName, String type, String data) {
        Message<String> message = MessageBuilder.withPayload(data)
                .setHeader("type", type)
                .build();
        queueMessagingTemplate.send(serviceQueueName, message);
    }
}
```

### Demo

> 