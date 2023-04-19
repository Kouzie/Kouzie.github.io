---
title:  "Java - JDBC Transaction!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - JDBC

---

## JDBC Transaction

게시판 MVC 패턴에서 게시글 조회는 2가지 작업으로 나뉘었다.  
조회수 증가, 게시글 출력...  

만약 증가가 성공했다 하더라도 게시글 출력이 실패한다면 이는 트랜잭션으로 묶여 있기 때문에 조회수 증가또한 취소되어야 한다.  

간단한 테스트를 위해 `up_insert_dept` 프로시저를 다음과 같이 변경

```sql
create or replace procedure up_insert_dept (
    pdeptno dept.deptno%type
   ,pdname dept.dname%type
   ,ploc dept.loc%type
)
is
  vsql varchar2(1000); 
  vdeptno dept.deptno%type;
begin
  select nvl(max(deptno),0)+10 
    into vdeptno
  from dept;
  vsql := ' INSERT INTO dept (deptno, dname, loc ) '; 
  vsql := vsql || ' VALUES (:deptno, :dname, :loc) ';  
  execute immediate vsql
    using pdeptno, pdname, ploc;  
END;
```

`deptno`를 시퀀스를 사용하지 않고 JDBC 에서 직접 입력하도록 변경하였다.

현재 dept테이블엔 다음 4개의 레코드만 존재.  

```
10    ACCOUNTING    NEW YORK
20    RESEARCH    DALLAS
30    SALES    CHICAGO
40    OPERATIONS    BOSTON
```

jdbc에서 똑같은 기본키를 가진 레코드 2개를 삽입해보자!  

```java
public static void main(String[] args) {
  String sql = " { call up_insert_dept(?, ? , ?) } ";

  int pdept;
  String pdname, ploc;
  Connection connection = DBConn.getConnection();
  connection.setAutoCommit(false); //default가 true인 auto commit을 비활성화
  try (CallableStatement cstmt = connection.prepareCall(sql)) {
    pdept = 70;
    pdname = "총무부";
    ploc = "제주도";
    cstmt.setInt(1, pdept);
    cstmt.setString(2, pdname);
    cstmt.setString(3, ploc);
    int resultCnt = cstmt.executeUpdate(); // 영향받은 레코드수
    if (resultCnt == 1)
      System.out.println("부서저장완료");
    
    resultCnt = cstmt.executeUpdate(); // 그리고 똑같은 레코드를 다시한번 삽입해보자.
    if (resultCnt == 1)
      System.out.println("부서저장완료");

    connection.commit();
  } catch (SQLException e) {
    e.printStackTrace();
    try {
      connection.rollback();
    } catch (SQLException e1) {
      e1.printStackTrace();
    }
  } finally {
  }
  DBConn.close();
}
```

출력값

```
부서저장완료
java.sql.SQLIntegrityConstraintViolationException: ORA-00001: unique constraint (SCOTT.PK_DEPT) violated
ORA-06512: at "SCOTT.UP_INSERT_DEPT", line 16
ORA-06512: at line 1
```

첫번째 레코드를 넣을땐 성공적으로 `executeUpdate`가 실행된다.  
똑같은 기본키를 가진 두번째 레코드를 넣으면 다음과 같이 에러가 발생하게 되고  

당연히 try..catch블럭의 `connection.rollback();`이 수행되면서 들어갔던 첫번째 레코드 또한 취소된다.  

트랜잭션에서 중요한 코드는 `connection.setAutoCommit(false);`!  

Connection 객체를 통해 만들어진 Statement들은 DML을 실행(`executeUpdate`)하면 자동으로 commit한다.  
이를 막기위해 `setAutoCommit`을 통해 AutoCommit못하도록 설정.  

---

## ResultSetMetaData

먼저 코드를 보고 ResultSetMetaData가 필요한지 알아보자.  

