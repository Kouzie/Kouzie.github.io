---
title:  "Spring Boot - OAuth!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
  - oauth
---

## 개요  

OAuth 중 `Authorization Code` 방식인증은 아래 순서대로 진행된다.  

> <https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-07#name-authorization-code-grant>  
> 여기서 `Resource Owner` 는 사용자, `User Agent` 는 브라우저라 할 수 있다.  

`Spring OAuth` 라이브러리에선 `OAuth 2.0, OIDC(OAuth 2.1)` 프로토콜을 모두 지원하며,  
`Authorization Server` 에선 기본적으로 `OIDC` 를 사용한다.  

```
 +----------+
 | Resource |
 |   Owner  |
 +----------+
       ^
       |
       |
 +-----|----+          Client Identifier      +---------------+
 | .---+---------(1)-- & Redirection URI ---->|               |
 | |   |    |                                 |               |
 | |   '---------(2)-- User authenticates --->|               |
 | | User-  |                                 | Authorization |  Front Channel
 | | Agent  |                                 |     Server    |
 | |        |                                 |               |
 | |    .--------(3)-- Authorization Code ---<|               |
 +-|----|---+                                 +---------------+
   |    |                                          ^      v
   |    |                                          |      |
   ^    v                                          |      |
 +----------+                                      |      |
 |          |>---(4)-- Authorization Code ---------'      |
 | Resource |          & Redirection URI                  |       Back Channel
 |  Client  |                                             |
 |          |<---(5)----- Access Token -------------------'
 +----------+       (w/ Optional Refresh Token)
              
              Figure 3: Authorization Code Flow
```

`Spring Boot` 에서 `OAuth 2.0, OAuth 2.1(OpenID)` 를 지원하는 라이브러리를 제공한다.  

> <https://spring.io/projects/spring-authorization-server>  
> <https://docs.spring.io/spring-authorization-server/reference/getting-started.html>  
> <https://docs.spring.io/spring-authorization-server/reference/guides/how-to-dynamic-client-registration.html>  

단순 라이브러리 설정을 통해 아래 3가지 서버를 구성 가능하다.  

- Authorization Server(인증서버)  
- Resource Client(리소스 서버)  
- Resource Server(사용자정보 서버)  

## Spring Boot Resource Client

`Resource Client` 는 `spring-boot-starter-oauth2-client` 라이브러리로 구성 가능하다.  

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    // oauth2 login 이후 이동될 페이지 출력용 thymeleaf
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

구성할 설정은 아래와 같다.  

- naver `OAuth 2.0`, kakao `OIDC` 프로토콜을 사용해 OAuth 기능 사용.  
- 자체구축한 `Spring Authorization Server` 의 `OIDC` 기능 사용.  
- 로그인한 `access token, refresh token` 정보는 DB 에 저장.  
  - `access token` 이 만료되거나 `Resource Server` 에서 데이터를 요청해야 할 때 사용할 수 있다.  
  - `spring-boot-starter-oauth2-client` 라이브러리에서 제공하는 `oauth2-client-schema.sql` 로 테이블 생성 가능.  
- session 기반 인증, stateless JWT 기반 인증 구성.  

```conf
# server config
server.port=8080
spring.profiles.active=jwt
logging.level.root=debug
# datasource config
spring.datasource.url=jdbc:h2:mem:test;MODE=MYSQL;
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
#spring data jpa config
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
#h2 console access
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
# 타 authorization server client id/secret
naver.oauth.client.id=TkIAw1v...
naver.oauth.client.secret=Pjz...
kakao.oauth.client.id=90f9c9a...
kakao.oauth.client.secret=L4a...
spring.jwt.secret=liytQ9XESwlBLtAReEvUv8f5fApFvHZLvYgvA8Pd9t1May0xQTokUdKrrY46tlJ0
# oauth2-client-schema 생성을 위한 설정, access token, refresh token 저장용
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:org/springframework/security/oauth2/client/oauth2-client-schema.sql
```

`naver, kakao` 의 `Authorization Server`, 그리고 자체구축할 `Spring Authorization Server` 를 사용하기 위해 3개의 `ClientRegistration` 등록.  
`Resource Client` 에 실시간으로 새로운 `Authorization Server` 가 추가될일은 없기에 `InMemoryClientRegistrationRepository` 로 구성.  

```java
@Value("${naver.oauth.client.id}")
private String naverOAuthClientId;
@Value("${naver.oauth.client.secret}")
private String naverOAuthClientSecret;
@Value("${kakao.oauth.client.id}")
private String kakaoOAuthClientId;
@Value("${kakao.oauth.client.secret}")
private String kakaoOAuthClientSecret;

@Bean
public ClientRegistrationRepository clientRegistrationRepository() {
    // oidc 를 지원할 경우 /.well-known/openid-configuration URL 을 통해
    // auth code, token, userinfo 를 가져오는 url 을 자동으로 등록한다
    ClientRegistration springAuthDemoClient = ClientRegistrations.fromIssuerLocation("http://authorization-server")
        .registrationId("oauth-demo-registration-id")
        .clientId("oauth-demo-client-id")
        .clientSecret("secret")
        .clientName("Resource Client Demo")
        .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // PKCE 방식, code_challenge code_verifier 를 사용
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect")
        .userNameAttributeName(IdTokenClaimNames.SUB) // default 가 sub
        .scope(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)
        .build();

    // naver 에선 oauth 2.0 만 지원, 각종 oauth 관련 url 을 수기로 작성해줘야 한다.
    ClientRegistration naverAuthClient = ClientRegistration.withRegistrationId("naver-auth-registration-id")
        .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
        .tokenUri("https://nid.naver.com/oauth2.0/token")
        .userInfoUri("https://openapi.naver.com/v1/nid/me")
        .clientId(naverOAuthClientId)
        .clientSecret(naverOAuthClientSecret)
        .clientName("Naver OAuth Client Demo")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // HTTP Basic 인증 헤더사용, Authorization 헤더에 client secret Base64
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://127.0.0.1:8080/login/oauth2/code/naver-oauth-redirect")
        .userNameAttributeName("response") // naver 에선 sub 가 없고 id 역할을 하는 username 을 response 로 감싸고 있어 부득이하기 부모 key 값을 써야함
        .scope("name", "email")
        .build();
    /* { resultcode=00, message=success, response={id=xqmroU.., name=홍길동, email=kouzie@naver.com} } */

    // kakao 에선 oidc 를 지원한다.
    // https://kauth.kakao.com/.well-known/openid-configuration
    ClientRegistration kakaoAuthClient = ClientRegistrations.fromIssuerLocation("https://kauth.kakao.com")
        .registrationId("kakao-auth-registration-id")
        .clientId(kakaoOAuthClientId)
        .clientSecret(kakaoOAuthClientSecret)
        .clientName("Kakao OAuth Client Demo")
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST) // Form 데이터 형태로 client secret Base64
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://127.0.0.1:8080/login/oauth2/code/kakao-auth-redirect")
        .scope(OidcScopes.OPENID) // https://developers.kakao.com/docs/latest/ko/kakaologin/utilize#scope-user
        .build();
    
    return new InMemoryClientRegistrationRepository(springAuthDemoClient, naverAuthClient, kakaoAuthClient);
}
```

`oauth2-client-schema.sql` 로 `access token, id token` 을 관리해야 함으로 `OAuth2AuthorizedClientService` 의 JDBC 구현체인 `JdbcOAuth2AuthorizedClientService` 생성.  

```java
/* oauth2-client-schema.sql
CREATE TABLE oauth2_authorized_client (
  client_registration_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  access_token_type varchar(100) NOT NULL,
  access_token_value blob NOT NULL,
  access_token_issued_at timestamp NOT NULL,
  access_token_expires_at timestamp NOT NULL,
  access_token_scopes varchar(1000) DEFAULT NULL,
  refresh_token_value blob DEFAULT NULL,
  refresh_token_issued_at timestamp DEFAULT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (client_registration_id, principal_name)
);
 */
// Resource Client 가 여러개 띄어져 있어도 JDBC 를 통해 DB 에서 access token, refresh token 등을 검색하기 때문에 로그인이 풀리지 않음
@Bean
public OAuth2AuthorizedClientService authorizedClientService(DataSource dataSource, ClientRegistrationRepository clientRegistrationRepository) {
    return new JdbcOAuth2AuthorizedClientService(new JdbcTemplate(dataSource), clientRegistrationRepository);
}
```

로그인, 에러, 인덱스 페이지는 `Spring Security` 에 `ignore` 되도록 처리.  

```java
@Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring()
      .requestMatchers("/")
      .requestMatchers("/login")
      .requestMatchers("/error")
      .requestMatchers("/h2-console/**");
}
```

```java
public HttpSecurity setDefaultHttpSecurity(ClientRegistrationRepository clientRegistrationRepository,
                                            OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                            ResourceClientUserService resourceClientUserService,
                                            HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .clientRegistrationRepository(clientRegistrationRepository) // naver kakao spring oauth 설정
            .authorizedClientService(oAuth2AuthorizedClientService) // jdbc authorizedClientService 사용하도록 변경
            .defaultSuccessUrl("/main", false) // alwaysUse 는 이전 방문 페이지로 이동시킴
            .loginPage("/login") // custom login page 지정
            .userInfoEndpoint(userinfo -> userinfo // userinfo 요청 처리 객체
                .userService(new DelegatingOAuth2UserService(List.of(
                    new NaverOAuth2UserService(resourceClientUserService)
                )))
                .oidcUserService(new DelegatingOAuth2UserService(List.of(
                    new KakaoOidc2UserService(resourceClientUserService),
                    new SpringOidc2UserService()
                )))
            )
        );
    http.exceptionHandling(exceptions -> exceptions
        .authenticationEntryPoint(authenticationEntryPoint())
        .accessDeniedHandler(accessDeniedHandler())
    );
    return http;
}
```

위와같이 `Resource Client` 의 `Spring Security` 구성 후 `OAuth` 요청을 처리하기 위해 등록되는 `Security Filter` 들을 출력하면 아래와 같다.  

