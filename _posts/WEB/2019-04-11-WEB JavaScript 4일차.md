---
title:  "Web - JavaScript 4일차 달력!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - HTML

tags:
  - web
  - html
  - javascript

toc: true

---


## 달력 만들기

combo박스에서 날짜를 선택하면 달력을 출력하도록 JavaScript코딩을 해보자.  

달력을 만드려면 년, 월, 시작요일, 마지막일이 필요하다.  

일단 `select`(콤보박스)에 동적으로 `option`들을 추가하자.  
(`for`문과 `Option`객체 사용)  

```html
<select name="" id="cmbyear"></select>
<select name="" id="cmbmonth" onchange="m_change();"></select>
<select name="" id="cmbday" onchange="d_change();"></select>
...
...

<script>
  var cmbyear = document.getElementById("cmbyear");
  var cmbmonth = document.getElementById("cmbmonth");
  
  for (var i = 1970; i <= 2050; i++) {
      cmbyear.options[i - 1970] = new Option(i + "년", i);
  }

  for (var i = 0; i < 12; i++) {
      cmbmonth.options[i] = new Option(i + 1 + "월", i);
  }
</script>
```
`index[0]`에 1970, `index[80]`에 2050 이 들어간다.

> 주의: options 시작 index는 0으로 설정하는 것을 주의하자.  

년은 1970 ~ 2050 까지, 월은 1 ~ 12월 까지 option을 추가한다.  

그리고 현재 년, 월을 선택 되도록 하기위해 `select`의 `options`객체의 `selected`속성을 사용하거나  
select
```js
cmbyear.options[now_year - 1970].selected = "seleted"
cmbmonth.options[now_month].selected = "seleted"
```
혹은
```js
cmbyear.selectedIndex = year - 1970;
cmbmonth.selectedIndex = month;
```

`options`객체의 `seleted`속성값을 주거나 `select`객체의 `selectedIndex`에 기본 선택 index를 줄 수 있다.  

달을 선택하면 동적으로 옆의 cmbday콤보박스에 마지막일 까지 선택`option`이 생기도록 설정해보자.  

일단 달을 선택하면 몇월 달인지에 따라 마지막일 을 가져와야 한다.  

### 마지막일을 가져오는 함수

```js
function getLastDay(year, month) {
  var lastDay = new Date(year, month + 1);
  lastDay.setDate(0);
  return lastDay.getDate();
}
```
3월달의 마지막일을 알고싶다면 XX년 4월 1일로 설정한 후 `setDate`에 인자로 0을 주면 된다.  
Date객체는 알아서 월간 이동하고 3월의 마지막일로 설정된다.  


달을 선택하는 `cmbmonth`콤보박스에서 `onchange`이벤트가 발생할때 마다 선택한 달의 다음달 1일로 설정한 후 `setDate(0)`을 호출해서 마지막일을 가져오면 된다.  
```js
function m_change() {
  cmbday.options.length = 0;
  var year = parseInt(cmbyear.options[cmbyear.selectedIndex].value);
  var month = parseInt(cmbmonth.options[cmbmonth.selectedIndex].value);
  //날짜간 덧셈, 뺄샘 과정이 일어날 수 있음으로 number로 형변환
  var lastDay = getLastDay(year, month);
  for (var i = 0; i <= lastDay - 1; i++) {
      cmbday.options[i] = new Option(i + 1 + "일", i + 1);
  }
  ...
  ...
}
```
`index[0]`에 1일, `index[30]`에 31일 이 들어가게됨.

주의할 점은 다시 달을 선택할때 `cmbday.options.length = 0;` 설정하는 것,  
2월의 마지막일은 28일인데 다른 달 로 인해 생긴 `index[28]` ~ `index[30]`이 지워지지 않아 2월 선택 시 표시될 수 있음.  

![js5-1]({{ "/assets/web/js/js5-1.png" | absolute_url }}){: .shadow}  

![js5]({{ "/assets/web/js/js5.png" | absolute_url }}){: .shadow}  

달력을 만들기 위해 필요한 시작요일은 쉽게 얻어올 수 있다.  
해당 달의 1일로 설정된 Date객체에서 `getDay()` 메서드를 호출하면 된다.  

```js
function getStartDay(year, month) {
  var date = new Date(year, month);
  return date.getDay();
}
```

