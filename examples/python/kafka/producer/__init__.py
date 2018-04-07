from __future__ import absolute_import

from examples.python.kafka.producer import KafkaProducer
from examples.python.kafka.producer.simple import SimpleProducer
from examples.python.kafka.producer.keyed import KeyedProducer

__all__ = [
    'KafkaProducer',
    'SimpleProducer', 'KeyedProducer' # deprecated
]
