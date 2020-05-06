---
title:  "java 제네릭, 와일드카드, Date, Calendar, SimpleDateFormat!"
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

## 제네릭(Generics)

`jdk1.5` 부터 추가되었다.  

다양한 타입의 객체들을 다루는 메서드, 컬렉션 클레스에
컴파일 시에 타입 체크(Complie Time Type Check)를 해주는 기능.  

객체의 타입 안정성, 형변환 번거로움을 줄인다.  

제네릭이 없을때 클래스 안에 여러 타입의 멤버를 사용하려면 템플릿 클래스를 써야했다.
```java
class Box
{
	Object content
	public Box(Obejct something) {this.content = something}
}
```
`Box` 생성자에 어떤 타입의 변수가 와도 상관 없다.

하지만 항상 형변환과정을 거쳐 사용해야하고 안정성이 떨어진다.

제네릭을 사용하면 형변환을 생략할 수 있어 코드가 간결해지고 안정성이 높아진다.

이 템플릿 클래스를 지네릭 클래스로 변환해보자.
```java
class Box <T>
{
	T content;
	public Box(T something) {this.content = something}
}
```

### 제네릭 용어

`Box<T>` : 제네릭클래스, 혹은 T의 Box, T Box(클래스명)   
`T` : 타입변수, 타입매개변수   
`BoX` : 원시타입   

### 제네릭 제한

`class Box <T extends Employee> {}` - Employee의 하위 객체들만 타입변수로 올 수 있음.  
`class Box <T super SalesMan> {}` - SalesMan의 상위 객체들만 타입변수로 올 수 있음.  
`class Box <T super SalesMan & 인터페이스명> {}` - SalesMan의 상위 객체이고 해당 인터페이스를 구현한 객체만 타입변수로 올 수 있음.  

---

## 와일드카드 <?>

다음과 같이 **`static`메서드**가 정의되어있다.
```java
class Juicer {
	static Juice makeJuice(FruitBox<Fruit> box)	{
	...
	}
}
```
해당 메서드는 제네릭 메서드가 아니다. 그저 Fruit을 타입변수로 받는 `FruitBox` 객체를 매개변수로 받는 정적 메서드이다.  
`class Apple extends Fruit {}` 라는 Apple클래스를 만들고   
`FruitBox<Apple> abox` 객체를 만들어 `makeJuice` 메서드의 매개변수로 넣을 수 있을것 같지만 안된다.  

그리고 다음과 같은 `static`메서드는 사용할 수 없다.   
`static Juice makeJuice(FruitBox<T> box)`  

> `staic`메서드가 메모리에 올라갈때 `T`가 제너릭 타입인지, `T`라는 클래스인지 구분 못한다고 한다. 
`static <T> Juice makeJuice(FruitBox<T> box)`  
따라서 위처럼 T가 제너릭 타입임을 알리도록 `static`키워드 바로 뒤에 `<T>`를 추가하도록 하자. 그냥 와일드 카드 쓰면 편함!


또한 `FruitBox`안의 타입변수만 바꿔서 오버로딩도 하지 못한다.(타입변수는 오버로딩 성립조건X)  

이런 애매모호한 상황을 해결하기 위한게 **와일드 카드**이다.  

타입변수를 모르는 제너릭클래스를 매개변수로 넣고싶을 때 `?` 키워드를 사용하면 된다.  
```java
class Juicer {
	static Juice makeJuice(FruitBox<? extends Fruit> box)	{
	...
	}
}
```

```java
static int numElementsInCommon(Set<?> s1, Set<?> s2) { 
	...
}
```
`Set`안의 요소가 어떤게 올지 모를때, 그래도 `Set`객체를 매개변수로 쓰고싶을때 와일드카드를 쓰면 된다.  
매개변수 안의 타입변수가 어떤게 오던지 상관없다.  