```java
@PostConstruct
public void printSecurityFilters() {
    List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
    for (SecurityFilterChain chain : filterChains) {
        List<Filter> filters = chain.getFilters();
        System.out.println("Security Filter Chain: " + chain);
        for (Filter filter : filters) {
            System.out.println(filter.getClass());
        }
    }
}
/*
class o.sf.sec.web.session.DisableEncodeUrlFilter
class o.sf.sec.web.context.request.async.WebAsyncManagerIntegrationFilter
class o.sf.sec.web.context.SecurityContextHolderFilter
class o.sf.sec.web.header.HeaderWriterFilter
class o.sf.web.filter.CorsFilter
class o.sf.sec.web.csrf.CsrfFilter
class o.sf.sec.web.authentication.logout.LogoutFilter
class o.sf.sec.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
class o.sf.sec.oauth2.client.web.OAuth2LoginAuthenticationFilter
class o.sf.sec.web.authentication.ui.DefaultLoginPageGeneratingFilter
class o.sf.sec.web.authentication.ui.DefaultLogoutPageGeneratingFilter
class o.sf.sec.web.savedrequest.RequestCacheAwareFilter
class o.sf.sec.web.servletapi.SecurityContextHolderAwareRequestFilter
class o.sf.sec.web.authentication.AnonymousAuthenticationFilter
class o.sf.sec.web.access.ExceptionTranslationFilter
class o.sf.sec.web.access.intercept.AuthorizationFilter
*/
```

### OAuth2AuthorizedClientService

`Resource Owner` 의 `access_token` 을 확인하여 유효기간이 남아있는 로그인인지 파악한다.  
동일한 `Resource Client` 가 여러개 띄어져 있는 상황에서 해당 데이터를 InMemory 가 아닌 DB 에 저장하고 관리할 수 있어야 한다.  

```java
// Resource Client 가 여러개 띄어져 있어도 JDBC 를 통해 DB 에서 access token, refresh token 등을 검색하기 때문에 로그인이 풀리지 않음
@Bean
public OAuth2AuthorizedClientService authorizedClientService(DataSource dataSource,
                                                             ClientRegistrationRepository clientRegistrationRepository) {
    return new JdbcOAuth2AuthorizedClientService(new JdbcTemplate(dataSource), clientRegistrationRepository);
}
```

```sql
-- org/springframework/security/oauth2/client/oauth2-client-schema.sql
CREATE TABLE oauth2_authorized_client (
  client_registration_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  access_token_type varchar(100) NOT NULL,
  access_token_value blob NOT NULL,
  access_token_issued_at timestamp NOT NULL,
  access_token_expires_at timestamp NOT NULL,
  access_token_scopes varchar(1000) DEFAULT NULL,
  refresh_token_value blob DEFAULT NULL,
  refresh_token_issued_at timestamp DEFAULT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (client_registration_id, principal_name)
);
```

별도로 지정하지 않을 경우 `InMemoryOAuth2AuthorizedClientService` 를 내부적으로 사용한다.  

### OAuth2UserService

`access token` 을 이용해 `userinfo` 를 조회하는 객체  

userinfo 는 정해진 표준이 없고 naver, kakao 등 지원하는 사용자 정보가 모두 다르다 보니 `OAuth2UserService` 는 커스텀 하는 경우가 대부분.  

- 위에서 설명했듯이 `OAuth2UserService` 는 사용자 정보를 가져오기 위한 객체, `access token` 까지 전달받은 뒤 수행된다.  
- `DefaultOAuth2UserService` 에서 `access token, ClientRepository` 의 정보를 사용해 `userinfo` 의 HTTP 요청하는 것까지 모두 구현되어 있기 떄문에 조금만 수정하면 바로 사용 가능하다.  
- 또한 OAuth 사용자 인증과 동시에 회원가입 처리할 경우 `userinfo` 조회 후 회원가입 로직까지 추가해야 한다.  

```java
public interface OAuth2UserService<R extends OAuth2UserRequest, U extends OAuth2User> {
  U loadUser(R userRequest) throws OAuth2AuthenticationException;
}

// package org.springframework.security.oauth2.client.userinfo;
public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
  ...
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
      // user info 를 가져오기 위한 request 생성 및 호출
      RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
      ResponseEntity<Map<String, Object>> response = getResponse(userRequest, request);
      ...
      return new DefaultOAuth2User(authorities, userAttributes, userNameAttributeName);
  }
}
```

- naver 의 경우 `userinfo` 응닶값이 `response` 로 한꺼풀 감싸져있어 사용자 정보 파싱을 위해 별도의 `OAuth2UserService` 를 구성해야 한다.  
- `DefaultOAuth2UserService` 에 대부분의 구현 내용이 있기 때문에 그대로 사용하며 파싱을 위한 내용만 `NaverOAuth2UserService` 를 사용, `DefaultOAuth2User` 도 그대로 사용한다.  

```java
@Slf4j
@RequiredArgsConstructor
public class NaverOAuth2UserService extends DefaultOAuth2UserService {
    private final static String registrationId = "naver-auth-registration-id";
    private final ResourceClientUserService resourceClientUserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        if (!userRequest.getClientRegistration().getRegistrationId().equals(registrationId)) {
            return null;
        }
        log.info("naver loadUser invoked, request:{}", userRequest.toString());
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // repsonse 로 감싸져있어 DefaultOAuth2UserService 를 사용하지 못하고 한꺼풀 벗기는 용으로 사용
        Map<String, Object> attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
        // naver 는 nickname 을 name 으로 사용함
        String nickname = attributes.getOrDefault("name", "unknown_nickname").toString();
        String email = attributes.getOrDefault("email", "unknown_email").toString();
        // naver 에서는 계정을 email 로 사용
        String name = attributes.getOrDefault("username", email).toString();
        String role = "ROLE_USER";
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
          nickname, email, name, role, registrationId, oAuth2User);
        // 회원가입 upsert
        ResourceClientUserDto user = resourceClientUserService.upsertUser(customOAuth2User);
        // 단순 로그인처리만 진행할거라면 oAuth2User 를 그대로 반환해도 상관없음.
        return customOAuth2User;
    }
}
@Slf4j
@RequiredArgsConstructor
public class KakaoOidc2UserService extends OidcUserService {
    private final static String registrationId = "kakao-auth-registration-id";
    private final ResourceClientUserService resourceClientUserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        if (!userRequest.getClientRegistration().getRegistrationId().equals(registrationId)) {
            return null;
        }
        log.info("kakao loadUser invoked, request:{}", userRequest.toString());
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> claims = oidcUser.getClaims();
        String nickname = claims.getOrDefault("nickname", "unknown_nickname").toString();
        String email = claims.getOrDefault("email", "unknown_email").toString();
        String name = oidcUser.getName();
        String role = "ROLE_USER";
        CustomOAuth2User customOAuth2User =  new CustomOAuth2User(
          nickname, email, name, role, registrationId, oidcUser);
        // 회원가입 upsert
        ResourceClientUserDto user = resourceClientUserService.upsertUser(customOAuth2User);
        return customOAuth2User;
    }
}
```

`userinfo` 조회 후 해당 객체로 `Authentication` 인증객체를 생성하고, JWT 까지 생성해야 하기 때문에 `CustomOAuth2User` 같은 `Resource Client` 전용 인증객체를 생성하는것을 권장한다.  

### JWT 기반 Resource Client  

MSA 구조, 모바일 앱 환경에선 `session` 을 잘 사용하지 않고 JWT 와 같은 `stateless` 한 방식을 주로 사용한다.  

> 쿠키를 사용하지 않을경우 JWT 를 별도로 저장하고 있다 응답하는 API 를 작성해야 하는데 번거로워 OAuth 에선 쿠키 사용방식을 권장한다.  
> session 이 없기 때문에 Access Denied 되었던 url 로 이동하고 싶다면 프론트엔드에서 별도로 처리해주어야함.  

```java
@Bean
@Profile("jwt")
public SecurityFilterChain securityFilterChainJwt(ClientRegistrationRepository clientRegistrationRepository,
                                                OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
                                                ResourceClientUserService resourceClientUserService,
                                                OAuthLoginSuccessHandler oAuthLoginSuccessHandler,
                                                JwtUtil jwtUtil,
                                                HttpSecurity http) throws Exception {
    setDefaultHttpSecurity(clientRegistrationRepository,
        oAuth2AuthorizedClientService,
        resourceClientUserService,
        http);
    http.oauth2Login(oauth2 -> oauth2.successHandler(oAuthLoginSuccessHandler)); // 로그인 성공시 jwt 토큰을 cookie 에 추가
    http
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class); // cookie 에 저장된 jwt 를 확인하는 filter 추가
    return http.build();
}
```

OAuth 의 웹뷰 기반 `redirect` 형태의 환경에서 JWT 를 넘겨주기 위해 쿠키를 사용한다.  

`redirect response` 의 쿠키에 JWT 를 넣어 전달하면 다음 request 부터는 쿠키에 JWT 데이터가 삽입되어 전달된다.  

`login success handler` 구성 후 `redirect response` 의 쿠키에 JWT 를 삽입한다.  

```java
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String token = generateToken(oAuth2User);
        response.addCookie(createCookie("Authorization", token));
        response.sendRedirect("http://resource-client/main");
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS가 필요한 경우 true로 설정
        cookie.setPath("/");
        //cookie.setDomain(""); localhost 환경에서 사용 X
        cookie.setMaxAge(60 * 60 * 60);
        return cookie;
    }
}


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // check request header JWT
        Cookie[] cookies = request.getCookies() == null ? new Cookie[]{} : request.getCookies();
        String token = null;
        for (Cookie cookie : cookies) {
            System.out.println(cookie.getName());
            if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                token = cookie.getValue();
            }
        }
        if (token == null) {
            log.warn("JWT Token does not begin with Bearer String, url:{}", request.getRequestURL());
            request.setAttribute("exception", "INVALID AUTHORIZATION HEADER");
            chain.doFilter(request, response);
            return;
        }
        if (jwtUtil.isExpired(token)) {
            log.warn("JWT Token expired, url:{}", request.getRequestURL());
            request.setAttribute("exception", "EXPIRED TOKEN");
            chain.doFilter(request, response);
            return;
        }
        // generate auth object & save at security context
        Authentication authentication = jwtUtil.getAuthentication(token);
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

}
```

oauth2Login success handler 구성과 JWT filter 클래스를 등록.  

```java
http
    .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
    .oauth2Login(oauth2 -> oauth2
        .successHandler(oAuthLoginSuccessHandler) // 로그인완료 후 호출되는 success handler 구성
        .clientRegistrationRepository(clientRegistrationRepository)
        .authorizedClientService(oAuth2AuthorizedClientService) // jdbc authorizedClientService 사용하도록 변경
        ...
    )
http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
```

### Spring OAuth2 Client 인증과정  

OIDC 프로토콜을 지원하는 `Authorization Server` 의 경우 `/.well-known/oauth-authorization-server` API 를 지원하며 아래 3개 URI 를 자동으로 가져올 수 있다.  
`OidcProviderConfigurationEndpointFilter` 에서 해당 url 을 처리한다.  

