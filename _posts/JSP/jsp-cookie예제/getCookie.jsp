<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Set"%>
<%@page import="com.util.Cookies"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 5. 7.-오후 2:43:43)</title>
<script>
	$(document).ready(function (){
		
	});
</script>
</head>
<body>
<form action="" id="form1">
	<% 
	Cookies cookies = new Cookies(request);
	Set<Entry<String, Cookie>> set = cookies.getCookieMap().entrySet();
	Iterator<Entry<String, Cookie>> ir = set.iterator();
	while(ir.hasNext())
	{
		Entry<String, Cookie> entry = ir.next();
		String cname = entry.getKey();
		String cvalue = entry.getValue().getValue();
	%>
		<input type="checkbox" name="ckb_cookie" value="<%= cname %>"/><%= cname %> = <%= cvalue %><br>
	<%
	}
	%>
</form>
<a href="ex08.jsp">home</a>
<a href="ex08_editCookie.jsp" id="editLink">쿠키수정</a>
<a href="ex08_deleteCookie.jsp" id="deleteLink">쿠키삭제</a>
<script type="text/javascript">
	$("#deleteLink, #editLink").click(function(event) {
		event.preventDefault(); //링크 기능 제거
		//$("#form1").attr("action", $(this).attr("href")).submit();
		/* 
		위처럼 해도 되지만 아래 함수를 사용하면 더 간편하다.
		$("#form1").serialize()
			뒤에 달고갈 파라미터를 자동으로 문자열 형식으로 출력한다. 
		*/
		
		var queryStr = $("#form1").serialize();
		location.href = $(this).attr("href")+"?"+queryStr;
	});
	
</script>
</body>
</html>