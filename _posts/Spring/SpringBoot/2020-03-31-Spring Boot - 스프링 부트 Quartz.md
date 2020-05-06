---
title:  "Spring Boot - 스프링 부트 Quartz!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true
toc_sticky: true

---

# Quartz

> Quartz는 다양한 Java 애플리케이션에 통합 될 수있는 작업 스케줄링 라이브러리입니다 - 위키

> https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-quartz

> https://blog.advenoh.pe.kr/spring/Quartz-Job-Scheduler란/


작업 스케줄링 으로 스프링 배치를 사용할 수 도 있지만 간단한 스케쥴링의 경우 `Quartz`를 사용하면 편하다.  

## 구조  

### Job

`Quartz API`에서 단 하나의 메서드 `execute(JobExecutionContext context)` 를 가진 `Job` 인터페이스를 제공한다.
`Quartz`를 사용하는 개발자는 수행해야 하는 실제 작업을 이 메서드에서 구현하면 된다.  

매개변수인 `JobExecutionContext`는 `Scheduler`, `Trigger`, `JobDetail` 등을 포함하여 `Job` 인스턴스에 대한 정보를 제공하는 객체이다

### JobDetail

`Job`을 실행시키기 위한 정보를 담고 있는 객체이다.
`Job`의 이름, 그룹, `JobDataMap` 속성 등을 지정할 수 있다.  

`Trigger`가 `Job`을 수행할 때 이 정보를 기반으로 스케줄링을 한다  

### JobDataMap

`JobDataMap`은 `Job` 인스턴스가 `execute` 실행할 때 사용할 수 있게 원하는 정보를 담을 수 있는 객체이다.  

`JobDetail`을 생성할 때 `JobDataMap`도 같이 세팅해주면 된다

### Trigger

`Trigger`는 `Job`을 실행시킬 스케줄링 조건 (ex. 반복 횟수, 시작시간) 등을 담고 있고
`Scheduler`는 이 정보를 기반으로 `Job`을 수행시킨다.  

`N Trigger = 1 Job`  
반드시 하나의 `Trigger`는 반드시 하나의 `Job`을 지정할 수 있다  

`SimpleTrigger` - 특정 시간에 `Job`을 수행할 때 사용되며 반복 횟수와 실행 간격등을 지정할 수 있다
`CronTrigger` - `CronTrigger`는 `cron` 표현식으로 `Trigger`를 정의하는 방식이다


### SchedulerFactory, Scheduler  

> https://www.javarticles.com/2016/03/quartz-scheduler-model.html  

`SchedulerFactory` – `Scheduler` 를 빌드하는 역할로 `Quartz` 관련 속성을 기반으로 스케줄러 모델을 빌드하는 역할을 한다.   
`Quartz` 관련 속성은 `application.properties` 에서 설정 가능, 아래사이트 참고  
> http://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/ConfigMain.html

`Scheduler` – 등록된 `Job` 과 `Trigger` 를 관리한다. 연관된 ​​`Trigger`의 발사시점을 보고있다가 관련 Job 을 실행시키는 역할을 한다.  

![springboot_quartz1](/assets/springboot/springboot_quartz1.png)  

`Quartz Scheduler` 는 `Job`, `Trigger`, `JobStore` 와 같은 리소스를 관리하며 `Trigger` 의 `fire` 시점에 따라 `ThreadPool` 에 있는 `Worker` 노드에게 해당 `Job` 을 실행하도록 명령한다.  

Java 클래스 관계도는 아래와 같다.  

![springboot_quartz2](/assets/springboot/springboot_quartz2.png)  


## 설정  

```gradle
dependencies {
  ...
  implementation 'org.springframework.boot:spring-boot-starter-quartz'
}
```

### DB 설치 - Mysql 

마이크로 서비스같이 서버가 여러개 돌아가는 상황에서 스케줄링을 하고 싶다면  
단 한번만 실행될 수 있게 설정해야 함  

실행 결과를 jdbc 로 저장할 수 있도록 설정  

> https://github.com/quartz-scheduler/quartz/blob/master/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_mysql.sql

