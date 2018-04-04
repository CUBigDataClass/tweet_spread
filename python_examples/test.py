from search import batch_search
from producer import produce
import time

if __name__ == "__main__":
	n = "0"
	topic = 'Cu Boulder'

	produce(topic)

	# results = batch_search(topic, '0', {})
	# for i in results['results']:
	# 	print(i)
		# time.sleep(0.01)
