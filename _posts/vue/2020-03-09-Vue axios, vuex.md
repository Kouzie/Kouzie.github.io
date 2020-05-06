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

## axios

서버와 통신하기 위한 라이브러리로 `fetch`, `superagent`, `axios` 등이 있고 vue 에서 지원하는 `vue-resource` 라는 플러그인도 있다.  

이중 `axios`가 가장 범용적이고 인기있는 라이브러리 라고 한다.  

프로젝트 생성 후 아래 명령으로 `axios` 설치

```
$ yarn add axios
```

라이브러리 형태로 로딩해서 사용할 수 도 있다.  
```js
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
```


### 프록시 서버 생성

일반적으로 vue 같은 클라이언트 서버는 단독으로 데이터를 출력하기 보단 별도의 서비스 제공자와 협력하여 클라이언트에게 데이터를 뿌리기 때문에 `CORS` 에러가 발생할 수 있다.  

![vue15](/assets/vue/vue15.png){: .shadow}   

> `Cross Origin Resource Sharing`(교차 출처 리소스 공유): 한 출처에서 실행 중인 웹 애플리케이션이 다른 출처의 선택한 자원에 접근할 수 있는 권한을 부여하도록 브라우저에 알려주는 체제. 
> https://developer.mozilla.org/ko/docs/Web/HTTP/CORS

서비스 제공자에 `axios` 를 사용해 리소스를 가져오는데 위와같은 사항때문에 때에 따라 프록시 서버 설정이 필요하다.  

많은 서비스 제공자는 `SOP(Same Origin Policy)`를 사용하기에 `CORS` 에러가 발생할 수 있다.   

서비스 서버 또한 같은 개발자가 관리한다면 `SOP` 를 사용하지 않음으로 해결이 가능하지만 아니라면  

프록시 서버를 두어 클라이언트가 자신을 통해 서비스 제공자에게 `request`할 수 있도록 한다.  

> 물론 서비스 제공자에서 `CORS` 를 지원한다면(`SOP`를 사용하지 않는다면) 프록시를 따로 둘 필요는 없다.  

```js
/* vue.config.js */
moudel.exports = {
    devServer: {
        proxy: {
            '/api': {
                target: 'http://sample.bmaster.kro.kr',
                changeOrigin: true,
                pathRewrite: {
                    '^/api': ''
                }
            }
        }
    }
}
```

`vue.config.js` 파일 생성후 위와 같이 작성,  

개발용 서버(프록시 서버)에 `/api/contacts` 로 요청하면 서비스 제공자 `sample.bmaster.kro.kr/contacts` 로의 접근을 자신을 경유해 요청 전달.



### 저수준 axios 사용

```js
axios({...})
  .then( function(response) {...} )
  .catch( function(exception) {...} )
  .finally( function() {...} );
```

위와 같은 형식으로 사용,
`aixos` 매개변수로 설정객체(`method`, `url`, `params`가 설정된) 를 전달한다.  
`axios` 로 반환되는 객체는 `Promise` 객체

```js
axios({
  method: "GET",
  url: "/api/contacts",
  params: { pageno: 1, pagesize: 5 }
})
.then((response) => {
  console.log(response);
  this.result = response.data;
})
.catch((ex) => {
  console.log("ERROR!! ", ex);
});
```

```js
axios.post('/api/contacts', {
  name: this.name, tel: this.tel, address: this.address
})
.then((response) => {
  console.log(response);
  this.result = response.data;
})
.catch((ex) => {
  console.log("ERROR!! ", ex);
});
```

또는 아래방식으로  

```js
axios({
  method: "POST",
  url: "/api/contacts",
  data: { name: this.name, tel: this.tel, address: this.address }
})
.then((response) => {
  console.log(response);
  this.result = response.data;
})
.catch((ex) => {
  console.log("ERROR!! ", ex);
});
```

이런식으로 `axios` 는 아래와 같은 메서드들은 제공한다.  

```js
axios.get(url [,config]);
axios.post(url [,config]);
axios.delete(url [,config]);
axios.put(url [,config]);
```

```js
axios.head(url [,config]);
axios.options(url [,config]);
```

### Vue 와 `axios`


