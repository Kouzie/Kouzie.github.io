---
title:  "Elastic Search - 개요!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - elasticsearch
---

## 개요

`Doug Cutting` 이 고안한 **역색인(Inverted Index)** 구조의 문서형태의 데이터 베이스 + 검색(분석)도구  

빠른 검색 결과를 제공하고 최적의 집계성능을 제공한다  

**Lucene**을 기반으로 분산처리가 가능하도록 고안된 **Solr**가 등장하고 그 다음 **Elastic Search** 가 등장했다.  

`Request Body` 방식의 `QueryDSL`(json 형태) 을 새롭게 도입해 상세한 쿼리를 쉽게 구성할 수 있다.  
검색서비스는 크게 아래 3가지로 구분할 수 있다.  

**검색 서비스 > 검색 시스템 > 검색엔진**  

**검색엔진(search engine)**  
수많은 데이터에서 정보를 수집해 검색 결과를 제공하는 프로그램

**검색 시스템(search system)**  
대용량 데이터를 기반으로 신뢰성 있는 검색 결과를 제공하기 위해 검색엔진을 기반으로 구축된 시스템을 통칭  
특정 필드나 문서에 가중치를 두거나 내부 정책에 따라 관련도를 계산하여 검색 점수로 계산하고 상위에 배치할 수 있다.  

**검색 서비스(search service)**  
검색 시스템을 서비스화 한 것  

Elastic Search 는 검색엔진 + 검색 시스템 + NoSQL(DB) 기능을 수행한다.  
기본적으로는 검색엔진이지만 MongoDB 나 Hbase 처럼 대용량 스토리지로도 활용할 수 있다.  

### 검색 시스템의 구성 요소

아래 4가지로 구성

**수집기**  
크롤러, 웹 로봇 등  

**스토리지**  
데이터베이스에서 데이터를 저장하는 물리적인 저장소다

**색인기**  
수집된 데이터를 검색 가능한 구 조로 가공하고 저장해야 한다. 그 역할을 하는 것이 색인기다.
형태소 분석기를 조합해 정보에서 의미가 있는 용어를 추출하고 검색에 유리한 역색인 구조로 데이터를 저장한다.

**검색기**  
검색기는 사용자 질의를 입력받아 색인기에서 저장한 역색인 구조에서 일치하는 문서를 찾아 결과로 반환한다.  
질의와 문서가 일치하는지는 유사도 기반의 검색 순위 알고리즘으로 판단한다

### Elastic 핵심 기능  

- 전문검색(Full Text)  
- 통계분석(Kibana)  
- 스키마리스  
- RESTful API  
- Multi-tenancy  
- Document-Oriented(계층 구조 문서)  
- 역색인(Inverted Index)  
- 확장,가용성(샤드 기반 분산환경)  

익숙하지 않은 몇개만 설명하자면  

**전문검색**  
RDB 와 다르게 전문검색(Full Text) 가능, 단순 지정 필드에 대해서만 일치하는지 검색하는 것이 아니라 문서 전체에서 분석기에 의한 검색을 수핸한다.  
이런 작업을 수행하기 위해 문서 저장시 색인과정을 거친다.  
다양한 기능별, 언어별 플러그인을 조합해 전문검색이 가능하다.  

**Multi-tenancy**  
tenancy는 차용권이란 뜻으로 Multi-tenancy는 다른 여러 사용자 그룹에 서비스를 제공할 수 있는 소프트웨어 아키텍처를 뜻한다.  
클라우드 컴퓨팅에서는 서로 다른 고객이 서버 리소스를 나누어 사용하는 공유 호스팅을 멀티테넌시라고 부르기도 한다.  

![es1](/assets/elastic-search/es1.png)  

**역색인**  
Lucene 기반의 검색엔진 기능을 수행하기 위해 문서의 역색인 기능을 지원한다.  
일반적인 NoSQL(MongoDB, CasandraDB) 은 역색인을 지원하지 않음  

### 단점  

- 준실시간(Near Realtime)  
  색인 된 데이터는 내부적으로 커밋과 플러시 같은 복잡한 과정을 거치기 때문에 실시간이 아니다  
- 트랜잭션과 롤백 기능을 제공하지 않는다  
- 데이터 손실의 위험이 있다  
- 데이터의 업데이트를 제공하지 않는다, 기존 문서를 삭제하고 다시 생성하는 방식을 사용, 대신 Immutable 이라는 이점을 취할 수 있다  

## 데이터 구조  

![es2](/assets/elastic-search/es2.png)  

RDB 의 데이터 구조와 비교하면 아래와 같다.  

| 엘라스틱서치 | RDB       |
| ------------ | --------- |
| Index        | Database  |
| Shard        | Partition |
| Type         | Table     |
| Document     | Row       |
| Field        | Column    |
| Mapping      | Schema    |
| Query(DSL)   | SQL       |

> **매핑**: 필드의 구조와 제약조건에 대한 명세  

### 인덱스  

**데이터의 저장공간**으로 비슷한 문서의 모음을 뜻한다

인덱스 내부에 색인된 데이터는 물리적인 공간에 여러개 파티션으로 나뉘어 구성되는데,  
이 파티션을 Shard 라고 부른다.  

### 타입

인덱스의 논리적구조를 의미하며 RDB 로 비유하면 테이블 명세서이다  

`6.0` 버전 이하에서는 하나의 인덱스에 여러 타입을 설정 가능했지만  
`6.1` 버전부터는 인덱스와 타입은 1:1 매핑이다  

`6.0` 이하 버전에서는 music 이라는 인덱스가 존재한다면 장르별(Rock , K-pop , Classic)로 분리해 타입을 지정해서 자주 사용했지만  
하지만 현재는 장르별로 별도의 인덱스를 각각 생성해서 사용해야 한다.  

### 문서

RDB 로 예를들면 테이블의 한 행(Row) 를 의미한다.  
Elastic Search 에서 데이터가 저장되는 최소 단윈로 기본적으로 json 포멧을 사용한다.  

하나의 문서는 다수의 필드로 구성된다. 문서는 중첩 구조를 지원하기 때문에 이를 이용해 문서 안에 문서를 지정하는 것도 가능하다.  

### 필드  

필드는 문서를 구성하기 위한 속성으로 `Data Type` 을 정의해야 한다.  
문자열, 정수, 부울 등 기본타입부터 배열, 객체형태의 타입을 가질 수 도 있다.  

필드를 정의하고 `Data Type` 지정하여 색인 방법을 정의하는 과정을 매핑이라 한다.  

> `Data Type` 이 `text` 이면 **분석기를 통해** 색인처리를 하고  
> `keyword` 라면 단순 비교만 하기 때문에 단순 문자열만 색인처리된다.  

매핑에는 여러 데이터 타입을 지정할 수 있지만 필드명은 중복 불가능하다.  

## 클러스터 구조  

클러스터는 **노드 인스턴스** 들의 모임이다.  

기본적으로 **마스터 노드**가 전체적인 클러스터를 관리하고 **데이터 노드**가 실제 데이터를 관리한다.  

아래 4가지 유형의 노드가 있음  

- **마스터 노드**  
  노드 추가와 제거 같은 클러스터의 전반적인 관리  
- **데이터 노드**  
  실질적인 데이터를 저장, 검색과 통계 같은 데이터 관련 작업을 수행  
- **코디네이팅 노드**  
  사용자의 요청만 받아서 처리, 클러스터 관련 요청은 마스터 노드에 전달하고 데이터 관련 요청은 데이터 노드에 전달  
- **인제스트 노드**  
  문서의 전처리 작업을 담당  
