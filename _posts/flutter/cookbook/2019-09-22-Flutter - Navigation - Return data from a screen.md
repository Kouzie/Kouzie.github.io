---
title:  "Flutter - Navigation - Return data from a screen!"

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

# Return data from a screen  

> https://flutter.dev/docs/cookbook/navigation/returning-data

`Navigator` 의 `push` 를 통해 데이터를 전달하는건 전장에서 알아보았다.  

이번엔 `pop` 을 통해 페이지가 반환되때 해당 페이지에서 생성된 데이터를 반환받을 수 있는 방법을 알아보자.  

2. Add a button that launches the selection screen  
3. Show the selection screen with two buttons  
4. When a button is tapped, close the selection screen  
5. Show a snackbar on the home screen with the selection  

## 1. Define the home screen  

간단한 버튼이 정의된 `Scaffold` 위젯을 `home`으로 설정  

```dart
class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Returning Data Demo')),
      body: Center(child: SelectionButton()),
    );
  }
}
```
![flutter25]({{ "/assets/flutter/flutter25.png" | absolute_url }}){: width="400" }  

## 2. Add a button that launches the selection screen

일반적인 `RaisedButton` 이 아닌 `SelectionButton` 클래스를 정의한 이유는  
해당 버튼으로 띄어진 페이지의 반환값을 받기 위헤 `await, async` 키워드를 사용하기 위해서  

```dart
class SelectionButton extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return RaisedButton(
      onPressed: () {
        _navigateAndDisplaySelection(context);
      },
      child: Text('Pick an option, any option!'),
    );
  }

  _navigateAndDisplaySelection(BuildContext context) async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => SelectionScreen()),

      // TODO use result...
    );
  }
}
```

버튼을 누르면 `SelectionScreen` 페이지로 이동되며 반환값을 기다린다.  

## 3. Show the selection screen with two buttons

![flutter24]({{ "/assets/flutter/flutter24.png" | absolute_url }}){: width="400" }  

`SelectionScreen`는 위처럼 버튼 2개를 가진 페이지.  

```dart
class SelectionScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Pick an option')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: RaisedButton(
                onPressed: () {},
                child: Text('Yep!'),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: RaisedButton(
                onPressed: () {},
                child: Text('Nope.'),
              ),
            )
          ],
        ),
      ),
    );
  }
}
```

## 4. When a button is tapped, close the selection screen

각 버튼을 누를때 특정 문자열을 반환시키도록 하자.  

```dart
RaisedButton(
  onPressed: () {
    Navigator.pop(context, 'Yep!');
  },
  child: Text('Yep!'),
);
```

`Navigator.pop` 의 두번재 매개변수로 `String`값 삽입  

## 5. Show a snackbar on the home screen with the selection

`Navigator.pop`으로부터 받을 문자값을 `snackbar`로 출력하자.  

```dart
_navigateAndDisplaySelection(BuildContext context) async {
  final result = await Navigator.push(
    context,
    MaterialPageRoute(builder: (context) => SelectionScreen()),
  );
  Scaffold.of(context)
    ..removeCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text("$result")));
}
```

`doubel dot`를 사용해 현재 `context`의 `SnackBar`를 지우고 새로운 `SnackBar`를 출력한다.  


### Cascade notation `..` - doubel dot

Dart 문법중 하나로 `chaining` 을 사용하려면 `dot` 2개를 써야한다.  

```dart
List list = [];
list.add(color1);
list.add(color2);
list.add(color3);
list.add(color4);

// with cascade

List list = [];
list
  ..add(color1)
  ..add(color2)
  ..add(color3)
  ..add(color4);
```

위의 코드도 아래처럼 변경 가능하다.  

```dart
// Scaffold.of(context)
//   ..removeCurrentSnackBar()
//   ..showSnackBar(SnackBar(content: Text("$result")));
ScaffoldState scaffoldState = Scaffold.of(context);
scaffoldState.removeCurrentSnackBar();
scaffoldState.showSnackBar(SnackBar(content: Text("$result")));
```