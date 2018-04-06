import boto3

dynamodb = boto3.resource('dynamodb', region_name='us-west-2')
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