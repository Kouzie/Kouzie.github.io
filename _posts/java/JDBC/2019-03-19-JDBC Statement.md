---
title:  "Java - JDBC statement!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - JDBC

---

## java에서 sql문 실행하기

java에서 sql명령 처리하는 클래스(**인터페이스**)는 3가지다.

1. `Statement`  
2. `PreparedStatement`  
3. `CallableStatement`  

**`PreparedStatement` 와 `Statement` 의 가장 큰 차이점은 캐시(cache) 사용여부이다.**

1. 쿼리 문장 분석  
2. 컴파일  
3. 실행  

`Statement` 를 사용하면 매번 쿼리를 수행할 때마다 1 ~ 3 단계를 거치게 되고,  
`PreparedStatement` 는 처음 한 번만 1 ~ 3 단계를 거친 후 캐시(DB서버 메모리)에 실행법을 저장하고 **재사용을 한다는 것이다.**  
만약 동일한 쿼리를 반복적으로 수행한다면 `PreparedStatment`가 DB에 훨씬 적은 부하를 주며, 성능도 좋다.  

`CallableStatement` 은 프로시저(plsql)를 사용할때 사용하는 클레스이다.  
`CallableStatement` 는 DB서버에 이미 올라가있는 sql혹은 plsql을 호출하는 클래스이다.  

### Statement 로 SELECT

`Statement` 인터페이스를 사용해서 `SELECT` 를 실행하고 값을 가져와 보자.  

`Statement` 객체는 `Connection` 의 `createStatement()` 메서드를 통해서 가져올 수 있다.  
쿼리의 실행은 `Statement`클래스의 `executeQuery()` 함수.  

```java
Statement stmt = conn.createStatement(); //일꾼역할을 하는 객체(Statement)를 가져오는 함수
stmt.executeQuery(sql); //sql문을 실행시키는 함수
```

`executeQuery`의 반환형이 `ResultSet`이다.
쿼리를 수행한 결과집합을 `ResultSet`에 담아서 반환한다.

```java
ResultSet rs = stmt.executeQuery(sql);
while (rs.next()) {
    empno = rs.getInt(1);
    System.out.println(empno);
}
rs.close();
stmt.close();
```

- `rs.next`: 다음 레코드가 있으면 `true` 없으면 `false`를 반환하는 함수.  
- `rs.getInt`: n번째 칼럼의 값을 가져오는 함수, 1부터 시작.  

만약 `getInt` 에 없는 숫자의 칼럼을 넣으면 다음과 같은 에러 발생 한다.  

```
Exception in thread "main" java.sql.SQLException: 부적합한 열 인덱스
at oracle.jdbc.driver.OracleResultSetImpl.getInt(OracleResultSetImpl.java:908)
at days02.Ex02.main(Ex02.java:32)
```

마지막에 `close` 하는게 중요하다.  
한번에 `ResultSet` 으로 가져오는게 아니기 때문에 항상 썼으면 `close` 작업을 해주어야 한다.  

칼럼 번호 말고 칼럼 라벨명으로도 가져올 수 있다.  
`Integer`, `Sting`, `Date` 여러 원시타입으로 파싱 가능하다.  

```java
while (rs.next()) {
    empno = rs.getInt(1);
    ename = rs.getString("ename");
    job = rs.getString("job");
    mgr = rs.getInt("mgr");
    hiredate = rs.getDate("hiredate");
    sal = rs.getInt("sal");
    comm = rs.getInt("comm");
    deptno = rs.getInt("deptno");
    System.out.printf("%10d  %10s  %10s  %10d  %10s  %10d  %10d  %10d \n",empno, ename, job, mgr, hiredate, sal, comm, deptno);
}
/* 
7369       SMITH       CLERK        7902  1980-12-17         800           0          20         800
7499       ALLEN    SALESMAN        7698  1981-02-20        1600         300          30        1900
7521        WARD    SALESMAN        7698  1981-02-22        1250         500          30        1750
7566       JONES     MANAGER        7839  1981-04-02        2975           0          20        2975
7654      MARTIN    SALESMAN        7698  1981-09-28        1250        1400          30        2650
7698       BLAKE     MANAGER        7839  1981-05-01        2850           0          30        2850
7782       CLARK     MANAGER        7839  1981-06-09        2450           0          10        2450
7839        KING   PRESIDENT           0  1981-11-17        5000           0          10        5000
7844      TURNER    SALESMAN        7698  1981-09-08        1500           0          30        1500
7900       JAMES       CLERK        7698  1981-12-03         950           0          30         950
7902        FORD     ANALYST        7566  1981-12-03        3000           0          20        3000
7934      MILLER      ARTIST        7782  1982-01-23        1300           0          10        1300
*/
```

