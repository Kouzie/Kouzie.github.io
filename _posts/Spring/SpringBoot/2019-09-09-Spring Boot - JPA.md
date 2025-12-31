---
title:  "Spring Boot - JPA!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## JPA(Java Persisitence API)  

> 참고  
> <https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html>
> <https://www.datanucleus.org/products/accessplatform/index.html>
> <https://www.datanucleus.org/products/accessplatform/jpa/annotations.html>

데이터를 유지하고 관리하기위해 DB를 사용하고 java프로그램에서 DB와 연동하는 JDBC를 사용하였다.  

일반적으로 DB 연동시 **데이터베이스의 종속적인** 클래스를 정의하는 코딩을 하는데  
(테이블에 해당하는 vo객체를 정의하고 매핑)
반대로 **정의한 클래스에 코드에 종속적인** `ORM(Object Relation Mapping)` 시스템이 있다.  

`JPA`는 java 진영에서 ORM 시스템을 구축하기위한 표준 라이브러리  

여러 기업에서 `JPA`를 구현한 라이브러리를 제공하는데 `Hibernate`가 가장 유명하다.  

> (이외에도 `EclipseLink`, `DataNuclues` 등이 있음)

### Spring Data JPA, JPA, Hibernate

3가지 라이브러리 사용시 쉽게 혼동할 만한 내용을 잘 정리해둔 블로그  

> 출처: <https://suhwan.dev/2019/02/24/jpa-vs-hibernate-vs-spring-data-jpa>

![springboot_jpa_1](/assets/springboot/springboot_jpa_1.png)

> JPA 의 구현체가 Hibernate 임으로 동일선상에 있거나 위치가 변경되어도 무방  
> DIP 에 익숙해진 사용자를 위해 위 그림처럼 표현한듯  

Spring 사용시 단순 `Hibernate`를 사용해 개발할 일이 거의 없다.  
대부분 `Hibernate` 를 덮어씌운 `Spring Data JPA` 를 사용하며 특수한 경우에만 `Hibernate` 전용 함수를 사용한다.  

> Spring 이 아닌 다른 java 기반 프레임워크로 개발한다면 `Hibernate` 를 사용해 개발해야 할것

### 엔티티 매니저

`Hibernate`에서 여러 엔티티를 관리하는 객체  

`Spring Data JPA` 를 사용하면 엔티티 매니저를 직접적으로 사용할 일은 없지만  
`Spring Data JPA` 내부적으로 `Hibernate` 규약과 엔티티 매니저를 통해 데이터를 관리하기에 알아두어야함  

> 엔티티는 DB 테이블을 만들기위한 **객체로 만든 테이블 명세서(클래스) 혹은 생성된 객체(인스턴스)**, 둘다 혼용하여 말함 

Spring 에서 지원하는 기능으로 엔티티와 DB가 연동되어 데이터를 가져오고 수정되고 저장되고 삭제되는 과정을 담당하는 객체이다.  

![springboot2_1](/assets/springboot/springboot_jpa_2.png)

엔티티 매니저가 위 사진에 있는 여러 기능을 호출하면서 엔티티와 DB 간 CRUD를 처리한다.  

`New`: 실제 DB와 연동되지 않은 새로 생성된 엔티티

`Managed`(영속): 엔티티의 정보가 DB에 저장되어있고 메모리도 이와 **같은 상태로 존재하는 상태**(실제 데이터와 메모리상의 테이터가 일치), 이 공간은 영속 컨텍스트라 한다.

`Detached`(준영속): 영속 컨텍스트에서 엔티티를 꺼내와 각종 CRUD를 사용하는 상태, 아직 DB와는 연동되지 않아 **같은상태로 존재하지 않는다.**  
준영속 상태에 있는 객체들은 1차 캐시에서 검색되지도 않는다.  
`merge` 메서드는 1차캐시 혹은 DB 에서 다시 엔티티를 검색 한 후 준영속 엔티티의 정보로 채워넣는 과정이다.  

`Removed`: 더이상 사용하지 않아 삭제되고 영속 컨텍스트에서도 쫓겨난 상태  

위의 각 상태에서 실제 DB에서 데이터를 검색, 연동, 수정/삭제 작업을 하기위해 수많은 메서드를 호출하는 것이 사진에 보인다.  
`Spring Data JPA` 에선 어노테이션 몇개로 웬만한 작업은 처리 가능하다.  

## JPA Query

JPA 에서 각종 SQL 쿼리들을 어떻게 생성하는지 알아본다.  

### CrudRepository

각종 `Spring Data` 프로젝트들은(`redis, jdbc, mongo` 등) 이 `Repository` 패턴을 사용한다  
`Spring Data JPA` 에서는 `CrudRepository` 를 주로 사용  


아래와 같이 엔티티를 정의하고 `CRUD` 작업을 처리할 `Repository` 인터페이스를 정의하는 패턴을 가진다.  

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


public interface BoardRepository extends CrudRepository<Board, Long> {

}
```

> `CrudRepository`에는 기본적인 `CRUD` 를 위한 가상메서드가 정의되어 있다.  
> `save` 메서드를 통해 `update`, `insert` 가 가능하다.  

![springboot2_2](/assets/springboot/springboot_jpa_3.png)

`Spring Data JPA` 가 DB 에 맞춰 알맞은 쿼리를 정의한 구현체를 동적으로 생성해준다.  

간단한 `CRUD` 작업은 `CrudRepository`  
각종 조건문이 들어간 복잡한 쿼리는  `JPA Query Methods` 로 수행한다  

```console
find..by..
read..by..
query..by..
get..by..
count..by..
```

위와같은 패턴으로 JPA가 자동으로 만들어주는 쿼리메서드를 사용해 여러가지 조건문을 추가한다.    


![springboot2_3](/assets/springboot/springboot_jpa_4.png)

> JPA는 **refelection 이 극한까지 사용된 프로젝트** 라 할 수 있다.

> 참고: <https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation>

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

### 페이징 처리, 정렬 처리 - Pageable, Sort

페이징 처리시 위의 `CrudRepository` 와 쿼리메서드에 `org.springframework.data.domain.Pageable` 클래스를 사용 가능하다.  
위의 쿼리메서드 마지막필드에 `Pageable` 객체를 추가한다.  

```java
public interface BoardRepository extends CrudRepository<Board, Long> {
    //bno를 기준으로 내림차순 정렬 - bno > ? ORDER BY bno & paging
    List<Board> findByBnoGreaterThanOrderByBnoDesc(Long bno, Pageable paging); 
}
```

`Pageable`객체는 `PageRequest.of()` 메서드의 2가지 방법으로 생성 가능

```java
PageRequest.of(int page, int size)
PageRequest.of(int page, int size, Sort.Direction direction, String ..props)
```

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

`thymleaf` 와 같이 HTML템플릿 까지 지원하는 경우  
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
System.out.println("-------------------------------------------");

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

```console
Hibernate: select board0_.bno as bno1_0_, board0_.content as content2_0_, board0_.regdate as regdate3_0_, board0_.title as title4_0_, board0_.updatedate as updateda5_0_, board0_.writer as writer6_0_ from tbl_boards board0_ where board0_.bno>? order by board0_.bno desc limit ?
Hibernate: select count(board0_.bno) as col_0_0_ from tbl_boards board0_ where board0_.bno>?
Board(bno=200, title=제목...199, writer=user09, content=내용...199채우기, regdate=2019-08-08 17:01:36.0, updatedate=2019-08-08 17:01:36.0)
...
...
Board(bno=191, title=제목...190, writer=user00, content=내용...190채우기, regdate=2019-08-08 17:01:36.0, updatedate=2019-08-08 17:01:36.0)
-------------------------------------------
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

