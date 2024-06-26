---
title:  "Spring Boot - DB Sharding!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Sharding

![1](/assets/springboot/springboot_sharding1.png)  

1. db proxy 를 구성하고 샤딩과 failover 모두 proxy 아래에서 처리  
2. sharing 은 어플리케이션이 직접하고 fialover 만 db proxy 아래에서 처리  

## AbstractRoutingDataSource

> <https://www.baeldung.com/spring-abstract-routing-data-source>

샤딩에 대한 처리를 직접 구현하고 싶다면 `AbstractRoutingDataSource` 를 `Bean` 으로 등록하여 `DataSource` 를 선택하는 부분을 구현하면 된다.  

여러개의 `DataSource` 와 `DataSource Key` 를 함께 `Map<Object, Object>` 형태로 구성하고,  
매번 DB 접근하기 전에 `DataSource Key` 를 사용해 `AbstractRoutingDataSource` 에 `DataSource` 를 질의하는 방식이다.  

### DataSource Key 매핑

먼저 DB 의 key 값으로 사용할 `문자열` 혹은 `enum` 을 정의한다.  

```java
public enum DemoDatabase {
    demo_ds_0, demo_ds_1,
}
```

그리고 각 ``DataSource Key`` 를 매핑시킨 `Map<Object, Object>` 구성.  

```java
private Map<Object, Object> createDataSourceMap() {
    Map<Object, Object> dataSourceMap = new HashMap<>();
    HikariDataSource dataSource0 = new HikariDataSource();
    dataSource0.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource0.setJdbcUrl("jdbc:mysql://localhost:3306/demo_ds_0");
    dataSource0.setUsername("root");
    dataSource0.setPassword("root");
    dataSource0.setMaximumPoolSize(20); // default 10
    dataSource0.setMinimumIdle(5);

    HikariDataSource dataSource1 = new HikariDataSource();
    dataSource1.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource1.setJdbcUrl("jdbc:mysql://localhost:3307/demo_ds_1");
    dataSource1.setUsername("root");
    dataSource1.setPassword("root");
    dataSource1.setMaximumPoolSize(20); // default 10
    dataSource1.setMinimumIdle(5);

    dataSourceMap.put(DemoDatabase.demo_ds_0, dataSource0);
    dataSourceMap.put(DemoDatabase.demo_ds_1, dataSource1);

    return dataSourceMap;
}
```

`AbstractRoutingDataSource` 의 구현체인 `DemoDataSourceRouter` 에다 위에서 생성했던 `Key - DataSource` 매핑객체인 `Map<Object, Object>` 를 삽입하고,  
`DataSource` 가 라우팅되지 않았을 때 사용할 `default DataSource` 도 지정한다.  

```java
@Bean // AbstractRoutingDataSource
public DataSource datasource() {
    Map<Object, Object> targetDataSources = createDataSourceMap();
    DataSource defaultDataSource = createDefaultDataSource();
    DemoDataSourceRouter sourceRouter = new DemoDataSourceRouter();
    sourceRouter.setTargetDataSources(targetDataSources);
    sourceRouter.setDefaultTargetDataSource(defaultDataSource);
    return sourceRouter;
}

private DataSource createDefaultDataSource() {
    HikariDataSource defaultDataSource = new HikariDataSource();
    defaultDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    defaultDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo_ds_0");
    defaultDataSource.setUsername("root");
    defaultDataSource.setPassword("root");
    defaultDataSource.setMaximumPoolSize(20); // default 10
    defaultDataSource.setMinimumIdle(5);
    return defaultDataSource;
}
```

### DataSource Router

`JPA` 나 `JDBC` 나 DB 쿼리 요청을 날리기 전에 어떤 `DataSource` 를 선택사용할 것인지 항상 `AbstractRoutingDataSource` 에 `DataSource Key` 값을 사용하여 질의한다.  

