---
title:  "java getEnv(환경변수), Reflection, Collection, ArrayList!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

## 환경변수 읽기 - getEnv()

환경변수 : OS에서 Name과 value로 관리되는 문자열

```java
String javaHome = System.getenv("JAVA_HOME");
System.out.println(javaHome);
```

출력
```
//C:\Program Files\Java\jdk1.8.0_192
```


---


모든 환경변수 출력, 처음쓰는 컬렉션!
```java
Map<String,String> map = System.getenv();

Set<String> s = map.keySet();
//Set<K> keySet()
//Returns a Set view of the keys contained in this map, 
//Set이라는 Key와 Value가 들어간 자료형 반환

Iterator<String> ir = s.iterator();  //Iterator : 반복자
while(ir.hasNext())
{
	String envName = ir.next();
	System.out.printf("%s: %s\n", envName, System.getenv(envName));
}
```

출력
```
C:\Program Files\Java\jdk1.8.0_192
USERDOMAIN_ROAMINGPROFILE: KGY19
LOCALAPPDATA: C:\Users\kgy19\AppData\Local
PROCESSOR_LEVEL: 6
USERDOMAIN: KGY19
FPS_BROWSER_APP_PROFILE_STRING: Internet Explorer
LOGONSERVER: \\KGY19
....
```

-----

## 리플렉션

리플렉션이란 `Class`클래스를 정보를 얻어와 분석, 사용하는 것.

먼저 `Class`클래스를 사용해서 클래스 정보를 얻어오는걸 알아보자.  

### Class 객체정보 얻는 방법1 - `Object`의 `getClass()`
```java
Class carInfo = myCar.getClass();
System.out.println(carInfo.getName());
System.out.println(carInfo.getPackage().getName());
```
출력
```
days22.Car
days22
```
`carInfo.getPackage()`를 통해 해당 클래스가 속한 패키지정보를 얻을 수 있음.  

방법1은 `Car`객체를 인스턴스화 해야 `Object`의 `getClass()`메서드를 통해 클래스 정보를 얻을 수 있다.  


### Class 객체정보 얻는 방법2 - `Class.forName`
```java
String className = "days22.Car";
Class carInfo2 = Class.forName(className);
```
클래스 full Name(문자열)으로 클래스 정보를 얻을 수 있다.  

이렇게 얻는 클래스 정보를 가지고 객체를 인스턴스화 할 수 있다.  
이렇게 Class클래스로 정보를 얻어 인스턴스 생성하는것을 **동적 객체생성**이라 한다.  
```java
Car myCar2 = (Car)carInfo2.newInstance();
```

`Class`의 `newInstance()`메서드
```java
public T newInstance() throws InstantiationException, IllegalAccessException
```
T라는 제너릭 클래스를 반환하는데 `DownCasting`해서 객체에 참조시킨다.  

동적으로 객체생성은 `java jdbc db`연동 객체를 만들때 많이 사용한다.
메모리에 올라갈 `jdbc`드라이버를 `Class.forName`으로 생성한다.  


---

### 1. 객체의 생성자 정보 얻어내기 - `Class`의 `getConstructors()` 메서드 사용

`getConstructors()` 원형
```java
public Constructor<?>[] getDeclaredConstructors() throws SecurityException
```

일단 `getConstructors()` 메서드를 호출하면 **생성자 정보들이 든 객체배열** `Constructor[]`를 반환한다.  
```java
Constructor[] cs = carInfo.getDeclaredConstructors();
```

`Constructor` 클래스 원형  
```java
public final class Constructor<T> extends Executable
```
`Executable` 추상클래스에서 `get...` 시작하는 메서드들을 구현하고 있음.  
```java
for (Constructor c : cs) {
	System.out.print(Modifier.toString(c.getModifiers()));
	System.out.print(" " + carInfo.getName() + "(");
	Class[] ps = c.getParameterTypes();
	for (int i = 0; i < ps.length; i++) {
		System.out.print(ps[i].getName() + ", ");
	}
	System.out.println(")");
	
}
```
출력값
```
public days22.Car()
public days22.Car(java.lang.String, java.lang.String, java.lang.String, int, )
```
`Constructor`객체의 `getModifiers` - 생성자의 접근제어자, 기타제어자 정보를 *정수*형태로 반환  
`Constructor`객체의 `getParameterTypes` - 생성자 매개변수 타입을 `Class`객체 배열로 반환  