- `authorizationUri`: `authorization code` 를 발급받을 수 있는 로그인 페이지 redirect url
- `tokenUri`: `access token` 을 발급받을 수 있는 `API url(REST, Formdata)`
- `userInfoUri`: `userinfo` 를 발급받을 수 있는 `Rest API url`

`Spring Authorization Server` 에서 제공하는 데이터는 아래와 같다.  

```json
// `/.well-known/oauth-authorization-server`
{
    "issuer": "http://resource-client",
    "authorization_endpoint": "http://resource-client/oauth2/authorize",
    "device_authorization_endpoint": "http://resource-client/oauth2/device_authorization",
    "token_endpoint": "http://resource-client/oauth2/token",
    "token_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post",
        "client_secret_jwt",
        "private_key_jwt"
    ],
    "jwks_uri": "http://resource-client/oauth2/jwks",
    "response_types_supported": [ "code" ],
    "grant_types_supported": [
        "authorization_code",
        "client_credentials",
        "refresh_token",
        "urn:ietf:params:oauth:grant-type:device_code"
    ],
    "revocation_endpoint": "http://resource-client/oauth2/revoke",
    "revocation_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post",
        "client_secret_jwt",
        "private_key_jwt"
    ],
    "introspection_endpoint": "http://resource-client/oauth2/introspect",
    "introspection_endpoint_auth_methods_supported": [
        "client_secret_basic",
        "client_secret_post",
        "client_secret_jwt",
        "private_key_jwt"
    ],
    "code_challenge_methods_supported": [ "S256" ]
}
```

OAuth 인증과정은 5단계, `Resource Client` 에서 사용되는 `Spring Filter`, `AuthenticationProvider` 는 아래와 같다.  

1. **Client Identifier & Redirection URI**
   - 인증되지 않은 사용자가 `Resource Client` 의 `/test` 페이지 접속시 `Access Denied` 에러 발생  
   - `LoginUrlAuthenticationEntryPoint` 는 `OAuth2Login` 에 `loginPage("/login")` 설정대로 로그인 페이지로 이동시킴.    
     - 동시에 `/main` 페이지로 이동하려 했던것을 `session cache` 에 저장(`Spring Security` 기본 동작구조).  
   - `/login.html` 에 정의한대로 아래 `Spring Authorization Sever` 로그인 방식 사용  
   - <http://resource-client/oauth2/authorization/oauth-demo-registration-id>(1)
   - `OAuth2AuthorizationRequestRedirectFilter` 에서 `url(1)` 를 처리.  
     - `registration-id` 와 매칭되는 `Authorization Server` 의 `authorizationUri` 로 `redirect`
     - <http://authorization-server/oauth2/authorize?response_type=code&...>(2)
2. **User authenticates** 
   - `url(2)` 이동된 후 사용자는 `Authorization Server` 의 로그인페이지에서 로그인 진행  
   - 로그인 성공시 `Resource Client` 가 등록한 `redirect url` 로 `authorization code` 와 함께 `redirect`  
   - <http://resource-client:8080/login/oauth2/code/oauth-client-redirect?code=EvsI...>(3)
3. **Authorization Code**
   - `OAuth2LoginAuthenticationFilter` 에서 `url(3)` 을 처리.  
   - `Authorization Code` 로 `OAuth2LoginAuthenticationToken` 을 생성하고 인증 `Provider` 로 넘김.  
   - `OIDC` 프로토콜의 경우 `OidcAuthorizationCodeAuthenticationProvider` 에서 요청을 처리
   - `OAuth 2.0` 프로토콜일 경우 `OAuth2LoginAuthenticationProvider` 에서 요청을 처리.  
4. **Authorization Code & Redirection URI**
   - `Authorization Code` 로 `access token` 을 요청  
   - 위에서 설명한 `OidcAuthorizationCodeAuthenticationProvider` 혹은 `OAuth2LoginAuthenticationProvider` 에서 `access token` 을 요청한다.  
   - `access token` 을 얻은 뒤 `OAuth2UserService` 를 통해 `userinfo` 에 대한 요청도 해당 `Provider` 내에서 수행한다.  
   - `userinfo` 요청시 `HTTP Header` `Authorization: Bearer {access_token}` 설정해서 요청한다.   
   - `OidcAuthorizationCodeAuthenticationProvider` 사용시 `userinfo` 요청 전에 `id token` 검증을 위해 `NimbusJwtDecoder` 에서 `/oauth/jwks` 호출한다.  
5. **Access Token**  
   - `Authorization Server` 는 `access token` 를 응답한다.  
   - `OAuth2LoginAuthenticationFilter` 에서 `access token` 을 DB 에 저장하고 `access token` 을 사용해 `Authentication` 객체를 생성한다.  
     - `session diable` 을 하지않았다면 `Spring Security` 기본 설정대로 인증 정보를 `session` 에 저장한다.  
   - `OAuth2LoginAuthenticationFilter` 에서 `url(3)` 의 응답은 `Spring Security` 기본 설정인 `SavedRequestAwareAuthenticationSuccessHandler` 가 이어서 처리한다.  
     - 아래 둘중 하나로 redirect 응답한다.  
     - `Access Denied` 기록이 `session` 에 저장 되었던 `/test` 페이지 로 redirect  
     - `Access Denied` 기록이 `session` 에 없다면 `defaultSuccessUrl(/main)` 페이지로 redirect  

실제 `access token, userinfo` 호출을 수행하는 `OidcAuthorizationCodeAuthenticationProvider, OAuth2LoginAuthenticationProvider` 코드는 아래와 같다.  

```java
// OidcAuthorizationCodeAuthenticationProvider::authenticate
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
  OAuth2LoginAuthenticationToken authorizationCodeAuthentication = (OAuth2LoginAuthenticationToken) authentication;
  // REQUIRED. OpenID Connect requests MUST contain the "openid" scope value.
  if (!authorizationCodeAuthentication.getAuthorizationExchange()
    .getAuthorizationRequest()
    .getScopes()
    .contains(OidcScopes.OPENID)) {
    // This is NOT an OpenID Connect Authentication Request so return null
    // and let OAuth2LoginAuthenticationProvider handle it instead
    return null;
  }
  ...
  /* /oauth2/token 요청 */
  OAuth2AccessTokenResponse accessTokenResponse = getResponse(authorizationCodeAuthentication);
  ClientRegistration clientRegistration = authorizationCodeAuthentication.getClientRegistration();
  ...
  /* /oauth2/jwks 요청, 서명값 인증 */
  OidcIdToken idToken = createOidcToken(clientRegistration, accessTokenResponse);
  validateNonce(authorizationRequest, idToken);
  /* /userinfo 요청, 사용자 정보 흭득, OAuth2UserService 사용 */
  OidcUser oidcUser = this.userService.loadUser(new OidcUserRequest(clientRegistration,
      accessTokenResponse.getAccessToken(), idToken, additionalParameters));
  ...
  OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(
      authorizationCodeAuthentication.getClientRegistration(),
      authorizationCodeAuthentication.getAuthorizationExchange(), oidcUser, mappedAuthorities,
      accessTokenResponse.getAccessToken(), accessTokenResponse.getRefreshToken());
  return authenticationResult;
}

// OAuth2LoginAuthenticationProvider::authenticate
public Authentication authenticate(Authentication authentication) throws AuthenticationException {
  OAuth2LoginAuthenticationToken loginAuthenticationToken = (OAuth2LoginAuthenticationToken) authentication;
  // REQUIRED. OpenID Connect requests MUST contain the "openid" scope value.
  if (loginAuthenticationToken.getAuthorizationExchange()
    .getAuthorizationRequest()
    .getScopes()
    .contains("openid")) {
    // This is an OpenID Connect Authentication Request so return null
    // and let OidcAuthorizationCodeAuthenticationProvider handle it instead
    return null;
  }
  ...
  /* https://nid.naver.com/oauth2.0/token, access_token 요청 */
  OAuth2AccessToken accessToken = authorizationCodeAuthenticationToken.getAccessToken();
  /* https://openapi.naver.com/v1/nid/me 요청, user info 요청 */
  OAuth2User oauth2User = this.userService.loadUser(new OAuth2UserRequest(
      loginAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));
  Collection<? extends GrantedAuthority> mappedAuthorities = this.authoritiesMapper
    .mapAuthorities(oauth2User.getAuthorities());
  OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(
      loginAuthenticationToken.getClientRegistration(), loginAuthenticationToken.getAuthorizationExchange(),
      oauth2User, mappedAuthorities, accessToken, authorizationCodeAuthenticationToken.getRefreshToken());
  authenticationResult.setDetails(loginAuthenticationToken.getDetails());
  return authenticationResult;
}
```

반환된 인증객체는 `OAuth2LoginAuthenticationFilter` 에서 이어서 처리하며 `DB` 에 인증객체를 저장하고 `session` 에도 저장될 수 있도록 인증객체를 반환한다.  

```java
// OAuth2LoginAuthenticationFilter::attemptAuthentication
// token 내용을 뺀 사용자 인증데이터(세션 저장용) 생성
OAuth2AuthenticationToken oauth2Authentication = this.authenticationResultConverter.convert(authenticationResult);
// access_token, refresh_token, 사용자 정보 등 생성
OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
    authenticationResult.getClientRegistration(), oauth2Authentication.getName(),
    authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());
// 토큰정보는 OAuth2AuthorizedClientRepository 저장소(inmemory, jdbc) 에 저장
this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, oauth2Authentication, request, response);
return oauth2Authentication;
// 반환된 oauth2Authentication 은 밖의 필터에 의해 session 에 저장됨
```

## Spring Authorization Server

`Spring Boot` 에서 `OAuth 2.1, OpenID Connect` 를 지원하는 `Authorization Server` 라이브러리는 아래와 같다.  

```groovy
dependencies {
    implementation "org.springframework.boot:spring-boot-starter-oauth2-authorization-server"
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf' // login page 용
}
```

등록될 `Resource Client` 에 대해 yaml 로 간단히 설정할 수 있다.  

```yaml
server:
  port: 9000
spring:
  security:
    user:
      name: user
      password: password
    oauth2:
      authorizationserver:
        client:
          my-demo-resource-client:              # resource client 설정 시작
            require-authorization-consent: true # 사용자가 인증 요청을 받을 때 동의 화면을 볼지 여부
            registration:
              client-id: "oidc-client"          # resource client id
              client-secret: "{noop}secret"     # resource client secret
              client-authentication-methods:    # client_id와 client_secret을 사용해 기본 인증을 수행
                - "client_secret_basic"
              authorization-grant-types:        # auth server 가 부여하는 데이터
                - "authorization_code"          # access token 을 얻을 때 사용
                - "refresh_token"               # access token 만료시 사용
              redirect-uris:
                - "http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect"
              post-logout-redirect-uris:
                - "http://127.0.0.1:8080/"
              scopes:
                - "openid"
                - "profile"
```

