---
title:  "JavaScript 객체!"

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

## object 개요  

> 출처: 모던 자바스크립트 Deep Dive

> 참고: <https://developer.mozilla.org/ko/docs/Web/JavaScript/Guide/Working_with_Objects>

JS 는 객체기반 언어이다. **원시타입 을 제외한 모든것이 객체라 할 수 있다**.  
심지어 일부 원시타입 wrapping 한 객체또한 존재한다(`String`, `Number`, `Boolean`)  

객체는 원시타입과 다르게 **변경가능한 값(mutable value)** 으로 깊은복사, 얕은복사 개념이 존재한다.  

### property

**객체는 property(`key`, `value`) 의 모음** 이라 할 수 있다.  

property 는 `camel case` 를 권장한다.  

```js
var person = {
    fistName: "Ko",
    'last-name': "zie" // 가능은 하나 권장 X
}
```

> 하이폰은 연산자로 취급되어 향후 property 값을 사용할 때에도 여러 문제가 발생한다.  

property 접근법 **마침표 표기법과 대괄호 표기법**이 있다.  

```js
var person = { fistName: "Ko", lastName: "zie" }
console.log(person.fistName)    // 마침표 표기법
console.log(person['lastName']) // 대괄호 표기법
```

동적으로 property 추가 삭제가 가능  

```js
var person = {}
person.fisrName = "Ko"
person["lastName"] = "zie"
console.log(person) // { fisrName: 'Ko', lastName: 'zie' }
```

```js
var person = { fistName: "Ko", lastName: "zie" }
delete person.fistName
console.log(person.fistName) // undefined
```

> 개인적으로 동적으로 추가/삭제 하는것은 권장하지 않는다.  

### method 

property 의 `value` 가 `function` 일 때 method 라고 명칭함  

```js
var s1 = {
    name: "홍길동",
    sayName1: function () {
        return `hello ${this.name}`
    },
    sayName2() { // 축약표현 method shorthand
        return `hello ${this.name}`
    }
};
console.log(s1.sayName1()) // hello 홍길동
console.log(s1.sayName2()) // hello 홍길동
```

### 접근자 함수  

`getter/setter` 함수라 부르기도 함

```js
const person = {
    name: 'lee',
    age: 20,

    get nameHello() {
        return `my name is ${this.name}`
    },

    set nameHi(str) {
        this.name = str + "new"
    }
}

console.log(person.nameHello) // my name is lee
person.nameHi = "Hi"
console.log(person.nameHello) // my name is Hinew
```

get, set 함수정의를 통해 마치 property 를 정의하듯 함수호출이 가능하다. 

### 내부슬롯, 내부메서드  

내부 슬롯과 내부 메서드는 자바스크립트 엔진의 내부 동작을 설명하기 위해 ECMAScript에서 정의한 **의사 프로퍼티(pseudo property)와 의사 메서드(pseudo method)**  

`[[...]]` 형태로 출력되며 property 와 method 처럼 외부에 공개되진 않지만 디버그창에서 확인할 순 있다.  

### property 확인 메서드  

```js
const person = {
    name: 'Lee',
    address: 'Seoul'
};

console.log('name' in person); // true
console.log(person.hasOwnProperty('name')); // true
console.log(Reflect.has(person, 'name')); // true

console.log(Object.keys(person)); // [ 'name', 'address' ]
console.log(Object.values(person)); // [ 'Lee', 'Seoul' ]
console.log(Object.entries(person)); // [ [ 'name', 'Lee' ], [ 'address', 'Seoul' ] ]

Object.entries(person).forEach(([key, value]) => console.log(key, value));
// name Lee
// address Seoul
```


## 객체를 생성하는 방법

### 리터럴{ } 을 사용한 객체생성  

중괄호 `{...}` 를 사용한 객체 생성

