---
title:  "Spring - 스프링 MyBatis!"

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

## 스프링 MyBatis

MyBatis 관련 자료는 아래 사이트에서 구할 수 있다.  

> https://blog.mybatis.org/  
> http://www.mybatis.org/mybatis-3/ko/index.html  


### 개요  

*마이바티스는 개발자가 지정한 SQL, 저장프로시저 그리고 몇가지 고급 매핑을 지원하는 퍼시스턴스 프레임워크이다. 마이바티스는 JDBC로 처리하는 상당부분의 코드와 파라미터 설정및 결과 매핑을 대신해준다.*   
> iBatis와 MyBatis의 차이: 1 ~ 2 버전의 Spring에선 iBatis라하고 3버전부턴 MyBatis라고 하고있음.  

> 출처: http://www.mybatis.org/mybatis-3/ko/index.html

전에 `JdbcTemplate`을 배워 나름 간단하게 Sql문을 실행하여 결과값을 받아와 각종 DTO객체에 넣어 사용하였다.  
> https://kouzie.github.io/spring/Spring-DB연동/#jdbctemplate  

MyBatis를 사용하면 좀더 간단하고 직관적인 코드를 사용해 JDBC연동 및 쿼리 결과값을 가져올 수 있다.  

스프링 4 버전에는 `MyBatis/iBatis` 와의 연동기능이 제공되지 않는다....  
다행이도 MyBatis쪽에서 Spring과의 연동 모듈을 제공한다.  

`pom.xml`에 다음 `<dependeny>`태그를 추가해 MyBatis 모듈을 추가하자.   
(`mybatis 3.2.3버전`, `maybatis-spring 1.2.2`를 설치)  

```xml
...
...
<!-- MyBatis -->
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>3.2.3</version>
</dependency>
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis-spring</artifactId>
  <version>1.2.2</version>
</dependency>
...
```
`mybatis`모듈을 `Mybatis Framework`를 사용할 수 있는 jar파일.  
`mybatis-spring`모듈은 `Spring Framework`와 `Mybatis Framework`을 같이 사용할 수 있도록 하는 연동 모듈, 스프링의 `DataSource`및 트랜잭션 관리 기능을 **MyBatis와 연동하는데 필요한 기능을 제공**한다.   

MyBatis를 사용하면 Dao객체를 생략하고 단순 xml파일로만 해당 객체를 만들어 관리할 수 있다.  
단순한 구조의 xml파일을 통해 Dao객체를 생성, 관리할 수 있어 유지보수 측면에서 효과적이다.  

xml을 통해 SQL을 실행하고 트랜잭션 제어를 위한 API를 제공하는 `SqlSession`(DAO같은 객체) 가 만들어진다.  

이런 `SqlSession`를 스프링과 연동하여 DB지원기능을 구현한 클래스가 `SqlSessionTemplate`  

xml설정에 따라 `SqlSessionTemplate`을 생성하는 구성요소가 `SqlSessionFactoryBean` 클래스이다.

위 2개 클래스가 `mybatis-spring`의 핵심 클래스이다.  


## MyBatis를 이용한 Dao객체 구현

MyBatis를 이용해 Dao객체 만드는 방법은 2가지다.

1. SqlSessionTemplate을 이용한 DAO 생성
2. 매퍼 동적생성을 이용한 DAO 생성

먼저 `SqlSessionTemplate을 이용한 DAO 생성`을 알아보자.  

  
### SqlSessionTemplate을 이용한 DAO 생성

MyBatis를 사용하려면 일단 **`mybatis-spring`모듈이 제공하는 `SqlSessionFactoryBean`와 `SqlSessionTemplate` 빈 객체를 생성**해야한다.  

일반적으로 DB연동 관련 객체들은 하위 xml설정에서도 사용할 수 있도록 `root AppliactionContext`에서 생성하고 관리함으로  
`service-context.xml`에서 생성하자.

