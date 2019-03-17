---
title:  "네트워크 - 패킷 트레이서 사용"
read_time: false
share: false
toc: false
author_profile: false
classes: wide

categories:
  - Network
  - PacketTracer

tags:
  - Network
---

## 패킷 트레이서 개요

앞으로 다룰 2가지 모델   

![image1]({{ "/assets/network/packettrace/pkt1/image1.png" | absolute_url }}){: .shadow}  

라우터와 스위치를 다룰때는 보통 CLI환경에서 실행된다.  

### 실행모드와 구성모드

CLI안에서 관리자는 실행모드와 구성모드에서 명령어를 입력한다.  

### 실행모드  

실행모드는 `User모드`와 `Privileged모드`로 나누어지며 Privileged는 관리자 모드라한다.  

`User` 모드는 `>`가 앞에
`Privileged` 모드는 `#` 앞에 붙는다.  

`Privileged` 모드에선 모든설정을 다 조회 가능하다.  

### 구성모드  

구성모드는 `(config)` 가 앞에 붙는다. 프롬프트 앞에 붙는 기호를 보고 현재 자신이 어떤 모드에 위치한지 알 수 있다.  

첫 실행시 User모드에 위치한다.  
유저 모드와 관리자 모드간의 변환은 다음 명령어를 통해 가능하다.  

`Usermode -> Privileged => enable`  
`Privileged -> Usermode => disable`, `exit`  

`실행모드 -> 구성모드 => configure terminal`, `conf t`

외에도 `interface(sub)`, `line`, `controller` 모드가 또 있다.  
`Router`의 경우 `router`모드가 추가된다.  

각 모드에서 수행할 수 있는 명령어가 다름으로 잘 숙지하고 있어야 한다. 

> Privileged 모드에서 이상한 명령을 쓰면 도메인서버를 검색, 분석하는 Look up하는 명령어를 실행하기 때문에 시간이 오래걸린다.   
> Config 모드가서 no ip domain-lookup 명령을 기본 구성 변경.  

시스코 장비는 앞에 no를 붙이면 앵간한 명령은 다 반대의 개념을 갖게된다.  
라우터도 인터페이스(연결부분)을 죽이려면 shutdown, 살리려면 no shutdown을 사용한다. (줄여서 no sh)  



Privileaged 모드에서 show interface 를 하면 갖가지 설정을 볼 수 있다.  
![image1]({{ "/assets/network/packettrace/pkt1/image1.png" | absolute_url }}){: .shadow}  

config모드에서 보고싶다면 do show interface를 사용하면 구성모드에서도 설정확인가능

Privileged 모드에서 이상한 명령을 쓰면 도메인서버를 검색, 분석하는 Look up하는 명령어를 실행하기 때문에 시간이 오래걸린다. 기본설정을 바꾸어 오타를 쳐도 이런일이 생기지 않게 바꾸어 보자.
Config 모드가서 no ip domain-lookup 명령을 써서 해제하자.

신기한건 앞에 no를 붙이면 앵간한 명령은 다 반대의 개념을 갖게된다. 라우터도 인터페이스(연결부분)을 죽이려면 shutdown, 살리려면 no shutdown을 사용한다. 줄여서 no sh
 
 
연결된 인터페이스 정보를 보고싶으면
show interfaces fastEthernet 0/1
show interfaces fastEthernet 0/2
0/0~ 1/12 총 24개
밑의 Switch3이 거슬린다. 호스트 이름을 바꾸려면 config 모드에서 
hostname “이름” 을 사용

해당 인터페이스의 통신설정을 바꿔보자
config모드에서 interface fastEthernet 0/1 명령으로 인터페이스sub 모드로 이동
 duflex full을 통해 바꾸자.
속도를 조절해보자.
 speed 10명령으로 속도를 10으로 설정하자
 
 
위와 같이 구성하고 스위치와 연결된 각 2대의 컴퓨터는 대역이 다르다, 한쪽은 192.168.0.1, 한쪽은 192.168.1.1

라우터도 스위치와 비슷한 ISO 환경이다
먼저 no를 통해 cli로 진입하고 config모드 그리고 interface fastethernet 0/0을 통해 sub모드로 진입해서 
ip address 192.168.0.1 255.255.255.0 를 통해 IP를 부여하고 no shutdown 명령을 통해 start시키자.
 
이번엔 interface fastethernet 0/1로 들어가 마저 설정해주자
ip address 192.168.1.254 255.255.255.0, no shutdown
   
연결되어버림

pc마다 사용 게이트웨이를 설정해주고 192.168.1.10 에서 192.168.0.10 으로 ping 해보자
 
0.10 뿐만 아니라 0.1 로도 통신 가능하다.

이렇게 대충 라우터와 스위치 설정을 끝냈는데 show running-config를 통해 실행중인 설정들을 볼수 있다.
라우터를 껐다키면 이런 설정들은 다 날아가게되는데 이걸백업용도로 copy r s 명령을 쓴다.
그리고 show startup-config를 치면 running-config에 쓰여있던 내용이 그대로 나온다.
Nvram에 ram에 있던 내용을 백업한다 생각하면 된다.
