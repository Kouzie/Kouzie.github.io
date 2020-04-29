
title:  "java String format!"
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


### String format

`java.util`패키지의 `Class Formatter`클래스의 `string`관련 내용에서 관련 내용을 찾을 수 있다.  

```
%[argument_index$][flags][width][.precision]conversion
```  

`[ ]`는 다 생략 가능한것들 생략하고 나면 `%conversion` 하나 남는다  
`%s`, `%d`와 같은 형식을 `conversion`라 볼수 있다.   

`[width]` 옵션을 사용해보자  

```java
int age=20;
System.out.println("[width]도 붙여보자");
System.out.printf("[%10s], [%10d]\n",name, age);
//생략하지 말고 써보자 [width]속성에 10 적용
```

결과값  

```
[width]도 붙여보자
[       홀길동], [        20]
```

`[flag]`옵션을 사용해 보자 

```java
System.out.println("[flags]도 붙여보자");
System.out.printf("[%10s], [%#10o]\n",name, age); //10칸 띄고 8진수로 age표시
System.out.printf("[%10s], [%(10d], [%(10d]\n",name, 1234, -1234); 
//음수의 경우 ()안에 표시

System.out.printf("[%10s], [%,10d]\n",name, 123123123); 
//3자리 마다 ,표시 (통화 표기시 유용)

System.out.printf("[%10s], [%010d]\n",name, age); //width만큼 0표시
```

결과값  
```
[flags]도 붙여보자
[       홀길동], [       024]
[       홀길동], [      1234], [    (1234)]
[       홀길동], [123,123,123]
[       홀길동], [0000000020]
```



`[argument_index$]`옵션을 사용해 보자  
```java
System.out.println("[argument_index$]도 붙여보자");
System.out.printf("[%1$10s], [%2$#10o], [%2$#10o]\n",name, age);
//2$가 두번째 인자인 age를 가리킨다.
```

결과값  
```
[argument_index$]도 붙여보자
[       홀길동], [       024], [       024]
```
String format 문서를 잘 찾아보면 따로 문자열을 변환하지 않아도  
사용자가 원하는대로 화면에 문자를 변경해서 출력할 수 있다.  

> https://micropai.tistory.com/48 좋은 설명...