와일드 카드도 제너릭처럼 제한을 걸 수 있다.  
`<? extends T>` - T의 하위클래스만 타입변수로 올 수 있다.  
`<? super T>` - T의 상위 클래스만 타입변수로 올 수 있다.  
 
그런데 왠만하면 와일드카드 쓰지 말고 제너릭과 멤버클래스로 선언하는게 안정성이 좋다고 한다.
static 멤버로 올 경우만 와일드 카드를 쓰도록 하자.  

---

## 제너릭 메서드

메서드 선언부, 반환자료형 앞에 제네릭 타입이 위치하는 메서드  

대표적인 제너릭 메서드로 `Collections`의 `sort`메서드가 있다.

`Collections`의 `sort`메서드 정의
```java
public static <T> void sort(List<T> list, Comparator<? super T> c)
```
이녀석도 `static`으로 선언된 정적 메소드이지만 와일드 카드를 쓰지 않고 제네릭을 통해 인자를 받는다.  

반환타입 앞의 `<T>`는 제네릭 메서드라는걸 명시하는 것이고 여기에 배웠던 제네릭 제한을 걸 수 도 있다.  

위에서 보았던 `Juicer`클래스의 와일드카드를 쓴 `makeJuice` 메서드를 제너릭으로 바꾸어 보자.
```java 
class Juicer {
	static Juice makeJuice(FruitBox<? extends Fruit> box)	{
	...
	}
}
```
```java
class Juicer {
	static <T extends Fruit> Juice makeJuice(FruitBox<T> box)	{
	...
	}
}
```
해석하면 해당 메서드는 제네릭 메서드이고 인자로 `FruitBox` 매개변수가 오고 `FruitBox`의 타입변수는 제네릭에 의해 달라질 수 있다.  
타입변수는 제너릭에 의해 `Fruit`를 상속하는 객체만 가능하다.  

`Collections`의 `sort`메서드  
```java
public static <T extends Comparable<? super T>> void sort(List<T> list)
```  

이제 `Collections`의 `sort`제너릭 메서드도 의미를 알수 있다.  

일단 `List`의 요소로 `Comparable`인터페이스를 상속하는 객체만 올수 있다.  
그냥 `Comparable`이 아니라 `T`를 정렬할수 있는 `Comparable`을 상속해야 한다.

```java
ArrayList<Integer> list = new ArrayList<>();
....
Collections.sort(list);
```
이런식으로 많이 사용했는데 `Integer`레퍼클래스는 `Comparable<Integer>` 을 구현하고 있기 때문에 사용 가능한것.

----

## Date & Calendar
	
`Date` - jdk 1.0 제공한 클래스, `java.util` 패키지 안에 있다.  

가장 구닥다리인 `Date`클래스... 호환성 때문에 클래스는 남아있지만 대부분의 메서드를 삭제될 예정...  
```java
Date now = new Date();
System.out.println(now);
System.out.println(now.getTime());
```
출력값
```java
Wed Jan 30 10:24:46 KST 2019
1548812137967
```
`Date`의 `toString()`이 아래처럼 오버라이딩 되어있다.  

`long`으로 반환된 저 숫자는 1970년 1월 1일 부터 계산한 초시간 이다.  


```java
Date when = new Date(2019-1900, 1-1, 29); //현재 30일
System.out.println(now.before(when));
System.out.println(now.after(when));
```
출력값
```
true
false
```

각종 날짜 정보 구하기 - `get...()` 메서드
```java
System.out.println(now.getDate()); 	//30
System.out.println(now.getDay()); 	//일요일=0 수요일=3
System.out.println(now.getMonth()+1); 	//0~11달
System.out.println(now.getYear()+1900); //1900 더해줘야함
System.out.println(now.getHours());
System.out.println(now.getMinutes());
```
출력값
```
30
3
1
2019
4
47
```

`Date`객체에 날짜 설정방법
```java
Date past = new Date(2010-1900, 5-1, 1); //2010 5 1 설정
now.setYear(2010-1900);
now.setMonth(5-1);
now.setDate(1);
```
생성자를 사용하거나 `set`메서드를 사용하면 된다.  
설정되지 않은 시, 분, 초는 현재시, 분, 초로 들어감...  

