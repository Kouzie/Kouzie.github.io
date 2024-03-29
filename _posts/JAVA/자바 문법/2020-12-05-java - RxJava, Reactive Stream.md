---
title:  "java - Reactive Stream, RxJava!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

## java9 - Flow

`CompletableFuture` 를 사용해 연산이 끝났음을 알리고(발행)  
이후에 동작할 연산을 미리 정의(구독) 하는 모델은 간단하지만  

어플리케이션이 커질수록 자바의 객체지향을 사용한 옵저버 패턴으로 변경해야 쉬운 유지보수가 가능하다.  

`java9` 에 추가된 `java.util.concurrent.Flow` 패키지를 사용해 옵저버 패턴을 기반으로하는 **리액티브 프로그래밍**이 가능하다.  

![image16](/assets/java/reactive-java/image16.png)  

`RxJava, Akka, Reactor` 등의 라이브러리들이 리액티브 프로그래밍을 위해 `java9 Flow` 패키지를 기반으로 개발되었다.  

`[Publisher, Subscriber, Subscription, Processor]` 인터페이스 들이 각 프레임워크, 라이브러리에 화려하게 구현되어 있으며 일반 개발자들도 위 인터페이스 규칙만 지키면 연동 가능한 코드 작성이 가능하다.  

![image17](/assets/java/reactive-java/image17.png)  

발행자는 구독자를 등록하고 subscription(신청) 할 데이터를 가져와 비즈니스 로직을 수행시킨다.  

```java
package java.util.concurrent;

public static interface Publisher<T> {
    public void subscribe(Subscriber<? super T> subscriber);
}

public static interface Subscriber<T> {
    public void onSubscribe(Subscription subscription);
    public void onNext(T item);
    public void onError(Throwable throwable);
    public void onComplete();
}
```

`Publisher` 가 발행하는 이벤트에 대한 콜백 메서드 4개  

* `onSubscribe` - 구독시작 콜백  
* `onNext` - 메세지 발행 콜백  
* `onError` - 에러 발행 콜백  
* `onComplete` - 완료 콜백  


```java
public static interface Processor<T,R> extends Subscriber<T>, Publisher<R> {
}
```

`Processor` 는 발행/구독자 상속으로 두가지 역할을 모두 수행한다.  

발행자와 구독자 사이에 껴서 데이터를 재가공 하는 역할이 가능하다.  

### 역압력  

발행자가 구독자에게 데이터를 밀어넣는(`onNext`) 것을 압력이라 한다.  

압력이 많아지면 시스템에 부담이 가기에  
요청했을 때만 압력이 쏟아지도록 하는 것, 압력을 `pull` 하는 것을 역압력이라 한다.  

`Subscription`(신청자) 인터페이스의 `request` 메서드를 사용해 역압력 구현이 가능하다.  

```java
public static interface Subscription {
    public void request(long n);
    public void cancel();
}
```

* `request` - `Publisher` 에게 주어진 개수의 이벤트 처리 준비가 완료됨을 알림  
* `cancel` - 구독 취소

신청자를 통해 `Subscriber` 가 이벤트를 처리할 수 있도록 설정해야 한다.  

아래의 `TempSubscription` 처럼 
`Subscriber` 에서 `onSubscribe(Subscription subscription)` 형식으로 필드에 `Subscription`(신청자) 을 저장해두고  
신청자를 동해 구독자에게 특정 데이터를 전달하거나 특정 코드(`request`, `cancel`)를 전달할 수 있다.  

![image18](/assets/java/reactive-java/image18.png)  

1. 신청자(`Subscription`)가 설정된 공급자(`Publisher`)와 구독자(`Subscriber`) 생
성 및 `subscribe` 메서드로 구독자 등록  
2. 구독과 동시에 구독자의 `onSubscribe` 호출, 구독자에게 신청자 등록  
3. 공급자는 이제 신청자를 통해 공급자에게 이벤트 신청을 할 수 있음  

구독자가 신청자의 `request` 메서드를 통해 몇개 데이터를 처리할지 결정하고 신청자는 공급자로부터 데이터를 가져와 구독자에게 전달한다.  

구독자가 데이터를 땡겨오기에 시스템 압력조절이 가능하다.  

### 발행자 구독자 모델  

```java
public static void main(String[] args) {
    Publisher<TempInfo> newYorkTempPub = new Publisher<TempInfo>() { // 발행자 생성
        @Override
        public void subscribe(Flow.Subscriber<? super TempInfo> subscriber) {
            // 구독자에게 TempSubscription 을 전송
            subscriber.onSubscribe(new TempSubscription(subscriber, "New York"));
        }
    };

    TempSubscriber tempSubscriber = new TempSubscriber();
    newYorkTempPub.subscribe(tempSubscriber);
}
```

발행자를 구독하는 순간 아래와 같은 `onSubscribe()` 메서드가 호출된다.  

```java
public class TempSubscriber implements Subscriber<TempInfo> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        // 구독 이벤트 실행
        this.subscription = subscription;
        subscription.request(1);
    }
    @Override
    public void onError(Throwable t) {
        System.err.println(t.getMessage()); // 에러 출력
    }
    @Override
    public void onNext(TempInfo tempInfo) {
        System.out.println(tempInfo);
        subscription.request(1);
    }
    @Override
    public void onComplete() {
        System.out.println("Done!");
    }
}
```

`subscription` 의 `request()` 메서드가 호출되고 위에서 생성한  
`new TempSubscription(subscriber, "New York")` 의 `request` 구현 메서드가 호출된다.
구현된 `request` 는 아래와 같다.  

```java
@RequiredArgsConstructor
public class TempSubscription implements Subscription {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Subscriber<? super TempInfo> subscriber;
    private final String town;

    @Override
    public void request(long n) {
        executor.submit(() -> {
            for (long i = 0L; i < n; i++) {
                System.out.println("N:" + n);
                try {
                    subscriber.onNext(TempInfo.fetch(town));
                } catch (Exception e) {
                    subscriber.onError(e);
                    // executor.shutdown();
                    break;
                }
            }
        });
    }
    ...
}
```

`onNext` 를 통해 구독자에게 데이터(메세지)를 발행하고  
예외가 발생하면 `onError` 를 통해 구독자에게 에러를 발행한다.  

위의 `onNext` 내부를 보면 메세지 수신 이벤트를 실행하고 다시 `request` 메서드를 호출함으로 재귀 루프가 형성된다.  

반면 onError 의 경우 에러 출력만 하고 이후의 처리가 없다.  

`TempInfo` 는 아래 참고  

```java
@Getter
@AllArgsConstructor
public class TempInfo {
    private final String town;
    private final int temp;
    public static TempInfo fetch(String town) {
        if (rndBound(10) == 0) { // 10 분의 1 확률로 에러 발생
            throw new RuntimeException("Error!");
        return new TempInfo(town, rndBound(100));
    }
    @Override
    public String toString() { return town + " : " + temp; }
}
```

순서는 간략히 아래와 같다.  

1. `Publisher` 정의  
2. `TempSubscriber` 생성 및 `Publisher` 구독  
3. 구독과 동시에 구독 이벤트 콜백으로 전달한 `TempSubscription` 의 코드(`request`) 실행  
4. `request` 내부에서 `onNext`, `onNext` 내부에서 `request` 를 번갈아가면 무한 재귀 호출  
5. `request` 에러 발생시 `onError` 호출 후 재귀 종료  

맨위의 `main` 을 실행시키면 아래와 같이 실행되다 1/10 확율로 에러가 발생하여 프로그램이 정지된다.

```
N:1
New York : 24
N:1
New York : 1
N:1
New York : 8
N:1
Error!
```

`TempSubscriber` 의 `onNext` 메서드 `TempSubscription` 의 `request` 를 호출하면서 역압력형식으로 구성된다.  
만약 구독자가 많은 시스템 부담을 느낀다면 request 호출을 제거하면 된다.  

## RxJava

> <http://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html>

**넷플릭스**에서 리액티브 프로그래밍을 위해 개발한 라이브러리로 `java9` 이 업데이트 되기 전에 개발되었다.  

`RxJava 2.0` 부터 `java9 Flow` 패키지의 인터페이스를 구현하도록 업데이트 되었으며  
많은 기업들이 `java9` 에서 제공한 표준을 사용해 리액티브 프로그래밍 라이브러리를 업데이트 했다.  

`RxJava` 에선 `Observable` 클래스가 공급자 역할, `Observer` 클래스가 구독자 역할을 한다.  

```java
package io.reactivex;

public interface ObservableSource<T> {
    void subscribe(@NonNull Observer<? super T> observer);
}

// 공급자
public abstract class Observable<T> implements ObservableSource<T> {
    ...
}

// 구독자
public interface Observer<T> {
    void onSubscribe(@NonNull Disposable var1);
    void onNext(@NonNull T var1);
    void onError(@NonNull Throwable var1);
    void onComplete();
}
```


다양한 방법으로 `Observable`(공급자) 생성 가능하고 구독자 인터페이스에서 `onNext` 메서드만 구현하면 바로 `Observable` 을 `Observer` 에 등록 가능하다.  

```java
public static void main(String[] args) {
    // 1 초 간격으로 long 값을 1에서 무한 증가 값을 방출
    Observable<Long> onePerSec = Observable.interval(1, TimeUnit.SECONDS);

    // 한개 이상의 요소를 방출하는 Observable 생성
    Observable<String> strings  = Observable.just("first", "second");
    // onNext("first"), onNext("second"), onComplete() 가 차례대로 호출됨
}
```

아래처럼 `Observer` 의 `onNext` 를 제외한 `onError`, `onComplete`, `onSubscribe` 는 기본 구현체를 사용할 수 있다.  

`subscribe` 오버라이딩 메서드는 모두 `Disposable` 객체를 반환한다.  

```java
public final Disposable subscribe(Consumer<? super T> onNext) {
    return this.subscribe(onNext, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer());
}

... 

public interface Disposable {
    void dispose();
    boolean isDisposed();
}
```

`dispose()` 메서드는 공급자가 더이상 데이터를 발행하지 않도록 구독관계를 해지하는 역할을 하며 `Observer` 의 `onComplete` 가 호출되면 자동으로 같이 호출된다.  

```java
// 0 ~ 1 초 간격으로 long 값을 무한 증가 값을 방출
Observable<Long> onePerSec = Observable.interval(1, TimeUnit.SECONDS);
onePerSec.subscribe(i -> {
    System.out.println("num:" + i);
    if (i == 3)
        throw new IllegalArgumentException("number is three");
});
System.out.println("test success");
Thread.sleep(10000); 
// rxjava 에서 제공하는 스레드풀의 데몬 스레드로 실행되기에 sleep 으로 main 이 종료되지 않도록 설정 
// blockingSubscribe 사용하면 main 스레드로 subscribe 하지만 영원히 끝나지 않음
```

증가된 `long` 값이 3이 되면 에러를 반환  

```
io.reactivex.exceptions.OnErrorNotImplementedException: 
    The exception was not handled due to missing onError handler in the subscribe() method call. 
Further reading: https://github.com/ReactiveX/RxJava/wiki/Error-Handling | number is three
```

### RxJava 를 사용한 발행자 구독자 모델

구독자(`Subscriber`)에 해당하는 `Observer` 생성  

