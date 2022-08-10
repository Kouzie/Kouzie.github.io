---
title: "CSS - SCSS!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - CSS

toc: true
toc_sticky: true

---

## 개요

> 참고사이트: <https://sass-lang.com/guide>

`CSS(Cascading Style Sheets)`

`SASS(Syntactically Awesome Style Sheets)`

`SCSS(Sassy CSS)`

> Syntactically Awesome: 문법적 훌륭한
> Sassy: 멋진, 건방진  

SASS, SCSS 둘다 CSS 의 문법적 약점을 보완하기 위한 방식으로 아래와 같은 문법이 존재한다.  

- 변수(Variable)  
- 중첩(Nesting)  
- 모듈화(Modularity)  
- 믹스인(Mixins)  
- 확장&상속(Extend/Inheritance)  
- 연산자(Operators)  


`SCSS` 가 가장 나중에 나온 문법이며 `CSS` 와 좀더 비슷한 문법을 가지며  
`SCSS` 사용률이 `SASS` 보다 높다.(둘이 거의 비슷한 문법을 가짐)  

`SCSS` 로 스타일시트를 작성하면 컴파일을 통해 CSS 로 변환해주는 작업을 거쳐야 한다.  

### 변수(Variable) 할당  

```scss
/* CSS */
body {
  font: 100% Helvetica, sans-serif;
  color: #333;
}

/* SCSS */
$font-stack: Helvetica, sans-serif;
$primary-color: #333;

body {
  font: 100% $font-stack;
  color: $primary-color;
}
```

### 중첩(Nesting)

```scss
/* CSS */
nav ul {
  margin: 0;
  padding: 0;
  list-style: none;
}
nav li {
  display: inline-block;
}
nav a {
  display: block;
  padding: 6px 12px;
  text-decoration: none;
}

/* SCSS */
nav {
  ul {
    margin: 0;
    padding: 0;
    list-style: none;
  }

  li { display: inline-block; }

  a {
    display: block;
    padding: 6px 12px;
    text-decoration: none;
  }
}
```

### 모듈화(Modularity)

`@use` 키워드를 사용  

```scss
/* _base.scss */
$font-stack: Helvetica, sans-serif;
$primary-color: #333;

body {
  font: 100% $font-stack;
  color: $primary-color;
}
```

```scss
/* styles.scss */
@use 'base';

.inverse {
  background-color: base.$primary-color;
  color: white;
}
```

### 믹스인(Mixins)

함수처럼 기본 파라미터와를 지정가능하고 값을 지정할 수 있다.  

```scss
/* CSS */
.info {
  background: DarkGray;
  box-shadow: 0 0 1px rgba(169, 169, 169, 0.25);
  color: #fff;
}

.alert {
  background: DarkRed;
  box-shadow: 0 0 1px rgba(139, 0, 0, 0.25);
  color: #fff;
}

.success {
  background: DarkGreen;
  box-shadow: 0 0 1px rgba(0, 100, 0, 0.25);
  color: #fff;
}


/* SCSS */
@mixin theme($theme: DarkGray) {
  background: $theme;
  box-shadow: 0 0 1px rgba($theme, .25);
  color: #fff;
}

.info {
  @include theme;
}
.alert {
  @include theme($theme: DarkRed);
}
.success {
  @include theme($theme: DarkGreen);
}
```

### 확장&상속(Extend/Inheritance)

`@extend` 키워드로 속성 집합을 상속받을 수 있다.  

```scss
/* This CSS will print because %message-shared is extended. */
%message-shared {
  border: 1px solid #ccc;
  padding: 10px;
  color: #333;
}

.message {
  @extend %message-shared;
}

.success {
  @extend %message-shared;
  border-color: green;
}

.error {
  @extend %message-shared;
  border-color: red;
}

.warning {
  @extend %message-shared;
  border-color: yellow;
}
```

### 연산자(Operators)

`math` 를 이용해  div, sin, cos, tan, random, max, min 등등 여러 가지 수학적 기능 사용 가능  

```scss
/* SCSS */
@use "sass:math";

.container {
  display: flex;
}

article[role="main"] {
  width: math.div(600px, 960px) * 100%;
}

aside[role="complementary"] {
  width: math.div(300px, 960px) * 100%;
  margin-left: auto;
}
```