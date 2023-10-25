---
title:  "k8s - kubeadm, Harbor, Jenkins, argoCD!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - kubernetes

---

## 개요  

이번 글에선 `ubuntu 22.04` PC에 `kubeadm` 기반의 클러스터 환경을 구성하고  
`Harbor, Jenkins, argoCD` 를 구성하는 방법을 진행한다.  

### host file 참고

`nginx ingress` 를 통해 `Horbor, Jenkins, argoCD` 에 접근할 것임으로  
`로컬 PC` `hosts` 파일에 `노드 IP` 와 `ingress url` 을 매핑  

```
192.168.10.228  core.harbor.domain jenkins.cluster.local argocd.example.com
```

## kubadm

`k8s v.1.24` 이후부터 컨테이너 런타임인 `dockershim` 지원을 종료하면서  
`cri-docker` 를 추가적으로 구성해야 한다.  

설치는 아래 URL 참고  

> <https://tech.hostway.co.kr/2022/08/30/1374/>
> `k8s v1.24` 이후부턴 `containerd` 등의 컨테이너 환경 사용을 권장한다.  


`kubadm v.1.28 + cri-docker` 가 설치 완료되었다면 아래와 같이 kubeadm 클러스터를 구성한다.  

```shell
# 스왑 비활성화 
sudo swapoff -a 

# 의존성 리스트 설지
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add 
sudo apt-add-repository "deb http://apt.kubernetes.io/ kubernetes-xenial main"

# sudo apt-get install -y kubeadm=1.28.x-00 버전지정 가능
sudo apt-get install kubeadm kubelet kubectl
sudo apt-mark hold kubeadm kubelet kubectl

# 버전확인
kubeadm version 
# kubeadm version: &version.Info{Major:"1", Minor:"28", GitVersion:"v1.28.2 ...

# Controller Node
sudo kubeadm init --ignore-preflight-errors=all \
  --pod-network-cidr=192.168.0.0/16 \
  --apiserver-advertise-address=192.168.1.228 \
  --cri-socket /var/run/cri-dockerd.sock

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

단순 개발환경 구성을 위해 `control-plane` 과 `data-plane` PC 를 나누지 않는다면  
아래와 같이 `control-plane` 에서 `Pod` 를 실행할 수 있도록 설정 가능하다.  

```shell
# taint all node
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### calico install

`CNI(Container Network Interface)` 로 `flannel, calico` 등 여러가지가 있지만  
가장 많이 사용하는 `calico` 를 설치  

```shell
kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml

watch kubectl get pods -n kube-system
NAME                                       READY   STATUS    
calico-kube-controllers-7ddc4f45bc-v5rpc   1/1     Running   
calico-node-mbwnt                          1/1     Running   
coredns-cfbfd9cb6-nhf5s                    1/1     Running   
coredns-cfbfd9cb6-p7h5p                    1/1     Running   
...
```

### ingress controller

`ingress` 를 사용하기위해 `ingress controller` 를 설치  


> <https://github.com/kubernetes/ingress-nginx>
> `deploy/static/provider/baremetal/deploy.yaml`
> 위치는 가끔 업데이트 되지만 baremetal 디렉토리를 찾으면 된다.  


```shell
curl https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/baremetal/deploy.yaml -o ingress-deploy.yaml
kubectl apply -f ingress-deploy.yaml
```

`random nodePort` 를 고정하기 위해 아래와 같이 수정  

```yaml
apiVersion: v1
kind: Service
metadata:
  ...
  name: ingress-nginx-controller
  namespace: ingress-nginx
spec:
  ipFamilies:
  - IPv4
  ipFamilyPolicy: SingleStack
  ports:
  - appProtocol: http
    name: http
    port: 80
    nodePort: 30080 # nodePort 고정
    protocol: TCP
    targetPort: http
  - appProtocol: https
    name: https
    port: 443
    nodePort: 30443 # nodePort 고정
    protocol: TCP
    targetPort: https
  ...
```

### rancher storageClass

`로컬 스토리지` `provisioner` `rancher` 설치  

> <https://github.com/rancher/local-path-provisioner>  

```shell
# stable 버전으로 설치
curl https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml -o local-storage.yaml

kubectl apply -f local-storage.yaml
kubectl get storageclass
NAME         PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   
local-path   rancher.io/local-path   Delete          WaitForFirstConsumer   false                  
```

## Harbor

cloud native repository for Kubernetes 라고 소개하고 있다.  
CNCF 졸업 프로젝트인만큼 많은 서비스사에서 운용중임.  

> <https://goharbor.io/>  
> <https://engineering.linecorp.com/ko/blog/harbor-for-private-docker-registry>

Private Docker Registry 의 경우 사내 어디에서든지 접근가능한 URL 이 필요함으로 local k8s cluster 내부에 정의하는 것 보단 외부 별도의 환경에서 운영하는 것을 권장  

Helm 으로 설치 진행  

```shell
helm repo add harbor https://helm.goharbor.io

# 압축파일 다운로드, harbor-1.13.0.tgz 버전 설치됨
helm fetch harbor/harbor

# 압축 파일 해제
tar zxvf harbor-*.tgz
mv harbor harbor-helm
```

