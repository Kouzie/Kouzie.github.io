---
title:  "Nodejs - nuxt with vue!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - Spring

tags:
  - Spring
  - java

toc: true
toc_sticky: true

---

## 개요

`nuxt` - 서버사이드 자바스크립트 프레임 워크  
`vue` - 프론트엔트 자바스크립트 프레임 워크  
```

> npm install -g vue-cli
> vue init nuxt-community/starter-template nuxt-exam
> cd nuxt-exam
> npm install 
> npm install --save nuxt
> npm run dev
```

http://localhost:3000/ 으로 접속 가능한 테스트 

`nuxt` 에서 `vue` 를 사용해 쉽게 프론트 엔드를 구축할 수 있다.  

## 서버 실행

`npm install --save nuxt`

`pages` 디렉토리에 `users.vue` 생성

```html
<!-- users.vue -->
<template>
    <h1>user page</h1>
</template>
```

`nuxt.js` 는 `pages` 디렉토리 구조에 따라 자동으로 라우팅된다.  
> http://localhost:3000/users


### 디렉토리 구조  

`nuxt.config.json`
nuxt 설정 정보 저장 파일, 

`package.json`
의존성 모듈, 스크립트를 정의 파일

`assets`
SASS, LESS, 빌드가 필요한 JS파일이 저장되는 디렉토리

`components`
화면 구성을 위해 사용되는 컴포넌트들이 저장되어 있는 디렉토리  

`layouts`
기본 레이아웃 포함하는 디렉토리, 반복되는 상단, 하단, 좌/우측 레이아웃을 위한 파일 정의

`middleware`
페이지 렌더링 전에 실행되는 파일이 정의되는 디렉토리

`pages`
컴포넌트의 일종, 

`plugins`
웹 어플리케이션이 실행되기전에 실행시키고 싶은 js 파일을 저장하는 디렉토리,

`static`
CSS, JS, image 같은 정적 파일 저장하는 디렉토리

`store`
상태관리를 도와주는 `vuex` 라이브러리가 포함된 디렉토리

## 파일 구조

{% highlight html %}{% raw %}
<template>
  <h1>message: {{ $store.state.data }}</h1>
</template>

<script>
export default {
  // vue instance init before
  // vuex 데이터 스토어에 데이터를 초기화 하기 위해 사용,
  // asyncData 와 data 는 컴포넌트에서 사용하는 데이터 초기화
  fetch({ store, params }) {
    console.log("fetch");
    store.commit("dataUpdate", data);
  },

  // vue instance init
  create() {
    console.log("create");
  },

  // 비동기적으로 데이터 생성, data와 merge 됨,
  // 사용자 입장에서 data 와 차이는 없으나 초기화 하는 데이터가 비동기, 동기이냐 차이
  asyncData(context) {
    console.log("asyncData");
    // get some data from another server
    return { message2: "world" };
  },

  // data 생성
  data() {
    return { message1: "hello" };
  },

  // 해당 페이지 <head> custom, this 키워드를 사용해 컴포넌트 data 접근 가능 ex) this.message1
  head() {
    return {
      title: "",
      meta: [{ hid: "고유값", name: "설멍", content: "커스텀 설명" }],
    };
  },

  methods: {},
  computed: {},
  components: {},

  // 특정 레이아웃 지정, 지정하지 않을시 layouts/default.vue 레이아웃 사용,
  layout: "boardDefault",
  // method 형태로도 사용 가능
  /* layout(context) {
      "boardDefault",
  }, */
  // 컴포넌트 호출전 특정 코드 실행 .js 파일 정의 및 값으로 지정
  middleware: "auth.js",
  // 페이지 스크롤 상/하단 여부 결정, false 라면 하단, true 라면 상단으로 스크롤 이동
  scrollToTop: true || false, // 최상단 true , 최하단 false
};
</script>
<style scoped>
h1 {
  color: red;
}
</style>
{% endraw %}{% endhighlight %}

## nuxt.config.js

![nuxt1](/assets/nodejs/nuxt1.png){: .shadow}     

`nuxt` 로 프로젝트 생성시 기본 설정파일이다.  

`head`, `loading`, `build` 에 대한 설정이 있으며 추가적으로 아래 설정을 지정할 수 있다.  

`cache`, `css`, `dev`, `end`, `generate`, `plugins`, `rootDit`, `router`, `srcDir`, `transition`

### cache

컴포넌트 캐시의 허용 유무, 

설정된 내용은 전역으로 모든 파일에 적용된다.  
```js
module.exports = {
    cache: {
        max: 1000,
        maxAge: 1000 * 60 * 60 // 1시간
    }
}
```

### loading

