import os
import requests
import time
import json
from requests.auth import HTTPBasicAuth

def batch_search(topic, n, ans):
	if n != '0':
		url = 'https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json?query='+topic+'&next='+n+'&maxResults=500'
		# r = requests.get('https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json?query='+topic+'&maxResults='+str(length), auth=HTTPBasicAuth('sayali.sonawane@colorado.edu', 'HashtagChronicles'))
	else:
		url = 'https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json?query='+topic+'&maxResults=500'
	
	r = requests.get(url , auth=HTTPBasicAuth('sayali.sonawane@colorado.edu', 'HashtagChronicles'))
	results = json.loads(r.content)

	
	ans['results'] = results['results']
	if 'next' in results:
		# print(url)
		# print(len(ans['results']))
		print(results['requestParameters'])
		n = results['next']
		results = batch_search(topic, n, ans)
		ans['results'] += results['results']
	print(len(ans['results']))
	return ans


if __name__ == '__main__':
	
	topic = 'CU Boulder'
	maxResults = 10
	
	results = batch_search(topic, '0', {})
	# for i in results['results']:
	# 	print(i)
		# time.sleep(0.01)
	print(len(results['results']))

# import urllib2
# import base64
# import json
# import sys

# class RequestWithMethod(urllib2.Request):
#     def __init__(self, url, method, headers={}):
#         self._method = method
#         urllib2.Request.__init__(self, url, headers)

#     def get_method(self):
#         if self._method:
#             return self._method
#         else:
#             return urllib2.Request.get_method(self) 

# def batch_search(topic, n):
# 	url = 'https://gnip-api.twitter.com/search/fullarchive/accounts/greg-students/prod.json'
# 	UN = 'sayali.sonawane@colorado.edu'
# 	PWD = 'HashtagChronicles'
# 	query = 'gnip'
# 	if n != '0':
# 		queryString = url + '?query=' + query + '&next=' + n
# 	else:
# 		queryString = url + '?query=' + query
# 	base64string = base64.encodestring('%s:%s' % (UN, PWD)).replace('\n', '')
	
# 	req = RequestWithMethod(queryString, 'GET')
# 	req.add_header("Authorization", "Basic %s" % base64string)
	
# 	try:
# 		response = urllib2.urlopen(req)
# 	except urllib2.HTTPError as e:
#         	print e.read()
        	
# 	the_page = response.read()
# 	results = json.loads(the_page)

# 	ans = {}
# 	ans['results'] = results['results']
# 	if 'next' in results:
# 		print(results['next'])
# 		n = results['next']
# 		results = batch_search(topic, n)
# 		ans['results'] += results['results']

# 	return ans


# if __name__ == "__main__":

# 	n = "0"
# 	topic = 'Donald Trump'
# 	results = batch_search(topic, n)
	
# 	print(len(results['results']))
# 	