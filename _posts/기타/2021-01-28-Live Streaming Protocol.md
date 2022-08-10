---
title:  "Live Streaming Protocol!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
## ## classes: wide

categories:
  - 기타
---

## 개요  

영상, 미디어에 관련된 용어를 간략히 설명한다.  

**H.264**  
`VCEG(Video Coding Experts Group)`와 `MPEG(Moving Picture Experts Group)` 에서 만든 동영상 표준 압축 방식(코덱)  
`MPEG-4 Part 10, AVC(Advanced Video Coding), H.264` 여러가지 이름으로 불리지만 보편적으로 `H.264` 으로 불림  

> 비트레이트(bitrate): 특정한 시간 단위(이를테면 초 단위)마다 처리하는 **비트의 수**이다

**AAC (Advanced Audio Coding)**  
오디오 코덱으로 `MPEG-4 Audio` 으로도 불리지만 `AAC` 가 보편적

## 스트리밍 프로토콜

### 라이브 스트리밍  

> <https://www.youtube.com/watch?v=6t6Si0BWCOo&t=349s>  
> <https://d2.naver.com/helloworld/7122>  
> <https://meetup.toast.com/posts/131>  
> <https://medium.com/naver-cloud-platform/인터넷-라이브-방송은-어떤-기술로-만들어질까요-98423dc7fcd4>  

`라이브 스트리밍` 이란 TV 생방송처럼 촬영한 정보를 실시간으로 사용자의 동영상 플레이어로 보내 재생하는 것  

> 라이브 스트리밍은 사용자의 네트워크 상태에 맞춰 영상 해상도를 줄일수 있어야함

`온 디맨드 스트리밍` 이란 저장된 동영상 파일을 사용자의 요구가 있을 때 동영상을 재생하는 것  

### 전송 서버(CDN)

미디어 서버가 전송서버 역할까지 하는 경우가 있지만 시청자 수가 늘어다면 전송담당 서버를 사용해야함  
많은 클라이언트가 요구하는 데이터의 경우 캐시에 담아 지원

### 멀티비트 레이트  

화질선택 기능을 지원하기 위한 기술  

동영상을 화질별로 세그먼트, 매니페스트로 나누어 클라이언트에게 선택 재생 할 수 있도록 지원한다.  

### RTP, RTCP, RTSP

> RTP: <http://www.ktword.co.kr/abbr_view.php?m_temp1=1381>  
> RTCP: <http://www.ktword.co.kr/abbr_view.php?m_temp1=1805>  
> RTSP: <http://www.ktword.co.kr/abbr_view.php?m_temp1=1798>  

![live-stream1](/assets/2021/live-stream1.png)  

**RTP (Real-Time Transport Protocol)**  
실제 동영상 전송을 위해 사용되는 프로토콜, `UDP` 프로토콜로 전송된다.  

**RTCP (Real-Time Transport Control Protocol)**  
`RTP` 와 같이 사용되며 `RTP` 의 전송 통계와 `QoS`를 모니터링  
스트림의 동기화를 지원하기 위해 속도 조절 처리 등을 담당  

**RTSP (Real-Time Streaming Protocol)**  
미디어 플레이어가 사용하는 프로토콜로 응용계층의 프로토콜 `554 PORT` 를 사용한다.  
`RTCP` 와 마찬가지로 `RTP` 데이터의 흐름을 제어  
`PLAY, PAUSE, REPLAY, ROLLBACK, MOVE` 등의 요청을 처리  

셋 모두 현재 잘 사용하지 않는다.  

### RTMP(Real Time Messaging Protocol)

`Adobe` 에서 개발 `TCP` 기반 프로토콜로 `1935` 포트사용  

`RTSP` 이후의 새로운 라이브 스트리밍 프로토콜로 현재 가장 많이 사용되고 있다.  
대부분의 라이브 스트리밍 플랫폼(유튜브, 트위치, 아프리카 등)이 해당 프로토콜로 라이브 스트리밍을 지원한다.  

`RTMP` 는 오디오, 비디오 밎 기타 데이터를 인터넷을 라이브 스트리밍할 때 사용  

오래된 규격, H.265 등 최신 비디오 코덱 지원 X, 암호화 불가능이라는 이슈등이 있어 서서히 사장되고 있는 프로토콜이기도 하다.  

### SRT (Secure Reliable Transport) Protocol

> 출처: <https://www.youtube.com/watch?v=2BkTLnHvH_g>

Haivision 에서 2017 년 오픈소스로 공개한 프로토콜이다.  

