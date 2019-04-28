---
title:  "JSP/Servlet - doGet, Servelet LifeCycle, request, response, URL Mapping!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - JSP

tags:
  - JSP
  - servelt

toc: true

---


## Servlet - doGet(), doPost(), service()

클라이언트가 서버에게 html페이지를 요청할 때   
http프로토콜 요청종류는 여러가지 있지만  
`url`에 해당하는 html페이지 요청시에는 `get`방식 요청, `post`방식 요청이 있다.  

`<form action="MethodServlet" method="get">`  
`<form action="MethodServlet" method="post">`  

서블릿 객체는 요청 종류에 따라 `doGet()`, `doPost()` 메서드를 호출한다.  

```java
/**
 * Servlet implementation class MethodServlet
 */
@WebServlet("/days02/MethodServlet")
public class MethodServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public MethodServlet() {
        super();
        // TODO Auto-generated constructor stub
        System.out.println("MethodServlet init....");
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.service(req, resp);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		System.out.println("called doGet()...");
		PrintWriter out = response.getWriter();
		String msg = request.getParameter("msg");
		out.append(msg);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8"); //응답받은 문자열을 UTF-8로 디코딩
		response.setContentType("text/html; charset=UTF-8"); //응답할 html페이지를 UTF-8로 설정
		doGet(request, response);
		System.out.println("called doPost()...");
	}
}
```
여기서 `service`란 오버라이드 메서드가 보인다.  
`service`메서드는 `get`방식, `post`방식 상관없이 호출되는 메서드로 `service`메서드가 서블릿 객체에 오버라이딩 되어있다면 `doGet()`, `doPost()`가 호출되지 않고 `service`메서드가 호출된다.  

```java
@Override
protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// TODO Auto-generated method stub
	super.service(req, resp);
	System.out.println("service method called...");
}
```

부모클래스의 `super.service(req, resp)` `service`메서드를 호출하면 `get`, `post`방식에 맞추어 `doGet()`, `doPost()`메서드가 호출된다.  

get, post방식 상관없이 공통적인 작업을 해야 한다면 `service`메서드를 사용토록 하자.  

## Servlet - 서블릿 객체 LifeCycle

서블릿 객체의 생성, 삭제과정은 다음과 같이 이루어진다.  

1. 클라이언트 요청으로 인한 서블릿 객체 생성   
2. 서버 종료, 재시작으로 인한 서블릿 객체 삭제  

한번 서블릿 객체가 생성되어 메모리에 올라가면 서버가 내려갈 때 까지 남아 있기 때문에 다음 클라이언트의 요청시에는 다시 생성할 필요 없다.  
```java
@Override
public void init() throws ServletException {
		super.init();
	System.out.println("called init()...");
}

@Override
public void destroy() {
		super.destroy();
	System.out.println("called destroy()...");
}
```
`init()`, `destory()`메서드를 오버라이딩 해서 **객체 생성**, **삭제**를 확인할 수 있다.  

### 서버 시작시 Servlet객체 올리기

객체를 생성해서 메모리에 올리는 과정이 제일 오랜 시간이 필요한 작업이다.  
DB연결객체같은 경우 이 과정에서 매우 많은 시간을 필요로 한다.  

클라이언트 요청이 들어올 때 객체를 메모리에 올리면 답답함으로 서버 시작과 동시에 필요한 객체는 메모리에 생성될 수 있도록 설정할 수 있다.  
```java
public class DBCPInit extends HttpServlet{
private static Connection connection = null;
@Override
public void init() throws ServletException {
	super.init();
	loadJDBCDriver();
	initConnectionPool();
	}
	...
	...
}
```

위와 같은 Servlet객체가 있을 때 서버 시작과 동시에 생성하려면 `web.xml`(배포서술자)에 다음과 같이 설정을 추가해야 한다.  
```xml
<servlet>
	<servlet-name>DBCPInit</servlet-name>
	<servlet-class>days02.DBCPInit</servlet-class>
	<load-on-startup>1</load-on-startup>
</servlet>
```

`load-on-startup` 태그안의 숫자는 객체 생성 순번이다.  

`DBCPInit`서블릿 객체를 서버 시작과 동시에 1번째로 메모리에 생성하겠단 뜻이다.  

### DB연결 서블릿 객체 생성

java에서 DB와 연결하려면 JDBC때 했던 과정이 필요하다.  
> https://kouzie.github.io/jdbc/JDBC.-1일차/#jdbc-driver  

다른점은 `ojdbc6.jar`파일을 build path에 추가하는 것이 아닌 `\WebContent\WEB-INF\lib`위치에 추가해야 한다는것.  

