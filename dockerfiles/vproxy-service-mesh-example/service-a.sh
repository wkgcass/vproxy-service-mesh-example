#!/bin/bash

term_func() {
  pid=$1
  echo "shell received SIGTERM, will kill $pid"
  kill -SIGTERM $pid
  wait $pid
}

pid_sidecar=
pid_java=

# launch sidecar
nohup java -jar /vproxy.jar                  \
    noLoadLast noSave                        \
    resp-controller 0.0.0.0:16379 mypassw0rd \
    http-controller 0.0.0.0:18080            \
    noStdIOController                        \
    sigIntDirectlyShutdown                   \
    discoveryConfig /discovery.conf          \
                                             \
    &
pid_sidecar=$!

# launch service a
java -cp /example.jar net.cassite.vproxy.example.servicemesh.Service a &
pid_java=$!

# handle signal
trap "term_func $pid_java" TERM
wait $pid_java
