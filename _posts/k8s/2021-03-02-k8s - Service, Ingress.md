---
title:  "k8s - Service, Ingress!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---

## Service

> <https://kubernetes.io/ko/docs/concepts/services-networking/service/>
> 출처: <https://medium.com/google-cloud/kubernetes-nodeport-vs-loadbalancer-vs-ingress-when-should-i-use-what-922f010849e0>

파드는 동일한 공간에서 항상 생성되지 않기 때문에 접근할 수 있도록 도와주는 게이트웨이가 필요하다.  

클러스터 안에서 실행된 파드에 대해 엑세스 할 때 `Service` 를 정의한다.  

> 러스터 내부 파드간에는 노드상의 Proxy 데몬이 송수신 처리를 해줌  

노드 컴포넌트인 `kube-proxy` 를 사용해 `Service` 구현의 근간이다.  
userspace, iptables, IPVS 등을 사용해 패킷 라우팅을 지원한다.  

> IPVS (IP Virtual Server) 는 리눅스 L4 로드밸런싱 기능,  

### Service Type

서비스타입에는 크게 4가지가 존재한다.  

**ClusterIP**
별도의 지정이 없을경우 설정되는 기본 서비스 타입, 
쿠버네티스 클러스터 내부에서 내부 서비스끼리만 사용하는 방식이다.  

**NodePort**
클러스터 외부에서 내부로 접근할 수 있는 가장 쉬운 방법, 
서비스 하나에 모든 노드의 포트를 지정하고 접근하는 방식이기에 클러스터에서 중복될 순 없다.    

**LoadBalancer(ExternalIp)**
공인 IP 가 설정된 로드밸런서 장비가 있어야 한다.  
AWS, GCP 등의 퍼블릭 클라우드 서비스에서 지원한다.  

**ExternalName**
`spec.externalName` 필드에 설정한 값과 연결, 
클러스터내부에서 외부로 접근할 때 주로 사용

원활한 테스트를 위해 클러스터 내부에서 사용하는 nginx 어플리케이션을 생성.

```
## app 생성 label > nginx-for-svc 
$ kubectl run nginx-for-service --image=nginx --replicas=2 --port=80 --labels="app=nginx-for-svc" 

## 클러스터안 app 의 ip 확인
$ kubectl get pods -o wide
NAME                READY   STATUS    RESTARTS   AGE   IP          NODE
nginx-for-service   1/1     Running   0          90s   10.1.0.89   docker-desktop
```

### 매니페스트

`spec.type`: 서비스 타입 설정, 기본타입은 `ClusterIP`.  
`spec.clusterIP`: 모든 종류의 서비스는 `ExternalName` 을 제외한 모든 서비스는 클러스터 내부에 서비스를  갖으며 내부에서 사용할 IP를 직접 설정할 수 있다. 설정하지 않으면 자동으로 IP값이 할당된다.  
`spec.selector`: 서비스와 연결할 labels 필드.  
`spec.ports[]`: 서비스에서 한꺼번에 포트 여러 개를 외부에 제공할 때는 `spec.ports[]` 하위에 필드 값을 설정하면 됩니다.

### ClusterIP

```yaml
## clusterip.yaml
apiVersion: v1
kind: Service
metadata:
  name: clusterip-service
spec:
  type: ClusterIP
  selector:
    app: nginx-for-svc
  ports:
  - protocol: TCP
    port: 80 ## 서비스가 사용할 포트
    targetPort: 80 ## 파드 포트
```

서비스 생성 후 같은 클러스터 내부 테스트용 app 생성 및 접속, 같은 클러스터 안 nginx app 에 접근 테스트  

```
$ kubectl apply -f clusterip.yaml

$ kubectl run -it --image nicolaka/netshoot testnet bash
bash-5.1## curl 10.1.0.89
<!DOCTYPE html> ..c.. </html>
```

### NodePort

```yaml
## nodeport.yaml
apiVersion: v1
kind: Service
metadata:
  name: nodeport-service
spec:
  type: NodePort
  selector:
    app: nginx-for-svc
  ports:
  - protocol: TCP
    port: 80 ## cluster내부 서비스에서 사용할 포트
    targetPort: 80 ## 파드의 포트
    nodePort: 30000 ## 노드외부 노출할 포트
```

