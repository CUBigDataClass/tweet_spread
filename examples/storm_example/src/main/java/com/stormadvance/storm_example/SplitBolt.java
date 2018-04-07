package com.stormadvance.storm_example;

import org.apache.storm.task.ShellBolt;
import org.apache.storm.topology.*;
import org.apache.storm.tuple.Fields;

import java.util.*;

public class SplitBolt extends ShellBolt implements IRichBolt {
    public SplitBolt() {
        super("python", "/home/ec2-user/tweet_spread/examples/storm_example/src/main/java/com/stormadvance/storm_example/process.py");
	}

 	@Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }

        @Override
        public Map<String, Object> getComponentConfiguration() {
            return null;
        }
}
