# elasticsearch-plugin-example



### _**프로젝트 목적**_

---

>  `Elasticsearch plugin`을 개발하기 위한 샘플 소스  
>  2021.05.20 Nori-analyzer 관련 plugin 추가   
>  [_**Nori analyzer git url**_](https://github.com/korlucene/argo-nori-analyzer)



### _**프로젝트 설정 **_

---

> _**Elasticsearch version - 7.9.2**_
>
> [_**참고 URL**_](https://nocode2k.blogspot.com/2019/03/elasticsearch-plugin.html)
>
> _**Build - Maven**_
>
> _**IDE - Intellij**_



### _**주요 코드**_

---

#### RestController

```java
// TestPlugin.java
@Override
public List<RestHandler> getRestHandlers(final Settings settings,
                                         final RestController restController,
                                         final ClusterSettings clusterSettings,
                                         final IndexScopedSettings indexScopedSettings,
                                         final SettingsFilter settingsFilter,
                                         final IndexNameExpressionResolver indexNameExpressionResolver,
                                         final Supplier<DiscoveryNodes> nodesInCluster) {

  return singletonList(new TestAction());
}
```

* Controller처럼 es의 url로 접근하여 처리하는 방식
* new TestAction()에 정의된 url로 특정 행위가 가능

```java
// TestAction.java
@Override
public List<Route> routes() {
  return unmodifiableList(asList(
    new Route(POST, "/_test")
    , new Route(GET, "/_test")
  )
	);
}
```

* 접근방식은 `POST`와 `GET`을 허용하며, http://localhost:9200/_test로 접속이 가능

```java
// TestAction.java
@Override
public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) {
  System.out.println("Call prepareRequest method");
  return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, "application/json","{ \"returnStatus\" : 200, \"returnCode\" : \"SUCCESS\", \"returnMessage\" : \"\" }"));
}
```

* 접근 후 처리 방식 (json 형태의 응답 200을 주겠다)



#### Index Field

```java
// TestPlugin.java
@Override
public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
  Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> filters = new HashMap<>();
  filters.put("test_filter", TestTokenFilterFactory::new);
  return filters;
}
```

* test_filter라는 filter를 추가한다.
* test_filter의 기능은 TestTokenFilterFactory에 정의한다.

```java
// TestTokenFilterFactory.java
@Override
public TokenStream create(TokenStream tokenStream) {
  System.out.println("token stream 내뱉는 create 함수");
  return new TestFilter(tokenStream);
}
```

* TokenStream(입력한 검색어 토큰)을 파라미터로 실제 필터 처리를 하는 TestFilter를 정의한다.

```java
// TestFilter.java
@Override
public boolean incrementToken() throws IOException {
  System.out.println("call incrementToken method");
  if(input.incrementToken()){
    System.out.println("term :: "+termAtt.toString());
    setAttributesFromQueue(termAtt.toString());
    return true;
  }
  return false;
}
```

* 실질적인 토큰 처리 메소드이다.
* 현재로서는 하나의 토큰을 받으면 한번의 처리만 하게끔 되어 있지만, 전달받은 토큰을 1:N으로 분리한다면 
  input.incrementToken()을 while문으로 처리하고 해당 토큰이 마지막일 경우에만 return false한다. (true면 무한루프) 

```java
// TestFilter.java
private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

// private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
// private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

private void setAttributesFromQueue(String term) {
  termAtt.setEmpty().append("sample_"+term);
  typeAtt.setType("WORD");
}
```

* 변형된 term을 termAtt에 저장한다. (기존 term은 삭제하고 변형된 term을 저장)
* 변형된 term이 어떤 type인지 명시한다.
* term의 position정보나 , offsSet정보를 추가로 저장할 수 있지만 예제이므로 제외한다.



### _**ES 적용 및 사용 방법**_

---

#### Maven Build

> <u>_**mvn clean install**_</u>
>
> zip파일로 만들기 위해 프로젝트에서 메이븐 빌드를 실행한다. 테스트 케이스 까지 정상적으로 실행되면 `BUILD SUCCESS`

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running org.elasticsearch.index.analysis.filter.TestFilterTest
[2021-03-15T20:30:55,201][INFO ][o.e.i.a.f.TestFilterTest ] [test_filter] before test
Input keyword :: TeST caSE
token stream 내뱉는 create 함수
call incrementToken method
sample_test [WORD]
call incrementToken method
sample_case [WORD]
call incrementToken method
[2021-03-15T20:30:55,618][INFO ][o.e.i.a.f.TestFilterTest ] [test_filter] after test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.647 sec

Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ elasticsearch-plugin ---
[INFO] Building jar: /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-plugin-7.9.2.jar
[INFO] 
[INFO] --- maven-assembly-plugin:2.4:single (tarball) @ elasticsearch-plugin ---
[INFO] Reading assembly descriptor: src/main/assemblies/plugin.xml
[WARNING] The following patterns were never triggered in this artifact inclusion filter:
o  'elasticsearch-plugin-7.9.2.jar'

[WARNING] The following patterns were never triggered in this artifact inclusion filter:
o  'com.google.guava:guava'

[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Building zip: /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-plugin-7.9.2.zip
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ elasticsearch-plugin ---
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-plugin-7.9.2.jar to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-plugin/7.9.2/elasticsearch-plugin-7.9.2.jar
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/pom.xml to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-plugin/7.9.2/elasticsearch-plugin-7.9.2.pom
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-plugin-7.9.2.zip to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-plugin/7.9.2/elasticsearch-plugin-7.9.2.zip
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

```

#### 설치된 플러그인 제거

>  <u>_**elasticsearch-7.9.2/bin/elasticsearch-plugin remove [plugin name]**_</u>
>
> plugin-descriptor.properties [name]에서 정의한 플러그인 이름을 이미 설치되어 있으면 제거한다.

```
> removing [test-plugin]...
```

#### 플러그인 설치

>  <u>_**elasticsearch-7.9.2/bin/elasticsearch-plugin install [zip file 경로]**_</u>
> 배포된 zip파일의 위치를 지정한다. (해당 프로젝트는 target이다.)

```
/elasticsearch-7.9.2/elasticsearch-plugin install file:///{zip파일 위치}/elasticsearch-plugin-7.9.2.zip
-> Installing file:///{zip파일 위치}/elasticsearch-plugin-7.9.2.zip
-> Downloading file:///{zip파일 위치}/elasticsearch-plugin-7.9.2.zip
[=================================================] 100%
```

> 플러그인의 대상 클래스, 플러그인 이름등을 작성해야 정상적으로 설치가 된다.  
> nori-plugin/resources/plugin-descriptor.properties에 작성하면 된다.  
```properties
version=${project.version}
description=${project.description}
name=nori-plugin
classname=org.elasticsearch.plugin.analysis.nori.NoriDefaultPlugin
java.version=11
elasticsearch.version=${elasticsearch.version}
```


#### 실행확인 (PostMan)

> PUT test_case
```json
{
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "analysis": {
        "analyzer": {
          "test_keyword_analyzer": {
            "filter": [
              "test_filter",
              "nori_stoptag_filter"
            ],
            "type": "custom",
            "tokenizer": "nori_default_tokenizer"
          }
        }
      }
    },
    "mappings": {
      "properties": {
        "test": {
          "type": "text",
          "analyzer": "test_keyword_analyzer"
        }
      }
    }
  }
```
  


> localhost:9200/test_case/_analyze
nori plugin을 추가하였으므로 샘플도 변경한다.(2021.05.20)

```json
//input
{
    "tokenizer": "nori_default_tokenizer",
    "filter": [
        "nori_stoptag_filter"
    ],
    "text": "  와디다락 맛있고, 대한민국은 나라입니다.  "
}
```

```json
//output
{
    "tokens": [
        {
            "token": "맛있",
            "start_offset": 7,
            "end_offset": 9,
            "type": "word",
            "position": 1
        },
        {
            "token": "대한민국",
            "start_offset": 12,
            "end_offset": 16,
            "type": "word",
            "position": 3
        },
        {
            "token": "입니다",
            "start_offset": 20,
            "end_offset": 23,
            "type": "word",
            "position": 6
        }
    ]
}
```



#### Test code

> JUnit 테스트 코드를 작성하지 않으면, 매번 실제 es 바이너리에 플러그인을 배포, 제거 , 설치를 반복해야만 한다. 
> 번거로운 작업을 하지 않기위해, 테스트 케이스를 작성하며, lib에서 제공하는 `ESTestCase`를 상속받아서 가상의 인덱스를 
> 생성하여 테스트를 진행할 수 있도록 한다.

```java
public class TestFilterTest extends ESTestCase
```

> nori-analyzer가 추가된 module의 class를 사용하기 위해, nori-plugin의 pom.xml에 다음을 추가한다.
```xml
<!-- nori-analyzer module set -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>nori-analyzer</artifactId>
    <version>8.4.0</version>
</dependency>
```


* ESTestCase를 extends하여 가상의 테스트용 인덱스를 생성 할 수있다.

```java
public class TestFilterTest extends ESTestCase {

    @Test
    public void test_filter() throws IOException {
        // 입력 키워드
        String keyword = "TeST caSE";
        System.out.println("Input keyword :: " + keyword);

        // 테스트 가상의 색인 생성
        final TestAnalysis testAnalysis = createTestAnalysis(new Index("test", "_na_"), Settings.EMPTY, new TestPlugin());

        // 토크나이저 팩토리 설정 ( 커스텀 된것이 없으므로 기본 standard)
        TokenizerFactory tokenizerFactory = testAnalysis.tokenizer.get("standard");
        // 토큰필터 설정 ( 커스텀된 test_filter와 기본 filter인 lowercase 설정 )
        TokenFilterFactory tokenFilterFactory = testAnalysis.tokenFilter.get("test_filter");
        TokenFilterFactory lowerCaseFilterFactory = testAnalysis.tokenFilter.get("lowercase");
        // 토크나이저 생성
        Tokenizer tokenizer = tokenizerFactory.create();

        // 토크나이저 키워드 추가
        tokenizer.setReader(new StringReader(keyword));

        // 토큰스트림에 토크나이저 생성 및 다음 필터엔 만들어진 토큰 스트림 삽입 ( 주의 : 파라미터가 다름 )
        TokenStream tokenStream;
        tokenStream = tokenFilterFactory.create(tokenizer);
        tokenStream = lowerCaseFilterFactory.create(tokenStream);

        // 만들어진 토큰스트림의 키워드 출력
        CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttr = tokenStream.addAttribute(TypeAttribute.class);

        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                System.out.println(termAtt.toString() + " [" + typeAttr.type() + "]");
            }
            tokenStream.end();
        } finally {
            tokenStream.close();
        }
    }
}

