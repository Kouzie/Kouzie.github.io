---
title:  "모니터링 - OpenTelemetry!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - monitoring
---

## OpenTelemetry 개요

> <https://opentelemetry.io/docs/what-is-opentelemetry/>

클라우드 네이티브 애플리케이션이 출시되면서 각 서비스에 대한 **관찰가능성(Observability)** 에 대한 중요도가 올라갔다.  

> 관찰가능성은 시스템의 외부 출력에 대한 결과 값(지식)으로부터 시스템의 내부 상태를 얼마나 잘 추론 할 수 있는지를 나타내는 척도  

관찰가능성 크게 `[로깅, 메트릭, 추적]` 3가지 주제로 나뉘며 주제별로 아래와 같은 대표적인 오픈소스가 존재한다.  

- 로깅  
  - Fluentd  
  - Logstash  
  - Apache Flum  

- 메트릭
  - Prometheus  
  - Grafana  
  - Graphite  
  - StatsD  

- 추적
  - OpenCensus  
  - OpenTracing  
  - Zipkin  
  - Jaeger  

다양한 관찰가능성 지원 오픈소스를 사용하기 위해 `OpenTelemtry`(이하 `OTEL`) 라는 공통적인 포멧을 만들었다.  
2023 년 기준으로 `OTEL` 에서 11 개 언어에 대한 구현을 제공, `Prometheus` 와 `OpenMetrics` 프로젝트를 포함한 많은 오픈소스 커뮤니티가 `OTEL` 에 합류했다.  

`OTEL` 는 `OpenTracing` 과 `OpenCensus` 라는 두 프로젝트 의 병합으로 탄생했다.  

> `OpenTracing` 은 분산추적을 위한 API 규격을 제공한다.  
> `OpenCensus` 구글 내부에서 시작된 프로젝트, 애플리케이션을 추적하고 메트릭을 생산 및 수집할 수 있도록 라이브 러리를 제공  
> Census: 인구조사  

`OpenCensus` 는 `[터기, 컬렉터, 스토리지]` 로 활동하면서 서비스의 **측정데이터**를 수집, 분석한다.  

모든 서비스가 `OpenCensus` SDK 에 의존성이 생기지만 단순한 아키텍처 구성이 가능하다.  

![zipkin](/assets/monitoring/observability_1.png)  

> `OpenCensus 컬렉터` 는 향후 `OTEL 컬렉터` 재개발된다.  

### OpenTelemetry 구성

`OTEL` 는 아래와 구성요소를 가진다.  

- 시그널  
- 파이프라인  
- 리소스  

#### 시그널  

**측정데이터**를 담을 수 있는 **개방형 규격(specification)**  

> 언어에 관계없이 통일된 경험, 유연한 확장을 지원함.  
> 아래 github 링크에서 확인 가능  
> <https://github.com/open-telemetry/opentelemetry-specification>  

**측정데이터**에 대해 아래 4가지 개념으로 나누고 `시그널`을 정의했다.  

> 시그널 = 규격화된 측정데이터

- 추적(tracking)  
- 메트릭(metric)  
- 로그(log)  
- 배기지(baggage)  

> `배기지`는 `메트릭, 추적, 로그` 에 `주석/문맥` 을 추가하기 위한 개념, `key-value` 형태를 가짐  

#### 파이프라인  

`OTEL 시그널` 에 해당하는 `메트릭, 추적, 로그` 의 측정데이터를 `[생성, 처리, 전송]`하는 일련의 과정

```text
프로바이더 → 생성기 → 처리기 → 익스포터
```

**프로바이더**  
파이프라인의 시작점, 생성기의 팩토리 역할.  
어플리케이션 코드 초반부에 설정됨.  
측정데이터를 만들 수 있도록 생성기를 정의하고, 어플리케이션이 접근가능하도록 하게함.  

**생성기**  
프로바이더로부터 정의/생성 됨.  
SDK 를 통해 코드 여러 지점에서 측정데이터를 생성한다.  
생성하는 측정데이터 종류에 따라 이름이 다르게 불림.  

- 추적기: 추척 데이터
- 미터: 메트릭 데이터

**처리기**  
생성기로부터 받은 측정데이터를 후처리 진행.  
빈도수 제어, 측정데이터의 수정을 진행.  