```
## 서비스 생성
$ kubectl apply -f nodeport.yaml

$ kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
nodeport-service   NodePort    10.102.110.207   <none>        80:30000/TCP   94s

## curl localhost:30000
<!DOCTYPE html> ..c.. </html>
```

`NodePort` 타입의 서비스이지만 `CLUSTER-IP`도 존재한다.  
노드외부에서 접근할 수 있도록 `30000` 포트를 노출한다.  

![kucbe8](/assets/k8s/kube8.png)  

### LoadBalancer

```yaml
## loadbalancer.yaml 
apiVersion: v1
kind: Service
metadata:
  name: loadbalancer-service
spec:
  type: LoadBalancer
  selector:
    app: nginx-for-svc
  ports:
  - protocol: TCP
    port: 30080 ## loadbalancer 접근 포트
    targetPort: 80 ## 파드 접근 포트
```

```
$ kubectl apply -f loadbalancer.yaml

$ kubectl get svc
NAME                   TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)        AGE
loadbalancer-service   LoadBalancer   10.102.127.3   localhost     30080:31652/TCP   90s
```

마찬가지로 `CLUSTER-IP` 가 존재함으로 `31652` 포트의 클러스터 내부 서비스가 별도로 생긴다.  


![kucbe9](/assets/k8s/kube9.png)  

30080->31652->80

### ExternalName

내부에서 외부로 가기위한 서비스이다.  

```yaml
## externalname.yaml
apiVersion: v1
kind: Service
metadata:
  name: externalname-service
spec:
  type: ExternalName
  externalName: google.com
```

```
$ kubectl apply -f externalname.yaml

$ kubectl get svc                   
NAME                   TYPE           CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
externalname-service   ExternalName   <none>       google.com    <none>    30s

$ kubectl run -it --image nicolaka/netshoot testnet bash
bash-5.1## curl externalname-service.default.svc.cluster.local
```

## Ingress

> Ingress: 입장권  
> <https://kubernetes.io/ko/docs/concepts/services-networking/ingress/>

클러스터 안의 파드에 대해 외부에서 엑세스 하기 위한 리소스가 `Ingress` 이다.
클러스터 외부에서 내부로 접근하는 요청 처리를 정의해둔 규칙이라 할 수 있다.  

`Ingress`는 L7이고 `Service` 는 L4이다.

`Service`와 `Ingress` 모두 클러스터 외부로 포트를 노출, 로드밸런싱 기능을 포함한다.  

하나의 외부 IP와 포트로 여러개의 서비스를 로드밸런싱할 수 있다.  

`Ingress` 에서는 HTTP 프로토콜의 URI, HOST명 등을 통해서도 노출할 어플리케이션 분기가 가능하다.  

![kucbe10](/assets/k8s/kube10.png)  


### Ingress 컨트롤러  

L7 로드벨런서 역할을 해줄 소프트웨어는 클라우스 서비스 에서 제공하는 자체 컨트롤러와 서드파티 솔루션이 있는데  
서드파티 솔루션중 가장 유명한 `ingress-nginx` 를 사용한다.  

```
$ git clone https://github.com/kubernetes/ingress-nginx.git

$ cd ingress-nginx/deploy/baremetal
$ kubectl apply -k .

$ kubectl get deploy,svc -n ingress-nginx
NAME                                       READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/nginx-ingress-controller   1/1     1            1           2m38s

NAME                    TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
service/ingress-nginx   NodePort   10.100.191.108   <none>        80:31026/TCP,443:31976/TCP   2m38s
```

생성된 `Service`, `Deployment` 확인, `NodePort` `31026` 포트로 생성되었다.  

### Ingress 정의

먼저 `Ingress` 를 통해 접근할 앱을 `Deployment` 로 정의  

```yaml
## deployment-nginx.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx-deployment
  ## annotations:
    ## kubernetes.io/change-cause: version 1.10.1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx-deployment
  template:
    metadata:
      labels:
        app: nginx-deployment
    spec:
      containers:
      - name: nginx-deployment
        image: nginx
        ports:
        - containerPort: 80
```


그리고 위에서 설정한 `ingress-nginx` 컨트롤러를 사용해 실제 요청을 분기해줄 `Ingress` 를 만든다.  

