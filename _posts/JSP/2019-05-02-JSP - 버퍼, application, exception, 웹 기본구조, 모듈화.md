---
title:  "JSP/Servlet - 버퍼, application, exception, 웹 기본구조, 모듈화!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - JSP

tags:
  - JSP
  - servelt

toc: true
toc_sticky: true

---

## JSP 버퍼

서버에서 클라이언트에게 데이터를 전송하는 과정은 다음과 같다.  

![image16](/assets/jsp/image16.png){: .shadow}{: width="500px"}  

클라이언트의 요청이 일어나면 jsp에서 만들어진 서블릿 객체가 `out.print()`메서드를 통해 버퍼에 출력할 값들을 저장한다.  

![image15](/assets/jsp/image15.png){: .shadow}{: width="500px"}    

일정이상의 버퍼가 모두 차면 클라이언트의 브라우저로 전송되며 출력되게 된다.  

자동으로 버퍼가 모두 차면 `flush`되도록 설정하려면 디렉티브(지시자)에 다음 설정이 있어야 한다  
`autoFlush="true"`  

`autoFlush`의 기본값이 true이기 때문에 `autoFlush="false"`별도 기입하지 않는 이상 자동으로 버퍼가 `flush`된다.  

버퍼의 크기는 `buffer="1kb"` 속성으로 지정할 수 있다.  

```js
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	autoFlush="false" buffer="1kb"
%>
```
디렉티브를 위와 같이 설정했을때 만약 버퍼가 다 차면 어떻게 되는지 보자.  

```js
<%
for(int i=0; i<1000; i++)
{
%>
	1234
<% 
}
%>
```
`java.io.IOException: Error: JSP Buffer overflow`예외가 발생한다.  

```js
<%
for(int i=0; i<1000; i++)
{
	if(i%20==0)
		out.flush();
%>
	1234
<% 
}
%>
```
중간중간에 `flush()`메서드를 설정하면 오류는 나지 않는다.

`out.clear()`메서드가 있는데 `flush()`는 클라이언트에게 보내고 비우는 거라면  
`clear()`는 보내지 않고 바로 비운다. 


## 웹 기본 구조

이클립스에서 하다 보니 실제 class파일들이 위치하는것, webapps위치가 약간 다르다.  
실제 아파치 서버를 깔면 에다 저장해야 한다.  

가장 상위폴더는 `...\apache-tomcat-8.5.39\webapps`폴더이다.  

`webapps`폴더 아래에 `jspPro`폴더를 만들고 안에 각종 jsp파일을 만들면 
`http://localhost/jspPro/`경로가 된다.  

`webapp`하위의 폴더들이 각각의 `contextpath`가 되며 하나의 웹 어플리케이션(사이트)이 되는것.  

> `request.getContextPath()`로 경로명을 구할 수 있다.  

`webapps`안에는 `ROOT`라는 폴더가 있는데 이 폴더는 특별히 `contextpath`를 생략 가능하다.   

``http://localhost/...`도메인명 바로 뒤에 url을 붙여 접근한다.  

eclipse에서 개발이 끝나고 배포하려면 `export`에서 `WAR File`로 배포하면 된다.  
만들어진 `.war`파일은 `webapps`아래에 위치시키면 끝.  


## 기본객체 - pageContext

`pageContext` 기본객체는 JSP페이지와 1:1로 연결된 객체로 다음 기능을 제공한다.  

* 기본객체 구하기

* 속성 처리하기

* 페이지의 흐름 제어하기

* 에러 데이터 구하기


`pageContext`를 통해서 다른 기본객체를 얻어올 수 있다.  


지금까지 `<% %>`스트릿코드안에서 바로 `request`객체나 다른 기본객체들을 사용해왔다.

`pageContext`를 통해서도 가져올 수 있다.

**메서드**|**반환형**|**설명**
:-----:|:-----:|:-----:
`getRequest()` | `ServletRequest` | `request` 기본 객체를 구한다. 
`getResponse()` | `ServletResponse` | `response` 기본 객체를 구한다. 
`getSession()` | `HttpSession` | `session` 기본 객체를 구한다. 
`getServletContext()` | `ServletContext` | `application` 기본 객체를 구한다. 
`getServletConfig()` | `ServletConfig` | `config` 기본 객체를 구한다. 
`getOut()` | `JspWriter` | `out` 기본 객체를 구한다.
`getException()` | `Exception` | `exception` 기본 객체를 구한다. 
`getPage()` | `Object` | `page` 기본 객체를 구한다. 

