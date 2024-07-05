---
title:  "Spring Boot - Observability!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - monitoring
---


## OpenTelemetry

> <https://opentelemetry.io/>  
> otel doc: <https://opentelemetry.io/docs/languages/java/instrumentation/>  
> otel logback: <https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/library>  

![1](/assets/springboot/spring-cloud/springcloud_otel2.png)  

클라우드 네이티브 환경에서 어플리케이션의 관측가능성을 지원하기 위한 유명한 프로젝트.  
각종 언어별 라이브러리, 컨테이너 이미지, 헬름 차트, `k8s CRD` 등을 제공한다.  

`OpenTelemetry` 프로젝트를 사용하면 아래 3개 관측 데이터에 대해 `Observability` 기능을 지원한다.  

- 로그
- 추적
- 메트릭

`Spring` 에서 `OpenTelemetry` 를 사용하는 여러가지 방법이 있지만, 여기선 가장 쉽고, 보편적인 `Micrometer` 를 사용한다.  

테스트를 위해 `OTEL 컬렉터`를 실행

```yaml
# docker-compose.yaml

version: '3.9'

services:
  otel-collector:
    image: otel/opentelemetry-collector-contrib
    # volumes:
    #   - ./otel-collector-config.yaml:/etc/otelcol-contrib/config.yaml
    ports:
      - 1888:1888 # pprof extension
      - 8888:8888 # Prometheus metrics exposed by the Collector
      - 8889:8889 # Prometheus exporter metrics
      - 13133:13133 # health_check extension
      - 4317:4317 # OTLP gRPC receiver
      - 4318:4318 # OTLP http receiver
      # - 55679:55679 # zpages extension
```

> `OpenTelemetry` `javaagent` 를 사용하면 코드변경 없이 `[로그, 추적, 메트릭]` 에 대해 자동계측이 가능하다.  
> 하지만 `javaagent` 의 잠재적인 보안 문제, 애플리케이션 내 메서드 인터셉터로 인해 성능 문제가 발생함으로 직접 구성하는 것을 추천한다.  
> <https://medium.com/cloud-native-daily/how-to-send-traces-from-spring-boot-to-jaeger-229c19f544db>  
>
> 모든 관측데이터를 수집하기 위해 `OpenTelemetry` 를 사용하지 않아도 된다.  
> 로그는 `fluentbit` 같은 `file log tail` 방식, 메트릭은 `prometheus pull` 방식을 사용하면 된다.  
> 추적데이터는 `OpenTelemetry` 연동구조가 가장 대중적이며, `zipkin` 이나 `jeager` 시스템을 사용중이라면 전용 라이브러리를 사용할 수 있다.  

## io.opentelemetry  

> <https://mvnrepository.com/artifact/io.opentelemetry>  

`OpenTelemetry` 에서 제공하는 라이브러리로 `[로그, 추적, 메트릭]` 관측데이터를 `OTEL 컬렉터` 로 전달 할 수 있다.  

`io.opentelemetry` 패키지에서 주로 사용하는 라이브러리는 아래 3가지  

- **opentelemetry-api**: 전송할 측정데이터 처리를 위한 클래스, 함수 정의.  
- **opentelemetry-sdk**: 측정데이터의 처리를 위한 클래스, 함수 구현체.  
- **opentelemetry-exporter-otlp**: 측정데이터 exporter 의 구현체, `OTEL HTTP`, `OTEL GRPC` 프로토콜을 사용 가능.  

> `opentelemetry-sdk` 안에 이미 `opentelemetry-api` 가 포함되어 있지만 비즈니스 로직에서는 `opentelemetry-api` 의존성 주입 받아 사용하는것을 권장.  

```groovy
dependencyManagement {
    imports {
        mavenBom "io.opentelemetry:opentelemetry-bom:1.34.1"
    }
}

dependencies {
  implementation "io.opentelemetry:opentelemetry-api"
  implementation "io.opentelemetry:opentelemetry-sdk"
  implementation "io.opentelemetry:opentelemetry-exporter-otlp"

  // json 형태로 로그 출력
  implementation "ch.qos.logback.contrib:logback-json-classic:0.1.5"
  implementation "ch.qos.logback.contrib:logback-jackson:0.1.5"

  // OTEL Log Exporter whit LOGBACK appender
  def OTEL_LOGBACK_VERSION = "2.0.0-alpha"
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:$OTEL_LOGBACK_VERSION"
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$OPENTELEMETRY_VERSION"
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="ch.qos.logback.contrib.json.classic.JsonLayout">
                <timestampFormat>yyyy-MM-dd'T'HH:mm:ss.SSSX</timestampFormat>
                <timestampFormatTimezoneId>Etc/UTC</timestampFormatTimezoneId>
                <appendLineSeparator>true</appendLineSeparator>
            </layout>
        </encoder>
    </appender>
    <appender name="OpenTelemetry" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender"/>
    <appender name="OpenTelemetryConsole" class="io.opentelemetry.instrumentation.logback.mdc.v1_0.OpenTelemetryAppender">
        <appender-ref ref="Console"/>
    </appender>

    <root level="info">
        <appender-ref ref="OpenTelemetry"/>
        <appender-ref ref="OpenTelemetryConsole"/>
    </root>
</configuration>
```

