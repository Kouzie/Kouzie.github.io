---
title:  "java 8 - 람다, 스트림, Optional!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

## Java 8 - 개요

`Java5`에서 제너릭이어 자바 역사상 가장 큰 변화가 일어난 업데이트, 
이 포스트에선 자바 8 에서 추가된 새로운 문법들을 소개한다.  

간단히 어떤 변화가 일어났는지 알아보자.  

```java
List<Apple> inventory = Arrays.asList(
  new Apple(80,"green"),
  new Apple(155, "green"),
  new Apple(120, "red"));
```

`Apple` 객체의 색, 무게를 필터링 하려면 함수를 작성해야 했다.

```java
public static List<Apple> filterGreenApples(List<Apple> inventory){
  List<Apple> result = new ArrayList<>();
  for (Apple apple: inventory){
    if ("green".equals(apple.getColor())) {
      result.add(apple);
    }
  }
  return result;
}

public static List<Apple> filterHeavyApples(List<Apple> inventory){
  List<Apple> result = new ArrayList<>();
  for (Apple apple: inventory){
    if (apple.getWeight() > 150) {
      result.add(apple);
    }
  }
  return result;
}
```

겨우 필터링 몇개 하는 거지만 코드가 20줄이 넘어간다.  
필터링 조건이 추가될 수록 함수는 많아지고 코드또한 길어진다.  

`Predicate` 를 사용해 코드 자체를 인자로 전달할 수 있다.  

```java
public interface Predicate<T> {
  boolean test(T t);
}
```

인자값을 받아 참, 거짓 을 반환한는 간단한 함수하나 정의된 인터페이스  

```java
public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p){
  List<Apple> result = new ArrayList<>();
  for(Apple apple : inventory){
    if(p.test(apple)){
      result.add(apple);
    }
  }
  return result;
}
```

이러한 방식을 동작 파라미터 방식이라 하고 전략 디자인 패턴을 사용한 코드이다.  

`Predicate` 가 필터링할 파라미터를 동적으로 전달해 필터링 할 수 있도록 하는 방식이다,  
이제 필터링 조건만 추가정의하면 된다.  

```java
// 동작 파라미터들
public static boolean isGreenApple(Apple apple) {return "green".equals(apple.getColor());}
public static boolean isHeavyApple(Apple apple) {return apple.getWeight() > 150;}
......

List<Apple> greenApples = filterApples(inventory, FilteringApples::isGreenApple);
```
`isGreenApple` 메서드를 `test` 메서드의 구현메서드로 적용해 `Predicate` 의 구현 클래스 인스턴스가 생성되고 매개변수로 전달된다.  
말 그대로 코드 자체를 인자로 전달하는 방식이다.  

여기서 람다를 사용하면 좀더 간략화된다.  

```java
List<Apple> greenApples = filterApples(inventory, (Apple a) -> "green".equals(a.getColor())));
```

한번만 사용하는 메서드라면 굳이 정의할 필요 없이 람다로 적용해버리자.  
어쩌면 위의 20줄 짜리 코드가 한줄로 끝날 수 도 있다.  

멀티코어 CPU가 보급화되면서 위의 개념에서 병령개념이 추가된다.  
스트림이다!  

스트림 개념은 유닉스 명령어에서도 사용된다.   

`cat file1 file2 | tr "[A-Z] "[a-z]" | sort | tail -3`  

> `tr`은 translate 의 약어로서, 지정한 문자를 바꾸어주거나 삭제하는 명령  
대문자 `A-Z` 를 소문자 `a-z` 로 치환

`sort` 명령은 병렬형식으로 출력받은 `file1`, `file2` 문자열 데이터를 입력받고 출력한다.  
`cat`, `tr` 명령이 완료되기 전에 `sort` 명령은 데이터를 전달받고 일하기 시작한다.  

자동차 수리공장에 수십대의 자동차가 들어와도 각각의 엔지니어가 맡은 일을 처리하고 다음 부서로 넘겨주면서 수십대의 자동차를 동시에 수리하듯이 

위와 같은 작업을 구성하려면 여러개의 스레드와 콜백 함수를 정의해야 하지만 자바의 추상 클레스를 통해 쉽게 병렬 구조를 구현할 수 있다.

```java
List<Apple> greenApples = inventory
  .stream()
  .filter((Apple a) -> a.getColor().equals("green"))
  .collect(toList());
List<Apple> greenApples = inventory
  .parallelStream()
  .filter((Apple a) -> a.getColor().equals("green"))
  .collect(toList());
```

## 동작 파라미터

사과의 색, 무게를 모두 필터링 하려면 어떤 메서드를 정의해야 하는지, Java8 에선 어떤식으로 처리할 수 있는지 알아보자.  

Java8 이전에 아래와 같은 코드로 필터링 할 수 있다.  

```java
public static List<Apple> filterApples(List<Apple> inventory, String color, int weight, boolean flag){
  List<Apple> result = new ArrayList<>();
  for(Apple apple : inventory){
    if ((flag && apple.getColor().equals(color)) || (!flag && apple.getWeight() > weight))
      result.add(apple);
  }
  return result;
}      
```
`flag` 가 `true` 일땐 `color`, `flag` 가 `false` 일땐 `weight` 를 기준으로 필터링한다.  

```java
List<Apple> greenApples = filterApples(inventory, "green", 0, true);
List<Apple> heavyApples = filterApples(inventory, "", 150, false);
```

함수내부를 뜯어보지 않는한 절대로 알수 없는 코드이다.  

Java8 의 동적파라미터를 사용하면 간결하고 유연성있는 코드 작성이 가능하다.  

```java
public static List<Apple> filterApples(List<Apple> inventory, ApplePredicate p){
  List<Apple> result = new ArrayList<>();
  for(Apple apple : inventory){
    if(p.test(apple)){
      result.add(apple);
    }
  }
  return result;
}
```
`Predicate<Apple> p` 가 동작 파라미터 역할을 하면 어떤 비교를 할건지 내부에 정의한다.  

동작 파라미터로 사용될 수 있는 클래스 정의  

```java
public interface ApplePredicate {
  boolean test(Apple apple);
}
public static class AppleWeightPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return apple.getWeight() > 150;
  }
}
public static class AppleGreenColorPredicate implements ApplePredicate {
  public boolean test(Apple apple) {
    return apple.getColor().equals("green");
  }
}
...
...
List<Apple> greenApples = filterApples(inventory, new AppleGreenColorPredicate());
```

이제 새로운 필터링 조건이 추가될때 마다 `ApplePredicate` 를 적절히 구현한 클래스만 정의하면 된다.  

### 클래스, 익명클래스, 람다

동작 파라미터 타입으로 인터페이스가 들어가다 보니 이를 구현하는 인스턴스로 **클래스, 익명클래스, 람다** 를 사용 가능하다.  

```java
List<Apple> greenApples = filterApples(inventory, new AppleGreenColorPredicate());

List<Apple> greenApples = filterApples(inventory, new ApplePredicate() {
  public boolean test(Apple a) {
    return a.getColor().equals("green");
  }
});

List<Apple> greenApples = filterApples(inventory, (Apple a) -> a.getColors().equals("green"));
```

### 추상화 with 동작 파라미터  

프로그램이 커질수록 사용 객체도 많아지며 필터링할 객체가 `Apple` 하나뿐이라는 건 장담할 수 없다.  

```java
public static <T> List<T> filter(List<T> list, Predicate<T> p) {
  List<T> result = new ArrayList<>();
  for(T elem : list) {
    if(p.test(elem))
      result.add(elem)
  }
  return result;
}
```

List 객체를 제너릭을 사용해 추상화 함으로  
이제 `Apple` 뿐 아니라 `Banana`, `Grape` 와 같은 객체들도 필터링할 수 있게 되었다.  

제너릭과 동작 파라미터를 사용하면 유연하고 가독성 좋은 코드를 쉽게 작성할 수 있다.  


## 람다

동작 파라미터에 메서드를 전달할때 익명 함수 형식으로 좀더 간편하게 전달할 수 있도록 하는 문법  


```java
int method() {
  rturn (int) (Math.random() * 5) + 1;
}
```

`() -> (int)(Math.random() * 5) + 1`


매서드명을 생략하고 `->`기호가 추가되며 연산된 값은 당연히 반환값이다.  

