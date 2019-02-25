---
layout: post
title:  "java Iterator, Enumeration, ListIterator, Comparable, Comparator!"
date: 2019-01-28
author: kouzie
categories: Java 
tags: java Iterator Enumeration ListIterator Comparable Comparator
cover:  "/assets/instacode.png"
published: true

---

### Iterator (반복자)

2중 배열처럼 Iterator를 사용해서 ArrayList안에 ArrayList출력하기
```
int totalStdNum = 0;
Iterator<ArrayList<Student>> gir = gradList.iterator();
int classNum = 1;
while (gir.hasNext()) {
	System.out.println(classNum++ + "반");
	ArrayList<Student> arrayList = gir.next();
	Iterator<Student> cir = arrayList.iterator();
	while (cir.hasNext()) {
		Student student = cir.next();
		System.out.println(student);
	}
	totalStdNum += arrayList.size();
	System.out.println();
}		
System.out.println("총학생수:" + totalStdNum);
```

Iterator를 쓰면 다중 쓰레드환경에서 충돌 발생할때 예외를 발생하기 때문에
나중에 동기화처리하기 편하다.

---------------------

### Enumeration (열거자)

```
Vector<BoardDTO> list = new Vector<>();
list.addElement(new BoardDTO("KO", "조퇴", "몸이아파서"));
list.addElement(new BoardDTO("JO", "지각예정", "실업급여"));
list.addElement(new BoardDTO("MO", "지각", "차가막혀서"));
System.out.println(list);
Enumeration<BoardDTO> en = list.elements();
while (en.hasMoreElements()) { //하나씩 꺼내오는건 Iterator랑 똑같다.
	BoardDTO bto = en.nextElement();
	System.out.println(bto);
}
```

열거자(Enumeration)와 반복자(Iterator)의 차이는 동기화처리가 되냐 안되냐 차이다.  
 
열거자는 3개의 요소를 출력할때 3개 요소 모두 임시 데이터 공간에 저장해놓고
하나씩 꺼내 출력한다.  
따라서 실시간의 데이터가 변해도 열거자는 복사해놓은 데이터를 관리하기 때문에 변한지 모른다!  

에로 A라는 스레드가 1번째 요소를 출력하고 2번째 요소를 출력하려 한다.  
이때 B라는 스레드가 2번째 게시글을 삭제했을때
A는 문제없이 출력 가능하다. 왜냐면 열거자 생성시 모든 값을 복사해서 사용하기 때문.  

반면 반복자는 실시간으로 데이터에 접근하기때문에 변화가 일어나면 예외가 발생한다.

열거자가 먼저나오고 반복자가 후에 나왔다.

----

### ListIterator 반복자

Iterator 반복자보다 향상된 버전으로 뒤로가는것이 가능하다.

ListIterator 원형
```
public interface ListIterator<E>
extends Iterator<E>
```

Iterator는 단방향으로만 이동했지만 ListIterator는 **양방향**으로 이동가능하다.  
ListIterator는 `hasNext()`뿐 아니라 `hasPrevious()` 도 있다.  
`previous()` 메서드로 객체를 얻어와 관리한다.  

```
ArrayList<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");
list.add("D");
list.add("E");
list.add("F");
System.out.println(list.size());

ListIterator<String> lir = list.listIterator();
while (lir.hasNext()) {
	String s = lir.next();
	System.out.println(s);
} //ABCDEF출력

while (lir.hasPrevious()) {
	String s = lir.previous();
	System.out.println(s);
} //FEDCBA출력
```

출력값
```
6
ABCDEF
FEDCBA
```

----

### LinkedList

List계열이기 때문에 순서가 있고 중복허용한다.

ArrayList와의 다른점은 배열을 사용 안하고 불연속적으로 존재하는 데이터를 연결한 형태.
	
LinkedList는 안에 배열을 만들어서 관리하는게 아니기때문에 new 연산을통해 heap에 인스턴스를 만들어도
일단 null값이 들어간 인스턴스가 생성되고 list가 이를 가리키고있다.  
자바에는 포인터 변수가 없기에 참조변수로 이를 대체한다. LinkedList 안의 멤버는 다음과 같다.  
```
transient int size = 0;
transient Node<E> first;
transient Node<E> last;
```
transient는 동기화 처리를 위한 키워드같음.  
자바에서 사용하는 LinkedList는 "더블리 링크드리스트(Doubly LinkedList)"를 기반으로 하고 있습니다.  

Vector나 ArrayList는 내부적으로는 배열을 만들고 이를 관리하기때문에
삭제, 추가 등의 작업에서 새 배열을 만들 필요가 있었지만
LinkedList는 삭제, 추가 작업에서 그저 연결작업을 끊고 삭제나 추가한후 연결해주면 된다.  
	
단 무조건 처음부터 접근해야 하기때문에 수정이나 특정인덱스 조회는 좀 느릴 수 있다.

-------

### Stack

last in last out 특성을 가진 자료구조

컬렉션 상속 구조 `Collection - List - Vector - Stack`

Vector를 상속하는 클래스이기 때문에 List의 메서드를 모두 구현중이고 내부는 배열형태로 되어있다.

```
Stack<String> lifo = new Stack<>(); //생성자는 하나뿐
lifo.push("test");
lifo.push("second");
System.out.println(lifo);
```
출력값
```
[test, second]
```

Vector를 상속하기때문에 add나 remove같은 메서드를 사용가능하지만
stack의 특성상 push, pop, peek 3가지 메서드를 쓰는것이 좋다   (LIFO구조를 항상 지키자).  
add remove쓸꺼면그냥 ArrayList쓰는것이 옳다....

