---
title:  "JavaScript 배열, Date!"

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

## 배열

JS 기본 `built-in` 클래스  

```js
// 리터럴
let m1 = [];// []
let m2 = [1, 2, 3]; // [ 1, 2, 3 ]
let m3 = new Array(); // []
let m4 = new Array(1, 2, 3); // [ 1, 2, 3 ]
let m5 = new Array(10); // [ <10 empty items> ]
let m6 = new Array('test'); // [ 'test' ]
let m7 = Array.of(2); // [ 2 ]
let m8 = Array.of(1,2,3); // [ 1, 2, 3 ]
```

JS 배열은 초기 길이를 정해줄 필요 없다.

```js
let m1 = [];
m1["1"] = "test"; // index 는 정수의 number 형식이기만 하면 됨
m1[2] = "test";
for (let index = 0; index < 3; index++) {
    console.log(m1[index]);
    // undefined
    // undefined
    // test
}
```

크기를 지정하지 않아도 별도의 에러가 출력되지 않으며  
바로 접근할 수 있다.  

JS 에서 배열은 **희소배열(sparse array)** 이다.  
타 언어에선 고정된 크기의 정해진 개수의 배열을 메모리에 할당하여 고속으로 탐색 가능하지만  
JS 에서 배열은 연속적이지도 않고 크기또한 고정이 아니다.  

**일종의 배열의 기능을 수행하는 객체**라 할 수 있다.  

```js
let arr = []
arr['foo'] = 3; // property 추가
arr.bar = 4;
console.log(typeof arr) // object

let obj = {}
obj[0] = "hello"
obj[1] = "world"
console.log(obj[0]); // hello
console.log(obj[1]); // world
```

객체의 배열 확인은 Array.isArray 혹은 instanceof 로 확인 가능

```js
var points = [];
console.log(Array.isArray(points)); //true
console.log(points instanceof Array); //true
```

`Array.from` 를 사용하면 `이터러블` 혹은 `유사배열객체` 를 인수로 받아 배열로 변환할 수 있다.  

> **유사배열객체(array-like object)** 는 배열처럼 인덱스로 프로퍼티 값에 접근할 수 있고 `length` property를 갖는 객체  


```js
let m1 = Array.from('Hello'); // [ 'H', 'e', 'l', 'l', 'o' ]
```

그외에도 `length` property 를 가진 유사배열객체를 전달해도 배열로 변환한다.   

```js
// [ undefined, undefined, undefined ]
let m1 = Array.from({ length: 3 }); 

// [ 'a', 'b', undefined ]
let m2 = Array.from({ length: 3, 1: 'b', 0: 'a' }); 

// [ 0, 1, 2 ]
let m3 = Array.from({ length: 3 }, (_, i) => i) 
```

### length  

`arr.length` 를 통해 배열의 원소를 삭제하거나 명시적으로 길이를 할당할 수 있다.  

```js
const arr = [1, 2, 3, 4, 5];
arr.length = 3;
console.log(arr); // [1, 2, 3]
arr.length = 10;
console.log(arr); // [ 1, 2, 3, <7 empty items> ]
```

### includes, indexOf

둘다 요소값으로 존재여부를 확인할 수 함수  

`includes` 는 요소존재 여부에 따라 `boolean` 값을 반환하고  
`indexOf` 는 요소존재 여부에 따라 `index` 값을 반환한다.  

```js
const arr = [1, 2, 3]
console.log(arr.includes(2)) // true
console.log(arr.includes('2')) // false
console.log(arr.indexOf(2)) // 1
console.log(arr.indexOf('2')) // -1
```


### push, pop, shift, unshift

> 원본을 변경 시키는 함수(mutator method)

