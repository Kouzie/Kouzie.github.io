---
title:  "Spring Boot - AWS!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## AWS Library

해당 포스팅에선 서비스의 설명은 문서로 대체하고 Spring 과의 연동을 위한 라이브러리 사용방법에 대해서만 설명한다.  

> <https://awspring.io/>  
> <https://github.com/awspring/spring-cloud-aws>  
> <https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/get-started.html>  

`Spring Boot3` 부터는 `AWS SDK for Java 2.x`, `Spring Cloud AWS 2023.0.x` 버전 이상을 써야한다.  

```kotlin
allprojects {
  // build.gradle.kts
  val springBootVersion="3.2.4"
  val springCloudAwsVersion="3.2.1"
  val softwareAwsSdkBomVersion="2.21.20"

  // ...

  dependencies {
      implementation(platform("software.amazon.awssdk:bom:${softwareAwsSdkBomVersion}"))
      implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${springCloudAwsVersion}"))
  }
}
```

### LocalStack  

> <https://www.localstack.cloud/>  

테스트는 `LocalStack` 사용, 각종 AWS 서비스를 로컬에서 테스트할 수 있게 도와준다.  

```yaml
services:
  localstack:
    container_name: "${LOCALSTACK_DOCKER_NAME:-localstack-main}"
    image: localstack/localstack
    ports:
      - "127.0.0.1:4566:4566"            # LocalStack Gateway
      - "127.0.0.1:4510-4559:4510-4559"  # external services port range
    environment:
      # LocalStack configuration: https://docs.localstack.cloud/references/configuration/
      - DEBUG=${DEBUG:-0}
      # - LOCALSTACK_AUTH_TOKEN=${LOCALSTACK_AUTH_TOKEN- } # pro version 에서 요구
    volumes:
      - "${LOCALSTACK_VOLUME_DIR:-./volume}:/var/lib/localstack"
      - "/var/run/docker.sock:/var/run/docker.sock"
```

GUI 사용은 `LocalStack Desktop` 혹은 아래 브라우저 기반 사이트에서 localhost 에서 동작중인 `LocalStack` 서비스에 접근 가능

> <https://app.localstack.cloud/inst/default/resources>
> 회원가입 필수

CLI 에선 `awslocal` 툴을 사용하는것을 추천.  

```shell
pip install awscli-local

awslocal configure
AWS Access Key ID [None]: test-access-key
AWS Secret Access Key [None]: test-secret-key
Default region name [None]: us-east-1
Default output format [None]: json

awslocal s3 mb s3://my-local-bucket
awslocal s3 ls
```

> LocalStack 에 접근하기 위한 access key, secret key 는 아무값이나 입력하면 된다.  

```kotlin
@Configuration
class AwsCredential {

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create("access-key", "secret-key")
        )
    }
}
```

`http://localhost:4566` URL 을 Endpoint 로 사용해 각종 AWS 서비스 테스트 가능하다.  

## 데모코드  

아래 서비스 테스트 진행

- SQS
- S3
- ParameterStore
- Lambda
- DynamoDB

> <https://github.com/Kouzie/spring-aws-demo>  

## SQS

> SQS 설명
> <https://docs.aws.amazon.com/ko_kr/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html>


```kotlin
dependencies {
    implementation("software.amazon.awssdk:sqs")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
}
```

`spring-cloud-aws-starter-sqs` 를 사용하면 어노테이션, `application.yml` 을 통해 SQS 를 설정하고 메세지를 수신받을 수 있다.  

