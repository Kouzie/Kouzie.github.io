---
title:  "Spring Boot - 스프링 부트 JPA!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true

---

## JPA(Java Persistent API)

데이터를 유지하고 관리하기위해 db를 사용하고  
java프로그램에서 db와 연동하는 jdbc를 사용하였다.  

여기서 발생하는 수많은 중복코드 (jdbc 연동 및 dto, vo객체에 데이터 매핑)을 제거하기 위해 Mybatis와 같은 매퍼도 사용한다.  

위의 설명은 모두 데이터베이스의 종속적인 (테이블에 해당하는 vo객체를 정의하고 매핑) 코딩이었다.  

반대로 개발자가 정의한 객체에 종속적인 (Object Relation Mapping ORM)적인 시스템을 구축하려면 어떻게 해야할까?  

`Java Persistent API`를 사용하면 된다!  

JDBC는 자바와 데이터베이스를 연결할 때 어떤 데이터베이스이던 간에  
개발자는 인터페이스에 제공하는 메서드를 사용하여 똑같은 방법으로 각종 DB에 연결이 가능하다.  
추상화를 이용하여 이렇게 구현가능하였다.  

JPA는 이에 더해 어떤 데이터베이스이던간 똑같은 방법으로 VO에 저장된 데이터를 DB에 저장하고 업데이트하고 검색, 삭제할 수 있다.  

JPA는 일종의 기술로 여러 기업에서 JPA를 구현한 라이브러리를 제공하는데 Hibernate가 가장 유명하다.  
(이외에도 EclipseLink, DataNuclues 등이 있음)

JPA를 개발하려면 지존엔 아래와 같은 코드가 필요했다.  




## 엔티티, 엔티티 매니저

`사원`이라는 테이블이 있다면 사원번호, 사원명, 생년월일, 주민번호와 같은 속성들이 존재할 것이고 이러한 정보를 가진 레코드들이 여러개 있을것이다.  

JPA에서 엔티티는 `사원` 테이블을 만들기 위한 일종의 객체이다(명세서같은)  

즉 엔티티는 테이블을 만들기위한 정보를 담고있는 객체이며 DB에서 데이터를 저장하기 위한 용도로도 사용되는 객체이다. (DTO, VO와 비슷하게 생겼다)  

엔티티는 위의 2가지 의미를 모두 가지고 있으며 자주 혼용하기 때문에 헷갈리지 않도록 주의...

엔티티 매니저란 여러 엔티티를 관리하는 객체이다.  
스프링 부트에서 지원하는 기능으로 엔티티가 DB와 연동되어 데이터를 가져오고 수정되고 저장되고 삭제되는 과정을 담당하는 객체이다.  


![springboot2_1]({{ "/assets/springboot/springboot2_1.png" | absolute_url }}){: .shadow}  

엔티티 매니저가 위 사진에 있는 여러 기능을 호출하면서 실제 java객체를 DB에 연동하고 CRUD작업을 처리한다.  

New : 실제 DB와 연동되지 않은 기존 java객체

Managed(영속) : 엔티티의 정보가 DB에 저장되어있고 메모리도 이와 **같은 상태로 존재하는 상태**(실제 데이터와 메모리상의 테이터가 일치), 이 공간은 영속 컨텍스트라 한다.

Detached(준영속) : 영속 컨텍스트에서 엔티티를 꺼내와 각종 CRUD작업을 사용하는 상태, 아직 DB와는 연동되지 않아 **같은상태로 존재하지 않는다.**   

Removed : 더이상 사용하지 않아 영속 컨텍스트에서 쫓겨난 상태, 

위의 각 상태에서 실제 DB에서 데이터를 검색, 연동, 수정/삭제 작업을 하기위해 수많은 메서드를 호출하는 것이 사진에 보인다.  

스프링 프레임워크를 사용하지 않고 이를 처리하려면 코드가 복잡하겠지만 스프링 부트에선 어노테이션 몇개로 웬만한 작업은 처리 가능하다.  

## Spring Boot DB연결


![springboot1_0]({{ "/assets/springboot/springboot1_0.png" | absolute_url }}){: .shadow}   

위의 라이브러리를 추가했다면 DB연동을 위한 Datasource설정을 진행한다.  
```properties
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

## JPA Repository 설계

먼저 테이블로 설계를 위한, 데이터를 담기위한 객체를 하나 생성.  

```java
@Getter
@Setter
@ToString
@Table(name="tbl_boards")
@Entity
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

위의 클래스로 CRUD 작업을 처리하게 되는데 이를 위한 Repository인터페이스를 정의해야한다.  

