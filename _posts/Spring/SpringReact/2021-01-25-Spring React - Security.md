---
title:  "Spring React - Security!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## classes: wide

categories:
  - spring-reative
---

## Spring Security with WebFlux

> 그림 출처: Hands-On Spring Security 5 for Reactive Applications

![1](/assets/springboot/spring-react/springreact_security1.png)  

그림을 보면 `Spring Security` 개발진들이 기존 WebMVC 와 동일하게 환경을 구성하기 위해 노력한 과정을 볼 수 있다.  
`Filter` 기반의 인증과정, `AuthenticationManager` 와 `UserDetailService` 를 사용한 인증과정이 기존 `WebMVC` 와 유사한것을 확인할 수 있다.  

`spring-boot-starter-security` 모듈 역시  `WebFlux` 를 지원할 수 있도록 업데이트 되었다.  

### SecurityContext from Reactor Context

`Webflux` 와 `WebMVC` 에서 `Spring Security` 의 가장 큰 차이점은 기존의 `SecurityContext` 의 개념이 사용되지 않는 것.  

`WebMVC Spring Security` 는 `ThreadLocal` 에 `SecurityContext` 를 저장해 한번의 연결동안 `Authentication` 인증객체를 계속 유지했지만,  
`Webflux` 에선 하나의 연결에 여러개의 `Thread` 꼬여있을 수 있어 **Reactor Context** 를 사용한다.  
(하나의 연결에대해 하나의 스레드가 담당하여 처리한다는 보장이 없다)  

`WebMVC` 에선 `SecurityContextHolder` 에서 `SecurityContext` 를 가져왔다면  
`WebFlux` 에선 `ReactiveSecurityContextHolder` 에서 `SecurityContext` 를 가져온다.  

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SecuredProfileController {

    private final ProfileService profileService;

    @GetMapping("/profiles")
    public Mono<Profile> getProfile() {
        return ReactiveSecurityContextHolder.getContext() // Mono<SecurityContext> 반환
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> profileService.getByUser(auth.getName()));
    }
}
```

로그인한 유저의 `SecurityContext` 가져와 `Profile` 에서 검색 후 출력한다.  
당연히 `SecurityContext::getAuthentication` 메서드는 `Reactor Context` 를 사용한다.  

```java
// ReactiveSecurityContextHolder.java

public static Mono<SecurityContext> getContext() {
    return Mono.subscriberContext()
        .filter( c -> c.hasKey(SECURITY_CONTEXT_KEY))
        .flatMap( c-> c.<Mono<SecurityContext>>get(SECURITY_CONTEXT_KEY));
}
```

사실 메소드의 인자로 추가하면 스프링 시큐리티가 **구독자 컨텍스트(subscriber context)** 에서 `Authentication` 정보를 추출해서 인자로 주입해준다.

```java
@GetMapping("/profiles/auth")
public Mono<Member> getProfileAuth(Authentication auth) {
    return memberRepo.findByUserName(auth.getName());
}
```

이런 코드가 작성 가능한 이유는 우리가 작성한 비즈니스 로직 전까지 `SecurityContext` 객체를 지속적으로 매개변수로 넘기기 때문.  

### SecurityWebFilterChain

![2](/assets/springboot/spring-react/springreact_security2.png)  

인증과정을 거친 `SecurityContext` 가 해당 `Reactor Context` 에 존재해야하고,  
`Authentication` 객체가 `SecurityContext` 안에 할당되어야 한다.  

`Spring MVC` 방식에서 `HttpSecurity` 를 설정해서 각종 보안설정을 했듯이  
`Spring WebFlux` 방식에서 `SecurityWebFilterChain` 을 설정해서 보안설정이 이루어진다.  

아래 예는 `Member` 들을 사전에 입력해두고 `formLogin` 설정시 `ReactiveUserDetailsService` 를 통해 로그인 지원하는 설정이다.  

```java
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
        
    @Bean
    public CommandLineRunner demo(MemberRepo repository, PasswordEncoder encoder) {
        return (args) -> {
            log.info("save start!");
            // save a few customers
            repository.saveAll(Arrays.asList(
                Member.builder().name("Kim").password(encoder.encode("Kim")).userName("Kim@test.com").role("ROLE_ADMIN").build(),
                Member.builder().name("Chloe").password(encoder.encode("Chloe")).userName("Chloe@test.com").role("ROLE_MEMBER").build(),
                Member.builder().name("David").password(encoder.encode("David")).userName("David@test.com").role("ROLE_MEMBER").build(),
                Member.builder().name("Michelle").password(encoder.encode("Michelle")).userName("Michelle@test.com").role("ROLE_MEMBER").build()
            )).blockLast(Duration.ofSeconds(10));
        };
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(MemberRepo repository) {
        return username -> repository.findByUserName(username)
            .map(member -> User.builder()
                .username(member.getUserName())
                .password(member.getPassword())
                .authorities(member.getRole())
                .build());
    }

    @Bean
    public SecurityWebFilterChain securityFilterChainConfigurer(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .authorizeExchange().pathMatchers("/member/**").hasAuthority("ROLE_MEMBER")
                .and()
                .httpBasic().and()
                .formLogin().and()
                .csrf().disable()
                .build();
    }
    ...
}
```

`Spring MVC` 에서 `@EnableGlobalMethodSecurity` 를 사용했듯이  
`Spring WebFlux` 에서 `@EnableReactiveMethodSecurity` 를 사용해 Security 어노테이션들을 사용할 수 있다.  

```java
@PreAuthorize("hasRole('MEMBER')")
@GetMapping("/profiles/auth")
public Mono<Member> getProfile(Authentication auth) {
    return memberRepository.findByName(auth.getName());
}
```

### ServerSecurityContextRepository


`SecurityContext` 를 `Reactor Context` 에 집어넣을 때 `ServerSecurityContextRepository` 를 사용한다.  

```java
package org.springframework.security.web.server.context;

