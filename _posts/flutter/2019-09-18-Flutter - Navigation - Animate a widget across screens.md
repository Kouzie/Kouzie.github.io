---
title:  "Flutter - Navigation - Animate a widget across screens, Create a widget with arguments!"

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

## Navigation

> https://kouzie.github.io/flutter/Flutter-Animation-Animate-a-page-route-transition/#navigator

## Navigation 애니메이션 - Hero

`Hero` 위젯을 통해 사진이 줌인되는 효과를 가질 수 있다.  

먼저 2개의 `StatelessWidget`을 생성   

```js
class MainScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Main Screen'),
      ),
      body: GestureDetector(
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) {
            return DetailScreen();
          }));
        },
        child: Image.network(
          'https://picsum.photos/250?image=9',
        ),
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: GestureDetector(
        onTap: () {
          Navigator.pop(context);
        },
        child: Center(
          child: Image.network(
            'https://picsum.photos/250?image=9',
          ),
        ),
      ),
    );
  }
}
```

`GestureDetector`를 통해 tab하면 `Navigation`의 `push`, `pop`메서드로 이동하는 평범한 코드  

물 흐르듯 확대/축소되는 애니메이션을 사요하기 위해 Hero위젯을 사용하자.  

보통 이미지 크게보기 기능에서 사용된다.  

이미지는 Hero위젯의 생성자 매개변수로 전달  

```js
Hero(
  tag: 'imageHero',
  child: Image.network(
    'https://picsum.photos/250?image=9',
  ),
);
```
tag명이 같아야 매칭되어 애니메이션 처리가 된다.  