```xml
<!-- service-context.xml -->
...
<!-- SqlSessionFactoryBean 빈 객체 생성 -->
<bean id="sqlSessionFactoryBean" class="org.mybatis.spring.SqlSessionFactoryBean">
  <property name="dataSource" ref="dataSource"/>
  <property name="mapperLocations">
    <list>
      <value>classpath:org/sist/web/newlecture/dao/mapper/NLNoticeDao.xml</value>
    </list>
  </property>
</bean>

<!-- SqlSession 빈 객체 생성 -->
<bean id="sqlSession" class="org.mybatis.spring.SqlSessionTemplate">
  <constructor-arg ref="sqlSessionFactoryBean"/>
</bean>
...
```
`sqlSessionFactoryBean` 빈 객체의 `mapperLocations`필드에 `List` 콜렉션 타입으로 여러 xml파일이 들어가는데  

앞으로 생성할 `SqlSession` 객체의 설정을 이 xml파일에 작성한다, 이런 파일을 매퍼파일이라 한다.  

우리는 지금까지 직접 `...Dao.java` 파일을 만들어 Dao객체를 자바코딩으로 구현해서 사용해왔다.  
Dao객체 안에는 각종 쿼리문이 `JdbcTemplate`을통해 실행되었는데 이 Dao객체 만드는 과정을 xml과 MyBatis 프레임워크로 바꾸자.  

매퍼파일만 있으면 `SqlSessionFactoryBean`가 DAO객체를 생성해주기 때문에 매퍼파일을 작성해보자.  

매퍼파일에는 프로그램에 필요한 sql 쿼리만 들어가면 된다.  (유지보수 확장성 증가)
> http://www.mybatis.org/mybatis-3/ko/configuration.html#mappers

매퍼파일을 만드려면 먼저 만들 Dao객체의 인터페이스를 정의해야 한다.(사용할 함수명, 매개변수 타입 및 개수)  

```java
package org.sist.web.newlecture.dao;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;
import org.sist.web.newlecture.vo.Notice;


public interface NoticeDao{
	public int getCount(String field, String query) throws ClassNotFoundException, SQLException;
	public List<Notice> getNotices(int page, String field, String query) throws ClassNotFoundException, SQLException;
	public int delete(String seq) throws ClassNotFoundException, SQLException;
	public int update(Notice notice) throws ClassNotFoundException, SQLException;
	public Notice getNotice(String seq) throws ClassNotFoundException, SQLException;
	public int insert(Notice notice) throws ClassNotFoundException, SQLException;  
	public void hitup(String seq);
	public int gethit(String seq);
}
```
위 메서드들에 대한 구현을 매퍼파일인 xml파일에 작성하면 된다.  


