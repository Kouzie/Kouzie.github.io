---
title:  "aws IoT!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - aws

toc: true
toc_sticky: true

---

## AWS MQTT

> https://www.youtube.com/watch?v=2abxd41tDlI
> https://www.youtube.com/watch?v=u0mMgTEPrQo
> https://www.youtube.com/watch?v=HTm2nORrkSw

![ddd1](/assets/2021/aws25.png)   

> https://docs.aws.amazon.com/ko_kr/iot/latest/developerguide/what-is-aws-iot.html

### 디바이스 게이트웨이  

MQTT, HTTP 를 통해 Thing 과의 통신 지원,
AWS 에선 Thing 에서 게이트웨이와 쉽게 연결하기 위해 `Device SDK` 라이브러리를 사용한다.  

`Device SDK` 를 사용하면 인증서 및 암호화까지 모두 지원하기에 AWS 에서 사용 필수  

풀 매니지드 서비스로 Thing 이 늘어난다고 별도의 확장관리를 할 필요가 없다.  


## IoT 정책

특정 토픽, 특정 기기에 대해서(`Resource`) 아래 `Action` 들을 수행할 수 있도록 `IAM Policy` 구성이 가능하다.  

`iot:Publish`: MQTT publish
`iot:Subscribe`: MQTT subscribe
`iot:UpdateThingShadow`: 쉐도우 수정
`iot:GetThingShadow`: 쉐도우 조회
`iot:DeleteThingShadow`: 쉐도우 삭제

Thing 에서는 인증서를 사용하기때문에 아래처럼 정책변수 `${iot:ClientId}` 를 사용하면 좋다.  

```json
{
    "Effect": "Allow", 
    "Action": "*",
    "Resource": "arn:aws:iot:us-east-1:....:client/${iot:ClientId}",
}
```

하나의 `Thing` 등록하려면 `AWS IoT Core` 에 `Thing` 에 해당하는 Metadata 를 등록해두어야 한다.  
 
`Thing(Metadata), Certificate(인증서), Policies(정책)`

![ddd1](/assets/2021/aws27.png)   

`aws cli` 로 표시하면 아래 `1 ~ 5` 과정과 같다.  
```
# 1. Thing 생성
$ aws iot create-thing --thing-name single-device --attribute-payload {
    "attribute": {
        "model_number": "M5711"
    }
}

# 2. Certificate 생성  
$ aws iot create-keys-and-certificate
# 출력된 인증서 Arn CERTIFICATE_ARN 변수에 삽입


# 3. Policy 생성
$ aws iot create-policy --policy-name all-pubsub --policy-document {
    "Version": "2012-10-17,
    "Statement": [{
        "Effect": "Allow",
        "Action": "iot:Publish",
        "Resource": "arn:aws:iot:us-east-1:my-topic"
    }, {
        "Effect": "Allow",
        "Action": "iot:Subscribe",
        "Resource": "arn:aws:iot:us-east-1:my-topic"
    }]
}

# 4. Policy - Certificate 서로 attach
$ aws iot attatch-policy --policy-name all-pubsub --target $CERTIFICATE_ARN 

# 5. Thing - Certificate 서로 attach
$ aws iot attatch-thing-principal --thing-name single-device --principal $CERTIFICATE_ARN 
```


### 사용자 지정 인증 방안  

`X.509` 인증서를 사용해 `IoT Gateway` 와 인증된 연결을 구성,
하나의 인증서당 하나의 `Thing`, 혹은 `Group`, 혹은 수만대에 적용하여 인증하는 방식

`Websocket` 으로 연결하여 `AWS SigV4` 방식을 사용해 인증된 연결을 구성하고 메세지를 전달하는 방식  

위 2가지 방식이 AWS 에서 제공하는 인증서를 활용한 인증방식이다.

**사용자 지정 인증 방식**은 자체적인 인증 솔루션을 사용하여 `IoT Gateway` 와 인증된 연결을 구성하는 방식이다,  