보통 `ThreadLocal` 클래스를 사용해서 실행중인 스레드에서 어떤 `DataSource Key` 를 사용할지 설정하는 코드를 사용한다.  

```java
public class ThreadLocalDatabaseContextHolder {

    private static ThreadLocal<DemoDatabase> CONTEXT = new ThreadLocal<>();
    public static DemoDatabase getClientDatabase() {
        return CONTEXT.get();
    }

    // 샤딩을 위해 id 값이 홀수/짝수 인지에 따라 DataSource 변경
    public static void setById(long id) {
        int idx = (int) (id % 2);
        DemoDatabase demoDatabase = DemoDatabase.values()[idx];
        CONTEXT.set(demoDatabase);
    }
    // 외부에서 직접 DataSource 키값을 설정
    public static void set(DemoDatabase demoDatabase) {
        Assert.notNull(demoDatabase, "clientDatabase cannot be null");
        CONTEXT.set(demoDatabase);
    }
```

`ThreadLocal` 에서 `DataSource Key` 값을 설정했다면 아래 `AbstractRoutingDataSource` 의 상속 클래스를 정의해서 해당 실행중인 스레드에서 설정된 `DataSource Key` 값을 반환할 수 있도록 처리한다.  

```java
@Slf4j
@RequiredArgsConstructor
public class DemoDataSourceRouter extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        DemoDatabase datasource = ThreadLocalDatabaseContextHolder.getClientDatabase();
        log.info(">>>>>> current data source : {}, isReadOnly : {}", datasource, isReadOnly);
        return datasource;
    }
}
```

### JpaTransactionManager 와의 호환 불량  

`JPA` 와 `AbstractRoutingDataSource` 를 같이 사용할 경우 `JpaTransactionManager` 의 호환 문제로 인해 추가 설정 없이 `read replica` 분리는 어려우 수 있다.  

아래는 `JpaTransactionManager` 의 doBegin 함수인데 `Datasource` 를 가져오는 `beginTransaction` 코드가 `TransactionSynchronizationManager` 의 `ThreadLocal` `readOnly` 값을 수정하는 코드보다 먼저 와있다.  

```java
// JpaTransactionManager.class
@Override
protected void doBegin(Object transaction, TransactionDefinition definition) {
  JpaTransactionObject txObject = (JpaTransactionObject) transaction;
  ...
  EntityManager em = txObject.getEntityManagerHolder().getEntityManager();

  // Delegate to JpaDialect for actual transaction begin.
  int timeoutToUse = determineTimeout(definition);
  // 여기서 Datasource 를 가져옴
  Object transactionData = getJpaDialect().beginTransaction(em,
      new JpaTransactionDefinition(definition, timeoutToUse, txObject.isNewEntityManagerHolder()));
  txObject.setTransactionData(transactionData);
  txObject.setReadOnly(definition.isReadOnly());
  ...
}
```

따라서 `AbstractRoutingDataSource` 에서 `TransactionSynchronizationManager.isCurrentTransactionReadOnly` 메서드를 호출해도 항상 `false` 가 반환된다.  

이를 해결하기 위한 방법은 아래 3가지.  

1. `ThreadLocal` 에 `readOnly` 값을 포함시킨 `Datasource Key` 를 저장하기  
2. `LazyConnectionDataSourceProxy` 사용하기  
   `AbstractRoutingDataSource` 로직을 `@Transaction` 으로 인한 AOP 뒤에 실행되도록 설정하는 방법.  
3. `JPA` 를 버리고 `JDBC` 사용하기  
   `JDBC` 에서 사용하는 `DatasourceTransactionManager` 의 경우 위와같은 문제가 발생하지 않음  

### LazyConnectionDataSourceProxy

`DataSource` 를 가져오는 과정은 `@Transaction` 으로 인한 AOP 로 인하여 정의 함수 실행 전 인터셉터되어 수행된다.  