특이한건 매개변수 타입, 반환값 타입이 모두 생략되는데 추론을 통해 타입이 결정된다.  

```java
public int bigger(int a, int b) {
  return a > b ? a : b
}
```

위의 메서드를 람다식으로 변경해보자.  

```java
(int a, int b) -> a > b ? a : b
(a, b) -> { return a > b ? a : b; }
(a, b) -> a > b ? a : b
```

위 3개 람다식은 모두 동작하며 같은 기능을 수행한다.  

매개변수 타입 생략이 가능하며  
람다식이 한줄로 끝날경우 중괄호와 세미콜론 생략이 가능한데 이를 **표현식**(expression) 이라 한다.  

반대로 세미콜론과 같이 사용하는 **구문**(statement) 를 사용하려면 중괄호와 리턴문이 반드시 사용되어야 한다.  

> 주의사항: 람다식에 `return`을 사용한다면 중괄호 생략은 불가능하다. 중괄호를 사용했다면 뒤에 세미콜론이 필요하다.   

만약 매개변수를 단 하나만 타입을 추론해서 사용한다면 소괄호를 생략가능하다.  

```
(a) -> a * a
a -> a * a
```

### 함수형 인터페이스, 익명 클래스, 람다  

함수형 인터페이스란 `Predicate<T>` 와 같은 오직 하나의 추상메서드만 정의된 인터페이스이다.  

`Predicate<T>`와 더불어 `Comparator<T>`, `Runnable<T>` 같은 인터페이스들을 함수형 인터페이스라 할 수 있다.  

함수형 인터페이스는 `@FunctionalInterface`어노테이션으로 명시적으로 표기한다. 추상 메서드가 2개 이상 정의되면 에러를 반환한다.  

```java
@FunctionalInterface
interface MyFunction {
    void myMethod();
}
```

람다는 이런 함수형 인터페이스를 **매개변수로 사용하는** 모든 메서드에서 사용할 수 있다.  

사실 람다식도 자바의 원칙인 모든 메서드는 클래스 내부에 정의되어야 하는 법칙에서 벗어날 수 없다.  

함수형 인터페이스의 람다식을 생성하면 해당 인터페이스를 구현한 클래스가 정의되고 해당 인터페이스를 구현한 익명객체가 생성되어 익명객체의 메서드가 호출된다.  

람다식을 사용 안한다면 아래 처럼 익명 객체를 `new` 연산자로 생성해야 한다.  

```java
@FunctionalInterface
interface MyFunction {
    void myMethod();
}

public class App 
{
    public static void aMethod(MyFunction f) {
        f.myMethod();
    }

    public static void main( String[] args )
    {
        MyFunction f = new MyFunction(){
            public void myMethod() {
                 System.out.println("Hello ramda");
            }
        };
        aMethod(f);
    }
}
```

`MyFunction`를 구현한 객체를 생성하여 `f`에 대입, `f`객체를 `aMethod()`에 매개변수로 전달한다.  

`aMethod` 는 전달받은 `f` 객체의 `myMethod` 를 호출하기에 익명객체로 구현한 `println` 이 실행된다.

람다를 사용하면 아래처럼 가능.  

```java
public static void main( String[] args )
{
    aMethod(() -> System.out.println("Hello ramda"));
}
```

파라미터도, 반환값도 없지만 람다가 알아서 추론해 `MyFunction` 구현객체로 반환한다.  

이는 `Collection.sort`메서드 같이 요구하는 인터페이스 구현 클래스를 위해 일일이 `@Override... 함수명(){...}`형식을 지켜 익명객체를 생성할 필요 없이 람다식 한줄로 끝낼 수 있단 뜻이다.   


기존에 `Collection.sort`를 사용해 리스트를 정렬하려면 `Comparator<T>` 함수형 인터페이스를 구현한 클래스를 `sort`의 매개변수로 전달해야 했다.  

```java
List<String> list = Arrays.asList("aaa", "abc", "bbb", "bbd", "ddd", "adb");
Collection.sort(list, new Comparator<String>() {
  public int compare(String s1, String s2) {
    return s2.compateTo(s1);
  }
});
```

람다식이 익명객체를 대체할 수 있기 때문에 아래와 같이 간결하게 변경 가능하다.

```java
List<String> list = Arrays.asList("aaa", "abc", "bbb", "bbd", "ddd", "adb");
Collection.sort(list, (s1, s2) -> s2.compateTo(s1));
```

> java 내부를 살펴보면 `Comparator<T>` 역시 함수형 인터페이스를 뜻하는 어노테이션을 위에 달고 있다.  
```java
@FunctionalInterface
public interface Comparator<T>
```

### 람다식의 타입, 형변환  

어쩃건 `MyFunction` 함수형 인터페이스를 구현한 익명객체를 람다식으로 만들어 변수에 매핑시키는 과정이기에 내부적으로 아래처럼 형변환 과정이 일어난다.(업 캐스팅이기에 생략가능하다)  

```java
public static void main( String[] args )
{
    MyFunction f = (MyFunction)() -> {System.out.println("Hello ramada");};
    aMethod(f);
}
```

그렇다면 모든 객체의 상위 객체인 `Object`에 함수형 인터페이스를 매칭할 수 있을까?  
Object 에 함수형 인터페이스를 구현한 람다를 참조시켜 보자.  

![image03](/assets/java/java/image03.png){: .shadow}  

되지 않는다.  

에러문구에선 오직 함수형 인터페이스로만 형변환이 가능하다고 출력된다.  

이는 `System.out.println()`같이 `Object`를 매개변수로 받는 클래스에게 바로 람다식으로 만들어진 익명객체를 사용 불가능하다는 뜻이다.  

또한 객체라면 모두 가지고 있을 `toString`과 같은 `Object`의 기본메서드 또한 사용할 수 없다.  

자바 컴파일러에서 일반 익명객체를 생성하는 것과 다르게 **람다식의 익명객체**를 생성하기 때문...


### 공통 함수형 인터페이스  

`Object` 클래스로 업캐스팅 하지 못하는 것은 자바 장점인 다형성에 위배된다.  
람다식을 사용할 수 있는 메서드를 정의하기 위해서 항상 매개변수 1개, 매개변수 2개 짜리 함수형 인터페이스를 선 정의후 사용해야 할까?   

이런 과정을 생략하기 위해 이미 `java.util.function`패키지에 해당 함수형 인터페이스를 모두 구현해 두었다.  

**함수형 인터페이스**|**메서드**|**설명**
|---|---|---|
`Predicate<T>`|`boolean test(T t)`|매개변수는 하나, 반환값은 `boolean`  
`Consumner<T>`|`void accept(T t)`|매개변수는 있고 반환값은 없음
`Function<T, R>`|`R apply(T t)`|매개변수, 반환값 모두 있음, `R`은 반환값 타입
`Supplier<T>`|`T get()`|매개변수는 없고 반환값은 있음
`Runnable`|`void run()`|매개변수, 반환값 모두 없음

매개변수, 반환값 타입 또한 제너릭으로 정의되어 있기 때문에 동적으로 람다식을 작성할 수 있다.  

물론 매개변수가 3개 이상인 특별한 람다식을 작성하고 `Object`로 업캐스팅까지 해야 한다면 직접 `FunctionalInterface`를 만들어야 한다.  

이제 사과 색깔, 무게 필터링하자고 `AppleWeightPredicate`, `AppleGreenPredicate` 같은 함수형 인터페이스 정의를 할 필요가 없다.  

### 메서드 레퍼런스

이미 정의된 메서드가 있다면 람다식을 새로 생성하는 것 보다 기존의 메서드를 재활용 하는게 효율적이다.  

이미 위에서 한번 사용한 적이 있다.  

```java
List<Apple> greenApples = filterApples(inventory, FilteringApples::isGreenApple);
```

메서드 레퍼런스로 static 메서드, 상위객체의 메서드, 인스턴스 메서드 모두 사용 가능하며 아래와 같은 형식으로 사용할 수 있다.  

```java
List<String> strList = Arrays.asList("a", "b", "c", "d");
strList.sort((s1, s2)->s1.compareToIgnoreCase(s2));


List<String> strList = Arrays.asList("a", "b", "c", "d");
strList.sort(String::compareToIgnoreCase);
```

