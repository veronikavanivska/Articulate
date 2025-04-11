FROM maven:3.8.4-openjdk-17-slim AS build

# Set workdir
WORKDIR /app

# Copy only the pom first and download dependencies (cache optimization)
COPY pom.xml .

# Leverage Docker cache by downloading dependencies only
#RUN mvn dependency:go-offline -B
RUN --mount=type=cache,id=maven-cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B
# Now copy the rest of the project
COPY src ./src

# Package the application
#RUN mvn clean package -DskipTests
RUN --mount=type=cache,id=maven-cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -B

# ------------ Stage 2: Run ------------
FROM openjdk:17-alpine

# Set working directory
WORKDIR /app

# Copy the jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (optional)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]