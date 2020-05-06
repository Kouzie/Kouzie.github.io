---
title:  "Flutter - Navigation - Animate a widget across screens, Create a widget with arguments!"

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

# Navigate to a new screen and back

Flutter 의 화면전환을 위한 `Navigator` 객체에 대해 알아보자.  

이전에 화면이동간 애니메이션 처리시에 잠깐 `Navigator`를 사용하였었다.  
> https://kouzie.github.io/flutter/Flutter-Animation-Animate-a-page-route-transition/#navigator

당시에는 `PageRouteBuilder` 객체를 통해 페이지 이동, 애니메이션 처리를 하였는데   
이번엔 조금 다른 방식으로 페이지동, 애니메이션 처리를 할 계획이다.  

## 1. Create two routes

먼저 페이지로 사용할 2개의 `StatelessWidget` 을 정의  

```dart 
class FirstRoute extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('First Route')),
      body: Center(
        child: RaisedButton(
          child: Text('Open route'),
          onPressed: () {Navigator.push(...);},
        ),
      ),
    );
  }
}

class SecondRoute extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Second Route")),
      body: Center(
        child: RaisedButton(
          onPressed: () {Navigator.pop(...);},
          child: Text('Go back!'),
        ),
      ),
    );
  }
}
```

`FirstRoute` 에는 `Navigator.push()`  
`SecondRoute` 에는 `Navigator.pop()` 를 사용해 페이지 이동을 구현해보자.  


## 2. Navigate to the second route using Navigator.push()

먼저 `FirstRoute` 에 `Navigator.push()` 를 정의한다.  

```dart
onPressed: () {
  Navigator.push(
    context,
    MaterialPageRoute(builder: (context) => SecondRoute()),
  );
}
```

`MaterialPageRoute`를 사용해 이동한다.  
애니메이션 처리 없이 이동만을 지원하기에 `PageRouteBuilder`보다 간단하다.  

### MaterialPageRoute

> https://api.flutter.dev/flutter/material/MaterialPageRoute-class.html

안드로이드는 페이지 전환이 push일 경우 위에서 아래로 슬라이드, pop일 경우 반대로 슬라이드된다.  

기본 안드로이드 형식의로 페이지 라우팅을 하고싶다면 `MaterialPageRoute`이 가장 간단한 방법이다.  


## 3. Return to the first route using Navigator.pop()

`SecondRoute` 에 `Navigator.pop()` 를 정의한다.  

```dart 
onPressed: () {
  Navigator.pop(context);
}
```

# Animate a widget across screens

> https://flutter.dev/docs/cookbook/navigation/hero-animations

`MaterialPageRoute`을 쓰면서 `Navigation`을 통해 간단한 애니메이션 효과와 함께 페이지 이동을 구현해보자.  

> 디테일한 애니메이션 효과를 구현하려면 `PageRouteBuilder`와 같은 별도의 route 객체를 써야겠지만 자주쓰이는 확대(Hero) 같은 애니메이션은 별도의 위젯으로 적당히 구현할 수 있다.  

## 1. Create two screens showing the same image

먼저 보여줄 2개의 페이지(`StatelessWidget`)부터 정의.  

```dart
import 'package:flutter/material.dart';
void main() => runApp(RootPage());

class RootPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Transition Demo',
      home: MainScreen(),
    );
  }
}

class MainScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Main Screen')),
      body: GestureDetector(
        child: Image.network('https://picsum.photos/250?image=9'),
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) {
            return DetailScreen();
          }));
        },
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GestureDetector(
        child: Center(
          child: Image.network('https://picsum.photos/250?image=9'),
        ),
        onTap: () {
          Navigator.pop(context);
        },
      ),
    );
  }
}
```

두 `StatelessWidget` 위젯 모두 `child` 로 `Image.network`를 가지며  
`DetailScreen`의 경우 중앙에 사진을 위치시킨다.  


> `GestureDetector`:  이전에 위젯 드래그 효과를 처리하기 위해 사용한 객체.  
https://kouzie.github.io/flutter/Flutter-Animation-Animate-a-widget-using-a-physics-simulation/#gesturedetector  
`Image` 에는 `onTap` 이벤트 처리기능이 없음으로 `GestureDetector`로 감싼다.  

