---
title:  "Flutter - 상태관리, BloC, Provider!"
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

# BLoC 패턴 

> BLoC: Bussiness Logic Component

`BLoC`을 쓰면 비지니스 로직과 UI를 위한 코드 분리가 쉬워진다.  
또한 이런 비지니스 로직을 다른 위젯에서도 재사용 가능하다.  

`BLoC` 패턴은 `Stream`을 통한 콜백구조로 이루어져 있기 때문에  
`Stream`을 쉽게 사용하기 위한 `RxDart` 패키지를 사용해보자

flutter 기본 어플리케이션인 `counter` 예제를 통해 `RxDart`와 `Stream`을 알아보자.  

## BehaviorSubject

> https://pub.dev/packages/rxdart

기존의 예제는 버튼을 눌러 `counter` 변수값을 늘리는 로직을 `setState()` 메서드를 사용해 진행해 왔다.  

```dart
class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('You have pushed the button this many times:'),
            Text('$_counter', style: Theme.of(context).textTheme.display1),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          setState(() {
            _counter++;
          });
        },
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}
```

`setState` 말고 `StreamBuilder`와 `RxDart`의 `BehaviorSubject`를 사용해 구현하자.  

`BehaviorSubject` 객체는 **새로 삽입된 데이터**, 즉 가장 최신상태의 `state`(데이터)를 `stream` 형식으로 반환하게 해주는 객체이다.  

아래 예제를 통해 알아보자.  

```dart
import 'package:rxdart/rxdart.dart';
...
...
class MyHomePage extends StatefulWidget {
  final String title;
  MyHomePage({Key key, this.title}) : super(key: key);

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final countSubjectg = BehaviorSubject<int>();
  int conut = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              child: Text("Add"),
              onPressed: () {
                countSubjectg.add(++conut);
              },
            ),
            StreamBuilder<int>(
                stream: countSubjectg.stream,
                initialData: 0,
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return Text("${snapshot.data}",
                        style: TextStyle(fontSize: 30));
                  }
                })
          ],
        ),
      ),
    );
  }
}
```


1. `final countSubjectg = BehaviorSubject<int>();`  
2. `countSubjectg.add(++conut);`  
3. `countSubjectg.stream`  

핵심 키워드는 위의 3가지 코드이다.  

1. `int` 데이터를 `state`로 가지는 `BehaviorSubject` 객체 정의  

2. `BehaviorSubject`의 `add` 메서드를 통해 지속적으로 `state`를 업데이트한다.(기존의 count는 지워지고 +1된 count가 새로운 `state`로 업데이트된다)  

3. `countSubjectg.stream`을 통해 `state`를 실시간으로 반환하는 스트림 객체를 반환한다.  


지금까지 `setState` 로 `state`를 변화시켜 다시 랜더링 트리에 삽입시켜 왔는데  

`BehaviorSubject` 가 반환하는 스트림 객체와 `StreamBuilder`를 통해 실시간으로 데이터 변화, 렌더링을 처리한다.   

`countSubjectg.add()` 메서드를 통해 새로운 데이터 `count`가 삽입되면  
이를 감시하고 있던 `StreamBuilder`의 `builder` 메서드가 콜백되고 `snapshot` 매개변수를 통해 해당 데이터를 가져올 수 있다.  

`initialData` 속성으로 `snapshot`의 초기 데이터 설정또한 가능하다.  

> 지금까지 예제에선 `state` 타입으로 단순 `int`값 하나만 사용하지만 상황에 맞춰 각종 콜렉션 객체(`List`, `Map` 등)들을 `state`의 타입으로 사용하면 된다.  


## BehaviorSubject 로 BLoC 패턴 사용하기  

위의 `BehaviorSubject` 를 사용해 `BloC` 패턴을 구현하자.   

우선 위에서 지정했던 `conut` 관련된 모든 로직을 새로만든 `CounterBloc` 로 이동시킨다.  

