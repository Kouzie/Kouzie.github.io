---
title:  "Spring Boot - Quartz!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Quartz

Java 애플리케이션에 통합 될 수있는 작업 스케줄링 라이브러리로 가장 유명한 작업 스케줄링 라이브러리  

> <https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-quartz>
> <https://blog.advenoh.pe.kr/spring/Quartz-Job-Scheduler란/>

### 구조  

![springboot_quartz1](/assets/springboot/springboot_quartz1.png)  

**Job**  
스케줄링할 실제 작업을 구현한 객체
`Quartz API`에서 단 하나의 메서드 `execute(JobExecutionContext context)` 를 가진 `Job` 인터페이스를 제공.
`Quartz`를 사용하는 개발자는 수행해야 하는 실제 작업을 이 메서드에서 구현하면 된다.  

매개변수인 `JobExecutionContext`는 `Scheduler`, `Trigger`, `JobDetail` 등을 포함하여 `Job` 인스턴스에 대한 정보를 제공하는 객체

**JobDetail**  
`Job`을 실행시키기 위한 정보를 담고 있는 객체
`Job`의 이름, 그룹, `JobDataMap` 속성 등을 지정할 수 있음.  

`Trigger`가 `Job`을 수행할 때 이 정보를 기반으로 스케줄링을 한다  

**JobDataMap**  
`JobDataMap`은 `Job` 인스턴스가 `execute` 함수를 실행할 때 사용할 수 있게 원하는 정보를 담을 수 있는 객체  
`JobDetail`을 생성할 때 `JobDataMap`도 같이 세팅해주면 된다  

**JobFactory**
실제로 `Job` 을 인스턴스화 시키는 클래스, 스프링에선 `SpringBeanJobFactory` 클래스로 구현되며
`Scheduler` 구현시에 스프링 구성설정에 따라 구현되어 내부 인스턴스에 저장된다.  
알아서 `Job` 에 `SpringBean` 을 의존성 주입 시키는등의 작업을 수행한다.  

**JobStore**  
`Scheduler` 에서 등록된 `Job`, `Trigger`, 그리고 실행이력이 저장되는 공간이다. 기본적으로 메모리공간에 저장되어 `JVM` 에서 관리되지만, 원한다면 다른 `RDB` 에서 관리할 수 있다.  

**Trigger**  
`Trigger`는 `Job`을 실행시킬 스케줄링 조건 (ex. 반복 횟수, 시작시간) 등을 담고 있고  
`Scheduler`는 이 정보를 기반으로 `Job`을 수행시킨다.  
`N Trigger = 1 Job`  
한개이상의 `Trigger`는 반드시 하나의 `Job`을 지정할 수 있다  

`SimpleTrigger` - 특정 시간에 `Job`을 수행할 때 사용되며 반복 횟수와 실행 간격등을 지정할 수 있다
`CronTrigger` - `CronTrigger`는 `cron` 표현식으로 `Trigger`를 정의하는 방식이다

**Scheduler**  
`JobDetail` 과 `Trigger` 을 시스템에 등록하고 스케쥴에 맞춰 `Job` 을 실행시키는 객체, 일반적으로 `StdScheduler` 로 구현된다.

**SchedulerFactory**  
Scheduler 인스턴스를 생성하는 역할, 스프링 부트에선 `application.properties` 를 사용해 다양한 설정을 통해 자동으로 구현가능하다.  

`Quartz Scheduler` 는 `Job`, `Trigger`, `JobStore` 와 같은 리소스를 관리(저장/삭제)하며  
`Quartz Scheduler Thread` 가 시작될 `Trigger` 보고있다가 관련 `Job` 을 실행시키는 구조이다.  

`Trigger` 의 `fire` 시점에 따라 `ThreadPool` 에 있는 `Worker` 노드에게 해당 `Job` 을 실행하도록 명령한다.  

클래스 관계도는 아래와 같다.  

![springboot_quartz2](/assets/springboot/springboot_quartz2.png)  

> <https://www.javarticles.com/2016/03/quartz-scheduler-model.html>