## 2. Add a Hero widget to the first screen

물 흐르듯 확대/축소되는 애니메이션을 사요하기 위해 `Hero`위젯을 사용.  

보통 이미지 크게보기 기능에서 사용된다.  

이미지는 `Hero`위젯의 생성자 매개변수로 전달  

```dart
class MainScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      ...
      body: GestureDetector(
        child: Hero(
          tag: 'imageHero',
          child: Image.network('https://picsum.photos/250?image=9'),
        ),
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) {
            return DetailScreen();
          }));
        },
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GestureDetector(
        child: Center(
          child: Hero(
            tag: 'imageHero',
            child: Image.network('https://picsum.photos/250?image=9'),
          ),
        ),
        onTap: () {
          Navigator.pop(context);
        },
      ),
    );
  }
}
```
`MaterialPageRoute`를 사용함에도 `Hero` 위젯을 사용해 애니메이션 처리가 가능하다.  

> `Hero`객체의 `tag` 속성이 같아야 매칭되어 애니메이션 처리가 된다.  


# Navigate with named routes

> https://flutter.dev/docs/cookbook/navigation/named-routes

`MaterialPageRoute`을 통해 쉽게 `Navigate` 처리를 진행하였다.  

페이지가 많아지고 복잡해질수록 라우팅 관리가 필요할 수 있다.  

이를 위해 페이지(위젯)과 `path` 문자열을 매칭해 `Navigate` 처리를 진행가능하다.    

`MaterialApp`의 `initialRoute`, `routes` 속성을 설정하자.  

```dart
return MaterialApp(
  title: 'Transition Demo',
  //home: MainScreen(),
  initialRoute: '/',
  routes: {
    '/': (context) => FirstScreen(),
    '/second': (context) => SecondScreen(),
  },
);
```

기존엔 `home` 속성을 통해 처음 어플에 출력할 페이지를 지정하였는데 `initialRoute` 속성으로 지정할 수 있다.  

해당 페이지를 이름으로 `Navigator`에 `push`하고 싶다면 `pushNamed` 메서드를 호출하면 된다.  
`Navigator.pushNamed(context, '/second');`

```dart
import 'package:flutter/material.dart';

void main() {
  runApp(MaterialApp(
    title: 'Named Routes Demo',
    initialRoute: '/',
    routes: {
      '/': (context) => FirstScreen(),
      '/second': (context) => SecondScreen(),
    },
  ));
}

class FirstScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('First Screen')),
      body: Center(
        child: RaisedButton(
          child: Text('Launch screen'),
          onPressed: () {
            Navigator.pushNamed(context, '/second');
          },
        ),
      ),
    );
  }
}

class SecondScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Second Screen")),
      body: Center(
        child: RaisedButton(
          onPressed: () {
            Navigator.pop(context);
          },
          child: Text('Go back!'),
        ),
      ),
    );
  }
}
```

오타예방으로 아래와 같이 route name 으로 사용할 문자열을 static 으로 미리 지정해두고 사용한다.  
```dart
class SecondScreen extends StatelessWidget {
  static const routeName = '/second';
}
```

## Create a widget with arguments   

> https://flutter.dev/docs/cookbook/navigation/navigate-with-arguments

새로운 페이지를 열때 `Navigator.push()`의 경우 **생성자**를 통해 데이터를 매개변수로 전달할 수 있다.  

`Navigator.pushNamed()` 는 단순 문자열로만 페이지를 열기 때문에 `arguments`속성과 `ModalRoute` 객체로 데이터를 전달받을 수 있다.  

`arguments` 넘기기 위한 객체로 `ScreenArguments` 를 정의하자.  

```dart
class ScreenArguments {
  final String title;
  final String message;

  ScreenArguments(this.title, this.message);
}
```

`Navigator.pushNamed()` 를 사용해 페이지를 이동시킬때 `ScreenArguments`를 생성해 매개변수로 같이 넘길것이다.  

### ModalRoute.of(context)  

