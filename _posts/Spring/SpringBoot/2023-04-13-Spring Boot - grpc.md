---
title:  "Spring Boot - gprc!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - springboot
---

## protobuf(protocol buffers)  

> mainpage: <https://developers.google.com/protocol-buffers>  

구글에서 개발한 데이터 전송을 빠르고 간편하게 하기 위한 정형화된 **데이터 구조**.  

`C++`, `C#`, `Dart`, `Go`, `Java`, `Python` 등 여러가지 언어 라이브러리를 지원한다.  

여러 프로토콜에서 사용되지만 `grpc` 라는 구글에서 만든 RPC 프로토콜에서 사용된다.  

`JSON` 과 같은 구조화된 데이터이지만 데이터 압출, 직렬화를 통해 보다 작고 빠르게 전송이 가능하다.  

사용하려면 `protobuf` 특유의 문법을 알아야 한다.  

> <https://protobuf.dev/programming-guides/proto3/>

### protoc (proto 컴파일러)

우리가 `XML`, `JSON`역시 일정한 구조를 가지고 전송해야 하듯이 `protobuf` 역시 이런 구조(문법)을 알고 데이터 구조 정의서를 작성해야 한다.  

`protobuf` 정의서를 `protoc` 로 컴파일 하면 언어별로 사용 가능한 데이터 클래스(코드)가 생성가능하다.  

컴파일 과정을 거치면 `grpc`가 제공하는 라이브러리로 전송할 수 있도록 **코드를 생성**해준다.  

`protoc` 의 소스와 실행파일은 아래 링크에서 다운가능하다.  

> <https://github.com/protocolbuffers/protobuf/releases>  
> 맥의 경우 `protoc-3.10.0-osx-x86_64.zip`를 설치하면 되겠다. (2019.10.16)  

`.proto` 확장자를 파일을 생성하고 `protobuf` 문법에 맞는 데이터 구조를 정의.  

```js
// AddressBookProtos.proto
syntax = "proto3";
package tutorial;

option java_multiple_files = true;
option java_package = "com.example.tutorial";
option java_outer_classname = "AddressBookProtos";

message Person {
   string name = 1;
   int32 id = 2;
   string email = 3;
  repeated PhoneNumber phones = 4;
  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }
  message PhoneNumber {
     string number = 1;
     PhoneType type = 2;
  }
}
message AddressBook {
  repeated Person people = 1;
}
```

정의가 끝났으면 다운받은 디렉터리로 이동해서 다음 명령어로 `.proto`파일을 컴파일  

```
protoc --proto_path=src --java_out=build/gen src/AddressBookProtos.proto
```  

`build/gen` 위치로 가보면 생각보다 많은 양의 java 파일이 생성된다.  

- AddressBook.java  
- AddressBookOrBuilder.java  
- AddressBookProtos.java  
- Person.java  
- PersonOrBuilder.java  

해당 객체의 데이터를 스트림으로 정방향/역방향 변환하는 java코드가 자동 `protoc` 컴파일러를 통해 자동 생성된다.  

## grpc

`grpc(Google Remote Procedure Call)` 는 구글이 최초로 개발한 오픈 소스 원격 프로시저 호출 시스템.  
전송을 위해 `HTTP/2` 프로토콜을 사용하고 `protobuf` 를 인터페이스 언어로 사용한다.  

![grpc1](/assets/springboot/springboot_grpc1.png)  

HTTP/2 의 기능을 적극 활용해서 아래와 같은 고급 기능을 제공한다.  

기능 | gRPC | JSON을 사용하는 HTTP API
|---|---|---|
**계약** | 필수(.proto) | 선택 사항(OpenAPI)
**프로토콜** | HTTP/2 | HTTP
**Payload** | Protobuf(소형, 이진) | JSON(대형, 사람이 읽을 수 있음)
**규범** | 엄격한 사양 | 느슨함. 모든 HTTP가 유효합니다.
**스트리밍** | 클라이언트, 서버, 양방향 | 클라이언트, 서버
**브라우저** 지원 | 아니요(gRPC-웹 필요) | 예
**보안** | 전송(TLS) | 전송(TLS)
**클라이언트 코드 생성** | 예 | OpenAPI + 타사 도구

`HTTP/2` 의 수명이 긴 실시간 통신 스트림을 통해 **통한 스트리밍**을 제공하는 점이 성능과 실시간 성에서 높은 우위를 가진다.  

### grpc 튜토리얼

아래 튜토리얼을 진행한다.  

> tutorial: <https://grpc.io/docs/languages/java/basics/>  
> git: <https://github.com/grpc/grpc-java/tree/master/examples/src/main/java/io/grpc/examples/routeguide>

해당 튜토리얼에서 사용하는 `route_guide.proto` 파일의 데이터형식과 서비스코드는 아래와 같다.  

