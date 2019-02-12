FROM openjdk:8u181-jre-slim-stretch

# init wget https
RUN apt-get update
RUN apt-get install -y wget

# fetch vproxy
RUN wget https://github.com/wkgcass/vproxy/releases/download/1.0.0-ALPHA-1/vproxy-1.0.0-ALPHA-1-linux
RUN mv vproxy-1.0.0-ALPHA-1-linux vproxy
RUN chmod +x vproxy

# add configs
ADD ./autolb.conf /autolb.conf
ADD ./sidecar.conf /sidecar.conf

# add runnables
ADD ./example.jar example.jar

# add init scripts
ADD ./autolb.sh /autolb.sh
RUN chmod +x /autolb.sh

ADD ./frontend.sh /frontend.sh
RUN chmod +x /frontend.sh

ADD ./service-a.sh /service-a.sh
RUN chmod +x /service-a.sh

ADD ./service-b.sh /service-b.sh
RUN chmod +x /service-b.sh

ENTRYPOINT ["/bin/bash"]
