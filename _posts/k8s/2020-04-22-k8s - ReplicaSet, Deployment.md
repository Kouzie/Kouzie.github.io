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

## 리플리카셋 (ReplicaSet)

파드가 자동으로 상태검사, 정상화(재시작) 하며 상태를 복구하는 것이라면
**리플리카셋은 파드의 수를 유지, 자동으로 새로운 파드를 시작**한다.  

### 매니페스트

매니페스트 기본 구성은 파드와 같다.  
`spec` 부분만 다른부분이 있는데 알아보자.  

|**필드**|**타입**|**설명**|
|---|---|---|
`replicaset.spec.replicas` | `Integer` | 클러스터 안에서 가동시킬 파드의 수 (기본값 1)
`replicaset.spec.selector` | `LabelSelector` | 어떤 파드를 가동할지 정의, 파드의 `template.metadata.label` 에 설정된 값과 일치해야함
`replicaset.spec.template` | `PodTemplateSpec` | 실제 클러스터 내부 파드 수가 `replicas` 에 설정된 수보다 적을때 새로작성되는 파드**의 템플릿**
`replicaset.spec.template.metadata` | `Object` | 템플릿의 이름, `Label`과 같은 메타데이터 
`replicaset.spec.template.spec` | `PodSpec` | 파드의 상세정보를 설정  

즉 리플리카셋을 사용하려면 리플리카셋의 `spec` 과 파드의 `spec` 을 모두 정의해야한다.  

### 리플리카셋 CRUD

```yaml
# ReplicaSet/replicaset.yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: photoview-rs
spec:
  replicas: 5
  selector:
    matchLabels:
      app: photoview
  template:
    metadata:
      labels:
        app: photoview
        env: prod
    spec:
      containers:
      - image: mydomain:5000/photo-view:v1.0
        name: photoview-container
        ports:
          - containerPort: 80
```


```
kubectl create -f ReplicaSet/replicaset.yaml
kubectl get pods --show-labels
NAME                 READY   STATUS    RESTARTS   AGE     LABELS
photoview-rs-466w6   1/1     Running   0          3h35m   app=photoview,env=prod
photoview-rs-fwv2f   1/1     Running   0          3h35m   app=photoview,env=prod
photoview-rs-j46rr   1/1     Running   0          3h40m   app=photoview,env=prod
photoview-rs-n64z2   1/1     Running   0          3h35m   app=photoview,env=prod
photoview-rs-zk4bz   1/1     Running   0          3h35m   app=photoview,env=prod

# 파일을 수정 후 apply 하면 기존 리플리카셋의 설정이 자동 수정/적용 된다.  
kubectl apply -f ReplicaSet/replicaset.yaml
kubectl delete -f ReplicaSet/replicaset.yaml
```

리플리카셋은 철저히 `spec.template.metadata.labels.app` 을 통해서 관리된다.  

먼저 라벨명이 `photo-view` 인 **파드** 를 하나 생성  

```
kubectl apply -f ReplicaSet/pod-nginx.yaml
pod/nginx-pod created

kubectl get pod
NAME        READY   STATUS    RESTARTS   AGE
nginx-pod   1/1     Running   0          27s
```

그리고 리플리카셋으로 `photo-view` 라벨을 가진 파드 5개를 생성하도록한다.  

```
kubectl apply -f ReplicaSet/replicaset-nginx.yaml
replicaset.apps/nginx-replicaset created

kubectl get pod
NAME                     READY   STATUS              RESTARTS   AGE
nginx-pod                1/1     Running             0          79s
nginx-replicaset-cg8l7   0/1     ContainerCreating   0          2s
nginx-replicaset-hgkkd   0/1     ContainerCreating   0          2s
nginx-replicaset-kg5ks   0/1     ContainerCreating   0          2s
nginx-replicaset-p2w5f   0/1     ContainerCreating   0          2s
```

기존에 파드 매니페스트로 생성했던 `nginx-pod` 파드와 함께 4개의 추가 파드가 생성된다.  

## 디플로이먼트 (Deployment)