꼼꼼한 오류체크를 처리하기 때문에 높은 안전성을 가지며 암호화 전송 또한 가능하다.  

범용성과 호환성이 떨어지지만 SRT 프로토콜이 오픈소스로 풀린 이상 금방 해결될 예정이다.  

![live-stream3](/assets/2021/live-stream3.png)  

실제 송신자(카메라)와 수신자(컴퓨터, 방송국)은 SRT 인코더와 디코더를 통해서 데이터를 송/수신 하기 때문에 이게 인터넷을 통해서 데이터가 오는지, SDI(Serial Digital Interface) 케이블이 직접 연결되어서 데이터가 오는지 모른다.  

아래와 같이 인코더와 디코더가 H.264, H.265 등의 압축 기술로 데이터를 압축, 전송, 압축해제, 암호화(AES) 등의 작업을 원활히 해야 문제가 발생하지 않는다.  

![live-stream4](/assets/2021/live-stream4.png)  

인터넷을 통해 전송되다 보니 데이터 유실, 딜레이 등의 문제가 생기는데 SRT 의 인코더 디코더는 커다란 버퍼를 이용하여 에러체크를 진행하고 유실 데이터를 최소화 한다.  
(단 가격이 비싸다)

장비가 많이 필요하고 도입비용이 높은만큼 방송국과 같은 업체에서 많이 사용함

**전송모드 - 리스너, 콜러**  
IP주소가 고정되어 있는 인코더 혹은 디코더  
영상 전송 주체와는 상관없다.  
대부분 영상을 수신받는 쪽(디코더)가 리스너가 된다.  

## Adaptive HTTP Live Streaming (HLS, DASH)

줄여서 `Adaptive Streaming` 이라고도 한다.  

과거 스트리밍 프로토콜은 단순 데이터의 전송뿐만 아니라 전송 규격(해상도, 포멧 등)에 맞도록 동영상 파일을 읽어서 변형하려면 동영상에 대한 정보 분석 기능도 갖춰야 한다.  
웹 서버에 비해 도입시 많은 처리작업이 필요하다  

프로토콜도 특이해서 방화벽, NAT 등에서의 별도작업도 필요하다.  

많은 기업들이 단순 `HTTP` 프로토콜만을 사용해 스트리밍 할 수 있도록 프로토콜을 만들었다.  

그중 가장 유명한게 HLS, DASH 이다.  

### HLS(HTTP Live Streaming)

`애플`에서 만든 스트리밍 프로토콜  

HTTP 기반 스트리밍 프로토콜, 2009년도 출시, `RFC8216` 등록
`QuickTime, OSX, Safari` 등에서 사용할 목적으로 만들어 졌으며  
단순한 구조로 인해 많은 기업들이 동영상 스트리밍 지원시 해당 프로토콜을 사용한다.  

`H.264 + AAC` 등으로 포맷된 동영상을 10초 정도의 작은 파일(`.ts`)로 분할  
> `MPEG-2 TS(transport stream)` 컨테이너 타입의 세그먼트(분할) 파일  

분할된 파일 정보가 작성된 매니페스트(`.m3u8`) 와 같이 사용,  

HLS 프로토콜을 사용하는 프로그램에 매니페스트 파일을 제공하면 분할된 세그먼트 파일을 실시간으로 불러들이면서  
라이브 스트리밍이 지원된다.  

#### m3u8 포맷

```
#EXTM3U
#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=200000, RESOLUTION=720x480
http://ALPHA.mycompany.com/lo/prog_index.m3u8
#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=200000, RESOLUTION=720x480
http://BETA.mycompany.com/lo/prog_index.m3u8

#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=500000, RESOLUTION=1920x1080
http://ALPHA.mycompany.com/md/prog_index.m3u8
#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=500000, RESOLUTION=1920x1080
http://BETA.mycompany.com/md/prog_index.m3u8
....중략....
```

`root` `m3u8` 파일은 각 해상도별 `m3u8` 위치를 나타내고  
각 해상도의 `m3u8` 은 아래처럼 구성된다.  

```
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-TARGETDURATION:11
#EXT-X-MEDIA-SEQUENCE:0
#EXTINF:10.006333,
index0.ts
#EXTINF:10.000000,
index1.ts
#EXTINF:10.178000,
index2.ts
#EXTINF:3.334000,
index3.ts
#EXT-X-ENDLIST
```

순서 및 실행 가능한 시간을 파일별로 나타낸다.  

### MPEG-DASH (Dynamic Adaptive Streaming over HTTP)

