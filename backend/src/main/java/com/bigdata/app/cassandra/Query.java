package com.bigdata.app.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import java.util.Date;

public class Query {

    public static void main(String args[]) {

        String date= "Sat Apr 28 18:20:57 +0000 2018";
        Date day = Date(date);
        System.out.println(day);

//        //query
//        String query = "update tweetanalysis.sentiments set positive_sentiments = positive_sentiments + 1, "+
//                "negative_sentiments = negative_sentiments + 2, neutral_sentiments = neutral_sentiments + 3 "+
//                "where hashtag = 'hash1';"
//        ;
//
//        //Creating Cluster object
//        Cluster cluster = Cluster.builder().addContactPoint("54.245.62.87").build();
//
//        //Creating Session object
//        Session session = cluster.connect("tweetanalysis");
//
//        //Executing the query
//        session.execute(query);
//
//        System.out.println("Changes done");
    }
}