아래 그림처럼 `Key/Token` 방식으로 람다함수와 같이 사용하여 외부 `EC2(Instance)` 나 서버에 전달하여 인증받는 방식이다.  

![ddd1](/assets/2021/aws26.png)   


### X.509 인증서 사용방안

`AWS IoT`의 `루트CA` 혹은 사설인증기관의 `루트CA` 에 등록된 인증서를 사용하여 
등록된 `루트CA` 인증서를 사용해 대량의 `Device 인증서` 생산 및 Device 에 적용하는 방식이다.  

대량의 `Device 인증서` 를 일괄로 `AWS IoT` `CA` 에 등록(`배치 플릿 프로비저닝`) 할 수 있다.     

### 배치 플릿 프로비저닝(Bulk Register Many AWS IoT Things)


위의 과정이 기기가 수만대 이상 늘어나면 사람이 일일이 적용하는것은 불가능하기에  
`aws cli` 을 이용해 쉘 프로그래밍을 작성해야 했는데

이를 좀더 쉽게 적용할 수 있도록 만들어진 기능이 `배치 플릿 프로비저닝`  

`배치 플릿 프로비저닝` 은 위에서 설명한 `aws cli` `1~5` 단계 기능을 모두 합쳐서 배치형식으로 대량 동작시키는것  

json 문서형식으로 위 단계를 정리하는데 `배치 플릿 프로비저닝 템플릿` 이라 한다.  

```json
{ 
    "Resources" : {
        "thing" : {
            "Type" : "AWS::IoT::Thing",
            "Properties" : {
                "ThingName" : {"Ref" : "ThingName"},
                "AttributePayload" : { "version" : "v1", "serialNumber" :  {"Ref" : "SerialNumber"}}, 
                "ThingTypeName" :  "lightBulb-versionA",
                "ThingGroups" : ["v1-lightbulbs", {"Ref" : "Location"}]
            },
            "OverrideSettings" : {
                "AttributePayload" : "MERGE",
                "ThingTypeName" : "REPLACE",
                "ThingGroups" : "DO_NOTHING"
            }
        },  
        "certificate" : {
            "Type" : "AWS::IoT::Certificate",
            "Properties" : {
                "CertificateSigningRequest": {"Ref" : "CSR"},
                "Status" : "ACTIVE"      
            }
        },
        "policy" : {
            "Type" : "AWS::IoT::Policy",
            "Properties" : {
                "PolicyDocument" : "{ \"Version\": \"2012-10-17\", \"Statement\": [{ \"Effect\": \"Allow\", \"Action\":[\"iot:Publish\"], \"Resource\": [\"arn:aws:iot:us-east-1:123456789012:topic/foo/bar\"] }] }"
            }
        }
    }
}
```

위처럼 메타데이터 [`Thing(Metadata), Certificate(인증서), Policies(정책)`] 를 한번에 등록하고 서로 `Attach` 해준다.  

가변데이터 설정만 잘 해주면 쉽게 대량으로 Thing 에 대한 메타데이터 설정이 가능하다.  


## IoT Rule

특정 토픽에 대해 메세지를 필터링, 변환, Lambda 실행, DB에 저장 등을 진행할 수 있고  
토픽을 변경하거나 하나의 메세지에서 여러가지 이벤트를 만들어 내는 등을 `IoT Rule Engine` 으로 정의 가능하다.  

단순하게 MQTT 메세지의 페이로드를 그대로 백앤드 어플리케이션에 전달할 수 있지만
아래와 같이 `Rule Query Statement` 를 적용해 `clientId` 를 같이 넘겨준다던지, 특정 데이터를 변조해서 보낸다던지 가능하다.  

```sql
SELECT clientid() as ClientID, topic() as Topic, * FROM 'topic/#'
```

