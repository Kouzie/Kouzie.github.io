---
title:  "CI/CD - Jenkins 트리거 job!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# # classes: wide

categories:
  - CI/CD
---

## CI/CD 의존성 분리 필요성  

Jenkins 로 프로젝트를 CI/CD 하게되면 git 에 Jenkinsfile 을 포함한 CI/CD 에 필요한 각종 문서들이 필요하다.  
프로젝트의 코드실행과는 관계 없는 해당 파일들이 의존성으로 느껴지는 순간이 오게된다.  

1. Dockerfile 빌드시 추가 환경변수가 필요로할 때
2. 빌드 명령에 파라미터가 일부 변경될 때  
3. 배포 위치가 변경될 때

프로젝트 코드와는 크게 관련 없는 내용이지만 git 에 추가적인 변경 이력을 생성시킨다.  

Dockerfile 기반으로 빌드하는 경우도 프로젝트의 구성, 언어 상관 없이 CI 스크립트는 어느정도 통일시킬 수 있는데,  
프로젝트별로 동일한 CI 코드(Jenkinsfile) 을 생성하고 관리하는 것이 지루하게 느껴질 때가 있다.  

만약 일부 CI 코드에 변경이 일어나면 모든 프로젝트의 Jenkinsfile 을 변경시켜야 함으로 의존관계로 인한 부하가 심해질 수 도 있다.  

아래와 같이 단계별로 스크립트를 구성하면 프로젝트에 CI/CD 파일 의존성 없이도 CI/CD 를 진행할 수 있다.  

1. 트리거 job  
   - pollSCM 기능을 사용해 git 의 commit 발생시 트리거 역할  
2. 빌드 job  
   - 트리거 job 으로부터 실행  
   - 프로젝트를 git 으로부터 clone  
   - 지정된 스크립트를 수행하여 build    
3. 배포 job
   - 빌드 job 으로부터 실행  
   - 빌드된 결과물을 실행

Jenkins 의 장점은 다양한 플러그인으로 인해 자유로은 설정으로 CI/CD 를 구성할 수 있다는 것.  

위와같이 구성하면 언어별, 프레임워크별로 빌드 job 을 미리 생성해두고 기존에 존재하는 프로젝트, 앞으로 생성할 프로젝트에 재사용할 수 있다.  
배포 job 또한 마찬가지로 dev, stg, prd 환경별로 배포 job 을 만들어두고 상황에 맞춰 재사용 가능하다.  

## 트리거 job 구성  

단순 git과 연동하여 poolSCM 을 통해 트리거 역할만 수행해줄 `Freestyle Project`

`H/2 * * * *` 으로 2분마다 커밋기록을 확인하고 트리거시킬 수 있다.  

트리거 되면 `parameterized-trigger` 플러그인을 사용해서 `빌드 job` 과 `배포 job` 을 순차적으로 실행시킨다.  

> <https://plugins.jenkins.io/parameterized-trigger/>

`Freestyle Project` 생성시 `build step` 에서 `Trigger/call builds on other projects` 을 사용하면 생성할 수 있다.  

`Predefined parameters` 을 사용하면 아래와 같이 config 설정으로 다름 job 에 파라미터를 넘길 수 있다.  

```conf
# for build job
SERVICE_NAME=my_demo_service
REPO_URL=https://gitlab.mydomain.com/my_demo_service
BRANCH=dev
```

```conf
# for deploy job
SERVICE_NAME=my_demo_service
DEPLOY_SERVER=192.168.0.10
DEPLOY_PATH=/home/kouzie/service-docker-struct
```

## 빌드 job 구성

build 는 Pipeline 으로 생성, 환경은 아래와 같다. 

- kaniko 사용, k8s jenkins 사용중  
- nexus Private Docker Registry 사용중  
- Pipeline script from SCM 사용  
  git 으로부터 pipeline 코드 가져오기  
  빌드에 필요한 Dockerfile_service_jvm 사전 정의  
- 사전에 git, nexus 를 위한 Credential 은 미리 저장해준다.  


