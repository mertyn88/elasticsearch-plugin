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
package org.apache.lucene.analysis.ko.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.TestKoreanTokenizer;
import org.apache.lucene.analysis.ko.util.TokenView;

/**
 * Test Korean morphological analyzer
 */
public class TestCustomKoreanAnalyzer extends BaseTokenStreamTestCase {

  public void test_sentence() throws  IOException {
    try(TokenStream ts = new KoreanAnalyzer()
        .tokenStream("dummy", "검색엔진은 대단한 나라입니다.")) {
      TokenView.analyzer(ts);
    }
  }

  public void test_stop_tag() throws  IOException {
    /*
      NNG | General Noun | 일반 명사 | 나라(NNG)
      NNP | Proper Noun  | 고유 명사 | 한국(NNP)
     */
    Set<POS.Tag> stopTags = new HashSet<>(Arrays.asList(POS.Tag.NNG, POS.Tag.NNP, POS.Tag.JX));
    try(TokenStream ts = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.NONE, stopTags, false)
        .tokenStream("dummy", "한국은 대단한 나라입니다.")){
      TokenView.analyzer(ts);
    }
  }

  public void test_unknown_word_outputUnknownUnigrams_true() throws IOException {
    // outputUnknownUnigrams true -> 참이면 알 수없는 단어에 대한 유니 그램을 출력합니다.
    // [2018] -> [2],[0],[1],[8]
    try(TokenStream ts = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.NONE, KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, true)
        .tokenStream("dummy", "2018 평창 동계올림픽대회")){
      TokenView.analyzer(ts);
    }
  }
  public void test_unknown_word_outputUnknownUnigrams_false() throws IOException {
    try(TokenStream ts = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.NONE, KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, false)
        .tokenStream("dummy", "2018 평창 동계올림픽대회")){
      TokenView.analyzer(ts);
    }
  }

  public void test_user_dict() throws IOException {
    try(TokenStream ts = new KoreanAnalyzer(TestKoreanTokenizer.readDict(),
        KoreanTokenizer.DEFAULT_DECOMPOUND, KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, true)
        .tokenStream("dummy", "2018년도 세종, 2019년도 세종시")){
      TokenView.analyzer(ts);
    }
  }
}