`@Transcational` 함수 실행 전에 `TransactionManager` 가 해당 메서드를 인터셉터하기 때문에 아래와 같이 함수 내부에서 `ThreadLocal` 에 `DataSource Key` 값을 변경해도 사용할 `DataSource` 가 바뀌지 않는다.  

```java
@Transactional // 이미 Routing 과정이 인터셉터로 인해 끝난 상태
public AccountDto addRandomAccount() {
    long accountId = System.currentTimeMillis();
    ThreadLocalDatabaseContextHolder.setById(accountId);
    AccountEntity accountEntity = new AccountEntity(accountId, RandomTestUtil.generateRandomString(10));
    accountEntity = accountRepository.save(accountEntity);
    return toDto(accountEntity);
}
```

정의 함수 실행 전에 `AbstractRoutingDataSource` 로직이 수행되기에 `@Transactional` 진입 전에 `DataSource Key` 선택을 진행해야 한다.  

하지만 컨트롤러 코드에 `ThreadLocalDatabaseContextHolder` 관련 코드가 들어가는것이 부담스러울 수 있고, 위에서 설명했던 `JpaTransactionManager` 호환 문제 떄문에 `AbstractRoutingDataSource` 로직을 정의 함수 안에서 가져오게 설정하고 싶을 수 있다.  

이때 `LazyConnectionDataSourceProxy` 한다.  

`TransactionManager` 가 가져가는 `Connection` 객체를 `LazyConnectionDataSourceProxy` 가 생성한 `Proxy Connection` 객체로 대채시켜 반환 시키고,  
향후 DB SQL 쿼리 요청시 해당 `Connection` 객체를 실제 사용할 때 `DataSource` 에서 진짜 `Connection` 객체를 가져오는 방식을 사용한다.  

그럼 `Proxy Connection` 객체로 인해 실제 `Connection` 을 사용하는 `accountRepository.save` 을 실행할 때 `AbstractRoutingDataSource` 를 통해 진짜 `Connection` 객체를 가져온다.  

대신 `JpaTransactionManager` 는 `Proxy Connection` 객체를 진짜 `Connection` 으로 알고 있기 때문에,  
이를 `ThreadLocal` 에서 가져다 사용하는 `repository` 메서드들은 계속 `Proxy Connection` 객체를 통해 `Connection` 객체를 가져오게 된다.  

### 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/sharding-demo/abstract-routing>

## Apache Sharding Sphere  

> <https://shardingsphere.apache.org/document/5.4.0/en/user-manual/shardingsphere-jdbc/yaml-config/jdbc-driver/>  
> <https://www.baeldung.com/java-shardingsphere>  
> <https://www.slideshare.net/slideshow/ss-183100083/183100083>  
> 
> <https://github.com/apache/shardingsphere/issues/24258>  
> ![1](/assets/springboot/springboot_sharding2.png)  

사전에 알아야할 부분이 `Spring Boot 3.x` 최신버전을 사용중이라면 `shardingsphere-jdbc-core` 를 사용해야 한다.  

기존의 마이너한 `shardingsphere dependency` 들은 업데이트 되고있지 않다.  

`Apache Sharding Sphere` 의 샤딩을 위한 개념을 알아야한다.  

- DataSource
- Rules
- Algorithm(Rules 에 적용되는 알고리즘)  

### DataSource

JDBC 기반으로 하는 커넥션 풀을 지원하는 `DataSource` 생성이 가능하다.  

```yaml
dataSources:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: root
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/ds_2
    username: root
    password: root
```

`Sharding Sphere` 에서는 `ShardingSphereDataSource` 를 사용하여 `DataSource Key` 와 매핑된 `ShardingSphereConnection(logic_db)` 객체를 가져온다.  

`@Transaction` `AOP` 로 인해 트랜잭션이 시작할 때에는 `logic_db` 라는 래핑된 `Connection` 을 가져와 사용하기에 의존성 분리가 되어있다.  

