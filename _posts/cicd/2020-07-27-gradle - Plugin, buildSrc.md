---
title:  "gradle - Plugin, buildSrc!"


read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - cicd
  - gradle
---

## Plugin

지금까지 `gradle` 을 보면 단순 쉘스크립트인데 언어가 `groovy` 인 것 같은 느낌이다.  
`Plugin` 을 사용하면 유용한 기능들을 가져올 수 있다.  

**plugin 은 유용한 기능을 가진 task 의 집합**이라 할 수 있다.  
`gradle` 에선 `JavaPlugin` 과 같은 `core plugins` 들을 제공하며  
`Plugin` 인터페이스를 구현해서 작성하거나 `gradle DSL(groovy, kotlin)` 을 사용해 작성할 수 있다.  

Plugin 을 직접 구현한다면 2가지 방식으로 구현할 수 있다.  

**Script Plugin**  
build 스크립트에서 선언방식으로 Plugin 을 구성하고 빌드에 관여하는 방식.  

**Binary Plugin**  
jar 형태로 배포되고 build 스크립트에 사용되는 형식

일반적으로 초기에 `script plugin` 으로 구성되어 개발되다가 조직간 공유할 수 있는 `binary plugin` 으로 마이그레이션 된다.  


### Core Plugin, Community Plugin

`Core Plugin` 는 배포된 `gradle` 에 자체적으로 저장되어 있는 `plugin` 으로 별도의 `version number` 없이 사용한다, `org.gradle` 네임스페이스를 사용하며 생략 가능하다.  

```groovy
plugins {
    id 'java'
    // id "org.gradle.java"
}
```

아래 url 에 접속하면 gradle 에서 제공하는 `Core Plugin` 리스트를 확인할 수 있다.  

> <https://docs.gradle.org/current/userguide/plugin_reference.html>

반대로 `Community Plugin` 는 사용자 정의 플러그인으로 `gradle portal` 에 저장된 `Plugin` 을 다운받아 사용한다.  
`Spring Boot`, `Spring Dependency` 와 같은 플러그인도 `Community Plugin` 이라 할 수 있다.  

```groovy
plugins {
    id 'org.springframework.boot' version '2.7.9'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}
```

정규화된 `id`, `version` 을 명시해야한다.  

아래 url 에 접속하면 `gradle portal` 에 등록한 사용자 정의 `Community Plugin` 리스트를 확인할 수 있다.  

> <https://plugins.gradle.org/>

### Plugin 적용

`Plugin` 을 `encapsulated` 하여 빌드구성에 적용시키고 싶으면 아래 2가지 과정을 거쳐야한다.  

**1. resolve the plugin**  
올바른 버전의 `plugin` 을 찾아 `script classpath` 에 적용시키는 것.  
url, 특정 경로를 통해 `plugin` 을 가져와 `resolve` 하여 빌드에 참여시킬 수 있다.  

**2. apply the plugin to the target**
`Plugin.apply(T)` 메서드를 사용해 `target` 에 `plugin` 을 적용시키는 것.  
대부분 `target` 은 `Project` 이다.  

아래와 같이 `plugins` 블록을 사용해 `Project` 객체에 `Plugin` 을 적용할 수 있다.  

```groovy
plugins {
    id «plugin id» // core
    id «plugin id» version «plugin version» [apply «false»] // community
}
```

`Project` 가 `plugins` 블록을 만나면 위에서 설명한 2가지 과정을 거치는데  
`apply false` 사용하면 `Plugin` 의 `apply` 과정을 막을 수 있다.  

선언만 해두고 `Plugin` 을 사용할 `subproject` 안에서만 사용하도록 설정가능하고,  
한번 `resolve` 된 `Plugin` 은 `subproject` 에서 `version` 명시 없이 사용 가능하다.  

```groovy
// setting.gradle
rootProject.name = 'basic'

include "project-a"
```

```groovy
// build.bradle [basic]
plugins {
    id 'org.springframework.boot' version '2.7.9' apply false
}
```

```groovy
// build.gradle [project-a]
plugins {
    id 'org.springframework.boot'
}
```

만약 `subprojects` 와 같은 블럭 안에 `Plugin` 설정을 해야 한다면 `apply plugin` 함수를 사용할 수 있다.  

