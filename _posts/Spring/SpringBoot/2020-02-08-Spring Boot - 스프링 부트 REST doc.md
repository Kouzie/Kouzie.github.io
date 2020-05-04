---
title:  "Spring Boot - 스프링 부트 Rest doc!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true

---

# Rest doc

협업하다 보면 서버간의 API호출은 빈번하게 일어나고 해당 API가 어떤 역할을 하는 API인지 문서화는 필수이다.  

일일이 공유 문서를 생성하여 협업하는 경우도 있지만 프로젝트가 커질수로 관리는 어려워지고 최신화 및 동기화가 힘들어진다.  

`Spring Rest doc`를 사용하면 이런 문제점을 일부 해결해준다.  

> 반드시 아래 튜토리얼을 참고해서 사용해야 한다. junit 버전마다 사용법이 다르다.  
> https://docs.spring.io/spring-restdocs/docs/2.0.4.RELEASE/reference/html5/#getting-started-build-configuration  
> 현재 스프링 버전은 `2.2.4.RELEASE`이기에 `junit5`를 사용한다.  
> `maven`, `gradle` 두가지 빌드환경에서 구축하는 설명

테스트를 위한 간단한 컨트롤러 클래스 작성 

```java
@Controller
@RequestMapping("/product")
public class ProductController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Integer id) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(id);
        productDTO.setName("Spring Boot");
        productDTO.setDesc("Spring");
        productDTO.setQuantity(10);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}

@Getter
@Setter
class ProductDTO {
    private Integer id;
    private String name;
    private String desc;
    private Integer quantity;
}
```

`localhost:8080/product/{id}` url을 호출하면 고정해둔 문자열을 세팅해둔 객체를 json형식으로 반환하는 간단한 컨트롤러 메서드이다.  

그리고 이를 테스트하는 코드를 작성하자 (위의 git url 참고)

```java
@ComponentScan //컴포넌트 빈 객체 포함
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class}) //junit 5 설정
// 서버 실행중에도 테스트 할수 있도록 랜덤포트 사용
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@Transactional //각 테스트 수행후 rollback 처리
public class ProductControllerTest {

    private RestDocumentationResultHandler document;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.document = document(
                "{class-name}/{method-name}",
                preprocessResponse(prettyPrint()) //json 문자열 줄맞춤
        );
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(document)
                .build();
    }

    @Test
    public void getProduct() throws Exception {
        mockMvc.perform(
                get("/product/{id}", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(parameterWithName("id").description("Product's Id")),
                        responseFields(
                                fieldWithPath("id").description("product's Id"),
                                fieldWithPath("name").description("product's name"),
                                fieldWithPath("desc").description("product's desc"),
                                fieldWithPath("quantity").description("product's quantity")
                        )
                ))
                .andExpect(jsonPath("$.name", is(notNullValue())))
                .andExpect(jsonPath("$.desc", is(notNullValue())))
                .andExpect(jsonPath("$.quantity", is(notNullValue())));
    }
}
```

테스트 코드를 실행하고 `target/generated-snippets`에 `rest doc` 로 인해 파일들이 자동생성 되는지 확인  

![restdoc1](/assets/2020/restdoc1.png){: .shadow}  

작성된 파일을 하나의 파일로 합칠수 있도록 `src/main/asciidoc` 아래에 생성된 `rest doc`들의 위치를 참조하는 문서 작성  
Spring Rest Doc 에서 기본문서위치가 `src/main/asciidoc` 임으로 주의!  

해당 문서위치에 `product-controller.adoc` 이름으로 하나 `asciidocv` 파일을 생성하였다  

> 마크다운처럼 `asciidocv` 혁시 문서 작성 양식이 있는데 아래 url 에서 문법을 확인가능하다.  
> https://asciidoctor.org/docs/asciidoc-writers-guide/  
> https://narusas.github.io/2018/03/21/Asciidoc-basic.html


```
product-controller.adoc
= ProductController

== getProduct

include::{snippets}/product-controller-test/get-product/path-parameters.adoc[]
include::{snippets}/product-controller-test/get-product/http-response.adoc[]
include::{snippets}/product-controller-test/get-product/response-fields.adoc[]
include::{snippets}/product-controller-test/get-product/curl-request.adoc[]
include::{snippets}/product-controller-test/get-product/http-request.adoc[]
include::{snippets}/product-controller-test/get-product/httpie-request.adoc[]
include::{snippets}/product-controller-test/get-product/request-body.adoc[]
include::{snippets}/product-controller-test/get-product/response-body.adoc[]
```

대충 모든 생성된 문서들을 포함만 하는 방식으로 작성  
> `Rest doc` 의 장점은 자동생성된 파일들을 간단하게 `include` 라는 오픈블럭으로 쉽게 합칠 수 있는것.  

메이븐으로 페키지를 생성하면 테스트가 진행된후 `rest doc`가 생성되고 `src/main/asciidoc` 에 생성해둔 파일양식대로 하나의 API 문서를 `pom.xml`에 설정해둔 위치에 생성된다.   

