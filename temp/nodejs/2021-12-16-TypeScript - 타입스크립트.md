---
title:  "타입 스크립트 - 개요!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - typescript

tags:
  - typescript

toc: true
toc_sticky: true

---

## 개요

MS가 개발하고 유지하고 있는 언어로 2012 처음 발표  

`Angular`, `React`, `Vue` 모두 타입스크립트를 사용해 개발되고 있음  

다음과 같이 변수타입을 지정하여 컴파일 에러로 파악할 수 있다.  

```ts
function makePerson(name: string, age: number) {}
```

대규모 소프트웨어를 개발할 떄 TS 를 주로 사용한다.  

## 설치


```
npm i -g typescript     # 컴파일러 설치
tsc -v                  # 버전 확인
 > Version 4.1.3
tsc --help              # 도움말 출력
 > Version 4.1.3
tsc test.ts             # 컴파일
```

```ts
// test.ts
let msg: string = 'hello world';
console.log(msg);
```

```js
//생성된 JS 파일
var msg = 'hello world';
console.log(msg);
```

TS 프로젝트 생성은 아래와 같다.  

```
$ npm init
$ npm i -D typescript ts-node @types/node
$ tsc --init # tsconfig.json 생성
```

TS 는 JS 기반의 별도의 컴파일 과정이 필요한 언어이기에 여러가지 config 설정들이 포함된다.  

```json
// default tsconfig.json
{
    "compilerOptions": {
        "target": "es5",                          /* Specify ECMAScript target version: 'ES3' (default), 'ES5', 'ES2015', 'ES2016', 'ES2017', 'ES2018', 'ES2019', 'ES2020', or 'ESNEXT'. */
        "module": "commonjs",                     /* Specify module code generation: 'none', 'commonjs', 'amd', 'system', 'umd', 'es2015', 'es2020', or 'ESNext'. */
        "strict": true,                           /* Enable all strict type-checking options. */
        "esModuleInterop": true,                  /* Enables emit interoperability between CommonJS and ES Modules via creation of namespace objects for all imports. Implies 'allowSyntheticDefaultImports'. */
        "skipLibCheck": true,                     /* Skip type checking of declaration files. */
        "forceConsistentCasingInFileNames": true  /* Disallow inconsistently-cased references to the same file. */
    }
}
```

아래처럼 변경

```json
// default tsconfig.json
{
    "compilerOptions": {
        "target": "es5",            // 트랜스 파일 버전
        "module": "commonjs",       // 컴파일 모듈 방식 
        "moduleResolution": "node", // 컴파일 플랫폼
        "baseUrl": ".",
        "outDir": "dist",           // 출력 파일 위치
        "paths": {                  // import 위치
            "*": ["node_modules/*"]
        },
        "esModuleInterop": true,
        "sourceMap": true,
        "downlevelIteration": true,
        "noImplicitAny": true,
    },
    "include": ["src/**/*"]
}
```

`module` : JS 는 브라우저에선 `AMD(async moudle definition)` 방식, `Nodejs` 에선 `commonjs` 방식으로 동작한다. `module` 에 플렛폼에 맞는 모듈 방식으로 컴파일 방법 설정  

`moduleResolution` : `commonjs` 일 경우 `node`, `amd` 방식일 경우 `classic` 으로 설정한다.  

`target` : 트랜스파일할 자바스크립트 버전설정, 대부분 `es5` 를 사용하며 최신버전의 `Nodejs` 를 사용한다면 `es6` 설정 가능  

`baseUrl, outDir` - 트랜스파일된 JS 파일을 저장하는 위치 설정, `tsconfig.json` 를 기반으로 설정되며 대부분 `.` 을 사용해 현재 디렉토리에 `dist` 폴더를 생성하고 출력파일을 저장한다.  

`paths` - import 문에서 from 부분 해석시 찾는 위치

`esModuleInterop` - `AMD` 방식으로 구현된 외부 패키지 사용시 `true` 로 설정해야 사용 가능  

`sourceMap` - `dist` 에 `.js.map` 생성, JS 코드와 TS 코드 위치를 매핑함(디버깅시 사용)  

`downlevelIteration` - TS 구문의 `generator` 사용시 `true` 필수

`noImplicitAny` - 타입 미지정과 같은 소소한 `warning` 을 `true` 설정시 무시  

