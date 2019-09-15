#!/usr/bin/env bash

echo "make sure all the services are started"
docker start example-frontend
docker start example-service-a
docker start example-service-a2
docker start example-service-b

echo "wait for some time to let them find each other and register nodes"
echo "wait for 25s"
sleep 5s
echo "wait for 25s, 20s left"
sleep 5s
echo "wait for 25s, 15s left"
sleep 5s
echo "wait for 25s, 10s left"
sleep 5s
echo "wait for 25s, 5s left"
sleep 5s

echo "request service a"
echo "two nodes in service a, so we can get two different responses"
echo "one node is set to weight 15, another is set to weight 10"
echo "so the response count of a1:a2 will be 3:2"
curl localhost:8080/service-a
sleep 0.7s
curl localhost:8080/service-a
sleep 0.7s
curl localhost:8080/service-a
sleep 0.7s
curl localhost:8080/service-a
sleep 0.7s
curl localhost:8080/service-a
sleep 0.7s
curl localhost:8080/service-a

sleep 1s

echo "request service b"
echo "only one node in service b, so we can only get the same responses"
curl localhost:8080/service-b
sleep 0.7s
curl localhost:8080/service-b
sleep 0.7s

echo "stop one of the nodes in service-a"
docker stop example-service-a2 &
docker_stop_pid=$!
sleep 0.5s
echo "and request service-a before it's acutally stopped"
echo "no failure should appear, and now we can only get response from one node"
curl localhost:8080/service-a
sleep 1s
curl localhost:8080/service-a
sleep 1s

wait $docker_stop_pid
