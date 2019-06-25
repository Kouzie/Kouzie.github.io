---
title:  "Spring - Spring AOP, Spring MVC!"

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

## 스프링 AOP

스프링AOP를 사용하지 않고 AOP기법을 사용하기 위해 각각 `MethodBeforeAdvice`, `AfterReturningAdvice`, `MethodInterceptor` 3개의 인터페이스를 구현한 클래스를 정의하였지만  

스프링AOP를 사용하면 태그를 사용용하면 하나의 클래스로 모두 구현할 수 있다.  

AOP스프링 프레임웤 안에선 **메서드 단위로 Advice를 정의**할 수 있기 때문에 상관 3개의 역할을 가지는 메서드를 가지고 있는 클래스 하나만 정의하면 된다.

`aopaliance-1.0.0.jar` 파일에 더불어 

`spring-framework-3.0.2.RELEASE-dependencies\org.aspectj\com.springsource.org.aspectj.weaver\1.6.8.RELEASE` 폴더 위치의 jar `com.springsource.org.aspectj.weaver-1.6.8.RELEASE.jar`파일 추가


먼저 스프링 AOP를 사용하지 않고 Proxy객체를 정의하려면 아래와 같이 xml설정을 하였다.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd"
	>
	<context:component-scan base-package="aop"/>	
	<!-- Calculator proxy = (Calculator)Proxy.newProxyInstance(
				target.getClass().getClassLoader(),
				target.getClass().getInterfaces(),
				new LogPrintHandler(target)); 이 과정과 같다.-->
	<bean id="proxy" class="org.springframework.aop.framework.ProxyFactoryBean">
		<property name="target"><!-- ProxyFactoryBean의 setTarget() 메서드 호출 -->
			<ref bean="calc"/>
		</property>
		<property name="proxyInterfaces"><!-- ProxyFactoryBean의 setProxyInterfaces() 메서드 호출 -->
			<list>
				<value>aop.Calculator</value>
			</list>
		</property>
		<property name="interceptorNames"><!-- ProxyFactoryBean의 setInterceptorNames() 메서드 호출 -->
			<list>
				<value>logPrintAroundAdvice</value>
				<value>logPrintBeforeAdvice</value>
				<value>logPrintAfterReturningAdvice</value>
			</list>
		</property>
	</bean>
</beans>
```

스프링 AOP로 다시 구현해보자.  

먼저 aop네임스페이스를 사용하기 위해 `<beans>` 태그에 다음 속성 추가  
`xmlns:aop="http://www.springframework.org/schema/aop"`,   
`xsi:schemaLocation=http://www.springframework.org/schema/aop`    

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/aop
	http://www.springframework.org/schema/aop/spring-aop.xsd">

	<context:component-scan base-package="aop"></context:component-scan>

	<!-- xml 기반 aop 설정 부분 -->
	<bean id="logPrintProfiler" class="aop.LogPrintProfiler"></bean>
	<aop:config>
		<aop:aspect id="traceAspect" ref="logPrintProfiler">
			<aop:pointcut expression="execution(public * aop..*(*,*))" id="publicMethod"/>  <!-- pointcut의 id는 마음대로 -->
			<aop:around pointcut-ref="publicMethod" method="trace"/>
			<aop:before pointcut-ref="publicMethod" method="before"/>
			<aop:after pointcut-ref="publicMethod" method="afterFinally"/>
		</aop:aspect>
	</aop:config>
</beans>
```
코드를 보면 `<bean>`태그를 사용해 `logPrintProfiler` 스프링 빈 객체를 생성.  

`<aop:config>` 태그를 통해 스프링 AOP기법을 사용한 **Proxy객체를 정의**한다.

`<aop:aspect>` 태그를 통해 공통기능과 핵심기능이 있는 **proxy객체 생성.**  

`<aop:pointcut id="publicMethod" expression="execution(public * aop..*(*,*))">`   
`Pointcut` 설정, `id` 속성은 `Pointcut`을 구분하는 데 사용하는 식별 값  
`expression` 속성을 통해 설정한 `Advice`를 어떤 핵심기능 어느위치에 적용할지 설정.  

> `public * aop..*(*,*)`:  `public` 접근 지정자의 `return type` 상관없이 aop패키지 안의 (..*하위포함) 매개변수 2개의 함수에 대해 다음 공통기능을 모두 사용.  

`Pointcut`을 설정하고 `<aop:around>`, `<aop:before>`, `<aop:after>`등 의 태그를 통해 어느 시점(`Advice`)에서 공통기능(`Aspect`)를 실행시킬지 설정한다.  

**태그** | **설명**
|--|--|
`<aop:before>` | 메서드 실행 전에 적용되는 Advice를 정의
`<aop:after-returning>` | 메서드가 정상적으로 실행 후 적용되는 Advice를 정의
`<aop:after-throwing>` | 메서드가 예외를 발생시킬 때 적용되는 Advice를 정의 (catch같은 역할)
`<aop:after>` | 예외발생 여부 상관없이 적용되는 Advice를 정의 (finally같은 역할)
`<aop:around>` | 메서드 호출 이전, 이후, 예외 발생 등 모든 시점에 적용 가능한 Advice를 정의

<br><br>

`logPrintProfiler` 스프링 빈 객체를 다음과 같이 정의

```java
// 공통 기능을 구현한 클래스: Aspect    <aop:aspect></aop:aspect>
public class LogPrintProfiler {

	// After Advice
	public void afterFinally(JoinPoint joinPoint) {
		String m_name = joinPoint.getSignature().getName();
		System.out.println("> after advice: " + m_name + "() 호출");
	}
	
	// Before Advice
	public void before(JoinPoint joinPoint) {
		// JoinPoint joinPoint 실제 대상 객체의 메소드 정보 또는 매개변수값을 얻어올 수 있다
		// joinPoint.getTarget();
		// joinPoint.getArgs();
		String m_name = joinPoint.getSignature().getName();
		System.out.println("> before advice: " + m_name + "() 호출");
	}
	
	// Around Advice
	public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
		String signatureString = joinPoint.getSignature().toShortString();
		System.out.println(signatureString + " 시작");
		long start = System.currentTimeMillis();
		
