#!/bin/sh

# launch sidecar
nohup /vproxy                                \
    resp-controller 0.0.0.0:16379 mypassw0rd \
    noStdIOController                        \
    sigIntDirectlyShutdown                   \
    serviceMeshConfig /sidecar.conf          \
    noLoadLast                               \
                                             \
    2>&1 > /vproxy.log &

# launch service b
java -cp /example.jar net.cassite.vproxy.example.servicemesh.Service b
