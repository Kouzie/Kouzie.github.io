---
title:  "구글 grpc, protocol buffers!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---


## protocol buffers 

데이터를 빠르고 간편하게 전송하기 위해 개발한 데이터 전송을 위한 정형화된 데이터 구조.  
`grpc`라는 구글에서 만든 원격 프로시저 호출 프로토콜에서 사용된다. 

일종의 XML, JSON과 같은 구조화된 데이터이지만 압축과 같은 원리로 보다 작고 빠르게 전송이 가능하다.  

구글에서 여러가지 언어로 사용할 수 있도록 라이브러리를 만들어 주었다.   
지원 언어: `C++`, `C#`, `Dart`, `Go`, `Java`, `Python` 등

> mainpage: https://developers.google.com/protocol-buffers  
> tutorial: https://grpc.io/docs/tutorials/basic/java/  

### protoc - proto파일 컴파일러

`grpc`를 사용하기 위해선 `grpc`가 사용하는 정의서를 작성할 줄 알아야 한다.  

우리가 `XML`, `JSON`역시 일정한 구조를 가지고 전송해야 하듯이 `grpc` 역시 이런 구조(문법)을 알고  
`protoc`확장자의 데이터 구조 정의서를 작성하여 전송을 위한 데이터객체로 컴파일 후에 해당 객체를 사용해 데이터를 전송한다.  

컴파일 과정을 거치면 `grpc`가 제공하는 라이브러리로 전송할 수 있도록 **코드를 생성**해준다.  

컴파일러(`protoc`)의 소스와 실행파일은 아래 링크에서 다운가능하다.   
> https://github.com/protocolbuffers/protobuf/releases  

맥의 경우 `protoc-3.10.0-osx-x86_64.zip`를 설치하면 되겠다. (2019.10.16)  

`.proto`확장자를 파일을 생성하고 grpc문법에 맞는 데이터 구조를 정의.  

```js
syntax = "proto3";
package tutorial;

option java_multiple_files = true;
option java_package = "com.example.tutorial";
option java_outer_classname = "AddressBookProtos";

message Person {
   string name =1;
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
~/protoc-3.10.0-osx-x86_64 $ bin/protoc --proto_path=src --java_out=build/gen src/AddressBookProtos.proto
```  

생성 위치로 가보면 생각보다 많은 양의 java소스파일이 생성되어있다.  

`AddressBook.java`  
`AddressBookOrBuilder.java`  
`AddressBookProtos.java`  
`Person.java`  
`PersonOrBuilder.java`  

해당 객체의 데이터를 스트림으로 정방향/역방향 변환하는 java코드가 자동 `protoc` 컴파일러를 통해 자동 생성된다.  

이제 `grpc`프로토콜을 사용해 해당 객체를 송/수신 하기만 하면 된다.  

> 문제가 하나 있는데 단순 객체만 생성하지 grpc 서비스 객체를 생성하지 못한다는 것, 서비스 객체를 생성하기 위해선 `protoc-gen-grpc-java` 이라는 플러그인을 설치해야 한다.  
플러그인을 설치하고 `--plugin=protoc-gen-grpc-java=`, `--grpc-java_out=` 을 설정해야 한다. https://github.com/grpc/grpc-java/tree/master/compiler 참고  
https://stackoverflow.com/questions/31029675/protoc-not-generating-service-stub-files  
하지만 위 방식대로 해도 바로 실행가능한 정상코드가 나오지 않기에 튜토리얼의 패키지 관리자를 통해 grpc 구현 클래스들을 생성하도록 하자.  


## grpc

![grpc1](/assets/2019/grpc1.png){: .shadow}  

위에서 `protoc`를 사용해 생성한 java 코드를 사용해 `grpc`라이브러를 사용해 데이터를 전송하기만 하면 된다.  

공식 홈페이지의 샘플코드를 사용해 어떤식으로 grpc가 사용되는지 알아보자.   
> https://grpc.io/docs/tutorials/basic/java/   

grpc에 대해 좀더 알아보고 싶다면 아래 유투브를 시청  
> https://www.youtube.com/watch?v=sKWy7BJxIas&t=2082s  

## grpc 환경설정  

