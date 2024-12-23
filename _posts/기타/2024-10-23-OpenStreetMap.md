---
title:  "OpenStreetMap!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---

## OpenStreetMap  

> <https://osm.kr/about/>
> 
> wiki: OpenStreetMap(이하 OSM) 은 2005년 설립된 영국의 비영리기구 오픈스트리트맵 재단이 운영하는 오픈 소스 방식의 참여형 무료 지도 서비스이다.  
> 네이버나 카카오같은 단순 타일맵 서비스가 아닌 지리공간 오픈 데이터베이스로써 참여자의 수정요청이 받아들여 진다.  

<http://download.geofabrik.de/> 링크에서 각국, 세계지도 다운로드가 가능하다.  

다운로드 받은 지도 데이터를 기반으로 서버를 구성하려면 아래 github 에서 docker 기반으로 쉽게 구동시킬 수 있다.  

> <https://github.com/Overv/openstreetmap-tile-server>

### OpoenMapTiles  

> <https://openmaptiles.org/>
> <https://wiki.openstreetmap.org/wiki/Tile_servers>  

위 docker-compose 에선 컨테이너 실행 한번으로 `WMS(Web Map Service)` 를 제공하지만 내부적으론 여러가지 과정을 거친다.  
`OSM` 데이터를 `rendering` 된 `Rester Files` 로 제공하려면 데이터를 변환하고 `vector tile` 형태로 제공하는 서버를 설치해야 한다.  

```
OSM --(import)--> PostGIS --(encode)--> MVT(Mapbox Vector Tile) --(rendering)--> Raster Tiles(PNG)
```

### osm2pgsql

osm.pbf(OpenStreetMap 데이터를 저장하는 Protocolbuffer Binary Format) 데이터를 PostgreSQL 에 저장하기 위한 툴로 `osm2pgsql` 이 주로 사용된다.  

```sh
sudo -u renderer osm2pgsql -d gis \
  --create --slim -G --hstore --number-processes 4 \
  --tag-transform-script /data/style/openstreetmap-carto.lua \
  -S /data/style/openstreetmap-carto.style \
  /data/region.osm.pbf
# osm2pgsql version 1.6.0
# Database version: 15.3 (Ubuntu 15.3-1.pgdg22.04+1)
# PostGIS version: 3.3
# Setting up table 'planet_osm_point'
# Setting up table 'planet_osm_line'
# Setting up table 'planet_osm_polygon'
# Setting up table 'planet_osm_roads'
```

- `sudo -u renderer`
  - 명령을 renderer 사용자로 실행합니다
- `-d gis`
  - `PostgreSQL` 에 `gis` 데이터베이스 생성, 그리고 아래 extention 설치  
- `--create --slim`
  - 새로운 테이블을 생성, 임시 데이터를 디스크에 저장하여 메모리 사용량 줄임(slim)  
- `-G`  
  - 지오메트리 클린업(Geometry Cleanup), 
  - 정확하지 않은 지오메트리(자가 교차(Self-intersecting) 다각형, 닫히지 않은 다각형 등)을 감지 및 수정.  
- `--hstore`
  - `key, value` 타입 지원
  - `CREATE EXTENSION IF NOT EXISTS hstore CASCADE`  
- `--number-processes 4`  
  - CPU 코어 수 지정
- `--tag-transform-script /data/style/openstreetmap-carto.lua`  
  - openstreetmap-carto 스타일에 맞게 데이터 변환
- `-S /data/style/openstreetmap-carto.style`
  - `openstreetmap-carto` 스타일로 렌더링하기 위해 필요한 데이터를 import.  

위처럼 4개 데이터가 생성됨 간단히 아래와 같은 정보를 포함한다.  

- `planet_osm_point` - 지하철역, 쇼핑 센터, 대학, 등
- `planet_osm_line` - 철도, 지하철 및 기타 선형 정보
- `planet_osm_polygon` - 테이블에는 공원, 수역 및 특정 도시 지역의 건물 등
- `planet_osm_roads` - 도로 정보

> <https://github.com/gravitystorm/openstreetmap-carto/tree/v5.4.0>