### 2. 객체의 필드정보 얻어내기 - `Class`의 `getDeclaredFields()`메서드 사용

`getDeclaredFields()` 메서드 원형  
```java
public Field[] getDeclaredFields() throws SecurityException
```

일단 `Class`객체의 `getDeclaredFields()` 메서드를 호출하면 필드정보가 든 `Field`배열 반환    
```java
Field[] fs = carInfo.getDeclaredFields();
```

`Field` 클래스 원형
```java
public final class Field extends AccessibleObject implements Member
```
`Member`인터페이스에 `get...` 추상 메서드가 선언되있고  
`Field`에서 이를 오버라이딩해서 사용중.  

```java
for (Field f : fs) {
	System.out.print(Modifier.toString(f.getModifiers()));
	System.out.println(" " + f.getType().getName() + " " + f.getName());
}
```
`Constructor`클래스의 `getModifiers()`메서드 처럼 `Field`의 `getModifiers()`메서드도 접근제어자 기타제어자 정보를 정수로 반환.  

`Field`의 `getType()`메서르도 멤버필드 `typde`명을, `getName`으로 멤버필드 이름을 반환.   

출력값
```
public staticjava.lang.String name
public staticjava.lang.String gearType
public staticjava.lang.String color
int speed
```


### 3. 객체의 메서드 정보 얻어내기 - `Class`의 `getDeclaredMethods()`메서드 사용

`getDeclaredMethods` 원형  
```java
public Method[] getDeclaredMethods() throws SecurityException
```

일단 `Class`의 `getDeclaredMethods()`메서드 를 호출해서 메서드 정보가든 `Method`배열을 반환  
```java
Method[] ms = carInfo.getDeclaredMethods();
```


`Method`클래스 원형
```
public final class Method extends Executable
```

`Constructor` 클래스랑 비슷한 형식  
`public final class Constructor<T> extends Executable`

```java
for (Method m : ms) {
	Class[] ps = m.getParameterTypes(); //매개변수 배열로 저장
	System.out.print(Modifier.toString(m.getModifiers()));
	System.out.print(" " + m.getReturnType() + " " + m.getName() + "("); //반환형 + 이름
	for (int i = 0; i < ps.length; i++) {
		System.out.print(ps[i].getName() + ","); //매개변수 출력
	}
	System.out.println(")");
}
```

`Method`의 `getParameterTpyes()`메서드로 매개변수 `type`이든 `Class`배열을 반환.   

출력값
```
public void speedDown(int,java.lang.String,)
public void speedUp(int,java.lang.String,)
public void Stop(boolean,)
```

이정보들로 뭘 할 수 있을진 모르겠지만 `Class`객체로 받은 정보로 분석하는것이 **리플렉션**이라는걸 알아두자.  

---

## 컬렉션 - Collection

### Collection
수집품이란 뜻, 컴퓨터에서 `Collection`은 데이터의 집합 이란 뜻.  


### Framework

어플, 솔루션 개발시 구체적 기능에 해당하는 부분 설계 구현을 재사용 하도록 
협업화된 형태로 제공하는 소프트웨어 환경을 뜻함.  

요약하면 프레임웍은 소프트웨어 설계, 구현을 팀작업할 수 있도록 구현된 작업 환경, 표준화된 작업환경!  
경력자나 초보자나 프레임워크 안에서 개발하게 되면 만들어지는 `output`이 비슷하다.  



데이터 집합을 처리하기위해 배열을 많이 사용했는데 단점이 많기때문에
데이터 집합을 다루기 위한 표준화된 설계를 `Collection FrameWork` 이라 한다.  

배열에 계속 데이터 집어넣는다 생각해보자.  
배열 크기 부족한지 체크하고, 부족하다면 새배열 다시 만들어서 원래 배열 집어넣고 거기다 데이터집어넣고... 삽입하려고 해도 오른쪽으로 쉬프트하고... 너무 힘들다....  

이런 작업들을 개발자마다 다 다르게 코딩하고있었는데 `Collection FrameWork`을 사용하면 위 작업을 표준화 시킬 수 있다.  

이런 표준화된 기능을 구현한 클래스를 `Collection Class` 라고 한다.

