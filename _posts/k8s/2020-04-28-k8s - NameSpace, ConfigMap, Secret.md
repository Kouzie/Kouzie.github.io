---
title:  "k8s - NameSpace, ConfigMap, Secret!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---

## 네임스페이스

k8s 리소스로 네임스페이스는 하나의 클러스터를 여러개의 논리적 단위로 나눠 사용하기 윈한 기능으로 클러스터 하나를 여러 팀이 함께 공유할 수 있음.  

> <https://kubernetes.io/ko/docs/concepts/overview/working-with-objects/namespaces/>

`Pod` 를 포함한 각종 k8s 리소스를 그룹화, 다른 그룹끼리는 분리화 하는 기능을 지원한다.  

네임스페이스만 다르면 동일한 리소스 네임을 사용할 수 있다.  
> 단 노드는 동일한 리소스명 사용 불가능  

k8s 설치시 자동으로 생성하는 네임스페이스들은 아래와 같다.  

```
$ kubectl get namespace
NAME              STATUS   AGE
default           Active   6d4h 
kube-public       Active   6d4h 
kube-system       Active   6d4h 
kube-node-lease   Active   6d4h 
```

* `default`: 기본 공간, 명령을 실행시 별도의 네임스페이스를 지정하지 않을경우 `default` 네임스페이스에 명령을 적용한다.  
* `kube-public`: 클러스터의 공용 공간, `ConfigMap` 같은 데이터가 저장되며 리소스 모니터링용으로 자주 사용된다.  
* `kube-system`:  k8s 시스템에서 관리하는 네임스페이스, 시스템 관리용 네임스페이스  
* `kube-node-lease`: 노드의 임대 오브젝트(`Lease object`)들을 관리하는 네임스페이스  

네임스페이스 내부에서 돌아가는 `pod` 목록 확인하면 아래와 같다.  

```
$ kubectl get pods --all-namespaces
NAMESPACE     NAME                                     READY   STATUS    RESTARTS   AGE
default       nginx-app-d6ff45774-nlvc8                1/1     Running   0          85m
default       nginx-app-d6ff45774-xwb82                1/1     Running   0          85m
kube-system   coredns-f9fd979d6-27z5q                  1/1     Running   0          177m
kube-system   coredns-f9fd979d6-z78k8                  1/1     Running   0          177m
kube-system   etcd-docker-desktop                      1/1     Running   0          176m
kube-system   kube-apiserver-docker-desktop            1/1     Running   0          176m
kube-system   kube-controller-manager-docker-desktop   1/1     Running   0          176m
kube-system   kube-proxy-g52r7                         1/1     Running   0          177m
kube-system   kube-scheduler-docker-desktop            1/1     Running   2          176m
kube-system   storage-provisioner                      1/1     Running   2          176m
kube-system   vpnkit-controller                        1/1     Running   0          176m
```

많은 종류의 파드가 `kube-system` 에 설정되어 동작하고 있고 새로만든 nginx 의 경우 기본 공간인 default 를 사용한다.  
지금까지 별도의 `namespace` 를 지정해서 `pod` 를 생성한적이 없기때문에 모두 `default` 네임스페이스에 포함되어 있을것.  

```yaml
## 
apiVersion: v1
kind: Namespace
metadata:
  name: trade-system
```

`kind: Namespace` 를 사용해 네임스페이스 생성  

```
$ kubectl create -f Namespace/namespace.yaml
namespace/trade-system created

$ kubectl get namespaces
NAME              STATUS   AGE
default           Active   6d4h
kube-node-lease   Active   6d4h
kube-public       Active   6d4h
kube-system       Active   6d4h
trade-system      Active   8s
```

선호하는 네임스페이스를 지속적으로 사용하기 위해 컨텍스트를 생성하고 해당 설정을 저장.  

```
$ kubectl config set-context my-context --namespace=trade-system
Context "my-context" created.

$ kubectl config get-contexts
CURRENT   NAME         CLUSTER    AUTHINFO   NAMESPACE
*         minikube     minikube   minikube
          my-context                         trade-system

# 컨텍스트 변경 가능
$ kubectl config use-context my-context 
Switched to context "my-context".

# 삭제
$ kubectl delete -f Namespace/namespace.yaml
namespace "trade-system" deleted
```

## ConfigMap

**애플리케이션 설정정보 관리를 위한 쿠버네티스 리소스** 로 `Key-Value` 형식의 파일형태의 데이터.  
주로 외부 API 키값, 환경변수 등을 별도로 관리하기 위한 리소스이다.  

공통 설정정보로 사용하기위해 볼륨 마운트를 통해 각 컨테이너에서 사용될 수 있다.  

### ConfigMap 생성방법

아래 2가지 방법을 주로 사용해 컨피그맵을 생성한다.  

**1. 매니페스트 파일로 만들기**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
    name: project-config
data:
    project.id: "hello-kubernetes"
    project.name: "world-kubernetes"
```

```
$ kubectl apply -f ConfigMap/configmap.yaml
$ kubectl get configmap
NAME             DATA   AGE
project-config   1      41s

$ kubectl describe configmap project-config
Name:         project-config
Namespace:    default
Labels:       <none>
Annotations:
Data
====
project.id:
----
hello-kubernetes
project.name:
----
world-kubernetes
Events:  <none>
```

**2. 어플리케이션 설정파일(`.ini`) 로 만들기**

```ini
; ui.ini - app에서 사용하는 config 파일
[UI]
color.top = blue
text.size = 10
```

위와 같은 `ini` 파일이 있을때 이를 사용해 쿠버네티스 리소스를 생성하자.  

```
$ kubectl create configmap app-config --from-file=ConfigMap/config/
configmap/app-config created