어차피 동작파라미터의 매개변수, 타입, 반환값 모두 이미 정의되어 있는 상황이기에 메서드 참조값만 전달한다.  

람다식을 사용하면 기존의 익명 클래스 정의할 필요 없이 코드 간략화가 가능하였다.  

```java
inventory.sort(new AppleComparator() {
  public int compare(Apple a1, Apple a2) {
    return a1.getWeight().compareTo(a2.getWeight());
  }
});
```

```java
inventory.sort((a1, a2) -> a1.getWeight().compareTo(a2.getWeight()));
```

여기서 `Comparator` 객체를 생성하는 `comparing` 메서드를 사용하면 좀더 간략화 할 수 있다.

```java
import static java.util.Comparator.comparing;
inventory.sort(comparing((a) -> a.getWeight()));
```

여기서 메서드 레퍼런스를 사용하면 조금 더 간략화 할 수 있는데 아래와 같다.  

```java
import static java.util.Comparator.comparing;
inventory.sort(comparing(Apple::getWeight));
```


### 람다 표현식 조합 조합

람다 표현식을 사용해 익명 클래스를 쉽게 정의(구현)할 수 있는 방법을 알았다.  
`Predicate`, `Comparator`, `Function` 3가지 함수는 각종 람다식을 조합해 익명클래스를 구현할 수 있다.  

#### Predicate 조합  

```java
public class FilteringApples {
  public static boolean isRedApple(Apple apple) {
    return "red".equals(apple.getColor());
  }
  public static boolean isHeavyApple(Apple apple) {
    return apple.getWeight() > 150;
  }
  public static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
    List<Apple> result = new ArrayList<>();
    for (Apple apple : inventory) {
      if (p.test(apple)) {
        result.add(apple);
      }
    }
    return result;
    }
}

public class MainApplication {
  public static void main(String... args) {
    Predicate<Apple> redApple = FilteringApples::isRedApple;
    Predicate<Apple> redAndHeavyApple = redApple.and(FilteringApples::isHeavyApple);
    ...
    ...
    List<Apple> result = filterApples(inventory, redAndHeavyApple);
  }
}
```

`and()`, `or()` 메서드를 사용해 조합이 가능하다.  

메서드 레퍼런스 없이 사용한다면 아래와 같이 정의할 수 있다.  
```java
Predicate<Apple> redAndHeavyApple =
  ((Predicate<Apple>) a -> a.getColor().equals("red"))
  .and(a -> a.getWeight() > 150);
```

#### Comparator 조합

기존 정렬 방식은 `Comparator` 객체를 람다나 메서드 레퍼런스로 구현하거나  
해당 구역할을 하는 `comparing` 메서드를 사용하였다.  

```java
inventory.sort(comparing(Apple::getWeight));
```

여기서 `reversed()`, `thenComparing()` 메서드를 더해 역정렬, 추가 정렬 조건을 조합할 수 있다.  

```java
inventory.sort(
  comparing(Apple::getWeight)
    .reversed()
    .thenComparing(Apple::getColor)
);
```

무게별로 역정렬하고 서로 값이 같다면 색깔별로 추가정렬한다.  


#### Function 조합  

T를 받아 R을 반환하는 구조의 메서드를 정의할때 `Function<T, R>` 형식의 함수형 인터페이스를 사용한다.  

`andThen()`, `compose()` 메서드를 조합해 두개 이상의 메서드를 하나로 합칠 수 있다.   

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x * 2;
Function<Integer, Integer> h = f.andThen(g);
h.apply(1); // 4
```

`h(x) = g(f(x))` 형식으로 표현할 수 있다.  

```java
Function<Integer, Integer> f = x -> x + 1;
Function<Integer, Integer> g = x -> x * 2;
Function<Integer, Integer> h = f.compose(g);
h.apply(1); // 3
```

`h(x) = f(g(x))` 형식으로 표현할 수 있다.  


## 스트림  

거의 모든 자바 어플리케이션은 컬렉션을 만들고 처리하는 과정을 포함한다.  

그리고 거진 대부분의 반복잡업들이 지루하게 연속된다.  
이런 반복 잡업을 병렬작업으로 처리하면서도 짧고 간결한 코드를 사용할 수 있도록 지원하는것이 스트림이다.  

```java
@Data
public class Dish {
  public enum Type {MEAT, FISH, OTHER}

  private final String name;
  private final boolean vegetarian;
  private final int calories;
  private final Type type;

  public static final List<Dish> menu =
    Arrays.asList(
      new Dish("pork", false, 800, Dish.Type.MEAT),
      new Dish("beef", false, 700, Dish.Type.MEAT),
      new Dish("chicken", false, 400, Dish.Type.MEAT),
      new Dish("french fries", true, 530, Dish.Type.OTHER),
      new Dish("rice", true, 350, Dish.Type.OTHER),
      new Dish("season fruit", true, 120, Dish.Type.OTHER),
      new Dish("pizza", true, 550, Dish.Type.OTHER),
      new Dish("prawns", false, 400, Dish.Type.FISH),
      new Dish("salmon", false, 450, Dish.Type.FISH)
    );
}
```

위와 같은 코드가 있을때 `calories` 가 400을 넘지 않고 `calories` 로 정렬된 요리 이름을 3개까지 `List<String>` 로 뽑아내려면 아래와 같은 과정을 거친다.  

```java
List<Dish> lowCaloricDishes = new ArrayList<>();
for(Dish d: Dish.menu){
  if(d.getCalories() > 400){
    lowCaloricDishes.add(d);
  }
}
lowCaloricDishes.sort(comparing(Dish::getCalories));
List<String> lowCaloricDishesName = new ArrayList<>();
for (int i = 0; i < 3 && i < lowCaloricDishes.size(); i++) {
  lowCaloricDishesName.add(lowCaloricDishes.get(i).getName());
}
return lowCaloricDishesName;
/* salmon
french fries
pizza */
```

이를 `stream()` 을 사용하면 아래와 같이 사용할 수 있다.  

```java
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
...
...
return Dish.menu.stream()
// return Dish.menu.parallelStream()
  .filter(d -> d.getCalories() > 400) // 스트림에서 특정 요소를 제외
  .sorted(comparing(Dish::getCalories)) // 
  .map(Dish::getName) // 람다를 이용해 다른 요소로 변환, 추출
  .limit(3) // 스트림의 크기를 최대 3개로 축소
  .collect(toList()); // stream 을 list 로 변경
```

![image06](/assets/java/java/image06.png){: .shadow}  


각 함수가 파이프라인을 구성하여 데이터 처리연산을 진행한다.  

> linux 의 `cat file1 | tr "[A-Z]" "[a-z]" | sort | tail -3` 명령어와 같이 파이프를 사용해 명령끼리 연결  


### 스트림과 콜렉션   

컬렉션과 스트림 모두 연속된 데이터를 순차적으로 접근한다.  

이 둘의 차이를 시각적으로 표현하면 동영상을 DVD 로 보냐, 인터넷 스트리밍으로 보냐 로 구분지을 수 있다.  

**데이터를 언제 연산하느냐**가 스트림과 콜렉션의 가장 큰 차이점이다.  

컬렉션은 데이터를 사용하든 안하든 일단 모두 메모리상에 저장해두고 계산을 진행하고  
스트림은 요청할 때만 요소를 계산할 수 있는 자료구조이다.(스트림에 요소를 추가하거나 제거할 수 없다)  

콜렉션과 다르게 **스트림은 단 한번만 탐색**할 수있다. (`Iterator` 와 같다)
만약 다시 탐색하려면 초기데이터에서 새로운 스트림을 다시 만들어야 한다.  

```java
List<String> title = Arrays.asList("Java8", "In", "Action");
Stream<String> s = title.stream();
s.forEach(System.out::println);
s.forEach(System.out::println); // IllegalStateException 발생  
```

콜렉션은 외부반복, 스트림은 **내부반복**을 사용한다.   

콜렉션을 탐색하려면 for 문등을 사용해 외부에서 루프를 돌지만  
스트림은 파이프라인을 실행할 뿐 외부적으로 반복 구문을 표기하지 않는다.  

내부반복을 통해 병렬 작업을 투명하게, 다양한 순서로 처리할 수 있다.  
만약 외부 반복(`forEach`등)을 통해 병렬 작업을 진행 하려면 `synchronize` 구문을 사용해 복잡한 코드를 구성해야 한다.  

![image04](/assets/java/java/image04.png){: .shadow}  

### 스트림 중간/최종 연산  

![image05](/assets/java/java/image05.png){: .shadow}  

스트림을 통해 실제 연산(중간 연산과정)이 이루어 지는 타이밍은 **최종연산** 때이다.  

실행 과정을 알기 위해 중간중간에 `print` 를 통해 요소를 출력  

```java
List<String> list = dishes.parallelStream()
  .limit(3)
  .filter(d -> {
    System.out.println("filtering: " + d.getName() + ", calories: " + d.getCalories());
    return d.getCalories() > 400;
  })
  .map(d -> {
    System.out.println("mapping: " + d.getName() + ", calories: " + d.getCalories());
    return d.getName();
  })
  .collect(toList());
  System.out.println(list);
