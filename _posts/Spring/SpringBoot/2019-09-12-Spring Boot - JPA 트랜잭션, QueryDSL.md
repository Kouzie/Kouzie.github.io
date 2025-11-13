---
title:  "Spring Boot - JPA 트랜잭션, Criteria, QueryDSL!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## 트랜잭션

**논리적인 하나의 작업단위가 트랜잭션**이다.  

위의 `Order` 와 `OrderLine` 이 같이 `insert` 되는 것도 하나의 작업단위이기에 하나의 트랜잭션이라 할 수 있다.  

![springboot2_1](/assets/springboot/springboot_jpa_5.png)  

구현체별로 다르겠지만 JPA 사용시 `JpaTransactionManager` 을 사용할 것  

`TransactionManager` 을 직접사용할 경우는 없지만 내부적으로 결국 `TransactionManager` `commit()`, `rollback()` 을 사용해 트랜잭션 처리가 진행되는 구조이다.  

```java
Connection conn = null;
try {
    conn = ConnectionProvider.getConncection();
    conn.setAutoCommit(false);
    // TODO Somthing
    conn.commit();
} catch (Exception e) {
    JdbcUtil.rollback(conn);
    throw new RuntimeException(e);
} finally {
    JdbcUtil.close(conn);
}
```

### 트랜잭션 전파

**트랜잭션 전파**란 **특정 A프랜잭션이 처리되는 과정 안에서 또다른 B트랜잭션 이 처리되는 경우** 에러가 발생할 경우 각 트랜잭션에 에러전파 하는것을 뜻한다.  

**전파방식** | **의미**
|---|---|
`REQUIRED(default)` | 트랜잭션 상황에서 실행되어야 한다. 진행 중인 트랜잭션이 있다면 이 트랜잭션에서 실행된다. 없는 경우에는 트랜잭션이 새로 시작된다.
`MANDATORY` | 호출 전에 반드시 진행 중인 트랜잭션이 존재해야 한다. 진행 중인 트랜잭션이 존재하지 않을 경우 예외 발생
`REQUIRED_NEW` | 자신만의 트랜잭션 상황에서 실행되어야 한다. 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션은 해당 메소드가 반환되기 전에 잠시 중단된다.
`SUPPORTS` | 진행 중인 트랜잭션이 없더라도 실행 가능하고, 트랜잭션이 있는 경우에는 이 트랜잭션 상황에서 실행된다.
`NOT_SUPPORTED` | 트랜잭션이 없는 상황에서 실행 만약 진행 중인 트랜잭션이 있다면 해당 메소드가 반환되기 전까지 잠시 중단한다.
`NEVER` | 트랜잭션 진행 상황에서 실행 될 수 없다. 만약 이미 진행 중인 트랜잭션이 존재하면 예외 발생
`NESTED` | 이미 진행 중인 트랜잭션이 존재하면 중첩된 트랜잭션에서 실행되어야 함을 나타낸다. 중첩된 트랜잭션은 본 트랜잭션과 독립적으로 커밋되거나 롤백될 수 있다. 만약 본 트랜잭션이 없는 상황이라면 이는 `REQUIRED`와 동일하게 작동한다. 그러나 이 전파방식은 DB 벤더 의존적이며, 지원이 안되는 경우도 많다.  

트랜잭션 전파방삭에 따라 A, B 의 `rollback` 결정이 달라진다.  

### @Transactional

`@Transactional` 어노테이션이 있으면 `Spring AOP` 가 알아서 `TransactionManager` 기반으로 `commit`, `rollback` 을 진행한다.  
`@Transactional` 어노테이션을 사용하는 메서드에서 데이터 소스에 접근하는 쿼리를 실행할 때 락이 걸린다.  

- `rollbackFor`: 특정 `Exception` 발생 시 `rollback` 하도록 설정
- `noRollbackFor`: 특정 `Exception` 발생 시 `rollback` 하지 않도록 설정

`@Transactional` 은 모든 예외발생시 `rollback` 하지 않고 `RuntimeException`, `Error` 를 상속한 예외 발생시에만 `rollback` 한다.  
위 전제조건을 토대로 상황에 맞게 `rollbackFor`, `noRollbackFor` 을 사용한다.  