실제 `hibernate` 에서 `prepareQueryStatement` 메서드가 `logic_db` 의 `Connection.prepareStatement` 메서드를 호출할 때 물리적 `HikariDataSource` 의 `Connection` 객체를 가져온다.  

> 라우팅 처리하는 코드가 궁금하다면 `org.apache.shardingsphere.infra.connection.kernel.KernelProcessor` 클래스의 `generateExecutionContext`, `route` 메서드를 확인하면 된다.  

### Rules

샤딩 기능을 포함하여 `Sharding Sphere` 에서 설정할 수 있는 각종 `rule` 들을 설정.  

> 하지만 여기선 `Sharding` 에 필요한 `rule` 들만 다룰 예정.  

#### rules.tables

샤딩에 대한 기본적인 전략을 설정하는 config

- `rules.tables.{table_name}.actualDataNodes`  
  사용할 DataSource 와 테이블 명을 기술하는 설정. 아래 databaseStrategy, tableStrategy 값과 INLINE 문법을 통해 결정됨.
- `rules.tables.{table_name}.databaseStrategy.{strategy}`  
  DB 선택 전략, standard, complex, hint, none 중 택 1
- `rules.tables.{table_name}.tableStrategy.{strategy}`  
  테이블 선택 전략, standard, complex, hint, none 중 택 1
- `rules.tables.{table_name}.keyGenerateStrategy`  
  테이블의 키 생성 전략, 아래 `keyGenerators` 에서 자세히 확인  
- `rules.tables.{table_name}.auditStrategy`  
  감사(audit) 전략, 아래 audit algorithm 에서 자세히 확인  

```yaml
rules:
- !SHARDING
  tables: # Sharding table configuration
    <logic_table_name> (+): # Logic table name
      actualDataNodes (?): # Describe data source names and actual tables (refer to Inline syntax rules)
      databaseStrategy (?): # Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.
        standard: # For single sharding column scenario
          shardingColumn: # Sharding column name
          shardingAlgorithmName: # Sharding algorithm name
        complex: # For multiple sharding columns scenario
          shardingColumns: # Sharding column names, multiple columns separated with comma
          shardingAlgorithmName: # Sharding algorithm name
        hint: # Sharding by hint
          shardingAlgorithmName: # Sharding algorithm name
        none: # Do not sharding
      tableStrategy: # Tables sharding strategy, same as database sharding strategy
      keyGenerateStrategy: # Key generator strategy
        column: # Column name of key generator
        keyGeneratorName: # Key generator name
      auditStrategy: # Sharding audit strategy
        auditorNames: # Sharding auditor name
          - <auditor_name>
          - <auditor_name>
        allowHintDisable: true # Enable or disable sharding audit hint
  ...
  ...
```

#### rules.autoTables

개발 초기에 간단히 설정할 때 사용하며 DB 와 테이블 샤딩을 한번에 처리하고 `standard` 샤딩 전략만 사용 가능하다.  

- `rules.autoTables.{table_name}.actualDataSources`  
  사용할 DataSource 와 테이블 명을 기술하는 설정. DB 와 테이블 샤딩을 아래 `shardingStrategy` 하나로만 INLINE 문법을 통해 결정한다.  
- `rules.autoTables.{table_name}.shardingStrategy.standard`  
  샤딩 전략으로 standard 만 사용 가능

```yaml
rules:
- !SHARDING
  tables: # Sharding table configuration
  ...
  autoTables: # Auto Sharding table configuration
    t_order_auto: # Logic table name
      actualDataSources (?): # Data source names
      shardingStrategy: # Sharding strategy
        standard: # For single sharding column scenario
          shardingColumn: # Sharding column name
          shardingAlgorithmName: # Auto sharding algorithm name
  ...
  ...
```

#### rules.bindingTables

동일한 컬럼을 기준으로 샤딩될 경우 테이블을 바인딩 테이블로 설정하여 조인 쿼리를 수행할 수 있도록 설정.  