`Spring Security` 와 연계되어 다수의 `Spring Security Filter` `Spring Security Provider` 가 등록되어 `OAuth` 를 구현할 수 있다.  

실제 `Spring Security Filter` 에 등록된 Filter 들을 출력하면 아래와 같다.  

```java
private final FilterChainProxy filterChainProxy;

@PostConstruct
public void printSecurityFilters() {
    List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
    for (SecurityFilterChain chain : filterChains) {
        List<Filter> filters = chain.getFilters();
        System.out.println("Security Filter Chain: " + chain);
        for (Filter filter : filters) {
            System.out.println(filter.getClass());
        }
    }
}
/* 
class o.sf.sec.oauth2.server.authorization.oidc.web.OidcLogoutEndpointFilter
class o.sf.sec.web.authentication.logout.LogoutFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2AuthorizationServerMetadataEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2DeviceVerificationEndpointFilter
class o.sf.sec.oauth2.server.authorization.oidc.web.OidcProviderConfigurationEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.NimbusJwkSetEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter
class o.sf.sec.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
class o.sf.sec.web.savedrequest.RequestCacheAwareFilter
class o.sf.sec.web.servletapi.SecurityContextHolderAwareRequestFilter
class o.sf.sec.web.authentication.AnonymousAuthenticationFilter
class o.sf.sec.web.access.ExceptionTranslationFilter
class o.sf.sec.web.access.intercept.AuthorizationFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2TokenEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2TokenIntrospectionEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2TokenRevocationEndpointFilter
class o.sf.sec.oauth2.server.authorization.web.OAuth2DeviceAuthorizationEndpointFilter
class o.sf.sec.oauth2.server.authorization.oidc.web.OidcUserInfoEndpointFilter
class o.sf.sec.oauth2.server.authorization.oidc.web.OidcClientRegistrationEndpointFilter
*/
```

### Authorization Server Core Model  

> <https://docs.spring.io/spring-authorization-server/reference/core-model-components.html>

- **RegisteredClient**  
  `Resource Client` 도메인 객체  
- **OAuth2Authorization**  
  `Resource Client - Resource Owner` 매핑 및 스코프 관련 도메인 객체  
- **OAuth2AuthorizationConsent**  
  `Resource Owner 동의(consent)` 도메인 객체  
- **OAuth2TokenGenerator**  
  authorization code, access token, refresh token, oidc id token 등 각종 토큰 생성 도메인 객체  

#### RegisteredClient

위 `application.yaml` 에선 하드코딩으로 `Resource Client` 를 등록하였지만 일반적으로 `Authorization Server` 에서 동적으로 `Resource Client` 를 등록한다.  

```java
package org.springframework.security.oauth2.server.authorization.client;

public class RegisteredClient implements Serializable {
    private static final long serialVersionUID;
    private String id;                          // Unique ID for Resource Client
    private String clientId;                    // Resource Client 아이디
    private Instant clientIdIssuedAt;           // Resource Client 등록일
    private String clientSecret;                // Resource Client 시크릿키, PasswordEncoder 로 인코딩되어야함
    private Instant clientSecretExpiresAt;      // Resource Client 시크릿키 만료일
    private String clientName;                  // Resource Client 설명용 이름(회사명 같은), 동의페이지 등 특정시나리오에서 사용 
    private Set<String> redirectUris;           // authorization_code 과정에서 사용할 redirect url 
    private Set<String> postLogoutRedirectUris; // 로그아웃 이후 redirect url
    private Set<String> scopes;                 // 클라이언트 요청 범위, openid, profile, email, address, phone 등
    private ClientSettings clientSettings;      // 권한부여, PKCE 등 클라이언트 사용자 정의 설정
    private TokenSettings tokenSettings;        // oauth token 의 사용자 정의 설정
    private Set<ClientAuthenticationMethod> clientAuthenticationMethods; // 클라이언트 사용 인증 방법 
    private Set<AuthorizationGrantType> authorizationGrantTypes;  // 권한부여 유형, authorization_code, refresh_token 를 주로 사용
    ...
}
```

생성된 `RegisteredClient` 는 `RegisteredClientRepository` 빈객체를 통해 관리된다.  

```java
@Bean
public RegisteredClientRepository registeredClientRepository() {
    List<RegisteredClient> registrations = ...;
    return new InMemoryRegisteredClientRepository(registrations);
}
```

```java
@RestController
@RequestMapping("/register")
public class ClientRegistrationController {

    @PostMapping
    public ResponseEntity<ClientRegistrationResponse> registerClient(@RequestBody ClientRegistrationRequest request) {
        // 클라이언트 등록 로직 (DB에 저장하거나 동적 생성)
        ClientRegistrationResponse response = clientRegistrationService.register(request);
        return ResponseEntity.ok(response);
    }
}
```

`ClientAuthenticationMethod` 는 `Resource Client` 의 요청을 인증하는 방법으로 default 는 `CLIENT_SECRET_POST` 를 사용.  

- **CLIENT_SECRET_BASIC**  
  `client_secret` 을 Authentication 헤더에 Base64로 인코딩하여 서버에 인증하는 방법.  
- **CLIENT_SECRET_POST**  
  `client_secret` 을 Form Data 파라미터로 추가하여 인증하는 방법.  
- **NONE**  
  `client_id`, `PKCE` 기반 인증을 사용.  
  프론트 기반 인증처럼 `client_secret` 을 사용할 수 없는 `공개 클라이언트`에서 사용하는 방식.  
- **CLIENT_SECRET_JWT**  
  `client_secret` 으로 서명된 JWT 을 사용하여 인증하는 방법.  
  Spring OAuth 에서 기본제공하지 않으며 추가구현 필요.  
- **PRIVATE_KEY_JWT**  
  비대칭 키로 서명된 JWT 을 사용하여 인증하는 방법. 서버는 클라이언트의 공개 키를 사용해 서명을 검증.  
  Spring OAuth 에서 기본제공하지 않으며 추가구현 필요.  

향후 `Resource Client` 가 동작할 수 있도록 `Auhtorization Server`에 사전등록.  


```java
// Ahotirzation Server 의 Resource Client 등록을 위한 코드  
@PostConstruct
void init() {
    // Ahotirzation Server 의 Resource Client 등록을 위한 코드
    RegisteredClient.Builder registration = RegisteredClient.withId("oauth-client-demo")
        .clientId("oauth-demo-client-id")
        // plaintext is secret It is encoded with BCrypt from EncodedSecretTests
        // do not include secrets in the source code because bad actors can get access to your secrets
        .clientSecret(passwordEncoder.encode("secret"))
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantTypes(types -> {
            types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
            types.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
            types.add(AuthorizationGrantType.REFRESH_TOKEN);
        })
        .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect")
        .scopes(scopes -> {
            scopes.add("openid");
            scopes.add("profile");
            scopes.add("email");
        })
        .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .build());
    this.save(registration.build());
}
```

#### OAuth2Authorization

`Resource Owner` 와 `Resource Client` 간의 매핑, 권한 부여관련 데이터  

```java
package org.springframework.security.oauth2.server.authorization;

public class OAuth2Authorization implements Serializable {
    private String id;                      // 고유 식별자
    private String registeredClientId;      // Resource Client ID
    private String principalName;           // Resource Owner 주요이름
    private Set<String> authorizedScopes;   // Resource Client 에게 승인된 scope
    private Map<String, Object> attributes; // 부여한 권한의 추가 속성
    private AuthorizationGrantType authorizationGrantType;      // 권한부여 유형, authorization_code, refresh_token 를 주로 사용
    private Map<Class<? extends OAuth2Token>, Token<?>> tokens; // OAuth2Token실행된 권한 부여 유형에 대한 인스턴스
    ...
}
```

`OAuth2AuthorizationService` 에 의해 관리된다.  

```java
package org.springframework.security.oauth2.server.authorization;

public interface OAuth2AuthorizationService {
    void save(OAuth2Authorization authorization);
    void remove(OAuth2Authorization authorization);
    OAuth2Authorization findById(String id);
    OAuth2Authorization findByToken(String token, @Nullable OAuth2TokenType tokenType);
}
```

#### OAuth2AuthorizationConsent

권한부여의 동의(consent)를 관리하기 위한 데이터  

```java
package org.springframework.security.oauth2.server.authorization;

public final class OAuth2AuthorizationConsent implements Serializable {
    private static final long serialVersionUID;
    private static final String AUTHORITIES_SCOPE_PREFIX = "SCOPE_";
    private final String registeredClientId;          // Resource Client ID
    private final String principalName;               // Resource Owner 주요이름
    private final Set<GrantedAuthority> authorities;  // 권한목록

```

`OAuth2AuthorizationConsentService` 에 의해 관리된다.  

```java

package org.springframework.security.oauth2.server.authorization;

public interface OAuth2AuthorizationConsentService {
    void save(OAuth2AuthorizationConsent authorizationConsent);
    void remove(OAuth2AuthorizationConsent authorizationConsent);
    OAuth2AuthorizationConsent findById(String registeredClientId, String principalName);
}
```

#### OAuth2TokenGenerator

```java
public interface OAuth2Token {
    String getTokenValue();

    @Nullable
    default Instant getIssuedAt() {
        return null;
    }

    @Nullable
    default Instant getExpiresAt() {
        return null;
    }
}

public abstract class AbstractOAuth2Token implements OAuth2Token, Serializable {
    private static final long serialVersionUID = 620L;
    private final String tokenValue;
    private final Instant issuedAt;
    private final Instant expiresAt;
}

@FunctionalInterface
public interface OAuth2TokenGenerator<T extends OAuth2Token> {
    @Nullable
    T generate(OAuth2TokenContext context);
}
```

`OAuth2TokenContext` 는 토큰 생성을 위한 입력 파라미터값 표현 클래스, `OAuth2TokenGenerator` 로 각종 토큰 생성 입력 파라미터로 사용된다.  

`OAuth2TokenGenerator` 예제에서 생성하는 토큰 종류는 아래 4가지  

- **OAuth2AuthorizationCode**  
- **OAuth2RefreshToken**  
  `OAuth2AuthorizationCode, OAuth2RefreshToken` 토큰은 단순 인증을 위해 사용하다 보니 서저에 저장될 데이터도 `tokenValue, issuedAt, expiredAt` 가 전부이다.  
- **OAuth2AccessToken**  
  `OAuth2AccessToken` 은 `scopes` 추가정보, 서버에서 해당 토큰을 통해 관리할 `claims` 데이터를 위한 추가 하위 객체를 제공한다.  
- **Jwt**  
  `Jwt` 도 마찬가지로 `clamis` 정보를 요구한다.  



