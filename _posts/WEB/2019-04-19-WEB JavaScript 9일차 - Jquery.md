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

### jQuery - prepend, append, prependTo, appendTo

`prepend()`와 `append()`는 자신의 자식 객체로 괄호안의 태그를 삽입한다.  
`parentTag.prepend(childTag)`형식으로 사용하면 된다.  

`prepend()`가 첫번째 자식으로 추가, `append()`가 마지막 자식으로 추가하겠단 뜻이다.  

다음과같은 div태그와 p태그가 있을 때 첫번째, 마지막번째에 새로운 p태그를 추가해보자.  
```html
<div id="demo">
  <p id="p1">p1</p>
  <p id="p2">p2</p>
  <p id="p3">p3</p>
</div>
```
```js
$("#demo").prepend("<p id='p0'>p0</p>").append("<p id='p4'>p4</p>");
```
출력값
```
p0
p1
p2
p3
p4
```
`prependTo()`, `appendTo()`는 괄호 안에 부모태그가 들어간다.  
위의 코딩을 아래와 같이 바꿀수 있다.  
```js
$("<p id='p0'>p0</p>").prependTo("#demo");
$("<p id='p4'>p4</p>").appendTo("#demo");
```
즉 `자식.appentTo(부모)` 형식으로 쓰인다.

### jQuery - before, after, insertBefore, insertAfterppendTo


`before()`, `after()`메서드는 형제레벨에서 해당 객체 앞, 뒤에 객체를 추가시킨다.  

다음과 같은 태그에서 `p1`앞에 `p0`을, `p3`뒤에 `p4`를 추가해보자.  
```html
<div id="demo">
  <p id="p1">p1</p>
  <p id="p2">p2</p>
  <p id="p3">p3</p>
</div>
```
```js
$("#p1").before("<p id='p0'>p0</p>");
$("#p3").after("<p id='p4'>p4</p>");
```

`insertBefore()`, `insertAfterppendTo()`는 반대로 생각하면 된다.  
```js
$("<p id='p0'>p0</p>").insertBefore("#p1");
$("<p id='p4'>p4</p>").insertAfter("#p3");
```

<br><br>

## Jquery 이벤트 핸들러

`on()`메서드 사용하거나 `이벤트명()`메서드를 사용하는 방법이 있다.  

`$("button").click( function(){ ... } )`  
`$("button").on( "click", function (){ ... } )`  

jquery, ajax 이벤트 등록 on메서드를 사용해야 함으로 기억해두자....


### one()

딱 한번만 호출해야 하는 이벤트라면 `one`메서드로 이벤트를 등록하자.  
```js
$("button").one("click", function () {
  alert();
}) //딱 한번만 이벤트 호출
```
### off()

이벤트를 제거애햐 한다면 `off()`메서드  

`$( "p" ).off();`  
p태그의 모든 이벤트 핸들러를 삭제   

`$( "body" ).off( "click", "p", foo );`  
`p`태그중 `click` 이벤트 발생시 호출하는 `foo`이벤트 핸들러 삭제

### trigger()

특정 요소의 이벤트를 강제로 호출하려면 해당 객체를 `jQuery()`로 가져와 `trigger()`메서드를 호출하면 된다.  

```html
<button id="btn1">btn1</button>
<button id="btn2">btn2</button>
```
```js
$("#btn1").on("click", function () {
  $("#btn2").trigger("click");
  // $("button:last").click(); //이렇게  click() 메서드를직접 호출해도됨
})
$("#btn2").on("click", function () {
  alert("btn2 에서 이벤트가 발생!");
})
```

`btn1`을 클릭하면 `btn2`를 `trigger()`를 통해 강제 호출  


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

<br><br>

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
전역함수처럼 사용할 때 첫번째 매개변수로 배열, 두번째 매개변수로 처리할 함수를 넣는다.  



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

