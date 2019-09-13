---
title:  "Flutter - Animation - Animate a page route transition!"

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



일단 제일 처음 제공되는 소스를 실행

```js
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
  _DraggableCardState createState() {
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
`StatefulWidget`이 처음 등장.  

`StatelessWidget`과 반대로 동적으로 변화가 발생하는 위젯,   

`StatelessWidget` 의 `body`속성으로 들어가고 생성자로 `FlutterLogo`를 넘긴다.

그리고 `State<DraggableCard>` 를 반환하는 `createState()`를 정의하는데 상태변화가 가능한 위젯 답게 변화된 상태를 관리하는 객체를 따로 생성하는 듯 하다.  

![flutter5]({{ "/assets/flutter/flutter5.png" | absolute_url }})  

아직은 어떠한 동작도 하지 않는다.  

> `initState`, `dispose`는 생성자, 소멸자로 생각하면 된다. 위젯이 트리에 추가/삭제 될 때 호출된다.  

> Align 은 정렬을 위한 위젯, alignment 속성을 통해 부모위젯(컨테이너) 어느 위치에 지정할 수 있다. 기본값은 Alignment.center로 가운데(Center 위젯과 같은 역할)
> https://www.youtube.com/watch?v=g2E7yl3MwMk  



### State<T extends StatefulWidget> class

상속관계  
`Object > Diagnosticable > State`  

> The logic and internal state for a StatefulWidget.

상태 변경을 위한 처리를 위해 정의되는 클래스  

`setState`메서드를 통해 1상태에서 2상태로 change한다고 함.  

`State` 객체는 `Flutter` 프레임 워크가 호출하는 `StatefulWidget`의 `createState`메서드로부터 만들어진다.  

`createState`메서드는 `StatefulWidget`객체가 렌더링 트리에 들어가는 순간 콜백된다.  

## SingleTickerProviderStateMixin, AnimationController 

클래스 명에서 알려주듯이 우리는 사진파일을 드래그할 수 있도록 설정해야 한다.  

이를 위해 `State` 클래스에 `SingleTickerProviderStateMixin`를 추가하라고 한다.  
`SingleTickerProviderStateMixin` 클래스는 애니메이션을 처리하기 위한 헬퍼 클래스이다.  

```js
mixin SingleTickerProviderStateMixin<T extends StatefulWidget> on State<T> implements TickerProvider {
  ...
  ...
}
```
`SingleTickerProviderStateMixin`는 Dart의 `mixin`이라는 데이터 타입을 갖고있는데 코드 재사용을 위해 `extends`한 메서드에 `mixin`에 정의된 기능을 추가한다 생각하면 된다.  

아래 코드를 보면 `State<DraggableCard>`에 mixin 객체가 추가된것을 볼 수 있다.  
```js
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

`SingleTickerProviderStateMixin`를 상속 받아서 `vsync`에 `this` 형태로 전달해야 애니메이션이 정상 처리된다.  


### AnimationController

> A controller for an animation.

상속구조
`Object > Listenable > Animation<double> > AnimationController`  

사용자가 정의한 애니메이션은 진행, 거꾸로 진행, 멈추는 기능을 가지고 있다.  
정의한 애니메이션은 value속성을 사용에 지정한다.  

위에서 보았듯이 `AnimationController`는 `TickerProvider`를 필요로 한다.(생성시 vsync 초기화를 위해)  
Ticker는 매 Frame마다 콜백함수를 호출하고 그럼 함수를 등록하고 스케줄링 하는 역할은 한다.  

`AnimationController`는 그런 `Ticker`를 사용해 애니메이션 처리를 하기에 `Ticker`를 만들수 있는 `TickerProvider`가 필요하다.  

어쨌든 위에서 애니메이션을 등록하고 실행/정지 시킬수 있는 컨트롤러를 정의했으니 동작되는 애니메이션만 정의하면 된다.  

