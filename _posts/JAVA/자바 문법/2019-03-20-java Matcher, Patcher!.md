---
title:  "java Matcher, Pattern!"
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

## Pathcer, Matcher

java.util.regex에 포함된 클래스로 정규식에 사용되는 클래스이다.  
`Pattern`은 정규식을 정의하는데 사용되고    
`Matcher`는 정규식을 데이터와 비교하는 역학을 한다.  



```java
Pattern p = Pattern.complie("c[a-z]*");

Marcher m = p.matcher(compStr);

if(m.matched())
...
```

사용법은 간단하다.  

`Pattern`의 `complile` 메서드를 사용해서 정규식을 정의하고 `matcher` 메서드를 사용해서 정규패턴을 검사할 `Matcher` 객체를 만들면서 검사할 문자열을 전달한다.   
만들어진 `Matcher`클래스는 `matched`함수를 호출하면서 패턴과 문자열이 일치하는지 `true`, `false`로 반환한다.  