**전송을 위핸 객체**  

- `Point`: 위경도객체  
- `Feature`: 지역객체(Point + name)  
- `Rectangle`: 사각형객체(Point + Point)  
- `FeatureDatabase`: 지역 데이터베이스객체(리스트)  
- `RouteNote`: Point 설명 객체(Point + message)  
- `RouteSummary`: 경로객체(수신받은 포인트, 해당되는 지역, 지역간 거리합)  

**전송시 사용되는 서비스**  

```js
service RouteGuide {
  // Point 에 해당하는 Feature 반환
  rpc GetFeature(Point) returns (Feature) {}
  // Rectangle 안의 Feature List 반환, server side streaming
  rpc ListFeatures(Rectangle) returns (stream Feature) {}
  // Point List 로 생성한 RouteSummary 반환, client side streaming
  rpc RecordRoute(stream Point) returns (RouteSummary) {}
  // bi streaming
  rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
}
```

> 한번에 여러개를 보내는것이 아닌 스트림 형식으로 데이터를 계속 보내기 때문에 `server side streaming`, `client side streaming` 이라는 단어를 사용한다.  

서버에 서비스에 해당하는 코드를 구현하고  
클라이언트에 서비스를 호출하는 코드를 구현한다.  

위 `grpc` 튜토리얼을 `Spring Boot` 에서 사용할수 있도록 데모 작성

`gradle` 에서 아래와 같이 설정하면 된다고 설명되어있다.  

```groovy
plugins {
    id 'com.google.protobuf' version '0.9.1'
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.21.7"
  }
  plugins {
    grpc {
      artifact = 'io.grpc:protoc-gen-grpc-java:1.54.1'
    }
  }
  generateProtoTasks {
    all()*.plugins {
      grpc {}
    }
  }
}
dependencies {
  runtimeOnly "io.grpc:grpc-netty-shaded:${grpcVersion}"
  implementation "io.grpc:grpc-protobuf:${grpcVersion}"
  implementation "io.grpc:grpc-stub:${grpcVersion}"
  // JsonFormat 사용시 protobuf-java-util 필요
  implementation "com.google.protobuf:protobuf-java-util:${protobufVersion}"
}
```

### RouteGuideUtil 작성  

샘플코드에서 사용하는 준비파일인 `route_guide_db.json` 파일을 DB 대용으로 사용  
주소를 가리키는 문자열과 그에 해당하는 위도, 경도값이 배열로 저장되어있다.  

> <https://github.com/grpc/grpc-java/blob/master/examples/src/main/resources/io/grpc/examples/routeguide/route_guide_db.json>

`Point`, `Feature` 데이터를 편하게 처리할 수 있는 유틸리티 코드 작성  

```java
public class RouteGuideUtil {
  private static final double COORD_FACTOR = 1e7; //10000000.000000

  // 위경도 변환
  public static double getLatitude(Point location) {
    return location.getLatitude() / COORD_FACTOR;
  }
  public static double getLongitude(Point location) {
    return location.getLongitude() / COORD_FACTOR;
  }
  // 스프링 부트에서 사용하다보니 조금 변경됨
  public static ClassPathResource getDefaultFeaturesFile() {
    ClassPathResource resource = new ClassPathResource("route_guide_db.json");
    return resource;
  }

  public static List<Feature> parseFeatures(ClassPathResource file) throws IOException {
    InputStream input = file.getInputStream();
    try {
      Reader reader = new InputStreamReader(input, Charset.forName("UTF-8"));
      try {
        FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
        JsonFormat.parser().merge(reader, database);
        return database.getFeatureList();
      } catch(Exception e) {
        e.printStackTrace();
      }
        finally {
        reader.close();
      }
    } finally {
      input.close();
    }
  return null;
  }

  public static boolean exists(Feature feature) {
    return feature != null && !feature.getName().isEmpty();
  }
}
```

`route_guide_db.json`파안의 데이터를 읽어와 `FeatureDatabase` 로 생성하고 `FeatureDatabase` 안의 `List<Feature>`를 반환한다.  

### 서버 - RouteGuideService 작성  

`route_guide.proto` 파일에 정의했던 서비스을 사용할 수 있도록 java 코드 작성  
자동으로 생성된 `RouteGuideGrpc.RouteGuideImplBase` 를 구현하여 서비스를 구성한다.  

```java
public class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {
  ...
}
```

서버에서 값을 반환하는 `getFeature`, `listFeatures` 를 보면 아래와 같다.  

