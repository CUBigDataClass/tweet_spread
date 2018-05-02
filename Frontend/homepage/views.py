from django.shortcuts import render
from django.http import HttpResponse,JsonResponse
from . import process_search
import json
import time


def index(request):
	return render(request, 'homepage/header.html')


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
			return HttpResponse(geoparsed)
		elif requester == "setmilesinterval":
			milestones = process_search.get_milestones(query_topic)
			return HttpResponse(json.dumps(milestones), content_type="application/json")

	if request.method == 'GET':
		query = request.GET['search']
		if query:
			mode = "Fetched from cassandra"
			sentiment = process_search.get_sentiment(query)
			geoparsed = process_search.get_geoparse(query)
			#top_tweets_1, top_tweets_2 = process_search.get_top_tweets(query)
			topic_models = process_search.get_topics(query)
			milestones = process_search.get_milestones(query, 1)
			if sentiment is None:
				mode = "Fetched from kafka"
				process_search.connect_kafka(query)
				#top_tweets_1, top_tweets_2 = process_search.get_top_tweets(query)
				while sentiment is None or geoparsed is None or topic_models is None or milestones is None:
					sentiment = process_search.get_sentiment(query)
					geoparsed = process_search.get_geoparse(query)
					topic_models = process_search.get_topics(query)
					milestones = process_search.get_milestones(query, 1)
			return render(request, 'homepage/search.html', {'query': query, 'sentiment': sentiment, 'mode': mode,
			'top_tweets_1': top_tweets_1, 'top_tweets_2': top_tweets_2, 'topic_models': topic_models,
			'milestones': milestones, 'geoparsed': geoparsed})





