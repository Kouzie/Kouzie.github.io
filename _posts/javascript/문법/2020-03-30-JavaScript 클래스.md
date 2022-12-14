---
title:  "JavaScript 클래스!"

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


## 클래스  

클래스기반 언어에 익숙한 사용자들을 위해 추가되었다.  


```js
class Person {}

console.log(typeof Person); // function
const me = new Person();
console.log(me); // Person {}
```

클래스는 일종의 생성자 함수로 취급된다.  
실행 컨텍스트 생성 과정에서 클래스가 평가되어 생성자 함수 취급 되는 것  

좀더 조건이 치밀한 생성자함수라고 볼 수 있다.  

사용법 또한 `생성자함수 + prototype` 과 유사하다.  

![2](/assets/javascript/image10.png) 

클래스에 정의되는 `constructor`, `method` 는 함수객체 선언문과 유사하지만  
좀더 특별한 함수실행 컨텍스트를 생성한다고 보면 된다.  

JS 에서 `constructor` 를 별도로 정의하지 않았다면 비어있는 `constructor` 가 자동으로 설정된다.  

### 클래스 정의

함수정의할 때 리터럴 방식으로 정의하듯이  
클래스도 리터럴형식으로 정의 가능하다.  

```js
// 익명 클래스 표현식
const Person = class {};

// 기명 클래스 표현식
const Person = class MyClass {};
```

클래스는 왠만하면 선언문으로 정의하는 것을 권장한다.  

### 클래스 필드  

> 클래스 필드는 Chrome72 이상, 최신 Node.js(버전 12 이상) 에서 동작  

객체의 property 를 `클래스 필드` 문법으로 정의할 수 있다.  

> 기존에는 `constructor` 에서 `this` 키워드를 통해 지정.  


```js
class Person {
    // 클래스필드 선언
    name = "default"
    constructor() {
        // property 추가 기존방식
        this.age = 10
    }
}

let p1 = new Person();
console.log(p1) // Person { name: 'default', age: 10 }
```

### 클래스 접근자 함수

```js
class Person {
    name = "default"

    constructor() {
        this.age = 10
    }

    get introduction() {
        return `my name is ${this.name} and im ${this.age}`
    }

    set initial(str) {
        [this.name, this.age] = str.split(' ');
    }
}

let p1 = new Person();
console.log(p1.introduction) // my name is default and im 10

let p2 = new Person();
p2.initial = "kouzie 20"
console.log(p2.introduction) // my name is kouzie and im 20
```

### 클래스 접근제한자  

> 클래스 접근제한자는 Chrome72 이상, 최신 Node.js(버전 12 이상) 에서 동작  

클로저를 통해 변수 은닉(접근제한) 하였는데  
클래스에서는 `#` 키워드로 쉽게 접근제한자 설정이 가능하다.  

```js
class Person {
    #name = "default"

    constructor(age=10) {
        this.age = age;
        // this.#test = 'test'
        // SyntaxError: Private field '#test' must be declared in an enclosing class
    }

    get myName() {
        return this.#name;
    }
}

let p1 = new Person();
console.log(p1) // Person { age: 10 }
console.log(p1.name) // undefined
console.log(p1.age) // 10
console.log(p1.myName) // default
```

> 주의사항  
> `constructor` 에 접근제한자 필드 정의 불가능


## 상속  

`extends` 키워드를 통해 부모클래스-자식클래스 상속이 가능하다.  


> 아래와 같은 명칭으로 부른다.  
> 자식클래스: 하위클래스, 서브클래스, 파생클래스  
> 부모클래스: 상위클래스, 수퍼클래스, 베이스클래스  

```js
class Animal {
    constructor(age, weight) {
        this.age = age;
        this.weight = weight;
    }
    eat() { return 'eat'; }
    move() { return 'move'; }
}

// 상속을 통해 Animal 클래스를 확장한 Bird 클래스
class Bird extends Animal {
    fly() { return 'fly'; }
}

const bird = new Bird(1, 5);

console.log(bird); // Bird {age: 1, weight: 5}
console.log(bird instanceof Bird); // true
console.log(bird instanceof Animal); // true

console.log(bird.eat()); // eat
console.log(bird.move()); // move
console.log(bird.fly()); // fly
```

사실상 `prototype chain` 기능을 통해 상속이 이루어진다 볼 수 있다.  

![2](/assets/javascript/image11.png) 

기존 생성자함수는 자동으로 이루어지는 `Object` 혹은 `Function` 객체의 `prototype chain` 형성 외에 명시적으로 상속을 표현할 수 있는 **문법이 존재하지 않는다**.  

