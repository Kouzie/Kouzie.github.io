---
title:  "Web - JavaScript 8일차 - DOM, BOM, 쿠키!"

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

## HTML DOM 객체

문서 객체 모델(`Document Object Model`)은 객체 지향 모델로써 **구조화된 문서를 표현하는 형식**이다.  
DOM은 플랫폼언어 중립적으로 구조화된 문서를 표현하는 W3C의 공식 표준이다(위키).  

`html` 문서의 모든 내용은 노드로 이루어져 있다.  
태그, 태그안의 content, 태그의 속성, 속성값, 주석 등등 모두 노드로 구성함.

노드는 다음 4가지로 분류된다.  

* html 문서의 요소(태그)는  **요소 노드**라 한다.   

* html 문서의 요소안의 내용(content)는  **텍스트 노드**라 한다.   

* html 문서의 요소의 속성(attribute)은  **속성 노드**라 한다.  

* html 문서의 주석은 **주석 노드**라 한다.  

모든 내용을 노드로 구분하기 때문에 DOM객체를 사용해 HTML문서의 모든 내용을 구분할 수 있고 내용을 가져올수 있다.  

`document`객체 아래로 **트리형식으로 노드가 구성**되어 있으며 `DOM트리`라 한다.  

Javascript를 사용해 모든 노드를 *접근, 수정, 추가, 삭제* 가능하다.  
모든 트리관계가 그렇듯 DOM트리에서도 노드간 상위 하위 관계가 있다.    

> 참고 :최상위(루트노드)는 document가 아닌 **html이다**. document는 모든 노드에 접근 가능노드로 루트노드라 할 수 없음.  

`document`객체는 모든 노드에 접근 가능합으로 `document.documentElement`속성을 통해 `html노드`를 를 가져온다.  

<br>

### Element와 Node의 차이점

html문서는 노드로 구성된다 할 수 있지만 `Element`로 구성된다 할 수는 없다.  
`Element`는 `Node`보다 큰 범위의 개념으로 객체 그 자체를 나타내지만  
`Node`는 객체안의 속성, `content` 등을 나타낸다.  

또한 대부분의 DOM객체 메서드에서 `Element`와 `Node`의 배열형태인 `HTML Collection`과 `NodeList`를 반환한다, 모두 배열형태의 데이터 구조이다.  

`[]`인덱스 연산자로 특정 위치를 가리키는건 `Collection`과 `NodeList` 공통이지만  
`push(), pop(), join()`등의 메서드는 `NodeList`만 사용 가능하다.  

<br>


### 노드간, 요소간 이동

**노드간 이동**은 다음 메서드를 통해 할 수 있다.    
```js
parentNode
childNodes[n]
firstChild
lastChild
nextSibling
previousSibling
```
`Element`와 다르게 노드는 매우 작은 범위로 이동한다. 때문에 노드간 이동에서 예상과 다른 결과를 얻을 수 있는데  
다음 `table`태그안의 노드 개수를 확인해 보면 노드가 얼마나 작으 단위인지 알 수 있다.  
```html
<table border="1" id="table" >
  <tbody id="tbody">
  </tbody>
</table>
```
위 테이블의 `Node`는 `tbody` 하나뿐이라 생각할 수 있는데 실제로는 3개 Node가 있다.    

```
NodeList(3) [text, tbody#tbody, text]
0: text
1: tbody#tbody
2: text
length: 3
...
```
chrome 디버그 창에서 `console.log(document.getElementById("table").childNodes);` 출력한 결과이다.  

`tbody`를 둘러싸고 있는 `text Node`(0과 2번째 자식노드)는 **개행문자**이다.   
(태그안의 개행마저 `text Node`인식하기때문)  

따라서 `nextSibling`, `previousSibling`로 이동하다 보면 이런 공백으로 다른 값을 참조할 가능성이 많다.  


개행문자 때문에 생기는 자식노드를 없애려면 아래저첨 개행을 없애면 `text Node`가 생기지 않는다.  
```html
<table border="1" id="table" ><tbody id="tbody"></tbody></table>
```
<br>

노드 말고 `Element`간 이동을 할 수 있다면 위와 같은 상황을 방지할 수 있는데  
물론 요소간 이동할 수 있는 메서드도 DOM객체가 지원한다.  
```js
parentElement
children
firstElementChild
lastElementChild
nextElementSibling
previousElementSibling
```
사용하는 메서드의 반환값, 매개변수 타입이 `Node`인지 `Element`인지 필이 확인하자.  

`Node`접근할 일이 없을수 있다고 생각되지만 실제 특정 문자열 뒤에 태그삽입 등의 작업을 할 때 자식 `Node`를 가져와야 할 때가 있다.  