```java
public interface BoardRepository extends CrudRepository<Board, Long> {

}
```


`CrudRepository` 인터페이스를 상속하는데 `CrudRepository`에는 기본적인 CRUD를 위한 가상메서드가 정의되어 있다.  

![springboot2_2]({{ "/assets/springboot/springboot2_2.png" | absolute_url }}){: .shadow}  

save 메서드를 통해 update, insert가 가능하다.  

`BoardRepository`를 구현한 클래스를 작성할 필요는 없다.  
springboot가 db에 맞춰 알맞은 쿼리를 정의한 구현체를 동적으로 생성해준다.  

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

## JPA 쿼리작성 - 쿼리메서드

간단한 CRUD작업은 `CrudRepository`를 참조한 `Repository`객체를 정의하는 것만으로 수행할 수 있는건 알겠다.  

하지만 DB를 사용하려면 간단한 쿼리로는 불가능하기에 개발자가 생각하는 복잡한 쿼리도 JPA로 수행할 수 있어야 한다.  

```
find..by..
read..by..
query..by..
get..by..
count..by..
```

위와같은 패턴으로 JPA가 자동으로 만들어주는 쿼리메서드를 사용해 여러가지 조건문을 추가할 수 있다.  

![springboot2_3]({{ "/assets/springboot/springboot2_3.png" | absolute_url }}){: .shadow}  


> https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation

<table class="tableblock frame-all grid-all fit-content">
<caption class="title">Table 3. Supported keywords inside method names</caption>
<colgroup>
<col>
<col>
<col>
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Keyword</th>
<th class="tableblock halign-left valign-top">Sample</th>
<th class="tableblock halign-left valign-top">JPQL snippet</th>
</tr>
</thead>
<tbody>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>And</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameAndFirstname</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname = ?1 and x.firstname = ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Or</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameOrFirstname</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname = ?1 or x.firstname = ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Is,Equals</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstname</code>,<code>findByFirstnameIs</code>,<code>findByFirstnameEquals</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname = ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Between</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateBetween</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate between ?1 and ?2</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeLessThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &lt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>LessThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeLessThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &lt;= ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeGreaterThan</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>GreaterThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeGreaterThanEqual</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age &gt;= ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>After</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateAfter</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate &gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Before</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByStartDateBefore</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.startDate &lt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeIsNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age is null</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IsNotNull,NotNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAge(Is)NotNull</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age not null</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Like</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameNotLike</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname not like ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>StartingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameStartingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound with appended <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>EndingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameEndingWith</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound with prepended <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Containing</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameContaining</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.firstname like ?1</code> (parameter bound wrapped in <code>%</code>)</p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>OrderBy</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeOrderByLastnameDesc</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age = ?1 order by x.lastname desc</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>Not</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByLastnameNot</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.lastname &lt;&gt; ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>In</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeIn(Collection&lt;Age&gt; ages)</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age in ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>NotIn</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByAgeNotIn(Collection&lt;Age&gt; ages)</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.age not in ?1</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>True</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByActiveTrue()</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.active = true</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>False</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByActiveFalse()</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where x.active = false</code></p></td>
</tr>
<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>IgnoreCase</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>findByFirstnameIgnoreCase</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>… where UPPER(x.firstame) = UPPER(?1)</code></p></td>
</tr>
</tbody>
</table>



### 페이징 처리, 정렬 처리 - Pageable, Sort

특정 페이지의 데이터만 가져오고 싶을때 `org.springframework.data.domain.Pageable` 클래스를 사용하면 편하다.  

