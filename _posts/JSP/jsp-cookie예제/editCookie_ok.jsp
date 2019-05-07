<%@page import="com.util.Cookies"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Map"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 5. 7.-오후 4:09:16)</title>
<script>
	$(document).ready(function (){
		
	});
</script>
</head>
<body>
<%
Map<String, String[]> map = request.getParameterMap();
Set<String> set = map.keySet();
Iterator<String> ir = set.iterator();

while(ir.hasNext())
{
	String cname = ir.next();
	String cvalue = map.get(cname)[0];
	System.out.println(cvalue);
	Cookie cookie = Cookies.createCookie(cname, cvalue);
	cookie.setPath("/");
	response.addCookie(cookie);
}

//response.sendRedirect("ex08.jsp");
%>
<script type="text/javascript">
alert("쿠키 수정 완료");
location.href = "ex08.jsp";
</script>
</body>
</html>