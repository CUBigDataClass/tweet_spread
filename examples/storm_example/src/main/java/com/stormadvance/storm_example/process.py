import storm

class SplitBoltPython(storm.BasicBolt):

    def initialize(self, conf, context):
        self._conf = conf;
        self._context = context;

    def process(self, tuple):
        word = tuple.values[0]
        word += "a"
        print("######### Name of input site is : " + word)
        storm.emit([word])  # return list object

SplitBoltPython().run()
