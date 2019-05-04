package days05;

import java.sql.Date;

public class MyBoardDTO{
	private int no;
	private int seq;
	private String name;
	private String password;
	private String content;
	private String subject;
	private String email;
	private int cnt;
	private Date regDate;
	private String tag;
	@Override
	public String toString() {
		
		return String.format("%2d  %4d %20s %8s %15s  %tF  %d %s", no, seq, subject, name, email, regDate, cnt, tag);
	}
	public int getSeq() {
		return seq;
	}
	public Date getRegDate() {
		return regDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getCnt() {
		return cnt;
	}
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setSeq(int seq) {
		this.seq = seq;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	public String getSubject() {
		return subject;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
}
