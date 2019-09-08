#!/usr/bin/env bash

# stop frontend
docker rm 'example-frontend' -f

# start service a
docker rm 'example-service-a' -f

# start service b
docker rm 'example-service-b' -f

# start service a2
docker rm 'example-service-a2' -f
