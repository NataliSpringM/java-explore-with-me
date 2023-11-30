FROM amazoncorretto:11
VOLUME /tmp
COPY target/*.jar statsApp.jar
ENTRYPOINT ["java","-jar","statsApp.jar"]