```

```
- 출력 결과
filtering: beef, calories: 700
filtering: pork, calories: 800
filtering: chicken, calories: 400
mapping: pork, calories: 800
mapping: beef, calories: 700
[pork, beef]
```

**쇼트 서킷** - `limit` 로 인해 첫째 조건에 해당하는 3가지 요소만 가져오고 더이상의 요소는 돌지 않는다.  

**루프 퓨전** - 중간 연산(`filter`, `map`) 을 한과정으로 병합

#### 중간 연산  

**연산**|**반환형식**|**연산의 인수**|**함수 디스크립터**|**목적**
|---|---|---|---|---|
`filter`|`Stream<T>`|`Predicate<T>`|`T -> boolean`|조건에 부합하는 요소 필터링  
`map`|`Stream<T>`|`Fcuntion<T, R>`|`T -> R`|요소 변환/추출  
`limit`|`Stream<T>`|||n개 요소만 포함  
`sorted`|`Stream<T>`|`Comparator<T>`|`(T, T) -> int`|요소 정렬  
`distinct`|`Stream<T>`||중복 요소 필터링  
`skip`|`Stream<T>`||첫 n개 요소 스킵

#### 최종 연산

**연산**|**목적**|**반환값**
|---|---|---|
`forEach`|스트림의 각 요소 소비(람다 사용)|반환값 없음  
`count`|스트림의 각 요소 개수 반환|`long`  
`collect`|스트림을 리듀스, 리스트, 맵 등의 컬렉션 생성|`Collection`  
`anyMatch`|`Predicate` 가 적어도 한 요소와 일치하는지 확인|`boolean`  
`allMatch`|`Predicate` 가 모든 요소와 일치하는지 확인|`boolean`  
`noneMatch`|`Predicate` 가 모든 요소와 일치하지 않는지 확인|`boolean`  
`findAny`|`Predicate` 가 일치하는 한 요소 반환|`Optional<T>`  
`findFirst`|`Predicate` 가 일치하는 첫 요소 반환|`Optional<T>`  

병령 처리시에는 `findAny` 를 사용해야 하며 그 외의 경우는 `findAny`, `findFirst` 결과값은 같다.  

`anyMatch` `allMatch` `noneMatch` `findAny` `findFirst` 모두 쇼트 서킷이 적용된다.  
하나의 일치하는 요소를 찾으면 모든 stream 을 돌지 않고 끝낸다.  

#### Optional

java 8 에 추가된 `null` 처리를 쉽게 해결하기 위해 만들어진 객체  

`isPresent()` - 값의 존재 여부에 따라 `boolean` 값 반환  
`isPresent(Consumer<T> block)` - 값이 존재하면 `block`의 구현 메서드 실행  
`T get()` - 값이 존재하면 반환, 없으면 `NoSuchElementException`예외 발생  
`T orElse(T other)` - 값이 존재하면 반환, 없으면 작성한 other 반환  



### 기타 연산 메서드  

#### flatMap

스트림을 통해 `words` 를 `letters` 처럼 변경하고 싶을 때  

```java
List<String> words = Arrays.asList("Hello", "World");
// word -> letters 로 변경
List<String> letters = Arrays.asList("H", "e", "l", "l", "o", "W", "o", "r", "l", "d");
```

`Stream<List<String>>` -> `Stream<String[]>` -> `Stream<String>`  


위처럼 변환과정을 거쳐야한다.  

단순하게 `map()` 을 사용해서도 해결될 것 같지만 결과는 그렇지 않다.  

```java
List<String> words = Arrays.asList("Hello", "World");
Stream<String[]> stream = words.stream().map(word -> word.split(""));
//[ ["H", "e", "l", "l", "o"], ["W", "o", "r", "l", "d"] ]
```

`word.split("")` 으로 인해 문자열이 쪼개지긴 한다. 하지만 각각의 배열에 배치될뿐 하나의 배열안의 데이터로 사용할 순 없다.  

여기서 `Stream<String[]>`에 `collect(toList())` 로 변환해 봤자 `List<String[]>` 가 반환될뿐 `List<String>` 이 반환되진 않는다.  

반환된 `Stream<String[]>` 에 다시 `map()` 을 해봤자 `List<Stream<String>>` 가 반환된다.  

`List<String[]>` 내부의 요소 자체를 `Stream`의 요소로 사용할 수 있어야 하는데  
이때 `flatMap` 을 사용해야 한다.  

```java
List<String> result = words.stream()
  .map(word -> word.split(""))
  //.flatMap(word -> Arrays.stream(word))
  .flatMap(Arrays::stream)
  .distinct()
  .collect(toList());

System.out.println(result.toString());
// [H, e, l, o, W, r, d]
```

![image06](/assets/java/java/image06.png){: .shadow}  

`flatMap` 을통해 각 배열안의 데이터 요소를 모두 꺼집어 내어 하나의 스트림으로 다시 생성한다.  


#### reduce

> reduce: 줄이다, 축소한다.  

각 스트림 요소를 조합해 복잡한 연산을 해야할 경우 **리듀싱 연산**을 사용한다.  

모든 요소를 더해야 할 경우 java 8 이전에 아래와 같이 for문을 사용해 순회한다.  

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
int sum = 0;
for (Integer number : numbers) {
  sum += number;
}
```

`reduce()` 를 사용하면 스트림의 모든 요소를 한번에 연산할 수 있다.  

```java
List<Integer> numbers = Arrays.asList(4, 5, 3, 9);
int sum = numbers.stream().reduce(0, (a, b) -> a + b);
```

![image07](/assets/java/java/image07.png){: .shadow}  

`reduce()`의 매개변수로 초기값과 `BinaryOperator<T>` 구현 메서드 2가지를 필요로한다.  

> `BinaryOperator<T>` - 두 요소를 조합하여 `T` 를 반환하는 반환하는 함수형 인터페이스 

```java
List<Integer> numbers = Arrays.asList(4, 5, 3, 9);
Optional<Integer> sum = numbers.stream().reduce((a, b) -> a + b);
```

위처럼 초기값을 생략할 수 도 있지만 반환타입이 `Optional<T>`이 된다.  
요소가 없을 경우도 있기 때문.  

최소, 최대값을 구하는것도 쉽게 할 수 있다.  
```java
Optional<Integer> max = numbers.stream().reduce(Integer::max);
Optional<Integer> min = numbers.stream().reduce(Integer::min);
```

![image08](/assets/java/java/image08.png){: .shadow}  


`count()`를 통해 개수를 구할 수도 있겠지만 아래와 같은 방식도 가능하다.  

```java
int count = Dish.menu.stream()
  .map(d -> 1)
  .reduce(0, (a, b) -> a + b);
```

#### 기본형 특화 스트립

```java
int calories = menu.stream()
  .map(Dish::getCalories)
  .reduce(0, Integer::sum);


int result = menu.stream()
  .mapToInt(Dish::getCalories) //IntStream
  //.average()
  //.max()
  //.min()
  .sum();
```

스트림 요소의 합계를 구할때 `reduce` 를 사용해 구할 수 있다.  

자주 사용되는 기본형 스트림(`IntStream`)의 경우 **평균, 합계, 최대, 최소값** 같은 흔히 사용되는 `reduce`메서드들을 추가 구현해두었다.  

