# Catalog

A non-blocking server for catalog crud operations

disable JMX settings in intellij https://stackoverflow.com/a/57434060/4841710

## Load Test

[vegeta](https://github.com/tsenart/vegeta)

The script below produces names like: `f7d43c71`

```shell
od -t x -An /dev/random | \
  tr -d " " | \
  fold -w 8 | \
  jq -ncMR 'while(true; .+1) | {method: "POST", url: "http://localhost:8080/context/api/item", body: {name: range(0;5) | input} | @base64, header: {"Content-Type": ["application/json"]} }' | \
  vegeta attack -rate=200/s -lazy -format=json -duration=1s | \
  tee results.bin | \
  vegeta report
```

