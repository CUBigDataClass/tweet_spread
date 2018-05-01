from django.shortcuts import render
from django.http import HttpResponse,JsonResponse
from . import process_search
import json
import time


def topic_model(request):
	# amruta : Todo .. pass this to the front end.
	'''
	   var topic_json = {"t1":[{"text":"donald", weight:10}, {"text":"president", weight:20}], "t2":[{"text":"hi", weight:5}], "t3" : {"text":"hiiiiiii", weight:15}};
	'''
	pass


def index(request):
	# This below json is wrong.. this is not how we accept it now. We need a new format, as shown in the JS
	my_json = [{"coords": [-63.2425206, -32.4079042],"frequency": 9},{"coords": [12.57994249, 55.68087366],"frequency": 3}];
	#my_json  = {"test":123};
	#js_data = json.dumps(my_json)		
	return render(request, 'homepage/header.html', {'my_json': my_json})


def home(request):

	if request.is_ajax():
		query_topic = request.GET['search']
		requester = request.GET['requester']
		if requester == "topicmodel":
			topics = process_search.get_topics(query_topic)
			return HttpResponse(topics)
		elif requester == "setinterval":
			sentiments = process_search.get_sentiment(query_topic)
			json_acceptable_string = sentiments.replace("'", "\"")
			return HttpResponse(json_acceptable_string)
		elif requester == "setgeointerval":
			geoparsed = process_search.get_geoparse(query_topic)
		elif requester == "setmilesinterval":
			milestones = process_search.get_milestones(query_topic)
			json_milestones_string = milestones.replace("x", "\"x\"")
			json_milestones_string = json_milestones_string.replace("y", "\"y\"")
			return HttpResponse(json_milestones_string)

	if request.method == 'GET':
		query = request.GET['search']
		if query:
			mode = "Fetched from cassandra"
			sentiment = process_search.get_sentiment(query)
			#geoparsed = process_search.get_geoparse(query)
			# top_tweets_1, top_tweets_2 = process_search.get_top_tweets(query)
			topic_models = process_search.get_topics(query)
			milestones = process_search.get_milestones(query, 1)
			if sentiment is None:
				mode = "Fetched from kafka"
				process_search.connect_kafka(query)
				# top_tweets_1, top_tweets_2 = process_search.get_top_tweets(query)
				while sentiment is None or geoparsed is None or topic_models is None or milestones is None:
					sentiment = process_search.get_sentiment(query)
					geoparsed = process_search.get_geoparse(query)
					topic_models = process_search.get_topics(query)
					milestones = process_search.get_milestones(query, 1)
			# return render(request, 'homepage/search.html', {'query': query, 'sentiment': sentiment, 'mode': mode,
			# 'top_tweets_1': top_tweets_1, 'top_tweets_2': top_tweets_2, 'topic_models': topic_models})
			return render(request, 'homepage/search.html', {'query': query, 'sentiment': sentiment, 'mode': mode,
			 'topic_models': topic_models, 'milestones': milestones})





