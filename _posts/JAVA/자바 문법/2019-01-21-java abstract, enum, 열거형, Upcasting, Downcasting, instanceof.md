---
title:  "java abstract, enum, 열거형, Upcasting, Downcasting, instanceof, 다형성!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - Java
tags:
  - Java
  - 문법
---


## 단일 상속

다른 객체지향언어인 C++은 다중상속이 가능하지만 Java에서는 단일상속밖에 안된다.  

만약 `class A`, `class B`, `class C`, `class D`가 있고  
`class B`는 `A`를 상속, `class C`도 `A`를 상속한다  
```java
class B extends A{}
class C extends A{}
```

여기서 `class D`를 선언하고 B와 C를 다중상속한다 생각해보자.  
```class D extends B,C{} //에러발생```  

`D`클래스가 `A`클래스를 2번 상속하기 때문이다.  
  
C++의 경우 B가 상속받은 A를 상속받을지, D가 상속받은 A를 상속받을지 결정할 수 있지만  
Java의 경우 이런 로직을 만들어 두지 않았기때문에 허용하지 않는다.  

최근 만들어지는 언어들도 다중상속의 여러 문제점 때문에 다중상속 문법을 구현하지 않는다.  
사실 다중상속이 있는 C++도 다중상속을 잘 쓰지 않는다(여러 에러를 동반하기 때문)  

--------------------------------------------------------------------------------------------

## Object 객체

```java
public class Ex01 {
	public static void main(String[] args) {
		A a = new A();
		a.
	}
}

class A
{
	//empty	
}
```
위와같이 아무것도 없는 A라는 클래스를 정의하고 인스턴스a를 생성해서 .점을 찍어보자.  

몇개의 메소드들을 사용할수 있다고 이클립스가 알려준다(`equals`, `toString`, `wait`등등).  

`A`클래스에 만들지도 않은 메서드가 뜨는 이유는 컴파일러가 자동적으로 `Object`라는 자바의 최상위 클래스를  
상속하도록 있기 때문이다.  
`Class A extends Object{}`  

그래서 `class B extends A{}` 할경우 B도 상위의 상위 클래스인 `Object`를 자동으로 상속받는다.  

--------------------------------------------------------------------------------------------

## 추상메소드 - abstract

아직 어떻게 만들지 몰라서 몸체를 생략한 불완전한 메소드를 `abstract`메소드라 한다.  
이런 불완전한 메소드가 있는 클래스는 불완전한 클래스가 되고 `abstract`클래스라고 할 수 도 있겠다.  

```java
class Test01
{
	void add()
	{
		System.out.println("추가합니다.");
	}
	abstract void print(); //print를 아직 어떻게 구현할지 안정했다. 앞에 abstract 키워드를 붙이자.
	//에러가 뜨는데 class 앞에 abstract 키워드를 붙이면 된다. --> absract class Test01{ ... }
}
```

`absract class Test01{ ... }` &larr; 이렇게 `abstract`키워드를 `class` 앞에 붙이면 더이상 이 불완전한 클레스로 **객체생성(인스턴스화)하지 못한다.**  


불완전한 클래스를 상속하는 `SubTest01`이라는 클래스를 정의해보자.  
```java
class SubTest01 extends Test01
{
	int a;
	void minus()
	{
		System.out.println("감소합니다.");
	}
}
```
마찬가지로 에러난다. `SubTest01`앞에도 `abstract`를 붙이면 에러가 사라진다.  
`abstract class SubTest01 extends Test01{...}`  

또는 불완전한 메서드 `print()`를 **오버라이딩**해서 재정의하면 에러가 사라진다. 아래처럼  
```java
class SubTest01 extends Test01
{
	int a;
	void minus()
	{
		System.out.println("감소합니다.");
	}
	@Override
	void print() {
		// TODO Auto-generated method stub
	}
}
```

즉 모든 상위 클래스의 추상 메소드를 **모두 오버라이딩** 시키던가, 오버라이딩 못하겠으면 하위 클래스도 불완전한 클래스라고 알리는  
**`abstract` 키워드를 붙여주어야 한다.**  


--------------------------------------------------------------------------------------------

## 추상메소드 - abstract2

```java
abstract class Test02
{
	int a;
	void disp() {}
}
```