**익스포터**  
측정데이터가 어플리케이션 컨텍스트를 빠져나가기 전에 거치는 마지막 단계.  
측정데이터는 익스포터를 통해 아래와 같은 형식으로 변환된다.  

- consul  
- jaeger  
- zipkin  
- prometheus  
- OpenTelemetry Protocol(OTLP)  
- OpenCensus  

#### 리소스, 시맨틱 표기법

측정데이터에 적용된 일련의 속성, 주석과 같은 개념이다.  
원격 측정 데이터의 출처가 서버인지, 컨테이너인지, 함수인지 식별할 때 사용한다.  

- `service.name`: 서비스명  
- `service.version`: 서비스 버전  
- `host.name`: 호스트명  

측정데이터의 종류 상관없이 `OTEL` 에서 생성되었다면 위와 같은 일련의 속성을 가진다.  
이때 속성은 `시맨틱 표기법` 을 통해 사전에 정의해둔 형태로 구성한다.  

`[WebServer, DB, Message Broker, RPC Server]` 등 여러가지 제품이 '서비스' 란 단어 하나로 표현된다.  
그리고 각종 서비스에서 발생하는 워크로드(프로토콜) 또한 수십종류이다.  

여러 형태의 서비스와 워크로드에 대한 정의, 일관성 있는 관측을 하려면 철저한 표기법과 규칙을 정의해야 한다.  

`OTEL` 에선 YAML 로 기술된 `시멘틱 표기법` 을 제공하여 `Observability` 요구사항을 만족시킨다.  

아래는 HTTP 클라이언트 스팬의 시멘틱 표기법이다.  

| 속성             | 값                              |
| ---------------- | ------------------------------- |
| http.method      | "GET"                           |
| http.flavor      | "1.1"                           |
| http.url         | "http://local.com/shopping?s=1" |
| net.peer.ip      | "192.0.2.5"                     |
| http.status_code | 200                             |

시그널별로 수많은 `속성`과 `리소스`가 들이 시멘틱 표기법을 따르며 관찰가능성 기능을 지원한다.  

> 스팬은 분산 시스템이 처리하는 작업 단위 혹은 요청을 의미  

### Opentelemetry 자동계측

대부분의 라이브러리, 프레임워크에서 별다른 코드추가 없이 `[zipkin, jaeger, prometheus]` 등의 오픈소스와 연동하고 `[메트릭, 추적, 로그]` 의 관측을 지원한다.  
위에서 알바왔던 `[메트릭, 추적, 로그]` 의 파이프라인 클래스를 생성하지 않고도 측정데이터를 수집하고 계측을 지원한다.  

`OTEL` 의 모토가 최소한의 코드변경, 최소비용으로 높은 관측가능성을 지원하는 **자동계측**이기 때문이다.  
언어별, 프레임워크별로 구현방법은 다르겠지만 라이브러리에서 `OTEL` 를 지원한다면 `자동계측`을 지원한다.  

java 진영에선 `[Akka, gRPC, Hibernate, JDBC, Kafka, Spring, Tomcat]` 등에서 `OTEL` 를 지원한다.  

> java 어플리케이션에서 `OTEL 자동계측`을 사용하려면 아래 url 참고  
>
> javaagent 모듈  
> <https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases>

`자동계측` 의 단점으론 운영에 필요없는 데이터 계측이 강제될 수 있고, 반대로 운영에 꼭 필요한 커스텀한 비지니스 로직은 계측하지 않는다.  
하지만 운영코드에 변경없이 꾸준한 관측데이터를 쌓을 수 있다는 점때문에 자동계측을 많이 사용한다.  

자동계측 내부에선 `[매트릭, 추적, 로그]` 측정데이터를 생성하고 백엔드로 보내기 위한 함수들이 호출되는 내용이 구현되어 있다는것을 알아야한다.  

### 측정데이터 운영

원격 측정 서버 시스템 또한 운영단계에선 아래와 같은 기능을 지원하기 위한 고민을 충분히 해야한다.  

- **가용성**  
  측정데이터 수신 지원  
  측정데이터 쿼리 지원  
  측정데이터 복구 지원  
- **확장성**  
  측정 서버의 개별적 확장 지원  