```js
import 'package:flutter/material.dart';

void main() => runApp(HeroApp());

class HeroApp extends StatelessWidget {
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
      appBar: AppBar(
        title: Text('Main Screen'),
      ),
      body: GestureDetector(
        child: Hero(
          tag: 'imageHero',
          child: Image.network(
            'https://picsum.photos/250?image=9',
          ),
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
            child: Image.network(
              'https://picsum.photos/250?image=9',
            ),
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


## Navigate with named routes

위에 Animation 배울때 링크를 클릭하면 `push()`메서드 2번째 매개변수로 `_createRoute()`가 생성한 `Route` 객체를 사용했다.  

`Navigator.push(context, _createRoute());`

```js
Route _createRoute() {
  return PageRouteBuilder(
    pageBuilder: (context, animation, secondaryAnimation) {
      return Page2();
    },
    transitionsBuilder: (context, animation, secondaryAnimation, child) {
      return child;
    },
  );
}
```

`pageBuilder`에 현재 상태, 에니메이션 등을 설정해 `StatelessWidget`을 생성하여 Route로 감싼후 반환한다.  

애니메이션 생략하고 그냥 밑에서 올라오는 페이지를 Navigator로 push하고 싶다면 `MaterialPageRoute` 사용 

```js
onPressed: () {
  Navigator.push(
    context,
    MaterialPageRoute(builder: (context) => SecondRoute()),
  );
}
```

현재 탭 삭제는 그냥 pop하면 된다.  
`Navigator.pop(context);`  

### Navigate with named routes

`MaterialPageRoute`의 람다식으로 쉽게 페이지를 올렸는데  
페이지를 정의하는 클래스에 이름을 지정하고 **이름을 통해 페이지를 생성할 수 있다**.(물론 Navigation의 메서드를 통해서)  

main함수쯤 되는 `MaterialApp`의 `initialRoute`, `routes` 속성을 설정하자.  

```js
MaterialApp(
  initialRoute: '/',
  routes: {
    '/': (context) => FirstScreen(),
    '/second': (context) => SecondScreen(),
  },
);
```

기존엔 아래와 같이 `home` 속성을 통해 처음 어플에 출력할 페이지를 지정하였는데 `initialRoute` 속성으로 지정할 수 있다.  

```js
return MaterialApp(
  title: 'Transition Demo',
  //home: MainScreen(),
  initialRoute: '/',
  routes: {
    '/': (context) => MainScreen(),
    ...
  },
);
```

`MaterialApp`의 `routes`속성에 모든 페이지들의 이름을 설정해놓자. (꼭 url처럼 생겼다)  

해당 페이지를 이름으로 push하고 싶다면 `pushNamed` 메서드를 호출하면 된다.  
`Navigator.pushNamed(context, '/second');`

```js
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
      appBar: AppBar(
        title: Text('First Screen'),
      ),
      body: Center(
        child: RaisedButton(
          child: Text('Launch screen'),
          onPressed: () {
            // Navigate to the second screen using a named route.
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
      appBar: AppBar(
        title: Text("Second Screen"),
      ),
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

> https://flutter.dev/docs/cookbook/navigation/named-routes

----

## Create a widget with arguments   

새로운 페이지를 열때 `Navigator.push()`, `Navigator.pushNamed()`으로 열었는데 단순 새로운 페이지를 열기만 하지 데이터를 넘기지는 않았다.  

전달하고 싶은 데이터 클래스를 정의하고 `Navigator` 매개변수로 같이 넘기도록 설정해보자.  

전달하고 싶은 `arguments`(데이터 클래스)정의, `title`은 제목, `message`는 내용으로 할용할 것이다.  

```js
class ScreenArguments {
  final String title;
  final String message;

  ScreenArguments(this.title, this.message);
}
```


### ModalRoute.of(context)  

출력할 페이지 정의 - `ExtractArgumentsScreen`,  
`arguments`를 넘겨받기 위해 `ModalRoute.of(context).` 메서드를 사용하는데 `route`가 설정한 `arguments`를 가져올 수 있다.  

```js
class ExtractArgumentsScreen extends StatelessWidget {
  static const routeName = '/extractArguments';

  @override
  Widget build(BuildContext context) {
    final ScreenArguments args = ModalRoute.of(context).settings.arguments;

    return Scaffold(
      appBar: AppBar(
        title: Text(args.title),
      ),
      body: Center(
        child: Text(args.message),
      ),
    );
  }
}
```

`Navigator.push()`메서드를 통해 해당 페이지를 오픈하고 `arguments`를 넘겨보자.  
홈으로 사용할 버튼이 있는 페이지 위젯 정의, 버튼을 누르면 위의 정의한 페이지 위젯을 연다.  

```js
class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Home Screen'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              child: Text("Navigate to screen that extracts arguments"),
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ExtractArgumentsScreen(),
                    settings: RouteSettings(
                      arguments: ScreenArguments(
                        'Extract Arguments Screen',
                        'This message is extracted in the build method.',
                      ),
                    ),
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
```

전번에는 `PageRouteBuilder`로 `PageRoute`객체를 만들었지만(애니메이션 파트 확인) 지금은 `MaterialPageRoute`로 바로 `PageRoute`객체를 생성한다.  

`MaterialPageRoute`는 기본적인 안드로이드의 아래에서 위로 올라오는 애니메이션의 `PageRoute`이다.  
`builder`속성에는 띄우고 싶은 페이지 위젯, `settings`속성에는 `RouteSettings`객체를 만드는데 안의 `arguments` 속성을 통해 전달 객체를 지정한다.  

`ModalRoute.of(context).settings.arguments;`으로 전달객체를 가져오는걸 다시 생각해보자.  


전체 코드

```js
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Navigation with Arguments',
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  ...위에 참고
  ...
}

class ExtractArgumentsScreen extends StatelessWidget {
  ...위에 참고
  ...
}

class ScreenArguments {
  ...위에 참고
}
```

![flutter23]({{ "/assets/flutter/flutter23.png" | absolute_url }}){: width="400" }  

### Navigator.pushNamed()과 argument  

당연히 `Navigator.pushNamed()`도 `argument`와 같이 사용할 수 있다.  

먼저 이름등록을 위해 `routes`속성에 페이지를 오픈하기 위해 사용할 이름 등록
```js
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Navigation with Arguments',
      home: HomeScreen(),
      routes: {
        ExtractArgumentsScreen.routeName: (context) => ExtractArgumentsScreen(),
      },
    );
  }
}
```

기존의 `Navigator.push()`를 `Navigator.pushNamed()`로 변환  

반면 `Navigator.pushNamed()`의 경우 이름만 지정하면 알아서 `PageRoute`객체가 만들기 때문에 바로 밑에 `arguments`속성을 지정해 데이터 전달이 가능하다.  

```js
class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      ...
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              child: Text("Navigate to screen that extracts arguments"),
              onPressed: () {
                // Navigator.push(
                //   context,
                //   MaterialPageRoute(
                //     builder: (context) => ExtractArgumentsScreen(),
                //     settings: RouteSettings(
                //       arguments: ScreenArguments(
                //         'Extract Arguments Screen',
                //         'This message is extracted in the build method.',
                //       ),
                //     ),
                //   ),
                // );
                Navigator.pushNamed(
                  context,
                  ExtractArgumentsScreen.routeName,
                  arguments: ScreenArguments(
                    'Extract Arguments Screen',
                    'This message is extracted in the build method2.',
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}
```

### onGenerateRoute  

위에서 `ModalRoute.of(context)`를 통해 `PageRoute`의 `setting`안의 `arguments`를 가져왔었는데 

`onGenerateRoute`를 통해 페이지를 생성하고 `arguments`를 넘겨보자.  

넘길 `arguments`클래스는 위에 `class ScreenArguments` 그대로 사용.  

페이지 클래스를 정의하자.  

```js
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
`ExtractArgumentsScreen`의 경우 `build`메서드 안에 아래처럼 `PageRoute`로부터 `ScreenArguments` 객체를 받는 코딩이 있었는데 `PassArgumentsScreen`는 없다.  
```js
class ExtractArgumentsScreen extends StatelessWidget {
  static const routeName = '/extractArguments';

  @override
  Widget build(BuildContext context) {
    final ScreenArguments args = ModalRoute.of(context).settings.arguments;
    ...
  }
}
```

하지만 `HomeScreen`에서는 똑같이 `Navigator.pushNamed()`매소드를 통해 `ScreenArguments`객체를 넘길것이다.  

```js
class HomeScreen extends StatelessWidget {
  ...
    ...
    RaisedButton(
      child: Text("Navigate to a named that accepts arguments"),
      onPressed: () {
        Navigator.pushNamed(
          context,
          PassArgumentsScreen.routeName,
          arguments: ScreenArguments(
            'Accept Arguments Screen',
            'This message is extracted in the onGenerateRoute function.',
          ),
        );
      },
    ),
    ...
  ...
}
```

물론 `routes`속성에서 `ModalRoute.of()`를 사용해 `PassArgumentsScreen`생성에 필요한 `ScreenArguments`객체를 받아 초기화 할 수 있다.  

```js
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      routes: {
        PassArgumentsScreen.routeName: (context) {
          ScreenArguments arguments = ModalRoute.of(context).settings.arguments;
          return new PassArgumentsScreen(
              title: arguments.title,
              message: arguments.message);
        }
      },
      title: 'Navigation with Arguments',
      home: HomeScreen(),
    );
  }
}
```

하지만 우리는 글 소제목 처럼 `routes`속성이 아닌 `onGenerateRoute`속성을 사용해 이름별로 처리를 해야한다.  

`onGenerateRoute`속은은 `RouteFactory` 클래스타입의 `MaterialApp`의 필드인데 `RouteSettings`를 필드로 가지고있다..  

```js
final RouteFactory onGenerateRoute
...
Route RouteFactory (
  RouteSettings settings
)
```
즉 `onGenerateRoute`속성을 통해 바로 `settings`을 가져올 수 있다는 뜻이다.  

```js
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
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
      },
      title: 'Navigation with Arguments',
      home: HomeScreen(),
    );
  }
}
```
그럼 `onGenerateRoute`는 `PageRoute`가 생성되는 `Navigator.pus..()`메서드가 호출되면 무조건 호출되는지 궁금해 하나는 `routes: {}`속성에 하나는 `onGenerateRoute`속성을 통해 페이지를 생성해 보았는데 이미 매핑된 pageRoute는 `onGenerateRoute`를 호출하지 않는다.  

```js
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
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
      },
      routes: {
        ExtractArgumentsScreen.routeName: (context) => ExtractArgumentsScreen()
      },
      title: 'Navigation with Arguments',
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Home Screen'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              child: Text("Navigate to screen that extracts arguments"),
              onPressed: () {
                Navigator.pushNamed(
                  context,
                  ExtractArgumentsScreen.routeName,
                  arguments: ScreenArguments(
                    'Extract Arguments Screen',
                    'This message is extracted in the build method.',
                  ),
                );
              },
            ),
            RaisedButton(
              child: Text("Navigate to a named that accepts arguments"),
              onPressed: () {
                Navigator.pushNamed(
                  context,
                  PassArgumentsScreen.routeName,
                  arguments: ScreenArguments(
                    'Accept Arguments Screen',
                    'This message is extracted in the onGenerateRoute function.',
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}

class ExtractArgumentsScreen extends StatelessWidget {
  static const routeName = '/extractArguments';

  @override
  Widget build(BuildContext context) {
    final ScreenArguments args = ModalRoute.of(context).settings.arguments;

    return Scaffold(
      appBar: AppBar(
        title: Text(args.title),
      ),
      body: Center(
        child: Text(args.message),
      ),
    );
  }
}

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

class ScreenArguments {
  final String title;
  final String message;

  ScreenArguments(this.title, this.message);
}
```

`ExtractArgumentsScreen`를 `Navigator.pushNamed()`로 생성할 때 `routes: {}`에 등록되어 있기 때문에 `onGenerateRoute`의 메서드가 호출되지 않는다.  
잘 이해가 안간다면 아래 페이지를 참조  
> https://medium.com/@larsenthomasj/from-flutter-to-flight-3-navigation-8d567d2cb011