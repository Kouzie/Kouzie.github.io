---
title:  "Spring Boot - 스프링 부트 어노테이션!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - jpa
  - java

toc: true

---

## @Entity

해당 클래스의 인스턴스들이 엔티티 임을 명시 (name속성을 사용해 엔티티 이름 지정가능, 지정하지 않을시 클래스명을 이름으로 사용)

## @Id

엔티티가 식별키로 사용할 칼럼 지정

### @GeneratedValue

`@Id`칼럼과 같이 사용되는 어노테이션, 식별기 생성 방법을 지정한다.  

Mysql과 같은 DB에선 테이블에 기본키 생성기능이 있고 Oracle의 경우 시퀀스를 사용해 기본키를 지정한다.  

각종 생성 전략에 따라 `@GeneratedValue`어노테이션을 사용해 지정 가능하다.  

#### strategy

`@GeneratedValue`의 `strategy`속성에 다음 속성들을 설정 가능하다.  

속성값|설명
|---|---|
`AUTO` | 특정 DB에 맞게 자동으로 생성, Oracle의 경우 자동으로 SEQUENCE사용
`TABLE` | 기본키 생성방식 자체를 DB에 위임, Mysql에서 자주 사용
`SEQUENCE` | 시퀀스를 사용해서 식별키 생성, Oracle에서 자주 사용
`IDENTITY` | 별도의 키를 생성해주는 채번 테이블 사용

> Hibernate 5.0부터 MySQL의 `AUTO`는 `IDENTITY`가 아닌 TABLE을 기본 시퀀스 전략으로 선택된다. 즉 5.0 이전에는 `AUTO`를 사용, 이후부턴 `IDENTITY`를 사용해야한다.  

 

#### generator


`@GeneratedValue`의 `strategy`속성에에 따라 속성값도 변경된다.  
`strategy`값이 `SEQUENCE`일경우 `@SequenceGenerator`를 엔티티 위에 정의
`strategy`값이 `TABLE`일경우 `@TableGenerator`를 엔티티클래스 위에 정의

```java
@Entity
@TableGenerator(name="my_seq_table", table="SEQTB_USER", pkColumnValue="user_seq", allocationSize=1)
public class User{
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="my_seq_table")
    private Long uid;
     
    private String uname;
}
```

```java
@Entity
@SequenceGenerator(name = "my_seq", sequnceName = "SEQ_BOARD", initialValue = 1, allocationSize = 1)
public class Board {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
  private Long id;
}
```

## @Column

테이블의 속성이 엔티티 클래스의 필드에 맞춰 생성된다.  
필드에 각종 설정(속성크기, Unique, default값, NN, 수정, 삽입 금지)을 지정하고 싶다면
속성 | type | 설명 | 허용값
|---|---|---|---|
`name` | `String` | 칼럼 이름 지정 | 
`unique` | `boolean` | 유니크 여부 | `true, false`
`nullable` | `boolean` | 널 허용 여부 | `true, false`
`insertable` | `boolean` | insert 가능여부 | `true, false`
`updateable` | `boolean` | update 가능여부 | `true, false`
`table` | `String` | 테이블 이름 지정 | 
`length` | `int` | 칼럼 사이즈 지정 | `255`
`precision` | `int` | 소수 정밀도 | `0`
`scale` | `int` | 소수 자리수 지정 | `0`

insert, update 여부는 `테이블 연관관계가 있을경우(참조관계)` a테이블을 통해 b테이블의 값을 수정/생성 할건지 지정하는 어노테이션이다.

## @Valid - 칼럼에 대한 각종 제약조건 설정  

`@Column`어노테이션 만으로 제약조건 지정이 부족하다면 아래 어노테이션들을 사용한다.  

어노테이션|설명|사용예
|---|---|---|
`@AssertFalse` | 값이 무조건 false 여야함 | @AssertFalse<br>boolean isUnsupported;
`@AssertTrue` | 값이 무조건 true여야함 | @AssertTrue<br>boolean isActive;
`@DecimalMax` | 10진수 최대값이 n값 이하 실수여야함 | @DecimalMax("30.00")<br>BigDecimal discount;
`@DecimalMin` | 10진수 최소값이 n값 이하 실수여야함 | @DecimalMin("5.00")<br>BigDecimal discount;
`@Digits` | 정수와 실수 자리수 지정 | @Digits(integer=6, fraction=2)<br>BigDecimal price;
`@Future` | 해당날짜가 현재보다 미래여야함 | @Future<br>Date eventDate;
`@Past` | 해당 날짜가 현재보다 과거여야함 | @Past<br>Date birthday;
`@Max` | n값 이하여야함 | @Max(10)<br>int quantity;
`@Min` | n값 이상이어야함 | @Min(5)<br>int quantity;
`@NotNull` | 값이 null일수 없음 | @NotNull<br>String username;
`@Null` | 값이 null이어아햠 | @Null<br>String unusedString;
`@Pattern` | 정규식을 만족해야함 | @Pattern(regexp="(d{3})d{3}-d{4}")<br>String phoneNumber;
`@Size` | 최소크기, 최대크기를 지정 | @Size(min=2, max=240)<br>String briefMessage;