```Java
// package org.springframework.security.oauth2.server.authorization;
public class OAuth2AuthorizationCode extends AbstractOAuth2Token {
    public OAuth2AuthorizationCode(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}

// package org.springframework.security.oauth2.core;
public class OAuth2RefreshToken extends AbstractOAuth2Token {
    public OAuth2RefreshToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}

// package org.springframework.security.oauth2.core;
public class OAuth2AccessToken extends AbstractOAuth2Token {
    private final TokenType tokenType; // new TokenType("Bearer");
    private final Set<String> scopes;

    public OAuth2AccessToken(TokenType tokenType, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes) {
        super(tokenValue, issuedAt, expiresAt);
        this.tokenType = tokenType;
        this.scopes = Collections.unmodifiableSet(scopes != null ? scopes : Collections.emptySet());
    }
}
// package org.springframework.security.oauth2.server.authorization.token;
private static final class OAuth2AccessTokenClaims extends OAuth2AccessToken implements ClaimAccessor {
    private final Map<String, Object> claims;

    private OAuth2AccessTokenClaims(OAuth2AccessToken.TokenType tokenType, String tokenValue, Instant issuedAt, Instant expiresAt, Set<String> scopes, Map<String, Object> claims) {
        super(tokenType, tokenValue, issuedAt, expiresAt, scopes);
        this.claims = claims;
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }
}

// package org.springframework.security.oauth2.jwt;
public class Jwt extends AbstractOAuth2Token implements JwtClaimAccessor {
    private final Map<String, Object> headers;
    private final Map<String, Object> claims;

    public Jwt(String tokenValue, Instant issuedAt, Instant expiresAt, Map<String, Object> headers, Map<String, Object> claims) {
        super(tokenValue, issuedAt, expiresAt);
        Assert.notEmpty(headers, "headers cannot be empty");
        Assert.notEmpty(claims, "claims cannot be empty");
        this.headers = Collections.unmodifiableMap(new LinkedHashMap(headers));
        this.claims = Collections.unmodifiableMap(new LinkedHashMap(claims));
    }
}
```

`DelegatingOAuth2TokenGenerator` 를 통해 위 4가지 토큰은 모두 생성가능하도록 지원한다.  

```java
@Bean
public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JwtEncoder jwtEncoder) {
    JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
    OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    return new DelegatingOAuth2TokenGenerator(
        jwtGenerator, // id_token 
        accessTokenGenerator, // access_token
        refreshTokenGenerator // refresh_token
    );
}
```

#### JPA Sample

> <https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html>

`Core Module` 에서 관리하는 도메인객체들을 관리할 때 아래와 같이 직접 생성 및 `InMemory` 에서 관리할 수 도 있지만 보통 DB 를 주로 사용한다.  

```java
RegisteredClient oidcClient = RegisteredClient.withId("oauth-client-demo")
    .clientId("oauth-demo-client-id")
    // plaintext is secret It is encoded with BCrypt from EncodedSecretTests
    // do not include secrets in the source code because bad actors can get access to your secrets
    .clientSecret(passwordEncoder.encode("secret"))
    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
    .authorizationGrantTypes(types -> {
        types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
        types.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
        types.add(AuthorizationGrantType.REFRESH_TOKEN);
    })
    .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect")
    .scopes(scopes -> {
        scopes.add("openid");
        scopes.add("profile");
        scopes.add("email");
    })
    .clientSettings(ClientSettings.builder()
            .requireAuthorizationConsent(true)
            .build());
    .build();
```

여기서는 spring doc 링크에서 제공해준 JPA 구현체를 사용.  

`JPA` 를 통해 `RegisteredClient, OAuth2Authorization, OAuth2AuthorizationConsent` 3가지 `Core Model` 를 관리하는 예제를 제공한다.  

클래스 파일을 추가하기 싫다면 라이브러리에서 제공하는 JDBC 로 구현한 `Core Model` 을 사용하는것도 가능하다.  

```conf
spring.sql.init.schema-locations=\
  classpath:org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql,\
  classpath:org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
```

### OAuth2AuthorizationServerConfigurer

정의한 `Core Module` 는 `Spring Security` 에서 관리하는 `OAuth2AuthorizationServerConfigurer` 에서 등록할 수 있다.  

> 개인적으로 application.properties 를 통해 구성설정을 하는 것 보다 Java Config 를 통해 구성하는 것을 선호한다.  

```java
@Bean
  @Order(1)
  public SecurityFilterChain authorizationServerSecurityFilterChain(RegisteredClientRepository registeredClientRepository,
                                                                    OAuth2AuthorizationService authorizationService,
                                                                    OAuth2AuthorizationConsentService authorizationConsentService,
                                                                    OAuth2TokenGenerator<OAuth2Token> tokenGenerator,
                                                                    JwtEncoder jwtEncoder,
                                                                    JwtDecoder jwtDecoder,
                                                                    HttpSecurity http) throws Exception {
      OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
      OAuth2AuthorizationServerConfigurer authz = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
      authz
              .registeredClientRepository(registeredClientRepository)
              .authorizationService(authorizationService)
              .authorizationConsentService(authorizationConsentService)
              .tokenGenerator(tokenGenerator)
              .authorizationServerSettings(AuthorizationServerSettings.builder().build())
              .oidc(oidc -> oidc.clientRegistrationEndpoint(clientRegistrationEndpoint -> {
                  clientRegistrationEndpoint
                          .authenticationProviders(configureCustomClientMetadataConverters());
              }));
      /*authz
              .authorizationServerSettings( AuthorizationServerSettings.builder()
                      .issuer("http://resource-client")
                      .authorizationEndpoint("/oauth2/v1/authorize")
                      .deviceAuthorizationEndpoint("/oauth2/v1/device_authorization")
                      .deviceVerificationEndpoint("/oauth2/v1/device_verification")
                      .tokenEndpoint("/oauth2/v1/token")
                      .tokenIntrospectionEndpoint("/oauth2/v1/introspect")
                      .tokenRevocationEndpoint("/oauth2/v1/revoke")
                      .jwkSetEndpoint("/oauth2/v1/jwks")
                      .oidcLogoutEndpoint("/connect/v1/logout")
                      .oidcUserInfoEndpoint("/connect/v1/userinfo")
                      .oidcClientRegistrationEndpoint("/connect/v1/register")
                      .build())
              // 각 endpoint 에 대해서 추가적으로 동작시킬 config 설정이 가능하다.  
              .clientAuthentication(clientAuthentication -> { })
              .authorizationEndpoint(authorizationEndpoint -> { })
              .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint -> { })
              .deviceVerificationEndpoint(deviceVerificationEndpoint -> { })
              .tokenEndpoint(tokenEndpoint -> { })
              .tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> { })
              .tokenRevocationEndpoint(tokenRevocationEndpoint -> { })
              .authorizationServerMetadataEndpoint(authorizationServerMetadataEndpoint -> { })
              .oidc(oidc -> oidc
                      .providerConfigurationEndpoint(providerConfigurationEndpoint -> { })
                      .logoutEndpoint(logoutEndpoint -> { })
                      .userInfoEndpoint(userInfoEndpoint -> { })
                      .clientRegistrationEndpoint(clientRegistrationEndpoint -> { })
              );*/
      http
              .securityMatchers(matchers -> matchers.requestMatchers(antMatcher("/oauth2/**"), authz.getEndpointsMatcher()))
              // Accept access tokens for User Info and/or Client Registration
              .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder)))
              // Redirect to the login page when not authenticated from the
              // authorization endpoint
              .exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
      return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
          throws Exception {
      http
              .authorizeHttpRequests((authorize) -> authorize
                      .requestMatchers("/login", "/error").permitAll()
                      .anyRequest().authenticated()
              )
              // Form login handles the redirect to the login page from the
              // authorization server filter chain
              .formLogin(login -> login.loginPage("/login"))
              .cors(AbstractHttpConfigurer::disable)
              .csrf(AbstractHttpConfigurer::disable);

      return http.build();
  }
```

`/oauth2/**` 로 시작하는 `OAuth Endpoint URL` 처리를 위해 `Spring Security Filter` 의 `requestMatchers` 에 등록해준다.  

각 엔드포인트들의 커스텀한 처리를 위해 아래 함수 및 `AbstractOAuth2Configurer` 구현클래스를 사용할 수 있다.  

- **authorizationEndpoint**
  OAuth2 인증 요청을 처리하는 엔드포인트,
- **tokenEndpoint**
  OAuth2 액세스 토큰을 발급하는 엔드포인트
- **tokenIntrospectionEndpoint**
  OAuth2 토큰 상태를 확인하는 엔드포인트
- **tokenRevocationEndpoint**
  토큰 무효화(Revocation) 처리하는 엔드포인트
- **jwkSetEndpoint**
  JSON Web Key Set(JWKS)를 제공하는 엔드포인트
- **oidcLogoutEndpoint**
  OpenID Connect 로그아웃을 처리하는 엔드포인트
- **oidcUserInfoEndpoint**
  OpenID Connect 사용자 정보를 제공하는 엔드포인트
- **oidcClientRegistrationEndpoint**
  OpenID Connect 클라이언트 등록을 처리하는 엔드포인트
- **deviceAuthorizationEndpoint**
  디바이스가 사용자 코드와 디바이스 코드를 요청하는 엔드포인트
  iot, smart tv 같이 입력이 제한된 환경에서 사용하는 device 인증 flow 에서 사용  
- **deviceVerificationEndpoint**
  사용자가 브라우저를 통해 코드 입력을 완료하는 엔드포인트


`AbstractOAuth2Configurer` 에선 `OAuth Endpoint URL` 를 처리할 `Filter` 클래스의 생성, 인증과정에 사용할 `Providers` 를 `Filter` 에 적용 등의 작업을 수행한다.  

```java
package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

abstract class AbstractOAuth2Configurer {
    private final ObjectPostProcessor<Object> objectPostProcessor;

    AbstractOAuth2Configurer(ObjectPostProcessor<Object> objectPostProcessor) {
        this.objectPostProcessor = objectPostProcessor;
    }
    // AuthenticationProvider 를 정의하고 Spring Security 에 등록
    abstract void init(HttpSecurity httpSecurity);
    // AuthenticationFilter 에서 인증과정을 수행할 필터 등록  
    abstract void configure(HttpSecurity httpSecurity);
    // 인증과정을 수행할 GET POST url 등록
    abstract RequestMatcher getRequestMatcher();

    protected final <T> T postProcess(T object) {
        return this.objectPostProcessor.postProcess(object);
    }

    protected final ObjectPostProcessor<Object> getObjectPostProcessor() {
        return this.objectPostProcessor;
    }
}
```