SQL 작성시 `StringBuffer` 사용을, 의무적으로 sql문 앞뒤에 공백을 붙여주는것을 추천  

```java
StringBuffer sb = new StringBuffer();
sb.append(" SELECT * FROM emp ");
sb.append(" JOIN dept ON emp.deptno = dept.deptno ");
ResultSet rs = stmt.executeQuery(sb.toString());
int empno;
String ename;
String job;
String dname;
while (rs.next()) {
    empno = rs.getInt(1);
    ename = rs.getString("ename");
    job = rs.getString("job");
    dname = rs.getString("dname");
    System.out.printf("%10d  %10s  %10s  %10s\n",empno, ename, job, dname);
}
/* 
7369       SMITH       CLERK    RESEARCH
7499       ALLEN    SALESMAN       SALES
7521        WARD    SALESMAN       SALES
7566       JONES     MANAGER    RESEARCH
7654      MARTIN    SALESMAN       SALES
7698       BLAKE     MANAGER       SALES
7782       CLARK     MANAGER  ACCOUNTING
7839        KING   PRESIDENT  ACCOUNTING
7844      TURNER    SALESMAN       SALES
7900       JAMES       CLERK       SALES
7902        FORD     ANALYST    RESEARCH
7934      MILLER      ARTIST  ACCOUNTING 
*/
```

### Statement 로 CRUD

다음과 같은 `tbl_dept` 테이블이 있을 때 CRUD 과정을 `Statement` 와 sql 로 진행  

```sql
SELECT * FROM tbl_dept
/* 
10    영업부    서울시
20    영업부    서울
30    영업부    서울
40    영업부    서울 
*/
```

`SELECT` 는 `executeQuery`를 사용하고  
`INSERT`, `DELETE`, `UPDATE` 같은 DML 은 `executeUPdate`를 사용한다.  
  
```
int executeUpdate(String sql) throws SQLException
```

`executeUpdate` 반환형은 int인데 영향받은 레코드의 수를 반한한다.  
또한 `Staetment` 로 실행된 DML문은 자동 커밋된다.  

> 못하게 하는 방법도 있음.  

```java
enum SelectMenu{
    DUMMY, 메뉴출력, 부서정보, 부서추가, 부서수정, 부서삭제, 부서검색, 종료;
};
public class PracticeJDBC {
    static Scanner sc = new Scanner(System.in);
    static StringBuffer sql = new StringBuffer();
    static Connection conn = null;
    private static int selectNum;
    private static char _conntinue;

    public static void main(String[] args) throws SQLException {
        conn = DBConn.getConnection();

        dispMenu();
        while (true) {
            SelectMenu();
            handleMenu();
        }
    } //end main

    private static void dispMenu() {
        // TODO Auto-generated method stub
        String[] menus = {"메뉴출력", "부서정보", "부서추가", "부서수정", "부서삭제", "부서검색", "종료"};
        System.out.println("--------메뉴 출력--------");
        for (int i = 0; i < menus.length; i++) {
            System.out.printf("%d, %s \n", i+1, menus[i]);
        }
    } //end disp

    private static void SelectMenu() {
        System.out.println("--------메뉴 선택--------");
        System.out.print("메뉴 입력: ");
        selectNum = sc.nextInt();
    } //end select

    private static void handleMenu() throws SQLException {
        System.out.println("--------메뉴 처리--------\n");
        SelectMenu sm;
        sm = SelectMenu.values()[selectNum];
        switch (sm) {
        case 메뉴출력:
            dispMenu();
            break;

        case 부서정보:
            getAllDeptInfo();
            break;

        case 부서추가:
            addDept();
            break;
            
        case 부서수정:
            updateDept();
            break;
            
        case 부서삭제:
            deleteDept();
            break;

        case 부서검색:
            searchDept();
            break;

        case 종료:
            exit();
            break;
        default:
            System.out.println("1~7 선택하세요");
            break;
        }
    } //handleMenu
}
```

