---
title:  "Flutter - Animation - Animate the properties of a container, Fade a widget in and out
!"

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

# Flutter cookbook - Animate the properties of a container

> https://flutter.dev/docs/cookbook/animation/animated-container

이 전 예제에선 `GestureDetector` 을 통해 이벤트 콜백 메서드를 정의하고 `StatefulWidget` 의 애니메이션 효과를 처리했다.  

이번엔 단순 `StatefulWidget`이 아닌 `AnimatedContainer` 위젯을 사용해 애니메이션을 사용하자.  

> `Container` 위젯의 상속구조는 아래와 같다.  
> `Object > Diagnosticable > DiagnosticableTree > Widget > StatelessWidget > Container`  

> 반면 `AnimatedContainer` 위젯의 상속구조는 아래와 같다.  
> `Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > ImplicitlyAnimatedWidget > AnimatedContainer`  

> 만약 `Container`와 같은 위젯을 사용해 애니메이션 구현을 하고 싶다면 `StatefulWidget`을 상속하는 `AnimatedContainer` 을  사용하도록 하자.  
> https://api.flutter.dev/flutter/widgets/AnimatedContainer-class.html


```dart
import 'dart:math';
import 'package:flutter/material.dart';

void main() => runApp(AnimatedContainerApp());

class AnimatedContainerApp extends StatefulWidget {
  @override
  _AnimatedContainerAppState createState() => _AnimatedContainerAppState();
}

class _AnimatedContainerAppState extends State<AnimatedContainerApp> {

  //초기 container 세팅값  
  double _width = 50;
  double _height = 50;
  Color _color = Colors.green;
  BorderRadiusGeometry _borderRadius = BorderRadius.circular(8);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('AnimatedContainer Demo')),
        body: _buildBody(),
        floatingActionButton: FloatingActionButton(
          child: Icon(Icons.play_arrow),
          onPressed: () {
            setState(() {
              // 버튼 클릭마다 설정값이 랜덤으로 변경  
              final random = Random();
              _width = random.nextInt(300).toDouble();
              _height = random.nextInt(300).toDouble();
              _color = Color.fromRGBO(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256),
                1,
              );
              _borderRadius = BorderRadius.circular(random.nextInt(100).toDouble());
            });
          },
        ),
      ),
    );
  }
}
```

`_buildBody()` 는 예상대로 `AnimatedContainer` 위젯을 반환하는 메서드이다.  

어쨋건 설정한 `_width`, `_height`, `_color`, `_borderRadius` 룰 설정한 `AnimatedContainer`이 반환되고 아래 그림처럼 출력된다.  

![flutter28]({{ "/assets/flutter/flutter28.png" | absolute_url }}){: width="400" }  

버튼클릭시 `Random` 객체로 위의 `AnimatedContainer` 세팅값이 랜덤으로 변경된다.  

이어서 `_buildBody()` 코드를 살펴보자.  

```dart
Widget _buildBody() {
  return Center(
    child: AnimatedContainer(
      width: _width,
      height: _height,
      decoration: BoxDecoration(
        color: _color,
        borderRadius: _borderRadius,
      ),
      duration: Duration(seconds: 1),
      curve: Curves.fastOutSlowIn,
    ),
  );
}
```

`Center` 위젯으로 감싸진 `AnimatedContainer` 가 반환된다.  
`BoxDecoration`객체를 통해 박스의 세부 디자인을 설정가능하다.  

`StatelessWidget` 를 상속하기에 동적변경이 물가능하다.  


# Flutter cookbook - Fade a widget in and out

모양변경이 아닌 동적으로 출력(`disable`, `enable`) 여부를 결정하고 싶을땐 `AnimatedOpacity` 객체를 사용하면 된다.  

```dart
AnimatedOpacity(
  opacity: _visible ? 1.0 : 0.0, // 투명도 선택  
  duration: Duration(milliseconds: 500),
  child: Container(
    width: 200.0,
    height: 200.0,
    color: Colors.green,
  ),
);
```

위의 `_buildBody` 메서드를 사용해 동적으로 모양이 변경하면서 투명도가 토글형식으로 변경되도록 해보자.  

```dart
import 'dart:math';
import 'package:flutter/material.dart';
...
...
class _AnimatedContainerAppState extends State<AnimatedContainerApp> {
  ...
  ...
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('AnimatedContainer Demo')),
        body: _buildBodyAnimatedOpacity(),
        floatingActionButton: FloatingActionButton(
          child: Icon(Icons.play_arrow),
          onPressed: () {
            setState(() {
              _visible = !_visible; //투명도 토글역할 변수  
              final random = Random();
              _width = random.nextInt(300).toDouble();
              _height = random.nextInt(300).toDouble();
              _color = Color.fromRGBO(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256),
                1,
              );
              _borderRadius = BorderRadius.circular(random.nextInt(100).toDouble());
            });
          },
        ),
      ),
    ); 
  }
  Widget _buildBodyAnimatedOpacity() {
    return AnimatedOpacity(
      opacity: _visible ? 1.0 : 0.0,
      duration: Duration(milliseconds: 500),
      child: _buildBody()
    );
  }
}
```