```conf
spring.quartz.job-store-type=jdbc
spring.quartz.jdbc.initialize-schema=always
```

`always` 설정을 추가하면 자동으로 테이블이 생성된다.  

기존 테이블 기록이 삭제되는 것을 원하지 않느다면 주석  

## 코드  

대충 구성과 설정을 알았으니 코드로 구현해보자.  

해당 어플은 특정 시간마다 등록된 리뷰를 통해 상점의 총 평점을 `DB` 에 `UPDATE` 하는 배치다.  

```java
@Slf4j
@Configuration
public class SchedulingConfiguration {

  @Autowired
  private Scheduler scheduler;

  @PostConstruct
  public void start() {
      log.info("JobController start invoked");
      try {
        JobDetail jobDetail = buildJobDetail(
          GradeRatingCronJob.class,
          "gradeRatingJob", //name
          "상점에 대한 리뷰 별점 적용", //desc
          new HashMap()); //param

        //이미 스케쥴이 DB에 등록되어 있다면 삭제
        if (scheduler.checkExists(jobDetail.getKey())) {
          scheduler.deleteJob(jobDetail.getKey());
        }

        //Job과 트리거를 설정, 
        scheduler.scheduleJob(
          jobDetail,
          buildCronJobTrigger("0 0 * * * ?")); //1시간 마다
      } catch (SchedulerException e) {
          e.printStackTrace();
      }
  }

  // *  *   *   *   *   *     *
  //초  분  시  일  월  요일  년도(생략가능)
  public Trigger buildCronJobTrigger(String scheduleExp) {
      return TriggerBuilder.newTrigger()
        .withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp))
        .build();
  }

  // 매개변수로 입력받은 시간단위로 실행 
  public Trigger buildSimpleJobTrigger(Integer hour) {
    return TriggerBuilder.newTrigger()
      .withSchedule(SimpleScheduleBuilder
        .simpleSchedule()
        .repeatForever()
        .withIntervalInHours(hour))
      .build();
  }

  public JobDetail buildJobDetail(Class job, String name, String desc, Map params) {
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.putAll(params);
    return JobBuilder
      .newJob(job)
      .withIdentity(name)
      .withDescription(desc)
      .usingJobData(jobDataMap)
      .build();
  }
}
```

`JobBuilder` 를 통해 `JobDetail` 를 생성하고 `buildCronJobTrigger` 를 통해 `Trigger` 생성 후 이를 스케쥴에 등록한다.  

`JobDetail` 등록시에 사용된 `GradeRatingCronJob` 의 코드.  

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

만약 특정상황이 발생하면 Job 을 중지시켜야 한다면 `InterruptableJob`을 상속받고 위처럼 구현  

`scheduler.interrupt(jobKey)` 메서드 호출로 중지시킬 수 있다.  

> https://github.com/Flipkart/quartz/blob/master/distribution/examples/src/main/java/org/quartz/examples/example7/DumbInterruptableJob.java  
위의 URL 에 해당 예제 외에도 다른 여러 예제가 많으니 참고


## 테스트 실행시 quartz 비활성화  

Test 클래스에서 어노테이션으로 간단히 비활성화 할 수 있으면 좋겠지만 불가능하다.  

실행시에 인자값 전달로 실행할 수 있도록 설정하자.  

```conf
app.scheduling.enable=false
```

```java
@Slf4j
@Controller
@Configuration
@ConditionalOnProperty(prefix = "app.scheduling", name = "enable", havingValue = "true", matchIfMissing = true)
public class SchedulingConfiguration {

    @Autowired
    private Scheduler scheduler;

    @PostConstruct
    public void start() {
      ...
    }
}
```
위와같이 `application.properties` 를 설정해 두었기 때문에  
`@ConditionalOnProperty` 어노테이션에 의해 해당 설정을 `true` 로 바꾸지 않는 이상 `SchedulingConfiguration`는 빈으로 등록되지 않는다.  

향후에 서버 실행시 `java -jar -Dapp.scheduling.enable=true build/libs/....jar` 을 사용  

`-D` 옵션으로 `app.scheduling.enable`설정값을 `true` 로 변경하여 사용하자.  



