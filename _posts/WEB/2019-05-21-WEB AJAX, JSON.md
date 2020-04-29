---
title:  "Web - AJAX, JSON!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - HTML

tags:
  - web
  - html
  - jquery
  - javascript

toc: true

---

## AJAX (Asynchronous Javascript And XML)

비동기식 자바스크립트 XML(Asynchronous Javascript And XML)의 약자  

별도 프로그램을 설치하거나 **웹페이지를 다시 로딩하지 않고**도 메뉴 등 화면상의 객체를 자유롭게 움직이기(변경하기) 위해 사용하는 기술

페이지 전체 이동이 아니라 일부만 요청, 응답이 일어나기 때문에 성능 효율이 좋고  
모든 응답을 기다릴 필요 없기 때문에 사용자 접근성 또한 높아진다.  

AJAX도 자바스크립트이기 때문에 브라우저별로 코딩이 다르며 보안적 측면에 신경써야할 부분이 많다는 단점이있다.

* 회원가입, 로그인

* 우편번호 검색

* 검색 자동완성

* ID중복체크 등등...


위와같은 작업을을 AJAX로 처리할 수 있다. 

서버와 통신하며 전체화면을 변경하지 않고 결과값을 받고 싶을 때 AJAX를 사용하면 된다.  

### AJAX 처리 객체 - `XMLHttpRequest`

AJAX처리를 위한 JavaScript객체로 브라우저가 제공하는 `XMLHttpRequest`가 있다.  

어쩃건 AJAX도 웹서버에 데이터를 요청하면 응답하는 값을 뿌려주는 것이기 때문에 다음과 같은 과정을 거친다.  

1. `XMLHttpRequest`객체 가져오기
2. `XMLHttpRequest`설정한다. (url, get/post방식, 동기/비동기, 콜백함수)  
3. 실제 요청 url을 서버로 전달한다.  
4. 응답받은 데이터(xml, json, text 등)를 처리한다.  

위의 작업을 `XMLHttpRequest`객체가 처리해준다.  

가장 간단한 예제로 `XMLHttpRequest`이 어떤식으로 동작하는지 알아보자.  

클라이언트가 AJAX로 **서버에게 특정 url로 요청**하면 서버에 저장된 `sample.txt`파일의 데이터를 읽어 전달받는 코드를 작성해보자.

```html
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ajax with javascript</title>
<script>
	var httpRequest;
	function getXMLHttpRequest() {
		if (window.ActiveXObject) {
			try {
				return new ActiveObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					return new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {
					return null;
				}
			}
		} else if (window.XMLHttpRequest) {
			return new XMLHttpRequest;
		} else
			return null;
  }
  //window객체에 ActiveXObject객체가 존재한다면 IE 브라우저이다.  
  //크롬의 경우 XMLHttpRequest 사용

	function load(url) {
    //1. XMLHttpRequest객체 가져오기
    httpRequest = getXMLHttpRequest();
    
    //2. XMLHttpRequest 설정하기
		httpRequest.onreadystatechange = callback;
		//httpRequest상태가 바뀔때마다 호출할 함수를 등록
		httpRequest.open("GET", url, true); //설정 open

    //3. 실제 요청 url을 서버로 전달
		httpRequest.send(null); //요청

	}
  //콜백함수 - 요청받은 데이터 확인하기 위한 함수
  //4. 응답받은 데이터 처리
	function callback() {
		if (httpRequest.readyState == 4) {
      //데이터를 잘 가져왔는지 확인.
			if (httpRequest.status == 200) {
				document.getElementById("demo").innerHTML = httpRequest.responseText;
			}
			else {
				alert("AJAX requeset failed...." + httpRequest.status);
			}
		} 
	}
</script>
</head>
<body>
	<input type="button" value="javascript ajax" onclick="load('sample.txt')"/>
	<div id="demo"></div>
</body>
</html>
```
설정과정을 자세히 살펴보자.  

