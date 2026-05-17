FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=builder /app/target/search-service-0.0.1-SNAPSHOT.jar /app/search-service.jar

EXPOSE 5020

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/search-service.jar"]
