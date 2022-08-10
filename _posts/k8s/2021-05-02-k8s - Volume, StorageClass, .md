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

## Volume

`k8s` 는 기본적으로 `stateless` 앱 컨테이너를 관리한다.  

하지만 `DB, Storage` 같은 서비스 특성에 따라 데이터를 영구 보관해야 하는 경우가 있다.  
`k8s` 에서도 restart 시 데이터가 지워지지 않도록 `Volume` 을 설정할 수 있다.  

> 상용 클라우드 서비스에서 `awsElasticBlockStore`, `azureDisk`, `gcePersistentDisk` 등 클라우드 자체 볼륨 서비스도 제공한다.  

k8s 에서 사용할 수 있는 `Volume` 플러그인이 굉장히 많은데 아래 두가지만 알아본다.  

- emptyDir  
- hostPath  

`emptyDir` 은 `Pod` 가 실행되는 노드의 디스크를 임시로 `컨테이너 볼륨`으로 할당해 사용한다. `Pod` 가 사라지때 `emptyDir` 도 같이 사라진다.  
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

`hostPath` 또한 노드의 디스크를 `컨테이너 볼륨`으로 사용하지만 `Pod` 가 종료되더라도 데이터가 남는다.  
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

## PV, PVC

위의 `spec.volumes` 속성은 해당 `k8s` 서비스에 임의의 저장공간을 할당하지만,  
`PersistentVolume` 을 만들면 다른 서비스도 사용할 수 있는 저장공간을 만들 수 있다.  

이때 사용하는 k8s 리소스 종류가 아래 2가지  

- `PV(PersistentVolume)`  
  Volume 자체를 뜻함  
- `PVC(PersistentVolumeClaim)`  
  사용자가 PV 에 하는 요청(사용 용량, rw 모드 등)

![1](/assets/k8s/k8s_volume2.png)  

> PV 와 PVC 의 매핑은 1:1 관계이다.  
> 대부분의 경우 클러스터 관리자가 `PV` 을 생성하고, 개발자가 `PVC` 를 생성한다.

### manifast

`PV` 와 `PVC` 를 생성하고 서로 연결한다.  

```yaml
# pv-hostpath.yaml
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
  persistentVolumeReclaimPolicy: Delete
  hostPath:
    path: /tmp/k8s-pv
  clameRef:
    name: pv-hostpath
    namespace: default
```

PV 의 `persistentVolumeReclaimPolicy` 종류는 아래 3개.  

- **Delete**: `PVC` 삭제시 볼륨도 같이 삭제됨, 기본반환정책이 `Delete`.  
- **Retain**: `PVC` 삭제되어도 `PV` 는 그대로 보존, `PVC` 를 다시 생성해서 재사용 가능.  
- **Recycle**: deprecated 됨, `PV` 의 데이터들을 삭제하고 다시 새로운 `PVC` 에서 `PV` 를 사용할 수 있도록 함.  

여기선 `persistentVolumeReclaimPolicy` 가 `Delete` 임으로 `PVC` 가 해제되면 `PV` 도 같이 삭제된다.  

```yaml
# pvc-hostpath.yaml
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
```

`PVC` 의 `accessModes` 종류는 아래 3개  

- `ReadWriteOnce`: 노드 하나에만 볼륨을 읽기/쓰기하도록 마운트할 수 있음
- `ReadOnlyMany`: 여러 개 노드에서 읽기 전용으로 마운트할 수 있음
- `ReadWriteMany`: 여러 개 노드에서 읽기/쓰기 가능하도록 마운트할 수 있음

`PV` 의 `clameRef` 를 통해 request 하는 `PVC` 를 제한할 수 있다.  

> `.metadata.labels.location` 필드를 사용해 조건식으로 `PVC` 를 매핑할 수 도 있으니 참고  

`PVC` 의 `resources` 속성을 보면 PV 의 용량보다 적어도 상관 없는 이유는 조건에 따라 용량이 조금 오바되어도 상관없는 정책이 있기 때문

```sh
# pv 생성
kubectl apply -f pv-hostpath.yaml
Kubectl get pv
# NAME          CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
# pv-hostpath   2Gi        RWO            Delete           Available                                   120m

# pvc 생성
kubectl apply -f volume/pvc-hostpath.yaml
kubectl get pvc
# NAME           STATUS   VOLUME        CAPACITY   ACCESS MODES   STORAGECLASS   AGE
# pvc-hostpath   Bound    pv-hostpath   2Gi        RWO                           5s
kubectl get pv
# NAME          CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                  STORAGECLASS   REASON   AGE
# pv-hostpath   2Gi        RWO            Delete           Bound    default/pvc-hostpath                           134m
```

`PV` 에 `PVC` 에 연결 된 후 `STATUS` 상태가 `Available -> Bound` 로 변경됨을 확인.  

