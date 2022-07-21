---
title:  "OpenStreetMap!"

read_time: false
share: false
author_profile: false
# # classes: wide

categories:
  - aws

toc: true
toc_sticky: true

---

## OpenStreetMap  

> <https://osm.kr/about/>
> wiki: 오픈스트리트맵은 2005년 설립된 영국의 비영리기구 오픈스트리트맵 재단이 운영하는 오픈 소스 방식의 참여형 무료 지도 서비스이다

> 네이버나 카카오같은 단순 타일맵을 서비스가 아닌 지리공간 데이터베이스
오픈 데이터베이스로써 수정요청이 즉각적으로 받아들여 진다.  

<http://download.geofabrik.de/> 링크에서 각국, 세계지도 다운로드가 가능하다.  


### OpoenMapTiles  

> https://openmaptiles.org/


`OpenStreetMap`(이하 `OSM`) 데이터를 `WMS(Web Map Service)` 로 제공하려면 데이터를 변환하고 `vector tile` 형태로 제공하는 서버를 설치해야 한다.  

우선 OSM 을 `vector tile` 형태로 변환시키기 위해 `PostGIS` 에 변환하여 저장한다.  

```
OSM --(import)--> PostGIS --(encode)--> MVT
```

> https://github.com/openmaptiles/openmaptiles

위의 openmaptiles 리포에들어가면 해당작업을 docker를 통해 자동으로 진행할 수 있도록 지원한다.  

### Data import - imposm, osm2pgsql

데이터 변환 방법으로 `imposm`, `osm2pgssql` 두가지 툴이 있으며  

둘다 `OSM` 데이터를 `PostGIS` 와 같은 데이터베이스에 맞는 형식으로 변환 및 import 하기 위한 툴이다.  

> mac 에선 모두 brew 를 통해 설치 가능하며  
linux 에선 binary 혹은 apt 명령을 통해 설치 가능하다.  
<https://formulae.brew.sh/formula/osm2pgsql>
<https://formulae.brew.sh/formula/imposm3>


**PostGIS 설치**  
sudo systemctl start postgresql.service
```
docker run -d --name postgis \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=password \
    -e PGDATA=/var/lib/postgresql/data/pgdata \
    -v ~/custom/mount:/var/lib/postgresql/data \
    -p 5432:5432 \
    postgis/postgis:13-3.2-alpine
```

기본으로 생성된 postgress 의 default 스키마에서 아래 쿼리 사용가능한지 확인  

```
CREATE EXTENSION IF NOT EXISTS postgis CASCADE;
```

**osm2pgsql**

https://geoserver.org/tips%20and%20tricks/tutorials/2009/01/30/geoserver-and-openstreetmap.html 

```
osm2pgsql -c -d osm -U admin -W -H 127.0.0.1 -P 5432 south-korea-latest.osm.pbf
# password 입력 후 진행
2022-06-03 11:39:13  Setting up table 'planet_osm_point'
2022-06-03 11:39:14  Setting up table 'planet_osm_line'
2022-06-03 11:39:14  Setting up table 'planet_osm_polygon'
2022-06-03 11:39:14  Setting up table 'planet_osm_roads'
```

위처럼 4개 데이터가 생성됨 간단히 아래와 같은 정보를 포함한다.  

`planet_osm_line` - 철도, 지하철 및 기타 선형 정보
`planet_osm_roads` - 도로 정보
`planet_osm_point` - 지하철역, 쇼핑 센터, 대학, 등
`planet_osm_polygon` - 테이블에는 공원, 수역 및 특정 도시 지역의 건물 등

> 툴 설치시에 사용하는 기본 스타일 `osm2pgsql/default.style` 을 사용  

```
imposm import -connection postgis://admin:password@localhost:5432/osm \
    -mapping example-mapping.yml -read south-korea-latest.osm.pbf -write
```

> mapping 형식 <https://imposm.org/docs/imposm3/latest/mapping.html> 에서 제공하는 `example-mapping.yml` 을 사용하여 import  