그런 의미에서 ES6 에 추가된 클래스 문법은 객체지향 관점에서 의미가 깊은 문법이다.  

또한 클래스 선언문과 생성자함수가 사실상 같은 취급이기에 섞어서 상속구현 또한 가능하다.  

```js
var Animal = (function () {
    function Animal(age, weight) {
        this.age = age;
        this.weight = weight;
    }
    Animal.prototype.eat = function () { return 'eat'; };
    Animal.prototype.move = function () { return 'move'; };
    return Animal;
}());

class Bird extends Animal {
    fly() { return 'fly'; }
}

const bird = new Bird(1, 5);
console.log(bird.eat());  // eat
```

### constructor - super

부모클래스의 생성자 혹은 필드, 메서드에 접근할 때 사용하는 키워드  

> `constructor` 에서 `super` 호출전엔 `this` 키워드 사용이 불가능  
> `constructor` 에서 `super` 호출이 없으면 에러가 발생  

```js
class Person {
    name = "default";
    age = 10;
    constructor(name, age) {
        this.name = name;
        this.age = age;
    }
}

class Student extends Person {
    nickname = "nickname"
    constructor(name, age, nickname) {
        super(name, age);
        this.nickname = nickname
    }
}

let std = new Student("kouzie", 20, "jason");
console.log(std); // Student { name: 'kouzie', age: 20, nickname: 'jason' }
```

위와같이 부모클래스의 필드초기화시 사용  

```js
class Person {
    name = "default";
    age = 10;
    constructor(name, age) {
        console.log("1.", this)
        console.log("2.", new.target)
        this.name = name;
        this.age = age;
    }
}

class Student extends Person {
    nickname = 'nickname'

    constructor(name, age, nickname) {
        // 암시적 instance 생성
        super(name, age)
        this.nickname = nickname
    }
}
let std = new Student("kouzie", 20, "jason");
console.log("3.", std)
// 1. Student { name: 'default', age: 10 }
// 2. [class Student extends Person]
// 3. Student { name: 'kouzie', age: 20, nickname: 'jason' }
```

또한 결과를 보면 `Person` 의 `constructor` 에서도 `Student` 의 인스턴스를 초기화 하는것을 확인할 수 있는데  

첫번째 출력문에서 필드초기화도 제대로 되지 않은것으로 보아  
`실행 컨텍스트`에서 클래스평가만 먼저 이루어지고 생성자의 호출 순서에 따라 코드실행되어 초기화되는 것을 확인할 수 있다.  

생성자함수 때와 마찬가지로 `this` 가 암묵적으로 반환 및 바인딩되어 
`constructor` 간 하나의 인스턴스를 초기화한다.  

### 암묵적 constructor 생성  

만약 자식클래스에서 `constructor` 를 별도정의하지 않을 경우 `constructor` 를 암시적 생성한다.  
부모클래스의 생성자에 맞게 `super` 를 호출한다.  

```js
class Person {
    name = "default";
    age = 10;
    constructor(name, age) {
        this.name = name;
        this.age = age;
    }
}

class Student extends Person {
    nickname = 'nickname'

    // 암시적 consturctor 생성
    // constructor(...agrs) {
    //     super(agrs);
    // }
}

let std = new Student("kouzie", 20, "jason");
console.log(std); // Student { name: 'kouzie', age: 20, nickname: 'nickname' }
```




### 의사 클래스 상속(pseudo classical inheritance)

기존에 생성자함수만을 사용했을 때에는 `의사 클래스 상속` 구현이 필요했다.  

```js
var Animal = (function () {
    function Animal(age, weight) {
        this.age = age;
        this.weight = weight;
    }
    Animal.prototype.eat = function () { return 'eat'; };
    Animal.prototype.move = function () { return 'move'; };
    return Animal;
}());

var Bird = (function () {
    function Bird() {
        Animal.apply(this, arguments);
    }
    // Animal.prototype 을 prototype 으로 가지는 새로운 객체를 생성
    Bird.prototype = Object.create(Animal.prototype);
    // constructor property 를 지정하고 
    Bird.prototype.constructor = Bird;
    Bird.prototype.fly = function () { return 'fly'; };
    return Bird;
}());
```

가독성이 안좋아 클래스 문법을 사용하는 것을 권장한다.  

### 동적 상속
 
```js
class Base1 { }
class Base2 { }
let condition = true;

// 조건에 따라 동적으로 상속 대상을 결정하는 서브클래스
class Derived extends (condition ? Base1 : Base2) { }

const derived = new Derived();
console.log(derived instanceof Base1); // true
console.log(derived instanceof Base2); // false
```

