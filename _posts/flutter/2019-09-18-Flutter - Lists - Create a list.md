---
title:  "Flutter - Lists - Create a grid list!"

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

## List.generate(...)

> https://kouzie.github.io/flutter/Flutter-Design-Display-a-snackbar/#gridview-orientationbuilder

GridView를 사용하여 화면에 여러 위젯을 타일형태로 출력하였다.  
`<Widget>[...]` 위젯 배열을 통해 `GridView`를 구성하였는데 `List.generate`를 통해서 위젯 배열을 생성가능하다.  

```js
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final title = 'Grid List';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: GridView.count(
          crossAxisCount: 2,
          children: List.generate(
            100,
            (index) {
              return Text('Item $index');
            }
          ),
        ),
      ),
    );
  }
}
```

![flutter18]({{ "/assets/flutter/flutter18.png" | absolute_url }})  

## Create lists with different types of items

위에선 `List.generate()` 메서드를 통해 동일한 형태의 위젯들을 배열로 대거 생성하였다.  

객체지향의 다형성은 Flutter에도 적용되기에 서로 다른 타입의 위젯을 배열로 만들어 `GridView`나 다른 `ListView`의 `children`속성값으로 사용해도 된다.   

먼저 위젯으로 사용할 서로 다른 타입의 2개 클래스를 정의한다.  

```js
// The base class for the different types of items the list can contain.
abstract class ListItem {
  //아무것도 없음
}

class HeadingItem implements ListItem {
  final String heading;
  HeadingItem(this.heading);
}
class MessageItem implements ListItem {
  final String sender;
  final String body;
  MessageItem(this.sender, this.body);
}
```
그리고 `List.generate(...)`메서드를 통해 `ListItem`배열을 생성하는데 

```js
final items = List<ListItem>.generate(
  1200,
  (i) => i % 6 == 0
      ? HeadingItem("Heading $i")
      : MessageItem("Sender $i", "Message body $i"),
);
```

Flutter에서도 제너릭처럼 사용할 수 있다.

6으로 나누어 떨어지면 `HeadingItem`을, 아니라면 `MessageItem`을 생성, 반환한다.  
만들어진 배열은 `items` 전역변수에 매핑한다.  


만들어진 `items`을 사용해 진정한 `ListView`에서 사용할 `ListTile`(위젯)들을 만든다.   

> https://kouzie.github.io/flutter/Flutter-Design-Add-a-Drawer-to-a-screen/#listview-listtile

기존에는 `ListView` 생성자를 통해 `children`속성에 `<Widget>[...]`을 지정하여 사용하였는데  
`List.generate(...)`를 사용하려면 `ListView.builder()`를 사용하여 ListView에 사용할 `ListTile`(위젯) 배열을 만든다.  

```js
ListView.builder(
  itemCount: items.length,
  itemBuilder: (context, index) {
    final item = items[index];

    if (item is HeadingItem) {
      return ListTile(
        title: Text(
          item.heading,
          style: Theme.of(context).textTheme.headline,
        ),
      );
    } else if (item is MessageItem) {
      return ListTile(
        title: Text(item.sender),
        subtitle: Text(item.body),
      );
    }
  },
);
```

통합 코드  

```js
import 'package:flutter/material.dart';

abstract class ListItem {}

class HeadingItem implements ListItem {
  final String heading;
  HeadingItem(this.heading);
}

class MessageItem implements ListItem {
  final String sender;
  final String body;
  MessageItem(this.sender, this.body);
}

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final List<ListItem> items = List<ListItem>.generate(
    1200,
        (i) => i % 6 == 0
        ? HeadingItem("Heading $i")
        : MessageItem("Sender $i", "Message body $i"),
  );

  @override
  Widget build(BuildContext context) {
    final title = 'Mixed List';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: ListView.builder(
          itemCount: items.length,
          itemBuilder: (context, index) {
            final item = items[index];

            if (item is HeadingItem) {
              return ListTile(
                title: Text(
                  item.heading,
                  style: Theme.of(context).textTheme.headline,
                ),
              );
            } else if (item is MessageItem) {
              return ListTile(
                title: Text(item.sender),
                subtitle: Text(item.body),
              );
            }
            return null;
          },
        ),
      ),
    );
  }
}
```

