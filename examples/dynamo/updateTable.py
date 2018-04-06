import boto3

dynamodb = boto3.resource('dynamodb', region_name='us-west-2')
table = dynamodb.Table('staff')

table.update_item(
    Key={
        'username': 'ruanb',
        'last_name': 'bekker'
    },
    UpdateExpression='SET age = :val1',
    ExpressionAttributeValues={
        ':val1': 29
    }
)