먼저 모든 부서정보를 출력하는 `getAllDeptInfo()` 메서드를 정의해보자.
emp 사원정보 출력하는것과 동일한 루틴이지만 `DTO`을 적용

```java
public class DeptDTO {

    private int deptno;
    private String dname;
    private String loc;
    
    @Override
    public String toString() {
        return String.format("%d, %10s %10s", deptno, dname, loc);
    }
    
    public DeptDTO() {
    }

    public void setDname(String dname) {
        this.dname = dname;
    }
    public String getLoc() {
        return loc;
    }
    public void setLoc(String loc) {
        this.loc = loc;
    }
}
```

정말 간단한 dept테이블에서 얻어온 정보를 저장하기 위한 DTO 객체 완성...  

dept 테이블에서 모든 정보를 읽어와서 만든 **DTO객체**에 하나씩 넣어서  
이걸 또 `ArrayList`에 집어넣어서 출력하도록 하자.  

```java
public static void getAllDeptInfo() throws SQLException
    {
        if(conn.isClosed())
        {
            System.out.println("DB 연결이 닫혔습니다.");
            return;
        }
        sql.append("SELECT * FROM dept");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql.toString());
        ArrayList<DeptDTO> list = new ArrayList<>();

        DeptDTO dto = null;
        while (rs.next()) {
            dto = new DeptDTO();
            dto.setDeptno(rs.getInt("deptno"));
            dto.setDname(rs.getString("dname"));
            dto.setLoc(rs.getString("loc"));
            list.add(dto);
        }

        Iterator<DeptDTO> ir = list.iterator();
        System.out.println("---모든 부서정보 출력---");
        while (ir.hasNext()) {
            dto = ir.next();
            System.out.println(dto.toString());
        } 
        //        stop();
        sql.setLength(0);
        rs.close();
        stmt.close();
    }
```

출력값

```
--------메뉴 처리--------

---모든 부서정보 출력---
10, ACCOUNTING   NEW YORK
20,   RESEARCH     DALLAS
30,      SALES    CHICAGO
40, OPERATIONS     BOSTON
--------메뉴 선택--------
```

이번엔 부서를 추가하는 SQL 쿼리를 `Statement`객체를 사용해 날려보자.  

```java
private static void addDept() throws SQLException {

    Statement stmt = null;
    do {
        System.out.print("부서명 입력: ");
        String dname = sc.next(); //부서명 입력
        System.out.print("지역 입력: ");
        String loc = sc.next(); //지역 입력

        sql.append(" INSERT INTO tbl_dept(deptno, dname, loc) ");
        sql.append(String.format(" VALUES(seq_dept.nextval, '%s', '%s') ", dname, loc));
        
        System.out.println(sql.toString());
        stmt = conn.createStatement();
        int num = stmt.executeUpdate(sql.toString()); 

        System.out.println(num + "개부서 입력 완료!");
        System.out.println("계속 하시겠습니까? Y/N");
        try {
            _conntinue = (char) System.in.read();
            System.in.skip(System.in.available());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sql.setLength(0);
    } while (Character.toUpperCase(_conntinue) == 'Y');
    stmt.close();
}
```

입출력값  

