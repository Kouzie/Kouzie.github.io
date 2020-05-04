---
title:  "Spring - Maven Spring Project!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true

---

## 스프링 프로젝트 시작  

`spring-tool-suite-3.9.3.RELEASE-e4.7.3-win32-x86_64.zip` 파일 풀어서 실행  
> http://dist.springsource.com/release/STS/index.html

스프링 프로젝트를 위해 만들어진 이클립스라고 보면 된다.  

인코딩 타입과 폰트, 테마등을 설정하고 웹 프로젝트를 만들어보자!

지금까지 `Dynamic Web Project`로 프로젝트를 만들고 스프링 프레임워크 library를 일일이 추가해서 사용해왔는데

이제 그럴필요 없이 **메이븐 웹 프로젝트**로 만들어보자.  

프로젝트명은 `stsMVC1`로 설정  
![image10](/assets/Spring/image10.png){: .shadow}   

![image11](/assets/Spring/image11.png){: .shadow}   

`Spring MVC Project` 선택후 진행,  

그리고 최상위 패키지명을 적으라 하는데 도메인 명을 사용하는게 일반적이다. (도메인은 겹치지 않으니까...)
![image12](/assets/Spring/image12.png){: .shadow}   

패키지명은 `org.sist.web`으로 설정  


프로젝트를 만들면 자동으로 `Central Maven Repository`(원격 중앙 리파지토리) 에서 SpringMVC에서 필요한 여러 jar파일들 `Local Repository`에  다운받는다.   

> `유저디렉토리\m2\repository`에 저장된다 -> `C:\Users\user\.m2\repository`   


이제는 일일이 jar파일을 lib폴더 혹은 `build path`에 넣을 필요 없다. (인터넷만 된다면!)  

만약 더 필요한 jar파일이 있다면 `pom.xml`에 설정추가만 해주면 자동으로 jar파일이 다운된다!  

> 인터넷 상황이 좋지 않다면 `Local Repository`에 파일이 모두 다운 안될 수 있는데 수동으로 추가해주면 된다.   


프로젝트 완성이 끝나면 밑의 프로그레스 바에서 `중앙 Repository`에서 jar파일을 다운받고 있는 상황을 알 수 있다.  

![image13](/assets/Spring/image13.png){: .shadow}   

![image14](/assets/Spring/image14.png){: .shadow}   

프로젝트 명 앞에 작게 `m` 알파벳이 붙어 있고  
폴더 앞에 `s` 알바펫이 붙어있다.  
각각 `Maven` 프로젝트, `Spring` **관련 폴더**임을 뜻한다.  

폴더구조에 대해선 나중에 설명하겠지만 간단히 `JSP/Servlet`과 비교하면  

`web-app` 폴더가 `JSP/Servlet`의 `WebContent`  

`src/main/java`폴더가 java소스파일과 패키지들이 저장되는 폴더이다.  

---

기존 서버가 아파치 톰캣이 아닌 다른 서버로 설정되어 있을 것인데 기존 설정된 서버를 지우고 아파치 톰캣으로 바꾸자.  

그리고 `pom.xml`에서 자바 버전이 `1.6`으로 되어있다면 1.8로 변경

```xml
<!-- pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/maven-v4_0_0.xsd">
    ...
    ...
		<java-version>1.8</java-version> <!-- 1.6이었던 내용을 1.8로 변경 -->
		<org.springframework-version>3.1.1.RELEASE</org.springframework-version>
		<org.aspectj-version>1.6.10</org.aspectj-version>
		<org.slf4j-version>1.6.6</org.slf4j-version>
	</properties>
	<dependencies>
		<!-- Spring -->
		...
    ...
    
		<!-- AspectJ -->
		...
    ...

		<!-- Logging -->
	</dependencies>
  <build>
    ...
    ...
  </build>
</project>
```

`<java-version>1.8</java-version>` 기존에 1.6이었던 자바 버전을 1.8로 변경후  
프로젝트 -> 우클릭 -> properties에서 java버전을 변경 다음 사진과 같이 변경  

![image15](/assets/Spring/image15.png){: .shadow}   

> `<org.springframework-version>`태그에 `3.1.1.RELEASE`로 설정된 정보를  `4.0.4.RELEASE`로 변경하면 jar파일들도 자동으로 `4.0.4`버전으로 변경된다.  


```xml
<properties>
		<java-version>1.8</java-version>
		<org.springframework-version>4.0.4.RELEASE</org.springframework-version>
		<org.aspectj-version>1.6.10</org.aspectj-version>
		<org.slf4j-version>1.6.6</org.slf4j-version>
	</properties>
```

---

