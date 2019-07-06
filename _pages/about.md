---
permalink: /about/
title: "About"
excerpt: "About Me!."
classes: wide
last_modified_at: 2019-07-05T15:15:09-04:00
toc: true
---
      
# profile


<img src="https://kouzie.github.io/assets/about/profile.jpg" alt="profile" class="center" width="250" style="border-radius: 5%;">

고지용 (1996.9 18)   
평범한 가정에서 자란 보통사람.  

그래도 개발이 제일 재밌다고 생각하는 초보 개발자  
이젠 개발로 돈도 한번 벌어보고 싶다....  

## 로드맵

* **북경 이공대 부속 중학교  - 고등학교 졸업**  
  생긴것 처럼 중국 유학생이다, 한국 온지 4년이 넘어 까막눈이 되었지만 회화는 자신있게 말할 수 있다.  
<br>
* **한국IT전문학교 - 학사취득**   
  꿈을 찾아 IT로 오게되었다~~(사실 북경대 시험 떨어짐)~~ 보안을 배우고 졸업작품으로 간단한 백신을 만들어 보았다.  
<br>
* **군 입대 - 육군 정보보호병(17.2 ~ 18.11)**   
  네트워크 장비, 방화벽, NAC로 군 인트라넷 연결을 담당하였다. 랜선을 순식간에 집는 능력을 얻었다.   
<br>
* **국비교육(쌍용교육센터)**  
  전역후 웹을 배우고 싶어 신청했다. 좋은 강사님을 만나 재밌게 교육받았다. 현재 스프링 프레임워크로 프로젝트를 진행중이다.   

---


# Project

## 0. Strike

학교 졸업 작품으로 만든 백신, C/C++, Qt 프레임워크를 사용해 개발하였다.  

![process]({{ "/assets/project/strike/process.png" | absolute_url }}){: .shadow}{: width="400"}
![job]({{ "/assets/project/strike/job.png" | absolute_url }}){: .shadow}{: width="400"}


### 실행화면

<vedio src="https://kouzie.github.io/assets/project/strike/media1.avi" controls="controls"></vedio> 

### 간단한 설명

이진 검색을 통한 바이러스 MD5 해시탐지(해시는 Virus Total에서 제공)  

Yara의 PE구조를 분석을 이용한 바이러스 패턴탐지  

실시간 탐지 - WindowSDK에서 제공하는 오픈소스 사용.  
> https://github.com/pauldotknopf/WindowsSDK7-Samples/tree/master/winui/shell/appplatform/ChangeNotifyWatcher   

dll인젝션을 통한 후킹 - 프로그램 실행전 시그니처를 통해 안전한 실행파일인지 검사, 시그니처가 없다면 후킹을 통해 제한된 권한으로 실행(MinHook Hooking Library 사용)  
>https://www.codeproject.com/Articles/44326/MinHook-The-Minimalistic-x-x-API-Hooking-Libra?msg=4843828#xx4843828xx  

> 후기: 오픈소스가 아니였다면 구현조차 안됐던 프로젝트, 실시간 탐지를 위한 필터 드라이버를 적용시키지 못한게 가장 아쉬웠다...


## 1. 테니스 계수기 출력 프로젝트

자바 문법수업 끝나고 객체지향적 설계 밑 코딩 실력을 배양하기 위한 프로젝트  

![image3]({{ "/assets/project/tennis/image3.png" | absolute_url }}){: .shadow}{: width="400"}
![image1]({{ "/assets/project/tennis/image1.png" | absolute_url }}){: .shadow}{: width="400"} 

`jfiglet` 라이브러리를 사용해 테니스 계수기 출력

> 상세설명: <a href="https://kouzie.github.io/java/project/java-테니스-프로젝트!/">https://kouzie.github.io/java/project/java-테니스-프로젝트!/</a>

## 1.5 채팅 프로그램

Java 소켓, 멀티스레드 파트를 마치고 시작한 개인 프로젝트.  
javaFX, Scene빌더를 사용해 UI 구현  
방을 만들어 여러명이서 채팅 가능.

![image1]({{ "/assets/project/chatting/image1.png" | absolute_url }})
![image3]({{ "/assets/project/chatting/image3.png" | absolute_url }})
![image2]({{ "/assets/project/chatting/image2.png" | absolute_url }})  

> 상세설명: <a>https://kouzie.github.io/java/project/java-채팅-프로그램!/</a>


## 2. DB Project  

Oracle DB sql, pl/sql 수업을 마치고 DB 개념적, 논리적 모델링 밑 SQL 실력향상을 위해 진행한 프로젝트  

![concetp]({{ "/assets/project/dbproject/modeling_concept.png" | absolute_url }}){: .shadow}{: width="700"}    
![logic]({{ "/assets/project/dbproject/modeling_logic.png" | absolute_url }}){: .shadow}  

> 상세설명 : <a href="https://prezi.com/mofotqxheo6s/db/">https://prezi.com/mofotqxheo6s/db/</a>  
> 참고자료: <a href="https://kouzie.github.io/database/DB-nested-table/">https://kouzie.github.io/database/DB-nested-table/</a>  


## 3. Java/Servlet MVC Model2 프로젝트

JSP/Servlet 수업이 끝나고 MVC Model2 패턴으로 웹 어플리케이션 개발  

기존 사이트 https://www.poing.co.kr/seoul 을 그대로 구현하는 프로젝트로 `JSP/Servlet`, `jQuery`, `Oracle` 사용

![dbmodel_login]({{ "/assets/project/poing/dbmodel_login.png" | absolute_url }}){: .shadow}  

### 실행화면  

**사용자 정보 수정**  
![image13]({{ "/assets/project/poing/image13.gif" | absolute_url }}){: .shadow}   

**리뷰 출력, 좋아요 찜하기**  
![image23]({{ "/assets/project/poing/image23.gif" | absolute_url }}){: .shadow}  

**GoogleMap, 레스토랑 리스트출력**
![image63]({{ "/assets/project/poing/image63.gif" | absolute_url }}){: .shadow}  

**관리자 레스토랑 리뷰남기기**  
![image70]({{ "/assets/project/poing/image70.gif" | absolute_url }}){: .shadow}  


> 소스코드 : <a href="https://github.com/Kouzie/Poing">https://github.com/Kouzie/Poing</a>


---
<!-- 
## 간단한 소개  

엄청 열심히 살아온 인생은 아니지만  
게으르게 살았다기에는 열심히 산 인생.  

성격에 대한 객관적 정보를 제공하기 위해 mbti검사를 해보았습니다.   

![image70]({{ "/assets/about/mbti.png" | absolute_url }}){: .shadow}{: width="300" }{: align-left}   

> <a href="https://www.16personalities.com/ko/성격유형-isfj">https://www.16personalities.com/ko/성격유형-isfj</a>  

`수호자형 사람은 무엇을 받으면 몇 배로 베푸는 진정한 이타주의자로 열정과 자애로움으로 일단 믿는 이들이라면 타인과도 잘 어울려 일에 정진합니다. 약 13%로 꽤 높은 인구 비율을 차지하는데, 인구 대다수를 차지하는 데 있어 이들보다 더 나은 성격 유형은 아마 없을 것입니다. `

좋게 포장해 놓았지만 세상 인구 13%가 가지는 가장 평범한 유형이다...  
 -->
