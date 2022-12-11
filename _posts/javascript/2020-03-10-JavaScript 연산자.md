---
title:  "JavaScript 연산자!"

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

## 연산자

JS 에서 다루는 익숙치 않은 연산자에 대해 알아본다.  

## 동등비교(loosee quality)  

`==` 연산자를 사용하는 표현식  

```js
var name1 = "admin";
var name2 = new String("admin");

console.log(typeof name1); //string
console.log(typeof name2); //Object
console.log(name1 == name2); //true
```

JS 에선 `==` 연산시 2개의 다른 자료형을 **같은 자료형으로 일치시킨다**.  

```js
console.log('0' == '')          // false
console.log(0 == '')            // true
console.log(0 == '0')           // true
console.log(1 == true)          // true
console.log(false == '0')       // true
console.log(null == undefined)  // true
console.log(false == null)      // false
console.log(false == undefined) // false

var name2 = new String("admin");
var name3 = new String("admin");
console.log(name2 == name3); // false
```

JS 내부에서 어떻게 형변화을 시키는지 모두 알기 힘듬으로 동등비교(`==`)보다 일치비교(`===`)를 권장한다.  

부동등비교 `!=` 역시 권장하지 않는다.  

## 일치 비교(strict equality)  

일치연산자`===`는 **내용과 type까지 일치해야 한다**.  

불일치비교(`!==`) 역시 내용과 type 을 모두 비교하여 결과를 반환  

```js
console.log(1 === 1);   // true 
console.log("1" === 1); // false
console.log(3 !== '3')  // true
console.log(4 !== 3)    // true
```

## 대소 비교

```js
console.log(1 < 5)        // true
console.log(1 < '5')      // true
console.log(1 > 'a')      // false
console.log(1 < 'a')      // false
console.log('a' < 'b')    // true
console.log('100' < '12') // true
```

숫자형식 문자열과 `number` 를 비교할 땐 문자열이 `number` 로 **자동 형변환** 되어 비교한다.  

숫자가 아닌 문자열과 `number`를 비교할땐 ASCII와 상관없이 무조건 `false` 반환.  

문자열끼리 비교할 때에는 항상 **ASCII 순으로 비교** 한다.  

## isNaN (Not a Number)

`isNaN`, `NaN` 모두 전역 스코프의 변수, 함수  

```js
declare var NaN: number;
declare function isNaN(number: number): boolean;
```

```js
console.log(1 + null)               // 1
console.log(1 + undefined)          // NaN
console.log(typeof NaN)             // number
console.log(isNaN(NaN))             // true
console.log(isNaN(10))              // false
console.log(NaN === NaN);           // false
console.log(isNaN(1 + undefined))   // true
```

## ||, && 단축평가 연산자

JS 에서 `||, &&` 연산자는 단순 `true` `false` 를 반환하는 연산자가 아니다.  

**`true` 혹은 `false` 를 판단할 수 있는 항목을 반환**하는 연산자이다.  

```js
// 논리합(||) 연산자
console.log('Cat' || 'Dog')  // -> "Cat"
console.log(false || 'Dog')  // -> "Dog"
console.log('Cat' || false)  // -> "Cat"
```

`||` 연산의 경우 첫항만 `Truhty` 임이 판단되어도 바로 반환 가능하기 때문에  
첫항이 `Truhty` 인 `Cat` 이 반환됨  


```js
// 논리곱(&&) 연산자
console.log('Cat' && 'Dog')  // -> "Dog"
console.log(false && 'Dog')  // -> false
console.log('Cat' && false)  // -> false
```

`&&` 연산의 경우 마지막항까지 `Truthy` 인지 `Falsy` 인지 파악해야 함으로  
모든항목이 `Truthy` 인 경우 맨 마지막항이 반환된다.  

특정 상황에선 **단축평가가 삼항연산이나 if 문을 대체할 수 있다.**  

```js
var elem = null
var value = elem && elem.value;
```

`elem.value` 로 인해 `null exception` 이 발생할 것 같지만 단축평가로 `value` 에는 `null` 이 할당된다.  
물론 `elem` 이 `not null` 일 때에는 `value` 값이 할당 될것  

## null 병합연산자(nullish coalescing)

`ES11(ECMAScript2020)` 추가된 된 null 병합연산자  

피연산자가 `null` 또는 `undefined` 인 경우 우항의 피연산자

```js
var foo = null
var bar = foo ?? "default string" // default string
console.log(bar)
```

단축평가 연산자가 `Truthy`, `Falsy` 를 체크하는 반면  
`null` 병합연잔자는 `null` 만을 체크한다.  

## 디스트럭처링(destructuring)

객체의 `key-value` 값을 풀어 별개의 변수로 할당할 수 있는 표현식  

```js
var person = {
    first: 'ko',
    last: 'zie'
};

const {first, last: l='default'} = person; // 변수 이름 변경 + defualt 값 설정가능
console.log(first, l) // ko zie
```

배열의 요소 또한 바로 지정가능  

```js
var [hungry, full] = ["yes", "no"]
console.log(hungry, full) // yes no

const person = ["ko", "zie", 27];
const [first, last, age] = person;
console.log(first, last, age) // ko zie 27
```

가변 연산자를 사용해 나머지값을 배열로 받을 수 있다.  

```js
const person = ["ko", "zie", 27, "hello", "world"];
const [first, last, ...age] = person;
console.log(first, last, age) // ko zie [ 27, 'hello', 'world' ]
```

```js
```

## 스프레드 연산자(spread syntax)

