---
title:  "Spring - DI 자동 연결, 스프링 AOP 개요, 스프링 생명주기!"

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

## 어노테이션을 이용한 객체간 DI 자동 연결

지금까지 **xml파일**에서 **스프링 빈 객체**를 만들고 `<property>`태그를 사용해서 **set메서드**, 혹은 **생성자**를 통해 **필드를 초기화** 했다.  

하지만 어노테이션을 이용한 객체간 의존 자동 연결을 서정하면 `<property>`태그를 사용해 일일이 필드를 초기화 할 필요 없이 **자동으로 추가 가능하다.**  

스프링 컨테이너가 알아서 자료 타입에 맞는 **자바 빈 객체를 찾아서 매치**시켜 준다!
즉 개발자가 일일이 의존정보를 프로퍼티 태그로 설정하지 않더라도 **자동적으로 의존관계를 추가**시킬 수 있다! 

<br><br>

프로젝트를 진행하다보면 의존관계가 뻔한 클래스들이 있다.

예를 들어 `DisplayHandler`는 `DisplayService`를 필요로 하고 `DisplayService`는 `DisplayDAO`를 필요로 한다.  

특히 싱글톤 같은 경우(`~~Factory`같은 클래스) 의존관계가 명확하기 때문에 자동주입 되면 편하다!  

의존관계를 자동 설정하는 여러 설정 어노테이션이 여러개 있는데 모두 알아보자.  
**`@Autowired`, `@Resource`, `@Inject`**

### `@Autowired` 어노테이션

**의존관계를 자동으로 설정**할 때 사용하는 어노테이션.  

**생성자, 필드, 메서드**에 사용 가능하다.  

어노테이션을 설정하고 DI를 자동 주입하려면 xml파일에 `<context:anntation-config>`태그가 필요하다.  
당연히 `context` 네임 스페이스를 사용하려면 `beans` 태그에 설정이 필요하다.
```xml
<beans 
	xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd"
	>
	...
	...
</beans>
```
> https://kouzie.github.io/spring/Spring-개요/#spring에서-객체생성

위 주소에서 사용했던 `Record`, `RecordImple`, `RecordView`, `RecordViewImple` 클래스를 `@Autowired`어노테이션을 사용해서 자동 주입되록 설정하자.

먼저 xml파일에 `context`네임스페이스를 사용할 수 있도록 `<beans>` 태그 속성을 추가하고  

`<context:annotation-config/>`태그를 통해 어노테이션을 통해 의존설정 한다고 스프링 컨테이너에 알린다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd" >
	
	<context:annotation-config/>
	<bean id="record" class="di.RecordImpl"/> 
	<bean id="service" class="di.RecordViewImpl"/>
</beans>
```

`record` 빈 객체, `sevice` 빈 객체 모두 생성만 하지 관계설정 코드는 xml에 있지 않다.  

이제 `RecordViewImpl`클래스에 스프링 컨테이너가 의존설정 할 수 있도록 필드, 메서드, 생성자 위에 `@Autowired`어노테이션을 추가한다.  

```java
public class RecordViewImpl implements RecordView{
	
	@Autowired
	private RecordImpl record = null;
	public RecordViewImpl() {}

	@Autowired
	public RecordViewImpl(Record record) {
		this.record = (RecordImpl) record;
	}
	public RecordImpl getRecord() {
		return record;
	}

	@Autowired
	public void setRecord(RecordImpl record) {
		this.record = record;
	}