## WMS(Web Map Service)

카카오, 다음 처럼 웹에서 제공하는 타일맵 형태의 서비스를 생성하려면 많은과정이 필요하다.  

> https://wiki.openstreetmap.org/wiki/Tile_servers
벡터 타일을 제공하는 서버는 여러개 있다.  

> https://github.com/openmaptiles/openmaptiles
> https://github.com/Overv/openstreetmap-tile-server


`OpenStreetMap's Standard tile layer` 라고 소개하는 `carto` 프로젝트가 가장 유명한 듯 하며  
해당 프로젝트로 지도 타일맵을 구성해보자.  

### carto

> 출처: <https://ircama.github.io/osm-carto-tutorials/tile-server-ubuntu>
> <https://www.linuxbabe.com/ubuntu/openstreetmap-tile-server-ubuntu-18-04-osm>


> 예제를 통해 서버를 설치하면서 꼭 모든 서버 구성(DB, WEB, Render)을 한 서버에 구성해야 하는지 의구심이 들 수 있는데  
DB ID 설정과 PW 로 인해 수정해야할 Config 파일이 너무 많기에 local 에 DB 를 설치하고 전용 계정을 ubuntu 에 생성하는 것을 추천한다.  

총 8개 정도의 컴포넌트로 구성되며 흐름은 아래 사진과 같다.  

- Mapnik
- Apache: 타일 웹서버
- Mod_tile
- renderd: 랜더링 컴포넌드
- osm2pgsql: 데이터 변환 툴
- PostgreSQL/PostGIS database
- carto: 타일 디자인
- openstreetmap-carto: 관련 오픈소스 프로젝트

![ddd1](/assets/2022/osm1.png)  

#### osm2pgsql import OSM to PostGIS

INSTALL 페이지에 들어가보면 `osm2pgsql` 를 통해 `openstreetmap-carto.style` 로 데이터를 import 한다.  

`-d gis` 속성이 있음으로 `postgis` 에 `gis` 데이터베이스 생성, 그리고 아래 extention 설치 
```sql
CREATE EXTENSION IF NOT EXISTS postgis CASCADE;
CREATE EXTENSION IF NOT EXISTS hstore CASCADE; -- key,value 필드 타입 지워
```

`openstreetmap-carto.style`, `openstreetmap-carto.lua` 파일은 위 git 참고  

```
$ osm2pgsql -G --hstore \
 --style openstreetmap-carto.style \
 --tag-transform-script openstreetmap-carto.lua \
 -d gis -U admin -H 127.0.0.1 -P 5432 -W \
 south-korea-latest.osm.pbf
```

데이터 삽입 완료 후 성능을 위해 `indexes.sql` 파일에 있는 인덱싱 처리 진행

#### Mpanik + Mod_tile + renderd + Apache

**Mapnik 설치**

> manik: <https://github.com/mapnik/mapnik>
> GIS 시각화를 위한 각종 알고리즘과 패턴이 저장된 C++ 기반 라이브러리
> Nodejs, Python 에서도 쓸 수 있도록 converting 가능  

```sh
$ sudo add-apt-repository ppa:ubuntugis/ppa
$ sudo apt-get update
$ sudo apt-get install -y curl unzip gdal-bin mapnik-utils libmapnik-dev python3-pip
$ sudo apt-get install -y git autoconf libtool libxml2-dev libbz2-dev \
  libgeos-dev libgeos++-dev libproj-dev gdal-bin libgdal-dev g++ \
  libmapnik-dev mapnik-utils python3-mapnik

# 설치 확인
$ mapnik-config -v
3.0.23

$ mapnik-config --input-plugins
/usr/lib/mapnik/3.0/input

$ python3 -c "import mapnik;print(mapnik.__file__)"
/usr/lib/python3/dist-packages/mapnik/__init__.py
```

**Apach 설치**

```
sudo apt-get install -y apache2 apache2-dev
sudo service apache2 start

# 설치확인
$ curl localhost| grep 'It works!'
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 10918  100 10918    0     0  3554k      0 --:--:-- --:--:-- --:--:-- 5331k
          It works!
```

