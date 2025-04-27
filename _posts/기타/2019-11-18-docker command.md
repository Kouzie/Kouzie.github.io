---
title:  "도커 - docker command!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---

# docker 개요

도커는 2013년도에 출시되었지만 컨테이너 기능은 기존에 있던 리눅스의 `lxc(Linux Container)`를 사용해 제공되어 왔다.  

`lxc`의 핵심이 `namespaces`, `cgroup` 인데 독립적인 공간 제공과 자원을 공급 역할을 담당한다.  
리눅스에서 이를 설정하려면 이미지부터 직접만들고 힘들고 번거러움 설정 작업을 해야하는데 
docker엔진이 이를 포함하고 있고 간단한 docker명령어 몇개로 조작 가능하다!   

![docker3](/assets/2019/docker3.png){: .shadow}  

도커와 기존 가상 서비스와의 차이는 `guestOS`필요 여부, 커널의 존재 여부이다.  

`mysql db` 를 지원하는 가상이미지를 사용하려면 `virtualbox` 혹은 `vmware`같은 가상화 도구 위에 운영체제 `linux` 혹은 `window` 서버를 설치하고 그 위에 `mysql db`를 설치하고 배포한다.  

docker의 경우 **`hostOS`의 자원과 커널을 공유**하기에 별도의 `guest OS`를 설치할 필요가 업다.  

![docker2](/assets/2019/docker2.png){: .shadow}  

성능과 리소스 절약을 위해 이러한 최소성을 유지하고 `centOS` 컨테이너의 경우 200MB정도밖에 안된다.  

<!-- 
어쨋건 docker로 `db`, `webserver`, `elk`와 같은 서비스를 설치하던 `centOS`와 같은 같은 OS이미지를 설치하던 해당 컨테이너에서 동작하는  OS이미지가 있긴 있다.  
단 해당 이미지의 kernal이 별도로 설치되는 것이 아닌 `hostOS`의 커널로 실행된다.  

도커는 최소성을 만족하기에 해당 기본적이로 설치되는 컨테이너안의 리눅스엔 `ifconfig`, `ps` 와 같은 기본적 명령어도 제공되지 않는다.  


## docker 설치  

현재 `hostOS`는 `ubuntu(16.04)`로 docker를 설치하기전 원활한 환경을 사용하기 위해 부가적인 툴을 설치한다.  

`sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common vim`

docker의 공식 `gpg(GNU Privacy Guard)`키 추가 보안연결을 위한 공개키 설치한다.  
`curl -fssL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -`

`sudo apt-key fingerprint` 명령실행시 아래 문구가 출력되면 공개키 등록이 완료된것.  

```
pub   4096R/0EBFCD88 2017-02-22
      Key fingerprint = 9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88
uid                  Docker Release (CE deb) <docker@docker.com>
sub   4096R/F273FCD8 2017-02-22
```

docker가 제공하는 우분투의 해당 버전의 apt리파지토리를 사용할수 있도록 등록한다.  
`sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"`  

등록하였다면 로컬 리파지토리를 업데이트하고 설치진행 후 version명령으로 확인한다.  
`sudo apt-get update`  
`sudo apt-get install docker-ce`  

docker명령을 사용할 때 항상 `sudo`를 붙여야 하는데 사용자에게 docker실행 권한을 부여
`sudo usermod -aG docker 사용자이름`  
`sudo service docker restart`  
로그아웃 후에 sudo없이 사용가능하다.(모든 원격쉘에서 로그아웃 필요, 마음 편하게 재부팅 하는 것도 좋다)  

`docker version` 명령어를 통해 
`Client`와 `Server`모두 출력되었다면 설치 완료.  

```
Client: Docker Engine - Community
 ...
 ...

Server: Docker Engine - Community
 ...
 ...
```

`Server`는 실제 컨테이너를 실행시키는 도커데몬(`dockerd`)   
`Client`는 `Server`에게 실행요청을 하는 프로그램(`docker`)  

도커데몬은 docker(클라이언트)로부터 여러 명령을 받아 컨테이너를 실행하고 멈추고 지우고 등등 작업을 한다.  
`sudo service docker stop`을 하면 도커데몬을 중지시킴으로 `server`가 사라짐을 볼수 있다.  
 -->


## docker 설치(for mac M1)

기존에 Docker Desktop 으로 docker 데몬과 GUI 설치시 컴퓨터가 느려지는 느낌을 받아 Colima + Docker 방식의 CLI 기반으로 설치하기로 결정

```sh
brew install colima docker docker-compose

colima start
# colima start --cpu 4 --memory 4 --disk 60 
# 기본설정은 2CPU, 2GB RAM, 60GB Disk

docker version
# Client: Docker Engine - Community
#  Version:           28.1.0
#  API version:       1.47 (downgraded from 1.49)
#  Go version:        go1.24.2
#  Git commit:        4d8c241ff0
#  Built:             Thu Apr 17 09:52:28 2025
#  OS/Arch:           darwin/arm64
#  Context:           colima
```

```sh
# buildx 설치
brew install docker-buildx
# 링크
ls /opt/homebrew/bin/docker-buildx
mkdir -p ~/.docker/cli-plugins
ln -sfn /opt/homebrew/bin/docker-buildx ~/.docker/cli-plugins/docker-buildx

# 기본 빌더는 buildx 로 설정
# buildx 빌더가 컨테이너로 실행되고 있어야함.
docker buildx create --name colima-builder --use
docker buildx inspect --bootstrap

# credsStore 속성 제거필요
vim ~/.docker/config.json

docker buildx version

# 설치 확인
docker info
# Client: Docker Engine - Community
#  Version:    28.1.0
#  Context:    colima
#  Debug Mode: false
#  Plugins:
#   buildx: Docker Buildx (Docker Inc.)
#     Version:  v0.23.0
#     Path:     /Users/kouzie/.docker/cli-plugins/docker-buildx

```


# docker command  

docker의 `life cycle`은 아래와 같다.  

![docker1](/assets/2019/docker1.png){: .shadow}  

해당 기능을 모두 `docker command`로 수행할수 있다.  

> `Registry`가 실제 `docker hub`의 저장소인데 `public`한 저장소는 상관 없지만 `private`한 저장소는 하나밖에 생성하지 못한다.  
> 기업입장에선 자기등리 사용하는 이미지를 모두 공개할 순 없으니 자기 서버에 별도의 `private registry`를 구축하여 사용한다.  

