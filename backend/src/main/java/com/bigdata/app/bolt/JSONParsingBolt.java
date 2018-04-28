package com.bigdata.app.bolt;

import java.io.Serializable;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONParsingBolt extends BaseRichBolt implements Serializable {

    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;

    }

    public void execute(Tuple input) {
        try {
            Map<String, String> inputMap = (Map<String, String>) input.getValue(0);
            String key = (String) inputMap.keySet().toArray()[0];
            String tweet = inputMap.get(key);
            Map<String, Object> map = new ObjectMapper().readValue(tweet, Map.class);
            collector.emit(new Values(key, map.get("text"), map.get("lang"),
                    map.get("geo_enabled"), map.get("geo")));
            this.collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("hashtag", "text", "lang", "geo_enabled", "geo"));
    }

}
