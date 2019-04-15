---
title:  "Web - JavaScript 6일차 - Obejct, setTimeout, 클로저!"

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


## form태그 안의 input태그 가져오기


다음과 같은 `form`태그가 있을때 `submit`버튼을 누르면 `onsubmit`이벤트가 발생하게된다.   
이때 `form`태그 안의 모든 `input`태그에 데이터가 맞게 들어갔는지 확인하기 위한 `validateForm()` 함수를 호출한다.

```html
<form name="form1" action="ex01_ok.jsp" method="POST" onsubmit="return validateForm();"> <!-- submit시 onsubmit이벤트 발생 -->
    Name: <input type="text" id="fname" name="fname">
    <br>
    <input type="submit">
</form>
```

`validateForm()`함수 안에선 form태그 안의 input태그를 가져올 때 `getElementById`가 아닌 `document.forms`DOM객체를 통해 직관적으로 코딩할 수 있다.  
```js
function validateForm() {
    // var fname = document.getElementById("fname").value;
    //HTML Conllection
    var fname = document.forms["form1"]["fname"].value;
    alert(fname);
    if(fname.trim() == "")
    {
        alert("name은 필수 입력사항입니다.");
        return false;
    }
    return true;
}
```

`document.forms`는 `HTML Conllection`으로 해당 `[폼이름][입력태그명]`으로 객체를 가져올 수 있다.  


`Name: <input type="text" id="fname" name="fname" required="required">`이런식으로 `required`속성을 사용해도 되지만 디테일한 정규식으로 검사하려면 `onsubmit`메서드와 각각의 입력태그를 `document.forms`로 가져와야한다.  

## JavaScript 메서드 정의와 호출

일반적으로 `function test() { ... }`이런식으로 정의하고   
`var result = test();`이런식으로 호출했다.  

JavaScript에선 `function` 정의와 호출방법이 몇가지 더 있다.

### new Function()  
function또한 Obejct의 일종으로 다음과 같이 생성이 가능하다.   
`new Function ([arg1[, arg2[, ...argN]],] functionBody)`   

```js
var func1 = function () { console.log("display") };
var func2 = new Function('console.log("display")'); //매개변수X, 함수 body정의

console.log(typeof func1); //function
console.log(typeof func2); //function
console.log(func1); //ƒ () { console.log("display") }
console.log(func2); 
// 	ƒ anonymous(
// 	) {
// 		console.log("display")
// 	}

func1(); //display
func2(); //display
```
`func1`, `func2` 모두 같은 type, 같은 내용, 같은 기능을 가지고 있다.  

### 표현식 사용 함수  

지금까지 무명 메서드를 정의하고, 이를 함수를 저장하는 변수에 담아 호출했다.
이런 방법은 **표현식 사용 함수**정의라 한다.  
```js
var sum = function(a, b) {
  return a + b;
}
var result = ();
```

### 메서드 선언과 동시에 호출

무명 메서드를 선언과 동시에 호출이 가능하다.  
`(function(){...})();` 이런식으로 정의 부분을 `()`괄호로 묶고 뒤에 또 `()`를 사용해 호출하면 된다.  

```js
var result = (function() {
    return "test"
  })();
  console.log(result); //test
```

### 무명 메서드 람다식 정의

간단한 함수의 경우 람다식을 사용해 직관적이고 간단한 `function`을 정의할 수 있다.
```js
var x = function(a,b){
  return a+b;
};
var x = (a,b) => a+b;
```

### 함수 호출

JavaScript에선 함수 정의부분에 매개변수가 없더라도 호출시 매개변수를 전달 할 수 있다.

```js
function test() {
  for (var i = 0; i < arguments.length; i++) {
    var arre = arguments[i];
    console.log(arre);
  }
}
test("홍길동", 10, 20, 30, "C");
```

출력값  
```
홍길동
10
20
30
C
```
`arguments` DOM객체를 사용하면 된다.  

JavaScript는 함수에 정의된 파라미터 개수와 매개변수 개수를 꼭 맞출필요 없다.(에러는 나지 않음)
단 입력되지 않은 매개변수는 `undifiend`로 초기화되기 때문에 예외처리를 해주어야함.  
```js
function sum(x, y) {
  if(y === undefined)
    y=0;
  return x+y;
}

console.log(sum(10));
console.log(sum(x=10, y=20));
```
또한 직관적인 코드를 위해 함수호출시 사용하는 매개변수명에 `value`를 직접 정의할 수 있다.  


