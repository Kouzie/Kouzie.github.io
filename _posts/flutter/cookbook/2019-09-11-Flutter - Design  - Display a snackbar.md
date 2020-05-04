---
title:  "Flutter - Design  - Display a snackbar, Update the UI based on orientation!"

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

# Display a snackbar

> https://flutter.dev/docs/cookbook/design/snackbars

## SnackBar

![flutter7](/assets/flutter/flutter7.png){: width="400" }  

이렇게 생긴놈  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatelessWidget > SnackBar`

스낵바 생성 순서  

1. Create a Scaffold.  
2. Display a SnackBar.  
3. Provide an optional action.  


사실 1, 2단계, `Scaffold` 생성후 `SnackBar` 출력만 하면 만들어지는 매우 간단한 위젯  

### 1. Create a Scaffold.

```dart
void main() => runApp(SnackBarDemo());

class SnackBarDemo extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SnackBar Demo',
      home: Scaffold(
        appBar: AppBar(title: Text('SnackBar Demo')),
        body: SnackBarPage(),
      ),
    );
  }
}
```

가장 기본적인 `Scaffold` 객체 생성   

### 2. Display a SnackBar.

`Scaffold`의 `body`로는 `StatelessWidget` 를 사용한다.  

```dart
class SnackBarPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: RaisedButton(
        onPressed: () {
          final snackBar = SnackBar(
            content: Text('Yay! A SnackBar!'),
            action: SnackBarAction(
              label: 'Undo',
              onPressed: () {
                // Some code to undo the change.
              },
            ),
          );
          Scaffold.of(context).showSnackBar(snackBar);
        },
        child: Text('Show SnackBar'),
      ),
    );
  }
}
```

`SnackBar` 를 위한 별도의 속성은 없으며  

`RaisedButton`을 누르면 `SnackBar` 객체를 생성하고 `Scaffold.of(context).showSnackBar(snackBar)` 메서드를 호출해 `SnackBar`를 출력한다.  

### 3. Provide an optional action.  

```dart
final snackBar = SnackBar(
  content: Text('Yay! A SnackBar!'),
  action: SnackBarAction(
    label: 'Undo',
    onPressed: () {
      // Some code to undo the change.
    },
  ),
);
```

`SnackBar` 객체 생성부분만 때어서 보자.  
객체 내부에서도 `action` 속성과 `SnackBarAction` 객체를 통해 이벤트처리가 가능하다.  

# Update the UI based on orientation

> https://flutter.dev/docs/cookbook/design/orientation

방향에 따른 UI 변경을 알아보자.  
스크린 화면을 회전하여 UI 배치를 변경하고 싶을때(`portrait mode` 에서 `landscape mode`로 변경한다고 한다) UI 변경을 조작할 수 있다.  

## GridView, OrientationBuilder

`GridView`, `OrientationBuilder` 를 사용해 해결할 수 있다.  

1. Build a GridView with two columns.  
2. Use an OrientationBuilder to change the number of columns.  

세로줄을 2개로 `GridView`를 생성하고 `OrientationBuilder`를 통해 `landscape mode`에선 3줄로 변경해보자.  

### GridView 생성

`ListView`처럼 `GridView`역시 `children: <Widget>[]` 형식의 자식 객체들을 가지며 출력한다.  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatelessWidget > ScrollView > BoxScrollView > GridView`  

```dart
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final appTitle = 'Drawer Demo';
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: appTitle,
      home: GridPage(),
    );
  }
}

class GridPage extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GridView.count(
        padding: const EdgeInsets.all(20),
        crossAxisSpacing: 10, //좌우 간격
        mainAxisSpacing: 10, //상하 간격
        crossAxisCount: 2, //칼럼수
        children: <Widget>[
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('He\'d have you all unravel at the'),
            color: Colors.teal[100],
          ),
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('Heed not the rabble'),
            color: Colors.teal[200],
          ),
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('Sound of screams but the'),
            color: Colors.teal[300],
          ),
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('Who scream'),
            color: Colors.teal[400],
          ),
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('Revolution is coming...'),
            color: Colors.teal[500],
          ),
          Container(
            padding: const EdgeInsets.all(8),
            child: const Text('Revolution, they...'),
            color: Colors.teal[600],
          ),
        ],
      ),
    );
  }
}
```

![flutter8](/assets/flutter/flutter8.png){: width="400" }  

* `crossAxisSp acing`: **좌우** 사이 공백
* `mainAxisSpacing`: **상하** 사이 공백
* `crossAxisCount`: 하나의 리스트에 표시할 **블럭 개수**

![flutter29](/assets/flutter/flutter29.png){: width="400" }  
> https://developpaper.com/flutter-grid-layout-gridview/

## OrientationBuilder

스크린 출력 모드에 따라 양식을 UI를 변경하려면 `OrientationBuilder`로 `GridView`를 감싼다.  

`portrait mode` 에선 `crossAxisCount = 2`  
`landscape mode` 에선 `crossAxisCount = 3` 으로 설정한다.  

```dart
OrientationBuilder(
  builder: (context, orientation) {
    return GridView.count(
      // Create a grid with 2 columns in portrait mode,
      // or 3 columns in landscape mode.
      crossAxisCount: orientation == Orientation.portrait ? 2 : 3,
    );
  },
);
```

다른 속성또한 모드에 따라 변경하고 싶다면 `orientation` 속석을 사용해 변경이 가능하다.  

```dart
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final appTitle = 'Drawer Demo';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: appTitle,
      home: GridPage(
        title: appTitle
      ),
    );
  }
}

class GridPage extends StatelessWidget {

  final String title;
  GridPage({Key key, this.title}):super(key:key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: OrientationBuilder(
        builder: (context, orientation) {
          return GridView.count(
            primary: false,
            padding: const EdgeInsets.all(20),
            crossAxisSpacing: 10,
            mainAxisSpacing: 10,
            crossAxisCount: orientation == Orientation.portrait ? 2 : 3,
            children: <Widget>[
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('He\'d have you all unravel at the'),
                color: Colors.teal[100],
              ),
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('Heed not the rabble'),
                color: Colors.teal[200],
              ),
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('Sound of screams but the'),
                color: Colors.teal[300],
              ),
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('Who scream'),
                color: Colors.teal[400],
              ),
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('Revolution is coming...'),
                color: Colors.teal[500],
              ),
              Container(
                padding: const EdgeInsets.all(8),
                child: const Text('Revolution, they...'),
                color: Colors.teal[600],
              ),
            ],
          );
        },
      ),
    );
  }
}
```

![flutter30](/assets/flutter/flutter30.png){: width="400" }  