> Deployment: 전개, 배치  
> <https://kubernetes.io/ko/docs/concepts/workloads/controllers/deployment/>

쿠버네티스에선 어플리케이션의 유연한 `CI/CD` 를 위해 `Deployment` 를 제공한다.  

`Deployment` 는 `ReplicaSet`의 상위개념이다.  
똑같이 여러개의 파드를 생성하고 관리면서 **업데이트 관련 기능을 추가 제공한다**.  

**Recreate**  
오래된 파드를 정지시키고 새로운 파드를 다시 작성하는 방식.  
가장 심플하고 빠르지만 서버가 모두 내려가 버리기에 다운타임이 발생한다.  

**Rolling update**  
애플리케이션 버전업이 모두 한꺼번에 업데이트 되는것이 아닌 순서대로 조금씩 업데이트하는 방법  
똑같은 애플리케이션이 여러 개 병렬로 움직이는 경우 가능하다.  

**blue/green Deployment**   
버전이 다른 두 애플리케이션을 동시에 가동하고 네트워크 설정을 사용해 별도의 공간에서 동작시킨다.  
업데이트 버전의 애플리케이션 테스트 완료 후 서비스는 전환시켜 업데이트 완료.  
블루(구버전), 그린(신버전) 을 전환하는 뜻에서 유래됨.  
그린의 애플리케이션에서 장애 발생시 블루로 바로 복구 가능한 장점이 있다.  

**Roll out, Roll back**  
롤아웃(`roll-out`) 간단히 번역하면 신제품 또는 정책출시 또는 릴리즈라 할 수 있다.  
`Deployment`는 컨테이너 이미지 버전업 등 업데이트가 있을 때 사로운 사양의 리플리카셋(매니페스트) 를 작성하고  
그에 해당하는 새로운 파드로 이를 대체해 롤아웃을 수행한다.  

### 매니페스트 

```yaml
# 기본항목
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
# 디플로이먼트 스팩
spec:
  replicas: 10
  revisionHistoryLimit: 2 # replicaset version 을 2개까지만 유지, default 10
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 50%
      maxSurge: 50%
  selector:
    matchLabels:
      app: nginx-pod # 템플릿 검색조건
  # 파드 템플릿
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
        - name: nginx
          image: nginx:1.14 # 컨테이너 이미지
          ports:
            - containerPort: 80
```


`Deployment`의 매니페스트 또한 `spec` 속성이 좀 다를뿐 나머지는 비슷하다.  

|필드|설명|
|---|---|
`replicas` | 클러스터 안에서 가동시킬 파드의 수  
`selector` | 어떤 파드를 가동시킬지에 대한 셀렉터, 파드에 적용된 라벨을 사용한다.  
`template` | 클러스터 내부 파드 수가 리플리카수보다 작을때 새로 작성할 파드의 템플릿  
`strategy` | 업데이트 방식 결정 가능, `RollingUpdate`, `Recreate` 가 있으며 기본값은 `RollingUpdate`  
`maxUnavailable` | 롤링 업데이트중 항상 사용가능한 파드의 총수, 위의 경우 신버전, 구버전 합쳐서 리플리카수의 50%의 파드가 항상 동작중이어야 한다. 기본값은 25%  
`maxSurge` | 파드를 작성할 수 있는 최대 개수, 100%로 설정시 신버전의 파드수가 리플리카수만큼 실행되어 한번에 20개의 파드가 동작하게 된다. 기본값은 25%. 리소스 상황에 따라 특이사항이 발생할 수 있음으로 `maxSurge` 는 작성하는 것을 권장한다.  
`readinessProbe` | 실행한 파드가 정상인지 확인하는 속성 파드의 `livenessProbe` 와 비슷하다. 



- `livenessProbe`: 컨테이너가 동작 중인지 여부를 나타낸다. 만약 활성 프로브(liveness probe)에 실패한다면, `kubelet`은 컨테이너를 죽이고, 해당 컨테이너는 재시작 정책의 대상이 된다. 만약 컨테이너가 활성 프로브를 제공하지 않는 경우, 기본 상태는 `Success`이다.  

