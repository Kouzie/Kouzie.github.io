---
title:  "java String, StringBuilder, Math클래스, 래퍼클래스!"
read_time: false
share: false
toc: true
author_profile: false

classes: wide
categories:
  - Java
tags:
  - Java
  - 문법
---

## String 클래스

`String`은 더이상 변할수 없는 **불변클래스**이다. (`immutable`클래스)  

이뜻은 한번 사용된 문자열은 변형, 추가될때 그자리에서 해당 문자열을 재활용 하는것이 아닌 **해당 문자열을 버리고 새로운 문자열을 만들어서 관리한다.**  
그래서 변하지 못하고 안쓰면 버림받는 불변클래스이다.  

`String`클래스 뒤에 `append`나 `+` 연산자로 문자열을 늘리는것은 **해당 문자열 참조를 끊고 더한 문자열을 메모리 공간에 다시 만들고 재참조하는 것이다.**

그래서 문자열이 계속 변할 가능성이 있다면 `StringBuilder`, `StringBuffer` 를 사용해야 한다.  

`StringBuilder`	- 동기화 처리가 되어있지 않아 스레드에 안전하지 않음   

`StringBuffer`	- 동기화 처리가 되어있어 스레드에 안전함   

버퍼를 사용해 버퍼크기까지 끝까지 채우지 않는 이상 문자열을 늘려도 버리지는 않는 녀석 정도로 알아놓자.  

`String`, `StringBuffer`, `StringBuilder` 이 세녀석 모두 `CharSequence` 라는 `Interface`를 구현하고있다.  

`CharSequence`의 추상 메서드중 `charAt(int index)`, `length()` 같은 메서드는 우리가 String다룰때 많이
써본 함수들이다.  

즉 `String`에 정의되어 있는 자주 사용되는 메서드는 `StringBuffer`, `StringBuilder`에도 있다.  

문자열을로 매개변수를 받는 함수들은 웬만해서 매개변수 `type`이 `CharSequence`로 되어있어서 
`CharSequence`를 구현하는 3개클래스 모두 매개변수로 들어갈 수 있다.

이젠 `String`클래스의 유용한 메서드에 대해 알아보자.  

-------------------------------------------------------------------------------------

### String의 toCharArray() - char배열과 String간의 형변환

```java
String msg = "hello world~";
char [] msgs = new char[msg.length()];	

for (int i = 0; i < msg.length() ; i++) {
	char one =  msg.charAt(i);
	System.out.println(one );
	msgs[i] = one;
}
```
`char`배열 `msgs`로 문자열 `msg`를 변환하는 과정이다.  

`for`문으로 복잡하게 사용하지말고 `toCharArray`메서드를 사용하자.  
위처럼 코딩하는게 효율적이다.   
```java
char [] msgs = msg.toCharArray();
```

`char`배열을 `String`으로 만들고 싶을 땐 아래와 같이 `valueOf`, `String`생성자 사용  
```java
String.valueOf(char[])
new String(char[])
```

-------------------------------------------------------------------------------------

### String의 split() 메서드 - 문자열을 나눠 문자열 배열로 반환!

```java
String name = "소지섭,이동석,김동현,차인표,장동건,강동원";
String regex = ",";
String[] names =name.split(regex);
Arrays.sort(names);
for (String str : strings) {
	System.out.print(str + ",");
}
```
출력
```
소지섭,이동석,김동현,차인표,장동건,강동원,
```

`split`함수는 정규화 과정을 통해 `String`을 `String[]`로 반환한다. 

`split`은 `regex`와 `int`가 함께올수 있는데 `int`는 몇 조각으로 자를지 정하는 정수.  

`String[] strings =names.split(regex, 2);` 처럼 정수가 올 수 있다.  
2조각으로 나뉨.

> 참고: `StringTokenizer` 클래스, 구분자로 토큰화할 수 있다.  
```java
String name = "소지섭,이동석,김동현,차인표,장동건,강동원";
StringTokenizer st = new StringTokenizer(name, ",");
while(st.hasMoreTokens())
{
	String token = st.nextToken();
	System.out.print(token);
}
```
출력
```
소지섭이동석김동현차인표장동건강동원
```
문자열 잘라쓰는일이 많다보니 쓰기 편하라고 `jdk 5.0`에서 만들어준 클래스  
하지만 그냥 `String`배열과 `split`사용하는게 대부분이다.  
유지보수를 위해 쓰던거 쓰자...  