`each`메서드의 매개변수로 들어가는 함수의 인자로 `index`와 `element`를 사용 가능하다.  
HTML DOM객체에서만 사용가능한 속성을 써야 할 때에 `element`인자를 사용하면 편하다.  
```js
$("*", document.body).each(function (index, elem) { 
  var parentTag = $(this).parent().get(0).tagName; //get()메서드는 DOM객체를 반환
  var parentTag = elem.parentElement.tagName; //윗줄과 같은 코딩
  $(this).prepend(document.createTextNode(parentTag + " > "));
});
```
body에 속하는 모든 태그를 가져와서 자기 자신 앞에 `부모태그명 >`을 붙여 넣는다.  

```html
<div>div,
  <span>span, </span>
  <b>b </b>
</div>

<p>p,
  <span>span,
    <em>em </em>
  </span>
</p>

<div>div,
  <strong>strong,
    <span>span, </span>
    <em>em,
      <b>b, </b>
    </em>
  </strong>
  <b>b </b>
</div>
```
출력값
```
BODY > div, DIV > span, DIV > b
BODY > p, P > span, SPAN > em

BODY > div, DIV > strong, STRONG > span, STRONG > em, EM > b, DIV > b
```

<br><br>

## jQuery - $.map()

배열의 각 아이템, 혹은 객체의 각 속성에 특정 작업을 한 후 **배열**로 반환한다.  
```js
var obj = {
  name: "Honggildong",
  age: 23,
  job: "student"
};

obj = $.map(obj, function(value, index){
  return value + " 입니다.";
});
$.each(obj, function(index, value){
  console.log(index + ": " + value);
});
```
출력값
```
0: Honggildong입니다.
1: 23입니다.
2: student입니다.
```
`each`에는 첫번째 매개변수에 첨자값을 넣었지만  
`map`에선 두번째 매개변수에 첨자값을 넣는다.  

또한 객체를 넣든 배열을 넣든 반환값은 무조건 배열이다.  

<br><br>

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

<br><br>

## jQuery - addClass(), removeClass()

Jquery객체에 클래스명을 부여하려면 `addClass`, `removeClass` 메서드를 사용해야 한다.  

Javascript에선 `className`속성을 사용하거나 `classList`속성의 `add()`, `remove()`를 사용했었다.  
`dot.className = "active";`  
`classList.add()`, `classList.remove()`  

Jquery객체에는 쓸 수 없음으로 `addClass()`, `removeClass()` 메서드를 사용.

```html
<p>Hello</p>
<p>and</p>
<p>Goodbye</p>
```

```js
$("p").addClass("selected"); //모든 p태그에 selected클래스 부여
$( "p:last" ).removeClass( "selected" );
```
`last`는 jquery선택자로 css에는 `last-child`가 있음


`addClass()`매개변수로 함수를 전달할 수 있는데 컬렉션중 조건에 부합하는 객체에 클래스를 부여하거나 제거할 수 있다.  
```html
<div>Lorem ipsum dolor sit amet.</div>
<div class="red">Neque libero est iusto architecto?</div>
<div>Dolores provident sapiente voluptatibus fugit?</div>
```
```js
$("div").addClass(function (index, currendClassName) {
  var addedClass;
  if (currendClassName === "red") {
    addedClass = "green";
    $("p").text("There is one green div");
  }
  return addedClass;
})
```
무명 메서드 안의 2번째 매개변수는 각 `div`태그의 `class`명을 가져온다.  
만약 `class`명이 `"red"`인 객체는 `addedClass`변수에 `"green"`을 할당하고 반환한다.  

### toggleClass()

`classList.toggle`메서드와 같은 기능을 하는 메서드   
`class`명을 부여했다 뺏는것을 한번씩 반복한다.

## jQuery - prop(), attr()

둘다 요소(객체)의 속성을 가져오거나 설정하는 메서드이다.    
`attr`이 1.6버전 이전에 쓰였고  
`prop`가 1.6버전 이후에 만들어 졌다.  

