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

스프링 클라우드 내부의 여러 라이브러리를 사용해 아래 기능들을 지원하였는데  

- Config Map
- Service Discovery
- Reverse Proxy(Gateway)
- Client side Loadbalancer

`spring-cloud-k8s` 라이브러리를 사용하면 k8s 와 연계해서 위와 같은 기능을 사용할 수 있다.  

> <https://spring.io/projects/spring-cloud-kubernetes#overview>  
> <https://docs.spring.io/spring-cloud-kubernetes/reference/getting-started.html>
> <https://www.youtube.com/watch?v=f4yOpHfVFw8&t=1404s>  

### ConfigMap

문서에선 기본적으로 `spring.application.name` 과 동일한 `configMap` 을 찾아 `Spring Config` 로 등록한다고 설명한다.  

`Spring Application` 과 `cloudMap` 의 `namespace` 도 물론 동일해야 한다.  

그 외에도 profile 에 따라 자동으로 가져오는 설정들이 있음으로 아래 url 참고

> <https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#kubernetes-propertysource-implementations>


하지만 커스텀한 `namespace`, `configMap` 이름을 지정해서 Spring Config 로 등록해야 할 경우 `bootstrap.properties` 를 사용하면 된다.  


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

### Spring Boot Dependencies

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

그리고 각 서비스를 도커 이미지화 할 수 있는 `Dockerfile` 작성 및 이미지 생성

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


```sh
# api/greeting 위치에서 실행
gradle build
docker build -t greeting .
```

```sh
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

배포전에 `k8s Service Account` 설정을 해야한다.  
`Spring Cloud Kubernetes` 에서 실행 중인 `Pod` 주소 목록, `ConfigMap` 을 검색할 수 있으려면 `k8s API`에 대한 액세스가 필요하다.  

`default` 계정이 각종 `k8s API` 를 사용할 수 있도록 권한을 부여.  

> <https://cloud.spring.io/spring-cloud-kubernetes/reference/html/#service-account>

위 url 에서 `Service Account` 섹션 확인 후 `Role`, `RoleBinding` 생성

```yaml
# name space
apiVersion: v1
kind: Namespace
metadata:
  name: spring
---
# rbac.yaml
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

```sh
kubectl apply -f rbac.yaml

# namespace/spring created
# role.rbac.authorization.k8s.io/namespace-reader created
# rolebinding.rbac.authorization.k8s.io/namespace-reader-binding created
```

```sh
kubectl apply -f calc-deployment.yaml

# deployment.apps/calc-deployment created
# service/calc-service created
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

`k8s CoreDNS`, `CNI` 에 의해 라우팅, 로드밸런싱이 이루어진다.  

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

### k8s 의존성 제거  

위와같이 `org.springframework.cloud` 라이브러리를 사용해  k8s와 연동하는 방법도 좋지만,  
k8s 라이브러리 의존성을 없에고 외부에서 환경변수를 사용해 주입시키는 방법도 있다.  

`SpringBoot` 버전에 따라 `ConfigMap` 을 가져오는 라이브러리도 달라지고, 언어 별로 k8s API 를 사용하는 방식이 다 다르기 때문에 `k8s API` 를 사용하지 않고 설정값을 가져올 수 있게 하는것이 좀 더 낫다 할 수 있다.  

설정값은 아래와 같이 `envFrom` 을 통해 환경변수로 등록하여 사용  

```yaml
    ...
    spec:
      containers:
        - name: greet-deployment
          image: ${REGISTRY_URL}/greeting:latest
          # image: greeting
          # imagePullPolicy: Never # 이미지가 로컬에 존재할 경우
          imagePullPolicy: Always
          envFrom:
            - secretRef:
                name: greeting-secret
```

더잉상 `spring-cloud-starter-loadbalancer` 의존성을 통해 서버 라우팅을 진행하지 않고 `k8s Service` 리소스 url 을 통해 접근하도록 설정

```java
@FeignClient(name="calc", url = "http://calc-service:8080")
public interface CalculatingClient {
    @GetMapping("/calculating/{num1}/{num2}")
    Long addNumbers(@PathVariable Long num1, @PathVariable Long num2);
}
```

`k8s CoreDNS` 를 통해 요청이 라우팅된다.  

### jib

> <https://github.com/GoogleContainerTools/jib>

기존에 java 기반 이미지를 생성하려면 Docker 데몬을 통해 빌드과정을 거쳐야 했지만  
jib 을 사용하면 Docker 데몬 없이도 이미지 생성이 가능하다.  

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

    def url = project.properties["registryUrl"].toString()
    def username = project.properties["registryUsername"].toString()
    def password = project.properties["registryPassword"].toString()

    jib {
        from {
            image = 'docker.io/library/openjdk:11'
        }
        to {
            image = "${url}/${project.name}"
            tags = ["latest"]
            setAllowInsecureRegistries(true) // docker registry 사설 인증서 허용
            auth.username = username
            auth.password = password
        }
    }
}
```

```sh
gradle clean api:calculating:jib \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345

gradle clean api:greeting:jib \
    -PregistryUrl=core.harbor.domain/demo \
    -PregistryUsername=admin \
    -PregistryPassword=Harbor12345
```

`jib` 플러그인을 설치하면 아래와 같은 3가지 task 를 사용할 수 있다.  

- `jib`: build your container image and Publish
- `jibBuildTar`: build your image directly to a Docker daemon
- `jibDockerBuild`: build and save your image to disk as a tarball

> `docker registry` 에 이미지를 업로드 하지 않는다면 `jibDockerBuild` 사용하면 된다.  
> `gradle :api:greeting:jibDockerBuild`  
> `gradle :api:calculating:jibDockerBuild`  


힘들게 docker build 명령어를 사용할 필요가 없으며  
`jib.to.image` 에 registry 주소까지 적용할 경우 자동으로 `image push` 됨으로 편하다.  

> 좀더 자세한 설명은 아래 url 참고  
> <https://github.com/GoogleContainerTools/jib/blob/master/jib-gradle-plugin/README.md>  

<!-- 
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

아래와 같은 에러문구가 뜬다면  

\
> <https://github.com/GoogleContainerTools/skaffold/issues/7985>

```shell
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?

sudo ln -s "$HOME/.docker/run/docker.sock" /var/run/docker.sock
```
-->


## 데모코드 

> <https://github.com/Kouzie/spring-kube-demo>
