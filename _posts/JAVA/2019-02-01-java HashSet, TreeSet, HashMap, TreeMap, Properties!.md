---
title:  "java HashSet, TreeSet, HashMap, TreeMap, Properties!"
read_time: false
share: false
toc: true
author_profile: false

categories:
  - Java
tags:
  - Java
  - 문법
---

### HashSet

Hashset 원형
```
public class HashSet<E>
extends AbstractSet<E>
implements Set<E>, Cloneable, Serializable
```

Set의 특징은 중복허용X 순서X 이다.  
만일 중복을 제거하는 동시에 순서를 유지하고 싶다면 LinkedHashSet을 사용


HashSet에서 요소를 추가하려면 add(Object obj)를 사용하거나 addAll(Collection)을 사용한다.  
add()메서드는 중복값인지 체크하기 위해 hashCode값, equals 메서드로 저장되는 개체가 같은지 2번확인한다. 

자바의 레퍼 클래스나 자주사용하는 클래스들은 hashCode와 equals 메서드가 모두 재정의 되어있다.  

Set 컬렉션 클래스에 직접 정의한 클래스를 요소 집어넣을때 hashCode와 equals메서드 재정의를 해야 논리적으로 같은 값을 구별 가능하다.

아래에 직접 Member라는 클래스를 정의하였다.  
이름과 학번이 같다면 논리적으로 같은 객체이기 두개의 논리적으로 같은 객체가  HashSet에 들어가지 못하도록 hashCode와 equals메서드를 재정의 하였다.

```
class Member
{
	int sno;  	//학번
	String name;	//이름
	int age;	//나이
	public Member(int sno, String name, int age) {
		super();
		this.sno = sno;
		this.name = name;
		this.age = age;
	}
	@Override
	public int hashCode() {
		return (name.hashCode() + sno);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Member) {
			Member m = (Member)obj;
			return 	this.sno==m.sno && 
				this.name.equals(m.name) && 
				this.age==m.age;
		}

		else {
			return false;
		}
	}
}
```

```
HashSet<Member> hs = new HashSet<>();
Member m1 = new Member(1, "Ko", 20);
System.out.println(hs.add(m1)); //true 출력

Member m2 = m1; //같은 인스턴스 참조
System.out.println(m1.hashCode()); //2018699554
System.out.println(m2.hashCode()); //2018699554
System.out.println(hs.add(m2)); //false 출력
```
m1과 m2가 같은 인스턴스를 가리키고 있기 때문에 당연히 같은 hashCode값이 나온다.

```
Member m3 = new Member(1, "Ko", 20); 
//인스턴스는 다르지만 논리적으론 m1과 같은 객체
System.out.println(m1.hashCode()); 
System.out.println(m3.hashCode());
System.out.println(hs.add(m3));

```
출력값
```
2437
2437
false
```
m1과 m3 인스턴스는 각각의 공간에 할당되어 있지만 논리적으론 같은 객체이다. (이름, 학번, 나이 모두같음)
논리적으로 같기에 Member에서 오버라이딩한 hashCode()메서드에 의해 같은 값이 출력됨. HashSet에도 들어가지 않는다.


name문자열의 hashCode()가 2436이 나오는 이유는 String클래스에서 hashCode를 오버라이딩 했기 때문.
String클래스 내부적으로 다음과 같은 식을 구성하고있음.
```
s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
```

-----

### HashSet & LinkedHashSet

LinkedHashSet 원형
```
public class LinkedHashSet<E>
extends HashSet<E>
implements Set<E>, Cloneable, Serializable
```
LinkedHashSet은 HashSet을 상속하는 클래스

LinkedHashSet의 생성자는 다음과 같다.
```
public LinkedHashSet() {
        super(16, .75f, true);
    }
```
HashSet의 인자 3개짜리 생성자를 호출하는데 사실 이 생성자에서
LinkedHashMap 컬렉션 클래스를 생성해서 반환한다....

HashSet의 인자 3개짜리 생성자.
```
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
  map = new LinkedHashMap<E,Object>(initialCapacity, loadFactor);
}
```
즉 내부적으론 
LinkedHashSet   =>   HashSet   =>   LinkedHashMap
이렇게 되어있단.... 알고보니 Map계열이였....

순서를 유지하기 위해 key를 사용하는듯 하다.

어쨌건 우리는 LinkedHashSet 특징인 중복X 순서O 인것만 알면 된다.  
입력한 순서대로 출력을 원한다면 LinkedHashSet을 사용토록....한다.

