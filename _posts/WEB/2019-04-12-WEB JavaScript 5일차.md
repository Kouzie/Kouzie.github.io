---
title:  "Web - JavaScript 5일차 - 참/거짓, 예외처리, 블럭범위!"

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


## JavaScript에서 참/거짓

JavaScript에선 참/거짓을 판별하는 `Boolean()`메서드와 `Boolean`객체, `boolean`자료형이 있다.  

```js
var b1 = new Boolean(false); //객체
var b2 = false; //자료형 boolean
```

`Boolean()` 메서드를 사용해 변수안의 내용이 true에 해당하는지, false에 해당하는지 알 수있다.  
(if문 안써도 되서 간편!)  

```js
console.log(Boolean(0)); //false
console.log(Boolean("")); //false
console.log(Boolean("0")); //true

var x;
var y = null;
console.log(Boolean(x)); //false
console.log(Boolean(null)); //false
console.log(Boolean(NaN)); //false

console.log(1 < 12); //true
console.log(1 < "12");  //true
console.log(1 < "a"); //false
console.log(100 < "a"); //false
console.log("100" < "12"); //true
```

숫자로된 문자열과 `number`를 비교할 땐 문자열이 `number`로 자동 형변환 되어 비교한다.  
숫자가 아닌 문자열과 `number`를 비교할땐 ASCII와 상관없이 무조건 `false`반환.  

숫자로된 문자열끼리 비교할 때에는 수의 크기를 비교하지 않고 ASCII 순으로 비교한다.

## JavaScript에서 예외처리

타 언어와 같이 `try, catch, finally`문을 사용한다.  

```html
<input type="text" id="num">
<button onclick="exec();">click</button>
<p id="demo"></p>
<script>
function exec() {
    var num = document.getElementById("num").value
    try {
        if (num == "")
            throw "empty"; //예외 발생시킴
        if (isNaN(num))
            throw "not a number"; //예외 발생시킴
        if (!(0 <= num && num <= 100))
            throw "out of range num";

        document.getElementById("demo").innerHTML = num;
    }
    catch (err) {
        document.getElementById("demo").innerHTML = err;
    }
    finally {

    }
}
```
만약 `input`태그 내용이 if조건문에 걸린다면 `throw`를 통해 예외가 발생하고 `p`태그에 출력된다.  

## JavaScript hoisiting

변수선언을 어디서 하던 **브라우저가 알아서** 맨 위로 끌어 올려 위에서 선언된것 처럼 해석한다. 선 사용 후 선언이 가능!  
이를 막고싶다면 `"use strict"` 키워드를 사용
```js
x = 20;
console.log(x);
var x = 10;
```
x는 전역변수로 선언된 것이 아닌 `hoisiting`을 사용한것,(권장하지 않는다)  

이는 변수 뿐 아니라 함수에도 적용된다.  
```js
//함수 호출을 선언보다 먼저해도 상관 없다 (hoisting)
console.log(multi(1, 2)); //정상 출력됨

function multi(a, b) {
    return a * b;
}
```



## JavaScript 변수 - 지역, 전역, 블럭

JavaScript에는 지역범위(local scope), 전역범위(global scope) 이외에 블록범위(block scope) 개념이 있다.  
ECMA SCRIPT 2015 이후에 추가되었으며 `let`, `const` 키워드를 사용해 블록범위 변수를 선언한다.  

```js
var name = "hong gil dong"; //<--global scope

function test() {
    var age // <--local scope
    alert(window.name);
}
```
우리가 JavaScript에서 알고있는 전역과 지역 범위는 위와 같다. 전역은 어디서든 사용 가능하지만 지역변수는 소속된 블럭 밖으로 빠져나가 사용하려 하면  `Uncaught ReferenceError: age is not defined` 오류를 반환한다.  



`functoin {}` 안에 선언된 변수는 지역변수, 밖에 선언될 경우 전역변수이다. 그리고 전역변수는 `window`최상위 객체의 멤버로 소속된다.


