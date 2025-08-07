FROM amazoncorretto:21-alpine
RUN apk update && apk add --no-cache curl

WORKDIR /app
COPY build/libs/submit-a-bulk-claim.jar ./app.jar
RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup
USER 1001
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]
