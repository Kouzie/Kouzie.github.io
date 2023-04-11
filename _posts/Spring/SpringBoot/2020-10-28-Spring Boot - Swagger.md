---
title:  "Spring Boot - Swagger!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Swagger

> <https://swagger.io/>
> <https://swagger.io/docs/specification/basic-structure/>

`OpenAPI` 사양을 기반으로 구성된 REST API 설명 형식  
`yaml`, `json` 형식으로 구성 가능함  

각종 웹프레임워크에서 자동으로 OpenAPI 파일을 생성하는 라이브러리를 제공한다.  


## Spring Fox Swagger

> <https://github.com/springfox/springfox>
> <https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api>

`Spring fox` 기반의 swagger 프로젝트는 2020 년을 이후로 업데이트되고 있지 않으며  
`Spring Boot 2.6` 이후 버전에서는 연동 이슈가 올라오고 있다.  

```groovy
dependencies {
    implementation 'io.springfox:springfox-boot-starter:3.0.0'
}
```

```java

@Profile({"dev", "stg"})
@Configuration
public class SwaggerConfig {

    private String applicationName = "swagger-demo";
    private String version = "0.0.1";

    private static final String HEADER_AUTH = "Authorization";
    private static final String BASE_URL = "localhost";

    @Bean
    public Docket api() {
        Server serverLocal = new Server("local", "http://localhost:8080", "local profile", Collections.emptyList(), Collections.emptyList());
        Server serverProfile = new Server("dev", "http://kouzie.com", "dev profile", Collections.emptyList(), Collections.emptyList());
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(getApiInfo())
                .servers(serverLocal, serverProfile)
                .consumes(Set.of("application/json;charset=UTF-8")) //Request Content-Type
                .produces(Set.of("application/json;charset=UTF-8")) //Response Content-Type .pathMapping("/")
                .forCodeGeneration(true)
                .ignoredParameterTypes(Date.class)
                .directModelSubstitute(java.time.LocalDate.class, Date.class)
                .directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
                .directModelSubstitute(java.time.LocalDateTime.class, Date.class)
                .securitySchemes(Collections.singletonList(apiKey()))
                .securityContexts(Collections.singletonList(securityContext()))
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.kouzie.app." + applicationName + ".controller")) //Swagger API 문서로 만들기 원하는 basePackage 경로
                .paths(PathSelectors.any()) // URL 경로를 지정하여 해당 URL에 해당하는 요청만 Swagger API 문서로 만듭
                .build();
    }

    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title(String.format("kouzie API SERVICE(%s)", applicationName))
                .description(String.format("kouzie API SERVICE(%s)", applicationName))
                .contact(new Contact(String.format("kouzie API SERVICE(%s)", applicationName),
                        "https://localhost:8080",
                        "kouzie@kouzie.com"))
                .version(version)
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey(HEADER_AUTH, HEADER_AUTH, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(defaultAuth()))
                .forPaths(PathSelectors.any())
                .build();
    }

    SecurityReference defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return new SecurityReference(HEADER_AUTH, authorizationScopes);
    }
}
```

간단히 사용법만 코드로 올리고 지속적으로 업데이트가 진행중인 `Spring Doc` 라이브러리를 알아볼 에정

## Spring Doc

아래 소개페이지에서 설명하듯이 `Migrating from SpringFox`  

> 홈: <https://springdoc.org/>  
> git: <https://github.com/springdoc/springdoc-openapi>  
> Web MVC 데모: <https://github.com/springdoc/springdoc-openapi-demos/tree/master/springdoc-openapi-spring-boot-2-webmvc>  
> 참고: <https://www.baeldung.com/spring-rest-openapi-documentation>

별도의 `configuration` 파일 없이 설정몇개로 구성 가능  

```conf
# https://springdoc.org/#properties
# swagger-ui custom path, static path
springdoc.swagger-ui.path=/static/swagger-ui.html
# 실제 open api json 포멧 데이터는 해당 url 로부터 가져옴
springdoc.api-docs.path=/api-docs
# disable api-docs
springdoc.api-docs.enabled=true
```

`http://127.0.0.1:8080/static/swagger-ui.html` url 로 접속해서 생성된 REST API 에 대한 Open API 생성된것 확인  

defualt 설정만으로 Controller 에 적용된 REST API 들을 보여주지만  
아래와 같이 `OpenAPI` 객체를 빈으로 등록하면 커스텀 가능  

```java
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("demo swagger title")
                .version("demo swagger version")
                .description("demo swagger description");
        return new OpenAPI()
                .components(new Components())
                .info(info)
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                ;
    }
}
```

## 데모코드

> <https://github.com/Kouzie/spring-boot-demo/tree/main/swagger-demo>