`Iterable` 구현 객체가 반환되며 여러가지 편리한 함수들도 포함되어 있다.  
알아서 `Count`까지 해서 `Page` 객체에 데이터를 넣어준다.  

> 편리한 만큼 오버헤드가 발생하여 REST 환경에선 잘 사용하지 않음  

### JPQL

복잡한 구조 (각종 join, 여러 조건문 등) 을 가질 경우 자동생성되는 쿼리메서드로는 한계가 있다.  

`@Query`어노테이션을 사용하면 각종 요청에 대처가능하다.  
> JPQL: 유사 SQL 문법으로 DB 종속을 피하면서 익숙한 쿼리문 작성이 가능.  

```java
@Query("SELECT b FROM Board b WHERE b.title LIKE %?1% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByTitle(String title);

@Query("SELECT b FROM Board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```

`?1`을 통해 첫번째 매개변수를 지정해 동적으로 검색조건을 변경 가능하고 `@Param`을 사용하면 변수 이름으로 지정가능하다.  

```java
@Query("SELECT b FROM #{#entityName} b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content);
```

`#{#entityName}`을 사용하면 혹시라도 테이블을 참조할 객체명이 변경되어도 대처가능하다.  

```java
@Query("SELECT b FROM #{#entityName} b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC")
public List<Board> findByContent(@Param("content") String content, Pageable paging);
```

페이징의 경우 `@Query` 어노테이션 상관 없이 페이징 처리 가능하다.  

```java
@Query(value = "SELECT b FROM tbl_board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC"
       nativeQuery=true)
public List<Board> findByContent(@Param("content") String content);
```

`nativeQuery` 속성을 사용하면 지정한 DB고유 문법을 사용할 수 있다.  
단 JPA의 장점인 DB 독립성은 사용할 수 없게된다.  

```java
public interface PDSBoardRepository extends CrudRepository<PDSBoard, Long> {
    
    @Modifying
    @Transactional
    @Query("UPDATE PDSFile f SET f.pdsfile = ?2 WHERE f.fno = ?1")
    public int updatePDSFile(Long fno, String newFileName);
}
```

`@Query`는 기본적으로 `SELECT`구문만을 지원하지만 `@Modifying` 어노테이션으로 `UPDATE, DELETE`구현이 가능하다.  

`@Modifying` 으로 변경된 값은 자동으로 영속성에 업데이트 된 정보가 들어가지 않는다.  
아래 옵션으로 쿼리 실행후 영속성을 비우거나 쿼리 실행전 flush 작업을 진행할 수 있다.  

- `flushAutomatically` : 쿼리 실행 전 쓰기 지연 저장소의 쿼리를 flush 하는 옵션
- `clearAutomatically` : 쿼리 실행 후 영속성 컨텍스트를 비우는 옵션

매개변수에 `Pageable` 객체와 같이 동작시켜도 정상적으로 동작한다.  

```java
@Query("SELECT b FROM Book b LEFT JOIN FETCH b.author")
List<Book> findAllWithPageable(Pageable pageable); // 이건 동작함
// select book0_.bno       as bno1_5_0_,
//        ...
//        author1_.ano     as ano1_3_1_,
//        ...
// from tbl_book book0_
//  left outer join tbl_author author1_ on book0_.author_id = author1_.ano
// limit ? offset ?
```

`Page` 객체를 반환하는 JPQL 은 동작하지 않는다.  

```java
@Query("SELECT b FROM Book b LEFT JOIN FETCH b.author")
Page<Book> findAllWithPageableResult(Pageable pageable); // 이건 컴파일에러
```

`Fetch join + Paging` 에선 `CountQuery` 를 별도로 만들어 줘야한다.

```java
@Query(value = "SELECT b FROM Book b LEFT JOIN FETCH b.author",
        countQuery = "SELECT COUNT(b.bno) FROM Book b")
Page<Book> findAllWithPageableResult(Pageable pageable);
```

### JPA Custom DTO

> <https://stackoverflow.com/questions/36328063/how-to-return-a-custom-object-from-a-spring-data-jpa-group-by-query>

엔티티 클래스가 아닌 직접 정의한 클래스에에 DB에서 읽어온 값을 삽입, 반환하고 싶다면 아래코드 참고  

