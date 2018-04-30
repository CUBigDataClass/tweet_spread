package com.bigdata.app.bolt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.*;
import java.lang.Double;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import com.google.common.base.Splitter;
import javax.json.*;

/**
 * Breaks each tweet into words and gets the location of each tweet and
 * assocaites its value to hashtag
 *
 * @author - centos
 */
public final class MilestonesBolt extends BaseRichBolt {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeoParsingBolt.class);
    private static final long serialVersionUID = -5094673458112835122L;
    private OutputCollector collector;
    private String path;

    public MilestonesBolt() {
        LOGGER.info("milestones bolt is initialized...........");
    }

    private Map<String, Integer> afinnSentimentMap = new HashMap<String, Integer>();

    public final void prepare(final Map map,
                              final TopologyContext topologyContext,
                              final OutputCollector collector) {
        this.collector = collector;

    }

    public final void declareOutputFields(
            final OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("count", "day", "month", "year", "hashtag"));
    }

    public final void execute(final Tuple input) {

        try {
            String hashtag = (String) input.getValueByField("hashtag");
            String date = (String) input.getValueByField("created_at");
            System.out.println("..... date .... " + date);

            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            String mo = date.substring(4, 3);

            int month = Arrays.asList(months).indexOf(mo);
            int day = (int) Integer.parseInt(date.substring(8, 2));
            int year = (int) Integer.parseInt(date.substring(26, 4));

            System.out.println("..... date, month, year .... " + day + month + year);
            collector.emit(new Values(1, day, month, year, hashtag));

            this.collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

}