- **데이터보존**  
  측정데이터 보존기간 설정  
  측정데이터의 수집 축소(샘플링 설정)  
  스토리지 유형 설정  
- **개인정보 보호규정**  
  측정데이터 저장 위치  
  측정데이터 암호화  
  측정데이터 내부 개인정보 가명화(pseudonymization)  

대부분의 클라우드 업체에서 아래와 같은 백엔드 서비스를 운영하며 위와 같은 고민사항들을 커스터마이징할 수 있다.  

- GCP Stackdriver  
- AWS Cloudwatch, X-ray  

베어메탈 k8s 에서도 Observability 를 위한 오픈소스 백엔드가 위와 같은 고민사항을 고려하고 개발되고 있다.  

## 메트릭

> <https://opentelemetry.io/docs/concepts/signals/metrics/>

메트릭 정보를 사용해서 아래와 같은 성능 지표를 측정 가능하다.  

- **SLI(service level indicator: 서비스 수준 지시자)**  
  성능 측정 지표  
- **SLO(service level objective: 서비스 수준 목표)**  
  오류와 장애 규모 측정 지표  
- **SLA(service level agreement: 서비스 수준 협약서)**  
  서드파티 의존성에 의한 가용성 영향도  

`OTEL` 에서 매트릭 시그널의 포맷을 통합, 오픈소스 커뮤니티의 사용 사례가 표준을 통해 이해되고 해결될 수 있도록 지원한다.  

### 메트릭 파이프라인  

기타 `OTEL 시그널` 과 동일하게 `프로바이더 → 생성기(미터) → 처리기(뷰) → 익스포터` 형태로 파이프라인이 구성된다.  

- MeterProvider : 메트릭의 생성 방식을 결정하는 프로바이더  
- Meter : 측정값을 기록하기 위한 계측기(instrument).
- View : 생성된 메트릭을 필터링하여 처리.
- MetricReader : 기록된 메트릭을 읽어드림.
- MetricExporter : 메트릭을 다양한 프로토콜에서 활용할 수 있는 출력 형식으로 변환.  

```py
def configure_meter_provider():
    # 익스포터 생성
    exporter = ConsoleMetricExporter()
    reader = PeriodicExportingMetricReader(exporter, export_interval_millis=5000)
    # 뷰 생성
    view = View(
        instrument_type=Counter,
        attribute_keys=[],
        name="sold",
        description="total items sold",
        aggregation=LastValueAggregation(),
    )
    # 프로바이더 생성
    provider = MeterProvider(
        metric_readers=[reader],
        resource=Resource.create(),
        views=[view],
        enable_default_view=False,
    )
    set_meter_provider(provider)

if __name__ == "__main__":
    configure_meter_provider()
    # Meter 생성
    meter = get_meter_provider().get_meter(
        name="metric-example", # app 식별 이름
        version="0.1.2", # app 식별 버전
        schema_url=" https://opentelemetry.io/schemas/1.9.0",
    )
    inventory_counter = meter.create_up_down_counter(
      name="inventory",
      unit="items",
      description="Number of items in inventory",
    )
```

일반적으로 메트릭에는 아래와 같은 데이터가 포함된다.  

- 진행 중인 현재 스팬의 추적 ID  
- 진행 중인 현재 스팬의 스팬 ID  
- 측정된 이벤트의 타임스탬프  
- 모범 사례와 관련된 속성  
- 기록되고 있는 값  

### push base, pull base

> <https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/exporter/prometheusexporter/README.md>  
> <https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/exporter/prometheusremotewriteexporter/README.md>  
> <https://prometheus.io/docs/prometheus/latest/storage/#overview>  
> the remote write receiver endpoint is `/api/v1/write`

메트릭 정보를 추출할 때 `[push base, pull base]` 방법을 사용할 수 있다.  

`프로메테우스 익스포터`가 대표적인 `pull base`, `Prometheus Remote Write` 기능을 사용하면 `push base` 방식도 지원한다.  

`OTEL` 과 `Prometheus` 의 데이터 형식은 서로 다르지만 통합하여 함께 동작 가능하다.  

