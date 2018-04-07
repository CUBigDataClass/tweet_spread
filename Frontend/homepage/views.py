from django.shortcuts import render
from . import process_search
from examples.python.producer import produce


def index(request):
	return render(request, 'homepage/header.html')


def home(request):
	if request.method == 'GET':
		query = request.GET['search']
		if query:
			produce(query)
			top_tweets_html = process_search.accept_input_for_processing(query)
			return render(request, 'homepage/search.html', {'query': query, 'top_tweets_html': top_tweets_html})


