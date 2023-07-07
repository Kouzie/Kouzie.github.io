---
title:  "Spring Boot - Test!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Rest doc

스프링 부트 테스트를 통해 만들어진 API를 테스트 하고 문서화 가능

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

### 진행하면서 난감했던 상황들  

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
## Spring Boot Test

> <https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing>

```groovy
plugins {
    id 'org.springframework.boot' version '2.4.1'
    id 'io.spring.dependency-management' version '1.0.10.RELEASE'
    id 'java'
}

...

dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

![springboot_test_1](/assets/springboot/springboot_test_1.png)  

위와 같은 `dependency` 들이 포함되어 있다.  

* `JUnit 5`: The de-facto standard for unit testing Java applications.  
* `Spring Test & Spring Boot Test`: Utilities and integration test support for Spring Boot applications.  
* `AssertJ`: A fluent assertion library.  
* `Hamcrest`: A library of matcher objects (also known as constraints or predicates).  
* `Mockito`: A Java mocking framework.  
* `JSONassert`: An assertion library for JSON.  
* `JsonPath`: XPath for JSON.  

아래와 같은 다양한 테스트 어노테이션 제공한다.  

* `@SpringBootTest`
* `@JsonTest`  
* `@RestTest`  
* `@WebMvcTest`  
* `@DataJpaTest`  
* `@RestClientTest`  
* `@BeforeAll`  
* `@BeforeEach`  

### @SpringBootTest

`end-to-end` 테스트를 위한 어노테이션  
모든 빈 객체를 `Spring Context` 에 등록하고 실제 서버 port 까지 지정해서 실행시킨다.  

보통 `end-to-end` 테스트의 경우 외부 접근 URL 부터 테스트하는 경우가많음으로 `@AutoConfigureMockMvc` 를 함께 사용한다.  

```java
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerEndTests extends MysqlTestContainer {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository repository;

    @Autowired
    MockMvc mockMvc;

    @Test
    void patch_user_end_to_end() throws Exception {
        User user = createUser();
        user = repository.save(user);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/{userId}", user.getUserId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        UserDto sutUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);

        Assertions.assertEquals(user.getUserId(), sutUser.getUserId());
        Assertions.assertEquals(user.getName(), sutUser.getName());
    }
}
```

### @WebMvcTest

**Web Layer** 에서 사용되는 Bean 들만을 등록하여 테스트.  
`Present Layer` 관련 컴포넌트만 스캔한다.  

* `Intercepter`  
* `WebMvcConfigurer`  
* `HandlerMethodArgumentResolver`  
* ...
* `@Controller`  
* `@RestController`  
* `@ControllerAdvice`  
* `@Filter`  

그외의 `[Service, Repository]` 는 등록되지 않음으로 `Mock` 으로 대체하거나 직접 준비해야 한다.  

```java
@WebMvcTest(UserController.class) // 테스트할 클래스
public class UserControllerTests {

