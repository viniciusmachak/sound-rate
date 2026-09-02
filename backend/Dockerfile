FROM maven:3.9.16-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src/ ./src/

RUN mvn -B package -DskipTests

FROM eclipse-temurin:21.0.12_8-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S soundrate && adduser -S soundrate -G soundrate

COPY --from=build --chown=soundrate:soundrate /app/target/*.jar app.jar

USER soundrate

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