`axios` 는 여러 컴포넌트에게 자주 사용되기 때문에 각 컴포넌트에 `import` 하기 보다 `main.js` 에 한번만 `import` 해서 사용하는 것이 깔끔하다.  

또한 프로토 타입으로 등록해 다른 뷰 인스턴스에서 쉽게 접근할 수 있도록 설정한다.  

```js
// main.js
import axios from 'axios'
Vue.prototype.$axios = axios;
...
...
```

이제 다른 Vue 인스턴스 내에서 `import` 없이 아래와 같이 사용할 수 있다.

```js
this.$axios.put(...)
```


## Vuex - 뷰 이벤트 상태관리자

Vue 에선 `MVVM` 모델을 사용중이다. 각 컴포넌트마다 자신의 데이터(상태)를 가지고 있으며 변경이 일어날때 마다 뷰 감시자(`watcher`)가 랜더링한다.  

> 뷰 에선 `props` 속성에 전달받은 데이터를 참조시키고 이를 `State`(상태)라 한다.  

그리고 데이터 변경을 위해선 자식 컴포넌트가 이벤트를 발송하고 부모컴포넌트가 자식에게 할당된 데이터를 수정하는 작업을 수행한다. (혹은 이벤트 버스를 사용해 이벤트를 전달해 데이터 수정)  

컴포넌트 구조가 복잡해지면 질수록 유지보수가 복잡해 진다.  

간단한 어플리케이션의 경우 이벤트버스나 이벤트처리로 데이터관리를 해도 상관 없지만  
복잡해 질수록 컴포넌트의 상태를 결정할 데이터를 한곳에 모아 집중관리할 필요가 있다.  

이를 위해 `Global` 상태 정보객체(전역 데이터 저장객체) 를 사용할 수 있다.  

데이터를 `Global` 하게 설정해 컴포넌트의 상태를 관리하게 되면 상태 추적이 어렵워 지는 문제가 발생하는데 `Vuex` 같은 상태관리 라이브러리를 사용해 해결할 수 있다.

> `Vuex`를 사용하면 상태관리를 `Global` 하게하면서 상태 추적, 컴포넌트별 접근 제한이 가능.  

```
$ yarn add vuex
```


### 단방향 데이터 흐름

`Vuex`의 아키텍처  

![vue14](/assets/vue/vue14.png){: .shadow}   

이벤트 발생에서 데이터 흐름을 보면 모두 한방향으로만 흐른다.  

1. `Vue Components` 가 `Action` 일으킴  
2. `Action` 에서 외부 API 호출, `Mutation`(변이) 일으킴  
3. `Mutation` 은 `Action` 의 결과를 받아 `State` 변경  
4. `Mutation` 에 의해 변경된 `State`는 랜더링되고 다시 `Vue Components`에 바인딩  

`Vuex` 는 이중 `Action`, `Mutation`, `State` 를 관리.  

## Vuex - Store (저장소)

중앙 관리할 `State`(데이터)들이 저장될 `Store`(저장소) 객체 정의  

```js
// /store/index.js
import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    ...
    ...
  },
  mutations: {
    addTodo: (state, payload) => {
      ...
      ...
    }
    ...
    ...
  }
})
```

> `Vue.use(Vuex)`: `Vuex` 플러그인을 사용할 수 있도록 설정   

모든 컴포넌트의 `State` 를 `Store` 에서 관리할 필요는 없다. 여러 컴포넌트가 공유할 데이터만 저장하면 된다.  

> `state`, `mutataions` 외에도 `actions`, `getters`, `module` 속성이 있는데 하나씩 알아보자.  


`this.$store` 형식으로 각 컴포넌트에서 `store` 접근 지원을 위해 뷰 인스턴스 생성시 아래처럼 설정  

```js
//main.js
new Vue({
  store,
  render: h => h(Todolist),
}).$mount('#app')
```

> `Vuex` 를 사용하면 단순 공유 데이터 생성뿐 아니라 상태 자체를 저장할 수 있기 때문에 크롬 디버깅창에서 확인 가능하다.  

### State

Store 생성의 목적, 상태(데이터) 를 저장하는 속성이다.  

컴포넌트에서 `store.state` 에 접근하려면 아래처럼 사용.  

```js
computed: {
  todolist: function() {
    return this.$store.state.todolist;
  }
}
```

### Mutation

