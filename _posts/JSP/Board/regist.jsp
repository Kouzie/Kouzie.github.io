<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 4. 30.-오전 9:20:12)</title>
<script>
   $(document).ready(function (){
   
   });
</script>
</head>
<body>
<!-- 액션 속성이 없으면 자기자신 url을 다시 요청한다. -->
<form name="registForm" method= "post" action="">
<table style="border: solid 1px gray; padding:2px; width:500px" >
	<tr>
		<td colspan="2" align="center"><b>글을 적어주세요</b></td>
	</tr>
	<tr>
		<td align="center">이름</td>
		<td><input type="text" name="name" size="15"></td>
	</tr>
	<tr>
		<td align="center">비밀번호</td>
		<td><input type="password" name="password" size="15"></td>
	</tr>
	<tr>
		<td align="center">Email</td>
		<td><input type="text" name="email" size="50"></td>
	</tr>
	<tr>
		<td align="center">제목</td>
		<td><input type="text" name="subject" size="50"></td>
	</tr>
	<tr>
		<td align="center">내용</td>
		<td><textarea name="content" cols="50" rows="10"></textarea></td>
	</tr>
	<tr>
		<td align="center">HTML</td>
		<td>
			<input type="radio" name="tag" value="T" checked>적용
			<input type="radio" name="tag" value= "F">비적용
		</td>
	</tr>
	<tr>
		<td colspan="2" align="center">
			<input type="submit" value="작성 완료">&nbsp;&nbsp;&nbsp;
			<input type="reset" value="다시 작성">
		</td>
	</tr>
</table>
</form>
</body>
</html>