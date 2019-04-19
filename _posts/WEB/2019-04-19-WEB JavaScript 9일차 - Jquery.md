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

```js
var a = {
  name: "hong",
  age: 20
};
var b = $("body");


alert(a.jquery); //undefined
alert(b.jquery); //3.3.1
```
`a`객체는 javascript도 만든 `Object`이고 `b`객체는 `$(  )`메서드를 사용한 `jquery객체`이다.

해당 객체가 `jquery`객체인지 아닌지 판단하려면 객체안에 멤버로 `jquery`변수가 있는지 확인하면 된다.  


### $, jQuery() 메서드


`$(  )` 굉장히 수상스럽게 생겼다.  

사실 위 연산자는 다음 연산자와 같은 역할을 한다. 
`jQuery(  )`  

`jQuery()` 함수는 매개변수로 `element`, `elementArray`, `object` 등의 `DOM`객체를 넣으면  Jquery객체로 포장하여 반환한다.  
css에서 사용했던 선택자 문자열로 넣어도 Jquery 객체로 반환한다.  

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

`$(  )` 메서드를 사용해 객체를 생성할 수 있다.  
```js
$("<div>test</div>");
$("<div>");

$("p").html("<span style='color:red'>span tag</span>");
```
`html()`메서드는 `DOM`객체의 `innerHTML`속성 역할을 하는 함수.  

>innerText속성 역학을 하는 text() 메서드도 있다.  


## Jquery 이벤트 핸들러

`on()`메서드 사용하거나 `이벤트명()`메서드를 사용하는 방법이 있다.  

`$("button").click( function(){ ... } )`  
`$("button").on( "click", function (){ ... } )`  

jquery, ajax 이벤트 등록 on메서드를 사용해야 함으로 기억해두자....

## jQuery - remove(), empty(), filter()

### remove()

`remove()`메서드는 자주 사용되다 보니 매개변수로 여러가지 종류가 들어간다.  

`$("p").remove();` - 모든 p 태그 삭제  
`$("p").remove(":contains('hello')");` - content에 `hello`문자열이 포함되는 것만 삭제

`$("p").remove(".hello");` - p태그중 `class명`이 `hello`인것 제거  
`$("p.hello").remove();` - p태그중 `class명`이 `hello`인것 제거  

HTML 요소 자체를 삭제해 버린다.

### empty()

요소 자체는 삭제하지 않고 요소안의 자식 노드들을 모두 삭제하고 싶을 때 `empty()`메서드를 사용한다.

`$("p.hello").empty();`  
p태그중 클래스가 hello인것의 자식노드를 제거


### filter()

`filter()`메서드는 Jquery객체 배열에서 괄호 안의 선택자에 해당하는 요소만 반환하는 메서드이다.  

`$("p").filter(":contains('hello')").remove();` - 모든 `p`태그중 `content`가 `hello`문자열이 포함되는 태그만 `filter`해서 제거  

`filter()`메서드의 매개변수로 함수로 들어갈 수 있는데 `true`반환하는 요소를 반환한다.  
```js
$( "li" )
  .filter(function( index ) {
    return index / 3 === 0;
  })
    .css( "background-color", "red" );
```
`li`태그들중 3의 배수번째 `li`만 배경색을 변경한다.  

<br><br>

## jQuery - wrap(), unwrap()

메서드명처럼 태그를 감싸는 태그를 추가하거나 제거하는 메서드.  

```html
<button>wrap/unwrap</button>
<p>Hello</p>
<p>cruel</p>
<p>World</p>
```

```js
$("button").click(function () {
  if ($( "p" ).parent().is("div")) {
    $( "p" ).unwrap();
  } else {
    $( "p" ).wrap("<div class='wrap'></div>");
  }
});
```
div태그로 감싸여져 있다면 제거(`unwrap`)하고 그렇지 않다면 div태그로 감싼다(`wrap`).

## jQuery - show(), hide(), toggle()

기존에 HTML 요소를 보이지 않게 설정하려면 `display`속성에 속성값으로 `"none"`을 줘야 했다.   '
반대로 다시 보이게 설정하려면 `"block"`속성값을 줘야 했다.  

