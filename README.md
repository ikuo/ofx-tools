# ofx-tools

## Running in CLI

```
$ sbt runMain net.shiroka.tools.ofx.<class-name> <account-number> <input-file> <output-location>
```

Example:

```
$ sbt runMain net.shiroka.tools.ofx.ShinseiBankOfxGeneration 6300215825 src/test/resources/shinsei-bank.txt target/out.ofx
```

## Deploying to AWS Lambda

To reduce jar size with proguard, run the following with Java 7 (assuming an `brew versions` environment in Mac OS):

```
JAVA_HOME=$(/usr/libexec/java_home -v 1.7) sbt
sbt> proguard:proguard
```

Upload the generated jar and set `net.shiroka.tools.ofx.aws.Lambda::handler` as an entry point.