```
--------메뉴 선택--------
메뉴 입력: 3
--------메뉴 처리--------

부서명 입력: 영업부
지역 입력: 서울
 INSERT INTO tbl_dept(deptno, dname, loc)  VALUES(seq_dept.nextval, '영업부', '서울') 
1개부서 입력 완료!
계속 하시겠습니까? Y/N
N
--------메뉴 선택--------
메뉴 입력: 2
--------메뉴 처리--------

---모든 부서정보 출력---
10, ACCOUNTING   NEW YORK
20,   RESEARCH     DALLAS
30,      SALES    CHICAGO
40, OPERATIONS     BOSTON
50,        영업부         서울
```

기존의 tbl_dept테이블에 50번 부서가 추가되었다.

이번엔 부서정보를 업데이트하는 `searchDept()`

```java
private static void searchDept() throws SQLException {
        System.out.println("------검색조건 선택------");
        System.out.println("1. 부서명");
        System.out.println("2. 지역");
        System.out.print("고르세요(1/2): ");
        int condition = sc.nextInt();
        
        System.out.print("검색어 입력: ");
        String searchWord = sc.next().toUpperCase();
        
        sql.append(" SELECT * FROM tbl_dept ");
        if(condition == 1)
        {            
            sql.append(" WHERE dname LIKE '%" + searchWord + "%'");
        }
        else
        {
            sql.append(String.format(" WHERE loc LIKE '%%%s%%'", searchWord));
        }
        System.out.println(sql.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeQuery(sql.toString());
        
        ResultSet rs = stmt.executeQuery(sql.toString());
        ArrayList<DeptDTO> list = new ArrayList<>();

        DeptDTO dto = null;
        int deptno;
        String dname =null, loc = null;
        while (rs.next()) {
            deptno = rs.getInt("deptno");
            if(condition == 1)
                dname = rs.getString("dname").replace(searchWord, "["+ searchWord+"]");
            else
                loc = rs.getString("loc").replace(searchWord, "["+ searchWord+"]");
            
            
            dto = new DeptDTO();
            dto.setDeptno(deptno);
            dto.setDname(dname);
            dto.setLoc(loc);
            list.add(dto);
        }

        Iterator<DeptDTO> ir = list.iterator();
        System.out.println("---모든 부서정보 출력---");
        while (ir.hasNext()) {
            dto = ir.next();
            System.out.println(dto.toString());
        } 
        //        stop();
        sql.setLength(0);
        rs.close();
        stmt.close();
    }
```

java의 에서 `%`를 출력하고 싶다면 두 개 써주어야 한다.  
`sql.append(String.format(" WHERE loc LIKE '%%%s%%'", searchWord));`  

입출력값  

```
--------메뉴 선택--------
메뉴 입력: 6
--------메뉴 처리--------

------검색조건 선택------
1. 부서명
2. 지역
고르세요(1/2): 1
검색어 입력: es
 SELECT * FROM tbl_dept  WHERE dname LIKE '%ES%'
---모든 부서정보 출력---
20, R[ES]EARCH       null
30,    SAL[ES]       null
```

수정과 삭제는 설명 생략...