위의 샘플 예제는 일반적인 자바 프로젝트로 maven을 사용해 간단히 위 코드 실행해보자,  
`server`와 `client`역할을 해줄 프로젝트 2개를 생성   

`mvn archetype:generate -DgroupId=example.grpc -DartifactId=grpc-client -DarchetypeArtifactId=maven-archetype-quickstart`  
`mvn archetype:generate -DgroupId=example.grpc -DartifactId=grpc-server -DarchetypeArtifactId=maven-archetype-quickstart`  

두개의 프로젝트 모두 `pom.xml`은 아래와 같이 작성한다.  

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>example.grpc</groupId>
  <artifactId>grpc-client</artifactId>
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>grpc-client</name>
  <url>http://maven.apache.org</url>

  <properties>
    <os.detected.classifier>osx-x86_64</os.detected.classifier>
    <grpc.version>1.15.0</grpc.version>
    <protobuf.version>3.9.0</protobuf.version>
    <protoc.version>3.9.0</protoc.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>3.8.1</version>
      <scope>test</scope>
    </dependency>

    <!-- grpc를 사용하기 위한 종속성 -->
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-netty</artifactId>
      <version>${grpc.version}</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-protobuf</artifactId>
      <version>${grpc.version}</version>
    </dependency>
    <dependency>
      <groupId>io.grpc</groupId>
      <artifactId>grpc-stub</artifactId>
      <version>${grpc.version}</version>
    </dependency>

    <!-- proto buffer데이터를 json, xml, object로 포팅 -->
    <dependency>
      <groupId>com.google.protobuf</groupId>
      <artifactId>protobuf-java-util</artifactId>
      <version>${protobuf.version}</version>
    </dependency>
    <!-- proto에서 자바 컴파일후 소스를 사용하기 위한 종속성 -->
    <dependency>
      <groupId>com.google.protobuf</groupId>
      <artifactId>protobuf-java</artifactId>
      <version>${protobuf.version}</version>
    </dependency>
  </dependencies>

  <build>
    <finalName>demoProtoClient</finalName>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
      </resource>
    </resources>
    <plugins>
      <!-- 자동으로 protoc컴파일러를 통해 관련 java클래스 생성 -->
      <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>0.6.1</version>
        <configuration>
          <protocArtifact>com.google.protobuf:protoc:${protoc.version}:exe:${os.detected.classifier}
          </protocArtifact>
          <pluginId>grpc-java</pluginId>
          <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}
          </pluginArtifact>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>compile</goal>
              <goal>compile-custom</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <!-- main클래스를 찾지 못해 지정 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.0.2</version>
        <configuration>
          <archive>
            <manifest>
              <mainClass>example.proto.MainClass</mainClass>
              <addClasspath>true</addClasspath>
              <addExtensions>true</addExtensions>
              <packageName>example.proto</packageName>
            </manifest>
          </archive>
        </configuration>
      </plugin>
      <!-- jar파일에 종속된 dependency의 jar들이 기본적으로 포함되지 않는데 포함되도록 설정 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>1.6</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <!-- 자바 버전이 다를때 override에러가 발생, 상속받는 클래스의 추상메서드를 찾지 못함 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.6.1</version>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
      <!-- 자동생성된 proto파일도 컴파일 시에 소스파일로 사용 -->
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>generate-sources</phase>
            <goals>
              <goal>add-source</goal>
            </goals>
            <configuration>
              <sources>
                <source>${project.build.directory}/generated-sources/wrappers</source>
              </sources>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

`os.detected.classifier`, `finalName`과 같은 속성은 프로젝트, 개발환경에 맞게 변경해주자.  

다행이 `org.xolstice.maven.plugins`을 통해 **자동으로 proto파일을 찾아 java객체로 만들어준다.**  
proto파일은 `src/main/proto`폴더 아래에 저장해두면 된다.  

아래 `proto`을 작성, 아래링크에서 구할 수있다.  
> https://github.com/grpc/grpc-java/blob/master/examples/src/main/proto/route_guide.proto  


