---
title:  "Spring Boot - Security!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Spring Security

> Spring doc : <https://spring.io/projects/spring-security>  
> <https://spring.io/guides/topicals/spring-security-architecture/>

![security6](/assets/springboot/springboot_security6.png)

- **AuthenticationFilter(인증 필터)**  
  Spring Security 백본, 전반적인 HTTP 요청을 처리, 하위 Security 객체들과 협력하여 인증처리를 진행한다.  
  아래 SecurityFilterChain 그림에서 Filter Chain 확인  
- **AuthenticationManager(인증 매니저)**  
  사용자 신원 확인하는 핵심 구성요소.  
- **AuthenticationProvider(인증 제공자)**  
  AuthenticationManager 의 요청을 수행하는 클래스들, `[DB, LDAP, JWT]` 등 여러 `AuthenticationProvider` 정의가 가능하다.  
- **UserDetailsService**
  인증자의 세부정보를 검색하는 인터페이스, AuthenticationProvider 와 협력하여 인증자의 세부정보 DB 등으로부터 가져옴.  
- **UserDetails, User**
  인증자의 신원객체


```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

`dependency` 만 추가하고 컨트롤러를 아무거나 추가해서 실행하면 아래와 같은 password 메세지가 출력된다.  

> default 아이디 `user`, 비밀번호는 아래 `security password`  

`Using generated security password: 60e8b37d-147a-4174-9003-3ca02800aada`

생성한 컨트롤러에 접근하면 아래의 이미지처럼 `/login` `url` 로 `redirect` 되고  
로그인하면 `security session` 을 위한 쿠키가 설정된다.  

![springboot_security1](/assets/springboot/springboot_security1.png)

## 기본 사용법

`Spring Security` 에서 보편적으로 사용하는 설정에 대해 학습

먼저 간단히 사용할 사용자 클래스 정의

```java
@Table(name = "tbl_members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String uname;
    private String upw;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "uid")
    List<MemberRole> roles;

    @CreationTimestamp
    private LocalDateTime regdate;
    @UpdateTimestamp
    private LocalDateTime updatedate;
}

