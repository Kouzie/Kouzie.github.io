---
title:  "Vue - Vue axios, veux!"

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

## vue-router


> https://router.vuejs.org/kr/


최근 개발되는 웹 어플리케이션은 단일 페이지 어플리케이션(SPA) 구조로  
하나의 페이지에서 동적으로 렌더링을 통해 `UI/UX` 를 제공한다.  

하지만 URI 를 통해 여러페이지를 지원해야 할 경우도 있을것, Vue 의 공식 라우터 라이브러리 `vue-router` 를 사용할 수 있다.  

```
$ yarn add vue-router
```

Vue Router 플러그인을 사용하기 위해 `Vue.use` 설정  

```js
// src/main.js
import VueRouter from 'vue-router'
Vue.use(VueRouter);
```


```html
<!-- App.vue -->
<template>
  <div>
    <div class="header">
      <h1 class="headerText">(주)OpenSG</h1>
      <nav>
        <ul>
          <li> <router-link to="/home">Home</router-link> </li>
          <li> <router-link to="/about">About</router-link> </li>
          <li> <router-link to="/contacts">Contacts</router-link> </li>
        </ul>
      </nav>
    </div>

    <div class="container">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import Home from "./components/Home.vue";
import About from "./components/About.vue";
import Contacts from "./components/Contacts.vue";
import VueRouter from "vue-router";

const router = new VueRouter({
  routes: [
    { path: "/", component: Home },
    { path: "/home", component: Home },
    { path: "/about", component: About },
    { path: "/contacts", component: Contacts }
  ]
});

export default {
  name: "App",
  router
};
</script>
```

`VueRouter` 인스턴스를 통해 라우팅 경로 매핑,    

`<router-link>`, `<router-view>` 태그를 통해 라우팅 경로, 출력화면을 지정한다.  

`<router-link>` 태그는 아래처럼 `<a>` 태그로 치환되고   

```html
<ul>
  <li><a href="#/home" class="">Home</a></li>
  <li><a href="#/about" class="">About</a></li>
  <li><a href="#/contacts" class="">Contacts</a></li>
</ul>
```

`<router-view>` 태그는 라우팅된 컴포넌트의 탬플릿으로 교체된다.  

```html
<div class="container">
  ::before
  <div>
    <h1>CONTACTS</h1>
    <div>...</div>
  </div>
  ::after
</div>
```

### 동적 라우팅  - 파라미터 전달  

조건에 따라 `<router-link>` 의 라우팅 경로가 변경되어야 하며 해당 경로와 매핑된 컴포넌트도 동적으로 변화된 링크 데이터를 받아들여야 한다.  


먼저 `VueRouter` 인스턴스에 동적 라우팅 `path` 를 추가.  

`path parameter` 형식으로 url 동적 변경, 데이터 전달이 가능하다.  

```js
import ContactByNo from "./components/ContactByNo.vue";
import VueRouter from "vue-router";

const router = new VueRouter({
  routes: [
    { path: "/", component: Home },
    { path: "/home", component: Home },
    { path: "/about", component: About },
    { path: "/contacts", component: Contacts },
    { path: "/contact/:no", component: ContactByNo }
  ]
});
```

**콜론**을 통해 동적으로 변하는 파라미터 `:no` 를 라우팅 `path` 에 적용할 수 있다.  

`<router-link>` 에선 간단히 `v-bind` 를 통해 `to` 속성값을 변경하면 된다.  

```html
<!-- Contacts.vue -->
<div class="box" v-for="c in contacts" :key="c.no">
  <router-link v-bind:to="'/contact/'+c.no">{{c.name}}</router-link>
</div>
```

`<router-link>` 를 클릭해 `/contact/:no` 로 이동할 때 생성되는 `ContactByNo` 컴포넌트에선 `this.$route.params` 객체를 사용해 `:no` 파라미터를 참조할 수 있다.  

```js
// ContactByNo.vue
export default {
  name: "contactbyno",
  created: function() {
    this.no = this.$route.params.no;
  },
  ...
  ...
};
```

