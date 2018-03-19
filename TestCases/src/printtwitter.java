
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;


import java.util.*;

public class printtwitter {
    public static void main(String[] args) throws NullPointerException, IOException {
        Scanner scan = new Scanner(System.in);
        String searchword = scan.next();


        File file = new File("/mnt/hgfs/task/Executable/finalresult.txt");
        BufferedReader reader = null;
//        double N = 1000000;
//        String docid = "971833867018686465";

        ArrayList<String> twitteridList = new ArrayList<>();

        reader = new BufferedReader(new FileReader(file));
        String tempString = "";
        while ((tempString = reader.readLine()) != null) {

//            String tempString = reader.readLine();
//            System.out.println(tempString);
//            if (tempString.contains("\t")){
            String[] wordAndIndex = tempString.split("\t");

            String word = wordAndIndex[0];
//                System.out.println(word);

            if (word.equals(searchword)) {
                String idAndCntList = wordAndIndex[1];
                String[] idAndCnt = idAndCntList.split(";");
//                double n_i = idAndCnt.length;
                for (String idcount : idAndCnt) {
                    String id = idcount.split(":")[0];
                    twitteridList.add(id);
                    if (twitteridList.size()==10){
                        break;
                    }

//                        double f_i = Integer.parseInt(idcount.split(":")[1]);
//                        double BM25 = (Math.log((N-n_i+0.5)/(n_i+0.5)))*((2.2*f_i)/(1.2+f_i));
//                        System.out.println("BM25 for "+id+" is "+BM25);

                }
            }
//            }


        }

        reader.close();


//        File file2 = new File("TweetsTest");
//        BufferedReader reader2 = null;
//
//        reader2 = new BufferedReader(new FileReader(file2));
//        String tempString2 = "";
//
//        while ((tempString2 = reader2.readLine()) != null) {
//            System.out.println(tempString2);
//            String[] fields = tempString2.split("\t");
//            if(fields.length != 6){
//                return;
//            }
//            String twitterid = fields[0];
//            String username = fields[3];
//            String content = fields[4];


//            for (String twitsid : twitteridList){
//
//                if (twitsid.equals(twitterid)){
//                    System.out.println(twitsid);
//                }
//
//            }

//        }

        FileWriter fw = null;
        fw = new FileWriter("nameContent.txt");

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream("/mnt/hgfs/task/TweetsTest1");
            sc = new Scanner(inputStream, "UTF-16");
            while (sc.hasNextLine()) {

                String line = sc.nextLine();
//                System.out.println(line);

                String[] fields = line.split("\t");
//                System.out.println(fields);
                if(fields.length != 6){
                    continue;
                }
//                System.out.println(fields[2]);

                String twitterid = fields[0];
                String username = fields[3];
                String content = fields[4];


                for (String twitsid : twitteridList){

                    if (twitsid.equals(twitterid)){
                        fw.write("{\"name\":"+"\""+username+"\",\"content\":\""+content+"\"},"+"\n");

                    }

                }
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        fw.close();


    }
}