- `readinessProbe`: 컨테이너가 요청을 처리할 준비가 되었는지 여부를 나타낸다. 만약 준비성 프로브(readiness probe)가 실패한다면, 엔드포인트 컨트롤러는 파드에 연관된 모든 서비스들의 엔드포인트에서 파드의 IP주소를 제거한다. 준비성 프로브의 초기 지연 이전의 기본 상태는 `Failure`이다. 만약 컨테이너가 준비성 프로브를 지원하지 않는다면, 기본 상태는 `Success`이다.  

### 디플로이먼트 CRUD

도커 허브에서 바로 이미지를 가져와 디플로이먼트 CRUD

```
# 생성
kubectl create deployment --image=nginx nginx-app
deployment.apps/nginx-app created

# 스케일 업
kubectl scale deploy nginx-app --replicas=2
deployment.apps/nginx-app scaled

kubectl get pods
NAME                        READY   STATUS    RESTARTS   AGE
nginx-app-d6ff45774-m6frx   1/1     Running   0          113s
nginx-app-d6ff45774-q7r5v   1/1     Running   0          26s

kubectl get deployments
NAME        READY   UP-TO-DATE   AVAILABLE   AGE
nginx-app   2/2     2            2           2m17s

# 서비스 생성
kubectl expose deployment nginx-app --type=NodePort --port=80
service/nginx-app exposed

kubectl get service nginx-app # localhost:30403 접속
NAME        TYPE       CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
nginx-app   NodePort   10.104.196.2   <none>        80:30403/TCP   6s

# 상세 조회
kubectl describe service nginx-app
Name:                     nginx-app
Namespace:                default
Labels:                   app=nginx-app
Annotations:              <none>
Selector:                 app=nginx-app
Type:                     NodePort
IP Families:              <none>
IP:                       10.104.196.2
IPs:                      <none>
LoadBalancer Ingress:     localhost
Port:                     <unset>  80/TCP
TargetPort:               80/TCP
NodePort:                 <unset>  30403/TCP
Endpoints:                10.1.0.11:80,10.1.0.12:80 # 중요
Session Affinity:         None
External Traffic Policy:  Cluster
Events:                   <none>

# 삭제
kubectl delete deployment nginx-app
deployment.apps "nginx-app" deleted
```

매니페스트를 생성후 `Deployment` 리플리카셋과 파드가 생성되는지 확인  

```yaml
# 기본항목
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
# 디플로이먼트 스팩
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx-pod # 템플릿 검색조건
  # 파드 템플릿
  template:
    metadata:
      labels:
        app: nginx-pod
    spec:
      containers:
        - name: nginx
          image: nginx:1.15 # 컨테이너 이미지
          ports:
            - containerPort: 80
```

```
kubectl apply -f Deployment/nginx-deployment.yaml
deployment.apps/nginx-deployment created

kubectl get deploy
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   3/3     3            3           24s

kubectl get replicaset,pod
NAME                                          DESIRED   CURRENT   READY   AGE
replicaset.apps/nginx-deployment-5bff7844cb   3         3         3       5m11s

NAME                                    READY   STATUS    RESTARTS   AGE
pod/nginx-deployment-5bff7844cb-6n2xh   1/1     Running   0          5m11s
pod/nginx-deployment-5bff7844cb-9jmvt   1/1     Running   0          5m11s
pod/nginx-deployment-5bff7844cb-kc7ph   1/1     Running   0          5m11s
```

디플로이먼트는 내부에서 리플리카셋, 파드 이력을 갖고 있다. `kubectl get replicaset` 명령에도 조회가 가능하다.  

리소스의 네이밍 규칙이 있다.  

`Deployment` - `nginx-deployment`  
`ReplicaSet` - `nginx-deployment-5bff7844cb`  
`Pod` - `nginx-deployment-5bff7844cb-kc7ph`  

뒤에 특정 해시값이 라벨로도 붙어있으며 이를 사용해 `Deployment`가 리소스를 관리한다.  