- `propagation`: 위 트랜잭션 전파 참고하여 설정, `Propagation.REQUIRED` 가 default  
- `isolation`: 트랜잭션 격리레벨 설정, `Isolation.DEFAULT` 가 default  

> `Isolation.DEFAULT` 는 DBMS 에 설정된 격리수준을 사용한다는 뜻  

#### TransactionManager

`TransactionManager` 는 DB 영역에서 트랜잭션 기능을 추상화 시킨 클래스이다.  

`JPA` 의 경우 `EntityManager` 의 `begin, commit` 를 통해 트랜잭션을 진행하고,  
`JDBC` 의 경우 `dataSource.getConnection` 의 `setAutoCommit(false), commit` 을 통해 트랜잭션을 진행한다.  

스프링에선 이를 아래와 같은 `TransactionManager` 인터페이스로 트랜잭션 과정을 추상화 시켰다.  

```java
public interface TransactionManager {}

public interface PlatformTransactionManager extends TransactionManager {
    TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;
    void commit(TransactionStatus status) throws TransactionException;
    void rollback(TransactionStatus status) throws TransactionException;
}

public abstract class AbstractPlatformTransactionManager
    implements PlatformTransactionManager, ConfigurableTransactionManager, Serializable {
    ...
}
// 아래와 같은 AbstractPlatformTransactionManager 구현체들이 있음.  
// JpaTransactionManager
// DataSourceTransactionManager
// JmsTransactionManager - java message system 을 같이 사용할 경우 이용
```

`JpaTransactionManager` 를 사용하는 상황에서 `@Transactional` 어노테이션을 만나면 `Spring AOP` 가 알아서 추상화 처리된 `AbstractPlatformTransactionManager` 의 동작대로 DB 와의 연결 및 트랜잭션 작업을 수행한다.  

1. 트랜잭션 시작, 아래함수를 순서대로 호출
   `TransactionAspectSupport.createTransactionIfNecessary`  
   `AbstractPlatformTransactionManager.getTransaction`  
   `AbstractPlatformTransactionManager.startTransaction`  
2. `DataSource` 로부터 `Connection` 흭득 및 `ThreadLocal` 에 등록
   `AbstractPlatformTransactionManager.doBegin` 
   `TransactionSynchronizationManager.bindResource - ThreadLocal 등록`  
3. 트랜잭션 내부에서 수행되는 `Repository` 메서드들은 `ThreadLocal` 로부터 `Connection` 을 가져와서 쿼리를 실행  
4. 트랜잭션 종료(commit or rollback)  
   `TransactionAspectSupport.cleanupTransactionInfo`
5. `DataSource` 에 `Connection` 반환  

`readOnly = true` 가 특별한 유형의 연결을 생성하지는 않지만, 읽기 전용 트랜잭션의 이점을 활용하여 ORM의 성능을 최적화할 수 있다.  
수정요청시 에러를 발생시키도록 하거나 `Hibernate` 의 **영속성 플러시** 작업을 추가적으로 하지않아 성능을 최적화 할 수 있다.  
이를 통해 데이터 일관성을 보장하고, 읽기 전용 작업의 성능을 극대화할 수 있다.  

#### 트랜잭션 general_log

> 출처: <https://tech.kakaopay.com/post/jpa-transactional-bri/>

`JpaTransactionManager` 가 트랜잭션을 위해 호출하는 SQL 쿼리를 `general_log` 를 통해 확인가능하다.  
기존에 FILE 에 출력되는 로그를 `mysql.general_log` 테이블에 출력되도록 변경.  

```sql
SHOW VARIABLES LIKE 'log_output';
-- +-------------+-----+
-- |Variable_name|Value|
-- +-------------+-----+
-- |log_output   |FILE |
-- +-------------+-----+
SET GLOBAL general_log = 'ON';
SET GLOBAL log_output = 'TABLE';

SELECT * FROM mysql.general_log
```

위와같이 설정하고 `spring.jpa.open-in-view=false` 상태에서 기본 `DataSource` 를 사용해서 `@Transaction` 설정별로 `mysql.general_log` 의 출력 결과를 확인하면 아래와 같다.  

```java
@Beans
public DataSource dataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo?useUnicode=true&serverTimezone=Asia/Seoul");
    dataSource.setUsername("root");
    dataSource.setPassword("root");
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    return dataSource;
}
```


