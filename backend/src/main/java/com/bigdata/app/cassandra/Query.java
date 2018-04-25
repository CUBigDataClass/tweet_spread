package com.bigdata.app.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Query {

    public static void main(String args[]) {

        //query
        String query = "update tweetanalysis.tweetSentiments set sentiment = 10 where tweet = '@Andersson_123 @Niklas14_ Skrev jag det? Är du Cathy Newman? Börjar bli lite misstänkt likt henne här.';"
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
