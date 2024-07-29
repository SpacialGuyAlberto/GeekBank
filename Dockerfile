FROM openjdk:21-jdk-slim
WORKDIR /app
COPY wait-for-it.sh /app/wait-for-it.sh
COPY build/libs/GeekBank-0.0.1-SNAPSHOT.jar /app/app.jar
RUN apt-get update && apt-get install -y netcat-openbsd && chmod +x /app/wait-for-it.sh
CMD ["/app/wait-for-it.sh", "postgres", "5432", "--", "java", "-jar", "/app/app.jar"]