```kotlin
/**
 * https://docs.awspring.io/spring-cloud-aws/docs/3.0.1/apidocs/io/awspring/cloud/sqs/listener/ContainerOptionsBuilder.html#maxMessagesPerPoll(int)
 * listener container bean 을 별도로 생성하고, 해당 bean 이 @SqsListener 메서드를 호출하는 방식
 * queueNames(values): queue 이름
 * factory: listener container bean 생성전략
 * id: listener container bean id 지정
 * maxConcurrentMessages: listener bean 내부 워커스레드 개수 지정, default 10,
 * pollTimeoutSeconds: SQS Long Polling 시간(초 단위) default 10
 * maxMessagesPerPoll: polling 당 가져올 메세지 수 default 10
 * messageVisibilitySeconds: 메세지 처리 보장시간 default 30, 시간내에 메세지를 확인처리하지 않으면 다시 소비가능한 상태로 돌아간다.
 * acknowledgementMode: 메세지 확인후 삭제 모드 default ON_SUCCESS
 *  - ON_SUCCESS: 오류 없이 메서드 완료시 확인 및 삭제처리
 *  - ALWAYS: 오류 상관 없이 확인 및 삭제처리
 *  - MANUAL: 수동 확인 및 삭제처리
 * */
@SqsListener(
    queueNames = ["\${demo.aws.sqs.queue-name}"],
    maxConcurrentMessages = "10",
    pollTimeoutSeconds = "20",
    maxMessagesPerPoll = "5",
    messageVisibilitySeconds = "10",
    acknowledgementMode = "MANUAL",
)
fun receiveMessage(
    message: String,
    @Header("demoAttr") demoAttrValue: String,
    ack: Acknowledgement,
) {
    logger.info("header: ${demoAttrValue}, payload:${message}")
    ack.acknowledge() // 수동 확인 처리
}
```

간결하고 직관적이지만 어노테이션 AOP 로 동작되기 때문에 커스텀하기 힘들 수 있다.  

kotlin 과 코루틴을 사용할 수 있다면 `software.amazon.awssdk:sqs` 라이브러리를 사용해 직접 Polling 처리를 하는것도 좋아보인다.  
빠른 SQS 메세지 처리를 수행할 수 있고 실시간으로 `maxNumberOfMessages, waitTimeSeconds` 를 조절해 서버 부하를 조절할 수 도 있다.  

```kotlin
private var running = true // 작업 실행 상태 플래그

companion object {
    const val maxNumberOfMessages = 10;
    const val waitTimeSeconds = 10;
}

/**
    * 과도한 흐름을 제어하기 위해 별도의 Thread Pool 적용
    * 하나의 scope 로 관리할 경우 과도한 메세지가 들어오면 가장 밭깥의 while 문만 돌고 delete Message 는 되지 않아 악순환반복됨으로 스코프를 나눔
    * 일종의 우선순위처럼 동작 가능
    * */
private val pollMessageScope = CoroutineScope(
    Executors.newFixedThreadPool(1).asCoroutineDispatcher() + SupervisorJob()
)
private val processMessageScope = CoroutineScope(
    Executors.newFixedThreadPool(2).asCoroutineDispatcher() + SupervisorJob()
)

@PreDestroy
fun stopPolling() {
    logger.info("Stopping SQS polling...")
    running = false
    pollMessageScope.cancel() // 모든 코루틴 중단
    processMessageScope.cancel() // 모든 코루틴 중단
}

@PostConstruct
fun startPolling() {
    pollMessageScope.launch {
        while (running) { // 공통 루프에서 관리
            pollMessages()
        }
    }
}

private suspend fun pollMessages() {
    val receiveMessageRequest = ReceiveMessageRequest.builder()
        .queueUrl(sqsComponent.queueUrl)
        .maxNumberOfMessages(maxNumberOfMessages) // 한 번에 가져올 메시지 수
        .waitTimeSeconds(waitTimeSeconds) // Long polling 시간
        .build()

    val messages = sqsClient.receiveMessage(receiveMessageRequest).messages()
    logger.info("${messages.size} messages received")
    var jobs = messages.mapIndexed() { idx, message ->
        processMessageScope.launch {
            val workerName = Thread.currentThread().name
            processMessage(workerName, idx, message)
            logger.info("Polling cycle complete, workerId: $workerName")
        }
    }
}

private suspend fun processMessage(workerName: String, idx: Int, message: Message) {
    logger.info("Worker($workerName) processing index(${idx}) message: ${message.body()}")
    delay(10000L)
    // 메시지 처리 로직 추가
    deleteMessage(message)
    logger.info("Worker($workerName) finished index(${idx}) message: ${message.body()}")
}

private fun deleteMessage(message: Message) {
    sqsClient.deleteMessage(
        DeleteMessageRequest.builder()
            .queueUrl(sqsComponent.queueUrl)
            .receiptHandle(message.receiptHandle())
            .build()
    )
}
```