```java
private static void updateDept() throws SQLException {
        System.out.println("------부서 수정------");
        System.out.println("수정할 부서번호 선택");

        int deptno = sc.nextInt();
        sql.append(" SELECT * FROM dept WHERE deptno = " + deptno);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql.toString());
        
        String dName;
        String loc;

        
        if (rs.next()) {
            System.out.printf("deptno: %d\n", deptno = rs.getInt("deptno"));
            System.out.printf("dname: %s\n", dName = rs.getString("dname"));
            System.out.printf("dloc: %s\n", loc = rs.getString("loc"));
        }
        else
            System.out.println("존재하지 않는 부서입니다.");
        
        System.out.print("부서명: ");
        sc.nextLine();
        dName = sc.nextLine();
        
        System.out.print("부서위치: ");
        loc = sc.nextLine();
        
        sql.setLength(0);
        sql.append( " UPDATE tbl_dept ");
        sql.append(String.format(" SET dname = '%s', loc = '%s' ", dName, loc));
        sql.append(" WHERE deptno = " + deptno);
        System.out.println(sql.toString());
        int num = stmt.executeUpdate(sql.toString());
        System.out.println(num+"개 수정되었습니다.");
        stmt.close();
        sql.setLength(0);
    }

    private static void deleteDept() throws SQLException {
        System.out.println("------부서 삭제------");
        System.out.printf("삭제할 부서 입력: ");
        
        String inputdel = sc.nextLine();
        inputdel = sc.nextLine();
        System.out.println(inputdel + "test");
        sql.append(String.format(" DELETE FROM tbl_dept WHERE deptno IN (%s) ", inputdel));
        System.out.println(sql.toString());
        Statement stmt = conn.createStatement();
        int num = stmt.executeUpdate(sql.toString()); 

        System.out.println(num + "개부서 삭제 완료!");
        sql.setLength(0);
        stmt.close();
    }
```

> IN 같은 SQL function을 잘 숙지하고 있다면 자바 코딩이 짧아진다...

### PreparedStatement 로 SELECT

`prepareStatement`는 구문분석, 파싱, 최적화후 메모리에 올려놓고  
다음 쿼리실행부턴 올려놓은 메모리에서 바로 쿼리를 실행하도록 하는 객체이다.  

```java
public class Practice {
    public static void main(String[] args) {
        
        StringBuffer sql = new StringBuffer();
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rsEmp = null;
        //
        try {
            con = DBConn.getConnection();    
            sql.append("SELECT rownum, emp.* FROM emp WHERE deptno = ?");
            pstmt = con.prepareStatement(sql.toString());
            //이제 ?에 들어가 바인딩 변수값을 설정하면 된다.
            //1번째 ?(인덱스)에 10을 설정
            pstmt.setInt(1, 10);
            rsEmp = pstmt.executeQuery();
            if (rsEmp.next()) {
                do {
                    int rownum = rsEmp.getInt("rownum");
                    int empno = rsEmp.getInt("empno");
                    String ename = rsEmp.getString("ename");
                    String job = rsEmp.getString("job");
                    Date   hiredate = rsEmp.getDate("hiredate");
                    System.out.printf("\t%d\t%d\t%s\t%s\t%tF\n"
                            ,rownum, empno, ename, job, hiredate);
                } while (rsEmp.next());
            }else {
                System.out.println("\t 사원 존재하지 않습니다.");
            }
        } catch (SQLException e) { 
            e.printStackTrace();
        } finally {
            try {
                rsEmp.close();
                pstmt.close();
                //con.close();
                DBConn.close();
            } catch (SQLException e) { 
                e.printStackTrace();
            }            
        } // finally 
    } //
} // 
```

특징으로 sql쿼리에 `?`를 사용해서 나중에 set~() 함수를 사용해서 값을 지정한다.  


### CallableStatement

`Stored Procedure(SP)` 를 호출할때 사용하는 클래스(인터페이스) 이다.

```sql
CREATE SEQUENCE seq_deptno
INCREMENT BY 10 
START WITH 50 ;

CREATE OR REPLACE PROCEDURE up_insert_dept
(
  pdname dept.dname%type
  , ploc dept.loc%type
)
IS
  vsql VARCHAR2(1000); 
BEGIN
  vsql := ' INSERT INTO dept (deptno, dname, loc ) '; 
  vsql := vsql || ' VALUES (:deptno, :dname, :loc) ';  
  execute immediate vsql
    using seq_deptno.nextval, pdname, ploc;  
END;
```

dept테이블에 시퀀스를 사용해서 부서를 추가하는 병범한 `SP`.  