`push`, `pop`은 스택 개념처럼 맨 **마지막 요소** 위치에 값을 집어넣고 빼내온다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(points.pop()); //1
console.log(points.toString()); //3,5,2,4
console.log(points.push(1)); //5, 배열의 길이 반환
console.log(points.toString()); //3,5,2,4,1
```
`pop`은 빼낸 배열 요소값을 반환한다.  
`push`는 매개변수로 요소를 집어넣고 배열의 길이를 반환한다.  

`shift`, `unshift`는 거꾸로된 스택, **제일 첫 요소** 위치에 값을 집어넣고 빼내온다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(points.shift()); // 첫번째 요소를 제거하고 반환, 왼쪽으로 한칸씩 땡겨진다.  
console.log(points.toString()); // 5,2,4,1
console.log(points.unshift(8)); // 5, index 0에 값을 추가하고 배열 길이 반환.
console.log(points.toString()); // 8,5,2,4,1
```

![2](/assets/javascript/image13.png) 

### delete, splice, slice

> `delete`, `splice` 는 원본을 변경 시키는 함수(mutator method)

index 배열값을 삭제하고 싶다면 `delete`사용.  

> 공간은 삭제되지 않고 내부의 값만 삭제됨  
> 없는 index 를 삭제해도 `true` 반환함

```js
let arr = [1, 2, 3, 4, 5]
console.log(delete arr[0]) // true
console.log(delete arr[100]) // true
console.log(arr) // [ <1 empty item>, 2, 3, 4, 5 ]
```

`splice` 는 공간까지 삭제한다.  


```js
let arr = [1, 2, 3, 4, 5]
console.log(arr.splice(0, 1, -1, -2)) // [ 1 ]
console.log(arr) // [ -1, -2, 2, 3, 4, 5 ]
```

첫번째 매개변수: `start index`  
두번째 매개변수: `delete count`  
이후 매개변수: `[itmes]` 삭제될 공간부터 넣을 아이템(가변인자), 생략가능  

> `slice` 는 원본을 변경 시키지 않음  

`start ~ end` index 를 잘라 반환하는 메서드.  

```js
let arr = [1, 2, 3, 4, 5];
console.log(arr); // [ 1, 2, 3, 4, 5 ]
console.log(arr.slice(2)); // [ 3, 4, 5 ]
console.log(arr.slice(2, 4)); // [ 3, 4 ]
```

### concat, join

> `concat`, `join` 원본을 변경시키지 않는다.  

`join` 은 요소를 합쳐 구분자로 연결할 **문자열을 반환**한다.  

```js
const arr = [1 , 2 , 3 , 4];
console.log(arr.join()) // 1,2,3,4
console.log(arr.join("")) // 1234
console.log(arr.join(":")) // 1:2:3:4
```

`concat` 은 두 배열을 합쳐 **하나의 배열로 반환**한다.  

```js
let arr1 = [1, 2];
let arr2 = [3, 4];
console.log(arr1.concat(arr2)); // [ 1, 2, 3, 4 ]
console.log(arr1.concat(3, 4, 5)); // [ 1, 2, 3, 4, 5 ]
console.log(arr1.concat(arr2, 5)); // [ 1, 2, 3, 4, 5 ]
```


### fill

> 원본을 변경 시키는 함수(mutator method)

배열 초기화 함수  

```js
let arr = [1, 2, 3, 4, 5];
arr.fill(-1)
console.log(arr); // [ -1, -1, -1, -1, -1 ]
```

```js
let arr = [1, 2, 3, 4, 5];
arr.fill(-1, 2)
console.log(arr); // [ 1, 2, -1, -1, -1 ]
```

```js
let arr = [1, 2, 3, 4, 5];
arr.fill(-1, 0, 2)
console.log(arr); // [ -1, -1, 3, 4, 5 ]
```

첫번째 매개변수: 초기화값  
두번째 매개변수: `start index`, 생략가능  
세번째 매개변수: `end index`, 생략가능  

### flat  

> `flat` 는 원본을 변경 시키지 않음

평탄화를 위한 함수, 

```js
// default=1
[1, [2, [3, [4]]]].flat();  // [1, 2, [3, [4]]]
[1, [2, [3, [4]]]].flat(2); // [1, 2, 3, [4]]
[1, [2, [3, [4]]]].flat(Infinity); // [1, 2, 3, 4]
```