`clientid()` 외에도 `get_thing_shadow(thingName, roleARN)` 함수를 사용해디바이스 Shadow 문서 자체를 가져올 수 있다.   

또한 AWS Iot 에서 브로커와의 `연결/해제`, 토픽 `구독/취소` 이벤트에 대해 모두 MQTT 메세지로 받아볼 수 있다.  


### 디바이스 이벤트  

기기가 `AWS Iot Core` 에 연결되어 발생시킬수 있는 `Event` 종류는 아래 3가지 

- `Job Event`  
- `Thing Type`  
- `Thing Group`  

과거에는 해당 이벤트에서 로그가 발생하면 CloudWatch 에서 따로 이벤트 MQTT 를 발행하는 구조였는데  
지금은 해당 이벤트에 대한 Topic 만 구독하고 있으면 된다.  



## 디바이스 그룹화

`디바이스 Shadow`, `디바이스 Registry` 문서들을 활용해서 Ting 을 그룹화 하여 정책설정 및 관리가 가능하다. 

기존에는 생성된 인증서를 기반은로 IoT 정책을 생성하고 그룹화 하였지만 계층화별로 그룹화 하기 어려운면이 있었다.  

이제는 그룹을 생성하고 계층화하기 편하도록 그룹화 정의문서로 `디바이스 Shadow` 와 `디바이스 Registry` 를 사용하여 IoT 정책을 선정할 수 있다.  

또한 빠른 검색기능을 위해 `fleet indexing` 기능을 제공한다.  

indexing 처리를 하려면 아래 명령어를 실행  

```
$ aws iot update-indexing-configuration --thing-indexing-configuration thingindexingMode=REGISTRY_AND_SHADOW
```

> `REGISTRY_AND_SHADOW` 외에도 `REGSITRY`, `OFF` 파라미터를 적용해 `디바이스 Registry` 만 설정하거나 기능을 제거할 수 있다.  

### 디바이스 Shadow

불안정한 인터넷 상태에 따라 메세지 순서가 변경되거나 기기 네트워크가 끊겨 전달되지 않을 수 있다.  

보통 IoT 상태를 관리할때 Control 시그널을 통해 상태를 변경하는데 인터넷 연결이 불안정해 `Thing`의 상태가 변경되었는지 확신할 수 없을 경우가 있다.  
이때 사용하는것이 디바이스 Shadow이다.  

기기 상태를 동기화할 수 있는 중간자 역할을 한다.  

`Desired` : 사용자가 원하는 상태
`Reported` : 현재 Thing의 상태 
`Delta` : `Desired` 와 `Reported` 가 서로 다른 상태 

다비이스 쉐도우는 아래와 같은 `Document` 형태이다.  

```json
{
    "state": {
        "desired": {
            "lights": { "color": "RED" },
            "engine": "ON"
        },
        "reported": {
            "lights": { "color": "GREEN" },
            "engine": "ON"
        },
        "delta": {
            "lights": { "color": "RED" },
        }
    },
    "version": 1
}
```

쉐도우 문서에는 항상 `version` 이 존재하며 수정될때마다 증가한다.  
문서 수정시 `version` 을 명시하지 않아도 되지만 명시할경우 버전에 맞춰 업데이트되도록 `ordering` 가능하다.  

`delta` 에 대한 토픽을 구독함으로써 `Desired` 와 `Reported` 가 서로 다른 상태임을 확인 가능하고 `Thing` 컨트롤을 수월하게 진행 가능  

`$aws/things/{thing-name}/shadow/update`
```json
{
    "state": {
        "desired": {...}
    }
}
```

위와 같은 토픽과 쉐도우 문서 를 전송하면 된다.  

아래와 같은 쉐도우와 관련된 대표적인 토픽이 존재한다.  

UPDATE: `$aws/things/{thing-name}/shadow/update`
GET: `$aws/things/{thing-name}/shadow/get`
DELETE: `$aws/things/{thing-name}/shadow/delete` - 쉐도우 Document 초기화
DELTA: `$aws/things/{thing-name}/shadow/delta` - Subscription 용
DOCUMENTS: `$aws/things/{thing-name}/shadow/update/documents` - 