```java
@Getter
@Setter
@AllArgsConstructor
public class SurveyAnswerStatistics {
  private String answer;
  private Long   cnt;
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

**클래스 풀 네임**을 사용해 반환할 수 있다.  

`nativeQuery` 를 사용해야 한다면 아래처럼 적용 가능, 단 `getter`, `setter` 메서드를 모두 정의해주어야 한다.  

```java
public interface SurveyRepository extends CrudRepository<Survey, Long> {
    @Query(value =
        "SELECT v.answer AS answer, COUNT(v) AS cnt " + 
        "FROM Survey v " + 
        "GROUP BY v.answer", nativeQuery = true)
    List<SurveyAnswerStatistics> findSurveyCount();
}
```

### 1차 캐시(First Level Cache, L1 Cache)

- 영속성 컨텍스트(Persistence Context) 내부에 존재하는 캐시
- 엔티티 매니저(EntityManager)가 관리하는 트랜잭션 범위의 캐시
- JPA를 사용하면 자동으로 설정되며, 별도의 설정 없이 사용 가능
- 트랜잭션이 시작되면 생성되고, 트랜잭션이 종료되면 함께 소멸

1. **조회 동작**  
   - `EntityManager.find()` 또는 `Repository.findById()` 호출 시 먼저 1차 캐시에서 엔티티를 조회  
   - 1차 캐시에 없으면 DB를 조회하고, 조회한 엔티티를 1차 캐시에 저장  
   - 이후 같은 트랜잭션 내에서 동일한 엔티티 조회 시 DB 조회 없이 1차 캐시에서 반환  
1. **동일성 보장(Identity)**  
   - 같은 트랜잭션 내에서 같은 ID로 조회한 엔티티는 항상 같은 인스턴스  
   - 이를 통해 데이터 일관성과 메모리 효율성 확보  
2. **쓰기 지연(Write Behind)**  
   - 엔티티를 저장하거나 수정해도 즉시 DB에 반영되지 않음  
   - 트랜잭션 커밋 시점에 한 번에 DB에 반영 (Flush)  
   - `save()` 메서드 호출 시 1차 캐시에 저장만 하고, 실제 INSERT 쿼리는 커밋 시 실행  
3. **변경 감지(Dirty Checking)**  
   - 1차 캐시에 저장된 엔티티의 변경사항을 자동으로 감지  
   - 트랜잭션 커밋 시 변경된 엔티티에 대해 UPDATE 쿼리 자동 실행  
   - 별도의 `update()` 메서드 호출 없이도 엔티티 수정 가능  

```java
// 첫 번째 조회: DB 쿼리 실행
Member member1 = memberRepository.findById(1L); // SELECT 쿼리 실행
// 두 번째 조회: 1차 캐시에서 조회 (DB 쿼리 없음)
Member member2 = memberRepository.findById(1L); // 쿼리 실행 안됨
// 동일성 보장
System.out.println(member1 == member2); // true
```

```java
@Transactional
public void saveMember() {
    Member member = new Member();
    member.setName("홍길동");
    memberRepository.save(member); // 1차 캐시에 저장, DB 쿼리 아직 실행 안됨
    // 트랜잭션 커밋 시점에 INSERT 쿼리 실행
}
```

```java
@Transactional
public void updateMember(Long id) {
    Member member = memberRepository.findById(id).get();
    member.setName("이름변경"); // 엔티티 수정
    
    // memberRepository.save() 호출 불필요
    // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행
}
```

5. **생명주기**
   - 트랜잭션 시작 시 생성
   - 트랜잭션 종료 시 소멸
   - 각 트랜잭션마다 독립적인 1차 캐시 생성
   - 멀티스레드 환경에서는 각 스레드마다 별도의 영속성 컨텍스트와 1차 캐시 보유

**장점**

- 같은 트랜잭션 내에서 반복 조회 시 DB 접근 최소화로 성능 향상
- 동일성 보장을 통한 데이터 일관성 유지
- 변경 감지로 명시적인 UPDATE 쿼리 작성 불필요

**단점**

- 트랜잭션 범위를 벗어나면 캐시가 사라짐
- 다른 트랜잭션과 캐시 공유 불가
- 메모리 사용량 증가 가능성 (대량의 엔티티 조회 시)

#### 영속성 concurrency

멀티 스레드 환경에서 `JPA`를 사용하다 보면 1차 캐시(영속성 컨택스트)의 동시성 문제가 발생한다.  

- `Thread1` 이 `DeviceRestartLog` 생성
  `Thread.sleep`으로 5초 후 DB에서 `findByLogid` 실행하여 재실행결과 확인  
- `Thread2` 는 `Device` 로부터 재실행 결과 수신, 재실행 결과 로그를 `DeviceRestartLog` 에 업데이트  

```java
// Thread2의 코드, 기기 재부팅 메세지 수신시 동작
Integer status = message.getStatus(); // 기기 재부팅 결과
Long logid = message.getLogid(); // 기기 재부팅 로그아이디
DeviceRestartLog deviceRestartLog = deviceRestartLogRepository.findByLogId(logid);
deviceRestartLog.setStatus(1);
deviceRestartLogRepository.saveAndFlush(deviceRestartLog);
```

```java
// Thread1의 코드
Long logid = curruntTimeMills();
DeviceRestartLog deviceRestartLog = new DeviceRestartLog()
deviceRestartLog.setId(logid);
deviceRestartLog.setDeviceId(deviceId);
deviceRestartLog.status(0); //0=restart 안됨
deviceRestartLogRepository.save(deviceRestartLog);
messageSender.sendMessage(new RestartMessage(logid, deviceId)); // 기기 재부팅 진행
// 5초후 재실행 결과 확인, 5초 동안 Thread2 코드가 실행됨
Thread.sleep(1000 * 5);
deviceRestartLog = deviceRestartLogRepository.findByLogId(String.valueOf(logid));
restartStatus = deviceRestartLog.getStatus();
if (restartStatus = 1) {
    logger.info("재시작 성공!");
} else {
    logger.error("재시작 실패!");
}
```

재시작이 성공해 DB에 정상적으로 `Update` 되었다 하더라도  
`Thread1` 이 `sleep` 후 가져온 값은 기존의 `old data` 이다.  
(JPA 영속성 개념으로 인해 select 쿼리가 발생하지 않는다)  

영속 컨텍스트에 저장된 기존 `Entity` 캐시를 비우고 DB에서 새로 값을 가져와 채워넣어줄 필요가 있다.  
이를 위해선 `EntityManager`의 `refresh` 메서드를 사용해야 하는데  
`Spring Data JPA` 에선 별도의 커스터마이징을 통해 호출이 가능하다.  

> 참고: <https://www.javacodegeeks.com/2017/10/access-entitymanager-spring-data-jpa.html>

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
DeviceRestartCustomRepository {
    DeviceRestartLog findByMessageId(String messageId);
}
```

그리고 검색해온 값을 `refresh` 해주기만 하면 끝!  

```java
// 5초후 재실행 결과 확인
Thread.sleep(1000 * 5);
deviceRestartLog = deviceRestartLogRepository.findByMessageId(String.valueOf(msgId));
deviceRestartLogRepository.refresh(deviceRestartLog);
restartStatus = deviceRestartLog.getStatus();
...
```

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = 'INACTIVE' WHERE u.lastLogin < :cutoffDate")
    void deactivateOldUsers(LocalDateTime cutoffDate);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM User u WHERE u.lastLogin < :cutoffDate")
    void deleteOldUsers(LocalDateTime cutoffDate);
}
```

### 2차 캐시(Second Level Cache, L2 Cache)

**개념 및 특징**

- 애플리케이션 레벨에서 공유하는 캐시
- 여러 트랜잭션과 엔티티 매니저 간에 공유 가능
- 애플리케이션이 종료될 때까지 유지
- Hibernate에서 제공하는 기능이나, 실제 프로덕션에서는 잘 사용하지 않음

**동작 방식**

1. **조회 흐름**
   - 엔티티 조회 시 1차 캐시 → 2차 캐시 → DB 순서로 조회
   - 2차 캐시에 있으면 DB 조회 없이 반환
   - DB에서 조회한 엔티티는 1차 캐시와 2차 캐시 모두에 저장

```
조회 요청
  ↓
