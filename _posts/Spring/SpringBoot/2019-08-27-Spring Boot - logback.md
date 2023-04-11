---
title:  "Spring Boot - logback!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## logback 개요

강력한 자바 오픈소스 로깅 프레임워크.  

기존의 스프링 프레임워크에선 `JCL(Java Common Logging)`을 사용하여 로그를 남기고 `log4j`라는 로깅 라이브러리를 자주 사용한다.  

스프링 부트에선 기본적으로 `logback`이라는 강력한 로깅 프레임워크를 사용한다.  

### 사용 전 설정

스프링 부트에선 기본적으로 `logback`을 사용하기 때문에 `pom.xml`에 따로 추가시킬 필요 없다.  
그저 설정을 위해 `classpath`(resource폴더) 아래에 `logback-spring.xml`에 로그 설정 정보를 적기만 하면 끝.  

만약 `logback-spring`이 아닌 다른 이름으로 로깅 설정파일을 만들고 싶다면 아래처럼 설정하자.  

* `application.properties` 파일에 아래와 같이 추가   
* `logging.config=classpath:logback.xml`    


## logback xml 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true">
    <contextName>demo</contextName>

    <property name="logHome" value="/data/logs/demo" />

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="fileAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>DEBUG</level>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${logHome}/%d{yyyyMMdd}/vending.%d{yyyyMMdd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>15MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <Pattern>
                %d{yyyy-MM-dd HH:mm:ss.SSS} %thread %-5level %logger - %m%n
            </Pattern>
        </encoder>
    </appender>

    <logger name="com.example.demo" level="INFO" />
    <root level="info">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="fileAppender" />
    </root>

</configuration>
```

복잡한 로그설정....

접어놓으면 아래 사진과 같다.  

![logback1](/assets/2019/logback1.png){: .shadow}  


logback 설정은 간단히 `appender`와 `logger`, `root`로 나뉜다.  

![logback2](/assets/2019/logback2.png){: .shadow}  

appender는 어디에, 어떻게 로그를 찍을 것인지에 대한 설정을 하는 부분이고,  
logger는 해당 appender들을 참조하며 해당 로거가 사용될 패키지와 로그레벨을 지정한다.

> `<configuration debug="true">` - configuration파일을 발견하고 설정이 제대로 되어있는지 상태정보라 출력된다.

### appender 종류 

`ConsoleAppender` : 콘솔에 로그 메시지를 출력한다.
`FileAppender` : 파일에 로그 메시지를 출력한다.
`RollingFileAppender` : 로그의 크기가 지정한 용량 이상이 되면 다른 이름의 파일을 출력한다.
`DailyRollingFileAppender` : 하루를 단위로 로그 메시지를 파일에 출력한다.
`SMTPAppender` : 로그 메시지를 이메일로 보낸다.
`NTEventLogAppender` : 윈도우의 이벤트 로그 시스템에 기록한다.

아무래도 가장 많이 쓰이는건 콘솔에 출력하는 `ConsoleAppender` 와 파일에 출력하는 `RollingFileAppender`  

### ConsoleAppender 속성  

**Property Name**|**Type**|**Description**
-----|-----|-----
`encoder`|`Encoder`|이벤트가 어떤식으로 작성되는지 설정. `pattern`속성을 통해 패턴 지정이 가능.
`target`|`String`|`"System.out"`, `"System.error"` 택1, 기본은 `"System.out"`.
`withJans`|`boolean`|기본값은 false, true로 지정하면 Jansi 라이브러리를 사용해 색을 입힐 수 있다.  

#### pattern 

* 패턴에 사용되는 요소

1. %Logger{length} - Logger name을 축약할 수 있다. {length}는 최대 자리 수

2. %thread - 현재 Thread 이름

3. %-5level - 로그 레벨, -5는 출력의 고정폭 값

4. %msg - 로그 메시지 (=%message)

5. %n - new line

6. ${PID:-} - 프로세스 아이디

기타

* %d : 로그 기록시간

* %p : 로깅 레벨

* %F : 로깅이 발생한 프로그램 파일명

* %M : 로깅일 발생한 메소드의 이름

* %l : 로깅이 발생한 호출지의 정보

* %L : 로깅이 발생한 호출지의 라인 수

* %t : 쓰레드 명

* %c : 로깅이 발생한 카테고리

* %C : 로깅이 발생한 클래스 명

* %m : 로그 메시지

* %r : 애플리케이션 시작 이후부터 로깅이 발생한 시점까지의 시간

> 출처: https://jeong-pro.tistory.com/154 [기본기를 쌓는 정아마추어 코딩블로그]

### RollingFileAppender 속성  

**Property Name**|**Type**|**Description**
-----|-----|-----
`append`|`boolean`|기본값은 true, true일 경우 기존파일에 이어서 쓰기, false일경우 새로운 파일을 생성하여 쓰기.
`encoder`|`Encoder`|이벤트가 어떤식으로 작성되는지 설정. `pattern`속성을 통해 패턴 지정이 가능.
`file`|`String`|See FileAppender properties.  
`rollingPolicy`|`RollingPolicy`|만들어질 파일 이름설정, minIndex, maxIndex설정 가능, 크기별, 시간별, 크기/시간별 로그파일을 만들 수 있다.  
`triggeringPolicy`|`TriggeringPolicy`|새로운 파일을 만들 트리거를 지정, maxFileSize 내부속성을 사용해서 크기가 다 차면 새로운 파일을 생성하도록 설정가능하다.
`prudent`|`boolean`|여러개의 JVM이 하나의 로그파일에 동시작성을 막기위한 lock설정, 성능저하 있을 수 있음. 일반적으로 2개잇아의 JVM이 하나의 로그파일을 같이사용하지 않는다.

방금 위에서 보았던 `RollingFileAppender`의 `rollingPolicy` 속성을 다시보자.  
```xml
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>${logHome}/%d{yyyyMMdd}/demo.%d{yyyyMMdd}.%i.log</fileNamePattern>
    <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>15MB</maxFileSize>
    </timeBasedFileNamingAndTriggeringPolicy>