`oauth2-authorization-server` 라이브러리에선 기본적으로 이를 구현한 **기본 설정 객체**들이 제공되어 `Spring Security` 에 등록되어 있다.  

각 `AbstractOAuth2Configurer` 에선 `Core Model` 객체를 사용해 `OAuth2` 의 인증과정을 `Spring Security Filter` 과정에 녹여낸다.  

예로 `기본설정객체` 중 `Access Token` 을 발급하는 `OAuth2TokenEndpointConfigurer` 의 `init, configure` 함수를 보면 아래와  같다.  

```java
package org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers;

public final class OAuth2TokenEndpointConfigurer extends AbstractOAuth2Configurer {
    ...
    void init(HttpSecurity httpSecurity) {
        // 인증과정 POST url 등록
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtils.getAuthorizationServerSettings(httpSecurity);
        this.requestMatcher = new AntPathRequestMatcher(authorizationServerSettings.getTokenEndpoint(), HttpMethod.POST.name());
        // 인증과정을 수행항 AuthenticationProvider 을 정의
        List<AuthenticationProvider> authenticationProviders = createDefaultAuthenticationProviders(httpSecurity);
        if (!this.authenticationProviders.isEmpty()) {
            authenticationProviders.addAll(0, this.authenticationProviders);
        }

        this.authenticationProvidersConsumer.accept(authenticationProviders);
        // 정의한 AuthenticationProvider 을 Spring Security 목록에 등록
        authenticationProviders.forEach((authenticationProvider) -> {
            httpSecurity.authenticationProvider((AuthenticationProvider)this.postProcess(authenticationProvider));
        });
    }

    void configure(HttpSecurity httpSecurity) {
        AuthenticationManager authenticationManager = (AuthenticationManager)httpSecurity.getSharedObject(AuthenticationManager.class);
        AuthorizationServerSettings authorizationServerSettings = OAuth2ConfigurerUtils.getAuthorizationServerSettings(httpSecurity);
        // AuthenticationFilter 에 등록할 Filter 생성
        OAuth2TokenEndpointFilter tokenEndpointFilter = new OAuth2TokenEndpointFilter(authenticationManager, authorizationServerSettings.getTokenEndpoint());
        // Http 요청에서 Authentication 인증객체로 컨버팅하는 객체 정의 및 필터에 추가
        List<AuthenticationConverter> authenticationConverters = createDefaultAuthenticationConverters();
        if (!this.accessTokenRequestConverters.isEmpty()) {
            authenticationConverters.addAll(0, this.accessTokenRequestConverters);
        }
        this.accessTokenRequestConvertersConsumer.accept(authenticationConverters);
        tokenEndpointFilter.setAuthenticationConverter(new DelegatingAuthenticationConverter(authenticationConverters));
        if (this.accessTokenResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationSuccessHandler(this.accessTokenResponseHandler);
        }

        if (this.errorResponseHandler != null) {
            tokenEndpointFilter.setAuthenticationFailureHandler(this.errorResponseHandler);
        }
        // 정의한 Filter 를 AuthenticationFilter 목록에 등록
        httpSecurity.addFilterAfter((Filter)this.postProcess(tokenEndpointFilter), AuthorizationFilter.class);
    }
}
```

`init` 메서드에서 호출하는 `createDefaultAuthenticationProviders` 메서드를 보면 `Core Module` 에서 등록한 객체들을 가져와 `AuthenticationProvider` 인증과정을 객체를 생성한다.  

```java
// o.sf.sec.oauth2.server.authorization.config.annotation.web.configurers.OAuth2TokenEndpointConfigurer
private static List<AuthenticationProvider> createDefaultAuthenticationProviders(HttpSecurity httpSecurity) {
  List<AuthenticationProvider> authenticationProviders = new ArrayList<>();

  // Core Module 객체를 가져오기
  OAuth2AuthorizationService authorizationService = OAuth2ConfigurerUtils.getAuthorizationService(httpSecurity);
  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = OAuth2ConfigurerUtils.getTokenGenerator(httpSecurity);

  // 인증을 수행하는 AuthenticationProvider 생성
  OAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider =
      new OAuth2AuthorizationCodeAuthenticationProvider(authorizationService, tokenGenerator);
  SessionRegistry sessionRegistry = httpSecurity.getSharedObject(SessionRegistry.class);
  if (sessionRegistry != null) {
    authorizationCodeAuthenticationProvider.setSessionRegistry(sessionRegistry);
  }
  OAuth2RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider =
      new OAuth2RefreshTokenAuthenticationProvider(authorizationService, tokenGenerator);
  OAuth2ClientCredentialsAuthenticationProvider clientCredentialsAuthenticationProvider =
      new OAuth2ClientCredentialsAuthenticationProvider(authorizationService, tokenGenerator);
  OAuth2DeviceCodeAuthenticationProvider deviceCodeAuthenticationProvider =
      new OAuth2DeviceCodeAuthenticationProvider(authorizationService, tokenGenerator);
  authenticationProviders.add(authorizationCodeAuthenticationProvider);
  authenticationProviders.add(refreshTokenAuthenticationProvider);
  authenticationProviders.add(clientCredentialsAuthenticationProvider);
  authenticationProviders.add(deviceCodeAuthenticationProvider);
  return authenticationProviders;
}
```

#### userinfo 반환값  

- `OAuth 2.0` 의 경우 사용자정보(userinfo)를 가져와 `OAuth2User` 를 만들고 이를 `Authentication` 인증객체로 사용한다.  
- naver 의 경우 `userinfo` 요청 URL 은 `https://openapi.naver.com/v1/nid/me` 이며, 일종의 `Resource Server` 역학을 한다.  
- `Spring Authoriztion Server` 의 경우 `OIDC` 프로토콜을 사용하며 `userinfo` 를 채우는 2가지 방법이 있다.  
  - JWT 형태로 `access token` 을 발급하기에 해당 JWT 에 사용자 정보 채워넣기.  
  - `/userinfo` 처리 코드에서 값 추가해서 반환하기.  
- `Spring Authoriztion Server` 에선 `OidcUserInfoEndpointFilter` 에서 `/userinfo` 를 처리하며 아래 URL 을 통해 `userinfo` 를 반환하는 객체를 커스텀할 수 있다.  
  - <https://docs.spring.io/spring-authorization-server/reference/guides/how-to-userinfo.html>
  - 이미 `id token` 안에 관련 데이터가 다 들어가 있어 `id token` 을 사용해 `userinfo` 를 반환하는 형태이다.  
- 만약 `Spring Authorization Server` 에서 `OAuth 2.0` 도 지원할 예정이라면 이를 위한 `userinfo` 요청 URL 을 별도로 구성하고 처리로직도 별도로 구성해야 한다.  

```java
@Service
@RequiredArgsConstructor
public class CustomOidcUserInfoService {

    private final AuthUserService authUserService;

    public OidcUserInfo loadUser(String uname) {
        AuthUserEntity entity = authUserService.findByUname(uname).orElseThrow();
        return OidcUserInfo.builder()
            .subject(uname)
            .name(uname)
            .nickname(entity.getNickname())
            .email(entity.getEmail())
            .updatedAt(entity.getUpdatedate().format(DateTimeFormatter.ISO_DATE))
            .claim("uid", entity.getUid().toString())
            .claim("role", entity.getRole())
            .claim("ragdate", entity.getRegdate().format(DateTimeFormatter.ISO_DATE))
            .build();
    }
}
```

`id token, access token` 이 위에서 설정한 claims 가 설정될 수 있도록 `OAuth2TokenGenerator` 를 커스텀.  

```java
@Bean
public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer(CustomOidcUserInfoService userInfoService) {
    return (OAuth2TokenClaimsContext context) -> {
        context.getClaims();
        OidcUserInfo userInfo = userInfoService.loadUser(context.getPrincipal().getName());
        context.getClaims().claims(claims -> claims.putAll(userInfo.getClaims()));
    };
}

@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer(CustomOidcUserInfoService userInfoService) {
    return (JwtEncodingContext context) -> {
        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            OidcUserInfo userInfo = userInfoService.loadUser(context.getPrincipal().getName());
            context.getClaims().claims(claims -> claims.putAll(userInfo.getClaims()));
        }
    };
}

@Bean
public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JwtEncoder jwtEncoder,
                                                        OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer,
                                                        OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer) {
    JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
    jwtGenerator.setJwtCustomizer(idTokenCustomizer);
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
    accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer);
    OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    return new DelegatingOAuth2TokenGenerator(
        jwtGenerator, // id_token
        accessTokenGenerator, // access_token
        refreshTokenGenerator // refresh_token
    );
}
```

### Spring Authorization Server 인증과정  


1. **Client Identifier & Redirection URI**
   - `Resource Client` 에서 아래 url 로 OAuth 인증 요청을 보내게된다.  
   - <http://authorization-server/oauth2/authorize?response_type=code&...>(1)
2. **User authenticates** 
   - `LoginUrlAuthenticationEntryPoint`  
     - 인증정보가 없기때문에 `Spring Security` 기본 설정에 의해 로그인 페이지로 이동하게 된다.  
     - 로그인 완료 후 `url(1)` 에 대한 처리를 이어서 수행한다.  
3. **Authorization Code**
   - `OAuth2AuthorizationEndpointFilter` 에서 `url(1)` 을 처리.  
     - 로그인 사용자 `consent` 확인 후 없다면 `consent` 동의페이지로 `redirect` 하도록 `OAuth2AuthorizationConsentAuthenticationToken` 타입의 `Authentication` 인증객체 생성.  
     - 로그인 사용자 `consent` 확인 후 있다면 `Resource Client` 가 등록한 `redirectUrl` 로 `Authorization Code` 와 함께 `redirect` 하도록 `OAuth2AuthorizationCodeRequestAuthenticationToken` 타입의 `Authorization` 인증객체 생성.  
   - 로그인 완료된 사용자는 `Resource Client` 가 등록한 아래 url 로 `Authorization Code` 와 함께 redirect 된다.  
   - <http://resource-client/login/oauth2/code/oauth-client-redirect?code=EvsIF...>(2)  
4. **Authorization Code & Redirection URI**
   - `url(2)` 로 redirect 된 `Resource Client` 는 전달받은 `Authorization Code` 를 사용해 `access token` 요청, 아래 `url(3)` 으로.  
   - <http://authorization-server/oauth2/token>(3)  
