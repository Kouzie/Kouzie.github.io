---
title:  "Spring Boot - MapStruct!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## MapStruct

> <https://mapstruct.org/>

최근 jhipster 를 사용하면서 자동으로 생성되는 스프링 부트의 구조 + 처음보는 라이브러리들을 학습중이다.  

유명한 Mapper 라이브러리로 `ModelMapper`와 `MapStruct` 가 있으며  
한국과 일본을 제외하곤 전 세계적으로 `MapStruct` 의 사용량이 많다.  


![springboot_mapstruct1](/assets/springboot/springboot_mapstruct1.png)  

성능 또한 `MapStruct` 가 더 우세하다고 한다.  

> 출처  
> <https://yonguri.tistory.com/125>  
> <https://better-dev.netlify.app/java/2020/10/26/compare_objectmapper/>

### Usage


`MapStruct` 역시 Lombok 과 같이 `annotation` 을 기반으로 `annotation processor` 가 구현체를 `auto generate` 한다.  


```groovy
dependencies {
    ...
    implementation 'org.mapstruct:mapstruct:1.4.2.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.4.2.Final'
}
```

아래와 같은 `Car` 객체를 매퍼를 사용해 `CarDto` 로 변경해야 할 경우 

```java
public class Car {
 
    private String make;
    private int numberOfSeats;
    private CarType type;
 
    //constructor, getters, setters etc.
}
public class CarDto {
 
    private String make;
    private int seatCount;
    private String type;
 
    //constructor, getters, setters etc.
}
```

`MapStruct` 라이브러리가 `annotation processor` 를 사용해 구현체를 만들수 있도록 `@Mapper` 어노테이션과 함께 `interface` 작성을 통해 정의해햐 한다.  

```java
@Mapper
public interface CarMapper {
 
    CarMapper INSTANCE = Mappers.getMapper( CarMapper.class );
 
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto carToCarDto(Car car);
}
```

매핑될 필드의 이름이 서로 다르더라도 `@Mapping` 어노테이션으로 처리 가능하다.  

```java
@Test
public void shouldMapCarToDto() {
    //given
    Car car = new Car( "Morris", 5, CarType.SEDAN );
 
    //when
    CarDto carDto = CarMapper.INSTANCE.carToCarDto( car );
 
    //then
    assertThat( carDto ).isNotNull();
    assertThat( carDto.getMake() ).isEqualTo( "Morris" );
    assertThat( carDto.getSeatCount() ).isEqualTo( 5 );
    assertThat( carDto.getType() ).isEqualTo( "SEDAN" );
}
```

### EntityMapper  

`jhipster` 를 사용하면 좀더 효율적으로 스프링부트에서 `MapStruct` 작성을 할 수 있도록 여러 인터페이스를 자동 작성해주는데  
Car 를 예제로 들면 아래처럼 수정해서 사용할 수 있다.  

```java
// D: dto, E: entity
public interface EntityMapper<D, E> {
    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);
    // null value 는 dto -> entity 업데이트시에 적용하지 않는다.  
    // @MappingTarget 으로 데이터 매핑 방향 설정 
}
```

`Entity`와 `Dto` 마다 인터페이스를 재정의해야 한다는 단점이 있지만  
필드명이 똑같을경우 재정의 필요 없이 `EntityMapper` 상속만으로 대부분의 함수는 자동 생성되어 개발자의 실수를 줄여준다.  

`Car` 클래스와 같이 `Mapping` 할 필드명이 많이 다를경우 아래처럼 모든 함수를 재정의해주어야 한다.  

```java
@Mapper(componentModel = "spring", uses = {})
public interface CarMapper extends EntityMapper<CarDto, Car> {

    @Mapping(source = "seatCount", target = "numberOfSeats")
    @Mapping(source = "carId", target = "id", ignore = true)
    Car toEntity(CarDto carDto);

    @Mapping(source = "id", target = "carId")
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto toDto(Car car);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "carId", target = "id", ignore = true)
    @Mapping(source = "seatCount", target = "numberOfSeats")
    void partialUpdate(@MappingTarget Car entity, CarDto dto);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        Car car = new Car();
        car.setId(id);
        return car;
    }
}
```

매핑 필드명이 달라 재정의해야할 경우  
`EntityMapper` 에 정의했던 어노테이션은 덮어씌어짐으로 모두 다시 작성해주어야 한다.  

`componentModel = "spring"` 속성을 통해 `Spring` 프로젝트에서 `bean` 으로 등록해준다.  

빌드가 완료되면 `build` 디렉토리에 아래와 같은 `Mapper` 구현체가 생성된다.  