### 디바이스 Registry  

`디바이스 Shadow`가 가변적인 정보를 표기한다면  
`디바이스 Registry` 는 불변하는 속성들을 관리하는 문서이다.  

`디바이스 Registry` 의 종류로 아래 3가지가 존재한다.  

- `Thing`  
- `Thing Type`  
- `Thing Group`  

`Thing`, `Thing Type` 은 자체 `Attribute` 를 가지며 

### fleet indexing 검색식  

위에서 `fleet indexing` 기능을 설정했다면 `aws iot search-index` 기능과 `디바이스 Registry`, `디바이스 Shadow` 를 사용해 특정 사물을 검색할 수 있다.  

**`디바이스 Registry` - 전체목록 검색**  

```
$ aws iot search-index --index-name "AWS-Things" --query-string "thingName:*"
```

**`디바이스 Shadow` - 특정 속성 검색**  
`Thing` 중 `thingTypeName` 이 `HDL500` 이고 `shadow.reported` 의 `status` 가 `uninitialized` 인것만 검색  

```
$ aws iot search-index --index-name "AWS-Things" --query-string "thingTypeName:HDL500 AND shadow.reported.status:uninitialized"
```

### 로깅 그룹화  

수만대의 MQTT 로그들을 검색 및 디버깅 하는것은 쉽지 않기에 AWS IoT 에서 로깅 검색식 또한 제공한다.  

Global 로그 - 모든 로그가 아닌 Error 에 해당하는 로그만 출력
Things Group 로그 - 특정 속성을 사용해 해당 Group 만 출력되는 상세한 로그 출력  

Cloud Watch 를 통해 IoT 로그들을 관리할 수 있다.  


## 기타 

### AWS Greengrass

수천대의 센서정보가 모두 인터넷연결되는 것은 불가능하기 때문에 게이트웨이 역할을 하는 노드를 지정하고 해당 노드에서 
Local Broker, Local Lambda, Local Device Shaow 와 같은 기능을 제공한다.  

반드시 Aws IoT 서비스와 연결될 필요 없기 때문에 가격적으로, 네트워크적으로 이득이 발생한다.  

노드에게 배포될 Greengrass 를 작성하고 노드에서 각각 알아서 동작할 수 있도록 한다.  


### FreeRTOS

대부분 대형 밴더사에서 제공하는 MPU 에서 돌아가는 Realtime OS  
로컬의 Greengrass 와 연동하여 주로 리프노드에 해당하는 저전력PC 에서 FreeRTOS 를 사용한다.  

### Over the air update  

개발장치 특정 타입, 특정 대상, 특정 지역에 대한 업데이트 기능을 OTA 로 제공한다.  

AWSA 에서는 업데이트 기능을 `Job` 이라는 문서형식으로 관리하고
아래 4가지 기능을 제공한다.  

- 전파속도 
- 우선순위 
- 상태확인 
- 이벤트

![ddd1](/assets/2021/aws28.png)   

0. `Thing` 이 `Job` 이 등록되면 대응하도록 토픽 subscribe  
1. `Job`을 생성하고 이미지를 `S3` 에 저장한다.  
2.  `Job Event` 가 발생함으로써 `Job` 을 검색하는 함수 호출
3.  `AWS Iot` 로부터 `Job` 에 대한 상세정보 검색  
4.  `S3` 로부터 이미지 다운로드  
5.  업데이트 진행  

`Job` 은 종류는 크게 2개로 나뉜다.  

**Continuous**  
한번 생성되면 삭제되지 않는다. 기기가 추가될 때 마다 지속적으로 `Job Event` 를 발행한다.  

**Snapshot**  
한번 특정 기기들을 대상으로 업데이트를 진행한다.  
