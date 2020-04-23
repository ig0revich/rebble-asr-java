FROM openjdk:8
VOLUME /tmp
ARG JAR_FILE
RUN apt-get update && apt-get install sox --yes
COPY target/${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]