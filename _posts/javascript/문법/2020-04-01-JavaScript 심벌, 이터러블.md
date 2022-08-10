---
title:  "JavaScript 심벌, 이터러블!"

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

## 심벌(Symbol)

ES6 에서 도입된 7번째 데이터 타입으로 **변경 불가능한 원시 타입의 값, 중복되지 않는 값**  


```js
const mySymbol = Symbol("kouzie");
console.log(typeof mySymbol); // symbol
console.log(mySymbol); // Symbol(kouzie)
console.log(mySymbol.toString()); // Symbol(kouzie)
console.log(mySymbol.description); // kouzie
```

심벌생성시의 문자열을 단순 description 일 뿐 심벌값의 영향을 끼치지 않는다.  

```js
const s1 = Symbol('mySymbol');
const s2 = Symbol('mySymbol');

console.log(s1 === s2); // false
```

아래와 같이 `enum` 과 같이 사용한다.  

```Js
const Direction = Object.freeze({
    UP: Symbol('up'),
    DOWN: Symbol('down'),
    LEFT: Symbol('left'),
    RIGHT: Symbol('right')
});

const myDirection = Direction.UP;

if (myDirection === Direction.UP) {
    console.log('You are going UP.');
}
```

### Symbol.for, Symbol.keyFor

`Symbol.for` 메서드를 사용하면 `심벌`을 을 `심벌 레지스트리`에 저장하고  
`Symbol.keyFor` 메서드를 사용하면 `심벌 레지스트리` 로부터 `key` 를 가져온다.  

> 심벌 레지스트리(global symbol registry): 문자열과 심벌을 `key-value` 로 하는 저장공간

```js
// mySymbol 문자열을 key 로 symbol 저장
const s1 = Symbol.for('mySymbol');
const s2 = Symbol.for('mySymbol'); // 중복저장되지 않음

console.log(s1 === s2); // true

// symbol 로부터 key 추출
let key = Symbol.keyFor(s1);
console.log(typeof key, key); // string mySymbol
```

`key` 로 심벌을 관리할 수 있기 때문에 직접 심벌을 생성하는 것 보다  
`Symbol.for` 메서드를 통해 심벌을 생성하고 관리하는 것이 효율적이다.  

### 심벌과 property  

객체의 property 는 문자열을 `key` 로 하는 `key-value` 형태의 데이터이지만  
심벌을 `key` 로 property 생성이 가능하다.  

> 심벌을 `key` 로 정의하거나 사용 시 대괄호 `[]` 를 사용해야 함  

```js
let obj = {
    [Symbol.for('test1')]: 1,
    [Symbol.for('test2')]: 1
};

console.log(obj[Symbol.for("test1")]) // 1
console.log(Object.keys(obj)); // []
console.log(Object.getOwnPropertyNames(obj)); // []
```

또한 심볼은 `for..in`, `Object.keys`, `Object.getOwnPropertyNames` 에서 감쳐진다.  

심볼로 감쳐진 property 의 `key` 목록을 보고싶다면 `Object.getOwnPropertySymbols` 함수 사용  

```js
let obj = {
    [Symbol.for('test1')]: 1,
    [Symbol.for('test2')]: 1
};

console.log(Object.getOwnPropertySymbols(obj));
// [ Symbol(test1), Symbol(test2) ]
```

### Symbol.iterator

JS 엔진에서 객체의 정체성을 표현하기 위한 사전정의된 심볼을 `well-known 심볼` 이라 한다.  

그중 자주사용되는 심볼이 `Symbol.iterator`  

`Array`, `String`, `Map`, `Set` 등의 순회가능한 객체(이터러블)들이 모두 `Symbol.iterator` 를 `key` 로 하는 메서드를 가지고 있다.  

`Symbol.iterator` 메서드를 호출하면 요소를 순회할 수 있는 `iterator` 를 반환하도록 설정되어있다.  

```js
console.log(arr[Symbol.iterator]) // [Function: values]
```

## 이터러블  

위에서 `Symbol.iterator` 심볼을 가진 객체를 이터러블객체라 하였다.  
그리고 이터러블로 판정된 객체는 `for..in`, `forEach` 같은 메서드로 순회가능하다.  

`Symbol.iterator` 함수로 순회할 수 있는 `iterator` 를 반환해야 하는데  
아래와 같은 형식을 가진다.  

![2](/assets/javascript/image14.png) 

`next` 함수를 가진 객체, `next` 함수는 `{value, doen}` property 로 가지는 객체를 매번 반환해야한다.  

배열의 `[Symbol.iterator]` 를 호출해 `iterator` 의 `next` 함수를 계속 호출하면 아래와 같다.  

```js
const array = [1, 2, 3];
const iterator = array[Symbol.iterator]();
console.log(iterator.next()); // { value: 1, done: false }
console.log(iterator.next()); // { value: 2, done: false }
console.log(iterator.next()); // { value: 3, done: false }
console.log(iterator.next()); // { value: undefined, done: true }
```

직접 정의하면 아래와 같다.  

```js
const iterable = {
    [Symbol.iterator]() {
        let cur = 1;
        const max = 5;
        // Symbol.iterator 메서드는 next 메서드를 소유한 이터레이터를 반환
        return {
            next() {
                return {
                    value: cur++,
                    done: cur > max + 1
                };
            }
        };
    }
};

console.log(Symbol.iterator in iterable); // true

for (const num of iterable) {
    console.log(num); // 1 2 3 4 5
}
```