### sort, reverse

> `sort`, `reverse` 는 원본을 변경 시키는 함수(mutator method)

기본 정렬조건이 ASCII 코드순(사전순) 이기 때문에 **정렬기준**을 지정해 줘야 한다.

> `1, 2, 11` 이 정렬되면 `1, 11, 2`로 출력된다.  


```js
let arr = [3, 5, 2, 4, 1];
console.log(arr.sort((a, b) => a - b).reverse()); // [ 1, 2, 3, 4, 5 ]
```

`0`, `음수`, `양수값`을 통해 정렬기준 생성  

> 반환값이 0보다 작으면 첫번째 인자를 앞으로  
> 반환값이 0보다 크면 두번째 인자를 앞으로 정렬한다  

객체배열을 사용할 경우 정렬기준 지정을 해야 원하는대로 정렬 가능한데  

```js
const todos = [
    { id: 4, content: 'JavaScript' },
    { id: 1, content: 'HTML' },
    { id: 2, content: 'CSS' }
];
todos.sort((a, b) => { return a.id - b.id })
/* [
  { id: 1, content: 'HTML' },
  { id: 2, content: 'CSS' },
  { id: 4, content: 'JavaScript' }
] */
```

가독성을 위해 아래와 같이 `compare` 함수를 정의하여 사용한다.  

```js
function compare(key) {
    return (a, b) => {
        if (a[key] > b[key]) return 1;
        else if (a[key] < b[key]) return -1;
        else return 0;
    }
}
todos.sort(compare("id"))
todos.sort(compare("content"))
```

`reverse` 를 사용하면 기존에 정렬해두었던 배열을 역순으로 출력할 수 있다.  

```js
let arr = [3, 5, 2, 4, 1];
console.log(arr.sort((a, b) => a - b).reverse()); // [ 5, 4, 3, 2, 1 ]
```

> 랜덤하게 정렬하기  
만약 기존 배열을 정렬되지 않고 랜덤하게 위치하도록 하려면 정렬`function`과 `Math.random()`내장 함수를 사용하면 된다.  

```js
var m = [1, 2, 3, 4, 5];
console.log(m.sort(function () {
    return 0.5 - Math.random();
}))
```

### forEach filter map

> `forEach` `filter` `map` 는 원본을 변경시키지 않는다.  
> 배열에서 가장 많이 사용하는 고차함수  

`forEach` 는 **기본적인 배열 요소를 순회**하는 메서드이다.  

셋다 매개변수로 받는 콜백함수 형식이 동일함  

```
forEach(callback(cur, [idx], [arr]), [thisArg])
filter(callback(cur, [idx], [arr]), [thisArg])
map(callback(cur, [idx], [arr]), [thisArg])
```

`thisArg` 의 경우 `this` 로 가리킬 객체를 전달 가능하지만  
고차함수 특성상 콜백함수를 화살표함수로 많이 정의하기에 대부분 생략함  
(생략시 전역변수를 `this` 로 사용)

```js
let arr = [1, 2, 3];
let callbackfn = function (item, index, arr) {
    console.log(`item:${item}, index:${index}, arr:${JSON.stringify(arr)}, this:${JSON.stringify(this)}`);
}
let thisArg = { test: "test" }
console.log(arr.forEach(callbackfn, thisArg));
/* 
item:1, index:0, arr:[1,2,3], this:{"test":"test"}
item:2, index:1, arr:[1,2,3], this:{"test":"test"}
item:3, index:2, arr:[1,2,3], this:{"test":"test"}
*/

let arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
console.log(arr.filter(item => item % 2 === 0)); 
// [ 2, 4, 6, 8, 10 ]

console.log(arr.map(item => item * 2));
// [ 2,  4,  6,  8, 10, 12, 14, 16, 18, 20 ]
```

### some, every

배열내부의 요소값중 부합하는 기준이 있을경우 `boolean` 을 반환하는 고차함수  

