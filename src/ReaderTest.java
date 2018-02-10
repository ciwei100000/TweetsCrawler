import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ReaderTest {

    public static void main(String[] args) throws Exception {
        ArrayList<Long> ids=new ArrayList<Long>();
        BufferedReader br=new BufferedReader(new FileReader("./TestInput"));
        String line;

        String[] wordsarray;
        long linenumber = 0;

        while ((line=br.readLine())!=null)
        {

            linenumber++;

            wordsarray = StringUtils.split(line,"\t");

            if (wordsarray.length == 5){

                try {

                    if (StringUtils.isNumeric(wordsarray[0]) && Long.valueOf(wordsarray[0]) > 100000) {

                        String[] test2 = wordsarray[4].split(";;");

                        String[] test3 = test2[0].split(",");

                        System.out.println(linenumber + " " + test3[0]);

                    }

                }
                catch(Exception ex){
                    System.out.println("Error: "+line);
                }
            }

        }

    }

}
