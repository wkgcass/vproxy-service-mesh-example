FROM openjdk:11-jre-slim-stretch

# add configs
ADD ./frontend.vproxy.conf /frontend.vproxy.conf
ADD ./discovery.conf /discovery.conf

# add runnables
ADD ./vproxy.jar /vproxy.jar
ADD ./example.jar /example.jar

# add init scripts
ADD ./frontend.sh /frontend.sh
RUN chmod +x /frontend.sh

ADD ./service-a.sh /service-a.sh
RUN chmod +x /service-a.sh

ADD ./service-b.sh /service-b.sh
RUN chmod +x /service-b.sh

ENTRYPOINT ["/bin/bash"]
