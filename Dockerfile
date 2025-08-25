FROM maven:3.9.9-eclipse-temurin-23-noble
WORKDIR /app
COPY pom.xml /app
COPY src /app/src

RUN mvn package -DskipTests
CMD ["java", "-jar", "target/cards-0.0.1-SNAPSHOT.jar"]