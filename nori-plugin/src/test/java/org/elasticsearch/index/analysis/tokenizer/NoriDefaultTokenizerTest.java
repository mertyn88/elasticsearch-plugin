package org.elasticsearch.index.analysis.tokenizer;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.plugin.analysis.nori.NoriDefaultPlugin;
import org.elasticsearch.test.ESTestCase;
import org.junit.Test;

public class NoriDefaultTokenizerTest extends ESTestCase {

    @Test
    public void test_nori_default_tokenizer() throws IOException {
        // 입력 키워드
        String keyword = "  와디다락 맛있고, 대한민국은 나라입니다.  ";
        System.out.println("Input keyword :: " + keyword);

        // 테스트 가상의 색인 생성
        final TestAnalysis testAnalysis = createTestAnalysis(new Index("test", "_na_"), Settings.EMPTY, new NoriDefaultPlugin());

        // 토크나이저 팩토리 설정 ( 커스텀 된것이 없으므로 기본 standard)
        TokenizerFactory tokenizerFactory = testAnalysis.tokenizer.get("nori_default_tokenizer");
        // 토큰필터 설정 ( 커스텀된 test_filter와 기본 filter인 lowercase 설정 )
        TokenFilterFactory tokenFilterFactory = testAnalysis.tokenFilter.get("nori_stoptag_filter");
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