empty, search 메서드도 있고 찾으면 0부터가 아닌 1부터 반환, 못찾으면 -1를 반환  
search말고 indexOf 써도 상관없다.  
	
---

### Queue

First in First Out 자료구조
		
Queue는 클래스가 아닌 인터페이스이다.

Queue원형
```
public interface Queue<E>
extends Collection<E>
```

그리고 LinkedList가 Queue를 구현중이다.
LinkedList원형
```
public class LinkedList<E>
extends AbstractSequentialList<E>
implements List<E>, Deque<E>, Cloneable, Serializable
```

보면 Deque라는 인터페이스를 구현하고 있는데 Deque가 Queue의 하위 인터페이스이다.  
따라서 Queue의 구현클래스인 LinkedList를 인스턴스화시켜 참조하는건 문제가 안된다.  

Queue주요 메소드
```
offer() 
poll()
peek()
```

Queue또한 Collection인터페이스를 상속하기 때문에 add, remove 사용 가능하지만
Queue구조상 offer와 poll을 사용하는걸 권장함.

```
Queue<String> queue = new LinkedList<>();

queue.offer("KO");
queue.offer("JO");
queue.offer("MO");

System.out.println(queue.size());
while (!queue.isEmpty()) {
	System.out.println(queue.poll());
}
System.out.println(queue.size());
```

출력값
```
3
KO
JO
MO
0
```

-------

### Comparable인터페이스 & Comparator인터페이스

Collection 제너릭 클래스 정렬방법은 2가지이다.

Class자체에서 Comparable인터페이스를 구현하던가,
정렬을 위한 Comparator인터페이스를 구현한 별도의 정렬 클래스를 만들어 주던가.


#### Comparable인터페이스

Collections 클래스의 sort메서드의 원형
```
public static <T> void sort(List<T> list, Comparator<? super T> c)
```

매개변수로 정렬할 List객체와 정렬을 위한 Comparator객체 2개를 필요로 한다.
하지만 Integer를 제너릭으로 갖고 있을경우 Comparator객체는 넘기지 않아도 오류가 뜨지 않는다.
```
ArrayList<Integer> list = new ArrayList<>();
list.add(0); list.add(2);
list.add(5); list.add(3);
Collections.sort(list);  //그냥 정렬된다! 오름차순으로
System.out.println(list);
```
출력값
```
[0, 2, 3, 5]
```
이유는 Integer클래스 같은 래퍼클래스들은 Comparable인터페이스를 구현중이기 때문이다.

Integer원형
```
public final class Integer
extends Number
implements Comparable<Integer>
```
Integer가 Comparable의 compateTo(Object o)메서드를 오버라이딩 중이기 때문에 Integer를 요소로 갖는 컬렉션은
Comparator없이 단독으로 sorting가능하다.

앞으로 우리가 만들 클래스를 요소로 같는 Collection클래스를 정렬할 때도 마찬가지.
Comparable을 implement하거나 Comparator를 implement하는 클래스를 별도로 만들어 주거나!

```
class Student implements Comparable<Student>
{
	private int no;
	private String name;
	private int kor, eng, mat;
	private int tot;
	private double avg;
	private int rank;
	
	@Override
	public int compareTo(Student o) {
		return this.name.compareTo(o.name);
	}
}
```
우리가 만든 Student클래스에 Comparable를 구현하고 Collections의 sort메서드로 바로 정렬 가능하다.


#### Comparator인터페이스

Comparator를 구현해서 정렬하면 정렬기준을 원하는대로 바꿀 수 있다.
단순히 오름차순, 내림차순이 아닌 이름과 나이를 합쳐 비교한다던지...등

```
class StudentComparator implements Comparator<Student>
{
	private StudentSortOption sortOption;
	public StudentComparator(StudentSortOption name) {
		super();
		sortOption = name;
	}
	@Override
	public int compare(Student o1, Student o2) {
		int result = 0;
		switch (this.sortOption) {
		case NAME:
			result = o1.getName().compareTo(o2.getName());
			break;
		case SCORE:
			result = Integer.compare(o1.getTot(), o2.getTot());
			break;
		case NO:
			result = Integer.compare(o1.getNo(), o2.getNo());
			break;
			//the value 0 if x == y;a value less than 0 if x < y; anda value greater than 0 if x > y
		}
		return result;
	}
}
```
별도의 클래스를 위처럼 만들고
```
list.sort(new StudentComparator(StudentSortOption.SCORE));
```
객체 생성해서 ArrayList의 sort메서드에 전달,
위처럼 클래스를 따로 만들기 싫어서 익명클래스를 써도 좋다.

요약하면
Comparable을 implement해서 comparTo메서드를 구현해서 정렬하던가.  
Comparator를 implement하는 클래스를 만들고 그 클래스 안에서 compare메서드를 구현해서 정렬하던가.  

참고: https://cwondev.tistory.com/15

-----

### 람다식 맛보기

사실 간단한 정렬의 경우 람다식만 알면 Comparable이고 Comparator고 다 필요없다.

```
list.sort((s1, s2)->s1.getName().compareTo(s2.getName()));
```

정렬말고 랜덤하게 값을 집어넣는 때에도 사용 가능
```
Arrays.setAll(m, (n)->(int)(Math.random()*100)+1);
```

나중에 잘 배워두면 복잡한 코드를 간단하게 구현 가능 할수 있을듯.

----