    // controller 에 필요한 서비스는 Mock 으로 대체
    @MockBean UserService service;
    @MockBean EmailGateway emailGateway;

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void rename_user() throws Exception {
        String email = "test@test.com";
        UserType type = UserType.CUSTOMER;
        String name = "testName";
        Company company = new Company("comp.com", 10);
        User user = new User(email, type, name, company);
        // 반환할 값 사전 지정
        Mockito.when(service.findById(1l)).thenReturn(user);
        Mockito.when(service.save(user)).thenReturn(user);
        UserPatchRequestDto requestDto = new UserPatchRequestDto();
        requestDto.setRename("rename");

        MvcResult result = mockMvc.perform(patch("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("result: " + result.getResponse().getContentAsString());

        UserDto sut = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

        Assertions.assertEquals(user.getName(), sut.getName());
    }
}
```

### @DataJpaTest

JPA 에서 사용하는 `[Entity, Repository]` 클래스 테스트.  
일부 `Service` 클래스의 경우 `Repository` 만 의존함으로 `@DataJpaTest` 만으로 테스트 진행이 가능하다.  

```java
@DataJpaTest
public class StoreServiceTests {

    @Autowired
    StoreRepository repository;

    static StoreService service;

    @BeforeAll
    static void beforeAll(@Autowired StoreRepository repository) {
        service = new StoreService(repository);
    }

    @Test
    void save_test() {
        Store store = new Store("test store");
        store.addInventory(1l, 10);
        store.addInventory(2l, 10);
        store = repository.save(store);
        Long storeId = store.getStoreId();

        Store sut = service.findById(storeId);
        Assertions.assertEquals(storeId, sut.getStoreId());
    }
}
```

#### @AutoConfigureTestDatabase

`@DataJpaTest` 같은 DB 통합 테스트를 진행할 때, `In-Memory DB` 를 사용할지, 테스트 DB 에 접근할지를 결정해야 한다.  
`@AutoConfigureTestDatabase` 어노테이션을 통해 지정할 수 있다.  

```java
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
```

기본적으로 아래와 같은 종류의 `In-Memory DB`(`[H2, DERBY, HSQLDB]`) 들의 설정을 가지고 있다가 사용한다.  
추가된 `Dependency` 에 따라 어떤 `In-Memory DB` `connection enum` 을 사용할지 결정된다.  

```java
public enum EmbeddedDatabaseConnection {
    NONE(null, null, null, (url) -> false),
    H2(EmbeddedDatabaseType.H2, DatabaseDriver.H2.getDriverClassName(), "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", (url) -> url.contains(":h2:mem")),
    DERBY(EmbeddedDatabaseType.DERBY, DatabaseDriver.DERBY.getDriverClassName(), "jdbc:derby:memory:%s;create=true", (url) -> true),
    ...
}
```

**replace** 속성  

* `ANY`: 설정된 `DataSource` 를 `In-Memory DB` `DataSource` 로 교체.  
* `AUTO_CONFIGURED`: `auto-configured` 로 설정된 `DataSource` 만 교체, 만약 직접 `DataSource` Bean 을 설정했다면 교체되지 않음  
* `NONE`: `Datasource` 교체하지 않음  

DB 다형성을 완벽히 지원하는 JPA 어플리케이션이라면 `ANY` 로 설정해서 테스트해도 큰 문제가 없다.  

**connection** 속성  

`EmbeddedDatabaseConnection` 에서 어떤 `In-Memory DB` 를 사용할건지 선택.  

### @RestClientTest

지정한 클래스의 `RestTemplateBuilder`, `RestTemplate` 등의 `Bean` 을 요청 시 내부 지정해둔 `MockRestServiceServer` 의 고정된 반환값을 사용하도록 설정  
실제로 외부에 Rest 요청을 수행하지 않고 고정된 문자열을 반환한다.  

```java
@RestClientTest(RestAdaptorImpl.class)
public class RestAdaptorTests {

    // dummy server
    @Autowired
    MockRestServiceServer server;

    @Autowired
    RestAdaptor adaptor;

    @Test
    void dummy_test_find_post_by_id() throws IOException {
        Long id = 1l;
        ClassPathResource cpr = new ClassPathResource("adaptor/RestUserPost.json");
        byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());

        // dummy response
        server
                .expect(MockRestRequestMatchers.requestTo("https://jsonplaceholder.typicode.com/posts/" + id))
                .andRespond(MockRestResponseCreators.withSuccess(bdata, MediaType.APPLICATION_JSON));

        RestUserPost sut = adaptor.findPostById(1l);

        Assertions.assertEquals(1, sut.getId());
    }
}
```

### @BeforeAll, @AfterAll, @BeforeEach, @AfterEach

**@BeforeAll, @AfterAll**

테스트 메서드들이 실행 전/실행 후 한번만 실행되는 `static` 메서드  
`@Autowired` 어노테이션으로 `static` 메서드 안에서 의존성을 주입받을 수 있다.  

```java
@DataJpaTest
public class CustomerServiceTests {
    private static CustomerService service;
    
    @BeforeAll
    static void beforeAll(@Autowired CustomerRepository repository) {
        service = new CustomerService(repository);
    }

    @AfterAll
    static void afterAll() {
      System.out.println("test finished");
    }