`MPEG(국제표준화단체 Moving Picture Experts Group)` 에서 만든 스트리밍 프로토콜  
HLS 유사하게 미디어 파일을 세그먼트로 나누고 이에 관한 매니페스트 파일을 제공한다.  

다른점은 매니패스트 파일 양식이 XML 형식이란 것  
세그먼트 파일 타입이 어떠한 컨테이너 타입이던지 상관 없다는 것  

#### MSE (Media Source Extensions)

사실 위의 `Adaptive Streaming` 프로토콜은 모든 브라우저가 자체제공하지 않는다.  

> HLS 의 경우 IOS 사파리만 자체 제공  

따라서 `HSL`, `DASH` 의 경우 `MSE` 기능을 사용하는 브라우저에 한하여  
`MSE` 는 `W3C` 에서 표준화 하고 있는 위 프로토콜을 사용할 수 있는 `자바스크립트 API` 이고  
`hls.js, dash.js` 는 MSE 를 쉽게 사용할 수 있도록 구현된 라이브러리이다.  

> <https://developer.mozilla.org/en-US/docs/Web/API/Media_Source_Extensions_API>

`Adaptive Streaming`에서 동영상을 라이브 스트리밍 할 경우 세그먼트 파일을 한번에 로딩한다.  
이는 서버에게 부담으로 느껴질 수 있는데 `hls.js, dash.js` 를 사용하면 현재 보는 시점에서  
세그먼트 파일 2개만 로딩한다던지 프로그래머 커스터마이징 설정이 가능하다.  

`MSE` 를 지원하지 않는 브라우저나 프로그램이라면 `Flash` 나 `Silbverlight` 를 사용해야 한다.  

> ISO Safari 는 MSE 를 지원하지 않기에 퓨어한 HLS 를 사용해야 하는데 커스터마이징이 불가능하기에  
> 한번에 모든 세그먼트를 불러오게 되고 서버 부담으로 돌아온다. 이로 인해 Netflix 의 경우 IOS Safari 에선 서비스를 지원하지 않고 별도의 앱을 통해 동영상 실행을 지원한다.  

### MP4 포맷

브라우저에서 `<video>` 태그를 사용해 서버에 저장된 `MP4` 파일을 호출하면 아래와 같은 정보가 출력된다.  

```
Request URL: http:/localhost:80800/static/video/20200516172837.mp4
Request Method: GET
Status Code: 206 Partial Content
Remote Address: 127.0.0.1:80
Referrer Policy: no-referrer-when-downgrade

Content-Length: 93499890
Content-Range: bytes 25001984-118501873/118501874
Content-Type: video/mp4
Date: Thu, 28 Jan 2021 02:32:21 GMT
ETag: "5ebfa435-71031f2"
Last-Modified: Sat, 16 May 2020 08:28:37 GMT
Server: nginx/1.14.0 (Ubuntu)
```

## WebRTC (Web Real-Time Communication)

> 데모코드: <https://github.com/Kouzie/WebRTC-SS>  
> <https://www.wowza.com/blog/what-is-webrtc>  
> <https://www.wowza.com/blog/webrtc-signaling-servers>  
> <https://developer.mozilla.org/ko/docs/Web/API/WebRTC_API/Signaling_and_video_calling>  

`WebRTC` 는 웹소켓을 통한 프로토콜로 서버를 거치지 않고 클라이언트간 연결을 형성에 P2P 방식도 지원하여  
따라서 delay 가 매우 짧으며 영상을 주고받는데에 좋다.  

그렇다고 서버가 필요 없는 것은 아니다.  
처음 2 개이상의 클라이언트가 연결되기 위해 `시그널링` 이라는 작업을 수행해야 하며  
이때 `시그널링 서버` 가 필요하다.  

![live-stream6](/assets/2021/live-stream5-1.png)  

시그널링 작업을 통해 각 클라이언트는 상대방의 공인IP 와 오픈된 port 를 알 수 있고  
해당 주소로 미디어 데이터를 전송한다.  

`NAT` 나 방화벽 환경에서 클라이언트 끼리 P2P 전송지원은 방화벽 등에 막힐 수 있어 다른방식으로 `WebRTC` 를 운형해야 하는데  

`STUN`, `TURN` 방식을 사용하여 동작할 수 있도록 한다.  

> <https://www.youtube.com/watch?v=ESZ4T2PSMc4&t=47s>

### STUN (Session Traversal Utilities for NAT)  