즉 `JobStore` 로부터 실행할 `Job` 과 `Trigger` 를 계속 지켜보고 있다가 실행시키는 것이기에
`Scheduler` 의 `schedule()` 함수를 통해 데이터만 입력하면 해당 스케줄은 `Quartz Scheduler Thread` 가 이어서 해준다.  

## Spring Boot Quartz 설정

유명하다보니 `spring-boot-starter` 프로젝트안에 Quartz 가 존재한다.  

```groovy
dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-quartz'
}
```

`Spring Boot` 의 경우 `application.properties` 파일에서 `Quartz` 에 간략한 설정은 모두 지정 가능하다.  

마이크로 서비스같이 서버가 여러개 돌아가는 상황에서 스케줄링을 하고 싶다면  
단 한번만 실행될 수 있게 설정해야 하고 실행 결과를 `jdbc` 로 저장할 수 있도록 설정한다.  

```conf
# spring quartz config
#
spring.quartz.scheduler-name=MyScheduler
spring.quartz.job-store-type=jdbc
# 자동으로 테이블이 생성된다, 이미 생성된 테이블은 삭제처리
spring.quartz.jdbc.initialize-schema=always
# 생성된 작업 덮어 쓰기
spring.quartz.overwrite-existing-jobs=true
단위테스트, Quartz 비활성화를 필요시 아래 속성을 false 로 지정
spring.quartz.auto-startup=true
```

만약 spring.quartz 외에 추가적인 설정이 필요하다면  
`spring.quartz.properties` 속성을 사용해서 아래와 같이 사용  

```conf
spring.quartz.properties.org.quartz.jobStore.isClustered=true
# jobdata string 으로 저장, blob 로 저장시 객체 바이트화 가능
spring.quartz.properties.org.quartz.jobStore.useProperties=true
```

더 많은 `spring.quartz.properties` 설정은 아래 URL 참고  

> <http://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/ConfigMain.html>

수동으로 테이블 생성시 아래 url 참고
> <https://github.com/quartz-scheduler/quartz/blob/master/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_mysql.sql>

`spring.quartz.jdbc.initialize-schema=always` 설정 사용시 기존에 있던 테이블을 삭제하고 다시 생성하는데 MSA 환경에선 곤란할 때가 많다.  

테이블이 없을경우에만 생성하고 없을때는 넘어가게 하고 싶을 때 직접 `quartz initial SQL` 쿼리를 정의할 수 있다.  

```conf
spring.quartz.jdbc.initialize-schema=always
spring.quartz.jdbc.schema=classpath:quartz-create.sql
```

`resource/quartz-create.sql` 파일을 생성하고 아래처럼 `DROP` 문은 모두 주석처리 후 `CREATE TABLE IF NOT EXISTS` 로 변경해준다.  

```sql
# DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
# DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
# DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
...
...

CREATE TABLE IF NOT EXISTS QRTZ_JOB_DETAILS
(
    ...
);
...
```

> 참고: <https://stackoverflow.com/questions/64101847/spring-boot-quartz-jdbc-tables-are-always-initailized>

`spring.quartz` 속성으로 `SchedulerFactoryBean` 등록하지 않고 직접 생성하고 싶다면  
`Spring.quartz.properties` 역시 아래처럼 별도의 파일 `quartz.properties` 로 빼서 설정해야 한다.  

```java
SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
schedulerFactory.setDataSource(dataSource);
...
...
return schedulerFactory;
```

> `datasource` 외에도 등록해야 되는 객체가 많음으로 웬만하면 `spring.quartz` 속성 사용을 권장  

## DB Tables

`Quartz` 의 `Jobstore` 를 DB 로 설정하고 생성되는 테이블 목록은 아래와 같다.  

```sql
QRTZ_BLOB_TRIGGERS
QRTZ_CALENDARS
QRTZ_CRON_TRIGGERS
QRTZ_FIRED_TRIGGERS
QRTZ_JOB_DETAILS
QRTZ_LOCKS
QRTZ_PAUSED_TRIGGER_GRPS
QRTZ_SCHEDULER_STATE
QRTZ_SIMPLE_TRIGGERS
QRTZ_SIMPROP_TRIGGERS
QRTZ_TRIGGERS
```