간단한 객체 생성 및 테스트

```typescript
// index.tx
let MAX_AGE = 100

interface IPerson {
    name: string
    age: number
}

class Person implements IPerson {
    constructor(public name: string, public age: number) { }
}

function makeRandomNumber(max: number = MAX_AGE): number {
    return Math.ceil((Math.random() * max))
}

const makePerson = (name: string, age: number = makeRandomNumber()) => ({ name, age })

const testMakePerson = (): void => {
    let jane: IPerson = makePerson('Jane')
    let jack: IPerson = makePerson('Jack')
    console.log(jane, jack)
}
testMakePerson()
```

TS 코드 실행을 위해 간단한 스크립트를 `package.json` 에 설정  

```json
// package.json
{
    ...
    "scripts": {
        "dev": "ts-node src", // ts-node 를 통해 typescript 코드 바로 실행
        "build": "tsc && node dist" // node 로 실행할 수 있는 javascript 문법으로 변환
    }
}
```

```
$ npm run build 
$ npm run dev
{ name: 'Jane', age: 50 } { name: 'Jack', age: 87 }
```


### 자료형

| 자바스크립트 | 타입스크립트 |
| ------------ | ------------ |
| `Number`     | `number`     |
| `Boolean`    | `boolean`    |
| `String`     | `string`     |
| `Object`     | `object`     |

```js
// java script
let var1 = 1
const var2 = 'string'
```

타입스크립트에선 **타입 주석** 을 사용해 변수의 타입을 지정한다.  

```ts
// type script
let var1: number = 1
const var2: string = 'string'
let var3: boolean = true
let var4: object = {}
```

자바스크립트의 경우 `let` 으로 선언된 변수는 자유롭게 변환이 가능하지만  
타입스크립트의 경우 해당 타입의 값으로만 변경가능  

```ts
// type script
let var1: number = 1
var1 = 'hello' // error TS2322: Type 'hello' is not assignable to type 'number'.
```

> 타입스크립트에는 타입 추론이 있기 때문에 변수 선언시 꼭 타입을 지정할 필요는 없다.  
물론 변수가 정의된 후에 다른 타입으로 변경은 불가능

타입 지정은 사용자 오류를 감소시키지만 불편하기도 하기에 모든 타입을 사용할 수 있는 `any` 타입을 지원한다.  

```ts
let var1: any = 1
var1 = 'string'
var1 = true
var1 = {}
```

또 다른 특수 타입으로 `undefined` 가 있다  
`undefined` 타입은 오직 `undefined` 값만을 가질 수 있다.  

```ts
let var1: undefined = undefined
var = 1 // error TS2322: Type '1' is not assignable to type 'undefined'.
```

![image1](/assets/ts/image1.png)

위에서 소개한 자료형 외에도 타입스크립트에는 여러 타입들이 존재한다.  

또한 트리 상위에 해당되는 타입은 하위에 해당되는 타입으로 변환이 가능하고  
하위에 해당하는 타입은 상위로는 변경 불가능하다.  

## TS 고유문법

JS 에는 없는 TS 에서 제공하는 문법을 알아본다.  

어쨋건 TS 역시 컴파일하면 JS 로 출력되기 때문에  
문법에 맞는 오류없는 JS 코드를 생성한다는 것을 잊으면 안된다.  


## 인터페이스  

`object` 의 하위 타입, 

```ts
interface IPerson {
    name: string // 필수속성
    age: number
    etc?: boolean // 선택속성
}
```

인터페이스 정의시 없으면 안되는 속성을 **필수속성**  
생략 가능한 속성의 경우 **선택속성** 으로 정의가능  
모든 필드는 반드시 **기본 자료형**을 가져야 한다.  

다음과 필수속성을 초기화 안하거나 unknown 필드 사용시 에러가 발생한다.  

```js
let p1: IPerson = { name: 'Ko', age: 25 }
let p2: IPerson = { name: 'Kim' } // Property 'age' is missing in type '{ name: string; }' but required in type 'IPerson'.
let p3: IPerson = {} // Type '{}' is missing the following properties from type 'IPerson': name, age
let p4: IPerson = { name: 'Hong', age: 26, test: "test" }
//  '{ name: string; age: number; test: string; }' is not assignable to type 'IPerson'.
//  Object literal may only specify known properties, and 'test' does not exist in type 'IPerson'.
```

