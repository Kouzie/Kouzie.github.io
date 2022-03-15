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


## 개요

<https://kotlinlang.org/docs/command-line.html#install-the-compiler>

```sh
$ brew install kotlin
$ kotlin -version
Kotlin version 1.6.10-release-923 (JRE 11.0.10+8-jvmci-21.0-b06)
```

```sh
vi hello.kr
```

```kotlin
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

### kotlin 스크립트 + String Template

```kotlin
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

자바에선 사용할 수 없었던 `${}` 키워드 를 문자열 사이에 사용 가능하다.   

### when  

다른 언어에서 `switch` 에 해당하는 연산자  


```kotlin
fun checkName(score: Int) {
    when(score) {
        0 -> println("zero")
        1 -> println("one")
        2,3 -> println("two or three")
        else -> println("unknown")
    }
}
```

when 과 변수를 같이 사용할 수 있지만 이럴경우 반드시 else 가 필요함  

```kotlin
fun getName(score: Int): String {
    val result: String = when (score) {
        0 -> "zero"
        1 -> "one"
        2, 3 -> "two or three"
        else -> "unknown"
    }
    return result
}
```

```kotlin
fun getName(score: Int): String {
    val result: String = when (score) {
        in 0..10 -> "zero"
        in 10..90 -> "one"
        in 90..100 -> "two or three"
        else -> "unknown"
    }
    return result
}
```

### for  

```kotlin
val array = arrayOf(1, 2, 3)
for (i in array) {
    println(i)
}
val list = listOf("a", "b", "c", "d")
for ((index, name) in list.withIndex()) {
    println("${index}:${name}")
}
for (i in 1..10 step 2) {
    println(i)
}
for (i in 10 downTo 1) {
    println(i)
}
for (i in 1 until 10) { // 10 은 포함하지 않음
    println(i)
}
```

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

## 코틀린 변수 var / val


타입 명시는 자유 단 초기화를 하지 않고, 변수를 생성하고 싶다면 타입을 명시해줘야 한다.  

```kotlin
val num1 = 42
val num2: Int = 45
val num3: Int
```

`i vs variable`

`val`은 변경할 수 없는 속성(immutable)  
초기에 값을 할당되면 나중에 값을 변경할 수 없으며 값을 변경하게 되면 컴파일 에러가 발생  
Java에서는 `final`과 같다  
참조가 가리키는 객체의 내부 값은 변경가능  

`var`은 변경할 수 있는 속성(mutable)  
초기화 후 값을 변경이 가능하다.

코틀린은 기본적으로 필드에 `null` 사용이 불가능한데 `?` 키워드를 사용하면 허용한다.  

```kotlin
class Person(
    val first: String,
    val middle: String?,
    val last: String
)
```

`String?` 과 `String` 은 `null` 허용 여부만 결정하는 것 처럼 보이지만 아예 다른 기능의 변수처럼 동작한다.  


### smart cast

```kotlin
fun main() {
    val num1: String

    val p1 = Person("Joanne", null, "Rowling")

    if (p1.middle != null) {
        val middleNameLength = p1.middle.length
    }
}
```

if 문을 통해 `p1.middle` 이 `null` 이 아님을 확신할수 있기에 `String?` 이 아닌 `String` 타입으로 인식되며 `String` 에 정의된 여러 함수를 문제없이 호출 가능하다.  

### 단언 연산자 (널 연산자), nullable

이번엔 `val` 대신 `var` 로 선언한 `Person` 객체를 사용해보자.  

```kotlin
fun main() {

    var p1 = Person("North",  null, "West")
    if (p1.middle != null) {
        // val middleNameLength = p1.middle.length // 똑같은 코드이지 에러 발생
        val middleNameLength = p1.middle!!.length
    }
}
```

그대로 사용하면 에러가 발생하여 `!!` 키워드를 앞에 붙어야 한다.  

`var` 로 선언된 `p2` 의 내부 값은 언제든 변할수 있기에 아무리 `if` 문을 통해 확인했더라도 `String!` 타입에서 벗어날 수 없다.  

