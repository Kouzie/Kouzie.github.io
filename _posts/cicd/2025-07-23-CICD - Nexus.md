---
title:  "CI/CD - Nexus!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# # classes: wide

categories:
  - CI/CD
---

## Nexus

기본적인 설치 & nginx proxy 설정

```yml
# docker-compose.yml
version: '3'
services:
  nexus:
    image: sonatype/nexus3:3.80.0-alpine
    container_name: nexus
    ports:
      - "8081:8081" # Nexus UI
      - "5000:5000" # Docker Registry
    volumes:
      - ./data/nexus-data:/nexus-data
    environment:
      - MYSQL_USER=${NEXUS_MYSQL_USER}
      - MYSQL_PASSWORD=${NEXUS_MYSQL_PASSWORD}
    restart: unless-stopped
```

docker registry 의 경우 https 미사용시 `insecure-registries` 설정없이는 동작하지 않음으로 아래와 같이 `nginx https proxy` 구성을 추천.  

```conf
# /etc/nginx/sites-available/nexus.mydomain.co.kr
server {
    listen 80;
    listen 443 ssl;
    ssl_certificate /etc/letsencrypt/live/nexus.mydomain.co.kr/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/nexus.mydomain.co.kr/privkey.pem;

    server_name nexus.mydomain.co.kr;

    access_log /var/log/nginx/nexus/access.log;
    error_log /var/log/nginx/nexus/error.log;
    # for docker image upload
    client_max_body_size 20G;
    chunked_transfer_encoding on;

    # redirect to docker registry
    location /v2/ {
        proxy_pass http://127.0.0.1:5000;  # Nexus Docker hosted registry 포트
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
        proxy_http_version 1.1;
        proxy_request_buffering off;
    }

    location / {
        # Use IPv4 upstream address instead of DNS name to avoid attempts by nginx to use IPv6 DNS lookup
        proxy_pass http://127.0.0.1:8081/;
        proxy_pass_header Server;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto "https";
    }
}
```

### Repository

`Nexus Repository` 의 종류는 많지만 보통 `Docker Registry`, `Maven` 을 주로 사용.  
`Nexus Repository` 생성시 아래 3가지 유형 중 하나로 등록을 진행.  

- **Hosted**  
  Nexus 서버에서 직접 호스팅하는 로컬 저장소  
- **Proxy**  
  외부 저장소(Maven Central, JCenter 등)를 프록시로 연결하여 Nexus가 외부 저장소에서 아티팩트를 가져와 캐싱, 클라이언트에 제공  
- **Group**  
  Hosted + Proxy  

`Private Docker Registry` 사용시 보동 Hosted 를 사용  

`Security - Realms` 에서 `Docker Bearer Token Realm` 를 활성화

```sh
docker login nexus.mydomain.co.kr

# Authenticating with existing credentials... [Username: admin]
# Info → To login with a different account, run 'docker logout' followed by 'docker login'
# Login Succeeded
```

`Maven Repository` 의 경우 기본적으로 아래 2개 Repository 가 Hosted 로 생성되어있음.  

- maven-snapshot: 업로드 이후 업데이트 가능  
- maven-release: 업로드 이후 업데이트 불가능  

gradle 에서 아래와 같이 설정해서 사용가능.  

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GatewayNexus"
        url = uri("https://nexus.mydomain.co.kr/repository/maven-releases/")
        credentials {
            username = System.getenv("NEXUS_USERNAME") 
                    ?: project.findProperty("nexusUsername") as String?
                    ?: error("NEXUS_USERNAME not exist")
            password = System.getenv("NEXUS_PASSWORD") 
                    ?: project.findProperty("nexusPassword") as String?
                    ?: error("NEXUS_PASSWORD not exist")
        }
    }
}
```

명령어로 build 시에는 환경변수에서 ID/PW 를 가져올 수 있지만 IDE 툴에서 환경변수를 가져오지 못하는 경우가 있어 project.findProperty 를 같이사용, 아래 디렉토리에서 ID/PW 를 저장해두면 된다.  

```sh
cat ~/.gradle/gradle.properties
nexusUsername=admin
nexusPassword=...
```