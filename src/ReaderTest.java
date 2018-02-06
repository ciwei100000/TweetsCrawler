import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReaderTest {

    public static void main(String[] args) throws Exception {
        ArrayList<Long> ids=new ArrayList<Long>();
        BufferedReader br=new BufferedReader(new FileReader("./Tweets"));
        String line;

        String[] wordsarray;
        long linenumber = 0;

        while ((line=br.readLine())!=null)
        {

            linenumber++;

            wordsarray = StringUtils.split(line,"\t");

            if (wordsarray.length > 0){

                try {

                    if (StringUtils.isNumeric(wordsarray[0])) {

                        System.out.println(linenumber + " " + Long.valueOf(wordsarray[0]));
                    }

                }
                catch(Exception ex){
                    continue;
                }
            }

        }

    }

}