-------------------------------------------------------------------------------------

### String의 join() 메서드 - 문자열들을 구분자로 합치기

문자열 합칠때에는 `join`함수를 사용하면 편하다.  
`join`메서드 원형
```java
public static String join(CharSequence delimiter,
	CharSequence... elements)
```

`join`보면 구분자와 문자열배열을 주면 되는데 매개변수로 위에서 말했던 `CharSequence`를 사용중이다.  

`delimiter`에 `String`뿐만 아니라 `StringBuffer`등 `CharSequence`를 구현하는 모든 클래스들을 사용할수 있다.  

```java
String  data = "[57 27 53 66 48 97 22 23 74 20]";
String [] datas = data.split(" ");
System.out.println( String.join("][", datas) ); 
```

출력값
```
[57][27][53][66][48][97][22][23][74][20]
```


`join`메서드 보다 합치는데 좀더 많은 기능을 하는 `StringJoiner`라는 클래스가 있다.
```java
String[] names = {"홍길동", "홍길동", "홍길동", "홍길동", "홍길동"};
StringJoiner sj = new StringJoiner("/", "[", "]");
for (String str : names) {
	sj.add(str);
}
System.out.println(sj);
```
출력값
```
[홍길동/홍길동/홍길동/홍길동/홍길동]
```
이걸 사용하면 html에서 반복적으로 나오는 `<li>`태그같은 녀석들을 붙여넣을수 있다.  

-------------------------------------------------------------------------------------

### String의 substring() 메서드 - 문자열을 자르자.

```java
String rrn = "123456-1234567";
System.out.println(rrn.substring(0, 6));
System.out.println(rrn.substring(7));
```
`substring`에 `int`형 하나만 들어가면 거기서부터 끝까지 읽어온다.

-------------------------------------------------------------------------------------

### String의 compareTo() 메서드 - 문자열을 비교하자.

문자열 비교는 `equals()` 메서드도 있지만 `compareTo()`메서드는 단순 값이 같은지 비교하는것 보다
좀더 얻을 수 있는 정보가 많다.  

```java
String msg = "hello world";
System.out.println(msg.compareTo("hello worD"));
```
같으면 0반환, 다르면 다른 정수 반환

`A.compareTo(B)`
* A와 B가 같으면 0을 반환  
* A가 B보다 크면 양수를 반환  
* A가 B보다 작으면 음수를 반환  

맨 첫자리부터 차례대로 비교하는 특성이 있음   

-------------------------------------------------------------------------------------

### String의 concat() 메서드 - 문자열 끝에 또다른 문자열을 붙이자.

```java
String msg = "hello world";
System.out.println(msg.concat(" Fisrt"));
``` 
출력값
```
hello world Fisrt
```
`concat`은 연결된 문자열을 반환한다.  

하지만 `String + String` 로 `+`연산자 쓰는게 더 나은듯 하다. (가독성 성능 부분에서)

-------------------------------------------------------------------------------------

### String의 contains() 메서드 - 문자열안에 해당 문자열이 있는지 검사하자.

```java
String msg = "hello world";
System.out.println(msg.contains("llo"));
```
`true` 출력  

`contain`메서드 원형 `public boolean contains(CharSequence s)`  
문자열 갖고있으면 `true` 없으면 `false` 반환를 반환하는 `CharSequence`를 매개변수로 갖는 메서드.  


-------------------------------------------------------------------------------------

### String의 indexOf() 메서드 - 문자열안에 해당 문자열이 어디에 있는지 알아보자.


```java
String msg = "hello world";
System.out.println(msg.indexOf('w'));
System.out.println(msg.indexOf("wo"));
```
출력값
```
6
9
```

문자열 속에 `char`(문자) 혹은 `String`(문자열)이 있으면 해당 위치(`index`)를 정수값으로 반환한다.  

