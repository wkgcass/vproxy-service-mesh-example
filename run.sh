#!/usr/bin/env bash

# start lb
docker run --name 'example-lb' -d vproxy-service-mesh-example /lb.sh

# start frontend
docker run --name 'example-frontend' -d -p 8080:8080 vproxy-service-mesh-example /frontend.sh

# start service a
docker run --name 'example-service-a' -d vproxy-service-mesh-example /service-a.sh

# start service b
docker run --name 'example-service-b' -d vproxy-service-mesh-example /service-b.sh

# start service a2
docker run --name 'example-service-a2' -d vproxy-service-mesh-example /service-a.sh