```js
var s1 = {
  name : "홍길동",
  age : 21,
  print: function () {
    return this.name + "/" + this.age;
  }
};
console.log(typeof s1);     //object
console.log(s1.name);       //홍길동
console.log(s1["name"]);    //홍길동
console.log(s1.print());    //홍길동/21
```

> **주의**
> 닫는 중괄호에 `;` 추가 간으  
> 리터럴은 블록으로 취급되지 않으며 블록스코프가 아님
 
### new Object() 를 사용한 객체 생성

`new Obejct()` 로 빈 객체를 생성후 property 와 메서드를 추가  

```js
var person = new Object();
person.firstName = "Hong";
person.lastName = "gildong";
person.age = "Hong";
person.print = function() {
  return this.firstName + " " + this.lastName;
};
```


`Object()` 는 사실 `built-in` 된 생성자함수로  
`String, Number, Boolean, Function, Array, Date` 등 여러 `built-in` 생성자함수가 존재한다.    

### 생성자함수를 사용한 객체 생성

JS에서도 클래스기반 언어처럼 `Class선언문`을 정의해서 인스턴스를 찍어낼 수 있는 문법이 있는데 **생성자함수** 이다.  

`this` 키워드를 사용해 생성된 객체에 프로퍼티를 정의하고 초기화한다.  

> 생성자함수에선 맨 앞의 문자를 대문자로 쓸 것 을 권장한다.  

```js
function Person(name, age, color) {
    this.name = name;
    this.age = age;
    this.color = color;

    this.print = function () {
        return this.name + " / " + this.age + " / " + this.color;
    }
}
var father = new Person("Hong", 30, "blue");
var son = new Person("Kim", 10, "red");

console.log(father.print()); // Hong / 30 / blue
console.log(son.print()); // Kim / 10 / red
```

### new 키워드  

생성자함수도 일반 함수이기 때문에 단순 호출시 `this.name`은 `name` 이라는 **전역변수**를 초기화 하는 문법이 되어버린다.  

때문에 함수앞에 암묵적으로 `new` 연산자를 통해 **인스턴스를 생성하고 생성자함수와 `this` 바인딩** 을 진행하고  
내부에서 `this` 키워드가 인스턴스의 공간을 가리키도록 한다.  

최종적으로 이미 바인딩된 인스턴스의 주소가 `new` 연산자의 **반환값**으로 사용된다.  

코드 `new Person(...)`가 실행될 때 다음과 같은 일이 발생한다  

1. 인스턴스 생성 및 생성자함수와 바인딩
2. `this` 를 통한 인스턴스 초기화
3. `new` 의 인스턴스 암묵적 반환

```Js
function Person(name, age, nickname) {
    // 1. 암묵적인 인스턴스 생성 및 new 와 바인딩
    console.log(this) // Person {}
    // 2. 초기화 진행
    this.name = name;
    this.age = age;
    this.nickname = nickname;

    // 3. 암묵적인 인스턴스 반환
    // return this; // 명시적 객체 반환
}

let p = new Person("kouzie", 20, 'jason');
console.log(p) // Person { name: 'kouzie', age: 20, nickname: 'jason' }
```

만약 생성자함수가 **명시적으로 객체를 반환** 하는경우 생성자함수의 최종 반환값은 명시적으로 반환된 객체이다.  

> 특별한 상황 외에 생성자함수에서 `return` 문을 권장하지 않는다.  

재밌는 점은 반환값이 객체가 아닌 원시타입일 경우 암묵적 인스턴스반환이 이루어진다는 것  

```js
function Circle(radius) {
    this.radius = radius;
    return 100;
}
const circle = new Circle(1);
console.log(circle); // Circle { radius: 1 }
```

생성자함수가 `new` 연산자와 같이 호출되었는지 확인하기 위해 `new.target` 을 사용한다.  