```yaml
rules:
- !SHARDING
  tables: # Sharding table configuration
  ...
  bindingTables: # Binding tables
    - t_order,t_order_item
```

#### rules.default

아래 4개 설정에 대해선 모든 테이블에 기본적으로 적용될 수 있도록 default 설정을 할 수 있다.  

- defaultDatabaseStrategy
- defaultTableStrategy
- defaultKeyGenerateStrategy
- defaultShardingColumn

내부 상세 설정에 대해서는 `rules.tables` 의 설정과 동일하니 참고  

```yaml
rules:
- !SHARDING
  tables: # Sharding table configuration
    ...
  autoTables: # Auto Sharding table configuration
    ...
  defaultDatabaseStrategy: # Default strategy for database sharding
  defaultTableStrategy: # Default strategy for table sharding
  defaultShardingColumn: # Default sharding column name
  defaultKeyGenerateStrategy: # Default Key generator strategy
```

### Sharding Algorithm

`Rules` 에서 아래 샤딩처리에 사용될 `Algorithms` 들을 정의 할 수 있다.  
대부분 비즈니스 로직에서 사용가능한 알고리즘이 내장되어 있다.  

```yaml
rules:
- !SHARDING # 샤딩 설정 시작
  tables: # 샤딩할 테이블 목록 정의 시작
    # ...
    # ...
  autoTables: # Auto Sharding table configuration
    # ...
    # ...

  # Sharding algorithm configuration
  shardingAlgorithms:
    <sharding_algorithm_name> (+): # Sharding algorithm name
      type: # Sharding algorithm type
      props: # Sharding algorithm properties
      # ...
  
  # Key generate algorithm configuration
  keyGenerators:
    <key_generate_algorithm_name> (+): # Key generate algorithm name
      type: # Key generate algorithm type
      props: # Key generate algorithm properties
      # ...
  
  # Sharding audit algorithm configuration
  auditors:
    <sharding_audit_algorithm_name> (+): # Sharding audit algorithm name
      type: # Sharding audit algorithm type
      props: # Sharding audit algorithm properties
      # ...
```

#### shardingAlgorithms

> shardingAlgorithms: <https://shardingsphere.apache.org/document/current/en/dev-manual/sharding/#shardingalgorithm>
 
주석에 써있는대로 `type` 에 설정된 `sharding algorithm` 종류는 아래가 있다.  

- **MOD**  
  Modulo Sharding Algorithm, 숫자 id 의 모듈러 연산을 통해 샤딩  
- **HASH_MOD**  
  Hash Modulo Sharding Algorithm, 숫자 id 의 해시 연산을 통해 샤딩  
- **VOLUME_RANGE**  
  Volume Based Range Sharding Algorithm, 숫자 id 의 range 를 통해 샤딩  
- **BOUNDARY_RANGE**  
  Boundary Based Range Sharding Algorithm  
- **AUTO_INTERVAL**  
  Mutable interval sharding algorithm, 시간값을 기준으로 샤딩  
- **INTERVAL**  
  Fixed interval sharding algorithm  
- **CLASS_BASED**  
  Class based sharding algorithm  
- **INLINE**
  Inline sharding algorithm	 
- **COMPLEX_INLINE**  
  Complex inline sharding algorithm  
- **HINT_INLINE**
  Hint inline sharding algorithm  

#### keyGenerators

> keyGenerators: <https://shardingsphere.apache.org/document/current/en/dev-manual/infra-algorithm/#keygeneratealgorithm>  

기본 제공되는 알고리즘은 아래 2개.  

- **SNOWFLAKE**  
  Snowflake key generate algorithm  
- **UUID**  
  UUID key generate algorithm  

