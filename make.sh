#!/usr/bin/env bash

set -e

# compile and package
gradle clean shadowJar

# copy the result into dockerfile context
rm -f dockerfiles/vproxy-service-mesh-example/example.jar
mv build/libs/vproxy-service-mesh-example-1.0-SNAPSHOT-all.jar dockerfiles/vproxy-service-mesh-example/example.jar

# make docker image (all in one)
cd dockerfiles/vproxy-service-mesh-example
docker build -t vproxy-service-mesh-example:latest .