		try {
			Object result = joinPoint.proceed();	// 실제 객체의 메소드(주요 기능) 실행
			return result;
		} finally {
			long finish = System.currentTimeMillis();
			System.out.println(signatureString + " 종료");
			System.out.println(signatureString + " 실행 시간 : " + (finish - start) + "ms");
		}
	}
}
```
AOP에선 `JoinPoint`인터페이스를 구현한 객체를 항상 첫번째 파라미터로 지정해야 오류가 발생하지 않는다.

`JoinPoint` 인터페이스는 아래 3개 함수를 통해 호출되는 대상 객체, 메서드 그리고 전달되는 파라미터 목록에 접근할 수 있는 메서드를 제공  

`Signature getSignature( )` - 호출되는 메서드에 대한 정보를 구함  

`Object getTarget( )` - 대상 객체를 구함  

`Object[ ] getArgs( )`- 파라미터 목록을 구함  


<br><br>

`Signature` 인터페이스를 통해 호출되는 메서드와 관련된 정보를 구할 수 있다.   

`String getName( )` - 메서드의 이름을 구함  

`String toLongName( )` - 메서드를 완전하게 표현한 문장을 구함(메서드의 리턴 타입, 파라미터 타입 모두 표시)  

`String toShortName( )` - 메서드를 축약해서 표현한 문장을 구함(메서드의 이름만 구함)  

<br>
### 기타정보

만약 `After Returning Advice`에서 반환된 값을 가지고 공통기능을 수행하고 싶다면 `Object` 타입의 매개변수를 추가하자.
```java
...
public void afterReturning(Object ref) {
	...
	//ref를 통해 반환된값 접근
}
```

특정 타입의 반환값만 공통 기능으로 수행시키고 싶다면 `Object` 타입 대신 별도의 타입을 주어도 된다.  

만약 호출 메서드의 정보나 전달되는 파라미터값도 알아야 한다면 `JoinPoint`객체와 `Object` 총 2개의 매개변수를 사용

<br><br>

`getArgs( )`메서드를 통해 파라미터 목록을 구할 수 있지만 xml설정을 통해 매개변수로 전달받을 수 도 있다.  

```xml
<aop:config>
	<aop:aspect id="traceAspect" ref="logPrintProfiler">
		<aop:pointcut expression="execution(public * aop..*(*,*))" id="publicMethod"/>
		<aop:around pointcut-ref="publicMethod" method="trace"/>
		<aop:before pointcut-ref="publicMethod" method="before"/>
		<aop:after pointcut-ref="publicMethod" method="afterFinally"/>
	</aop:aspect>
</aop:config>
```

### Arround Advice


`Around Advice`로 `Before`, `After`, `After Returning`, `After Throwing Advice`들을 모두 구현할 수 있으며 다른 `Advice`과는 다르게 첫번째 매개변수로 `ProceedingJoinPoint`를 사용한다.  
`ProceedingJoinPoint`는 `JoinPoint`의 자식 클래스로 `Signature`, 타겟, 파라미터 목록을 구할수 있을 뿐 아니라  
대상 객체를 호출할 수 있는 `proceed()` 메서드를 제공 한다.  

`Around Advice` 를 구현할때 `ProceedingJoinPoint`를 매개변수 타입으로 사용하지 않으면 에러 발생한다.  


구현할 Advice에 따라 `proceed()` 메서드 호출 지점을 정하면 된다.  

```java
public class LogPrintProfiler {
	...
	...	
	// Around Advice
	public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
		String signatureString = joinPoint.getSignature().toShortString();
		System.out.println(signatureString + " 시작");
		long start = System.currentTimeMillis();
		
		try {
			Object result = joinPoint.proceed();	// 실제 객체의 메소드(주요 기능) 실행
			return result;
		} finally {
			long finish = System.currentTimeMillis();
			System.out.println(signatureString + " 종료");
			System.out.println(signatureString + " 실행 시간 : " + (finish - start) + "ms");
		}
	}
}
```

### Aspect 어노테이션 기반 AOP

공통기능(`Aspect`)을 정의한 클래스에서 어노테이션을 사용하면 xml에 설정한 `aop`네임스페이스 태그 생략이 가능하다. 

`@Aspect`, `@Poingcut("expression")`, `@After`, `@Before`, `@Arround` 등 어노테이션을 사용해서 위의 xml을 대타가능

```java
@Component
@Aspect
public class LogPrintProfiler {

	@Pointcut("execution(public * aop..*(*,*))")
	public void profileTarget() {}
	
	// After Advice
	@After("profileTarget()")
	public void afterFinally(JoinPoint joinPoint) {
		String m_name = joinPoint.getSignature().getName();
		System.out.println("> after advice: " + m_name + "() 호출");
	}
	
	// Before Advice
	@Before("profileTarget()")
	public void before(JoinPoint joinPoint) {
		// JoinPoint joinPoint 실제 대상 객체의 메소드 정보 또는 매개변수값을 얻어올 수 있다
		// joinPoint.getTarget();
		// joinPoint.getArgs();
		String m_name = joinPoint.getSignature().getName();
		System.out.println("> before advice: " + m_name + "() 호출");
	}
	
	// Around Advice
	@Around("profileTarget()")
	public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
		String signatureString = joinPoint.getSignature().toShortString();
		System.out.println(signatureString + " 시작");
		long start = System.currentTimeMillis();
		
		try {
			Object result = joinPoint.proceed();	// 실제 객체의 메소드(주요 기능) 실행
			return result;
		} finally {
			long finish = System.currentTimeMillis();
			System.out.println(signatureString + " 종료");
			System.out.println(signatureString + " 실행 시간 : " + (finish - start) + "ms");
		}
	}
}
```
코드를 보면 `	@Pointcut("execution(public * aop..*(*,*))")`을 통해 `Pointcut`을 정의하고 각 Advie설정 괄호 안에 Pointcut 식별자를 값으로 넣는다.  
`	@After("profileTarget()")`

`@Aspect`어노테이션을 가진 스프링 빈 객체 (`Aspect`객체)를 xml설정을 통해 생성하려면 `<aop:aspectj-autoproxy/>`태그를 추가로 적용해야 한다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/aop
	http://www.springframework.org/schema/aop/spring-aop.xsd">

	<aop:aspectj-autoproxy/>
	<context:component-scan base-package="aop"></context:component-scan>
</beans>
```

xml설정이 아닌 `@Configuration`을 통한 자바 설정 파일을 사용할 경우 `<aop:aspectj-autoproxy/>`대신 `@EnableAspectJAutoProxy` 어노테이션을 사용  

```java
@Aspect
public class LogPrintProfiler {
	...
	...
}

---

@Configuration
@EnableAspectJAutoProxy
public class Config {
	@Bean
	public LogPrintProfiler logPrintProfiler() {
		return new LogPrintProfiler();
	}

	@Bean
	public RecordImpl record() {
		return new RecordImpl();
	}
	
	@Bean(name="service") 
	public RecordViewImpl getService() {
		return new RecordViewImpl();
	}
}

```

### AspactJ 표현식

어떤 클래스들에 `Aspect(공통기능)`클래스를 접합해 Proxy 객체로 만들지 범위를 지정하기 위해 사용하는 `AspactJ` 표현식에 대해 알아보자.

