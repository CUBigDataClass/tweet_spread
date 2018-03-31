from search import batch_search
import time

if __name__ == "__main__":
	n = "0"
	topic = 'Cu Boulder'
	results = batch_search(topic, n)
	
	for i in results['results']:
		print(i)
		time.sleep(0.01)
	
