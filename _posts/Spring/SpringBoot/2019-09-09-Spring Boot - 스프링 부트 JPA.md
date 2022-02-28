---
title:  "Spring Boot - 스프링 부트 JPA!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true
toc_sticky: true

---

## JPA 개념

데이터를 유지하고 관리하기위해 DB를 사용하고   
java프로그램에서 DB와 연동하는 jdbc를 사용하였다.  

여기서 발생하는 수많은 중복코드 (jdbc 연동 및 dto, vo객체에 데이터 매핑)을 제거하기 위해 Mybatis와 같은 매퍼도 사용한다.  

위의 설명은 모두 데이터베이스의 종속적인 (테이블에 해당하는 vo객체를 정의하고 매핑) 코딩이었다.  

반대로 `Java Persistent API` 를 사용해 개발자가 정의한 객체에 종속적인 (`Object Relation Mapping ORM`)적인 시스템을 구축해보자!

JDBC는 자바와 데이터베이스를 연결할 때 어떤 데이터베이스이던 간에  
개발자는 인터페이스에 제공하는 메서드를 사용하여 똑같은 방법으로 각종 DB에 연결이 가능하다.  
추상화를 이용하여 이렇게 구현가능하였다.  

JPA는 이에 더해 어떤 데이터베이스이던간 똑같은 방법으로 VO에 저장된 데이터를 DB에 저장하고 업데이트하고 검색, 삭제할 수 있다.  

JPA는 일종의 기술로 여러 기업에서 JPA를 구현한 라이브러리를 제공하는데 Hibernate가 가장 유명하다.  
(이외에도 EclipseLink, DataNuclues 등이 있음)

JPA를 개발하려면 기존엔 아래와 같은 코드가 필요했다.  

### Spring Data JPA, JPA, Hibernate

3가지 라이브러리 사용시 쉽게 혼동할 만한 내용을 잘 정리해둔 블로그  

> 출처: https://suhwan.dev/2019/02/24/jpa-vs-hibernate-vs-spring-data-jpa/

Spring으로 ORM을 사용해 개발시 단순 Hibernate를 사용해 개발할 일이 없다. JPA만을 사용할 일은 더더욱 없다.  

![springboot_jpa_1](/assets/springboot/springboot_jpa_1.png){: .shadow}   

즉 Hibernate, Spring Boot JPA모두 JPA를 구현한 구현체이지만 `Spring Boot JPA` 가 훨신 편하게 사용할 수 있음을 알 수 있다.  

Spring이 아닌 다른 프레임워크로 개발한다면 Hibernate를 사용해 개발해야 할것.  



## 엔티티, 엔티티 매니저

`Spring Data JPA` 를 사용하면 엔티티를 엔티티 매니저를 직접적으로 사용할 일은 없다.  
하지만 `Spring Data JPA` 내부적으로 `JPA` 규약과 이를 구현한 `Hibernate` 가 엔티티 매니저를 통해 데이터를 관리하기에 알아두어야함  

`사원`이라는 테이블이 있다면 `사원번호, 사원명, 생년월일, 주민번호`와 같은 속성들이 존재할 것이고 이러한 정보를 가진 레코드들이 여러개 있을것이다.  

JPA에서 엔티티는 `사원` 테이블을 만들기 위한 일종의 객체이다(명세서같은)  

즉 엔티티는 테이블을 만들기위한 정보를 담고있는 객체이며 DB에서 데이터를 저장하기 위한 용도로도 사용되는 객체이다. (DTO, VO와 비슷하게 생겼다)  

엔티티는 위의 2가지 의미를 모두 가지고 있으며 자주 혼용하기 때문에 헷갈리지 않도록 주의...

엔티티 매니저란 여러 엔티티를 관리하는 객체이다.  
스프링 부트에서 지원하는 기능으로 엔티티가 DB와 연동되어 데이터를 가져오고 수정되고 저장되고 삭제되는 과정을 담당하는 객체이다.  


![springboot2_1](/assets/springboot/springboot2_1.png){: .shadow}  

엔티티 매니저가 위 사진에 있는 여러 기능을 호출하면서 실제 java객체를 DB에 연동하고 CRUD작업을 처리한다.  

`New` : 실제 DB와 연동되지 않은 기존 java객체

`Managed`(영속) : 엔티티의 정보가 DB에 저장되어있고 메모리도 이와 **같은 상태로 존재하는 상태**(실제 데이터와 메모리상의 테이터가 일치), 이 공간은 영속 컨텍스트라 한다.

`Detached`(준영속) : 영속 컨텍스트에서 엔티티를 꺼내와 각종 CRUD작업을 사용하는 상태, 아직 DB와는 연동되지 않아 **같은상태로 존재하지 않는다.**   

`Removed` : 더이상 사용하지 않아 영속 컨텍스트에서 쫓겨난 상태, 

위의 각 상태에서 실제 DB에서 데이터를 검색, 연동, 수정/삭제 작업을 하기위해 수많은 메서드를 호출하는 것이 사진에 보인다.  

스프링 프레임워크를 사용하지 않고 이를 처리하려면 코드가 복잡하겠지만 스프링 부트에선 어노테이션 몇개로 웬만한 작업은 처리 가능하다.  

## Spring Boot DB연결


![springboot1_0](/assets/springboot/springboot1_0.png){: .shadow}   

위의 라이브러리를 추가했다면 DB연동을 위한 Datasource설정을 진행한다.  