```js
function Circle(radius) {
  // 이 함수가 new 연산자와 함께 호출되지 않았다면 new.target은 undefined다.
  if (!new.target) {
    // new 연산자와 함께 생성자함수를 재귀 호출하여 생성된 인스턴스를 반환한다.
    return new Circle(radius);
  }

  this.radius = radius;
  this.getDiameter = function () {
    return 2 * this.radius;
  };
}
```

대부분 `built-in` 생성자함수가 위와같은 패턴을 가지고 있다.  
`new` 연산자와 같이 호출하지 않아도 동일하게 동작함  

> 함수앞에 `new` 연산자를 붙이는개 생소하지만 문법으로 받아들이자.  
> JS 에서 함수객체 내부에 일반함수 호출을 위한 `[call]`, 생성자함수 호출을 위한 `[constructor]` 가 별도로 정의되어있다.  
> https://developer.mozilla.org/ko/docs/Web/JavaScript/Reference/Operators/new


### Object.create() 로 객체 생성

명시적으로 `prototype 객체` 를 생성이 가능하다.  

아래처럼 prototype 을 `null` 로 지정하면 내부슬롯도 하나 없는 깔끔한 객체하나가 생성된다.  

```js
let obj = Object.create(null);
console.log(Object.getPrototypeOf(obj) === null); // true
// console.log(obj.toString()); 
// TypeError: obj.toString is not a function
```

`리터럴` 이나 `생성자함수`로 만든 객체는 아래와 동일하다.  

```js
obj = Object.create(Object.prototype);
```

### OrdinaryObjectCreate

`리터럴`, `생성자함수`, `Object.create` 로 객체를 생성할 때 내부에서 공통적으로 추상연산인 `OrdinaryObjectCreate` 이 호출된다.  

`Object.prototype` 객체를 인스턴스에 연결한다.  


## data property, accessor property

객체의 property 는 원시타입이나 하위 객체를 참조하는 단순변수가 아니다.  

객체의 property 는 **data property, accessor property** 로 나뉘며  
지금까지 정의했던 property 들은 모두 data property이다.  

**data property, accessor property 에 여러가지 attribute 가 내부슬롯으로 존재**하며  
이를 이용해 마치 일반 변수처럼 동작한다.   

### data property attribute 

```js
const person = {
    name: 'lee',
    age: 20
}
console.log(Object.getOwnPropertyDescriptor(person, 'name'))
// { value: 'lee', writable: true, enumerable: true, configurable: true }

// getOwnPropertyDescriptors 로 모든 property 한번에 확인 가능  
console.log(Object.getOwnPropertyDescriptors(person));
// {
//     name: {value: 'lee', writable: true, enumerable: true, configurable: true}
//     age: { value: 20, writable: true, enumerable: true, configurable: true }
// }
```

`Descriptor 함수` 를 사용해보면 `data property` 에 `value, writable, enumerable, configurable` 4가지 내부슬롯이 있음을 알 수 있다.  

**value**
key 를 통해 접근하면 반환되는 `value`

**writable**
`value` 의 변경가능 여부를 나타냄

**enumerable**
`value` 의 열거가능(iterable) 여부를 나타냄, `for..in` 과 같은 연산자에 열거되지 않음  

**configurable**
property 의 삭제, `value` 의 재정의 가능여부, `writable` 의 상위개념
`property attribute` 변경 또한 막아버린다.  

### accessor property attribute

```js
const person = {
    name: 'lee',
    age: 20,

    get nameHello() {
        return `my name is ${this.name}`
    },

    set nameHi(str) {
        this.name = str + "new"
    }
}

console.log(Object.getOwnPropertyDescriptor(person, 'nameHello'))
// { get: [Function: get nameHello], set: undefined, enumerable: true, configurable: true }
```

`accessor property` 역시 내부슬롯을 가지고있다.  

**get**  
접근자 함수의 value, 호출시 함수가 실행되고 결과가 반환됨  

**set**  
접근자 함수의 value, 호출시 함수가 실행됨  