    @Test
    void create_and_find_customer() {
        Customer customer = new Customer("hello customer");
        customer = service.save(customer);
        Long customerId = customer.getCustomerId();

        Customer sut = service.findById(customerId);
        Assertions.assertEquals(customerId, sut.getCustomerId());
    }
}
```

테스트 매서드들이 모두 실행 된 후 한번만 실행되는 `static` 메서드  

**@BeforeEach, @AfterEach**

각 테스트 메서드 호출마다 실행되는 일반 메서드, `[@BeforeAll, @AfterAll]` 와 마찬가지로 초기화/마무리 할 때 사용하지만  
테스트 메서드들의 순서를 보장하지 못하기에 어떻게 초기화가 이루어질지 예측할 수 없다.  
테스트 메서드에 `@Order` 어노테이션으로 순서를 강제지정 할 수 있지만 테스트 속도가 느려짐으로 잘 사용하지 않는다.  

### @JsonTest

json serialize, deserialize 를 테스트 할 수 있는 어노테이션  
아래 객체들이 bean 으로 등록되어 테스트할 수 있다.  

* `@JsonComponent`  
* `ObjectMapper`  
* `JacksonTest`  
* `GsonTester`  
* `BasicJsonTest`  

```java
@JsonTest
public class UserJsonTests {

    @Autowired
    private JacksonTester<User> json;

    @BeforeAll
    static void beforeAll(@Autowired ObjectMapper objectMapper) {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    void json_serialize_user() throws IOException {
        Company company = new Company("mycorp.com", 1);
        User user = new User("user@gmail.com", UserType.CUSTOMER, "demo-user", company);
        JsonContent<User> userJson = json.write(user);
        System.out.println(userJson.getJson());
        File file = new ClassPathResource("domain/user/User.json").getFile();

        User sut = json.read(file).getObject();

        Assertions.assertEquals(user.getName(), sut.getName());
        Assertions.assertEquals(user.getEmail(), sut.getEmail());
        Assertions.assertEquals(user.getType(), sut.getType());
        Assertions.assertEquals(user.getCompany().getNumberOfEmployees(), sut.getCompany().getNumberOfEmployees());
    }
}
```

`@JsonTest` 에선 기본생성된 `ObjectMapper` 를 테스트한다.  

만약 직접만든 `ObjectMapper` 를 테스트하고 싶다면 아래와 같이 `@ContextConfiguration` 를 사용해 `ObjectMapper` 가 Bean 으로 등록되어 있는 클래스 지정한다.  

### @ContextConfiguration, @Import

`test` 시에 부족한 Bean 을 `Spring Container` 에 등록할 때 아래 2가지 어노테이션을 사용 가능하다.  

* `@ContextConfiguration`  
* `@Import`  

```java
// org.springframework.test.context.ContextConfiguration
// org.springframework.context.annotation.Import;

@ContextConfiguration(classes = {WebConfig.class})
@Import({WebConfig.class})
```

`@Import` 는 `ComponentScan` 과 비슷한 기능을 하는 어노테이션,  
테스트에서 두 어노테이션 모두 정상동작 하지만 `Spring Test Layer` 에서 사용하는 `@ContextConfiguration` 사용을 권장한다.  

## Mockito

> java 모킹 프레임워크  
> <https://site.mockito.org/>

`Mockito.mock` 함수를 사용해 `Mock` 객체를 쉽게 생성 가능하다.  

```java
public class PurchaseServiceTests {

    @Test
    void purchase_normal_test() {
        long productId = 23;
        int quantity = 10;
        long storeId = 1;
        Store store = createStore(productId, quantity);
        // mock 으로 사용할 Test Doubles
        StoreRepository storeRepository = Mockito.mock(StoreRepository.class);
        PurchaseService service = new PurchaseService(storeRepository);
        // 일부 메서드 상태값 반환 지정
        Mockito.when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        Mockito.when(storeRepository.save(ArgumentMatchers.any(Store.class))).thenReturn(store);

        Store sut = service.purchase(storeId, productId, 3);
        
        // 상태 및 행동 검증
        Assertions.assertEquals(quantity - 3, sut.getInventory(productId));
        Mockito.verify(storeRepository, Mockito.times(1)).findById(storeId);
        Mockito.verify(storeRepository, Mockito.times(1)).save(ArgumentMatchers.any(Store.class));
    }


