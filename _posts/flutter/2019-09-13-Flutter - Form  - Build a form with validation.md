---
title:  "Flutter - Form  - Build a form with validation!"

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

## Form

어플에도 당연히 사용자 입력을 위한 인터페이스가 필요하다. 로그인시 이메일과 비밀번호를 입력하는 행위.  
텍스트 필드 하나를 통해 사용자가 정보를 잘못 입력하면 이에대한 처리를 하도록 코딩해보자.  

1. Create a Form with a GlobalKey.  
2. Add a TextFormField with validation logic.  
3. Create a button to validate and submit the form.  

```js
import 'package:flutter/material.dart';

void main() {
  runApp(MyCustomForm());
}

// Define a custom Form widget.
class MyCustomForm extends StatefulWidget {
  @override
  MyCustomFormState createState() {
    return MyCustomFormState();
  }
}

// Define a corresponding State class.
// This class holds data related to the form.
class MyCustomFormState extends State<MyCustomForm> {
  // Create a global key that uniquely identifies the Form widget
  // and allows validation of the form.
  //
  // Note: This is a `GlobalKey<FormState>`,
  // not a GlobalKey<MyCustomFormState>.
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    // Build a Form widget using the _formKey created above.
    return Form(
        key: _formKey,
        child: ...// Build this out in the next steps.
    );
  }
}
```

필드 입력값이 잘못되면 에러메세지를 화면에 표시하기 때문에 `StatefulWidget`을 사용한다.  

실제 출력되는 `MyCustomFormState`를 살펴보자.  

### GlobalKey, Form

처음보는 클래스가 2개 들어있다.  

> Global keys uniquely identify elements  

`final _formKey = GlobalKey<FormState>();`  
`GlobalKey`는 요소(위젯)을 대표하는 유니크한 값, 트리에 올라가거나 변경될 때 생성된다.  

따라서 `StatefulWidget`의 하나의 `Context`, `State`를 표시하는 값이다.(`StatefulWidget`의 경우 랜더링 될때 마다 다시 트리에 올라가기 때문)  

위의 경우 Form이 생성될 때 마다 Form에 해당하는 `GlobalKey`를 생성한다.  

`GlobalKey`가 비교적 많은 성능을 필요로 해서 `Key`, `ValueKey`, `ObjectKey`, `UniqueKey`로 대체해도 된다고 한다.  

`GlobalKey`는 당연히 Form에 접근하기 위해 사용한다.  

### TextFormField

Form을 만드는 것 까지는 위와 같고 안에 TextField를 넣으려면 `TextFormField`를 사용한다.  

```js
TextFormField(
  decoration: InputDecoration(
      labelText: 'Enter your username'
  ),
  validator: (value) {
    if (value.isEmpty) {
      return 'Please enter some text';
    }
    return null;
  },
),
```
생성자로 `validator`속성을 초기화 하고 value가 매개변수로 들어가는 익명클래스를 지정한다.  
데이터가 비어있다면 아래같이 경고문구를 출력하는 역할을 수행하도록 하기 위한 메서드.  

![flutter11]({{ "/assets/flutter/flutter11.png" | absolute_url }}){: width="400" }  

단순히 값이 있다면 null을, 없다면 경고문구르 반환하는 익명메서드이다.  

### Create a button to validate and submit the form

이제 버튼을 만들고 버튼을 누르면 Form의 상태를 확인해 반환받은 경고문구를 출력할지, 해당 문구를 Submit할지 지정하는 코드를 작성하면 된다.  

```js
RaisedButton(
  onPressed: () {
    if (_formKey.currentState.validate()) {
      Scaffold.of(context).showSnackBar(
        SnackBar(content: Text('Processing Data'))
      );
    }
  },
  child: Text('Submit'),
);
```

`Scaffold.of(context)` 메서드로 `StatefulWidget`위젯의 현 상태를 가져와 snackbar를 출력한다.  

조건문을 보면 `GlobalKey`인 `_formKey`를 통해 Form의 현 상태에 접근하는 것을 알 수 있다.  

`currentState`는 위의 `TextFormField`정의할때 같이 정의한 `validate`필드에 설정한 메서드를 호출하는 것이 아니다. Form의 `validate`메서드를 호출한다.  

Form의 모든 필드값의 `validate`를 호출하여 값이 모든 필드의 값이 null이면 true, 아니면 false를 반환한다.  

이제 Form, Textfield, Button을 각각 만들었다, 이 3개를 Form에 합쳐주기만 하면 된다.  

