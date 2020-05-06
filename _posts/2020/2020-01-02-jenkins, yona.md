---
title:  "jenkins, yona!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - 잡담

tags:
  - 

toc: true
toc_sticky: true

---

## jenkins

작업 결과물을 빌드하고 배포하는 과정은 아래와 같다.  

1. 개인 PC에서의 코드작성, 빌드, 테스트  
2. 배포를 위한 `war` 혹은 `jar`파일 생성  
3. 서비스 제공 서버로 `war/jar`파일 전송  
4. 서버에서 전달받은 `war/jar`파일 실행  

매우 귀찮은 `2~4` 까지의 과정을 `jenkins`라는 자동 빌드/배포 툴을 사용하면 생략가능하다.  

### jenkins 설치

도커를 사용해 `jenkins`를 설치한다.  

```
$ docker pull jenkins/jenkins
$ docker run -d -p 8080:8080 -p 50000:50000 --name myjenkins jenkins/jenkins
```

> 주의 사항: https://hub.docker.com/r/jenkins/jenkins/   
> 그냥 `docker pull jenkins` 실행시 더이상 업데이트 되지 않는 구 버전의 `jenkins`가 설치됨으로 위의 `docker pull jenkins/jenkins`명령을 실행필요.    


다음과 같이 `docker`가 실행중인지 확인 후  

![jenkins1](/assets/2019/jenkins1.png)   

```
$ docker exec -it -u root myjenkins /bin/bash
$ cat /var/jenkins_home/secrets/initialAdminPassword
```

도커의 기본 서비스 포트는 `8080`으로 접속하면 비밀번호를 입력하라 한다.  
위의 `initialAdminPassword` 파일에서 비밀번호를 찾아 입력후 모든 `plugin install`.   


### jenkins, git 연동

#### 기본 설정 - 플러그인 설치, 인증서 관리  

우리의 목적은 자동 빌드 및 배포이기 때문에 개발자가 git에 수정된 코드를 commit하면 
`jenkins`는 **webhook**을 통해 이를 감시하다 수정된 코드를 깃으로 부터 다운받아 maven으로 build후  
docker안에서 생성된 배포파일을 밖의 실사용 서버로 전송하는 작업을 수행시키도록 해야한다.  

추가적으로 필요한 아래 플러그인을 설치한다. `jenkins관리 -> 플러그인 관리`

- `Deploy to container`  
- `Maven Integration`  
- `SSH`  
- `Publish Over SSH`  
- `GitLab`  


> 주의사항: 만약 허가되지 않은 인증서(사내 서버 등)의 경우 `jenkins`에서 인증서 확인을 무시하도록 설정해야 한다.  
`JENKINS_HOME` 환경변수 위치에 `.gitconfig` 파일을 생성   
```
vi /var/jenkins_home/.gitconfig

[http]
sslVerify=false
```
젠킨스 환경변수 값은 우측과 같다.  `JENKINS_HOME=/var/jenkins_home`  

#### ssh 설정  

우리는 도커로 `jenkins`를 설치하였기 때문에 ssh를 통해서 실서버(host)로 젠킨스 도커(container)에서 빌드한 결과물을 전달해야 한다.  
도커안의 컴파일된 `war`, `jar`파일을 밖의 실사용 서버로 옮길때 ssh를 사용해 이동시킨다.  

jenkins설정에서 ssh를 등록하면 빌드 결과물 뿐만 아니라 기존 데이터를 백업하는 등의 작업을 원격명령을 통해 쉽게 처리할 수 있다.  

`jenkins관리 -> 시스템 관리`에서 다음과 같이 ssh 서버 설정  

![jenkinsssh](/assets/2019/jenkins_ssh.png){: .shadow}  

id/pw로 접속이 가능하고 Remote Directory는 원격서버의 기본 디렉토리 위치를 지정할 수 있다. 

## jenkins item 추가

이제 Git에 설정한 프로젝트를 기준으로 자동 빌드할 item만 추가하면 된다.  

좌측의 새로운 item 선택후 이름 설정  
![jenkins_add1](/assets/2019/jenkins_add1.png){: .shadow}  

general부분은 굳이 적을 필요없다(단순 설명)  
![jenkins_add2](/assets/2019/jenkins_add2.png){: .shadow}  

소스코드 관리에서 깃허브 주소와 계정(id와 pw가 설정된) 정보를 지정하고 진행하면 된다.  

