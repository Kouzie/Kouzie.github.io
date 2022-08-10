---
title:  "Spring Boot - Mybatis!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Mybatis


MyBatis 관련 자료는 아래 사이트에서 구할 수 있다.  

> https://blog.mybatis.org/  
> http://www.mybatis.org/mybatis-3/ko/index.html  


스프링 부트에서 Mybatis를 사용하려면 아래 dependency를 추가  

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

일반 스프링 프레임워크에서 Mybatis를 사용하기 위해 DAO객체의 인터페이스를 정의하고 이를 구현하기 위한 sql쿼리를 xml에 작성했다.  
그리고 SqlSession 빈 객체를 사용해 생성된 DAO객체를 가져와 sql쿼리를 실행하였다.  

혹읜 xml을 사용하지 않고 어노테이션 안에 쿼리를 작성해 DAO객체를 구현하였다.(아래 링크 참고)  

> https://kouzie.github.io/spring/Spring-스프링-MyBatis/

  
또한 스프링 프레임워크에서 JUnit으로 테스트 시에는 아래처럼 어노테이션을 사용해 매퍼xml의 위치를 지정해 주어야 한다.  
`@ContextConfiguration(locations= {"file:src/main/webapp/WEB-INF/spring/**/*.xml"})`  

스프링 부트에선 필요 없다! (이미 `application.properties`에 정의되어 있기때문에....)

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Log
public class Boot06ApplicationTests {

    @Autowired
    TimeMapper timeMapper;

    @Test
    public void test() {
        log.info("-------------------------");
        log.info("Time: " + timeMapper.getTimeXML());
    }
}


public interface TimeMapper {
    @Select("SELECT now()")
    public String getTime();

    public String getTimeXML();
}

```

### 인터페이스 + 어노테이션


스프링 부트에서 `인터페이스 + 어노테이션`를 사용해 DB와 연결된 DAO객체를 생성해보자.  

우선 간단히 Mysql에서 시간을 `SELECT`하는 쿼리를 가진 인터페이스 작성

```java
public interface TimeMapper {
    @Select("SELECT now()")
    public String getTime();
}
```

그리고 테스트

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Log
public class TestClassForMyBatis {

    @Autowired
    TimeMapper timeMapper;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test1() {
        log.info("-------------------------");
        log.info("Time: " + timeMapper.getTime());
    }
}
```

출력값  
```
2019-09-19 11:25:25.510  INFO 63692 --- [           main] org.zerock.Boot06ApplicationTests        : -------------------------
2019-09-19 11:25:25.552  INFO 63692 --- [           main] org.zerock.Boot06ApplicationTests        : Time: 2019-09-19 02:25:25
```

어노테이션의 장점은 **간단한 구조 + 별도의 설정없이** 바로 사용 가능하다는 점이다.  
단 동적 SQL작성시 약간의 까다로움이 있다.  


```java
@Update("<script>
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </set>
  where id=#{id}
</script>")
```

> 출처: https://www.google.com/search?q=mybatis+annotation+dynamic+query&oq=mybatis+annot&aqs=chrome.3.69i57j0l5.12081j0j4&sourceid=chrome&ie=UTF-8   


### 인터페이스 + XML

이번엔 `인터페이스 + XML` 설정으로 Mybatis 설정을 진행해보자.  

Mybatis를 xml과 함께 사용하려면 Mybatis기본 설정파일인 `mybatis-config.xml`, 
Mybatis매퍼용 파일들을 생성해야한다.  

그리고 이들의 위치를 `application.properties` 파일에 지정해야한다  
`mybatis-config.xml`파일은 기본적으로 `src/main/resource` 디렉토리에서 찾는다.  
이 외에 위치할 시에는 `mybatis.config-location` 속성으로 지정할 수 있다.  

```conf
# mybatis.config-location=classpath:mybatis-config.xml #생략 가능
mybatis.mapper-locations=classpath:mappers/**/*.xml
```

SQL문 정의문서는 `mybatis.mapper-locations`속성으로 정의한다.  

잠시 기존의 스프링 프레임워크와 사용법을 비교해 보자.  
스프링 프레임워크에선 아래처럼 `SqlSession`으로 일일이 xml파일에 정의해 놓은 위치를 문자열 name을 통해 찾아서 지정해 주어야 한다.  

```java
@Repository
public class MemberDAOImpl implements MemberDAO{
	private static final Logger logger = LoggerFactory.getLogger(MemberDAOImpl.class);
	private String namespace = "org.sist.project.mapper.MemberMapper";

	@Autowired
	private SqlSession sqlSession;

	@Override
	public List<MemberVO> selectAdminList() throws Exception {
		return sqlSession.selectList("org.sist.project.mapper.MemberMapper.selectAdminList");
	}
}
```

하지만 스프링 부트에선 `interface`정의하고 해당 추상 **메서드 이름만 일치**시켜 놓으면 된다.  
애초에 `sqlSessionFactory` 빈객체를 만드는 코드가 없다.(아마 `dependency` 추가되면서 자동으로 만들어지는 듯?)

그럼 sql문이 작성된 xml안의 sql문과 연동할 매퍼클래스를 정의하고 연동시키는 작업을 수행해보자.  

위에 지정한 `mybatis.mapper-locations=classpath:mappers/**/*.xml` 설정대로 `src/main/resources/mappers/xxx.xml` 형식으로 파일을 생성한다.  

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.example.mapper.UserMapper" >
  <resultMap id="UserModel" type="com.example.model.Userr" >
    <id column="id" property="id" jdbcType="BIGINT" />
    <result column="name" property="name" jdbcType="VARCHAR" />
    <result column="password" property="password" jdbcType="VARCHAR" />
    <result column="create_time" property="createTime" jdbcType="TIMESTAMP" />
    <result column="update_time" property="updateTime" jdbcType="TIMESTAMP" />
  </resultMap>
  <sql id="base_list" >
    id, name, password, create_time, update_time
  </sql>
  <select id="selectById" resultMap="UserModel" parameterType="java.lang.Long">
    select 
    <include refid="base_list" />
    from user
    where id = #{id,jdbcType=BIGINT}
  </select>
  <delete id="deleteById" parameterType="java.lang.Long" >
    delete from user
    where id = #{id,jdbcType=BIGINT}
  </delete>
  <insert id="insert" parameterType="com.example.model.Userr" >
    insert into user (id, name, password, create_time, update_time)
    values (#{id,jdbcType=BIGINT}, #{vendor,jdbcType=VARCHAR},
        #{createTime,jdbcType=TIMESTAMP}, #{updateTime,jdbcType=TIMESTAMP})
  </insert>
</mapper>
```

이제 `mapper`에 정의된 쿼리문 `id`만 메서드명과 일치시켜 인터페이스를 생성하면 된다.  

```java
public interface UserMapper {
    User selectById(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
    int insert(User user);
}
```