이제 DB 서블릿 객체에 `db url`, `user`명, `password`와 로드한 `jdbc driver`를 사용해 `Connection` 객체를 만들면 된다.  

`url`, `user`명, `password`는 수시로 바뀔수 있기 때문에 서블릿 클래스의 `.java`파일 안에 문자열로 하드코딩하지 않고  
`web.xml`(배포서술자)에서 전달해주는 값을 사용한다.  

`web.xml`에서 `<init-param>`태그를 사용하면 DB연결용 서블릿 객체에게 데이터를 전달할 수 있다.  

```xml
<servlet>
	<servlet-name>DBCPInit</servlet-name>
	<servlet-class>days02.DBCPInit</servlet-class>
	<init-param>
		<param-name>url</param-name>
		<param-value>jdbc:oracle:thin:@172.17.107.68:1521:xe</param-value>
	</init-param>
	<init-param>
		<param-name>user</param-name>
		<param-value>scott</param-value>
	</init-param>
	<init-param>
		<param-name>password</param-name>
		<param-value>tiger</param-value>
	</init-param>
	<load-on-startup>1</load-on-startup>
</servlet>
```
> `<load-on-startup>1</load-on-startup>`은 마지막에 위치해야 한다.  


`web.xml`로부터 전달받은 파라미터는 `ServletConfig`의 `getInitParameter()`메서드를 사용해 가져올 수 있다.  

> `HttpServlet`이 `ServletConfig` interface를 구현한다.  

```java
public class DBCPInit extends HttpServlet{
	private static Connection connection = null;
	@Override
	public void init() throws ServletException {
		super.init();
		loadJDBCDriver();
		initConnectionPool();
	}

	private void initConnectionPool() {
		String url = this.getInitParameter("url");
		String user = this.getInitParameter("user"); 
		String password = this.getInitParameter("password"); 	
		try {
			connection = DriverManager.getConnection(url, user, password);
			System.out.println("create Connection success");
		} catch (SQLException e) { 
			System.out.println("create Connection error");
			e.printStackTrace();
		}
	}

	private void loadJDBCDriver() {
		String className = "oracle.jdbc.driver.OracleDriver";
		try {
			Class.forName(className);
			System.out.println("JDBC Driver loading complete!");
		} catch (ClassNotFoundException e) { 
			e.printStackTrace();
		}
	}
}
```
만약 `getInitParameter`매개변수로 없는 `parameter`이름을 넣으면 `null`을 반환한다.  





## 기본객체

### request

서버에서 클라이언트가 보낸 파라미터를 읽어오려면 `request`객체의 `getParameter...()`메서드를 사용해야 한다.  

**`request.getParameter(String name)`**  
`name`에 해당하는 파라미터의 `value`를 반환  

**`request.getParameterValues(String name)`**  
`name`에 해당하는 파라미터의 모든 `value`를 반환, 다중선택이 가능한 `checkbox`, `select` `input`태그에 multi옵션이 달려있는 경우 같은 이름으로 여러값의 파라미터가 넘어오기 때문에 `getParameterValues`메서드를 사용한다.  
당연히 반환값은 `String[]`이다.  

**`request.getParameterNames()`**
클라이언트가 보낸 파라미터의 모든 `name`을 반환, 반환값은 `Enumeration<String>`이다.  
다음과 같이 출력할 수 있다.  
```js
<% 
	Enumeration<String> en = request.getParameterNames();
	while(en.hasMoreElements())
	{
		String pname = en.nextElement();
%>
	<li><%= pname %></li>
<% 
	}
%>
```

**`request.getParameterMap()`**
`name`과 `value`가 매칭되는 `Map<String,String[]>`컬렉션을 반환  
같은 name의 많은 value가 올 수 있음으로 두번째 `Element`는 `String[]`이다.  

다음과 같이 사용할 수 있다.  
```js
<% 
    Map<String,String[]> map = request.getParameterMap();
    Set<Entry<String, String[]>> set = map.entrySet();
    Iterator<Entry<String, String[]>> ir = set.iterator();
	while(ir.hasNext())
	{
		Entry<String, String[]> entry = ir.next();
%>
	<li><%= entry.getKey() %> - <%= String.join(", ", entry.getValue()) %></li>
<% 
	}
%>
```

