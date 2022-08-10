---
title:  "JavaScript 변수!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - javaScript

tags:
  - javaScript

toc: true
toc_sticky: true

---

## 읽을거리 - 자바 스크립트 시리즈

> 자바스크립트 엔진은 V8(크롬과 Node), SpiderMonkey(파이어폭스), JavaScriptCore(사파리) 등 다양한 종류가 있으며, 모든 자바스크립트 엔진은 ECMAScript 사양을 준수한다

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

> http://hacks.mozilla.or.kr/category/es6-in-depth/
ES2016에 대한 설명

## 변수 개요

```js
var x = 100;
```

리터럴값 `100` 을 변수 `x` 가 참조하는 변순선언 및 할당문  

JS 에선 밑의 hoisting 으로 인해 위와같이 변수 선언 할당 한줄처리도  
`x` 가 `undefined` 으로 설정 후 `100` 으로 할당된다.  

> literal: 정확한

변수타입은 아래와 같다.  

- `number` - 숫자를 모두 실수로 표현, 64bit 부동소수점 형식  
- `string`  
- `boolean` - `true`, `false` 
- `undefined` - var 에 설정된 암묵적인 값  
- `null` - 의도적으로 값이 없음을 명시, 가비지콜렉터 호출예약  
- `symbol` - 변경 불가능한 원시 타입의 값
- `object` - 객체, 함수, 배열 등  


```js
console.log(typeof "admin");       //string
console.log(typeof 10);            //number
console.log(typeof 3.14);          //number
console.log(typeof true);          //boolean
console.log(typeof NaN);           //number
console.log(typeof [1, 2, 3, 4]);  //object
console.log(typeof null);          //object
console.log(typeof {name: "hong", age: 20});  //object
console.log(typeof new Date());    //object
console.log(typeof function(){});  //function
```

### hoisting 

> `hoisiting`: 끌어 올리기
 
실행 컨텍스트 생성시 `var, function, class` 같은  
일부 키워드를 먼저 읽어 변수를 미리 생성한다.  

```js
x = 20;
console.log(x);
var x = 10;
var x = 20;
```

```js
console.log(multi(1, 2));

function multi(a, b) {
    return a * b;
}
```

`ES2015` 등장 전까진 `var` 키워드로 변수생성을 자주 했었다.  
`var` 는 `hoisting` 가능한 키워드이기도 하고 중복선언 스코프에서도 어느정도 자유로운면이 있어 편하지만 혼란을 주는 키워드라 권장하지 않는다.  

이를 막고싶다면 `use strict` 키워드를 사용  
> Strict mode: <https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Strict_mode>  

```js
'use strict';
var v = "Hi!  I'm a strict mode script!";
```

> `use strict` 사용은 권장하지 않는다, 라이브러리별로 strict mode 가 다를 수 있으며 같은 JS 파일 안에서 strict mode 가 변경되면서 발생하는 오류가 있을 수 있다.  
> hosting 의존적인 코드를 제거하는 것을 권장한다.  

### 원시타입(primitive type)

원시타입의 값들은 **변경 불가능한 값(immutable value)** 이다.  

JS 에선 원시타입을 메모리공간에 할당해서 read 만 할 수 있을 뿐  
같은 메모리 공간에 다른 원시타입을 덮어씌우거나 할 수 없다  

원시 값을 할당한 변수는 **재할당 이외에 변수 값을 변경할 수 있는 방법이 없다**

```js
var score = 80;
var copy = score;

console.log(score); // 80
console.log(copy);  // 80
```

또한 `score`, `copy` 변수는 같은 메모리 위치의 원시타입을 참조할 것 같지만  
다른 메모리 공간에 저장된 별개의 값이다.  

![1](/assets/javascript/image1.png)

> python 의 경우 같은 위치의 원시타입을 참조함  

## 스코프

`ES2015` 이후에 추가되었으며 아래 3가지 스코프 존재

- `global scope`  
- `local scope` - `function scope` 로 부르기도 함, `var` 변수가 종속됨  
- `block scope` - `let` 변수가 종속됨  

> `local` 이 `block` 과 `function` 을 포함한다고 소개하는 글도 있음  

`let`, `const` 키워드를 사용해 **블록범위** 변수를 선언한다.  

전역과 지역 범위는 아래와 같다.  

```js
// global scope
var name = "hong gil dong"; 

function test() {
    // local scope
    var age = 10
    console.log(name);
}
console.log(age) // Uncaught ReferenceError: age is not defined

{
    // block scope
    var a = 100;
}

for (var i = 0; i < 10; i++) {
    // block scope
    var leak = "Hello world" 
}
```