이미지부터 컨테이너 조작까지 할 수 있는 docker command를 하나씩 알아보자.  

## docker 이미지 관련 명령어 - pull, image, image rm, image inspect, image tag 

`docker pull image_name:version`  

`pull`명령은 다운받을 이미지의 이름과 버전을 한 쌍으로 사용한다. 버전 생략시 `latest` 사용시 최종버전을 설치한다.  
받을수 있는 버전이 궁금하다면 `docker hub` 에 가서 검색. (항상 버전을 명시해서 사용하기를 권장한다.)

`docker pull centos:7`: centOS 버전7 이미지 다운  
`docker image ls`: 다운받은 docker 이미목록 조회(`docker images` 명령도 같은 기능을 수행)   

```
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
centos              7                   67fa590cfc1c        2 months ago        202MB
```

> `IMAGE ID`는 이미지의 고유 코드로 매우 긴 값을 앞의 몇글자만 출력해준다.  
이미지 삭제, 컨테이너 생성 등의 명령에서 `IMAGE ID`를 사용하는데 중복되지 않는다면 앞의 3~4글자만 사용해도 상관없다.  

`docker image rm imagename`: `image rm`명령으로 이미지 삭제 가능하다  
(name대신 id를 넣어도 상관 없다 `docker rmi imagename`으로 사용 가능)  

만약 아래와 같은 명령이 출력된다면 해당 이미지로 컨테이너가 생성되 있어 삭제 할 수 없다는 뜻이다.  
```
Error response from daemon: conflict: unable to remove repository reference "mywebserver" (must force) - container 1d37ed64b2c9 is using its referenced image fc4c50b45ccd
```

`docker image rm -f imagename` 으로 컨테이너와 함께 강제 삭제 가능하다.  

`docker image rm -a` : 사용하지 않는 이미지 모두 삭제  
`docker image rm -f` : 이미지를 강제로 삭제  


`docker image inspect imagename`: 이미지의 각종 정보를 확인

이미지의 각종 정보를 `--format` 속성을 사용해 일부만 출력할 수 있다.  

설치된 `os`, `os bit`, `imageID`를 확인해보자  
```
$ docker inspect --format="{{.Os}}" centos:7
linux
$ docker inspect --format="{{.Architecture}}" centos:7
amd64
$ docker inspect --format="{{.ContainerConfig.Image}}" centos:7
sha256:4c66d610f9092e18227ae1d0de68350d3da2875452762261ccf1c552462dd90d
```


`docker image tag 현재이미지명 변경이미지명`: 이미 생성된 이미지에 별칭을 부여, 기존의 이미지를 그대로 사용한다.(이미지는 불변, 링크를 만드는 개념)  

> 권장되는 이미지 명칭 부여 규칙: `username/repositoryname:tag`
```
$ docker image tag nginx kouzie/webserver:1.0
$ docker image ls
kouzie/webserver          1.0                 540a289bab6c        10 days ago         126MB
nginx                     latest              540a289bab6c        10 days ago         126MB
```

아래의 `docker hub repository`에 만든 내가 만든 이미지를 업로드할 때 위의 이미지 명칭 규칙을 따라야 올릴 수 있다.  

### docker repository - docker login, docker image push  

도커 허브 `public repository`에 만든 이미지를 올릴 수 있다. 당연히 로그인 과정을 먼저 거쳐야 한다.  

```
$ docker login
Login with your Docker ID to push and pull images from Docker Hub. If you don't have a Docker ID, head over to https://hub.docker.com to create one.
Username: kouzie
Password:
WARNING! Your password will be stored unencrypted in /home/kouzie/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

`nginx`를 다운받고 해당 이미지를 내 `public repository`에 올려보자.   
```
$ docker pull nginx
$ docker image tag nginx kouzie/exam:mywebserver
$ docker image push kouzie/exam
```

> `image tag`명령을 통해 `user_name/repositort_name:tag_name` 형식으로 이름을 설정필수.  
  
명령이 끝나면 `docker hub` 페이지에 아래와 같이 `repository`가 생성되었는지 확인  

![docker6](/assets/2019/docker6.png){: .shadow}  


### docker container commit

생성한 컨테이너를 다시 이미지화 하고 싶다면 `docker container commit`명령을 수행하면 된다.  

`unbuntu`이미지를 다운받고 `apache` 서버를 설치한뒤 이를 다시 image로 저장   

```
$ docker run -it --name ubuntu_webserver -P -p 80 ubuntu:16.04
$ apt-get update
$ apt-get install apache2 -y
$ service apache2 start

[CTRL + P + Q]

$ docker commit ubuntu_webserver ubuntu14/apache2:1.0
sha256:4356ed973c2bd5ecd3c100e4d92d08b1372a685a22a7fb8b8e55c4bcb223ca5b
$ docker image ls
REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
ubuntu14/apache2        1.0                 4356ed973c2b        10 seconds ago      247MB
...
```

#### 새로 생성한 이미지 리파지토리에 올리기  

`$ docker container commit -a "kouzie" webserver webserver:nginx`

`-a` : 저자 설정 옵션

`$ docker tag webserver:kouzie kouzie/webserver:nginx`

```
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
kouzie/webserver    nginx               78c168df3453        4 minutes ago       126MB
webserver           kouzie              78c168df3453        4 minutes ago       126MB

$ docker push kouzie/webserver:nginx
```

### docker container export / import

컨테이너를 `tar`파일로 추출 삽입 가능하다.  

`$ docker export webserver > webserver.tar`  
`$ docker import .webserver.tar`  


### docker image save / load

이미지를 `tar`파일로 추출 삽입 가능하다.  
주로 인터넷 연결이 되지 않는 내부 환경에서 사용하는 명령어  

```
$ docker save mongo > mongo.tar
$ docker rmi -f mongo
Untagged: mongo:latest
Untagged: mongo@sha256:2704b1f2ad53c0c5fb029fc112f99b5e9acdca3ab869095a3f8c6d14b2e3c0f3
Deleted: sha256:965553e202a44592bc26d8c076eafef996a6dfc0de5bb2c1cf1795cd3b3a7373