```groovy
plugins {
    id 'org.springframework.boot' version '2.7.9' apply false
}

subprojects {
    if (name.startsWith('app')) {
        apply plugin: 'org.springframework.boot'
    }
}
```


#### pluginManagement 블록  

아래와 같이 `setting.gradle` 파일에 `pluginManagement` 블록을 정의하여 `Plugin` 의 관리를 할 수 있다.  

```groovy
// setting.gradle
pluginManagement {
    plugins {
        id 'org.springframework.boot' version "${springBootVersion}"
    }
}

rootProject.name = 'basic'

include "project-a"
```

```groovy
// gradle.properties
springBootVersion=2.7.9
```

```groovy
// build.gradle [basic]
plugins {
    id 'org.springframework.boot'
}
```

### Plugin Repository

기본적으로 `plugins` 블록에 지정된 `Plugin` 들을 찾기 위해 `gradle plugin potal` 를 사용한다.  
하지만 보안, 빌드구조상 **커스텀 plugin repository** 를 사용해야 할 경우 아래와 같이 `pluginManagement` 에서 `repositories` 블록을 사용한다.  


```groovy
pluginManagement {
    repositories {
        
        gradlePluginPortal() // 1순위
        google() // 2순위
        mavenCentral() // 3순위
        // 4순위
        maven { url 'https://xxx:8081/maven-repo' }
        ivy { url './ivy-repo' }
    }
}
```


## buildSrc

> <https://docs.gradle.org/current/userguide/plugins.html#sec:buildsrc_plugins_dsl>

`buildSrc` 를 사용하면 커스텀한 코드를 `gradle plugin` 으로 등록하고 빌드시에 `task` 를 동작할 수 있도록 한다.  

디렉터리가 발견되면 `gradle` 에선 코드를 자동으로 컴파일하고 테스트하여 빌드 스크립트의 클래스 경로에 넣는다.  
별도의 설정 없이 루트 `build.gradle` 과 동일한 위치에 `buildSrc` 이름으로 디렉토리, `src/main/java/{package-name}` 구조로 생성  

`package-name` 은 테스트를 위해 `com.demo.generate` 로 지정  


### plugin 등록  

`buildSrc/src/main/resources/META-INF/gradle-plugins` 디렉토리에 `com.demo.generate.properties` 파일 생성  

테스크로 등록할 클래스를 지정해준다.  

```conf
# com.demo.generate.properties 파일에 아래 설정
implementation-class=com.demo.generate.plugin.GeneratorPlugin
```

```java
public class GeneratorPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create(
                "generateDialect", 
                GeneratorTask.class);
        // generateDialect 를 테스크명으로 MavlinkGeneratorTask 코드 등록

        project.getTasks()
                .getByName("compileJava")
                .dependsOn("generateDialect");
        // compileJava 에 실행시 같이 수행
    }
}
```

```java
public class GeneratorTask extends DefaultTask {

    @InputDirectory
    private File definitions; // 

    @OutputDirectory
    private File generatedSources;
    // getter, setter...

    // task code 
    @TaskAction
    public void generate() throws FileNotFoundException, XMLStreamException {
        // no need to proceed if definitions is null
        if (definitions == null) return;

        if (!definitions.isDirectory()) throw new IllegalArgumentException(
                "'definitions' should be a directory, but got a file instead.");

        if (generatedSources == null) {
            throw new IllegalStateException("'generatedSources' is not specified.");
        }

        if (generatedSources.exists() && !deleteAll(generatedSources)) { // 생성 전에 삭제
            throw new IllegalStateException("unable to clean generated sources.");
        }

        if (!generatedSources.mkdirs()) {
            throw new IllegalStateException("unable to create 'generatedSources' directory at " + generatedSources.getAbsolutePath());
        }

        // TODO ...
    }

    private boolean deleteAll(File f) {
        if (f.isDirectory()) {
            //noinspection ConstantConditions
            Arrays.stream(f.listFiles()).forEach(this::deleteAll);
        }
        return f.delete();
    }
}
```

이제 사용하고 싶은 application 의 `build.gradle` 에 가서 `plugin` 등록 및 `task` 를 호출하면 된다.  

