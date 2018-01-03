# ofx-tools

Utilities to generate [OFX](http://www.ofx.net/) files.

## Running in CLI

```
$ sbt run <conversion-name> <input-file> [<output-file>]
```

Examples:

```
$ sbt run convert shinsei-bank src/test/resources/shinsei-bank.csv target/out.ofx
```

```
$ sbt run convert shinsei-bank s3://mybucket/shinsei-bank.csv -
# prints OFX to stdout
```

```
$ sbt run convert shinsei-bank s3://mybucket/shinsei-bank.csv
# uploads result to s3://mybucket/shinsei-bank.ofx
```

See src/main/resources/reference.conf for configuration.

## Deploying to AWS Lambda

Prepare src/main/resources/application.conf to override config if any.

To reduce jar size, run proguard as follows (assuming `brew versions` environment in Mac OS):

```
$ rm -rf ./project/target ./target
JAVA_HOME=$(/usr/libexec/java_home -v 1.8) sbt
sbt> proguard:proguard
```

Upload the generated jar and set `net.shiroka.tools.ofx.aws.Lambda::handler` as an entry point.

Optionally, test the jar as follows:

```
java -classpath target/scala-2.11/proguard/ofx-tools_2.11-<version>.jar \
  net.shiroka.tools.ofx.Cli \
  convert shinsei-bank s3://mybucket/reports/shinsei-bank/1001111111/1.csv -
```

Then set up [dispatch-s3-events.js](src/main/javascript/dispatch-s3-events.js) as another Lambda function to extract S3 path
and pass it to the main Lambda function above.
