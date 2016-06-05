# ofx-tools

Utilities to generate [OFX](http://www.ofx.net/) files.

## Running in CLI

```
$ sbt runMain net.shiroka.tools.ofx.<class-name> <account-number> <input-file> <output-location>
```

Example:

```
$ sbt runMain net.shiroka.tools.ofx.ShinseiBankGeneration 1001111111 src/test/resources/shinsei-bank.csv target/out.ofx
```

## Deploying to AWS Lambda

To reduce jar size, run proguard as follows (assuming `brew versions` environment in Mac OS):

```
JAVA_HOME=$(/usr/libexec/java_home -v 1.7) sbt
sbt> proguard:proguard
```

Upload the generated jar and set `net.shiroka.tools.ofx.aws.Lambda::handler` as an entry point.

Optionally, test the jar as follows:

```
java -classpath target/scala-2.11/proguard/ofx-tools_2.11-<version>.jar \
  net.shiroka.tools.ofx.ShinseiBankGeneration s3://mybucket/reports/shinsei-bank/1001111111/1.csv -
```

Then set up [dispatch-s3-events.js](src/main/javascript/dispatch-s3-events.js) as another Lambda function to extract S3 path
and pass it to the main Lambda function above.
