from twitter import *
import simplejson as json
from cassandra.cluster import Cluster
import requests
from requests import put, get
import re

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
	query_string = "select positive_sentiments, negative_sentiments, neutral_sentiments from tweetanalysis.sentiments where hashtag='"+topic+"'"
	result = session.execute(query_string)
	cluster.shutdown()
	final_res = [0]*3
	if result:
		final_res[0] = result[0][0]
		final_res[1] = result[0][1]
		final_res[2] = result[0][2]
		final_res = [(x/float(sum(final_res)))*100 for x in final_res]
		my_json = str([{'y': final_res[0], 'label': "POS"}, {'y': final_res[1], 'label': "NEG"}, {'y': final_res[2], 'label': "NEU"}])
		return my_json
	else:
		return None


def get_topics(topic):
	cluster = Cluster(['54.245.62.87'])
	session = cluster.connect()
	query_string = "select topic from tweetanalysis.topicmodeling where hashtag='"+topic+"'"
	result = session.execute(query_string)
	cluster.shutdown()
	final_res = []
	if result:
		for elem in result:
			for i in elem:
				final_res.append(i)
	return final_res


def get_geoparse(topic):
	cluster = Cluster(['54.245.62.87'])
	session = cluster.connect()
	query_string = "select locations from tweetanalysis.geoparsing where hashtag='"+topic+"'"
	result = session.execute(query_string)
	cluster.shutdown()
	final_res = ""
	if result:
		final_res = ','.join(result[0][0])
		final_res = "{" + final_res + "}"
	return final_res


def get_milestones(topic):
	cluster = Cluster(['54.245.62.87'])
	session = cluster.connect()
	milestone_json = []
	for day in range(31, 20, -1):
		for hour in range(23, -1, -1):
			query = "select count from tweetanalysis.hashtag_milestones where " \
			        "hashtag = 'techcrunch' and year = 2018 and month = 3 and " \
			        "day = " + str(day)+ " and hour = " + str(hour)
			milestone_json.append("query fired")
			result = session.execute(query)
			if result:
				milestone_json.append(result)
			else:
				milestone_json.append(query)
	cluster.shutdown()
	return milestone_json


def get_top_tweets(search_string):
	tweets_file_1 = t.search.tweets(q=search_string, result_type='recent', lang='en', count=5)
	tweets_file_2 = t.search.tweets(q=search_string, result_type='popular', lang='en', count=5)
	top_tweets_1 = []
	top_tweets_2 = []
	if tweets_file_1:
		status_1 = tweets_file_1['statuses']
		for tweet in status_1:
			tweet_id = tweet['id']
			top_tweets_1.append(t.statuses.oembed(_id=tweet_id, omit_script=True)['html'])
	if tweets_file_2:
		status_2 = tweets_file_1['statuses']
		for tweet in status_2:
			tweet_id = tweet['id']
			top_tweets_2.append(t.statuses.oembed(_id=tweet_id, omit_script=True)['html'])
		return top_tweets_1, top_tweets_2





