import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReaderTest {

    public static void main(String[] args) throws Exception {
        //ArrayList<Long> ids = new ArrayList<Long>();
        BufferedReader br = new BufferedReader(new FileReader("/mnt/hgfs/task/TweetsTest1"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("./Pr.txt"));
        String line;

        Set<String> tweetids = new HashSet<>();

        String[] wordsarray;
        long linenumber = 0;

        while ((line = br.readLine()) != null) {

            //linenumber++;

            wordsarray = StringUtils.split(line, "\t");

            try {

                if (wordsarray.length == 6 && StringUtils.isNumeric(wordsarray[0]) && StringUtils.isNumeric(wordsarray[1])
                        && Long.valueOf(wordsarray[1]) > 100000) {

                    if (!tweetids.contains(wordsarray[0])) {

                        tweetids.add(wordsarray[0]);
                        linenumber++;

                        bw.write(wordsarray[0] + "\t" + "1");
                        bw.newLine();

                    }

                }

                bw.flush();
            } catch (Exception ex) {
                System.out.println("Error: " + line + " " + linenumber);
            }
        }
        System.out.println("Total Line: " + linenumber);
        br.close();
        bw.close();

    }


}


