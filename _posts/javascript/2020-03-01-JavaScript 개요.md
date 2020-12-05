---
title:  "JavaScript ES2015!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - HTML

tags:
  - web
  - html
  - JavaScript

toc: true
toc_sticky: true

---

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

> http://hacks.mozilla.or.kr/category/es6-in-depth/
ES2016에 대한 설명


## JavaScript 키워드

### const, var, let

`ES2015` 등장 전까진 JavaScript 에서 `var` 키워드로 변수생성을 자주 했었다.  
`var`는 `hoisting` 한 키워드라 편하기도 하지만 혼란을 주는 키워드이다.  

> JavaScript hoisiting (끌어 올리기): 실행 컨텍스트 생성시 `var`, `function` 과 같은 일부 키워드를 먼저 읽어 변수를 미리 생성한다.  


```js
x = 20;
console.log(x);
var x = 10;
```

x는 전역변수로 선언된 것이 아닌 `hoisiting`을 사용한것(권장하지 않는다)  
이는 변수 뿐 아니라 함수에도 적용된다.  

```js
//함수 호출을 선언보다 먼저해도 상관 없다 (hoisting)
console.log(multi(1, 2)); //정상 출력됨

function multi(a, b) {
    return a * b;
}
```
이를 막고싶다면 `"use strict"` 키워드를 사용, 파일 상단에 `"use strict";` 작성한다.  
> use strict (Strict mode): https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Strict_mode


### local, global, block

JavaScript 에는 지역범위(`local scope`), 전역범위(`global scope`) 이외에 블록범위(`block scope`) 개념이 있다.  

`ES2015` 이후에 추가되었으며 `let`, `const` 키워드를 사용해 **블록범위** 변수를 선언한다.  

```js
var name = "hong gil dong"; //<--global scope

function test() {
    var age // <--local scope
    alert(window.name);
}
// alert(age) Uncaught ReferenceError: age is not defined
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

`function` 내부가 아닌 그냥 블록 `{ }` 안에 `var`, `let`키워드를 사용해 변수를 선언 및 초기화 하였다.   

둘의 차이점은 출력값 처럼 블럭범위 밖에서 살아남느냐 살아남지 못하느냐.  

`var`는 살아남지만 `let`은 블록변수라 블록 밖에선 사용하지 못한다.  

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
위처럼 블럭 안에는 이미 `let`으로 `y`변수를 선언하였기 때문에 더이상 같은 이름의 변수는 사용 불가능하다. 
> let은 `JavaScript hoisiting` 되지 않는다.  

또한 블록 밖으로 빠져나오면 `y`는 더이상 존재하지 않는 변수이다.  

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
console.log(i); //10 출력. 

var j = 5;
for (let j = 0; j < 10; j++) {
  
}
console.log(j); //5 출력
```

for문안의 `let j`는 블록안에 별도 공간에서 사용되기 위해 선언된 변수로 for문 밖의 `j`와 전혀 관계 없다.  

### try, catch, finally

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

## this

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Operators/this

객체지향 언어에서 `this`는 대부분 해당 메서드를 정의한 객체를 가리킬 때 사용하는 키워드이다.  

JavaScript에선 조금 다른데 메서드의 실행 문맥(호출한 객체) 따라 `this` 가 가리키는 값이 달라진다.  

### 메서드에서 `this` 사용

메서드에서 `this`키워드는 해당 메서드를 호출한 객체와 같다.  

`<script>` 태그 바로 아래에 메서드 정의, 호출한다면 `window` 객체아래에 메서드를 정의하고 호출한 경우이다.  

이 경우 메서드 내부의 `this` 는 해당 메서드를 호출한 `window` 를 가리킨다.

단 엄격모드(`use strict`)에선 철저히 실행 컨텍스트를 지정해 줘야 하기에  
별도로 함수 앞에 실행 컨텍스트를 지정하지 않으면 `this`는 아무 객체도 가리키지 않는다.  

```html
<script>
  function f1() {
      console.log(this);
  }
  function f2() {
      "use strict";
      console.log(this);
  }
  f1(); //window 객체
  f2(); //undefined
  window.f2(); //window 객체
</script>
```

객체 내부에 메서드를 정의하고 사용하는 경우가 대부분이기에  
당연히 `this` 는 해당 메서드를 정의한 객체를 가리키는 것 같지만 실제로는 **해당 메서드를 호출한 객체**를 가리킨다.

