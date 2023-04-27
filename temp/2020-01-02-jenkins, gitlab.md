---
title:  "CI/CD jenkins!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
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
$ docker run -d -p 8080:8080 -p 50000:50000 -e JENKINS_OPTS="--prefix=/jenkins" --name myjenkins jenkins/jenkins
```

> 주의 사항: https://hub.docker.com/r/jenkins/jenkins/   
> 그냥 `docker pull jenkins` 실행시 더이상 업데이트 되지 않는 구 버전의 `jenkins`가 설치됨으로 위의 `docker pull jenkins/jenkins`명령을 실행필요.    


다음과 같이 `docker`가 실행중인지 확인 후 패스워드확인 후에 설치를 이어나가면 된다.  

```
$ docker exec -it -u root myjenkins /bin/bash
$ cat /var/jenkins_home/secrets/initialAdminPassword
password....
```

도커의 기본 서비스 포트는 `8080`으로 접속하면 비밀번호를 입력하라 한다.  
위의 `initialAdminPassword` 파일에서 비밀번호를 찾아 입력후 모든 `plugin install`.   

<!-- 
`nginx`와 같이 사용하기위해 `url prefix`를 수정하고 싶다면 아래와 같은 환경변수를 지정  
`JENKINS_OPTS="--prefix=/jenkins"`  
이미 젠킨스 컨테이너를 환경변수 없이 만들었다면 https://github.com/moby/moby/issues/8838 참고  
해당 컨테이너를 멈추고 `/var/lib/docker/containers/conainerID/config.json` 파일에서 `Env` 배열에 문자열을 추가한 후 다시 서버를 실행하면 된다.   
아니면 지우고 컨테이너를 아래와 같이 다시 생성
-->

#### jenkins - docker in docker 

또한 `jenkins` 내부에서 `docker image` 를 빌드하는 경우가 많은데  
그럴경우 `docker` 안에서 `docker` 명령어를 사용해야 한다.  

이때 volume 을 사용하여 host 의 docker 도커명령어를 사용하는 것을 권장한다.  

설정이 많음으로 jenkins 이미지를 기반으로 다시 이미지를 생성하는 것을 권장  

```docker
#공식 젠킨스 이미지를 베이스로 한다. 
FROM jenkins/jenkins:lts

#root 계정으로 변경(for docker install) 
USER root 

#DIND(docker in docker)를 위해 docker 안에서 docker를 설치 
COPY docker_install.sh /docker_install.sh RUN chmod +x /docker_install.sh 

RUN /docker_install.sh 
RUN usermod -aG docker jenkins 
USER jenkins
```

```
$ docker build -t custom-jenkins . 
```


```sh
# docker_install.sh
apt-get update && apt-get -y install apt-transport-https ca-certificates curl gnupg2 zip unzip software-properties-common && \
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
unzip awscliv2.zip && \
sudo ./aws/install && \
curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg > /tmp/dkey; apt-key add /tmp/dkey && \
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") $(lsb_release -cs) stable" && \
apt-get update && apt-get -y install docker-ce 
```

```yaml
version: '2'
services:
    jenkins:
        image: custom-jenkins
        privileged: true
        volumes:
            - /data/jenkins_home:/var/jenkins_home
            - /var/run/docker.sock:/var/run/docker.sock
        restart: always
        ports:
            - "8080:8080"
            - "50000:50000"
        environment:
            - JENKINS_OPTS:"--prefix=/jenkins"
