---
title:  "Web - JavaScript 1일차 개요!"

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

## JavaScript 개요

웹 프로그래밍 언어중 하나, html의 내용을 **동적**으로 변경할 수 있다.  
스타일, 속성, 요소를 숨기거나 표시할 수 있음.  

>에크마스크립트(ECMAScript): 유럽 컴퓨터 제조업자 협회(ECMA)에서 제정된 자바스크립트(JavaScript) 언어 표준. [네이버 지식백과]  
브라우저도 표준을 따라 동작해야한다.  
1년마다 한번씩 JavaScript의 표준을 정의해준다.  


<br><br>


## 읽을거리 - 자바 스크립트 시리즈

>https://engineering.huiseoul.com/자바스크립트는-어떻게-작동하는가-엔진-런타임-콜스택-개관-ea47917c8442   
자바스크립트는 어떻게 작동하는가: 엔진, 런타임, 콜스택 개관  
How JavaScript works: an overview of the engine, the runtime, and the call stack  

>https://engineering.huiseoul.com/자바스크립트는-어떻게-작동하는가-v8-엔진의-내부-최적화된-코드를-작성을-위한-다섯-가지-팁-6c6f9832c1d9  
자바스크립트는 어떻게 작동하는가: V8 엔진의 내부 + 최적화된 코드를 작성을 위한 다섯 가지 팁  
How JavaScript works: inside the V8 engine + 5 tips on how to write optimized code  

>https://engineering.huiseoul.com/자바스크립트는-어떻게-작동하는가-메모리-관리-4가지-흔한-메모리-누수-대처법-5b0d217d788d  
자바스크립트는 어떻게 작동하는가: 메모리 관리 + 4가지 흔한 메모리 누수 대처법  
How JavaScript works: memory management + how to handle 4 common memory leaks  

>https://engineering.huiseoul.com/자바스크립트는-어떻게-작동하는가-이벤트-루프와-비동기-프로그래밍의-부상-async-await을-이용한-코딩-팁-다섯-가지-df65ffb4e7e  
자바스크립트는 어떻게 작동하는가: 이벤트 루프와 비동기 프로그래밍의 부상, async/await을 이용한 코딩 팁 다섯 가지  
How JavaScript works: Event loop and the rise of Async programming + 5 ways to better coding with async/await  

>https://engineering.huiseoul.com/자바스크립트는-어떻게-작동하는가-웹소켓-및-http-2-sse-1ccde9f9dc51  
자바스크립트는 어떻게 작동하는가: 웹소켓 및 HTTP/2 SSE  
How JavaScript works: Deep dive into WebSockets and HTTP/2 with SSE + how to pick the right path  

<br><br>

## JavaScript 작성

`<script></script>` 태그 안에 JS코딩을 해야한다.  
`<script type="text/javascript">` 타입속성을 적는 경우도 있는데  
type속성은 적어도 되고 안적어도 된다.(브라우저가 알아서 JS라고 알아서 해석한다.) 

보통은 `head` 혹은 `body` 태그 안에서 사용한다. 태그 밖에서 사용하면 브라우저가 알아서 해석해서 `head`나 `body`태그 안으로 집어넣음.  

JavaScript에서도 다른 언어들 처럼 명령라인 구분을 위해 세미콜론`;`을 추가해도 되고 엔터로 구분해도 된다.  
단 한줄에서 명령라인 구분을 위해선 추가한다.(사용 권장)

### .js확장자 파일 - 외부 스크립트 파일

`.css`파일처럼 하나의 페이지에서만 사용하는 것 이 아닌 여러 페이지에서 javascript코딩을 사용한다면 `.js`확장자 파일을 만들자.  

`.js`파일 안에서 `script`태그는 쓰지 않아도 된다.  
사용시에는 `script`태그안의 `src`속성을 사용하면 된다.  
`<script src = " "></script>`  

**외부스크립트의 장접**
1. html과 js코드 분리로 인한 유지보수 용이  
2. 캐시된 javascript파일로 인해 처리속도 향상  

<br><br>

### Javascript 함수선언

```js
function name([매개변수]) {
  ...
  [return 리턴값;]
}
```
리턴자료형을 따로 명시하기 않고 리턴값이 없을 수도 있다.  

### Javascript 함수 호출

**1. 만들어진 함수는 이벤트 처리(`onclick`같은)에서 호출**  
`<button onclick="btn_click();">ok</button>`  

**2. 다른 만들어진 함수에서 호출**   

```js
function window_load() {
  var demo = document.getElementById("demo");
  var result = sum(10, 20);
  demo.innerHTML=result;
}
function sum(a, b) {
  return a + b;
}
```

> 함수 이름뒤에 `()` 괄호를 다음과 같이 생략하면 `var result = sum`  
> sum의 정의가 result에 담기게 된다.  
> 정의를 담으면 `result()` 형식으로 sum을 가리키는 함수포인터 처럼 사용될 수 있다.(js에선 함수또한 하나의 변수처럼 취급받음)  



