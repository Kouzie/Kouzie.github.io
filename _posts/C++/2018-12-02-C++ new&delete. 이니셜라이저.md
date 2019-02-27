---
title:  "C++ new&delete, 생성자, 멤버 이니셜라이저!"
read_time: false
share: false
toc: true
author_profile: false

categories:
  - C++
tags:
  - C++
  - 열혈
---

### new & delete
C에선 힙 메모리 할당을 위해 malloc과 free를 썼지만 C++은 new와 delete를 쓴다.  
malloc은 다음과 같은 불편한 점이 있다.
- 할당된 대상의 정보를 무조건 byte단위로 전달한다.
- 반환형이 void형 포인터이기 때문에 형변환을 거친다
```
char *str=(char*)malloc(sizeof(char)*len);
```
C++은 다음과 같이 편하게 사용 가능하다.
```
int *ptr=new int;
int* arr1=new int[3];
char* title = new char[strlen(mytitle)+1];

```
그리고 제일 중요한건 C++에선 생성자, 소멸자 개념이 있는데 이를 호출할 new와 delete를 무조건 사용해야 한다.
***
### 접근제어자
- public
어디서든 접근 허용
- protexted
상속관계일때 유도(하위) 클래스에서 접근허용
- private
클래스 내에서만 접근 허용

접근제어자 키워드를 사용해서 객체 멤버 접근을 막기도 하지만 허용해야 할 때도 있다. 타 객체 또는 메인 함수에서 객체 멤버 값을 필요로 할때 접근 pulic으로 선언되어 안전하게 접근할 수 있는 함수(함수 뒤에 const키워드를 많이 씀)를 엑세스 함수라 한다.  
다른 사람 코드들을 보면 Getxxx, Setxxx와 같은 함수들이 보통 엑세스 함수이다. 객체를 만들면 엑세스 함수도 자동으로 만들어 주는 IDE도 있고 당장 필요하지 않고 사용하지 않아도 기본으로 만들고 가능 겨우도 있다.
***
### 생성자
생성자의 사용형태는 다음과 같다.
```
...
SimpleClass sc1(100);
SimpleClass *ptr1 = new SimpleClass(100);
SimpleClass sc1;
SimpleClass *ptr1 = new SimpleClass;
SimpleClass *ptr1 = new SimpleClass();

SimpleClass sc1()
//이 문장은 사용할 수 없다. SimpleClass안에 sc1()이라는 함수가 있을 수 있기 때문.
```
생성자는 객체가 생성될때 호출되는 함수이다. 위와 같은 다양한 형태로 호출할 수 있다. 매개변수를 전달할 수 도 있으며 보통 객체안의 멤버를 초기화하는데 많이 쓰인다.

생성자의 형태는 다음과 같다.
```
class AAA
{
  private:
        int num;
  pulic:
        AAA()
        {};
        AAA(int n)
        {
          num=n;
        }
}
```
생성자는 오버로딩으로 인해 다른 형식으로 여러개 생성될 수 있고 반환형이 없다. ```AAA();```는 디폴트 생성자 라고 위에서는 코드로 직접 써서 만들었지만 굳이 만들지 않아도 자동 생성된다.
***
#### 멤버 이니셜라이저를 이용한 멤머 초기화
아래는 생성자를 사용해 멤버를 초기화 하는 일반적인 방법
```
class Rectangle
{
  private:
          Point upLeft;
          Point lowRight;
  public:
          Rectangle(const int &x1, const int &y1, const int &x2, const int &y2//생성자
          {
            upLeft.x = x1;
            upLeft.y = y1;
            lowRight.x = y2;
            lowRight.y = y2;
          }
          //이녀석이 생성자
}
```
<br>
생성자에 멤버 이니셜 라이저를 사용하면 아래와 같이 조금 간편하게 할 수 있다. 참고로 이니셜라이저 뜻은 초기화란 뜻이다.
```
class Rectangle
{
  private:
          Point upLeft;
          Point lowRight;
  public:
          Rectangle(const int &x1, const int &y1, const int &x2, const int &y2) : upLeft(x1, y1), lowRight(x2, y2)
          {//empty
          }
}
```
<br>
간편함 뿐만 아니라 이니셜라이저를 사용하면 const변수도 초기화 할 수 있다.
```
class SimpleClass
{
  const int num;
  SimpleClass(int n) : num(n)
  {}
}
```
const은 생성과 동시에 초기화 해야 오류가 안난다고 했지만 위 코드는 int num이 생성되는 과정이 아니다. SimpleClass의 멤버를 설명하는 코드이고 num의 생성은 SimpleClass객체가 인스턴스(생성)되어 메모리에 올라갈때 생성된다. 즉 생성자에서 초기화 해주면 된다는 뜻.  

그리고 이니셜라이저는 성능도 더 좋다. 이니셜 라이즈 코드 ```num(n)```은 ```int num=n```이고  몸체에서 초기화 한건은 ```int num; num=n;``` 라 할수 있다. 생성되는 바이너리 코드 양이 다르다.
***
### 소멸자
객체 소멸시 반드시 호출되는 함수, 클래스 이름 앞에 ~가 붙은 형태. 디폴트 생성자처럼 소멸자도 디폴트로 만들어진다.
```
class AAA
{
  //empty
};
```

만약 이런 객체를 만들면 컴파일러에 의해 아래와 같은 형식으로 자동 생성된다.
```
class AAA
{
  public:
        AAA()
        {}
        ~AAA()
        {}
};
```
객체의 소멸 과정은 소멸자가 먼저 실행되고 메모리에서 해제된다.