`<aop:pointcut id="publicMethod" expression="execution(public * aop..*(*,*))">`

`excution` 괄호 안에 들어가는 조합의 기본 형식은 아래와 같다.  

`excution(수식어패턴? 리턴타입 클래스이름패턴?메서드이름패턴(파라미터패턴))`

> 파라미터패턴 - 매칭될 파라미터에 대해 명시함.
`*` 을 이용해서 모든 값을 표현할 수 있다.
`..` 을 이용해서 0개 이상이라는 의미를 표현할 수 있다.  

`execution( public void set*(..) )`  
* 수식어패턴 : public  
* 리턴타입 : void  
* 클래스이름 : 생략됨  
* 메서드이름 : setter (set으로 시작하는 메서드)  
* 파라미터패턴 : 파라미터가 0개 이상 '(..)'  



`execution( * chap07.*.*() )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : *   ← 전부  
* 클래스이름 : chap07.*  ← chap07패키지에 있는 모든 클래스. chap07 하위패키지까지는 아님. (chap07 : 패키지명.)  
* 메서드이름 : *   ← 전부  
* 파라미터패턴 : 공란  ← 없음  



`execution( * chap07..*.*(..) )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : *   ← 전부  
* 클래스이름 : chap07..*  ← chap07패키지 및 해당 패키지 하위의 모든 클래스 (chap07 : 패키지명.)  
* 메서드이름 : *   ← 전부  
* 파라미터패턴 : (..)  ← 0개 이상  



`execution( Long chap07.Calculator.factorial(..) )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : Long  
* 클래스이름 : chap07.Calculator  
* 메서드이름 : factorial  
* 파라미터패턴 : (..)  ← 0개 이상  



`execution( * get*(*) )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : *   ← 전부  
* 클래스이름 : 생략됨  
* 메서드이름 : getter (get으로 시작하는 메서드)  
* 파라미터패턴 : *  ← 1개  



`execution( * get*(*, *) )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : *   ← 전부  
* 클래스이름 : 생략됨  
* 메서드이름 : getter (get으로 시작하는 메서드)  
* 파라미터패턴 : *, * ← 2개  



`execution( * read*(Integer, ..) )`  
* 수식어패턴 : 생략됨  
* 리턴타입 : *   ← 전부  
* 클래스이름 : 생략됨  
* 메서드이름 : read로 시작하는 모든 메서드  
* 파라미터패턴 : Integer, ..  ← 1개 이상의 파라미터를 가짐. 첫번째 파라미터형은 Integer형이여야 함  

> 출처: https://m.blog.naver.com/PostView.nhn?blogId=imf4&logNo=220697094435&proxyReferer=https%3A%2F%2Fwww.google.com%2F

### Pointcut의 조합


```xml
<aop:pointcut id="publicMethod" expression="execution(public * aop..*(*,*))">
```

```java
@Aspect
public class LogPrintProfiler {

	@Pointcut("execution(public * aop..*(*,*))")
	public void profileTarget() {}
	...
	...
}
```
`<aop:pointcut>`태그,  `@Pointcut` 어노테이션을 보면

접근지정자가 `public`이고, 매개변수, 몸체가 없다.  
함수명은 `Poingcut`의 식별자로 사용할 수 있다.  

각각의 표현식은 `&&` 또는 `||` 연산자를 이용하여 연결 가능  
XML 설정파일에 한해서는 스프링에서 `and`, `or`를 사용 허용함(`&&`를 `&amp;&amp;`로 써야 하는 불편함이 있기 때문)  


```java
@Pointcut("execution( * get*(*) )")
public void getterMethod() {}

@Pointcut("execution( public void set*(..) )")
public void setterMethod() {}

@Pointcut("execution( * get*(*) ) || execution( public void set*(..) )")
public void getsetMethod() {}
```
위와 같이 2개의 `Pointcut`을 정의했을때 이 두개를 합친 `Pointcut`을 정의할 수 있다.  

혹은 아래와 같이 설정도 가능하다.  
```java
@Pointcut("getterMethod() || setterMethod()")
public void getsetMethod() {}
```

xml설정파일에서도 조합이 가능하다.  

`<aop:pointcut id="publicMethod" expression="execution( * get*(*) ) or execution( public void set*(..) )">`

xml문서에선 `||`표시하려면 &#124;&#124;를 사용해야 하는데 번거로움으로 xml 한해서는 `or`문자열을 사용 가능하다.  


### Advice 적용 순서

한 `JoinPoint`에 한 개 이상의 `Advice`가 적용될 경우, 순서를 명시적으로 저장할 수 있다.  

1. `@Order` 애노테이션 적용  
```java
@Aspect
@Order(2)
public class LogginAspect { ... }
```

2. `Ordered` 인터페이스 구현 - `getOrder()` 메소드를 구현해서 `Advice` 적용 순서 값을 리턴  
```java
@Aspect
public class LogginAspect implements Ordered {
	private int order = 1;
	public int getOrder() {
		return this.order;
	}
}
```

XML 스키마 사용 시, `<aop:aspect>` 태그의 `order` 속성을 사용해서 `Advice` 순서 지정

`order`는 값이 낮을수록 높은 우선순위의 `Advice`


## 스프링MVC

기존에 `JSP/Servlet`에서 사용하던 `Model2`패턴 `MVC`와  
스프링 프레임워크에서 사용하는 `MVC` 여러 차이점이 있다.  

**JSP/Servlet** | **Spring Framework**
|---|---|
`Controller` | `Front Controller`
`Handler` | `Controller`
`View` | `View`
`Spring`에선 보통 `maven` 빌드관리를 사용한 프로젝트를 생성하지만 전체적인 구조를 알기 위해 먼저 `eclipse`에서 `Dynamic web project`로 스프링 프레임워크를 사용해보자.  

### 스프링 MVC 구성 요소

![image6]({{ "/assets/Spring/image6.png" | absolute_url }}){: .shadow}  

**요소**|**역할**
:-----:|:-----:
`DispatcherServlet`|브라우저가 보낸 **요청을 일괄 관리**하는 `FrontController`, 결과값을 `view`에 전달하여 알맞은 응답을 생성.`View에` 전달하여 알맞은 응답을 생성하도록 한다.
`HandlerMapping`|요청URL과 `Controller`클래스를 **맵핑**
`HandlerAdapter`|`DispatcherServlet`의 처리 요청을 변환해서 컨트롤러에게 전달하고, 컨트롤러의 응답 결과를 **`DispatcherServlet`이 요구하는 형식으로 변환**한다, 웹 브라우저 캐시 등의 설정도 담당(실제 일을 시키는 역할)
`Controller(컨트롤러)`|클라이언트의 요청을 처리(비즈니스 로직-`Service`을 호출), 결과를 리턴한다. 응답 결과에서 보여줄 데이터를 모델에 담아 전달한다.
`ModelAndView`|컨트롤러가 처리한 결과 정보 및 뷰 선택에 필요한 정보를 담는다.
`ViewResolver`|컨트롤러 클래스가 반환한 뷰 이름으로 이동할 뷰를 결정, 컨트롤러의 처리 결과를 보여줄 뷰를 결정한다.
`View(뷰)`|JSP를 이용하여 웹브라우저에 컨트롤러의 처리 결과 화면을 생성한다.


### 스프링 MVC를 위한 설정을 web.xml에 추가하기  

스프링 MVC를 사용하기 위해 아래 기본 설정은 반드시 설정되고 개발과정이 시작된다.  

1. web.xml에 DispatcherServlet 설정  
2. web.xml에 캐릭터 인코딩 처리 위한 필터 설정  
3. 스프링 MVC 설정  
  A. Handlermapping, HandlerAdapter 설정  
  B. ViewResolver 설정  

### web.xml에 DispatcherServlet 설정 

`java` 프로젝트에선 xml설정파일 혹은 자바코드 설정파일과 `GenericXmlApplicationContext`, `AnnotationConfigApplicationContext` 객체를 통해 스프링 컨테이너를 생성했다면  

웹 프로젝트에선 `org.springframework.web.servlet.DispatcherServlet` 객체를 통해 스프링 컨테이너를 생성한다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
		http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	metadata-complete="true" 
	version="3.1">
	<display-name>Spring Web Project</display-name>
	<description>
		Welcome to Tomcat
	</description>
	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>*.htm</url-pattern>
	</servlet-mapping>
</web-app>
```

