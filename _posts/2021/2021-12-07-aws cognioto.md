---
title:  "aws Cognito!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - aws

toc: true
toc_sticky: true

---

## 개요 - Cognito(Cognito: 안면이 있는, 알려진)

> https://www.youtube.com/watch?v=OCt71JaL-vQ  
https://www.youtube.com/watch?v=nazoivmrDfk  

사용자의 프로필정보와 사용하는 데이터를 연계하여  
접속기기 상관없이 연동할수 있는 백앤드 솔루션을 만드는것은 매우 복잡하다(인프라 구축 + 개발).  

`AWS Cognito` 는 사용자 자격증명 및 데이터 동기화를 위한 인증 인가:[`Authentication and Authorization` (이하 `AuthN` & `AuthR`)] 매니지드 서비스로  
모바일앱 혹은 웹앱에서 사용자의 회원가입, 로그인, 사용자 프로필 정보를 쉽게 관리할 수 있다.  

사용자의 `AuthN` 이 필요하다면 서드파티제공자(구글, 페이스북 등) `Cognioto User Pool` 을 통해 기능 제공한다.

`Cognito` 초창기, S3 에 데이터를 저장하라면 아래 그림처럼 구성해야 했다.  

![ddd1](/assets/2021/aws19.png)   

최초에는 `Cognioto Identity Provider(IDP)` 기능만을 제공하여 `Cognioto Identity Provider(IDP)` 에서 인증 및 `OpenID Token(AuthN)` 을 받아 직접 `AWS Security Token Service(AWS STS)` 에 `AuthR`에 해당하는 임시자격 토큰 부여받아 S3 에 접근했다.  

이런 기능으로 인해 모바일 앱같은 경우에서는 유저 데이터베이스로 인해 더이상 백앤드를 구축하지 않아도 된다. 

시간이 지나면서 Cognito에 `User Pools, Federation Identities` 라는 유저 데이터베이스, 사용자 통합 인증 시스템이 구축되면서 더욱 편하게 변경되었다.  

## User Pools & Federation Identities(통합ID)

![ddd1](/assets/2021/aws24.png)   

이제는 `AWS STS` 를 사용하지 않고(사실 `Cognito` 뒤 에 `STS` 가 포함된것이라 보면 된다)  
`User Pools & Federation Identities` 를 사용해 `AuthN`, `AuthR(Role from STS)` 을 제공한다.  

`Cognito` 에서는 `AuthN`, `AuthR` 데이터를 토큰형식으로 관리하며 종류는 아래와 같다.  

**토큰 종류**  

`ID Token`: `AuthN` 에 해당하는 사용자의 신원정보를 포함한 JWT 형태 토큰, 유효기간은 1시간  
`Access Token`: `AuthR` 에 해당하는 사용자의 권한내용이 정의된 JWT 형태 토큰, 유효기간은 1시간  
`Refresh Token`: 기존 `Id Token` 혹은 `Access Token` 이 만료되었을 때 새로운 토큰을 부여받는데 사용하는 문자열 형태 토큰, 유효기간은 `1~3650` 사이의 값으로 설정 가능.  

`Cognito` 에서 각종 토큰정보를 가져오는 과정은 아래 그림과 같다.  

![ddd1](/assets/2021/aws20.png)  
 
먼저 `Login Provider`인 서드파티 제공자 혹은 `User Pools` 를 통해 `AuthN` 데이터를 가져오고

`Federation Identities` 에서 각종 서드파티 제공자(구글, 트위터) 혹은 `User Pools` 에서 제공되는 `AuthN` 을 기반으로 **통합된 인증데이터(통합ID)** 를 제공한다.  

`Federation Identities` 는 전달했던 최종적으로 **통합된 인증데이터** 를 통해 `STS` 로부터 임시권한 `AuthR(Role from STS)` 까지 발급받아 토큰형형식으로 사용자에게 전달한다.  


### 추가 기능 및 설명  

**Lambda Trigger**  
`SignIn`, `SingOut` 등등 사용자의 이벤트성 작업에 후킹되어 호출되는 `Lambda` 함수를 지정할 수 있다.  

**User 상태**  
유저를 `SignIn` 하거나 `delete` 하기 위한 유저상태가 별도로 존재하며 항상 해당 상태를 확인하여 User Pools 를 운영해야 한다.  

`Registered`: `SingUp` 시 적용되는 유저상태  
`Confirmed`: `SingUp` 이후 관리자 컨펌한 유저상태,  `Confirmed` 되어야 `SignIn` 가능하다  
`Disabled`: 사용자 삭제되려면 되어야 하는 유저상태  

## API Gateway 와 Cognito 연동

아래 2가지 개념을 통해 `API Gateway` 에서 제공하는 API 에 `Allow/Deny` 를 적용한다.  

**Custom AuthR**  
다른 서비스의 연계를 위해 사용자 인증 사이에 람다함수가 껴있는 형태.  
사용자 인증은 여전히 `User Pools` 를 통해 하지만 반환값으로 추가데이터를 타 서비스로부터 가져와 같이 전달한다던지 등을 위해 사용하는 기능  
`OAuth`, `SAML` 등의 연동을 프로그래밍틱하게 사용하거나 이메일 인증을 통해 자동으로 Confirm 하든 등의 방식을 사용할때 이용한다.  

**Native Support** 
제공되는 `User Pools` 에 인증정보를 묻고 바로 인증/인가 되어 API 를 사용하는 방식  

위의 기능을 사용해 아래와 같이 3가지 방식으로 `AuthN`, `AuthR` 시스템을 구축할 수 있다.  

![ddd1](/assets/2021/aws21.png)  
`Cognioto User Pools` 로부터 인증 및 토큰을 부여받고 해당 토큰을 통해 `API Gateway` 에게 Request 하는 방식  

`API Gateway`는 **바로 Response 하지 않고** 확실한 인증/인가를 확인하기 위해 다시한번 `Cognioto User Pools` 로부터 해당 토큰을 검색하여 확인후 응답한다.  

![ddd1](/assets/2021/aws22.png)   
위에서 말한 `Custom AuthR` 기능이 껴있는 방식 

![ddd1](/assets/2021/aws23.png)  
가장 진보된 방식으로 인증토큰방식이 아닌 인증/인가가 모두 완료된 `Header` 정보를 통해 `API` 를 호출,  
`User Pools` 를 통해 `AuthR` 하고 `Federation Identities` 로 `AuthN` 까지 한 후 `SigV4` 해더를 생성해서 `API Gateway` 와 통신하는 방식이다.  

`API Gateway` 뒤의 `Resource(S3, EC2 Webserver, Lambda)` 을 접근은 생략되었으며 추가될 경우 `API Gateway` 과 `Resource` 가 연결될 수 있도록 `IAM Policy` 가 추가된다 보면 된다.