```
some(callback(cur, [idx], [arr]), [thisArg])
every(callback(cur, [idx], [arr]), [thisArg])
```


```js
let evenArr = [2, 4, 6, 8, 10]

// 모두 짝수인지
console.log(evenArr.every(item => {
    return item % 2 === 0;
})) // true

// 3의 배수가 있는지
console.log(evenArr.some(item => {
    return item % 3 === 0;
})) // true
```

`indexOf` 와 `lastIndexOf` 메서드로 배열 내 요소 중복여부 체크

```js
function isDuplicate(arr) {
    const isDup = arr.some((x) =>
        arr.indexOf(x) !== arr.lastIndexOf(x));
    return isDup;
}

console.log(isDuplicate(['a', 'b', 'c', 'b']));
// true
```

### reduce

> reduce: 줄이다

```
reduce(callback(acc, cur, [idx], [arr]), [initialValue])
```

- `acc` - `accumulator`, 누적값
- `cur` - `currentValue`, 현재 요소
- `idx` - `currentIndex`, 처리할 현재 요소의 인덱스, `initialValue`를 제공한 경우 0, 아니면 1부터 시작
- `arr` - 호출한 배열  
- `initialValue` - 초기값

```js
let arr = [1, 2, 3, 4, 5];
console.log(arr.reduce((acc, cur, idx, arr) => {
    console.log(`acc:${acc}, cur:${cur}, acc:${idx}, acc:${arr}`);
    return acc + cur;
}, 0)); // 15
/*
acc:0, cur:1, acc:0, acc:1,2,3,4,5
acc:1, cur:2, acc:1, acc:1,2,3,4,5
acc:3, cur:3, acc:2, acc:1,2,3,4,5
acc:6, cur:4, acc:3, acc:1,2,3,4,5
acc:10, cur:5, acc:4, acc:1,2,3,4,5
*/
```

## Set

`===` 연산자를 통해 중복을 허용하지 않는다.  

```js
const s1 = new Set([1, 2, 3, 3]);
console.log(s1); // Set(3) {1, 2, 3}

console.log(s1.size) // 3
console.log(s1.has(1)) // true
console.log(s1.delete(2)) // true
```

## Map

`key-value` 형태의 데이터 타입,  

`key` 의 일치여부는 `===` 연산자로 확인한다.  

```js
const m1 = new Map();
m1
    .set('key1', 'value1')
    .set('key2', 'value2')
    .set('key0');

const m2 = new Map([
    ['key1', 'value1'],
    ['key2', 'value2']
]);

console.log(m1); // Map(3) { 'key0' => undefined, 'key1' => 'value1', 'key2' => 'value2' }
console.log(m2); // Map(2) { 'key1' => 'value1', 'key2' => 'value2' }

console.log(m1.has("key1")) // true
console.log(m1.get("key1")) // value1
console.log(m1.delete("key0")) // true
```

`Map` 은 이터러블임음로 순회하는 방법은 `forEach` 문과 `for...in`

```js
const m1 = new Map();
m1
    .set('key1', 'value1')
    .set('key2', 'value2')
    .set('key0');

m1.forEach((v, k, map) => {
    console.log(`value:${v}, key:${k}`);
})
/* 
value:value1, key:key1
value:value2, key:key2
value:undefined, key:key0
*/

for(const entry of m1.entries()) {
    let k = entry[0]
    let v = entry[1];
    console.log(`value:${v}, key:${k}`);
}
/* 
value:value1, key:key1
value:value2, key:key2
value:undefined, key:key0
*/
```

key 와 value 를 순회할 수 있는 iterator 를 가져올 수 있다.  

```js
const m1 = new Map();
m1
    .set('key1', 'value1')
    .set('key2', 'value2')
    .set('key0');

console.log(m1.keys()); // [Map Iterator] { 'key1', 'key2', 'key0' }
console.log(m1.values()); // [Map Iterator] { 'value1', 'value2', undefined }
```


## Date

JS `built-in` 날짜객체