<br>

### 노드 관련 속성값

해당 노드가 어떤 타입의 노드인지 **`nodeType`** 속성값을 통해 알 수 있다.  

```html
<h1 id="id01">body first child</h1>
<p id="id02">body second child</p>
<script>
  console.log(document.getElementById("id01").firstChild.nodeType;)
</script>
```
`h1`태그의 `firstChild`(노드반환) 속성으로 얻은 노드의 `nodeType`을 출력하면 `3`이 출력된다.  
```
1   Node.ELEMENT_NODE         요소노드
2   Node.ATTRIBUTE_NODE       속성소느
3   Node.TEXT_NODE			      텍스트노드
8   Node.COMMENT_NODE		      주석노드
9   Node.DOCUMENT_NODE	      Documnet노드(문서노드)
10  Node.DOCUMENT_TYPE_NODE   <!DOCTYPE html>
```
> 참고자료: https://developer.mozilla.org/ko/docs/Web/API/Node/nodeType

참고로 속성노드를 얻고싶다면 `attributes`속성을 사용하자.  
`NodeMap`을 반환하는데 배열처럼 사용하면 된다.   

`console.log(document.getElementById("id01").attributes[0].nodeType)`  
`2`를 반환한다.  


노드의 종류를 숫자말고 문자열로 알고싶다면 **`nodeName`** 속성을 사용 
```
텍스트노드	#text
속성노드		속성명
요소노드		태그명
문서노드		#document
주석노드		#comment
```
`nodeName`은 읽기전용 속성으로 변경 불가능하다.  

`console.log(document.getElementById("id01").attributes[0].nodeName)`  
속성 이름인 `id`를 반환한다.  

> 참고자료: https://developer.mozilla.org/ko/docs/Web/API/Node/nodeName  

속성노드이건, text노드이건, 주석노드이건 해당 노드의 값을 가져오고 싶다면 **`nodeValue`**속성을 사용한다.  

`console.log(document.getElementById("id01").firstChild.nodeValue);`  
"body first child" 출력된다.

`Element`또한 하나의 `Node`이기 때문에 `Element`에서 `nodeValue`를 사용하면 안의 text노드를 반환할 것 같지만 `null`을 반환한다.  

`innerText`속성을 쓰도록 하자.  

<br>

### 노드 생성, 추가, 삭제

`document`객체의 `createElement`, `appendChild`, `removeChild` 메서드를 통해 생성, 추가, 삭제가 가능하다.  
```html
<div id="demo">
  <p id="p1">Lorem.</p>
  <p id="p2">ipsum</p>
</div>
```
위와 같은 `div`태그와 `p`태그가 있을때 `p2`를 삭제하고 `p3`를 생성 및 추가해보자.  

```js
var demo = document.getElementById("demo");
var p2 = document.getElementById("p2");
demo.removeChild(p2); // p2삭제

var newP = document.createElement("p");
newP.id="p3";
newP.appendChild(document.createTextNode("new created P tag"));

demo.appendChild(newP);
```

위 3개 함수외에 `replaceChild`, `insertBefore` 메서드가 있다.  

`appendChild`로 인해 추가된 노드는 맨 아래 노드(last child)로 추가되기 때문에 위의 두 함수를 사용하면 수월하게 중간에 노드를 추가할  수 있다.  

`replaceChild`는 기존 노드를 삭제하고 그자리에 새 노드를 추가한다.  
기존에 p1태그를 새로만든 p4태그로 변환하자.  
```js
var changeP = document.createElement("p");
changeP.id="p3";
changeP.appendChild(document.createTextNode("chage P tag"));

demo.replaceChild(changeP, document.getElementById("p1"))
```
첫번째 파라미터로 새로 넣을 노드, 두번째 파라미터로 변경될 노드를 넣는다.  

`insertBefore`는 특정 태그 앞에 특정태그를 위치하는 메서드이다.  

```js
var p3 = document.getElementById("p3");
var beforeP = document.createElement("p");
beforeP.id="p5";
beforeP.appendChild(document.createTextNode("insert before P tag"));

// insertBefore()
demo.insertBefore(beforeP, p3);
```
첫번째 파라미터로 삽입할 노드, 두번째 파라미터로 기준이 될 노드를 넣는다.  

> 주의할점은 Node단위로 삽입되기 때문에 기준노드의 before가 어디를 가리키는지 잘 확인해야함  

<br>

### DOM의 여러 속성들

DOM에는 위에서 설명했던 메서드와 속성 외에 여러 편리한 속성과 메서드들을 가지고 있다.  

