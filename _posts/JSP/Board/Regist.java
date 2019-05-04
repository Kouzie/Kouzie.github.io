package days05;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.DBConn;

public class Regist extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Regist doGet() called....");
//		String location = "/jspPro/days05/regist.jsp";
		String path = "/days05/regist.jsp";
		//response.sendRedirect(location);
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);
		dipatcher.forward(request, response);
		//forward로 돌림으로 url주소가 변경되지 않고 form태그에서 확인을 누르면 자기자신url을 다시 호출한다.
		//form태그에선 post방식으로 요청하기 때문에 아래의 doPost를 호출한다.
		//하나의 서블릿에서 출력을 위한 foward를 doGet에서 사용하고
		//입력을 위한 코드를 doPost에서 사용한다.  
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Regist doPost() called....");
		request.setCharacterEncoding("utf-8");
		
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		String subject = request.getParameter("subject");
		String content = request.getParameter("content");
		String tag = request.getParameter("tag");
		System.out.println(name);
		System.out.println(password);
		System.out.println(email);
		System.out.println(subject);
		System.out.println(content);
		System.out.println(tag);
		
		MyBoardDTO bdto = new MyBoardDTO();
		bdto.setName(name);
		bdto.setPassword(password);
		bdto.setEmail(email);
		bdto.setSubject(subject);
		bdto.setContent(content);
		bdto.setTag(tag);
		
		StringBuffer sql = new StringBuffer();
		sql.append(" INSERT INTO tbl_board ");
		sql.append(" (seq, name, password, email, subject, content, tag, userip) ");
		sql.append(" VALUES(seq_myboard.nextval, ?, ?, ?, ?, ?, ?, '127.0.0.1') ");
		
		String state = "";
		Connection conn = null;
		PreparedStatement pstmt;
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			//? 5
			pstmt.setString(1, bdto.getName());
			pstmt.setString(2, bdto.getPassword());
			pstmt.setString(3, bdto.getEmail());
			pstmt.setString(4, bdto.getSubject());
			pstmt.setString(5, bdto.getContent());
			System.out.println(bdto.getTag().equals("T")?"y":"n");
			pstmt.setString(6, bdto.getTag().equals("T")?"y":"n");
			System.out.println("pstmt실행 완료");
			int result = 0;
			result = pstmt.executeUpdate();
			if(result == 1)
			{
				state = "success";
				System.out.printf("Sucess Insert: %d", result);
			}
			else
			{
				state = "fail";
				System.out.printf("Faild Insert");
			}

			pstmt.close();
			DBConn.close();
		} catch (Exception e) {
			state = "Insert fail";
			System.out.printf("Faild Insert");
		}
		
		String location = "/jspPro/board/list?insert="+state;
		//list서블리셍서 list.jsp로 접속하도록 설정하자.
		response.sendRedirect(location );
		
	}
}