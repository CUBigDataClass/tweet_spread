package com.bigdata.app.bolt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.lang.Float;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import org.json.simple.JSONObject;

/**
 * Breaks each tweet into words and gets the location of each tweet and
 * assocaites its value to hashtag
 *
 * @author - centos
 */
public final class GeoParsingBolt extends BaseRichBolt {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeoParsingBolt.class);
    private static final long serialVersionUID = -5094673458112835122L;
    private OutputCollector collector;
    private String path;

    public GeoParsingBolt() {
        LOGGER.info("geoparsing bolt is initialized...........");
    }

    private Map<String, Integer> afinnSentimentMap = new HashMap<String, Integer>();

    public final void prepare(final Map map,
                              final TopologyContext topologyContext,
                              final OutputCollector collector) {
        this.collector = collector;
        // Bolt will read the AFINN Sentiment file [which is in the classpath]
        // and stores the key, value pairs to a Map.

    }

    public final void declareOutputFields(
            final OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("longitude", "latitude", "hashtag"));
    }

    public final void execute(final Tuple input) {
        try {
            String hashtag = (String) input.getValueByField("hashtag");
            Boolean geo_enabled = (Boolean) input.getValueByField("geo_enabled");
            Collection<Float> location = new ArrayList<Float>();
            if (geo_enabled != null || geo_enabled != false) {
                if (input.getValueByField("geo") != null) {
                    JSONObject geo = (JSONObject) input.getValueByField("geo");
                    if (geo != null) {
                        Collection<Float> loc = (Collection) geo.get("coordinates");
                        if ((String) geo.get("type") == "point") {
                            collector.emit(new Values(((Float[]) loc.toArray())[0], ((Float[]) loc.toArray())[1], hashtag));
                            LOGGER.info("..... geo is " + geo);
                        }
                    }
                }
                LOGGER.info("........... geo is null.............");
            }

            this.collector.ack(input);
        } catch (Exception exception) {
            LOGGER.info("............... collector is null............");
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

}
