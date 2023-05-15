FROM eclipse-temurin:17-alpine

WORKDIR /opt/app

COPY /build/libs/*.jar app.jar

RUN chown -R 1000:1000 /opt/app
USER 1000

ENTRYPOINT java -jar /opt/app/app.jar