`getDay()`는 일요일: 0 ~ 토요일: 6까지 반환한다.  

그럼 m_change메서드가 onchage 이벤트로 호출될때 마다 년, 월을 표시하고  
시작요일 만큼 빈 `<li>`태그 생성, 1일 ~ 마지막일 이 content로 저장된 `<li>`를 동적으로 생성해서 스타일 시트를 적용하면 된다.  


```html
<select name="" id="cmbyear"></select>
<select name="" id="cmbmonth" onchange="m_change();"></select>
<select name="" id="cmbday" onchange="d_change();"></select>


<div class="month">
    <ul>
        <li class="prev" onclick="changeCalendar(-1);">&#10094;</li>
        <li class="next" onclick="changeCalendar(1);">&#10095;</li>
        <li>
            <span id="month">
                1
            </span>
            <br>
            <span id="year" style="font-size:18px">2017</span>
        </li>
    </ul>
</div>
<ul class="weekdays">
    <li>Su</li>
    <li>Mo</li>
    <li>Tu</li>
    <li>We</li>
    <li>Th</li>
    <li>Fr</li>
    <li>Sa</li>
</ul>
<ul class="days">
    
</ul>
```
![js6]({{ "/assets/web/js/js6.png" | absolute_url }}){: .shadow}  


년, 월을 콤보박스에서 선택하면 

`<span id="year">`와 `<span id="month">`에 선택한 년/월로 content를 변경하고  

`<ul class="days">`안에 방금 말한 `li`태그들을 집어넣자

### 동적으로 날짜 li태그 생성


```js
var ul_days = document.getElementsByClassName("days")[0];
function printCalender(year, month) {
  ul_days.innerHTML = "";

  var lastDay = getLastDay(year, month);
  var startDay = getStartDay(year, month);
  spn_year.innerHTML = year;
  spn_month.innerHTML = month + 1;

  for (let i = 0; i < startDay; i++) {
      var li_date = document.createElement("li"); //li태그 생성
      ul_days.appendChild(li_date);
  }
  for (let i = 1; i <= lastDay; i++) {
      var li_date = document.createElement("li"); //li태그 생성
      var li_text = document.createTextNode(i);
      li_date.appendChild(li_text);
      ul_days.appendChild(li_date);
  }
}
```
다양한 방법이 있겠지만 `printCalender`함수는  
`document.createElement`와 `document.createTextNode`, `li_date.appendChild` **DOM 내장 객체**를 사용해서 `li`태그를 생성한다.  


![js7]({{ "/assets/web/js/js7.png" | absolute_url }}){: .shadow}  

`startDay`만큼 빈 `li`태그를 생성하고 `appendChild` 함수를 통해  `ul_days`에 집어넣고  

`lastDay`만큼 일자가 저장된 li태그를 생성, 저장한다.  
content는 li태그에 넣고 li태그는 ul태그에 집어넣는다.  

DOM객체에선 content마저 하나의 자식(`Node`)로 취급하기 때문에 태그안에 content를 넣을 때 `document.createTextNode`함수 통해 content를 만들 수 있다.  

`printCalender`함수에서도 주의사항은  `ul_days.innerHTML = "";` 기존 `ul`태그에 있던 내용을 초기화 해줘야 한다.  



### 다음달, 이전달 이동  

위에있는 `> <`기호를 누르면 다음달, 이전달로 이동하도록 JavaScript함수를 만들자.  

```js
<li class="prev" onclick="changeCalendar(-1);">&#10094;</li>
<li class="next" onclick="changeCalendar(1);">&#10095;</li>
```
```js
function changeCalendar(value) {
  var year = spn_year.innerHTML;
  var month = parseInt(spn_month.innerHTML) + value;

  printCalender(year, month - 1);
  if (month == 0) {
      month = 12; 
      year--;
  } else if (month == 13) {
      month = 1; 
      year++;
  }
  spn_year.innerHTML = year;
  spn_month.innerHTML = month;
}
```

만약 1월에서 전달로 이동한다면 month는 12로, year는 1 감소해야한다.  
12월은 그 반대로...



### 달력 스타일 시트  

달력 출력을 위한 데이터를 가져오는 일은 JavaScript로 끝났다.  
스타일시트를 적용해서 달력같은 달력은 말들면 된다.  