@Table(name = "tbl_member_role")
public class MemberRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fno;

    private String roleName;
}
```

### WebSecurityConfigurerAdapter

`Spring Security` 사용을 알리는 java config 객체  

```java
@EnableWebSecurity
public class SessionSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
    }
}
```

정의와 동시에 그림과 같은 수많은 `SecurityFilterChain` 이 요청-응답사이에 들어간다.  

![security7](/assets/springboot/springboot_security7.png)

아무것도 설정하지 않고 `spring-boot-starter-security` 의존성만 넣었을 때 적용되는 `HttpSecurity` 의 설정은 아래와 같다.  

```java
protected void configure(HttpSecurity http) throws Exception {
    this.logger.debug("Using default configure(HttpSecurity). "
            + "If subclassed this will potentially override subclass configure(HttpSecurity).");
    http.authorizeRequests((requests) -> requests.anyRequest().authenticated());
    http.formLogin();
    http.httpBasic();
}
```

기본적으로 모든 `request` 에 `security filter chain` 이 적용되고  
세션기반의 로그인  `formLogin`, `httpBasic` 방식이 들어가는 `security` 필터가 설정된다.  

### PasswordEncoder

`PasswordEncoder`를 통해 해시 인코딩 후 비교  

```java
@Bean
public PasswordEncoder passwordEncoder () {
    return new BCryptPasswordEncoder();
}
```

`Spring Security` 의 필수 `Bean` 임으로 반드시 생성해야함  
`BCryptPasswordEncoder` 가 가장 무난하게 사용 가능  

### AuthenticationManager

`AuthenticationManager` 는 사용자 인증을 담당하는 클래스로 `Spring Security Filter` 에서 반드시 거쳐야할 클래스  

단순 테스트용도로 `inMemoryAuthentication` 으로 사용자를 정의해서 로그인시 사용할 수 있다.  

간단히 DB 연동을 통해 로그인처리를 해야한다면 `jdbcAuthentication` 으로 사용자를 검색해서 사용할 수 있다.  


아래는 `inMemoryAuthentication`, `jdbcAuthentication` 을 사용해 `AuthenticationManager` 를 생성하는 예

```java
@EnableWebSecurity
public class SessionSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(User.withUsername("user").password(passwordEncoder.encode("user")).roles("BASIC"))
                .withUser(User.withUsername("admin").password(passwordEncoder.encode("admin")).roles("BASIC", "ADMIN"));
    }
}
```

```java
@EnableWebSecurity
public class SessionSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataSource datasource;

    @Autowired
    private MemberRepository memberRepository;

    @PostConstruct
    private void init() {
        if (memberRepository.findByUname("basic").isEmpty()) {
            memberRepository.save(new Member("basic", passwordEncoder.encode("basic"), "BASIC"));
        }
        if (memberRepository.findByUname("manager").isEmpty()) {
            memberRepository.save(new Member("manager", passwordEncoder.encode("manager"), "MANAGER"));
        }
        if (memberRepository.findByUname("admin").isEmpty()) {
            memberRepository.save(new Member("admin", passwordEncoder.encode("admin"), "ADMIN"));
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //enable 은 해당 계정 사용가능 여부
        String query1 = "SELECT uid username, upw password, true enabled FROM tbl_members WHERE uname = ?";
        String query2 = "SELECT uid, role_name role FROM tbl_member_role WHERE uid = ?";
        auth.jdbcAuthentication()
                .dataSource(datasource)
                .usersByUsernameQuery(query1)
                .authoritiesByUsernameQuery(query2)
                .rolePrefix("ROLE_");
    }
}
```

> 검색된 쿼리 결과의 칼럼 순서, alias 명 모두 중요하니 주의

### AuthenticationProvider, UserDetailsService

`AuthenticationManagr` 를 직접 정의하는 방식을 제한적인 환경(제공받은 메서드로만 구성해야함)으로 인해 잘 사용하지 않는다.  
위와 같이 테스트용도로 inMemory, jdbc SQL 를 직접 정의하여 사용할 때에나 사용한다.  

대부분 `AuthenticationProvider` 만을 Bean 으로 등록하고 `AuthenticationManager` 에서 자동으로 사용되도록 한다.  

사용자 테이블로 부터 커스텀하게 로그인처리를 구현하는 경우가 많아 `UserDetailService` 를 사용해서 `AuthenticationProvider` 를 구성한다.  

![security7](/assets/springboot/springboot_security5.png)

> `DaoAuthenticationProvider` 가 `UserDetailService` 를 기반으로 만들어진 객체임.   

```java
@Service
@RequiredArgsConstructor
public class CustomSecurityUsersService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    private void init() {
        if (memberRepository.findByUname("basic").isEmpty()) {
            memberRepository.save(new Member("basic", passwordEncoder.encode("basic"), "BASIC"));
        }
        if (memberRepository.findByUname("manager").isEmpty()) {
            memberRepository.save(new Member("manager", passwordEncoder.encode("manager"), "MANAGER"));
        }
        if (memberRepository.findByUname("admin").isEmpty()) {
            memberRepository.save(new Member("admin", passwordEncoder.encode("admin"), "ADMIN"));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUname(username)
                .orElseThrow(() -> new IllegalArgumentException());
        return new CustomSecurityUser(member);
    }
}

@Getter
@Setter
public class CustomSecurityUser extends User {

    private static final String ROLE_PREFIX = "ROLE_";

    private Member member;

    public CustomSecurityUser(Member member) {
        super(member.getUname(), member.getUpw(), makeGrantedAuth(member.getRoles()));
        this.member = member;
    }