이후 jar파일을 사용해 서버 실행 후 `localhost:8080/docs/product-controller.html` 요청

아래처럼 출력되는지 확인  
![restdoc2](/assets/2020/restdoc2.png){: .shadow}  

## 진행하면서 난감했던 상황들  

**각종 파라미터 정의하기**  
사용할때 마다 헷갈리는 request 요청 필드 정의하기  

```java
.andDo(document.document(
  requestHeaders( //헤더 내부의 설명 
    headerWithName("Authorization").description("인증 토큰")
  ),
  pathParameters( //url에 path 파라미터 사용시 설명 
    parameterWithName("id").description("아이디")
  ),
  requestParameters( //?, & 를 사용하 query 파라미터, post방식에서의 form데이터 사용시 설명
    parameterWithName("pageNum").description("페이지번호"),
    parameterWithName("pageSize").description("페이지당 출력수")
  ),
  requestFields( //json 형식의 request body 사용시 설명
    fieldWithPath("name").type(JsonFieldType.STRING).description("사용자 이름"),
    fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일"),
    fieldWithPath("data").type(JsonFieldType.ARRAY).description("전달 데이터 배열형식"),
    fieldWithPath("data.[].id").type(JsonFieldType.NUMBER).description("전달 데이터 아이디"),
    fieldWithPath("data.[].name").type(JsonFieldType.STRING).description("전달 데이터 이름"),
    fieldWithPath("code").type(JsonFieldType.STRING).optional().description("코드, null 일수 도 있음")
  ),
  responseFields( //json 형식의 response body 사용시 
    fieldWithPath("msg").type(JsonFieldType.STRING).description("성공/실패 메세지"),
    fieldWithPath("code").type(JsonFieldType.NUMBER).description("성공/실패 코드")
  )
))
```

MultipartFile 전달시

```java
mvc.perform(fileUpload(url)
  .file(new MockMultipartFile("image", "mythumbnail.jpeg", "image/jpeg", Files.readAllBytes(imageFile.toPath())))
  .param("storeId", store.getId().toString())
  .param("name", "탕수육")
  .param("price", "18000")
  .param("discountPrice", "16000")
  .param("description", "맛있는 눈꽃 탕수육")
  .header("Authorization", "Bearer " + jwtToken))
  .andDo(document.document(
    requestParts(
      partWithName("image").description("메뉴 이미지")
    ),
    requestHeaders(
      headerWithName("Authorization").description("발급받은 jwt 토큰, 앞에 'Bearer ' 문자열 삽입 필수")
    ),
    requestParameters(
      parameterWithName("storeId").description("메뉴 추가할 가게 아이디"),
      parameterWithName("name").description("메뉴 이름"),
      parameterWithName("price").description("가격"),
      parameterWithName("discountPrice").description("할인가"),
      parameterWithName("description").description("메뉴 설명")
    ),
    responseFields(
      fieldWithPath("msg").type(JsonFieldType.STRING).description("성공/실패 메세지"),
      fieldWithPath("code").type(JsonFieldType.NUMBER).description("성공/실패 코드")
    )
  ))
  .andExpect(jsonPath("$.code", is(0)))
  .andExpect(status().isOk())
  .andReturn();
}
```

**`RestDocumentationRequestBuilders` 와 `MockMvcRequestBuilders` 차이**

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
```

`get`, `post`, `put`, `delete` 등의 메서드를 `static import` 로 자주 사용하는데
`pathParameter` 를 `rest doc` 로 설명해야 할때 `MockMvcRequestBuilders` 보다 `RestDocumentationRequestBuilders`를 사용하라고 경고문구가 출력된다.  


**전처리 작업**

```java
@BeforeEach
public void setUp(RestDocumentationContextProvider restDocumentation) {
  this.document = document(
    "{class-name}/{method-name}",
    preprocessRequest(
      // rest doc 에 localhost 가 아닌 정상적인 host url 로 출력되도록 설정, context 설정은 mvc에서 해야한다 
      modifyUris()
        .scheme("https")
        .host("hello.mywebsite.org")
        .removePort(), 
      prettyPrint()), 
    preprocessResponse(prettyPrint()) //request, response 둘다 줄맞춤 출력 
  );
  mvc = MockMvcBuilders
    .webAppContextSetup(context)
    .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 전달되는 request 데이터 내부의 한글 깨짐 방지 
    .apply(springSecurity()) //security 설정도 적용
    .apply(documentationConfiguration(restDocumentation)) //junit5 설정
    .alwaysDo(document)
    .build();
}


// 자동 생성되는 curl 명령에서 context path 도 추가해 편하게 사용하기 위해 설정 
MvcResult result = mvc.perform(get("/mycontext/customer/api/v1/main").contextPath("/mycontext")
  ... //
  ...
)
.andExpect(jsonPath("$.code", is(0)))
.andReturn()
```