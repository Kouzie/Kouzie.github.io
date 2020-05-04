---
title:  "Flutter - Lists - Create a grid list!"

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

# Use lists

> https://flutter.dev/docs/cookbook/lists/basic-list

리스트 형식의 UI는 매우 자주 쓰이기에 Flutter에서 아주 간단하게 사용할 수 있도록 여러 위젯을 제공한다.  

## Create a ListView

> https://api.flutter.dev/flutter/widgets/ListView-class.html  

ListView 는 디자인을 위해 굉장히 많은 속성을 지원함으로 위의 url 참고바람

```dart
ListView(
  children: <Widget>[
    ListTile(...)
  ]
);
```

Flutter에서 List UI를 사용하려면 `ListView`, `ListTile` 위젯을 사용해야 한다.  

> Drawer 위젯에 자식으로 `ListView`가 들어가면서 잠깐 설명한 포스트가 있다.  
> https://kouzie.github.io/flutter/Flutter-Design-Add-a-Drawer-to-a-screen/#listview-listtile


## ListTile  

> https://api.flutter.dev/flutter/material/ListTile-class.html

예제에선 `leading`, `title` 속성만을 사용해 간단히 구현하였지만 디자인을 위해 많은 속성이 존재함으로 위의 url을 참고하기 바람.  

```dart
Scaffold(
  appBar: AppBar(title: Text(title)),
  body: ListView(
    children: <Widget>[
      ListTile(
        leading: Icon(Icons.map),
        title: Text('Map'),
      ),
      ListTile(
        leading: Icon(Icons.photo_album),
        title: Text('Album'),
      ),
      ListTile(
        leading: Icon(Icons.phone),
        title: Text('Phone'),
      )
    ]
  )
)
```

![flutter33](/assets/flutter/flutter33.png){: width="400" }  


# Create a horizontal list

> https://flutter.dev/docs/cookbook/lists/horizontal-list

`ListView`의 `scrollDirection` 속성과 `horizontal`을 지원하는 `Container`의 조합으로 가로형식으로 리스트를 출력 가능하다.  

```dart
ListView(
  // This next line does the trick.
  scrollDirection: Axis.horizontal,
  children: <Widget>[
    Container(...)
  ],
)
```

> 단순 가로형식으로 레이아웃을 잡고싶다면 `Row` 위젯을 사용하자.


# Work with long lists

> https://flutter.dev/docs/cookbook/lists/long-lists

`ListView` 를 정의할때 일반적으로 아래 형식으로 사용하였다.  

```dart
ListView(
    children: <Widget>[
      ListTile(...),
      ListTile(...),
  ],
)
```

만약 출력해야할 `ListTile`이 100개 이상 넘어가게 된다면 위와같이 배열안에 일일이 정의하기는 힘들것이다.  

`ListView.builder`를 사용하면 간단한 화살표 함수로 알아서 ListTile을 요소로 배열형태의 위젯리스트를 만들어준다.  

```dart 
class MyApp extends StatelessWidget {
  final List<String> items;
  MyApp({Key key, @required this.items}) : super(key: key);

  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      title: 'Long List',
      home: Scaffold(
        appBar: AppBar(title: Text(title)),
        body: ListView.builder(
          itemCount: items.length,
          itemBuilder: (context, index) {
            return ListTile(
              title: Text('${items[index]}'),
            );
          },
        ),
      ),
    );
  }
}
```

물론 `ListTile` 속성에 들어갈 각종 데이터(타이틀, 서브타이틀, 리딩 등)은 `List` 데이터로 미리 정의해두어야 하다.  

위에서도 `List<String> items` 필드를 통해 출력할 데이터는 미리 생성자에서 파라미터로 전달받는다.  


# Create a grid list

> https://flutter.dev/docs/cookbook/lists/grid-lists
> grid: 격자무늬   
달력처럼 격자무늬로 데이터를 출력하고 싶다면 `GridView`를 사용한다.  

이전에 휴대폰 방향 설정(`orientation`)에 따라 다른형식으로 GridView가 출력되도록 설정한적 있다. 간단한 디자인 속성이 있으니 참고  
> https://kouzie.github.io/flutter/Flutter-Design-Display-a-snackbar/#gridview-orientationbuilder