`OTEL 컬렉터` 에서 `Prometheus remote write` 를 사용하여 메트릭 정보를 내보내도록 구성할 수 있다(`push base`).  
반대로 `Prometheus` 와 같은 백엔드 서비스에서 `OTEL 컬렉터` 가 가지고 있는 Metric 데이터를 가져갈 수 있다(`pull base`).  

> `Prometheus remote write` 는 `Prometheus` 에서 default 로 제공해주지 않는 설정이기 때문에 추가구성을 해줘야 한다.  
> `Thanos Receiver`, `Cortex remote write` 기능을 사용하는 것도 가능.  

## 추적  

추적데이터는 **추적 컨텍스트(Trace Context)** 로 이루어진다.  
`추전 컨텍스트` 내부에는 스팬에 대한 상세정보를 담기위한 **스팬 컨텍스트(Span Context)** 가 포함되어있다.  

논리적으로 분리되어 있는 `[작업단위, 서비스]` 사이에 중요한 컨텍스트 정보를 전달 할 수 있는 **전파기능**을 제공한다.  

`추적 컨텍스트` 를 전파하는 과정을 가시화 해서 아래 그림과 같이 모니터링할 수 있다.  

![1](/assets/monitoring/observability_2.png)  

### 추적 파이프라인

`OTEL` 의 기타 `시그널` 과 동일하게 `프로바이더 → 생성기 → 처리기(추적전파기) → 익스포터` 형태로 파이프라인이 구성된다.  

```py
# 초기설정
def configure_tracer():
  resource = Resource.create( # 추적기 주석정보
    { 
      "service.name": "shopper",
      "service.version": "0.1.2",
    }
  ) 
  provider = TracerProvider(resource=resource)
  provider = TracerProvider() # 프로바이더
  exporter = ConsoleSpanExporter() # 익스포터
  span_processor = SimpleSpanProcessor(exporter) # 처리기
  provider.add_span_processor(span_processor) 
  trace.set_tracer_provider(provider)
  return trace.get_tracer("shopper.py", "0.0.1")
```

```py
def browse():
  print("visiting the grocery store")

if __name__ == "__main__":
  tracer = configure_tracer()
  span = tracer.start_span("visit store") # parent span
  ctx = trace.set_span_in_context(span)
  token = context.attach(ctx)
  span2 = tracer.start_span("browse") # child span
  browse()
  span2.end()
  context.detach(token)
  span.end()
```

`컨텍스트 API` 함수를 사용해서 추적 스팬을 직접 관리하는 과정은 유지보수가 어렵고 번거롭다.  

`컨텍스트 매니저` 를 통해 추적하고자 하는 과정을 래핑할 수 있다.  
`추적 컨텍스트` 의 계층구조를 코드로 표현가능하다.  

```py
def browse():
  print("visiting the grocery store")

def add_item_to_cart(item):
  print("add {} to cart".format(item))

if __name__ == "__main__":
  tracer = configure_tracer()
  with tracer.start_as_current_span("visit store"):
    with tracer.start_as_current_span("browse"):
      browse()
      with tracer.start_as_current_span("add item to cart"):
        add_item_to_cart("orange")
```

데코레이트를 사용하면 비즈니스 코드와 추적 파이프라인을 위한 코드를 분리할 수 있다.  

```py
@tracer.start_as_current_span("browse")
def browse():
  print("visiting the grocery store")

@tracer.start_as_current_span("add item to cart")
def add_item_to_cart(item):
  print("add {} to cart".format(item))

@tracer.start_as_current_span("visit store")
def visit_store():
    browse()
```

생성기가 만들어내는 `[추적 컨텍스트, 스팬 컨텍스트]` 에는 아래와 같은 정보가 포함된다.  

추적 컨텍스트의 구성요소

- trace_id: 현재 추적 식별자  
- 요청 시작시간  
- 요청 지속시간 (RootSpan End Time - Start Time)  
- 요청에 기록된 서비스 수  
- 요청에 기록된 스팬 수  
- 요청에 기록된 스팬 계층 구조  

스팬 컨텍스트 구성요소

- span_id: 현재 스팬 식별자  
- trace_id: 현재 추적 식별자  
- trace_flags: 추적 전파 옵션  
- trace_state: 전파할 시스템별 정보  
- is_remote: 상위에서 전파되었는지 여부  

### 처리기

