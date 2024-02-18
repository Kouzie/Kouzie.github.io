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


## Observability

> <https://opentelemetry.io/>

클라우드 네이티브 환경에서 어플리케이션의 관측가능성을 지원하기 위한 유명한 프로젝트.  
각종 언어별 라이브러리, 컨테이너 이미지, helm 차트, `k8s CRD` 등을 제공한다.  

### OpenTelemetry

> <https://opentelemetry.io/docs/languages/java/instrumentation/>
> <https://medium.com/cloud-native-daily/how-to-send-traces-from-spring-boot-to-jaeger-229c19f544db>  
> <https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/library>  

`Spring` 진영에서 `OpenTelemetry` 를 지원하는 프로젝트가 대표적으로 3개 있다.  

- Micrometer - Spring Boot 3
- Sleuth - Spring Boot 2
- OpenTelemetry agent - javaagent

결론부터 말하자면 `SpringBoot 3.x` 부터는 `Micrometer` 사용을 권장한다.  

`OpenTelemetry agent` 의 경우 Java 에이전트의 잠재적인 보안 문제, 애플리케이션 내 메서드 인터셉터로 인해 성능 문제가 발생한다.  

`Micrometer` 가 `SpringBoot 3.x` 부터 지원되며 `Spring Cloud Sleuth` 형태를 이어받았다.  

```conf
# application.properties
spring.main.banner-mode=off
```

```groovy
// spring cloud 에 종속성 있음₩
implementation "io.micrometer:micrometer-tracing-bridge-otel" 
```

> `Spring Cloud Sleuth` 는 `SpringBoot 3.x` 에서 중단되었다.  
> 반대로 `SpringBoot 2.x` 를 사용한다면 `Spring Cloud Sleuth` 를 사용해야한다.  

여기까지 설정하고 실행하면 아래와 같은 로그가 출력된다.  
`mdc(traceId, spanId)` 확인.  

```
{
    timestamp=2024-02-05T07:18:03.603Z, 
    level=INFO, 
    thread=http-nio-8080-exec-2, 
    mdc={traceId=f7b4a70ed567bf06493478e16b0c71ae, spanId=4896f0334deed5a3}, 
    logger=com.kube.demo.calculating.filter.RequestFilter, 
    message=client:0:0:0:0:0:0:0:1, 
    URL:/greeting, 
    context=default
}
```

`micrometer-tracing-bridge-otel` 은 `[log, trace]` 정보를 `OTEL` 에 맞춰 생성해줄뿐, `OTEL 컬렉터`로 전송하진 않는다.  

해당 `[log, trace]` 정보를 `OTEL 컬렉터` 로 전송하는 `exporter` 만 정의하면 된다.  
여러가지 방법이 있겠지만 여기선 `io.opentelemetry` 에서 배포한 `dependency` 들을 사용한다.  

> <https://mvnrepository.com/artifact/io.opentelemetry>  

여러가지 라이브러리중 이번 포스팅에서 사용하는 라이브러리는 아래 3개.  

- **opentelemetry-api**: 전송할 측정데이터의 계산 방법을 정의, 비지니스 로직에서 주로 사용함.  
- **opentelemetry-sdk**: 측정데이터의 처리 및 출력을 설정.  
- **opentelemetry-exporter-otlp**: exporter 의 구현체, `OTEL HTTP`, `OTEL GRPC` 프로토콜을 사용 가능.  

`opentelemetry-sdk` 안에 이미 `opentelemetry-api` 가 포함되어 있지만 비니니스 로직에서는 `opentelemetry-api` 의존성 주입 받아 사용하는것을 권장.  

```groovy
dependencyManagement {
    imports {
        mavenBom "io.opentelemetry:opentelemetry-bom:1.34.1"
    }
}

dependencies {
  implementation "io.opentelemetry:opentelemetry-sdk"
  implementation "io.opentelemetry:opentelemetry-exporter-otlp"

  // OTEL Log Exporter whit LOGBACK appender
  def OTEL_LOGBACK_VERSION = "2.0.0-alpha"
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:$OTEL_LOGBACK_VERSION"
}
```

```java
/**
 * https://opentelemetry.io/docs/languages/java/exporters/#usage
 ***/
@Configuration
public class OtlpConfig {

    @Bean
    public OpenTelemetry openTelemetry(@Value("${otel.endpoint}") String endpoint,
                                       @Value("${spring.application.name}") String serviceName,
                                       @Value("${service.version:0.1.0}") String serviceVersion) {
        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .put(ResourceAttributes.SERVICE_NAMESPACE, "demo")
                .put(ResourceAttributes.SERVICE_VERSION, serviceVersion).build();

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
                                .build())
                .setResource(resource)
                .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(
                        BatchLogRecordProcessor
                                .builder(OtlpGrpcLogRecordExporter.builder().setEndpoint(endpoint).build())
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

`OpenTelemetryAppender.install` 를 사용하게 되면 콘솔에서 `mdc` 정보가 뜨지 않지만 실제 `Loki` 서비스에 전송된 로그를 확인해보면 `trace` 정보가 같이 찍혀있다.  

파싱 및 라벨링에서 번거로운 작업을 줄이기 위해 json 형태로 로그를 저장.  

```groovy
implementation "ch.qos.logback.contrib:logback-json-classic:0.1.5"
implementation "ch.qos.logback.contrib:logback-jackson:0.1.5"
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
    <appender name="OpenTelemetry" 
      class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
    </appender>
    <!-- LOG INFO level -->
    <root level="info">
        <appender-ref ref="Console"/>
        <appender-ref ref="OpenTelemetry"/>
    </root>