$ docker load < mongo.tar
Loaded image: mongo:latest
$ docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
mongo               latest              965553e202a4        8 days ago          363MB
```


## docker container - create, start, stop, run, ps, inspect   

`docker container create imagename` 

`create`속성으로 컨테이너 생성가능하다.  
(대부분 `run` 명령을 통해 `create`와 `start`를 생략한다.) 

`docker container start containername`

이미 생성한 컨테이너는 `start`속성으로 시작가능하다.

멈출때에는 `docker container stop containername`을 사용한다. (`container` 키워드는 생략 가능하다.)

`docker run imagename` 의 경우 `[image pull], create, start` 명령이 합쳐진 명령어이다. 

`docker run centos:7 echo "hello world"`: `centOS` 이미지를 컨테이너로 생성하고 `echo`명령을 수행  

`docker run -it --name "ctin_test1" centos /bin/cal`: `centos:latest`버전을 다운받고 `/bin/cal` 프로그램 실행  

```
$ docker run -it --name "ctin_test1" centos /bin/cal

Unable to find image 'centos:latest' locally
latest: Pulling from library/centos
729ec3a6ada3: Pull complete
Digest: sha256:f94c1d992c193b3dc09e297ffd54d8a4f1dc946c37cbeceb26d35ce1647f88d9
Status: Downloaded newer image for centos:latest
    November 2019
Su Mo Tu We Th Fr Sa
                1  2
 3  4  5  6  7  8  9
10 11 12 13 14 15 16
17 18 19 20 21 22 23
24 25 26 27 28 29 30
```

`docker run -d centos /bin/ping localhost` : `centos:latest`버전을 다운받고 백그라운드로 `/bin/ping localhost` 프로그램 실행  

`docker logs -t -f 컨테이너ID`,  `-f`는 실시간으로 확인  

> 출력된 컨테이너 id를 가져와 `logs` 명령어로 현재 터미널에 출력된 값을 확인 가능하다.  
위에서 `ping`명령을 날리고 있는 `centos`에 `logs`명령으로 접근해서 결과를 확인해보자.  


```
$ docker run -it --hostname=www.test.com --add-host=node1.test.com:192.168.1.1 ubuntu:16.04
-- 컨테이너 생성, 실행과 동시에 터미널에 붙는다, 호스트명과 hosts파일 조작을 한번에 할 수 있다.  

# hostname
www.test.com

# cat /etc/hosts
127.0.0.1	localhost
::1	localhost ip6-localhost ip6-loopback
fe00::0	ip6-localnet
ff00::0	ip6-mcastprefix
ff02::1	ip6-allnodes
ff02::2	ip6-allrouters
192.168.1.1	node1.test.com
172.17.0.5	www.test.com www
```

컨테이너 일시정지, 정지해제는 `pause`와 `unpause` 명령을 사용한다.(잘 쓰이진 않음)  
```
$ docker pause container_name
$ docker unpause container_name
```


도커 컨테이너를 생성했다면 `hostOS`에서 `docker0` 브릿지와 연결되는 터널링 가상 렌카드가 생성되었는지 확인  
```
veth8d1fe0e Link encap:Ethernet  HWaddr 1a:99:df:2d:99:52
  inet6 addr: fe80::1899:dfff:fe2d:9952/64 Scope:Link
  UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
  RX packets:1397 errors:0 dropped:0 overruns:0 frame:0
  TX packets:1428 errors:0 dropped:0 overruns:0 carrier:0
  collisions:0 txqueuelen:0
  RX bytes:78867 (78.8 KB)  TX bytes:10014694 (10.0 MB)
```

생성된 컨테이너 만큼의 렌카드가 생성된다.  

**docker 컨테이너 os 확인**

grep . /etc/*-release



### docker 컨테이너 실행 속성

`-it` : `interactive tty mode(표준 입출력 터미널)`의 약자이다.   
`--name` : 컨테이너명 이름 지정  
`-p port1:port2` : port1은 `hostOS`의 port1, port2은 컨데이너의 port로 매핑한다.  
`-d` : `--detach=true`를 줄임으로 컨테이너를 백그라운드로 실행한다. 명령이 실행되면 컨테이너 고유 id를 반환한다. 
`-P`: 포트매핑시 host의 랜덤포트를 부여, 32768 부터 시작한다.  

> -p (publish)와 Dockerfile의 expose의 차이점
> publish의 경우 host와 container같의 포트매핑, expose의 경우 내부적인 포트노출, 같은 대역의 컨테이너들은 접근 가능   

### docker 컨테이너 확인  

`docker ps`: 현재 실행중인 컨테이너 목록 확인  
`docker ps -a`: 생성된 컨테이너 목록 확인  
`docker ps -a -f name=cadviser`: 컨테이너의 이름을 지정해 확인  
`docker ps -a -f exited=0`: 생성된 컨테이너중 종료된 컨테이너 확인  

### docker image, container 제거  

`docker stop $(docker ps -q)` : 실행중인 컨테이너 모두 전체 정지  
`docker rm $(docker ps -a -q)` : 컨테이너 모두 삭제(exit 된 것)  
`docker rmi $(docker images -q)` : 이미지 모두 삭제(컨테이너 생성되지 않는 것), `-f`붙이면 강제삭제  
`docker prune` : 정지중인 컨테이너 모두 삭제   


### docker 컨테이너 파일 전송 -cp 

`hostOS`에서 컨테이너로의 파일 전송시에는 파일명을 먼저 작성하고  
`docker cp file_name container_name:/file_name`   

역으로 컨테이너에서 hostOS로의 파일 전송시에는 컨테이너명을 먼저 작성한다.    
`docker cp container_name:/file_name file_name`   


## docker 컨테이너 정보조회

### docker attach  

실행중인 컨테이너의 표준 입출력을 연결해주는 명령어  

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                 NAMES
4f89c02f810f        mysql:5.7           "docker-entrypoint.s…"   About an hour ago   Up About an hour    3306/tcp, 33060/tcp   friendly_shamir
$ docker attach friendly_shamir
root@4f89c02f810f:/#
```

`docker exec -it name /bin/bash`과 같은 명령으로 컨테이너를 실행시키고 입력이 가능한 쉘을 띄어놔야 입출력이 모두 가능하고  
단순 백그라운드로 실행시킬 경우 출력되는 로그만 확인 가능하다.  

#### docker diff contain_name

컨테이너 생성후 달라진 파일시스템을 출력하는 명령어  
`useradd testuser` 를 컨테이너에서 실행후 아래 명령을 실행  