메이븐 프로젝트를 만들면 다음 `web.xml`이 자동 생성된다.  
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee https://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">

	<!-- The definition of the Root Spring Container shared by all Servlets and Filters -->
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>/WEB-INF/spring/root-context.xml</param-value>
	</context-param>
	
	<!-- Creates the Spring Container shared by all Servlets and Filters -->
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!-- Processes application requests -->
	<servlet>
		<servlet-name>appServlet</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>/WEB-INF/spring/appServlet/servlet-context.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
		
	<servlet-mapping>
		<servlet-name>appServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

</web-app>
```

보면 모든 `Dispatcher`가 공용으로 사용할 빈 객체들이 들어갈 `root-context.xml`과  
기본 `Dispatcher`객체가 `appServlet`이름으로 하나 만들어진다.  
기본 `Dispatcher`의 설정파일은 아래와 같다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans:beans xmlns="http://www.springframework.org/schema/mvc"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:beans="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/mvc https://www.springframework.org/schema/mvc/spring-mvc.xsd
		http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">
	<annotation-driven />
	<resources mapping="/resources/**" location="/resources/" />
	<beans:bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<beans:property name="prefix" value="/WEB-INF/views/" />
		<beans:property name="suffix" value=".jsp" />
	</beans:bean>
	<context:component-scan base-package="org.sist.web" />
</beans:beans>
```

어노테이션을 사용할수 있는 설정 `<annotation-driven />`  

`@Component`, `@Controller`, `@Service`어노테이션이 설정된 클래스를 자동으로 빈 객체로 생성해주는 `<context:component-scan>`  

> 사실 `<context:component-scan>`만 있어도 어노테이션 사용 및 빈 객체 자동 생성이 가능하지만 `<annotation-driven />`을 추가로 설정하면 `@RequestMapping`, `@ModelAttribute`, `@SessionAttribute`, `@RequestParam` 등 MVC에서 사용되는 `Annotation`을 사용가능하다.  

## maven

`maven`은 빌드도구이다. 
빌드도구는 간단히 말하면 컴파일하고 실행하는 도구 (java프로젝트에서 `javac.exe`에서 빌드하고 `java.exe`에서 실행하듯이....)

> 꼭 웹프로젝트를 위한 빌드도구가 아님...

단 `maven`은 단순 빌드, 실행 기능뿐 아니라  
`라이브러리 설정(pom.xml)`  
`코드작성`  
`컴파일`  
`테스트`  
`패키지`  
`인스톨/배포`  
등의 기능이 더 있다.  

메이븐 빌드도구를 다운받아 위의 기능들을 사용해보자.  

> https://maven.apache.org/download.cgi 에서 `apache-maven-3.6.1-bin.zip`파일 설치

압축을 풀고 환경변수에 `maven` 실행 파일 위치를 환경변수로 등록

![image16](/assets/Spring/image16.png){: .shadow}    
![image17](/assets/Spring/image17.png){: .shadow}    

경로설정이 잘 됐는지 확인을 위해 `mvn -version`명령어를 cmd에서 실행  
```
Apache Maven 3.6.1 (d66c9c0b3152b2e69ee9bac180bb8fcc8e6af555; 2019-04-05T04:00:29+09:00)
Maven home: C:\apache-maven-3.6.1\bin\..
Java version: 1.8.0_192, vendor: Oracle Corporation, runtime: C:\Program Files\Java\jdk1.8.0_192\jre
Default locale: ko_KR, platform encoding: MS949
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
```

이제 `maven`을 사용하는 프로젝트를 생성해보자.  

`cmd`에서 아래 명령을 실행  
`mvn archetype:generate -DgroupId=com.newlecture -DartifactId=javaprj -DarchetypeArtifactId=maven-archetype-quickstart`  

`archetype` - 메이븐 프로젝트 템플릿 도구(`generate`)
`groupId` - 프로젝트 도메인명(`com.newlecture`)
`artifactId` - 프로젝트명(`javaprj`)
`archetypeArtifactId` - 템플릿 이름(`maven-archetype-quickstart`)

