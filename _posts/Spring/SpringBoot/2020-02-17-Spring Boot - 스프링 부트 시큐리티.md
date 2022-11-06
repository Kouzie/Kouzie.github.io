---
title:  "Spring Boot - 스프링 부트 시큐리티!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

# Spring Security

> Spring doc : https://docs.spring.io/spring-security/site/docs/5.2.3.BUILD-SNAPSHOT/reference/htmlsingle/


이전에 `Spring Framework` 에서 시큐리티를 다룬적이 있다.  
> https://kouzie.github.io/spring/Spring-스프링-시큐리티/#  

스프링 부트에선 어떻게 변했는지 확인해보자.  

프로젝트에 `dependency`를 하나 추가하고 `config`용 `java`파일을 하나 설정하자.  
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

컨트롤러를 아무거나 추가해서 실행해보자.  

아래 문구가 출력된다.  

```
Using generated security password: 60e8b37d-147a-4174-9003-3ca02800aada
```

컨트롤러와 뷰페이지 하나 생성후 접근해보자.   
아래의 이미지처럼 로그인 페이지가 생성된다.  

![springboot_security1](/assets/springboot/springboot_security1.png){: .shadow}  

아이디에는 `user`, 비밀번호에는 위에 출력된 `security password` 를 입력해야한다.  

기본적으로 모든 `request` 에 `security filter chain`을 적용한다.  

아래처럼 아무런 필터도 설정 못하도록 `WebSecurityConfigurerAdapter`의 `configure`를 공백으로 처리해 필터를 생략해놓고 후에 하나씩 추가해보자.  


```java
@Log
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) {
        log.info("security config....");
    }
}
```

## 멤버 클래스 설계  

```java
@Getter
@Setter
@Entity
@Table(name = "tbl_members")
public class Member {
    @Id
    private String uid;

    private String upw;
    private String uname;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER)
    List<MemberRole> roles;

    @CreationTimestamp
    private LocalDateTime regdate;
    @UpdateTimestamp
    private LocalDateTime updatedate;
}

@Getter
@Setter 
@Entity
@Table(name = "tbl_member_role")
public class MemberRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fno;

    private String roleName;

    @ManyToOne
    @JoinColumn(name = "member")
    @JsonIgnore
    Member member;
}
```

`Member`는 여러개의 `role`을 가질 수 있는 N:1 관계.  


## 로그인/로그아웃 필터 처리, 테스트 로그인 데이터 생성 

특정 권한을 가진 유저만 `request` 요청을 허용하고 싶을때 `configure` 메서드에 필터를 등록해 처리할 수 있다.  

또한 테스트 멤버용 데이터를 `AuthenticationManagerBuilder`의 `inMemoryAuthentication`로 메모리에 등록 후 테스트 진행이 가능하다.  

```java
@Log
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(auth);
        log.info("SecurityConfig configure...");
        http.authorizeRequests()
                .antMatchers("/boards/list").permitAll()
                .antMatchers("/boards/register").hasAnyRole("BASIC", "MANAGER", "ADMIN");
        http.formLogin();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.inMemoryAuthentication()
                .withUser(User.withUsername("user").password("{noop}user").roles("USER"))
                .withUser(User.withUsername("admin").password("{noop}admin").roles("USER", "ADMIN"));
        /* auth.inMemoryAuthentication()
                .withUser("manager")
                .password("{noop}manager")
                .roles("MANAGER"); */
    }
}
```



> 스프링 5.0 버전 이상부턴 입력된 패스워드를 `PasswordEncoder`를 통해 해시 인코딩 후 비교한다고 한다.  
```
{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
{noop}password
{pbkdf2}5d923b44a6d129f3ddf3e3c8d29412723dcbde72445e8ef6bf3b508fbf17fa4ed4d6b99ca763d8dc
{scrypt}$e0801$8bWJaSu2IKSn9Z9kM+TPXfOc/9bdYSrN1oD9qfVThWEwdRTnO7re7Ei+fUZRJ68k9lTyuTeUp4of4g24hHnazw==$OAOec05+bXxvuu/1qZ6NUR+xQYvYv7BeL1QxwRpY5Pc=
{sha256}97cde38028ad898ebc02e690819fa220e88c62e0699403e94fff291cfffaf8410849f27605abcbc0
```  
패스워드 앞에 `{...}` 문자열을 사용해 어떤 방식으로 인코딩 되었는지 확인 가능하다.  
`{noop}` 을 앞에 붙여 인코딩 과정을 사용하지 않음을 명시한다.  

아이디는 `manger`, 비밀번호는 `1111` 로 설정  

`boards/register` 로 이동해 `AuthenticationManagerBuilder` 으로 생성한 테스트 데이터가 작동하는지 확인하자.  

테스트로 db안의 데이터를 사용하고 싶다면 아래처럼 설정  