> 출처: <https://alnova2.tistory.com/1110>  
> STUN: `IETF RFC 5389`에 정의된 네트워크 프로토콜/패킷 포맷으로, 네트워크 환경에 대한 Discovery 를 위한 프로토콜

![live-stream6](/assets/2021/live-stream6.png)  

NAT 안에 설치된 peer 는 본인이 NAT 안에 있는지, 공인네트워크상에 있는지 알지 못한다.  
외부에 있는 시그널링 서버가 각 peer 의 NAT 상의 IP 를 알려주면 사설 네트워크 안에서도 영상을 송/수신 할 수 있다.  
NAT 상의 `IP/Port` 정보 를 알려주는게 STUN 서버이다.  

그림과 같이 P2P 전송 지원을 위해 **STUN 서버를 통해 자신의 IP/Port 정보**를 받는다.  

수신받은 자신의 `IP/port` 정보를 시그널링 서버로 전송하여 다른 peer 가 자신에게 미디어 데이터를 송신 할 수 있도록 한다.  

즉 **STUN 으로부터 전달받은 종단간 NAT 의 Access 가능한 IP/Port** 를 사용하며 미디어 데이터를 주고받는다.

### TURN (Traversal Using Relays around NAT)

> TURN: `IETF RFC 5766`, STUN 서버의 확장, 미디어 데이터를 릴레이하는 서버를 별도로 두어 엄격한 보안정책을 가진 NAT 안에서 동작할 수 있도록 하는 방식

![live-stream7](/assets/2021/live-stream7.png)  

방화벽, 물리적이유 등으로 **Peer 간 직접 미디어 데이터가 송/수신 안될경우 데이터 릴레이를 수행하는 TURN 서버를 사용**해야 한다.  

`TURN 서버`에 접근하여 `TURN 클라이언트`와 P2P 연결이 이루어지는 구조이다.  

### ICE(Interactive Connectivity Establishment)

`STUN`, `TURN` 등으로 찾아낸 **연결 가능한 네트워크 주소들을 Candidate(후보)로 취급**  

사용가능한 클라이언트의 모든 통신 가능한 주소를 식별한 후 가장 최적의 경로를 찾아서 연결하는 방식이다.  

### Group calling architectures in WebRTC

> <https://www.youtube.com/watch?v=d2N0d6CKrbk&t=1s>

1:1 통신이라면 상관없지만 1:N, N:M 통신일 경우 `uplink` 와 `downlink` 의 숫자가 개인당 여러개씩 늘어나게 되는데  

단순 P2P 통신으로만 구현할 경우 클라이언트 부하가 급속도로 늘어나게 된다.  

상황에 맞춰 적절하게 `Mesh`, `MCU(Multi-point Control Unit)`, `SFU(Selective Forwarding Unit)` 방식을 써야 한다.  

![live-stream5](/assets/2021/live-stream5.png)  

각 방식별로 `Uplink` 와 `Downlink` 를 보면 어떤 상황에서 어떤 아키텍처를 사용해야 하는지 판단할 수 있다.  

MCU 의 경우 클라이언트별로 uplink 와 downlink 가 하나씩으로  
업로드된 미디어스트림들을 mixing 하거나 변환하여 1개의 미디어스트림으로 내려주는 구조이다.  
클라이언트의 트래픽 관점에서 효율적이나 서버 리소스를 많이 사용하는 단점이 있다.  

위 3가지 방식은 WebRTC 의 한정되는 내용이 아니다.  
zoom 의 경우 자체 개발 프로토콜로 영상을 전송하고 있으며  
SFU 방식을 사용하여 1000명이 한번에 연결되어 회의 할 수 있도록 지원한다.  

> 대신 클라이언트의 downlink 가 많아져 cpu 부담이 발생하여 데스크탑에서만 사용하기도 한다.  

#### WebRTC Media Server  

Mesh 방식의 경우 완벽한 P2P 통신이기에 별도의 Media Server 가 필요없겠지만  

대규모 사용자를 지원하기위해 MCU 혹은 SFU 방식을 지원해야하는 경우 성능좋은 Media Server 가 필요하다.  

> <https://ourcodeworld.com/articles/read/1212/top-5-best-open-source-webrtc-media-server-projects>

많은 Media Server 가 오픈소스로 제공되어 있으며 그중 `Ant-Media-Server` 를 알아보자.  

> <https://github.com/ant-media/Ant-Media-Server>

## 인터넷 라이브 방송

> <https://medium.com/naver-cloud-platform/인터넷-라이브-방송은-어떤-기술로-만들어질까요-98423dc7fcd4>

