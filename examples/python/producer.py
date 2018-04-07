#!/usr/bin/env python
import threading, logging, time
import multiprocessing
import json
from kafka import KafkaConsumer, KafkaProducer
from search import batch_search

class Producer(threading.Thread):
    def __init__(self, topic):
        threading.Thread.__init__(self)
        self.stop_event = threading.Event()
        self.topic = topic

    def stop(self):
        self.stop_event.set()
    
    def run(self):
        producer = KafkaProducer(bootstrap_servers='localhost:9092')
        topic = self.topic

        # while not self.stop_event.is_set():
            #producer.send('0403', b"test")
            #time.sleep(1)

        # r = batch_search(topic, '0', {})
        # while not self.stop_event.is_set():
        #     for i in r['results']:
        #         producer.send('0403', json.dumps(i))

        destination = '0407_1'
        r = batch_search(topic, '0', {})
        for i in r['results']:
            producer.send(destination, json.dumps(i))
        # while not self.stop_event.is_set():
        while 'next' in r:
            n = results['next']
            r = batch_search(topic, n, {})
            for i in r['results']:
                producer.send(destination, json.dumps(i))

        producer.close()

def produce(topic):
    tasks = [
        Producer(topic)
    ]

    for t in tasks:
        t.start()

    # time.sleep(10)

    # for task in tasks:
    #     task.stop()

    for task in tasks:
        task.join()


if __name__ == "__main__":
    logging.basicConfig(
        format='%(asctime)s.%(msecs)s:%(name)s:%(thread)d:%(levelname)s:%(process)d:%(message)s',
        level=logging.INFO
        )
    main()