파라미터 외에도 `request`객체는 클라이언트(브라우저)요청에 관한 여러 정보를 가져온다.    
```js
<%= request.getRemoteAddr() %>
<!-- 0:0:0:0:0:0:0:1 클라이언트의 IP주소를 가져온다. (ipv6)  -->

<%= request.getContentLength() %>
<!--요청한 정보의 길이를 반환한다, 
-1이 의미하는 것은 전송된 데이터의 길이를 알 수 없다는 뜻 -->

<%= request.getCharacterEncoding() %>
<!-- 요청한 정보의 인코딩 타입을 반환
null의 의미하는 것은 기본값, 설정 된것이 없다는 뜻 -->

<%= request.getContentType() %>
<!-- 요청한 정보의 컨텐츠 타입을 반환 -->

<%= request.getProtocol() %>
<!-- 요청한 정보의 인코딩 타입을 반환, HTTP/1.1 출력
http프로토콜에 1.1버전으로 요청-->

<%= request.getRequestURL() %>
<!-- 요청한 정보의 인코딩 타입을 반환, //jspPro/days02/ex04.jsp-->
<%= request.getRequestURI() %>
<!-- 요청한 정보의 인코딩 타입을 반환 -->

<!-- URI(Uniform Resource Identifier)는 존재하는 자원을 식별하기 위한 
일반적인 식별자를 규정하기 위한 것으로 URL에서 HTTP프로토콜,호스트명,port 번호를 제외한 것이다. -->

<%= request.getContextPath() %>
<!-- ContextPath 반환, /jspPro-->

<%= request.getServerName() %>
<!-- 연결할 때 사용한 서버이름(도메인)반환, localhost-->

<%= request.getServerPort() %>
<!-- 요청한 정보의 인코딩 타입을 반환, 80-->
```

http프로토콜의 헤더정보를 읽어올 수 있다.  
|**내장 객체**|**설명**|**리턴 타입**|
|---|---|---|
`getHeader()`|지정한 이름의 헤더값을 얻어옴 |`String`
`getHeaders()`|지정한 이름의 헤더 목록을 얻어옴|`Enumeration<String>`
`getHeaderNames()`|모든 헤더 이름을 얻어옴|`Enumeration<String>`
`getIntHeader()`|지정한 해더값을 정수로 얻어옴|`int`
`getDateHeader()`|지정한 헤더값을 시간값으로 얻어옴|`long`

```js
<% 
	Enumeration<String> en = request.getHeaderNames(); 
	en.toString();
	while(en.hasMoreElements())
	{
		String hname = en.nextElement();
		String hvalue = request.getHeader(hname);
%>
<li><%= hname %> - <%= hvalue %></li>
<%
	}
%>
```
출력값
```
host - localhost
connection - keep-alive
upgrade-insecure-requests - 1
user-agent - Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36
accept - text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3
accept-encoding - gzip, deflate, br
accept-language - ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
cookie - JSESSIONID=DBFB435E266CEEA73EB004BBF0F7F6D7; ID_save=admin
```
### response

`response`객체는 서버에서 클라이언트에게 응답할때 사용되는 객체이다.  

클라이언브(브라우저)에게 보내는 응답을 위한 객체
1. 헤더에 추가정보 입력
2. 리다이렉트(redirect) 기능

`response.sendRedirect("url")`
이렇게 매개변수로 url이 포함되는경우 한글이 들어갈때 인코딩을 해야한다.  

다행이 인코딩 지원 메서드가 있다.  
`URLEncoder.encode("한글", "utf-8");`

<br><br>


## request 디코딩, response 인코딩

클라이언트가 `input`태그에 데이터를 담아 서버로 `submit`하면 서버는 `request`객체를 사용해 클라이언트로부터 받은 데이터에 접근한다.  

그리고 `reponse`객체를 통해 클라이언트에게 html페이지를 제공한다.  

웹브라우저가 서버에게 파라미터를 보낼때 인코딩하면  
서버는 받은 파라미터를 디코딩해서 읽어온다.  

`request`로부터 데이터를 받을때 받은 파라미터를 디코딩하고,  
`response`로 데이터를 전달할 때 다시 인코딩하여 웹브라우저에게 html페이지를 전달한다.  

이 디코딩, 인코딩 과정에서 한글이 다음과 같이 깨질 수 있다.  

```html
<form action="ex05_ok.jsp" method="post">
	이름: <input type="text" name="name" autofocus="autofocus" value="홍길동"/><br><br>
	성별: <input type="radio" name="gender" value="m" checked="checked"/>남자
			<input type="radio" name="gender" value="w"/>여자<br><br>
	좋아하는 동물: 
	<input type="checkbox" name="pet" value="dog" checked="checked"/>개
	<input type="checkbox" name="pet" value="cat" checked="checked"/>고양이
	<input type="checkbox" name="pet" value="pig" />돼지<br><br>
	
	기타: <textarea name="etc" id="etc" cols="30" rows="10">기타사항 없음....</textarea><br>
	<input type="submit" />
	<input type="reset" />
</form>
```
`value`속성에 한글을 막 집어넣고 해당 데이터를 출력하는 jsp페이지를 요청해보자.  

