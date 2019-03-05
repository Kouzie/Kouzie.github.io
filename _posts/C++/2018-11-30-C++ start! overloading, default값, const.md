---
title:  "C++ start! overloading, default값, const!"
read_time: false
share: false
toc: true
author_profile: false
classes: wide

categories:
  - C++
tags:
  - C++
  - 열혈
---

### 소개

Unmanaged언어 중 하나쯤은 숙지하고 있어야 한다는 생각에 배우게 되었다. 윤성우 저자님의 교재를 사용할 것이다. 사실 학교다닐 때 한번 훑어본적 있지만 다 까먹어서 처음부터 다시 공부하는거..... 일단은 천천히 문법부터...

모든 언어가 그렇듯 문법은 서로 엇비슷한것 같다. 그래서 포스트할 내용들은 C++에서 특별히 사용되는, C++에서만 혀용되는 그런 내용들만 올릴 계획이다.  

***

#### C++은 별도의 포멧 지정이 필요 없다.

```c++
int var;  
std::cout<<var;  
std::cin>>var;  
```

C에서는 ```printf("%d", var);``` 같이 썼지만 C++에선 포멧 지정을 해줄 필요가 없다. 배열도 마찬가지  

***

#### 오버로딩
```c++
void MyFunc(void)
{
  std::cout<<"MyFunc(void)"<<std::endl;
}
void MyFunc(char c)
{
  std::cout<<"MyFunc(char c)"<<std::endl;
}
int main()
{
  MyFunc();
  MyFunc('A');
  return 0;
}
```

C에선 함수이름이 같으면 오류가 발생하지만 C++은 허용, C++에선 허용, 인자의 개수와 자료형만 다르면OK.

***

#### 함수 매개변수의 디폴트값 지정

```c++
int MyFunc(int num=7);
int main()
{
  ...
}
int MyFunc(int num)
{
  ...
}
```

선언부에 함수 선언과 동시에 매개변수 초기화를 할 수 있다.  

```c++
int MyFunc1(int a=1, int b=2, int c=3)
int MyFunc2(int a, int b=2, int c=3)
```
위처럼도 가능, 하지만 아래는 불가능

```c++
int MyFunc3(int a=1, int b=2, int c)
```
만약 함수 호출을 `MyFunc3(1)` 같이 하면 **a와 b는 초기화 되지만 c는 초기화 되지 않기 떄문에** 오류가 발생하게 될 것. `MyFunc3`처럼은 사용 못한다.  

***

#### 키워드 Const의 의미

`const`는 다음과 같이 여러 방법으로 쓰일 수 있다.
```c++
const int num=10;
//흔히 쓰이는 변수를 상수화

const int* ptr1=&val1;
//포인터 ptr1을 사용해서 가리키는 주소를 변경 가능.
//ptr1=ptr1+1 은 되지만 *ptr1=*ptr+1 은 안됨

int* const ptr2=&val2;
//포인터가 상수화되어 주소변경이 불가능. 주소안의 데이터는 변경 가능
//*ptr1=*ptr1+1 은 되지만 ptr1=ptr1+1 은 안됨

const int* const ptr3=&val3;
//위에 두개를 합친 방법. 둘다 변경이 안됨.

int SimpleFunc(int num) const;
//SimpleFunc안에선 모든 변수값 변경이 허용 안됨

int SimpleFunc(const int num);
//SimpleFunc안에선 변수num값만 변경이 허용 안됨
```

const는 문법보단 어떻게 사용하는지가 더 중요한듯. 안정성이 좋아짐.
