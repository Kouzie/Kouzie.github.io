---
title:  "k8s - NameSpace, Role, ConfigMap, Secret!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---

## NameSpace

k8s 리소스로 네임스페이스는 하나의 클러스터를 여러개의 논리적 단위로 나눠 사용하기 윈한 기능으로 클러스터 하나를 여러 팀이 함께 공유할 수 있음.  


> <https://kubernetes.io/ko/docs/concepts/overview/working-with-objects/namespaces/>

특정 네임스페이스의 있는 자원들은 다른 네임스페이스의 자원에 공유되기 힘듬으로 분리성이 강함.  

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

## 인증, 권한

`kubectl` 명령을 사용해 `kube-apiserver` 에 접근해왔고  
`~/.kube/config` 파일을 사용해 인증과정을 거쳤다.  

`docker internal k8s` 를 사용한다면 config 정보는 아래와 같다.  

```yaml
apiVersion: v1
current-context: docker-desktop
kind: Config
clusters:
preferences: {}
- cluster:
    certificate-authority-data: ...
    server: https://kubernetes.docker.internal:6443 # 쿠버네티스 API 에 접속할 주소
  name: docker-desktop # 클러스터의 이름
contexts: # 사용자, 네임스페이스를 연결하는 설정
- context:
    cluster: docker-desktop # 접근할 클러스터를 설정
    user: docker-desktop # 클러스터에 접근할 사용자 그룹이 누구인지를 설정
  name: docker-desktop # 컨텍스트의 이름
users:
- name: docker-desktop # 사용자 그룹의 이름을 설정
  user:
    client-certificate-data: ... # 클라이언트 인증에 필요한 해시값(TSL 인증 기반)
    client-key-data: ... # 클라이언트의 키 해시값
```

`kube-apiserver` 접근시 사용자 인증과 더불어 **권한**을 검증한다.  
클러스터 하나를 여러 명이 사용할 때 Namespace 별로 사용자 권한을 구분하여 접근을 제한시키는것이 대부분이다.  

권한관리는 크게 아래 2가지로 나뉜다.  

* **ABAC**(Attribute-based access control)
  권한 설정 내용을 파일로 관리, 수정시  `kube-apiserver` 컴포넌트를 재시작해야 해서 잘 사용하지 않음
* **RBAC**(Role-based access control)  
  **사용자(ServiceAccount)** 와 **역할(Role)** 을 별개의 리소스로 생성한 후 두 가지를 **조합(RoleBinding)**

. `RBAC` 를 주로 사용함.  

`Role`(역할) 는 아래 두가지 `kind` 가 있음  

- Role  
- ClusterRole  

### Role

`일반 Role` 이라 부르며 롤이 속한 **네임스페이스의 사용 권한**을 관리한다.  

```yaml
kind: Role
apiVersion: rbac.authorization.k8s.io/v1 # RBAC
metadata:
  namespace: default # 네임스페이스
  name: read-role # 룰 이름
rules: # 룰 규칙
- apiGroups: [""] # core api 사용
  resources: ["pods"] # 접근 가능한 리소스 정의
  resourceNames: ["mypod"] # 접근 가능한 리소스의 name 설정
  verbs: ["get", "list"] # 리소스에서 어떤 동작을 할 건지 정의 
```

`verbs` 들어갈 수 있는 동작들은 아래와 같다.  

|verb|설명|
|---|---|
`Create` | 새로운 자원 생성
`Get` | 개별자원조회
`List` | 여러개자원조회
`Update` | 기존자원내용전체업데이트
`Patch` | 기존자원중일부내용변경
`Delete` | 개별자원삭제
`deletecollection` | 여러개자원삭제

### ClusterRole

`클러스터 Role` 이라 부르며 **클러스터 전체 사용 권한**을 관리한다.  

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: read-clusterrole
rules:
- apiGroups: [""] # core api 사용
  resources: ["pods"]
  verbs: ["get", "list"]