```
$ docker diff cent-server
C /home
A /home/testUser
A /home/testUser/.bash_profile
A /home/testUser/.bashrc
A /home/testUser/.bash_logout
A /test
C /etc
C /etc/shadow
C /etc/passwd-
C /etc/group
C /etc/passwd
C /etc/gshadow
C /etc/shadow-
C /etc/group-
C /etc/gshadow-
A /local.txt
C /var
C /var/log
C /var/log/lastlog
C /var/spool
C /var/spool/mail
A /var/spool/mail/testUser
```

### docker inspect  

이미지의 정보도 `docker image inspect`로 확인가능했는데 컨테이너 들도 `inspect` 명령이 있다.  

`docker inspect container_name`명령을 통해 생성된 컨테이너의 정보를 json형태로 확인 가능 

컨테이너를 설치하고 ip를 확인하려면 `ifconfig` 명령 사용을 위해 `net-tools` 설치를 해야 확인 가능한데 그런거 필요 없이 아래 명령처럼 `inspect`로 확인 가능하다.  

`docker inspect confident_shirley | grep "IPAddress"`  
```
"SecondaryIPAddresses": null,
"IPAddress": "172.17.0.2",
  "IPAddress": "172.17.0.2",
```

> 빠져나가는건 `ctrl + p + q` (`exit`을 사용하면 `docker` 컨테이너가 stop되어버린다)  
> 만약 컨테이너 안에서 `exit`명령으로도 계속 컨테이너 실행상태를 유지하고 싶다면 `--restart=”always”` 속성을 사용해 컨테이너를 실행한다.  

다시 들어가려면 `docker exec` 으로 bash을 실행하면 된다.  
`docker exec -it confident_shirley /bin/bash`  
hello world 찍을때 생성되었던 centos 컨테이너로 접속  

> `ifconfig`명령 수행을 위해 `yum install net-tools`를 설치   


`docker container stats`: 컨테이너 실행 상태 확인 (메모리, cpu, disk I/O 등이 출력된다)  
```
CONTAINER ID        NAME                CPU %               MEM USAGE / LIMIT     MEM %               NET I/O             BLOCK I/O           PIDS
8f028da11021        webserver           0.00%               1.383MiB / 3.859GiB   0.03%               2.89kB / 0B         0B / 0B             2
```

### google cadviser - docker 모니터링

구글에서 제공하는 docker 모니터링 도구이다. 다른 컨테이너를 감시하는 또 하나의 도커 컨테이너이다.  

`docker run`명령과 함께 볼륨을 일치시켜주는 속성을 적용  
```
docker run \
--volume=/:/rootfs:ro \
--volume=/var/run:/var/run:rw \
--volume=/sys:/sys:ro \
--volume=/var/lib/docker/:/var/lib/docker:ro \
--publish=8080:8080 --detach=true --name=cadviser \
google/cadvisor:latest  
```

볼륨은 후에 살펴보겠지만 `hostOS`와 컨테이너간 파일 시스템을 일부 공유할 수 있게 해주는 속성이다.  
다른 도커 컨테이너들을 모니터링 하려면 `hostOS`의 협력이 필요하기에 `hostOS`의 저장소를 `google cadviser`컨테이너와 일치시켜주어야 한다.

`8080`포트로 접속해 구동중인지 확인.  

모니터링할 docker 컨테이너를 `nginx`로 만들어보자.  
`docker run --name webserver -d -p 80:80 nginx`  

아래와 같은 실시간 모니터링 화면이 출력된다.  

![docker5](/assets/2019/docker5.png){: .shadow}  

`80`포트로 계속 새로고침을 누르며 `webserver`컨테이너가 사용하는 `cpu`, `memory`, `network` 사용량 변화를 확인  

## docker network

`docker engine`을 설치하면 가상 브릿지 네트워크가 생긴다(`docker0`).  
`virtual ethernet bridge`라 칭한다.  

![docker4](/assets/2019/docker4.png){: .shadow}  

컨테이너를 가동하면 `vethxxxxxxx`형태의 컨테이너 내부의 인터페이스(`eth0`)와 통신하는 가상의 `peer`가 생성되고 게이트웨이와 통신하는 접점역할을 수행한다  
(direct 케이블 연결 형식의 격리된 네트워크 공간을 제공).  

최종적으론 `docker0`로 사용해 host네트워크를 통해 외부네트워크와 통신한다.  

### docker network command

docker에서 사용하는 브릿지 목록을 확인하기 위해 `brctl` 설치   

```
$ sudo apt-get install bridge-utils
$ btctl show
bridge name	bridge id		STP enabled	interfaces
docker0		8000.024292bdf88f	no		veth473de41
							vethfc5d5f3       
```

현재 `docker0` 브릿지에 접근하는 2개의 peer를 확인할 수 있다.(컨테이너 2개 동작중)  

`docker container` 생성시 네트워크 설정을 진행해보자.  

```
$ docker run -d -P -p 80 nginx

$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                   NAMES
a878e70341c3        nginx               "nginx -g 'daemon of…"   56 seconds ago      Up 55 seconds       0.0.0.0:32768->80/tcp   reverent_proskuriakova

$ sudo netstat -nlp | grep 32768
tcp6       0      0 :::32768                :::*                    LISTEN      6997/docker-proxy
```

이런식으로 포트번호를 외부로 노출시키는 옵션을 부여해 컨테이너를 생성하면 포트 by 포트로 패킷을 넘기는 `docker-proxy` 프로세스가 생성된다.  
kernel이 아닌 user단에서 수행되기 때문에 host가 받은 패킷을 그대로 container의 port로 전송한다.  


```
$ docker netowrk ls
NETWORK ID          NAME                DRIVER              SCOPE
c735800b8660        bridge              bridge              local
f6972031d057        host                host                local
531d93456e6e        none                null                local
```
`docker0`와 같은 기본 브릿지네트워크 말고도 다른 네트워크 옵션을 사용할 수 있다.  
`hostOS`와 같은 네트워크 대역을 사용할건지 네트워크를 아예 사용하지 않을것이지 선택 가능하다.  

`docker network inspect bridge`  

