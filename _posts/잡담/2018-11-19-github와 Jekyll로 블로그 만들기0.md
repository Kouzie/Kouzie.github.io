---
title:  "Github page 만들기!"
read_time: false
share: false
toc: true
author_profile: false

classes: wide

categories:
  - 잡담
tags:
  - Jekyll
  - github
---

### Github and Jekyll

깃허브 아이디를 만들고 깃허브에서 블로그를 만드는 방법, Jeky11의 테마 설정, Disqus로 댓글 달기 등을 설명할 것입니다.

깃허브 ID를 만들고 Create a new repository를 통해 다음과 같이 설정합니다. 중요한건 Repository name이  owner의 이름+.github.io 양식을 지겨줘야 하는것.
![Create a new repository]({{ "/assets/posts/create Repository.PNG" | absolute_url }})
그리고 settings에 들어가 좀 내리다 보면 그림의 항목의 Choose theme버튼을 클릭하고 맘에드는 아무 테마나 적용한다!
![choeese theme]({{ "/assets/posts/choose theme.PNG" | absolute_url }})
그리고 Repository name으로 설정했던 주소로 접속해보면 짜잔!
![fisrt github page]({{ "/assets/posts/first github page.PNG" | absolute_url }})
첫번째 github page로 만든 블로그가 생겼다.

***

### Jekyll 테마

위처럼 처음부터 만들어서 지킬 사용법대로 하나하나 따라가도 된다. 지킬 디렉토리 구조를 아래와 같다.
<!--
```sh
.
├── _config.yml
├── _data
|   └── members.yml
├── _drafts
|   ├── begin-with-the-crazy-ideas.md
|   └── on-simplicity-in-technology.md
├── _includes
|   ├── footer.html
|   └── header.html
├── _layouts
|   ├── default.html
|   └── post.html
├── _posts
|   ├── 2007-10-29-why-every-programmer-should-play-nethack.md
|   └── 2009-04-26-barcamp-boston-4-roundup.md
├── _sass
|   ├── _base.scss
|   └── _layout.scss
├── _site
├── .jekyll-metadata
└── index.html # can also be an 'index.md' with valid YAML Frontmatter
```
-->
https://jekyllrb-ko.github.io/docs/structure/ 에서 확인가능

하지만 이 많은 디렉토리 구조와 블로그를 이쁘게 꾸미기엔 배울것도 많고 귀찮으므로 남들이 만들어 놓은 테마를 가져다 쓰도록 합시다.
http://jekyllthemes.org/ 에서 하나 선택.
![instagram theme]({{ "/assets/posts/instagram theme.PNG" | absolute_url }})
homepage버튼을 클릭해서 github로 이동, fork버튼 클릭, 자신의 Repository에 해당 테마가 통째로 복사되었다. settings에 들어가 name을 바꿔주자.
![change theme name]({{ "/assets/posts/change theme name.PNG" | absolute_url }})
그리고 ```_config.yml```파일에서 조금만 수정해주면....
![dkskzhsek blog]({{ "/assets/posts/dkskzhsek blog.PNG" | absolute_url }})
짜잔! 테마를 적용한 page가 생성된다. 물론 page를 수정하고 지킬이 어떻게 돌아가는지 markdown을 어떻게 쓰는지도 약간은 알아야한다.

아 그리고 page가 생성되고 수정되는 것이 시간이 좀 걸릴때가 있다. 시간때에 따라 다르기 때문에 최대 5분 넘게 걸리는 경우도 있었다.
그리고 왠지 모르게 오랜 시간을 기다렸는데도 수정이 안된다면 이메일을 확인해야한다. 지킬 문법이 틀리거나 각종 오류가 발생하면 github에서 해당 내용을 메일로 보내준다(저의 경우 인코딩이 잘못되서 자꾸 utf-8인지 확인하라고... 알고보니 github 연동 에디터에서 잘못된 인코딩 사용중이였...)