```java
public class TempObserver implements Observer<TempInfo> {
    @Override
    public void onSubscribe(Disposable disposable) { }
    @Override
    public void onError(Throwable t) { System.err.println(t.getMessage()); }
    @Override
    public void onNext(TempInfo tempInfo) { System.out.println(tempInfo); }
    @Override
    public void onComplete() { System.out.println("Done!"); }
}
```

공급자(`Publisher`)에 해당하는 `Observable` 생성

```java
public static void main(String[] args) {
    String town = "NewYork";
    Observable<TempInfo> observable = Observable.create(new ObservableOnSubscribe<TempInfo>() {
        @Override
        public void subscribe(ObservableEmitter<TempInfo> observableEmitter) throws Exception {
            Observable.interval(1, TimeUnit.SECONDS).subscribe(i -> { //매초마다 1 ~ n long 방출
                if (!observableEmitter.isDisposed()) {  // 구독자가 폐기되지 않았다면 수행
                    if (i >= 5) observableEmitter.onComplete(); // 구독자 완료처리
                    else {
                        try { observableEmitter.onNext(TempInfo.fetch(town)); }
                        catch (Exception e) { observableEmitter.onError(e); }
                    }
                }
            });
        }
    });
    observable.subscribe(new TempObserver());
    try { Thread.sleep(10000L); } 
    catch (InterruptedException e) { throw new RuntimeException(e); }
}
```

```출력값
NewYork : 88
NewYork : 34
NewYork : 97
NewYork : 20
NewYork : 30
Done!
```

`java9` 의 발행자 모델과 다르게 `onSubscribe` 와 `onNext` 부분에 역압력을 담당하는 `request` 호출문이 없다.  
또한 신청자(`Subscription`)를 필드로 저장하지도 않는다.  

## Observalble  

### create

```java
// 함수원형
public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
    ObjectHelper.requireNonNull(source, "source is null");
    return RxJavaPlugins.onAssembly(new ObservableCreate<T>(source));
}
```

개발자가 `onNext, onComplete, onError` 등의 메서드를 직접 호출하는 수동적인 `Observable` 생성 방법

매개변수로 사용되는 `ObservableOnSubscribe` 인터페이스에는 하나의 메서드만 정의되어 있고  

```java
public interface ObservableOnSubscribe<T> {
    void subscribe(@NonNull ObservableEmitter<T> var1) throws Exception;
}
```

`ObservableEmitter` 인터페이스는 아래와 같은 같다.  

> Emitter 상속하여 구독자역할을 수행하는 인터페이스  
> <http://reactivex.io/RxJava/javadoc/io/reactivex/ObservableEmitter.html>

```java
public interface Emitter<T> {
    void onNext(@NonNull T var1);
    void onError(@NonNull Throwable var1);
    void onComplete();
}

public interface ObservableEmitter<T> extends Emitter<T> {
    @NonNull
    ObservableEmitter<T> serialize();
    void setDisposable(@Nullable Disposable var1);
    void setCancellable(@Nullable Cancellable var1);
    boolean isDisposed();
    boolean tryOnError(@NonNull Throwable var1);
}
```

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.create(emitter -> {
        emitter.onNext("hello");
        emitter.onNext("world");
        emitter.onNext("one");
        emitter.onNext("two");
        emitter.onNext("three");
        emitter.onComplete();
    });
    Disposable disposable = observable.subscribe(s -> System.out.println(s)); // 데이터 발행 시작
    System.out.println(disposable.isDisposed()); // true
}
```

### fromArray, fromIterable

배열과 컬렉션 객체기반으로 `Observable` 생성  

```java
public static void main(String[] args) {
    List<String> stringList = new ArrayList<>();
    stringList.add("one");
    stringList.add("two");
    stringList.add("three");
    String[] stringArray = {"four", "five", "six"};
    Observable<String> observable1 = Observable.fromIterable(stringList);
    Observable<String> observable2 = Observable.fromArray(stringArray);
    Observable.merge(List.of(observable1, observable2))
            .subscribe(s -> System.out.println(s));
}
```

### formCallable, fromFuture, fromPublisher

`Callable` - `java5` 추가된 매개변수는 없고 반환값은 존재하는 함수형 인터페이스로 `Supplier` 와 비슷
`Publisher` - `java9` 에 추가된 Flow.Publisher 와 똑같은 형식의 인터페이스이지만 rxjava 패키지에서 재구성되어 `org.reactivestreams` 패키지를 사용해야함  

```java
public interface Callable<V> {
    V call() throws Exception;
}
```

해당 반환값을 기반으로 `Observable` 생성  

```java
public static void main(String[] args) {
        Callable<String> callable = () -> {
            System.out.println("callable invoked");
            return "hello";
        };
        Future<String> future = Executors.newSingleThreadExecutor().submit(() -> {
            System.out.println("future invoked");
            return "world";
        });
        Publisher<String> publisher = (s) -> {
            s.onNext("welcome");
            s.onComplete();
        };
        Observable<String> observable1 = Observable.fromCallable(callable);
        Observable<String> observable2 = Observable.fromFuture(future);
        Observable<String> observable3 = Observable.fromPublisher(publisher);
        Observable.merge(List.of(observable1, observable2, observable3))
                .subscribe(s -> System.out.println(s));
    }
```

### interval, intervalRange, timer

`interval` 메서드로 일정 시간 간격으로 데이터 발행하는 발행자 생성  

```java
// long initialDelay, long period, TimeUnit unit
public static void main(String[] args) throws InterruptedException {
    Observable observable = Observable.interval(1000l, 100l, TimeUnit.MILLISECONDS)
        .map(data -> {
            System.out.println("interval:" + data);
            return (data + 1) * 100;
        })
        .take(5);
    observable.subscribe(System.out::println);
    Thread.sleep(3000);
}
// 출력결과
// interval:0
// 100
// interval:1
// 200
// interval:2
// 300
// interval:3
// 400
// interval:4
// 500
```

1초 후부터 0.1초마다 데이터 발행, `take` 를 통해 5개 까지만 발행.  
`interval`은 데이를 지속적으로 발행 가능하다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.intervalRange(1, 9,
            2000l, 1000l, TimeUnit.MILLISECONDS);
    observable.subscribe(System.out::println);
    Thread.sleep(15000);
}
```

`range` 와 `interval` 를 합친 메서드로 2초 후 1초마다 9회 발행한다.  


`timer` 메서드로 일정 시간 후에 데이터를 발행하는 발행자 생성  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.timer(2000l, TimeUnit.MILLISECONDS)
        .map(data -> data);
    observable.subscribe(System.out::println); // 0
    Thread.sleep(3000);
}
```

2초 후 데이터를 1회 발행한다.  

### defer

일반적인 `Observable` 의 발행시점은 구독자가 `subscribe` 하면서 데이터가 생성되지만 새로운 구독자가 `subscribe`(추가 구독)된다고 데이터를 다시 발행하지 않는다.  

`defer` 를 사용하면 구독자가 `subscribe` 할때마다 새로운 데이터를 다시 만들어 발행한다.  

![image28](/assets/java/reactive-java/image28.png)  

그림처럼 시간별로 발행 데이터 색이 변경되어야 하는 경우 사용할 수 있다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<LocalTime> observable = Observable.defer(() -> Observable.just(LocalTime.now()));
    observable.subscribe(i -> System.out.println("subscribe1:" + i));
    Thread.sleep(3000);
    observable.subscribe(i -> System.out.println("subscribe2:" + i));
    Thread.sleep(3000);
    observable.subscribe(i -> System.out.println("subscribe3:" + i));
}
// 출력결과
// subscribe1:05:39:15.161997
// subscribe2:05:39:18.180470
// subscribe3:05:39:21.184535

public static void main(String[] args) throws InterruptedException {
    Observable<LocalTime> observable = Observable.just(LocalTime.now());
    observable.subscribe(i -> System.out.println("subscribe1:" + i));
    Thread.sleep(3000);
    observable.subscribe(i -> System.out.println("subscribe2:" + i));
    Thread.sleep(3000);
    observable.subscribe(i -> System.out.println("subscribe3:" + i));
}
// 출력결과 
// subscribe1:05:38:58.197359
// subscribe2:05:38:58.197359
// subscribe3:05:38:58.197359
```

`defer` 의 경우 구독자가 `subscribe` 할 때 마다 시간값이 변경되는 것으로 보아 새로운 `LocalTime` 이 생성된다.  
반면에 `just` 는 새로운 `LocalTime` 을 만들지는 않는다.  

### repeat  

말 그대로 지정된 횟수만큼 발행 데이터를 반복 발행

```java
public static void main(String[] args) {
    Observable<Integer> observable = Observable.range(1, 3).repeat(3);
    observable.subscribe(System.out::print);
}
// 출력결과
// 123123123
```

반복회수 제거시 `Long.MAX_VALUE` 횟수만큼 반복한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<LocalTime> observable = Observable.timer(2, TimeUnit.SECONDS)
            .map(i -> LocalTime.now())
            .repeat();
    observable.subscribe(System.out::println);
    Thread.sleep(10000);
}
// 출력결과
// 05:53:27.620033
// 05:53:29.622134
// 05:53:31.627118
// 05:53:33.631298
```

`timer` 와 `repeat` 를 같이 사용해 `interval` 처럼 사용할 수 있다.  

하지만 `repeat` 는 발행 시퀀스가 종료되면 해당 발행자의 스레드를 종료하고 새로운 발행자(스레드)를 만들어 구독자를 재등록하기에 약간 다르다.  

### Hot Observable, Cold Observable

`subscribe` 메서드가 호출되기 전까진 데이터를 발행하지 않는 구조를 차가운 `Observable` 이라 한다(Lazy 접근법).  

위의 `interval, timer` 발행자도 구독자가 구독을 시작해야 시간을 체크하고 데이터를 발행하기 시작한다.  

반면 `Hot Observable` 은 구독 여부 상관 없이 데이터를 발행하며, 이로 인해 앞부분 데이터를 유실될 수 있다.  

웹요청, DB쿼리, 파일 입출력은 차가운 `Observable`,  
마우스, 키보드, 센서 입출력은 `Hot Observable` 를 사용한다.  

### ConnectableObservable

`Hot Observable` 생성시 사용하는 클래스  

```java
public abstract class ConnectableObservable<T> extends Observable<T> {
}
```

![image25](/assets/java/reactive-java/image25.png)  

#### publish

`Hot Observable` 인 `ConnectableObservable` 은 `Observable` 의 `publish` 메서드 를 통해 생성된다.  
`ConnectableObservable` 의 `connect` 메서드 호출 전까지는 데이터 발행을 하지 않는다.  
반대로 `connect` 메서드가 호출되었다면 구독자가 없더라도 데이터가 발행된다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Integer> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
            .map(Long::intValue); // 0 부터 시작
    ConnectableObservable<Integer> connObservable = observable.publish();
    connObservable.connect(); // data 발행 시작
    Thread.sleep(3500);
    connObservable.subscribe(i -> System.out.println("subscribe1:" + i));
    connObservable.subscribe(i -> System.out.println("subscribe2:" + i));
    Thread.sleep(3500);
    connObservable.subscribe(i -> System.out.println("subscribe3:" + i));
    Thread.sleep(3500);
}
// 출력결과
// subscribe1:3 
// subscribe2:3
// subscribe1:4
// subscribe2:4
// subscribe1:5
// subscribe2:5
// subscribe1:6
// subscribe2:6
// subscribe1:7
// subscribe2:7
// subscribe3:7
// subscribe1:8
// subscribe2:8
// subscribe3:8
// ...
```