현재 bridge 에 붙어있는 컨테이너들의 네트워크 정보를 간략히 조회 가능  
```
"Containers": {
    "14db1ca245159c7d0129dd1419ab818ce5de13361be1266a6506d12eab8c6331": {
        "Name": "pedantic_hoover",
        "EndpointID": "130fb05f769a503c51b9b39ace6c1683b14aa54e716ff0fe816c60dcc487fa76",
        "MacAddress": "02:42:ac:11:00:02",
        "IPv4Address": "172.17.0.2/16",
        "IPv6Address": ""
    },
    "a878e70341c3abaa658bd5b2387df63b134b7d0d3a5cd6b0db5d42e66a15014c": {
        "Name": "reverent_proskuriakova",
        "EndpointID": "45c514593649572b733898d1d1155a003ff6ea32faaebe60ff164da89e66ba21",
        "MacAddress": "02:42:ac:11:00:04",
        "IPv4Address": "172.17.0.4/16",
        "IPv6Address": ""
    }
},
```

### docker network create 

`docker0`는 일종의 소프트웨어적인 스위치 개념으로 아래와 같은 대역을 갖는다.  

```
docker0   Link encap:Ethernet  HWaddr 02:42:92:bd:f8:8f
          inet addr:172.17.0.1  Bcast:172.17.255.255  Mask:255.255.0.0
```
또한 컨테이너 생성시 기본적으로 할당되는 브릿지이다.  
별도의 분리된 브릿지 네트워크를 생성하고 컨테이너를 생성해 해당 브릿지에 붙여보자.  

```
$ docker network create -d bridge webap-net
4fc0a743bfbb014caee7788c2625724407ed376d9b05a301839507570919ef1a

$ ifconfig
...
br-a9d59233db3b Link encap:Ethernet  HWaddr 02:42:d1:fc:15:c5
          inet addr:172.18.0.1  Bcast:172.18.255.255  Mask:255.255.0.0
          UP BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)
```

`-d, --driver` : 네트워크를 `bridge`, `host`, `none` 등으로 설정할 때 사용  

`br-a9d59233db3b`라는 이름의 인터페이스가 생성되었다. 18의 `B class`의 ip대역을 갖는다.  

물론 속성을 통해 ip 대역을 직접 설정 가능하다.  

```
$ docker network create --driver bridge --subnet 172.100.1.0/24 --ip-range 172.100.1.0/24 --gateway=172.100.1.1 vswitch01
8db2d48388b722912b6c0e49c9fd813786ab1eb1d0070de3f00ab64f3eed153b

$ docker network inspect vswitch01
[
    {
        "Name": "vswitch01",
        "Id": "8db2d48388b722912b6c0e49c9fd813786ab1eb1d0070de3f00ab64f3eed153b",
        "Created": "2019-11-02T15:42:15.706759964+09:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.100.1.0/24",
                    "IPRange": "172.100.1.0/24",
                    "Gateway": "172.100.1.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {},
        "Options": {},
        "Labels": {}
    }
]
```

`docker network ls` 명령으로 네트워크 검색을 해보면  
```
$ docker network ls
NETWORK ID          NAME                DRIVER              SCOPE
c735800b8660        bridge              bridge              local
f6972031d057        host                host                local
531d93456e6e        none                null                local
8db2d48388b7        vswitch01           bridge              local
4fc0a743bfbb        webap-net           bridge              local
```

기존에 만들었던 `webap-net`과 `vswitch01`가 추가됨을 확인 가능하다.  

### docker 컨테이너 ip 확인  

컨테이너 생성시 `--net` 속성으로 사용할 네트워크 인터페이스를 지정 가능하다.  

`apt-get install net-tools`로 `ifconfig`를 설치해 ip확인도 가능하지만 `ip a`나 `inspect` 속성으로 간단히 확인도 가능하다.  

```
$ docker container run --net=webap-net -it --name mycent centos:7

[CTRL + Q + P]

$ docker exec mycent ip a

1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
36: eth0@if37: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default
    link/ether 02:42:ac:12:00:02 brd ff:ff:ff:ff:ff:ff link-netnsid 0
    inet 172.18.0.2/16 brd 172.18.255.255 scope global eth0
       valid_lft forever preferred_lft forever

$ docker inspect mycent | grep "IPAddress"
    "SecondaryIPAddresses": null,
    "IPAddress": "172.17.0.2",
            "IPAddress": "172.17.0.2",

$ docker network inspect webap-net
[
    {
        "Name": "webap-net",
        "Id": "4fc0a743bfbb014caee7788c2625724407ed376d9b05a301839507570919ef1a",
        "Created": "2019-11-02T15:31:17.76002942+09:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.18.0.0/16",
                    "Gateway": "172.18.0.1"
                }
            ]
        },
        "Internal": false,
        "Attachable": false,
        "Ingress": false,
        "ConfigFrom": {
            "Network": ""
        },
        "ConfigOnly": false,
        "Containers": {
            "1d5fccc2df16d1ba3e43f66509559758ada5ba18e13ded9f4b94c25081b66836": {
                "Name": "modest_matsumoto",
                "EndpointID": "65a645d3fa1a3ec4b5fa79f2c109c6c9efa7e64e64e30c1000d885b301d26ba1",
                "MacAddress": "02:42:ac:12:00:02",
                "IPv4Address": "172.18.0.2/16",
                "IPv6Address": ""
            }
        },
        "Options": {},
        "Labels": {}
    }
]
```

기존 `docker0`의 대역`172.17.0.0/24`에서 1 더한 `172.18.0.0/24` 대역의 `bridge`가 생성되었다.  

그리고 현재 해당 브릿지 네트워크에 붙어있는 컨테이너 정보도 출력되는데  
`--net=webap-net` 속성으로 실행시킨 `modest_matsumoto` 컨테이너의 ip 는 자동으로 `172.18.0.2` 설정되었다.  

### docker 컨테이너 - 가상네트워크 - 브릿지 연결 확인  

```
$ brctl show
bridge name	bridge id		STP enabled	interfaces
docker0		8000.02428bcce5ca	no		veth220fd5a
br-4fc0a743bfbb		8000.024292bdf88f	no		vetha6f982e
```
`brctl show`명령으로 확인해보면 브릿지 2개가 동작중이다.  
각 컨테이너가 어떤 가상 네트워크 인터페이스를 통해 브릿지에 연결되어 있는지 확인해보자.  

> 단순 `컨테이너 - 브릿지` 연결 정보는 `docker network inspect`혹은 ifconfig 명령으로 대조해서 확인 가능하다.  

먼저 아래의 명령으로 `modest_matsumoto`이름의 컨테이너의 네트워크 정보 확인  