### Function.call()

`Function`이라는 `Object`에는 `call()`함수가 있는데 자신(`this`)이 함수에서 사용할 객체전달이 가능하다.  
```js
var person1 = {
  name : "hong",
  age : 15
}
var person2 = {
  name : "kim",
  age : 25
}
var person = {
  print : function() {
    return this.name + "/" + this.age;
  }
};
console.log(person.print.call(person1));
console.log(person.print.call(person2));
```


<br><br>

## JavaScript Object(객체)

객체의 종류에는 여러가지가 있다. 직접만들 수 도 있고 JavaScript에 이미 존재하는 객체또한 많다.

* Date  
* Array  
* String  
* Number  
* Boolean  
* Fcuntion
* Regex
* 등등....

**자바의 객체**는 특징(속성)과 기능(메서드)로 이루어졌는데 JavaScript역시 속성과 기능을 정의할 수 있다.  

`var s1 = {name: "홍길동", age: 12}`  
JavaScript에선 **필드 초기화**를 위해 `:`콜론을 사용한다.  

```js
var s1 = {
			name : "홍길동",
			age : 21,
			
			print: function () {
				return this.name + "/" + this.age;
			}
		};

		console.log(typeof s1); //object
		console.log(s1.name); //홍길동
		console.log(s1["name"]); //홍길동
		console.log(s1.print()); //홍길동/21
```
`print`이라는 이름의 함수를 정의,  
사실 `print` 함수 정의보다는 `print`라는 함수 `Object`변수를 생성하고 기능을 넣었다는 것이 더 맞는 듯 하다.

보다싶이 `Object`안의 프로퍼티(`Property`) 출력하는 방법은 2가지, `.`뒤에 바로 프로퍼티명을 붙여 쓰는 방법과 `[]`를 사용해서 출력하는 방법이 있다.  


> `for(.. in ..)`문을 통해 쉽게 객체의 프로퍼티명, 프로퍼티의 값을 출력할 수 있다.  
```js
for ( var x in person) {
			console.log(x + " = " + person[x]);
		}
```
~~Object가 배열과 약간 비슷한 면이 있다...~~



### 객체를 생성하는 방법 1. new Obejct()

객체를 생성하는 방법은 `var obj = {};`방식 외에도 여러가지 있다.  

`new Obejct()`를 통해 빈 Object변수를 생성하고 프로퍼티를 추가해 나간다.  
```js
var person = new Object();
person.firstName = "Hong";
person.lastName = "gildong";
person.age = "Hong";
person.print = function() {
			return this.firstName + " " + this.lastName;
		};
```
> 선언만하고 초기화 하지 않는경우 문법오류를 반환한다(애초에 초기화 하지 않은 프로퍼티는 필요 없다).  

프로퍼티를 삭제하고 싶다면 `delete`키워드를 사용한다.  
`delete person.age;`  


<br>

### 객체를 생성하는 방법 2. 생성자 메서드

메서드를 호출할 때 마다 객체를 반환하는 생성자 역할을 하는 생성자 메서드를 정의한다.  
`this`키워드를 사용해 객체를 생성하고 프로퍼티를 정의한다.  
```js
function Person(name, age, color) {
  this.name = name;
  this.age = age;
  this.color = color;
  
  this.print = function() {
    return this.name +" / "+ this.age +" / "+ this.color;
  }
  //메서드추가도 가능.
}
var father = new Person("Hong", 30, "blue");
var son = new Person("Kim", 10, "red");
```

생성자 메서드로 인해 만들어진 객체들은 JavaScript의 다른 객체들처럼 프로퍼티를 삭제하거나, 추가, 재정의 할 수 있다.  

<br><br>

## setTimeout, clearTimeOut

`setTimeout()`메서드는 특정시간후에 특정함수를 호출하도록 하는 메서드이다.  
반환값으로 `timeoutID`라는 타이머를 식별할 수 있는 0이 아닌 값을 반환한다.  

예약한 `setTimeout()` 안의 메서드는 `clear​Timeout()`메서드로 실행을 취소시킬 수 있다.  