## Create a horizontal list

> https://flutter.dev/docs/cookbook/lists/horizontal-list

기존의 리스트 뷰에서 `scrollDirection: Axis.horizontal` 속성을 추가하면 된다.  

`ListTile`은 `horizontal` 속성을 지원하지 않음으로 `Container`로 대체  

```js
import 'package:flutter/material.dart';

abstract class ListItem {}

class HeadingItem implements ListItem {
  final String heading;
  HeadingItem(this.heading);
}

class MessageItem implements ListItem {
  final String sender;
  final String body;
  MessageItem(this.sender, this.body);
}

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final List<ListItem> items = List<ListItem>.generate(
    1200,
        (i) => i % 6 == 0
        ? HeadingItem("Heading $i")
        : MessageItem("Sender $i", "Message body $i"),
  );

  @override
  Widget build(BuildContext context) {
    final title = 'Mixed List';

    return MaterialApp(
      title: title,
      home: Scaffold(
        appBar: AppBar(
          title: Text(title),
        ),
        body: ListView.builder(
          scrollDirection: Axis.horizontal,
          itemCount: items.length,
          itemBuilder: (context, index) {
            final item = items[index];

            if (item is HeadingItem) {
              return Container(
                child: Text(
                  item.heading,
                  style: Theme.of(context).textTheme.headline,
                ),
              );
            } else if (item is MessageItem) {
              return Container(
                child: Text(item.sender),
              );
            }
            return null;
          },
        ),
      ),
    );
  }
}
```

![flutter20]({{ "/assets/flutter/flutter20.png" | absolute_url }})  


## Create a CustomScrollView

일반적으로 `material`디자인을 위해 `Scaffold` 위젯을 생성하면 `appBar property`를 사용한다.  

일반적인 `ListView`는 스크롤하면 `appBar`는 상단에 그대로 남아있지만 `CustomScrollView`는 `appBar`까지 스크롤 가능하다.  

```js
Scaffold(
  body: CustomScrollView(
    slivers: <Widget>[]
  ),
);
```

`slivers`속성에 `Widget`배열을 요구하는데 `appBar`역할을 하는 `SliverAppBar`,  
내용물로 사용할 `List`역하을 하는 `SliverList` 를 요구한다.  
> 자매품으로 `SliverGrid`기 있다.  

```js
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  MyApp({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final title = 'Floating App Bar';

    return MaterialApp(
      title: title,
      home: Scaffold(
        body: CustomScrollView(
          slivers: <Widget>[
            SliverAppBar(
              title: Text(title),
              floating: true,
              flexibleSpace: Placeholder(),
              expandedHeight: 200,
            ),
            SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) ListTile(title: Text('Item #$index')),
                childCount: 1000,
              ),
            ),
          ],
        ),
      ),
    );
  }
} 
```

![flutter21]({{ "/assets/flutter/flutter21.png" | absolute_url }})  

`SliverAppBar`의 `floating` 속성은 아래 url을 참고하면 쉽게 알 수 있다.  

> https://api.flutter.dev/flutter/material/SliverAppBar-class.html

`pinned`, `snap`속성도 알려주니 꼭 참고!  

`SliverList`의 `delegate`속성으로 리스트 아이템들을 설정할 수 있다.  

`SilverList` 경우 `SliverChildBuilderDelegate`안에 ListTile을  
`SliverGrid` 경우 `SliverChildBuilderDelegate`안에 컨테이너들을 만들면 된다.  

> https://youtu.be/ORiTTaVY6mM


### Placeholder class

> A widget that draws a box that represents where other widgets will one day be added.
This widget is useful during development to indicate that the interface is not yet complete.  


개발시에 도움되는 클래스로 박스형태의 그림을 그려준다고 한다.  

위 그림 appBar부분의 x표시 출력된 이유가 Placeholder 설정떄문  


