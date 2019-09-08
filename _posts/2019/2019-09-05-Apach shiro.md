---
title:  "Apach shiro!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - java

tags:
  - shiro

toc: true

---


## Apach shiro

스프링 시큐리티와 같은 보안 프레임워크. 인증, 권한부여, 암호화, 세션관리 기능을 제공한다.  

> https://shiro.apache.org/


## Subject, SecurityManage, Realm

여기서부터는 Shiro 프레임워크에 중요한 세 가지 개념. `Subject`, `SecurityManage`, `Realm` 이라는 인터페이스로 제공된다.  
스프링 시큐리티에도 Provider, Authentication, UserDetailManager 등과 같은 기능이 있듯 아파치 시로에도 비슷한 역할을 한다 생각하면 된다.  

> 출처: https://dololak.tistory.com/629, https://www.infoq.com/articles/apache-shiro/

### Subject

현재 응용프로그램에 접속된 사용자(주체)를 뜻한다.  
Session과 비슷한 객체로 Session을 기반으로 Subject를 만들고 더 많은 정보를 내포한다.  


### SecurityManage

SecurityManager는 어플리케이션에 접속해 있는 모든 사용자를 관리하는 역할.  

Subject를 만들고 관리하고 Shiro를 사용하기 위해 SecurityManage 우선 설정해야한다.  

### Realm

shiro와 사용자 데이터 사이의 연결고리 역할, 사용자 데이터를 Realm에 설정하고 Realm을 통해 로그인 처리가 가능하다.  

## 전체적인 구조

![shiro1]({{ "/assets/2019/shiro1.png" | absolute_url }}){: .shadow}  

전체적인 구조는 위 사진과 같다.  
실제 사용자가 로그인 요청(application code)을 하고 서버에선 `Subject`의 `login`메서드를 호출한다.  
내부적으론 `SecurityManager`에 로직에 따라 진행되며 로그인에 필요한 정보는 `Realm`을 통해 얻는다.  

![shiro2]({{ "/assets/2019/shiro2.png" | absolute_url }}){: .shadow}  

그림과 같이 Security Manager는 시로의 핵심 기능 구현체로 Realm뿐 아니라 인증, 인가 구현체 또한 관리한다.

### Authenticator (org.apache.shiro.authc.Authenticator) (인증)

로그인과 같은 사용자 인증을 진행하는 구현체로 로그인 로직은 Authenticator에 의해 실행된다.  
`Realms`과 협력하여 사용자 데이터를 가져와 인증을 진행한다.  

### Authentication Strategy

만약 로그인 과정이 여러단계(여러 Realm)을 통해 진행된다면 하나만 통과해도 진행 시킬것인지, 무조건 모두 통과해야 진행시킬 것인지 결정 가능하다.  

### Authorizer (org.apache.shiro.authz.Authorizer) (인가)

사용자 접근 제어를 관리하는 구현체, `Authenticator` 처럼 다른 구현체와 협력해 role, permission 정보를 가져와 작업한다.  

## shiro - pom.xml 설정

스프링 부트에서 shiro를 사용하기위해 밑의 `dependency`추가


```xml
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-ehcache</artifactId>
    <version>1.4.1</version>
</dependency>
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring</artifactId>
    <version>1.4.1</version>
    <exclusions>
        <exclusion>
            <artifactId>slf4j-api</artifactId>
            <groupId>org.slf4j</groupId>
        </exclusion>
    </exclusions>
</dependency>
```

스프링에서 직접 웹을 지원한다면 shiro 필터 사용을 위해 shiro-web dependency도 추가.  

```xml
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-web</artifactId>
    <version>1.4.1</version>
</dependency>
```

각 dependency에 대한 설명은 아래 사이트 참고

> https://shiro.apache.org/download.html#latest



## EhCache

> https://www.ehcache.org/

> It's the most widely-used Java-based cache

다른 여러 라이브러리에서 자주 사용되는 캐시 라이브러리이다.  
캐시를 사용하면 DB접근 횟수를 줄일 수 있고 빠른 피드백이 가능하다, 

### Local Cache vs Global Cache  

