import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class TestJson {

    static public void main(String[] args) throws Exception {

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/mnt/hgfs/task/IndexOutput")));
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addTokenFilter(StandardFilterFactory.class)
                .addTokenFilter(LowerCaseFilterFactory.class)
                .addTokenFilter(KStemFilterFactory.class)
                .addTokenFilter(StopFilterFactory.class)
                .build();
        QueryParser parser = new QueryParser("content",analyzer);

        Query query = parser.parse("iphone");
        TopDocs results = searcher.search(query, 5);
        ScoreDoc[] hits = results.scoreDocs;
        Document doc;

        int numTotalHits = Math.toIntExact(results.totalHits);
        System.out.println(numTotalHits + " total matching documents");

        for (int i = 0; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            System.out.println(doc.get("tweets"));


        }
    }
}