---
title:  "java time패키지, LocalDate, LocalTime, Pathcer, Matcher!"
read_time: false
share: false
toc: true
toc_sticky: true
author_profile: false

# classes: wide
categories:
  - java
---

# java.time 클래스

jdk 1.8부터 추가된 java.time패키지, 시간을 다루는 클래스들이 포함되어 있다.

date와 calendar의 단점을 해결하고 시간 관련 연산을 더욱 편하게 하기 위해 만들어짐.

기존 프로그램의 호환성때문에 Date와 Calendar는 계속 사용될 예정
따라서 java.time패키지의 클래스와 Date, Calendar간의 변환방법을 알아둬야함
그냥 날짜 담는용도로는 Date쓰는것도 나쁘지 않음

java.time  - 날짜, 시간 다루는 핵심 클래스 대거 포함되어 있다.

java.time의 하위 패키지
1. java.time.chrono - 표준이 아닌 달력 시스템을 위한 클래스 제공.  
2. java.time.format - 날짜 시간을 파싱해서 원하는 포멧으로 변환 출력.  
3. java.time.temporal - 날짜 시간 필드와 단위를 위한 클래스 제공  
4. java.time.zone - 시간대(timezone)과 관련된 클래스 제공  


java.time 클래스들은 String클래스 처럼 불변하다.
```
Calendar cal = Calendar.getInstance();
cal.set(field, value); //cal객체의 정보가 바뀜
```

```
LocalDate now = LocalDate.now();
now = now.plusDays(1); //객체가 바뀌지 않아 다시 대입
System.out.println(now); //2019-02-01
```

Calendar는 기존 인스턴스의 정보를 바꾸지만 java.time패키지의 객체들은 기존의 인스턴스를 버리고 새 인스턴스를 참조한다.  
`now.plusDays(1)` 하루 증가후 꼭 반환하는 값을 재참조 해야한다.

---

## LocalDate, LocalTime, LocalDateTime 클래스

java.time에서는 **날짜**를 다루는 클래스와 **시간**을 다루는 클래스들 분리해두었다.  
날짜 - LocalDate  
시간 - LocalTime  

날짜와 시간 같이 다루어아 한다면 LocalDateTime 객체를 사용하면 된다.

### LocalDate

LocalDate 원형
```
public final class LocalDate
extends Object
implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable
```

LocalDate 생성과 출력
```java
LocalDate now = LocalDate.now();
System.out.println(now.getYear());
System.out.println(now.getMonthValue());
System.out.println(now.getDayOfMonth());
```
출력값
```
2019
1
31
```

LocalDate역시 Calendar클래스처럼 new연산으로 객체생성이 불가능하고 now()메서드를 사용해서 인스턴스를 생성한다.   
now() 메서드는 현재시간이 담긴 객체를 반환한다.  



### LocalDate - get...메서드

LocalDate객체의 정보를 얻기위해 많은 get 메서드가 정의되어 있다.  
enum클래스인 java.time.temporal.ChronoField를 같이 사용해서 정보를 가져올 수 있다.  
```java
System.out.println(now.get(ChronoField.YEAR)); //2019
System.out.println(now.get(ChronoField.MONTH_OF_YEAR)); //1
System.out.println(now.get(ChronoField.DAY_OF_MONTH)); //31
System.out.println(now.get(ChronoField.DAY_OF_WEEK)); //4 (목)  0(일)
```

year, month, day는 가져오는일이 너무 많아 별도의 함수를 만들어 놓았다.  
getYear, getMonthValue, getDayOfMonth
```
System.out.println(now.getYear());
System.out.println(now.getMonthValue());
System.out.println(now.getDayOfMonth());	
```
<br>
<br>

### LocalDate - 날짜 설정 메서드들

1. LocalDate - plusDays(), minusDays() 메서드  

현재 날짜에서 날짜를 더하거나 빼거나
```
now = now.plusDays(1);
System.out.println(now); //2019-02-01
now = now.plusDays(-1);
System.out.println(now); //2019-01-31
now = now.minusDays(1);
System.out.println(now); //2019-01-30
```
```
now.plusMonths(long time);
now.plusWeeks(long time);
now.plusYears(long time);
```
비슷한 기능을 하는 plus... minus... 메서드  
<br>