```yaml
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: test
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: / ## 모든 request 를 / uri 로 변경
spec:
  rules:
  - host: foo.bar.com ## host 명을 이용해 룰 설정
    http:
      paths:
      - path: /foos1 ## /foos1 의 요청은 s1 인도
        backend:
          serviceName: s1
          servicePort: 80
      - path: /bars2 ## /bars2 의 요청은 s2 인도
        backend:
          serviceName: s2
          servicePort: 80
  - host: bar.foo.com
    http:
      paths:
      - backend:
          serviceName: s2
          servicePort: 80
```

`host`, `path` 등을 사용해 요청을 분기시킬 수 있다.  

```
$ kubectl apply -f deployment-nginx.yaml 
$ kubectl expose deploy nginx-deployment --name s1

$ kubectl describe ingress test
Warning: extensions/v1beta1 Ingress is deprecated in v1.14+, unavailable in v1.22+; use networking.k8s.io/v1 Ingress
Name:             test
Namespace:        default
Address:          localhost
Default backend:  default-http-backend:80 (<error: endpoints "default-http-backend" not found>)
Rules:
  Host         Path  Backends
  ----         ----  --------
  foo.bar.com  
               /foos1   s1:80 (10.1.0.94:80,10.1.0.95:80,10.1.0.96:80)
               /bars2   s2:80 (10.1.0.94:80,10.1.0.95:80,10.1.0.96:80)
  bar.foo.com  
                  s2:80 (10.1.0.94:80,10.1.0.95:80,10.1.0.96:80)
Annotations:   nginx.ingress.kubernetes.io/rewrite-target: /
Events:
  Type    Reason  Age   From                      Message
  ----    ------  ----  ----                      -------
  Normal  CREATE  19m   nginx-ingress-controller  Ingress default/test
  Normal  UPDATE  18m   nginx-ingress-controller  Ingress default/test
```


## k8s 네트워킹

k8s에선 컨테이너간 통신, kuberctl 을통한 명령어 실행을 해야하는데 k8s 네트워킹을 알아보자.  

### kubnet

`kubnet` 은 k8s 표준 네트워크 플러그인이다.  
노드간 연결은 `10.100.0.0/16` 네트워크로 연결된다.  
위에선 각각의 노드에 `10.1.1.0/24`, `10.1.1.1/24`를 할당했다.  

![kube5](/assets/k8s/kube5.png)  

파드 네트워크는 클러스터 전체에서 검사 후 결정된다.  
그리고 중복되지 않는 파드 `CIDR`을 할당한다.  
파드 통신지원을 위해 특정 포드에 접근할 수 있는 노드의 인터페이스를 사용자 정의 루트에 기록한다.  

k8s 내부에서 자동으로 이런 네트워크 구조와 라우팅 테이블을 유지하는 것을 기억해두자.  

> 사이더(`Classless Inter-Domain Routing`): `CIDR`는 클래스 없는 도메인 간 라우팅 기법으로 1993년 도입되기 시작한, 최신의 IP 주소 할당 방법이다. https://ko.wikipedia.org/wiki/사이더_(네트워킹)

### 리눅스 namespace  

> `Namespace`: 한덩어리의 데이터에 이름을 붙혀 충돌 가능성을 줄이고, 쉽게 참조할 수 있게하는 개념

`Linux` 커널의 `namespace` 기능은 Linux의 오브젝트에 이름을 붙임으로써 다음과 같은 6개의 독립된 환경을 구축할 수 있다.

1. PID namespace  
  프로세스에 할당된 고유한 ID를 말하며 이를 통해 프로세스를 격리할 수 있다
  namespace가 다른 프로세스 끼리는 서로 액세스할 수 없다

2. Network namespace  
  네트워크 디바이스, IP 주소, Port 번호, 라우팅 테이블, 필터링테이블 등의 네트워크 리소스를 namespace마다 격리시켜  
  독립적으로 가질 수 있다. 이 기능을 이용하면 OS 상에서 사용중인 Port가 있더라도 컨테이너 안에서 동일한 Port를 사용 가능하다.

3. UID namespace  
  UID, GID를 namespace 별로 독립적으로 가질 수 있도록 한다.  
  namespace 안과 호스트 OS 상에서 서로 다른 UID, GID를 가질 수 있다.  

4. Mount namespace  
  호스트 OS와 namespace가 서로 다른 격리된 파일시스템 트리를 가질 수 있도록 한다  
  마운트는 컴퓨터에 연결된 기기나 기억장치를 OS에 인식시켜 사용가능한 상태로 만드는 것을 의미한다

