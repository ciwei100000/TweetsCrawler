import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

public class Limit {

        public static void main(String[] args) throws Exception {
            ConfigurationBuilder cf = new ConfigurationBuilder();

            cf.setDebugEnabled(true)
                    .setOAuthConsumerKey("1fehb8awFzhHnqrA60131abfU")
                    .setOAuthConsumerSecret("L1KVo62xOYAVZZhx9kD4tGVnZugLfbJLbbmcYJBt6g9XYjjvfi")
                    .setOAuthAccessToken("636879179-yvnBY1rIzsQa5WqhtekktwPkI28QTL2kP1tUPGzj")
                    .setOAuthAccessTokenSecret("dcx7IBY2j1qUXmGtismxafSFwTNWbCvdyjnvQ9GetufRP")
                    .setTweetModeExtended(true);

            TwitterFactory tf = new TwitterFactory(cf.build());

            Twitter twitter = tf.getInstance();


            Map<String, RateLimitStatus> map = twitter.getRateLimitStatus("friends,statuses");

            int size = map.entrySet().toArray().length;

            for (int i = 0; i<size;i++){
                System.out.println(map.entrySet().toArray()[i]);
            }
        }
}
