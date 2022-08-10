---
title:  "Spring Boot - AOP!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## AOP (Aspect Oriented Programming)

> AOP: 관점 지향적 프로그래밍 기법(Aspect Oriented Programming)

스프링에서 모든 기능은 **핵심 비지니스 로직**과 **공통 기능**으로 나뉜다.  

로그인 인증과정 같이 중복되고 공통적으로 사용되는 기능을 **공통 기능**이라 하며  
`cross-cutting concern`(공통 관심사항)이라 표현한다.  

![image4](/assets/springboot/springboot_aop1.png)  

스프링 부트의 수많은 기본 `Filter`, `Interceptor` 등이 모두 `AOP` 기법으로 개발되었다 할 수 있다.  

### AOP 라이브러리  

AOP 기법을 위한 대표적인 라이브러리는 아래 2개  

- `Spring AOP`  
간단한 AOP 구현을 위해 Spring 컨테이너가 관리하는 빈에만 적용 가능한 라이브러리

- `AspectJ`  
완전한 AOP 솔루션 제공, 모든 자바객체에 적용가능하지만 복잡한 구성을 가지는 라이브러리  

`Spring AOP` 를 사용하게되면 자동으로 생성되는 코드로 인해 `AspectJ` 보다 훨씬 성능이 느리지만  
간단한 사용방법과 설정으로 인해 `Spring AOP` 를 주로 사용한다.  

> 두 라이브러리가 아예 분리되어있진 않고 `Spring AOP` 코드에서 `AOP` 개념을 가져오기 위해 `AspectJ` 라이브러리에 의존중임. 

### AOP 주요 용어

용어 | 의미  
|--|--|
`Aspect` | 여러 객체에 **공통적으로 적용되는 기능**을 `Aspect`라 한다. 인증, 트랜잭션, 보안 과 같은 공통적 기능을 의미한다.
`Advice` | 공통적으로 사용되는 기능(`Aspect`)를 핵심 로직에 언제 적용하는지, **시점**을 의미한다(핵심기능 시작 전, 후, 예외발생시 등).  
`JoinPoint` | `Advice`를 적용 가능한 **지점**을 의미한다. 핵심 기능과 공통 기능이 어느지점에서 만나는지를 의미하며 메서드 호출, 필드값 **변경 지점**을 의미한다.
`Pointcut` | 많은 `JoinPoint`중에 실제 핵심기능과 보조기능이 **접하게 되는 지점**을 `Pointcut`이라 한다. `JoinPoint`의 부분집합을 `Pointcut`이라 할 수 있다.
`Weaving` | 프로그래머가 설정한 `Advice`대로 `Aspect`를 핵심로직 코드에 적용하는 것, **행위를 뜻한다.**

### 구현되는 Advice의 종류

**Advice명** | **설명** | **사용 클래스**
|--|--|--|
`Before Advice` | **핵심기능 전**에 수행하는 공통 기능 (로그인 체크 등) | `MethodBeforeAdvice`
`Around Advice` | **핵심기능 수행 전 후**에 수행하는 고통 기능 (시간 체크 등) | `MethodInterceptor`  
`After Returning Advice` | **예외 발생이 없을 경우** 수행하는 기능 | `AfterReturningAdvice`
`After Throwing Advice` | **예외 발생할 경우** 수행하는 기능 | `ThrowsAdvice`  
`After Advice` | **예외 발생 상관없이 핵심기능 수행 후 수행**하는 공통 기능 |  `AfterAdvice`

공통기능이 핵심기능 어느시점에 실행 되는지에 따라 위 5가지 `Advice`를 택해야 한다.  

### Weaving하는 시점

다음 3가지 시점에 `Weaving(적용)`한다.  

1. 컴파일시  
2. 클래스 로딩시  
3. 런타임시  

> `Spring AOP` 에선 3번째 방법인 런타임시에 `Weaving`할 수 있는데 **Proxy기반의 AOP**이다.  

