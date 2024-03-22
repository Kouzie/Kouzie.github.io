---
title:  "k8s - kubeadm, harbor, jenkins, argocd!"

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

`ubuntu 22.04` 단일 PC에 `kubeadm` 기반의 클러스터 환경을 구성,  

k8s 운영에 필요한 기본적인 오픈소스 설치방법에 대해 설명.  

- Harbor
- Jenkins
- argoCd

`nginx ingress` 를 통해 `Horbor, Jenkins, argoCD` 에 접근할 것임으로  
클라이언트 `hosts` 파일에 `ingress url` 과 k8s 클러스터 IP 를 매핑  

```conf
# /etc/hosts
192.168.10.XXX  core.harbor.domain jenkins.cluster.local argocd.example.com
```

### 데모코드  

> <https://github.com/Kouzie/local-k8s>

## kubadm

`k8s v.1.24` 이후부터 컨테이너 런타임인 `dockershim` 지원을 종료하면서 `cri-docker` 를 추가적으로 구성해야 한다.  

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
  --pod-network-cidr=10.244.0.0/16 \
  --apiserver-advertise-address=192.168.10.XXX \
  --cri-socket /var/run/cri-dockerd.sock

mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

단순 개발환경 구성을 위해 `control-plane` 에서도 `Pod` 를 실행할 수 있도록 설정.  

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

L7 Gateway 역할을 해주는 `ingress` 를 사용하기위해 `ingress controller` 를 설치  

> <https://github.com/kubernetes/ingress-nginx>
> 위치는 가끔 업데이트 되지만 baremetal 디렉토리에서 아래와 같은 파일을 찾으면 된다.  
> `deploy/static/provider/baremetal/deploy.yaml`

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

이제부터 NodePort 에 설정한 `[30080, 30443]` 포트를 통해 클러스터 내부로 라우팅되는 nginx 에 접근 가능하다.  

### rancher storageClass

> <https://github.com/rancher/local-path-provisioner>  

각종 DB, ObjectStorage 등의 솔루션을 k8s 위에서 운영하려면 `PersistentVolume` 설정이 필요하다.  
AWS 의 경우 EBS 같은 서비스를 사용해 `PersistentVolume` 을 지원하지만, 베어메탈 k8s 경우 로컬스토리지를 `PersistentVolume` 으로 생성해주는 `pv provisioner` 가 필요하다.  

`rancher` 에서 이를 지원한다.  

```shell
# stable 버전으로 설치
curl https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml -o local-storage.yaml

kubectl apply -f local-storage.yaml

# local-path storageclass 생성 확인
kubectl get storageclass
NAME         PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   
local-path   rancher.io/local-path   Delete          WaitForFirstConsumer   false                  
```

`PersistentVolumeClaim` 만 지정하면 자동으로 `PersistentVolume` 을 생성하고 매핑해준다.  

> PC의 `/opt/local-path-provisioner` 위치에 pv 가 생성되도록 하드코딩되어 있음으로  
> 위치를 변경하고 싶다면 다운받은 파일에서 `data.config.json` 내부 값을 수정  

## Harbor

> <https://goharbor.io/>  
> <https://engineering.linecorp.com/ko/blog/harbor-for-private-docker-registry>  
> cloud native repository for Kubernetes 라고 소개하고 있다.  
> CNCF 졸업 프로젝트인만큼 많은 서비스사에서 운용중임.  

`k8s` 운영시 대부분 `Private Docker Registry` 를 직접 구축하여 사용하는 경우가 많음.  
SSL 연결이 필수로 요구됨으로, 공인인증 인증서 설치가 가능한 위치에 설치하는 것이 가장 쉬운방법이다.  

하지만 여기에선 베어메탈 k8s 내부에 설치하는 방법을 설명.  

Helm 으로 설치를 진행한다.  

```shell
helm repo add harbor https://helm.goharbor.io

# 압축파일 다운로드, harbor-1.13.0.tgz 버전 설치됨
helm fetch harbor/harbor

# 압축 파일 해제
tar zxvf harbor-*.tgz
# 이름변경
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
externalURL: https://core.harbor.domain
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
      secretName: "harbor-ca-secret" # 위에서 생성한 secret 이름으로 설정
...
caSecretName: "harbor-ca-secret" # 위에서 생성한 secret 이름으로 설정
```

```shell
# namespace 생성
kubectl create ns harbor
helm install harbor -f values.yaml . -n harbor

watch kubectl get pods -n harbor
```

### k8s cluster 에 harbor dns 등록  

클라이언트 PC 를 포함해서 k8s 노드에서도 설치된 `Harbor` 의 `registry` 를 찾아갈 수 있어야 한다.  

> `k8s 노드` 에서 `Pods` 를 실행시키기 위한 이미지를 `Harbor` 로부터 가져옴으로  

`hosts` 파일에 `core.harbor.domain` 도메인을 등록한다.  

```conf
# /etc/hosts
192.168.10.XXX core.harbor.domain
```

> 실제 해당 `k8s 노드` 에 `Harbor` 가 설치되어있지 않더라도 `Ingress Controller` 가 알아서 라우팅 해줄것이다.  