```java
requset = (HttpServletRequest)pageContext.getRequest();
response = (HttpServletResponse)pageContext.gerResponse();
```

`getRequest()` 메서드가 `Requset`객체를 반환함으로 `HttpServletRequest`로 다운캐스팅 해주어야한다. (`response`객체도 마찬가지)

`pageContext`는 커스텀태에서 새로운 변수를 추가할 때 에도 사용된다.
`<c:if test=""></c:if>`지금까지 사용해온 코어 태그 역시 일종의 커스텀 태그이다.  
단 너무 많이 사용하다 보니 라이브러리화된 것.




## 기본객체 - application

웹어플리케이션(**웹사이트 전체**)과 관련되있는, 웹어플리케이션 전반적인 정보를 가지고 있는 객체를 `application`객체라 한다.  

`application`객체를 사용해 서버정보, 웹 어플리케이션이 제공하는 자원(저장공간, 파일)을 읽어올 수 있다.  

> `application`의 자료형은 `ServletContext`타입이다.  

`web.xml`에 `<init-param>`태그를 사용했었는데 이는 **하나의 서블릿 객체 초기화시** 필요한 파라미터를 설정하는 태그이다.  

동일한 `contextPath`에 존재하는 서블릿에게(모든 서블릿에게) 초기화시 파라미터를 전달 해 주고싶다면 `<context-param>`을 사용하면 된다.  


`<context-param>`에서 매개변수를 사용하려면 `application`의
`getInitParameter("파라미터명")`와 `getInitParameterNames()` 메서드를 사용한다.  

web.xml에 아래와 같이 설정하자
```xml
<context-param>
	<description>로깅 여부</description>
	<description>블라블라</description> <!-- 설명을 담는 태그 -->
	<param-name>loginEnabled</param-name>
	<param-value>true</param-value>
</context-param>

<context-param>
	<description>디버깅 레벨</description>
	<param-name>debugLevel</param-name>
	<param-value>5</param-value>
</context-param>
```
`<description>`태그는 단순 설명 태그로 생략되어도 상관 없다.  


설정된 `context-param`은 모든 서블릿 클래스, jsp파일에서 가져올 수 있고 사용하는 함수는 아래와 같다.  

**메서드**|**리턴타입**|**설명**
|--|--|
`application.getInitParameter("name")`|`String`|이름이 `name`인, `context-param`에 설정된 `application`초기화 파라미터 `value`를 가져온다.
`application.getInitParameterNames()`|`Enumeration<String>`|모든 `application`초기화 파라미터 `name`을 가져온다.  

```js
<% 
Enumeration<String> en = application.getInitParameterNames();
while(en.hasMoreElements())
{
	String pname = en.nextElement();
	String pval = application.getInitParameter(pname);
	out.print(pname +": " + pval +"<br>");
}
%>
```
출력값
```
loginEnabled: true
debugLevel: 5
```

>jsp파일에선 application은 기본 제공되는 객체이지만 서블릿 클래스에선 기본 제공되지 않는다.  
따라서 `application`객체를 얻고 싶다면 `HttpServlet.getServletContext()`메서드를 호출해야 한다.  



### 웹 어플리케이션 자원 읽어오기

웹 어플리케이션의 자원 읽어 오는 것은 `application`에서 가장 많이 사용되는 기능으로  
실제 서버에 저장된 파일을 읽어오거나 저장할 때 정확한 위치를 통해 `File`객체와 입출력 스트림을 생성해야 한다.  

사용되는 메서드는 아래 3가지

**메서드**|**리턴타입**|**설명**
|---|---|---|
`getRealPath(String path)`|`String`|웹 어플리케이션 내에서 지정한 경로를 **시스템경로**로 반환한다.  
`getResource(String path)`|`java.net.URL`|웹 어플리케이션 내에서 지정한 경로를 접근할수 있는 **URL주소**로 반환한다.  
`getResourceAsStream(String path)`|`java.io.inputStream`|웹 어플리케이션 내에서 지정한 경로에 연결된 `inputStream`객체를 반환한다.  

