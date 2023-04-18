---
title:  "Web - JavaScript 2일차 자료형, 이벤트!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - HTML

tags:
  - web
  - html
  - javascript

toc: true
toc_sticky: true

---

## JavaScript this, srcElement

JavaSCript에도 `this`키워드가 있다.  
요소 자기 자신을 전달.

```html
<button onclick="btn_keyclick(this);">btn1</button><br>
<button onclick="btn_keyclick(this);">btn2</button><br>
<button onclick="btn_keyclick(this);">btn3</button><br>
<button onclick="btn_keyclick(this);">btn4</button><br>
<button onclick="btn_keyclick(this);">btn5</button><br>
<script>
  function btn_keyclick(clickedbtn) {
    alert(clickedbtn.innerText);
  }
</script>
```

요소자체(시작태그부터 닫기태그까지)를 넘기기 때문에 요소가 사용할 수 있는 속성들을 `this`로 전달받은 매개변수로도 사용 가능하다.  
`clickedbtn`전달받은 요소에서 `innerText`속성값을 사용!  

this키워드를 사용하면 간단한 속성 변경의 경우 JavaScript를 사용하지 않고도 객체 속성을 변경할 수 있다.  
`<button onclick="this.innerHTML='처리완료!'">이벤트 처리</button>`  


### srcElement

호출한 함수안에서 자기자신 속성값을 사용하기 위해 굳이 `this` 키워드를 매개변수로 전달할 필요 없다.  
`event`객체 안의 `srcElement`를 사용하면 어떤 요소가 호출했는지 알 수 있음.  

```html
<button onclick="btn_keyclick(this);">btn1</button><br>
<button onclick="btn_keyclick(this);">btn2</button><br>
<button onclick="btn_keyclick(this);">btn3</button><br>
<button onclick="btn_keyclick(this);">btn4</button><br>
<button onclick="btn_keyclick(this);">btn5</button><br>
<script>
  function btn_keyclick() {
    alert(event.srcElement.innerText);
  }
</script>
```



## JavScript 이벤트 등록


### 동적으로 이벤트 처리 함수 등록
```html
<button id="btn2" onclick="">이벤트 처리2</button> <br>
<script>
  function test() {
    alert("동적으로 클릭 이벤트 등록");
  }
</script>
```
맨 처음에는 버튼 `btn2`에 `onclick` 이벤트 처리하는 함수를 등록해놓지 않았다.  

```js
function window_load() {
  alert("window_load 이벤트 발생, 페이지 로딩중...")
  document.getElementById("btn2").onclick = test;
}
function test() {
  alert("동적으로 클릭 이벤트 등록");
}
```
특정 이벤트가 발생하면 test라는 함수를 `btn2`의 `onclick`이벤트 처리 함수로 **등록**시킨다.  
`요소.이벤트명 = 함수명` 이런식으로 이벤트에 해당하는 처리 메서드를 등록 할 수 있다.

### 무명메서드

Java에 익명 클래스, 람다식이 있듯이 JavaScript에도 **무명 메서드**가 있다.  

굳이 `test()` 메서드를 만들지 말고 무명 메서드를 사용해보자.  

```js
function window_load() {
  alert("window_load 이벤트 발생, 페이지 로딩중...")
  document.getElementById("btn2").onclick = function(){
    alert("무명 메서드 등록")
  }
}
```
바로 함수를 만들고 `onclick`이벤트 처리함수로 등록!

### html DOM EventListener 이벤트 처리 등록

DOM객체로 변환되면서 제공되는 `addEventListener`메서드를 사용해 이벤트 등록

양식은 다음과 같다.  
`요소.addEventListener(이벤트, 처리함수, 버블링/캡쳐)`

```js
document.getElementById("btn3").addEventListener(
  "click",
  function () {
    alert("EventListenr 함수 등록");
  }); //useCapture는 생략
document.getElementById("btn3").addEventListener(
  "click",
  func_click
  );
```
위와 같이 `click`이벤트를 처리하는 함수를 여러개 등록할 수 있다(무명메소드와 `func_click`함수를 처리 함수로 등록).  


등록된 이벤트 처리 함수 삭제는 `removeEventListener` 사용
```js
document.getElementById("btn3").removeEventListener(
  "click",
  func_click);
```
`func_click`를 처리함수에서 지운다, 이제 처리함수중 무명 메서드 한 개 남음.  


`click`이란 이벤트 외에도 `focus`, `blur`, `mouseover`, `mouseout` 등 JavaScript에서 사용하는 여러 이벤트명이 있음.

```js
element.addEventListener("mouseover", function () { // 마우스가 가리킬때
  event.srcElement.style.backgroundColor = "yellow";
});
element.addEventListener("mouseout", function () { // 마우스가 빠져나올때
  event.srcElement.style.backgroundColor = "white";
});
```


