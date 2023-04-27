---
title:  "Spring Cloud - k8s!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## 개요  

스프링 클라우드 내부의 여러 서드파티(유레카, 스프링 클라우드 게이트웨이, 히스트릭스 등)를 사용하는 것 도 좋지만  
시간이 지남에 따라 쿠버네티스에서 제공하는 각종 컴포넌트들을 사용하는 것이 더 효율적이다.  

`discovery client`, `load balancing`, `configmap` 과 같은 굵직한 기능을 모두 쿠버네티스에서 제공하는 컴포넌트를 사용해 처리할 수 있다.  

> <https://spring.io/projects/spring-cloud-kubernetes#overview>  
> <https://www.youtube.com/watch?v=f4yOpHfVFw8&t=1404s>  

## 프로젝트 구성  

k8s 구성을 위한 간단한 프로젝트를 구성예정

> 데모코드 <https://github.com/Kouzie/spring-kube-demo>

`greeting`, `calculating` 서비스를 `k8s` 에서 실행시키고  
`configMap`, `feign client` 를 사용한다.  

### Service Account

`Spring Cloud Kubernetes` 에서 실행 중인 포드의 주소 목록, `ConfigMap` 을 검색할 수 있으려면 `Kubernetes API`에 대한 액세스가 필요하다.  

`default` 계정이 각종 `Kubernetes API` 를 사용할 수 있도록 권한을 부여해야 한다.  

> <https://cloud.spring.io/spring-cloud-kubernetes/reference/html/#service-account>

위 url 에서 `Service Account` 섹션 확인 후 `Role`, `RoleBinding` 생성

```yaml
# name space
apiVersion: v1
kind: Namespace
metadata:
  name: spring

---

kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: spring
  name: namespace-reader
rules:
  - apiGroups: ["", "extensions", "apps"]
    resources: ["configmaps", "pods", "services", "endpoints", "secrets"]
    verbs: ["get", "list", "watch"]

---

kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: namespace-reader-binding
  namespace: spring
subjects:
  - kind: ServiceAccount
    name: default
    apiGroup: ""
roleRef:
  kind: Role
  name: namespace-reader
  apiGroup: ""
```

```
kubectl apply -f rbac.yaml

namespace/spring created
role.rbac.authorization.k8s.io/namespace-reader created
rolebinding.rbac.authorization.k8s.io/namespace-reader-binding created
```

### ConfigMap

서비스들이 사용할 `configMap` 리소스 정의

```yaml
# config map
apiVersion: v1
kind: ConfigMap
metadata:
  namespace: spring
  name: demo-config
data:
  application.properties: |
    TEST_STRING=Hello Test
    calc.test=Hello Calc
    demo=Hello Demo
```

```
kubectl apply -f config.yaml 

configmap/demo-config created
```

### Spring Boot 실행  

사용하는 `dependency` 는 아래와 같다.  

```groovy
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.boot:spring-boot-starter-validation'
  implementation 'org.springframework.boot:spring-boot-starter-actuator'

  // for k8s configMap
  implementation 'org.springframework.cloud:spring-cloud-starter-kubernetes-client-config'
  // for spring cloud feign client
  implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
  // for k8s service loadbalancer
  implementation 'org.springframework.cloud:spring-cloud-starter-loadbalancer' 
}
```

그리고 각 서비스를 도커 이미지화 할 수 있는 `Dockerfile` 작성  

```dockerfile
FROM openjdk:8-jdk-alpine
# gradle
ARG JAR_FILE=build/libs/*.jar
# maven
# ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java","-jar","/app.jar", "--sprig.active.profile=${SPRING_PROFILES_ACTIVE}"]
```

이미지 생성 및 컨테이너 실행  

```
# api/greeting 위치에서 실행
gradle build
docker build -t greeting .
```

```
# api/calculating 위치에서 실행
gradle build
docker build -t calculating .
```

생성된 이미지를 기반으로 `k8s Deployment` 생성할 수 있는 `manifest` 파일 구성

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: spring
  name: calc-deployment
  labels:
    app: calc-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: calc-deployment
  template:
    metadata:
      labels:
        app: calc-deployment
    spec:
      containers:
        - name: calc-deployment
          image: calculating
          imagePullPolicy: Never #이미지가 로컬에 존재할 경우
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 5
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 5
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  namespace: spring
  name: calc-service
  labels:
    app: calc-service
spec:
  type: NodePort
  ports:
    - port: 80 # CLUSTER Service 접근 포트
      targetPort: 8080
      nodePort: 30080
  selector:
    app: calc-deployment
```

```
kubectl apply -f calc-deployment.yaml

deployment.apps/calc-deployment created
service/calc-service created
```

```
curl --location 'http://localhost:30080/calculating'