5. UTS namespace  
   namespace 별로 호스트명이나 도메인 명을 독자적으로 가질 수 있다  

6. IPC namespace  
  프로세스간 통신(IPC) 오브젝트를 namespace 별로 독립적으로 가질 수 있다

### 파드 네트워크 구조

도커 네트워킹에서 베스(veth, virtual ethernet) 라는 가상 장치 를사용해서 IP 를 부여하는데 k8s에선 파드 단위로 컨테이너들을 관리한다.  

좌측이 도커 네트워크 구조, 우측이 파드 네트워킹 구조이다.  

![kucbe11](/assets/k8s/kube11.png)  

파드 네트워킹에서 파드당 하나의 `virtual ethernet` 생성되고 파드에 속한 컨테이너들은 `virtual ethernet` 하나를 공유한다 컨테이너들은 이 하나의 IP 를 공유한다.  

> <https://medium.com/google-cloud/understanding-kubernetes-networking-pods-7117dd28727>

같은 파드의 컨테이너끼리는 `localhost` 를 통해 통신 가능하다.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: podnet-01
spec:
  containers:
   - name: web
     image: nginx
   - name: ubuntu
     image: ubuntu:16.04
     command: ["/bin/sh", "-c", "while : ;do curl http://localhost/; sleep 10; done"]
```

생성된 ubuntu 컨테이너가 `localhsot` 요청도 문제없이 수행된다.  

### 서비스 네트워크

k8s 에서 일반적으로 서비스를 통해 컨테이너간 통신한다.  
서비스가 생성되면 각 서비스 IP와 파드 네트워크를 매핑한 `NAT` 테이블이 생성되고

![kucbe13](/assets/k8s/kube13.png)  

> <https://arisu1000.tistory.com/27851?category=787056>

각 노드 NAT 에 별개의 대역을 가진 **서비스 전용 IP** 가 추가되며 이 IP 로 접근시 파드로의 접근이 진행된다.  
노드 컴포넌트인 `kube-proxy` 가 해당 역할을 수행하며 `NAT` 테이블을 만든다.  

> `kube-proxy` 가 직접 패킷을 받는 서버는 아니고 `iptables` 만 수정하는 컴포넌트이다.  
> 실제 패킷은 넷필터(netfilter) 가 처리한다.  

`NodePort` 로 만들시 각 클러스터의 `NAT` 에만 추가되는 것이 아닌 게이트웨이에도 라우팅 테이블이 추가된다.  

### CNI(Container Network Interface)  

클러스터는 여러개의 노드와 여러개의 파드로 구성된다.  

k8s에서는 파드 각각이 모두 고유의 IP를 갖도록 구성하기에 `CNI(Container Network Interface)`라는 플러그인을 사용한다.  

![kucbe12](/assets/k8s/kube12.png)  
> 출처: <https://github.com/dybooksIT/kubernetes-book/blob/master/readme/errata/errata.md>

`CNI`가 각 파드마다 별도의 IP대역을 설정하고 기록하고 공유하고 아래와 같은 기능들을 수행한다.  

- 커널 라우팅  
- 동적 라우팅  
- 파드 인터페이스 생성 및 IP, Subnet, Routing Table 설정  
- Proxy ARP 기능  

`CNI` 플러그인의 구현체로 *플라넬, 칼리코, 실리엄* 이 있으며 네트워크 플러그인에 따라서 호스트 네트워크를 구성하는 방법과 특성이 다르다.  

## k8s DNS

파드간 통신에서 클러스터 안에서만 사용하는 DNS를 설정해서 사용한다.  

```yaml
apiVersion: v1
kind: Service
metadata:
  namespace: monitoring
  labels:
    app: prometheus-app
  name: prometheus-app
spec:
  type: NodePort
  ports:
    - nodePort: 30990
      port: 9090
      targetPort: 9090
  selector:
    app: prometheus-app
```

만약 위와같은 서비스 정의시에 도메인은 아래와 같다.  

`prometheus-app.monitoring.svc.cluster.local`  
`서비스이름.네임스페이스이름.svc.cluster.local`  

파드에 접근할 수 있는 도메인 생성이 가능하다.  

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
      hostname: appname
      subdomain: default-subdomain
      dnsPolicy: ClusterFirst
      containers:
      - name: kubernetes-simple-app
        image: arisu1000/simple-container-app:latest
        ports:
        - containerPort: 8080
```