### element.attachEvent(), element.detachEvent() 이벤트 등록

`addEventListener`와 똑같은 기능이지만 IE 8.0 이전에는 사용 불가능함으로 추가 시 `attachEvent`, 삭제 시 `detachEvent`을 사용해야 한다.  
```js
var btn = document.getElementById("btn");

if (btn.addEventListener) {
  btn.addEventListener("click", test); //IE 8.0 이상용
}
else {
  btn.attachEvent("onclick", test); //IE 8.0 이하용
}
```
```js
if (btn.addEventListener) {
  btn.removeEventListener("click", test); //IE 8.0 이상용
}
else {
  btn.dettachEvent("onclick", test); //IE 8.0 이하용
}
```
`test`이름의 메서드를 `click` 이벤트처리 함수로 등록, `attachEvent`와 `dettachEvent`에선 이벤트 명으로 `onclick`을 사용.

`요소.addEventListener` 를 통해 나오는 `boolean`값으로 해당 브라우저가 `addEventListener`을 지원하는지 하지 않는지 알 수 있다.  


### 이벤트를 도중에 종료하는 방법

`<input type="text" onkeydown="return rrn6_keydown();" onkeyup="rrn6_keyup();" id="rrn6" autofocus="autofocus">`

위와 같이 이벤트에 해당하는 함수명 앞에 `return`키워드를 등록하면 `onkeydown` 이벤트 처리함수에서 `return false`로 인해 함수가 종료되었다면 `onkeydown`완료후 진행되는 `onkeyup`이벤트는 발생하지 않는다.  
`onkeyup`다음에 문자가 `input`태그에 입력되는데 `onkeydown`도중에 끝났음으로 입력또한 되지 않는다.  

```js
function rrn6_keydown() {
  var pattern = /^\d$/;
  console.log(pattern.test(String.fromCharCode(event.keyCode)));
  if (!pattern.test(String.fromCharCode(event.keyCode))) {
    return false;
  }
}
```
만약 입력하 값이 숫자가 아닐경우 `return false` 함으로 키보드 입력 이벤트를 중단시킴.  

### 이벤트 버블링(Bubbling) 캡처링(Capturing) 

위에서 이벤트 등록시 생략했던 3번째 인자값이 요소의 이벤트 `버블링`을 적용할건지, 이벤트 `캡처링`을 적용할건지 boolean값으로 결정하는 인자값이다.  

```html
<div id="out">
  out
  <div id="in">in</div>
</div>


<script>
  document.getElementById("out").addEventListener(
    "click",
    function () {
      alert("out div 클릭...")
    },
    true
  );
  //이벤트 버블링(false), 캡처링(true) 선택 옵션, 생략시 버블링

  document.getElementById("in").addEventListener(
    "click",
    function () {
      alert("in div 클릭...")
    }
  );
</script>
```

![js3](/assets/web/js/js3.png){: .shadow}  

out `div`가 자식으로 in `div`를 가지고 있다. 그리고 두 개의 `div`에 `click`이벤트 발생시 `alert`되도록 설정하였다.  

안쪽 `div`를 클릭했을 때 두개의 `div`중 누구의 이벤트 처리 함수가 먼저 호출되는지는 부모 div가 이벤트 버블링인지 캡처링인지에 따라 다르다.  

`Bubbling`은 아래에서 공기방울이 올라오듯이 자식태그의 이벤트처리 함수를 먼저 호출하고 부모의 이벤트 처리를 이어 한다.  
`Capturing`은 반대로 큰 순서대로 부모의 이벤트 처리후 자식의 이벤트를 처리한다.  


### event.target

`event`**발생 진원지**를 찾고 싶을때 사용하는 속성이다. 이벤트가 발생한 DOM객체를 반환한다.  
위의 `in`, `out` 2개의 `div`태그중 클릭한 div태그에 대해서만 이벤트 핸들러를 호출하고 싶다면 이벤트 발생 진원지를 알 필요가 있다.   
`event`가 발생하는 가장 큰 범위 `body`에 `onclick`이벤트 처리 핸들러를 걸어두고 진원지에 따라 처리되도록 조건문을 작성하면 된다.  
```js
document.body.onclick = function () {
  if(event.target.id == "in")
    alert("in div 클릭...")
  else if(event.target.id == "out")
    alert("out div 클릭...")
}
```

> `event.currentTarget`라는 비슷한 event객체의 비슷한 속성이 있는데 이벤트가 발생한 DOM객체를 반환하지 않고 이벤트가 바인딩된 요소 `body`를 반환한다.  

### event.preventDefault()

`preventDefault()`메서드는 기존 `event`수행을 막는 역할을 한다.  