```dart
import 'package:rxdart/rxdart.dart';

class CounterBloc {
  int _count = 0;
  final _countSubject = BehaviorSubject.seeded(0); //초기값: 0

  void addCount() {
    _count ++;
    _countSubject.add(_count);
  }
  
  Stream<int> get count => _countSubject.stream;
}
```

`getter` 를 통해 `_countSubject`의 `stream`을 반환하여 외부에서도 접근 가능하도록 설정한다.  

다른 위젯들에서도 접근할 수 있도록 `main.dart` 최상위에 `CounterBloc` 정의한다.  

```dart 
//main.dart
import 'package:flutter/material.dart';
import 'package:flutter_basic/bloc/counter_bloc.dart';

void main() => runApp(MyApp());

final counterBloc = CounterBloc(); //최상위 정의 

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Retrieve Text Input',
      home: Scaffold(appBar: AppBar(title: Text("카운터")), body: Counter()), 
      //가독성을 위해 Scaffold 생성을 외부로 뺌
    );
  }
}

class Counter extends StatefulWidget {
  @override
  _CounterState createState() => _CounterState();
}


class _CounterState extends State<Counter> {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RaisedButton(
            child: Text("Add"),
            onPressed: counterBloc.addCount
          ),
          StreamBuilder<int>(
              stream: counterBloc.count,
              initialData: 0,
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  return Text("${snapshot.data}",
                      style: TextStyle(fontSize: 30));
                }
              })
        ],
      ),
    );
  }
}
```

`main.dart` 내부의 `count` 관련 로직은 모두 `countbloc` 객체를 통해 호출된다.  

또한 어디서든 `countBloc` 를 `import` 해 비지니스 로직을 재사용하고 `count` 데이터(`state`)를 관리할 수 있다.  

## flutter_bloc

> https://pub.dev/packages/flutter_bloc
> 현 ^3.2.0 ver 

위의 `BLoC` 패턴을 사용하기 위해 항상 정형화된 구조를 사용한다.  

- `BehaviorSubject` 객체 정의 
- 최상위에 `BloC` 객체 생성
- `Stream`반환 객체 정의  
- `add`, `delete`, `update` 로직  

이러한 반복구조에서 실수가 발생하지 않도록 `flutter_bloc` 이라는 패키지를 사용한다.  

먼저 `flutter_bloc` 의 기본 구조를 알아보자.  

각종 이벤트들을 처리할 이벤트 `enum` 객체,  
`Bloc`에 사용될 이벤트 객체와 비지니스 로직에 필요한 데이터 요소를 `generic` 으로 설정한다.  

```dart 
import 'package:rxdart/rxdart.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

enum CounterEvent { increment, decrement }

class CounterBloc extends Bloc<CounterEvent, int> {
  @override
  int get initialState => 0;

  @override
  Stream<int> mapEventToState(CounterEvent event) async* {
    switch (event) {
      case CounterEvent.decrement:
        yield state - 1;
        break;
      case CounterEvent.increment:
        yield state + 1;
        break;
    }
  }
}
```

`Bloc` 객체를 `extends` 하면 `initialState`, `mapEventToState` 두개의 메서드를 구현해야 한다.  

`initialState` `state`의 초기값이고 `mapEventToState`의 경우 새로 발생한 이벤트에 따라 `state`를 업데이트하고 반환하는 코드이다.  

`state`는 이미 `Bloc`을 상속하면서 필드로 가지고있다.  

> `async*`, `yield` : `Stream` 형태로 지속적으로 방출하기 위한 키워드 

### BlocProvider

> Provider: 제공자

`BlocProvider` 는 `BloC` 객체를 제공해주기 위해 사용하는 클래스이다.  

위의 정의된 `flutter_bloc`객체를 사용하기 위해서 `BlocProvider`, `BlocBuilder`를 사용해야 한다.  

먼저 `BlocProvider` 로 `MaterialApp` 을 감싸 `_counterBloc` 을 모든 앱 내부에서 사용할 수 있도록 한다.  