먼저 서버에서 응답이 오면 응답데이터를 처리할 callback함수를 등록한다.  
`httpRequest.onreadystatechange = callback;`

`onreadystatechange`는 `httpRequest`상태가 바뀔때마다 호출할 함수를 등록하는 것 인데  

request의 상태값은 다음과 같이 나뉜다.

num | 상태 | 설명
|---|---|---|
0 | uninitialized | request가 초기화되지 않음
1 | loading | 서버와의 연결이 성사됨
2 | loaded | 서버가 request를 받음
3 | interactive | request(요청)을 처리하는 중
4 | complete | request에 대한 처리가 끝났으며 응답할 준비가 완료됨

여기서 요청에 대한 응답이 온 상황인 4번째 상황만 콜백함수가 처리하면 되기 때문에  
`if (httpRequest.readyState == 4)` 이런 조건문이 들어간다.  

그 다음에는 `GET`방식인지 `POST`방식인지 설정하고 url을 등록한다.   

`httpRequest.open("GET", url, true)`

3번째 매개변수 `boolean`값은 동기 비동기 선택여부를 정한다, 비동기화 방식이 `true`이다.   

> 동기와 비동기의 차이는 응답받는 것을 마치고 다음 코드를 수행하느냐, 응답받는 것을 기다리지 않고 바로 다음 코드를 수행하느냐 이다.  

`XMLHttpRequest`객체 설정이 끝나면 설정한 url과 요청방식, 동기/비동기 방식 대로 서버에 요청한다.  

`httpRequest.send(null)`   

`send()`안의 매개변수가 null인데 POST방식의 경우 `send()`메서드의 매개변수로 파라미터값이 들어간다.  

`httpRequest.send("city=Seoul&zipcode=06141")`

GET방식의 경우 url뒤에 `?`와 함께 파라미터가 전달되기 때문에 `send()`안의 매개변수를 null로 설정한다.  

> get방식일경우 open메서드의 url에 파라미터를 붙이고  
post방식일 경우 send메서드의 url에 파라미터를 붙인다.    


## jquery를 사용한 AJAX - load()

jquery로 AJAX를 사용하면 더 간단하게 처리 가능하다.  

위에서 `<div id="demo">`에 ajax로 응답받은 데이터를 간단히 출력하는 코드를 작성하였는데  
`.load (url [, data] [, complete])` 메서드를 사용하면 3줄로 끝난다.  

```html
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ajax with javascript</title>
<script type="text/javascript">
	$(document).ready(function() {
		$("#btnAjax").click(function() {
			$("#demo").load("sample.txt", function(response, status, xhr) {
				if(status == "error")
				  alert("AJAX requeset failed...." + status);
			} );
		});
	});
</script>
</head>
<body>
	<input type="button" value="jquery ajax" id="btnAjax" onclick=""/>
	<div id="demo"></div>
</body>
</html>
```


`url`, `파라미터`, `callback함수`를 `load`의 매개변수로 등록과 동시에 호출한다.  


위에는 url과 함수만 매개변수로 넘겼지만 두 번째 매개변수로 **객체를 넘기면 POST방식으로 호출**한다.
`.load( "smaple.jsp", { limit: 25 }, function(){...} )`

> $( "#new-projects" ).load( "/resources/load.html #projects li" );  
`load()` 메서드의 추가기능으로 jQuery는 리턴 된 문서를 구문 분석하여 원하는 데이터만 읽어올 수 있다.  

> http://api.jquery.com/load/#load-url-data-complete  

## JSON

`JSON: JavaScript Object Notation` **자바스크립트를 사용한 객체 표기법**을 `JSON`이라 한다.

데이터를 저장하고 서로 교환할 때 사용하는 표기법이다.  

먼저 자바스크립트 객체를 어떻게 사용하는지 간단히 알아보자.

자바의 경우 `class`선언을 다음과 같이 했다.  

```java
class Member {
	private String id;
	private int age;

	public Member(id, age) {
		this.id = id;
		this.age = age;
	}
	public void display() {
		...
	}
}
...
...
Member mem = new Member(id, age);
```

