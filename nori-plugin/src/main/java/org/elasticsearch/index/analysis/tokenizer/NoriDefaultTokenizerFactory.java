package org.elasticsearch.index.analysis.tokenizer;

import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.plugin.analysis.nori.NoriDefaultPlugin;


public class NoriDefaultTokenizerFactory extends AbstractTokenizerFactory {


    public NoriDefaultTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
    }

    @Override
    public Tokenizer create() {
        KoreanTokenizerFactory koreanTokenizerFactory = new KoreanTokenizerFactory(new HashMap<>(){{
            put("discardPunctuation", "true");   // 출력에서 구두점 토큰을 삭제해야하는 경우
            put("outputUnknownUnigrams", "false");
            put("userDictionary", "user_dict.txt");
            put("decompoundMode", KoreanTokenizer.DecompoundMode.NONE.toString());
        }});
        try {
            koreanTokenizerFactory.inform(new ClasspathResourceLoader(getClass().getClassLoader()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Default Nori tokenizer");
        return koreanTokenizerFactory.create();
    }
}
