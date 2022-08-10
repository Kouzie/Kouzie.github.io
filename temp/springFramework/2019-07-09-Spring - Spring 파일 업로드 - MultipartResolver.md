---
title:  "Spring - 파일 업로드 - MultipartResolver!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - spring-framework
---

## 파일 업로드 - MultipartResolver

파일 업로드를 위해 jsp에선 `cos.jar` 파일을 사용하였는데 Spring에선 `CommonsMultipartResolver`클래스를 제공한다.  



먼저 위 클래스 사용을 위한 `pom.xml`에 `dependency`추가와 `bean`객체를 만들어보자.  

```xml
<!-- 파일 업로드 -->
<dependency>
  <groupId>commons-fileupload</groupId>
  <artifactId>commons-fileupload</artifactId>
  <version>1.2</version>
</dependency>

<dependency>
  <groupId>commons-io</groupId>
  <artifactId>commons-io</artifactId>
  <version>1.2</version>
</dependency>
```

스프링 컨트롤러가 파일을 받을 수 있도록 해주는 `multipartResolver` 빈 객체를 스프링에 등록한다.  
```xml
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
  <property name="maxUploadSize" value="-1"></property>
</bean>
```

파일 업로드에 대한 설정이 모두 끝났으면 다음과 같이 컨트롤러 메서드가 파일을 받을 수 있게 파라미터명을 매칭해주면 된다.

```java
@RequestMapping(value="join", method = RequestMethod.POST)
	public String joinPOST(
      @RequestParam("image") MultipartFile multipartFile,
      ...
      ...
			HttpServletRequest request,
			RedirectAttributes rttr) throws Exception
{
  ...
  ...
}
```

### 서버에서 getRealPath 설정

실제 파일이 저장되고 이미지 파일을 서버에서 출력, 저장하려면 서버가 접근할 수 있는 `ContextPath` 아래에 있는 폴더 어딘가에 파일들이 저장되어 있어야 한다.  
서버가 pc에 정착해서 변동될 일이 없다면 다음과 같이 spring 빈 객체로 서버의 이미지 저장 시스템 경로 문자열을 등록하고 `@Autowired`로 받아 사용해도 된다.  
```xml
<beans:bean id="uploadPath" class="java.lang.String">
	<beans:constructor-arg value="C:\Server\..."></beans:constructor-arg>
</beans:bean>
```

하지만 서버 위치가 변동되거나 PC가 변경되면 안될수 도 있고 Git과 같은 협업툴을 사용하게 되면 번거로운 일이 발생함으로 `ServletContext`객체의 `getRealPath` 메서드를 사용해 서버가 실행될 때 동적으로 시스템 경로를 얻어오는 것 이 좋다.  

위의 `joinPOST()` 메서드처럼 매개변수로 `HttpServletRequest` 객체를 받으면 `request.getRealPath`를 사용할 수 있지만 모든 파일 처리 함수에 `HttpServletRequest`를 매개변수로 잡는 일은 매우 번거롭다.  

`ServletContext`에도 `getRealPath` 메서드가 있음으로 이를 사용해 컨트롤러 초기화시 `realPath` 문자열을 생성하도록 설정하자.  

```java
@Controller
@RequestMapping("/survey/*")
public class SurveyController {
	@Autowired
	ServletContext c;
	
	private String realPath;
  
  ...
  ...

	@PostConstruct
	public void initController() {
		this.realPath = c.getRealPath("/resources/img");
  }
  
  ...
  ...
}

```

컨트롤러가 초기화 될 때 `ServletConext`를 자동으로 의존관계에 두고 `@PostConstruct` 어노테이션을 통해 컨트롤러가 메모리상에 올라갈 때 realPath를 초기화 하도록 설정하자.  


### 실제 파일 저장

```java
public void addMember(MemberVO member, MultipartFile multipartFile, String realPath) throws Exception {
  try {
    if (multipartFile.getSize() != 0) {
      String uuidname = UUID.randomUUID().toString()+".jpg";
      byte[] bytes = multipartFile.getBytes();
      File file = new File(realPath, uuidname);
      FileCopyUtils.copy(bytes, file);
      member.setImage(uuidname);
    }
  }
  catch (IOException e) {
    logger.warn("file upload fail....");
    throw e;
  }
  ...
}
```

### 파일 `mimeType` 알아내기  

> https://stackoverflow.com/questions/51438/getting-a-files-mime-type-in-java

```java
@Test
void getMimeTypeFromFile() throws IOException {
  File file = new File("src/main/resources/static/mybanner.txt");
  InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
  String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
  System.out.println("mimeType: " + mimeType);
  //mimeType: image/jpeg
}
```

사진 파일을 txt 로 변경후 실행시에도 이미지 파일임을 알려준다.  