`IntStream` 외에도 `Double`, `Long` 의 기본형 스트림을 지원한다.

### collect

최종 연산 `collect()` 메서드와 `Collectors.toList()` 를 사용해 스트림의 결과값을 리스트 객체로 반환하였다.  

`collect()`는 리스트 외에도 다양한 방법으로 결과값을 산출해 낼 수 있다.  

```java
public enum Currency {
  EUR, USD, JPY, GBP, CHF
}
@Data
public static class Transaction {
  private final Currency currency;
  private final double value;
}

public static List<Transaction> transactions = Arrays.asList(
  new Transaction(Currency.EUR, 1500.0),
  new Transaction(Currency.USD, 2300.0),
  new Transaction(Currency.GBP, 9900.0),
  new Transaction(Currency.EUR, 1100.0),
  new Transaction(Currency.JPY, 7800.0),
  new Transaction(Currency.CHF, 6700.0),
  new Transaction(Currency.EUR, 5600.0),
  new Transaction(Currency.USD, 4500.0),
  new Transaction(Currency.CHF, 3400.0),
  new Transaction(Currency.GBP, 3200.0),
  new Transaction(Currency.USD, 4600.0),
  new Transaction(Currency.JPY, 5700.0),
  new Transaction(Currency.EUR, 6800.0));
```

위와 같은 형태의 데이터가 있을때 `Currency` 별로 `Transaction` 객체를 `List`로 모아 `Map`으로 관리하고 싶을때   

```java
Map<Currency, List<Transaction>> transactionsByCurrencies = new HashMap<>();
for (Transaction transaction : transactions) {
  ...
  ...
}
```

최대한 간단하게 작성하려 해도 최소 6줄은 써야할 것 같다.  

스트림과 `collect` 메서드를 사용하면 두줄이면 끝난다.  

```java
Map<Currency, List<Transaction>> transactionsByCurrencies = 
  transactions.stream().collect(Collectors.groupingBy(Transaction::getCurrency));
```

![image09](/assets/java/java/image09.png){: .shadow}  

#### collect, Collectors

위의 `reduce` 의 기능을 `collect` 최종연산과 `Collectors` 객체의 각종 메서드를 사용하면 그대로 구현할 수 있다.  

```java
int reduce_cnt = Dish.menu.stream().map(d -> 1).reduce(0, (a, b) -> a + b);

int collect_cnt = Dish.menu.stream().collect(Collectors.counting());
```

> 물론 바로 최종연산자인 `count()` 컬렉션의 `size()` 메서드 를 호출하는게 제일 간단하다.  
`menu.stream().count(); menu.size()`  

`Collectors` 객체 내부에 각종 `reduce` 연산 방법들을 알아보자.  


```java
import static java.util.stream.Collectors.*;
...
...
/*최대값 찾기*/  
Optional<Dish> collect = menu.stream().collect(maxBy(comparing(Dish::getCalories)));
Optional<Dish> reduce = menu.stream().reduce((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2);
// minBy

/**합계 구하기*/
int collect = menu.stream().collect(summingInt(Dish::getCalories));
int reduce = menu.stream().mapToInt(Dish::getCalories).reduce(0, (d1, d2) -> d1 + d2);
// int reduce = menu.stream().mapToInt(Dish::getCalories).sum();
// summingDouble, summingLong

/*평균 구하기*/
double collect = menu.stream().collect(averagingDouble(Dish::getCalories));
OptionalDouble reduce = menu.stream().mapToInt(Dish::getCalories).average();
// averagingInt, averagingLong\

/*종합세트*/
IntSummaryStatistics summary = menu.stream().collect(summarizingInt(Dish::getCalories));
summary.getAverage();
summary.getCount();
summary.getMax();
summary.getMin();
summary.getSum();
// summarizingDouble, summarizingLong
```

#### reducing

```java
public static <T, U> Collector<T, ?, U> reducing(
  U identity,
  Function<? super T, ? extends U> mapper,
  BinaryOperator<U> op)
```

연산 타입과 반환 타입이 자동으로 정해지는 `reduce()` 와 달리  
`Collectors.reducing` 을 사용하면 자유자재로 지정할 수 있다.  

```java
int reduce = menu.stream().mapToInt(Dish::getCalories).reduce(0, (d1, d2) -> d1 + d2);
int reduce = menu.stream().collect(reducing(0, Dish::getCalories, (d1, d2) -> d1 + d2);
```

기존 `reduce()` 메서드와 달리 매개변수를 3개를 받는다.  

`reducing()` 은 두번째 매개변수로 `Fcuntion<T, R>` 형식의 람다식을 받아 변환처리 후 연산할 수 있도록 지원한다.  

> `(d1, d2) -> d1 + d2` 는 `Integer::sum` 으로 대체 가능하다.  


```java
menu.stream().collect(reducing(0, Dish::getCalories, Integer::sum);
```

> `Collectors.reducing` 과 `Stream.reduce` 의 차이 - `reducing()` 의 경우 `mapper` 과정을 통해 데이터를 연산하기 좋은 값으로 한번 변환해서 사용한다. 때문에 변환을 해도 기존 시드 데이터에 영향이 가지 않는다.  
> 반면 `recduce()`의 경우 기존의 데이터를 바로 사용하기에 변환과정에서 아직 연산하지 못한 시드 데이터에 영향이 갈 수 있다. 병령이외의 경우는 별 차이점 없지만 병렬 과정에선 `reduce()` 는 문제가 발생할 수 있다.  

#### collect, Collectors - groupingBy

위의 최종연산 과정은 굳이 `collect` 를 사용하지 않고도 충분히 간결하게 구성할 수 있지만  
그룹핑은 `collect` 를 사용하는 것이 훨씬 수월하다.  

```java
public static <T, K> Collector<T, ?, Map<K, List<T>>> 
  groupingBy(Function<? super T, ? extends K> classifier)
```

```java
Map<Currency, List<Transaction>> transactionsByCurrencies = 
  transactions.stream().collect(Collectors.groupingBy(Transaction::getCurrency));
```

`groupingBy`를 사용하면 위와같은 일반적인 1차원 그룹화도 가능하지만 n차원 그룹화도 쉽게 할 수 있다.  

```java
enum CaloricLevel { DIET, NORMAL, FAT };
enum Type { MEAT, FISH, OTHER }

Arrays.asList( 
  new Dish("pork", false, 800, Type.MEAT),
  new Dish("beef", false, 700, Type.MEAT),
  new Dish("chicken", false, 400, Type.MEAT),
  new Dish("french fries", true, 530, Type.OTHER),
  new Dish("rice", true, 350, Type.OTHER),
  new Dish("season fruit", true, 120, Type.OTHER),
  new Dish("pizza", true, 550, Type.OTHER),
  new Dish("prawns", false, 400, Type.FISH),
  new Dish("salmon", false, 450, Type.FISH));
```

위의 데이터는 `Dish.Type` 별, 그안에서 `calories` 별로 그룹핑(2차원 그룹화) 해보자.  

400 이하 칼로리는 `DIET`, 700 이하 칼로리는 `NORMAL`, 그외에는 `FAT` 으로 분류한다.  
`groupingBy` 를 연덜아 사용하여 그룹핑 

```java
Map<Dish.Type, Map<CaloricLevel, List<Dish>>> goupTypeAndDish = 
  menu.stream().collect(
    groupingBy(Dish::getType, //1번째 분류함수
      groupingBy((Dish dish) -> { //2번째 분류함수
        if (dish.getCalories() <= 400) return CaloricLevel.DIET;
        else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
        else return CaloricLevel.FAT;
      })
    )
  );
```

두번째 매개변수로 `Collectors.counting()` 를 넣으면 각종 정보를 그룹핑할 수 있다.  

```java
Map<Dish.Type, Long> typesCount = menu.stream().collect(
  groupingBy(Dish::getType, Collectors.counting())
);
Map<Dish.Type, Dish> typesMax = menu.stream().collect(
  groupingBy(Dish::getType, Collectors.maxBy(ComparingInt(Dish::getColories), Optional::get))
);
```

사실 처음 사용한 매개변수 1개짜리 `groupingBy(Function<T, K> classifier)` 는 두번째 매개변수로 `toList()` 가 생략된 것이다. 실제는 아래와 같다.  
`groupingBy(Function<T, K> classifier, toList())`