> 그 외의 속성은 `data property attribute` 와 동일함

### 객체 변경 방지  

내부슬롯 은 외부로 노출되지 않기때문에 수정하려면 JS 에 `built-in` 되어있는 함수를 사용하여 수정해야한다.  

`Object.defineProperty` 혹은 `Object.defineProperties` 를 사용해 property attribute 를 조작 가능  

```js
Object.defineProperties(person, {
  // 데이터 프로퍼티 정의
  firstName: {
    value: 'Ungmo',
    writable: true,
    enumerable: true,
    configurable: true
  },
  lastName: {
    value: 'Lee',
    writable: true,
    enumerable: true,
    configurable: true
  },
  // 접근자 프로퍼티 정의
  fullName: {
    // getter 함수
    get() {
      return `${this.firstName} ${this.lastName}`;
    },
    // setter 함수
    set(name) {
      [this.firstName, this.lastName] = name.split(' ');
    },
    enumerable: true,
    configurable: true
  }
});
```

| 메서드 | 프로퍼티 추가 | 프로퍼티 삭제 | 프로퍼티 값 읽기 | 프로퍼티 값 쓰기 | 프로퍼티 어트 리뷰트 재정의 |  
|---|---|---|---|---|---|---|  
`Object.preventExtensions` | ✕ | ◯ | ◯ | ◯ | ◯  
`Object.seal` | ✕ | ✕ | ◯ | ◯ | ✕  
`Object.freeze` | ✕ | ✕ | ◯ | ✕ | ✕  


`Object.freeze` 에 대해서만 살펴보면  

```js
const person = { name: 'Lee' };

// person 객체는 동결(freeze)된 객체가 아니다.
console.log(Object.isFrozen(person)); // false

// person 객체를 동결(freeze)하여 프로퍼티 추가, 삭제, 재정의, 쓰기를 금지한다.
Object.freeze(person);

// person 객체는 동결(freeze)된 객체다.
console.log(Object.isFrozen(person)); // true

// 동결(freeze)된 객체는 writable과 configurable이 false다.
console.log(Object.getOwnPropertyDescriptors(person));
// { name: {value: "Lee", writable: false, enumerable: true, configurable: false} }

// 프로퍼티 추가가 금지된다.
person.age = 20; // 무시. strict mode에서는 에러
console.log(person); // {name: "Lee"}

// 프로퍼티 삭제가 금지된다.
delete person.name; // 무시. strict mode에서는 에러
console.log(person); // {name: "Lee"}

// 프로퍼티 값 갱신이 금지된다.
person.name = 'Kim'; // 무시. strict mode에서는 에러
console.log(person); // {name: "Lee"}

// 프로퍼티 어트리뷰트 재정의가 금지된다.
Object.defineProperty(person, 'name', { configurable: true });
// TypeError: Cannot redefine property: name
```

`Object.freeze` 사용하여 객체의 중첩 객체까지 불변으로 만드려면 재귀함수를 돌며 필드까지 모두 `freeze` 해야한다.  

```js
function deepFreeze(target) {
  // 객체가 아니거나 동결된 객체는 무시하고 객체이고 동결되지 않은 객체만 동결한다.
  if (target && typeof target === 'object' && !Object.isFrozen(target)) {
    Object.freeze(target);
    /*
      모든 프로퍼티를 순회하며 재귀적으로 동결한다.
      Object.keys 메서드는 객체 자신의 열거 가능한 프로퍼티 키를 배열로 반환한다.
      forEach 메서드는 배열을 순회하며 배열의 각 요소에 대하여 콜백 함수를 실행한다.
    */
    Object.keys(target).forEach(key => deepFreeze(target[key]));
  }
  return target;
}

const person = {
  name: 'Lee',
  address: { city: 'Seoul' }
};

// 깊은 객체 동결
deepFreeze(person);

console.log(Object.isFrozen(person)); // true
// 중첩 객체까지 동결한다.
console.log(Object.isFrozen(person.address)); // true

person.address.city = 'Busan';
console.log(person); // {name: "Lee", address: {city: "Seoul"}}
```

