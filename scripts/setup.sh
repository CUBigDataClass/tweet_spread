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
wget http://mirrors.sorengard.com/apache/kafka/1.1.0/kafka-1.1.0-src.tgz
tar -zxf kafka-1.1.0-src.tgz
mv kafka-1.1.0-src kafka
rm kafka-1.1.0-src.tgz

# Set ZooKeeper environment
export ZK_HOME=~/zookeeper
cp tweet_spread/config/zoo.cfg ./zookeeper/conf/
sudo mkdir /var/zookeeper
sudo chmod 777 /var/zookeeper