2번째 매개변수로 합계, 개수등 위에서 소개한 `Collectors`각종 메서드(최대, 최소, 합계, 평균)를 사용할 수 있다.  

#### collect, Collectors - partitioningBy  

분할은 특별한 방식의 그룹화라 할 수 있다.  

```java
return menu.stream().collect(partitioningBy(Dish::isVegetarian));
```

매개변수로 `Predicate` 를 받기 때문에 `true` ,`false` 로만 그룹화가 가능한 메서드이다.  

### 커스텀 Collectors

지금까지 `stream().collect()` 내부에 `Collectors` 의 구현체를 집어넣어  
그룹화, 리스트반환, 집계결과를 출력하였다.  

`groupingBy`, `toList` 등과 같은 메서드가 모두 구현된 `Collector` 객체를 반환하였다.  

정말 특별한 기능을 하고싶다면 기존 `Collectors` 에 이미 정의되어 있는 메서드가 아닌 직접 커스터마이징 한 메서드를 사용해야 할것이다.  

```java
public class CustomCollector implements Collector {
    @Override
    public Supplier supplier() {...}
    @Override
    public BiConsumer accumulator() {...}
    @Override
    public BinaryOperator combiner() {...}
    @Override
    public Function finisher() {...}
    @Override
    public Set<Characteristics> characteristics() {...}
}
```

`Collector` 를 구현하려면 기본적으로 위의 5가지 메서드를 구현해야 한다.  

커스텀하기 전에 먼저 `toList()` 메서드가 반환하는 `Collector` 객체를 확인해보자.  

```java
public static <T> Collector<T, ?, List<T>> toList() {
      return new CollectorImpl<>(
          (Supplier<List<T>>) ArrayList::new, 
          List::add,
          (left, right) -> { left.addAll(right); return left; },
          CH_ID);
}
```

위의 형식대로 커스터마이징할 객체를 정의하면 된다.  

#### Supplier - supplier

`Supplier` 는 공통 함수형 인터페이스중 하나로 `T get()` 형식의 구조를 갖는다.  
`Supplier` 는 수집과정에서 사용되는 **빈 누적자**를 반화하는 메서드  

`ArrayList::new` 생성자 레퍼런스를 사용해 `Supplier` 를 구현하였다.  
초기에 요소를 집어넣게 전에 비어있는 `ArrayList` 인스턴스를 생성하기 위해

커스텀을 하면 초기값에 이미 몇가지의 데이터가 삽입되어 있는 상태로 구성할 수 도 있다.  

#### BiConsumer - accumulator 

`BiConsumer` 함수형 인터페이스는 `void accept(T t, U u)` 형식의 구조를 갖는다.  
리듀싱 연산을 수행하는 함수를 반환한다.  

`n-1` 번까지 누적해둔 요소와 `n`번째 요소를 연산하는 메서드. `toList`는 `List::add` 메서드 레퍼런스를 사용한다.  
아래와 같이 구현된다 보면 된다.  
```java
public BiConsumer<List<T>, T> accumulator() {
  return (list, item) -> list.add(item);
}
```

#### BinaryOperator - combiner 

`BinaryOperator<T>` 함수 인터페이스는 `T apply(T t1, T t2)` 형식의 구조를 갖는다.  

`BiFunction` 과 같은 구조이지만 매개변수 타입과 반환타입이 모든 같다.  

스트림을 병렬로 처리할때 누적자가 이 결과를 어떻게 처리할 지 결정한다.  

`toList()` 에선 아래의 람다식을 사용한다.  

```java
(left, right) -> { left.addAll(right); return left; }
```

그냥 단순히 2개의 리스트를 합치는 간단한 방식이다.  


#### Function - finisher 

`Function<T, R>` 함수 인터페이스는 `R apply(T t)` 형식의 구조를 갖는다.  

스트림 탐색을 끝내고 누적자 객체를 최종 결과로 변환할 때 사용하는 메서드.  

이상하게도 `toList()` 에선 매개변수가 4개밖에 없고 finisher 를 위한 구현 클래스는 생략되었다.  
이는 뒤에 나오는 `characteristics` 과 연관이 있는데 누적자를 그대로 결과물로 사용할 수 있는경우 생략한다(`CH_ID` 사용).  

생략할 경우 finisher 메서드는 아래와 같이 구현된다 보면된다.  

```java
public Function<List<T>, List<T>> finisher() {
  return function.identity();
}
...
...
static <T> Function<T, T> identity() {
    return t -> t;
}
```

위의 5가지 메서드를 합치면 최종적으로 아래와 같은 그림이 완성된다.  

![image10](/assets/java/java/image10.png){: .shadow}  


#### characteristics

컬렉터의 연산을 정의하는 `Characteristics` 형식의 불변집합을 반환.  
스트림 병려 연산과정에서 어떤식으로 연산할지 정해주는 역할을 한다.

```java
static final Set<Collector.Characteristics> CH_CONCURRENT_ID
  = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                            Collector.Characteristics.UNORDERED,
                                            Collector.Characteristics.IDENTITY_FINISH));
static final Set<Collector.Characteristics> CH_CONCURRENT_NOID
  = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.CONCURRENT,
                                            Collector.Characteristics.UNORDERED));
static final Set<Collector.Characteristics> CH_ID
  = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));
static final Set<Collector.Characteristics> CH_UNORDERED_ID
  = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                                            Collector.Characteristics.IDENTITY_FINISH));
static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();
```

`Collectors` 클래스에서 여러가지 형식의 `Set<Characteristics>` 데이터를 이미 정의해 두었다. 

타입|설명  
|---|---|
`UNORDERED` | 리듀싱 결과는 스트림의 요소 방문, 누적 순서에 영향을 받지 않는다.  
`CONCURRENT` | 다중 스레드에서 `accumulator` 함수를 동시에 호출 가능, `UNORDERED`와 같이 사용되지 않을경우 `Set` 과 같은 집합에서만 사용 가능하다.  
`IDENTITY_FINISH` | `finisher` 메서드가 생략 가능. 리듀싱 최종 결과로 누적자 객체를 바로 사용한다는 뜻.

## 병렬  

컬렉션 클래스에서 `parallel()` 메서드를 쉽게 병렬로 데이터 처리가 가능하다.  
하지만 병렬이라고 무조건 싱글 스레드 처리보다 빠른것이 아니며  
병렬로 실행할 경우 분리(포크)/합병(조인) 하는 과정이 생기기에 추가적인 연산과정이 필요하기 때문에  
적은 데이터 연산 과정의 경우 오히려 연산시간이 더 늘어날 수 있다.  

측정이 아닌 스트림 성능 측정을 통해 선택방식을 결정해야 한다.  

### sequential(), parallel()

사실 우리가 일반적으로 사용하는 스트림은 `sequential`(순차적) 방식이며 내부적으로 `boolean flag` 가 스위치 역할을 하며 `parallel` 로 실행할지 `sequential`로 실행할지 결정된다.  

만약 `stream().sequential().parallel()...;` 처럼 2개 모두 사용하면 마지막에 호출된 `parallel()` 메서드가 `boolean flag` 를 변환한다.  

### 성능 측정  

`jmh(Java Microbenchmark Harness)` 라이브러리를 사용하면 가비지 컬렉터, 바이트코드 최적화 등의 잡다한 시간을 무시하고 쉽게 벤치마크를 할 수 있다.  

```xml
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.21</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.21</version>
</dependency>
```

```java

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime) // 밴체마크 대상 실행 평균 시간 측정
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 밴치마크 시간 단위
@Fork(value = 2, jvmArgs = { "-Xms4G", "-Xmx4G" }) // 4G 힙공간에서 2회 실행
@Measurement(iterations = 2)
@Warmup(iterations = 3)
public class ParallelStreamBenchmark {

  private static final long N = 10_000_000L;

  @Benchmark // 밴치마크 대상 메서드
  public long iterativeSum() {
    long result = 0;
    for (long i = 1L; i <= N; i++) {
      result += i;
    }
    return result;
  }
/*Result "modernjavainaction.chap07.ParallelStreamBenchmark.iterativeSum":
  3.910 ±(99.9%) 0.376 ms/op [Average]
  (min, avg, max) = (3.838, 3.910, 3.957), stdev = 0.058
  CI (99.9%): [3.533, 4.286] (assumes normal distribution)*/

  @Benchmark
  public long parallelSum() {
    return Stream.iterate(1L, i -> i + 1).limit(N).parallel().reduce(0L, Long::sum);
  }
/*Result "modernjavainaction.chap07.ParallelStreamBenchmark.parallelSum":
  103.221 ±(99.9%) 28.860 ms/op [Average]
  (min, avg, max) = (99.191, 103.221, 107.178), stdev = 4.466
  CI (99.9%): [74.361, 132.081] (assumes normal distribution)*/
```