```java
public interface BoardRepository extends CrudRepository<Board, Long> {

  public List<Board> findByBnoGreaterThanOrderByBnoDesc(Long bno, Pageable paging); 
  //bno를 기준으로 내림차순 정렬 - bno > ? ORDER BY bno
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

사용예  
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

### Page<T> 타입

프로젝트 크기가 크지 않다면 서버에서 페이징 처리까지 모두 해주는 Page 클래스를 사용하면 뷰에서 페이지를 출력하기 편하다.   

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

## @Query, @Param

아무래도 sql문보다 쿼리메서드가 덜 익숙할 수 밖에 없다.  

또한 sql의 조건문 작성시 일반 쿼리문보다 까다로운데 `@Query`어노테이션을 사용하면 편하다.  

`@Query`어노테이션으로 실제 DB종속적 쿼리는 아니지만 sql쿼리를 작성할 수 있다.

```java
@Query("SELECT b FROM Board b WHERE b.title LIKE %?1% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByTitle(String title);

@Query("SELECT b FROM Board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```

`?1`을 통해 첫번째 매개변수를 지정해 동적으로 검색조건을 변경 가능하고 `@Param`을 사용하면 변수 이름으로 지정가능하다.  

`Board`라는 테이블은 실제 DB안의 테이블 정보가 아닌 JPA를 통해 만들어질 객체명을 사용한다.  

```java
@Query("SELECT b FROM #{#entityName} b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```
> `#{#entityName}`을 사용하면 혹시라도 테이블을 참조할 객체명이 변경되어도 대처가능하다.  
Repository의 엔티티타입을 자동으로 매핑하기 때문, 아래 Repository의 엔티티 타입은 `Board`  
`public interface BoardRepository extends CrudRepository<Board, Long>`   

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


### @Modifying, @Transactional, @Commit


`@Query`는 기본적으로 `SELECT`구문만을 지원하지만 `@Modifying` 어노테이션으로 `UPDATE, DELETE`구현이 가능합니다.  

`@Modifying`을 통해 DML 사용시 해당 쿼리 메서드를 사용하려면 `@Transactional` 어노테이션 처리 필요.  

그리고 `@Transactional`의 경우 자동 롤백처리 됨으로 `@Commit`어노테이션 설정으로 자동 `commit`설정  
```java
public interface PDSBoardRepository extends CrudRepository<PDSBoard, Long> {
    @Modifying
    @Query("UPDATE PDSFile f SET f.pdsfile = ?2 WHERE f.fno = ?1")
    public int updatePDSFile(Long fno, String newFileName);
}


@RunWith(SpringRunner.class)
@SpringBootTest
@Log
@Commit
public class PDSBoardTest {
    @Autowired
    PDSBoardRepository repo;

    @Transactional
    @Test
    public void testUpdateFileName() {
        Long fno = 1L;
        String newName = "updatedFile1.doc";
        int count = repo.updatePDSFile(fno, newName);
        log.info("update count: " + count);
    }
}
```



## Querydsl - 동적sql처리

> http://www.querydsl.com/static/querydsl/4.0.1/reference/ko-KR/html_single/  


변수는 `@Param`, `?1` 기능을 통해 동적으로 지정가능하지만 조건문의 경우도 동적으로 추가할 수 있어야 한다.  
`Querydsl`을 사용하면 처리할 수 있다, 먼저 아래 `dependency`, `plugin`을 추가한다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
...
...
    <dependencies>
        ...
        ...
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-apt</artifactId>
            <version>4.1.4</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.querydsl</groupId>
            <artifactId>querydsl-jpa</artifactId>
            <version>4.1.4</version>
        </dependency>
    </dependencies>
        
    <build>
        <plugins>
            ...
            ...
            <plugin>
                <groupId>com.mysema.maven</groupId>
                <artifactId>apt-maven-plugin</artifactId>
                <version>1.1.3</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>process</goal>
                        </goals>
                        <configuration>
                            <outputDirectory>target/generated-sources/java</outputDirectory>
                            <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

Intellij의 경우 pom.xml에 우클릭후 아래 그림처럼 클릭

![springboot2_4]({{ "/assets/springboot/springboot2_4.png" | absolute_url }}){: .shadow}  

그럼 pom.xml에 지정했던 대로 `/generated-sources/java` 패키지에 새로 클래스가 정의된다.  

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

## 객체간 연관관계

위에선 하나의 테이블을 예제로 객체를 정의하였지만 정상적으로 사용하려면 거미줄처럼 테이블간의 연관관계가 구성되어있다.  

JPA에선 총 4가지로 관계를 구성한다.  

1. `@OneToOne`   
2. `@OneToMany`   
3. `@ManyToOne`   
4. `@ManyToMany`   

이런 참조관계는 유동적으로 지정할 수 있다.  
Board와 Reply라는 클래스(테이블 정의)가 있을때 당연히 게시글당 n개의 댓글이 달리니 `@OneToMany`혹은 `@ManyToOne` 관계가 구성된다.  
게시글 입장에선 `@OneToMany`이고 댓글 입장에선 `@ManyToOne` 이다.  

그렇다 해서 무조건 두 클래스다 서로를 가리키고 있을 필요는 없다. `Member`를 통해 댓글을 가져와야 한다면 참조해야 하지만 만약 쿼리를 2번 호출해 각각 가져온다면 서로 참조관계일 필요는 없다.  

Reply역시 댓글을 검색할 때 게시글 정보도 가져오고 싶다면 참조관계로 클래스를 정의하면 되지만 쿼리를 2번 호출할경우 혹은 게시글 정보를 필요로 하지 않을경우 굳이 참조관계를 구성할 필요는 없다.  

하지만 일반적으로 `OneToMany`의 양방향 혹은 단방향 참조관계가 대부분이다.  

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

## 테이블 추가 생성 문제 - @JoinColumn, @JoinTable, mappedBy

단방향이던 양방향이던 별다른 어노테이션이나 속성 설정 없이 
`@ManyToOne`, `@OneToMany` 어노테이션을 사용해서 관계 설정시 관계를 표시하기 위한 테이블이 추가로 생성된다.  

이는 자식테이블의 외래키 설정이 없기때문에 생성되는데  
단방향의 경우 `@JoinColumn`으로 자식테이블 외래키 칼럼을 설정하고  
양방향의 경우 부모테이블의 `@OneToMany`의 `mappedBy` 설정으로 지정가능하다.  

만약 테이블을 만들어야 하고 그에대한 설정을 하고 싶다면 `@JoinTable` 어노테이션 사용  
> http://wonwoo.ml/index.php/post/834




### 양방향 참조관계 주의사항

서로가 서로를 참조하고 있는 관계이기 때문에 `@ToString`과 같은 어노테이션을 사용해 코드를 자동으로 만들면 무한루프가 발생할 수 있다.  

> board에서 toString으로 reply를 출력하고, reply에서 toString으로 board를 출력....(반복)

방지를 위해 적어도 한개이상 엔티티에서 아래와 같이 `exclude`를 통해 출력을 제외설정한다.  
`@ToString(exclude = "board")`  

`@ToString` 어노테이션 외에도 `@JsonIgnore`가 필요하다. 
객체를 반환하거나 model객체에 포함시킬때 json으로 변환하면서 무한루프가 발생하기 때문에 참조관계에서 제외시켜 주어야 한다.  
```java
@JsonIgnore
private Board board;
```

또한 양방향의 경우 부모 엔티티로 인해 자식 엔티티까지 변경되는 상황에는 `@Transactional`, `cascade` 속성을 지정해야 한다.  

예를 들어 JPA로 Board객체를 `find..By..()`메서드로 읽어와 안의 `List<Reply>`객체에 새로운 Reply를 추가한다던지 기존 Reply를 삭제하여 다시 Board를 `save()` 할 경우 `@Transactional`, `cascade` 속성을 지정해야 한다. (Board 엔티티를 사용해서 Reply 엔티티 저장/삭제)

> 단방향으로 Reply와 같은 하위 엔티티를 insert, delete하는 경우, 예를 들어 Reply객체를 만들어 저장할 데이터를 집어넣고 Board관련 참조 칼럼 데이터만 지정하여 저장, `@Transactional`, `cascade` 속성이 필요없다.  
```java
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

> 오해하기 쉬운게 참조관계를 설정했다 하더라도 조회시 `JOIN`을 사용하지는 않는다.   
> Board정보, Reply정보를 따로따로 `SELECT`호출한다.  

## 지연로딩(Lazy), 즉시로딩(Eager)  

부모테이블(주 테이블) 입장에서 자식테이블 정보가 필요할 수 도, 필요하지 않을 수 도 있다.  
게시판을 예로 들어 게시글 목록 출력시 자식테이블인 댓글 정보는 필요 없고, 게시글 상세정보 출력시 자식 테이블 정보가 필요하다.  

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

하나의 게시글 정보를 읽어올 경우 문제는 없지만 게시글 리스트를 댓글과 함께 읽어올 경우 문제가 발생한다.  

게시글의 댓글 개수를 읽어올 경우 `JOIN`과 `COUNT()`쿼리로 한번의 쿼리 수행으로 데이터를 가져올 수 있다.  

하지만 기존 쿼리 메서드 `find..By()`로 읽어올 경우 JOIN을 사용하지 않기때문에 Board List를 얻는 쿼리를 호출하고 각 Board에 대한 댓글 개수를 읽어오는 쿼리를 수행한다.  

> 지연로딩또한 `@Transactional` 어노테이션을 사용하면 즉시 로딩처럼 부모, 자식 엔티티 정보를 한번에 읽어올 수 있다, 하지만 쿼리호출을 여러번 하는 것은 똑같다.  

성능 문제를 해결하기 위해선 쿼리메서드를 사용하지 않고 **지연로딩과 `@Query`어노테이션을 사용해 `JOIN` JPA쿼리 작성**을 하는 방법이 있다.  

