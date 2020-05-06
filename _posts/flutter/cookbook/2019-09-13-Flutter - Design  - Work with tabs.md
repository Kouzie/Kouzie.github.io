---
title:  "Flutter - Design  - Work with tabs!"

read_time: false
share: false
author_profile: false
# classes: wide

categories:
  - flutter

tags:
  - flutter
  - cookbook


toc: true
toc_sticky: true

---

# Work with tabs

> https://flutter.dev/docs/cookbook/design/tabs

머티리얼 디자인의 일반적인 가이드 라인, 탭을 사용하는 디자인이다.  
`Scaffold` 를 감싸 탭을 구성하는 방식으로 사용된다.  

> 물론 `Scaffold` 만 사용해도 탭 비슷한 기능을 구현할 수는 있다.  

1. Create a TabController.  
2. Create the tabs.  
3. Create content for each tab.  

## 1. Create a TabController.

탭을 클릭해 발생하는 이벤트를 제어하려면 `TabController` 를 사용해야 한다.  

위의 과정을 진행하기 전에 `Scaffold`를 감싸는 `DefaultTabController` 를 먼저 생성하자.  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > DefaultTabController`  

탭을 이벤트를 처리하며 변경되는 `StatefulWidget`이라 생각하면 된다.  

일반적인 `Scaffold` 위젯은 아래와 같이 구성한다.  

```dart
Scaffold(
  appBar: _buildAppBar();
  body: _buildBody();
)
```

`appBar` 속성에는 `AppBar()` 메서드로 생성된 앱바 위젯이  
`body` 속성에는 출력할 위젯(`List`, `Cetner`, `GridView` 등)들이 들어가게 된다.  

`DefaultTabController` 로 `Scaffold` 를 감싼다.  

```dart
DefaultTabController(
  length: 3,
  child: Scaffold(...)
)
```
  
## 2. Create the tabs.   

지금까지 `AppBar`에는 단순한 `Text`객체만 `title` 속성만 초기화 하였는데  
탭바를 사용하려면 `TabBar` 객체를 `AppBar` 안에 생성해 주어야 한다.  

```dart
Widget _buildAppBar() {
  return AppBar(
    title: Text('Tabs Demo'),
    bottom: TabBar(
      tabs: [
        Tab(icon: Icon(Icons.directions_car)),
        Tab(icon: Icon(Icons.directions_transit)),
        Tab(icon: Icon(Icons.directions_bike)),
      ],
    ),
  );
}
```

`bottom` 속성에 `TabBar` 객체 생성  

`TabBar` 내부의 tabs 속성으로 탭에 사용될 아이콘들을 정의한다.  
> 생성자 매개변수로 `@required List<Widget> tabs` 를 요구하며 `controller` 속성를 통해 애니메이션 설정도 가능하다.  
> https://api.flutter.dev/flutter/material/TabBar-class.html

![flutter9](/assets/flutter/flutter9.png){: width="400" }  

`TabBar` 위젯 내부에 `onPressed()` 와 같은 별도의 이벤트 처리 속성, 메서드를 정의하지 않는다.

`TabController`가 우리가 클릭한 탭의 순번에 따라 알아서 순번에 맞는 탭뷰로 이동시켜주는 역할을 하기 때문  
(기본 애니메이션 처리는 덤 - 왼쪽 오른쪽 슬라이드 이동)  

## 3. Create content for each tab.  

`TabBar`를 생성했으니 `TabBar` 순번에 맞는 `TabBarView`를 생성한다.  

```dart
Widget _buildBody() {
  return TabBarView(
    children: [
      Icon(Icons.directions_car),
      Icon(Icons.directions_transit),
      Icon(Icons.directions_bike),
    ],
  );
}
```

![flutter10](/assets/flutter/flutter10.png){: width="400" }  


### TabBarView

> https://api.flutter.dev/flutter/material/TabBarView-class.html

탭에 해당하는 페이지 혹은 위젯을 `TabBarView`로 사용하면 된다.  
생성자에서 필수로 `@required List<Widget> children` 리스트 형태의 위젯을 요구한다.  

```dart
body: TabBarView(
  children:[
    SomePage1(),
    SomPage2(),
    Icon(Icons.directions_bike),
  ],
),
```