기존의 `nginx:1.14` 버전의 이미지를 `nginx:1.15` 로 변경해보자.  

```
kubectl apply -f Deployment/nginx-deployment.yaml
deployment.apps/nginx-deployment configured

kubectl describe deploy nginx-deployment
Name:                   nginx-deployment
Namespace:              default
...
...
OldReplicaSets:  <none>
NewReplicaSet:   nginx-deployment-f75fb748c (3/3 replicas created)
Events:
  Type    Reason             Age   From                   Message
  ----    ------             ----  ----                   -------
  Normal  ScalingReplicaSet  28m   deployment-controller  Scaled up replica set nginx-deployment-5bff7844cb to 3
  Normal  ScalingReplicaSet  18s   deployment-controller  Scaled up replica set nginx-deployment-f75fb748c to 1
  Normal  ScalingReplicaSet  4s    deployment-controller  Scaled down replica set nginx-deployment-5bff7844cb to 2
  Normal  ScalingReplicaSet  4s    deployment-controller  Scaled up replica set nginx-deployment-f75fb748c to 2
  Normal  ScalingReplicaSet  3s    deployment-controller  Scaled down replica set nginx-deployment-5bff7844cb to 1
  Normal  ScalingReplicaSet  3s    deployment-controller  Scaled up replica set nginx-deployment-f75fb748c to 3
  Normal  ScalingReplicaSet  2s    deployment-controller  Scaled down replica set nginx-deployment-5bff7844cb to 0
```

`nginx-deployment-5bff7844cb` 이름의 리플리카셋의 파드를 하나씩 줄이며  
`nginx-deployment-f75fb748c` 이름의 새로운 리플리카셋의 파드를 하나씩 생성한다.  

디플로이먼트 이력(history)을 확인하려면 `rollout history` 옵션을 사용한다.  

```
kubectl rollout history deploy rollout-deployment --revision=3
deployment.apps/rollout-deployment with revision #3
Pod Template:
  Labels: app=photo-view pod-template-hash=d8bf6cb58
  Containers:
   photoview-container:
    Image: mydomain:5000/photo-view:v1.0
    Port: 80/TCP
    Host Port: 0/TCP
    Environment: <none>
    Mounts: <none>
  Volumes: <none>

kubectl rollout history deploy rollout-deployment --revision=4
deployment.apps/rollout-deployment with revision #4
Pod Template:
  Labels: app=photo-view pod-template-hash=6b5ddcb6b7
  Containers:
   photoview-container:
    Image: mydomain:5000/photo-view:v2.0
    Port: 80/TCP
    Host Port: 0/TCP
    Environment: <none>
    Mounts: <none>
  Volumes: <none>
```

### Roll out, Roll back  

디플로이먼트를 업데이트하고 문제가 생기면 다시 이전버전으로 돌릴 수 있는 기능(롤백) 을 사용해보자.  

먼저 아래와 같은 디플로이먼트를 생성 후 실제 서비스까지 동작하는지 확인  

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rollout-deployment
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
        - image: mydomain:5000/photo-view:v1.0
          name: photoview-container
          ports:
            - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: rollout
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 80
      protocol: TCP
  selector:
    app: photo-view
```

```sh
kubectl apply -f Deployment/rollout-depoyment.yaml

# deployment.apps/rollout-deployment created
# service/rollout created
```

현재 사용중인 이미지 `photo-view:v1.0` 를 `photo-view:v2.0` 으로 수정 후 다시 적용(`Roll out`)

```sh
kubectl apply -f Deployment/rollout-depoyment.yaml
# deployment.apps/rollout-deployment configured
# service/rollout unchanged
``` 

`describe` 명령으로 출력된 `Annotations` 속성으로 `revision` 값 확인  
해당 디플로이먼트가 **몇번 업데이트** 되었는지 확이 가능하다.  

```sh

