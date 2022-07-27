---
title:  "Spring Boot - 스프링 부트 직렬화!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true
toc_sticky: true

---

## ObjectMapper

스프링에는 `@RestController` 등을 사용하여 HTTP Body 안의 문자열을 클래스로 변환 할때 `ObjectMapper` 를 사용한다.

`Jackson` 라이브러리가 기본으로 `Spring boot starter web` 에 포함되어 있기 때문.  
해당 라이브러리에선 `ObjectMapper` Bean 뿐 아니라 각종 Json 관련 어노테이션 등도 제공한다.  

스프링부트에서 `ObjectMapper` 의 커스텀을 properties 설정을 제공한다.  
`spring.jackson.###` 

예를들어 json 네이밍 전략을 SNAKE 나 CAMEL 로 변경하고 싶다면 아래와같이 설정 가능  

```conf
spring.jackson.property-naming-strategy=SNAKE_CASE
spring.jackson.property-naming-strategy=CAMEL_CASE
```

아예 Java 코도를 사용해 별도로 `ObjectMapper` 를 `@Bean` 어노테이션으로 등록하면  
기존 생성되는 `ObjectMapper` 를 대체할 수 도 있다.  

```java

public static class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
    @Override
    public ZonedDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return ZonedDateTime.parse(jsonParser.getText(), formatter);
    }
}

@Bean
public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // for zone date time
    SimpleModule module = new JavaTimeModule()
            .addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(formatter))
            .addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
    objectMapper.registerModule(module);
    objectMapper.setTimeZone(TimeZone.getTimeZone(zoneId));

    // for Date class
    objectMapper.setDateFormat(dateFormat);
    // WRITE_DATES_AS_TIMESTAMPS JSON에서 날짜를 문자열로 표시
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.setVisibility(objectMapper
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL) // null 필드는 변환 X
                    .setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE) // 네이밍 전략
                    .getVisibilityChecker()
    );
    // UnrecognizedPropertyException 처리, 알수없는 필드 처리 X
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // InvalidDefinitionException, Object 클래스는 빈 객체로 변환(필드가 없는 객체도 변환하도록)
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return objectMapper;
}
```


### Json Annotation

Jackson 라이브러리에 수많은 json 관련 어노테이션이 있지만  
자주 사용하는것 몇가지만 소개하고자 한다.  

#### @JsonCreator, @JsonValue

```java
@Getter
@RequiredArgsConstructor
public enum SearchType {
    USERNAME("username"),
    TYPE("type"),
    TITLE("title");

    private final String key;

    @JsonCreator
    public static SearchType forValue(String key) {
        for (SearchType value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return key;
    }
}
```

`enum` 클래스의 경우 변환할때 `enum` 인스턴스가 가지고있는 name 을 value 로 사용하게 되는데
`key` 값을 가지고 `enum` 인스턴스를 `serialize`, `deserialize` 하고 싶다면 위와같이 사용  

`ObjectMapper` 의 `writeValueAsString` 를 호출하게 되면 `@JsonValue` 에 설정된 문자열이 반환된다.  

#### @JsonProperty

```java
@Getter
@Setter
@ToString
public class Board {
    private String title;
    private String username;
    @JsonProperty("Type")
    private String type;
}
```

일반적으로 Json 필드의 문자열은 네이밍 전략을 철저히 따라 생성해야 하지만  
특정 필드 하나만 특별한 사유로 다른 네이밍 전략을 사용하고 싶을때  

위와같이 `@JsonProperty` 어노테이션을 사용하면 좋다.  

기본 `value` 필드 외에도 `index` 를 지정하거나 `access` 를 통해 접근제한, `required` 옵션을 통해 `validation` 구성도 가능하니 상세 구현페이지 참고 

#### @JsonNaming

```java
@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public static class WeatherData {
    private String weather;
    private String weatherCd;
    private String rainy;
    private String maxTemperature;
    private String minTemperature;
}
```

Jackson 에서 제공하는 `ObjectMapper` 의 기본 네이밍 전략은 `lower camel case` 이다.  
만약 특정 클래스의 네이밍 전략만 `snake case` 로 변경하고 싶다면 `@JsonNaming` 어노테이션 사용하면 된다.    

## Date Time String

시간값을 표현하는 방식은 time format 을 통해 결정된다.  
현재 날짜를 표기하는 문자열로 전 세계 공통으로 사용하는 format 은 **ISO 8601** 이다.  

> 위키: ISO 8601 은 날짜 및 시간 관련 데이터 의 전 세계적인 교환 및 통신을 다루는 국제 표준 입니다.  
큰 시간 기간(일반적으로 1년)이 왼쪽에 배치되고 연속적으로 작은 각 기간이 이전 기간의 오른쪽에 배치되도록 정렬  
특정 의미가 할당된 특정 컴퓨터 문자(예: "-", ":", "+", "T", "W", "Z")의 조합으로 작성된 문자열을 뜻합니다.
![springboot_serialize1](/assets/springboot/springboot_serialize1.png)  
<https://en.wikipedia.org/wiki/ISO_8601>

이중 ISO 8601 의 가장 많이 사용하는 format 문자열은 Local Time Format 을 표현하는 `yyyy-MM-dd'T'HH:mm:ss` 이다.  