```

> `usermod -aG docker jenkins` 진행하여도 docker sock 권한그룹이 host 와 docker jenkins 그룹이 다를 수 있다.  
> 그럴경우 /var/run/docker.sock 에 777 권한을 설정하는 것을 추천
 


### git, ssh 연동

우리의 목적은 자동 빌드 및 배포이기 때문에 개발자가 `git`에 `commit`하면  
`webhook` 을 통해 수정된 코드를 깃으로 부터 다운받아 `maven build` 후  
생성된 배포파일을 서버로 전송하는 작업을 수행시키도록 해야한다.  

추가적으로 필요한 아래 플러그인을 설치한다. `jenkins관리 -> 플러그인 관리`

- `Deploy to container`  
- `Maven Integration`  
- `SSH`  
- `Publish Over SSH`  
- `GitLab`  

> 주의사항: 만약 허가되지 않은 인증서(사내 서버 등)의 경우 `jenkins`에서 인증서 확인을 무시하도록 설정해야 한다.  
> `JENKINS_HOME` 환경변수 위치에 `.gitconfig` 파일을 생성   
> 젠킨스 환경변수 값은 우측과 같다.  `JENKINS_HOME=/var/jenkins_home`  
> 
```
vi /var/jenkins_home/.gitconfig

[http]
sslVerify=false
```


도커로 `jenkins`를 설치하였기 때문에 `젠킨스 컨테이너 -> 실서버` 로 빌드한 결과물을 전달해야 한다.  
도커안의 컴파일된 `war`, `jar`파일을 밖의 실사용 서버로 옮길때 위에서 설치한 `Publish Over SSH` 플러그인을 사용해 이동시킨다.  

`jenkins` 설정에서 `ssh` 연결설정을 등록하면 빌드 결과물 전달과 함께 원격명령(기존데이터를 백업 등)까지 쉽게 진행할 수 있다.  

![jenkinsssh](/assets/2019/jenkins_ssh.png){: .shadow}  

`id/pw`로 접속이 가능하고 `Remote Directory`는 원격서버의 기본 디렉토리 위치를 지정할 수 있다. 

> `SSH` 이나 `Publish Over SSH` 플러그인의 경우 최신 jenkins 에선 사라질 예정임으로 아래 `jenkins pipeline` 으로 빌드 및 배포하는것을 권장  

### jenkins pipelie 구성 

> https://www.jenkins.io/doc/book/pipeline/

간단한 프로젝트의 경우 위와같이 ssh 원격명령과 파일전송으로 빌드 및 배포가 가능하지만  
복잡해질경우 `pipeline` 사용을 권장한다.  

단순 플러그인만으로는 복잡한 환경의 `CI/CD` 구축에 사용할수 없는데 `jenkins pipeline` 이 스크립트 형식의 언어를 사용하여 프로그래밍 형식으로 `CI/CD` 를 지원해준다.  


장의 방법은 2가지 `Declarative Pipeline`, `Scripted Pipeline(Groovy)` 이 존재하는데 
`Declarative Pipeline` 사용을 권장한다.  


**pipeline 프로젝트 생성**

Pipeline 설정에서 직접 스크립트를 작성하는 것도 좋지만 Git 에서 항상 읽어와서 코드처럼 관리하는 것도 가능  

![jenkinsssh](/assets/2019/jenkins_pipeline1.png){: .shadow}


`cron expression` `H/2 * * * *` 을 `Build Triggers - Poll SCM` 에 지정 (2분마다 commit 검사)
git 플랫폼에 따라 trigger 하는 방식은 다르겠지만 옛날 버전의 gitlab 은 `webhook` 과 같은 기능이 없어 `Poll SCM` 을 사용하였음


### pipline 문법  

> https://www.youtube.com/watch?v=JPDKLgX5bRg
> https://www.jenkins.io/doc/book/pipeline/

```
# Jenkinsfile (Declarative Pipeline)
pipeline {
    agent any 
    stages {
        stage('Build') { 
            agent any
            steps {
            }
            post {
                success {
                    echo "build success"
                } 
                failure {
                    echo "build failed"
                }
                always {
                    echo "alway"
                }
                cleanup {
                    echo "after all other post condtion"
                }
            }
        }
        stage('Test') { 
            when {
                branch "dev"
                envrionment name: "PROFILE", value: "dev"
                steps {
                    // 
                }
            }
        }
        stage('Deploy') { 
            steps {
                dir ("./website"){
                    // 
                }
            }
        }
    }
}
```

위의 스크립트 구성을 보면 루트에 pipeline 구조 안에 대략적으로 4개의 `section` 을 구성할 수 있다.  

- `agent section`: slave node 생성, docker 기반 빌드환경 구성 가능  
- `stages section`: 파이프라인의 핵심, `test, build, deploy` 단계별 흐름을 정의하는 섹션  
- `steps section`: 각종 플러그인설치시 생성되는 여러가지 종류의 step 을 여기서 사용  
- `post section`: 스테이지 결과에 따라 후속조치  

대략적으로 아래와 같은 문법을 가진다.  

### example - multi module & pipeline  

안타깝게도 gitlab 이나 github 에서 제공하는 job 의 extends 와 같은 문법이 없기때문에  
스크립트 형식으로 module 을 정의하고 변화를 감지한 후 빌드&배포를 진행해야 한다.  

maven 이나 gradle 에서 multi module 로 프로젝트를 구성하였을 때  
특정 파일 변화를 확인하고 해당 모듈만 빌드해서 `CI/CD` 하는 방법을 소개한다.  

아래의 플러그인을 설치하고 global tool 를 설정  

- plugin 
  - AWS Steps plugin
  - AdoptOpenJDK installer Plugin

- global tool 
  - gradle
  - openjdk




## gitlab 

`jenkins pipeline` 으로도 `CI/CD` 를 구현 가능하지만 `gitlab` 을 사용하여 구현 가능하다.  

공인 gitlab 주소인 `gitlab.com` 에서 작업을 진행한다면 소규모 프로젝트의 경우 `CI/CD` 를 위한 컴퓨팅 리소스(`Runner`) 를 무료 제공한다.  


> tutorial  
> https://docs.gitlab.com/ee/ci/  
> 
> jenkins vs gitlab  
> https://about.gitlab.com/devops-tools/jenkins-vs-gitlab/gitlab-differentiators/  


### Runner 등록  

사설운영중인 `gitlab` 에서 `CI/CD` 를 처리해야 한다면 컴퓨팅 리소스인 `Runner` 를 등록해야 한다.  

도메인 인증서 문제로 `Runner` 등록이 안될경우 별도로 인증서 경로를 추가해주어야 한다.  



```sh
# https://docs.gitlab.com/runner/configuration/tls-self-signed.html
# 인증서 추출하기
$ openssl s_client -showcerts -connect dev.mydomain.com:3000 < /dev/null 2>/dev/null | openssl x509 -outform PEM > /etc/gitlab-runner/certs/dev.mydomain.com.crt
$ echo | openssl s_client -CAfile /etc/gitlab-runner/certs/dev.mydomain.com.crt -connect dev.mydomain.com:3000

