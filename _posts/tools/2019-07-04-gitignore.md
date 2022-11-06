---
title:  "gitignore!"

read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - tools
---


### .gitignore

개인이 아닌 다수가 같이 개발하기 위해 깃 허브 연동을 진행한다.  

깃허브에 올리지 말아야할 정보들을 저장하는 파일로 컴퓨터 종속적인 정보들이 저장된 파일, 폴더의 경우 다른사람과 공유되면 오히려 오류를 발생시킨다.  
ex) 로컬 리파지토리 위치를 지정하는 파일의 경우 사용자마다 컴퓨터명이 다름으로 오류발생, 각종 이클립스 설정(.classpath, .project파일)의 설정 불일치로 오류발생

프로젝트를 깃 허브에 올려 같이 사용하는 방법을 소개한다.  

대다수의 ide (vscode, intellij, eclips) 가 프로젝트 생성시 알아서 `.gitignore` 파일을 만들어 주지만  
특수한 상황에서 직접 `.gitignore 파일을` 만들어야 할 때 

이런 정보들을 제외시키는 설정을 자동으로 만들어 주는 사이트가 있다!  

> https://www.gitignore.io/

위 사이트에서 다음과 같이 검색

![image26](/assets/Spring/image26.png){: .shadow}  

그럼 다음과 같은 설정파일이 자동으로 생성된다.  

```conf
# Created by https://www.gitignore.io/api/git,java,maven,windows,eclipse
# Edit at https://www.gitignore.io/?templates=git,java,maven,windows,eclipse

### Eclipse ###
.metadata
bin/
tmp/
*.tmp
*.bak
*.swp
*~.nib
local.properties
.settings/
.loadpath
.recommenders

# External tool builders
.externalToolBuilders/

# Locally stored "Eclipse launch configurations"
*.launch

# PyDev specific (Python IDE for Eclipse)
*.pydevproject

# CDT-specific (C/C++ Development Tooling)
.cproject

# CDT- autotools
.autotools

# Java annotation processor (APT)
.factorypath

# PDT-specific (PHP Development Tools)
.buildpath

# sbteclipse plugin
.target

# Tern plugin
.tern-project

# TeXlipse plugin
.texlipse

# STS (Spring Tool Suite)
.springBeans

# Code Recommenders
.recommenders/

# Annotation Processing
.apt_generated/

# Scala IDE specific (Scala & Java development for Eclipse)
.cache-main
.scala_dependencies
.worksheet

### Eclipse Patch ###
# Eclipse Core
.project

# JDT-specific (Eclipse Java Development Tools)
.classpath

# Annotation Processing
.apt_generated

.sts4-cache/

### Git ###
# Created by git for backups. To disable backups in Git:
# $ git config --global mergetool.keepBackup false
*.orig

# Created by git when using merge tools for conflicts
*.BACKUP.*
*.BASE.*
*.LOCAL.*
*.REMOTE.*
*_BACKUP_*.txt
*_BASE_*.txt
*_LOCAL_*.txt
*_REMOTE_*.txt

### Java ###
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml
hs_err_pid*

### Maven ###
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

### Windows ###
# Windows thumbnail cache files
Thumbs.db
Thumbs.db:encryptable
ehthumbs.db
ehthumbs_vista.db

# Dump file
*.stackdump

# Folder config file
[Dd]esktop.ini

# Recycle Bin used on file shares
$RECYCLE.BIN/

# Windows Installer files
*.cab
*.msi
*.msix
*.msm
*.msp

# Windows shortcuts
*.lnk

# End of https://www.gitignore.io/api/git,java,maven,windows,eclipse
```

이파일 내용을 .gitignore 파일로 다음과 같이 저장  
![image27](/assets/Spring/image27.png){: .shadow}  

>.gitignore 파일 생성 시 .gitignore 파일명을 입력하면  
파일 이름을 입력해야 합니다 라는 경고창과 함께 파일명 변경이 안된다.  
이 때 .gitignore. 이렇게 뒤에 .을 붙이면 정상적으로 파일이 만들어진다.  

이제 이 프로젝트를 깃 리파지토리에 올리면 개인설정은 제외된 소스코드만 올라가게 된다.  

### 깃 리파지토리에서 다운

문제는 깃허브에 올라간 리파지토리를 다운받을 때 발생한다.   

프로젝트 설정을 다 빼버려서 이클립스가 이를 프로젝트로 인식하지 않는다...  
![image28](/assets/Spring/image28.png){: .shadow}  

우선 이전단계로 돌아가 3번째 라디오버튼을 클릭해 `general project`로 생성하자.  
![image29](/assets/Spring/image29.png){: .shadow}  

만들어진 프로젝트를 보면 평소 우리가 알던 Spring maven프로젝트 형식이 아니다.   
(소스코드 덩어리를 당연히 프로젝트로 인식할 일이 없음...)  
![image30](/assets/Spring/image30.png){: .shadow}  


우클릭 `Configure->Convert to Maven Project` 클릭  

![image31](/assets/Spring/image31.png){: .shadow}  

maven 형식으로 변경되었으면 설정 끝....