```conf
# DB연결 과정
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/jpa_ex?useSSL=false
spring.datasource.username=jpa_user
spring.datasource.password=jpa_user

# ddl 생성시 DB고유의기능을 사용
#spring.jpa.hibernate.ddl-auto=create
spring.jpa.hibernate.ddl-auto=update

# 데이터베이스 고유의 기능을 사용하는가
spring.jpa.generate-ddl=false

# 실행되는 sql문을 보여줄 것인가
spring.jpa.show-sql=true

# jpa가 사용하는 mysql 지정
spring.jpa.database=mysql

# 로그 레벨
logging.level.org.hibernate=info

spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
```
`spring.jpa.hibernate.ddl-auto` 속성은 DDL문을 어떻게 처리할건지 기능을 지정한다.  

`create` - 기존 테이블 삭제후 다시생성  
`create-drop` - 종료시 생성된 테이블 삭제후 종료  
`update` - 기존 테이블을 그대로 사용  
`validate` - 엔티티와 테이블이 매핑되는지만 확인  
`none` - 사용하지 않음  

보통 기존에 있는 테이블을 사용하기 때문에 `spring.jpa.hibernate.ddl-auto=update`를 주로 사용한다.  

## JPA Repository

`Spring Data JPA` 외에도 여러 `Spring Data` 프로젝트들은(`redis, jdbc, mongo` 등) `Repository` 패턴을 사용한다.  

먼저 테이블로 설계를 위한, 데이터를 담기위한 객체를 하나 생성.  

```java
@Getter
@Setter
@ToString
@Entity
@Table(name="tbl_boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    private String title;
    private String writer;
    private String content;

    @CreationTimestamp
    private Timestamp regdate;
    @UpdateTimestamp
    private Timestamp updatedate;
}
```

위의 클래스로 `CRUD` 작업을 처리하게 되는데 이를 위한 `Repository` 인터페이스를 정의해야한다.  

```java
public interface BoardRepository extends CrudRepository<Board, Long> {

}
```

`CrudRepository` 인터페이스를 상속하는데 `CrudRepository`에는 기본적인 `CRUD` 를 위한 가상메서드가 정의되어 있다.  

![springboot2_2](/assets/springboot/springboot2_2.png){: .shadow}  

`save` 메서드를 통해 `update`, `insert` 가 가능하다.  

