package days05;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.util.DBConn;

public class PageBlock {
	private int curPage = 1;
	private int numberOfBlock = 10; //페이지 수
	private int numberOfBlocks = 10; //총 수
	private int numberPerPage = 15; //출력할 게시글 수
	private int start = 1; //시작 페이지 블록 값
	private int end = start + numberOfBlocks; //끝 페이지 블록 값

	public boolean prev, next; //이전, 다음버튼

	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public int getNumberOfBlock() {
		return numberOfBlock;
	}

	public void setNumberOfBlock(int numberOfBlock) {
		this.numberOfBlock = numberOfBlock;
	}

	public int getNumberOfBlocks() {
		return numberOfBlocks;
	}

	public void setNumberOfBlocks(int numberOfBlocks) {
		this.numberOfBlocks = numberOfBlocks;
	}

	public int getNumberPerPage() {
		return numberPerPage;
	}

	public void setNumberPerPage(int numberPerPage) {
		this.numberPerPage = numberPerPage;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public boolean isPrev() {
		return prev;
	}

	public void setPrev(boolean prev) {
		this.prev = prev;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}

	public void getNumberOfPages() {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT CEIL(COUNT(*) / ?) numberOfBlocks FROM tbl_board ");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1,  numberPerPage);
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				numberOfBlocks = rs.getInt("numberOfBlocks");
				System.out.printf("numberOfBlocks:%d\n", numberOfBlocks);
			}
			rs.close();
			pstmt.close();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	public void getSearchNumberOfPages(int pageSize, int searchCondition, String searchWord)  {
		StringBuffer sql = new StringBuffer();
		int numberOfPages = 0;
		sql.append(" SELECT CEIL(COUNT(*) / ?) numberOfBlocks FROM tbl_board ");
		switch (searchCondition) {
		case 1: //제목
			sql.append(" WHERE REGEXP_LIKE(subject, ?, 'i') ");
			break;
		case 2: //내용
			sql.append(" WHERE REGEXP_LIKE(content, ?, 'i') ");
			break;
		case 3: //글쓴이
			sql.append(" WHERE REGEXP_LIKE(name, ?, 'i') ");
			break;
		case 4: //제목+내용
			sql.append(" WHERE REGEXP_LIKE(subject, ?, 'i') OR REGEXP_LIKE(content, ?, 'i') ");
			break;

		default:
			break;
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBConn.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, pageSize);
			pstmt.setString(2,  searchWord);

			if(searchCondition == 4)
			{
				pstmt.setString(3,  searchWord);
			}
			rs = pstmt.executeQuery();
			if(rs.next())
			{
				numberOfBlocks = rs.getInt("numberOfBlocks");
			}
			rs.close();
			pstmt.close();
		}catch (Exception e) {
			System.out.println(e);
		}
	}
}