```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.sist.web.newlecture.dao.NoticeDao"> 
	...
  ...
</mapper>
```
가장 바깥에 `<mapper>`태그가 있고 구현할 DAO객체의 인터페이스 풀클래스 네임을 속성값으로 지정한다.  

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper
    PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.sist.web.newlecture.dao.NoticeDao"> 
	<select id="getCount" resultType="Integer">
		SELECT COUNT(*) CNT 
		FROM NOTICES 
		WHERE ${ param1 } LIKE '${ param2 }'
	</select>
  <update id="update" parameterType="org.sist.web.newlecture.vo.Notice">
		UPDATE notices SET
		title = #{ title },
		content = #{ content }
		<if test="filesrc != null">
			, filesrc = #{ filesrc }
		</if>
		WHERE SEQ=#{ seq }
	</update>
  <select id="getNotices" resultType="org.sist.web.newlecture.vo.Notice">
		SELECT * 
		FROM(
			SELECT ROWNUM NUM, N.* FROM (
				SELECT * 
				FROM NOTICES 
				WHERE ${field} LIKE '%${query}%' 
				ORDER BY REGDATE DESC
			) N
		)
		WHERE NUM BETWEEN (1 + (#{page}-1)*15) AND (15 + (#{page}-1)*15)
	</select>
</mapper>
```
그 다음엔 메서드 안에서 수행할 쿼리를 `<select>`,`<update>`,`<delete>`,`<insert>` 태그 안에 작성한다.  

만약 반환값이 있다면 `resultType` 속성을 통해 반환타입을 지정 가능하다. (래퍼클래스, Object만 가능)

또한 매개변수로 받는값이 기본형 타입이 아니라면 `parameterType`을 통해 지정가능하다.  
`{}`안에 `parameterType`으로 받은 객체의 필드명을 통해 접근가능하다.  

> 매개변수가 여러개인데 매개변수 타입이 기본형이 아니라면, `parameterType`은 하나만 지정 가능하기 때문에 애매한 상황이 발생한다. 그럴 땐 매개변수로 Map을 받도록 설정하자.  

`${}` - 전달된 파라미터 값을 그대로 변환하여 사용 (홀따옴표, 쌍따옴표는 붙지 않음)  
`#{}` - 전달된 파라미터 값을 자료형의 값에 맞게 변환해서 사용 (int를 넣었다면 홀따옴표 생략, String이라면 홀따옴표 자동 추가)  

대괄호 안에는 `{param1}` 형식으로 매개변수의 인덱스를 통해 매개변수에 접근할 수 있고 **매개변수 식별자명**으로도 접근 가능하다.  

xml에서도 동적쿼리작성이 가능한데 제어문, 반복문 사용이 가능하다.  
위에서도 `<if test="">`태그를 통해 동적쿼리를 작성하였다.  

> http://www.mybatis.org/mybatis-3/ko/dynamic-sql.html  

```xml
<mapper namespace="org.sist.web.newlecture.dao.NoticeDao"> 
  ...
  <insert id="insert">
    <selectKey order="BEFORE" keyProperty="seq" resultType="String"> 
      SELECT NVL(MAX(TO_NUMBER(SEQ)), 0) + 1 FROM NOTICES
    </selectKey>
    INSERT INTO NOTICES(SEQ, TITLE, CONTENT, WRITER, REGDATE, HIT, FILESRC) VALUES
    <!-- (#{seq}, #{title}, #{content}, #{writer}, SYSDATE, 0, #{filesrc, javaType=String, jdbcType=VARCHAR}) -->
    (#{seq}, #{title}, #{content}, #{writer}, SYSDATE, 0, #{filesrc})
  </insert>
</mapper>
```
`INSERT`쿼리를 수행하기 전에 특정한 값을 알아야 한다면 `<selectKey>` 태그를 사용하자.  

> 참고: 하나의 메서드엔 하나의 sql 실행태그, insert하고 해당 시퀀스값을 알아오거나, 조회수 증가작업이나 모두 서비스에서 2개의 메서드를 호출하고 트랜잭션으로 묶어주자...

xml설정이 끝났으면 `@Autowired`어노테이션을 통해 만들어진 `SqlSession`을 가져올 수있다.  

`SqlSession`객체의 `getMapper`메서드를 통해 어떤 Dao객체를 만들지 정할 수 있다.  
```java
public class ...Service {

  @Autowired
  private SqlSession sqlSession;
  public SqlSession getSqlSession() {
    return sqlSession;
  }
  ...
  ...
  public NoticeDto getNoticeBySeq(int seq) {
    ...
    NoticeDao mybatis_NoticeDao = this.sqlSession.getMapper(NoticeDao.class);
    Notice notice = mybatis_NoticeDao.getNotices(seq);
    return notice;
  }
  ...
}
```

### 매퍼 동적생성을 이용한 DAO 생성

간단한 Dao객체의 경우 굳이 매퍼용 xml파일을 만들고 싶지 않을 수 있다.   
그럴땐 어노테이션을 사용해 xml파일을 만들지 않고 Dao객체를 생성할 수 있다.  

우선 기존에 사용했떤 매퍼파일을 주석처리하고 인터페이스에 어노테이션을 사용해 매퍼 동적생성을 이용해 DAO객체를 생성해보자.
```xml
<bean id="sqlSessionFactoryBean" class="org.mybatis.spring.SqlSessionFactoryBean">
  <property name="dataSource" ref="dataSource"></property>
  <!-- 
  <property name="mapperLocations">
    <list>
      <value>classpath:org/sist/web/newlecture/dao/mapper/NLNoticeDao.xml</value>
    </list>
  </property>
  -->
</bean>
```

```java
public interface NoticeDao{
	@Select("SELECT COUNT(*) CNT FROM NOTICES WHERE ${ field } LIKE '${ query }'")
	public int getCount(@Param("field") String field, @Param("query") String query) throws ClassNotFoundException, SQLException;
	
	@Select(" SELECT * " +
			" FROM(" +
			"   SELECT ROWNUM NUM, N.* FROM (" +
			"     SELECT * " +
			"     FROM NOTICES " +
			"     WHERE ${param2} LIKE '%${param3}%' " +
			"     ORDER BY REGDATE DESC" +
			"   ) N" +
			" )" +
			" WHERE NUM BETWEEN (1 + (#{param1}-1)*15) AND (15 + (#{param1}-1)*15)")
	public List<Notice> getNotices(@Param("page") int page, @Param("field") String field, @Param("query") String query) throws ClassNotFoundException, SQLException;
	
	@Delete("DELETE FROM notices WHERE seq = #{seq}")
	public int delete(String seq) throws ClassNotFoundException, SQLException;
	
	@Update(" UPDATE notices SET "+
			" title = #{ title }, "+
			" content = #{ content } "+
			" <if test='filesrc != null'> "+
			"   , filesrc = #{ filesrc } "+
			" </if> "+
			" WHERE SEQ=#{ seq } ")
	public int update(Notice notice) throws ClassNotFoundException, SQLException;
	
	@Select("SELECT * FROM notices WHERE seq = #{seq}")
	public Notice getNotice(String seq) throws ClassNotFoundException, SQLException;
	
	@SelectKey(before=true, keyProperty="seq", resultType=String.class,
			statement="SELECT NVL(MAX(TO_NUMBER(SEQ)), 0) + 1 FROM NOTICES")
	@Insert(" INSERT INTO NOTICES(SEQ, TITLE, CONTENT, WRITER, REGDATE, HIT, FILESRC) VALUES "+
			" (#{seq}, #{title}, #{content}, #{writer}, SYSDATE, 0, #{filesrc} ")
	public int insert(Notice notice) throws ClassNotFoundException, SQLException;  
	
	@Update("UPDATE notices SET hit = hit+1 WHERE seq = #{ seq }")
	public void hitup(String seq);
	
	@Select("SELECT hit FROM notices WHERE seq = #{ seq }")
	public int gethit(String seq);
}
```

`@Select`, `@Update`, `@Delete` 어노테이션을 통해 sql쿼리를 지정할 수 있다.  
또한 `@Param`어노테이션을 매개변수 앞에 사용해 `alias`로 지정할 수 있다.  


### 알아두면 좋을내용


```xml
<!-- XML설정파일에서 -->
<typeAlias type="com.someapp.model.User" alias="User"/>

<!-- SQL매핑 XML파일에서 -->
<select id="selectUsers" resultType="User">
  select id, username, hashedPassword
  from some_table
  where id = #{id}
</select>
```

ResultMap에 대한 중요한 내용은 다 보았다. 하지만 다 본건 아니다. 칼럼명과 프로퍼티명이 다른 경우에 대해 데이터베이스 별칭을 사용하는 것과 다른 방법으로 명시적인 resultMap 을 선언하는 방법이 있다.

```xml
<resultMap id="userResultMap" type="User">
  <id property="id" column="user_id" />
  <result property="username" column="username"/>
  <result property="password" column="password"/>
</resultMap>
```
구문에서는 resultMap속성에 이를 지정하여 참조한다. 예를들면

```xml
<select id="selectUsers" resultMap="userResultMap">
  select user_id, user_name, hashed_password
  from some_table
  where id = #{id}
</select>
```

> `resultType`과 차이점은 `result property`속성값 변경으로 `resultType`에서 강요하던 `smallBigCamel`형식의 이름 지정방법을 따를 필요가없다. 하지만 `resultType`쓰는것이 코드 가독성에 더 좋음으로 `resultMap`사용을 지향하자.  