스팬마다 설정되는 `추적 파이프라인` 의 `처리기` 는 멀티스레드로 동작하여 메인스레드 지연을 최소화 해야한다.  

`SimpleSpanProcessor` 는 메인스레드에 포함되어 동작함으로 `BatchSpanProcessor` 사용을 권장한다.  

```py
# 초기설정
def configure_tracer():
  provider = TracerProvider() # 프로바이더
  exporter = ConsoleSpanExporter() # 익스포터
  span_processor = BatchSpanProcessor(exporter) # 처리기
```

`처리기` 에는 **전파기능**이 포함되어 하나의 요청이 네트워크 경계를 넘어 추적할 수 있도록 원격 요청시에 `추적 컨텍스트 전파` 한다.  

![1](/assets/monitoring/observability_3.png)  

`추적 컨텍스트 전파`는 **분산추적**의 핵심개념으로 `W3C Trace Context` 전파 형식을 따르며 `[jeager, ot-trace, B3]` 등의 전파기능을 처리기에서 지원한다.  

### 이벤트, 예외, 상태

추적 과정에서 스팬에서 발생하는 `[이벤트, 예외, 상태]` 를 기록하는 방법을 알아본다.  

```py
try:
  span.add_event("about to send a request") # 이벤트를 전달하는 메서드
  resp = requests.get(url, headers=headers)
  span.add_event("request sent", attributes={"url": url},timestamp=0)
  ...
  ...
  span.set_status(Status(StatusCode.OK)) # span 상태 저장
except Exception as err:
  span.record_exception(err) # 예외를 전달하는 메서드
  span.set_status(Status(StatusCode.ERROR)) # span 상태 저장
```

실시간으로 기록된 스팬의 `[이벤트, 예외, 상태]` 관측가능성을 한단계 업그레이드해준다.  

## 로그  

`OTEL 로그 시그널` 규격 6개  

| 범위명 | 설명                                                            |
| ------ | --------------------------------------------------------------- |
| TRACE  | 디버깅 이벤트 세부 정보(보통 기본 설정에서는 비활성화되어 있음) |
| DEBUG  | 디버깅 이벤트 정보                                              |
| INFO   | 정보성 이벤트(이벤트 발생 여부만 전달함)                        |
| WARN   | 경고 이벤트(오류는 아니지만 정보성 이벤트보다 중요함)           |
| ERROR  | 오류 이벤트(무언가 잘못되었을 때 발생함)                        |
| FATAL  | 애플리케이션 또는 시스템 중지와 같은 치명적인 오류 이벤트       |

### 로그 파이프라인  

기타 `OTEL 시그널` 과 동일하게 `프로바이더 → 생성기(로거) → 처리기 → 익스포터` 형태로 파이프라인이 구성된다.  

- LoggerProvider: Logger 생성자
- Logger: LogRecord 데이터를 생성하는 생성기
- LogRecordProcessor: LogRecord 데이터를 처리하고 백엔드 시스템으로 전송하기 위해 Logger 로 넘깁니다.

```py
def configure_logger(name, version):
    local_resource = LocalMachineResourceDetector().detect()
    resource = local_resource.merge(
        Resource.create(
            {
                ResourceAttributes.SERVICE_NAME: name,
                ResourceAttributes.SERVICE_VERSION: version,
            }
        )
    )
    # 프로바이더
    provider = LogEmitterProvider(resource=resource)
    set_log_emitter_provider(provider)
    # 익스포터
    exporter = ConsoleLogExporter()
    # 처리기
    provider.add_log_processor(BatchLogProcessor(exporter))
    # 생성기(로거)
    logger = logging.getLogger(name)
    logger.setLevel(logging.DEBUG)
    handler = OTELPHandler()
    logger.addHandler(handler)
    return logger
```

```py
if __name__ == "__main__":
    configure_log_emitter_provider()
    log_emitter = get_log_emitter_provider().get_log_emitter(
        "shopper",
        "0.1.2",
    )
    log_emitter.emit(
        LogRecord(
            timestamp=time.time_ns(),
            body="first log line",
            severity_number=SeverityNumber.INFO,
        )
    )
```

로그는 스팬에 속해있고, 스팬은 추적에 속해있다 보니 추적 컨텍스트, 스팬 컨텍스트와 관련된 많은 내용이 `LogRecord` 에 포함되어 있다.  

