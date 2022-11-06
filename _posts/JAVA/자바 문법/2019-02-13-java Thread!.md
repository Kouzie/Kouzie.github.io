---
title:  "java Thread!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

## 프로세스와 스레드

## 각종 용어

1. Process  
실행중인 프로그램: OS로부터 실행에 필요한 자원(메모리)를 할당받아 동작중인 프로그램  
모든 프로세스에는 최소 한개이상 스레드가 있다. 둘 이상의 스레드를 가진 프로세스를 멀티 스레드 프로세스라 한다.  

2. Thread  
하나의 프로세스가 가질수 있는 스레드의 개수는 제한되지 않으나 
스레드가 작업을 할때 필요한 개인적 메모리 공간(호출스택)이 필요하기 때문에 프로세스 메모리 한계에 따라 생성가능한 스레드 수가 결정된다.  

**경량 프로세스(LWP)**: 스레드 하나가 프로세스만큼의 일처리가 가능하기에 Light Weight Process라고도 함.  

**Multi Tasking**: OS에서 여러개의 프로그램(Multi process)을 실행, 자동 관리하는 환경  

**Multi Threading**: 하나의 프로세스 안에서 여러개의 스레드가 작업수행.   


**멀티 스레드의 장점**
1. CPU 사용률 향상  
2. 자원의 효율적 사용(자원 공유)  
3. 사용자와 응답성 향상  
4. 작업이 분리되며 코드가 간결해짐  

**멀티 스레드의 단점**
1. 자원을 공유하기 때문에 동기화 문제가 발생한다.  


**자바에서의 스레드 구현**
1. Thread클래스를 상속  
2. Runnable 인터페이스 구현  

자바는 다중상속이 안되기 때문에 `Runnable` 인터페이스를 `implements`하는 경우가 대부분이다.  


---

## Thread 클래스

### Thread.currentThread  

현재 실행중인 스레드 반환하는 static 메서드
Thread t = Thread.currentThread(); //현재 실행중인 스레드 반환

### getName

현재 실행중인 스레드 이름을 반환하는 인스턴스 메서드
System.out.println(t.getName()); //main 출력(스레드 이름)

---

main 코드
```
Thread t = Thread.currentThread();
String tName = t.getName();
for (int i = 0; i <= 100; i++) {
	System.out.printf("%s 대청소: %d%%\n", tName, i);
}
for (int i = 0; i <= 100; i++) {
	System.out.printf("%s 장보기: %d%%\n", tName, i);
}
```

출력값
```
main 대청소: 0%
main 대청소: 1%
...
main 대청소: 98%
main 대청소: 99%
main 대청소: 100%
main 장보기: 0%
main 장보기: 1%
...
main 장보기: 99%
main 장보기: 100%
```
순차대로 대청소를 100까지 수행하고 장보기를 100까지 수행 후 프로그램이 종료된다.  


스레드를 사용해 순차대로 말고 같이 수행되도록 만들어보자.

먼저 Thread클래스를 상속받는 클래스를 만들어보자 (extends Thread)
```java
class CleaningWorker extends Thread
{
	@Override
	public void run() {
		for (int i = 0; i <= 5; i++) {
			System.out.printf("%s 대청소: %d%%\n", this.getName(), i);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			super.run();
		}
	}
}
```
run을 오버라이딩 받아 자기가 멀티스레드로 동작시킬 코드를 작성.  
스레드가 독립적으로 수행해야 하는 기능을 `run()`이라는 메서드 안에 집어넣기 때문에  
*스레드를 메서드 라고도 부른다.*  

```java
CleaningWorker t1 = new CleaningWorker();
t1.setName("Thread1");
t1.start();
System.out.println("=end=");
```
start메서드가 내부적으로 run을 수행한다.

출력값
```
=end=
Thread1 대청소: 0%
Thread1 대청소: 1%
Thread1 대청소: 2%
Thread1 대청소: 3%
Thread1 대청소: 4%
Thread1 대청소: 5%
```

=end=가 먼저 출력되버렸다....   
main스레드는 Thread1의 진행상황과 관계없이 별도로 수행되기 때문.   
이를 문제로 여기고 해결하려면 *동기화 과정*을 거쳐야한다.  