위는 함수안의 지역변수 설명이었고 새로 추가된 블록범위 변수는 다음과 같다.
```js
{
    var a = 100;
    let b = 100;
}
console.log(a); //100
// console.log(b); //Uncaught ReferenceError: b is not defined
```
함수 `{}` 괄호 안이 아닌 그냥 블록 안에 선언 및 초기화된 변수이다. `var`, `let`키워드를 사용해서 선언 하였다.  
둘의 차이점은 출력값 처럼 블럭범위 밖에서 살아남느냐 살아남지 못하느냐.  

이 외에도 다른점이 더 있다.  

```js
var x = 400;
{
    console.log(x); //400
    var x = 300;
    console.log(x); //300
}
console.log(x); //300
```
JavaScript에선 변수 재선언이 가능하기에 블럭 밖에서 재선언하던 안에서 하던 상관 없다. 그저 새로 선언된 변수에 의해 덮어 씌어질 뿐이다.  
> 허용하고 싶지 않다면 `"use strict"` 키워드 사용  

하지만 `let`으로 선언된 변수를 사용한다면 재선언 할 수 없다.  
```js
var y = 200;
{
    // console.log(y); //Uncaught ReferenceError: y is not defined
    let y = 300;
    console.log(y); //300
}
// console.log(y); //Uncaught ReferenceError: y is not defined
//let으로 인해 전역변수였던 y는 사라져 버린다.
```
위처럼 블럭 안에는 이미 `let`으로 y변수를 선언하였기 때문에 더이상 같은 이름의 변수는 사용 불가능하다. 
> let은 `JavaScript hoisiting` 되지 않는다.  

또한 블록 밖으로 빠져나오면 y는 더이상 존재하지 않는 변수이다.  

같은 이름으로 `let`변수를 선언하려면 다른 블록 범위에 각각 선언해야 한다.  
```js
let z = 3;
{
    let z = 4;
}
```



`let`을 사용하면 다음과 같은 논리오류를 방지할 수 있다.  
```js
var i = 5;
for (var i = 0; i < 10; i++) {

}
console.log(i); //10이 출력됨. 

var j = 5;
for (let j = 0; j < 10; j++) {

}
console.log(j); //5
```
for문안의 `let j`는 블록안에 별도 공간에서 사용되기 위해 선언된 변수로 for문 밖의 `j`와 전혀 관계 없다.  

### const  

`const`는 `let`과 똑같은 개념이지만  
**선언과 초기화를 한번에** 해야하고 그이후에 값은 바뀔 수 없다.  

<br><br>


## JavaScript this

`this`키워드는 총 4가지 방법으로 쓰인다.
자기자신을 가리키는 키워드로 어디서 사용되냐에 따라 가리키는 객체가 다르다.  

### 메서드에서 this 사용

메서드에서 `this`키워드는 해당 메서드를 소유한 객체와 같다.  

```js
var person = {
    name: "admin",
    age: 20,
    info: function () {
        return this.name + "/" + this.age;
    }
}; //object
console.log(person.name); //admin
console.log(person["age"]); //20
console.log(person.info()); //admin/20
```
함수를 가리키는 info변수에 들어간 함수에서 사용되는 this는  
함수를 소유한 객체 `person`을 가리킨다.  


### 최상위 객체(전역객체) this 사용

`Obejct` 안에서 함수를 정의한 것 이 아닌 그냥 `<script>` 태그 안에서 정의한 함수의 경우 최상위 객체인 `window` 소속의 함수이다.  
해당 함수안에서 사용되는 `this`는 최상위 객체인 `window`를 가리킨다.  

로 사용할 수 있음.
```js
var x = 10;
console.log(x);
console.log(window.x);
console.log(this.x);

function this_test() {
    return this;
}
console.log(this_test()); //Window {postMessage: ƒ, blur: ƒ, focus: ƒ, close: ƒ, parent: Window, …}
console.log(window); //Window {postMessage: ƒ, blur: ƒ, focus: ƒ, close: ƒ, parent: Window, …}
```

> `"use strict"` 키워드를 사용하면 this로 window 객체를 가리키는 것이 불가능해진다. 가리킨다 하더라도 `undefined`출력된다.  


### 태그안에서 자기자신을 가리키는 this 사용

이벤트를 수신/호출한 요소를 가리키는 용도로 `this`키워드를 사용가능 하다.  

```html
<button onclick="this.style.color = 'red'">click</button>
```