```java
// @Transaction 이 없을 때  
public List<Board> findAll() {
    return repository.findAll();
}
```

```java
@Transactional(readOnly = true)
public List<Board> findAll() {
    return repository.findAll();
}
```

위와 같이 `@Transactional(readOnly = true)` 있는것과 없는 메서드 실행시 아래 6개의 `general_log` 가 출력된다.  

- **set session transaction read only**  
  세션의 읽기 전용 지정  
- **SET autocommit=0**  
  세션에서 호출될 쿼리들이 자동커밋되지 않고 트랜잭션으로 묶이는 것을 의미, 트랜잭션 시작을 의미.  
- **select b1_0.bno,... from tbl_boards b1_0**  
  쿼리 수행  
- **commit**  
  쿼리 커밋, 트랜잭션 종료  
- **SET autocommit=1**  
  autocommit 원복  
- **set session transaction read write**  
  세션 읽기 쓰기 지정 원복  

> `@Transactional` 을 지정하지 않으면 `repository` 메서드 호출마다 `session` 에 대한 설정을 수행하기 때문에 위 예제의 경우 `@Transactional(readOnly = true)` 설정한것과 동일하다.  

`@Transactional(readOnly=false)` 의 경우 4개의 `general_log` 가 출력된다.  

```java
@Transactional(readOnly=false)
public List<Board> findAll() {
    return repository.findAll();
}
// SET autocommit=0
// "select b1_0.bno,... from tbl_boards b1_0"
// commit
// SET autocommit=1
```

`@Transactional(readOnly=true)` 를 설정할 경우 영속성 레이어에서 추가작업을 하지 않아 어플리케이션 레이어에선 부하가 줄어들겠지만,  
`session transaction` 의 `read only, read write` 작업을 추가적으로 수행하기 때문에 DB 레이어에선 부하가 증가한다.  

`DataSource` 에서 `autocommit` 설정을 `disable` 처리하고, `@Transactional` 만 지정된 메서드를 수행하면 단 2개의 `general_log` 가 출력된다.  

```java
HikariDataSource dataSource = new HikariDataSource();
...
dataSource.setAutoCommit(false);
```

```sql
"select b1_0,.... from tbl_boards b1_0"
commit
```

대부분의 상황에서 트랜잭션은 필수이기에 `autocommit` 을 사용하겠지만 아래와 같은 특수한 상황에선 사용할만 하다.   

- SELECT 만 수행하는 CQRS 패턴 어플리케이션의 경우 DB 부하를 줄이기 위해.  
- 읽기전용 `DataSource` 를 구성하고 `AbstractRoutingDataSource, LazyConnectionDataSourceProxy` 를 통해 분리호출 할 경우.  

실시간 트랜잭션 `read_only` 활성여부를 확인하려면 아래 SQL 참고  

```sql
-- SET GLOBAL TRANSACTION READ WRITE;
-- SET SESSION TRANSACTION READ WRITE;
SET GLOBAL TRANSACTION READ ONLY;
SET SESSION TRANSACTION READ ONLY;

SELECT @@SESSION.transaction_isolation as 'STI',
       @@SESSION.transaction_read_only as 'STR',
       @@GLOBAL.transaction_isolation as 'GTI',
       @@GLOBAL.transaction_read_only as 'GTR';

SELECT * FROM mysql.general_log;
```

> MySQL Workbench 기준, DB Client 별로 출력결과가 다를 수 있음.  

#### @Lock, @Version

스레드가 애그리거트를 read, write 하는 동안  
다른 스레드가 수정할 수 없도록 설정하기 위한 기능  

> `Pessimistic Lock` 비관적 락, 선점잠금  
> `Optimistic Lock` 낙관적 락, 비선점잠금  

![jpa7](/assets/springboot/springboot_jpa_7.png)

`DB row` 에 잠금을 걸어 트랜잭션을 `block` 시키는 **비관적 락** 방식이 있고  
`version` 정보를 통해 `Lost Update` 를 제한시키는 **낙관적 락** 방식이 있다.  

#### 낙관적 락(Optimistic Lock)

`낙관적 락` 에선 DB에서 제공하는 락을 사용하지 않고 `@Version` 을 사용한다.  
`@Version` 어노테이션만 지정해도 자동 사용된다.  

