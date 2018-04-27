package com.bigdata.app.topology;

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

import com.bigdata.app.bolt.JSONParsingBolt;
import com.bigdata.app.sentiments.SentimentBolt;

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
        SpoutConfig kafkaConfig = new SpoutConfig(zkHosts, "twitterData", "",
                "id");

        // Specify that the kafka messages are String
        kafkaConfig.scheme = new KeyValueSchemeAsMultiScheme(new StringKeyValueScheme());

        // We want to consume all the first messages in the topic every time
        // we run the topology to help in debugging. In production, this
        // property should be false
        kafkaConfig.startOffsetTime = kafka.api.OffsetRequest
                .EarliestTime();


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

//        CQLStatementTupleMapper get = simpleQuery("SELECT state FROM words_ks.words_table WHERE word = ?")
//                .with(fields("word"))
//                .build();
//
//        CQLStatementTupleMapper put = simpleQuery("INSERT INTO words_ks.words_table (word, state) VALUES (?, ?)")
//                .with(fields("word", "state"))
//                .build();
//
//        CassandraBackingMap.Options<Integer> mapStateOptions = new CassandraBackingMap.Options<Integer>(new CassandraContext())
//                .withBatching(BatchStatement.Type.UNLOGGED)
//                .withKeys(new Fields("word"))
//                .withNonTransactionalJSONBinaryState("state")
//                .withMultiGetCQLStatementMapper(get)
//                .withMultiPutCQLStatementMapper(put);
//
//        CassandraMapStateFactory factory = CassandraMapStateFactory.nonTransactional(mapStateOptions)
//                .withCache(0);

        String cql = "INSERT INTO tweetSentiments (tweet, sentiment) values(?, ?);";
        CassandraWriterBolt cassandraBolt = new CassandraWriterBolt(async(
                simpleQuery(cql).with(fields("hashtag", "negative_sentiment", "neutral_sentiment", "positive_sentiment"))));

        // Create JSON parser bolt
        builder.setBolt("json", new JSONParsingBolt()).shuffleGrouping("KafkaSpout");

        // Create sentiment analysis bolt
        builder.setBolt("sentiment", new SentimentBolt("/home/ec2-user/tweet_spread/backend/src/main/resources/AFINN-111.txt")).shuffleGrouping("json");

        // Create Cassandra writer bolt
        builder.setBolt("cassandra-bolt", cassandraBolt, 3).shuffleGrouping("sentiment");

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
