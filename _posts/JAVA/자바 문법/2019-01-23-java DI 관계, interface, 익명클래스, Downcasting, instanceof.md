---
title:  "java DI 관계, interface, WindowListener & WindowAdapter, 익명클래스, Downcasting, instanceof!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

## DI(Dependency Injection)관계

`Car`와 `Engine`클래스가 있을때 `has-a` 관계이다.  
```java
class NewCar
{
	public Engine eng = new Engine();
}
```

보면 명시적 초기화? 방법으로 `eng`을 `new`연산자를 통해서 객체 선언과 동시에 인스턴스화 하면서 초기화했다.  

이런 방법을 "**결합력 강한**" 코딩이라고 한다. (`NewCar`생성과 동시에 `Engine`도 생성되고 `eng`을 다른 인스턴스로 교체가 불가능)  

만약 더 좋은 엔진이 생기면(새로운 `Engine`클래스 정의) 교체하고 싶어질건데 결합력이 강하면 교체가 불가능하다!  

```java
public NewCar()
{	
	this.eng = new Engine();
}
```
이렇게 사용한다면 나중에 `Engine`객체가 `abstract` **추상화객체가 되면 사용 불가능해진다.**  
결합력은 보다 약해졌지만 좋은 코딩은 아니다.  

```java
public NewCar(Engine engine)
{	
	this.eng = engine;
}
```

이렇게 생성자를 만들고 밖에서 인스턴스를 만든후 초기화하자   

`NewCar car1 = new NewCar(new S_Engine);`   
`S_Engine`과 `Engine`객체는 상속관계이다.   

`car`객체를 만들때 마다 `Upcasting`을 통해 여러 `engine`객체를 상속하는 다른 `Engine`으로 인스턴스화 할 수 있다.  

`has-a` 관계를 의존성(`Dependency`)관계라고 하는데 이런 관계에서 밖에서 인스턴스를 만들고 객체에 생성자를 통해 주입하는 방법을  
`Dependency + Injection = DI` 관계라고 한다.  

--------------

## interface - 다형성을 위한 클래스

일종의 추상클래스인데 `abstract`키워드가 붙는 클래스보다 **추상화 강도가 높아서**(더 불안정한)  
가질수 있는 멤버가 보다 제한된다.

인터페이스는 인스턴스변수와 인스턴스메서드는 가질 수 없다.  

가질수 있는건 `final`로 선언된 상수, `abstract`키워드가 붙은 추상메서드 정도이다.  
`JDK 1.8`부터 추가로 가질수 있는게 `default`메소드와 `static`메소드가 있다.  

그래서 `interface`클래스가 가질수 있는 종류는 총 4가지이다.  
(상수, 추상메서드, 디폴트메서드, 정적메서드)  

### 인터페이스 선언형식

```java
interface 인스페이스명
{
	final ...   		//상수 필드
	
	abstract ...();		//추상 메서드
}
```

만약 `final` 제어자만 붙여 선언했다면 아래와 같이 **자동으로 다음 접근제어타와 기타제어자가 붙어 컴파일 된다**  
`final ...` &rarr; `public static final ...`   

메소드 또한 `abstract`와 `public`키워드가 자동으로 붙어 컴파일된다.  
`void ....()` &rarr; `public abstract void ...()`  

```java
public interface IEmployee {
	int MALE = 1;       //&rarr;public static final int MALE = 1;
	int FEMALE = 0;		//&rarr;public static final int FEMALE = 0;
	
	int getPay();		//&rarr;public abstract int getPay();
	void dispEmpInfo(); //&rarr;public abstract void dispEmpInfo();
}
```
`interface`를 하나 선언하고 

```java
public abstract class Employee implements IEmployee{
	private String name;
	private String addr;
	...
}
```
이를 상속하는 `Employee`객체를 만들자.  
`interface`역시 `abstract`처럼 무조건 정의된 메서드는 자식클래스가 오버라이딩 해주어야 한다.  

이제 `Employee`클래스와 `Employee`를 상속하는 클래스는 무조건 `getPay`와 `dispEmpInfo`함수를 오버라이딩해야한다.  

즉 `interface`는 재사용의 목적보단 규격을 정하고 사용법을 명확히 하기위해 사용되는 키워드이다.  

해당 추상클래스를 상속하는 클래스들이 무조건 구현해야하는 메소드들이 있다면 `interface`로 추상클래스를 만드자  


