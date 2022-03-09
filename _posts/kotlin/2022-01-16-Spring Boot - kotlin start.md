---
title:  "Kotlin - start!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Kotlin

tags:
  - Kotlin

toc: true
toc_sticky: true

---


## 설치  

<https://kotlinlang.org/docs/command-line.html#install-the-compiler>

```sh
$ brew install kotlin
$ kotlin -version
Kotlin version 1.6.10-release-923 (JRE 11.0.10+8-jvmci-21.0-b06)
```

```sh
vi hello.kr
```

```kt
fun main() {
    println("Hello, World!")
}
```

```sh
$ kotlinc-jvm hello.kt
$ kotlin HelloKt
Hello, World!
```

`kotlinc-jvm` 대신에 `kotlinc` 사용 가능  

```sh
$ kotlincc hello.kt -include-runtime -d hello.jar
$ java -jar hello.jar
Hello, World!
```

```sh
$ kotlinc
Welcome to Kotlin version 1.6.10 (JRE 11.0.10+8-jvmci-21.0-b06)
Type :help for help, :quit for quit
>>> println("hello world")
hello world
>>> var name = "test"
>>> println("hello $name")
hello test
>>> :help
Available commands:
:help                   show this help
:quit                   exit the interpreter
:dump bytecode          dump classes to terminal
:load <file>            load script from specified file
>>> :quit
```

### kotlin 스크립트  

```kt
import java.time.*

val instant = Instant.now()
val southPole = instant.atZone(ZoneId.of("Antarctica/South_Pole"))
val dst = southPole.zone.rules.isDaylightSavings(instant)
println("It is ${southPole.toLocalTime()} (UTC${southPole.offset}) at the south Pole")
println("The south Pole ${if (dst) "is" else "is not"} on Daylight Savings Time")
```

위처럼 `main` 이 없는 코드를 나열하고 JVM 상에서 스크립트 형식으로 실행  

```sh
$ kotlinc -script southpole.kts
It is 20:50:43.486781 (UTC+13:00) at the south Pole
The south Pole is on Daylight Savings Time
```

## 코틀린 변수 var / val

타입 명시는 자유 단 초기화를 하지 않고, 변수를 생성하고 싶다면  
타입을 명시해줘야 한다.  

```kt
val num1 = 42
val num2: Int = 45
val num3: Int
```

`val`은 변경할 수 없는 속성(immutable)  
초기에 값을 할당되면 나중에 값을 변경할 수 없으며 값을 변경하게 되면 컴파일 에러가 발생  
Java에서는 `final`과 같다  
참조가 가리키는 객체의 내부 값은 변경가능  

`var`은 변경할 수 있는 속성(mutable)  
초기화 후 값을 변경이 가능하다.

코틀린은 기본적으로 필드에 `null` 사용이 불가능한데 `?` 키워드를 사용하면 허용한다.  

```kt
class Person(
    val first: String,
    val middle: String?,
    val last: String
)
```

`String?` 과 `String` 은 `null` 허용 여부만 결정하는 것 처럼 보이지만 아예 다른 기능의 변수처럼 동작한다.  


### smart cast

```kt
fun main() {
    val num1: String

    val p1 = Person("Joanne", null, "Rowling")

    if (p1.middle != null) {
        val middleNameLength = p1.middle.length
    }
}
```

if 문을 통해 `p1.middle` 이 `null` 이 아님을 확신할수 있기에 `String?` 이 아닌 `String` 타입으로 인식되며 `String` 에 정의된 여러 함수를 문제없이 호출 가능하다.  

### 단언 연산자 (널 연산자)  

이번엔 `val` 대신 `var` 로 선언한 `Person` 객체를 사용해보자.  

```kt
fun main() {

    var p2 = Person("North",  null, "West")
    if (p2.middle != null) {
        // val middleNameLength = p1.middle.length // 똑같은 코드이지 에러 발생
        val middleNameLength = p1.middle!!.length
    }
}
```

그대로 사용하면 에러가 발생하여 `!!` 키워드를 앞에 붙어야 한다.  

p2 의 내부 값은 언제든 변할수 있기에 아무리 `if` 문을 통해 확인했더라도 `String!` 타입에서 벗어날 수 없다.  

