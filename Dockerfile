FROM openjdk:22-slim

WORKDIR /app

COPY pom.xml .

COPY src ./src

COPY target ./target

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/MockDataService-1.0-SNAPSHOT.jar"]