자바스크립트의 경우 `function`을 사용해 객체생성자로 객체를 생성할수 있다.  

```js
var Member = function (id, age) {
	this.id = id;
	this.age = age;

	this.display = function(){
		...
	}
}
...
...
var mem = new Member(id, age);
```

물론 객체생성자를 사용하지 않고 바로 생성할 수 있으며  
자바스크립트는 `prototype`을 통해 새로운 메서드, 필드를 확장, 삭제할 수 있다.  

```js
var person = {
  firstName: "John",
  lastName: "Doe",
  age: 50
};
person.nationality = "English";

var person2 = new Object();
person2.firstName = "John";
person2.lastName = "Doe";
person2.age = 50;

delete person2.age;
```

> https://kouzie.github.io/html/WEB-JavaScript-6일차/

자바스크립트의 객체를 만들어 JSON객체로 변경후 서버로 전송하고,  
JSON객체를 받아 자바스크립트 객체로 변경후 클라이언트에서 사용할 수 있다.  

AJAX로 여러가지 데이터(문자열, xml)을 넘길 수 있지만 JSON을 사용하는 것이 효율적이다.  

### javascript객체를 JSON객체로 변경

```js
var myObj = {name: "John", age: 31, city: "New York"};
var myJSON = JSON.stringify(myObj);
window.location = "demo_json.jsp?x=" + myJSON;
```

매우 간단하다. `JSON.stringify()`함수만 사용하면 바로 객체변경이 가능하다.  

JSON객체는 자바스크립트의 객체 선언형식으로 작성된 **문자열**이다.  

데이터 전송, 수신간에는 문자열로 보내고 받는 것이 편하기 때문에  

**JSON은 일정한 규칙의 문자열이라 할 수 있다.**  


### JSON객체를 javascript객체로 변경

```js
var myJSON = '{"name":"John", "age":31, "city":"New York"}';
var myObj = JSON.parse(myJSON);
document.getElementById("demo").innerHTML = myObj.name;
```

다시 역으로 변경하는 것 도 매우 간단.  

json객체의 내용은 자바스크립트 객체처럼 `name` 과 `value` 한쌍으로 이루어진다.  
`name`은 `String`형태이다, `value`는 객체, 배열, 문자열, 숫자, 부울, null이 올 수 있다.  

그리고 자바스크립트에서 **JSON객체는 문자열**로 참조하기 때문에 앞 뒤에 `'`가 붙어있다. 밖에 홀따옴표가 오기 때문에 문자열을 표기할 때는 **무조건 쌍따옴표를 사용해야 한다**.  

`JSON.parse()`메서드는 JSON문법으로 표기된 문자열을 javascript객체로 변경하여 반환한다.  


JSON객체는 javascript 객체 표기법 구문에서 파생되었기 때문에 크게 다르지 않다.  

아래는 emp테이블안의 정보를 json객체로 만든것이다.  
```js
{"emp":[
		{"empno":7839,"ename":"KING"},
		{"empno":7902,"ename":"FORD"},
		{"empno":7566,"ename":"JONES"},
		{"empno":7698,"ename":"BLAKE"},
		{"empno":7782,"ename":"CLARK"},
		{"empno":7499,"ename":"ALLEN"},
		{"empno":7844,"ename":"TURNER"},
		{"empno":7934,"ename":"MILLER"},
		{"empno":7654,"ename":"MARTIN"},
		{"empno":7521,"ename":"WARD"},
		{"empno":7900,"ename":"JAMES"},
		{"empno":7369,"ename":"SMITH"}
	]
}
```
`emp`라는 `name`에 `value`로 객체 배열이 있다.  

배열의 하나의 객체 안에는 `empno`와 `enmae`이라는 `name`의 속성이 정의되어 있다.  

만약 `TURNER`라는 value값에 접근하고 싶다면 아래 수식으로 접근가능하다.  
`emp[6].ename` 혹은 `emp[6][ename]`

