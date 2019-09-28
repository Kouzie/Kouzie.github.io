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

## Return data from a screen  
 
`ModalRoute.of(context)` 혹은 `onGenerateRoute`를 통해 `Navigator.push()` 메서드로 `arguments`를 새로운 페이지 위젯에 전달하였다.  

이번엔 `Navigator.pop()`메서드로 원래 페이지로 돌아갈때 데이터를 반환해보자.  

```dart
class SelectionScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Pick an option'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: RaisedButton(
                onPressed: () {
                  Navigator.pop(context, 'Yep!');
                },
                child: Text('Yep!'),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: RaisedButton(
                onPressed: () {
                  Navigator.pop(context, 'Nope.');
                },
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

![flutter24]({{ "/assets/flutter/flutter24.png" | absolute_url }}){: width="400" }  

위처럼 버튼 2개를 가진 페이지이다.     
지금까진 매개변수는 `context`하나만 사용해 `Navigator.pop()`메서드호출해 왔는데 위의 코드를 보면  2번째 매개변수로 `String`이 존재한다.  

저 `Navigator.pop()`메서드의 2번째 매개변수를 기존 페이지가 어떻게 받는지만 알아내면 이번 파트내용은 끝이다.  

먼저 메인 페이지 스크린을 정의하자.  

```dart
void main() {
  runApp(MaterialApp(
    title: 'Returning Data',
    home: HomeScreen(),
  ));
}

class HomeScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Returning Data Demo'),
      ),
      body: Center(child: SelectionButton()),
    );
  }
}
```

![flutter25]({{ "/assets/flutter/flutter25.png" | absolute_url }}){: width="400" }  

단순 버튼 하나 있는 페이지로 버튼타입은 `SelectionButton`클래스로 우리가 직접 정의한 클래스이다.  

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
    );
  
    Scaffold.of(context)
      ..removeCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text("$result")));
  }
}
```

`build()`메서드를 보면 단순한 `RaisedButton()`을 반환한다, `onPressed` 하면 `_navigateAndDisplaySelection(context)`메서드를 호출하는....

`_navigateAndDisplaySelection()`메서드를 보면 새로운 문법이 등장했다.  

`await`라는 키워드와 함께 `Navigator.push()` 메서드의 값을 `result`가 참조하고 있다.  

그리고 `Scaffold.of()`메서드로 현재 `context`의 기존 `snackBar`를 지우고 받아온 `result`로 새로운 `snackBar`를 만들어 출력하는 코드같다.  

일단 `await`키워드를 사용하면 `Navigator.pop()`메서드의 2번째 매개변수를 가져올 수 있는 듯 하고 아래 코드만 이해하면 이번 챕터는 끝이다.  

```dart
Scaffold.of(context)
  ..removeCurrentSnackBar()
  ..showSnackBar(SnackBar(content: Text("$result")));
```

### Cascade notation (..) - doubel dot

dart의 문법중 하나로 chaining 을 사용하려면 dot 2개를 써야하나 봄.  

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

## Send data to a new screen  

> https://kouzie.github.io/flutter/Flutter-Navigation-Animate-a-widget-across-screens/#create-a-widget-with-arguments

위의 링크에선 `RouteSettings`을 사용해 `argument`를 넘기는 방식으로 새로운 페이지에 데이터를 전달했다.  

```dart
MaterialPageRoute(
  builder: (context) => ExtractArgumentsScreen(),
  settings: RouteSettings(
    arguments: ScreenArguments(
      'Extract Arguments Screen',
      'This message is extracted in the build method.',
    ),
  ),
),
```
하지만 생각해 보면 굳이 이런방식으로 넘길 필요 없이 생성할 페이지의 생성자에 전달하고 싶은 데이터 `arguments`를 넘기면 된다.

```dart
MaterialPageRoute(
  builder: (context) => ExtractArgumentsScreen(
    data: ScreenArguments(
      'Extract Arguments Screen',
      'This message is extracted in the build method.',
    ),
  ),
),
```

생성자에 대한 코드가 추가되겠지만 `ModalRoute.of(context)` 코드는 생략 가능하다.   

어떤 방식을 사용하던 개발자의 자유이다.  

리스트 배열을 만들어 해당 리스트 요소를 클릭하면 설정해 둔 데이터를 자세히 출력하는 페이지를 생성해보자.  

```dart
class Todo {
  final String title;
  final String description;

  Todo(this.title, this.description);
}

final todos = List<Todo>.generate(
  20,
  (i) => Todo(
        'Todo $i',
        'A description of what needs to be done for Todo $i',
      ),
);
```
우선 각 리스트에 저장될 데이터, 리스트를 클릭하면 전달될 데이터를 정의,  
그리고 `List<Todo>.generate()`메서드를 통해 리스트로 생성.  

데이터가 전달되어 출력된 새 페이지 `DetailScreen`정의  

```dart
class DetailScreen extends StatelessWidget {
  final Todo todo;

  DetailScreen({Key key, @required this.todo}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(todo.title),
      ),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Text(todo.description),
      ),
    );
  }
}
```
생성자로 `Todo`객체를 넘겨 데이터를 전달한다.  

메인 페이지로 사용할 `TodosScreen`생성 + 람다식으로 `Todo` 배열 전달  
```dart
void main() {
  runApp(MaterialApp(
    title: 'Passing Data',
    home: TodosScreen(
      todos: List.generate(
        20,
        (i) => Todo(
              'Todo $i',
              'A description of what needs to be done for Todo $i',
            ),
      ),
    ),
  ));
}

class TodosScreen extends StatelessWidget {
  final List<Todo> todos;

  TodosScreen({Key key, @required this.todos}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Todos'),
      ),
      body: ListView.builder(
        itemCount: todos.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(todos[index].title),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => DetailScreen(todo: todos[index]),
                ),
              );
            },
          );
        },
      ),
    );
  }
}

```

`itemBuilder`를 사용해 `todos`배열을 `ListTile`로 생성  
