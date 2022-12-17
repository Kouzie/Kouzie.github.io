---
title:  "JavaScript 모듈"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - HTML

tags:
  - javaScript

toc: true
toc_sticky: true

---

## 모듈  

JS 는 WEB 에서 단순 보조기능을 위해 사용되었지만 NodeJS 나 SPA 프레임워크들이 생겨나면서 **모듈화 구조**가 필요해졌다.  

모듈에서 사용하기 위한 JS 의 표준화된 문법정도라 생각하면 된다.  
`모듈 스코프`(파일 스코프) 에서 자산인 변수 함수 객체들을 모듈내에서 따로 관리하며  
외부접근을 차단하고 내부 리소스가 외부로 참조되는것도 방지한다.  

대표적은 모듈화 구조로 아래 3개

- UMD(Universal Module Definition)
- CommonJS
- ESM(ES Module, ES6 Module)

> AMD(Asynchronous Module Definition) 도 있지만 거의 사용안함
> `CommonJS` 는 `NodeJS` 백앤드 진영에서 표준으로 사용된다 

클라이언트 사이드에선 `ESM` 을 주로 사용한다.  

`ESM` 사용방법은 아래처럼 `script` 태그안에 `type` 을 지정해서 사용한다.

```html
<script type="module" src="app mjs"></script>
```

> 확장자는 mjs 사용을 권장하며 기본적으로 `strict mode` 로 동작된다.  

다음과 같이 `foo.mjs`, `bar.mjs`, `test.html` 문서를 만들고 문서를 열어보면 console 에 foo 와 bar 가 정상출력된다.  

```js
// foo.mjs
var x = 'foo';
console.log(window.x);

// bar.mjs
var x = 'bar';
console.log(window.x);
```

```html
<!-- test.html -->
<!DOCTYPE html>
<html>
<body>
    <script src="foo.js"></script>
    <script src="bar.js"></script>
</body>
</html>
```

실상 script 파일을 로드해서 하나의 JS 코드블록처럼 동작시킨다.  
그러다 보니 기껏 별도의 파일로 분리한 `foo.js`, `bar.js` 에서 동일한 `window` 객체도 접근하고 x 를 집어넣어 정상출력되는 것.  

아래처럼 module 형식으로 파일을 불러오면 모듈화 구조를 사용하기에  
전역scope 에 존재하는 `window` 객체를 `foo.js`, `bar.js` 에서 접근할 수 없어 `undefined` 가 출력된다.  

> foo, bar 는 모듈스코프에서 자산을 관리하기 때문에 전역스코프 객체인 window 에는 접근불가  

```html
<script type="module" src="foo.mjs"></script>
<script type="module" src="bar.mjs"></script>
```


### import, export  

모듈 스코프의 자산들은 외부 모듈에서 접근할 수 없으며 분리되어 관리된다.  
`import`, `export` 키워드를 사용해 명시적으로 공개해야 외부 모듈에서 접근할 수 있다.  

> `import`, `export` 는 `ESM` 문법이다.  

아래와 같이 `lib.mjs` 파일을 생성하고 공유하려는 모듈내의 변수, 함수, 객체에 모두 `export` 키워드를 지정,

```js
// lib.mjs
export const pi = Math.PI;

export function square(x) {
    return x * x;
}

export class Person {
    constructor(name) {
        this.name = name;
    }
}
// export { pi , square , Person };
// export 를 마지막에 한번에 지정 가능
```

자산을 공유받는 `app.js` 모듈에선 `import` 키워드를 지정한다.  

```js
// app.js
import { pi, square, Person } from './lib.mjs';

console.log(pi); // 3.141592653589793
console.log(square(10)); // 100
console.log(new Person('Lee')); // Person { name: 'Lee' }
```


```html
<!DOCTYPE html>
<html>
<body>
    <script type="module" src="app.mjs"></script>
    <script type="module">
        import { pi, square, Person } from './lib.mjs';
        console.log(pi); // 3.141592653589793
    </script>
</body>
</html>
```

### as  


다음처럼 객체형태로 `export` 하고 `as` 키워드를 사용해 객체에 `import` 할 수 있다.  

```js
// lib.mjs
const pi = Math.PI;

function square(x) {
    return x * x;
}

class Person {
    constructor(name) {
        this.name = name;
    }
}
export { pi , square , Person };


// app.mjs
import * as lib from './lib.mjs';

console.log(lib);            // class Module
console.log(lib.pi);         // 3.141592653589793
console.log(lib.square(10)); // 100
console.log(new lib.Person('Lee')); // Person { name: 'Lee' }
```

