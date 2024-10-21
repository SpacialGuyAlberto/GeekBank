FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiar el script de espera y el jar de la aplicación
COPY wait-for-it.sh /app/wait-for-it.sh
COPY build/libs/GeekBank-0.0.1-SNAPSHOT.jar /app/app.jar

# Instalar netcat solo si es necesario para la ejecución
RUN apt-get update && \
    apt-get install -y --no-install-recommends netcat-openbsd && \
    rm -rf /var/lib/apt/lists/* && \
    chmod +x /app/wait-for-it.sh

# Ejecutar la aplicación
CMD ["/app/wait-for-it.sh", "postgres", "5432", "--", "java", "-jar", "/app/app.jar"]