```js
Element.style - 스타일 시트 설정
Element.setAttribute(attrubute, value) 
//요소의 속성에 해당하는 속성값 설정

document.images 
//html문서의 모든 img태그를 HTML Collection으로 반환
document.links
//html문서의 모든 a태그를 HTML Collection으로 반환

document.cookie
document.forms // 컬렉션 form태그들을 가지고 있는 집합
document.script // <script>태그 객체를 가져옴
document.documentElement // <html> 최상위 태그
document.body // <body>객체를 가져옴
document.head // <head>객체를 가져옴
document.URL //현재 html 문서의 url을 반환
document.referrer; // 링크를 통해 현재 페이지로 이동 시킨, 전 페이지의 URI 정보를 반환.
```

<br>

### documentElement 속성을 사용한 progress Bar 구현

스크롤을 내린만큼 progress bar가 진행되도록 css와 Js로 구현.  

```css
body {
  margin: 0;
  font-size: 28px;
}

.header {
  position: fixed;
  top: 0;
  z-index: 1;
  width: 100%;
  background: #f1f1f1;
}

.header h2 {
  text-align: center
}

.progress-container {
  width: 100%;
  height: 8px;
  background: #ccc;
}

.progress-bar {
  height: 8px;
  background: red;
  width: 0px;
}

.content {
  padding: 100px 0;
  margin: 50px auto 0 auto;
  width: 80%;
}

.header {
  padding: 10px 16px;
  background: #555;
  color: #f1f1f1;
}

#btnTop {
  display: none;
  position: fixed;
  bottom: 20px;
  right: 30px;
  z-index: 99;
  border: none;
  outline: none;
  background: red;
  color: white;
  padding: 15px;
  border-radius: 10px;
  cursor: pointer;
}

#btnTop:hover {
  background: #555;
}
```
```html
	<div class="header">
		<h2>스크롤 표시하기</h2>
		<div class="progress-container">
			<div class="progress-bar" id="myBar"></div>
		</div>
	</div>
	<div>
    <!-- p*50>lorem10 -->
		<p>Lorem ipsum dolor sit amet, consectetur adipisicing.</p>
    ...
    ...
    (p태그 스크롤 생길만큼 여러개 생성)
		<p>Nemo vero aspernatur quisquam sint sunt aperiam?</p>
	</div>
	<button id="btnTop" title="goToTop" onclick="onclick_btnTop()">Top</button>
```
```js
  window.onscroll = function () {
    var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;

    var scrollHeight = document.documentElement.scrollHeight;
    var clientHeight = document.documentElement.clientHeight;
    console.log(scrollHeight + " / " + clientHeight);

    var hiddenHeight = scrollHeight - clientHeight;
    var widthPercent = (scrollTop / hiddenHeight) * 100;
    document.getElementById("myBar").style.width = widthPercent + "%";


    if (scrollTop >= clientHeight) {
      document.getElementById("btnTop").style.display = "block";
    }
    else {
      document.getElementById("btnTop").style.display = "none";
    }
  }
  function onclick_btnTop() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
  }
```

![js13]({{ "/assets/web/js/js13.png" | absolute_url }}){: .shadow}

`scrollTop`: 스크롤 내린길이, 스크롤 위 내용의 높이값, 스크롤을 맨 위로 올리면 0.  
`scrollHeight`: **문서 전체**의 길이값.  
`clientHeight`: 클라이언트에게 뿌릴수 있는 내용의 길이값.  

`scrollTop`(내린길이)/`scrollHeight`(전체길이) 를 퍼센트로 사용해 Progress Bar를 진행시킨다.

<br><br>

## BOM (Browser Object Model)

DOM이 W3C가 표준으로 정한 HTML문서를 읽고 만들어지는 객체라면
BOM은 **브라우저에서 만들어지는 객체**이다. 브라우저의 각종 정보를 갖고 있다.  

브라우저 개발 회사마다 약간씩 다르기 때문에 크로스 브라우징 환경을 갖추려면 각 브라우저별로 제공하는 BOM객체의 다른점을 알아햐 한다.  
(표준이 있기 때문에 거의 비슷함) 

BOM으로 만들어지는 객체는 다음과 같다.
1. window   
2. screen   
3. history   
4. location   
5. navigator  

<br>

### BOM - window

모든 브라우저에서 제공하는 브라우저 창 전체에 대한 정보를 갖는 객체  
DOM객체 또한 window객체의 멤버로 있다.  
원래 `window.document`형식으로 쓰여야 하지만 최상위 객체인 window는 생략 가능하다.  

