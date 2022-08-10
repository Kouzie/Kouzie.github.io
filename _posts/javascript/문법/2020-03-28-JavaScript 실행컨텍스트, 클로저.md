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

## 실행 컨텍스트

JS 의 실행환경을 책임지는 `실행 컨텍스트`에 대해 알아본다.  

JS 에서 코드는 아래 4가지로 나뉘며 각 코드환경에 따라 `실행 컨텍스트`가 존재한다.  

모든 코드는 **평가**와 **실행** 단계로 나뉜다.  

- 전역코드(global code) - `전역실행 컨텍스트`  
- 함수코드(function code) - `함수실행 컨텍스트`  
- eval코드(eval code) - `eval실행 컨텍스트`  
- 모듈코드(module code) - `모듈실행 컨텍스트`  

각 코드는 평가단계에서 `실행 컨텍스트`가 생성되고  
생성된 `실행 컨텍스트`에 따라 코드가 실행된다.  

### 실행 컨텍스트 스택  

`실행 컨텍스트` 는 메모리상 스택 구조로 존재한다.  

코드실행전 실행컨텍스트를 **실행 컨텍스트 스택**에 `push` 하고 코드실행이 종료되면 `pop` 한다.  

```js
const x = 1;
function foo() {
    function bar() { 
        console.log("hello world");
    }
    bar();
}
foo();
```

`전역실행 컨텍스트`와 `foo()`, `bar()` 에 대한 `함수실행 컨텍스트` 가 스택에 쌓이는 순서  

![1](/assets/javascript/image6.png)  

### 실행 컨텍스트 구성  

각 **`실행 컨텍스트`는 `lexical env` 로부터 만들어진다(둘이 같은거라고 봐도 무방함)**  
`lexical env` 에는 `scope` 별 식별자들이 저장되어있으며 2개로 구분할 수 있다.   

![1](/assets/javascript/image7.png)  

**Environment Record(환경 레코드)** 
  스코프에 포함된 식별자를 등록, 관리하는 저장소다

**Outer Lexical Environment Reference(외부 렉시컬 환경에 대한 참조)** 
  외부 렉시컬 환경에 대한 참조는 상위 스코프(상위 외부 렉시컬 환경)를 가리킨다.
  스코프체인의 구현이 이루어진다.  


`전역실행 컨텍스트`라 할 수 있는 `global lexical env` 의 구성은 아래와 같다.  

```js
var x = 1;
const y = 2;

function foo(a) {
  console.log("hello")
}
```

![1](/assets/javascript/image8.png)  

전역객체(`BindingObject`)는 `전역실행 컨텍스트` 생성 이전에 만들어지고  
`Global Environment Record` 생성후 `this binding` 을 통해 매칭된다.  

> ES6 이전에는 전역객체가 `Global Environment Record` 역할을 수행했지만  
> ES6 이후 let, const 키워드로 선언한 전역변수는 별도의 저장공간에 존재한다.  

`Global Environment Record` 는 전역객체 바인딩 외에 아래 2가지로 나뉜다.  

- `Object Environment Record`(객체 환경 레코드)  
  `var` 전역변수, 전역함수, `built-in` 객체 식별자 저장, `BindObject` 를 통해 전역객체의 property 로 등록됨  
- `Declarative Environment Record`(선언적 환경 레코드)  
  `let`, `const` 전역변수, `class` 식별자 등록, 등록된 식별자는 코드 실행전에 사전등록되어 초기화 전까진 오류가 발생된다.    


`전역실행 컨텍스트` 생성이 끝나면 코드가 순차적으로 실행된다.  
등록된 전역변수에 값을 할당하고 함수호출 등의 코드를 실행한다.  

> `hoisting` 이 가능한 이유가 실행전인 `전역실행 컨텍스트` 과정에서 `var` 전역변수가 이미 등록되었기 때문  
> 이런 특징때문에 JS 를 `lexical scope` 언어라 부름


`함수실행 컨텍스트` 또한 마찬가지 비슷한 기능을 `lexical env` 에서 구현하지만 구성이 약간 다르다.  

함수 실행전 `local scope` 를 관리할 `함수실행 컨텍스트`를 만들고 `Function Environment Record` 에 지역변수들을 등록한다.  
(`arguments`, `this binding`, `스코프체인` 등을 추가로 등록함)  

> `outer lexical env reference` 는 `[[Environment]]` 내부슬롯을 통해 참조하며 스코프체인 역할을 수행한다.  

![1](/assets/javascript/image9.png)  

`블록실행 컨텍스트` 역시 다른 `실행 컨텍스트`와 비슷하게 `Environment Record` 가 존재하고 등록된 블록변수들이 관리된다.  

보든 `실행 컨텍스트들`은 더이상 참조자가 없어지면 가비지 콜렉터에 의해 사라진다.  


## 클로저

**`함수` + `lexical env` 의 조합**  

**지역변수를 만들고 함수가 끝나더라도 생명주기 연장** 을 위해 별도의 **중첩함수**를 만들어 반환값으로 사용, 반환값으로 사용한 **중첩함수를 클로저**라 한다.  