![image4](/assets/springboot/springboot_aop2.png)  

> `crossCuttingConcern`: 공통 관심사항  
> `coreConcern`: 핵심 기능  

핵심 기능을 하는 클래스, 보조(공통)기능을 하는 클래스가 있으면 따로 2개의 클래스가 올라가고  
가상으로 만들어진 프로그래머가 설정한 대로 위의 여러 클래스(핵심, 보조 클래스들)가 적용(Weaving)된 **Proxy클래스(가짜)** 가 만들어진다.  

이러한 Proxy 클래스를 만드는 방법은 다음 3가지가 있다.  

1. XML스키마 기반의 POJO 클래스를 이용한 AOP구현  
2. AspectJ에서 정의한 @Aspect 어노테이션 기반의 AOP구현  
3. 스프링API를 이용한 AOP구현  

이중 XML스키마를 이용한 1번과 어노테이션을 사용한 2번을 알아보자

### Proxy 객체 만들어보기

위의 3가지 방법은 스프링에서 제공하는 기능을 이용해 **Proxy클래스**를 만드는 것 인데  
어떻게 Proxy 클래스가 만들어지는지 알기 위해 라이브러리를 사용하지 않고 Proxy클래스를 만들어 보자.  

- 핵심기능을 계산기 기능  
- 공통기능을 핵심기능이 처리되는 시간을 로그로 기록  

먼저 계산기 뼈대를 가진 `Calculator` 인터페이스 작성

```java
public interface Calculator {
    int add(int x, int y);
    int sub(int x, int y);
    int mult(int x, int y);
    int div(int x, int y);
}
```

그리고 이를 구현한 `CalcultorImpl` 클래스 작성

```java
@Component
public class CaculatorImpl implements Calculator {
    @Override
    public int add(int x, int y) {
        Log log = LogFactory.getLog(this.getClass());
        StopWatch sw = new StopWatch();
        log.info("add() start");
        sw.start();
        int result = x + y;
        sw.stop();
        log.info("add() stop");
        log.info("add() 처리시간: " + sw.getTotalTimeMillis());
        return result;
    }

    @Override
    public int sub(int x, int y) {
        Log log = LogFactory.getLog(this.getClass());
        StopWatch sw = new StopWatch();
        log.info("sub() start");
        sw.start();
        int result = x - y;
        sw.stop();
        log.info("sub() stop");
        log.info("sub() 처리시간: " + sw.getTotalTimeMillis());
        return result;
    }
    
    @Override
    public int mult(int x, int y) {
        int result = x * y; return result;
    }

    @Override
    public int div(int x, int y) {
        int result = x / y; return result;
    }
}

public static void main(String[] args) {
    Calculator calc = new CaculatorImpl();
    System.out.println(calc.add(4,2));
    System.out.println(calc.sub(4,2));
}
// CaculatorImpl add
// add() start
// CaculatorImpl add
// add() stop
// CaculatorImpl add
// add() 처리시간: 0
// 6
// CaculatorImpl sub
// sub() start
// CaculatorImpl sub
// sub() stop
// CaculatorImpl sub
// sub() 처리시간: 0
// 2
```

로그출력같은 **공통적인 보조업무**를 빼서 AOP 기능수행을 하는 **Proxy 클래스**를 생성  

`java.lang.reflect.InvocationHandler` 인터페이스를 상속한 `LogPrintHandler`클래스를 정의  
공통 기능을 구현한 보조클래스와 핵심 기능이 합쳐진 **proxy 클래스** 이다.  

```java
public class LogPrintHandler implements InvocationHandler {
    private Object target;
    public LogPrintHandler(Object target) {  
        this.target = target;
    } 
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 보조 기능1
        Log log = LogFactory.getLog(this.getClass());
        StopWatch sw = new StopWatch();
        log.info(method.getName() + "() start"); //method 이름을 가져오는 메서드
        sw.start();
        int result = (int) method.invoke(target, args); // 핵심기능 수행
        // 보조 기능2
        sw.stop();
        log.info(method.getName() + "() stop");
        log.info(method.getName() + "() 처리시간: " + sw.getTotalTimeMillis());
        return result;
    }
}
```