```
$ docker exec modest_matsumoto ip addr show eth0  

36: eth0@if37: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default
    link/ether 02:42:ac:12:00:02 brd ff:ff:ff:ff:ff:ff link-netnsid 0
    inet 172.18.0.2/16 brd 172.18.255.255 scope global eth0
       valid_lft forever preferred_lft forever
```

`36`을 잘 기억해두자.   

브릿지만 네트워크 아이디확인  

`/sys/class/net/` 디렉토리에 접근하면 브릿지를 포함한 각종 네트워크 인터페이스 파일이 존재한다.  
`ifindex` 파일을 출력하면 `36`에서 +1 된 정수가 나올 것인데 해당 인터페이스가 브릿지와 컨테이너를 이어주느 가상 인터페이스이다!  

```
$ cat /sys/class/net/vetha6f982e/ifindex
27
$ cat /sys/class/net/veth220fd5a/ifindex
37
```
`modest_matsumoto`는 `veth220fd5a`와 peer를 이루고 있었다.  

이제 `veth220fd5a` 가상 인터페이스가 어떤 브릿지 네트워크와 연결되어있는지 확인하면 된다.  

```
$ brctl show

bridge name	bridge id		STP enabled	interfaces
docker0		8000.0242911aa594	no		veth2ef053f
```
`brctl show`명령으로 어떤 브릿지와 가상 인터페이스간 연결 정보를 확인 가능하다.  

즉 `modest_matsumoto`컨테이너는 `veth220fd5a` peer와 연결되어 있고  
해당 peer는 `docker0` 브릿지와 연결되어 있다.  

### docker container 네트워크 붙이기  

기존에 생성해둔 `webap-net` 브릿지 네트워크를 컨테이너에 붙여보자.  

```
$ docker run -it centos:7 bash
$ docker netowrk create --driver=bridge webap-net
$ docker network connect webap-net dreamy_wilbur # 생성된 컨테이너 이름
```

컨테이너 안에서 ifconfig 명령으로 네트워크 인터페이스 확인    
```
[root@ce1f8e20e4db /]# ifconfig
eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.17.0.4  netmask 255.255.0.0  broadcast 172.17.255.255
        ether 02:42:ac:11:00:04  txqueuelen 0  (Ethernet)
        RX packets 2047  bytes 10064351 (9.5 MiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 2045  bytes 113964 (111.2 KiB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

eth1: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 172.18.0.2  netmask 255.255.0.0  broadcast 172.18.255.255
        ether 02:42:ac:12:00:02  txqueuelen 0  (Ethernet)
        RX packets 53  bytes 8247 (8.0 KiB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 0  bytes 0 (0.0 B)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```

### docker network 삭제  

```
$ docker network rm webap-net
Error response from daemon: error while removing network: network webap-net id a9d59233db3b4a66b257c10200d45797abf3790cfe6a10332dca430abef00d59 has active endpoints
```

당연히 기존에 사용중인 네트워크는 삭제가 불가능해서 연결을 끊고 삭제해야한다.  

`docker network disconnect webap-net dreamy_wilbur` 혹은 컨테이너를 stop하고 삭제해도 상관없다.  

### 127.0.0.11

docker 내부의 dns서버 IP는 127.0.0.11
호스트의 이름을 유동적인 컨테이너를 찾을 때 주로 사용  

아래와 같이 docker 내부 nginx 에서 서버를 동작중이라면 docker dns 로부터 refresh 수행이 가능하다.  

```conf
http {
  client_max_body_size 15M;

  resolver 127.0.0.11 valid=5s;  # Docker 내 DNS 서버 사용

  upstream api-server {
    server api-server:8080;
  }
}
```

## doker 컨테이너간 통신 - ssh 

```
$ docker run -it --name c7_ssh centos:7

# yum install openssh-server openssh-clients -y

-- SSH key 생성
# ssh-keygen -q -b 1024 -N '' -t rsa -f /etc/ssh/ssh_host_rsa_key
# ssh-keygen -q -b 1024 -N '' -t dsa -f /etc/ssh/ssh_host_dsa_key
# ssh-keygen -q -b 521 -N '' -t ecdsa -f /etc/ssh/ssh_host_ecdsa_key
# ssh-keygen -q -b 521 -N '' -t ed25519 -f /etc/ssh/ssh_host_ed25519_key

-- ssh를 실행하도록 bashrc에 환경변수 설정
# echo /usr/sbin/sshd >> /root/.bashrc && source /root/.bashrc

-- 생성된 키를 인증할 수 있는 키 목록에 추가
# mkdir ~/.ssh
# cat /etc/ssh/ssh_host_rsa_key >> ~/.ssh/id_rsa
# cat /etc/ssh/ssh_host_rsa_key.pub >> ~/.ssh/authorized_keys

-- ssh의 설정 파일을 새로 만들고 아래 내용을 넣으면 ssh 접속 시 yes/or 질문을 차단
# vi /root/.ssh/config
Host *
        StrictHostKeyChecking no
        
-- 비밀번호 인증 비활성화
# sed -i -e 's~^PasswordAuthentication yes~PasswordAuthentication no~g' /etc/ssh/sshd_config

-- 키 파일의 권한 조정
# chmod 400 ~/.ssh/id_rsa
```

설정이 끝났으면 해당 컨테이너를 이미지화 해서 2개의 컨테이너 실행  
```
$ docker commit c7_ssh ssh-test-image:0.0

docker run -it --name c7_ssh_test1 ssh-test-image:0.0
docker run -it --name c7_ssh_test2 ssh-test-image:0.0
```

ip확인후 각 컨테이너에 접속해 서로 ssh로 접근시도시 비밀번호 체크를 하는지 확인  
```
$ docker inspect c7_ssh_test1 | grep 172
            "Gateway": "172.17.0.1",
            "IPAddress": "172.17.0.4",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.4",
$ docker inspect c7_ssh_test2 | grep 172
            "Gateway": "172.17.0.1",
            "IPAddress": "172.17.0.5",
                    "Gateway": "172.17.0.1",
                    "IPAddress": "172.17.0.5",

-- 실제 ssh 접근시 암호 입력이 필요한지 확인  
$ docker exec -it c7_ssh_test1 bash
# ssh 172.17.0.5

$ docker exec -it c7_ssh_test2 bash
# ssh 172.17.0.4
```

## docker 컨테이너 resource limit, 모니터링

3대 리소스 `cpu`, `memory`, `network`를 모니터링  

### network 모니터링

`iptraf-ng`라는 툴을 설치해 확인  