이렇게 자투리로 남는 시간값 때문에 오차가 생길 수 도 있다.  

날짜끼리 차이 계산할때에는 시간은 모두 `0`으로 세팅하거나 `clone`메서드를 사용해서 서로 같은 시간을 가리키는 식으로 오차를 없애야함.

`Date`객체 출력 형식
```java
System.out.println(now.toString());
System.out.println(now.toGMTString());
System.out.println(now.toLocaleString());
```
출력값
```
Mon May 31 16:50:40 KST 2010
31 May 2010 07:50:40 GMT
1.    5. 31 오후 4:50:40
```
`String.format`으로 출력
```java
String d = String.format("%d년 %d월 %d일 %d:%d:%d (%c)"
	         , 1900 + now.getYear()
	         , now.getMonth() + 1
	         , now.getDate()
	         , now.getHours()
	         , now.getMinutes()
	         , now.getSeconds()
	         , "일월화수목금토".charAt( now.getDay() )  // 3
	         );
	    System.out.println(d);
```
출력값
```
2010년 5월 31일 22:2:27 (월)
```

----

## Calendar
	
`Calendar`는 추상클래스로 `new`를 통해 객체생성 불가능하다.  

`Calendar.getInstance()` 정적 메서드로 인스턴스를 가져올 수 있음.  
```java
Calendar cal = Calendar.getInstance(); //현재날짜....생성
```

사실 `GregorianCalendar`클래스를 사용하면 더 쉽게 `Calendar`객체를 생성 가능하다.  
`GregorianCalendar`원형
```java
public class GregorianCalendar
extends Calendar
```

추상클래스인 `Calender`를 상속중이다.  
다음과 같이 생성자에 날짜, 시간을 전달 가능하다.  

```java
Calendar cal = new GregorianCalendar(2018,5-1,20);
```
설정하지 않은 시간, 초, 밀리초는 모두 `0`으로 초기화된다.  


### Calendar 날자 출력 - get 메서드
```java
System.out.println(cal.get(Calendar.YEAR));
System.out.println(cal.get(Calendar.MONTH)+1); //0~11
System.out.println(cal.get(Calendar.DATE));
/*일(1) 월(2) 화(3) 수(4) 목(5) 금(6) 토(7)*/
System.out.println( cal.get(Calendar.DAY_OF_WEEK) );
System.out.println("일월화수목금토".charAt(cal.get(Calendar.DAY_OF_WEEK)-1));
System.out.println(cal.get(Calendar.HOUR)); //12시 기준
System.out.println(cal.get(Calendar.HOUR_OF_DAY)); //24시기준
System.out.println(cal.get(Calendar.MINUTE));
System.out.println(cal.get(Calendar.SECOND));

```
출력값
```
2019
2
1
9
21
59
37
```


### Calendar 날자 설정 - set, add 메서드.
```java
cal.set(Calendar.YEAR, 2019); 	//년만 변경
cal.set(Calendar.MONTH, 1-1); 	//날짜만 변경
cal.set(Calendar.DATE, 1); 	//날짜만 변경
```
```java
cal.set(2018, 12-1, 21); 
// 한꺼번에 변경, 달은 0부터 시작.  
```
```java
cal.add(Calendar.DATE, 100); //100일 후 날짜
cal.add(Calendar.DATE, -100); //100일 전 날짜
```

### Data -> Clalendar 형변환 Calendar의 setTime 메서드
```java
Date d = new Date();
Calendar cal = Calendar.getInstance();
c.setTime(d);
```

### Clalendar -> Data 형변환 Calendar의 getTime, getTimeInMillis 메서드

```java
Calendar cal = Calendar.getInstance();
Date d = cal.getTime();
```

```java
Calendar cal = Calendar.getInstance();
Date d =  new Date( c.getTimeInMillis() );
```