아래와 같이 `@GeneratedValue(strategy = GenerationType.IDENTITY)` 어노테이션을 사용하면 자동으로 알고리즘에서 제공하는 ID 를 설정해준다.  

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

    protected AccountEntity() {
    }

    public AccountEntity(String userName) {
        this.userName = userName;
        this.createTime = OffsetDateTime.now();
    }
}
```

#### auditors

> auditors: <https://shardingsphere.apache.org/document/current/en/dev-manual/sharding/#shardingauditalgorithm>

요청되는 모든 SQL을 모니터링, 감사를 수행한다.  

기본 제공되는 알고리즘은 아래 1개  

- **DML_SHARDING_CONDITIONS**  
  샤딩 조건이 없는 DML을 금지하는 감사 알고리즘  

### 샘플 yaml  

> <https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sharding/>  

위 url 에서 `t_account` 의 테이블 샤딩 부분만 수정하였다.  

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password: root
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3307/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password: root

rules:
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: ds_${0..1}.t_order_${0..1}
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: t_order_inline
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
        auditStrategy:
          auditorNames:
            - sharding_key_required_auditor
          allowHintDisable: true
      t_order_item:
        actualDataNodes: ds_${0..1}.t_order_item_${0..1}
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: t_order_item_inline
        keyGenerateStrategy:
          column: order_item_id
          keyGeneratorName: snowflake
      t_account:
        actualDataNodes: ds_${0..1}.t_account
        tableStrategy:
          none:
        keyGenerateStrategy:
          column: account_id
          keyGeneratorName: snowflake
    defaultShardingColumn: account_id
    bindingTables:
      - t_order,t_order_item
    defaultDatabaseStrategy:
      standard:
        shardingColumn: account_id
        shardingAlgorithmName: database_inline
    defaultTableStrategy:
      none:

    shardingAlgorithms:
      database_inline:
        type: INLINE
        props:
          algorithm-expression: ds_${account_id % 2}
      t_order_inline:
        type: INLINE
        props:
          algorithm-expression: t_order_${order_id % 2}
      t_order_item_inline:
        type: INLINE
        props:
          algorithm-expression: t_order_item_${order_id % 2}
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
        props:
          worker-id: 123
    auditors:
      sharding_key_required_auditor:
        type: DML_SHARDING_CONDITIONS

  - !BROADCAST
    tables: # Broadcast tables
      - t_address

props:
  sql-show: true
```

<!-- 
#### DISTRIBUTED TRANSACTION

분산 트랜잭션 지원, 여러 노드에 저장되어있는 데이터의 변경을 하나의 트랜잭션으로 관리할 수 있도록 설정  

제공하는 분산 트랜잭션은 아래와 같다.  

- `LOCAL`
- `XA(Narayana/Atomikos )`  
- `BASE(Seata)`  

```yaml
# https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/rules/transaction/
transaction:
  defaultType: LOCAL
```

#### discoveryTypes

DB 검색 기능을 활성화하는 데 사용됩니다. 이 설정을 사용하면 ShardingSphere는 데이터베이스의 테이블 구조를 자동으로 검색하여 자동 샤딩 설정을 수행할 수 있습니다.
DB 장애를 감지하고, failover 를 수행, read replica 를 write replica 로 격상하여 가용성을 보장한다.  
 -->

### Rules 기타 기능  

`Sharding` 외에도 `Rules` 를 통해 기타기능을 설정가능하다.  
각 기능에 필요한 커스텀한 알고리즘들이 내장되어 있다.  

#### BROADCAST

모든 데이터 노드에 동일하게 복제하여 저장하는 테이블
아래 용도로 사용한다.  

- 공통 참조 데이터 저장  
- 조인 성능 향상  
- 간단한 데이터 일관성 유지  

```yaml
rules:
- !BROADCAST
  # https://shardingsphere.apache.org/document/current/en/user-manual/shardingsphere-jdbc/yaml-config/rules/broadcast/
  tables: # Broadcast tables
    - <table_name>
    - <table_name>
```

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !BROADCAST
  tables:
    - t_address