단순한 `log print` 를 하는 `Job` 생성하고 어떻게 DB에 저장되는지 확인  

```java
@Slf4j
public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap map = context.getJobDetail().getJobDataMap();
        log.info("RequestContractJob execute invoked, job-detail-key:{}, fired-time:{}, num:{}", 
            context.getJobDetail().getKey(), context.getFireTime(), map.getInt("num"));
        log.info("RequestContractJob execute complete");
    }
}

```

```java
@Configuration
@RequiredArgsConstructor
public class QuartzTestConfig {

    private final SchedulerFactoryBean schedulerFactory;

    @PostConstruct
    public void scheduled() throws SchedulerException {
        JobDataMap map1 = new JobDataMap(Collections.singletonMap("num", 1));
        JobDataMap map2 = new JobDataMap(Collections.singletonMap("num", 2));
        JobDetail job1 = jobDetail("hello1", "hello-group", map1);
        JobDetail job2 = jobDetail("hello2", "hello-group", map2);
        SimpleTrigger trigger1 = trigger("trigger1", "trigger-group");
        SimpleTrigger trigger2 = trigger("trigger2", "trigger-group");
        schedulerFactory.getObject().scheduleJob(job1, trigger1);
        schedulerFactory.getObject().scheduleJob(job2, trigger2);
    }

    public JobDetail jobDetail(String name, String group, JobDataMap dataMap) {
        JobDetail job = JobBuilder.newJob(HelloJob.class)
                .withIdentity(name, group)
                .withDescription("simple hello job")
                .usingJobData(dataMap)
                .build();
        return job;
    }

    public SimpleTrigger trigger(String name, String group) {
        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(10))
                .withDescription("hello my trigger")
                .build();
        return trigger;
    }
}
```

```
RequestContractJob execute invoked, job-detail-key:hello-group.hello1, fired-time:Tue Dec 14 11:12:05 KST 2021, num:1
RequestContractJob execute complete
RequestContractJob execute invoked, job-detail-key:hello-group.hello2, fired-time:Tue Dec 14 11:12:05 KST 2021, num:2
RequestContractJob execute complete
```

위와같이 `Schduler`, `Trigger`, `JobDetail` 을 설정하고 실행하였을때 
다음 5개의 테이블에 데이터가 저장된다.  

```sql
SELECT * FROM QRTZ_FIRED_TRIGGERS;
SELECT * FROM QRTZ_JOB_DETAILS;
SELECT * FROM QRTZ_LOCKS;
SELECT * FROM QRTZ_SIMPLE_TRIGGERS;
SELECT * FROM QRTZ_TRIGGERS;
```

![springboot_quartz1](/assets/springboot/springboot_quartz3.png)  

## Job 중단  

쿼리문으로 트리거 비활성화  
```SQL
UPDATE QRTZ_TRIGGERS SET TRIGGER_STATE = "PAUSED"
```

스케줄러 정지  
```java
scheduler.stanby()
```

### InterruptJob  

