---
title: "CSS - 레이아웃!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - CSS

toc: true
toc_sticky: true

---


## position

> 참고: <http://ko.learnlayout.com/position.html>
> <https://www.youtube.com/watch?v=0kA0mBvumrg>

html 요소 위치를 지정할 수 있는 속성, 부모와의 관계속에서 위치를 지정할 수 있다.  

`position` 값에 의한 `left`, `right`, `top`, `bottom` 속성 적용방식도 달라진다.  

```html
<div class="main">
  <br>
  <div>Hello1</div>
  <div>Hello2
    <div>Hello3
      <div>Hello4</div>
      <div class="static">static</div>
      <div class="relative">relative</div>
      <div class="absolute">absolute</div>
      <div class="fixed">fixed</div>
      <div>Hello</div> <!-- lv3 -->
    </div>
  </div>
  <div>Hello</div>
</div>

<style lang="scss" scoped>
.main {
  margin: 0;
}

.main div {
  border: 5px solid black;
  padding: 20px;
  font-weight: bold;
}

.static {
  position: static;
}

.relative {
  border: 5px solid red;
  background-color: rgba(255, 0, 0, 0.5);
  position: relative;
}

.absolute {
  border: 5px solid green;
  background-color: rgba(0, 255, 0, 0.5);
  text-align: center;
  /*position: absolute;*/
}

.fixed {
  border-color: blue;
  background-color: rgba(0, 0, 255, 0.5);
  text-align: right;
  /*position: fixed;*/
}
</style>
```

![css-layout-5](/assets/web/css/css-layout1.png)


### static 

`position` 의 기본값,  
`left`, `right`, `top`, `bottom` 은 비활성화 된다.  

```css
.static {
  position: static;
}
```

`display`(`block` `inline`) 모드에 따라 아래에 추가되는지 우측에 추가되는지 결정

### relative

상대좌표로 `left`, `right`, `top`, `bottom` 이 활성화 된다.  

원래 표시되어야 할 위치에서 이동한 거리에 출력된다.  

> html 요소 위치는 부모태그로부터 결정되기에 상대좌표라는 말은 사용한다.  

아래처럼 `left: 20px` 적용시 그림처럼 적용된다.  

```css
.relative {
  border: 5px solid red;
  background-color: rgba(255, 0, 0, 0.5);
  position: relative;
  left: 20px;
}
```

![css-layout-5](/assets/web/css/css-layout5.png)


### absolute

절대좌표로 `left`, `right`, `top`, `bottom` 이 활성화 된다.  

`absolute`는 다른 태그 레이아웃에 영향을 끼치지 않는다.  
다른 요소들의 위치를 결정짖지 않아 동떨어져 공중부양 하는 결과를 가진다.  

```css
.absolute {
  border: 5px solid green;
  background-color: rgba(0, 255, 0, 0.5);
  text-align: center;
  position: absolute;
}
```

![1](/assets/web/css/css-layout2.png)

`left`, `right`, `top`, `bottom` 지정시  
**`position :static` 이 아닌** 부모태그를 찾아  
해당 위치를 기준으로 이동한다.  

모든 부모태그가 `position :static` 이라면 `body` 를 기준으로 이동한다. 


```css
.absolute {
  border: 5px solid green;
  background-color: rgba(0, 255, 0, 0.5);
  text-align: center;
  position: absolute;
  left: 0;
  top: 0;
}
```

![1](/assets/web/css/css-layout3.png)

부모태그중 하나를 `position: relative` 로 변경하면 해당 부모태그를 기준으로 이동한다.  

```html
<div style="position: relative">Hello3
```

![1](/assets/web/css/css-layout4.png)


### fixed

`absolute` 와 동일하나 `left`, `right`, `top`, `bottom` 지정시 부모태그를 무시하고 `body` 기준으로 위치가 결정된다.  

또한 스크롤 이동시에도 위치가 고정되어 표시된다.  

```css
.fixed {
  border-color: blue;
  background-color: rgba(0, 0, 255, 0.5);
  text-align: right;
  position: fixed;
}
```

![1](/assets/web/css/css-layout6.png)  

### sticky

