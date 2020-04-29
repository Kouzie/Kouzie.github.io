<%@page import="com.util.Cookies"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 5. 7.-오후 3:51:29)</title>
<script>
	$(document).ready(function (){
		
	});
</script>
</head>
<body>
<%
System.out.println(request.getParameterValues("ckb_cookie") == null);
if(request.getParameterValues("ckb_cookie") == null)
{
	response.sendRedirect("ex08.jsp");
	return;
}
%>
	
<form action="ex08_editCookie_ok.jsp">
<%
String[] cname = request.getParameterValues("ckb_cookie");
Cookies cookies = new Cookies(request);
for(int i=0; i<cname.length; i++)
{
	String value = cookies.getValue(cname[i]);
%>
	<%= cname[i] %>: <input type="text" name="<%= cname[i] %>" value="<%= value %>"/><br>
<%
}
%>
<input type="submit" />
</form>
</body>
</html>