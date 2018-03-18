import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.StringReader;

public class testlu {

    public static void main(String[] args) throws Exception {

        String text
                = "Lucene is simple 0.37 。 yet powerful java based search open libraries. apples apple flying apply teachers powerful based goes ";
        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(StandardFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(KStemFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();

        TokenStream tokenStream
                = analyzer.tokenStream("content",
                new StringReader(text));
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            System.out.print("[" + charTermAttribute.toString() + "]");
        }

    }
}
