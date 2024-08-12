FROM openjdk:22-slim

WORKDIR /app

COPY pom.xml .

COPY src ./src

COPY target ./target

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean

RUN mvn clean package -DskipTests

EXPOSE 8081

# Run the jar file
CMD ["java", "-jar", "target/MainSpringBootApp-1.0-SNAPSHOT.jar"]