FROM openjdk:17-ea-33-jdk-slim-buster

WORKDIR /app
COPY ./target/exness-commission-0.0.1.jar /app

CMD ["java", "-jar", "exness-commission-0.0.1.jar"]