물론 순서가 유지 된다고 indexOf()메서드를 사용할 수 있는건 아니다.  

----

### TreeSet

이진 검색트리 자료구조 형태로 데이터 저장한다.  
트리 구조이기 때문에 정렬, 검색, 범위검색에 탁월한 효율을 자랑, 어떤 데이터를 찾던지 트리 높이만큼만 찾으면 된다.  
따라서 수정 삭제할 용도로 쓰면 좋지 않다, 다시 재정렬하는데는 트리 구조를 유지시키려면 시간 오래걸리기 때문.  
한번 데이터들을 넣어넣고 지속적으로 유지하며 사용할때 TreeSet을 사용하면 좋다.  

Set이기 때문에 중복X 순서유지X이다. 애초에 정렬이니까 집어넣은 순서 기억할 필요가...

노드로 구성되어있으면 노드는 오른쪽 서브트리와 왼쪽 서브트리를 가리키는 참조변수와 요소 저장용 참조변수가 있다.

```
TreeSet<Integer> ts = new TreeSet<>();
for (int i = 0; i < 7; i++) {
	int n = (int) (Math.random()*45) +1;
	ts.add(n);
}
System.out.println(ts);
```
출력값
```
[1, 4, 11, 19, 21, 26, 32]
```


```
System.out.println(ts.first()); //get min
System.out.println(ts.last()); //get max
```
출력값
```
1
32
```

```
System.out.println(ts.pollFirst()); //제일 작은값 삭제 하면서 값 반환
System.out.println(ts.pollLast()); //제일 큰값 삭제 하면서 값 반환
System.out.println(ts); 
```

출력값
```
1
32
[4, 11, 19, 21, 26]
```

tree는 index(순서)가 없기때문에 List처럼 `remove(int index)`, `remove(Obejct obj)` 2개가 아니라 `remove(Obejct obj)` 한개만 정의되어 있다. 
```
ts.remove(100); 
```

100 을 넣어도 Boxing되서 Integer(100)을 뜻하고 100인 숫자를 삭제한다.  
실패하면 false반환


```
System.out.println(ts.ceiling(num)); //지정된 값중 가장 가까운 큰값 반환, 없으면 null
System.out.println(ts.floor(num)); //지정된 값중 제일 가까운 작은값 반환, 없으면 null

System.out.println(ts.higher(num)); 
System.out.println(ts.lower(num)); //같은 기능을 하는 함수 2개
```


#### TreeSet의 정렬 기준

정렬하면서 집어넣기 때문에 정렬기준이 정해진 클래스가 아닌 객체를 집어넣을 때에는 정렬 기준을 정해주어야 한다.(Collections.sort할때처럼)  
맨처음 TreeSet 생성자에 Comparator를 구현하는 객체를 넣어주던가   
TreeSet add매서드 호출할때 Comparable을 구현하는 객체를 넣어주어야 한다.

```
TreeSet<Member> set = new TreeSet<>(); //comparator구현 X
set.add(new Member(1, "Ko", 27)); //뭐를 기준으로 배치할지 지정해줘야한다.
```

Member의 sno(학번)을 기준으로 정렬.
```
class Member implements Comparable<Member>
{
	int sno;
	String name;
	int age;
	
	....
	
	@Override
	public int compareTo(Member arg0) {
		return Integer.compare(this.sno, arg0.sno);
	}
```
이번엔 Comparator로 정렬!  
Member의 정렬 기준을 구현하는 Comparator객체를 익명클래스로 집어넣기.
```
public static void main(String[] args) {
		TreeSet<Member> set = new TreeSet<>(new Comparator<Member>() {
			@Override
			public int compare(Member o1, Member o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		set.add(new Member(1, "Ko", 27));
		set.add(new Member(2, "Do", 27));
		set.add(new Member(3, "Go", 27));
		set.add(new Member(4, "Ho", 27));
		Iterator<Member> it = set.iterator();
		while (it.hasNext()) {
			Member member = it.next();
			System.out.println(member);
		}
	}
```
출력값
```
Member [sno=2, name=Do, age=27]
Member [sno=3, name=Go, age=27]
Member [sno=4, name=Ho, age=27]
Member [sno=1, name=Ko, age=27]
```
이름순으로 정렬되서 출력된다. Comparator 승!

#### TreeSet의 범위탐색