이번엔 Runnable 인터페이스를 구현하는 클래스를 만들어보자
```java
class ShoppingWorker implements Runnable{
	@Override
	public void run() {
		for (int i = 0; i <= 5; i++) {
			Thread.currentThread().setName("Thread2");
			String tName = Thread.currentThread().getName();
			System.out.printf("%s 장보기: %d%%\n",tName, i);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
}
```
Runnable또한 run()메서드를 필수로 오버라이딩 해야한다.

```java
Runnable shop = new ShoppingWorker();
Thread t2 = new Thread(shop);
t2.start();
System.out.println("=end=");
```
출력값
```
=end=
Thread2 장보기: 0%
Thread2 장보기: 1%
Thread2 장보기: 2%
Thread2 장보기: 3%
Thread2 장보기: 4%
Thread2 장보기: 5%
```

Runnable을 구현한 클래스는 main클래스에서 *생성후 바로 start()메서드로 동작시킬 수 없다.*   
start메서드가 구현필요 메서드도 아니고 별도로 start메서드를 만들지도 않았기 때문.  

보통 Runnable 구현클래스를 단독으로 사용하지 않고 Thread클래스 객체와 같이 사용한다.  
`Thread t2 = new Thread(shop);` 처럼 스레드 생성자 매게변수로 전달하는 방식.  

그냥 `t2.run()`으로 실행하는건 멀티 스레드가 아니라 그냥 함수호출하는 것....    
동시에 실행시키려면 start()메서드를 호출해야 한다.    

그럼 두개의 스레드를 main메서드에서 만들어 같이 수행해보자  
```
CleaningWorker t1 = new CleaningWorker();
t1.setName("Thread1");

Runnable shop = new ShoppingWorker();
Thread t2 = new Thread(shop);
t1.start(); //내부적으로 run 실행
t2.start();
System.out.println("=end=");
```

출력값
```
=end=
Thread2 장보기: 0%
Thread1 대청소: 0%
Thread2 장보기: 1%
Thread1 대청소: 1%
Thread1 대청소: 2%
Thread2 장보기: 2%
Thread2 장보기: 3%
Thread1 대청소: 3%
Thread2 장보기: 4%
Thread1 대청소: 4%
Thread2 장보기: 5%
Thread1 대청소: 5%
```

3개의 스레드가 동작한다. main스레드, Thread1, Thread2.  
sleep메서드를 호출하면 cpu를 다음 프로세스 혹은 스레드에게 넘겨준다(NonRunnable 모드로 넘어가면서).  
eclipse에서 위의 main스레드만 동작하는게 아니기 때문에 순서, 횟수 모두 cpu상황에 맞춰 동작한다.  

----

### 추가 - 익명클래스, 람다식으로 Thread만들기

익명클래스로 Runnable을 구현하는 클래스 만들기  
람다식으로 Thread상속하는 클래스 만들기  

```java
Thread t1 = new Thread(new Runnable() {
	@Override
	public void run() {
		String tName = Thread.currentThread().getName();
		for (int i = 0; i < 10; i++) {
			System.out.printf("%s - 노는중: %d%%\n", tName, i);
		}
	}
} , "Thread1");
t1.start();
	
Thread t2 = new Thread(()->{
	String tName = Thread.currentThread().getName();
	for (int i = 0; i < 10; i++)
		System.out.printf("%s - 일하는중: %d%%\n", tName, i);
},"Thread2");
t2.start();
```

---

## Thread 우선순위(Priority)

package days30;

public class Ex08 {
	public static void main(String[] args) {
		PriorityWorker[] pws = new PriorityWorker[10];
		for (int i = 0; i < pws.length; i++) {
			pws[i] = new PriorityWorker();
			pws[i].setPriority(i+1);
			pws[i].setName("t"+i);
		}
		for (int i = 0; i < pws.length; i++) {
			pws[i].start();
		}
		System.out.println("=main end=");
	}
}

우선순위가 높은 스레드부터 끝날것 같지만 멀티코어 환경에서 엄청 큰 차이는 없다.  
그냥 약간 차이나는 정도 오히려 우선순위 높은 녀석이 늦게 끝날 수 있음.  

----

## Thread - join()

