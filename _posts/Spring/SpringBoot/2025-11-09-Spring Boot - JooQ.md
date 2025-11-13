---
title:  "Spring Boot - Pool config, jOOQ!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Pool config

Spring Boot 2.x 이상은 기본 커넥션 풀로 HikariCP 를 사용하며 아래 설정 가능하다.  

- **maximumPoolSize**: 풀에서 유지할 최대 커넥션 수(기본값 10).
- **minimumIdle**: 유휴 상태로 유지할 최소 커넥션 수(기본값 maximumPoolSize).
- **maxLifetime**: 커넥션의 최대 생존 시간(기본 30분).
- **connectionTimeout**: 커넥션을 얻기 위한 최대 대기 시간(기본 30초).
- **idleTimeout**: 유휴 커넥션이 풀에 유지되는 시간(기본 10분).

`AWS 4 vCPU, 16GB` 자원에서 `Tomcat max-threads` 를 500 정도를 유지할 경우 아래와 같은 설정을 추천한다.  

```yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50 # 스레드 수(500)의 10~20% 수준으로 설정.
      minimum-idle: 10 # 코어 수(4) × 2~3배 정도로 설정
      max-lifetime: 1800000  # 30분(기본값)
      connection-timeout: 30000  # 30초(기본값)
      idle-timeout: 600000  # 10분(기본값)
```

MySQL 서버 설정도 커넥션 개수를 기본값 151 에서 500 으로 확장  

> 보통 4vCPU 16GB 정도 리소스에서 500개 정도는 커버 가능하다.  

```conf
SHOW VARIABLES LIKE 'max_connections'; -- 151
SET GLOBAL max_connections = 500;
```

커넥션 수가 부족하다 느껴진다면 read, write 인스턴스 분리, 샤딩 과 같은 물리적인 증가방법을 사용하거나
MongoDB 와 같이 커넥션 수 제한이 없는 NoSQL DB 사용을 권장한다.  

## jOOQ

`jOOQ(Java Object Oriented Querying)`

> <https://www.jooq.org/>
> <https://github.com/jOOQ/jOOQ>

타입 안전 SQL 빌더, 코드생성도구.  
데이터베이스 스키마 혹은 DDL(데이터 정의어)쿼리를 기반으로 Java 클래스를 자동 생성하여 타입 안전한 쿼리를 작성할 수 있다.  

Spring Boot 공식 지원 스타터 패키지 존재.

```groovy
dependencies {
    // JOOQ + Spring Data JDBC 함께 사용
    implementation 'org.springframework.boot:spring-boot-starter-jooq'
    implementation 'org.springframework.boot:spring-boot-starter-data-jdbc' // Spring Data JDBC 같이 테스트
    implementation 'com.h2database:h2'
}

configurations {
    jooqCodegen {
        extendsFrom implementation
    }
}

dependencies {
    // JOOQ code generation (DDLDatabase 사용)
    // jooq-codegen은 Spring Boot가 관리하므로 버전 생략 가능
    // jooq-meta-extensions는 Spring Boot가 관리하지 않으므로 버전 명시 필요
    jooqCodegen 'org.jooq:jooq-codegen'  // Spring Boot가 관리하는 버전 사용
    jooqCodegen 'org.jooq:jooq-meta-extensions:3.18.13'  // DDLDatabase 지원 (버전 명시 필요)
    jooqCodegen 'com.h2database:h2'
}
```

**장점:**

- 타입 안전성: 컴파일 타임에 쿼리 오류를 발견할 수 있다.
- SQL 친화적: SQL에 가까운 형태로 쿼리를 작성할 수 있다.
- 복잡한 쿼리: 복잡한 SQL 쿼리를 쉽게 작성할 수 있다.
- 동적 쿼리: 조건에 따라 동적으로 쿼리를 생성할 수 있다.
- 성능: 생성된 쿼리가 최적화되어 있다.

**단점:**

- 코드 생성: 스키마 변경 시 코드 재생성이 필요하다.
- 학습 곡선: jOOQ 문법을 익혀야 한다.
- 의존성: 데이터베이스 스키마에 의존적이다.


### 코드생성

데이터베이스 연결 혹은 지정한 DDL 파일로부터 코드를 생성한다.  

아래는 `src/main/resources/jooq-codegen.xml` 을 통해 코드생성하는 과정.  

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<configuration xmlns="http://www.jooq.org/xsd/jooq-codegen-3.18.0.xsd">
    <generator>
        <database>
            <name>org.jooq.meta.extensions.ddl.DDLDatabase</name>
            <properties>
                <property>
                    <key>scripts</key>
                    <value>src/main/resources/init.sql</value>
                </property>
                <property>
                    <key>defaultNameCase</key>
                    <value>lower</value>
                </property>
            </properties>
            <!-- TIMESTAMP 타입을 Instant로 강제 매핑 -->
            <forcedTypes>
                <forcedType>
                    <name>INSTANT</name>
                    <types>TIMESTAMP</types>
                </forcedType>
            </forcedTypes>
        </database>
        <target>
            <packageName>com.example.jooq.generated</packageName>
            <directory>src/main/java</directory>
        </target>
        <generate>
            <pojos>true</pojos>
            <pojosEqualsAndHashCode>true</pojosEqualsAndHashCode>
            <pojosToString>true</pojosToString>
            <javaTimeTypes>true</javaTimeTypes>
        </generate>
    </generator>