# register 등록
$ sudo gitlab-runner register --url https://dev.mydomain.com:3000/ \

--registration-token 6Va7JAuPwBcSkiGYsoZg \

--tls-ca-file /etc/gitlab-runner/certs/dev.mydomain.com.crt \

--executor shell
```

### keyword

| Global keyword | desc                                                |
| -------------- | --------------------------------------------------- |
| `default`      | Custom default values for job keywords.             |
| `include`      | Import configuration from other YAML files.         |
| `stages`       | The names and order of the pipeline stages.         |
| `variables`    | Define CI/CD variables for all job in the pipeline. |
| `workflow`     | Control what types of pipeline run.                 |


| job keywords         | desc                                                                                                        |
| -------------------- | ----------------------------------------------------------------------------------------------------------- |
| `after_script`       | Override a set of commands that are executed after job.                                                     |
| `allow_failure`      | Allow job to fail. A failed job does not cause the pipeline to fail.                                        |
| `artifacts`          | List of files and directories to attach to a job on success.                                                |
| `before_script`      | Override a set of commands that are executed before job.                                                    |
| `cache`              | List of files that should be cached between subsequent runs.                                                |
| `coverage`           | Code coverage settings for a given job.                                                                     |
| `dast_configuration` | Use configuration from DAST profiles on a job level.                                                        |
| `dependencies`       | Restrict which artifacts are passed to a specific job by providing a list of jobs to fetch artifacts from.  |
| `environment`        | Name of an environment to which the job deploys.                                                            |
| `except`             | Control when jobs are not created.                                                                          |
| `extends`            | Configuration entries that this job inherits from.                                                          |
| `image`              | Use Docker images.                                                                                          |
| `inherit`            | Select which global defaults all jobs inherit.                                                              |
| `interruptible`      | Defines if a job can be canceled when made redundant by a newer run.                                        |
| `needs`              | Execute jobs earlier than the stage ordering.                                                               |
| `only`               | Control when jobs are created.                                                                              |
| `pages`              | Upload the result of a job to use with GitLab Pages.                                                        |
| `parallel`           | How many instances of a job should be run in parallel.                                                      |
| `release`            | Instructs the runner to generate a release object.                                                          |
| `resource_group`     | Limit job concurrency.                                                                                      |
| `retry`              | When and how many times a job can be auto-retried in case of a failure.                                     |
| `rules`              | List of conditions to evaluate and determine selected attributes of a job, and whether or not it’s created. |
| `script`             | Shell script that is executed by a runner.                                                                  |
| `secrets`            | The CI/CD secrets the job needs.                                                                            |
| `services`           | Use Docker services images.                                                                                 |
| `stage`              | Defines a job stage.                                                                                        |
| `tags`               | List of tags that are used to select a runner.                                                              |
| `timeout`            | Define a custom job-level timeout that takes precedence over the project-wide setting.                      |
| `trigger`            | Defines a downstream pipeline trigger.                                                                      |
| `variables`          | Define job variables on a job level.                                                                        |
| `when`               | When to run job.                                                                                            |

### job  

> https://docs.gitlab.com/ee/ci/jobs/


`GitLab CI/CD` 의 지침서를 `yaml` 형식으로 구현한다.  
`.gitlab-ci.yml` 파일에다 정의.  

```yaml
stages:
  - build
  - test
  - deploy