1차 캐시 확인
  ↓ (없으면)
2차 캐시 확인
  ↓ (없으면)
DB 조회
  ↓
1차 캐시 + 2차 캐시에 저장
```

2. **캐시 영역**
   - 엔티티 캐시: 엔티티 객체 자체를 저장
   - 컬렉션 캐시: 연관관계 컬렉션을 저장
   - 쿼리 캐시: 쿼리 결과를 저장

**설정 방법**

1. **의존성 추가** (예: EhCache 사용 시)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-ehcache</artifactId>
</dependency>
```

2. **application.yml 설정**

```yaml
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_second_level_cache: true
          use_query_cache: true
        region:
          factory_class: org.hibernate.cache.ehcache.EhCacheRegionFactory
```

3. **엔티티에 캐시 활성화**

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Member {
    // ...
}
```

**캐시 전략(CacheConcurrencyStrategy)**

- `READ_ONLY`: 읽기 전용, 수정 불가
- `READ_WRITE`: 읽기/쓰기 가능, 동시성 제어 포함
- `NONSTRICT_READ_WRITE`: 읽기/쓰기 가능, 약한 일관성 보장
- `TRANSACTIONAL`: 트랜잭션 범위에서 동시성 제어

**사용 시나리오**

- 읽기 작업이 많은 경우
- 변경이 거의 없는 마스터 데이터
- 복잡한 조인 쿼리 결과

**주의사항**

- 동시성 문제: 여러 트랜잭션에서 동시 수정 시 데이터 일관성 문제 발생 가능
- 메모리 관리: 잘못된 설정 시 메모리 부족 발생 가능
- 캐시 무효화: 데이터 변경 시 캐시 무효화 전략 필요

**실무에서의 사용**

- 대부분의 경우 Hibernate 2차 캐시 대신 **애플리케이션 레벨의 캐시** 사용
- **Redis**, **EhCache**, **Caffeine** 등을 직접 사용하여 캐시 구현
- 이유:
  - 더 세밀한 캐시 제어 가능
  - 분산 환경에서의 캐시 공유 용이
  - 캐시 전략의 유연성
  - JPA와의 결합도 감소

**Hibernate 2차 캐시 vs 애플리케이션 레벨 캐시**

| 구분 | Hibernate 2차 캐시 | 애플리케이션 레벨 캐시 (Redis 등) |
|------|------------------|--------------------------------|
| 범위 | JPA/Hibernate에 종속 | 프레임워크 독립적 |
| 설정 | JPA 설정에 포함 | 별도 설정 필요 |
| 제어 | JPA가 자동 관리 | 개발자가 직접 제어 |
| 분산 | 어려움 | 용이 (Redis Cluster 등) |
| 유연성 | 제한적 | 높음 |

**참고 자료**
> <https://docs.jboss.org/hibernate/orm/6.2/userguide/html_single/Hibernate_User_Guide.html#caching>  

## 기본 어노테이션

JPA 가장 기본적인 어노테이션의 속성들 설명  

### @Table

속성 | type | 설명
|---|---|---|
`name` | `String` | 테이블 이름 지정
`catalog` | `String` | 테이블 카테고리 지정
`schema` | `String` | 스키마 지정
`uniqueConstraints` | `UniqueConstraints[]` | 칼럼값 유니크 제약 조건
`indexes` | `Index[]`  | 인덱스 생성

DB 예약어의 경우 테이블명으로 사용할 수 없는데 아래처럼 `name` 속성으로 문자열로 처리할 수 있다.  

> 웬만하면 예약어는 피하는것을 권장

```java
@Table(name = "[order]")
```

### @Column

속성 | type | 설명 | 허용값
|---|---|---|---|
`unique` | `boolean` | 유니크 여부 | default `false`
`nullable` | `boolean` | 널 허용 여부 | default `true`
`insertable` | `boolean` | `insert` 가능여부 | default `true`
`updateable` | `boolean` | `update` 가능여부 | default `true`
`name` | `String` | 칼럼 이름 지정 |
`table` | `String` | 연관 테이블 이름 지정 |
`length` | `int` | 칼럼 사이즈 지정 | `255`
`precision` | `int` | 소수 정밀도 | `0`
`scale` | `int` | 소수 자리수 지정 | `0`

### @CreationTimestamp, @UpdateTimestamp, @CreatedDate, @LastModifiedDate

`@CreationTimestamp`, `@UpdateTimestamp` 의 경우 `org.hibernate`에서 지원하는 어노테이션, `VM date` 의 시간값을 사용해 값을 기록한다.  

> VM date: 어플리케이션 서버 시간이지만, 추가적으로 동기화 가능.  

```java
@CreationTimestamp
private LocalDateTime createTime;
@UpdateTimestamp
private LocalDateTime updateTime;
```

```java
// Hibernate 6.0.0 부터 SourceType 지정 가능
@CreationTimestamp(source = SourceType.DB)
private Instant createdOn;
@UpdateTimestamp(source = SourceType.DB)
private Instant lastUpdatedOn;
```

`@CreatedDate`, `@LastModifiedDate` 의 경우 `spring data` 에서 지원하는 어노테이션, 어플리케이션 서버의 시간값을 사용해 값을 기록한다.  

`spring data` 에서 제공하는 어노테이션이 좀 더 범용적이지만 `@EnableJpaAuditing`, `@EntityListeners(AuditingEntityListener.class)` 설정이 필요함으로 편한거 사용하면 된다.  

MySQL 의 경우 Instant, ZoneDateTime 같은 offset 관련 시간객체의 경우 TIMESTAMP 로 저장하며, LocalDateTime 같은 경우 DATETIM 형태로 저장한다.  

- DATETIME  
  시간대 정보 없이 날짜와 시간을 저장. Hibernate는 UTC 시간 값을 저장하지만 변환은 서버가 아닌 애플리케이션에서 처리.
- TIMESTAMP  
  UTC로 저장되며, MySQL이 서버 시간대를 기준으로 변환 처리. 조회 시 서버의 시간대 설정에 따라 시간 반환.
  MySQL 의 TIMEZONE 설정을 변경하면 결과값이 다르게 나올 수 있다.  

### @Inheritance, @DiscriminatorValue, @DiscriminatorColumn

상속관계에 있는 객체를 DB에서 사용하기위한 어노테이션  

1. `InheritanceType.SINGLE_TABLE` [default]  
2. `InheritanceType.JOINED`  
3. `InheritanceType.TABLE_PER_CLASS`  

부모클레스에 `@DiscriminatorColumn(name = "image_type")`, 정의하지 않으면 필드 기본이름은 `DTYPE`  
하위클레스에 `@DiscriminatorValue("value")` 지정, 부모클래스 구분 컬럼에 저장할 값을 지정  

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "image_type")
@Table(name = "image")
public abstract class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_path")
    private String path;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
}

@Entity
@DiscriminatorValue("II")
public class InternalImage extends Image {
    ...
}
```