페이지 이동시 상단에 표시되는 progress bar 설정

```js
module.exports = {
loading: {
        color: '#3B8070',
        height: '5px',
        failedColor: 'red', // error 발생시 색상
        duration: 1000 * 10 // 최대 진행시간 default 5초
    },
}
```

## 라우팅

`pages` 디렉토리 구조에 따라 라우팅 가능,  
만약 디렉토리 구조가 아래와 같다면  

```
pages
└── users
    ├── index.vue
    └── profile.vue
├── README.md
├── index.vue
```

`pages/users/index.vue`에 접근하기 위해 아래 Url 을 입력하면 된다.  
http://localhost:3000/users

`pages/users/profile.vue` 는 http://localhost:3000/users/profile 로 접근 가능하다.  

### 동적 라우팅

만약 `path parameter` 형식으로 라우팅 + 파라미터 전달을 하고 싶다면 아래와 같은 pages 디렉토리 구조를 갖추면 된다.  

```
pages
└── users
    ├── index.vue
    └── _id.vue
├── README.md
├── index.vue
```

{% highlight html %}{% raw %}
<template>
  <section>
    <h1>hello user! {{ userId }}</h1>
  </section>
</template>

<script>
export default {
  data() {
    return { userId: this.$route.params.id };
  },
};
</script>
{% endraw %}{% endhighlight %}

`this.$route.params` 프록시 변수를 사용해 접근 가능하다.(언더바는 생략)  

### 페이지 이동

nuxt 에선 페이지 이동을 위해 nuxt-link 속성을 사용한다.  

```vue
<template>
  <section>
      <h1>hello user!</h1>
      <div>
          <nuxt-link to=""></nuxt-link>
          <nuxt-link to="/users/1">1 유저</nuxt-link>
          <nuxt-link to="/users/2">2 유저</nuxt-link>
          <nuxt-link to="/users/test">test 유저</nuxt-link>
      </div>
  </section>
</template>
```

만약 숫자형식의 path parameter 만 허용하고 싶다면 이동되는 페이지에서 `validate` 메서드 정의  


{% highlight html %}{% raw %}
<template>
  <section>
    <h1>hello user! {{ userId }}</h1>
  </section>
</template>

<script>
export default {
  validate({ params }) {
      if (!isNaN(params.id)) {
          return true;
      } else {
          return false;
      }
  },
  data() {
    return { userId: this.$route.params.id };
  },
};
</script>
{% endraw %}{% endhighlight %}

![nuxt2](/assets/nodejs/nuxt2.png){: .shadow}     

http://localhost:3000/users/test 로 접근시 아래와 같이 404 에러가 발생한다.  


## 레이아웃

`layouts` 디렉토리에 가면 기본적으로 `default.vue` 파일이 있다.   

```vue
<template>
  <div>
    <nuxt/>
  </div>
</template>
```

공백의 `div` 태그가 정의되어 있다.  

`index.vue` 나 `_id.vue` 파일에 실제 생성된 html 파일을 보면 `_nuxt` `id` 를 가지는 `div` 태그가 포함되어 있다.  

저부분에 공통적으로 작성할 레이아웃을 지정한다.  


### error 레이아웃   

`layouts/error.vue` 파일을 정의해두면 에러페이지 레이아웃을 지정해둘 수 있다.  


{% highlight html %}{% raw %}
<template>
    <div>
        <h1 v-if="error.statusCode === 404">페이지를 찾을 수 없습니다</h1>
        <h1>에러가 발생했습니다</h1>
    </div>
</template>

<script>
export default {
    props: ['error']
}
</script>
{% endraw %}{% endhighlight %}

`props` 속성을 사용해 `nuxt.js` 에서 전달한 `error` 객체를 받아 사용할 수 있다.  


## 컴포넌트

반복되는 코드를 요소로 만들어 관리할 수 있도록 하는 것이 컴포넌트이다.  

`components` 디렉토리안에 정의해두고 필요할 때 마다 꺼내어 쓸 수 있다.  
디렉토리 안에 다음과 같은 `c1.vue`, `c2.vue` 파일 정의  

```html
<!-- c1.vue -->
<template>
  <div>
    <h1>c1 컴포넌트</h1>
  </div>
</template>
```
```html
<!-- c2.vue -->
<template>
  <div>
    <h1>c2 컴포넌트</h1>
  </div>
</template>
```

{% highlight html %}{% raw %}
<template>
  <div class="index">
    <c1></c1>
    <c2></c2>
  </div>
</template>
<script>
import c1 from '~/components/c1.vue'
import c2 from '~/components/c2.vue'

export default {
  components: { c1, c2 },
}
</script>
{% endraw %}{% endhighlight %}