`!!` 키워드가 있다는 것은 `NullPointException` 이 발생할 수 있음(코드스멜)으로 사용하지 않기를 권장  

### 안전호출 연산자(?.), 엘비스 연산자(?:), 안전타입 변환연산자(as?)

`NullPointException` 를 위해 안전호출 연산자(`?.`), 엘비스 연산자(`?:`) 사용을 권장한다.  

```kt
fun main() {
    var p2 = Person("North",  null, "West")
    if (p2.middle != null) {
        val middleNameLength = p1.middle?.length ?: 0
    }
}
```

`middle?.` 은 `Int?` 를 반환하고 엘비스 연산자가 반환된 Int 값이 `null` 일 경우 0을 반환한다.  

```kt
val p = null
val p1 = p as? Person
```

변수 `p` 의 타입을 `Person` 으로 변환하지만 안전타입 변환연산자 `as?` 를 사용하기에 `Person?` 으로 변환된다.  
사실상 아래 코드와 같다.  

```kt
val p4:Person? = p as? Person
```

### infix, 확장함수

코틀린은 기본타입 `int`, `float`, `double` 등의 변수를 제공하지 않는다.  

코틀린에 Math 연산을 위한 객체가 있지만 부족한 부분이 있다.  

```kotlin
// 기본적으로 제공하는   Math 의 pow 함수, Double 타입만 제공
var d = Math.pow(2.0, 2.0)
println(d)
```

거듭제곱 매개변수로 `Double` 타입만 받으며 `Int`, `Float` 을 지원하지 않는데  
`infix` 연산자 함수를 정의하거나 확장함수를 추가로 정의하면 가능하다.  

```kotlin
// Int 타입에서 pow 하기위한 infix 메서드 생성
var a: Int = 2 `**` 2
println(a)

// 확장함수
var b = a.pow(2);
println(b)

...
...

infix fun Int.`**`(i: Int): Int {
    return toDouble().pow(i).toInt()
}

fun Int.pow(x: Int) = `**`(x)
```

일반 문자열을 `infix` 함수로 사용할땐 백탭 \` 을 쓰지 않아도 되지만 특수문자를 사용할땐 백탭으로 감싼다.  

### 비트연산  

코틀린에는 쉬프트연산 외에도 각종 비트 연산자를 제공한다.  

`and`, `or`, `xor`, `inv(not)`  

```kotlin
fun main() {
    var b1: Int = 0b0001_1111
    var b2: Int = 0b0001_0000
    println(b1 and b2)  // 16 
    println(b1 or b2)   // 31
    println(b1 xor b2)  // 15 
    println(b2.inv())   // -17
}
```

보수까지 전환된다.  

### map

```kotlin
val m = mapOf("a" to 1, "b" to 2, "c" to 3)
val p1: Pair<String, Int> = "a" to 1
val p2: Pair<String, Int> = Pair("b", 2)
val (k,v) = p2
```

코틀린에서 제공하는 각종 map 관련 함수

`"a" to 1` 또한 이미 내부적으로 `infix` 함수가 정의되어 있기 때문에 가능한 문법이다.  

```kotlin
infix fun <A, B> A.to(that: B): Pair<A, B> {
    return Pair(this, that)
}
```



## java 호환  

kotlin 과 java 호환을 위한 `@JvmOverloads` 어노테이션에 대해 알아보면  

```kt
class Person(
    val first: String,
    val middle: String?,
    val last: String
) {
    @JvmOverloads
    fun getDesc(first: String, age: Int = 0, desc: String? = null) =
        "first name $first, desc ${desc ?: "None"}, and age " + NumberFormat.getCurrencyInstance().format(age)
}
```

```kt
fun main() {
    val p1 = Person("hello", null, "world")
    p1.getDesc("test");
    p1.getDesc("test", 10);
    p1.getDesc("test", 10, "world");
}   
```

`@JvmOverloads` 어노테이션을 추가하면 컴파일 과정에서 java 에서도 kotlin 처럼 사용할 수 있도록 함수가 자동 셍성됨  

```java
public static void main(String[] args) {
    Person p = new Person("hello", null, "world");
    System.out.println(p.getDesc("hello"));
    System.out.println(p.getDesc("hello", 5));
    System.out.println(p.getDesc("hello", 5, "world"));
}
```