```java
/**
 * https://opentelemetry.io/docs/languages/java/exporters/#usage
 ***/
@Configuration
public class MyOtlpConfig {

    @Bean
    public OpenTelemetry openTelemetry(@Value("${tracing.url}") String endpoint,
                                       @Value("${spring.application.name}") String serviceName,
                                       @Value("${service.version:0.1.0}") String serviceVersion) {
        Resource resource = Resource.getDefault().toBuilder()
                .put(SERVICE_NAME, serviceName)
                .put(SERVICE_NAMESPACE, "spring")
                .put(SERVICE_VERSION, serviceVersion)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(
                        BatchSpanProcessor
                                .builder(OtlpGrpcSpanExporter.builder().setEndpoint(endpoint).build())
                                .build())
                .setResource(resource)
                .build();
        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(
                        PeriodicMetricReader
                                .builder(OtlpGrpcMetricExporter.builder().setEndpoint(endpoint).build())
                                .setInterval(Duration.ofSeconds(5))
                                .build())
                .setResource(resource)
                .build();
        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(
                        BatchLogRecordProcessor
                                .builder(OtlpGrpcLogRecordExporter.builder()
                                        .setEndpoint(endpoint)
                                        .build())
                                .build())
                .setResource(resource)
                .build();
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
        // install log agent in log appender
        OpenTelemetryAppender.install(openTelemetry);
        return openTelemetry;
    }
}
```

### Meter, Trace

`Meter` 에 대한 설정을 하지 않으면 생존신고를 위한 메트릭만 `OTEL 컬렉터`로 전송한다.  
`Tracer` 에 대한 설정을 하지 않으면 어떠한 추적데이터도 `OTEL 컬렉터`로 전송되지 않는다.  

추가적인 메트릭, 추적데이터를 `OTEL 컬렉터` 에 전송하기 위한 `Meter`, `Tracer` 설정방법은 아래와 같다.  

```java
// 메트릭데이터 생성기 io.opentelemetry.api.metrics.Meter
@Bean
public Meter customMeter(OpenTelemetry openTelemetry) {
    return openTelemetry.meterBuilder("exampleMeter")
            .setInstrumentationVersion("1.0.0")
            .build();
}
// 추적데이터 생성기 io.opentelemetry.api.trace
@Bean
public Tracer customTracer(OpenTelemetry openTelemetry) {
    return openTelemetry.tracerBuilder("exampleTracer")
            .setInstrumentationVersion( "1.0.0")
            .build();
}

...
// io.opentelemetry.api.metrics.LongCounter
private LongCounter counter;
private Attributes attributes;

@PostConstruct
private void init() {
    // Build counter e.g. LongCounter
    this.counter = meter
            .counterBuilder("processed_jobs")
            .setDescription("Processed jobs")
            .setUnit("1")
            .build();
    this.attributes = Attributes.of(AttributeKey.stringKey("Key"), "SomeWork");
}

@GetMapping
public String greet() throws JsonProcessingException {
    // Span 생성
    Span span = tracer.spanBuilder("exampleSpan")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();
    // Span 내에서 작업 수행
    try (Scope scope = span.makeCurrent()) {
        // 수행할 작업
        log.info("greet invoked");
        counter.increment(1);
        span.addEvent("count increment inside the span");
    } catch (Exception e) {
        span.setStatus(StatusCode.ERROR, "Exception occurred");
        span.recordException(e);
    } finally {
        span.end();
    }
    HelloJava helloJava = new HelloJava(greetingMessage + ", version:" + version, LocalDateTime.now());
    return objectMapper.writeValueAsString(helloJava);
}
```

### Log

