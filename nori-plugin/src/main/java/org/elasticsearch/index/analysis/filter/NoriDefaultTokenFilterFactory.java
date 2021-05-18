package org.elasticsearch.index.analysis.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilterFactory;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;


public class NoriDefaultTokenFilterFactory extends AbstractTokenFilterFactory {

    public NoriDefaultTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
        Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        KoreanPartOfSpeechStopFilterFactory koreanPartOfSpeechStopFilterFactory = new KoreanPartOfSpeechStopFilterFactory(new HashMap<>(){{
            put("luceneMatchVersion", Version.LATEST.toString());
            put("tags", "JX,ETM,EF,NNG,SP,EC");
        }});

        System.out.println("Default Nori token filter");
        return koreanPartOfSpeechStopFilterFactory.create(tokenStream);
    }
}