```js

<h3>request.getParameterMap</h3>
<% 
    Map<String,String[]> map = request.getParameterMap();
    Set<Entry<String, String[]>> set = map.entrySet();
    Iterator<Entry<String, String[]>> ir = set.iterator();
	while(ir.hasNext())
	{
		Entry<String, String[]> entry = ir.next();
%>
	<li><%= entry.getKey() %> - <%= String.join(", ", entry.getValue()) %></li>
<% 
	}
%>
```

출력값
```
request.getParameterMap
name - íê¸¸ë
gender - m
pet - dog, cat
etc - ê¸°íì¬í­ ìì....
```
영어 `m`, `dog`, `cat`을 제외하곤 모두 깨졌다.  

원인은 다음과 같다.  

`post`방식에선 파라미터를 보낼 때 html문서와 똑같은 타입으로 파라미터를 인코딩하여 보낸다.  

그럼 `request.getParameterMap()`메서드를 통해 받은 파라미터를 디코딩하는데 여기서 문제가 생긴다.  

`request`객체의 기본 캐릭터 셋은 `ISO-8859-1`이다! UTF-8을 저런 서유럽 방식으로 디코딩 하니 한글이 깨질 수 밖에 없다!

따라서 `request.setCharacterEncoding("utf-8");`을 통해 캐릭터 셋을 `utf-8`로 바꾸어 주어야 한다.  


`get`방식도 파라미터를 보낼 때 html문서와 똑같은 타입으로 파라미터를 인코딩한다.  
하지만 파라미터를 `request.getParameterMap()`로 읽을 때에는 `tuf-8`로 읽어온다.  
~~아주 지멋대로다...~~  

`get`방식 파라미터는 `was`마다 파라미터를 읽을 때 사용하는 디코딩 방식이 다르다.  

우리가 사용하는 `tomcat8.5`의 경우 기본 `utf-8`방식을 사용하기 때문에 get방식을 사용할 때에는 `setCharacterEncoding`으로 디코딩 방식을 바꿀 필요가 없다(어차피 `utf-8`이니까!)  

또한 `setCharacterEncoding`방식으로 캐릭터 셋이 바뀌지도 않기 때문에 `get`방식에서 인코딩 방식을 바꾸려면 `apache-tomcat-8.5.39\conf\server.xml`파일에서 다음과 같이 속성을 추가해주어야 한다.  

```xml
<Connector port="80" protocol="HTTP/1.1"
	connectionTimeout="20000"
	redirectPort="8443" 
	useBodyEncodingForURI="true"/><!-- 추가된 속성 -->
```

속성을 추가해서애 `request.setCharacterEncoding()`메서드로 캐릭터 셋을 변경 가능하다.  

따라서 jsp파일의 경우 `html`태그 위에, servlet의 경우 `doPost()`, `doGet()`메서드 시작하자 마자 다음 코드를 추가해주는 것이 마음 편하다.  

```java
request.setCharacterEncoding("UTF-8"); 
//응답받은 파라미터를 UTF-8로 디코딩

response.setContentType("text/html; charset=UTF-8"); 
//응답할 html페이지를 UTF-8로 설정
```

## URL 패턴

서블릿 객체를 사용하기 위해 `web.xml`에서 다음과 같이 Servlet객체와 url을 매핑해주었다.  
```xml
<servlet>
	<servlet-name>now</servlet-name>
	<servlet-class>days01.NowServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>now</servlet-name>
	<url-pattern>/days01/now</url-pattern>
</servlet-mapping>
<servlet>
```
이 `<url-pattern>`태그 안에서 여러 방식으로 패턴을 지정할 수 있다.  


### 1. `/`로 시작해서 `/*`로 끝나는 경우  

ex: `<url-pattern> /foo/bar/* </url-pattern>`
`url`시작이 `contextPath`뒤에 `/foo/bar/`로 시작하면 매핑된 서블릿을 반환한다.   

### 2. `*.확장자` 형식을 사용하는 경우  

ex: `<url-pattern> *.do </url-pattern>`  
`.do`확장자로오는 url 요청이 들어오면 매핑된 서블릿을 반환한다.  

> 참고: `/foo/bar/*.do` 이런형식으로 사용하지 못한다.  
	
### 3. 오직 `/`하나 사용

ex: `<url-pattern> / </url-pattern>`
기본 서블릿을 매핑시킨다.
	
### 4. 그외 패턴

ex: `<url-pattern> /days02/Test </url-pattern>`
우리가 지금까지 사용했던 경로가 완변히 일치해야하는 매핑방식이다.