    private static List<GrantedAuthority> makeGrantedAuth(List<MemberRole> roles) {
        List<GrantedAuthority> list = new ArrayList<>();
        roles.forEach(memberRole ->
                list.add(new SimpleGrantedAuthority(ROLE_PREFIX + memberRole.getRoleName())));
        return list;
    }
}
```

`UserDetailsService`, `User` 객체가 `Spring Security` 의 핵심 클래스  

> `User` 클래스는 `UserDetails` 의 구현체.

`AuthenticationManager` 가 알아서 `Bean` 으로 등록된 `UserDetailService` 을 사용함으로 별다른 설정을 하지 않아도 된다.  

직접 설정하려면 아래처럼 `SecurityConfig` 에 해당 `userDetailService` 를 사용해 인증객체를 생성하도록 설정  

```java
@Autowired
private UserDetailsService userDetailsService;

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService);
}
```

### Authentication

Spring Security 는 `[inMemory, jdbc, DAO, LDAP]` 등 다양한 인증서비스를 제공한다.  

이에 맞는 `AuthenticationProvider` 를 정의해야 하며, 이러한 다양한 인증과정에서  `Authentication` 구현객체를 표준으로 사용한다.  

![security7](/assets/springboot/springboot_security2.png)

- **Credential**: 자격, 인증서  
- **Pincipal**: 주체  
- **Authorities**: Role & Authority, 권한

```java
public interface Authentication extends Principal, Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();
    Object getCredentials();
    Object getPrincipal();
    ...
}
```

`Credential, Pincipal` 모두 `Object` 이기 때문에 로직에 맞는 보안객체를 할당하면 된다.  

위에서 사용한 `DaoAuthenticationProvider` 가 `Authentication` 을 구현한 `UsernamePasswordAuthenticationToken` 을 지원하기 때문에 해당 객체를 `Authentication` 객체로 자주 사용한다.  

`Crendential` 에 `username`, `Principal` 에 `password` 를 자주 설정한다.  

```java
// 미인증 Authentication 객체
UsernamePasswordAuthenticationToken authRequest = 
    new UsernamePasswordAuthenticationToken(username, password);

/* 
public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
}

public UsernamePasswordAuthenticationToken(Object principal, Object credentials,
        Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true); // must use super, as we override
}
*/
```

`Spring Security` 내부 코드에서 `Role` 과 `Authority` 를 처리하는 방법은 동일하다.  

둘다 권한을 뜻하는 개념이고 `SimpleGrantedAuthority` 클래스를 사용한다.  
그리고 `AbstractAuthenticationToken` 객체에 권한(`Role`, `Authority`) 들이 들어간다.  

`Spring Security` 설정에 따라 `authorities` 내부를 검사하는데  

`hasAnyRole('ADMIN')` 과 같은 코드가 있다면 `ROLE_ADMIN` 과 같은 문자열이 있는지 탐색,  
`hasAuthority('getBoard')` 과 같은 코드가 있다면 `getBoard` 문자열이 있는지 탐색한다.  

실제 비밀번호를 가진 `UsernamePasswordAuthenticationToken` 객체의 인증은 `UsernamePasswordAuthenticationFilter` 가 `DaoAuthenticationProvider` 의 인증 메서드를 호출하면서 수행된다.  

`DaoAuthenticationProvider` 는 `UserDetailService` 로부터 `UsernamePasswordAuthenticationToken` 의 `username` 을 사용해 사용자를 검색하고 `password` 를 비교해서 인증을 수행한다.  

### Spring Security configure

`Spring Security` 는 `DSL` 형식의 언어로 구성이 가능하며  
자주 사용하는 설정은 아래와 같다.  

```java
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class SessionSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .csrf().disable()
            .antMatchers("/boards/list").permitAll() // 모든 사용자 허용
            .antMatchers("/boards/register").hasAnyRole("MANAGER", "ADMIN"); // Role 가진 사용자 허용
            .anyRequest().authenticated() // 모든 요청은 인증받은 사용자만 허용
            .exceptionHandling() // 예외처리 시작
            .authenticationEntryPoint(authenticationEntryPoint()) // 인증 예외
            .accessDeniedHandler(accessDeniedHandler()) // 인가 예외

    }

    private AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                log.error("url {} access denied, msg:{}", request.getRequestURL(), accessDeniedException.getMessage());
            }
        };
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                log.error("url {} authentication denied, msg:{}", request.getRequestURL(), authException.getMessage());
            }
        };
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