```

`metadata.namespace` 속성이 없는 것 빼고 일반 `Role` 과 크게 다를건 없다.  

`클러스터 Role` 엔 다른 `클러스터 Role` 설정을 가져올 수 있는 `aggregationRule` 속성이 있다.  
일치된 레이블의 `클러스터 Role` 을 가져와 집계(aggregation), 대부분 k8s 에 사전 정의되어있는 `클러스터 Role` 을 가져와 사용.   

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: admin-aggregation
aggregationRule:
  clusterRoleSelectors:
  - matchLabels:
      kubernetes.io/bootstrapping: rbac-defaults
      rbac.example.com/aggregate-to-monitoring: "true"
rules: []
```

클러스터의 관리용 API 호출 또한 접근가능한 리소스로 구분할 수 있다.  

```yaml
rules:
- nonResourceURLs: ["/healthcheck", "/metrics/*"] 
  verbs: ["get", "post"] # nonResourceURLs 은 get, post 2가지만 존재
```

### ServiceAccount

서비스계정 `ServiceAccount` 을 생성  

```yaml
apiVersion: v1
kind: ServiceAccount
metadata: 
  name: myuser
  namespace: default
```

### 룰 바인딩

`일반 Role` `클러스터 Role` 과 서비스계정을 묶기 위해 사용

`RoleBinding` `ClusterRoleBinding` 2가지 종류의 리소스를 사용한다.  

```yaml
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: read-rolebinding
  namespace: default # ServiceAccount, Role 모두 같은 네임스페이스에 있어야함
subjects: # 어떤 유형의 사용자 계정과 연결하는지 설정
- kind: ServiceAccount
  name: myuser
  apiGroup: "" # core api 사용
roleRef:
  kind: Role
  name: read-role
  apiGroup: rbac.authorization.k8s.io # RBAC 룰 사용
```

```yaml
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: read-clusterrolebinding
subjects:
- kind: ServiceAccount
  name: myuser
  namespace: default # namespace 지정 필수
  apiGroup: ""
roleRef:
  kind: ClusterRole
  name: read-clusterrole
  apiGroup: rbac.authorization.k8s.io
```

### 사용자 변경

> `deploy` 와 같은 클러스터 단위의 명령어를 사용하려면 `ClusterRole` 을 사용해야 한다.  

```yaml
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1 # RBAC
metadata:
  name: spring-role # 룰 이름
rules: # 룰 규칙
  - apiGroups: [""] # core api 사용
    resources: ["*"] # 접근 가능한 리소스 정의
    verbs: ["*"] # 리소스에서 어떤 동작을 할 건지 정의
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spring-user
  namespace: spring
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: spring-rolebinding
  namespace: spring
subjects:
  - kind: ServiceAccount
    name: spring-user
    namespace: spring
    apiGroup: ""
roleRef:
  kind: ClusterRole
  name: spring-role
  apiGroup: rbac.authorization.k8s.io # RBAC 룰 사용
```

유저 생성에 따라 자동으로 만들어진 `secret` 확인

```
kubectl get secret -n spring 
NAME                                TYPE                                  DATA   AGE
default-token-rplrs                 kubernetes.io/service-account-token   3      96m
spring-user-token-d4fjj   kubernetes.io/service-account-token   3      3m19s

kubectl describe secrets -n spring spring-user-token-d4fjj 
...
token:      eyJhbGciOiJSUzI1NiIsIm...
```

만들어진 secret 을 로컬 `context` 에 저장하고 `사용자명-클러스터명` 를 묶어 저장.

```
kubectl config set-credentials -n spring spring-user --token=eyJhbGciOiJ...
kubectl config set-context spring-context --cluster=cluster.dev --user=spring-user
```

사용하고자 하는 `private registry` 를 등록하기 위한 코드

```
kubectl patch -n spring serviceaccount spring-user -p '{"imagePullSecrets": [{"name": "docker-secret"}]}'
serviceaccount/spring-user patched
```

만들어진 `context` 로 현재 `context` 를 변경

```
kubectl config use-context spring-context 
Switched to context "spring-context".

kubectl config current-context                     
spring-context
```

현재 `context` 가 사용중인 `namespace` 를 `default` 에서 다른 `namespace` 로 지정

```
kubectl config set-context spring-context --namespace=spring
Context "spring-context" modified.

kubectl config get-contexts $(kubectl config current-context)
CURRENT   NAME                       CLUSTER       AUTHINFO                NAMESPACE
*         spring-context   cluster.dev   spring-user   spring
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
