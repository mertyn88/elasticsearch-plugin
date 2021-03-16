package org.elasticsearch.index.analysis.filter;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public final class TestFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    public TestFilter(TokenStream tokenStream){
        super(tokenStream);
    }

    @Override
    public boolean incrementToken() throws IOException {
        System.out.println("call incrementToken method");
        if(input.incrementToken()){
            setAttributesFromQueue(termAtt.toString());
            return true;
        }
        return false;
    }

    private void setAttributesFromQueue(String term) {
        termAtt.setEmpty().append("sample_"+term);
        typeAtt.setType("WORD");
    }
}
