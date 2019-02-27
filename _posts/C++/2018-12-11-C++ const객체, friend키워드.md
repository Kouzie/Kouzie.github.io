---
title:  "C++ static멤버변수, static멤버함수, const static 멤버변수, const객체, mutable키워드, friend.md!"
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

### static 멤버변수
```
class SoSimple
{
  private:
    static int simObjCnt;
  
  public:
    SoSimple()
    {
      simObjCnt;
      cout<<simObjCnt<<endl;
    }
};

int SoSimple::simObjCnt=0;

int main()
{
  SoSimple sim1;
  SoSimple sim2;
  SoSimple sim3;
  //sim1, 2, 3가 simObjCnt를 공유한다.
}
```
static변수의 생성 및 소멸 시점은 전역변수와 같다. 프로그램 시작과 동시에 생성된다. 인스턴스를 생성하지 않아도 존재한다.  
굳이 ```int SoSimple::simObjCnt=0;``` 같이 밖에서 초기화 하는 이유도 객체 안에 존재하지도 않고 인스턴스가 생길때 생성되는 것 도 아니기 떄문이다.  
그리고 생성자 안에서 초기화하면 인스턴스 생성때마다 0으로 초기화 될 것.
그저 멤버변수처럼 접근하도록(편하니까) 만든 방법이다. 전역변수와 다른점이 있다면 정보 은닉이 된다는점.

***

### static멤버함수

static멤버함수도 static멤버변수랑 똑같다. 모든 인스턴스가 공유하고 객체 멤버로 존재하는 것이 아니다.  
전역함수를 대체 가능하고 정보은닉이 가능하다.
```
class SoSimple
{
  private:
    int num1;
    static int num2;
  
  public:
    SoSimple(int n) : num1(n)
    {}
    static void Adder(int n)
    {
      //num1+=n; 주석을 지우면 컴파일 에러
      num2+=n;
      //static멤버함수는 static멤버변수만 사용 가능하다.
    }
};

int SoSimple::num2=0;
```
static멤버함수 입장에선 아직 만들어 지지도 않은 num1멤버변수에 접근하려고 하기 때문에 에러가 뜬다.
static멤버함수는 객체의 멤버도 아니고 인스턴스 생성 이전에도 호출 가능하다.

***

###const static 멤버변수

```
class ConstStaticClass
{
  public:
    const static int RUSSIA = 1707540;
    const static int KOREA = 1957290;
};
```
static은 밖에서 초기화 했지만 const static은 객체 안에서 초기화 가능하다. 안에 있어도 바뀔일이 없으니 가능하게 문법을 만들었나 봄.

### const 객체
const 변수를 선언하듯이 객체도 const로 선언할 수 있다.
```
const SoSimple sim(20);
```
위처럼 선언된 객체는 데이터 변경을 허용하지 않으며 함수도 const멤버 함수만 호출 가능하다.  

const는 오버로딩 구분 조건으로도 쓰일 수 있다.
```
void SimpleFunc()
{...}

void SimpleFunc() const
{...}

...

const SoSimple obj(10);
obj.SimpleFunc();
```
위의 두 함수가 SoSimple클래스의 멤버함수들일때 const객체인 obj에게 호출될 함수는 const가 달린 SimpleFunc일 것이다.


***

### mutable 키워드

가끔씩 const함수 내에서 변수값을 변경하고 싶을때가 있다. 그때 쓰는 키워드가 mutable이다.
const를 의미없게 만드는 키워드 임으로 신중하게 사용해야 한다.
```
class SoSimple
{
  private:
    int num1;
    mutable int num2;
  
  public:
    SoSimple(int n1, int n2) : num1(n1), num2(n2)
    {}
    void ShowSimpleData() const //const함수이기에 안에서 변수값 변경이 불가능
    {
      cout<<num1<<num2<<endl;
    }
    void CopyToNum2() const
    {
      num2=num1;
      //num2는 mutable 키워드가 사용된 변수라 변경 가능함.
    }
};
```

***

### friend 키워드

private은 자기자신 아니면 누구도 접근할 수 없어! 란 개념으로 만들어 졌지만 friend키워드를 쓰면 자신이 지정한 클래스의 객체들은 private일지라도 접근 가능하게 한다.
```
Class Boy
{
  private:
    int height;
    friend class Girl;
  public:
    Bod(int len) : height(len)
    {}
};
//이제 Girl클래스 객체들은 Boy의 private에 모두 접근 가능하다.
//friend는 클래스 뿐만 아니라 함수 대상으로도 선언할 수 있다.
//해당 함수에선 private에 바로 접근 가능하이기에 신중히 사용해야 한다. 굳이 왜 사용하냐하면 연산자 오버로딩에서 요긴하게 쓰인다.