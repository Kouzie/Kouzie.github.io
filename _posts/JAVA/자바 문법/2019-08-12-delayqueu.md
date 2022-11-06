---
title:  "java - DelayQueue!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - java
---

`DelayQueue`는 `BlockingQueue`와 `PriorityQueue`의 기능을 가지고 있는 구현체이다.

`BlockingQueue` 는 스레드 동기화를 위해 생긴 인터페이스이다.  

`take, put` 메서드를 사용할 경우 **꺼낼 요소가 없거나 이미 너무 많은 요소가 들어가있어 요소를 더이상 넣을 수 없다면** 해당 메서드를 호출한 스레드는 넣거나 뺄 수 있을 때까지 기다리게된다.  

`PriorityQueue` 는 지정한 값의 우선순위에 따라 데이터가 빠져나오는 순위가 달라진다.  

`DelayQueue`의 요소로는 `Delayed` 인터페이스의 구현체만 들어갈 수 있는데 

모든 요소는 `Delayed`의 `getDelay()` 오버라이드 메서드를 구현해야 한다.  
이 `getDelayed()`의 반환값이 크면 클수록 우선순위가 빨라진다.  

보통 요소에는 해당 요소가 생성되고 등록된 시간을 필드로 지정하고 `getDelayed()`를 통해 `현재시간 - 등록시간` 계산을 많이 함으로  
대부분의 `DelayQueue`에서 가장 빨리 등록된 요소가 가장 높은 우선순위를 가지게 된다.  

먼저 `DelayedQueue`의 요소로 사용할 클래스 정의  

```java
class DelayedEvent implements Delayed
{
    private long id;
    private String name;
    private LocalDateTime activationDateTime;
 
    public DelayedEvent(long id, String name, LocalDateTime activationDateTime) {
        super();
        this.id = id;
        this.name = name;
        this.activationDateTime = activationDateTime;
    }
 
    public long getId() {
        return id;
    }
 
    public String getName() {
        return name;
    }
 
    public LocalDateTime getActivationDateTime() {
        return activationDateTime;
    }
 
    @Override
    public int compareTo(Delayed that)
    {
        long result = this.getDelay(TimeUnit.NANOSECONDS)
                        - that.getDelay(TimeUnit.NANOSECONDS);
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        }
        return 0;
    }
 
    @Override
    public long getDelay(TimeUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        long diff = now.until(activationDateTime, ChronoUnit.MILLIS);
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }
 
    @Override
    public String toString() {
        return "DelayedEvent [id=" + id + ", name=" + name + ", activationDateTime=" + activationDateTime + "]";
    }
}
```

> 출처: https://howtodoinjava.com/java/multi-threading/java-delayqueue/  

그리고 다음 메인함수를 정의  

```java
public class TestDelayQueue {
    public static void main(String[] args) {
        System.out.println("test delay queue");
        DelayQueue<DelayedEvent> queue = new DelayQueue<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            queue.offer(new DelayedEvent(i, "user" + i, now.plusSeconds(i)));
            System.out.println("add " + i);
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DelayedEvent event = null;
        while ((event = queue.poll()) != null) {
            System.out.println(event);
            System.out.println(Duration.between(event.getActivationDateTime(), LocalDateTime.now()).toMillis());
        }
    }
}
```
`Thread.sleep`을 통해 4초간 쉬었다 `while`문으로 꺼낼 수 있는 요소를 꺼내 출력한다.  

결과값

```
test delay queue
add 0
add 1
add 2
add 3
add 4
add 5
add 6
add 7
add 8
add 9
DelayedEvent [id=0, name=user0, activationDateTime=2019-08-13T13:43:14.379]
4012
DelayedEvent [id=1, name=user1, activationDateTime=2019-08-13T13:43:15.379]
3013
DelayedEvent [id=2, name=user2, activationDateTime=2019-08-13T13:43:16.379]
2013
DelayedEvent [id=3, name=user3, activationDateTime=2019-08-13T13:43:17.379]
1014
DelayedEvent [id=4, name=user4, activationDateTime=2019-08-13T13:43:18.379]
14
```
딱 5개까지, 심지어 마지막은 아슬아슬하게 0.014초 넘겨서 출력되고 메인함수가 끝나버렸다.  
`poll` 메서드의 경우엔 스레드가 기다리고 그런거 없이 Delay때문에 못꺼내면 그냥 `null`반환한다.  

