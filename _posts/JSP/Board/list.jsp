<%@page import="days05.PageBlock"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- <%
	PageBlock pageBlock = (PageBlock)request.getAttribute("pageBlock");
%> --%>
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<meta charset="UTF-8">
<title>JSP / Servelet Class - kouzie(2019. 4. 30.-오전 11:10:17)</title>
<style type="text/css">
	table, thead th, td, tr{
		border: 1px solid gray;
	}
	tbody tr td:nth-child(2) {
		text-align: left;
	}
	a {
		text-decoration: none;
		color: black;
	}
</style>
<!-- 페이징 처리 style -->
<style>
	.pagination{
		margin: 0 auto;
		display: flex;
		justify-content: center;
	}
	.pagination a {
		color: black;
		float: left;
		padding: 4px 8px;
		text-decoration: none;
		transition: background-color .3s;
	}
	
	.pagination a.active {
		background-color: #ddd;
		color: dodgerblue;
	}
	
	.pagination a:hover:not(.active) {
		background-color: #ddd;
	}
	
	.searchWord {
		background-color: yellow;
		color: red;
	}
</style>
<script>
	$(document).ready(function (){
		var insert_st = "${param.insert}";
		if(insert_st == "success")
		{
			alert("Insert " + insert_st);
		}
		else if (insert_st == "fail") {
			alert("Insert " + insert_st);
		}
		
		var delete_st = "${param.delete}";
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
	<div align="center">
	<h2>목록보기</h2>
	<table style="border: solid 1px gray; width: 600px;">
		<thead>
			<tr>
				<th>번호</th>
				<th>제목</th>
				<th>작성자</th>
				<th>등록일</th>
				<th>조회</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty list }">
				<tr align="center">
					<td colspan="5">등록된 게시가 없습니다</td>
				</tr>
			</c:if>
			
			<c:if test="${not empty list }">
				<c:forEach items="${list }" var="dto">
					<tr align="center">
						<td>${dto.seq }</td>
						<td>
							<a href="content?seq=${dto.seq }" class="subjectLink">${dto.subject }</a>
						</td>
						<td>
							<a href="mailto:${dto.email }">${dto.name }</a>
						</td>
						<td>${dto.regDate }</td>
						<td>${dto.cnt }</td>
					</tr>	
				</c:forEach>
			</c:if>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="5" align="center">
					<div class="pagination">
						<%-- <% 
						if(pageBlock.prev){
						%>
							<a href="/jspPro/board/list?currentPage=<%= pageBlock.start - 1 %>">&laquo;</a>
						<% 
						}
						for(int i=pageBlock.start; i<=pageBlock.end; i++)
						{
							if(i == pageBlock.curPage)
							{
						%>
								<a class="active" href="#"><%= i %></a>
						<%
								
							}
							else
							{
						%>
								<a href="/jspPro/board/list?currentPage=<%= i %>"><%= i %></a>
						<%						
							}
						}				
						%>
						<% 
						if(pageBlock.next){
						%>
							<a href="/jspPro/board/list?currentPage=<%= pageBlock.end + 1 %>">&raquo;</a>
						<% 
						}
						%> --%>
						<c:if test="${pageBlock.prev }">
							<a href="/jspPro/board/list?currentPage=${pageBlock.start - 1 }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}">&laquo;</a>
						</c:if>
						
						<c:forEach begin="${pageBlock.start }" end="${pageBlock.end }" step="1" var="i">
						
							<c:if test="${i eq pageBlock.curPage }">
								<a class="active" href="#">${i }</a>
							</c:if>
							
							<c:if test="${i ne pageBlock.curPage }">
								<a href="/jspPro/board/list?currentPage=${i }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}">${i }</a>
							</c:if>
						</c:forEach>
						<c:if test="${pageBlock.next }">
							<a href="/jspPro/board/list?" class="subjectLink">&raquo;</a>
						</c:if>
					</div>
				</td>
			</tr>
			<tr>
				<td colspan="5" align="center">
					<form action="" method="get">
						<select name="searchCondition" id="searchCondition">
							<option value="1">제목</option>
							<option value="2">내용</option>
							<option value="3">글쓴이</option>
							<option value="4">제목+내용</option>
						</select>
						<input type="text" name="searchWord" id="searchWord" value="${param.searchWord}"/>
						<input type="submit" value="검색"/>
						<%-- <input type="hidden" name="currentPage" value="${param.currentPage }"/> --%>
					</form>
				</td>
			</tr>
			<tr>
				<td colspan="5" align="center">
					<a href="list" style="float: left;">홈</a>
					<a href="regist">글쓰기</a>
				</td>
			</tr>
		</tfoot>
	</table>
	</div>
	<script>
		$("#searchCondition").val("${ empty param.searchCondition ? 1 : param.searchCondition}");
		
		$(".subjectLink").attr( "href", function( i, val ) {
			return val + "&currentPage=${param.currentPage }&searchCondition=${param.searchCondition}&searchWord=${param.searchWord}";
		})
	</script>
</body>
</html>