출력결과에서 `subscribe1,2` 가 출력되는 시기는 3.5초 뒤 `connect` 메서드가 가 호출된 인 4번째 부터이다.  
3.5초 뒤 `subscribe3` 에서 출력되기 시작하고 프로그램이 살아있는 3.5초 동안 세번의 인터벌을 더 출력하고 종료된다.  

또 한가지 `publish` 의 특징으로 **멀티캐스팅** 기능이 있다.  

먼저 `publish` 없이 `map` 을 통해 1초마다 시간값을 출력하면 두 구독자 사이의 값이 다르게 나온다.  

> 각 구독자에게 다른 데이터가 발행된다는 뜻.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
        .map(i -> System.currentTimeMillis());
    observable.subscribe(i -> System.out.println("subscribe1:" + i));
    observable.subscribe(i -> System.out.println("subscribe2:" + i));
    Thread.sleep(5000);
}
// 출력결과  
// subscribe1:1610329665255
// subscribe1:1610329666254
// subscribe1:1610329667255
// subscribe2:1610329667259
// ...
```

`publish` 사용시 시간값이 정확히 일치하며 두 구독자에게 같은 데이터가 발행된다.

```java
public static void main(String[] args) throws InterruptedException {
    ConnectableObservable<Long> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
        .map(i -> System.currentTimeMillis()).publish();
    observable.subscribe(i -> System.out.println("subscribe1:" + i));
    observable.subscribe(i -> System.out.println("subscribe2:" + i));
    observable.connect();
    Thread.sleep(5000);
}
// 출력결과
// subscribe1:1610329779472
// subscribe2:1610329779472
// subscribe1:1610329780473
// subscribe2:1610329780473
// ...
```

`share` 는 일잔적인 `Observable` 에게 `publish().refCount()` 한 것 과 같다.  

![image63](/assets/java/reactive-java/image63.png)  

`publish` 을 통해서 `multicast` 형식으로 사용하며 차가운 `Observable` 기능을 사용하기 위해 사용된다.  
구독자가 모두 `unSubscribe` 되면 더이상 데이터를 발행하지 않는다.  

`refCount` 메서드로 구독자 수를 지정할 수 있는데 구독자 수가 지정된 수에 도달하면 데이터 발행을 시작하게 하고 모든 구독자가 구독해제하면 발행 프로세스도 종료된다.   
`share` 에서 호출하는 `refCount` 의 경우 매개변수가 없음으로 별도의 발행 시작조건이 없다  


#### autoConnect

`publish` 의 멀티캐스팅 기능을 살리면서 차가운 `Observable` 처럼 사용하고 싶을때 사용한다.  
`connect` 메서드가 호출되지 않더라도 구독자가 subscribe 되는 순간 데이터 발행이 시작된다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1,TimeUnit.SECONDS)
        .publish()
        .autoConnect();
    observable.subscribe(l-> System.out.println("sub1:" + l));
    Thread.sleep(3000);
    observable.subscribe(l-> System.out.println("sub2:" + l));
    Thread.sleep(2000);
}
// 출력결과
// sub1:0
// sub1:1
// sub1:2
// sub1:3
// sub2:3 출력시작 
// sub1:4
// sub2:4
```

3번째 `interval` 부터 같이 `sub1, sub2` 가 같이 출력되기 시작한다.  


### replay, cache, cacheWithInitialCapacity

![image69](/assets/java/reactive-java/image69.png)  
```java
public static void main(String[] args) throws InterruptedException {
    Observable<LocalTime> observable = Observable.interval(1,TimeUnit.SECONDS)
        .map(i -> LocalTime.now())
        .replay()
        .autoConnect();
    observable.subscribe(l-> System.out.println("sub1:" + l));
    Thread.sleep(3000);
    observable.subscribe(l-> System.out.println("sub2:" + l));
    Thread.sleep(2000);
}
```

`replay` 는 기존에 발행했던 데이터를 버퍼에 저장해 두고 있다 새로 `subscribe` 된 구독자에게 전달한다.  

> `replay` 는 다양한 오버라이딩을 제공하며 매개변수로 버퍼사이즈나 시간값을 전달해 발행 데이터 축소가 가능하다.  

![image68](/assets/java/reactive-java/image68.png)  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<LocalTime> observable = Observable.interval(1,TimeUnit.SECONDS)
        .map(i -> LocalTime.now())
        .cache();
    observable.subscribe(l-> System.out.println("sub1:" + l));
    Thread.sleep(3000);
    observable.subscribe(l-> System.out.println("sub2:" + l));
    Thread.sleep(2000);
}
```

`replay` 는 반환값이 `ConnectableObservable` 이고  
`cache` 의 경우 `Observable` 이다.  

`replay` 의 경우 `Hot, Cold Observable` 을 `autoConnect` 나 `publish, connect` 를 통해 선택 가능하다.  

위 두 코드의 실행결과는 아래와 같다.  

```
sub1:10:19:12.477905
sub1:10:19:13.456234
sub1:10:19:14.457482
sub2:10:19:12.477905 # cache, replay 된 데이터 발행 시작 
sub2:10:19:13.456234
sub2:10:19:14.457482
sub1:10:19:15.456149
sub2:10:19:15.456149
sub1:10:19:16.454502
sub2:10:19:16.454502
```

출력결과를 보면 밀리초 단위까지 일치하며 두 메서드 모두 멀티캐스팅으로 동작하는 것을 알 수 있다.  
캐시데이터 감축을 원한다면 `replay` 를 사용, 기본기능만 사용한다면 아무거나 사용해도 무관하다.  
> 발행 데이터 수가 예측된다면 `cacheWithInitialCapacity(N)` 를 통해 버퍼를 미리 초기화 할 수 있음, `cache` 는 `autoConnect` 용도로만 사용 가능하다.  

## 기타 발행자 클래스 

### Single, Maybe

`Single` - `Observable` 의 변형 클래스로 똑같은 기능을 하나 발행 데이터가 하나임.  

```java
// Observable 구조
public abstract class Observable<T> implements ObservableSource<T> {
    ...
}
public interface ObservableSource<T> {
    void subscribe(@NonNull Observer<? super T> observer);
}
// Single 구조
public abstract class Single<T> implements SingleSource<T> {
    ...
}
public interface SingleSource<T> {
    void subscribe(@NonNull SingleObserver<? super T> observer);
}
```

클래스명만 다르지 내부 구조는 매우 비슷  

여러 방법으로 생성 가능하다.  

```java
public static void main(String[] args) {
    Observable source = Observable.just("hello");
    Single single1 = Single.fromObservable(source);
    Single single2 = Observable.just("world").single("default value");
    Single single3 = Observable.just("one", "two", "three").first("default value");
    Single.merge(single1, single2, single3).subscribe(System.out::println);
}
```

`Single` 의 경우 최소, 최대 1개 데이터를 발행하기에 `default value` 가 필요하지만  
`Maybe` 의 경우 최소 0개, 최대 1개 데이터를 발행 할 수 있다.  

### AsyncSubject  

```java
public abstract class Subject<T> extends Observable<T> implements Observer<T> {
    ...
}

public final class AsyncSubject<T> extends Subject<T> {
    ...
}
```

`Subject` 클래스를 상속하는 클래스로 `Subject` 는 발행자와 구독자 2가지 기능을 가진 클래스이다.  

`AsyncSubject` 클래스는 발행이 완료되기 전 마지막 데이터만 처리한다(`onComplete` 호출후 마지막 데이터)  

![image21](/assets/java/reactive-java/image21.png)  

```java
public static void main(String[] args) {
    Subject<String> subject = AsyncSubject.create();
    subject.subscribe(s -> System.out.println("subscribe1:" + s));
    subject.onNext("hello");
    subject.subscribe(s -> System.out.println("subscribe2:" + s));
    subject.onNext("one");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe3:" + s));
    subject.onNext("two");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe4:" + s));
}
// 출력결과
// subscribe1:one
// subscribe2:one
// subscribe3:one
// subscribe4:one
```

그림처럼 2개의 구독자를 정의 및 `subscribe` 로 등록하고 `onComplete` 호출시에 발행자는 마지막 데이터를 구독자에게 전달한다.  

`onComplete` 이후의 `onNext, onComplete` 는 무시됨으로 영향을 끼치지 않는다.  
이미 `onComplete` 가 호출되었음에도 `subscribe` 된 구독자에게 마지막 데이터를 계속 발행한다.  

`Subject` 는 `Observer` 를 구현함으로 `Observable` 의 `subscribe` 에 등록 가능하다.  

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.fromArray("one", "two", "three");
    AsyncSubject subject = AsyncSubject.create();
    subject.subscribe(s -> System.out.println("subscriber1:" + s));
    subject.subscribe(s -> System.out.println("subscriber2:" + s));
    observable.subscribe(subject);
}
// 출력결과
// subscriber1:three
// subscriber2:three
```


### BehaviorSubject

가장 최근값 혹은 기본값을 발행하는 발행자 겸 구독자.  

![image22](/assets/java/reactive-java/image22.png)  

반드시 `onComplete` 를 호출하지 않아도 데이터를 지속적으로 발행한다.  
`onComplete` 되었다면 그 이후에 `subscribe` 된 구독자는 무시된다.  

```java
public static void main(String[] args) {
    Subject<String> subject = BehaviorSubject.create();
    // 기본값 적용시에는 createDefault 사용
    subject.subscribe(s -> System.out.println("subscribe1:" + s));
    subject.onNext("hello");
    subject.subscribe(s -> System.out.println("subscribe2:" + s));
    subject.onNext("one");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe3:" + s));
    subject.onNext("two");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe4:" + s));
}
// 출력결과
// subscribe1:hello
// subscribe2:hello
// subscribe1:one
// subscribe2:one
```

### PublishSubject

일반적인 발행자 클래스,  

![image23](/assets/java/reactive-java/image23.png)  

발행시 `subscribe` 된 구독자에게 데이터 발행, `onComplete` 이후 등록된 구독자는 무시함.  


### ReplaySubject

`Hot Observable` 로 동작시키기 위한 발행자 모델  

![image24](/assets/java/reactive-java/image24.png)  

지금까지 발행했던 데이터를 신규 `subscribe` 된 구독자에게 모두 전달  
메모리 누수 가능성 있음.  

```java
public static void main(String[] args) {
    Subject<String> subject = ReplaySubject.create();
    subject.subscribe(s -> System.out.println("subscribe1:" + s));
    subject.onNext("hello");
    subject.subscribe(s -> System.out.println("subscribe2:" + s));
    subject.onNext("one");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe3:" + s));
    subject.onNext("two");
    subject.onComplete();
    subject.subscribe(s -> System.out.println("subscribe4:" + s));
}
// 출력결과
// subscribe1:hello
// subscribe2:hello
// subscribe1:one
// subscribe2:one
// subscribe3:hello
// subscribe3:one
// subscribe4:hello
// subscribe4:one
```

`onComplete` 이후 `subscribe` 된 구독자에게도 발행한 데이터를 모두 전달  


## Observable 연산자