2. LocalDate - of(int year, int month, int dayOfMonth) 메서드  

LocalDate의 static메서드인 of메서드를 사용해서 LocalDate객체 생성.
```
LocalDate d = LocalDate.of(2015, 12, 31);
System.out.println(d); //2015-12-31
```  
<br>

3. LocalDate - ofYearDay(int year, int daysOfYear) 메서드
```
LocalDate date = LocalDate.ofYearDay(2010, 37);
System.out.println(date); //2010-02-06 출력
```
2010년도에서 37일이 지난 날짜 객체를 반환한다.  
단 설정한 년도를 넘어가면 DateTimeException 발생한다.(366 이상 더한경우)  
<br>

4. LocalDate - with.... 메서드

LocalDate의 날짜를 바꾸고 싶으면 plus, minus 메서드를 사용해서 날짜를 변경하는것도 가능하지만 with메서드를 사용해서 해당 날짜로 변경 가능하다.
```
now = now.withMonth(9);
now = now.withDayOfMonth(11);
System.out.println(now); //2019-09-11
```
with 메서드와 ChronoField enum클래스를 사용해서 설정도 가능하다.
```
now = now.with(ChronoField.MONTH_OF_YEAR, 9); 
now = now.with(ChronoField.DAY_OF_MONTH, 11);
System.out.println(now); //2019-09-11
```
<br><br>

### LocalDate - parse(), format() 메서드
문자열을 LocalDate로 변환   
LocalDate를 문자열로 변환  

SimpleDateFormat을 사용해 Date객체를 원하는 문자열로 출력하고, 문자열을 Date객체로 만들기도 했다.   
문자열로 Date객체를 만들었는데 LocalDate에는 parse메서드와 format메서드가 내장되어있다.  

먼저 LocalDate의 parse  static 메서드
```
String s = "2018-09-18";
LocalDate d = LocalDate.parse(s);
System.out.println(d);
```
출력값
```
2018-09-18
```
parse 메서드의 기본 문자열 패턴은 yyyy-MM-dd 이다. (4-2-2 글자수 유지 필요)  
문자열 패턴은 parse메서드와 DateTimeFormatter의 ofPattern() 메서드로 바꿀수 있다.  
```
String s = "2018년 09월 19일";
String pattern = "yyyy년 MM월 dd일";
LocalDate d = LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
System.out.println(d);
```
출력값
```
2018-09-19
```

그럼 format메서드로 LocalDate를 내가 원하는 문자열 형식으로 출력도 가능하다.  
```
LocalDate d = LocalDate.of(2018, 11, 11);
String pattern = "yyyy년 MM월 dd일";
String date_str = d.format(s, DateTimeFormatter.ofPattern(pattern));
System.out.println(date_str);
```

출력값
```
2018년 11월 11일
```
<br><br>

### LocalDate - lengthOfMonth()메서드

Calendar에서 마지막 날짜를 구하려면 getActualMaximum()메서드를 사용했다.  
LocalDate에도 마지막 날짜 구하는 메서드가 있다! lengthOfMonth()!
```
LocalDate now = LocalDate.now();
System.out.println(now); //2019-02-04 출력
int endDay = now.lengthOfMonth(); //마지막날짜 반환
System.out.println(endDay);
```
출력값
```
2019-02-04
28
```
자매품 lengthOfYear 메서드 - 해당 년도의 일 수 를 반환  
<br><br>

### LocalDate - compareTo, isAfter, isBefore, isEqual

친절하게도 비교메서드를 여러개 만들어 주었다.
기능은 타 클래스 비교 메서드들과 비슷하다. 

```
LocalDate now = LocalDate.now();
LocalDate dday = LocalDate.of(2019, 2, 4);
System.out.println(now); //2019-02-04
System.out.println(now.compareTo(dday)); //같으면 0
now = now.minusDays(1);
System.out.println(now.compareTo(dday)); //작으면 -1
now = now.plusMonths(1);
System.out.println(now.compareTo(dday)); //크면 1
```
```
LocalDate dday = LocalDate.of(2019, 2, 4);
System.out.println(now); //2019-02-04
System.out.println(now.isAfter(dday)); //false
dday = dday.minusDays(1);
System.out.println(now.isAfter(dday)); //true
```
isEqual 메서드가 있기때문에 isAfter와 isBefore는 당연히 해당 날짜를 포함하면 false반환한다.
<br><br>