예로 `ArrayList` 라는 클래스가 어떤 추상클래스(`interface`)들과 상속관계에있는지, 어떤 규격제약이 있는지지 알아보자.  
```java
public class ArrayList<E>  
extends AbstractList<E>  
implements List<E>, RandomAccess, Cloneable, Serializable  
```

`List<E>, RandomAccess, Cloneable, Serializable` 안에는 여러가지 기능들이 있을거다,    

복제, 랜덤접근(생성), 직렬화기능 등등 이런 기능들을 `ArrayList`만의 메소드로 오버라이딩을 통해 구현되어있을것이다.  

### 인터페이스 다중상속 

**인터페이스 끼리는 상속이 가능하다. 또한 동시에 여러개 구현할 수 있다.**  

```java
interface IA{
	void a();
	void b();
}

interface IB{
	void c();
}

// 인터페이스 끼리 상속이 가능
interface IC extends IA{
	// void a()
	// void b()
	void c();
}

// The type AA must implement the inherited
// abstract method IA.a()
// 인터페이스를 사용한 다중 상속이 가능하다.
class AA implements IA, IB{	
	@Override
	public void a() {}

	@Override
	public void b() {}

	@Override
	public void c() {}

```

인터페이스 객체와 인터페이스를 `implements`한 클래스를 인스턴스화 시켜서 `Upcasting`으로 참조시키자.  
`IA obj = new AA();`  

`obj`는 `IA`의 함수 `a()`, `b()` 만 사용 가능하고 `AA`가 `implements`하고있는 `IB`의 `c()`는 사용 불가능하다.   

이는 다중상속의 문제점을 막기위해(만약 `IA`에도 `c()`라는 함수가 있다면 어느 `interface`의 `c()`인지 모르니까) 구역을 정해놓은듯 하다.  

```java
interface IA{
	void a();
	void b();
}
interface IC extends IA{
	void c();
}
class AA implements IC{
@Override
	public void a() {}

	@Override
	public void b() {}

	@Override
	public void c() {}
}
```

`IC`와 `IA`는 상속관계이다.  

여기서 `IA obj = new AA()` 역시 가능하다.  
물론 obj에서 호출 가능한 함수는 `a()`와 `b()`밖에 없다.  
이는 다형성이 가능하단 뜻이다.  

만약 `IA`를 상속하는 `interface`가 `IC`말고 `ID~IZ`까지 있다면,  
그리고 `ID~IZ` `interface`를 `implements`하는 여러 클래스(`AA`같은)들이 있다면  

이 여러 클래스를 모두 `IA`와 `Upcasting`을 통해 참조가능하다!  

물론 해당 클래스의 모든 메서드를 쓸순없고 `IA`추상메서드를 `overriding`하는 메서드만 쓸 수 있다.  

### 인터페이스의 장점
* 개발시간 단축 &rarr; (물론 유지보수 입창에서 시간단축, 인터페이스 사용하면 처음 개발 시간은 더 길다)  


* 표준화 가능 &rarr; 함수이름을 인터페이스에 정해놓으면 이를 구현하는 클래스들의 함수도 같아야하고 기능을 직관적으로 알 수 있다.  


* 서로 관계없는 클래스간 관계 형성(다형성) &rarr; `ArrayList`도 `Serializable`를 구현하고 `Employee`도 `Serializable`구현했을때 서로 연관지을 수 있다.  


* 독립적 프로그래밍 가능  

가장 중요한건 **표준화**와 **다형성**이다.  

### 인터페이스 역할
1. 객체 사용방법을 정의한 타입  
2. 객체의 교환성을 높여준다. (다형성 구현에 중요한 역할)  


물론 객체 인터페이스`type`에 따라 구현될 메소드만 사용 가능하다.  


**요약**  
* 인터페이스는 객체를 생성할 수 없기에 생성자가 필요없다.  
  
* 인터페이스는 일종의 클래스임으로 .class파일 생성됨  
  
* 인터페이스끼리는 상속이 가능하다.  
  
* 인터페이스는 여러개 implement(구현, 다중상속)할 수 있다.  


### interface - static, default 메소드

`default`메소드를 `interface`에 선언 해놓으면 **해당 메소드는 오버라이딩 하지 않아도 된다.**(이것만큼은 완전한 메소드 인정!)  

만약 인터페이스에 메서드를 추가해야 한다면 인터페이스를 구현하는 모든 클래스들이 같이 메서드를 추가해줘야 했다.  
그런데 `default` 키워드를 사용하면 걱정할 필요없다! 오버라이딩 하지 않더라도 오류가 나지 않음.

