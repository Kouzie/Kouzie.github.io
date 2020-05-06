---
title:  "도커 - Dockerfile, docker-compose!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - docker

tags:

toc: true
toc_sticky: true

---

# Dockerfile


`nginx`서버 `/usr/share/nginx/html/index.html` 디렉토리에 `index.html`파일을 삽입하여 다시 이미지를 만들고 싶을때  
이미 존재하는 `nginx` 서비스를 사용할때 다시 이미지화 할 수 있으면 좋지 않을까?  

**이미지 레이어**를 사용해 효과적으로 이미지를 생성할 수있도록 스크립트를 제공하는데 `Dockerfile`이다.  

다음과 같이 `Dockerfile`을 작성  
```
FROM nginx:latest
COPY index.html /usr/share/nginx/html/index.html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

> `index.html`파일에 `<h1>Hello Docker!<h1>` 삽입!  

`docker build -t mywebserver ./`   
현재 디렉토리의 `Dockfile`을 사용해서 `mywebserver`란 이미지를 새성한다.  

> `docker build -t 생성이미지이름:태그 Dockerfile위치`

출력값  

```
Sending build context to Docker daemon  3.072kB
Step 1/4 : FROM nginx:latest
 ---> 540a289bab6c
Step 2/4 : COPY index.html /usr/share/nginx/html/index.html
 ---> Using cache
 ---> 560cc55dd2ad
Step 3/4 : EXPOSE 80
 ---> Using cache
 ---> 6c70c38cad8f
Step 4/4 : CMD ["nginx", "-g", "daemon off;"]
 ---> Using cache
 ---> fc4c50b45ccd
Successfully built fc4c50b45ccd
Successfully tagged mywebserver:latest
```

`mywebserver`라는 이미지가 생성되었다, 기존의 nginx이미지를 사용해서!  

> 주의: 도커파일의 이름 기본값은 `Dockerfile`이며 이를 변경하면 별도의 이름 지정 속성을 사용해야 한다.  

## Dokcerfile 명령어  

도커파일은 docker이미지를 생성하기 위한 파일로 기존 이미지에 여러가지 설정을 붙여 새로운 이미지를 만들때 사용하는 파일이다.  

**명령어** | **설명**
|---|---|
`FROM` | 가장 기본이 되는 이미지 지정, 필수 입력 항목   
`RUN` | 커맨드 실행, 실행할 때 마다 새로운 레이어가 생성된다, 때문에 왠만한건 한줄처리로 해결하는 것이 좋다. `RUN apt-get update && apt-get install -y curl nginx`  
`CMD` | `docker run` 명령인자 생략시에 실행되는 `default` 명령어 혹은 `default` 파라미터, 파라미터는 `ENTRYPOINT` 에서 사용된다. 한번만 입력 가능.  
`ENTRYPOINT` | 데몬 실행, `docker run` 시 실행되는 명령어, `CMD` 와 다르게 생략되지 않는다  
`ONBUILD` | 빌드완료후 실행할 명령, 여기서 명령은 `Dockerfile`의 명령(`ADD`, `COPY` 등), `ONBUILD`를 통해 새로운 이미지 레이어가 생성되고 지정된 명령어를 수행한다.  
`STOPSIGNAL` | 컨테이너 종료시 송신하는 시그널 설정. 시그널번호(`9`), 시그널명(`SIGKILL`) 지정 가능  
`HEALTHCHECK` | 컨테이너 안의 프로세스가 정상동작하는지 체크, ex: `HEALTHCHECK --interval=5m --timeout=3s CMD cur -f http://localhost/ || exit 1`  
`ENV` | 환경변수 지정, 공백이 표함될 경우 쌍따옴표 필수, 명령 사용마다 레이어가 늘어남으로 `\` 로 계행하여 한줄로 사용하는 것이 효율적. `docker run --env` 로 기본값 대체 가능  
`ARG` | `Dockerfile` 안의 환경변수, `ENV` 와는 달리 `Dokcerfile` 안에서만 사용 가능. `docker run --build-arg` 로 기본값 대체 가능  
`WORKDIR` | 작업 디렉터리 지정, 여러번 사용해 디렉토리 지정이 가능하며 상대 경로 입력해 현 경로에서 이동함.   
`LABEL` | 라벨 지정, key value 형식의 각종 코멘트(작성자, 버전 등) 지정   
`EXPOSE` | 공개 포트번호 지정, ex: `EXPOSE 8080`  
`ADD` | 파일 및 디렉토리 추가, `ADD <호스트 파일경로> <Docker이미지 파일경로>`, 호스트 파일 경로는 `HTTP URL` 가능. `*`, `?` 기호 사용하여 파일 이름 형식 지정 가능.<br>`WORKDIR` 과 상대경로로 디렉터리 지정, 호스트 파일경로를 `URL` 로 사용시 마지막 디텍토리 슬래시 `/` 를 제거해 파일명 지정 가능, 제거하지 않을경우 `URL`로 파일명 지정.<br>tar, gzip 등의 압축은 자동으로 해제됨, 단 URL의 경우 풀리지 않음.<br>`.dockerignore` 을 통해 특정 파일 제외 가능  
`COPY` | 파일 및 디렉토리 추가, `ADD` 가 더 많은 기능을 포함하고 있다. 단순 로컬 파일을 이미지로 전달하고 싶을땐는 `COPY` 를 사용.  
`USER` | `RUN`, `CMD`, `ENTRYPOINT` 실행 사용자 지정. ex: `USER [username/UID]`   
`SHELL` | 기본 쉘 설정. ex: `SHELL ["/bin/sh", "-c"]`  
`VOLUME` | 볼륨 마운트 ex: `VOLUME ["/data/log", "/var/log"]`, 인자를 하나만 사용할 경우 호스트와 컨테이너 모두 해당 디렉토리를 사용.  
`MAINTAINER` | 작성자 지정  

> `RUN, CMD, ENTRYPOINT` 차이점: https://blog.leocat.kr/notes/2017/01/08/docker-run-vs-cmd-vs-entrypoint  

`RUN` - 새로운 레이어에서 명령어를 실행하고, 새로운 이미지를 생성한다. 보통 패키지 설치 등에 사용된다. `e.g. apt-get`  

`CMD` - `default` 명령/파라미터를 설정한다. `docker run` 실행 시 실행 커맨드를 주지 않으면 이 `default` 명령이 실행된다. 실행 커맨드 입력 `CMD` 설정은 생략된다.

`ENTRYPOINT` - CMD 와 차이점은 실행 커맨드 입력시에도 생략되지 않는것. 아래와 같은 설정이 가능하다.  

```dockerfile
FROM ubuntu:16.04
ENTRYPOINT ["top"]
CMD ["-d", "10"]
```
해당 `Dockerfile` 로 `sample` 이란 이미지를 생성 후 아래처럼 사용 가능  

```
$ docker run -it sample # CMD 의 인자를 그대로 사용, 10초 간격 갱신
$ docker run -it sample -d 2 # CMD 의 인자 생략, 2초 간격 갱신    
```

`FROM`만 필수항목이고 나머지는 모두 없어도 된다.  
(나머지 속성은 모두 기본이미지에 살을 붙이는 속성)    

`FROM` 명령만 적어 `Dockerfile`을 생성해보자.  
`ubuntu` 컨테이너에 `nginx`를 설치하고 `index.html`파일을 삽인한 후 다시 이미지화.  

```dockerfile
# Dockerfile.base
# Dockerfile 에선 주석처리를 # 을 이용
# ubuntu 컨테이너 생성
FROM ubuntu:16.04