예를 들어 `contextPath`가 `jspPro`라는 폴더일 때 실제 폴더위치는 다음일 것이다.  
`C:\Class\apache-tomcat-8.5.39\webapps\jspPro`  

그리고 `jspPro`프로 안에 `ex01.txt`라는 파일이 있을 때  
동일한 위치의 jsp파일이 이 파일에 접근하기 위해선 다음과 같은 주소를 사용해야 한다.  
`\ex01.txt`  

웹상에선 `url`주소를 사용하기 때문에 실제 시스템 경로가 아닌 `contextPath/파일명`형식으로 접근하게 된다.   

하지만 입출력 스트림을 통해 실제 서버안의 파일을 저장한다던가, 읽어올 때에는 절대경로인 시스템 경로명으로 접근해야 하는데  
시스템 주소(절대경로)를 가져오는 것이 `application`의 `getRealPath(String path)`메서드이다.   

그냥 `getRealPath("/")` 입력시 `contextPath`까지의 절대경로를 가져온다.   
`getRealPath("test")`입력시 다음 주소가 반환된다.  
`....webapps\jspPro\test` (`contextPath`가 `jspPro`임)  

파일 입력시 바로 `getResourceAsStream`를 사용하는 것 이 `inputStream`객체를 바로 반환하기 때문에 효율적이다.  

<br>

### 서버정보 읽어오기

`application` 기본객체로 서버관련 정보를 얻어올 수 있다.  

`application.getServerInfo()` - 서버 정보 반환  
`application.getMajorVersion()` - 서버 지원 서블릿 규약의 메이저 버전 반환    
`application.getMinorVersion()` - 서버 지원 서블릿 규약의 마이너 버전 반환  

```js
톰켓버전: <%= application.getServerInfo() %><br>

서블릿 규약:
<%= application.getMajorVersion() %>.
<%= application.getMinorVersion() %><br>
```

출력값
```
톰켓버전: Apache Tomcat/8.5.39
서블릿 규약: 3. 1
```

<br>

### 로그메세지 기록


`application`으로 로그기록도 한다

`application.log(String msg)` 혹은 `application.log(String msg, Throwable throwable);` 예외정보도 함께 로그에 기록한다.  

로그파일 저장 위치는 `톰켓설치폴더/logs/폴더` 안에 `localhost.yyyy-mm-dd.log`형식으로 저장된다.  


## 기본객체 - exception

`name 파라미터값: <%= request.getParameter("name").toUpperCase() %>`

다음과 같은 코드가 있고 `name`파라미터를 넘겨받지 못했다면 당연히 오류난다....(`null`값을 `toUpperCase()`했으니)

출력값
```
java.lang.NullPointerException 예외 발생

Type Exception Report
...
...
13: </script>
14: </head>
15: <body>
16: 	name 파라미터값: <%= request.getParameter("name").toUpperCase() %>
17: </body>
18: </html>
```

이런 페이지가 클라이언트에게 출력되면 보안적으로도 취약하고 보기도 싫다....

`try, catch`문으로 예외처리 가능하다.  

```java
<% 
try {
%>
	name 파라미터값: <%= request.getParameter("name").toUpperCase() %>
<%
}
catch (Exception e)
{
%>
	<%= e %>
<%
}
%>
```
출력값: `name 파라미터값: java.lang.NullPointerException `

하지만 서블릿 객체에서 발생하는 예외는 `try, catch`로 처리하지 않는다.  

### 에러페이지 이동

jsp에선 일반적으로 예외처리를 `try, catch`로 하지 않고 다른 **예외처리 페이지**로 이동시킨다.  

예외가 발생할만한 페이지에 예외처리를 위한  **페이지 설정 디렉티브(지시자)**를 추가하자  
`<%@ page errorPage="/error/viewErrorMessage.jsp" %>`  

만약 해당 파일에서 에러가 발생하면 `/error/viewErrorMessage.jsp`파일로 이동하게 된다.  