`WebSecurity ignoring` 과 `HttpSecurity permitAll` 의 차이는 `SecurityFilterChain` 을 거치는지 아닌지 차이  
인증, 인가 모두 필요없는 리소스의 경우 `WebSecurity ignoring` 사용이 성능상 유리하다.  
인증은 필요하지만 인가는 필요없는 경우 `HttpSecurity permitAll` 를 사용하면 된다.  

위 설정처럼 `antMatchers("...").hasAnyRole("...")` 접근제한이 가능하지만  
메서드에 어노테이션을 지정하는 것으로도 접근제한이 가능하다.  

`@EnableGlobalMethodSecurity` 어노테이션 설정, 다른 클래스에서도 시큐리티 어노테이션을 사용할 수 있도록 설정한다.  

## 세션 기반 스프링 시큐리티

`formLogin` 과 세션기반의 `Spring Security` 설명  

### 로그인폼 설정

별도의 설정을 하지 않을경우 `formLogin`, `httpBasic` 가 설정된 기본 `Spring Security Config` 를 사용한다.  

`/login` 을 호출하면 `AuthenticationManager` 에 따라 로그인 절차가 이루어지고 세션에 로그인정보가 남게된다.  
`/logout` 을 호출하면 세션을 초기화하는 과정을 진행한다.  

```java

@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/boards/random").hasAnyRole("BASIC", "MANAGER")
        .antMatchers("/boards/list").permitAll()
        .anyRequest().authenticated()
        .and()
        .formLogin() // login config
        .usernameParameter("username_demo") // default: username
        .passwordParameter("password_demo") // default: password
        .loginPage("/auth/login_demo") // default: /login[GET]
        .loginProcessingUrl("/auth/login_demo_process") // default: /login[POST]
        .successForwardUrl("/auth/login_success") // login success redirect url
        .failureUrl("/auth/login_demo?error=true") // login failed redirect url
        .and()
        .logout() // logout config
        .logoutUrl("/auth/logout_demo") // default: /logout[GET, POST]
        .logoutSuccessUrl("/boards/list") // logout success redirect url
        .invalidateHttpSession(true) // logout 후 세션삭제여부, default: true
        .and()
        .exceptionHandling() // exception config
        .accessDeniedPage("/auth/access_denied") // access denied redirect url
    ;
}
```

### rememberMe

서버 `session` 에 로그인 데이터를 저장해놓고 로그인을 유지하는 방법,  
쿠키에 **로그인토큰**을 저장해 로그인을 유지하는 방법이 있다.  

아래와 같이 `rememberMe config` 를 설정하면 `RememberMeAuthenticationFilter` 가 추가 `Security Filter Chain` 에 추가된다.  

```java
http.rememberMe()
    .rememberMeParameter("remember-me") // default: remember-me
    .key("spring-demo-security-key") // secret key
    .tokenValiditySeconds(60 * 60 * 24) // 24 hour, default  2week
    .alwaysRemember(false) // default: false
    .userDetailsService(userDetailsService) // use TokenBasedRememberMeServices
;
```

`TokenBasedRememberMeServices` 객체가 `userDetailsService` 를 사용하 사용자정보를 가져와 로그인토큰을 만들수 있도록 설정한다.  

로그인 폼에 `remember-me` 파라미터를 추가  

```html
<form method="post">
  <p> <label for="username">Username</label> <input type="text" id="username" name="username" value="user88" /> </p>
  <p> <label for="password">Password</label> <input type="password" id="password" name="password" value="pw88" /> </p>
  <p> <label for="remember-me">Remember-Me</label> <input type="checkbox" id="remember-me" name="remember-me" /> </p>
  <button type="submit" class="btn">Log in</button>
  </form>
```

