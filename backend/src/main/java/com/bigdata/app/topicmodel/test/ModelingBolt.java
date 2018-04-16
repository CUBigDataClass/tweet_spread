package com.bigdata.app.topicmodel.test;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.codehaus.jackson.map.ObjectMapper;

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
        File stopWords = new File(classLoader.getResource("en.txt").getFile());
    }

    public void execute(Tuple input) {
        try {
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

            // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
            //  Note that the first parameter is passed as the sum over topics, while
            //  the second is the parameter for a single dimension of the Dirichlet prior.
            int numTopics = 100;
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

            FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
            LabelSequence topics = model.getData().get(0).topicSequence;

            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            for (int position = 0; position < tokens.getLength(); position++) {
                out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
            }
            System.out.println("out is: " + out);

            // Estimate the topic distribution of the first instance,
            //  given the current Gibbs state.
            double[] topicDistribution = model.getTopicProbabilities(0);

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();


            // Show top 5 words in topics with proportions for the first document
            for (int topic = 0; topic < numTopics; topic++) {
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

                out = new Formatter(new StringBuilder(), Locale.US);
                out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
                int rank = 0;
                while (iterator.hasNext() && rank < 5) {
                    IDSorter idCountPair = iterator.next();
                    out.format("%s (%.0f)  ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                    rank++;
                }
                System.out.println(out);
            }

            // Create a new instance with high probability of topic 0
            StringBuilder topicZeroText = new StringBuilder();
            Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
                rank++;
            }

            // Create a new instance named "test instance" with empty target and source fields.
            InstanceList testing = new InstanceList(instances.getPipe());
            testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));
            System.out.println(testing.getAlphabets());
            TopicInferencer inferencer = model.getInferencer();
            double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
            System.out.println("Final topic is:");
            System.out.println("0\t" + testProbabilities[0]);
            this.collector.ack(input);
        } catch (Exception exception) {
            exception.printStackTrace();
            this.collector.fail(input);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("topic-stream", new Fields("tweets"));
    }

    private void save(String fileName, List<String> list) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
        for (int i = 0; i < list.size(); i++) {
            pw.println(i + "\tX\t" + list.get(i));
        }
        pw.close();
    }

}
