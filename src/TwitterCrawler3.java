import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterCrawler3 {

    public static void main(String[] args) throws Exception {


        String nameFileName = "./Tweets3";
        String Consumerkey = "BxM5joby45AZ4zlxinGeGEgEs";
        String ConsumerSecret = "AvLohJY81UJwQAYcAdBQR1G0zYtgPr4UpxTQdyZ8tjHfDExWyX";
        String AccessToken = "636879179-WIelQuAe5RFHJKuLJk4VglIBIYga26HB0DL9864Y";
        String AccessTokenSecret = "50fnNL9DpH8H814ytxiSJ5DpoG40M0DM5JC0bQ2mj6kQd";

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

        BufferedWriter bw = new BufferedWriter(new FileWriter(nameFileName));

        HashMap<Long, Integer> userids = new HashMap<>();

        List<User> users = twitter.getFriendsList(twitter.getId(), -1, 200);

        System.out.println("Friend Count: " + users.size());

        for (User user : users) {

            if (user.getFollowersCount() > 200 && !user.isProtected()&&
                    user.getStatusesCount() > 1000 && user.getId() % 3 == 2) {
                userids.put(user.getId(), 0);
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
                            List<Status> statuses = twitter.getUserTimeline(userid, new Paging(page, 200));
                            System.out.println("Statuses Count: " + statuses.size());

                            try {

                                for (Status st : statuses) {

                                    bw.write(st.getUser().getId() + "\t" + st.getUser().getName() + "\t" + st.getText());
                                    bw.newLine();
                                    //System.out.println(st.getUser().getName() + "------" + st.getText());

                                }
                                bw.flush();

                            } catch (Exception te) {

                                System.out.println(nameFileName + "\tRound:\t" + count + "\tfailed");
                            }

                            System.out.println("counter: " + count++);

                            Thread.sleep(time);
                        }
                    }

                    if (count >= 120)
                        break;
                }
            }

            System.out.println("Step to II, count: " + count);

            Map<String, RateLimitStatus> map = twitter.getRateLimitStatus("statuses,friends");

            int size = map.entrySet().toArray().length;

            for (int i = 0; i < size; i++) {
                System.out.println(map.entrySet().toArray()[i]);
            }


            if (count >= 120 && userids.size() < 1000000) {

                for (Map.Entry<Long, Integer> pair : userids.entrySet()) {


                    long userid = pair.getKey();
                    int value = pair.getValue();

                    System.out.println("II. Count: " + count + "\tuserid\t" + userid + "\tValue\t" + value);

                    if ((value / 2) == 0) {

                        users = twitter.getFriendsList(userid, -1, 200);
                        users.addAll(twitter.getFollowersList(userid, -1, 200));
                        pair.setValue(value | 2);

                        System.out.println("Friend Follower Count: " + users.size());

                        for (User user : users) {


                            if (!userids.containsKey(user.getId()) && !user.isProtected() && user.getFollowersCount() > 200
                                    && user.getStatusesCount() >= 1000 && user.getId() % 3 == 2)
                            {

                                userids.put(user.getId(), 0);

                            }

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
