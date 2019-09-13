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

## snackbar

이렇게 생긴놈  

![flutter7]({{ "/assets/flutter/flutter7.png" | absolute_url }})  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatelessWidget > SnackBar`

스낵바 생성 순서  

1. Create a Scaffold.  
2. Display a SnackBar.  
3. Provide an optional action.  

사실 1, 2단계만 거쳐도 만들어지는 매우 간단한 위젯  

```js
void main() => runApp(SnackBarDemo());

class SnackBarDemo extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SnackBar Demo',
      home: Scaffold(
        appBar: AppBar(
          title: Text('SnackBar Demo'),
        ),
        body: SnackBarPage(),
      ),
    );
  }
}
```

별도로 MyApp같은걸 만들지 말고 바로 Scaffold생성자로 home에 생성한다.  

```js
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

Scaffold 페이지 body부분은 위와같다.  
버튼을 클릭하면 `SnackBar`가 생성되고  

현제 Scaffold 페이지에서 SnackBar를 출력하는 `Scaffold.of(context).showSnackBar(...)` 를 호출한다.  


## GridView, OrientationBuilder

스크린 화면을 회전하여 UI 배치를 변경하고 싶을때 `portrait mode` 에서 `landscape mode`로 변경할 때 `GridView`, `OrientationBuilder` 를 사용해 해결할 수 있다.  

1. Build a GridView with two columns.  
2. Use an OrientationBuilder to change the number of columns.  

### GridView

`ListView`처럼 `GridView`역시 `children: <Widget>[]` 형식의 자식 객체들을 가지며 출력한다.  

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatelessWidget > ScrollView > BoxScrollView > GridView`  


```js
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
        primary: false,
        padding: const EdgeInsets.all(20),
        crossAxisSpacing: 10,
        mainAxisSpacing: 10,
        crossAxisCount: 3,
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

![flutter8]({{ "/assets/flutter/flutter8.png" | absolute_url }})  

* primary: false, - 스크롤 관련 속성, false의 경우 충분한 content가 있어야 스크롤 가능, true의 경우 스크롤 가능
* crossAxisSpacing: 10, - 좌우 사이 공백
* mainAxisSpacing: 10, - 상하 사이 공백
* crossAxisCount: 2, - 하나의 리스트에 표시할 블럭 개수

## OrientationBuilder

스크린 출력 모드에 따라 양식을 UI를 변경하려면 `OrientationBuilder`로 `GridView`를 만들어야 한다.
```js
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

딱보면 어떤식으로 운영되는지 알 수 있다.  
세로모드(portrait)일 경우 2개씩 출력, 아닐경우 3개 출력

다른 속성또한 이런식으로 변경이 가능.  

```js
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

> 주의: 가상 휴대폰 회전 설정을 허용하고 테스트 진행....