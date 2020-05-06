---
title:  "Vue - Vue 트랜스 파일러, Vue project!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Vue

tags:
  - javascript
  - vue

toc: true
toc_sticky: true

---

## 트랜스 파일러  

대규모 에플리케이션을 개발하려면 단순 `vue.js` 라이브러리 참조가 아닌 `SPA(Single Page Application)` 구조의 프로젝트를 구축해야 한다.  

`Vue Router`, `Vuex` 등 다양한 요소가 포함되는데 이를 사용하려면 `ES2015` 혹은 `Typescript`를 사용해야 한다.  

하위 버전의 브라우저에선 이들을 지원하지 않기에 과거의 자바스크립트 문법으로 포팅해주어야 한다.  

이 과정을 **트랜스 파일러** 라는 녀석이 해주는데 대표적으로 `Babel`, `TSC(TypeScript Compiler)`가 있다.  

### Babel

`yarn` 혹은 `npm` 패키지 매니저로 `babel-cli, babel-preset-env, babel-preset-stage-2` 설치  

```
$ npm install --save-dev babel-cli babel-preset-env babel-preset-stage-2
$ yarn add -D babel-cli babel-preset-env babel-preset-stage-2
```

`package.json` 에서 아래 의존객체들이 추가되었는지 확인  

```json
{
  ...
  ...
  "devDependencies": {
    "babel-cli": "^6.26.0",
    "babel-preset-env": "^1.7.0",
    "babel-preset-stage-2": "^6.24.1"
  }
}
```

간단한 ES2015 문법을 가진 샘플 파일 작성  

```js
let name = 'world';
console.log(`hello ${name}`);
```

`let` 블록범위 변수는 구형 브라우저에선 지원하지 않는 기능이다.  

```
$ babel src -d build
```

위 명령 실행후 `build` 디렉토리에 생성된 파일 확인  

```js
'use strict';

var name = 'world';
console.log(`hello ${name}`);
```

> https://babeljs.io/repl - 온라인 사이트에서 트랜스파일된 코드를 실시간으로 확인 가능  

## Vue-CLI

Vue 어플리케이션을 빠르게 개발할 수 있는 여러가지 기능을 제공하는 도구.  
각종 스캐폴딩, 웹팩번들러 설정을 자동/대화형 으로 만들어줌

### 프로젝트 생성

```
$ vue create test1
...
? Please pick a preset: 
❯ default (babel, eslint) 
  Manually select features 
```

`default preset`의 경우 `babel`, `eslint` 플러그인만 설치됨.  
자유롭게 플러그인 선택을 원한다면 `Manually select features` 를 선택  

> `eslint`: `EcmaScript + Lint`입니다. `Lint`는 보푸라기라는 뜻인데 프로그래밍 쪽에서는 에러가 있는 코드에 표시를 달아놓는 것을 의미합니다.  
> 즉 `eslint`는 자바스크립트 문법 중 에러가 있는 곳에 표시를 달아놓는 도구를 의미합니다.  
> https://www.zerocho.com/category/JavaScript/post/583231719a87ec001834a0f2  


Linter / Formatter - ESLint with error prevention only - Lint on save
in dedicated config files - 플러그인 구성정보를 별도의 파일로 저장 or package.json에 저장


`src` - 개발자가 생서한 소스파일 저장 디렉토리
`public` - 배포버전 빌드시 사용하는 설정파일, 웹팩을 사용해 이 디렉토리에 있는 설정파일을 로드하고 빌드버전을 생성
`node_modules` - 앱 개발과 배포에 필요한 npm 패키지 저장 디렉토리
`dist` - 배포버전이 저장되는 디렉토리, vue 컴포넌트들은 모두 js파일로 트랜스파일되어 난독화 되어 저장된다. .css 파일도 마찬가지, 

### 구성요소  

Vue 프로젝트는 크게 3가지로 구성된다.  
`@vue/cli`, `@vue/cli-service`, `CLI 플러그인`


<!-- ### @vue/cli -->


### @vue/cli-service

cli 서비스는 프로젝트 생성시 개발 의존성으로 설치되는 구성요소
`webpack`, `webpck-dev-server` 기반으로 작성

생성된 `package.json` 파일에 아래와 같은 실행명령어가 저장되어있다.  

