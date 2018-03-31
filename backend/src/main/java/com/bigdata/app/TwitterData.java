package com.bigdata.app;

import java.util.Properties;

import kafka.producer.ProducerConfig;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class TwitterData {

    static String consumerKeyStr = "gFLDIKaiJeYbJ6j8j2EeeedWh";
    static String consumerSecretStr = "H0NDFdIlR5nwmf4K6y7u9SrZJcSHBWPuQAj8g4y1HvA4isaXMd";
    static String accessTokenStr = "852401867715862528-cNabl5QL9HE4aeYJRRYKVgJvOgsS1V6";
    static String accessTokenSecretStr = "rCYGtko2E3l40PwErsiL0AcAopQf2LJBoTda2RPvcEhWn";

    public void start() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKeyStr);
        cb.setOAuthConsumerSecret(consumerSecretStr);
        cb.setOAuthAccessToken(accessTokenStr);
        cb.setOAuthAccessTokenSecret(accessTokenSecretStr);
        cb.setJSONStoreEnabled(true);
        cb.setIncludeEntitiesEnabled(true);

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();

        final Producer<String, String> producer = new KafkaProducer<String, String>(
                getProducerConfig());

        try {
            Query query = new Query("gnip");
            QueryResult result;
            do {
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                for (Status tweet : tweets) {
                    System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                    ProducerRecord<String, String> data = new ProducerRecord<String, String>(
                        "twitterData", DataObjectFactory.getRawJSON(tweet));
                    // Send the data to kafka
                    producer.send(data);
                }
            } while ((query = result.nextQuery()) != null);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
        }
    }

    private Properties getProducerConfig() {

        Properties props = new Properties();

        // List of kafka borkers. Complete list of brokers is not required as
        // the producer will auto discover the rest of the brokers.
        props.put("bootstrap.servers", "localhost:9092");
        props.put("batch.size", 1);
        // Serializer used for sending data to kafka. Since we are sending
        // string,
        // we are using StringSerializer.
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("producer.type", "sync");

        return props;

    }

    public static void main(String[] args) throws InterruptedException {
        new TwitterData().start();
    }

}
