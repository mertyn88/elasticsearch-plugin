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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanNumberFilterFactory;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilterFactory;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.apache.lucene.analysis.ko.util.TokenView;
import org.apache.lucene.analysis.util.FilesystemResourceLoader;
import org.junit.Before;

/**
 * Simple tests for {@link KoreanNumberFilterFactory}
 */
public class TestCustomKoreanNumberFilterFactory extends BaseTokenStreamTestCase {

  String dicPath = "";

  @Before
  public void setDicPath(){
    dicPath = "/Users/junmyung/IdeaProjects/elasticsearch-plugin/nori-analyzer/src/test/resources/org/apache/lucene/analysis/ko";
  }

  /**
   * charFilter 적용하지 않음
   * tokenizer 품사적용(Space 제외)
   * filter 숫자 관련 적용
   * @throws IOException
   */
  public void test_part_of_speech_stop_filter_and_number_filter() throws  IOException {
    KoreanTokenizerFactory tokenizerFactory = new KoreanTokenizerFactory(new HashMap<>(){{
      put("discardPunctuation", "false");
      put("outputUnknownUnigrams", "false");
      put("userDictionary", "userdict.txt");
      put("decompoundMode", KoreanTokenizer.DecompoundMode.NONE.toString());
    }});

    tokenizerFactory.inform(new FilesystemResourceLoader(Paths.get(dicPath)));
    Tokenizer tokenizer = tokenizerFactory.create(newAttributeFactory());
    tokenizer.setReader(new StringReader("어제 초밥 가격은 10만 원"));
    KoreanPartOfSpeechStopFilterFactory koreanPartOfSpeechStopFilterFactory = new KoreanPartOfSpeechStopFilterFactory(new HashMap<>(){{
      put("tags","SP");
    }});
    TokenStream tokenStream = koreanPartOfSpeechStopFilterFactory.create(tokenizer);
    tokenStream = new Analyzer.TokenStreamComponents(tokenizer,
                    new KoreanNumberFilterFactory(new HashMap<>()).create(tokenStream)
                  ).getTokenStream();
    TokenView.analyzer(tokenStream);
  }
}
