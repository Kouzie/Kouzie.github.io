---
title:  "java 9 - Future!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---



## Future

하나의 웹 어플리케이션 구현시 하나의 서비스만 제공하는 것이 아닌 여러개의 서비스를 엮어서 사용자에게 제공한다(메시업 형태).  

여러개의 서비스가 엮이면서 하나의 서비스에서 병목현상이 일어나면 그 이후의 서비스까지 병목되는데  
이를 해결하기 위해 계속 뛰어난 병렬처리 방식이 아닌 **동시처리(Concurrency) 방식**이 나오고 있으며 자바의 `Future` 또한 그중 하나이다.  

![image15](/assets/java/java/image15.png){: .shadow}  

병렬 처리(Parallelism)는 2개의 작업을 동시에 처리하는 것이고  
동시 처리(Concurrency)는 2개의 작업중 하나가 멈추었을때 이어서 남은 작업을 진행한다.  

java 8 에서 `Future`, `Flow` 등의 동시처리 가능한 독립적인 테스크 지원이 가능하다.  

```java
public interface Future<V> {

    boolean cancel(boolean mayInterruptIfRunning);

    boolean isCancelled();

    boolean isDone();

    V get() throws InterruptedException, ExecutionException;

    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```

직관적인 구조를 가지고 있으며 아래와 같은 방법으로 사용한다.  

```java
public static void main(String[] args) throws ExecutionException, InterruptedException {
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    Future<Integer> y = executorService.submit(() -> f(x));
    Future<Integer> z = executorService.submit(() -> g(x));
    System.out.println(y.get() + z.get());
    executorService.shutdown();
}
```
간단하게 멀티 스레드를 생성할 수 있는 `ExecutorService` 를 사용해 프로세스를 진행하는 `Future` 객체가 생성되면 향후 `get()` 메서드를 사용해 결과를 가져올 때 까지 기다린다.  

> `executorService.shutdown()` 을 통해 모든 스레드를 종료하지 않으면 `main` 이 종료되지 않는다. 

## 박스 채널 모델

![image16](/assets/java/java/image16.png){: .shadow}  

아래와 같은 코드로 표현할 수 있는 그림이다.  

```java
int t = p(x);
int r = r(q1(t), q2(t));
```

만약 동시성을 갖추고 위 코드를 실행하고 싶다면 각 메서드를 박스(`Future`)로 감싸 사용하면 된다.  

```java
int x = 1;
int t = p(x);
Future<Integer> qr1 = excutorService.submit(() -> q1(t));
// 실제 반환 객체는 Future 를 구현하는 FutureTask
Future<Integer> qr2 = excutorService.submit(() -> q2(t));
int r = r(qr1.get(), qr2.get());
System.out.println(r);
```

지금은 박스에 하나의 간단한 함수만 포함하고 있지만  
박스가 커지고 박스내부에 또 다른 박스가 존재한다면 무작정 각 박스(`Future`)가  끝나기를 기다릴 순 없다.  

만약 `q1` 과 `q2` 가 어마어마한 연산량이 필요한 메서드라면 `get()` 호출마다 병목이 발생할 것이고  
결국 `r(...)` 호출 라인에서 많은시간이 소비된다.  

`get()` 메서드를 호출때 마다 박스의 결과를 기다리기에 블록이 발생함으로 이를 최소화 해야한다.  

> 물론 일부 상황해 한해 **데이터 동기화**를 위해 특정 연산 완료까지 대기해야 하는 상황이 발생할 때에는 당연히 대기해야한다.  
> 얼마나 `Future` 객체의 결과를 데이터 동기화가 필요한 위치까지 효율적으로 끌고 가는지가 핵심이다.  


## CompletableFuture

java 9 추가된 클래스로 `Future` 구현클래스이다.  

```java
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {...}
```

`CompletableFuture`는 박스 채널 모델의 문제점을 해결(효율적으로 결과값을 처리)하기 위해 **콤비네이터 메서드**를 사용할 수 있다.  

> `CompletionStage` 인터페이스에 여러가지 콤비네티어 메서드가 정의되어 있다.  

### complete, thenCombine  

`complete` - `Future` 의 연산이 끝나지 않을 경우 **반환값을 강제로 지정**하여 종료  
`thenCombine` - 현재 `CompletionStage` 와 매개변수로 들어온 `CompletionStage` 를 합쳐 새로운 `CompletionStage` 를 반환, 2개의 `Future` 를 합침.