로그인후 쿠키에서 `remember-me` 를 확인  

```
YmFzaWM6MTY4MDIzMDczNDgwMDo1OWMyYjI0NjU0ZGUzYzQ3OWZjMjFjMzQ3OTdkN2UwNg
username:expiryTime:Md5(username:expiryTime:password:key)
```

브라우저를 종료하더라도 로그인토큰이 쿠키값으로 유지되기에 로그인이 유지되며  
서버 세션이 없어저도 `TokenBasedRememberMeServices` 가 전달받은 로그인토큰 기반으로 재 로그인처리를 진행한다.  

서버가 재실행되거나 동시에 여러대의 서버가 실행되어도 로그인이 풀리지 않는다.  

영구적으로 토큰관리하는 방법도 있다, `PersistentTokenBasedRememberMeServices` 를 사용하면 별도의 토큰용 DB 를 사용태 `remember-me` 쿠키를 비교한다.  

```java
@Autowired
DataSource datasource;

private PersistentTokenRepository getJDBCRepository() {
    JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
    jdbcTokenRepository.setDataSource(datasource);
    return jdbcTokenRepository;
}
```

```java
http.rememberMe()
    .rememberMeParameter("remember-me") // default: remember-me
    .key("spring-demo-security-key") // secret key
    .tokenValiditySeconds(60 * 60 * 24) // 24 hour, default  2week
    .alwaysRemember(false) // default: false
    .tokenRepository(getJDBCRepository())
;
```

아래와 같이 로그인토큰 관리용 테이블 생성  
> `persistent_logins` 테이블명이 하드코딩되어있음으로 변경 불가능  

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

### 로그인 정보 표시

현재 `thymeleaf`를 통해 뷰 페이지를 출력하고 있으며 시큐리티에 대한 태그를 사용하려면 `thymeleaf-extras-springsecurity5` 의존성을 추가해야 한다.  

```xml
<dependency>
  <groupId>org.thymeleaf.extras</groupId>
  <artifactId>thymeleaf-extras-springsecurity5</artifactId>
  <version>3.0.4.RELEASE</version>
</dependency>
```

```jsp
<div class="panel panel-default">
    <div sec:authorize="isAuthenticated()">
        <h3>LOGIN USER INFO</h3>
        <div sec:authentication="name">Spring seucurity username</div>
        <div>[[${#authentication.name}]]</div>
        <div sec:authorize="hasRole('ROLE_ADMIN')">This Conetent Only For ADMIN</div>
        <div sec:authorize="hasRole('ROLE_MANAGER')">This Conetent Only For MANAGER</div>
        <div sec:authorize="hasRole('ROLE_BASIC')">This Conetent Only For BASIC</div>
        <div sec:authorize="hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_BASIC')">This Content For Everyone</div>
        <div>[[${#authentication.principal}]]</div>
        <div th:with="member=${#authentication.principal.member}">
            <div>[[${member.uid}]]</div>
            <div>[[${member.upw}]]</div>
            <div>[[${member.uname}]]</div>
        </div>
    </div>
</div>
```

### 로그인 후 페이지 이동  

로그인 후 기존 url 로 다시 이동시키려면 단순 `redirect` 형식으로는 불가능하고  
`referer` 헤더에 저장된 이전 url 을 세션에 저장해두었다가 login success handler 가 세션에서 데이터를 꺼내어 redirect 하는 방식을 사용한다.  

> 직접 `/login` url로 로그인시에는 루트 디렉토리로 이동

다음과 같이 `successForwardUrl` 대신 `successHandler` 를 사용  

```java
http
    ...
    .formLogin() // login config
    //.successForwardUrl("/auth/login_success") // login success redirect url
    .successHandler(new CustomLoginSuccessHandler("/boards/list"))
    ...
;
```

