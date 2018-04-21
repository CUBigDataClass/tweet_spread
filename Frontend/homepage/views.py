from django.shortcuts import render
from . import process_search
import json
import os
from Frontend.mysite.settings import STATIC_ROOT


def index(request):
	json_file = open(os.path.join(STATIC_ROOT, 'homepage/world.json'))
	data = json.load(json_file)
	return render(request, 'homepage/header.html', {"data": data})


def home(request):
	if request.method == 'GET':
		query = request.GET['search']
		if query:
			process_search.connect_kafka(query)
			top_tweets_html = process_search.accept_input_for_processing(query)
			sentiment = process_search.get_sentiment()
			return render(request, 'homepage/search.html', {'query': query, 'top_tweets_html': top_tweets_html,
															'sentiment': sentiment})


