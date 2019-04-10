---
title:  "Web - JavaScript 3일차!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - HTML

tags:
  - web
  - html

toc: true

---

## JavaScript 배열


Js에서 배열은 Object의 일종으로 `var 배열명 = [];` 이런식으로 선언한다.  
초기값을 주고 싶다면 `var m = [1, 2, 3];`  

```js
var m1 = [];
var m1 = [1, 2, 3];
var m2 = new Array();
var m2 = new Array(1, 2, 3);
```

4개 모두 똑같은 배열 선언, 초기화 방법.  
통산 `var m1 = [];`형식을 사용한다.  

인덱스는 0부터 시작하며 각 배열 인덱스에 접근하고 싶다면 기타 언어와 같이 `[]` 인덱스 연산자를 사용하면 된다.  

```js
console.log(m2[0]);
console.log(m2[1]);
console.log(m2[2]);
```

다른 언어와 다르게 JavaScript 에서 배열은 **초기 길이를 정해줄 필요 없다.**  
```js
var m1 = [];
for (var index = 0; index < 10; index++) {
    m1[index] = index;
}
for (var index = 0; index < 10; index++) {
    console.log(m1[index]);
}
```

가장 마지막 인덱스를 보고 싶다면 `m1[m1.length-1]`, `length`속성을 사용하자.  


### split

문자열을 identifies(식별자)로 자르는 함수, 식별자로 문자, 정규식을 사용한다.  
반환값은 문자열 배열.


### join

배열을 합쳐서 하나의 문자열로 반환하는 메서드, `li`같은 html태그를 배열 양끝에 붙여 출력할 수 있다.
```js
document.getElementById("demo").innerHTML = 
  "<li>" + arr.join('</li><li>') + "</li>";
```

### toString

이름에서 예측가능, 배열요소에 `,`를 붙여 문자열로 반환하는 함수.  
```js
var points = [3,5,2,4,1];
console.log(points.toString());
```
`3,5,2,4,1`가 출력된다.  


### sort

> http://dudmy.net/javascript/2015/11/16/javascript-sort/

JavsScript에도 정렬을 위한 함수가 내장되어있다.  

단 정렬방법이 ASCII코드순, 즉 사전순이기 때문에 `1, 2, 11` 이 정렬되면 `1, 11, 2`로 출력된다.  
정렬 방법을 지정해 줘야 한다.(java에서 객체 배열 정렬할때 정렬을 위한 `Comparator`인터페이스 구현하듯이)  

```js
var arr = [3, 5, 2, 4, 1];
arr.sort(function (a, b) {
    return a-b;
});
```
0, 음수, 양수값을 통해 정렬기준을 정함, `return a-b`는 내림차순, `reutrn b-a`는 오름차순이다.  

나중에 객체배열을 사용할 때에도 정렬기준을 지정하면 객체끼리 정렬 가능하다.  

> 랜덤하게 정렬하기  
만약 기존 배열을 정렬되지 않고 랜덤하게 위치하도록 하려면 정렬`function`과 `Math.random()`내장 함수를 사용하면 된다.  
```js
var m = [1, 2, 3, 4, 5];
console.log(m.sort(function () {
    return 0.5 - Math.random();
}).toString())
```



### reverse

배열 역순으로 된 배열을 반환하는 메서드  

```js
var m = [3, 5, 2, 4, 1];
console.log(m.sort(function (a, b) {return a-b;})
            .reverse().toString());
```
오름차순 정렬 `function`한 뒤 `reverse`를 통해 내림차순 할 수 있다.  



### push, pop