> `lib` 는 `Module` 객체로서 공유받은 모듈 자산들을 property 로 가지고 있음  

`as` 키워드는 모듈 자산의 식별자를 변경할 수 도 있다.  

```js
import { pi as PI, square as sq, Person as P } from './lib.mjs';
```

### default

```js
// lib.mjs export 
default x = > x * x;
```

```js
// app.mjs 
import square from ' /lib mjs';
console.log(square(3)); // 9
```

`defulat` 키워드를 사용하면 `import` 와 동시에 식별자 바인딩이 완료됨  

> `default` 뒤에 `var` 같은 키워드는 사용하지 못하고 표현식만 가능함  


## Webpack

> <https://webpack.js.org/>
> <https://webpack.kr/>


```js
<script type="module" src="foo.mjs"></script>
<script type="module" src="bar.mjs"></script>
```

위처럼 module script 태그를 통해 JS 파일을 불러와 브라우저 화면을 구성해도 되지만  
JS 파일을 가져오기 위해 수많은 HTTP 요청이 발생하고 성능하락의 원인이 된다.  

또한 브라우저별로 모듈화 문법들이 달라 `ESM` 문법을 사용할 경우 특정 브라우저에서 동작하지 않을 수 있다.  

특히 최근 React, Vue 와 같은 SPA 프로젝트의 경우 사용하는 js 파일 개수와 크기가 100MB 단위를 넘기 때문에 실제 서버에 올리려면 압축과정이 필수이다.  

> CSS, JPG 리소스파일 역시 HTTP 요청이 발생함으로 성능하락의 원인이 됨  

이러한 문제를 해결하기 위해 여러개의 리소스파일을 합쳐 최소한의 HTTP 요청을 위해 **번들링 작업을 수행하는 번들러**가 있다.  

> npm 으로 여러 라이브러리를 다운받은 경우 번들링 툴 사용을 필수  

대표적인 번들러로 `RequireJS`, `Browserify`, `Parcel` 등이 있으며, 현재는 `Webpack` 이 거의 표준이라 할 수 있다.  

> 번들링(Bundling): 묶는다
> 모듈들의 의존성 관계를 파악하여 그룹화시켜주는 작업을 뜻함  
> 모듈화 구조별로 사용되는 번들러가 다름  

![2](/assets/javascript/image15.png) 

그림처럼 수많은 js, css 등의 파일들을 수집해서 단 4개의 파일로 만들어준다.  

### webpack-cli

먼저 `webpack-cli` 만을 사용해 번들링하는 방법을 알아본다.  

`Webpack` 을 사용하기위해 NodeJS 프로젝트로 만들어야 한다.  

```
# 기본 package.json 생성
npm init

# -D 옵션은 개발환경에서만 사용한다는 뜻
npm install -D webpack webpack-cli
```

위 명령어 두개를 실행하면 `devDependencies` 에 `webpack` 구성이 추가된다.  

```json
// package.json
"devDependencies": {
    "webpack": "^5.75.0",
    "webpack-cli": "^5.0.1"
}
```

`webpack-cli` 명령어를 사용하여 기존에 만들어 두었던 `app.mjs` 을 번들링  

```
npx webpack --entry ./app.mjs --output-path ./dist
```

`dist/main.js` 파일이 생성된다.  
`app.js` 와 내부에서 사용하던 `lib.js` 가 `main.js` 로 합쳐졌다.  

아래처럼 `main.js` 를 불러오면 기존 코드와 동일하게 동작한다.  

```html
<!DOCTYPE html>
<html>
<head>
    <script type="module" src="dist/main.js"></script>
</head>
<body></body>
</html>
```

## webpack.conf.json

> <https://webpack.js.org/configuration/>
> 버전별로 `webpack-cli`, `config` 파일 형식이 다름으로 document 확인을 권장  

`webpack-cli` 툴에서 `option` 을 지정해서 번들링을 진행해도 되지만  
복잡한 프로젝트의 경우 webpack 용 `config` 을 사용하는 것이 일반적이다.  

위의 `webpack-cli` 명령을 `config` 파일로 변경하면 아래와 같다.  

```js
// webpack.conf.json
const path = require("path");

module.exports = {
  entry: "./app.mjs",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "main.js"
  }
}
```