```java
String sql = " SELECT ROWNUM seq, table_name FROM tabs ";
Connection conn = null;
ResultSet rs = null;

conn = DBConn.getConnection();
try(PreparedStatement pstmt = conn.prepareStatement(sql);) {
    rs = pstmt.executeQuery();

    while (rs.next()) {
    System.out.printf("%3d. ", rs.getInt(1));
    System.out.printf("%s", rs.getString(2));
    System.out.println();
    }

    System.out.print("테이블 이름 입력: ");

    Scanner sc = new Scanner(System.in);
    String tableName = sc.nextLine();
    sql = "SELECT * FROM "+ tableName;
    PreparedStatement pstmt2 = conn.prepareStatement(sql);
    System.out.println(sql);
    ResultSetMetaData rsmd = pstmt2.getMetaData();
    ...
}
```

출력값
```
  1. DEPT
  2. EMP
  3. SALGRADE
  4. INSA
  5. TBL_INSTR
  6. TB_LIKE
  7. TBL_PIVOT
  8. TBL_LEVEL
  9. TBL_CHAR
  ...
  ...
 49. TBL_ROWTRIGGER01
 50. TBL_ROWTRIGGER02
 51. 상품
 52. 입고
 53. 판매
 54. DEPT_SAME
 55. TBL_DEPT
테이블 이름 입력: emp
SELECT * FROM emp
```

사용자가 직접 검색할 테이블을 선택후 해당 결과값을 테이블 형태로 동적으로 출력하고 싶다면 `ResultSetMetaData`를 사용해야 한다.  

`ResultSetMetaData`을 통해 해당 레코드의 칼럼수, 칼럼타입, 칼럼크기 등을 알아 올 수 있다.  

`desc emp` 로 얻어온 emp의 정의는 다음과 같다.

```
이름       널?       유형           
-------- -------- ------------ 
EMPNO    NOT NULL NUMBER(4)    
ENAME             VARCHAR2(10) 
JOB               VARCHAR2(9)  
MGR               NUMBER(4)    
HIREDATE          DATE         
SAL               NUMBER(7,2)  
COMM              NUMBER(7,2)  
DEPTNO            NUMBER(2)    
```

emp의 구조를 `ResultSetMetaData`의 `get~()` 메서드로 알아온다면 동적으로 테이블마다 다른 처리를 할 수 있다.  

```java
public static void main(String[] args) {
  String sql = " SELECT ROWNUM seq, table_name FROM tabs ";
  Connection conn = null;
  ResultSet rs = null;
  
  conn = DBConn.getConnection();
  try(PreparedStatement pstmt = conn.prepareStatement(sql);) {
    rs = pstmt.executeQuery();
    
    while (rs.next()) {
      System.out.printf("%3d. ", rs.getInt(1));
      System.out.printf("%s", rs.getString(2));
      System.out.println();
    }
    
    System.out.print("테이블 이름 입력: ");
    
    Scanner sc = new Scanner(System.in);
    String tableName = sc.nextLine();
    sql = "SELECT * FROM "+ tableName;
    PreparedStatement pstmt2 = conn.prepareStatement(sql);
    ResultSetMetaData rsmd = pstmt2.getMetaData();
    System.out.println("칼럼갯수: " + rsmd.getColumnCount());
    for (int i = 1; i <= rsmd.getColumnCount() ; i++) {
      System.out.print(rsmd.getColumnLabel(i));
      System.out.print("   ");
      System.out.print(rsmd.getColumnTypeName(i));
      int columType = rsmd.getColumnType(i);
      int scale = rsmd.getScale(i); // double  number( p,s )
      if( columType == Types.NUMERIC && scale != 0 ) {  // double
        int pre = rsmd.getPrecision(i);
        System.out.print("(" + pre + "," + scale + ")");
      }else if(columType == Types.NUMERIC && scale == 0 ) {  // int
        int pre = rsmd.getPrecision(i);
        System.out.print("(" + pre + ")");
      }else if(columType == Types.VARCHAR || columType == Types.CLOB ) {
        System.out.print("(" + rsmd.getColumnDisplaySize(i) + ")");
      }else if(columType == Types.DATE || columType == Types.TIMESTAMP) {
      }
      System.out.println();
    }
    pstmt.close();
    rs.close();
    DBConn.close();
  } catch (SQLException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
}
```

