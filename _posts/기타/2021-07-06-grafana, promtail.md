---
title:  "grafana, loki, promtail!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---

# 개요  

모니터링에서 사용하는 툴로 자주 거론되는게 `grafana, influxdb, prometheus, promtail` 등등이 있는데  

이번 포스팅에선 matrix 시계열 데이터 보단 `grafana, loki, promtail` 를 사용한 로그 모니터링을 다룬다.  

`grafana` 는 시계열 데이터를 사용자가 보기 편하게 그래프로 그려주는 툴이다.  

주로 쿠버네티스같은 클라우드 환경에서 `node` 모니터링을 위해 사용하는데  
베어메탈 형식의 싱글 서버에서도 사용하지 못할 건 없다.  


## 설치  

> <https://grafana.com/oss/loki/>  
> <https://grafana.com/docs/loki/latest/clients/promtail/>  

설치파일보단 `docker` 와 컴포즈를 사용해 설정하는것이 간편하다.  

```yaml
version: "3"
services: 
    grafana:
        image: grafana/grafana
        container_name: grafana
        restart: always
        ports:
            - "3000:3000"
        volumes:
            - ./grafana/grafana.ini:/etc/grafana/grafana.ini
            - grafana:/var/lib/grafana
    loki:
        container_name: loki
        image: grafana/loki:2.0.0
        command: -config.file=/etc/loki/local-config.yaml
        volumes:
            - loki-data:/loki
    promtail:
        container_name: promtail
        image: grafana/promtail:2.0.0
        volumes:
            - ./loki/promtail-docker-config.yaml:/etc/promtail/config.yml
            - /var/log:/var/log
            - ~/logs/my-application:/var/log/my-application
        command: -config.file=/etc/promtail/config.yml
volumes:
        grafana:
        loki-data:
```

`grafana` 외에는 `port` 를 따로 공유하지 않는다.  

### grafana config  

> https://github.com/grafana/grafana/blob/main/conf/defaults.ini

`grafana` `default` 설정은 `docker image` 설치시 내부에 있으며  
`proxy` 설정을 위해 root url 만 뒤에 `/grafana` 가 붙도록 설정하였다.  

```ini
...
# The full public facing url
# root_url = %(protocol)s://%(domain)s:%(http_port)s/
root_url = %(protocol)s://%(domain)s:%(http_port)s/grafana/

# Default UI theme ("dark" or "light")
# default_theme = dark
default_theme = light
...
```

`loki config` 는 별도로 건드리지 않고 그대로 사용,  

### promtail config

`promtail` 은 `/var/log` 에 있는 디렉토리와 홈디렉토리에 있는 `~/logs` 디렉토리를 `volume` 으로 설정  
`promtail` 컨테이너가 로그를 검색해 `loki` 로 보낼수 있도록 설정하였다.  


```yaml
# promtail config
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

client:
  url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: nginx
    static_configs:
    - targets:
        - localhost
      labels:
        job: nginx
        env: production
        host: nginx
        __path__: /var/log/nginx/*.log

  - job_name: my-application
    static_configs:
    - targets:
        - localhost
      labels:
        job: my-application
        env: production
        host: my-application
        __path__: /var/log/my-application/*/*.log

  - job_name: system
    static_configs:
    - targets:
        - localhost
      labels:
        job: system
        env: production
        host: system
        __path__: /var/log/*.log
```

같은 `docker-compose` 네트워크 안에 있는 `loki` 서버에 `http` 로 메세지 `push`  

![ddd1](/assets/2021/grafana1.png)  


### nginx proxy 설정  

`nginx` 에서 `proxy` 설정을 통해 `http, https` 를 사용해 `grafana` 에 접근할 수 있도록 `nginx.conf` 설정  

```conf
upstream grafana {
        server 127.0.0.1:3000;
}

...

location /grafana {
    rewrite ^/grafana(/.*)$ $1 break;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header Host $http_host;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_pass http://localhost:3000;
}
```

> 실시간 로그 확인은 websocket 을 통해 이루어짐으로 핸드쉐이크에서 `upgrade` 해더 추가 설정   

## promtail pipline  

`promtail pipeline` 을 통해서 좀더 로그에 세세한 설정정보를 라벨링처리하여 `loki` 에 저장할 수 있다.  

> https://grafana.com/docs/loki/latest/clients/promtail/pipelines/  


```
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %logger{50} - %msg%n
```

현재 logback 에서 출력하는 로그 형식은 위와 같다.  

```
2021-07-06 13:28:51.780 DEBUG o.springframework.r2dbc.core.DefaultDatabaseClient - Executing SQL stat ...
```

위와 같이 `timestamp LogLevel Logger - msg` 로 출력되며  
아래 `pipeline_stages` 속성을 사용해 라벨링처리한다.  

```yaml
- job_name: my-application
static_configs:
- targets:
    - localhost
    labels:
    job: my-application
    env: production
    host: ruan-prod-my-application
    __path__: /var/log/my-application/*/*.log
pipeline_stages:
- match:
    selector: '{job="my-application"}'
    stages:
    - regex:
        expression: '^(?P<timestamp>\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}\.\d{3})\s(?P<level>[A-Z]{4,5})\s(?P<logger>.*)\s-\s(?P<message>.*)$'
    - labels:
        timestamp:
        level:
        logger:
        message:
```

> 공백을 통해 구분가능함으로 `\s` 를 사용해 라벨링처리한다.  