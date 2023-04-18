---
title:  "k8s - ReplicaSet, Deployment!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---


## 스케일러빌리티

순간적으로 많은양의 요청이 들어올 경우 시스템 처리능력을 높이는 방법  
시스템 처리능력을 높이는 방법은 2가지가 있다.  

- 스케일 아웃(수평 스케일): 시스템 구성 서버 대수를 늘림으로 처리능력 향상, 로드밸런서를 추가해야 하며 시스템의 가용성이 높아진다.  
- 스케일 업(수직 스케일): 서버의 리소스(CPU, Memory) 를 증각 시켜 처리능령 향상  

수직이던 수평이던 파드와 노드단위로 스케일(처리능령 향상) 가능하다.  

처리량의 증가정도에 따라 파드를 스케일할지 노드를 스케일할지 결정한다.  

### 파드 수동 수평 스케일

`kubectl scale` 명령으로 파드 스케일을 늘릴 수 있다.  

리플리카셋으로 `nginx` 파드를 3개 생성  

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: nginx-replicaset
spec:
  replicas: 3
  selector:
    matchLabels:
      app: photo-view
  template:
    metadata:
      labels:
        app: photo-view
    spec:
      containers:
      - image: nginx
        name: photoview-container
```


```
kubectl apply -f HPA/pod-scale.yaml
replicaset.apps/nginx-replicaset created

kubectl scale --replicas=8 rs/nginx-replicaset
replicaset.apps/nginx-replicaset scaled

kubectl get pod
NAME                     READY   STATUS    RESTARTS   AGE
nginx-replicaset-4bv5g   1/1     Running   0          19s
nginx-replicaset-4cjjq   1/1     Running   0          19s
nginx-replicaset-5z9qk   1/1     Running   0          19s
nginx-replicaset-bt98h   1/1     Running   0          19s
nginx-replicaset-c5fkk   1/1     Running   0          2m29s
nginx-replicaset-hnv8w   1/1     Running   0          2m29s
nginx-replicaset-nxtcn   1/1     Running   0          19s
nginx-replicaset-zbsd6   1/1     Running   0          2m29s
```

### 파드 자동 수평 스케일

`kubectl scale` 명령으로 수동으로 스케일하였다면 `HorizontalPodAutoscaler` 쿠버네티으 리소스를 사용해 자동으로 수평 스케일 가능하다.  
쿠버는 `CPU` 사용률, 기타 메트릭을 체크해 파드 스케일이 가능하다.  

> 리플리카셋 외에도 `Deployment`, `Relication Controller`, `StatefuleSet` 등의 리소스에서 스케일링 가능하다.  

먼저 스케일전의 리플리카셋을 생성한다.  

```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: busy-replicaset
spec:
  replicas: 2
  selector:
    matchLabels:
      app: busy
  template:
    metadata:
      labels:
        app: busy
    spec:
      containers:
      - image: busybox
        name: hpa-container
        command: ["dd", "if=/dev/zero", "of=/dev/null"]
        resources:
          requests:
            cpu: 100m
          limits:
            cpu: 100m
```

자동 스케일을 위해선 `requests`, `limits` 속성을 둘다 설정할 필요가 있다.  
내부에서 dd 명령을 통해 `limits` 인 `100m` 만큼의 코어가 되어있다.  

```
kubectl apply -f HPA/replicaset-hpa.yaml
replicaset.apps/busy-replicaset created

kubectl get pod --show-labels
NAME                    READY   STATUS    RESTARTS   AGE   LABELS
busy-replicaset-w97wr   1/1     Running   0          35s   app=busy
busy-replicaset-z5sh8   1/1     Running   0          35s   app=busy

kubectl top pod
Error from server (NotFound): the server could not find the requested resource (get services http:heapster:)
```

이 상태에서 자동 스케일 가능하도록 `HorizontalPodAutoscaler` 리소스를 작성  

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: budy-hpa
spec:
  minReplicas: 1   # 최소 리플리카 수
  maxReplicas: 5   # 최대 리플리카 수
  metrics:
  - resource:
      name: cpu
      targetAverageUtilization: 30  # CPU 가 30% 되도록 조정
    type: Resource
  scaleTargetRef: # 자동스케일 대상 리소스(리플리카셋)를 결정
    apiVersion: apps/v1
    kind: ReplicaSet
    name: busy-replicaset
```


## 잡

웹, DB 서버와 같은 상주 서비스는 파드를 통해 항시 관리되지만  
기계학습, 배치처리는 `Job`, `CronJob` 을 통해 실행되고 정지(실행완료)된다.  

**Job**  
`Job`은 하나 이상의 파드에서 배치처리를 하기 위한 리소스.  
DB 마이크레이션같은 한번의 실행으로 끝나는 것에 이용된다.  

`Job` 의 실행 오류, 예외 발생시 `Job` 컨트롤러가 성공할 때까지 파드를 새로 생성해 실행시킨다.

**CronJob**  
`CronJob` 은 정해진 시간에 `Job` 을 수행하기 위한 리소스.  
백업, 메일 송신 등과 같은 배치처리에 사용된다.  