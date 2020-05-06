---
title:  "PL/SQL nested table!"
# classes: wide
header:
  overlay_image: /assets/DB/dbimage.jpg
  caption: "Photo credit: [**oracle**](https://www.oracle.com)"

read_time: false
share: false
author_profile: false

categories:
  - DataBase
tags:
  - DB
  - OracleDB

toc: true
toc_sticky: true

---


## 콜렉션 (COLLECTION)

프로시저에서 배열처럼 사용할수 있는 변수를 찾아다니다가 콜렉션이란 걸 알게되었다.  

https://sites.google.com/site/smcgbu/home/gongbu-iyagi/plsqluiguseong-yoso

그중 `nested table` 이란 콜렉션이 내가 원하는 구조였고
간단하게 쓰기 위해 남기는 글

```
TYPE 타입명 IS TABBLE OF 요소데이터타입 [NOT NULL];
```

선언은 위와같이 하면 된다.


```sql
type arr_table is table of NUMBER(11);
numberlist arr_table := arr_table();
```
선언과 초기화는 별도 과정으로 각각 해주어야 한다.  
특히 초기화 안했다고 뜨는 오류때문에 처음에 많이 당황했다.  

```sql
ORA-06531: Reference to uninitialized collection
```
컴파일은 잘 되지만 실제 프로시저를 실행하면 위와같은 오류가 발생..  
초기화는 선언과 동시에 해주자...


`nested tabled`의 특징은 크기를 지정해줄 필요가 없다는것.  
원하는 만큼 나중에 늘릴 수 있다.

보통 FOR문을 통해 동적으로 크기가 결정될 때 가 많은데 `nested table`을 사용하면 편하다. (java의 arraylist같은)

`numberlist.extend;` 이런식으로 1개 공간만큼 늘릴 수 도 있고  
`numberlist.extend(3)` 한꺼번에 늘릴 수 도 있다.

FOR문을 돌며 한개늘리고 한개 집어넣고를 반복하면 된다.  

```sql
loop
    FETCH cur_loc INTO vcode;
    EXIT WHEN cur_loc%notfound;
    vnum := vnum+1;
    namelist.extend;
    namelist(vnum) := vcode;
end loop;
```

넣었으면 그만큼 FOR문을 돌며 출력하거나 값을 조작해야 하는데 `namelist.COUNT` 를 통해 얼만큼의 공간이 할당됐는지 알 수 있음.  

1 부터 시작함으로 `namelist(1)` 이런식으로 수를 늘려가며 출력하면 된다.  


나의 경우엔 읽어온 배열안에 해당 숫자가 포함되는지 구해야 하는 문제에 맞닥뜨려서 다음과 같은 쿼리를 날려야 했다.
```sql
SELECT * FROM test
WHERE num IN (numberlist);
```
당연히 오류가 났다....  
```
ORA-22905: cannot access rows from a non-nested table item
PLS-00642: local collection types not allowed in SQL statements
```
SQL문에선 Collection type이 안된다나 뭐래나....  또 찾아서 돌아다니다 보니 해결법을 알려준 사람이 많다.

type으로 정의하고 사용하면 된다고 함  
~~지금까지 type정의는 한번도 해본적이...~~  

```sql
CREATE OR REPLACE TYPE num_arr AS OBJECT
(testNumber NUMBER(11));

CREATE OR REPLACE TYPE numlist IS TABLE OF num_arr;
```

정의를 했으면 아래처럼 선언하고 초기화해서 사용하면 된다.

```sql
testlist numlist := numlist();
...
LOOP
  testlist(testlist.last) := N;
END LOOP;
```

collection이름.last하면 마지막위치 수를 반환한다.  
위처럼 하면 또 에러난다.....  
```
“ORA-06530: Reference to uninitialized composite” error
```

진짜 선언도 하고 초기화도 다 해줬는데 왜 오류나는지 기초가 없으니 알 수 가 없다...

type으로 정의한 collection을 사용할 때에는 아무리 안에 들어가는 자료형이 NUMBER라도 위처럼 초기화하면 안된다...

`testlist(testlist.last) := num_arr(N);` 요런식으로 Element를 초기화해서 집어넣어야 오류가 나지 않는다....

사실 패키지를 사용하면 이런식으로 복잡한 Collection을 사용하지 않아도 내가 원하는 기능을 구현 가능하다 하지만.... 패키지 배우는게 더 힘들거 같아서...

### 예제) 지역정보 기반 게시글 출력 프로시저


도로명 주소 개발자 센터에서 제공하는 DB를 사용한  
반경 10km 위치의 게시글 출력 프로시저  

> <a href="http://www.juso.go.kr/addrlink/addressBuildDevNew.do?menu=geodata">http://www.juso.go.kr/addrlink/addressBuildDevNew.do?menu=geodata</a>

`nested table Collection`을 위한 type정의    
![image21](/assets/project/dbproject/image21.png){: .shadow}  

검색 기준 위치를 같는 `Collection` 객체 생성  
![image22](/assets/project/dbproject/image22.png){: .shadow}  
![image24](/assets/project/dbproject/image24.png){: .shadow}  

조회한 정보를 `Collection` 객체에 저장 및 출력  
![image25](/assets/project/dbproject/image25.png){: .shadow}  
![image26](/assets/project/dbproject/image26.png){: .shadow}  
![image27](/assets/project/dbproject/image27.png){: .shadow}  