```sql
create table image
(
    image_id    int auto_increment primary key,
    image_path  varchar(255) null,
    product_id  varchar(50)  null,
    upload_time datetime     null
    image_type  varchar(10)  null,
)
```

### @Access, @Transient

`@Access` 어노테이션 사용하면 DB데이터 매핑전력을 지정할 수 있다.  
필드에 접근시킬 지 `getter` 를 통해 접근시킬 지 지정할 수 있다.  

- `@Access(AccessType.FIELD)`  
  필드에 직접 접근, Class 위에 선언, private field 접근 가능    
- `@Access(AccessType.PROPERTY)`  
  getter 를 통해 접근

별도로 정의하지 않았을 경우 `@Id` 어노테이션이 필드에 정의되어 있는지, getter 메서드에 정의되어 있는지에 따라 매핑전략이 결정된다.  

대부분 필드에 `@Id` 를 지정하기에 `@Access(AccessType.FIELD)` 를 사용한다.  


```java
@Entity
public class Member {
    // 기본적으로 @Access(AccessType.FIELD) 전략 사용
    @Id  
    private String id;

    @Transient
    private String firstName;

    @Transient
    private String lastName;

    @Access(AccessType.PROPERTY)
    public String getFullName() {
        return firstName + lastName;
    }
}
```

`getFullName` 에 의해 DB 에 `fulle_name` 칼럼이 생성되고 매핑된다.  

`@Transient` 는 민감하거나 필요없는 정보의 경우 DB 에 저장하고 싶지 않을때 사용.

### @Id, @GeneratedValue, @GenericGenerator

식별키 생성 방법을 지정한다.  

속성값|설명
|---|---|
`AUTO` | 아래 3개중 DB에 맞게 자동으로 생성  
`TABLE` | 기본키 생성방식 자체를 DB에 위임  
`SEQUENCE` | 시퀀스를 사용해서 식별키 생성, `Oracle`에서 자주 사용  
`IDENTITY` | 별도의 키를 생성해주는 채번 테이블 사용, `MySQL`에서 자주 사용  

같은 DB 더라도 버전별로 시퀀스 식별키 생성방법이 달라 `GenerationType.AUTO` 는 혼란을 야기할수 있어 사용을 권장하지 않는다.  
실제 `MySQL 8.0` 이상버전에 `AUTO` 사용시 아래와 같은 시퀀스용 테이블을 만들어 `insert`, `select`, `update` 를 반복한다.  

```sql
create table hibernate_sequence
(
    next_val bigint
) engine = InnoDB;

insert into hibernate_sequence values (1)
select next_val as id_val from hibernate_sequence for update
update hibernate_sequence set next_val= ? where next_val=?
```

보통 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 를 사용하여 `Id` 생성전략을 `DataSource` 에 맡긴다.  
MySQL 의 경우 테이블의 auto increment 기능을 사용한다.  

```java
@Getter
@Entity(name = "t_account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "account_id")
    private Long accountId;
    private String userName;
    private OffsetDateTime createTime;
}
```

`strategy`값이 `TABLE` `SEQUENCE` 일 경우 별도의 테이블을 생성해야 함으로  
`@SequenceGenerator`혹은 `@TableGenerator` 를 엔티티클래스 위에 정의한다.  

```java
@Entity
@TableGenerator(name="my_seq_table", table="SEQTB_USER", pkColumnValue="user_seq", allocationSize=1)
public class User{
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="my_seq_table")
    private Long uid;
     
    private String uname;
}

@Entity
@SequenceGenerator(name = "my_seq", sequnceName = "SEQ_BOARD", initialValue = 1, allocationSize = 1)
public class Board {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
  private Long id;
}
```

이외에도 `@GeneratedValue` 를 사용해서 `UUID`, `Sequence ID` 등을 사용할 수 있다.  

```java
@Entity
class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String status;
    private String number;
}
```

직접 커스텀한 ID 생성방식이 있다면 아래와 같이 `@GenericGenerator` 를 사용하면 된다.  

```java
@Getter
@Entity(name = "t_account")
public class AccountEntity {
    @Id
    @GeneratedValue(generator = "customIdGenerator") // @GenericGenerator의 name modifier 에 지정한 이름
    @GenericGenerator(name = "customIdGenerator",
            type = CustomIdGenerator.class)
    @Column(name = "account_id")
    private Long accountId;
    private String userName;
    private OffsetDateTime createTime;
}
```

```java
public class CustomIdGenerator implements IdentifierGenerator {

    // snowflake 는 @Bean 으로 등록
    private final Snowflake snowflake;

    public CustomIdGenerator(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        // 원하는 ID 생성 로직을 구현합니다.
        // 여기서는 UUID를 예로 사용합니다.
        return snowflake.nextId();
    }
}
```

#### Persistable

Persistable 인터페이스는 `@GeneratedValue` 를 통해 `Id` 를 생성할 수 없을 때 사용할만하다.  
`@Id` 어노테이션은 객체의 영속성 역할에도 영향을 끼친다, `@GeneratedValue` 로 생성된 Id 라면 `INSERT` 쿼리가 호출되겠지만, 아래처럼 `@Id` 어노테이션만 설정하고 초기화 했을 때 `INSERT` 요청일지 `UPDATE` 요청일지 구분할 수 없다.  

```java
@Entity
public class CustomEntity {
    @Id
    private Long id;
    ...
}
```

`managed` 영역에 이미 `persist`(영속)되어 있다면 `UPDATE` 요청을 하겠지만,  
new 연산자를 통해 생성한 상태라면 `INSERT` 해야할지 `UPDATE` 해야할 지 모르기 때문에 `SELECT` 를 먼저 한번 수행한다.  

