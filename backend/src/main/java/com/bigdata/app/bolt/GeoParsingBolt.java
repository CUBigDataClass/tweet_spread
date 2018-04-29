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
//        try {
//            String hashtag = (String) input.getValueByField("hashtag");
//            Collection<Float> location = new ArrayList<Float>();
//            if (input.getValueByField("user") != null) {
//                JSONObject user = (JSONObject) input.getValueByField("user");
//                if (user.get("derived") != null) {
//                    JSONObject derived = (JSONObject) user.get("derived");
//                    System.out.println("derived ... "+user.get("derived"));
//                    if (derived.get("locations") != null) {
//                        JSONArray locations = (JSONArray) derived.get("locations");
//                        System.out.println("locations ... "+derived.get("locations"));
//                        for (int i = 0; i < locations.size(); i++) {
//                            JSONObject o = (JSONObject) locations.get(i);
//                            if (o.get("geo") != null) {
//                                JSONObject geo = (JSONObject) o.get("geo");
//                                System.out.println("geo ... "+o.get("geo"));
//                                if (geo.get("coordinates") != null) {
//                                    Collection<Float> loc = (Collection) geo.get("coordinates");
//                                    System.out.println("coordinates ... "+geo.get("coordinates"));
//                                    collector.emit(new Values(((Float[]) loc.toArray())[0], ((Float[]) loc.toArray())[1], hashtag));
//                                }
//                            }
//                        }
//                    }
//                }
//            }

            try {
                String hashtag = (String) input.getValueByField("hashtag");
                Collection<Float> location = new ArrayList<Float>();
                if (input.getValueByField("user") != null) {
                    LinkedHashMap user = (LinkedHashMap) input.getValueByField("user");
                    if (user.get("derived") != null) {
                        LinkedHashMap derived = (LinkedHashMap) user.get("derived");
                        if (derived.get("locations") != null) {
                            List<LinkedHashMap> locations = (List<LinkedHashMap>) derived.get("locations");
                            for (int i = 0; i < locations.size(); i++) {
                                LinkedHashMap o = (LinkedHashMap)locations.get(i);
                                if (o.get("geo") != null) {
                                    LinkedHashMap geo = (LinkedHashMap) o.get("geo");
                                    if (geo.get("coordinates") != null) {
                                        ArrayList<Object> loc = (ArrayList<Object>) geo.get("coordinates");
                                        Double lat = (Double)(loc.get(0));
                                        Double lon = (Double)(loc.get(1));
                                        String str = "{\"" + hashtag + "\": {latitude: " + String.valueOf(lat) + ", longitude: " +
                                                String.valueOf(lon) + "}";
                                        ArrayList<String> s = new ArrayList<String>();
                                        s.add(str);
                                        System.out.println("...... geo parse " + str);
                                        collector.emit(new Values(s, hashtag));
                                    }
                                    System.out.println("...... coordinates is null .......");
                                }
                                System.out.println("...... geo is null .......");
                            }
                            System.out.println("...... location is null .......");
                        }
                        System.out.println("...... locations is null .......");
                    }
                    System.out.println("...... derived is null .......");
                }

            LOGGER.info("........... geo is null final .............");
            this.collector.ack(input);
        } catch (Exception exception) {
            LOGGER.info("............... collector is null............");
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

}
