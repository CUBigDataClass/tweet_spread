import boto3

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('staff')

table.put_item(
   Item={
        'username': 'ruanb',
        'first_name': 'ruan',
        'last_name': 'bekker',
        'age': 30,
        'account_type': 'administrator',
    }
)