`Observable`은 발행 데이터 변경(`map`), 합치기(`merge`), 필터링(`filter`), 개수만큼 가져오기(`take`) 할수 있는 다양한 체이닝 형식의 메서드를 제공한다.  


`Flow` 패키지의 `Processor` 는 발행과 구독 기능을 동시에 하는 인터페이스로 발행자와 구독자 사이에서 발행되는 데이터를 변환하는 기능을 수행한다.  

`RxJava` 도 `ObservableMap` 를 사용하면 발행되는 데이터를 자유자재로 변환할 수 있다.  
심지어 여러개의 `Observable` 를 하나의 `Observable` 로 합쳐서 `ObservableMap` 객체로 제공한다.  

위의 `Observable.create` 은 사실 단순 `Observable` 를 반환하지 않고 `ObservableMap` 를 반환하여 `stream` 객체처럼 발행자와 구독자 사이의 `Processor` 를 정의한것 처럼 행동할 수있다.  

### map, merge, filter

![image19](/assets/java/reactive-java/image19.png)  

![image20](/assets/java/reactive-java/image20.png)  


```java
public static void main(String[] args) {
    Observable<String> observable1 = Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
        observableEmitter.onNext("hello");
        observableEmitter.onNext("world");
        observableEmitter.onNext("one");
        observableEmitter.onNext("two");
        observableEmitter.onNext("three");
        observableEmitter.onComplete();
    });
    Observable<String> observable2 = Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
        observableEmitter.onNext("test");
        observableEmitter.onNext("command");
        observableEmitter.onNext("four");
        observableEmitter.onNext("five");
        observableEmitter.onNext("six");
        observableEmitter.onComplete();
    });
    Observable.merge(List.of(observable1, observable2))
            .map(String::toUpperCase)
            .filter(s -> s.length() > 3)
            .subscribe(s -> System.out.println(s));
}
// 출력결과
// HELLO
// WORLD
// THREE
// ...
```

2개의 발행자를 하나로 합치고(`merge`) 소문자를 대문자로 변경(`map`), 3글자 이상의 데이터만 필터(`filter`) 하여 데이터 발행   

### take, takeLast, takeUntil, takeWhile, 

![image50](/assets/java/reactive-java/image50.png)  

`take` 는 발행 데이터를 몇개 까지 가져올 것인지 개수 혹은 시간으로 설정 가능하다.  

```java
public static void main(String[] args) throws InterruptedException {
    String[] strings = {"one", "two", "three", "four"};
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        //.take(5)
        .take(5000, TimeUnit.MILLISECONDS);
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
// 출력결과
// 0
// 1
// 2
// 3
// 4
```

![image47](/assets/java/reactive-java/image47.png)  

`takeLast` 는 `onComplete` 이벤트가 발행되고 설정된 개수만큼의 마지막 데이터를 발행한다.  

```java
public static void main(String[] args) {
    String[] strings = {"one", "two", "three", "four"};
    Observable<String> observable = Observable.fromArray(strings)
        .takeLast(2);
    observable.subscribe(System.out::println);
}
// 출력결과
// three
// four
```

![image42](/assets/java/reactive-java/image42.png)  

`takeUntil` 로 지정한 `Observable` 에서 데이터를 발행할 때까지 구독자에게 데이터를 발행한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .takeUntil(Observable.timer(5000, TimeUnit.MILLISECONDS));
    observable.subscribe(System.out::println);
    Thread.sleep(10000);
}
// 출력결과
// 0
// 1
// 2
// 3
// 4
```

![image43](/assets/java/reactive-java/image43.png)  

다른 사용법으로 조건 람다식을 사용해 발행을 멈출 수 도 있다.  

```java
public static void main(String[] args) {
    String[] strings = {"one", "two", "three", "four"};
    Observable<String> observable = Observable.fromArray(strings)
        .takeUntil((String s) -> s.equals("two"));
    observable.subscribe(System.out::println);
}
// 출력결과
// one
// two
```

![image44](/assets/java/reactive-java/image44.png)  

`takeWhile` 은 해당 조건식이 `true` 라면 계속 데이터를 발행한다.  

```java
public static void main(String[] args) {
    String[] strings = {"one", "two", "three", "four"};
    Observable<String> observable = Observable.fromArray(strings)
        .takeWhile((String s) -> s.length() < 4);
    observable.subscribe(System.out::println);
}
// 출력결과
// one
// two
```

### skip, skipLast, skipUntil, skipWhile, 

![image48](/assets/java/reactive-java/image48.png)  

`skip` 은 발행 데이터를 생략하며 생략할 개수 혹은 시간 설정이 가능하다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        //.skip(3);
        .skip(3000, TimeUnit.MILLISECONDS);
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
// 출력결과
// 3
// 4
// 5
// ...
```

![image49](/assets/java/reactive-java/image49.png)  

`skipLast` 는  마지막 데이터를 생략하고 발행하기에 `takeLast` 와 다르게 데이터가 설정한 개수 이상 쌓이면 데이터를 발행하기 시작한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .skipLast(3);
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
// 출력결과
// 0
// 1
// 2
```

![image51](/assets/java/reactive-java/image51.png)  

`skipUntil` 은 매개변수로 받은 `Observable` 에서 데이터가 발행될 때 까지 스킵한다.  
`takeUntil` 과 완전히 반대된다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .skipUntil(Observable.timer(5000, TimeUnit.MILLISECONDS));
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
```

![image52](/assets/java/reactive-java/image52.png)  

`skipWhile` 은 해당 조건이 false 가 반환되기 전까지 데이터 발행을 스킵한다.

```java
public static void main(String[] args) {
    String[] strings = {"one", "two", "three", "four"};
    Observable<String> observable = Observable.fromArray(strings)
        .skipWhile((String s) -> s.length() < 4);
    observable.subscribe(System.out::println);
}
// 출력결과
// three
// four
```

### combineLatest

![image32](/assets/java/reactive-java/image32.png)  

2개 이상의 `Observable` 를 연관지어 새로운 데이터가 발행될 때 마다 요소를 엮어 구독자에게 다시 발행한다.  

```java
public static void main(String[] args) throws InterruptedException {
    String[] strings1 = {"one", "two", "three", "four"};
    String[] strings2 = {"first", "second", "third"};
    Observable<String> observable = Observable.combineLatest(
        Observable.fromArray(strings1)
            .zipWith(Observable.interval(1000L, TimeUnit.MILLISECONDS),
                (s, notUsed) -> s),
        Observable.fromArray(strings2)
            .zipWith(Observable.interval(1500L, TimeUnit.MILLISECONDS),
                (s, notUsed) -> s),
        (s1, s2) -> s1 + "-" + s2);
    observable.subscribe(System.out::println);
    Thread.sleep(10000);
}
// 출력결과
// one-first
// two-first
// three-first
// three-second
// four-second
// four-third
```

두개 `Observable` 중 하나라도 요소가 발행되면 각 `Observable` 의 최신 요소를 결합하여 반환한다.  

### zip, zipWith

![image40](/assets/java/reactive-java/image40.png)  

`merge` 가 두개의 `Observable` 을 하나로 `Observable`로 합치는 거라면  
`zip` 은 2 개의 요소를 합쳐 반환하는 `Observable` 을 생성한다  

```java
public static void main(String[] args) {
    String[] strings1 = {"one", "two", "three", "four"};
    String[] strings2 = {"first", "second", "third"};
    Observable<String> observable = Observable.zip(
            Observable.fromArray(strings1),
            Observable.fromArray(strings2),
            (string1, string2) -> string1 + "-" + string2);
    observable.subscribe(System.out::println);
}
// 출력결과
// one-first
// two-second
// three-third
```

두 발행자의 요소 개수가 달라 먼저 `onComplete` 가 호출된다면 근접 발행자도 같이 종료된다.  

이미 생성된 `Observable` 에 새로운 `Observable` 의 요소를 합칠땐 `zipWith` 메서드를 사용하는게 편하다.  

```java
public static void main(String[] args) {
    String[] strings1 = {"one", "two", "three", "four"};
    String[] strings2 = {"first", "second", "third"};
    String[] strings3 = {"hello", "rx", "java", "reactive"};
    Observable<String> observable = Observable
        .zip(Observable.fromArray(strings1),
            Observable.fromArray(strings2),
            (string1, string2) -> string1 + "-" + string2)
        .zipWith(Observable.fromArray(strings3),
            (string1, string2) -> string1 + "-" + string2);
    observable.subscribe(System.out::println);
}
// 출력결과
// one-first-hello
// two-second-rx
// three-third-java
```

### concat, concatWith

![image38](/assets/java/reactive-java/image38.png)  

2개 이상의 `Observable` 를 이어 붙이는 메서드, 첫번째 `Observable` 이 종료되어야 두번째 `Observable` 이 진행된다.  

```java
public static void main(String[] args) throws InterruptedException {
    String[] strings1 = {"one", "two", "three", "four"};
    String[] strings2 = {"first", "second", "third"};
    Observable<String> observable = Observable.concat(
        Observable.fromArray(strings2)
            .zipWith(Observable.interval(1000L, TimeUnit.MILLISECONDS),
                (s, notUsed) -> s),
        Observable.fromArray(strings1)
            .zipWith(Observable.interval(1500L, TimeUnit.MILLISECONDS),
                (s, notUsed) -> s));
    observable.subscribe(System.out::println);
    Thread.sleep(10000);
}
```

생성된 `Observable` 뒤에 `concatWith` 메서드로 똑같은 기능 수행이 가능

```java
public final Observable<T> concatWith(ObservableSource<? extends T> other)
```


### flatMap, concatMap, switchMap

![image26](/assets/java/reactive-java/image26.png)  

`map` 과 같이 데이터 가공을 위한 메서드  

그림을 보면 원 하나당 마름모 2개를 발행하는데  
`stream` 의 `flatMap` 과 같이 반환되는 값의 **요소가** 단일 데이터가 아닌 `Observable` 일 경우    
`Observable` 의 발행 데이터(마름모 2개)를 끄집어 내서 구독자에게 전달할 수 있도록 한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<String> observable = Observable.fromArray("one", "two", "three");
    Function<String, Observable<String>> modifyString = s -> Observable.just(s + "*", s + "#");
    observable.flatMap(modifyString).subscribe(System.out::println);
}
// 출력결과
// one*
// one#
// two*
// two#
// three*
// three#
```

> `Function` 은 `java8` 의 함수형 인터페이스와 똑같은 형식의 `reactivex` 패키지의 인터페이스이다.  

위 코드로 문자열 1개 집어 넣으면 뒤에 `*, #` 을 붙인 문자열 2개 발행한다.  

간단한 구구단 기능을 `flatMap` 으로 구현  

```java
public static void main(String[] args) {
    Function<Integer, Observable<String>> gugudan = dan -> Observable
            .range(1, 9)
            .map(row -> dan + "*" + row + "=" + dan * row);
    Observable<String> observable = Observable.just(9).flatMap(gugudan);
    observable.subscribe(System.out::println); // 9단 출력
}
// 출력결과
// 9*1=9
// 9*2=18
// 9*3=27
// 9*4=36
// 9*5=45
// 9*6=54
// 9*7=63
// 9*8=72
// 9*9=81
```

단을 넣으면 9개의 새로운 문자열 데이터가 발행된다.  