## ParameterStore

`/demo/param/` 을 prefix 로 `ParameterStore` 에 `key-value` 지정  

```shell
awslocal ssm put-parameter \
    --type String \
    --name "/demo/param/DEMO_PROPERTIES_ID" \
    --value "aws_id"
    
awslocal ssm put-parameter \
    --type String \
    --name "/demo/param/DEMO_PROPERTIES_KEY" \
    --value "aws_key"

awslocal ssm put-parameter \
    --type String \
    --name "/demo/param/DEMO_PROPERTIES_URL" \
    --value "aws_url"
```

`spring.config.import` 속성을 통해 `Parameter Store` 에 저장된 값을 `Spring Properties` 값으로 사용할 수 있다.  

```kotlin
dependencies {
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")
}
```

```yml
spring:
  application:
    name: param-demo
  profiles:
    active: paramstore
demo:
  properties:
    id: ${DEMO_PROPERTIES_ID:default_id}
    key: ${DEMO_PROPERTIES_KEY:default_key}
    url: ${DEMO_PROPERTIES_URL:default_url}
---
spring:
  config:
    activate:
      on-profile: paramstore
    import: 'optional:aws-parameterstore:/demo/param/'
  cloud:
    aws:
      region:
        static: us-east-1
      credentials:
        secret-key: test-access-key
        access-key: test-secret-key
      parameterstore:
        enabled: true
        endpoint: http://localhost:4566
```

> 비슷한 역할을 수행하는 서비스로 Secret Manager 가 있으며 key-value 이외에도 인증서, 암호화 키등을 관리할 수 있으며 수명주기로 관리할 수 있다.  

## Lambda

```kotlin
dependencies {
    implementation("software.amazon.awssdk:lambda")
}
```

비동기로 호출할것인지 동기로 호출할 것인지 지정, Event 로 호출할 경우 `HttpStatus.ACCEPTED(202)` 가 반환된다.  

```kotlin
/**
    * @param functionName 등록한 함수명
    * @param payload lambda 에 전달할 페이로드
    * @param type 호출 타입
    *  Event: 비동기
    *  RequestResponse: 동기
    *  DryRun: 테스트호출, 실행하지 않고 request 가 유효한지만 확인
    *
    * */
fun invokeLambda(functionName: String, payload: String, type: InvocationType): String {
    var request = InvokeRequest.builder()
        .invocationType(type)
        .functionName(functionName)
        .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(mapOf("body" to payload))))
        .build()
    var response: InvokeResponse = lambdaClient.invoke(request)
    if (response.statusCode() in listOf(HttpStatus.OK.value(), HttpStatus.ACCEPTED.value())) {
        return response.payload().asUtf8String()
    } else {
        throw RuntimeException("failed to invoke lambda:${functionName}, status:${response.statusCode()}")
    }
}
```

## DynamoDB

> 참고: 한번씩 읽어볼만한 문서들
> <https://kouzie.github.io/database/DynamoDB>  
> <https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/java_dynamodb_code_examples.html>  
> <https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/DynamoDBEnhanced.html>  
> <https://github.com/aws/aws-sdk-java-v2/tree/master/services-custom/dynamodb-enhanced>
>
> Srping Boot 에서 `java sdk v2 dynamodb` 를 사용하는 예제
> <https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/usecases/creating_dynamodb_web_app>  
> <https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/usecases/creating_first_project>  
> <https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/ProgrammingWithJava.html>

```kotlin
noArg {
    annotation("software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean") // @DynamoDbBean 기본생성자 생성
}

dependencies {
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:dynamodb-enhanced")
}
```

해당 예제에선 싱글 테이블 패턴을 사용한다.  