```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO]
[INFO] >>> maven-archetype-plugin:3.0.1:generate (default-cli) > generate-sources @ standalone-pom >>>
[INFO]
[INFO] <<< maven-archetype-plugin:3.0.1:generate (default-cli) < generate-sources @ standalone-pom <<<
[INFO]
[INFO]
[INFO] --- maven-archetype-plugin:3.0.1:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] Using property: groupId = com.newlecture
[INFO] Using property: artifactId = javaprj
Define value for property 'version' 1.0-SNAPSHOT: :
[INFO] Using property: package = com.newlecture
Confirm properties configuration:
groupId: com.newlecture
artifactId: javaprj
version: 1.0-SNAPSHOT
package: com.newlecture
 Y: :
[INFO] ----------------------------------------------------------------------------
[INFO] Using following parameters for creating project from Old (1.x) Archetype: maven-archetype-quickstart:1.0
[INFO] ----------------------------------------------------------------------------
[INFO] Parameter: basedir, Value: C:\Users\user
[INFO] Parameter: package, Value: com.newlecture
[INFO] Parameter: groupId, Value: com.newlecture
[INFO] Parameter: artifactId, Value: javaprj
[INFO] Parameter: packageName, Value: com.newlecture
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] project created from Old (1.x) Archetype in dir: C:\Users\user\javaprj
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  56.925 s
[INFO] Finished at: 2019-06-27T15:30:48+09:00
[INFO] ------------------------------------------------------------------------
```
명령을 실행한 폴더위치에 `javaprj` 폴더가 생겼다면 
프로젝트가 만들어 졌다면 테스트로 컴파일하자.

`javaprj`로 이동후 cmd에서 `mvn compile` 명령 실행
```
[INFO] Scanning for projects...
[INFO]
[INFO] -----------------------< com.newlecture:javaprj >-----------------------
[INFO] Building javaprj 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ javaprj ---
[WARNING] Using platform encoding (MS949 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory C:\Users\user\javaprj\src\main\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ javaprj ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding MS949, i.e. build is platform dependent!
[INFO] Compiling 1 source file to C:\Users\user\javaprj\target\classes
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.843 s
[INFO] Finished at: 2019-06-27T15:36:28+09:00
[INFO] ------------------------------------------------------------------------
```

단위테스트로 가능하다. 
cmd창에서 `mvn test` 실행

```
[INFO] Scanning for projects...
[INFO]
[INFO] -----------------------< com.newlecture:javaprj >-----------------------
[INFO] Building javaprj 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ javaprj ---
[WARNING] Using platform encoding (MS949 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory C:\Users\user\javaprj\src\main\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ javaprj ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ javaprj ---
[WARNING] Using platform encoding (MS949 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory C:\Users\user\javaprj\src\test\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ javaprj ---
[INFO] Changes detected - recompiling the module!
[WARNING] File encoding has not been set, using platform encoding MS949, i.e. build is platform dependent!
[INFO] Compiling 1 source file to C:\Users\user\javaprj\target\test-classes
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ javaprj ---
[INFO] Surefire report directory: C:\Users\user\javaprj\target\surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.newlecture.AppTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.003 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.315 s
[INFO] Finished at: 2019-06-27T15:41:16+09:00
[INFO] ------------------------------------------------------------------------
```

`mvn package` - jar파일 생성
```
[INFO] Scanning for projects...
[INFO]
[INFO] -----------------------< com.newlecture:javaprj >-----------------------
[INFO] Building javaprj 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ javaprj ---
[WARNING] Using platform encoding (MS949 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory C:\Users\user\javaprj\src\main\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ javaprj ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ javaprj ---
[WARNING] Using platform encoding (MS949 actually) to copy filtered resources, i.e. build is platform dependent!
[INFO] skip non existing resourceDirectory C:\Users\user\javaprj\src\test\resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ javaprj ---
[INFO] Nothing to compile - all classes are up to date
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ javaprj ---
[INFO] Surefire report directory: C:\Users\user\javaprj\target\surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.newlecture.AppTest
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.006 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO]
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ javaprj ---
[INFO] Building jar: C:\Users\user\javaprj\target\javaprj-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.467 s
[INFO] Finished at: 2019-06-27T15:41:36+09:00
[INFO] ------------------------------------------------------------------------
```

추가로 아래 명렁이 있다.  
`mvn install` - 로컬 저장소로 이동  
`mvn deploy` - 원격 저장소로 이동(배포)  

자기가 만든 jar파일을 로컬 리파지토리, 원격 리파지토리에 저장 가능.  

### 로컬 리파지토리에 jar 추가하기

이전에 스프링 타일즈를 사용해 웹 페이지를 모듈화 하였다.  

전에는 http://archive.apache.org/dist/tiles/v2.2.2/ 에서 `tiles-2.2.2-bin.zip` 저장해 10개정도의 jar파일을 저장하였는데 이제는 pom.xml에서 설정하나만 추가해주면 된다.  

> http://tiles.apache.org/download.html#Tiles_2_as_a_Maven_dependency

위 사이트에서 아래 `<dependency>`태그내용을 `pom.xml`에 삽입

