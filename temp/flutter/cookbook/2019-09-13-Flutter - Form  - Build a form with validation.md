---
title:  "Flutter - Form  - Build a form with validation, Handle changes to a text field!"

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
# Build a form with validation

> https://flutter.dev/docs/cookbook/forms/validation

어플에도 당연히 **사용자 입력을 위한 인터페이스**가 필요하다.  
텍스트 필드를 통해 사용자가 정보를 잘못 입력하면 이에대한 처리를 하도록 코딩해보자.  

1. Create a Form with a GlobalKey.  
2. Add a TextFormField with validation logic.  
3. Create a button to validate and submit the form.  


## 1. Create a Form with a GlobalKey.  

`Form` 상속구조 
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > Form`  

필드 입력값이 잘못되면 에러메세지를 화면에 표시해야 하기 때문에 `MyCustomForm`는 `StatefulWidget`을 사용.  

```dart
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
      child: buildFormBody(_formKey, context)
    );
  }
}
```

`Form`의 `key` 속성에 지정된 `GlobalKey` 를 통해 Form의 상태(`State`) 에 접근가능함으로 의무적으로 설정하자.

### GlobalKey

> Global keys uniquely identify elements  

`GlobalKey`는 **요소(위젯)을 대표하는 유니크한 값**, 키를 설정한 위젯이 트리에 생성, 변경될 때 `GlobalKey`도 생성, 변경된다.  

위젯과 항상 같이 업데이트 되기에 실시간으로 `GlobalKey`를 통해 `StatefulWidget`의 `Context`, `State`을 가져올 수 있다. (`StatefulWidget`의 경우 랜더링 될때 마다 다시 트리에 올라가기 때문)  
> `GlobalKey`가 비교적 많은 성능을 필요로 해서 상황에 따라 `Key`, `ValueKey`, `ObjectKey`, `UniqueKey`로 대체가능하다.  

위의 경우 `Form`이 생성될 때 마다 `Form`에 해당하는 `GlobalKey`를 생성한다.  

`GlobalKey`는 당연히 Form에 접근하기 위해 사용한다.  

## 2. Add a TextFormField with validation logic.

이제 `Form`에 넣을 입력 인터페이스 `TextFormField`를 정의하고 유효성 검사 메서드만 추가하면 된다.  

> https://api.flutter.dev/flutter/material/TextFormField-class.html

`TextFormField`의 `validator` 속성을 사용해 유효성 검사가 가능하다.  

```dart
Widget buildFormBody(_formKey, context) {
  return Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: <Widget>[
      TextFormField(
        validator: (value) {
          if (value.isEmpty) return 'Please enter some text';
          else return null;
        },
      )
    ],
  );
}
```

![flutter11](/assets/flutter/flutter11.png){: width="400" }  

`validator` 반환값이 있다면 유효하지 않은것으로 간주하고 반환값으로 에러메세지를 출려한다.   

![flutter31](/assets/flutter/flutter31.png){: width="400" }  

## 3. Create a button to validate and submit the form.  

이제 버튼을 만들고 버튼을 누르면 `TextFormField`의 `validator`를 확힌후 경고문구를 출력할지  
`Submit`할지 지정하는 코드를 작성하면 된다.  

```dart
Widget buildFormBody(_formKey, context) {
  return Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: <Widget>[
      TextFormField(...),
      Padding(
        padding: const EdgeInsets.symmetric(vertical: 16.0),
        child: RaisedButton(
          child: Text('Submit'),
          onPressed: () {
            if (_formKey.currentState.validate()) {
              Scaffold.of(context).showSnackBar(SnackBar(content: Text('Processing Data')));
            }
          },
        ),
      ),
    ],
  );
}

```

조건문을 보면 `GlobalKey`인 `_formKey`를 통해 `Form`의 `currentState.validate` 에 접근하는 것을 알 수 있다.  

`Form`의 `validate`메서드를 호출하게 되면 `TextFormField`를 포함한 `Form` 내부의 각종 필드의 `validate`필드에 설정한 메서드를 호출한다.  

Form의 모든 필드값의 `validate`를 호출하여 값이 모든 필드의 값이 `null`이면 `true`, 아니면 `false`를 반환한다.  

최종적으로 `Scaffold.of(context)` 메서드로 `StatefulWidget`위젯에 `SnackBar`를 출력한다.  

# Create and style a text field - TextField, TextFormField

> https://flutter.dev/docs/cookbook/forms/text-input

Flutter는 2가지 텍스트 입력 폼을 제공한다.  

`TextField` 상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > TextField`


`TextFormField` 상속구조
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > FormField<String> > TextFormField`

`TextFormField`는 `TextField`를 포함하는 위젯으로 `Form`에 필요한 통합적인 기능을 더 제공한다.  (`validation`같은)   
`TextField`기능을 `TextFormField`도 모두 가지고 있기 때문에, 또한 대부분 `Form` 안에 텍스트를 입력하기 때문에 `TextFormField`를 사용한다.  

`TextField`에서 `decoration`속성을 통해 `border` 와 같은 기본 표시값등을 설정 가능하다.  
> https://api.flutter.dev/flutter/material/TextField-class.html

```dart
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


# Handle changes to a text field

> https://flutter.dev/docs/cookbook/forms/text-field-changes

