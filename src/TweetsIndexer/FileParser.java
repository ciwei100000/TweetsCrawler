package TweetsIndexer;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileParser {

    public static String ParseFile(String path)
    {
        try {

            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            String[] wordsarray;
            long linenumber = 0;

            while ((line = br.readLine()) != null) {

                linenumber++;

                wordsarray = StringUtils.split(line, "\t");

                if (wordsarray.length == 5) {

                    try {

                        if (StringUtils.isNumeric(wordsarray[0]) && Long.valueOf(wordsarray[0]) > 100000) {

                            return line + "\t" + path +"::"+ linenumber;

                        }

                    } catch (Exception ex) {
                        System.out.println("Error: " + linenumber + "\t" + ex.getMessage());
                    }
                }
            }

        }
        catch (Exception ex)
        {

            System.out.println("ParseFile Error: " + ex.getMessage());

        }

        return "";

    }
}