relative와 fixed를 합친 느낌의 속성,  

sticky를 적용하기 위한 **조건**

`top, bottom, left, right` 중 하나이상의 값을 필요. 즉 `position: sticky`가 제대로 동작하지 않는 경우 이처럼 방향 값의 설정 여부를 확인해야합니다.  
또한! 부모 요소들 중 어느 하나라도 `overflow: hidden` 값인 경우에도 동작하지 않으므로 이 두 가지 값을 확인해야합니다.  


### sticky와 float

맞는지는 모르겠지만 `sticky`를 사용해 스크롤을 내리면 따라오도록 하려면 밑에 출력할 요소가 있어야 합니다(당연함, 그래야 스크롤도 생기니까!)  
하지만 모슨요소가 `float: left`와 같이 떠다니는 상태일 경우 인식을 못하게 되고 `sticky`는 따라다닐 요소를 찾지 못하기 때문에 움직이지 않습니다.  

따라서 모든 레이아웃용 `div` 태그가 `float` 속성으로 떠다닐 경우   
`sticky`로 문저마지막까지 따라다기게 하고 싶다면 문서 마지막에 `<div style="clear: both"></div>`을 추가해 주어야 합니다~  


```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="">
    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
        }

        ul.nav {
            list-style: none;
            margin: 0;
            padding: 0;
            background: #333;
            position: sticky;
            top: 0;
            float: left;
            width: 25%;
            height: 100%;
            overflow: auto;
        }

        ul.nav li a {
            display: block;
            color: white;
            text-align: center;
            padding: 14px 16px;
            text-decoration: none;
        }

        ul.nav li a:hover:not(.active) {
            background: #111;
        }

        .active {
            background: #4caf50;
        }

        div.content {
            padding: 20px;
            background: #1abc9c;
            height: 1500px;
            width: 75%;
            float: left;
        }
    </style>
</head>
<body>
    <h1>Header</h1>
    <div style="background: yellow">
        <ul class="nav">
            <li><a href="#" class="active">Home</a></li>
            <li><a href="#">News</a></li>
            <li><a href="#">Contact</a></li>
            <li><a href="#">Help</a></li>
        </ul>
        <div class="content">
            <h1>Lorem ipsum dolor.</h1>
            <h2>Lorem ipsum dolor sit amet.</h2>
            <h2>Voluptate unde quae aliquid. Rem.</h2>
            <p>Lorem ipsum dolor sit amet, consectetur adipisicing.</p>
            <p>Rem minus incidunt explicabo omnis ullam atque.</p>
            <p>Minus optio voluptatum quibusdam odit voluptate fugit.</p>
            <p>Voluptatum architecto eligendi pariatur nisi maxime voluptates.</p>
            <p>Sed debitis assumenda ipsum molestiae odio aspernatur.</p>
            <p>Totam quaerat pariatur inventore officiis ducimus ex.</p>
            <p>Quia est optio voluptates eaque recusandae cupiditate.</p>
            <p>Commodi vel officia architecto quibusdam odio blanditiis!</p>
            <p>Vel nam rem eaque culpa ex repudiandae.</p>
            <p>Natus impedit necessitatibus voluptatem hic ut cumque.</p>
            <p>Quas ipsa alias neque consectetur autem vitae!</p>
            <p>Unde delectus officia laborum eius quisquam culpa.</p>
            <p>Itaque ab maiores voluptas aspernatur veritatis expedita.</p>
            <p>Assumenda atque corrupti natus excepturi debitis dolorum.</p>
            <p>Hic optio sapiente et quod quidem earum.</p>
            <p>Quae iure explicabo facilis unde blanditiis possimus.</p>
            <p>Numquam deleniti eaque nobis ipsum voluptatum debitis.</p>
            <p>Esse alias deserunt aspernatur nam quis deleniti.</p>
            <p>Est exercitationem fugit quos nobis officiis excepturi.</p>
            <p>Maxime optio hic sunt quas iusto obcaecati!</p>
        </div>
        <div style="clear: both"></div>
    </div>
</body>
</html>
```


## transform

