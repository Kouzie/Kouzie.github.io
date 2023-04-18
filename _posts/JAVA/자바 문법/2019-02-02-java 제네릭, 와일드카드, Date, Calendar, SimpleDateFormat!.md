---
title:  "java 제네릭, 와일드카드!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
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