## Prototype

> Prototype: 원형
> https://medium.com/@bluesh55/JavaScript-prototype-이해하기-f8e67c286b67

**JS는 프로토타입 기반 객체지향 프로그래밍 언어**이다.  

`Porototpye` 은 클래스기반 언어보다 효율적으로 상속과 객체 선언 등의 문법을 제공한다.  

JS 의 모든 객체는 `Prototype 객체` 를 내부 슬롯으로 가지고있으며   
모든 생성자함수에서 인스턴스 생성시 `Prototype 객체` 를 배부한다.  

```js
// 생성자함수
function Circle(radius) {
  this.radius = radius;
  this.getArea = function () {
    return Math.PI * this.radius ** 2;
  };
}

const circle1 = new Circle(1);
const circle2 = new Circle(2);
console.log(circle1.getArea === circle2.getArea); // false
```

![2](/assets/javascript/image2.png) 

`Circle` 생성자함수로 만들어진 인스턴스마다 계속하여 getArea 메서드가 정의된다(메모리낭비).  

```js
function Circle(radius) {
    this.radius = radius;
}
Circle.prototype.getArea = function () {
    return Math.PI * this.radius ** 2;
};
```

![2](/assets/javascript/image3.png) 

**생성자함수는 `prototype` 라는 특별한 property 를 가지고 있으며**  
인스턴스생성시 `prototype 객체` 주소를 발부할 때 사용한다.  

`생성자함수.prototype === prototype 객체`

효율적인 메모리 관리와 static 변수와 같은 기능구현도 가능하다.  

`prototype 객체` 라고 특별한 객체가 아니다.  
`constructor` property 가 존재하는(생성자함수 참조) 일반 객체이다.  

일반적으로 가독성을 위해 생성자함수 정의와 protoype 정의를 하나의 함수 리터럴에서 처리한다.  

```js
const Circle = (function () {
    function Circle(radius) {
        this.radius = radius;
    }
    Circle.prototype.getArea = function () {
        return Math.PI * this.radius ** 2;
    };
    return Circle;
})();

let c1 = new Circle(1);
console.log(c1.getArea()) // 3.141592653589793
```

### `__proto__`

인스턴스는 `__proto__` 라는 `accessor property` 를 통해 `Prototype 객체` 에 접근 가능하다.  

```js
// Circle 클래스의 prototype 객체 출력
console.log(Object.getOwnPropertyDescriptors(new Circle(1).__proto__))
/* 
{
  constructor: {
    value: [Function: Circle],
    writable: true,
    enumerable: false,
    configurable: true
  },
  getArea: {
    value: [Function (anonymous)],
    writable: true,
    enumerable: true,
    configurable: true
  }
}
*/

// Object 클래스의 prototype 객체 출력
console.log(Object.getOwnPropertyDescriptors({}.__proto__))
/* 
{
  constructor: {
    value: [Function: Object],
    writable: true,
    enumerable: false,
    configurable: true
  },
  ...
  hasOwnProperty: {
    value: [Function: hasOwnProperty],
    writable: true,
    enumerable: false,
    configurable: true
  },
  ...
  isPrototypeOf: {
    value: [Function: isPrototypeOf],
    writable: true,
    enumerable: false,
    configurable: true
  },
  propertyIsEnumerable: {
    value: [Function: propertyIsEnumerable],
    writable: true,
    enumerable: false,
    configurable: true
  },
  ...
  ['__proto__']: {
    get: [Function: get __proto__],
    set: [Function: set __proto__],
    enumerable: false,
    configurable: true
  },
}
*/
```

자바스크립트 엔진은 접근하려는 property 가 없다면 `__proto__` 로부터 자신의 `prototype 객체`, 이어서 자신부모의 `prototype 객체` 순차적으로 검색하는데 이를 **Prototype Chain** 이라 한다.  