또한 `k8s cluster` 상에 배포된 어플리케이션이 `Harbor` 에 접근해야 할 경우,  
예를 들어 `Jenkins` 와 같은 어플리케이션이 `CI/CD` 를 위해 `Harbor` 에 접근해야할 경우 Pod 내부에서도 `Harbor` 를 찾아갈 수 있어야 한다.  

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
          192.168.10.XXX    core.harbor.domain
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

현재 `Harbor` 에 접근할 때 위에서 설정한 `사설 인증서` 가 적용된 `Ingress` 를 통해 `Harbor` 에 접근한다.  
`Docker` 클라이언트에서 `https` 프로토콜을 사용해 `registry` 에 접근할 때 `공식 서명된 인증서` 만 허용하기 때문에 `insecure-registries` 속성을 통해 인증여부 상관없이 `registry` 를 사용할 수 있도록 설정해야 한다.  

**모든 `k8s 노드` 에서도 해당 설정을 해야**, `k8s node` 에서 `imagePullbackoff` 같은 에러가 발생하지 않는다.  

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
    "https://core.harbor.domain"
  ]
}
```

설정 완료 후 클라이언트PC, 노드에서 login 및 이미지 pull push 가 잘 되는지 확인  

```sh
docker login -u admin -p Harbor12345 core.harbor.domain

docker build -t hello:demo . 
docker tag hello:demo core.harbor.domain/library/hello:demo
docker push core.harbor.domain/library/hello:demo
```

### Docker Registry Login Secret 등록

Harbor 에서 이미지를 다운받을 때 항상 인증 시크릿 `kubernetes.io/dockerconfigjson` 을 **namespace 별로** 지정해 줘야 이미지를 다운받을 수 있다.  

```yaml
# harbor-secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: regcred
  namespace: my-app-ns
type: kubernetes.io/dockerconfigjson
data:
  .dockerconfigjson: $HARBOR_DOCKER_CONFIG_JSON
```

```sh
HARBOR_DOCKER_AUTH=$(echo -n 'admin:Harbor12345' | base64) \
HARBOR_DOCKER_CONFIG_JSON=$(echo -n '{"auths": {"core.harbor.domain": {"auth": "'$HARBOR_DOCKER_AUTH'"}}}' | base64) \
envsubst < harber-default-secret.yaml | \
kubectl apply -f -

# 생성결과 확인
kubectl get secret -n my-app-ns
NAME                 TYPE                             DATA   AGE
regcred              kubernetes.io/dockerconfigjson   1      1m
```

```yaml
...
  # 파드 템플릿
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
        - name: my-app
          image: core.harbor.domain/library/my-app:latest # 컨테이너 이미지
          ports:
            - containerPort: 8080
          envFrom:
            - secretRef:
                name: my-app-secret
          env:
            - name: SERVICE_PROFILE
              value: "dev"
      imagePullSecrets:
        - name: regcred
```

## jenkins

> <https://github.com/jenkinsci/helm-charts>

`Helm` 차트를 사용하면 `Cloud Native` 하게 동작하는 `Jenkins` 설치가 가능하다.  
`master, agent` 구조로 동작하는 Jenkins 생성이 가능하다.  

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

jenkinsUrl: "https://jenkins.cluster.local"
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

jenkins container 에서도 `Harbor registry` 에 이미지를 `pull/push` 하기위해 `kubernetes.io/dockerconfigjson` 타입 시크릿 지정.  

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
HARBOR_DOCKER_CONFIG_JSON=$(echo -n '{"auths": {"core.harbor.domain": {"auth": "'$HARBOR_DOCKER_AUTH'"}}}' | base64) \
envsubst < harber-jenkins-secret.yaml | \
kubectl apply -f -

kubectl get secret -n jenkins
NAME                             TYPE                             DATA   AGE
regcred                          kubernetes.io/dockerconfigjson   1      1m
...
```

### jenkins git SSL 무시

> <https://stackoverflow.com/questions/41930608/jenkins-git-integration-how-to-disable-ssl-certificate-validation>

사내 `git` 서버를 사용중이고 `사설 인증서` 를 사용중이라면 아래와 같은 오류문구가 뜰 수 있다.  

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

### Scripted Pipeline

`Cloud Native Jenkins` 에서 `Script Pipeline` 을 사용해 간단한 gradle 프로젝트를 CI 하는 코드를 알아본다.  

> <https://www.jenkins.io/doc/pipeline/steps/kubernetes/#podtemplate-define-a-podtemplate-to-use-in-the-kubernetes-plugin>

`helm` 으로 설치한 `Jenkins` 에서 `kubernetes-plugin` 가 기본적으로 설치되어있다.  

k8s 기반 Jenkins CI 가 좋은점은 빌드시에 컨테이너기반으로 동작시킬 수 있어 추가적인 플러그인을 설치할 필요가 없다.  