```js
$("thead :checkbox").click(function () {
  console.log($(this).prop("checked")); //true
  $("tbody :checkbox").prop("checked", $(this).prop("checked"));
});
```
`thead`안의 `checkbox`에 checked속성을 가져와서 tbody의 `chekced`속성에 대입한다.  

원래 `checkbox`에서 체크 속성을 주려면 다음과 같이 태그안 속성을 설정한다.  
`<input type="checkbox" checked="checked">`  



<br><br>

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

### toggle() 
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


<br><br>


## jQuery - fadeIn(), fadeOut(), fadeToggle()

`show()`, `hide()`, `toggle()` 메서드와 비슷한 역할을 하는 함수이다.  

차이점은 `show`와 `hide`는 `width`, `height`를 0으로 만드는 애니메이션이라면  
`fadeIn`과 `fadeOut`는 `Opacity` 투명도를 0으로 만드는 애니메이션이다.  

```js
$("#div1").fadeIn("fast");
$("#div2").fadeIn("slow");
$("#div3").fadeIn(3000);
```
속성값으로 애니메이션 진행속도를 결정하는 키워드 문자열, ms 정수값을 넣을 수 있다.  

```js
$("#div3").fadeToggle("fast",function () {
  alert("test")
});
```
당연히 단순 지웠다 표시할 생각이라면 `fadeToggle`메서드를 사용하는 것이 효율적이고 각 메서드의 두 번째 매개변수로 `callback`메서드 등록이 가능하다.  

## jQuery - slideUp(), slideDown(), slideToggle()

위의 `show`, `hide`, `fade` 메서드와 더불어 객체를 보이게하고 보이지 않게 하는 애니메이션 메서드.  

`slide`이름 답게 `accordion` 애니메이션 기능을 한다.  
`class`명`flip` `div`태그를 클릭하면 `panel` `div`태그가 `slideUp`, `slideDown`되도록 설정.  
```html
<div class="flip">slide 01</div>
<div class="panel">Lorem, ipsum dolor sit amet consectetur adipisicing elit. Veniam, voluptatum.</div>
<div class="flip">slide 02</div>
<div class="panel">Lorem, ipsum dolor sit amet consectetur adipisicing elit. Veniam, voluptatum.</div>
<div class="flip">slide 03</div>
<div class="panel">Lorem, ipsum dolor sit amet consectetur adipisicing elit. Veniam, voluptatum.</div>
<div class="flip">slide 04</div>
<div class="panel">Lorem, ipsum dolor sit amet consectetur adipisicing elit. Veniam, voluptatum.</div>
```
```js
$(document).ready(function () {
  $(".flip").click(function () {
    $(".panel").slideUp();
    if($(this).next().css("display") == "block")
      return;
    $(this).next().slideToggle();
  })
})
```
그냥 `$(this).next().slideToggle();`를 사용해도 되지만 하나의 `panel` `div`태그만 열고싶다면 모든 panel을 `slideUp`시키고 선택한 `panel`만 `slideToggle()` 되도록 설정,  

<br><br>

## jQuery - animate()

위에서 설명한 애니메이션 효과들은 효율적이고 좋지만 애니메이션을 직접 구현해야 할 때 가 있다.  
`animate()`는 각종 `properties`를 사용해 다양한 애니메이션을 제공한다.  

아래처럼 버튼과 `div`태그가 있을 때 버튼을 누르면 `div`태그가 오른쪽으로 50px만큼 이동하도록 애니메이션을 만들어보자. 

```html
<style>
div {
  background-color: green;
  position: absolute;
  height: 100px;
  width: 100px;
}
</style>
...
...
<button>run</button>
<div></div>
```

```js
$("button").click(function () {
  $("div").animate(
    {
      left: "+=50px",
      opacity: "0.5",
      height: "150px",
      width: "150px"
    });
})
```
가장 기본적 처번째 매개변수로 애니메이션 속성을 나타내는 `insertAfter`객체를 전달한다.  

