# ofx-tools

## Deploying to AWS Lambda

```
sbt> assembly
```

Upload the fat-jar and set `net.shiroka.tools.ofx.Lambda::handler` as an entry point.
