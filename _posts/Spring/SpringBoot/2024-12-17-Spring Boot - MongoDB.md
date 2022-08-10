---
title:  "Spring Boot - MongoDB!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## Spring Data Mongo

> <https://docs.spring.io/spring-data/mongodb/reference/index.html>  

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb')
}
```

기본적인 MongoDB 사용을 위한 Bean 등록  

```java
@Slf4j
@Configuration
public class MongoClientConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.host}")
    private String host;
    @Value("${mongodb.username}")
    private String username;
    @Value("${mongodb.password}")
    private String password;
    @Value("${mongodb.database}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        String connection = "mongodb://" + username + ":" + password + "@" + host + ":27017/" + databaseName + "?replicaSet=rs0";
        return MongoClients.create(new ConnectionString(connection)); // MongoDB 연결 URI
    }

    /**
     * Replica Set 에서 동작할 MongoTransactionManager Bean 생성
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * class 필드 제거를 위해 AbstractMongoClientConfiguration 오버라이딩
     * AbstractMongoClientConfiguration 에서 아래 Bean 등록해줌
     *   - MongoClient
     *   - MongoTemplate
     */
    @Override
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory,
                                                       MongoCustomConversions customConversions,
                                                       MongoMappingContext mappingContext) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(databaseFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(customConversions);
        converter.setCodecRegistryProvider(databaseFactory);
        // _class 필드 제거
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }


    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * @Index 어노테이션 사용
     */
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
```

`Spring Data Mongo` 에선 주로 `MongoRepository` 를 구현하여 `MongoDB` 에 접근한다.  

```java
public interface UserDocumentRepository extends MongoRepository<UserDocument, String> {
    // 커스텀 메서드 정의 예시
    UserDocument findByEmail(String email);

    // org.springframework.data.mongodb.repository.Query
    // 특정 조건을 추가한 커스텀 쿼리 예시
    @Query("{ 'username' : ?0, 'email' : ?1 }")
    UserDocument findByUsernameAndEmail(String username, String email);
}

@Getter
@Setter
@Document(collection = "users") // MongoDB의 컬렉션 이름
@NoArgsConstructor
public class UserDocument {
  @Id
  public String id;
  @Indexed(unique = true) // 사용자 계정 인덱스로 설정
  private String username;
  private String email;

  public UserDocument(String username, String email) {
    this.username = username;
    this.email = email;
  }
}
```

## MongoTemplate

`Spring Data Mongo` 에는 `org.mongodb:mongodb-driver-core` 라이브러리도 포함되어 있어 `MongoTemplate` 사용또한 가능하다.  

다음과 같은 상황에선 `MongoTemplate` 을 사용하느게 효과적이다.  

- 동적쿼리
- 집계쿼리(Aggregation)
- 배치쿼리
- upsert

```java
private final MongoTemplate mongoTemplate;

/**
 * username과 email을 동적으로 조건에 따라 조회
 */
public UserDocument getUserByParam(String username, String email) {
    Query query = new Query().addCriteria(Criteria.where("username").is(username));
    // 동적 조건 추가
    if (email != null) {
        query.addCriteria(Criteria.where("email").is(email));
    }
    return mongoTemplate.findOne(query, UserDocument.class);
}
```

### save, insert

```
// 새 문서 삽입
db.collection.insert({ name: "홍길동", age: 30 });
db.collection.save({ name: "이순신", age: 40 });

// id 지정해서 insert, id 가 이미 존재한다면 예외발생
db.collection.insert({ _id: ObjectId("1234"), name: "홍길동", age: 30 });