스프링에서 `DateTimeFormatter.ISO_DATE_TIME` 를 formatter 로 사용하면 된다.  

```java
public static final DateTimeFormatter ISO_DATE_TIME;
static {
    ISO_DATE_TIME = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME) // yyyy-MM-dd'T'HH:mm:ss.SSS
            .optionalStart()
            .appendOffsetId() // 'Z', "+HH:MM:ss"
            .optionalStart()
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId() // zone id
            .appendLiteral(']')
            .toFormatter(ResolverStyle.STRICT, IsoChronology.INSTANCE);
}
```

각종 optional 조건들을 사용하여 zone 관련된 내용이 들어가 웬만한 문자열을 날자 객체로 desieralize 하는데에는 문제가 없다.  
하지만 serialize 의 경우 아래와 같이 출력된다.  

```java
DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
ZonedDateTime zonedDateTime = ZonedDateTime.now();
System.out.println(dtf.format(zonedDateTime)); 
// 2022-07-22T00:37:36.368955+09:00[Asia/Seoul]
```

컴팩트한 문자열로 seiralize 하기를 원한다면 `ObjectMapper` 에 deserialize 객체는 별도로 지정하길 권장한다.  

### Time Format String  

`DateTimeFormatter.ISO_DATE_TIME` 와 같이 미리 제공된 Formatter 말고  
직접 time format 문자열을 사용해서 Formatter 를 생성하고 싶다면 날짜를 표현하는 여러가지 문자 및 기호를 알아야 한다.  

> 아래 url 참고  
<https://pro.arcgis.com/en/pro-app/2.8/help/mapping/time/convert-string-or-numeric-time-values-into-data-format.htm>

format 을 Optional 하게 설정하고 싶다면 `[`, `]` 특수문자를 사용,   

`yyyy-MM-dd'T'HH:mm:ss[.SSS]` - 나노초가 있을수도 없을수도 있다

### Zone

여러 국가에서 지원하는 서비스의 Local Time 만 표기하는 것이 아니라  
`Universal Time Coordinated(UTC: 세계 협정시)` 을 지원해야 한다.  

그리니치 표준시라고도 하는데 런던 웰링턴의 그리니치 시계탑을 기준으로 표준시를 결정했기 때문  

Zulu time 이라고도 하는데 군에서 UTC 를 뜻하는 단어이다.  
> ISO 8601 의 특수문자 Z 가 Zulu time 을 뜻한다.  

대표적인 나라 도시의 `UTC Time Zone` 은 아래와 같다.  

```
0:00	GMT/LON(런던)	GMT+0
1:00	PAR(파리)	GMT+1
2:00	CAI/JRS(카이로/예루살렘)	GMT+2
3:00	JED(제다)	GMT+3
3:30	THR(테헤란)	GMT+3.5
4:00	DXB(두바이)	GMT+4
4:30	KBL(카불)	GMT+4.5
5:00	KHI(카라치)	GMT+5
5:30	DEL(델리)	GMT+5.5
6:00	DAC(다카)	GMT+6
6:30	RGN(양곤)	GMT+6.5
7:00	BKK(방콕)	GMT+7
8:00	HKG(홍콩)	GMT+8
9:00	SEL(서울)	GMT+9
9:30	ADL(다윈)	GMT+9.5
10:00	SYD(시드니)	GMT+10
11:00	NOU(누메아)	GMT+11
12:00	WLG(웰링턴)	GMT+12
```

> 더많은 도시의 Time Zone 을 확인하고 싶다면 아래 url 참고
<https://jp.cybozu.help/general/en/admin/list_systemadmin/list_localization/timezone.html>

Time Zone 을 표기하기 위해서 time format 문자열에 Zone 을 표기할 수 있는 문자열을 추가해야 한다.  

`2011-08-12T20:17:46.384Z` - 뒤에 Z(Zulu Time) 특수문자가 붙어서 표준시를 뜻함. 

위 time format 을 표현할 수 있는 Formatter 를 만들고 싶다면 아래 코드 참고  

```java
SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
format.setTimeZone(TimeZone.getTimeZone("UTC"));
```

`'Z'` 는 일반 문자열, 그리고 TimeZone 을 UTC 로 설정해서 Formatter 를 구현하면 된다.  


`2020-09-10T10:58:19+09:00` - UTC+9 를 뜻하며 서울이나 도쿄 등의 도시에서 사용한다.  
`yyyy-MM-dd'T'HH:mm:ssXXX` 를 Formatter 의 format 문자열로 정의하면 된다.  


### @DateTimeFormatter

Request Parameter 에서 문자열을 바로 날짜 객체로 변환하고 싶을때 `@DateTimeFormat` 어노테이션을 사용  

```java
@GetMapping("/board")
List<BaordRequestDto> getBoard(@PathVariable Long deviceId,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime beginDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime endingDate);
```


```java
@Getter
@Setter
public class BaordRequestDto {
    private String ttile;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime beginDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime endingDate;
    private String type;
}
```

설명은 아래와 같이 적혀있다.  

> @DateTimeFormat: The most common ISO Date Time Format yyyy-MM-dd'T'HH:mm:ss.SSSXXX — for example, "2000-10-31T01:30:00.000-05:00".