join() - 다른 스레드의 작업을 기다린다. 매개변수가 없으면 끝날때까지 자신은 대기상태에서 기다림.  
join(long millis) - 다른 스레드의 작업을 기다린다. 밀리초 안에 안끝나면 기다리지 않고 진행.  
join또한 sleep처럼 대기상테에서 interrupt()메서드 호출로 인해 깨어날 수 있다.  

```java
JoinThread t1 = new JoinThread(100);
t1.start();
try {
	t1.join();
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
System.out.println("1~100 합: "+t1.getSum());
```
t1 스레드를 동작시키고 끝날때까지 main스레드를 정지시킨다.  
join메서드에 long값을 넣을시 그 시간만큼만 main스레드를 정지시킨다.  

```java
class JoinThread extends Thread
{
	private long sum = 0;
	private int n = 10;

	public JoinThread(int n) {
		super();
		this.n = n;
	}

	public long getSum() {
		return sum;
	}

	@Override
	public void run() {
		for (int i = 0; i <= n; i++) {
			sum+=i;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
```


----

## 스레드 동기화

```java
class PrintMachin
{
	public void printName(String name) //명찰출력 메서드
	{
		System.out.print("[");
		try {
			System.out.print("***");
			Thread.sleep(500);
			for (int i = 0; i < name.length(); i++) {
				System.out.print(name.charAt(i));
				Thread.sleep(500);
			}
			Thread.sleep(500);
			System.out.print("***");
			Thread.sleep(500);
			System.out.print("]");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
```
그냥 명찰 출력하는 메서드가 하나있는 클래스이다.  
```
PrintMachin pm = new PrintMachin();
pm.printName("홍길동");
```
[***홍길동***] 잘 출력된다. 물론 0.5초씩 쉬었다 한개씩 출력..  

동시에 5개의 명찰을 출력하고 싶다면 아래와 같이 코딩해야 한다.  
먼저 각 스레드로 실행할 수 있도록 Thread를 상속받는 명찰 출력용 클래스를 만들자.  
```
class PrintThread extends Thread
{
	String name;
	PrintMachin machin = null;
	@Override
	public void run() {
		this.machin.printName(name);
	}
	public PrintThread(String name) {
		this.name = name;
		this.machin = new PrintMachin();
	}
	public PrintThread() {
	}
}
```
run()메서드에서 printName(name)를 호출한다.

메인에서 호출하는 다음과 같다.
```java
PrintMachine machine = new PrintMachine();

PrintThread t1 = new PrintThread("홍길동", machine);
PrintThread t2 = new PrintThread("고길동", machine);
PrintThread t3 = new PrintThread("김길동", machine);
PrintThread t4 = new PrintThread("최길동", machine);
PrintThread t5 = new PrintThread("이길동", machine);

t1.start();
t2.start();
t3.start();
t4.start();
t5.start();
```
출력값
```
[***[***[***[***[***고이홍최김길길길길길동동동동동***************]
]
]
]
]
```

동기화가 안되어있으니 순차적으로 Runnable상태의 스레드를 실행시킨다.  
하지만 우리가 원한건 Runnable일지라도 명찰하나를 다 출력하고 다음 명찰을 출력하도록 하고 싶다.  
이럴때 동기화 작업이 필요하다, 하나의 스레드가 PrintMachine객체를 사용중이라면  
다른 스레드는 PrintMachine를 사용하지 못해야한다(잠금Lock).
잠그는 범위를 임계영역이라 한다.  

동기화 작업을 해보자.  

함수 반환형 앞에 `synchronized`
```java
public synchronized void printName(String name)
{
	System.out.print("[");
	try {
		...
		...
		...
		System.out.print("]\n");
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
```

또는 블럭단위로 `synchronized`
```java
public void printName(String name)
{	
	//이 블럭에 접근자체를 못하도록..
	synchronized (this) { 
		System.out.print("[");
		try {
			...
			...
			...
			System.out.print("]\n");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
```

또는 PrintThread의 run()메서드안에 코드를 동기화 처리.
```java
public void run() {
	synchronized (machine) {
		this.machine.printName(name);
	}
}
```
run앞에 `synchronized` 키워드를 붙이면 좋겠지만 오버라이딩 되는 메서드이기 때문에
`synchronized`을 붙이면 오버로딩