	@Override
	public void input() { 
		try(Scanner scanner = new Scanner(System.in);){
			System.out.print("> kor,eng,mat input ? ");
			this.record.setKor(scanner.nextInt());
			this.record.setEng(scanner.nextInt());
			this.record.setMat(scanner.nextInt());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void output() { 
		System.out.printf(
				"> kor:%d, eng:%d, mat:%d, tot=%d, avg=%.2f\n"
				,this.record.getKor()
				,this.record.getEng()
				,this.record.getMat()
				,this.record.total()
				,this.record.avg()
				);
	}
}
```
위에선 필드, 메서드, 생성자 모두 `@Autowired`어노테이션을 추가했지만 하나만 추가해도 동작한다.  

스프링 컨테이너가 `Record` 타입에 해당하는 빈 객체를 찾아 알아서 주입시킨다.****  
> xml파일을 보면 `Record`을 구현한 `RecordImpl`이 자동으로 주입되는데 상속관계에만 있다면 주입시킬 수 있다.


### 자바 코드에서의 자동 주입

xml설정과 비해 따로 달라질 건 없다. 오히려 `<context:annotation-config/>` 태그 역할도 이미 자바코드설정의 `@Configuration` 어노테이션에 포함되어 있기 때문에 xml에 비해 더더욱 건들일 것 이 없다.  

똑같이 `@Autowired` 어노테이션을 사용하나 생성자에 붙여 사용하지 못한다.  

이뉴는 자바코드에선 **직접 생성자를 호출**하기 때문에 우리가 지정한 `@Anntation`으로 설정한 스프링 컨테이너가 끼어들 틈이 없기 때문.  

먼저 설정파일 역할을 해줄 `Config.java` 를 다음과 같이 설정

기존에는 다음과 같이 사용했었다.
```java
@Bean
	public RecordViewImpl service(Record record) {
		return new RecordViewImpl(record);
	}
```

```java
@Configuration
public class Config {      
	@Bean
	public RecordImpl record() {
		return new RecordImpl();
	}
	
	@Bean
	public RecordViewImpl service() {
		return new RecordViewImpl();
	}
}
```

`service()`메서드에 매개변수도 설정함수도 없고 그저 `RecordViewImpl`를 반환한다.  

`service` 빈 객체에 `record`를 설정하는 코드가 일절 없는데도 자동주입된다.  

```java
public class RecordViewImpl implements RecordView{
	@Autowired
	private RecordImpl record = null;
	...
	...
	@Autowired
	public void setRecord(RecordImpl record) {
		this.record = record;
	}
	...
	...
}
```
당연히 생성자를 제회한 필드나 set메서드에 `@Autowired` 어노테이션 적용이 필요하다.




만약 xml에 `record` 빈 객체를 생성하는 `<bean>`태그가 없다면? 다음과 같은 오류가 발생한다.  
```
...
[di.Record]: : No matching bean of type [di.Record] found for dependency: expected at least 1 bean which qualifies as autowire candidate for this dependency. 
...
```

분명 `@Autowired`어노테이션을 통해 `Record`타입의 빈 객체를 의존관계로 형성하겠다고 스프링 컨테이너한테 알렸는데 생성된 `Record` 빈 객체가 하나도 없기 때문!  

`Record`타입의 빈 객체가 있다면 의존관계로 설정하고 없다면 `null`로 설정하고 싶다면 `@Autowired(required=false)`로 설정하면 된다.  

### `@Qualifier` - 한정자


만약 `Record` 타입의 빈 객체가 2개 이상 만들어질 경우? 
```xml
...
<bean id="frist_record" class="di.RecordImpl"/> 
<bean id="second_record" class="di.RecordImpl"/> 
<bean id="service" class="di.RecordViewImpl"/>
...
```
다음과 같은 오류가 발생한다.  

```
...
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No unique bean of type [di.Record] is defined: expected single matching bean but found 2: [frist_record, second_record]
...
```

당연히 오류가 발생한다. 

DI관계를 만들 때 같은 타입의 객체가 여러개 생성될 경우는 드물지만 여러개의 같은 type의 빈 객체를 만들 상황도 발생한다.  


`@Autowired`어노테이션을 사용할 때 이런 상황을 대처하기 위해 `@Qualifier`어노테이션을 사용해 별칭을 주어 의존관계를 설정할 클래스를 한정지을 수 있다.  

먼저 `<qualifier>`태그를 사용해 스프링 빈 객체에 한정자(별칭)을 부여한다.  

```xml
...
<context:annotation-config/>
<bean id="first_record" class="di.RecordImpl">
	<qualifier value="first"/>
</bean>
<bean id="second_record" class="di.RecordImpl">
	<qualifier value="second"/>
</bean> 
<bean id="service" class="di.RecordViewImpl"/>
...
```

그리고 `@Autowired`를 사용하는 생성자, 필드, 메서드에 다음과 같이 `@Qualifier` 어노테이션을 적용

```java
@Autowired()
@Qualifier("first")
public void setRecord(RecordImpl record) {
	this.record = record;
}
```

> `@Autowired`를 사용하는 상황에서 대처하라고 만들어진게 `@Qualifier`, 그런 상황이 아니라면 ref 태그 사용하면 된다.


### `@Resource` - 식별자

`@Resource`어노테이션도 같은 타입의 스프링 빈 객체가 여러개 생성되었을 때 특정한 빈 객체 하나만 의존관계로 설정하고 싶을 때 사용하는 어노테이션이다.  

`@Qualifier`와 다르게 `@Resource`는 name속성에 정의된 내용을 기준으로 스프링 빈 객체를 선택한다.  

따로 별칭을 주는 것 보다 그냥 **식별자**를 통해 DI를 할 빈 객체를 주입하기 때문에 더 수월하다.

다음과 같이 xml이 설정되어 있을 때 
```xml
<bean id="first_record" class="di.RecordImpl"/>
<bean id="second_record" class="di.RecordImpl"/>
<bean id="service" class="di.RecordViewImpl"/>
```

`@Resource`어노테이션과 `name` 설정으로 스프링 빈 객체를 지정한다.  
```java
@Autowired(required=false)
@Resource(name="first_record")
public void setRecord(RecordImpl record) {
	this.record = record;
}
```

>참고로 `@Resource`을 사용하면 `@Autowired`를 생략할 수 있다. 어차피 자동 생성시 빈 객체를 한정하기 위해서 사용하는 어노테이션 임으로.
```java
@Resource(name="first_record")
public void setRecord(RecordImpl record) {
	this.record = record;
}
```
>심지어 `@Resource()` 괄호안의 내용을 생략할 경우 `@Autowired`처럼 사용할 수 있다. 


### `@Component` - 컴포넌트 스캔을 이용한 빈 자동 등록


`@Autowired`를 사용하면 아래와 같이 `<bean>`태그를 통해 스프링 빈 객체를 생성하기만 해도 의존관계가 자동 형성된다.   
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd" >
	
	<context:annotation-config/>
	<bean id="record" class="di.RecordImpl"/> 
	<bean id="service" class="di.RecordViewImpl"/>
</beans>
```
`<property>`태그를 생략할 수 있어 매우 간편하다!  

그런데 `@Component`어노테이션을 사용하면 빈 객체를 생성하는 `<bean>`태그마저도 생략하고 생성후 자동 등록할 수 있다.  

`@Component`을 사용해 빈객체를 자동 생성해보자.

먼저 xml을 다음과 같이 설정
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd" >

	<context:component-scan base-package="di"/>	
</beans>
```
`<context:component-scan base-package="di"/>` 새로운 태그가 추가되었는데  
`di`패키지 안에 정의된 모든 클래스(하위 패키지까지)를 돌며 `@Component`어노테이션이 적용된 클래스를 빈 객체로 자동 생성한다.  

**생성할 클래스 위에 `@Component`어노테이션을 추가하면 된다.**  
```java
@Component
public class RecordImpl implements Record{
	private int kor;
	private int mat;
	private int eng;
	//get, set 생략...
	public RecordImpl(int kor, int mat, int eng) {
		this.kor = kor;
		this.mat = mat;
		this.eng = eng;
	}
	@Override
	public int total() {
		return kor+mat+eng;
	}
	@Override
	public double avg() {
		return (kor+mat+eng)/3.0;
	}
}

@Component
public class RecordViewImpl implements RecordView{
	private RecordImpl record = null;

	public RecordViewImpl() {}
	public RecordViewImpl(Record record) {
		this.record = (RecordImpl) record;
	}

	public RecordImpl getRecord() {
		return record;
	}
	@Autowired
	public void setRecord(RecordImpl record) {
		this.record = record;
	}

	@Override
	public void input() { 
		try(Scanner scanner = new Scanner(System.in);){
			System.out.print("> kor,eng,mat input ? ");
			this.record.setKor(scanner.nextInt());
			this.record.setEng(scanner.nextInt());
			this.record.setMat(scanner.nextInt());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void output() { 
		System.out.printf(
				"> kor:%d, eng:%d, mat:%d, tot=%d, avg=%.2f\n"
				,this.record.getKor()
				,this.record.getEng()
				,this.record.getMat()
				,this.record.total()
				,this.record.avg()
				);
	}

}
```

클래스 위에 `@Component`를 설정하면 스프링 컨테이너에게 이 클래스는 자바빈 객체로 자동 등록하라고 명시한다.

> 자동 생성을 하면 당연히 자동 연결은 따라와야 한다. 연결과정만 xml에서 따로할 수 는 없다...
그리고 `<context:component-scan>`을 설정하면 `<context: annotation-config>`은 생략해도 `@Resource`, `@Autowired`를 사용 가능하다.

자바코드 설정파일에선 `@ComponentScan(basePackage="")`으로 패키지를 지정할 수 있으나  
`3.x`버전에선 지원하지 않는다. 3.x버전에선 xml을 통해서만 지정 가능하다.  

<br><br>

그럼 자동 등록된 클래스들의 식별자는 어떻게 지정될까?  
보통 클래스명 앞에 **첫 문자만 소문자로 지정되어** 스프링 컨테이너에서 생성된다.  

만약 이름을 별도로 지정하고 싶다면 `@Component("지정할 이름")` 괄호에 이름을 지정할 수 있다.  

`basepackage`를 통해 `component-scan`할 패키지를 등록하는데  
`filter`를 통해 특정한 하위 패키지를 제외하거나 regex를 사용해 패키지를 등록하는 기능이 있다.  

```xml
<context:component-scan base-package="패키지명">
	<context:include-filter type="regex" expression=".*Service">
	<context:exclude-filter type="aspectj" expression="net..*DAO">
</context:component-scan>
```
정규식 외에 여러 방법으로 패키지 지정이 가능하다. `type`속성으로 어떤 방법을 사용할 것 인지 명시한다.  

**tpye 속성** | **설명**
|---|---|
`annotaion` | 클래스에 지정한 어노테이션이 적용됐는지 여부, `expression="org.sample.SomeAnnotaion"`, `@SomeAnnotaion`이 적용되어 있을 경우 빈 객체로 생성
`assignable` | 클래스가 지정한 타입으로 할당 가능한지의 여부, `expression="di.Record"`, `Record`로 할당 할 수 있는 클래스를 빈 객체로 생성
`regex` | 정규 표현식으로 클래스 명을 지정
`aspectj` | `AspectJ` 표현식으로 클래스 명을 지정 

위의 `AspectJ` 표현식중 `net..*DAO`의 뜻은 net 패키지 하위에 모든 `DAO`로 끝나는 클래스를 빈 객체로 등록한다.  

>참고: 빈 객체로 자동 등록 시킬 때 `@Component`외에도 사용하는 어노테이션이 3가지 있다.  
`@Service`, `@Repository`, `@Controller`  

>각각 **MVC패턴**에서 사용되는 `Service`, `DAO`, `Controlloer`를 의미하며  
`@Repository`을 사용 할 경우 스프링에서 사용되는 익셉션으로 변환되는 기능이 자동 적용된다.  

`@Component`혹은 `@Repository` 등의 어노테이션을 사용해 빈 객체를 생성하는 `<bean>`태그 혹은 자바설정에서의 `@Bean`어노테이션을 생략하게 되면 자연스럽게 `<property>`태그 혹은 자바설정에서 필드를 설정하는 코드도 생략된다.  

`@Component`등의 어노테이션으로 자동 등록했으면 `@Autowired`, `@Resource`등의 어노테이션으로 의존관계의 자동연결은 필수이다.  

> 자바코드설정의 `<context:component-scan base-package="패키지명">`의 어노테이션 버전은 `@ComponentScan(basePackage="패키지명")` 이다.  


## Spring AOP

`AOP`는 `Aspect Oriented Programming` (관점 지향적 프로그래밍 기법)의 약자이다.  

우리가 게시판을 만들때 글쓰기, 혹은 댓글쓰기 기능을 수행하기 전에 로그인 인증과정을 거쳐야 가능하도록 설정하는데 이런 기능 관점으로 프로그래밍 하는 것을 뜻한다.  

`jsp/servlet` 글쓰기 작업을 하기 전에 로그인 인증과정을 거치려면 필터를 사용하거나 핸들러에서 로그인 인증과정을 거치는 코딩을 처음에 위치하도록 해야한다.  

로그인 인증과정을 거치는 과정이 글쓰기 하나뿐이면 상관없지만 계속 늘어난다면 필터를 쓰지 않고는 중복코드가 계속 늘어난다.  

이러한 중복 코드를 제거하기 위해 필터를 사용하는데  
물론 스프링에서도 필터를 사용해도 되지만 `AOP`를 사용하면 좀더 구조적으로(유지보수 쉽게) 처리 가능하다.

스프링에서 모든 기능은 `핵심 비지니스 로직`과 `공통 기능`으로 나뉜다.  
로그인 인증과정 같이 중복되고 공통적으로 사용되는 기능을 공통 기능이라 하며 `cross-cutting concern(공통 관심사항)`이라 표현한다.  

이런 공통 기능을 AOP에서 어떻게 처리하는지 알아보자.  

![image4]({{ "/assets/Spring/image4.png" | absolute_url }}){: .shadow}  


### AOP 주요 용어

용어 | 의미 
|--|--|
`Aspect` | 여러 객체에 **공통적으로 적용되는 기능**을 `Aspect`라 한다. 인증, 트랜잭션, 보안 과 같은 공통적 기능을 의미한다.
`Advice` | 공통적으로 사용되는 기능(`Aspect`)를 핵심 로직에 언제 적용하는지, **시점**을 의미한다(핵심기능 시작 전, 후, 예외발생시 등). 
`JoinPoint` | `Advice`를 적용 가능한 **지점**을 의미한다. 핵심 기능과 공통 기능이 어느지점에서 만나는지를 의미하며 메서드 호출, 필드값 변경 지점을 의미한다.
`Pointcut` | 많은 `JoinPoint`중에 실제 핵심기능과 보조기능이 접하게 되는 지점을 `Pointcut`이라 한다. `JoinPoint`의 부분집합을 `Pointcut`이라 할 수 있다.
`Weaving` | 프로그래머가 설정한 `Advice`대로 `Aspect`를 핵심로직 코드에 적용하는 것, 행위를 뜻한다.

### 구현되는 Advice의 종류

**Advice명** | **설명** | **사용 클래스**
|--|--|--|
`Before Advice` | 핵심기능 전에 수행하는 공통 기능 (로그인 체크) | `MethodBeforeAdvice`
`Around Advice` | 핵심기능 수행 전 후에 수행하는 고통 기능 (시간 체크) | `MethodInterceptor` 
`After Returning Advice` | 예외 발생이 없을 경우 수행하는 기능 | `AfterReturningAdvice`
`After Throwing Advice` | 예외 발생할 경우 수행하는 기능 | `ThrowsAdvice` 
`After Advice` | 예외 발생 상관없이 핵심기능 수행 후 수행하는 공통 기능 | 

공통기능이 핵심기능 어느시점에 실행 되는지에 따라 위 5가지 Advice를 택해야 한다.  

여러개의` Advice`를 하나의 핵심 기능에 적용시켜도 상관 없다.  

### Weaving하는 시점

AOP는 꼭 스프링에서만 사용하는 개념이 아니며 다음 3가지 시점에 Weaving한다.  

1. 컴파일시 
2. 클래스 로딩시
3. 런타임시

스프링 프레임워크에선 3번째 방법인 런타임시에 Weaving할 수 있는데 **Proxy기반의 AOP**이다.  

> proxy : (측정·계산하려는 다른 것을 대표하도록 이용하는) 대용물 - naver백과사전

![image5]({{ "/assets/Spring/image5.png" | absolute_url }}){: .shadow}  

`crossCuttingConcern`: 공통 관심사항
`coreConcern`: 핵심 기능

핵심 기능을 하는 클래스, 보조기능을 하는 클래스가 있으면 따로 2개의 클래스가 올라가고   
가상으로 만들어진 프로그래머가 설정한 대로 Weaving된 Proxy(가짜)클래스가 만들어진다.   

이러한 Proxy 클래스를 만드는 방법은 다음 3가지가 있다.  

1. XML스키마 기반의 POJO 클래스를 이용한 AOP구현  
2. AspectJ에서 정의한 @Aspect 어노테이션 기반의 AOP구현  
3. 스프링API를 이용한 AOP구현  

이중 XML스키마를 이용한 1번과 어노테이션을 사용한 2번을 알아보자

### Proxy 객체 만들어보기

위의 3가지 방법은 스프링에서 제공하는 기능을 이용해 Proxy클래스를 만드는 것 인데  
어떻게 Proxy 클래스가 만들어지는지 알기 위해 라이브러리를 사용하지 않고 Proxy 클래스를 만들어 보자.  

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
		int result = x + y; return result;
	}
	@Override
	public int sub(int x, int y) {
		int result = x - y; return result;
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

```

```java
public static void main(String[] args) {
	Calculator calc = new CaculatorImpl();
	System.out.println(calc.add(4,2));
	System.out.println(calc.sub(4,2));
}
```
출력값
```
6
2
```
핵심기능이 계산기 기능을 하는 연산과정이다.  

공통기능으로 핵심기능이 처리되는 시간을 로그로 기록하도록 설정하자.  

```java
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
```

```
6월 19, 2019 8:00:43 오후 CaculatorImpl add
정보: add() start
6월 19, 2019 8:00:43 오후 CaculatorImpl add
정보: add() stop
6월 19, 2019 8:00:43 오후 CaculatorImpl add
정보: add() 처리시간: 0
6
6월 19, 2019 8:00:43 오후 CaculatorImpl sub
정보: sub() start
6월 19, 2019 8:00:43 오후 CaculatorImpl sub
정보: sub() stop
6월 19, 2019 8:00:43 오후 CaculatorImpl sub
정보: sub() 처리시간: 0
2

```
모든 함수에 위와 같은 코드를 추가시키는 것이 부담스럽다... 공통적인 보조업무를 빼서 Proxy 클래스를 만들어 보자

`InvocationHandler` 인터페이스를 상속한 `LogPrintHandler`클래스를 정의해 공통 기능을 구현한 보조클래스와 핵심 기능이 합쳐진 proxy 클래스를 만들어보자.  
```java
public class LogPrintHandler implements InvocationHandler {
	private Object target;
	public LogPrintHandler(Object target) {  
		this.target = target;
	} 
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Log log = LogFactory.getLog(this.getClass());
		StopWatch sw = new StopWatch();
		log.info(method.getName() + "() start"); //method 이름을 가져오는 메서드
		sw.start();
		int result = (int) method.invoke(target, args);
		sw.stop();
		log.info(method.getName() + "() stop");
		log.info(method.getName() + "() 처리시간: " + sw.getTotalTimeMillis());
		return result;
	}
}
```
Spring AOP과정에서 위와같은 `LogPrintHandler`와 같은 **Proxy클래스**가 생기게 된다.  

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
```
6월 19, 2019 3:13:52 오후 aop.LogPrintHandler invoke
정보: add start
6월 19, 2019 3:13:52 오후 aop.LogPrintHandler invoke
정보: add stop
6월 19, 2019 3:13:52 오후 aop.LogPrintHandler invoke
정보: add 처리시간: 0
3
```

이제 스프링 프레임워크를 사용해서 `Proxy` 클래스를 만들어보자.  

### 스프링에서 Proxy객체 생성

스프링의 AOP기능을 사용하기 위해 먼저 다음 jar파일을 `build path`에 추가하자.  
`C:\Class\SpringClass\spring-framework-3.0.2.RELEASE-dependencies\org.aopalliance\com.springsource.org.aopalliance\1.0.0\com.springsource.org.aopalliance-1.0.0.jar`  

목표는 `add`메서드 **실행 전** 로그기록, **실행 후** 로그기록, 소요시간 로그 기록이다.  
즉 **핵심 기능 전 후** 에 실행되는 공통 기능인 `Around Advice`클래스를 만들자.

`Around Advice`기능을 가진 Proxy클래스를 정의하려면 먼저 공통기능 클래스를 정의하기 위한 `MethodInterceptor` 인터페이스를 구현해야한다.  
```java
@Component
public class LogPrintAroundAdvice implements MethodInterceptor{
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


만약 핵심기능 실행 전, 혹은 후에 공통기능을 먼저 사입하고 싶다면,  
핵심기능 수행전 매개변수, 핵심기능 수행 후 반환값을 사용해 공통 기능을 수행해야 한다면 

`Before Advice` 역할을 하는 `MethodBeforeAdvice` 인터페이스를 구현,  
`After Returning Advice`역할을 하는 `AfterReturningAdvice` 인터페이스를 구현한 공통 기능 클래스를 정의해야 한다.  

```java
@Component
public class LogPrintBeforeAdvice implements MethodBeforeAdvice {

	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		String m_name = method.getName();
		Log log = LogFactory.getLog(this.getClass());
		log.info(">>>  " + m_name + "() : BeforAdvice called...");
	}
}
```

```java
@Component
public class LogPrintAfterReturningAdvice implements AfterReturningAdvice{

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		// 수행이 완료된 후 호출되는 Advice임으로 첫번째 매개변수로 method가 반환한 값이 들어간다.
		String m_name = method.getName();
		Log log = LogFactory.getLog(this.getClass());
		log.info("<<<  " + m_name + "() : AfterReturningAdvice called...");
	}
}
```

위와 같이 AOP 기법을 사용한 공통 기능 클래스를 모두 정의하였으면 xml을 통해 핵심기능 클래스와 공통기능 클래스를 하나의 Proxy클래스로 만들어주면 된다.  

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


## 추가 내용

### ApplicationContext 메서드

지금까지 여러 `ApplicationContext`객체의 자식 클래스들 (`GenericXmlApplicationContext`, `AnnotationConfigApplicationContext`)의 `getBeans()`메서드를 추로 사용해 왔는데 다른 메서드들을 알아보자.  

**`BeanFactory` 제공 메서드**

한개의 Bean에 대한 정보를 구하는데 메서드의 초점이 맞춰져 있다.
|**메서드명**|**설명**|
|---|---|
`boolean containBean (String name)` | 인수에 지정된 이름의 Bean 이 정의되어 있는지 여부를 반환한다.
`Object getBean(String name)` | 지정된 이름의 Bean 인스턴스를 생성해서 반환한다. (형변환 필요)
`Object getBean(Class<T> requiredType)` | `requiredType`인 빈 객체를 구한다, 없거나 2개 이상이면 예외발생
`T getBean(String name, Class<T> requiredType)` | 인수에 지정된 이름의 requiredType에 지정한 타입으로 Bean 인스턴스를 생성해서 반환한다.
`Class<?> getType(String name)` | 이름을 통해 Bean의 type을 반환한다.

**`ListableBeanFactory` 제공 메서드** 
빈의 목록과 관련된 메서드를 정의하고 있다.
|**메서드명**|**설명**|
|---|---|
`int getBeanDefinitionCount()` | 전체 빈의 개수를 반환
`String[] getBeanDefinitionNames()` | 전체 빈의 이름 목록을 배열로 반환
`String[] getBeanNamesForType(Class<?> type)` | 지정한 타입의 빈의 이름 목록을 배열로 반환
`<T> Map<String, T> getBeansOfType(Class<T> type)` | 지정한 타입을 가진 빈 객체를 맵으로 반환, 키는 빈의 이름, 값은 빈 객체
`String[] getBeanNamesForAnnotation(Class<? extends Annotation> anntationType)` | 클래스가 지정한 어노테이션을 가진 빈 이름 목록을 배열로 반환
`Map<String, Object> getBeansWithAnnotation(Class<? extensd Annotation> annotationType)` | 지정한 어노테이션을 가진 빈 객체를 맵으로 반환

### 스프링 컨테이너 라이프 사이클

스프링 컨테이너의 생명주기는 다음과 같다.

1. 컨테이너 생성  
2. Bean 메타 정보(XML, 자바기반 설정)을 이용한 빈 객체 생성  
3. 컨테이너 사용  
4. 컨테이너 종료 (Bean 객체 제거)  

1번과 2번은 `GenericXmlApplicationContext` `AnnotationConfigApplicationContext`등의 객체를 사용해 스프링 컨테이너를 생성할 때 같이 진행됨.  

3번 과정은 `getBeans()`메서드를 사용해 스프링 컨테이너에 생성된 빈 객체를 사용 사용  

스프링 컨테이너를 다 사용했다면 `.close()`메서드를 사용해 제거, 빈 객체들도 같이 소멸된다.  


## 스프링 빈 객체 라이프 사이클

![image7]({{ "/assets/Spring/image7.png" | absolute_url }}){: .shadow}  

빈 객체 생성과 프로퍼티 설정은 스프링 컨테이너가 xml설정 혹은 자바코드 설정 파일을 통해 수행한다.  

그 이후로 이루어 지는 `BeanNameAware`인터페이스의 `setBeanNames()`메서드, `ApplicationContextAware`인터페이스의 `setApplicationContext()`메서드는 만약 빈 객체가 `BeanNameAware`과 `ApplicationContextAware` 인터페이스를 구현하고 있다면 생성후 호출되는 메서드들이다.  

`InitializingBean.afterPropertiesSet()`또한 마찬가지.   

전체적인 흐름은 `객체생성/프로퍼티 설정 -> 초기화 -> 사용 -> 소멸`
4단계를 거친다.

### `InitializingBean`, `DisposableBean` 인터페이스

```java
public interface InitializingBean {
  public void afterPropertiesSet() throws Exception;
}
public interface DisposableBean {
  public void destroy() throws Exception;
}
```

각각 빈 객체의 초기화, 소멸 단계에서 실행될 메서드의 구현을 필요로 할 때 사용되는 인터페이스이다.  

지금까지 초기화, 소멸과정에서 특별한 작업을 하는 스프링 빈 객체를 정의한 적이 없지만  
DB 커넥션 풀 기능을 가지는 빈 객체는 별도의 초기화, 소멸 과정이 필요하다.  

커넥션 풀을 생성하기 위해 초기화시 미리 커넥션을 생성해 두어야 하고  
더이상 커넥션이 필요 없을 때 생성한 커넥션을 모두 닫는 소멸 과정이 필요하다.  


### `@PostConstruct`, `@PreDestroy` 어노테이션


`InitializingBean`, `DisposableBean` 인터페이스에서 구현한 메서드들과 같은 기능을 하는 어노테이션

메서드 위에 어노테이션을 정의해 두면 빈 초기화, 소멸시 호출된다.  

```java
public class ConnectionPool {
	@PostConstruct
	public void initPool() {
		...
	}

	@PreDestroy
	public void destroyPool() {
		...
	}
}
```
어노테이션을 사용하기 위해 xml설정에서 `<context:annotation-config>`태그를 필요로한다.  

### 커스텀 `init`, `destroy` 메서드

`InitializingBean`, `DisposableBean` 인터페이스도 구현하기 싫고 `@PostConstruct`, `@PreDestroy` 어노테이션도 사용하기 싫고  
내가 지정한 메서드를 초기화, 소멸시 호출시키고 싶을 때  

빈 객체를 생성하는 xml설정 혹은 자바코드 설정으로 커스텀 `init`, `destroy`메서드를 정의 가능하다.  

`<bean id="connectionPool" class="spring.dbconn.ConnectionPool" init-method="init" destroy-method="destroy"/>`
```java
@Bean(initMethod="init", destroyMethod="destroy")
public ConnectionPool connectionPool() {
	return new ConnectionPool();
}
```

### `BeanNameAware`, `ApplicationContextAware` 인터페이스

빈 객체 초기화시 스프링 컨테이너를 사용해 다른 빈 객체를 가져와 작업을 한다던가  
생성된 빈 객체의 이름을 사용해 로그를 남기는 작업을 한다던가

스프링 컨테이너, 빈 객체 이름이 필요하다면 `BeanNameAware`, `ApplicationContextAware` 인터페이스의 메서드를 구현

```java
public interface ApplicationContextAware extends Aware {
	void setAppliactionContext(ApplicationContext applicationContext) throws BeansException;
}

public interface BeanNamesAware extends Aware {
	void setBeanNames(String name);
}
```

`setAppliactionContext`메서드를 사용해 필드에 스프링 컨테이너를 참조시킬 수 있다.  

`setBeanNames`메서드를 사용해 필드에 빈 객체 이름을 참조시킬 수 있다.  

같은 타입의 빈 객체가 여러개일 때 이름을 사용해 로그를 남기는 편....

### 빈 객체 범위(scope)

스프링의 범위는 총 2가지.

1. 싱글톤 범위
2. 프로토타입 범위

> 사실 request, session 범위가 존재하지만 잘 사용되지 않음으로 생략....

스프링은 기본(deafult)로 싱글톤 범위를 사용한다. 아래와 같이 빈 객체를 생성하고  

`<bean id="connPoll" class="spring.dbconn.ConnectionPool" />`

```java
AbstractApplicationContext ctx = new GenericXmlApplicationContext(configLocation);
ConnectionPool p1 = ctx.getBean("connPoll", ConnectionPool.class);
ConnectionPool p2 = ctx.getBean("connPoll", ConnectionPool.class);
```

위와 같이 ConnectionPool 객체를 2개 생성하면 모두 같은 인스턴스를 가리킨다.  

이는 기본적으로 스프링 컨테이너가 빈 객체를 싱글톤 방식으로 생성하기 때문...

> 만약 해당 빈 객체가 싱글톤 객체로 생성되는 것을 명시하고 싶다면 아래 설정 참고
```java
//<bean id="connPoll" class="spring.dbconn.ConnectionPool" scope="singleton"/>

@Bean
@Scope("singleton")
public ConnectionPool connectionPool() {
	return new ConnectionPool();
}
```

<br><br>

반면 프로토 타입으로 scope를 설정할 경우 여러개의 인스턴스 생성이 가능  

단 스프링 컨테이너는 프로토 타입 scope 빈 객체의 초기화 까지만 관리하고 소멸과정을 따로 관리하지 않는다.(스프링 컨테이너 종료시 같이 소멸되지 않음)  

일반적으로 사용되는 Scope는 싱글톤이고 만약 같은 타입의 객체를 여러개 생성해야 한다면 프로토 타입보만 팩토리 클래스를 만들어 사용하는 것이 좋다.  