    private Store createStore(long productId, int quantity) {
        Store store = new Store("mock-store");
        store.addInventory(productId, quantity);
        return store;
    }
}
```

`Mockito.spy` 함수를 사용해 `Spy` 객체를 쉽게 생성 가능하다.  

```java
@Test
public void purchase() {
    // spy 로 사용할 Test Doubles
    PurchaseService service = new PurchaseService(null) {
        @Override
        public Store purchase(long storeId, long productId, int quantity) {
            Store store = new Store("spy-store");
            store.addInventory(productId, 20 - quantity);
            return store;
        }
    };
    CustomerController controller = new CustomerController(Mockito.spy(service));
    int productId = 23;
    int quantity = 7;

    Store sut = controller.purchase(0, productId, quantity);
    
    Assertions.assertEquals("spy-store", sut.getName());
    Assertions.assertEquals(13, sut.getInventory(productId));
}
```

아래 익스텐더를 추가하면 `Mockito` 관련된 어노테이션을 사용해 `Mock` 객체를 제어할 수 있다.  

```java
@ExtendWith(MockitoExtension.class)
```

* `@Mock`  
* `@Spy`  
* `@InjectMock`  
* `@MockBean`  

### @Mock, @Spy

`Mockito` 프레임워크에서 제공하는 어노테이션  

`[Mockito.mock, Mockito.spy]` 메서드를 사용하지 않고 `[@Mock, @Spy]` 어노테이션을 통해 객체 생성이 가능하다.  

```java
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceTests {

    @Mock
    StoreRepository storeRepository;

    @Test
    void purchase_normal_test() {
        long productId = 23;
        int quantity = 10;
        long storeId = 1;
        Store store = createStore(productId, quantity);
        // StoreRepository storeRepository = Mockito.mock(StoreRepository.class);
        PurchaseService service = new PurchaseService(storeRepository);
        Mockito.when(storeRepository.findById(storeId))
                .thenReturn(Optional.of(store));
        Mockito.when(storeRepository.save(ArgumentMatchers.any(Store.class)))
                .thenReturn(store);
        Store sut = service.purchase(storeId, productId, 3);
        Assertions.assertEquals(quantity - 3, sut.getInventory(productId));
        Mockito.verify(storeRepository, Mockito.times(1)).findById(storeId);
        Mockito.verify(storeRepository, Mockito.times(1)).save(ArgumentMatchers.any(Store.class));
    }

    private Store createStore(long productId, int quantity) {
        Store store = new Store("mock-store");
        store.addInventory(productId, quantity);
        return store;
    }
}
```

```java
@ExtendWith(MockitoExtension.class)
public class CustomerControllerTests {

    @Spy
    PurchaseService service;
    
    @Test
    public void purchase() {
        int productId = 23;
        int quantity = 7;
        Mockito.doAnswer(invocation -> {
            Store store = new Store("spy-store");
            store.addInventory(productId, 20 - quantity);
            return store;
        }).when(service).purchase(0, productId, quantity);
        CustomerController controller = new CustomerController(service);

        Store sut = controller.purchase(0, productId, quantity);

        Assertions.assertEquals("spy-store", sut.getName());
        Assertions.assertEquals(13, sut.getInventory(productId));
    }
}
```

`@Spy` 은 함수 블럭으로 구현할 뿐 `@Mock` 과 크게 다를 건 없다.  

### @InjectMocks

`@Mock` 이 붙은 `Mock` 객체를 `@InjectMocks` 이 붙은 객체에 주입시킬 수 있다.  

```java
@ExtendWith(MockitoExtension.class)
public class StoreServiceTests {

    @Mock
    StoreRepository repository;

    @InjectMocks
    StoreService service;