build-job:
  stage: build
  script:
    - echo "Hello, $GITLAB_USER_LOGIN!"

test-job1:
  stage: test
  script:
    - echo "This job tests something"

test-job2:
  stage: test
  script:
    - echo "This job tests something, but takes more time than test-job1."
    - echo "After the echo commands complete, it runs the sleep command for 20 seconds"
    - echo "which simulates a test that runs 20 seconds longer than test-job1"
    - sleep 20

deploy-prod:
  stage: deploy
  script:
    - echo "This job deploys something from the $CI_COMMIT_BRANCH branch."
```

![jenkins1](/assets/2022/gitlab1.png)   

파이프라인에 `echo` 명령, `sleep` 명령만 존재한다.  

최상위의 가장 기본적인 요소, 항상 처음은 `job` 으로 시작됨  

**job namimg rule**

256 글자 이하여야 하며 아래 예약어는 job 이름으로 사용 불가능  

- `image`  
- `services`  
- `stages`  
- `types`  
- `before_script`  
- `after_script`  
- `variables`  
- `cache`  
- `include`  

주석처리하거나 앞에 `.` 사용해 `GitLab CI/CD` 에서 처리되지 않음  

```yaml
# hidden_job:
#   script:
#     - run test
.hidden_job:
  script:
    - run test
```

**job group**

`/, :, ' '` 을 사용해서 `job group` 을 생성 가능하다.  
```yaml
build ruby 1/3:
  stage: build
  script:
    - echo "ruby1"

build ruby 2:3:
  stage: build
  script:
    - echo "ruby2"

build ruby 3 3:
  stage: build
  script:
    - echo "ruby3"
```

**job Child / Parent**

`job` 에 `needs` 키워드를 사용해 `Child / Parent` 구조를 적용 가능  

```yaml
stages:
  - build
  - test
  - deploy

image: alpine

build_a:
  stage: build
  script:
    - echo "This job builds something."

test_a:
  stage: test
  needs: [build_a]
  script:
    - echo "This job tests something."