`push`, `pop`은 스택 개념처럼 맨 마지막 요소 위치에 값을 집어넣고 빼내온다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(points.pop()); //1
console.log(points.toString()); //3,5,2,4
console.log(points.push(1)); //5, 배열의 길이 반환
console.log(points.toString()); //3,5,2,4,1
```
`pop`은 빼낸 배열 요소값을 반환한다.  
`push`는 매개변수로 요소를 집어넣고 배열의 길이를 반환한다.  

### shift, unshift

`push`, `pop`이 배열 마지막 index에 추가, 삭제한다면  
`shift`, `unshift`는 배열 첫 index에 추가, 삭제한다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(points.shift()); // 첫번째 요소를 제거하고 반환, 왼쪽으로 한칸씩 땡겨진다.  
console.log(points.toString()); // 5,2,4,1
console.log(points.unshift(8)); // 5, index 0에 값을 추가하고 배열 길이 반환.
console.log(points.toString()); // 8,5,2,4,1
```
`shift` 메서드는 배열 첫 index 요소를 **제거**하고 값을 반환한다.  
`unshift` 메서드는 매개변수로 첫 index 요소에 값을 **추가**하고 배열 길이를 반환한다.  

### delete

중간 위치에 있는 배열값을 삭제하고 싶다면 `delete`사용.  
단 공간은 삭제하지 않고 값만 삭제한다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(delete points[2]); //true
console.log(points.toString()); //3,5,,4,1
console.log(points.length); //5
console.log(points[2]); //undefined
points[2] = 2;
console.log(points.toString()); //3,5,2,4,1
```
`delete`의 반환값은 `true`, `false`, 성공했는지 실패했는지 결과값을 반환한다.  
삭제된 후 배열을 출력해보면 `3,5,,4,1` 컴마 사이에 값이 없다. 값은 없어도 길이(`length`)는 5를 출력한다.  
빈 공간은 `undefined`표시된다.  

### splice

중간 위치의 index 요소를 삭제하고 추가하기 위한 메서드, `delete`와 다르게 공간까지 삭제된다.  

```js
var points = [3, 5, 2, 4, 1];
console.log(points.splice(2, 1).toString()); //첫번째 파라미터는 index, 두번째 파라미터는 길이, 반환값은 자른 배열이 반환됨. [2]
console.log(points.toString()); //3,5,4,1

console.log(points.splice(2, 0, 100, 200)); //두번째 파라미터를 0주면 자르는게 아닌 추가, 뒤의 파라미터값이 순차적으로 2번째 index부터 추가됨.
console.log(points.toString()); //3,5,100,200,4,1
```
`splice`의 첫번째 매개변수는 `start index`, 두번째 매개변수는 삭제시엔 `end index`, 추가시엔 `0`이다.  

반환값은 **배열값**을 반환.
추가시엔 반환값은 없음.


### concat

여러 배열을 합칠 때 `concat` 메서드를 사용하면 좋다.  
합친 배열이 반환될뿐 **원본이 바뀌진 않는다**.  
```js
var arr1 = [1, 2, 3];
var arr2 = [10, 20, 30];
var arr3 = [100, 200, 300];
console.log(arr1.concat(arr2, arr3).toString()); 
// 1,2,3,10,20,30,100,200,300
```
`String`의 `concat`처럼 배열의 `concat`역시 가변인자이다.

### slice

원본 배열을 건들지 않고 `start index`부터 `end index`까지 잘라 반환하는 메서드.  

```js
var points = [1, 2, 3, 4, 5];
console.log(points.toString());
console.log(points.slice(2).toString());
console.log(points.toString()); 
console.log(points.slice(2, 4).toString());
```

매개변수가 하나일 때는 `start index`부터 끝까지 잘라 반환한다.  
`end index`는 포함하지 않고 바로 전 index까지 잘라 반환한다.  

### Arrays.isArray

```js
var points = [];
console.log(Array.isArray(points)); //true
```

`Arrays.isArray()` 내장 메서드를 통해 변수가 배열인지 확인할 수 있다.  

> 참고로 Java의 instanceof같은 연산자도 있음. `alert(points instanceof Array)`


### Math.max.apply

배열중 가장 큰값이나 작은 값을 가져오고 싶다면 `Math.max.apply()`, `Math.min.apply()` 내장 메서드를 사용하면 된다.  

```js
var m = [1, 2, 3, 4, 5];
console.log(Math.max(1, 5, 2, 3, 4)); //5
console.log(Math.min(1, 5, 2, 3, 4)); //5
console.log(Math.max.apply(null, m)); //5
console.log(Math.min.apply(null, m)); //1
```
비교 함수를 `apply()` 메서드의 첫번째 매개변수로 넣을 수 있다. 문자열, 숫자는 알아서 가장 큰 값을 가져오지만 객체같은 경우 비교함수를 지정해야 큰지 작은지 비교 가능하다.  


### forEach, filter, map

`forEach, filter, map` 세 함수를 사용하면 반복문을 사용하지 않아도 반복문 처리를 할 수 있는 직관적인 코드 작성이 가능하다.  

> https://bblog.tistory.com/300

`arr.forEach/filter/map(function (value, index, array){ });`
함수에 전달할 무명 메서드의 매개변수는 3개 들어갈 수 있고 `value`는 항목값, `index`는 위치값, `array`는 배열 자체를 뜻한다.  
(매개면수명은 자유롭게 지정 가능하다)  

꼭 3개 다 쓸 필요 없으며 필요한 값만 사용하면 된다.   

### forEach

`forEach`는 가장 기본적인 배열 요소를 순회하는 메서드이다.  

```js
var output = "";
var hap = 0;
var m = [45, 3, 10, 18, 9];
m.forEach(function (value) {
    output += value + "+";
    hap += value;
});
console.log(output); // 45+3+10+18+9+
console.log(hap); // 85
```

배열을 순회하며 요소의 값을 `value`에 전달해주고 연산 가능하다.

> 굳이 index값과 접근중인 array에 접근할 필요가 없다면 위처럼 매개변수 하나만 사용하면 된다.

### filter

이름 그대로 조건에 부합하지 않는 요소들을 걸러내는 메서드

```js
var m = [];
for (var i = 0; i < 10; i++) {
    m.push(Math.round(Math.random() * 100));
}