`animate()`메서드 사용 양식은 2가지이다.  
1. `animate( properties [, duration ] [, easing ] [, complete ] )`  
2. `animate( properties, options )`  

첫번째 양식은 `properties`객체와 `지속시간`, `timing`, `콜백함수`를 등록할 수 있는 가장 기본적인 양식이다.  
간단히 사용하려면 첫번째 양식을 사용하면 된다.   

추가로 `width`, `height`, `opacity`이 세 속성에 대해서는 속성값으로 `show`, `hide`, `toggle` 사용할 수 있다.  
```html
<button id="clickme">
  Click here
</button>
<img id="book" src="https://via.placeholder.com/100x123" alt="" width="100" height="123" style="position: relative; left: 10px;">
```
```js
$("#clickme").click(function () {
  $("#book").animate({
    width: "toggle",
    height: "toggle",
    opacity: "toggle"
  }, 5000, "linear", function () {
    $(this).after("<div>Animation complete.</div>");
  });
});
```
뒤의 코드는 `width`, `height`가 줄어들면서 투명도 또한 줄어든다. 그리고 콜백함수가 호출된다.  
그리고 `toggle`속성값이기 때문에 다시 클릭하면 `width`, `height`, `opacity`가 원상복구되고 콜백함수가 호출된다.  


`width`에만 `toggle` 속성을 주면 가로방향으로 `slide` 되는 효과를 줄 수 있다.
```js
$(document).ready(function () {
  $("button").click(function () {
    $("div").animate({ width: 'toggle' }, 350);
  });
});
```
반대로 `height: 'toggle'`로 설정하면 세로 방향으로 `slide`되는 효과를 줄 수 있음.  

---

두번째 양식은 `properties`객체와 `options`객체를 매개변수로 넘긴다.  

`options`객체의 멤버변수로 다양한 설정을 할 수 있는데 첫번째 양식의 `duration`, `easing`, `complete`는 당연히 설정 가능하고 `queue`, `step`, `start`, `direction` 등의 속성이 있다.  
> https://api.jquery.com/animate/#animate-properties-options

```js
$("#clickme").click(function () {
  $("#book").animate({
    width: "toggle",
    height: "toggle"
  }, {
    duration: 5000,
	  easing: "linear",
    complete: function () {
      $(this).after("<div>Animation complete.</div>");
    }
  });
});
```
### animate() - queue

`animate()`메서드를 통해 한 객체에 여러가지 애니메이션 효과를 적용하면 이 여러가지 애니메이션이 한번에 동시에 적용되는 것이 아닌  
`queue`형태(선입선출) 먼저 정의된 애니메이션이 끝난후 차례대로 진행된다.
```css
div {
  background-color: green;
  height: 100px;
  width: 100px;
  position: absolute;
}
```
```html
<button>run</button>
<div>sample</div>
```
```js
$("button").click(function () {
  $("div").animate({height: "300px"});
  $("div").animate({width: "300px"});
  $("div").animate({height: "100px",})
  .animate({width: "100px"})
  .animate({"font-size": "3em"}); //체인으로 연결 가능.
});
```

하지만 동시에 적용시키고 싶을 수도 있다!  
그럴땐 두번째 양식의 매개변수인 `options`객체의 `queue` 속성을 사용하면 된다.
```js
.animate({
  width: "90%"
}, {
    queue: false,
    duration: 3000
})
```

<br><br>

### JqujQuery - stop()
`stop()`메서드를 통해 객체에 애니메이션 효과가 진행중이라면 멈추게 기다리는 것(`duration`) 없이 끝낼 수 있다.  

`stop( [clearQueue ] [, jumpToEnd ] )`  
스탑의 매개변수로는 `boolean`형 2개가 들어간다.  

첫번째 파라미터인 `clearQueue`에 `true`를 전달하면 해당 객체에 적용할(큐에서 대기중) 애니메이션들이 모두 삭제(`clear`)된다.  