```java
// SecurityConfig.java

@Autowired
DataSource datasource;

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  super.configure(auth);
  
  log.info("SecurityConfig configureGlobal...");
  //enable 은 해당 계정 사용가능 여부
  String query1 = "SELECT uid username, CONCAT('{noop}', upw) password, true enabled FROM tbl_members WHERE uid = ?"; 
  String query2 = "SELECT member uid, role_name role FROM tbl_member_role WHERE member = ?";
  auth.jdbcAuthentication()
    .dataSource(datasource)
    .usersByUsernameQuery(query1)
    .authoritiesByUsernameQuery(query2)
    .rolePrefix("ROLE_");
}
```

`spring-security login` 페이지에서 중요한건 필터가 요구하는 데이터의 `alias`이다.  
`user`에 대한 데이터를 조회할땐 `username`, `password`, `enabled`  
`role`에 대한 데이터를 조회할땐 `uid`, `role` 


> `rolePrefix("ROLE_")` : `spring-security` 는 롤 베이스 기반 보안정책을 제공하며 기본적으로 `ROLE_`접두사가 기본적으로 붙이도록 설정함.  
공식문서에선 `RoleVoter`를 커스텀하면 접두사 변경이 가능하다 하는데 커스텀이 쉽지 않다...  
> https://javadeveloperzone.com/spring-boot/spring-security-custom-rolevoter-example/


## 커스텀 로그인, 로그아웃, 접근제한 페이지 

```java
// SecurityConfig.java

@Override
protected void configure(HttpSecurity http) throws Exception {
    super.configure(auth);
    log.info("SecurityConfig configure...");
    http.authorizeRequests()
        .antMatchers("/boards/list").permitAll()
        .antMatchers("/boards/register").hasAnyRole("BASIC", "MANAGER", "ADMIN");

    http.csrf().disable();
    http.formLogin().loginPage("/login");
    //http.formLogin().loginProcessingUrl(""); 로그인 정보 post 로 전달할 url 변경
    //http.formLogin().successForwardUrl(""); 로그인 success 리다이렉트 페이지
    //http.formLogin().failureForwardUrl(""); 로그인 failure 리다이렉트 페이지
    // 어차피 form 을 사용해 post 방식의 /login url에 로그인 처리과정을 적용하기에 사용하지 않는다.

    //http.formLogin().usernameParameter("user_id");
    //http.formLogin().passwordParameter("user_pw"); login 요청시 사용 파라미터 명

    http.exceptionHandling().accessDeniedPage("/accessDenied");
    http.logout().logoutUrl("/logout").invalidateHttpSession(true);
    http.userDetailsService(customUsersService);
}
```

```java
@Controller
public class LoginController {

    @GetMapping("/login")
    public void login() {
      // resource/template 밑에 login.html 생성 요망 
    }

    @GetMapping("/logout")
    public void logout() {

    }

    @GetMapping("/accessDenied")
    public void accessDenied() {

    }
}
```


## userDetailsService

간단한 데이터베이스 조회 후 인증작업을 거치려면 `AuthenticationManagerBuilder`의 `jdbcAuthentication`를 사용하면 되지만  

커스텀 인증과정을 거치려면 `userDetailsService` 를 사용해야 한다.  
`UserDetailsService`를 상속받는 서비스를 정의  

```java
@Log
@Service
public class CustomSecurityUsersService implements UserDetailsService {

    @Autowired
    MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findById(username);
        return new CustomSecurityUser(member);
    }
}
```

어떻게 `Member` 객체를 `spring-security` 에서 요구하는 `UserDetails` 로 반환하는지 알아보자.   

방법은 3가지다.  

1. `Member` 를 `UserDetails`의 구현체로 만드는것.  
2. `Member` 를 `UserDetails`의 구현체인 `User`의 하위객체로 만드는것.  
3. `User` 를 상속하면서 `Member` 를 필드로 갖는 새로운 객체를 정의  

3번 방법을 사용해 `CustomSecurityUser` 라는 개체를 정의하고 반환해보자.  

```java
import org.springframework.security.core.userdetails.User;

@Getter
@Setter
public class CustomSecurityUser extends User {

    private static final String ROLE_PREFIX = "ROLE_";

    private Member member;
    public CustomSecurityUser(Member member) {
        super(member.getUid(), "{noop}" + member.getUpw(), makeGrantedeAuth(member.getRoles()));
        // password 를 사용하지 않을거라면 굳이 넘길 필요 없다.  
        this.member = member;
    }

    private static List<GrantedAuthority> makeGrantedeAuth(List<MemberRole> roles) {
        List<GrantedAuthority> list = new ArrayList<>();
        roles.forEach(memberRole -> 
            list.add(new SimpleGrantedAuthority(ROLE_PREFIX + memberRole.getRoleName())));
        return list;
    }
}
```

정의했으면 `SecurityConfig` 에 해당 `userDetailService` 를 사용해 인증객체를 생성하도록 설정  

```java
// SecurityConfig.java

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    super.configure(auth);
    auth.userDetailsService(customSecurityUsersService);
}
```

### 컨트롤러에서 로그인 정보 접근  

