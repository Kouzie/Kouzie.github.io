---
title:  "JavaScript 제어문!"

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


## for, continue, break  

JS 역시 타 언어와 같이 `for (var i = 1; i < 10; i++)` 형태로 for 문을 제공하고  
`continue`, `break` 키워드를 통해 탈출 가능하다.  

### for in

객체의 모든 열거할 수 있는 **프로퍼티(enumerable properties)** 를 순회할 수 있도록 해주는 반복문  

```js
const cls = { name: "kouzie", age: 27 }
for (let f in cls) {
    console.log(cls[f])
}
// kouzie
// 27
```

`Array` 의 경우 각 요소의 `propertie` 인 요소순서를 가져온다.  

```js
var arr = [1, 1, 2, 3, 4, 5];
for (let value in arr) {
    console.log(typeof value, value)
}
// string 0
// string 1
// string 2
// string 3
// string 4
// string 5
```

### for of

**iterable objects** 를 순회할 수 있도록 해주는 반복문


```js
var arr = [1, 1, 2, 3, 4, 5];
for (let value of arr) {
    console.log(typeof value, value)
}
// number 1
// number 1
// number 2
// number 3
// number 4
// number 5
```

```js
var arr = new Set([1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 5]);
for (let value of arr) {
    console.log(typeof value, value)
}
// number 1
// number 2
// number 3
// number 4
// number 5
```

### forEach

배열은 일반 `for`, `for..of`, `forEach` 사용을 권장한다.   

```js
var arr = new Set([1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 5, 5]);
arr.forEach(v => console.log(v)); // 1, 2, 3, 4, 5
```