**Local Cache**  
* Local 장비 내에서만 사용 되는 캐시  
* Local 장비의 Resource를 이용한다 (Memory, Disk)  
* Local에서 작동 되기 때문에 속도가 빠르다.  
* Local에서만 작동되기 때문에 다른 서버와 데이터 공유가 어렵다  

**Global Cache**  
* 여러 서버에서 Cache Server에 접근하여 사용하는 캐시  
* 데이터를 분산하여 저장 할 수 있다.  
  * Replication - 데이터를 복제  
  * Sharding - 데이터를 분산하여 저장  
* Local Cache에 비해 상대적으로 느리다 (네트워크 트래픽)  
* 별도의 Cache Server를 이용하기 때문에 서버 간 데이터 공유가 쉽다.  

EhCache는 Local Cache를 사용한다.  
> reddis는 Global Chache  


### CacheManager 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:noNamespaceSchemaLocation="http://ehcache.org/ehcache.xsd"
         updateCheck="false" monitoring="autodetect"
         dynamicConfig="true">

    <diskStore path="java.io.tmpdir/ehcache"/>

    <defaultCache
            maxElementsInMemory="50000"
            eternal="false"
            timeToIdleSeconds="3600"
            timeToLiveSeconds="3600"
            overflowToDisk="true"
            diskPersistent="false"
            diskExpiryThreadIntervalSeconds="120"
    />

    <cache name="authorizationCache"
           maxEntriesLocalHeap="2000"
           eternal="false"
           timeToIdleSeconds="3600"
           timeToLiveSeconds="3600"
           overflowToDisk="false"
           statistics="true">
    </cache>

    <cache name="authenticationCache"
           maxEntriesLocalHeap="2000"
           eternal="false"
           timeToIdleSeconds="3600"
           timeToLiveSeconds="3600"
           overflowToDisk="false"
           statistics="true">
    </cache>

    <cache name="org.apache.shiro.realm.text.PropertiesRealm-0-accounts"
           maxElementsInMemory="1000"
           eternal="true"
           overflowToDisk="true"/>