`dispatcher`란 이름으로 서블릿 객체를 생성하였는데 스프링 컨테이너를 생성할 때 필요한 설정파일의 이름이 `DispatcherServlet` 객체를 생성할 때 사용한 식별자 이름을 사용해야 한다.  

`dispatcher`란 이름을 사용하면 스프링 컨테이너 생성시 필요한 설정파일을 `dispatcher-servlet.xml` 이란 xml파일로 관리한다.  
> `/WEB-INF/` 안에 위치한 `[서블릿이름]-servlet.xml` 파일

만약 설정파일을 1개 이상 사용하고 싶거나 설정 파일명을 식별자에 따르게 하고 싶지 않다면 다음과 같이 설정.

```xml
...
...
<servlet>
	<servlet-name>dispatcher</servlet-name>
	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
	<load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
	<servlet-name>dispatcher</servlet-name>
	<url-pattern>*.htm</url-pattern>
</servlet-mapping>
<init-param>
	<param-name>contextConfigLocation</param-name>
	<param-value>
		/WEB-INF/contextConfig1.xml
		/WEB-INF/contextConfig2.xml
		/WEB-INF/contextConfig3.xml
	</param-value>
</init-param>
...
...
```

`<param-value>` 태그에서 xml경로를 설정할 때 구분자로 콤자, 공백, 탭, 개행, 세미콜론 을 사용할 수 있다.

`dispatcher`를 위한 xml설정파일을 다음과 같이 설정  
`contextConfigLocation`라는 파라미터를 쓰면, `Context Loader`가 `load`할 수 있는 설정파일을 여러개 쓸 수 있다.

>`dispatcher-servlet.xml`는 `spring-framework-3.0.2.RELEASE\docs\spring-framework-reference\pdf` 파일에서 450페이지로 참고  
```xml
<!-- dispatcher-servlet.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns:p="http://www.springframework.org/schema/p"
xmlns:context="http://www.springframework.org/schema/context"
xsi:schemaLocation="
http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
http://www.springframework.org/schema/context
http://www.springframework.org/schema/context/spring-context-3.0.xsd">
   <bean id="noticeDao" class="newlecture.dao.NoticeDao"></bean>
   <bean name="/customer/notice.htm" class="controllers.customer.NoticeController">
     <property name="noticeDao" ref="noticeDao"></property>
   </bean>
   <bean name="/customer/noticeDetail.htm" class="controllers.customer.NoticeDetailController">
     <property name="noticeDao" ref="noticeDao"></property>
   </bean>
</beans>
```
형식은 java프로젝트에서 사용하던 xml형식과 같다.(`<bean>`태그로 스프링 빈 객체 생성, `<property>`태그로 의존설정 등)  

만약 `dispatcher-servlet.xml`과 같은 xml설정파일이 아닌 자바 코드기반 설정파일을 `DispatcherServlet`객체 생성시 설정파일로 사용하고 싶다면 `web.xml`에 아래와 같이 설정

```xml
<!-- web.xml -->
<servlet>
	<servlet-name>dispatcher</servlet-name>
	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
	<load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
	<servlet-name>dispatcher</servlet-name>
	<url-pattern>*.htm</url-pattern>
</servlet-mapping>
<init-param>
	<param-name>contextClass</param-name>
	<param-value>org.springframework.context.annotation.AnnotationConfigApplicationContext</param-value>
</init-param>
<init-param>
	<param-name>contextConfigLocation</param-name>
	<param-value>config.ConfigLocation</param-value>
</init-param>
```

`DispatcherServlet`은 기본적으로 스프링 컨테이너를 생성할 때 설정을 통해 사용할 구현 클래스로 `org.springframework.web.context.support.XmlWebApplicationContext`을 사용하는데 이를 `@Configuration` 어노테이션을 사용한 자바코드 설정파일로 구현하고 싶다면 구현클래스를 위와같이 `org.springframework.context.annotation.AnnotationConfigApplicationContext` 클래스로 바꿔 주어야 한다.  

### 스프링 `CharacterEncodingFilter`

`JSP/Servlet`에선 http프로토콜을 요청받는 문자열 인코딩을 `utf-8`로 사용하기 위해 별도로 `CharacterEncodingFilter` 필터클래스를 생성해 샤용 하였다.

> https://kouzie.github.io/jsp/JSP-필터/#webxml에-필터-매핑

스프링 프레임워크에서 `CharacterEncodingFilter`를 제공함으로 `web.xml`에 설정만 하면 된다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://xmlns.jcp.org/xml/ns/javaee"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
		http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	metadata-complete="true" version="3.1">
	...
	...
	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
	...
	...