`!!` 키워드가 있다는 것은 `NullPointException` 이 발생할 수 있음(코드스멜)으로 사용하지 않기를 권장하며 아래의 안전호출 연산자(?.) 를 호출하는것을 권장한다.  

### 안전호출 연산자(?.), 엘비스 연산자(?:), 안전타입 변환연산자(as?)

`NullPointException` 를 위해 안전호출 연산자(`?.`), 엘비스 연산자(`?:`) 사용을 권장한다.  

```kotlin
fun main() {
    var p2 = Person("North",  null, "West")
    if (p2.middle != null) {
        val middleNameLength = p1.middle?.length ?: 0
    }
}
```

`middle?.` 은 `nullable`한 `Int?` 를 반환하고 엘비스 연산자가 반환된 `Int` 값이 `null` 일 경우 최종적으로 `0`을 반환한다.  

```kotlin
val p = null
val p1 = p as? Person
```

변수 `p` 의 타입을 `Person` 으로 변환하지만 안전타입 변환연산자 `as?` 를 사용하기에 `Person?` 으로 변환된다.  
사실상 아래 코드와 같다.  

```kotlin
val p4:Person? = p as? Person
```

### let  

`nullable` 상태의 변수를 다룰때 `let` 을 사용하여 `null` 이 아닐경우만 `let` 안의 람다형식의 함수를 실행, `it` 키워드를 사용함

```kotlin
val email : String ?="test@test.com"
email?.let {
    println(it)
}
```

보수까지 전환된다.  

## 클래스

kotlin 의 클래스는 java 와 다른부분이 상당있는데  

```kotlin
// 기본생성자 (primary constructor) constructor 키워드생략 가능
class Person (name: String) { 

    // 멤버변수
    private val firstProperty = "First property: $name"

    // 보조 생성자 (Secondary constructor)
    constructor(i: Int) : this(i.toString()) { 
        println("Secondary constructor block that prints $i")
    }

    // 생성자 초기화 블럭
    init { 
        println("First initializer block that pr-ints $name")
    }

    fun printName(): String = this.firstProperty
}
```

위의 처음보는 형식의 클래스 정의문을 하나씩 살펴보도록 한다.  

### 기본 생성자: Primary constructor

```kotlin
class Person constructor(name: String) {
    val name = name
    fun hello() {
        println("my name is ${name}")
    }
}
...
// 아래처럼 생략 가능  
class Person (val name: String) {
    fun hello() {
        println("my name is ${name}")
    }
}
```

기본생성자에 initial 데이터를 추가하면 자동으로 입력값에 맞는 생성자를 여러개 생성함.  

```kotlin
class Person (val name: String = "kozie") {
    fun hello() {
        println("my name is ${name}")
    }
}

fun main() {
    var human1 = Person("test")
    var human2 = Person()
    human1.hello() // my name is test
    human2.hello() // my name is kozie
}
```

### 보조 생성자: Secondary constructor

```kotlin
class Person(val name: String = "kozie") {

    constructor(name: String, age: Int) : this(name) {
        println("my name is ${name} and ${age} years old")
    }

    fun hello() {
        println("my name is ${name}")
    }
}
fun main() {
    var human = Person("test", 26) // my name is test and 26 years old
}
```

`c++` 의 생성자와 비슷하게 생겼는데 보조생성자는 항상 주 생성자로부터 데이터를 받아오는 형식으로 정의된다.  

### init 블럭

기본생성자만 사용하면서 특정 코드를 블럭 내부에서 실행하고 싶다면 `init` 블럭 사용  

```kotlin
class Person(val name: String = "kozie") {
    
    constructor(name: String, age: Int) : this(name) {
        println("my name is ${name} and ${age} years old")
    }
    
    init {
        println("i love eat cake")
    }

    fun hello() {
        println("my name is ${name}")
    }
}

fun main() {
    var human = Person("test", 26)
    // i love eat cake
    // my name is test and 26 years old
}
```