`BoardRepository`를 구현한 클래스를 작성할 필요는 없다.  
`Spring Data JPA` 가 DB 에 맞춰 알맞은 쿼리를 정의한 구현체를 동적으로 생성해준다.  

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class BoardRepositoryTests {

    @BoardRepository
    private BoardRepository boardRepo;

    @Test
    public void testInsert() {
        Board board = new Board();
        board.setTitle("게시물의 제목");
        board.setContent("게시물 내용 넣기");
        board.setWriter("user00");
        boardRepo.save(board);
    }
}
```

그냥 `@Autowired`로 스프링부트가 생성해준 빈 객체를 참조시키면 된다.  

## JPA 쿼리작성 - 자동 생성 쿼리 메서드

간단한 `CRUD` 작업은 `CrudRepository`를 참조한 `Repository`객체를 정의하는 것만으로 수행할 수 있는건 알겠다.  

하지만 DB 를 사용하려면 간단한 쿼리로는 불가능하기에 개발자가 생각하는 복잡한 쿼리도 JPA로 수행할 수 있어야 한다.  

```
find..by..
read..by..
query..by..
get..by..
count..by..
```

위와같은 패턴으로 JPA가 자동으로 만들어주는 쿼리메서드를 사용해 여러가지 조건문을 추가할 수 있다.  

![springboot2_3](/assets/springboot/springboot2_3.png){: .shadow}  

> https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation



`Keyword` | `Sample` | `JPQL snippet`  
|---|---|---|
`And` | `findByLastnameAndFirstname` | `… where x.lastname = ?1 and x.firstname = ?2`  
`Or` | `findByLastnameOrFirstname` | `… where x.lastname = ?1 or x.firstname = ?2`  
`Is,Equals` | `findByFirstname,findByFirstnameIs,findByFirstnameEquals` | `… where x.firstname = ?1`  
`Between` | `findByStartDateBetween` | `… where x.startDate between ?1 and ?2`  
`LessThan` | `findByAgeLessThan` | `… where x.age < ?1`  
`LessThanEqual` | `findByAgeLessThanEqual` | `… where x.age <= ?1`  
`GreaterThan` | `findByAgeGreaterThan` | `… where x.age > ?1`  
`GreaterThanEqual` | `findByAgeGreaterThanEqual` | `… where x.age >= ?1`  
`After` | `findByStartDateAfter` | `… where x.startDate > ?1`  
`Before` | `findByStartDateBefore` | `… where x.startDate < ?1`  
`IsNull` | `findByAgeIsNull` | `… where x.age is null`  
`IsNotNull,NotNull` | `findByAge(Is)NotNull` | `… where x.age not null`  
`Like` | `findByFirstnameLike` | `… where x.firstname like ?1`  
`NotLike` | `findByFirstnameNotLike` | `… where x.firstname not like ?1`  
`StartingWith` |`findByFirstnameStartingWith` | `… where x.firstname like ?1 (parameter bound with appended %)`  
`EndingWith` | `findByFirstnameEndingWith` | `… where x.firstname like ?1 (parameter bound with prepended %)`  
`Containing` | `findByFirstnameContaining` | `… where x.firstname like ?1 (parameter bound wrapped in %)`  
`OrderBy` | `findByAgeOrderByLastnameDesc` | `… where x.age = ?1 order by x.lastname desc`  
`Not` | `findByLastnameNot` | `… where x.lastname <> ?1`  
`In` | `findByAgeIn(Collection<Age> ages)` | `… where x.age in ?1`  
`NotIn` | `findByAgeNotIn(Collection<Age> ages)` | `… where x.age not in ?1`  
`True` | `findByActiveTrue()` | `… where x.active = true`  
`False` | `findByActiveFalse()` | `… where x.active = false`  
`IgnoreCase` | `findByFirstnameIgnoreCase` | `… where UPPER(x.firstame) = UPPER(?1)`  


> `Entitiy`의 칼럼 제약조건 설정은 아래 사이트 참고
> http://www.thejavageek.com/2014/05/24/jpa-constraints/

### 페이징 처리, 정렬 처리 - Pageable, Sort

특정 페이지의 데이터만 가져오고 싶을때 `org.springframework.data.domain.Pageable` 클래스를 사용하면 편하다.  

```java
public interface BoardRepository extends CrudRepository<Board, Long> {
    //bno를 기준으로 내림차순 정렬 - bno > ? ORDER BY bno
    List<Board> findByBnoGreaterThanOrderByBnoDesc(Long bno, Pageable paging); 
}
```

`Pageable`객체는 `PageRequest.of()` 메서드의 3가지 방법으로 생성 가능

```java
PageRequest.of(int page, int size)
PageRequest.of(int page, int size, Sort.Direction direction, String ..props)
```
`page` - 출력할 페이지 `int`값  
`size` - 페이지당 출력할 데이터 개수  
`direction` - `ASC`, `DESC`(오름, 내림차순 지정)  
`props` - 정렬시킬 속성명(속성명들)  

```java
Pageable paging1 = PageRequest.of(0, 10);
List<Board> list1 = repo.findByBnoGreaterThanOrderByBnoDesc(0L, paging1);
list1.forEach(board->{
    System.out.println(board);
});
Pageable paging2 = PageRequest.of(0, 10, Sort.Direction.DESC, "bno");
List<Board> list2 = repo.findByBnoGreaterThan(0L, paging2);
list2.forEach(board->{
    System.out.println(board);
});
```

쿼리메서드를 통해서 정렬할 수 있고 `Pageable`를 사용해서 쿼리메서드에 정렬기능을 추가할 수 도 있다.  

페이징에서 정렬은 필수이니 어떤 방식을 사용하건 정렬기능을 추가해야한다.  

### Page<T> 타입

Rest Server 가 아닌 스프링에서 템플릿 까지 지원하는 경우  
서버에서 페이징 처리까지 모두 해주는 `Page` 클래스를 사용하면 템플릿 뷰에서 페이지를 출력하기 편하다.   

| 메서드                          | 설명                |
| ---------------------------- | ----------------- |
| `int getNumber();`           | 현제 페이지 넘버         |
| `int getSize();`             | 한 페이지 출력 데이터 크기   |
| `int getTotalPages();`       | 총 페이지 수           |
| `int getNumberOfElements();` | 실제 페이지에 출력할 데이터 수 |
| `long getTotalElements();`   | 총 데이터 수           |
| `boolean hasPreviousPage();` | 이전페이지 유무          |
| `boolean hasNextPage();`     | 이후페이지 유무          |
| `boolean isFirst();`         | 현제 페이지 첫 페이지인지    |
| `boolean isLast();`          | 현제 페이지가 마지막 페이지인지 |
| `Pageable previousPageable`  | 이전 페이지 객체         |
| `Pageable nextPageable();`   | 다음 페이지 객체         |
| `List<T> getContent();`      | 조회된 데이터 리스트       |
| `boolean hasContent();`      | 결과 데이터 존재 유무      |
| `Sort getSort();`            | 검색시 사용된 sort정보    |

반환값을 `List<Board>` 에서 아래처럼 `Page<Board>` 로 변경

```java
Pageable paging = PageRequest.of(0, 10, Sort.Direction.DESC, "bno");
Page<Board> result = repo.findByBnoGreaterThan(0L, paging);
result.forEach(board->{
    System.out.println(board);
});
System.out.println("result.getTotalElements(): " + result.getTotalElements());
System.out.println("result.getTotalPages(): " + result.getTotalPages());
System.out.println("result.getContent(): " + result.getContent());
System.out.println("result.getNumber(): " + result.getNumber());
System.out.println("result.getNumberOfElements(): " + result.getNumberOfElements());
System.out.println("result.getPageable(): " + result.getPageable());
System.out.println("result.getSize(): " + result.getSize());
System.out.println("result.getSort(): " + result.getSort());
System.out.println("result.get(): " + result.get());
System.out.println("result.hasContent(): " + result.hasContent());
System.out.println("result.isLast(): " + result.isLast());
System.out.println("result.isFirst(): " + result.isFirst());
System.out.println("result.hasNext(): " + result.hasNext());
System.out.println("result.hasPrevious(): " + result.hasPrevious());
System.out.println("result.nextPageable(): " + result.nextPageable());
System.out.println("result.previousPageable(): " + result.previousPageable());
```

```
Hibernate: select board0_.bno as bno1_0_, board0_.content as content2_0_, board0_.regdate as regdate3_0_, board0_.title as title4_0_, board0_.updatedate as updateda5_0_, board0_.writer as writer6_0_ from tbl_boards board0_ where board0_.bno>? order by board0_.bno desc limit ?
Hibernate: select count(board0_.bno) as col_0_0_ from tbl_boards board0_ where board0_.bno>?
Board(bno=200, title=제목...199, writer=user09, content=내용...199채우기, regdate=2019-08-08 17:01:36.0, updatedate=2019-08-08 17:01:36.0)
...
...
Board(bno=191, title=제목...190, writer=user00, content=내용...190채우기, regdate=2019-08-08 17:01:36.0, updatedate=2019-08-08 17:01:36.0)
result.getTotalElements(): 199
result.getTotalPages(): 20
result.getContent(): [생략...]
result.getNumber(): 0
result.getNumberOfElements(): 10
result.getPageable(): Page request [number: 0, size 10, sort: bno: DESC]
result.getSize(): 10
result.getSort(): bno: DESC
result.get(): java.util.stream.ReferencePipeline$Head@1cc9bd9b
result.hasContent(): true
result.isLast(): false
result.isFirst(): true
result.hasNext(): true
result.hasPrevious(): false
result.nextPageable(): Page request [number: 1, size 10, sort: bno: DESC]
result.previousPageable(): INSTANCE
```

알아서 `Count`까지 해서 `Page` 객체에 데이터를 넣어준다.  

## JPA 쿼리작성 - @Query, @Param

복잡한 구조 (각종 join, 여러 조건문 등) 을 가질 경우 자동생성되는 쿼리메서드로는 한계가 있다.  

`@Query`어노테이션을 사용하면 각종 요청에 대처가능하다.  
> `@Query`어노테이션으로 실제 DB종속적 쿼리는 아니지만 sql쿼리 비슷하게 작성된다.  

```java
@Query("SELECT b FROM Board b WHERE b.title LIKE %?1% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByTitle(String title);

@Query("SELECT b FROM Board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```

`?1`을 통해 첫번째 매개변수를 지정해 동적으로 검색조건을 변경 가능하고 `@Param`을 사용하면 변수 이름으로 지정가능하다.  

`Board`라는 테이블은 실제 DB안의 테이블 정보가 아닌 JPA를 통해 만들어질 객체명을 사용한다.  
> 칼럼명에 매칭되는 필드를 가지고 있는 객체라면 `ObjectMapper` 를 통해 `@Query`의 엔티티로 사용할 수 있다.  


```java
@Query("SELECT b FROM #{#entityName} b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```

> `#{#entityName}`을 사용하면 혹시라도 테이블을 참조할 객체명이 변경되어도 대처가능하다.  
Repository의 엔티티타입을 자동으로 매핑하기 때문.


나중에 JOIN처리나 칼럼 일부만들 질의할 때 `@Query`를 사용할 수 있다.  

페이징 처리 또한 `@Query` 어노테이션 상관 없이 페이징 처리 가능하다.  
```java
@Query("SELECT b FROM #{#entityName} b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content, Pageable paging);
```

### nativeQuery

`@Query`어노테이션의 속성으로 DB종속적인 sql를 작성할 수 있다.   
단 JPA의 장점인 어떤 DB이던 똑같은 문법을 사용하던 상관없는 독립성은 사용할 수 없게된다.  


```java
@Query(value = "SELECT b FROM tbl_board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC"
       nativeQuery=true)
public List<Board> findByContent(@Param("content") String content);
```

설정파일에 지정한 DB고유 문법을 사용할 수 있다.  


### JPA Custom DTO

> https://stackoverflow.com/questions/36328063/how-to-return-a-custom-object-from-a-spring-data-jpa-group-by-query

기존의 Entity(db정의된)가 아닌 직접 정의한 Java Object에 JPA를 사용해 Database에서 읽어온 값을 삽입, 반환하고 싶다면 위처럼 지정

```java
public class SurveyAnswerStatistics {
  private String answer;
  private Long   cnt;

  public SurveyAnswerStatistics(String answer, Long cnt) {
    this.answer = answer;
    this.count  = cnt;
  }
}

...
...

public interface SurveyRepository extends CrudRepository<Survey, Long> {
@Query( "SELECT new com.path.to.SurveyAnswerStatistics(v.answer, COUNT(v)) " +
        "FROM Survey v " +
        "GROUP BY v.answer")
List<SurveyAnswerStatistics> findSurveyCount();
}
```

클래스 풀 네임을 사용해 반환할 수 있다.  

`nativeQuery` 를 사용해야 한다면 아래처럼 적용 가능, 단 `getter` 메서드를 모두 정의해주어야 한다.  

```java
public interface SurveyRepository extends CrudRepository<Survey, Long> {
    @Query(value =
        "SELECT v.answer AS answer, COUNT(v) AS cnt " + 
        "FROM Survey v " + 
        "GROUP BY v.answer", nativeQuery = true)
    List<SurveyAnswerStatistics> findSurveyCount();
}
```

### @Modifying, @Transactional, @Commit


`@Query`는 기본적으로 `SELECT`구문만을 지원하지만 `@Modifying` 어노테이션으로 `UPDATE, DELETE`구현이 가능합니다.  

`@Modifying`을 통해 DML 사용시 해당 쿼리 메서드를 사용하려면 `@Transactional` 어노테이션 처리 필요.  

```java
public interface PDSBoardRepository extends CrudRepository<PDSBoard, Long> {
    
    @Modifying
    @Transactional
    @Query("UPDATE PDSFile f SET f.pdsfile = ?2 WHERE f.fno = ?1")
    public int updatePDSFile(Long fno, String newFileName);
}
```

Junit Test 의 경우 `@Transactional` 처리된 쿼리는 테스트가 끝남과 동시에 모두 자동 롤백 된다.  
이를 원치안을 경우 `@Commit` 어노테이션을 사용하면 마지막에 변경된 결과가 커밋된다.  

```java
@Log
@RunWith(SpringRunner.class)
@SpringBootTest
@Commit
public class PDSBoardTest {
    @Autowired
    PDSBoardRepository repo;

    @Test
    public void testUpdateFileName() {
        Long fno = 1L;
        String newName = "updatedFile1.doc";
        int count = repo.updatePDSFile(fno, newName);
        log.info("update count: " + count);
    }
}
```

반대로 `@Modifying` 와 `save` 메서드를 통해 테스트 메서드로 DB 변경을 원치 않는다면 상위에 
`@Transactional` 어노테이션 사용  
 
```java
@Transactional
public class PDSBoardTest {
    ...
```


## Querydsl - 동적sql처리

변수는 `@Param`, `?1` 기능을 통해 동적으로 지정가능하지만 조건문의 경우도 동적으로 추가할 수 있어야 한다.  

> maven 일 경우 설치는 아래 사이트 참고  
> http://www.querydsl.com/static/querydsl/4.0.1/reference/ko-KR/html_single/  

```java
/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = 384858839L;

    public static final QBoard boardx = new QBoard("board");

    public final NumberPath<Long> bno = createNumber("bno", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.sql.Timestamp> regdate = createDateTime("regdate", java.sql.Timestamp.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.sql.Timestamp> updatedate = createDateTime("updatedate", java.sql.Timestamp.class);

    public final StringPath writer = createString("writer");

    public QBoard(String variable) {
        super(Board.class, forVariable(variable));
    }

    public QBoard(Path<? extends Board> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoard(PathMetadata metadata) {
        super(Board.class, metadata);
    }
}
```
기존의 정수, 문자열 변수들이 `NumberPath`, `StringPath` 과 같은 객체형으로 변경되었다.   
내부적으로 조건문 처리를 할 수 있는 함수들이 정의되어있다.  

본격적으로 동적쿼리를 사용하기 위해 Repository 클래스를 변경해야 한다.  


```java
public interface BoardRepository extends 
    CrudRepository<Board, Long>, 
    QuerydslPredicateExecutor<Board> {
    ...
}
```

```java
...

@Autowired
BoardRepository repo;

@Test
public void testPredicate() {
    String type = "t";
    String keyword = "17";

    BooleanBuilder builder = new BooleanBuilder();
    
    QBoard board = QBoard.board;
    if (type.equals("t")){
        builder.and(board.title.like("%"+keyword+"%"));
    }
    builder.and(board.bno.gt(0L));
    Pageable pageable = PageRequest.of(0, 10);
    Page<Board> result = repo.findAll(builder, pageable);

    System.out.println(result.getSize());
    System.out.println(result.getTotalPages());
    System.out.println(result.getTotalElements());
    System.out.println(result.nextPageable());
    List<Board> list = result.getContent();
    list.forEach(b-> System.out.println(b));
    }
```
### path, expression

`alias` 와 표현식을 뜻함.  

```java
NumberExpression<Double> distanceExpression = 
    acos(cos(radians(Expressions.constant(latitude)))
        .multiply(cos(radians(qStore.latitude))
            .multiply(cos(radians(qStore.longitude))
                .subtract(radians(Expressions.constant(longitude)))
                .add(sin(radians(Expressions.constant(latitude)))
                    .multiply(sin(radians(qStore.latitude))))))).multiply(6371);

NumberPath<Double> distancePath = Expressions.numberPath(Double.class, "distance");

queryFactory.select(Expressions.as(distanceExpression, distancePath))
    ...
    ...
    .having(Expressions.predicate(Ops.LOE, distancePath, Expressions.constant(distance)))
```

이런식으로 쿼리를 추가하고 조건식에 적용할 수 있음.  

## 객체간 연관관계

위에선 하나의 테이블을 예제로 객체를 정의하였지만 정상적으로 사용하려면 거미줄처럼 테이블간의 연관관계가 구성되어있다.  

JPA에선 총 4가지로 관계를 구성한다.  

1. `@OneToOne`   
2. `@OneToMany`   
3. `@ManyToOne`   
4. `@ManyToMany`   

이런 참조관계는 유동적으로 지정할 수 있다.  

`Board`와 `Reply`라는 클래스(테이블 정의)가 있을때 당연히 게시글당 `n`개의 댓글이 달리니 `N:1` 관계가 구성된다.  

게시글 입장에선 `@OneToMany`이고 댓글 입장에선 `@ManyToOne` 이다.  

그렇다 해서 무조건 두 클래스다 서로를 가리키고 있을 필요는 없다.  

`Board`를 통해 댓글을 가져와야 한다면 참조해야 되지만  
만약 쿼리를 2번 호출해 `Board`와 `Reply`를 각각 가져온다면 서로 참조관계일 필요는 없다.  

`Reply`역시 댓글을 검색할 때 게시글 정보도 가져오고 싶다면 참조관계로 클래스를 정의하면 되지만  
쿼리를 2번 호출할경우 혹은 게시글 정보를 필요로 하지 않을경우 굳이 참조관계를 구성할 필요는 없다.  

일반적으로 `OneToMany`의 양방향 혹은 단방향 참조관계가 대부분이다.  
(`Board` 를 검색하며 `Reply` 들을 가져옴)

```java
@Getter
@Setter
@Entity
@Table(name = "tbl_boards")
@EqualsAndHashCode(of = "bno")
@ToString(exclude = "replies")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    private String title;
    private String writer;
    private String content;

    @CreationTimestamp
    private Timestamp regdate;
    @UpdateTimestamp
    private Timestamp updatedate;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<Reply> replies;
}
```

```java
@Getter
@Setter
@Entity
@Table(name = "tbl_replies")
@EqualsAndHashCode
@ToString(exclude = "board")
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;

    private String replyText;
    private String replyer;

    @CreationTimestamp
    private Timestamp regdate;
    @UpdateTimestamp
    private Timestamp updatedate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;
}
```

### @OneToOne 관계에서 FetchMode.LAZY 적용하기  

```java
public class Order {
    ...
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    private PurchaseLog purchaseLog;
    ...
}
```

`PurchaseLog`가 부모 엔티티  
`Order`가 자식 엔티티 이다, 서로를 `@OneToOne` 관계로 바라보고 있다.

`PurchaseLog` 의 `Order` 가 `null` 일 수 도 있는 상황,  
`Order`입장에선 `PurchaseLog`가 무조건 있다.  


이 상황에서 `Order` 안의 필드만 몇개 만 필요할 경우 Order 를 `select` 해오면 

분명 `FetchType.LAZY` 지정을 했음에도 `PurchaseLog`의 정보까지 `select` 되며 `N+1` 문제가 발생한다.  

`Hibernate` 개발자는 자식이 부모에게 `@OneToOne` 관계를 갖는것을 권장하지 않는다.  
그럼에도 `@OneToOne` 관계를 부모엔티티에 적용하고 싶다면 아래 조건중 하나를 택해야 한다.  

**부모엔티티를 통해 자식엔티티에 접근**  

위의 경우 `Purchase` 테이블을 `Order` 의 외래키 칼럼을 가지지 않는다.

`@OneToOne` 관계에서 `Lazy`로딩을 사용하려면 아래 규칙을 적용해야 한다고 한다.  

1. `not null`조건의 `@OneToOne`관계만 허용된다.   
2. 양방향이 아닌, 단방향 `@OneToOne`관계만 허용된다.  
3. `@PrimaryKeyJoin`은 허용되지 않는다.  

**부모를 가리키는 외래키 칼럼추가**  

만약 자식테이블에 부모를 가리키는 외래키 칼럼추가 가능하다면 위의 문제들을 `@MapsId` 로 모두 해결 가능하다.  

부모를 가리키는 칼럼이 생성되고 `not null`조건이 자동 적용된다.  


## 연관관계용 테이블 추가 생성 문제 - @JoinColumn, @JoinTable, mappedBy 속성

단방향이던 양방향이던 별다른 어노테이션이나 속성 설정 없이  
`@ManyToOne`, `@OneToMany` 어노테이션을 사용해서 관계 설정시 관계를 표시하기 위한 테이블이 추가로 생성된다.  

이는 자식테이블의 외래키 설정이 없기때문에 생성되는데  

자식 엔티티에서 `@JoinColumn`으로 부모 엔티티의 외래키 칼럼을 설정하고  
부모 엔티티에서 `@OneToMany`의 `mappedBy` 설정으로 자식을 매핑 가능하다.  

만약 테이블을 만들어야 하고 그에대한 설정을 하고 싶다면 `@JoinTable` 어노테이션 사용  
> http://wonwoo.ml/index.php/post/834  

### @JoinColumn, @OneToMany의 mappedBy 속성 차이  

둘다 연관관계를 설정할 때 사용하는 어노테이션이다.  

`mappedBy` 속성은 사용하는 쪽이 무조건 **주인 관계**의 객체이다.  

```java
@Entity //주인
public class WebBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    private String title;
    private String content;

    ...

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<WebReply> replies;
}

@Entity //종속
public class WebReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;

    private String replyText;
    
    ...

    @ManyToOne(fetch = FetchType.LAZY)
    private WebBoard board;
}
```

게시글과 댓글을 확인하면 게시글이 주인이며 게시글 테이블엔 답글의 연관관계 형성을 위한 외래키 속성이 들어가지 않는다.  

만약 게시글당 하나의 댓글만 달 수 있는 `@OneToOne` 관계라 하더라도 마찬가지이다.  

```java
@Entity
public class WebBoard {
    ...
    ...
    @OneToOne(mappedBy = "board")
    private WebReply reply;
}
```

> 위와 같이 설정할 경우 위에서 말한 연관관계용 테이블이 자동 생성됨으로 `@JoinColumn` 을 사용해 부모 엔티티의 외래키 지정을 해야한다.

반면 `@JoinColumn` 은 무조건 자식 엔티티에서 사용되는 것 이 아니다.  

`@ManyToOne`, `@OneToMany` 에 따라 주종 관계가 달라질 수 있다.  

```java
@Entity //주인
public class Member {
    @Id
    private String uid;
    private String upw;

    ...

    @OneToMany
    @JoinColumn(name = "member")
    List<MemberRole> roles;
}

@Entity //종속
public class MemberRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fno;

    private String roleName;
}
```

`Member`와 `Member`의 권한을 나타낼 `MemberRole` 테이블이다.  

당연히 `Member`가 주인이며 `MemberRole`은 `Member`테이블과 연관관계를 맺기위한 외래키 속성을 가지고 있어야 한다.  

이 외래키 속성을 `MemberRole`에 별도로 지정하진 않았지만 `Member`의 `@JoinColumn` 사용함으로 **자동 생성**된다.  

즉 `Member`의 `@JoinColumn`이 `MemberRole`의 `member`외래키 속성을 생성한다.  

반면애 아래와 같이 설정도 가능하다.  
테이블을 모두 지우고 다시 설정해보자.  

```java
@Entity //주인
public class Member {
    @Id
    private String uid;
    private String upw;

    ...

    @OneToMany(mappedBy = "member")
    List<MemberRole> roles;
}

@Entity //종속
public class MemberRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fno;

    private String roleName;
    
    @ManyToOne
    @JoinColumn(name = "member")
    Member member;
}
```

`@JoinColumn` 위치가 `MemberRole`로 이동했지만 생성된 테이블은 변한 것이 없다. `MemberRole`에 `Member`의 외래키 칼럼이 생성된다.  


즉 `@OneToMany`에서의 `@JoinColumn`은 **주인테이블에서** 종속테이블의 외래키 칼럼을 생성할 때 쓰이고  
`@ManyToOne`에서의 `@JoinColumn`은 **종속테이블에서** 주인테이블의 연관관계 설정을 위해 외래키 칼럼 생성할 때 쓰인다.  

또 `mappedBy`는 주인테이블에서 종속테이블을 참조하고 싶을때만 쓰이기에 **주인테이블에서만** 쓰인다.  

개인적으로 종속테이블에서 연관관계 설정을 직접 생성하는 `@ManyToOne`에서의 `@JoinColumn`을 많이 사용한다.  

그래야 단독으로 쿼리 메서드 생성 작업이 가능하다.  

게시글에 댓글을 추가할 경우 게시글을 검색한 후 리스트에 댓글을 추가한 뒤 다시 게시글을 저장하는 방법보단 바로 댓글을 저장하는게 수월하다, 

또한 `@Query` 어노테이션이나 `querydsl`을 통해 `SELECT` 할때에도 주인테이블의 조건적용이 수월하다.  

단점은 `@ManyToOne` 이나 `@OneToOne`의 검색조건이 `EAGER` 고정이기 때문에 `n+1` 문제가 쉽게 발생한다.  


### 양방향 참조관계 주의사항 - toString, jackson

서로가 서로를 참조하고 있는 관계이기 때문에 `@ToString`과 같은 어노테이션을 사용하거나 `jackson` 라이브러리도 자동 `json`객체 생성시에 무한루프가 발생할 수 있다.  

방지를 위해 적어도 한개이상 엔티티에서 아래와 같이 `exclude`를 통해 출력을 제외설정한다.  
`@ToString(exclude = "board")`  
`@JsonIgnore private Board board;`  

### 양방향 참조관계 주의사항 - 트랜잭션 처리 

양방향의 경우 부모 엔티티로 인해 자식 엔티티까지 변경되는 상황에는  
`@Transactional`, `cascade` 속성을 지정해야 한다.  

예를 들어 JPA로 `Board`객체를 `findById()` 같은 메서드로 읽어와 `OneToMany`관계의 `List<Reply>` 객체에 새로운 `Reply`를 추가/삭제하여  
`Board`객체를 `save()` 할 경우(Board 엔티티를 사용해서 Reply 엔티티 저장/삭제) `@Transactional`, `cascade` 속성을 지정해야 한다.  

단방향으로 Reply와 같은 하위 엔티티를 `insert`, `delete`하는 경우,  
예를 들어 `Reply`객체를 만들어 저장할 데이터를 집어넣고 `Board`관련 참조 칼럼 데이터(`id`)만 지정하여 저장할 경우에는 `@Transactional`, `cascade` 속성이 필요없다.  

```java
//편하지만 jpa 관점에 위배되는 코드 
public void insertReply() {
    Board board = new Board();
    board.setBno(1L);

    Reply reply = new Reply();
    reply.setReply("댓글1");
    reply.setReplyer("홍길동");
    reply.setBoard(board);
    replyRepo.save(reply);
}
```

## FetchType.EAGER, FetchType.LAZY   

부모테이블 입장에서 자식테이블 정보가 필요할경우와 그렇지 않을경우가 있다.  

게시글 리스트를 출력할 경우엔 댓글 정보는 필요없고  
게시글 상세정보를 출력할 경우 댓글 정보가 필요하다.  

지연로딩은 첫번쨰 상황인 부모테이블의 정보만 필요할 경우 사용하고  
즉시로딩은 두번째 상황인 모든테이블의 정보가 필요할 경우 사용한다.  


* `FetchType.EAGER` - 엔티티를 조회할때 연관된 엔티티도 같이 조회한다.  

* `FetchType.LAZY` - 연관된 엔티티를 실제 사용할때 조회한다.   

물론 지연로딩이 있는 이유는 성능때문. 기본값은 `LAZY`이다.  

`@OneToMany, @ManyToOne`의 `fetch` 속성을 사용해 지연로딩, 즉시로딩 설정이 가능.  

```java
@OneToMany(mappedBy = "board", fetch = FetchType.EAGER)
private List<Reply> replies;
```

당연히 `FetchType.EAGER` 를 사용하면 부가적인 코드를 생략할 수 있어 코드가 짧아지지만  

하나의 게시글 정보를 읽어올 경우 문제는 없지만 게시글 리스트를 댓글과 함께 읽어올 경우 문제가 발생한다.  

예로 게시글과 해당 게시글의 댓글 개수를 읽어올 경우  
`JOIN` 과 `COUNT()` 쿼리로 2개의 테이블의 조인하여 한번의 쿼리 수행으로 필요한 데이터를 가져올 수 있다.  

하지만 기존 쿼리 메서드 `findAllBy()` 로 읽어올 경우 `JOIN`을 사용하지 않기때문에 게시글 리스트를 얻는 쿼리를 호출하고 각 게시글에 대한 댓글들을 읽어오는 쿼리를 수행한다.  

> 지연로딩또한 `@Transactional` 어노테이션을 사용하면 즉시 로딩처럼 부모, 자식 엔티티 정보를 한번에 읽어올 수 있다, 하지만 쿼리호출을 여러번 하는 것은 똑같다.  

성능 문제를 해결하기 위해선 쿼리메서드를 사용하지 않고 **지연로딩과 `@Query` 어노테이션을 사용해 `JOIN` JPA쿼리 작성**을 하는 방법이 있다.  

## jpa N+1, paging

`ManyToOne`, `OneToOne` 관계에선 `innerJoin`, `leftJoin` 을 통해 `N+1` 문제와 `paging`
처리를 모두 할 수 있다.  

하지만 `OneToMany` 관계의 데이터까지 가져오려면 포기해야할 사항이 많다.  

1. 여러개의 OneToMany 속성에 대한 데이터를 같이 검색할 수 없다.  
   - SQL 특성상 2개의 `1:N`, `1:m` 관계의 테이블을 조인하게 되면 `n x m` 개 `Cartesian` 곱만큼 raw 가 반환된다.
2. 페이징 처리가 불가능하다.
   - `1:N` 테이블을 조인 할 경우 `n`개의 raw 가 출력되게 되며 raw 기반으로 페이징 처리하는 SQL 특성상 페이징이 불가능하다.


`JPQL` 의 경우 `DISTINCT` 키워드와 함께 `JOIN FETCH` 쿼리 시행  

```java
@Query("SELCET DISTINCT o FROM Order o JOIN FETCH o.orderMenus om WHERE o.customer.id = :id")
List<Order> findAllWithOrderMenus(Long id);
```

`QueryDsl` 의 경우 알아서 중복데이터를 객체로 만들지 않는다.
```java
QStoreMenu qStoreMenu = QStoreMenu.storeMenu;
QOrderMenu qOrderMenu = QOrderMenu.orderMenu;
return queryFactory.selectFrom(qOrder)
    //.limit(pageSize)
    //.offset(pageNum)
    .join(qOrder.orderMenus, qOrderMenu).fetchJoin()
    .join(qOrderMenu.storeMenu, qStoreMenu).fetchJoin()
    .join(qStoreMenu.store).fetchJoin()
    .where(qOrder.customerEnable.eq(true)
        .and(qOrder.customer.id.eq(customer.getId())))
    .fetch();
}
```

### paging 처리 딜레마

위의 두 방법으로 `N+1` 문제는 해결이 되나 페이징처리는 사용 불가능하다.  
`Pageable` 객체나 `orderBy`, `limit`, `offset` 3개의 메서드를 모두 사용해도 페이징 처리는 되지 않는다.  

`N+1` 문제를 해결하기 위해 `Order` 를 중복적으로 `OrderMenu` 만큼 가져오는 `inner join` 쿼리를 사용하며 그만큼 행의 길이(`Cartesian` 곱)가 늘어나기에 `paging` 처리 메서드를 삽입해도 정확한 값이 나오지 않는다.   

`paging` 메서드나 객체를 사용하고 싶다면 `OneToMany` 객체를 `join` 하는 쿼리를 포기해야 한다.  

```java
QStoreMenu qStoreMenu = QStoreMenu.storeMenu;
QOrderMenu qOrderMenu = QOrderMenu.orderMenu;
return queryFactory.selectFrom(qOrder)
    .limit(pageSize)
    .offset(pageNum)
    //.join(qOrder.orderMenus, qOrderMenu).fetchJoin()
    //.join(qOrderMenu.storeMenu, qStoreMenu).fetchJoin()
    //.join(qStoreMenu.store).fetchJoin()
    .where(qOrder.customerEnable.eq(true)
        .and(qOrder.customer.id.eq(customer.getId())))
    .fetch();
}
```

검색되는 데이터 양이 얼마 되지 않는다면 join 을,  
데이터 양이 많다면 페이징 처리를 사용하자

데이터 양도 많고 `join` 해야할 테이블도 많다면 `id`에 해당하는 칼럼만 `paging` 으로 가져온 후 해당 `id` 를 `select...in` 쿼리 문으로 `join` 해서 가져올 수 있다.  

## EntityManager concurrency

멀티 스레드 환경에서 `JPA`를 사용하다 보면 동시성 문제가 발생한다.  

* Thread1이 `DeviceRestartLog` 생성, redis와 같은 저장소에 Device번호와 logid를 저장  
* Thread2는 Device를 재시작하고 메세지로 실행되는 스레드이다. Device번호를 이용해 logid를 흭득 후 `DeviceRestartLog` 업데이트  
* Thread1은 Thread2가 성공적으로 일을 수행했는지 `Thread.sleep`으로 5초 후 DB에서 `findByLogid` 실행  

```java
// Thread1의 코드
deviceRestartLog.setId(logid);
deviceRestartLog.status(0); //0=restart 안됨
deviceRestartLogRepository.save(deviceRestartLog);
Thread.sleep(1000 * 5);
deviceRestartLog = deviceRestartLogRepository.findByLogId(String.valueOf(logid));
restartStatus = deviceRestartLog.getStatus();
if (restartStatus = 1) {
    logger.info("재시작 성공!");
} else {
    logger.error("재시작 실패!");
}
...
```

```java
// Thread2의 코드, 메세지 수신시 동작 
String device_id = message.getString("device_id");
Long logid = RedisUtil.get(device_id)
DeviceRestartLog deviceRestartLog = deviceRestartLogRepository.findByLogId(logid);
deviceRestartLog.setStatus(1);
deviceRestartLogRepository.saveAndFlush(deviceRestartLog);
```

2초후에 `Thread1`의 `findByLogId` 로 가져온 `deviceRestartLog` 가져온 값은 재시작이 성공해 DB에 이미 `Update` 되었다 하더라도 `old data`를 가져온다.  

기존의 `Entity`에 할당된 캐시를 비우고 DB에서 새로 값을 가져와 채워넣어줄 필요가 있다.  

이를 위해선 `EntityManager`의 `refresh` 메서드를 사용해야 하는데 불행이도 `Spring Data JPA`에선 별도의 커스터마이징을 통해 호출이 가능하다.  

> https://www.javacodegeeks.com/2017/10/access-entitymanager-spring-data-jpa.html

```java
public interface DeviceRestartCustomRepository {
    void refresh(DeviceRestartLog deviceRestartLog);
}
```

```java
import org.springframework.transaction.annotation.Transactional;

public class DeviceRestartCustomRepositoryImpl implements DeviceRestartCustomRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void refresh(DeviceRestartLog deviceRestartLog) {
        em.refresh(deviceRestartLog);
    }
}
```

```java
public interface DeviceRestartLogRepository extends JpaRepository<DeviceRestartLog, Long>, 
        QuerydslPredicateExecutor<DeviceRestartLog>, 
        DeviceRestartCustomRepository 
{
    DeviceRestartLog findByMessageId(String messageId);
}
```

그리고 검색해온 값을 `refresh`해주기만 하면 끝!  
```java
...
deviceRestartLog = deviceRestartLogRepository.findByMessageId(String.valueOf(msgId));
deviceRestartLogRepository.refresh(deviceRestartLog);
restartStatus = deviceRestartLog.getStatus();
...
```