실행중인 `Job` 이 `Interrupt` 되었을 때 이벤트 호출

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class GradeRatingCronJob extends QuartzJobBean implements InterruptableJob {

    private final StoreService storeService;
    private boolean isInterrupted = false;
    private JobKey jobKey = null;

    @Override //InterruptableJob
    public void interrupt() throws UnableToInterruptJobException {
        log.info(jobKey + "  -- INTERRUPTING --");
        isInterrupted = true;
    }

    @Override // QuartzJobBean
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        jobKey = context.getJobDetail().getKey();
        log.info("GradeRatingCronJob executeInternal invoked, jobKey: " + jobKey + ", time:" + LocalDateTime.now().toString());
        if (isInterrupted) {
            log.warn("jobKey: " + jobKey + "is Interrupted.");
            return;
        }
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        storeService.updateAllStoreGrade();
    }
}
```

전달받은 `jobDataMap` 는 `JobExecutionContext` 에서 가져올 수 있다.  
부가적인 데이터(`name`, `desc` 등) 도 `JobExecutionContext` 에서 가져올 수 있다.  

`scheduler.interrupt(jobKey)` 메서드 호출로 중지시킬 수 있다.  

만약 특정상황이 발생하면 Job 을 중지시키고 특정 이벤트를 호출해야 한다면 `InterruptableJob`을 상속받고 위처럼 구현  

> <https://github.com/Flipkart/quartz/blob/master/distribution/examples/src/main/java/org/quartz/examples/example7/DumbInterruptableJob.java>  
위의 URL 에 해당 예제 외에도 다른 여러 예제가 많으니 참고

## Crone Expression  

`Crone Expression` 은 공백으로 구분되는 6개 또는 7개의 필드로 구성됩니다  

`* 1-5,7,8 * * * ?`

위의 예를 들 경우 `1-5,7,8` 사이에 공백이 없음으로 하나의 필드로 인식하며
`1,2,3,4,5,7,8` 분에 trigger 된다.  

`이름` | `필수여부` | `허용값` | `허용 특수문자`
|---|---|---|---|
`Seconds` | `YES` | `0-59` | `, - * /`
`Minutes` | `YES` | `0-59` | `, - * /`
`Hours` | `YES` | `0-23` | `, - * /`
`Day of month` | `YES` | `1-31` | `, - * ? / L W C`
`Month` | `YES` | `1-12 or JAN-DEC` | `, - * /`
`Day of week` | `YES` | `1-7 or SUN-SAT` | `, - * ? / L C #`
`Year` | `NO` | `empty` | `1970-2099` | `, - * /`


마지막 7번째는 년도를 기입해야 하기때문에 일반적으로 생략하여 6자리를 표현식을 주로 사용한다.  

`*` : 모두 포함
`?` : 해당 필드 고려 X, 일자를 나타내는 필드와 요일을 나타내는 필드는 동시에 설정 할 수 없음으로 둘중 하나는 `?` 이어야 함.  
`-` : 일련의 범위, `2-4`는 `2, 3, 4`를 의미  
`,` : 일련의 값을 나열 `2-4`는 `2,3,4로` 표현 가능  
`/` : 초기치를 기준으로 일정하게 증가하는 값을 의미, 초를 나타내는 필드에 `0/15`는 0초를 시작으로 15초씩 증가를 의미 (0, 15, 30, 45)


- 매 초마다 실행 : `* * * ? * *`
- 매 분마다 실행 : `0 * * ? * *`
- 매 시간마다 실행 : `0 0 * ? * *`
- 매일 0시에 실행 : `0 0 0 * * ?`
- 매일 1시에 실행 : `0 0 1 * * ?`
- 매일 1시 15분에 실행 : `0 15 1 * * ?`
- 4시간마다 실행 : `0 0 */4 ? * *`

## misfire  

> <https://github.com/quartz-scheduler/quartz/issues/95>  
> <https://github.com/quartz-scheduler/quartz/issues/218>  

JOB의 실행 도중 어플리케이션 문제가 발생해 중간에 멈추는일이 발생했고, `CRON_JOB` 으로 생성한 `QRTZ_TRIGGERS` 테이블 `TRIGGER_STATE` 칼럼값이 `ACQUIRED` 로 설정되어 장기간동안 실행되지 않았다.  

스케쥴이 실행되지 않을 경우를 `misfired trigger` 라 하며 이를 위한 정책을 추가할 수 있다.  

`Cron Trigger` 에서 지원하는 정책은 아래 3가지.  

- **withMisfireHandlingInstructionIgnoreMisfires**  
  misfire 상황에서 스케줄에 별도처리하지 않음. 향후 원복되었을때 fire 횟수를 채우기 위해 한번에 여러번 실행될 수 있음.  
- **withMisfireHandlingInstructionDoNothing**  
  misfire 상황에서 다음 스케줄 시간에 실행되도록 설정.  
- **withMisfireHandlingInstructionFireAndProceed**  
  misfire 상황에서 스케줄러에게 지금 실행되도록 설정. 다른 잘못된 실행은 병합하여 실행하지 않음.  

## 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/quartz-demo>
