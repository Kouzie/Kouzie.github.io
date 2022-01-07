---
title:  "aws ECS!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - aws

toc: true
toc_sticky: true

---

# 개요 - ECS(Elastic Container Service)

기존에는 `EC2 Conatiner Service` 의 약자였지만 `Elastic Container Service` 로 변경되었다.  

마이크로 서비스 구축을 위한 서비스로 Docker 기반의 컨테이너 이미지를 효율적으로 관리할 수 있다. 

마이크로서비스에 발생하는 여러 문제들

- 서비스    Discovery   
- 서비스 Metadata(설정 및 보안  
- 서비스별 버전관리  
- 서비스별 캐시관리  

위와 같은 문제들은 여전히 존재하지만 ECS 에서는 AWS 의 각종 서비스
`API Gateway`, `Elastic Load Balancing` 와 같이 연계되어 쉽게 해결할 수 있다.   

컨테이너를 스케줄링, 배치, 서비스제공, 오토스케일링과 로드벨런싱 하기 위해 오케스트레이션 툴로 스웜이나 쿠버네티스를 많이 사용하지만  
AWS 에서는 오케스트레이션 기능을 모두 `ECS`가 제공하고 관리자는 매니징만 하면 되기에 별도의 환경구축을 할 필요가 없다.  

이런 이유때문에 `ECS` 는 오케스트레이션 툴 보다는 매니지드 서비스라 불린다.  

## ECS 구성요소

`ECS Cluster` - 인스턴스의 함대  
`ECS Task` - 인스턴스에서 실행되는 실제 작업  
`ECS Task Definitions` - 작업에 대한 환경 및 정의  
`ECS Cluster Management Engine` - 클러스터 리소스, 작업 상태 관리  
`ECS Scheduler` - 서비스이거나 배치 형태인지 파악후 EC2에 `Task` 를 스케줄링  
`ECS Agent` - 인스턴스간 통신하기 위한 클라이언트

![ddd1](/assets/2021/aws16-1.png)  

![ddd1](/assets/2021/aws16.png)  
> https://www.youtube.com/watch?v=_eBiF1Ut-KY

3개의 `EC2` 가 클러스터를 이루고 도커환경 내부에서 `ECS Task` 들이 동작하고 있다.  
`EC2`간 통신, `Cluster Management Engine` 끼리의 통신은 `ECS Agent` 를 통해 진행된다.   

`EC2` 나 `Blox` 같은 오픈소스 데몬들이 `ECS API`  에 접근해서 스케줄링이 가능하다.  


### ECS Task Definitions (작업정의)

`ECS Scheduler` 가 `ECS Task Definitions` 을 확인 후 실제 `EC2` 에서 동작할 `ECS Task(컨테이너)` 들을 생성한다.  

**Container Definition, Volume Definition**  
`ECS Task Definitions` 하기 전에 `Cluster` 에 생성될 `EC2` 의 컨테이너 이미지 주소, 사양, 네트워크설정, 환경변수, 볼륨 등 을 먼저 정의해야 한다.  

**테스크 종류**
대용량 데이터 처리를 하는 **배치형태** 테스크, 지속적인 **서비스형태** 테스크가 존재한다.  

테스크 종류마다 하나의 `Task` 에 여러 `EC2`를 구성할것인지(컴퓨팅, 데이터 스토어 역할 각각분리)
혹은 `Task` 당 하나의 `EC2`를 구성할 것인지(서비스의 경우 가용성을 위해 분리) 여러 작업 배치 전략을 사용할 수 있다.  

### ECS Service  

`Task Definition` 으로 `서비스형태의 Task` 를 정의하고 `ECS Scheduler` 가 클러스터 내부에 `ECS Service`를 생성할 수 있다.  

`Task Definition` 설정대로 개수를 지정하고 AWS 의 각종 서비스(`ELB, Auto Scaling` 등)와 결합되어 `자동복구, 로드벨런싱, 오토스케일링` 설정이 가능하다.  

즉 `ECS Service`란 특정 작업(`Task`) 인스턴스가 일정 갯수를 항상 유지할 수 있도록 관리해주는 객체로 `k8s`의 `Replicaset` 과 유사한 개념이다.  


### 작업 배치    

`ECS Scheduler` 는 `ECS Task Definitions`를 확인 후 `ECS Service` 가 생성되면서 `ECS Task(컨테이너도)` 같이 생성된다.  
`ECS Task` 는 `EC2` 에 할당되는데 이때 각 컨테이너의 배치 제약조건과 배치전략을 통해 배치한다.  

**클러스터 제약조건**
가장 기본적인 관리방식으로 인스턴스의 `CPU, Memory, Netowrk Port` 등 리소스가 부족할 경우 다른 인스턴스에 배치하도록 한다.  

**커스텀 제약조건**
사용자가 지정한 `expression` 에 따라 리소스를 배치한다.  
`--placement-constraints type="memberOf"` 옵션이 있을 경우 
아래와 같이 `expression` 에 지정한 제약조건대로 `Task` 를 배치할 수 있다.  

```shell
aws ecs run-task \
--cluster ecs-demo \ # cluser 이름
--task-definition myapp \ # task-definition 이름
--count 5 \ # 실행 개수
--placement-constraints type="memberOf", \ # 아래 표현식 사용
expression="attrubute:ecs.instance-type==g2.xlarge" # 커스텀 제약조건
```

**배치전략**  
컨테이너를 각 인스턴스에 어떻게 배치할지 전략을 지정한다.  
`--placement-strategy type="spread"` 옵션이 있을경우 최대한 넓은  AZ에 Task 를 배치한다.  
전략의 종류는 아래 4가지  
![ddd1](/assets/2021/aws17.png)  

**Binpacking(적재)** - 최대한 자원을 아끼는 배치방법  

**Spread(확산)** - 최대한 여러 AZ에 나눠 배치하는 방법  

**Affinity(유연)** - 2개의 `ECS Service` 가 같은 인스턴스에 있을경우 더 효율적일때 사용하는 배치방법

**Distnict Instance(고유 인스턴스)** - 인스턴스당 하나씩 배치방법



## ECR (Elastic Container Registry)

`docker` 의 cli 를 대부분 제공하는 `private registry` 라 할 수 있다.  
`S3`기반 저장이 이루어진다.  


## ECS 모니터링

Cloud Watch 

Demesion - 매트릭이 수집되는 단위(클럽스터이름단위, 서비스이름단위)


# CloudMap

> https://www.youtube.com/watch?v=Z0HXc34OJo0

MSA 구축 진행시 `Service Discovery` 기능은 필수이다.  
AWS 의 다양한 외부 리소스들을 연동/호출 하려면 어쨋건 해당 서비스의 IP 주소나 DNS 를 알아야 한다.  
`AWS Cloud Map` 을 사용하면 `Registry` 과정을 통해 아래 그림과 같이 특정 도메인을 지정하여 호출 가능하다.  

![ddd1](/assets/2021/aws33.png)  

`Cloud Map` 은 아래 3가지 컴포넌트로 구성된다.  

`Namespace`: 도메인과 매핑시킬 공간, 도메인이 곧 `Namespace`의 역할을 하게 된다.  

`Service`: `Namespace` 에서 동작하는 서비스

`Service Instance`: 해당 서비스를 실제 구현하는 인스턴스들  


## 사용방법

도메인이름이 있는 서비스의 경우 아래와 같이 `create-public-dns-namespace` 속성을 통해 `Namespace` `Service` `Service Instance` 생성 및 등록  

```
# 외부에서 lookup, API 조회가 가능한 이 가능한 dns 생성
$ aws servicediscovery create-public-dns-namespace --name kouzie.com

# DnsRecord 를 통해 서비스 생성시 DNS에 IP를 여러개 설정할건지(Multi Value) 지정 가능, TTL 60 이기 때문에 분마다 다른IP 를 반환하도록 설정한다.
$ aws servicediscovery create-service --name backend --dns-config "Namespace=%namepsace_id%, DnsRecords=[{Type=A, TTL=60}]"

# 실제 IP 를 가진 인스턴스를 등록
$ aws servicediscovery register-instance --service-id %service_id% --instance-id %id% --attributes \
AWS_INSTANCE_IPV4=192.168.10.2, \
stage=beta, \
version=1.0, \
ready=yes
```

`public-dns` 로 지정했기 때문에 어디에서든지 해당 dns 를 통해 `Service Discovery` 가 가능하다.  
만약 같은 VPC 안에서만 해당 dns 를 통해 `Service Discovery` 하고 싶다면 `private-dns` 생성하면 된다.  

`S3 Bucket` 이나 `DynamoDB Table` 등 DNS 가 없는 서비스에 접속하기 위한 `Namespace` `Service` `Service Instance` 생성하고 싶을때는 `create-http-namespace` 속성을 사용

```
&  aws servicediscovery create-http-namespace --name shared
& aws servicediscovery create-service --name logs --namepsace-id %namepsace_id%
& aws servicediscovery register-instance --service-id %service_id% --instance-id %id% --attributes \
ARN=arn:aws:s3:::testbucket, \
stage=beta, \
shard=s_1, \
read_only=no, \
path=/mylogs
```

`create-service` 서비스 사용시에 `--health-check-config "Type=TCP, FailtuerTreshold=3"` 속성을 통해 

등록된 `Service Instance` 들은 `dicover-instances` 명령을 통해 조회할 수 있다.  

```
$ aws servicediscovery discover-instances --namepsace-name shared --service-name logs
...
...
```

`dicover-instances` 는 `attribute` 명령어를 통해 서비스중 원하는 속성이 설정된 `Service Instance` 만 조회할 수 있다.  
`version` 이나 `stage` 등을 `attribute` 로 지정시 유연한 설계를 작성할 수 있다.  



# AppMesh

`AppMesh` 는 AWS 에서 제공하는 `Service Mesh` 를 위한 서비스이다.  

## Service Mesh

MSA 인프라 구성시 서비스간의 원활한 통신을 위해 사이드카 형식의 프록시를 두어 서비스 인스턴스가 네트워크 상황을 몰라도 사이드카 프록시를 통해 로드밸런싱 될 수 있도록 한다.  
그렇기에 어플리케이션 단에서 별도의 `Service Dicovery` 기능이나 `Client Base Loadbalancing` 을 수행할 필요가 없다.  

![ddd1](/assets/2021/aws34.png)  

크게 `Data Plane` 과 `Controle Plane` 으로 나뉜다.  
`Controle Plane` 은 `Data Plane` 의 Policy 를 설정하며 실제 패킷의 라우팅 경로, 프록시의 배치 등을 담당한다.  
`Data Plane` 으로는 `Envoy` `Nginx` 제품이 유명하며 대략적으로 아래 6가지 기능을 담당한다.  

- Service Discovery  
- Health Checking    
- Routing  
- Load Balancing  
- Authentication & Authorization  
- Observability(Monitoring)  

## AppMesh 구성요소  

![ddd1](/assets/2021/aws35.png)  


![ddd1](/assets/2021/aws36.png)  

`AppMesh` 의 가장 기본적인 구성은 그림과 같이 `(Virtual Node, Virtual Service)` `Virtual Router` `Route` 컴포넌트로 구성된다.  
`Virtual Node` 는 런타임 서비스의 추상컴포넌트로 여러 버전의 런타임 서비스를 분리해서 운영하거나 태그를 지정하는 등의 설정이 가능하다.  

**Virtual Node, Virtual Service**  

`Virtual Node`는 각 런타임 서비스의 논리적 단위를 나타내는 컴포넌트로 아래 3가지 설정이 가능하다.    

- **Listner**: Traffic 에 대한 `Port`, `Health Checking`, `CircuitBreaker`, `Retires` 에 대한 설정을 다룬다.  
- **Service Discovery**: `Caller` 가 해당 `Node` 를 찾아갈 수 있도록 도와줌
- **Backends**: 서비스 노드에서 동작하는 코드  

`Virtual Service` 는 `Virtual Node` 의 추상컴포넌트로 `Virtual Node` 를 가리킨다.  
`Virtual Router` 의 도움을 받아 여러 버전의 `Virtual Node` 로 미러링 전달 혹은 퍼센티지로 배포하는 등의 작업이 가능하다.  


```yaml
ColorGatewayVirtualNode:
    Type: AWS::AppMesh::VirtualNode
    DependsOn:
      - ColorTellerVirtualService
      - TcpEchoVirtualService
    Properties:
      MeshName: !Ref AppMeshMeshName
      VirtualNodeName: colorgateway-vn
      Spec:
        Listeners:
          - PortMapping:
              Port: 9080
              Protocol: http
        ServiceDiscovery:
          DNS:
            Hostname: !Sub "colorgateway.${ServicesDomain}"
        Backends:
          - VirtualService:
              VirtualServiceName: !Sub "colorteller.${ServicesDomain}"
          - VirtualService:
              VirtualServiceName: !Sub "tcpecho.${ServicesDomain}"
```

**Virtual Router**  
`Virtual Service` 로부터 트래픽을 등록된 서비스(`Virtual Node`)로 전달하기 위한 컴포넌트  

```yaml
  ColorTellerVirtualRouter:
    Type: AWS::AppMesh::VirtualRouter
    Properties:
      MeshName: !Ref AppMeshMeshName
      VirtualRouterName: colorteller-vr
      Spec:
        Listeners:
          - PortMapping:
              Port: 9080
              Protocol: http
```



**Route**  
`Virtual Router` 의 트래픽이 처리되는 방법을 정의한 설정  

`Virtual Router` 와 `Virtual Node` 의 이동경로를 작성하여 서로 엮는다.  
하나의 `Virtual Router` 에 여러개의 `Route` 가 등록될 수 있고  
각각의 `Route` 는 여러개의 `Virtual Node` 를 대상으로 경로지정이 가능하다.   

미러링 전송, 카나리아 전송 등의 설정이 가능하다.  

```yaml
ColorTellerRoute:
    Type: AWS::AppMesh::Route
    DependsOn:
      - ColorTellerVirtualRouter
      - ColorTellerWhiteVirtualNode
      - ColorTellerRedVirtualNode
      - ColorTellerBlueVirtualNode
    Properties:
      MeshName: !Ref AppMeshMeshName
      VirtualRouterName: colorteller-vr
      RouteName: colorteller-route
      Spec:
        HttpRoute:
          Action:
            WeightedTargets:
              - VirtualNode: colorteller-white-vn
                Weight: 1
              - VirtualNode: colorteller-blue-vn
                Weight: 1
              - VirtualNode: colorteller-red-vn
                Weight: 1
          Match:
            Prefix: "/"
```

`VirtualRouter(colorteller-vr)` 에 등록된 `Route` 로 `Spec` 속성을 보면 `/` 에 `Match` 되는 요청을 3개의 `VirtualNode(colorteller-white/blue/red-vn)` 에 각각 전송한다.  



## AppMesh 데모  

> https://github.com/aws/aws-app-mesh-examples/tree/main/examples



