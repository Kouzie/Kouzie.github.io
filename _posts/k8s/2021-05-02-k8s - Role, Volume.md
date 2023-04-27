---
title:  "k8s - Role, Volume!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---

## 인증  

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

## 권한 관리

`kube-apiserver` 접근시 사용자 인증과 더불어 권한을 검증한다.  
클러스터 하나를 여러 명이 사용할 때 Namespace 별로 사용자 권한을 구분하여 접근을 제한시키는것이 대부분이다.  

권한관리는 크게 아래 2가지로 나뉜다.  

- **ABAC**(Attribute-based access control)  
  권한 설정 내용을 파일로 관리, 수정시  `kube-apiserver` 컴포넌트를 재시작해야 해서 잘 사용하지 않음

- **RBAC**(Role-based access control)  
  **사용자(ServiceAccount)** 와 **역할(Role)** 을 별개의 리소스로 생성한 후 두 가지를 **조합(RoleBinding)**

`RBAC` 를 주로 사용함.  

`Role`(역할) 는 아래 두가지 `kind` 가 있음  

- `Role`  
- `ClusterRole`  

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

## 볼륨

k8s 는 기본적으로 stateless 앱 컨테이너를 관리한다.  

하지만 DB 혹은 Jenkins 와 같이 앱 특성에 따라 데이터를 보관해야 하는 경우가 있다.  
k8s 에서도 앱 컨데이터 재시작시 데이터가 지워지지 않도록 `volume` 을 설정할 수 있다.  

> 클라우드 서비스에서 `awsElasticBlockStore`, `azureDisk`, `gcePersistentDisk` 등 클라우드 자체 볼륨 서비스도 제공한다.  

### 노드 볼륨  

k8s 에서 사용할 수 있는 volume 플러그인이 굉장히 많은데 아래 두가지만 알아본다.  

- emptyDir  
- hostPath  

`emptyDir` 은 파드가 실행되는 노드의 디스크를 임시로 컨테이너 볼륨으로 할당해 사용한다.  
파드가 사라지때 `emptyDir` 도 같이 사라진다.  
배치나 학습과 같은 오랜 시간이 걸릴 때 임시데이터 저장용으로 사용한다.  

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: kubernetes-simple-pod
spec:
  containers:
  - name: kubernetes-simple-pod
    image: mydomain:5000/photo-view:v2.0
    volumeMounts: # 마운트 설정
    - mountPath: /emptydir # 볼륨을 컨테이너의 /emptydir 위치에 마운트
      name: emptydir-vol # 마운트할 볼륨 이름
  volumes: # 볼륨 설정
  - name: emptydir-vol # 이름
    emptyDir: {} # emptyDir 설정
```

`hostPath` 또한 노드의 디스크를 컨테이너 볼륨으로 사용하지만 파드가 종료되더라도 데이터가 남는다.  
중요 데이터를 보존하는 역할로 사용하거나 노드의 시스템 디렉토리를 마운트해서 모니터링 용도로 사용할 수 있다.  

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: kubernetes-hostpath-pod
spec:
  containers:
  - name: kubernetes-hostpath-pod
    image: mydomain:5000/photo-view:v2.0
    volumeMounts:
    - mountPath: /test-volume
      name: hostpath-vol
  volumes:
  - name: hostpath-vol
    hostPath:
      path: /tmp # 경로
      type: Directory # 타입
```

`type` 으로는 아래와 같은 값들이 올 수 있다.  

- `DirectoryOrCreate`: 경로에 디렉토리가 없으면 755 권한으로 생성
- `Directory`
- `FileOrCreate`
- `File`
- `Socket`, `CharDevice`, `BlockDevice` 등

### PersistentVolume, PersistentVolumeClaim

위의 볼륨속성은 해당 k8s 서비스에 임의의 저장공간을 할당하지만  
k8s 매니페스트 파일을 사용해 PersistentVolume 을 만들면 다른 서비스도 사용할 수 있는 저장공간을 만들 수 있다.  

이때 사용하는 k8s 리소스 종류가 `PV(PersistentVolume)`와 `PVC(PersistentVolumeClaim)` 2가지 있다.  

- **PV**  
  볼륨 자체를 뜻함  
- **PVC**  
  사용자가 PV 에 하는 요청(사용 용량, rw 모드 등)

PV 와 PVC 의 매핑은 1:1 관계이다.  

#### PersistentVolume 생명주기  

볼륨이 생성되고 삭제되는 과정은 아래 그림과 같다.  

![kucbe8](/assets/k8s/k8s_volume1.png)  

**Provisioning**  
**프로비저닝은 PV 를 만드는 단계**로 아래 2가지 방법이 있다.  