### Year, YearMonth, MonthDay 클래스
날짜를 더 세분화해서 관리.... 알고만 있자.

---

### LocalTime

LocalTime 원형
```
public final class LocalTime
extends Object
implements Temporal, TemporalAdjuster, Comparable<LocalTime>, Serializable
```

LocalDate가 날짜 전용 클래스라면 LocalTime은 시간 전용 클래스  
둘이 비슷한 역할을 하는 클래스이기 때문에 비슷한 메서드가 많다.  
plus, get, with, of, parse, 비교메서드 등등...  
<br><br>

### LocalTime - now 메서드

```
LocalTime now = LocalTime.now();
System.out.println(now);
```
출력값
```
16:27:34.704
```
시:분:초.밀리초 형식으로 출력된다.  
<br><br>

### LocalTime - of 메서드
```
LocalTime now = LocalTime.now();
System.out.println(now);
now = LocalTime.of(14, 52);
System.out.println(now);
now = LocalTime.of(14, 52, 10, 999999999);
System.out.println(now);
```
출력값
```
16:30:48.239
14:52
14:52:10.999999999
```
<br><br>


### LocalTime - get 메서드

```
int time = now.getHour();
int min = now.getMinute();
int second = now.getSecond();
int mils = now.getNano();
System.out.printf("%d시 %d분 %d.%d초", time, min, second, mils);
```
출력값
```
14시 52분 10.999999999초
```


LocalTime의 get메서드로 ChronoField 열거형 클래스와 같이 사용가능하다.
```
int time = now.get(ChronoField.HOUR_OF_DAY);
int min = now.get(ChronoField.MINUTE_OF_HOUR);
int second = now.get(ChronoField.SECOND_OF_MINUTE);
int mils = now.get(ChronoField.MILLI_OF_SECOND);
System.out.printf("%d시 %d분 %d.%d초", time, min, second, mils);
```
출력값
```
14시 52분 10.999초
```
ChronoField를 사용하면 더 다양한 값을 얻을수 있다.

with메서드도 withHour, withMinute 등이 있음  
<br><br>

### LocalTime - parse, format 메서드
```
String ts = "10:10:10";
System.out.println(LocalTime.parse(ts)); //10:10:10
```
LocalTime의 기본 문자열 패턴은 hh:mm:ss 이다. 설정하지 않은 초 이하는 다 0으로 설정됨.  

LocalTime역시 DateTimeFormatter를 사용해서 다양한 문자열을 패턴을 사용해 LocalTime객체 생성이 가능.  
반대로 LocalTime객체를 사용해 다양한 패턴의 시간형태 문자열을 생성가능하다.  
```
String ts = "10.10.10";
String pattern = "HH.mm.ss";
LocalTime time = LocalTime.parse(ts, DateTimeFormatter.ofPattern(pattern));
System.out.println(time); //10:10:10
String time_str = time.format(DateTimeFormatter.ofPattern(pattern));
System.out.println(time_str); //10.10.10
```
<br><br>

### LocalTime - truncatedTo 메서드

truncatedTo은 밑으로 다 버리는 메서드이다.
```
LocalTime now = LocalTime.now();
System.out.println(now);
System.out.println(now.truncatedTo(ChronoUnit.HOURS));
```
출력값
```
17:07:43.062
17:00
```
ChronoUnit을 사용해서 시간을 조정한다.


---

### LocalDateTime

LocalDate와 LocalTime을 합쳐놓은 클래스  
날짜와 시간정보 모두 포함하고 있고 두 클래스에서 사용하는 클래스 대부분을 
LocalDateTime클래스 역시 가지고 있다.  

LocalDateTime 원형
```
public final class LocalDateTime
extends Object
implements Temporal, TemporalAdjuster, ChronoLocalDateTime<LocalDate>, Serializable
```
3클래스가 상속관계는 똑같은 기능의 메서드들이 선언되어있다.
<br><br>


### LocalDateTime - of메서드