```json
{
  ...
  "scripts": {
    "serve": "vue-cli-service serve", //웹팩 개발서버를 이용, 프로젝트 코드 실행, 코드변경시 실시가 반영됨
    "build": "vue-cli-service build", //빌드하여 배포파일을 생성
    "lint": "vue-cli-service lint" //코드의 표준화 되지 않은 부분 검사 및 교정 
  },
  ...
}
```

모두 `vue-cli-service`를 사용한다.  

`yarn`, `npm` 과 같은 패키저를 통해 해당 명령을 포함시켜 수행한다.  

```
$ yarn serve -> vue-cli-service serve 실행
$ yarn build -> vue-cli-service build 실행
$ yarn lint  -> vue-cli-service lint 실행
```

`package.json`을 통하지 않고 직접 `vue-cli-service` 를 실행하고 싶다면 `yarn` 혹은 `npx` 를 사용하면 된다.  

```
$ yarn vue-cli-service inspect - 프로젝트 설정정보 출력 명령
$ yarn vue-cli-service build - 프로젝트 빌드, 생성된 dist 디렉토리 확인
```

`vue-cli-service serve` 가 개발용으로 서버실행한다 했는데
실행과 동시에 브라우저를 열고 포트번호를 기본 `8080` 에서 다른 포트로 변경하고 싶다면 아래와 같은 옵션사용  

`vue-cli-service serve --open --port 3000`

`package.json` 에 옵션을 추가 지정해서 `$ yarn serve` 로 서버 실행  

### CLI플러그인

추가 기능을 제공하는 npm 패키지
프로젝트 생성시 추가할 플러그인 선택 가능, 향후 추가도 가능

대표적으로 vue 프로젝트 구성시 필수적으로 필요한 플러그인이 몇개 있는데 그중 하나인 `vue-router` 플러그인을 설치해보자.  

```
$ vue add router - @vue/cli-plugin-router 설치
```

플러그인을 설치하고 `package.json` 확인

```json
{
  ...
  "devDependencies": {
    "@vue/cli-plugin-babel": "^4.2.0",
    "@vue/cli-plugin-eslint": "^4.2.0",
    "@vue/cli-plugin-router": "file:node_modules/@vue/cli-plugin-router",
    "@vue/cli-service": "^4.2.0",
    "babel-eslint": "^10.0.3",
    "eslint": "^6.7.2",
    "eslint-plugin-vue": "^6.1.2",
    "vue-template-compiler": "^2.6.11"
  }
}
```

프로젝트 생성시 설치한 `babel`, `eslint`와 함께 `router` 가 추가됬다.  

대부분의 vue 플러그인은 `node_modules/@vue/` 디렉토리 아레에 추가된다.  

### vue ui

서버의 각종 설정정보, 설치 플러그인, 실행/종료 등 각종 기능을 GUI 환경에서 확인 가능하다.  
프로젝트들이 저장된 디렉토리에서 $ vue cli 실행


## 단일파일 컴포넌트  

이전장에서 `vue.js` 라이브러리를 통해 `html` 파일에서 Vue 컴포넌트를 사용하였는데 이는 전역 컴포넌트로 대규모 웹을 구성하기엔 부적절하다.  

이미 복잡한 `html` 파일에 `script`, `style`, `template` 태그가 범벅되는 것은 정신건강에 매우 해롭다.  
또한 빌드과정이 없음으로 구형 브라우저에션 최신 ECMA스크립트를 사용할 수 없다. 

Vue 프로젝트를 생성하고 어떤식으로 단일파일 컴포넌트가 구성되는지 간단히 알아보자.  

```
$ vue create todolistapp
```

생성된 `App.vue` 확인  

![vue11](/assets/vue/vue11.png){: .shadow}     

크게 `<template>`, `<script>`. `<style>` (머리 가슴 배)로 구성된다.  

`Vue.component("id", {...})` 같은 Vue 컴포넌트 객체 생성코드도, `<templtae id="...">` 탬플릿 태그에 id 속성도 사용되지 않는다.  

```js
export default {
  name: 'App',
  components: {
    HelloWorld
  }
}
```

`components/HelloWorld.vue` 파일을 살펴보면 비슷하다.  