출력값
```
  1. DEPT
  2. EMP
  3. SALGRADE
  4. INSA
  5. TBL_INSTR
  6. TB_LIKE
  ...
  ...
 53. 판매
 54. DEPT_SAME
 55. TBL_DEPT
테이블 이름 입력: emp
칼럼갯수: 8
EMPNO   NUMBER(4)
ENAME   VARCHAR2(10)
JOB   VARCHAR2(9)
MGR   NUMBER(4)
HIREDATE   DATE
SAL   NUMBER(7,2)
COMM   NUMBER(7,2)
DEPTNO   NUMBER(2)
```


## JDBC Connection Pooling, Connection Factory 

데이터베이스의 처리에서 가장 많은 시간을 필요로 하는 부분은 데이터베이스의 로그인 부분이다.  
jdbc 프로그래밍 하다보면 가장 오래걸리는 부분이 `DBConn.getConnection`함수 호출부분이란걸 알 수 있다..  

컨넥션 풀링을 사용하면 미리 연결 해놓고 기다릴 필요 없다.  
프로그래밍 시작할 때 미리 컨넥션(`Connection`)을 여러 개 개설한 뒤 필요할 때 만들어 둔 컨넥션을 사용하는 기법을 컨넥션 풀링이라 한다.  

빠른 연결을 위한 `Connection Pooling`과 `Connection Factory`를 알아보자.  

먼저 DB연결을 위한 XML을  Properties로 생성.  

```java
public static void main(String[] args) throws IOException {
  Properties p = new Properties();
  p.put("Driver","oracle.jdbc.driver.OracleDriver");
  p.put("URL","jdbc:oracle:thin:@172.17.107.68:1521:xe");
  p.setProperty("MaxConn","10");
  p.setProperty("User","scott");
  p.setProperty("Password","tiger");
  
  FileOutputStream out = new FileOutputStream(".\\src\\days06\\jdbc.properties");
  p.store(out,"JDBC Config Setting");
  out.close();
}
```
> put과 `setProperty` 함수는 거의 동일한 역할을 하는 함수.  

그럼 현재폴더에 다음과 같이 `jdbc.properties` 파일이 생긴다.  

```
#JDBC Config Setting
#Mon Mar 25 10:07:04 KST 2019
URL=jdbc\:oracle\:thin\:@172.17.107.68\:1521\:xe
Password=tiger
Driver=oracle.jdbc.driver.OracleDriver
User=scott
MaxConn=10
```

### Connection Factory  

![image1](/assets/java/jdbc/days06/image1.png){: .shadow}  

원래는 바로 DB에서 `OracleDriver`클래스와 `DriverManager`를 통해 `Connetion`객체를 가져왔는데 `ConnFactory` 클래스를 통해 `Connection`을 가져온다.  
우리가 전에 만든 `DBConn`클래스와 거의 비슷하다,  

`DBConn`은 `getConnection`을 통해 하나 만들어져 있는 `Connection`객체를 반환하고  
`ConnFactory`는 `createConnection` `Connection`객체를 계속 생성하여 반환한다.  


앞으로 `~Factory` 라는 클래스가 많은데 대부분 앞에 붙은 객체를 생성해서 반환해주는 클래스이다.  