인터페이스는 규약일뿐 실제 필드가 존재하지 않는다.  

```java
interface IPerson {
    name: string
    age?: number
}

class Person implements IPerson{
    constructor(public name: string, age?: number) { }
}
```

구현시 반드시 해당 인터페이스가 정의하고 있는 필드를 속성으로 포함해야 한다.  

### 익명 인터페이스  

- 메서드에서 특정 필드를 가지는 객체를 매개변수로 받고 싶을 때  
- 인터페이스처럼 필드를 가지고 있는 객체를 생성하고 싶을 때  

다음과 같이 함수 구현시에 자주 사용  

```ts
function printMe(me: { name: string, age: number, etc?: boolean }) {
    console.log(me.etc ? `${me.name} ${me.age} ${me.etc}` : `${me.name} ${me.age}`)
}
let person = { name: 'jack', age: 32 }
printMe(person); // jack 32
```

객체 생성에도 사용 가능하다.  

```ts
let person: {
    name: string
    age: number
    etc?: boolean
} = { name: 'jack', age: 32 }
```


## 제네릭

사용법  

```ts
class Container<T> {
    value: T
}

function g2<T, Q>(a: T, b: Q): void {}
```

타입추론기능  

```ts
printValue(new Valuable<number>(1))
printValue(new Valuable(1))
```

타입제약기능

```ts
interface IValuable<T> {
    value: T
}

class Container<T extends IValuable<T>> {
    value: T
}
```

### new 타입 제약  

타입을 매개변수로 전달해서 new 키워드로 객체를 생성하는 함수  

```ts
function create<T>(test: T): T {
    return new test();
    // 'unknown' 형식에 구문 시그니처가 없습니다.ts(2351)
}
let d=create(Date)
console.log(d)
```

제네릭으로 사용한 `T` 는 `test` 변수의 타입이라 할 수 있는데  
`test` 마저 변수의 타입이다 보니 타입의 타입이라 할 수 있다.  

```ts
function create<T>(type: new() => T): T {
    return new type();
}

let d=create(Date)
console.log(d)
```

```ts
function create<T>(type: new(...args) => T, ...args): T {
    return new type(args);
}

let d=create(Date, 2023,1,1)
console.log(d)
```

`new() => T` 로 

## 함수

TS 에서 강제하는 여러가지 설정들이 있음  

### 반환값  

TS 에선 함수 반환값을 지정할 수 있다.  
생략될 경우 자동인식하여 반환값을 지정  

```js
// 반환값 number 로 자동지정
function sum1(a: number, b: number) {
    return a + b;
}

function sum2(a: number, b: number): number {
    return a + b;
}

const sum3 = (a: number, b: number): number => a + b

let result1: number = sum1(1, 2)
let result2: number = sum2(3, 4)
let result3: number = sum3(5, 6)
console.log(result1) // 3
console.log(result2) // 7
console.log(result3) // 11
```

반환값이 없다면 `void` 를 지정한다.  

```ts
function printMe(name: string, age: number): void {
    console.log(`name: ${name}, age: ${age}`);
}
```


### 함수 시그니처  

함수객체를 변수에 할당시 매개변수, 빈환값을 모두 표현하는 함수 시그니처 사용
생략될 경우 자동인식하여 함수 시그니처 지정

```ts
let printMe: (string, number) => void =
    function (name: string, age: number): void {
        console.log(`name: ${name}, age: ${age}`);
    }
```

`(string, number) => void` 이게 하나의 타입이라 할 수 있다.  
함수 시그니처를 `type` 키워드로 별칭으로 만들어 사용할 수 있다.  

```ts
type strNumVoid = (string, number) => void
let printMe: strNumVoid = function (name: string, age: number): void {
    console.log(`name: ${name}, age: ${age}`);
};
```

### readonly 

함수 매개변수에 `readonly` 키워드를 사용할 수 있으며 참조객체의 변경을 막을 수 있다.  

```ts
function arrPush(arr:readonly number[]) {
    arr.push() // 에러발생
}
```

## 클래스

`public`, `protected`, `private` 와 같은 접근제한자를 제공한다.  
생략시 기본 `public` 으로 취급한다.  