위에서 로그를 `OTEL 컬렉터` 로 전달하기 위해 두가지 dependency 를 추가하고 logback 에 설정했다.  

```groovy
dependencies {
  def OTEL_LOGBACK_VERSION = "2.0.0-alpha"
  // for push log data(OTEL Collector)
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:$OTEL_LOGBACK_VERSION"
  // for print file loe
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$OPENTELEMETRY_VERSION"
}
```

```java
OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
    .setTracerProvider(sdkTracerProvider)
    .setMeterProvider(sdkMeterProvider)
    .setLoggerProvider(sdkLoggerProvider)
    .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
    .buildAndRegisterGlobal();
// install log agent in log appender
OpenTelemetryAppender.install(openTelemetry);
```

```xml
<appender name="OpenTelemetry" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender"/>
<appender name="OpenTelemetryConsole" class="io.opentelemetry.instrumentation.logback.mdc.v1_0.OpenTelemetryAppender">
    <appender-ref ref="Console"/>
</appender>

<root level="info">
    <!-- OTEL 컬렉터 전달을위한 appender -->
    <appender-ref ref="OpenTelemetry"/> 
    <!-- mdc 정보를 같이 로그에 출력하기 위한 appender -->
    <appender-ref ref="OpenTelemetryConsole"/>
</root>
```

만약 `[fluentbit, promtail]` 를 사용해 `file log tail` 방식으로 전송할 예정이라면 `file log` 에도 `mdc` 정보 출력해야 함으로 `opentelemetry-logback-mdc` 라이브러리를 사용해야 한다.  

`opentelemetry-logback-mdc` 설정하고 실행하면 아래와 같은 추적 데이터가 포함된 로그가 출력된다.  

```text
// logback 출력 로그
{
    timestamp=2024-05-14T04:02:18.106Z, 
    level=INFO, 
    thread=http-nio-8080-exec-1, 
    mdc={
        trace_id=db5882a7b3d310198106b96f529f0ade, 
        trace_flags=01, 
        span_id=3e72805295708786
    }, 
    logger=com.kube.demo.greeting.contorller.GreetingController, 
    message=greet invoked, 
    context=default
}
```

## io.micrometer

위에서 느꼈겠지만 `io.opentelemetry` 는 자동계측은 지원하지 않는다,  

사용자가 코드 사이사이에 `[Meter, Tracer]` 를 사용해 수기로 관측데이터를 생성 및 지정해줘야 한다.  

대부분의 사용자가 운영코드 사이사이에 계측관련 코드를 넣고싶지 않을것이기에 `io.micrometer` 와 같은 자동계측을 지원하는 라이브러리와 같이 사용한다.  

`io.micrometer` 는 자동계측 뿐만 아니라 동일한 코드로 다양한 관측 백엔드 서비스를 사용할 수 있도록 도와준다.  
메트릭, 추적 데이터를 수집하는 어플리케이션은 `OpenTelemetry` 말고도 굉장히 많은데,  

- OpenTelemetry  
- Prometheus  
- Zipkin  
- Jaeger  

대부분 관측백엔드에서 제공하는 라이브러리의 사용방식이 비슷하다.  
메트릭에선 `[Counter, Gauage, Summary]` 를 정의하고 추적에선 `Span` 을 정의한다.  

`io.micrometer` 를 사용하면 여러가지 백엔드 서비스 라이브러리를 주입받아 동일한 코드로 관측데이터 계측을 지원한다.  

### Metric  

`io.micrometer` 에서 제공하는 `MeterRegistry` 를 사용면 jvm 자동계측 메트릭과 사용자 지정 메트릭을 같이 관리할 수 있다.  

```groovy
implementation 'io.micrometer:micrometer-registry-otlp'
```

```java
// Micrometer 메트릭 생성기
// io.micrometer.core.instrument.MeterRegistry
@Bean
public MeterRegistry meterRegistry() {
    OtlpConfig otlpConfig = new OtlpConfig() {
        @Override
        public String get(String key) {
            return null;
        }

        // 아쉽게도 micrometer 에서 otlp(grpc) 프로토콜은 지원하지 않음
        @Override
        public String url() {
            return "http://localhost:4318/v1/metrics";
        }

        @Override
        public Duration step() {
            return Duration.ofSeconds(5);
        }
    };
    return new OtlpMeterRegistry(otlpConfig, Clock.SYSTEM);
}
```

친숙한 `Micrometer` 의 메트릭 클래스, 함수들을 사용할 수 있다.  