`Collection FrameWork` : 초보 개발자나 고급 개발자나 표준화된 개발환경 안에서 표준화된 개발을 하도록 구성된 환경 대표적으로 Spring이 있다...  
`Collection Class`: 데이터를 다루기 위한 기준이 표준화된 클래스  

예를들어 로또 배열 중복체크하려고 함수를 만들었는데 개발자마다 중복체크 함수 구현 방법이 다 다르다.  

하지만 `Set`이라는 `Collection Class`를 사용하면 애초에 중복허용을 하지 않기 때문에 중복체크할 필요가 없다!  

모든 개발자는 그저 `Set`에 6개의 숫자가 채워질때까지 반복하기만 하면 된다. &larr; 표준화!  

이제 `Collection Class의` 특징만 알면 바로 쉽게, 표준화된 코딩을 할 수 있다!  

------

## 컬렉션 의 핵심 인터페이스

`Collection`(인터페이스) &larr; `List`(인터페이스) &larr; `ArrayList`(클래스)  

`Collection`(인터페이스) &larr; `Set`(인터페이스) &larr; `HashSet`(클래스)  

`Map`(인터페이스) &larr; `HashMap`(클래스)  

**`List`**의 특징은 순서가 있고 중복을 허용 한다.  

**`Set`**의 특징은 순서가 없고 중복을 허용하지 않는다 (순서 없고 중복허용하는 `Collection Class`도 있다)  

`Collection`은 최상위 인터페이스이기 때문에 `Collection` 인터페이스에 있는 메서드들은 
모든 `Collection Class`가 구현중이다. `add()`, `remove()` 등등..    


> 그림 참고: https://hackersstudy.tistory.com/26

---

## ArrayList

가장 많이 사용되는 컬렉션 클레스, `Vector Collection Class`를 개선한 클래스이다.  

차이점은 `Vector`는 동기화처리가 가능.  
`ArrayList`는 동기화 처리가 안된다.   

동기화처리 하려면 `ArrayList`에서 동기화 기능 구현하거나 `Vector` 를 사용하자.  

`ArrayList`도 내부적으로는 배열을 만들고 그 안에 `Object`를 집어넣는 구조이다.  

배열이 꽉차면 더큰 배열을 생성하고 기존 내용을 큰 배열로 이동... 
(사용은 편하지만 효율은 그닥...)  

`ArrayList` 원형
```java
public class ArrayList<E> extends AbstractList<E>
	implements List<E>, RandomAccess, Cloneable, Serializable {
	transient Object[] elementData;
```

`AbstractList`는 `List`인터페이스의 뼈대구현용 추상 클래스이다.  

`<E>`는 `Element`(요소)의 약자, 집어넣을 변수`type`을 지정한다.    
이렇게 요소를 집어넣는 클래스를 `Generic`클래스라 한다.   

보통 컬렉션 클래스는 제너릭 클래스로 구현하기 때문에
컬렉션 클래스를 제너릭 클래스라고도 한다.  

요소에는 **참조타입**만 올 수 있기때문에 기본형은 넣을 수 없다.  

implements(구현)부분을 보면 3개의 인터페이스를 다중 상속중이다.  

`RandomAccess`인터페이스를 구현함으로 랜덤 접근 가능.  
`Cloneable`인터페이스를 구현함으로 복제가능.  
`Serializable`인터페이스를 구현함으로 전송(네트워크, 파일) 할때 통째로 전송(직렬화)이 가능하다.  


### ArrayList의 인스턴스화
```java
ArrayList<Integer> list = new ArrayList<>();
list.add(1); 
list.add(2);
```
뒤의 `<Generic Type>`은 생략 가능하다.   

> 참고: Constructs an empty list with an initial capacity of ten. 기본생성자로 생성시 초기 용량(capacity)은 10이다.  


`Integer`래퍼 클래스를 요소로 받기 때문에 1이 `Integer`로 `auto Boxing`되서 넣어진다.  


```java
int num = list.get(0);
System.out.println(num);
```
이것도 Integer클래스가 int형으로 auto UnBoxing되서 num을 초기화한다.  

제너릭을 사용하니까 Integer로 반환되는거지 따로 지정안하면 Object로 반환된다. Object로 반환되면 우리가 직접 DownCasing해야하는데 귀찮다!  
즉 제너릭을 사용하는 이유는 우리가 지정한 값으로 반환하라! 고 명시하는것.  
형변환 하기 싫으니까!  

