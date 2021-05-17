package org.apache.lucene.analysis.ko.util;

import java.io.IOException;
import java.util.Optional;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class TokenView {

    /*
        TokenStream
            - Source text를 Token으로 나눈 이후 token이 순서대로 나열된 상태를 Token stream 이라고 합니다.
        AttributeSource
            - AttributeSoucre는 TokenStream의 부모 클래스로 Token의 메타 정보를 담고 있습니다.
            - TokenStream이 생성될때 ArrtibuteSrouce에 보고자 하는 정보를 등록해 놓으면 그때 그때의 snap shot을 확인할 수 있습니다
     */
    public static void analyzer(TokenStream ts) throws IOException {
        // 토큰의 text
        CharTermAttribute termAtt = (CharTermAttribute)ts.getAttribute(CharTermAttribute.class);
        // 토큰의 offSet
        OffsetAttribute offsetAtt = (OffsetAttribute)ts.getAttribute(OffsetAttribute.class);
        // 토큰이 차지하는 자리의 개수
        PositionLengthAttribute positionLengthAttribute = (PositionLengthAttribute)ts.getAttribute(PositionLengthAttribute.class);
        // payload 정보를 (payload 기반 쿼리를 질의할때 Score에 영향을 준다.
        PayloadAttribute payloadAttribute = (PayloadAttribute)ts.getAttribute(PayloadAttribute.class);
        // 토큰의 유형
        TypeAttribute typeAtt = (TypeAttribute)ts.getAttribute(TypeAttribute.class);
        // 현재 읽어야 하는 토큰의 위치 반환. 생략된 토큰이 있다면 생략된 개수만큼 더해진 값이 써진다.
        // 예를들어 불용어에 의해 토큰값이 삭제 되었다면 다음노출되는 토큰값에 +1을 한다.
        PositionIncrementAttribute posIncrAtt = (PositionIncrementAttribute)ts.getAttribute(PositionIncrementAttribute.class);
        // 토큰을 키워드로 표시할 때 사용???
        KeywordAttribute keywordAtt = (KeywordAttribute)ts.getAttribute(KeywordAttribute.class);
        // 토큰의 품사 정보
        PartOfSpeechAttribute posAttr = (PartOfSpeechAttribute)ts.getAttribute(PartOfSpeechAttribute.class);


        ts.reset();
        while(ts.incrementToken()){
            Optional.ofNullable(termAtt).ifPresent(data -> {
                System.out.println("토큰 값 : " + data.toString());
            });
            Optional.ofNullable(typeAtt).ifPresent(data -> {
                System.out.println("토큰 유형 : " + data.type());
            });
            Optional.ofNullable(posAttr).ifPresent(data -> {
            });
            Optional.ofNullable(posAttr).ifPresent(data -> {
                System.out.println("Left 품사 : " + data.getLeftPOS());
                System.out.println("Right 품사 : " + data.getRightPOS());
                System.out.println("품사 유형 : " + data.getPOSType());
            });
            Optional.ofNullable(offsetAtt).ifPresent(data -> {
                System.out.println("시작 offSet : " + (long)data.startOffset());
            });
            Optional.ofNullable(offsetAtt).ifPresent(data -> {
                System.out.println("좋료 offSet : " + (long)data.endOffset());
            });
            Optional.ofNullable(posIncrAtt).ifPresent(data -> {
                System.out.println("토큰 위치 : " + data.getPositionIncrement());
            });
            Optional.ofNullable(keywordAtt).ifPresent(data -> {
                System.out.println("토큰의 키워드 여부 : " + data.isKeyword());
            });
            Optional.ofNullable(positionLengthAttribute).ifPresent(data -> {
                System.out.println("토큰의 자리값 : " + data.getPositionLength());
            });
            Optional.ofNullable(payloadAttribute).ifPresent(data -> {
                System.out.println("payloadAttribute : " + data.getPayload());
            });
            System.out.println("===========================================");
        }
    }
}