```java
@Slf4j
@RestController
@RequestMapping("/greeting")
@RequiredArgsConstructor
public class GreetingController {
    private final ObjectMapper objectMapper;
    private final MeterRegistry registry;
    @Value("${greeting.message}")
    private String greetingMessage;
    // io.micrometer.core.instrument.Counter
    private Counter counter; // 카운터 메트릭

    @PostConstruct
    private void init() {
        // Counter 설정
        this.counter = Counter.builder("api.call.count")
                .description("api call count")
                .tags("team", "monitoring", "deploy_version", "dev")
                .register(registry);
    }

    @Value("${image.version}")
    private String version;

    @GetMapping
    public String greet() throws JsonProcessingException {
        log.info("greet invoked");
        counter.increment(1);
        HelloJava helloJava = new HelloJava(greetingMessage + ", version:" + version, LocalDateTime.now());
        return objectMapper.writeValueAsString(helloJava);
    }
}
```

`MeterRegistry` 는 HTTP 프로토콜을 사용하는 만큼 `Opentelemetry` 와의 의존성이 완벽히 분리되어 아예 `io.opentelemetry` 라이브러리를 사용하지 않는다.  

메트릭만 측정해도 되는 상황이라면 `io.opentelemetry` 라이브러리를 모두 걷어내고 `micrometer-registry-otlp` 만 설정해도 `Micrometer` 에서 메트릭 데이터를 `OTEL 컬렉터` 로 Export 해준다.  

![1](/assets/springboot/spring-cloud/springcloud_otel3.png)  

#### Prometheus

> <https://opentelemetry.io/docs/kubernetes/operator/>  
> <https://medium.com/@dudwls96/kubernetes-환경에서-opentelemetry-collector-구성하기-d20e474a8b18>

`OTEL 컬렉터` 에서 `Pull Prometheus Metric` 방식도 지원하기에 같이 소개한다.  

보통 모든 `Pod` 의 `Pull Prometheus Metric` 수집 시 `ServiceMonitor k8s CRD` 사이드카 방식을 사용하지만, 이번 포스팅에선 `OTEL 사이드카 컬렉터` 를 사용해보기로 한다.  

`Prometheus Metric` 노출을 위해 `[actuator, micrometer]` 라이브러리 사용.  

```groovy
implementation "org.springframework.boot:spring-boot-starter-actuator"
implementation "io.micrometer:micrometer-registry-prometheus"
```

```conf
# actuator
management.endpoint.health.enabled=true
# readiness, liveness enable
management.endpoint.health.probes.enabled=true
management.endpoints.web.exposure.include=prometheus,health
management.server.port=9404
```

`http://localhost:9094/actuator/prometheus` 를 통해 `Metric` 을 읽어올 수 있다.  

`OTEL 사이드카 컬렉터` 운영을 위해 `opentelemetry-operator deployment` 및 `k8s CRD` 추가  

`sidecar.opentelemetry.io/inject` 주석이 추가되면 `OTEL 사이드카 컬렉터` 가 같은 `Pod` 에서 동작한다.  

> `opentelemetry-operator deployment`, `k8s CRD` 설치참고  
> <https://kouzie.github.io/monitoring/모니터링-OpenTelemetry/#sidecar-operating-mode>

`[Log, Trace]` 관측데이터는 `otlp` 로 전달받고, `Metric` 은 `Prometheus` `pull base` 로 진행,  
`OTEL 사이드카 컬렉터` 에서 수집한 모든 관측데이터는 `OTEL 게이트웨이 컬렉터` 로 전달한다,  

```yaml
apiVersion: opentelemetry.io/v1alpha1
kind: OpenTelemetryCollector
metadata:
  name: sidecar-for-spring
  namespace: spring
spec:
  mode: sidecar
  config: |
    receivers:
      otlp:
        protocols:
          grpc:
            endpoint: 0.0.0.0:4317 # 모든 입력 IP 허용
      prometheus:
        config:
          scrape_configs:
          - job_name: 'spring-kube-demo'
            scrape_interval: 1m
            static_configs:
            - targets: ["0.0.0.0:9404"]
            metrics_path: "/actuator/prometheus"
    processors:
    exporters: # 모든 데이터 otel gateway 로 전송
      logging: {}
      otlp:
        endpoint: "http://opentelemetry-collecor-opentelemetry-collector.monitoring.svc.cluster.local:4317"
        tls:
          insecure: true

    service:
      pipelines:
        traces:
          receivers: [otlp]
          processors: []
          exporters: [logging, otlp]
        logs:
          receivers: [otlp]
          processors: []
          exporters: [logging, otlp]
        metrics:
          receivers: [otlp, prometheus]
          processors: []
          exporters: [logging, otlp]
```