spec에 도메인 관련 설정시 접근가능한 도메인은 아래와 같다.  
`appname.default-subdomain.default.svc.cluster.local`  
`호스트네임이름.서브도메인이름.네임스페이스이름.svc.cluster.local`  

### coreDNS

DNS 질의를 처리해주는 `coreDNS` 파드가 존재한다.  

```
kubectl get pod -n kube-system
NAME                                     READY   STATUS    RESTARTS         AGE
coredns-6d4b75cb6d-jvhfg                 1/1     Running   3 (103s ago)     16d
coredns-6d4b75cb6d-qdbbr                 1/1     Running   3 (103s ago)     16d
...

kubectl get svc -n kube-system
NAME       TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                  AGE
kube-dns   ClusterIP   10.96.0.10   <none>        53/UDP,53/TCP,9153/TCP   16d
```

> 과거엔 `kube-dns` 파드를 사용했었으나 현재는 `coreDNS` 를 주로 사용, 서비스의 이름만 `kube-dns` 로 유지됨  

`coreDNS` 는 `Corefile` 설정파일을 통해 운영되며 컨피그맵으로 `Corefile` 을 설정할 수 있다.  
기본으로 설정된 `Corefile` 컨피그맵은 아래와 같다.  

```
kubectl describe configmap coredns -n kube-system

Name:         coredns
Namespace:    kube-system
Labels:       <none>
Annotations:  <none>

Data
====
Corefile: # 설정을 위한 파일
----
.:53 { # DNS 를 위한 각종 플러그인
    # stdout 으로 에러로그 출력
    errors 
    health { # 헬스체크
       lameduck 5s
    }
    ready
    kubernetes cluster.local in-addr.arpa ip6.arpa {
       # 모든 DNS 쿼리 응답, pods verified 변경시 같은 네임스페이스 DNS 쿼리만 응답
       pods insecure 
       # DNS 찾기 실패시 동작, IP로 도메인을 찾는 리버스 쿼리 진행(IPv6 도 지원)
       fallthrough in-addr.arpa ip6.arpa
       ttl 30
    }
    # http://localhost:9153/metrics 주소로 프로메테우스 형식의 메트릭 정보 제공
    prometheus :9153
    # 외부형식 DNS 쿼리를 /etc/resolv.conf 에 설정된 외부 DNS 서버로 보내서 처리
    forward . /etc/resolv.conf {
       max_concurrent 1000
    }
    cache 30 # DNS 캐시 유지 초
    loop # 순환참조 발견시 coreDNS 프로세스 중지
    reload # CoreFile 실시간 감시 및 재시작(2min)
    loadbalance # 레코드를 라운드로빈 밸런스로 DNS 쿼리 응답
}

Events:  <none>
```

### 파드 DNS 설정

```yaml
apiVersion: v1
kind: Pod
metadata:
  namespace: default
  name: dns-test
spec:
  containers:
  - name: dns-test
    image: arisu1000/simple-container-app:latest
  dnsPolicy: ClusterFirst
  dnsConfig:
    nameservers:
    - 8.8.8.8
    searches:
    - example.com
    options:
    - name: name01
      value: value01
    - name: name02
```

`.spec.dnsPolicy` 설정을 통해 k8s 에서 DNS 질의 우선순위 조절이 가능하다.  

1. `Default` - 노드의 DNS 설정에 따름  
2. `ClusterFirst` - 클러스터 외부 DNS 서버에 질의  
3. `ClusterFirstWithHostNet` - 파드를 호스트 모드로 사용하겠다고 설정  
4. `None` - k8s 클러스터 안 DNS 설정을 무시, 별도의 DNS 서버 설정 필요  

`.spec.dnsPolicy` 설정을 통해 `/etc/resolv.conf` 에 들어갈 속성을 지정할 수 있다.  

3가지 속성을 지정할 수 있다.  `nameservers`, `searches`, `options`

```
kubectl exec dns-test cat /etc/resolv.conf

nameserver 10.96.0.10
nameserver 8.8.8.8
search default.svc.cluster.local svc.cluster.local cluster.local example.com
options name01:value01 name02 ndots:5
```

단순 서비스의 이름만 지정해도 자동으로 `서비스이름.네임스페이스이름.svc.cluster.local` 으로 설정해서 찾기 때문에 정확한 위치를 찾을 수 있는것.  
