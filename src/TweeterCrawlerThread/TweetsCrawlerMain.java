package TweeterCrawlerThread;

import TweetsIndexer.TweetsIndexer;
import twitter4j.Twitter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class TweetsCrawlerMain {

    public static void main(String[] args){

        String usage = " [-output OUTPUT_FILE] [-tokens TOKENS_FILE_PATH] [-restart] [-num NUM_OF_THREADS]\n\n";

        boolean isRestart = false;
        String outputpath = null;
        String tokensfilepath = null;
        int numofthread = 1;

        for (int i = 0; i < args.length; i++) {
            if ("-restart".equals(args[i])) {
                isRestart = true;
                i++;
            } else if ("-tokens".equals(args[i])) {
                tokensfilepath = args[i + 1];
                i++;
            } else if ("-output".equals(args[i])) {
                outputpath = args[i + 1];
            } else if ("-num".equals(args[i])) {
                numofthread = args[i + 1].;
            }
        }

        if (tokensfilepath == null || outputpath == null || numofthread == 0 || Files.isReadable(Paths.get(tokensfilepath))){
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        try {

            BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
            ExecutorService executorService = Executors.newCachedThreadPool();

            TweetsFileWriter tweetsFileWriter = new TweetsFileWriter(blockingQueue,isRestart,outputpath);

            String patterns = "^#";

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tokensfilepath))) {

                String line;
                int num = numofthread;
                ArrayList<String> tokens = new ArrayList<>();
                ArrayList<Future> futures = new ArrayList<>();

                while ((line = bufferedReader.readLine()) != null ) {

                    if(Pattern.matches(patterns,line)){
                        tokens.add(line);
                    }

                    if (tokens.size() == 4 && num > 0){

                        num--;
                        TwitterCrawlerThread twitterCrawlerThread = new TwitterCrawlerThread(tokens.get(0),tokens.get(1)
                                ,tokens.get(2),tokens.get(3), numofthread, (numofthread - num), blockingQueue);
                        tokens.clear();
                        futures.add(executorService.submit(tweetsFileWriter));
                    }
                }

                Future writerThread = executorService.submit(tweetsFileWriter);

            }
            catch(Exception ex){

            }

        }
        catch (Exception ex){

        }
    }
}
