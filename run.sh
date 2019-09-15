#!/usr/bin/env bash

# start frontend
docker run --name 'example-frontend' -d -p 8080:8080 vproxy-service-mesh-example /frontend.sh

# start service a
docker run --name 'example-service-a' -d vproxy-service-mesh-example /service-a.sh 15

# start service b
docker run --name 'example-service-b' -d vproxy-service-mesh-example /service-b.sh 10

# start service a2
docker run --name 'example-service-a2' -d vproxy-service-mesh-example /service-a.sh 10
