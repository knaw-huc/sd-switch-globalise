FROM maven:3-openjdk-17-slim AS build

COPY . /app
WORKDIR /app

RUN mvn install -DskipTests

FROM openjdk:17-alpine

COPY --from=build /app/cp /app/cp
COPY --from=build /app/mp /app/mp

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/app/cp/*", "-p", "/app/mp", "-m", "org.knaw.huc.sdswitch.server/org.knaw.huc.sdswitch.server.Application"]