### Javascript 변수 선언

`var`키워드로 변수선언한다. 다른 언어처럼 한 라인에 여러 변수 선언 및 초기화가 가능하다.

```js
function btn1_click() {
  var x, y, z;
  x=5;
  y=3.14;
  z="hong";
  z=x+y;
}
```
자료형이 구분하는 키워드가 `var` 하나이고  하나의 변수로 문자열, 실수, 정수, 객채 등 여러가지 자료형으로 재정의 할 수 있다.  

당연히 함수안에 들어있는 변수는 지역변수로 사용되며 함수를 빠져나오면 사라진다.  

<br><br>

## JavaScript로 data 출력하는 방법

**1. innerHTML, innerText**  
```html
<p id="demo"></p>
<script>
  document.getElementById("demo").innerHTML="출력"
</script>
```
`innerHTML` 또는 `innerText` 함수로 출력처리가 가능하다.  
둘의 차이는 `innerHTML`은 태그까지 적용되고 `innerText`는 태그효과가 적용되지 않고 문자열로 출력된다.  

`inner...`함수는 시작태그와 닫기태그 사이에 content를 다룬다.  

시작태그, 닫기태그 따로 구분 없는 `input`태그같은 경우 `inner...`함수를 사용하지 않는다.  

<br>

**2. document.write()**   
```html
<script>
	document.write("홍길동1");
	document.write("<br>");
</script>
```
html출력해주는 내장객체, 함수 사용

<br>

**3. window.alert()**  

`<p id="demo" onclick="window.alert('경고!');"></p>`

`window`는 자바스크립트 최상위 객체로 생략 가능하다.  
`<p id="demo" onclick="alert('경고!');"></p>`

사실 지금까지 써온 `document`객체 또한 `window`객체의 자식객체이다.

<br>

**4. console.log**  

**브라우저 콘솔**에 출력(디버깅 용도로 자주 사용)
`console.log("출력문자열")`처럼 사용한다.  

![js1]({{ "/assets/web/js/js1.png" | absolute_url }}){: .shadow}  

<br><br>

## onkeydown, onkeypress, onkeyup

키보드 입력시 발생하는 이벤트 속성들이다.  
키보드를 누른다던가, 마우스 클릭, 윈도우 사이즈 이동, 닫기, 최소화, 최대화 등  
브라우저에서 발생할 수 있는 이벤트 처리를 위한 속성들이 JavaScript에 정의되어 있다.  

| | |
:-----:|:-----:|:-----:
**`onkeydown`**|키를 개체 위에서 눌렀을 때 발생
**`onkeypress`**|키를 개체 위에서 눌렀을 때 (영어, 숫자 등)문자가 입력되면 이벤트 발생
**`onkeyup`**|키를 개체 위에서 놓았을 때 발생

`onkeydown`과 `onkeypress`의 발생시기는 동일하며 차이점은  
`onkeydown`는 키보드의 모든 자판이 눌리면 감지하고 `onkeypress`는 키보드에서 **입력가능한 자판**이 눌리면 이벤트가 발생.  

> https://dororongju.tistory.com/91

```html
<input type="text" id="in" autofocus="autofocus"
  onkeydown="in_keydown();">
<br />
<input type="text" id="out">
<script>
  function in_keydown() {
    var out = document.getElementById("out");
    out.value = document.getElementById("in").value;
    
  }
</script>
```

`onkeydown`은 실제 화면에 문자가 찍히기 전에 동작하기 때문에 한박자 느린데  
이게 불편하다면 `onkeyup`을 사용하면 된다.  

채팅 프로그램에서 상대방이 채팅중인지 아닌지 알 수 있는데 `onkey...`함수와 소켓을 통해 실시간으로 상태를 전송중인 javascript함수가 있는 것 이다.  


### event객체 - keyCode 속성

이벤트와 관련된 모든 정보, 마우스 좌우클릭, 휠, 키보드 타건 등 **모든 이벤트 관리는 event내장객체에서 관리**한다.  

웹에서 발생 가능한 이벤트 종류는 매우 많기에 지금은 키보드 관련 이벤트에 해당하는 `keyCode`속성 를 알아보자.  

> onkeydown, onkeypress, onkeyup 이벤트에서 10진수 키값을 반환한다.  


```js
function in_keydown() {
  if (event.keyCode == 13) {
    var out = document.getElementById("out");
    out.value = document.getElementById("in").value;
  }
}
```

> 참고: onkeypress에선 백스페이스를 눌리는것은 탐지하지 않는다.  

### event객체 - altkey, ctrlKey, shiftKey

```js
function in_keydown() {
  if (event.keyCode == 13 && event.altKey || event.keyCode == 8) {
    console.log(document.getElementById("in").value);
    var out = document.getElementById("out");
    out.value = document.getElementById("in").value;
  }
}
```
`alt`, `ctrl`, `shift` 등과 같은 키는 다른키와 조합하여 같이 사용하는 경우가 많아 event객체에서 해당 키가 눌려있는지 아닌지 체크하는 **속성**이 있다.

