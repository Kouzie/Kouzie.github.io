package days05;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.DBConn;

public class Edit extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Edit doGet() called....");
		
		request.setCharacterEncoding("utf-8");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MyBoardDTO mdto  = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT seq, name, email, subject, content, cnt, tag, regdate ");
		sql.append(" FROM tbl_board ");
		sql.append(" WHERE seq = ? ");

		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			int seq = Integer.parseInt(request.getParameter("seq"));
			pstmt.setInt(1, seq );
			rs = pstmt.executeQuery();

			if(rs.next())
			{
				mdto = new MyBoardDTO();
				mdto.setSeq(rs.getInt("seq"));
				mdto.setName(rs.getString("name"));
				mdto.setEmail(rs.getString("email"));
				mdto.setSubject(rs.getString("subject"));
				mdto.setContent(rs.getString("content"));
				mdto.setRegDate(rs.getDate("regdate"));
				mdto.setTag(rs.getString("tag").equals("y") ? "T" : "F");
				mdto.setCnt(rs.getInt("cnt"));
			}
			rs.close();
			pstmt.close();
			
		} catch (Exception e) {
			System.out.println(e);
		}

		String path = "/days05/edit.jsp";
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);
		System.out.println(request.getAttribute("update"));
		request.setAttribute("dto", mdto);
		dipatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Edit doPost() called....");
		request.setCharacterEncoding("utf-8");
		String password = request.getParameter("password");
		
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");
		
		String tag = request.getParameter("tag");
		
		int seq = Integer.parseInt(request.getParameter("seq"));
		
		StringBuffer sql1 = new StringBuffer();
		sql1.append("SELECT password FROM tbl_board WHERE seq = ?");
		
		
		StringBuffer sql2 = new StringBuffer();
		sql2.append(" UPDATE tbl_board ");
		sql2.append(" SET subject = ?, content = ?, tag = ?");
		sql2.append(" WHERE seq = ? ");
		String state = "fail";
		Connection conn = null;
		PreparedStatement pstmt;
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql1.toString());
			pstmt.setInt(1, seq);
			ResultSet rs = null;
			rs = pstmt.executeQuery();
			rs.next();
			String o_password = rs.getString("password");
			rs.close();
			//? 5
			if(o_password.equals(password))
			{
				pstmt = conn.prepareStatement(sql2.toString());
				System.out.println(subject);
				System.out.println(content);
				System.out.println(tag);
				System.out.println(seq);
				pstmt.setString(1, subject);
				pstmt.setString(2, content);
				pstmt.setString(3, tag.equals("T") ? "y" : "n");
				pstmt.setInt(4, seq);
				int result = 0;
				result = pstmt.executeUpdate();
				if(result == 1)
				{
					state = "success";
					System.out.printf("Sucess Update: %d\n", result);
				}
				else
				{
					state = "fail";
					System.out.println("Faild Update");
				}
			}
			else //비밀번호 틀릴경우
			{
				System.out.println("비밀번호가 일치하지 않음");
				pstmt.close();
				DBConn.close();
				System.out.println(state);
				request.setAttribute("update", state);
				doGet(request, response);
				return;
			}
			pstmt.close();
			DBConn.close();
			
		} catch (Exception e) {
			System.out.println(e);
			state = "fail";
			System.out.println("Faild Update");
		}

		String currentPage = request.getParameter("currentPage");
		String searchCondition = request.getParameter("searchCondition");
		String searchWord = request.getParameter("searchWord");
		String location = "/jspPro/board/content?seq="+ seq +
				"&update="+state+"&currentPage="+currentPage+
				"&searchCondition="+searchCondition+"&searchWord="+searchWord;
		//list서블리셍서 list.jsp로 접속하도록 설정하자.
		response.sendRedirect(location );
		
	}
}