`mutations`(변이) 속성에는 공유데이터 `state`를 **변경시키는 메서드를 정의**한다.  
2개의 매개변수, 공유데이터인 `state` 와 변경시 필요한 정보 `payload` 가 필요하다.  

나중에 각 컴포넌트에서 공용데이터 `state`변경을 위해 `mutaion`에 정의된 함수를 호출하는데 아래와 같은 방식을 사용한다.   

```js
const store = new Vuex.Store({
  state: {
    ...
    ...
  },
  mutations: {
    addTodo: (state, payload) => {
      ...
      ...
    },
    deleteTodo: (state, payload) => {
      ...
      ...
    }
    ...
    ...
  }
})
```

```js
this.$store.commit(Constant.DONE_TOGLE, { id: id });
this.$store.commit(Constant.DELETE_TODO, { id: id });
```

`commit` 은 db에서 흔히 사용되는 명령어로 변경되는 상태값을 실제 데이터 저장소에 동기화 시키는 역할을 한다.  

`mutations` 도 똑같이 변경할 데이터, 변경을 유발하는 데이터 `payload` 를 `state`를 변화시키는 작업을 **동기적으로** 수행한다  

> Vuex에선 상태관리를 위해 모든 변이에 대해 상태의 "이전" 및 "이후" 스냅 샷을 캡처 해야하기 때문에 콜백형식의 비동기 처리방식은 변이에 적용 불가능하다.  

### Getter  

```js
computed: mapState(['todolist'])
```

`mapState`를 쓰면 `store` 에 저장된 `state` 안의 데이터를 가져올 수 있다.  

만약 단순히 `state` 안의 데이터를 가져오는 것이 아닌  
`filter`나 `map` 과같은 `state` 안의 **특정 데이터만** 연산을 통해 가져오고 싶다면  

모든 컴포넌트에 우선 `state`를 가져와 원하는 데이터를 얻기위한 해당 연산과정을 작성해도 되지만  

둘 이상의 컴포넌트가 같은 로직의 연산과정을 통해 데이터를 가져와야 한다면 `getters` 를 통해 `Store` 객체에 특정 데이터만 반환해주는 메서드를 작성하는 것이 효율적이다.   

```js
const store = new Vuex.Store({
  state: {
    todos: [
      { id: 1, text: '...', done: true },
      { id: 2, text: '...', done: false }
    ]
  },
  getters: {
    doneTodos: (state, getters) => {
      return state.todos.filter(todo => todo.done)
    }
  }
})
```

또한 두번째 매개변수를 사용해 `getters` 에 정의된 다른 메서드도 사용 가능하다.  

```js
computed: {
  todolist: function() {
    return this.$store.getters.doneTodos;
  }
}
```

위처럼 이미 저장된 데이터를 기반으로 필터링해 가져올 수도 있지만  
동적으로 변하는 컴포넌트에서 인자값 전달하여 필터링된 데이터를 가져오고 싶을 수 도 있다.  

```js
const store = new Vuex.Store({
  getters: {
    getTodoById: (state) => (id) => {
      return state.todos.find(todo => todo.id === id)
    }
  }
})
```

```js
store.getters.getTodoById(2)
```

화살표 함수가 익숙치 않다면 아래 형식을 참고  

```js
const store = new Vuex.Store({
  getters: {
    getTodoById: function (state) {
      return function(id) {
        return state.todos.find(todo => todo.id === id)
      }
    }
  }
})
```

즉 store 인스턴스가 생성될때 매개변수 1개를 인자로 받는 함수참조값을 `getTodoById` 에 매핑한다.  

### Action

`store` 의 `mutations` 속성을 통해 `state` 를 변경하는 메서드를 작성하고 호출했다.  

`mutations` 동기적으로만 처리하며 비동기적으로 처리하려면 `actions` 속성을 사용해야 한다.  


```js
const store = new Vuex.Store({
    state: {...},
    mutations: {
        [Constant.DONE_TOGGLE]: (state, payload) => {
            var index = state.todolist.findIndex((item) => item.id === payload.id);
            state.todolist[index].done = !state.todolist[index].done;
        },
    },
    actions: {
        [Constant.DONE_TOGGLE]: (store, payload) => {
            console.log("doneTodo!!!", payload);
            store.commit(Constant.DONE_TOGGLE, payload);
        },
    }
})
```