- trace_id : LogRecord 에 관한 추적 ID
- span_id : LogRecord 에 관한 스팬 ID
- trace_flags : LogRecord 생성시점의 trace_flags
- severity_text : 심각도를 나타내는 문자열
- severity_number : 심각도를 나타내는 값
- body : 기록된 로그 메시지의 내용
- resource : LogRecord 생산자 관련 주석
- attributes : LogRecord 관련 주석
- timestamp : LogRecord 생성 나노초  

## Opentelemetry 컬렉터

> <https://github.com/open-telemetry/opentelemetry-collector>  
> <https://opentelemetry.io/docs/collector/getting-started/>  
> 사진출처: <https://deploy.equinix.com/blog/getting-the-most-out-of-opentelemetry-collector/>

![1](/assets/monitoring/observability_4.png)  

`OTEL 컬렉터` 구조는 `[Receiver, Processer, Exporter]` 3가지로 구성된다.  

![1](/assets/monitoring/observability_5.png)  

- **Receiver**: `[jeager, zinkin, OLTP, prometheus]` 등 다양한 입력 포멧 데이터를 수집 및 OTLP 형식으로 변환.  
- **Processer**: 측정데이터 필터링, 변환(속성추가 등) 등의 보조 작업을 수행.  
- **Exporter**: OLTP 형식의 데이터를 출력 형식으로 변환 및 지정된 대상으로 전송.  

지금까지는 어플리케이션에서 자체적으로 파이프라인을 설정하고 벡엔드에 측정데이터를 전달했었다.  
`OTEL 컬렉터` 로 인해 환경이 통합되면서 설정이 간편해지고 효율적인 파이프라이닝이 가능해졌다.  
어플리케이션에서 각종 백엔드에 관측데이터를 전송하기 위한 `[라이브러리, 주소, 프로토콜]` 을 관리할 필요 없이 `OTEL 컬렉터` 만 알고 있으면 된다.  

`OTEL 컬렉터` 를 사용하는 방식은 크게 3가지 방식이 있다.  

![1](/assets/monitoring/observability_6.png)  

- **sidecar**: 동일한 포드에 컬렉터를 배치, 어플리케이션과 동일한 리소스 및 수명주기 를 공유.  
- **agent**: 모든 노드에 컬렉터를 배치, 측정데이터를 일괄처리, 필터링, 전처리 가능.  
- **gateway**: 특정 노드에 컬렉터를 배치, 독립 실행 서비스로 운영되며 수평확장, 중앙제어 등이 가능.  

`sidecar -> agent -> gateway` 규모순으로, 각 위치에 배포되어 있는 컬렉터들간의 연동 또한 가능하다.  

규모가 작을수록 어플리케이션 입장에서 데이터 전송을 위한 네트워크 오버헤드가 줄어들고, 규모가 클수록 수집할 수 있는 데이터 종류가 늘어나며 관측 백엔드 와의 통신량을 줄일 수 있다.  

대부분 `[agent, gateway]` 방식중 하나를 사용한다.  

### OTEL 컬렉터 helm

> <https://opentelemetry.io/docs/kubernetes/helm/>  
> <https://opentelemetry.io/docs/kubernetes/getting-started/>  
> <https://github.com/open-telemetry/opentelemetry-helm-charts>  

`OTEL` 은 `CNCF` 규칙을 따르는 만큼 k8s 와 같은 클라우드 환경에서 주로 실행된다.  
k8s 클러스터 자체 관측데이터 수집, 어플리케이션 관측데이터 수집을 수행한다.  

일반적으로 `agent` 방식으로 관측데이터를 수집하기 때문에 `sideccar` 방식을 주로 사용한다.  

위 helm 차트에서 쉽게 `gateway` 방식으로 운영되는 `OTEL 컬렉터` 설치가 가능하다.  


```shell
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts

# 압축파일 다운로드, opentelemetry-collector-0.78.1.tgz 버전 설치됨
helm fetch open-telemetry/opentelemetry-collector

# 압축 파일 해제
tar zxvf opentelemetry-collector-*.tgz
mv opentelemetry-collector opentelemetry-collector-helm
```