또다른 예로 공유자원 sharedData를 static 정적변수로 선언후 여러 스레드에서 접근하는 경우  

```java
class Data
{
	public int num = 0;
}

public class Main {
	static Data sharedData = new Data();
	public static void main(String[] args) throws InterruptedException {
		System.out.println("main 시작");
		Tom tom = new Tom();
		Jane jane = new Jane();
		tom.start();
		jane.start();
		System.out.println("main 종료");
	}
}
```
main 스레드에서 Tom과 Jane 스레드를 생성후 실행  
Tom과 Jane은 공유자원 sharedData에 접근해서 하나씩 증가시킨다.  
```java
class Tom extends Thread
{
	@Override
	public void run() {
		for (int i = 0; i < 100000; i++) {
			Main.sharedData.num++;
		}
		System.out.println(">Tom: " + Main.sharedData.num);
	}
}

class Jane extends Thread
{
	@Override
	public void run() {
		for (int i = 0; i < 100000; i++) {
			Main.sharedData.num++;
		}
		System.out.println(">Jane: " + Main.sharedData.num);
	}
}
```
출력값
```
main 시작
main 종료
>Tom: 88830
>Jane: 143407
```
십만번씩 for문을 반복하는데 값이 십만이하로 나오는 경우가 발생했다.  
동기화 처리가 안되어있기 때문인데 num++하는 작업이 나누어져서 cpu에서 실행되기 때문.  
num++은 기계어로 read, add, store 3개의 명령으로 이루어지는데 읽어서 증가시키기 전에
read명령 실행 후 cpu를 다른 스레드에게 뺏겨 자신이 증가시킨 num이 다른 스레드가 증가시킨 값으로 덮어씌어진다.  

```java
public void run() {
	for (int i = 0; i < 100000; i++) {
		synchronized (Main.sharedData) {
			Main.sharedData.num++;
		}
	}
	System.out.println(">Tom: " + Main.sharedData.num);
}
```
임계영역을 설정해서  sharedData의 num을 증가시킬 동안에는 다른 스레드가 접근 못하도록 막는다.  

---

## Thread 각종 메서드 - start, stop, resume, suspand

stop, resume, suspand 는 사라질 예정이다. 사용은 가능하지만
교착상태 해결이 까다롭기 때문에 사용하지 않는걸 권장한다.

```java
class InterruptThread implements Runnable
{
	boolean suspended = false;
	boolean stopped = false;
	Thread th; //Runnable구현클래스를 받기위한..

	public InterruptThread(String name) {
		th = new Thread(this, name);
	}

	@Override
	public void run() {
		String name = th.getName();
		while (!stopped) {
			if(!suspended)
			{
				System.out.println(name);
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					System.out.println(name + " - interrupted");
				}
			}
			else
				Thread.yield();
		}
		System.out.println(name + " - stopped");
	}

	public void suspend()
	{
		suspended = true;
		th.interrupt();
		System.out.println(th.getName()+ " - interrupt() by suspend()");
	}
	public void stop()
	{
		stopped = true;
		th.interrupt();
		System.out.println(th.getName()+ " - interrupt() by stop()");
	}
	public void resume()
	{
		suspended = false;
	}
	public void start()
	{
		th.start();
	}
}
```
suspend, stop, resume 모두 직접구현함.

```java
InterruptThread th1 = new InterruptThread("*");
InterruptThread th2 = new InterruptThread("**");
InterruptThread th3 = new InterruptThread("***");

th1.start();
th2.start();
th3.start();
try {
	Thread.sleep(2000);
	th1.suspend();
	Thread.sleep(2000);
	th2.suspend();
	Thread.sleep(3000);
	th1.resume();
	Thread.sleep(3000);
	th1.stop();
	th2.stop();
	Thread.sleep(2000);
	th3.stop();
}
catch(InterruptedException e){}
```

밑의 InterruptThread 클래스도 stop, resume, suspand를 직접 구현해놨다.  
while문 boolean변수로 돌려놓았는데 wait, notify를 사용하는게 더 효율적이다.  


임계영역에 못들어가서 기다리는 시간이 아깝다!  
wait와 notify를 사용하면 기다리는 시간이 필요없다.  

---

## Thead - wait, notify