이런식으로 `actions` 속성을 정의해놓고 호출되면 `store.commit` 을 통해 `mutations` 에 정의된 메서드를 호출한다.  

```js
methods: {
  doneToggle: function(payload) {
    //this.$store.commit(Constant.DONE_TOGGLE, payload);
    this.$store.dispatch(Constant.DONE_TOGGLE, payload);
  }
}
```

위처럼 기존의 `mutations` 를 호출하던 `commit` 을 `actions` 속성의 메서드를 호출하도록 `dispatch` 로 변경  

> 비동기 처리는 시간이 오래걸리는 network 작업과 동반되어야 효율적이다. `axios` 와 같이 자주 사용된다.


`actions` 에 정의된 메서드의 매개변수를 확인하면 `store`, `payload` 를 매개변수로 받는다.

`mutations` 의 메서드는 인자로 `state` 를 받지만  
`actions` 의 메서드는 인자로 `store` 를 받는다.  

즉 `actions` 에선 상태(`state`), 변이(`mutation`), `getter`, 또다른 `action` 모두 이용 가능하다.  

하나의 액션을 호출하고 해당 액션 안에서 또다른 액션, 변이를 추가 호출하거나 여러개의 액션, 변이를 추가 호출할 수 있다.

### 헬퍼 메서드 (컴포넌트 바인딩 헬퍼 메서드)

각 컴포넌트들은 공유데이터 `store.state` 를 바로 사용하거나,  
때에 따라선 `store.getters` 를 사용해 필터링해 가져온다.    

공유데이터 `store.state` 를 변경하기 위해 `store.mutations` 속성에 각종 메서드들을 정의하고  
`commit` 함수를 사용해 호출한다.  

정의된 `mutations` 내부 메서드들은 `actions` 에서 호출된다.

![vue16](/assets/vue/vue16.png){: .shadow}   

최종적으로 위와 같은 형식을 갖춘다.  

각 컴포넌트에서 `this.$store` 를 통해 `state`, `mutations`, `actions`, `getter` 를 사용하는데  
매우 반복적인 작업들이다.  

헬퍼메서드를 사용하면 간단하게 `state`, `mutations`, `actions`, `getter` 를 바인딩해서 사용할 수 있다.(코드 생략)  

#### mapState

지금까지 `store.state` 를 `data`, `computed` 객체 속성과 매칭시키기 위해 `function...return` 을 사용해왔다.  

```js
computed: {
  todolist: function() {
    return this.$store.state.todolist;
  }
}
```

`mapState` 메서드를 사용하면 단축 가능하다.  

먼저  `vuex` 에서 `mapState` `import`

```js
import {mapState} from 'vuex';
```

위의 코드를 아래처럼 변경  

```js
computed: mapState(['todolist'])
```

자동으로 `computed` 객체내부의 속성명이 `todolist`로 고정되기에 속성명을 변경하고 싶다면 아래와 같이 설정할 수 도있다.    

```js
computed: mapState({
  todolist2: (state) => state.todolist
})
```

#### mapMutations

`store.mutations` 속성에 정의된 메서드를 호출하기 위해 아래처럼 `commit` 를 사용해왔다.  

```js
methods: {
  deleteTodo: function(id) {
    this.$store.commit(Constant.DELETE_TODO, { id: id });
  }
}
```

어차피 `commit` 의 첫번째 매개변수는 `mutations` 속성의 이름, 두번째 매개변수는 전달할 객체(`payload`)가 항상 고정적으로 들어간다.  

이런 지루한 과정을 `mapMutations` 함수로 생략하자.  

`mapState`에 이어 `mapMutations`도 `import`

```js
import {mapState, mapMutations} from 'vuex';
```

```js
methods: {
  ...mapMutations(['deleteTodo'])
}
```

`deleteTodo` 메서드를 사용하면 `mutations`속성에 정의된 `deleteTodo` 가 호출된다.  

`mapState`와 마찬가지로 메서드 명이 `mapMutations` 에서 사용한 문자열로 고정되기에 변경하고 싶다면 배열이 아닌 아래처럼 객체형식으로 전달해야 한다.  

```js
methods: {
  ...mapMutations({
    removeTodo: Constant.DELETE_TODO
  })
}
```

