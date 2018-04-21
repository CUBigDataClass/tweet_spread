package com.bigdata.app.topology;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;
import static org.apache.storm.cassandra.DynamicStatementBuilder.*;
import org.apache.storm.cassandra.bolt.CassandraWriterBolt;

import com.bigdata.app.bolt.JSONParsingBolt;
import com.bigdata.app.sentiments.SentimentBolt;

import com.hmsonline.storm.cassandra.StormCassandraConstants;
import com.hmsonline.storm.cassandra.bolt.AckStrategy;
import com.hmsonline.storm.cassandra.bolt.CassandraBatchingBolt;
import com.hmsonline.storm.cassandra.bolt.mapper.DefaultTupleMapper;
//import com.hmsonline.storm.cassandra.bolt.CassandraWriterBolt;

import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StormCassandraTopology {

    private static final String CASSANDRA_KEYSPACE = "tweetanalysis";
    private static final String CASSANDRA_COLUMN_FAMILY = "tweetSentiments";
    private static final String CASSANDRA_ROWKEY_FIELD = "tweet";

    public static void main(String[] args) {
        // zookeeper hosts for the Kafka cluster
        BrokerHosts zkHosts = new ZkHosts("54.245.62.87:2181");

        // Create the KafkaSpout configuartion
        // Second argument is the topic name
        // Third argument is the zookeeper root for Kafka
        // Fourth argument is consumer group id
        SpoutConfig kafkaConfig = new SpoutConfig(zkHosts, "twitterData", "",
                "id");

        // Specify that the kafka messages are String
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

        // We want to consume all the first messages in the topic every time
        // we run the topology to help in debugging. In production, this
        // property should be false
        kafkaConfig.startOffsetTime = kafka.api.OffsetRequest
                .EarliestTime();

        // Create storm topology
        TopologyBuilder builder = new TopologyBuilder();

        // Set kafka spout
        builder.setSpout("KafkaSpout", new KafkaSpout(kafkaConfig), 1);

//        Config config = new Config();
//        String configKey = "cassandra-config";
//        HashMap<String, Object> clientConfig = new HashMap<String, Object>();
//        clientConfig.put(StormCassandraConstants.CASSANDRA_HOST, "172.31.21.76:9160");
////        clientConfig.put(StormCassandraConstants.CASSANDRA_PORT, "9160");
//        clientConfig.put(StormCassandraConstants.CASSANDRA_KEYSPACE, Arrays.asList(new String[]{"tweetanalysis"}));
//        config.put(configKey, clientConfig);

        // TODO: Create cassandra bot
        // Create a CassandraBolt that writes to the column family and use Tuple field as the row key
//        CassandraBatchingBolt<String, String, String> cassandraBolt = new CassandraBatchingBolt<String, String, String>(configKey,
//                new DefaultTupleMapper(CASSANDRA_KEYSPACE, CASSANDRA_COLUMN_FAMILY, CASSANDRA_ROWKEY_FIELD));
//        cassandraBolt.setAckStrategy(AckStrategy.ACK_ON_WRITE);
        //Load properties file
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

        //Copy properies to storm config
        for (String name : props.stringPropertyNames()) {
            conf.put(name, props.getProperty(name));
        }

        conf.setMaxTaskParallelism(Runtime.getRuntime().availableProcessors());
        conf.setDebug(false);
        String cql = "INSERT INTO tweetSentiments (tweet, sentiment) values(?, ?);";
        CassandraWriterBolt cassandraBolt = new CassandraWriterBolt(async(
                simpleQuery(cql).with(fields("tweet", "sentiment"))));


        builder.setBolt("json", new JSONParsingBolt()).shuffleGrouping("KafkaSpout");


        builder.setBolt("sentiment", new SentimentBolt("/home/ec2-user/tweet_spread/backend/src/main/resources/AFINN-111.txt")).shuffleGrouping("json", "stream2");

        // TODO: Create cassandra bot
        builder.setBolt("cassandra-bolt", cassandraBolt, 3).shuffleGrouping("sentiment");

        // Submit topology for execution
        try {
            StormSubmitter.submitTopology("StormCassandraToplogy", conf, builder.createTopology());
        } catch (AlreadyAliveException alreadyAliveException) {
            System.out.println(alreadyAliveException);
        } catch (InvalidTopologyException invalidTopologyException) {
            System.out.println(invalidTopologyException);
        } catch (AuthorizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
