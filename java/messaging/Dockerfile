FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY ./ ./

RUN ["./gradlew", "build", "--no-daemon"]

CMD ["./gradlew", ":app:run"]