5. **Access Token**  
   - `OAuth2ClientAuthenticationFilter` 에서 `url(3)` 요청 처리. 올바른 요청인 검증한다.  
     - `ClientSecretAuthenticationProvider` 
       - `CLIENT_SECRET_BASIC, CLIENT_SECRET_POST` 인증 요청일 경우 사용 `Provider`.  
       - `Authorization Code, Redirection URL, Resource Client Secret` 이 기존에 등록된 `RegisteredClient` 와 일치하는지 검증.  
     - `PublicClientAuthenticationProvider` 
       - `NONE` 인증 요청일 경우 사용 `Provider`, secret 키를 사용하지 않는 `Public Client` 일 경우 사용.  
       - `code_challenge`, `code_verifier` 검증.  
       - `Public Client` 의 경우 `refresh token` 은 반환하지 않는다.  
     - `Provider` 는 `OAuth2ClientAuthenticationToken` 타입의 `Authentiaction` 객체 생성.  
   - `OAuth2TokenEndpointFilter` 에서 `url(3)` 요청을 이어서 처리. `access token` 을 생성한다.  
     - `Access Token, Refresh Token` 등이 포함된 `OAuth2AccessTokenAuthenticationToken` 생성 및 response 에 write.  
6. **ID Token**  
   - `NimbusJwkSetEndpointFilter` 에서 `/oauth2/jwks` 요청 처리.  
     - `access token, id token` 모두 `Authorization Server` 의 비밀키로 서명되어 `/oauth2/jwks` 에서 출력되는 공개키로 인증가능하다.  
   - `OidcUserInfoEndpointFilter` 에서 `/userinfo` 요청 처리.  
     - `OidcUserInfoAuthenticationProvider` 에서 사용자의 token 유효성 확인(DB, InMemory), 
       - `DefaultOidcUserInfoMapper` 를 통해 `scope` 확인 후 필요한 데이터만 반환.  

> 인증, 인가에 필요한 각종 도메인을 CRUD 하기 위한 API 를 개발하는건 피곤한 일이다.  
> Spring Auth Server 를 통해 직접 구축할 수 도 있지만 Keycloak 과 같은 오픈소스를 사용하는 것도 좋은 방법이다.  

## 인증과정 중 요청/응답 값  

`Resource Client` 가 `Authorization Server` 에게 인증요청하기 위해 생성하는 redirect url 은 아래와 같다.  

```sh
http://authorization-server/oauth2/authorize?response_type=code&
 client_id=oauth-demo-client-id&
 scope=openid%20profile%20email&
 redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect&
 state=jtkQ2aipINvyG...& # CSRF 공격을 방지하기 위한 임의의 값
 nonce=glNhNNX2m5yuwu_.. # ID 토큰 replay 공격을 방지하기 위한 임의의 값
```

만약 Resource Client 가 Public Client 일 경우 code_challenge code_verifier 를 위한 매개변수가 추가된다.  

```sh
http://authorization-server/oauth2/authorize?response_type=code&
 client_id=oauth-demo-client-id&
 scope=openid%20profile%20email&
 redirect_uri=http://127.0.0.1:8080/login/oauth2/code/oauth-client-redirect&
 state=eDh9FTFZ34...&
 nonce=vamfPXjJNX...&
 code_challenge=uZ7U1Sn2sf4Yjomlz9V4sTRF4g3JAKyvJysJ5blkKu0&
 code_challenge_method=S256
```

> `Resource Client`에선 `Authorization Server` 로 redirect 될때 `state` 값을 세션에 저장해놓고,  
> 로그인 완료 후 `Resource Client` 로 다시 `redirect` 될때 세션에 저장되어있는 `state` 값을 비교해서 일치하는지 확인한다,

위 `/oauth2/authorize` url 을 통해 로그인페이지로 이동, 로그인을 수행한다.  

Authorization Server 에선 사용자의 consent 수행여부를 확인하고, consent 가 처리되어 있지 않다면 consent 페이지로 redirect 시킨다.  

`Authorization Server` 에서 제공한 `Login page` 에서 로그인 수행, `Consent Page` 에서 동의처리 수행.  

```sh
# consent 페이지로 redirect 할 수조
http://authorization-server/oauth2/consent?client_id=oauth-demo-client-id&
 scope=openid%20profile&
 state=9hy4RG-nb9ldssBvsKuI4YcEsYlLUvphUS9z4vcWahI
```

consent 가 이미 되어있거나 consent 처리를 완료한 사용자는 Resource Client 가 등록한 redirect url 로 `Authorization Code` 와 함께 redirect 된다.  

```sh
# Resource Client 가 등록한 redirectUrl 로 redirect 할 주소
http://resource-client/login/oauth2/code/oauth-client-redirect?
 code=EvsIFciLUuK_HYD3...&
 state=XAFdONvLhAYe7DO...
```

`Resource Client` 는 전달받은 `Authorization Code` 를 사용해 `access token` 을 요청한다.  
`NONE` 타입으로 요청하기에 `code_verifier` 를 설정해서 요청한다.  

```sh
# Spring Authorization Server 요청
POST http://authorization-server/oauth2/token
# HEADER
Authorization: Basic b2F1dGgtZGVtby1jbGllbnQtaWQ6c2VjcmV0 #(Resource Client Secret 을 base64 로 변환)
# BODY
grant_type=authorization_code
code=bwibdMroYP9i...
redirect_uri=http://resource-client/login/oauth2/code/oauth-client-redirect
client_id=oauth-demo-client-id
code_verifier=Wzv5cdcfWYxm9...
```

응답값은 아래와 같다. `NONE` 타입이다 보니 `refresh token` 을 전달하지 않는다.  
`access_token` 과 더불어 `OIDC` 의 `id_token` 도 JWT 형태로 같이 반환된다.  

```js
{
  "access_token": "eyJraWQ...",
  "scope": "openid profile email",
  "id_token": "eyJraWQ...",
  "token_type": "Bearer",
  "expires_in": 299
}
```

`Spring Authorization Server` 에서 전달한 `access_token, id_token` 의 base64 헤더는 아래와 같다.  

```js
{
  "sub": "admin",
  "aud": "oauth-demo-client-id",
  "azp": "oauth-demo-client-id",
  "auth_time": 1729760759,
  "iss": "http://authorization-server",
  "exp": 1729762968,
  "iat": 1729761168,
  "nonce": "BV2Ik4qWdCn-BdL-9hANoGiq9FyFZH9wrgQonYGWA-I",
  "jti": "8fc50133-fa41-4ced-9f84-757169364203",
  "sid": "XssxnxS8ItPN4NNFHSkZOgLiUPwAmln9d41wD6W7SKM"
}
```

응답된 `Response Body` json 값은 `OAuth2AccessTokenAuthenticationToken` 타입의 인증객체로 사용된다.  

```java
package org.springframework.security.oauth2.server.authorization.authentication;

// OAuth2AuthorizationCodeAuthenticationProvider::authenticate
return new OAuth2AccessTokenAuthenticationToken(
    registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
/* 
{
    "access_token": "eyJraWQ...",
    "refresh_token": "PhFV77...",
    "scope": "openid profile email",
    "id_token": "eyJraWQiOi...",
    "token_type": "Bearer",
    "expires_in": 299
}
```

아래는 naver `OAuth 2.0` 에서 `access token` 을 요청할 때 사용하는 `HTTP Request` 형식 
`CLIENT_SECRET_BASIC` 요청하기에 `HTTP Header` 에 `Authorization: Basic {base64 secret}` 값을 설정해서 요청한다.    

```sh
# Naver Authorization Server 요청
POST https://nid.naver.com/oauth2.0/token
# HEADER
Authorization: Basic VGtJQX... #(Resource Client Secret 을 base64 로 변환)
# Body
grant_type=authorization_code
code=XeI8R9...
redirect_uri=http://127.0.0.1:8080/login/oauth2/code/naver-oauth-redirect
# https://nid.naver.com/oauth2.0/token 응답 json body
# {
#     "access_token": "AAAANyN...",
#     "refresh_token": "ey7ipDq9...",
#     "token_type": "bearer",
#     "expires_in": "3600"
# }
# naver 의 token response 에는 scope 가 없어 DB 에 scope 가 누락되어 저장된다
```

`userinfo` 요청할 때 사용하는 `HTTP Request` 형식

```sh
# Authorization Server 에 userinfo 요청
GET http://authorization-server/userinfo
# HEADER
Accept=application/json
Authorization="Bearer eyJraWQi..."
# 응답 json body
# {
#   sub=admin, 
#   aud=[oauth-demo-client-id], 
#   nbf=2024-10-28T15:51:50Z, 
#   scope=[openid, profile, email], 
#   iss=http://authorization-server, 
#   exp=2024-10-28T15:56:50Z, 
#   iat=2024-10-28T15:51:50Z, 
#   jti=72c3e846-e12b-4eef-a648-3ee9889d3d38, 
#   nickname=admin_nickname, 
#   phone_number=010-1111-2222, 
#   birthdate=1998-02-03, 
#   gender=female
# },
```

<!-- 
## SSL 적용  

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

크롬기반 브라우저는 정책상 유효하지 않은 인증서는 사용할 수 없어 파이어폭스같은 브라우저로 테스트하길 권장  -->

## Spring Boot Resource Server  

이전에는 `Spring Authorization Server` 에서 사용자 인증 및 조회까지 수행하였는데,  
사용자의 상세정보는 별도의 서버에서 운영하는 경우가 많다. naver 와 kakao 의 경우에도 인증서버와 `userinfo` 조회서버가 다르다.  

- naver
  - authorization_url: <https://nid.naver.com/oauth2.0/authorize>
  - userinfo_url: <https://openapi.naver.com/v1/nid/me>
- kakao
  - authorization_endpoint: <https://kauth.kakao.com/oauth/authorize>,
  - userinfo_endpoint: <https://kapi.kakao.com/v1/oidc/userinfo>,

`userinfo` 용 서버를 `spring-boot-starter-oauth2-resource-server` 를 통해 설정 가능하다.  

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
}
```

`userinfo` 뿐만 아니라 `scope` 설정대로 사용자의 데이터를 다루는 서버를 `Resource Server` 로 구현한다.  

### Authorization Server userinfo 변경

기본 설정이었던 `userinfo` url 을 `Resource Server` 를 바라보도록 `Authorization Server` 의 `Spring Security` 설정에서 수정.  

```java
// Authorization Server 의 security config
authz
    .registeredClientRepository(registeredClientRepository)
    .authorizationService(authorizationService)
    .authorizationConsentService(authorizationConsentService)
    .tokenGenerator(tokenGenerator)
    .oidc(Customizer.withDefaults())    // Initialize `OidcConfigurer`
    .oidc(oidc -> oidc.providerConfigurationEndpoint(config -> config
        .providerConfigurationCustomizer(customizer -> customizer
            .userInfoEndpoint("http://resource-server/userinfo")
        )
    ))