만약 해당 문자나 문자열이 없다면 -1 반환.  

`contains` 메서드로 문자열이 있는지 검사할 수 도 있겠지만 `indexOf`를 사용해서도 문자열이 있는지
검사할 수 있다.(없다면 -1 반환)  

`indexOf`에 오버로딩으로 정의되어 있는   
`public int indexOf(int ch, int fromIndex)` 방식 사용하기


```java
String fileName = "Sam.ple.txt";
int idx = fileName.indexOf(".", 4);
```
4번째 위치인 `p`에서 부터 점을 찾기 시작한다.  
출력값
```
7
```


-------------------------------------------------------------------------------------

### String의 lastIndexOf() 메서드 - 문자열안에 해당 문자열이 끝에서 어디에 있는지 알아보자.

`indexOf`메서드는 앞에서 어디에 위치한지 찾았다면  
`lastIndexOf`는 뒤에서부터 해당 문자(열)를 찾아 어디에 위치한지 찾는다.  

```java
String fileName = "Sam.ple.txt";
int idx = fileName.lastIndexOf("."); //마지막 점 위치를 정수로 반환
```
출력값
```
7
```

-------------------------------------------------------------------------------------

### String의 replace() 메서드 - 찾은 문자열 수정하기.

```java
String msg = "hello world";
System.out.println(msg.replace('o', 'x'));
```
`h`를 `x`로 수정   

출력값
```
hellx wxrld
```

`hello`를 `xxx`로 수정  

```java
String msg = "hello world";
System.out.println(msg.replace("hello", "xxx"));
```
출력값
```
xxx world
```
`char`와 `String`모두 매개변수로 전달 가능하다.  


`replaceAll`과 `replaceFirst`도 있는데 정규표현식 패턴으로도 바꿀수 있다.  
```java
public String replaceAll(String regex, String replacement)
public String replaceFirst(String regex, String replacement) 
```

-------------------------------------------------------------------------------------

### String의 startsWith() 메서드 - 해당 문자열로 시작하는지 검사하기.

```java
String url = "http://www.naver.com";
System.out.println(url.startsWith("http://"));
```
`true` 출력   
 
`public boolean startsWith(String prefix, int toffset)`   
인덱스를 주어서 `offset`부터 `prefix`로 시작하는지 검사할 수 도 있다.  

-------------------------------------------------------------------------------------

### String의 endWith() 메서드 - 해당 문자열로 끝나는지 검사하기.

```java
String directory = "C:\\Class\\javaclass\\javaPro\\src\\days02";
```