![image39](/assets/java/reactive-java/image39.png)  

`flatMap` 과 같이 다중 요소를 처리하는 `map` 메서드이지만 `flatMap` 과 다르게 순차적으로 처리하는 특징이 있다.  

```java
public static void main(String[] args) throws InterruptedException {
    System.out.println("start:" + LocalTime.now());
    Observable<LocalTime> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
        .map(time -> LocalTime.now())
        .take(5)
        .flatMap(now -> Observable.interval(2000l, TimeUnit.MILLISECONDS)
            .map(notUsed -> now)
            .take(2));
    observable.subscribe(now -> System.out.println(now.getMinute() + ":" + now.getSecond()));
    Thread.sleep(30000);
}
// 출력 결과
// 14:29
// 14:30
// 14:31
// 14:29
// 14:32
// 14:30
// 14:33
// 14:31
// 14:32
// 14:33
```

첫번째 발행자는 1초마다 현재 시간값을 5번 행하고  
두번째 발행자는 2초마다 첫번째 발행자에게 받은 시간값을 다시 2번 발행한다.  

총 10번 데이터가 발행되는데 출력결과를 보면 `flatMap` 안에 있는 두번째 발행자가 데이터를 받는 순가 바로 처리하기에  
2초 유격 사이에 1초마다 쏟아지는 데이터를 바로 발행해버린다.  

`flatMap` 을 그대로 `concatMap` 으로 변환하면 아래와 같이 출력된다.  

```java
public static void main(String[] args) throws InterruptedException {
    System.out.println("start:" + LocalTime.now());
    Observable<LocalTime> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
        .map(time -> LocalTime.now())
        .take(5)
        .concatMap(now -> Observable.interval(2000l, TimeUnit.MILLISECONDS)
            .map(notUsed -> now)
            .take(2));
    observable.subscribe(now -> System.out.println(now.getMinute() + ":" + now.getSecond()));
    Thread.sleep(30000);
}
// 출력결과
// 16:46
// 16:46
// 16:47
// 16:47
// 16:48
// 16:48
// 16:49
// 16:49
// 16:50
// 16:50
```

데이터를 받더라도 두번째 발행자의 발행이 완료되지 않았다면 그 이후의 데이터를 발행하지 않고 기다린다.  
(물론 첫번째 발행자의 데이터는 5초동안 발행이 모두 완료된다)

![image29](/assets/java/reactive-java/image29.png)  

`concatMap` 이 순서 보장을 위해 데이터를 발행하지 않고 대기한다면  
`switchMap` 은 순서 보장을 위해 기존에 진행중이던 작업을 중단하고 새로 들어온 작업을 진행한다.   

그림처럼 `3` 데이터 발행 후 바로 `5` 발행될 경우 기존 작업을 취소하고 `5` 에 대한 작업을 수행한다.  

```java
public static void main(String[] args) throws InterruptedException {
    System.out.println("start:" + LocalTime.now());
    Observable<LocalTime> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS)
        .map(time -> LocalTime.now())
        .take(5)
        .switchMap(now -> Observable.interval(2000l, TimeUnit.MILLISECONDS)
            .map(notUsed -> now)
            .take(2));
    observable.subscribe(now -> System.out.println(now.getMinute() + ":" + now.getSecond()));
    Thread.sleep(30000);
}
// 출력결과
// 20:6
// 20:6
```

위 예제를 그대로 `switchMap` 으로 변경시 마지막 데이터만 정상 처리되고 이전 발행 데이터는 모두 취소되어 버린다.  

### reduce, reduceWith

![image27](/assets/java/reactive-java/image27.png)  

`stream` 의 `reduce` 와 같이 재귀적인 구조를 가지며 발행되는 데이터를 모아 같이 처리할 수 있다.  

```java
public static void main(String[] args) {
    Maybe<Integer> observable = Observable.range(1, 9).reduce((i, j) -> i + j);
    observable.subscribe(System.out::println);
}
// 출력결과
// 45
```
> 반환하는 발행자 타입이 `Maybe` 이며 이는 발행 데이터가 있을수도, 없을수도 있음을 뜻한다.  

`reduce` 에 `seed` 매개변수를 추가하거나 `reduceWith` 을 사용하면 초기 설정값을 지정할 수 있다.  
초기 설정으로 인해 데이터가 무조건 반환됨으로 반환값이 `Single` 클래스이다.  

```java
public static void main(String[] args) {
    Single<Integer> observable = Observable.range(1, 9)
        //.reduce(10, (i, j) -> i + j); // set seed
        .reduceWith(() -> 10, (i, j) -> i + j);
    observable.subscribe(System.out::println);
}
```

### scan, scanWith

![image31](/assets/java/reactive-java/image31.png)  

그림을 보면 `reduce` 와 비슷하지만 데이터 발행 건수가 다른것을 알 수 있다.  
반환값 또한 `Observable`  

```java
public static void main(String[] args) {
    Observable<Integer> observable = Observable.range(1, 9).scan((i, j) -> i + j);
    observable.subscribe(System.out::println);
}
// 출력결과
// 1
// 3
// 6
// 10
// 15
// 21
// 28
// 36
// 45
```

`scanWith` 의 경우 첫 시드값을 반환하는 설정을 할 수 있음.  

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.fromArray("1","2","3","4").scanWith(() -> {
        System.out.println("seed supplier invoke");
        return "0";
    }, (s1, s2) -> s1 + s2);
    observable.subscribe(System.out::println);
}
//출력결과
// supplier invoke
// 0
// 01
// 012
// 0123
// 01234
```

### groupBy

![image30](/assets/java/reactive-java/image30.png)  

```java
public static void main(String[] args) {
    Observable<GroupedObservable<Boolean, Integer>> observable = Observable.fromArray(1, 2, 3, 4, 5, 6)
        .groupBy(i -> i % 2 == 0)
        .filter(groupedObservable -> groupedObservable.getKey() == false);

    observable.subscribe(groupedObservable -> groupedObservable
        .subscribe(val -> System.out.println(val)));
}
// 출력결과
// 1
// 3
// 5
```

입력된 데이터를 `groupBy` 로 짝/홀 로 그룹화하기에 최대 2개의 `Observable` 만 생성될 수 있다.  
`filter` 로 홀수에 해당하는 데이터만 걸러낸다.  

발행되는 요소를 보면 `GroupedObservable` 이다.  

```java
public abstract class GroupedObservable<K, T> extends Observable<T> {
    final K key;
    protected GroupedObservable(@Nullable K key) {
        this.key = key;
    }
    @Nullable
    public K getKey() {
        return key;
    }
}
```

`key` 필드를 갖는 `Observable`(발행자)이며 2차원 배열처럼 `subscribe` 로 하나씩 벗겨가며 `groupBy` 된 데이터에 접근할 수 있다.  

최상위 `Observable` 에서 `onComplete` 가 밸행되면 연쇄적으로 `GroupedObservable` 도 `onComplete` 가 발행된다.  

### amb, ambArray, ambWith

![image41](/assets/java/reactive-java/image41.png)  

여러개의 `Observable` 중 가장 먼저 데이터 발행을 시작하는 `Observable` 를 선택, 그 외의 `Observable` 의 발행 데이터는 모두 무시한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<String> observable1 = Observable.interval(500, TimeUnit.MILLISECONDS)
        .map(i -> "observable1:" + i);
    Observable<String> observable2 = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .map(i -> "observable2:" + i);
    Observable.ambArray(observable1, observable2).subscribe(System.out::println);
    // List 로 Observable 객체를 묶으면 amb 메서드로 사용 가능  
    Thread.sleep(100000);
}
// 출력결과
// observable1:0
// observable1:1
// observable1:2
// ...
```

이미 생성된 `Observable` 이 있으면 `ambWith` 메서드를 사용할 수 있음  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<String> observable1 = Observable.interval(500, TimeUnit.MILLISECONDS)
        .map(i -> "observable1:" + i);
    Observable<String> observable2 = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .map(i -> "observable2:" + i).ambWith(observable1);
    observable2.subscribe(System.out::println);
    Thread.sleep(100000);
}
```

### all

![image53](/assets/java/reactive-java/image53.png)  

반환값이 `boolean` 타입이며 모든 발행 데이터가 해당 조건에 맞아야 `true` 를 반환한다.  

```java
public static void main(String[] args) {
    String[] strings = {"one", "two", "three", "four"};
    Single<Boolean> result = Observable.fromArray(strings)
        .all((s) -> s.length() > 2);
    result.subscribe(System.out::println);
}
// 출력결과
// true
```

### delay, delaySubscription

![image54](/assets/java/reactive-java/image54.png)  

`timer` 메서드가 `Observable` 를 생성하는 메서드라면
`delay` 는 연산자로서 보조 역할이다.  

단순히 발행 데이터의 지연시간을 설정해 지연 발행한다.  

```java
public static void main(String[] args) throws InterruptedException {
    String[] strings = {"one", "two", "three", "four"};
    Observable<String> observable = Observable.fromArray(strings)
        .delay(1000, TimeUnit.MILLISECONDS);
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
```

`timer` 처럼 1초 후에 모든 데이터가 발행되어 출력된다.

![image67](/assets/java/reactive-java/image67.png)  

`delay` 의 경우 각 발행 데이터가 정해진 시간만큼 delay 되어 발행되지만  
`delaySubscription` 의 경우 해당 시간만큼 구독 지연되고 그 이후부턴 정상적인 시간으로 발행 데이터를 수신받는다.  

### timeInterval

![image55](/assets/java/reactive-java/image55.png)  

`Timed<String>` 데이터로 변환하여 발행하며 `subscribe` 와 데이터 발행 시간값, 데이터 발행 사이의 시간값을 같이 발행한다.   

```java
public static void main(String[] args) throws InterruptedException {
    String[] strings = {"one", "two", "three", "four"};
    Observable<Timed<String>> observable = Observable.fromArray(strings)
        .delay(1000, TimeUnit.MILLISECONDS)
        .timeInterval();
    observable.subscribe(System.out::println);
    Thread.sleep(100000);
}
// 출력결과
// Timed[time=1006, unit=MILLISECONDS, value=one]
// Timed[time=0, unit=MILLISECONDS, value=two]
// Timed[time=1, unit=MILLISECONDS, value=three]
// Timed[time=0, unit=MILLISECONDS, value=four]
```

`delay` 로 인한 첫번째 발행 시간만 1006 이 출력되고 그 외는 바로 출력됨으로 0 비슷한 시간이 출력된다.  


### collect, collectInto

![image65](/assets/java/reactive-java/image65.png)  

![image66](/assets/java/reactive-java/image66.png)  

```java
public static void main(String[] args) {
    List<Integer> phraseIDs = new ArrayList<>();
    Observable.range(1, 5)
        .collect(() -> phraseIDs, (l, i) -> l.add(i))
        .subscribe(list -> list.stream().forEach(System.out::print)); // 12345
    System.out.println();

    Observable.range(1, 5)
        .collectInto(new ArrayList<>(), ArrayList::add)
        .subscribe(list -> list.stream().forEach(System.out::print)); // 12345
}
```

발행 스트림이 무한할 경우 메모리가 고갈될 수 있음으로 조심  

## Observable 흐름제어 메서드  

데이터 발행 속도와 데이터 처리 속도 차이가 발생할 때 제어(처리)하는 흐름제어 메서드가 있다.  
센서와 같이 1초에 수십건씩 데이터를 발행하는 경우 그래프 처리, 파일 저장 처리는 보다 느림으로 흐름제어를 해야한다.  

### sample

![image56](/assets/java/reactive-java/image56.png)  

특정한 시간동안 가장 최근에 발행된 데이터만 처리.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .map(i -> {
            System.out.println("observe data:" + i);
            return i;
        });
    observable.sample(3000, TimeUnit.MILLISECONDS)
        .subscribe(i -> System.out.println("subscribe:" + i));
    Thread.sleep(100000);
}
// 출력결과
// observe data:0
// observe data:1
// observe data:2
// subscribe:1
// observe data:3
// observe data:4
// subscribe:4
// observe data:5
// observe data:6
// observe data:7
// observe data:8
// subscribe:7
```

데이터가 정확히 1초마다 발행되는게 아니다 보니 약간의 오차가 있지만 대략적으로 3초마다 최신 데이터 하나만 구독자에게 발행한다.  

### throttleFirst, throttleLast

![image59](/assets/java/reactive-java/image59.png)  

![image60](/assets/java/reactive-java/image60.png)  
 
`sample` 처럼 특정 시간내의 발행된 데이터 일부를 가져오는 메서드로  
`throttleFirst` 는 첫 데이터 발행 후 해당 시간동안은 더이상의 데이터 발행을 막고  
`throttleLast` 는 `sample` 과 같은 기능을 수행하며 `sample` 이 더 다양한 오버로딩 메서드를 가지고 있다.  
```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .map(i -> {
                System.out.println("observe data:" + i);
                return i;
            });
        observable.throttleFirst(3000, TimeUnit.MILLISECONDS)
            .subscribe(i -> System.out.println("subscribe:" + i));
        Thread.sleep(100000);
    }
}
// 출력결과
// observe data:0
// subscribe:0
// observe data:1
// observe data:2
// observe data:3
// observe data:4
// subscribe:4 // 의도상 3을 출력해야하나 약간의 시간차이로 4가 출력됨.
```

### buffer

![image57](/assets/java/reactive-java/image57.png)  

![image58](/assets/java/reactive-java/image58.png)  

버퍼처럼 발행 데이터를 일정 개수 이상 모았다가 `List` 형식으로 구독자에게 발행

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS)
        .map(i -> {
            System.out.println("observe data:" + (i + 1));
            return i + 1;
        });
    observable.buffer(2, 3).subscribe(list -> list.stream().forEach(System.out::println));
    Thread.sleep(100000);
}
// 출력결과
// observe data:1
// observe data:2
// 1
// 2
// observe data:3
// observe data:4
// observe data:5
// 4
// 5
```

개수 외에도 시간 설정으로 특정 시간동안 모인 데이터를 발행할 수 도 있음  

### window

![image61](/assets/java/reactive-java/image61.png)  

`grouBy` `Observable` 연산자와 비슷하나 `window` 는 발행 개수, 시간을 기반으로 그룹화하여 데이터를 발행한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Observable<Long>> observable = Observable
        .interval(1000, TimeUnit.MILLISECONDS)
        .window(3);
    observable.subscribe(longObservable -> longObservable.subscribe(System.out::println));
    Thread.sleep(100000);
}
// 출력결과
// 0
// 1
// 2
// ...
```