`AOP`과정에서 위와같은 `LogPrintHandler`와 같은 **java.lang.reflect.Proxy** 인스턴스가 생기게 된다 보면 된다.  

```java
public static void main(String[] args) {
    Calculator target = new CaculatorImpl();
    Calculator calc_proxy = (Calculator)Proxy.newProxyInstance(
        target.getClass().getClassLoader(),
        target.getClass().getInterfaces(),
        new LogPrintHandler(target));
    System.out.println(calc_proxy.add(1, 2));
}
```

## Spring AOP

`런타임 AOP` 에서 사용하는 방법은 아래 2가지  

- `JDK Dynamic Proxy`
- `cglib(Code Generator Library) Proxy`

`JDK Dynamic Proxy` 는 위에서 살펴보았던 reflect 패키지의 클래스를 사용하는 방법이다.  

- `java.lang.reflect.Proxy`  
- `java.lang.reflect.InvocationHandler`  

`cglib` 는 바이트코드 조작을 통해 프록시 객체를 생성하기때문에 `reflect` 패키지의 인터페이스 구현 등의 별도의 추가작업을 하지 않아도 Proxy 패턴 구현이 가능하다.  

- `org.springframework.cglib.proxy.Enhancer`  
- `org.springframework.cglib.proxy.MethodInterceptor`  

`Spring AOP` 에선 `cglib Proxy` 를 사용한다.  
런타임시에 각종 어노테이션이 설정된 클래스들을 `classpath` 에서 찾은 뒤 `cglib` 클래스를 사용하여 동적으로 프록시 관련 코드를 생성 후 로직 사이사이에 삽입하는 과정을 거친다.  

### cglib Proxy 객체 생성

Spring AOP 를 사용하면 지금부터 소개할 `cglib` 클래스들이 자동으로 런타임시 생성된다고 보면 된다.  

하지만 여기선 Spring AOP 를 사용하기 전에 직접 `cglib` 를 클래스를 생성하고 Bean 으로 등록하는 과정을 수행한다.  

`cglib` 클래스를 사용하기 위해 아래 `dependency` 를 삽입.  

```groovy
implementation 'org.springframework.boot:spring-boot-starter-aop'
```

`add` 라는 **핵심 기능 전 후** 에 아래 3가지 AOP 작업을 수행하도록 설정한다.  

- 실행 전 로그기록  
- 실행 후 로그기록  
- 소요시간 로그 기록   

공통 기능인 `Around Advice` 역할을 수행할 `cglib Proxy` 클래스로 생성한다.  

**`Around Advice`기능을 가진 Proxy클래스를 정의하려면** 먼저 공통기능 클래스를 정의하기 위한 **`MethodInterceptor` 인터페이스를 구현**해야한다.  

```java
@Component
public class LogPrintAroundAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation method) throws Throwable {
        String m_name = method.getMethod().getName();
        Log log = LogFactory.getLog(this.getClass());
        StopWatch sw = new StopWatch();
        log.info(m_name + " start");
        sw.start();
        Object result = method.proceed();
        sw.stop();
        log.info(m_name + " stop");
        log.info(m_name + " 처리시간: " + sw.getTotalTimeMillis());
        return result;
    }
}
```

`Around Advice`을 통해 핵심기능 함수 실행문으로 들어온 후 시작 바로후, 끝나기 바로 전에 공통기능 삽입이 가능하다.  

만약 핵심기능 실행 전, 혹은 후에 공통기능을 먼저 삽입하고 싶다면,  
핵심기능 수행전 매개변수, 핵심기능 수행 후 반환값을 사용해 공통 기능을 수행해야 한다면  

