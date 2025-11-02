# etapa build con caché de Gradle
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.gradle \
    gradle clean bootJar --no-daemon

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /workspace/build/libs/GeekBank-0.0.1-SNAPSHOT.jar app.jar
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]
