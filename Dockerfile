# Maven is provided by the builder image; tests run in the verify job.
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./
RUN mvn -B --no-transfer-progress dependency:go-offline

COPY src/ src/
RUN mvn -B --no-transfer-progress package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app app

COPY --from=build /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
