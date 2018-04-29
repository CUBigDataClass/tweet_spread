from django.shortcuts import render
from django.http import HttpResponse
from . import process_search
import json


def topic_model(request):
	# amruta : Todo .. pass this to the front end.
	'''
	   var topic_json = {"t1":[{"text":"donald", weight:10}, {"text":"president", weight:20}], "t2":[{"text":"hi", weight:5}], "t3" : {"text":"hiiiiiii", weight:15}};
	'''
	pass


def index(request):
	my_json = [{"coords": [-63.2425206, -32.4079042],"frequency": 9},{"coords": [12.57994249, 55.68087366],"frequency": 3}];
	#my_json  = {"test":123};
	#js_data = json.dumps(my_json)		
	return render(request, 'homepage/header.html', {'my_json': my_json})


def home(request):
	if request.is_ajax():
		query = request.GET['search']
		mode = "Fetched from ajax"
		sentiment = process_search.get_sentiment(query)
		return HttpResponse(json.loads(sentiment), content_type='application/json')

	if request.method == 'GET':
		query = request.GET['search']
		if query:
			mode = "Fetched from cassandra"
			sentiment = process_search.get_sentiment(query)
			if sentiment is None:
				mode = "Fetched from kafka"
				process_search.connect_kafka(query)
				sentiment = process_search.get_sentiment(query)
			return render(request, 'homepage/search.html', {'query': query, 'sentiment': sentiment, 'mode': mode})