`path parameter` 형식 외에도 `query parameter` 형식으로도 데이터 전달 가능.  

```html
<router-link v-bind:to="{path:'/article', params: {no:234}}">article</router-link>
<!-- /article/234 -->
<router-link v-bind:to="{path:'/board', query: {page:2}}">Board</router-link>
<!-- /board?page=2 -->
```

컴포넌트에서 `query` 형식의 파라미터를 받으려면 `this.$route.query` 사용  

### 중첩 라우트

`<router-view>` 내부의 계층형식으로 `<router-view>` 를 정의하고 싶을때 중첩 라우트를 사용한다.  

아래처렴 중첩 라우트를 사용하고 싶은 객체에 `childer` 배열 정의  

```js
// App.vue
const router = new VueRouter({
  routes: [
    { path: "/", component: Home },
    { path: "/home", component: Home },
    { path: "/about", component: About },
    { path: "/contacts", component: Contacts ,
      children: [
        { path: "/contact/:no", component: ContactByNo }
      ]
    }
  ]
});
```

> `path` 맨 앞에 `root` 를 가리키는 `/` 가 없으면 부모 경로 뒤에 이어지도록 path 가 설정된다.  
> 만약 `children` 속성에 `path: ":no"` 로 설정시 부모 `path` 뒤에붙어 `/contacts/:no` 형식의 라우팅 `path` 가 구성된다.  

`Contacts.vue` 내부에 자식 `<router-view>` 정의  

```html
<!-- Contacts.vue -->
<template>
  <div>
    <h1>CONTACTS</h1>
    <div class="wrapper">
      <div class="box" v-for="c in contacts" :key="c.no">
        <router-link v-bind:to="'/contact/'+c.no">{{c.name}}</router-link>
      </div>
    </div>
    <router-view></router-view>
  </div>
</template>
```

이미 `App.vue` 에서 자식 `<router-view>` 에 대한 라우팅을 지정해주었기에 `Contacts.vue`에서 추가 설정은 필요 없다.  

주의사항으로 `<router-view>` 에 의해 생성된 컴포넌트는 처음 추가될 때에만 `created`, `mounted` 에 정의된 메서드가 호출되고  

그 이후부턴 데이터만 변경됨으로 `created`, `mounted` 메서드는 호출되지 않는다.  

`computed` 속성에서 바로 `this.$route.params.no` 를 바로 사용하거나  
아래처럼 `watch` 속성을 사용해야 한다.  

```js
export default {
  ...
  ...
  watch: {
    '$route': function(to, from) {
      this.no = to.params.no;
    }
  }
}
```

### 명명된 라우트 - `name`

지금까지 `<router-link>` 의 `to` 속성에 `path` 를 지정하고  
`VueRouter` 인스턴스에 `path` 값이 설정된 객체배열을 지정했다.  

```html
<template>
  ...
  <router-link to="/home">Home</router-link>
</template>
<script>
...
const router = new VueRouter({
  routes: [
    { path: "/home", component: Home },
  ]
});
...
</script>
```

명명된 라우트는 `path` 값이 아닌 `name` 값을 통해 라우팅한다.  
각각의 객체에 `name` 속성을 추가한다.  

```html
<template>
  ...
  <router-link v-bind:to="{name: 'home'}">Home</router-link>
</template>
<script>
...
const router = new VueRouter({
  routes: [
    { path: "/home", name: "home", component: Home },
  ]
});
...
</script>
```

> 문자열이 아닌 객체 바인딩을 위해 `v-bind` 를 사용해야 한다.  

파라미터와 쿼리를 추가할 수 도 있다.  

```html
<router-link v-bind:to="{name:'article', params: {no:234}}">article</router-link>
<!-- /article/234 -->
<router-link v-bind:to="{name:'board', query: {page:2}}">Board</router-link>
<!-- /board?page=2 -->
```

이제 복잡한 path 구조를 외우지 않고 라우팅이 가능하다.  