# 명령어 실행
RUN apt-get update && apt-get install -y -q nginx

# 현재 디렉토리에 index.html을 해당 경로에 복사
COPY index.html /usr/share/nginx/html

# 데몬 실행 daemon off는 포그라운드로 실행 
CMD ["nginx", "-g", "daemon off;"]
```


`docker images`로 내가 지정한 이미지가 생성됬는지 확인하고 `docker history 이미지명:태그명`으로 `Dockerfile`을 실행하면서 어떤 명령들이 실행되었는지 확인가능하다.   


```
$ docker build -t ubuntunginx:1.0 -f Dockerfile.base .
$ docker history ubuntunginx:1.0
IMAGE               CREATED              CREATED BY                                      SIZE                COMMENT
f4dcde6a2f2e        About a minute ago   /bin/sh -c #(nop)  CMD ["nginx" "-g" "daemon…   0B
f4b126ec4a47        About a minute ago   /bin/sh -c #(nop) COPY file:b673a17565a2aafc…   23B
4b48682856d7        2 minutes ago        /bin/sh -c apt-get update && apt-get install…   82.1MB
5f2bf26e3524        8 days ago           /bin/sh -c #(nop)  CMD ["/bin/bash"]            0B
<missing>           8 days ago           /bin/sh -c mkdir -p /run/systemd && echo 'do…   7B
<missing>           8 days ago           /bin/sh -c set -xe   && echo '#!/bin/sh' > /…   745B
<missing>           8 days ago           /bin/sh -c rm -rf /var/lib/apt/lists/*          0B
<missing>           8 days ago           /bin/sh -c #(nop) ADD file:9511990749b593a6f…   123MB
```

`-f` : Dockerfile 별도 이름 지정  


생각해보면 docker hub에서 이미지 다운받아 컨테이너를 만들때 부터 어떤 포트가 들어갈지, 어떤 명령을 수행할지 이미 설정된 이미지들이 있는데 모두 `Dockerfile`로 만들어진 이미지들이다.  

### union file system

위의 출력값을 통해 도커는 `union file system`을 사용함을 알 수 있다.  
계층화된 파일 시스템을 사용해 중복 데이터를 피해 효율적인 이미지 관리를 할 수 있다.  
(시간 단축, 데이터 효율!)

`docker images`로 확인해 보면 기존에 생성했던 `ningx` 이미지를 사용해 새로만든 `mywebserver`의 이미지를 생성한다.  

```
REPOSITORY                TAG                 IMAGE ID            CREATED             SIZE
mywebserver               latest              fc4c50b45ccd        26 hours ago        126MB
nginx                     latest              540a289bab6c        4 days ago          126MB
```

`docker run --name newNginx -d -p 80:80 mywebserver`명령어로 해당 이미지를 실행시키고 위에서 정의한 `index.html` 파일이 `/usr/share/nginx/html` 디렉터리에 들어있는지 확인  

`docker image inspect`로 `nginx:latest` 이미지와 `mywebserver` 이미지를 확인하면 

```json
// nginx:latest
"GraphDriver": {
    "Data": {
        "LowerDir": "/var/lib/docker/overlay2/3365ae3267b423c3807e23070b9d68c6aeb907291c4e1b3a7029d9e998fa23cd/diff:/var/lib/docker/overlay2/dc2502d1901912453f88751bf1e058db15e4c6f7ef1517454c2787c4f15bcc41/diff",
        "MergedDir": "/var/lib/docker/overlay2/b3670092ad204d3be3e63222eabadd9e16c81d7f8077c200fc9ff142a6ea4754/merged",
        "UpperDir": "/var/lib/docker/overlay2/b3670092ad204d3be3e63222eabadd9e16c81d7f8077c200fc9ff142a6ea4754/diff",
        "WorkDir": "/var/lib/docker/overlay2/b3670092ad204d3be3e63222eabadd9e16c81d7f8077c200fc9ff142a6ea4754/work"
    },
    "Name": "overlay2"
},
```

```json
// mywebserver
"GraphDriver": {
    "Data": {
        "LowerDir": "/var/lib/docker/overlay2/b3670092ad204d3be3e63222eabadd9e16c81d7f8077c200fc9ff142a6ea4754/diff:/var/lib/docker/overlay2/3365ae3267b423c3807e23070b9d68c6aeb907291c4e1b3a7029d9e998fa23cd/diff:/var/lib/docker/overlay2/dc2502d1901912453f88751bf1e058db15e4c6f7ef1517454c2787c4f15bcc41/diff",
        "MergedDir": "/var/lib/docker/overlay2/bff24d6762626ed3eeeb8c7405c450f75fa53920b9bac02422fc84e09c960e16/merged",
        "UpperDir": "/var/lib/docker/overlay2/bff24d6762626ed3eeeb8c7405c450f75fa53920b9bac02422fc84e09c960e16/diff",
        "WorkDir": "/var/lib/docker/overlay2/bff24d6762626ed3eeeb8c7405c450f75fa53920b9bac02422fc84e09c960e16/work"
    },
    "Name": "overlay2"
},
```

`MergedDir, UpperDir, WorkDir`은 도커 이미지 구축을 위한 데이터로 
두 이미지의 내용이 일치하는 부분이 상당히 많다.  

## docker private registry 구축 및 관리  

프라이빗 레지스트리를 구축하기 위한 이미지를 다운받아 컨테이너 생성해보자.  

`main server - 192.168.56.102`에 `private registry`구축하고 다른 서버에서 이에 접근에 올라가 있는 이미지를 다운.  

```
[main server - 192.168.56.102]
$ docker pull registry:2.0
$ docker run --restart=always --name local-registry -d -p 5000:5000 registry:2.0

$ docker ps | grep registry
sudo netstat -nlp | grep 5000
```
`5000`번 포트를 매핑하고 해당 `local-registry` 컨테이너가 동작중이지 확인  

`private registry` 구축끝!  

이제 이미지를 다운받고 싶은 서버에서 우리가 구축한 `private registry`서버를 등록하기만 하면된다.  

메인서버에도 이미지를 `private registry`서버에 올리고 다운받을 수 있도록 설정  

```
[main server - 192.168.56.102]
[another server - 192.168.56.103]
$ sudo vi /etc/init.d/docker
31 ... DOCKER_OPTS=--insecure-registry 192.168.56.102:5000

-- daemon.json은 새로 작성한다.  
$ sudo vi /etc/docker/daemon.json
{"insecure-registries": ["192.168.56.102:5000"]}
```

`sudo service docker restart`, `docker version` 으로 서버가 다시 뜨는지 확인  
`docker info` 명령어로 프라이빗 레지스트리가 추가되었는지 확인, 아래 문구처럼 뜨면 등록완료.

```
[main server - 192.168.56.102]
[another server - 192.168.56.103]
$ docker info
Insecure Registries:
192.168.56.102:5000
127.0.0.0/8
```

이제 main 서버에 이미지 푸쉬, 기존에 만들어 두었던 `cadvisor`이미지를 업로드한다.  
> `docker image tag`명령으로 이미지 명을 따로 줄 필요가 있다. `ip:port/imagename`  

```
[main server - 192.168.56.102]
$ docker image tag google/cadvisor:latest localhost:5000/google-monitoring
localhost:5000/google-monitoring   latest              eb1210707573        12 months ago       69.6MB
$ docker image push localhost:5000/google-monitoring
```

`push`가 끝났으면 `another`서버에서 다운.  

```
[another server - 192.168.56.103]
$ docker image pull 192.168.56.102:5000/google-monitoring
$ docker images
REPOSITORY                              TAG                 IMAGE ID            CREATED             SIZE
192.168.56.102:5000/google-monitoring   latest              eb1210707573        12 months ago       69.6MB
```
이미지명 앞에 `ip:port`를 입력할 필요가 있다.  

### 외부 서버에 private registry 등록  

```
docker run -d \
-p 5000:5000 \
--restart=always \
--name docker-registry \
-v /etc/letsencrypt:/etc/letsencrypt \
-v /data/registry:/var/lib/registry/docker/registry \
-v /data/auth:/auth \
-e "REGISTRY_AUTH=htpasswd" \
-e "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm" \
-e "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd" \
-e "REGISTRY_HTTP_TLS_CERTIFICATE=/etc/letsencrypt/live/<mydomain>/fullchain.pem" \
-e "REGISTRY_HTTP_TLS_KEY=/etc/letsencrypt/live/<mydomain>/privkey.pem" \
registry
```

`private registry` 를 외부에서 사용하려면 `https` 사용이 필수이다.  
때문에 `dns` 설정, `ssl` 인증서 설치가 필요한대 `letsencrypt` 를 사용해 `ssl` 를 설치했다.  

`letsencrypt`의 `pem` 파일들은 심볼릭 링크이기에 실제 파일을 사용하려면 `/etc/letsencrypt`를 모두 볼륨처리 해야한다.  
인증 파일이 저장되는 `/data/auth` 위치 `htpasswd` 를 사용해 계정정보 파일 생성 및 계정정보 입력

```
$ apt install apache2-utils
$ htpasswd -Bn kouzie >> htpasswd
$ htpasswd -Bbn newid newpw > htpasswd
```

### 구글 클라우드플랫폼 사용  

> 구글 id, 카드등록 필요  

> https://cloud.google.com/container-registry/docs/pushing-and-pulling?hl=ko

gcp에서 프로젝트를 하나 생성해보자.  
![docker14](/assets/2019/docker14.png){: .shadow}  

gcp에서 프로젝트를 생성하면 다음과 같이 projectID가 랜덤으로 생성되는데 gcp에 올릴때 도커 이미지 명명규칙으로 저 문자열을 사용해야한다.    

`$ docker tag nginx asia.gcr.io/projectID/imagename`

아시아 서버를 사용할 것이기에 맨 앞엔 아시아 서버 도메인을 사용 맨 뒤엔 사용할 이미지 이름을 사용.  

#### google cloud sdk install

> https://cloud.google.com/sdk/docs/quickstart-debian-ubuntu?hl=ko

gcp에 이미지를 업로드하려면 먼저 `google cloud sdk` 를 설치해야한다.  

리눅스 버전에 따른 `cloud-sdk`버전을 환경변수등록  
```
vi .bashrc
export CLOUD_SDK_REPO="cloud-sdk-$(lsb_release -c -s)"
```

`/etc/apt/sources.list.d/google-cloud-sdk.list` 파일에 `deb http://packages.cloud.google.com/apt cloud-sdk-xenial main` 문자열 삽입 및 
`apt-key` 등록  
```
echo "deb http://packages.cloud.google.com/apt $CLOUD_SDK_REPO main" | sudo tee a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add -
```

`$ sudo apt-get update`
```
$ sudo apt-get update
Hit:1 https://download.docker.com/linux/ubuntu xenial InRelease
Hit:2 https://deb.nodesource.com/node_6.x xenial InRelease
Hit:3 http://kr.archive.ubuntu.com/ubuntu xenial InRelease
Get:4 http://security.ubuntu.com/ubuntu xenial-security InRelease [109 kB]
Get:5 http://packages.cloud.google.com/apt cloud-sdk-xenial InRelease [6,372 B]
Get:6 http://packages.cloud.google.com/apt cloud-sdk-xenial/main amd64 Packages [94.4 kB]
Get:7 http://kr.archive.ubuntu.com/ubuntu xenial-updates InRelease [109 kB]
Get:8 http://kr.archive.ubuntu.com/ubuntu xenial-backports InRelease [107 kB]
Get:9 http://packages.cloud.google.com/apt cloud-sdk-xenial/main i386 Packages [94.5 kB]
```

`$ sudo apt-get install -y google-cloud-sdk`

`gcloud` 명령을 쓸수 있게 되었다!   

이미지를 올리기전 구글 클라우드에 로그인을 해야한다.
`gcloud auth login`  

출력된 url로 들어가 구글계정으로 로그인후 출력되는 `certify`키로 인증을 마친다.  

올리고 싶은 프로젝트로 설정하고 이미지를 push하면 된다.  
```
gcloud config set project project-id
gcloud docker -- push asia.gcr.io/project-id/nginx-cloudtest
```

올렸으면 `anotherserver`에서 pull해보자.(마찬가지로 `gcloud`명령을 쓰기 위해 `apt`키 설정 및 업데이트, `auth`를 모두 진행해야한다)  

`gcloud docker -- pull asia.gcr.io/project-id/nginx-cloudtest`

### maria db 샤딩

`Docker`를 이용한 `MariaDB sharding` 구축 


먼저 `mariadb` 이미지를 설치하자.  
`$ docker pull mariadb:10.1`

설치했으면 해당 이미지로 3개의 컨테이너를 생성. 각각 IP는 잘 기억해두자.  
`spider`, `korea1`, `korea2` 으로 컨테이너 이름을 설정,  
`spider`가 메인 db서버, `korea1`, `korea2`는 slave서버이다.  

```
$ docker run -d -e MYSQL_ROOT_PASSWORD=koreapass --name=spider mariadb:10.1

$ docker inspect spider | grep "IPAddress"
  "SecondaryIPAddresses": null,
  "IPAddress": "172.17.0.2",
          "IPAddress": "172.17.0.2",

$ docker exec -it spider bash

# mysql -u root -p < /usr/share/mysql/install_spider.sql
# mysql -uroot -p

MariaDB [(none)]> show engines\G;
*************************** 1. row ***************************
      Engine: SPIDER
     Support: YES
     Comment: Spider storage engine
Transactions: YES
          XA: YES
  Savepoints: NO
```

이작업을 2개의 컨테이너를 새로 만들고 똑같이 수행한다.  
단 새로 만들 2개의 컨테이너명을 `spider`가 아닌 `korea1`, `korea2`로 지정  

```
docker run -d -e MYSQL_ROOT_PASSWORD=koreapass --name=korea1 mariadb:10.1
docker run -d -e MYSQL_ROOT_PASSWORD=koreapass --name=korea2 mariadb:10.1

$ docker inspect korea1 | grep "IPAddress"
  "SecondaryIPAddresses": null,
  "IPAddress": "172.17.0.3",
          "IPAddress": "172.17.0.3",
$ docker inspect korea2 | grep "IPAddress"
  "SecondaryIPAddresses": null,
  "IPAddress": "172.17.0.4",
          "IPAddress": "172.17.0.4",
```

IP확인 후 각각 들어가서 intall_spider.sql를 실행시킨다.  

모든 컨테이너에서 다음과 같이 출력되면 설정끝

```
MariaDB [(none)]> show engines;
+--------------------+---------+--------------------------------------------------------------------------------------------------+--------------+------+------------+
| Engine             | Support | Comment                                                                                          | Transactions | XA   | Savepoints |
+--------------------+---------+--------------------------------------------------------------------------------------------------+--------------+------+------------+
| SPIDER             | YES     | Spider storage engine                                                                            | YES          | YES  | NO         |
| MRG_MyISAM         | YES     | Collection of identical MyISAM tables                                                            | NO           | NO   | NO         |
| CSV                | YES     | Stores tables as CSV files                                                                       | NO           | NO   | NO         |
| MEMORY             | YES     | Hash based, stored in memory, useful for temporary tables                                        | NO           | NO   | NO         |
| MyISAM             | YES     | Non-transactional engine with good performance and small data footprint                          | NO           | NO   | NO         |
| SEQUENCE           | YES     | Generated tables filled with sequential values                                                   | YES          | NO   | YES        |
| Aria               | YES     | Crash-safe tables with MyISAM heritage                                                           | NO           | NO   | NO         |
| PERFORMANCE_SCHEMA | YES     | Performance Schema                                                                               | NO           | NO   | NO         |
| InnoDB             | DEFAULT | Percona-XtraDB, Supports transactions, row-level locking, foreign keys and encryption for tables | YES          | YES  | YES        |
+--------------------+---------+--------------------------------------------------------------------------------------------------+--------------+------+------------+
```


이제 `spider`, `korea1`, `korea2` 컨테이너의 db에 계정 생성 및 권한을 할당한다.  
그리고 `koreaDB`라는 데이터베이스를 생성  
```
MariaDB [(none)]> use mysql;
MariaDB [mysql]> create user 'spider-user'@'%' identified by 'spiderpass';
MariaDB [mysql]> grant all on *.* to 'spider-user'@'%' with grant option;
MariaDB [mysql]> flush privileges;
MariaDB [(none)]> create database koreaDB;
MariaDB [(koreaDB)]> 
```

모든 컨테이너에 사용자와 database가 생성되었으면 
master서버인 `spider`에서 slave 서버인 `korea1`, `korea2`를 등록하자.  

```
-- spider의 koreaDB --
MariaDB [(koreaDB)]> create server korea1
  foreign data wrapper mysql
  options(
  host '172.17.0.3',
  database 'koreaDB',
  user 'spider-user',
  password 'spiderpass',
  port 3306
  );

Query OK, 0 rows affected (0.00 sec)

MariaDB [(koreaDB)]> create server korea2
  foreign data wrapper mysql
  options(
  host '172.17.0.4',
  database 'koreaDB',
  user 'spider-user',
  password 'spiderpass',
  port 3306
  );
MariaDB [(koreaDB)]> select * from mysql.servers;
+-------------+------------+---------+-------------+------------+------+--------+---------+-------+
| Server_name | Host       | Db      | Username    | Password   | Port | Socket | Wrapper | Owner |
+-------------+------------+---------+-------------+------------+------+--------+---------+-------+
| korea1      | 172.17.0.3 | koreaDB | spider-user | spiderpass | 3306 |        | mysql   |       |
| korea2      | 172.17.0.4 | koreaDB | spider-user | spiderpass | 3306 |        | mysql   |       |
+-------------+------------+---------+-------------+------------+------+--------+---------+-------+
```

그리고 공유할 테이블인 `shard_table`을 생성 
```
-- spider의 koreaDB --
MariaDB [mysql]> use koreaDB;
MariaDB [koreaDB]> create table shard_table
  (id int not null auto_increment
  , name varchar(255) not null
  , address varchar(255) not null
  , primary key(id))
  engine=spider comment='wrapper "mysql", table "shard_table"'
  partition by key(id)
  ( partition korea1 comment = 'srv "korea1"'
  , partition korea2 comment = 'srv "korea2"' );

MariaDB [koreaDB]> FLUSH TABLES;
```

마스터 서버에서 공유할 테이블을 만들고 slave서버설정까지 끝났으면

slave서버들이 마스터 서버의 table을 같이 사용할 수 있게 테이블을 생성, `korea1`, `korea2`에서 각각 설정한다.  

```
-- korea1, korea2 의 koreaDB --
MariaDB [(none)]> use koreaDB;
MariaDB [koreaDB]> create table shard_table
  (
  id int not null auto_increment,
  name varchar(255) not null,
  address varchar(255) not null,
  primary key(id)
  );
MariaDB [koreaDB]> FLUSH TABLES;
```

slave에서 db 테이블 생성이 모두 끝났으면 
`masger`서버에서 `insert` 쿼리를 여러번 수행 

```
-- spider의 koreaDB --
insert into shard_table(name, address) values ('kim', 'seoul');
insert into shard_table(name, address) values ('lee', 'seoul');
insert into shard_table(name, address) values ('park', 'seoul');
insert into shard_table(name, address) values ('kim', 'busan');
insert into shard_table(name, address) values ('lee', 'daegu');
insert into shard_table(name, address) values ('park', 'jeju');
```

그리고 spider, korea1, korea2 서버의 DB에 접속해 `select * from shard_table;` 명령 수행  

```
-- spider koreaDB --
MariaDB [koreaDB]> select * from shard_table;
+----+------+---------+
| id | name | address |
+----+------+---------+
|  1 | kim  | seoul   |
|  3 | park | seoul   |
|  5 | lee  | daegu   |
|  7 | kim  | seoul   |
|  9 | park | seoul   |
|  2 | lee  | seoul   |
|  4 | kim  | busan   |
|  6 | park | jeju    |
|  8 | lee  | seoul   |
| 10 | kim  | busan   |
+----+------+---------+
36 rows in set (0.01 sec)
```

```
-- korea1 koreaDB --
MariaDB [koreaDB]> select * from shard_table;
+----+------+---------+
| id | name | address |
+----+------+---------+
|  1 | kim  | seoul   |
|  3 | park | seoul   |
|  5 | lee  | daegu   |
|  7 | kim  | seoul   |
|  9 | park | seoul   |
+----+------+---------+
18 rows in set (0.00 sec)
```

```
-- korea2 koreaDB --
MariaDB [koreaDB]> select * from shard_table;
+----+------+---------+
| id | name | address |
+----+------+---------+
|  2 | lee  | seoul   |
|  4 | kim  | busan   |
|  6 | park | jeju    |
|  8 | lee  | seoul   |
| 10 | kim  | busan   |
+----+------+---------+
18 rows in set (0.00 sec)
```


# docker-compose

지금까지 도커와의 링크를 위해 네트워크, 볼륨, 혹은 `--link` 속성으로 컨테이너간의 연결을 진행하였는데  
`docker-compose` 혹은 `dockerfile`을 사용하면 복잡한 명령어를 파일단위로 관리할 수 있다.  

`docker-compose`는 일종의 툴로 `docker-compose.yaml` 파일을 사용해 여러개의 컨테이너를 한번에 생성할 수 있다.  

`Dockerfile` 로 이미지를 생성하고  
`docker-compose` 로 생성한 이미지를 컨테이너화 시킨다.  

컨테이너화 할때 사양한 설정 명령을 삭성해야 하며 `docker run` 명령이 10줄이 넘어갈 수 있다.  
`docker-compose`는 단순 명령 실행의 떨어지는 가독성을 보완하고 1개의 컨테이너만 생성하는 것이 아니라 여러개의 컨테이너를 연관지어 한꺼번에 생성 가능하게해준다.  


## docker compose 설치 및 운영

`apt-get install`로 설치가능하지만 버전에 따른 오류가 많기에 아래 명령으로 실행

https://github.com/docker/compose/releases?after=1.23.1

위 사이트에서 docker가 제공해준 실행파일을 다운 받은 후 권한을 설정한다.  

```
$ sudo curl -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
$ chmod +x /usr/local/bin/docker-compose
$ sudo chown kouzie /usr/local/bin/docker-compose
```

그후 `vi`로 `docker-compose.yml`파일 생성  

샘플로 wordpress와 wordpress가 사용할 mysqlDB를 생성해보자.  
```
version: '2'
services:
    db:
        image: mysql:5.7
        volumes:
            - ./mysql:/var/lib/mysql
        restart: always
        environment:
            MYSQL_ROOT_PASSWORD: wordpress
            MYSQL_DATABASE: wordpress
            MYSQL_USER: wordpress
            MYSLQ_PASSWORD: wordpress
    wordpress:
        image: wordpress:latest
        volumes:
            - ./wp:/var/www/html
        ports:
            - "8080:80"
        restart: always
        environment:
            WORDPRESS_DB_HOST: db:3306
            WORDPRESS_DB_PASSWORD: wordpress
```

`docker-compose up`명령으로 `docker-compose.yml`파일 실행  
```
$ docker-compose up
Starting kouzie_wordpress_1 ... done
Starting kouzie_db_1        ... done
```
실행과 동시에 출력되는 로그가 계속 뜨는데 도중에 나갈 수 없다.  
시작할때 백그라운드로 실행해야 한다.  

`$ docker-compose up -d` : 백그라운드로 실행  

묶음으로 실행되기 때문에 해당 컨테이너들끼리 사용하는 브릿지 네트워크가 자동으로 생기는데 파일이 존재하는 **디렉터리 명**으로 생성된다.  
`kouzie` 홈디렉토리에서 `docker-compose.yml`을 생성해 실행했기 때문에 `kouzie_default` 라는 브릿지 네트워크가 생겼다.  

그리고 컨테이너 명도 별도로 지정하지않아 db와 wordpress앞에 디렉터리명이 붙어 생성된다.  

```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                  NAMES
2e7c11994ccc        wordpress:latest    "docker-entrypoint.s…"   10 minutes ago      Up 23 seconds       0.0.0.0:8080->80/tcp   kouzie_wordpress_1
f4fe464220e4        mysql:5.7           "docker-entrypoint.s…"   10 minutes ago      Up 23 seconds       3306/tcp, 33060/tcp    kouzie_db_1
```


> https://github.com/asashiho/dockertext2/tree/master/chap07

이번엔 redis와 파이썬 웹서버를 묶어사용하는 `docker-compose.yml`을 생성 
```
version: '3.3'
services:
  # WebServer config
  webserver:
    build: .
    ports:
     - "80:80"
    depends_on:
     - redis

  # Redis config
  redis:
    image: redis:4.0
```

webserver의 경우 `build: .` 이 적혀있는데 이는 같은 디렉토리에 있는 `Dockerfile`을 빌드해서 생성한 컨테이너를 사용하겠다는 뜻.  

```
# Base Image
FROM python:3.6

# Maintainer
LABEL maintainer "Shiho ASA"

# Upgrade pip
RUN pip install --upgrade pip

# Install Path
ENV APP_PATH /opt/imageview

# Install Python modules needed by the Python app
COPY requirements.txt $APP_PATH/
RUN pip install --no-cache-dir -r $APP_PATH/requirements.txt

# Copy files required for the app to run
COPY app.py $APP_PATH/
COPY templates/ $APP_PATH/templates/
COPY static/ $APP_PATH/static/

# Port number the container should expose
EXPOSE 80

# Run the application
CMD ["python", "/opt/imageview/app.py"]
```

`Dockerfile`에는 각종 파일을 복사하고 `app.py`코드르 실행하는 스크립트가 작성되어있다.  

`docker-compose up` 실행, 실행하는 주소로 접속해보면 그림처럼 출력된다.  

![docker13](/assets/2019/docker13.png){: .shadow}  

## docker-compose 상태 확인  

`docker-compose ps` 명령으로 컨테이너 동작 상황을 알 수 있다.  
단 `docker-compose.yml`파일이 있는 위치에서 실행해야한다.  

```
$ docker-compose ps
       Name                     Command               State         Ports
--------------------------------------------------------------------------------
chap07_redis_1       docker-entrypoint.sh redis ...   Up      6379/tcp
chap07_webserver_1   python /opt/imageview/app.py     Up      0.0.0.0:80->80/tcp
```

그 외에도 각종 명령어 들이 존재  

`docker-compose port webserver 80` (80번포트가 어떤 외부포트와 매핑되어있는지 출력)
`docker-compose config` (`docker-compose.yml`파일 안의 정보 출력)
`docker-compose kill -s SIGINT` (kill -s 9 하면 서버를 종료시킨다)
`docker-compose rm` (컨테이너 삭제)
`docker-compose down` (kill -s 9 and rm)

`down`후에 `ps`로 한번 모든 컨테이너가 종료되었는지 확인해보자.  


# docker swarm

도커가 공식적으로 만든 `Orchestration tool`(도커 관리도구)

여러개의 도커 컨테이너를 모니터링, 실행, 종료시킬 수 있다.  

도커 스웜은 서비스의 안정성을 높이기 위해 사용된다.  
이러한 역할로 대표적인 툴인 쿠버네티스가 있지만 도커 스웜도 여러기능을 가지고 있다.  

3개의 `hostOS`를 구성하고 각 시스템에서 같은 기능의 서비스를 여어랙 호출해 관리해보자.  


먼저 메인 서버에서 각 도커 스웜을 관리하는 스웜 매니저를 설치  
그리고 스웜 워커역할을 할 우분투 총 2대를 설치 


보기 편하도록 `hostname`을 변경

`sudo hostnamectl set-hostname swarm-manager`  
`sudo hostnamectl set-hostname swarm-worker1`  
`sudo hostnamectl set-hostname swram-worker2`  

```
swarm-manager
192.168.56.101

swarm-worker1
192.168.56.111

swarm-worker2
192.168.56.112
```

스웜 매니저를 `192.168.56.101`에 설치  

`docker swarm init --advertise-addr 192.168.56.101`

아래와 같은 문구가 출력된다.

```
Swarm initialized: current node (1gsa9qe7zesj6y8zsdlp2ql6k) is now a manager.
To add a worker to this swarm, run the following command:
    docker swarm join --token SWMTKN-1-3kjflwhbxca586emsjjqszn4lbkek3x61p10ckd7y8qyewqw5y-9vb62ynzudemnqussm5149bnv 192.168.56.101:2377
To add a manager to this swarm, run 'docker swarm join-token manager' and follow the instructions.
```

스웜 매니저가 생성되었고 스웜 워커를 등록하려면 join와 토큰을 사용하라고한다.  

만약 토큰이 지워지거나 잊어먹었을 경우 다시 보거나 생성할 수 있다.  

`docker swarm join-token manager` - 기존의 토근을 출력   
`docker swarm join-token --rotate manager` - 새로운 토큰을 생성 및 출력   

우선 스웜매니저를 비주얼적으로 볼 수 있도록 아래 `visualizer`를 설치
```
$ docker service create \
--name=viz_swarm \
--publish=8080:8080 \
--constraint=node.role==manager \
--mount=type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
dockersamples/visualizer
```

스웜 매니저와 모니터링 서비스가 모두 설치 되었으면  
각 hostOS(swarm-worker1, 2) 에서 위에 출력문구에 적힌 명령어를 수행하자 

`docker swarm join --token SWMTKN-1-3kjflwhbxca586emsjjqszn4lbkek3x61p10ckd7y8qyewqw5y-9vb62ynzudemnqussm5149bnv 192.168.56.101:2377`


그리고 `swarm-manager`에서 우분투 서비스 하나를 실행, 2초마다 `echo`명령을 수행하는 ubuntu 컨테이너

> 도커 스웜에선 가상 컨테이너를 서비스라 한다. 

```
$ docker service create \
ubuntu:14.04 \
/bin/sh -c "while true; do echo hello world; sleep 2; done"
```

![dockercompose1](/assets/2019/dockercompose1.png){: .shadow}  


각 `swarm-worker`와 `swarm-manager`위에서 동작하는 ubuntu서비스와 모니터링 서비스가 동작중임을 알 수 있다.  


스웜 매니저에서  `docker info` 명령을 수행  
동작중인 컨테이너와 노드 확인 가능  

```
Client:
 Debug Mode: false

Server:
 Containers: 2
  ...
 Plugins:
  ...
 Swarm: active
  ...
  Managers: 1
  Nodes: 2
```


웹서버 서비스를 동작시키자.  
`--replicas` 속성은 최소 컨테이너 실행 개수로 스웜 매니저는 서비스 실행 컨테이너 수가 2 미만으로 떨어지지 않도록 유지한다.  

```
docker service create --name myweb \
--replicas 2 \
-p 80:80 \
nginx:1.10
```

![dockercompose2](/assets/2019/dockercompose2.png){: .shadow}  

실행은 `swarm-manager`에서 했지만 실제 동작은 `worker`에서 동작된다.  
컨테이너 수에 맞게 각각 하나씩 실행된다.  

서비스 유지 개수를 2개에서 5개로 늘려보자.  
`$ docker service scale myweb=5`

![dockercompose3](/assets/2019/dockercompose3.png){: .shadow}  

myweb을 5개 띄우게 되면 기존 2개에 3개가 추가로 더 가동된다.  

> 거꾸로 스케일을 낮추면 기존의 서비스가 종료되며 줄어들음.  

스웜 매니저, 워커의 주소에서 모두 웹서버에 접속 가능한지 확인해보자.  


새로운 서비스를 각 스웜에 하나씩 동작시키고 싶다면 `--mode global`속성을 붙여 사용하자.  

```
docker service create --name global_myweb \
--mode global \
nginx
```
기존 myweb 5개에 global_myweb이 3개 추가로 실행되었다.  
![dockercompose4](/assets/2019/dockercompose4.png){: .shadow}  

(`docker service rm global_myweb` 으로 다시 삭제하자 )


위에서 이미 myweb서비스의 scale을 최소 5개로 설정하였는데 이상황에서 `swarm-worker1`을 멈춰보자.  

자동으로 하나씩 추가된다.   
![dockercompose5](/assets/2019/dockercompose5.png){: .shadow}  

## docker swarm - Rolling update  

만약 기존의 서비스가 오류가 발생해 업데이트를 해야할 때 
`Rolling update`를 사용하자  

기존에 `myweb` 서비스를 생성할 때 `nginx:1.10` 버전을 설치했는데 `1.11`버전으로 업그레이드 해야 한다면  

`$ docker service update --image nginx:1.11 myweb`

순차적으로 update된다....

<!-- 
### docker swarm - inspect  

서비스의 상세정보를 보고 싶다면 `inspect`를 사용 

`$ docker service inspect --pretty myweb`

```
ID:             6tmnfueng4wjdpti2xx0k5ha4
Name:           myweb
Service Mode:   Replicated
 Replicas:      2
UpdateStatus:
 State:         completed
 Started:       About a minute ago
 Completed:     About a minute ago
 Message:       update completed
Placement:
UpdateConfig:
 Parallelism:   1
 On failure:    pause
 Monitoring Period: 5s
 Max failure ratio: 0
 Update order:      stop-first
RollbackConfig:
 Parallelism:   1
 On failure:    pause
 Monitoring Period: 5s
 Max failure ratio: 0
 Rollback order:    stop-first
ContainerSpec:
 Image:         nginx:1.11@sha256:e6693c20186f837fc393390135d8a598a96a833917917789d63766cab6c59582
 Init:          false
Resources:
Endpoint Mode:  vip
Ports:
 PublishedPort = 80
  Protocol = tcp
  TargetPort = 80
  PublishMode = ingress
```

```
$ docker network ls

NETWORK ID          NAME                DRIVER              SCOPE
e955360760d9        bridge              bridge              local
47a3f4ceeb20        docker_gwbridge     bridge              local
65a9d343d6ef        host                host                local
z3shct78ok58        ingress             overlay             swarm
2fc7c50757eb        none                null                local
``` -->

<!-- 
### 엘라스틱 서치

mariadb3개를 운용하며 헷갈리거나 잘못 설정할 수 있는데 이를 묶어서 관리할 수 있는 docker-compose.yml파일로 컨테이너들을 묶어서 관리할 수 있도록 설정한다.  

sudo sysctl -w vm.max_map_count=262144
sudo service docker restart


[ElasticSearch 공식 문서 참고]
- 구성은 master-node 1대, data-node 2대, kibana 1대
- elasticsearch:6.5.3 (centos7 기반)★, 
- elasticsearch:6.7.1 (Ubuntu16 기반) 선택★★★


kouzie@kouzie-VirtualBox:~$ mkdir elk_cluster
kouzie@kouzie-VirtualBox:~$ cd elk_cluster/
kouzie@kouzie-VirtualBox:~/elk_cluster$ vi docker-compose.yml
version: '2.2'
services:
  elasticsearch:
    container_name: elk_m
    image: elasticsearch:6.5.3
    environment:
      - cluster.name=LDCC-cluster
      - node.name=master-node1
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es1:/usr/share/elasticsearch/data
    ports:
      - 9200:9200
      - 9300:9300
    networks:
      - esnet
    stdin_open: true
    tty: true
  elasticsearch1:
    container_name: elk_d1
    image: elasticsearch:6.5.3
    environment:
      - cluster.name=LDCC-cluster
      - node.name=data-node1
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - "discovery.zen.ping.unicast.hosts=elasticsearch"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es2:/usr/share/elasticsearch/data
    ports:
      - 9301:9300
    networks:
      - esnet
    stdin_open: true
    tty: true
    depends_on:
      - elasticsearch
  elasticsearch2:
    container_name: elk_d2
    image: elasticsearch:6.5.3
    environment:
      - cluster.name=LDCC-cluster
      - node.name=data-node2
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - "discovery.zen.ping.unicast.hosts=elasticsearch1"
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es3:/usr/share/elasticsearch/data
    ports:
      - 9302:9300
    networks:
      - esnet
    stdin_open: true
    tty: true
    depends_on:
      - elasticsearch
  kibana:
    container_name: kibana
    image: kibana:6.5.3
    ports:
      - 5601:5601
    networks:
      - esnet
    depends_on:
      - elasticsearch
      - elasticsearch1
      - elasticsearch2
volumes:
  es1:
    driver: local
  es2:
    driver: local
  es3:
    driver: local
networks:
  esnet:

복붙
> 주의 : vmware용량이 최소 4기가는 필요하다  



-- 설치
$ docker-compose up -d

-- 정지 및 삭제
$ docker-compose down
Stopping kibana         ... done
Stopping elasticsearch2 ... done
Stopping elasticsearch  ... done
Removing kibana         ... done
Removing elasticsearch2 ... done
Removing elasticsearch  ... done
Removing network elasticsearch_esnet


docker-compose ps
 Name               Command                State             Ports
---------------------------------------------------------------------------
elk_d1   /usr/local/bin/docker-entr ...   Exit 1
elk_d2   /usr/local/bin/docker-entr ...   Exit 1
elk_m    /usr/local/bin/docker-entr ...   Exit 137
kibana   /usr/local/bin/kibana-docker     Up         0.0.0.0:5601->5601/tcp