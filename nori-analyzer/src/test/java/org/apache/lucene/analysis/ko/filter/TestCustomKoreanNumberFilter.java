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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanNumberFilter;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.TestKoreanTokenizer;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.ko.util.TokenView;
import org.junit.Test;

public class TestCustomKoreanNumberFilter extends BaseTokenStreamTestCase {
  @Test
  public void test_basic() {
    List.of(
         "오늘 십만이천오백원의 와인 구입",    // "오늘", "102500", "원", "의", "와인", "구입"
         "어제 초밥 가격은 10만 원",         // "어제", "초밥", "가격", "은", "100000", "원" ( 초밥 사전 추가 )
         "자본금 600만 원"                  // "자본", "금", "6000000", "원"
    ).forEach(text -> {
        try(TokenStream ts = analyzer.tokenStream("dummy", text)){
          TokenView.analyzer(ts);
        }catch (IOException e){
          e.printStackTrace();
        }
    });
  }

  @Test
  public void test_variants() {
    // variant = 다른, 여러가지의
    // 숫자로 색인되는 여러가지 표현
    List.of(
        "3",      // 숫자 3
        "３",     // 특수문자 3
        "삼",     // 한글 3
        "003",    // 0을 포함하는 숫자 3
        "００３",  // 특수문자 0을 포함하는 특수문자 3
        "영영삼",  // 한글 0을 포함하는 한글 3
        "1천",    // 숫자 + 단위
        "１０백",  // 잘못된 표현이지만 지원한다. 1000으로
        "삼오칠팔구", // 단순 숫자나열
        "해경조억만천백십일", // 큰 범위의 숫자
        "천천천천"        // 1000100010001000 값으로 나열되는것이 아닌 덧셈이 수행되어 4000이 나옴
    ).forEach(text -> {
        try(TokenStream ts = analyzer.tokenStream("dummy", text)){
          TokenView.analyzer(ts);
        }catch (IOException e){
          e.printStackTrace();
        }
    });
  }

  public static UserDictionary readDict() {
    InputStream is = TestKoreanTokenizer.class.getResourceAsStream("userdict.txt");
    if (is == null) {
      throw new RuntimeException("Cannot find userdict.txt in test classpath!");
    }
    try {
      try {
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        return UserDictionary.open(reader);
      } finally {
        is.close();
      }
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }


  private Analyzer analyzer;
  @Override
  public void setUp() throws Exception {
    super.setUp();
    UserDictionary userDictionary = readDict();
    Set<POS.Tag> stopTags = new HashSet<>();
    stopTags.add(POS.Tag.SP);
    analyzer = new Analyzer() {
      @Override
      protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new KoreanTokenizer(newAttributeFactory(), userDictionary,
            KoreanTokenizer.DEFAULT_DECOMPOUND, false, false);
        TokenStream stream = new KoreanPartOfSpeechStopFilter(tokenizer, stopTags);
        return new TokenStreamComponents(tokenizer, new KoreanNumberFilter(stream));
      }
    };
  }

  @Override
  public void tearDown() throws Exception {
    analyzer.close();
    super.tearDown();
  }
}