> `[Long, Int, Short, Timestamp]` 사용 가능  

```java
@Getter
@Access(AccessType.FIELD)
@Entity
@Table(name = "purchase_order")
public class Order {

    @EmbeddedId
    private OrderId orderId;
    ...
    ...
    // 낙관적 락을 위한 필드
    @Version
    private long version;
}
```

`Entity` 에 `@Version` 만 지정하면 별도의 어노테이션을 사용하지 않아도 `version` 정보를 기반으로 `UPDATE` 하기 때문에 `Lost Update` 문제가 발생하지 않는다.  

```sql
-- OrderService->patch start!
select order0_.order_number as order_nu1_1_,
       order0_.state        as state2_1_,
       order0_.version      as version3_1_
from purchase_order order0_
where order0_.order_number = ?
-- UPDATE 시 version 체크
update purchase_order set state=?, version=? where order_number=? and version=?
-- OrderService->patch end!
```

쿼리 메서드에 별도로 `@Lock` 어노테이션을 사용해 `낙관적 락`에 대한 추가설정을 할 수 있다.  

- **OPTIMISTIC**  
  트랜잭션 종료 시점에 한번 더 버전정보를 체크한다.  
  만약 종료시점에서 검색된 version 이 다를경우 `OptimisticLockException` 을 발생시킨다.  
  현재 스레드의 `Dirty Read, Lost Update` 상황을 방지한다.  
- **OPTIMISTIC_FORCE_INCREMENT**  
  단순 `SELECT` 요청도 `version` 을 증가시킨다. 변경까지 한다면 `version` 이 2 증가한다.  
  타 스레드의 `Dirty Read, Lost Update` 상황을 방지한다.  

```java
@Lock(LockModeType.OPTIMISTIC)
@Query("SELECT o FROM Order o WHERE o.orderId = :orderId")
Optional<Order> findByIdOptimistic(OrderId orderId);

@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
@Query("SELECT o FROM Order o WHERE o.state = :state")
List<Order> findAllByOrderStateOptimistic(OrderState state);
```

`LockModeType.OPTIMISTIC` 을 사용했다면 초기 `SELECT` 한 `Entity` `version` 과 트랜잭션 종료 직전 조회한 `version` 이 일치하지 않는다면 `OptimisticLockException` 가 발생한다.  

```sql
-- OrderService->findByIdOptimistic start! 함수 트랜잭션 시작
-- service function start!
select order0_.order_number as order_nu1_1_,
       order0_.state        as state2_1_,
       order0_.version      as version3_1_
from purchase_order order0_
where order0_.order_number = ?

-- service function end!
select version as version_ from purchase_order where order_number =?
-- OrderService->findByIdOptimistic end! 함수 트랜잭션 종료 전 version 검사
-- 일치하지 않으면 OptimisticLockException
```

`LockModeType.OPTIMISTIC_FORCE_INCREMENT` 을 사용했다면 `SELECT` 로 조회한 모든 `Entity` `version` 을 증가시킴.  

```sql
-- OrderService->findAllByOrderState start!
select order0_.order_number as order_nu1_1_,
       order0_.state        as state2_1_,
       order0_.version      as version3_1_
from purchase_order order0_
where order0_.state = ?

update purchase_order set version=? where order_number=? and version=?
update purchase_order set version=? where order_number=? and version=?
update purchase_order set version=? where order_number=? and version=?
update purchase_order set version=? where order_number=? and version=?
update purchase_order set version=? where order_number=? and version=?
-- SELECT 로 조회된 purchase_order 개수만큼 수행
-- OrderService->findAllByOrderState end!
```

`LockModeType.OPTIMISTIC_FORCE_INCREMENT` 는 부하를 유발시키는 설정이긴 하지만 `first-commiter win` 과 같은 형태로 운영할 수 있다.  

`낙관적 락`의 단점은 DB 락을 가져올수 있는지 즉시 체크하지 못하기 때문에 **데이터 일관성 체크를 커밋 시점에야 가능**하다는 것이다.  
`낙관적 락` 과 연계된 쿼리가 있다면 별도의 처리를 해줘야할 수 도 있다.  

#### 비관적 락(Pessimistic Lock)

`비관적 락`에선 `@Lock(LockModeType.PESSIMISTIC...)` 을 사용한다.  