Jquery에선 `show()`, `hide()`메서드를 통해 쉽게 숨기기, 표시하기 할 수 있다.(물론 내부적으론 display속성값을 건들인다.)

### show(), hide()

```html
<button>hide</button>
<p>test</p>
```
```js
$("button").on("click", function () {
  if ($(this).html() == "hide") {
    $("p").hide();
    $(this).html("show");
  }
  else {
    $("p").show();
    $(this).html("hide");
  }
});
```
버튼을 클릭하면 숨기고, 다시 클릭하면 보여주는 이벤트 핸들러,  

`hide()`와 `show()` 메서드 매개변수로 사라지게하는 속도, 사라지고 호출되는 callback함수를 등록할 수 있다. 아무것도 등록하지 않으면 바로 사라졌다 바로 나타난다.  

```html
<button id="hider">Hide</button>
<button id="shower">Show</button>
<div>
  <span>Once</span> <span>upon</span> <span>a</span>
  <span>time</span> <span>there</span> <span>were</span>
  <span>three</span> <span>programmers...</span>
</div>
```

```js
$("#hider").click(function () {
  $("span:last-child").hide("fast", function () {
    $(this).prev().hide("slow", arguments.callee);
  });
});
$("#shower").click(function () {
  $("span").show(2000);
});
```
`fast`는  `200ms`, `slow`는 `600ms`속도이다.  
정수로 넘겨줘도 상관 없다.  

### toggle 
```js
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
```

그냥 사라졌다 나타나게 하려면 `if..else`문 줄 필요없이 `toggle()`메서드를 사용하면 편하다.  
내부적으로 번갈아 가면서 `hide()`, `show()`메서드를 호출한다.

## jQuery - $.each()

javascript의 `each`처럼 Jquery의 `each`역시 각 배열의 모든 요소를 돌며 특정 행동을 한다.  

Jquery의 `each`는 첫번째 매개변수로 **배열 뿐 아니라 객체또한 넘길 수 있다.**  
객채의 각 속성을 돌며 속성명과 속성값을 가져올 수 있다.  

여기서 `$.`은 `jQuery`객체의 멤버라 전역변수(함수)처럼 사용할 수 있다는 뜻.
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


`each()`메서드를 도중에 중단 시키고 싶다면 조건문으로 `false`를 반환한다.  
```html
<ul>
  <li>item1</li>
  <li>item2</li>
  <li>item3</li>
  <li>item4</li>
  <li>item5</li>
</ul>
```
```js
$("ul>li").each(function (index) {
  console.log(index+": "+ $(this).html());
  return index != 2;
})
```
출력값
```
0: item1
1: item2
2: item3
```


## jQuery - css()

Jquery에서 `style`설정하려면 `css()`메서드를 사용한다.  

`css()`메서드를 통해 설정할 수 도, 설정을 가져올 수 도 있다.  
```js
$("p").css("width", "400px");

$("p").css({
  "height": "400px",
  border: "solid 1px gray",
  backgroundColor: "yellow",
  "background-color": "#ccc"
});
```

설정 하나만 설정하려면 문자열로 속성과 속성값을 넘기면 된다.  

설정 여러개를 설정하고 싶다면 속성명과 속성값을 매치한 객체를 넘기면 된다.  

기존 css에서 사용하는 `-`하이폰이 들어가는 속성명을 사용하고 싶다면 쌍따옴표로 묶어야 한다.  
Javascript에서 사용하는 속성명은 쌍따옴표를 줘도되고 안줘도 된다.  


`css()`메서드의 매개변수로 속성명을 전달하면 속성명에 해당하는 속성값을 반환한다.  

```js
var color = $(this).css("background-color");
$("#result").html("That div is <span style='color:" + color + ";'>" + color + "</span>.");
```

스타일의 속성을 한꺼번에 여러개 알고 싶다면 속성명을 배열로 전달하면 된다.  
```js
var styleProps = $(this).css([
  "width", "height", "color", "background-color"
]);
$.each(styleProps, function (prop, value) { 
  html.push(prop + ": " + value);
});
```

알고자 하는 속성을 배열로 전달하면 해당 속성의 값이 들은 `Object`를 반환한다.  
출력값
```
width: 80px
height: 50px
color: rgb(255, 255, 255)
background-color: rgb(15, 99, 30)
```