전통적인 for문 방식은 3초대가 나온반면 병렬로 실행하는 방식은 100초 가까이 걸렸다.  

사실 위의 `parallelSum()` 은 병렬방식으로 실행되지않고 순차적인 방식으로 진행된다.  

`iterate`를 사용하면 `reduce` 메서드 실행전까지 스트림이 생성되지 않기 때문에 일반적인 순차 스트림 방식으로 `reduce` `sum` 함수를 진행하고  
여기서 발생하는 기본형으로 언박싱, 스레드가 생성으로 인한 오버헤드가 발생한다.  


```java


  @Benchmark
  public long rangedSum() {
    return LongStream.rangeClosed(1, N).reduce(0L, Long::sum);
  }

  @Benchmark
  public long parallelRangedSum() {
    return LongStream.rangeClosed(1, N).parallel().reduce(0L, Long::sum);
  }
/*
## Run progress: 20.00% complete, ETA 00:06:43
## Fork: 1 of 2
## Warmup Iteration   1: 1.177 ms/op
## Warmup Iteration   2: 1.077 ms/op
## Warmup Iteration   3: 1.102 ms/op
Iteration   1: 1.079 ms/op
Iteration   2: 1.084 ms/op

## Run progress: 30.00% complete, ETA 00:05:53
## Fork: 2 of 2
## Warmup Iteration   1: 6.546 ms/op
## Warmup Iteration   2: 8.517 ms/op
## Warmup Iteration   3: 7.539 ms/op
Iteration   1: 7.777 ms/op
Iteration   2: 7.675 ms/op


Result "modernjavainaction.chap07.ParallelStreamBenchmark.parallelRangedSum":
  4.404 ±(99.9%) 24.792 ms/op [Average]
  (min, avg, max) = (1.079, 4.404, 7.777), stdev = 3.837
  CI (99.9%): [≈ 0, 29.195] (assumes normal distribution)
*/
  @TearDown(Level.Invocation) // 각 밴치마크 실행 후 가비지 컬렉터 동작
  public void tearDown() {
    System.gc();
  }
}
```

일반적인 순차스트림에서 사용하는 공유 데이터를 사용하는 것에서도 병렬 처리방식으로 변경될 경우 정확한 데이터처리가 이루어지지 않을 수 있다.  


### 포크/조인 프레임워크  

`java8` 이전에 어떤식으로 병렬 프로그래밍을 진행했는지 알아보자.  

`java7`에 추가된 포크/조인 프레임워크 방식을 사용한다.  


![image11](/assets/java/java/image11.png){: .shadow}  

각각의 작업들을 잘게 쪼개 서브테스크로 만들고 모든 서브테스크를 수행  
처리결과를 조합하는 과정이 실제 병렬처리에 들어가있다.  

작업을 나누고 스레드에 할당하는 과정에서 `java.util.concurrent.RecursiveTask` 객체를 통해 재귀적으로 스레드 풀을 사용하게 된다.  

```java
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    public static final long THRESHOLD = 10_000;
    private final long[] numbers;
    private final int start;
    private final int end;

    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD)  // 처리개수가 THRESHOLD 보다 작아진다면 연산 후 반환 
            return computeSequentially();
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2); // 분할1
        leftTask.fork(); // 태스크 비동기 실행
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end); // 분할2
        Long rightResult = rightTask.compute(); // 태스크 분할
        Long leftResult = leftTask.join(); // 결과가 도출될때 까지 대기 
        return leftResult + rightResult;
    }

    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static long forkJoinSum(long n) {
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return new ForkJoinPool().invoke(task); // compute 결과 반환.
    }
}
```

`forkJoinSum()` 메서드를 보면 `java.util.concurrent.ForkJoinTask` 태스크를 생성해 `ForkJoinPool` 에 집어넣어 병렬실행한다.  

`ForkJoinPool` 인스턴스는 은 일반적으로 한개이상 생성하지 않으며 런타임의 `availableProcessors` 에서 반환한 개수만큼의 스레드 풀을 생성해 두며 태스크가 자유롭게 해당 풀에 접근할 수 있도록 한다.  

`invoke()` 메서드를 통해 `ForkJoinSumCalculator` 의 `compute` 메서드가 호출되게 된다.  

`compute` 내부를 살펴보면 `leftTaks` 의 경우 `ForkJoinTask` 의 `fork()`, `join()` 메서드를 호출해 결과를 계산했다.  

`fork()` 를 통해 `compute()` 메서드를 스레드 풀에서 실행시키고 `join()` 메서드를 호출해 예하의 모든 메서드가 종료될때까지(최종 결과값 반환) 기다린다.

> https://www.baeldung.com/java-fork-join

```java
@Override
protected Long compute() {
    int length = end - start;
    if (length <= THRESHOLD) {
        return computeSequentially();
    }
    ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);
    leftTask.fork();
    ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);
    rightTask.fork();
    Long rightResult = rightTask.join();
    Long leftResult = leftTask.join();
    return leftResult + rightResult;
}
```

`compute` 대신 `fork` 를 사용해도 작업내용은 동일하다. `task` 큐에 밀어넣고 `compute` 메서드가 실행되길 기다린다.  

#### 작업훔치기

`fork()` 를 통해 분할된 스레드가 동일한 크기의 태스크를 맡는다는 것을 확신할 수 없고, 비슷한 크기의 태스크를 맡는다 해도 비슷한 시간에 같이 끝난다는 것도 확신할 수 없다.  

최대한 많은 코어가 일을 할 수 있도록 `ForkJoinPool` 에선 **작업훔치기** 기법을 사용한다.  

스레드마다 처리해야할 태스크가 저장되는 작업 큐가 있으며  
자신의 큐의 태스크가 비워지면 다른 스레트의 큐의 꼬리에서 작업을 훔쳐와 처리한다.  

모든 스레드의 작업 큐가 비워질때까지 재귀적으로 반복한다.  

![image12](/assets/java/java/image12.png){: .shadow}  

그림을 보면 분할(`split`)과 동시에 각 스레드에서 작업훔치기(`steal`)를 통해 순식간에 4개의 스레드가 동작한다.  


### Spliterator 인터페이스

![image13](/assets/java/java/image13.png){: .shadow}  

> `Spliterator`: 분할할 수 있는 반복자, `spli + iterator`

java8 부터 모든 컬렉션 클래스에 구현되어 있다.  

간단한 단어의 개수를 세는 코드이다.  

```java
public static int countWordsIteratively(String s) {
    int counter = 0;
    boolean lastSpace = true;
    for (char c : s.toCharArray()) {
        if (Character.isWhitespace(c)) 
            lastSpace = true;
        else {
            if (lastSpace) counter++;
            lastSpace = false;
        }
    }
    return counter;
}
public static void main(String[] args) {
    String sentence = " Nel   mezzo del cammin  di nostra  vita " +
            "mi  ritrovai in una  selva oscura" +
            " che la  dritta via era   smarrita ";
    System.out.println("Found " + countWordsIteratively(sentence) + " words"); 
    // Found 19 words
}
```

공백을 만나면 `lastSpace` 를 `true` 로 변경하고 공백이 아닌 문자를 만날때 마다 `lastSpace`가 `true` 라면 단어 시작으로 보고 카운트를 1 증가하는 코드이다.  


이번엔 `parallel, stream` 의 `reduce` 를 사옹해 `count` 를 구해보자.  