`mapState`, `mapMutations` 모두 일반적인 호출방식과 사용법만 조금 다를뿐 기능은 동일하다.  

#### mapGetter


```js
import Vue from 'vue';
import Vuex from 'vuex';
import Constant from '../Constant.js';

Vue.use(Vuex);

const store = new Vuex.Store({
    state: {
      currentRegion: "all",
      countries : [{ no:1,  name : "미국", capital : "워싱턴DC", region:"america" },...]
    },
    getters : {
        countriesByRegion(state) {
            if (state.currentRegion == "all") {
                return state.countries;
            } else {
                return state.countries.filter(c => c.region==state.currentRegion);
            }
        }
    }, 
    mutations: {
        [Constant.CHANGE_REGION] : (state, payload) => {
            state.currentRegion = payload.region;
        }
    }
})

export default store;
```

`getters` 속성에 `countriesByRegion` 이라는 `state.currentRegion` 값에 따라 특정 데이터만 반환하는 메서드를 정의, `state.countries` 값이 `filter` 를 통해 달라진다.  

일반적으로 `getters` 에 정의한 `countriesByRegion` 메서드로 반환된 데이터를 뷰 컴포넌트에서 등록하려면 아래 처럼 설정해왔다.  

```js
export default {
  name: "CountryList",
  computed: {
    countries: function() {
      return this.$store.getters.countriesByRegion;
    }
  }
}
```

`mapGetters` 를 사용하면 아래처럼 단축 가능하다.  

```js
import { mapGetters } from "vuex";
export default {
  name: "CountryList",
  computed: mapGetters({countries: "countriesByRegion"})
};
```

#### mapActions

`mapMutations` 과 마찬가지로 `mapActions`를 통해 `actions`와 바인딩시켜 코드 생략이 가능하다.  

```js
methods: {
  // doneToggle: function(payload) {
  //   this.$store.dispatch(Constant.DONE_TOGGLE, payload);
  // }
  ...mapActions([Constant.DONE_TOGGLE])
}
```


### Module

![vue17](/assets/vue/vue17.png){: .shadow}   

> https://vuex.vuejs.org/kr/guide/modules.html

각 컴포넌트가 공유하는 데이터 `state`, 그리고 `state` 를 관리하는 `mutations`, `actions` 등 을 정의하였다.  

어플리케이션이 커지면 커질수록 컴포넌트는 많아지고 컴포넌트들이 공유할 데이터도 다양해진다.  

각 컴포넌트들이 하나의 공유데이터만을 사용하는 것이 아닌 모듈형식으로 state 를 공유 관리할 수있도록 하는것이 modules 속성  

```js
const module1 = {
    state: {...},
    mutations: {...},
    actions: {...},
    getters: {...}
}
const module2 = {
    state: {...},
    mutations: {...},
    actions: {...},
    getters: {...}
}

const store = new Vuex.Store({
    state: {...},
    mutations: {...},
    actions: {...},
    moduls: {
      m1: module1,
      m2: module2
    }
})
```

이제 모든 `actions`, `mutations`, `state` 를 꾸역꾸역 집어넣을 필요 없이 별도의 객체 형식으로 계층형으로 관리할 수 있다.  

모듈 방식을 사용해도 기존의 액션과 변이 메서드 호출 방식(`commit`, `dispatch`) 을 사용하면 된다.  

또한 각 모듈에서 아래 방법을 통해 매개변수를 통해 `rootState`, `rootGetters` 에 접근할 수 있다.  

반면 루트 `Store`(저장소) 에선 모듈의 상태에 접근은 불가능하다.  

```js
// 모듈 getters 에서 root state 접근하기  
modules: {
  ...
  getters: {
    someGetter (state, getters, rootState, rootGetters) {
      getters.someOtherGetter // -> 'foo/someOtherGetter'
      rootGetters.someOtherGetter // -> 'someOtherGetter'
    },
    someOtherGetter: state => { ... }
  }
}
```

```js
// 모듈 actions 에서 root state 접근하기 
modules: {
  ...
  actions: {
    doneToggle: (store, payload) => {
        store.rootState.someData;
        store.rootGetters.someMethods;
        ...
    },
  }
}
```

이외에도 모듈 관련된 다양한 문법이 있으니 위의 url 참고  