뒤에 `\`가 안붙어있는데. 뒤에 붙어있는지 안붙어있는지 검사하는 방법은 여러가지다.  

1. `directory.charAt(directroy.length() - 1) == '\\';`  
2. `directory.lastIndexOf('\\') == directory.length() -1;`  

하지만 `endWith`라는 메서드를 사용하면 간단하게 검사할 수 있다.  
`directory.endsWith("\\")`

-------------------------------------------------------------------------------------

### String의 trim() 메서드 - 앞뒤 공백 제거하기

```java
String name = "   소지섭     ";
System.out.println(name.trim());
```
출력값
```
소지섭
```
공백을 모두 없애준다.

-------------------------------------------------------------------------------------

### String이 null인지 공백인지 구분하기 - isEmpty, isNull

```java
String msg = "";
System.out.println(msg.length());
if(msg.isEmpty())
{
	System.out.println("공백입니다.");
}
if(Objects.isNull(msg))
{
	System.out.println("NULL 입니다.");
} 
```

### 자바의 인코딩 변환

자바 문자 인코딩은 `utf-16`을 사용한다.  

이걸 `uft-8`로 억지로 바꾸려면 `getBytes()` 메서드를 사용하면 된다.  
```java
byte[] utf_8 = "자".getBytes("UTF-8"); //한글은 3byte, 영어는 1byte
System.out.println(Arrays.toString(utf_8));
```

출력값
```
[-20, -98, -112]
```

`utf-8`로 저장된 배열을 다시 `String`으로 바꾸고 싶다면 그냥 `new String()`생성자에 인코딩 `type`지정.  
```java
String convert = new String(utf_8, "UTF-8");
System.out.println(convert);
```
"가" 출력된다!!  

-------------------------------------------------------------------------------------

### 정수 &rarr; 문자열,   문자열 &rarr; 정수

1. 정수&rarr;문자열  
`String istr = String.valueOf(10);`  

2. 문자열&rarr;정수  
`int n = Integer.valueOf(istr);`  
Integer 래퍼클래스를 반환

-------------------------------------------------------------------------------------

### 파일 복제본있으면 이름 바꿔서 저장하기

```java
String fileName = "Sam.ple.txt";
int idx = fileName.lastIndexOf("."); //마지막 점 위치를 정수로 반환
String name = fileName.substring(0, idx); //파일 이름
String ext = fileName.substring(idx);  //파일 확장자 .txt 반환
int idx = 1;
String copyFileName = String.format("%s - 복사본(%d)%s", name, idx++, ext);
System.out.println(copyFileName);
```

-------------------------------------------------------------------------------------

### StringBuilder

`StringBuffer`와 `StringBuilder`차이.

* `StringBuffer`는 동기화처리 되있음   
* `StringBuilder`는 동기화처리 안되있음   
		
`String`은 불변클래스라 데이터를 변경하려면 새로 생성하고 재참조해야 하지만 `StringBuilder`와 `StringBuffer`는 데이터를 변경할 수 있다.  
(`Builder`와 `Buffer`는 **별도의 메모리 공간을 `list`처럼 할당해놓고** 거기서 데이터를 변경한다)  


그럼 뒤에 기존 할당된 문자열 뒤에 java라는 문자열을 붙여보자.
```java
StringBuilder msg_b = new StringBuilder("hello world");
msg_b.append(" java ");
```

`append`메서드엔 매개변수로 모든 기본자료형과 `char`배열, `Object`클래스도 들어갈 수 있다.  
```java
char[] chs = {'k', 'o', 'g', 'y'};
msg_b.append(chs, 1, 2); //1번째 인덱스에서 2길이만큼 붙여넣기
System.out.println(msg_b);
```
출력값
```
hello world java og
```

따라서 문자열 가공할 일이 있으면 `StringBuilder`로 가공한 후에 `String`으로 변환해서 주자.  

훨씬 효율이 좋아진다. (`String은` 가공할때마다 새로 만들어야하니까)  

`StringBuilder`의 초기 저장 용량은 16byte이다, 추가할때마다 자동으로 증가한다.  
자동 증가할땐 `list`형식으로 뒤에 추가되는듯 하다.  

-------------------------------------------------------------------------------------

## StringBuilder의 유용한 메서드들

### StringBuilder의 insert()메서드 - 문자열 사이에 문자열을 삽입.


앞의 공백에 `,`를 삽입해보자.
```java
StringBuilder msg_b = new StringBuilder("hello world java og");
int offset = msg_b.indexOf(" ");
msg_b.insert(offset, ",");
System.out.println(msg_b);
```

출력값
```
hello, world java og
```

-------------------------------------------------------------------------------------

### StringBuilder의 delete()메서드 - 해당 문자열을 삭제.

만약 `String`클래스에서 일정 문자열만 삭제하고 싶다면 어떻게 해야할까?   

삭제는 아니지만 비슷한 효과를 가진 `replace`를 사용하면 된다.  
```java
String first = "Hello World Java";
System.out.println(first.replace("World", ""));
```
`Hello  Java` 출력  

하지만 `World`라는 문자열이 여러개 있는데 그중 하나만 삭제하고 싶다면?  

`world` 시작 `index`를 구해서 3개로 문자열을 잘라서 삭제하고 합쳐야 할것이다.  


하지만 `StringBuilder`에는 고유의 `delete`라는 삭제하는 함수가 있다.  

```public StringBuilder delete(int start, int end)```  
시작 `index`부터 끝 `index`까지 삭제시킨다.  

                            
```java
StringBuilder msg_b = new StringBuilder(hello, world java);
System.out.println(msg_b);
msg_b.delete(msg_b.indexOf("world"), msg_b.indexOf("world")+"world".length());
System.out.println(msg_b);
```
출력값
```
hello, world java
hello,  java
```

-------------------------------------------------------------------------------------

### 기타 함수들....

```java
StringBuilder msg_b = new StringBuilder(hello, world java og);
System.out.println(msg_b.codePointAt(1)); //index 번째 문자를 읽어 정수(코드)로 반환
```

출력값
```
101
```
`'e'`의 아스키 코드값인 101이 출력된다.  

### 거꾸로 출력하기 - `reverse()`메서드
```java
msg_b.reverse();
System.out.println(msg_b);
```
출력값
```
avaj  ,olleh
```

-------------------------------------

### StringBuilder와 StringBuffer간 문자열 비교

`StringBuilder`나 `StringBuffer`는 비교함수 `equals`함수로 비교할때 무조건 `String`으로 변환후 비교하자  

`StringBuilder`를 `String`으로 변환
```java
String msg = msg_b.toString();
String msg = new String(msg_b);
```

--------------------------------------------------------

## 래퍼클래스

**기본 자료형을 객체지향적으로 더 쉽고 편하게 사용하기 위해 만들어진 클래스**

래퍼클래스를 통한 문자열과 기본 자료형 간의 변환
```java
String n = "100";
int i = Integer.parseInt(n);
byte b = Byte.parseByte(n);
short s = Short.parseShort(n);
long l = Long.parseLong(n);
float f = Float.parseFloat(n);
double d = Double.parseDouble(n);
```


`Integer integer = Integer.valueOf(n);`  
반환값이 `Integer` 래퍼클래스  

`int i = Integer.valueOf(n).intValue();`  
`Integer`를 `int`형으로 받고싶다면 `intValue()`메서드  
`intValue()`는 반환값이 `int`형  

하지만 `Integer`를 비롯한 래퍼클래스는 기본자료형으로 `valueOf()`같은 메서드 필요 없이 바로 초기화 가능하다.  
```java
int i = 10;
Integer rapperInt = i;
long l = 1L;
Long rapperLong = l;
```

자동형변환이 되는 이유는 `Boxing`과 `Unboxing` 때문이다.

기본형 &rarr; 래퍼: `Boxing`  
래퍼 &rarr; 기본형: `Unboxing`  

둘다 캐스팅 연산이 필요없이 자동으로 변환되기 때문에 앞에 `auto`가 붙는다.
> `Auto Boxing`, `Auto Unboxing`

```java
public static void disp(Object o)
{
	System.out.println(o);
}