```java
@GetMapping("/list")
@Transactional
public void list(Authentication authentication, @ModelAttribute("pageVO") PageVO vo, Model model) {
  log.info("list() called");
  Pageable page = vo.makePageable(0, "bno");
  Page<Object[]> result = customCrudRepository.getCustomPages(vo.getType(), vo.getKeyword(), page);

  if (authentication != null) {
    CustomSecurityUser customSecurityUser = (CustomSecurityUser) authentication.getPrincipal();
    log.info("meber: " + customSecurityUser.getMember());
  }
  model.addAttribute("result", new PageMaker(result));
}
```

## 로그인 정보 표시

현재 `thymeleaf`를 통해 뷰 페이지를 출력하고 있으며 시큐리티에 대한 태그를 사용하려면 `thymeleaf-extras-springsecurity5` 의존성을 추가해야 한다.  

```xml
<!-- 현재 사용중인 의존객체들 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
  <groupId>nz.net.ultraq.thymeleaf</groupId>
  <artifactId>thymeleaf-layout-dialect</artifactId>
  <version>2.4.1</version>
</dependency>
<dependency>
  <groupId>org.thymeleaf.extras</groupId>
  <artifactId>thymeleaf-extras-springsecurity5</artifactId>
  <version>3.0.4.RELEASE</version>
</dependency>
```

```jsp
<div class="page-header">
  <h1>Boot06 Project <small>for Spring MVC + JPA</small></h1>
  <h3 sec:authentication="name">Spring seucurity username</h3>
  <h3>[[${#authentication.name}]]</h3>
  <h3 sec:authorize="hasRole('ROLE_ADMIN')">This Conetent Only For ADMIN</h3>
  <h3 sec:authorize="hasRole('ROLE_MANAGER')">This Conetent Only For MANAGER</h3>
  <h3 sec:authorize="hasRole('ROLE_BASIC')">This Conetent Only For BASIC</h3>
  <h3 sec:authorize="hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_BASIC')">This Conetent For Everyone</h3>
  <h3><div>[[${#authentication.principal}]]</div></h3>
  <h3><div>[[${#authentication.principal.member.uname}]]</div></h3>
  <h3 th:with="member=${#authentication.principal.member}">
      <div>[[${member.uid}]]</div>
      <div>[[${member.upw}]]</div>
      <div>[[${member.uname}]]</div>
  </h3>
</div>

```

![springboot_security2](/assets/springboot/springboot_security2.png){: .shadow}  


> `Role`(역할) 과 `Authority`(권한): 스프링 프레임워크에선 둘의 기능은 같다. 하지만 다른 프레임워크에선 다르게 쓰이는 용어일 수 있다.  
> `Authority`가 좀더 세세한 의미로 관리자는 모두 `ROLE_ADMIN` 이란 역할(`ROLE`)을 가지지만 각 관리자별로 별도의 `AUTHORITY`(권한)을 할당해 줄 수 있다.  


## 로그인 유지 기능 추가  

로그인 유지를 위해 서버상 `session`에 데이터를 저장해놓고 처리하는 방법이 있고
클라이언트에 유저정보를 담은 쿠키를 생성하고 해당 쿠키를 전달받아 처리하는 방법이 있다.  

우선 로그인 폼에 `remember-me` 파라미터를 추가 

```html
<form method="post">
  <p>
    <label for="username">Username</label> <input type="text" id="username" name="username" value="user88" />
  </p>
  <p>
    <label for="password">Password</label> <input type="password" id="password" name="password" value="pw88" />
  </p>
  <p>
    <label for="remember-me">Remember-Me</label> <input type="checkbox" id="remember-me" name="remember-me" />
  </p>
  <button type="submit" class="btn">Log in</button>
  </form>
```