실제 클라이언트에게 출력되는 html 파일은 아래와 같다.  

```html
<div id="__layout">
    <section>
      <div class="index">
        <div><h1>c1 컴포넌트</h1></div>
        <div><h1>c2 컴포넌트</h1></div>
      </div>
    </section>
</div>
```

### 컴포넌트에 데이터 전달하기 - props

우선 컴포넌트에서 전달받을 데이터 변수를 정의  

{% highlight html %}{% raw %}
<!-- c1.vue -->
<template>
  <div>
    <h1>c1 컴포넌트</h1>
    <p>{{ message }}</p>
  </div>
</template>

<script>
export default {
  props: ["message"],
};
</script>
{% endraw %}{% endhighlight %}

전달받을 데이터는 `props` 속성을 정의한다.  

`콜론 + 변수명`을 사용해 원하는 데이터를 전달한다.  

{% highlight html %}{% raw %}
<!-- index.vue -->
<template>
  <div class="index">
    <c1 :message="message1"></c1>
    <c2 :message="message2"></c2>
  </div>
</template>
<script>
import c1 from "~/components/c1.vue";
import c2 from "~/components/c2.vue";

export default {
  components: { c1,c2, },
  data() {
    return {
      message1: "hello1",
      message2: "hello2",
    };
  },
};
</script>
{% endraw %}{% endhighlight %}

자바스크립트에선 함수 또한 하나의 변수(객체) 로 보기에 전달 가능하다.  

{% highlight html %}{% raw %}
<!-- index.vue -->
<template>
    ...
    <c2 :touch="f"></c1>
    ...
</template>
<script>
    ...
    methods: {
        f() {alert('touch!')}
    }
</script>
{% endraw %}{% endhighlight %}

{% highlight html %}{% raw %}
<!-- c2.vue -->
<template>
  <div>
    <h1>c2 컴포넌트</h1>
    <button @click="touch">click</button>
  </div>
</template>

<script>
export default {
  props: ["touch"],
};
</script>

{% endraw %}{% endhighlight %}

### 전달받은 데이터 검사  

`String`, `Number`, `Boolean`, `Function`, `Object`, `Array`, `Symbol` 전달받은 인자를 7가지 타입으로 정의  

단순 데이터를 받기 위해서 위처럼 배열형태로 `props` 를 사용했지만  
검사를 위해서는 `json` 객체 형식을 필요로 한다.  

`props: {...}`  

아래처럼 `p1` ~ `p5` 에 어떤 데이터가 들어갈 수 있는지 지정 할 수 있다.  

```js
props: {
    p1: [Number, String], // 여러타입 허용
    p2: {type:String, required:true}, // type: 데이터타입 지정, required: 필수 유무
    p3: {type:Number, default:100}, // default 값 정의
    p4: {type:Object, default: () -> {message: 'hello'}}, // object 일경우 default를 메서드로 정의 필수
    p5: {validator:(value) -> value > 0} // 넘어온값 검사
}
```


### 양방향 바인딩

index.vue 에서 c1.vue 컴포넌트로 변수를 전달했을 때 어디서 영향을 끼치고 받는지 알아보자.  

{% highlight html %}{% raw %}
<!-- index.vue -->
<template>
  <div class="">
    <c1 :message="n"></c1>
    <h1>index 페이지 {{ n }}</h1>
    <button @click="increment">index 증가</button>
  </div>
</template>
  
<script>
import c1 from "~/components/c1.vue";

export default {
  components: { c1 },
  data() {
    return { n: 1 };
  },
  methods: {
    increment() {
      this.n += 1;
    },
  },
};
</script>
{% endraw %}{% endhighlight %}


{% highlight html %}{% raw %}
<!-- c1.vue -->
<template>
  <div>
    <h1>c1 컴포넌트 {{ message }}</h1>
    <button @click="increment">index 증가</button>
  </div>
</template>

<script>
export default {
  props: {
    message: {
      type: Number,
    },
  },
  methods: {
    increment() {
      this.message += 1;
    },
  },
};
</script>
{% endraw %}{% endhighlight %}

index 는 `n` 이란 변수를 c1 의 message 변수에 전달하고 각각의 문서에서 increment 메서드를 통해 변수를 증가시킨다.  

![nuxt3](/assets/nodejs/nuxt3.gif){: .shadow}     

하지만 위 그림을 보면 index.vue 에서만 영향을 끼친다.  

양방향 바인딩을 통해 같이 변수를 공유할 수 있도록 설정해보자.  

{% highlight html %}{% raw %}
<template>
  <div>
    <h1>c1 컴포넌트 {{ message }}</h1>
    <button @click="increment">index 증가</button>
  </div>