`arguments`를 넘겨받기 위해 `ModalRoute.of(context).settings` 객체를 사용해 `route`가 설정한 `arguments`를 가져올 수 있다.  

> `ModalRoute.of(context)` 메서드는 `PageRoute` 객체를 반환한다.  

```dart
class ExtractArgumentsScreen extends StatelessWidget {
  static const routeName = '/extractArguments';

  @override
  Widget build(BuildContext context) {
    final ScreenArguments args = ModalRoute.of(context).settings.arguments;

    return Scaffold(
      appBar: AppBar(title: Text(args.title)),
      body: Center(child: Text(args.message)),
    );
  }
}
```

보낼때는 아래와 같이 `arguments` 속성을 사용한다.  

```dart
Navigator.pushNamed(
  context,
  ExtractArgumentsScreen.routeName,
  arguments: ScreenArguments(
    'Accept Arguments Screen', //title
    'This message is extracted in the onGenerateRoute function.', //message
  ),
);
```

![flutter23](/assets/flutter/flutter23.png){: width="400" }  

### onGenerateRoute  

위에서 `ModalRoute`를 통해 전달한 `arguments` 를 가져왔었는데  

`onGenerateRoute`를 통해서도 데이터를 전달받을 수 있다.  

> 보내는 방법은 똑같다. `Navigator.pushNamed`의 `arguments` 속성을 통해 `ScreenArguments` 객체 전달  

```dart
class PassArgumentsScreen extends StatelessWidget {
  static const routeName = '/passArguments';

  final String title;
  final String message;

  const PassArgumentsScreen({
    Key key,
    @required this.title,
    @required this.message,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
      ),
      body: Center(
        child: Text(message),
      ),
    );
  }
}
```
`PassArgumentsScreen`의 경우 `build`메서드 안에 `ModalRoute` 관련 코드가 없다.  
대신 `@required` 어노테이션을 사용해 생성자에서 `arguments` 를 전달받는다.  

`Navigator.pushNamed`을 사용하기 위해 `MaterialApp`의 `routes` 속성에 라우트 객체를 정의해 주었듯이  
생성자를 통해 arguments를 받으려면 `MaterialApp`의 `onGenerateRoute` 속성에 아래와 같은 설정을 해주어야 한다.  

```dart
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Navigation with Arguments',
      home: HomeScreen(),
      onGenerateRoute: (settings) {
        if (settings.name == PassArgumentsScreen.routeName) {
          final ScreenArguments args = settings.arguments;
          return MaterialPageRoute(
            builder: (context) {
              return PassArgumentsScreen(
                title: args.title,
                message: args.message,
              );
            },
          );
        }
      }
    );
  }
}
```

코드상 `Navigator.pushNamed`를 통한 모든 `pageRoute` 호출이 `onGenerateRoute` 속성에 정의된 메서드를 거쳐가야 하는 느낌이다.  

다행이도 `routes` 속성에 이미 매핑된 `pageRoute`는 `onGenerateRoute`를 호출하지 않는다.  

> https://medium.com/@larsenthomasj/from-flutter-to-flight-3-navigation-8d567d2cb011



## RouteSettings

> https://flutter.dev/docs/cookbook/navigation/passing-data

`Navigator.pushNamed`만 `arguments` 속성을 통해 인자를 전달할수 있는건 아니다.  
`Navigator.push` 도 `RouteSettings` 객체를 사용하면 `arguments` 속성으로 인자를 전달 가능하다.  

```dart
onTap: () {
  Navigator.push(
    context,
    MaterialPageRoute(
      builder: (context) => DetailScreen(),
      settings: RouteSettings(
        arguments: todos[index],
      ),
    ),
  );
}
```

`MaterialPageRoute`의 `settings` 속성을 사용해 `RouteSettings`를 사용한다.  

똑같이 `ModalRoute.of(context)` 코드로 가져오거나 위의 `onGenerateRoute`를 사용해 생성자로 인자를 넘겨주면 된다.  
어떤 방식을 사용하던 개발자의 자유이다.  

생성자를 사용하던, `ModalRoute`를 사용하던, `onGenerateRoute`를 사용하던 상황에 맞게 사용하면 된다.  

