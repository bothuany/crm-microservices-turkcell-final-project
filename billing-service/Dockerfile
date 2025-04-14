FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/billing-service-0.0.1-SNAPSHOT.jar app.jar

# Add timezone
RUN apk add --no-cache tzdata
ENV TZ=Europe/Istanbul

# Expose ports
EXPOSE 8050
EXPOSE 8052

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