### 스코프 체인

```js
var x = "global x"
var y = "global y"
function outer() {
    var z = "outer's local z";
    console.log(x); // global x
    console.log(y); // global y
    console.log(z); // outer's local z

    function inner() {
        var x = "inner's local x";
        console.log(x); // inner's local x
        console.log(y); // global y
        console.log(z); // outer's local z
    }
    inner();
}
outer();
console.log(x); // global x
console.log(z); // ReferenceError: z is not defined
```

위와 같이 중복함수가 정의된 상태에서 `global scope` 와 `local scope` 변수를 찾아가는 조건이 있다.  

`outer` 함수와 `inner` 함수는 모두 자신만의 `local scope` 를 가지고 있는데  
`하위 local scope -> 상위 local scope -> global scope` 방향으로 흐른다.  

### lexical 스코프

- `dynamic scope`: 함수 호출시점에 상위 스코프 결정  
- `lexical scope`: `static scope` 라 하기도 하며 함수 정의 위치에 따라 상위 스코프 결정  

JS 는 `lexical scope` 이고 대부분의 언어가 `lexical scope` 이다.  

```js
var x = 1;

function foo() {
    var x = 10;
    bar();
}

function bar() {
    console.log(x);
}

foo() // 1
bar() // 1
```

`bar` 의 정의위치가 `foo` 의 내부(중첩함수)가 아니기에 상위 스코프는 `global scope`, 가장 외부의 x 를 참조한다.  

## 변수 키워드

JS 에서는 별도의 변수타입 없이 var, let, const 등으로 바로 변수 생성한다.  

### var

`var` 변수는 `local scope` 에 종속된다.  

아래처럼 `block scope` 에서 정의될 경우 종속되지 않아 **전역변수로 정의된다(암묵적 전역)**.  

```js
for (var i = 0; i < 10; i++) {
    // block scope
    var leak = "Hello world" 
}
console.log(leak); // "Hello world" 
```

`for` 의 인덱스용 변수 `var i` 도 전역변수로 등록되어 오류를 야기할 수 있다.  

`local scope` 에 정의될 경우 그 안에서만 사용되고 밖으로 나올순 없다.  

```js
function test() {
    // block scope
    var leak = "Hello world" 
}
console.log(leak) // ReferenceError: leak is not defined
```

`var` 의 경우 아래처럼 중복정의될 가능성이 있고  
실수로 전역변수로 정의될 수 있음으로 사용자체를 권장하지 않는다.  

```js
var test = 10
var test = 20

console.log(test)
```

### let

`let` 은 `block scope` 에 종속된다.  
아래처럼 블록내부에서 정의되면 블록 내부에서만 사용 가능하다.  

```js
{
    var a = 100;
    let b = 100;
}
console.log(a); //100
console.log(b); //Uncaught ReferenceError: b is not defined
```

하위블록에서 사용가능하고 변경도 가능하다.  

같은스코프에서는 중복정의가 불가능하며  
블록 레벨이 다를경우 재정의 가능하다.  

하지만 블록내부에서 재정의할 경우 기존 `let` 변수를 변경하는 것이 아닌 새로운 변수를 생성하기에 밖의 let 변수와는 상관이 없어진다.  

```js
let x = 'global'
if(x=='global') {
    console.log(x); // global
}
if(x=='global') {
    let x = 'block';
    console.log(x); // block
}
console.log(x); // global
```

`let`을 사용하면 다음과 같은 논리오류를 방지할 수 있다.  

```js
var i = 5;
for (var i = 0; i < 10; i++) {

}
console.log(i); //10 출력. 

var j = 5;
for (let j = 0; j < 10; j++) {
  
}
console.log(j); //5 출력
```

`let` 은 `hoisting` 대상이지만 미리 사용하는것이 불가능하다. 이 상황을 TDZ(temporal dead zone) 이라 한다.  

```js
console.log(num) // ReferenceError: Cannot access 'num' before initialization
let num = 10 
```

### const

대부분 `let` 과 비슷하나  
`const` 로 변수를 선언할 경우 값이 변경되지 않도록 고정 가능하다.  

```js
const num1 = 10;
num1 = 20; // TypeError: Assignment to constant variable.
```

### var vs. let vs. const

개인적으로 생각하는 변수 키워드 우선순위  