```kotlin
@Component
class DynamodbComponent(
    val dynamoDbClient: DynamoDbClient,
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    @Value("\${ddb.sigle-table-name}")
    private lateinit var tableName: String

    /**
     * single table 전략
     * pk sk 필수
     * expired 가 있는 데이터는 자동 삭제됨
     * */
    @PostConstruct
    private fun createTableIfNotExists() {
        val tableNames = dynamoDbClient.listTables().tableNames()
        if (!tableNames.contains(tableName)) {
            dynamoDbClient.createTable {
                it.tableName(tableName)
                it.keySchema(
                    KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                    KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build()
                )
                it.attributeDefinitions(
                    AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                    AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build()
                )
                it.provisionedThroughput {
                    it.readCapacityUnits(5)
                    it.writeCapacityUnits(5)
                }
            }
            println("Created table $tableName")
        }
        val describeTTLRequest = DescribeTimeToLiveRequest.builder()
            .tableName(tableName)
            .build()
        val describeTTLResponse: DescribeTimeToLiveResponse = dynamoDbClient.describeTimeToLive(describeTTLRequest)
        val currentTTLStatus: TimeToLiveStatus = describeTTLResponse.timeToLiveDescription().timeToLiveStatus()

        if (currentTTLStatus == TimeToLiveStatus.DISABLED) {
            // TTL 설정
            val ttlAttributeName = "expired"
            val ttlReq = UpdateTimeToLiveRequest.builder()
                .tableName(tableName)
                .timeToLiveSpecification(
                    TimeToLiveSpecification.builder()
                        .attributeName(ttlAttributeName)
                        .enabled(true)
                        .build()
                )
                .build()
            dynamoDbClient.updateTimeToLive(ttlReq)
            println("Updated TTL Config $tableName $ttlAttributeName")
        }
    }
    /**
     * mapping 만 해줄뿐이지 single table pattern 에서 타입별 필터링까진 해주지 않는다.
     * */
    fun <T : Any> generateDynamoDbTable(entityClass: KClass<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(entityClass.java))
    }
}
```

### 어노테이션  

> <https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/ddb-en-client-anno-index.html>  

- **enhanced.dynamodb.mapper.annotations**
  - **DynamoDbBean**  
    class, 데이터 클래스 지정  
  - **DynamoDbPartitionKey**  
    getter, 기본 파티션키 지정  
  - **DynamoDbSortKey**  
    getter, 기본 정렬키 지정  

위에 3가지는 필수적으로 사용하고 나머지 어노테이션은 선택사항  

- **enhanced.dynamodb.mapper.annotations**
  - **DynamoDbSecondaryPartitionKey**  
    getter, GSI 의 파티션키
  - **DynamoDbSecondarySortKey**  
    getter, GSI 의 정렬키, LSI 의 정렬키
  - **DynamoDbAttribute**  
    getter, 속성의 이름을 변경  
  - **DynamoDbIgnore**  
    getter, 속성 무시  
  - **DynamoDbFlatten**  
    getter, 중첩 클래스 평탄화  
  - **DynamoDbConvertedBy**  
    getter, 컨버터 클래스 지정  
  - **DynamoDbIgnoreNulls**  
    getter, not null 효과  
  - **DynamoDbImmutable**  
    class, update 불가한 데이터  
  - **DynamoDbPreserveEmptyObject**  
    getter, 중첩클래스의 경우 값을 가져올 때 null 대신 기본생성자로 데이터가 설정됨  
  - **DynamoDbUpdateBehavior**  
    getter, 업데이트시 동작 행동, `[WRITE_ALWAYS, WRITE_IF_NOT_EXISTS]` 설정 가능  
- **enhanced.dynamodb.extensions.annotations**  
  - **DynamoDbVersionAttribute**
    getter, 항목 버전 번호를 증가시키고 추적 낙관적 잠금 기능을 제공  
  - **DynamoDbAtomicCounter**  
    getter, 레코드가 write 될 때마다 태그가 지정된 숫자 속성이 증가  
  - **DynamoDbAutoGeneratedTimestampAttribute**  
    getter, 레코드가 write 될 때마다 태그가 지정된 속성을 현재 타임스탬프로 지정