`viewErrorMessage.jsp`파일의 코드는 다음과 같다.  
```html
<!-- viewErrorMessage.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
...
...
<body>
요청 처리과정에서 에러가 발생하였습니다. <br>
바른 시간 내에 문제를 해결하도록 하겠습니다.  

<p>
	에러타입: <%= exception.getClass().getName() %><br>
	에러 메세지: <%= exception.getMessage() %> 
</p>
</body>
...
```
현제페이지가 **에러처리용 페이지**임을 알리는 디렉티브`<%@ page isErrorPage="true" %>`가 있어야  `exception`기본객체를 사용 가능하다.  


### 응답 상태별 에러페이지 설정하기

각 페이지별로 예외처리 디렉티브를 추가하는 것이 상당히 비효율 적이기 때문에 한꺼번에 처리할 수 있도록 `web.xml`에 설정해보자.  

또한 HTTP 에러코드는 여러가지 종류가 있는데 에러코드, 에러를 발생시키는 예외 타입에 따라 별도의 에러페이지를 띄울 수 있다.  

`400`, `404`, `500` 등 여러 응답 코드별로 에러페이지를 지정해보자.  

`web.xml`에서 `<error-page>`태그 설정이 필요하다.  

`<error-page>`태그 안에 들어가는 속성은 `<error-code>`, `<exception-type>`이 있고 이를 처리하는 location이 필요하다.  

양식: `Content Model : ((error-code | exception-type)?, location)`  
두개의 속성중 하나만 사용하면 되고 에러페이지의 위치정보(`location`)은 필수로 있어야 한다.  


```xml
<error-page>
	<error-code>404</error-code>
	<location>/error/error404.jsp</location>
</error-page>

<error-page>
	<error-code>500</error-code>
	<location>/error/error500.jsp</location>
</error-page>
```

404에러의 경우 `error404.jsp`페이지를  
500에러의 경우 `error500.jsp`페이지를 출력한다.  

> `web.xml`은 `contextPath`안에서 발생한 에러코드만 관리한다.  

```html
<!-- error500.jsp -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
...
...
<body>
	<h2>서버 내부에서 에러가 발생했습니다.</h2>
	관리자에게 문의하세요.
</body>
...
```

`web.xml`로 공통적인 에러처리 할 때에는 에러페이지를 알리는 **디렉티브를 설정할 필요 없다.**  


예외설정시 범위가 겹치는 부분이 생기는 것을 알 수 있는데  
예외페이지를 띄우는 우선순위가 존재한다.  

1. 디렉티브
2. error-type
3. error-code
4. 웹컨테이너 제공 기본 에러페이지

<br><br>

## 페이지 모듈화, 요청흐름 제어

페이지 상단 배너, 하단 배너는 거의 바뀌지 않는다.  

상단, 하단 외에도 고정 배너들이 많은데 각 서블릿 객체, jsp페이지마다 html코드를 넣는것 보다 별도의 파일로 두고 불러오는 것이 효율적이고 유지보수도 편하다.  

이렇게 별도의 파일을 하나의 파일로 관리하는 것을 모듈화라고 한다.  

모듈화된 파일을 합치는 방법은 2가지 있다. 

1. `<jsp:include>`액션태그를 사용  

2. `<%@ include="" %>` include 디렉티브(지시자)를 사용

(이로써 모든 디렉티브 종류가 다 나왔다.)


`include`액션태그는 **두개의 서블릿 클래스가 출력버퍼를 공유**해 가며 클라이언트에게 보여줄 데이터를 작성하고

디렉티브를 사용하면 **두개의 jsp파일이 하나의 서블릿 클래스로** 만들어진다.  

```html
...
...
<div style="text-align: center">
<table width="600px" border="1" align="center">
<tr>
	<td colspan="3">
		<jsp:include page="/layout/top.jsp">
			<jsp:param value="홍길동" name="name"/>
		</jsp:include>
	</td>
</tr>
<tr height="300px">
	<td width="150px" valign="top" style="background-color: yellow">
		<jsp:include page="/days07/layout/left.jsp"></jsp:include>
	</td>
	<td>
		게시판 내용 부분
	</td>
	<td style="background-color: red">
		<jsp:include page="/days07/layout/right.jsp"></jsp:include>
	</td>
</tr>
<tr>
	<td colspan="3">
		<jsp:include page="/layout/bottom.jsp">
			<jsp:param value="홍길동" name="name"/>	
		</jsp:include>
	</td>
</tr>
</table>
</div>
...
...
```