```html
<a href="https://naver.com" id="naver">네이버</a>
<script>
  document.getElementById("naver").onclick = function () {
    event.preventDefault();
    console.log("네이버로 가는것을 막음");
  }
</script>
```
`a`태그의 기본 역할인 link로 이동하는 이벤트를 막고 `console.log`를 수행한다.  

<br><br>

## Element - className

**요소에 class명을 지정하거나 가져올수 있는 속성.**  
기존에 `myClass`라는 클래스명을 위한 스타일 시트를 정의해놓고 이벤트 발생시 요소의 class명을 변경시켜 해당 스타일 시트를 적용받도록 할 수 있다.  


```css
.mystyle {
  width: 300px;
  height: 100px;
  background-color: coral;
  text-align: center;
  font-size: 25px;
  color: white;
  margin-bottom: 10px;
}
```
```js
element.addEventListener("focus", function () {
  event.srcElement.className = "mystyle";
});
element.addEventListener("blur", function () { // 포커스를 잃을때
  event.srcElement.className = "";
});
```
요소에 `focus` 이벤트 발생할때 class명을 `mystyle`로 바꾸는 무명 메서드를 등록하고  
요소에 포커스를 잃고 `blur` 이벤트 발생할때 class명을 공백으로 로 바꾸는 무명 메서드를 등록한다.  

요소의 class명이 동적으로 변하면서 css의 가상클래스 역할을 대신 할 수 있다.


## getElementsByTagName

Element를 JavaScript에서 사용하기 위해 `getElementsById` 메서드를 사용했는데 모든 태그에 대한 요소들을 갖고 오고 싶다면 `getElementsByTagName`함수를 사용하면 된다.  

`var tags = document.getElementsByTagName("input");`  
모든 input태그의 요소들을 `tags`변수에 집어넣는다.

`for`문을 사용해서 하나하나의 요소에 접근해야 한다. (자료형은 콜렉션으로 `Object`의 일종)
```js
for (var i = 0; i < tags.length; i++) {
    var element = tags[i];
}
```

## getElementsByClassName

Element를 class명을 사용해서도 가져올 수 있다.  

class명은 여러개체가 그룹으로 사용할 수 있기 때문에 반환값이 배열이다.
class명을 가지는 특정 객체 하나만 가져오고 싶다면 index값을 사용해야 한다.  

`document.getElementsByClassName("days")[0];`

class명이 days로 설정된 개체중 첫 번째 객체를 가져온다.  



## 윈도우 크기 - innerWidth, innerHeight

문서, 브라우저 크기가 변경될때 역시 `resize`라는 이벤트가 발생하는데 최상위 BOM객체인 `window`에 `addEventListener`를 통해 이벤트 처리함수를 등록해주면 된다.  

```html
<p id="demo1"></p>
<p id="demo2"></p>
...
<script>
window.addEventListener("resize", function () {
  // document.getElementById("demo").innerHTML = Math.random();
  document.getElementById("demo1").innerHTML = window.innerWidth;
  document.getElementById("demo2").innerHTML = window.innerHeight;
});
</script>
```

IE 구버전을 위한 함수가 따로 있음
```js
/* 창의 너비와 높이 */
document.body.clientWidth
document.body.clientHeight

/* 문서 전체의 너비와 높이 */
document.body.scrollWidth
documnet.body.scrollHeight 
```

> `outerWidth`속성도 있는데 `innerWidth`가 문서의 넓이라면 `outerWidth`는 브라우저의 넓이.  

>BOM객체는 나중에 알아보자.

### JavaScript 문자열

JavaScript에선 문자열표현시 `"`쌍따옴표 혹은 `'`홀따옴표를 사용한다.  
만약 홀따옴표나 쌍따옴표를 출력하고 싶다면 다음과 같이 작성

```js
var x1 = "hello 'world'!"
var x2 = 'hello "world"!'
var x3 = 'hello \'world\'!'
var x4 = "hello \"world\"!"
```
역슬레시`\`를 통해 제어문자도 사용 가능하다. `\n`, `\t`, `\b` 등...  
`var x4 = "hello \"world\"!\nwelcome!"`




```html
<script>
  var x = 10;
  var k = 30;
  var y = "20";
  console.log(x+k+y);
</script>
```

출력되는 값은 `"4020"`이다.(일반적인 연산은 좌측에서 우측으로 진행)  
`문자열 + 정수` 는 자바처럼 문자열간의 연산으로 취급받는다.   

문자열 숫자를 형변환하는 방법은 3가지 있다.

### 3. eval() 전역 메서드

`eval()`메서드는 단순히 문자열을 숫자로 변환하는 함수가 아니다.  
문자열로 된 코드를 실행시키는 함수로 여러가지 특권을 가지고 있어 남발하는 것은 좋지 않다.  
> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/eval  


`eval("2 + 2");` 완전 문자열로 된 식을 실행시켜 4를 반환한다.  

