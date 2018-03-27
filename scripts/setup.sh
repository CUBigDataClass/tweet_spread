# Install git
sudo yum install git
# Clone our code
git clone https://github.com/CUBigDataClass/tweet_spread.git
# Download Zookeeper
wget http://mirrors.gigenet.com/apache/zookeeper/zookeeper-3.4.11/zookeeper-3.4.11.tar.gz
tar -zxf zookeeper-3.4.11.tar.gz
mv zookeeper-3.4.11 zookeeper
rm zookeeper-3.4.11.tar.gz
