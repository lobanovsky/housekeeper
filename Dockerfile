FROM gradle:9.2.1-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/housekeeper.jar app.jar

VOLUME ["/app/data"]

CMD ["java", "-jar", "app.jar"]