```js
var person = {
    name: "admin",
    age: 20,
    info: function () {
        return this.name + "/" + this.age;
    }
}; //object
console.log(person.name);   //admin
console.log(person["age"]); //20
console.log(person.info()); //admin/20
```

위의 `this` 도 `person` 정의했기 때문이 아닌 `person` 이 호출했기 때문에 `this` 가 `person` 을 가리키는 것.  


## Object(객체)

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Guide/Working_with_Objects

> 객체는 프로퍼티의 모음, 프로퍼티는 `key`, `value` 의 연결로 이루어진다.  

자바스크립트 세상에서는 거의 모든 것들이 객체이다. `null` 과 `undefined` 를 제외한 모든 원시 타입도 객체로 취급된다.  
이러한 원시 타입들에도 프로퍼티가 추가될 수 있고 모두 객체로서의 특징을 갖는다.

객체의 종류에는 여러가지가 있다. 직접만들 수 도 있고 JavaScript 에 이미 존재하는 객체또한 많다.

* `Date`  
* `Array`  
* `String`  
* `Number`  
* `Boolean`   
* `Function`  
* `Regex`  

등등...  

### 객체를 생성하는 방법 0 - `{ }` 리터럴을 사용한 객체생성  

`var s1 = {name: "홍길동", age: 12}`  

JavaScript에선 **필드(프로퍼티) 초기화**를 위해 `:`콜론을 사용한다.  

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
사실 `print` 함수 정의보다는 `print`라는 함수 `Object`변수를 생성하고 `Function` 객체를 참조한 것이 더 맞는 듯 하다.  

`Object`안의 프로퍼티(`Property`) 출력하는 방법은 2가지, 
`.`뒤에 바로 프로퍼티명을 붙여 쓰는 방법과 `[]`를 사용해서 출력하는 방법이 있다.  

코드 `new Foo(...)`가 실행될 때 다음과 같은 일이 발생한다  

1. `Foo.prototype`을 상속하는 새로운 객체가 하나 생성된다.  
2. 명시된 인자 그리고 새롭게 생성된 객체에 바인드된 this와 함께 생성자 함수 `Foo`가 호출된다. `new Foo`는 `new Foo()`와 동일하다. 즉 인자가 명시되지 않은 경우, 인자 없이 `Foo`가 호출된다.  
3. 생성자 함수에 의해 리턴된 객체는 전체 `new` 호출 결과가 된다. 만약 생성자 함수가 명시적으로 객체를 리턴하지 않는 경우, 첫 번째 단계에서 생성된 객체가 대신 사용된다.(일반적으로 생성자는 값을 리턴하지 않는다. 그러나 일반적인 객체 생성을 재정의(override)하기 원한다면 그렇게 하도록 선택할 수 있다.)  


### 객체를 생성하는 방법 1 - `new` 키워드

객체를 생성하는 방법은 `var obj = {}` 방식(리터럴) 외에도 여러가지 있다.  

> `var obj = new Object();`  

`new` 키워드를 통해 `Obejct()` 라는 내장객체 타입의 인스턴스를 생성한다 생각하면 된다.  


```js
var person = new Object();
person.firstName = "Hong";
person.lastName = "gildong";
person.age = "Hong";
person.print = function() {
  return this.firstName + " " + this.lastName;
};
```

인스턴스 생성후 `property`를 하나씩 추가해 나간다.  

> 선언만하고 초기화 하지 않는경우 문법오류를 반환한다(애초에 초기화 하지 않은 프로퍼티는 필요 없다).  

프로퍼티를 삭제하고 싶다면 `delete`키워드를 사용한다.  
`delete person.age;`  

> 사실 `var obj = {};`과 `var obj = new Object();`은 똑같은 방법, 모든 객체를 만들때에는 JavaScript에 이미 정의되어 있는 함수를 통해 만들어진다.

### 객체를 생성하는 방법 2 - 생성자 메서드

메서드를 호출할 때 마다 객체를 반환하는 생성자 역할을 하는 생성자 메서드를 정의한다.  
`this`키워드를 사용해  생성된 객체에 프로퍼티를 정의하고 초기화한다.  

> 생성자 함수에선 맨 앞의 문자를 대문자로 쓸 것 을 권장한다.  

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

