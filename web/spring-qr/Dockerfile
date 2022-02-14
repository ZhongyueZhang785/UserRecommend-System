# Ubuntu Linux as the base image
FROM openjdk:8-jre-alpine

# Install the packages by using the default package manager in Ubuntu
#RUN apt-get update && \
#    apt-get -y install default-jre

# Open port 8080
EXPOSE 8080

# Copy the files from the host file system and ADD them to the desired directory inside the container image
ADD ./target/spring-qr-0.0.1-SNAPSHOT.jar /

# Define the command which runs when the container starts
CMD ["java -jar spring-qr-0.0.1-SNAPSHOT.jar"]

# Use Bash as the container's entry point.
ENTRYPOINT ["/bin/sh", "-c"]