var n = m.filter(function (value, index, array) {
    return value > 30;
});
console.log(m.toString());
console.log(n.toString());
```
배열 m에 0~100까지의 랜덤한 값을 10개 집어넣고 `filter`메서드를 사용해서 30이상의 값을 걸러 내어 배열값으로 반환한다.  

출력값
```
6,1,70,48,48,97,53,16,32,10
70,48,48,97,53,32
```

만약 부합하는 배열이 없다면 빈 배열 `[]`을 반환한다.  

짝수에 해당하는 요소를 `filter`하고 싶다면 다음과 같이 작성
```
var n = m.filter(function (value, index, array) {
    return value % 2 == 0;
});
```


### map

배열의 각 요소에 일괄적인 처리를 한 배열을 만들고 싶다면 `map` 메서드를 사용.  

```js
var m = [45, 3, 10, 18, 9];
var n = m.map(function (value, index, array) {
    return value * 3; //각요소에 3을 곱해서 반환
});
console.log(n.toString()); //135,9,30,54,27
```
value(각 요소)에 3을 곱한 배열을 반환한다.  

<br><br>

## arguments

자바스크립트에선 함수호출시 받을 매개변수를 지정하지 않아도 된다.  
`arguments`객체(컬렉션)에 함수 호출 때 사용한 매개변수가 담겨서 전달된다.  

```js
function sum(n, m) {
  console.log(arguments[2]); //40
  return n + m;
}
console.log(sum(10, 20, 40)); //30
```
sum 함수가 받는 매개변수는 2개고 sum 호출 시 전달한 매개변수는 3개 인데 오류는 커녕 정상 작동한다.  

심지어 `arguments`를 배열처럼 사용하면 매개변수로 받지 않은 `40`까지 사용 가능하다.  


```js
function sumAll() {
    console.log(arguments.length);
    var hap = 0;
    for (var i = 0; i < arguments.length; i++) {
        hap += arguments[i];
    }
    return hap;
}
console.log(sumAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)); //55
```

매개변수 개수가 정확히 정할 수 없을 경우 `arguments.length` 속성을 사용해서 동적으로 처리 가능하다.  


## select, option

```html
<select id="bgcolor" onchange="bgcolor_onchange();">
    <option value="black">black</option>