```groovy
// CI 에 사용할 Pod 컨테이너
podTemplate(
    yaml: '''
kind: Pod
spec:
  containers:
  - name: gradle
    image: gradle:7.6.1-jdk17
    command: ['sleep']
    args: ['99d']
  - name: kaniko
    image: gcr.io/kaniko-project/executor:v1.6.0-debug
    command: ['sleep']
    args: ['99d']
    volumeMounts:
      - name: registry-credentials
        mountPath: /kaniko/.docker
  volumes:
    - name: registry-credentials
      secret:
        secretName: regcred
        items:
        - key: .dockerconfigjson
          path: config.json
''',
    envVars: [envVar(key: 'GIT_SSL_NO_VERIFY', value: 'false')],
) {
    node(POD_LABEL) {
        properties([
            parameters([
                // core.harbor.domain 은 k8s CoreDNS 에 이미 설정함
                string(name: 'IMAGE_REGISTRY_ACCOUNT', defaultValue: 'core.harbor.domain/library'),
                string(name: 'IMAGE_NAME', defaultValue: 'hello')
            ])
        ])
        // pipeline steps...
        stage('Get a gradle project') {
            container('gradle') {
                stage('gradle build project') {
                    def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
                    echo scmUrl
                    git branch: 'main',
                        credentialsId: 'kouzie-git-username',
                        url: scmUrl
                    sh 'gradle build -x test'
                }
            }
        }
        stage('Kaniko build image') {
            container('kaniko') {
                // dockerfile 위치와 context 위치를 pwd 명령으로 지정
                sh "executor -f `pwd`/Dockerfile -c `pwd` \
                  --insecure --skip-tls-verify --cache=true --force \
                  --destination=${params.IMAGE_REGISTRY_ACCOUNT}/${params.IMAGE_NAME}:${env.BUILD_NUMBER} \
                  --destination=${params.IMAGE_REGISTRY_ACCOUNT}/${params.IMAGE_NAME}:latest"
            }
        }
        stage('Deploy') {
            withCredentials([gitUsernamePassword(credentialsId: 'kouzie-git-username', gitToolName: 'git-tool')]) {
                sh ("""
                    sed -i 's|core.harbor.domain/library/hello:[0-9a-zA-Z]*|core.harbor.domain/library/hello:${env.BUILD_NUMBER}|g' k8s/hello.yaml
                    git config --global --add safe.directory `pwd`
                    git config --global http.sslVerify false
                    git config --global user.email jenkins@test.com
                    git config --global user.name jenkins
                    git add k8s/hello.yaml
                    git commit -m "update the image tag"
                    git push origin main
                """)
            }
        }
    }
}
```

### Declarative Pipeline

```yaml
kind: Pod
spec:
  containers:
    - name: gradle
      image: gradle:7.6.1-jdk17
      command: ['sleep']
      args: ['99d']
#      jib 으로 대체
#    - name: kaniko
#      image: gcr.io/kaniko-project/executor:v1.6.0-debug
#      command: ['sleep']
#      args: ['99d']
#      volumeMounts:
#        - name: registry-credentials
#          mountPath: /kaniko/.docker
  volumes:
    - name: registry-credentials
      secret:
        secretName: regcred
        items:
          - key: .dockerconfigjson
            path: config.json
```

```groovy
def getCurrentTime() {
    def currentDate = new Date()
    def formattedDate = currentDate.format("yyyyMMddHHmmss")
    return formattedDate
}

pipeline {
    agent {
        kubernetes {
            label 'gradle_pod'
            yamlFile 'k8s_stateful/KubernetesPod.yaml'
        }
    }
    options {
        disableConcurrentBuilds() // 파이프 라인 동시 실행 X
    }
    environment {
        CREDENTIALS_ID = 'harbor_credentials'
        CURRENT_TIME = getCurrentTime()
    }
    stages {
        stage('build service') {
            steps {
                script {
                    echo "build service start"
                    container("gradle") {
                        withCredentials([usernamePassword(credentialsId: CREDENTIALS_ID, passwordVariable: 'CREDENTIALS_PASSWORD', usernameVariable: 'CREDENTIALS_USERNAME')]) {
                            sh "gradle clean jib -PregistryUsername=${CREDENTIALS_USERNAME} -PregistryPassword=${CREDENTIALS_PASSWORD} -PuniqueBuildId=${CURRENT_TIME}"
                        }
                    }
                    echo "build service end"
                }
            }
        }
        stage('deploy service - gitops') {
            steps {
                script {
                    withCredentials([gitUsernamePassword(credentialsId: 'kouzie-git-username', gitToolName: 'git-tool')]) {
                        def scmUrl = scm.getUserRemoteConfigs()[0].getUrl()
                        git branch: 'main',
                            credentialsId: 'kouzie-git-username',
                            url: scmUrl
                        sh ("""
                            sed -i 's|core.harbor.domain/library/demo-service:[0-9a-zA-Z]*|core.harbor.domain/library/demo-service:${CURRENT_TIME}|g' k8s/demo-deploy.yaml
                            git config --global --add safe.directory `pwd`
                            git config --global http.sslVerify false
                            git config --global user.email jenkins@kouzie.com
                            git config --global user.name jenkins
                            git add k8s/demo-deploy.yaml
                            git commit -m "update the image tag"
                            git push origin main
                        """)
                    }
                }
            }
        }
    }
}
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

watch kubectl get pods -n argocd
```

작성중....