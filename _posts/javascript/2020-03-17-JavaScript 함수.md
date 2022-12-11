---
title:  "JavaScript 함수!"

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

## 함수 개요

자바스크립트의 함수는 객체 타입의 값  

객체는 property 와 value 로 이루어진다고 하였는데  
함수역시 객체임으로 아래와 같은 property 를 가지게 된다.  

- function 키워드  
- 함수 이름  
- 매개 변수 목록  
- 함수 몸체  

함수는 다른 객체와 다르게 호출개념이 있는데 함수객체만의 고유한 property 가 있기 때문  

### 매개변수 기본값, 가변 파라미터(Rest 파라미터)   

```js
function sum(x=0, y=0) {
  return x + y;
}
console.log(sum(x=10, y=20));

function foodReport(name, age, ...favoriteFoods) {
    console.log(name + ", " + age);
    console.log(favoriteFoods);
}

foodReport("이몽룡", 20, "짜장면", "초밥", "냉면", "불고기");
// 이몽룡, 20
// [ '짜장면', '초밥', '냉면', '불고기' ]
```

> 가변파라미터는 단 하나만 선언 가능하다.  

### 함수 종류

ES6 로 업데이트되며 함수는 아래 3가지로 나뉜다.  

구분 | constructor | prototype | super | arguments
|---|---|---|---|---|
일반함수 | ◯ | ◯ | ✕ | ◯
메서드 | ✕ | ✕ | ◯ | ◯
화살표함수 | ✕ | ✕ | ✕ | ✕

함수 사용목적에 맞게 일반함수가 가지고 있던 내부슬롯은 메서드나 화살표함수에선 존재하지 않고  
`super` 와 같은 키워드도 메서드 외의 함수에서는 사용하지 못한다.  

> `super` 키워드는 `[[HomeObject]]` 내부슬롯을 가질경우 사용가능  
> 
> 화살표함수의 경우 모든 내부슬롯과 키워드를 사용할수 없다고 되어있는데  
> 상위스코프에 해당 내부슬롯과 키워드접근이 가능할 경우 상위스코프의 것을 그대로 사욯안다.  

## 함수 정의 방법

함수선언문, 함수리터럴 외에 기반으로 여러가지 함수정의방법에 대해 알아본다.  

### 함수선언문, 함수리터럴

```js
// 함수선언문
function greet(name) {
    console.log("hello " + name);
}

// 함수리터럴
const greeter = function (name) { // 익명함수
    console.log("hello " + name);
};

greet("world"); // hello world
greeter("world"); // hello world

function foo() { console.log('foo'); }
(function bar() { console.log('bar'); });

foo()
bar() // bar is not defined
```

함수리터럴은 일종의 변수에 객체를 참조시키는 **표현식** 으로 함수이름을 생략 가능하다.  

함수표현식으로 정의된 `foo` 는 JS 엔진이 암묵적으로 생성한 식별자다.  
함수리터럴의 경우 표현식의 식별자가 없다면 아예 호출이 불가능하다.  

**기명함수리터럴**은 아래와 같이 함수이름을 포함시킨 함수리터럴이다.  

```js
var add = function add(x , y) { return x + y; };
```

함수선언문과 기명함수리터럴의 차이가 아예 없는데  
JS 엔진의 의해 기명함수리터럴은 함수선언문이나 일반적인 함수리터럴 표현식으로 해석될 수 있다.  

> 함수리터럴의 경우 호이스팅 조건에 부합하지 않아 선호출 후정의가 불가능하다.  
> undefined 로 생성된 함수리터럴 식별자를 호출할 순 없으니 
> 이런 안정성으로 함수선언문보다 함수리터럴을 사용해야 한다는 개발자들이 많음  

이런 특징으로 **함수는 호출가능한 객체이며 식별자를 통해 호출**된다 할 수 있다.   

> **call by value, call by reference**  
원시타입의 값들은 **변경 불가능한 값(immutable value)** 객체는 **변경가능한 값(mutable value)**  
JS에서도 원시타입은 `call by value`, 객체는 `call by reference` 로 이루어진다.  


### 즉시실행함수

함수선언과 동시에 호출.  

함수선언문을 소괄호로 묶고 뒤에 또 `()`를 사용해 호출하면 된다.  

```js
var result = (function foo() {
    return "test"
})();
console.log(result); //test
```

### Function 객체

> `Function` 객체로 함수정의는 권장하지 않는다.  
> 정의가 문자열로되어있어 외부 변수들과 정상적인 참조가 이루어지지 않는다.  

`new Function ([arg1[, arg2[, ...argN]],] functionBody)`   

JS 빌트인객체인 `Function` 의 생성자함수로 함수를 정의  

```js
var test1 = new Function('a', 'b', 'return a + b');
console.log(test1(1+2)) // 3
console.log(test1 instanceof Object) // true

function test2(a, b) {
    return a + b;
}
```