deploy_a:
  stage: deploy
  needs: [test_a]
  script:
    - echo "This job deploys something."
```

```yaml
rubocop:
  inherit:
    default: false
    variables: false
  script: bundle exec rubocop
```

**job inherit**

docker image 나 variables 를 job 에 상속시킬 수 있다.  

`inherit` 키워드를 사용한다.  

```yaml
default:
  image: 'ruby:2.4'
  before_script:
    - echo Hello World

variables:
  DOMAIN: example.com
  WEBHOOK_URL: https://my-webhook.example.com

rubocop:
  inherit:
    default: false
    variables: false
  script: bundle exec rubocop

rspec:
  inherit:
    default: [image]
    variables: [WEBHOOK_URL]
  script: bundle exec rspec

capybara:
  inherit:
    variables: false
  script: bundle exec capybara
```

`capybara` `job` 의 경우엔 `default` 의 `image`, `before_script` 모두 기본 상속된다.  

**job rules**


### variables

코드상에 입력하기 어려운 변수의 경우 `GitLab CI/CD` 페이지에서 직접 설정한 후 `piepline` 에서 참조하여 사용 가능  

![jenkins1](/assets/2022/gitlab2.png)   

```yaml 
variables:
  TEST_VAR: "All jobs can use this variable's value"


test_variable:
  stage: test
  variables:
    TEST_VAR_JOB: "Only job1 can use this variable's value"
  script:
    - echo "$CI_JOB_STAGE"
    - echo "$TEST_VAR"
    - echo "$TEST_VAR_JOB"
    - echo "$AWS_ACCESS_KEY"
    - echo "$AWS_SECRET_KEY"

```

출력값  

```
$ echo "$CI_JOB_STAGE"
test
$ echo "$TEST_VAR"
All jobs can use this variable's value
$ echo "$AWS_ACCESS_KEY"
AKIAY....
$ echo "$AWS_SECRET_KEY"
LdqgM....
Job succeeded
```


#### Predefined variables

> https://docs.gitlab.com/ee/ci/variables/predefined_variables.html

| Variable | Desc |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| CI_COMMIT_BRANCH | The commit branch name. Available in branch pipelines, including pipelines for the default branch. Not available in merge request pipelines or tag pipelines. |


### AWS 배포  

> https://docs.gitlab.com/ee/ci/cloud_deployment/

`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_DEFAULT_REGION` 정도의 환경변수를 CI/CD 에 지정  

`registry.gitlab.com/gitlab-org/cloud-deploy/aws-base:latest` 이미지를 사용해 `aws cli` 을 사용한다.  

```yaml
deploy:
  stage: deploy
  image: registry.gitlab.com/gitlab-org/cloud-deploy/aws-base:latest  # see the note below
  script:
    - aws s3 ...
    - aws create-deployment ...
```

### 심각도  

`job` 의 성공 실패 여부는 심각도를 통해 알 수 있다.  

- failed  
- warning  
- pending  
- running  
- manual  
- scheduled  
- canceled  
- success  
- skipped  
- created  



<!-- 
## yona

네이버에서 개발한 코드 + 이슈 관리 시스템,

docker로 간단히 설치가능하다.  

> https://github.com/pokev25/docker-yona

`git clone https://github.com/pokev25/docker-yona`명령을 통해 `docker-compose.yml`, `Dockerfile`을 다운받는다.  

> `docker-compose` 설치는 아래 사이트 확인  
> https://kouzie.github.io/docker/docker-Dockerfile,-docker-compose/#docker-compose

만약 서버에서 사용하고 있는 DB가 `mariaDB` 이면 DB를 별도로 설치할 필요 없지만 만약 다른 db를 이미 시스템에서 사용중이라면 docker로 `mariaDB` 컨테이너를 별도로 실행하자.  

> 현재 `yona`에서 공식적으로 `mariadb:10.2`

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

> `nginx`와 같이 사용하기위해 `yona`의 `url prefix`를 수정하고 싶다면 
`application.conf` 를 아래처럼 수정   
`application.context = /yona`  

 -->