public interface ServerSecurityContextRepository {
    Mono<Void> save(ServerWebExchange exchange, SecurityContext context);
    Mono<SecurityContext> load(ServerWebExchange exchange);
}
```


### WebFlux with JWT

#### Custom SecurityContextRepository, AuthenticationManager

스프링 시큐리티는 `SecurityContextRepository` 를 통해 `SecurityContext` 를 `리액터 컨텍스트`에 저장하고 삭제한다.  

default 스프링 시큐리티의 경우 DB 로부터 UserDetails 를 가져와 등록해두고 사용하겠지만 우리는 JWT 를 사용하기에 `ServerSecurityContextRepository` 상속받아 커스터마이징 해야한다.  

```java
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authToken != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return authenticationManager
                    .authenticate(auth)
                    .map(authentication -> new SecurityContextImpl(authentication));
        } else {
            return Mono.empty();
        }
    }
}
```

`SecurityContext` 를 생성 및 저장하기 request 헤더로 부터 JWT 토큰을 가져와 `AuthenticationManager`로 넘기는 코드가 `load` 에 정의되어 있다.  

```java
@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        if (!jwtUtil.validateToken(authToken)) {
            return Mono.empty();
        }
        Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
        String role = claims.get("role", String.class);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        return Mono.just(new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities));
    }
}
```

`AuthenticationManager` 는 전달받은 토큰으로 `role` 을 꺼내어 `Authority` 를 지정하고 `Authentication` 인증 객체를 반환한다.  

`SecurityContextPath` 가 리액터 컨텍스트로 변경되었을 뿐 기존 `WebMVC` 모델의 스프링 시큐리티 방식과 비슷하다.  

```java
// ReactorContextWebFilter 에 적용할 시큐리티 필터
// 기존 mvc 에서 사용하던 HttpSecurity 에서 webflux 용으로 정의된 ServerHttpSecurity
// 기존 spring security 사용 방식과 크게 다르지 않다.
@Bean 
public SecurityWebFilterChain securityFilterChainConfigurer(ServerHttpSecurity httpSecurity) {
    return httpSecurity
        .exceptionHandling()
        //.authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> // 미 로그인
        //        swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
        //.accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> // 미 로그인
        //        swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
        .authenticationEntryPoint((swd, e) -> Mono.error(new AuthenticationCredentialsNotFoundException("")))
        .accessDeniedHandler((swe, e) -> Mono.error(new AccessDeniedException("")))
        .and()
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .csrf().disable()
        .cors().disable()
        .httpBasic().disable() // Basic Authentication disable
        .formLogin().disable()
        .authorizeExchange()
        .pathMatchers("/member/join", "/member/login").permitAll()
        .pathMatchers("/member/**", "/rent/**").authenticated()
        .pathMatchers("/admin/**").hasAnyRole("ROLE_ADMIN")
        .anyExchange().permitAll()
        .and()
        .build();
}
```

마지막으로 `ServerHttpSecurity` 에 커스터마이징한 `SecurityContextRepository, AuthenticationManager` 를 등록하고 별도의 에러 핸들링 처리를 한다면  에러를 반환해 핸들링,  
없다면 `HttpStatus.UNAUTHORIZED` 로 단순 `HTTP Status` 만 반환할 수 있다.  
