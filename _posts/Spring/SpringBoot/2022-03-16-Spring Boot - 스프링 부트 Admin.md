---
title:  "Spring Boot - 스프링 부트 Admin!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Boot Admin

> github: <https://github.com/codecentric/spring-boot-admin>
> ref doc: <https://codecentric.github.io/spring-boot-admin/2.6.6/>

spring actuator 를 사용한 오픈소스 모니터링 프로젝트  
모니터링에 대해 별도의 운영환경이 없다면 사용해볼만한 프로젝트임.  

관제 서버로 사용할 `Boot Admin Server` 디펜던시  
관제할 클라이언트로 사용할 `Boot Admin Client` 디펜던시가 따로 있다.  

```groovy
implementation 'de.codecentric:spring-boot-admin-starter-server:2.6.6'
implementation 'de.codecentric:spring-boot-admin-starter-client:2.6.6'
```

### Admin Server

```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'de.codecentric:spring-boot-admin-starter-server:2.6.6'
```

`Admin Server` 를 운영하기 위해 위 3개 `dependency` 는 필수  
`security` 의 경우 `id/pw` 를 통해 자동으로 보안처리 할 수 있음으로 사용 권장한다.  

> 참고: <https://www.baeldung.com/spring-boot-admin>

우선 security 에 고정된 문자열로 계정을 사용한다.  

```conf
server.port=8080
spring.security.user.name=admin
spring.security.user.password=admin
```

`security` 에 `UI` 를 위한 리소스 파일은 권한없이 접근하도록 지정하고  
`csrf`, `cors` 등의 대한 처리를 진행  


```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AdminServerProperties adminServer;
    private final SecurityProperties security;

    public SecurityConfig(AdminServerProperties adminServer, SecurityProperties security) {
        this.adminServer = adminServer;
        this.security = security;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.getContextPath() + "/applications");
        http
                .httpBasic().and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).ignoringRequestMatchers(
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances", HttpMethod.POST.toString()),
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/instances/*", HttpMethod.DELETE.toString()),
                    new AntPathRequestMatcher(this.adminServer.getContextPath() + "/actuator/**")).and()
                .cors().configurationSource(corsConfigurationSource()).and()
                .authorizeRequests()
                .antMatchers(this.adminServer.getContextPath() + "/favicon.ico").permitAll()
                .antMatchers(this.adminServer.getContextPath() + "/assets/**").permitAll()
                .antMatchers(this.adminServer.getContextPath() + "/login").permitAll()
                .anyRequest().authenticated().and()
                .formLogin().loginPage(this.adminServer.getContextPath() + "/login").successHandler(successHandler).and()
                .logout().logoutUrl(this.adminServer.getContextPath() + "/logout").and()
                .rememberMe().key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600);
        //.httpBasic(Customizer.withDefaults())
    }
    // Required to provide UserDetailsService for "remember functionality"

    // 사전에 설정한 security user, pw 사용 
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(security.getUser().getName())
                .password("{noop}" + security.getUser().getPassword()).roles("USER");
    }

    public CorsConfigurationSource corsConfigurationSource() {
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

`this.adminServer.getContextPath()` 의 `default` 값은 공백이지만 `proxy` 환경이나 별도의 `Boot Admin` 을 위한 `context path` 를 지정하고 싶을때 `spring.boot.admin.context-path` 를 사용하여 변경 가능하다.  

`Boot Admin` 의 경우 각종 메트릭 정보를 `Boot Client` 의 `actuator` 로부터 가져오는데  
`Boot Client` 에서도 아무나 actuator 정보를 가져가지 못하도록 Security 를 설정해야 한다.  

필자의 경우 `Boot Client` 애서 `Filter` 를 통해 헤더에 `x-api-key` 가 들어있을 경우 매트릭 정보를 얻기 위한 접근으로 인식하도록 할 예정이기에 `HttpHeadersProvider` `Bean` 을 등록해 `Boot Admin` 의 모든 요청에 헤더를 심어서 요청하도록 할 예정이다.  

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.security.user.password}")
    private String xApiKey;

    @Bean
    public HttpHeadersProvider customHttpHeadersProvider() {
        return instance -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("x-api-key", xApiKey);
            return httpHeaders;
        };
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(-1)   // add maxAge
                .allowCredentials(false);
    }
}
```

### Admin Client  

`Boot Admin` 에서 지정한 security 계정, `actuator` 에 대한 설정, log 를 볼수 있도록 `logfile` 에 대해 설정한다.  

```conf
# --------------- profile
server.port=8082
management.endpoint.health.enabled=true
management.endpoints.web.base-path=/actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
spring.profiles.active=dev
spring.application.name=store
application.api.key=1q2w3e4r
# --------------- Spring Boot Admin
spring.boot.admin.client.url=http://localhost:8080
spring.boot.admin.client.username=admin
spring.boot.admin.client.password=admin
logging.file.name=${HOME}/demo/logs/${spring.application.name}/application.log
```

위에서 말했던 대로 `actuator` 의 모든 `endpoint` 를 `security` 로 보호되며  
정의한 `RequestFilter` 에 의해 `x-api-key` 헤더가 있을경우 `Boot Admin` 의 요청으로 인식하도록 설정한다.  

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${application.api.key}")
    private String apiKey;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource()).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .anyRequest().authenticated().and()
                .addFilterAfter(new RequestFilter(apiKey), BasicAuthenticationFilter.class);
    }

    public CorsConfigurationSource corsConfigurationSource() {
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

public class RequestFilter extends OncePerRequestFilter {
    
    private static final String API_KEY = "api-key";
    private final String apiKey;

    public RequestFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // x-api-key 를 가지고 있을 경우 system 호출
        final String apiKey = request.getHeader(API_KEY);
        if (apiKey != null && apiKey.equals(this.apiKey)) {
            Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_SYSTEM"));
            Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin", authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
            return;
        } else {
            String msg = "api key does not exist";
            request.setAttribute("exception", msg);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }
}
```

## Spring Boot Amdin + Proxy

`Nginx` 나 Aws 환경에서 `Boot Admin` 프로젝트를 사용할 경우 `Proxy` 환경에서 운영하게 될 수 있는데  
`Boot Admin` 의 경우 관련 UI 관련 리소스 파일을 가져오거나 로그인 후 `redirect` 요청등을 수행했을 때 되지 않는 경우가 대부분이다.  

로그인 핸들러의 `successHandler.setDefaultTargetUrl` 함수의 `redirect url` 을 주의하여 삽입하고 각종 UI 리소스 파일의 경로, proxy 조건에 따라 `context path` 를 사용할지 안할지를 잘 지정해서 `Boot Admin` 을 구축해야한다.  

### spring.boot.admin.ui.public-url

UI를 위한 `js`, `css` 등의 리소스 파일을 가져올때 `spring.boot.admin.ui.public-url` 속성을 설정하지 않을 경우
`spring boot admin` 이 동작하고있는 서버의 `url` 을 가져오기 때문에 `proxy` 서버에서 사용되는 `url` 과 다를 수 있다.  

```conf
# default "/"
spring.boot.admin.context-path=/admin
spring.boot.admin.ui.public-url=https://custom.domain.com/admin
```

## 데모 코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/admin-demo>