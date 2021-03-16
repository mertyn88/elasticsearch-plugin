# elasticsearch-plugin-example



### _**프로젝트 목적**_

---

>  `Elasticsearch plugin`을 개발하기 위한 샘플 소스



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

* 실직적인 토큰 처리 메소드이다.
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
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ elasticsearch-example-plugin ---
[INFO] Building jar: /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-example-plugin-7.9.2.jar
[INFO] 
[INFO] --- maven-assembly-plugin:2.4:single (tarball) @ elasticsearch-example-plugin ---
[INFO] Reading assembly descriptor: src/main/assemblies/plugin.xml
[WARNING] The following patterns were never triggered in this artifact inclusion filter:
o  'elasticsearch-example-plugin-7.9.2.jar'

[WARNING] The following patterns were never triggered in this artifact inclusion filter:
o  'com.google.guava:guava'

[WARNING] File encoding has not been set, using platform encoding UTF-8, i.e. build is platform dependent!
[INFO] Building zip: /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-example-plugin-7.9.2.zip
[INFO] 
[INFO] --- maven-install-plugin:2.4:install (default-install) @ elasticsearch-example-plugin ---
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-example-plugin-7.9.2.jar to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-example-plugin/7.9.2/elasticsearch-example-plugin-7.9.2.jar
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/pom.xml to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-example-plugin/7.9.2/elasticsearch-example-plugin-7.9.2.pom
[INFO] Installing /Users/junmyung/IdeaProjects/elasticsearch-plugin-example/target/elasticsearch-example-plugin-7.9.2.zip to /Users/junmyung/.m2/repository/org/elasticsearch/elasticsearch-example-plugin/7.9.2/elasticsearch-example-plugin-7.9.2.zip
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
/elasticsearch-7.9.2/elasticsearch-plugin install file:///{zip파일 위치}/elasticsearch-example-plugin-7.9.2.zip
-> Installing file:///{zip파일 위치}/elasticsearch-example-plugin-7.9.2.zip
-> Downloading file:///{zip파일 위치}/elasticsearch-example-plugin-7.9.2.zip
[=================================================] 100%
```

#### 실행확인 (PostMan)

> localhost:9200/test_case/_analyze

```json
//input
{
  "tokenizer": "standard",
  "filter": [
    "test_filter"
  ],
  "text": "단어1 단어2"
}
```

```json
//output
{
    "tokens": [
        {
            "token": "sample_단어1",
            "start_offset": 0,
            "end_offset": 3,
            "type": "WORD",
            "position": 0
        },
        {
            "token": "sample_단어2",
            "start_offset": 4,
            "end_offset": 7,
            "type": "WORD",
            "position": 1
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