</configuration>
```

`init.sql` 테이블 DDL 작성.

```sql
-- 사용자(Author) 테이블 생성
CREATE TABLE IF NOT EXISTS author (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 게시판 테이블 생성 (author_id 추가)
CREATE TABLE IF NOT EXISTS board (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);

-- 댓글 테이블 생성
CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_board_id ON comment(board_id);
CREATE INDEX IF NOT EXISTS idx_board_created_at ON board(created_at);
CREATE INDEX IF NOT EXISTS idx_board_author_id ON board(author_id);
CREATE INDEX IF NOT EXISTS idx_author_email ON author(email);
```

Gradle에서 코드 생성 태스크를 추가한다.

```groovy
// JOOQ code generation 태스크 (DDLDatabase 방식: init.sql에서 직접 생성)
task jooqCodegen(type: JavaExec) {
    classpath = configurations.jooqCodegen
    mainClass = 'org.jooq.codegen.GenerationTool'
    args = ['src/main/resources/jooq-codegen.xml']
}

// JOOQ code generation을 compileJava 전에 실행 (필요시 주석 해제)
tasks.named('compileJava') {
    dependsOn 'jooqCodegen'
}
```

코드 생성 실행하여 `jooq-codegen.xml` 파일에 지정한 `com.example.jooq.generated` 패키지 위치에 코드생성 확인.  

```bash
./gradlew jooqCodegen
```

실제 데이터베이스에 연결하여 스키마를 읽어들일경우 아래 xml 참고.  

```xml
<configuration>
    <jdbc>
        <driver>com.mysql.cj.jdbc.Driver</driver>
        <url>jdbc:mysql://localhost:3306/demo</url>
        <user>root</user>
        <password>root</password>
    </jdbc>
    <generator>
        <database>
            <name>org.jooq.meta.mysql.MySQLDatabase</name>
            <inputSchema>demo</inputSchema>
        </database>
        <target>
            <packageName>com.example.jooq</packageName>
            <directory>src/main/java</directory>
        </target>
    </generator>
</configuration>
```

### 기본 사용법

생성된 클래스를 사용하여 타입 안전한 쿼리를 작성할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;  // Spring Data JDBC 인터페이스
    private final DSLContext dsl;  // JOOQ DSL (다이나믹 쿼리용)

    // 게시판 + 작성자 조회 (DTO 사용) - Spring Data JDBC 사용
    public List<BoardWithAuthorDto> findAllWithAuthor() {
        return boardRepository.findAllWithAuthor();
    }

    // 게시판 ID로 조회: 게시판 + 작성자 (JOOQ JOIN 사용)
    public BoardAuthorDto findByIdWithAuthor(Long boardId) {
        Record record = dsl.select()
                .from(BOARD)
                .join(AUTHOR).on(BOARD.AUTHOR_ID.eq(AUTHOR.ID))
                .where(BOARD.ID.eq(boardId))
                .fetchOne();

        if (record == null) return null;

        Board board = record.into(BOARD).into(Board.class);
        Author author = record.into(AUTHOR).into(Author.class);
        BoardAuthorVo vo = new BoardAuthorVo(board, author, null);

        return convertToDto(vo);
    }

    // INSERT
    @Transactional
    public BoardDto createBoard(BoardCreateRequest request) {
        Instant now = Instant.now();

        // INSERT 실행 후 생성된 ID 반환 (returning() 사용)
        // 주의: MySQL/H2는 RETURNING 절을 지원하지 않을 수 있음
        Record1<Long> boardRecord = dsl.insertInto(BOARD)
                .set(BOARD.TITLE, request.getTitle())
                .set(BOARD.CONTENT, request.getContent())
                .set(BOARD.AUTHOR_ID, request.getAuthorId())
                .set(BOARD.CREATED_AT, now)
                .set(BOARD.UPDATED_AT, now)
                .set(BOARD.VIEW_COUNT, 0)
                .returningResult(BOARD.ID) // 자동으로 MySQL/H2 fallback: LAST_INSERT_ID() 사용
                .fetchOne();

        Long boardId = boardRecord.getValue(BOARD.ID);
        Board board = findById(boardId);
        return convertToDto(board);
    }

    // UPDATE
    @Transactional
    public BoardDto updateBoard(Long boardId, BoardUpdateRequest request) {
        int updatedRows = dsl.update(BOARD)
                .set(BOARD.TITLE, request.getTitle())
                .set(BOARD.CONTENT, request.getContent())
                .set(BOARD.UPDATED_AT, Instant.now())
                .where(BOARD.ID.eq(boardId))
                .execute();

        if (updatedRows == 0) {
            return null; // 게시판이 존재하지 않음
        }

        Board board = findById(boardId);
        return convertToDto(board);
    }

    // DELETE
    @Transactional
    public boolean deleteBoard(Long boardId) {
        int deletedRows = dsl.deleteFrom(BOARD)
                .where(BOARD.ID.eq(boardId))
                .execute();
        return deletedRows > 0;
    }

    // JOOQ로 단일 게시판 조회 (내부 메서드)
    private Board findById(Long boardId) {
        BoardRecord record = dsl.selectFrom(BOARD)
                .where(BOARD.ID.eq(boardId))
                .fetchOne();
        return record != null ? record.into(Board.class) : null;
    }
}
```

### 동적 쿼리

조건에 따라 동적으로 쿼리를 생성할 수 있다.

```java
// 게시판 검색 (다이나믹 파라미터): 게시판 + 작성자 (JOOQ 사용)
public List<BoardAuthorDto> searchBoards(BoardSearchRequest request) {
    Condition condition = null;

    if (request.getTitle() != null && !request.getTitle().isEmpty()) {
        condition = BOARD.TITLE.like("%" + request.getTitle() + "%");
    }

    if (request.getContent() != null && !request.getContent().isEmpty()) {
        condition = condition == null
                ? BOARD.CONTENT.like("%" + request.getContent() + "%")
                : condition.and(BOARD.CONTENT.like("%" + request.getContent() + "%"));
    }

    SelectJoinStep<Record> selectStep = dsl.select()
            .from(BOARD)
            .join(AUTHOR).on(BOARD.AUTHOR_ID.eq(AUTHOR.ID));

    if (condition != null) {
        return selectStep.where(condition)
                .orderBy(BOARD.ID.desc())
                .fetch()
                .stream()
                .map(record -> {
                    Board board = record.into(BOARD).into(Board.class);
                    Author author = record.into(AUTHOR).into(Author.class);
                    BoardAuthorVo vo = new BoardAuthorVo(board, author, null);
                    return convertToDto(vo);
                })
                .collect(Collectors.toList());
    } else {
        return selectStep.orderBy(BOARD.ID.desc())
                .fetch()
                .stream()
                .map(record -> {
                    Board board = record.into(BOARD).into(Board.class);
                    Author author = record.into(AUTHOR).into(Author.class);
                    BoardAuthorVo vo = new BoardAuthorVo(board, author, null);
                    return convertToDto(vo);
                })
                .collect(Collectors.toList());
    }
}
```

### 트랜잭션

`@Transactional` 어노테이션을 사용하여 트랜잭션을 관리할 수 있다. jOOQ는 Spring의 트랜잭션 관리와 완벽하게 통합된다.

```java
@Transactional
public void createUserWithOrder(String name, String email) {
    // 여러 쿼리를 하나의 트랜잭션으로 묶을 수 있다
    dsl.insertInto(AUTHOR)
            .set(AUTHOR.NAME, name)
            .set(AUTHOR.EMAIL, email)
            .execute();
    
    dsl.insertInto(BOARD)
            .set(BOARD.TITLE, "제목")
            .set(BOARD.CONTENT, "내용")
            .set(BOARD.AUTHOR_ID, 1L)
            .execute();
}
```

## Spring Data JDBC와 함께 사용

단순 조회는 `spring-boot-starter-data-jdbc` 를 사용하고, 복잡한 동적 쿼리는 jOOQ를 사용하는 것도 추천.

```java
@Repository
public interface BoardRepository extends CrudRepository<Board, Long> {
    // 생성자 기반 자동 매핑 시도 (컬럼 순서와 생성자 매개변수 순서 일치 필요)
    @Query("""
            SELECT b.id, b.title, b.content, b.author_id, b.created_at, b.updated_at, b.view_count,
                   a.id AS author_id_value, a.name AS author_name,
                   a.email AS author_email, a.created_at AS author_created_at,
                   a.updated_at AS author_updated_at
            FROM board b
            INNER JOIN author a ON b.author_id = a.id
            ORDER BY b.id DESC
            """)
    List<BoardWithAuthorDto> findAllWithAuthor();
}

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByBoardId(Long boardId);
    List<Comment> findByBoardIdIn(List<Long> boardIds);
}
```

```java
@Service
@RequiredArgsConstructor
public class BoardService {
    
    private final BoardRepository boardRepository;  // Spring Data JDBC
    private final DSLContext dsl;  // JOOQ (동적 쿼리용)
    
    // 단순 조회는 Spring Data JDBC 사용
    public List<BoardWithAuthorDto> findAllWithAuthor() {
        return boardRepository.findAllWithAuthor();
    }
    
    // 동적 쿼리는 jOOQ 사용
    public List<BoardAuthorDto> searchBoards(BoardSearchRequest request) {
        // ... jOOQ 동적 쿼리
    }
}
```

## 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/jooq-demo>