`static` 키워드로 정석속성 선언도 가능하다.  

```ts
class Person {
    private name: string
    age?: number // default public 
    static INT_VALUE: number = 1

    constructor(name: string, age?: number) {
        this.name = name
        this.age = age
    }
    
    printMe(): void { // 메서드 정의
        console.log(`name: ${this.name}, age: ${this.age}`)
    }
}

let jack: Person = new Person("jack", 25)
console.log(Person.INT_VALUE) // 1
console.log(jack) // Person { name: 'jack', age: 25 }
// console.log(jack.name) error
jack.printMe()
```

### 인터페이스  

TS 의 인터페이스는 구현해야하는 메서드와 필드를 강제한다.  

```ts
interface IPerson {
    name: string
    age: number
    printMe(): void;
}

// name age 중 하나라도 없을경우 컴파일 오류
let p1: IPerson = {
    name: "kouzie", age: 20,
    printMe: function (): void {
        throw new Error("Function not implemented.");
    }
};

// 인터페이스 구현체  
class Student implements IPerson {
    // name, age 중 하나라도 없을경우 컴파일 오류
    name: string;
    age: number;
    grade: number;
    // 구현필수
    printMe(): void {
        throw new Error("Method not implemented.");
    }
}
```

### 추상 클래스  

`abstract` 키워드로 추상클래스 생성 가능  
`abstract` 키워드로 추상메서드 구현을 강제한다.  

```ts
abstract class AbstractPerson {
    name: string
    age?: number

    constructor(age?: number) {
        this.age = age;
    }
    // 기본메서드
    sayHi(): void {
        console.log(`hi i'm ${this.age}`)
    }
    // 추상메서드
    abstract printMe(): void;
}

class Person extends AbstractPerson {
    name: string;

    constructor(name: string, age?: number) {
        super(age);
        this.name = name;
    }

    // 없으면 에러발생
    printMe(): void {
        throw new Error("Method not implemented.");
    }
}

let p1 = new Person("kouzie", 20);
p1.sayHi(); // hi i'm 20
```


## 타입단언(형변환)

`as` , `<...>` 키워드를 통해 **타입단언**할 수 있음   

```ts
interface INameable { name: string };
let obj: object = { name: 'Jack' };

let name1 = (obj as INameable).name;
let name2 = (<INameable>obj).name;
console.log(name1, name2) // Jack Jack
```

## 배열  

TS 의 배열정의를 보면 아래와 같이 Generic Type 을 기반으로 구성되어있다.  

```ts
interface Array<T> {
    /**
     * Gets or sets the length of the array. This is a number one higher than the highest index in the array.
     */
    length: number;
    /**
     * Returns a string representation of an array.
     */
    toString(): string;
    ...
```

다양한 요소타입에 대응가능하도록 좋은 연동함수를 제공한다.  
또한 TS 에선 자동으로 배열의 요소 타입을 추론하기 때문에 아래 각 배열들의 속성은 서로 같다.  

```js
let arr1: number[] = [1,2,3];
let arr2 = [1, 2, 3]; // [ 1, 2, 3 ]

let arr3: any[] = [1, 2, 3];
let arr4 = [1, '2', 3];
```


### 튜플  

TS 에서 튜플은 고정된 타입의 값의 나열이다.  

```ts
let tp: [number, string, boolean] = [1, '2', false]
console.log(tp[0]) // 1
console.log(tp[1]) // 2
console.log(tp[2]) // false
let [n, s, b] = tp; // 디스트럭처링
```

한번 정의되면 더이상의 추가와 삭제는 불가능하다.  

`type` 키워드로 별칭을 만들어 자주 사용한다.  

```ts
type nsbForTuple = [number, string, boolean]
let tp: nsbForTuple = [1, '2', false]
```

## 대수 데이터 타입

합집합 대수 타입  

```ts
type numOrStr = number | string
let ns_n: numOrStr = 1;
let ns_s: numOrStr = "1";

console.log(ns_n) // 1
console.log(ns_s) // 1
```

교집합 대수 타입  

```ts
type INameable = { name: string }
type IAgeable = { age: number }

let obj1: INameable = { name: 'Jack' };
let obj2: IAgeable = { age: 32 };
let obj3: INameable & IAgeable = { name: 'Jack', age: 32 };
```