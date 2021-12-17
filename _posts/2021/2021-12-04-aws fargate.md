---
title:  "aws Fargate!"

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


![ddd1](/assets/2021/aws16.png)  
> https://www.youtube.com/watch?v=_eBiF1Ut-KY

3개의 `EC2` 가 클러스터를 이루고 도커환경 내부에서 `ECS Task` 들이 동작하고 있다.  
`EC2`간 통신, `Cluster Management Engine` 끼리의 통신은 `ECS Agent` 를 통해 진행된다.   

`EC2` 나 `Blox` 같은 오픈소스 데몬들이 `ECS API`  에 접근해서 스케줄링이 가능하다.  


### Task Definition

`ECS Scheduler` 가 `ECS Task Definitions` 을 확인 후 실제 `EC2` 에서 동작할 `ECS Task` 들을 생성한다.  


**Container Definition, Volume Definition**  
`ECS Task Definitions` 하기 전에 `Cluster` 에 생성될 `EC2` 의 컨테이너 이미지 주소, 사양, 네트워크설정, 환경변수, 볼륨 등 을 먼저 정의해야 한다.  

**테스크 종류**
대용량 데이터 처리를 하는 **배치형태** 테스크, 지속적인 **서비스형태**   테스크가 존재한다.  

테스크 종류마다 하나의 `Task` 에 여러 EC2를 구성할것인지(컴퓨팅, 데이터 스토어 역할 각각분리)

Task 당 하나의 EC2를 구성할 것인지(서비스의 경우 가용성을 위해 분리)
여러 전략을 사용할 수 있다.  

## ECS Service  


`Task Definition` 으로 `서비스형태의 Task` 를 정의하고 `ECS Scheduler`가 클러스터 내부에 서비스를 생성할 수 있다.  

`Task Definition` 설정대로 개수를 지정하고 AWS 의 각종 서비스(ELB, Auto Scaling 등)와 결합되어 `자동복구, 로드벨런싱, 오토스케일링` 설정이 가능하다.  

### 컨테이너 배치  

`ECS Scheduler` 에서 컨테이너를 생성하고 각 EC2 에 배치할때 제약조건과 배치전략을 통해 배치한다.  

**클러스터 제약조건**
가장 기본적인 관리방식으로 인스턴스의 CPU, 메모리, Netowrk Port 등 리소스가 부족할 경우 다른 인스턴스에 배치하도록 한다.  

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

**Spread(확산)** - 여러 AZ에 최대한 나눠 배치하는 방법  

**Affinity(유연)** - 2개의 Service 가 같은 인스턴스에 있을경우 더 효율적일때 사용하는 배치방법

**Distnict Instance(고유 인스턴스)** - 인스턴스당 하나씩 배치방법


## ECS 모니터링

Cloud Watch 

Demesion - 매트릭이 수집되는 단위(클럽스터이름단위, 서비스이름단위)


## ECR (Elastic Container Registry)

`docker` 의 cli 를 대부분 제공하는 `private registry` 라 할 수 있다.  
`S3`기반 저장이 이루어진다.  



## 서비스 디스커버리


각 마이크로 서비스 아키텍쳐마다 다른 서비스 디스커버리 방식을 지원하겠지만
AWS ECS에서는 아래 방식으로 환경을 구축할 수 있도록 git에 샘플 아키텍처를 공유해놓앟다.  

> https://github.com/awslabs/ecs-refarch-service-discovery
![ddd1](/assets/2021/aws18.png)  

ECS 서비스가 ELB내부에 만들어질 경우 내부적으로 `Cloudtrail` 에서 이벤트를 감지하고 `Cloud Watch` 이 이벤트에 매핑되는 `Lambda` 를 호출하여 `Lambda`가 `Route53`에 해당 서비스의 DNS를 등록한다.  

그리고 생성된 DNS 와 ELB가 감지한 서비스의 포트를 엮어서 서비스의 엔드포인트 사용하게 된다.  

그리고 각 인스턴스에서 동작중인 Task 들은 `ECS Agent` 을 통해서 각 서비스 인스턴스의 주소를 가져오고 헬스체크 또한 람다로 검사하여 업데이트한다.  

### 서드파티  

아무래도 직접 구축하는 것 보단 이미 개발되어있는 서드파티 consul 이나 etcd 를 사용하는것이 일반적  

# 워크샵