```java
public static void main(String[] args) throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    int x = 100;
    CompletableFuture<Integer> a = new CompletableFuture<>();
    CompletableFuture<Integer> b = new CompletableFuture<>();
    CompletableFuture<Integer> c = a.thenCombine(b, (y, z) -> y + z); // a, b 연산이 끝난 thenCombine 두번째 파라미터인 람다식 진행
    executorService.submit(() -> a.complete(f(x))); // 트리거 발생, 연산 시작
    executorService.submit(() -> b.complete(g(x)));
    System.out.println(c.get()); // 람다식 결과 대기 
    executorService.shutdown();
}

public static int f(int x) {
    System.out.println("f(x) invoked");
    return x * 2;
}
public static int g(int x) {
    System.out.println("g(x) invoked");
    return x + 1;
}
```

결과값

```
f(x) invoked
g(x) invoked
301
```

병렬실행의 효율성은 높이고 병목현상은 최소화 한다.  


### thenApply

> This method is analogous to {@link java.util.Optional#map Optional.map} and {@link java.util.stream.Stream#map Stream.map}.  

```java
public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);
```

스트림의 `map` 과 유사한 메서드로 인스턴스의 `CompletionStage` 가 무사히 끝난 후 새로운 `CompletionStage` 로 변환하여 반환.

```java
CompletableFuture<Integer> a = new CompletableFuture<>();
CompletableFuture<Long> r = a.thenApply(value -> value.longValue());
a.complete(5);
System.out.println(r.get()); // 5
```

`thenApply` 안에서 동기 메서드로 작성된다.  


### thenCompose

> This method is analogous to {@link java.util.Optional#flatMap Optional.flatMap} and {@link java.util.stream.Stream#flatMap Stream.flatMap}.

```java
public <U> CompletionStage<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn);
```

스트림의 `flatMap` 과 유사한 메서드로 `thenApply` 와 기능이 거의 유사하나 `Function` 의 반환요소가 `CompletionStage` 로 감쌓여 있다.  

```java
CompletableFuture<Integer> a = new CompletableFuture<>();
CompletableFuture<Long> r = a.thenCompose(value -> CompletableFuture.supplyAsync(() -> value.longValue()));
a.complete(5);
System.out.println(r.get()); // 5
```

`supplyAsync` 를 통해 `CompletionStage<Long>` 을 반환한다.  

`thenApply`, `thenCompose` 메서드의 반환값은 모두 `CompletionStage<U>` 이고 `then...` 이기에 둘다 선행된 `CompletionStage` 가 끝난 후 실행된다  
`thenCompose` 는 여러개의 `CompletionStage`를 하나의 `CompletionStage` 재 조합하고 `thenApply` 는 여러개의 `CompletionStage`를 각각 실행하고 새로운 `CompletionStage` 를 반환한다고 생각하면 된다.  


### 비동기 메서드 안에서 예외전달  

간단한 지연 발생 메서드를 생성 

```java
@Getter
@AllArgsConstructor
public enum Code {
    NONE(0),
    SILVER(5),
    GOLD(10),
    PLATINUM(15),
    DIAMOND(20);
    private final int percentage;
}

@Getter
public class Shop {

  private final String name;
  private final Random random;

  public Shop(String name) {
    this.name = name;
    random = new Random(name.charAt(0) * name.charAt(1) * name.charAt(2));
  }

  public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
      double price = calculatePrice(product);
      futurePrice.complete(price); // complete 로 Future 종료
    }).start();
    return futurePrice;
  }

  public double calculatePrice(String product) {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return format(random.nextDouble() * product.charAt(0) + product.charAt(1));
  }
}
```

`Shop` 의 `calculatePrice` 호출마다 지연이 발생한다.  

```java
public static void main(String[] args) {
    Shop shop = new Shop("BestShop");
    long start = System.nanoTime(); // 시작시간
    Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
    long invocationTime = ((System.nanoTime() - start) / 1_000_000); //연산 시작시간
    System.out.println("Invocation returned after " + invocationTime + " msecs");
    // 다른 상점 질의 같은 다른 작업 수행
    doSomethingElse();
    try {
        double price = futurePrice.get();
        System.out.printf("Price is %.2f%n", price);
    } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
    }
    long retrievalTime = ((System.nanoTime() - start) / 1_000_000); // 연산 종료시간
    System.out.println("Price returned after " + retrievalTime + " msecs");
}

private static void doSomethingElse() {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("Doing something else...");
}
```

결과값

```
Invocation returned after 4 msecs
Doing something else...
Price is 123.26
Price returned after 2037 msecs
```

동기메서드만 사용한다면 `calculatePrice` 에서 2초, `doSomethingElse` 에서 1초 총 3초가량이 소비되어야 하지만 비동기 형식 사용시 대략 2초정도이다.  

`getPriceAsync` 사이에 **예외**가 발생하도록 설정.  

```java
public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
        double price = calculatePrice(product);
        if (true) throw new RuntimeException("something execption");
        futurePrice.complete(price);
    }).start();
    return futurePrice;
}
```

```
Invocation returned after 3 msecs
Doing something else...
Exception in thread "Thread-0" java.lang.RuntimeException: something execption
	at modernjavainaction.chap16.v1.Shop.lambda$getPriceAsync$0(Shop.java:32)
	at java.base/java.lang.Thread.run(Thread.java:834)

```

`get()` 으로 데이터를 가져올 수 있을때 까지 영원히 기다린다.  
`Future` 안에서 발생한 예외를 밖으로 전달해야 한다.  

```java
public Future<Double> getPriceAsync(String product) {
    CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
        try {
            double price = calculatePrice(product); // 예외 발생
            futurePrice.complete(price); // 가격정보를 저장하고 종료
        } catch (NullPointerException e) {
            futurePrice.completeExceptionally(e);
        }
    }).start();
    return futurePrice;
}
public double calculatePrice(String product) {
    try {
        Thread.sleep(2000);
        if (true) throw new NullPointerException("something execption");
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return format(random.nextDouble() * product.charAt(0) + product.charAt(1));
}
```

`completeExceptionally()` 예외 설정으로 인해 `get()` 메서드 호출시 `ExecutionException` 예외가 발생한다.  

```java
try {
    double price = futurePrice.get(); // 전달된 예외 발생
    System.out.printf("Price is %.2f%n", price);
} catch (ExecutionException | InterruptedException e) {
    System.out.println("name:" + e.getClass().getName() + ", message:" + e.getMessage());
    // name:java.util.concurrent.ExecutionException, message:java.lang.NullPointerException: something execption
    throw new RuntimeException(e);
}
```

### supplyAsync

```java
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {...}
```


`CompletableFuture.supplyAsync` 메서드를 사용해 쉽게 `CompletableFuture` 객체를 생성하고 실행할 수 있다.  

```java
public Future<Double> getPriceAsync(String product) {
    /* CompletableFuture<Double> futurePrice = new CompletableFuture<>();
    new Thread(() -> {
        try {
            double price = calculatePrice(product);
            futurePrice.complete(price); // 가격정보를 저장하고 종료
        } catch (NullPointerException e) {
            futurePrice.completeExceptionally(e);
        }
    }).start();
    return futurePrice; */
    return CompletableFuture.supplyAsync(() -> calculatePrice(product));
}
```

예외발생시 내부적으로 `completeExceptionally()` 를 호출하기 때문에 `calculatePrice()` 메서드 안에서 예외가 발생하면 `get()` 에서 예외가 전달된다.  

아래처럼 `Collection` 과 함께 비동기 처리가 가능하다.  

```java
public double getPrice(String product) { // shop 의 getPrice 메서드
    delay(); // 1초 sleep
    return random.nextDouble() * product.charAt(0) + product.charAt(1);
}

private final List<Shop> shops = Arrays.asList(
    new Shop("BestPrice"),
    new Shop("LetsSaveBig"),
    new Shop("MyFavoriteShop"),
    new Shop("BuyItAll"));
    
public List<String> findPricesFuture(String product) {
    List<CompletableFuture<String>> priceFutures =
        shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() ->
                shop.getName() + " price is " + shop.getPrice(product))) // 비동기로 전부 실행
            .collect(Collectors.toList());

    List<String> prices = priceFutures.stream()
        .map(CompletableFuture::join) // 위에서 실행된 메서드 완료 대기. 
        .collect(Collectors.toList());
    return prices;
}
```


> `join` 은 `get` 과 똑같은 기능을 하지만 예외를 처리 블록으로 감쌀 필요가 없다. 만약 `join` 과정에서 예외 발생시 `CompletionException` 예외에 감싸져서 예외가 전달된다.  
 
`getPrice` 호출마다 1초 딜레이가 발생하지만 `stream` 과 `CompletableFuture` 를 사용해 동시에 호출해 동시성이 보장된다.  

밑의 `join` 을 사용해 연산 완료된 데이터만 주서 담으면 된다.  

### 타임아웃

Future 의 연산결과를 무한정 기다릴 수 있기 때문에 특정 시간 이후에는 `orTimeout` 메서드를 사용해 `TimeoutException` 을 발생시키거나 `completeOnTimeout` 메서드로 **기본 값을 반환**하도록 지정할 수 있다.  

```java
CompletableFuture<Double> price = CompletableFuture.supplyAsync(() -> shop.getPrice(product))
    //.completeOnTimeout(DEFAULT_PRICE, 3, TimeUnit.SECONDS)
    .orTimeout(3, TimeUnit.SECONDS);
```



### 스레드 최적화  

위와 같은 경우 `shops` 의 개수가 4개 밖에 안되기 때문에 4코어 이상의 스레드를 가진 컴퓨터에선 1초정도의 시간으로 모두 처리 가능하지만 `shops` 가 들어나면 그만큼의 시간이 추가된다.  

필자의 컴퓨터는 8코어로 main 메서드가 하나의 스레드를 잡아먹고 있어 7개의 `shop` 까지 1초 대로 처리 가능하다.  
아래와 같이 7 + 7 + 1 개의 `shop` 을 가진 경우 총 3초의 시간이 필요하다.  

```java
 private final List<Shop> shops = Arrays.asList(
    new Shop("BestPrice"),
    new Shop("LetsSaveBig"),
    new Shop("MyFavoriteShop"),
    new Shop("Lorem"),
    new Shop("Ipsum"),
    new Shop("Dolor"),
    new Shop("BuyItAll"), // 1초

    new Shop("BestPrice"),
    new Shop("LetsSaveBig"),
    new Shop("MyFavoriteShop"),
    new Shop("Lorem"),
    new Shop("Ipsum"),
    new Shop("Dolor"),
    new Shop("BuyItAll"), // 1초

    new Shop("BestPrice")); // 1초
```

스레드수를 `java.util.concurrent.Executor` 를 통해 조절 가능하다.  

```java
private final Executor executor = Executors.newFixedThreadPool(shops.size(), (Runnable r) -> {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
});
```

> `shops` 의 크기만큼 스레드 풀을 생성한다.  `daemon` 스레드로 생성해 자바 프로그램이 종료될 때 같이 종료될 수 있도록 설정. 스레드 풀 개수는 100 개 이하로 설정하는 것을 권장

```java
public List<String> findPricesFuture(String product) {
    List<CompletableFuture<String>> priceFutures =
        shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(() ->
                shop.getName() + " price is " + shop.getPrice(product), executor))
            .collect(Collectors.toList());
    List<String> prices = priceFutures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    return prices;
}
```

`supplyAsync` 의 2번째 매개변수로 `executor` 지정하여 다시 1초 안에 끝나는지 확인


### thenAccept

```java
public CompletionStage<Void> thenAccept(Consumer<? super T> action);
```

complete 시에 특정작업을 하는 콤비네이터 메서드  

위의 `findPricesFuture` 메서드의 호출 목적은 반환된 `List<String>` 을 아래와 같이 콘솔에 출력하는 것이다.  

```
[BestPrice price is 123.25651664705744, LetsSaveBig price is 169.4653393606115, MyFavoriteShop price is 214.12914480588853, BuyItAll price is 184.74384995303313]
```

지금까지의 `shop.getPrice()` 의 경우 1초 고정지연 이기에 병목현상이 발생하지 않는다.  
하지만 하나의 `shop.getPrice()` 에서 5초지연이 발생할 경우 `map(CompletableFuture::join)` 에서 해당 지연이 끝날 때 까지 병목현상이 발생하며 최종적으로 5초후에 콘솔출력이 진행된다.  

모든 `shop.getPrice()` 가 끝나는 것을 기다릴 필요없이 먼저 끝난 `CompletionStage`내용을 출력하고 싶다면 `thenAccept` 를 사용할 수 있다.  

```java
public static void randomDelay() {
    int delay = Math.max(1000, RANDOM.nextInt(5000));
    try {
        Thread.sleep(delay);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

public double getPrice(String product) { // shop 의 getPrice 메서드
    // delay(); // 1초 sleep
    randomDelay(); // 1 ~ 5 초 sleep
    return random.nextDouble() * product.charAt(0) + product.charAt(1);
}

public List<String> findPricesFuture(String product) {
    List<CompletableFuture<String>> priceFutures = shops.stream()
        .map(shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " price is " + shop.getPrice(product), executor))
        .collect(Collectors.toList());

    List<String> prices = priceFutures.stream()
        .map(future -> {
            future.thenAccept(System.out::println);
            return future; // 출력 및 반환
        })
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
    return prices;
}
```