// id 지정해서 save, id 가 없다면 추가, 있다면 업데이트함.
db.collection.save({ _id: ObjectId("1234"), name: "이순신", age: 41 });
```

동작에는 큰 차이가 없지만 명시적으로 `save`, `insert` 를 구분해서 사용하는것을 권장한다.  

`Spring Data Mongo` 에서 다중삽입의 경우 `saveAll` 함수는 반복문이고 `insert(list)` 는 일괄처리임으로 성능상에도 차이가 있다.  

## 트랜잭션

`MongoDB` 트랜잭션은 `Oplog` 를 지원하는 **Cluster 환경(Replica Set, Sharded Cluster) 환경에서만 동작**한다.  
`Spring Data Mongo` 에선 `org.springframework.transaction.annotation.Transactional` 어노테이션을 지원하여 쉽게 트랜잭션 구현이 가능하다.  

```java
@Transactional
public UserDocument createUser(CreateUserRequestDto requestDto) {
    UserDocument user = userRepository.save(new UserDocument(requestDto.getUsername(), requestDto.getEmail()));
    // if (true) throw new IllegalArgumentException(""); // 트랜잭션 확인용
    UserDetailDocument userDetail = userDetailRepository.save(new UserDetailDocument(
            user.getId(),
            requestDto.getAge(),
            requestDto.getGender(),
            requestDto.getNickname(),
            requestDto.getDesc()));
    return user;
}
```

주석을 해제하고 고의로 예외를 발생시키면 `UserDocuemnt` 저장요청도 롤백된다.  

세밀하게 트랜잭션 구간을 나누고 싶다면 아래 직접 트랜잭션 구간을 설정할 수 있다.  

- transactionTemplate(주로사용)
- mongoTemplate.withSession(session)
- MongoOperations

`TransactionTemplate` 을 사용하려면 Bean 으로 등록해야한다.  

```java
@Bean
public TransactionTemplate transactionTemplate(MongoTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
}
```

```java
// transactionTemplate
public UserDocument updateUserBySession(String id, CreateUserRequestDto requestDto) {
    return transactionTemplate.execute((TransactionStatus action) -> {
        UserDocument user = userRepository.findById(id).orElseThrow();
        user.setEmail(requestDto.getEmail());
        user.setUsername(requestDto.getUsername());
        user = userRepository.save(user);
        if (true) throw new IllegalArgumentException(""); // 트랜잭션 확인용
        UserDetailDocument userDetail = userDetailRepository.findById(id).orElseThrow();
        userDetail.setAge(requestDto.getAge());
        userDetail.setGender(requestDto.getGender());
        userDetail.setNickname(requestDto.getNickname());
        userDetail.setDesc(requestDto.getDesc());
        userDetail = userDetailRepository.save(userDetail);
        return user;
    });
}

// mongoTemplate.withSession(session)
public UserDocument updateUserBySession2(String id, CreateUserRequestDto requestDto) {
    ClientSessionOptions options = ClientSessionOptions.builder()
            .causallyConsistent(true) // 옵션 설정
            .build();
    try (ClientSession session = client.startSession(options)) {
        session.startTransaction();
        try {
            UserDocument user = userRepository.findById(id).orElseThrow();
            user.setEmail(requestDto.getEmail());
            user.setUsername(requestDto.getUsername());
            mongoTemplate.withSession(session).save(user); // User 저장
            if (true) throw new IllegalArgumentException("Rollback test");
            UserDetailDocument userDetail = userDetailRepository.findById(id).orElseThrow();
            userDetail.setAge(requestDto.getAge());
            userDetail.setGender(requestDto.getGender());
            userDetail.setNickname(requestDto.getNickname());
            userDetail.setDesc(requestDto.getDesc());
            mongoTemplate.withSession(session).save(userDetail);
            session.commitTransaction();
            return user;
        } catch (Exception e) {
            session.abortTransaction();
            throw e;
        }
    }
}

// MongoOperations action
public UserDocument updateUserBySession3(String id, CreateUserRequestDto requestDto) {
    ClientSessionOptions options = ClientSessionOptions.builder()
            .causallyConsistent(true) // 옵션 설정
            .build();
    try (ClientSession session = client.startSession(options)) {
        return mongoTemplate.withSession(() -> session).execute((MongoOperations action) -> {
            session.startTransaction(); // 트랜잭션 시작
            try {
                Query userQuery = Query.query(where("_id").is(id));
                UserDocument user = action.findOne(userQuery, UserDocument.class);
                user.setEmail(requestDto.getEmail());
                user.setUsername(requestDto.getUsername());
                action.save(user);
                if (true) throw new IllegalArgumentException("Rollback test");
                Query userDetailQuery = Query.query(where("_id").is(id));
                UserDetailDocument userDetail = action.findOne(userDetailQuery, UserDetailDocument.class);
                userDetail.setAge(requestDto.getAge());
                userDetail.setGender(requestDto.getGender());
                userDetail.setNickname(requestDto.getNickname());
                userDetail.setDesc(requestDto.getDesc());
                action.save(userDetail);
                session.commitTransaction(); // 트랜잭션 커밋
                return user;
            } catch (Exception e) {
                session.abortTransaction(); // 트랜잭션 롤백
                throw e;
            }
        });
    }
}
```

## Converter

`Spring Data Mongo` 에서도 `org.springframework.core.convert.converter.Converter` 를 지원한다.  

아래와 같이 정의한 `NotificationDocument` 클래스를 `MongoDB` 의 `Document` 으로부터 비직렬화 하기 위해 `Converter` 를 추가할 수 있다.  

```java
@Bean
public MongoCustomConversions customConversions() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModules(
            new ParameterNamesModule(), // 기본생성자 없어도 직렬화
            new JavaTimeModule() // JSR310 모듈 등록
    );
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return new MongoCustomConversions(List.of(
            new NotificationReadConverter(objectMapper),
            new UserAlarmReadConverter(objectMapper)
    ));
}

