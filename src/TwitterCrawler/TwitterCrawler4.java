package TwitterCrawler;

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

public class TwitterCrawler4 {

    public static void main(String[] args) throws Exception {


        String nameFileName = "./TweetsTest4";
        String Consumerkey = "uZB6Fcxe84IJiE4wx6C7fm409";
        String ConsumerSecret = "UHJK2Aq88qfwJd8Jw0TK4bftli2lNc2lD1UajP1dlooOD61eZl";
        String AccessToken = "636879179-CfBB9fYs0IpuHWSvcBC82Z95a6PJAmJx47LSFi2c";
        String AccessTokenSecret = "0fwdZ3P1gjjeMU35GoUNc7TCH6dTokMubrymEnplY81ip";
        boolean restart = false;
        int num = 4;

        int time = 1005;

        ConfigurationBuilder cf = new ConfigurationBuilder();

        cf.setDebugEnabled(true)
                .setOAuthConsumerKey(Consumerkey)
                .setOAuthConsumerSecret(ConsumerSecret)
                .setOAuthAccessToken(AccessToken)
                .setOAuthAccessTokenSecret(AccessTokenSecret)
                .setTweetModeExtended(true)
                .setIncludeEntitiesEnabled(true);

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

                    if (user.getFollowersCount() > 200 && user.getStatusesCount() > 1000 && !user.isProtected() && user.getId() % num == 3) {

                        userids.put(user.getId(), 0);
                    }

                }

            } catch (TwitterException te) {
                System.out.println("User Error: " + te.getErrorMessage());
            }

        } else {
            String[] wordsarray;
            BufferedReader br = new BufferedReader(new FileReader(nameFileName));
            String line;

            while ((line = br.readLine()) != null) {

                wordsarray = line.split("\t");

                try {

                    if (wordsarray.length > 0) {

                        if (StringUtils.isNumeric(wordsarray[0]) && Long.valueOf(wordsarray[0]) > 100000) {

                            userids.put(Long.valueOf(wordsarray[0]), 1);

                        }
                    }

                } catch (Exception ex) {

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

                                        String text = st.getText().replaceAll("\r?\n", "   ")
                                                    .replaceAll("\t", "  ");

                                        StringBuilder stringBuilder = new StringBuilder().append(st.getUser().getId())
                                                .append("\t").append(st.getUser().getScreenName()).append("\t")
                                                .append(st.getUser().getName().replaceAll("\t", "  ")).append("\t")
                                                .append(text).append("\t");

                                        try {

                                            stringBuilder.append("#");

                                            for (HashtagEntity hastag : st.getHashtagEntities()) {

                                                stringBuilder.append(",").append(hastag.getText());
                                            }

                                            stringBuilder.append(";;");

                                        } catch (Exception ex) {

                                            System.out.println("HashTag Error: " + ex.getMessage());
                                        }

                                        try {

                                            stringBuilder.append("/");

                                            for (URLEntity url : st.getURLEntities()) {

                                                stringBuilder.append(",").append(url.getDisplayURL());
                                                stringBuilder.append(",").append(url.getExpandedURL());

                                            }

                                            stringBuilder.append(";;");

                                        }
                                        catch (Exception ex){

                                            System.out.println("URL Error: " + ex.getMessage());
                                        }

                                        try {

                                            stringBuilder.append("@");

                                            for (UserMentionEntity userMentionEntity : st.getUserMentionEntities()) {

                                                stringBuilder.append(",").append(userMentionEntity.getScreenName());

                                            }

                                            //stringBuilder.append(";;");

                                        }
                                        catch (Exception ex){

                                            System.out.println("UserMention Error: " + ex.getMessage());
                                        }

                                        bw.write(stringBuilder.toString());
                                        bw.newLine();
                                        //System.out.println(st.getUser().getName() + "------" + st.getText());

                                    }
                                    bw.flush();

                                } catch (Exception ex) {

                                    System.out.println(nameFileName + "\tRound:\t" + count + "\tfailed" + ex.getMessage());

                                }
                                bw.flush();

                            } catch (TwitterException te) {
                                System.out.println("Timeline Error: " + te.getErrorMessage());
                                Thread.sleep(time);
                                break;
                            }

                            System.out.println("counter: " + count++);

                            Thread.sleep(time);
                        }
                    }

                    if (count >= 120) {

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

                restart = false;

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
                                        && user.getStatusesCount() >= 1000 && user.getId() % num == 3) {

                                    userids.put(user.getId(), 0);

                                }

                            }
                        } catch (TwitterException te) {

                            System.out.println("User Error: " + te.getErrorMessage());
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
            } else if (!breakmark) {
                nomoreusers = true;
            }

        }

        bw.close();
    }
}