### Pod 에서 PVC 사용

파드에서 `PVC` 사용하기 위해서 마찬가지로 `volumeMounts`, `volumes` 속성을 사용한다.  

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

### PV 생명주기  

`PV(볼륨)` 가 생성되고 삭제되는 과정은 아래 그림과 같다.  

![1](/assets/k8s/k8s_volume1.png)  

**1. Provisioning, PV 를 만드는 단계**  
아래 2가지 방법이 있다.  

- `static` 방법: `PV` 를 미리 만들어 두고 사용  
- `dynamic` 방법: 요청이 있을 때마다 `PV` 를 만듬  

**2. Binding, PV 를 PVC 와 연결하는 단계**  
`PVC` 에서 원하는 스토리지의 용량과 접근방법을 명시해 요청시 알맞은 `PV` 가 할당된다.  

**3. Using, PVC 와 PV 가 바인딩 되어 사용되는 단계**  
`Pod` 가 `PVC` 를 볼륨으로 인식해서 실제 디스크에 읽기/쓰기 진행,  
`PVC` 가 사용되는 동안 시스템에서 임의로 삭제할 수 없음  

> 스토리지 오브젝트 보호(Storage Objectin Use Protection) 단계라고도 한다.  
> 사용중인 데이터 스토리지를 임의로 삭제하면 치명적인 결과가 발생할 수 있으므로  

**4. Reclaiming, 사용이 끝난 PVC 와 PV 가 삭제되는 단계**  
사용중이던 `PVC` 를 제거, `PV` 반환 정책에 의해 저장공간이 삭제되거나 유지됨.  

## StorageClass

위에서 `PV` 는 클러스터 관리자가 수동으로 생성한다 하였는데 이러한 과정이 번거로울 수 있다.  
또한 개발자가 `Pod` 를 생성할 때 얼만큼의 용량을 설정해 `PVC` 를 만들지 모름으로 미리 `PV` 를 생성해 놓기도 애매하다.  

`StorageClass` 는 관리자의 개입없이 동적으로 `PV` 생성을 지원한다.  

아래는 `AWS EBS` 를 통해 `StorageClass` 를 사용하는 예.  

![1](/assets/k8s/k8s_volume3.png)  

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: standard
provisioner: kubernetes.io/aws-ebs
volumeBindingMode: Immediate
reclaimPolicy: Retain
allowVolumeExpansion: true
parameters:
  type: gp2
mountOptions:
  - debug
```

- **provisioner**  
  스토리지 제공자, `aws-ebs` 외에도 여러 상용 클러스터에서 `provisioner` 를 지원함  
- **volumeBindingMode**  
  - `Immediate`: PVC 생성시 바인딩  
  - `WaitForFirstConsumer`: PVC 와 이를 사용하는 Pod 생성시 바인딩  
- **parameters**
  `provisioner` 종류별로 상이함  

> `k8s v1.23` 부터 `EBS, EFS` 로 `StorageClass` 를 생성하려면 별도의 `CSI 드라이버` 설치가 필요하다.  
> <https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/ebs-csi.html>  
> <https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/efs-csi.html>  

### rancher

베어메탈 클러스터를 구성했다면 로컬 스토리지를 `provisioner` 로 사용하기 위한 별도 설정을 지정해줘야 한다.  

이때  로컬 스토리지 `provisioner` 로 `rancher` 를 많이 사용한다.  

> <https://github.com/rancher/local-path-provisioner>  

```sh
curl https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml -o local-storage.yaml

kubectl apply -f local-storage.yaml

kubectl get sc     
# NAME         PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
# local-path   rancher.io/local-path   Delete          WaitForFirstConsumer   false                  24s
```

`local-path` 라는 이름으로 `StorageClass` 가 생성됨을 볼 수 있다.  

데모로 아래 `PVC` 와 `Deployment` 를 생성  

```yaml
# test-rancher.yaml
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: pvc-hostpath
spec:
  accessModes:
  - ReadWriteOnce
  volumeMode: Filesystem
  storageClassName: local-path
  resources:
    requests:
      storage: 1Gi
---
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

```sh
kubectl apply -f test-rancher.yaml
kubectl port-forward deployment/kubernetes-simple-app 8080:8080
```

`/opt/local-path-provisioner` 위치(혹은 개인설정한 위치)로 이동해 PV 가 생성되고 그 안에 로그파일이 있는지 확인.  

기본적으로 설정되는 `/opt/local-path-provisioner` 경로를 변경하고 싶다면 `nodePathMap` 에 원하는 경로로 변경

```yaml
data:
  config.json: |-
    {
      "nodePathMap":[
        {
          "node":"DEFAULT_PATH_FOR_NON_LISTED_NODES",
          "paths":["/opt/local-path-provisioner"]
        }
      ]
    }
```
