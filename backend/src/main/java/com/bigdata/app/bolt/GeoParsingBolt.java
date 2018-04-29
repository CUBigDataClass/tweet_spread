package com.bigdata.app.bolt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.json.*;

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
            Collection<Float> location = new ArrayList<Float>();
            if (input.getValueByField("user") != null) {
                JsonObject user = (JsonObject) input.getValueByField("user");
                if (user.getJsonObject("derived") != null) {
                    JsonObject derived = (JsonObject) user.getJsonObject("derived");
                    if (derived.getJsonArray("locations") != null) {
                        JsonArray locations = (JsonArray) derived.getJsonArray("locations");
                        for (int i = 0; i < locations.size(); i++) {
                            JsonObject o = locations.getJsonObject(i);
                            if (o.getJsonObject("geo") != null) {
                                JsonObject geo = (JsonObject) o.getJsonObject("geo");
                                if (geo.get("coordinates") != null) {
                                    Collection<Float> loc = (Collection) geo.get("coordinates");
                                    collector.emit(new Values(((Float[]) loc.toArray())[0], ((Float[]) loc.toArray())[1], hashtag));
                                }
                            }
                        }
                    }
                }
            }

            LOGGER.info("........... geo is null.............");
            this.collector.ack(input);
        } catch (Exception exception) {
            LOGGER.info("............... collector is null............");
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

}
