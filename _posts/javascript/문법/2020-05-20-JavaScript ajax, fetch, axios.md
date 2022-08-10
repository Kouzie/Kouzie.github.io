---
title:  "JavaScript ajax, fetch, axios"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - javaScript

tags:
  - javaScript

toc: true
toc_sticky: true

---

## 개요 

서버와 통신하기 위한 라이브러리로 `fetch`, `superagent`, `axios` 등이 있다.  


## JSON

ES6 의 JSON 라이브러리 사용법을 설명한다.  

### JSON.stringify, JSON.parse

```js
const obj = { name: 'Lee', age: 20, alive: true, hobby: ['traveling', 'tennis'] };
const json = JSON.stringify(obj);
const prettyJson = JSON.stringify(obj, null, 2); // 들여쓰기 두칸
console.log(json);
console.log(prettyJson);
/*
{"name":"Lee","age":20,"alive":true,"hobby":["traveling","tennis"]}
{
  "name": "Lee",
  "age": 20,
  "alive": true,
  "hobby": [
    "traveling",
    "tennis"
  ]
}
*/

const jsonObj = JSON.parse(json);
```

## Ajax(Asynchronous JavaScript and XML)

비동기 방식으로 데이터를 요청하고, 웹페이지를 동적으로 갱신하는 프로그래밍 방식  

`WebAPI` 인 `XMLHttpRequest` 객체 기반으로 동작함  

```js
// XMLHttpRequest 객체 생성
const xhr = new XMLHttpRequest();

// HTTP 요청 초기화
xhr.open('GET', '/users');

// HTTP 요청 헤더 설정
// 클라이언트가 서버로 전송할 데이터의 MIME 타입 지정: json
xhr.setRequestHeader('content-type', 'application/json');

// HTTP 요청 전송
xhr.send();


// readystatechange 이벤트는 HTTP 요청의 현재 상태를 나타내는 readyState 프로퍼티가 변경될 때마다 발생한다.
xhr.onreadystatechange = () => {
  // readyState 프로퍼티는 HTTP 요청의 현재 상태를 나타낸다.
  // readyState 프로퍼티 값이 4(XMLHttpRequest.DONE)가 아니면 서버 응답이 완료되지 상태다.
  // 만약 서버 응답이 아직 완료되지 않았다면 아무런 처리를 하지 않는다.
  if (xhr.readyState !== XMLHttpRequest.DONE) return;

  // 정상적으로 응답된 상태라면 response 프로퍼티에 서버의 응답 결과가 담겨 있다.
  if (xhr.status === 200) {
    console.log(JSON.parse(xhr.response));
  } else {
    console.error('Error', xhr.status, xhr.statusText);
  }
};

```

```js
// XMLHttpRequest 객체 생성
const xhr = new XMLHttpRequest();

// HTTP 요청 초기화
xhr.open('POST', '/users');

// HTTP 요청 헤더 설정
// 클라이언트가 서버로 전송할 데이터의 MIME 타입 지정: json
xhr.setRequestHeader('content-type', 'application/json');

// HTTP 요청 전송
xhr.send(JSON.stringify({ id: 1, content: 'HTML', completed: false }));
```

## fetch  

`Ajax` 와 마찬가지로 `WebAPI` 를 지원하기위한 라이브러리  

`Promise` 기반으로 작성되어 좀더 사용하기 편하다.  

```js
fetch('https://jsonplaceholder.typicode.com/todos/1')
  .catch(err => console.error(err))
  .then(response => response.json())
  .then(json => console.log(json));
  // {userId: 1, id: 1, title: "delectus aut autem", completed: false}
```

POST 사용방법은 아래와 같다.   

```js
fetch(url, {
  method: 'POST',
  headers: { 'content-Type': 'application/json' },
  body: JSON.stringify(payload)
});
```

```js
const request = {
  get(url) {
    return fetch(url);
  },
  post(url, payload) {
    return fetch(url, {
      method: 'POST',
      headers: { 'content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  },
  patch(url, payload) {
    return fetch(url, {
      method: 'PATCH',
      headers: { 'content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  },
  delete(url) {
    return fetch(url, { method: 'DELETE' });
  }
};
```


## axios

`Promise` 객체 기반의 `WebAPI` 를 지원하기위한 라이브러리  

프로젝트 생성 후 아래 명령으로 `axios` 설치

```
$ yarn add axios
```

라이브러리 형태로 로딩해서 사용할 수 도 있다.  

```html
<script src="https://unpkg.com/axios/dist/axios.min.js"></script>
```

`axios` 로 반환되는 객체는 `Promise` 객체로 아래와 같이 사용  

```js
axios({...})
  .then( function(response) {...} )
  .catch( function(exception) {...} )
  .finally( function() {...} );
```

`axios` 에 매개변수로 형식을 전달하던지 별도 정의된 메서드를 호출한다.  

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
  name: this.name, 
  tel: this.tel, 
  address: this.address
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

아래와 같은 메서드들은 제공한다.  

```js
axios.get(url [,config]);
axios.post(url [,config]);
axios.delete(url [,config]);
axios.put(url [,config]);

axios.head(url [,config]);
axios.options(url [,config]);
```