`__proto__` 가 `circle` 객체의 `prototype 객체`에 존재하지 않아서 `Object` 의 `__proto__` 를 호출하여 `circle` 의 `prototype` 객체에 접근한다.  

### 상속 - Prototype Chain

`prototype 객체` 는 특별한 객체가 아닌 생성자함수를 참조하는 `constructor` property 가 있는 개체라 하였는데  

역시 내부슬롯으로 `prototype 객체`를 가지게 되는데 마치 상속처럼 동작하게 된다.  

실제 디버그창에서 `circle` 인스턴스를 출력해보면 아래와 같다.  

![2](/assets/javascript/image4.png) 

`circle` 객체의 `prototype 객체` 에도 `prototype 객체` 가 있는데  
`Object` 클래스의 `Prototype 객체` 이다  

대부분의 객체는 `prototype 객체` 을 가지고 있으며 연결된 `prototype 객체` 를 타고 올라가면 상위는 `Object` 클래스의 `prototype 객체` 이다.  


### 생성자함수 - prototype property

**함수객체만이 소유하는 property** 로  
생성자함수가 발급할 `prototype 객체`의 주소와 동일하다.  

```js
// 생성자함수
function Person(name) {
    this.name = name;
}
const me = new Person('Lee');

// 결국 Person.prototype과 me.__proto__는 결국 동일한 프로토타입을 가리킨다.
console.log(Person.prototype === me.__proto__);  // true
```

`prototype 객체` 는 은 생성자함수가 생성되는 시점에 생성된다
생성자함수과 `prototype 객체` 는 항상 한쌍으로 구성된다 (`constructor - prototype`)

![2](/assets/javascript/image5.png) 

> 화살표 함수는 `prototype property` 를 사용하지 않음으로 생성자함수로 사용할 수 없다. `non-constructor` 함수라 한다.  

생성자함수의 `prototype property` 자체를 교체가능하다.  

```js
const Person = (function () {
    function Person(name) {
        this.name = name;
    }

    Person.prototype = {
        constructor: Person,
        sayHello() {
            console.log(`Hi! My name is ${this.name}`);
        }
    };

    return Person;
}());
```

즉시실행함수로 `Person` 생성자함수를 반환  
즉시실행함수내부에서 생성자함수 정의와 `prototype 객체` 교체가 같이 이루어진다.  

이번에는 생성된 객체의 `prototype 객체` 교체를 진행한다.  

```js
function Person(name) {
    this.name = name;
}

const me = new Person('Lee');
const you = new Person('Ko');

// 프로토타입으로 교체할 객체
const parent = {
    sayHello() {
        console.log(`Hi! My name is ${this.name}`);
    }
};

Object.setPrototypeOf(me, parent);

console.log(me.constructor === Person);  // false
console.log(you.constructor === Person);  // true
```

객체 하나에 대해서만 교체가 진행되고 `constructor` 필드의 연결이 파괴된다.  
교체할 `prototype 객체`에 기존 `constructor` 까지 재설정하는 것을 권장한다.  

### instanceof 와 prototype

위에서 `prototype` 객체를 교체한 `me` 객체의 경우 `instanceof` 로 `Person` 객체인지 검사하면 `false` 로 나온다.  

```js
function Person(name) {
    this.name = name;
}

const me = new Person('Lee');
const you = new Person('Ko');

// 프로토타입으로 교체할 객체
const parent = {
    sayHello() {
        console.log(`Hi! My name is ${this.name}`);
    }
};

Object.setPrototypeOf(me, parent);

console.log(me.constructor === Person);  // false
console.log(me instanceof Person) // false
```

즉 `instance of` 명령어는 객체의 `prototype 객체`와, 생성자함수의 `prototype 객체`가 일지하는지 여부를 검사하는 함수라 할 수 있다.  