```js
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final appTitle = 'Form Validation Demo';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: Text(appTitle),
        ),
        body: MyCustomForm(),
      ),
    );
  }
}

class MyCustomForm extends StatefulWidget {
  @override
  MyCustomFormState createState() {
    return MyCustomFormState();
  }
}

class MyCustomFormState extends State<MyCustomForm> {
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          TextFormField(
            decoration: InputDecoration(
                labelText: 'Enter your username'
            ),
            validator: (value) {
              if (value.isEmpty) {
                return 'Please enter some text';
              }
              return null;
            },
          ),
          RaisedButton(
              onPressed: () {
                if (_formKey.currentState.validate()) {
                  // If the form is valid, display a Snackbar.
                  Scaffold.of(context)
                      .showSnackBar(SnackBar(content: Text('Processing Data')));
                }
              },
              child: Text('Submit'),
          ),
        ],
      ),
    );
  }
}
```

Form의 children 속성으로 `<Widget>[...]`위젯 배열을 설정,  
Form의 각종 입력필드와 버튼을 추가하면 된다.  


### TextField

Flutter는 2가지 텍스트 입력 폼을 제공한다.  

`TextField` 상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > TextField`


`TextFormField` 상속구조
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > FormField<String> > TextFormField`


`TextField`에서 `decoration`속성을 통해 border난 기본 표시값등을 설정 가능하다.  
> https://api.flutter.dev/flutter/material/TextField-class.html

```js
TextField(
  obscureText: true,
  decoration: InputDecoration(
    border: OutlineInputBorder(),
    labelText: 'Password',
  ),
)
```
> `InputDecoration`
> https://api.flutter.dev/flutter/material/InputDecoration-class.html

`TextFormField`는 `TextField`를 포함하는 위젯으로 Form에 필요한 통합적인 기능을 더 제공한다.  (`validation`같은)   
`TextField`기능을 `TextFormField`도 모두 가지고 있기 때문에, 또한 대부분 Form 안에 텍스트를 입력하기 때문에 `TextFormField`를 사용한다.  

## Focus and text fields

웹에서도 특정 코드를 통해 포커스를 지정할 수 있듯이 Flutter도 마찬가지로 지정 가능하다.  

만약 단순히 TextField가 화면에 표시됨가 동시에 포커스를 맞추고 싶다면 생성시 `autofocus` 속성을 true로 초기화 하자.  

```js
TextField(
  autofocus: true,
);
```

엔터를 치면 포커스를 이동시킨다던가, 주민번호 합 6글자를 다 쓰면 뒤 7자리를 쓸수 있도록, 휴대전화 번호를 3 - 4 - 4 형식으로 자동 포커싱을 맞추는 등의 행위를 할 경우 포커스 작업을 해줘야한다.  

### FocusNode  

포커스를 관리하고 싶은 `TextField`를 위한 `FocusNode`를 생성하고 `TextField`생성시에 `focusNode`속성으로 초기화 하면 된다.  

```js
TextField(
  focusNode: myFocusNode,
);
```
위와 같이 `focusNode`속성 설정으로 `FocusNode`객체를 focus tree에 넣어 포커스를 조절한다고 한다.   

후에 특정 동작을 한후 포커스를 맞추고 싶다면 아래 메서드를 호출.

`FocusScope.of(context).requestFocus(myFocusNode)`

`TextField`객체 생성시 사용했던 `FocusNode`객체를 지정한다.  

> https://flutter.dev/docs/cookbook/forms/focus

위 사이트에서 제공하는 플로팅 버튼을 클릭하면 아래의 `TextField`로 포커싱 되도록 설정해보자.  
```js
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Text Field Focus',
      home: MyCustomForm(),
    );
  }
}

// Define a custom Form widget.
class MyCustomForm extends StatefulWidget {
  @override
  _MyCustomFormState createState() => _MyCustomFormState();
}

class _MyCustomFormState extends State<MyCustomForm> {
  FocusNode myFocusNode;

  @override
  void initState() {
    super.initState();
    myFocusNode = FocusNode();
  }

  @override
  void dispose() {
    myFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Text Field Focus'),
      ),
      body: Column(
        children: [
          TextField(
            autofocus: true,
          ),
          TextField(
            focusNode: myFocusNode,
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => FocusScope.of(context).requestFocus(myFocusNode),
        tooltip: 'Focus Second Text Field',
        child: Icon(Icons.edit),
      ),
    );
  }
}
```

`initState`, `dispose` 오버라이드 메서드를 통해 `TextField`에 사용할 `FocusNode`를 생성하고 삭제한다.  

