---
title: "CSS - 애니메이션 transform!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - CSS

toc: true
toc_sticky: true

---

## transform

html 요소 **변환(transform)**을 위한 속성  

> 여기서 변환은 이동, 회전, 크기변경, 기울기변경 등을 뜻한다. 
> 
> web으로 표시할수 있는 변환에는 2d변환, 3d변환(webGL 라이브러리)이 있는데 여기선 2d 변환만을 확인한다.  
> 3d변환 참조: <https://developer.mozilla.org/en-US/docs/Web/CSS/transform-function/rotate3d>


```
IE: -ms-
opera: -webkit-
safari: -webkit-
chrome: -webkit-
firefox: -moz-
```

변환을 위한 메서드들로는 다음과 같은 함수가 있다.  

- `translate[translateX, translateY]`  
- `rotate`  
- `scale`  
- `skew[skewX, skewY]`  
- `martix`  

### translate

위치변환으로 가장 기본적은 변환 방법이다.  

```css
div {
    width: 300px;
    height: 100px;
    border: solid 1px black;
    background-color: yellow;
}
div {
    transform: translate(50px, 100px);
    -ms-transform: translate(50px, 100px);
    -webkit-transform: translate(50px, 100px);
}
```

x축 50px, y축 100px 이동시킨다.  
다른 레이아웃에 영향을 줌.  

### rotate()

회전하는 변환으로 애니메이션 효과와 같이 사용하면 있어보인다.  

```css
div {
    transform: rotate(20deg);
    -ms-transform: rotate(20deg);
    -webkit-transform: rotate(20deg);
}
```
중심을 기준으로 회전하기 때문에 예상하던 모양이 나오지 않는다면 회전축 위치를 바꿀 수 있다.

`transform-origin: 0 0;`  
회전중심을 left, top(왼쪽 꼭지점)으로 변경한다. 

```css
body {
    background-color: #e9e9e9;
    margin: 30px;
}

div.polaroid {
    width: 284px;
    padding: 10px 10px 20px 10px;
    border: solid 1px #bfbfbf;
    box-shadow: 10px 10px 5px #aaa;
    background-color: #ddd;
    transition: 0.3s;
}

div.polaroid:hover {
    tranform: rotate(0);
    -webkit-transform: rotate(0);
}
div.rotate_right {
    float: left;
    tranform: rotate(7deg);
    -ms-transform: rotate(7deg);
    -webkit-transform: rotate(7deg);
}

div.rotate_left {
    float: left;
    tranform: rotate(-8deg);
    -ms-transform: rotate(-8deg);
    -webkit-transform: rotate(-8deg);
    z-index: 1;
}
```

```html
<div class="polaroid rotate_right">
    <img src="/WebPro/css/images/cinqueterre.jpg" alt="" width="284"
        height="213">
    <p class="caption">Lorem ipsum dolor sit.</p>
</div>
<div class="polaroid rotate_left">
    <img src="/WebPro/css/images/pulpitrock.jpg" alt="" width="284"
        height="213">
    <p class="caption">Lorem ipsum dolor sit.</p>
</div>
...
```

![css-animation-3](/assets/web/html/css-animation-3.png){: .shadow}  

이런식으로 사진을 기울여서 출력하고 마우스를 `hover`하면 `rotate`를 다시 0으로, `transition: 0.3s;`과 같이 설정하면 볼만하다.  


### skew()

skew의 뜻은 `비스듬한`이다. 기울기변환하는 함수, 네모모양의 div를 기울이면 평행사변형이 된다.  
```css
div {
    transform: skewX(20deg); 
    -ms-transform: skewX(20deg);
    -webkit-transform: skewX(20deg);
}
```
![css-animation-3](/assets/web/html/css-animation-3.png){: .shadow}  
x축을 기준으로 20도 만큼 기울임

-90 ~ 90 도까지 기울일 수 있다. 기울기가 90도 이상일 경우 화면에 출력되지 않는다. (89도가 되면 선처럼 출력됨)

### scale()

크기변환하는 함수, 확대와 축소 기능을 가지고 있음.  
```css
div {
    transform: scale(2, 3); 
    -ms-transform: scale(2, 3);
    -webkit-transform: scale(2, 3);
}
```
`width`는 2배, `height`는 3배 확대한다. 

### matrix

이동, 회전, 기울임, 확대를 한번에 2가지 이상 적용하고 싶다면 `matrix`함수를 사용해야 한다.  
`transform`으로 두 종류의 함수를 같이사용하면 마지막에 사용한 것으로 덮어 씌어짐  

```css
div {
/* matrix(scaleX, skewX, skewY, scaleY, translateX, translateY) */
    transform: matrix(1, -0.3, 0, 1, 0, 0);
    -ms-transform: transform: matrix(1, -0.3, 0, 1, 0, 0);
    -webkit-transform: transform: matrix(1, -0.3, 0, 1, 0, 0);
}
```
`matrix(scaleX, skewX, skewY, scaleY, translateX, translateY)`  
`matrix` 매개변수로 총 6개가 들어가며 순서는 위와 같다.  
~~알고만 있자~~


### 로딩이미지

```css
.loader {
    border: solid 16px #f3f3f3;
    border-top: solid 16px #3498db;
    width: 120px;
    height: 120px;
    border-radius: 50%;  
    animation: spin 1.5s linear infinite;
    -webkit-animation: spin 1.5s linear infinite;
}
```

`border` 16px 짜리 두꺼운 div태그 `<div class="loader"></div>`를 `border-radius: 50%;`를 통해 원형으로 만든다.  

![css-animation-6](/assets/web/html/css-animation-6.png){: .shadow}  

이런 원이 만들어지는데 `transform: rotate()` 함수를 통해 돌리기만 하면 된다.  

```css
@keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}
@-webkit-keyframes spin {
    0% {
        transform: rotate(0deg);
    }
    100% {
        transform: rotate(360deg);
    }
}
```

### 이미지 떨림

이미지를 진동시키려면 다음 애니메이션 효과를 적용
```css
@keyframes shake {
    0%{
        transform: translate(1px, 1px) rotate(0deg);
    }
    10%{
        transform: translate(-1px, -1px) rotate(1deg);
    }
    20%{
        transform: translate(-2px, 1px) rotate(-1deg);
    }
    30%{
        transform: translate(1px, 2px) rotate(0deg);
    }
    40%{
        transform: translate(-1px, 1px) rotate(1deg);
    }
    50%{
        transform: translate(1px, -1px) rotate(-2deg);
    }
    60%{
        transform: translate(2px, 1px) rotate(1deg);
    }
    70%{
        transform: translate(-1px, 2px) rotate(2deg);
    }
    80%{
        transform: translate(2px, -1px) rotate(1deg);
    }
    90%{
        transform: translate(-1px, 2px) rotate(0deg);
    }
    100%{
        transform: translate(2px, 1px) rotate(-1deg);
    }
}

img:hover {
    box-shadow: 0 0 2px 1px rgba(0, 140, 186, 0.5);
    animation: shake 0.5s infinite;
}
```
1, 2px정도 좌우로 이동하면서 -2 ~ 2 도 사이로 기울임이 반복된다.   

hover하면 그림자와 함께 떨림을 0.5초 동안 무한반복시킨다.  