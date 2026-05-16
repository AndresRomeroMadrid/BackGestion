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

FROM public.ecr.aws/lambda/java:17

COPY --from=build /function.jar ${LAMBDA_TASK_ROOT}/app.jar

CMD ["com.example.BackGestion.StreamLambdaHandler::handleRequest"]
