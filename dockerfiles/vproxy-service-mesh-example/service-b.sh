#!/bin/sh

# launch sidecar
nohup java -Deploy=Sidecar -jar /vproxy.jar  \
    resp-controller 0.0.0.0:16379 mypassw0rd \
    noStdIOController                        \
    sigIntDirectlyShutdown                   \
    serviceMeshConfig /service-mesh.conf     \
                                             \
    2>&1 > /vproxy.log &

# launch service b
java -cp /example.jar net.cassite.vproxy.example.servicemesh.Service b