```kotlin
// Sort key for primary index and partition key for GSI "SubjectLastPostedDateIndex".
@DynamoDbSortKey
@DynamoDbSecondaryPartitionKey(indexNames = "SubjectLastPostedDateIndex")
public String getSubject() {
    return Subject;
}

public void setSubject(String subject) {
    Subject = subject;
}

// Sort key for GSI "SubjectLastPostedDateIndex" and sort key for LSI "ForumLastPostedDateIndex".
@DynamoDbSecondarySortKey(indexNames = {"SubjectLastPostedDateIndex", "ForumLastPostedDateIndex"})
public String getLastPostedDateTime() {
    return LastPostedDateTime;
}
```

이 외의 어노테이션은 위 aws 공식문서에서 확인  

```kotlin
@DynamoDbBean
class DemoEntity(
    @get:DynamoDbPartitionKey
    var pk: String, // DEMO#{demoId}
    @get:DynamoDbSortKey
    var sk: String, // DEMO#{demoId}
    @get:DynamoDbAttribute("attrKey")
    val internalKey: String,
    @get:DynamoDbIgnore
    val ignoreKey: String,
    @get:DynamoDbVersionAttribute // 항목 버전 번호, 낙관적 잠금 지원
    val version: Long,
    @get:DynamoDbAtomicCounter(startValue = 0, delta = 1) // 1씩 증가
    val updateCounter: Long,

    @get:DynamoDbAutoGeneratedTimestampAttribute
    val updateTimestamp: Instant,
    @get:DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    val updated: Instant,
    @get:DynamoDbUpdateBehavior(UpdateBehavior.WRITE_IF_NOT_EXISTS)
    val created: Instant,
) {

}

@DynamoDbBean
class DemoFlatten {

}
```

### Query

`기본 키(Primary Key)` 를 `PartitionKey`, `SortKey` 로 구성하였다면 해당 파티션 내에 `Query(범위질의)`가 가능하다.  

```kotlin
fun getByCustomerId(customerId: String, beginDate: Long, endingDate: Long): List<OrderDto> {
    val key = Key.builder().partitionValue("CUSTOMER#$customerId").build()
    val request: QueryConditional = QueryConditional.keyEqualTo(key)

    val query = QueryConditional.sortBetween(
        Key.builder().partitionValue("CUSTOMER#$customerId").sortValue("ORDER#${beginDate}#${MIN_UUID}").build(),
        Key.builder().partitionValue("CUSTOMER#$customerId").sortValue("ORDER#${endingDate}#${MAX_UUID}").build()
    )
    orderTable.query(request)
    val result: PageIterable<CustomerOrderEntity> = customerOrderTable.query(query)
    val keysToGet: List<String> = result.items()
        .map { "ORDER#${it.sk.split("#")[2]}" }

    val readBatchBuilder = ReadBatch.builder(OrderEntity::class.java)
        .mappedTableResource(orderTable)
    for (orderId in keysToGet) {
        readBatchBuilder.addGetItem(Key.builder().partitionValue(orderId).sortValue(orderId).build())
    }
    var resultPages: BatchGetResultPageIterable =
        enhancedClient.batchGetItem { b -> b.readBatches(readBatchBuilder.build()) }
    return resultPages.resultsForTable(orderTable).map { mapper.toDto(it) }
}
```

### Scan

```kotlin
fun getByCreateTimeBetween(startTime: Instant, endTime: Instant): List<CustomerDto> {
    val scanRequest = ScanEnhancedRequest.builder()
        .filterExpression(
            Expression.builder()
                .expression("created BETWEEN :startTime AND :endTime")
                .expressionValues(
                    mapOf(
                        ":startTime" to AttributeValue.builder().s(startTime.toString()).build(),
                        ":endTime" to AttributeValue.builder().s(endTime.toString()).build()
                    )
                )
                .build()
        )
        .build()
    val result: PageIterable<CustomerEntity> = customerTable.scan(scanRequest)
    return result.items()
        .filter { it.type == "CUSTOMER"}
        .map { mapper.toDto(it) }
}
```

