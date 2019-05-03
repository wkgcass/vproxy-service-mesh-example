#!/bin/sh

java -jar /vproxy.jar                        \
    resp-controller 0.0.0.0:16379 mypassw0rd \
    noStdIOController                        \
    sigIntDirectlyShutdown                   \
    serviceMeshConfig /service-mesh.conf     \
    load /vproxy.conf