`@GeneratedValue` 를 사용하는 것이 정석이지만 `Persistable` 인터페이스를 구현시켜 `isNew` 속성을 지정해주어 영속화 되지 않는 객체도 `INSERT, UPDATE` 구분할 수 있다.

```java
import org.springframework.data.domain.Persistable;

@Getter
@Entity
public class CustomEntity implements Persistable<Long> {
    
    @Id
    private Long id;
    
    @Setter
    private boolean isNew;

    public CustomEntity(Long id) {
        this.id = id;
        this.isNew = true;  // 새로운 엔티티임을 명시적으로 설정
    }
}
```

## 연관관계 어노테이션

> <https://www.datanucleus.org/products/accessplatform/jpa/annotations.html>

위에선 하나의 테이블을 예제로 객체를 정의하였지만 실제 서비스를 구성할때에는 거미줄처럼 테이블간의 연관관계가 구성되어있다.  
총 4가지로 엔티티간의 관계를 구성한다.  

1. `@OneToOne`  
2. `@OneToMany`  
3. `@ManyToOne`  
4. `@ManyToMany`  

이런 참조관계는 유동적으로 지정할 수 있다.  

`Board`, `Reply` 엔티티가 있을때 당연히 게시글당 `N` 개의 댓글이 달리니 `N:1` 관계가 구성된다.  

게시글 입장에선 `@OneToMany`이고  
댓글 입장에선 `@ManyToOne` 이다.  

> 그렇다 해서 무조건 두 클래스다 서로를 가리키고 있을 필요는 없다, 오히려 단방향 매핑을 권장함  
> 일반적으로 `@OneToMany`의 양방향 혹은 단방향 참조관계가 대부분이다.  

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

    // 양방향 매핑, @JsonIgnore 로 서로간 serialize 제한
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;
}
/* 
create table tbl_replies
(
    rno        bigint not null auto_increment,
    regdate    datetime,
    reply_text varchar(255),
    replyer    varchar(255),
    updatedate datetime,
    board_bno  bigint,
    primary key (rno)
) engine = InnoDB
*/
```

replies 의 DDL 에 `board_bno` 필드가 지정된것 확인  

일반적으로 `Board` 를 부모엔티티, `Reply` 를 자식엔티티 라 칭한다.  

> Board 는 존재하더라도 Reply 는 없을 수 있음으로  
> FK 각 생성되는 쪽을 자식엔티티라 함  

### 추가 테이블 생성이슈

`...ToMany` 어노테이션을 별도의 설정없이 사용시 **관계를 표시하기 위한 추가 테이블이 생성된다**  

> 자식테이블의 외래키 설정없이 생성되기에 추가테이블이 생성됨  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long bno;
    ...

    /*
    아래 ddl 이 추가됨
    create table tbl_boards_replies
    (
        board_bno   bigint not null,
        replies_rno bigint not null
    ) engine = InnoDB
    */
    @OneToMany
    private List<Reply> replies;
}
```

아래 방법으로 추가테이블 생성문제를 막을 수 있다.  

1. `...ToOne` 에서 `@JoinColumn`  
2. `...ToMany` 에서 `@JoinColumn`  
3. `...ToMany`with mappedBy

일반적으로 DDD 나 객체지향 관점에서 개발을 많이하기에 주로 `부모 -> 자식` 기준으로 개발을 진행함  
2, 3 방법이 주로 사용된다.  

`@JoinColumn` 은 단방향 매핑에서 사용하고  
`@OneToMany`(with mappedBy) 은 양방향 매핑에서 사용한다.  

#### ...ToOne 에서 @JoinColumn

`...ToOne` 은 기본적으로 자식엔티티에서 사용됨  
FK 역시 자식엔티티에 생성됨  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long bno;
    ...
    // 별도 어노테이션 필요없음
}

public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;

    private String replyText;
    private String replyer;

    @CreationTimestamp
    private LocalDateTime regdate;
    @UpdateTimestamp
    private LocalDateTime updatedate;

    // 양방향 매핑, @JsonIgnore 로 서로간 serialize 제한
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "my_bno")
    private Board board;
}
```

`Board` 의 PK 를 가져와 `my_bno` 필드명으로 FK 설정한다.  

#### ...ToMany 에서 @JoinColumn

`...ToMany` 는 기본적으로 부모엔티티에서 사용됨  
FK 는 설정한 자식엔티티에 생성됨  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long bno;
    
    ...

    @OneToMany
    @JoinColumn(name = "my_bno")
    private List<Reply> replies;
}

public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;
    // 별도 어노테이션 필요없음
    ...
}
```

#### @OneToMany with mappedBy

기존 방식대로 `@OneToMany` 에 `mappedBy` 속성 사용  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    
    ...
    
    @OneToMany(mappedBy = "board")
    private List<Reply> replies;
}

public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;
    
    ...
    
    @JsonIgnore
    @ManyToOne
    private Board board;
}
```

#### 테이블 생성처리 - @JoinTable  

역으로 테이블을 생성이 필요하고 추가설정을 해야한다면 `@JoinTable` 사용

```java
@OneToMany
@JoinTable(name = "b_r_table",
    joinColumns = @JoinColumn(name = "bno"),
    inverseJoinColumns = @JoinColumn(name = "rno"))
private List<Reply> replies;
```

#### 주의사항

양방향 매핑 관계에선 `@ToString`, `json` 변환같이 `serialize` 코드에서 무한루프가 발생할 수 있다.  

`@JoinColumn` 으로 `부모 -> 자식` 관계만을 사용하는것을 권장하며  
반드시 양방향 연관관계로 엔티티를 구성해야할 경우 아래와 같이 `serialize` 제외설정 필수  

- `@ToString(exclude = "board")`  
- `@JsonIgnore private Board board;`  

### @OneToOne 관계매핑

> `@OneToOne` 사용 자체를 권장하지 않는다.  

`@OneToOne` 은 관계 특성상 양방향 매핑일 수 밖에 없다, 부모객체와 자식객체에 모두 `@OneToOne` 필드를 정의해야 한다.  
이떄 아무런 설정을 하지 않을경우 두 엔티티에 서로 FK 가 생성된다.  

`mappedBy` 속성을 사용하면 양방향 매핑,
`@JoinColumn` 을 사용하면 단방향 매핑이 가능하다.  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    ...

    @OneToOne(mappedBy = "board", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Thumbnail thumbnail;
}

public class Thumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tno;

    ...

    @OneToOne
    @JoinColumn(name = "my_bno")
    private Board board;
}
```

`@JoinColumn` 을 사용하는 엔티티가 자식엔티티가 된다.  

