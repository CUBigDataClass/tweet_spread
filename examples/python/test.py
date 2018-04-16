from search import batch_search
from producer import produce
import time
import requests
from requests import put, get


if __name__ == "__main__":
        n = "0"
	topic = 'CU Boulder'
	url = 'http://ec2-54-218-84-101.us-west-2.compute.amazonaws.com:5000/' + topic
	print(url)
	get(url).json()
	print("a")
	# produce(topic)
        
	
	# results = batch_search(topic, '0', {})
	# for i in results['results']:
	# 	print(i)
		# time.sleep(0.01)