브라우저 창 크기를 px단위로 반환하는 
`innerHeight`, `innerWidth`,  `outerHeight`, `outerWidth` 속성이 있다.  

`window.inner...` 속성은 브라우저안에서 html문서를 출력하는 너비와 높이  

`window.outer...` 속성은 브라우저 자체(스크롤, 메뉴바 등등 포함)의 너비와 높이  

`document.documentElement.clientHeight`와 `document.documentElement.clientWidth`도 있는데 이는 `window.inner...`와 값이 같다.  
`document.documentElement`는 html태그를 가리키는데 `client...`는 태그의 width와 height를 반환한다)


> 참고: https://nuknukhan.tistory.com/19

---

`window.open()`, `window.close()` 메서드 이름부터 알 수 있듯이 새창 열기와 닫기이다.  

`open`메서드는 다음과 같은 양식을 같는다.  
`window.open([sURL],[sTarget],[sStatus],[bReplace])`

> http://koxo.com/lang/js/method/open_window.html

중요한 속서은 `sURL`과 `sStatus`  
url주소와 창의 크기, 위치 등을 조절할 수 있다.  

반환값으로 `open`한 html문서의 `window`객체를 반환한다.
```js  
newwindow = window.open(url, "", "+width=" + width + 
    ", height=" + height + ", top=" + 100 + ", left=" + left);
...
...
newwindow.close(newwindow);
```
변수로 받아두었다 `close`할 수 있음.  

광고창, 팝업창 등이 이 함수를 통해 열린다.  
```js
function html_load() {
  var newWindow = window.open(url);
  setTimeout(function () {
    newWindow.close();
  }, 3000);
}
```
html문서가 `onload`됨과 동시에 팝업창을 열고 3초후에 자동으로 `close`되도록 설정!  

---

`window.moveTo()` `window.moveBy()` `window.resizeTo()` `window.resizeBy()`  
위 함수들은 브라우저 위치를 이동시키고 크기를 조절하는 메서드이다.(pixel단위)

`window.moveTo(10, 10);` - 고정위치인 모니터 왼쪽 상단으로 부터 10만큼 떨어진 위치로 이동  

`window.moveBy(10, 10);` - 상대위치로 기존 위치에서 우측하단으로 10만큼 이동

`window.resizeTo(300, 300);` - 고정크기로 300만큼 크기로 설정

`window.resizeBy(10, 10);` - 상대크기로 기존 크기에서 10만큼 커짐

> window.open함수에 의해서 생성되지 않은 윈도우나 탭의 크기를 변경할 수 없음.   
> 해당 윈도우가 한개 이상의 탭을 포함하고 있을 경우, 해당 윈도우나 탭의 크기를 조정할 수 없음.  


<br>

### BOM - screen

브라우저 보단 사용자 컴퓨터 환경 정보를 갖고있다.  

**Property**|**Description**
:-----:|:-----:
availHeight|screen 높이 반환 (Windows Taskbar 제외)
availWidth|screen 넓이 반환 (Windows Taskbar 제외)
colorDepth|색품질 반환 (아주높음 32bit)
height|screen 전체 높이 반환
pixelDepth|색품질 반환 (아주높음 32bit)
width|screen 전체 넓이 반환

```js
var demo = document.getElementById("demo");
var output = "";
output += screen.width + "/" + screen.height + "<br>";
output += screen.availWidth + "/" + screen.availHeight + "<br>";
output += screen.colorDepth + "<br>";
output += screen.pixelDepth + "<br>";
demo.innerHTML = output;
```

출력값
```
1536/864
1536/824
24
24
```

<br>

### BOM - location

현재 페이지의 **`URL`** 주소에 대한 정보를 갖고 있는 객체
```js
console.log(location.hostname); //도메인 이름 출력
console.log(location.pathname); //도메인 뒤의 경로값
console.log(location.protocol); //프로토콜 출력
```

출력값
```
localhost
/WebPro/javascript/days09/ex10.html
http:
```

`href`속성과 `assign()`메서드를 통해 페이지 이동가능하다.  
```js
location.href = "http://naver.com";
// location.assign("http://naver.com");
```

<br>

### BOM - history

```js
history.back(); //뒤로가기
history.go(-1); //back과 같다.
history.forward(); //앞으로가기
history.go(1); //forward와 같다..
```
`go()` 메서드 안의 number를 통해 점프 가능하다.  
보안상의 이유로 `url`은 제공하지 않음

<br>

### BOM - navigator

방문자 브라우저 정보를 갖고있는 객체

