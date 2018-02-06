import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterCrawler2 {

    public static void main(String[] args) throws Exception {


        String nameFileName = "./Tweets2";
        String Consumerkey = "26Msk70edozh5gXbPHreFGP5r";
        String ConsumerSecret = "hkqXjbe8zLSaUb8W35zT61EIm2nUYw2P181nypplkbdRr0F2Zp";
        String AccessToken = "636879179-7kLWwgzsRuM0aKXyGpTOJSCl39ZCaKxa9xnykdhs";
        String AccessTokenSecret = "Krce7neNLgvTd5lf9E2mzHa8hMVDxRLdexboXj0hauIqX";
	boolean restart = true;

        int time = 1005;

        ConfigurationBuilder cf = new ConfigurationBuilder();

        cf.setDebugEnabled(true)
                .setOAuthConsumerKey(Consumerkey)
                .setOAuthConsumerSecret(ConsumerSecret)
                .setOAuthAccessToken(AccessToken)
                .setOAuthAccessTokenSecret(AccessTokenSecret)
                .setTweetModeExtended(true);

        TwitterFactory tf = new TwitterFactory(cf.build());

        Twitter twitter = tf.getInstance();

        BufferedWriter bw = new BufferedWriter(new FileWriter(nameFileName, restart));

        HashMap<Long, Integer> userids = new HashMap<>();

        List<User> users;

        if (!restart) {

            try {

                users = twitter.getFriendsList(twitter.getId(), -1, 200);

                System.out.println("Friend Count: " + users.size());

                for (User user : users) {

                    if (user.getFollowersCount() > 200 && user.getStatusesCount() > 1000 && !user.isProtected() && user.getId() % 3 == 1) {

                        userids.put(user.getId(), 0);
                    }

                }

            } catch (TwitterException te) {
                System.out.println("User Error: " + te.getErrorMessage());
            }

        }
        else
        {
            String[] wordsarray;
            BufferedReader br=new BufferedReader(new FileReader(nameFileName));
            String line;

            while ((line=br.readLine())!=null)
            {

                wordsarray = line.split("\t");

                try {

                    for (String word : wordsarray) {

                        if (StringUtils.isNumeric(word)) {

                            userids.put(Long.valueOf(word), 1);

                        }
                    }

                }
                catch(Exception ex){

                    System.out.println("User ID reading error");
                }
            }


        }

        System.out.println("User Count: " + userids.size());

        Thread.sleep(time);

        boolean nomoreusers = false;
        int count = 0;

        while (!nomoreusers) {

            boolean breakmark = false;

            if (count == 0) {

                for (Map.Entry<Long, Integer> pair : userids.entrySet()) {

                    long userid = pair.getKey();
                    int value = pair.getValue();

                    System.out.println("Count: " + count + "\tuserid\t" + userid + "\tValue\t" + value);

                    if ((value % 2) == 0) {

                        for (int page = 1; page <= 5; page++) {

                            pair.setValue(value | 1);

                            try {

                                List<Status> statuses = twitter.getUserTimeline(userid, new Paging(page, 200));
                                System.out.println("Statuses Count: " + statuses.size());

                                try {

                                    for (Status st : statuses) {

                                        String text = st.getText().replaceAll("\r?\n", "   ");

                                        bw.write(st.getUser().getId() + "\t" + st.getUser().getName() + "\t" + text);
                                        bw.newLine();
                                        //System.out.println(st.getUser().getName() + "------" + st.getText());

                                    }
                                    bw.flush();

                                } catch (Exception ex) {

                                    System.out.println(nameFileName + "\tRound:\t" + count + "\tfailed");

                                }
                                bw.flush();

                            }
                            catch (TwitterException te){
                                System.out.println("Timeline Error: " + te.getErrorMessage());
                                Thread.sleep(time);
                                break;
                            }

                            System.out.println("counter: " + count++);

                            Thread.sleep(time);
                        }
                    }

                    if (count >= 120){

                        breakmark = true;
                        break;
                    }

                }
            }

            System.out.println("Step to II, count: " + count);

//            Map<String, RateLimitStatus> map = twitter.getRateLimitStatus("statuses,friends");
//
//            int size = map.entrySet().toArray().length;
//
//            for (int i = 0; i < size; i++) {
//                System.out.println(map.entrySet().toArray()[i]);
//            }


            if ((count == 0 && restart) || count >= 120 && userids.size() < 1000000) {

                restart =false;

                for (Map.Entry<Long, Integer> pair : userids.entrySet()) {

                    long userid = pair.getKey();
                    int value = pair.getValue();

                    System.out.println("II. Count: " + count + "\tuserid\t" + userid + "\tValue\t" + value);

                    if ((value / 2) == 0) {

                        try {

                            users = twitter.getFriendsList(userid, -1, 200);
                            users.addAll(twitter.getFollowersList(userid, -1, 200));
                            pair.setValue(value | 2);

                            System.out.println("Friend Follower Count: " + users.size());

                            for (User user : users) {

                                if (!userids.containsKey(user.getId()) && !user.isProtected() && user.getFollowersCount() > 200
                                        && user.getStatusesCount() >= 1000 && user.getId() % 3 == 1) {

                                    userids.put(user.getId(), 0);

                                }

                            }
                        }
                        catch(TwitterException te){

                            System.out.println("User Error: "+te.getErrorMessage());
                            userids.remove(userid);

                        }

                        System.out.println("User Count: " + userids.size());

                        Thread.sleep(time);
                        count = 0;
                        break;

                    }
                }

            } else if (count < 120) {

                count++;
                Thread.sleep(time);
            } else if (!breakmark){
                nomoreusers = true;
            }

        }

        bw.close();
    }
}