![js15-2](/assets/javascript/image12.png)  

`test1.__proto__.__proto__` 을 실행하면 `Object.prototype` 이 반환된다.  

### 화살표 함수(fat arrow)

```js
// 화살표 형식
const greeter = (name) => {
    let str = `hello ${name}`;
    return str;
}
const greeter = name => `hello ${name}`;
```

한줄처리시 중괄호 및 return 생략(암시적 반환)
매개변수가 하나일경우 괄호 생략

일반적으로 콜백함수나 `collection`(`list`, `map` 등) 에서 화살표함수를 많이 사용한다.  

```js
const race = '100m dash'
const runner = ['user1', 'user2', 'user3']
const results = runner.map((user, i) => ({ name: user, item: race, place: i + 1 }));
// const results = runner.map((user, i) => {
//     return { name: user, item: race, place: i + 1 }
// });
console.log(results)
```

## this

> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Operators/this

객체지향 언어에서 `this`는 클래스가 생성하는 인스턴스를 가리킨다.  

JS 의 `this` 는 **함수가 호출되는 방식**에 따라 `this` 에 바인딩될 값이 동적으로 결정된다.

크게 아래 4가지 함수에서 this 를 사용했을 때  
어떤 객체를 가리키는지 알아본다.  

1. 일반함수  
2. 객체 메서드  
3. 생성자함수  
4. 화살표함수  

```js
function square(number) { // 일반함수
  console.log(this); // global
  return number * number;
}
square(2);

const person = {
  name: 'Lee',
  getName() { // 메서드
    console.log(this); // 인스턴스주소
    return this.name;
  }
};

function Person(name) { // 생성자함수
  this.name = name;
  console.log(this); // 생성할 인스턴스주소
}

const me = new Person('Lee');

function foo() {
    let bar = () => { // 화살표함수
        console.log(this) // foo 의 this
    }
    bar()
}
foo(); // global
```

> use strict 를 사용할 경우 square 의 this 는 Undefined 가 된다.  

### 일반함수에서 this

**일반함수에서 `this` 는 전역객체**를 가리킨다.  

이는 메서드 내에서 중첨함수로 일반함수를 정의하고 호출해도 예외가 아니다.  

```js
var value = 1;
const obj = {
    value: 100,
    foo() {
        console.log("foo's this: ", this);  // {value: 100, foo: ƒ}
        console.log("foo's this.value: ", this.value); // 100

        function bar() {
            console.log("bar's this: ", this); // window
            console.log("bar's this.value: ", this.value); // 1
        }
        bar();
    }
};

obj.foo();
```

함수선언문, 리터럴로 정의한 **일반함수**에서 테스트해본 결과  
어디에 정의되어 있던지 일반함수의 `this` 는 전역객체를 가리킨다  

> 콜백함수로 리터럴 방식의 일반함수 정의 후 `this` 사용시 문제를 야기함  

### 메서드에서 this

**메서드에서 `this` 는 인스턴스**를 가리킨다.  
메서드는 객체 property 에 매핑된 함수객체 뿐 객체의 일부가 아니다.  

```js
const person = {
    name: 'Lee',
    getName() { return this.name; }
};

// 다른 객체의 property 에 함수객체 바인딩
const anotherPerson = { name: 'Kim' };
anotherPerson.getName = person.getName;
console.log(anotherPerson.getName()); // Kim

// 전역변수에 함수객체 바인딩
const getName = person.getName;
console.log(getName()); // ''
```

함수객체 바인딩 위치에따라 메서드, 일반함수 구분이 나뉘며  
메서드로 호출하였는지, 일반함수로 호출하였는지에 따라 `this` 가 달리 가리킨다.  

### 생성자함수에서 this

**생성자함수에서 `this` 는 생성될 인스턴스**를 가리킨다.  

`new` 연산자와 함께 실행하면 생성자함수로 인식되어 `new` 로 생성된 **인스턴스와 `this` 가 바인딩**된다.  


### 화살표함수 에서 this

**화살표함수에서 `this` 는 상위 스코프의 `this`** 를 이어받는다.

물론 바깥 스코프가 따로 없거나(전역함수)  
객체 정의문과 같이 스코프에 `this` 가 따로 없는 경우 비어있는 객체를 가리킨다.  

```js
let obj = {
    test: "TEST",
    func0: () => {
        console.log(this)
        // 객체 정의시 this 없음으로 비어있음
    },
    func1: function foo() {
        let bar = () => {
            console.log(this)
            // foo 의 this, 인스턴스를 가리킴
        }
        temp()
    },
}

obj.func0()
obj.func1()
```

메서드와 생성자함수 모두 인스턴스를 this 로 가짐으로  
메서드나 생성자함수 안에서 콜백함수 사용시 요긴하게 사용할 수 있다.  

```js
function Person(){
  this.age = 0;

  setInterval(() => {
    this.age++; 
  }, 1000);
}
```