생성자 함수도 일반 함수이기 때문에 `this.name`은 최상위 객체인 `window`의 `name`이라는 전역변수를 정의하고 초기화 하는 문법이 되어버린다.  


때문에 함수앞에 `new`키워드 를 붙여 객체를 생성하고 `new` 연산자는 사용자 정의 객체 타입 또는 내장 객체 타입의 인스턴스를 생성한다.  
`this`키워드는 해당 객체의 공간을 나타낸다.  

생성자 메서드로 인해 만들어진 객체들은 JavaScript의 다른 객체들처럼 프로퍼티를 삭제하거나, 추가, 재정의 할 수 있다.  

함수앞에 `new`키워드를 붙이는개 생소하지만 문법으로 받아들이자.  

### 객체를 생성하는 방법 3 - `Object.create`

`Object.create(proto[, propertiesObject])`

## Prototype (원형)

> https://medium.com/@bluesh55/JavaScript-prototype-이해하기-f8e67c286b67

JavaScript는 프로토타입 기반 객체지향 프로그래밍 언어이다.  

위의 생성자 함수를 통해 클래스 비슷하게 흉내내었었다.  
`Porototpye`을 사용하면 상속과 비슷한 효과에 더불어 `static` 변수와 같은 공용 변수를 정의할 수 있다.  

```js
function Person(name, age, color) {
  this.name = name;
  this.age = age;
  this.color = color;
}

Person.prototype.print = function() {
  return this.name +" / "+ this.age +" / "+ this.color;
}

var hong = new Person("hong", 25, "red");
console.log(hong.print());
// hong / 25 / red
```

위에서 정의했던 `Person`의 `print`메서드를 모든 객체가 공유하는 하는 함수로 정의, 효율적인 메모리 관리도 가능하다.  

이런 설계가 가능한 이유는 `Person()`이라는 생성자 함수자체에 `prototype`객체가 생성되기 때문  

chrome debug 창으로 `hong`의 값을 출력하였다.  

```js
Person {name: "hong", age: 25, color: "red"}
  name: "hong"
  age: 25
  color: "red"
  __proto__:
    print: ƒ ()
    constructor: ƒ Person(name, age, color)
    __proto__:
      constructor: ƒ Object()
      __defineGetter__: ƒ __defineGetter__()
      __defineSetter__: ƒ __defineSetter__()
      hasOwnProperty: ƒ hasOwnProperty()
      __lookupGetter__: ƒ __lookupGetter__()
      __lookupSetter__: ƒ __lookupSetter__()
      isPrototypeOf: ƒ isPrototypeOf()
      propertyIsEnumerable: ƒ propertyIsEnumerable()
      toString: ƒ toString()
      valueOf: ƒ valueOf()
      toLocaleString: ƒ toLocaleString()
      get __proto__: ƒ __proto__()
      set __proto__: ƒ __proto__()
```

`hong` 인스턴스 내부에 `__proto__` 객체가 저장되어있고 위에서 정의한 `print` 메서드외에  
`__proto__`, `constructor`를 가진다.  

`constructor`는 `hong` 인스턴스 생성시 사용했던 생성자 함수가 저장되어있다.  
`__proto__`는 출력된 내용으로 보아 `Object` 객체를 가리키는 것 같다.  

JavaScript 에선 이 프로토타입 객체를 통해 객체지향을 지원한다.  

위에서도 모든 객체의 상위객체인 `Obejct`가 `hong.__proto__.__proto__` 를 통해 참조되고 있다.  

### Prototype Link, Prototype Chain

그렇다면 `function Person` 으로 생성된 인스턴스 `hong` 의 `proto` 객체는 어떤걸 가리키고 있는지 알아보자.  

![js14-1](/assets/web/js/js14-1.png){: .shadow}  

`Person` 메서드 객체의 `prototype`객체를 `hong`의 `__proto__` 객체가 가리킨다.  

![js14](/assets/web/js/js14.png){: .shadow}  

즉 `function Person(...) {...}` 이라는 메서드를 정의하면 위와 같은 그림이 형성된다.  
그리고 `new Person(...)` 으로 생성된 모든 인스턴스는 `Person` 메서드 객체의 `prototype`을 `__proto__` 객체를 통해 공유하게 된다.  

`__proto__` 객체가 생성자 함수의 `prototype`객체에 접근하도록 해주는 열쇠같은 녀석이다.  