위의 클래스는 `abstarct` 메소드가 하나도 없는 완전한 클래스이다. 근데 `class`앞에 `abstract` 키워드가 붙었다.  

이유는 `Test02`객체 인스턴스화를 막기 위해서다. 완전한 클래스더라도 `abstract`키워드가 있으면 `new`연산을 통해 인스턴스화 시키지 못한다.  


```java
class SubTest02 extends Test02
{
	int a;
	void disp() {}
}
```
이번엔 `Test02`를 상속한 `SubTest02` 클래스를 선언하였다. 이녀석은 `new`연산을 통해 인스턴스화 가능하다.  

> 완전한 클래스인데 `abstract` 키워드를 붙임으로 부모는 생성못하게 막고 자식은 생성 가능하도록 구현할 수 있게된다.

--------------------------------------------------------------------------------------------

## 열거형 - enum

```java
public class Ex03 {
	enum DayOfWeek {SUN,MON,TUE,WED,THU,FRI,SAT} //; 있어도 되고 없어도 되고 (클래스로 변환되기때문에)
	public static void main(String[] args) {
		System.out.println(DayOfWeek.MON); //MON
		System.out.println(DayOfWeek.MON.name()); //MON
		System.out.println(DayOfWeek.MON.ordinal()); //1
		DayOfWeek week = DayOfWeek.MON;  //  요렇게 변수명으로 사용할 수 도 있음.
		switch(week)
		{
			case MON:  
			//스위치문에선 특이하게 열거형이름 생략, 
			//이미 week이 열거형이란걸 알고있기때문에
				break;
			case TUE:
				break;
			case WED:
				break;
			
			....
			
		}
	}
}
```
일일이 `final static int`로 상수 선언하자니 귀찮고 헷갈려서 생긴게 `enum`이다.   

생각해보면 고유명칭이 `int`형 변수로 저장되는것도 이상하다.(`int`형 변수와 비교하다보면 에러도 생긴다)  

`name()` 열거형 상수의 이름을 문자열로 반환한다.    

`ordinal()`은 열거형 상수값위치를 반환한다.맨 처음값이 `0`, 맨 뒤의 값 `N` 까지의 값을 반환할 수 있다.  

**열거형 변수간의 비교연산**으로 `==` 을 사용할 수 있지만 `><`는 사용할 수 없다.  

비교를 위해선 `compareTo()`를 사용해야 한다.    
비교대상이 같으면 `0`, 왼쪽이 크면`+`, 오른쪾이 크면`-` 를 반환한다.  

`valueOf(열거명.class, 문자열)` 아래와 같이 뜻이다.  
`valueOf(DayOfWeek.class, "MON")`는 `DayOfWeek.MON`와 같은 값을 반환한다.  

제어문을 사용할 때 편리할 수 있다.  

문자열에 해당하는 열거형 인스턴스를 반환한다. 문자열이 `MON`이라면 `MON`반환  
`.class`는 `Class`라는 **상위 클래스의 참조변수**로 해당 클래스 자료형 정보를 담고있는 `Class클래스`의 멤버(참조)변수이다.  


`enum`안의 값들이 `0~N`까지의 값으로 채워지지 않고 불규칙하게 채우려면 어떻게 해야할까?  
```java
enum DayOfWeek {SUN(10),MON(20),TUE(30),WED(40),THU(50),FRI(60),SAT(70)}; 
```
위같이 c언어 방식으로 하면 될것 같지만 안된다...  


```java
public class Ex03 {
	enum DayOfWeek {
		SUN(10),MON(20),TUE(30),WED(40),THU(50),FRI(60),SAT(70);
		
		private final int value;
		DayOfWeek(int value) //생성자
		{
			this.value = value;
		}
		public int getValue() 
		{
			return this.value;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(DayOfWeek.MON); //MON
		System.out.println(DayOfWeek.MON.name()); //MON
		System.out.println(DayOfWeek.MON.ordinal()); //데이터중 사전순서값 0~N
		System.out.println(DayOfWeek.MON.getValue()); //10
	}
}
```
출력값
```
MON
MON
1
20
```

자바에서는 c와는 다르게 **`enum`은 일종의 클래스이다.**  

`SUN`, `MON`, `TUE` 같은 열거형안의 참조변수(?) 모두가 `DayOfWeek`클래스 자료형의 **인스턴스**인샘  