`name` 으로 라우팅을 진행하면 `path` 와는 전혀 라우팅이 상관 없어진다.  
그래도 `url` 을 통해 컴포넌트 구분은 해야 함으로 `path` 값은 필수이고 중복되면 안된다.  

중복만 되지 않는다면 `name` 속성만으로도 충분이 라우팅 구성이 가능하다.  

```js
const router = new VueRouter({
  routes: [
    ...
    { path: "/contacts", name: "contacts", component: Contacts ,
      children: [
        { path: ":no", name: "contactbyno", component: ContactByNo }
      ]
    }
  ]
});
```

이번엔 `path` 앞에 `/` 를 생략해 부모 `path` 에 이어지도록 설정  
`name`속성이 없었다면 `/contacts/:no` 형식으로 `router-link` 의 `to` 속성을 설정해야 한다.  

하지만 `name` 속성을 통해 라우팅 함으로 아래처럼 `router-link` 구성이 가능하다.   

```html
<template>
  <div>
    <h1>CONTACTS</h1>
    <div class="wrapper">
      <div class="box" v-for="c in contacts" :key="c.no">
        <router-link v-bind:to="{name:'contactbyno', params:{no:c.no}}">{{c.name}}</router-link>
      </div>
    </div>
    <router-view></router-view>
  </div>
</template>
```

### router.push  

`<router-link>` 태그 외에도 라우팅 기능을 해주는 메서드가 있다.  

`router.push` 메서드를 사용하면 스크립트 코드 안에서 라우팅이 가능하다.  

아래처럼 `<router-link>` 태그를 생략하고 이벤트 메서드를 등록해 라우팅을 구성   

```html
<template>
  <div>
    <h1>CONTACTS</h1>
    <div class="wrapper">
      <div class="box" v-for="c in contacts" :key="c.no">
        <span @click="navigate(c.no)" style="cursor:pointer">[{{c.name}}]</span>
        <!-- <router-link v-bind:to="{name:'contactbyno', params:{no:c.no}}">{{c.name}}</router-link> -->
      </div>
    </div>
    <router-view></router-view>
  </div>
</template>
<script>
```

`this.$router.push` 첫번째 매개변수로 명명된 라우팅으로 구성한 `name, params` 값을 가진 객체,  
두번째 매개변수로 이동후 호출될 콜백메서드를 등록한다.  

```js
import contactlist from "../ContactList";

export default {
  name: "contacts",
  data: function() {
    return {
      contacts: contactlist.contacts
    };
  },
  methods: {
    navigate: function(no) {
      if(confirm("상세보기로 이동하시겠습니까?")) {
        this.$router.push({name:"contactbyno", params:{no:no}}, function() {
          console.log("/contacts/" + no + "로 이동 완료!")
        })
      }
    }
  }
};
</script>
```

아래처럼 `path` 형식으로도 `router.push` 메서드 사용이 가능하다.  

```js
this.$router.push({path: "/contacts/" + no}, function() {
  console.log("/contacts/" + no + "로 이동 완료!")
})
```



### Navigation Guards

라우팅 이동 전, 후 에 메서드를 사용해 라우팅을 취소, 혹은 
라우팅을 제어하는 방법이 있다. `Navigation Guards` 라 한다.  


#### beforeEnter

라우트별로 라우팅이 일어나기 전에 실행되는 메서드를 정의할 수 있다.  
주로 로그인, 권한을 이동전에 확인할 때 사용한다.  

매개변수로 `to`, `from`, `next` 세 가지가 사용되며 하나씩 알아보자.  

`to`, `from` 은 이동 전, 후의 라우팅 객체를 참조  
next는 함수로 아래의 속성을 갖는다.  

```js
const router = new VueRouter({
  routes: [
    ...
    {
      path: "/contacts/:no",
      name: "contactbyno",
      component: ContactByNo,
      beforeEnter: (to, from, next) => {
        console.log("@@ beforeEnter!: " + from.path + "-->" + to.path);
        next();
      }
    }
  ]
});
```

