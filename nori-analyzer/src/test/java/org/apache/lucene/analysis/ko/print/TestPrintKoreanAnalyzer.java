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
package org.apache.lucene.analysis.ko.print;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.TestKoreanTokenizer;
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Test Korean morphological analyzer
 */
public class TestPrintKoreanAnalyzer extends BaseTokenStreamTestCase {

  public void analyzer(TokenStream ts) throws IOException {
    CharTermAttribute termAtt = (CharTermAttribute)ts.getAttribute(CharTermAttribute.class);
    OffsetAttribute offsetAtt = (OffsetAttribute)ts.getAttribute(OffsetAttribute.class);
    PositionIncrementAttribute posIncrAtt = (PositionIncrementAttribute)ts.getAttribute(PositionIncrementAttribute.class);
    TypeAttribute typeAtt = (TypeAttribute)ts.getAttribute(TypeAttribute.class);
    KeywordAttribute keywordAtt = (KeywordAttribute)ts.getAttribute(KeywordAttribute.class);
    PartOfSpeechAttribute posAttr = (PartOfSpeechAttribute)ts.getAttribute(PartOfSpeechAttribute.class);


    ts.reset();
    while(ts.incrementToken()){
      if(keywordAtt != null){
        System.out.println("keywordAtt.toString() " + keywordAtt.toString());
      }
      System.out.println("termAtt.toString() " + termAtt.toString());
      System.out.println("typeAtt.toString() " + typeAtt.type());
      System.out.println("getLeftPOS " + posAttr.getLeftPOS());
      System.out.println("getRightPOS " + posAttr.getRightPOS());
      System.out.println("getPOSType " + posAttr.getPOSType());

      System.out.println("(long)offsetAtt.startOffset() " + (long)offsetAtt.startOffset());
      System.out.println("(long)offsetAtt.endOffset() " + (long)offsetAtt.endOffset());
      System.out.println("posIncrAtt.getPositionIncrement() " + posIncrAtt.getPositionIncrement());
      System.out.println("===========================================");
    }
  }

  public void test_sentence() throws  IOException {
    try(TokenStream ts = new KoreanAnalyzer()
        .tokenStream("dummy", "검색엔진은 대단한 나라입니다.")) {
      analyzer(ts);
    }
  }

  public void test_stop_tag() throws  IOException {
    /*
      NNG | General Noun | 일반 명사 | 나라(NNG)
      NNP | Proper Noun  | 고유 명사 | 한국(NNP)
     */
    Set<POS.Tag> stopTags = new HashSet<>(Arrays.asList(POS.Tag.NNG, POS.Tag.NNP, POS.Tag.JX));
    try(TokenStream ts = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.DISCARD, stopTags, false)
        .tokenStream("dummy", "한국은 대단한 나라입니다.")){
      analyzer(ts);
    }
  }


  public void testStopTags() throws IOException {
    Set<POS.Tag> stopTags = Arrays.asList(POS.Tag.NNP, POS.Tag.NNG).stream().collect(Collectors.toSet());
    Analyzer a = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.DISCARD, stopTags, false);
    assertAnalyzesTo(a, "한국은 대단한 나라입니다.",
        new String[]{"은", "대단", "하", "ᆫ", "이", "ᄇ니다"},
        new int[]{ 2, 4, 6, 6, 10, 10 },
        new int[]{ 3, 6, 7, 7, 13, 13 },
        new int[]{ 2, 1, 1, 1, 2, 1 }
    );
    a.close();
  }

  public void testUnknownWord() throws IOException {
    Analyzer a = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.DISCARD,
        KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, true);

    assertAnalyzesTo(a,"2018 평창 동계올림픽대회",
        new String[]{"2", "0", "1", "8", "평창", "동계", "올림픽", "대회"},
        new int[]{0, 1, 2, 3, 5, 8, 10, 13},
        new int[]{1, 2, 3, 4, 7, 10, 13, 15},
        new int[]{1, 1, 1, 1, 1, 1, 1, 1});
    a.close();

    a = new KoreanAnalyzer(null, KoreanTokenizer.DecompoundMode.DISCARD,
        KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, false);

    assertAnalyzesTo(a,"2018 평창 동계올림픽대회",
        new String[]{"2018", "평창", "동계", "올림픽", "대회"},
        new int[]{0, 5, 8, 10, 13},
        new int[]{4, 7, 10, 13, 15},
        new int[]{1, 1, 1, 1, 1});
    a.close();
  }

  /**
   * blast random strings against the analyzer
   */
  public void testRandom() throws IOException {
    Random random = random();
    final Analyzer a = new KoreanAnalyzer();
    checkRandomData(random, a, atLeast(1000));
    a.close();
  }

  /**
   * blast some random large strings through the analyzer
   */
  public void testRandomHugeStrings() throws Exception {
    Random random = random();
    final Analyzer a = new KoreanAnalyzer();
    checkRandomData(random, a, 2 * RANDOM_MULTIPLIER, 8192);
    a.close();
  }

  // Copied from TestKoreanTokenizer, to make sure passing
  // user dict to analyzer works:
  public void testUserDict() throws IOException {
    final Analyzer analyzer = new KoreanAnalyzer(TestKoreanTokenizer.readDict(),
        KoreanTokenizer.DEFAULT_DECOMPOUND, KoreanPartOfSpeechStopFilter.DEFAULT_STOP_TAGS, false);
    assertAnalyzesTo(analyzer, "c++ 프로그래밍 언어",
        new String[]{"c++", "프로그래밍", "언어"},
        new int[]{0, 4, 10},
        new int[]{3, 9, 12},
        new int[]{1, 1, 1}
    );
  }
}
