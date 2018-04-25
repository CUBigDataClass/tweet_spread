package com.bigdata.app.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Query {

    public static void main(String args[]) {

        //query
        String query = "insert into tweetanalysis.sentiments (hashtag, positive_sentiment, negative_sentiment, neutral_sentiment)"+
                " values ('hash1', 1, 0, 0) if not exists;"+
                "update tweetanalysis.sentiments set positive_sentiment = positive_sentiment + 1, "+
                "negative_sentiment = negative_sentiment + 2, neutral_sentiment = neutral_sentiment + 3 "+
                "where hashtag = 'hash1' if exists;" +
                "update tweetanalysis.sentiments set positive_sentiment = positive_sentiment + 1, "+
                "negative_sentiment = negative_sentiment + 2, neutral_sentiment = neutral_sentiment + 3 "+
                "where hashtag = 'hash2' if exists;"
        ;

        //Creating Cluster object
        Cluster cluster = Cluster.builder().addContactPoint("54.245.62.87").build();

        //Creating Session object
        Session session = cluster.connect("tweetanalysis");

        //Executing the query
        session.execute(query);

        System.out.println("Changes done");
    }
}