```java
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    // 
    public CustomLoginSuccessHandler(String defaultTargetUrl) {
        setDefaultTargetUrl(defaultTargetUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session != null) {
            String redirectUrl = (String) session.getAttribute("prevPage");
            if (redirectUrl != null) {
                session.removeAttribute("prevPage");
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
```

세션에 `prevPage` 데이터를 넣는 과정은 아래 `login[GET]` 과정에서 진행한다.  

```java
@GetMapping("/login_demo")
public void login(HttpServletRequest request) {
    String referrer = request.getHeader("Referer");
    request.getSession().setAttribute("prevPage", referrer);
    // /resource/template/auth/login_demo.html 생성 필요
}
```

만약 로그인버튼에 `/auth/login_demo?prefPage` 형식처럼 파라미터를 붙일 수 있다면  
아래처럼 `/auth/login_demo` 에 인터셉터를 걸어서도 사용가능하다.  

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry r   egistry) {
        registry.addInterceptor(new LoginCheckInterceptor()).addPathPatterns("/auth/login_demo");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}

public class LoginCheckInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String prevPage = request.getParameter("prevPage");
        if (prevPage != null)
            request.getSession().setAttribute("prevPage", prevPage); // 목적지가 있었다면 세션에 저장 
        return super.preHandle(request, response, handler);
    }
}
```

## Rest API 기반 스프링 시큐리티

`Spring Boot` 가 `Rest API` 위주의 서비스 지원 서버가 되면서 JWT 기반의 `session less` 한 방식을 주로 사용한다.  

`remember-me` 와 비슷하게 JWT 라는 로그인토큰을 발급해서 인증한다.  

> JWT(JSON Web Token)  
> <https://jwt.io/>  

### 스프링 시큐리티 + JWT

> 참고: <https://www.javainuse.com/spring/boot-jwt>
> java jwt library: <https://github.com/jwtk/jjwt#install-jdk-gradle>

![springboot_security4](/assets/springboot/springboot_security3.png)

스프링 시큐리티의 인증구조를 커스터마이징 해야 하기에 변경해야할 코드가 많다.  

`jwt` 토큰 생성을 위해 아래 `dependency` 포함

```groovy
dependencies {
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5',
    // Uncomment the next line if you want to use RSASSA-PSS (PS256, PS384, PS512) algorithms:
    //'org.bouncycastle:bcprov-jdk15on:1.70',
    'io.jsonwebtoken:jjwt-jackson:0.11.5' // or 'io.jsonwebtoken:jjwt-gson:0.11.5' for gson
}
```

로그인시 jwt 토큰을 생성 및 반환하는 구조는 아래 사진과 같다.  

![springboot_security4](/assets/springboot/springboot_security4.png)

1. `/authenticate` url 로 `username`, `password` 정보와 함께 `jwt` 토큰 요청  
2. 이미 토큰 데이터를 가지고 있지 않은지 확인  
3. 없다면 `generateAuthenticateToken()` 메서드 호출  
4. `authenticate()` 를 사용해 `username`, `password` 를 검증  
5. 검증을 위해 `username` 을 DB 에서 검색, `Userdetails` 를 요청  
6. `UserdetailsService` 로부터 로그인정보 수신  
7. 반환값을 토대로 성공/실패 결정  
8. `generateToken()` 메서드 호출, 로그인정보로 JWT 토큰 생성요청  
9. 토큰값 반환  

### JwtTokenUtil

먼저 `JWT` 토큰을 생성가능한 `JwtTokenUtil` 정의  

```java
public class JwtTokenUtil implements Serializable {

    public static final long JWT_TOKEN_VALIDITY_SEC = 5 * 60; //5분
    public static Random random = new Random();
    private static SecretKey secretKey;