즉 `현재시간 - 지정한시간` 이 0보다 커야 (`getDelay()`메서드 반환값이 0보다 커야) `DelayQueue`에서 꺼낼 수 있다.


이번엔 for문을 `for (int i = 10; i > 0; i--)`으로 설정해서 큐에 집어넣고 4초후에 꺼내보도록 설정.  

```java
public class TestDelayQueue {
    public static void main(String[] args) {
        System.out.println("test delay queue");
        DelayQueue<DelayedEvent> queue = new DelayQueue<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 10; i > 0; i--) {
            queue.offer(new DelayedEvent(i, "user" + i, now.plusSeconds(i)));
            System.out.println("add " + i);
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DelayedEvent event = null;
        while ((event = queue.poll()) != null) {
            System.out.println(event);
            System.out.println(Duration.between(event.getActivationDateTime(), LocalDateTime.now()).toMillis());
        }
    }
}
```

출력값
```
test delay queue
add 10
add 9
add 8
add 7
add 6
add 5
add 4
add 3
add 2
add 1
DelayedEvent [id=1, name=user1, activationDateTime=2019-08-13T13:44:11.634]
3011`
DelayedEvent [id=2, name=user2, activationDateTime=2019-08-13T13:44:12.634]
2012
DelayedEvent [id=3, name=user3, activationDateTime=2019-08-13T13:44:13.634]
1013
DelayedEvent [id=4, name=user4, activationDateTime=2019-08-13T13:44:14.634]
13
```
분명 역순으로 집어넣었는데 `id=1`부터 출력된다. `Priority`가 있기때문

`take` 메서드를 사용해서 `DelayQueue`안의 요소를 꺼내보자. 꺼낼 메서드가 없다면 꺼낼수 있을때 까지 스레드는 잠들게된다.

```java
public class TestDelayQueue {
    public static void main(String[] args) {
        System.out.println("test delay queue");
        DelayQueue<DelayedEvent> queue = new DelayQueue<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 10; i > 0; i--) {
            queue.offer(new DelayedEvent(i, "user" + i, now.plusSeconds(i)));
            System.out.println("add " + i);
        }

        DelayedEvent event = null;
        try {
            while ((event = queue.take()) != null) {
                System.out.println(event);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
```
`Thread.sleep()` 메서드도 지웠고 바로 출력시킨다.  

1초에 하나씩 출력된다.
```
test delay queue
add 10
add 9
add 8
add 7
add 6
add 5
add 4
add 3
add 2
add 1
DelayedEvent [id=1, name=user1, activationDateTime=2019-08-13T13:55:41.091]
DelayedEvent [id=2, name=user2, activationDateTime=2019-08-13T13:55:42.091]
DelayedEvent [id=3, name=user3, activationDateTime=2019-08-13T13:55:43.091]
DelayedEvent [id=4, name=user4, activationDateTime=2019-08-13T13:55:44.091]
DelayedEvent [id=5, name=user5, activationDateTime=2019-08-13T13:55:45.091]
DelayedEvent [id=6, name=user6, activationDateTime=2019-08-13T13:55:46.091]
DelayedEvent [id=7, name=user7, activationDateTime=2019-08-13T13:55:47.091]
DelayedEvent [id=8, name=user8, activationDateTime=2019-08-13T13:55:48.091]
DelayedEvent [id=9, name=user9, activationDateTime=2019-08-13T13:55:49.091]
DelayedEvent [id=10, name=user10, activationDateTime=2019-08-13T13:55:50.091]
```

일반적으로 Delay큐는 스레드와 같이 사용한다.  

```java
public class TestDelayQueue implements InitializingBean {
	private DelayQueue<DelayedEvent> delayQueue = new DelayQueue<DelayedEvent>();

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

    public void init() {
		new DelayQueueConsumeThread().start();
	}

    class DelayQueueConsumeThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    if (delayQueue.isEmpty() == false) {
                            DelayedEvent message = delayQueue.take();
                            ...
                            ...
                        } else {
                            Thread.sleep(50);
                        }
                } else {
                    Thread.sleep(50);
                }
            } 
        }
    }
}
```