```

### Resource Server Security Config  

Resource Server 의 Spring Security Config 를 설정.  

```java
@Bean
public SecurityFilterChain resourceServer(HttpSecurity http) throws Exception {
    // 공개키 조회 및 jwtDecoder 등록
    http.oauth2ResourceServer(resourceServer -> resourceServer
            .jwt(jwtConfigurer -> jwtConfigurer.jwkSetUri("http://authorization-server/oauth2/jwks")));

    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/userinfo").hasAuthority("SCOPE_profile") // 해당 권한이 있어야 /userinfo 접근 가능
            .anyRequest().authenticated()
    );
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
}
```

위와같이 설정하면 아래와 같은 `Spring Security Filter` 들이 등록된다.  

```java
@PostConstruct
public void printSecurityFilters() {
    List<SecurityFilterChain> filterChains = filterChainProxy.getFilterChains();
    for (SecurityFilterChain chain : filterChains) {
        List<Filter> filters = chain.getFilters();
        log.info("Security Filter Chain: " + chain);
        for (Filter filter : filters) {
            log.info(filter.getClass().toString());
        }
    }
}
/*
Security Filter Chain: DefaultSecurityFilterChain
class org.springframework.security.web.session.DisableEncodeUrlFilter
class org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter
class org.springframework.security.web.context.SecurityContextHolderFilter
class org.springframework.security.web.header.HeaderWriterFilter
class org.springframework.web.filter.CorsFilter
class org.springframework.security.web.csrf.CsrfFilter
class org.springframework.security.web.authentication.logout.LogoutFilter
class org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
class org.springframework.security.web.savedrequest.RequestCacheAwareFilter
class org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter
class org.springframework.security.web.authentication.AnonymousAuthenticationFilter
class org.springframework.security.web.session.SessionManagementFilter
class org.springframework.security.web.access.ExceptionTranslationFilter
class org.springframework.security.web.access.intercept.AuthorizationFilter
*/
```

### BearerTokenAuthenticationFilter

등록된 여러 Filter 객체중 `BearerTokenAuthenticationFilter` 에서 `access token` 검증을 수행한다.  

- `HTTP Header` 에 저장된 `access token` 을 검증한다.  
  - `Authorization: Bearer {access_token}` 
- `Spring Authorizaion Server` 의 `/oauth/jwks` 에서 얻은 공개키를 사용해 검증한다.  
  - 내부적으론 `JwtAuthenticationProvider` 를 사용하여 공개키가 등록된 `jwtDecoder` 로 `access token` 을 검증한다.  

```java
package org.springframework.security.oauth2.server.resource.web.authentication;

public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    ...
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = token = this.bearerTokenResolver.resolve(request);
        ...
        BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
        // JwtAuthenticationProvider
        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authenticationResult);
        // security context 에 저장
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
        }
        filterChain.doFilter(request, response);
    }
}
```

## Spring OAuth Opaque Token

> **Opaque Token**
> 불투명 토큰, Authorization Server 에서 식별자 용도로 사용하는 랜덤 문자열 형태의 토큰  
> `OAuth 2.0` 프로토콜에서 사용한다.  

JWT 의 경우 `Authorization Server` 의 공개키를 통해 `Resource Server` 에서도 자체적으로 검증이 가능하기 때문에 추가적인 `Authorization Server` 의 개입을 필요로 하지 않는다.  
하지만 만료시간으로 토큰 유효성을 검증할 경우 `Authorization Server` 에서 토큰을 비활성화(수동삭제 등) 해도 `Resource Server` 에서 반영되지 않는다.  

`Opaque Token` 는 매 요청마다 토큰을 `Authorization Server` 로부터 검증받아야 하기 때문에 실시간 유효성 검증이 가능하다.  

`Spring Authorization Server` 에서도 `OAuth 2.0` 기반의 `Opaque Token` 을 지원한다.  

### Authorization Server 에서의 Opaque  

`Opqeue Token` 의 경우 유효성을 확인하기 위해선 항상 `Authorization Server` 에 요청이 필요하다.  
`Authorization Server` 의 개입이 각 API 마다 발생하지만 즉각적인 토큰의 유효성 체크가 가능하다.  

```java
// token claim 에 추가적인 정보를 삽입하기 위해 사용,  
// email 정보가 Resource Server 에 존재하거나 email 자체가 필요하지 않다면 추가할필요없다.  
@Bean
public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer() {
    return context -> {
        AuthUserEntity authUser = authUserService.findByUname(context.getPrincipal().getName()).orElseThrow();
        context.getClaims().claims(claims -> claims.put("email", authUser.getEmail()));
    };
}

@Bean
public OAuth2TokenGenerator<OAuth2Token> tokenGenerator(OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer) {
    OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
    accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer);
    OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    return new DelegatingOAuth2TokenGenerator(
            accessTokenGenerator, // access_token
            refreshTokenGenerator // refresh_token
    );
}

// oauth core module 등록
@Bean
@Order(1)
public SecurityFilterChain authorizationServerSecurityFilterChain(RegisteredClientRepository registeredClientRepository,
                                                                  OAuth2AuthorizationService authorizationService,
                                                                  OAuth2AuthorizationConsentService authorizationConsentService,
                                                                  OAuth2TokenGenerator<OAuth2Token> tokenGenerator,
                                                                  HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
    OAuth2AuthorizationServerConfigurer authz = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);


    authz
            .registeredClientRepository(registeredClientRepository)
            .authorizationService(authorizationService)
            .authorizationConsentService(authorizationConsentService)
            .tokenGenerator(tokenGenerator)
            .authorizationEndpoint(configurer -> configurer.consentPage("/oauth2/consent"))
    ;
    http
            .securityMatchers(matchers -> matchers.requestMatchers(antMatcher("/oauth2/**"), authz.getEndpointsMatcher()))
            .exceptionHandling((exceptions) -> exceptions
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
    ;
    http.addFilterAfter(new PrintResponseBodyFilter(), UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
/* 
org.springframework.security.web.session.DisableEncodeUrlFilter
org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter
org.springframework.security.web.context.SecurityContextHolderFilter
org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.AuthorizationServerContextFilter
org.springframework.security.web.header.HeaderWriterFilter
org.springframework.web.filter.CorsFilter
org.springframework.security.web.csrf.CsrfFilter
org.springframework.security.web.authentication.logout.LogoutFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationServerMetadataEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2DeviceVerificationEndpointFilter
org.springframework.security.oauth2.server.authorization.web.NimbusJwkSetEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2ClientAuthenticationFilter
com.example.auth.server.demo.config.PrintResponseBodyFilter
org.springframework.security.web.savedrequest.RequestCacheAwareFilter
org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter
org.springframework.security.web.authentication.AnonymousAuthenticationFilter
org.springframework.security.web.access.ExceptionTranslationFilter
org.springframework.security.web.access.intercept.AuthorizationFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2TokenIntrospectionEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2TokenRevocationEndpointFilter
org.springframework.security.oauth2.server.authorization.web.OAuth2DeviceAuthorizationEndpointFilter
*/
```

`Resour Server` 가 `access token(Opaque Token)` 과 함께 `userinfo` 요청을 받으면 `Opaque Token` 의 유효성을 `Authorization Server` 로부터 검증받아야 한다.  

- `OAuth2TokenIntrospectionEndpointFilter` 에서 `/oauth2/introspect` url 을 처리하며 로 `Opaque Token` 형태의 `access token` 검증을 수행한다.  
- `OAuth2TokenIntrospectionAuthenticationProvider` 에서 `Authorization Entity` 를 DB 에서 조회, 해당 토큰이 유효한지 확인 후 `OAuth2TokenIntrospectionAuthenticationToken` 을 반환한다.  

요청 및 응답은 아래와 같다.  

```sh
# Authorization Server 에 Opaque Token 검증 요청
POST http://authorization-server/oauth2/introspect
# HEADER
Accept=application/json
Authorization="Bearer b2F1dGgtZGVtby1jbGllbnQtaWQ6c2VjcmV0" # Opaque Token 형태의 access token
# 응답 json body
# {
#     "active": true,
#     "sub": "admin",
#     "aud": ["oauth-demo-client-id"],
#     "nbf": 1730341373,
#     "scope": "profile email",
#     "iss": "http://",
#     "exp": 1730341673,
#     "iat": 1730341373,
#     "jti": "3177b005-9652-4784-9cd6-4f45674930ad",
#     "client_id": "oauth-demo-client-id",
#     "token_type": "Bearer"
# }
```

### Resource Server 에서 Opaque

`Resource Client` 가 요청한 `HTTP Request` 와 `access token(Opaque Token)` 이 유효한지 확인하기 위해 `Resource Server` 의 `/oauth2/introspect` 를 요청한다.  

`Resource Server` 의 `Spring Security` 를 `Opaque Token` 을 사용하도록 변경  
`JwtAuthenticationProvider` 대신 `OpaqueTokenAuthenticationProvider` 를 사용하게 된다.  

```java
@Bean
@Profile("opaque")
public SecurityFilterChain opaqueResourceServer(HttpSecurity http) throws Exception {
    http.oauth2ResourceServer(resourceServer -> resourceServer
        .opaqueToken(configurer -> configurer
            .introspectionUri("http:///authorization-server/oauth2/introspect")
            .introspectionClientCredentials("oauth-demo-client-id", "secret")
        )
    );
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/userinfo").hasAuthority("SCOPE_profile") // 해당 권한이 있어야 /userinfo 접근 가능
        .anyRequest().authenticated()
    );
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
}
```

- `BearerTokenAuthenticationFilter` 에서 `access token` 이 유효한 `Opaque Token` 인지 검증한다.  
- `OpaqueTokenAuthenticationProvider` 에서 `/oauth2/introspect` 를 호출하여 Authorization Server 로부터 토큰 검증 후 `BearerTokenAuthentication` 을 반환한다.  

```java
@GetMapping("/userinfo")
public Map<String, Object> getUserinfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String uname = authentication.getName();
    AbstractOAuth2TokenAuthenticationToken<?> oAuth2TokenAuthenticationToken = (AbstractOAuth2TokenAuthenticationToken<?>) authentication;
    Map<String, Object> response = new HashMap<>();
    if (oAuth2TokenAuthenticationToken.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("SCOPE_profile"))) {
        AuthUserDetailEntity entity = service.getUserById(uname);
        response.putAll(oAuth2TokenAuthenticationToken.getTokenAttributes());
        response.put("nickname", entity.getNickname());
        response.put("phone_number", entity.getPhone());
        response.put("birthdate", entity.getBirth());
        response.put("gender", entity.getGender());
    }
    return response;
}
```

## 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/oauth-demo>