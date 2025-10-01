# Specify java runtime base image
FROM amazoncorretto:21-alpine

# Update vulnerable system libraries
RUN apk update && apk upgrade --no-cache && apk add --no-cache openssl

# Set up working directory in the container
RUN mkdir -p /opt/laa-submit-a-bulk-claim-ui/
WORKDIR /opt/laa-submit-a-bulk-claim-ui/

# Copy the JAR file into the container
COPY laa-submit-a-bulk-claim-ui/build/libs/laa-submit-a-bulk-claim-service-1.0.0-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the JAR file
CMD java -jar app.jar