```

#### READWRITE_SPLITTING  

`read/write replica` 의 요청을 분리해주는 설정.  

```yaml
rules:
- !READWRITE_SPLITTING
  # loadBalancerName is specified by users, and its property has to be consistent with that of loadBalancerName in read/write splitting rules.
  loadBalancers:
      # type and props, please refer to the built-in read/write splitting algorithm load balancer: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/load-balance/
      type: xxx
      props:
        xxx: xxx
```

```yaml
rules:
- !READWRITE_SPLITTING
  dataSourceGroups:
    readwrite_ds:
      writeDataSourceName: write_ds
      readDataSourceNames:
        - read_ds_0
        - read_ds_1
      transactionalReadQueryStrategy: PRIMARY
      loadBalancerName: random
  loadBalancers:
    random:
      type: RANDOM
```

#### MASK  

민감한 데이터를 보호하기 위해 데이터를 마스킹, 

```yaml
rules:
- !MASK
  # maskAlgorithmName is specified by users, and its property should be consistent with that of maskAlgorithm in mask rules.
  maskAlgorithms:
    <maskAlgorithmName>:
      # type and props, please refer to the built-in mask algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/mask/
      type: xxx
      props:
        xxx: xxx
```

```yaml
rules:
- !MASK
  tables:
    t_user:
      columns:
        password:
          maskAlgorithm: md5_mask
        email:
          maskAlgorithm: mask_before_special_chars_mask
        telephone:
          maskAlgorithm: keep_first_n_last_m_mask

  maskAlgorithms:
    md5_mask:
      type: MD5
    mask_before_special_chars_mask:
      type: MASK_BEFORE_SPECIAL_CHARS
      props:
        special-chars: '@'
        replace-char: '*'
    keep_first_n_last_m_mask:
      type: KEEP_FIRST_N_LAST_M
      props:
        first-n: 3
        last-m: 4
        replace-char: '*'
```

#### ENCRYPT

저장되는 데이터 암호화를 지원, AES, RSA 등의 암호화 알고리즘을 지원한다.  

```yaml
rules:
- !ENCRYPT
  # encryptorName is specified by users, and its property should be consistent with that of encryptorName in encryption rules.
  encryptors:
    <encryptorName>:
      # type and props, please refer to the built-in encryption algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/encrypt/
      type: xxx
      props:
      # ...
```

```yaml
rules:
- !ENCRYPT
  tables:
    t_user:
      columns:
        username:
          cipher:
            name: username
            encryptorName: name_encryptor
          assistedQuery:
            name: assisted_username
            encryptorName: assisted_encryptor
  encryptors:
    name_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    assisted_encryptor:
      type: MD5
      props:
        salt: 123456
```

#### SHADOW

프로덕션 환경과 동일한 테스트DB(shadow DB)을 구성해놓았다면 `shadowAlgorithms` 을 사용해 동일한 어플리케이션 실행 환경에서 shadow DB 테스트가 가능하다.  
테이블 칼럼값이나 SQL 힌트를 사용해 분기처리를 위한 알고리즘을 설정한다.  

```yaml
rules:
- !SHADOW
  # shadowAlgorithmName is specified by users, and its property has to be consistent with that of shadowAlgorithmNames in shadow DB rules.
  shadowAlgorithms:
    <shadowAlgorithmName>:
      # type and props, please refer to the built-in shadow DB algorithm: https://shardingsphere.apache.org/document/current/en/user-manual/common-config/builtin-algorithm/shadow/
      type: xxx
      props:
        xxx: xxx
```

```yaml
  # discoveryTypeName is specified by users, and its property has to be consistent with that of discoveryTypeName in the database discovery rules.
  discoveryTypes:
    type: # Database discovery type, such as: MGR、openGauss
    props:
      # ...
```

### 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/sharding-demo/sharding-sphere>
