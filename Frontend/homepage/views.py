from django.shortcuts import render
from . import process_search
import logging
log = logging.getLogger(__name__)


def index(request):
	return render(request, 'homepage/header.html')


def home(request):
	if request.method == 'GET':
		query = request.GET['search']
		if query:
                        log.debug("Hey there it works!!")
			top_tweets_html = process_search.accept_input_for_processing(query)
			sentiment = process_search.get_sentiment()
			return render(request, 'homepage/search.html', {'query': query, 'top_tweets_html': top_tweets_html,
															'sentiment': sentiment})


