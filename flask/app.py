from flask import Flask, request
from flask.ext.restful import Resource, Api
from producer import produce
import thread

app = Flask(__name__)
api = Api(app)

class TodoSimple(Resource):
    def get(self, key):
        thread.start_new_thread(produce, (key, ))
        # produce(key)
        return {key: key}

api.add_resource(TodoSimple, '/<string:key>')

if __name__ == '__main__':
    app.run(debug=True)
