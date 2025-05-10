FROM openjdk:17
VOLUME /tmp
COPY target/bank.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