**Mod_tile, renderd 설치** 

> Mod_tile: <https://github.com/openstreetmap/mod_tile> 
> 래스터 타일을 제공하는 Apache 2 모듈.

```sh
# for add repository
$ sudo apt-get install -y software-properties-common
$ sudo add-apt-repository -y ppa:osmadmins/ppa
$ sudo apt-get update

# this includes both mod-tile and renderd
$ sudo apt-get install -y libapache2-mod-tile 
```

<!-- 
**Python**  

```
$ sudo apt-get install -y python3-pip
$ python3 -m pip install --upgrade pip
$ sudo apt-get install -y python-yaml
```
-->

**Nodejs**  

```
sudo apt install -y nodejs npm
```

#### renderd, mod_tile, Apache 연결   

이제 Apache 에 두 컴포넌트를 연결해서 타일 요청을 수신 및 반환해야한다.  


```
$ sudo vi /etc/renderd.conf
; BASIC AND SIMPLE CONFIGURATION:

[renderd]
stats_file=/run/renderd/renderd.stats
socketname=/run/renderd/renderd.sock
num_threads=4
tile_dir=/var/cache/renderd/tiles

[mapnik]
plugins_dir=/usr/lib/mapnik/3.1/input
font_dir=/usr/share/fonts/truetype
font_dir_recurse=true

; ADD YOUR LAYERS:
```

위 항목을 아래처럼 수정 

```conf
/usr/lib/mapnik/3.0/input

[renderd]
stats_file=/run/renderd/renderd.stats
socketname=/run/renderd/renderd.sock
num_threads=4
tile_dir=/var/cache/renderd/tiles

[mapnik]
plugins_dir=/usr/lib/mapnik/3.0/input 
# mapnik-config --input-plugins 명령에서 출력된 위치로 수정

font_dir=/usr/share/fonts/truetype
font_dir_recurse=true

[default]
URI=/osm_tiles
TILEDIR=/var/lib/mod_tile
XML=/home/tileserver/src/openstreetmap-carto/style.xml
HOST=localhost
TILESIZE=256
```

그 외에 manik 에서 인덱싱할때 사용하는 `mapnik-utils` 설치  

```
sudo apt-get install -y mapnik-utils
```

ls /var/run/renderd
renderd.pid  renderd.sock  renderd.stats

#### carto 설치  

폰트 설치

```
sudo apt-get install -y fonts-noto-cjk fonts-noto-hinted fonts-noto-unhinted fonts-hanazono ttf-unifont fonts-dejavu-core
```

`nodejs`, `npm` 설치

```
sudo apt-get install -y nodejs npm
```

carto 0버전 설치

```
sudo npm install -g carto@0
carto -v
carto 0.18.2 (Carto map stylesheet compiler)
```

```
$ mkdir carto

$ npm install mapnik-reference
$ node -e "console.log(require('mapnik-reference'))"
{
  versions: [
    '2.0.0',  '2.0.1',
    '2.0.2',  '2.1.0',
    '2.1.1',  '2.2.0',
    '2.3.0',  '3.0.0',
    '3.0.3',  '3.0.6',
    '3.0.20', '3.0.22'
  ],
  latest: '3.0.22',
  load: [Function (anonymous)]
}
```

```
$ cd ../
$ git clone https://github.com/gravitystorm/openstreetmap-carto.git
$ cd openstreetmap-carto
$ carto -a "3.0.22" project.mml > style.xml
$ ls -l style.xml
```



## GeoServer  

> wiki: GeoServer(지오서버)는 지리공간 데이터를 공유하고 편집할 수 있는 Java로 개발된 오픈 소스 GIS 소프트웨어 서버이다

> web:<https://geoserver.org/>
> git: <https://github.com/geoserver/geoserver/tree/2.21.0>



### Shapefile

지원 파일

> http://data.nsdi.go.kr/dataset
> http://download.geofabrik.de/asia.html