$ kubectl describe configmap app-config
Name:         app-config
Namespace:    default
Labels:       <none>
Annotations:  <none>

Data
====
ui.ini:
----
; app에서 사용하는 config 파일
[UI]
color.top = blue
text.size = 10

Events:  <none>
```

`file name`을 키값으로 `file content`가 `value` 로 들어갔다.  

### ConfigMap 전달  

파드에게 `ConfigMap` 의 데이터를 전달하는 방법은 여러가지다.  

1. `spec.containers.[].env[]` 속성으로 전달  
2. `spec.volumes` 으로 전달  

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: configmap-deployment
spec:
  replicas: 3
  selector:
    matchLabels:
      app: config-view
  template:
    metadata:
      labels:
        app: config-view
    spec:
      containers:
        - image: mydomain:5000/photo-view:v3.0
          name: photoview-container
          imagePullPolicy: Always
          ports:
            - containerPort: 80
          env:
            - name: PROJECT_ID # 환경변수명
              valueFrom:
                configMapKeyRef:
                  name: project-config
                  key: project.id
          volumeMounts:
            - name: config-volume
              mountPath: /etc/config
      volumes:
        - name: config-volume
          configMap:
            name: app-config
```

`spec.containers.[].env[]` 속성을 보면 위에서 만든 `project-config` 의 `project.id` 키에 해당하는 `value` 를 `PROJECT_ID` 환경변수로 할당한다.  

`spec.volumes` 속성을 보면 경우 위에서 만든 `app-config` 모든 정보를 `config-volume` 볼륨에 마운트한다.  

## Secret

**컨피그 맵과 같이 구성정보를 어플리케이션에 전달하기 위한 데이터**  
`DB password`, `OAuth` 토큰과 같은 비밀 정보를 주로 관리한다.  

> 암호화 되어 `etcd` 에서 관리됨.  

1. 내장 시크릿  
   `ServiceAccount` 생성시 같이 생성되며 kube api 를 호출할 때 사용됨
2. 사용자 정의 시크릿  
   비밀번호, 인증키와 같은 기밀데이터를 다룰때 사용하는 사용자가 직접 만든 configMap 과 같은리소스  

`ConfigMap` 과 매우 비슷하나 `etcd` 안에서 암호화된 상태로 관리된다.  

주의할점은 `key-value` 형식의 데이터로 저장할 경우 `base64` 로 인코딩 해서 저장해야 한다.  
`base64` 명령어로 인코딩, 디코딩 가능하다.  

```
$ echo -n 'dbuser' | base64
ZGJ1c2Vy

$ echo 'ZGJ1c2Vy' | base64 --decode
dbuser%
```

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: api-key
type: Opaque
data:
  id: ZGJ1c2Vy # dbuser
  key: YUJjRDEyMw== # aBcD123
```

### Secret Type

|**설정**|**설명**|
|---|---|
`Opaque` | 일반적인 `key-value` 형식
`kubernetes.io/tls` | `TLS` 정보
`kubernetes.io/dockerconfigjson` | `Docker Registry` 정보
`kubernetes.io/service-account-token` | `Service Account` 정보

> `TLS`: 전송 계층 보안(Transport Layer Security)  
`TLS`는 가장 최신 기술로 더 강력한 버전의 SSL입니다. 그러나 SSL이 더 일반적으로 사용되는 용어이기에, 여전히 보안 인증서는 SSL이라 불립니다.

### Secret 생성, 마운트

컨피그맵과 마찬가지로 `Secret` 도 매니페스트 파일을 작성하거나, 이미 존재하는 파일을 마운트 가능하다.  

1. `key-value` 데이터가 들어가 있는 시크릿 매니페스트 파일 작성  
2. `--from-file` 시크릿 데이터가 들어가있는 파일 경로 설정

```
$ kubectl apply -f Secrets/secrets.yaml

$ kubectl create secret generic apl-auth --from-file=Secrets/key/
secret/apl-auth created
```

생성한 시크릿은 컨피그맵과 마찬가지로 `spec.containers.[].env[]`, `spec.volumes` 속성으로 마운트 가능하다.  

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: secret-deployment
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
          imagePullPolicy: Always
          ports:
            - containerPort: 80
          env:
            - name: SECRET_ID # 환경변수명 
              valueFrom:
                secretKeyRef:
                  name: api-key # 위에서 만든 Secret 리소스명
                  key: id
            - name: SECRET_KEY
              valueFrom:
                secretKeyRef:
                  name: api-key
                  key: key
          volumeMounts:
            - name: secrets-volume
              mountPath: /etc/secrets
              readOnly: true
      volumes:
        - name: secrets-volume
          secret:
            secretName: apl-auth
```

실제 생성된 pod 에 접속해 생성된 환경변수로 파일 확인  

```
$ kubectl apply -f Secrets/deployment.yaml
deployment.apps/secret-deployment created

$ kubectl get pods
NAME                                 READY   STATUS    RESTARTS   AGE
secret-deployment-65dd57cfb7-cq2jh   1/1     Running   0          5s
secret-deployment-65dd57cfb7-kcpmv   1/1     Running   0          5s
secret-deployment-65dd57cfb7-v9gql   1/1     Running   0          5s

$ kubectl exec -it secret-deployment-65dd57cfb7-cq2jh -- /bin/bash

root@~# env | grep SECRET*
SECRET_KEY=aBcD123
SECRET_ID=dbuser

root@~# ls /etc/secrets/
apl.crt

root@~# cat /etc/mtab | grep /etc/secrets
tmpfs /etc/secrets tmpfs ro,relatime 0 0
```

> `tmpfs`: 메모리 기반 파일 시스템
