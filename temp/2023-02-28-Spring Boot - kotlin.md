---
title:  "Spring Boot - kotlin!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-kotlin
---

## 개요

`java` 대신 `kotlin` 을 사용해 `spring boot` 를 사용하는 방법을 알아본다.  

`spring initializr` 를 통해 아래와 같은 설정후 프로젝트 생성

```
Gradle - Kotlin
Language - Kotlin
Spring Boot - 2.7.9
Packaging - jar
Java - 11
```

> 링크: <https://start.spring.io/>

```groovy
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.9"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version "1.6.21" // 코틀린 JVM 플러그인
    kotlin("plugin.spring") version "1.6.21" // 코틀린 스프링 플러그인
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

## plugin

```kotlin
kotlin("plugin.allopen") 
kotlin("plugin.noarg")
```

합성한 기본 연산자를 코틀린 클래스에 추가한다

## kotlin JPA 

```groovy
plugins {
  kotlin("plugin.jpa") version "1.2.71"
}
```


`plugin.jpa` 은 `plugin.noarg` 를 기반으로 만들어 졌으며 아래 어노테이션을 가지고 있을경우 자동으로 기본생성자를 합성해준다.  

- `@Entity`  
- `@Embeddable`  
- `@MappedSuperclass`  
