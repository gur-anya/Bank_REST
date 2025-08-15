FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY  src ./src

RUN mvn clean install -DskipTests


FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/target/bank-cards-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