</select>
```

위와 같은 콤보박스 `select` html태그를 JavaScript에서 다루기 위해 `select`객체(상위)와 `option`객체(하위)를 사용한다.  

>`onchange` 이벤트는 option값을 변경시 발생

### Option

select태그에 JavaScript를 사용해 동적으로 옵션을 **추가**하고 싶거나 **삭제**하고 싶을때 `Option`개체를 사용한다.  

`new Option([text[,value[,defaultSelected[,selected]]]])`
`Option`객체 생성자에 여러가지 파라미터가 들어갈 수 있다.

초기값으로의 선택은 인수 `defaultSelected`값을 true로 , 복수 선택을 위해선 `selected`값을 true로 설정한다.(복수 선택하기 위해선 select 태그가 다음과 같이 설정되어 있어야함 `<select multiple='multiple'>`)   



```js
var bgcolor = document.getElementById("bgcolor");
var optionObj = new Option("red", "red");
bgcolor.options[1] = optionObj;
```

![js4]({{ "/assets/web/js/js4.png" | absolute_url }}){: .shadow}  

`bgcolor`의 두번째 `option`으로 red를 추가.  
만약 삭제하고 싶다면 해당 `options[i]`를 `null`로 설정하면 된다.  

`bgcolor.options[1] = null;`  

```js
var bgcolor = document.getElementById("bgcolor");
var optionlist = ["black", "red", "green", "blue"];
for (var i = 0; i < optionlist.length; i++) {
    bgcolor.options[i] = new Option(optionlist[i], optionlist[i]);
}
```
배열과 for문을 통해 한꺼번에 추가 가능하다.

```js
var idx = bgcolor.selectedIndex;
var value = bgcolor.options[idx].value;
```
선택한 option의 값을 가져오고 싶다면 `select`객체의 `selectedIndex`속성으로 선택한 index값을 가져온 후 배열 첨자값으로 사용하면 된다.  


## Date

JavaScript의 날짜 객체, 각종 시간의 `get..`메서드로 가져올 수 있고 `set...`메서드로 설정 가능하다. 

### Date 객체 생성

매개변수 모두 생략할 시 현재 시간 객체를 생성한다.   
월은 `0 ~ 11` 값을 가진다.  

```js
var d1 = new Date();

var d2 = new Date(2018, 11, 21);
console.log(d2); //Fri Dec 21 2018 00:00:00 GMT+0900 (한국 표준시)
//시, 분, 초는 생략시 0으로 초기화

var d3 = new Date(2018, 11);
console.log(d3); //Sat Dec 01 2018 00:00:00 GMT+0900 (한국 표준시)
//일 생략시 1일로 초기화

var d4 = new Date(2018);
console.log(d4); //Thu Jan 01 1970 09:00:02 GMT+0900 (한국 표준시)
//밀리세컨드와 년도를 구분못함.

d4 = new Date("2019"); //Tue Jan 01 2018 09:00:00 GMT+0900 (한국 표준시)
console.log(d4);
//문자열은 구분함.
```

년도 외에 모든 시간을 생략(기본값으로 초기화)하고 싶다면 정수가 아닌 문자열로 년도을 집어넣어야 한다.  
그냥 년도는 밀리세컨드와 구분 못하기 때문.

```js
var d5 = new Date(99, 11, 21);
console.log(d5); //Tue Dec 21 1999 00:00:00 GMT+0900 (한국 표준시)
var d6 = new Date(19, 11, 21);
console.log(d6); //Sun Dec 21 1919 00:00:00 GMT+0900 (한국 표준시)
```
년도에 2글자 적용시 이전세기(100년전)로 적용된다. `99`, `19` 모두 `1900`년도로 설정됨.


문자열을 사용해서 Date객체 생성또한 가능하다.  
`ISO Date "2015-03-05"`  
`Short Date "03/05/2015"`  
`Long Date "May 05 2015"`  


위 3개 날짜 포멧에 해당하는 문자열로 `Date`객체 생성이 가능하다.  
```js

