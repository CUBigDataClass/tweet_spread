package com.bigdata.app.topology;

import com.bigdata.app.topicmodel.ModelingBolt;
import com.bigdata.app.topicmodel.TweetsBolt;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringKeyValueScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.KeyValueSchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import static org.apache.storm.cassandra.DynamicStatementBuilder.*;
import org.apache.storm.cassandra.bolt.CassandraWriterBolt;
import com.bigdata.app.bolt.MilestonesBolt;

import com.bigdata.app.bolt.JSONParsingBolt;
import com.bigdata.app.sentiments.SentimentBolt;
import com.bigdata.app.bolt.GeoParsingBolt;
import org.apache.storm.tuple.Fields;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StormCassandraTopology {

    public static void main(String[] args) {
        // zookeeper hosts for the Kafka cluster
        BrokerHosts zkHosts = new ZkHosts("54.245.62.87:2181");

        // Create KafkaSpout configuration
        // Second argument is the topic name
        // Third argument is the zookeeper root for Kafka
        // Fourth argument is consumer group id
        SpoutConfig kafkaConfig = new SpoutConfig(zkHosts, "tweet", "",
                "id");

        // Specify that the kafka messages are String
        kafkaConfig.scheme = new KeyValueSchemeAsMultiScheme(new StringKeyValueScheme());

        // We want to consume all the first messages in the topic every time
        // we run the topology to help in debugging. In production, this
        // property should be false
        kafkaConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();

        // Create storm topology
        TopologyBuilder builder = new TopologyBuilder();

        // Set kafka spout
        builder.setSpout("KafkaSpout", new KafkaSpout(kafkaConfig), 1);

        // Load properties file
        Properties props = new Properties();
        try {
            InputStream is = StormCassandraTopology.class.getClassLoader().getResourceAsStream("cassandra.properties");

            if (is == null)
                throw new RuntimeException("Classpath missing cassandra.properties file");

            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Config conf = new Config();
        conf.setDebug(true);
        // Copy properties to storm config
        for (String name : props.stringPropertyNames()) {
            conf.put(name, props.getProperty(name));
        }
        conf.setMaxTaskParallelism(Runtime.getRuntime().availableProcessors());
        conf.setDebug(false);

        /**
         * LEVEL 1
         **/

        // Create JSON parser bolt
        builder.setBolt("json", new JSONParsingBolt(), 3).shuffleGrouping("KafkaSpout");

        /**
         * LEVEL 2
         **/
        // Create sentiment analysis bolt
        builder.setBolt("sentiment", new SentimentBolt("/home/ec2-user/tweet_spread/backend/src/main/resources/AFINN-111.txt"), 3).shuffleGrouping("json");

        // Create geo parsing bolt
        builder.setBolt("geoparsing", new GeoParsingBolt(), 3).shuffleGrouping("json");

        // Create topic modeling bolt
        builder.setBolt("tweets", new TweetsBolt(), 3).fieldsGrouping("json", new Fields("hashtag"));
        builder.setBolt("topic-modeling", new ModelingBolt(), 6).shuffleGrouping("tweets");

        // Create milestones bolt
        builder.setBolt("milestones", new MilestonesBolt(), 3).shuffleGrouping("json");

        /**
         * LEVEL 3
         **/
        // Create Cassandra writer bolt
        // order of the outputs from previous bolt should be positive_sentiments, negative_sentiments,
        // neutral_sentiments, hashtag
        String query = "update sentiments set positive_sentiments = positive_sentiments + ?, negative_sentiments = negative_sentiments + ?, neutral_sentiments = neutral_sentiments + ? where hashtag = ?;";
        CassandraWriterBolt cassandraSentimentBolt = new CassandraWriterBolt(async(
                simpleQuery(query).with(fields("positive_sentiments", "negative_sentiments", "neutral_sentiments", "hashtag"))));
        builder.setBolt("cassandraSentimentBolt", cassandraSentimentBolt, 3).shuffleGrouping("sentiment");

        // create cassandra bolt for geo parsing
        String query1 = "update geoparsing set locations = locations + ? where hashtag = ?;";
        CassandraWriterBolt cassandraGeoParsingBolt = new CassandraWriterBolt(async(
                simpleQuery(query1).with(fields("locations", "hashtag"))));
        builder.setBolt("cassandraGeoParsingBolt", cassandraGeoParsingBolt, 3).shuffleGrouping("geoparsing");

        // create cassandra bolt for topic modeling
        String query2 = "update topicmodeling set topic = ? where hashtag = ?;";
        CassandraWriterBolt cassandraTopicBolt = new CassandraWriterBolt(async(
                simpleQuery(query2).with(fields("topic", "hashtag"))));
        builder.setBolt("cassandraTopicBolt", cassandraTopicBolt, 3).shuffleGrouping("topic-modeling");

        // create cassandra bolt for milestones
        String query3 = "update hashtag_milestones set count = count + ? where hour = ? and day = ? and month = ? and year = ? and hashtag = ?;";
        CassandraWriterBolt cassandraMilestonesBolt = new CassandraWriterBolt(async(
                simpleQuery(query3).with(fields("count", "hour", "day", "month", "year", "hashtag"))));
        builder.setBolt("cassandraMilestonesBolt", cassandraMilestonesBolt, 3).shuffleGrouping("milestones");

        // Submit topology for execution
        try {
            StormSubmitter.submitTopology("StormCassandraTopology", conf, builder.createTopology());
        } catch (AlreadyAliveException alreadyAliveException) {
            System.out.println(alreadyAliveException);
        } catch (InvalidTopologyException invalidTopologyException) {
            System.out.println(invalidTopologyException);
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
    }
}