실행시키는 Jenkinsfile 의 코드는 아래와 같다.  

```groovy
pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  name: kaniko
  namespace: jenkins
spec:
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:debug
      tty: true
      command: # 컨테이너가 종료되지 않도록 tty 설정과 명령어 실행
        - /busybox/cat
      volumeMounts:
        - name: docker-config
          mountPath: /kaniko/.docker
  volumes:
    - name: docker-config
      emptyDir: {}
'''
        }
    }
    parameters {
        string(name: 'SERVICE_NAME', description: 'Service name for Docker image', defaultValue: '')
        string(name: 'REPO_URL', description: 'Git repository URL', defaultValue: '')
        string(name: 'BRANCH', description: 'Branch to build', defaultValue: 'dev')
    }
    environment {
        REGISTRY = 'nexus.mydomain.co.kr'
        IMAGE_NAME = "${params.SERVICE_NAME}"
        NEXUS_CREDENTIALS_ID = 'nexus-registry'
        GIT_CREDENTIALS_ID = 'kouzie_accesstoken'
    }
    stages {
        stage('Check Parameters') {
            steps {
                script {
                    echo "SERVICE_NAME: ${params.SERVICE_NAME}"
                    echo "REPO_URL: ${params.REPO_URL}"
                    echo "BRANCH: ${params.BRANCH}"
                    if (!params.SERVICE_NAME?.trim()) {
                        error "SERVICE_NAME 파라미터가 비어있습니다."
                    }
                    if (!params.REPO_URL?.trim()) {
                        error "REPO_URL 파라미터가 비어있습니다."
                    }
                    if (!params.BRANCH?.trim()) {
                        error "BRANCH 파라미터가 비어있습니다."
                    }
                }
            }
        }
        stage('Create Docker Config') {
            steps {
                container('kaniko') {
                    withCredentials([usernamePassword(credentialsId: env.NEXUS_CREDENTIALS_ID,
                                                    usernameVariable: 'DOCKER_CREDENTIALS_USR',
                                                    passwordVariable: 'DOCKER_CREDENTIALS_PSW')]) {
                        script {
                            sh '''
                            set +x
                            printf '{"auths": {"%s": {"auth": "%s"}}}' "$REGISTRY" "$(echo -n "$DOCKER_CREDENTIALS_USR:$DOCKER_CREDENTIALS_PSW" | base64)" > /kaniko/.docker/config.json
                            cat /kaniko/.docker/config.json
                            set -x
                            '''
                        }
                    }
                }
            }
        }
        stage('Checkout Service Source Code') {
            steps {
                // 컨테이너 지정 필요 없음 (Jenkins가 자체적으로 git을 사용)
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: params.BRANCH]],
                    userRemoteConfigs: [[
                        url: params.REPO_URL,
                        credentialsId: env.GIT_CREDENTIALS_ID
                    ]],
                    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'app/source']]
                ])
                script {
                    echo "서비스 소스코드가 app/source 디렉토리에 정상적으로 체크아웃되었습니다."
                }
            }
        }
        stage('Copy Dockerfile') {
            steps {
                container('kaniko') {
                    script {
                        // 현재 pipeline repo에서 Dockerfile만 복사
                        checkout scm
                        def dockerfileName = "Dockerfile_service_jvm"
                        sh "cp ${dockerfileName} app/source/Dockerfile"
                        echo "${dockerfileName}을 app/source/Dockerfile로 복사 완료"
                    }
                }
            }
        }
        stage('Build and Push with Kaniko') {
            steps {
                container('kaniko') {
                    withCredentials([usernamePassword(credentialsId: env.NEXUS_CREDENTIALS_ID,
                                                      usernameVariable: 'NEXUS_USERNAME',
                                                      passwordVariable: 'NEXUS_PASSWORD')]) {
                        script {
                            def tag = params.BRANCH ?: 'latest'
                            try {
                                sh '''
                                /kaniko/executor \
                                  --dockerfile app/source/Dockerfile \
                                  --context app/source \
                                  --destination $REGISTRY/$IMAGE_NAME:''' + tag + ''' \
                                  --cache=true \
                                  --snapshotMode=redo \
                                  --cache-repo=$REGISTRY/$IMAGE_NAME/cache \
                                  --build-arg NEXUS_USERNAME=$NEXUS_USERNAME \
                                  --build-arg NEXUS_PASSWORD=$NEXUS_PASSWORD \
                                  --cleanup
                                '''
                            } catch (Exception e) {
                                error "Kaniko build failed: ${e}"
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            dir('.') {
                sh 'rm -rf app/source' // 민감 정보 정리
            }
        }
    }
}
```

