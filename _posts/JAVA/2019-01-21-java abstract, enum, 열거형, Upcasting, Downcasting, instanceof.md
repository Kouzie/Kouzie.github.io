---
title:  "java abstract, enum, 열거형, Upcasting, Downcasting, instanceof!"
read_time: false
share: false
toc: true
author_profile: false

categories:
  - Java
tags:
  - Java
  - 문법
---


### 단일 상속

다른 객체지향언어인 C++은 다중상속이 가능하지만 Java에서는 단일상속밖에 안된다.

만약 class A, class B, class C, class D가 있고
class B는 A를 상속, class C도 A를 상속한다
```
class B extends A{}
class C extends A{}
```
여기서 class D를 선언하고 B와 C를 다중상속한다 생각해보자.
```class D extends B,C{} //에러발생```

D클래스가 A클래스를 2번 상속하기 때문이다.
C++의 경우 B가 상속받은 A를 상속받을지, D가 상속받은 A를 상속받을지 결정할 수 있지만
Java의 경우 이런 로직을 만들어 두지 않았기때문에 허용하지 않는다.

최근 만들어지는 언어들도 다중상속의 여러 문제점 때문에 다중상속 문법을 구현하지 않는다.
사실 다중상속이 있는 C++도 다중상속을 잘 쓰지 않는다(여러 에러를 동반하기 때문)

--------------------------------------------------------------------------------------------

### Object 객체

```
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
몇개의 메소드들을 사용할수 있다고 이클립스가 알려준다(equals, toString, wait등등).
이유는 코딩을 따로 하지는 않았지만 컴파일러가 자동적으로 A라는 클래스가 Object라는 자바의 최상위 클래스를
상속하도록 있기 때문이다. --> `Class A extends Object{}`

그래서 `class B extends A{}` 할경우 B도 상위의 상위 클래스인 Object를 자동으로 상속받는다.

--------------------------------------------------------------------------------------------

### 상속에서 final

클래스 앞에 final키워드가 붙으면 자식을 가질수 없는 최종 클래스란 뜻이다.

메소드 앞에 final키워드가 붙으면 더이상 오버라이딩 할 수 없는 최종 메소드란 뜻이다.

필드 앞에 붙으면 그냥 변경할수 없는 상수화 시키는것..(상속이랑 관계 X)
final 변수를 초기화 할수 있는 방법은 여러가지다.
1. final변수 선언과 동시에 초기화 하는 방법
2. 생성자에서 초기화 하는 방법.
3. 초기화 블럭에서 초기화 하는 방법.
물론 생성자에서 초기화 할때 이렇게 하면 오류난다.
```
class A
{
	final int t;
	A()
	{
		t=100;
	}
	A(int num)
	{
		this();
		//t = 200; //오류발생, t가 두번 초기화됨.
	}
}
```

--------------------------------------------------------------------------------------------

### 추상메소드 - abstract

아직 어떻게 만들지 몰라서 몸체를 생략한 불완전한 메소드를 abstract메소드라 한다.
이런 불완전한 메소드가 있는 클래스는 불완전한 클래스가 되고 abstract클래스라고 할 수 도 있겠다.

```
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

`absract class Test01{ ... }` <-- 이렇게 abstract키워드를 class 앞에 붙이면 더이상 이 불완전한 클레스로 객체생성(인스턴스화)하지 못한다.


