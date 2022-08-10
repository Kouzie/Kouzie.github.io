---
title:  "JavaScript 실행 컨텍스트, 클로저!"

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

## Promise

비동기 특성상 이벤트 처리 콜백 메서드의 순서를 지정할 수 없는데 `Promise` 객체를 사용하면  
문제를 해결할 수 있을 뿐 아니라 콜백 지옥을 해결 가능하다. 

> <https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Global_Objects/Promise>

`then`, `catch`, `finally` 3가지 콜백함수 등록이 가능  

> `finally` 는 ES2018 에 추가됨  

```js
var p = new Promise(function (resolve, reject) {
  /* 비동기적으로 수행할 작업 */
  if(...) {
      resolve(num); // 작업성공시 호출할 콜백
  } else {
      reject(num); // 작업 실패시 호출할 콜백
  }
});

// 위의 작업이 끝나 후 호출할 성공/실패 콜백 메서드 정의
p.then(function (num) {
    console.log("resolve invoked, num:", num); // 작업 성공 콜백
}).catch(function (num) {
    console.log("reject invoked, num:", num); // 작업 실패 콜백
}).finalnny(() => {
    console.log("Done!")
})
```

`p` 내부의 비동기 코드 수행 완료 후 `resolve-then`, `reject-catch` 세트로 정의된 콜백메서드가 호출된다.  

아래처럼 `then` 메서드에 `resolve`, `reject` 메서드를 모두 정의해도 된다.  

```js
p.then(
  (result) => console.log("resolve invoked, num:", result),
  (error) => console.log("reject invoked, num:", error)
);
```

### Promise Chaining - all

위의 `then`, `catch` 메서드는 또다른 `Promise` 객체를 반환하기 때문에 `then`, `catch` 를 여러개 묶을 수 있다.  

```js
const promiseFirst = new Promise(resolve => resolve(1)) // 그냥 실행 성공 콜백에 1 전달
    .then(result => `${result + 10}`) // 11 반환
    .then(result => {console.log(result); return result + 1;}) // 11출력 111 반환

const promiseSecond = new Promise(resolve => resolve(1))
    .then(result => `${result + 20}`) // 21 반환
    .then(result => {console.log(result); return result + 1;}) // 21 출력 211 반환

// 2개의 비동기 메서드 결합
Promise.all([promiseFirst, promiseSecond]).then(result => console.log(result))
```

문자열로 인식되어 `1`이 붙어 `111`, `211` 로 출력된다.  

```
11
21
[ '111', '211' ]
```

`then` 에 정의된 람다식을 실행 후 `resolve` 를 호출, 아래의 `then` 의 정의된 람다식을 호출, 이를 반복하여 `chaining` 을 구성한다.  

`Promise.all` 전역 메서드를 사용해 여러개의 `Promise` 객체를 병합하여 새로운 `Promise` 객체를 생성하는 것도 가능하다.  

### async, await

`async`, `await` 문법을 사용하면 `Promise` 를 좀 더 편하게 사용할 수 있다.  

함수 앞에 비동기 함수임을 뜻하는 `async` 키워드 사용  

값을 반환할 필요는 없지만 반환값이 있을경우 항상 `Promise` 객체로 감싸서 반환한다.  

```js
async function f() {
  return 1;
}
// 아래 함수와 동일함
// function f() {
//   return Promise.resolve(1);
// } 
f().then(alert); // 1
```

`await` 키워드는 `async` 메서드 내부에서만 사용가능하며 `Promise` 를 동기적으로 동작하도록 구성한다.  


```js
async function foo() {
    await 1
}
// 아래 함수와 동일함
// function foo() {
//     return Promise.resolve(1).then(() => undefined)
// }
f().then(alert); // 1
```

`await` 는 주로 네트워크 요청이나 파일읽기 같은 시간이 오래걸리는 함수호출문 앞에 사용한다.  

```js
const fs = require('fs');

const _p = (val) => {
    return new Promise((resolve, reject) => {
        fs.readFile('./test.txt', (err, data) => {
            if (err) {
                reject(err)
            }
            resolve(`${val}: ${data.toString()}`)
        })
    })
}

async function p() {
    try {
        var data1 = await _p(1);
        var data2 = await _p(2);
        var data3 = await _p(3);
        console.log(data1, data2, data3);
    } catch (err) {
        console.log('error! ' + err);
    }
}

p();
```

`try, catch` 를 사용해 비동기 메서드 안에서 `reject` 가 호출되거나 `throw` 로 예외를 발생시킨 것을 쉽게 처리할 수 있다.  

비동기 처리 메서드는 `Promise` 객체를 반환해야 `await`가 의도한 대로 동작한다.  

그렇다고 `async, await` 키워드로 비동기 처리를 하려고 꼭 `Promise` 객체를 정의할 필요는 없다.  
아래처럼 `async` 와 람다식을 사용해 비동기 메서드 정의가 가능하다.  

```js
const originPromise = new Promise((resolve, reject) => {
    console.log("origin called")
    resolve("origin promise");
});

originPromise.then(res => {
    console.log(res);
});

const newMetaPromise = async () => {
    console.log("new meta called")
    return "new meta promise";
}
newMetaPromise().then(res => {
    console.log(res);
});
// origin called
// new meta called
// origin promise
// new meta promise
```

비동기 처리를 위해 `callback`을 마지막 매개변수로 넘겨주거나 `Promise` 객체를 정의하는 것 보다  
아래처럼 비동기 메서드를 `async, await` 키워드로 한번 더 감싸면 코드는 조금 늘어나도 비동기적 사고는 적게할 수 있다.    

```js
const newMetaPromise = async () => {
    console.log("new meta called")
    return "new meta promise";
}

async function test() {
    let result = await newMetaPromise();
    console.log(result);   
}

test();
```

### 비동기 반복  

반복문 내부에 비동기 구문이 있을 경우  
반복문의 완료여부를 기다리고 싶을 때 **비동기 반복문**을 사용

```js
import axios from "axios";

const params = [1, 2, 3, 4];

const resArray = [];
params.forEach(async param => {
  const res = await axios.get(`https://jsonplaceholder.typicode.com/todos/${param}`);
  resArray.push(res.data);
});

console.log(resArray); // []
```

위처럼 비동기반복문을 사용할 경우 빈 배열이 출력된다.  

```js
import axios from "axios";

const params = [1, 2, 3, 4];

const resArray = [];
for await (const param of params) {
  const res = await axios.get(`https://jsonplaceholder.typicode.com/todos/${param}`);
  resArray.push(res.data);
}

console.log(resArray); // [x, x, x, x]
```

`for await ...of` 구문을 사용하면 반복문 내부의 모든 비동기함수가 완료될 때 까지 다음 구문을 대기한다.  