### Clalendar 객체로 마지막 날짜 구하기 - getActualMaximum()메서드

예외상황이 있음.  
2월의 경우 28일까지 밖에 없음으로 `set`메서드로 달 설정시 28일을 넘지 않도록 주의.  
만약 넘을 경우 2월의 경우 31일 반환  

```java
Calendar cal = Calendar.getInstance();
cal.set(2018, 1-1, 1); 
for (int i = 0; i < 12; i++) {
	c.set(Calendar.MONTH, i);
	System.out.println(c.getActualMaximum(Calendar.DAY_OF_MONTH));
}
```

출력값
```
31
28
31
30
31
30
31
31
30
31
30
31
```

## 두 날짜사이의 차이 구하기
```java
Calendar now = Calendar.getInstance(); // 현재 시간
Calendar past = (Calendar) today.now();
openingDay.set(2018, 12-1, 21);
```
`getTimeInMillis()` 메서드를 통해 두 날짜 사이의 '밀리초'를 가질고 날짜 차이를 얻기 때문에 '시' 이하의 시간단위로 인해 미세한 오차가 생길수 있음으로 `clone`을 통해 '시'이하 단위를 일치화.  

`GregorianCalendar`으로 생성하면 시, 분, 초 모두 0으로 설정되기 때문에 `clone()`으로 복제할 필요가 없으니
`Calendar`로 차이를 구해야 한다면 `GregorianCalendar`로 생성하자.

```java
long gap = today.getTimeInMillis() - openingDay.getTimeInMillis();
System.out.println(gap +" ms" ); // 3455999985 ms
System.out.println((gap/1000) +" s" ); // 3455999985 ms
System.out.println((gap/1000/60) +" m" ); // 3455999985 ms
System.out.println((gap/1000/60/60) +" h" ); // 3455999985 ms
System.out.println((gap/1000/60/60/24) +" 일" ); // 3455999985 ms
```
출력값
```
43d
1032h
61920m
3715200s
3715200000ms
```

### Calendar객체를 사용한 달력그리기...


```java
...
int year = 2019;
int month = 3;
//달 1일 의 요일 필요, 마지막 일자 필요
printCalendar(year, month);
```

```java
private static void printCalendar(int year, int month) {
		int week = 0;
		int endDay = 0;
		
		
		System.out.printf("%d년 %d월\n", year, month);
		String weeks = "일월화수목금토";
		for (int i = 0; i < weeks.length(); i++) {
			System.out.printf("%c\t", weeks.charAt(i));
		}
		System.out.println();
		
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, 1);
		week = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, -week+1); //전달의 일요일 부터 출력.
		for (int i = 0; i < 42; i++) { 
			System.out.printf("%d\t", cal.get(Calendar.DATE));
			cal.add(Calendar.DATE, 1);
			if(i%7==6)
				System.out.println();
		}
	}
```


----

## DecimalFormat

정수,실수를 패턴이 적용된 문자열 변환하는데 사용하는 클래스

```java
int i = 12;
DecimalFormat df = new DecimalFormat("0000");
System.out.println( df.format(i) ); 
```
출력값
```
0012
```
`String.format` 메서드로도 가능.
```java
String result = String.format("%04d", i);
System.out.println(result); //0012 출력
```

생략....


----

## SimpleDateFormat

날짜를 다양하게 다루기 위해서 사용되는 클래스
Format클래스중 가장 많이 사용된다. 

다양한 형식을 날짜를 `Calendar`클래스로 저장하고
`Calendar`나 `Date` 클래스의 날짜를 다양한 형식으로 변환 출력한다.

```java
String pattern = "G"; // BC AD
String pattern = "y"; // 년도
String pattern = "M"; // 월
String pattern = "w"; // 주(월단위)
String pattern = "W"; // 주(년단위)
String pattern = "d"; // 일(월단위)
STring pattern = "D"; // 일(년단위)
STring pattern = "H"; // 시(24시)
STring pattern = "h"; // 시(12시)
String pattern = "E"; // 요일
```

