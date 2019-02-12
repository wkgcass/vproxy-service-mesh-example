#!/bin/sh

/vproxy                                      \
    resp-controller 0.0.0.0:16379 mypassw0rd \
    noStdIOController                        \
    sigIntDirectlyShutdown                   \
    serviceMeshConfig /autolb.conf           \
    noLoadLast