```css
* {
    box-sizing: border-box;
}

ul {
    list-style-type: none;
}

body {
    font-family: Verdana, sans-serif;
}

.month {
    padding: 70px 25px;
    width: 100%;
    background: #1abc9c;
    text-align: center;
}

.month ul {
    margin: 0;
    padding: 0;
}

.month ul li {
    color: white;
    font-size: 20px;
    text-transform: uppercase;
    letter-spacing: 3px;
}

.month .prev {
    float: left;
    padding-top: 10px;
}

.month .next {
    float: right;
    padding-top: 10px;
}

.weekdays {
    margin: 0;
    padding: 10px 0;
    background-color: #ddd;
}

.weekdays li {
    display: inline-block;
    width: 13.6%;
    color: #666;
    text-align: center;
}

.days {
    padding: 10px 0;
    background: #eee;
    margin: 0;
}

.days li {
    padding: 5px;
    list-style-type: none;
    display: inline-block;
    width: 13.6%;
    text-align: center;
    margin-bottom: 5px;
    font-size: 12px;
    color: #777;
}

.days li.active {
    padding: 5px;
    background: #1abc9c;
    color: white !important
}
```


![js8]({{ "/assets/web/js/js8.png" | absolute_url }}){: .shadow}  

### 날짜 선택시 표시

```js
var before_value = 0;
var cmbday = document.getElementById("cmbday");
function d_change() {
  var selectDay = cmbday.options[cmbday.selectedIndex].value;
  var year = cmbyear.options[cmbyear.selectedIndex].value;
  var month = parseInt(cmbmonth.options[cmbmonth.selectedIndex].value);
  var lastDay = getLastDay(year, month);
  var startDay = getStartDay(year, month);

  var num = Number(startDay) + Number(selectDay) - 1;
  ul_days.getElementsByTagName("li")[before_value].className = "";
  before_value = num;
  ul_days.getElementsByTagName("li")[num].className = "active";
}
```

선택한 날짜는 `ul`태그 안의 `li`태그에 가서 `class명`을 `active`로 설정해주면 된다.  
`getElementsByTagName`을 통해 반환받은 배열에 `index`를 `startDay + selectDay`로 지정하면 선택한 날에 해당하는 `li`객체를 가져올 수 있다.  

다른날 선택시 그 전에 선택했던 li태그에서 active 클래스명을 제거해야 하기 때문에 `before_value` 백업용 변수를 만든다.  
~~(아직까진 힘들게 Javascript로 코딩하지만 JQuery쓰면 간단하겠지...?)~~


## 클래스명 부여, 제거

Element에 class명을 부여하고싶을때, 다중으로 class를 갖고있을 때 특정 클래스만 지우고 싶다면 어떻게 하는지 알아보자.  

class도 요소가 가지고 있는 수많은 속성중 하나일 뿐, 별 다를 것 없다.  
객체의 className 속성을 사용해 설정, 추가, 제거하면 된다.


일반적인 class명을 추가하는 방법은 다음과 같다.  
```js
dot.className = "active";
dot.className += " block";
```
만약 `active`외에 다른 class를 더 추가하고 싶다면 다음과 같이 `+=` 연산자를 사용하면 된다.
주의할점은 클래스 구분을 **공백**으로 하기 때문에 문자열 추가하듯이 앞에 공백을 붙여야 한다.  


특정 클래스명을 삭제하고 싶을땐 `replace`함수를 사용하면 된다.  
```js
dot.className = dot.className.replace("active", "").trim();
```

> 나중에 jquery를 배우면 제공하는 메서드로 간단히 처리 가능
```js
$(".dot").addClass("active");
$(".dot").removeClass("active");
```

> https://developer.mozilla.org/ko/docs/Web/API/Element/classList

`classList`를 사용하면 `classList`에서 제공하는 함수로 쉽게 클래스를 주입할 수 있다.  

### classList.toggle

> `classList`는 요소의 클래스 속성을 관리하는 `DOM Property`이다 (읽기전용이지만 add, remove등을 통해 변경 가능)  

버튼을 한번 누르면 class명을 부여하고,  
다시 누르면 부여했던 class명을 빼앗고 싶을때 사용하는 메서드가 `toggle()`이다.  


물론 `className`속성과 `replace()`메서드를 사용해서 기존에 해당 class명이 있다면 빼았고, 없다면 주는 방식을 사용해도 되지만   
똑딱이처럼 class명을 주었다 뺐는 경우가 많기 때문에 `classList`의 `toggle` 메서드를 사용하면 편하다.  