```java
public static void main(String[] args) {
  Connection connection = DBConn.getConnection();
  
  Scanner scanner = new Scanner(System.in);
  String sql = " { call up_insert_dept(? , ?) } ";
  String pdname, ploc;
  System.out.print("부서명 입력: ");
  pdname = scanner.nextLine();
  System.out.print("위치 입력: ");
  ploc = scanner.nextLine();

  
  try (CallableStatement cstmt = connection.prepareCall(sql)) {
      cstmt.setString(1, pdname);
      cstmt.setString(2, ploc);
      int resultCnt = cstmt.executeUpdate(); // 영향받은 레코드수

      if (resultCnt == 1)
        System.out.println("부서저장완료!");
  } catch (SQLException e) {
      e.printStackTrace();
  } finally {
  DBConn.close();
}
```


다른 `Statement`와 다른건  `call` 을 통해 SP를 호출하는 것,  
`Connection`의 `prepareCall`을 통해 `CallableStatement` 클래스를 초기화 하는것.  

```java
String sql = " { call up_insert_dept(? , ?) } ";
CallableStatement cstmt = connection.prepareCall(sql);
int resultCnt = cstmt.executeUpdate();
```

CallableStatement 또한 `executeUpdate`와 `executeQuery`가 있다.

출력값

```
부서명 입력: 영업부
위치 입력: 서울
부서저장완료!
```

> 참고: 프로시저에 파라미터 들어갈게 없다면 괄호없이 호출하면 된다. `String sql = " { call 프로시저명 } ";`  

#### 프로시저에서 실행된 결과값 받아오기

INSERT, DELETE, UPDATE는 영향받은 `executeUpdate`로 레코드 수를 반환하고  

`CallableStatement`에서 수행된 결과를 받아오려면 `out mode`의 `argument`를 사용해야 한다.  

>SP mode:  https://kouzie.github.io/database/DB-15일차/#sp-내에서-정의한-매개변수-mode   

emp테이블에서 10번 부서 사원목록을 출력하는 SP를 작성해보자.

```sql
CREATE OR REPLACE PROCEDURE up_selectEmp
(
    pdeptno IN dept.deptno%type,
    pcursor OUT sys_refcursor
)
IS
    vsql VARCHAR2(1000);
BEGIN
    vsql := ' SELECT deptno, empno, ename, job, hiredate ';
    vsql := vsql || ' FROM emp ';
    vsql := vsql || ' WHERE deptno = :pdeptno ';
    OPEN pcursor FOR vsql 
    USING pdeptno;
END;
```

`pdeptno`로 부서번호를 입력받고 결과를 `pcursor`에 담는다.  



이 `out mode`의 `argument`를 java에서 받아오는 방법은 다음과 같다.  

```java
public static void main(String[] args) {
  Connection conn = DBConn.getConnection();
  Scanner sc = new Scanner(System.in);
  System.out.print("deptno 입력: ");
  int deptno = sc.nextInt();

  StringBuffer sql = new StringBuffer();
  ArrayList<EmpDTO> list = null;
  sql.append("{ CALL up_selectEmp(?,?) } ");
  ResultSet rs = null;
  try (
      CallableStatement cstmt = conn.prepareCall(sql.toString())
      )
  {
    cstmt.setInt(1, deptno);
    cstmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
    cstmt.executeQuery();
    rs = (ResultSet) cstmt.getObject(2);
    if(rs.next())
    {
      list = new ArrayList<>();
      EmpDTO dto = new EmpDTO();
      do {
        dto.setDeptno(rs.getInt("deptno"));
        dto.setEmpno(rs.getInt("empno"));
        dto.setEname(rs.getString("ename"));
        dto.setJob(rs.getString("job"));
        dto.setHiredate(rs.getDate("hiredate"));
        list.add(dto);
      } while (rs.next());
    }
    else
    {
      System.out.println("no data");
    }
    Iterator<EmpDTO> lit = list.iterator();
    while (lit.hasNext()) {
      EmpDTO empDTO = (EmpDTO) lit.next();
      System.out.println(empDTO);
    }
    rs.close();
    cstmt.close();
    DBConn.close();
  } catch (SQLException e) {
    e.printStackTrace();
  }
}
```
  
