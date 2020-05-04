---
title:  "Spring - 프로젝트 - 설문조사 사이트!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - Spring

tags:
  - Spring
  - java
  - project

toc: true

---

## 간단 소개

스프링 프레임워크 수업후 프로젝트 진행, 간단한 설문조사 사이트,

> 소스코드 : https://github.com/Kouzie/SpringSurvey

### DB구조

![image1](/assets/project/survey/image1.png){: .shadow}  

기본적으로 `DELETE ON CASCADE` 속성을 통해 자동 삭제 되도록 구성하였다.  

알림의 경우 댓글, 설문 참여가 일어나면 자동으로 알림이 채워지도록 트리거를 작성 하였다.  

```sql
CREATE OR REPLACE TRIGGER TRI_INSERT_AUTH -- 사용자 추가시 auth에 자동으로 ROLE_USER로 추가되도록 설정
AFTER
    INSERT ON tbl_member
    FOR EACH ROW -- 행트리거 필수 선언
BEGIN
    INSERT INTO tbl_auth(member_seq) VALUES
    (:new.member_seq);
END;

-- 1일경우엔 등록한 설문조사에 댓글이 추가될 경우 tbl_notice에 알림 등록
-- 2일경우엔 등록한 설문조사에 누군가 참여할 경우 tbl_notice에 알림 등록

CREATE OR REPLACE TRIGGER TRI_INSERT_NOTICE_SURVEY_REPLY
AFTER
    INSERT ON tbl_reply
    FOR EACH ROW -- 행트리거 필수 선언
DECLARE    
    receive_member NUMBER;
BEGIN
    IF INSERTING THEN
        SELECT member_seq 
            INTO receive_member
        FROM tbl_survey 
        WHERE survey_seq = :new.survey_seq;
        
        INSERT INTO tbl_notice
        (notice_seq, recieve_member_seq, notice_member_seq, survey_seq, reply_seq, survey_result_seq, notice_message, notice_type, notice_regdate, notice_readdate)
        VALUES
        (seq_notice.nextval, receive_member, :new.member_seq, :new.survey_seq, :new.reply_seq, null, '%s'||'님이'|| '%s'||'설문에 댓글을 남기셨습니다.', 1, sysdate, null);
    END IF;
END;



--tbl_survey의 작성자를 알기위해 투표한 번호의 tbl_survey를 join해서 작성자를 찾는다. (res는 트리거 이름이 30글자 넘어가면 안되서 짜름)

CREATE OR REPLACE TRIGGER TRI_INSERT_NOTICE_SURVEY_RES
AFTER
    INSERT ON tbl_survey_result
    FOR EACH ROW -- 행트리거 필수 선언
DECLARE    
    receive_member NUMBER;
    var_survey_seq NUMBER;
BEGIN
    IF INSERTING THEN
        SELECT tbl_survey.member_seq, tbl_survey.survey_seq  
            INTO receive_member, var_survey_seq
        FROM tbl_survey_item
        JOIN tbl_survey ON tbl_survey.survey_seq = tbl_survey_item.survey_seq
        WHERE survey_item_seq = :new.survey_item_seq;
        
        INSERT INTO tbl_notice
        (notice_seq, recieve_member_seq, notice_member_seq, survey_seq, reply_seq, survey_result_seq, notice_message, notice_type, notice_regdate, notice_readdate)
        VALUES
        (seq_notice.nextval, receive_member, :new.member_seq, var_survey_seq, null, :new.survey_result_seq, '%s'||'님이 '||'%s'||'설문에 참여하였습니다.', 2, sysdate, null);
    END IF;
END;
```

## 적용 기술

스프링 타일즈
스프링 시큐리티(비밀번호 암호화, 접근제어)
이메일을 이용한 비밀번호 변경


## 기본 UI 구성

> https://templatemo.com/live/templatemo_524_product_admin

위 부트스트랩을 사용해 기본적인 UI, 차트를 사용하였습니다.

전체적으로 `header`, `content`, `footer`로 구성되어 있어 3단계로 나누어 타일즈 작업을 하였습니다.  

```html
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
<!-- 각종 javascript, css url -->
</head>
<body id="reportsPage">
	<!-- header -->
	<tiles:insertAttribute name="header" />

	<!-- content -->
	<tiles:insertAttribute name="content" />

	<!-- footer -->
	<tiles:insertAttribute name="footer" />
</body>
</html>
```

### DatePicker

> http://t1m0n.name/air-datepicker/docs/

날짜 선택기를 위한 자바스크립트 사용

## 실행 화면

### 회원가입

![회원가입](/assets/project/survey/회원가입.gif){: .shadow} 

### 설문등록

![회원가입](/assets/project/survey/회원가입.gif){: .shadow} 

### 댓글

![댓글](/assets/project/survey/댓글.gif){: .shadow} 

### 알림

![알림](/assets/project/survey/알림.gif){: .shadow} 

### 이메일로 비밀번호 변경

![이메일전송](/assets/project/survey/이메일전송.gif){: .shadow} 

### 관리자

![관리자](/assets/project/survey/관리자2.gif){: .shadow} 

### 결과출력

![결과](/assets/project/survey/결과.gif){: .shadow} 



## 힘들었던 점

회원가입 후 바로 로그인 처리 후 메인으로 이동...  
스프링 시큐리티 이해없이 진행하였다가 시간만 날리고 다시 공부했다.     
그래도 덕분에 비밀번호 변경/찾기, 각종 로그인 인증했는지 작업은 수월하게 진행하였다.  

각종 예외처리... 값이 입력 안되거나 이상한 값 체크하는 것이 시간도 많이 걸리고 시행착오도 많았다...
~~(사실 아직도 예외처리 안된 부분이 많다... 공백체크 등....)~~

알림 테이블 트리거는 금방 었지만 설문을 삭제하면 설문참여, 댓글 등록 알림도 삭제되어야 했는데 계속 `Trigger Mutating Error`가 발생하였다.  
트리거가 `DELETE CACADE` 를 감지 못하는 것을 알았다....  
결론적으론 DB구조를 바꾸어 해결하였고 바꾸지 않고 진행하려면 복합트리거를 만들어야한다.   
 