`beforeEnter` 는 첫 컴포넌트가 생성될 때에만 호출되기 때문에 이미 생성된 컴포넌트에서 경로만 이동될 때에는 호출되지 않는다.(`/contacts/1` 에서 `/contacts/2` 로 이동할때에는 호출되지 않는다는 뜻)  

`next()` 는 꼭 호출해야 네비게이션이 중지되지 않는다.  

#### beforeRouteEnter, beforeRouteLeave, beforeRouteUpdate  

컴포넌트 내부에서도 라우팅 제어 메서드를 정의할 수 있다.  

`beforeRouteEnter` - 랜더링하는 라우트 이전에 호출되는 훅, 뷰 인스턴스 생성전에 호출되기에 this 사용 불가능  
`beforeRouteLeave` - 현재경로에서 다른 경로로 빠져나갈때 호출되는 훅  
`beforeRouteUpdate` - 이미 렌더링된(뷰 인스턴스가 생성된) 컴포넌트의 경로가 변경될 때 호출되는 훅, 컴포넌트가 첫 생성시에는 `beforeRouteEnter` 가 호출되고 그이후로 경로만 변경될 경우 `beforeRouteUpdate` 가 호출된다.  

`beforeEnter` 와 마찬가지로 `beforeRouteEnter`는 컴포넌트가 첫 생성될 때에만 호출되고 url 변경으로 인한 데이터만 변경될 때에는 호출되지 않는다.  

```js
export default {
  name: "contactbyno",
  ...
  ...
  beforeRouterEnter(to, from, next) {
    console.log("** beforeRouteUpdate");
    this.no = to.params.no;
    next();
  }
};
```

똑같이 매개변수로 `to`, `from`, `next` 를 갖는다.  

> `beforeEach` 에선 `next()` 를 생략하면 네비게이션이 진행되지 않지만 컴포넌트의 `beforeRoute...` 에선 생략해도 진행된다.  

#### router.beforeEach, router.afterEach  

`router.beforeEach`, `router.afterEach` 메서드 정의를 통해 각 라우터가 아닌 모든 라우터,  
전역수준의 `Navigation Guards` 를 위한 메서드를 정의할 수 있다.  

`afterEach` 는 라우팅이 완료된 이후에 호출되는 콜백 메서드를 정의한다.  

```js
const router = new VueRouter({...});

router.beforeEach((to, from, next) => {});
router.afterEach((to, from, next) => {});
```

1. 내비게이션 시작  
2. `beforeEach`  
3. `beforeEnter` - 이미 인스턴스가 생성되어 있다면 호출 안될 수 도 있음.  
4. `beforeRouteEnter` - 이미 인스턴스가 생성되어 있다면 호출 안될 수 도 있음.  
5. `beforeRouteUpdate`  
6. 내비게이션 완료  
7. `afterEach`  
8. `beforeRouteLeave`  

#### next

`next()` - 다음 훅 으로 이동
`next(false)` - 현재 내비게이션 중지, `from` 라우트 객체의 `url` 로 재설정  
`next("path")` - `path` 경로로 리다이렉트, 새로운 내비게이션 시작  
`next(error)` - 현재 내비게이션 중지, `route.onError()` 에 등록한 콜백 메서드가 호출됨. 

아래와 같이 `from` 라우트 객체의 데이터를 사용해 접근제어를 할 수 있다.  
지정한 `url` 에서 이동된것이 아니라면 `/home` 으로 리다이렉트 시킨다.  

```js
const router = new VueRouter({
  routes: [
    {
      path: "/contacts",
      name: "contacts",
      component: Contacts,
      children: [
        {
          path: ":no",
          name: "contactbyno",
          component: ContactByNo,
          beforeEnter: (to, from, next) => {
            console.log("@@ beforeEnter!: " + from.path + "-->" + to.path);
            if (from.path.startsWith("/contacts")) next();
            else next("/home");
          }
        }
      ]
    }
  ]
});
```

### 라우팅 모드

