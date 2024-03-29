package TweetsIndexer;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


public class TweetsIndexer {

    private IndexWriter writer;
    private long docCounter;

    TweetsIndexer(IndexWriter iw) {

        writer = iw;
        docCounter = 0;

    }

    public void Indexing(String linedoc) {

        try {

            String[] wordsarray = StringUtils.split(linedoc, "\t");

            if (wordsarray.length == 7 && StringUtils.isNumeric(wordsarray[0]) && StringUtils.isNumeric(wordsarray[1]) && Long.valueOf(wordsarray[1]) > 100000) {

                String tweetid, userid, content, userscreenname, username, position, tweets;

                Document doc = new Document();

                tweetid = wordsarray[0];
                userid = wordsarray[1];
                username = wordsarray[2];
                userscreenname = wordsarray[3];
                content = wordsarray[4];
                position = wordsarray[6];
                tweets = username + "\t" + content;


                String[] tweetEntity = StringUtils.split(wordsarray[5], ";;");

                if (tweetEntity.length == 4) {

                    String[] retweetid = tweetEntity[0].split(",");
                    String[] hashtags = tweetEntity[1].split(",");
                    String[] urls = tweetEntity[2].split(",");
                    String[] mentions = tweetEntity[3].split(",");

                    for (int i = 1; i < retweetid.length; ++i) {
                        doc.add(new StringField("retweetid", retweetid[i], Field.Store.NO));
                    }

                    for (int i = 1; i < hashtags.length; ++i) {
                        doc.add(new StringField("hashtag", hashtags[i], Field.Store.NO));
                    }

                    for (int i = 1; i < urls.length; ++i) {
                        doc.add(new StringField("url", urls[i], Field.Store.NO));
                    }

                    for (int i = 1; i < mentions.length; ++i) {
                        doc.add(new StringField("mention", mentions[i], Field.Store.NO));
                    }

                }

                doc.add(new StringField("tweetid", tweetid, Field.Store.YES));

                doc.add(new TextField("content", new StringReader(content)));

                doc.add(new StringField("userid", userid, Field.Store.NO));

                doc.add(new StringField("username", username, Field.Store.YES));

                doc.add(new StringField("username", userscreenname, Field.Store.YES));

                doc.add(new StringField("position", position, Field.Store.NO));

                doc.add(new StoredField("tweets", tweets));

                if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                    //System.out.println("adding " + position);

                    writer.addDocument(doc);

                    docCounter++;
                } else {
                    System.out.println("updating " + position);
                    writer.updateDocument(new Term("tweetid", tweetid), doc);

                    docCounter++;
                }
            }
        } catch (Exception ex) {

            System.out.println("Indexing Error:" + ex.getMessage());

        }

    }

    public static Analyzer SetAnalyzer() {

        try {

            Map<String, Analyzer> analyzerPerField = new HashMap<>();
            analyzerPerField.put("userid", new KeywordAnalyzer());
            analyzerPerField.put("tweetid", new KeywordAnalyzer());
            analyzerPerField.put("username", new SimpleAnalyzer());
            analyzerPerField.put("hashtag", new SimpleAnalyzer());
            analyzerPerField.put("url", new KeywordAnalyzer());
            analyzerPerField.put("retweetid", new KeywordAnalyzer());
            analyzerPerField.put("mention", new SimpleAnalyzer());
            Analyzer analyzer;

            try {
                analyzer = CustomAnalyzer.builder()
                        .withTokenizer(StandardTokenizerFactory.class)
                        .addTokenFilter(StandardFilterFactory.class)
                        .addTokenFilter(LowerCaseFilterFactory.class)
                        .addTokenFilter(KStemFilterFactory.class)
                        .addTokenFilter(StopFilterFactory.class, "ignoreCase", "false", "words", "stopwords.txt", "format", "wordset")
                        .build();
            } catch (IOException io) {

                analyzer = CustomAnalyzer.builder()
                        .withTokenizer(StandardTokenizerFactory.class)
                        .addTokenFilter(StandardFilterFactory.class)
                        .addTokenFilter(LowerCaseFilterFactory.class)
                        .addTokenFilter(KStemFilterFactory.class)
                        .addTokenFilter(StopFilterFactory.class)
                        .build();
            }

            return new PerFieldAnalyzerWrapper(analyzer, analyzerPerField);
        } catch (Exception ex) {
            System.out.println("SetAnalyzer Error:  " + ex.getClass() +
                    "\n with message: " + ex.getMessage());
            return new StandardAnalyzer();
        }
    }


    public long CountDocs() {

        return docCounter;

    }
}