`posistion` 속성을 통해 가운데로 가고 싶다면 부모태그의 `position`이 `relative` 여야 하고 자식태그는 `absolute`의 `top: 50%`, `left: 50%` 이면 된다.  

하지만 정확히 가운데로 위치하지 않고 약간 어긋나게 보이는데 자식태그의 기준점이 정 중앙이 아닌 `left top`이기 때문.  

![css-layout-18](/assets/web/html/css-layout-18.png){: .shadow}   

길다란 자식태그를 정 중앙에 위치하게 하고 싶다면 기준점을 `left top`이 아닌 중앙으로 옮겨주어야 한다.  

기준점을 옮길때 사용되는 속성이 `transform`  
`transform: translate(-50%, -50%);` 위쪽 -50%, 왼쪽 -50% 으로 기준점을 옮기면 중앙에 기준점이 오게된다.  

![css-layout-19](/assets/web/html/css-layout-19.png){: .shadow}   


### 중앙 정렬

지금까지 총 3가지 방법으로 중앙정렬을 할 수 있다. 
`position`과 `transform: translate(-50%, -50%);` 방금했던 중심점을 가운데로 맞추고 `left`와 `top`을 부모태그 높이, 너비의 50%로 잡는방법.

자식태그는 `margin: 0 auto;`로 수평정렬, 부모태그에서 `padding:100px;`에서 수직정렬해서 가운데를 맞추는 방법.

```html
<div class="out">
    <div class="in"></div>
</div>
```

```css
.out{
        background-color: orange;
        padding:100px;
        text-align: center;
}
.in{
        background-color: blue;
        width: 100px;
        height: 100px;
        margin: 0 auto;
}
```

텍스트의 경우 `text-align`으로 수평정렬 `padding`으로 수직정렬해도 되지만 `line-height`를 통해 라인간격으로도 맞출 수 있다.  

```html
<div class="out">test</div>
```

```css
.out{
        background-color: orange;
        /* padding:100px; */
        line-height: 100px;
        text-align: center;
}
```

그리고 `display: flex`, `justify-content`와 `align-content`를 모두 `center`속성값으로 지정하는 방법이 있다.  



### column 속성

신문처럼 한 문단을 특정 개수의 컬럼으로 나눌 수 있다.

```html
<div class="news">
    Lorem ipsum dolor sit amet, consectetur
    adipisicing elit. Voluptas repudiandae mollitia ea odit tenetur alias
    quidem ut pariatur quasi assumenda molestiae neque consequuntur
    veritatis minus repellat! Sint expedita ut inventore?
    Lorem ipsum dolor sit amet, consectetur adipisicing elit. Reprehenderit explicabo cupiditate doloremque earum
    alias ipsum, quibusdam perferendis adipisci at a neque ullam fugit pariatur ut ex quaerat magni numquam unde!
</div>
```

```css
.news {
    width: 600px;
    border: 1px solid red;
    column-count: 3;
    column-rule: 1px solid black;
}
```

`column-count`로 등분하고 `column-rule`로 나눠진 공간에 줄을 그을 수 있다.  

![css-layout-20](/assets/web/html/css-layout-20.png){: .shadow}   

문단 사이 공백을 주고 싶다면 `column-gap: 100px;`  


글 제목같은경우 문단으로 나뉘면 안되기 때문에 `column-span: all` 속성을 사용한다.  

```html
<div class="news">
    <h2 style="column-span: all">Lorem ipsum dolor sit amet, consectetur adipisicing elit. Sint
        recusandae dolorem magni provident et soluta dolores officia cumque
        asperiores totam non illum</h2>
    Lorem ipsum dolor sit amet, consectetur
    adipisicing elit. Voluptas repudiandae mollitia ea odit tenetur alias
    quidem ut pariatur quasi assumenda molestiae neque consequuntur
    veritatis minus repellat! Sint expedita ut inventore?
    Lorem ipsum, dolor sit amet consectetur adipisicing elit. Porro natus dignissimos in ut aspernatur totam
    voluptas, delectus illo suscipit ex? Asperiores tempore obcaecati est iste magni, quia corporis sit dicta.
</div>
```

![css-layout-21](/assets/web/html/css-layout-21.png){: .shadow}   