### debounce

![image62](/assets/java/reactive-java/image62.png)  

발행 데이터 주기를 정하고 해당 주기동안 데이터가 들어오지 않아야 데이터를 발행한다.  

```java
public static void main(String[] args) {
    Random rnd = new Random();
    Observable<Integer> observable = Observable.range(1, 9).map(i -> {
        int time = 1000 * (rnd.nextInt(5) + 1);
        System.out.println("sleep time:" + time + ", data:" + i);
        Thread.sleep(time);
        return i;
    });
    observable.debounce(3900, TimeUnit.MILLISECONDS)
        .subscribe(System.out::println);
} 
// 출력결과
// sleep time:3000, data:1
// sleep time:3000, data:2
// sleep time:2000, data:3
// sleep time:4000, data:4
// 3
// sleep time:1000, data:5
// sleep time:5000, data:6
// 5
```

보다싶이 데이터 발행 이후 다음 데이터 발행시 `sleep` 을 4초 이상 한 데이터만 출력된다.  

### Schedulers.newThread  

새로운 스레드를 생성하여 데이터를 발행하는 방법  

별도의 스케줄러 없이 `subscribe` 호출시 main 스레드에서 모든 발행이 이루어진다.  

```java
public static void main(String[] args) throws InterruptedException {
    String[] string1 = {"one", "two", "three", "four"};
    Observable.fromArray(string1)
        .subscribe(s -> System.out.println(Thread.currentThread().getName() + ":" + s));
    Thread.sleep(1000);
    String[] strings2 = {"first", "second", "third"};
    Observable.fromArray(strings2)
        .subscribe(s -> System.out.println(Thread.currentThread().getName() + ":" + s));
    Thread.sleep(1000);
}
// 출력결과
// main:one
// main:two
// main:three
// main:four
// main:first
// main:second
// main:third
```

`Schedulers.newThread()` 메서드로 스케줄러 설정  

```java
public static void main(String[] args) throws InterruptedException {
    String[] string1 = {"one", "two", "three", "four"};
    Observable.fromArray(string1)
        .subscribeOn(Schedulers.newThread())
        .subscribe(s -> System.out.println(Thread.currentThread().getName() + ":" + s));
    Thread.sleep(1000);
    String[] strings2 = {"first", "second", "third"};
    Observable.fromArray(strings2)
        .subscribeOn(Schedulers.newThread())
        .subscribe(s -> System.out.println(Thread.currentThread().getName() + ":" + s));
    Thread.sleep(1000);
}
// 출력결과
// RxNewThreadScheduler-1:one
// RxNewThreadScheduler-1:two
// RxNewThreadScheduler-1:three
// RxNewThreadScheduler-1:four
// RxNewThreadScheduler-2:first
// RxNewThreadScheduler-2:second
// RxNewThreadScheduler-2:third
```

`Schedulers.newThread` 는 다른 스케줄러이 비해 사용빈도가 낮다.  

### Schedulers.computation

대표적으로 `interval` 메서드에서 기본 사용되는 스케줄러이다.  
빠른 연산 및 반환을 위해 내부생성한 스레드 풀을 사용해 데이터를 발행한다.  

> 스레드 개수는 프로세서 개수와 동일  

```java
@CheckReturnValue
@SchedulerSupport(SchedulerSupport.COMPUTATION)
public static Observable<Long> interval(long period, TimeUnit unit) {
    return interval(period, period, unit, Schedulers.computation());
}
```

```java
public static void main(String[] args) throws InterruptedException {
    Observable.interval(1000l, TimeUnit.MILLISECONDS)
            .subscribe(i -> System.out.println(Thread.currentThread().getName() + ":" + i));
    Thread.sleep(5000l);
}
// 출력결과
// RxComputationThreadPool-1:0
// RxComputationThreadPool-1:1
// RxComputationThreadPool-1:2
// RxComputationThreadPool-1:3
// RxComputationThreadPool-1:4
```

### Schedulers.trampoline

새로운 스레드를 만들기 싫다면 `Schedulers.trampoline` 사용  
크기가 무한한 blocking queue 를 만들어 데이터를 발행하여 집어넣는다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable.interval(1000l, TimeUnit.MILLISECONDS, Schedulers.trampoline())
        .subscribe(i -> System.out.println(Thread.currentThread().getName() + ":" + i));
    System.out.println("end");
}
// 출력결과
// main:0
// main:1
// main:2
// main:3
// .....
```

대부분의 발행자 생성 메서드에서 `Schedulers` 를 변경할 수 있어 `interval` 의 `computation` 스케줄러를 `trampoline` 으로 변경  

프로그램이 끝나지 않고 큐에서 대기하며 계속 발행 데이터를 접어넣고 처리한다.  

### Schedulers.single

아무리 스레드를 추가생성하기 싫어도 `trampoline` 처럼 현재 스레드에 대기 큐 를 만들어 프로그램이나 메서드가 끝나지 않고 대기상태로 빠지는 것을 원하지 않는다면  
`single` 스케줄러를 사용해 미리 생성된 싱글 스레드에 작업을 할당하는 것도 방법이다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable.interval(1000l, TimeUnit.MILLISECONDS, Schedulers.single())
            .subscribe(i -> System.out.println(Thread.currentThread().getName() + ":" + i));
    Thread.sleep(10000l);
}
// 출력결과
// RxSingleScheduler-1:0
// RxSingleScheduler-1:1
// RxSingleScheduler-1:2
// RxSingleScheduler-1:3
// RxSingleScheduler-1:4
// RxSingleScheduler-1:5
// RxSingleScheduler-1:6
// RxSingleScheduler-1:7
// RxSingleScheduler-1:8
// RxSingleScheduler-1:9
```

`Schedulers.single()` 호출 후에 `Thread.getAllStackTraces()` 메서드로 생성된 스레드를 확인하면 `RxSingleScheduler-1` 이름으로 스레드가 생성된 것 을 확인할 수 있다.  

앞으로 `Schedulers.single()` 사용시 생성된 스레드로 작업이 할당된다.  

`Schedulers.single, Schedulers.trampoline` 둘다 리액티브 프로그래밍에선 잘 사용되지 않는다.  

### Schedulers.io

네트워크, 파일 입출력, DB 쿼리 처리를 위한 스케줄러로 기본 생성 스레드 개수가 다름  

`Schedulers.computation` 의 경우 CPU 개수만큼 스레드가 생성되지만 `Schedulers.io` 의 경우 필요한 만큼 스레드가 계속 생성됨  

### 스케줄러 지정 - subscribeOn, observeOn

`subscribeOn` 메서드는 구독자가 `subscribe` 되었을 때 데이터 발행 스레드를 지정  
`observeOn` 메서드는 처리된 결과를 구독자에게 전달하는 스레드를 지정한다.  

`subscribeOn` 데이터 발행 스레드는 첫 지정시 고정되며 다시호출해도 무시되지만  
`observeOn` 은 발행된 데이터를 조작하는 스레드로 계속 추가할 수 있다.  

