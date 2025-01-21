---
title:  "도커 - Dockerfile, docker-compose!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---

## Dockerfile

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

### Dokcerfile 명령어  

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

```sh
docker run -it sample ## CMD 의 인자를 그대로 사용, 10초 간격 갱신
docker run -it sample -d 2 ## CMD 의 인자 생략, 2초 간격 갱신    
```

`FROM`만 필수항목이고 나머지는 모두 없어도 된다.  
(나머지 속성은 모두 기본이미지에 살을 붙이는 속성)    

`FROM` 명령만 적어 `Dockerfile`을 생성해보자.  
`ubuntu` 컨테이너에 `nginx`를 설치하고 `index.html`파일을 삽인한 후 다시 이미지화.  

```dockerfile
## Dockerfile.base
## Dockerfile 에선 주석처리를 ## 을 이용
## ubuntu 컨테이너 생성
FROM ubuntu:16.04

## 명령어 실행
RUN apt-get update && apt-get install -y -q nginx

## 현재 디렉토리에 index.html을 해당 경로에 복사
COPY index.html /usr/share/nginx/html

## 데몬 실행 daemon off는 포그라운드로 실행 
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

#### union file system

위의 출력값을 통해 도커는 `union file system`을 사용함을 알 수 있다.  
계층화된 파일 시스템을 사용해 중복 데이터를 피해 효율적인 이미지 관리를 할 수 있다.  
(시간 단축, 데이터 효율!)

`docker images`로 확인해 보면 기존에 생성했던 `ningx` 이미지를 사용해 새로만든 `mywebserver`의 이미지를 생성한다.  

```
REPOSITORY                TAG                 IMAGE ID            CREATED             SIZE
mywebserver               latest              fc4c50b45ccd        26 hours ago        126MB
nginx                     latest              540a289bab6c        4 days ago          126MB
```

```
docker run --name newNginx -d -p 80:80 mywebserver
```

위 명령어로 해당 이미지를 실행시키고 위에서 정의한 `index.html` 파일이 `/usr/share/nginx/html` 디렉터리에 들어있는지 확인  

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

### arm docker build

apple silicon 에서 docker build 시 각별한 주의가 필요하다.  

apple silicon 에서 빌드한 이미지는 arm 시스템에서 동작하는 Docker 이미지가 생성되고  
일반 amd CPU 를 사용하는 시스템에서 해당 이미지를 실행하면 아래와 같은 오류가 발생한다.  

`exec format error`

빌드시 아래와 같이 amd 아키텍처에서 실행할 것임을 명시해야한다.  

```sh
docker build --platform linux/amd64 -t docker-test .
```

### not certified ssl 등록

공식 Docker Registry 가 아닌 사설 SSL 인증서가 적용된 Private Registry 지정시 아래와 같은 오류가 발생할 수 있다.  
`https` 를 사용하더라도 공식 SSL 인증서가 아니라면 아래 에러가 docker login, pull, push 할 때 마다 발생한다.  

```
"SSL certificate problem: self signed certificate in certificate chain"
```

아래 파일에서 `insecure-registries` 속성을 추가해서 해결 가능하다.  

- ubuntu: `/etc/docker/daemon.json`  
- mac: `/Users/{username}/.docker/daemon.json`  

```json
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

### 멀티스테이지 빌드(Multi-Stage Build)

Spring Boot 서버를 실행하는 Docker 이미지를 만드려면 jar 파일을 COPY 하여 실행시키는 Dockerfile 을 작성한다.  
jar 파일을 만들기 위해 host 에는 JDK 가 설치되어 있어야 하는데, jar 를 만드는 과정까지 Docker 컨테이너에서 수행할 수 있다.  

```
# 빌드 단계
FROM gradle:8.8-jdk17 AS build
WORKDIR /app

# 프로젝트 소스 복사
COPY . .

# Gradle 빌드 실행
RUN gradle build --no-daemon

# ------------------------------

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

```

## docker-compose