`interface`의 `static`메서드 또한 마찬가지.  
인스턴스 생성없이 사용가능한 `static`메서드를 인터페이스에서 충분히 사용할 수 있다.   

단 static은 오버라이딩 개념이 아니라 그냥 `static`메서드 사용하는것.  

> 참고로 인터페이스에서 사용하는 상수필드 `static`자동으로 붙기때문에 모든 필드 사용가능하다.  

---------------

## `WindowListener`와 `WindowAdapter`

`WindowListener`는 윈도우 이벤트를 처리해주는 `interface`이다.  

**윈도우 폼**을 만들고 이벤트처리를 하고싶다면 `WindowListener`을 `implement`(구현)하고 안에 추상메서드를 모두 `overide`해야한다.  
(`overide`할 이벤트처리용 추상메서드가 좀 많다....)  

내가 구현할건 닫기이벤트인 `windowClosing`밖에 없는데 이를 모두 구현하려면 좀 짜증난다.  

그래서 사용하는게 `WindowAdapter`  

`WindowAdapter`의 구조는 우선 `WindowListener`를 `implement`하고  
그안의 추상메서드를 모두 구현해놓았다(**공백으로**).

우리는 `WindowListener`의 추상메소드를 모두 구현한 완전한 "클래스"인 `WindowAdapter`를 사용하기만 하면 된다.  

```java
class MyForm extends Frame  {
	// 기본 생성자
	public MyForm() {
		this.setTitle("예제");
		this.setSize(500,500);
		this.addWindowListener(new FirstAdapter());
		this.setVisible(true);
	}
} // class

class FirstAdapter extends WindowAdapter  
{ //여기서 사용할 이벤트 (추상)메서드만 재정의
	public void windowClosing(WindowEvent e) { 
		System.exit(-1);
	}
}
```

----------------------------------------------------------------------------------

## 익명클래스 - Anonymous class

1회용으로 한번쓰고 안쓰는 클래스 선언과 동시에 인스턴스화(생성) 되는 클래스  

`new 클래스명();`  
평범한 객체 인스턴스화 구문이다  

`new 조상클래스명() { ... }`  
익명클래스 객체 선언과 동시에 인스턴스화 구문  
`{...}` 안에 각종 멤버들이 들어감    
멤버는 조상클래스를 `overiding`한 메서드들이 들어간다.  

`interface`로 익명클래스만들수 도 있다.    

`new 인터페이스명() { ... }`  
... 안에 멤버는 인터페이스의 추상메서드를 오버라이딩한 메서드들이 있다.  
단 인터페이스이기 때문에 모두 오버라이딩 해줘야한다.  

익명클래스는 java뿐 아니라 각종 언어에서 자중 사용되는 개발기법이다. 잘 알아두도록 하자....

```java
class MyForm extends Frame  {
	// 기본 생성자
	public MyForm() {
		this.setTitle("예제");
		this.setSize(500,500);
		this.addWindowListener(new FirstAdapter());
		this.setVisible(true);
	}
} // class

class FirstAdapter extends WindowAdapter  
{ //여기서 사용할 이벤트 (추상)메서드만 재정의
	public void windowClosing(WindowEvent e) { 
		System.exit(-1);
	}
}
```

아까 봤던 코드인데 `FirstAdapter` 만드는 과정을 **익명클래스**를 사용하면 생략 할 수 있다.  
`FirstAdapter`라는 이름주기도 귀찮고 `class`정의할 위치찾는것도(파일추가하고 이름바꾸고...등등) 귀찮다할 때 익명클래스를 사용하면 된다!!  
```java
class MyForm extends Frame  {
	// 기본 생성자
	public MyForm() {
		this.setTitle("예제");
		this.setSize(500,500);
		this.addWindowListener(new WindowAdapter() {//WindowAdapter를 상속하는 익명클래스 생성
			public void windowClosing(WindowEvent e) { 
				System.exit(-1);
			}
		});
		//WindowAdapter 익명클래스 선언과 동시에(메서드 오버라이딩) new를 통한 생성
		this.setVisible(true);
	}
} // class
```
익명클래스의 **클래스파일**은 만들어 지긴 하는데 클래스를 생성한 클래스(`MyForm`)이름에 `$index`번호가 붙는다  
&rarr;`MyForm$1.class`

-----------