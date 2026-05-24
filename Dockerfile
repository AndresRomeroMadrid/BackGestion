FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src

RUN ./mvnw -DskipTests package \
    && set -eux; \
       JAR="$(find target -maxdepth 1 -type f -name '*.jar' ! -name 'original-*' | sort | tail -n 1)"; \
       test -n "$JAR"; \
       cp "$JAR" /function.jar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /function.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