</web-app>
```

### 스프링 MVC 설정 파일 작성

`JSP/Servlet`에서는 하나의 작업(글쓰기, 글목록 보기, 글 상세 보기 등)에 하나의 핸들러가 매칭되서 작업했었다.    
> https://kouzie.github.io/jsp/JSP-MVC패턴/#커맨드-패턴-기반-컨트롤러   
`CommandHandler`라는 인터페이스를 구현한 핸드러들은 `process()` 메서드를 오버라이딩 하고 이동할 `view` 파일명을 전달한다.   

스프링도 위와 같이 하나의 작업에 하나의 컨트롤러를 등록해 사용할 수 있다.  

> 스프링에선 행위를 처리하는 객체를 컨트롤러라 부른다. `핸들러 → 컨트롤러`

```xml
<!-- dispatcher-servlet.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	...
	...>
   <bean id="noticeDao" class="newlecture.dao.NoticeDao"></bean>
   <bean name="/customer/notice.htm" class="controllers.customer.NoticeController">
     <property name="noticeDao" ref="noticeDao"></property>
   </bean>
   <bean name="/customer/noticeDetail.htm" class="controllers.customer.NoticeDetailController">
     <property name="noticeDao" ref="noticeDao"></property>
   </bean>
</beans>
```

글목록보기와 글 상세보기가 정의되어 있는 컨트롤러 클래스이다.  
게시글 DB연결 관련 메서드를 사용하기 위해 `NoticeDao` 객체를 의존설정한다.  
 

`dispatcher-servlet.xml`가 위와 같이 설정되어 있었는데 `NoticeController`, `NoticeDetailController`는 다음과 같이 정의되어 있다.  

```java
public class NoticeController implements Controller{
	private NoticeDao noticeDao; 
	public void setNoticeDao(NoticeDao noticeDao) {
		this.noticeDao = noticeDao;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String p_page = request.getParameter("pg");
		String p_field = request.getParameter("field");
		String p_query = request.getParameter("query");

		int page = 1;
		String field = "title";
		String query = "%%";

		if( p_page != null && !p_page.equals(""))  
			page= Integer.parseInt(p_page);
		
		if( p_field != null && !p_field.equals(""))    
			field= p_field;
		
		if( p_query != null && !p_query.equals(""))  
			query=  "%"+p_query+"%";

		ModelAndView mv = new ModelAndView("notice.jsp");
		mv.addObject("test", "Hello, SpringMVC");
		List<Notice> list = this.noticeDao.getNotices(page , field, query);
		mv.addObject("list", list);
		return mv;
	}
}
```

```java
public class NoticeDetailController implements Controller{
	
	private NoticeDao noticeDao; 
	public void setNoticeDao(NoticeDao noticeDao) {
		this.noticeDao = noticeDao;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		 String seq = request.getParameter("seq");
		 Notice notice = this.noticeDao.getNotice(seq);
		 ModelAndView mv= new ModelAndView("noticeDetail.jsp");
		 mv.addObject("notice", notice);
		return mv;
	}
}
```

모두 `org.springframework.web.servlet.mvc.Controller`란 인터페이스와 `handleRequest`메서드를 구현하고 있는데 
`<bean name="/customer/notice.htm" ...>` 빈 객체 `name` 속성에 설정된 url 패턴에 해당하는 작업을 수행하는 컨트롤러 임을 의미한다.  

또한 `ModelAndView`객체에 이동할 `view` 파일 설정, `view`페이지에서 출력할 `list`혹은 `notice`객체를 포함시켜 반환한다.

`Controller`를 구현해 **하나의 작업에 하나의 컨트롤러**를 작성하게 되면 나중에 컨트롤러 **클래스가 많아지며 관리가 어려워 진다.**  

### `@Controller`을 사용한 컨트롤러 구현 과정

1. `@Controller` 애노테이션을 클래스에 적용  
2. `@RequestMapping` 애노테이션을 이용해서 처리할 요청 경로 지정  
3. 웹 브라우저의 [요청을 처리할 메소드(컨트롤러 메소드)]를 구현하고, 뷰 이름 리턴  

스프링 프레임워크에선 `@Controller`, `@RequestMapping` 어노테이션과 `Model`객체를 통해 **하나의 메서드로 하나의 작업을 처리할 수 있다.**  

컨트롤러 클래스를 작업당 하나씩 만들지 않아도 되기 때문에 관리가 수월해진다.  

`@Controller`어노테이션이 적용된 클래스 안의 메서드를 **컨트롤러 메서드**라 한다.

```java
@Controller
@RequestMapping("/customer/*")
public class CustomerController {
	private NoticeDao noticeDao; 

	@Autowired
	public void setNoticeDao(NoticeDao noticeDao) {
		this.noticeDao = noticeDao;
	}

	//상세보기
	@RequestMapping("noticeDetail.htm") 
	public String noticeDetail(HttpServletRequest request, HttpServletResponse response) 
	throws ClassNotFoundException, SQLException {		 
		int req = Integer.parseInt(request.getParameter("req"));
		Notice notice = this.noticeDao.getNotice(seq);
		requset.setAttribute("notice", notice);
		return "noticeDetail.jsp";
	}
	