`harbor` 의 서비스에서 `PVC` 사용을 요구함으로 `rancher` 로 설치했던 `storageClass` 지정.  
`values.yaml` 에서 `local-path` `storageClass` 를 지정  

`storageClass` 에 모두 `local-path` 로 지정한다.  

```yaml
# values.yaml
persistence:
  enabled: true
  resourcePolicy: "keep"
  persistentVolumeClaim:
    registry:
      existingClaim: ""
      storageClass: "local-path"
      ...
```

`nginx ingress controller` 환경에서 돌아가기 때문에 외부접근을 허용하기 위해 아래 어노테이션 추가

```yaml
# values.yaml
  ingress:
    hosts:
      core: core.harbor.domain
    ...
    annotations:
      # note different ingress controllers may require a different ssl-redirect annotation
      # for Envoy, use ingress.kubernetes.io/force-ssl-redirect: "true" and remove the nginx lines below
      kubernetes.io/ingress.class: "nginx"
externalURL: https://core.harbor.domain:30443
```

`harbor ingress` 에서 사용할 `tls` 10년짜리 인증서를 생성해서 사용  

```sh
openssl genrsa -out ca.key 4096
openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=KR/ST=Seoul/L=Seoul/O=hello/OU=kouzie/CN=core.harbor.domain" \
 -key ca.key \
 -out ca.crt

TLS_CRT=$(cat ca.crt | base64) \
TLS_KEY=$(cat ca.key | base64) \
envsubst < harbor-ca-secret.yaml | \
kubectl apply -f -
```

```yaml
# values.yaml
expose:
  type: ingress
  tls:
    enabled: true
    certSource: secret
    secret:
      secretName: "harbor-ca" # 위에서 생서한 secret 이름으로 설정
...
caSecretName: "harbor-ca" # 위에서 생서한 secret 이름으로 설정
```

```shell
# namespace 생성
kubectl create ns harbor
helm install harbor -f values.yaml . -n harbor

watch kubectl get pods -n harbor
```

### k8s cluster 에 harbor dns 등록  

`로컬 PC` 를 포함해서 `k8s 노드` 에서도 설치된 `Harbor` 의 `registry` 를 찾아갈 수 있어야 한다.  

> `k8s 노드` 에서 `Pods` 를 실행시키기 위한 이미지를 `Harbor` 로부터 가져옴으로  

`hosts` 파일에 `core.harbor.domain` 도메인을 등록한다.  

```conf
# /etc/hosts
192.168.10.228 core.harbor.domain
```

> 실제 해당 `k8s 노드` 에 `Harbor` 가 설치되어있지 않더라도 `Ingress Controller` 가 알아서 라우팅 해줄것이다.  

또한 `k8s cluster` 상에 배포된 어플리케이션이 `Harbor` 에 접근해야 할 경우,  
예를 들어 `Jenkins` 와 같은 어플리케이션이 `CI/CD` 를 위해 `Harbor` 에 접근해야할 경우,  
내부에서도 `Harbor` 를 찾아갈 수 있어야 한다.  

`core.harbor.domain` 도메인을 `k8s corDNS` 에 설정.  

```sh
kubectl edit configmap coredns -n kube-system
```

```yaml
apiVersion: v1
data:
  Corefile: |
    .:53 {
        errors
        health {
           lameduck 5s
        }
        ready
        kubernetes cluster.local in-addr.arpa ip6.arpa {
           pods insecure
           fallthrough in-addr.arpa ip6.arpa
           ttl 30
        }
        prometheus :9153
        forward . /etc/resolv.conf {
           max_concurrent 1000
        }
        hosts {
          192.168.10.228    core.harbor.domain
          fallthrough
        }
        cache 30
        loop
        reload
        loadbalance
    }
kind: ConfigMap
...
```

### image push & pull

도커 이미지를 `Harbor` 에 `push, pull` 하기 전에 몇가지 알아야할 점, 설정해야할 점이 있다.  

현재 `Harbor` 에 접근할 때 위에서 설정한 `unknown 서명된 인증서` 가 적용된 `Ingress` 를 통해 `Harbor` 에 접근한다.  

안타깝게도 `Docker` 클라이언트에서 `https` 프로토콜을 사용해 `registry` 에 접근할 때 `known 서명된 인증서` 만 허용하기 때문에,  
`insecure-registries` 속성을 통해 서명여부 상관없이 `registry` 를 사용할 수 있도록 설정해야 한다.  

중요한건 **`k8s 노드` 에서도 해당 설정을 해야**, `k8s node` 에서 `imagePullbackoff` 같은 에러가 발생하지 않는다.  

```json
// ubuntu: /etc/docker/daemon.json
// mac: ~/.docker/daemon.json
// 설정후 docker 데몬 재시작 필요
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "insecure-registries": [
    "https://core.harbor.domain:30443"
  ]
}
```

설정 완료 후 login 및 이미지 push 가 잘 되는지 확인  

