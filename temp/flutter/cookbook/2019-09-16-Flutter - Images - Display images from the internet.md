---
title:  "Flutter - Images - Display images from the internet
!"

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

# Image

> https://flutter.dev/docs/cookbook/images/network-image  

상속구조   
`Object > Diagnosticable > DiagnosticableTree > Widget > StatefulWidget > Image`  

이미지를 가져오기 위한 `Image`클래스의 여러 메서드, 필드를 알아보자.  

## Image.network() constructor.  

> 주의: 에뮬레이터에서 먼저 인터넷이 되는지 확인하자

이미지를 Url로 네트워크로 가져오기 위한 생성자, 

```dart
Image.network('https://picsum.photos/250?image=9')
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
        appBar: AppBar(title: Text(title)),
        body: Image.network('https://picsum.photos/250?image=9'),
      ),
    );
  }
}
```

![flutter16](/assets/flutter/flutter16.png){: width="400" }  


# Fade in images with a placeholder

> https://flutter.dev/docs/cookbook/images/fading-in-images

이미지가 띄어지기 전 `placeholder`(로딩 효과)가 출력되며 천천히 fade in 되는 효과원한다면 `FadeInImage` 객체를 사용하면 된다.  

```dart
FadeInImage.memoryNetwork(
  placeholder: 'assets/loading.gif',
  image: 'https://picsum.photos/250?image=9',
);
```

`/assets` 디렉토리에 로딩바로 사용할 이미지를 저장해놓고 `placeholder`로 사용할 수 있다.  

별도의 로딩 이미지를 사용하기 싫다면 `kTransparentImage` 라는 바이너리 배열을 사용해 원형으로 돌아가는 로딩 애니메이션을 구현할 수 있다.  

`kTransparentImage` 객체를 사용하려면 패키지 설치가 필요한다.  
> https://pub.dev/packages/transparent_image

```dart
FadeInImage.memoryNetwork(
  placeholder: kTransparentImage,
  image: 'https://picsum.photos/250?image=9',
);
```

# Work with cached images

> https://flutter.dev/docs/cookbook/images/cached-images

때에따라선 이미지를 캐시에 저장시키고 지속적으로 사용할 때가 있다.  
`CachedNetworkImage` 객체를 사용하면 된다. 

`CachedNetworkImage` 객체를 사용하려면 패키지 설치가 필요하다.  
> https://pub.dev/packages/cached_network_image

위의 `FadeInImage`와 사용법은 다르지 않으나 속성명만 조금 다르다.  

```dart
CachedNetworkImage(
  placeholder: (context, url) => CircularProgressIndicator(),
  imageUrl: 'https://picsum.photos/250?image=9',
);
```

`placeholder` 속성을 사용해 별도의 패키지나 이미지없이 로딩 애니메이션 구현가능 