```java
private static class WordCounter {

    private final int counter;
    private final boolean lastSpace;

    public WordCounter(int counter, boolean lastSpace) {
        this.counter = counter;
        this.lastSpace = lastSpace;
    }
    public WordCounter accumulate(Character c) {
        if (Character.isWhitespace(c))
            return lastSpace ? this : new WordCounter(counter, true);
        else
            return lastSpace ? new WordCounter(counter + 1, false) : this;
    }
    public WordCounter combine(WordCounter wordCounter) {
        return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
    }
    public int getCounter() {
        return counter;
    }
}

public static int countWordsByStream(String sentence) {
    WordCounter wordCounter = IntStream.range(0, sentence.length())
        .mapToObj(i -> sentence.charAt(i))
        .parallel()
        .reduce(new WordCounter(0, true),
                (wordCounter1, character) -> wordCounter1.accumulate(character),
                (wordCounter1, wordCounter2) -> wordCounter1.combine(wordCounter2));
    return wordCounter.getCounter();
}

public static void main(String[] args) {
    System.out.println("Found " + countWordsByStream(sentence) + " words"); 
    // Found 43 words
}
```
`WordCounter` 를 통해 charactor 를 감싸고 병렬처리를 위한 메서드 `accumulate`, `combine` 를 정의한다.  

병렬로 실행하게 되먼 생각하지 못했던 문제가 발생한다.  

```java
public static int countWords(String sentence) {
    WordCounter wordCounter = IntStream.range(0, sentence.length())
            .mapToObj(i -> sentence.charAt(i))
            .parallel()
            .reduce(new WordCounter(0, true),
                    (word, character) -> {
                        System.out.println("character:" + character);
                        return word.accumulate(character);
                    },
                    (word1, word2) -> word1.combine(word2));
    return wordCounter.getCounter();
    /* 
    character:a
    character:i
    character: 
    character: 
    character: 
    character:c
    */
}
```

이유는 간단히 `parallel` 로 시작되다 보니 어디서 누가 먼저 시작되는지 모르기 떄문이다.  

단어 중간, 공백 한가운데에서 시작될 수 있고 단어 중간에서 시작될 경우 한 단어를 2개 로 카운트해버린다.  

스레드가 여러개일 수록 단어를 여러 스레드가 쪼개서 가져가게 될것이고 모두 첫 카운트를 증가시킬 것이다.  

단어가 끝나는 위치에서 스레드를 분할시켜야 한다.  

`spliterator` 를 통해 구현해보자.  

```java
private static class WordCounterSpliterator implements Spliterator<Character> {

    private final String string;
    private int currentIndex = 0;

    private WordCounterSpliterator(String string) {
        this.string = string;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        // 소비한 문자 반환 및 진행여부 결정
        action.accept(string.charAt(currentIndex++));
        return currentIndex < string.length();
    }
    @Override
    public Spliterator<Character> trySplit() {
        int restSize = string.length() - currentIndex; // 현재 위치에서 나머지 크기 구함
        if (restSize < 10) return null; // 나머지가 일정크기 이하일 경우 split 하지 않음
        for (int splitPos = currentIndex + restSize / 2; splitPos < string.length(); splitPos++) {
            // 현재위치 + 나머지의 중간부문으로 이동 및 공백일 경우 split
            if (Character.isWhitespace(string.charAt(splitPos))) {
                Spliterator<Character> spliterator = new WordCounterSpliterator(string.substring(currentIndex, splitPos));
                currentIndex = splitPos;
                return spliterator;
            }
        }
        return null;
    }
    @Override
    public long estimateSize() {
        //탐색 요소 개수
        return string.length() - currentIndex;
    }
    @Override
    public int characteristics() {
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}

public static int countWordsSpliterator(String sentence) {
    WordCounterSpliterator spliterator = new WordCounterSpliterator(sentence);
    Stream<Character> stream = StreamSupport.stream(spliterator, true);
    WordCounter wordCounter = stream.reduce(new WordCounter(0, true),
            (word, character) -> word.accumulate(character),
            (word1, word2) -> word1.combine(word2));
    return wordCounter.counter;
}

public static void main(String[] args) {
    String sentence = "Nel   mezzo del cammin  di nostra  vita " +
            "mi  ritrovai in una  selva oscura" +
            " che la  dritta via era   smarrita ";
    System.out.println("Found " + countWordsSpliterator(sentence) + " words");
    // Found 19 words
}
```

## Optional

`Java` 에서 `NPE(NullPointerException)` 를 피하기위해 만들어진 구조  

`Optional` 은 `Serializable` 를 구현하지 않음으로 직렬화가 불가능하다.  
객체 필드로 사용하기 보단 아래처럼 메서드를 사용해 가져오는 것을 권장한다.  

```java
public class Person {
    private Car car;
    public Optional<Car> getCarAsOptional() {
        return Optional.ofNullable(car);
    }
}
```

`Map<String, Object>` 의 `value` 를 가져오기 위해 `get(String key)` 같은 `null` 위험성이 있는 메서드를 사용할때 `Optional` 를 사용하면 좋다.  

```java
Optional<Object> value = Optional.ofNullable(map.get("key"));
```

`Optional` 를 사용하면 좀더 좀더 직관적인 유틸리티 메서드를 만들 수 있다.  

```java
public static Optional<Integer> stringToInteger(String s) {
    try{
        return Optional.of(Integer.parseInt(s));
    } catch(Exception e) {
        return Optional.empty();
    }
}
```

### Optional 생성방법  

```java
Optional<Car> optCar1 = Optional.empty(); // 빈 Optional 객체 생성

Car car = new Car();
Optional<Car> optCar2 = Optional.of(car); // Optional 객체 생성, car가 null이라면 NPE 발생

Car nullCar = null;
Optional<Car> optCar2 = Optional.ofNullable(nullCar); // nullable Optional 객체 생성
```
`empty` `of` `ofNullable` 3가지 메서드로 생성 가능  

```java
Optional<Car> optCar1 = Optional.empty();
Car car = null;
Optional<Car> optCar2 = Optional.ofNullable(car);
System.out.println(optCar1 == optCar2); // true
```
`empty` 는 `Optional`의 `static` 메서드로 사전 정의된 싱글턴 객체를 반환  
`ofNullable` 또한 전달받은 `Object`가 `null`일경우 동일한 싱글턴 객체를 반환  

`==` 연산자로 참조위치까지 동일한 객체임을 알 수 있음.  

### Optional.map 

`stream` 의 `map` 메서드와 비슷, 요소의 개수가 하나인 컬렉션으로 생각할 수있다.  

```java
Optional<Car> optCar = Optional.empty();
Optional<String> carName = optCar.map(Car::getName);
```

반환값이 `Optional` 이며 요소(`Car`)가  `null`이라 해도 에러가 반환되지 않는다.  
이런식으로 `NPE` 지옥에서 빠져나갈 수 있다.  

객체 내부의 객체내부의 객체.... 의 요소 또한 이런식으로 가져올 수있을 것 같지만 `map` 이 아닌 `flatMap` 을 사용해야 한다.  
아래처럼 `Optional` 에 감싸진 `Optional` 을 가져오기 때문  

```java
Optional<Car> optCar = Optional.empty();
Optional<Optional<Engine>> carEngine = optCar.map(Car::getEngine);

Optional<String> carName = optCar
    .flatMap(Car::getEngine)
    .map(Engine::getName);
```


### Optional 언랩

Optional 내부의 요소를 가져오는 여러가지 메서드 
`get()` - 값이 없으면 `NoSuchElementException` 발생, 가장 간단하지만 `Optional` 객체 목적을 위반
`orElse(T other)` - 값이 없으면 대체할 기본값 제공.
`orElseGet(Supplier<? extends T> supplier)` - `orElse` 와 비슷한 메서드, 값이 없으면 `Supplier` 실행, 디폴트 메서드 생성에 리소스가 많이 필요하기에 꼭 필요한 경우에만 사용할 것.  
```java
Engine test = optCar.flatMap(Car::getEngine).orElseGet(() -> {
    Engine engine = new Engine();
    return engine;
});
```
`orElseThrow(Supplier<? extends T> exceptionSupplier)` - 값이 없으면 예외를 발생, 예외 객체를 `Supplier` 를 통해 지정가능.  
`ifPresent(Consumer<? super T> consumer)` - 값이 있을경우에만 `Consumer` 실행
`ifPresent(Consumer<? super T> action, Runnable emptyAction)` - 값이 있을경우 `Consumer` 실행, 없을경우 `Runnable` 실행  
