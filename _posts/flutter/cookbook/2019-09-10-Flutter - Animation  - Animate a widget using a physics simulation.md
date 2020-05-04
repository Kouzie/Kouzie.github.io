---
title:  "Flutter - Animation - Animate a widget using a physics simulation!"

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

# Flutter cookbook - Animate a widget using a physics simulation

> https://flutter.dev/docs/cookbook/animation/physics-simulation

물리 시뮬레이션.... 아마 애니메이션 처리 cookbook중 가장 어려운 파트일것이다.  
복잡한 수학적 계산은 Flutter의 각종 객체가 대신해주나 구조가 복잡한 편이다.  

동적인 애니메이션 처리를 하기 위해 상태변화가 가능한 `StatefulWidget` 을 사용하며 상태처리객체와 함께 정의해야 한다  

```dart
import 'package:flutter/material.dart';

main() {
  runApp(MaterialApp(home: PhysicsCardDragDemo()));
}

class PhysicsCardDragDemo extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: DraggableCard(
        child: FlutterLogo(
          size: 128,
        ),
      ),
    );
  }
}

class DraggableCard extends StatefulWidget {
  final Widget child;
  DraggableCard({this.child});

  @override
  State createState() {
    return _DraggableCardState();
  }
}

class _DraggableCardState extends State<DraggableCard> {
  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Align(
      child: Card(
        child: widget.child,
      ),
    );
  }
}
```

`DraggableCard` 는 `StatefulWidget` 를 상속한다.  
그리고 `State<T>` 를 반환하는 `createState()`를 정의하는데 상태변화가 가능한 위젯 답게 변화된 상태를 관리하는 객체를 따로 생성하는 듯 하다.  

`PhysicsCardDragDemo`라는 `StatelessWidget`하위 클래스의 `body`속성으로 들어가고 생성자로 `FlutterLogo`를 넘긴다.


![flutter5](/assets/flutter/flutter5.png){: width="400" }  

아직은 어떠한 동작도 하지 않는다.  

> `initState`, `dispose`는 생성자, 소멸자로 생각하면 된다. 위젯이 트리에 추가/삭제 될 때 호출된다.  
`Align` 은 정렬을 위한 위젯, `alignment` 속성을 통해 부모위젯(컨테이너) 어느 위치에 지정할 수 있다. 기본값은 `Alignment.center`로 가운데(`Center` 위젯과 같은 역할)
https://www.youtube.com/watch?v=g2E7yl3MwMk  
`Alignment.center`는 내부코드에 `static const Alignment center = Alignment(0.0, 0.0)` 로 정의되어 있고 `(0.0, 0.0)`은 부모컨테이너의 정 중앙을 **백분율**로 표시한 것  
`Alignment(-1.0, -1.0)` represents the **top left** of the rectangle.  
`Alignment(1.0, 1.0)` represents the **bottom right** of the rectangle.  


## State<T extends StatefulWidget> class

상속관계  
`Object > Diagnosticable > State`  

> The logic and internal state for a StatefulWidget.

`StatefulWidget` 의 현 상태를 저장하기 위한 객체,    

`State` 객체는 `Flutter` 프레임 워크가 호출하는 `StatefulWidget`의 `createState`메서드로부터 만들어진다.  
`createState`메서드는 `StatefulWidget`객체가 렌더링 트리에 들어가는 순간 호출된다.  

```dart
class DraggableCard extends StatefulWidget {
  ...
  @override
  State createState() {
    return _DraggableCardState();
  }
}
```

맨 위의 코드에서 `State` 만 따로 분석하자.  

```dart
class _DraggableCardState extends State<DraggableCard> {
  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Align(
      child: Card(
        child: widget.child,
      ),
    );
  }
}
```

위의 객체에서 `setState`메서드를 호출해 통해 현상태에서 다른상태로 change 할 수 있다. 

`_DraggableCardState` 클래스에 각종 이벤트 콜백 함수를 정의하고 함수안에서 `setState` 를 통해 상태변경으로 애니메이션 처리를 구현해보자.  

이벤트 등록, 애니메이션 처리를 위해선 `SingleTickerProviderStateMixin`, `AnimationController` 객체들을 사용해야 한다.  

### SingleTickerProviderStateMixin

> https://api.flutter.dev/flutter/widgets/SingleTickerProviderStateMixin-mixin.html