```js
syntax = "proto3";
option java_multiple_files = true;
option java_package = "example.grpc.routeguide";
option java_outer_classname = "RouteGuideProto";
option objc_class_prefix = "RTG";

package routeguide;

service RouteGuide {
  rpc GetFeature(Point) returns (Feature) {}

  rpc ListFeatures(Rectangle) returns (stream Feature) {}

  rpc RecordRoute(stream Point) returns (RouteSummary) {}

  rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
}

message Point {
  int32 latitude = 1;
  int32 longitude = 2;
}

message Rectangle {
  Point lo = 1;
  Point hi = 2;
}

message Feature {
  string name = 1;
  Point location = 2;
}

message FeatureDatabase {
  repeated Feature feature = 1;
}

message RouteNote {
  Point location = 1;
  string message = 2;
}

message RouteSummary {
  int32 point_count = 1;
  int32 feature_count = 2;
  int32 distance = 3;
  int32 elapsed_time = 4;
}
```

전송을 위핸 객체: `Point, Rectangle, Feature, FeatureDatabase, RouteNote, RouteSummary`  
전송시 사용되는 메서드  
```js
rpc GetFeature(Point) returns (Feature) {}
rpc ListFeatures(Rectangle) returns (stream Feature) {}
rpc RecordRoute(stream Point) returns (RouteSummary) {}
rpc RouteChat(stream RouteNote) returns (stream RouteNote) {}
```

> 간단히 설명하자면 `rpc ListFeatures(Rectangle) returns (stream Feature) {}`의 경우 클라이언트로부터 `Rectangle`객체를 받아 `Feature`를 반환한다.   
앞에 `stream` 키워드가 붙어있는데 하나가 아니라 여러개의 `Feature`객체를 보낸다 생각하면 된다.  


이상태에서 `target/generated-sources`폴더에 proto파일에 해당하는 java파일이 자동으로 생성되었는지 확인, 안됐다면 `mvn package`와 같은 명령을 실행하면 생길것이다.  
![grpc2](/assets/2019/grpc2.png){: .shadow}  

이제 서버는 클라이언트가 전송하는 값을 조작해 반환하고  
클라이언트는 서버의 어떤 메서드를 호출하는지 지정하면 된다.  

그리고 샘플코드에서 사용하는 준비파일인 `route_guide_db.json`파일을 `src/main/resources`에 저장,  
`pom.xml`에서 이미 `<directory>src/main/resources</directory>`을 통해 리소스 파일위치를 지정해 두었다.  

`route_guide_db.json` 데이터는 아래 링크에서 확인  
> https://github.com/grpc/grpc-java/blob/master/examples/src/main/resources/io/grpc/examples/routeguide/route_guide_db.json

주소를 가리키는 문자열과 그에 해당하는 위도, 경도값이 배열로 저장되어있다.(왜인지 위도 경도가 십진수의 매우 큰값으로 설정되어 있다. 변환작업 필요)  

여기까지는 서버와 클라이언트 공통작업임으로 같이 진행해야한다.  

> 진행하면서 오류가 발생할 수 있는데 `mvn clean`을 자주 진행....

### Creating the server

이제 위의 `.proto`파일에서 정의했던 `전송시 사용되는 메서드`를 정의해보자.  

grpc에선 이를 메서드라 하지 않고 스텁이라 하는데 스텁에는 3가지 종류가 있다.   

* 끝나면 `StreamObserver`에 콜백하는 **비동기 스텁**  

* 응답이 올때까지 기다리는 **블록킹 스텁**  

* `GrpcFuture<T>`을 반환하는 **Future 스텁**, `GrpcFuture`는 `ListenableFuture`를 상속한다.  

### Server - RouteGuideUtil 작성  