> `property` 속성은 `Finction` 객체만 가지고 있으며 일반 객체는 `__property__` 속성만 가지고있다.

생성자 함수로 생성되던, 리터럴로 생성되던 모든 `Object`의 하위객체는 `__proto__`객체를 멤버로 가지고 있다.  

```js
var hong = new Person("Hong", 30, "blue");
console.log(hong.__proto__)

var kim = {
  name: "kim",
  age: 25,
  color: "red"
}
console.log(kim.__proto__);
```

![js15](/assets/web/js/js15.png){: .shadow}  

리터럴로 생성된 `kim`객체또한 `Object()`함수를 통해 만들어진 것이기 때문에 `__proto__`는 `Object` 생성자 메서드의 `prototype`을 가리킨다.  

생성자 함수로 만들어진 객체이건, 리터럴로 만들어진 객체이건 `Prototype`을 통해 위로 올라가면 `Object`객체가 나온다. 
이를 통해 `Object` 생성자 메서드의 `prototype`에 정의된 메서드들을 `kim`, `hong`과 같은 인스턴스가 체인처럼 연결된 프로토 체인을 통해 접근할 수 있다.  

> 아무래도 `Object()`멤버의 `prototype`객체에 기본적으로 정의된 전역적 함수, 변수가 우리가 정의한 `Person()`보단 많다.  

이렇게 `__proto__` 객체를 통해 `하위->상위`로 객체간 연결하는 것을 `Prototype Link` 라 한다.  

어쨋건 `hong.print()` 형식으로 메서드를 호출할 수 있는 이유는 먼저 `print`메서드가 자신에게 있는지 탐색하고 없다면 `__proto__`에 연결된 생성자 함수의 `prototype`객체에 `print`가 정의되어 있는지 탐색하기 때문  

JavaScript 는 모두 객체로 이루어져 있으며 메서드객체 `Function` 또한 `Object` 객체의 하위객체이다.  

`new Function()` 문법이 기억 나는가?  

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/Function  

사실 `function test(){...}` 메서드 정의는 위의 `var test = new Function(...)` 로 객체 생성한 것 과 같다.(완벽히 같지는 않음)  
즉 `test()`는 `Function()` 이라는 생성자 함수로 만들어진 일종의 `Obejct`의 하위 객체 `Function`이다.  

`__proto__`객체는 모든 객체가 가지고 있기때문에 `test`객체또한 가지고 있다.  

```js
function test() {};
console.log(test.__proto__);
```

`Person` 객체 `hong` 의 `__proto__` 가 `Person.prototype` 을 가리키듯이  
`Function` 객체 `test`의 `__proto__` 는 `Function.prototype`을 가리킨다.  

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Operators/new

만약 `input`태그 내용이 if조건문에 걸린다면 `throw`를 통해 예외가 발생하고 `p`태그에 출력된다.  

## function

일반적으로 `function test() { ... }`이런식으로 정의하고   
`var result = test();`이런식으로 호출했다.  

JavaScript에선 `function` 정의와 호출방법이 몇가지 더 있다.


### function - arguments

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Functions/arguments  


> `arguments` 객체는 함수에 전달된 인수에 해당하는 Array 형태의 객체입니다.  
> 형태일 뿐이지 `Array` 객체는 아니다. (변환은 가능)  


1. `arguments.callee`  
   현재 실행 중인 함수를 가리킵니다.  
2. `arguments.length`  
   함수에 전달된 인수의 수를 가리킵니다.  


```js
var fn1 = function calleeTest1(numb) {
  document.write('A');
};
document.write(fn1);
```
출력값  
`function calleeTest(numb) { document.write('A'); }`  

`fn1`이 함수 `calleeTest1` 자체를 나타내기 때문에 아래와 같이 출력된다.  
`fn1` 변수가 함수자체를 가리키기 때문에 `fn1();` 형식으로 괄호를 붙여 호출 가능하다.  

`arguments.callee`는 위처럼 함수(자신) 자체를 가리키는 객체

```js
var fn2 = function calleeTest2(numb) { 
  document.write('A');
  return arguments.callee;
}
fn2()()()()()();
```
출력값  
`AAAAAA`  

`calleeTest2`가 종료되면서 계속 자기자신을 반환하기 때문에 `()` 호출 연산자를 연속으로 붙여 사용할 수 있다.  


