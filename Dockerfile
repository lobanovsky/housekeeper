FROM gradle:9.2.1-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon


FROM eclipse-temurin:21-jre

WORKDIR /opt/app

# Копируем jar
COPY --from=builder /app/build/libs/housekeeper.jar housekeeper.jar

# Копируем ресурсы (ГЛАВНОЕ — из build/resources/main)

COPY --from=builder /app/build/resources/main/receipt /opt/app/receipts
RUN ls -R  /opt/app/receipts

ENV RECEIPT_BASE_PATH=/opt/app/receipts

VOLUME ["/opt/app/logs", "/opt/app/receipts"]

CMD ["java", "-jar", "housekeeper.jar"]