두번째 파라미터인 `jumpToEnd`에 `true`를 전달한하면 해당 객체의 애니메이션을 최종상태로 완료시킨다.


이런식으로 중간에 `stop()`메서드를 호출에 진행중이던 애니메이션을 없애고 바로 새로운 애니메이션을 적용시킬 수 있다.  

```js
$("#go").click(function () {
  $(".block").stop(true, false).animate({
    left: "+=100px"
  }, 2000);
});
$("#back").click(function () {
  $(".block").stop(true, false).animate({
    left: "-=100px"
  }, 2000);
});
```


## Jquery - DOM객체 탐색

```js
parentElement
children
firstElementChild
lastElementChild
nextElementSibling
previousElementSibling
```
자바 스크립트에서 위 `DOM`객체 함수를 통해 객채간 이동이 가능하듯이  
Jquery에서도 `Traversing`메서드를 지원한다.

다음과 같이 태그가 있을때 상위, 하위태그를 어떻게 찾는지 확인해보자.  
```css
.ancestors * {
  display: black;
  border: solid 2px gray;
  padding: 5px;
  margin: 15px;
}

span {
  display: block;
}
```
```html
<div class="ancestors">
  <div style="width: 500px;"> div(증조부모)
    <ul> ul(조부모)
      <li>li(부모)<span>span</span></li>
    </ul>
    <ul> ul(조부모)
      <li>li(부모)<span>span</span></li>
    </ul>
  </div>
  <div style="width: 500px;">div(조부모)
    <p>p(부모)
      <span>span</span>
    </p>
  </div>
</div>
```
![js16]({{ "/assets/web/js/js16.png" | absolute_url }}){: .shadow}{: width="300"}


### parent(), parents(), parentsUntil()

`parent()` 메서드는 바로 위의 부모태그를 가리킨다.  
```js
$("span").css("border-color", "red")
.parent().css({ //직계부모
	"border": "solid 2px blue"
}) */
```
![js17]({{ "/assets/web/js/js17.png" | absolute_url }}){: .shadow}{: width="300"}


`parents()`메서드는 모든 부모(상위)태그를 가리킨다.  
```js
$("span").css("border-color", "red")
.parents().css({ //부모의 부모의 부모...모든 부모
	"border": "solid 2px blue"
})
```
![js18]({{ "/assets/web/js/js18.png" | absolute_url }}){: .shadow}{: width="300"}


`body`, `html`태그까지도 가리키기 때문에 조절이 필요하다.  
```js
$("span").css("border-color", "red")
.parents("ul").css({ //selector를 주면 부모중 해당 선택자만 선택 가능
	"border": "solid 2px blue"
})
```
![js19]({{ "/assets/web/js/js19.png" | absolute_url }}){: .shadow}{: width="300"}

이런식으로 `parents(selector)` 매개변수에 선택자를 주어 해당 태그만 선택할 수 있다.  


`parentsUntil()`메서드는 특정 조건을 찾을때까지 부모를 계속 찾는다.
부모중에 selector는 포함하지 않음.
```js
$("span").css("border-color", "red")
.parentsUntil("div").css({
  "border": "solid 2px blue"
});
```
![js20]({{ "/assets/web/js/js20.png" | absolute_url }}){: .shadow}{: width="300"}

<br><br>

### children

자식객체를 선택하는 메서드는 `children`이 있다.  
```js
$(".ancestors").children().css({ //children은 직계 자식만 찾는다.  
  border: "solid 2px blue"
});
```

직계 자식만 선택하기 때문에 더 깊은 자식객체를 선택하고 싶다면 `find()`메서드를 사용해야 한다.  

만약 모든 요소뿐 아니라 모든 노드를 가져오고 싶다면 `contents()`메서드를 사용
> https://api.jquery.com/contents/#contents

### find()

`find(selector)`메서드는 괄호안의 선택자를 사용해 호출객체안의 객체중 해당하는 객체를 반환한다.  