```java
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2021-06-16T16:00:13+0900",
    comments = "version: 1.4.2.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-7.0.2.jar, environment: Java 11.0.10 (GraalVM Community)"
)
@Component
public class CarMapperImpl implements CarMapper {

    @Override
    public List<Car> toEntity(List<CarDto> dtoList) {
        if ( dtoList == null ) {
            return null;
        }

        List<Car> list = new ArrayList<Car>( dtoList.size() );
        for ( CarDto carDto : dtoList ) {
            list.add( toEntity( carDto ) );
        }

        return list;
    }

    @Override
    public List<CarDto> toDto(List<Car> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<CarDto> list = new ArrayList<CarDto>( entityList.size() );
        for ( Car car : entityList ) {
            list.add( toDto( car ) );
        }

        return list;
    }

    @Override
    public Car toEntity(CarDto carDto) {
        if ( carDto == null ) {
            return null;
        }

        Car car = new Car();

        car.setNumberOfSeats( carDto.getSeatCount() );
        car.setMake( carDto.getMake() );
        if ( carDto.getType() != null ) {
            car.setType( Enum.valueOf( CarType.class, carDto.getType() ) );
        }

        return car;
    }

    @Override
    public CarDto toDto(Car car) {
        if ( car == null ) {
            return null;
        }

        CarDto carDto = new CarDto();

        carDto.setCarId( car.getId() );
        carDto.setSeatCount( car.getNumberOfSeats() );
        carDto.setMake( car.getMake() );
        if ( car.getType() != null ) {
            carDto.setType( car.getType().name() );
        }

        return carDto;
    }

    @Override
    public void partialUpdate(Car entity, CarDto dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getSeatCount() != null ) {
            entity.setNumberOfSeats( dto.getSeatCount() );
        }
        if ( dto.getMake() != null ) {
            entity.setMake( dto.getMake() );
        }
        if ( dto.getType() != null ) {
            entity.setType( Enum.valueOf( CarType.class, dto.getType() ) );
        }
    }
}
```

컨트롤러나 서비스 클래스에서 어래처럼 사용할 수 있다.  

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/car")
public class CarController {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @PostMapping
    CarDto setCar(@RequestBody CarDto carDto) {
        Car car = carMapper.toEntity(carDto);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @PatchMapping("{id}")
    CarDto patchCar(@PathVariable Long id, @RequestBody CarDto carDto) {
        Car car = carRepository.findById(id).get();
        carMapper.partialUpdate(car, carDto);
        car = carRepository.save(car);
        return carMapper.toDto(car);
    }

    @GetMapping("/{id}")
    public CarDto getCar(@PathVariable Long id) {
        Car car = carRepository.findById(id).get();
        return carMapper.toDto(car);
    }
}
```

### Mapstruct With Setter  

`DDD` 개발 구조에선 `Entity` 클래스에 `Setter` 함수 정의를 피한다.  
하지만 `MapStruct` 의 `partialUpdate` 메서드는 `Entity` 에 `Setter` 메서드가 정의되어 있어야 한다.  

결론은 `Setter` 메서드없이 `partialUpdate` 하는 방법은 없다.  
만약 `DDD` 구조에서 `MapStruct` 를 사용하고 싶다면 `partialUpdate` 메서드는 정의하지 말아야 한다.  

> 어차피 `DDD` 특성상 `update` 함수를 애그리거트 클래스에서 정의함으로 큰 부담을 아닐것이다.  

## @Valid - 칼럼에 대한 각종 제약조건 설정  

`@Column`어노테이션 만으로 제약조건 지정이 부족하다면 아래 어노테이션들을 사용한다.  

어노테이션|설명|사용예
|---|---|---|
`@AssertFalse` | 값이 무조건 `false` 여야함 | @AssertFalse<br>boolean isUnsupported;
`@AssertTrue` | 값이 무조건 true여야함 | @AssertTrue<br>boolean isActive;
`@DecimalMax` | 10진수 최대값이 n값 이하 실수여야함 | @DecimalMax("30.00")<br>BigDecimal discount;
`@DecimalMin` | 10진수 최소값이 n값 이하 실수여야함 | @DecimalMin("5.00")<br>BigDecimal discount;
`@Digits` | 정수와 실수 자리수 지정 | @Digits(integer=6, fraction=2)<br>BigDecimal price;
`@Future` | 해당날짜가 현재보다 미래여야함 | @Future<br>Date eventDate;
`@Past` | 해당 날짜가 현재보다 과거여야함 | @Past<br>Date birthday;
`@Max` | n값 이하여야함 | @Max(10)<br>int quantity;
`@Min` | n값 이상이어야함 | @Min(5)<br>int quantity;
`@NotNull` | 값이 null일수 없음 | @NotNull<br>String username;
`@Null` | 값이 null이어아햠 | @Null<br>String unusedString;
`@Pattern` | 정규식을 만족해야함 | @Pattern(regexp="(d{3})d{3}-d{4}")<br>String phoneNumber;
`@Size` | 최소크기, 최대크기를 지정 | @Size(min=2, max=240)<br>String briefMessage;

> <http://www.thejavageek.com/2014/05/24/jpa-constraints/>


## 샘플 프로젝트  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/mapper-demo>