- 기본적으로 const 를 사용하자  
- 작은 스코프의 로컬 변수에는 let 을 사용한다  
- 재할당이 필요한 경우에만 let 을 사용하자  
- 코드 작성이 어느 정도 진행된 후에만 let 을 const 로 리팩터링한다. 변수 재할당을 막아야 하는 경우라는 것이 확실해야 한다  
- var 는 ES6 에서 절대 사용하지 않는다  
- 전역변수 생성은 고심끝에(메모리 해재되지 않음)  

## 타입 변환 

### 명시적 타입변환(explicit coercion)

타입캐스팅(type casting) 이라고도 하며 타입변환을 코드로 명시하는 것  

```js
// 1. String 생성자 함수를 new 연산자 없이 호출하는 방법
// 숫자 타입 => 문자열 타입
String(1);        // -> "1"
String(NaN);      // -> "NaN"
String(Infinity); // -> "Infinity"
// 불리언 타입 => 문자열 타입
String(true);     // -> "true"
String(false);    // -> "false"

// 2. Object.prototype.toString 메서드를 사용하는 방법
// 숫자 타입 => 문자열 타입
(1).toString();        // -> "1"
(NaN).toString();      // -> "NaN"
(Infinity).toString(); // -> "Infinity"
// 불리언 타입 => 문자열 타입
(true).toString();     // -> "true"
(false).toString();    // -> "false"

// 3. Number 생성자 함수를 new 연산자 없이 호출하는 방법
// 문자열 타입 => 숫자 타입
Number('0');     // -> 0
Number('-1');    // -> -1
Number('10.53'); // -> 10.53
// 불리언 타입 => 숫자 타입
Number(true);    // -> 1
Number(false);   // -> 0

// 4. parseInt, parseFloat 함수를 사용하는 방법(문자열만 변환 가능)
// 문자열 타입 => 숫자 타입
parseInt('0');       // -> 0
parseInt('-1');      // -> -1
parseFloat('10.53'); // -> 10.53
```


### 암묵적 타입변환(implicit coercion)

위의 두가지 상황에서 암묵적 타입변환에 대해 알아본다.  


#### to string

```js
// 숫자 타입
console.log(0 + '')         // -> "0"
console.log(-0 + '')        // -> "0"
console.log(1 + '')         // -> "1"
console.log(-1 + '')        // -> "-1"
console.log(NaN + '')       // -> "NaN"
console.log(Infinity + '')  // -> "Infinity"
console.log(-Infinity + '') // -> "-Infinity"

// 불리언 타입
console.log(true + '')  // -> "true"
console.log(false + '') // -> "false"

// null 타입
console.log(null + '') // -> "null"

// undefined 타입
console.log(undefined + '') // -> "undefined"

// 심벌 타입
// console.log((Symbol()) + '') // -> TypeError: Cannot convert a Symbol value to a string

// 객체 타입
console.log(({}) + '')           // -> "[object Object]"
console.log(Math + '')           // -> "[object Math]"
console.log([] + '')             // -> ""
console.log([10, 20] + '')       // -> "10,20"
console.log((function(){}) + '') // -> "function(){}"
console.log(Array + '')          // -> "function Array() { [native code] }"
```

#### to number

```js
// 문자열 타입
console.log(+'')       // -> 0
console.log(+'0')      // -> 0
console.log(+'1')      // -> 1
console.log(+'string') // -> NaN

// 불리언 타입
console.log(+true)     // -> 1
console.log(+false)    // -> 0

// null 타입
console.log(+null)     // -> 0

// undefined 타입
console.log(+undefined) // -> NaN

// 심벌 타입
// console.log(+Symbol()) // -> TypeError: Cannot convert a Symbol value to a number

// 객체 타입
console.log(+{})             // -> NaN
console.log(+[])             // -> 0
console.log(+[10, 20])       // -> NaN
console.log(+(function(){})) // -> NaN
```

#### Truthy, Falsy

대부분의 변수들은 `if` 문에서 사용되면 암시적으로 `true` 혹은 `false` 로 변환하게 된다.  
이를 `Truthy`, `Falsy` 한 변수라 칭한다.  

아래는 `false` 로 암시적 변환되는 변수타입, 값 들이다.  

```js
console.log(Boolean(false))     // false
console.log(Boolean(undefined)) // false
console.log(Boolean(null))      // false
console.log(Boolean(0))         // false
console.log(Boolean(NaN))       // false
console.log(Boolean(''))        // false
```

반대로 `true` 로 암시적 변환되는 값

```js
console.log(Boolean(1))     // true
console.log(Boolean('str')) // true
```