```
$ sudo apt-get install iptraf-ng
$ sudo iptraf-ng
```

실행시키면 다음과 같은 화면이 출력

![docker7](/assets/2019/docker7.png)  


`container -> 실제OS`(mac혹은 윈도우)로 ping 요청시 어떻게 출력되는지 확인  

![docker8](/assets/2019/docker8.png)  

### memory, cpu 모니터링, 제한  

메모리 제한을 하면 아래처럼 실패 메세지가 출력된다.  

```
$ docker run -d --memory=1g --name=nginx_mem_1g nginx
WARNING: Your kernel does not support swap limit capabilities or the cgroup is not mounted. Memory limited without swap.
6f60b58a001137fb72409f183b1a914ac9035080d3836a4b740e3993e6d211a9
```

`hostOS`의 별도의 커널 설정이 필요  

```
$ sudo vi /etc/default/grub

-- 주석처리 후 아래와 같이 수정
GRUB_CMDLINE_LINUX_DEFAULT="cgroup_enable=memory swapaccount=1"

$ sudo update-grub
$ sudo reboot

$ docker run -d --memory=1g --name=nginx_mem_1g nginx
049bb26173d89fc794b7416cba6b4979fe55d4ae9befae28212d35e479104d90
```

메모리 공간 뿐 아니라 스왑 공간까지 제한해보자  

```
$ docker run -m=200m --memory-swap=300m -it ubuntu:16.04 /bin/bash
$ docker ps
CONTAINER ID        IMAGE                COMMAND                  CREATED             STATUS              PORTS               NAMES
7df0e835c797        ubuntu:16.04         "/bin/bash"              16 seconds ago      Up 16 seconds                           objective_kowalevski

$ docker inspect objective_kowalevski | grep "Memory"
            "Memory": 209715200,
            "KernelMemory": 0,
            "KernelMemoryTCP": 0,
            "MemoryReservation": 0,
            "MemorySwap": 314572800,
            "MemorySwappiness": null,
```

cpu모니터링을 `htop`을 통해 진행하자.  

`htop` 설치  
`sudo apt-get install htop`

아무것도 실행하고 있지 않을때 cpu코어 2개와 4g만큼의 메모리를 할당한 우분투의 상태  

![docker9](/assets/2019/docker9.png)  


`alicek106/stress`라는 테스트용 컨테이너를 설치해 다시한번 모니터링 해보자.  


```
$ docker run -d --name cpu_1024 --cpu-shares 1024 alicek106/stress stress --cpu 1  
$ docker run -d --name cpu_512 --cpu-shares 512 alicek106/stress stress --cpu 1  
```
`--cpu-shares`: cpu할당율을 제한, 1024가 기본값으로 뒤에 값을 줄이거나 늘려 cpu할당률을 늘릴 수 있다.  
`--cpu 1`: 사용 코어 개수를 1개로 지정  

```
$ ps -auxf | grep stress
root      6268  0.3  0.0   7484   868 ?        Ss   17:47   0:00  |   \_ stress --cpu 1
root      6303 98.4  0.0   7484    92 ?        R    17:47   0:31  |       \_ stress --cpu 1
root      6377  0.5  0.0   7484   876 ?        Ss   17:47   0:00      \_ stress --cpu 1
root      6417 99.1  0.0   7484    92 ?        R    17:48   0:20          \_ stress --cpu 1
kouzie      6446  0.0  0.0  22572   956 pts/8    S+   17:48   0:00  |           \_ grep --color=auto stress
```

확실히 cpu점유 시간이 11초 차이가 나긴 하지만 31초의 절반인 15초만큼은 아니다   
환경에 따라 cpu 스케줄링 시간을 반절을줘도 거의 비슷한 실행시간을 갖는 경우도 있다. (코어수가 많은경우)  

이번엔 두개의 cpu 모두 사용하도록 설정하고 htop으로 사용량 체크  
`docker run -d --name cpuset_2 --cpuset-cpus=0,1 alicek106/stress stress --cpu 2`  

htop화면  

![docker10](/assets/2019/docker10.png)  

google cadviser 화면  

![docker11](/assets/2019/docker11.png)  


`--cpuset-cpus=0,3`: 0번째, 3번째 코어만 사용
`--cpuset-cpus=0-2`: 0번째, 1번째, 2번째 코어사용


### docker 컨테이너 update

실행중인 컨테이너에 cpu 사용량을 줄이고 싶다면  
`docker update --cpus 0.2 cpuset_1`  

`--cpus`: cpu코어 개수를 0.2개로 설정  

## docker 컨테이너 volume

`hostOS`와 컨테이너의 파일 공유  

먼저 `hostOS`에 공유할 디렉토리 2개 생성 

`$ mkdir hello1 hello2`  

컨테이너 생성시 `-v` 속성으로 공유 디렉토리 설정  

각 폴더에 test라는 파일을 만들고 문자열을 입력한다.  
```
$ cat > hello1/test
hello1 test

$ cat > hello2/test
hello2 test
```

위에서 만든 2개 디렉토리를 ubuntu 컨테이너의 볼륨으로 설정  
```
$ docker run -it --name ubuntu_volume \
-v /home/kouzie/hello1:/hello1 \
-v /home/kouzie/hello2:/hello2 \
ubuntu:16.04

# ls
bin  boot  dev  etc  hello1  hello2  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var

# cat hello1/test
hello1 test

# cat hello2/test
hello2 test

# mount | grep hello
/dev/sda1 on /hello1 type ext4 (rw,relatime,errors=remount-ro,data=ordered)
/dev/sda1 on /hello2 type ext4 (rw,relatime,errors=remount-ro,data=ordered)

# df -h | grep hello
Filesystem      Size  Used Avail Use% Mounted on
/dev/sda1        50G  7.3G   40G  16% /hello1
```

> `mount`는 2개 모두 출력되지만 `df`는 하나만 확인 가능하다

`docker inspect`명령으로도 컨테이너에 어떤 디렉토리가 매핑되었는지 확인 가능하다.  

```
$ docker inspect --format="{{ .HostConfig.Binds }}" ubuntu_volume
[/home/kouzie/hello1:/hello1 /home/kouzie/hello2:/hello2]
```

> 참고: `hostOS`에 볼륨용 디렉토리를 만들지 않아도 docker 컨테이너 생성시 자동으로 만들어진다.  


### 컨테이너 volume 용량 제한  