![image33](/assets/java/reactive-java/image33.png)  

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.create(observableEmitter -> {
        observableEmitter.onNext("hello");
        observableEmitter.onNext("world");
        observableEmitter.onNext("one");
        observableEmitter.onNext("two");
        observableEmitter.onNext("three");
        observableEmitter.onComplete();
    });
    observable
        .observeOn(Schedulers.trampoline())
        .map(s -> {
            System.out.println(Thread.currentThread().getName());
            return s;
        })
        .observeOn(Schedulers.single())
        .filter(s -> {
            System.out.println(Thread.currentThread().getName());
            return s.length() > 3;
        })
        .take(3)
        .observeOn(Schedulers.newThread())
        .subscribe(s -> {
            System.out.println(Thread.currentThread().getName());
            System.out.println(s);
        });
}
// 출력결과
// main
// main
// RxSingleScheduler-1
// main
// main
// main
// RxSingleScheduler-1
// RxSingleScheduler-1
// RxNewThreadScheduler-1
// hello
// RxSingleScheduler-1
// RxNewThreadScheduler-1
// world
// RxSingleScheduler-1
// RxNewThreadScheduler-1
```

`observeOn` 메서드로 여러 스케줄러를 지정해서 데이터를 가공하였다.  

요소의 개수만큼 현재 스레드 이름을 출력하도록 하였고 `main, RxSingleScheduler-1, RxNewThreadScheduler-1` 3 종류의 스레드 이름이 출력되었다.  

## RxJava 디버깅  

### doOnNext, doOnError, doOnComplete, doOnEach

이름 그대로 `onNext, onError, onComplete` 에 대응되는 이벤트 메서드  

```java
public static void main(String[] args) {
    // Integer[] arr = {1, 2, 3};
    Integer[] arr = {1, 2, 3, 0}; // onError 결과를 보기 위해 배열 마지막에 0 추가
    Observable<Integer> observable = Observable.fromArray(arr);
    observable
        .doOnNext(i -> System.out.println("doOnNext invoked:" + i))
        .doOnError(e -> System.out.println("doOnError invoked:" + e.getMessage()))
        .doOnComplete(() -> System.out.println("doOnComplete invoked"))
        .map(i -> 6 / i)
        .subscribe(i -> System.out.println(i));
    System.out.println("main end");
}
/* 
출력결과
doOnNext invoked:6
6
doOnNext invoked:3
3
doOnNext invoked:2
2
doOnError invoked:/ by zero
main end
*/
```

`doOnNext, doOnError, doOnComplete` 를 한번에 구현할 수 있는 `doOnEach` 도 있음.  

`doOnEach` 의 경우 `onNext, onError, onComplete, onTerminate(아래 설명)` 에 대한 모든 `Consumer` 메서드를 매개변수로 넘길 수 도 있지만 `Notification` 이라는 알림 객체를 사용해 처리할 수 도 있다.  

```java
public static void main(String[] args) {
   Observable.just(1, 2, 3).doOnEach(s -> { 
       // Consumer<? super Notification<T>> onNotification
        if (s.isOnNext())
            System.out.println(s.getValue());
        else if (s.isOnError())
            System.err.println(s.getError());
        if (s.isOnComplete())
            System.out.println("complete");
    }).subscribe();
}
// 출력결과
// 1
// 2
// 3
// complete
```

### doOnSubscribe, doOnDispose  

`subscribe, dispose` 에 대응되는 이벤트 메서드  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1000l, TimeUnit.MILLISECONDS);
    Disposable disposable = observable
        .doOnComplete(() -> System.out.println("doOnComplete invoked"))
        .doOnSubscribe(d -> System.out.println("doOnSubscribe invoked"))
        .doOnDispose(() -> System.out.println("doOnDispose invoked"))
        .subscribe(i -> System.out.println(i));
    Thread.sleep(2000);
    disposable.dispose();
    System.out.println("main end");
}
// 출력결과
// doOnSubscribe invoked
// 0
// 1
// doOnDispose invoked
// main end
```

1초 마다 발행되는 `interval` 의 구독자를 2초 후에 `dispose`(구독 해지) 할 때 이벤트 메서드 호출 확인  

`doOnLifecycle` 로 두 이벤트를 동시에 적용 가능하다.  

### doOnTerminate, doFinally

`complete, error, dispose` 등의 메서드 호출로 발행이 완료되면 `doOnTerminate` 와 `doFinally` 가 호출될 수 있다.  

```java
public static void main(String[] args) {
    Integer[] arr = {1, 2, 3, 0};
    Observable<Integer> observable = Observable.fromArray(arr);
    observable.map(i -> 6 / i)
        .doOnNext(i -> System.out.println("doOnNext invoked:" + i))
        .doOnError(e -> System.out.println("doOnError invoked:" + e.getMessage()))
        .doOnComplete(() -> System.out.println("doOnComplete invoked"))
        .doOnTerminate(() -> System.out.println("doOnTerminate invoked"))
        .doFinally(() -> System.out.println("doFinally invoked"))
        .subscribe(i -> System.out.println(i));
    System.out.println("main end");
}
/* 
onComplete, onError -> doOnTerminate -> doFinally
dispose -> doFinally

출력결과
6
3
2
doOnError invoked:/ by zero
doOnTerminate invoked
doFinally invoked
main end
 */
```


## 예외처리


```java
public static void main(String[] args) {
    Observable<String> observable = Observable.create(observableEmitter -> {
        observableEmitter.onNext("hello");
        if (true)
            throw new IllegalArgumentException("test");
        observableEmitter.onNext("world");
        observableEmitter.onComplete();
    });
    try {
        observable.subscribe(System.out::println);
    } catch (Exception e) {
        System.out.println(e);
    }
}
```

평볌한 `try, catch` 문으로는 데이터 발행중 오류를 처리할 수 없다.  

```
hello
io.reactivex.exceptions.OnErrorNotImplementedException: The exception was not handled due to missing onError handler in the subscribe() method call. Further reading: https://github.com/ReactiveX/RxJava/wiki/Error-Handling | test
```

동일한 스레드 내에서 진행되지 않기 때문에 `Observable` 에서 제공하는 별도의 메서드를 사용해야 한다.  

데이터 발행시 에러가 발생하면 아래와 같이 동작한다.  

```java
// Observable.java
public final Disposable subscribe(Consumer<? super T> onNext) {
    return subscribe(onNext, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION, Functions.emptyConsumer());
}
```

`subscribe` 호출시 별도의 에러처리 `Consumer` 지정을 하지 않으면 기본 설정인 `Functions.ON_ERROR_MISSING` 으로 지정된다.  

`subscribe` 는 최종적으로 `LambdaObserver` 객체를 생성하여 반환하고 에러 발생시 `LambdaObserver.onError` 메서드가 호출된다.  

```java
// LambdaObserver.java
@Override
public void onError(Throwable t) {
    if (!isDisposed()) {
        lazySet(DisposableHelper.DISPOSED);
        try {
            onError.accept(t);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            RxJavaPlugins.onError(new CompositeException(t, e));
        }
    } else {
        RxJavaPlugins.onError(t);
    }
}
```

에러 발생시 `onError.accept(t)` 로 에러가 전달되고 여기서 `onError` 는 따로 정의하지 않았으면 `subscribe` 메서드에서 지정한 `Functions.ON_ERROR_MISSING` 이다.  

`Functions.ON_ERROR_MISSING` 는 아래와 같다.  

```java
static final class OnErrorMissingConsumer implements Consumer<Throwable> {
    @Override
    public void accept(Throwable error) {
        RxJavaPlugins.onError(new OnErrorNotImplementedException(error));
    }
}
```

```java
// RxJavaPlugins.onError
public static void onError(@NonNull Throwable error) {
    Consumer<? super Throwable> f = errorHandler;
    if (error == null) {
        error = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
    } else {
        if (!isBug(error)) {
            error = new UndeliverableException(error);
        }
    }
    if (f != null) {
        try {
            f.accept(error);
            return;
        } catch (Throwable e) {
            // Exceptions.throwIfFatal(e); TODO decide
            e.printStackTrace(); // NOPMD
            uncaught(e);
        }
    }
    error.printStackTrace(); // NOPMD
    uncaught(error);
}

static void uncaught(@NonNull Throwable error) {
    Thread currentThread = Thread.currentThread();
    UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
    handler.uncaughtException(currentThread, error);
}
```

### onErrorReturn

![image34](/assets/java/reactive-java/image34.png)  

에러가 발생하면 `onErrorReturn` 에 설정한 기본값을 전달하고 `onComplete` 이벤트 발행, `onError` 이벤트는 발행되지 않는다.  


```java
public static void main(String[] args) {
        Observable<String> observable = Observable.create(observableEmitter -> {
            observableEmitter.onNext("hello");
            if (true)
                throw new IllegalArgumentException("test");
            observableEmitter.onNext("world");
            observableEmitter.onComplete();
        });
        try {
            observable
                .onErrorReturn(e -> {
                    if (e instanceof IllegalArgumentException)
                        System.out.println(e.getMessage());
                    return "error";
                })
                .doOnError(e -> System.out.println("doOnError invoked:" + e))
                .doOnComplete(() -> System.out.println("doOnComplete invoked"))
                .subscribe(System.out::println);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
// 출력결과
// hello
// test
// error
// doOnComplete invoked
```


### onErrorResumeNext

![image35](/assets/java/reactive-java/image35.png)  