똑같이 머리 가슴 배로 이루어지며 탬플릿에 외부에서 전달받은 `msg` 를 출력하는 컴포넌트이다.  

그리고 서버가 최종적으로 실행하는 `main.js` 를 보면 Vue 인스턴스 생성후 `App.vue` 에 정의된 `App` 컴포넌트를 랜더링한다.  

```js
import Vue from 'vue'
import App from './App.vue'

Vue.config.productionTip = false

new Vue({
  render: h => h(App),
}).$mount('#app')
```

대충 구조는 `App` 컴포넌트가 `HelloWorld` 같은 작은 컴포넌트들을 자식으로 두로 `main.js`에서 최종적으로 랜더링 되는 구조이다.  


`vue-loader` 플러그인을 설치하고 `.vue` 확장자로 시작하는 파일을 작성해 단일 파일 컴포넌트를 작성해보자.  

### 스타일 범위

각 단일파일 컴포넌트마다 `<style>` 태그가 있다.  
빌드되며 병합되면서 의도치 않게 클래스명 중복사용으로 인해 충동발생할 수 있다.  

특정 컴포넌트만의 스타일을 지정하려면 **범위CSS**, **CSS모듈** 2가지 방법을 사용할 수 있다.  

**범위CSS** 를 사용하려면 `.vue` 파일 안의 `style` 태그에 `scoped` 키워드만 넣어주면 된다.  

{% highlight html %}{% raw %}
<template>
  <div class="main">{{msg}}</div>
</template>

<script>
export default {
  name: "child1",
  data: function() {
    return { msg: "child1" };
  }
};
</script>

<style scoped>
.main {
  border: solid 1px black;
  background-color: yellow;
}
</style>
{% endraw %}{% endhighlight %}


`.vue` 파일이 빌드되면서 `template` 내부의 태그와 매칭되는 css에 고유속성이 부여된다.  

{% highlight html %}{% raw %}
<!-- 생성된 Element -->
<div data-v-3a3c19c6="" class="main">child2</div>
{% endraw %}{% endhighlight %}

범위CSS의 단점은 CSS선택자로 속성을 사용하기에 스타일 적용 속도가 느리다. 빠른 속도를 원한다면 id, class 를 이용해 스타일 적용할것  

또한 부모컴포넌트에 적용된 CSS는 자식 컴포넌트에서도 영향을 끼친다.  

{% highlight html %}{% raw %}
<template>
  <div class="main">
    {{msg}}
    <child11></child11>
    {{msg}}
  </div>
</template>
{% endraw %}{% endhighlight %}

위처럼 자식 컴포넌트를 탬플릿에 적용할때 부모의 속성값을 전이한다.  

결과적으로 아래처럼 2개의 속성을 모두 가지게 된다.  

{% highlight html %}{% raw %}
<div data-v-0b9bd83c="" data-v-3a2e0245="" class="test">
  <h3 data-v-0b9bd83c="">Child's child</h3>
</div>
{% endraw %}{% endhighlight %}

**CSS모듈**은 `<style module>` 키워드를 사용한다.  
마치 css 스타일을 객채처럼 사용할 수 있도록 설정.  

{% highlight html %}{% raw %}
<template>
  <div :class="$style.hand">CSS Module을 적용한 버튼</div>
</template>

<script>
export default {
  created: function () {
    console.log(this.$style);
    //{hand: "Module1_hand_1l2s2"}
  }
};
</script>
<style module>
.hand {
  cursor: pointer;
  background-color: purple;
  color: yellow;
}
</style>
{% endraw %}{% endhighlight %}

아래처럼 변환된다.  

{% highlight html %}{% raw %}
<style>
.Module1_hand_1l2s2 {
    cursor: pointer;
    background-color: purple;
    color: yellow;
}
</style>
...
<div class="Module1_hand_1l2s2">CSS Module을 적용한 버튼</div>
{% endraw %}{% endhighlight %}

클래스명을 컴포넌트명과 결합시켜 변환시켜버리기에 중복될 가능성이 없다.  

만약 사용해야할 CSS 객체가 여러개라면 아래처럼 배열형식으로 다중적용가능

{% highlight html %}{% raw %}
<template>
  <div :class="[$style.hand. $style.foot]">CSS Module을 적용한 버튼</div>
</template>
{% endraw %}{% endhighlight %}