### new Function()  

`function`또한 `Obejct`의 일종으로 다음과 같이 생성이 가능하다.   
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
var result = sum(1, 2);
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


### 기본 파라미터, 가변 파라미터   

```js
function addContact(name, mobile, home="없음", address="없음", email="없음") {
    var str = `name=${name}, mobile=${mobile}, home=${home}, address=${address}, email=${email}`;
    console.log(str);
}

addContact("홍길동", "010-1234-1234");
addContact("이몽룡", "010-6789-6789", '서울');
//name=홍길동, mobile=010-1234-1234, home=없음, address=없음, email=없음
//name=이몽룡, mobile=010-6789-6789, home=서울, address=없음, email=없음
```

```js
function foodReport(name, age, ...favoriteFoods) {
    console.log(name + ", " + age);
    console.log(favoriteFoods);
}

foodReport("이몽룡", 20, "짜장면", "초밥", "냉면", "불고기");
foodReport("고길동", 20, "백반");
```

### Function.prototype.call

`Function` 객체에는 `call()` 전역 함수가 있는데 해당 메서드를 호출하는 개체를 임의로 변경할 수 있다.  
즉 `this`로 사용할 객체 지정이 가능하다.  

코드를 설명  

```js
var obj1 = {
  name: "hong",
  age: 15
}
var obj2 = {
  name: "kim",
  age: 25
}
var Person = {
  name: 'some-name',
  age: 0,
  print: function () {
    return this.name + "/" + this.age;
  }
}
console.log(Person.print()); // some-name/0
console.log(Person.print.call(obj1)); // hong/15
console.log(Person.print.call(obj2)); // kim/25
```

`person`객체 안에는 어떠한 프로퍼티도 있지 않지만 `call`메서드를 통해 전달 받은 객체를 `this`키워드와 함께 사용 가능하다  

- 사용법  
  `func.call(thisArg[, arg1[, arg2[, ...]]])`

- 매개변수  
  1. `thisArg`  
    `func` 호출에 제공되는 `this`의 값.  
  2. `arg1, arg2, ...`  
    객체를 위한 인수.  

### Function.prototype.apply

`apply` 도 `call` 과 똑같은 기능을 지원한다.  
`this` 로 가리킬 객체를 전달한다. 


```js
function Person(name, yearCount) {
    this.name = name;
    this.age = 0;
    this.count = 0;
    var incrAge = function () {
        this.age++;
    }
    var incrCount = function () {
        this.count++;
    }
    for (let i = 0; i < yearCount; i++) {
        incrAge.apply(this);
        incrCount(); //window 객체를 가리키게 된다.
    }
}

var p1 = new Person("홍길동", 20);
console.log(p1.name + "님의 나이: " + p1.age);
console.log(p1.name + "님의 카운트: " + p1.count);
// 홍길동님의 나이: 20
// 홍길동님의 카운트: 0
```

`Person` 생성자 함수의 `this` 는 `new` 로 생성된 인스턴스를 가리키게 된다.  
반면 `incrAge` 나 `incrCount` 의 `this` 는 호출자를 별도로 지정하지 않으면 최상위 객체를 가리킨다.  

> `Person` 내부에서 정의했다 하더라도 메서드의 `this` 는 `new` 인스턴스를 가리키지 않는다.


- 사용법
  `fun.apply(thisArg, [argsArray])`
  
- 매개변수  
1. `thisArg`  
  `func` 를 호출하는데 제공될 `this` 의 값.  
2. `argsArray`  
옵션. `func` 이 호출되어야 하는 인수를 지정하는 유사 배열 객체

`call` 과 차이점은 파라미터를 가변 파라미터 형식으로 받느냐, 배열 파라미터 형식으로 받느냐  

### Function.prototype.bind

`bind` 역시 `this` 로 가리킬 객체를 전달하는 메서드이다.  
단 `call`, `apply` 와의 차이점은 `this` 가 영구적으로 `bind()` 의 첫 번째 매개변수로 고정 되는것.

```js
function f() {
  return this.a;
}

var g = f.bind({a: 'azerty'});
console.log(g()); // azerty

var h = g.bind({a: 'yoo'}); // bind는 한 번만 사용 가능!
console.log(h()); // azerty

var o = {a: 37, f: f, g: g, h: h};
console.log(o.a, o.f(), o.g(), o.h()); // 37,37, azerty, azerty
```