</template>

<script>
export default {
  props: {
    message: { type: Number },
    increment: { type: Function },
  },
};
</script>
{% endraw %}{% endhighlight %}

`increment` 메서드까지 전달해서 변수를 `index.vue` 에 정의된 `n` 변수를 실질적으로 증가시킴


### 공용 변수 정의  

`vuex store` 를 사용하면 `props` 없이 변수를 공유할 수 있다.  

`store` 디렉토리에 아래 js 파일 생성  

```js
/* store/index.js */
export const state = () => ({  // 공용 변수를 지정
    count: 0
})

export const mutations = { // 공용 변수 변화 메서드 정의 
    increment(state) {
        state.count++
    }
}
```

기존의 `index.vue`, `c1.vue` 를 `store` 를 사용하여 출력하도록 설정  

{% highlight html %}{% raw %}
<!-- index.vue -->
<template>
  <div class="">
    <c1></c1>
    <h1>index 페이지 {{ $store.state.count }}</h1>
    <button @click="$store.commit('increment')">index 증가</button>
  </div>
</template>
  
<script>
import c1 from "~/components/c1.vue";

export default {
  components: { c1 },
};
</script>
{% endraw %}{% endhighlight %}


{% highlight html %}{% raw %}
<!-- c1.vue -->
<template>
  <div>
    <h1>c1 컴포넌트 {{ $store.state.count }}</h1>
    <button @click="$store.commit('increment')">index 증가</button>
  </div>
</template>
{% endraw %}{% endhighlight %}

### mapState, mapMutations, mapGetters 

`$store.state` 키워드가 거추장 스럽다면 `vuex` 의 `mapState`, `mapMutations`, `mapGetters` 를 사용해 생략할 수 있다.  

{% highlight html %}{% raw %}
<!-- index.vue -->
<template>
  <div class="">
    <c1></c1>
    <h1>index 페이지 {{ count }}</h1>
    <button @click="$store.commit('increment')">index 증가</button>
  </div>
</template>
  
<script>
import c1 from "~/components/c1.vue";
import { mapState } from "vuex";

export default {
  components: { c1 },
  computed: mapState(["count"]),
};
</script>
{% endraw %}{% endhighlight %}


```js
// users.js
export const state = () => ({
    userInfos: [{ name: 'pjt', id: 0, age: '26' }]
})

export const mutations = {
    add(state, userInfo) {
        state.userInfos.push(userInfo)
    }
}

export const getters = {
    userInfos(state) {
        return state.userInfos
    }
}
```

{% highlight html %}{% raw %}
<!-- users/index.vue -->
<template>
  <div class="">
    <ul class="users">
      <li v-for="user in users" :key="user.id">
        id:{{ user.id }}, name: {{ user.name }}, age: {{ user.age }}
      </li>
    </ul>
    <div class="add">
      <input type="text" v-model="id" />
      <input type="text" v-model="name" />
      <input type="text" v-model="age" />
      <button type="button" @click="userAdd">추가</button>
    </div>
  </div>
</template>

<script>
import { mapMutations, mapGetters } from "vuex";

export default {
  computed: mapGetters({ users: "users/userInfos" }), // users.js 의 getters 에 정의된 userInfos 사용
  data() {
    return { name: "", age: "", id: "" };
  },
  methods: {
    userAdd() {
      let userInfo = { name: this.name, age: this.age, id: this.id };
      this.add(userInfo);
      this.name = "";
      this.age = "";
      this.id = "";
    },
    ...mapMutations({ add: "users/add" }), // users.js 의 mutations 의 add 사용
    // ... 은 methods 에 merge 하는 역할
  },
};
</script>
{% endraw %}{% endhighlight %}



## 초기화 - fetch

컴포넌트가 렌더링되기 전에 특정코드를 실행시키고 싶을때 `fetch` 메서드를 사용한다.   

위에서 사용한 `count` 변수의 시작값을 `index.vue` 생성시 `2`로 초기화 되도록 설정하자.  


```js
export const mutations = {
    increment(state) {
        state.count++
    },
    init (state, value) {
        value = value || 0 // value 가 null 이라면 0
        state.count = value
    }
}
```

초기화 함수 `init(state, value)` 정의, 2번째 매개변수인 value 에 들어오는 값으로 count 를 초기화할 예정  


```js
export default {
  components: { c1 },
  computed: mapState(["count"]),
  fetch({ store }) {
    // 매개변수의 중괄호는
    console.log("test"); // 커맨드 출력 확인
    store.commit("init", 2); // value 에 2 전달 
  },
};
```

> `fetch` 내부에선 `this` 사용이 불가능하다.(내부 `data` 사용 불가능)  