`@OneToOne` 사용시 부모엔티티에서 단방향 매핑하는 방법은 없다.  
**자신 엔티티에만 `[@JoinColumn, @OneToOne]` 어노테이션을 지정하면 단방향 매핑이 된다고 생각할 수 있지만 `@ManyToOne` 처럼 동작하기에 `@OneToOne` 이라 할 수 없다.**  

### FetchMod, N + 1 문제


```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    ...
    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER)
    private List<Reply> replies;
}

public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;
    ...
    @JsonIgnore
    @ManyToOne
    private Board board;
}
```

지연로딩, 즉시로딩을 지정할 수 있으며 default 설정은 아래와 같다.  

1. `@OneToOne` - EAGER
2. `@ManyToOne` - EAGER
3. `@OneToMany` - LAZY
4. `@ManyToMany` - LAZY

`...ToOne` 등을 가진 엔티티에서 **리스트 조회** 할 경우 자동으로 조인쿼리가 발생할것 같지만 JPA 가 알아서 조인쿼리를 생성하진 않으며 `N+1` 문제가 발생한다.  
`LAZY` 로 설정하더라도 참조가 이루어지는 코드, `serialize` 과정에서 `N+1` 이슈가 발생할 수 있다.  

아래와 같이 `FETCH JOIN` 을 사용하는것을 권장한다.  

```java
@Query("SELECT b FROM Book b LEFT JOIN FETCH b.author")
List<Book> findAllWithFetch();
```

#### Cartessian Product 이슈

`...ToMany(fetch = FetchType.EAGER)` 을 사용할 때 `LEFT OUTER JOIN` 을 통해 `1:N` 관계 엔티티를 조회하기에 N개의 row 를 검색하는 쿼리를 수행한다.  

`...ToMany(fetch = FetchType.EAGER)` 필드가 2개 이상일경우 `Cartesian Product` 이슈가 발생 가능하다.  

#### 테이블 풀스캔 이슈

이번엔 반대로 `OneToMany` 관계 주인인 `Author` 에서 `FETCH JOIN` 과 `pageable` 을 사용해보자.  

> 1:N 관계 페이징처리는 SQL 문법으로 불가능하다, 중복된 author 가 출력된 row 를 페이징처리하려 하기 때문.  

```java
@Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
List<Author> findAllWithPageable(Pageable pageable);
```


동작하긴 하지만 마법같은 SQL 문이 발생하는 것이 아니라 Author 와 Book 을 조인한 **테이블 풀스캔** 쿼리 실행 후 Java 메모리 내에서 Author 기준으로 distinct 처리 + 페이지 슬라이싱처리하여 반환한다.  

### 영속성, cascade(전이전략)

> 참고: <https://www.baeldung.com/jpa-cascade-types>
> `Oracle`, `Mysql` 등 DDL 에서 `on delete cascade` 편의기능과는 관련없다.  

`EntityManager` 가 제공하는 `[New, Managed, Detached, Removed]` 상태의 전이를 `[OneToMany, ManyToOne]` 관계 객체에게 적용하기 위한 설정.  

![springboot2_1](/assets/springboot/springboot_jpa_2.png)

- **New**: 엔티티를 생성자를 통해 생성만 한 상태  
- **Managed**: `persist()` 를 통해 영속 컨텍스트에 저장해둔 상태,  
  트랜잭션이 완료(flush)되면 DB 에 쿼리가 발생한다.  
- **Removed**: `remove()` 를 통해 영속 컨텍스트에서 삭제된 상태,  
- 트랜잭션이 완료(flush)되면 DB 에 쿼리가 발생한다.  
- **Detached**: `detached(), clear()` 를 통해 준영속(영속 컨텍스트에서 분리)된 상태,  
  `merge()` 를 통해 준영속에서 업데이트된 객체를 영속 컨텍스트에서 찾아 병합시킨다.  

`default cacade` 는 빈 배열 형태, 아래 설정을 진행할 수 있음.  

- **CascadeType.PERSIST**: 부모 엔티티 영속화시 때 연관된 자식들도 함께 영속화.  
- **CascadeType.MERGE**: 부모 엔티티 병합시 연관된 자식들도 함께 병합.  
- **CascadeType.REMOVE**: 부모 객체 삭제시 연관된 자식들도 함께 삭제.  
- **CascadeType.REFRESH**: 엔티티 매니저의 `refresh()`호출시 전이.  
- **CascadeType.DETACH**: 부모 객체 detach() 함수 호출시 연관객체도 준영속 진행.  
- **CascadeType.ALL**: 위의 모든 사항을 포함, 가장 일반적으로 사용됨.  

> CascadeType defaults to the empty array  

`Board` 와 단방향으로 설정된 `Reply` 등의 자식엔티티를 같이 `insert`, `delete` 해야하는 경우  

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

### orphanRemoval

`CascadeType.REMOVE`와 같은 거의 같은 기능, 부모객체가 삭제되면 `orphanRemoval` 또한 같이 삭제된다.  

차이점은 자식엔티티를 `null`로 설정하고 저장했을 때 `orphanRemoval`는 삭제하고  
`CascadeType.REMOVE`는 삭제하지 않는다.  

> 부모 엔티티에서 의도적으로 연결관계를 끊었을 때 고아 객체로 인식되어 `orphanRemoval` 된다.  