불완전한 클래스를 상속하는 SubTest01이라는 클래스를 정의해보자.
```
class SubTest01 extends Test01
{
	int a;
	void minus()
	{
		System.out.println("감소합니다.");
	}
}
```
마찬가지로 에러난다. SubTest01앞에도 abstract를 붙이면 에러가 사라진다. --> `abstract class SubTest01 extends Test01{...}`
또는 불완전한 메서드 `print()`를 **오버라이딩**해서 재정의하면 에러가 사라진다. 아래처럼
```
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

즉 모든 상위 클래스의 추상 메소드를 *모두 오버라이딩 시키던가, 오버라이딩 못하겠으면 하위 클래스도 불완전한 클래스라고 알리는
abstract 키워드*를 붙여주어야 한다.


--------------------------------------------------------------------------------------------

### 추상메소드 - abstract2

```
abstract class Test02
{
	int a;
	void disp() {}
}
```

위의 클래스는 abstarct 메소드가 하나도 없는 완전한 클래스이다. 근데 class앞에 abstract 키워드가 붙었다.

이유는 Test02객체 인스턴스화를 막기 위해서다. 완전한 클래스더라도 abstract키워드가 있으면 new연산을 통해 인스턴스화 시키지 못한다.


```
class SubTest02 extends Test02
{
	int a;
	void disp() {}
}
```
이번엔 Test02를 상속한 SubTest02 클래스를 선언하였다. 이녀석은 new연산을 통해 인스턴스화 가능하다.

*완전한 클래스인데 abstract 키워드를 붙임으로 부모는 생성못하게 막고 자식은 생성 가능하도록 구현할 수 있게된다.*

--------------------------------------------------------------------------------------------

### 열거형 - enum

```
public class Ex03 {
	enum DayOfWeek {SUN,MON,TUE,WED,THU,FRI,SAT} //; 있어도 되고 없어도 되고 (클래스로 변환되기때문에)
	public static void main(String[] args) {
		System.out.println(DayOfWeek.MON);
		System.out.println(DayOfWeek.MON.name());
		System.out.println(DayOfWeek.MON.ordinal());
		DayOfWeek week;  // <-- 요렇게 변수명으로 사용할 수 도 있음.
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
일일이 final static int로 상수 선언하자니 귀찮고 헷갈려서 생긴게 enum이다.
그리고 고유명칭이 int형 변수로 저장되는것도 이상하다.(int형 변수로 비교하다보면 에러도 생긴다)

`name()` 열거형 상수의 이름을 문자열로 반환한다.
`ordinal()`은 열거형 상수값위치를 반환한다.맨 처음값이 0, 맨 뒤의 값 N 까지의 값을 반환할 수 있다.
열거형 변수간의 비교는 "==" 을 사용할 수 있지만 "><"는 사용할 수 없다. 비교를 위해선 `compareTo()`를 사용해야 한다.
비교대상이 같으면 0, 왼쪽이 크면+, 오른쪾이 크면- 를 반환한다.
`valueOof(열거명.class, 문자열)` swtich로 비교시 유용하게 사용
`열거형명 참조변수명 = valueOof(열거형명.class , 문자열);`
문자열에 해당하는 열거형 인스턴스를 반환한다. 문자열이 MON이라면 MON반환
.class는 Class라는 상위 클래스의 참조변수로 해당 클래스 자료형 정보를 담고있는 Class클래스의 멤버(참조)변수이다.


enum안의 값들이 0~N까지의 값으로 채워지지 않고 불규칙하게 채우려면 어떻게 해야할까?
```
enum DayOfWeek {SUN(10),MON(20),TUE(30),WED(40),THU(50),FRI(60),SAT(70)}; 
```
위같이 c언어 방식으로 하면 될것 같지만 안된다...


```
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
		System.out.println(DayOfWeek.MON);
		System.out.println(DayOfWeek.MON.name());
		System.out.println(DayOfWeek.MON.ordinal()); //데이터중 사전순서값 0~N
		System.out.println(DayOfWeek.MON.getValue());
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

자바에서는 c와는 다르게 enum은 일종의 클래스이다.
SUN, MON, TUE 같은 열거형안의 참조변수(?) 모두가 DayOfWeek클래스 자료형의 인스턴스인샘
사실 한번 값이 초기화되면 고정되니 참조변수는 아니다, 참조상수라 해야하나?
DayOfWeek클래스 맴버로는 int value하나있다.

그래서 MON, TUE, WED등은 인스턴스를 참조하는 일종의 참조변수,
그렇기 때문에 int형변수가 아니라 비교가 안된다.
그래서 특이한 값을 넣으려면 생성자도 있어야 하고 값을가져올 함수도 필요하다.
물론 DayOfWeek.MON.value 이렇게 갖고와도 된다.

디버그 결과 열거형 참조변수를 사용하는 순간 DayOfWeek안의
모든 인스턴스(MON~SAT)가 생성자를 통해 value를 초기화한다.(한꺼번에)
사용하는 순간 모든 enum 인스턴스들이 초기화 되서 메모리에 올라가나 보다.

참고로 모든 열거형 클래스는 Enum이라는 상위 클래스가 있다, 이안에 valueOf라던가 compareTo
메소드가 있는거. 물론 Enum클래스는 Object클래스를 상속한다.

---------------------------------------------------------------------------------------------


### 클래스간 형변환 - UpCasting DownCasting

AA BB CC 클래스가 있고
`class BB extends AA {}` 일때 UpCasting DownCasting 알아보자


#### UpCasting
`AA a = new BB();`
자식객체를 인스턴스화 시켜서 부모객체에 참조시킨다.
이를 UpCasting이라 한다. (작은크기의 부모객체에 큰크기의 자식인스턴스를 참조시킴)
이 과정은 딱히 형 변환 과정이 필요 없다. 

#### Downcasting
그럼 반대로 BB에다 AA를 참조시켜보자
`BB b = new AA();`
오류난다. (Type mismatch, 형변환 하시오)

`BB b = (BB)new AA();`
컴파일 오류는 없어졌지만 런타임 오류가 발생한다. 처음부터 작은공간할당받고 이걸 억지로 넓히는건 불가능하다.
b는 받은 인스턴스공간이 BB만큼 큰 공간인줄 알고 BB만의 공간에 접근했는데 알고보니 아닌거다! 그래서 런타임 오류가 발생
```
AA a = new BB();
BB b = (BB)a;
```
하지만 이거는 가능하다. a는 원래 큰 공간을 할당 받았었고 작게쓰고 있었는데 이걸 형 변환하여 b에게 주는거는 문제가 없다.

즉 작은 부모객체를 인스턴스화 시켜서 주는게 아니라
알고보니 자식객체의 인스턴스였는데 부모객체가 쓰던걸 형변환해서 다시 자식객체에게 주는것이 DownCasting이다.


UpCasting에서 형변환이 필요없는 이유.
a에는 고작 add메서드와 print메서드가 들어가있다. b에는 add와 print는 상속받고 minus, multi, division 메서드 가 있다.
비록 a객체에 에 BB인스턴스가 할당었지만 BB인스턴스안에는 add와 print는 물론 minus, multi, division모두 들어가있다.
따라서 a에서 add건 print건 minus건 모두 접근하는것이 가능하다. 
(자바는 특이하게도 virtual키워드를 붙이지 않아도 하위클래스 메소드에 접근 제한이 없다, 오버라이딩만 해놓으면 알아서 하위로 접근)

DownCasting에서 형변환이 필요한 이유
```
AA a = new BB();
BB b = (BB)a;
```
애초에 b입장에서 AA크기의 인스턴스를 받으면 안된다. b는 BB의 메소드를 써야하는데 AA인스턴스를 받으면 안되기 때문, 캐스팅을 통해 b에게 BB인스턴스라고 속요 할당하는식(BB인스턴스라고 속여서 줬는데 AA인스턴스이면 런타임 에러인거고, 사실 진짜 BB인스턴스면 잘 실행되고)

---------------------------------------------------------------------------------------------

### 다형성(polymorphism) - OOP 필수 개념 

클래스 4개 선언
Employee						(사원)
Regular extends Employee			(정규직)
SalesMan extends Regular			(영업직)
TempEmployee extends Employee		(임시직)

Employee 		- 이름, 주소, 연락처, 입사일자 를 필드로 갖고있다.
Regular 			- Employee + 기본급 - 기본생성자, 매개변수 5개 생성자.
SalesMan 		- Regular + 판매개수 + 물품금액
TempEmployee	- Employee + 출근일수 + 일급

*다형성은 UpCasting과 오버라이딩을 통해 알수있는 개념이다.*

Employee 객체로 다양한(다형) 인스턴스를 모두 참조할 수 있다.

다형성과 abstract는 항상 자주 붙어다니는데 getPay라는 함수로 예를 들어보면
Employee는 그저 사원의 이름, 주소정도만 저장하는 객체이다 (절대 생성을 목표로 만들어진게 아님)
이렇게 코드 절약을 위해 생겨난 상속만을 위한 클래스를 추상클래스라 한다. Employee에 getPay라는 추상 메소드를 만들고
Employee를 상속하는 하위 클래스들은 모두 getPay를 오버라이딩 해야한다. 보직마다 모두 다른 급여형태로 오버라이딩 될거고
이게 다형성을 의미한다.

Employee같은 추상 클래스 객체에 할당된 인스턴스들은 인스턴스 종류에 따라 overrding된 각기 다른 getPay함수를 수행할 것이다.

Employee클래스를 배열로 만들고 각종 다른 종류의 사원들은 이 배열안에 모두 인스턴스화 될수 있다. 관리도 편하고 다양하고!
함수에서 Employee객체로 parameter를 사용하면 Employee를 상속하는 하위클래스가 매개변수로 들어와도 하나의 함수로 모두 커버 가능하다.

---------------------------------------------------------------------------------------------

### instanceof 연산자

다형성에 UpCasting이였다면 instanceof에는 DownCasting이다.
아무생각없이 DownCasting쓰면 런타임 에러나는데 물어보고
instanceof를 쓰면 자기보다 큰 인스턴스를 할당 받을수있는걸 확신할 수 있다.

*instanceof란 어떤 인스턴스인지 물어보는 연산자이다*

```
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
주의할점은 instanceof은 상위 하위 인스턴스 구분을 못한다. 만약 `if(emp instanceof Employee)`을 if문들중 가장 위에
올려다 놓으면 모두 사원으로 출력이 된다(모두 Employee를 상위객체로 두고있으니까!). 쓸거면 자식부터 물어보자.

그럼 DownCasting하는이유는 뭘까
```
class Parent {}
class Child extends Parent {} 
```
여기서 Child만 갖고있는 함수를 하나 만들자
`public void printChild() {}`

그리고 별도의 함수를 선언한다. 그리고 child만 갖고있는 함수를 호출해보자.
```
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