DBMS 마다 다르지만 MySQL 의 경우 `비관적 락` 을 설정하면 쿼리 마지막에 `FOR SHARE, FOR UPDATE` 키워드가 붙는다.  

- **PESSIMISTIC_READ**  
  `FOR SHARE` 키워드를 사용, `[UPDATE, DELETE]` 를 막는다.  
- **PESSIMISTIC_WRITE**
  `FOR UPDATE` 키워드를 사용, `[SELECT, UPDATE, DELETE]` 를 막는다.  
  현재 스레드의 `Dirty Read, Lost Update` 를 막는다.  
- **PESSIMISTIC_FORCE_INCREMENT**  
  `PESSIMISTIC_WRITE` 와 동일한 기능에 더불어 잠금 흭득시 `@Version` 을 증가시킨다.  

> `비관적 락` 방식의 경우 락에 의한 교착상태가 발생가능하니 타임아웃 설정을 권장한다.  
> DBMS 레이어에서 Lock Timeout 을 설정해도 된다. `innodb_lock_wait_timeout=50(default)`

```java
public interface OrderRepository extends CrudRepository<Order, OrderId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findById(OrderId orderId);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    // javax.persistence.lock.timeout
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value   ="5000"))
    @Query("SELECT o FROM Order o WHERE o.state = :state")
    List<Order> findAllByOrderState(OrderState state);
}
```

```sql
-- findAllByOrderState 실행
-- 리스트 개수만큼 version update 가 추가실행된다.  
select order0_.order_number as order_nu1_1_,
       order0_.state        as state2_1_,
       order0_.version      as version3_1_
from purchase_order order0_
where order0_.state = ?
for update;

update purchase_order set version=? where order_number = ? and version = ?;
update purchase_order set version=? where order_number = ? and version = ?;
...
update purchase_order set version=? where order_number = ? and version = ?;
```

`비관적 락`을 사용하는 대부분 이유가 `Dirty Read` 이후 이어지는 `Lost Update` 를 막기 위함이기 때문에 `PESSIMISTIC_WRITE` 를 주로 사용한다.  

### 분산락  

> <https://hyperconnect.github.io/2019/11/15/redis-distributed-lock-1.html>

`분산락(Distributed lock)` 은 DB 접근을 제한하기 보다, 특정 코드접근(임계영역)을 제한하기 위한 기법이다.  

분산 서버 환경으로 인해 다수의 동일한 코드가 동시 동작하고 있을 때 해당 코드영역의 동기화를 위해 접근을 제한시킬 때 분산락을 사용한다.  

> java 에서 `syncronize` 사용을 최대한 피하는것 처럼, 분산락 사용을 최대한 기피해야한다.  

중앙에서 Lock 을 관리해줄 별도의 서버가 필요한데, 아래와 같은 서비스를 사용해 구현 가능하다.  

- Redis: Redisson  
- Mysql: NamedLock(메타데이터 락)  

```java
@Repository
@RequiredArgsConstructor
public class NamedLockRepository {
    private final JdbcTemplate jdbcTemplate;

    public Integer getLock(String lockName, int timeout) {
        // timeout 은 락을 획득하기 위해 기다리는 시간(초)
        Integer result = jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, ?)",
                Integer.class, // return type
                lockName, timeout // params
        );
        return result;
    }

    public Integer releaseLock(String lockName) {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT RELEASE_LOCK(?)",
                Integer.class,
                lockName
        );
        return result;
    }

}
```

