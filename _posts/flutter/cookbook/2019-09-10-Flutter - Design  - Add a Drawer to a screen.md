---
title:  "Flutter - Design - Add a Drawer to a screen!"

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

## Drawer

> https://flutter.dev/docs/cookbook/design/drawer

> A material design panel that slides in horizontally from the edge of a Scaffold to show navigation links in an application.

![flutter27](/assets/flutter/flutter27.png){: width="400" }  

서랍(`Drawer`) 모양의 icon 을 누르면 옆에서 슬라이드 형식으로 버튼이 출력된다.  
버튼을 눌러 페이지 변경도 가능하다.  

### 1. Create a Scaffold.   

`appBar`에 `Drawer`가 추가될것 같지만 별도의 속성으로 추가한다.  

```dart
Scaffold(
  drawer: // Add a Drawer here in the next step.
);
```  

### 2. Add a drawer.  


`Drawer({...})` 생성자로 Drawer 객체를 생성할 수 있다.  

```dart
Scaffold(
  drawer: Drawer(
    child: // Populate the Drawer in the next step.
  )
);
```

길어지기 때문에 별도의 메서드로 구분하자.  

```dart
Scaffold(
  drawer:_buiuldDrawer(context)
  ...
  ...
)
Widget _buildDrawer(context) {...}
```

### 3. Populate the drawer with items.  

이제 `Drawer` 객체를 생성, 반환하는 `_buildDrower` 메서드를 정의해보자.  

서랍목록처럼 표시하기 위해 `child` 로 `ListView`를 사용  

```dart
Widget _buildDrower(context) {
  return Drawer(
    child: ListView(
      padding: EdgeInsets.zero, //기본적으로 존재하는 상단매뉴 공백을 지워줌 
      children: <Widget>[ //List에 들어가게될 위젯들 
        DrawerHeader( 
          child: Text('Drawer Header'),
          decoration: BoxDecoration(color: Colors.blue)
        ),
        ListTile(
          title: Text('Item 1'),
          onTap: () {}
        ),
        ListTile(
          title: Text('Item 2'),
          onTap: () {}
        ),
      ],
    ),
  );
}
```


### 4. Close the drawer programmatically.  

버튼을 눌러 `Drawer` 를 닫고 싶다면 `Navigator.pop(context)` 을 호출한다.  

```dart
Widget _buildDrower(context) {
  return Drawer(
    child: ListView(
      padding: EdgeInsets.zero, //기본적으로 존재하는 상단매뉴 공백을 지워줌 
      children: <Widget>[ //List에 들어가게될 위젯들 
        ...
        ...
        ListTile(
          title: Text('Item 2'),
          onTap: () {
            Navigator.pop(context); //Drawer 를 닫음  
          },
        ),
      ],
    ),
  );
}
```

#### ListView, ListTile

위의 `Drawer` 예제에선 `ListView` 내부의 children 으로 `ListTile` 를 사용했다.  

> ListView: A scrollable list of widgets arranged linearly.   
> ListTile: A single fixed-height row that typically contains some text as well as a leading or trailing icon.  

`ListView`, `ListTile` 는 `Drawer` 외에도 리스트 형식의 디자인을 표시할때 많이 사용된다.  

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
      home: ListPage(),
    );
  }
}

class ListPage extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    int _act = 2;
    return Scaffold(
      body: ListView(
        children: <Widget>[
          Card(
            child: ListTile(
              title: Text('One-line with trailing widget'),
              trailing: Icon(Icons.more_vert),
            ),
          ),
          Card(
            child: ListTile(
              leading: FlutterLogo(),
              title: Text('One-line with both widgets'),
              trailing: Icon(Icons.more_vert),
            ),
          ),
          Card(
            child: ListTile(
              title: Text('One-line dense ListTile'),
              dense: true,
            ),
          ),
          Card(
            child: ListTile(
              leading: FlutterLogo(size: 56.0),
              title: Text('Two-line ListTile'),
              subtitle: Text('Here is a second line'),
              trailing: Icon(Icons.more_vert),
            ),
          ),
          Card(
            child: ListTile(
              leading: FlutterLogo(size: 56.0),
              title: Text('Two-line ListTile'),
              subtitle: Text('Here is a second line'),
              trailing: Icon(Icons.more_vert),
              isThreeLine: true,
            ),
          ),
          Card(
            child: ListTile(
                leading: Icon(Icons.flight_land),
                title: Text('Trix\'s airplane'),
                subtitle: _act != 2 ? Text('The airplane is only in Act II.') : null,
                enabled: _act == 2,
                onTap: () { /* react to the tile being tapped */ }
            ),
          ),
        ],
      )
    );
  }
}
```

![flutter6](/assets/flutter/flutter6.png){: width="400" }  
