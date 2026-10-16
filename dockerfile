FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw \
    && ./mvnw -B -ntp package


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app \
    && adduser -S -G app app

ENV APP_PORT=3001 \
    CSV_PATH=/app/data/compras.csv \
    APP_ENV=production

COPY --from=build --chown=app:app /build/target/*.jar app.jar

USER app

EXPOSE 3001

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]