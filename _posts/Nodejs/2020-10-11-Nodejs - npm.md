---
title:  "Nodejs - npm!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Nodejs

tags:
  - Nodejs
  - npm

toc: true
toc_sticky: true

---


## npm

`Node Package Manager`  

### npm 프로젝트 생성

프로젝트 디렉토리를 생성후 해당 위치에서 `npm init` 실행  
아래와 같은 `package.json` 파일이 생성된다.  


```json
{
  "name": "node_exam_200",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "author": "",
  "license": "ISC"
}
```

설정, 라이브러리, 실행 스크립트 등의 내용이 지정됨.  

### npm 패키지 설치  

`npm` 명령을 통해 쉽게 사용할 라이브러리들을 설치 가능하다.  

```
npm install request --save
```

`request` 모듈을 설치, `--save` 속성을 통해 `dependencies`에 어떻게 저장할 것인지 지정 가능하다.  

* `-P, --save-prod`: `package.json의` `dependencies`에 패키지를 등록. (default)  
* `-D, --save-dev`: `package.json의` `devDependencies`에 패키지를 등록.  
* `-O, --save-optional`: `package.json의` `optionalDependencies`에 패키지를 등록   
* `--no-save`: `dependencies`에 패키지를 등록하지 않습니다.   

위 명령을 실행하면 아래처럼 `package.json` 파일에 ₩ 가 추가된 것을 알 수 있다.  
```json
{
  "name": "node_exam_200",
  ...
  "dependencies": {
    "request": "^2.88.2"
  }
}
```

`npm uninstall 패키지명` 명령으로 삭제 가능하다.  

## express

웹서버 개발시 많이 사용되는 `express` 라는 패키지를 설치하여 서버를 실행해보자.  

```
npm install express --save
```

```js
const express = require('express');
const app = express();

app.get('/', (req, rep) => {
    const data = { name: 'ko', age: 25 };
    rep.send(data);
});
app.get('/error', (req, res) => {
    res.status(404).send('404 Error');
})
app.listen(5000, () => {
    console.log('server is running, http://localhost:5000');
})
```

출력값

```
{"name":"ko","age":25}
```

`express` 에선 객체 출력시 자동으로 `JSON` 형식으로 출력한다.  

## global

```
npm install -g '모듈이름'
```

`permission error` 가 뜰 것인데 아래 명령어로 `global` 모듈 설치 위치를 HOME 디렉토리로 설정하고 다운받아야 한다.  

Make a directory for global installations:

1. `mkdir ~/.npm-global`  
2. `npm config set prefix '~/.npm-global'`  
3. `export PATH=~/.npm-global/bin:$PATH`  
4. `source ~/.profile`  

js 파일에서 `var module = require('모듈이름')` 을 실행하면 `global` 에 저장된 모듈을 찾을 수 없다.  

`npm link 모듈이름` 명령을 먼저 실행 하고 js 파일을 실행하자.  