```dart
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final _counterBloc = new CounterBloc();
    return BlocProvider(
        create: (BuildContext context) {
          return _counterBloc;
        },
        child: MaterialApp(
          title: 'Retrieve Text Input',
          home: Scaffold(appBar: AppBar(title: Text("카운터")), body: Counter()),
        ));
  }
}
```

`create` 속성을 사용해 `_counterBloc` 을 `MaterialApp` 에 주입, 모든 하위 위젯들이 사용가능하다.  

이제 다른 위젯들이 어떻게 `CounterBloc` 에 접근해 비지니스 로직을 재사용하는이 알아보자.  

### BlocBuilder

`BlocProvider` 를 통해 `BloC`객체를 주입받았다면 `BlocProvider.of` 메서드를 사용해 가져올 수 있다.  

```dart
class Counter extends StatefulWidget {
  @override
  _CounterState createState() => _MyHomePageState();
}

class _CounterState extends State<Counter> {
  @override
  Widget build(BuildContext context) {
    final _counterBloc = BlocProvider.of<CounterBloc>(context);

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RaisedButton(
              child: Text("Add"),
              onPressed: () => _counterBloc.add(CounterEvent.increment)),
          BlocBuilder(
              bloc: _counterBloc,
              builder: (BuildContext context, int state) {
                return Text("${state}", style: TextStyle(fontSize: 30));
              })
        ],
      ),
    );
  }
}
```
`BlocBuilder` 를 통해 `BloC` 객체 `state`가 업데이트 될때마다 다시 랜더링을 할 수 있다.  

지켜볼 `BloC` 객체를 `bloc` 속성으로 설정하고 `builder` 를 사용해 출력할 위젯을 정의한다.  

만약 새로운 `BloC` 객체를 하위 위젯에 공유해야 한다면 다시 `BlocProvider`로 감싸면 된다.

```dart
return BlocProvider(
  create: (BuildContext context) {
    return _someBloc;
  },
  child: Center(
    child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RaisedButton(...),
          BlocBuilder(...)
        ],
      ),
    );
  )
);
```

> 추가자료: https://bloclibrary.dev/#/flutterbloccoreconcepts


## RxDart with Provider - InheritedWidget 

> https://api.flutter.dev/flutter/widgets/InheritedWidget-class.html

`flutter_bloc` 패키지를 사용한 `BloC` 패턴이 너무 복잡하게 느껴진다면  
`InheritedWidget`를 사용해 `BlocProvider` 처럼 사용 가능하다.  

```dart
import 'package:count/bloc/counter_bloc.dart';
import 'package:flutter/material.dart';

class CounterProvider extends InheritedWidget {
  final CounterBloc counterBloc;

  CounterProvider({Key key, CounterBloc counterBloc, Widget child})
      : counterBloc = counterBloc ?? new CounterBloc(),
        super(key: key, child: child);

  @override
  bool updateShouldNotify(InheritedWidget oldWidget) {
    // TODO: implement updateShouldNotify
    return true;
  }

  static CounterBloc of(BuildContext context) =>
      context.dependOnInheritedWidgetOfExactType<CounterProvider>().counterBloc;
}
```

내부의 `CounterBloc` 객체가 있고 생성자에서 받거나 받지 않을시에는 자동으로 생성한다.  
생성된 `CounterBloc` 인스턴스는 `of()` 메서드를 통해 외부에 반환가능하다.  

상위 위젯에서 `CounterProvider` 를 생성만 하면 하위 위젯에서 언제든지 접근가능하게 된다.  

