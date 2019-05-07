<%@page import="com.util.Cookies"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 5. 7.-오후 2:46:07)</title>
<script>
	$(document).ready(function (){
		
	});
</script>
</head>
<body>
<%
String cname = request.getParameter("cname");
String cvalue = request.getParameter("cvalue");

Cookies cookies = new Cookies(request);
Cookie cookie = cookies.createCookie(cname, cvalue, "/", -1);

response.addCookie(cookie);
response.sendRedirect("ex08.jsp");
%>
</body>
</html>