public static void main(String[] args){
	disp(100);
}
```
출력값
```
100
```
`int` 자료형 `100`을 `Object` 매개변수로 넘겨서 바로 출력한다.  

안될것 같지만 된다!  
원리는 아래와 같다.  

`100`이 바로 `Object`로 변환되는 것이 아니라 몇 단계를 거쳐서 `Object`로 변환된다.  

`100` &rarr; `Integer` &rarr; `Object`  
`auto Boxing` &rarr; `DownCasting` 을 통해 `100`이 출력된다.....  

솔직히 `Object`에서 `Integer`로 `DownCasting`될때 왜 캐스팅연산을 안해도 되는지 모르겠지만  
래퍼클래스의 특징이라 생각하고 넘어가자.  


### Integer를 사용한 진법 변환
```java
i = Integer.parseInt("100", 2);
System.out.println(i);  
```
출력값
```
4
```
`"100"`이 10진수 `100`이 아니라 가 아니라 2진수 `100(2)`으로 인식한다.

	
`radix`에 다른 값을 넣어 여러 진법을 출력해보자!
```java
System.out.println(Integer.parseInt("110", 2));
System.out.println(Integer.parseInt("FF", 16));
System.out.println(Integer.parseInt("1F", 16));
```
출력값
```
6
255
31
```

만약 `int n = 100;` 정수형 변수가 주어졌을때 해당 정수를 여러 진법으로 출력하기
```java
System.out.println(Integer.parseInt(Integer.toString(n), 2));
System.out.println(Integer.parseInt(Integer.toString(n), 8));
System.out.println(Integer.parseInt(Integer.toString(n), 16));
```

출력값
```
4
64
256
```

위처럼 억지로 문자열로 바꿔서 출력할 수 도 있지만 문자열 format을 쓰면 더 간단하게 해결 가능하다.
```java
System.out.println(String.format("%#X", 10));  // 16진수로 출력
System.out.println(String.format("%#o", 10));  // 8진수로 출력
```

출력
```
0XA
012
```
> 아쉽게도 2진수는 없음....


만약 정수 `n`이 2진수로 저장되어 있을때 16진수로 바꾸어 출력하기
```java
n = 1010;  //10진수로 10, 16진수로 a가 출력되어야 한다.
System.out.println(Integer.toHexString(Integer.valueOf(n+"",2))); 
```

출력값
```
a
```

----



## Math클래스

`Math`의 산술연산에서 오류가 발생할때 예외를 발생시키는 메서드.  
`jdk 1.8`부터 추가되었음.  

```java
int addExact(int x, int y)
int subtracExact(int x, int y)
int mulitplyExact(int x, int y)
int incrementExact(int a)
int decrementExact(int a)
int negateExact(int a)
int toIntExact(long value)
```

```java
int i = Integer.MAX_VALUE;
int j = 100;
System.out.println(i+j); //-2147483549 출력
```

`int`범위를 넘어간 산술오버플러우 에러이다.  

하지만 예외를 발생시키지 않고 예상치 못한값이 그냥 출력되기 때문에  
예외를 발생시켜 혹시 모를 상황에 대비할 수 있어야 한다.  


상황이 발생할수도 있다고 예측할 수 있다면 `Math`의 `addExact`를 사용하자  
`addExact`가 알아서 범위를 검사해서 예외를 던져준다.  
```java
int k = Math.addExact(i, j);
```
출력
```
Exception in thread "main" java.lang.ArithmeticException: integer overflow
```


뺄샘도 마찬가지다.
```java
int i = Integer.MIN_VALUE;
int j = 100;
System.out.println(i-j); //2147483548출력
``` 

만약 `int`형이 표시할수 있는 음수 범위를 넘어갈것 같다면 `subtractExact`를 사용하자.
```java
int k = Math.subtractExact(i, j);
```
출력값
```
Exception in thread "main" java.lang.ArithmeticException: integer overflow
```

단항연산자에서 사용할 수 있는 메서드도 있음 &rarr; `Math.decrementExact(i--);`  

만약 더 큰 정수, 더큰 실수 범위를 표현하고 싶다면 자료형을 바꿔야한다.
`long`혹은 `BigInteger`(래퍼클래스)으로바꾸자!!

### `Math` - 올림, 반올림, 버림  

`Math` 메서드중 올림, 반올림, 버림 메서드가 자주 사용된다.
```java
double val = 90.7552;
System.out.println(Math.round(val)); 
```
출력값
```
91
```
무조건 **소수 첫째자리에서 반올림**하고 반환형은 `long`형이다.  

만약 3번째 자리에서 반올림 하려면?  
아래와 같이 먼저 100을 곱하고 반올림한 후에 100을 나누면 된다.  
```java
double val = 90.7552;
System.out.println((double)Math.round(val*100)/100);
```
출력값
```
90.76
```

`round()`의 경우 반환형이 `long`형이기 때문에 억지로 형변환 해주어야 한다.

반환형이 `double`형인 반올림 함수도 있다. &rarr; `rint()` 메서드  
```java
double val = 90.7552;
System.out.println(Math.rint(val));
```
출력값
```
91.0
```

3자리수 소수점에서 반올림하기  
```java
double val = 90.7552;
System.out.println(Math.rint(val*100)/100);
```

음수값 반올림
```java
val = -1.5;
System.out.println(Math.rint(val));
```
출력값
```
-2.0
```

**올림 함수와 버림함수 - `ceil`, `floor`**

```java
//올림함수
val = 90.7552;
System.out.println(Math.ceil(val)); //91.0
//버림함수
System.out.println(Math.floor(val)); //90.0
//모두 double형을 반환한다.
```
