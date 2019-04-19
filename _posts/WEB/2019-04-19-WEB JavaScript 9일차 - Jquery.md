---
title:  "Web - JavaScript 8일차 - Jquery!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - HTML

tags:
  - web
  - html
  - jquery
  - javascript

toc: true

---

## Jquery

jQuery는 빠르고 작으며 기능이 풍부한 **JavaScript 라이브러리**이다.  

다양한 브라우저에서 작동하는 사용하기 쉬운 API를 사용하면 
HTML 문서 탐색 및 조작, 이벤트 처리, 애니메이션 및 Ajax를 훨씬 간단하게 만들 수 있다.   
다양성과 확장 성을 결합한 jQuery는 수백만 명이 JavaScript를 작성하는 방식을 변경했다.  

> http://jquery.com/

### Jquery 사용하는법

> https://jquery.com/download/

위 사이트 Donwload에서 `.js`파일을 다운받아 `script`태그로 연결하거나  
CDN에서 호스팅 받으면 된다.  

`.min`이라는 문자열이 붙은 버전이 있는데 압축버전을 뜻한다.  

디버깅이나 Jquery코딩을 보고싶다면 `.min`이 붙지않은 파일을 사용하면 된다.  

### CDN 사이트

https://developers.google.com/speed/libraries/#jquery

```html
<head>
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
</head>
```

<br><br>

## Jquery 요소 선택

Javascript에서 요소선택할 때 다양한 `DOM`객체 메서드로 요소의 객체를 가져왔다. (`getElement...`, `querySelector...` 등)  

Jquery는 `$(  )` 연산을 통해 선택자를 매개변수로 넣고 객체를 가져올 수 있다.  

`$("선택자").함수명()`
css에서 사용하는 선택자를 집어넣는다. id는 `#`을 붙인다, class는 `.`, 태그명 등  
> https://kouzie.github.io/html/HTML-CSS-가상요소,-결합자,-속성선택자,-전역변수/#


html요소가 전부 로딩된후인 <body> 닫기바로 위에 script를 추가하였다.
body닫기 바로 위가 싫다면 window.onload 의 이벤트 헨들러 함수안에서 해야함
jquery로는 `$(document).ready( function(){ } )`

<br><br>

## Jquery 객체

항상 `HTML DOM`객체인 `document`객체를 통해 요소를 선택에 객체로 가져왔다.  

Jquery에서 `$(  )`연산을 통해 가져오는 객체는 `DOM`객체가 아닌 `Jquery`객체이다.  

### jQuery() 메서드


`$(  )` 굉장히 수상스럽게 생겼다.  

사실 위 연산자는 다음 연산자와 같은 역할을 한다. 
`jQuery(  )`  

`jQuery()` 함수는 매개변수로 `element`, `elementArray`, `object` 등의 `DOM`객체를 넣으면  Jquery객체로 포장하여 반환한다.  
혹은 css에서 사용했던 선택자를 넣어도 Jquery 객체로 반환한다.  

Jquery객체로 변경됨으로 Jquery에서 제공하는 많은 메서드를 편리하게 사용할 수 있다.  

보면 알겠지만 `$`는 `jQuery`의 **약자**이다.   

`jQuery` 역시 함수를 가리키는 변수명으로 `jquery-3.4.0.js`파일 안에 다음과 같이 코딩되어 있다.  

```js
jQuery = function( selector, context ) {
  // The jQuery object is actually just the init constructor 'enhanced'
  // Need init if jQuery is called (just allow error to be thrown if not included)
  return new jQuery.fn.init( selector, context );
},
```
```js
jQuery.fn.init = function( selector, context, root ) {
  ...
  ...
}
```
이런식으로 되어있다.  

> `$`기호는 다른 라이브러리에서도 많이 쓰이기때문에 `Jquery`객체를 못가져오는데 그 때 `jQuery()` 함수를 사용해야 한다. 혹은 약어를 `$`말고 다른 기호로 바꾸던가 해야함.



## Jquery 객체 생성

```js
$("div")
```

## Jquery 이벤트 핸들러

`$("button").click(function(){ })`
해도된다.

// jquery, ajax 이벤트 등록 on메서드를 사용
$("button").on("click", function () {
  /* if ($(this).html() == "hide") {
    $("p").hide();
    $(this).html("show");
  }
  else {
    $("p").show();
    $(this).html("hide");
  } */
  $("p").toggle()
});

## jQuery - css()

## jQuery - each()

javascript의 `each`처럼 Jquery의 `each`역시 각 배열의 모든 요소를 돌며 특정 행동을 한다.  

Jquery의 `each`는 첫번째 매개변수로 **배열 뿐 아니라 객체또한 넘길 수 있다.**  
객채의 각 속성을 돌며 속성명과 속성값을 가져올 수 있다.  

```js
var arr = ["one", "two", "three", "four", "five"];
var obj = { one: 1, two: 2, three: 3, four: 4, five: 5 };
    
$.each(arr, function (index, value) {
  console.log(index + ": " + value)
  // 0: one
  // 1: two
  // 2: three
  // 3: four
  // 4: five
});

$.each(obj, function (prop, value) {
  console.log(prop + ": " + value)
  // one: 1
  // two: 2
  // three: 3
  // four: 4
  // five: 5
});
```
