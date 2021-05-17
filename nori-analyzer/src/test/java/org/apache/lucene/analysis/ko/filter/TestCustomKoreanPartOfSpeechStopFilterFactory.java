/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.analysis.ko.filter;


import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilterFactory;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.apache.lucene.analysis.ko.util.TokenView;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.apache.lucene.util.Version;
import org.junit.Before;

/**
 * Simple tests for {@link KoreanPartOfSpeechStopFilterFactory}
 */
public class TestCustomKoreanPartOfSpeechStopFilterFactory extends BaseTokenStreamTestCase {

  String dicPath = "";

  @Before
  public void setDicPath(){
    dicPath = "/Users/junmyung/IdeaProjects/elasticsearch-plugin/nori-analyzer/src/test/resources/org/apache/lucene/analysis/ko";
  }

  public void test_part_speech_stop_filter() throws IOException {
    KoreanTokenizerFactory tokenizerFactory = new KoreanTokenizerFactory(new HashMap<>(){{
      put("discardPunctuation", "true");   // 출력에서 구두점 토큰을 삭제해야하는 경우
      put("outputUnknownUnigrams", "false");
      put("userDictionary", "userdict.txt");
      put("decompoundMode", KoreanTokenizer.DecompoundMode.NONE.toString());
    }});

    tokenizerFactory.inform(new FilesystemResourceLoader(Paths.get(dicPath)));
    Tokenizer tokenizer = tokenizerFactory.create();
    // 입니다 는 Left 품사는 VCP(긍정 지정사 >> ~~이다) Right 품사는 EP(종결 어미 >> 춥다, 없다, 먹자 등 )
    // TODO 그럼 입니다는 왜 종결어미가 Right품사로 되었을까?Right 품사는 뭘까..
    tokenizer.setReader(new StringReader(" 한국은 대단한 나라입니다."));
    KoreanPartOfSpeechStopFilterFactory koreanPartOfSpeechStopFilterFactory = new KoreanPartOfSpeechStopFilterFactory(new HashMap<>(){{
      put("luceneMatchVersion", Version.LATEST.toString());
      put("tags", "JX,ETM,EF");
    }});

    TokenStream tokenStream = koreanPartOfSpeechStopFilterFactory.create(tokenizer);
    TokenView.analyzer(tokenStream);
  }
}