에러 발생시 발행 데이터를 대체하는 것이 아닌 `Observable` 를 대체해버린다.  

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.create(observableEmitter -> {
        observableEmitter.onNext("hello");
        if (true)
            throw new IllegalArgumentException("test");
        observableEmitter.onNext("world");
        observableEmitter.onComplete();
    });
    try {
        observable
            .onErrorResumeNext(Observable.fromArray("one","two"))
            .doOnError(e -> System.out.println("doOnError invoked:" + e))
            .doOnComplete(() -> System.out.println("doOnComplete invoked"))
            .subscribe(System.out::println);
    } catch (Exception e) {
        System.out.println(e);
    }
}
// 출력결과
// hello
// one
// two
// doOnComplete invoked
```

아래와 같이 예외 객체를 가져올 수 도 있음.  

```java
onErrorResumeNext((Throwable throwable) -> Observable.fromArray("one", "two"))
```

### retry, retryUntil

![image36](/assets/java/reactive-java/image36.png)  

에러 발생시 구독자를 재 구독하여 데이터를 다시 발행한다.  
`retry count` 만 지정할 수 있으며 내부 람다식(`BiPredicate`)을 사용해 `sleep` 과 같이 쓸 수 있다.  

성공 실패 여부는 반환 `boolean` 값으로 설정할 수 있다.  

```java
public static void main(String[] args) {
    Observable<String> observable = Observable.create(observableEmitter -> {
        observableEmitter.onNext("hello");
        if (true)
            throw new IllegalArgumentException("test");
        observableEmitter.onNext("world");
        observableEmitter.onComplete();
    });
    try {
        observable
            //.retry(3)
            .retry((i, e) -> {
                System.out.println(i + ":" + e.getMessage());
                Thread.sleep(1000l);
                return i <=3 ? true : false;
            }).onErrorResumeNext(Observable.fromArray("one", "two"))
            .doOnError(e -> System.out.println("doOnError invoked:" + e))
            .doOnComplete(() -> System.out.println("doOnComplete invoked:"))
            .subscribe(System.out::println);
    } catch (Exception e) {
        System.out.println(e);
    }
}
// 출력결과
// hello
// 1:test
// hello
// 2:test
// hello
// 3:test
// hello
// 4:test
// one
// two
// doOnComplete invoked:
```

결국엔 실패하여 `onErrorResumeNext` 이벤트가 발행된다.  

> `retryUntil` 로 성공할 때 까지 실행할 수 있음.  

### retryWhen

![image37](/assets/java/reactive-java/image37.png)  

재시작 조건을 동적으로 설정해야 할 때 사용한다.  

```java
public final Observable<T> retryWhen(
        final Function<? super Observable<Throwable>, ? extends ObservableSource<?>> handler) {
    ObjectHelper.requireNonNull(handler, "handler is null");
    return RxJavaPlugins.onAssembly(new ObservableRetryWhen<T>(this, handler));
}
```

원형이 복잡한데 매개변수로 에러 `Observable` 을 받아 반환값으로 에러 발행 데이터를 변조한 `Observable` 를 반환하는 `Function` 함수형 파라미터를 요구한다.  

```java
public static void main(String[] args) throws InterruptedException {
    Observable<String> observable = Observable.create(observableEmitter -> {
        observableEmitter.onNext("hello");
        if (true)
            throw new IllegalArgumentException("test");
        observableEmitter.onNext("world");
        observableEmitter.onComplete();
    });
    try {
        observable
            .retryWhen(attempts -> {
                System.out.println("retryWhen invoked");
                return attempts.zipWith(
                    Observable.range(1, 5), (e, i) -> i)
                    .flatMap(i -> {
                        System.out.println("delay retry by " + i + " second(s)");
                        if (i < 3) return Observable.timer(i, TimeUnit.SECONDS);
                        return Observable.error(new TimeoutException("time out!"));
                    });
            })
            .onErrorResumeNext(Observable.fromArray("one", "two"))
            .doOnError(e -> System.out.println("doOnError invoked:" + e))
            .doOnComplete(() -> System.out.println("doOnComplete invoked"))
            .subscribe(System.out::println);
    } catch (Exception e) {
        System.out.println(e);
    }
    Thread.sleep(20000);
}
// 출력결과
// retryWhen invoked
// hello
// delay retry by 1 second(s)
// hello
// delay retry by 2 second(s)
// hello
// delay retry by 3 second(s)
// one
// two
// doOnComplete invoked:
```
`zipWith, range, flatMap` 을 통해 최대 3회 retry 할 수 있도록 설정  
카운트 미초과시 `timer Observable` 을 반환, 3회 초과시 `Observable<Throwable>` 반환  

최상위 `Observable` 에선 에러 발생시 `retryWhen` 에 설정된 발행자에 데이터가 추가되어 동적으로 재실행할 지 끝낼지 정할 수 있다.   

## Reactive Stream

`RxJava, Reactor, Akka` 등 각 라이브러리 밴더사에서 같은 스펙을 사용해 이용자들이 일관된 사용방법으로 코딩할 수 있도록 하기 위한 연동을 위해 정해둔 스팩이다.  

> https://github.com/reactive-streams/reactive-streams-jvm

`RxJava` 역시 `2.x` 버전부터 지원 시작한다.  


```java
import io.reactivex.Observable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Publisher<String> publisher = s -> { // subscribe() 구현 익명 클래스
            s.onNext("hello publisher");
            s.onComplete();
        };
        Observable<String> observable = Observable.fromPublisher(publisher);
        observable.subscribe(System.out::println); // hello publisher
    }
}
```

`org.reactivestreams` 패키지에 주목   

실제 `org.reactivestreams` 패키지에 정의된 자바파일을 보면 `java9` 에 정의된 `Flow` 패키지의 자바파일 이름부터 구성까지 모두 동일하다.(`Processor`, `Publisher`, `Subscriber`, `Subscription`)  

아직 이전버전의 jdk 유저를 위해 `org.reactivestreams` 패키지에 별도 정의해두었지만 위에서 설명한 `java9 - Flow` 패키지의 스펙과 일치한다.  

이제 `RxJava` 외의 다른 라이브러리나 프레임워크를 사용해도 `Publisher, Subscriber` 를 지원하는 코드만 작성한다면 쉽게 연동이 가능하다.  

![image18](/assets/java/reactive-java/image18.png)  

### RxJava to Reactive Stream  

> https://github.com/ReactiveX/RxJavaReactiveStreams  
위의 git 주소에서 `RxJava` 개발자들이 제공하는 `RxJava` 와 `Reactive Stream` 호환을 위한 `dependency` 를 먼저 적용해야한다.  

`RxJava` 의 `Observable` 을 `Reactive Strema` 의 `Publisher` 로 변경,  
`RxReactiveStreams.toPublisher` 고정 메서드를 통해 `Observable -> Publisher` 변경이 가능하다.  

> 역으로 `toObservable` 을 사용해 `Publisher -> Observable` 으로 변경 가능  

```java
@Service
public class RxLogService implements LogService {
    final HttpClient<ByteBuf, ByteBuf> rxClient =
            HttpClient.newClient(new InetSocketAddress(8080));
    @Override
    public Publisher<String> stream() {
        Observable<String> rxStream = rxClient.createGet("/logs")
                .flatMap(HttpClientResponse::getContentAsServerSentEvents)
                .map(ServerSentEvent::contentAsString);
        return RxReactiveStreams.toPublisher(rxStream);
    }
}
```

사실 `RxJava 1.x` 버전에서 자주 사용하던 라이브러리라 `RxJava 2.x` 부터는 위의 `Observable` 스팩을 따르면서 `Publisher` 를 구현한 `Flowable` 을 사용한다(아래 설명).  

단순 `Observable` 을 `Publisher` 로 변경하는 것 보단  
푸시전용 발행자는 `Observable`, 배압관리는 `Flowable` 객체를 사용하는 것을 권장한다.    
 
### RxJava Flowable

`org.reactivestreams.Publisher` 클래스를 구현하는 `Reactive Stream` 을 위한 클래스이다.  
`Observable` 의 배압문제를 해결했다.  

> http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html


```java
public interface Publisher<T> {
    public void subscribe(Subscriber<? super T> s);
}
public abstract class Flowable<T> implements Publisher<T> {
    static final int BUFFER_SIZE;
    static {
       // 기본적으로 데이터 저장용으로 128개의 버퍼를 내장하고 있다
        BUFFER_SIZE = Math.max(1, Integer.getInteger("rx2.buffer-size", 128));
    }
    ...
}
```

`toFlowable` 메서드로 `Observable -> Publihser` 변환이 가능  

```java
Observable<Long> observable = Observable.interval(100, TimeUnit.MILLISECONDS);
Flowable<Long> flowable = observable.toFlowable(BackpressureStrategy.LATEST);
Publisher<Long> publisher = flowable;
```

```java
public static void main(String[] args) throws InterruptedException {
    Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS);
    Publisher<Long> publisher = observable.toFlowable(BackpressureStrategy.LATEST);
    publisher.subscribe(new Subscriber<Long>() {

        private Subscription subscription;
        
        @Override
        public void onSubscribe(Subscription s) {
            System.out.println("subscribe start");
            System.out.println(s.getClass().getCanonicalName());
            this.subscription = s;
            s.request(10);
        }
        @Override
        public void onNext(Long aLong) {
            System.out.print(aLong + ",");
            if (aLong.longValue() == 15l)
                this.subscription.cancel();
            if (aLong % 10 == 9) {
                System.out.println();
                this.subscription.request(10);
            }
        }
        @Override
        public void onError(Throwable t) {}
        @Override
        public void onComplete() {}
    });
    Thread.sleep(20000);
}
// 출력결과
// subscribe start
// io.reactivex.internal.subscribers.StrictSubscriber
// 0,1,2,3,4,5,6,7,8,9,
// 10,11,12,13,14,15,
```

> `org.reactivestreams.FlowAdapters` 클래스를 사용해 `reactivestreams.Publisher` 객체를 `Flow.Publisher` 로도 변경가능하다.  



### Observable, Flowable 선택 기준  

데이터가 발핼되는 업스트림 속도보다 데이터를 처리하는 다운스트림 속도가 차이가 많이 날 경우 사용한다.  

> `Observable` 흐름제어 메서드를 사용하여 처리 불가능할 경우 사용하는 것이 좋다  

#### Observable을 선택하는 기준  

* 최대 1000개 미만의 데이터 흐름, Out of Memory Exception 이 발생할 확률이 적은 경우  
* 마우스, 터치 이벤트를 다루는 GUI 프로그래밍, 초당 1000회 이하의 이벤트를 다룸  

#### Flowable을 선택하는 기준

* 10000개 이상의 데이터를 처리하는 경우, 메서드 체인에서 데이터 소스에 데이터 개수 제한을 요청해야 함  
* 디스크에서 파일을 읽어 들일 경우  
* JDBC를 활용해 데이터베이스의 쿼리 결과를 가져오는 경우  
* 네트워크 I/O를 실행하는 경우 ( 서버에서 가져오길 원하는 만큼의 데이터양을 요청할 수 있을 때 )  

### onBackPressure - 배압 이슈 대응  

`Flowable` 에 제공되는 버퍼를 기반으로 배압 전략을 설정할 수 있다.  
`onBackPressureBuffer` - 배압 이슈 발생시 별도의 버퍼에 저장 발행된 데이터를 저장한다.  
`onBackPressureDrop` - 배압 이슈 발생시 데이터 무시  
`onBackPressureLatest` - 배압 이슈 발생시 데이터 무시 및 최신 데이터만 유지  

```java
// 발행자 생성
PublishSubject<Integer> subject = PublishSubject.create();
// 구독 메서드 등록
subject.observeOn(Schedulers.computation())
    .subscribe(i -> {
        Thread.sleep(1000);
        System.out.println(System.currentTimeMillis() + ":" + i);
    }, err -> System.err.println(err));
// 데이터 발생 5천만회
for (int i = 0; i < 50_000_000; i++) {
    subject.onNext(i);
}
subject.onComplete();
Thread.sleep(100000);
```

`subscribe`에서 1초마다 데이터 하나씩 처리할 동안 5천만회에 달하는 데이터가 발행된다.  
단순 구독/발행 패턴을 사용하면 위와 같은 코드가 많아지면 메모리 이슈가 발생하고 전체 시스템에 영향을 끼친다.  

`Flowable` 을 사용해 배압처리적용  

```java
public static void main(String[] args) throws InterruptedException {
    // BackpressureOverflowStrategy.ERROR - MissingBackPressureException 예외 발생
    // BackpressureOverflowStrategy.DROP_LATEST - 버퍼의 최근 데이터 제거
    // BackpressureOverflowStrategy.DROP_OLDEST - 버퍼의 가장 오래된 데이터 제거
    Flowable.range(1, 50_000_000)
        .onBackpressureBuffer(128, () -> {
            System.err.println("overflow data!");
        }, BackpressureOverflowStrategy.DROP_OLDEST) // 버퍼가 가득 찼을때 추가전략
        //.onBackpressureDrop()
        //.onBackpressureLatest()
        .observeOn(Schedulers.computation())
        .subscribe(i -> {
            Thread.sleep(1000);
            System.out.println(System.currentTimeMillis() + ":" + i);
        }, err -> System.err.println(err));
    Thread.sleep(100000);
}
```

## TCK

> TCK: (`Reactive Stream Technology Compatibility Key`: 리액티브 스트릠 기술 호환성 키트)  
> <https://github.com/reactive-streams/reactive-streams-jvm/tree/master/tck>

`Reactive Stream` 을 사용하기 위한 수많은 규칙들이 정의되어 있고 모든 벤더사들은 해당 규칙을 준수하여 호환성을 보장한다.  

이런 규칙들을 테스트하기 위한 툴킷이 이미 정의되어 있으며 TCK 라 한다.  

TCK 안에는 아래 4가지 종류의 클래스가 이미 정의되어 있으며  

1. `PublisherVerification`  
2. `SubscriberWhiteboxVerification`  
3. `SubscriberBlackboxVerification`  
4. `IdentityProcessorVerification`  

위 클래스를 상속하여 테스트 하는 것만으로도 구현된 `Publisher, Subscriber, Processor` 객체의 검증이 가능하다.  