```dart
import 'package:count/bloc/counter_provider.dart';
import 'package:flutter/material.dart';
import 'bloc/counter_bloc.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Retrieve Text Input',
      home: Scaffold(
          appBar: AppBar(title: Text("카운터")),
          body: CounterProvider(child: Counter())), 
          // 최상위에서 provider 클래스로 인스턴스 생성 
          // Counter 내부에선 of 메서드로 bloc 객체를 가져올 수 있다.  
    );
  }
}

class Counter extends StatefulWidget {
  @override
  _CounterState createState() => _CounterState();
}

class _CounterState extends State<Counter> {
  @override
  Widget build(BuildContext context) {
    CounterBloc counterBloc = CounterProvider.of(context);
    // 이미 부모 위젯에서 Provider 내부에 bloc 객체를 생성해 두었기에 of 로 가져오기만 하면 된다.  

    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          RaisedButton(child: Text("Add"), onPressed: counterBloc.addCount),
          StreamBuilder<int>(
              stream: counterBloc.count,
              initialData: 0,
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  return Text("${snapshot.data}",
                      style: TextStyle(fontSize: 30));
                }
              })
        ],
      ),
    );
  }
}
```


# Provider 패턴  

> https://pub.dev/packages/provider

|name|description|
|----|----|
`Provider` | The most basic form of provider. It takes a value and exposes it, whatever the value is.
`ListenableProvider` | A specific provider for Listenable object. ListenableProvider will listen to the object and ask widgets which depend on it to rebuild whenever the listener is called.  
`ChangeNotifierProvider` | A specification of ListenableProvider for ChangeNotifier. It will automatically call `ChangeNotifier.dispose` when needed.  
`ValueListenableProvider` | Listen to a ValueListenable and only expose ValueListenable.value.  
`StreamProvider` | Listen to a Stream and expose the latest value emitted.  
`FutureProvider` | Takes a Future and updates dependents when the future completes.  


위의 `BloC` 패턴에서 사용된 `Provider` 와 비슷하나  
`Provider` 패턴이 제공하는 데이터는 `BloC`객체가 아닌 `Notifier` 라는 알림객체를 제공한다.     

가장 자주 사용되는 `ChangeNotifierProvider` 객체를 사용해보자.  

먼저 `state` 관리를 위한 알림객체 정의  

```dart 
import 'package:flutter/material.dart';

class CountNotify with ChangeNotifier {
  int _count = 0; // 관리대상 state

  int get count => _count;

  void increment() {
    _count++;
    notifyListeners(); // 상태가 변했을때 알림!
  }
}
```


알림객체를 사용하기 위해 `MultiProvider` 위젯을 사용해야 한다.  

`providers` 속성으로 제공할 알림 객체를 생성하고  
`child` 속성으로 `Consumer` 위젯으로 감싸 알림객체를 사용할 하위 위젯을 생성한다.  

```dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'counter.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (BuildContext context) {
          return CountNotify(); //알림 객체 생성 
        })
      ],
      child: Consumer<CountNotify>( //알림 객체를 사용할 하위 위젯 
        builder: (BuildContext context, value, Widget child) {
          return MaterialApp(
            title: 'Flutter Demo',
            theme: ThemeData(primarySwatch: Colors.blue),
            home: MyHomePage(title: 'Flutter Demo Home Page'),
          );
        },
      ),
    );
  }
}
```

중요한건 제너릭에 제공될 알림 객체 타입을 설정해야 한다.  
`Consumer<CountNotify>`   

이제 하위객체에서 알림객체를 가져와 알림을 생성하고 state를 가져오는 코드를 알아보자.  

```dart  
class MyHomePage extends StatelessWidget {
  final String title;

  const MyHomePage({Key key, this.title}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final counter = Provider.of<CountNotify>(context);
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('You have pushed the button this many times:'),
            Text('${counter.count}', style: Theme.of(context).textTheme.display1),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: counter.increment,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}
```

`Provider.of`를 통해 `Provider` 가 제공하는 알림객체를 가져올 수 있다.  
가져온 알림객체 내부의 `state`(count변수)를 `getter` 메서드로 바로 가져올 수 있다.

특이한점은 `StatelessWidget`임에도 불구하고 `counter.count` 증가에 따라 숫자가 변화한다는 것.   
아마 `Consumer` 위젯으로 생성할때 변화되는 부분만 별도의 처리를 해주지 않았나 싶다.  