console.log(new Date("2015-03-05"));
console.log(new Date("03/05/2015"));
console.log(new Date("May 05 2015"));
//Thu Mar 05 2015 00:00:00 GMT+0900 (한국 표준시)
```


특수 문자가 구분자 역할을 하는데 왠만한 구분자는 다 인식해서 `Date`객체를 생성해준다.
```js
var d7 = new Date("2019-12-31");
console.log(d7); //Tue Dec 31 2019 09:00:00 GMT+0900 (한국 표준시)
var d8 = new Date("2019 12 31");
console.log(d8); //Tue Dec 31 2019 09:00:00 GMT+0900 (한국 표준시)
var d9 = new Date("2019#12#31");
console.log(d9); //Tue Dec 31 2019 09:00:00 GMT+0900 (한국 표준시)
```


### Date 객체 출력

`console.log(new Date());`  
위와 같이 Date객체 자체를 출력문에 넣어도 `toString()`함수를 사용한 것과 같다.  

`toString()` 외에도 여러가지 포멧으로 날짜 출력이 가능하다.  
```js
console.log(d1.toString()); //Wed Apr 10 2019 15:35:57 GMT+0900 (한국 표준시)
console.log(d1.toDateString()); //Wed Apr 10 2019
console.log(d1.toLocaleString()); //2019. 4. 10. 오후 3:34:51
console.log(d1.toLocaleDateString()); //2019. 4. 10.
console.log(d1.toLocaleTimeString()); //오후 3:34:51
```

출력 형식이 마음에 들지 않는다면 다음과 같이 `get...`메서드를 사용해서 문자열로 조합하면 된다.   
```
var demo = document.getElementById("demo");
var d = new Date();
var year = d.getFullYear();
var month = d.getMonth() + 1;
var days = d.getDate();
var week = d.getDay();
var dayofWeeks = ["일", "월", "화", "수", "목", "금", "토"]

console.log(year + "년 " + month + "월 " + days + "일(" + dayofWeeks[week] +")");
```
`2019년 4월 10일(수)` <- 출력된다.


### Date 객체 밀리세컨드 출력

여러가지 방식으로 밀리세컨드를 출력 가능하다.  
`Date.parse()` 내장객체   
`객체명.gettime()`  
`Number(new Date())`  

```js
var d = new Date();
console.log(Date.parse(d)); //1554878559000
console.log(d.gettime()); //1554878559000
console.log(Number(new Date())); //1554878559152
console.log(new Date(1554878559000)); //Wed Apr 10 2019 15:42:39 GMT+0900 (한국 표준시)
```

날짜끼리 연산시 밀리세컨드가 쓰임으로 중요하다.


### Date 객체 `set...`

기존 Date객체의 시간값을 `set...` 메서드를 통해 변경할 수 있다.
```js
var d = new Date();
console.log(d.toLocaleString()); //2019. 4. 10. 오후 4:13:24
d.setFullYear(2000);
d.setMonth(5-1);
d.setDate(21);
console.log(d.toLocaleString()); // 2000. 5. 21. 오후 4:13:54

d.setDate(41);
console.log(d.toLocaleString()); // 2000. 6. 10. 오후 4:14:12

d.setTime(1554878559152);
console.log(d.toLocaleString()); //2019. 4. 10. 오후 3:42:39
```

매개변수의 값이 date, month 가 지정한 범위를 넘으면 자동으로 다음, 이전달로 계산되어진다.  
`setTime`메서드를 사용해 밀리세컨드로 날짜를 통째로 변경 가능.  


### Date 객체간 연산

JavaScript에는 `java.util.date`에서 제공하는 `add`와 같은 메서드가 없다.  

대신 `set...`과 `get...`을 사용해 날짜끼리 빼거나 더할 수 있다.  

```js
var d = new Date(2016, 2, 1);
d.setDate(d.getDate()-1);
console.log(d.getDate()); //29
```