```js
var i;
var demo = document.getElementById("demo");
  setTimeout(function () {
    for (i = 0; i < 10; i++) {
        console.log(i);
    }
  }, 1000);
```
`setTimeout`메서드가 호출되고 멈추는(`sleep`) 것이 아니라 특정 시간 이후에 실행시키도록 타이머를 설정하고 계속 진행하기 때문에 `i`값이 모두 10으로 출력된다.  

### 시간 출력

```html
<h1 id="demo"></h1>
<button onclick="timer_setTimeOut();">시작</button><br>
<button onclick="timer_clearTimeOut();">정지</button><br>
```
시작 버튼을 누르면 demo에 시간이 출력되도록, 또한 `setTimeOut()`메서드를 사용해서 1초마다 계속 출력하도록 설정해보자.  

```js
var demo = document.getElementById("demo");
var timer;
function dispTime() {
  var now = new Date();
  demo.innerHTML = now.toLocaleString();
  //한번 호출되면 1초마다 계속 호출되도록 설정
  timer = setTimeout(dispTime, 1000);
}
function timer_setTimeOut() {
  dispTime();
}
function timer_clearTimeOut() {
  clearTimeout(timer1);
}
```
버튼을 누르면 `timer_setTimeOut()`함수가 호출되고 `dispTime()`를 다시 호출한다.  
`dispTime()`안에선 `setTimeout()`메서드를 통해 `dispTime`을 다시 타이머를 설정해 예약한다.  
꼬리에 꼬리를 물듯이 재귀적으로 호출한다.  

정지 버튼을 누르면 `clearTimeout()`메서드를 통해 기존 예약되었던 `dispTime()`실행 타이머가 사라지면서 재귀호출이 멈추게 된다.  

<br><br>

## setInterval, clearInterval

사실 그냥 특정 시간동안 반복적으로 출력할 용도라면 `setInterval`과 `clearInterval` 메서드를 사용하는게 더 효율적이다.  
이 함수들은 애초에 특정시간동안 반복호출하기 위해 설계 되었기 때문에 재귀적 구성을 할 필요가 없다.  


```js
function dispTime_Interval() {
  var now = new Date();
  demo.innerHTML = now.toLocaleString();
}
var timer;
function timer_setInterval() {
  dispTime_Interval(); //지연시작 맨처음 1초를 없애기 위해 처음에 한번 출력

  timer = setInterval(dispTime_Interval, 1000);
}
function timer_clearInterval() {
  clearInterval(timer);
}
```
<br><br>

## 클로저

함수안의 지역변수를 만들고 **함수가 끝나더라도 지역변수를 유지**시키기 위해 함수안에 해당 지역변수를 참조하는 함수를 만드는데  
간단히 말하면 함수안에 선언된 이 함수를 클로저라 한다.  



JavaScript외에 다른 언어에서도 클로저의 개념은 쓰이는데 함수가 끝남에도 지역변수가 계속 유지된다는 것이 특이하다.  
클로저 함수가 지역변수를 참조하고 전역변수가 이 클로저함수를 참조하고 있다면 지역변수는 계속 유지된다.  


### 클로저의 가장 간단한 예

```js
function getClosure() {
  var text = 'variable 1';
  return function () {
    return text;
  }; //반환되는 함수 - 클로저
}
var closure = getClosure();
console.log(closure()); // 'variable 1'
```
> 출처: https://hyunseob.github.io/2016/08/30/javascript-closure/


`getClosure`함수의 지역변수 `text`가 함수가 끝난 시점인 `console.log`에서 정상적으로 출력된다.  
이는 클로저 함수(위의 무명 메서드)가 text를 참조하고있고 `var closure`가 클로저 함수를 참조중이기 때문!

즉 클로저를 사용하는 이유는 호출된 **함수의 지역변수를 유지하고 싶기 때문**이다. 이를 통해 객체지향적인 설계가 가능하다!(코드 간략, 재활용, 은닉화)  

### 클로저를 사용한 private 따라하기 - 모듈 패턴

아래 코드는 단순 클로저 함수를 반환하는 함수를 정의한 것이 아닌  
**클로저 함수를 가지고 있는 객체**를 반환하는 함수를 정의하였다.  