`executeQuery()` 하기 전에 `registerOutParameter()` 메서드를 호출한다.  

```java
cstmt.registerOutParameter(2, oracle.jdbc.OracleTypes.CURSOR);
cstmt.executeQuery();
rs = (ResultSet) cstmt.getObject(2);
```

2번째 `argument`가 Out mode의 매개변수이고 반환형은 `oracle.jdbc.OracleTypes.CURSOR`임을 알린다.  

각종 오라클의 자료형을 지정 가능하다. `oracle.jdbc.OracleTypes.NUMBER`, `oracle.jdbc.OracleTypes.VARCHAR` 등등...  

출력값

```
deptno 입력: 10
EmpDTO [empno=7934, ename=MILLER, job=ARTIST, mgr=0, hiredate=1982-01-23, sal=0, deptno=10]
EmpDTO [empno=7934, ename=MILLER, job=ARTIST, mgr=0, hiredate=1982-01-23, sal=0, deptno=10]
EmpDTO [empno=7934, ename=MILLER, job=ARTIST, mgr=0, hiredate=1982-01-23, sal=0, deptno=10]
```

> 요약! `CallableStatement` 에서 결과값을 받아오려면 `out mode` Parameter를 사용!  

#### Stored Procedure Exception 처리

저장 프로시저에서 예외가 발생하거나, 다른 이유로 `SQLException`이 발생하면 다음과 같이 `try...catch`문으로 처리 가능하다.  

```sql
create or replace procedure up_selectEmp
(
   pempno  in number
   , pename out varchar2
   , pjob  out varchar2
   , phiredate out date
)
is
    vsql varchar2(1000);
begin
    vsql := ' select ename, job, hiredate ';
    vsql := vsql || ' from emp ';
    vsql := vsql || ' where empno = :pempno ';
    execute IMMEDIATE vsql
    into pename, pjob, phiredate
    using pempno;
exception
    when no_data_found then
        raise_application_error(-20002, 'Data Not Found...');
    when others then
        raise_application_error(-20004, 'Othres Error...');
end;
```

`no_data_found` 일때 `-20002` 에러코드의 예외를 발생시킨다.

```java
public static void main(String[] args) {
    Connection conn = DBConn.getConnection();
    Scanner sc = new Scanner(System.in);
    System.out.print("empno 입력: ");
    int empno = sc.nextInt();
    String pename;
    String pjob;
    Date phiredate;
    StringBuffer sql = new StringBuffer();
    sql.append("{ CALL up_selectEmp(?,?,?,?) } ");
    try (CallableStatement cstmt = conn.prepareCall(sql.toString())) {
        cstmt.setInt(1, empno);
        cstmt.registerOutParameter(2, oracle.jdbc.OracleTypes.VARCHAR);
        cstmt.registerOutParameter(3, oracle.jdbc.OracleTypes.VARCHAR);
        cstmt.registerOutParameter(4, oracle.jdbc.OracleTypes.DATE);
        cstmt.executeQuery();
        pename = cstmt.getString(2);
        pjob = cstmt.getString(3);
        phiredate = cstmt.getDate(4);
        EmpDTO dto = new EmpDTO();
        dto.setEmpno(empno);
        dto.setEname(pename);
        dto.setJob(pjob);
        dto.setHiredate(phiredate);
        System.out.println(dto);
        cstmt.close();
        DBConn.close();
    } catch (SQLException e) {
        if( e.getErrorCode() == 20002 )
            System.out.println("empno 가 존재하지 않습니다.");
    }
}
```

`empno`를 입력하면 `ename`, `job`, `hirdate`를 `out mode argument`로 반환하는 간단한 예제...  

if문과 `SQLException`의 `getErrorCode()`를 통해 추가적인 예외처리가 가능하다.  

```java
if( e.getErrorCode() == 20002 )
        System.out.println("empno 가 존재하지 않습니다.");
```
