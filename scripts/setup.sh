# Download ZooKeeper
wget http://download.nextag.com/apache/zookeeper/zookeeper-3.4.11//zookeeper-3.4.11.tar.gz
tar -zxf zookeeper-3.4.11.tar.gz
mv zookeeper-3.4.11 zookeeper
rm zookeeper-3.4.11.tar.gz

# Download Storm
wget http://mirror.cc.columbia.edu/pub/software/apache/storm/apache-storm-1.2.1/apache-storm-1.2.1.tar.gz
tar -zxf apache-storm-1.2.1.tar.gz
mv apache-storm-1.2.1 storm
rm apache-storm-1.2.1.tar.gz

# Download Kafka
wget http://apache.mirrors.lucidnetworks.net/kafka/1.1.0/kafka_2.11-1.1.0.tgz
tar -zxf kafka_2.11-1.1.0.tgz
mv kafka_2.11-1.1.0 kafka
rm kafka_2.11-1.1.0.tgz