전 여기서 계속 ssh, 안증서 관련에러가 발생했는데 위에 작성은 `/var/jenkins_home/.gitconfig`파일에 위에 작성한대로 쓰기후 jenkins를 재실행하면 됩니다.(사내 깃과 같은 인증서를 CA-인증기관 에서 인증받지 않았다면 발생)  

젠킨스 재실행 명령은 웹브라우저에서 `http://localhost:8080/restart` 처럼 주소 바로 뒤에 `/restart` 입력  


![jenkins_add3](/assets/2019/jenkins_add3.png){: .shadow}  

![jenkins_add4](/assets/2019/jenkins_add4.png){: .shadow}  

![jenkins_add5](/assets/2019/jenkins_add5.png){: .shadow}  


## yona

네이버에서 개발한 코드 + 이슈 관리 시스템,

docker로 간단히 설치가능하다.  

> https://github.com/pokev25/docker-yona

`git clone https://github.com/pokev25/docker-yona`명령을 통해 `docker-compose.yml`, `Dockerfile`을 다운받는다.  

> `docker-compose` 설치는 아래 사이트 확인  
> https://kouzie.github.io/docker/docker-Dockerfile,-docker-compose/#docker-compose

만약 서버에서 사용하고 있는 DB가 `mariaDB` 이면 DB를 별도로 설치할 필요 없지만 만약 다른 db를 이미 시스템에서 사용중이라면 docker로 `mariaDB` 컨테이너를 별도로 실행하자.  

> 현재 `yona`에서 공식적으로 지원하는 db는 `mariadb:10.2` 버전이다, `mysql`을 사용하고 싶어 `mysql jar`파일을 다운받아 실행시켰지만 실행하는 sql문에서 버전에 따른 `syntex`에러가 발생함으로 다른 db를 사용하고 싶다면 소스를 다운받아 별도로 커스텀해야 한다, 우리는 도커를 사용해야 함으로 `mariadb`를 사용하자.  

docker-compose.yml을 살짝 번경하자.  

```yml
version: '3'

services:
  yona_db:
    image: mariadb:10.2
    restart: always
    volumes:
      - ./mysql/data:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: test
      MYSQL_DATABASE: yona
      MYSQL_USER: yona
      MYSQL_PASSWORD: test
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
  yona:
    build: .
    image: pokev25/yona:1.12.0
    restart: always
    environment:
      - BEFORE_SCRIPT=before.sh
      - JAVA_OPTS=-Xmx2048m -Xms1024m
    volumes:
      - ./data:/yona/data
    ports:
      - "9000:9000"
    links:
      - yona_db
```

어차피 `mariadb`는 `yona`만 사용할 것임으로 `links`를 통해 연결시키고 별도의 포트매핑을 생략한다.  

실행하면 볼륨으로 연결된 data 폴더에 `yona`의 `/docker-yona/data/conf/application.conf` 설정파일이 공유되는데 db주소를 변경해야한다.  

기존에 `docker-compose.yml`에서 설정한 db컨테이너의 호스트명을 사용  

```conf
# MariaDB
db.default.driver=org.mariadb.jdbc.Driver
db.default.url="jdbc:mariadb://yona_db:3306/yona?useServerPrepStmts=true"
db.default.user=yona
db.default.password="test!"
```

> 특수문자가 들어가면 꼭 따옴표로 문자열을 감싸자

nginx와 ssl을 같이 사용하기위해 yona의 url prefix(context path)를 수정하고 싶다면 
주석을 풀고 `application.conf` 를 아래처럼 수정   
`application.context = /yona`  

젠킨스의 경우 환경변수에 아래 문자열을 추가하면 된다.  

`JENKINS_OPTS="--prefix=/jenkins"`

이미 젠킨스 컨테이너를 환경변수 없이 만들었다면 https://github.com/moby/moby/issues/8838 참고
`sudo service docker stop`
`/var/lib/docker/containers/conainerID/config.json` 파일에서 `Env` 배열에 문자열을 추가한 후 다시 서버를 실행하면 된다.  

아니면 지우고 컨테이너를 아래와 같이 다시 생성
`docker run -d -p 8080:8080 -p 50000:50000 -e "JENKINS_OPTS=--prefix=/jenkins" --name jenkins jenkins/jenkins`