```java
@Override
public void getFeature(Point request, StreamObserver<Feature> responseObserver) {
    responseObserver.onNext(checkFeature(request));
    responseObserver.onCompleted();
}

@Override
public void listFeatures(Rectangle request, StreamObserver<Feature> responseObserver) {
    int left = min(request.getLo().getLongitude(), request.getHi().getLongitude());
    int right = max(request.getLo().getLongitude(), request.getHi().getLongitude());
    int top = max(request.getLo().getLatitude(), request.getHi().getLatitude());
    int bottom = min(request.getLo().getLatitude(), request.getHi().getLatitude());

    for (Feature feature : features) {
        if (!RouteGuideUtil.exists(feature)) {
            continue;
        }

        int lat = feature.getLocation().getLatitude();
        int lon = feature.getLocation().getLongitude();
        // 해당 Rectangle 범위안에 부합하는 Feature 모두 전달
        if (lon >= left && lon <= right && lat >= bottom && lat <= top) {
            responseObserver.onNext(feature);
        }
    }
    responseObserver.onCompleted();
}
```

`return` 이 별도로 존재하지 않고 호출자에게 반환값을 전달할 때 `StreamObserver` 객체를 사용한다.  
`onNext` 로 값을 저장하고 `onCompleted` 를 호출하면 rpc 가 종료되는 구조이다.  

이번엔 `client side streaming` 형식으로 데이터를 보내는 `recordRoute`, `routeChat` 서비스를 구현해보자.  

```java
// rpc RecordRoute(stream Point) returns (RouteSummary) {}
@Override
public StreamObserver<Point> recordRoute(StreamObserver<RouteSummary> responseObserver) {
    return new StreamObserver<RouteSummary>() {
        int pointCount;
        int featureCount;
        int distance;
        Point previous;
        long startTime = System.nanoTime();

        @Override
        public void onNext(Point point) {
            pointCount++;
            if (RouteGuideUtil.exists(checkFeature(point)))
                featureCount++;
            // For each point after the first, add the incremental distance from the previous point
            // to the total distance value.
            if (previous != null) 
                distance += calcDistance(previous, point);
            previous = point;
        }

        @Override
        public void onError(Throwable t) {
            log.warn("Encountered error in recordRoute", t);
        }

        @Override
        public void onCompleted() {
            long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            responseObserver.onNext(RouteSummary.newBuilder().setPointCount(pointCount)
                    .setFeatureCount(featureCount).setDistance(distance)
                    .setElapsedTime((int) seconds).build());
            responseObserver.onCompleted();
        }
    };
}
// rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
@Override
public StreamObserver<RouteNote> routeChat(StreamObserver<RouteNote> responseObserver) {
    return new StreamObserver<RouteNote>() {
        @Override
        public void onNext(RouteNote note) {
            List<RouteNote> notes = getOrCreateNotes(note.getLocation());
            // Respond with all previous notes at this location.
            for (RouteNote prevNote : notes.toArray(new RouteNote[0])) {
                responseObserver.onNext(prevNote);
            }
            // Now add the new note to the list
            notes.add(note);
        }

        @Override
        public void onError(Throwable t) { log.warn("Encountered error in routeChat, {}", t.getMessage()); }
        @Override
        public void onCompleted() { responseObserver.onCompleted(); }
    };
}
```

`client side streaming` 형식 서비스는 `return` 문이 존재한다.  
두개의 `StreamObserver` 가 존재하며 입력값에 대한 `handler` 역할을 하는 구현체를 반환한다.  

### 서버 - RouteGuideServer 작성  

`Grpc.newServerBuilderForPort` 를 사용해 `grpc` 서버를 생성한다.  
내부적으로 `netty` 서버를 사용한다.  

```java
this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
    .addService(routeGuideService)
    .build();
this.server.start();
```

> 스프링 부트를 통해 grpc 서버를 실행하다 보니 튜토리얼 코드와 다르다.  
> 자세한 내용은 데모코드 참고  

### grpc 스텁종류

클라이언트 코드를 작성하기 전에 grpc 의 서비스를 호출하기 위한 **스텁**을 알아야한다.  
grpc 클라이언트는 서비스를 호출할 때 **스텁**을 사용하는데 3가지 종류가 있다.  

- **비동기 스텁**: 끝나면 `StreamObserver` 에 콜백하는 스텁  
- **블록킹 스텁**: 응답이 올때까지 기다리는 스텁  
- **퓨처 스텁**: `GrpcFuture<T>`을 반환하는 스텁  

그리고 4개의 통신방식을 지원한다.  

- **unary** (1개 request , 1개 respone)  
- **server stream** (1개 request, n개 response)  
- **client stream** (n개 request, 1개 response)  
- **bi stream** (n개 request, n개 response)  

스텁종류와 통신방식에 따라 12가지 호출방식을 지원해야 하지만  
몇몇 통신방식에서 특정 스텁을 지원하지 않는경우가 있으니 주의  

| | unary | server stream | client stream | bi stream |
|---|:---:|:---:|:---:|:---:|
**asyn** | o | o | o | o
**blocking** | o | o | x | x
**future** | o | x | x | x