애니메이션 처리설정을 하려면 `SingleTickerProviderStateMixin`를 추가해야 한다.  

```dart
mixin SingleTickerProviderStateMixin<T extends StatefulWidget> on State<T> implements TickerProvider {
  ...
  ...
}
```

`SingleTickerProviderStateMixin`는 Dart의 `mixin`이라는 데이터 타입을 갖고있는데 코드 재사용을 위해 `extends`한 메서드에 `mixin`에 정의된 기능을 추가(확장)한다 생각하면 된다. 

`SingleTickerProviderStateMixin` 내부에는 `Ticker` 객체를 생성하는 메서드가 정의되어 있으며  
`Ticker`는 매 `Frame`마다 콜백함수를 호출, 등록하고 스케줄링 하는 역할을 한다.  

밑의 `AnimationController` 를 사용해 애니메이션을 구현하려면 `Ticker` 들을 생성하는 `TickerProvider`가 필요하기에 `SingleTickerProviderStateMixin` 을 사용해 `_DraggableCardState` 객체를 확장해야 한다.  

### AnimationController

> A controller for an animation.

상속구조
`Object > Listenable > Animation<double> > AnimationController`  

`AnimationController`는 사용자가 정의한 애니메이션은 진행, 거꾸로 진행, 멈춤 기능을 가지고 있다.  

```dart
class _DraggableCardState extends State<DraggableCard> with SingleTickerProviderStateMixin {
  AnimationController _controller;

  @override
  void initState() {
    _controller = AnimationController(vsync: this, duration: Duration(seconds: 1));
    super.initState();
  }
 @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
  ...
}
```

> `SingleTickerProviderStateMixin`, `AnimationController` 위치 확인 

`AnimationController`는 생성자 파라미터로 `TickerProvider`를 필요로 한다.  

기본적인 애니메이션을 위한 구조는 정의했으니  
동작되는 애니메이션만 정의하면 된다.  


### GestureDetector  

제스처를 통해 위젯을 움직이려면 `GestureDetector`가 필요하다.  
빌드시에 `GestureDetector`를 생성 반환한다.  

```dart
class _DraggableCardState extends State<DraggableCard> with SingleTickerProviderStateMixin {
  AnimationController _controller;
  ...
  ....

  Alignment _dragAlignment = Alignment.center;
  Animation<Alignment> _animation;

  @override
  Widget build(BuildContext context) {
    var size = MediaQuery.of(context).size;
    return GestureDetector(
      onPanDown: (details) {},
      onPanUpdate: (details) {
        setState(() {
          _dragAlignment += Alignment(
            details.delta.dx / (size.width / 2),
            details.delta.dy / (size.height / 2),
          );
        });
      },
      onPanEnd: (details) {

      },
      child: Align(
        alignment: _dragAlignment,
        child: Card(
          child: widget.child,
        ),
      ),
    );
  }
}
```


`GestureDetector` 의 생성자 속성으로 사용된 속성들을 살펴보자.  

* `GestureDetector.onPanDown`   
*A pointer has contacted the screen with a primary button and might begin to move.*  
`GestureDragDownCallback` 필드를 초기화, 처음 위젯(패널)에서 위젯을 건들이면 호출

* `GestureDetector.onPanUpdate`  
*A pointer that is in contact with the screen with a primary button and moving has moved again.*  
위젯에서 누른채 움직이면 호출  

* `GestureDetector.onPanEnd`  
위젯에서 손을 때면 호출

위의 코드를 보면 `onPanUpdate`가 발생할때 마다 `setState()`를 호출해 `_dragAlignment`를 수정한다.  
밑의 `child`속성으로 있던 `Card` 객체가  `Align` 위젯에 의해 `_dragAlignment` 설정대로 정렬되어 있기 때문에  
프레임단위로 변화되는 `_dragAlignment` 값으로 인해 드래그효과가 발생한다.  

`GestureDetector`를 초기화 할떄 사용되는 생성자 매개변수는 아래와 같다.  