배열처럼 처음 값은 0부터 시작한다.

출력값
```
1
```
---

### ArrayList의 길이(요소의 수)를 알고 싶다면
```
System.out.println(list.size());
```

---

### ArrayList모든 요소 출력 - toString();
```
for (int i = 0; i < list.size(); i++) {
	System.out.println(list.get(i));
}
System.out.println(list.toString());
System.out.println(list);
```
toString()메서드를 생략해도 되는 이유는   
println이 매개변수를 Object로 받았을때
Object클래스의 toString() 메서드를 호출하도록 println이 오버로딩 되어있기때문.  
Object의 toString()을 호출해도 list방식으로 toString()이 오버라이딩 되어있기 때문에 요소전체가 출력된다.  

---

### ArrayList - add(), set() 메서드
```
ArrayList<Integer> list = new ArrayList<>();
list.add(1); 
list.add(2);
list.add(1, 100);
list.set(1, 10);
System.out.println(list.toString());
```
출력값
```
[1, 10, 2]
```
`public void add(int index, E element)` list의 오버로딩된 add() 메서드  
index위치에 E(요소)를 추가한다.  
`public void add(int index, E element)` list의 set() 메서드  
index위치의 E(요소)를 입력한 E(요소)로 변경.  

---

### ArrayList index값 찾기 - indexOf() 메서드


ArrayList생성후 add(Element) 호출하여 랜덤값 초기화
```
ArrayList<Integer> list = new ArrayList<>();
System.out.println(list.size());
Random rnd = new Random();
for (int i = 0; i < 10; i++) {
	list.add(rnd.nextInt(100) + 1);
}
```

```
System.out.println(list);
System.out.print("찾을 정수 입력: ");
Scanner sc = new Scanner(System.in);
int num = sc.nextInt();
int idx = list.indexOf(num);
System.out.println(idx);
```

출력값
```
[59, 47, 80, 66, 60, 47, 88, 57, 8, 34]
찾을 정수 입력: 66
3
```
indexOf(Object)를 통해 찾을 참조변수의 index를 반환.

---

### ArrayList 요소 삭제 - remove() 메서드

ArrayList의 remove함수는 2가지

1. remove(int index)
```
E	remove(int index)
Removes the element at the specified position in this list.
```
해당 index의 요소를 삭제하고 삭제한 요소를 반환!

2. remove(Object o)
```
boolean	remove(Object o)
Removes the first occurrence of the specified element from this list, if it is present.
```
Object와 일치하는 요소를 삭제! 성공 실패 여부를 boolean타입으로 반환

예제
```
Integer [] m = {2,4,3,5,1};		
ArrayList<Integer> list = new ArrayList<>();
list.addAll(Arrays.asList(m));
list.remove(2); //index 2번 삭제
list.remove(new Integer(5)); //요소 5를 찾아서 삭제(Object로 전달했기떄문)
System.out.println(list);
```
출력값
```
[2, 4, 1]
```

---

## ArrayList->배열, 배열->ArrayList

배열->ArrayList 방법1 - Arrays의 asList()메서드 사용하기
```
Integer [] m = {2,4,3,5,1};		
List<Integer> list1 =Arrays.asList(m);
ArrayList<Integer> list2 = new ArrayList<>();
list2.addAll(list1);
```

배열->ArrayList 방법2 - Collections의 addAll()메서드 사용하기.
```
Integer [] m = {2,4,3,5,1};
ArrayList<Integer> list = new ArrayList<>();
Collections.addAll(list	,m);
```

중요한건 배열이 모두 Integer배열로 선언되었다는 것.
이는 List인터페이스를 구현하는 모든 Collection Class가 참조타입만 요소로 받을 수 있기 때문.  

int배열을 Integer배열로 변환하는법  
```
int a[] = {1,2,3,4};
Integer b[] = Arrays.stream(a).boxed().toArray(Integer[]::new);
```
너무 어렵다....
그냥 for문 돌려서 바꾸는 걸로....


ArrayList->배열 방법 - ArrayList의 toArray()메서드 사용하기.
```
Integer [] arr = list.toArray(arr);
System.out.println(Arrays.toString(arr));
```