```groovy
plugins {
    ...
    id 'com.demo.generate'
}

def genSrc = "src/main/java-gen"
    generateDialect {
        definitions file('definition-xml')
        generatedSources file(genSrc)
    }
    sourceSets.main.java.srcDirs += genSrc
```

<!-- 본인의 경우 drone mavlink 를 사용할 일이 있어 커스텀한 xml 로부터 추가로 java 클래스파일을 생성해야 했는데 https://github.com/dronefleet/mavlink/tree/master/buildSrc 에 정의되어 있는 코드를 가져와 사용하엿다.  -->
<!-- 
## Annotation Processor

> <https://www.baeldung.com/java-annotation-processing-builder>
> <https://medium.com/@jason_kim/annotation-processing-101-번역-be333c7b913>

`Lombok`, `QueryDSL`, `MapStruct` 같이 어노테이션을 사용해 새로운 코드가 컴파일, 소스 단계에서 사용자가 정의한 대로 클래스파일이 생성되도록 하는게 `Annotation Processor` 이다.  

```java
public class MavlinkBuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotationTypes = new HashSet<>();
        supportedAnnotationTypes.add("com.example.demo.annotation.*");
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
```

`process` 메서드에서 어노테이션 설정과 클래스 구조를 확인해 새로운 `java` 파일을 생성할 수 있다.  

`jdk 8` 이상에선 `@Supported...` 어노테이션을 사용해 설정할 수 있다고 한다.  
> 안드로이드에서 대부분 해당 어노테이션으로 처리  

```java
@SupportedAnnotationTypes("com.example.demo.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MavlinkBuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        return false;
    }
}
```

생성한 `Annotation Processor` 를 컴파일시에 처리되도록 등록과정이 필요하다.  

크게 3가지 경우로 사용가능

### 구글 auto-service 라이브러리 사용  

```java
@AutoService(Processor.class)
public BuilderProcessor extends AbstractProcessor {
    // …
}
```

auto-service 라이브러리가 `javax.notation.processing.Processor` 파일을 자동으로 생성해서 `@AutoService` 처리된 `Annotation Processor` 를 지정해준다.  

대부분 해당 방식을 사용  

### 컴파일러 옵션에 지정

`processor` 컴파일러 키는 컴파일러의 소스 처리 단계를 사용자 자신의 주석 프로세서로 증가시키기 위한 표준 JDK 기능이다.

프로세서 자체와 주석은 별도의 컴파일에 이미 클래스로 컴파일되어 클래스 경로에 표시되어야 하므로, 먼저 해야 할 일은 다음과 같다.

그런 다음 방금 컴파일한 주석 프로세서 클래스를 지정하는 `-processor` 키로 실제 소스 컴파일을 수행하십시오.

여러 주석 프로세서를 한 번에 지정하려면 다음과 같이 클래스 이름을 쉼표로 구분하십시오.

```
javac com/baeldung/annotation/processor/BuilderProcessor
javac com/baeldung/annotation/processor/BuilderProperty

javac -processor com.baeldung.annotation.processor.MyProcessor Person.java
javac -processor package1.Processor1,package2.Processor2 SourceFile.java
```

### 메이븐, 그래들 플러그인 사용  

`maven-compiler-plugin` 은 구성의 일부로 주석 프로세서를 지정할 수 있다.

다음은 컴파일러 플러그인에 대한 주석 프로세서를 추가하는 예입니다. 생성된 SourceDirectory 구성 매개 변수를 사용하여 생성된 소스를 저장할 디렉터리를 지정할 수도 있다.

예를 들어 빌드 종속성의 다른 병에서 가져온 `BuilderProcessor` 클래스는 이미 컴파일되어야 한다는 점에 유의하십시오.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.5.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <encoding>UTF-8</encoding>
                <generatedSourcesDirectory>${project.build.directory}
                  /generated-sources/</generatedSourcesDirectory>
                <annotationProcessors>
                    <annotationProcessor>
                        com.baeldung.annotation.processor.BuilderProcessor
                    </annotationProcessor>
                </annotationProcessors>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Classpath 에 Annotation Processor 지정

main 디렉토리 밑에 `META-INF/services/javax.annotation.processing.Processor` 파일 생성  
직접 작성한 `Annotation Processor` 를 패키지 경로까지 모두 표시하여 작성

```
package1.Processor1
package2.Processor2
package3.Processor3
```
 -->