TreeSet의 `tailSet`, `headSet`, `subSet`을 쓰면 된다.... 생략
```
TreeSet<String> set = new TreeSet<>();

String[] data = {"abc","alien","bat","car","Car","disc","dna","dance",
		"dZZZ","dzzz","elephant","elevator","fan","flower"};
for (int i = 0; i < data.length; i++) {
	set.add(data[i]);
}
System.out.println(set);
System.out.println(set.subSet("b", "d" + "zzzz"));
//예상값과 다르다면 String의 compare 메서드를 탓하자...
```
출력값
```
[Car, abc, alien, bat, car, dZZZ, dance, disc, dna, dzzz, elephant, elevator, fan, flower]
[bat, car, dZZZ, dance, disc, dna, dzzz]
```
대문자가 순서상 앞서기 때문에 앞에위치....  
만약 사전순으로 바꾸고 싶다면 Comparator를 상속한 String 정렬용 클래스를 만들면 된다.

---

### HashMap


Map인터페이스를 구현한 클래스.  
Ket와 Value를 한쌍(entry)로 저장 관리하는 컬렉션이다.  

HashMap과 HashTable이 있는데 ArrayList와 Vector와 같은 관계  
HashMap은 동기화x  
HashTable은 동기화O  

HashMap이 나중에 나왔고 동기화 필요가 없다면 HashMap을 쓰는걸 권장.  
객체의 key값은 중복되면 안되기때문에 hashCode(), equals() 사용해서 검사한다, value는 중복되도 상관없다.

HashMap원형
```
HashMap<K, V>
public class HashMap<K,V>
extends AbstractMap<K,V>
implements Map<K,V>, Cloneable, Serializable
```

HashMap안에 Entry라는 중첩클래스 정의가 되어있다.
```
static class Entry implement Map.Entry{
	final Object key; 
	final Object value; 
}
```

`Entry<K,V> [] table;`  
내부에선  Entry를 배열로 구성하고있다. 

학번을 key로 이름을 value로
```
HashMap<Integer, String> hm = new HashMap<Integer, String>();

hm.put(1, "Ko");
hm.put(3, "Ho");
hm.put(4, "Ho");
hm.put(2, "Go");
System.out.println(hm);
```

출력값
```
{1=Ko, 2=Go, 3=Ho, 4=Ho}
```
HashMap은 Comparable을 구현하는 Object가 key로 들어가면 key에 의해 자동 정렬된다.  
구현하지 않을경우 정렬되지 않음.  

```
hm.put(1, "Xo"); 	//같은 key가 put될경우 덮어씌워짐
hm.put(null, null);
hm.put(null, "NULL"); 	//null도 키값으로 인식
hm.put(5, null); 	//null도 value으로 인식
System.out.println(hm);
```
출력값
```
{null=No, 1=Xo, 2=Go, 3=Ho, 4=Ho, 5=null}
```

HashMap의 `containsKey(key)` - key에 해당하는 엔트리가 있는지 검사, boolean값 반환  
HashMap의 `containsValue(value)` - value에 해당하는 엔트리가 있는기 검사, boolean값 반환  
HashMap의 `get(key)` - key해당되는 엔트리의 value값 반환, key가 없는경우 null 반환
출력값  
```
System.out.println(hm.containsKey(5));
System.out.println(hm.containsValue("Ho"));
String value = hm.get(1); 
System.out.println(value);
```
출력값
```
true
true
Xo
```

#### 모든value출력
HashMap의 `values()`메서드를 통해 Collection객체로 생성된 Obejct배열을 전달받음.
```
Collection<String> vc = hm.values(); 
Iterator<String> it = vc.iterator();
while (it.hasNext()) {
	String string = it.next();
	System.out.println(string);
}
```


#### 모든키값출력
HashMap의 keySet()메서드를 통해 Collection-Set 반환.  
Set으로 반환하는 이유는 어차피 key는 중복되지 않으니까.
```
Set<Integer> kc = hm.keySet();
Iterator<Integer> ic = kc.iterator();
while (ic.hasNext()) {
	Integer integer = (Integer) ic.next();
	System.out.println(integer);
}
```

출력값
```
1
2
3
4
5
```

#### 모든 엔트리 출력 (key - value)

HashMap의 `entrySet()`메서드를 통해 Set 반환.  
Set의 요소로 HashMap의 Entry가 들어가있다.
```
System.out.println("> 모든 엔트리(key - value) 출력 < ");
Set<Entry<Integer, String>> eset = hm.entrySet();
Iterator<Entry<Integer, String>> eir = eset.iterator();
while (eir.hasNext()) {
	Entry<Integer, String> entry =  eir.next();
	System.out.printf("%d - %s\n", entry.getKey(), entry.getValue());
}
```
Entry클래스의 `getKey()`와 `getValue()` 메서드를 통해 key와 value값을 가져올 수 있음.