@RequiredArgsConstructor
public class NotificationReadConverter implements Converter<Document, NotificationDocument> {

    private final ObjectMapper objectMapper;
    @Override
    public NotificationDocument convert(Document source) {
        NotificationDocument result = objectMapper.convertValue(source, NotificationDocument.class);
        return result;
    }
}
```

아래와 같이 다형성을 지원하는 객체를 비직력화 하는데 효과적.  

> `Converter` 를 정의하지 않고 `_class` 필드와 `@TypeAlias` 사용으로도 클래스 타입에 맞춰 비직렬화 가능하다.  

아래와 같이 `[id, type, userId, message, timestamp]` 까지는 일치하지만 구현별로 데이터가 조금씩 다를경우 상속구조로 클래스를 설계하는데,

```js
[
  {
    "id": "msg-001",
    "type": "message",
    "userId": 1001,
    "message": "You have a new message.",
    "timestamp": "2024-12-25T14:28:11.221Z",
    "senderId": 2001
  },
  {
    "id": "fr-001",
    "type": "friend_request",
    "userId": 1002,
    "message": "John Doe sent you a friend request.",
    "timestamp": "2024-12-25T14:28:11.221Z",
    "requesterId": 3001
  },
  {
    "id": "evt-001",
    "type": "event_invite",
    "userId": 1003,
    "message": "You are invited to the Annual Meetup.",
    "timestamp": "2024-12-25T14:28:11.221Z",
    "eventId": 4001,
    "location": "New York City"
  }
]
```

`ObjectMapper` 와 `Conveter` 를 같이 사용하면 쉽게 다형성을 지원하는 비직렬화 지원을 구성할 수 있다.  

```java
@Getter
@Setter
@Document(collection = "notifications")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true // type 정보도 출력할건지 여부
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageNotification.class, name = "message"),
        @JsonSubTypes.Type(value = FriendRequestNotification.class, name = "friend_request"),
        @JsonSubTypes.Type(value = EventInviteNotification.class, name = "event_invite")
})
public abstract class NotificationDocument {
    @Id
    private String id;

    // mongo document 에서 역직렬화 시 "_id"를 사용
    @JsonSetter("_id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter("id")
    public String getId() {
        return this.id;
    }

    private String type;
    private Long userId;
    private String message;
    private Instant timestamp;

    public NotificationDocument(String id, String type, Long userId, String message, Instant timestamp) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Getter
    @Setter
    public static class EventInviteNotification extends NotificationDocument {
        private Long eventId;
        private String location;

        public EventInviteNotification(String id, String type, Long userId, String message, Instant timestamp, Long eventId, String location) {
            super(id, type, userId, message, timestamp);
            this.eventId = eventId;
            this.location = location;
        }
    }

    @Getter
    @Setter
    public static class FriendRequestNotification extends NotificationDocument {
        private Long requesterId;

        public FriendRequestNotification(String id, String type, Long userId, String message, Instant timestamp, Long requesterId) {
            super(id, type, userId, message, timestamp);
            this.requesterId = requesterId;
        }
    }

    @Getter
    @Setter
    public static class MessageNotification extends NotificationDocument {
        private Long senderId;

        public MessageNotification(String id, String type, Long userId, String message, Instant timestamp, Long senderId) {
            super(id, type, userId, message, timestamp);
            this.senderId = senderId;
        }
    }
}
```

## 데모코드  

> <https://github.com/Kouzie/spring-boot-demo/tree/main/mongodb-demo>  