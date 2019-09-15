---
title:  "Flutter - Design  - Work with tabs!"

read_time: false
share: false
author_profile: false
classes: wide

categories:
  - flutter

tags:
  - flutter
  - cookbook


toc: true

---

## Work with tabs

> https://flutter.dev/docs/cookbook/design/tabs

### DefaultTabController   

탭 생성은 `DefaultTabController` 를 생성하는 것 으로부터 시작한다.  


상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > DefaultTabController`  

탭을 클릭하면 화면이 이동하기 때문에 `StatefulWidget`를 상속한다.  

탭을 설정할 수 있는 위젯이라 생각하면 된다.  
똑같이 `appBar`를 설정하고 `body`를 설정한다.  

단 일반적인 `StatefulWidget`와는 다르게 `AppBar`에는 `TabBar`를, `body`에는 `Widget`대신 `TabBar`를 지정한다.  

일반적으로 body에는 `StatelessWidget`혹은 `StatefulWidget`을 상속하는 클래스를 생성자로 집어넣지만 `TabBarView(children[...])`를 집어넣는다.   


## 탭 생성 순서  

### 1. Create a TabController, Create the tabs.   

```js
DefaultTabController(
  length: 3,
  child: Scaffold(
    appBar: AppBar(
      bottom: TabBar(
        tabs: [
          Tab(icon: Icon(Icons.directions_car)),
          Tab(icon: Icon(Icons.directions_transit)),
          Tab(icon: Icon(Icons.directions_bike)),
        ],
      ),
    ),
  ),
);
```

`DefaultTabController`를 생성자로 생성하고 `StatefulWidget` 처럼 AppBar설정,  
그리고 `bottom`에 `TabBar`설정   


![flutter9]({{ "/assets/flutter/flutter9.png" | absolute_url }}){: width="400" }  

`TabController`가 우리가 클릭한 텝의 순번에 따라 지정한 탭뷰로 이동시켜주는 역할은 한다.(기본 애니메이션 처리는 덤 - 왼쪽 오른쪽 이동)  

## TabBar

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > TabBar`

일종의 `StatefulWidget`를 상속받는 위젯, 탭을 누르면 해당 탭에 표시되기 때문,   

생성자 매개변수로 `@required List<Widget> tabs` 를 요구하며 `TabController controller`를 통해 애니메이션 설정도 가능하다.  

> https://api.flutter.dev/flutter/material/TabBar-class.html

### 2. Create content for each tab.  

탭에 맞는 `TabBarView`(탭 뷰들)를 생성한다.  

```js
body: TabBarView(
  children: [
    Icon(Icons.directions_car),
    Icon(Icons.directions_transit),
    Icon(Icons.directions_bike),
  ],
),
```

![flutter10]({{ "/assets/flutter/flutter10.png" | absolute_url }}){: width="400" }  


#### TabBarView

탭에 해당하는 페이지, 스크린 위젯을 `TabBarView`로 사용하면 된다.  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > TabBarView`  

생성자에서 필수로 `@required List<Widget> children` 리스트 형태의 위젯을 요구한다.  

위에선 Icon 위젯을 View로 지정하였지만 아래처럼 `StatelessWidget` 클래스를 지정해도 된다.  

```js
body: TabBarView(
  children:[
    Page1(),
    Page2(),
    Icon(Icons.directions_bike),
  ],
),
```

> Page1과 Page2는 아래 링크 참고
> https://kouzie.github.io/flutter/Flutter-Animation-Animate-a-page-route-transition/#flutter-cookbook---animate-a-page-route-transition