지금까지 도커와의 링크를 위해 네트워크, 볼륨, 혹은 `--link` 속성으로 컨테이너간의 연결을 진행하였는데  
`docker-compose` 혹은 `dockerfile`을 사용하면 복잡한 명령어를 파일단위로 관리할 수 있다.  

`docker-compose`는 일종의 툴로 `docker-compose.yaml` 파일을 사용해 여러개의 컨테이너를 한번에 생성할 수 있다.  

`Dockerfile` 로 이미지를 생성하고  
`docker-compose` 로 생성한 이미지를 컨테이너화 시킨다.  

컨테이너화 할때 사양한 설정 명령을 삭성해야 하며 `docker run` 명령이 10줄이 넘어갈 수 있다.  
`docker-compose`는 단순 명령 실행의 떨어지는 가독성을 보완하고 1개의 컨테이너만 생성하는 것이 아니라 여러개의 컨테이너를 연관지어 한꺼번에 생성 가능하게해준다.  

### docker compose 설치 및 운영

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
  ## WebServer config
  webserver:
    build: .
    ports:
     - "80:80"
    depends_on:
     - redis

  ## Redis config
  redis:
    image: redis:4.0
```

webserver의 경우 `build: .` 이 적혀있는데 이는 같은 디렉토리에 있는 `Dockerfile`을 빌드해서 생성한 컨테이너를 사용하겠다는 뜻.  

```
## Base Image
FROM python:3.6

## Maintainer
LABEL maintainer "Shiho ASA"

## Upgrade pip
RUN pip install --upgrade pip

## Install Path
ENV APP_PATH /opt/imageview

## Install Python modules needed by the Python app
COPY requirements.txt $APP_PATH/
RUN pip install --no-cache-dir -r $APP_PATH/requirements.txt

## Copy files required for the app to run
COPY app.py $APP_PATH/
COPY templates/ $APP_PATH/templates/
COPY static/ $APP_PATH/static/

## Port number the container should expose
EXPOSE 80

## Run the application
CMD ["python", "/opt/imageview/app.py"]
```

`Dockerfile`에는 각종 파일을 복사하고 `app.py`코드르 실행하는 스크립트가 작성되어있다.  

`docker-compose up` 실행, 실행하는 주소로 접속해보면 그림처럼 출력된다.  

![docker13](/assets/2019/docker13.png){: .shadow}  

### docker-compose 상태 확인  

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

### docker-compose 앵커

yaml 설정 중복을 줄이고 설정을 재사용하기 위해 사용

```yaml
# 앵커이름 및 내부에 사용할 yaml 설정 지정
x-test-logging: &default_logging
  driver: "json-file"
  options:
    max-size: "10m" # docker 로그 최대 사이즈
    max-file: "3"   # docker 로그 최대 개수

services:
  demo-log-app:
    image: alpine
    command: sh -c "trap 'exit' SIGTERM; while true; do echo 'demo log'; sleep 2; done"
    logging: *default_logging
```

`병합 키(<<)` 문법을 사용하여 속성명부터 재활용할 수 있다.  

```yaml
x-test-logging: &default_logging
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"


x-common-settings: &common
  image: alpine
  logging: *default_logging

services:
  demo-log-app1:
    <<: *common
    command: sh -c "trap 'exit' SIGTERM; while true; do echo 'demo log1'; sleep 2; done"
  demo-log-app2:
    <<: *common
    command: sh -c "trap 'exit' SIGTERM; while true; do echo 'demo log2'; sleep 5; done"
```

<!-- 
## docker swarm

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

### docker swarm - Rolling update  

만약 기존의 서비스가 오류가 발생해 업데이트를 해야할 때 
`Rolling update`를 사용하자  

기존에 `myweb` 서비스를 생성할 때 `nginx:1.10` 버전을 설치했는데 `1.11`버전으로 업그레이드 해야 한다면  

`$ docker service update --image nginx:1.11 myweb`

순차적으로 update된다....
 -->