package com.bigdata.app.topicmodel.test;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class TweetsBolt extends BaseRichBolt implements Serializable {

    private OutputCollector collector;
    private final int NUM_TWEET = 500;
    private int countTweet = 0;
    private List<String> tweets = new ArrayList<String>();

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple input) {
        try {
            String hashtag = (String) input.getValueByField("hashtag");
            System.out.println("[bigdata] get hashtag from json bolt: " + hashtag);
            String text = (String) input.getValueByField("text");
            String lang = (String) input.getValueByField("lang");
            if (lang.equals("en")) {
                tweets.add(text);
                countTweet++;
                if (countTweet == NUM_TWEET) {
                    collector.emit(new Values(hashtag, tweets));
                    System.out.println("[bigdata] emit hashtag: " + hashtag);
                    countTweet = 0;
                }
            }
            collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag", "tweets"));
    }

}