중요한점은 `init` 블록이 항상 먼저 실행된다는 점   

### companion object

static 메서드, 변수를 정의할때 사용  

```kotlin
interface IdProvider {
    fun getId(): Int
}

class Book private constructor(val id: Int, val name: String) {
    companion object BookFactory :IdProvider{
        val myBook = "DEFAULT_BOOK"
        override fun getId(): Int {
            return 444
        }
        fun create(): Book = Book(getId(), "animal farm")
    }

    override fun toString(): String {
        return "Book(id=$id, name='$name')"
    }
}

fun main() {
    val book = Book.create();
    println(Book.BookFactory.myBook) // DEFAULT_BOOK
    println(Book.myBook) // DEFAULT_BOOK
    println(book) // Book(id=444, name='animal farm')
}
```

`companion object` 도 클래스이기 때문에 이름을 지정하거나 상속 가능하다.  


### apply 블록


코틀린은 모든 것이 기본적으록 `public` 이다.  
객체지향의 은닉원칙을 

```kotlin
class Task(val name: String) {
    val priority = 3
}
```

`name` 은 생성자에서, `priority` 는 최상위 필드로 2개의 속성을 정의하였는데 
위와 같이 정의할 경우 `priority` 는 `apply` 블록을 통해 값을 지정할 수 있다.  

```kotlin
var myTask = Task("kouzie").apply { priority = 4 }
```

### data class

데이터를 담는 클래스로 사용, toString, hashCode, equals, copy 등 데이터 처리용 함수들이 자동생성됨  

```kotlin
data class Product constructor(
    val name: String,
    val price: Double = 0.0,
    val desc: String? = null
)
```
 
### object class

싱글톤 패턴을 사용하기 위한 클래스 타입  

```kotlin
object CarFactory {
    var cars = mutableListOf<Car>()
    fun makeCar(power: Int): Car {
        val car = Car(power)
        cars.add(car)
        return car
    }
}
```

단순히 `object` 키워드 하나를 사용하는 것만으로도 처음 컴파일될때 딱 한번 인스턴스화 되도록 강제할 수 있다.  


## 클래스 상속  

kotlin 에서 class 는 항상 final 로 정의된다.  

따라서 다음처럼 open 키워드로 상속 클래스임을 알려야 한다.  


## 컬렉션  

### Array vs List

`List` 는 내부 요소의 값을 변경 불가능한 컬렉션으로  
내부 요소를 변경하고싶다면 `ArrayList` 컬렉션을 사용  

`Array` 의 경우에는 선언 당시에 배열의 크기가 정해져 있으며 변경 불가능  

```kotlin
val array = arrayOf(1, 2, 3)
val list = listOf(1, 2, 3)
val anyArray = arrayOf("1", 2.0, 3)
array[0] = -1
// list[0] = -1 에러 발생, set 명령도 없음
var arrayList = arrayListOf(1, 2, 3)
arrayList[0] = -1
```

### map with Pair

```kotlin
val m = mapOf("a" to 1, "b" to 2, "c" to 3)
val p1: Pair<String, Int> = "a" to 1
val p2: Pair<String, Int> = Pair("b", 2)
val (a,b) = p1
println("${a}, ${b}") // a, 1
val (c,d) = p2
println("${c}, ${d}") // b, 2
```

코틀린에서 제공하는 각종 `map` 관련 함수

`"a" to 1` 또한 이미 내부적으로 `infix` 함수가 정의되어 있기 때문에 가능한 문법이다.  

```kotlin
infix fun <A, B> A.to(that: B): Pair<A, B> {
    return Pair(this, that)
}
```

## 함수 

```kotlin
fun add(a: Int, b: Int): Int {
    return a + b
}

fun add2(a: Int, b: Int): Int = a + b
```

kotlin 에서의 함수정의는 스펙타클하다.  


### 람다함수

자바에서 람다는 아래와 같이 사용하는데  