1. 원본 영상 → 라이브 인코더(압축) > 영상 송출 (to 미디어 서버)
    촬영한 영상을 라이브 인코더가 `H.264` 혹은 `H.265` 포멧으로 압축  
    영상을 서버로 송출시엔 인터넷 스트리밍에 최적화된 `RTMP` 프로토콜을 사용  
2. 미디어 서버 → 전송 서버(CDN) > 영상 변환 및 전송  
    미디어 서버는 받은 영상을 여러가지 화질 및 비트레이트로 변환(트랜스 코딩)
    최종적으로 HLS 형식으로 변환되게 된다.
3. 동영상 플레이어 → 시청자 > 영상 재생  
    HLS 프로토콜을 사용해 다양한 사용자들에게 TS 파일을 실시간 전송  


카메라가 촬영하는 시간과 그 영상이 시청자에게 표시되는 시간의 차이(`latency`) 가 발생하는데  
시청자와 실시간 소통이 필요할수록 짧은 지연(`low latency`) 이 필요.  

과거에는 10~15초 분량의 TS 청크를 재생 목록에 3개씩 담아 사용하는 것이 일반적(30~45초 분량 지연 발생)
버퍼가 클수록 재생이 안정적이지만 지연 시간은 길어짐  
반대로 버퍼를 짧게 가져가면 지연은 적지만 네트워크 상황에 민감해져 재생 품질이 불안정  

최근에는 네트워크 환경 향상으로 인해 1~2초 정도의 TS 청크를 5개씩 버퍼에 담아 사용(10초 분량 지연 발생)

### 미디어 서버 솔루션

#### Wowza Media Server

유료, 성능 우수

#### Nginx-rtmp

오픈소스, `RTMP` 소스를 `Pull/Push` 방식으로 입력  
모듈을 통해 `RTMP, HLS, MPEG-DASH` 출력을 지원  
`ffmpeg` 모듈 연동시 CDN 을 위한 트랜스 코딩 지원  

#### Nginx-rtmp 설치 및 실행  

> <https://www.youtube.com/watch?v=Js1OlvRNsdI>

위 영상에선 `nginx` 와 `rtmp` 모듈을 설정해 컴파일, 빌드까지 하지만  
우리는 `Dockerfile` 를 통해 `nginx` 와 `rtmp` 가 이미 빌드된 도커 이미지를 만들거나  
아니면 이미 생성된 도커 이미지를 pull 받아서 `nginx-rmpt` 서버를 구축할 예정이다.  

> <https://github.com/Kouzie/nginx-rtmp-docker>  
`HLS, DASH, Put Uploading` 기능을 사용하기 위해 `Dockerfile` 에 `http_dav_module` 과 `nginx.conf` 수정해두었다.  

Dockerfile 빌드 및 실행  

```
$ docker build -t nginx-rtmp ./
$ docker run -d \
-p 1935:1935 -p 8080:8080 \
-v ~/nginx-rtmp/tmp:/tmp -v ~/nginx-rtmp/record:/record \
--name nginx-media-server \
nginx-rtmp:latest
```

공유된 volume 디렉토리에 실시간 영상이 쌓이는지, 녹화영상이 떨어지는지 확인  

```
$ ffmpeg -re -i BigBuckBunny.mp4 -vcodec copy -loop -1 -c:a aac -b:a 160k -ar 44100 -strict -2 -f flv rtp://localhost/live/bbb
...
```

VLC 에서 다음과 같은 주소로 송출되는 영상을 실시간 시청 가능  

```
rtmp://127.0.0.1/live/bbb
http://127.0.0.1:18080/hls/bbb.m3u8
http://127.0.0.1:18080/dash/bbb.mpd
```

`HLS` 와 `DASH` 의 경우 트랜스코딩이 필요함으로 `rtmp` 프로토콜로 바로 접근하는것 보다 `delay` 가 길다.  

`OBS 스튜디오`를 사용하면 `HLS` 를 사용해 `HTTP` 로 스트리밍영상을 업로드하고 클라이언트에서 접근 가능하다.  

![live-stream2](/assets/2021/live-stream2.png)  

### RTSP 서버 

> <https://github.com/aler9/rtsp-simple-server>
> <https://github.com/Kouzie/nginx-rtmp-docker>


```
$ docker run --rm -it -e RTSP_PROTOCOLS=tcp -p 8554:8554 aler9/rtsp-simple-server
...
rtsp://localhost:8554/mystream
```