kubectl describe deploy rollout-deployment
# Name:                   rollout-deployment
# Namespace:              default
# ...
# Annotations:            deployment.kubernetes.io/revision: 2
# ...
# Events:
#   Type    Reason             Age   From                   Message
#   ----    ------             ----  ----                   -------
#   Normal  ScalingReplicaSet  41s   deployment-controller  Scaled up replica set rollout-deployment-d8bf6cb58 to 3
#   Normal  ScalingReplicaSet  27s   deployment-controller  Scaled up replica set rollout-deployment-6b5ddcb6b7 to 1
```

`d8bf6cb58 -> 6b5ddcb6b7` 해시의 변경값이다.  

`photo-view:v2.0` 에 문제가 있단 가정 하에, 업데이트된 디플로이먼트를 다시 이전버전으로 `rollback` 해보자.  

첫번째 방법으로 탬플릿 이미지를 `photo-view:v1.0` 로 다시 적용해보자.  

```sh
kubectl apply -f Deployment/rollout-depoyment.yaml
# deployment.apps/rollout-deployment configured
# service/rollout unchanged

kubectl describe deploy rollout-deployment
# ...
# Annotations:            deployment.kubernetes.io/revision: 3
# ...

kubectl get pod
# NAME                                 READY   STATUS    RESTARTS   AGE
# rollout-deployment-d8bf6cb58-9nwh5   1/1     Running   0          33s
# rollout-deployment-d8bf6cb58-t2f4l   1/1     Running   0          34s
# rollout-deployment-d8bf6cb58-tgg57   1/1     Running   0          31s
```

`revision`값은 3이 되었고 해시값이 새롭게 변하지 않고 다시 `d8bf6cb58` 로 돌아갔다.  
`[Deployment, Replicaset, Pod]` 모두 매니페스트 이력을 가지고 롤아웃/롤백을 진행하고 있기 때문.  

이 외에도 현재 사용중인 매니페스트 수정하거나 `kubectl rollout` 명령을 사용해 롤백이 가능하다.  

```sh
kubectl edit deploy rollout-deployment
kubectl rollout undo deployment rollout-deployment --to-revision=2 
```

구성관리가 유지되지 않음으로 매니페스트 파일의 갱신(선언적 관리)으로 롤아웃/롤백 하는것을 권장한다.  

### 블루/그린 디플로이먼트  

블루/그린 이라고 별다른 기법이 있는건 아니다.

서로 다른 버전의 2개의 디플로이먼트를 각각 생성하고 이에 접근하는 `Service` 리소스를 변경하는 것이다.

```yaml
# blue-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: blue-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: photo-view
  template:
    metadata:
      labels:
        app: photo-view
        ver: v1.0
    spec:
      containers:
        - image: mydomain:5000/photo-view:v1.0
          name: photoview-container
          ports:
            - containerPort: 80
```

```yaml
# green-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: green-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: photo-view
  template:
    metadata:
      labels:
        app: photo-view
        ver: v2.0
    spec:
      containers:
        - image: mydomain:5000/photo-view:v2.0
          name: photoview-container
          ports:
            - containerPort: 80
```

```sh
kubectl apply -f Deployment/blue-deployment.yaml
# deployment.apps/blue-deployment created

kubectl apply -f Deployment/green-deployment.yaml
# deployment.apps/green-deployment created

kubectl get pod
# NAME                                 READY   STATUS        RESTARTS   AGE
# blue-deployment-58d5d4869b-kmn88     1/1     Running       0          19s
# blue-deployment-58d5d4869b-vlx2c     1/1     Running       0          19s
# blue-deployment-58d5d4869b-w4729     1/1     Running       0          19s
# green-deployment-5466fc4568-7hxqp    1/1     Running       0          15s
# green-deployment-5466fc4568-mvs2w    1/1     Running       0          15s
# green-deployment-5466fc4568-t8hp2    1/1     Running       0          15s
```

그리고 이 `Deployment` 에 접근하는 서비스를 작성하고 실행한다.  

```yaml
apiVersion: v1
kind: Service
metadata:
  name: webserver
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 80
      protocol: TCP
  selector: # 접근할 파드 수정
    app: photo-view
    ver: v1.0