```java
package example.grpc.routeguide;
import ...;

public class RouteGuideUtil {
  private static final double COORD_FACTOR = 1e7; //10000000.000000

  public static double getLatitude(Point location) {
    return location.getLatitude() / COORD_FACTOR;
  }

  public static double getLongitude(Point location) {
    return location.getLongitude() / COORD_FACTOR;
  }

  public static URL getDefaultFeaturesFile() {
    return RouteGuideServer.class.getResource("/route_guide_db.json");
  }

  public static List<Feature> parseFeatures(URL file) throws IOException {
    InputStream input = file.openStream();
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

`route_guide_db.json`파안의 데이터를 읽어와 `FeatureDatabase`로 생성하고 `FeatureDatabase`안의  `List<Feature>`를 반환한다.   
> `JsonFormat`을 사용하려면 `protobuf-java-util`를 dependency에 추가.  

그리고 10진수 위도 경도로 변환하는 `getLatitude`, `getLongitude`메서드가 정의되어있다.  

### Server - RouteGuideServer 작성  

실제 클라이언트와 통신하는 `RouteGuideServer`를 생성하고 `RouteGuideService`에서 `.proto`파일에 정의했던 stub 메서드를 정의하자.  
grpc라이브러리의 `ServerBuilder`를 사용해 grpc서버를 생성한다.  

> 원본: https://github.com/grpc/grpc-java/blob/master/examples/src/main/java/io/grpc/examples/routeguide/RouteGuideServer.java

간단히 `listFeatures`와 `recordRoute`만 어떤식으로 데이터를 수신/전송하는지 알아보자.  
```
rpc ListFeatures(Rectangle) returns (stream Feature) {}
rpc RecordRoute(stream Point) returns (RouteSummary) {}

message Rectangle {
  Point lo = 1;
  Point hi = 2;
}
message Feature {
  string name = 1;
  Point location = 2;
}
message Point {
  int32 latitude = 1;
  int32 longitude = 2;
}
message RouteSummary {
  int32 point_count = 1;
  int32 feature_count = 2;
  int32 distance = 3;
  int32 elapsed_time = 4;
}
```

ListFeatures는 `Rectangle`을 수신받아 Feature를 여러개 반환한다.  
RecordRoute는 `Point`를 여러개 수신받아 `RouteSummary`를 반환한다.  

후의 client코드에서 보겠지만 `Rectangle`의 데이터는 아래와 같다.  

```js
Ractangle {
{Point1: {400000000, -750000000}},
{Point2: {420000000, -730000000}}
}
```
딱 봐도 저 범위안에있는 모든 `Feature`를 찾아서 클라이언트에게 반환해야할 것 같다.  

`RecordRoute` 스텁을 호출하는 클라이언트의 코드는 아래와 같다.  
`client.recordRoute(features, 10);`  

`features`에는 `route_guide_db.json`에서 읽어들인 모든 값이 저장되어 있는데 이중 10개를 랜덤으로 뽑아 Server에게 전송하고  
서버는 이를 받아 거리계산 후에 `RouteSummary`에 데이터를 담아 반환한다.  



```java
import ...;

public class RouteGuideServer {
  ...
  private static class RouteGuideService extends RouteGuideGrpc.RouteGuideImplBase {
    private final Collection<Feature> features;
    private final ConcurrentMap<Point, List<RouteNote>> routeNotes = new ConcurrentHashMap<Point, List<RouteNote>>();