> http://www.thejavageek.com/2014/05/24/jpa-constraints/

## @CreateTimestamp, @UpdateTimestamp

모든 jpa가 사용하는 것이 아닌 `org.hibernate`에서 지원하는 어노테이션, 엔티티가 생성되거가 업데이트 되는 시점의 날짜를 기록하는 설정.  

## @Table

클래스 정의에 의해 테이블이 생성되기 때문에 테이블 관련 정보를 `@Table` 어노테이션으로 지정할 수 있다.  


속성 | type | 설명
|---|---|---|
`name` | `String` | 테이블 이름 지정
`catalog` | `String` | 테이블 카테고리 지정
`schema` | `String` | 스키마 지정
`uniqueConstraints` | UniqueConstraints[] | 칼럼값 유니크 제약 조건
`indexes` | Index[]  | 인덱스 생성

## @Query

쿼리메서드 외에 JPA정의에 따른 sql문을 작성하고 싶을때, 혹은 DB종속적인 native쿼리를 작성하고 싶을 때 사용하는 어노테이션   

```java
@Query("SELECT b.bno, b.title, b.writer, b.regdate, COUNT(r) " +
        "FROM Board b " +
        "LEFT OUTER JOIN b.replies r WHERE b.bno > 0 GROUP BY b ")
public List<Object[]> getListWithQuery(Pageable page);
```

### native

DB종속적인 쿼리를 작성하고 싶을 때 사용하는 속성  

```java
@Query(value = "SELECT b FROM tbl_board b WHERE b.content LIKE %:content% AND b.bno > 0 ORDER BY b.bno DESC"
       nativeQuery=true)
public List<Board> findByContent(@Param("content") String content);
```

### @Modifying, @Transactional

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
## @OneToOne, @OneToMany, @ManyToOne, @ManyToMany  

DB 연관관계 설정시 사용하는 어노테이션  

### targetEntity 속성  

관계를 맺을 Entity Class명 정의.


### cascade  

Entity의 변경에 대해 관계를 맺은 Entity도 변경 전략을 결정. 부모테이블에서 설정한다 (`@OneToMany`)

`CascadeType.PERSIST` -  부모를 영속화할 때 연관된 자식들도 함께 영속화 한다. 부모 객체 저장과 동시에 같이 설정된 자식객체도 저장.

`CascadeType.REMOVE` -  부모 객체 삭제시 연관된 자식들도 함께 삭제

`CascadeType.MERGE` -  병합 시에만 전이

`CascadeType.REFRESH` -  엔티티 매니저의 `refresh()`호출시 전이

`CascadeType.DETACH` -  부모 엔티티가 detach되면 자식도 detach

`CascadeType.ALL` -  위의 모든 사항을 포함, 가장 일반적으로 사용됨.

DB의 `ON DELETE CASCADE, UPDATE`

### fetch

관계 Entity의 데이터 읽기 전략을 결정합니다. 즉시로딩(Eager), 지연로딩(Lazy)가 있다.  

`FetchType.EAGER` - 엔티티를 조회할때 연관된 엔티티도 같이 조회한다.

`FetchType.LAZY` - 연관된 엔티티를 실제 사용할때 조회한다.  


### mappedBy

양방향 관계 설정시 관계의 주체가 되는 쪽에서 정의. 자식테이블의 외래키를 지정한다.  
```java
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
다대일, 다대다 관계를 설정하면 관계를 위한 다(Many)에 대한 정보를 저장하는 테이블이 추가 생성된다.   
단 방향에서는 `@JoinColumn`을 사용해 해결하였는데 


### orphanRemoval

`orphan` - 고아란 뜻으로 부모 엔티티와 연관관계가 끊어진 객체를 뜻한다.  

`CascadeType.REMOVE`와 같은 거의 같은 기능이다.  

둘다 JPA 문법이기에 DB 레이어인 `ON DELETE CASCADE`와는 관련이 없다.  

`CascadeType.REMOVE`와 `orphanRemoval=true`의 차이점은 자식객체를 `null`로 설정하고 저장했을 때 `orphanRemoval=true`는 삭제하고 `CascadeType.REMOVE`는 삭제하지 않는다.  

`remove()`의 경우 동일한 기능을 수행한다.  


### @ManyToOne - optional 속성  

해당 객체에 null 허용 여부, 반드시 값이 필요하다면 true 설정, 기본값은 true입니다.

### @JsonIgnore

Spring REST API를 사용하면 반환값이 JSON형식으로 변경되는데 양방향 참조관계에서 무한루프를 방지하기 위해 사용하는 어노테이션  


### @Transactional

`rollbackFor` - 특정 `Exception` 발생 시 `rollback` 하도록 설정

`noRollbackFor` - 특정 `Exception` 발생 시 `rollback` 하지 않도록 설정


## @CacheConfig, @CachePut, @Cacheable, @CacheEvict  

