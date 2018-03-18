import org.apache.commons.lang3.StringUtils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class TwitterCrawlerThread implements Runnable {

    final private Twitter twitter;
    final private HashMap<Long, Integer> userids;
    final private boolean isRestart;
    final private int numOfThread;
    final private int numthThread;
    final private  BlockingQueue<String> queue;


    public TwitterCrawlerThread(String consumerkey, String consumerSecret, String accessToken, String accessTokenSecret,
                                int numOfThread, int numthThread, BlockingQueue<String> queue) {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerkey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setTweetModeExtended(true)
                .setIncludeEntitiesEnabled(true);

        twitter = new TwitterFactory(configurationBuilder.build()).getInstance();

        this.numOfThread = numOfThread;
        this.numthThread = numthThread;

        isRestart = false;

        userids = new HashMap<>();

        this.queue = queue;

    }


    public TwitterCrawlerThread(String consumerkey, String consumerSecret, String accessToken, String accessTokenSecret,
                                int numOfThread, int numthThread, BlockingQueue<String> queue,
                                HashMap<Long, Integer> userids) {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerkey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setTweetModeExtended(true)
                .setIncludeEntitiesEnabled(true);

        twitter = new TwitterFactory(configurationBuilder.build()).getInstance();
        this.numOfThread = numOfThread;
        this.numthThread = numthThread;

        isRestart = true;

        this.userids = userids;
        this.queue = queue;

    }

    @Override
    public void run() {
        try {

            CrawlTweets();

        } catch (Exception ex) {
            System.out.println("Thread " + numthThread + " Execute Error: " + ex.getClass() + " With messgage: " + ex.getMessage());
        }
    }

    // numOFThread: Number of threads to be created.
    // numthThread: value range (0 to numOFThread) e.g numthThread = 3, it is the 2nd threads to be created;
    private void CrawlTweets() throws Exception {

        int time = 1005;

        if (numOfThread <= numthThread) {

            throw new IllegalArgumentException("numthThread exceeds the total number of threads to be created.");

        }

        List<User> users;
        boolean restart = isRestart;

        if (!isRestart) {

            try {

                users = twitter.getFriendsList(twitter.getId(), -1, 200);

                System.out.println("Thread " + numthThread + " Friend Count: " + users.size());

                for (User user : users) {

                    if (user.getFollowersCount() > 200 && user.getStatusesCount() > 1000 && !user.isProtected() &&
                            user.getId() % numOfThread == numthThread) {

                        userids.put(user.getId(), 0);
                    }

                }

            } catch (TwitterException te) {
                System.out.println("Thread " + numthThread + " Parse Restart User Error: " + te.getClass() + " With Message: " + te.getErrorMessage());
            }

            Thread.sleep(time);

        }

        System.out.println("Thread " + numthThread + " User Count: " + userids.size());

        int count = 0;

        while (true)

        {

            if (count == 0) {

                for (Map.Entry<Long, Integer> pair : userids.entrySet()) {

                    long userid = pair.getKey();
                    int value = pair.getValue();

                    System.out.println("Thread " + numthThread + " I Count: " + count + " userid " + userid + " Value " + value);

                    if ((value % 2) == 0) {

                        for (int page = 1; page <= 5; page++) {

                            pair.setValue(value | 1);

                            try {

                                List<Status> statuses = twitter.getUserTimeline(userid, new Paging(page, 200));
                                System.out.println("Thread " + numthThread + " Statuses Count: " + statuses.size());

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

                                            System.out.println("Thread " + numthThread + " HashTag Error: " + ex.getMessage());
                                        }

                                        try {

                                            stringBuilder.append("/");

                                            for (URLEntity url : st.getURLEntities()) {

                                                stringBuilder.append(",").append(url.getDisplayURL());
                                                stringBuilder.append(",").append(url.getExpandedURL());

                                            }

                                            stringBuilder.append(";;");

                                        } catch (Exception ex) {

                                            System.out.println("Thread " + numthThread + " URL Error: " + ex.getMessage());
                                        }

                                        try {

                                            stringBuilder.append("@");

                                            for (UserMentionEntity userMentionEntity : st.getUserMentionEntities()) {

                                                stringBuilder.append(",").append(userMentionEntity.getScreenName());

                                            }

                                            //stringBuilder.append(";;");

                                        } catch (Exception ex) {

                                            System.out.println("Thread " + numthThread + " UserMention Error: " + ex.getMessage());
                                        }

                                        queue.put(stringBuilder.toString());

                                        //System.out.println(st.getUser().getName() + "------" + st.getText());

                                    }


                                } catch (Exception ex) {

                                    System.out.println("Thread " + numthThread + " Round: " + count + " failed" + ex.getMessage());

                                }

                            } catch (TwitterException te) {
                                System.out.println("Thread " + numthThread + " Timeline Error: " + te.getErrorMessage());
                                Thread.sleep(time);
                                break;
                            }

                            System.out.println("Thread " + numthThread + " counter: " + ++count);

                            Thread.sleep(time);
                        }
                    }

                    if (count >= 120) {

                        break;
                    }

                }
            }

            System.out.println("Thread " + numthThread + " Stepping to II, count: " + count);

            if (restart || count >= 120 && userids.size() < 1000000) {

                restart = false;

                for (Map.Entry<Long, Integer> pair : userids.entrySet()) {

                    long userid = pair.getKey();
                    int value = pair.getValue();

                    System.out.println("Thread " + numthThread + " II. Count: " + count + " userid " + userid + " Value " + value);

                    if ((value / 2) == 0) {

                        try {

                            users = twitter.getFriendsList(userid, -1, 200);
                            users.addAll(twitter.getFollowersList(userid, -1, 200));
                            pair.setValue(value | 2);

                            System.out.println("Thread " + numthThread + " Friend Follower Count: " + users.size());

                            for (User user : users) {

                                if (!userids.containsKey(user.getId()) && !user.isProtected() && user.getFollowersCount() > 200
                                        && user.getStatusesCount() >= 1000 && user.getId() % numOfThread == numthThread) {

                                    userids.put(user.getId(), 0);

                                }

                            }
                        } catch (TwitterException te) {

                            System.out.println("Thread " + numthThread + " User Error: " + te.getErrorMessage());
                            //userids.remove(userid);

                        }

                        System.out.println("Thread " + numthThread + " User Count: " + userids.size());

                        Thread.sleep(time);
                        count = 0;
                        break;

                    }
                }

            } else if (count < 120) {

                if (userids.size() >= 1000000)
                    break;

                count++;
                Thread.sleep(time);
            }

        }
    }

    public static void ParseRestartFile(String restartFile, Map<Long, Integer> userids) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(restartFile));
            String line;

            while ((line = br.readLine()) != null) {

                String[] wordsarray = line.split("\t");

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

        } catch (Exception ex) {

            System.out.println("Reading Restart File Error: " + ex.getMessage());
        }
    }
}