	@RequestMapping("notice.htm")
	public String notices(HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, SQLException {
		int p_page = Integer.parseInt(requset.getParameter("page"));
		String p_field = requset.getParameter("field");
		String p_query = requset.getParameter("query");
		
		int page = 1;
		String field = "title";
		String query = "%%";

		if( p_page != null && !p_page.equals(""))  
			page= Integer.parseInt(p_page);

		if( p_field != null && !p_field.equals(""))    
			field= p_field;

		if( p_query != null && !p_query.equals(""))  
			query=  "%"+p_query+"%";

		List<Notice> list = this.noticeDao.getNotices( page , field, query);
		requset.setAttribute("list", list); 
		return "notice.jsp";
	}
}
```

위의 예제를 보면 클래스명 위에 `@Controller`에 더불어 `@RequestMapping("/customer/*")` 어노테이션이 적용되어 있다.  

그리고 밑의 컨트롤러 메서드에는 `@RequestMapping("notice.htm")` 괄호안에 마지막 url패턴만 정의되어 있다.  

즉 `웹context/customer/notice.htm` 형식의 url패턴이 들어오면 `@RequestMapping("/customer/*")`가 지정되어 있는 컨트롤러 클래스 `CustomerController` 안의 `@RequestMapping("notice.htm")`가 지정되어 있는 컨트롤러 메서드가 `notices()` 호출되어 요청을 처리하고 `notice.jsp` 뷰 페이지로 이동 시킨다.  

`return "notice.jsp"`이 되어있는데 `@RequestMapping("/customer/*")`로 설정된 컨트롤러 클래스에서 반환된 값이기 때문에 뷰 페이지도 `/WebContent/customer` 폴더 아래에 있는 `notice.jsp`파일을 호출한다.  

### 뷰페이지, ViewResolver

보안적인측면을 위해 외부에서 바로 접근하지 못하는 `/WEB-INF` 폴더에 `jsp`파일들을 넣어놓는다.  

이를 위해 반환된 문자열 앞에 `/WEB-INF/view/` 을 붙여야 하는데 `@RequestMapping("/customer/*")`은 url매핑도 겸하고 있음으로 변경할 수 없다.  

`JSP/Servlet`에서는 커맨드 컨트롤러에 `prefix`와 `suffix`를 사용해 앞 뒤에 뷰 페이지 경로를 붙여 완전한 경로를 완성했는데  
스프링 프레임워크에서도 xml설정을 통해 `prefix`와 `suffix`를 반환된 뷰 페이지 문자열에 붙일 수 있다.  

```xml
<mvc:annotation-driven />
<bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
  <property name="prefix" value="/WEB-INF/view" />
  <!-- <property name="suffix" value=".jsp" /> -->
</bean>
```

`웹context/customer/notice.htm` 형식으 url패턴이 들어오면 `notice.jsp` 문자열을 반환하고 이는 `@RequestMapping("/customer/*")`을 통해 `/customer/notice.jsp`으로 변경된다.  

그리고 xml에 설정된 `ViewResolver`를 통해 `/WEB-INF/view/customer/notice.jsp`으로 변경된다.  

### 서블릿 매핑에 따른 컨트롤러 경로 매핑, 디폴트 서블리 설정

`JSP/Servlet`에선 컨트롤러가 url패턴을 잡기 위해 뒤에 `.do`, `.ad` 등의 의미없는 확장자 같은 문자열을 붙여 요청을 컨트롤러가 캐치하였는데  

웹 개발 초기에는 위와같은 방식을 자주 사용했지만 최근에는 확장자 방식을 사용하기 보다 의미에 맞는 URL을 사용하는 방법을 주로 사용한다.  

기존에 아래와 같이 url패턴을 설정했다면
```
http://localhost/springMVC2/customer/notice.do
http://localhost/springMVC2/customer/notice.do?notice_seq=10
```
최근에는 아래와 같이 url패턴을 사용한다.  
```
http://localhost/springMVC2/customer/notice/list
http://localhost/springMVC2/customer/notice/10
```

위와같은 url매핑을 사용하려면 아래와 같이 `DispatcherServlet` 프론트 컨트롤러에 url매핑해줘야 한다.  
```xml
<servlet>
	<servlet-name>dispatcher</servlet-name>
	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
	<load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
	<servlet-name>dispatcher</servlet-name>
	<url-pattern>/customer/*</url-pattern>
</servlet-mapping>
```
`/customer/*` 처럼 `url-parttern`을 사용했다면 `@RequestMapping`을 통해 컨트롤러 메서드와 url을 매핑시킬 때는 `customer`을 제외해야 한다.  

만약 `/customer/notice/list` url패턴을 처리하는 컨트롤러 메서드를 만들고 싶다면 `@RequestMapping`를 아래와 같이 설정

`@RequestMapping("/notice/list")`

**요청 URL** | **서블릿 매핑 URL 패턴** | **컨트롤러 매핑 경로**
|---|---|---|
`/SpringContext/notice/list.do` | `/notice/*` | `/list.do`
`/SpringContext/notice/list.do` | `*.do` | `/notice/list.do`
`/SpringContext/noticeDetail` | `/noticeDetail` | `/noticeDetail`

위와 같은 컨트롤러 매핑경로는 혼란이 많은데 그냥 모든 url경로를 사용해 컨트롤러 메서드에 매핑하고 싶다면 아래

실제 웹 사이트를 만들때 `/customer` url패턴 외에도 여러 경로의 url패턴이 생길 것인대 이를 일일이 `<url-pattern>`으로 지정하는 것은 너무 귀찮은 일이다.  

`DispatcherServlet` 의 url매핑 경로로 전체 매핑경로를 사용하면 전체경로를 매핑경로로 사용할 수 있다.  `<url-pattern>/</url-pattern>`  

하지만 위와같이 설정하면 `.js`, `.css` 등 여러 파일도 `DispatcherServlet`에 걸리게 되는데 이를 처리하는 컨트롤러 메서드가 없으면 스타일과 자바스크립트가 사이트에 적용되지 않는다...

그렇다고 수많은 스타일 시트, 자바스크립트 파일 위치를 반환시키는 컨트롤러 메서드를 정의할 수 도 없다.  

`Jsp/Serviet`에선 이를 처리하기 위한 방법이 있지 않지만 Spring에선 디폴트 서블릿 객체를 통해 처리 가능하다.  

작성중.... @PathVariable...



### `@RequestMapping`을 이용한 요청 매핑

위에선 간단히 컨트롤러 클래스에는 `@RequestMapping("/customer/*")` 설정을  
컨트롤러 메서드에는 `@RequestMapping("notice.htm")` 설정을 적용하였다.  

이 외에 `@RequestMapping` 어노테이션을 통해 어떻게 url매핑을 할 수 있는지 알아보자.  

**여러개의 url패턴을 하나의 컨트롤러 메서드로 처리**  
`String`배열 형식으로 어노테이션 괄호안에 값을 지정한다.  
```java
@RequestMapping({"/notice.htm", "/noticeDetail.htm"})
public String sampleMethod(...) {
	...
	...
}
```

**http 전송방식(post, get)에 따른 컨트롤러 메서드 지정**  
url매핑과 method설정을 같이 하고 싶다면 `value`, `method`속성을 사용하자.
```java
@RequestMapping(value="/noticeReg.htm", method="RequestMethod.GET")
public String sampleMethodGet(...) {
	...
	...
}
@RequestMapping(value="/noticeReg.htm", method="RequestMethod.POST")
public String sampleMethodPost(...) {
	...
	...
}
```
배열 형식으로 `value` 속성을 설정해도 된다.  
`@RequestMapping( value={ "noticeReg.htm"}, method=RequestMethod.GET )`

최근에 REST API가 많이 사용되며 HTTP데이터로 JSON이나 XML데이터를 전송하는 일이 많아졌다.  

웹 브라우저에서 폼을 전송할 때 사용하는 `application/x-www-form-urlencoded` 외에  
ajax를 통해 JSON 데이터를 전송할 떄 사용하는 `application/json`  
xml데이터를 전송할 때 사용하는 `application/xml` 과 같은 정보가 헤더에 포함되어 서버로 전달되는데  

`@RequestMapping`의 `consumes`속성을 사용하면 이를 구분해서 컨트롤러 메서드에 매핑 가능하다.  

`@RequestMapping( value={ "noticeReg.htm"}, method=RequestMethod.GET, consumes="application/json")`
`@RequestMapping( value={ "noticeReg.htm"}, method=RequestMethod.GET, consumes="application/xml")`



### Ant 패턴을 이용한 경로 매핑

`@RequestMapping` 매핑의 경로를 설정할 때 Ant패턴을 사용해 url패턴을 잡아낸다.  

Ant패턴은 다음 3가지 특수문자를 이용해 경로 표시

1. * - 0개 또는 그이상의 글자  
2. ? - 1개 이상의 글자  
3. ** - 0개 또는 그이상의 디렉토리 경로  

`@RequestMapping("/member/?*.info")`  
`/member/`로 시작하고 확장자가 `.info`로 끝나는 모든 경로  

`@RequestMapping("/faq/f?00.fq)`  
`/faq/`경로의 1글자가 `f`와 `0`사이에 위치하는 모든 경로    
 
`@RequestMapping("/folder/**/files)`  
`/folder/`로 시작해서 `/files`로 끝나는 모든 경로



### 컨트롤러 메서드 주요 파라미터

MVC 구조에선 model이 처리한 결과(데이터)를 view에서 출력해야 하는데  
데이터를 받고 넘기는 방법이 여러가지 있다.  

컨트롤러 메서드의 파라미터와 리턴값을 통해 여러 방법으로 요청 파라미터를 받고 view로 결과를 넘기는데 하나씩 알아보자.  

**파라미터** | **설명**
|---|---|
`HttpServletRequest`, `HttpServletResponse` | 요청/응답 처리를 위한 서블릿 API
`Httpsession` | HTTP세션을 위한 서블릿 API 
`Model`, `ModelMap`, `Map` | 뷰에 데이터를 전달하기 위한 모델
`@RequestParam` | HTTP 요청 파라미터 값
`@RequestHeader`, `@CookieValue` | 요청 헤더와 쿠키값
`@PathVariable` | 경로변수
`커맨드 객체` | 요청 데이터를 저장할 객체
`Errors`, `BindingResult` | 검증 결과를 보관할 객체, 커맨드 객체 바로뒤에 일치해야함
`@RequestBody (파라미터에 적용)` | 요청 몸체를 객체로 반환, 요청 몸체의 JSON이나 XML을 알마게 객체로 변환
`Writer`, `OutputStream` | 응답 결과를 직접 쓸 때 사용할 출력 스트림

위에선 `HttpServletRequest`, `HttpServletResponse` 객체를 통해 뷰에 표시할 데이터를 담아 전달했지만  
스프링 프레임 워크에선 이 외에도 많은 방법이 있다.  

```java
@RequestMapping("notice.htm")
public String notices(HttpServletRequest request, HttpServletResponse response, Model model)
throws ClassNotFoundException, SQLException {
	int p_page = Integer.parseInt(requset.getParameter("page"));
	String p_field = requset.getParameter("field");
	String p_query = requset.getParameter("query");
	
	int page = 1;
	String field = "title";
	String query = "%%";

	if( p_page != null && !p_page.equals(""))  
		page= Integer.parseInt(p_page);

	if( p_field != null && !p_field.equals(""))    
		field= p_field;

	if( p_query != null && !p_query.equals(""))  
		query=  "%"+p_query+"%";

	List<Notice> list = this.noticeDao.getNotices( page , field, query);
	// requset.setAttribute("list", list); 
	model.addAttribute("list", list);
	return "notice.jsp";
}
```
`Model`의 주요 함수

`Model addAttribute(String attrName, Object attrValue)`  
이름이 attrName이고 값이 attrValue인 모델 속성을 추가한다. 반환값이 `Model`이기 때문에 체인처럼 연결해 추가 가능

`Model addAllAttribute(Map)`  
맵 콜렉션을 사용해 속석을 추가, 마찬가지로 `Model`을 반환.    

`boolean containsAttribute(String attrName)`
`attrName`을 가진 모델 속성이 있는지 boolean값으로 반환.  

`ModelMap`의 경우 `Model`객체의 자식 클래스로 부모 클래스의 함수를 그대로 사용 가능하다.  

차이는 인터페이스, 구현 클래스 차이....

#### `@RequestParam` 어노테이션

`@RequestParam` 어노테이션을 통해 `HttpServletRequest`의 `getParameter()`메서드를 사용하지 않고 요청 파라미터를 구할 수 있다.  
```java
@RequestMapping("noticeDetail.htm")
public String noticeDetail(HttpServletRequest request, HttpServletResponse response, Model ) 
throws ClassNotFoundException, SQLException {		 
	int req = Integer.parseInt(request.getParameter("req"));
	Notice notice = this.noticeDao.getNotice(seq);
	requset.setAttribute("notice", notice);
	return "noticeDetail.jsp";
}
```

그저 `req`라는 파라미터 하나 받는 일인데 `request`부터 형변환, `getParameter` 메서드 호출까지 하는일이 너무 많다.  

`@RequestParam` 어노테이션을 사용하면 코드를 많이 생략할 수 있다.  

```java
@RequestMapping("noticeDetail.htm")
public String noticeDetail(@RequestParam("req") int req, Model model) 
throws ClassNotFoundException, SQLException {		 
	Notice notice = this.noticeDao.getNotice(req);
	model.addAttribute("notice", notice);
	return "noticeDetail.jsp";
}
```
> 만약 파라미터로 받은 값을 `@RequestParam`을 통해 형변환 불가능 할 경우 400 error을 반환한다.  

`@RequestParam`에는 `value`, `required`, `defaultValue` 속성이 있다.  

value는 파라미터명을 지정할 때 사용하며 required는 파라미터가 넘어오지 않더라도 오류발생하지 않도록 하는 설정이다.  

`public String search(@RequestParam(value="req", required=false) String req) {...}`
당연히 값이 넘어오지 않으면 req는 `null`로 초기화된다.  
(기본형의 경우 `null`로 초기화 할 수 없으니 조심)   

`defaultValue`속성의 경우 값이 넘어오지 않을경우 기본값을 설정하는 속성이다.   
`public String search(@RequestParam(value="req", required=false, defaultValue="10") String req) {...}`    
`null`검사를 생략하기 때문에 페이징 처리 등에서 유용하게 사용된다.  


#### Httpsession 

`Httpsession` 객체를 받기 위해 `request.getSession()` 메서드를 사용했는데  
스프링MVC에선 파라미터로 받을 수 있다.  

단 `HttpServletRequest`의 `getSession(false)` 메서드는 매개변수로 `false`를 넣으면 기존에 session이 존재하지 않았다면 `null`을 반환한다.  

스프링MVC를 통해 매개변수로 `Httpsession`객체를 받으면 기존에 존재하지 않을경우 생성해서 반환한다.  

만약 세션 존재 유무에 따른 코딩을 해야한다면 `HttpServletRequest`을 사용하도록 하자.  


#### 커맨드 객체

회원가입을 받기 위해 아래와 같은 `form`태그를 사용할 때 
```html
<form action="">
	이메일: <input type="text" name="email"/>><br>
	이름: <input type="text" name="name"/><br>
	암호: <input type="text" name="pw1"/><br>
	암호확인: <input type="text" name="pw2"/><br>
