---
title:  "Spring Cloud - Testcontainers!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-cloud
---

## Testcontainers  

> <https://testcontainers.com/>  
> <https://github.com/testcontainers/testcontainers-java>

`Testcontainers` 는 도커환경에서 다양한 서드파티(DB, Redis, Broker) 시스템을 쉽게 테스트할 수 있도록 도와주는 테스트 지원 라이브러리.  

테스트가 종료되면 테스트 컨테이너 자원도 남김없이 깔끔하게 종료된다.  

```groovy
dependencies {
    testImplementation 'org.testcontainers:rabbitmq:1.18.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.18.3'
    ...
}
```