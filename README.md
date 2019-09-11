# vproxy service mesh example

## vproxy

[https://github.com/wkgcass/vproxy](https://github.com/wkgcass/vproxy)

## requirements

* make sure you have `gradle` and `docker` commands.

## explain

The application source code is under `src` directory, it's written in java. Both `Frontend` and `Service` are executable.

The `Frontend` accepts requests, and forward them to corresponding `Service`s. The `Service` will respond with its container id and listening port.

The `vproxy` configurations can be found under `dockerfiles/vproxy-service-mesh-example/` directory: `frontend.vproxy.conf` and `discovery.conf`. Sidecars of both `Frontend` and `Service` require `discovery.conf`, but only the sidecar of `Frontend` require `frontend.vproxy.conf` because the `Service`s here only expose themselves but would not make requests to other nodes.

The `frontend.vproxy.conf` defines how to find services of `my-service-a` and `my-service-b`, and also defines how to handle the incoming requests and redirect them to these services.

The `discovery.conf` defines how the nodes find each other. You may refer to the documentation about `discovery.conf` in vproxy repository.

The `Service` program is designed to be able to automatically register itself to the vproxy discovery network, and also be able to deregister itself before actually exiting, in this way, netflow will not be affected when running `docker stop` commands.

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
