package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.filter.TestFilter;

public class TestTokenFilterFactory extends AbstractTokenFilterFactory{

    public TestTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
        Settings settings) {
        super(indexSettings, name, settings);
        //System.out.println("Environment toString ::: " + environment.toString());
        //System.out.println("Environment binFile ::: " + environment.binFile());
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        System.out.println("token stream 내뱉는 create 함수");
        return new TestFilter(tokenStream);
    }
}