### 트랜잭션

```kotlin
@DynamoDbBean
class CustomerEntity(
    @get:DynamoDbPartitionKey
    var pk: String, // CUSTOMER#{customerId}
    @get:DynamoDbSortKey
    var sk: String, // CUSTOMER#{customerId}
    var type: String, // CUSTOMER
    var username: String,
    var password: String,
    var expired: Long, // 유효기간
    var updated: Instant,
    var created: Instant
)


@DynamoDbBean
class CustomerInfoEntity(
    @get:DynamoDbPartitionKey
    var pk: String, // CUSTOMER#{customerId}
    @get:DynamoDbSortKey
    var sk: String, // CUSTOMER_INFO#{customerId}
    var type: String, // CUSTOMER_INFO
    var age: Int,
    var email: String,
    var nickname: String,
    var intro: String,
    var expired: Long, // 유효기간
    var updated: Instant,
    var created: Instant
)
```

고객 테이블의 `로그인 Entity` 와 `상세정보 Entity` 2개를 Transactional 하게 저장  

```kotlin
val customerTable = dynamodbComponent.generateDynamoDbTable(CustomerEntity::class)
val customerInfoTable = dynamodbComponent.generateDynamoDbTable(customerInfoTable::class)

fun create(request: CustomerAddRequest): CustomerDetailDto {
    val customerId = UUID.randomUUID().toString()
    val now = Instant.now()
    val customerEntity = mapper.toEntity(request, customerId, now)
    val customerInfoEntity = mapper.toCustomerInfoEntity(request, customerId, now)
    enhancedClient.transactWriteItems(
        TransactWriteItemsEnhancedRequest.builder()
            .addPutItem(customerTable, customerEntity)
            .addPutItem(customerInfoTable, customerInfoEntity)
            .build()
    )
    return mapper.toDetailDto(customerEntity, customerInfoEntity)
}
```

### 배치 Read

두개 이상의 `기본 키(Primary Key)` 를 한번에 조회할 때 사용한다.  

> `SortKey` 를 사용한 `Query` 는 `배치 Read` 에선 사용할 수 없다.  

```kotlin
fun getById(customerId: String): CustomerDetailDto {
    val pk = "CUSTOMER#${customerId}"
    val infoSk = "CUSTOMER_INFO#${customerId}"
    val customerRead: ReadBatch = ReadBatch.builder(CustomerEntity::class.java)
        .mappedTableResource(customerTable)
        .addGetItem(Key.builder().partitionValue(pk).sortValue(pk).build())
        .build()

    val customerInfoRead: ReadBatch = ReadBatch.builder(CustomerInfoEntity::class.java)
        .mappedTableResource(customerInfoTable)
        .addGetItem(Key.builder().partitionValue(pk).sortValue(infoSk).build())
        .build()

    val resultPages: BatchGetResultPageIterable = enhancedClient.batchGetItem(
        BatchGetItemEnhancedRequest.builder()
            .readBatches(customerRead, customerInfoRead)
            .build()
    )

    // results 를 반복하면 일치하는 값 필터링
    val entity: CustomerEntity = resultPages.resultsForTable(customerTable)
        .first { it.type == "CUSTOMER" }
    val customerInfoEntity: CustomerInfoEntity = resultPages.resultsForTable(customerInfoTable)
        .first { it.type == "CUSTOMER_INFO" }
    return mapper.toDetailDto(entity, customerInfoEntity)
}
```


<!-- 
## java sdk v1 dynamodb

java sdk v1, Sprinb Boot 2 버전을 기준으로 DynamoDB 사용을 위한 SDK, 

### Spring Data DynamoDB

해당 SDK 를 보다 쉽게 사용할 수 있도록 비공식 라이브러리인 `Spring Data DynamoDB` 를 사용할 수 있다.  

> <https://github.com/boostchicken/spring-data-dynamodb>

```conf
# application.properties
spring.data.dynamodb.entity2ddl.auto=create-only
```

