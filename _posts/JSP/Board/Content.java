package days05;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.DBConn;

public class Content extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Content doGet() called....");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		MyBoardDTO mdto  = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT seq, name, email, subject, content, cnt, regdate, tag ");
		sql.append(" FROM tbl_board ");
		sql.append(" WHERE seq = ? ");

		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			int seq = Integer.parseInt(request.getParameter("seq"));
			pstmt.setInt(1, seq );
			increaseCnt(seq); //조회수 증가
			rs = pstmt.executeQuery();

			if(rs.next())
			{
				mdto = new MyBoardDTO();
				mdto.setSeq(rs.getInt("seq"));
				mdto.setName(rs.getString("name"));
				mdto.setEmail(rs.getString("email"));
				mdto.setSubject(rs.getString("subject"));
				mdto.setTag(rs.getString("tag"));
				String content = rs.getString("content").replace("\r\n", "<br>");
				if(mdto.getTag().equals("n"))
				{
					content = content.replaceAll("<", "&lt;");
					content = content.replaceAll(">", "&gt;");
				}
				mdto.setContent(content);
				mdto.setRegDate(rs.getDate("regdate"));
				mdto.setCnt(rs.getInt("cnt"));
			}
			rs.close();
			pstmt.close();

		} catch (Exception e) {
			System.out.println(e);
		}

		String path = "/days05/content.jsp";
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);

		request.setAttribute("dto", mdto);
		dipatcher.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public int increaseCnt(int seq) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE tbl_board ");
		sql.append(" SET cnt = cnt+1 ");
		sql.append(" WHERE seq = ? ");
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, seq);
			int resultCnt = pstmt.executeUpdate();
			pstmt.close();
			return resultCnt;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return seq;
	}
}