</configuration>
```


> 반드시 모든 관측데이터의 exporter 를 java 라이브러리로 구현하지 않아도 된다.  
>
> log 의 경우 fluentbit 같은 file log 를 tail 하는 방식, metric 의 경우 시스템 메트릭만 관측해도 된다면 prometheus 의 pull 방식을 사용하면 된다.  
> trace 는 opentelemetry 연동구조가 가장 대중적이며, zipkin 이나 jeager 시스템을 사용중이라면 전용 라이브러리를 사용할 수 있다.  

만약 `otlp log exporter` 를 사용하지 않고 `[fluentbit, promtail]` 를 사용해 tail 방식으로 file log 를 전송할 예정이라면 단순 출력을 위한 라이브러리를 사용  

```groovy
dependencies {
  // OTEL Log Exporter whit LOGBACK appender
  def OTEL_LOGBACK_VERSION = "2.0.0-alpha"
  // implementation "io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:$OTEL_LOGBACK_VERSION"
  implementation "io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:$OTEL_LOGBACK_VERSION"
}
```

```xml
<appender name="OpenTelemetry" class="io.opentelemetry.instrumentation.logback.mdc.v1_0.OpenTelemetryAppender">
    <appender-ref ref="Console"/>
</appender>
```

## Metric  

`OpenTelemetry` 의 `SdkMeterProvider` 를 사용해서 `Metric` 정보를 `OTEL 컬렉터`에 전송하는 방법은 아래와 같다.  

```java
@Bean
public Meter customMeter(OpenTelemetry openTelemetry) {
    Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
            .setInstrumentationVersion("1.0.0")
            .build();
    return meter;
}

...

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
    log.info("greet invoked");
    counter.add(1, attributes);
    HelloJava helloJava = new HelloJava(greetingMessage, LocalDateTime.now());
    return objectMapper.writeValueAsString(helloJava);
}
```

### Prometheus

`Metric` 대부분 서버 상태 관측에 사용되며 `Prometheus` 를 주로 사용한다.  
위와같은 단건 정보를 전송시에도 `micrometer` 라이브러리를 사용해 `Proemtheus` 데이터에 통합시키는 방법을 사용한다.  

`Prometheus Metric` 정보는 전달하는 방식으로 `push base, pull base` 가 있으며 대부분 `pull base` 를 사용한다.  

`Prometheus Metric` 사용시 아래와 같이 `[actuator, micrometer]` 라이브러리 조합을 사용한다.  

```groovy
implementation "org.springframework.boot:spring-boot-starter-actuator"
implementation "io.micrometer:micrometer-registry-prometheus"
```

`push base` 를 사용하고 싶다면 `OTEL 컬렉터` 를 통하지 않고 아래와 같이 `SpringBoot` 서버에서 `Prometheus` 서버에 직접 `Metric` 을 전달해야한다.  

> `OTEL 컬렉터 receivers` 에서 `Prometheus` 의 `push base` 를 지원하지 않음.  

```conf
management.prometheus.metrics.export.pushgateway.base-url=${METRIC_URL:http://localhost:9091}
```

> <https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.export.prometheus>
> `--management.prometheus.metrics.export.pushgateway.enabled=true` 커맨드 실행명령으로 전달시 동작한다.  

`pull base` 의 경우 아래 actuator 설정을 통해 진행.  
`http://localhost:9094/actuator/prometheus` 를 통해 `Metric` 을 읽어올 수 있다.  

```conf
# actuator
management.endpoint.health.enabled=true
# readiness, liveness enable
management.endpoint.health.probes.enabled=true
management.endpoints.web.exposure.include=prometheus,health
management.server.port=9404
```

보통 `Prometheus` 에서 모든 `Pod` `Metric` 수집 시 `ServiceMonitor` `k8s CRD` 방법을 사용하지만,  
이번 포스팅에선 `OTEL 사이드카 컬렉터` 를 사용해보기로 한다.  

> <https://opentelemetry.io/docs/kubernetes/operator/>  
> <https://medium.com/@dudwls96/kubernetes-환경에서-opentelemetry-collector-구성하기-d20e474a8b18>

`Sidecar` 방식으로 운영하기 위해 `opentelemetry-operator deployment` 및 `k8s CRD` 추가  

`[Log, Trace]` 관측데이터는 `otlp` 로 전달받고, `Metric` 은 `Prometheus` `pull base` 로 진행,  
`OTEL 사이드카 컬렉터` 에서 수집한 모든 관측데이터는 `OTEL 게이트웨이 컬렉터` 로 전달한다,  
`OTEL 게이트웨이 컬렉터` 에서 최종으로 `[Loki, Tempo, Prometheus]` 와 같은 백엔드 서비스에 관측 데이터를 전달하게 된다.   

![1](/assets/springboot/spring-cloud/springcloud_otel1.png)  

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

`OTEL 게이트웨이 컬랙터`는 아래 데모코드 참고 helm 차트로 설치.  

## 데모코드  

> <https://github.com/Kouzie/local-k8s>  
> <https://github.com/Kouzie/spring-kube-demo>  
