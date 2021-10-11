FROM gradle:7-jdk8 AS build

WORKDIR /build

COPY --chown=gradle:gradle ./src ./src
COPY --chown=gradle:gradle ./build.gradle.kts ./

RUN gradle :jar



FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY --from=build /build/build/libs/build.jar ./main.jar

ENTRYPOINT ["java", "-jar", "/app/main.jar"]