![image17](/assets/jsp/image17.png){: .shadow}{: width="500px"}    

`<jsp:include>`액션태그를 통해 `top.jsp`, `left.jsp`, `right.jsp`, `bottom.jsp` 그리고 현재 페이지인 `template.jsp`까지 출력값을 한데 모아 클라이언트에게 전송하게 된다.  

```xml
<jsp:include page="/layout/top.jsp">
	<jsp:param value="홍길동" name="name"/>
</jsp:include>
```
또한 `<jsp:param>`을 통해 파라미터로 값을 넘길 수 있는데 
`EL`을 사용하거나 `request.getParameter`로 값을 전달 받을 수 있다. `${param.user}`  
`<h3>${param.user} 웹 사이트 BOTTOM 소개</h3>`  

`<jsp:param>`으로 파라미터를 보낼 때 `ISO-8859-1`방식으로 인코딩 하기 때문에   
`<%request.setCharacterEncoding("UTF-8"); %>`로 인코딩 방식을 변경해주어야 한다.  


UI적인 요소를 모듈화 해서 붙여넣을 수 도 있지만  
웹사이트 전체에 사용되는 공통적 Java 코드 기능을 선언, 수행시키도록 모듈화 할 수 있다.  

보통 UI수행 요소는 `<jsp:include>`액션 태그,  
JSP공통 기능을 포함시키는 것은 `<%@ include="" %>`지시자를 사용한다.   
(물론 include지시자로 레이아웃을 잡을 수 도 있다.)  


```html
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	//웹사이트 전체에 사용되는 공통적 기능을 선언, 수행
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
	String downloadPath = "download";
%>
<h3>TItle = <%= title %></h3>
```

```html
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String title = "지시자 테스트";
%>
<!DOCTYPE html>
<html>
<head>
</head>
<body>
	<%@ include file="includee.jspf" %>
	contextPath = <%= contextPath %><br>
	downloadPath = <%= downloadPath %><br>
</body>
</html>
```

### web.xml에서 모듈 추가하도록 설정

만약 웹 어플리케이션에 필요한 페이지가 500페이지가량 된다면  나온다면 `inlcude` 지시자를 500번 추가해야 하나?  

`web.xml`설정 한번으로 모든 페이지에 모듈화된 페이지를 삽입시킬 수 있다.  


아래 `includee.jspf`파일은 모든 웹페이지에 추가시켜야할 정보가 들어가 있다.  
`jspf`란 확장자는 모듈페이지를 표기하기 위해 사용하는 것으로 `jsp`확장자를 사용해도 무관하다. 그저 구분을 위해 사용한 것.
```js
/* includee.jspf */
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	//웹사이트 전체에 사용되는 공통적 기능을 선언, 수행
	request.setCharacterEncoding("UTF-8");
	String contextPath = request.getContextPath();
	String downloadPath = "download";
%>
```

이제 위의 `includee.jspf`를 내가 설정한 `url-pattern`에 해당하는 모든 페이지에 모듈을 추가해보자.   
```xml
<jsp-config>
	<jsp-property-group>
		<url-pattern>/days08/*</url-pattern>
		<include-prelude>/days08/includee.jspf</include-prelude>
		<!-- <include-coda></include-coda> -->
	</jsp-property-group>
</jsp-config>
```

`<jsp-config>`, `<jsp-property-group>`, `<include-prelude>`, `<include-coda>`  

처음보는 태그가 대거 등장했는데 모두 모듈을 추가하기 위한 태그들이다.  

`include-prelude`은 페이지 상단에 추가되는 코드가 있는 모듈 페이지를 추가하고

`include-coda`은 페이지 하단에 추가되는 코드가 있는 모듈 페이지를 추가한다.  


보통 사이트를 보면 상단에는 상단 배너(검색창이나 목록, 연관 사이트보기 등)이 있고  

하단에는 하단배너(저작권 표시 등)이 있다.  

모든 페이지에 이런 배너들이 상, 하단에 반복되기 때문에 `<jsp-config>`태그를 통해 한꺼번에 넣어주면 편하다.  