`Dockerfile_service_jvm` 파일은 `SpringBot + Kotlin` 빌드를 위한 `Dockerfile` 로 아래와 같다.  

```conf
# 빌드 단계
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# Nexus 인증 정보
ARG NEXUS_USERNAME
ARG NEXUS_PASSWORD
ENV NEXUS_USERNAME=$NEXUS_USERNAME
ENV NEXUS_PASSWORD=$NEXUS_PASSWORD

# 먼저 Gradle 설정 파일만 복사, 의존성 미리 로딩
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
# wrapper 복사(gradle 이미지에선 생략해도 됨)
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 프로젝트 소스 복사
COPY src ./src

# Gradle 빌드 실행
RUN gradle build --no-daemon -x test

# 실행 단계
FROM openjdk:21-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## 배포 job 구성  

개발단계의 서비스의 경우 개인서버에서 배포하여 테스트 진행중이다.  
단순 ssh 로 접속 후 nexus 에 업로드된 이미지를 `Pull & Up` 한다.  

```groovy
pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  name: ssh-deploy-agent
  namespace: jenkins
spec:
    containers:
      - name: ssh
        image: lscr.io/linuxserver/openssh-server:latest
        command:
          - cat
        tty: true
'''
        }
    }
    options {
        disableConcurrentBuilds() // 동시 실행 방지
    }
    stages {
        stage('Validate Parameters') {
            steps {
                container('ssh') {
                    script {
                        if (!params.SERVICE_NAME) {
                            error "SERVICE_NAME must be provided"
                        }
                        if (!params.DEPLOY_SERVER) {
                            error "DEPLOY_SERVER must be provided"
                        }
                        if (!params.DEPLOY_PATH) {
                            error "DEPLOY_PATH must be provided"
                        }
                    }
                }
            }
        }
        stage('Prepare SSH') {
            steps {
                container('ssh') {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'dev-server-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )]) {
                        sh """
                        mkdir -p ~/.ssh && chmod 700 ~/.ssh

                        ssh-keyscan -H ${params.DEPLOY_SERVER} >> ~/.ssh/known_hosts
                        chmod 600 ~/.ssh/known_hosts
                        chmod 600 \$SSH_KEY
                        ssh -i \$SSH_KEY \$SSH_USER@${params.DEPLOY_SERVER} whoami
                        """
                    }
                }
            }
        }
        stage('Deploy via SSH') {
            steps {
                container('ssh') {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: 'dev-server-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )]) {
                        sh """
                            ssh -i \$SSH_KEY \$SSH_USER@${params.DEPLOY_SERVER} \
                            "cd ${params.DEPLOY_PATH} && \
                            docker-compose -f docker-compose.yml pull ${params.SERVICE_NAME} && \
                            docker-compose -f docker-compose.yml up -d ${params.SERVICE_NAME}"
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            container('ssh') {
                sh 'rm -rf ~/.ssh' // 민감 정보 정리
            }
        }
    }
}
```

## 결론  

- 기존 서비스 프로젝트의 CI/CD 관련 코드 의존성을 제거하기 위해 별도의 Jenkinsfile 리포지토리를 운영  
- Jenkins 관련 코드도 유지하면서 재사용 가능한 job 을 생성할 수 있다.  
- 기존의 방식보단 GUI 콘솔 환경에서 손이 더 많이 가는것은 단점이다.  