또한 필터설정에서도 `remember-me`에 대한 정보를 클라이언트에게 반환해야 하기때문에 아래 설정 추가  

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  log.info("SecurityConfig configure...");
  ...
  ...
  http.rememberMe().key("securitykey")
          .userDetailsService(customSecurityUsersService)
          .tokenValiditySeconds(60 * 60 * 24); //24시간
}
```

`securitykey`라는 문자열을 키로 `customSecurityUsersService`객체에서 사용자 데이터를 가져와 암호화(Hash) 하여 사용자에게 반환한다.  
해당 쿠키는 기본 2주 유지되지만 `tokenValiditySeconds` 메서드를 통해 변경 가능하다.  

> `Base64Encode(username:expiryTime:Md5(username:expiryTime:password:key)` 한 값을 반환한다고 한다.  

이렇게 토큰을 쿠키값으로 유지하도록 설정하면 브라우저를 종료하더라도 쿠기는 남아있기 때문에 바로 로그인 처리된 세션생성이 가능하다.  

문제는 비밀번호 변경시 쿠키값도 변경되어야 한다는 거다.  
이를 해결하기 위해 DB에 유저에 대한 토큰값을 저장하고 지속적으로 업데이트가 일어날 수 있도록 설정하자.  

먼저 `spring-security`가 토큰 정보를 관리하는 테이블 생성  

```java
@Getter
@Setter
@Table(name = "persistent_logins")
@Entity
public class PersistentLogin {
    @Id
    private String series;

    private String username;
    private String token;
    private LocalDateTime lastUsed;
}
```

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  log.info("SecurityConfig configure...");
  ...
  ...
  http.rememberMe().key("securitykey")
    .userDetailsService(customSecurityUsersService)
    .tokenRepository(getJDBCRepository())
    .tokenValiditySeconds(60 * 60 * 24); //24시간
}

private PersistentTokenRepository getJDBCRepository() {
  JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
  jdbcTokenRepository.setDataSource(datasource);
  return jdbcTokenRepository;
}
```

## Controller Method 접근 제한  

위의 `WebSecurityConfigurerAdapter`의 필터설정 `http.authorizeRequests().antMatchers("...").hasAnyRole("...")` 을 통해서도 접근제한이 가능하지만  
메서드에 어노테이션을 지정하는 것으로도 접근제한이 가능하다.  

먼저 `WebSecurityConfigurerAdapter` 하위 클래스에 `@EnableGlobalMethodSecurity` 어노테이션 설정,  
다른 클래스에서도 시큐리티 어노테이션을 사용할 수 있도록 설정한다.  

```java
@Log
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  ...
  ...
}
```

간단한 `ROLE_MANAGER` 만 접근 가능한 컨트롤러 메서드 정의  

```java
@Log
@Controller
@RequestMapping("/manager")
public class ManagerController{

    @Secured({"ROLE_MANAGER"})
    @RequestMapping("/page")
    public void getPage() {
        log.info("getPage revoke ");
    }
}
```

## 패스워드 암호화  

`PasswordEncoder` 구현객체를 통해 암호화 가능  
이중 `BCryptPasswordEncoder` 클래스를 통해 단방향 해시 암호화 구현   

```java
@Log
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  ...
  ...
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
      log.info("SecurityConfig configureGlobal...");
      auth.userDetailsService(customSecurityUsersService).passwordEncoder(passwordEncoder());
  }
}
```

또한 `AuthenticationManagerBuilder` 를 통해 `customSecurityUsersService` 에 `PasswordEncoder` 를 등록한다.   
`PasswordEncoder`는 다른 서비스에서도 쓰일 수 있음으로 빈객체로 등록.  

> 위에서 설명한 `{bcrypt}$somehash` 형식으로 저장된다. `BCryptPasswordEncoder` 외에도 `Pbkdf2PasswordEncoder` `SCryptPasswordEncoder` `StandardPasswordEncoder` 가 있으며 이를 모두 지원하는 `DelegatingPasswordEncoder` 도 있다.  
> https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/crypto/password/DelegatingPasswordEncoder.html



### 회원가입

```html
<form method="post">
  <p>
    <label for="uid">UID</label> <input type="text" id="uid" name="uid" value="newbie"/>
  </p>
  <p>
    <label for="upw">UPW</label> <input type="password" id="upw" name="upw" value="newbie"/>
  </p>
  <p>
    <label for="uname">UNAME</label> <input type="text" id="uname" name="uname" value="newbie"/>
  </p>
  <p>
    <input type="checkbox" class="roles[0].roleName" value="BASIC" checked>BASIC
    <input type="checkbox" class="roles[1].roleName" value="MANAGER">MANAGER
    <input type="checkbox" class="roles[2].roleName" value="ADMIN">ADMIN
  </p>
  <button type="submit" class="btn">Log in</button>
</form>
```

```java
@PostMapping("/join")
public String joinPost(@ModelAttribute("member") Member member) {
  log.info("joinPost invoke, member:" + member);
  String encryptPassword = passwordEncoder.encode(member.getUpw());
  member.setUpw(encryptPassword);
  memberRepository.save(member);

  member.getRoles().forEach(
    memberRole -> memberRole.setMember(member)
  );
  memberRoleRepository.saveAll(member.getRoles());

  return "/member/joinResult";
}
```

## 로그인 후 페이지 이동  

로그인이 필요한 `url`로 이동시에 로그인 필터에 걸려 로그인 페이지 이동시한다.  
로그인 완료 후 원래 이동하려는 페이지로 이동하려면 원래 이동하려 했던 `url` 을 알아야 한다.  

> 직접 `/login` url로 `GET Request` 요청후 로그인시에는 루트 디렉토리로 이동된다.  

이동할 페이지 지정을 위해 로그인 페이지로 리다이렉트 될때 원래 가려했던 `url` 을 파라미터로 넘긴다.  
`/login?dest=...` 세션에 저장하고 로그인 성공시 이동하도록 설정해야 한다.  

먼저 `login` 필터에 `successHandler` 메서드를 사용해 로그인 성공후 실행할 코드들이 정의되어 있는 클래스 `LoginSuccessHandler`를 설정한다.  

```java
@Log
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
      log.info("SecurityConfig configure...");
      http.authorizeRequests()
        .antMatchers("/boards/list").permitAll()
        .antMatchers("/boards/register").hasAnyRole("BASIC", "MANAGER", "ADMIN");

      http.csrf().disable()
        .formLogin()
        .loginPage("/login")
        .successHandler(new LoginSuccessHandler());
      ...
      ...
  }
  ...
}
```
`LoginSuccessHandler` 는 `SavedRequestAwareAuthenticationSuccessHandler`의 여러가지 메서드중 로그인 성공시 이동경로를 설정할 `determineTargetUrl` 메서드를 오버라이딩 한다.  

```java
@Log
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Override
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
    Object dest = request.getSession().getAttribute("dest");
    String nextUrl = null;
    if (dest != null) {
      request.getSession().removeAttribute("dest");
      nextUrl = (String) dest;
    } else {
      nextUrl = super.determineTargetUrl(request, response);
    }
    return nextUrl; // 로그인 성공시 session 에서 url 을 꺼내 반환
  }
}
```

코드를 보면 `session` 객체 안의 `dest` 객체를 꺼내 `url` 로 설정한다.  

위의 로직이 가능하려면 먼저 `session` 에 `dest` 값을 삽입하는 과정이 있어야 하는데 인터셉터로 처리한다.  

```java
@Log
public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle...");
        String dest = request.getParameter("dest");
        if (dest != null)
            request.getSession().setAttribute("dest", dest); // 목적지가 있었다면 세션에 저장 
        return super.preHandle(request, response, handler);
    }
}
```

특정 요청이 들어오기 전에 호출되는 인터셉터와 `preHandle` 정의  

해당 인터셉터를 `WebMvcConfigurer` 를 통해 로그인 요청 `url` 에 매핑  

```java
@Log
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor()).addPathPatterns("/login");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
```

앞으로 비로그인 유저가 로그인이 필요한 url 에 접근히 

# Spring Security Rest API

스프링 부트가 `Rest API` 위주의 서비스 지원 서버가 되면서 더이상 로그인 폼 과 같은 UI를 지원하지 않는다.  

`Rest API` 에서 스프링 시큐리티를 사용해 인증, 인가하는 방법을 알아보자.  

## JWT (JSON Web Token)

Rest API 를 사용하는 클라이언트가 `Session`/`Cookie` 시스템을 갖추고 있을지는 확실하지 않다.  

때문에 로그인 성공시 항상 `jwt` 토큰을 발급하고 헤더에 포함시켜 인증처리하도록 설정한다.  

웹상에서 전달되는 인증객체, 개인만이 아는 데이터(비밀번호 등)의 해쉬화된 값이 서명역할을 할 수 있다.  

웹으로 전달하는 데이터가 정말 본인이 작성한게 맞는지 확인할 때 이 라이브러리를 사용하면 간단하게 해결할 수 있다.  

> https://jwt.io/

### JWT 구조

![jwt1](/assets/2019/jwt1.png){: .shadow}  

그림처럼 3부분으로 나뉜다.  

`Header`, `Payload(Claim Set)`, `Signature`

`Header`와 `Payload`는 단순 `Base64`로 인코딩된 문자열로 아래같은 사이트에서 디코딩하면 확인할 수 있다.  
> https://www.base64decode.org/  

![jwt2](/assets/2019/jwt2.png){: .shadow}  

최종적인 결과 : `Header + "." + Payload + "." + Signature`

> `Signature` 는 `Header + Payload` 문자열을 `seceret` 키로 해시한 값이다. 인증을 위한 데이터로 sha, rsa 등 여러 알고리즘으로 인증처리 가능하다.  

`Header`와 `Payload`는 암호화 되지 않기때문에 위변조가 가능하며 중요한 데이터를 처리하거나 넣는 일이 발생하면 안된다.  
딱 해당 유저가 보낸 토큰이구나 확인할수 정도로만 운영한다.  

## 스프링 시큐리티 + JWT

> https://www.javainuse.com/spring/boot-jwt

스프링 시큐리티의 인증구조를 커스터마이징 해야 하기에 변경해야할 코드가 많다.  

전체적은 구조는 아래 사진과 같다.  

![springboot_security3](/assets/springboot/springboot_security3.png){: .shadow}  

1. `/authenticate` url 로 `username`, `password` 정보와 함께 `jwt` 토큰 요청  
2. `username`, `password` 검사후 `jwt` 토큰 생성 및 전달  
3. `/hello` url 로 `jwt` 토큰을 header 에 담아 리소스 요청  
4. `jwt` 토큰 확인 후 `/hello` 에 대한 리소스 반환

`jwt` 토큰 생성을 위해 아래 `dependency` 포함

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

## 스프링 시큐리티 JWT Generating 

로그인시 jwt 토큰을 생성 및 반환하는 구조는 아래 사진과 같다.   

![springboot_security4](/assets/springboot/springboot_security4.png){: .shadow}   

1. `/authenticate` url 로 `username`, `password` 정보와 함께 `jwt` 토큰 요청  
2. 이미 토큰 데이터를 가지고 있지 않은지 확인 
3. 없다면 `generateAuthenticateToken()` 메서드 호출 
4. `username`, `password` 를 `spring-security` `authenticate()` 를 사용해 검증시도
5. 검증을 위해 `username` 을 DB 에서 검색, `Userdetails` 를 요청  
6. `Userdetails` 를 반환, 내부에는 `username`, `password` 데이터가 포함되어있음.  
7. 입력한 `username`, `password` 가 DB에서 반환된 값과 다를경우 에러발생  
8. `Userdetails`를 사용해 `generateToken` 메서드 호출  
9. 토큰값 반환  


먼저 JWT 토큰을 마음대로 주무를수 있는 `JwtTokenUtil` 정의  

```java
@Component
public class JwtTokenUtil implements Serializable {

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; //5분

    @Value("${jwt.secret}") //암호화에 사용할 키값
    private String secret;

    //jwt 토큰으로 부터 username get
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //jwt 토큰으로 부터 expirationDate get
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Claims expiration date 혹은 username 를 가져오기 위해 호출
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        // 모든 값은 verify signature 부분에서 가져온다.
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 토큰의 각종 데이터를 되찾아 오기 위해 시크릿 키가 필요
    private Claims getAllClaimsFromToken(String token) {
        return Jwts
                .parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 시간 초과 확인
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    //user를 위한 토큰 생성
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    //1. 발급자, 만료, 주제 및 ID와 같은 토큰의 클레임을 정의하십시오.
    //2. HS512 알고리즘과 비밀 키를 사용하여 JWT에 서명하십시오.
    //3. JWS Compact Serialization에 따르면 (https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // URL-safe string 로 JWT 압축한다.
    private String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder()
                .setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000)) //1
                .signWith(SignatureAlgorithm.HS512, secret) //2
                .compact(); //3
    }

    //validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

### JWTUserDetailsService

토큰으로부터 사용자 정보를 받아 db에서 조회  

```java
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findById(username);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        return new CustomSecurityUser(member);
    }
}
```

### JwtRequestFilter

토큰으로부터 유저 아이디를 확인하고 해당 토큰이 로그인시에 암호화해서 발급했던 토큰이 맞는지 확인  

```java
//OncePerRequestFilter 하나의 request 당 필터가 한번만 실행되는 것을 보장
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // headaer 로부터 토큰 흭득
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get JWT Token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.error("JWT Token has expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token has expired");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token does not begin with Bearer String");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // DB 에서 토큰정보로 검색 
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);

            // 토큰 유효시간도 많고 Spring Security 부터 가져온 userDetails 과도 인증정보가 일치하면
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                // 인증 정보가 일치함으로 context 에 인증정보를 저장하고 통과, filter 외부의 컨트롤러에서도 인증정보를 참조하기에 저장해두어야 한다.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}
```

### SecurityConfig  

```java
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 다른 클래스에서도 Security 관련 어노테이션사용 가능  
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Autowired
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

 /**
  * @Component 로 등록된 JwtRequestFilter 가 일반 서블릿 필터로 중복 등록되는것을 방지
  */
  @Bean
  public FilterRegistrationBean registration(JwtRequestFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    super.configure(auth);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.httpBasic().and().csrf().disable();
    http
        // jwt token으로 인증할것이므로 세션필요없으므로 생성안함.
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
      .and()
        .authorizeRequests() //url 별 권한관리 시작점
        .antMatchers("/customer/api/v1/**").hasAnyRole(Role.CUSTOMER.name(), Role.ADMIN.name())
        .antMatchers("/merchant/api/v1/**").hasAnyRole(Role.MERCHANT.name(), Role.ADMIN.name())
        .antMatchers("/admin/api/v1/**").hasRole(Role.ADMIN.name())
        .anyRequest().authenticated() // 위의 url 외에는 모두 인증된 사용자는 가능
      .and()
        .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint) // 로그인 실패시 처리 객체
      .and()
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
  }

  /**
  * anyRequest().authenticated() 로 인해 404 코드 대신 401 코드가 반환되는 건에 대해
  * 실제 404, 500 등의 에외가 발생하면 default servlet filter 에 의해 /error url 로 redirect된다, 
  * 따라서 아래 ignoring 에 /error 도 포함시켜 주어야 정상적으로 404가 반환된다.
  */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers(
      "/static/**"
      , "/docs/**"
      , "/error"
      , "/member/**"
      , "/oauth2/**"
      , "/css/**"
      , "/images/**"
      , "/js/**"
      , "/h2-console/**"
      , "/profile"
    );
}
```

### RestController

```java
@RestController
@CrossOrigin
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		final UserDetails userDetails = userDetailsService.loadUserByUsername(
      authenticationRequest.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new JwtResponse(token));
	}

	private void authenticate(String username, String password) throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}
}
```

단점은 매 요청마다 `jwtFilter` 에서 DB 요청이 일어난다는 것.  
`JwtUserDetailsService` 에서 인증 객체를 저장하기 위해 DB에서 토큰에 저장된 username 을 검색한다.  

사실 username 이 토큰으로부터 나왔다면 이미 인증된 것이나 다름없기에 해당 과정을 생략하고 바로 인증객체를 토큰으로부터 뽑아 `SecurityContext` 에 저장해도 된다.  

아래 메서드를 사용해서 인증객체를 생성

```java
public Authentication getAuthentication(String token) {
Map<String, Object> parseInfo = jwtTokenUtil.getUserParseInfo(token);
Long memberId = (Long) parseInfo.get("id");
String username = (String) parseInfo.get("username");
String role = (String) parseInfo.get("role");
Collection<GrantedAuthority> authorities = new ArrayList<>();
authorities.add(new SimpleGrantedAuthority(role));

JwtUserDetails jwtUserDetail = new JwtUserDetails(
  memberId,
  username,
  DEFAULT_PASSWORD, //패스워드 없이도 인증객체는 생성 가능  
  Role.getValue(role),
  authorities);

UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
  new UsernamePasswordAuthenticationToken(jwtUserDetail, null, jwtUserDetail.getAuthorities());
return usernamePasswordAuthenticationToken;
```

토큰에 필요한 정보 `id`, `username`, `role` 을 이미 포함시켜 두었기 때문에 더이상의 DB 요청은 생략할 수 있다.  
만약 이 외에도 더 필요한 정보가 있다면 로그인시 발급하는 토큰에 미리 지정해두면 된다.  

## OAuth2

스프링 시큐리티에서 `OAuth`를 지원하는 프레임워크가 잘 되어있지만 스프링 부트에서 템플릿까지 제공할 경우이다.  

`Rest API` 서버이면서 `OAuth2` 인증을 지원하려면 별도로 지원하는게 마음 편하다.  

### OAuth2 개요  

> https://opentutorials.org/course/3405

1. `OAuth` 를 사용하기 위해 서비스 등록, 등록시 발급하는 Client ID, Client Secret 저장 필수  
2. 구글에서 제공하는 인증 URL 파라미터에 **Client ID** 와 **리다이렉트될 URL** 을 입력, 인증 성공시 해당 URL 로 클라이언트가 로그인 성공했다는 `code` 값을 반환한다.
3. `code`값을 사용해 구글에 클라이언트 정보에 접근할 수 있는 `accessToken` 을 발급받는다. 이 모든 과정은 클라이언트가 이미 나의 `Client ID`를 사용해서 로그인에 성공했기에 가능한 일이다.   
4. `accessToken`을 발급받으면 구글에서 계정정보 (email, 사진, 이름) 등을 요청해 DB 에 저장후 로그인 처리한다(JWT 토큰 발급)  


### 용어 설명

* `Resoucrce` - 로그인 자원  
* `Resoucrce Owner` - 로그인 자원을 가지는 개인 휴대폰, PC 등  
* `Resoucrce Client` - 로그인 자원을 이용하는 어플리케이션 (우리가 만드는 스프링 서버)  
* `Resoucrce Server` - 로그인 자원을 관리하는 서버 (구글, 트위터, 페이스북)  


### 예제 코드  

```java
@Slf4j
@RestController
@RequestMapping("/oauth2/code")
@RequiredArgsConstructor
public class OAuth2Controller {

  private final static String DEFAULT_PASSWORD = "default_password";
  private final OAuthGoogleClient oAuthGoogleClient;
  private final JwtTokenUtil jwtTokenUtil;
  private final MemberService memberService;
  @Value("${oauth2.client.registration.google.client-id")
  private String googleClientId;
  @Value("${oauth2.client.registration.google.client-secret")
  private String googleClientSecret;
  @Value("${oauth2.client.registration.google.scope")
  private String googleScope;
  @Value("${oauth2.client.registration.google.redirect-uri")
  private String googleRedirectUri;

  /**
    * google oauth2 redirect
    */
  @GetMapping("/google")
  public ResponseEntity<ResponseDto> googleRedirect(HttpServletRequest request) {
    log.info("googleRedirect invoked");
    Map<String, String[]> map = request.getParameterMap();
    for (Map.Entry<String, String[]> stringEntry : map.entrySet()) {
      String key = stringEntry.getKey();
      String values = Arrays.toString(stringEntry.getValue());
      log.info("key:" + key + ", values:" + values);
    }
    // 2.인증 성공시 해당 URL 로 클라이언트가 로그인 성공했다는 code 값을 반환한다.
    String code = request.getParameter("code");
    OAuthAccessToken token;
    OAuthAttributes oAuthAttributes;
    // 3. code값을 사용해 구글에 클라이언트 정보에 접근할 수 있는 accessToken 을 발급받는다. 
    token = oAuthGoogleClient.getUserAccessToken(code);
    // 4. accessToken을 발급받으면 구글에서 계정정보 (email, 사진, 이름) 등을 요청해 DB 에 저장후 로그인 처리한다(JWT 토큰 발급) 
    oAuthAttributes = oAuthGoogleClient.getUserAttributes(token);
    Member member = null;
    if (memberService.iskMemberEmailExist(oAuthAttributes.getEmail())) {
      // 이미 이메일 정보가 있을경우 DB 에서 find
      member = memberService.findMemberByEmail(oAuthAttributes.getEmail());
    } else {
      // 기존 이메일이 없다면 새로운 계정 생성
      member = oAuthAttributes.toEntity();
      member = memberService.saveMember(member);
    }

    JwtUserDetails userDetails = new JwtUserDetails(
      member.getId(),
      member.getEmail(),
      DEFAULT_PASSWORD,
      "ROLE_MEMBER",
      authorities);

    JwtResponseDto jwtResponse = new JwtResponseDto(jwtTokenUtil.generateToken(userDetails), member.getRole());
    return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
  }
}
```

### acessToken, OAuthAttribute

위에 3, 4번 과정인 `accessToken` 을 사용해 사용자 정보 `OAuthAttribute` 를 가져오는 코드  

```java
public OAuthAccessToken getUserAccessToken(String authenticate_code) {
  UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_REQUEST_TOKEN_URL)
    .queryParam("code", authenticate_code)
    .queryParam("client_id", googleClientId)
    .queryParam("client_secret", googleClientSecret)
    .queryParam("redirect_uri", googleRedirectUri)
    .queryParam("grant_type", "authorization_code");

  HttpHeaders headers = new HttpHeaders();
  headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
  HttpEntity<?> entity = new HttpEntity<>(headers);

  HttpEntity<GoogleAccessToken> responseEntity = restTemplate.exchange(
    builder.toUriString(),
    HttpMethod.POST,
    entity,
    GoogleAccessToken.class);
  GoogleAccessToken token = responseEntity.getBody();
  System.out.println(token.toString());
  return token;
}