### SimpleDateFormat의 format() 메서드로 Date객체를 변환한 String문자열로
```java
Date now = new Date();
String pattern = "yyyy년 MM월 dd일 HH'h' mm'm' ss's' (E)"; 
SimpleDateFormat sdf = new SimpleDateFormat(pattern);
String result =  sdf.format(now);
System.out.println( result );
```
출력값
```
2019년 02월 02일 03h 48m 21s (토)
```

만약 위 과정을 String의 format 메서드로 하려면 다음과 같이 긴 코드를 작성해야함
```java
Date now = new Date();
String result =
	String.format("%d년 %02d월 %02d일 %dh %dm %ds (%c)"
      , 1900 + now.getYear()
      , now.getMonth() + 1
      , now.getDate()
      , now.getHours()
      , now.getMinutes()
      , now.getSeconds()
      , "일월화수목금토".charAt(now.getDay())
      );
System.out.println(result);
```

`SimpleDateFormat`의 `format()` 메서드는 매개변수로 Date객체를 받기 때문에 Calendar를 사용한다면
Date로 변환해서 사용해야 한다.
```java
Calendar cal = new GregorianCalendar(2018,5-1,20);
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:sss E");
System.out.println(sdf.format(cal.getTime()));
```
출력값
```
2018-05-20 12:00:00:000 일
```

### SimpleDateFormat의 parse() 메서드로 String문자열을 Date객체로

날짜 객체를 문자열로 변환해서 출력도 가능하지만
문자열로 날짜 객체를 만들 수 있다.

```java
String strDate = "2019년 02월 02일";
DateFormat sdf2 = new SimpleDateFormat("yyyy년 M월 d일"); //부모클레스인 DateFormat에 UpCasting
Date date = sdf2.parse(strDate);
System.out.println(date.toLocaleString());
System.out.println(sdf2.format(date));
```
출력값
```
1.    2. 2 오전 12:00:00
2018년 2월 2일
```
만약 문자열이 잘못된 포멧이면 `ParseException` 예외를 발생한다.
`SimpleDateFormat`의 `parse` 메서드로 생성되는 날짜 객체 역시 설정되지 않은 시, 분, 초는 0으로 세팅된다.

```java
String pattern = "y/M/d";
Scanner sc = new Scanner(System.in);
SimpleDateFormat sdf = new SimpleDateFormat(pattern);
Date inDate = null;
System.out.print("날짜입력 y/M/d: ");

while (true) {
	try {
		inDate = sdf.parse(sc.nextLine());
		break;
	} catch (ParseException e) {
		System.out.print("잘못된 날짜 포멧... 다시입력: (y/M/d): ");
	}
}	
```
출력값
```
날짜입력 y/M/d: 2019.01.1
잘못된 날짜 포멧... 다시입력: (y/M/d): 2019/1/01
1.    1. 1 오전 12:00:00
```
```
날짜입력 y/M/d: 2019/13/41
1.    2. 10 오전 12:00:00
```
웬만한 날짜형식은 다 알아 먹는다. `SimpleDateFormat`짱!

----

## MessageFormat

데이터를 정해진 양식에 맞춰 출력하는데 도움을 주는 클래스.
다른 `type`이 들어가있는 배열
```java
Object[] arguments = {"홍길동", 176.565, 27, 'A'};
String msgformat = "이름:{0}\n키:{1}\n나이:{2}\n혈액형:{3}\n";
String msg = MessageFormat.format(msgformat, arguments);
System.out.println(msg);
```
출력값
```
이름:홍길동
키:176.565
나이:27
혈액형:A
```
`%`(데이터형) 따로 형식을 붙여줄 필요없이 `MessageFormat`을 사용하면 몇번째 인자인지만 알려주면 된다.
출력할 변수 타입이 뭔지 모를때 사용하면 편할듯 함.