```java
class Person extends Thread{
	VideoShop vShop;
	public Person(VideoShop vShop) {
		this.vShop = vShop;
	}

	public void run(){
		try{
			String v = vShop.lendVideo(); //하나 지우기
			System.out.println(this.getName() + ":" + v  + " 대여");
			System.out.println(this.getName() + ":" + v  + " 보는중");
			Thread.sleep(5000);
			System.out.println(this.getName() + ":" + v  + " 반납");
			vShop.returnVideo(v); //하나 더하기
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
```
```java
class VideoShop{
	private Vector<String> buffer = new Vector<String>();
	public VideoShop(){
		buffer.addElement("은하철도999-0");
		buffer.addElement("은하철도999-1");
		buffer.addElement("은하철도999-2");
		buffer.addElement("은하철도999-3");
	}
	public synchronized String lendVideo() throws InterruptedException{
		Thread t = Thread.currentThread();
		if(buffer.size()==0){
			System.out.println(t.getName() + ": 대기 상태 진입");
			this.wait();
			System.out.println(t.getName() + ": 대기 상태 해제");
		}
		String v = (String)this.buffer.remove(buffer.size()-1);
		return v;
	}
	public synchronized void returnVideo(String video){
		this.buffer.addElement(video);
		this.notify();
	}
}
```
lendVieo는 공유자원(vector)에서 하나씩 remove...  
returnVideo는 공유자원(vector)에 하나씩 add...  
wait와 notify는 synchronized 블록 안에서만 사용할 수 있다.  
메서드를 synchronized로 설정하고 안에서 wait, notift 호출  

notify의 경우 하나의 스레드에게만 연락한다.
연락한 스레드가 문제가 생겨 종료되었다면 뒤의 나머지 스레드들은 공유자원을 사용 못하게 될 수 도 있다.
대기중인 모든 스레드를 깨우는 notifyAll()을 사용하는것이 안정적이다.



```java
VideoShop videoShop = new VideoShop();

System.out.println("프로그램 시작");
Person p1 = new Person(videoShop);
Person p2 = new Person(videoShop);
Person p3 = new Person(videoShop);
Person p4 = new Person(videoShop);
Person p5 = new Person(videoShop);
Person p6 = new Person(videoShop);
Person p7 = new Person(videoShop);
p1.start();
p2.start();
p3.start();
p4.start(); 
p5.start();
p6.start();
p7.start();
```

---

## 데몬 스레드

다른 일반적인 스레드의 작업을 돕는 보조적인 역할을 수행하는 스레드를 뜻함.  
주스레드가 종료하면 자동으로 데몬스레드도 종료되어야 한다.  

데몬 스레드를 생성하고 시작하기 전에 `setDaemon(true)` 메서드를 호출한다.  
`isDaemon()` 메서드로 실행중인 스레드가 데몬스레드 인지 아닌지 판별 가능하다.  


```java
class AutoSaveThread extends Thread
{
	public void save()
	{
		System.out.println("작업을 자동 저장중...");
	}
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			save();
		}
	}
}
```

```java
AutoSaveThread t = new AutoSaveThread();
t.start();

System.out.println("5초후 main스레드 종료...");
Thread.sleep(5000);
System.out.println("main 종료...");
```

출력값
```
5초후 main스레드 종료...
작업을 자동 저장중...
작업을 자동 저장중...
작업을 자동 저장중...
작업을 자동 저장중...
main 종료...
작업을 자동 저장중...
```

main이 종료되도 AutoSaveThread는 계속 실행중이다...  
interrupt메서드와 예외처리로 강제종료 시켜도 되지만  
데몬스레드를 사용하면 main스레드 종료할때 같이 종료시킬 수 있다.  

```java
AutoSaveThread t = new AutoSaveThread();
t.setDaemon(true); 
t.start();

System.out.println("5초후 main스레드 종료...");
Thread.sleep(5000);
System.out.println("main 종료...");
```

setDaemon메서드 스레드의 종속관계를 정한다. t스레드는 main스레드를 돕는 데몬스레드가 되고
main스레드에 종속된다.  
main스레드가 종료됨과 동시에 데몬스레드들도 같이 종료된다.  

---

## 스레드 그룹

