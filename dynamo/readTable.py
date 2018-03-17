import boto3

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('staff')

response = table.get_item(
   Key={
        'username': 'ruanb',
        'last_name': 'bekker'
    }
)

item = response['Item']
name = item['first_name']

print(item)
print("Hello, {}" .format(name))