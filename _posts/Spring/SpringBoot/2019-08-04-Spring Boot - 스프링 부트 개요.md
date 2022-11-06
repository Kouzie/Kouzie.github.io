---
title:  "Spring Boot - 스프링 부트 개요!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## 스프링 부트

지금까지 스프링MVC에서 각종 라이브러리 때문에 생기던 충돌 문제들
지원되지 않는 라이브러리 버전, `web.xml`과 `pom.xml`에 들어가는 복잡한 설정 등

모두 `제어의 역전(Inverse of Control: IoC)` 에서 발생하는 문제점들이다.  
코드가 아닌 설정파일이 서버코드보다 장황하다  

이런 문제점을 해결하기 위해 2009 년 `Spring Roo` 라는 새로운 프로젝트가 발표되고(인기없음)  
2013 년 `Spring Boot` 프로젝트가 출시된다.  

스프링 부트의 핵심은 개발 프로세스를 단순화 하고 사용자의 추가 인프라 설정 없이 프로젝트 시작이 가능하도록 하는 것 이다.  

장황한 시작코드가 아래처럼 5줄 코드로 변경된다.  

```java
@SpringBootApplication
public class DummyApp {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(DummyApp.class, args);
    }
}
```

### 스프링 프로젝트 생성

자주 사용하는 다음 6개의 라이브러리는 추가하고 만들도록 하자.(메이븐으로 설정)  

![springboot1_0](/assets/springboot/springboot1_0.png){: .shadow}   

`Spring Boot Devtools` 는 코드 변경과 동시에 톰켓 재실행을 설정할 수 있다. 

`Lombok`은 `getter`, `setter`, `tostring`등과 같은 반복코딩을 어노테이션으로 생략가능하다.  

`Thymeleaf`는 뷰 영역의 출력, 모듈화가 가능하다. 스프링부트에선 jsp보단 Velocity, `Thymeleaf` 를 사용해 뷰를 구현한다.  

> 보통 spring boot는 REST API 와 같은 비지니스 로직만 수행하고 결과를 출력하는 용도는 nodejs와 javascript(vue, react)를 같이 사용하여 뷰영역을 담당하는 프론트용 서버를 별도로 운영한다.  


`Spring Data JPA`는 보다 쉽게 DB접근을 도와준다.  

기본적으로 DB는 `MYSQL`를 사용하기 때문에 `mysql`용 드라이버도 같이 설치한다.  

아래오 같은 `pom.xml`이 생성된다.  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.7.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

기본적으로 아래 설정을적용  

```conf
spring.thymeleaf.cache=false
spring.devtools.restart.enabled=true
```

java파일이나 html파일을 수정하면 자동으로 서버가 재실행 된다.  

그리고 Intellij 의 `Lombok`과 `Thymeleaf`는 라이브러리 추가뿐 아니라 추가적으로 `preference`에서 플러그인 설치를 진행해야한다.  

### 스프링 부트 테스트

스프링 부트에선 이미 단위 테스트를 위한 라이브러리들이 포함되어 있기 때문에 별도의 pom.xml수정 없이 테스트가 가능하다.  

아래와 같은 어노테이션 사용
```java
@RunWith(ClassName.class)
@SpringBootTest
@WebMvcTest(SampleControllerTest.class)
```

먼저 간단히 아래와 같은 객체 데이터 저장용 객체 생성
```java
@Data
@ToString(exclude = {"val3"})
public class SampleVO {
    private String val1;
    private String val2;
    private String val3;

}
```

> `@Data` 어노테이션을 사용하면 getter, setter, tostring, 생성자, euqals, hashcode 를 모두 자동생성해준다.   

일반적인 테스트는 아래와 같이 테스트 진행이 가능하다.  

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Log
public class Boot01ApplicationTests {

    @Test
    public void contextLoads() {
        log.info(new SampleVO().toString());
    }
}
```

만약 컨트롤러를 테스트하고 싶다면 아래와같이 설정  

우선 간단한 컨트롤러용 클래스 생성
```java
@RestController
public class SampleController {
    @GetMapping("/hello")
    public String sayHello() {
        return "hello world";
    }
}
```

`MockMvc`의 `perform()` 메서드를 통해 보낼 요청을 설정하고 실행, 결과값을 받는 테스트를 진행할 수 있다.  

```java
@RunWith(SpringRunner.class)
@WebMvcTest(SampleControllerTest.class)
public class SampleControllerTest {

    @Autowired
    MockMvc mock;

    @Test
    public void hello() throws Exception {
        MvcResult result = mock.perform(MockMvcRequestBuilders.get("/hello"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("hello world"))
                .andReturn();

        System.out.println(result.getResponse().getContentAsString());
    }
}
```

> https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/test/web/servlet/ResultActions.html  

`perform`, `endExpect` 메서드는 `ResultActions` 인터페이스를 구현한 클래스를 반환하고 
`andExpect`는 `MockMvcResultMatchers` 클래스를 통해 실행받은 결과값(`ResultMatch`)를 인자값으로 받으며 여기 안에 설정된 값을 통해 테스트를 진행한다.  




아래처럼 `andExpect`와 `MockMvcResultMatchers`를 사용해 결과값 테스트를 틀릴경우(없는 url주소 요청, OK가 아니 404 에러, content가 hello world가 아닐경우)  

`MvcResult result = mock.perform(MockMvcRequestBuilders.get("/hello2"))`  

다음과 같은 결과값을 반환
```
MockHttpServletRequest:
      HTTP Method = GET
      Request URI = /hello2
       Parameters = {}
          Headers = []
             Body = <no character encoding set>
    Session Attrs = {}

Handler:
             Type = org.springframework.web.servlet.resource.ResourceHttpRequestHandler

Async:
    Async started = false
     Async result = null

Resolved Exception:
             Type = null

ModelAndView:
        View name = null
             View = null
            Model = null

FlashMap:
       Attributes = null

MockHttpServletResponse:
           Status = 404
    Error message = null
          Headers = []
     Content type = null
             Body = 
    Forwarded URL = null
   Redirected URL = null
          Cookies = []



java.lang.AssertionError: Status 
Expected :200
Actual   :404
<Click to see difference>
```

`andExpect`에 잘못된 설정이 들어있는 `ResultMatch` 를 인자로 전달하면 예외가 발생하며 람다식을 통해 예외를 테스트 가능하다.  
> https://github.com/HomoEfficio/dev-tips/blob/master/SpringMVCTest에서의%20예외%20테스트.md


`andReturn()` 메서드는 `MvcResult`를 반환하고 이를통해 요청에 대한 응답값을 확인 가능하다.  

`SampleController`의 `hello`매핑된 메서드는 `sayHello()` 컨트롤러 메서드이고 단순 문자열을 반환하는 기능을 가짐으로 `MvcResult`를 출력하면 

`hello world`가 출력된다.  