/**
 * AWS Lambda function to dispatch S3 events.
 * It extracts bucket and key and calls another lambda function with `s3://${bucket}/${key}`.
 */

'use strict';
console.log('Loading function');
const aws = require('aws-sdk');
const targetFunction = 'convertToOfx';

exports.handler = (event, context, callback) => {
    //console.log('Received event:', JSON.stringify(event, null, 2));
    const bucket = event.Records[0].s3.bucket.name;
    const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));
    const uri = `s3://${bucket}/${key}`;

    console.log(`Dispatching ${uri} to ${targetFunction}`);
    new aws.Lambda().invoke({
        FunctionName: targetFunction,
        Payload: `"${uri}"`,
        InvocationType: 'Event'
    }).promise().then((data) => {
        console.log(`Invoked ${targetFunction} with result ${JSON.stringify(data)}`);
        callback(null, uri);
    });
};
