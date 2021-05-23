package org.elasticsearch.index.analysis.analyzer;

import static org.apache.lucene.analysis.TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.KoreanReadingFormFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.util.ClasspathResourceLoader;
import org.apache.lucene.util.IOUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;


public class NoriDefaultAnalyzer extends AbstractIndexAnalyzerProvider<KoreanAnalyzer> {

    public NoriDefaultAnalyzer(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);
    }

    @Override
    public KoreanAnalyzer get() {
        return new KoreanAnalyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                // tokenizer + filter(테스트용 한자 변환)
                Tokenizer tokenizer = new KoreanTokenizer(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, readDict(), KoreanTokenizer.DecompoundMode.NONE, false);
                KoreanReadingFormFilter filter = new KoreanReadingFormFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, filter);
            }
        };
    }

    public UserDictionary readDict() {
        try (InputStream stream = new ClasspathResourceLoader(getClass().getClassLoader()).openResource("user_dict.txt")) {
            String encoding = IOUtils.UTF_8;
            CharsetDecoder decoder = Charset.forName(encoding).newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
            Reader reader = new InputStreamReader(stream, decoder);
            return UserDictionary.open(reader);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