`OTEL 게이트웨이 컬랙터`는 아래 데모코드 참고(helm 차트로 설치).  

아래 그림과 같은 구성으로, `OTEL 게이트웨이 컬렉터` 에서 최종으로 `[Loki, Tempo, Prometheus]` 와 같은 백엔드 서비스에 관측 데이터를 전달하게 된다.  

![1](/assets/springboot/spring-cloud/springcloud_otel1.png)  

#### Prometheus Metric Push Base

참고로 `OTEL 컬렉터` receivers 에서 `Prometheus` 의 `push base` 를 지원하지 않는다.  
`push base` 를 사용하고 싶다면 아래와 같이 `SpringBoot` 서버에서 `Prometheus` 서버에 직접 `Metric` 을 전달해야한다.  

```conf
management.prometheus.metrics.export.pushgateway.base-url=${METRIC_URL:http://localhost:9091}
```

> <https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.export.prometheus>  
> `--management.prometheus.metrics.export.pushgateway.enabled=true` 커맨드 실행명령으로 전달시 동작한다.  


### Trace

`SpringBoot2` 에서는 `zipkin`, `jaeger` 등의 추적 백엔드 서비스를 사용하기 위해 `Slueth` 자동계측 라이브러리를 사용했다.  
`SpringBoot3` 부터는 `Micrometer` 를 사용해서 추적 백엔드 서비스 사용이 가능하다.  

> `Spring Cloud Sleuth` 는 `SpringBoot 3.x` 에서 중단되었다.  
> `Micrometer` 를 사용한 추적데이터 수집은 `SpringBoot 3.x` 부터 지원되며 `Spring Cloud Sleuth` 형태를 이어받았다.  

`micrometer-tracing-bridge-otel` 은 의존성 분리가 되어있지 않기 때문에 `io.opentelemetry` 라이브러리를 같이 사용해야한다.  
실제 `Micromter` 에서 제공하는 `OtelTracer` 구현체 내부에서 `io.opentelemetry` 패키지의 구현체를 필요로 한다.  

`OTEL 컬렉터` 로 추적 데이터를 Push 하려면 아래와 같이 설정  

```groovy
implementation "io.micrometer:micrometer-tracing-bridge-otel" 
implementation "io.opentelemetry:opentelemetry-sdk"
implementation "io.opentelemetry:opentelemetry-exporter-otlp"
```

`application.properties` 에도 아래와 같이 `OTEL 컬렉터` 주소를 설정한다.  

```
management.otlp.tracing.endpoint=http://localhost:4318/v1/tracing
```

```java
package io.micrometer.tracing.otel.bridge;

...
import io.micrometer.tracing.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

public class OtelTracer implements Tracer {

    private final io.opentelemetry.api.trace.Tracer tracer;
    ...
}
```

이제 모든 HTTP 요청에 자동으로 추적 데이터가 설정되며 로그에도 관련 mdc 정보가 출력되고 `OTEL 컬렉터` 로도 추적 데이터가 Push 된다. 

아래와 같이 `micrometer-tracing` 라이브러리에서 제공하는 어노테이션을 사용해서 새로운 Span 을 메서드마다 생성할 수 있다.  

> `Controller` 는 자동계측되기 때문에 `Span` 태그가 의미 없지만 `Service` 나 다른 메서드에는 의미있는 `Span` 생성이 가능  

```java
@GetMapping("/{num1}/{num2}")
@ContinueSpan("calculate")
public String calculate(@SpanTag("num1") @PathVariable Long num1, @SpanTag("num2") @PathVariable Long num2) {
    log.info("calculate invoked, num1:{}, num2:{}", num1, num2);
    Long addResult = calculatingClient.addNumbers(num1, num2);
    // 결과 값을 저장할 AtomicInteger 생성
    result.set(addResult);
    summary.record(num1);
    summary.record(num2);
    return result.toString();
}
```

#### Feign Client Trace

```groovy
implementation 'io.github.openfeign:feign-micrometer:12.3'
```

위와같은 라이브러리를 추가하면 모든 Feign Client 의 HTTP 요청에 대해 Trace Id 를 연계할 수 있다.  

## 데모코드  

> <https://github.com/Kouzie/local-k8s>  
> <https://github.com/Kouzie/spring-kube-demo>  