사실 한번 값이 초기화되면 고정되니 참조변수는 아니다, 참조상수라 해야하나?  
`DayOfWeek`클래스 맴버로는 `int value`하나있다.   
  
그래서 `MON`, `TUE`, `WED`등은 인스턴스를 참조하는 일종의 **참조변수**,  
그렇기 때문에 `int`형변수가 아니라 비교가 안된다.  

그래서 특이한 값을 넣으려면 생성자도 있어야 하고 값을가져올 함수도 필요하다.  
물론 `DayOfWeek.MON.value` 이렇게 갖고와도 된다.  
  
디버그 결과 열거형 참조변수를 사용하는 순간 `DayOfWeek`안의  
모든 인스턴스(`MON~SAT`)가 생성자를 통해 `value`를 초기화한다.(한꺼번에)  

사용하는 순간 모든 `enum` 인스턴스들이 초기화 되서 메모리에 올라가나 보다.  
  
참고로 모든 열거형 클래스는 `Enum`이라는 상위 클래스가 있다, 이안에 `valueOf`라던가 `compareTo`  
메소드가 있는 것. 물론 `Enum`클래스는 `Object`클래스를 상속한다.  

조금 특이한 클래스....

> 상수를 전의하는 다양한 방법:  http://www.nextree.co.kr/p11686/

---------------------------------------------------------------------------------------------


## 클래스간 형변환 - UpCasting DownCasting

`AA` `BB` `CC` 클래스가 있고  

`class BB extends AA {}` 일때  

`UpCasting` `DownCasting` 알아보자  
 

### UpCasting
`AA a = new BB();`  
자식객체를 인스턴스화 시켜서 부모객체에 참조시킨다.  
이를 `UpCasting`이라 한다.  
(작은크기의 부모객체에 큰크기의 자식인스턴스를 참조시킴)  

이 과정은 딱히 **형 변환 과정이 필요 없다.**  

### Downcasting
그럼 반대로 `BB`에다 `AA`를 참조시켜보자  
`BB b = new AA();`  

오류난다. (`Type mismatch`) 형변홚 해서 다시 적용해보자.  

`BB b = (BB)new AA();`  
컴파일 오류는 없어졌지만 **런타임 오류가 발생**한다.  
처음부터 작은공간할당받고 이걸 억지로 넓히는건 불가능하다.  

참조변수`b`는 받은 인스턴스공간이 `BB`만큼 큰 공간인줄 알고 `BB`만의 공간(`BB`의 메서드, 변수가 존재하는)에 접근했는데 알고보니 아닌거다!  

```java
AA a = new BB();
BB b = (BB)a;
```
하지만 이거는 가능하다. `a`는 원래 큰 공간을 할당 받았었고 작게쓰고 있었는데 이걸 형 변환하여 `b`에게 주는거는 문제가 없다.  

즉 작은 부모객체를 인스턴스화 시켜서 주는게 아니라   
알고보니 자식객체의 인스턴스였는데 부모객체가 쓰던걸 형변환해서 다시 자식객체에게 주는것이 `DownCasting`이다.  


`UpCasting`에서 형변환이 필요없는 이유.   

`a`에는 고작 `add`메서드와 `print`메서드가 들어가있다.  
`b`에는 `add`와 `print는` 상속받고 `minus`, `multi`, `division` 메서드 가 있다.  

비록 `a`참조변수에 `BB`인스턴스가 할당었지만 `BB`인스턴스안에는 `add`와 `print는` 물론 `minus`, `multi`, `division`모두 들어가있다.  

따라서 `a`에서 `add`건 `print`건 `minus`건 모두 접근하는것이 가능하다.   

자바는 특이하게도 `virtual`키워드를 붙이지 않아도 하위클래스 메소드에 접근 제한이 없다.  
오버라이딩만 해놓으면 알아서 하위로 접근한다.  


`DownCasting`에서 형변환이 필요한 이유  
```java
AA a = new BB();
BB b = (BB)a;
```
애초에 `b`참조변수가 `AA`크기의 인스턴스를 받으면 안된다.   
`b`는 `BB`의 메소드를 써야하는데 `AA`공간에는 이 메서드가 없기때문,  
캐스팅을 통해 `b`에게 `BB`인스턴스라고 속여 할당하는식  
(`BB`인스턴스라고 속여서 줬는데 `AA`인스턴스이면 런타임 에러발생하고, 사실 진짜 `BB`인스턴스면 잘 실행되고)  