    static {
        byte[] data = new byte[255];
        random.nextBytes(data);
        String secret = "tM6S1ERulKlPWSsvzZa3Kun9vpH3YbikZpospKYhYS97vtUKiNDFFXFnyTqJX1bL";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes()); //or HS384 or HS512
    }

    // jwt 생성
    public static String generateToken(CustomSecurityUser customSecurityUser) {
        Map<String, Object> claims = customSecurityUser.getClaims();
        String subject = customSecurityUser.getUsername();
        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + JWT_TOKEN_VALIDITY_SEC * 1000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    // Claims expiration date 혹은 username 를 가져오기 위해 호출
    // 모든 값은 verify signature 부분에서 가져온다.
    private static <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //jwt 로부터 username get
    private static String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    //jwt 로부터 exp get
    private static Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 토큰의 각종 데이터를 되찾아 오기 위해 시크릿 키가 필요
    public static Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 시간 초과 확인
    public static Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // validate token
    public static Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

### JwtRequestFilter

토큰으로부터 유저 아이디를 확인하고 해당 토큰이 로그인시에 암호화해서 발급했던 토큰이 맞는지 확인  

```java

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // check request header JWT
        final String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER)) {
            log.warn("JWT Token does not begin with Bearer String, url:{}", request.getRequestURL());
            request.setAttribute("exception", "INVALID AUTHORIZATION HEADER"); //
        } else {
            // generate auth object & save at security context
            String jwtToken = authorization.substring(7);
            Map<String, Object> claims = JwtTokenUtil.getAllClaimsFromToken(jwtToken);
            Authentication authentication = getAuthentication(claims); // generate auth object
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private Authentication getAuthentication(Map<String, Object> claims) {
        Long uid = Long.valueOf(claims.getOrDefault("uid", 0).toString());
        String subject = claims.getOrDefault("sub", "").toString();
        List<String> roles = (List<String>) claims.get("roles");
        UserDetails userDetails = new CustomSecurityUser(uid, subject, roles);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
```

`doFilterInternal` 에 jwt 기반으로 DB검색을 통해 `authentication` 를 생성해도 되지만  
DB 연결이 발생함으로 민감하지 않은 정보만 가지고 `authentication` 객체를 생성, 사용해도 된다.  

> jwt 토큰 해시검증을 통해 로그인한 사용자임은 알 수 있음으로 패스워드와 같은 정보를 비교할 필요가 없다.  

`CustomSecurityUser` 객체는 아래와 같이 jwt 토큰으로 생성할 수 있도록 변경  

```java
@Getter
@Setter
public class CustomSecurityUser extends User {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Long uid;
    private final String uname;

    // create by login
    public CustomSecurityUser(Member member) {
        super(member.getUname(), member.getUpw(), makeGrantedAuth(member.getRoles()));
        this.uid = member.getUid();
        this.uname = member.getUname();
    }

    // create by jwt
    public CustomSecurityUser(Long uid, String subject, List<String> roles) {
        super(subject, "", roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList()));
        this.uid = uid;
        this.uname = subject;
    }

    // make auth from login
    private static List<GrantedAuthority> makeGrantedAuth(List<MemberRole> roles) {
        List<GrantedAuthority> list = new ArrayList<>();
        roles.forEach(memberRole ->
                list.add(new SimpleGrantedAuthority(ROLE_PREFIX + memberRole.getRoleName())));
        return list;
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", uid);
        claims.put("uname", uname);
        claims.put("roles", getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return claims;
    }
}
```

### Login Controller

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class RestAuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @PostMapping("/login_demo")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword());
        authentication = authenticationManagerBuilder.getObject().authenticate(authentication);
        String jwtToken = JwtTokenUtil.generateToken((CustomSecurityUser) authentication.getPrincipal());
        return new LoginResponseDto(jwtToken);
    }
}
```

<!-- 
## OAuth2

스프링 시큐리티에서 `OAuth`를 지원하는 프레임워크가 잘 되어있지만 스프링 부트에서 템플릿까지 제공할 경우이다.  

`Rest API` 서버이면서 `OAuth2` 인ㄴ증을 지원하려면 별도로 지원하는게 마음 편하다.  

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

## Spring Security cors 에러 이슈

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
-->