`config` 설정에서 위에서 보았던 `OTEL 컬렉터`의 `[exporters, processors, receivers]` 3개 컴포넌트 설정이 가능하다.  

여기선 사전 설정한 `[Loki, Temp, Thanos]` 서비스에 대한 설정을 진행한다. 아래 url 참고.  

> <https://grafana.com/docs/opentelemetry/>  

여러 운영방식이 있겠지만 여기선 어플리케이션이 직접 전달한 `[메트릭, 로그, 추적]` 데이터만 gateway 방식으로 수집하기 위해 `deployment` 으로 운영.  

```yaml
# Valid values are "daemonset", "deployment", and "statefulset".
mode: "deployment"

# Specify which namespace should be used to deploy the resources into
namespaceOverride: "monitoring"

presets:
  # Configures the collector to collect logs.
  logsCollection:
    enabled: false # Kubernetes 컨테이너 런타임이 모든 컨테이너 로그 수집 X
    includeCollectorLogs: false # 자체로그 전달 X

...

config:
  exporters:
    debug: {}
    logging: {}
    loki:
      endpoint: http://loki-write.loki.svc.cluster.local:3100/loki/api/v1/push
      default_labels_enabled: # exporter, job, instance, level
        exporter: false
        job: true
    otlp/trace:
      endpoint: http://tempo.tempo.svc.cluster.local:4317 # use oltp protocol
      tls:
        insecure: true
        insecure_skip_verify: true 
    prometheusremotewrite: # push base, remote write
      endpoint: "http://prometheus-kube-prometheus-prometheus.prometheus.svc.cluster.local:9090/api/v1/write"
      external_labels:
        collector_label: ${env:MY_POD_IP}
  # ...
  processors:
    # ...
    attributes: # attribute 추가 프로세스
      actions:
        - action: insert # insert, update, upsert,
          key: loki.attribute.labels
          value: container
        - action: insert
          key: loki.format
          value: json # default value is json
    resource: # resource 추가 프로세스
      attributes:
        - action: insert
          key: loki.resource.labels
          value: service.name, service.namespace

  receivers:
    # ...
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317 # 모든 입력 IP 허용
        http:
          endpoint: 0.0.0.0:4318
  service:
    # ...
    pipelines:
      logs:
        exporters: [debug, loki]
        processors: [memory_limiter, batch, attributes, resource]
        receivers: [otlp]
      metrics:
        exporters: [debug, prometheusremotewrite]
        processors: [memory_limiter, batch]
        receivers: [otlp]
      traces:
        exporters: [debug, otlp/trace]
        processors: [memory_limiter, batch]
        receivers: [otlp]
```

> `config.extensions` 에선 인증 확장, 디버깅 인터페이스와 같은 추가 확장이 가능하다.  
> grafana cloud 와 같은 시스템에 인증된 요청을 하기 위해 설정해야함.  

```sh
kubectl create ns monitoring
helm install opentelemetry-collecor -f values.yaml . -n monitoring
```

### Operating Mode

> <https://github.com/open-telemetry/opentelemetry-operator>  
> <https://opentelemetry.io/docs/kubernetes/operator/>  
> <https://cert-manager.io/docs/installation/helm/>  

sidecar 방식은 위의 helm 차트만으로는 설치할 수 없다.  
OpenTelemetry 에서 제공하는 `k8s 커스텀 리소스` 를 설치하고 `Pod` 의 설치마다 `sidecar` 어플리케이션이 같이 동작하도록 설정해야한다.  

```shell
# cert-manager 설치
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.2/cert-manager.yaml
kubectl get all -n cert-manager

# opentelemetry-operator 설치
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/latest/download/opentelemetry-operator.yaml
kubectl get all -n opentelemetry-operator-system
```

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
          - job_name: 'otel-scrape'
            scrape_interval: 1m
            static_configs:
            - targets: ["0.0.0.0:9404"]
            metrics_path: "/actuator/prometheus"
    processors:
    exporters:
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

`sidecar` 의 모든 입력, 처리 결과를 `otlp` 로 출력하여 기존에 배포되어있는 `OTEL 게이트웨이` 에 전달한다.  

## 데모코드  

> <https://github.com/Kouzie/local-k8s>  
> <https://github.com/Kouzie/spring-kube-demo>  