```js
var counter = (function () {
  var privateCounter = 0;
  function changeBy(val) {
    privateCounter += val;
  }
  return { //Object반환
    increment: function () {
      changeBy(1);
    },
    decrement: function () {
      changeBy(-1);
    },
    value: function () {
      return privateCounter;
    }
  };
})();
```
무명 메서드를 정의와 동시에 바로 호출하였다, `})();` 이부분 주의!  
무명 메서드코드에 따라 지역변수를 만들고 지역변수를 참조하는 함수를 정의, 해당 함수를 참조하는 객체를 반환한다.  

반환된 객체의 `increment`, `decrement`, `value` 메서드를 통해  `changBy()`메서드를 호출하고 `privateCounter`지역변수를 변경한다.  

```js
console.log(counter.value()); // logs 0
counter.increment();
counter.increment();
console.log(counter.value()); // logs 2
counter.decrement();
console.log(counter.value()); // logs 1
```

설계를 보면 알겠지만 counter가 반환받은 객체의 함수를 통해서만 지역변수인 `privateCounter`에 접근 가능하다.   
(지역변수인 `privateCounter`심볼을 메모리에서 해재되지 않을까?)  

> 출처: https://developer.mozilla.org/ko/docs/Web/JavaScript/Guide/Closures#클로저를_이용해서_프라이빗_메소드_(private_method)_흉내내기

이런 방식으로 클로저를 사용하는 것을 **모듈 패턴**이라 한다.

위에선 객체를 반환하는 함수를 선언과 동시에 실행시켜 객체를 반환시켰지만  
이번엔 객체를 반환하는 함수를 `makeCounter`라는 변수에 저장하고 `makeCounter`를 통해 객체를 만들도록 하자.
```js
var makeCounter = function() {
  var privateCounter = 0;
  function changeBy(val) {
    privateCounter += val;
  }
  return {
    increment: function() {
      changeBy(1);
    },
    decrement: function() {
      changeBy(-1);
    },
    value: function() {
      return privateCounter;
    }
  }  
};
var counter1 = makeCounter();
var counter2 = makeCounter();
alert(counter1.value()); /* 0 */
counter1.increment();
counter1.increment();
alert(counter1.value()); /* 2 */
counter1.decrement();
alert(counter1.value()); /* 1 */
alert(counter2.value()); /* 0 */
```

### 클로저 - Progress Bar

`div`태그 2개와 스타일 시트를 사용해 다음과 같은 Progress Bar 생성  

```css
<style>
  .myProgress {
    width: 100%;
    background-color: #ddd;
  }

  .myBar {
    width: 1%;
    height: 30px;
    background-color: #4caf50;
    text-align: right;
    color: red;
  }
</style>
```
```html
<h3>Progress Bar1</h3>
<div id="myProgress1" class="myProgress">
  <div id="myBar1" class="myBar"></div>
</div>
<button id="btn1">진행</button>

<h3>Progress Bar2</h3>
<div id="myProgress2" class="myProgress">
  <div id="myBar2" class="myBar"></div>
</div>
<button id="btn2">진행</button>

<h3>Progress Bar3</h3>
<div id="myProgress3" class="myProgress">
  <div id="myBar3" class="myBar"></div>
</div>
<button id="btn3">진행</button>
```

![js10]({{ "/assets/web/js/js10.png" | absolute_url }}){: .shadow}{: .align-right}


`setTimeout`메서드와 클로저를 사용해 각각의 프그레스바가 각각의 변수를 가지고 증가되도록 설정해보자.  

각 버튼에 클로저 함수가 포함된, 각각의 width를 가질수 있는 이벤트 처리 함수를 등록,  
0.1초에 width가 1%씩 증가되며 width가 다 증가하면 `clearTimeout`를 호출해 재귀호출을 막는다.  
```js
var buttons = document.getElementsByTagName("button");
var myBars = document.getElementsByClassName("myBar");

for (let i = 0; i < buttons.length; i++) {
  buttons[i].onclick = progressBar(myBars[i]);
}

function progressBar(myBar) {
  var width = 1;
  var bar = myBar;
  return function innerfunc() {
    if (width >= 100)
      clearTimeout(timer);
    else {
      width++;
      bar.style.width = width + "%";
      bar.innerHTML = width + "%";
    }
    var timer = setTimeout(innerfunc, 30);
  }
}
```
![js11]({{ "/assets/web/js/js11.png" | absolute_url }}){: .shadow}{: .align-right}