@Override
public OAuthAttributes getUserAttributes(OAuthAccessToken accessToken) {
  UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GOOGLE_REQUEST_USER_INFO_URL)
    .queryParam("access_token", accessToken.getAccessToken());
  System.out.println(builder.toUriString());
  HttpHeaders headers = new HttpHeaders();
  headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
  HttpEntity<?> entity = new HttpEntity<>(headers);

  HttpEntity<JSONObject> responseEntity = restTemplate.exchange(
    builder.toUriString(),
    HttpMethod.GET,
    entity,
    JSONObject.class);

  JSONObject attributesMap = responseEntity.getBody();
  OAuthAttributes oAuthAttributes = OAuthAttributes.of("google", attributesMap);
  return oAuthAttributes;
}
```

## Spring security cors 에러 이슈

`spring-security`를 사용하지 않는경우 `CorsRegistry` 등록하여 `cors` 에러 이슈 처리방식을 사용해왔다.  


```java
@Configuration
@RequiredArgsConstructor
public class WebAdminConfig implements WebMvcConfigurer, Filter {

    // WebMvcConfigurer 에서 cors 에러
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```


`spring-security` 를 사용한다면 `configure` 에 해당 필터를 등록해준다.  

```java
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type,XFILENAME,XFILECATEGORY,XFILESIZE,X-Token");
        chain.doFilter(request, resp);
    }
}
```


```java
.addFilterBefore(new CorsFilter(), ChannelProcessingFilter.class)
```

최신 `spring-security` 사용시에 `CorsConfiguration` 를 사용한 `cors` 이슈 처리  

> https://stackoverflow.com/questions/36809528/spring-boot-cors-filter-cors-preflight-channel-did-not-succeed  

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    ...
    ...

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                ...
                .cors().configurationSource(corsConfigurationSource())
                ...
        ;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(false);
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "TOKEN_ID", "X-Requested-With", "Authorization", "Content-Type", "Content-Length", "Cache-Control"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

> 주의 : `WebSecurity` 의 `web.ignoring()` 사용시에 `spring-security filter` 에서 아예 제외됨으로 `CORS` 설정을 사용하지 않는다.  
> `HttpSecurity` 와 `permitAll()` 을 통해 진행하는 것을 권장 
> CORS는 응답이 Access-Control-Allow-Credentials: true 을 가질 경우, Access-Controll-Allow-Origin의 값으로 *를 사용하지 못하게 막고 있다.

# SSL 적용  

```
# 비밀번호 123456 사용, 그외의 값은 아무값이나 적용  
$ keytool -genkey -alias spring -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 4000
...
Is CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown correct?
  [no]:  yes
```

`keystore.p12` 생성이 확인되었으면 아래처럼 설정  

```conf
#ssl
server.ssl.key-store=keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=123456
server.ssl.key-alias=spring
```

> https://127.0.0.1:8080/ 

위와 같이 `https` 와 `ip`, `port` 를 사용해 접속 할 수 있다.  

크롬기반 브라우저는 정책상 유효하지 않은 인증서는 사용할 수 없어 파이어폭스같은 브라우저로 테스트하길 권장