```js
console.log(navigator.appName); //Netscape - 브라우저응용프로그램 이름
console.log(navigator.appCodeName); //Mozilla - 브라우저응용프로그램 코드
console.log(navigator.product); //Gecko - 브라우저 앤잔 아룸
console.log(navigator.cookieEnabled); //true - 쿠키 사용여부
console.log(navigator.appVersion); 
// 5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36

console.log(navigator.javaEnabled()); //false - 자바 사용 여부
console.log(navigator.platform); //Win32 - 운영체제
```

<br><br>

## 쿠키

웹에서 설정유지를 위해 사용하는 text형태의 파일.  

브라우저가 만들고 관리한기 때문에 브라우저별로 저장위치가 다르다.   
(그리고 chrome은 DB형식으로 저장하기 때문에 SQL뷰어같은게 필요함)  

사용 용도는 주로 다음 세 가지 목적을 위해 사용된다.  

**세션 관리(Session management)**  
서버에 저장해야 할 로그인, 장바구니, 게임 스코어 등의 정보 관리   

**개인화(Personalization)**  
사용자 선호, 테마 등의 세팅  

**트래킹(Tracking)**   
사용자 행동을 기록하고 분석하는 용도  

> https://developer.mozilla.org/ko/docs/Web/HTTP/Cookies

쿠키는 `키(ID)`, `값`, `만료일`, `도메인(경로)`로 구성된다.    

사용할 수 있는 속성과 값들은 다음과 같다.  
`name="nameValue"; expires="expireDate"; path="pathHolders"; domain="domainName"; secure `

**속성명**|**설명**
:-----:|:-----
`name`| `키` 이름
`nameValue`| `키`에해당하는 `값`  
`expires`| `만료시간` 설정 속성   
`path`| 서버의 `경로`, 경로에 해당하는 url에서 쿠키가 사용될 수 있다.  
`domain`| 서버의 `도메인`, 도메인에 해당하는 url에서 쿠키가 사용될 수 있다.   
`secure`| `HTTPS` 프로토콜일 경우에만 쿠키가 사용됨.  

`path`속성을 `/`로 설정시 웹 어플리케이션 안에서 쿠키를 사용 가능하다.  

`expireDate`속성에 들어가는 날짜형식은 `new Date().toUTCString()` 문자형식(`Thu, 18 Apr 2019 08:38:14 GMT`) 이 들어간다.  

> `toGMTString()`이 더이상 표준이 아님으로 위의 `toUTCString()` 사용을 권장한다.(브라우저에서 아지까진 지원한다)

쿠키는 `document.cookie`객체에 문자열을 삽입해 생성한다.  


쿠키를 만들고 가져오고 지우는 메서드 생성
```js
function setCookie(name, value, exdays) {
	var now = new Date();
	now.setDate(now.getDate() + exdays);
	now.setTime(now.getTime() + 1000*10) //10초 유지 추가
	//Thu, 18 Apr 2019 01:33:39 GMT
	document.cookie = name + "=" + escape(value) + "; expires=" + now.toUTCString() + "; path=/;";
	//localhost도메인에서 모두 사용하겠다고 하려면 path=/
}

function getCookie(name) {
	var cookies = document.cookie;
	var carr = cookies.split("; ");
	var result = "";
	for (let i = 0; i < carr.length; i++) {
		var rarr = carr[i].split("=");
		if (rarr[0] == name) {
			return unescape( rarr[1] );
		}
	}
	return null;
}

function deleteCokie(name) {
	//쿠키 삭제 메서드는 따로 없음으로 만료시점을 과거로 만들어 삭제한다.  
	// 고정 10일
	if(getCookie(name))
	{
		return;
	}
	var now = new Date();
	now.setDate(now.getDate() - 1); //과거로 설정
	//Thu, 18 Apr 2019 01:33:39 GMT
	document.cookie = name + "=" + "; expires=" + now.toUTCString();
}
```



### 인코딩과 디코딩

쿠키를 저장하고 다시 가져오는 과정에서 `escape`, `unescape` 사용하였는데 인코딩, 디코딩 하는 메서드이다.  

쿠키는 아스키 문자열만 인식 가능함으로 위의 메서드를 통해 한글같은 경우 16진수로 변환해주어야 한다.  

다음을 제외한 모든 문자열을 변환한다.
```
ABCDEFGHIJKLMNOPQRSTUVWXYZ
abcdefghijklmnopqrstuvwxyz
1234567890
@*-_+./
```
한글같은 경우는 16진수로 변환되어 저장된다.  

이외에도 `encodeURI()`, `encodeURIIComponent()` 메서드가 있는데 인코딩 하는 문자열의 범위가 각각 다르다.  