    RouteGuideService(Collection<Feature> features) {
      this.features = features;
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
        if (lon >= left && lon <= right && lat >= bottom && lat <= top) {
          responseObserver.onNext(feature);
        }
      }
      responseObserver.onCompleted();
    }
    ...
```
`route_guide_db.json`안의 데이터가 저장되어있는 `features`의 값을 하나하나 꺼내 비교후 조건에 일치하면 
`responseObserver.onNext(feature)`를 호출한다.  

그럼 클라이언트로 `feature`가 가게 되고 클라이언트는 아래와 같이 Iterator로 수신을 기다리고 있다.  

```java
// 클라이언트 코드
features = blockingStub.listFeatures(request);
  for (int i = 1; features.hasNext(); i++) {
    ...
  }
...
```
`blockingStub`이기에 하나하나 받을때마다 for문이 하나씩 돌기 시작한다.  
그리고 서버에서 `responseObserver.onCompleted()`호출되어야 for문에서 빠져나간다.  


이어서 `recordRoute` 메서드를 살펴보자.  
클라이언트가 호출할땐 아래와 같이 스텁을 사용한다.  

```java
StreamObserver<RouteSummary> responseObserver = new StreamObserver<RouteSummary>() {
  ...
}
//연결 통로를 생성
StreamObserver<Point> requestObserver = asyncStub.recordRoute(responseObserver);

try {
    // Send numPoints points randomly selected from the features list.
    for (int i = 0; i < numPoints; ++i) {
        int index = random.nextInt(features.size());
        Point point = features.get(index).getLocation();
        info("Visiting point {0}, {1}", RouteGuideUtil.getLatitude(point), RouteGuideUtil.getLongitude(point));
        requestObserver.onNext(point);
        ...
    }
} ...
```
클라이언트도 서버에게 여러개의 point 클래스를 보내기에 `requestObserver.onNext(point)`메서드를 호출하고  
반면에 위의 `blockingStub`을 사용할 때와는 다르게 `asyncStub`을 사용하기에 비동기 콜백에 대응할 `responseObserver`를 만들어 같이 사용한다.  
(아래 client 코드 참고)  

`asyncStub`에선 연결통로를 생성했었다.  
`StreamObserver<Point> requestObserver = asyncStub.recordRoute(responseObserver)`  
서버에서도 이러한 연결통로를 생성하도록 `recordRoute`메서드를 를 정의한다.  
```java
    ...
    @Override
    public StreamObserver<Point> recordRoute(final StreamObserver<RouteSummary> responseObserver) {

      return new StreamObserver<Point>() {
        int pointCount;
        int featureCount;
        int distance;
        Point previous;
        long startTime = System.nanoTime();

        public void onNext(Point point) {
          pointCount++;
          if (RouteGuideUtil.exists(checkFeature(point))) {
            featureCount++;
          }
          if (previous != null) {
            distance += calcDistance(previous, point);
          }
          previous = point;
        }

        public void onError(Throwable t) {
          logger.log(Level.WARNING, "Encountered error in recordRoute", t);
        }

        public void onCompleted() {
          long seconds = NANOSECONDS.toSeconds(System.nanoTime() - startTime);
          responseObserver.onNext(RouteSummary
            .newBuilder()
            .setPointCount(pointCount)
            .setFeatureCount(featureCount)
            .setDistance(distance)
            .setElapsedTime((int) seconds)
            .build());
          responseObserver.onCompleted();
        }
      };
    }
    private Feature checkFeature(Point location) {
      for (Feature feature : features) {
        if (feature.getLocation().getLatitude() == location.getLatitude()
            && feature.getLocation().getLongitude() == location.getLongitude()) {
          return feature;
        }
      }
      return Feature.newBuilder().setName("").setLocation(location).build();
    }

    private static int calcDistance(Point start, Point end) {
      int r = 6371000; // earth radius in meters
      double lat1 = toRadians(RouteGuideUtil.getLatitude(start));
      double lat2 = toRadians(RouteGuideUtil.getLatitude(end));
      double lon1 = toRadians(RouteGuideUtil.getLongitude(start));
      double lon2 = toRadians(RouteGuideUtil.getLongitude(end));
      double deltaLat = lat2 - lat1;
      double deltaLon = lon2 - lon1;

      double a = sin(deltaLat / 2) * sin(deltaLat / 2)
              + cos(lat1) * cos(lat2) * sin(deltaLon / 2) * sin(deltaLon / 2);
      double c = 2 * atan2(sqrt(a), sqrt(1 - a));

      return (int) (r * c);
    }
  }
}
```

거리 계산 공식은 잘 모르겠다.... 

어쩃건 클라이언트가 `onNext`메서드를 통해 데이터를 계속 받다 클라이언트가 `onCompleted`를 호출하면  
서버도 `onNext`를 호출해 RouteSummary를 보내고 `onCompleted`로 스텁을 종료한다.  


메인클래스는 아래와 같이 작성, 포트를 지정하고 실행한다.  
```java
package example.grpc;
import example.grpc.routeguide.RouteGuideServer;
public class MainClass {

    public static void main(String[] args) throws Exception {
        RouteGuideServer server = new RouteGuideServer(8980); //서버를 실행할수 있는 RouteGuideServer 생성, feature를 List로 가지고 있음
        server.start();
        server.blockUntilShutdown();
    }
}
```
클라이언트의 코드는 생략한다.  


서버와 클라이언트 소스코드를 첨부하니 디버깅으로 확인하면서 참고  
(좀더 보기 쉽게 만들어 둔것을 제외하곤 구글에서 제공하는 example코드와 다른건 없습니다)  
> https://github.com/Kouzie/Kouzie.github.io/tree/master/assets/2019/grpc.zip