```java
// @Bean
// public AWSCredentialsProvider awsCredentialsProvider() {
//     return new DefaultAWSCredentialsProviderChain();
// }

// local-dynamodb 사용을 위한 설정
@Bean(name = "amazonDynamoDB")
public AmazonDynamoDB amazonDynamoDb(AWSCredentialsProvider awsCredentialsProvider) {
    AmazonDynamoDB amazonDynamoDb = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "ap-northeast-2"))
            .build();
    return amazonDynamoDb;
}
```

DynamoDB 테이블 설정을 위한 주석은 아래 url 을 참조  

> <https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/DynamoDBMapper.Annotations.html>  

가장 중요한건 `DynamoDBIndexHashKey`, `DynamoDBIndexRangeKey` 어노테이션일 것인데  
각각 보조 인덱스를 만들기 위한 파티션키와 정렬키를 설정하는 어노테이션이다.  

해당 어노테이션이 설정된 후 `findBy...` 과 같은 함수로 호출시 자동으로 인덱스를 찾아 객체를 매핑한다.  

### DyanamoDB Java Client

> <https://docs.aws.amazon.com/ko_kr/sdk-for-java/latest/developer-guide/java_ec2_code_examples.html>

비공식 `Spring Data DyanamoDB` 를 사용하기보다 AWS 에서 제공하는 `DyanamoDB Java Client` 라이브러리를 사용하는것도 좋은 방법이다.  

또한 `Dynamic Query` 지원을 위해서는 AWS 에서 제공하는 `DyanamoDB Java Client` 의 `DynamoDBMapper` 의 `scan`, `query` 기능을 사용할 수 밖에 없다.  

> DynamoDB scan vs query: <https://dynobase.dev/dynamodb-scan-vs-query/>
![ddd1](/assets/2022/dynamodb4.png)  
> `scan`, `query` 모두 테이블에서 컬렉션을 읽어오기 위한 메서드이지만, `query` 가 `파티션 키`를 사용하기 때문에 성능이 더 뛰어나며 문서 역시 `query` 메서드 사용을 권장한다.  

일단 아래처럼 `Filter Condition` 을 생성하는 코드를 작성할 수 있다.  

```java
private Map<String, Condition> generateFilter(GetCustomerRequestDto requestDto) {
    Map<String, Condition> filter = new HashMap<>();
    if (StringUtils.hasLength(requestDto.getName())) {
        filter.put("name", new Condition()
            .withComparisonOperator(ComparisonOperator.CONTAINS)
            .withAttributeValueList(new AttributeValue(requestDto.getName())));
    }
    if (StringUtils.hasLength(requestDto.getType())) {
        CustomerType type = CustomerType.forValue(requestDto.getType());
        if (type != null) {
            filter.put("type", new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue()));
        }
    }
    if (requestDto.getBeginDate() != null && requestDto.getEndingDate() != null) {
        if (requestDto.getEndingDate().isBefore(requestDto.getBeginDate())) {
            throw new IllegalArgumentException("being date is after then ending date");
        }
        filter.put("create", new Condition()
            .withComparisonOperator(ComparisonOperator.BETWEEN)
            .withAttributeValueList(
                new AttributeValue(CustomTimeUtil.getUTCString(requestDto.getBeginDate())),
                new AttributeValue(CustomTimeUtil.getUTCString(requestDto.getEndingDate()))
                // zone date time to UTC Time String
            )
        );
    }
    return filter;
}
```

타입에 따라 사용할 수 있는 `ComparisonOperator` 가 있으며 자세한 사항은 공식 문서 확인

> <https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_Condition.html>  

위의 경우 `name`, `type`, `create` 필드에 따라 dynamic 하게 쿼리를 생성할 수 있도록 설정하였다.  

```java
public List<Customer> findAllByUmidAndContractCdAndPortNo(String group, GetCustomerRequestDto requestDto) {
    Customer forHash = new Customer();
    forHash.setGroup(group);
    Map<String, Condition> queryFilter = generateFilter(requestDto);
    DynamoDBQueryExpression expression = new DynamoDBQueryExpression()
        .withHashKeyValues(forHash)
        .withConsistentRead(false)
        .withQueryFilter(queryFilter);
    return dynamoDBMapper.query(Customer.class, expression);
}
```

