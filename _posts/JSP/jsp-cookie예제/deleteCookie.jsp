<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="com.util.Cookies"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 5. 7.-오후 3:17:40)</title>
<script>
	$(document).ready(function (){
		
	});
</script>
</head>
<body>

<%
if(request.getParameterValues("ckb_cookie") == null)
{
	response.sendRedirect("ex08.jsp");
	return;
}
String[] del_cname = request.getParameterValues("ckb_cookie");
Cookies cookies = new Cookies(request);
Set<Entry<String, Cookie>> set = cookies.getCookieMap().entrySet();

for(int i=0; i<del_cname.length; i++)
{
	if(cookies.exists(del_cname[i]))
	{
		System.out.println(del_cname[i]);
		Cookie cookie = new Cookie(del_cname[i], "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
%>
<script>
alert("쿠키 삭제 완료");
location.href = "ex08.jsp";
</script>
<%
//response.sendRedirect("ex08.jsp?del_cname="+String.join("&", del_cname));
%>
</body>
</html>