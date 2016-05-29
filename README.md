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

```
sbt> assembly
```

Upload the fat-jar and set `net.shiroka.tools.ofx.aws.Lambda::handler` as an entry point.
