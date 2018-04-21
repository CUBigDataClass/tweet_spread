from django.shortcuts import render
from . import process_search
import json


def topic_model(request):
	# amruta : Todo .. pass this to the front end.
	topic_json = {1:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	2:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	3:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	4:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	5:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	6:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	7:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	8:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	9:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 },
	10:{"word1":10, "word2":20, "word3":30, "word4":40, "word5":50, "word6":60, "word7":70, "word8":80, "word9":90, "word10":100 }
	}

def index(request):
	my_json = [{"coords": [-63.2425206, -32.4079042],"frequency": 9},{"coords": [12.57994249, 55.68087366],"frequency": 3}];
	#my_json  = {"test":123};
	#js_data = json.dumps(my_json)		
	return render(request, 'homepage/header.html', {'my_json': my_json});


def home(request):
	if request.method == 'GET':
		query = request.GET['search']
		if query:
			process_search.connect_kafka(query)
			top_tweets_html = process_search.accept_input_for_processing(query)
			sentiment = process_search.get_sentiment()
			return render(request, 'homepage/search.html', {'query': query, 'top_tweets_html': top_tweets_html,
															'sentiment': sentiment})