### 태그안에서 자기자신을 가리키는 this 사용

이벤트를 수신/호출한 요소를 가리키는 용도로 `this`키워드를 사용가능 하다.  

```html
<button onclick="this.style.color = 'red'">click</button>
```

### 화살표 함수(람다) 에서 this 사용

화살표 함수전까지는, 모든 새로운 함수는 자신의 `this` 값을 정의했다.  

자신을 호출한 객체를 할당하거나 `apply`, `call`, `bind` 등의 메서드를 통해 `this` 객체를 지정한다,  
직관적이지 못한 `this` 사용때문에 아래처럼 `Person()` 이 호출될때 `this` 값을 사용할 수 있도록 비 전역변수 `that` 에 저장할 수 도 있다.   

```js
function Person() {
  var that = this;  
  that.age = 0;

  setInterval(function growUp() {
    // 콜백은  `that` 변수를 참조하고 이것은 값이 기대한 객체이다.
    that.age++;
  }, 1000);
}
```

이런 과정을 화살표 함수를 사용하면 위의 과정과 매우 비슷하지만 거추장 코드를 생략하고 사용 가능하다.  

화살표 함수는 전역 컨텍스트에서 실행될 때 `this`를 새로 정의하지 않고 위처럼 바로 바깥의 범위(블록)의 `this`값이 사용된다.  

```js
function Person(){
  this.age = 0;

  setInterval(() => {
    this.age++; // |this|는 Person 객체를 참조
  }, 1000);
}
```

사실상 위의 블록 바로 바깥에서 사용한 `this` 를 `that` 변수로 사용하고 별도로 할당한 것 과 같다.  

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
> 출처: https://hyunseob.github.io/2016/08/30/JavaScript-closure/


`getClosure`함수의 지역변수 `text`가 함수가 끝난 시점인 `console.log`에서 정상적으로 출력된다.  
이는 클로저 함수(위의 무명 메서드)가 `text`를 참조하고있고 `var closure`가 클로저 함수를 참조중이기 때문!

즉 클로저를 사용하는 이유는 호출된 **함수의 지역변수를 유지하고 싶기 때문**이다.  
이를 통해 객체지향적인 설계가 가능하다!(코드 간략, 재활용, 은닉화)  

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

설계를 보면 알겠지만 `counter`가 반환받은 객체의 함수를 통해서만 지역변수인 `privateCounter`에 접근 가능하다.   
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

클로저 함수를 사용함으로 바로 객체의 변수에 접근하지 못하도록 하고 접근용 함수에서 유효성 체크를 할 수 있다.  

## Promise

비동기 처리를 위해 JavaScript 에서 제공하는 전역객체.  

`Promise` 객체를 사용하면 좀더 깔끔하게 수행 가능하다. 

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/Promise


```js
var p = new Promise(function (resolve, reject) {
  ...
  if(...) {
      resolve(num);
  } else {
      reject(num);
  }
});
p.then(function (num) {
    console.log("resolve invoked, num:", num);
}).catch(function (num) {
    console.log("reject invoked, num:", num);
})

const promiseFirst = new Promise(resolve => resolve(1)) // resove 안의 데이터 전달
    .then(result => `${result + 10}`) // 11 반환
    .then(result => {console.log(result); return result + 1;}) // 11출력 111 반환

const promiseSecond = new Promise(resolve => resolve(1))
    .then(result => `${result + 20}`) // 21 반환
    .then(result => {console.log(result); return result + 1;}) // 21 출력 211 반환


Promise.all([promiseFirst, promiseSecond]).then(result => console.log(result)) // 111, 211 출력
```

`Promise`에 설정한 함수 내부에서 `resolve`를 호출하면 외부의 `then`에 설정해둔 함수가 호출된다, 매개변수로 `resolve`가 전달한 데이터를 사용 가능.  

반면 `reject` 가 호출되면 외부의 `catch`에 설정해둔 함수가 호출, 마찬가지로 매개변수를 통해 데이터 전달 가능  



`Promise` 객체 생성시 전달하는 함수가 비동기로 실행, 

### async, await

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Statements/async_function

`promise` 대신 비동기 처리를 가능하게 해주는 키워드 


## 연산자 

### 확산연산자 `...`