package com.bigdata.app.topicmodel;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class ModelingBolt extends BaseRichBolt implements Serializable {

    private OutputCollector collector;
    private File stopWords;
    private static final String tweetFilename = "tweets.txt";

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
        stopWords = new File("/home/ec2-user/tweet_spread/backend/src/main/resources/en.txt");
    }

    public void execute(Tuple input) {
        try {
            String hashtag = (String) input.getValueByField("hashtag");
            System.out.println("[bigdata] get hashtag from tweets bolt: " + hashtag);
            // Begin by importing documents from text to feature sequences
            ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
            List<String> tweets = (List<String>) input.getValueByField("tweets");
            save(tweetFilename, tweets);

            // Pipes: lowercase, tokenize, remove stopwords, map to features
            pipeList.add( new CharSequenceLowercase() );
            pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
            pipeList.add( new TokenSequenceRemoveStopwords(stopWords, "UTF-8", false, false, false) );
            pipeList.add( new TokenSequence2FeatureSequence() );

            InstanceList instances = new InstanceList (new SerialPipes(pipeList));

            Reader fileReader = new InputStreamReader(new FileInputStream(tweetFilename), "UTF-8");
            instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                    3, 2, 1)); // data, label, name fields

            // Create a model with 10 topics, alpha_t = 0.01, beta_w = 0.01
            //  Note that the first parameter is passed as the sum over topics, while
            //  the second is the parameter for a single dimension of the Dirichlet prior.
            int numTopics = 10;
            ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

            model.addInstances(instances);

            // Use two parallel samplers, which each look at one half the corpus and combine
            //  statistics after every iteration.
            model.setNumThreads(2);

            // Run the model for 50 iterations and stop (this is for testing only,
            //  for real applications, use 1000 to 2000 iterations)
            model.setNumIterations(50);
            model.estimate();

            // The data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

            // Get top 20 words in topics with proportions for the first document
            int topicsToDisplay = 20;
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (int topic = 0; topic < numTopics; topic++) {
                builder.append("\"t").append(topic + 1).append("\":[");
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

                int rank = 0;
                while (iterator.hasNext() && rank < topicsToDisplay) {
                    IDSorter idCountPair = iterator.next();
                    builder.append("{\"text\":\"").append(dataAlphabet.lookupObject(idCountPair.getID())).append("\",");
                    builder.append("\"weight\":").append((int)idCountPair.getWeight()).append("}");
                    if (rank != topicsToDisplay - 1) {
                        builder.append(",");
                    }
                    rank++;
                }
                builder.append("]");
                if (topic != numTopics - 1) {
                    builder.append(",");
                }
            }
            builder.append("}");
            System.out.println("[bigdata] topic hashtag: " + hashtag);
            collector.emit(new Values(builder.toString(), hashtag));
            collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("topic", "hashtag"));
    }

    private void save(String fileName, List<String> list) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
        for (int i = 0; i < list.size(); i++) {
            pw.println(i + "\tX\t" + list.get(i));
        }
        pw.close();
    }

}