```java
public void executeWithLock(String lockName) {
    int lockStatus = lockRepository.getLock(lockName, 10);
    if (lockStatus == 1) {
        try {
            // 락을 획득한 상태에서 실행할 작업
            Thread.sleep(100);
            count += 1;
            log.info("Lock acquired. Executing protected code. count:{}", count);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockRepository.releaseLock(lockName);
            log.info("sLock released.");
        }
    } else {
        log.info("Could not acquire lock.");
    }
}

public void executeWithoutLock() {
    try {
        Thread.sleep(100);
        count += 1;
        log.info("Lock acquired. Executing protected code. count:{}", count);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

아래와 같이 api 100번 연속 호출시 분산락 내부 임계영역이 정확히 몇번 호출되는지 테스트하면 된다.  

```sh
for i in {1..100}; do curl -s http://localhost:8080/distribute-lock/test & done; wait
```

아래 명령으로 현재 사용중인 메타데이터 락을 확인 가능.  

```SQL
SELECT * FROM performance_schema.metadata_locks;
```

하지만 MySQL 의 `메타데이타 락` 은 코드의 임계영역을 동시에 한번 실행시키진 않는다.  
동일 세션의 `GET_LOCK` 호출은 이미 락을 흭득했다 보고 성공코드를 돌려주기 때문.

`SELECT GET_LOCK('testLock', 10);` 해당 코드를 같은 DB 콘솔에서 여러번 실행하면 동일 세션이기 때문에 모두 1(성공) 이 출력된다.  

즉 어플리케이션 단위로 `메타데이타 락` 을 가져간다고 봐야한다.  
어플리케이션 레이어에서 `메타데이타 락` 과 함께 `로컬 Lock` 를 관리하거나 `synchronized` 키워드를 사용하면 분산환경에서도 임계영역을 지정할 수 있다.  

### open-in-view

```text
spring.jpa.open-in-view is enabled by default. Therefore, database queries may be performed during view rendering. Explicitly configure spring.jpa.open-in-view to disable this warning
```

보통 `Service` 에서 `@Transactional` 을 사용해 `영속성 컨텍스트`를 생성하고  
`Controller` 나 외부 컴포넌트에선 `준영속 컨텍스트`가 될거라 새각하지만  

`open-in-view=true` 의 경우 영속성 컨텍스트의 생존 법위가 스레드의 종료까지 이어진다  
(REST API 의 Response 완료까지)
`default true` 이기 떄문에 컨트롤러에서 `Lazy Loading` 을 통해 엔티티를 통해 객체를 찾고 DB 에서 가져올 수 있다.  

`open-in-view=false` 일 경우 `준영속 컨텍스트`에선 `지연로딩` 사용이 불가능하다.  
지연로딩 기법을 사용한다면 `@Transactional` 외부에서 영속공간에 접근하는 내용을 제거해야한다.  
Transaction 안에서만 Lazy Loading 을 수행할 수 있고, 컨트롤러 코드에서 접근시 no session 에러가 발생하게 된다.  

## Criteria

JPA의 공식 동적 쿼리 생성 API. 타입 안전한 쿼리를 작성할 수 있지만 코드가 복잡하고 가독성이 떨어진다.

### 설정

`EntityManager`를 주입받아 사용한다.

```java
@Service
@RequiredArgsConstructor
public class BookService {
    private final EntityManager entityManager;
    
    // ...
}
```

### 동적 쿼리 예제

```java
@Transactional(readOnly = true)
public List<Book> search(String title, String authorName, LocalDate createdFrom, LocalDate createdTo) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Book> query = cb.createQuery(Book.class);
    Root<Book> book = query.from(Book.class);
    Join<Book, Author> author = book.join("author", JoinType.LEFT);

    List<Predicate> predicates = new ArrayList<>();

    // 제목 검색 (LIKE 검색)
    if (title != null && !title.isEmpty()) {
        predicates.add(cb.like(cb.lower(book.get("title")), "%" + title.toLowerCase() + "%"));
    }

    // 작성자 이름 검색 (LIKE 검색)
    if (authorName != null && !authorName.isEmpty()) {
        predicates.add(cb.like(cb.lower(author.get("name")), "%" + authorName.toLowerCase() + "%"));
    }

    // 생성일 시작 범위
    if (createdFrom != null) {
        predicates.add(cb.greaterThanOrEqualTo(book.get("created"), createdFrom));
    }

    // 생성일 종료 범위
    if (createdTo != null) {
        predicates.add(cb.lessThanOrEqualTo(book.get("created"), createdTo));
    }

    // 조건 조합
    query.where(predicates.toArray(new Predicate[0]));

    // 정렬 (생성일 내림차순)
    query.orderBy(cb.desc(book.get("created")));

    TypedQuery<Book> typedQuery = entityManager.createQuery(query);
    return typedQuery.getResultList();
}
```

### Controller 예제

```java
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @GetMapping("/search")
    public List<Book> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo) {
        return bookService.search(title, authorName, createdFrom, createdTo);
    }
}
```

## Querydsl

> <http://querydsl.com/>

QueryDSL은 빌드 과정에서 `Qclass` 를 자동생성하여 타입 안전한 동적 쿼리를 작성할 수 있게 해준다.  
아래 annotationProcessor 라이브러리 삽입을 통해 Entity 클래스들을 확인하고 컴파일 과정에서 자동으로 `QClass` 를 생성한다.  

```groovy
def queryDslVersion = '5.0.0'
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation "com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta"
    implementation "com.querydsl:querydsl-core:${queryDslVersion}"

    // QueryDsl 쿼리 타입 생성 (QClass 생성 시 @Entity 탐색)
    annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}:jakarta"
    // java.lang.NoClassDefFoundError:javax/persistence/Entity 에러 방지
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
}
```

```groovy
// QueryDSL QClass 생성 디렉토리 설정
 def querydslDir = layout.buildDirectory.dir("generated/querydsl").get().asFile