</form>
```
보통은 `request`객체를 이용해 개별적으로 요청 파라미터를 받는다.  
```java
String email = requset.getParameter("email");
String name = requset.getParameter("name");
String pw1 = requset.getParameter("pw1");
String pw2 = requset.getParameter("pw2");
```

하지만 스프링 MVC에선 넘어오는 파라미터 명과 같은 이름을 가진 필드들을 가지고 있는 클래스와  
클래스 안에 get, set 메서드가 정의되어 있다면 위와 같은 번거로운 작업을 할 필요 없다.

```java
public class MemberRequest {
	private String email;
	private String name;
	private String pw1;
	private String pw2;

	//get, set 메서드....
}
```
위와 같이 넘어오는 파라미터명과 똑같은 이름을 가진 필드로 설정한 클래스를 정의하고 

컨트롤러 메서드 매개변수로 그냥 `MemberRequest`를 사용하면 자동으로 요청 파라미터로 초기화된 객체가 매개변수로 넘어온다.  

```java
@RequestMapping("member_join.htm")
public String member_join(MemberRequest member) throws ClassNotFoundException, SQLException {		 
	...
	...
	return "index.jsp";
}
```
또한 이렇게 초기화된 커맨드 객체는 뷰에 전달할 모델에 자동 포함된다.  
모델 식별자로 `MemberRequest`클래스중 앞글자만 소문자로 변환되어 `memberRequest`으로 뷰에서 사용 가능하다.  

만약 클래스명이 너무 길거나 복잡에 모델명으로 사용하고 싶지 않다면 `@ModelAttribute`어노테이션을 사용하자.  
```java
@RequestMapping("member_join.htm")
public String member_join(@ModelAttribute("mem") MemberRequest member) throws ClassNotFoundException, SQLException {		 
	...
	...
	return "index.jsp";
}
```

`@ModelAttribute` 어노테이션은 모델명 지정외에도 공통 모델 처리가 가능하다.  

```java
@Controller
@RequestMapping("/event/*")
public class EventController {

