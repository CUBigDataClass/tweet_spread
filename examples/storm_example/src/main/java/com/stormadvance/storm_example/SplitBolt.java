package com.stormadvance.storm_example;

public class SplitBolt extends ShellBolt implements IRichBolt {
    public SplitBolt(){
        super("python", "/home/ec2-user/tweet_spread/examples/storm_example/src/main/java/com/stormadvance/storm_example/process.py");
    }
}