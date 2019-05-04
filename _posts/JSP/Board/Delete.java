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

public class Delete extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Delete doGet() called....");
		String path = "/days05/delete.jsp";
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);
		dipatcher.forward(request, response);
		//forward로 돌림으로 url주소가 변경되지 않고 form태그에서 확인을 누르면 자기자신url을 다시 호출한다.
		//form태그에선 post방식으로 요청하기 때문에 아래의 doPost를 호출한다.
		//하나의 서블릿에서 출력을 위한 foward를 doGet에서 사용하고
		//입력을 위한 코드를 doPost에서 사용한다.  
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Delete doPost() called....");
		request.setCharacterEncoding("utf-8");
		
		int seq = Integer.parseInt(request.getParameter("seq"));
		String password = request.getParameter("password");
		
		System.out.println(seq);

		StringBuffer sql1 = new StringBuffer();
		sql1.append("SELECT password FROM tbl_board WHERE seq = ?");
		
		StringBuffer sql2 = new StringBuffer();
		sql2.append(" DELETE FROM tbl_board WHERE seq = ? ");
		
		String state = "fail";
		Connection conn = null;
		PreparedStatement pstmt;
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql1.toString());
			pstmt.setInt(1, seq);
			System.out.println("pstmt1실행 완료");
			ResultSet rs = null;
			rs = pstmt.executeQuery();
			rs.next();
			String o_password = rs.getString("password");
			rs.close();
			
			//? 5
			if(o_password.equals(password))
			{
				pstmt = conn.prepareStatement(sql2.toString());
				pstmt.setInt(1, seq);
				int result = 0;
				result = pstmt.executeUpdate();
				System.out.println("pstmt2실행 완료");
				if(result == 1)
				{
					state = "success";
					System.out.printf("Sucess Delete: %d", result);
				}
				else
				{
					state = "fail";
					System.out.println("Faild Delete");
				}
				pstmt.close();
				DBConn.close();
				String currentPage = request.getParameter("currentPage");
				String searchCondition = request.getParameter("searchCondition");
				String searchWord = request.getParameter("searchWord");
				
				String location = "/jspPro/board/list?delete="+state+"&currentPage="+currentPage+
						  "&searchCondition="+searchCondition+"&searchWord="+searchWord;
				//list서블리셍서 list.jsp로 접속하도록 설정하자.
				response.sendRedirect(location );
			}
			else
			{
				pstmt.close();
				DBConn.close();
				System.out.println(state);
				request.setAttribute("delete", state);
				doGet(request, response);
				return;
			}
			
		} catch (Exception e) {
			state = "fail";
			System.out.println("Faild Delete");
		}
	}
}