<br><br>

## JavaScript 전역변수

**`var`키워드 없이 선언할 경우** 지역변수가 아닌 전역변수로 생성되며 다른 함수 안에서도 사용 가능하다.  

```js
function btn2_click() {
  msg = "hello"
  alert(msg);
}
```
>이런식으로 선언하지 않고 사용하는 방식은 독이될 수 있는데 이 방식을 사용하고 싶지 않다면 다음 키워드를 사용한다.  
```html
<script>
  "use strict"
  ...
</script>
```
엄격한 사용기준을 따르도록 한다. ECMAScript5부터 추가됨

**함수 밖에서 선언할 경우** 역시 전역변수이다.
```html
<script>
	<button onclick="btn1_click();">버튼1</button>
	<button onclick="btn2_click();">버튼2</button>
  var age1 = 25;
  age2 = 26;
  function btn1_click() {
    alert(age1);
  }
  function btn2_click() {
    alert(age2);
  }
</script>
<button onclick="btn3_click();">버튼3</button>
<script>
  function btn3_click() {
    alert(age2);
  }
</script>
```

```html
<script>
  var name = "홍길동";
  var name = "김길동";
  function btn3_click() {
    alert(age2);
    document.getElementById("demo").innerHTML = name;
    console.log(name);
  }
</script>
```
이런식으로 동일한 이름의 변수를 중복 선언해도 오류는 안난다. 나중에 선언된 name으로 초기화됨.  

<br><br>

## JavaScript 자료형

`var`키워드 하나로 모든 변수를 선언하지만 JavaScript에도 분명 자료형은 있다.  

```js
console.log(typeof "admin");  //string
console.log(typeof 10);       //number
console.log(typeof 3.14);     //number
console.log(typeof true);     //boolean
console.log(typeof [1, 2, 3, 4]);  //object
console.log(typeof NaN);      //number
console.log(typeof new Date());    //object
console.log(typeof null);     //object
console.log(typeof function(){});  //function
console.log(typeof {name: "hong", age: 20});  //object
```

출력값.
```
string
number
number
boolean
object
number
object
object
function
object
```

>초기화 하지 않은 변수를 넣으면 undifiend로 출력된다. null은 object로 출력됨.

총 5가지의 자료형 존재  

1. string  
2. number  
3. boolean  
4. object  
5. function  

자바 스크립트에선 `===` 연산자가 있는데 안의 데이터 뿐 아니라 type까지 비교해서 일치하는지 검사한다.  

<br>

## JavaScript 객체

그리고 객체는 총 6가지로 나뉜다.  
1. Object  
2. Date  
3. Array  
4. String  
5. Number  
6. Boolean  


### Object
자바의 객체는 특징(속성)과 기능(메서드)로 이루어졌는데 JS도 비슷하다.  

`var s1 = {name: "홍길동", age: 12}`  
JavaScript에선 필드 초기화를 위해 `:`콜론을 사용, 
```js
var s1 = {
			name : "홍길동",
			age : 21,
			
			toString: function () {
				return this.name + "/" + this.age;
			}
		};
		console.log(typeof s1);
		console.log(s1.name);
		console.log(s1["name"]);
		console.log(s1.toString());
```
`toString`이라는 이름의 함수를 정의, 


출력값
```
object
홍길동
홍길동
홍길동/21
```

### Array

`var m = [1, 2, 3, 4];`
`var m = new Array(1, 2, 3, 4);`

두개는 같은 Array객체를 만드는 코딩, 

### String
`var msg = new String("홍길동");`
`var msg = "홍길동";`


### Number
`var  x = 10;`
`var y = new  Number(20);`

<br><br>

## JavaScript 반올림 함수 - Math 객체

`Math.ceil()` : 소수점 올림, 정수 반환
`Math.floor()` : 소수점 버림, 정수 반환
`Math.round()` : 소수점 반올림, 정수 반환
위 3개 함수는 모두 소수점 자체를 없애버리기 때문에 2자리수 반올림을 하고싶다면 100을 곱한 후 반올림 후 다시 100을 나눠야 한다.  

`toFixed()`는 특정 소수점 자리수를 매개변수로 넣어 해당 자리까지 반올림 가능하다.  
```js
var num = 99.9876543;
num.toFixed(0); // 100 출력
num.toFixed(5); // 99.98765 출력
```

<br><br>

## JavaScript 정규표현식


```js
function num_keydown() {
  var num = document.getElementById("num").value;
  if (event.keyCode == 13) {
    /* JS에서 정규표현식 */
    /* /pattern/modifies; */
    var pattern = /^[0-9]+$/;
    pattern.test(num); /* 일치하는지 물어보는 test()함수, boolean반환 */
    if (!pattern.test(num)) { /* 일치하지 않는다면 */
      alert("숫자입력!")
    }
  }
}
```