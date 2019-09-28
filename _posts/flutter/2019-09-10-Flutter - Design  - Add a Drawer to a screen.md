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

> A material design panel that slides in horizontally from the edge of a Scaffold to show navigation links in an application.

scaffold 의 edge에서 슬라이드 형식으로 위젯, 

참고로 Drawer는 서랍이란 뜻  

다음 절차대로 Drawer를 만들수 있다함  

## 1. Create a Scaffold.     
```dart
Scaffold(
  drawer: // Add a Drawer here in the next step.
);
```
## 2. Add a drawer.  
```dart
Scaffold(
  drawer: Drawer(
    child: // Populate the Drawer in the next step.
  )
);
```
## 3. Populate the drawer with items.  4. Close the drawer programmatically.  

```dart
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  final appTitle = 'Drawer Demo';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: appTitle,
      home: MyHomePage(title: appTitle),
    );
  }
}

class MyHomePage extends StatelessWidget {
  final String title;

  MyHomePage({Key key, this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(child: Text('My Page!')),
      drawer: Drawer(
        // Add a ListView to the drawer. This ensures the user can scroll
        // through the options in the drawer if there isn't enough vertical
        // space to fit everything.
        child: ListView(
          // Important: Remove any padding from the ListView.
          padding: EdgeInsets.zero,
          children: <Widget>[
            DrawerHeader(
              child: Text('Drawer Header'),
              decoration: BoxDecoration(
                color: Colors.blue,
              ),
            ),
            ListTile(
              title: Text('Item 1'),
              onTap: () {
                // Update the state of the app
                // ...
                // Then close the drawer
                Navigator.pop(context);
              },
            ),
            ListTile(
              title: Text('Item 2'),
              onTap: () {
                // Update the state of the app
                // ...
                // Then close the drawer
                Navigator.pop(context);
              },
            ),
          ],
        ),
      ),
    );
  }
}
```

### ListView, ListTile

> ListView: A scrollable list of widgets arranged linearly.

> ListTile: A single fixed-height row that typically contains some text as well as a leading or trailing icon.

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

![flutter6]({{ "/assets/flutter/flutter6.png" | absolute_url }}){: width="400" }  


다시 `Drawer`로 돌아와서 `Scaffold`의 `drawer`속성 정의를 보자.  

단순히 `Drawer` 위젯에 child로 `ListView`가 들어갈 뿐이다.    
처음보는 것은 패딩설정 `padding: EdgeInsets.zero,`  
`children`속성을 보면 `List<Widget>`의 `DrawerHeader` 위젯  