`TextField`의 이벤트처리를 위한 방법은 2가지가 존재한다.  

> `TextFormField`는 `TextField`보다 많은 기능을 가짐으로 똑가은 이벤트처리 설정가능  

1. Supply an onChanged() callback to a TextField or a TextFormField  
2. Use a TextEditingController  

`onChanged()` 콜백 메서드를 정의하는것, `TextEditingController` 객체를 정의하고 `TextField`에 연결하는 것.  


## 1. Supply an onChanged() callback to a TextField or a TextFormField  

첫번째 방법인 `onChanged()` 콜백메서드를 정의해서 `TextField` 이벤트처리해보자.  

```dart
class MyCustomForm extends StatefulWidget {
  @override
  _MyCustomFormState createState() => _MyCustomFormState();
}

class _MyCustomFormState extends State<MyCustomForm> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Retrieve Text Input')),
      body: TextField(
        onChanged: (text) {
          print("First text field: $text");
        },
      ),
    );
  }
}

```

`TextField`에 변화가 생길때 `onChanged`가 호출되고 `text`파라미터에 `TextField`의 `value`가 들어온다.  

![flutter32](/assets/flutter/flutter32.png){: width="400" }  


`print` 함수로 아래 문구처럼 출력됨  
```
I/flutter (29797): First text field: 1
I/flutter (29797): First text field: 12
I/flutter (29797): First text field: 123
I/flutter (29797): First text field: 1234
I/flutter (29797): First text field: 12345
```

## 2. Use a TextEditingController  

`TextEditingController` 을 사용하려면 아래 과정을 거쳐야 한다.  

1. `callback method` 정의  
2. `StatefulWidget`생성시 `addListener` 로 `callback method` 등록  
3. `StatefulWidget`소멸시 `dispose`메서드호출, `TextController` 해제  

이번엔 `onChanged` 속성이 아닌 `TextController`를 사용해 `TextField`의 `value`를 가져와보자.  

```dart
class MyCustomForm extends StatefulWidget {
  @override
  _MyCustomFormState createState() => _MyCustomFormState();
}

class _MyCustomFormState extends State<MyCustomForm> {
  final myController = TextEditingController();

  _printLatestValue() {
    print("Second text field: ${myController.text}");
  }

  @override
  void initState() {
    super.initState();
    myController.addListener(_printLatestValue);
  }

  @override
  void dispose() {
    myController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: Text('Retrieve Text Input')),
        body: TextField(controller: myController));
  }
}
```

`initState` 와 `dispose` 메서드가 추가되긴 했지만 복잡한 구조는 아니다.  

`initState` 와 `dispose`  는 초기 `StatefulWidget` 생성, 소멸시 호출되는 이니셜라이져, 소멸자이다.  
> 컨트롤러는 꼭 메모리해제가 필요하다. 

`addListener`로 `TextField` 변경시 호출할 콜백메서드를 등록할 수 있다.  

이니셜라이저 호출 후 `build` 실행시 `controller` 속성을 사용해 `TextEditingController`를 등록하여 `TextField` 를 생성한다.  


또한 `myController.text` 처럼 각종 속성을 사용해 `TextField`의 정보를 얻는것도 가능하다.  

# Focus and text fields

> https://flutter.dev/docs/cookbook/forms/focus

웹에서도 특정 코드를 통해 `Form` 내부 `field` 의 포커스를 지정할 수 있듯이 Flutter 에서도 마찬가지로 지정 가능하다.  

만약 단순히 `TextField`가 화면에 표시됨가 동시에 포커스를 맞추고 싶다면 생성시 `autofocus` 속성을 true로 초기화 하자.  

```dart
TextField(
  autofocus: true,
);
```

엔터를 치면 포커스를 이동시킨다던가, 주민번호 합 6글자를 다 쓰면 뒤 7자리를 쓸수 있도록, 휴대전화 입력시 자동 포커싱을 맞추는 등의 행위를 할 경우 포커스 작업을 해줘야한다.  

## FocusNode  

포커스를 관리하고 싶은 `TextField`를 위한 `FocusNode`를 생성하고  
`FocusNode`로 해당 `TextField`에 포커싱처리가 가능하다.  

먼저 포커스 노드 생성  

```dart
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
}
```

`FocusNode` 도 객체 생성, 소멸시 메모리 해제해주어야 한다.  

`FocusNode` 생성 후에 `TextField`의 `focusNode` 속성을 사용해 연결해주어야 한다.  

```dart
TextField(
  focusNode: myFocusNode,
);
```

`focusNode`속성 설정으로 `focus tree`에 넣어 포커스를 조절한다고 한다.   

후에 특정 동작을 한후 포커스를 맞추고 싶다면 아래 메서드를 호출.

`FocusScope.of(context).requestFocus(myFocusNode)`

`requestFocus` 메서드에 객체 생성시 사용했던 `FocusNode`객체를 파라미터로 지정한다.  

위 사이트에서 제공하는 플로팅 버튼을 클릭하면 아래의 `TextField`로 포커싱 되도록 설정해보자.  

```dart
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
      appBar: AppBar(title: Text('Text Field Focus')),
      body: Column(
        children: [
          TextField(autofocus: true),
          TextField(focusNode: myFocusNode)
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

`floatingActionButton` 버튼 클릭시 두번째 `TextField` 로 포커싱을 맞춘다.  