위의 데이터를 만드려면 아래와 같은 java 코딩이 필요하다.

```java
String jsonData = "{";
jsonData += "\"emp\":[";
while (rs.next()) {
	int empno = rs.getInt("empno");
	String ename = rs.getString("ename");
	jsonData += "{\"empno\":" + empno + ",\"ename\":\"" + ename + "\"},";
}
jsonData = jsonData.substring(0, jsonData.length() - 1);

jsonData += "]";
jsonData += "}";
```

상당히 코드가 더러운데 JSON라이브러리를 사용하면 깔끔하게 처리 가능하다.

## $.ajax() - ajax와 jquery

json객체를 보내고 받을때 `httpRequest.open("GET", url, true)`, `httpRequest.send(null)`, `.load()` 등의 함수를 사용했는데  

`$.ajax()` 함수를 사용하면 한번에 가능하다.  
파라미터 지정부터 콜백함수 등록까지!


### JSON객체를 위한 java 라이브러리

위와같이 문자열 형태로 이어붙이는 방법은 실수하기 쉽고 유지보수가 어렵기 때문에 JSON객체를 만들때 라이브러리를 사용한다.  

> https://code.google.com/archive/p/json-simple/downloads

`json-simple-1.1.1.jar` 하나만 다운받으면 사용가능하다.  

사용되는 객체로는 `JsonObject`와 `JsonArray`가 있다.  

```java
JSONObject jobj = new JSONObject();
JSONArray jarr = new JSONArray();
```

`JSONObject` 클래스에는 `put(key, value)`, `remove(key)` 등의 메서드가 있고  
`JSONArray` 클레스에는 `add(Object)`, `remove(index)`, `remove(Object)` 등의 메서드가 있다.  


바로 위에서 만든 `emp` json객체를 JSON 라이브러리를 사용해서 만들어보자.  
```java
<%@page import="org.json.simple.JSONObject"%>
<%@page import="org.json.simple.JSONArray"%>
<%@page import="com.util.ConnectionProvider"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>
<%@ page trimDirectiveWhitespaces="true"
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	String sql = "select empno, ename from emp order by sal desc";
	JSONObject jsonData = null;
	try {
		conn = ConnectionProvider.getConnection();
		pstmt = conn.prepareStatement(sql);
		rs = pstmt.executeQuery();
		jsonData = new JSONObject();

		JSONArray jsonEmpArray = new JSONArray(); //[{empno:??, ename:??, sal:??}, {...}, {...}]

		while (rs.next()) {
			JSONObject jsonEmp = new JSONObject();
			int empno = rs.getInt("empno");
			String ename = rs.getString("ename");
			jsonEmp.put("empno", empno);
			jsonEmp.put("ename", ename);
			jsonEmpArray.add(jsonEmp);

		}

		jsonData.put("emp", jsonEmpArray);

	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		pstmt.close();
		rs.close();
		conn.close();
	}
%>
<%=jsonData%>
```
`jsonData`의 `toString()` 메서드를 출력하면 요청한 클라이언트에게 jsonObject가 문자열로 전송된다. 


```js
$(document).ready(function() {
	$("#jsontest").click(function() {
		$.ajax({
			url : "emp_json.jsp",
			dataType : "json",
			data: {
				"detpno": 20
			},
			type : "get",
			cache : false, //로컬말고 서버에 재요청 
			success : function(data) {
				$(data.emp).each(function(index, e) {
					var info = e.empno + " / " + e.ename;
					$("#demo").append("<div>" + info + "</div>");
				});
			},
			error : function() {
				alert("에러~~~~")
			}
		});
	});
});
...
...
<input type="button" id="jsontest" value="jquery+ajax+json" />
<div id="demo"></div>
```

```
7839 / KING
7902 / FORD
7566 / JONES
7698 / BLAKE
7782 / CLARK
7499 / ALLEN
7844 / TURNER
7934 / MILLER
7654 / MARTIN
7521 / WARD
7900 / JAMES
7369 / SMITH
```

