---
title:  "JavaScript 문자열!"

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

## 템플릿 리터럴

ES6 에서 추가된 문법으로 백틱을 사용해 멀티라인, 표현식 등을 문자열에 삽입할 수 있다.  

```js
var template = 
`<ul> 
    <li><a href="#">Home</a></li> 
</ul>`;

var str1 = "hello"
console.log(`${str} world`)
console.log(`1+2=${1+2}`) // 1+2=3
```

## 문자열 함수  

### indexOf 메서드

못찾으면 -1, 찾으면 찾은 위치값을 돌리는 함수,  
`source.indexOf(search)`  
`source.indexOf(search, fromidx)`  

두가지 방식으로 사용되며 매개변수를 하나만 입력시 맨 처음 찾은 위치 값을 반환,  
두번째 매개변수 입력시 찾기 시작할 인덱스 위치를 지정할 수 있다.  

`lastIndexOf`함수도 사용할 수 있음(설명 생략)

### serach 메서드

정규표현식과 주어진 스트링간에 첫번째로 매치되는 것의 인덱스를 반환한다.
찾지 못하면 -1 를 반환한다.

```js
var msg = "hello world~~";
var pattern = "world";
var result = msg.search(pattern);
```

### match

정규식에 일치하는 문자열을 찾아 반환한다.  

문자열이 정규식과 일치하면, 일치하는 전체 문자열을 첫 번째 요소로 포함하는 `Array`를 반환, 없다면 `null`반환
```js
var test = 'love you. love me. love everything!'
var regExp = /love/gi;

result = test.match(regExp);
console.log(result); //Array(3)0: "love"1: "love"2: "love"length: 3__proto__: Array(0)
```


### replace

정규표현식을 사용해 문자열 중 패턴과 일치하는 문자열을 변경하고 반환한다. (원본은 바뀌지 않음)  

```js
var src = "hello world LLow~~";
var pattern = /llo/gi;
// * g : 발생할 모든 pattern에 대한 전역 검색
// * i : 대/소문자 구분 안함
// * m: 여러 줄 검색
console.log(src.replace(pattern, "xxx"));
console.log(src);
```

`replace`메서드에선 제일 앞의 일치하는 문자열만 바꾸기 때문에 일치하는 모든 문자열을 변경하고 싶다면 정규식 modify에 `g`옵션을 적용해야 한다.  
(JavaScript에는 `replaceAll`없음)

### substring, slice, substr

3개 메서드 모두 문자열을 `beginIndex`부터 `endIndex`까지 자르는 역할을 한다, `endIndex`는 생략 가능하며 생략시 마지막을 가리킴.

`slice` 메서드의 특징은 매개변수로 음수 입력이 가능하다.  
만약 음수라면, `beginIndex`는  `strLength(문자열 길이) + beginIndex`  
음수를 사용해 뒷자리부터 잘라올 수 있다.  

`substr` 메서드는 `beginIndex`와 가져올 문자 수를 지정한다.  
`str.substr(2, 2)` 2번째 index부터 2글자를 가져오도록 설정.  

### repeat

문자열을 입력한 정수만큼 반복한 문자열을 반환하는 함수  
`document.write("-".repeat(10));`  
하이폰이 10개 이어진 문자열을 반환해서 출력한다.  


### concate

```js
console.log("hello".concat("my ", "world"));
console.log("hello".concat("my ").concat("world"));
```

`+`연산자를 통해 문자열을 접합할 수 있지만 `concate`메서드를 사용해도 된다.  

뒤에 계속 `.`을 붙여가며 이어도 되지만 가변인자처럼 매개변수로 계속 나열해도 됨.  

### trim, toUpperCase, toLowerCase

설명 생략, 공백 지우고, 대문자로 변경하고 소문자로 변경하는 메서드.

### charAt, charCodeAt

매개변수로 문자 위치를 가리키는 정수형 index값이 들어감.  

`charAt`: 문자열의 특정 index에 해당하는 문자를 반환하는 함수  
`charCodeAt`: `charAt`과 같지만 반환하는 값이 문자의 ASCII코드값이다.  



```js
var data = "hello world~";
for (var i = 0; i < data.length; i++) {
  document.write(data.data[i]);
}
```
`ECMA Script 5`부터 문자열을 배열처럼 사용 가능하다, 굳이 `charAt`을 사용하지 않고 `[]`인덱스 연산자를 사용하면 된다.  

### split

문자열을 잘라 배열로 반환하는 함수  
```js
var str = event.srcElement.value;
var str_arr = str.split(/\s*,\s*/);
var demo = document.getElementById("demo");

for (var i = 0; i < str_arr.length; i++) {
  demo.innerHTML += "<li>[" + str_arr[i] + "]</li>";
}
```

`split` 매개변수로는 정규식이 사용됨

## 숫자 -> 문자

### `+`와 `""` 사용
```js
var msg = 10 + "";
var msg = new String(10)
```


## 문자 -> 숫자


### 1. Number() 전역 메서드

`console.log(Number(n1) + Number(n2));`

`window`객체가 가지고 있는 Number 메서드를 사용하여 문자열을 숫자형으로 변환

```js
console.log(Number(""));
console.log(Number(" "));
console.log(Number("10a"));
```

출력값
```
0
0
NaN
```

`Number` 메서드는 숫자 사이 혹은 뒤에 숫자가 아닌 다른 문자가 껴있으면 빡빡하게 검사해서 `NaN`으로 반환한다.


### 2. parseInt() 전역 메서드

10진수의 경우 `parseInt('n')` 이런 형식으로 사용하면 된다.  

`parseInt` 메서드는 `10a`같은 숫자형태가 아니더라도 `NaN`을 반환하지 않고 a를 제외한 10을 가져온다.(문자밖에 없다면 `NaN`반환함)

10진수외 다른 진법으로 문자형을 숫자형으로 바꾸고 싶다면 `parseInt('n', 16)` 이런식으로 쓰거나 `parseInt('0x100')` 으로 쓴다(16진수임을 알려주는 0x필요)  

문자형을 실수형으로 바꾸는 parseFloat도 있다.  
어느정도 공백이나 숫자 사이에 숫자가 아닌 문자가 들어있어도 어느정도 관용적으로 구분한다.  

> https://www.codingfactory.net/11026

예제
```js
function num2_enter() {
  var num1 = document.getElementById("num1").value;
  var num2 = document.getElementById("num2").value;

  if (event.keyCode == 13) {
    if(isNaN(num1))
    {
      alert("숫자를 입력하세요!");
      document.getElementById("num1").focus();
      return;
    }
    if(isNaN(num2))
    {
      alert("숫자를 입력하세요!");
      document.getElementById("num2").select(); /* select쓰면 모두 선택 */
      return;
    }
    document.getElementById("result").value = window.parseInt(num1, 10)+parseInt(num2, 10);
  }
}
```
>focus와 select의 차이는 드래그 되어있냐, 커서만 위치하느냐 차이  