`history` 와 `hash` 모드가 있으며 
`VueRouter` 객체의 기본 라우팅 모드는 해시모드이다.  

해시모드를 사용하면 `url` 의 `#` 기호 이후의 경로는 페이지 내부의 이름으로 여겨져 주소창에서 변경된 `url` 다시 호출해도 페이지가 다시 로드되지 않는다.  

맨 처음 페이지가 로딩될때 마다 필요한 뷰 인스턴스와 필요한 데이터는 모두 로딩 해두었기에 추가적인 네트워크 요청도 하지 않는다.  

`hash` 모드에서 `history` 모드로 변겨하려면 아래처럼 `mode` 설정을 변경한다.  


```js
const router = new VueRouter({
  mode: 'history',
  routes: [...]
});
```

`url`에서 `#` 기호가 사라졌는지 확인  
`history` 모드도 단순 `url` 변경으로 네트워크 요청을 하지는 않지만 직접 주소창에 변경된 `url` 을 다시 요청하면 새로운 네트워크 요청이 일어난다.  

### 찾을 수 없는 라우팅 처리  

먼저 404 대신 출력할 페이지를 작성, 컴포넌트의 간단한 템플릿만 사용하자.  

```html
<!-- NotFound.vue -->
<template>
  <h1>요청하신 경로는 존재하지 않습니다</h1>
</template>
```

맨 아래 `router` 객체 마지막에 `*` 에스타링크 기호로 라우팅 되지 않은 요청을 위에 정의한 컴포넌트로 이동  

```js
const router = new VueRouter({
  mode: 'history',
  routes: [
    { path: "/home", name: "home", component: Home },
    { path: "/about", name: "about", component: About },
    ...
    ...
    { path: "*", component: NotFound }
  ]
});
```

### 라우트 속성 연결  


```html
<router-link v-bind:to="{name:'article', params: {no:234}}">...</router-link>
<!-- /article/234 -->
<router-link v-bind:to="{name:'board', query: {page:2}}">...</router-link>
<!-- /board?page=2 -->
```

컴포넌트는 전달받은 url 에서 `this.$route.params`, `this.$route.query` 형식으로 파라미터를 가져온다.  

`this.$route.params` 이 아닌 `props` 속성을 통해 파라미터를 전달 받을 수 있다.  

```js
export default {
  name: "contactbyno",
  props: ['no'],
  computed: {
    contact: function() {
      var contact = this.$store.getters.contactByNo(this.no);
      return contact;
    }
  },
};
```

컴포넌트에서 `props` 를 사용하도록 하려면 `VueRouter` 객체에 `path parameter`, `query parameter` 형식에 따라 `props` 사용 가능설정을 해주어야 한다.  


#### query parameter 형식 props 매핑

`path` 와 각종 파라미터를 속성으로 갖는 객체를 반환하는 메서드를 정의하고 `VueRouter` 객체의 `props` 속성에 할당  

```js
function connectQueryToProp(route) {
  return { no: route.query.no, path: route.path };
}

const router = new VueRouter({
  mode: "history",
  routes: [
    {
      path: "/contacts",
      name: "contacts",
      component: Contacts,
      children: [
        {
          path: "contact",
          name: "contactbyno",
          component: ContactByNo,
          props: connectQueryToProp
        }
      ]
    },
    { path: "*", component: NotFound }
  ]
});
```


#### path parameter 형식 props 매핑  

어차피 파라미터를 `path` 를 통해 넘기기에 `{param, path}`객체를 반환하는 메서드를 따로 정의할 필요가 없다.  


단 `props` 속성만 `true` 로 변경하고 `path` 속성에 파라미터명을 적용.  

```js
const router = new VueRouter({
  mode: "history",
  routes: [
    {
      path: "/contacts",
      name: "contacts",
      component: Contacts,
      children: [
        {
          path: ":no",
          name: "contactbyno",
          component: ContactByNo,
          props: true
        }
      ]
    },
    { path: "*", component: NotFound }
  ]
});
```