스레드 관리를 편하게 하기위해 서로 관련있는 스레드를 묶어 스레드 그룹을 만든다.  
JVM을 실행하면 main이 실행되고 main스레드가 만들어지고   main스레드도 System 이라는 스레드 그룹에 포함되어있다.  

그리고 main스레드 에서 다른 스레드를 만들면 main스레드 그룹이 생기고 그안에 속하게된다.  
스레드 그룹을 별도 설정하지 않을시 부모스레드 그룹에 속하게된다.
즉 모든 스레드는 자동으로 어떤 그룹에 속하게 된다.  
```java
ThreadGroup mainTG = Thread.currentThread().getThreadGroup();
System.out.println(mainTG.toString());
System.out.println(mainTG.getName());
System.out.println(mainTG.getParent().getName());
```
출력값
```
java.lang.ThreadGroup[name=main,maxpri=10]
main
system
```

### Thread의 getAllStackTraces() 메서드.

실행중 또는 대기상태, 즉 작업완료 되지 않은 모든 스레드의 호출스택을 출력할 수 있다.  
`public static Map<Thread,StackTraceElement[]> getAllStackTraces()`
반환값은 Map 컬렉션...  
```java
AutoSaveThread t = new AutoSaveThread();
t.setDaemon(true);
t.setName("autoThread");
t.start();
Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
Set<Thread> kSet = map.keySet();
Iterator<Thread> ir = kSet.iterator();
while (ir.hasNext()) {
	Thread thread = ir.next();
	System.out.printf("%s\t %s\t %b\n", thread.getThreadGroup().getName(), thread.getName(), thread.isDaemon());
}
```

출력값
```
main 	 autoThread 	 true 
system 	 Finalizer 	 true 
system 	 Attach Listener 	 true 
system 	 Reference Handler 	 true 
system 	 Signal Dispatcher 	 true 
main 	 main 	 false 
```
main과 autoThread말고도 뒤에서 실행중인 스레드들이 많다....  


### ThreadGroup의 interrupt() 메서드

스레드 그룹을 사용하면 그룹에 해당하는 모른 스레드에게 인터럽트를 발생시킬 수 있다.  

```java
class WorkThread extends Thread
{
	public WorkThread(ThreadGroup tg, String name)
	{
		//스레드 생성자에 2개 넘기는 생성자가 이미 있음
		super(tg, name);
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println(this.getName()+"인터럽트 발생....");
				break;
			}
		}
		System.out.println(this.getName()+"종료....");
	}
}
```
Thread객체의 생성자의 인자값 2개를 넘길 수 있다. 소속될 스레드 그룹, 스레드 이름  
WorkThread는 인터럽트가 발생하기 전까지 무한루프....  

스레드 그룹 MyGroup을 만들고 t1,t2,t3 생성시 myGruop에 속하도록 설정.  
```java
ThreadGroup myGroup = new ThreadGroup("MyGroup");
//myGroup 스레드 그룹에 3개의 스레드 추가
WorkThread t1 = new WorkThread(myGroup, "t1");
WorkThread t2 = new WorkThread(myGroup, "t2");
WorkThread t3 = new WorkThread(myGroup, "t3");

t1.start();
t2.start();
t3.start();
```

myGroup역시 main스레드에서 만들어 졌기때문에 main스레드 그룹에 속한다.  

```java
myGroup.interrupt();
System.out.println("main 종료");
```

출력값
```
main 종료
t3인터럽트 발생....
t3종료....
t1인터럽트 발생....
t1종료....
t2인터럽트 발생....
t2종료....
```
그룹에 interrupt 메서드를 전달함으로 그에 속한 모든 스레드를 인터럽트.  


### ThreadGroup의 list

해당 스레드 그룹에 속한 스레드 그룹, 스레드 목록을 출력하고 싶다면
ThreadGroup의 list메서드를 사용하면 된다.  

```java
System.out.println("[main] 스레드 그룹 목록 출력...");
ThreadGroup tg = Thread.currentThread().getThreadGroup();
tg.list();
```
출력값
```
java.lang.ThreadGroup[name=main,maxpri=10]
	   Thread[main,5,main]
	   java.lang.ThreadGroup[name=MyGroup,maxpri=10]
	        Thread[t1,5,MyGroup]
	        Thread[t2,5,MyGroup]
	        Thread[t3,5,MyGroup]
```