```

#### Test시에 주요 에러 (구글링해도 안나옴..)

> **<span style="color:red">Caused by: java.lang.IllegalStateException: jar hell!</span>**
> <span style="color:red">jar1: /Users/junmyung/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar</span>
> <span style="color:red">jar2: /Users/junmyung/.m2/repository/org/hamcrest/hamcrest/2.1/hamcrest-2.1.jar</span>
>
> ESTestCase를 import를 하면, 분명히 pom.xml에 junit - exclusions를 2개 (hamcrest, hamcrest-core) 추가하였는데도 불구하고, hamcrest jar
> 지옥 관련 예외가 발생한다. 
> 해당 예외가 발생하지 않기 위해서는 jarHell을 체크하는 class(JarHell)를 빈값으로 만들어 다시 정의해야한다.
> 그리고 반드시 `rebuild`하여 JarHell의 클래스 파일이 빈값으로 되어있는거으로 덮어씌워야 한다.

```java
// 내용이 없는 빈 JarHell Class
public class JarHell {
    private JarHell() {}
    public static void checkJarHell(Consumer<String> output) throws IOException, URISyntaxException {}
    public static Set<URL> parseClassPath() { return Collections.emptySet(); }
    public static void checkJarHell(Set<URL> urls, Consumer<String> output) throws URISyntaxException, IOException {}
    public static void checkVersionFormat(String targetVersion) {}
    public static void checkJavaVersion(String resource, String targetVersion) {}
}
```

> **<span style="color:red">java.lang.AssertionError: TokenStream implementation classes or at least their incrementToken() implementation must be final</span>**
> (TokenStream 구현 클래스 또는 해당 클래스 이상의 incrementToken() 구현은 final이여야 한다.)
>
> 실제 테스트시에는 문제도 없고 작동도 잘하는데 TestCase에서는 해당 문제가 발생한다. 해당 이유는 TokenFilter를 상속받는 커스텀 클래스(TestFilter)에서 final로 지정이 되어 있지 않기 때문이다. (TokenFilter의 Root 상속클래스인 AttributeSource에서 `static final ClassValue<Class<? extends Attribute>[]> implInterfaces` 이부분 때문인것 같은데 추측..)
> 반드시 TokenFilter를 상속받는 클래스는 final로 선언해 주어야 한다.

```java
public final class TestFilter extends TokenFilter
```

> 테스트 케이스도 통과해서 ES에 plugin을 설치하면 JarHell 예외가 발생할수 있다. 해당 원인은  
> ES내부에서 이미 사용하고 있는 경우가 대부분일 것이며 해당부분은 lucene-analyzers-common이 nori-analyzer가 추가됨에 따라  
> jar-hell이 발생하여 해당 부분을 assemblies/plugin.xml에 exlcude한다.

```xml
<dependencySet>
    <useProjectArtifact>true</useProjectArtifact>
    <useTransitiveFiltering>true</useTransitiveFiltering>
    <excludes>
        <exclude>org.elasticsearch:elasticsearch</exclude>
        <exclude>org.apache.lucene:lucene-core</exclude>
        <exclude>org.apache.logging.log4j:log4j-core</exclude>
        <!-- nori-analyzer 추가로 인하여 JarHell 방지 -->
        <exclude>org.apache.lucene:lucene-analyzers-common</exclude>
    </excludes>
</dependencySet>
```