```js
var colls = document.getElementsByClassName("collapsible");
for (let i = 0; i < colls.length; i++) {
    colls[i].addEventListener("click", function () {
        if (this.classList.toggle("active"))
            this.nextElementSibling.style.maxHeight = "150px";
            //고정된 값이 싫다면 scrollheight값을 사용
            //this.nextElementSibling.style.maxHeight = this.nextElementSibling.scrollHeight + "px";
        else
            this.nextElementSibling.style.maxHeight = "0px";
    });
}
```


`collapsible`란 클래스명을 가진 객체배열(콜렉션)을 가져와서 for문과 `addEventListener`를 통해 각 객체 `click`이벤트 처리용 함수를 주입한다.  

자기자신의 `classList`의 `toggle`메서드를 호출하는데 `active`라는 클래스명이 기존 자기자신에게 없는 클래스명이라면 `true`를 반환하고 `active`클래스명을 적용한다.  
만약 `active`클래스명이 자기자신에게 이미 있는 class명이었다면 `false`를 반환하고 `active`클래스명을 빼았는다.  

> `nextElementSibling`은 자기자신 바로 다음 요소를 가져올 때 사용하는 `DOM Property`

![js9]({{ "/assets/web/js/js9.png" | absolute_url }}){: .shadow}

전체코드

```html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>docuemnt</title>

    <style>
        .container {
            width: 500px;
            border: 1px solid black;
        }

        .collapsible {
            background-color: #777;
            color: white;
            cursor: pointer;
            padding: 18px;
            width: 100%;
            border: none;
            text-align: left;
            outline: none;
            font-size: 15px;
        }

        .collapsible::after {
            content: '+';
            color: white;
            font-weight: bold;
            margin-left: 5px;

        }
        .active::after {
            content: '-';
        }
        .collapsible:hover,
        .active {
            background-color: #555;
        }

        .content {
            padding: 0px;
            max-height: 0;
            overflow-y: scroll;
            transition: max-height 0.2s ease-out;
            background-color: #f1f1f1;
        }

    </style>
</head>

<body>
    <div class="container">
        <button class="collapsible">collapsible1</button>
        <div class="content">Lorem ipsum dolor sit amet, consectetur adipisicing elit. Sed animi voluptatum aliquam
            ducimus
            quasi eveniet delectus ratione, nam ipsum numquam fugit quae eius at tempora consectetur architecto,
            sapiente
            <br><br></div>
        <button class="collapsible">collapsible2</button>
        <div class="content">Lorem ipsum, dolor sit amet consectetur adipisicing elit. Officia quam aspernatur
            praesentium
            earum dolorem magnam voluptates omnis numquam in, explicabo esse obcaecati aliquid tempore et neque. Velit
            recusandae distinctio expedita!Lorem ipsum, dolor sit amet consectetur adipisicing elit. Officia quam
            aspernatur
            <br><br></div>
        <button class="collapsible">collapsible3</button>
        <div class="content">Lorem ipsum, dolor sit amet consectetur adipisicing elit. Officia quam aspernatur
            praesentium
            earum dolorem magnam voluptates omnis numquam in, explicabo esse obcaecati aliquid tempore et neque. Velit
            recusandae distinctio expedita!Lorem ipsum, dolor sit amet consectetur adipisicing elit. Officia quam
            aspernatur
            praesentium
            earum dolorem magnam voluptates omnis numquam in, explicabo esse obcaecati aliquid tempore et neque. Velit
            recusandae distinctio expedita!praesentium
            earum dolorem magnam voluptates omnis numquam in, explicabo esse obcaecati aliquid tempore et neque. Velit
            recusandae distinctio expedita!Lorem ipsum, dolor sit amet consectetur adipisicing elit. Officia quam
            aspernatur
            praesentium
            <br><br></div>
    </div> <!-- container -->

    <script>
        var colls = document.getElementsByClassName("collapsible");
        for (let i = 0; i < colls.length; i++) {

            colls[i].addEventListener("click", function () {
                if (this.classList.toggle("active"))
                    this.nextElementSibling.style.maxHeight = this.nextElementSibling.scrollHeight + "px";
                else
                    this.nextElementSibling.style.maxHeight = "0px";
            });
        }
    </script>
</body>

</html>
```