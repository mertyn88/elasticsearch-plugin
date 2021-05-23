package org.elasticsearch.index.analysis.analyzer;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.plugin.analysis.nori.NoriDefaultPlugin;
import org.elasticsearch.test.ESTestCase;
import org.junit.Test;

public class NoriDefaultAnalyzerTest extends ESTestCase {

    @Test
    public void test_nori_default_analyzer() throws IOException {
        // 입력 키워드
        String keyword = "  와디다락 맛있고, 대한민국은 나라입니다.  ";
        System.out.println("Input keyword :: " + keyword);

        // 테스트 가상의 색인 생성
        final ESTestCase.TestAnalysis testAnalysis = createTestAnalysis(new Index("test", "_na_"), Settings.EMPTY, new NoriDefaultPlugin());

        Analyzer analyzer = testAnalysis.indexAnalyzers.get("nori_default_analyzer");
        TokenStream tokenStream = analyzer.tokenStream("dummy", keyword);

        // 만들어진 토큰스트림의 키워드 출력
        CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttr = tokenStream.addAttribute(TypeAttribute.class);
        PartOfSpeechAttribute posAttr = tokenStream.getAttribute(PartOfSpeechAttribute.class);

        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                System.out.println(termAtt.toString() + " [" + typeAttr.type() + "] [" + posAttr.getLeftPOS() + "]");
            }
            tokenStream.end();
        } finally {
            tokenStream.close();
        }
    }
}