```
npx webpack --config webpack.config.js

# 기본 이름을 사용할 경우 생략 가능
npx webpack

# 변경을 감지하여 자동 컴파일
npx webpack --watch
```

### mode

프로젝트의 빌드 환경에 맞춰 mode 를 다르게 설정하여 

```js
string = 'production': 'none' | 'development' | 'production'
```

`production` 이 기본값이고 `none` `development` 설정이 가능하며  
webpack 의 기본값들이 버전설정에 따라서 변경된다.  

아래와 같이 mode 를 명시적으로 development 로 변경하고 다시 번들링을 진행하면  

```js
const path = require("path");

module.exports = {
  mode: "development",
  entry: "./app.mjs",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "main.js"
  }
}
```

번들링 파일의 압출률이 없어저 사람이 알아볼수 있는 형식으로 변경된다.  

일반적으로 dev 용 prd 용 config 파일을 여러개 생성해서 사용하거나  

```
npx webpack --config webpack.config.dev.js
npx webpack --config webpack.config.prd.js
```

환경변수와 조건문을 사용한다.  
 
### loader  


지금까지 JS 파일만을 번들링했지만 `loader` 를 사용함에 따라 CSS, IMG 모두 번들링할 수 있다.  

확장자별로 번들링할 수 있는 `loader` 가 별도로 존재한다.  

> <https://webpack.js.org/loaders/>

`js` 파일 안에 `css` 파일을 `import` 하는 구문을 추가  

```js
// app.js
import { pi, square, Person } from './lib.mjs';
import css from './style.css'

console.log(pi); // 3.141592653589793
console.log(square(10)); // 100
console.log(new Person('Lee')); // Person { name: 'Lee' }
console.log(css)
```

css 파일은 아래와 같다.  

```css
/* style.css */
body {
    background-color: powderblue;
}
```

`loader` 를 사용하기 위해 사전 설치작업이 필요하다.  

> <https://webpack.js.org/guides/asset-management/#loading-css>

```
# css-loader: css 번들링
# style-loader : css 를 웹에 style 태그로 적용
npm install --save-dev style-loader css-loader
```

저 2가지 외에도 많은 `loader` 들이 존재하며  
`loader` 들과 적용조건을 아는것이 webpack 숙련도를 결정한다.  


```js
const path = require("path");

module.exports = {
  mode: "development",
  entry: "./app.mjs",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "main.js"
  },
  module: {
    rules: [{
      test: /\.css$/,
      use: [
        'style-loader',
        'css-loader'
      ]
    }]
  }
}
```

`main.js` 하나만 http 요청으로 받았음에도 불구하고  
css 의 배경색으로 페이지가 변경된 것을 확인할 수 있다.  

> 뒤쪽의 loader 가 먼저 실행됨으로 `style-loader` 는 가장 늦게 실행되는 첫번째 행에 적용해야함  

### plugin  

`plugin` 은 최종적인 결과물을 변화하는 작업을 수행한다.  

번들링의 개념이 아닌 추가적인 편의요소를 제공하기 위한 기능으로 많은 `plugin` 들이 존재한다.  

> <https://webpack.js.org/plugins/>

html 파일을 template 으로 사용하여 여러개의 html 을 자동생성하는 webpack `plugin` 인 
`HtmlWebpackPlugin` 을 사용해보자  

```js
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin")

module.exports = {
  mode: "development",
  entry: "./app.mjs",
  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "main.js"
  },
  module: {
    rules: [{
      test: /\.css$/,
      use: [
        'style-loader',
        'css-loader'
      ]
    }]
  },
  plugins: [ new HtmlWebpackPlugin() ],
}
```

webpack 을 실행하면 `dist/main.js` 만 생길 뿐 아니라 자동으로 아래와 같은 `index.html` 파일을 생성해준다.  

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Webpack App</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"><script defer src="main.js"></script></head>
  <body>
  </body>
</html>
```

다음처럼 생성자함수 파라미터에 여러가지 설정값을 추가해서  
자동생성할 파일의 `template` 을 사전지정하거나 출력파일 이름을 지정할 수 있다.  

```js
plugins: [
    new HtmlWebpackPlugin({
      template:'./index.html',
      filename:'./index_auto.html'
    }),
    new HtmlWebpackPlugin({
      template:'./index_2.html',
      filename:'./index_2_auto.html'
    })
]
```