`Customer` 객체의 경우 `group` 문자열 필드를 `GSI` 로 설정하여 `HashKeyValue` 데이터로 사용하였다.  

`GSI` 를 사용하다 보니 일관적인 읽기지원이 불가능함으로 `withConsistentRead(false)` 를 설정해주어야 한다.  

`scan` 의 경우 아래처럼 진행해야 하는데 `id` 리스트를 기반으로 검색을 진행하려면 어쩔수 없이 `scan` 요청을 해야한다.  

```java
public List<Customer> findAllByIdIn(List<String> customerIds, GetCustomerRequestDto requestDto) {
    Map<String, Condition> scanFilter = generateFilter(requestDto);
    List<AttributeValue> attList = customerIds.stream().map(id -> new AttributeValue(id)).collect(Collectors.toList());
    scanFilter.put("id", new Condition()
        .withComparisonOperator(ComparisonOperator.IN)
        .withAttributeValueList(attList));
    DynamoDBScanExpression expression = new DynamoDBScanExpression()
        .withScanFilter(scanFilter);
    return dynamoDBMapper.scan(Customer.class, expression);
}
```

RCU 를 낮추기 위해 HashKey 를 사용하는데, 아쉽게도 동시에 여러개의 HashKey 를 사용하여 쿼리하는 것은 불가능하다.  

queryFilter 를 사용해 전체읽기를 사용하거나, 두번 읽은다음 어플리케이션 레이어에서 조인해야한다.  

### 트랜잭션  

<https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/DynamoDBMapper.Methods.html>
<https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/DynamoDBMapper.Transactions.html>

위 url 에 작성된 데모코드와 같이 `DynamoDBMapper` 을 사용하면 트랜잭션 기능을 사용할 수 있기는 하다.  
`TransactionLoadRequest` 를 작성하고 아래와 같이 매퍼에 전달하면 된다.  

```java
loadedObjects = mapper.transactionLoad(transactionLoadRequest);
```

### 여담  

테이블 생성시 `LSI` 을 생성하려면 `RangeKey` 를 설정해야 한다.  
그런데 이 `RangeKey` 를 `Spring Data DynamoDB` 와 같이 사용하기가 쉽지 않다.  

`Spring Data DynamoDB` 에서 제공하는 `Repository` 객체들이 `RangeKey` 와 `HashKey` 중 어떤 값을 키값(Id)로 설정해야 하는지 혼동되어 아래와 같은 에러가 발생한다.  

`no field or method annotated with interface org.springframework.data.annotation.id found`

그렇다고 `@Id` 어노테이션을 추가하면 아래 에러가 발생하게 되는데  

`No method or field annotated by @DynamoDBHashKey within type java.lang.String!`

모두 `Repository` 인터페이스에서 제공하는 에러들이다.  

`RangeKey` 를 써야한다면 `Repository` 객체를 사용하지 않고 `DynamoDBMapper` 를 이용해 쿼리를 작성하면 된다.  

혹은 `Spring Data DynamoDB` 에서 제공하는 `Custom Key Class` 를 별도로 작성하면 된다.  

<https://github.com/derjust/spring-data-dynamodb/wiki/Use-Hash-Range-keys>

> `DynamoDBMapper` 만을 사용하는것을 추천

만약 두개의 칼럼을 기반으로 필터링해야할 경우 `칼럼1#칼럼2` 형태로 2개의 칼럼을 하나의 칼럼에 우겨넣어 LSI 로 설정해야 한다.  

DynamoDB 는 단순한 CRUD 에선 최적이라할 수 있지만 복잡한 쿼리식은 아예 설계불가능할 수 있기에 충분한 요구분석후에 사용을 결정해야 한다.
-->

## SigV4

> <https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/http/auth/aws/signer/AwsV4HttpSigner.html>  
> <https://github.com/aws-samples/sigv4-signing-examples>  