격자무늬 형식의 UI를 출력하기 위해 `GridView.count` 생성자로 `GridView` 위젯을 생성하며 자식으로 `List`, `<Widget>[]` 형식의 데이터가 필요하다. 

```dart
Scaffold(
  appBar: AppBar(title: Text(title)),
  body: GridView.count(
    crossAxisCount: 2,
    children: List.generate(100, (index) {
      ...
    }),
  ),
),
``` 

간단히 `List.generate` 메서드를 사용해 임의의 `List` 데이터를 생성하자.  

```dart
List.generate(100, (index) {
  return Text('Item $index');
}
```

![flutter18](/assets/flutter/flutter18.png){: width="400" }  

## Create lists with different types of items

> https://flutter.dev/docs/cookbook/lists/mixed-list

`GridView`, `ListView`의 `children` 으로 `<Widget>[]` 형식의 데이터도 들어갈 수 있기때문에 서로 다른 형식의 위젯들이 격자형식으로 출력될 수 있다.  

먼저 위젯으로 사용할 서로 다른 타입의 2개 클래스를 정의한다.  
 
# Place a floating app bar above a list

> https://flutter.dev/docs/cookbook/lists/floating-app-bar

List를 사용하면 스크롤이 생기고  

스크롤을 이용하게 된다면 floating app bar 라는 UI를 사용할 수 있다.  

아래 3가지 과정을 따라 floating app bar UI를 지원해보자.  

1. Create a CustomScrollView.  
2. Use SliverAppBar to add a floating app bar, Add a list of items using a SliverList
  

## 1. Create a CustomScrollView

기존의 `Scaffold`의 기본적인 UI로 `appbar` 속성과 `body` 속성을 사용해왔다.  

floating app bar를 위해 `Scaffold`의 `appbar`는 생략하고 `body` 속성에 `CustomScrollView`만을 사용한다.  

그리고 appBar로 사용할 위젯을 `CustomScrollView`에 정의할 것이다.  

> `appBar`로 사용될 위젯까지 스크롤 가능하다.  

```dart
Scaffold(
  body: CustomScrollView(
    slivers: <Widget>[
      SliverAppBar(...)
      SliverList(...)
    ]
  )
);
```

`SliverAppBar` 에 floating app bar 로 사용할 위젯 정의,
`SliverList` 에 출력될 내용물을 정의한다.  

> 자매품으로 `SliverGrid`기 있다.  

## 2. Use SliverAppBar to add a floating app bar, Add a list of items using a SliverList

> https://api.flutter.dev/flutter/material/SliverAppBar-class.html  
> https://api.flutter.dev/flutter/widgets/SliverList-class.html  
> UI에 대한 자세한 설명이 있음으로 위의 url을 참고하길 바람  

> Sliver: 조각 

대략적인 구조를 알았으니 `SliverAppBar`와 `SliverList` 내부의 디자인 속성과 데이터를 어떻게 집어넣는지 알아보자.  

```dart
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  MyApp({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final title = 'Floating App Bar';

    return MaterialApp(
      title: title,
      home: Scaffold(
        body: CustomScrollView(
          slivers: <Widget>[
            SliverAppBar(
              title: Text(title),
              floating: true, //https://api.flutter.dev/flutter/material/SliverAppBar/floating.html
              flexibleSpace: Placeholder(), // 개발시에 도움되는 클래스로 박스형태의 그림을 출력함
              expandedHeight: 200,
            ),
            SliverList(
              delegate: SliverChildBuilderDelegate(
                childCount: 1000,
                (context, index) ListTile(title: Text('Item #$index')),
              ),
            ),
          ],
        ),
      ),
    );
  }
} 
```

![flutter21](/assets/flutter/flutter21.png){: width="400" }  

`SliverList`의 `delegate`속성으로 리스트 아이템들을 설정할 수 있다.  

`SilverList` 경우 `SliverChildBuilderDelegate`안에 `ListTile`을  
`SliverGrid` 경우 `SliverChildBuilderDelegate`안에 `Container`를 만들면 된다.  