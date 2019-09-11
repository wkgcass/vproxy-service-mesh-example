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

# you may stop a container
# the node will be automatically
# deregistered before exiting
docker stop example-service-a2
```

## clean

```
./stop.sh
./remove-image.sh
```

## client

I provided a client for you to use in your own project, check `ServiceRegisterClient.java` for more info.