//def querydslDir = file("src/main/generated") # src/main/generated 위치에 저장

sourceSets {
    main.java.srcDirs += [querydslDir]
}

// annotationProcessor 에서 생성할 코드 위치 지정
project.tasks.named('compileJava') {
    options.generatedSourceOutputDirectory = file(querydslDir)
}

// QueryDSL QClass 생성 디렉토리 정리 태스크
task cleanQuerydslGenerated(type: Delete) {
    delete querydslDir
}
// clean 태스크에 QueryDSL 생성 파일 삭제 추가
clean {
    delete querydslDir
}
```

```java
@Configuration
public class SpringConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
```

### 동적 쿼리 예제

빌드 과정에서 `@Entity` 클래스를 기반으로 `QClass`가 자동 생성된다.  

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
    ...
}
```

기존의 정수, 문자열 변수들이 `NumberPath`, `StringPath` 과 같은 객체형으로 변경되었다.  
내부적으로 조건문 처리를 할 수 있는 함수들이 정의되어있다.

`BooleanBuilder`를 사용하여 조건에 따라 동적으로 쿼리를 생성할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository repository;
    private final JPAQueryFactory queryFactory;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Board> search(String title, String writer, String content, 
                              LocalDateTime regdateFrom, LocalDateTime regdateTo) {
        BooleanBuilder builder = new BooleanBuilder();

        // 제목 검색 (LIKE 검색, 대소문자 무시)
        if (title != null && !title.isEmpty()) {
            builder.and(QBoard.board.title.containsIgnoreCase(title));
        }

        // 작성자 검색 (LIKE 검색, 대소문자 무시)
        if (writer != null && !writer.isEmpty()) {
            builder.and(QBoard.board.writer.containsIgnoreCase(writer));
        }

        // 내용 검색 (LIKE 검색, 대소문자 무시)
        if (content != null && !content.isEmpty()) {
            builder.and(QBoard.board.content.containsIgnoreCase(content));
        }

        // 등록일 시작 범위
        if (regdateFrom != null) {
            builder.and(QBoard.board.regdate.goe(regdateFrom));
        }

        // 등록일 종료 범위
        if (regdateTo != null) {
            builder.and(QBoard.board.regdate.loe(regdateTo));
        }

        return queryFactory
                .selectFrom(QBoard.board)
                .where(builder)
                .orderBy(QBoard.board.regdate.desc())
                .fetch();
    }
}
```

### Controller 예제

```java
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/search")
    public List<Board> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String writer,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime regdateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime regdateTo) {
        return boardService.search(title, writer, content, regdateFrom, regdateTo);
    }
}
```

### QuerydslPredicateExecutor 사용

`QuerydslPredicateExecutor` 를 상속하면 `CrudRepository` 에서도 `Qclass` 사용이 가능하다.  

```java
public interface BoardRepository extends 
    CrudRepository<Board, Long>, 
    QuerydslPredicateExecutor<Board> {
    ...
}
```

```java
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

> JPA 의 공식 지원 라이브러리인 `Criteria` 도 `Specifications` 동적쿼리 생성방법이 있으니 참고  
> <https://www.baeldung.com/rest-api-search-language-spring-data-specifications>

## 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/jpa-demo>

<!-- 
### path, expression

geo 관련 sql 에서 `alias` 와 표현식을 뜻함.  

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
-->
