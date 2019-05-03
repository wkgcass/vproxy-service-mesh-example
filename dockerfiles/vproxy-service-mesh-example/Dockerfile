FROM openjdk:11-jre-slim-stretch

# init wget https
RUN apt-get update
RUN apt-get install -y wget

# fetch vproxy
RUN wget https://github.com/wkgcass/vproxy/releases/download/1.0.0-BETA-1/vproxy-1.0.0-BETA-1.jar
RUN mv vproxy-1.0.0-BETA-1.jar vproxy.jar

# add configs
ADD ./vproxy.conf /vproxy.conf
ADD ./service-mesh.conf /service-mesh.conf

# add runnables
ADD ./example.jar example.jar

# add init scripts
ADD ./lb.sh /lb.sh
RUN chmod +x /lb.sh

ADD ./frontend.sh /frontend.sh
RUN chmod +x /frontend.sh

ADD ./service-a.sh /service-a.sh
RUN chmod +x /service-a.sh

ADD ./service-b.sh /service-b.sh
RUN chmod +x /service-b.sh

ENTRYPOINT ["/bin/bash"]