`$(".ancestors").find("*"));`  
`"*"`키워드를 사용하면 해당 객체의 모든 직계 자손 뿐 아니라 자식태그를 가져온다.  

`$(".ancestors").find("span");` 물론 선택자를 사용해 해당하는 객체만 가져올 수 있음.


### siblings, next, prev, nextAll, prevAll

형재레벨간 이동할 때 해당 메서드들을 사용한다.  

다음과 같은 태그가 있을 때 각 메서드가 어떤 역할을 하는지 확인해보자.  
```css
div *{
  display: block;
  border: solid 2px gray;
  margin: 15px;
}
```
```html
<div>
  <p>p</p>
  <span>span</span>
  <h2>h2</h2>
  <h3>h3</h3>
  <p>p</p>
</div>
```
![js21]({{ "/assets/web/js/js21.png" | absolute_url }}){: .shadow}{: width="300"}



`siblings()`메서드는 자신을 제외한 주위의 모든 형제 객체를 선택한다.
```js
$("h2").siblings().css({ //모든 형제객체 가져옴
  "border-color": "red"
})
```
![js22]({{ "/assets/web/js/js22.png" | absolute_url }}){: .shadow}{: width="300"}

물론 매개변수로 선택자를 지정 가능하다.  
```js
$("h2").siblings("p").css({
  "border-color": "red"
})
```

`next()`와 `prev()`는 앞, 뒤의 형제 객체를 선택한다.  
```js
$("h2").next().css({ //뒤에 형제
  "border-color": "red"
})
$("h2").prev().css({ //앞의 형제
  "border-color": "blue"
})
```
![js23]({{ "/assets/web/js/js23.png" | absolute_url }}){: .shadow}{: width="300"}

`nextAll()`과 `prevAll()`은 앞, 뒤의 모든 형제 객체를 선택한다.  
```js
$("h2").nextAll().css({ //뒤의 모든 형제
  "border-color": "red"
})
$("h2").prevAll().css({ //앞의 모든 형제
  "border-color": "blue"
})
```
![js24]({{ "/assets/web/js/js24.png" | absolute_url }}){: .shadow}{: width="300"}

`nextUntil()`과 `prevUntil()`메서드도 있으니 참고할 수 있도록 하자.  
> https://api.jquery.com/category/traversing/tree-traversal/

<br><br>


### jQuery - end()

`end()`메서드는 jquery 체인에서 중요한 역할을 한다.  
`find`, `filter`등의 Traversing메서드에서 빠져나와 상위의 객체로 가는 역할을 한다.  

```html
<ul class="first">
  <li class="foo">list item 1</li>
  <li>list item 2</li>
  <li class="bar">list item 3</li>
</ul>

<ul class="second">
  <li class="foo">list item 1</li>
  <li>list item 2</li>
  <li class="bar">list item 3</li>
</ul>
```
```js
$("ul.first")
  .find(".foo")
  .css("background-color", "red")
.end()
  .find(".bar")
  .css("background-color", "green");
```




## jquery - 분류

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

### not()

호출한 객체 set(배열)에서 `not(selector)` 괄호안의 선택자를 제외한 객체set을 반환한다.
```html
<ul>
  <li>list item 1</li>
  <li>list item 2</li>
  <li id="notli">list item 3</li>
  <li>list item 4</li>
  <li>list item 5</li>
</ul>
```
```js
$( "li" ).not( $( "#notli" ) )
  .css( "background-color", "red" );
```

3번째 `li`태그를 제외한 모든 `li`태그 배경색을 red로 변경

<br><br>

## Jquery 선택자

Jquery에선 css에서 사용하는 선택자 외에 Jquery만의 별도의 선택자를 지원한다.  

