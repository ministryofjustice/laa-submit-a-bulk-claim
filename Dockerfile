# Runtime stage
FROM amazoncorretto:25.0.4-alpine3.24 AS base

# Fix SNYK-ALPINE324-ZLIB-20541555 / CVE-2026-85091 (zlib 1.3.2-r1), not yet in the base image
RUN apk upgrade --no-cache zlib

# Set up working directory in the container
RUN mkdir -p /opt/submit-a-bulk-claim/
WORKDIR /opt/submit-a-bulk-claim/

# --- Stage for copying from the local filesystem (CI/Manual) ---
COPY laa-submit-a-bulk-claim-ui/build/libs/laa-submit-a-bulk-claim-ui-*-SNAPSHOT.jar app.jar

# Create a group and non-root user
RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Set the default user
USER 1001

# Expose the port that the application will run on
EXPOSE 8080
# Local port
EXPOSE 8082

# Allow Netty and other unnamed-module native libraries on Java 25+ without warning.
CMD ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