Hello Calc:Hello Demo
```

반환문자열 이 `ConfigMap` 에 지정한 문자열  
`bootstrap.properties` 에 `ConfigMap` 이 정상적으로 등록된것을 확인할 수 있다.  

### feign client  

`Spring Cloud` 플랫폼에선 `Eureka` 를 사용해 `Discovery Client` 가 이루어졌지만  
`k8s` 에선 `Discovery Client` 를 할 필요가 없다.  

`k8s DNS`, `k8s Networking` 시스템에 의해 서비스 로드벨런싱이 이루어진다.  

```java
@FeignClient(name = "calc-service") // service name
public interface CalculatingClient {
    @GetMapping("/calculating/{num1}/{num2}")
    Long addNumbers(@PathVariable Long num1, @PathVariable Long num2);
}
```

`feignClient` 와 같은 간단한 `RestTemplate` 구현체는 `k8s DNS` 에서 사용하는 `정규화된 도메인 이름(FQDN)` 만으로 서비스 호출이 가능하다.  

> <https://cloud.spring.io/spring-cloud-kubernetes/reference/html/#kubernetes-native-service-discovery>
> RestTemplate 에서 `{service-name}.{namespace}.svc.{cluster}.local:{service-port}` url 을 호출하게 된다.  
> 나머지는 `k8s Networking` 이 알아서 파드까지 `http` 요청을 전달함  

```
curl --location 'http://localhost:30081/greeting/1/2'
3
```

`greeting` 서비스를 통해 `calculating` 서비스까지 호출이 이어짐  

`Discovery Client` 할 필요가 없고 내부에 `client server list` 또한 가지고 있을 필요가 없기에 서비스 입장에선 효율적이다.  

## CD 툴  

기존에 java 프로그램을 쿠버네티스 환경에 배포하려면 아래 그림과 같은 과정을 거쳐야 한다.  

![jib1](/assets/springboot/spring-cloud/jib1.png)

자바 컨테이너에 대해 잘 알지 못한 자바개발자에겐 배포부터가 업무였지만
`skaffold` 나 `jib` 같은 툴들이 만들어지면서 알아야할 지식이 대폭 감소하였다.  

### jib

> <https://github.com/GoogleContainerTools/jib>

빌드 흐름을 아래처럼 간단하게 변경해준다.  

![jib1](/assets/springboot/spring-cloud/jib1.png)  
![jib2](/assets/springboot/spring-cloud/jib2.png)  

다음과 같이 plugin 지정  

```groovy
plugins {
    id 'com.google.cloud.tools.jib' version '2.8.0'
}
subprojects {
    apply plugin: 'com.google.cloud.tools.jib'
    ...
    jib.from.image = 'openjdk:11'
    jib.to.image = "${project.name}"
    jib.to.tags = ["latest"]
    jib.container.creationTime = "USE_CURRENT_TIMESTAMP"
}
```

`jib` 플러그인을 설치하면 아래와 같은 3가지 task 를 사용할 수 있다.  

- `jib`: build your container image and Publish
- `jibBuildTar`: build your image directly to a Docker daemon
- `jibDockerBuild`: build and save your image to disk as a tarball

`docker registry` 에 업로드는 하지 않을것임으로 `jibDockerBuild` 사용  

```
gradle :api:greeting:jibDockerBuild
gradle :api:calculating:jibDockerBuild
```

힘들게 docker build 명령어를 사용할 필요가 없으며  
`jib.to.image` 에 registry 주소까지 적용할 경우 자동으로 `image push` 됨으로 편하다.  

> 좀더 자세한 설명은 아래 url 참고  
> <https://github.com/GoogleContainerTools/jib/blob/master/jib-gradle-plugin/README.md>  

### skaffold  

Skaffold는 컨테이너 기반 및 Kubernetes 애플리케이션의 지속적인 개발을 촉진하는 명령줄 도구.  

> <https://skaffold.dev/docs>  
> <https://github.com/GoogleContainerTools/skaffold>  

설치, 그리고 프로젝트 생성방법은 아래와 같다.  

```
brew install skaffold

skaffold version          
v2.3.0

skaffold init --skip-build
```

생성된 기본 skaffold 파일은 아래와 같다.  

```yaml
apiVersion: skaffold/v4beta2
kind: Config
metadata:
  name: k-s
manifests:
  rawYaml:
    - rbac.yaml
    - config.yaml
    - calc-deployment.yaml
    - greet-deployment.yaml
```

`skaffold` 는 `gradle, jib` 연동이 가능하다.  
`build` 옵션에 `jib` 추가,  

> <https://skaffold.dev/docs/builders/builder-types/jib/>

```yaml
apiVersion: skaffold/v4beta2
kind: Config
metadata:
  name: k-s
manifests:
  rawYaml:
    - rbac.yaml
    - config.yaml
    - calc-deployment.yaml
    - greet-deployment.yaml
# build by jib
build:
  artifacts:
    - image: calculating
      jib:
        project: api:calculating
      context: ..
    - image: greeting
      jib:
        project: api:greeting
      context: ..
```

```
skaffold dev
```

`jib` 명령으로 이미지를 만들고 `manifests` 에 저장된 `k8s` 리소스를 올린다.  