`mkdir` 명령으로 `myvolume` 디렉토리를 만들고 아래 명령을 실행  

`docker run -v /home/kouzie/myvolume:/webapp -it ubuntu:16.04`

docker안에서 `df -h`명령으로 용량을 확힌해보면 `hostOS`에서 사용할 수 있는 용량과 같다  

`/dev/sda1        50G  7.3G   40G  16% /hello1`
hostOS의 전체용량인 50G가 출력된다.  

이를 `hostOS`에서 용량제한을 설정할 수 있다. 아래명령을 `root` 권한에서 실행  

`/home/kouzie/myvolume` 디렉토리를 만들고 용량 제한을 설정한뒤 docker컨테이너에 볼륨설정해보자.  

```
# dd if=/dev/zero of=harddrive.img count=512 bs=1M
# mkfs.ext4 harddrive.img
# fdisk -l harddrive.img
# mkdir /home/kouzie/myvolume
# mount -o loop harddrive.img /home/kouzie/myvolume
# chown -R kouzie.kouzie /home/kouzie/myvolume
```

`docker run -v /home/kouzie/myvolume:/webapp -it ubuntu:16.04`  

![docker12](/assets/2019/docker12.png){: .shadow}  

설정댈로 `512MB` 크기의 용량을 갖는다.  

## docker 컨테이너 환경설정  

**환경변수**  

오라클이나 자바를 사용하려면 환경변수 설정이 필수인데 환경변수를 설정한 파일을 사용해 docker 컨테이너에 적용할 수 있다.  

설정할 환경변수가 몇개 안될 경우 -e옵션으로 입력가능하다.  
`docker run -it -e foo=bar centos:7 /bin/bash`

컨테이너 안에서 `set`명령으로 환경변수를 확인. 

여러개의 환경변수를 설정할때에는 `--evn-file=evn_list`

`docker run -it --env-file=env_list.txt centos:7 /bin/bash`

`evn_list.txt`파일은 아래처럼 설정  

```
hoge=fuga
foo=bar
```

**워킹디렉토리**

docker의 기본 워킹 디렉토리는 `root`디렉토리인데 `-w` 옵션으로 처음 들어갈 때 워킹디렉토리를 지정할 수 있다.  

간단한 환경변수설정이나 워킹디렉토리 설정은 옵션으로 지정 가능하지만 여러 설정이 필요한 경우 `Dokcerfile`이나 `docker-compose`로 처리하는 경우가 대부분.  

## docker 컨테이너 연결 - 링크, 네트워크   

도커의 철학은 한 컨테이너 안에 `DB + WEB + WAS` 등 모든 서비스를 묶는 게 아닌 하나의 서비스만을 가볍게 지원하도록 만들어진 개념이다.  

각 서비스를 만들고 링크를 걸어보자.  

https://hub.docker.com/_/mysql
사이트의 `Environment Variables` 항목에 필요한 환경변수들이 모두 있다.  


```
$ docker run -d --name wordpressdb -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=wordpress mysql:5.7
$ docker run -d --name wordpress -e WORDPRESS_DB_PASSWORD=password --link wordpressdb:mysql -p 80 wordpress

$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                   NAMES
28ccddae6f48        wordpress           "docker-entrypoint.s…"   10 seconds ago      Up 9 seconds        0.0.0.0:32768->80/tcp   wordpress
0fde3543cca3        mysql:5.7           "docker-entrypoint.s…"   3 minutes ago       Up 3 minutes        3306/tcp, 33060/tcp     wordpressdb
```

32768포트에 접속해서 확인해 db에 데이터가 입력되는지 확인해보자.  

이번엔 링크가 아닌 네트워크로 연결해 운영해보자.  

```
-- wordpress 전용 network 생성  
$ docker network create --driver=bridge wp-network

-- hostOS에 wp_db 디렉터리를 만들고 컨테이너의 var/lib/mysql을 현재위치밑의 wp_db에 연결 
$ docker run -d --name wp-db --network wp-network -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=wp -v `pwd`/wp_db:/var/lib/mysql mysql:5.7
-- $ ls wp_db가 생겼는지 확인  

-- 만들어둔 도커 컨테이너와 통신 하기 위해 환경변수 설정
$ docker run -d --name wp --network wp-network -e WORDPRESS_DB_PASSWORD=password -e WORDPRESS_DB_HOST=wp-db -p 8080:80 wordpress

$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                  NAMES
75a8b0fed0bf        wordpress           "docker-entrypoint.s…"   18 seconds ago      Up 16 seconds       0.0.0.0:8080->80/tcp   wp
844feade3d93        mysql:5.7           "docker-entrypoint.s…"   2 minutes ago       Up 2 minutes        3306/tcp, 33060/tcp    wp-db
```

> 참고: 데이터베이스 데이터가 wp_db와 볼륨을 이루고 있어 공유되고 있는데 해당 컨테이너를 삭제해도 공유된 데이터는 삭제되지 않는다.  

## docker 볼륨

`-v` 옵션으로 `hostOS`와 컨테이너간의 파일 시스템 공유를 했다면  

`docker volume` 명령으로 도커파일에 볼륨을 생성하고 컨테이너간 해당 볼륨을 공유할 수 있다.  

```
$ docker volume create wp-db-volume
wp-db-

$ docker inspect --type volume wp-db-volume
[
    {
        "CreatedAt": "2019-11-09T14:35:36+09:00",
        "Driver": "local",
        "Labels": {},
        "Mountpoint": "/var/lib/docker/volumes/wp-db-volume/_data",
        "Name": "wp-db-volume",
        "Options": {},
        "Scope": "local"
    }
]

$ sudo ls /var/lib/docker/volumes/wp-db-volume
_data

# 생성한 볼륨을 사용해 컨테이너에 적용
$ docker run -d --name wp-db --network wp-network -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=wp -v wp-db-volume:/var/lib/mysql mysql:5.7

# 실제 볼륨은 아래 위치에 저장되며 wordpress로 인해 생성된 mysql 관련 데이터가 저장됨을 알 수 있다.  
$ sudo ls /var/lib/docker/volumes/wp-db-volume/_data
auto.cnf    ca.pem	     client-key.pem  ibdata1	  ib_logfile1  mysql		   private_key.pem  server-cert.pem  sys
ca-key.pem  client-cert.pem  ib_buffer_pool  ib_logfile0  ibtmp1       performance_schema  public_key.pem   server-key.pem
```