```js
function getClosure() {
  const text = 'variable 1';
  return function closure() { //반환되는 함수 - 클로저
    return text;
  };
}
const closure = getClosure();
console.log(closure()); // 'variable 1'
```

클로저에의해 참조되는 외부변수를 `자유변수(free variable)` 라 하며  
자유변수에 묶여(닫혀: Closed) 있는 함수라 하여 클로저라 부른다.  

JS 가비지 컬렉터 특성상 참조변수가 있는 메모리 공간을 해재하지 않기에  
`text` 변수의 부모격인 `closure()`,  
`closure()` 의 부모격인 `getClosure()` 의 `lexical env` 는 사라지지 않는다.  

두 함수의 `함수실행 컨텍스트`는 스택에서 사라지지만 `lexical env` 는 `text` 변수의 참조가 끊기지 않는이상 유지된다.  

> JS 가비지 컬렉터의 최적화로 자유변수 외의 외부변수들은 메모리 해제된다.  

클로저를 통해 객체지향적인 설계가 가능하다
(코드 간략, 재활용, 은닉화)  

### 클로저를 변수 은닉화 - 모듈 패턴

카운트를 위한 변수를 전역변수가 아닌 자유변수로 지정하여  
**클로저의 함수를 통해서만 접근**할 수 있도록 설정  

```js
var counter = (function () {
    let privateCounter = 0; // 자유변수

    return { //Object반환
        increment: function () {
            privateCounter++;
        },
        decrement: function () {
            privateCounter--;
        },
        value: function () {
            return privateCounter;
        }
    };
})();

console.log(counter.value()); // logs 0
counter.increment();
counter.increment();
console.log(counter.value()); // logs 2
```

이런 방식으로 클로저를 사용하는 것을 **모듈 패턴**이라 한다.

위에선 즉시실행함수로 객체를 반환시켰지만 가독성을 위해 함수리터럴 방식 사용을 권장한다.  

```js
const makeCounter = function() {
  var privateCounter = 0;
  
  return {
    increment: function() {
        privateCounter++;
    },
    decrement: function() {
      changeBy(-1);
    },
    value: function() {
      return privateCounter;
    }
  }  
}; // 함수리터럴로 정의
const counter = makeCounter();
console.log(counter.value()); // logs 0
```

객체들이 공유하는 static 한 변수를 사용하려면 생성자함수와 클로저를 엮어서 사용하면 된다.  
prototype 특성상 모든 객체들이 하나의 자유변수를 공유하게 된다.  

```js
const Counter = (function () {
    let privateCounter = 0; // 자유변수 + static
    function Counter() { } // 생성자 함수이자 클로저
    Counter.prototype.increment = function () {
        privateCounter++;
    };
    Counter.prototype.decrement = function () {
        privateCounter--;
    };
    Counter.prototype.value = function () {
        return privateCounter;
    };
    return Counter;
})();

const counter1 = new Counter();
counter1.increment();
const counter2 = new Counter();
console.log(counter1.value()); // logs 1
console.log(counter2.value()); // logs 1
```



### 예제 - Progress Bar

`div`태그 2개와 스타일 시트를 사용해 다음과 같은 Progress Bar 생성  

```css
<style>
  .myProgress {
    width: 100%;
    background-color: #ddd;
  }

  .myBar {
    width: 1%;
    height: 30px;
    background-color: #4caf50;
    text-align: right;
    color: red;
  }
</style>
```

```html
<h3>Progress Bar1</h3>
<div id="myProgress1" class="myProgress">
  <div id="myBar1" class="myBar"></div>
</div>
<button id="btn1">진행</button>

<h3>Progress Bar2</h3>
<div id="myProgress2" class="myProgress">
  <div id="myBar2" class="myBar"></div>
</div>
<button id="btn2">진행</button>

<h3>Progress Bar3</h3>
<div id="myProgress3" class="myProgress">
  <div id="myBar3" class="myBar"></div>
</div>
<button id="btn3">진행</button>
```

![js10](/assets/web/js/js10.png){: .shadow}{: .align-right}


`setTimeout`메서드와 클로저를 사용해 각각의 프그레스바가 각각의 변수를 가지고 증가되도록 설정해보자.  

각 버튼에 클로저 함수가 포함된, 각각의 width를 가질수 있는 이벤트 처리 함수를 등록,  
0.1초에 width가 1%씩 증가되며 width가 다 증가하면 `clearTimeout`를 호출해 재귀호출을 막는다.  

```js
var buttons = document.getElementsByTagName("button");
var myBars = document.getElementsByClassName("myBar");

for (let i = 0; i < buttons.length; i++) {
  buttons[i].onclick = progressBar(myBars[i]);
}

function progressBar(myBar) {
  var width = 1;
  var bar = myBar;
  return function innerfunc() {
    if (width >= 100)
      clearTimeout(timer);
    else {
      width++;
      bar.style.width = width + "%";
      bar.innerHTML = width + "%";
    }
    var timer = setTimeout(innerfunc, 30);
  }
}
```

![js11](/assets/web/js/js11.png){: .shadow}{: .align-right}