</rollingPolicy>
```
`TimeBasedRollingPolicy` 클래스를 사용한다. 크기와 시간별로 로그파일을 생성,  
`timeBasedFileNamingAndTriggeringPolicy`로 파일마다 트리거를 걸어 파일 최대 용량을 설정하면 새로운 index이름으로 파일을 생성,  
시간설정과 함께 용량이 다차면 파일 분리까지 해서 계속 저장하려면 `timeBasedFileNamingAndTriggeringPolicy`사용.  

이외에 `TimeBasedRollingPolicy`에 `maxHistory`, `totalSizeCap` 속성이 있다.  
```xml
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    ...
    <maxHistory>30</maxHistory> 
    <totalSizeCap>3GB</totalSizeCap>
</rollingPolicy>
```
`maxHistory`설정으로 최대 30보관하고 이전 로그는 삭제할수 있고
`totalSizeCap` 아카이브 총 용량이 3GB를 넘어가면 오래된 파일을 비동기적으로 삭제, 먼저 maxHistory설정이 있어야 사용가능하다.  


### 로그남기기

로그 레벨은 `TRACE > DEBUG > INFO > WARN > ERROR` 순으로 있다.  

만약 INFO를 로그레벨로 설정하면 그 아래에 있는 WARN, ERROR 또한 같이 기록됩니다.

`Logger`는 실제 로그 기능을 수행하는 객체로 각 `Logger`마다 Name을 부여하여 사용합니다, root태그에 있는 설정과 같이 사용됩니다.
```xml
<logger name="com.example.demo" level="INFO" />
<root level="info">
    <appender-ref ref="STDOUT" />
    <appender-ref ref="fileAppender" />
</root>
```

`root`에 로그를 기록하기 위한 appender를 설정, `root logger`를 설정한다. 모든 logger들은 root logger에 따라 실제 로그를 기록하게된다. 

위처럼 설정하게 되면 자동적으로 com.example.demo 패키지 아래에 모든 클래스만큼 logger가 생성된다.  
당연히 상위의 level인 INFO를 따르게 되며 별로 지정하고 싶다면 아래와같은 logger태그를 통해 한번더 정의하면 된다.  
`<logger name="com.example.demo.ExampleClass1" level="DEBUG" />`


> https://logback.qos.ch/manual/


또한 스프링 부트에선 xml을 사용하지 않고 `application.properties` 설정만으로 logback 처리가 가능하다.  

`logback.xml` 설정에 덮어씌어지기 때문에 `logback.xml` 에서 INFO 로그만 남긴다 설정해도 `logging.level.org.springframework.r2dbc=DEBUG` 사용시 `org.springframework.r2dbc` 패키지 로그는 DEBUG 레벨까지 찍히게 된다.  

### AppenderBase

로그를 기록하는기능 외에 locback을 사용하면 특정 로그가 발생할때 추가적인 작업을 할 수 있도록 설정 가능하다.  

만약 ERROR 레벨의 로그가 발생할 경우 관리자에게 메일을 보낸다던지, 특정 코드를 수행하게 하고싶을때  
`AppenderBase`를 구현한 클래스를 정의하여 사용하면 된다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true">
    <contextName>demo</contextName>

    <property name="logHome" value="/data/logs/demo" />

    ...
    ...(기존의 여러 appender 생략)


    <appender name="errorLogMonitorAppender" class="com.example.demo.ErrorLogMonitorAppender">
    </appender>

    <logger name="com.example.demo" level="INFO" />
    <root level="info">
        ...
        ...
        <appender-ref ref="errorLogMonitorAppender" />
    </root>

</configuration>
```

`AppenderBase`를 구현한 `ErrorLogMonitorAppender` 클래스를 정의하고 이를 `appender`로 설정한다.  
그럼 root logger에 의해 로그가 발생할 때 마다 `errorLogMonitorAppender`에게 전송하게 되고 `ERROR`정도의 심각한 로그만 캐치해 처리하면 된다.  
```java
public class ErrorLogMonitorAppender extends AppenderBase<ILoggingEvent> {

	@Override
	protected void append(ILoggingEvent event) {
        if (event != null  
            && event.getMessage() != null 
            && Level.ERROR.equals(event.getLevel())
            ) 
        {
            //여기서 관리자에게 메일을 보내거나 특정 처리하는 코드를 작성
		}
	}
}
```

스프링 bean 객체를 `AppenderBase` 로 등록하고 싶다면 xml 설정이 아닌 `LoggerContext` 를 사용해야 한다.  

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorLogMonitorAppender extends AppenderBase<ILoggingEvent> {

    private final SlackUtil slackUtil;

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${slack.web.hook.url}")
    private String slackWebHookUrl;

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLoggerList().forEach(logger -> logger.addAppender(ErrorLogMonitorAppender.this));
        setContext(context);
        start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event != null && Level.ERROR.equals(event.getLevel())) {
            log.info("error invoked:{}", event.getLoggerName());
        }
    }
}
```