# syntax=docker/dockerfile:1

#===================================================
# === Stage 1: Build the Spring Boot application ===
#===================================================
# Use Maven with Amazon Corretto JDK 17.
FROM maven:3.9.6-amazoncorretto-17 as build

# Set the app directory to /app
WORKDIR /app

# Copy Maven wrapper files and pom.xml.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy the source code
COPY src src

# Build the Spring Boot application's executable JAR.
RUN mvn clean package -DskipTests

#========================================
#=== Stage 2: Create the docker image ===
#========================================
# Use Amazon Corretto JDK 17 Alpine for the runtime.
FROM amazoncorretto:17-alpine-jdk

# Set the directory for temporary files.
VOLUME /tmp

# Set the app directory to /app
WORKDIR /app

# Copy the built jar to /app and name it app.jar
COPY --from=build /app/target/*.jar app.jar

# Expose the Spring Boot Application port (8080)
EXPOSE 8080

# When the container start, this command runs to start the application ('java -jar /app/app.jar')
ENTRYPOINT ["java","-jar","/app/app.jar"]