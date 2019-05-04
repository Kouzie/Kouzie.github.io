<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 4. 30.-오전 9:20:12)</title>
<style>
	table {
		width: 600px;
	}
	table, th, td {
		border: 1px solid gray;
	}

</style>
<script>
	$(document).ready(function (){
		var udate_st = "${update}";
		if(udate_st == "success")
		{
			alert("Update " + udate_st);
		}
		else if (udate_st == "fail") {
			alert("Update " + udate_st);
		}
	});
</script>
</head>
<body>
<form name="editForm" method="post" action="">
	<input type="hidden" name="seq" value="${dto.seq }" />
	<table>
		<tr>
			<td colspan="2" align="center"><b>글을 수정합니다</b></td>
		</tr>
		<tr>
			<td align="center">이름</td>
			<td>
				${dto.name }
			</td>
		</tr>
		<tr>
			<td align="center">Email</td>
			<td>
				${dto.email }
			</td>
		</tr>
		<tr>
			<td align="center">제목</td>
			<td>
				<input type="text" name="subject" size="50" value="${dto.subject }">
			</td>
		</tr>
		<tr>
			<td align="center">내용</td>
			<td>
				<textarea name="content" cols="50" rows="10">${dto.content }</textarea>
			</td>
		</tr>
		<tr>
			<td align="center">HTML</td>
			<td>
				<input type="radio" name="tag" value="T" >적용
				<input type="radio" name="tag" value="F" >비적용
				<script>
					$(":radio[value='${dto.tag}']").prop("checked", true);
				</script>
			</td>
		</tr>
		 <tr>
			<td align="center">비밀번호</td>
			<td>
				<input type="password" name="password" size="15">
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="submit" value="작성 완료">&nbsp;&nbsp;
				<input type="button" onClick="location.href = '/jspPro/board/content?seq=${param.seq }&currentPage=${param.currentPage }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}'" value="이전으로">
			</td>
		</tr>
	</table>
</form>
<script>
	$( ".subjectLink").attr( "href", function( i, val ) {
		return val + "&currentPage=${param.currentPage }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}";
	})
</script>
</body>
</html>