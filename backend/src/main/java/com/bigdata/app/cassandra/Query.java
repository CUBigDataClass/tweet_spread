package com.bigdata.app.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Query {

    public static void main(String args[]) {

        //query
        String query = " BEGIN BATCH INSERT INTO tweet_sentiments (tweet, sentiment) values( 'I hate Trump','Neg');"

                + "UPDATE tweet_sentiments SET sentiment = 'Neg' WHERE tweet = 'I hate Trump';" + "APPLY BATCH;";

        //Creating Cluster object
        Cluster cluster = Cluster.builder().addContactPoint("54.245.62.87").build();

        //Creating Session object
        Session session = cluster.connect("tweetanalysis");

        //Executing the query
        session.execute(query);

        System.out.println("Changes done");
    }
}