### 클라이언트 - RouteGuideClient 작성

스텁 사용도 마찬가지로 자동으로 생성된 코드를 사용한다.  

`route_guide.proto` 로 인해 생성된 코드를 보면 위에서 설명한 3가지 스텁별로 클래스가 생성된것을 확인 가능하다.  

- **asyn**: `RouteGuideStub`  
- **blocking**: `RouteGuideBlockingStub`  
- **future**: `RouteGuideFutureStub`  

내부를 살펴보면 통신방식 지원별로 메서드가 생성되어 있다.  

`GetFeature`, `ListFeatures` 는 `RouteGuideBlockingStub` 클래스를 사용하고  
`RecordRoute`, `RouteChat` 는 `RouteGuideStub` 클래스를 사용하여 구성한다.  

`blocking` 스텁을 사용하는 `GetFeature`, `ListFeatures` 메서드는 코드가 간결하다.  
`return` 문이 정의되어 있음으로 받아서 사용하면 된다.  

```java
public Feature getFeature(int lat, int lon) {
    Point request = Point.newBuilder().setLatitude(lat).setLongitude(lon).build();
    Feature feature = blockingStub.getFeature(request);
    return feature;
}

public Iterator<Feature> listFeatures(int lowLat, int lowLon, int hiLat, int hiLon) {
    Rectangle request = Rectangle.newBuilder()
        .setLo(Point.newBuilder().setLatitude(lowLat).setLongitude(lowLon).build())
        .setHi(Point.newBuilder().setLatitude(hiLat).setLongitude(hiLon).build())
        .build();
    Iterator<Feature> features = blockingStub.listFeatures(request);
    return features;
}
```

반면 `RecordRoute`, `RouteChat` 함수는 `client stream`, `bi stream` 이다 보니 `async` 스텁을 사용해야 한다.  

단순 요청, 응답 형식의 API 가 아니고 `stream` 통로를 통해 데이터를 주고 받다보니 `return` 문이 없고  
요청, 응답에 사용할 핸들러 객체 `StreamObserver` 를 사전 정의하고 호출해야 한다.  

```java
// rpc RecordRoute(stream Point) returns (RouteSummary) {}
public StreamObserver<Point> recordRoute(StreamObserver<RouteSummary> responseObserver)

// rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
public StreamObserver<RouteNote> routeChat(StreamObserver<RouteNote> responseObserver)
```

위의 자동생성된 async 스텁 메서드를 보면  
매개변수로 응답 핸들러 객체가 사용되고  
반환값으로 요청 핸들러 객체가 사용된다.  

```java
public void recordRoute(List<Point> pointList) throws InterruptedException {
    StreamObserver<RouteSummary> responseObserver = new StreamObserver<>() {
        @Override
        public void onNext(RouteSummary summary) {
            info("Finished trip with {} points. Passed {} features. Travelled {} meters. It took {} seconds.",
                summary.getPointCount(), summary.getFeatureCount(), summary.getDistance(), summary.getElapsedTime());
        }

        @Override
        public void onError(Throwable t) {
            warning("RecordRoute Failed: {}", Status.fromThrowable(t));
        }

        @Override
        public void onCompleted() {
            info("Finished RecordRoute");
        }
    };

    StreamObserver<Point> requestObserver = asyncStub.recordRoute(responseObserver);
    for (Point point : pointList) {
        requestObserver.onNext(point);
        Thread.sleep(1500);
    }
    // Mark the end of requests
    requestObserver.onCompleted();
}

public void routeChat(RouteNote[] routeNotes) {
    final CountDownLatch finishLatch = new CountDownLatch(1);
    StreamObserver<RouteNote> responseObserver = new StreamObserver<>() {
        @Override
        public void onNext(RouteNote note) {
            info("Got message \"{}\" at {}, {}", note.getMessage(), note.getLocation().getLatitude(), note.getLocation().getLongitude());
        }

        @Override
        public void onError(Throwable t) { warning("RouteChat Failed: {}", Status.fromThrowable(t)); }

        @Override
        public void onCompleted() { info("Finished RouteChat"); }
    };
    StreamObserver<RouteNote> requestObserver = asyncStub.routeChat(responseObserver);
    for (RouteNote routeNote : routeNotes) {
        info("Sending message \"{}\" at {}, {}", 
            routeNote.getMessage(), routeNote.getLocation().getLatitude(), routeNote.getLocation().getLongitude());
        requestObserver.onNext(routeNote);
    }
    requestObserver.onCompleted();
}
```

서버로 부터 받는 값은 단순 `log` 출력만 진행.

## 데모코드  

tutorial 코드에서 spring boot 에서 사용할 수 있도록 약간 변경

> <https://github.com/Kouzie/spring-boot-demo/tree/main/grpc-demo>