	@ModelAttribute("recEventList")
	public List<Event> recommend() {
		return eventService.getRecommendedEventList();
	}

	@RequestMapping("/list")
	public String list(Model model) {
		List<Event> eventList = eventService.getOpenedEventList();
		model.addAttribute("eventList", eventList);
		return "event/list";
	}

	@RequestMapping("/detail")
	public String list(Model model) {
		...
		...
		return "event/detail";
	}
}
```

`/event/list`요청이 오던 `/event/detail`요청이 오던 상관없이 `eventService.getRecommendedEventList()` 메서드가 반환하는 `List<Event>` 객체를 사용하고 싶을 때 `@ModelAttribute("recEventList")`를 사용해 `list.jsp`, `detail.jsp`에서 `recEventList`모델을 사용할 수 있다.  


### 컨트롤러 메서드 주요 리턴 타입  

**리턴 타입** | **설명**
`String` | 뷰 이름
`void` | 컨트롤러에서 응답을 직접 작성
`ModelAndView` | 모델과 뷰 정보를 함께 리턴
`객체` | 메서드에 `@RequestBody`가 적용된 경우 리턴 객체를 JSON이나 XML과 같은 알맞은 응답으로 반환



### 리다이렉트 처리

`Jsp/Servlet`에선 `HttpServletResponse`의 `sendRedirect()`메서드를 통해 클라이언트에게 리다이렉트 응답을 했는데  

스프링 MVC에선 `redirect:` 접두어를 리다이렉트 주소 앞에 붙여 응답한다.  

ex) `return "redirect:/main";`

접두어 뒤에 완전한 url을 사용할 수 있다.  
`return "redirect:https://kouzie.github.io";`  

<!-- 
p.426~ Chap09. 스프링MVC: XML/JSON, 파일 업로드, 웹소켓
XML, JSON 형식의 요청/응답 데이터를 처리하는 방법 설명, 일단 읽고 넘어감...


p.438 파일 업로드
파일을 업로드하기 위해선 HTML 폼 태그의 속성으로 enctype="multipart/form-data"를 추가함(method는 post)

MultipartResolver 설정
멀티파트 지원 기능을 사용하려면 먼저 MultipartResolver를 스프링 설정 파일에 등록
MultipartResolver: 멀티파트 형식으로 데이터가 전송된 경우, 해당 데이터를 스프링 MVC에서 사용할 수 있도록 변환

스프링이 기본 제공하는 MultipartResolver는 두 개
- o.s.web.multipart.commons.CommonsMultipartResolver
- o.s.web.multipart.support.StandardServletMultipartResolver
둘 중 하나를 스프링 빈으로 등록하면 됨. 이 때 스프링 빈의 이름은 "multipartResolver"여야 함
(DispatcherServlet은 이름이 "multipartResolver"인 빈을 사용하기 때문)

표 9.2 CommonsMultipartResolver 클래스의 프로퍼티
프로퍼티		타입				설명
maxUploadSize	        long		최대 업로드 가능한 바이트 크기. -1은 제한 없음을 의미. 기본값 -1
maxInMemorySize		 int		디스크에 임시 파일을 생성하기 전에 메모리에 보관할 수 있는 최대
					바이트 크기. 기본 값은 10240 바이트
defaultEncoding		String		요청을 파싱할 때 사용할 캐릭터 인코딩. 지정하지 않을 경우,
					HttpServletRequest.setCharacterEncoding() 메소드 지정한 캐릭터 셋 사용
					아무 값도 없을 경우 ISO-8859-1 사용

[파일 업로드 실습 준비]
1. 아래 두개 파일 추가
com.springsource.org.apache.commons.fileupload-1.2.0.jar
com.springsource.org.apache.commons.io-1.4.0.jar

2. dispatcher-servlet.xml 안에 multipartResolver 빈 등록
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver"></bean>

3. customer 폴더 안에 upload 폴더 추가

4. Notice.java에서 CommonsMultipartFile 필드 추가 & getter/setter

5. CustomerController.java 내의 noticeReg(POST일 때) 컨트롤러 메소드에서 MultipartFile 객체 생성 및 코딩

6. 파일 존재 여부 확인해서 존재하면 파일 객체 만들고 transferTo메소드를 통해 저장 -->