```

```sh
kubectl apply -f Deployment/service.yaml
# service/webserver created
```

이제 `service.yml` 매니페스트 파일만 수정해서 두 버전의 디플로이먼트에 접근할 수 있도록 설정하면 된다.  

## 오토스케일링

순간적으로 많은양의 요청이 들어올 경우 시스템 처리능력을 높이는 방법  
시스템 처리능력을 높이는 방법은 2가지가 있다.  

- **스케일 아웃(수평 스케일)**: 시스템 구성 서버 대수를 늘림으로 처리능력 향상, 로드밸런서를 추가해야 하며 시스템의 가용성이 높아진다.  
- **스케일 업(수직 스케일)**: 서버의 리소스(CPU, Memory) 를 증가 시켜 처리능령 향상시킨다.  

처리량의 증가정도에 따라 파드를 스케일할지 노드를 스케일할지 결정한다.  

가장 쉽게 스케일 아웃하는 방법은 `kubectl scale` 명령으로 수동으로 진행하는 것이다.  

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

`nginx` 파드를 3개 생성하고 `kubectl scale` 명령으로 `replicas` 개수를 8개 까지 스케일 아웃

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

### HPA(Horizontal Pod Autoscaler)

`HPA(Horizontal Pod Autoscaler)` 를 사용하면 CPU 사용률에 따라 사용해 자동으로 스케일 아웃 가능하다.  

k8s 의 마스터 컴포넌트인 컨트롤러 매니저가 주기적으로 파드들의 CPU 를 감시하며 HPA 설정대로 스케일 아웃한다.  

스케일 아웃 조건은 아래와 같다.  

```
목표 파드 개수 = (현재 파드의 CPU 사용률을 모두 더한 값 / 목표 CPU 사용률) 올림값
```

> 목표 CPU 사용률이 60 이고 파드 2개 CPU 사용률이 각각 50, 80 일 때  
> 위 수식값은 130/60=2.17, 올림하면 3 이다.  
> 목표 파드개수는 3개가 된다.  

```yaml
apiVersion: autoscaling/v1
kind: HorizontalPodAutoscaler
metadata:
  name: kubernetes-simple-app-hpa
  namespace: default
spec:
  minReplicas: 1 # 최소 설정개수
  maxReplicas: 10 # 최대 설정개수
  scaleTargetRef: # 오토스케일링 대상
    apiVersion: extensions/v1beta1
    kind: Deployment
    name: kubernetes-simple-app
  targetCPUUtilizationPercentage: 30 # CPU 사용률
```

> `Deployment`, `Relication Controller`, `StatefuleSet` 등의 리소스에서 오토스케일링 가능  

`kubectl autoscale` 명령으로도 설정 가능하다.  

```
kubectl autoscale \
 deployment kubernetes-simple-app \
 --cpu-percent=30 \
 --min=1 --max=10
```

`autoscaling/v2` 부턴 `metrics` 설정을 통해 CPU 외에 다른 메트릭 데이터를 통해 오토스케일링 가능하다.  

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: kubernetes-simple-app-hpa
  namespace: default
spec:
  minReplicas: 1 # 최소 설정개수
  maxReplicas: 10 # 최대 설정개수
  scaleTargetRef: # 오토스케일링 대상
    apiVersion: extensions/v1beta1
    kind: Deployment
    name: kubernetes-simple-app
  metrics:
  - type: Resource
    resource:
      name: cpu
      targetAverageUtilization: 30  # CPU 가 30% 되도록 조정
```

한번 스케일 아웃 HPA 가 발동하면 다음번 HPA 는 3분뒤에 다시 발동가능하다.  
(줄어드는건 5분)

아래 k8s 컨트롤 매니저 설정파일에서 조절 가능하다.  

```
/etc/kubernetes/manifests/kube-controller-manager.yaml

--horizontal-pod-autoscaler-upscale-delay=3m0s
--horizontal-pod-autoscaler-downscale-delay=5m0s
```