---------------------------------------------------------------------------------------------

## 다형성(polymorphism) - OOP 필수 개념 

클래스 4개 선언  
|class명|dscription|
|---|---|
|`Employee`|(사원), 이름, 주소, 연락처, 입사일자 를 필드로 갖고있다.|
|`Regular` extends Employee|(정규직), `Employee` + 기본급 - 기본생성자, 매개변수 5개 생성자.|
|`SalesMan` extends Regular|(영업직), `Regular` + 판매개수 + 물품금액|
|`TempEmployee` extends Employee|(임시직), `Employee` + 출근일수 + 일급|

*다형성은 `UpCasting`과 오버라이딩을 통해 알수있는 개념이다.*  

`Employee` 객체로 **다양한(다형) 인스턴스를 모두 참조할 수 있다.**  
 
다형성과 `abstract`는 항상 자주 붙어다니는데 `getPay`라는 함수로 예를 들어보면   

`Employee`는 그저 사원의 이름, 주소정도만 저장하는 객체이다 (절대 생성을 목표로 만들어진게 아님)  

이렇게 코드 절약을 위해 생겨난 상속만을 위한 클래스를 추상클래스라 한다.  

`Employee`에 `getPay`라는 추상 메소드를 만들고  
`Employee`를 상속하는 하위 클래스들은 모두 `getPay`를 오버라이딩 해야한다.  

보직마다 모두 다른 급여형태로 오버라이딩 될거고  
이게 다형성을 의미한다.  

`Employee`같은 추상 클래스 객체에 할당된 인스턴스들은 인스턴스 종류에 따라 `overrding`된 **각기 다른 `getPay`함수를 수행**할 것이다.  

`Employee`클래스를 배열로 만들고 각종 다른 종류의 사원들은 이 배열안에 모두 인스턴스화 될수 있다. 
관리도 편하고 다양하고!  

함수에서 `Employee`객체로 parameter를 사용하면 `Employee`를 상속하는 하위클래스가 매개변수로 들어와도 하나의 함수로 모두 커버 가능하다.  

---------------------------------------------------------------------------------------------

## instanceof 연산자

다형성에 `UpCasting`이였다면 `instanceof`에는 `DownCasting`이다.   

아무생각없이 `DownCasting`쓰면 **런타임 에러**나는데  
`instanceof`를 쓰면 자기보다 큰 인스턴스를 할당 받을수있는걸 확신할 수 있다.  

*`instanceof`란 어떤 인스턴스인지 물어보는 연산자이다*  

```java
private static void printEmpPay(Employee emp)
{
	String empName;
	if(emp instanceof SalesMan) //emp가 SalesMan이니?
	{
		SalesMan e = (SalesMan)emp; 
		//확인하고 downcasting하자 안그러면 런타임 에러나니까
		empName = "영업직";
	}
	else if(emp instanceof TempEmployee)
	{
		empName = "임시직";
	}
	else if(emp instanceof Regular)
	{
		empName = "정규직";
	}
	else if(emp instanceof Employee)
	{
		empName = "사원";
	}
	System.out.printf("%s의 급여: %d", empName, emp.getPay());
}
```
주의할점은 `instanceof`은 상위 하위 인스턴스 구분을 못한다.  
만약 `if(emp instanceof Employee)`을 `if`문들중 가장 위에 올려다 놓으면 모두 사원으로 출력이 된다. 

(모두 Employee를 상위객체로 두고있으니까!). 쓸거면 자식부터 물어보자.  

그럼 `DownCasting`하는이유는 뭘까  
```java
class Parent {}
class Child extends Parent {} 
```
여기서 `Child`만 갖고있는 함수를 하나 만들자  
`public void printChild() {}`

그리고 별도의 함수를 선언한다. 그리고 `child`만 갖고있는 함수를 호출해보자.  
```java
public static void DownCastindTest(Parent p)
{
	//p.printChild(); //에러난다. 만약 이 함수를 쓰고 싶다면 Parent객체인 p를 Child로 DownCasting해줘야한다.
	if(p instanceof Child)
	{
		Child c = (Child)p;
		c.printChild();
	}
} 
```