from twitter import *
import simplejson as json
from cassandra.cluster import Cluster
import requests
from requests import put, get

token = '76608965-qok8bHTPepS7k0gGtbBg7tNHVtS6XgpbCL7kT8TDt'
token_secret = 'KvjPjxjRbuqs08dqABMgzkl5zCJIDGNt1sOuisdhMUEM0'
consumer_key = 'dKMoOcOFb5VhIiEDb7jn1qG0E'
consumer_secret = 'FSQPvpXy3YS3O9c19qysMibq4xNtzzYgzXX18nOFfRTaWYTXdY'

t = Twitter(
    auth=OAuth(token, token_secret, consumer_key, consumer_secret))


def connect_kafka(topic):
	url = 'http://ec2-54-218-84-101.us-west-2.compute.amazonaws.com:5000/' + topic
	print(url)
	r = get(url).json()
	return r


def get_sentiment(topic):
	cluster = Cluster(['54.245.62.87'])
	session = cluster.connect()
	result = session.execute("select " + topic + "from tweetanalysis.sentiments;")
	cluster.shutdown()
	final_res = []
	for elem in result:
		final_res.append(elem)
	return final_res



# def get_top_tweets(search_string):
# 	tweets_file = t.search.tweets(q=search_string, result_type='recent', lang='en', count=10)
# 	print(tweets_file)
# 	print(type(t))
# 	status = tweets_file['statuses']
# 	top_tweets = []
# 	for tweet in status:
# 		tweet_id = tweet['id']
# 		top_tweets.append(t.statuses.oembed(_id=tweet_id, omit_script=True)['html'])
# 	return top_tweets