- `static` 방법: `PV` 를 미리 만들어 두고 사용  
- `dynamic` 방법: 요청이 있을 때마다 `PV` 를 만듬  

**Binding**  
**PV 를 PVC 와 연결하는 단계**, `PVC` 에서 원하는 스토리지의 용량과 접근방법을 명시해 요청시 알맞은 `PV` 가 할당된다.  

**Using**  
**PVC 와 PV 가 바인딩 되어 사용되는 단계**, 파드가 `PVC` 를 볼륨으로 인식해서 `PV` 를 사용한다.  
`PVC` 가 사용되는 동안 시스템에서 임의로 삭제할 수 없음  

> 스토리지 오브젝트 보호(Storage Objectin Use Protection) 단계라고도 한다.  
> 사용중인 데이터 스토리지를 임의로 삭제하면 치명적인 결과가 발생할 수 있으므로  

**Reclaiming**  
**사용이 끝난 PVC 와 PV 가 삭제되는 단계**, `PVC` 를 사용하던 `PV` 는 반환된다. 반환 정책으로 아래 3가지 종류가 있음  

- `Delete`  
  PV 를 삭제시 볼륨도 같이 삭제됨, 기본반환정책이 `Delete`  
- `Retain`  
  `PV` 를 `released` 하고 데이터는 그대로 보존, 다시 연결해서 재사용 가능  
- `Recycle`  
  중단 예정 정책, `PV` 의 데이터들을 삭제하고 다시 새로운 `PVC` 에서 `PV` 를 사용할 수 있도록 함  

#### manifast

`PV` 와 `PVC` 를 생성하고 서로 연결한다.  

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-hostpath
spec:
  capacity:
    storage: 2Gi
  volumeMode: Filesystem # 볼륨을 파일 시스템 형식으로 설정
  accessModes: # 볼륨의 읽기/쓰기 옵션을 설정
  - ReadWriteOnce
  storageClassName: test # StorageClass 설정
  persistentVolumeReclaimPolicy: Delete
  hostPath:
    path: /tmp/k8s-pv
```

```yaml
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: pvc-hostpath
spec:
  accessModes:
  - ReadWriteOnce
  volumeMode: Filesystem
  resources:
    requests:
      storage: 1Gi
  storageClassName: test # StorageClass 설정
```

`accessModes` 종류는 아래 3개  

- `ReadWriteOnce`: 노드 하나에만 볼륨을 읽기/쓰기하도록 마운트할 수 있음
- `ReadOnlyMany`: 여러 개 노드에서 읽기 전용으로 마운트할 수 있음
- `ReadWriteMany`: 여러 개 노드에서 읽기/쓰기 가능하도록 마운트할 수 있음

`storageClassName` 은 `PV`, `PVC` 를 매핑하기 위한 문자열값  

> `.metadata.labels.location` 필드를 사용해 조건식으로 `PV`, `PVC` 를 매핑할 수 도 있으니 참고  

```
kubectl apply -f volume/pv-hostpath.yaml

Kubectl get pv
NAME          CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
pv-hostpath   2Gi        RWO            Delete           Available           manual                  120m

kubectl apply -f volume/pvc-hostpath.yaml

Kubectl get pvc
NAME           STATUS   VOLUME        CAPACITY   ACCESS MODES   STORAGECLASS   AGE
pvc-hostpath   Bound    pv-hostpath   2Gi        RWO            manual         5s

Kubectl get pv
NAME          CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                  STORAGECLASS   REASON   AGE
pv-hostpath   2Gi        RWO            Delete           Bound    default/pvc-hostpath   manual                  134m
```

`PV` 와 `PVC` 에 연결 전후 `STATUS` 변경사항 확인  
`RECLAIM POLICY` 가 `Delete` 임으로 `PVC` 가 해제되면 `PV` 도 같이 삭제된다.  

#### 파드에서 PVC 사용하기  

`emptyDir`, `hostPath` 와 마찬가지로 `volumeMounts`, `volumes` 속성을 사용한다.  

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubernetes-simple-app
  labels:
    app: kubernetes-simple-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kubernetes-simple-app
  template:
    metadata:
      labels:
        app: kubernetes-simple-app
    spec:
      containers:
      - name: kubernetes-simple-app
        image: arisu1000/simple-container-app:latest
        ports:
        - containerPort: 8080
        imagePullPolicy: Always
        volumeMounts: # 볼륨 매핑
        - mountPath: "/tmp"
          name: myvolume
      volumes: # 볼륨 설정
      - name: myvolume
        persistentVolumeClaim: # PVC 설정
          claimName: pvc-hostpath
```
