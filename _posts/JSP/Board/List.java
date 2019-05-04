package days05;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.DBConn;

public class List extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("List doGet() called....");
		String curPage_param = request.getParameter("currentPage");
		
		String searchCondition = request.getParameter("searchCondition");
		System.out.printf("searchCondition: %s\n", searchCondition);
		if(searchCondition == null || searchCondition.isEmpty())
			searchCondition = "1";

		System.out.printf("searchCondition: %s\n", searchCondition);
		
		String searchWord = request.getParameter("searchWord");
		if(searchWord == null || searchWord.isEmpty())
			searchWord = "*";
		
		int curPage;
		if(curPage_param == null || curPage_param.isEmpty())
			curPage = 1;
		else
			curPage = Integer.parseInt(curPage_param);
		System.out.println(curPage_param);
		
		int pageSize = 15; //뿌릴 게시글 개수
		int numberOfBlock = 10; //표시할 페이지 수

		int start = (curPage - 1)* pageSize + 1 ;
		int end = curPage * pageSize;

		StringBuffer sql = new StringBuffer();
		sql.append(" WITH temp AS( ");
		sql.append(" SELECT ROWNUM AS no, temp.* ");
		sql.append(" FROM ");
		sql.append(" ( ");
		sql.append("    SELECT seq, name, email, subject, cnt, regdate ");
		sql.append("    FROM tbl_board ");
		System.out.printf("searchCondition: %s\n", searchCondition);
		switch (Integer.parseInt(searchCondition)) {
		case 1: //제목
			sql.append("    WHERE REGEXP_LIKE(subject, ?, 'i') ");
			break;
		case 2: //내용
			sql.append("    WHERE REGEXP_LIKE(content, ?, 'i') ");
			break;
		case 3: //글쓴이
			sql.append("    WHERE REGEXP_LIKE(name, ?, 'i') ");
			break;
		case 4: //제목+내용
			sql.append("    WHERE REGEXP_LIKE(subject, ?, 'i') OR REGEXP_LIKE(content, ?, 'i') ");
			break;

		default:
			break;
		}
		sql.append("    ORDER BY seq desc ");
		sql.append("    )temp ");
		sql.append(" ) ");
		sql.append(" SELECT temp.* FROM temp ");
		sql.append(" WHERE temp.no BETWEEN ? AND ? ");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<MyBoardDTO> blist = null;

		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());


			pstmt.setString(1,  searchWord);

			if(Integer.parseInt(searchCondition) == 4)
			{
				//참고로 ?에 %를 같이쓰고싶다면 문자열 자체에 %를 붙여야된다. 
				//sql자체에 %와 ?같이쓸 수는 없음.
				pstmt.setString(2,  searchWord);
				pstmt.setInt(3,  start);
				pstmt.setInt(4,  end);
			}
			else
			{
				pstmt.setInt(2,  start);
				pstmt.setInt(3,  end);
			}
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				MyBoardDTO mdto = null;
				blist = new ArrayList<>();
				do {
					mdto = new MyBoardDTO();
					mdto.setNo(rs.getInt("no"));
					mdto.setSeq(rs.getInt("seq"));
					mdto.setName(rs.getString("name"));
					mdto.setEmail(rs.getString("email"));
					String subject = rs.getString("subject");
					if(!searchWord.equals("*"))
						subject = subject.replaceAll("(?i)"+searchWord, "<span class='searchWord'>"+searchWord+"</span>");
					mdto.setSubject(subject );
					mdto.setCnt(rs.getInt("cnt"));
					mdto.setRegDate(rs.getDate("regdate"));

					blist.add(mdto);
				} while (rs.next());
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			System.out.println(e);
		}


		PageBlock pageBlock = new PageBlock();
		pageBlock.setCurPage(curPage);
		pageBlock.setNumberPerPage(pageSize);
		pageBlock.setNumberOfBlock(numberOfBlock); //페이지 블록 10개씩 출력
		pageBlock.getSearchNumberOfPages(pageSize, Integer.parseInt(searchCondition), searchWord);

		int pageBlockStart = (curPage-1)/numberOfBlock * numberOfBlock + 1;
		int pageBlockEnd = (curPage-1)/numberOfBlock * numberOfBlock + numberOfBlock;

		if(pageBlockEnd > pageBlock.getNumberOfBlocks())
			pageBlockEnd = pageBlock.getNumberOfBlocks();

		pageBlock.setStart(pageBlockStart);
		pageBlock.setEnd(pageBlockEnd);

		pageBlock.prev = pageBlock.getStart() == 1 ? false : true; //start가 1이면 << 보일 필요 없음
		pageBlock.next = pageBlock.getEnd() == pageBlock.getNumberOfBlocks() ? false : true; //end가 pageBlock.numberOfBlock이면 >> 보일 필요 없음

		System.out.printf("curpage:%d\n",curPage);
		System.out.printf("start: %d\n",pageBlock.getStart());
		System.out.printf("end: %d\n",pageBlock.getEnd());
		System.out.printf("numberOfBlocks: %d\n",pageBlock.getNumberOfBlocks());

		String path = "/days05/list.jsp";
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);

		request.setAttribute("pageBlock", pageBlock);
		request.setAttribute("list", blist);
		dipatcher.forward(request, response);
		//list를 띄우기 위한 서블릿, jsp로 포워드시킨다.
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("List doPost() called....");

		int searchCondition = Integer.parseInt(request.getParameter("searchCondition"));
		String searchWord = request.getParameter("searchWord");

		StringBuffer sql = new StringBuffer();
		sql.append(" WITH temp AS( ");
		sql.append(" SELECT ROWNUM AS no, temp.* ");
		sql.append(" FROM ");
		sql.append(" ( ");
		sql.append("    SELECT seq, name, email, subject, cnt, regdate ");
		sql.append("    FROM tbl_board ");

		switch (searchCondition) {
		case 1: //재목
			sql.append("    WHERE REGEXP_LIKE(subject, ?, 'i') ");
			break;
		case 2: //내용
			sql.append("    WHERE REGEXP_LIKE(content, ?, 'i') ");
			break;
		case 3: //글쓴이
			sql.append("    WHERE REGEXP_LIKE(name, ?, 'i') ");
			break;
		case 4: //제목+내용
			sql.append("    WHERE REGEXP_LIKE(subject, ?, 'i') OR REGEXP_LIKE(content, ?, 'i') ");
			break;

		default:
			break;
		}
		sql.append("    ORDER BY seq desc ");
		sql.append("    )temp ");
		sql.append(" ) ");
		sql.append(" SELECT temp.* FROM temp ");
		sql.append(" WHERE temp.no BETWEEN ? AND ? ");

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<MyBoardDTO> blist = null;

		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			//			int start = (curPage - 1)* pageSize + 1 ;
			//			int end = curPage * pageSize;
			int start = 1;
			int end = 10;
			pstmt.setString(1,  searchWord);

			if(searchCondition == 4)
			{
				//참고로 ?에 %를 같이쓰고싶다면 문자열 자체에 %를 붙여야된다. 
				//sql자체에 %와 ?같이쓸 수는 없음.
				pstmt.setString(2,  searchWord);
				pstmt.setInt(3,  start);
				pstmt.setInt(4,  end);
			}
			else
			{
				pstmt.setInt(2,  start);
				pstmt.setInt(3,  end);
			}
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				MyBoardDTO mdto = null;
				blist = new ArrayList<>();
				do {
					mdto = new MyBoardDTO();
					mdto.setNo(rs.getInt("no"));
					mdto.setSeq(rs.getInt("seq"));
					mdto.setName(rs.getString("name"));
					mdto.setEmail(rs.getString("email"));
					mdto.setSubject(rs.getString("subject"));
					mdto.setCnt(rs.getInt("cnt"));
					mdto.setRegDate(rs.getDate("regdate"));

					blist.add(mdto);
				} while (rs.next());
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		request.setAttribute("list", blist);
		String path = "/days05/list.jsp";
		System.out.println(blist.toString());
		RequestDispatcher dipatcher = request.getRequestDispatcher(path);
		dipatcher.forward(request, response);
	}
}