<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 4. 30.-오후 2:23:28)</title>
<style>
	table, td, th{
		border: solid 1px gray;
	}
	table {
		width: 300px;
		margin: 0 auto;
	}
</style>
<script>
	$(document).ready(function (){
		console.log("")
		var delete_st = "${delete}"
		if(delete_st == "success")
		{
			alert("Delete " + delete_st);
		}
		else if (delete_st == "fail") {
			alert("Delete " + delete_st);
		}
		
	});
</script>
</head>
<body>
<form name="deleteForm" method="post" action=""> <!-- Delete.java의 doPost전달 -->
	<input type="hidden" name="seq" value="${param.seq }"/>
	<table>
		<tr>
			<td colspan="2" align="center"><b>글을 삭제합니다</b></td>
		</tr>
		<tr>
			<td align="center">비밀번호</td>
			<td>
				<input type="password" name="password" size="15" autofocus="autofocus">
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="submit" value="삭제">&nbsp;&nbsp;
				<input type="button" onClick="location.href = '/jspPro/board/content?seq=${param.seq }&currentPage=${param.currentPage }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}'" value="취소">
			</td>
		</tr>
	</table>
</form>

</body>
</html>