```java
BiConsumer<Integer, String> labmda = (Integer age, String name) -> {
    System.out.println("my name is " + name + "and my age is " + age);
};
```

함수형 인터페이스를 구현하는 형식으로 주로 람다함수를 사용한다.   


아래처럼 `입력Type`, `반환Type` 을 미리 작성하여 추론없이 바로 사용하거나  
`arg` 에 `입력Type`을 정의하고 반환값은 자동으로 추론되도록 한다.  

`val funName: (입력Type) -> (반환Type) = {arg -> body}`  
`val funName = {arg: 입력Type -> body}`  

개인적으로 2번 방법이 더 깔끔하고 가독성이 좋아보인다.  

```kotlin
fun main() {
    val square1: (Int) -> (Int) = { number -> number * number }
    val square2 = { number: Int -> number * number }
    println(square1(2)) // 4
    println(square2(2)) // 4
}
```

`square1` 함수는 생략없이 입력, 반환에 대한 `Type` 모두 정의
`square2` 는 입력만 `arg` 에 정의하여 반환에 대한 `Type` 에 자동 추론되도록 설정됨  

람다를 파라미터로 받을땐 아래와 같이 작성한다.  

```kotlin
fun main() {
    // 람다 변수 정의
    val lambda = { str: String -> str.length }
    var result1 = callLambda(lambda)
    println(result1) // 11

    // 리터럴 람다, 매개변수 타입이 람다함수 1개일 경우 사용 가능  
    val result2 = callLambda {
        it.length
    }
    println(result2) // 11
}


fun callLambda(lambda: (String) -> Int): Int {
    return lambda("hello world")
}
```

### 확장함수

확장함수를 정의할때 람다함수 형식을 사용해야 한든데 아래와 같다.  

```kotlin
fun main() {
    println("helloWorld!".addInteger1(1)) // helloWorld! extends String 1
    println("helloWorld!".addInteger2(2)) // helloWorld! extends String 2
}

val addInteger1: String.(Int) -> String = {
    "${this} extends String ${it}"
}

fun String.addInteger2(num: Int): String {
    return "${this} extends String ${num}"
}
```

람다함수 변수로 정의한 `String.(Int) -> String` 이부분은 `String` 에 대한 확장함수이며 매개변수로 `Int` 타입을 받고 `String` 값을 반환하겠다는 뜻이다.  
`fun` 키워드로 정의한 확장함수의 경우 람다함수가 아닌 일반적인 함수를 추가로 정의하는 문법, 만약 매개변수가 하나가 아닌 여러개라면 이방식을 사용하길 권장  

### infix

`c++` 의 연산자 오버로딩과 비슷한 역할을 하는 `infix` 키워드, 다음과 같은 상황에서 사용된다.  

코틀린은 기본타입 `int`, `float`, `double` 등의 변수를 제공하지 않는다.  

코틀린에 `Math` 연산을 위한 객체가 있지만 부족한 부분이 있다.  

```kotlin
// 기본적으로 제공하는   Math 의 pow 함수, Double 타입만 제공
var d = Math.pow(2.0, 2.0)
println(d)
```

거듭제곱 매개변수로 `Double` 타입만 받으며 `Int`, `Float` 을 지원하지 않는데  
`infix` 연산자 함수를 정의하거나 확장함수를 추가로 정의하면 가능하다.  

```kotlin
import kotlin.math.pow

infix fun Int.`**`(i: Int): Int {
    return toDouble().pow(i).toInt()
}

// 확장함수
fun Int.pow(x: Int) = `**`(x)

fun main() {
    var a: Int = 2 `**` 2
    println(a) // 4

    var b = a.pow(2);
    println(b) // 16
}
```

일반 문자열을 `infix` 함수로 사용할땐 백탭 \` 을 쓰지 않아도 되지만 특수문자를 사용할땐 백탭으로 감싼다.  

## java 호환  

kotlin 과 java 호환을 위한 `@JvmOverloads` 어노테이션에 대해 알아보면  

```kotlin
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

```kotlin
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