현재시간을 생성하거나 `1970/1/1 UTC` 기점으로 밀리초를 사용하여 생성할 수 있다.  

```js
console.log(new Date());  // 2018-11-20T13:52:52.804Z
console.log(new Date(0)); // 1970-01-01T00:00:00.000Z
```

매개변수에 `year`, `monthIndex`, `day` 등을 지정하여 생성
`monthIndex` 의 경우 `0 ~ 11` 까지의 정수로 1월부터 12월까지 나타냄  

```
new Date(year, monthIndex, [day, hours, minutes, seconds, milliseconds])
```

지정하지 않은 값은 모두 기본값 0 이기에 아래와 같이 시차로 인해 날짜가 달리보일 수 있다.  

```js
console.log(new Date(2018, 11, 21));    // 2018-12-20T15:00:00.000Z
```

> 한국시간으로 `2018-12-21T00:00:00` 이 맞다.

ISO 타임문자열을 사용하여 생성 가능하다.  
별도의 `zone` 을 설정하지 않을경우 `system zone` 에 맞춰 `local time` 을 생성한다.  

```js
console.log(new Date("2018-11-20T15:00:00"));       // 2018-11-20T06:00:00.000Z
console.log(new Date("2018-11-20T15:00:00+09:00")); // 2018-11-20T06:00:00.000Z
console.log(new Date("2018-11-20T15:00:00Z"));      // 2018-11-20T15:00:00.000Z
```

> ISO 타임문자열 포멧은 다양한 종류가 있으나 위 범위를 벗어나지 않는것을 권장

### now, parse, UTC

`now`, `parse`, `UTC` 모두 전역변수로 입력받은 밀리초를 반환한다.  

```js
// 현재시간 밀리초
console.log(Date.now()) // 1542721972804

// 파싱문자열 밀리초
console.log(Date.parse("2018-11-20T13:52:52Z")) // 1542721972000

// UTC시간의 밀리초
console.log(Date.parse("1970-01-01T09:00:00")) // 0
console.log(Date.UTC(1970, 1 - 1, 1, 9)) // 32400000
```

> UTC 는 쓸일이 없음으로 무시해도 됨

### getter, Date 객체 출력

JS 에선 별도의 파싱포멧이 없음으로 아래와 같이 `getter` 함수를 사용해 직접 파서를 만들어야 함  

```js
let d = new Date('2018-11-20T09:00:00Z')
let year = d.getFullYear();
let month = d.getMonth() + 1;
let days = d.getDate();
let week = d.getDay();
let dayofWeeks = ["일", "월", "화", "수", "목", "금", "토"]

console.log(year + "년 " + month + "월 " + days + "일(" + dayofWeeks[week] +")");
// 2018년 11월 20일(화)
```

> `getYear()`: 현재년도 - 1900 을 반환, 2019년으로 설정된 Date객체의 경우 119를 반환.

여러가지 출력방법  

```js
let d = new Date('2018-11-20T09:00:00Z')
console.log(d.toString());           // Tue Nov 20 2018 18:00:00 GMT+0900 (대한민국 표준시)
console.log(d.toISOString());        // 2018-11-20T09:00:00.000Z
console.log(d.toDateString());       // Tue Nov 20 2018
console.log(d.toLocaleString());     // 2018. 11. 20. 오후 6:00:00
console.log(d.toLocaleDateString()); // 2018. 11. 20.
console.log(d.toLocaleTimeString()); // 오후 6:00:00
console.log(d.getTime());            // 1542704400000
```

### setter

```js
let d = new Date()
d.setTime(86400000)
console.log(d) // 1970-01-02T00:00:00.000Z

d.setFullYear(2000);
d.setMonth(5 - 1);
d.setDate(21);
console.log(d); // 2000-05-21T00:00:00.000Z

d.setDate(41);
console.log(d); // 2000-06-10T00:00:00.000Z
```

매개변수의 값이 지정범위 `date(1~31)`, `month(0~11)` 를 넘으면 다음, 이전달로 계산되어진다.  
