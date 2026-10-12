FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/delta-api-postgres-*.jar /build/app.jar

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --gid 1000 app && useradd --system --gid app --home-dir /app app

COPY --from=build --chown=app:app /build/app.jar ./app.jar

ENV PORT=10000 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:+ExitOnOutOfMemoryError"

USER app

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar --server.address=0.0.0.0 --server.port=${PORT:-10000} --server.forward-headers-strategy=framework"]