    @Test
    void save_and_find_store() {
        Store store1 = createStore("test store1");
        Store store2 = createStore("test store2");
        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(store1);
        Mockito.when(repository.findById(2l)).thenReturn(Optional.of(store2));
        Store sut1 = service.save(store1);
        Store sut2 = service.findById(2l);
        Assertions.assertEquals("test store1", sut1.getName());
        Assertions.assertEquals("test store2", sut2.getName());
    }

    private Store createStore(String storeName) {
        Store store = new Store(storeName);
        store.addInventory(1l, 10);
        store.addInventory(2l, 10);
        return store;
    }
}
```

### @MockBean

> `org.springframework.boot.test.mock.mockito` 패키지에 속해있는 어노테이션  

`Mock` 으로 생성된 객체를 `Spring Context` 에 삽입해야할 경우 사용  
`@WebMvcTest` 의 경우 주로 `Web Layer` 이후 `Mock Service Bean` 들을 `Spring Context` 에 넣어야 하다보니 `@MockBean` 을 사용한다.  

```java
@WebMvcTest(UserController.class)
public class UserControllerTests {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    UserService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void rename_user() throws Exception {
        User user = createUser();
        when(service.findById(1l)).thenReturn(user);
        when(service.save(user)).thenReturn(user);
        UserPatchRequestDto requestDto = new UserPatchRequestDto();
        requestDto.setRename("rename");

        MvcResult sut = mockMvc.perform(patch("/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
            )
            .andExpect(status().isOk())
            .andReturn();

        UserDto sutUser = objectMapper.readValue(sut.getResponse().getContentAsString(), UserDto.class);
        Assertions.assertEquals(user.getName(), sutUser.getName());
    }
}
```

## Testcontainers

> <https://testcontainers.com/>  
> <https://testcontainers.com/guides/getting-started-with-testcontainers-for-java/>

`H2DB` 는 훌륭한 테스트용 `In-Memort DB` 이지만 `native query` 는 테스트가 부정확할 수 있다.  
또한 DB 외의 기타 `프로세스 외부 의존성`의 경우 테스트를 Mock 으로 대체하거나 테스트를 포기해야할 경우가 있는데, 이때 도움을 주는게 컨테이너 기반 테스트이다.  

실제 테스트용 Docker 컨테이너를 실행시켜 테스트를 지원한다.  

```java
@Testcontainers
@ActiveProfiles("test")
public class MysqlTestContainer {

    static MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8") // MySQLContainer 객체 생성
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
    }


    @BeforeAll
    static void beforeAll() {
        MY_SQL_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        MY_SQL_CONTAINER.stop();
    }
}
```

만약 직접 `DataSource` 를 구축해야 한다면 아래와 같이 `@ContextConfiguration` 으로 `DatsSource` 빈을 등록한다.  

```java
@Testcontainers
@ContextConfiguration(classes = MysqlTestContainer.TestDataSourceConfiguration.class)
public class MysqlTestContainer {


    static MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:8") // MySQLContainer 객체 생성
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpassword");

    @BeforeAll
    static void beforeAll() {
        MY_SQL_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        MY_SQL_CONTAINER.stop();
    }

    @TestConfiguration
    public static class TestDataSourceConfiguration {
        @Bean
        public DataSource dataSource() {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
            dataSourceBuilder.url(MY_SQL_CONTAINER.getJdbcUrl());
            dataSourceBuilder.username(MY_SQL_CONTAINER.getUsername());
            dataSourceBuilder.password(MY_SQL_CONTAINER.getPassword());
            return dataSourceBuilder.build();
        }
    }
}
```

### 로그 출력  

`[org.testcontainers, com.github.dockerjava]` 두개 패키지 클래스의 DEBUG 로그들을 제거하고 싶다면  
`src/test/resources/logback.xml` 파일을 아래와 같이 구성  

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder><pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n</pattern></encoder>
    </appender>

    <root level="info"><appender-ref ref="STDOUT"/></root>

    <logger name="org.testcontainers" level="INFO"/>
    <logger name="com.github.dockerjava" level="WARN"/>
</configuration>
```

## 데모코드  

> <https://github.com/Kouzie/unit-test-demo>  