```js
function Person(name) {
    this.name = name;
}

let obj = Object.create(Person.prototype);
console.log(obj instanceof Person) // true
console.log(obj instanceof Object) // true
```

실제 `Person` 생성자함수로 만든 객체가 아니고 `prototype` 객체만 일치하면 `true` 로 반환한다.  

> `prototype chain` 으로 인해 상위 `prototype 객체`를 `instance of` 로 검사해도 true 가 출력됨

### 주의사항  

`__proto__` 사용을 권장하지 않는다.  
상속구조에 따라 `Object.prototype` 을 상속받지 않았을 때 `__proto__` 이 오류를 야기한다.  
`set __proto__` 로 바라보고 있는 `prototype 객체` 를 변경시키는 것도 비정상적 상황을 야기한다.  

`Object.prototype` 의 빌트인 메서드 `hasOwnProperty`, `isPrototypeOf`, `propertyIsEnumerable` 등을 객체가 직접 호출하는 것을 권장하지 않는다.  
마찬가지로 `Object.prototype` 을 상속받지 않았을 떼 오류를 야기한다.  
아래처럼 직접호출을 권장

```js
Object.prototype.hasOwnProperty.call(obj, 'a')
```

## 표준 built-in 객체  

`ECMAScript` 사양에 정의된 40 여개의 `표준 built-in` 객체

- `Object`  
- `String`  
- `Number`  
- `Boolean`  
- `Symbol`  
- `Date`  
- `Math`  
- `RegExp`  
- `Array`  
- `Map/Set`  
- `WeakMap/ WeakSet`  
- `Function`  
- `Promise`  
- `Reflect`  
- `Proxy`  
- `JSON`  
- `Error`  


### 원시타입과 래퍼객체  

```js
const str = "hello"
console.log(Object.getPrototypeOf(str) === String.prototype) // true

const num1 = 1.5;
console.log(num1.toFixed()) // 2
```

JS 엔진이 일시적으로 원시값을 연관된 객체로 변환하고 작업이 끝나면 다시 원시값으로 되돌린다.

이때 `String`, `Number`, `Boolean` 처럼 접근하면 생성되는 임시객체를 래퍼객체라 한다.  

> 임시사용한 래퍼객체는 가비지 컬렉터의 대상이 됨으로 성능하락의 원인이 됨으로 권장하지 않는다.  


### 전역객체  

**전역객체(global object)** 는 코드가 실행되기전 JS 엔진에 의해 제일 먼저 생성되는 특수한 객체  

브라우저 환경에서는 `window`  
`Node.js` 환경에서는 `global`  

`local scope` 밖에서 정의된 `var 변수`, `전역함수`는 전역객체의 property 로 포함되게 된다(**암묵적 전역**).  

```js
var foo = 1; // 전역 변수
bar = 2; // 암묵적 전역, 전역객체의 프로퍼티로 사용됨
console.log(window.foo); // 1
console.log(window.bar); // 2

// 전역 함수
function baz() { return 3; }
console.log(window.baz()); // 3
```

#### 전역객체 property

사실 전역객체는 위에서 말한 표준 `built-in` 객체들을 property 로 가지고 있다.  
지금까지 built-in 함수를 바로 호출하는 건 전역객체 참조이름을 생략하고 호출했던 것  

```js
const num1 = new global.Number(2)
console.log(num1) // [Number: 2]
global.parseInt === parseInt; // -> true
global.parseInt('F', 16); // -> 15
```

표준 `built-in` 객체들을 제외하고 아래와 같은 전역객체 property(객체, 함수) 가 존재한다.  

- `Infinity`  
- `NaN`  
- `undefined`  
- `eval`  
- `isFinite`  
- `isNaN`  
- `parseFloat`  
- `parseInt`  
- `encodeURI`, `decodeURI`  
- `encodeURIComponent`, `decodeURIComponent`  
