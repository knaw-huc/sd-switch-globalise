FROM maven:3-openjdk-17-slim AS build

COPY . /app
WORKDIR /app

RUN mvn install -DskipTests

RUN mkdir -p server/target/dependency && (cd server/target/dependency; jar -xf ../server-1.0-SNAPSHOT.jar)

FROM openjdk:17-alpine

#COPY --from=build /app/cp /app/cp
#COPY --from=build /app/mp /app/mp
COPY --from=build /app/server/target/dependency /app

EXPOSE 8080

#ENTRYPOINT ["java", "-cp", "/app/cp/*", "-p", "/app/mp", "-m", "nl.knaw.huc.sdswitch.server/nl.knaw.huc.sdswitch.server.Application"]
ENTRYPOINT ["java", "-cp", "/app", "nl.knaw.huc.sdswitch.server.Application"]
