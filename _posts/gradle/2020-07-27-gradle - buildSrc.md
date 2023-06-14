---
title:  "gradle - buildSrc!"


read_time: false
share: false
author_profile: false
toc: true
toc_sticky: true
# classes: wide

categories:
  - gradle
---


## buildSrc

> <https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#sec:build_sources>

디렉터리가 발견되면 Gradle은이 코드를 자동으로 컴파일하고 테스트하여 빌드 스크립트의 클래스 경로에 넣는다.  

별도의 설정 없이 루트 `build.gradle` 과 동일한 위치에 `buildSrc` 이름으로 디렉토리, `src/main/java/package-name` 구조로 생성  

`package-name` 은 테스트를 위해 `com.demo.generate` 로 지정  

커스텀한 코드를 `gradle plugin` 으로 등록하고 빌드시에 `task` 를 동작할 수 있도록 한다.  

### plugin 등록  

`buildSrc/src/main/resources/META-INF/gradle-plugins` 디렉토리에 `com.demo.generate.properties` 파일 생성  

테스크로 등록할 클래스를 지정해준다.  

```conf
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

## Annotation Processor

> <https://www.baeldung.com/java-annotation-processing-builder>
> <https://medium.com/@jason_kim/annotation-processing-101-번역-be333c7b913>

`Lombok` 이나 `JPA` 의 각종 어노테이션을 사용해 새로운 코드가 컴파일, 소스 단계에서 사용자가 정의한 대로 클래스파일이 생성되도록 하는게 `Annotation Processor` 이다.  

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

## TypeElement, Element, RoundEnvironment

## Annotation Processor 등록

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