**Jquery 선택자**|**설명**
:-----:|:-----
`$("*")`|모든 요소들 선택
`$("tag")`|태그명을 주어 해당하는 모든 태그를 선택
`$("#id")`|`#`을 붙여 `id`에 해당하는 요소를 선택
`$(".class")`|`.`을 붙여 `class`명에 해당하는 요소를 선택
`$(".class, .class")`|css에서 사용하는 방법, 나열한 모든 `class`선택
`$("class:first")`|jquery에서 사용하는 선택자, 선택된 클래스중 **첫번째** 요소를 선택한다.
`$("class:last")`|jquery에서 사용하는 선택자, 선택된 클래스중 **마지막** 요소를 선택한다.
`$("class:even")`|jquery에서 사용하는 선택자, 선택된 클래스중 **짝수번째** 요소를 선택한다.
`$("class:odd")`|jquery에서 사용하는 선택자, 선택된 클래스중 **홀수번째** 요소를 선택한다.
`$("div:eq(n)")`|`eq`는 괄호 안의 **n번째 태그**를 가져오는 선택자
`$("div:gt(n)")`|`gt`는 괄호 안의 **n번째 초과** 위치하는 태그를 가져오는 선택자
`$("div:lt(n)")`|`eq`는 괄호 안의 **n번째 이하** 위치하는 태그를 가져오는 선택자
`$(":header")`|모든 `hn`태그를 가져오는 선택자, `:input`, `:image` 등 자주 사용되는 태그는 선택자로 모두 가져올 수 있다.
`$(":text")`|`input`태그중 `type`이 `text`인것 선택
`$(":animated")`|현재 `animate`메서드가 동작중인 요소를 찾아옴
`$("div:contains('test')`|요소의 content중 특정 문자열이 포함된 요소를 찾아옴
`$("p:has(span)")`|`span`태그를 가지고 있는 `p` 태그를 요소로 선택함
`$("p:empty")`|`p`태그중 비어있는 태그를 선택(css에도 있음)
`$("p:parent")`|태그중 자식이 있는 태그만 선택 `$("p:not(:empty)")`와 같다 볼 수 있음
`$("div:hidden")`|`display: none`, `visibility: hidden`, `opacity: 0` 모두 찾아낸다. `:visible` 선택자도 있음.
`$(":root")`|`html`태그를 선택

> https://api.jquery.com/category/selectors/


> https://kouzie.github.io/html/HTML-CSS-가상요소,-결합자,-속성선택자,-전역변수/#before---counter  
css에서 사용하던 선택자를 모두 Jquery에서 사용 가능하다.  

<br><br>

### is()

## jQuery - noConflict()

충돌을 방지하기 위한 메서드, 위에서 jquery는 `$`기호를 사용해 Jquery객체를 만든다.

하지만 다른 라이브러리에서 `$`를 키워드로 쓴다면 Jquery와 충돌이 일어날 수 있는데 `noConflict()` 메서드를 사용해 다른 기호를 사용할 수 있다.
```js
var $q = jQuery.noConflict();
$q( "div" ).hide();
```

## jQuery - $.extend()

`$.extend()`메서드는 객체 확장 메서드로 객체에 추가 속성을 주고 싶을때 일일이 `객체명.속성명 = 속성값` 형식을 사용하면 지저분하다.  
```js
var obj = {};
obj.name = "Hong";
obj.age = 23;
obj.job = "student";
```
위 코드를 extend()메서드를 사용해 다음과 같이 바꿀 수 있다.
```js
var obj = {fisrtName: "Hong"};
$.extend(obj{
  middleName: "gildong",
  age: 23,
  job: "student"
});
```

`extend()`를 사용해 여러개의 객체를 결합할 수 있다.    
`$.extend(객체1, 객체2, 객체3, ... 객체N)`  

```js
var obj = {
  name: "Honggildong",
  age: 23,
  job: "student"
};
var stdinfo = {
  kor: 100,
  mat: 95,
  eng: 89
};
var studentObj = $.extend(obj, stdinfo);
$.each(studentObj, function(index, value){
  console.log(index + ": " + value);
});
```
출력값
```
name: Honggildong
age: 23
job: student
kor: 100
mat: 95
eng: 89
```