---

## ArrayList 요소에 Collection Class를 넣는 경우

```
ArrayList<ArrayList<String>> class5 = new ArrayList<>();
...
while( (line = br.readLine()) != null )  {
   String[] team = line.split("-")[1].trim().split("\t");
   Collections.addAll(temp = new ArrayList<>(), team);
   class5.add(temp);
}
...
```
`Collections.addAll(temp = new ArrayList<>(), team);`
addAll에 temp라는 컬랙션 클래스 객체에 인스턴스생성후 team 문자열 배열 삽입  
`class5.add(temp);`
그리고 class5 컬렉션 객체에 temp 삽입.  

2차원 배열처럼 사용할 수 있다.  

----

## Collections 클래스

배열의 보조(도와주는) 클래스 Arrays.  
Object의 보조 클래스 Objects.  
Collection의 보조 클래스 Collections!  

즉 Collections클래스도 내부의 모든 함수가 static으로 선언되어 있다.
Collections에는 컬렉션 클래스에 도움될만한 각종 함수들이 정의되어 있다.  

```
ArrayList<Integer> list1 = new ArrayList<>();
Random rnd = new Random();
for (int i = 0; i < 10; i++)
	list1.add(rnd.nextInt(100) + 1);
System.out.println(list1);
```
출력값
```
[90, 52, 97, 26, 84, 76, 77, 10, 80, 45]
```
위에서 보았던 ArrayList에 랜덤한값 10개 삽입


### Collections의 sort() 메서드

이제 이녀석을 Collections의 sort() 메소드를 사용해서 정렬해보자.
```
Collections.sort(list1);
System.out.println(list1);
```
출력값
```
[10, 26, 45, 52, 76, 77, 80, 84, 90, 97]
```

Collections의 sort() 메소드 원형
```
public static <T extends Comparable<? super T>> void sort(List<T> list)
```

```
public static <T> void sort(List<T> list,
                            Comparator<? super T> c)
```

뭔가 접근제어자, 기타제어자, 반환형 말고 이상한게 껴있는데...<T 뭐...>  
어쨋든 sort메서드는 2가지로 오버로딩 되어있다. Comparator가 필요한 것과 필요없는 것.  

`Collections.sort(list1);` 가능한 이유는 Integer나 String같은 녀석은 이미 Comparable라는 인터페이스를 구현중이기 때문에 매개변수로 list만 넘겨도 괜찮다.   

### 거꾸로 출력 - Collections의 reverse()메서드

descending(내림차순)하고 싶다면 정렬하고 뒤집으면 된다.
```
Collections.reverse(list1);
System.out.println(list1);
```

----------------

## Vector - Collection Class

ArrayList와 거의 동일한 역할을 하는 컬렉션 클래스, 동기화 처리가 되어있기 때문에 멀티 스레드 환경에서 편하게 사용 가능하다.  

Vector 원형
```
public class Vector<E>
extends AbstractList<E>
implements List<E>, RandomAccess, Cloneable, Serializable
```
ArrayList랑 완벽히 동일.

Vector엔 있지만 ArrayList엔 없는기능이 몇가지 있다.

1. 용량(capacity), 늘어날 크기 지정하기
```
Vector<String> vt = new Vector<>(3,5); 
System.out.println(vt.capacity()); //3출력
```
용량(capacity)를 3으로 잡고, 다차면 5씩 늘어남.  
할당된 용량도 출력할 수 있음. 늘어나면 늘어난 용량 출력
둘다 ArrayList에는 없는 기능이다.  


2. 각종 Element 메서드 (동기화 메서드)
```
vt.addElement("aaa"); 
vt.add("bbb");
vt.add("ccc");
vt.add("ddd");
vt.add("fff");
System.out.println(vt.size());
System.out.println(vt.capacity());
```
출력값
```
5
8
```
객체뒤에 . 찍어보면 add, set, remove에 Element가 붙는 경우가 있는데 모두 동기화 처리된 메서드라 보면 된다.


```
vt.trimToSize();
System.out.println(vt.size());
System.out.println(vt.capacity());
```
출력값
```
5
5
```

trimToSize()를 통해 size만큼 용량을 줄인다(딱맞게)  

Vector의 경우 동기화 처리때문에 ArrayList보다 성능이 느리다.  