--------------------------------------------------

### TreeMap

Map계열이기 때문에 Entry를 가지고 있다.  
Tree이름처럼 이진 트리구조로 생성되어 있으며 검색, 정렬 하는데 용이한 컬렉션 클랙스이다.  

하지만 검색의 대부분 경우 HashMap이 TreeMap보다 뛰어남. Tree구조를 유지하기 위해 발생하는 오버헤드가 크기때문.....  
일정 이상 데이터집합의 범위 검색이나 정렬이 필요한 경우 TreeMap을 사용하는것을 권장한다.  

```
String [] data = { "A","K","A","K","D","K","A","K","K","K","Z","D" };

TreeMap<String, Integer> map = new TreeMap<>();
for (int i = 0; i < data.length; i++) {
	map.put(data[i], map.get(data[i])==null? 1 : map.get(data[i])+1); 
}
System.out.println(map);
```
Key로 String을 Value로 Integer를 사용.  
TreeMap의 get(key)으로 얻어온 value에 +1해서 재저장.  
```
Iterator<Entry<String, Integer>> ir = map.entrySet().iterator();
while (ir.hasNext()) {
	Entry<String, Integer> entry = (Entry<String, Integer>) ir.next();
	System.out.println(entry.getKey() + ": " + printBar('#', entry.getValue()) + entry.getValue());
}
```
출력값
```
A: ###3
D: ##2
K: ######6
Z: #1
```
printBar 함수는 그냥 value만큼 문자 출력.  
Map계열은 일반적으로 key를 기준으로 자동 정렬된다(Comparable 구현일때만)  
String을 기준으로 정렬되기 때문에 A~Z순으로 정렬되어서 출력  

만약 Value를 기준으로 정렬하고 싶다면 해당 TreeMap의 요소(Entry<String, Integer>)에 정렬기준을 추가하면 된다.
```
Set<Entry<String, Integer>> set = map.entrySet();
ArrayList<Entry<String, Integer>> list = new ArrayList<>(set);
Collections.sort(list, new Comparator<Entry<String, Integer>>() //익명클래스 사용
{
	@Override
	public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1) {
		return arg0.getValue().compareTo(arg1.getValue()) * -1; //descending
	}
});
```

출력값
```
K: ######6
A: ###3
D: ##2
Z: #1
```

람다식 버전
```
Collections.sort(list,  (o1, o2)->o1.getValue().compareTo(o2.getValue())*-1);
ir = list.iterator();
while (ir.hasNext()) {
	Entry<String, Integer> entry = (Entry<String, Integer>) ir.next();
	System.out.println(entry.getKey() + ": " + printBar('#', entry.getValue()) + entry.getValue());
}
```

----

### Properties

Properties는 HashMap과 형제인 HashTable클래스의 하위클래스이다.  
Map계열이기 때문에 entry를 가지고 있다.  
특징은 Entry의 key와 value 속성이 정해져 있다, 둘다 String으로!  
애플리케이션의 환경설정과 관련된 속성을 관리하는데 사용되며, 
데이터를 파일로부터 읽고load  쓰는store 편리한 기능을 제공한다.

```
Properties prop = new Properties();
String fileName =".\\src\\days24\\database.properties";
prop.load(new FileReader(fileName));
```
Properties의 load()메서드를 통해 자동으로 파일 내용을 Entry화 해서 저장함
'=' 를 기준으로 key와 value를 나눔.

```
Iterator ir = prop.entrySet().iterator();
while (ir.hasNext()) {
	Map.Entry entry = (Entry) ir.next();
	System.out.printf("%s - %s\n", (String)entry.getKey(), (String)entry.getValue());
}
```
출력값
```
password - tiger
url - jdbc:oracl:thin:@localhost:1521:orcl
driver - oracle.jdbc.OracleDriver
username - scott
```

FileOutputStream객체를 사용해서 해당 설정 정보를 원하는 형식(XML 등)으로 저장가능하다.
```
String fileName2 = ".\\src\\days24\\database_save.properties";
prop.store(new FileWriter(fileName2), "DB 연결정보");
prop.storeToXML(new FileOutputStream(fileName2), "DB XML 연결정보");
```