```dart
GestureDetector({Key key, Widget child, GestureTapDownCallback onTapDown, GestureTapUpCallback onTapUp, GestureTapCallback onTap, GestureTapCancelCallback onTapCancel, GestureTapDownCallback onSecondaryTapDown, GestureTapUpCallback onSecondaryTapUp, GestureTapCancelCallback onSecondaryTapCancel, GestureTapCallback onDoubleTap, GestureLongPressCallback onLongPress, GestureLongPressStartCallback onLongPressStart, GestureLongPressMoveUpdateCallback onLongPressMoveUpdate, GestureLongPressUpCallback onLongPressUp, GestureLongPressEndCallback onLongPressEnd, GestureDragDownCallback onVerticalDragDown, GestureDragStartCallback onVerticalDragStart, GestureDragUpdateCallback onVerticalDragUpdate, GestureDragEndCallback onVerticalDragEnd, GestureDragCancelCallback onVerticalDragCancel, GestureDragDownCallback onHorizontalDragDown, GestureDragStartCallback onHorizontalDragStart, GestureDragUpdateCallback onHorizontalDragUpdate, GestureDragEndCallback onHorizontalDragEnd, GestureDragCancelCallback onHorizontalDragCancel, GestureForcePressStartCallback onForcePressStart, GestureForcePressPeakCallback onForcePressPeak, GestureForcePressUpdateCallback onForcePressUpdate, GestureForcePressEndCallback onForcePressEnd, GestureDragDownCallback onPanDown, GestureDragStartCallback onPanStart, GestureDragUpdateCallback onPanUpdate, GestureDragEndCallback onPanEnd, GestureDragCancelCallback onPanCancel, GestureScaleStartCallback onScaleStart, GestureScaleUpdateCallback onScaleUpdate, GestureScaleEndCallback onScaleEnd, HitTestBehavior behavior, bool excludeFromSemantics: false, DragStartBehavior dragStartBehavior: DragStartBehavior.start })
```
`onPanDown`, `onPanUpdate`, `onPanEnd` 외에도 굉장히 많은 이벤트 처리 함수를 정의할 수 있으니 참고....

제스처 추가설명  
> https://flutter.dev/docs/cookbook/gestures  
> https://api.flutter.dev/flutter/widgets/GestureDetector-class.html  


## 스프링 처럼 원래대로 돌아가는 효과

다시 원래 위치 `Alignment.center` 로 스프링 효과처럼 돌아가기 위한 메서드 `_runAnimation`를 정의  

먼저 `onPanDown` 이벤트 발생시 `_controller.stop()` 호출  
위젯을 건드는 순간 기존 애니메이션은 종료시키며 또 다른 애니메이션을 진행시킨다.  

`_controller.stop()`를 호출하지 않으면 스프링 효과로 `Alignment.center` 다 돌아간 후 애니메이션이 종료될 때 까지는 꼼짝없이 기다려야 한다.  

```dart
@override
Widget build(BuildContext context) {
  final size = MediaQuery.of(context).size;
  return GestureDetector(
    onPanDown: (details) {
      _controller.stop();
    },
    onPanEnd: (details) {
      _runAnimation(details.velocity.pixelsPerSecond, size);
    },
    onPanUpdate: (details) {
      setState(() {
        _dragAlignment += Alignment(
          details.delta.dx / (size.width / 2),
          details.delta.dy / (size.height / 2),
        );
      });
    },
    child: Align(
      alignment: _dragAlignment,
      child: Card(
        child: widget.child,
      ),
    ),
  );
}
```

터치를 땐 순간(`onPanEnd`) `_runAnimation` 메서드가 호출되며 스프링 효과를 적용해 `Alignment.center` 위젯을 되돌린다.  

```dart
void _runAnimation(Offset pixelsPerSecond, Size size) {
  _animation = _controller.drive(
    AlignmentTween(
      begin: _dragAlignment,
      end: Alignment.center,
    ),
  );

  final unitsPerSecondX = pixelsPerSecond.dx / size.width;
  final unitsPerSecondY = pixelsPerSecond.dy / size.height;
  final unitsPerSecond = Offset(unitsPerSecondX, unitsPerSecondY);
  final unitVelocity = unitsPerSecond.distance;

  const spring = SpringDescription(
    mass: 30,
    stiffness: 1,
    damping: 1,
  );

  final simulation = SpringSimulation(spring, 0, 1, -unitVelocity);
  _controller.animateWith(simulation);
}
```

이 과정에서 `SpringDescription` 과 `SpringSimulation`을 사용한다.  

> https://flutter.dev/docs/cookbook/animation/physics-simulation