</ehcache>
```

`name` - 캐시 이름을 설정한다. 캐시를 식별할때 사용한다.  
`maxElementsInMemory` - 메모리에 저장될 수 있는 객체의 최대 개수  
`eternal` - 저장된 캐시를 제거할지 여부 true 인 경우 저장된 캐시는 제거되지 않으며 `timeToIdleSeconds`, `timeToLiveSeconds` 설정은 무시된다.  
`timeToIdleSeconds` - 생성후 해당 시간 동안 **캐쉬가 사용되지 않으면 삭제된다.** 0은 삭제되지 않는 다.  
`timeToLiveSeconds` - 생성후 해당 **시간이 지나면 캐쉬는 삭제된다.** 0은 삭제되지 않는 다.  
`overflowToDisk` - 메모리에 저장된 객체 개수가 `maxElementsInMemory`에서 지정한 값에 다다를 경우 디스크에 오버플로우 되는 객체는 저장할 지의 여부.  
`diskPersistent` - 재 가동할 때 디스크 저장소에 캐싱된 객체를 저장할지의 여부를 지정한다. 기본값은 false.  
`diskExpiryThreadIntervalSeconds` - 디스크에 저장된 캐시들을 정리하기 위한 작업의 실행 간격 시간을 설정한다. 기본값은 120초.  
`maxEntriesLocalHeap` - 메모리에 생성될 객체의 최대 수를 설정.  
`maxEntriesLocalDisk` - 디스크에 저장될 객체의 최대 수를 설정.  
`diskSpoolBufferSizeMB` - 디스크 크기 설정. OutOfMemory 에러가 발생 시 설정한 크기를 낮추는 것이 좋다.  
`clearOnFlush` - flush() 메서드가 호출되면 캐시 삭제할지 여부를 설정. 기본값은 true.  
`memoryStoreEvictionPolicy` -: maxEntriesLocalHeap 설정 값에 도달했을때 설정된 정책에 따리 객체가 제거되고 새로 추가된다.  
`statistics` - 업데이트 허용 여부.  

위와 같은 설정 파일로 CacheManager를 만들어 캐시를 관리한다.  
shiro를 사용시 CacheManager를 사용하기위해 CacheManager만드는 과정만 알고 진행하도록 한다.  

## shiro spring integration

스프링에서 shiro를 사용하려면 아래 페이지를 따르자.  

> https://shiro.apache.org/spring.html

```xml
<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
    <property name="securityManager" ref="securityManager"/>
    <!-- override these for application-specific URLs if you like:
    <property name="loginUrl" value="/login.jsp"/>
    <property name="successUrl" value="/home.jsp"/>
    <property name="unauthorizedUrl" value="/unauthorized.jsp"/> -->
    <!-- The 'filters' property is not necessary since any declared javax.servlet.Filter bean  -->
    <!-- defined will be automatically acquired and available via its beanName in chain        -->
    <!-- definitions, but you can perform instance overrides or name aliases here if you like: -->
    <!-- <property name="filters">
        <util:map>
            <entry key="anAlias" value-ref="someFilter"/>
        </util:map>
    </property> -->
    <property name="filterChainDefinitions">
        <value>
            # some example chain definitions:
            /admin/** = authc, roles[admin]
            /docs/** = authc, perms[document:read]
            /** = authc
            # more URL-to-FilterChain definitions here
        </value>
    </property>
</bean>

<!-- Define any javax.servlet.Filter beans you want anywhere in this application context.   -->
<!-- They will automatically be acquired by the 'shiroFilter' bean above and made available -->
<!-- to the 'filterChainDefinitions' property.  Or you can manually/explicitly add them     -->
<!-- to the shiroFilter's 'filters' Map if desired. See its JavaDoc for more details.       -->
<bean id="someFilter" class="..."/>
<bean id="anotherFilter" class="..."> ... </bean>
...

<bean id="securityManager" class="org.apache.shiro.web.mgt.DefaultWebSecurityManager">
    <!-- Single realm app.  If you have multiple realms, use the 'realms' property instead. -->
    <property name="realm" ref="myRealm"/>
    <!-- By default the servlet container sessions will be used.  Uncomment this line
         to use shiro's native sessions (see the JavaDoc for more): -->
    <!-- <property name="sessionMode" value="native"/> -->
</bean>
<bean id="lifecycleBeanPostProcessor" class="org.apache.shiro.spring.LifecycleBeanPostProcessor"/>

<!-- Define the Shiro Realm implementation you want to use to connect to your back-end -->
<!-- security datasource: -->
<bean id="myRealm" class="...">
    ...
</bean>
```

위의 xml을 그대로 스프링 부트에서 사용할 수 있도록 `java config`를 통해 빈 객체를 생성할 것이다.  
ShiroConfig라는 설정파일에 하나씩 필요한 자바 빈 객체(`SecurityManager`, `UserRealm`)를 생성해보자.  

```java
public class ShiroConfig {
    @Bean
    public EhCacheManager ehCacheManager() {
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:ehcache.xml");
        return cacheManager;
    }
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }
    ...
    ...
}
```

일단 위 2개의 자바 빈 객체는 별다른 의존관계 설정필요 없이 바로 생성가능하다.  

### Realm 정의

`secuityManager`는 `Realm`을 필요로 하고  
`ShiroFilterFactoryBean`은 `secuityManager`를 필요로 하기에 먼저 `Realm` 자바 빈 객체를 정의해야 한다.   

Realm은 로그인을 위한 정보를 담고있는 DAO같은 객체인데 라이브러리에서 LDAP이나 ,JDBC, INI같은 파일 등 여러가지 데이터소스와 연동할 수 있는 Realm을 제공한다.  

`JdbcRealm`의 내부는 아래와 같다.  
```java
public class JdbcRealm extends AuthorizingRealm {
    protected static final String DEFAULT_AUTHENTICATION_QUERY = "select password from users where username = ?";
    protected static final String DEFAULT_SALTED_AUTHENTICATION_QUERY = "select password, password_salt from users where username = ?";
    protected static final String DEFAULT_USER_ROLES_QUERY = "select role_name from user_roles where username = ?";
    protected static final String DEFAULT_PERMISSIONS_QUERY = "select permission from roles_permissions where role_name = ?";
    private static final Logger log = LoggerFactory.getLogger(JdbcRealm.class);
    protected DataSource dataSource;
    protected String authenticationQuery = "select password from users where username = ?";
    protected String userRolesQuery = "select role_name from user_roles where username = ?";
    protected String permissionsQuery = "select permission from roles_permissions where role_name = ?";
    protected boolean permissionsLookupEnabled = false;
    protected JdbcRealm.SaltStyle saltStyle;
    ...
    ...
}
```
아무래도 `password`를 통해 인증하고 `role_name`과 `permission`을 1:N으로 설정해 인가처리를 하는듯 하다.  

또한 테이블명이나 칼럼명을 밑에 정의되어있는 `get, set` 메서드를 통해 변경 가능하다.  

중요한건 구현하고 있는 `AuthenticatingRealm` 추상 메서드인데 내부적으로 인증 기가처리하는 메서드가 정의되어 있다.  
여기에 있는 메서드를 오버라이딩하는 새로은 Realm객체를 정의 하여 인증, 인가 방식을 커스터마이징 할 수 있다.  

먼저 사용자를 위한 테이블 정의

```java
@Data
@Entity
@Table(name = "tbl_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;
    private String password;
    private String name;

    @OneToOne
    UserRole role;

    @CreationTimestamp
    private Date createTime;
    @UpdateTimestamp
    private Date updateTime;
}
```

대충 JPA로 정의한다.  

인가를 위한 Role역시 정의한다.  
```java
@Data
@Entity
@Table(name = "tbl_role")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer status;
    private String role_name;

    @OneToMany
    @JoinColumn(name = "permission_id")
    private List<UserPermission> permissions;

    @CreationTimestamp
    private Date createTime;
    @UpdateTimestamp
    private Date updateTime;
}
```
`Role`에 해당하는 `Permission`들이 저장될 테이블도 정의.

```java
@Data
@Entity
@Table(name = "tbl_permission")
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roleId;
    private String permission_name;

    @CreationTimestamp
    private Date createTime;
    @UpdateTimestamp
    private Date updateTime;
}
```

```java
public interface UserRepo extends CrudRepository<User, Long> {
    User findByUserName(String username);
}

public interface RoleRepo extends CrudRepository<UserRole, Long> {
}

public interface PermissionRepo extends CrudRepository<UserPermission, Long> {
}
```

그리고 위의 정보를 기준으로 인증, 인가를 위한 `Realm` 정의


```java
@Component
public class UserRealm extends AuthorizingRealm {
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        return null;
    }
}
```

필수로 구현해야할 메서드는 `doGetAuthenticationInfo`이다. 

사용할 토큰을 JwtToken으로 변경한다.
```java
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
```

```java
@Component
@Log
public class UserRealm extends AuthorizingRealm {

    @Autowired
    UserRepo userRepo;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) //인증
            throws AuthenticationException {
        log.info("doGetAuthenticationInfo invoke");
        String token = (String) auth.getCredentials();
        if (token.equals("null")) {
            throw new AuthenticationException("token empty");
        }
        // 해독된 username값을 확인,DB에서 비교를 위해 가져옴
        String userName = JwtUtil.getUserName(token);
        if (userName == null) {
            throw new AuthenticationException("token 이상");
        }
        User user = userRepo.findByUserName(userName);
        if (user == null) {
            throw new AuthenticationException("사용자 존재하지 않음!"); //사용자가 존재하지 않습니다.
        }
        //맨처음 토큰을 만들 때 비밀번호를 같이 섞어 해시화하였기 때문에 비밀번호도 같이 전달
        if (!JwtUtil.verify(token, userName, user.getPassword())) {
            throw new AuthenticationException("Token 비밀번호 다름");
        }

        return new SimpleAuthenticationInfo(token, token, "my_realm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) { //인가
        log.info("doGetAuthorizationInfo invoke");
        String userName = JwtUtil.getUserName(principals.toString());
        User user = userRepo.findByUserName(userName);
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> roleSet = new HashSet<>();
        Set<String> permissionSet = new HashSet<>();
        String roleName = user.getRole().getRole_name();
        log.info("roleName:"+roleName);
        roleSet.add(roleName);
        for (UserPermission permission : user.getRole().getPermissions()) {
            permissionSet.add(permission.getPermission_name());
        }
        authorizationInfo.setRoles(roleSet);
        authorizationInfo.setStringPermissions(permissionSet);
        return authorizationInfo;
    }
}
```

`Realm` 정의가 끝났으면 `ShiroConfig`파일을 마저 정의한다.  

```java
public class ShiroConfig {
    @Bea
    public EhCacheManager ehCacheManager() {
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:ehcache.xml");
        return cacheManager;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public UserRealm userRealm() {
        UserRealm userRealm = new UserRealm();
        return userRealm;
    }

    @Bean
    public SecurityManager securityManager(UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false); //세션 저장 X
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);
        securityManager.setRealm(userRealm);
        securityManager.setCacheManager(ehCacheManager());
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        Map<String, Filter> filtersMap = shiroFilterFactoryBean.getFilters();
        filtersMap.put("jwt", new JwtFilter());
        shiroFilterFactoryBean.setFilters(filtersMap);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

        filterChainDefinitionMap.put("/join", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/**", "jwt");
        
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }
    //인가 처리를 위한 설정, aop와 같은 역할수행
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
```

## Shiro Filter

`setFilterChainDefinitionMap` 메서드를 통해 url에 매칭되는 필터를 적용했다.   
아래 `isLoginAttempt`, `executeLogin`, `executeLogin`, `preHandle` 메서드를 오버라이딩해 로그인처리를 구현한다.  

```java

public class JwtFilter extends BasicHttpAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        logger.info("preHandle invoke");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL());
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        logger.info("isAccessAllowed invoke");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL());
        if (isLoginAttempt(request, response)) {
            try {
                executeLogin(request, response); //로그인 실패시 예외 발생, 로그인 허용하면 모두 통과시키는듯.
            } catch (Exception e) {
                logger.info("exception:"+e.getMessage());
                responseError(request, response, e.getMessage());
            }
        }
        //반환값이 true면 엑세스 허용, false의 경우 접근 거부, 에러페이지 출력
        return true;
        //로그인한 사용자와 하지않은 사용자의 요청 정보는 다르다.
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        logger.info("isLoginAttempt invoke");
        // request가 로그인 요청인지 검사, header값을 사용해 비교한다.
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL());

        String authorization = getAuthzHeader(request);
        Boolean result = authorization != null; //로그인 요청이면 true, 아니라면 false
        return result;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {
        logger.info("executeLogin invoke");

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL());

        String authorization = httpServletRequest.getHeader("Authorization");
        JwtToken token = new JwtToken(authorization);
        getSubject(request, response).login(token); //UserRealm의 doGetAuthentication을 호출할듯
        //realm을 제출하여 로그인을 진행, 만약 틀린 정보라면 AuthenticationException 예외를 반환
        return true;
    }

    private void response401(ServletRequest req, ServletResponse resp) {
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
            httpServletResponse.sendRedirect("/401");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseError(ServletRequest request, ServletResponse response, String message) {
        logger.info("executeLogin invoke");

        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            System.out.println(httpServletRequest.getMethod() + ":" + httpServletRequest.getRequestURL());
            httpServletResponse.setHeader("Access-Control-Allow-Origin", httpServletRequest.getHeader("Origin"));
            httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpServletResponse.setContentType("application/json; charset=utf-8");
            httpServletResponse.setCharacterEncoding("UTF-8");
            PrintWriter out = httpServletResponse.getWriter();
            JSONObject result = new JSONObject();
            if (message.equals("Token인증 실패")) {
                result.put("message", message);
                result.put("returnCode", "0001");
            }else {
                result.put("message", message);
                result.put("returnCode", "0000");
            }
            out.println(result);
            out.flush();
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
```
filter안의 메서드 호출순서

```
2019-09-08 16:01:38.309  INFO 1678 --- [nio-8080-exec-7] com.example.shiro.config.JwtFilter       : preHandle invoke
GET:http://localhost:8080/home/main
2019-09-08 16:01:39.731  INFO 1678 --- [nio-8080-exec-7] com.example.shiro.config.JwtFilter       : isAccessAllowed invoke
GET:http://localhost:8080/home/main
2019-09-08 16:01:39.731  INFO 1678 --- [nio-8080-exec-7] com.example.shiro.config.JwtFilter       : isLoginAttempt invoke
GET:http://localhost:8080/home/main
```