---
title:  "Flutter - Images - Display images from the internet
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

## Image

Text를 위한 여러 위젯이 있듯이 Image를 위한 위젯역시 많다.  

> https://api.flutter.dev/flutter/widgets/Image-class.html

상속구조  
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > Image`  

이미지를 가져오기 위한 Image클래스의 여러 메서드, 필드를 알아보자.  

### Image.network() constructor.

> 주의: 에뮬레이터에서 먼저 인터넷이 되는지 확인하자

이미지를 Url로 네트워크로 가져오기 위한 생성자, 

```dart
Image.network(
  'https://picsum.photos/250?image=9',
)
```

`Image.network()`로 만들어진 이미지는 단순 출력용으로 사용되는 경우가 많다(애니메이션 처리가 안되고 캐싱을 통한 빠른 로딩 역시 안된다고 함)  

```dart
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    var title = 'Web Images';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: Image.network(
          'https://picsum.photos/250?image=9',
        ),
      ),
    );
  }
}
```

![flutter16]({{ "/assets/flutter/flutter16.png" | absolute_url }}){: width="400" }  


## Fade in images with a placeholder

이미지가 띄어지기 전 placeholder(로딩 효과)를 적용해보자.  


```dart
FadeInImage.memoryNetwork(
  placeholder: kTransparentImage,
  image: 'https://picsum.photos/250?image=9',
);
```

동그라미가 돌아가는 효과를 주기 위해 `kTransparentImage`라는 int형 static 전역으로 생성된 배열을 사용하는데  
`package:transparent_image/transparent_image.dart` 파일에 있다.  

패키지 설치과정은 아래와 같다.  

> https://flutter.dev/docs/cookbook/images/fading-in-images#in-memory  

![flutter17]({{ "/assets/flutter/flutter17.png" | absolute_url }})  


패키지 안의  `pubspec.yaml`파일을 열어 `dependencies`부분에 아래와 같이 추가  

```properties
...
...
dependencies:
  flutter:
    sdk: flutter
  # The following adds the Cupertino Icons font to your application.
  # Use with the CupertinoIcons class for iOS style icons.
  cupertino_icons: ^0.1.2
  transparent_image: ^1.0.0

dev_dependencies:
  flutter_test:
    sdk: flutter
...
...
```

`transparent_image: ^1.0.0`를 추가한다.  

이후 아래 명령을 해당 패키지 위치에서 실행  
`$ flutter pub get`  
(사전에 flutter 실행파일 위치가 환경변수로 등록되어 있어야 한다)  

`placeholder: kTransparentImage`의 역할은 이미지가 띄어지기 전에 loading 표시를 하기위한 설정  

메모리상에 `kTransparentImage`배열을 올려두고 사용하기 때문에 `FadeInImage.memoryNetwork`로 이미지 위젯을 만들고 `placeholder`속성을 처리하는 듯 하다.  
```dart
import 'package:flutter/material.dart';
import 'package:transparent_image/transparent_image.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final title = 'Fade in images';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: Stack(
          children: <Widget>[
            Center(child: CircularProgressIndicator()),
            Center(
              child: FadeInImage.memoryNetwork(
                placeholder: kTransparentImage,
                image: 'https://picsum.photos/250?image=9',
              ),
            ),
          ],
        ),
      ),
    );
  }
}

```



굳이 패키지 설정 및 `kTransparentImage`사용하지 않고 로딩용 gif 파일을 사용하려면 해당 파일을 패키지 내의 특정 폴더에 집어넣고 사용하면 된다.  
`FadeInImage.assetNetwork` 사용

그전에 사용할 로딩용 이미지 저장 폴더를 설정
```properties
...
flutter:

# The following line ensures that the Material Icons font is
# included with your application, so that you can use the icons in
# the material Icons class.
uses-material-design: true
assets:
  - assets/loading.gif
...
```

패키지 안에 `assets`폴더 생성후 로딩용 파일을 저장한다.  
> `FadeInImage.assetNetwork`사용


```dart
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final title = 'Fade in images';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: Center(
          child: FadeInImage.assetNetwork(
            placeholder: 'assets/loading.gif',
            image: 'https://picsum.photos/250?image=9',
          ),
        ),
      ),
    );
  }
}
```