```sh
docker login -u admin -p Harbor12345 core.harbor.domain:30443

docker build -t hello:demo . 
docker tag hello:demo core.harbor.domain:30443/library/hello:demo
docker push core.harbor.domain:30443/library/hello:demo
```

## jenkins

> <https://github.com/jenkinsci/helm-charts>

`Helm` 차트를 사용하면 `Cloud Native` 하게 동작하는 `Jenkins` 설치가 가능하다.  

```sh
helm repo add jenkins https://charts.jenkins.io

# 압축파일 다운로드, jenkins-4.7.2.tgz 버전 설치됨
helm fetch jenkins/jenkins

# 압축 파일 해제
tar zxvf jenkins-*.tgz
mv jenkins jenkins-helm
```

`Ingress, StoreClass` 에 대한 설정 진행  

```yaml
  ingress:
    enabled: true
    paths: []
    apiVersion: "networking.k8s.io/v1"
    labels: {}
    annotations: {}
    ingressClassName: nginx
    hostName: jenkins.cluster.local
    tls:
    # - secretName: jenkins.cluster.local
    #   hosts:
    #     - jenkins.cluster.local

persistence:
  enabled: true
  existingClaim:
  storageClass: "local-path"

jenkinsUrl: "https://jenkins.cluster.local:30443"
```

```shell
cd jenkins-helm
# namespace 생성
kubectl create ns jenkins
helm install jenkins -f values.yaml . -n jenkins
# 비밀번호 확인
kubectl exec --namespace jenkins -it svc/jenkins -c jenkins -- /bin/cat /run/secrets/additional/chart-admin-password && echo

watch kubectl get pods -n jenkins
```

### jenkins git SSL 무시

> <https://stackoverflow.com/questions/41930608/jenkins-git-integration-how-to-disable-ssl-certificate-validation>

사내 `git` 서버를 사용중이고 `unknown 서명된 인증서` 를 사용중이라면 아래와 같은 오류문구가 뜰 수 있다.  

```
SSL certificate problem: self signed certificate in certificate chain
```

`k8s helm` 으로 설치한 `jenkins` 는 `master/agent` 가 나뉘어서 동작하는 구조이기 때문에  
`master, agent` 모두 `git ssl` 을 무시하는 환경변수 설정을 해줘야 한다.  

```yaml
# master ssl disable
controller:
  ...
  initContainerEnv:
    - name: "GIT_SSL_NO_VERIFY"
      value: "true"
  containerEnv:
    - name: "GIT_SSL_NO_VERIFY"
      value: "true"

...

# agent ssl disable
agent:
  enabled: true
  ...
  # Pod-wide environment, these vars are visible to any container in the agent pod
  envVars: 
  - name: "GIT_SSL_NO_VERIFY"
    value: "true"
```

만약 `PodTemplate` 과 같은 `jenkins pipeline` 문법을 사용할 경우 `values.yaml` 에서 설정한 환경변수가 `agent` 에서 동작하지 않기 때문에,  
아래와 같이 `Jenkinsfile` 에서 직접 환경변수 지정하는것을 권장  

```groovy
podTemplate(
  yaml: '''
kind: Pod
  ...
''',
  envVars: [envVar(key: 'GIT_SSL_NO_VERIFY', value: 'false')],
) {
  node(POD_LABEL) {
    ...
  }
}
```

### jenkins docker login secret  

`jenkins agent` 에서 `Harbor registry` 에 이미지를 `push` 하려면 로그인할 수 있는 인증정보가 필요하다.  
`kubernetes.io/dockerconfigjson` 타입의 시크릿을 사용한다.

`Harbor registry` 에 접근하기 위한 `login secret` 이다.  

```yaml
# harber-jenkins-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: regcred
  namespace: jenkins
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: $HARBOR_DOCKER_CONFIG_JSON
```

```sh
HARBOR_DOCKER_AUTH=$(echo -n 'admin:Harbor12345' | base64) \
HARBOR_DOCKER_CONFIG_JSON=$(echo -n '{"auths": {"core.harbor.domain:30443": {"auth": "'$HARBOR_DOCKER_AUTH'"}}}' | base64) \
envsubst < harber-jenkins-secret.yaml | \
kubectl apply -f -
```

## argoCd

작성중...

```shell
kubectl create namespace argocd

helm repo add argo https://argoproj.github.io/argo-helm

# 압축파일 다운로드, argo-cd-5.46.8.tgz 다운도르됨
helm fetch argo/argo-cd

# 압축 파일 해제
tar zxvf argo-cd-*.tgz
mv argo-cd argo-cd-helm
```

마찬가지로 ingress 를 통해 접근함으로 아래처럼 `value.yaml` 수정  

```yaml
server:
  ...
  ingress:
    # -- Enable an ingress resource for the Argo CD server
    enabled: true
    # -- Additional ingress annotations
    annotations: {
      kubernetes.io/ingress.class: nginx
    }
    ```
    
    ...
    # https redirect 방지
    server.insecure: true
...
```

```shell
cd argo-cd-helm
# namespace 생성
kubectl create ns argocd
helm install argocd -f values.yaml . -n argocd

# 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

watch kubectl get pods -n jenkins
```