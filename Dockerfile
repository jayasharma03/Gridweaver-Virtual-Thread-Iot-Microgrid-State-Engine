FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw.cmd pom.xml ./
RUN chmod +x mvnw.cmd
RUN ./mvnw.cmd -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw.cmd -q -DskipTests package

FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown -R spring:spring /app
USER spring

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
