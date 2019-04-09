---
title:  "Web - JavaScript 2일차!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - HTML

tags:
  - web
  - html

toc: true

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

<br><br>

## DOM (문서 객체 모델)

> https://ko.wikipedia.org/wiki/문서_객체_모델  

문서 객체 모델(`Document Object Model`)은 객체 지향 모델로써 **구조화된 문서를 표현하는 형식**이다.(위키)  

html또한 형식이 있다.  `html - head - body`로 이루어진 구조있는 형식이다.(XML또한 구조있는 형식)   
**브라우저**는 html구조를 읽고 **DOM이라는 객체를 생성한다**.  

JavaScript는 만들어진 DOM 객체에서 제공하는 각종 메서드와 객체를 사용해서 동적인 처리를 한다.    

html, xml등 구조있는 문서들은 모두 객체화 될 수 있는데 `html DOM`, `XML DOM` 이라 한다.  

DOM은 플랫폼/언어 중립적으로 구조화된 문서를 표현하는 W3C의 공식 표준이다(위키). 

<br><br>

## JavScript 이벤트 처리 방법

지금까진 함수호출 또는 `on...` 이벤트 처리속성 뒤에 바로 JavaScript코딩을 하였다.  


### 간단한 속성변경
함수호출하지 말고 바로 Javascript코딩을 작성  
`<button onclick="this.innerHTML='처리완료!'">이벤트 처리</button>`  

### 동적으로 이벤트 처리 함수 등록
```html
<button id="btn2" onclick="">이벤트 처리2</button> <br>
<script>
  function test() {
    alert("동적으로 클릭 이벤트 등록");
  }
</script>
```
맨 처음에는 `btn2`에 `onclick`을 처리하는 함수를 등록해놓지 않고  

```js
function window_load() {
  alert("window_load 이벤트 발생, 페이지 로딩중...")
  document.getElementById("btn2").onclick = test;
}
function test() {
  alert("동적으로 클릭 이벤트 등록");
}
```
특정 이벤트가 발생하면 test라는 함수를 btn2의 onclick이벤트 처리 함수로 등록시킨다.

굳이 test()란 메서드 만들 필요 없이 무명 메서를 등록할 수 있따.

```js
function window_load() {
  alert("window_load 이벤트 발생, 페이지 로딩중...")
  document.getElementById("btn2").onclick = function(){
    alert("무명 메서드 등록")
  }
}
```

### html DOM EventListener 이벤트 처리 등록

```js
document.getElementById("btn3").addEventListener(
  "click",
  function () {
    alert("EventListenr 함수 등록");
  }); //useCapture는 생략
document.getElementById("btn3").addEventListener(
  "click",
  test);
document.getElementById("btn2").onclick = function () {
  alert("무명 메서드 등록")
}
```

이벤트 처리함수를 여러개 등록할 수 있다.  

삭제는 `removeEventListener` 사용

```js
document.getElementById("btn3").removeEventListener(
  "click",
  test);
```

### element.attachEvent(), element.detachEvent() 이벤트 처리

`addEventListener`와 똑같은 기능이지만 IE 8.0 이전에는 사용 불가능해서 나온 메서드


## getElementsByTagName

