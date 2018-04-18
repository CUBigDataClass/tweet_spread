package com.bigdata.app.topology;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.TopologyBuilder;

import com.bigdata.app.bolt.JSONParsingBolt;
import com.bigdata.app.sentiments.SentimentBolt;

import com.hmsonline.storm.cassandra.StormCassandraConstants;
import com.hmsonline.storm.cassandra.bolt.AckStrategy;
import com.hmsonline.storm.cassandra.bolt.CassandraBatchingBolt;
import com.hmsonline.storm.cassandra.bolt.mapper.DefaultTupleMapper;

import java.util.Arrays;
import java.util.HashMap;

public class StormCassandraTopology {

    private static final String CASSANDRA_KEYSPACE = "tweetanalysis";
    private static final String CASSANDRA_COLUMN_FAMILY = "tweetSenitiments";
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

        Config config = new Config();
        String configKey = "cassandra-config";
        HashMap<String, Object> clientConfig = new HashMap<String, Object>();
        clientConfig.put(StormCassandraConstants.CASSANDRA_HOST, "172.31.21.76");
        clientConfig.put(StormCassandraConstants.CASSANDRA_PORT, "9160");
        clientConfig.put(StormCassandraConstants.CASSANDRA_KEYSPACE, Arrays.asList(new String[]{"tweetanalysis"}));
        config.put(configKey, clientConfig);

        // TODO: Create cassandra bot
        // Create a CassandraBolt that writes to the column family and use Tuple field as the row key
        CassandraBatchingBolt<String, String, String> cassandraBolt = new CassandraBatchingBolt<String, String, String>(configKey,
                new DefaultTupleMapper(CASSANDRA_KEYSPACE, CASSANDRA_COLUMN_FAMILY, CASSANDRA_ROWKEY_FIELD));
        cassandraBolt.setAckStrategy(AckStrategy.ACK_ON_WRITE);

        builder.setBolt("json", new JSONParsingBolt()).shuffleGrouping("KafkaSpout");


        builder.setBolt("sentiment", new SentimentBolt("AFINN-111.txt")).shuffleGrouping("json", "stream2");

        // TODO: Create cassandra bot
        builder.setBolt("cassandra-bolt", cassandraBolt, 3).shuffleGrouping("sentiment");

        // create an instance of LocalCluster class for executing topology in
        // local mode.
        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();

        // Submit topology for execution
        cluster.submitTopology("KafkaToplogy", config, builder.createTopology());

        try {
            // Wait for some time before exiting
            System.out.println("Waiting to consume from kafka");
            Thread.sleep(6000000);
        } catch (Exception exception) {
            System.out.println("Thread interrupted exception : " + exception);
        }

        // kill the KafkaTopology
        cluster.killTopology("KafkaToplogy");

        // shut down the storm test cluster
        cluster.shutdown();

    }
}
