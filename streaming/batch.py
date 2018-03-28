import os
import requests
from requests.auth import HTTPBasicAuth

if __name__ == '__main__':
	# payload = {'username': 'sayali.sonawane@colorado.edu', 'password': 'HashtagChronicles'}
	# r = requests.get('https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json', params=payload)
	topic = "Donald Trump"
	maxResults = 10
	r = requests.get('https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json?query='+topic+'&maxResults='+str(maxResults), auth=HTTPBasicAuth('sayali.sonawane@colorado.edu', 'HashtagChronicles'))

	for i in r:
		print(i)