## GestureDetector  

제스처를 통해 위젯을 움직이려면 `GestureDetector`가 필요하다.  


```js

class _DraggableCardState extends State<DraggableCard> with SingleTickerProviderStateMixin {
  AnimationController _controller;
  Alignment _dragAlignment = Alignment.center;
  Animation<Alignment> _animation;

  //initState, dispose 생략
  ...
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

>   `Alignment _dragAlignment = Alignment.center;` 위에서 봤던 Align 클래스의 alignment속성으로 들어간 값이다.  
>   내부적으로 `static const Alignment center = Alignment(0.0, 0.0);` 정의되어 있고 0. 0은 사각형의 정 중앙, 좌/하 는 백분율로 -1, 우/상은 +1의 값을 지정하면 된다.  

GestureDetector를 초기화 할떄 사용되는 생성자 매개변수는 아래와 같다.  
```js
GestureDetector({Key key, Widget child, GestureTapDownCallback onTapDown, GestureTapUpCallback onTapUp, GestureTapCallback onTap, GestureTapCancelCallback onTapCancel, GestureTapDownCallback onSecondaryTapDown, GestureTapUpCallback onSecondaryTapUp, GestureTapCancelCallback onSecondaryTapCancel, GestureTapCallback onDoubleTap, GestureLongPressCallback onLongPress, GestureLongPressStartCallback onLongPressStart, GestureLongPressMoveUpdateCallback onLongPressMoveUpdate, GestureLongPressUpCallback onLongPressUp, GestureLongPressEndCallback onLongPressEnd, GestureDragDownCallback onVerticalDragDown, GestureDragStartCallback onVerticalDragStart, GestureDragUpdateCallback onVerticalDragUpdate, GestureDragEndCallback onVerticalDragEnd, GestureDragCancelCallback onVerticalDragCancel, GestureDragDownCallback onHorizontalDragDown, GestureDragStartCallback onHorizontalDragStart, GestureDragUpdateCallback onHorizontalDragUpdate, GestureDragEndCallback onHorizontalDragEnd, GestureDragCancelCallback onHorizontalDragCancel, GestureForcePressStartCallback onForcePressStart, GestureForcePressPeakCallback onForcePressPeak, GestureForcePressUpdateCallback onForcePressUpdate, GestureForcePressEndCallback onForcePressEnd, GestureDragDownCallback onPanDown, GestureDragStartCallback onPanStart, GestureDragUpdateCallback onPanUpdate, GestureDragEndCallback onPanEnd, GestureDragCancelCallback onPanCancel, GestureScaleStartCallback onScaleStart, GestureScaleUpdateCallback onScaleUpdate, GestureScaleEndCallback onScaleEnd, HitTestBehavior behavior, bool excludeFromSemantics: false, DragStartBehavior dragStartBehavior: DragStartBehavior.start })
```

당연히 모두 볼수 없으니 위에서 사용했던 생성자 변수 몇개를 살펴보자.  

### `GestureDetector.onPanDown`  

> A pointer has contacted the screen with a primary button and might begin to move.
`GestureDragDownCallback` 필드를 초기화, 처음 위젯(패널)에서 위젯을 건들이면 호출

### `GestureDetector.onPanUpdate`  

> A pointer that is in contact with the screen with a primary button and moving has moved again.
위젯에서 누른채 움직이면 호출

### `GestureDetector.onPanEnd`  

위젯에서 손을 때면 호출

위의 코드를 보면 `onPanUpdate`가 발생할때 마다 `setState()`를 호출해 `_dragAlignment`를 수정한다. 밑의 child속성으로 있던 Card 객체가  Align 위젯에 의해 `_dragAlignment` 로 정렬되어 있기 때문에 드래그 효과가 발생한다.  

### 스프링 처럼 원래대로 돌아가는 효과

생략 

> https://flutter.dev/docs/cookbook/animation/physics-simulation