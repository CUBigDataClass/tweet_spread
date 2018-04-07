from __future__ import absolute_import
from .version import *


__title__ = 'kafka'
__author__ = 'Dana Powers'
__license__ = 'Apache License 2.0'
__copyright__ = 'Copyright 2016 Dana Powers, David Arthur, and Contributors'

# Set default logging handler to avoid "No handler found" warnings.
import logging
try:  # Python 2.7+
    from logging import NullHandler
except ImportError:
    class NullHandler(logging.Handler):
        def emit(self, record):
            pass

logging.getLogger(__name__).addHandler(NullHandler())


from .consumer import KafkaConsumer
from .consumer.subscription_state import ConsumerRebalanceListener
from .producer import KafkaProducer
from .conn import BrokerConnection
from .protocol import (
    create_message, create_gzip_message, create_snappy_message)
from .partitioner import RoundRobinPartitioner, HashedPartitioner, Murmur2Partitioner
from .structs import TopicPartition, OffsetAndMetadata
#from .serializer import Serializer, Deserializer

# To be deprecated when KafkaProducer interface is released
from .client import SimpleClient
from .producer import SimpleProducer, KeyedProducer

# deprecated in favor of KafkaConsumer
from .consumer import SimpleConsumer, MultiProcessConsumer


import warnings


class KafkaClient(SimpleClient):
    def __init__(self, *args, **kwargs):
        warnings.warn('The legacy KafkaClient interface has been moved to'
                      ' kafka.SimpleClient - this import will break in a'
                      ' future release', DeprecationWarning)
        super(KafkaClient, self).__init__(*args, **kwargs)


__all__ = [
    'KafkaConsumer', 'KafkaProducer', 'KafkaClient', 'BrokerConnection',
    'SimpleClient', 'SimpleProducer', 'KeyedProducer',
    'RoundRobinPartitioner', 'HashedPartitioner',
    'create_message', 'create_gzip_message', 'create_snappy_message',
    'SimpleConsumer', 'MultiProcessConsumer',
]