다음과 같이 하나는 `REMOVE`, 하나는 `orphanRemoval` 를 설정해서 테스트  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "bno")
    private List<Reply> replies;

    @OneToMany(cascade = {CascadeType.PERSIST}, orphanRemoval = true)
    @JoinColumn(name = "bno")
    private List<Attachment> attachments; // 첨부파일

    public void testOrphan() {
        this.replies.clear();
        this.attachments.clear();
    }
}
```

`deleteById` 로 삭제 진행시 아래와 같이 `update & delete` 쿼리가 진행된다.  

```sql
update attachment set bno=null where bno=?
update tbl_replies set bno=null where bno=?
delete from attachment where ano=?
delete from attachment where ano=?
delete from tbl_replies where rno=?
delete from tbl_replies where rno=?
delete from tbl_boards where bno=?
```

아래와 같이 `List clear` 후 저장진행시 `replies` 는 `update` 만,  
`attachments` 는 `update & delete` 쿼리가 진행된다.  

> `clear()` 함수로 **같은 참조에 데이터가 지워져야** 정상동작함을 주의

```java
@PatchMapping("/test/orphan/{id}")
public Board testOrphan(@PathVariable Long id) {
    Board board = boardService.findById(id);
    board.testOrphan();
    board = boardService.save(board);
    return board;
}
```

```sql
update attachment set bno=null where bno=?
update tbl_replies set bno=null where bno=?
delete from attachment where ano=?
delete from attachment where ano=?
```

`replies` 도 `update` 로 인해 `board` 와 연결이 끊겼기 때문에 더이상 조인쿼리를 통해 검색되지 않는다.  

### @OrderColumn

`...ToMany` 를 사용할경우 필드로 `List` 와 같은 컬렉션을 사용하는데  
`@OrderColumn` 을 추가하면 순서가 있는 객체로 인식한다(DB에 순서값을 저장)  

```java
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "bno")
    @OrderColumn // replies_order 로 Order 필드 생성됨
    private List<Reply> replies;
}
```

`name` 속성으로 이름 지정 가능, 인덱스는 0부터 시작한다.  

## 밸류 매핑 어노테이션

엔티티 클래스 내부에서 사용자정의 클래스를 멤버변수로 사용할경우  
반드시 연관된 엔티티일 필요는 없다.  

필요에 따라 사용자정의 클래스를 단순 밸류타입으로 원시타입처럼 엔티티에서 사용 가능하다.  

### @Convert

아래 인터페이스의 구현체로 DB 에 저장될 원시타입과 Object 를 변환해줌  

`X` 타입이 `Object`  
`Y` 타입이 원시타입  

```java
public interface AttributeConverter<X,Y> {
    public Y convertToDatabaseColumn (X attribute);
    public X convertToEntityAttribute (Y dbData);
}
```

구현체에 `@Converter(autoApply = true)` 지정시 엔티티 `property`에 `@Convert` 어노테이션 없이도 자동으로 변환설정됨

```java
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Money money) {
        return money == null ? null : money.getValue();
    }

    @Override
    public Money convertToEntityAttribute(Integer value) {
        return value == null ? null : new Money(value);
    }
}
```

### @EmbeddedId

단일키를 가독성을 높이기 위해 클래스화시키거나 복합키를 구현해야할 때 사용  

`@Embeddable` 과 함께 `Serializable` 의 구현체여야함  

```java
@Entity
@Table(name = "purchase_order")
public class Order {
    @EmbeddedId
    private OrderId number;
}

@Embeddable
public class OrderId implements Serializable {
    @Column(name = "order_number")
    private String number;
}
/*
create table purchase_order
(
    order_number varchar(255) not null,
    primary key (order_number)
) engine = InnoDB
*/
```

아래처럼 복합키를 사용해야할 경우에도 사용할 수 있다.  

```java
@Entity
@Table(name = "purchase_order")
public class Order {
    @EmbeddedId
    private OrderId number;
}

@Embeddable
public class OrderId implements Serializable {
    @Column(name = "order_number")
    private String number;

    @Column(name = "order_date")
    private String date;
}
/*
create table purchase_order
(
    order_date   varchar(255) not null,
    order_number varchar(255) not null,
    primary key (order_date, order_number)
) engine = InnoDB
*/
```

```java
public interface OrderRepository extends JpaRepository<Order, OrderId> {
    List<Order> findByOrderIdIn(List<OrderId> ids);
    // SELECT * FROM purchase_order
    // WHERE (number, date) IN (("1", "20230918"), ("1", "20230714"));
}
```

### @Embedded

벨류객체를 사용해야할 때 사용  

`@Embeddable` 어노테이션과 함께 사용하여  
order 내의 각종 사용자정의 클래스를 DB 테이블에 평탄화시킬 수 있음  

```java
@Entity
@Table(name = "purchase_order")
public class Order {
    @EmbeddedId
    private OrderNo number;

    @Embedded
    private ShippingInfo shippingInfo;
}

@Embeddable
public class ShippingInfo {
    @Embedded
    private Address address;

    @Embedded
    private Receiver receiver;

    @Column(name = "shipping_message")
    private String message;
}

@Embeddable
public class Address {
    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;
}

@Embeddable
public class Receiver {
    @Column(name = "receiver_name")
    private String name;
    @Column(name = "receiver_phone")
    private String phone;
}
/* 
create table purchase_order
(
    order_number     varchar(255) not null,
    address1         varchar(255),
    address2         varchar(255),
    zip_code         varchar(255),
    shipping_message varchar(255),
    receiver_name    varchar(255),
    receiver_phone   varchar(255),
    primary key (order_number)
) engine = InnoDB
*/
```

`@AttributeOverrides` 를 사용하면 엔티티에서 DDL 필드명을 별도로 지정가능하다  

```java
@Embeddable
public class ShippingInfo {
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
        @AttributeOverride(name = "address1", column = @Column(name = "shipping_addr1")),
        @AttributeOverride(name = "address2", column = @Column(name = "shipping_addr2"))
    })
    private Address address;

    @Embedded
    private Receiver receiver;

    @Column(name = "shipping_message")
    private String message;
}
```

> 여러 엔티티클래스에서 동일한 `@Embeddable` 객체를 사용할 때 필드명 헷갈림 방지용으로 사용

### @CollectionTable, @ElementCollection

`@Embeddable` 을 사용하는 **벨류타입이지만** `1:N`, `M:N` 매핑을 구성할 때  
`@Convert` 사용하기에는 데이터가 너무 많을때 별도의 테이블로 나눠야 한다.  

벨류타입에서 `@OneToMany`, `@ManyToMany` 방식보단 `@CollectionTable` 을 사용하는게 간결할 수 있다.  

> 사용법은 `...ToMany` 와 비슷함

```java
@Embeddable
public class OrderLine {
    @Embedded
    private ProductId productId;

    @Column(name = "price")
    private Integer price;

    @Column(name = "quantity")
    private int quantity;
}

@Entity
@Table(name = "purchase_order")
public class Order {

    @EmbeddedId
    private OrderId number;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "order_line", joinColumns = @JoinColumn(name = "order_number"))
    private List<OrderLine> orderLines;
}
/*
create table order_line
(
    order_number varchar(255) not null,
    price        integer,
    product_id   varchar(255),
    quantity     integer
) engine = InnoDB

create table purchase_order
(
    order_number varchar(255) not null,
    primary key (order_number)
) engine = InnoDB
*/
```

`order_line` 에는 별도의 `primary key` 가 생기지 않음으로 벨류타입 정의에 합리적이다.  

### @Enumerated

`enum` 타입저장시 지정하여 사용가능.  
지정하지 않을경우 enum 객체의 정의 순서에 따라 int 값이 저장됨.  

```java
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String uname;
    private String upw;

    @Enumerated(EnumType.STRING) 
    private MemberRole role;
}
```