`Before Advice` 역할을 하는 `MethodBeforeAdvice` 인터페이스를 구현,  
`After Returning Advice`역할을 하는 `AfterReturningAdvice` 인터페이스를 구현한 공통 기능 클래스를 정의해야 한다.  

그냥 Spring 프레임워크를 사용하게 될 경우 

위와 같이 AOP 기법을 사용한 공통 기능 클래스를 모두 정의하였으면 **핵심기능 클래스와 공통기능 클래스를 하나의 Proxy클래스**로 만들어주면 된다.  

### Spring AOP 라이브러리


`cglib` 클래스를 사용하여 `AOP` 를 사용하는 방법을 알아보았는데 스프링에선 대부분 어노테이션을 사용해서 `AOP` 를 수행한다.  

```java
@Aspect
@Configuration
@RequiredArgsConstructor
public class LoggingAspect {

    /**
     * Retrieves the {@link Logger} associated to the given {@link JoinPoint}.
     *
     * @param joinPoint join point we want the logger for.
     * @return {@link Logger} associated to the given {@link JoinPoint}.
     */
    private Logger logger(JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
    }

    /**
     * Pointcut that matches all repositories, services and Web REST endpoints.
     */
    @Pointcut("within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut, the implementations are in the advices.
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint join point for advice.
     * @return result.
     * @throws Throwable throws {@link IllegalArgumentException}.
     */
    @Before("springBeanPointcut()")
    public void logBefore(JoinPoint joinPoint) throws Throwable {
        Logger log = logger(joinPoint);
        log.info("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }

}
```

#### AOP Advice annotation

AOP Advice 를 설정할 수 있는 어노테이션은 아래 5가지


**@Before**  

```java
@Before("execution(* com.example.service.*.*(..))")
public void beforeAdvice(JoinPoint joinPoint) {
    // 로직
}
```

**@After**

```java
@After("execution(* com.example.service.*.*(..))")
public void afterAdvice(JoinPoint joinPoint) {
    // 로직
}
```

**@AfterReturning**

```java
@AfterReturning(pointcut = "execution(* com.example.service.*.*(..))", returning = "result")
public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
    // 로직
}
```

**@AfterThrowing**

```java
@AfterThrowing(pointcut = "execution(* com.example.service.*.*(..))", throwing = "error")
public void afterThrowingAdvice(JoinPoint joinPoint, Throwable error) {
    // 로직
}
```

**@Around**

```java
@Around("execution(* com.example.service.*.*(..))")
public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    // 메서드 호출 전 로직
    Object result = joinPoint.proceed();
    // 메서드 호출 후 로직
    return result;
}
```



```java
// org.springframework.aop.AfterReturningAdvice 클래스 상속
@Component
public class LogPrintAfterReturningAdvice implements AfterReturningAdvice {

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        // 수행이 완료된 후 호출되는 Advice임으로 첫번째 매개변수로 method가 반환한 값이 들어간다.
        String m_name = method.getName();
        Log log = LogFactory.getLog(this.getClass());
        log.info("<<<  " + m_name + "() : AfterReturningAdvice called...");
    }
}
```

#### 직접 어노테이션 생성  

```java
@Inherited // 자식 클래스에도 상속
@Retention(value = RetentionPolicy.RUNTIME) // 어노테이션 보존 라이프타임, 런타임에도 유지
@Target(ElementType.METHOD)
public @interface CheckApiKey {
}
```

### Spring AOP 의 문제점

- Proxy 객체의 외부 호출을 래핑하는 방식이기 때문에 내부 함수호출시에는 AOP 과정이 일어나지 않는다.  
- 추가적인 클래스를 다량 작성해야한다.  

컴파일 타임에선 어노테이션 사용의 문제가 있어도 알수 없어 런타임시에 확인이 필요하다.  

빌드시 어노테이션 오류를 체크하기 위해 `Annotation Processor` 를 사용할 수 있다.  

> <https://kouzie.github.io/cicd/gradle/gradle-Plugin,-buildSrc/#annotation-processor>