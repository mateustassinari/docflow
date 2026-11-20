# Backend do DocFlow (Spring Boot). Os testes rodam fora da imagem: ./gradlew test
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Dependências primeiro, para o cache do Docker reaproveitar quando só o código muda
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S docflow && adduser -S docflow -G docflow
WORKDIR /app
COPY --from=build /workspace/build/libs/docflow-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads && chown -R docflow:docflow /app
USER docflow

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
