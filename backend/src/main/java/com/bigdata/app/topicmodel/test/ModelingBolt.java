package com.bigdata.app.topicmodel.test;

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
        ClassLoader classLoader = ModelingBolt.class.getClassLoader();
        stopWords = new File(classLoader.getResource("en.txt").getFile());
    }

    public void execute(Tuple input) {
        try {
            String hashtag = (String) input.getValueByField("hashtag");
            // Begin by importing documents from text to feature sequences
            ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
            List<String> tweets = (List<String>) input.getValue(0);
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

            // Show the words and topics in the first instance

            // The data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();

            // Estimate the topic distribution of the first instance,
            //  given the current Gibbs state.
            double[] topicDistribution = model.getTopicProbabilities(0);

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

            // Get top 10 words in topics with proportions for the first document
            int topicsToDisplay = 10;
            for (int topic = 0; topic < numTopics; topic++) {
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

                Formatter out = new Formatter(new StringBuilder(), Locale.US);
                out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
                int rank = 0;
                while (iterator.hasNext() && rank < topicsToDisplay) {
                    IDSorter idCountPair = iterator.next();
                    out.format("%s (%.0f)  ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                    rank++;
                }
            }
            this.collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tweets", "hashtag"));
    }

    private void save(String fileName, List<String> list) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
        for (int i = 0; i < list.size(); i++) {
            pw.println(i + "\tX\t" + list.get(i));
        }
        pw.close();
    }

}
