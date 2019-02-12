# vproxy service mesh example

## vproxy

[https://github.com/wkgcass/vproxy](https://github.com/wkgcass/vproxy)

## requirements

* make sure you have `gradle` and `docker` commands.

## run

```
./make.sh
./run.sh

# wait for a while
curl localhost:8080/service-a
curl localhost:8080/service-b
```