`openstreetmap-carto.style`, `openstreetmap-carto.lua` 파일은 위 git 참고  

### mapnik, mod_tile, renderd

> <https://github.com/mapnik/mapnik>  
> <https://github.com/openstreetmap/mod_tile>  

- **mapnik**
  - GIS 시각화를 위한 각종 알고리즘과 패턴이 저장된 C++ 기반 라이브러리
  - GIS 데이터를 기반으로 `Rester Files`(PNG, JPEG, SVG, PDF) 등의 지도를 생성
  - 다양한 언더(Nodejs, Python) 에서 지원  
- **mod_tile**  
  - An Apache 2 module to deliver map tiles.
  - Apache 서버의 모듈인 `mod_tile` 이 요청을 수신.
  - 요청된 타일이 이미 생성되어 캐시에 저장되어 있으면, 해당 타일을 반환.
  - 캐시에 없으면, `renderd` 에 타일 생성을 요청.
- **renderd**  
  - A daemon that renders map tiles using mapnik.
  - `renderd` 는 요청된 타일을 확인하고, `mapnik` 통해 타일을 생성(캐시에 저장).
  - 생성된 타일을 `mod_tile` 로 반환.

### openstreetmap-carto

> <https://github.com/gravitystorm/openstreetmap-carto>  

OSM 시각화를 위해 사용되는 **지도 스타일 정의셔** 

지도 데이터(도로, 건물, 수로, 공원 등)를 시각적으로 표현하기 위해 색상, 선 굵기, 라벨 폰트, 아이콘 등 지도 디자인을 정의한다.  

OSM 에서 대표적으로 사용되는 지도 스타일이 `openstreetmap-carto`  

이미 `osm2pgsql` 툴을 통해 `osm.pbf` 데이터를 openstreetmap-carto 에 맞게 변환 및 변화에 필요한 모든 데이터를 import 해두었다.  


### 최종

아래와 같은 컴포넌트로 구성되며 사진과 같은 흐름을 가진다.  

- osm2pgsql: 데이터 변환 툴
- openstreetmap-carto: 디자인 정의서
- Apache: 타일 웹서버
- mod_tile: Apache 웹 서버의 모듈, Rester Files 캐싱 및 응답 서비스
- renderd: 랜더링 컴포넌트, mapnik 연동
  - mapnik: Rester Files 생성데몬
- PostgreSQL/PostGIS database

![ddd1](/assets/2022/osm1.png)

> 출처: <https://ircama.github.io/osm-carto-tutorials/tile-server-ubuntu/>

## 데모코드  

> <https://github.com/Kouzie/osm-docker>

> <https://github.com/Overv/openstreetmap-tile-server/issues/378>  
> 원활한 지도데이터 다운로드를 위해 프록시 서버를 구성하는것을 권장  
> OSM 공식 사이트의 경우 다운로드 속도가 매우 느림...

`./external` 에 프록시로 전달할 지도 데이터 저장  

* south-korea-latest.osm.pbf  
  <https://download.geofabrik.de/asia/south-korea-latest.osm.pbf>  
* simplified-water-polygons-split-3857.zip  
  <https://osmdata.openstreetmap.de/download/simplified-water-polygons-split-3857.zip>
* water-polygons-split-3857.zip  
  <https://osmdata.openstreetmap.de/download/water-polygons-split-3857.zip>  
* antarctica-icesheet-polygons-3857.zip  
  <https://osmdata.openstreetmap.de/download/antarctica-icesheet-polygons-3857.zip>
* antarctica-icesheet-outlines-3857.zip  
  <https://osmdata.openstreetmap.de/download/antarctica-icesheet-outlines-3857.zip>  
* ne_110m_admin_0_boundary_lines_land.zip  
  <https://naturalearth.s3.amazonaws.com/110m_cultural/ne_110m_admin_0_boundary_lines_land.zip>  

지도 다운로드를 프록시로 요청하도록 설정파일을 변경한 이미지 생성

```sh
docker build -t overv/openstreetmap-tile-server-localfile:latest .
```

```sh
docker-compose up osm-import
docker-compose up osm-tile-server
```