`success :`에서 성공시 호출하는 콜백함수를 등록하고 
`error :`에서 실패시 호출하는 콜백함수를 등록한다.  
`complete: `정상이든 비정상인든 실행이 완료될 경우 실행될 함수.  

`data:`에서 파라미터를 추가한다. post방식의 경우 무조건 `data:`를 사용해야 하지만 get방식의 경우 url뒤에 붙여보내도 상관없다.  

### 자동완성

> https://jqueryui.com/autocomplete/

위 사이트에서 결과물 배열을 DB에서 읽어온 JSON객체로 대체해서 자동완성 기능을 구현해보자.  
```html
<!DOCTYPE html>

<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Document</title>
	<style>
		#txtSearch {
			border: solid 1px black;
			width: 250px;
		}

		#searchList {
			visibility: hidden;
			width: 250px;
			height: 200px;
			border: solid 1px gray;
			position: absolute;
			left: 70px;
			top: 30px;
		}

		#searchList_Header {
			width: 96%;
			height: 16px;
			background: #d3d3d3;
			padding: 5px;
			font-size: 13px;
			font-weight: bold;
		}

		.item {
			color: red;
			font-weight: bold;
		}
	</style>
</head>

<body>
	<div>
		검색어 : <input type="text" id="txtSearch" name="txtSearch" onkeyup="txtSearch_keyup();" />
		<div id="searchList">
			<div id="searchList_Header">검색어 자동 완성</div>
			<div id="searchList_Items"></div>
		</div>
	</div>
	<hr>
	<p>.</p>
	<p>.</p>
	<p>.</p>
	<p>.</p>
	<script>
		var items; //검색결과 저장용 변수

		function txtSearch_keyup() {
			var search_list = document.getElementById("searchList")
			console.log("search_list: " + search_list);
			var search_list_items = document.getElementById("searchList_Items")
			var searchWord = document.getElementById("txtSearch").value;
			//이전 검색결과 모두 제거
			search_list_items.innerHTML = "";
			//검색어 값이 없으면 검색창을 보일 필요 없음.
			if (searchWord == "") {
				search_list.style.visibility = "hidden";
				return;
			}
			switch (searchWord) {
				case "가":
					items = ["가","가격","가격비교","가격비교사이트","가자미","가자재곤","가판대","가수","가지런한",];
					break;
				case "가격":
				items = ["가격","가격비교","가격비교사이트"];
					break;
				case "가격비":
				items = ["가격비교","가격비교사이트"];
					break;
				case "가자":
				items = ["가자미","가자재곤"];
					break;
			}
			for (let i = 0; i < items.length; i++) {
				var item = document.createElement("div");
				item.innerHTML = items[i].replace(searchWord, "<span class='item'>"+ searchWord +"</span>")
				item.onmouseover = function () {
					this.style.backgroundColor = "#ffcc66";
				}
				item.onmouseout = function () {
					this.style.backgroundColor = "#fff";
				}
				search_list_items.appendChild(item);
			}
			search_list.style.visibility = "visible";
		}
	</script>
</body>

</html>
```

> 참고: url과 파라미터를 요청하면 어떤 json객체가 오는지 보고 싶을때 아래 프로그램을 사용하자.  


### $.getJSON

URL하나만 사용해 JSON데이터를 가져와야 하는 상황이라면 `ajax`함수를 사용하기 보다 `getJSON`함수를 사용하면 더 수월하게 처리 가능하다.  

```js
$.getJSON("/replies/all/${param.bno}/"+page, function(data) {
	console.log(data.list.length);
	var str="";
	$(data.list).each(function() {
			str += "<li data-rno='"+this.rno+"' class='replyLi'>"
				+ this.rno + ":" + this.replytext
				+ "<button>mod</button></li>"
		}
	);
	$("#replies").html(str);
	printPaging(data.pageMaker);
});
```

ajax를 위한 데이터객체를 별도로 정희할 필요 없이 url만 쓰면된다.  