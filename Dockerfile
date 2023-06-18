# Base image
FROM openjdk:17-jdk

# Set working directory
WORKDIR /src

# Copy the JAR file
COPY target/wallet-0.0.1-SNAPSHOT.jar wallet.jar

# Expose the container port
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "wallet.jar"]