```java
public class PracticeConnFactpry {
    public static void main(String[] args) {
        ConnFactory factory = ConnFactory.getDefaultFactory();
        Connection conn = factory.createConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM emp");
            while (rs.next()) {
                System.out.println(rs.getString("ename"));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class ConnFactory{
    private static int maxconn = 0;
    private static String url = null;
    private static String driver = null;
    private static String user = null;
    private static String password = null;
    private static ConnFactory connFactory = new ConnFactory();
    static{
        try{
            loadProperties("./jdbc.properties");
        }catch(IOException e){
            System.out.println("jdbc.properties ............");
            e.printStackTrace();
        }
    }
    private ConnFactory() {}; //싱글톤
    public Connection createConnection() {
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;
    }
    public static ConnFactory getDefaultFactory() {
        if(connFactory == null)
            connFactory = new ConnFactory();
        return connFactory;
    }
    private static void loadProperties(String fileName) throws IOException {
        Properties p = new Properties();
        FileInputStream in = new FileInputStream(fileName);
        p.load(in);
        in.close();
        url = p.getProperty("URL");
        driver = p.getProperty("Driver");
        user = p.getProperty("User");
        password = p.getProperty("Password");
        maxconn = Integer.parseInt(p.getProperty("MaxConn"));
    }
    public static int getMaxConn(){
        return maxconn;
    }
}
```

`ConnFactory` 역시 싱글톤 기법으로 생성되었고 생성된 `ConnFactory`의 `createConnection`메서드 통해 `Connection`객체를 계속 생성가능하다.  


### Connection Pooling

`Connection Pooling`은 `Connection Factory`를 사용해 미리 컨넥션을 일정 수만큼 생성시킨 뒤  
사용자에게 컨넥션을 빌려주고 다시 반환받는 형식으로 컨넥션을 관리한다.  

![image2](/assets/java/jdbc/days06/image2.png){: .shadow}  
  

```java
public class PracticeConnPool {
    public static void main(String[] args) throws InterruptedException {
        ConnPool pool = ConnPool.getConnPool();
        Connection conn = pool.getConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM emp");
            while (rs.next()) {
                System.out.println(rs.getString("ename"));
            }
            rs.close();
            stmt.close();
            pool.releaseConnection(conn);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
} 
class ConnPool{
    
    private Vector<Connection> buffer = new Vector<>();
    private static ConnPool connPool = new ConnPool();
    static {
        initConnPool();
    }
    private ConnPool(){} //싱글톤
    private static void initConnPool() {
        destroyConnPool();
        Vector<Connection> temp = ConnPool.getConnPool().getConnPoolBuffer();
        ConnFactory factory = ConnFactory.getDefaultFactory();
        for (int i = 0; i < ConnFactory.getMaxConn(); i++) {
            Connection conn = factory.createConnection();
            temp.addElement(conn);
            System.out.println("NewConnection Created.."+conn);
        }
    }
    private static void destroyConnPool() {
        Vector<Connection> temp = ConnPool.getConnPool().getConnPoolBuffer();
        Enumeration<Connection> en = temp.elements();
        while (en.hasMoreElements()) {
            Connection conn = (Connection) en.nextElement();
            if(conn != null) {
                try {
                    conn.close();
                    System.out.println("Connection Closed.."+conn);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    private Vector<Connection> getConnPoolBuffer() {
        return this.buffer;
    }
    public static ConnPool getConnPool(){
        if(connPool == null){
            connPool = new ConnPool();
        }
        return connPool;
    }
    public synchronized Connection getConnection() throws InterruptedException
    {
        while (this.buffer.size() == 0) {
            this.wait();
            }
        Connection conn = this.buffer.remove(this.buffer.size()-1); //맨끝의 Connection 대여
        System.out.println("Connection 대여 getConnection()"+conn);
        return conn;
    }
    public synchronized void releaseConnection(Connection conn)
    {
        this.buffer.addElement(conn);
        System.out.println("Connection 반환 releaseConnection()"+conn);
        this.notifyAll();
    }
}
```


`ConnPool`역시 싱글톤 방식으로 객체 생성하고 `ConnFactory`를 사용해 `MaxConn` 수만큼 `Connection`객체를 생성.  
만들어진 `Connection`을 `Vector`에 담아 `getConnection`메서드가 호출될 때 마다 하나씩 `remove`하며 `return connn`  
사용자가 `Connection`객체를 다쓰고 `releaseConnection`메서드를 호출하면 다시 `Vector`에 `add`한다.
ㄴ