```xml
<dependency>
	<groupId>org.apache.tiles</groupId>
	<artifactId>tiles-jsp</artifactId>
	<version>2.2.2</version>
</dependency>
```

![image18](/assets/Spring/image18.png){: .shadow}   

자동 추가됬다!  


`ojdbc6.jar`파일의 경우 `Oracle`사의 파일이기 때문에 `<dependency>`를 추가해도 중앙 리파지토리에 없기 때문에 자동 다운이 되지 않는다....

로컬 리파지토리에 올려야지 `<dependency>`태그를 통해 jar파일을 프로젝트에서 사용가능하다.  

먼저 메이븐 빌드도구를 통해 이미 가지고 있는 `ojdbc6.jar` 파일을 로컬리파지토리에 추가 , cmd에서 아래 명령을 실행한다.  

`mvn install:install-file  "-Dfile=ojdbc6.jar" "-DgroupId=com.oracle" "-DartifactId=ojdbc" "-Dversion=6.0" "-Dpackaging=jar"`
```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO]
[INFO] --- maven-install-plugin:2.4:install-file (default-cli) @ standalone-pom ---
[INFO] Installing C:\Class\ojdbc6.jar to C:\Users\user\.m2\repository\com\oracle\ojdbc\6.0\ojdbc-6.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.200 s
[INFO] Finished at: 2019-06-27T16:17:32+09:00
[INFO] ------------------------------------------------------------------------
```
실제 로컬 리파지토리에 추가되었는지 아래 시스템 경로에서 확인 가능  
`C:\Users\user\.m2\repository\com\oracle\ojdbc\6.0`


스프링 타일즈와 DB 드라이버외에도  
스프링 JDBC, 파일 업로드, DI 관계 주입을 위한 각종 라이브러리도 `pom.xml`에 추가하자.  
```xml
<!-- tiles -->
<dependency>
	<groupId>org.apache.tiles</groupId>
	<artifactId>tiles-jsp</artifactId>
	<version>2.2.2</version>
</dependency>

<!-- oracle -->
<dependency>
	<groupId>com.oracle</groupId>
	<artifactId>ojdbc</artifactId>
	<version>6.0</version>
</dependency>

<!-- Spring jdbc jdbc와 tx가 추가됐다.-->
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-jdbc</artifactId>
	<version>${org.springframework-version}</version>
</dependency>

<!-- 파일 업로드 -->
<dependency>
	<groupId>commons-fileupload</groupId>
	<artifactId>commons-fileupload</artifactId>
	<version>1.2</version>
</dependency>

<dependency>
	<groupId>commons-io</groupId>
	<artifactId>commons-io</artifactId>
	<version>1.2</version>
</dependency>

<!-- 의존관계가 있는 cglib -->
	<dependency>
		<groupId>cglib</groupId>
		<artifactId>cglib</artifactId>
		<version>2.2</version>
	</dependency>
```

`프로젝트 우클릭 -> Maven -> Add Dependency`를 통해서도 추가 가능하다.  

![image19](/assets/Spring/image19.png){: .shadow}  

![image20](/assets/Spring/image19.png){: .shadow}   


## 자주사용하는 어노테이션 목록

어노테이션 | 설명 | 사용 
|---|---|---|
`@Controller` | 스프링 MVC의 컨트롤러 객체임을 명시함 | 클래스
`@RequestMapping` | URL매핑을 위한 어노테이션 | 클래스, 메서드
`@RequestParam` | 요청에서 특별한 파라미터 값을 매개변수에 초기화 | 파라미터
`@RequestHeader` | 요청에서 특정 HTTP헤터 정보를 추출할 때 사용 | 파라미터
`@PathValue` | 현재 URL에서 원하는 정보를 추출할 때 사용하는 어노테이션 | 파라미터
`@CookieValue` | 요청 헤더에서 쿠키값을 추출할 때 사용하는 어노테이션 | 파라미터
`@ModelAttribute` | 자동으로 해당 객체를 뷰까지 전달하도록 하는 어노테이션 | 메서드, 파라미터
`@SessionAttribute` | 세션상에서 모델의 정보를 유지하고 싶은 경우 사용 | 클래스
`@InitBinder` | 파라미터를 수집해 객체로 만들경우에 커스터마이징 | 메소드
`@ResponseBody` | 리턴 타입이 HTTP 응답 메시지로 전송, JSON객체 사용시 적용함 | 메소드, 리턴타입
`@RequestBody` | 요청 문자열이 그대로 파라미터로 전달 | 파라미터
`@Repository` | DOA객체임을 명시하는 어노테이션 | 클래스
`@Service` | 서비스 객체임을 명시하는 어노테이션 | 클래스