> 콜백이나 list, map 과 같은 콜렉션 내부에서 함수정의시 별도의 that 과 같은 변수생성없이 상위스코드의 this 를 그대로 사용할 수 있으로 화살표함수 사용을 권장함  
> 아니면 아래 사용할 bind 와 같은 함수 사용 권장


### this 바인딩 - call, apply, bind

`Function` 의 `prototype 객체` 에 `call`, `apply`, `bind` 라는 함수가 정의되어 있다.  

- `Function.prototype.call`  
- `Function.prototype.apply`  
- `Function.prototype.bind`  

전역함수의 `this` 는 `global` 객체를 가리키지만 아래처럼 `apply` 혹은 `call` 을 사용해 호출하면 `this` 를 사용자 지정할 수 있다.  

```js
function getThisBinding() {
    return this; // 원래 global
}

const thisArg = { a: 1 };

// 인수로 전달한 객체를 this에 바인딩한다.
console.log(getThisBinding.apply(thisArg)); // {a: 1}
console.log(getThisBinding.call(thisArg)); // {a: 1}
```

`apply` 는 함수 인수로 배열을 사용하고  
`call` 은 함수 인수로 가변인자를 사용할 뿐  
두 함수의 동작은 동일하다.  

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
    print: function (msg1, msg2) {
        return `${this.name} / ${this.age}: (${msg1}, ${msg2})`;
    }
}
console.log(Person.print("hi", "hello")); // some-name / 0: (hi, hello)
console.log(Person.print.apply(obj1, ["hi", "hello"])); // hong / 15: (hi, hello)
console.log(Person.print.call(obj2, "hi", "hello")); // kim / 25: (hi, hello)
```

`bind` 역시 `this` 를 다른 객체로 바인딩하는 것은 동일하나  
함수를 호출하지 않고 함수자체를 반환한다.  

호출하려면 아래처럼 즉시실행함수처럼 사용해야 한다. 

```js
function getThisBinding() {
    return this; // 원래 global
}
const thisArg = { a: 1 };
console.log(getThisBinding.bind(thisArg)()); // {a: 1}
```

콜백함수에서 요긴하게 사용할 수 있다.  

```js
var value = 1;
const obj = {
    value: 100,
    foo() {
        // 콜백 함수에 명시적으로 this를 바인딩한다.
        setTimeout(function () {
            console.log(this.value); // 100
        }.bind(this), 100);
    }
};
obj.foo();
```


## 함수 객체

JS 에선 함수또한 객체로 취급되기 때문에 아래처럼 property 와 method 를 가질 수 있다.  

```js
function foo() { }
foo.prop = 10;
foo.method = function () {
    console.log(this.prop);
};
foo.method(); // 10
```

일반객체와 다른점은 함수객체는 호출할 수 있다는 것  

함수객체의 property 조회해보면  

```js
function square(number) {
  return number * number;
}

console.log(Object.getOwnPropertyDescriptors(square));
/*
{
  length: {value: 1, writable: false, enumerable: false, configurable: true},
  name: {value: "square", writable: false, enumerable: false, configurable: true},
  arguments: {value: null, writable: false, enumerable: false, configurable: false},
  caller: {value: null, writable: false, enumerable: false, configurable: false},
  prototype: {value: {...}, writable: true, enumerable: false, configurable: false}
}
*/
```

위와같이 5개의 property 가 출력된다.  

- `length` - 함수선운무에 정의된 매개변수 개수 
- `caller`  - 비표준임으로 무시
- `name` - 함수명, 익명함수는 `anonymousFunc` 으로 표기
- `arguments`  
- `prototype`  

### arguments

`arguments` property 는 함수에 전달된 인수와 각종 정보를 가지는 `iterable` 한 객체.  

1. `arguments.callee` - 현재 실행 중인 함수  
2. `arguments.length` - 함수에 전달된 인수의 수  

JS 함수호출시 매개변수 카운팅을 하지 않으며 변수와 매개변수를 따로 취급하지 않는다.  

```js
function test() {
  for (var i = 0; i < arguments.length; i++) {
    var arre = arguments[i];
    console.log(arre);
  }
}
test("홍길동", 10, 20, 30, "C");
// 홍길동
// 10
// 20
// 30
// C
```

`arguments.callee` 는 함수(자신) 자체를 가리키는 객체임으로 아래처럼 재귀형식처럼 사용할 수 있다.  

```js
var fn2 = function calleeTest2(numb) { 
  document.write('A');
  return arguments.callee;
}
fn2()()()()()(); // AAAAAA
```

arguments 는 `iterable` 한 객체일 뿐 배열이 아님으로  
배열함수를 사용하고 싶다면 변환과정이 필요하다.  

```js
function sum() {
    var array = Array.prototype.slice.call(arguments);
    return array.reduce(function (pre, cur) {
        return pre + cur;
    }, 0);
}
```

