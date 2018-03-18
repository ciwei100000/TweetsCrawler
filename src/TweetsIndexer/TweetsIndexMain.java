package TweetsIndexer;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Stack;

public class TweetsIndexMain {

    public static void main(String[] args) throws Exception {

        String usage = " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";

        String indexPath = "index";
        String docsPath = null;
        boolean create = true;

        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        try {

            Date start = new Date();

            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));

            Analyzer analyzer = TweetsIndexer.SetAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            TweetsIndexer tweetsIndexer = new TweetsIndexer(writer);

            File file = new File(docsPath);

            if (file.exists()) {
                Stack<File> fileStack = new Stack<>();
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    try {
                        for (File file2 : files) {
                            if (file2.isDirectory()) {
                                fileStack.push(file2);
                            } else {
                                doIndex(tweetsIndexer, file2.getAbsolutePath());
                            }
                        }
                        File temp_file;
                        while (!fileStack.isEmpty()) {
                            temp_file = fileStack.pop();
                            files = temp_file.listFiles();
                            for (File file2 : files) {
                                if (file2.isDirectory()) {
                                    fileStack.push(file2);
                                } else {
                                    doIndex(tweetsIndexer, file2.getAbsolutePath());
                                }
                            }
                        }

                    } catch (NullPointerException ex) {
                        System.out.println("No directories in the directories");
                    }
                } else {
                    doIndex(tweetsIndexer, file.getAbsolutePath());
                }
            } else {
                System.out.println("Directory does not exist !");
            }

            Date end = new Date();

            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

            writer.close();

        } catch (Exception ex) {
            System.out.println(" caught a " + ex.getClass() +
                    "\n with message: " + ex.getMessage());
        }

    }

    private static void doIndex(TweetsIndexer tweetsIndexer, String docPath) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(docPath));
            String line;

            String[] wordsarray;
            long linenumber = 0;

            while ((line = br.readLine()) != null) {

                linenumber++;

                wordsarray = StringUtils.split(line, "\t");

                if (wordsarray.length == 6) {

                    try {

                        if (StringUtils.isNumeric(wordsarray[0]) && StringUtils.isNumeric(wordsarray[1]) && Long.valueOf(wordsarray[1]) > 100000) {

                            tweetsIndexer.Indexing(line + "\t" + docPath + "::" + linenumber);

                        }

                    } catch (Exception ex) {
                        System.out.println("Error: " + linenumber + "\t" + ex.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println("Error: " + docPath + "\t" + ex.getMessage());
        }

    }
}
