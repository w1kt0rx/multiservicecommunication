FROM maven:3.9-eclipse-temurin-21
WORKDIR /proxy
COPY ../target/test-0.0.1-SNAPSHOT.jar /proxy/test-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/proxy/test-0.0.1-SNAPSHOT.jar"]