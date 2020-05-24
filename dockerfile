FROM openjdk:8-jre-alpine

VOLUME /tmp

ARG JAR_FILE=alltv.jar
ARG PORT=8888
ARG TIME_ZONE=Asia/Shanghai

ENV TZ=${TIME_ZONE}
ENV JAVA_OPTS="-Xms256m -Xmx256m"

COPY ${JAR_FILE} alltv.jar

EXPOSE ${PORT}

ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -server -jar alltv.jar
