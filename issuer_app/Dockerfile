FROM gradle:jdk17 as build
WORKDIR /workspace/app

COPY ./src /workspace/app/src
COPY ./build.gradle.kts /workspace/app/build.gradle.kts
COPY ./settings.gradle.kts /workspace/app/settings.gradle.kts

RUN gradle --no-daemon build


FROM eclipse-temurin:17-jdk

WORKDIR /workspace/app
COPY --from=build /workspace/app/build/libs/issuer_app-0.0.1-SNAPSHOT.jar /workspace/app/build/libs/issuer_app-0.0.1-SNAPSHOT.jar

RUN mkdir /workspace/app/issuer_secrets


ENTRYPOINT ["java","-jar","/workspace/app/build/libs/issuer_app-0.0.1-SNAPSHOT.jar"]