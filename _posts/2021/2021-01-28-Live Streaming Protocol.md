---
title:  "Live Streaming Protocol!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - streaming

toc: true
toc_sticky: true

---

# 개요  

## H.264

`VCEG(Video Coding Experts Group)`와 `MPEG(Moving Picture Experts Group)` 에서 만든 동영상 표준 압축 방식(코덱)

`MPEG-4 Part 10, AVC(Advanced Video Coding), H.264` 여러가지 이름으로 불리지만 보편적으로 `H.264` 으로 불림  

> 비트레이트(bitrate): 특정한 시간 단위(이를테면 초 단위)마다 처리하는 **비트의 수**이다

## AAC (Advanced Audio Coding)

오디오 코덱으로 `MPEG-4 Audio` 으로도 불리지만 AAC 가 보편적

## 라이브 스트리밍  

> https://www.youtube.com/watch?v=6t6Si0BWCOo&t=349s
> https://d2.naver.com/helloworld/7122
> https://meetup.toast.com/posts/131
> https://medium.com/naver-cloud-platform/인터넷-라이브-방송은-어떤-기술로-만들어질까요-98423dc7fcd4

라이브 스트리밍이란 텔레비전 생방송처럼 촬영한 정보를 실시간으로 사용자의 동영상 플레이어로 보내 재생하는 것  

온 디맨드 스트리밍이란 저장된 동영상 파일을 사용자의 요구가 있을 때 동영상을 재생하는 것  

라이브 스트리밍은 사용자의 네트워크 상태에 맞춰 영상 해상도를 줄일수 있어야함

## 전송 서버(CDN)

미디어 서버가 전송서버 역할까지 하는 경우가 있지만 시청자 수가 늘어다면 전송담당 서버를 사용해야함  
많은 클라이언트가 요구하는 데이터의 경우 캐시에 담아 지원

# 스트리밍 프로토콜 

## RTP, RTCP, RTSP

> RTP: http://www.ktword.co.kr/abbr_view.php?m_temp1=1381
> RTCP: http://www.ktword.co.kr/abbr_view.php?m_temp1=1805
> RTSP: http://www.ktword.co.kr/abbr_view.php?m_temp1=1798

![live-stream1](/assets/2021/live-stream1.png)  


**RTP (Real-Time Transport Protocol)**  

실제 동영상 전송을 위해 사용되는 프로토콜  
`UDP` 프로토콜로 전송된다.  


**RTCP (Real-Time Transport Control Protocol)**  

`RTP` 와 같이 사용되며 `RTP` 의 전송 통계와 `QoS`를 모니터링  
스트림의 동기화를 지원하기 위해 속도 조절 처리 등을 담당  


**RTSP (Real-Time Streaming Protocol)**  

미디어 플레이어가 사용하는 프로토콜로 응용계층의 프로토콜 `554 PORT` 를 사용한다.  
`RTCP` 와 마찬가지로 `RTP` 데이터의 흐름을 제어  
`PLAY, PAUSE, REPLAY, ROLLBACK` 등의 요청을 처리  


## RTMP(Real Time Messaging Protocol)

`Adobe` 에서 개발 `TCP` 기반 프로토콜로 `1935` 포트사용  
`RTMP` 는 오디오, 비디오 밎 기타 데이터를 인터넷을 라이블 스트리밍할 때 사용  



## Adaptive HTTP Live Streaming

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

따라서 HSL, DASH 의 경우 MSE 기능을 사용하는 브라우저에 한하여 

`MSE` 는 `W3C` 에서 표준화 하고 있는 위 프로토콜을 사용할 수 있는 `자바스크립트 API` 이고  
`hls.js, dash.js` 는 MSE 를 쉽게 사용할 수 있도록 구현된 라이브러리이다.  

> https://developer.mozilla.org/en-US/docs/Web/API/Media_Source_Extensions_API

`Adaptive Streaming`에서 동영상을 라이브 스트리밍 할 경우 세그먼트 파일을 한번에 로딩한다.  
이는 서버에게 부담으로 느껴질 수 있는데 `hls.js, dash.js` 를 사용하면 현재 보는 시점에서  
세그먼트 파일 2개만 로딩한다던지 프로그래머 커스터마이징 설정이 가능하다. 

`MSE` 를 지원하지 않는 브라우저나 프로그램이라면 `Flash` 나 `Silbverlight` 를 사용해야 한다.  

> ISO Safari 는 MSE 를 지원하지 않기에 퓨어한 HLS 를 사용해야 하는데 커스터마이징이 불가능하기에 
한번에 모든 세그먼트를 불러오게 되고 서버 부담으로 돌아온다. 이로 인해 Netflix 의 경우 IOS Safari 에선 서비스를 지원하지 않고  
별도의 앱을 통해 동영상 실행을 지원한다.  

### MP4

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

## 멀티비트 레이트  

화질선택 기능을 지원하기 위한 기술  

동영상을 화질별로 세그먼트, 매니페스트로 나누어 클라이언트에게 선택 재생 할 수 있도록 지원한다.  


# 인터넷 라이브 방송

> https://medium.com/naver-cloud-platform/인터넷-라이브-방송은-어떤-기술로-만들어질까요-98423dc7fcd4

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

## 미디어 서버 솔루션

### Wowza Media Server

유료, 성능 우수 

### Nginx-rtmp

오픈소스, `RTMP` 소스를 `Pull/Push` 방식으로 입력, 
모듈을 통해 `RTMP, HLS, MPEG-DASH` 출력을 지원  
`ffmpeg` 모듈 연동시 CDN 을 위한 트랜스 코딩 지원  

### Nginx-rtmp 설치 및 실행  

> https://www.youtube.com/watch?v=Js1OlvRNsdI

위 영상에선 `nginx` 와 `rtmp` 모듈을 설정해 컴파일, 빌드까지 하지만  
우리는 `Dockerfile` 를 통해 `nginx` 와 `rtmp` 가 이미 빌드된 도커 이미지를 만들거나  
아니면 이미 생성된 도커 이미지를 pull 받아서 `nginx-rmpt` 서버를 구축할 예정이다.  

> https://github.com/Kouzie/nginx-rtmp-docker
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

## RTSP 서버 

> https://github.com/aler9/rtsp-simple-server

```
$ docker run --rm -it -e RTSP_PROTOCOLS=tcp -p 8554:8554 aler9/rtsp-simple-server
```

```
rtsp://localhost:8554/mystream
```


## WebRTC (Web Real-Time Communication)

> https://www.wowza.com/blog/what-is-webrtc
> https://developer.mozilla.org/ko/docs/Web/API/WebRTC_API/Signaling_and_video_calling  

WebRTC 는 웹소켓을 통한 프로토콜로 서버를 거치지 않고  
클라이언트간 연결을 형성에 통신한다.  

따라서 delay 가 매우 짧으며 영상을 주고받는데에 좋다.  

### sample code 

> https://github.com/Kouzie/WebRTC-SS