LocalDateTime의 객체를 반환하는 of메서드는 오버로딩이 7개나 되었다....
최소한 년, 월, 일, 시간, 분은 설정해줘야하 한다.
그이후로 초, 밀리초 를 다양한 방식으로 입력받도록 오버로딩 되어있다.

또는 LocalDate와 LocalTime객체를 매개변수로 입력받아 LocalDateTime 객체 생성도 가능하다.
```
LocalDate d = LocalDate.of(2015, 12, 31);
LocalTime t = LocalTime.of(12, 34, 56);
LocalDateTime dt = LocalDateTime.of(d, t);
System.out.println(dt);
```
출력값
```
2015-12-31T12:34:56
```

이녀석도 get, with, format, parse, 비교메서드 등 다있다.

---

### TemporalAdjusters - 시간조정자


해당일자로 부터 다음주 토요일, 다음달 2번째 월요일 등  
까다로운 날짜 계산이 필요할때 TemporalAdjusters를 사용하면 편하다.  
위같은 연산을 plus, minus, with 메서드 로만 진행하면 분명 복잡한 연산이 필요하겠지만 TemporalAdjusters에는 이미 해당 메서드들이 다 정의 되어있다.  
```
LocalDate today = LocalDate.now();
System.out.println(today);

System.out.println("--------------");
System.out.println(today.with(TemporalAdjusters.firstDayOfMonth())); //첫째날 반환
System.out.println(today.with(TemporalAdjusters.lastDayOfMonth()));  //마지막날 반환
System.out.println(today.with(TemporalAdjusters.firstInMonth(DayOfWeek.TUESDAY))); //첫 화요일 반환
System.out.println(today.with(TemporalAdjusters.lastInMonth(DayOfWeek.TUESDAY))); //마지막 화요일 반환
System.out.println(today.with(TemporalAdjusters.previous(DayOfWeek.TUESDAY))); // 저번 화요일
System.out.println(today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY))); //다음 화요일
```
출력값
```
2019-02-07
--------------
2019-02-01
2019-02-28
2019-02-05
2019-02-26
2019-02-05
2019-02-12
```

---

## Period, Duration - 두 시간, 날짜 차이를 구하는 클래스


### Period

날짜 차이가 얼마나 나는지 구하고 싶을땐 Period메서드를 사용하면 편하다.
```
LocalDate d1 = LocalDate.of(2014, 1, 1);
LocalDate d2 = LocalDate.of(2015, 12, 31);

Period pe = Period.between(d1, d2);
System.out.println(pe.getYears());
System.out.println(pe.getMonths());
System.out.println(pe.getDays());
```
출력값
```
1
11
30
```
총 1년 11개월 30일 차이난다는것을 알 수 있다.

### Duration

시간 차이가 얼마나 나는지 구하고 싶을땐 Duration

```
LocalTime t1 = LocalTime.of(00, 00, 10);
LocalTime t2 = LocalTime.of(12, 34, 56);

Duration du = Duration.between(t1, t2);
System.out.printf("%d시 %d분 %d초\n",
		du.toHours(), 
		du.toMinutes()%60,
		du.getSeconds()%60
		);
```
출력값
```
12시 34분 46초
```

DDAY를 구하고 싶다면 LocalDate의 until메서드를 사용하면 편하다.

```
LocalDate endDay = LocalDate.of(2019, 7, 19);
LocalDate today = LocalDate.now();

long dday = today.until(endDay, ChronoUnit.DAYS);
System.out.println(dday);
```

출력값
```
162
```


# Pathcer, Matcher

`java.util.regex`에 포함된 클래스로 정규식에 사용되는 클래스이다.  
`Pattern`은 정규식을 정의하는데 사용되고    
`Matcher`는 정규식을 데이터와 비교하는 역학을 한다.  



```java
Pattern p = Pattern.complie("c[a-z]*");

Marcher m = p.matcher(compStr);

if(m.matched())
...
```

사용법은 간단하다.  

`Pattern`의 `complile` 메서드를 사용해서 정규식을 정의하고 `matcher` 메서드를 사용해서 정규패턴을 검사할 `Matcher` 객체를 만들면서 검사할 문자열을 전달한다.   
만들어진 `Matcher`클래스는 `matched`함수를 호출하면서 패턴과 문자열이 일치하는지 `true`, `false`로 반환한다.  

